package dev.aurora.struct.Types.Float;

import dev.aurora.Execption.ArgumentParseException;
import dev.aurora.struct.ArgumentType;
import org.bukkit.command.CommandSender;

import java.awt.*;
import java.util.Collections;
import java.util.List;

public class FloatArgumentType implements ArgumentType<Float> {
    @Override
    public String getName() {
        return "float";
    }

    @Override
    public Float parse(CommandSender sender, String input) throws ArgumentParseException {
        try {
            return Float.parseFloat(input);
        }catch (NumberFormatException e){
            throw new ArgumentParseException(input);
        }
    }

    @Override
    public List<String> getCompletions(CommandSender sender) {
        return Collections.emptyList();
    }
}
