# EnhancedCompass - End User Guide

## What is EnhancedCompass?

EnhancedCompass is a plugin that supercharges your Minecraft compass! Instead of just pointing to your spawn point, your compass can now point to **any structure** or **any biome** in the game - ancient cities, villages, strongholds, End cities, dark forests, cherry groves, and more!

Even better, when you hold your compass, a **boss bar** appears at the top of your screen showing you the target name and exactly how far away it is in real-time. Watch the distance count down as you get closer!

---

## How to Activate The /enchancedcompass (or /ecompass) Command

It is a perk onlocked by buying and using a Perk Key at https://pixels.supafloof.com

## Features at a Glance

✨ **Point to Any Structure**: Find villages, temples, strongholds, ancient cities, and more  
🌲 **Point to Any Biome**: Find dark forests, cherry groves, mushroom fields, and more  
📏 **Real-Time Distance**: Boss bar shows your distance to the target as you travel  
🌍 **All Dimensions**: Works in Overworld, Nether, and End  
🔍 **Smart Search**: Special commands to find ANY village or ANY nearby structure  
💾 **Remembers Your Target**: Your compass keeps pointing to your target even after logout  
🎯 **Easy to Use**: Simple commands with auto-complete

---

## Getting Started

### Basic Usage

1. **Hold a compass** in your main hand or off hand
2. **Run a search command** to find a structure or biome
3. **A boss bar appears** showing the target and distance
4. **Follow your compass** - it points directly to the target!

### Your First Search

Let's find the nearest village:

```
/enhancedcompass village
```

The plugin will:
- Search for all types of villages (plains, desert, snowy, etc.)
- Find the closest one
- Point your compass to it
- Show you the distance

Now just hold your compass and follow it! The boss bar will update every half second showing your distance.

---

## Commands

### Main Commands

| Command | Description | Example |
|---------|-------------|---------|
| `/enhancedcompass help` | Shows the help menu | `/enhancedcompass help` |
| `/enhancedcompass <structure>` | Points compass to nearest structure | `/enhancedcompass ancient_city` |
| `/enhancedcompass biome <biome>` | Points compass to nearest biome | `/enhancedcompass biome dark_forest` |
| `/enhancedcompass village` | Finds nearest village of ANY type | `/enhancedcompass village` |
| `/enhancedcompass anything` | Finds nearest structure of ANY type | `/enhancedcompass anything` |
| `/enhancedcompass current` | Shows your current target | `/enhancedcompass current` |

### Structure Examples

Here are some popular structures you can search for:

**Overworld:**
```
/enhancedcompass ancient_city
/enhancedcompass stronghold
/enhancedcompass village_plains
/enhancedcompass mansion
/enhancedcompass monument
/enhancedcompass temple_desert
/enhancedcompass mineshaft
/enhancedcompass shipwreck
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

Here are some popular biomes you can search for:

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

**Pro Tip:** Use tab completion! Start typing a structure or biome name and press TAB to see all available options for your current dimension.

---

## Special Searches

### The "Village" Search

Can't remember all the village types? No problem!

```
/enhancedcompass village
```

This searches for:
- village_plains
- village_desert
- village_savanna
- village_snowy
- village_taiga

...and finds whichever one is closest to you!

### The "Anything" Search

Feeling adventurous? Want to find the nearest structure without caring what it is?

```
/enhancedcompass anything
```

This searches for **every enabled structure type** in your current dimension and points you to the absolute closest one. Perfect for exploration!

---

## Understanding the Boss Bar

When you hold a compass with a target set, a boss bar appears at the top of your screen:

### Same Dimension
```
Ancient City - 1,432 blocks
```
or
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
- **Red warning message** = You need to change dimensions

### Boss Bar Behavior

- ✅ **Appears** when you hold a compass (main or off hand)
- ✅ **Updates** every 0.5 seconds while you hold the compass
- ✅ **Disappears** when you put the compass away
- ✅ **Persists** when switching between hands

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

- The compass shows straight-line distance (as the crow flies)
- Actual travel distance may be longer due to terrain
- Y-level (height) is included in distance calculations
- For underground targets like ancient cities or deep dark biomes, you may need to dig down when you get close

### Cross-Dimension Targets

If you set a target in the Overworld, then go to the Nether:
- Your compass will still point toward the target
- The boss bar will say "Not in same dimension"
- Return to the Overworld to see the actual distance again

### Persistent Targets

Your compass target is automatically saved! It will still be set even if you:
- Log out and back in
- The server restarts
- You die and respawn

To change your target, simply run a new search command.

---

## Troubleshooting

### "You don't have permission to use enhanced compass features"

Your server has permission restrictions enabled. Ask an admin for the `enhancedcompass.use` permission.

### "Enhanced compass is disabled in this world"

The server admin has disabled the compass in this world. This is common in lobby worlds or special game areas.

### "This structure type is not enabled in the current world type"

The structure you searched for either:
- Doesn't exist in your current dimension (e.g., searching for fortress in the Overworld)
- Has been disabled by the server admin

Try using tab completion to see what structures are available in your dimension.

### "This biome type is not enabled in the current world type"

The biome you searched for either:
- Doesn't exist in your current dimension (e.g., searching for crimson_forest in the Overworld)
- Has been disabled by the server admin

Try using tab completion to see what biomes are available in your dimension.

### "No [structure/biome] found within X blocks"

The target wasn't found within the search radius. This could mean:
- The target is farther away
- The target doesn't exist in your world (rare)
- Try searching for a different target

### Boss bar isn't showing

Make sure you're:
1. Actually holding a compass (check both hands)
2. Have set a target with a search command
3. Have the `enhancedcompass.use` permission


---

## Examples & Scenarios

### Scenario 1: Finding a Village to Trade
```
/enhancedcompass village
```
*Compass points to nearest village. Boss bar shows: "Village Plains - 892 blocks"*

### Scenario 2: Finding an Ancient City
```
/enhancedcompass ancient_city
```
*Boss bar appears: "Ancient City - 2,134 blocks"*  
*Travel toward the compass direction, digging down when close*

### Scenario 3: Finding a Cherry Grove
```
/enhancedcompass biome cherry_grove
```
*Boss bar appears: "Cherry Grove - 1,876 blocks"*  
*Follow the compass to find beautiful pink trees!*

### Scenario 4: Exploration Mode
```
/enhancedcompass anything
```
*Could find a mineshaft, temple, mansion, or any other nearby structure!*

### Scenario 5: Checking Your Target
```
/enhancedcompass current
```
*Output: "Current target (Structure): Stronghold - Distance: 3,421 blocks"*
*or: "Current target (Biome): Dark Forest - Distance: 456 blocks"*

### Scenario 6: Finding Rare Biomes
```
/enhancedcompass biome mushroom_fields
```
*Boss bar appears: "Mushroom Fields - 4,521 blocks"*  
*Rare biomes may be far away, but you'll get there!*

---

## Frequently Asked Questions

**Q: Does this work with modded structures?**  
A: The plugin uses Minecraft's built-in structure system, so it depends on whether the mod properly registers structures with Minecraft.

**Q: Does this work with modded biomes?**  
A: Yes, as long as the mod registers biomes properly with Minecraft's registry.

**Q: Can I have multiple targets?**  
A: No, you can only have one target at a time. Setting a new target replaces the old one.

**Q: Does this consume my compass?**  
A: No! The compass is never consumed. You can use any compass, even an enchanted one.

**Q: Will this help me find buried treasure?**  
A: Yes! Try `/enhancedcompass buried_treasure` 

**Q: Can I find the deep dark biome?**  
A: Yes! Try `/enhancedcompass biome deep_dark`

**Q: What if I can't find the structure/biome when I get there?**  
A: Some targets are underground or underwater. The compass points to the target center, but you may need to explore the area.

**Q: Can other players see my boss bar?**  
A: No, the boss bar is personal to you. Other players won't see it.

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

Press TAB while typing for auto-completion!

---

## Need More Help?

- Use `/enhancedcompass help` in-game
- Ask your server administrators
- Check if certain structures or biomes are disabled on your server
- Make sure you have the correct permissions

Happy exploring! 🧭

---

**Made with ❤️ by SupaFloof Games, LLC**
