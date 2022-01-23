package com.wzc.gradle.plugin;

import android.util.Log;

public class Test {

    private String string1 = "1111";
    private final String string2 = "2222";
    private static String string3 = "3333";
    private static final String string4 = "4444";
    private static final String string6 = "6666";
    private static final String string5;
    private boolean test = false;

    static {
        string5 = "5555";
    }

    public void test() {
        if (test) {
            Log.d("wzc==", "switch is open");
        }
    }

    public void test(String a) {
        if (test) {
            Log.d("wzc==", "switch is open");
        }
    }

    class InnerClassTest {
        private int i;
        private String s;

        private void doSth(String p) {
            p = "hahah";
            Log.d("wzc==", p);
        }
    }
}
