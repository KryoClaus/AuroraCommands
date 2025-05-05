package dev.aurora.struct.Types;

import dev.aurora.Execption.ArgumentParseException;
import dev.aurora.struct.ArgumentType;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class LocationArgumentType implements ArgumentType<Location> {
    @Override
    public String getName() {
        return "location";
    }

    @Override
    public Location parse(CommandSender sender, String input) throws ArgumentParseException {
        String[] parts = input.split(",");
        if (parts.length != 3 && parts.length != 4) {
            throw new ArgumentParseException("Location must be in format 'x,y,z' or 'world,x,y,z'");
        }

        World world;
        int startIndex;
        if (parts.length == 4) {
            world = Bukkit.getWorld(parts[0]);
            if (world == null) {
                throw new ArgumentParseException("World '" + parts[0] + "' not found");
            }
            startIndex = 1;
        } else {
            if (!(sender instanceof Player)) {
                throw new ArgumentParseException("Must specify world for non-player senders (world,x,y,z)");
            }
            world = ((Player) sender).getWorld();
            startIndex = 0;
        }

        try {
            double x = Double.parseDouble(parts[startIndex]);
            double y = Double.parseDouble(parts[startIndex + 1]);
            double z = Double.parseDouble(parts[startIndex + 2]);
            return new Location(world, x, y, z);
        } catch (NumberFormatException e) {
            throw new ArgumentParseException("Invalid coordinates: " + input);
        }
    }

    @Override
    public List<String> getCompletions(CommandSender sender) {
        List<String> completions = new ArrayList<>();
        // Suggest world names for the first part
        completions.addAll(Bukkit.getWorlds().stream()
                .map(World::getName)
                .collect(Collectors.toList()));
        // Suggest placeholder formats
        completions.add("<x>,<y>,<z>");
        if (!(sender instanceof Player)) {
            completions.add("<world>,<x>,<y>,<z>");
        }
        return completions;
    }
}