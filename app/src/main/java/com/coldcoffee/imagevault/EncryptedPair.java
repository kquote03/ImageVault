package com.coldcoffee.imagevault;

import javax.crypto.spec.IvParameterSpec;

/**
 * A simple class to group the possibly encrypted data with an IV.
 * Was much easier than f###ing around with Java streams to append the IV.
 */
public class EncryptedPair {
    byte[] data;
    IvParameterSpec iv;

    /**
     * The main constructor
     * @param data The Encrypted/Decrypted data
     * @param iv The generated or stored IV
     */
    public EncryptedPair(byte[] data, IvParameterSpec iv) {
        this.data = data;
        this.iv = iv;
    }
}