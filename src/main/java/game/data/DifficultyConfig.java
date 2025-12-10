package game.data;

/**
 * Configurable difficulty settings for easy developer adjustment.
 * Each difficulty has unique scaling for HP, coins, cash, and enemy types.
 */
public class DifficultyConfig {
    private int difficultyLevel;
    private String name;
    private double enemyHPMultiplier;
    private double coinsMultiplier;
    private double cashMultiplier;
    private String specialEnemies;
    
    // === DEVELOPER CONFIGURATION ZONE ===
    // Adjust these arrays to change difficulty scaling for the entire game
    
    private static final String[] DIFFICULTY_NAMES = {
        "Easy",
        "Normal", 
        "Hard",
        "Expert",
        "Master",
        "Nightmare"
    };
    
    private static final double[] HP_MULTIPLIERS = {
        1.0,    // Easy: 100% HP
        1.3,    // Normal: 130% HP
        1.7,    // Hard: 170% HP
        2.2,    // Expert: 220% HP
        3.0,    // Master: 300% HP
        4.0     // Nightmare: 400% HP
    };
    
    private static final double[] COINS_MULTIPLIERS = {
        1.5,    // Easy: 150% coins
        1.2,    // Normal: 120% coins
        1.0,    // Hard: 100% coins
        0.8,    // Expert: 80% coins
        0.6,    // Master: 60% coins
        0.5     // Nightmare: 50% coins
    };
    
    private static final double[] CASH_MULTIPLIERS = {
        0.8,    // Easy: 80% cash
        1.0,    // Normal: 100% cash
        1.3,    // Hard: 130% cash
        1.7,    // Expert: 170% cash
        2.5,    // Master: 250% cash
        4.0     // Nightmare: 400% cash
    };
    
    private static final String[] SPECIAL_ENEMIES = {
        "None",
        "Mages at Wave 7",
        "Mages at Wave 5",
        "Mages at Wave 3",
        "Mages & Bosses",
        "All Enemy Types"
    };
    
    // === ENDLESS MODE CONFIGURATION ===
    // Customize endless mode scaling (Difficulty 999)
    private static final String ENDLESS_NAME = "Endless";
    private static final double ENDLESS_HP_MULTIPLIER = 5;    // 500% HP & Damage
    private static final double ENDLESS_COINS_MULTIPLIER = 1.2; // 120% coins
    private static final double ENDLESS_CASH_MULTIPLIER = 3.0;  // 300% cash
    private static final String ENDLESS_SPECIAL = "Infinite Waves";
    
    // === END CONFIGURATION ZONE ===
    
    /**
     * Get configuration for a specific difficulty level (1-6, or 999 for Endless).
     */
    public static DifficultyConfig getConfig(int level) {
        DifficultyConfig config = new DifficultyConfig();
        
        // Endless mode (999)
        if (level == 999) {
            config.difficultyLevel = 999;
            config.name = ENDLESS_NAME;
            config.enemyHPMultiplier = ENDLESS_HP_MULTIPLIER;
            config.coinsMultiplier = ENDLESS_COINS_MULTIPLIER;
            config.cashMultiplier = ENDLESS_CASH_MULTIPLIER;
            config.specialEnemies = ENDLESS_SPECIAL;
            return config;
        }
        
        // Regular difficulties (1-6)
        level = Math.max(1, Math.min(6, level)); // Clamp to 1-6
        int index = level - 1;
        
        config.difficultyLevel = level;
        config.name = DIFFICULTY_NAMES[index];
        config.enemyHPMultiplier = HP_MULTIPLIERS[index];
        config.coinsMultiplier = COINS_MULTIPLIERS[index];
        config.cashMultiplier = CASH_MULTIPLIERS[index];
        config.specialEnemies = SPECIAL_ENEMIES[index];
        
        return config;
    }
    
    /**
     * Get total number of difficulties available.
     */
    public static int getTotalDifficulties() {
        return DIFFICULTY_NAMES.length;
    }
    
    // Getters
    public int getDifficultyLevel() { return difficultyLevel; }
    public String getName() { return name; }
    public double getEnemyHPMultiplier() { return enemyHPMultiplier; }
    public double getCoinsMultiplier() { return coinsMultiplier; }
    public double getCashMultiplier() { return cashMultiplier; }
    public String getSpecialEnemies() { return specialEnemies; }
    
    /**
     * Get formatted display string for HP modifier.
     */
    public String getHPModifierText() {
        int percent = (int)((enemyHPMultiplier - 1.0) * 100);
        if (percent > 0) {
            return "+" + percent + "% Enemy HP";
        } else if (percent < 0) {
            return percent + "% Enemy HP";
        }
        return "Normal Enemy HP";
    }
    
    /**
     * Get formatted display string for coins modifier.
     */
    public String getCoinsModifierText() {
        int percent = (int)((coinsMultiplier - 1.0) * 100);
        if (percent > 0) {
            return "+" + percent + "% Coins";
        } else if (percent < 0) {
            return percent + "% Coins";
        }
        return "Normal Coins";
    }
    
    /**
     * Get formatted display string for cash modifier.
     */
    public String getCashModifierText() {
        int percent = (int)((cashMultiplier - 1.0) * 100);
        if (percent > 0) {
            return "+" + percent + "% Cash";
        } else if (percent < 0) {
            return percent + "% Cash";
        }
        return "Normal Cash";
    }
}
