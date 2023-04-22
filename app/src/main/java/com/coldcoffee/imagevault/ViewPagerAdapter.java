package com.coldcoffee.imagevault;

import static android.content.Context.MODE_PRIVATE;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.viewpager.widget.PagerAdapter;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Objects;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;

class ViewPagerAdapter extends PagerAdapter {

    // Context object
    Context context;

    // Array of images
    ArrayList<String> images;

    // Layout Inflater
    LayoutInflater mLayoutInflater;
    SecretKey key;
    CryptoUtils cryptoUtils;
    String sharedPrefsFile = "com.coldcoffee.imagevault";
    SharedPreferences sharedPreferences;


    // Viewpager Constructor
    public ViewPagerAdapter(Context context, ArrayList<String> images, SecretKey key) {
        this.context = context;
        this.images = images;
        this.key = key;
        cryptoUtils = new CryptoUtils(context);
        mLayoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        sharedPreferences = context.getSharedPreferences(sharedPrefsFile, MODE_PRIVATE);
    }

    @Override
    public int getCount() {
        // return the number of images
        return images.size();
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view.equals(object);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, final int position) {
        // inflating the item.xml
        View itemView = mLayoutInflater.inflate(R.layout.card_layout, container, false);

        // referencing the image view from the item.xml file
        ImageView imageView = (ImageView) itemView.findViewById(R.id.idIVImage);

        // setting the image in the imageView
        try {
            IvParameterSpec iv = new IvParameterSpec(Base64.getDecoder().decode(sharedPreferences.getString(images.get(position),"null").getBytes()));
            imageView.setImageBitmap(cryptoUtils.getBitmapFromEncryptedImage(images.get(position), key,iv));
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (InvalidAlgorithmParameterException e) {
            throw new RuntimeException(e);
        } catch (NoSuchPaddingException e) {
            throw new RuntimeException(e);
        } catch (IllegalBlockSizeException e) {
            throw new RuntimeException(e);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        } catch (BadPaddingException e) {
            throw new RuntimeException(e);
        } catch (InvalidKeyException e) {
            throw new RuntimeException(e);
        }
        imageView.setOnTouchListener(new View.OnTouchListener() {
            private float mScaleFactor = 1.0f;
            ScaleGestureDetector scaleGestureDetector = new ScaleGestureDetector(context.getApplicationContext(), new ScaleListener());

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                scaleGestureDetector.onTouchEvent(event);
                return true;
            }
            class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
                // on below line we are creating a class for our scale
                // listener and  extending it with gesture listener.
                @Override
                public boolean onScale(ScaleGestureDetector scaleGestureDetector) {

                    // inside on scale method we are setting scale
                    // for our image in our image view.
                    mScaleFactor *= scaleGestureDetector.getScaleFactor();
                    mScaleFactor = Math.max(0.1f, Math.min(mScaleFactor, 10.0f));

                    // on below line we are setting
                    // scale x and scale y to our image view.
                    imageView.setScaleX(mScaleFactor);
                    imageView.setScaleY(mScaleFactor);
                    return true;
                }
            }

        });

        // Adding the View
        Objects.requireNonNull(container).addView(itemView);

        return itemView;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((LinearLayout) object);
    }


}
