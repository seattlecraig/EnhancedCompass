# EnhancedCompass - End User Guide

## What is EnhancedCompass?

EnhancedCompass is a plugin that supercharges your Minecraft compass! Instead of just pointing to your spawn point, your compass can now point to **any structure** in the game - ancient cities, villages, strongholds, End cities, and more!

Even better, when you hold your compass, a **boss bar** appears at the top of your screen showing you the structure name and exactly how far away it is in real-time. Watch the distance count down as you get closer!

---

## How to Activate The /enchancedcompass (or /ecompass) Command

It is a perk onlocked by buying and using a Perk Key at https://pixels.supafloof.com

## Features at a Glance

✨ **Point to Any Structure**: Find villages, temples, strongholds, ancient cities, and more  
📏 **Real-Time Distance**: Boss bar shows your distance to the target as you travel  
🌍 **All Dimensions**: Works in Overworld, Nether, and End  
🔍 **Smart Search**: Special commands to find ANY village or ANY nearby structure  
💾 **Remembers Your Target**: Your compass keeps pointing to your target even after logout  
🎯 **Easy to Use**: Simple commands with auto-complete

---

## Getting Started

### Basic Usage

1. **Hold a compass** in your main hand or off hand
2. **Run a search command** to find a structure
3. **A boss bar appears** showing the structure and distance
4. **Follow your compass** - it points directly to the structure!

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

**Pro Tip:** Use tab completion! Start typing a structure name and press TAB to see all available structures for your current dimension.

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
- **Aqua structure name** = Target is in your dimension
- **Yellow distance** = How far away in blocks

### Different Dimension
```
End City - Not in same dimension
```
- **Red structure name** = Target is in a different dimension
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

### Distance Strategy

- The compass shows straight-line distance (as the crow flies)
- Actual travel distance may be longer due to terrain
- Y-level (height) is included in distance calculations
- For underground structures like ancient cities, you may need to dig down when you get close

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

### "No [structure] found within X blocks"

The structure wasn't found within the search radius. This could mean:
- The structure is farther away
- The structure doesn't exist in your world (rare)
- Try searching for a different structure type

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

### Scenario 3: Exploration Mode
```
/enhancedcompass anything
```
*Could find a mineshaft, temple, mansion, or any other nearby structure!*

### Scenario 4: Checking Your Target
```
/enhancedcompass current
```
*Output: "Current target: Stronghold - Distance: 3,421 blocks"*

---

## Frequently Asked Questions

**Q: Does this work with modded structures?**  
A: The plugin uses Minecraft's built-in structure system, so it depends on whether the mod properly registers structures with Minecraft.

**Q: Can I have multiple targets?**  
A: No, you can only have one target at a time. Setting a new target replaces the old one.

**Q: Does this consume my compass?**  
A: No! The compass is never consumed. You can use any compass, even an enchanted one.

**Q: Will this help me find buried treasure?**  
A: Yes! Try `/enhancedcompass buried_treasure` 

**Q: What if I can't find the structure when I get there?**  
A: Some structures generate underground or underwater. The compass points to the structure center, but you may need to explore the area.

**Q: Can other players see my boss bar?**  
A: No, the boss bar is personal to you. Other players won't see it.

---

## Command Quick Reference

```
/enhancedcompass help                  # Show help
/enhancedcompass village               # Find any village
/enhancedcompass anything              # Find any structure
/enhancedcompass <structure>           # Find specific structure
/enhancedcompass current               # Show current target
```

Press TAB while typing for auto-completion!

---

## Need More Help?

- Use `/enhancedcompass help` in-game
- Ask your server administrators
- Check if certain structures are disabled on your server
- Make sure you have the correct permissions

Happy exploring! 🧭

---

**Made with ❤️ by SupaFloof Games, LLC**