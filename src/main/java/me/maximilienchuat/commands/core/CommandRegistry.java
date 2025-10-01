package me.maximilienchuat.commands.core;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.jetbrains.annotations.NotNull;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Modifier;
import java.util.*;

public class CommandRegistry {

    public static final Logger logger = LoggerFactory.getLogger(CommandRegistry.class);

    private final Map<String, CategoryCommand> rootCategories = new HashMap<>();
    private final Map<String, Command> allCommands = new HashMap<>();

    // ---------------- Discovery ----------------
    public void discoverAndRegister(String packageName) {
        Reflections reflections = new Reflections(packageName);
        Set<Class<? extends Command>> classes = reflections.getSubTypesOf(Command.class);

        for (Class<? extends Command> clazz : classes) {
            if (Modifier.isAbstract(clazz.getModifiers())) continue;

            try {
                Command cmd = clazz.getDeclaredConstructor().newInstance();

                if (clazz.isAnnotationPresent(CommandInfo.class)) {
                    CommandInfo info = clazz.getAnnotation(CommandInfo.class);
                    for (String path : info.paths()) {
                        registerPath(path, cmd);
                    }

                    String name = info.paths()[0].split("/")[info.paths()[0].split("/").length - 1];
                    allCommands.putIfAbsent(name, cmd);

                }
            } catch (Exception e) {
                logger.error("Failed to load command: " + clazz.getSimpleName(), e);
            }
        }
    }

    private void registerPath(String path, Command cmd) {
        String[] parts = path.split("/");

        CategoryCommand current = rootCategories.computeIfAbsent(parts[0], k -> new CategoryCommand());

        // create subcategories for multi-part paths
        for (int i = 1; i < parts.length - 1; i++) {
            current = current.getOrCreateSubcategory(parts[i]);
        }

        // if this is a leaf command (last part), add the subcommand
        if (parts.length > 1) {
            current.addSubcommand(parts[parts.length - 1], cmd);
        }

        // if it's a top-level command (no subcategory), just store it in allCommands
        if (parts.length == 1) {
            allCommands.putIfAbsent(parts[0], cmd);
        }

        // register directPaths
        CommandInfo info = cmd.getClass().getAnnotation(CommandInfo.class);
        if (info != null && info.directPaths().length > 0) {
            for (String direct : info.directPaths()) {
                allCommands.putIfAbsent(direct, cmd);
            }
        }
    }



    // Simple wrapper to expose a CategoryCommand as a PrefixCommand without anonymous class
    private static class CategoryCommandWrapper extends Command implements PrefixCommand {
        private final CategoryCommand category;
        private final String name;

        public CategoryCommandWrapper(CategoryCommand category, String name) {
            this.category = category;
            this.name = name;
        }

        @Override
        public void executePrefix(@NotNull CommandContext ctx) {
            StringBuilder sb = new StringBuilder();
            sb.append("Commands in category **").append(name).append("**:\n");
            appendCategory(sb, "", category, "  ");
            ctx.reply(sb.toString());
        }

        private void appendCategory(StringBuilder sb, String pathSoFar, CategoryCommand cat, String indent) {
            for (Map.Entry<String, Command> entry : cat.getSubcommands().entrySet()) {
                Command sub = entry.getValue();
                String fullPath = pathSoFar.isEmpty() ? entry.getKey() : pathSoFar + "/" + entry.getKey();

                if (sub instanceof CategoryCommand subCat) {
                    sb.append(indent).append("**").append(entry.getKey()).append("**\n");
                    appendCategory(sb, fullPath, subCat, indent + "  ");
                } else if (sub instanceof PrefixCommand) {
                    CommandInfo info = sub.getClass().getAnnotation(CommandInfo.class);
                    String desc = (info != null) ? info.description() : "";
                    sb.append(indent).append("`").append(fullPath).append("` - ").append(desc).append("\n");
                }
            }
        }
    }

    // Original private appendCategory made public, now modified
    public void appendCategoryPublic(StringBuilder sb, String displayPath, CategoryCommand cat, String indent) {
        for (Map.Entry<String, Command> entry : cat.getSubcommands().entrySet()) {
            Command sub = entry.getValue();
            String fullDisplayPath = displayPath + " " + entry.getKey();

            if (sub instanceof CategoryCommand subCat) {
                sb.append(indent).append("**").append(entry.getKey()).append("**\n");
                appendCategoryPublic(sb, fullDisplayPath, subCat, indent + "  ");
            } else if (sub instanceof PrefixCommand) {
                sb.append(indent).append("`").append(fullDisplayPath).append("`");

                CommandInfo info = sub.getClass().getAnnotation(CommandInfo.class);
                if (info != null && !info.description().isEmpty()) {
                    sb.append(" - ").append(info.description());
                }
                sb.append("\n");
            }
        }
    }





    // ---------------- Getters ----------------
    public Map<String, CategoryCommand> getRootCategories() {
        return Collections.unmodifiableMap(rootCategories);
    }

    public Map<String, Command> getAllCommands() {
        return Collections.unmodifiableMap(allCommands);
    }

    public Command getRootCategory(String name) {
        return rootCategories.get(name);
    }

    public Command getFlatCommand(String name) {
        Command cmd = allCommands.get(name);
        return (cmd instanceof SlashCommand) ? cmd : null;
    }

    // ---------------- Type-safe prefix parsing ----------------
    public String[] parsePrefixArgs(Command cmd, String[] rawArgs, MessageReceivedEvent event) throws IllegalArgumentException {
        if (!(cmd instanceof PrefixCommand)) return rawArgs;

        CommandInfo info = cmd.getClass().getAnnotation(CommandInfo.class);
        if (info == null || info.args().length == 0) return rawArgs;

        ArgInfo[] argInfos = info.args();
        String[] parsed = new String[argInfos.length];

        for (int i = 0; i < argInfos.length; i++) {
            ArgInfo argInfo = argInfos[i];
            if (i >= rawArgs.length) {
                if (argInfo.required()) throw new IllegalArgumentException("Missing required argument: " + argInfo.name());
                parsed[i] = null;
                continue;
            }

            Object obj = parseSingleArg(rawArgs[i], argInfo.type(), event);

            if (obj instanceof User user) parsed[i] = user.getAsMention();
            else parsed[i] = obj.toString();
        }

        return parsed;
    }

    private Object parseSingleArg(String raw, Class<?> type, MessageReceivedEvent event) throws IllegalArgumentException {
        if (type == String.class) return raw;

        if (type == Integer.class) {
            try { return Integer.parseInt(raw); }
            catch (NumberFormatException e) { throw new IllegalArgumentException("Expected a number, got: " + raw); }
        }

        if (type == User.class) {
            if (!raw.matches("<@!?\\d+>")) throw new IllegalArgumentException("Invalid user mention: " + raw);
            String id = raw.replaceAll("\\D", "");
            User user = event.getJDA().getUserById(id);
            if (user == null) throw new IllegalArgumentException("User not found: " + raw);
            return user;
        }

        throw new IllegalArgumentException("Unsupported type: " + type.getSimpleName());
    }

    // ---------------- Slash command registration ----------------
    public void registerSlashCommands(JDA bot) {
        List<CommandData> list = new ArrayList<>();
        Set<String> addedNames = new HashSet<>();

        for (Command cmd : allCommands.values()) {
            CommandInfo info = cmd.getClass().getAnnotation(CommandInfo.class);
            if (info == null) continue;

            String[] pathParts = info.paths()[0].split("/");
            String name = pathParts[pathParts.length - 1];
            if (addedNames.contains(name)) continue;
            addedNames.add(name);

            SlashCommandData cd = Commands.slash(name, info.description())
                    .setNSFW(info.nsfw());

            for (SlashArg arg : info.options()) {
                cd.addOption(arg.type(), arg.name(), arg.description(), arg.required());
            }

            list.add(cd);
        }

        if (!list.isEmpty()) {
            bot.updateCommands().addCommands(list).queue(
                    success -> logger.info("Registered {} slash commands", list.size()),
                    error -> logger.error("Failed to register slash commands", error)
            );
        }
    }
}
