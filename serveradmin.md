# EnhancedCompass - Server Admin Guide

## Overview

EnhancedCompass is a Paper/Spigot plugin that enhances the vanilla compass by allowing players to point their compass to any structure type or biome in the game, with real-time distance tracking via boss bar display.

---

## Features

- **Permission-Based Access Control** - Fine-grained permission system
- **Multi-Dimension Support** - Works in Overworld, Nether, and End
- **Configurable Structure Whitelist** - Control which structures can be found per dimension
- **Configurable Biome Whitelist** - Control which biomes can be found per dimension
- **Configurable Search Radius** - Limit how far searches extend (in chunks)
- **World Blacklist** - Disable plugin in specific worlds (lobbies, minigames, etc.)
- **Real-Time Distance Display** - Boss bar updates every 0.5 seconds
- **Player Data Persistence** - Targets saved when set and on player logout
- **Hot Reload** - Configuration changes without server restart
- **Generic Searches** - "village" and "anything" searches for convenience
- **Tab Completion** - Full auto-completion for all commands
- **Asynchronous Biome Searches** - Biome searches run off the main thread to prevent lag

---

## Requirements

### Server Requirements
- **Paper** or **Spigot** server (Paper recommended)
- **Minecraft Version**: 1.19+ (requires Bukkit Registry API)
- **Java**: Java 17+

### Dependencies
- No external dependencies required
- Uses Adventure API (bundled with Paper 1.19+)
- Uses Bukkit Registry API (built-in)

---

## Installation

1. **Download** the EnhancedCompass.jar file
2. **Place** the JAR in your server's `plugins/` folder
3. **Start/Restart** your server
4. The plugin will:
   - Create `plugins/EnhancedCompass/` folder
   - Generate default `config.yml`
   - Create `playerdata/` subfolder for storing player targets

---

## Configuration

### File Location
```
plugins/EnhancedCompass/config.yml
```

### Configuration File Structure

```yaml
# Search radius in chunks (1 chunk = 16 blocks)
# Default: 100 chunks = 1,600 blocks
# Higher values find structures/biomes farther away but searches take longer
search-radius: 100

# Worlds where the plugin is completely disabled
# Useful for lobby worlds, minigames, etc.
blacklisted-worlds:
  - lobby
  - minigames

# Structure whitelist per dimension
# Set to true to enable, false to disable
enabled-structures:
  
  # Overworld structures
  normal:
    ancient_city: true
    buried_treasure: true
    desert_pyramid: true
    igloo: true
    jungle_pyramid: true
    mansion: true
    mineshaft: true
    mineshaft_mesa: true
    monument: true
    ocean_ruin_cold: true
    ocean_ruin_warm: true
    pillager_outpost: true
    ruined_portal: true
    ruined_portal_desert: true
    ruined_portal_jungle: true
    ruined_portal_mountain: true
    ruined_portal_ocean: true
    ruined_portal_swamp: true
    shipwreck: true
    shipwreck_beached: true
    stronghold: true
    swamp_hut: true
    trail_ruins: true
    trial_chambers: true
    village_desert: true
    village_plains: true
    village_savanna: true
    village_snowy: true
    village_taiga: true
  
  # Nether structures
  nether:
    bastion_remnant: true
    fortress: true
    nether_fossil: true
    ruined_portal_nether: true
  
  # End structures
  the_end:
    end_city: true

# Biome whitelist per dimension
# Set to true to enable, false to disable
enabled-biomes:
  
  # Overworld biomes
  normal:
    badlands: true
    bamboo_jungle: true
    beach: true
    birch_forest: true
    cherry_grove: true
    cold_ocean: true
    dark_forest: true
    deep_cold_ocean: true
    deep_dark: true
    deep_frozen_ocean: true
    deep_lukewarm_ocean: true
    deep_ocean: true
    desert: true
    dripstone_caves: true
    eroded_badlands: true
    flower_forest: true
    forest: true
    frozen_ocean: true
    frozen_peaks: true
    frozen_river: true
    grove: true
    ice_spikes: true
    jagged_peaks: true
    jungle: true
    lukewarm_ocean: true
    lush_caves: true
    mangrove_swamp: true
    meadow: true
    mushroom_fields: true
    ocean: true
    old_growth_birch_forest: true
    old_growth_pine_taiga: true
    old_growth_spruce_taiga: true
    pale_garden: true
    plains: true
    river: true
    savanna: true
    savanna_plateau: true
    snowy_beach: true
    snowy_plains: true
    snowy_slopes: true
    snowy_taiga: true
    sparse_jungle: true
    stony_peaks: true
    stony_shore: true
    sunflower_plains: true
    swamp: true
    taiga: true
    warm_ocean: true
    windswept_forest: true
    windswept_gravelly_hills: true
    windswept_hills: true
    windswept_savanna: true
    wooded_badlands: true
  
  # Nether biomes
  nether:
    basalt_deltas: true
    crimson_forest: true
    nether_wastes: true
    soul_sand_valley: true
    warped_forest: true
  
  # End biomes
  the_end:
    end_barrens: true
    end_highlands: true
    end_midlands: true
    small_end_islands: true
    the_end: true
```

---

## Configuration Options

### search-radius

**Type:** Integer  
**Default:** 100  
**Unit:** Chunks (multiply by 16 for blocks)

Controls how far structure and biome searches extend from the player's location.

| Value Range | Performance | Success Rate |
|-------------|-------------|--------------|
| 25-50 | Fast searches | May not find distant targets |
| 50-100 | Balanced | Good success rate |
| 100-200 | Slower searches | Finds distant targets |

**Note:** Structure searches use Bukkit's `World.locateNearestStructure()` which is optimized. Biome searches run asynchronously with a step size of 32 blocks to prevent lag.

---

### blacklisted-worlds

**Type:** List of Strings  
**Default:** Empty list

Worlds where the plugin is completely disabled. Players in these worlds cannot use any compass features.

**Use Cases:**
- Lobby worlds where compass shouldn't work
- Minigame arenas with custom mechanics
- Creative worlds where structure finding would be unfair

**Example:**
```yaml
blacklisted-worlds:
  - lobby
  - minigames
  - creative
```

**Note:** World names are case-sensitive and must match exactly.

---

### enabled-structures

**Type:** Nested Map  
**Structure:** `dimension` → `structure_name` → `boolean`

Controls which structures can be found in each dimension.

**Dimensions:**
- `normal` - Overworld structures
- `nether` - Nether structures
- `the_end` - End structures

**Balancing Examples:**

**All Enabled (Easy):**
```yaml
normal:
  stronghold: true
  ancient_city: true
  mansion: true
```

**Selective (Balanced):**
```yaml
normal:
  stronghold: false
  ancient_city: false
  village_plains: true
  desert_pyramid: true
```

**Minimal (Hard):**
```yaml
normal:
  village_plains: true
  village_desert: true
  # Everything else false
```

---

### enabled-biomes

**Type:** Nested Map  
**Structure:** `dimension` → `biome_name` → `boolean`

Controls which biomes can be found in each dimension.

**Dimensions:**
- `normal` - Overworld biomes
- `nether` - Nether biomes
- `the_end` - End biomes

---

## Permissions

| Permission | Description | Default |
|------------|-------------|---------|
| `enhancedcompass.use` | Use all compass features | op |
| `enhancedcompass.reload` | Reload configuration | op |

### Setting Up Permissions

**LuckPerms (grant to all players):**
```
/lp group default permission set enhancedcompass.use true
```

**PermissionsEx:**
```yaml
groups:
  default:
    permissions:
      - enhancedcompass.use
```

---

## Commands

### Player Commands

| Command | Description |
|---------|-------------|
| `/enhancedcompass help` | Show help menu |
| `/enhancedcompass <structure>` | Find specific structure |
| `/enhancedcompass biome <biome>` | Find specific biome |
| `/enhancedcompass village` | Find any village type |
| `/enhancedcompass anything` | Find any enabled structure |
| `/enhancedcompass current` | Show current target and distance |

### Admin Commands

| Command | Description |
|---------|-------------|
| `/enhancedcompass reload` | Reload configuration |

**Console Usage:**
```
enhancedcompass reload
```

---

## File Structure

```
plugins/EnhancedCompass/
├── config.yml           # Main configuration
└── playerdata/          # Player target data
    ├── uuid1.yml        # Per-player target files
    ├── uuid2.yml
    └── ...
```

### Player Data File Format

```yaml
structure-type: ANCIENT_CITY
world: world
x: 123.456
y: -45.0
z: 789.012
```

---

## Performance

### Boss Bar Updates
- Runs every 10 ticks (0.5 seconds)
- Only processes players actively holding a compass
- O(n) where n = players holding compass

### Structure Searches
- Uses Bukkit's `World.locateNearestStructure()` API
- Runs synchronously (API is already optimized)

### Biome Searches
- Uses Bukkit's `World.locateNearestBiome()` API
- Runs **asynchronously** to prevent server lag
- Step size of 32 blocks (matches vanilla `/locate biome` resolution)

### Memory Usage

| Item | Per Player |
|------|------------|
| CompassTarget object | ~100 bytes |
| BossBar object | ~500 bytes |
| YAML file on disk | ~200 bytes |

**Total for 1000 players:** ~800 KB (negligible)

---

## Troubleshooting

### Players Can't Use Commands

**Symptom:** "You don't have permission to use enhanced compass features"

**Solutions:**
1. Grant `enhancedcompass.use` permission
2. Verify permission plugin is working
3. Check player is not in blacklisted world

### Structures/Biomes Not Found

**Symptom:** "No [target] found within X blocks"

**Possible Causes:**
1. Target disabled in config
2. Search radius too small
3. Target doesn't exist in current dimension
4. Target doesn't generate in this world type

**Solutions:**
1. Enable target in config
2. Increase search-radius
3. Try searching in correct dimension
4. Use tab completion to see available targets

### Boss Bar Not Showing

**Symptom:** Commands work but no boss bar appears

**Causes:**
1. Player not holding compass
2. Player doesn't have target set
3. Player lacks permission

**Solutions:**
1. Hold compass in main or off hand
2. Run a search command first
3. Grant `enhancedcompass.use` permission

### Config Not Reloading

**Solutions:**
1. Run `/enhancedcompass reload`
2. Verify config.yml is valid YAML syntax
3. Check console for errors
4. Restart server if reload fails

### Plugin Not Loading

**Causes:**
1. Wrong server type (needs Paper/Spigot 1.19+)
2. Wrong Java version
3. Corrupted JAR file

**Solutions:**
1. Verify server is Paper or Spigot 1.19+
2. Check Java 17+ is installed
3. Re-download plugin JAR
4. Check console for startup errors

---

## Best Practices

### Configuration

1. **Start Conservative**: Begin with moderate search-radius (50-100)
2. **Test First**: Try config changes on test server
3. **Balance Gameplay**: Disable structures/biomes that would trivialize progression
4. **Document Changes**: Note why certain targets are disabled

### Permission Management

1. **Default Allow**: Grant `enhancedcompass.use` to all players by default
2. **Restrict Admin**: Keep `enhancedcompass.reload` for staff only
3. **Use World Blacklist**: Prefer world blacklist over per-world permissions

### Maintenance

1. **Regular Backups**: Back up playerdata folder
2. **Clean Old Data**: Remove data for inactive players periodically
3. **Monitor Performance**: Watch for issues with large search radius values

---

## Integration with Other Plugins

### Compatible Plugins

- **Essentials** - No conflicts
- **WorldGuard** - No conflicts
- **Multiverse** - Works with multiple worlds
- **LuckPerms** - Full permission support
- **Dynmap** - No conflicts

### Potential Conflicts

- **Custom Compass Plugins** - May conflict with plugins that modify compass behavior
- **Custom Boss Bar Plugins** - May cause visual overlap

---

## Updating the Plugin

### Update Process

1. **Backup** current config and playerdata
2. **Download** new version
3. **Replace** JAR file
4. **Restart** server
5. **Test** functionality
6. **Verify** config compatibility (check console for warnings)

### Version Compatibility

- Config format is forward-compatible
- New structures/biomes may be added in updates
- Old playerdata files remain valid

---

## Quick Reference

### Essential Commands
```
/enhancedcompass reload    # Reload config (admin)
```

### Important Permissions
```
enhancedcompass.use        # Player usage
enhancedcompass.reload     # Admin reload
```

### Critical Files
```
plugins/EnhancedCompass/config.yml              # Configuration
plugins/EnhancedCompass/playerdata/*.yml        # Player data
```

### Configuration Sections
```yaml
search-radius: 100                              # Search distance in chunks
blacklisted-worlds: []                          # Disabled worlds
enabled-structures.normal: {}                   # Overworld structures
enabled-structures.nether: {}                   # Nether structures
enabled-structures.the_end: {}                  # End structures
enabled-biomes.normal: {}                       # Overworld biomes
enabled-biomes.nether: {}                       # Nether biomes
enabled-biomes.the_end: {}                      # End biomes
```

---

**For additional support, refer to the Developer Guide (devguide.md) for technical details, or the End User Guide (enduser.md) to help your players.**
