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

public class EncryptionUtil {

    private static final Logger LOGGER=LoggerFactory.getLogger(EncryptionUtil.class);
    private static final String ALGORITHM = "AES/ECB/PKCS5Padding";
    private static final String FILE_ENCODING = "UTF-8";
    private final String encryptionKey;

    public EncryptionUtil(String key) {
        this.encryptionKey = key;
    }

    public String encrypt(String sensitiveInfo) {
        try {
            Cipher cipher = Cipher.getInstance(ALGORITHM, "SunJCE");
            SecretKeySpec key = new SecretKeySpec(encryptionKey.getBytes(FILE_ENCODING), "AES");
            cipher.init(Cipher.ENCRYPT_MODE, key);
            return Base64.encodeBase64String(cipher.doFinal(sensitiveInfo.getBytes(FILE_ENCODING)));
        } catch(NoSuchAlgorithmException | NoSuchProviderException | NoSuchPaddingException
                | UnsupportedEncodingException | InvalidKeyException | IllegalBlockSizeException
                | BadPaddingException e) {
            LOGGER.error("EMA Encryption error!", e);
            return null;
        }
    }

    public String decrypt(String encryptedInfo) {
        try {
            Cipher cipher = Cipher.getInstance(ALGORITHM, "SunJCE");
            SecretKeySpec key = new SecretKeySpec(encryptionKey.getBytes(FILE_ENCODING), "AES");
            cipher.init(Cipher.DECRYPT_MODE, key);
            return new String(cipher.doFinal(Base64.decodeBase64(encryptedInfo.getBytes(FILE_ENCODING))), FILE_ENCODING).trim();
        } catch(NoSuchAlgorithmException | NoSuchProviderException | NoSuchPaddingException
                | UnsupportedEncodingException | InvalidKeyException | IllegalBlockSizeException
                | BadPaddingException e) {
            LOGGER.error("EMA Decryption error!", e);
            return null;
        }
    }

}
