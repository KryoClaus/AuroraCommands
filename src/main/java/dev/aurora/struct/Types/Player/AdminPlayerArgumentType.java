package dev.aurora.struct.Types.Player;

import dev.aurora.Execption.ArgumentParseException;
import dev.aurora.struct.ArgumentType;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AdminPlayerArgumentType implements ArgumentType<Player> {
    @Override
    public String getName() {
        return "admins";
    }

    @Override
    public Player parse(CommandSender sender, String input) throws ArgumentParseException {
        if (input == null || input.trim().isEmpty()){
            throw new ArgumentParseException("(!) Input for command argument admins was null");
        }

        for (Player admin : getAdmins()){
            if (admin.getName().equalsIgnoreCase(input)){
                return admin;
            }
        }
        throw new ArgumentParseException(input);
    }

    @Override
    public List<String> getCompletions(CommandSender sender) {
        List<String > admins = new ArrayList<>();
        for (Player admin : getAdmins()){
            admins.add(admin.getName());
        }
        return admins;
    }

    public List<Player> getAdmins(){
        List<Player> admins = new ArrayList<>();
        for (Player player : Bukkit.getOnlinePlayers()){
            if (player.isOp() || player.hasPermission("*")){
                admins.add(player);
            }
        }
        return admins;
    }

}
