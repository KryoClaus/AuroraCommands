package dev.aurora.Command;

import dev.aurora.Execption.ArgumentParseException;
import dev.aurora.Manager.CommandManager;
import dev.aurora.struct.ArgumentType;
import dev.aurora.struct.CommandContext;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * The core class for defining and executing commands in the AuroraCommand API.
 * Supports fluent configuration of commands, subcommands, arguments, permissions, cooldowns, and tab completion.
 * Allows arbitrary argument names for flexible command design.
 */
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
    private final Logger logger;

    // Inner class to store argument name and type
    private static class ArgumentEntry {
        private final String name;
        private final ArgumentType<?> type;

        ArgumentEntry(String name, ArgumentType<?> type) {
            this.name = name;
            this.type = type;
        }

        String getName() {
            return name;
        }

        ArgumentType<?> getType() {
            return type;
        }
    }

    /**
     * Constructs a new AuroraCommand with the specified name and manager.
     *
     * @param name    The command name (e.g., "message").
     * @param manager The CommandManager instance managing this command.
     */
    public AuroraCommand(String name, CommandManager manager) {
        this.name = name;
        this.manager = manager;
        this.aliases = new ArrayList<>();
        this.cooldowns = new HashMap<>();
        this.arguments = new ArrayList<>();
        this.subCommands = new ArrayList<>();
        this.senderType = CommandSender.class;
        this.logger = Logger.getLogger("InfusedAddons");
    }

    /**
     * Adds an alias for the command.
     *
     * @param alias The alias to add.
     * @return This AuroraCommand for chaining.
     */
    public AuroraCommand addAlias(String alias) {
        aliases.add(alias.toLowerCase());
        logger.info("Added alias '" + alias + "' for command: " + name);
        return this;
    }

    /**
     * Adds multiple aliases for the command.
     *
     * @param aliases The aliases to add.
     * @return This AuroraCommand for chaining.
     */
    public AuroraCommand addAliases(String... aliases) {
        for (String alias : aliases) {
            addAlias(alias);
        }
        return this;
    }

    /**
     * Sets the required permission for the command.
     *
     * @param permission The permission node (e.g., "infusedpvp.message").
     * @return This AuroraCommand for chaining.
     */
    public AuroraCommand addPermission(String permission) {
        this.permission = permission;
        logger.info("Set permission '" + permission + "' for command: " + name);
        return this;
    }

    /**
     * Sets a cooldown for the command in seconds.
     *
     * @param seconds The cooldown duration.
     * @return This AuroraCommand for chaining.
     */
    public AuroraCommand addCooldown(long seconds) {
        this.cooldownMillis = seconds * 1000;
        logger.info("Set cooldown " + seconds + " seconds for command: " + name);
        return this;
    }

    /**
     * Adds an argument to the command with a user-defined name and type.
     *
     * @param name The name of the argument (e.g., "customName").
     * @param type The argument type (e.g., StringArgumentType).
     * @return This AuroraCommand for chaining.
     */
    public AuroraCommand addArgument(String name, ArgumentType<?> type) {
        logger.info("Adding argument: name=" + name + ", type=" + type.getName());
        arguments.add(new ArgumentEntry(name, type));
        return this;
    }

    /**
     * Sets the execution logic for the command, restricted to a specific sender type.
     *
     * @param senderType The type of sender (e.g., Player.class).
     * @param executor   The execution logic.
     * @return This AuroraCommand for chaining.
     */
    public AuroraCommand addExecution(Class<? extends CommandSender> senderType, BiConsumer<CommandSender, CommandContext> executor) {
        this.senderType = senderType;
        this.executor = executor;
        logger.info("Set execution for command: " + name + ", senderType: " + senderType.getSimpleName());
        return this;
    }

    /**
     * Adds a subcommand to this command.
     *
     * @param subCommand The subcommand to add.
     * @return This AuroraCommand for chaining.
     */
    public AuroraCommand addSubCommand(AuroraCommand subCommand) {
        subCommands.add(subCommand);
        logger.info("Added subcommand '" + subCommand.getName() + "' to command: " + name);
        return this;
    }

    /**
     * Registers the command with the CommandManager.
     */
    public void register() {
        manager.registerCommand(this);
        logger.info("Registered command: " + name);
    }

    /**
     * Executes the command or its subcommands.
     *
     * @param sender The sender executing the command.
     * @param args   The command arguments.
     * @throws ArgumentParseException If argument parsing fails.
     */
    public void execute(CommandSender sender, String[] args) throws ArgumentParseException {
        logger.info("Executing command: " + name + " for sender: " + sender.getName() + ", args: " + (args != null ? String.join(", ", args) : "null"));
        if (!senderType.isInstance(sender)) {
            sender.sendMessage("§cThis command is only for " + senderType.getSimpleName() + "!");
            return;
        }

        // Check for subcommands
        if (args != null && args.length > 0) {
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

        // Validate argument count
        if (args == null || arguments.size() > args.length) {
            sender.sendMessage("§cUsage: /" + name + " " + getUsage());
            logger.warning("Insufficient arguments for " + name + ": expected " + arguments.size() + ", got " + (args != null ? args.length : 0));
            return;
        }

        // Parse arguments
        CommandContext context = new CommandContext();
        for (int i = 0; i < arguments.size(); i++) {
            ArgumentEntry entry = arguments.get(i);
            String argName = entry.getName(); // Use user-defined name
            ArgumentType<?> type = entry.getType();
            String input = i < args.length ? args[i] : null;
            try {
                logger.info("Parsing argument " + argName + " (type: " + type.getName() + ") with input: " + (input != null ? input : "null"));
                Object value = type.parse(sender, input);
                if (value == null) {
                    logger.warning("Parsed value is null for argument " + argName);
                }
                context.addArgument(argName, value);
                logger.info("Added argument " + argName + ": " + (value != null ? value.toString() : "null"));
            } catch (ArgumentParseException e) {
                logger.warning("Failed to parse argument " + argName + ": " + e.getMessage());
                throw e;
            }
        }

        // Execute command
        if (executor != null) {
            logger.info("Executing command with context: " + context.toString());
            executor.accept(sender, context);
        } else if (subCommands.size() > 0) {
            sender.sendMessage("§cAvailable subcommands: " + getSubCommandNames());
        } else {
            sender.sendMessage("§cNo execution defined for this command.");
        }
    }

    /**
     * Checks if the sender has the required permission.
     *
     * @param sender The sender to check.
     * @return True if the sender has permission or no permission is set, false otherwise.
     */
    public boolean hasPermission(CommandSender sender) {
        if (permission == null || permission.isEmpty()) {
            return true;
        }
        boolean hasPermission = sender.hasPermission(permission);
        logger.info("Checking permission '" + permission + "' for sender: " + sender.getName() + ", result: " + hasPermission);
        return hasPermission;
    }

    /**
     * Checks if the command is on cooldown for the sender.
     *
     * @param sender The sender to check.
     * @return True if the command is on cooldown, false otherwise.
     */
    public boolean isOnCooldown(CommandSender sender) {
        if (cooldownMillis <= 0 || !(sender instanceof Player)) {
            return false;
        }
        UUID uuid = ((Player) sender).getUniqueId();
        Long lastUsed = cooldowns.get(uuid);
        if (lastUsed == null) {
            return false;
        }
        long now = System.currentTimeMillis();
        boolean onCooldown = now < lastUsed + cooldownMillis;
        logger.info("Checking cooldown for " + name + " by " + sender.getName() + ": onCooldown=" + onCooldown);
        return onCooldown;
    }

    /**
     * Gets the remaining cooldown time for the sender.
     *
     * @param sender The sender to check.
     * @return The remaining cooldown time in milliseconds, or 0 if not on cooldown.
     */
    public long getCooldownRemaining(CommandSender sender) {
        if (cooldownMillis <= 0 || !(sender instanceof Player)) {
            return 0;
        }
        UUID uuid = ((Player) sender).getUniqueId();
        Long lastUsed = cooldowns.get(uuid);
        if (lastUsed == null) {
            return 0;
        }
        long now = System.currentTimeMillis();
        long remaining = lastUsed + cooldownMillis - now;
        return Math.max(0, remaining);
    }

    /**
     * Applies a cooldown to the sender.
     *
     * @param sender The sender to apply the cooldown to.
     */
    public void applyCooldown(CommandSender sender) {
        if (cooldownMillis <= 0 || !(sender instanceof Player)) {
            return;
        }
        UUID uuid = ((Player) sender).getUniqueId();
        cooldowns.put(uuid, System.currentTimeMillis());
        logger.info("Applied cooldown for " + name + " to " + sender.getName());
    }

    /**
     * Gets tab completion suggestions for the command.
     *
     * @param sender The sender requesting completions.
     * @param args   The current arguments.
     * @return A list of completion suggestions.
     */
    public List<String> getTabCompletions(CommandSender sender, String[] args) {
        logger.info("Generating tab completions for " + name + ", args: " + (args != null ? String.join(", ", args) : "null"));
        if (args == null || args.length == 0) {
            return new ArrayList<>();
        }

        // Suggest subcommands for the first argument
        if (args.length == 1) {
            List<String> completions = new ArrayList<>();
            for (AuroraCommand subCommand : subCommands) {
                if (subCommand.hasPermission(sender)) {
                    completions.add(subCommand.getName());
                    completions.addAll(subCommand.getAliases());
                }
            }
            for (ArgumentEntry entry : arguments) {
                completions.addAll(entry.getType().getCompletions(sender));
            }
            return completions.stream()
                    .filter(completion -> completion.toLowerCase().startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }

        // Suggest argument completions
        if (args.length <= arguments.size()) {
            ArgumentType<?> type = arguments.get(args.length - 1).getType();
            return type.getCompletions(sender).stream()
                    .filter(completion -> completion.toLowerCase().startsWith(args[args.length - 1].toLowerCase()))
                    .collect(Collectors.toList());
        }

        // Suggest subcommand completions
        for (AuroraCommand subCommand : subCommands) {
            if (subCommand.getName().equalsIgnoreCase(args[0]) || subCommand.getAliases().stream().anyMatch(alias -> alias.equalsIgnoreCase(args[0]))) {
                if (subCommand.hasPermission(sender)) {
                    return subCommand.getTabCompletions(sender, Arrays.copyOfRange(args, 1, args.length));
                }
            }
        }

        return new ArrayList<>();
    }

    /**
     * Gets the usage string for the command.
     *
     * @return The usage string.
     */
    public String getUsage() {
        StringBuilder usage = new StringBuilder();
        for (ArgumentEntry entry : arguments) {
            usage.append("<").append(entry.getName()).append("> ");
        }
        for (AuroraCommand subCommand : subCommands) {
            if (usage.length() > 0) {
                usage.append("|");
            }
            usage.append(subCommand.getName());
        }
        return usage.toString().trim();
    }

    /**
     * Gets the names of available subcommands.
     *
     * @return A string of subcommand names.
     */
    public String getSubCommandNames() {
        return subCommands.stream()
                .map(AuroraCommand::getName)
                .collect(Collectors.joining(", "));
    }

    // Getters
    public String getName() {
        return name;
    }

    public List<String> getAliases() {
        return new ArrayList<>(aliases);
    }
}