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
import org.bukkit.block.Biome;
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
 * A sophisticated Minecraft Paper/Spigot plugin that enhances the vanilla compass functionality
 * by allowing players to point their compass to any structure type or biome in the game and see real-time
 * distance updates via a boss bar while holding the compass.
 * 
 * <p><b>Core Features:</b></p>
 * <ul>
 *   <li>Permission-based access control (enhancedcompass.use)</li>
 *   <li>Support for all structure types across all dimensions (Overworld, Nether, End)</li>
 *   <li>Support for all biome types across all dimensions</li>
 *   <li>Generic "village" search - finds closest village of any type (plains, desert, snowy, etc.)</li>
 *   <li>Generic "anything" search - finds closest structure of any enabled type</li>
 *   <li>Configurable structure whitelist per dimension for granular control</li>
 *   <li>Configurable biome whitelist per dimension for granular control</li>
 *   <li>Configurable search radius (in chunks)</li>
 *   <li>World blacklist to disable plugin in specific worlds</li>
 *   <li>Real-time boss bar distance display (updates every 0.5 seconds)</li>
 *   <li>Complete tab completion for all commands and structure types</li>
 *   <li>Complete tab completion for all biome types</li>
 *   <li>Player data persistence - targets saved between sessions</li>
 *   <li>Hot-reload capability for configuration changes</li>
 * </ul>
 * 
 * <p><b>Technical Architecture:</b></p>
 * <ul>
 *   <li>Uses Adventure API for modern text components and boss bars</li>
 *   <li>Uses Bukkit Registry API for accurate structure lookups</li>
 *   <li>Uses Bukkit Registry API for accurate biome lookups</li>
 *   <li>Asynchronous boss bar updates via scheduled tasks</li>
 *   <li>YAML-based player data persistence</li>
 *   <li>Modular design with inner classes for organization</li>
 * </ul>
 * 
 * <p><b>Important Implementation Notes:</b></p>
 * <ul>
 *   <li>Structure lookups use the Structure object directly (not StructureType) to ensure 
 *       accurate results, as many different structures share the same StructureType (e.g., "jigsaw")</li>
 *   <li>Biome lookups use World.locateNearestBiome() for efficient biome searching</li>
 *   <li>Boss bars only appear when players are actively holding a compass (main or off hand)</li>
 *   <li>Distance calculations are 3D Euclidean distance in blocks</li>
 *   <li>Cross-dimension targets show "Not in same dimension" warning in boss bar</li>
 * </ul>
 * 
 * @author SupaFloof Games, LLC
 * @version 1.1.0
 */
public class EnhancedCompass extends JavaPlugin implements CommandExecutor, TabCompleter, Listener {
    
    /**
     * Maps each player's UUID to their current compass target.
     * This stores both the target type name (structure or biome) and the exact location coordinates.
     * Key: Player UUID
     * Value: CompassTarget object containing target type and location
     */
    private Map<UUID, CompassTarget> playerTargets = new HashMap<>();
    
    /**
     * Maps each player's UUID to their active boss bar instance.
     * Boss bars are created when a player holds a compass with a target set,
     * and removed when they stop holding the compass.
     * Key: Player UUID
     * Value: BossBar instance showing structure name and distance
     */
    private Map<UUID, BossBar> playerBossBars = new HashMap<>();
    
    /**
     * Configuration manager instance that handles all config.yml operations.
     * Provides methods to check enabled structures, enabled biomes, search radius, and world blacklists.
     * Initialized in onEnable() and recreated on reload.
     */
    private ConfigManager configManager;
    
    /**
     * Scheduled task that runs every 0.5 seconds (10 ticks) to update all active boss bars.
     * This task checks every online player to see if they're holding a compass,
     * and if so, updates their boss bar with current distance to target.
     * Cancelled in onDisable() to prevent memory leaks.
     */
    private BukkitRunnable updateTask;
    
    /**
     * Directory where individual player data files are stored.
     * Each player gets a UUID.yml file containing their last compass target.
     * Location: plugins/EnhancedCompass/playerdata/
     * File format: UUID.yml containing target-type, target-name, world, x, y, z
     */
    private File playerDataFolder;
    
    /**
     * Plugin initialization method called by Bukkit when the plugin is enabled.
     * This method sets up all necessary components in the following order:
     * 
     * <p><b>Initialization Steps:</b></p>
     * <ol>
     *   <li>Save default config.yml from resources if it doesn't exist</li>
     *   <li>Initialize ConfigManager to load and parse configuration</li>
     *   <li>Create playerdata directory for storing player targets</li>
     *   <li>Register command executor and tab completer for /enhancedcompass</li>
     *   <li>Register event listener for player quit events</li>
     *   <li>Start the repeating boss bar update task</li>
     *   <li>Send colored startup messages to console</li>
     * </ol>
     * 
     * <p><b>Important:</b> This method runs synchronously on the main server thread.
     * All initialization should complete quickly to avoid server startup delays.</p>
     */
    @Override
    public void onEnable() {
        // Extract default config.yml from plugin JAR to plugins/EnhancedCompass/ if not present
        // This ensures server admins have a template to work with
        saveDefaultConfig();
        
        // Initialize the configuration manager which will parse and validate config.yml
        // ConfigManager handles structure whitelists, biome whitelists, search radius, and world blacklists
        configManager = new ConfigManager(this);
        
        // Create the playerdata directory if it doesn't exist
        // Individual player compass targets will be saved here as UUID.yml files
        playerDataFolder = new File(getDataFolder(), "playerdata");
        if (!playerDataFolder.exists()) {
            playerDataFolder.mkdirs();
        }
        
        // Register this class as the handler for /enhancedcompass command
        // The command is defined in plugin.yml
        getCommand("enhancedcompass").setExecutor(this);
        
        // Register this class as the tab completer for /enhancedcompass
        // Provides auto-completion for structure names, biome names, and subcommands
        getCommand("enhancedcompass").setTabCompleter(this);
        
        // Register this class as an event listener
        // Currently only listens for PlayerQuitEvent to cleanup boss bars
        getServer().getPluginManager().registerEvents(this, this);
        
        // Start the repeating task that updates boss bars for players holding compasses
        // This task runs every 10 ticks (0.5 seconds) indefinitely
        startUpdateTask();
        
        // Send startup messages to console with Adventure API colored text
        // Green for main message, light purple for author credit
        getServer().getConsoleSender().sendMessage(Component.text("[EnhancedCompass] EnhancedCompass Started!", NamedTextColor.GREEN));
        getServer().getConsoleSender().sendMessage(Component.text("[EnhancedCompass] By SupaFloof Games, LLC", NamedTextColor.LIGHT_PURPLE));
    }
    
    /**
     * Plugin shutdown method called by Bukkit when the plugin is disabled.
     * This method performs cleanup operations to prevent memory leaks and ensure
     * graceful shutdown.
     * 
     * <p><b>Cleanup Steps:</b></p>
     * <ol>
     *   <li>Cancel the boss bar update task to stop further updates</li>
     *   <li>Hide all active boss bars from players</li>
     *   <li>Clear boss bar and target maps to release memory</li>
     *   <li>Log shutdown message</li>
     * </ol>
     * 
     * <p><b>Important:</b> Player targets are NOT saved here - they're saved immediately
     * when set and when players quit. This is intentional to prevent data loss from
     * unexpected shutdowns or crashes.</p>
     */
    @Override
    public void onDisable() {
        // Stop the repeating task that updates boss bars
        // Check if task exists and hasn't already been cancelled to avoid NullPointerException
        if (updateTask != null && !updateTask.isCancelled()) {
            updateTask.cancel();
        }
        
        // Iterate through all active boss bars and hide them from players
        // This prevents boss bars from lingering on the client after plugin disable
        for (Map.Entry<UUID, BossBar> entry : playerBossBars.entrySet()) {
            Player player = Bukkit.getPlayer(entry.getKey());
            // Only attempt to hide if player is still online
            if (player != null) {
                player.hideBossBar(entry.getValue());
            }
        }
        
        // Clear the boss bar map to release references
        // This allows garbage collection of BossBar objects
        playerBossBars.clear();
        
        // Clear the player targets map to release references
        // Note: Targets are already saved to disk, so this is safe
        playerTargets.clear();
        
        // Log shutdown message using standard Java logging (not Adventure API)
        getLogger().info("EnhancedCompass has been disabled!");
    }
    
    /**
     * Starts a repeating asynchronous task that updates boss bars for all online players.
     * This task is the core of the real-time distance display feature.
     * 
     * <p><b>Update Logic:</b></p>
     * <ol>
     *   <li>Iterate through all online players</li>
     *   <li>Check if player is holding a compass in main hand OR off hand</li>
     *   <li>If holding compass AND has target set AND has permission: update boss bar</li>
     *   <li>If not holding compass: remove boss bar if it exists</li>
     * </ol>
     * 
     * <p><b>Performance Considerations:</b></p>
     * <ul>
     *   <li>Runs every 10 ticks (0.5 seconds) - balance between responsiveness and performance</li>
     *   <li>Only calculates distance for players actively holding compass</li>
     *   <li>Uses 3D Euclidean distance calculation via Location.distance()</li>
     *   <li>Permission check included to prevent unauthorized players from seeing boss bars</li>
     * </ul>
     * 
     * <p><b>Boss Bar Behavior:</b></p>
     * <ul>
     *   <li>Boss bar appears immediately when compass is held (if target exists)</li>
     *   <li>Boss bar disappears immediately when compass is put away</li>
     *   <li>Boss bar persists when switching between main and off hand</li>
     *   <li>Distance updates continuously while held</li>
     * </ul>
     */
    private void startUpdateTask() {
        // Create a new BukkitRunnable (repeating task)
        updateTask = new BukkitRunnable() {
            @Override
            public void run() {
                // Iterate through all players currently online on the server
                for (Player player : Bukkit.getOnlinePlayers()) {
                    // Check if player is holding a compass in EITHER hand
                    // This includes both main hand and off hand to support dual-wielding
                    boolean holdingCompass = player.getInventory().getItemInMainHand().getType() == Material.COMPASS ||
                                            player.getInventory().getItemInOffHand().getType() == Material.COMPASS;
                    
                    // Only show boss bar if player is holding compass
                    if (holdingCompass) {
                        // Retrieve the player's current compass target (if any)
                        CompassTarget target = playerTargets.get(player.getUniqueId());
                        
                        // Update boss bar only if:
                        // 1. Player has a target set (target != null)
                        // 2. Player has permission to use enhanced compass features
                        if (target != null && player.hasPermission("enhancedcompass.use")) {
                            // Update or create boss bar with current distance
                            updateBossBar(player, target);
                        }
                    } else {
                        // Player is not holding compass, remove their boss bar if it exists
                        // This ensures boss bar disappears immediately when compass is put away
                        removeBossBar(player);
                    }
                }
            }
        };
        
        // Schedule the task to run repeatedly
        // Parameters: delay (0 ticks = start immediately), period (10 ticks = 0.5 seconds)
        // 20 ticks = 1 second in Minecraft, so 10 ticks = 0.5 seconds
        updateTask.runTaskTimer(this, 0L, 10L);
    }
    
    /**
     * Updates or creates a boss bar for a player showing their compass target and distance.
     * This method is called by the update task for each player holding a compass with a target.
     * 
     * <p><b>Boss Bar Content:</b></p>
     * <ul>
     *   <li>Structure name in aqua/red color (aqua if same dimension, red if different)</li>
     *   <li>Gray dash separator</li>
     *   <li>Distance in blocks (yellow) OR "Not in same dimension" message (red)</li>
     * </ul>
     * 
     * <p><b>Color Coding:</b></p>
     * <ul>
     *   <li>Aqua structure name = target is in same dimension</li>
     *   <li>Red structure name = target is in different dimension</li>
     *   <li>Yellow distance = normal distance display</li>
     *   <li>Red message = cross-dimension warning</li>
     * </ul>
     * 
     * <p><b>Boss Bar Behavior:</b></p>
     * <ul>
     *   <li>Progress bar always at 100% (full) - only used as a distance display</li>
     *   <li>Blue color scheme for consistency</li>
     *   <li>PROGRESS overlay style (standard bar)</li>
     * </ul>
     * 
     * @param player The player to show/update the boss bar for
     * @param target The compass target containing target type/name and location
     */
    private void updateBossBar(Player player, CompassTarget target) {
        // Attempt to retrieve existing boss bar for this player
        BossBar bossBar = playerBossBars.get(player.getUniqueId());
        
        // Create a new boss bar if player doesn't have one yet
        if (bossBar == null) {
            // Create boss bar with:
            // - Empty initial text (will be set below)
            // - 100% progress (1.0f)
            // - Blue color
            // - PROGRESS overlay (standard bar, not NOTCHED variants)
            bossBar = BossBar.bossBar(Component.empty(), 1.0f, BossBar.Color.BLUE, BossBar.Overlay.PROGRESS);
            
            // Show the boss bar to the player
            player.showBossBar(bossBar);
            
            // Store boss bar reference for future updates
            playerBossBars.put(player.getUniqueId(), bossBar);
        }
        
        // Check if target location exists and is in the same world as the player
        // Target location should never be null, but check anyway for safety
        if (target.location != null && player.getWorld().equals(target.location.getWorld())) {
            // Same dimension - show distance
            
            // Calculate 3D Euclidean distance in blocks
            // Uses Bukkit's Location.distance() which calculates: sqrt((x2-x1)² + (y2-y1)² + (z2-z1)²)
            double distance = player.getLocation().distance(target.location);
            
            // Format target name from UPPER_CASE to Title Case
            String targetName = formatStructureName(target.structureType);
            
            // Build boss bar title with colored components:
            // [Aqua Target Name] - [Yellow distance blocks]
            Component title = Component.text(targetName, NamedTextColor.AQUA)
                .append(Component.text(" - ", NamedTextColor.GRAY))
                .append(Component.text(String.format("%.0f", distance) + " blocks", NamedTextColor.YELLOW));
            
            // Update boss bar with new title
            bossBar.name(title);
            
            // Ensure progress is at 100% (full bar)
            bossBar.progress(1.0f);
        } else {
            // Different dimension - show warning message
            
            // Format target name from UPPER_CASE to Title Case
            String targetName = formatStructureName(target.structureType);
            
            // Build boss bar title with red colors to indicate issue:
            // [Red Target Name] - [Red "Not in same dimension"]
            Component title = Component.text(targetName, NamedTextColor.RED)
                .append(Component.text(" - ", NamedTextColor.GRAY))
                .append(Component.text("Not in same dimension", NamedTextColor.RED));
            
            // Update boss bar with warning message
            bossBar.name(title);
            
            // Ensure progress is at 100% (full bar)
            bossBar.progress(1.0f);
        }
    }
    
    /**
     * Removes and hides a player's boss bar if it exists.
     * This method is called when a player stops holding a compass or quits the server.
     * 
     * <p><b>Cleanup Process:</b></p>
     * <ol>
     *   <li>Remove boss bar from internal map (returns null if not present)</li>
     *   <li>If boss bar existed, hide it from the player</li>
     *   <li>Boss bar is garbage collected after references are removed</li>
     * </ol>
     * 
     * <p><b>Safety Considerations:</b></p>
     * <ul>
     *   <li>Safe to call even if player has no boss bar (null check prevents errors)</li>
     *   <li>Called frequently by update task, so must be efficient</li>
     *   <li>Uses Map.remove() which returns null if key not present</li>
     * </ul>
     * 
     * @param player The player whose boss bar to remove
     */
    private void removeBossBar(Player player) {
        // Remove boss bar from map (returns the boss bar if it existed, null otherwise)
        BossBar bossBar = playerBossBars.remove(player.getUniqueId());
        
        // Only hide if boss bar actually existed
        if (bossBar != null) {
            // Hide the boss bar from the player's screen
            // This removes it from the client but doesn't prevent future boss bars
            player.hideBossBar(bossBar);
        }
    }
    
    /**
     * Formats a structure or biome type name for display to players.
     * Converts internal UPPER_CASE_WITH_UNDERSCORES format to readable Title Case format.
     * 
     * <p><b>Conversion Examples:</b></p>
     * <ul>
     *   <li>ANCIENT_CITY → "Ancient City"</li>
     *   <li>VILLAGE_PLAINS → "Village Plains"</li>
     *   <li>STRONGHOLD → "Stronghold"</li>
     *   <li>END_CITY → "End City"</li>
     *   <li>DARK_FOREST → "Dark Forest"</li>
     *   <li>CHERRY_GROVE → "Cherry Grove"</li>
     * </ul>
     * 
     * <p><b>Algorithm:</b></p>
     * <ol>
     *   <li>Split string on underscores</li>
     *   <li>For each word: capitalize first letter, lowercase remaining letters</li>
     *   <li>Join words with spaces</li>
     * </ol>
     * 
     * <p><b>Performance:</b> This method is called frequently (every boss bar update),
     * but String operations are relatively fast for short names.</p>
     * 
     * @param structureType The structure or biome type in UPPER_CASE format (e.g., "ANCIENT_CITY", "DARK_FOREST")
     * @return Formatted name with proper capitalization (e.g., "Ancient City", "Dark Forest")
     */
    private String formatStructureName(String structureType) {
        // Split on underscores to get individual words
        String[] words = structureType.split("_");
        
        // StringBuilder for efficient string concatenation
        StringBuilder result = new StringBuilder();
        
        // Process each word
        for (String word : words) {
            // Add space before word if not first word
            if (result.length() > 0) {
                result.append(" ");
            }
            
            // Capitalize first letter, lowercase the rest
            // substring(0, 1) = first character
            // substring(1) = rest of string
            result.append(word.substring(0, 1).toUpperCase()).append(word.substring(1).toLowerCase());
        }
        
        return result.toString();
    }
    
    /**
     * Sends formatted help message to a command sender.
     * Help message includes all available commands with descriptions.
     * 
     * <p><b>Displayed Commands:</b></p>
     * <ul>
     *   <li>/enhancedcompass help - Show help menu</li>
     *   <li>/enhancedcompass &lt;structure&gt; - Point compass to structure</li>
     *   <li>/enhancedcompass biome &lt;biome&gt; - Point compass to biome</li>
     *   <li>/enhancedcompass current - Show current target</li>
     *   <li>/enhancedcompass reload - Reload config (only shown to authorized users)</li>
     * </ul>
     * 
     * <p><b>Formatting:</b></p>
     * <ul>
     *   <li>Gold colored borders and header</li>
     *   <li>Bold header text</li>
     *   <li>Yellow command syntax</li>
     *   <li>Gray descriptions</li>
     * </ul>
     * 
     * <p><b>Permission-Based Display:</b></p>
     * The reload command is only shown to console or players with enhancedcompass.reload permission.
     * This prevents confusion for regular players who can't use the command.
     * 
     * @param sender The command sender (player or console) to send help to
     */
    private void sendHelp(CommandSender sender) {
        // Top border - decorative line of equals signs in gold
        sender.sendMessage(Component.text("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━", NamedTextColor.GOLD));
        
        // Header - bold gold text
        sender.sendMessage(Component.text("Enhanced Compass Commands", NamedTextColor.GOLD, TextDecoration.BOLD));
        
        // Bottom border - matches top border
        sender.sendMessage(Component.text("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━", NamedTextColor.GOLD));
        
        // Help command - shows this menu
        sender.sendMessage(Component.text("/enhancedcompass help", NamedTextColor.YELLOW)
            .append(Component.text(" - Show this help menu", NamedTextColor.GRAY)));
        
        // Structure search command - main functionality
        sender.sendMessage(Component.text("/enhancedcompass <structure>", NamedTextColor.YELLOW)
            .append(Component.text(" - Point compass to nearest structure", NamedTextColor.GRAY)));
        
        // Biome search command - biome location functionality
        sender.sendMessage(Component.text("/enhancedcompass biome <biome>", NamedTextColor.YELLOW)
            .append(Component.text(" - Point compass to nearest biome", NamedTextColor.GRAY)));
        
        // Current command - shows active target
        sender.sendMessage(Component.text("/enhancedcompass current", NamedTextColor.YELLOW)
            .append(Component.text(" - Show current compass target", NamedTextColor.GRAY)));
        
        // Reload command - only show to console or players with reload permission
        // instanceof check: console is CommandSender but not Player
        // Permission check: only show if sender has enhancedcompass.reload
        if (!(sender instanceof Player) || sender.hasPermission("enhancedcompass.reload")) {
            sender.sendMessage(Component.text("/enhancedcompass reload", NamedTextColor.YELLOW)
                .append(Component.text(" - Reload configuration", NamedTextColor.GRAY)));
        }
        
        // Bottom border - completes the help box
        sender.sendMessage(Component.text("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━", NamedTextColor.GOLD));
    }
    
    /**
     * Handles all /enhancedcompass commands and subcommands.
     * This is the main command handler that routes to appropriate functionality.
     * 
     * <p><b>Supported Commands:</b></p>
     * <ul>
     *   <li>/enhancedcompass reload - Reloads configuration (console or permission required)</li>
     *   <li>/enhancedcompass help - Shows help menu</li>
     *   <li>/enhancedcompass current - Shows current compass target</li>
     *   <li>/enhancedcompass village - Finds nearest village of any type</li>
     *   <li>/enhancedcompass anything - Finds nearest structure of any enabled type</li>
     *   <li>/enhancedcompass biome &lt;biome&gt; - Finds nearest specific biome</li>
     *   <li>/enhancedcompass &lt;structure&gt; - Finds nearest specific structure</li>
     * </ul>
     * 
     * <p><b>Command Flow:</b></p>
     * <ol>
     *   <li>Check for reload command (works from console, permission-based for players)</li>
     *   <li>Check for help command</li>
     *   <li>Verify sender is a player (other commands require player)</li>
     *   <li>Verify player has enhancedcompass.use permission</li>
     *   <li>Verify world is not blacklisted</li>
     *   <li>Show help if no arguments provided</li>
     *   <li>Handle current/village/anything/biome/structure commands</li>
     * </ol>
     * 
     * <p><b>Structure Search Process:</b></p>
     * <ol>
     *   <li>Validate structure type is enabled for current dimension</li>
     *   <li>Retrieve structure from Bukkit Registry</li>
     *   <li>Use World.locateNearestStructure() with configured radius</li>
     *   <li>Set vanilla compass target to structure location</li>
     *   <li>Store CompassTarget for boss bar updates</li>
     *   <li>Save target to player data file</li>
     *   <li>Display success message with distance</li>
     * </ol>
     * 
     * <p><b>Error Handling:</b></p>
     * <ul>
     *   <li>Permission denied - red error message</li>
     *   <li>Blacklisted world - red error message</li>
     *   <li>Disabled structure - red error message</li>
     *   <li>Invalid structure - red error with suggestion to use tab complete</li>
     *   <li>Structure not found - red error with search radius</li>
     * </ul>
     * 
     * @param sender The command sender (player or console)
     * @param command The command object (always "enhancedcompass")
     * @param label The command alias used (typically "enhancedcompass")
     * @param args Command arguments (subcommand and parameters)
     * @return true if command was handled (always true - we handle all cases)
     */
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // RELOAD COMMAND - works from console or with permission
        // This command reloads config.yml and recreates the ConfigManager
        if (args.length > 0 && args[0].equalsIgnoreCase("reload")) {
            // Permission check only for players (console always has permission)
            if (sender instanceof Player && !sender.hasPermission("enhancedcompass.reload")) {
                sender.sendMessage(Component.text("You don't have permission to reload the configuration.", NamedTextColor.RED));
                return true;
            }
            
            // Reload config.yml from disk
            // This re-reads the file but doesn't recreate the ConfigManager
            reloadConfig();
            
            // Recreate ConfigManager to parse the newly loaded config
            // This updates all cached values (search radius, enabled structures, etc.)
            configManager = new ConfigManager(this);
            
            // Confirm reload success
            sender.sendMessage(Component.text("EnhancedCompass configuration reloaded!", NamedTextColor.GREEN));
            return true;
        }
        
        // HELP COMMAND - works for anyone
        // Shows formatted help menu with all available commands
        if (args.length > 0 && args[0].equalsIgnoreCase("help")) {
            sendHelp(sender);
            return true;
        }
        
        // ALL OTHER COMMANDS REQUIRE A PLAYER
        // Console can't have compass targets or hold items
        if (!(sender instanceof Player)) {
            sender.sendMessage(Component.text("This command can only be used by players (except /enhancedcompass reload).", NamedTextColor.RED));
            return true;
        }
        
        // Cast to Player for remaining commands
        Player player = (Player) sender;
        
        // PERMISSION CHECK - player must have enhancedcompass.use permission
        // This is the main permission gate for all compass functionality
        if (!player.hasPermission("enhancedcompass.use")) {
            player.sendMessage(Component.text("You don't have permission to use enhanced compass features.", NamedTextColor.RED));
            return true;
        }
        
        // WORLD BLACKLIST CHECK - plugin can be disabled in specific worlds
        // Useful for lobby worlds, minigame arenas, etc.
        if (configManager.isWorldBlacklisted(player.getWorld().getName())) {
            player.sendMessage(Component.text("Enhanced compass is disabled in this world.", NamedTextColor.RED));
            return true;
        }
        
        // NO ARGUMENTS - show help menu
        if (args.length == 0) {
            sendHelp(player);
            return true;
        }
        
        // CURRENT COMMAND - shows player's current compass target and distance
        // Useful for checking what structure you're heading towards
        if (args[0].equalsIgnoreCase("current")) {
            // Retrieve player's current target (if any)
            CompassTarget target = playerTargets.get(player.getUniqueId());
            
            // Check if player has a target set
            if (target == null) {
                player.sendMessage(Component.text("You don't have a compass target set.", NamedTextColor.YELLOW));
                return true;
            }
            
            // Format structure name for display
            String structureName = formatStructureName(target.structureType);
            
            // Check if target is in same dimension as player
            if (target.location != null && player.getWorld().equals(target.location.getWorld())) {
                // Same dimension - calculate and show distance
                double distance = player.getLocation().distance(target.location);
                
                // Send structure name in aqua
                player.sendMessage(Component.text("Current target: ", NamedTextColor.GREEN)
                    .append(Component.text(structureName, NamedTextColor.AQUA)));
                
                // Send distance in yellow
                player.sendMessage(Component.text("Distance: ", NamedTextColor.GREEN)
                    .append(Component.text(String.format("%.0f", distance) + " blocks", NamedTextColor.YELLOW)));
            } else {
                // Different dimension - show warning
                player.sendMessage(Component.text("Current target: ", NamedTextColor.GREEN)
                    .append(Component.text(structureName, NamedTextColor.AQUA)));
                player.sendMessage(Component.text("Target is in a different dimension.", NamedTextColor.RED));
            }
            
            return true;
        }
        
        // Convert structure input to lowercase for case-insensitive matching
        String structureInput = args[0].toLowerCase();
        
        // BIOME COMMAND - searches for a specific biome type
        // Usage: /enhancedcompass biome <biome_name>
        if (structureInput.equals("biome")) {
            // Check if biome name was provided
            if (args.length < 2) {
                player.sendMessage(Component.text("Usage: /enhancedcompass biome <biome_name>", NamedTextColor.RED));
                player.sendMessage(Component.text("Use tab completion to see available biomes.", NamedTextColor.YELLOW));
                return true;
            }
            
            // Get biome input from second argument
            String biomeInput = args[1].toLowerCase();
            
            // Check if biome type is enabled in config for current world type
            // Prevents searching for biomes that are disabled or don't exist in this dimension
            if (!configManager.isBiomeEnabled(player.getWorld().getEnvironment(), biomeInput.toUpperCase())) {
                player.sendMessage(Component.text("This biome type is not enabled in the current world type.", NamedTextColor.RED));
                return true;
            }
            
            // Get biome from Bukkit's Registry using Minecraft namespaced key
            // This uses the Biome object directly for accurate lookups
            Biome biome = Registry.BIOME.get(NamespacedKey.minecraft(biomeInput));
            
            // Validate that biome exists in the registry
            if (biome == null) {
                player.sendMessage(Component.text("Invalid biome type: " + biomeInput, NamedTextColor.RED));
                player.sendMessage(Component.text("Use tab completion to see available biomes.", NamedTextColor.YELLOW));
                return true;
            }
            
            // Notify player that search is starting (can take a moment for large search radius)
            player.sendMessage(Component.text("Searching for nearest " + formatStructureName(biomeInput) + " biome...", NamedTextColor.YELLOW));
            
            // Get configured search radius from config (in chunks, not blocks)
            int searchRadius = configManager.getSearchRadius();
            
            // Store final variables for use in async callback
            final String finalBiomeInput = biomeInput.toUpperCase();
            final World world = player.getWorld();
            final Location playerLoc = player.getLocation().clone();
            final UUID playerUUID = player.getUniqueId();
            
            // Run biome search asynchronously to prevent server lag
            // Biome searches are computationally expensive as they require noise calculations
            // Using BukkitRunnable to run the search off the main thread
            new BukkitRunnable() {
                @Override
                public void run() {
                    // Search for the nearest biome using Bukkit's biome location API
                    // Parameters:
                    //   - origin: Starting location for search (player's saved location)
                    //   - biome: The specific Biome object to search for
                    //   - radius: Search radius in BLOCKS (converted from chunks)
                    //   - step: Search step size of 32 blocks (matches vanilla /locate biome command)
                    //           Vanilla uses 32 horizontal / 64 vertical resolution
                    //           Using 32 provides good balance of speed and accuracy
                    //
                    // Returns: Location of nearest biome, or null if not found within radius
                    Location biomeResult = world.locateNearestBiome(
                        playerLoc,
                        biome,
                        searchRadius * 16,  // Convert chunks to blocks
                        32  // Step size of 32 blocks - matches vanilla /locate biome resolution
                    );
                    
                    // Switch back to main thread to update player state and send messages
                    // Bukkit API calls must be made on the main thread
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            // Verify player is still online before sending messages
                            Player onlinePlayer = Bukkit.getPlayer(playerUUID);
                            if (onlinePlayer == null) {
                                return;  // Player disconnected during search
                            }
                            
                            // Check if biome was found within search radius
                            if (biomeResult == null) {
                                // Not found - inform player with search radius in blocks
                                onlinePlayer.sendMessage(Component.text("No " + formatStructureName(finalBiomeInput) + 
                                                 " biome found within " + (searchRadius * 16) + " blocks.", NamedTextColor.RED));
                                return;
                            }
                            
                            // Set vanilla compass target to the biome location
                            // This makes the compass needle point towards the biome
                            onlinePlayer.setCompassTarget(biomeResult);
                            
                            // Store the target for boss bar updates
                            // This allows the update task to show real-time distance in boss bar
                            CompassTarget target = new CompassTarget(finalBiomeInput, biomeResult);
                            playerTargets.put(playerUUID, target);
                            
                            // Calculate initial distance for display
                            // This is a 3D Euclidean distance in blocks
                            double distance = onlinePlayer.getLocation().distance(biomeResult);
                            
                            // Send success message with biome name in formatted Title Case
                            onlinePlayer.sendMessage(Component.text("Compass now pointing to " + formatStructureName(finalBiomeInput) + " biome!", NamedTextColor.GREEN));
                            
                            // Send distance message with formatted block count
                            // String.format("%.0f", distance) rounds to nearest whole number
                            onlinePlayer.sendMessage(Component.text("Distance: ", NamedTextColor.GREEN)
                                .append(Component.text(String.format("%.0f", distance) + " blocks", NamedTextColor.YELLOW)));
                            
                            // Save the player's target to disk for persistence across server restarts
                            savePlayerTarget(onlinePlayer, target);
                        }
                    }.runTask(EnhancedCompass.this);  // Run on main thread
                }
            }.runTaskAsynchronously(EnhancedCompass.this);  // Run search off main thread
            
            return true;
        }
        
        // VILLAGE COMMAND - special handler that searches ALL village types
        // This is more convenient than typing a specific village type
        // Searches: village_plains, village_desert, village_savanna, village_snowy, village_taiga
        if (structureInput.equals("village")) {
            // Notify player that search is starting
            player.sendMessage(Component.text("Searching for nearest village of any type...", NamedTextColor.YELLOW));
            
            // Get configured search radius from config
            int searchRadius = configManager.getSearchRadius();
            
            // Array of all village structure types in Minecraft
            String[] villageTypes = {
                "village_plains",
                "village_desert", 
                "village_savanna",
                "village_snowy",
                "village_taiga"
            };
            
            // Variables to track the closest village found
            Location closestLocation = null;
            String closestVillageType = null;
            double closestDistance = Double.MAX_VALUE;
            
            // Search for each village type and find the closest one
            for (String villageType : villageTypes) {
                // Check if this village type is enabled in config for this dimension
                // Skip disabled village types
                if (!configManager.isStructureEnabled(player.getWorld().getEnvironment(), villageType.toUpperCase())) {
                    continue;
                }
                
                // Get structure from Bukkit Registry
                // NamespacedKey.minecraft() creates key like "minecraft:village_plains"
                org.bukkit.generator.structure.Structure structure = Registry.STRUCTURE.get(NamespacedKey.minecraft(villageType));
                
                // Skip if structure doesn't exist in registry (shouldn't happen for villages)
                if (structure == null) {
                    continue;
                }
                
                // Use Bukkit's structure location API
                // Parameters: origin location, structure type, search radius in chunks, find unexplored
                var structureResult = player.getWorld().locateNearestStructure(
                    player.getLocation(),
                    structure,
                    searchRadius,
                    false  // false = can return already found structures
                );
                
                // If this village type was found, check if it's closest
                if (structureResult != null) {
                    Location loc = structureResult.getLocation();
                    double distance = player.getLocation().distance(loc);
                    
                    // Update closest if this is nearer
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
            
            // Set vanilla compass target to the village location
            // This makes the compass needle point to the structure
            player.setCompassTarget(closestLocation);
            
            // Store the target for boss bar updates
            // This allows the boss bar system to show real-time distance
            CompassTarget target = new CompassTarget(closestVillageType, closestLocation);
            playerTargets.put(player.getUniqueId(), target);
            
            // Display success messages with structure name and distance
            player.sendMessage(Component.text("Found " + formatStructureName(closestVillageType) + "!", NamedTextColor.GREEN));
            player.sendMessage(Component.text("Compass now pointing to " + formatStructureName(closestVillageType) + "!", NamedTextColor.GREEN));
            player.sendMessage(Component.text("Distance: ", NamedTextColor.GREEN)
                .append(Component.text(String.format("%.0f", closestDistance) + " blocks", NamedTextColor.YELLOW)));
            
            // Save the player's target to disk for persistence
            savePlayerTarget(player, target);
            
            return true;
        }
        
        // ANYTHING COMMAND - special handler that searches ALL enabled structure types
        // This finds the absolute closest structure of any type that's enabled
        // Useful for exploration or when you don't care what structure you find
        if (structureInput.equals("anything")) {
            // Notify player that search is starting
            player.sendMessage(Component.text("Searching for nearest structure of any type...", NamedTextColor.YELLOW));
            
            // Get configured search radius from config
            int searchRadius = configManager.getSearchRadius();
            
            // Get current world's dimension type (NORMAL, NETHER, or THE_END)
            World.Environment environment = player.getWorld().getEnvironment();
            
            // Get list of all enabled structures for this dimension from config
            List<String> enabledStructures = configManager.getEnabledStructuresForEnvironment(environment);
            
            // Check if any structures are enabled for this dimension
            if (enabledStructures.isEmpty()) {
                player.sendMessage(Component.text("No structures are enabled in this world type.", NamedTextColor.RED));
                return true;
            }
            
            // Variables to track the closest structure found across all types
            Location closestLocation = null;
            String closestStructureType = null;
            double closestDistance = Double.MAX_VALUE;
            
            // Search for each enabled structure type and find the absolute closest
            for (String structureType : enabledStructures) {
                // Get structure from Bukkit Registry
                // Convert to lowercase for NamespacedKey (e.g., "ANCIENT_CITY" → "ancient_city")
                org.bukkit.generator.structure.Structure structure = Registry.STRUCTURE.get(NamespacedKey.minecraft(structureType.toLowerCase()));
                
                // Skip if structure doesn't exist in registry
                if (structure == null) {
                    continue;
                }
                
                // Search for this structure type
                var structureResult = player.getWorld().locateNearestStructure(
                    player.getLocation(),
                    structure,
                    searchRadius,
                    false  // false = can return already found structures
                );
                
                // If this structure type was found, check if it's closest
                if (structureResult != null) {
                    Location loc = structureResult.getLocation();
                    double distance = player.getLocation().distance(loc);
                    
                    // Update closest if this is nearer than any structure found so far
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
            
            // Set vanilla compass target to the structure location
            player.setCompassTarget(closestLocation);
            
            // Store the target for boss bar updates
            CompassTarget target = new CompassTarget(closestStructureType, closestLocation);
            playerTargets.put(player.getUniqueId(), target);
            
            // Display success messages with structure name and distance
            player.sendMessage(Component.text("Found " + formatStructureName(closestStructureType) + "!", NamedTextColor.GREEN));
            player.sendMessage(Component.text("Compass now pointing to " + formatStructureName(closestStructureType) + "!", NamedTextColor.GREEN));
            player.sendMessage(Component.text("Distance: ", NamedTextColor.GREEN)
                .append(Component.text(String.format("%.0f", closestDistance) + " blocks", NamedTextColor.YELLOW)));
            
            // Save the player's target to disk for persistence
            savePlayerTarget(player, target);
            
            return true;
        }
        
        // SPECIFIC STRUCTURE COMMAND - searches for a single structure type
        // This is the main/default command path for named structures
        
        // Check if structure type is enabled in config for current world type
        // Prevents searching for structures that are disabled or don't exist in this dimension
        if (!configManager.isStructureEnabled(player.getWorld().getEnvironment(), structureInput.toUpperCase())) {
            player.sendMessage(Component.text("This structure type is not enabled in the current world type.", NamedTextColor.RED));
            return true;
        }
        
        // Get structure from Bukkit's Registry using Minecraft namespaced key
        // This uses the Structure object directly, not StructureType
        // This is CRITICAL because many structures share the same StructureType (e.g., "jigsaw")
        // but are different actual structures (ancient_city, trial_chambers, villages, etc.)
        org.bukkit.generator.structure.Structure structure = Registry.STRUCTURE.get(NamespacedKey.minecraft(structureInput));
        
        // Validate that structure exists in the registry
        if (structure == null) {
            player.sendMessage(Component.text("Invalid structure type: " + structureInput, NamedTextColor.RED));
            player.sendMessage(Component.text("Use tab completion to see available structures.", NamedTextColor.YELLOW));
            return true;
        }
        
        // Notify player that search is starting (can take a moment for large search radius)
        player.sendMessage(Component.text("Searching for nearest " + formatStructureName(structureInput) + "...", NamedTextColor.YELLOW));
        
        // Get configured search radius from config (in chunks, not blocks)
        int searchRadius = configManager.getSearchRadius();
        
        // Create final variable for use in lambda/inner classes if needed
        final String finalStructureInput = structureInput.toUpperCase();
        
        // CRITICAL: Pass the Structure object directly, NOT the StructureType
        // Many structures share the same StructureType (e.g., "jigsaw") 
        // but are different actual structures (ancient_city, trial_chambers, villages, etc.)
        // Using the Structure object ensures we search for the correct structure
        //
        // Method: World.locateNearestStructure()
        // Parameters:
        //   - origin: Starting location for search (player's current location)
        //   - structure: The specific Structure object to search for
        //   - radius: Search radius in CHUNKS (not blocks)
        //   - findUnexplored: false = can return previously found structures
        //
        // Returns: StructureSearchResult containing location, or null if not found
        var structureResult = player.getWorld().locateNearestStructure(
            player.getLocation(),
            structure,  
            searchRadius,
            false  // false = can return already found/generated structures
        );
        
        // Check if structure was found within search radius
        if (structureResult == null) {
            // Not found - inform player with search radius in blocks (radius * 16)
            // 1 chunk = 16 blocks, so multiply radius by 16 for block distance
            player.sendMessage(Component.text("No " + formatStructureName(finalStructureInput) + 
                             " found within " + (searchRadius * 16) + " blocks.", NamedTextColor.RED));
            return true;
        }
        
        // Extract location from search result
        Location structureLocation = structureResult.getLocation();
        
        // Set vanilla compass target to the structure location
        // This makes the compass needle point towards the structure
        player.setCompassTarget(structureLocation);
        
        // Store the target for boss bar updates
        // This allows the update task to show real-time distance in boss bar
        CompassTarget target = new CompassTarget(finalStructureInput, structureLocation);
        playerTargets.put(player.getUniqueId(), target);
        
        // Calculate initial distance for display
        // This is a 3D Euclidean distance in blocks
        double distance = player.getLocation().distance(structureLocation);
        
        // Send success message with structure name in formatted Title Case
        player.sendMessage(Component.text("Compass now pointing to " + formatStructureName(finalStructureInput) + "!", NamedTextColor.GREEN));
        
        // Send distance message with formatted block count
        // String.format("%.0f", distance) rounds to nearest whole number
        player.sendMessage(Component.text("Distance: ", NamedTextColor.GREEN)
            .append(Component.text(String.format("%.0f", distance) + " blocks", NamedTextColor.YELLOW)));
        
        // Save the player's target to disk for persistence across server restarts
        savePlayerTarget(player, target);
        
        return true;
    }
    
    /**
     * Provides tab completion suggestions for /enhancedcompass commands.
     * This method is called automatically by Bukkit as players type the command.
     * 
     * <p><b>Tab Completion Behavior:</b></p>
     * <ul>
     *   <li>First argument: subcommands and structure names</li>
     *   <li>Second argument (for biome): biome names</li>
     *   <li>Filters suggestions based on what player has typed</li>
     *   <li>Results are sorted alphabetically</li>
     *   <li>Console gets all types, players get only enabled types for their world</li>
     *   <li>Reload command only shown to authorized users</li>
     * </ul>
     * 
     * <p><b>Subcommands Provided:</b></p>
     * <ul>
     *   <li>help - Always shown</li>
     *   <li>current - Always shown</li>
     *   <li>biome - Always shown (biome search subcommand)</li>
     *   <li>village - Always shown (generic village search)</li>
     *   <li>anything - Always shown (generic structure search)</li>
     *   <li>reload - Only shown to console or players with enhancedcompass.reload</li>
     * </ul>
     * 
     * <p><b>Structure Names:</b></p>
     * <ul>
     *   <li>Players: Only structures enabled for their current world's dimension</li>
     *   <li>Console: All structure types from Bukkit Registry</li>
     *   <li>Lowercase format (e.g., "ancient_city", "village_plains")</li>
     * </ul>
     * 
     * <p><b>Biome Names (when first arg is "biome"):</b></p>
     * <ul>
     *   <li>Players: Only biomes enabled for their current world's dimension</li>
     *   <li>Console: All biome types from Bukkit Registry</li>
     *   <li>Lowercase format (e.g., "dark_forest", "cherry_grove")</li>
     * </ul>
     * 
     * <p><b>Performance Considerations:</b></p>
     * Tab completion is called frequently as players type, so this method needs to be fast.
     * Uses Java streams for efficient filtering and sorting.
     * 
     * @param sender The command sender (player or console)
     * @param command The command object (always "enhancedcompass")
     * @param alias The command alias used (typically "enhancedcompass")
     * @param args Current command arguments (partial or complete)
     * @return List of possible completions matching current input
     */
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        // Create list to hold completion suggestions
        List<String> completions = new ArrayList<>();
        
        // Only provide tab completion for first argument (subcommand or structure name)
        // We don't have multi-argument commands that need further completion
        if (args.length == 1) {
            // Add standard subcommands that are always available
            completions.add("help");      // Shows help menu
            completions.add("current");   // Shows current target
            completions.add("biome");     // Biome search subcommand
            
            // Add special generic structure searches
            completions.add("village");   // Finds any village type
            completions.add("anything");  // Finds any enabled structure
            
            // Add reload command only for authorized users
            // Console (not a Player) or players with enhancedcompass.reload permission
            if (!(sender instanceof Player) || sender.hasPermission("enhancedcompass.reload")) {
                completions.add("reload");
            }
            
            // Add structure type completions based on sender type
            if (sender instanceof Player) {
                // PLAYER: Add only structure types enabled for their current world
                // This prevents suggesting structures that won't work in their dimension
                Player player = (Player) sender;
                
                // Get enabled structures for player's current world environment
                List<String> enabledStructures = configManager.getEnabledStructuresForEnvironment(player.getWorld().getEnvironment());
                
                // Convert to lowercase and add to completions
                // Config stores in UPPER_CASE but commands use lowercase
                completions.addAll(enabledStructures.stream()
                    .map(String::toLowerCase)
                    .collect(Collectors.toList()));
            } else {
                // CONSOLE: Add all available structure types from Bukkit Registry
                // Console doesn't have a specific world, so show everything
                Registry.STRUCTURE.forEach(structure -> {
                    // Get the namespaced key and extract the structure name
                    // e.g., "minecraft:ancient_city" → "ancient_city"
                    completions.add(structure.getKey().getKey());
                });
            }
            
            // Filter completions based on what user has typed so far
            // Example: if they typed "anc", only show completions starting with "anc"
            return completions.stream()
                .filter(s -> s.toLowerCase().startsWith(args[0].toLowerCase()))  // Case-insensitive match
                .sorted()  // Alphabetically sort for easier reading
                .collect(Collectors.toList());  // Collect to list
        }
        
        // SECOND ARGUMENT TAB COMPLETION - only for "biome" subcommand
        // Provides biome name suggestions when first argument is "biome"
        if (args.length == 2 && args[0].equalsIgnoreCase("biome")) {
            // Add biome type completions based on sender type
            if (sender instanceof Player) {
                // PLAYER: Add only biome types enabled for their current world
                // This prevents suggesting biomes that won't work in their dimension
                Player player = (Player) sender;
                
                // Get enabled biomes for player's current world environment
                List<String> enabledBiomes = configManager.getEnabledBiomesForEnvironment(player.getWorld().getEnvironment());
                
                // Convert to lowercase and add to completions
                // Config stores in UPPER_CASE but commands use lowercase
                completions.addAll(enabledBiomes.stream()
                    .map(String::toLowerCase)
                    .collect(Collectors.toList()));
            } else {
                // CONSOLE: Add all available biome types from Bukkit Registry
                // Console doesn't have a specific world, so show everything
                Registry.BIOME.forEach(biome -> {
                    // Get the namespaced key and extract the biome name
                    // e.g., "minecraft:dark_forest" → "dark_forest"
                    completions.add(biome.getKey().getKey());
                });
            }
            
            // Filter completions based on what user has typed so far
            // Example: if they typed "dar", only show completions starting with "dar"
            return completions.stream()
                .filter(s -> s.toLowerCase().startsWith(args[1].toLowerCase()))  // Case-insensitive match
                .sorted()  // Alphabetically sort for easier reading
                .collect(Collectors.toList());  // Collect to list
        }
        
        // Return empty list for arguments beyond the first (or second for biome)
        // We don't have any multi-argument commands that need completion beyond this
        return completions;
    }
    
    /**
     * Event handler called when a player disconnects from the server.
     * Performs cleanup operations to prevent memory leaks and save player data.
     * 
     * <p><b>Cleanup Operations:</b></p>
     * <ol>
     *   <li>Remove and hide player's boss bar if active</li>
     *   <li>Save player's current compass target to disk</li>
     * </ol>
     * 
     * <p><b>Data Persistence:</b></p>
     * Player targets are saved immediately when:
     * <ul>
     *   <li>Target is set (after finding a structure)</li>
     *   <li>Player quits (here)</li>
     * </ul>
     * This ensures targets persist across sessions even if server crashes.
     * 
     * <p><b>Boss Bar Cleanup:</b></p>
     * Boss bars must be explicitly removed when players quit to:
     * <ul>
     *   <li>Free client-side rendering resources</li>
     *   <li>Prevent ghost boss bars on reconnect</li>
     *   <li>Clean up server-side references for garbage collection</li>
     * </ul>
     * 
     * @param event The PlayerQuitEvent containing the disconnecting player
     */
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        // Get the player who is disconnecting
        Player player = event.getPlayer();
        
        // Remove and hide the player's boss bar (if they have one)
        // This prevents memory leaks and ensures clean disconnect
        removeBossBar(player);
        
        // Retrieve the player's current compass target
        CompassTarget target = playerTargets.get(player.getUniqueId());
        
        // Save target to disk if player has one set
        // This ensures target persists for next login
        if (target != null) {
            savePlayerTarget(player, target);
        }
    }
    
    /**
     * Saves a player's compass target to a YAML file in the playerdata directory.
     * Each player gets their own file named UUID.yml containing their target information.
     * 
     * <p><b>File Location:</b></p>
     * plugins/EnhancedCompass/playerdata/[player-uuid].yml
     * 
     * <p><b>File Structure:</b></p>
     * <pre>
     * structure-type: ANCIENT_CITY
     * world: world
     * x: 100.5
     * y: -20.0
     * z: 250.75
     * </pre>
     * 
     * <p><b>Saved Data:</b></p>
     * <ul>
     *   <li>structure-type: Structure type in UPPER_CASE format</li>
     *   <li>world: World name (e.g., "world", "world_nether", "world_the_end")</li>
     *   <li>x: X coordinate (double precision)</li>
     *   <li>y: Y coordinate (double precision)</li>
     *   <li>z: Z coordinate (double precision)</li>
     * </ul>
     * 
     * <p><b>When to Save:</b></p>
     * <ul>
     *   <li>Immediately after setting a new compass target</li>
     *   <li>When player quits the server</li>
     * </ul>
     * 
     * <p><b>Error Handling:</b></p>
     * If save fails, a warning is logged but execution continues normally.
     * This prevents a save error from breaking compass functionality.
     * 
     * @param player The player whose target to save
     * @param target The compass target containing structure type and location
     */
    private void savePlayerTarget(Player player, CompassTarget target) {
        // Validate inputs - don't save if target or location is null
        // This prevents creating invalid save files
        if (target == null || target.location == null) {
            return;
        }
        
        // Create file path: playerdata/[uuid].yml
        File playerFile = new File(playerDataFolder, player.getUniqueId().toString() + ".yml");
        
        // Create new YAML configuration for saving
        // This is a fresh config, not loaded from disk
        org.bukkit.configuration.file.YamlConfiguration config = new org.bukkit.configuration.file.YamlConfiguration();
        
        // Set structure type (e.g., "ANCIENT_CITY")
        config.set("structure-type", target.structureType);
        
        // Set world name (e.g., "world", "world_nether")
        config.set("world", target.location.getWorld().getName());
        
        // Set coordinates with full double precision
        config.set("x", target.location.getX());
        config.set("y", target.location.getY());
        config.set("z", target.location.getZ());
        
        // Attempt to save configuration to file
        try {
            config.save(playerFile);
        } catch (Exception e) {
            // Log warning if save fails, but don't throw exception
            // This prevents a save error from breaking gameplay
            getLogger().warning("Failed to save compass target for player " + player.getName() + ": " + e.getMessage());
        }
    }
    
    /**
     * Loads a player's compass target from their saved data file.
     * Called when a player joins the server to restore their last compass target.
     * 
     * <p><b>Load Process:</b></p>
     * <ol>
     *   <li>Check if player data file exists</li>
     *   <li>Load YAML configuration from file</li>
     *   <li>Extract structure type, world, and coordinates</li>
     *   <li>Validate world still exists (may have been deleted)</li>
     *   <li>Create Location object from coordinates</li>
     *   <li>Create CompassTarget and store in memory</li>
     *   <li>Set vanilla compass target</li>
     *   <li>Notify player after 1 second delay</li>
     * </ol>
     * 
     * <p><b>Error Handling:</b></p>
     * <ul>
     *   <li>No save file: silently return (player has no saved target)</li>
     *   <li>World doesn't exist: log warning and return</li>
     *   <li>Parse error: log warning and return</li>
     * </ul>
     * 
     * <p><b>Player Notification:</b></p>
     * A delayed notification (1 second after join) informs the player their
     * target was restored. Delay prevents message from being lost in join spam.
     * 
     * <p><b>Usage:</b></p>
     * This method should be called from a PlayerJoinEvent handler to restore
     * targets when players log in.
     * 
     * @param player The player whose target to load
     */
    private void loadPlayerTarget(Player player) {
        // Create file path: playerdata/[uuid].yml
        File playerFile = new File(playerDataFolder, player.getUniqueId().toString() + ".yml");
        
        // Check if save file exists
        if (!playerFile.exists()) {
            // No saved target for this player - this is normal for new players
            return;
        }
        
        // Load YAML configuration from file
        org.bukkit.configuration.file.YamlConfiguration config = org.bukkit.configuration.file.YamlConfiguration.loadConfiguration(playerFile);
        
        // Attempt to parse and restore target
        try {
            // Extract structure type (e.g., "ANCIENT_CITY")
            String structureType = config.getString("structure-type");
            
            // Extract world name (e.g., "world")
            String worldName = config.getString("world");
            
            // Extract coordinates with full double precision
            double x = config.getDouble("x");
            double y = config.getDouble("y");
            double z = config.getDouble("z");
            
            // Get world object from Bukkit
            // This may return null if world was deleted/renamed
            org.bukkit.World world = Bukkit.getWorld(worldName);
            
            // Validate world exists
            if (world == null) {
                // World doesn't exist anymore (deleted, renamed, or disabled)
                // Log warning and abort load
                getLogger().warning("Could not load compass target for " + player.getName() + ": world " + worldName + " not found");
                return;
            }
            
            // Create Location object from saved coordinates
            Location location = new Location(world, x, y, z);
            
            // Create CompassTarget object
            CompassTarget target = new CompassTarget(structureType, location);
            
            // Store target in memory for boss bar updates
            playerTargets.put(player.getUniqueId(), target);
            
            // Set vanilla compass target so compass needle points correctly
            player.setCompassTarget(location);
            
            // Schedule delayed notification to player
            // Delay of 20 ticks (1 second) ensures message isn't lost in join spam
            getServer().getScheduler().runTaskLater(this, () -> {
                // Send notification with formatted structure name
                player.sendMessage(Component.text("Your compass target has been restored: ", NamedTextColor.GREEN)
                    .append(Component.text(formatStructureName(structureType), NamedTextColor.AQUA)));
            }, 20L);
            
        } catch (Exception e) {
            // Failed to parse target file (corrupt file, wrong format, etc.)
            // Log warning but don't throw exception - player can set a new target
            getLogger().warning("Failed to load compass target for player " + player.getName() + ": " + e.getMessage());
        }
    }
    
    /**
     * Inner class representing a compass target.
     * Stores both the structure type name and the exact location coordinates.
     * This is a simple immutable data holder (all fields are final).
     * 
     * <p><b>Design Pattern:</b></p>
     * This is a simple Data Transfer Object (DTO) with no business logic.
     * Fields are final to ensure immutability and thread safety.
     * 
     * <p><b>Usage:</b></p>
     * CompassTarget instances are created when:
     * <ul>
     *   <li>Player searches for a structure and one is found</li>
     *   <li>Player's saved target is loaded from disk on join</li>
     * </ul>
     * 
     * CompassTarget instances are stored in:
     * <ul>
     *   <li>playerTargets map (UUID → CompassTarget)</li>
     *   <li>Player data YAML files (persisted to disk)</li>
     * </ul>
     * 
     * <p><b>Thread Safety:</b></p>
     * This class is immutable (all fields final, no setters), making it inherently thread-safe.
     * Multiple threads can safely read the same CompassTarget instance.
     */
    private static class CompassTarget {
        /**
         * The structure type in UPPER_CASE format (e.g., "ANCIENT_CITY", "VILLAGE_PLAINS").
         * This matches Minecraft's structure naming convention.
         * Used for display in boss bars and save files.
         */
        final String structureType;
        
        /**
         * The exact location of the structure in the world.
         * Includes world reference and X, Y, Z coordinates.
         * Used for compass targeting and distance calculations.
         */
        final Location location;
        
        /**
         * Constructor for creating a new compass target.
         * 
         * @param structureType The structure type in UPPER_CASE format
         * @param location The location of the structure
         */
        CompassTarget(String structureType, Location location) {
            this.structureType = structureType;
            this.location = location;
        }
    }
    
    /**
     * Inner class that manages plugin configuration from config.yml.
     * Handles loading, parsing, and validation of all configuration values.
     * 
     * <p><b>Managed Configuration:</b></p>
     * <ul>
     *   <li>search-radius: Search radius in chunks (default: 100)</li>
     *   <li>blacklisted-worlds: List of world names where plugin is disabled</li>
     *   <li>enabled-structures: Per-dimension structure whitelists</li>
     * </ul>
     * 
     * <p><b>Config Structure Example:</b></p>
     * <pre>
     * search-radius: 100
     * blacklisted-worlds:
     *   - lobby
     *   - minigames
     * enabled-structures:
     *   normal:
     *     ancient_city: true
     *     village_plains: true
     *     stronghold: true
     *   nether:
     *     fortress: true
     *     bastion_remnant: true
     *   the_end:
     *     end_city: true
     * </pre>
     * 
     * <p><b>Dimension Mapping:</b></p>
     * <ul>
     *   <li>normal → World.Environment.NORMAL (Overworld)</li>
     *   <li>nether → World.Environment.NETHER</li>
     *   <li>the_end → World.Environment.THE_END</li>
     * </ul>
     * 
     * <p><b>Design Pattern:</b></p>
     * This class uses the Facade pattern to provide a simple interface
     * to complex configuration operations. All config access goes through
     * this class rather than directly accessing FileConfiguration.
     * 
     * <p><b>Lifecycle:</b></p>
     * ConfigManager is created in onEnable() and recreated on config reload.
     * All values are cached in memory for fast access during gameplay.
     * 
     * <p><b>Thread Safety:</b></p>
     * This class is effectively immutable after construction (all maps are
     * populated in loadConfig() and never modified). Safe for concurrent reads.
     */
    private static class ConfigManager {
        /**
         * Reference to main plugin instance.
         * Used to access getConfig() and getLogger() methods.
         */
        private final EnhancedCompass plugin;
        
        /**
         * Search radius in chunks for structure searches.
         * Default: 100 chunks
         * 1 chunk = 16 blocks, so 100 chunks = 1600 blocks
         */
        private int searchRadius;
        
        /**
         * List of world names where the plugin is completely disabled.
         * Example: ["lobby", "minigames", "hub"]
         * Empty list = plugin enabled in all worlds
         */
        private List<String> blacklistedWorlds;
        
        /**
         * Nested map of enabled structures per dimension.
         * Structure: Map<Environment String, Map<Structure Type, Enabled Boolean>>
         * 
         * Example structure:
         * {
         *   "NORMAL": {
         *     "ANCIENT_CITY": true,
         *     "VILLAGE_PLAINS": true,
         *     "SHIPWRECK": false
         *   },
         *   "NETHER": {
         *     "FORTRESS": true,
         *     "BASTION_REMNANT": true
         *   }
         * }
         * 
         * Outer map keys: "NORMAL", "NETHER", "THE_END"
         * Inner map keys: Structure types in UPPER_CASE
         * Inner map values: true = enabled, false = disabled
         */
        private Map<String, Map<String, Boolean>> enabledStructures;
        
        /**
         * Nested map of enabled biomes per dimension.
         * Structure: Map<Environment String, Map<Biome Type, Enabled Boolean>>
         * 
         * Example structure:
         * {
         *   "NORMAL": {
         *     "DARK_FOREST": true,
         *     "CHERRY_GROVE": true,
         *     "DESERT": false
         *   },
         *   "NETHER": {
         *     "CRIMSON_FOREST": true,
         *     "WARPED_FOREST": true
         *   }
         * }
         * 
         * Outer map keys: "NORMAL", "NETHER", "THE_END"
         * Inner map keys: Biome types in UPPER_CASE
         * Inner map values: true = enabled, false = disabled
         */
        private Map<String, Map<String, Boolean>> enabledBiomes;
        
        /**
         * Constructor that initializes the ConfigManager and loads configuration.
         * 
         * @param plugin Reference to main plugin instance
         */
        ConfigManager(EnhancedCompass plugin) {
            this.plugin = plugin;
            loadConfig();
        }
        
        /**
         * Loads all configuration values from config.yml.
         * This method populates all instance variables from the config file.
         * 
         * <p><b>Load Process:</b></p>
         * <ol>
         *   <li>Get FileConfiguration from plugin</li>
         *   <li>Load search-radius (with default)</li>
         *   <li>Load blacklisted-worlds list</li>
         *   <li>Load enabled structures for each dimension</li>
         * </ol>
         * 
         * <p><b>Default Values:</b></p>
         * <ul>
         *   <li>search-radius: 100 (if not set)</li>
         *   <li>blacklisted-worlds: empty list (if not set)</li>
         *   <li>enabled-structures: empty maps (if not set)</li>
         * </ul>
         * 
         * <p><b>Configuration Sections:</b></p>
         * enabled-structures has three subsections:
         * <ul>
         *   <li>normal: Overworld structures</li>
         *   <li>nether: Nether structures</li>
         *   <li>the_end: End structures</li>
         * </ul>
         * 
         * <p><b>Case Handling:</b></p>
         * Structure names in config can be lowercase (ancient_city),
         * but are stored internally as UPPER_CASE (ANCIENT_CITY) for consistency.
         */
        private void loadConfig() {
            // Get the plugin's FileConfiguration
            FileConfiguration config = plugin.getConfig();
            
            // Load search radius with default of 100 chunks
            // If not present in config, uses default value
            searchRadius = config.getInt("search-radius", 100);
            
            // Load blacklisted worlds list
            // Returns empty list if not present in config
            blacklistedWorlds = config.getStringList("blacklisted-worlds");
            
            // Initialize enabled structures map
            enabledStructures = new HashMap<>();
            
            // LOAD OVERWORLD (NORMAL) STRUCTURES
            // Create map for normal dimension structures
            Map<String, Boolean> normalStructures = new HashMap<>();
            
            // Check if config section exists
            if (config.contains("enabled-structures.normal")) {
                // Iterate through all keys in normal section
                for (String key : config.getConfigurationSection("enabled-structures.normal").getKeys(false)) {
                    // Store as UPPER_CASE for consistency
                    // Value is boolean (true = enabled, false = disabled)
                    normalStructures.put(key.toUpperCase(), config.getBoolean("enabled-structures.normal." + key));
                }
            }
            // Store normal structures map with "NORMAL" key
            enabledStructures.put("NORMAL", normalStructures);
            
            // LOAD NETHER STRUCTURES
            // Create map for nether dimension structures
            Map<String, Boolean> netherStructures = new HashMap<>();
            
            // Check if config section exists
            if (config.contains("enabled-structures.nether")) {
                // Iterate through all keys in nether section
                for (String key : config.getConfigurationSection("enabled-structures.nether").getKeys(false)) {
                    // Store as UPPER_CASE for consistency
                    netherStructures.put(key.toUpperCase(), config.getBoolean("enabled-structures.nether." + key));
                }
            }
            // Store nether structures map with "NETHER" key
            enabledStructures.put("NETHER", netherStructures);
            
            // LOAD END STRUCTURES
            // Create map for end dimension structures
            Map<String, Boolean> endStructures = new HashMap<>();
            
            // Check if config section exists
            if (config.contains("enabled-structures.the_end")) {
                // Iterate through all keys in the_end section
                for (String key : config.getConfigurationSection("enabled-structures.the_end").getKeys(false)) {
                    // Store as UPPER_CASE for consistency
                    endStructures.put(key.toUpperCase(), config.getBoolean("enabled-structures.the_end." + key));
                }
            }
            // Store end structures map with "THE_END" key
            enabledStructures.put("THE_END", endStructures);
            
            // Initialize enabled biomes map
            enabledBiomes = new HashMap<>();
            
            // LOAD OVERWORLD (NORMAL) BIOMES
            // Create map for normal dimension biomes
            Map<String, Boolean> normalBiomes = new HashMap<>();
            
            // Check if config section exists
            if (config.contains("enabled-biomes.normal")) {
                // Iterate through all keys in normal section
                for (String key : config.getConfigurationSection("enabled-biomes.normal").getKeys(false)) {
                    // Store as UPPER_CASE for consistency
                    // Value is boolean (true = enabled, false = disabled)
                    normalBiomes.put(key.toUpperCase(), config.getBoolean("enabled-biomes.normal." + key));
                }
            }
            // Store normal biomes map with "NORMAL" key
            enabledBiomes.put("NORMAL", normalBiomes);
            
            // LOAD NETHER BIOMES
            // Create map for nether dimension biomes
            Map<String, Boolean> netherBiomes = new HashMap<>();
            
            // Check if config section exists
            if (config.contains("enabled-biomes.nether")) {
                // Iterate through all keys in nether section
                for (String key : config.getConfigurationSection("enabled-biomes.nether").getKeys(false)) {
                    // Store as UPPER_CASE for consistency
                    netherBiomes.put(key.toUpperCase(), config.getBoolean("enabled-biomes.nether." + key));
                }
            }
            // Store nether biomes map with "NETHER" key
            enabledBiomes.put("NETHER", netherBiomes);
            
            // LOAD END BIOMES
            // Create map for end dimension biomes
            Map<String, Boolean> endBiomes = new HashMap<>();
            
            // Check if config section exists
            if (config.contains("enabled-biomes.the_end")) {
                // Iterate through all keys in the_end section
                for (String key : config.getConfigurationSection("enabled-biomes.the_end").getKeys(false)) {
                    // Store as UPPER_CASE for consistency
                    endBiomes.put(key.toUpperCase(), config.getBoolean("enabled-biomes.the_end." + key));
                }
            }
            // Store end biomes map with "THE_END" key
            enabledBiomes.put("THE_END", endBiomes);
        }
        
        /**
         * Gets the configured search radius in chunks.
         * This value is used when searching for structures.
         * 
         * <p><b>Usage:</b></p>
         * Used in structure search operations to limit how far the search goes.
         * Larger values find structures farther away but take longer to execute.
         * 
         * <p><b>Conversion:</b></p>
         * To convert to blocks: radius * 16
         * Example: 100 chunks * 16 = 1600 blocks
         * 
         * @return Search radius in chunks (not blocks)
         */
        int getSearchRadius() {
            return searchRadius;
        }
        
        /**
         * Checks if a world is blacklisted (plugin disabled).
         * Used to prevent compass functionality in specific worlds.
         * 
         * <p><b>Use Cases:</b></p>
         * <ul>
         *   <li>Lobby servers where you don't want compasses to work</li>
         *   <li>Minigame arenas that have custom compass behavior</li>
         *   <li>Creative worlds where structure finding would be cheating</li>
         * </ul>
         * 
         * <p><b>Configuration:</b></p>
         * Set in config.yml under blacklisted-worlds list.
         * World names must match exactly (case-sensitive).
         * 
         * @param worldName The world name to check (e.g., "world", "world_nether")
         * @return true if world is blacklisted, false otherwise
         */
        boolean isWorldBlacklisted(String worldName) {
            return blacklistedWorlds.contains(worldName);
        }
        
        /**
         * Checks if a structure type is enabled for a specific world environment.
         * This is the main permission check for structure searches.
         * 
         * <p><b>Usage:</b></p>
         * Called before every structure search to verify the structure is
         * allowed in the current dimension.
         * 
         * <p><b>Return Values:</b></p>
         * <ul>
         *   <li>true: Structure is explicitly enabled in config</li>
         *   <li>false: Structure is disabled, not in config, or environment not configured</li>
         * </ul>
         * 
         * <p><b>Environment Mapping:</b></p>
         * <ul>
         *   <li>World.Environment.NORMAL → "NORMAL" section</li>
         *   <li>World.Environment.NETHER → "NETHER" section</li>
         *   <li>World.Environment.THE_END → "THE_END" section</li>
         * </ul>
         * 
         * @param environment The world environment (NORMAL, NETHER, THE_END)
         * @param structureType The structure type in UPPER_CASE format (e.g., "ANCIENT_CITY")
         * @return true if structure is enabled for this environment, false otherwise
         */
        boolean isStructureEnabled(World.Environment environment, String structureType) {
            // Convert environment enum to string key
            // NORMAL → "NORMAL", NETHER → "NETHER", THE_END → "THE_END"
            String envKey = environment.toString();
            
            // Get the structures map for this environment
            Map<String, Boolean> structures = enabledStructures.get(envKey);
            
            // Return false if environment section doesn't exist in config
            if (structures == null) {
                return false;
            }
            
            // Get enabled status for this structure type
            // Returns false if structure not present in config (disabled by default)
            return structures.getOrDefault(structureType, false);
        }
        
        /**
         * Gets all enabled structure types for a specific world environment.
         * Used for tab completion and "anything" searches.
         * 
         * <p><b>Usage:</b></p>
         * <ul>
         *   <li>Tab completion: Show only available structures for player's dimension</li>
         *   <li>"anything" command: Search only enabled structures</li>
         * </ul>
         * 
         * <p><b>Return Format:</b></p>
         * Returns a list of structure type strings in UPPER_CASE format.
         * Example: ["ANCIENT_CITY", "VILLAGE_PLAINS", "STRONGHOLD"]
         * 
         * <p><b>Empty List:</b></p>
         * Returns empty list if:
         * <ul>
         *   <li>Environment not configured in config</li>
         *   <li>All structures disabled for this environment</li>
         * </ul>
         * 
         * @param environment The world environment (NORMAL, NETHER, THE_END)
         * @return List of enabled structure types in UPPER_CASE format (never null, may be empty)
         */
        List<String> getEnabledStructuresForEnvironment(World.Environment environment) {
            // Convert environment enum to string key
            String envKey = environment.toString();
            
            // Get the structures map for this environment
            Map<String, Boolean> structures = enabledStructures.get(envKey);
            
            // Create result list
            List<String> result = new ArrayList<>();
            
            // Only process if environment section exists
            if (structures != null) {
                // Iterate through all structure entries for this environment
                for (Map.Entry<String, Boolean> entry : structures.entrySet()) {
                    // Only add structures that are explicitly enabled (value = true)
                    if (entry.getValue()) {
                        result.add(entry.getKey());
                    }
                }
            }
            
            return result;
        }
        
        /**
         * Checks if a biome type is enabled for a specific world environment.
         * This is the main permission check for biome searches.
         * 
         * <p><b>Usage:</b></p>
         * Called before every biome search to verify the biome is
         * allowed in the current dimension.
         * 
         * <p><b>Return Values:</b></p>
         * <ul>
         *   <li>true: Biome is explicitly enabled in config</li>
         *   <li>false: Biome is disabled, not in config, or environment not configured</li>
         * </ul>
         * 
         * <p><b>Environment Mapping:</b></p>
         * <ul>
         *   <li>World.Environment.NORMAL → "NORMAL" section</li>
         *   <li>World.Environment.NETHER → "NETHER" section</li>
         *   <li>World.Environment.THE_END → "THE_END" section</li>
         * </ul>
         * 
         * @param environment The world environment (NORMAL, NETHER, THE_END)
         * @param biomeType The biome type in UPPER_CASE format (e.g., "DARK_FOREST")
         * @return true if biome is enabled for this environment, false otherwise
         */
        boolean isBiomeEnabled(World.Environment environment, String biomeType) {
            // Convert environment enum to string key
            // NORMAL → "NORMAL", NETHER → "NETHER", THE_END → "THE_END"
            String envKey = environment.toString();
            
            // Get the biomes map for this environment
            Map<String, Boolean> biomes = enabledBiomes.get(envKey);
            
            // Return false if environment section doesn't exist in config
            if (biomes == null) {
                return false;
            }
            
            // Get enabled status for this biome type
            // Returns false if biome not present in config (disabled by default)
            return biomes.getOrDefault(biomeType, false);
        }
        
        /**
         * Gets all enabled biome types for a specific world environment.
         * Used for tab completion.
         * 
         * <p><b>Usage:</b></p>
         * <ul>
         *   <li>Tab completion: Show only available biomes for player's dimension</li>
         * </ul>
         * 
         * <p><b>Return Format:</b></p>
         * Returns a list of biome type strings in UPPER_CASE format.
         * Example: ["DARK_FOREST", "CHERRY_GROVE", "MUSHROOM_FIELDS"]
         * 
         * <p><b>Empty List:</b></p>
         * Returns empty list if:
         * <ul>
         *   <li>Environment not configured in config</li>
         *   <li>All biomes disabled for this environment</li>
         * </ul>
         * 
         * @param environment The world environment (NORMAL, NETHER, THE_END)
         * @return List of enabled biome types in UPPER_CASE format (never null, may be empty)
         */
        List<String> getEnabledBiomesForEnvironment(World.Environment environment) {
            // Convert environment enum to string key
            String envKey = environment.toString();
            
            // Get the biomes map for this environment
            Map<String, Boolean> biomes = enabledBiomes.get(envKey);
            
            // Create result list
            List<String> result = new ArrayList<>();
            
            // Only process if environment section exists
            if (biomes != null) {
                // Iterate through all biome entries for this environment
                for (Map.Entry<String, Boolean> entry : biomes.entrySet()) {
                    // Only add biomes that are explicitly enabled (value = true)
                    if (entry.getValue()) {
                        result.add(entry.getKey());
                    }
                }
            }
            
            return result;
        }
    }
}