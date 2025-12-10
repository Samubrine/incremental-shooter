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
    private int critChanceLevel;
    private int critDamageLevel;
    private boolean specialAbilityUnlocked;
    
    // Temporary upgrades tracking (for current run only)
    private int tempFireRateLevel;
    private int tempDamageLevel;
    private int tempHealthLevel;
    private int tempSpeedLevel;
    private int tempBulletCountLevel;
    private int tempBulletSpeedLevel;
    private int tempCritChanceLevel;
    private int tempCritDamageLevel;
    
    // === DEVELOPER CONFIGURATION ZONE ===
    // Adjust these values to balance each upgrade independently
    
    // PERMANENT UPGRADES (bought with cash)
    // Base costs for each upgrade type
    private static final int PERM_FIRE_RATE_BASE = 50;
    private static final int PERM_DAMAGE_BASE = 50;
    private static final int PERM_HEALTH_BASE = 50;
    private static final int PERM_SPEED_BASE = 50;
    private static final int PERM_BULLET_COUNT_BASE = 75;  // More expensive (powerful)
    private static final int PERM_BULLET_SPEED_BASE = 40;  // Cheaper (less impact)
    private static final int PERM_CRIT_CHANCE_BASE = 60;
    private static final int PERM_CRIT_DAMAGE_BASE = 60;
    
    // Cost multipliers per level for each upgrade type
    private static final double PERM_FIRE_RATE_MULT = 1.1;
    private static final double PERM_DAMAGE_MULT = 1.1;
    private static final double PERM_HEALTH_MULT = 1.1;
    private static final double PERM_SPEED_MULT = 1.1;
    private static final double PERM_BULLET_COUNT_MULT = 1.3;  // Scales faster
    private static final double PERM_BULLET_SPEED_MULT = 1.08;  // Scales slower
    private static final double PERM_CRIT_CHANCE_MULT = 1.12;
    private static final double PERM_CRIT_DAMAGE_MULT = 1.12;
    
    // TEMPORARY UPGRADES (bought with coins, per-run)
    // Base costs for each upgrade type
    private static final int TEMP_FIRE_RATE_BASE = 50;
    private static final int TEMP_DAMAGE_BASE = 50;
    private static final int TEMP_HEALTH_BASE = 50;
    private static final int TEMP_SPEED_BASE = 50;
    private static final int TEMP_BULLET_COUNT_BASE = 75;  // More expensive (powerful)
    private static final int TEMP_BULLET_SPEED_BASE = 40;  // Cheaper (less impact)
    private static final int TEMP_CRIT_CHANCE_BASE = 60;
    private static final int TEMP_CRIT_DAMAGE_BASE = 60;
    
    // Cost multipliers per level for each upgrade type
    private static final double TEMP_FIRE_RATE_MULT = 1.1;
    private static final double TEMP_DAMAGE_MULT = 1.1;
    private static final double TEMP_HEALTH_MULT = 1.1;
    private static final double TEMP_SPEED_MULT = 1.1;
    private static final double TEMP_BULLET_COUNT_MULT = 1.3;  // Scales faster
    private static final double TEMP_BULLET_SPEED_MULT = 1.08;  // Scales slower
    private static final double TEMP_CRIT_CHANCE_MULT = 1.12;
    private static final double TEMP_CRIT_DAMAGE_MULT = 1.12;
    
    // === END CONFIGURATION ZONE ===
    
    public UpgradeManager() {
        fireRateLevel = 0;
        damageLevel = 0;
        healthLevel = 0;
        speedLevel = 0;
        bulletCountLevel = 0;
        bulletSpeedLevel = 0;
        critChanceLevel = 0;
        critDamageLevel = 0;
        specialAbilityUnlocked = false;
        
        tempFireRateLevel = 0;
        tempDamageLevel = 0;
        tempHealthLevel = 0;
        tempSpeedLevel = 0;
        tempBulletCountLevel = 0;
        tempBulletSpeedLevel = 0;
        tempCritChanceLevel = 0;
        tempCritDamageLevel = 0;
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
        tempCritChanceLevel = 0;
        tempCritDamageLevel = 0;
    }
    
    /**
     * Get actual stat value calculation (base + permanent + temp).
     * Used for displaying stats in upgrade UI.
     */
    public String getStatCalculation(UpgradeType type) {
        switch (type) {
            case FIRE_RATE:
                double baseFireRate = 0.5; // 2 shots/sec
                double fireRate = baseFireRate / (1 + getLevel(type) * 0.3);
                return String.format("%.2f shots/sec (Base: %.2f, +%.0f%%)", 
                    1.0/fireRate, 1.0/baseFireRate, getLevel(type) * 30.0);
            case DAMAGE:
                int baseDmg = 10;
                int totalDmg = baseDmg + (getLevel(type) * 5);
                return String.format("%d (Base: %d, +%d)", totalDmg, baseDmg, getLevel(type) * 5);
            case HEALTH:
                int baseHp = 100;
                int totalHp = baseHp + (getLevel(type) * 20);
                return String.format("%d (Base: %d, +%d)", totalHp, baseHp, getLevel(type) * 20);
            case SPEED:
                int baseSpeed = 200;
                int totalSpeed = baseSpeed + (getLevel(type) * 20);
                return String.format("%d (Base: %d, +%d)", totalSpeed, baseSpeed, getLevel(type) * 20);
            case BULLET_COUNT:
                int baseBullets = 1;
                int totalBullets = baseBullets + getLevel(type);
                return String.format("%d bullets (Base: %d, +%d)", totalBullets, baseBullets, getLevel(type));
            case BULLET_SPEED:
                int baseBSpeed = 400;
                int totalBSpeed = baseBSpeed + (getLevel(type) * 50);
                return String.format("%d (Base: %d, +%d)", totalBSpeed, baseBSpeed, getLevel(type) * 50);
            case CRIT_CHANCE:
                double baseCrit = 10.0;
                double totalCrit = baseCrit + (getLevel(type) * 0.5);
                return String.format("%.1f%% (Base: %.0f%%, +%.1f%%)", totalCrit, baseCrit, getLevel(type) * 0.5);
            case CRIT_DAMAGE:
                double baseCritDmg = 50.0; // 1.5x = +50%
                double totalCritDmg = baseCritDmg + (getLevel(type) * 1.0);
                return String.format("+%.0f%% (Base: +%.0f%%, +%.0f%%)", totalCritDmg, baseCritDmg, getLevel(type) * 1.0);
            default:
                return "Unknown";
        }
    }
    
    /**
     * Calculate cost for next permanent upgrade level.
     * Each upgrade type has its own base cost and scaling multiplier.
     * @param type The upgrade type
     * @param currentPermanentLevel The current permanent level of this upgrade
     */
    public int getUpgradeCost(UpgradeType type, int currentPermanentLevel) {
        int baseCost;
        double multiplier;
        
        switch (type) {
            case FIRE_RATE:
                baseCost = PERM_FIRE_RATE_BASE;
                multiplier = PERM_FIRE_RATE_MULT;
                break;
            case DAMAGE:
                baseCost = PERM_DAMAGE_BASE;
                multiplier = PERM_DAMAGE_MULT;
                break;
            case HEALTH:
                baseCost = PERM_HEALTH_BASE;
                multiplier = PERM_HEALTH_MULT;
                break;
            case SPEED:
                baseCost = PERM_SPEED_BASE;
                multiplier = PERM_SPEED_MULT;
                break;
            case BULLET_COUNT:
                baseCost = PERM_BULLET_COUNT_BASE;
                multiplier = PERM_BULLET_COUNT_MULT;
                break;
            case BULLET_SPEED:
                baseCost = PERM_BULLET_SPEED_BASE;
                multiplier = PERM_BULLET_SPEED_MULT;
                break;
            case CRIT_CHANCE:
                baseCost = PERM_CRIT_CHANCE_BASE;
                multiplier = PERM_CRIT_CHANCE_MULT;
                break;
            case CRIT_DAMAGE:
                baseCost = PERM_CRIT_DAMAGE_BASE;
                multiplier = PERM_CRIT_DAMAGE_MULT;
                break;
            default:
                baseCost = 50;
                multiplier = 1.1;
        }
        
        return (int)(baseCost * Math.pow(multiplier, currentPermanentLevel));
    }
    
    /**
     * Calculate cost for next temporary upgrade level.
     * Each upgrade type has its own base cost and scaling multiplier.
     * @param type The upgrade type
     * @param currentTempLevel The current temporary level of this upgrade
     */
    public int getTempUpgradeCost(UpgradeType type, int currentTempLevel) {
        int baseCost;
        double multiplier;
        
        switch (type) {
            case FIRE_RATE:
                baseCost = TEMP_FIRE_RATE_BASE;
                multiplier = TEMP_FIRE_RATE_MULT;
                break;
            case DAMAGE:
                baseCost = TEMP_DAMAGE_BASE;
                multiplier = TEMP_DAMAGE_MULT;
                break;
            case HEALTH:
                baseCost = TEMP_HEALTH_BASE;
                multiplier = TEMP_HEALTH_MULT;
                break;
            case SPEED:
                baseCost = TEMP_SPEED_BASE;
                multiplier = TEMP_SPEED_MULT;
                break;
            case BULLET_COUNT:
                baseCost = TEMP_BULLET_COUNT_BASE;
                multiplier = TEMP_BULLET_COUNT_MULT;
                break;
            case BULLET_SPEED:
                baseCost = TEMP_BULLET_SPEED_BASE;
                multiplier = TEMP_BULLET_SPEED_MULT;
                break;
            case CRIT_CHANCE:
                baseCost = TEMP_CRIT_CHANCE_BASE;
                multiplier = TEMP_CRIT_CHANCE_MULT;
                break;
            case CRIT_DAMAGE:
                baseCost = TEMP_CRIT_DAMAGE_BASE;
                multiplier = TEMP_CRIT_DAMAGE_MULT;
                break;
            default:
                baseCost = 50;
                multiplier = 1.1;
        }
        
        return (int)(baseCost * Math.pow(multiplier, currentTempLevel));
    }
    
    /**
     * Get current temporary level for an upgrade type.
     */
    public int getTempLevel(UpgradeType type) {
        switch (type) {
            case FIRE_RATE: return tempFireRateLevel;
            case DAMAGE: return tempDamageLevel;
            case HEALTH: return tempHealthLevel;
            case SPEED: return tempSpeedLevel;
            case BULLET_COUNT: return tempBulletCountLevel;
            case BULLET_SPEED: return tempBulletSpeedLevel;
            case CRIT_CHANCE: return tempCritChanceLevel;
            case CRIT_DAMAGE: return tempCritDamageLevel;
            default: return 0;
        }
    }
    
    /**
     * Attempt to purchase permanent upgrade with cash.
     */
    public boolean purchaseUpgrade(UpgradeType type, int cash) {
        int currentLevel = getPermanentLevel(type); // Use permanent level for cost calculation
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
            case CRIT_CHANCE: tempCritChanceLevel++; break;
            case CRIT_DAMAGE: tempCritDamageLevel++; break;
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
            case CRIT_CHANCE: return critChanceLevel + tempCritChanceLevel;
            case CRIT_DAMAGE: return critDamageLevel + tempCritDamageLevel;
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
            case CRIT_CHANCE: return critChanceLevel;
            case CRIT_DAMAGE: return critDamageLevel;
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
            case CRIT_CHANCE: critChanceLevel++; break;
            case CRIT_DAMAGE: critDamageLevel++; break;
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
    public int getCritChanceLevel() { return critChanceLevel + tempCritChanceLevel; }
    public int getCritDamageLevel() { return critDamageLevel + tempCritDamageLevel; }
    public boolean hasSpecialAbility() { return specialAbilityUnlocked; }
    
    public enum UpgradeType {
        FIRE_RATE, DAMAGE, HEALTH, SPEED, BULLET_COUNT, BULLET_SPEED, CRIT_CHANCE, CRIT_DAMAGE
    }
    
    // For save/load
    public void loadUpgrades(game.data.GameData data) {
        this.fireRateLevel = data.getFireRateLevel();
        this.damageLevel = data.getDamageLevel();
        this.healthLevel = data.getHealthLevel();
        this.speedLevel = data.getSpeedLevel();
        this.bulletCountLevel = data.getBulletCountLevel();
        this.bulletSpeedLevel = data.getBulletSpeedLevel();
        this.critChanceLevel = data.getCritChanceLevel();
        this.critDamageLevel = data.getCritDamageLevel();
        this.specialAbilityUnlocked = data.hasSpecialAbility();
    }
}
