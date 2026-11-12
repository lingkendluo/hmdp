package com.hmdp.utils;

import java.util.regex.Pattern;

/**
 * 正则校验工具
 */
public class RegexUtils {

    private static final Pattern PHONE_PATTERN = Pattern.compile("^1([38][0-9]|4[579]|5[0-3,5-9]|6[6]|7[0135678]|9[89])\\d{8}$");

    private static final Pattern CODE_PATTERN = Pattern.compile("^\\d{6}$");

    private static final Pattern VERIFY_CODE_PATTERN = Pattern.compile("^[0-9]{6}$");

    /**
     * 手机号格式是否非法
     */
    public static boolean isPhoneInvalid(String phone) {
        if (phone == null) {
            return true;
        }
        return !PHONE_PATTERN.matcher(phone).matches();
    }

    /**
     * 验证码格式是否非法
     */
    public static boolean isCodeInvalid(String code) {
        if (code == null) {
            return true;
        }
        return !CODE_PATTERN.matcher(code).matches();
    }

    public static boolean isVerifyCodeInvalid(String code) {
        return code == null || !VERIFY_CODE_PATTERN.matcher(code).matches();
    }
}
