package com.supafloof.enhancedcompass;

import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

/**
 * EnhancedCompass Plugin
 * 
 * Allows players to point their compass to any structure type and see real-time distance
 * updates via a boss bar while holding the compass.
 * 
 * Features:
 * - Permission-based access (enhancedcompass.use)
 * - All structure types supported (Overworld, Nether, End)
 * - Generic "village" search - finds closest village of any type
 * - Generic "anything" search - finds closest structure of any enabled type
 * - Configurable structure whitelist per dimension
 * - Configurable search radius
 * - World blacklist
 * - Real-time boss bar distance display
 * - Tab completion for all commands and structure types
 * 
 * @author SupaFloof Games, LLC
 * @version 1.0.0
 */
public class EnhancedCompass extends JavaPlugin implements CommandExecutor, TabCompleter, Listener {
    
    // Stores each player's current compass target (structure type and location)
    private Map<UUID, CompassTarget> playerTargets = new HashMap<>();
    
    // Stores the boss bar for each player showing distance to their target
    private Map<UUID, BossBar> playerBossBars = new HashMap<>();
    
    // Manages configuration loading and validation
    private ConfigManager configManager;
    
    // Task that updates boss bars every 0.5 seconds
    private BukkitRunnable updateTask;
    
    // Directory for storing player data
    private File playerDataFolder;
    
    /**
     * Called when the plugin is enabled
     * Sets up config, commands, events, and starts the boss bar update task
     */
    @Override
    public void onEnable() {
        // Save default config.yml if it doesn't exist
        saveDefaultConfig();
        
        // Initialize configuration manager
        configManager = new ConfigManager(this);
        
        // Create player data folder
        playerDataFolder = new File(getDataFolder(), "playerdata");
        if (!playerDataFolder.exists()) {
            playerDataFolder.mkdirs();
        }
        
        // Register command executor and tab completer
        getCommand("enhancedcompass").setExecutor(this);
        getCommand("enhancedcompass").setTabCompleter(this);
        
        // Register event listener for player disconnects
        getServer().getPluginManager().registerEvents(this, this);
        
        // Start the task that updates boss bars for players holding compasses
        startUpdateTask();
        
        // Send colored startup messages to console
        getServer().getConsoleSender().sendMessage(Component.text("[EnhancedCompass] EnhancedCompass Started!", NamedTextColor.GREEN));
        getServer().getConsoleSender().sendMessage(Component.text("[EnhancedCompass] By SupaFloof Games, LLC", NamedTextColor.LIGHT_PURPLE));
    }
    
    /**
     * Called when the plugin is disabled
     * Cleans up boss bars and cancels update task
     */
    @Override
    public void onDisable() {
        // Cancel the boss bar update task
        if (updateTask != null && !updateTask.isCancelled()) {
            updateTask.cancel();
        }
        
        // Hide and remove all boss bars from players
        for (Map.Entry<UUID, BossBar> entry : playerBossBars.entrySet()) {
            Player player = Bukkit.getPlayer(entry.getKey());
            if (player != null) {
                player.hideBossBar(entry.getValue());
            }
        }
        playerBossBars.clear();
        playerTargets.clear();
        
        getLogger().info("EnhancedCompass has been disabled!");
    }
    
    /**
     * Starts a repeating task that updates boss bars for all online players
     * Runs every 10 ticks (0.5 seconds) to show real-time distance updates
     */
    private void startUpdateTask() {
        updateTask = new BukkitRunnable() {
            @Override
            public void run() {
                // Check each online player
                for (Player player : Bukkit.getOnlinePlayers()) {
                    // Check if player is holding a compass in either hand
                    boolean holdingCompass = player.getInventory().getItemInMainHand().getType() == Material.COMPASS ||
                                            player.getInventory().getItemInOffHand().getType() == Material.COMPASS;
                    
                    // If holding compass and has a target set and has permission
                    if (holdingCompass) {
                        CompassTarget target = playerTargets.get(player.getUniqueId());
                        if (target != null && player.hasPermission("enhancedcompass.use")) {
                            updateBossBar(player, target);
                        }
                    } else {
                        // Not holding compass, remove their boss bar
                        removeBossBar(player);
                    }
                }
            }
        };
        // Run every 10 ticks (0.5 seconds), starting immediately
        updateTask.runTaskTimer(this, 0L, 10L);
    }
    
    /**
     * Updates or creates a boss bar for a player showing their compass target and distance
     * 
     * @param player The player to show the boss bar to
     * @param target The compass target containing structure type and location
     */
    private void updateBossBar(Player player, CompassTarget target) {
        BossBar bossBar = playerBossBars.get(player.getUniqueId());
        
        // Create boss bar if it doesn't exist
        if (bossBar == null) {
            bossBar = BossBar.bossBar(Component.empty(), 1.0f, BossBar.Color.BLUE, BossBar.Overlay.PROGRESS);
            player.showBossBar(bossBar);
            playerBossBars.put(player.getUniqueId(), bossBar);
        }
        
        // Check if target is in same world as player
        if (target.location != null && player.getWorld().equals(target.location.getWorld())) {
            // Calculate distance and format boss bar title
            double distance = player.getLocation().distance(target.location);
            String structureName = formatStructureName(target.structureType);
            
            Component title = Component.text(structureName, NamedTextColor.AQUA)
                .append(Component.text(" - ", NamedTextColor.GRAY))
                .append(Component.text(String.format("%.0f", distance) + " blocks", NamedTextColor.YELLOW));
            
            bossBar.name(title);
            bossBar.progress(1.0f);
        } else {
            // Target is in different dimension
            String structureName = formatStructureName(target.structureType);
            Component title = Component.text(structureName, NamedTextColor.RED)
                .append(Component.text(" - ", NamedTextColor.GRAY))
                .append(Component.text("Not in same dimension", NamedTextColor.RED));
            
            bossBar.name(title);
            bossBar.progress(1.0f);
        }
    }
    
    /**
     * Removes and hides a player's boss bar
     * 
     * @param player The player whose boss bar to remove
     */
    private void removeBossBar(Player player) {
        BossBar bossBar = playerBossBars.remove(player.getUniqueId());
        if (bossBar != null) {
            player.hideBossBar(bossBar);
        }
    }
    
    /**
     * Formats a structure type name for display
     * Converts ANCIENT_CITY to "Ancient City"
     * 
     * @param structureType The structure type in UPPER_CASE format
     * @return Formatted structure name with proper capitalization
     */
    private String formatStructureName(String structureType) {
        String[] words = structureType.split("_");
        StringBuilder result = new StringBuilder();
        
        for (String word : words) {
            if (result.length() > 0) {
                result.append(" ");
            }
            result.append(word.substring(0, 1).toUpperCase()).append(word.substring(1).toLowerCase());
        }
        
        return result.toString();
    }
    
    /**
     * Sends help message with styled formatting
     */
    private void sendHelp(CommandSender sender) {
        sender.sendMessage(Component.text("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━", NamedTextColor.GOLD));
        sender.sendMessage(Component.text("Enhanced Compass Commands", NamedTextColor.GOLD, TextDecoration.BOLD));
        sender.sendMessage(Component.text("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━", NamedTextColor.GOLD));
        sender.sendMessage(Component.text("/enhancedcompass help", NamedTextColor.YELLOW)
            .append(Component.text(" - Show this help menu", NamedTextColor.GRAY)));
        sender.sendMessage(Component.text("/enhancedcompass <structure>", NamedTextColor.YELLOW)
            .append(Component.text(" - Point compass to nearest structure", NamedTextColor.GRAY)));
        sender.sendMessage(Component.text("/enhancedcompass current", NamedTextColor.YELLOW)
            .append(Component.text(" - Show current compass target", NamedTextColor.GRAY)));
        
        // Only show reload command to console or players with reload permission
        if (!(sender instanceof Player) || sender.hasPermission("enhancedcompass.reload")) {
            sender.sendMessage(Component.text("/enhancedcompass reload", NamedTextColor.YELLOW)
                .append(Component.text(" - Reload configuration", NamedTextColor.GRAY)));
        }
        
        sender.sendMessage(Component.text("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━", NamedTextColor.GOLD));
    }
    
    /**
     * Handles all /enhancedcompass commands
     * 
     * @param sender The command sender (player or console)
     * @param command The command object
     * @param label The command alias used
     * @param args Command arguments
     * @return true if command was handled successfully
     */
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // Handle reload command (works from console)
        if (args.length > 0 && args[0].equalsIgnoreCase("reload")) {
            // Only check permission if sender is a player (console always allowed)
            if (sender instanceof Player && !sender.hasPermission("enhancedcompass.reload")) {
                sender.sendMessage(Component.text("You don't have permission to reload the configuration.", NamedTextColor.RED));
                return true;
            }
            
            // Reload config and reinitialize config manager
            reloadConfig();
            configManager = new ConfigManager(this);
            sender.sendMessage(Component.text("EnhancedCompass configuration reloaded!", NamedTextColor.GREEN));
            return true;
        }
        
        // Handle help command
        if (args.length > 0 && args[0].equalsIgnoreCase("help")) {
            sendHelp(sender);
            return true;
        }
        
        // All other commands require a player
        if (!(sender instanceof Player)) {
            sender.sendMessage(Component.text("This command can only be used by players (except /enhancedcompass reload).", NamedTextColor.RED));
            return true;
        }
        
        Player player = (Player) sender;
        
        // Check if player has permission to use enhanced compass
        if (!player.hasPermission("enhancedcompass.use")) {
            player.sendMessage(Component.text("You don't have permission to use enhanced compass features.", NamedTextColor.RED));
            return true;
        }
        
        // Check if enhanced compass is disabled in current world
        if (configManager.isWorldBlacklisted(player.getWorld().getName())) {
            player.sendMessage(Component.text("Enhanced compass is disabled in this world.", NamedTextColor.RED));
            return true;
        }
        
        // Show help if no arguments provided
        if (args.length == 0) {
            sendHelp(player);
            return true;
        }
        
        // Handle "current" command - shows current target and distance
        if (args[0].equalsIgnoreCase("current")) {
            CompassTarget target = playerTargets.get(player.getUniqueId());
            
            if (target == null) {
                player.sendMessage(Component.text("You don't have a compass target set.", NamedTextColor.YELLOW));
                return true;
            }
            
            String structureName = formatStructureName(target.structureType);
            
            // Check if target is in same dimension
            if (target.location != null && player.getWorld().equals(target.location.getWorld())) {
                double distance = player.getLocation().distance(target.location);
                player.sendMessage(Component.text("Current target: ", NamedTextColor.GREEN)
                    .append(Component.text(structureName, NamedTextColor.AQUA)));
                player.sendMessage(Component.text("Distance: ", NamedTextColor.GREEN)
                    .append(Component.text(String.format("%.0f", distance) + " blocks", NamedTextColor.YELLOW)));
            } else {
                player.sendMessage(Component.text("Current target: ", NamedTextColor.GREEN)
                    .append(Component.text(structureName, NamedTextColor.AQUA)));
                player.sendMessage(Component.text("Target is in a different dimension.", NamedTextColor.RED));
            }
            
            return true;
        }
        
        // Handle structure search command
        String structureInput = args[0].toLowerCase();
        
        // Handle special "village" search - searches all village types
        if (structureInput.equals("village")) {
            player.sendMessage(Component.text("Searching for nearest village of any type...", NamedTextColor.YELLOW));
            
            int searchRadius = configManager.getSearchRadius();
            
            // List of all village types
            String[] villageTypes = {
                "village_plains",
                "village_desert", 
                "village_savanna",
                "village_snowy",
                "village_taiga"
            };
            
            Location closestLocation = null;
            String closestVillageType = null;
            double closestDistance = Double.MAX_VALUE;
            
            // Search for each village type and find the closest
            for (String villageType : villageTypes) {
                // Check if this village type is enabled
                if (!configManager.isStructureEnabled(player.getWorld().getEnvironment(), villageType.toUpperCase())) {
                    continue;
                }
                
                org.bukkit.generator.structure.Structure structure = Registry.STRUCTURE.get(NamespacedKey.minecraft(villageType));
                if (structure == null) {
                    continue;
                }
                
                var structureResult = player.getWorld().locateNearestStructure(
                    player.getLocation(),
                    structure,
                    searchRadius,
                    false
                );
                
                if (structureResult != null) {
                    Location loc = structureResult.getLocation();
                    double distance = player.getLocation().distance(loc);
                    
                    if (distance < closestDistance) {
                        closestDistance = distance;
                        closestLocation = loc;
                        closestVillageType = villageType.toUpperCase();
                    }
                }
            }
            
            // Check if any village was found
            if (closestLocation == null) {
                player.sendMessage(Component.text("No villages found within " + (searchRadius * 16) + " blocks.", NamedTextColor.RED));
                return true;
            }
            
            // Set compass target to closest village
            player.setCompassTarget(closestLocation);
            
            // Store the target for boss bar updates
            CompassTarget target = new CompassTarget(closestVillageType, closestLocation);
            playerTargets.put(player.getUniqueId(), target);
            
            // Display results
            player.sendMessage(Component.text("Found " + formatStructureName(closestVillageType) + "!", NamedTextColor.GREEN));
            player.sendMessage(Component.text("Compass now pointing to " + formatStructureName(closestVillageType) + "!", NamedTextColor.GREEN));
            player.sendMessage(Component.text("Distance: ", NamedTextColor.GREEN)
                .append(Component.text(String.format("%.0f", closestDistance) + " blocks", NamedTextColor.YELLOW)));
            
            // Save the player's target
            savePlayerTarget(player, target);
            
            return true;
        }
        
        // Handle special "anything" search - searches ALL enabled structure types
        if (structureInput.equals("anything")) {
            player.sendMessage(Component.text("Searching for nearest structure of any type...", NamedTextColor.YELLOW));
            
            int searchRadius = configManager.getSearchRadius();
            World.Environment environment = player.getWorld().getEnvironment();
            List<String> enabledStructures = configManager.getEnabledStructuresForEnvironment(environment);
            
            if (enabledStructures.isEmpty()) {
                player.sendMessage(Component.text("No structures are enabled in this world type.", NamedTextColor.RED));
                return true;
            }
            
            Location closestLocation = null;
            String closestStructureType = null;
            double closestDistance = Double.MAX_VALUE;
            
            // Search for each enabled structure type and find the closest
            for (String structureType : enabledStructures) {
                org.bukkit.generator.structure.Structure structure = Registry.STRUCTURE.get(NamespacedKey.minecraft(structureType.toLowerCase()));
                if (structure == null) {
                    continue;
                }
                
                var structureResult = player.getWorld().locateNearestStructure(
                    player.getLocation(),
                    structure,
                    searchRadius,
                    false
                );
                
                if (structureResult != null) {
                    Location loc = structureResult.getLocation();
                    double distance = player.getLocation().distance(loc);
                    
                    if (distance < closestDistance) {
                        closestDistance = distance;
                        closestLocation = loc;
                        closestStructureType = structureType;
                    }
                }
            }
            
            // Check if any structure was found
            if (closestLocation == null) {
                player.sendMessage(Component.text("No structures found within " + (searchRadius * 16) + " blocks.", NamedTextColor.RED));
                return true;
            }
            
            // Set compass target to closest structure
            player.setCompassTarget(closestLocation);
            
            // Store the target for boss bar updates
            CompassTarget target = new CompassTarget(closestStructureType, closestLocation);
            playerTargets.put(player.getUniqueId(), target);
            
            // Display results
            player.sendMessage(Component.text("Found " + formatStructureName(closestStructureType) + "!", NamedTextColor.GREEN));
            player.sendMessage(Component.text("Compass now pointing to " + formatStructureName(closestStructureType) + "!", NamedTextColor.GREEN));
            player.sendMessage(Component.text("Distance: ", NamedTextColor.GREEN)
                .append(Component.text(String.format("%.0f", closestDistance) + " blocks", NamedTextColor.YELLOW)));
            
            // Save the player's target
            savePlayerTarget(player, target);
            
            return true;
        }
        
        // Check if structure type is enabled in config for current world type
        if (!configManager.isStructureEnabled(player.getWorld().getEnvironment(), structureInput.toUpperCase())) {
            player.sendMessage(Component.text("This structure type is not enabled in the current world type.", NamedTextColor.RED));
            return true;
        }
        
        // Get structure from Bukkit's Registry
        org.bukkit.generator.structure.Structure structure = Registry.STRUCTURE.get(NamespacedKey.minecraft(structureInput));
        
        if (structure == null) {
            player.sendMessage(Component.text("Invalid structure type: " + structureInput, NamedTextColor.RED));
            player.sendMessage(Component.text("Use tab completion to see available structures.", NamedTextColor.YELLOW));
            return true;
        }
        
        player.sendMessage(Component.text("Searching for nearest " + formatStructureName(structureInput) + "...", NamedTextColor.YELLOW));
        
        int searchRadius = configManager.getSearchRadius();
        final String finalStructureInput = structureInput.toUpperCase();
        
        // IMPORTANT: Pass the Structure object directly, NOT the StructureType
        // Many structures share the same StructureType (e.g. "jigsaw") 
        // but are different actual structures (ancient_city, trial_chambers, villages, etc.)
        var structureResult = player.getWorld().locateNearestStructure(
            player.getLocation(),
            structure,  
            searchRadius,
            false
        );
        
        // Check if structure was found
        if (structureResult == null) {
            player.sendMessage(Component.text("No " + formatStructureName(finalStructureInput) + 
                             " found within " + (searchRadius * 16) + " blocks.", NamedTextColor.RED));
            return true;
        }
        
        // Get location from result
        Location structureLocation = structureResult.getLocation();
        
        // Set compass target
        player.setCompassTarget(structureLocation);
        
        // Store the target for boss bar updates
        CompassTarget target = new CompassTarget(finalStructureInput, structureLocation);
        playerTargets.put(player.getUniqueId(), target);
        
        // Calculate and display initial distance
        double distance = player.getLocation().distance(structureLocation);
        player.sendMessage(Component.text("Compass now pointing to " + formatStructureName(finalStructureInput) + "!", NamedTextColor.GREEN));
        player.sendMessage(Component.text("Distance: ", NamedTextColor.GREEN)
            .append(Component.text(String.format("%.0f", distance) + " blocks", NamedTextColor.YELLOW)));
        
        // Save the player's target
        savePlayerTarget(player, target);
        
        return true;
    }
    
    /**
     * Provides tab completion for commands
     * 
     * @param sender The command sender
     * @param command The command object
     * @param alias The command alias used
     * @param args Current command arguments
     * @return List of possible completions
     */
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        
        // Tab complete first argument
        if (args.length == 1) {
            // Add subcommands
            completions.add("help");
            completions.add("current");
            
            // Add special generic structure searches
            completions.add("village");
            completions.add("anything");
            
            // Only show reload to console or players with reload permission
            if (!(sender instanceof Player) || sender.hasPermission("enhancedcompass.reload")) {
                completions.add("reload");
            }
            
            if (sender instanceof Player) {
                // Add structure types enabled for player's current world
                Player player = (Player) sender;
                List<String> enabledStructures = configManager.getEnabledStructuresForEnvironment(player.getWorld().getEnvironment());
                completions.addAll(enabledStructures.stream().map(String::toLowerCase).collect(Collectors.toList()));
            } else {
                // Console - show all available structure types from registry
                Registry.STRUCTURE.forEach(structure -> completions.add(structure.getKey().getKey()));
            }
            
            // Filter completions based on what user has typed so far
            return completions.stream()
                .filter(s -> s.toLowerCase().startsWith(args[0].toLowerCase()))
                .sorted()
                .collect(Collectors.toList());
        }
        
        return completions;
    }
    
    /**
     * Called when a player disconnects
     * Cleans up their boss bar
     * 
     * @param event The player quit event
     */
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        
        // Remove boss bar
        removeBossBar(player);
        
        // Save their target before they leave
        CompassTarget target = playerTargets.get(player.getUniqueId());
        if (target != null) {
            savePlayerTarget(player, target);
        }
    }
    
    /**
     * Saves a player's compass target to a file
     * 
     * @param player The player whose target to save
     * @param target The compass target to save
     */
    private void savePlayerTarget(Player player, CompassTarget target) {
        if (target == null || target.location == null) {
            return;
        }
        
        File playerFile = new File(playerDataFolder, player.getUniqueId().toString() + ".yml");
        org.bukkit.configuration.file.YamlConfiguration config = new org.bukkit.configuration.file.YamlConfiguration();
        
        config.set("structure-type", target.structureType);
        config.set("world", target.location.getWorld().getName());
        config.set("x", target.location.getX());
        config.set("y", target.location.getY());
        config.set("z", target.location.getZ());
        
        try {
            config.save(playerFile);
        } catch (Exception e) {
            getLogger().warning("Failed to save compass target for player " + player.getName() + ": " + e.getMessage());
        }
    }
    
    /**
     * Loads a player's compass target from a file
     * 
     * @param player The player whose target to load
     */
    private void loadPlayerTarget(Player player) {
        File playerFile = new File(playerDataFolder, player.getUniqueId().toString() + ".yml");
        
        if (!playerFile.exists()) {
            return; // No saved target
        }
        
        org.bukkit.configuration.file.YamlConfiguration config = org.bukkit.configuration.file.YamlConfiguration.loadConfiguration(playerFile);
        
        try {
            String structureType = config.getString("structure-type");
            String worldName = config.getString("world");
            double x = config.getDouble("x");
            double y = config.getDouble("y");
            double z = config.getDouble("z");
            
            org.bukkit.World world = Bukkit.getWorld(worldName);
            
            if (world == null) {
                getLogger().warning("Could not load compass target for " + player.getName() + ": world " + worldName + " not found");
                return;
            }
            
            Location location = new Location(world, x, y, z);
            CompassTarget target = new CompassTarget(structureType, location);
            
            playerTargets.put(player.getUniqueId(), target);
            player.setCompassTarget(location);
            
            // Notify player their target was restored
            getServer().getScheduler().runTaskLater(this, () -> {
                player.sendMessage(Component.text("Your compass target has been restored: ", NamedTextColor.GREEN)
                    .append(Component.text(formatStructureName(structureType), NamedTextColor.AQUA)));
            }, 20L); // Wait 1 second after join
            
        } catch (Exception e) {
            getLogger().warning("Failed to load compass target for player " + player.getName() + ": " + e.getMessage());
        }
    }
    
    /**
     * Inner class representing a compass target
     * Stores the structure type name and location
     */
    private static class CompassTarget {
        final String structureType;  // Structure type in UPPER_CASE format
        final Location location;      // Location of the structure
        
        CompassTarget(String structureType, Location location) {
            this.structureType = structureType;
            this.location = location;
        }
    }
    
    /**
     * Inner class that manages plugin configuration
     * Handles loading config values and checking enabled structures
     */
    private static class ConfigManager {
        private final EnhancedCompass plugin;
        private int searchRadius;
        private List<String> blacklistedWorlds;
        private Map<String, Map<String, Boolean>> enabledStructures;
        
        ConfigManager(EnhancedCompass plugin) {
            this.plugin = plugin;
            loadConfig();
        }
        
        /**
         * Loads all configuration values from config.yml
         */
        private void loadConfig() {
            FileConfiguration config = plugin.getConfig();
            
            // Load search radius (in chunks, default 100)
            searchRadius = config.getInt("search-radius", 100);
            
            // Load blacklisted worlds list
            blacklistedWorlds = config.getStringList("blacklisted-worlds");
            
            enabledStructures = new HashMap<>();
            
            // Load Overworld (NORMAL) structures
            Map<String, Boolean> normalStructures = new HashMap<>();
            if (config.contains("enabled-structures.normal")) {
                for (String key : config.getConfigurationSection("enabled-structures.normal").getKeys(false)) {
                    normalStructures.put(key.toUpperCase(), config.getBoolean("enabled-structures.normal." + key));
                }
            }
            enabledStructures.put("NORMAL", normalStructures);
            
            // Load Nether structures
            Map<String, Boolean> netherStructures = new HashMap<>();
            if (config.contains("enabled-structures.nether")) {
                for (String key : config.getConfigurationSection("enabled-structures.nether").getKeys(false)) {
                    netherStructures.put(key.toUpperCase(), config.getBoolean("enabled-structures.nether." + key));
                }
            }
            enabledStructures.put("NETHER", netherStructures);
            
            // Load End structures
            Map<String, Boolean> endStructures = new HashMap<>();
            if (config.contains("enabled-structures.the_end")) {
                for (String key : config.getConfigurationSection("enabled-structures.the_end").getKeys(false)) {
                    endStructures.put(key.toUpperCase(), config.getBoolean("enabled-structures.the_end." + key));
                }
            }
            enabledStructures.put("THE_END", endStructures);
        }
        
        /**
         * Gets the configured search radius in chunks
         * 
         * @return Search radius in chunks
         */
        int getSearchRadius() {
            return searchRadius;
        }
        
        /**
         * Checks if a world is blacklisted
         * 
         * @param worldName The world name to check
         * @return true if world is blacklisted
         */
        boolean isWorldBlacklisted(String worldName) {
            return blacklistedWorlds.contains(worldName);
        }
        
        /**
         * Checks if a structure type is enabled for a specific world environment
         * 
         * @param environment The world environment (NORMAL, NETHER, THE_END)
         * @param structureType The structure type in UPPER_CASE format
         * @return true if structure is enabled
         */
        boolean isStructureEnabled(World.Environment environment, String structureType) {
            String envKey = environment.toString();
            Map<String, Boolean> structures = enabledStructures.get(envKey);
            
            if (structures == null) {
                return false;
            }
            
            return structures.getOrDefault(structureType, false);
        }
        
        /**
         * Gets all enabled structure types for a specific world environment
         * 
         * @param environment The world environment (NORMAL, NETHER, THE_END)
         * @return List of enabled structure types in UPPER_CASE format
         */
        List<String> getEnabledStructuresForEnvironment(World.Environment environment) {
            String envKey = environment.toString();
            Map<String, Boolean> structures = enabledStructures.get(envKey);
            
            List<String> result = new ArrayList<>();
            if (structures != null) {
                for (Map.Entry<String, Boolean> entry : structures.entrySet()) {
                    if (entry.getValue()) {
                        result.add(entry.getKey());
                    }
                }
            }
            
            return result;
        }
    }
}