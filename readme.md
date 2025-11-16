# EnhancedCompass

> **Transform your Minecraft compass into a powerful structure-finding tool with real-time distance tracking!**

[![Server Type](https://img.shields.io/badge/Server-Paper%20%2F%20Spigot-orange)](https://papermc.io/)
[![Minecraft Version](https://img.shields.io/badge/Minecraft-1.19%2B-brightgreen)](https://www.minecraft.net/)
[![Java Version](https://img.shields.io/badge/Java-17%2B-blue)](https://adoptium.net/)
[![License](https://img.shields.io/badge/License-Custom-red)]()

EnhancedCompass is a feature-rich Minecraft plugin that supercharges the vanilla compass, allowing players to point their compass to **any structure** in the game—villages, ancient cities, strongholds, End cities, and more! Best of all, when you hold your compass, a **boss bar** displays the structure name and real-time distance as you travel.

---

## ✨ Features at a Glance

🎯 **Point to Any Structure** - Find villages, temples, strongholds, ancient cities, bastions, End cities, and every other structure in Minecraft  
📏 **Real-Time Distance Display** - Boss bar shows structure name and distance, updating every 0.5 seconds  
🌍 **Multi-Dimension Support** - Works seamlessly in Overworld, Nether, and End  
🔍 **Smart Generic Searches** - Use `village` to find any village type, or `anything` to find the nearest structure of any kind  
💾 **Persistent Targets** - Your compass remembers your target even after logout or server restart  
⚙️ **Highly Configurable** - Whitelist structures per dimension, set search radius, blacklist worlds  
🔐 **Permission-Based** - Fine-grained permission system for access control  
🎨 **Beautiful UI** - Colored boss bars with Adventure API text components  
⌨️ **Tab Completion** - Full auto-completion for all commands and structure names  
🔄 **Hot Reload** - Configuration changes without server restart  
📖 **Comprehensive Documentation** - Detailed guides for players, admins, and developers

---

## 🎮 Quick Start for Players

1. **Get a compass** (any compass works, even enchanted ones!)
2. **Run a command** to find a structure:
   ```
   /enhancedcompass ancient_city
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
/enhancedcompass current         # Check your current target
```

Use **tab completion** to see all available structures!

---

## 📦 Installation for Server Admins

### Requirements

- **Server**: Paper or Spigot 1.19+
- **Java**: Java 17 or higher
- **Dependencies**: None (Adventure API bundled with Paper)

### Installation Steps

1. **Download** the latest `EnhancedCompass.jar`
2. **Place** the JAR in your server's `plugins/` folder
3. **Start or restart** your server
4. **Configure** (optional) - Edit `plugins/EnhancedCompass/config.yml`
5. **Grant permissions** to your players (see below)

### Default Configuration

The plugin generates a `config.yml` with sensible defaults:

```yaml
# Search radius in chunks (100 = 1,600 blocks)
search-radius: 100

# Worlds where plugin is disabled
blacklisted-worlds: []

# Structure whitelist per dimension
enabled-structures:
  normal:    # Overworld
    ancient_city: true
    stronghold: true
    village_plains: true
    # ... and more
  nether:    # Nether
    fortress: true
    bastion_remnant: true
  the_end:   # End
    end_city: true
```

### Permissions

```yaml
enhancedcompass.use       # Use compass features (default: op)
enhancedcompass.reload    # Reload configuration (default: op)
```

**Grant to all players:**
```bash
/lp group default permission set enhancedcompass.use true
```

### Reload Configuration

Make changes to `config.yml` and reload without restarting:
```
/enhancedcompass reload
```

---

## 🎯 How It Works

### The Boss Bar System

When you hold a compass with a target set, a boss bar appears at the top of your screen:

**Same Dimension:**
```
Ancient City - 1,432 blocks
```
- Aqua text = Target is in your dimension
- Yellow distance = Real-time block count

**Different Dimension:**
```
End City - Not in same dimension
```
- Red text = Warning that target is in another dimension
- Red message = You need to change dimensions first

The boss bar:
- ✅ Appears instantly when you hold the compass
- ✅ Updates every 0.5 seconds while held
- ✅ Disappears when you put the compass away
- ✅ Works in both main hand and off hand

### Structure Searches

The plugin uses Minecraft's **built-in structure location system** to find structures:

1. You run a search command
2. Plugin queries the world generator within configured radius
3. Nearest matching structure is found
4. Vanilla compass is set to point to the structure
5. Target is saved for persistence
6. Boss bar shows real-time distance

**Search Types:**

- **Specific**: `/enhancedcompass ancient_city` - Find exact structure type
- **Generic Village**: `/enhancedcompass village` - Find any village (plains, desert, snowy, etc.)
- **Generic Any**: `/enhancedcompass anything` - Find any enabled structure in current dimension

---

## 🛠️ Configuration Guide

### Search Radius

Control how far structure searches extend:

```yaml
search-radius: 100  # In chunks (100 chunks = 1,600 blocks)
```

- **Lower values (25-50)**: Faster searches, but may not find structures
- **Medium values (50-100)**: Balanced performance and success rate
- **Higher values (100-200)**: Find distant structures but slower searches

### World Blacklist

Disable the plugin in specific worlds:

```yaml
blacklisted-worlds:
  - lobby
  - minigames
  - creative
```

Perfect for:
- Lobby worlds where compasses shouldn't work
- Minigame arenas with custom mechanics
- Creative worlds where finding structures would be unfair

### Structure Whitelist

Control which structures can be found per dimension:

**Easy Mode (Everything Enabled):**
```yaml
normal:
  ancient_city: true
  stronghold: true
  mansion: true
  # ... everything true
```

**Balanced Mode (Selective):**
```yaml
normal:
  stronghold: false      # Rare structures disabled
  ancient_city: false
  village_plains: true   # Common structures enabled
  temple_desert: true
```

**Hard Mode (Minimal):**
```yaml
normal:
  village_plains: true   # Only villages allowed
  village_desert: true
  # Everything else false
```

---

## 📚 Documentation

### Complete Guides

- **[End User Guide](enduser.md)** - Complete player documentation with examples, tips, and troubleshooting
- **[Server Admin Guide](serveradmin.md)** - Installation, configuration, permissions, and management
- **[Developer Guide](devguide.md)** - Code architecture, API, extension points, and technical details

### Quick Reference

**Commands:**
```
/enhancedcompass help              # Show help menu
/enhancedcompass <structure>       # Find specific structure
/enhancedcompass village           # Find any village
/enhancedcompass anything          # Find any structure
/enhancedcompass current           # Show current target
/enhancedcompass reload            # Reload config (admin)
```

**Permissions:**
```
enhancedcompass.use                # Use compass features
enhancedcompass.reload             # Reload configuration
```

**Config Files:**
```
plugins/EnhancedCompass/config.yml              # Main configuration
plugins/EnhancedCompass/playerdata/*.yml        # Player targets
```

---

## 🎨 Screenshots

### In-Game Boss Bar
```
┌────────────────────────────────────────┐
│  Ancient City - 1,432 blocks           │
└────────────────────────────────────────┘
```

### Command Output
```
> /enhancedcompass ancient_city
Searching for nearest Ancient City...
Compass now pointing to Ancient City!
Distance: 1,432 blocks
```

### Help Menu
```
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
Enhanced Compass Commands
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
/enhancedcompass help - Show this help menu
/enhancedcompass <structure> - Point compass to nearest structure
/enhancedcompass current - Show current compass target
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
```

---

## 🌟 Why EnhancedCompass?

### For Players

- **No More Getting Lost** - Always know exactly how far away your target is
- **Save Time** - Find structures quickly instead of wandering aimlessly
- **Explore Efficiently** - Use "anything" mode for random exploration
- **Persistent Targets** - Your compass remembers where you were going
- **Easy to Use** - Simple commands with tab completion

### For Server Owners

- **Balanced Gameplay** - Disable rare structures to maintain challenge
- **Flexible Configuration** - Control everything via simple config file
- **Performance Friendly** - Minimal server impact, efficient code
- **Zero Dependencies** - No external plugins required
- **Professional Quality** - Extensive error handling and validation
- **Well Documented** - Comprehensive guides for users, admins, and developers

### For Developers

- **Clean Code** - Single-file architecture with extensive comments
- **Well Documented** - JavaDoc on every method, detailed inline comments
- **Modular Design** - Inner classes for clear separation of concerns
- **Extension Friendly** - Easy to add features or modify behavior
- **Modern APIs** - Uses Adventure API and Bukkit Registry system
- **Best Practices** - Proper error handling, data persistence, permission checks

---

## 🔧 Technical Details

### Architecture

- **Single File Design** - All code in one Java file for simplicity
- **Inner Classes** - Modular components (ConfigManager, CompassTarget)
- **Adventure API** - Modern text components and boss bars
- **Bukkit Registry** - Accurate structure identification
- **YAML Storage** - Config and player data persistence
- **Scheduled Tasks** - Async boss bar updates every 0.5 seconds

### Performance

- **Boss Bar Updates**: O(n) where n = players holding compass
- **Structure Searches**: World generator queries (async-safe)
- **Memory Usage**: ~700 bytes per player (negligible)
- **Disk Usage**: ~200 bytes per player (minimal)

### Compatibility

- **Server**: Paper, Spigot, Purpur (1.19+)
- **Java**: Java 17 or higher
- **Minecraft**: 1.19.x, 1.20.x, 1.21.x (any version with Registry API)
- **Dependencies**: None (Adventure API bundled with Paper)

---

## 🤝 Contributing

Contributions are welcome! Whether it's bug reports, feature requests, or code contributions:

1. **Fork** the repository
2. **Create** a feature branch
3. **Make** your changes with clear commits
4. **Test** on a clean server
5. **Submit** a pull request

Please follow the existing code style and add comments for new code.

---

## 📝 Support & Contact

- **Documentation**: See guides above
- **Issues**: Create an issue on GitHub
- **Questions**: Check FAQ in End User Guide
- **Community**: Ask in server forums/Discord

---

## 📋 Examples

### Example 1: Finding a Village to Trade
```bash
> /enhancedcompass village
Searching for nearest village of any type...
Found Village Plains!
Compass now pointing to Village Plains!
Distance: 892 blocks
```

### Example 2: Finding an Ancient City
```bash
> /enhancedcompass ancient_city
Searching for nearest Ancient City...
Compass now pointing to Ancient City!
Distance: 2,134 blocks
```
*Boss bar appears: "Ancient City - 2,134 blocks"*  
*Walk toward the compass direction, boss bar updates in real-time*  
*Boss bar shows: "Ancient City - 1,876 blocks"*  
*Keep walking...*  
*Boss bar shows: "Ancient City - 234 blocks"*  
*You're close! Start digging down (ancient cities are underground)*

### Example 3: Exploration Mode
```bash
> /enhancedcompass anything
Searching for nearest structure of any type...
Found Mineshaft!
Compass now pointing to Mineshaft!
Distance: 456 blocks
```

### Example 4: Checking Your Target
```bash
> /enhancedcompass current
Current target: Stronghold
Distance: 3,421 blocks
```

---

## 🎯 Use Cases

### Survival Servers
- Enable common structures (villages, temples)
- Disable rare structures (strongholds, ancient cities) to maintain challenge
- Set reasonable search radius (50-100 chunks)

### Creative Servers
- Enable all structures for easy access
- Large search radius (150-200 chunks)
- Great for builders finding specific biomes/structures

### Adventure Maps
- Disable plugin in map worlds via blacklist
- Enable only in "hub" worlds
- Useful for players finding resources

### SMP Servers
- Balanced configuration for fair gameplay
- Enable villages and common structures
- Disable or limit stronghold searches to maintain End portal challenge

---

## 🔮 Roadmap

### Planned Features

- [ ] GUI interface for structure selection
- [ ] Multiple waypoint system
- [ ] Party/guild shared targets
- [ ] Statistics and leaderboards
- [ ] Economy integration (costs per search)
- [ ] Cooldown system (configurable per structure)
- [ ] Custom structure support
- [ ] Distance limits per permission
- [ ] Search history per player

### Suggestions Welcome!

Have an idea? Create a GitHub issue with the "enhancement" label!

---

## ⚖️ License

*Specify your license here*

---

## 🏆 Credits

**Author:** SupaFloof Games, LLC  
**Version:** 1.0.0  
**Built With:**
- [Paper API](https://papermc.io/)
- [Adventure API](https://docs.advntr.dev/)
- [Bukkit](https://bukkit.org/)

---

## 🎉 Thank You!

Thank you for using EnhancedCompass! We hope it enhances your Minecraft experience. If you enjoy the plugin, please consider:

- ⭐ Starring the repository
- 📢 Sharing with other server owners
- 💬 Leaving feedback
- 🐛 Reporting bugs

Happy exploring! 🧭

---

## Quick Links

📖 **[End User Guide](enduser.md)** - Player documentation  
⚙️ **[Server Admin Guide](serveradmin.md)** - Configuration and management  
💻 **[Developer Guide](devguide.md)** - Technical documentation and API  

---

*Made with ❤️ by SupaFloof Games, LLC*
