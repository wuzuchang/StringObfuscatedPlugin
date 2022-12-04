package cn.wzc.test;

import android.util.Log;

/**
 * @description:
 * @author: wuzuchang
 * @date: 2022/3/17
 */
public class TestString {

    private String string_ = "";
    private String string_null = null;
    private String string__;
    private String string1 = "测试中文";
    private final String string2 = "test English";
    private static String string3 = "测试特殊字符@！#¥%……&*（）～+——()_="; //会生成<clinit>
    private static final String string4 = "测试换行\n换行了";
    private static final String string6 = "测试\r\n换行了";
    private static final String string5;
    private boolean test = false;

    static {
        string5 = "测试静态代码块"; //会生成<clinit>
    }

    public String test() {
        Log.d("wzc==", "switch is open");
        return string1 + string2 + string3 + string4 + string5 + string6;
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
