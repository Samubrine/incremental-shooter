package game.systems;

/**
 * Demonstration of HitSoundPlayer usage in a game loop.
 * 
 * This example shows:
 * 1. Initialization with preloaded sounds
 * 2. Non-blocking playback during gameplay
 * 3. Simultaneous sound playback
 * 4. Proper resource cleanup
 */
public class HitSoundPlayerDemo {
    
    public static void main(String[] args) throws InterruptedException {
        System.out.println("=== HitSoundPlayer Demo ===\n");
        
        // 1. INITIALIZATION (do this once at game startup)
        String soundDirectory = "src/main/resources/sounds";
        String[] soundNames = {
            "hit",
            "hit_critical",
            "player_damaged"
        };
        
        HitSoundPlayer hitSoundPlayer = new HitSoundPlayer(soundDirectory, soundNames);
        hitSoundPlayer.setMasterVolume(0.7f);
        
        System.out.println("\n=== Simulating Game Loop ===\n");
        
        // 2. IN GAME LOOP - Collision Detection Example
        simulateGameplay(hitSoundPlayer);
        
        // 3. SHUTDOWN (do this when game closes)
        System.out.println("\n=== Shutting Down ===");
        hitSoundPlayer.shutdown();
        
        System.out.println("Demo complete!");
    }
    
    /**
     * Simulate a game loop with collision events.
     */
    private static void simulateGameplay(HitSoundPlayer player) throws InterruptedException {
        // Example 1: Single hit
        System.out.println("Frame 0: Player bullet hits enemy");
        player.playHit("hit"); // <5ms latency, returns immediately
        Thread.sleep(100);
        
        // Example 2: Multiple simultaneous hits (e.g., multi-bullet hits multiple enemies)
        System.out.println("Frame 10: Multi-bullet hits 5 enemies simultaneously");
        for (int i = 0; i < 5; i++) {
            player.playHit("hit"); // All 5 sounds play overlapped
        }
        Thread.sleep(200);
        
        // Example 3: Critical hit
        System.out.println("Frame 25: Critical hit!");
        player.playHit("hit_critical");
        Thread.sleep(150);
        
        // Example 4: Rapid-fire scenario (10 hits in quick succession)
        System.out.println("Frame 40: Rapid-fire hitting enemy wave");
        for (int i = 0; i < 10; i++) {
            player.playHit("hit");
            Thread.sleep(30); // 33ms = ~30fps collision events
        }
        
        // Example 5: Player takes damage
        System.out.println("Frame 70: Player damaged by enemy");
        player.playHit("player_damaged");
        Thread.sleep(100);
        
        // Example 6: Chaos - multiple sound types simultaneously
        System.out.println("Frame 80: Chaotic battle - all sounds at once");
        player.playHit("hit");
        player.playHit("hit_critical");
        player.playHit("player_damaged");
        player.playHit("hit");
        player.playHit("hit");
        
        Thread.sleep(500); // Let sounds finish
    }
    
    /**
     * Example integration into actual game collision detection.
     */
    public static class GameLoopIntegration {
        private HitSoundPlayer hitSoundPlayer;
        
        public void initGame() {
            // Initialize during game startup
            hitSoundPlayer = new HitSoundPlayer("src/main/resources/sounds", 
                new String[]{"hit", "hit_critical", "player_damaged"});
        }
        
        public void updateGameLogic(double deltaTime) {
            // Your game update logic here...
            
            // Example: Check bullet-enemy collisions
            // for (Projectile bullet : bullets) {
            //     for (Enemy enemy : enemies) {
            //         if (bullet.collidesWith(enemy)) {
            //             enemy.takeDamage(bullet.damage);
            //             
            //             // Play sound with NO delay - returns instantly
            //             if (bullet.isCritical) {
            //                 hitSoundPlayer.playHit("hit_critical");
            //             } else {
            //                 hitSoundPlayer.playHit("hit");
            //             }
            //             
            //             bullet.alive = false;
            //         }
            //     }
            // }
            
            // Example: Check enemy-player collisions
            // for (Enemy enemy : enemies) {
            //     if (enemy.collidesWith(player)) {
            //         player.takeDamage(enemy.damage);
            //         hitSoundPlayer.playHit("player_damaged");
            //     }
            // }
        }
        
        public void cleanupGame() {
            // Cleanup when game closes
            hitSoundPlayer.shutdown();
        }
    }
}
