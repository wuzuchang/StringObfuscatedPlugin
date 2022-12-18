package com.wzc.gradle.plugin.utils;

public class LogUtils {
    private static boolean openLog = true;

    public static void setOpenLog(boolean debug){
        openLog = debug;
    }

    public static void d(String message) {
        if (!openLog) {
            return;
        }
        System.out.println(message);
    }
}
