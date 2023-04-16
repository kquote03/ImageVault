package com.coldcoffee.imagevault;

import androidx.room.TypeConverter;

import javax.crypto.spec.IvParameterSpec;

public class Converter {
    @TypeConverter
    public byte[] fromIvParameterSpec(IvParameterSpec iv){
        return iv.getIV();
    }
    public IvParameterSpec toIvParameterSpec(byte[] iv){
        return new IvParameterSpec(iv);
    }
}
