package dev.aurora.struct.Types;

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
        Player player = Bukkit.getPlayer(input);
        if (player == null) {
            throw new ArgumentParseException("Player '" + input + "' not found!");
        }
        return player;
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