package com.coldcoffee.imagevault;

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import java.io.File;
import java.util.ArrayList;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

public class GridViewActivity extends AppCompatActivity {
    private static final int PERMISSION_REQUEST_CODE = 200;
    private static final int PICKFILE_RESULT_CODE = 8778;
    private ArrayList<String> imagePaths;
    private RecyclerView imagesRV;
    private RecyclerViewAdapter imageRVAdapter;
    ViewPager viewPager;
    SecretKey key;
    Intent fileIntent;
    CryptoUtils cryptoUtils;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.recycler_view);
        Toolbar myToolbar = (Toolbar) findViewById(R.id.add_toolbar);
        //setSupportActionBar(myToolbar);

        //The file-picker intent
        fileIntent = new Intent(Intent.ACTION_GET_CONTENT);
        fileIntent.addCategory(Intent.CATEGORY_OPENABLE);
        //Filter by images only using MIME types, the * is just a wildcard here to say "any image type, I'm not racist"
        fileIntent.setType("image/*");

        key = new SecretKeySpec(Base64.getDecoder().decode(getIntent().getStringExtra("key")), "AES");
        cryptoUtils = new CryptoUtils(getApplicationContext());

        imagePaths = new ArrayList<>();
        imagesRV = findViewById(R.id.idRVImages);

        requestPermissions();
        prepareRecyclerView();
    }

    private boolean checkPermission() {
        // in this method we are checking if the permissions are granted or not and returning the result.
        int result = ContextCompat.checkSelfPermission(getApplicationContext(), READ_EXTERNAL_STORAGE);
        return result == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermissions() {
        if (checkPermission()) {
            // if the permissions are already granted we are calling
            // a method to get all images from our external storage.
            Toast.makeText(this, "Permissions granted..", Toast.LENGTH_SHORT).show();
            prepareRecyclerView();  // <--- JUST ADD THIS LINE HERE
            getImagePath();
        } else {
            // if the permissions are not granted we are
            // calling a method to request permissions.
            requestPermission();
        }
    }



    private void requestPermission() {
        //on below line we are requesting the read external storage permissions.
        ActivityCompat.requestPermissions(this, new String[]{READ_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE);
    }

    private void prepareRecyclerView() {
        // in this method we are preparing our recycler view.
        // on below line we are initializing our adapter class.
        imageRVAdapter = new RecyclerViewAdapter(getApplicationContext(), imagePaths, key);

        // on below line we are creating a new grid layout manager.
        GridLayoutManager manager = new GridLayoutManager(getApplicationContext(), 4);

        // on below line we are setting layout
        // manager and adapter to our recycler view.
        imagesRV.setLayoutManager(manager);
        imagesRV.setAdapter(imageRVAdapter);
    }


    private void getImagePath() {
        for (final File fileEntry : getFilesDir().listFiles()) {
            if(fileEntry.getName().equals("random"))
                continue;
            imagePaths.add(fileEntry.getName());
            imageRVAdapter.notifyDataSetChanged();
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        // this method is called after permissions has been granted.
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            // we are checking the permission code.
            case PERMISSION_REQUEST_CODE:
                // in this case we are checking if the permissions are accepted or not.
                if (grantResults.length > 0) {
                    boolean storageAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    if (storageAccepted) {
                        // if the permissions are accepted we are displaying a toast message
                        // and calling a method to get image path.
                        Toast.makeText(this, "Permissions Granted..", Toast.LENGTH_SHORT).show();
                        getImagePath();
                    } else {
                        // if permissions are denied we are closing the app and displaying the toast message.
                        Toast.makeText(this, "Permissions denied, Permissions are required to use the app..", Toast.LENGTH_SHORT).show();
                    }
                }
                break;
        }
    }

    public void bringUpImagePicker(View view) {
        fileIntent = Intent.createChooser(fileIntent,"Import Image");
        startActivityForResult(fileIntent, PICKFILE_RESULT_CODE);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Uri uri = data.getData();
        try {
            cryptoUtils.cipher(key, uri, new File(uri.getPath()).getName(), Cipher.ENCRYPT_MODE, null);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}