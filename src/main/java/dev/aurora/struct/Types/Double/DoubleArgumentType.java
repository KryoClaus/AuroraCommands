package dev.aurora.struct.Types.Double;

import dev.aurora.Execption.ArgumentParseException;
import dev.aurora.struct.ArgumentType;
import org.bukkit.command.CommandSender;

import java.util.Collections;
import java.util.List;

public class DoubleArgumentType implements ArgumentType<Double> {
    @Override
    public String getName() {
        return "double";
    }

    @Override
    public Double parse(CommandSender sender, String input) throws ArgumentParseException {
        try {
            return Double.parseDouble(input);
        }catch (NumberFormatException e){
            throw new ArgumentParseException(input);
        }
    }

    @Override
    public List<String> getCompletions(CommandSender sender) {
        return Collections.emptyList();
    }
}
