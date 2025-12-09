package game;

import game.entities.Player;
import game.systems.*;
import game.ui.GamePanel;
import game.data.GameData;

/**
 * Core game engine managing all game systems and state.
 * Coordinates between entities, wave management, progression, and rendering.
 */
public class GameEngine {
    private static GameEngine instance;
    
    private GamePanel gamePanel;
    private Player player;
    private WaveManager waveManager;
    private UpgradeManager upgradeManager;
    private CollisionManager collisionManager;
    private InputManager inputManager;
    private SoundManager soundManager;
    private SaveManager saveManager;
    
    private GameState gameState;
    private int currentDifficulty;
    private boolean isPaused;
    
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
        this.gamePanel = panel;
        
        // Initialize managers
        soundManager = new SoundManager();
        saveManager = new SaveManager();
        inputManager = new InputManager();
        collisionManager = new CollisionManager();
        upgradeManager = new UpgradeManager();
        
        // Load game data
        GameData data = saveManager.loadGame();
        if (data != null) {
            currentDifficulty = data.getUnlockedDifficulty();
            upgradeManager.loadUpgrades(data);
        }
        
        // Initialize player with saved upgrades
        player = new Player(400, 300, upgradeManager);
    }
    
    /**
     * Start a new game at specified difficulty.
     */
    public void startGame(int difficulty) {
        this.currentDifficulty = difficulty;
        upgradeManager.resetTempUpgrades(); // Reset temporary upgrades
        waveManager = new WaveManager(difficulty);
        player.fullReset();
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
        
        // Award cash for surviving this wave
        GameData data = saveManager.loadGame();
        if (data == null) {
            data = new GameData();
        }
        data.addCash(10); // +10 cash per wave survived
        saveManager.saveGame(data);
        
        if (waveManager.getCurrentWave() >= 15) {
            handleGameWin();
        } else {
            gameState = GameState.SHOP;
            soundManager.playSound("wave_complete");
        }
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
        // Award bonus cash for completing all 15 waves
        int bonusCash = currentDifficulty * 50;
        GameData data = saveManager.loadGame();
        if (data == null) {
            data = new GameData();
        }
        data.addCash(bonusCash);
        data.unlockDifficulty(currentDifficulty + 1);
        saveManager.saveGame(data);
        
        gameState = GameState.WIN;
        soundManager.playSound("game_win");
    }
    
    private void handleGameOver() {
        gameState = GameState.GAME_OVER;
        soundManager.playSound("game_over");
    }
    
    public void togglePause() {
        isPaused = !isPaused;
    }
    
    public void returnToMenu() {
        gameState = GameState.MENU;
        saveManager.saveGame(new GameData(currentDifficulty, upgradeManager));
    }
    
    // Getters
    public Player getPlayer() { return player; }
    public WaveManager getWaveManager() { return waveManager; }
    public UpgradeManager getUpgradeManager() { return upgradeManager; }
    public SoundManager getSoundManager() { return soundManager; }
    public SaveManager getSaveManager() { return saveManager; }
    public InputManager getInputManager() { return inputManager; }
    public GameState getGameState() { return gameState; }
    public boolean isPaused() { return isPaused; }
    public int getCurrentDifficulty() { return currentDifficulty; }
    
    public enum GameState {
        MENU, PLAYING, PAUSED, SHOP, SETTINGS, UPGRADES, WIN, GAME_OVER
    }
    
    public void openSettings() {
        gameState = GameState.SETTINGS;
    }
    
    public void openUpgrades() {
        gameState = GameState.UPGRADES;
    }
}
