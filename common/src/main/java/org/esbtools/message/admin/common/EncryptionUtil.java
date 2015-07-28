package org.esbtools.message.admin.common;

import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.util.logging.Logger;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Base64;

public class EncryptionUtil {

    private final static Logger log = Logger.getLogger(EncryptionUtil.class.getName());
    private static final String ALGORITHM = "AES/ECB/PKCS5Padding";
    private final String encryptionKey;
    public EncryptionUtil(String key) {
        this.encryptionKey = key;
    }

    public String encrypt(String sensitiveInfo) {
        try {
            Cipher cipher = Cipher.getInstance(ALGORITHM, "SunJCE");
            SecretKeySpec key = new SecretKeySpec(encryptionKey.getBytes("UTF-8"), "AES");
            cipher.init(Cipher.ENCRYPT_MODE, key);
            return Base64.encodeBase64String(cipher.doFinal(sensitiveInfo.getBytes("UTF-8")));
        } catch(NoSuchAlgorithmException | NoSuchProviderException | NoSuchPaddingException
                | UnsupportedEncodingException | InvalidKeyException | IllegalBlockSizeException
                | BadPaddingException e) {
            log.severe("EMA Encryption error!"+e);
            return null;
        }
    }

    public String decrypt(String encryptedInfo) {
        try {
            Cipher cipher = Cipher.getInstance(ALGORITHM, "SunJCE");
            SecretKeySpec key = new SecretKeySpec(encryptionKey.getBytes("UTF-8"), "AES");
            cipher.init(Cipher.DECRYPT_MODE, key);
            return new String(cipher.doFinal(Base64.decodeBase64(encryptedInfo.getBytes("UTF-8")))).trim();
        } catch(NoSuchAlgorithmException | NoSuchProviderException | NoSuchPaddingException
                | UnsupportedEncodingException | InvalidKeyException | IllegalBlockSizeException
                | BadPaddingException e) {
            log.severe("EMA Decryption error!"+e);
            return null;
        }
    }

}
