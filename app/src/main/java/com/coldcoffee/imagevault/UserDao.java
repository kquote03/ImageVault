package com.coldcoffee.imagevault;

import android.database.sqlite.SQLiteConstraintException;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Upsert;

@Dao
public interface UserDao {
    @Query("SELECT * FROM user WHERE username = :username")
    User findByName(String username);

    //Prevents a name collision from crashing the app
    @Insert(onConflict = OnConflictStrategy.ABORT)
    void insertUser(User user) throws SQLiteConstraintException;

    @Upsert
    void upsertUser(User user);

    @Query("SELECT salt FROM user WHERE username = :username")
    byte[] getSalt(String username) ;
}
