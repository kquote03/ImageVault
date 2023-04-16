package com.coldcoffee.imagevault;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class User {

    public User(String username, byte[] iv, byte[] salt) {
        this.username = username;
        this.iv = iv;
        this.salt = salt;
    }

    @PrimaryKey(autoGenerate = true)
    public int uid;

    @ColumnInfo(name = "username")
    public String username;

    @ColumnInfo(name = "iv")
    public byte[] iv;

    @ColumnInfo(name = "salt")
    public byte[] salt;


}
