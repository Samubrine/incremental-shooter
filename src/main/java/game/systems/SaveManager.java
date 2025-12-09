package game.systems;

import game.data.GameData;
import java.io.*;

/**
 * Handles saving and loading game progress.
 * Saves to a file in user's home directory.
 */
public class SaveManager {
    private static final String SAVE_FILE = System.getProperty("user.home") + 
                                           "/.incremental_shooter_save.dat";
    
    public void saveGame(GameData data) {
        try (ObjectOutputStream out = new ObjectOutputStream(
                new FileOutputStream(SAVE_FILE))) {
            out.writeObject(data);
        } catch (IOException e) {
            System.err.println("Failed to save game: " + e.getMessage());
        }
    }
    
    public GameData loadGame() {
        File saveFile = new File(SAVE_FILE);
        if (!saveFile.exists()) {
            return new GameData(); // New game
        }
        
        try (ObjectInputStream in = new ObjectInputStream(
                new FileInputStream(SAVE_FILE))) {
            return (GameData) in.readObject();
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Failed to load game: " + e.getMessage());
            return new GameData(); // Return new game on error
        }
    }
    
    public void deleteSave() {
        File saveFile = new File(SAVE_FILE);
        if (saveFile.exists()) {
            saveFile.delete();
        }
    }
    
    /**
     * Reset all save data to defaults.
     */
    public void resetSaveData() {
        deleteSave();
        saveGame(new GameData()); // Create fresh save file
    }
}
