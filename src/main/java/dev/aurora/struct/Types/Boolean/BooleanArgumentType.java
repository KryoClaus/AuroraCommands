package dev.aurora.struct.Types.Boolean;

import dev.aurora.Execption.ArgumentParseException;
import dev.aurora.struct.ArgumentType;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BooleanArgumentType implements ArgumentType<Boolean> {
    @Override
    public String getName() {
        return "boolean";
    }

    @Override
    public Boolean parse(CommandSender sender, String input) throws ArgumentParseException {
        try{
            return Boolean.parseBoolean(input);
        }catch (Exception e){
            throw new ArgumentParseException(input);
        }
    }

    @Override
    public List<String> getCompletions(CommandSender sender) {
        List<String> completions = new ArrayList<>();
        completions.add("true");
        completions.add("false");
        return completions;
    }
}
