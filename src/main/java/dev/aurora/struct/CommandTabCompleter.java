package dev.aurora.struct;

import dev.aurora.Command.AuroraCommand;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.List;
import java.util.Map;

public class CommandTabCompleter implements TabCompleter {
    private final Map<String, AuroraCommand> commands;
    private final ArgumentTypeRegistry argumentRegistry;

    public CommandTabCompleter(Map<String, AuroraCommand> commands, ArgumentTypeRegistry argumentRegistry) {
        this.commands = commands;
        this.argumentRegistry = argumentRegistry;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        AuroraCommand auroraCommand = commands.get(command.getName().toLowerCase());
        if (auroraCommand == null) return null;
        return auroraCommand.getTabCompletions(sender, args);
    }
}