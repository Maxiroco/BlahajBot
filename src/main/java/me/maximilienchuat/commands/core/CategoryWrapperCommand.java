package me.maximilienchuat.commands.core;

import org.jetbrains.annotations.NotNull;

public class CategoryWrapperCommand extends Command implements PrefixCommand {

    private final String categoryName;
    private final CategoryCommand category;
    private final CommandRegistry registry;
    private final String displayPrefix;

    public CategoryWrapperCommand(String categoryName, CategoryCommand category, CommandRegistry registry) {
        this(categoryName, category, registry, "b."); // default display prefix
    }

    public CategoryWrapperCommand(String categoryName, CategoryCommand category, CommandRegistry registry, String displayPrefix) {
        this.categoryName = categoryName;
        this.category = category;
        this.registry = registry;
        this.displayPrefix = displayPrefix;
    }

    @Override
    public void executePrefix(@NotNull CommandContext ctx) {
        StringBuilder sb = new StringBuilder();
        sb.append("Commands in category **").append(categoryName).append("**:\n");
        registry.appendCategoryPublic(sb, displayPrefix + categoryName, category, "  "); // pass full prefix here
        ctx.reply(sb.toString());
    }
}

