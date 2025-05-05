package dev.aurora.struct;

import java.util.HashMap;
import java.util.Map;

public class ArgumentTypeRegistry {
    private final Map<String, ArgumentType<?>> types;

    public ArgumentTypeRegistry() {
        this.types = new HashMap<>();
    }

    public void registerType(String name, ArgumentType<?> type) {
        types.put(name.toLowerCase(), type);
    }

    public ArgumentType<?> getType(String name) {
        return types.get(name.toLowerCase());
    }
}
