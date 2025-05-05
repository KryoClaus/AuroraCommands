package dev.aurora.struct;

import dev.aurora.Execption.ArgumentParseException;
import org.bukkit.command.CommandSender;

import java.util.List;

public interface ArgumentType<T> {
    String getName();
    T parse(CommandSender sender, String input) throws ArgumentParseException;
    List<String> getCompletions(CommandSender sender);
}