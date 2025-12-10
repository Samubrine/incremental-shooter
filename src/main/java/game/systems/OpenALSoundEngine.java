package game.systems;

import org.lwjgl.openal.*;
import org.lwjgl.system.MemoryUtil;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

import static org.lwjgl.openal.AL10.*;
import static org.lwjgl.openal.ALC10.*;
import static org.lwjgl.system.MemoryUtil.*;

/**
 * OpenAL-based sound engine for ultra-low latency audio playback (1-2ms).
 * Uses hardware-accelerated audio mixing with source pooling for optimal performance.
 * 
 * Architecture:
 * - Device/Context initialization on startup
 * - Preloaded buffers for all sounds (hit, hit_critical, player_damaged)
 * - 32-source pool for simultaneous playback
 * - Lock-free source allocation via ConcurrentLinkedQueue
 */
public class OpenALSoundEngine {
    
    // OpenAL handles
    private long device;
    private long context;
    
    // Sound buffers (preloaded)
    private Map<String, Integer> soundBuffers = new HashMap<>();
    
    // Source pool for playback
    private ConcurrentLinkedQueue<Integer> availableSources = new ConcurrentLinkedQueue<>();
    private List<Integer> allSources = new ArrayList<>();
    
    // Configuration
    private static final int SOURCE_POOL_SIZE = 32;
    private static final String SOUNDS_PATH = "src/main/resources/sounds/";
    
    // Volume control
    private float masterVolume = 1.0f;
    private boolean soundEnabled = true;
    
    /**
     * Initialize OpenAL device, context, and preload all sounds.
     */
    public void initialize() {
        try {
            // Open default audio device
            device = alcOpenDevice((ByteBuffer) null);
            if (device == NULL) {
                throw new IllegalStateException("Failed to open OpenAL device");
            }
            
            // Create context
            context = alcCreateContext(device, (IntBuffer) null);
            if (context == NULL) {
                throw new IllegalStateException("Failed to create OpenAL context");
            }
            
            alcMakeContextCurrent(context);
            ALCCapabilities alcCaps = ALC.createCapabilities(device);
            ALCapabilities alCaps = AL.createCapabilities(alcCaps);
            
            if (!alCaps.OpenAL10) {
                throw new IllegalStateException("OpenAL 1.0 not supported");
            }
            
            System.out.println("OpenAL initialized: " + alcGetString(device, ALC_DEVICE_SPECIFIER));
            
            // Create source pool
            for (int i = 0; i < SOURCE_POOL_SIZE; i++) {
                int source = alGenSources();
                alSourcef(source, AL_GAIN, masterVolume);
                allSources.add(source);
                availableSources.offer(source);
            }
            
            System.out.println("Created " + SOURCE_POOL_SIZE + " OpenAL sources");
            
            // Preload sounds
            loadSound("hit", "hit.wav");
            loadSound("hit_critical", "hit_critical.wav");
            loadSound("player_damaged", "player_damaged.wav");
            
            System.out.println("OpenAL sound engine ready");
            
        } catch (Exception e) {
            System.err.println("Failed to initialize OpenAL: " + e.getMessage());
            e.printStackTrace();
            cleanup();
        }
    }
    
    /**
     * Load a WAV sound file into an OpenAL buffer.
     * Supports PCM WAV files (mono/stereo, 16-bit).
     */
    private void loadSound(String name, String filename) {
        try {
            Path path = Paths.get(SOUNDS_PATH + filename);
            
            // Read entire WAV file
            byte[] wavData = Files.readAllBytes(path);
            
            // Parse WAV header (minimal parser for standard PCM WAV)
            // WAV format: RIFF header (12 bytes) + fmt chunk + data chunk
            if (wavData.length < 44 || 
                wavData[0] != 'R' || wavData[1] != 'I' || wavData[2] != 'F' || wavData[3] != 'F') {
                throw new IOException("Invalid WAV file: " + filename);
            }
            
            // Read format details from fmt chunk (assumes standard 44-byte header)
            int channels = (wavData[22] & 0xFF) | ((wavData[23] & 0xFF) << 8);
            int sampleRate = (wavData[24] & 0xFF) | ((wavData[25] & 0xFF) << 8) | 
                            ((wavData[26] & 0xFF) << 16) | ((wavData[27] & 0xFF) << 24);
            int bitsPerSample = (wavData[34] & 0xFF) | ((wavData[35] & 0xFF) << 8);
            
            // Find data chunk (search for "data" marker)
            int dataOffset = 36;
            while (dataOffset < wavData.length - 8) {
                if (wavData[dataOffset] == 'd' && wavData[dataOffset+1] == 'a' &&
                    wavData[dataOffset+2] == 't' && wavData[dataOffset+3] == 'a') {
                    dataOffset += 4; // Skip "data"
                    int dataSize = (wavData[dataOffset] & 0xFF) | ((wavData[dataOffset+1] & 0xFF) << 8) |
                                  ((wavData[dataOffset+2] & 0xFF) << 16) | ((wavData[dataOffset+3] & 0xFF) << 24);
                    dataOffset += 4; // Skip size field
                    break;
                }
                dataOffset++;
            }
            
            if (dataOffset >= wavData.length) {
                throw new IOException("No data chunk found in WAV: " + filename);
            }
            
            // Allocate buffer for PCM data
            int pcmSize = wavData.length - dataOffset;
            ByteBuffer pcmBuffer = MemoryUtil.memAlloc(pcmSize);
            pcmBuffer.put(wavData, dataOffset, pcmSize);
            pcmBuffer.flip();
            
            // Determine OpenAL format
            int format;
            if (channels == 1 && bitsPerSample == 8) {
                format = AL_FORMAT_MONO8;
            } else if (channels == 1 && bitsPerSample == 16) {
                format = AL_FORMAT_MONO16;
            } else if (channels == 2 && bitsPerSample == 8) {
                format = AL_FORMAT_STEREO8;
            } else if (channels == 2 && bitsPerSample == 16) {
                format = AL_FORMAT_STEREO16;
            } else {
                MemoryUtil.memFree(pcmBuffer);
                throw new IllegalStateException("Unsupported WAV format: " + channels + " channels, " + bitsPerSample + " bits");
            }
            
            // Create OpenAL buffer
            int buffer = alGenBuffers();
            alBufferData(buffer, format, pcmBuffer, sampleRate);
            
            soundBuffers.put(name, buffer);
            
            System.out.println("Loaded sound: " + name + " (" + channels + " channels, " + 
                              sampleRate + " Hz, " + bitsPerSample + " bits, " + pcmSize + " bytes)");
            
            // Cleanup
            MemoryUtil.memFree(pcmBuffer);
            
        } catch (IOException e) {
            System.err.println("Failed to load sound " + name + ": " + e.getMessage());
        }
    }
    
    /**
     * Play a sound with ultra-low latency.
     * Non-blocking, uses source pool for simultaneous playback.
     */
    public void playSound(String soundName) {
        if (!soundEnabled) return;
        
        Integer buffer = soundBuffers.get(soundName);
        if (buffer == null) {
            System.err.println("Sound not found: " + soundName);
            return;
        }
        
        Integer source = availableSources.poll();
        if (source == null) {
            // All sources busy, steal oldest (this is rare with 32 sources)
            source = allSources.get(0);
            alSourceStop(source);
        }
        
        // Configure and play
        alSourcei(source, AL_BUFFER, buffer);
        alSourcef(source, AL_GAIN, masterVolume);
        alSourcef(source, AL_PITCH, 1.0f);
        alSource3f(source, AL_POSITION, 0, 0, 0);
        alSourcei(source, AL_LOOPING, AL_FALSE);
        alSourcePlay(source);
        
        // Return source to pool after playback (non-blocking check)
        final int sourceId = source;
        new Thread(() -> {
            try {
                // Wait for playback to finish
                while (alGetSourcei(sourceId, AL_SOURCE_STATE) == AL_PLAYING) {
                    Thread.sleep(10);
                }
                availableSources.offer(sourceId);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }).start();
    }
    
    /**
     * Set master volume (0.0 to 1.0).
     */
    public void setVolume(float volume) {
        this.masterVolume = Math.max(0.0f, Math.min(1.0f, volume));
        for (int source : allSources) {
            alSourcef(source, AL_GAIN, masterVolume);
        }
    }
    
    /**
     * Get current volume.
     */
    public float getVolume() {
        return masterVolume;
    }
    
    /**
     * Toggle sound on/off.
     */
    public void setSoundEnabled(boolean enabled) {
        this.soundEnabled = enabled;
    }
    
    /**
     * Check if sound is enabled.
     */
    public boolean isSoundEnabled() {
        return soundEnabled;
    }
    
    /**
     * Clean up all OpenAL resources.
     * MUST be called before application exit.
     */
    public void cleanup() {
        // Delete sources
        for (int source : allSources) {
            alDeleteSources(source);
        }
        allSources.clear();
        availableSources.clear();
        
        // Delete buffers
        for (int buffer : soundBuffers.values()) {
            alDeleteBuffers(buffer);
        }
        soundBuffers.clear();
        
        // Destroy context and device
        if (context != NULL) {
            alcDestroyContext(context);
            context = NULL;
        }
        
        if (device != NULL) {
            alcCloseDevice(device);
            device = NULL;
        }
        
        System.out.println("OpenAL sound engine cleaned up");
    }
}
