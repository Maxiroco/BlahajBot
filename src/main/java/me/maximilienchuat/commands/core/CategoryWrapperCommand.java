package me.maximilienchuat.commands.core;

import org.jetbrains.annotations.NotNull;

public class CategoryWrapperCommand extends Command implements PrefixCommand {

    private final String categoryName;
    private final CategoryCommand category;
    private final CommandRegistry registry;

    public CategoryWrapperCommand(String categoryName, CategoryCommand category, CommandRegistry registry) {
        this.categoryName = categoryName;
        this.category = category;
        this.registry = registry;
    }

    @Override
    public void executePrefix(@NotNull CommandContext ctx) {
        StringBuilder sb = new StringBuilder();
        sb.append("Commands in category **").append(categoryName).append("**:\n");
        registry.appendCategory(sb, ctx.getPrefix(), categoryName, category, "  ");
        ctx.reply(sb.toString());
    }



}
