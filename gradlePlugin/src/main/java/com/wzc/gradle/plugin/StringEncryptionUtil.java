package com.wzc.gradle.plugin;


import java.io.UnsupportedEncodingException;
import java.util.Base64;

public class StringEncryptionUtil {

   public static String encryption(String string){
      try {
         return  Base64.getEncoder().encodeToString(string.getBytes("UTF-8"));
      } catch (UnsupportedEncodingException e) {
         e.printStackTrace();
      }
      return "";
   }

}
