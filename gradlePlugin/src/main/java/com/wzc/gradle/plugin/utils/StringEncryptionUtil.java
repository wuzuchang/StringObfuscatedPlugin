package com.wzc.gradle.plugin.utils;


import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class StringEncryptionUtil {

    public static String encryption(String string) {
        return Base64.getEncoder().encodeToString(string.getBytes(StandardCharsets.UTF_8));
    }

}
