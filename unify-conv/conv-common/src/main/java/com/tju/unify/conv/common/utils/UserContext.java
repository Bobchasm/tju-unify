package com.tju.unify.conv.common.utils;


public class UserContext {
    private static final ThreadLocal<String> USERNAME_THREAD_LOCAL = new ThreadLocal<>();

    private static final ThreadLocal<String> TOKEN_THREAD_LOCAL = new ThreadLocal<>();

    public static void setToken(String token) {
        TOKEN_THREAD_LOCAL.set(token);
    }

    public static String getToken() { return TOKEN_THREAD_LOCAL.get(); }

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
