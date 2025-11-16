# EnhancedCompass - Server Admin Guide

## Overview

EnhancedCompass is a Paper/Spigot plugin that enhances the vanilla compass by allowing players to point their compass to any structure type in the game, with real-time distance tracking via boss bar display.

---

## Features

- ✅ **Permission-Based Access Control** - Fine-grained permission system
- ✅ **Multi-Dimension Support** - Works in Overworld, Nether, and End
- ✅ **Configurable Structure Whitelist** - Control which structures can be found per dimension
- ✅ **Configurable Search Radius** - Limit how far structure searches extend
- ✅ **World Blacklist** - Disable plugin in specific worlds (lobbies, minigames, etc.)
- ✅ **Real-Time Distance Display** - Boss bar updates every 0.5 seconds
- ✅ **Player Data Persistence** - Targets saved across sessions
- ✅ **Hot Reload** - Configuration changes without restart
- ✅ **Generic Searches** - "village" and "anything" searches for convenience
- ✅ **Tab Completion** - Full auto-completion for all commands

---

## Requirements

### Server Requirements
- **Paper** or **Spigot** server (Paper recommended)
- **Minecraft Version**: 1.19+ (any version with modern structure system)
- **Java**: Java 17+ (matches your server version requirement)

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

### Default Config Location
```
plugins/EnhancedCompass/config.yml
```

### Configuration File Structure

```yaml
# Search radius in chunks (1 chunk = 16 blocks)
# Default: 100 chunks = 1,600 blocks
# Higher values = find structures farther away but slower searches
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
    end_city: false  # Not in overworld
    fortress: false  # Not in overworld
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
    ruined_portal_nether: false  # Not in overworld
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
```

---

## Configuration Options Explained

### search-radius

**Type:** Integer  
**Default:** 100  
**Unit:** Chunks (multiply by 16 for blocks)

Controls how far structure searches will extend from the player's location.

- **Lower values** (25-50): Faster searches, but may not find structures
- **Medium values** (50-100): Balanced performance and success rate
- **Higher values** (100-200): Finds distant structures but slower searches

**Performance Impact:** Higher values can cause lag when structures are searched. Consider server performance when setting this value.

**Example:**
```yaml
search-radius: 100  # Searches up to 1,600 blocks away
```

---

### blacklisted-worlds

**Type:** List of Strings  
**Default:** Empty list

Worlds where the plugin is completely disabled. Players in these worlds cannot use any compass features.

**Use Cases:**
- Lobby worlds where compass shouldn't work
- Minigame arenas with custom mechanics
- Creative worlds where structure finding would be cheating
- Plot worlds where navigation isn't needed

**Example:**
```yaml
blacklisted-worlds:
  - lobby
  - minigames
  - creative
  - plotworld
```

**Note:** World names are case-sensitive and must match exactly.

---

### enabled-structures

**Type:** Nested Map  
**Structure:** `dimension` → `structure_name` → `boolean`

Controls which structures can be found in each dimension. This provides granular control over gameplay balance.

**Dimensions:**
- `normal` - Overworld structures
- `nether` - Nether structures
- `the_end` - End structures

**Balancing Considerations:**

**Easy Mode (All Enabled):**
```yaml
normal:
  stronghold: true
  ancient_city: true
  mansion: true
  # ... everything true
```
Players can find anything easily. Good for casual/creative servers.

**Balanced Mode (Selective):**
```yaml
normal:
  stronghold: false      # Make finding rare
  ancient_city: false    # Make finding rare
  village_plains: true   # Common structures OK
  temple_desert: true
```
Rare structures require exploration. Good for survival servers.

**Hard Mode (Minimal):**
```yaml
normal:
  village_plains: true   # Only villages allowed
  village_desert: true
  village_snowy: true
  # Everything else false
```
Players can find villages but must explore for other structures. Good for hardcore servers.

**Structure Name Reference:**

**Overworld Common:**
- `village_plains`, `village_desert`, `village_savanna`, `village_snowy`, `village_taiga`
- `pillager_outpost`
- `ruined_portal`
- `shipwreck`, `ocean_ruin_cold`, `ocean_ruin_warm`

**Overworld Rare:**
- `stronghold` - Contains End portal
- `ancient_city` - Deep dark structure
- `mansion` - Woodland mansion
- `monument` - Ocean monument
- `mineshaft` - Abandoned mineshaft

**Overworld Temples:**
- `desert_pyramid` - Desert temple
- `jungle_pyramid` - Jungle temple
- `swamp_hut` - Witch hut
- `igloo` - Ice structure

**Nether:**
- `fortress` - Nether fortress (blazes, nether wart)
- `bastion_remnant` - Piglin bastion
- `ruined_portal_nether` - Ruined portal in Nether

**End:**
- `end_city` - End city with shulkers and elytra

---

## Permissions

### Permission Nodes

| Permission | Default | Description |
|------------|---------|-------------|
| `enhancedcompass.use` | op | Use all compass features (searching, viewing targets) |
| `enhancedcompass.reload` | op | Reload the configuration file |

### Permission Setup Examples

**All Players Can Use:**
```yaml
permissions:
  enhancedcompass.use:
    default: true
```

**Only Specific Rank:**
```yaml
groups:
  member:
    permissions:
      - enhancedcompass.use
  
  admin:
    permissions:
      - enhancedcompass.use
      - enhancedcompass.reload
```

**Using LuckPerms:**
```
/lp group default permission set enhancedcompass.use true
/lp group admin permission set enhancedcompass.reload true
```

---

## Commands

### Player Commands

| Command | Permission | Description |
|---------|------------|-------------|
| `/enhancedcompass help` | enhancedcompass.use | Show help menu |
| `/enhancedcompass <structure>` | enhancedcompass.use | Find specific structure |
| `/enhancedcompass village` | enhancedcompass.use | Find any village type |
| `/enhancedcompass anything` | enhancedcompass.use | Find any enabled structure |
| `/enhancedcompass current` | enhancedcompass.use | Show current target |

### Admin Commands

| Command | Permission | Description |
|---------|------------|-------------|
| `/enhancedcompass reload` | enhancedcompass.reload | Reload configuration |

**Note:** The reload command works from both console and in-game (with permission).

---

## Console Usage

### Reload Configuration
```
enhancedcompass reload
```
No permission required from console. Reloads config.yml and applies changes immediately.

### Viewing Plugin Status
Check console on server startup for:
```
[EnhancedCompass] EnhancedCompass Started!
[EnhancedCompass] By SupaFloof Games, LLC
```

---

## Player Data Storage

### Storage Location
```
plugins/EnhancedCompass/playerdata/
```

### File Format
Each player gets a UUID.yml file:
```
plugins/EnhancedCompass/playerdata/550e8400-e29b-41d4-a716-446655440000.yml
```

### File Contents
```yaml
structure-type: ANCIENT_CITY
world: world
x: 123.456
y: -45.0
z: 789.012
```

### Data Lifecycle
- **Created:** When player first sets a compass target
- **Updated:** Every time player sets a new target
- **Loaded:** When player joins server
- **Deleted:** Manual deletion only (not automatic)

### Data Management

**View a Player's Target:**
```bash
cat plugins/EnhancedCompass/playerdata/<uuid>.yml
```

**Clear a Player's Target:**
```bash
rm plugins/EnhancedCompass/playerdata/<uuid>.yml
```

**Clear All Player Targets:**
```bash
rm plugins/EnhancedCompass/playerdata/*.yml
```

**Backup Player Data:**
```bash
cp -r plugins/EnhancedCompass/playerdata/ backups/enhancedcompass-playerdata-$(date +%Y%m%d)/
```

---

## Performance Considerations

### Boss Bar Updates

The plugin updates boss bars every 10 ticks (0.5 seconds) for all players holding compasses.

**Performance Impact:**
- Minimal for small servers (<50 players)
- Low for medium servers (50-200 players)
- Consider impact on large servers (200+ players)

**Optimization:**
- Boss bars only update for players actually holding compasses
- Distance calculations are simple and fast
- No database queries during updates

### Structure Searches

**What Happens During a Search:**
1. Bukkit queries world generation data
2. Search extends up to configured radius
3. May cause brief lag spike for large search radius

**Optimization Tips:**
- Keep search-radius reasonable (50-100 chunks)
- Disable rarely-used structures to reduce search scope
- Consider adding cooldowns via external plugins if needed

### Memory Usage

**Per Player:**
- 1 CompassTarget object (~100 bytes)
- 1 BossBar object (~500 bytes)
- 1 YAML file on disk (~200 bytes)

**Total:** Negligible even for large servers

---

## Troubleshooting

### Players Can't Use Commands

**Symptoms:** "You don't have permission to use enhanced compass features"

**Solutions:**
1. Grant `enhancedcompass.use` permission
2. Check permission plugin is working correctly
3. Verify player is not in blacklisted world
4. Check server console for errors

### Structures Not Found

**Symptoms:** "No [structure] found within X blocks"

**Possible Causes:**
1. **Structure disabled in config** - Enable it in enabled-structures section
2. **Search radius too small** - Increase search-radius value
3. **Structure doesn't generate in this world** - Check world generator settings
4. **Wrong dimension** - Some structures only spawn in specific dimensions

**Solutions:**
1. Increase search-radius in config
2. Enable more structure types
3. Use `/enhancedcompass anything` to find any nearby structure
4. Check world is properly generated (not void/flat/custom)

### Boss Bar Not Showing

**Symptoms:** Commands work but no boss bar appears

**Causes:**
1. Player not holding compass
2. Player doesn't have target set
3. Client-side resource pack conflict

**Solutions:**
1. Make sure player is holding compass in either hand
2. Run a structure search command first
3. Try relogging
4. Check client doesn't have boss bar disabled

### Config Not Reloading

**Symptoms:** Changes to config.yml don't take effect

**Solutions:**
1. Run `/enhancedcompass reload` (or `enhancedcompass reload` from console)
2. Restart server if reload fails
3. Check config.yml syntax is valid YAML
4. Check console for configuration errors

### Plugin Not Loading

**Symptoms:** Plugin doesn't appear in `/plugins` list

**Causes:**
1. Wrong server type (needs Paper/Spigot)
2. Wrong Java version
3. Corrupted JAR file
4. Dependency conflict

**Solutions:**
1. Verify server is Paper or Spigot (1.19+)
2. Check Java version matches server requirements
3. Re-download plugin JAR
4. Check console for startup errors

---

## Best Practices

### Configuration

1. **Start Conservative**: Begin with moderate search-radius (50-100)
2. **Test Before Production**: Try config changes on test server first
3. **Balance Gameplay**: Consider disabling structures that would trivialize progression
4. **Document Changes**: Keep notes on why certain structures are disabled

### Permission Management

1. **Default Allow**: Grant enhancedcompass.use to all players by default
2. **Restrict Admin Commands**: Keep enhancedcompass.reload for staff only
3. **World-Specific Rules**: Use world blacklist rather than per-world permissions

### Maintenance

1. **Regular Backups**: Back up playerdata folder regularly
2. **Monitor Performance**: Watch for lag spikes after large player groups search
3. **Update Regularly**: Keep plugin updated for bug fixes and new features
4. **Clean Old Data**: Periodically remove data for players who haven't joined in months

### Player Support

1. **Provide Documentation**: Link players to end user guide
2. **Set Expectations**: Tell players which structures are enabled/disabled
3. **Explain Limits**: Inform players about search radius limits
4. **Handle Reports**: Respond to "structure not found" reports with search radius info

---

## Integration with Other Plugins

### Compatible Plugins

- ✅ **Essentials** - No conflicts
- ✅ **WorldGuard** - No conflicts (respects region protections)
- ✅ **Multiverse** - Works with multiple worlds
- ✅ **LuckPerms** - Full permission support
- ✅ **Vault** - Not required, no economy integration
- ✅ **Dynmap** - No conflicts

### Potential Conflicts

- ⚠️ **Custom Compass Plugins** - May conflict with other plugins that modify compass behavior
- ⚠️ **Custom Boss Bar Plugins** - May cause visual overlap if both show boss bars simultaneously

### Recommended Combinations

**Exploration Server:**
```
- EnhancedCompass (for structure finding)
- Dynmap (for mapping)
- BlueMap (for web map)
```

**Survival Server:**
```
- EnhancedCompass (limited structures)
- Essentials (for /home, /spawn)
- GriefPrevention (for land protection)
```

---

## Updating the Plugin

### Update Process

1. **Backup** current config and playerdata:
   ```bash
   cp -r plugins/EnhancedCompass/ backups/
   ```

2. **Download** new version

3. **Replace** JAR file:
   ```bash
   mv EnhancedCompass.jar plugins/
   ```

4. **Restart** server

5. **Test** functionality:
   - Run `/enhancedcompass help`
   - Try a structure search
   - Check boss bar appears

6. **Verify** config compatibility (check console for warnings)

### Version Compatibility

- Config format is forward-compatible
- New structures may be added in updates
- Old playerdata files remain valid

---

## Support & Resources

### Getting Help

1. **Check Documentation**: End user guide, this admin guide, dev guide
2. **Console Logs**: Check for error messages on startup and during use
3. **Test Mode**: Try commands from console to rule out permission issues
4. **Community**: Check with other server admins

### Reporting Issues

When reporting issues, include:
- Server version (Paper/Spigot + MC version)
- Plugin version
- Config.yml contents
- Console error messages
- Steps to reproduce

---

## FAQ for Admins

**Q: Does this work with custom world generators?**  
A: Yes, as long as structures are properly registered. Some custom generators may not include all structures.

**Q: Can I disable compass for specific player ranks?**  
A: Yes, don't grant `enhancedcompass.use` permission to those ranks.

**Q: How much disk space does player data use?**  
A: ~200 bytes per player. 1,000 players = ~200 KB. Negligible.

**Q: Can I pre-load structures to speed up searches?**  
A: No, the plugin uses Minecraft's built-in structure location API which requires real-time world queries.

**Q: What happens if a player's target world is deleted?**  
A: Plugin logs a warning and the target won't load. Player can set a new target normally.

**Q: Can I migrate player data from another compass plugin?**  
A: Not directly, but you can manually create YAML files in the correct format.

**Q: Does this work on 1.18 or earlier?**  
A: The plugin is designed for 1.19+. Earlier versions may work but are unsupported.

**Q: Can I add custom structures to the config?**  
A: Yes! Any structure registered in Minecraft's Registry will work. Just add it to the config.

---

## Quick Reference

### Essential Commands
```
/enhancedcompass reload    # Reload config
enhancedcompass reload     # Console reload
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
search-radius: 100                              # Search distance
blacklisted-worlds: []                          # Disabled worlds
enabled-structures.normal: {}                   # Overworld structures
enabled-structures.nether: {}                   # Nether structures
enabled-structures.the_end: {}                  # End structures
```

---

## Changelog Format

When updating, check for:
- New structure types added to config
- Permission changes
- Command changes
- Breaking config changes

---

**For additional support, refer to the Developer Guide (devguide.md) for technical details, or the End User Guide (enduser.md) to help your players.**
