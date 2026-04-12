package com.tju.unify.conv.common.utils;


public class UserContext {
    private static final ThreadLocal<String> USERNAME_THREAD_LOCAL = new ThreadLocal<>();

    public static void setUsername(String username) {
        USERNAME_THREAD_LOCAL.set(username);
    }

    public static String getUsername() {
        return USERNAME_THREAD_LOCAL.get();
    }

    public static void clear() {
        USERNAME_THREAD_LOCAL.remove();
    }
}
