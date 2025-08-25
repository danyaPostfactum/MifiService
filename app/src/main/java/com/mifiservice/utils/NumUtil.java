package com.mifiservice.utils;

/* loaded from: classes.dex */
public class NumUtil {
    public static byte[] short2Bytes(short shortData) {
        byte[] result = {(byte) ((65280 & shortData) >> 8), (byte) (shortData & 255)};
        return result;
    }

    public static short bytes2Short(byte[] byteData) {
        return (short) ((byteData[0] << 8) | (byteData[1] & 255));
    }

    public static int bytes2Int(byte[] byteData) {
        return ((byteData[0] & 255) << 24) | ((byteData[1] & 255) << 16) | ((byteData[2] & 255) << 8) | (byteData[3] & 255);
    }

    public static byte[] int2Bytes(int num) {
        byte[] result = {(byte) (num >>> 24), (byte) (num >>> 16), (byte) (num >>> 8), (byte) num};
        return result;
    }

    public static byte[] long2Bytes(long num) {
        byte[] byteNum = new byte[8];
        for (int ix = 0; ix < 8; ix++) {
            int offset = 64 - ((ix + 1) * 8);
            byteNum[ix] = (byte) ((num >> offset) & 255);
        }
        return byteNum;
    }

    public static long bytes2Long(byte[] byteNum) {
        long num = 0;
        for (int ix = 0; ix < 8; ix++) {
            num = (num << 8) | (byteNum[ix] & 255);
        }
        return num;
    }
}