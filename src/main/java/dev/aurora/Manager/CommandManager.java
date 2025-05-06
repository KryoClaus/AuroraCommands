package dev.aurora.Manager;

import dev.aurora.Command.AuroraCommand;
import dev.aurora.Execption.ArgumentParseException;
import dev.aurora.struct.ArgumentTypeRegistry;
import dev.aurora.struct.CommandTabCompleter;
import dev.aurora.struct.Types.IntegerArgumentType;
import dev.aurora.struct.Types.LocationArgumentType;
import dev.aurora.struct.Types.OnlinePlayerArgumentType;
import dev.aurora.struct.Types.StringArgumentType;
import org.bukkit.command.*;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

public class CommandManager implements CommandExecutor {
    private final JavaPlugin plugin;
    private final Map<String, AuroraCommand> commands;
    private final ArgumentTypeRegistry argumentRegistry;

    public CommandManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.commands = new HashMap<>();
        this.argumentRegistry = new ArgumentTypeRegistry();
        registerDefaultArgumentTypes();
        plugin.getLogger().info("CommandManager initialized for plugin: " + plugin.getName());
    }

    private void registerDefaultArgumentTypes() {
        argumentRegistry.registerType("player", new OnlinePlayerArgumentType());
        argumentRegistry.registerType("string", new StringArgumentType());
        argumentRegistry.registerType("integer", new IntegerArgumentType());
        argumentRegistry.registerType("location", new LocationArgumentType());
    }

    public void registerCommand(AuroraCommand command) {
        commands.put(command.getName().toLowerCase(), command);
        for (String alias : command.getAliases()) {
            commands.put(alias.toLowerCase(), command);
        }
        PluginCommand pluginCommand = plugin.getCommand(command.getName());
        if (pluginCommand != null) {
            pluginCommand.setExecutor(this);
            pluginCommand.setTabCompleter(new CommandTabCompleter(commands, argumentRegistry));
            plugin.getLogger().info("Successfully registered command: " + command.getName());
        } else {
            plugin.getLogger().warning("PluginCommand null for: " + command.getName() + ". Attempting manual registration.");
            try {
                Constructor<PluginCommand> constructor = PluginCommand.class.getDeclaredConstructor(String.class, Plugin.class);
                constructor.setAccessible(true);
                pluginCommand = constructor.newInstance(command.getName(), plugin);
                pluginCommand.setAliases(command.getAliases());
                pluginCommand.setExecutor(this);
                pluginCommand.setTabCompleter(new CommandTabCompleter(commands, argumentRegistry));
                Field commandMapField = plugin.getServer().getPluginManager().getClass().getDeclaredField("commandMap");
                commandMapField.setAccessible(true);
                SimpleCommandMap commandMap = (SimpleCommandMap) commandMapField.get(plugin.getServer().getPluginManager());
                commandMap.register(plugin.getName(), pluginCommand);
                plugin.getLogger().info("Manually registered command: " + command.getName());
            } catch (Exception e) {
                plugin.getLogger().severe("Failed to manually register command: " + command.getName());
                e.printStackTrace();
            }
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        plugin.getLogger().info("Processing command: " + command.getName() + " with label: " + label);
        AuroraCommand auroraCommand = commands.get(command.getName().toLowerCase());
        if (auroraCommand == null) {
            plugin.getLogger().warning("No AuroraCommand found for: " + command.getName());
            return false;
        }

        if (!auroraCommand.hasPermission(sender)) {
            sender.sendMessage("§cYou don't have permission!");
            return true;
        }

        if (auroraCommand.isOnCooldown(sender)) {
            long remaining = auroraCommand.getCooldownRemaining(sender);
            sender.sendMessage("§cCommand on cooldown! Wait " + (remaining / 1000) + " seconds.");
            return true;
        }

        try {
            auroraCommand.execute(sender, args);
        } catch (ArgumentParseException e) {
            sender.sendMessage("§c" + e.getMessage());
        }

        auroraCommand.applyCooldown(sender);
        return true;
    }

    public ArgumentTypeRegistry getArgumentRegistry() {
        return argumentRegistry;
    }
}