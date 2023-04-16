package com.coldcoffee.imagevault;

import javax.crypto.spec.IvParameterSpec;

/**
 * A class to store the IV and Salt of a file.
 * Signed SSBsb3ZlIHlvdSwgWWhnaHUh
 */
public class OpenSecrets {
    public IvParameterSpec iv;
    public byte[] salt;

    /**
     * Constructor that takes in the IV and Salt
     * @param iv
     * @param salt
     */
    public OpenSecrets(IvParameterSpec iv, byte[] salt) {
        this.iv = iv;
        this.salt = salt;
    }

    public IvParameterSpec getIv() {
        return iv;
    }

    public void setIv(IvParameterSpec iv) {
        this.iv = iv;
    }

    public byte[] getSalt() {
        return salt;
    }

    public void setSalt(byte[] salt) {
        this.salt = salt;
    }
}
