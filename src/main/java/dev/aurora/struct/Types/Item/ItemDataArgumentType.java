package dev.aurora.struct.Types.Item;

import dev.aurora.Execption.ArgumentParseException;
import dev.aurora.struct.ArgumentType;
import org.bukkit.command.CommandSender;

import java.util.Collections;
import java.util.List;

public class ItemDataArgumentType implements ArgumentType<Integer> {
    @Override
    public String getName() {
        return "itemData";
    }

    @Override
    public Integer parse(CommandSender sender, String input) throws ArgumentParseException {
        if (input == null || input.trim().isEmpty()){
            throw new ArgumentParseException("value cannot be null");
        }
        return Integer.parseInt(input);
    }

    @Override
    public List<String> getCompletions(CommandSender sender) {
        return Collections.emptyList();
    }
}
