package me.maximilienchuat.commands;

import java.util.*;


public class CommandsRegistry {
    private final Map<String, Command> commands = new HashMap<>();

    public CommandsRegistry() {
        // Initialize all commands here
        commands.put("hello", new HelloCommand());
        commands.put("roll", new RollCommand());

        CategoryCommand money = new CategoryCommand();
        // money.add("wallet", new WalletCommand());
        // money.add("bank", new BankCommand());
        commands.put("money", money);
    }

    public Command get(String name) {
        return commands.get(name);
    }

    public Map<String, Command> getAll() {
        return Collections.unmodifiableMap(commands);
    }
}

