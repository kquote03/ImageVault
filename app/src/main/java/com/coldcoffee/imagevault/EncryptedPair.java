package com.coldcoffee.imagevault;

import javax.crypto.spec.IvParameterSpec;

public class EncryptedPair {
    byte[] data;
    IvParameterSpec iv;

    public EncryptedPair(byte[] data, IvParameterSpec iv) {
        this.data = data;
        this.iv = iv;
    }
}
