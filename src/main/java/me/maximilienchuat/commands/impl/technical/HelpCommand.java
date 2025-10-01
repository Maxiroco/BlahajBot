package me.maximilienchuat.commands.impl.technical;

import me.maximilienchuat.commands.core.*;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

@CommandInfo(
        paths = {"help"},
        description = "List all available prefix commands",
        directPaths = {"help"}
)
public class HelpCommand extends Command implements PrefixCommand {

    @Override
    public void executePrefix(@NotNull CommandContext ctx) {
        CommandRegistry registry = ctx.getRegistry(); // use context registry
        String[] args = ctx.getArgs();
        String botPrefix = ctx.getPrefix();
        StringBuilder sb = new StringBuilder();

        if (args.length == 0) {
            sb.append("Available prefix commands:\n\n");

            // List category commands
            sb.append("List of available category commands:\n");
            for (Map.Entry<String, CategoryCommand> entry : registry.getRootCategories().entrySet()) {
                CategoryCommand cat = entry.getValue();
                if (cat.getSubcommands().isEmpty()) continue;
                sb.append("`").append(botPrefix).append(entry.getKey()).append("`\n");
            }

            // List direct commands
            sb.append("\nList of available direct commands:\n");
            for (Map.Entry<String, Command> entry : registry.getAllCommands().entrySet()) {
                Command cmd = entry.getValue();
                if (!(cmd instanceof PrefixCommand)) continue;
                CommandInfo info = cmd.getClass().getAnnotation(CommandInfo.class);
                if (info == null || info.directPaths().length == 0) continue;

                sb.append("`").append(botPrefix).append(entry.getKey()).append("` - ").append(info.description()).append("\n");
            }

            ctx.reply(sb.toString());

        } else {
            // Help for a specific category
            String categoryName = args[0];
            CategoryCommand cat = registry.getRootCategories().get(categoryName);
            if (cat == null) {
                ctx.reply("Category not found: " + categoryName);
                return;
            }

            sb.append("Commands in category **").append(categoryName).append("**:\n");
            appendCategory(sb, categoryName, cat, "  ", botPrefix);
            ctx.reply(sb.toString());
        }
    }

    // Recursive category listing with proper prefix and indentation
    private void appendCategory(StringBuilder sb, String pathSoFar, CategoryCommand cat, String indent, String prefix) {
        for (Map.Entry<String, Command> entry : cat.getSubcommands().entrySet()) {
            Command sub = entry.getValue();
            String fullPath = pathSoFar + "/" + entry.getKey();

            if (sub instanceof CategoryCommand subCat) {
                // Print category name in bold
                sb.append(indent).append("**").append(entry.getKey()).append("**\n");
                // Recurse into subcategory
                appendCategory(sb, fullPath, subCat, indent + "  ", prefix);
            } else if (sub instanceof PrefixCommand) {
                CommandInfo info = sub.getClass().getAnnotation(CommandInfo.class);
                String desc = (info != null) ? info.description() : "";

                sb.append(indent)
                        .append("`").append(prefix).append(fullPath.replace("/", " ")).append("`")
                        .append(desc.isEmpty() ? "" : " - " + desc)
                        .append("\n");
            }
        }
    }

}
