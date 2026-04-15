package com.yuriyuri.util;

import java.security.SecureRandom;
import java.util.Random;

public class CaptchaUtil {

    private static final Random RANDOM = new SecureRandom();

    public static String generateCode(int length) {
         StringBuilder sb = new StringBuilder();
         for (int i = 0; i < length; i++) {
             sb.append(RANDOM.nextInt(10));
         }
         return sb.toString();
    }
}
