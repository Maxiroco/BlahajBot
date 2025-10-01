package me.maximilienchuat.commands.core;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface CommandInfo {
    String[] paths();
    boolean nsfw() default false;
    String description() default "";
    SlashArg[] options() default {};
    ArgInfo[] args() default {};
    String[] directPaths() default {}; // <-- NEW
}



