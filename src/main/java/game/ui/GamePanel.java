package game.ui;

import game.GameEngine;
import game.systems.UpgradeManager;
import game.data.GameData;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.AffineTransform;

/**
 * Main rendering panel with game loop.
 * Handles all rendering and frame updates at 60 FPS.
 */
public class GamePanel extends JPanel implements Runnable, KeyListener, MouseListener {
    private static final int PANEL_WIDTH = 800;
    private static final int PANEL_HEIGHT = 600;
    private static final int FPS = 60;
    private static final long FRAME_TIME = 1000000000 / FPS; // nanoseconds
    
    private Thread gameThread;
    private GameEngine engine;
    private MenuUI menuUI;
    private DifficultySelectUI difficultySelectUI;
    private GameUI gameUI;
    private ShopUI shopUI;
    private SettingsUI settingsUI;
    private UpgradesUI upgradesUI;
    
    public GamePanel() {
        setPreferredSize(new Dimension(PANEL_WIDTH, PANEL_HEIGHT));
        setBackground(Color.BLACK);
        setDoubleBuffered(true);
        setFocusable(true);
        
        engine = GameEngine.getInstance();
        engine.initialize(this);
        
        // Register input manager for WASD movement
        addKeyListener(engine.getInputManager());
        
        // Register this panel for game state control
        addKeyListener(this);
        
        // Add mouse listener to request focus on click
        addMouseListener(this);
        
        menuUI = new MenuUI();
        difficultySelectUI = new DifficultySelectUI();
        gameUI = new GameUI();
        shopUI = new ShopUI();
        settingsUI = new SettingsUI();
        upgradesUI = new UpgradesUI();
        
        startGameLoop();
        
        // Request focus so keyboard input works
        requestFocusInWindow();
    }
    
    private void startGameLoop() {
        gameThread = new Thread(this);
        gameThread.start();
    }
    
    @Override
    public void run() {
        long lastTime = System.nanoTime();
        
        while (gameThread != null) {
            long currentTime = System.nanoTime();
            long elapsedTime = currentTime - lastTime;
            
            if (elapsedTime >= FRAME_TIME) {
                double deltaTime = elapsedTime / 1000000000.0; // Convert to seconds
                
                update(deltaTime);
                repaint();
                
                lastTime = currentTime;
            }
        }
    }
    
    private void update(double deltaTime) {
        engine.update(deltaTime);
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        
        // Enable anti-aliasing for smoother shapes
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, 
                            RenderingHints.VALUE_ANTIALIAS_ON);

        // Apply screen shake translation if any
        AffineTransform old = g2d.getTransform();
        double shakeX = engine.getShakeX();
        double shakeY = engine.getShakeY();
        if (shakeX != 0.0 || shakeY != 0.0) {
            g2d.translate(shakeX, shakeY);
        }
        
        switch (engine.getGameState()) {
            case MENU:
                menuUI.render(g2d, engine);
                break;
            case DIFFICULTY_SELECT:
                GameData data = engine.getSaveManager().loadGame();
                int unlockedDiff = (data != null) ? data.getUnlockedDifficulty() : 1;
                difficultySelectUI.render(g2d, engine, unlockedDiff);
                break;
            case PLAYING:
            case PAUSED:
                gameUI.render(g2d, engine);
                // render damage texts on top of game UI so they are visible
                for (var d : engine.getDamageTexts()) {
                    d.render(g2d);
                }
                break;
            case SHOP:
                shopUI.render(g2d, engine);
                break;
            case SETTINGS:
                settingsUI.render(g2d, engine);
                break;
            case UPGRADES:
                upgradesUI.render(g2d, engine);
                break;
            case WIN:
            case GAME_OVER:
                gameUI.render(g2d, engine);
                renderGameEndScreen(g2d);
                break;
        }
        
        // restore transform so UI overlays like system cursor etc. aren't shifted
        g2d.setTransform(old);
    }
    
    private void renderGameEndScreen(Graphics2D g2d) {
        g2d.setColor(new Color(0, 0, 0, 180));
        g2d.fillRect(0, 0, PANEL_WIDTH, PANEL_HEIGHT);
        
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Arial", Font.BOLD, 48));
        
        String message = engine.getGameState() == GameEngine.GameState.WIN 
                        ? "VICTORY!" : "GAME OVER";
        FontMetrics fm = g2d.getFontMetrics();
        int x = (PANEL_WIDTH - fm.stringWidth(message)) / 2;
        g2d.drawString(message, x, PANEL_HEIGHT / 2);
    }
    
    @Override
    public void keyPressed(KeyEvent e) {
        int key = e.getKeyCode();
        
        switch (engine.getGameState()) {
            case MENU:
                GameData saveData = engine.getSaveManager().loadGame();
                boolean hasSave = (saveData != null && saveData.getCash() > 0);
                
                if (key == KeyEvent.VK_C && hasSave) {
                    // Continue from last difficulty
                    engine.getSoundManager().playSound("click_button");
                    engine.startGame(engine.getCurrentDifficulty());
                } else if (key == KeyEvent.VK_N && hasSave) {
                    // New game - select difficulty
                    engine.getSoundManager().playSound("click_button");
                    engine.openDifficultySelect();
                } else if (key == KeyEvent.VK_SPACE && !hasSave) {
                    // No save - go directly to difficulty select
                    engine.getSoundManager().playSound("click_button");
                    engine.openDifficultySelect();
                } else if (key == KeyEvent.VK_U) {
                    engine.getSoundManager().playSound("click_button");
                    engine.openUpgrades();
                } else if (key == KeyEvent.VK_S) {
                    engine.getSoundManager().playSound("click_button");
                    engine.openSettings();
                } else if (key == KeyEvent.VK_ESCAPE) {
                    engine.cleanup();
                    System.exit(0);
                }
                break;
                
            case DIFFICULTY_SELECT:
                if (key >= KeyEvent.VK_1 && key <= KeyEvent.VK_6) {
                    int selectedDiff = key - KeyEvent.VK_0;
                    GameData data = engine.getSaveManager().loadGame();
                    int unlockedDiff = (data != null) ? data.getUnlockedDifficulty() : 1;
                    
                    if (selectedDiff <= unlockedDiff) {
                        engine.getSoundManager().playSound("click_button");
                        engine.startGame(selectedDiff);
                    }
                } else if (key == KeyEvent.VK_E) {
                    // Endless mode - always accessible
                    engine.getSoundManager().playSound("click_button");
                    engine.startGame(999);
                } else if (key == KeyEvent.VK_ESCAPE) {
                    engine.getSoundManager().playSound("click_button");
                    engine.returnToMenu();
                }
                break;
                
            case PLAYING:
                if (key == KeyEvent.VK_P || key == KeyEvent.VK_ESCAPE) {
                    engine.togglePause();
                } else if (key == KeyEvent.VK_Q && engine.isPaused()) {
                    // Allow quitting to menu when paused
                    engine.getSoundManager().playSound("click_button");
                    engine.quitToMenuFromGame();
                }
                break;
                
            case PAUSED:
                if (key == KeyEvent.VK_P || key == KeyEvent.VK_ESCAPE) {
                    engine.togglePause();
                } else if (key == KeyEvent.VK_Q) {
                    engine.getSoundManager().playSound("click_button");
                    engine.quitToMenuFromGame();
                }
                break;
                
            case SHOP:
                if (key == KeyEvent.VK_SPACE) {
                    engine.continueToNextWave();
                } else if (key >= KeyEvent.VK_1 && key <= KeyEvent.VK_8) {
                    handleShopPurchase(key - KeyEvent.VK_1);
                }
                break;
                
            case WIN:
            case GAME_OVER:
                if (key == KeyEvent.VK_SPACE || key == KeyEvent.VK_ESCAPE) {
                    engine.returnToMenu();
                }
                break;
                
            case SETTINGS:
                if (key == KeyEvent.VK_T) {
                    engine.getSoundManager().toggleSound();
                    engine.getSoundManager().playSound("click_button");
                } else if (key == KeyEvent.VK_LEFT) {
                    engine.getSoundManager().adjustVolume(-10);
                    engine.getSoundManager().playSound("click_button");
                } else if (key == KeyEvent.VK_RIGHT) {
                    engine.getSoundManager().adjustVolume(10);
                    engine.getSoundManager().playSound("click_button");
                } else if (key == KeyEvent.VK_MINUS) {
                    engine.getSoundManager().adjustVolume(-1);
                    engine.getSoundManager().playSound("click_button");
                } else if (key == KeyEvent.VK_EQUALS || key == KeyEvent.VK_PLUS) {
                    engine.getSoundManager().adjustVolume(1);
                    engine.getSoundManager().playSound("click_button");
                } else if ((key == KeyEvent.VK_A || key == KeyEvent.VK_Q) && engine.getHitSoundPlayer() != null) {
                    // Decrease latency (more negative/predictive)
                    int current = engine.getHitSoundPlayer().getLatencyOffset();
                    engine.getHitSoundPlayer().setLatencyOffset(Math.max(-100, current - 5));
                    engine.getSoundManager().playSound("click_button");
                } else if ((key == KeyEvent.VK_D || key == KeyEvent.VK_E) && engine.getHitSoundPlayer() != null) {
                    // Increase latency (more positive/delayed)
                    int current = engine.getHitSoundPlayer().getLatencyOffset();
                    engine.getHitSoundPlayer().setLatencyOffset(Math.min(100, current + 5));
                    engine.getSoundManager().playSound("click_button");
                } else if (key == KeyEvent.VK_R) {
                    // Reset all data
                    engine.getSaveManager().resetSaveData();
                    engine.getUpgradeManager().resetTempUpgrades();
                    // Reload upgrade manager to sync with reset save file
                    engine.reloadUpgrades();
                    engine.getSoundManager().playSound("click_button");
                    // Return to menu
                    engine.returnToMenu();
                } else if (key == KeyEvent.VK_ESCAPE) {
                    engine.getSoundManager().playSound("click_button");
                    engine.returnToMenu();
                }
                break;
                
            case UPGRADES:
                if (key >= KeyEvent.VK_1 && key <= KeyEvent.VK_8) {
                    handlePermanentUpgrade(key - KeyEvent.VK_1);
                } else if (key == KeyEvent.VK_ESCAPE) {
                    engine.getSoundManager().playSound("click_button");
                    engine.returnToMenu();
                }
                break;
        }
    }
    
    private void handlePermanentUpgrade(int upgradeIndex) {
        UpgradeManager.UpgradeType[] types = {
            UpgradeManager.UpgradeType.FIRE_RATE,
            UpgradeManager.UpgradeType.DAMAGE,
            UpgradeManager.UpgradeType.HEALTH,
            UpgradeManager.UpgradeType.SPEED,
            UpgradeManager.UpgradeType.BULLET_COUNT,
            UpgradeManager.UpgradeType.BULLET_SPEED,
            UpgradeManager.UpgradeType.CRIT_CHANCE,
            UpgradeManager.UpgradeType.CRIT_DAMAGE
        };
        
        if (upgradeIndex >= 0 && upgradeIndex < types.length) {
            GameData data = engine.getSaveManager().loadGame();
            if (data != null) {
                UpgradeManager.UpgradeType type = types[upgradeIndex];
                int cost = engine.getUpgradeManager().getUpgradeCost(type, 
                           engine.getUpgradeManager().getPermanentLevel(type));
                
                if (data.spendCash(cost)) {
                    // Increment the permanent upgrade level
                    engine.getUpgradeManager().incrementLevel(type);
                    
                    // Save with updated upgrades AND remaining cash
                    int latency = (engine.getHitSoundPlayer() != null) ? 
                                  engine.getHitSoundPlayer().getLatencyOffset() : 0;
                    GameData newData = new GameData(engine.getCurrentDifficulty(), 
                                                    engine.getUpgradeManager(),
                                                    latency);
                    // Cash was already spent from 'data', now preserve what's left
                    newData.addCash(data.getCash());
                    engine.getSaveManager().saveGame(newData);
                    engine.getSoundManager().playSound("click_button");
                }
            }
        }
    }
    
    private void handleShopPurchase(int upgradeIndex) {
        UpgradeManager.UpgradeType[] types = {
            UpgradeManager.UpgradeType.FIRE_RATE,
            UpgradeManager.UpgradeType.DAMAGE,
            UpgradeManager.UpgradeType.HEALTH,
            UpgradeManager.UpgradeType.SPEED,
            UpgradeManager.UpgradeType.BULLET_COUNT,
            UpgradeManager.UpgradeType.BULLET_SPEED,
            UpgradeManager.UpgradeType.CRIT_CHANCE,
            UpgradeManager.UpgradeType.CRIT_DAMAGE
        };
        
        if (upgradeIndex >= 0 && upgradeIndex < types.length) {
            UpgradeManager.UpgradeType type = types[upgradeIndex];
            int currentTempLevel = engine.getUpgradeManager().getTempLevel(type);
            int cost = engine.getUpgradeManager().getTempUpgradeCost(type, currentTempLevel);
            
            if (engine.getPlayer().spendCoins(cost)) {
                engine.getUpgradeManager().purchaseTempUpgrade(type);
                // Reset player to apply new upgrade values
                engine.getPlayer().reset();
                engine.getSoundManager().playSound("click_button");
            }
        }
    }
    
    @Override
    public void keyReleased(KeyEvent e) {
        // Not used for game state control
    }
    
    @Override
    public void keyTyped(KeyEvent e) {
        // Not used
    }
    
    // MouseListener implementation - request focus on click
    @Override
    public void mouseClicked(MouseEvent e) {
        requestFocusInWindow();
    }
    
    @Override
    public void mousePressed(MouseEvent e) {
        requestFocusInWindow();
    }
    
    @Override
    public void mouseReleased(MouseEvent e) {}
    
    @Override
    public void mouseEntered(MouseEvent e) {}
    
    @Override
    public void mouseExited(MouseEvent e) {}
}
