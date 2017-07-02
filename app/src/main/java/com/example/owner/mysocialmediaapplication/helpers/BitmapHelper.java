package com.example.owner.mysocialmediaapplication.helpers;

import android.content.Context;
import android.graphics.Bitmap;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

import java.io.ByteArrayOutputStream;

/**
 Helper classes for the Histogram
 */
public class BitmapHelper {

    private BitmapHelper() {
    }

    public static void showBitmap(Context context, Bitmap bitmap, ImageView imageView) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, baos);
        byte[] data = baos.toByteArray();
        Glide.with(context).load(data).into(imageView);
    }
}
