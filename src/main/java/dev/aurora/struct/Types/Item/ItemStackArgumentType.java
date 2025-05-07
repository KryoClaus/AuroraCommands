package dev.aurora.struct.Types.Item;

import dev.aurora.Execption.ArgumentParseException;
import dev.aurora.struct.ArgumentType;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ItemStackArgumentType implements ArgumentType<ItemStack> {
    @Override
    public String getName() {
        return "item";
    }

    @Override
    public ItemStack parse(CommandSender sender, String input) throws ArgumentParseException {
        if (input == null || input.trim().isEmpty()){
            throw new ArgumentParseException("Item cant be null");
        }
        for (Material mat : Material.values()){
            if (mat.name().equalsIgnoreCase(input)){
                return new ItemStack(mat);
            }
        }
        throw new ArgumentParseException("invalid Item");
    }

    @Override
    public List<String> getCompletions(CommandSender sender) {
        List<String> items = new ArrayList<>();
        for (Material mat : Material.values()){
            items.add(mat.name());
        }
        return items;
    }
}
