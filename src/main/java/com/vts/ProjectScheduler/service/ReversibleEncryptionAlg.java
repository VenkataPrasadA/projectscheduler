package com.vts.ProjectScheduler.service;

import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

public class ReversibleEncryptionAlg {

	 private static final String ENCRYPTION_ALGORITHM = "AES";
	    //nvalid AES key length: 15 bytes." AES requires a key length of 16, 24, or 32 bytes. Your current key has a length of 15 characters, which means it's only 15 bytes when encoded using UTF-8.
	    private static final String ENCRYPTION_KEY = "18818Vedts795864"; // Replace with a secure key
	    private static final String CHARSET = "UTF-8";
	    
	    public String encryptByAesAlg(String plaintext) throws Exception {
	        SecretKey secretKey = new SecretKeySpec(ENCRYPTION_KEY.getBytes(CHARSET), ENCRYPTION_ALGORITHM);
	        Cipher cipher = Cipher.getInstance(ENCRYPTION_ALGORITHM);
	        cipher.init(Cipher.ENCRYPT_MODE, secretKey);
	        byte[] encryptedBytes = cipher.doFinal(plaintext.getBytes(CHARSET));
	        return Base64.getEncoder().encodeToString(encryptedBytes);
	    }

	    public String decryptByAesAlg(String encryptedText) throws Exception {
	        SecretKey secretKey = new SecretKeySpec(ENCRYPTION_KEY.getBytes(CHARSET), ENCRYPTION_ALGORITHM);
	        Cipher cipher = Cipher.getInstance(ENCRYPTION_ALGORITHM);
	        cipher.init(Cipher.DECRYPT_MODE, secretKey);
	        byte[] decryptedBytes = cipher.doFinal(Base64.getDecoder().decode(encryptedText));
	        return new String(decryptedBytes, CHARSET);
	    } 

}
