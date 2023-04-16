package com.coldcoffee.imagevault;

import androidx.room.Database;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

@TypeConverters(Converter.class)
@Database(entities = {User.class}, version = 1)
public abstract class AppDB extends RoomDatabase {
    public abstract UserDao userDao();
}
