package com.coldcoffee.imagevault;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Index;

@Entity(indices = {@Index(value = {"filepath"},unique = true)})
public class Files {

    public Files(String filepath, String filename, String owner, String filetype) {
        this.filepath = filepath;
        this.filename = filename;
        this.owner = owner;
        this.filetype = filetype;
    }

    @ColumnInfo(name = "filepath")
    private String filepath;

    @ColumnInfo(name = "filename")
    private String filename;

    @ColumnInfo(name = "owner")
    private String owner;

    @ColumnInfo(name = "filetype")
    private String filetype;
}
