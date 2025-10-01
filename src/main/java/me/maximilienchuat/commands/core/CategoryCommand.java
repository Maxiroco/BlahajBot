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
}
