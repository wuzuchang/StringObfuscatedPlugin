package com.wzc.gradle.plugin;

import android.util.Log;

public class Test {

    private final static String string = "false";
    public final static boolean mSwitch = false;
    private boolean test = false;

    public void test() {

        if (test) {
            Log.d("wzc==", "switch is open");
        } else {
            Log.d("wzc==", "switch is close");
        }
    }

}
