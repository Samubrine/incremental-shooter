# 2D Incremental Shooter

A wave-based 2D shooter game built with Java Swing for Object-Oriented Programming (PBO) course.

## Game Features

- **Wave-based progression**: Survive 15 waves with increasing difficulty
- **Dual currency system**: 
  - Coins (temporary) - earned during runs, spent on wave upgrades
  - Cash (permanent) - earned on difficulty completion, spent on permanent upgrades
- **Enemy variety**:
  - Basic enemies (straight movement)
  - Mage enemies (shoot projectiles)
  - Boss enemies (every 5 waves)
- **Upgradeable stats**: Fire rate, damage, health, speed, bullet count, bullet speed
- **Multiple difficulties**: Unlock harder difficulties by completing previous ones
- **Auto-fire mechanic**: Automatic shooting with upgradeable fire rate
- **Save/load system**: Progress persists between sessions

## Controls

- **WASD**: Movement
- **P**: Pause game
- **ESC**: Return to menu
- **1-6**: Purchase upgrades (in shop)
- **SPACE**: Continue/Start

## Build & Run

### Option 1: Maven
```bash
mvn clean compile exec:java
```

### Option 2: Direct Java compilation
```bash
cd src/main/java
javac game/Main.java
java game.Main
```

### Option 3: Using IDE
Open the project in IntelliJ IDEA, Eclipse, or NetBeans and run `game.Main`

## Project Structure

```
src/main/java/game/
├── Main.java              # Entry point
├── GameEngine.java        # Core game logic and state management
├── entities/              # Game entities with inheritance
│   ├── Entity.java        # Base class
│   ├── Player.java
│   ├── Enemy.java         # Abstract enemy
│   ├── BasicEnemy.java
│   ├── MageEnemy.java
│   ├── BossEnemy.java
│   └── Projectile.java
├── systems/               # Game systems
│   ├── WaveManager.java
│   ├── CollisionManager.java
│   ├── InputManager.java
│   ├── UpgradeManager.java
│   ├── SoundManager.java
│   └── SaveManager.java
├── ui/                    # UI components
│   ├── GameWindow.java
│   ├── GamePanel.java
│   ├── MenuUI.java
│   ├── GameUI.java
│   └── ShopUI.java
└── data/                  # Data models
    └── GameData.java
```

## OOP Concepts Demonstrated

- **Inheritance**: Enemy hierarchy (Enemy → BasicEnemy, MageEnemy, BossEnemy)
- **Polymorphism**: Different enemy behaviors through abstract methods
- **Encapsulation**: Private fields with public getters
- **Abstraction**: Entity and Enemy abstract base classes
- **Composition**: Entities own projectiles, GameEngine coordinates systems
- **Singleton pattern**: GameEngine single instance

## Requirements

- Java 11 or higher
- No external dependencies (uses Java Swing)

## License

Educational project for PBO course.
