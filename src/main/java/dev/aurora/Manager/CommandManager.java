package dev.aurora.Manager;

import dev.aurora.Command.AuroraCommand;
import dev.aurora.Execption.ArgumentParseException;
import dev.aurora.struct.ArgumentTypeRegistry;
import dev.aurora.struct.CommandTabCompleter;
import dev.aurora.struct.Types.IntegerArgumentType;
import dev.aurora.struct.Types.LocationArgumentType;
import dev.aurora.struct.Types.OnlinePlayerArgumentType;
import dev.aurora.struct.Types.StringArgumentType;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

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
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        AuroraCommand auroraCommand = commands.get(command.getName().toLowerCase());
        if (auroraCommand == null) return false;

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