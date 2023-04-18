package com.coldcoffee.imagevault;

import android.content.Context;

import androidx.room.Room;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;

public class DataManagement extends CryptoUtils {

    AppDB db;

    public DataManagement(Context context) {
        super(context);
        db = Room.databaseBuilder(getApplicationContext(),
                AppDB.class, "db").allowMainThreadQueries().build();
    }

    public void registerUser(User user){
        db.userDao().insertUser(new User(user.username, user.iv, user.salt));
    }

    public void encryptFile(String file, SecretKey key) throws IOException, InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException {
        EncryptedPair encryptedData = super.cryptStream(openFileInput(file), key, Cipher.ENCRYPT_MODE);
        FileOutputStream outputStream = context.openFileOutput(file, Context.MODE_PRIVATE);
        outputStream.write(encryptedData.data);
    }

    public void decryptFile(String file, SecretKey key) throws IOException, InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException {
        EncryptedPair encryptedData = super.cryptStream(openFileInput(file), key, Cipher.ENCRYPT_MODE);
        FileOutputStream outputStream = context.openFileOutput(file, Context.MODE_PRIVATE);
        outputStream.write(encryptedData.data);
    }

    public void addFile(String file, String owner,String filetype) throws IOException {

        if(openFileInput(file).read() != -1){
            db.filesDao().insertFile(new Files(context.getFilesDir().getAbsolutePath(),file,owner,filetype));
        }
    }

}
