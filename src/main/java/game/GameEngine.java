package game;

import game.entities.Player;
import game.systems.*;
import game.ui.GamePanel;
import game.data.GameData;
import game.entities.DamageText;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Core game engine managing all game systems and state.
 * Coordinates between entities, wave management, progression, and rendering.
 */
public class GameEngine {
    private static GameEngine instance;
    
    private Player player;
    private WaveManager waveManager;
    private UpgradeManager upgradeManager;
    private CollisionManager collisionManager;
    private List<DamageText> damageTexts = new ArrayList<>();
    private InputManager inputManager;
    private SoundManager soundManager;
    private HitSoundPlayer hitSoundPlayer;
    private SaveManager saveManager;
    
    private GameState gameState;
    private int currentDifficulty;
    private boolean isPaused;

    // --- Screen shake state ---
    private double screenShakeTimer = 0.0;
    private double screenShakeDuration = 0.0;
    private double screenShakeMagnitude = 0.0;
    private double shakeX = 0.0;
    private double shakeY = 0.0;
    private final Random rnd = new Random();
    
    private GameEngine() {
        currentDifficulty = 1;
        isPaused = false;
        gameState = GameState.MENU;
    }
    
    public static GameEngine getInstance() {
        if (instance == null) {
            instance = new GameEngine();
        }
        return instance;
    }
    
    /**
     * Initialize all game systems and load saved data.
     */
    public void initialize(GamePanel panel) {
        // Initialize managers
        soundManager = new SoundManager();
        hitSoundPlayer = new HitSoundPlayer("src/main/resources/sounds",
            new String[]{"hit", "hit_critical", "player_damaged"});
        saveManager = new SaveManager();
        inputManager = new InputManager();
        collisionManager = new CollisionManager(this);
        upgradeManager = new UpgradeManager();
        
        // Load game data
        GameData data = saveManager.loadGame();
        if (data != null) {
            currentDifficulty = data.getUnlockedDifficulty();
            upgradeManager.loadUpgrades(data);
            hitSoundPlayer.setLatencyOffset(data.getAudioLatencyOffset());
        }
        
        // Initialize player with saved upgrades
        player = new Player(400, 300, upgradeManager);
    }
    
    public void spawnDamageText(double x, double y, int value, boolean isCrit) {
        damageTexts.add(new DamageText(x, y, value, isCrit));
    }

    /**
     * Trigger a screen shake effect.
     * @param duration seconds
     * @param magnitude pixels (max offset)
     */
    public void triggerScreenShake(double duration, double magnitude) {
        this.screenShakeDuration = duration;
        this.screenShakeTimer = duration;
        this.screenShakeMagnitude = magnitude;
    }

    /**
     * Returns current shake X offset (can be fractional).
     */
    public double getShakeX() {
        return shakeX;
    }

    /**
     * Returns current shake Y offset (can be fractional).
     */
    public double getShakeY() {
        return shakeY;
    }
    
    /**
     * Start a new game at specified difficulty.
     */
    public void startGame(int difficulty) {
        this.currentDifficulty = difficulty;
        upgradeManager.resetTempUpgrades(); // Reset temporary upgrades
        waveManager = new WaveManager(difficulty);
        player.fullReset();
        damageTexts.clear();
        gameState = GameState.PLAYING;
        isPaused = false;
    }
    
    /**
     * Main game update loop - called every frame.
     */
    public void update(double deltaTime) {
        if (isPaused || gameState != GameState.PLAYING) {
            return;
        }
        
        // Update player
        player.update(deltaTime, inputManager);
        
        // Update wave system
        waveManager.update(deltaTime, player);
        
        // Check collisions
        collisionManager.checkCollisions(player, waveManager.getEnemies(), 
                                        waveManager.getEnemyProjectiles());
        
        // Update damage texts
        damageTexts.removeIf(d -> !d.isAlive());
        for (DamageText d : damageTexts) {
            d.update(deltaTime);
        }

        // Update screen shake
        if (screenShakeTimer > 0) {
            screenShakeTimer -= deltaTime;
            double t = Math.max(0.0, screenShakeTimer / screenShakeDuration); // 1 -> 0
            // random offset scaled by t (fade out)
            double magnitude = screenShakeMagnitude * t;
            shakeX = (rnd.nextDouble() * 2.0 - 1.0) * magnitude;
            shakeY = (rnd.nextDouble() * 2.0 - 1.0) * magnitude;
            if (screenShakeTimer <= 0) {
                shakeX = 0;
                shakeY = 0;
            }
        }
        
        // Check wave completion
        if (waveManager.isWaveComplete()) {
            handleWaveComplete();
        }
        
        // Check game over
        if (player.isDead()) {
            handleGameOver();
        }
    }
    
    private void handleWaveComplete() {
        int coinsEarned = waveManager.getCurrentWave() * 10;
        player.addCoins(coinsEarned);
        
        // Check for game win (15 waves, unless Endless mode)
        if (waveManager.getCurrentWave() >= 15 && currentDifficulty != 999) {
            handleGameWin();
        } else {
            // Transition to shop (cash will be saved when game ends, not per wave)
            gameState = GameState.SHOP;
            soundManager.playSound("click_button");
        }
    }
    
    /**
     * Award cash for completing a wave and save it.
     * Called when entering the shop screen.
     */
    private void awardWaveCash() {
        // This method is no longer used - cash is awarded at game end, not per wave
        // Kept for backwards compatibility but does nothing
    }
    
    /**
     * Continue to next wave after shopping.
     */
    public void continueToNextWave() {
        if (gameState == GameState.SHOP && waveManager != null) {
            waveManager.startNextWave();
            gameState = GameState.PLAYING;
        }
    }
    
    private void handleGameWin() {
        // Award bonus cash for completing all 15 waves (Endless mode doesn't trigger this)
        int bonusCash = currentDifficulty * 50;
        GameData data = saveManager.loadGame();
        if (data == null) {
            data = new GameData();
        }
        
        // Store current cash and add bonus
        int currentCash = data.getCash();
        currentCash += bonusCash;
        
        // Save current unlocked difficulty before creating new GameData
        int currentUnlockedDiff = data.getUnlockedDifficulty();
        int nextDifficulty = currentDifficulty + 1;
        
        // Save with current upgrade manager and latency to preserve all data
        int latency = (hitSoundPlayer != null) ? hitSoundPlayer.getLatencyOffset() : 0;
        GameData saveData = new GameData(currentUnlockedDiff, upgradeManager, latency);
        saveData.addCash(currentCash); // Set the total accumulated cash
        
        // Only unlock next difficulty if not in endless mode (999)
        if (currentDifficulty != 999) {
            saveData.unlockDifficulty(nextDifficulty);
        }
        
        saveManager.saveGame(saveData);
        
        gameState = GameState.WIN;
        soundManager.playSound("win_difficulty");
    }
    
    private void handleGameOver() {
        // Award cash for waves COMPLETED (not just started)
        // Players must complete at least 1 wave to earn cash
        int wavesSurvived = waveManager.getCurrentWave() - 1; // Subtract 1 because current wave wasn't completed
        if (wavesSurvived > 0) {
            GameData data = saveManager.loadGame();
            if (data == null) {
                data = new GameData();
            }
            
            // Store current cash and add earned cash
            int currentCash = data.getCash();
            currentCash += wavesSurvived * 10; // Award 10 cash per wave survived
            
            // Preserve the unlocked difficulty (don't overwrite with current difficulty)
            int currentUnlockedDiff = data.getUnlockedDifficulty();
            
            // Save the cash earned
            int latency = (hitSoundPlayer != null) ? hitSoundPlayer.getLatencyOffset() : 0;
            GameData saveData = new GameData(currentUnlockedDiff, upgradeManager, latency);
            saveData.addCash(currentCash);
            saveManager.saveGame(saveData);
        }
        
        gameState = GameState.GAME_OVER;
        soundManager.playSound("wave_lose");
    }
    
    public void quitToMenuFromGame() {
        // Award cash for waves COMPLETED (not just started)
        if (waveManager != null) {
            int wavesSurvived = waveManager.getCurrentWave() - 1; // Subtract 1 because current wave wasn't completed
            if (wavesSurvived > 0) {
                GameData data = saveManager.loadGame();
                if (data == null) {
                    data = new GameData();
                }
                
                // Store current cash and add earned cash
                int currentCash = data.getCash();
                currentCash += wavesSurvived * 10; // Award 10 cash per wave survived
                
                // Preserve the unlocked difficulty (don't overwrite with current difficulty)
                int currentUnlockedDiff = data.getUnlockedDifficulty();
                
                // Save the cash earned
                int latency = (hitSoundPlayer != null) ? hitSoundPlayer.getLatencyOffset() : 0;
                GameData saveData = new GameData(currentUnlockedDiff, upgradeManager, latency);
                saveData.addCash(currentCash);
                saveManager.saveGame(saveData);
            }
        }
        
        // Return to menu
        isPaused = false;
        gameState = GameState.MENU;
        damageTexts.clear();
    }

    public void togglePause() {
        isPaused = !isPaused;
    }
    
    public void returnToMenu() {
        gameState = GameState.MENU;
        damageTexts.clear();
        
        // Load existing data to preserve cash and unlocked difficulty
        GameData existingData = saveManager.loadGame();
        int currentCash = (existingData != null) ? existingData.getCash() : 0;
        int currentUnlockedDiff = (existingData != null) ? existingData.getUnlockedDifficulty() : 1;
        
        // Create new save data with current state (preserve unlocked difficulty)
        GameData saveData = new GameData(currentUnlockedDiff, upgradeManager, hitSoundPlayer.getLatencyOffset());
        saveData.addCash(currentCash); // Preserve cash
        saveManager.saveGame(saveData);
    }
    
    /**
     * Reload upgrade manager from save file.
     * Called after resetting save data or when permanent upgrades need to sync.
     */
    public void reloadUpgrades() {
        GameData data = saveManager.loadGame();
        if (data != null) {
            upgradeManager.loadUpgrades(data);
        }
    }
    
    // Getters
    public Player getPlayer() { return player; }
    public WaveManager getWaveManager() { return waveManager; }
    public UpgradeManager getUpgradeManager() { return upgradeManager; }
    public SoundManager getSoundManager() { return soundManager; }
    public HitSoundPlayer getHitSoundPlayer() { return hitSoundPlayer; }
    public SaveManager getSaveManager() { return saveManager; }
    public InputManager getInputManager() { return inputManager; }
    public GameState getGameState() { return gameState; }
    public boolean isPaused() { return isPaused; }
    public int getCurrentDifficulty() { return currentDifficulty; }
    public List<DamageText> getDamageTexts() { return damageTexts; }
    
    public enum GameState {
        MENU, DIFFICULTY_SELECT, PLAYING, PAUSED, SHOP, SETTINGS, UPGRADES, WIN, GAME_OVER
    }
    
    public void openSettings() {
        gameState = GameState.SETTINGS;
    }
    
    public void openUpgrades() {
        gameState = GameState.UPGRADES;
    }
    
    public void openDifficultySelect() {
        gameState = GameState.DIFFICULTY_SELECT;
        soundManager.playSound("click_button");
    }
    
    /**
     * Cleanup resources before shutdown.
     * Call this before System.exit() to ensure proper shutdown.
     */
    public void cleanup() {
        if (hitSoundPlayer != null) {
            hitSoundPlayer.shutdown();
        }
        // Save game state before exit
        if (upgradeManager != null && hitSoundPlayer != null) {
            // Load existing data to preserve cash and unlocked difficulty
            GameData existingData = saveManager.loadGame();
            int currentCash = (existingData != null) ? existingData.getCash() : 0;
            int currentUnlockedDiff = (existingData != null) ? existingData.getUnlockedDifficulty() : 1;
            
            // Create new save data with current state (preserve unlocked difficulty)
            GameData saveData = new GameData(currentUnlockedDiff, upgradeManager, hitSoundPlayer.getLatencyOffset());
            saveData.addCash(currentCash); // Preserve cash
            saveManager.saveGame(saveData);
        }
    }
}
