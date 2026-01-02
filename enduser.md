# EnhancedCompass - End User Guide

## What is EnhancedCompass?

EnhancedCompass transforms your Minecraft compass into a powerful navigation tool. Instead of just pointing to your spawn point, your compass can now point to **any structure** or **any biome** in the game—ancient cities, villages, strongholds, End cities, dark forests, cherry groves, and more.

When you hold your compass, a **boss bar** appears at the top of your screen showing the target name and exactly how far away it is. The distance updates in real-time every half second as you travel, so you can watch the count go down as you get closer.

---

## How to Unlock EnhancedCompass

The `/enhancedcompass` command (or `/ecompass`) is a perk unlocked by purchasing and using a Perk Key at https://pixels.supafloof.com

---

## Features

- **Point to Any Structure**: Find villages, temples, strongholds, ancient cities, and more
- **Point to Any Biome**: Find dark forests, cherry groves, mushroom fields, and more
- **Real-Time Distance**: Boss bar shows distance to target, updating every 0.5 seconds
- **All Dimensions**: Works in Overworld, Nether, and End
- **Smart Searches**: Special commands to find ANY village or ANY nearby structure
- **Persistent Targets**: Your compass keeps pointing to your target even after logout
- **Tab Completion**: Auto-complete for all commands, structure names, and biome names

---

## Getting Started

### Basic Usage

1. **Hold a compass** in your main hand or off hand
2. **Run a search command** to find a structure or biome
3. **A boss bar appears** showing the target and distance
4. **Follow your compass**—it points directly to the target

### Your First Search

Find the nearest village:

```
/enhancedcompass village
```

The plugin will search for all types of villages (plains, desert, snowy, savanna, taiga), find the closest one, point your compass to it, and show you the distance. Hold your compass and follow it—the boss bar updates in real-time.

---

## Commands

### Main Commands

| Command | Description |
|---------|-------------|
| `/enhancedcompass help` | Shows the help menu |
| `/enhancedcompass <structure>` | Points compass to nearest structure |
| `/enhancedcompass biome <biome>` | Points compass to nearest biome |
| `/enhancedcompass village` | Finds nearest village of ANY type |
| `/enhancedcompass anything` | Finds nearest structure of ANY type |
| `/enhancedcompass current` | Shows your current target and distance |

### Structure Examples

**Overworld:**
```
/enhancedcompass ancient_city
/enhancedcompass stronghold
/enhancedcompass village_plains
/enhancedcompass mansion
/enhancedcompass monument
/enhancedcompass desert_pyramid
/enhancedcompass jungle_pyramid
/enhancedcompass mineshaft
/enhancedcompass shipwreck
/enhancedcompass buried_treasure
/enhancedcompass trail_ruins
/enhancedcompass trial_chambers
```

**Nether:**
```
/enhancedcompass fortress
/enhancedcompass bastion_remnant
```

**End:**
```
/enhancedcompass end_city
```

### Biome Examples

**Overworld:**
```
/enhancedcompass biome dark_forest
/enhancedcompass biome cherry_grove
/enhancedcompass biome mushroom_fields
/enhancedcompass biome deep_dark
/enhancedcompass biome lush_caves
/enhancedcompass biome jungle
/enhancedcompass biome badlands
/enhancedcompass biome ice_spikes
/enhancedcompass biome flower_forest
/enhancedcompass biome mangrove_swamp
/enhancedcompass biome pale_garden
/enhancedcompass biome meadow
```

**Nether:**
```
/enhancedcompass biome crimson_forest
/enhancedcompass biome warped_forest
/enhancedcompass biome soul_sand_valley
/enhancedcompass biome basalt_deltas
```

**End:**
```
/enhancedcompass biome end_highlands
/enhancedcompass biome small_end_islands
```

Use **tab completion** to see all available structures and biomes for your current dimension.

---

## Special Searches

### The "Village" Search

Can't remember all the village types? No problem:

```
/enhancedcompass village
```

This searches for village_plains, village_desert, village_savanna, village_snowy, and village_taiga simultaneously and finds whichever one is closest to you.

### The "Anything" Search

Feeling adventurous?

```
/enhancedcompass anything
```

This searches for **every enabled structure type** in your current dimension and points you to the absolute closest one. Perfect for random exploration.

---

## Understanding the Boss Bar

When you hold a compass with a target set, a boss bar appears at the top of your screen.

### Same Dimension
```
Ancient City - 1,432 blocks
```
```
Dark Forest - 523 blocks
```
- **Aqua target name** = Target is in your dimension
- **Yellow distance** = How far away in blocks

### Different Dimension
```
End City - Not in same dimension
```
- **Red target name** = Target is in a different dimension
- **Red warning** = You need to change dimensions

### Boss Bar Behavior

- Appears when you hold a compass (main or off hand)
- Updates every 0.5 seconds while you hold the compass
- Disappears when you put the compass away
- Persists when switching between hands

---

## Tips & Tricks

### Finding Structures Faster

1. **Use tab completion**: Start typing and press TAB to see available structures
2. **Try "village" or "anything"** for quick exploration
3. **Check your current target** with `/enhancedcompass current` if you forget what you're searching for

### Finding Biomes

1. **Use tab completion**: Type `/enhancedcompass biome ` and press TAB
2. **Biomes are great for**: Finding specific resources, locating rare mobs, building in unique terrain
3. **Some biomes are rare**: Cherry groves, mushroom fields, and ice spikes may be far away

### Distance Strategy

- Distance shown is straight-line distance (as the crow flies)
- Actual travel distance may be longer due to terrain
- Y-level (height) is included in distance calculations
- For underground targets like ancient cities or deep dark biomes, you may need to dig down when you get close

### Cross-Dimension Targets

If you set a target in the Overworld, then go to the Nether:
- Your compass will still point toward the target
- The boss bar will say "Not in same dimension"
- Return to the Overworld to see the actual distance again

### Persistent Targets

Your compass target is automatically saved. It stays set even if you:
- Log out and back in
- Die and respawn
- The server restarts

To change your target, simply run a new search command.

---

## Examples & Scenarios

### Finding a Village to Trade
```
/enhancedcompass village
```
Boss bar appears: "Village Plains - 892 blocks"

### Finding an Ancient City
```
/enhancedcompass ancient_city
```
Boss bar appears: "Ancient City - 2,134 blocks"  
Travel toward the compass direction, digging down when close.

### Finding a Cherry Grove
```
/enhancedcompass biome cherry_grove
```
Boss bar appears: "Cherry Grove - 1,876 blocks"  
Follow the compass to find beautiful pink trees.

### Exploration Mode
```
/enhancedcompass anything
```
Could find a mineshaft, temple, mansion, or any other nearby structure.

### Checking Your Target
```
/enhancedcompass current
```
Output: "Current target: Stronghold" and "Distance: 3,421 blocks"

### Finding Rare Biomes
```
/enhancedcompass biome mushroom_fields
```
Boss bar appears: "Mushroom Fields - 4,521 blocks"  
Rare biomes may be far away, but you'll get there.

---

## Command Quick Reference

```
/enhancedcompass help                  # Show help
/enhancedcompass village               # Find any village
/enhancedcompass anything              # Find any structure
/enhancedcompass <structure>           # Find specific structure
/enhancedcompass biome <biome>         # Find specific biome
/enhancedcompass current               # Show current target
```

Press TAB while typing for auto-completion.

---

Happy exploring! 🧭

---

**Made with ❤️ by SupaFloof Games, LLC**
