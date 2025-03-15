package com.example.vcmessenger;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class ImageUtils {
    static AppCompatActivity context;
    public static Bitmap rotateBitmapIfRequired(Bitmap bitmap, Uri uri) throws IOException {
        InputStream input = context.getContentResolver().openInputStream(uri);
        ExifInterface exif = new ExifInterface(input);
        int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);

        Log.d("ImageRotation", "EXIF Orientation: " + orientation);

        switch (orientation) {
            case ExifInterface.ORIENTATION_ROTATE_90:
                return rotateBitmap(bitmap, 90);
            case ExifInterface.ORIENTATION_ROTATE_180:
                return rotateBitmap(bitmap, 180);
            case ExifInterface.ORIENTATION_ROTATE_270:
                return rotateBitmap(bitmap, 270);
            default:
                return bitmap;
        }
    }

    public static Uri getPickImageResultUri(Intent data, AppCompatActivity context) {
        try {
            ImageUtils.context = context;
            Uri imageURI = data.getData();
            Bitmap originalBitmap = MediaStore.Images.Media.getBitmap(context.getContentResolver(), imageURI);

            Bitmap correctedBitmap = rotateBitmapIfRequired(originalBitmap, imageURI);

            int maxWidth = 800;
            int maxHeight = 800;
            Bitmap resizedBitmap = resizeBitmap(correctedBitmap, maxWidth, maxHeight);

            byte[] compressedImageData = compressImage(resizedBitmap);

            File tempFile = saveImageToPrivateStorage(compressedImageData);

            imageURI = Uri.fromFile(tempFile);
            return imageURI;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }


    private static Bitmap rotateBitmap(Bitmap bitmap, int degrees) {
        Matrix matrix = new Matrix();
        matrix.postRotate(degrees);
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    }
    private static File saveImageToPrivateStorage(byte[] compressedImageData) {
        File tempFile = null;
        try {
            tempFile = File.createTempFile("profile", "jpg", context.getCacheDir());
            tempFile.deleteOnExit();

            FileOutputStream fos = new FileOutputStream(tempFile);
            fos.write(compressedImageData);
            fos.flush();
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return tempFile;
    }
    private static Bitmap resizeBitmap(Bitmap originalBitmap, int maxWidth, int maxHeight) {
        int width = originalBitmap.getWidth();
        int height = originalBitmap.getHeight();
        float aspectRatio = (float) width / height;

        if (width > height) {
            width = maxWidth;
            height = (int) (width / aspectRatio);
        } else {
            height = maxHeight;
            width = (int) (height * aspectRatio);
        }

        return Bitmap.createScaledBitmap(originalBitmap, width, height, false);
    }

    private static byte[] compressImage(Bitmap bitmap) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        int quality = 100;
        bitmap.compress(Bitmap.CompressFormat.JPEG, quality, baos);


        while (baos.toByteArray().length > 600 * 1024) {
            baos.reset();
            quality -= 10;
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, baos);
        }
        return baos.toByteArray();
    }
}
