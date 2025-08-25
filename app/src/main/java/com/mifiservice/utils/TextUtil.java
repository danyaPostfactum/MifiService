package com.mifiservice.utils;

import com.alibaba.fastjson.asm.Opcodes;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.fileupload.FileUploadBase;
import org.eclipse.jetty.http.HttpVersions;

/* loaded from: classes.dex */
public class TextUtil {
    public static boolean isEmpty(String str) {
        return str == null || HttpVersions.HTTP_0_9.equals(str.trim());
    }

    public static boolean isEmpty(byte[] b) {
        return b == null || b.length == 0;
    }

    public static boolean isEmpty(String... strs) {
        if (strs == null || strs.length == 0) {
            return true;
        }
        for (String str : strs) {
            if (isEmpty(str)) {
                return true;
            }
        }
        return false;
    }

    public static final String md5(String s) {
        char[] hexDigits = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};
        try {
            byte[] btInput = s.getBytes();
            MessageDigest mdInst = MessageDigest.getInstance("MD5");
            mdInst.update(btInput);
            byte[] md = mdInst.digest();
            int j = md.length;
            char[] str = new char[j * 2];
            int k = 0;
            for (byte byte0 : md) {
                int k2 = k + 1;
                str[k] = hexDigits[(byte0 >>> 4) & 15];
                k = k2 + 1;
                str[k2] = hexDigits[byte0 & 15];
            }
            return new String(str);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static boolean isPhoneNumber(String str) {
        if (isEmpty(str)) {
            return false;
        }
        Pattern pattern = Pattern.compile("^[1][358]\\d{9}$");
        Matcher matcher = pattern.matcher(str);
        return matcher.matches();
    }

    public static boolean isEmail(String str) {
        if (isEmpty(str)) {
            return false;
        }
        Pattern pattern = Pattern.compile("^\\w+@(\\w+.)+[a-z]{2,3}$");
        Matcher matcher = pattern.matcher(str);
        return matcher.matches();
    }

    public static boolean isPasswordFormated(String password) {
        if (isEmpty(password)) {
            return false;
        }
        Pattern p = Pattern.compile("^(?![0-9]+$)(?![a-zA-Z]+$)[0-9A-Za-z!@#$%^&*]{8,16}$");
        Matcher m = p.matcher(password);
        return m.matches();
    }

    public static String formatTime(long time) {
        Date date = new Date(time);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd  HH:mm:ss", Locale.getDefault());
        return sdf.format(date);
    }

    public static String getCurrentTime(String format) {
        Date date = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat(format, Locale.getDefault());
        String currentTime = sdf.format(date);
        return currentTime;
    }

    public static String getCurrentTime() {
        return getCurrentTime("yyyy-MM-dd  HH:mm:ss");
    }

    public static String calcMd5(File file) {

        // TODO try-catch mess
        return "0";/*
        if (!file.isFile()) {
            return null;
        }
        FileInputStream in = null;
        byte[] buffer = new byte[FileUploadBase.MAX_HEADER_SIZE];
        try {
            try {
                MessageDigest digest = MessageDigest.getInstance("MD5");
                FileInputStream in2 = new FileInputStream(file);
                while (true) {
                    try {
                        int len = in2.read(buffer, 0, FileUploadBase.MAX_HEADER_SIZE);
                        if (len != -1) {
                            digest.update(buffer, 0, len);
                        } else {
                            try {
                                break;
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    } catch (Exception e2) {
                        e = e2;
                        in = in2;
                        e.printStackTrace();
                        try {
                            in.close();
                            return null;
                        } catch (IOException e3) {
                            e3.printStackTrace();
                            return null;
                        }
                    } catch (Throwable th) {
                        th = th;
                        in = in2;
                        try {
                            in.close();
                        } catch (IOException e4) {
                            e4.printStackTrace();
                        }
                        throw th;
                    }
                }
                in2.close();
                BigInteger bigInt = new BigInteger(1, digest.digest());
                return bigInt.toString(16);
            } catch (Throwable th2) {
                th = th2;
            }
        } catch (Exception e5) {
            e = e5;
        }*/
    }

    private static byte CRC8(byte[] buffer) {
        int crci = 255;
        for (byte b : buffer) {
            crci ^= b & 255;
            for (int i = 0; i < 8; i++) {
                if ((crci & 1) != 0) {
                    crci = (crci >> 1) ^ Opcodes.INVOKESTATIC;
                } else {
                    crci >>= 1;
                }
            }
        }
        return (byte) crci;
    }
}