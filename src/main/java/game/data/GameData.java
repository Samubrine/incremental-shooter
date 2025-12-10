package game.data;

import game.systems.UpgradeManager;
import java.io.Serializable;

/**
 * Data class for save/load persistence.
 * Stores player progress, upgrades, and unlocked content.
 */
public class GameData implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private int cash;
    private int unlockedDifficulty;
    private int highScore;
    
    // Permanent upgrades
    private int fireRateLevel;
    private int damageLevel;
    private int healthLevel;
    private int speedLevel;
    private int bulletCountLevel;
    private int bulletSpeedLevel;
    private int critChanceLevel;
    private int critDamageLevel;
    private boolean specialAbilityUnlocked;
    
    // Audio settings
    private int audioLatencyOffset; // -100 to +100ms
    
    public GameData() {
        this.cash = 0;
        this.unlockedDifficulty = 1;
        this.highScore = 0;
        
        fireRateLevel = 0;
        damageLevel = 0;
        healthLevel = 0;
        speedLevel = 0;
        bulletCountLevel = 0;
        bulletSpeedLevel = 0;
        critChanceLevel = 0;
        critDamageLevel = 0;
        specialAbilityUnlocked = false;
        audioLatencyOffset = 0;
    }
    
    public GameData(int difficulty, UpgradeManager upgradeManager, int audioLatencyOffset) {
        this();
        this.unlockedDifficulty = difficulty;
        this.audioLatencyOffset = audioLatencyOffset;
        
        // Save ONLY permanent upgrades (not temporary ones)
        this.fireRateLevel = upgradeManager.getPermanentLevel(UpgradeManager.UpgradeType.FIRE_RATE);
        this.damageLevel = upgradeManager.getPermanentLevel(UpgradeManager.UpgradeType.DAMAGE);
        this.healthLevel = upgradeManager.getPermanentLevel(UpgradeManager.UpgradeType.HEALTH);
        this.speedLevel = upgradeManager.getPermanentLevel(UpgradeManager.UpgradeType.SPEED);
        this.bulletCountLevel = upgradeManager.getPermanentLevel(UpgradeManager.UpgradeType.BULLET_COUNT);
        this.bulletSpeedLevel = upgradeManager.getPermanentLevel(UpgradeManager.UpgradeType.BULLET_SPEED);
        this.critChanceLevel = upgradeManager.getPermanentLevel(UpgradeManager.UpgradeType.CRIT_CHANCE);
        this.critDamageLevel = upgradeManager.getPermanentLevel(UpgradeManager.UpgradeType.CRIT_DAMAGE);
        this.specialAbilityUnlocked = upgradeManager.hasSpecialAbility();
    }
    
    public void addCash(int amount) {
        cash += amount;
    }
    
    public boolean spendCash(int amount) {
        if (cash >= amount) {
            cash -= amount;
            return true;
        }
        return false;
    }
    
    public void unlockDifficulty(int difficulty) {
        if (difficulty > unlockedDifficulty) {
            unlockedDifficulty = difficulty;
        }
    }
    
    public void updateHighScore(int score) {
        if (score > highScore) {
            highScore = score;
        }
    }
    
    // Getters
    public int getCash() { return cash; }
    public int getUnlockedDifficulty() { return unlockedDifficulty; }
    public int getHighScore() { return highScore; }
    
    public int getFireRateLevel() { return fireRateLevel; }
    public int getDamageLevel() { return damageLevel; }
    public int getHealthLevel() { return healthLevel; }
    public int getSpeedLevel() { return speedLevel; }
    public int getBulletCountLevel() { return bulletCountLevel; }
    public int getBulletSpeedLevel() { return bulletSpeedLevel; }
    public int getCritChanceLevel() { return critChanceLevel; }
    public int getCritDamageLevel() { return critDamageLevel; }
    public boolean hasSpecialAbility() { return specialAbilityUnlocked; }
    public int getAudioLatencyOffset() { return audioLatencyOffset; }
}
