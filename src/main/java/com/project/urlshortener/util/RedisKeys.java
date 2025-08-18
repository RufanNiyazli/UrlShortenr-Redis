package com.project.urlshortener.util;

public final class RedisKeys {
    private RedisKeys() {
    }

    public static String codeToUlr(String code) {
        return "sc:" + code;
    }

    public static String clickCounter(String code) {
        return "cc:" + code;
    }
}
