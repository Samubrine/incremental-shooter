package game.ui;

import game.GameEngine;
import game.systems.UpgradeManager;
import game.data.GameData;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

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
        
        switch (engine.getGameState()) {
            case MENU:
                menuUI.render(g2d);
                break;
            case PLAYING:
            case PAUSED:
                gameUI.render(g2d, engine);
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
                if (key == KeyEvent.VK_SPACE) {
                    engine.startGame(1); // Start at difficulty 1
                } else if (key == KeyEvent.VK_U) {
                    engine.openUpgrades();
                } else if (key == KeyEvent.VK_S) {
                    engine.openSettings();
                } else if (key == KeyEvent.VK_ESCAPE) {
                    System.exit(0);
                }
                break;
                
            case PLAYING:
                if (key == KeyEvent.VK_P || key == KeyEvent.VK_ESCAPE) {
                    engine.togglePause();
                }
                break;
                
            case PAUSED:
                if (key == KeyEvent.VK_P || key == KeyEvent.VK_ESCAPE) {
                    engine.togglePause();
                }
                break;
                
            case SHOP:
                if (key == KeyEvent.VK_SPACE) {
                    engine.continueToNextWave();
                } else if (key >= KeyEvent.VK_1 && key <= KeyEvent.VK_6) {
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
                if (key == KeyEvent.VK_S) {
                    engine.getSoundManager().toggleSound();
                } else if (key == KeyEvent.VK_R) {
                    // Reset all data
                    engine.getSaveManager().resetSaveData();
                    engine.getUpgradeManager().resetTempUpgrades();
                    // Return to menu
                    engine.returnToMenu();
                } else if (key == KeyEvent.VK_ESCAPE) {
                    engine.returnToMenu();
                }
                break;
                
            case UPGRADES:
                if (key >= KeyEvent.VK_1 && key <= KeyEvent.VK_6) {
                    handlePermanentUpgrade(key - KeyEvent.VK_1);
                } else if (key == KeyEvent.VK_ESCAPE) {
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
            UpgradeManager.UpgradeType.BULLET_SPEED
        };
        
        if (upgradeIndex >= 0 && upgradeIndex < types.length) {
            GameData data = engine.getSaveManager().loadGame();
            if (data != null) {
                UpgradeManager.UpgradeType type = types[upgradeIndex];
                int cost = engine.getUpgradeManager().getUpgradeCost(type, 
                           engine.getUpgradeManager().getPermanentLevel(type));
                
                if (data.spendCash(cost)) {
                    engine.getUpgradeManager().incrementLevel(type);
                    // Save updated cash and upgrades
                    GameData newData = new GameData(engine.getCurrentDifficulty(), 
                                                    engine.getUpgradeManager());
                    newData.addCash(data.getCash()); // Preserve remaining cash
                    engine.getSaveManager().saveGame(newData);
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
            UpgradeManager.UpgradeType.BULLET_SPEED
        };
        
        if (upgradeIndex >= 0 && upgradeIndex < types.length) {
            if (engine.getPlayer().spendCoins(50)) {
                engine.getUpgradeManager().purchaseTempUpgrade(types[upgradeIndex]);
                // Reset player to apply new upgrade values
                engine.getPlayer().reset();
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
