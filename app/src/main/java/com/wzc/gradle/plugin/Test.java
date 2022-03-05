package com.wzc.gradle.plugin;


import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;

import java.util.Base64;

public class Test {

    private String string1 = "1111";
    private final String string2 = "2222";
    private static String string3 = "3333"; //会生成<clinit>
    private static final String string4 = "4444";
    private static final String string6 = "6666";
    private static final String string5;
    private boolean test = false;

    static {
        string5 = "5555"; //会生成<clinit>
    }

    public String test() {
        if (test) {
            Log.d("wzc==", "switch is open");
        }
        return string1 + string2 + string3;
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

    @RequiresApi(api = Build.VERSION_CODES.O)
    private static String stringDecrypt(String value, int key) {
        if (value == null) {
            return null;
        }
        return new String(Base64.getDecoder().decode(value));
    }
}
