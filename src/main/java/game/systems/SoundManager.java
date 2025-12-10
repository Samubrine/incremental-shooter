package game.systems;

import javax.sound.sampled.*;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Sound effect manager with volume control and clip pooling for rapid-fire sounds.
 * Loads and plays .wav and .mp3 files from resources/sounds/.
 */
public class SoundManager {
    private Map<String, Clip> sounds;
    private Map<String, List<Clip>> soundPools; // Pool of clips for rapid-fire sounds
    private boolean soundEnabled;
    private float masterVolume; // 0.0 to 1.0
    
    // Sounds that need multiple instances for simultaneous playback
    private static final String[] POOLED_SOUNDS = {"hit", "hit_critical", "player_damaged"};
    private static final int POOL_SIZE = 5; // 5 instances per pooled sound
    
    public SoundManager() {
        sounds = new HashMap<>();
        soundPools = new HashMap<>();
        soundEnabled = true;
        masterVolume = 0.7f; // Default 70%
        loadSounds();
    }
    
    private void loadSounds() {
        // Load all available sound files
        String[] soundFiles = {
            "click_button", "hit", "hit_critical", "player_damaged", 
            "wave_lose", "win_difficulty"
        };
        
        for (String soundName : soundFiles) {
            // Check if this sound needs pooling
            boolean needsPool = false;
            for (String pooledSound : POOLED_SOUNDS) {
                if (soundName.equals(pooledSound)) {
                    needsPool = true;
                    break;
                }
            }
            
            if (needsPool) {
                // Create a pool of clips for this sound
                List<Clip> pool = new CopyOnWriteArrayList<>();
                for (int i = 0; i < POOL_SIZE; i++) {
                    Clip clip = loadSingleClip(soundName);
                    if (clip != null) {
                        pool.add(clip);
                    }
                }
                if (!pool.isEmpty()) {
                    soundPools.put(soundName, pool);
                    System.out.println("Loaded sound pool: " + soundName + " (" + pool.size() + " instances)");
                }
            } else {
                // Load single clip for non-pooled sounds
                Clip clip = loadSingleClip(soundName);
                if (clip != null) {
                    sounds.put(soundName, clip);
                }
            }
        }
    }
    
    private Clip loadSingleClip(String soundName) {
        // Try .wav first, then .mp3
        String[] extensions = {".wav", ".mp3"};
        for (String ext : extensions) {
                try {
                    File soundFile = new File("src/main/resources/sounds/" + soundName + ext);
                    if (soundFile.exists()) {
                        AudioInputStream audioStream = AudioSystem.getAudioInputStream(soundFile);
                        AudioFormat baseFormat = audioStream.getFormat();
                        
                        // Convert to PCM if needed (for MP3 support)
                        AudioFormat decodedFormat = new AudioFormat(
                            AudioFormat.Encoding.PCM_SIGNED,
                            baseFormat.getSampleRate(),
                            16,
                            baseFormat.getChannels(),
                            baseFormat.getChannels() * 2,
                            baseFormat.getSampleRate(),
                            false
                        );
                        
                        AudioInputStream decodedStream = AudioSystem.getAudioInputStream(decodedFormat, audioStream);
                        Clip clip = AudioSystem.getClip();
                        clip.open(decodedStream);
                        return clip;
                    }
                } catch (Exception e) {
                    // If conversion fails, try without conversion
                    try {
                        File soundFile = new File("src/main/resources/sounds/" + soundName + ext);
                        if (soundFile.exists()) {
                            AudioInputStream audioStream = AudioSystem.getAudioInputStream(soundFile);
                            Clip clip = AudioSystem.getClip();
                            clip.open(audioStream);
                            return clip;
                        }
                    } catch (Exception e2) {
                        // Continue to next extension
                    }
                }
            }
        return null;
    }
    
    public void playSound(String soundName) {
        if (!soundEnabled) return;
        
        // Check if this sound has a pool
        List<Clip> pool = soundPools.get(soundName);
        if (pool != null && !pool.isEmpty()) {
            // Find an available clip from the pool (not currently playing)
            Clip availableClip = null;
            for (Clip clip : pool) {
                if (!clip.isRunning()) {
                    availableClip = clip;
                    break;
                }
            }
            
            // If all clips are busy, use the first one anyway (will restart it)
            if (availableClip == null) {
                availableClip = pool.get(0);
            }
            
            // Play the clip
            playClip(availableClip);
        } else {
            // Use single clip for non-pooled sounds
            Clip clip = sounds.get(soundName);
            if (clip != null) {
                playClip(clip);
            }
        }
    }
    
    private void playClip(Clip clip) {
        if (clip == null) return;
        
        // Stop if already playing to prevent delay
        if (clip.isRunning()) {
            clip.stop();
        }
        
        // Rewind to beginning
        clip.setFramePosition(0);
            
            // Set volume
            try {
                FloatControl volumeControl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
                float min = volumeControl.getMinimum();
                float max = volumeControl.getMaximum();
                // Convert 0-1 range to dB (logarithmic scale)
                float dB = min + (max - min) * masterVolume;
                volumeControl.setValue(dB);
            } catch (Exception e) {
                // Volume control not available for this clip
            }
            
            // Start immediately
            clip.start();
    }    public void toggleSound() {
        soundEnabled = !soundEnabled;
    }
    
    public boolean isSoundEnabled() {
        return soundEnabled;
    }
    
    /**
     * Set master volume from 0-100 (percentage).
     */
    public void setVolume(int volumePercent) {
        volumePercent = Math.max(0, Math.min(100, volumePercent));
        masterVolume = volumePercent / 100.0f;
    }
    
    /**
     * Get master volume as 0-100 (percentage).
     */
    public int getVolume() {
        return (int)(masterVolume * 100);
    }
    
    /**
     * Adjust volume by delta amount.
     */
    public void adjustVolume(int delta) {
        setVolume(getVolume() + delta);
    }
}
