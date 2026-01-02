# EnhancedCompass - Developer Guide

## Table of Contents

1. [Architecture Overview](#architecture-overview)
2. [Class Structure](#class-structure)
3. [Core Systems](#core-systems)
4. [Data Flow](#data-flow)
5. [Key Implementation Details](#key-implementation-details)
6. [Extension Points](#extension-points)
7. [Build & Development](#build--development)
8. [Performance Considerations](#performance-considerations)
9. [Common Modifications](#common-modifications)

---

## Architecture Overview

### Design Philosophy

EnhancedCompass follows a **single-file architecture** pattern with modular inner classes. This design prioritizes:

- **Simplicity**: All code in one Java file for easy navigation
- **Maintainability**: Clear separation of concerns via inner classes
- **Readability**: Extensive JavaDoc and inline comments
- **Performance**: Minimal overhead, direct method calls

### Technology Stack

| Component | Technology | Purpose |
|-----------|------------|---------|
| Server API | Bukkit/Spigot/Paper | Core Minecraft server integration |
| Text/UI | Adventure API | Modern text components and boss bars |
| Structure Lookup | Bukkit Registry API | Accurate structure identification |
| Biome Lookup | Bukkit Registry API | Accurate biome identification |
| Configuration | Bukkit YAML | Config and player data storage |
| Scheduling | BukkitRunnable | Asynchronous boss bar updates and biome searches |

### Core Principles

1. **Zero External Dependencies**: No external JARs required
2. **Permission-Driven**: All features gated by permissions
3. **Data Persistence**: Targets saved immediately when set and on player quit
4. **Fail-Safe**: Errors logged but never crash server
5. **Performance First**: Biome searches run asynchronously; efficient algorithms

---

## Class Structure

### Main Class: `EnhancedCompass`

```java
public class EnhancedCompass extends JavaPlugin 
    implements CommandExecutor, TabCompleter, Listener
```

**Implements:**
- `JavaPlugin` - Bukkit plugin lifecycle
- `CommandExecutor` - Command handling
- `TabCompleter` - Tab completion
- `Listener` - Event handling (PlayerQuitEvent)

**Key Instance Variables:**
```java
private Map<UUID, CompassTarget> playerTargets;    // Player → Target mapping
private Map<UUID, BossBar> playerBossBars;         // Player → BossBar mapping
private ConfigManager configManager;               // Configuration facade
private BukkitRunnable updateTask;                 // Boss bar update task
private File playerDataFolder;                     // playerdata/ directory
```

**Key Methods:**
- `onEnable()` - Plugin initialization
- `onDisable()` - Cleanup and shutdown
- `onCommand()` - Command routing
- `onTabComplete()` - Tab completion
- `startUpdateTask()` - Initialize boss bar updates
- `updateBossBar()` - Create/update player boss bars
- `removeBossBar()` - Hide and remove boss bars
- `savePlayerTarget()` - Persist target to disk
- `loadPlayerTarget()` - Load target from disk (see note below)

---

### Inner Class: `CompassTarget`

```java
private static class CompassTarget {
    final String structureType;  // UPPER_CASE format (e.g., "ANCIENT_CITY" or "DARK_FOREST")
    final Location location;     // World + coordinates
    
    CompassTarget(String structureType, Location location) {
        this.structureType = structureType;
        this.location = location;
    }
}
```

**Purpose:** Immutable data holder for compass targets

**Design Pattern:** Data Transfer Object (DTO)

**Thread Safety:** Immutable (all fields final)

**Note:** The field is named `structureType` for historical reasons but stores both structure and biome type names.

---

### Inner Class: `ConfigManager`

```java
private static class ConfigManager {
    private final EnhancedCompass plugin;
    private int searchRadius;
    private List<String> blacklistedWorlds;
    private Map<String, Map<String, Boolean>> enabledStructures;  // Dimension → Structure → Enabled
    private Map<String, Map<String, Boolean>> enabledBiomes;      // Dimension → Biome → Enabled
}
```

**Purpose:** Configuration management and validation

**Design Pattern:** Facade

**Key Methods:**
- `getSearchRadius()` - Returns search radius in chunks
- `isWorldBlacklisted(String)` - Check if world is disabled
- `isStructureEnabled(Environment, String)` - Check if structure allowed
- `isBiomeEnabled(Environment, String)` - Check if biome allowed
- `getEnabledStructuresForEnvironment(Environment)` - List enabled structures
- `getEnabledBiomesForEnvironment(Environment)` - List enabled biomes

**Environment Mapping:**
- `NORMAL` → Overworld
- `NETHER` → Nether
- `THE_END` → End

---

## Core Systems

### 1. Boss Bar Update System

**Architecture:** Scheduled repeating task running every 10 ticks (0.5 seconds)

```
startUpdateTask()
       ↓
BukkitRunnable.runTaskTimer(plugin, 0L, 10L)
       ↓
For each online player:
       ↓
  Check if holding compass (main hand OR off hand)
       ↓
  If holding AND has target AND has permission:
       → updateBossBar(player, target)
       ↓
  If not holding:
       → removeBossBar(player)
```

**updateBossBar() Implementation:**
```java
private void updateBossBar(Player player, CompassTarget target) {
    BossBar bossBar = playerBossBars.get(player.getUniqueId());
    
    if (bossBar == null) {
        // Create new boss bar: empty text, 100% progress, blue, PROGRESS overlay
        bossBar = BossBar.bossBar(Component.empty(), 1.0f, 
            BossBar.Color.BLUE, BossBar.Overlay.PROGRESS);
        player.showBossBar(bossBar);
        playerBossBars.put(player.getUniqueId(), bossBar);
    }
    
    if (target.location != null && 
        player.getWorld().equals(target.location.getWorld())) {
        // Same dimension: show distance
        double distance = player.getLocation().distance(target.location);
        String name = formatStructureName(target.structureType);
        
        Component title = Component.text(name, NamedTextColor.AQUA)
            .append(Component.text(" - ", NamedTextColor.GRAY))
            .append(Component.text(String.format("%.0f", distance) + " blocks", 
                NamedTextColor.YELLOW));
        bossBar.name(title);
    } else {
        // Different dimension: show warning
        String name = formatStructureName(target.structureType);
        Component title = Component.text(name, NamedTextColor.RED)
            .append(Component.text(" - ", NamedTextColor.GRAY))
            .append(Component.text("Not in same dimension", NamedTextColor.RED));
        bossBar.name(title);
    }
    
    bossBar.progress(1.0f);
}
```

**Performance:**
- O(n) per tick where n = online players
- Only calculates distance for players holding compass
- Fast distance calculation via `Location.distance()`

---

### 2. Structure Search System

**Search Types:**
1. **Specific Structure** - `/enhancedcompass ancient_city`
2. **Generic Village** - `/enhancedcompass village`
3. **Generic Any** - `/enhancedcompass anything`

**Search Flow (Specific Structure):**

```
Player command: /enhancedcompass ancient_city
       ↓
Validate: permission, world not blacklisted, structure enabled
       ↓
Registry.STRUCTURE.get(NamespacedKey.minecraft("ancient_city"))
       ↓
World.locateNearestStructure(playerLoc, structure, radius, false)
       ↓
If found:
  → player.setCompassTarget(location)
  → playerTargets.put(uuid, new CompassTarget(...))
  → savePlayerTarget(player, target)
  → Send success messages
       ↓
If not found:
  → Send "not found within X blocks" message
```

**⚠️ Critical Implementation Detail:**

**Use `Structure` object, NOT `StructureType`:**

```java
// CORRECT - Uses specific Structure object
Structure structure = Registry.STRUCTURE.get(NamespacedKey.minecraft("ancient_city"));
world.locateNearestStructure(location, structure, radius, false);

// WRONG - Uses StructureType (causes incorrect results)
StructureType type = StructureType.JIGSAW;  // Too generic!
```

**Reason:** Many structures share the same StructureType. For example, villages, ancient cities, and trial chambers all use the "jigsaw" structure type. Using the Structure object ensures accurate targeting.

**Village Search:**
```java
String[] villageTypes = {
    "village_plains", "village_desert", "village_savanna", 
    "village_snowy", "village_taiga"
};
// Searches all enabled village types, returns closest
```

**Anything Search:**
```java
List<String> enabledStructures = configManager.getEnabledStructuresForEnvironment(environment);
// Searches all enabled structures, returns absolute closest
```

---

### 3. Biome Search System

**Search Type:** `/enhancedcompass biome <biome_name>`

**Search Flow:**

```
Player command: /enhancedcompass biome dark_forest
       ↓
Validate: permission, world not blacklisted, biome enabled
       ↓
Registry.BIOME.get(NamespacedKey.minecraft("dark_forest"))
       ↓
Send "Searching for nearest..." message
       ↓
BukkitRunnable.runTaskAsynchronously() {
    // OFF MAIN THREAD
    Location result = world.locateNearestBiome(
        playerLoc, 
        biome, 
        searchRadius * 16,  // Convert chunks to blocks
        32                   // Step size (matches vanilla)
    );
    
    BukkitRunnable.runTask() {
        // BACK ON MAIN THREAD
        if (result != null) {
            player.setCompassTarget(result);
            playerTargets.put(uuid, new CompassTarget(...));
            savePlayerTarget(player, target);
            Send success messages
        } else {
            Send "not found" message
        }
    }
}
```

**Key Differences from Structure Search:**

| Aspect | Structure Search | Biome Search |
|--------|-----------------|--------------|
| Registry | `Registry.STRUCTURE` | `Registry.BIOME` |
| API Method | `locateNearestStructure()` | `locateNearestBiome()` |
| Radius Unit | Chunks | Blocks |
| Execution | Synchronous | **Asynchronous** |
| Step Parameter | None | 32 blocks |

**Why Async?** Biome searches are computationally expensive as they require noise calculations. Running async prevents server lag during searches.

---

### 4. Data Persistence System

**Storage Location:** `plugins/EnhancedCompass/playerdata/`

**File Naming:** `<player-uuid>.yml`

**File Structure:**
```yaml
structure-type: ANCIENT_CITY
world: world
x: 123.456
y: -45.0
z: 789.012
```

**Save Lifecycle:**
```
Target Set (structure/biome found) → savePlayerTarget() immediately
       ↓
Player Quits → savePlayerTarget() (redundant backup)
       ↓
File persists on disk
```

**Load Note:**  
`loadPlayerTarget()` is fully implemented but there is no `PlayerJoinEvent` handler to call it automatically. Player targets ARE saved but NOT automatically restored on rejoin. To enable auto-restore, add:

```java
@EventHandler
public void onPlayerJoin(PlayerJoinEvent event) {
    loadPlayerTarget(event.getPlayer());
}
```

---

### 5. Tab Completion System

**First Argument Completions:**
- `help` - Always shown
- `current` - Always shown
- `biome` - Always shown
- `village` - Always shown
- `anything` - Always shown
- `reload` - Only for console or players with `enhancedcompass.reload`
- Structure names - Only enabled structures for player's current dimension

**Second Argument Completions (when first arg is "biome"):**
- Biome names - Only enabled biomes for player's current dimension

**Console Completions:**
- Shows ALL structures/biomes from Registry (no dimension filtering)

**Implementation:**
```java
if (sender instanceof Player) {
    Player player = (Player) sender;
    List<String> enabled = configManager.getEnabledStructuresForEnvironment(
        player.getWorld().getEnvironment()
    );
    completions.addAll(enabled.stream()
        .map(String::toLowerCase)
        .collect(Collectors.toList()));
} else {
    // Console: show everything
    Registry.STRUCTURE.forEach(s -> completions.add(s.getKey().getKey()));
}
```

---

## Data Flow

### Complete Command Flow

```
┌──────────────────────────────────────────────────────────────────┐
│                        PLAYER INPUT                               │
│                 /enhancedcompass ancient_city                     │
└──────────────────────────────────────────────────────────────────┘
                                  ↓
┌──────────────────────────────────────────────────────────────────┐
│                     COMMAND EXECUTOR                              │
│  1. Check if reload/help command                                  │
│  2. Verify sender is Player                                       │
│  3. Check enhancedcompass.use permission                          │
│  4. Check world not blacklisted                                   │
│  5. Route to appropriate handler                                  │
└──────────────────────────────────────────────────────────────────┘
                                  ↓
┌──────────────────────────────────────────────────────────────────┐
│                    STRUCTURE HANDLER                              │
│  1. Check structure enabled in config                             │
│  2. Get Structure from Registry                                   │
│  3. Call World.locateNearestStructure()                           │
│  4. If found: update compass target, save to disk                 │
│  5. Send result message to player                                 │
└──────────────────────────────────────────────────────────────────┘
                                  ↓
┌──────────────────────────────────────────────────────────────────┐
│                      BOSS BAR SYSTEM                              │
│  1. Update task runs every 10 ticks                               │
│  2. Checks if player holding compass                              │
│  3. Updates/removes boss bar accordingly                          │
│  4. Distance calculated from current player location              │
└──────────────────────────────────────────────────────────────────┘
```

---

## Key Implementation Details

### Structure vs StructureType

**Problem:** Many structures share the same `StructureType`. Villages, ancient cities, and trial chambers all use "jigsaw".

**Solution:** Use `Registry.STRUCTURE` to get specific `Structure` objects:
```java
Structure structure = Registry.STRUCTURE.get(NamespacedKey.minecraft("ancient_city"));
```

### Biome Search Step Size

The step parameter (32) in `locateNearestBiome()` matches vanilla `/locate biome` resolution:
- Vanilla uses 32 horizontal / 64 vertical
- Provides good balance of speed and accuracy
- Larger values = faster but less precise
- Smaller values = slower but more precise

### Compass Target Persistence

Targets are saved at two points:
1. **Immediately after set** - Primary persistence point
2. **On player quit** - Redundant backup

This ensures data survives crashes (data already on disk before any failure).

### Name Formatting

`formatStructureName()` converts internal names to display names:
```
ANCIENT_CITY → "Ancient City"
VILLAGE_PLAINS → "Village Plains"
DARK_FOREST → "Dark Forest"
```

Algorithm: Split on underscores, capitalize first letter of each word.

---

## Extension Points

### Adding New Commands

In `onCommand()`, add new command handlers before the structure search fallback:

```java
if (structureInput.equals("mycommand")) {
    // Handle custom command
    return true;
}
```

### Adding Economy Integration

```java
// In onCommand(), before structure/biome search:
double cost = 100.0;
if (economy != null && economy.has(player, cost)) {
    economy.withdrawPlayer(player, cost);
} else {
    player.sendMessage("Insufficient funds! Cost: " + cost);
    return true;
}
```

### Adding Cooldowns

```java
private Map<UUID, Long> lastSearch = new HashMap<>();
private long cooldown = 300000L; // 5 minutes

// In onCommand():
long last = lastSearch.getOrDefault(player.getUniqueId(), 0L);
if (System.currentTimeMillis() - last < cooldown) {
    long remaining = (cooldown - (System.currentTimeMillis() - last)) / 1000;
    player.sendMessage("Cooldown: " + remaining + " seconds remaining");
    return true;
}
lastSearch.put(player.getUniqueId(), System.currentTimeMillis());
```

### Enabling Auto-Restore on Join

```java
@EventHandler
public void onPlayerJoin(PlayerJoinEvent event) {
    loadPlayerTarget(event.getPlayer());
}
```

Don't forget to import `org.bukkit.event.player.PlayerJoinEvent`.

---

## Build & Development

### Requirements

- **Java:** 17+
- **Build Tool:** Maven or Gradle
- **IDE:** IntelliJ IDEA recommended

### Dependencies (pom.xml)

```xml
<dependencies>
    <dependency>
        <groupId>io.papermc.paper</groupId>
        <artifactId>paper-api</artifactId>
        <version>1.20.4-R0.1-SNAPSHOT</version>
        <scope>provided</scope>
    </dependency>
</dependencies>

<repositories>
    <repository>
        <id>papermc</id>
        <url>https://repo.papermc.io/repository/maven-public/</url>
    </repository>
</repositories>
```

### plugin.yml

```yaml
name: EnhancedCompass
version: 1.1.0
main: com.supafloof.enhancedcompass.EnhancedCompass
api-version: 1.19
author: SupaFloof Games, LLC
description: Enhanced compass with structure and biome finding

commands:
  enhancedcompass:
    description: Enhanced compass commands
    usage: /<command> <structure|biome|village|anything|current|help|reload>
    aliases: [ecompass]

permissions:
  enhancedcompass.use:
    description: Use enhanced compass features
    default: op
  enhancedcompass.reload:
    description: Reload configuration
    default: op
```

---

## Performance Considerations

### Boss Bar Updates
- O(n) where n = online players
- Only processes players holding compass
- Fast `Location.distance()` calculation

### Structure Searches
- Uses Bukkit's optimized `locateNearestStructure()`
- Runs synchronously (API handles optimization)
- One search per command

### Biome Searches
- Runs asynchronously to prevent server lag
- Step size of 32 blocks balances speed and accuracy
- Returns to main thread for Bukkit API calls

### Memory Usage

| Component | Per Player |
|-----------|------------|
| CompassTarget | ~100 bytes |
| BossBar | ~500 bytes |
| YAML file | ~200 bytes |

**Total for 1000 players:** ~800 KB (negligible)

---

## Code Style

### Formatting
- Indentation: 4 spaces
- Line length: 120 characters max
- Braces: Egyptian style

### Naming
- Classes: PascalCase
- Methods: camelCase
- Constants: UPPER_SNAKE_CASE
- Variables: camelCase

### Documentation
- JavaDoc for all methods
- Inline comments for complex logic
- Explain "why", not "what"

### Structure
- Single-file architecture
- Inner classes for modularity
- Follow existing patterns

---

## Version Compatibility

### Minecraft Versions

| Version | Status |
|---------|--------|
| 1.19.x | ✓ Supported |
| 1.20.x | ✓ Supported |
| 1.21.x | ✓ Supported |
| 1.18.x and earlier | ✗ No Registry API |

### Server Software

| Software | Status |
|----------|--------|
| Paper | ✓ Recommended |
| Spigot | ✓ Supported |
| Purpur | ✓ Supported |
| CraftBukkit | ✗ Missing Adventure API |
| Forge/Fabric | ✗ Wrong API |

---

## Resources

### Documentation
- [Bukkit API](https://hub.spigotmc.org/javadocs/bukkit/)
- [Paper API](https://jd.papermc.io/paper/1.20/)
- [Adventure API](https://docs.advntr.dev/)

### Tools
- [Paper Downloads](https://papermc.io/downloads)
- [IntelliJ IDEA](https://www.jetbrains.com/idea/)
- [Maven](https://maven.apache.org/)

---

## License & Credits

**Author:** SupaFloof Games, LLC  
**Version:** 1.1.0

---

**For additional information, refer to:**
- **End User Guide (enduser.md)** - Player documentation
- **Server Admin Guide (serveradmin.md)** - Configuration and management
- **README.md** - Overview and quick start
