package me.maximilienchuat.commands.core;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
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
                    for (String path : info.paths()) registerPath(path, cmd);

                    // Register primary leaf in allCommands
                    String leafName = info.paths()[0].split("/")[info.paths()[0].split("/").length - 1];
                    allCommands.putIfAbsent(leafName, cmd);

                    // Register directPaths
                    for (String direct : info.directPaths()) {
                        allCommands.putIfAbsent(direct, cmd);
                    }
                }
            } catch (Exception e) {
                logger.error("Failed to load command: {}", clazz.getSimpleName(), e);
            }
        }
    }

    private void registerPath(String path, Command cmd) {
        String[] parts = path.split("/");
        CategoryCommand current = rootCategories.computeIfAbsent(parts[0], k -> new CategoryCommand());

        for (int i = 1; i < parts.length - 1; i++) {
            current = current.getOrCreateSubcategory(parts[i]);
        }

        if (parts.length > 1) current.addSubcommand(parts[parts.length - 1], cmd);
        else allCommands.putIfAbsent(parts[0], cmd);
    }

    // ---------------- Category traversal ----------------
    public void appendCategory(StringBuilder sb, String prefix, String displayPath, CategoryCommand cat, String indent) {
        for (Map.Entry<String, Command> entry : cat.getSubcommands().entrySet()) {
            Command sub = entry.getValue();
            String fullPath = displayPath.isEmpty() ? entry.getKey() : displayPath + " " + entry.getKey();

            if (sub instanceof CategoryCommand subCat) {
                sb.append(indent).append("**").append(entry.getKey()).append("**\n");
                appendCategory(sb, prefix, fullPath, subCat, indent + "  ");
            } else if (sub instanceof PrefixCommand) {
                CommandInfo info = sub.getClass().getAnnotation(CommandInfo.class);
                String desc = info != null ? info.description() : "";
                sb.append(indent).append("`").append(prefix).append(fullPath).append("`");
                if (!desc.isEmpty()) sb.append(" - ").append(desc);
                sb.append("\n");
            }
        }
    }

    // ---------------- Slash command registration ----------------
    public void registerSlashCommands(JDA bot) {
        List<CommandData> commandsList = new ArrayList<>();
        Set<String> addedNames = new HashSet<>();

        for (Command cmd : allCommands.values()) {
            if (!(cmd instanceof SlashCommand)) continue;
            CommandInfo info = cmd.getClass().getAnnotation(CommandInfo.class);
            if (info == null) continue;

            String leafName = info.paths()[0].split("/")[info.paths()[0].split("/").length - 1];
            if (addedNames.contains(leafName)) continue;
            addedNames.add(leafName);

            SlashCommandData cd = Commands.slash(leafName, info.description())
                    .setNSFW(info.nsfw());
            for (SlashArg arg : info.options()) {
                cd.addOption(arg.type(), arg.name(), arg.description(), arg.required());
            }
            commandsList.add(cd);
        }

        if (!commandsList.isEmpty()) {
            bot.updateCommands().addCommands(commandsList).queue(
                    success -> logger.info("Registered {} slash commands", commandsList.size()),
                    error -> logger.error("Failed to register slash commands", error)
            );
        }
    }

    // ---------------- Getters ----------------
    public Map<String, CategoryCommand> getRootCategories() { return Collections.unmodifiableMap(rootCategories); }
    public Map<String, Command> getAllCommands() { return Collections.unmodifiableMap(allCommands); }
    public Command getFlatCommand(String name) {
        Command cmd = allCommands.get(name);
        return (cmd instanceof SlashCommand) ? cmd : null;
    }

    // ---------------- Helper class ----------------
    public record PrefixMatchResult(Command command, int length) { }

    public PrefixMatchResult matchPrefixCommand(String[] parts) {
        Command matched = null;
        int matchedLength = 0;

        for (Command cmd : allCommands.values()) {
            if (!(cmd instanceof PrefixCommand)) continue;
            CommandInfo info = cmd.getInfo();
            if (info == null) continue;

            // Match paths
            for (String path : info.paths()) {
                String[] pathParts = path.split("/");
                if (parts.length >= pathParts.length) {
                    boolean match = true;
                    for (int i = 0; i < pathParts.length; i++) {
                        if (!parts[i].equalsIgnoreCase(pathParts[i])) {
                            match = false;
                            break;
                        }
                    }
                    if (match && pathParts.length > matchedLength) {
                        matched = cmd;
                        matchedLength = pathParts.length;
                    }
                }
            }

            // Match directPaths
            for (String direct : info.directPaths()) {
                if (parts[0].equalsIgnoreCase(direct) && 1 > matchedLength) {
                    matched = cmd;
                    matchedLength = 1;
                    break;
                }
            }
        }

        // Check root categories
        if (matched == null && rootCategories.containsKey(parts[0])) {
            CategoryCommand cat = rootCategories.get(parts[0]);
            matched = new CategoryWrapperCommand(parts[0], cat, this); // prefix can be passed later
            matchedLength = 1;
        }

        return new PrefixMatchResult(matched, matchedLength);
    }


}
