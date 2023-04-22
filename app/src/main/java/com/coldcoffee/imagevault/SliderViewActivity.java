package com.coldcoffee.imagevault;

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;

import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.viewpager.widget.ViewPager;

import java.io.File;
import java.util.ArrayList;
import java.util.Base64;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

/**
 * The same as GridViewActivity but adapted to receive the key
 * And more importantly, starts a ViewPagerAdapter, dubbed "slider" in internal communications
 * Because it allows us to slide from one image to another or some shit idfk
 */
public class SliderViewActivity extends AppCompatActivity {
    private static final int PERMISSION_REQUEST_CODE = 200;
    private ArrayList<String> imagePaths;
    ViewPager viewPager;
    SecretKey key;
    CryptoUtils cryptoUtils;
    ViewPagerAdapter mViewPagerAdapter;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.recycler_view);

        //Get the key
        key = new SecretKeySpec(Base64.getDecoder().decode(getIntent().getStringExtra("key")), "AES");
        cryptoUtils = new CryptoUtils(getApplicationContext());
        imagePaths = new ArrayList<>();

        // Initializing the ViewPagerAdapter and adding the adapter
        viewPager = (ViewPager)findViewById(R.id.viewPagerMain);
        mViewPagerAdapter = new ViewPagerAdapter(SliderViewActivity.this, imagePaths, key);
        requestPermissions();
        viewPager.setAdapter(mViewPagerAdapter);
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



    private void getImagePath() {
        for (final File fileEntry : getFilesDir().listFiles()) {
                imagePaths.add(fileEntry.getName());
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

}