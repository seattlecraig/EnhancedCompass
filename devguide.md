# EnhancedCompass - Developer Guide

## Table of Contents

1. [Architecture Overview](#architecture-overview)
2. [Class Structure](#class-structure)
3. [Core Systems](#core-systems)
4. [Data Flow](#data-flow)
5. [Key Implementation Details](#key-implementation-details)
6. [Extension Points](#extension-points)
7. [Build & Development](#build--development)
8. [Testing Guidelines](#testing-guidelines)
9. [Performance Considerations](#performance-considerations)
10. [Common Modifications](#common-modifications)

---

## Architecture Overview

### Design Philosophy

EnhancedCompass follows a **single-file architecture** pattern with modular inner classes. This design choice prioritizes:

- **Simplicity**: All code in one file for easy navigation
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
| Scheduling | BukkitRunnable | Asynchronous boss bar updates |

### Core Principles

1. **Zero External Dependencies**: No external JARs required
2. **Permission-Driven**: All features gated by permissions
3. **Data Persistence**: All data saved immediately on change
4. **Fail-Safe**: Errors logged but never crash server
5. **Performance First**: Efficient algorithms, no blocking operations

---

## Class Structure

### Main Class: `EnhancedCompass`

```
EnhancedCompass extends JavaPlugin
                implements CommandExecutor, TabCompleter, Listener
```

**Implements:**
- `JavaPlugin` - Bukkit plugin lifecycle
- `CommandExecutor` - Command handling
- `TabCompleter` - Tab completion
- `Listener` - Event handling

**Key Responsibilities:**
- Plugin lifecycle (enable/disable)
- Command routing
- Event handling (player quit)
- Boss bar management
- Player target management

### Inner Enum: `TargetType`

```java
private enum TargetType {
    STRUCTURE,
    BIOME
}
```

**Purpose:** Differentiate between structure and biome targets

### Inner Class: `CompassTarget`

```java
private static class CompassTarget {
    final TargetType targetType;   // STRUCTURE or BIOME
    final String targetName;       // UPPER_CASE format (e.g., "ANCIENT_CITY" or "DARK_FOREST")
    final Location location;       // World + coordinates
}
```

**Purpose:** Immutable data holder for compass targets

**Design Pattern:** Data Transfer Object (DTO)

**Thread Safety:** Immutable (all fields final)

### Inner Class: `ConfigManager`

```java
private static class ConfigManager {
    private final EnhancedCompass plugin;
    private int searchRadius;
    private List<String> blacklistedWorlds;
    private Map<String, Map<String, Boolean>> enabledStructures;
    private Map<String, Map<String, Boolean>> enabledBiomes;
}
```

**Purpose:** Configuration management and validation

**Design Pattern:** Facade

**Responsibilities:**
- Load and parse config.yml
- Validate configuration values
- Provide config access methods
- Cache config values for performance

---

## Core Systems

### 1. Boss Bar Update System

**Architecture:** Scheduled repeating task

```
startUpdateTask()
  ↓
BukkitRunnable (10 ticks = 0.5s)
  ↓
For each online player:
  ↓
  Check if holding compass
  ↓
  If YES → updateBossBar()
  ↓
  If NO → removeBossBar()
```

**Key Methods:**
- `startUpdateTask()` - Initialize the repeating task
- `updateBossBar(Player, CompassTarget)` - Create/update boss bar
- `removeBossBar(Player)` - Hide and remove boss bar

**Implementation Details:**
```java
updateTask = new BukkitRunnable() {
    @Override
    public void run() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            boolean holdingCompass = /* check main + off hand */;
            if (holdingCompass && hasTarget && hasPermission) {
                updateBossBar(player, target);
            } else {
                removeBossBar(player);
            }
        }
    }
};
updateTask.runTaskTimer(this, 0L, 10L);
```

**Performance:**
- O(n) per tick where n = online players
- Only processes players holding compass
- Fast distance calculation (Location.distance())
- No blocking operations

---

### 2. Structure Search System

**Search Types:**

1. **Specific Structure** - `/enhancedcompass ancient_city`
2. **Generic Village** - `/enhancedcompass village`
3. **Generic Any** - `/enhancedcompass anything`

**Search Flow:**

```
Player Command
  ↓
Validate (permission, world, structure enabled)
  ↓
Registry.STRUCTURE.get(NamespacedKey)
  ↓
World.locateNearestStructure(location, structure, radius, findUnexplored)
  ↓
Set compass target + Store in memory + Save to disk
  ↓
Display results to player
```

**Critical Implementation Detail:**

⚠️ **Use Structure object, NOT StructureType**

```java
// CORRECT - Uses specific Structure object
Structure structure = Registry.STRUCTURE.get(NamespacedKey.minecraft("ancient_city"));
world.locateNearestStructure(location, structure, radius, false);

// WRONG - Uses StructureType (causes incorrect results)
StructureType type = StructureType.JIGSAW;  // Too generic!
world.locateNearestStructure(location, type, radius, false);
```

**Reason:** Many structures share the same StructureType (e.g., "jigsaw" type includes villages, ancient cities, trail chambers, etc.). Using the Structure object ensures accurate targeting.

---

### 3. Biome Search System

**Search Type:**
- **Specific Biome** - `/enhancedcompass biome dark_forest`

**Search Flow:**

```
Player Command: /enhancedcompass biome <biome_name>
  ↓
Validate (permission, world, biome enabled)
  ↓
Registry.BIOME.get(NamespacedKey)
  ↓
World.locateNearestBiome(location, biome, radius, step)
  ↓
Set compass target + Store in memory + Save to disk
  ↓
Display results to player
```

**Implementation:**

```java
// Get biome from registry
Biome biome = Registry.BIOME.get(NamespacedKey.minecraft(biomeInput));

// Search for nearest biome
// Note: radius is in BLOCKS (not chunks like structures)
// Step size of 8 provides good balance of speed and precision
var biomeResult = player.getWorld().locateNearestBiome(
    player.getLocation(),
    biome,
    searchRadius * 16,  // Convert chunks to blocks
    8  // Step size
);
```

**Key Differences from Structure Search:**
- Uses `Registry.BIOME` instead of `Registry.STRUCTURE`
- Uses `World.locateNearestBiome()` instead of `World.locateNearestStructure()`
- Radius parameter is in blocks, not chunks
- Has a step parameter for search granularity

---

### 4. Data Persistence System

**Storage Format:** YAML files in `playerdata/` directory

**File Naming:** `<player-uuid>.yml`

**File Structure (v1.1.0+):**
```yaml
target-type: STRUCTURE
target-name: ANCIENT_CITY
world: world
x: 123.456
y: -45.0
z: 789.012
```

Or for biome targets:
```yaml
target-type: BIOME
target-name: DARK_FOREST
world: world
x: 456.789
y: 64.0
z: 123.456
```

**Backwards Compatibility:**
Old format (v1.0.0) is still supported:
```yaml
structure-type: ANCIENT_CITY
world: world
x: 123.456
y: -45.0
z: 789.012
```

**Lifecycle:**

```
Set Target → Save Immediately
  ↓
Player Quits → Save on Quit
  ↓
Player Joins → Load from Disk
  ↓
Delayed Notification (1 second)
```

**Implementation:**

```java
// Save
private void savePlayerTarget(Player player, CompassTarget target) {
    YamlConfiguration config = new YamlConfiguration();
    config.set("target-type", target.targetType.toString());
    config.set("target-name", target.targetName);
    config.set("world", target.location.getWorld().getName());
    config.set("x", target.location.getX());
    config.set("y", target.location.getY());
    config.set("z", target.location.getZ());
    config.save(playerFile);
}

// Load (with backwards compatibility)
private void loadPlayerTarget(Player player) {
    YamlConfiguration config = YamlConfiguration.loadConfiguration(playerFile);
    String targetTypeStr = config.getString("target-type", "STRUCTURE");
    TargetType targetType = TargetType.valueOf(targetTypeStr);
    
    String targetName = config.getString("target-name");
    if (targetName == null) {
        // Backwards compatibility with old format
        targetName = config.getString("structure-type");
    }
    // ... create Location and CompassTarget
    playerTargets.put(player.getUniqueId(), target);
    player.setCompassTarget(location);
}
```

**Error Handling:**
- Missing file: Silent return (normal for new players)
- World not found: Log warning, return
- Parse error: Log warning, return
- Save failure: Log warning, continue execution

---

### 5. Configuration System

**Structure:**

```yaml
search-radius: 100
blacklisted-worlds: [...]
enabled-structures:
  normal: {...}
  nether: {...}
  the_end: {...}
enabled-biomes:
  normal: {...}
  nether: {...}
  the_end: {...}
```

**Loading Process:**

```
onEnable() / reload command
  ↓
reloadConfig()
  ↓
new ConfigManager(this)
  ↓
loadConfig()
  ↓
Parse and cache all values
```

**Caching Strategy:**
All config values cached in memory for O(1) access:
- `searchRadius` - int
- `blacklistedWorlds` - List<String>
- `enabledStructures` - Map<String, Map<String, Boolean>>
- `enabledBiomes` - Map<String, Map<String, Boolean>>

**Access Methods:**
```java
int getSearchRadius()
boolean isWorldBlacklisted(String worldName)
boolean isStructureEnabled(Environment env, String type)
boolean isBiomeEnabled(Environment env, String type)
List<String> getEnabledStructuresForEnvironment(Environment env)
List<String> getEnabledBiomesForEnvironment(Environment env)
```

---

## Data Flow

### Command Execution Flow (Structure)

```
Player types /enhancedcompass ancient_city
  ↓
CommandExecutor.onCommand() receives command
  ↓
Validate reload/help commands (special handling)
  ↓
Verify sender is Player (not console)
  ↓
Check permission (enhancedcompass.use)
  ↓
Check world blacklist
  ↓
Handle special commands (current, village, anything, biome)
  ↓
Validate structure enabled in config
  ↓
Get Structure from Registry
  ↓
Call World.locateNearestStructure()
  ↓
Create CompassTarget object (type=STRUCTURE)
  ↓
Store in playerTargets map
  ↓
Set vanilla compass target
  ↓
Save to disk
  ↓
Send success message with distance
```

### Command Execution Flow (Biome)

```
Player types /enhancedcompass biome dark_forest
  ↓
CommandExecutor.onCommand() receives command
  ↓
Validate args[0] == "biome"
  ↓
Verify sender is Player (not console)
  ↓
Check permission (enhancedcompass.use)
  ↓
Check world blacklist
  ↓
Validate biome enabled in config
  ↓
Get Biome from Registry
  ↓
Call World.locateNearestBiome()
  ↓
Create CompassTarget object (type=BIOME)
  ↓
Store in playerTargets map
  ↓
Set vanilla compass target
  ↓
Save to disk
  ↓
Send success message with distance
```

### Boss Bar Update Flow

```
Every 10 ticks (0.5 seconds):
  ↓
Iterate all online players
  ↓
Check if holding compass (main OR off hand)
  ↓
  If YES:
    ↓
    Get CompassTarget from map
    ↓
    Check permission
    ↓
    Calculate current distance
    ↓
    Update/create boss bar with formatted text
  ↓
  If NO:
    ↓
    Remove boss bar if exists
```

### Player Join/Quit Flow

```
Player Joins:
  (Note: Currently no join handler, but loadPlayerTarget() can be called from one)
  ↓
  loadPlayerTarget(player)
  ↓
  Load YAML file
  ↓
  Parse target-type (STRUCTURE or BIOME)
  ↓
  Validate world exists
  ↓
  Create CompassTarget
  ↓
  Set compass target
  ↓
  Schedule delayed notification

Player Quits:
  ↓
  PlayerQuitEvent fired
  ↓
  Remove boss bar
  ↓
  Get target from map
  ↓
  Save target to disk
```

---

## Key Implementation Details

### 1. Structure Type vs Structure Object

**The Problem:**
Minecraft's structure system has two concepts:
- `StructureType` - Generic type (e.g., "jigsaw", "stronghold")
- `Structure` - Specific structure (e.g., "ancient_city", "trial_chambers")

Many different structures share the same StructureType. For example, StructureType.JIGSAW includes:
- All village variants
- Ancient city
- Trail ruins
- Trial chambers
- And more...

**The Solution:**
Always use the specific Structure object from the Registry:

```java
// Get specific structure
Structure structure = Registry.STRUCTURE.get(
    NamespacedKey.minecraft("ancient_city")
);

// Use in search
world.locateNearestStructure(location, structure, radius, false);
```

**Impact:** This ensures accurate structure targeting and prevents finding the wrong structure type.

---

### 2. Biome Registry Lookup

**Implementation:**
```java
// Get biome from registry
Biome biome = Registry.BIOME.get(NamespacedKey.minecraft("dark_forest"));

// Validate
if (biome == null) {
    player.sendMessage("Invalid biome type");
    return true;
}

// Use in search
world.locateNearestBiome(location, biome, radiusInBlocks, stepSize);
```

**Key Points:**
- Biomes use `Registry.BIOME`
- Radius is in blocks (not chunks)
- Step size affects search granularity (lower = more precise, slower)

---

### 3. Boss Bar Lifecycle Management

**Creation:**
```java
if (bossBar == null) {
    bossBar = BossBar.bossBar(
        Component.empty(),
        1.0f,                    // Always 100%
        BossBar.Color.BLUE,
        BossBar.Overlay.PROGRESS
    );
    player.showBossBar(bossBar);
    playerBossBars.put(uuid, bossBar);
}
```

**Update:**
```java
// Same dimension - works for both structures and biomes
Component title = Component.text(targetName, NamedTextColor.AQUA)
    .append(Component.text(" - ", NamedTextColor.GRAY))
    .append(Component.text(distance + " blocks", NamedTextColor.YELLOW));

// Different dimension
Component title = Component.text(targetName, NamedTextColor.RED)
    .append(Component.text(" - ", NamedTextColor.GRAY))
    .append(Component.text("Not in same dimension", NamedTextColor.RED));
```

**Removal:**
```java
BossBar bossBar = playerBossBars.remove(uuid);
if (bossBar != null) {
    player.hideBossBar(bossBar);
}
```

**Key Points:**
- Boss bars created lazily (only when needed)
- Boss bars removed immediately when compass put away
- Progress always 100% (cosmetic bar, not progress indicator)
- Color-coded: Blue bar, Aqua=good, Red=warning

---

### 4. Distance Calculation

```java
double distance = player.getLocation().distance(target.location);
```

**Algorithm:** 3D Euclidean distance
```
distance = √[(x₂-x₁)² + (y₂-y₁)² + (z₂-z₁)²]
```

**Characteristics:**
- Includes Y-axis (vertical distance)
- Straight-line distance (as the crow flies)
- May not match walking distance due to terrain

**Display Formatting:**
```java
String.format("%.0f", distance)  // No decimal places
```

---

### 5. Name Formatting

```java
private String formatStructureName(String name) {
    String[] words = name.split("_");
    StringBuilder result = new StringBuilder();
    for (String word : words) {
        if (result.length() > 0) result.append(" ");
        result.append(word.substring(0, 1).toUpperCase())
              .append(word.substring(1).toLowerCase());
    }
    return result.toString();
}
```

**Transformations:**
- `ANCIENT_CITY` → "Ancient City"
- `VILLAGE_PLAINS` → "Village Plains"
- `DARK_FOREST` → "Dark Forest"
- `CHERRY_GROVE` → "Cherry Grove"

---

### 6. Tab Completion System

```java
@Override
public List<String> onTabComplete(...) {
    if (args.length == 1) {
        // Add subcommands
        completions.add("help");
        completions.add("current");
        completions.add("biome");     // NEW: biome subcommand
        completions.add("village");
        completions.add("anything");
        
        if (hasReloadPermission) {
            completions.add("reload");
        }
        
        // Add structure types based on sender
        if (sender instanceof Player) {
            completions.addAll(getEnabledStructuresForDimension());
        } else {
            Registry.STRUCTURE.forEach(s -> completions.add(s.getKey().getKey()));
        }
        
        return filterAndSort(completions, args[0]);
    }
    
    // NEW: Second argument for biome command
    if (args.length == 2 && args[0].equalsIgnoreCase("biome")) {
        if (sender instanceof Player) {
            completions.addAll(getEnabledBiomesForDimension());
        } else {
            Registry.BIOME.forEach(b -> completions.add(b.getKey().getKey()));
        }
        
        return filterAndSort(completions, args[1]);
    }
    
    return completions;
}
```

---

## Extension Points

### Adding New Features

**1. Add New Command:**
```java
// In onCommand()
if (args[0].equalsIgnoreCase("newcommand")) {
    // Your logic here
    return true;
}
```

**2. Add New Permission:**
```java
// Define in plugin.yml
permissions:
  enhancedcompass.newfeature:
    description: New feature permission
    default: op

// Check in code
if (player.hasPermission("enhancedcompass.newfeature")) {
    // Feature code
}
```

**3. Add New Config Option:**
```java
// In ConfigManager.loadConfig()
someNewValue = config.getBoolean("new-option", true);

// In config.yml
new-option: true
```

**4. Add Event Handler:**
```java
@EventHandler
public void onSomeEvent(SomeEvent event) {
    // Your logic
}
```

**5. Customize Boss Bar:**
```java
// In updateBossBar()
bossBar.color(BossBar.Color.RED);  // Change color
bossBar.overlay(BossBar.Overlay.NOTCHED_10);  // Change style
```

---

### Common Modifications

**1. Add Cooldown System:**
```java
private Map<UUID, Long> cooldowns = new HashMap<>();

// In onCommand(), before structure/biome search:
long lastUse = cooldowns.getOrDefault(player.getUniqueId(), 0L);
long cooldown = 60000L; // 60 seconds in milliseconds
if (System.currentTimeMillis() - lastUse < cooldown) {
    long remaining = (cooldown - (System.currentTimeMillis() - lastUse)) / 1000;
    player.sendMessage("Cooldown: " + remaining + " seconds remaining");
    return true;
}
cooldowns.put(player.getUniqueId(), System.currentTimeMillis());
```

**2. Add Cost System (Vault integration):**
```java
// In onCommand(), before structure/biome search:
if (!economy.has(player, cost)) {
    player.sendMessage("You need " + cost + " to use this!");
    return true;
}
economy.withdrawPlayer(player, cost);
```

**3. Add Distance Limit:**
```java
// After structure/biome found:
if (distance > maxDistance) {
    player.sendMessage("Target too far away! (Max: " + maxDistance + ")");
    return true;
}
```

**4. Add Search History:**
```java
private Map<UUID, List<CompassTarget>> searchHistory = new HashMap<>();

// After successful search:
List<CompassTarget> history = searchHistory.computeIfAbsent(
    player.getUniqueId(), k -> new ArrayList<>()
);
history.add(target);
if (history.size() > 10) history.remove(0); // Keep last 10
```

**5. Add Custom Messages:**
```java
// Load from config
String searchMessage = config.getString(
    "messages.searching", 
    "Searching for nearest {target}..."
);

// Use in code
player.sendMessage(searchMessage.replace("{target}", targetName));
```

---

## Build & Development

### Requirements

- **Java Development Kit**: JDK 17+
- **Build Tool**: Maven or Gradle
- **IDE**: IntelliJ IDEA recommended

### Maven Setup

```xml
<dependencies>
    <dependency>
        <groupId>io.papermc.paper</groupId>
        <artifactId>paper-api</artifactId>
        <version>1.20.4-R0.1-SNAPSHOT</version>
        <scope>provided</scope>
    </dependency>
</dependencies>
```

### Project Structure

```
EnhancedCompass/
├── src/
│   └── main/
│       ├── java/
│       │   └── com/supafloof/enhancedcompass/
│       │       └── EnhancedCompass.java
│       └── resources/
│           ├── plugin.yml
│           └── config.yml
├── pom.xml
└── README.md
```

### plugin.yml

```yaml
name: EnhancedCompass
version: 1.1.0
main: com.supafloof.enhancedcompass.EnhancedCompass
api-version: '1.19'
description: Point your compass to any structure or biome with real-time distance tracking

commands:
  enhancedcompass:
    description: Enhanced compass commands
    aliases: [ecompass, ec]
    usage: /enhancedcompass <structure|biome|village|anything|current|help|reload>

permissions:
  enhancedcompass.use:
    description: Use enhanced compass features
    default: op
  enhancedcompass.reload:
    description: Reload configuration
    default: op
```

---

## Testing Guidelines

### Manual Testing Checklist

**Structure Search:**
- [ ] Find structure in same dimension
- [ ] Try structure in wrong dimension
- [ ] Try disabled structure
- [ ] Try invalid structure name
- [ ] Test tab completion

**Biome Search:**
- [ ] Find biome in same dimension
- [ ] Try biome in wrong dimension
- [ ] Try disabled biome
- [ ] Try invalid biome name
- [ ] Test tab completion for second argument

**Boss Bar:**
- [ ] Bar appears when holding compass
- [ ] Bar disappears when compass put away
- [ ] Distance updates while moving
- [ ] Cross-dimension warning displays

**Persistence:**
- [ ] Target persists after logout
- [ ] Target loads on login
- [ ] Notification shows after join

**Config:**
- [ ] Reload command works
- [ ] Disabled structures can't be searched
- [ ] Disabled biomes can't be searched
- [ ] Blacklisted worlds block commands

---

## Performance Considerations

### Boss Bar Updates

**Current Implementation:** O(n) per tick
```java
for (Player player : Bukkit.getOnlinePlayers()) {
    // Check compass, update boss bar
}
```

**Optimization Options:**
1. Track compass holders separately (reduce iteration)
2. Increase tick interval (reduce frequency)
3. Skip players without targets (already implemented)

### Structure/Biome Searches

**Structure Search:**
- Uses world generator data
- Performance scales with radius
- May cause brief TPS drop for large radius

**Biome Search:**
- Uses `locateNearestBiome()` with step size
- Step size of 8 blocks is reasonable balance
- Larger radius = longer search time

### Memory Usage

**Per Player:**
- CompassTarget: ~100 bytes
- BossBar: ~500 bytes
- Map entries: ~100 bytes
- **Total: ~700 bytes per player**

**For 1000 players:** ~700 KB (negligible)

---

## Common Modifications

### 1. Add Player Join Handler

```java
@EventHandler
public void onPlayerJoin(PlayerJoinEvent event) {
    Player player = event.getPlayer();
    loadPlayerTarget(player);
}
```

### 2. Add Cost Per Search

```java
private double searchCost = 100.0;

// In onCommand(), before structure/biome search:
if (economy != null && economy.has(player, searchCost)) {
    economy.withdrawPlayer(player, searchCost);
} else {
    player.sendMessage("Insufficient funds! Cost: " + searchCost);
    return true;
}
```

### 3. Add Search Cooldowns

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

### 4. Add Statistics Tracking

```java
private Map<UUID, Integer> searchCount = new HashMap<>();

// After successful search:
searchCount.merge(player.getUniqueId(), 1, Integer::sum);

// Add command to view stats:
if (args[0].equalsIgnoreCase("stats")) {
    int count = searchCount.getOrDefault(player.getUniqueId(), 0);
    player.sendMessage("Total searches: " + count);
    return true;
}
```

### 5. Add Multi-Language Support

```java
private Map<String, String> messages = new HashMap<>();

// In loadConfig():
for (String key : config.getConfigurationSection("messages").getKeys(false)) {
    messages.put(key, config.getString("messages." + key));
}

// Usage:
player.sendMessage(messages.get("search.starting")
    .replace("{target}", targetName));
```

---

## API for Other Plugins

### Exposing Functionality

**Create Service Interface:**
```java
public interface EnhancedCompassAPI {
    void setPlayerTarget(Player player, TargetType type, String name, Location location);
    CompassTarget getPlayerTarget(Player player);
    void clearPlayerTarget(Player player);
}
```

**Implement in Main Class:**
```java
public class EnhancedCompass extends JavaPlugin implements EnhancedCompassAPI {
    @Override
    public void setPlayerTarget(Player player, TargetType type, String name, Location location) {
        CompassTarget target = new CompassTarget(type, name, location);
        playerTargets.put(player.getUniqueId(), target);
        player.setCompassTarget(location);
        savePlayerTarget(player, target);
    }
    
    @Override
    public CompassTarget getPlayerTarget(Player player) {
        return playerTargets.get(player.getUniqueId());
    }
    
    @Override
    public void clearPlayerTarget(Player player) {
        playerTargets.remove(player.getUniqueId());
        removeBossBar(player);
    }
}
```

**Register Service:**
```java
@Override
public void onEnable() {
    // ... existing code ...
    getServer().getServicesManager().register(
        EnhancedCompassAPI.class, 
        this, 
        this, 
        ServicePriority.Normal
    );
}
```

**Usage by Other Plugins:**
```java
EnhancedCompassAPI api = Bukkit.getServicesManager()
    .getRegistration(EnhancedCompassAPI.class)
    .getProvider();

api.setPlayerTarget(player, TargetType.BIOME, "DARK_FOREST", location);
```

---

## Debugging

### Enable Debug Logging

```java
private boolean debug = false;

// In loadConfig():
debug = config.getBoolean("debug", false);

// Usage:
if (debug) {
    getLogger().info("Player " + player.getName() + " searching for " + target);
}
```

### Common Issues

**Boss Bar Not Showing:**
```java
// Add debug logging:
if (debug) {
    getLogger().info("Holding compass: " + holdingCompass);
    getLogger().info("Has target: " + (target != null));
    getLogger().info("Has permission: " + player.hasPermission("enhancedcompass.use"));
}
```

**Structure/Biome Not Found:**
```java
// Log search parameters:
getLogger().info("Searching for: " + targetInput);
getLogger().info("Search radius: " + searchRadius);
getLogger().info("Target enabled: " + configManager.isStructureEnabled(env, targetInput));
```

**Config Not Loading:**
```java
// Validate config:
if (!config.contains("search-radius")) {
    getLogger().warning("Config missing search-radius, using default");
}
```

---

## Version Compatibility

### Minecraft Versions

**Tested On:**
- 1.19.x ✓
- 1.20.x ✓
- 1.21.x ✓

**Should Work:**
- Any version with Bukkit Registry API (1.19+)

**Won't Work:**
- 1.18.x and earlier (no Registry API)

### Server Software

**Supported:**
- Paper ✓ (recommended)
- Spigot ✓
- Purpur ✓

**Not Supported:**
- Bukkit (missing Adventure API)
- Forge (wrong API)
- Fabric (wrong API)

---

## Contributing Guidelines

### Code Style

1. **Formatting:**
   - Indentation: 4 spaces
   - Line length: 120 characters max
   - Braces: Egyptian style

2. **Naming:**
   - Classes: PascalCase
   - Methods: camelCase
   - Constants: UPPER_SNAKE_CASE
   - Variables: camelCase

3. **Documentation:**
   - JavaDoc for all public methods
   - Inline comments for complex logic
   - Explain "why", not "what"

4. **Structure:**
   - Keep single-file architecture
   - Use inner classes for modularity
   - Follow existing patterns

### Pull Request Process

1. Test on clean server
2. Update documentation
3. Add comments for new code
4. Follow existing code style
5. Create detailed PR description

---

## Troubleshooting Guide

### Plugin Won't Load

**Check:**
1. Server version (Paper/Spigot 1.19+)
2. Java version (17+)
3. plugin.yml format
4. Main class path correct
5. Console errors

### Commands Don't Work

**Check:**
1. Command registered in plugin.yml
2. Permission granted
3. World not blacklisted
4. Console errors during command

### Boss Bar Issues

**Check:**
1. Player holding compass
2. Target set
3. Permission granted
4. Update task running
5. Adventure API available

---

## Future Enhancements

### Potential Features

1. **GUI Interface**: Click-based structure/biome selection
2. **Waypoint System**: Multiple saved locations
3. **Shared Targets**: Party/guild shared compasses
4. **Dimension Linking**: Auto-convert coordinates between dimensions
5. **Custom Structures**: Support for custom/modded structures
6. **Custom Biomes**: Support for custom/modded biomes
7. **Statistics**: Track searches per player/target
8. **Economy Integration**: Costs per search
9. **Cooldowns**: Per-player or per-target cooldowns
10. **Distance Limits**: Maximum search distance per permission
11. **Search History**: Recent searches per player

---

## Resources

### Documentation
- [Bukkit API Documentation](https://hub.spigotmc.org/javadocs/bukkit/)
- [Paper API Documentation](https://jd.papermc.io/paper/1.20/)
- [Adventure API Documentation](https://docs.advntr.dev/)

### Tools
- [Paper Test Server](https://papermc.io/downloads)
- [IntelliJ IDEA](https://www.jetbrains.com/idea/)
- [Maven](https://maven.apache.org/)

### Community
- [SpigotMC Forums](https://www.spigotmc.org/)
- [Paper Discord](https://discord.gg/papermc)
- [Bukkit Forums](https://bukkit.org/)

---

## License & Credits

**Author:** SupaFloof Games, LLC  
**Version:** 1.1.0  
**License:** (Specify your license here)

---

**For additional information, refer to:**
- **End User Guide (enduser.md)** - Player documentation
- **Server Admin Guide (serveradmin.md)** - Configuration and management
- **README.md** - Overview and quick start
