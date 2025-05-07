package dev.aurora.struct.Types.Player;

import dev.aurora.Execption.ArgumentParseException;
import dev.aurora.struct.ArgumentType;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class OnlinePlayerArgumentType implements ArgumentType<Player> {

    @Override
    public String getName() {
        return "player";
    }

    @Override
    public Player parse(CommandSender sender, String input) throws ArgumentParseException {
        if (input == null || input.trim().isEmpty()) {
            System.out.println("Player input is null or empty");
            throw new ArgumentParseException("Player name cannot be empty!");
        }

        // Case-insensitive player lookup
        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            if (onlinePlayer.getName().equalsIgnoreCase(input)) {
                return onlinePlayer;
            }
        }

        throw new ArgumentParseException("Player '" + input + "' not found or offline!");
    }

    @Override
    public List<String> getCompletions(CommandSender sender) {
        List<String> completions = new ArrayList<>();
        for (Player player : Bukkit.getOnlinePlayers()) {
            completions.add(player.getName());
        }
        return completions;
    }
}