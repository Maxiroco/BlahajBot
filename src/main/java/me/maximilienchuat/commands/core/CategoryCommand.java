package me.maximilienchuat.commands.core;

import java.util.*;

public class CategoryCommand extends Command {
    private final Map<String, Command> subcommands = new HashMap<>();

    public void addSubcommand(String name, Command cmd) {
        subcommands.put(name, cmd);
    }

    public CategoryCommand getOrCreateSubcategory(String name) {
        Command cmd = subcommands.get(name);
        if (cmd instanceof CategoryCommand cat) return cat;

        CategoryCommand newCat = new CategoryCommand();
        subcommands.put(name, newCat);
        return newCat;
    }

    public Map<String, Command> getSubcommands() {
        return Collections.unmodifiableMap(subcommands);
    }

    public CategoryCommand getSubcategory(String name) {
        Command cmd = subcommands.get(name);
        if (cmd instanceof CategoryCommand cat) return cat;
        return null;
    }

    // New traversal for prefix commands
    public void executePrefix(CommandContext ctx) {
        String[] args = ctx.getArgs();
        if (args.length == 0) return;

        Command sub = subcommands.get(args[0]);
        if (sub instanceof PrefixCommand pc) {
            String[] remaining = Arrays.copyOfRange(args, 1, args.length);
            pc.executePrefix(new CommandContext(ctx.getMessageEvent(), remaining, ctx.getPrefix()));
        }
    }

    // New traversal for slash commands
    public void executeSlash(CommandContext ctx) {
        for (Command sub : subcommands.values()) {
            if (sub instanceof SlashCommand sc) {
                sc.executeSlash(ctx);
            }
        }
    }
}
