package me.maximilienchuat.commands.core;

public abstract class Command {
    // Shared utilities, metadata access, etc.
    // Example helper:
    public CommandInfo getInfo() {
        return this.getClass().getAnnotation(CommandInfo.class);
    }
}