package dev.aurora.struct.Types;

import dev.aurora.struct.ArgumentType;
import org.bukkit.command.CommandSender;

import java.util.Collections;
import java.util.List;

public class StringArgumentType implements ArgumentType<String> {
    private final boolean phrase;

    public StringArgumentType() {
        this(false);
    }

    public StringArgumentType(boolean phrase) {
        this.phrase = phrase;
    }

    @Override
    public String getName() {
        return "string";
    }

    @Override
    public String parse(CommandSender sender, String input) {
        return input;
    }

    @Override
    public List<String> getCompletions(CommandSender sender) {
        return Collections.emptyList();
    }
}
