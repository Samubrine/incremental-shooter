package game.systems;

import javax.sound.sampled.*;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * Simple sound effect manager.
 * Loads and plays .wav files from resources/sounds/.
 */
public class SoundManager {
    private Map<String, Clip> sounds;
    private boolean soundEnabled;
    
    public SoundManager() {
        sounds = new HashMap<>();
        soundEnabled = true;
        loadSounds();
    }
    
    private void loadSounds() {
        // Try to load sound files if they exist
        String[] soundFiles = {
            "shoot", "hit", "enemy_death", "wave_complete", 
            "game_over", "game_win", "upgrade"
        };
        
        for (String soundName : soundFiles) {
            try {
                File soundFile = new File("src/main/resources/sounds/" + soundName + ".wav");
                if (soundFile.exists()) {
                    AudioInputStream audioStream = AudioSystem.getAudioInputStream(soundFile);
                    Clip clip = AudioSystem.getClip();
                    clip.open(audioStream);
                    sounds.put(soundName, clip);
                }
            } catch (Exception e) {
                // Sound file not found or couldn't load - continue without it
                System.out.println("Could not load sound: " + soundName);
            }
        }
    }
    
    public void playSound(String soundName) {
        if (!soundEnabled) return;
        
        Clip clip = sounds.get(soundName);
        if (clip != null) {
            clip.setFramePosition(0); // Rewind to beginning
            clip.start();
        }
    }
    
    public void toggleSound() {
        soundEnabled = !soundEnabled;
    }
    
    public boolean isSoundEnabled() {
        return soundEnabled;
    }
}
