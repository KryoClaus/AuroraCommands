package dev.aurora.Command;

import dev.aurora.Execption.ArgumentParseException;
import dev.aurora.Manager.CommandManager;
import dev.aurora.struct.ArgumentType;
import dev.aurora.struct.CommandContext;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

public class AuroraCommand {
    private final String name;
    private final List<String> aliases;
    private String permission;
    private long cooldownMillis;
    private final Map<UUID, Long> cooldowns;
    private final List<ArgumentEntry> arguments;
    private BiConsumer<CommandSender, CommandContext> executor;
    private Class<? extends CommandSender> senderType;
    private final List<AuroraCommand> subCommands;
    private final CommandManager manager;

    public static class ArgumentEntry {
        private final String name;
        private final ArgumentType<?> type;

        public ArgumentEntry(String name, ArgumentType<?> type){
            this.name = name;
            this.type = type;
        }

        public String getName(){
            return name;
        }

        public ArgumentType<?> getType(){
            return type;
        }
    }

    /**
     *
     * @param name Command Name
     * @param manager Command Manager
     */
    public AuroraCommand(String name, CommandManager manager) {
        this.name = name;
        this.manager = manager;
        this.aliases = new ArrayList<>();
        this.cooldowns = new HashMap<>();
        this.arguments = new ArrayList<>();
        this.subCommands = new ArrayList<>();
        this.senderType = CommandSender.class;
    }

    /**
     *
     * @param alias
     * @return
     */

    public AuroraCommand addAlias(String alias) {
        aliases.add(alias);
        return this;
    }

    /**
     *
     * @param aliases
     * @return
     */
    public AuroraCommand addAliases(String... aliases) {
        Collections.addAll(this.aliases, aliases);
        return this;
    }

    /**
     *
     * @param permission
     * @return
     */

    public AuroraCommand addPermission(String permission) {
        this.permission = permission;
        return this;
    }

    /**
     *
     * @param seconds
     * @return
     */

    public AuroraCommand addCooldown(long seconds) {
        this.cooldownMillis = seconds * 1000;
        return this;
    }

    /**
     *
     * @param name
     * @param type
     * @return
     */
    public AuroraCommand addArgument(String name, ArgumentType<?> type) {
        arguments.add(new ArgumentEntry(name, type));
        return this;
    }

    /**
     *
     * @param senderType
     * @param executor
     * @return
     */

    public AuroraCommand addExecution(Class<? extends CommandSender> senderType, BiConsumer<CommandSender, CommandContext> executor) {
        this.senderType = senderType;
        this.executor = executor;
        return this;
    }

    /**
     *
     * @param subCommand
     * @return
     */

    public AuroraCommand addSubCommand(AuroraCommand subCommand) {
        subCommands.add(subCommand);
        return this;
    }

    /**
     *
     * @return
     */

    public AuroraCommand register() {
        manager.registerCommand(this);
        return this;
    }

    /**
     *
     * @return
     */

    public String getName() {
        return name;
    }

    public List<String> getAliases() {
        return aliases;
    }

    public boolean hasPermission(CommandSender sender) {
        return permission == null || sender.hasPermission(permission);
    }

    public boolean isOnCooldown(CommandSender sender) {
        if (!(sender instanceof Player) || cooldownMillis == 0) return false;
        Player player = (Player) sender;
        Long lastUsed = cooldowns.get(player.getUniqueId());
        if (lastUsed == null) return false;
        return System.currentTimeMillis() < lastUsed + cooldownMillis;
    }

    public long getCooldownRemaining(CommandSender sender) {
        if (!(sender instanceof Player)) return 0;
        Player player = (Player) sender;
        Long lastUsed = cooldowns.get(player.getUniqueId());
        if (lastUsed == null) return 0;
        return Math.max(0, lastUsed + cooldownMillis - System.currentTimeMillis());
    }

    public void applyCooldown(CommandSender sender) {
        if (sender instanceof Player && cooldownMillis > 0) {
            Player player = (Player) sender;
            cooldowns.put(player.getUniqueId(), System.currentTimeMillis());
        }
    }

    /**
     *
     * @param sender
     * @param args
     * @throws ArgumentParseException
     */

    public void execute(CommandSender sender, String[] args) throws ArgumentParseException {
        if (!senderType.isInstance(sender)) {
            sender.sendMessage("§cThis command is only for " + senderType.getSimpleName() + "!");
            return;
        }

        // Check for subcommands
        if (args.length > 0) {
            for (AuroraCommand subCommand : subCommands) {
                if (subCommand.getName().equalsIgnoreCase(args[0]) || subCommand.getAliases().stream().anyMatch(alias -> alias.equalsIgnoreCase(args[0]))) {
                    if (!subCommand.hasPermission(sender)) {
                        sender.sendMessage("§cYou don't have permission!");
                        return;
                    }
                    if (subCommand.isOnCooldown(sender)) {
                        long remaining = subCommand.getCooldownRemaining(sender);
                        sender.sendMessage("§cSubcommand on cooldown! Wait " + (remaining / 1000) + " seconds.");
                        return;
                    }
                    subCommand.execute(sender, Arrays.copyOfRange(args, 1, args.length));
                    subCommand.applyCooldown(sender);
                    return;
                }
            }
        }

        // Execute main command
        if (arguments.size() > args.length) {
            sender.sendMessage("§cUsage: /" + name + " " + getUsage());
            return;
        }

        // Parse arguments
        CommandContext context = new CommandContext();
        for (int i = 0; i < arguments.size(); i++) {
            ArgumentEntry entry = arguments.get(i);
            String argName = entry.getName();
            ArgumentType<?> type = entry.getType();
            String input = i < args.length ? args[i] : null;
            try {
                Object value = type.parse(sender, input);
                context.addArgument(type.getName(), value);
            }catch (ArgumentParseException e){
                throw e;
            }
            ;

        }

        if (executor != null) {
            executor.accept(sender, context);
        } else if (subCommands.size() > 0) {
            sender.sendMessage("§cAvailable subcommands: " + getSubCommandNames());
        } else {
            sender.sendMessage("§cNo execution defined for this command.");
        }
    }

    public List<String> getTabCompletions(CommandSender sender, String[] args) {
        if (args.length == 1) {
            List<String> completions = new ArrayList<>();
            // Add subcommand names and aliases
            for (AuroraCommand subCommand : subCommands) {
                if (subCommand.hasPermission(sender)) {
                    completions.add(subCommand.getName());
                    completions.addAll(subCommand.getAliases());
                }
            }
            // Add main command argument completions if applicable
            if (!arguments.isEmpty()) {
                ArgumentType<?> type = arguments.get(args.length - 1).getType();
                return type.getCompletions(sender).stream().filter(completion -> completion.toLowerCase().startsWith(args[args.length - 1].toLowerCase())).collect(Collectors.toList());
            }
            return completions.stream()
                    .filter(c -> c.toLowerCase().startsWith(args[0].toLowerCase()))
                    .sorted()
                    .collect(Collectors.toList());
        } else if (args.length > 1) {
            // Check for subcommand
            for (AuroraCommand subCommand : subCommands) {
                if (subCommand.getName().equalsIgnoreCase(args[0]) || subCommand.getAliases().stream().anyMatch(alias -> alias.equalsIgnoreCase(args[0]))) {
                    return subCommand.getTabCompletions(sender, Arrays.copyOfRange(args, 1, args.length));
                }
            }
        }
        return new ArrayList<>();
    }

    /**
     *
     * @return
     */
    private String getUsage() {
        StringBuilder usage = new StringBuilder();
        if (!subCommands.isEmpty()) {
            usage.append("[");
            usage.append(getSubCommandNames());
            if (!arguments.isEmpty()) {
                usage.append("|");
                for (ArgumentEntry entry : arguments) {
                    usage.append("<").append(entry.getName()).append(">");
                }
            }
            usage.append("]");
        } else {
            for (ArgumentEntry entry : arguments) {
                usage.append("<").append(entry.getName()).append("> ");
            }
        }
        return usage.toString().trim();
    }

    private String getSubCommandNames() {
        StringBuilder names = new StringBuilder();
        for (AuroraCommand subCommand : subCommands) {
            names.append(subCommand.getName()).append(", ");
        }
        return names.length() > 0 ? names.substring(0, names.length() - 2) : "";
    }
}