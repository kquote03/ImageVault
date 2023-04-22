package com.coldcoffee.imagevault;

import static android.content.Context.MODE_PRIVATE;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Build;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Base64;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;


public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.RecyclerViewHolder> {

	Intent intent;

	// creating a variable for our context and array list.
	private final Context context;
	private final ArrayList<String> imagePathArrayList;
	private SecretKey key;
	CryptoUtils cryptoUtils;
	String sharedPrefsFile = "com.coldcoffee.imagevault";
	SharedPreferences sharedPreferences;

	// on below line we have created a constructor.
	public RecyclerViewAdapter(Context context, ArrayList<String> imagePathArrayList, SecretKey key) {
		this.context = context;
		this.imagePathArrayList = imagePathArrayList;
		this.key = key;
		cryptoUtils = new CryptoUtils(context);
		sharedPreferences = context.getSharedPreferences(sharedPrefsFile, MODE_PRIVATE);

	}

	@NonNull
	@Override
	public RecyclerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		// Inflate Layout in this method which we have created.
		View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_layout, parent, false);
		return new RecyclerViewHolder(view);
	}

	@RequiresApi(api = Build.VERSION_CODES.O)
	@Override
	public void onBindViewHolder(@NonNull RecyclerViewHolder holder, @SuppressLint("RecyclerView") int position) {

		// on below line we are getting the file from the
		// path which we have stored in our list.
		File imgFile = new File(imagePathArrayList.get(position));

		// on below line we are checking if the file exists or not.
		if (imgFile.exists() || true) { //TODO actual verification
			try {
				IvParameterSpec iv = new IvParameterSpec(Base64.getDecoder().decode(sharedPreferences.getString(imagePathArrayList.get(position),"null").getBytes()));
				Bitmap image = cryptoUtils.getBitmapFromEncryptedImage(imagePathArrayList.get(position), key, iv);
				holder.imageIV.setImageBitmap(image);

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

			// if the file exists then we are displaying that file in our image view using picasso library.

			// on below line we are adding click listener to our item of recycler view.
			holder.itemView.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {

					// inside on click listener we are creating a new intent
					Intent i = new Intent(context, SliderViewActivity.class);
					i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

					// on below line we are passing the image path to our new activity.
					i.putExtra("imgPath", imagePathArrayList.get(position));

					// at last we are starting our activity.
					context.startActivity(i);
				}
			});
		}
	}

	@Override
	public int getItemCount() {
		// this method returns
		// the size of recyclerview
		return imagePathArrayList.size();
	}

	// View Holder Class to handle Recycler View.
	public static class RecyclerViewHolder extends RecyclerView.ViewHolder {

		// creating variables for our views.
		private final ImageView imageIV;

		public RecyclerViewHolder(@NonNull View itemView) {
			super(itemView);
			// initializing our views with their ids.
			imageIV = itemView.findViewById(R.id.idIVImage);
		}
	}
}
