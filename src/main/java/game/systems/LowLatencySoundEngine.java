package game.systems;

import javax.sound.sampled.*;
import java.io.File;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Low-latency sound engine inspired by Minecraft's audio system.
 * 
 * Features:
 * - Preloads all sounds as raw PCM buffers
 * - Single mixer thread writes to one SourceDataLine
 * - <10ms latency with 256-sample buffer
 * - Supports overlapping sounds via PCM mixing with clipping protection
 * - High-priority mixer thread to avoid GC delays
 */
public class LowLatencySoundEngine {
    
    // Audio format constants
    private static final float SAMPLE_RATE = 44100f;
    private static final int CHANNELS = 2; // Stereo
    private static final int SAMPLE_SIZE_BITS = 16;
    private static final int FRAME_SIZE = CHANNELS * (SAMPLE_SIZE_BITS / 8); // 4 bytes per frame
    private static final int BUFFER_FRAMES = 256; // ~5.8ms latency at 44.1kHz
    private static final int BUFFER_SIZE_BYTES = BUFFER_FRAMES * FRAME_SIZE;
    
    // Preloaded sound bank (sound name -> PCM samples)
    private final Map<String, float[]> soundBank = new HashMap<>();
    
    // Active sound sources being mixed
    private final Queue<SoundSource> activeSources = new ConcurrentLinkedQueue<>();
    
    // Mixer thread components
    private Thread mixerThread;
    private final AtomicBoolean running = new AtomicBoolean(false);
    private SourceDataLine outputLine;
    
    // Master volume (0.0 to 1.0)
    private volatile float masterVolume = 0.7f;
    
    /**
     * Represents an active sound being played
     */
    private static class SoundSource {
        final float[] samples;
        int position;
        final float volume;
        
        SoundSource(float[] samples, float volume) {
            this.samples = samples;
            this.position = 0;
            this.volume = volume;
        }
        
        boolean isFinished() {
            return position >= samples.length;
        }
    }
    
    /**
     * Initialize the sound engine and preload all sounds
     */
    public LowLatencySoundEngine(String soundDirectory, String[] soundNames) {
        // Preload all sounds
        for (String soundName : soundNames) {
            loadSound(soundDirectory, soundName);
        }
        
        // Start mixer thread
        startMixer();
    }
    
    /**
     * Load a WAV file and convert to normalized float PCM samples
     */
    private void loadSound(String directory, String soundName) {
        String[] extensions = {".wav", ".WAV"};
        
        for (String ext : extensions) {
            File soundFile = new File(directory + "/" + soundName + ext);
            if (!soundFile.exists()) continue;
            
            try (AudioInputStream audioStream = AudioSystem.getAudioInputStream(soundFile)) {
                AudioFormat baseFormat = audioStream.getFormat();
                
                // Convert to target format (44.1kHz, 16-bit, stereo)
                AudioFormat targetFormat = new AudioFormat(
                    AudioFormat.Encoding.PCM_SIGNED,
                    SAMPLE_RATE,
                    SAMPLE_SIZE_BITS,
                    CHANNELS,
                    FRAME_SIZE,
                    SAMPLE_RATE,
                    false // little-endian
                );
                
                // Resample if needed
                AudioInputStream convertedStream = AudioSystem.getAudioInputStream(targetFormat, audioStream);
                
                // Read all bytes
                byte[] audioBytes = convertedStream.readAllBytes();
                
                // Convert to normalized float samples (-1.0 to 1.0)
                float[] samples = bytesToFloatSamples(audioBytes);
                
                soundBank.put(soundName, samples);
                System.out.println("[LowLatencySoundEngine] Loaded: " + soundName + " (" + samples.length + " samples)");
                
                convertedStream.close();
                return;
                
            } catch (Exception e) {
                System.err.println("[LowLatencySoundEngine] Failed to load: " + soundName);
                e.printStackTrace();
            }
        }
    }
    
    /**
     * Convert byte array to normalized float samples
     */
    private float[] bytesToFloatSamples(byte[] bytes) {
        int sampleCount = bytes.length / 2; // 16-bit = 2 bytes per sample
        float[] samples = new float[sampleCount];
        
        ByteBuffer buffer = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN);
        
        for (int i = 0; i < sampleCount; i++) {
            short sampleValue = buffer.getShort();
            samples[i] = sampleValue / 32768.0f; // Normalize to -1.0 to 1.0
        }
        
        return samples;
    }
    
    /**
     * Start the mixer thread
     */
    private void startMixer() {
        try {
            AudioFormat format = new AudioFormat(
                SAMPLE_RATE,
                SAMPLE_SIZE_BITS,
                CHANNELS,
                true, // signed
                false // little-endian
            );
            
            DataLine.Info info = new DataLine.Info(SourceDataLine.class, format);
            outputLine = (SourceDataLine) AudioSystem.getLine(info);
            outputLine.open(format, BUFFER_SIZE_BYTES);
            outputLine.start();
            
            running.set(true);
            
            mixerThread = new Thread(this::mixerLoop, "AudioMixer");
            mixerThread.setPriority(Thread.MAX_PRIORITY); // High priority to avoid GC delays
            mixerThread.setDaemon(true);
            mixerThread.start();
            
            System.out.println("[LowLatencySoundEngine] Mixer started (buffer: " + BUFFER_FRAMES + " frames, ~" + 
                             String.format("%.1f", (BUFFER_FRAMES / SAMPLE_RATE) * 1000) + "ms latency)");
            
        } catch (LineUnavailableException e) {
            System.err.println("[LowLatencySoundEngine] Failed to start mixer");
            e.printStackTrace();
        }
    }
    
    /**
     * Main mixer loop - runs in dedicated high-priority thread
     */
    private void mixerLoop() {
        float[] mixBuffer = new float[BUFFER_FRAMES * CHANNELS];
        byte[] outputBuffer = new byte[BUFFER_SIZE_BYTES];
        
        while (running.get()) {
            // Clear mix buffer
            Arrays.fill(mixBuffer, 0.0f);
            
            // Mix all active sources
            Iterator<SoundSource> iterator = activeSources.iterator();
            while (iterator.hasNext()) {
                SoundSource source = iterator.next();
                
                if (source.isFinished()) {
                    iterator.remove();
                    continue;
                }
                
                // Mix this source into buffer
                int samplesToMix = Math.min(mixBuffer.length, source.samples.length - source.position);
                for (int i = 0; i < samplesToMix; i++) {
                    mixBuffer[i] += source.samples[source.position++] * source.volume * masterVolume;
                }
            }
            
            // Apply clipping protection and convert to bytes
            floatSamplesToBytes(mixBuffer, outputBuffer);
            
            // Write to output line
            outputLine.write(outputBuffer, 0, outputBuffer.length);
        }
    }
    
    /**
     * Convert float samples to bytes with clipping protection
     */
    private void floatSamplesToBytes(float[] floatSamples, byte[] byteOutput) {
        ByteBuffer buffer = ByteBuffer.wrap(byteOutput).order(ByteOrder.LITTLE_ENDIAN);
        
        for (float sample : floatSamples) {
            // Clipping protection
            if (sample > 1.0f) sample = 1.0f;
            if (sample < -1.0f) sample = -1.0f;
            
            // Convert to 16-bit signed
            short sampleValue = (short) (sample * 32767.0f);
            buffer.putShort(sampleValue);
        }
    }
    
    /**
     * Play a sound with essentially no added latency
     */
    public void playSound(String soundName) {
        playSound(soundName, 1.0f);
    }
    
    /**
     * Play a sound with custom volume
     */
    public void playSound(String soundName, float volume) {
        float[] samples = soundBank.get(soundName);
        if (samples == null) {
            System.err.println("[LowLatencySoundEngine] Sound not found: " + soundName);
            return;
        }
        
        // Add to active sources - will be picked up by mixer on next iteration
        activeSources.offer(new SoundSource(samples, volume));
    }
    
    /**
     * Set master volume (0.0 to 1.0)
     */
    public void setMasterVolume(float volume) {
        this.masterVolume = Math.max(0.0f, Math.min(1.0f, volume));
    }
    
    /**
     * Get master volume
     */
    public float getMasterVolume() {
        return masterVolume;
    }
    
    /**
     * Shutdown the sound engine
     */
    public void shutdown() {
        running.set(false);
        
        if (mixerThread != null) {
            try {
                mixerThread.join(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        
        if (outputLine != null) {
            outputLine.drain();
            outputLine.stop();
            outputLine.close();
        }
        
        System.out.println("[LowLatencySoundEngine] Shutdown complete");
    }
}
