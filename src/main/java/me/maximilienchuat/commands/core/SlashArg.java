package me.maximilienchuat.commands.core;

import net.dv8tion.jda.api.interactions.commands.OptionType;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface SlashArg {
    String name();
    String description();
    OptionType type() default OptionType.STRING;
    boolean required() default false;
}