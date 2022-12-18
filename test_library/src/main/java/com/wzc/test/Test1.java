package com.wzc.test;

public class Test1 {
    private String string1 = "测试中文";
    private final String string2 = "test English";
    private static String string3 = "测试特殊字符@！#¥%……&*（）～+——()_="; //会生成<clinit>
    private static final String string4 = "测试换行\n换行了";
    private static final String string6 = "测试\r\n换行了";
    private static final String string5;

    static {
        string5 = "测试静态代码块"; //会生成<clinit>
    }

    @Override
    public String toString() {
        return "Test1{" + "string1='" + string1 + '\'' + ", string2='" + string2 + '\'' + ", string3='" + string3 + '\'' + ", string4='" + string4 + '\'' + ", string5='" + string5 + '\'' + ", string6='" + string6 + '\'' + '}';
    }
}
