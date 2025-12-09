package game.systems;

/**
 * Manages permanent (cash) and temporary (coins) upgrades.
 * Cash is spent on permanent upgrades, coins on temporary wave upgrades.
 */
public class UpgradeManager {
    // Permanent upgrades (bought with cash)
    private int fireRateLevel;
    private int damageLevel;
    private int healthLevel;
    private int speedLevel;
    private int bulletCountLevel;
    private int bulletSpeedLevel;
    private boolean specialAbilityUnlocked;
    
    // Temporary upgrades tracking (for current run only)
    private int tempFireRateLevel;
    private int tempDamageLevel;
    private int tempHealthLevel;
    private int tempSpeedLevel;
    private int tempBulletCountLevel;
    private int tempBulletSpeedLevel;
    
    // Upgrade costs (1.1x scaling per level)
    private static final int BASE_COST = 50;
    private static final double COST_MULTIPLIER = 1.1;
    
    public UpgradeManager() {
        fireRateLevel = 0;
        damageLevel = 0;
        healthLevel = 0;
        speedLevel = 0;
        bulletCountLevel = 0;
        bulletSpeedLevel = 0;
        specialAbilityUnlocked = false;
        
        tempFireRateLevel = 0;
        tempDamageLevel = 0;
        tempHealthLevel = 0;
        tempSpeedLevel = 0;
        tempBulletCountLevel = 0;
        tempBulletSpeedLevel = 0;
    }
    
    /**
     * Reset temporary upgrades (called when starting new game).
     */
    public void resetTempUpgrades() {
        tempFireRateLevel = 0;
        tempDamageLevel = 0;
        tempHealthLevel = 0;
        tempSpeedLevel = 0;
        tempBulletCountLevel = 0;
        tempBulletSpeedLevel = 0;
    }
    
    /**
     * Calculate cost for next permanent upgrade level (1.1x per level for that specific upgrade).
     * @param type The upgrade type
     * @param currentPermanentLevel The current permanent level of this upgrade
     */
    public int getUpgradeCost(UpgradeType type, int currentPermanentLevel) {
        return (int)(BASE_COST * Math.pow(COST_MULTIPLIER, currentPermanentLevel));
    }
    
    /**
     * Get cost for temporary upgrade (always 50 coins).
     */
    public int getTempUpgradeCost() {
        return 50;
    }
    
    /**
     * Attempt to purchase permanent upgrade with cash.
     */
    public boolean purchaseUpgrade(UpgradeType type, int cash) {
        int currentLevel = getLevel(type);
        int cost = getUpgradeCost(type, currentLevel);
        
        if (cash >= cost) {
            incrementLevel(type);
            return true;
        }
        return false;
    }
    
    /**
     * Purchase temporary upgrade with coins (for current run).
     * Just increments the temp level - caller handles coin spending.
     */
    public void purchaseTempUpgrade(UpgradeType type) {
        switch (type) {
            case FIRE_RATE: tempFireRateLevel++; break;
            case DAMAGE: tempDamageLevel++; break;
            case HEALTH: tempHealthLevel++; break;
            case SPEED: tempSpeedLevel++; break;
            case BULLET_COUNT: tempBulletCountLevel++; break;
            case BULLET_SPEED: tempBulletSpeedLevel++; break;
        }
    }
    
    public int getLevel(UpgradeType type) {
        switch (type) {
            case FIRE_RATE: return fireRateLevel + tempFireRateLevel;
            case DAMAGE: return damageLevel + tempDamageLevel;
            case HEALTH: return healthLevel + tempHealthLevel;
            case SPEED: return speedLevel + tempSpeedLevel;
            case BULLET_COUNT: return bulletCountLevel + tempBulletCountLevel;
            case BULLET_SPEED: return bulletSpeedLevel + tempBulletSpeedLevel;
            default: return 0;
        }
    }
    
    /**
     * Get only permanent level for an upgrade.
     */
    public int getPermanentLevel(UpgradeType type) {
        switch (type) {
            case FIRE_RATE: return fireRateLevel;
            case DAMAGE: return damageLevel;
            case HEALTH: return healthLevel;
            case SPEED: return speedLevel;
            case BULLET_COUNT: return bulletCountLevel;
            case BULLET_SPEED: return bulletSpeedLevel;
            default: return 0;
        }
    }
    
    public void incrementLevel(UpgradeType type) {
        switch (type) {
            case FIRE_RATE: fireRateLevel++; break;
            case DAMAGE: damageLevel++; break;
            case HEALTH: healthLevel++; break;
            case SPEED: speedLevel++; break;
            case BULLET_COUNT: bulletCountLevel++; break;
            case BULLET_SPEED: bulletSpeedLevel++; break;
        }
    }
    
    public void unlockSpecialAbility() {
        specialAbilityUnlocked = true;
    }
    
    // Getters - return combined permanent + temporary levels
    public int getFireRateLevel() { return fireRateLevel + tempFireRateLevel; }
    public int getDamageLevel() { return damageLevel + tempDamageLevel; }
    public int getHealthLevel() { return healthLevel + tempHealthLevel; }
    public int getSpeedLevel() { return speedLevel + tempSpeedLevel; }
    public int getBulletCountLevel() { return bulletCountLevel + tempBulletCountLevel; }
    public int getBulletSpeedLevel() { return bulletSpeedLevel + tempBulletSpeedLevel; }
    public boolean hasSpecialAbility() { return specialAbilityUnlocked; }
    
    public enum UpgradeType {
        FIRE_RATE, DAMAGE, HEALTH, SPEED, BULLET_COUNT, BULLET_SPEED
    }
    
    // For save/load
    public void loadUpgrades(game.data.GameData data) {
        this.fireRateLevel = data.getFireRateLevel();
        this.damageLevel = data.getDamageLevel();
        this.healthLevel = data.getHealthLevel();
        this.speedLevel = data.getSpeedLevel();
        this.bulletCountLevel = data.getBulletCountLevel();
        this.bulletSpeedLevel = data.getBulletSpeedLevel();
        this.specialAbilityUnlocked = data.hasSpecialAbility();
    }
}
