package me.maximilienchuat.commands.impl;

import me.maximilienchuat.Whatever;
import me.maximilienchuat.commands.core.*;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

@CommandInfo(
        paths = {"help"},
        description = "List all available prefix commands",
        directPaths = {"help"} // only "hug" will appear in direct commands
)
public class HelpCommand extends Command implements PrefixCommand {

    public HelpCommand() {} // required for reflections

    @Override
    public void executePrefix(@NotNull CommandContext ctx) {
        CommandRegistry registry = Whatever.getRegistry();
        String[] args = ctx.getArgs();
        StringBuilder sb = new StringBuilder();
        String botPrefix = ctx.getPrefix(); // make sure you have getPrefix() in CommandContext

        if (args.length == 0) {
            sb.append("Available prefix commands:\n\n");

            // List category commands (ignore single-path direct commands)
            sb.append("List of available category commands:\n");
            for (Map.Entry<String, CategoryCommand> entry : registry.getRootCategories().entrySet()) {
                CategoryCommand cat = entry.getValue();
                Command cmdAtRoot = cat.getSubcommands().get(entry.getKey());
                boolean isDirect = cmdAtRoot != null && cmdAtRoot.getClass().isAnnotationPresent(CommandInfo.class)
                        && cmdAtRoot.getClass().getAnnotation(CommandInfo.class).directPaths().length > 0;

                if (cat.getSubcommands().isEmpty() || isDirect) continue;
                sb.append("`").append(botPrefix).append(entry.getKey()).append("`\n"); // <-- prepend prefix here
            }

            // Direct commands
            sb.append("\nList of available direct commands:\n");
            for (Map.Entry<String, Command> entry : registry.getAllCommands().entrySet()) {
                Command cmd = entry.getValue();
                if (!(cmd instanceof PrefixCommand)) continue;
                CommandInfo info = cmd.getClass().getAnnotation(CommandInfo.class);
                if (info == null || info.directPaths().length == 0) continue;

                String desc = info.description();
                sb.append("`").append(botPrefix).append(entry.getKey()).append("` - ").append(desc).append("\n");
            }

            ctx.reply(sb.toString());

        } else {
            // Category-specific help
            String categoryName = args[0];
            CategoryCommand cat = registry.getRootCategories().get(categoryName);
            if (cat == null) {
                ctx.reply("Category not found: " + categoryName);
                return;
            }

            sb.append("Commands in category **").append(categoryName).append("**:\n");
            appendCategory(sb, botPrefix + categoryName, cat, "  ");
            ctx.reply(sb.toString());
        }
    }


    private void appendCategory(StringBuilder sb, String pathSoFar, CategoryCommand cat, String indent) {
        for (Map.Entry<String, Command> entry : cat.getSubcommands().entrySet()) {
            Command sub = entry.getValue();
            String fullPath = pathSoFar + "/" + entry.getKey();

            if (sub instanceof CategoryCommand subCat) {
                sb.append(indent).append("**").append(entry.getKey()).append("**\n");
                appendCategory(sb, fullPath, subCat, indent + "  ");
            } else if (sub instanceof PrefixCommand) {
                CommandInfo info = sub.getClass().getAnnotation(CommandInfo.class);
                String desc = (info != null) ? info.description() : "";
                sb.append(indent).append("`").append(fullPath.replace("/", " ")).append("` - ").append(desc).append("\n");
            }
        }
    }





    private void appendCategory(StringBuilder sb, String pathSoFar, CategoryCommand cat, String indent, String prefix) {
        for (Map.Entry<String, Command> entry : cat.getSubcommands().entrySet()) {
            Command sub = entry.getValue();
            String fullPath = pathSoFar.isEmpty() ? entry.getKey() : pathSoFar + "/" + entry.getKey();

            if (sub instanceof CategoryCommand subCat) {
                sb.append(indent).append("**").append(entry.getKey()).append("**\n");
                appendCategory(sb, fullPath, subCat, indent + "  ", prefix);
            } else if (sub instanceof PrefixCommand) {
                CommandInfo info = sub.getClass().getAnnotation(CommandInfo.class);
                String desc = (info != null) ? info.description() : "";
                sb.append(indent).append("`").append(prefix).append(fullPath.replace("/", " ")).append("` - ").append(desc).append("\n");
            }
        }
    }



    // Check if a command is inside any category
    private boolean isInCategory(String cmdName, CommandRegistry registry) {
        for (CategoryCommand cat : registry.getRootCategories().values()) {
            if (containsCommand(cat, cmdName)) return true;
        }
        return false;
    }

    private boolean containsCommand(CategoryCommand cat, String cmdName) {
        for (Map.Entry<String, Command> entry : cat.getSubcommands().entrySet()) {
            Command sub = entry.getValue();
            if (entry.getKey().equals(cmdName)) return true;
            if (sub instanceof CategoryCommand subCat && containsCommand(subCat, cmdName)) return true;
        }
        return false;
    }
}
