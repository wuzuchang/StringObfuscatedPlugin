package com.wzc.gradle.plugin.utils;


import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Random;

public class StringEncryptionTestUtil {


    private static int getRandomKey() {
        Random rand = new Random();
        return rand.nextInt(127);
    }

    public static void main(String[] args) {
        int key1 = getRandomKey();
        int key2 = getRandomKey();
        int key3 = getRandomKey();
        int key4 = getRandomKey();
     //   String s0 = encryption("", key1, key2, key3);
//        String s1 = encryption("wzc@123哈哈哈", key1, key2, key3);
//        String s2 = encryption("english\n中文@#¥……%……&*（）——+!@#$%^&*()123哈哈哈", key4, key2, key3);
//        System.out.println("加密后：s1=" + s1 + " s2=" + s2);
//        String d1 = decrypt(s1, key1, key2, key3);
//        String d2 = decrypt("aa1974290d703b165f013a5b", 11, 1, 76);
//        System.out.println("解密后：d1=" + d1 + " d2=" + d2);
    }


    public static String decrypt(String ciphertext, int key1, int key2, int key3) {

        try {
            int length = ciphertext.length() / 2;
            char[] hexChars = ciphertext.toCharArray();
            byte[] stringByte = new byte[length];
            for (int i = 0; i < length; i++) {
                int pos = i * 2;
                stringByte[i] = (byte) ("0123456789abcdef".indexOf(hexChars[pos]) << 4 | "0123456789abcdef".indexOf(hexChars[pos + 1]));
            }
            byte keyXor = (byte) (key1 ^ key2);
            System.out.println("解密前：" + Arrays.toString(stringByte));
            stringByte[0] = (byte) (stringByte[0] ^ key3);
            byte temp = stringByte[0]; //解密之后的数据
            for (int i = 1; i < stringByte.length; i++) {
                stringByte[i] = (byte) (stringByte[i] ^ temp ^ keyXor);
                temp = stringByte[i];
            }
            System.out.println("解密后：" + Arrays.toString(stringByte));
            return new String(stringByte, "UTF-8");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    /**
     * 字符串加密
     * String -> byte
     * 按byte加密，加密之后参与下一个字节的加密
     *
     * @param string 原始字符串
     * @param key1   key1
     * @param key2   key2
     * @param key3   key3
     * @return 加密后的字符串
     */
    public static String encryption(String string, int key1, int key2, int key3) {
        byte[] stringByte = string.getBytes(StandardCharsets.UTF_8);
        // System.out.println("string加密前：" + Arrays.toString(stringByte));
        byte keyXor = (byte) (key1 ^ key2);
        StringBuilder result = new StringBuilder();
        byte c = (byte) (stringByte[0] ^ key3);
        result.append(String.format("%02x", c));
        byte key4 = stringByte[0];
        for (int i = 1; i < stringByte.length; i++) {
            byte temp = stringByte[i];
            c = (byte) (stringByte[i] ^ key4 ^ keyXor);
            key4 = temp;
            // System.out.println("string[" + i + "]加密后：" + c + " 转16进制" + String.format("%02x", c));
            result.append(String.format("%02x", c));
        }
        return result.toString();
    }

}
