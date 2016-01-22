package org.esbtools.message.admin.common.utility;

import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;


import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Base64;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

public class EncryptionUtility {

    private static final Logger LOGGER=LoggerFactory.getLogger(EncryptionUtility.class);
    private static final String ALGORITHM = "AES/ECB/PKCS5Padding";
    private static final String FILE_ENCODING = "UTF-8";
    public static final String SECURITY_PROVIDER = "SunJCE";
    private final String encryptionKey;

    public EncryptionUtility(String key) {
        this.encryptionKey = key;
    }

    public String encrypt(String sensitiveInfo) {
        try {
            Cipher cipher = Cipher.getInstance(ALGORITHM, SECURITY_PROVIDER);
            SecretKeySpec key = new SecretKeySpec(encryptionKey.getBytes(FILE_ENCODING), "AES");
            cipher.init(Cipher.ENCRYPT_MODE, key);
            return Base64.encodeBase64String(cipher.doFinal(sensitiveInfo.getBytes(FILE_ENCODING)));
        } catch(Exception e) {
            LOGGER.error("EMA Encryption error!", e);
            return null;
        }
    }

    public String decrypt(String encryptedInfo) {
        try {
            Cipher cipher = Cipher.getInstance(ALGORITHM, SECURITY_PROVIDER);
            SecretKeySpec key = new SecretKeySpec(encryptionKey.getBytes(FILE_ENCODING), "AES");
            cipher.init(Cipher.DECRYPT_MODE, key);
            return new String(cipher.doFinal(Base64.decodeBase64(encryptedInfo.getBytes(FILE_ENCODING))), FILE_ENCODING).trim();
        } catch(Exception e) {
            LOGGER.error("EMA Decryption error!", e);
            return null;
        }
    }

}
