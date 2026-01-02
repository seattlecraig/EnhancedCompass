# EnhancedCompass

> **Transform your Minecraft compass into a powerful structure and biome finding tool with real-time distance tracking!**

[![Server Type](https://img.shields.io/badge/Server-Paper%20%2F%20Spigot-orange)](https://papermc.io/)
[![Minecraft Version](https://img.shields.io/badge/Minecraft-1.19%2B-brightgreen)](https://www.minecraft.net/)
[![Java Version](https://img.shields.io/badge/Java-17%2B-blue)](https://adoptium.net/)

EnhancedCompass is a feature-rich Minecraft plugin that supercharges the vanilla compass, allowing players to point their compass to **any structure** or **any biome** in the game—villages, ancient cities, strongholds, End cities, dark forests, cherry groves, and more! When you hold your compass, a **boss bar** displays the target name and real-time distance as you travel.

---

## Features

🎯 **Point to Any Structure** - Find villages, temples, strongholds, ancient cities, bastions, End cities, and more  
🌲 **Point to Any Biome** - Find dark forests, cherry groves, mushroom fields, deep dark, and more  
📏 **Real-Time Distance Display** - Boss bar shows target name and distance, updating every 0.5 seconds  
🌍 **Multi-Dimension Support** - Works in Overworld, Nether, and End  
🔍 **Smart Generic Searches** - Use `village` to find any village type, or `anything` to find the nearest structure  
💾 **Persistent Targets** - Your compass remembers your target even after logout or server restart  
⚙️ **Highly Configurable** - Whitelist structures and biomes per dimension, set search radius, blacklist worlds  
🔐 **Permission-Based** - Fine-grained permission system for access control  
⌨️ **Tab Completion** - Full auto-completion for all commands, structures, and biomes  
🔄 **Hot Reload** - Configuration changes without server restart  
⚡ **Async Biome Searches** - Biome searches run off the main thread to prevent lag

---

## Quick Start

### For Players

1. **Get a compass** (any compass works, even enchanted ones)
2. **Run a command** to find a structure or biome:
   ```
   /enhancedcompass ancient_city
   /enhancedcompass biome dark_forest
   ```
3. **Hold your compass** and watch the boss bar appear
4. **Follow the compass** and watch your distance count down!

### Popular Commands

```bash
/enhancedcompass village         # Find nearest village of any type
/enhancedcompass anything        # Find nearest structure of any type
/enhancedcompass ancient_city    # Find nearest ancient city
/enhancedcompass stronghold      # Find nearest stronghold
/enhancedcompass fortress        # Find nearest nether fortress
/enhancedcompass end_city        # Find nearest End city
/enhancedcompass biome dark_forest    # Find nearest dark forest
/enhancedcompass biome cherry_grove   # Find nearest cherry grove
/enhancedcompass biome mushroom_fields # Find nearest mushroom fields
/enhancedcompass current         # Check your current target
```

Use **tab completion** to see all available structures and biomes!

---

## Installation

### Requirements

- **Server**: Paper or Spigot 1.19+
- **Java**: Java 17 or higher
- **Dependencies**: None (Adventure API bundled with Paper)

### Steps

1. **Download** the latest `EnhancedCompass.jar`
2. **Place** the JAR in your server's `plugins/` folder
3. **Start or restart** your server
4. **Configure** (optional) - Edit `plugins/EnhancedCompass/config.yml`
5. **Grant permissions** to your players

### Permissions

| Permission | Description | Default |
|------------|-------------|---------|
| `enhancedcompass.use` | Use compass features | op |
| `enhancedcompass.reload` | Reload configuration | op |

**Grant to all players (LuckPerms):**
```
/lp group default permission set enhancedcompass.use true
```

---

## How It Works

### The Boss Bar

When you hold a compass with a target set:

**Same Dimension:**
```
Ancient City - 1,432 blocks
Dark Forest - 523 blocks
```
- Aqua text = Target is in your dimension
- Yellow distance = Real-time block count

**Different Dimension:**
```
End City - Not in same dimension
```
- Red text = Target is in another dimension

The boss bar appears when you hold the compass, updates every 0.5 seconds, and disappears when you put it away.

### Search Types

| Search | Description |
|--------|-------------|
| `/enhancedcompass <structure>` | Find specific structure |
| `/enhancedcompass biome <biome>` | Find specific biome |
| `/enhancedcompass village` | Find any village type |
| `/enhancedcompass anything` | Find any enabled structure |

---

## Configuration

Edit `plugins/EnhancedCompass/config.yml`:

```yaml
# Search radius in chunks (100 = 1,600 blocks)
search-radius: 100

# Worlds where plugin is disabled
blacklisted-worlds:
  - lobby
  - minigames

# Structure whitelist per dimension
enabled-structures:
  normal:     # Overworld
    ancient_city: true
    stronghold: true
    village_plains: true
  nether:     # Nether
    fortress: true
    bastion_remnant: true
  the_end:    # End
    end_city: true

# Biome whitelist per dimension
enabled-biomes:
  normal:
    dark_forest: true
    cherry_grove: true
    mushroom_fields: true
  nether:
    crimson_forest: true
    warped_forest: true
  the_end:
    end_highlands: true
```

Reload without restart:
```
/enhancedcompass reload
```

---

## Documentation

| Guide | Description |
|-------|-------------|
| **[End User Guide](enduser.md)** | Complete player documentation with examples and tips |
| **[Server Admin Guide](serveradmin.md)** | Installation, configuration, permissions, troubleshooting |
| **[Developer Guide](devguide.md)** | Code architecture, API, extension points, technical details |

---

## Command Reference

```
/enhancedcompass help              # Show help menu
/enhancedcompass <structure>       # Find specific structure
/enhancedcompass biome <biome>     # Find specific biome
/enhancedcompass village           # Find any village
/enhancedcompass anything          # Find any structure
/enhancedcompass current           # Show current target
/enhancedcompass reload            # Reload config (admin)
```

---

## Why EnhancedCompass?

### For Players

- **No More Getting Lost** - Always know exactly how far away your target is
- **Save Time** - Find structures and biomes quickly
- **Explore Efficiently** - Use "anything" mode for random exploration
- **Find Rare Biomes** - Easily locate cherry groves, mushroom fields, and more
- **Easy to Use** - Simple commands with tab completion

### For Server Owners

- **Balanced Gameplay** - Disable structures or biomes to maintain challenge
- **Flexible Configuration** - Control everything via config file
- **Performance Friendly** - Async biome searches, efficient code
- **Zero Dependencies** - No external plugins required
- **Well Documented** - Comprehensive guides for users, admins, and developers

### For Developers

- **Clean Code** - Single-file architecture with extensive comments
- **Well Documented** - JavaDoc on every method
- **Extension Friendly** - Easy to add features or modify behavior
- **Modern APIs** - Adventure API and Bukkit Registry system

---

## Technical Details

### Architecture

- **Single File Design** - All code in `EnhancedCompass.java`
- **Inner Classes** - ConfigManager, CompassTarget for organization
- **Adventure API** - Modern text components and boss bars
- **Bukkit Registry** - Accurate structure and biome identification
- **YAML Storage** - Config and player data persistence
- **Scheduled Tasks** - Boss bar updates every 0.5 seconds
- **Async Operations** - Biome searches run off main thread

### Compatibility

- **Server**: Paper, Spigot, Purpur (1.19+)
- **Java**: Java 17 or higher
- **Minecraft**: 1.19.x, 1.20.x, 1.21.x

---

## File Structure

```
plugins/EnhancedCompass/
├── config.yml                    # Configuration
└── playerdata/                   # Player targets
    └── *.yml                     # Per-player data
```

---

## Examples

### Finding a Village
```
> /enhancedcompass village
Searching for nearest village of any type...
Found Village Plains!
Compass now pointing to Village Plains!
Distance: 892 blocks
```

### Finding an Ancient City
```
> /enhancedcompass ancient_city
Searching for nearest Ancient City...
Compass now pointing to Ancient City!
Distance: 2,134 blocks
```
*Hold compass, follow it, boss bar updates as you travel*

### Finding a Biome
```
> /enhancedcompass biome cherry_grove
Searching for nearest Cherry Grove biome...
Compass now pointing to Cherry Grove biome!
Distance: 1,245 blocks
```

---

## Support

- **Documentation**: See guides linked above
- **Issues**: Create an issue on GitHub

---

## Credits

**Author:** SupaFloof Games, LLC  
**Version:** 1.1.0  
**Built With:** [Paper API](https://papermc.io/) • [Adventure API](https://docs.advntr.dev/)

---

📖 **[End User Guide](enduser.md)** • ⚙️ **[Server Admin Guide](serveradmin.md)** • 💻 **[Developer Guide](devguide.md)**

---

*Made with ❤️ by SupaFloof Games, LLC*
