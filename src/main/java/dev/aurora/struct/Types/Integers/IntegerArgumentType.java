package dev.aurora.struct.Types.Integers;

import dev.aurora.Execption.ArgumentParseException;
import dev.aurora.struct.ArgumentType;
import org.bukkit.command.CommandSender;

import java.util.Collections;
import java.util.List;

public class IntegerArgumentType implements ArgumentType<Integer> {
    @Override
    public String getName() {
        return "integer";
    }

    @Override
    public Integer parse(CommandSender sender, String input) throws ArgumentParseException {
        try {
            return Integer.parseInt(input);
        } catch (NumberFormatException e) {
            throw new ArgumentParseException("'" + input + "' is not a valid number!");
        }
    }

    @Override
    public List<String> getCompletions(CommandSender sender) {
        return Collections.emptyList();
    }
}