package com.coldcoffee.imagevault;

import android.database.sqlite.SQLiteConstraintException;

import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Upsert;

public interface FilesDao {
    @Query("SELECT * FROM files WHERE filename = :filename")
    Files findFile(String filename);

    //Prevents a name collision from crashing the app
    @Insert(onConflict = OnConflictStrategy.ABORT)
    void insertFile(Files file) throws SQLiteConstraintException;
}
