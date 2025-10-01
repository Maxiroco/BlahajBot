package me.maximilienchuat.commands.core;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface ArgInfo {
    String name();
    Class<?> type() default String.class; // e.g., String.class, Integer.class, User.class
    boolean required() default true;
}

