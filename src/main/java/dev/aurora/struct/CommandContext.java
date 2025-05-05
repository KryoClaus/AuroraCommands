package dev.aurora.struct;

import java.util.HashMap;
import java.util.Map;

public class CommandContext {
    private final Map<String, Object> arguments;

    public CommandContext() {
        this.arguments = new HashMap<>();
    }

    public void addArgument(String name, Object value) {
        arguments.put(name, value);
    }

    @SuppressWarnings("unchecked")
    public <T> T getArgument(String name) {
        return (T) arguments.get(name);
    }
}