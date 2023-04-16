package com.coldcoffee.imagevault;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

@Dao
public interface UserDao {
    @Query("SELECT * FROM user WHERE username = :username")
    User findByName(String username);

    @Insert
    void insertUser(User user);
}
