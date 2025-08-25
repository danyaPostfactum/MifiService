package com.mifiservice.utils;

import com.alibaba.fastjson.asm.Opcodes;
import java.math.BigInteger;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.spec.SecretKeySpec;
import org.apache.commons.fileupload.FileUploadBase;

/* loaded from: classes.dex */
public class CryptUtil {
    private static final String AES = "AES/ECB/PKCS5Padding";
    private static final String RSA = "RSA/ECB/PKCS1Padding";

    public static byte[] generateAESKey() {
        try {
            KeyGenerator keygen = KeyGenerator.getInstance("AES");
            SecureRandom random = new SecureRandom();
            keygen.init(Opcodes.IOR, random);
            return keygen.generateKey().getEncoded();
        } catch (NoSuchAlgorithmException e) {
            return null;
        }
    }

    public static KeyPair generateKeyPair() {
        try {
            KeyPairGenerator pairgen = KeyPairGenerator.getInstance("RSA");
            SecureRandom random = new SecureRandom();
            pairgen.initialize(FileUploadBase.MAX_HEADER_SIZE, random);
            return pairgen.generateKeyPair();
        } catch (NoSuchAlgorithmException e) {
            return null;
        }
    }

    public static byte[] aesEncrypt(byte[] content, byte[] key) throws Exception {
        SecretKeySpec keySpec = new SecretKeySpec(key, "AES");
        return encrypt(content, keySpec, AES);
    }

    public static byte[] aesDecrypt(byte[] data, byte[] key) throws Exception {
        SecretKeySpec keySpec = new SecretKeySpec(key, "AES");
        return decrypt(data, keySpec, AES);
    }

    public static byte[] rsaDecryptByPrivate(byte[] data, PrivateKey privateKey) throws Exception {
        return decrypt(data, privateKey, RSA);
    }

    public static byte[] rsaEncryptByPublicKey(byte[] data, PublicKey pubKey) throws Exception {
        return encrypt(data, pubKey, RSA);
    }

    private static byte[] encrypt(byte[] content, Key key, String algorithm) throws Exception {
        Cipher cipher = Cipher.getInstance(algorithm);
        cipher.init(1, key);
        return cipher.doFinal(content);
    }

    private static byte[] decrypt(byte[] content, Key key, String algorithm) throws Exception {
        Cipher cipher = Cipher.getInstance(algorithm);
        cipher.init(2, key);
        return cipher.doFinal(content);
    }

    public static PublicKey bytes2PublicKey(byte[] key) throws Exception {
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(key);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        PublicKey publicKey = keyFactory.generatePublic(keySpec);
        return publicKey;
    }

    public static PrivateKey bytes2PrivateKey(byte[] key) throws Exception {
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(key);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        PrivateKey privateKey = keyFactory.generatePrivate(keySpec);
        return privateKey;
    }

    public static String md5(String str) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(str.getBytes());
            return new BigInteger(1, md.digest()).toString(16);
        } catch (NoSuchAlgorithmException e) {
            return null;
        }
    }
}