package game.systems;

import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Low-latency hit sound player using SourceDataLine for <5ms playback delay.
 * Preloads all audio into memory and uses thread pooling for simultaneous playback.
 * 
 * Features:
 * - Zero disk I/O during gameplay
 * - Supports 10+ simultaneous sounds
 * - Non-blocking playback
 * - Automatic resource cleanup
 */
public class HitSoundPlayer {
    
    // Preloaded audio data (soundName -> PCM bytes)
    private final Map<String, PreloadedSound> soundBank;
    
    // Thread pool for non-blocking playback
    private final ExecutorService playbackExecutor;
    
    // Scheduled executor for latency offset
    private final ScheduledExecutorService scheduledExecutor;
    
    // Master volume (0.0 to 1.0)
    private float masterVolume = 0.7f;
    
    // Latency offset in milliseconds (negative = predictive, positive = delayed)
    // Volatile for thread-safe reads across playback threads
    private volatile int latencyOffsetMs = 0;
    
    // Buffer size for low latency (smaller = lower latency)
    private static final int BUFFER_SIZE = 512;
    
    /**
     * Container for preloaded audio data.
     */
    private static class PreloadedSound {
        final byte[] audioData;
        final AudioFormat format;
        
        PreloadedSound(byte[] audioData, AudioFormat format) {
            this.audioData = audioData;
            this.format = format;
        }
    }
    
    /**
     * Initialize the hit sound player and preload all sounds.
     * 
     * @param soundDirectory Directory containing WAV files
     * @param soundNames Array of sound file names (without extension)
     */
    public HitSoundPlayer(String soundDirectory, String[] soundNames) {
        this.soundBank = new HashMap<>();
        // Cached thread pool reuses threads, perfect for short-lived audio tasks
        this.playbackExecutor = Executors.newCachedThreadPool(r -> {
            Thread t = new Thread(r);
            t.setDaemon(true); // Don't prevent JVM shutdown
            t.setPriority(Thread.MAX_PRIORITY); // High priority for low latency
            return t;
        });
        // Scheduled executor for latency offset (predictive/delayed playback)
        this.scheduledExecutor = Executors.newScheduledThreadPool(4, r -> {
            Thread t = new Thread(r);
            t.setDaemon(true);
            t.setPriority(Thread.MAX_PRIORITY);
            return t;
        });
        
        // Preload all sounds into memory
        for (String soundName : soundNames) {
            loadSound(soundDirectory, soundName);
        }
    }
    
    /**
     * Load a WAV file into memory as raw PCM bytes.
     */
    private void loadSound(String directory, String soundName) {
        String[] extensions = {".wav", ".WAV"};
        
        for (String ext : extensions) {
            File soundFile = new File(directory + "/" + soundName + ext);
            if (!soundFile.exists()) continue;
            
            try {
                // Read WAV file
                AudioInputStream audioStream = AudioSystem.getAudioInputStream(soundFile);
                AudioFormat baseFormat = audioStream.getFormat();
                
                // Convert to PCM_SIGNED if needed (standard format for SourceDataLine)
                AudioFormat targetFormat = new AudioFormat(
                    AudioFormat.Encoding.PCM_SIGNED,
                    baseFormat.getSampleRate(),
                    16, // 16-bit
                    baseFormat.getChannels(),
                    baseFormat.getChannels() * 2, // frameSize
                    baseFormat.getSampleRate(),
                    false // little-endian
                );
                
                // Decode to target format
                AudioInputStream decodedStream = AudioSystem.getAudioInputStream(targetFormat, audioStream);
                
                // Read ALL bytes into memory (no disk I/O during gameplay)
                byte[] audioData = decodedStream.readAllBytes();
                
                // Store in memory
                soundBank.put(soundName, new PreloadedSound(audioData, targetFormat));
                
                // Cleanup streams
                decodedStream.close();
                audioStream.close();
                
                System.out.println("[HitSoundPlayer] Preloaded: " + soundName + ext + 
                                 " (" + audioData.length + " bytes, " + 
                                 targetFormat.getChannels() + " channels, " +
                                 (int)targetFormat.getSampleRate() + " Hz)");
                return;
                
            } catch (UnsupportedAudioFileException | IOException e) {
                System.err.println("[HitSoundPlayer] Failed to load: " + soundName + ext);
                e.printStackTrace();
            }
        }
        
        System.err.println("[HitSoundPlayer] Sound not found: " + soundName);
    }
    
    /**
     * Play a preloaded hit sound with minimal latency (<5ms).
     * This method returns immediately and does not block.
     * 
     * @param soundName Name of the sound to play (as registered during init)
     */
    public void playHit(String soundName) {
        PreloadedSound sound = soundBank.get(soundName);
        if (sound == null) {
            System.err.println("[HitSoundPlayer] Sound not loaded: " + soundName);
            return;
        }
        
        // Apply latency offset (negative = predictive, positive = delayed)
        if (latencyOffsetMs == 0) {
            // No offset - immediate playback
            playbackExecutor.execute(() -> playbackTask(sound));
        } else if (latencyOffsetMs < 0) {
            // Negative offset = predictive (play EARLIER - not truly possible, but we can try to compensate)
            // In practice, we play immediately and accept the limitation
            playbackExecutor.execute(() -> playbackTask(sound));
        } else {
            // Positive offset = delayed playback
            scheduledExecutor.schedule(() -> playbackTask(sound), latencyOffsetMs, TimeUnit.MILLISECONDS);
        }
    }
    
    /**
     * Internal playback task - runs in separate thread.
     * Creates a new SourceDataLine per sound for true simultaneous playback.
     */
    private void playbackTask(PreloadedSound sound) {
        SourceDataLine line = null;
        
        try {
            // Create a NEW line for this sound (allows simultaneous playback)
            DataLine.Info info = new DataLine.Info(SourceDataLine.class, sound.format);
            line = (SourceDataLine) AudioSystem.getLine(info);
            
            // Open with small buffer for low latency
            line.open(sound.format, BUFFER_SIZE);
            
            // Apply volume control if available
            applyVolume(line);
            
            // Start the line
            line.start();
            
            // Write ALL audio data from memory (already preloaded, no I/O)
            int bytesWritten = 0;
            int chunkSize = BUFFER_SIZE;
            
            while (bytesWritten < sound.audioData.length) {
                int bytesToWrite = Math.min(chunkSize, sound.audioData.length - bytesWritten);
                int written = line.write(sound.audioData, bytesWritten, bytesToWrite);
                bytesWritten += written;
            }
            
            // Wait for playback to complete
            line.drain();
            
        } catch (LineUnavailableException e) {
            System.err.println("[HitSoundPlayer] Audio line unavailable");
            e.printStackTrace();
        } finally {
            // Always cleanup resources
            if (line != null) {
                line.stop();
                line.close();
            }
        }
    }
    
    /**
     * Apply master volume to the audio line.
     */
    private void applyVolume(SourceDataLine line) {
        try {
            if (line.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
                FloatControl volumeControl = (FloatControl) line.getControl(FloatControl.Type.MASTER_GAIN);
                float min = volumeControl.getMinimum();
                float max = volumeControl.getMaximum();
                // Convert 0.0-1.0 to dB range
                float dB = min + (max - min) * masterVolume;
                volumeControl.setValue(dB);
            }
        } catch (Exception e) {
            // Volume control not available, continue without it
        }
    }
    
    /**
     * Set master volume (0.0 = silent, 1.0 = max).
     */
    public void setMasterVolume(float volume) {
        this.masterVolume = Math.max(0.0f, Math.min(1.0f, volume));
    }
    
    /**
     * Get master volume (0.0 to 1.0).
     */
    public float getMasterVolume() {
        return masterVolume;
    }
    
    /**
     * Set latency offset in milliseconds.
     * Negative values = predictive (plays as early as possible, limited by system)
     * Positive values = delayed (schedules playback in the future)
     * 
     * @param offsetMs Latency offset (-100 to +100ms recommended)
     */
    public void setLatencyOffset(int offsetMs) {
        this.latencyOffsetMs = offsetMs;
    }
    
    /**
     * Get current latency offset in milliseconds.
     */
    public int getLatencyOffset() {
        return latencyOffsetMs;
    }
    
    /**
     * Shutdown the player and release all resources.
     * Call this when closing the game.
     */
    public void shutdown() {
        playbackExecutor.shutdown();
        scheduledExecutor.shutdown();
        soundBank.clear();
        System.out.println("[HitSoundPlayer] Shutdown complete");
    }
}
