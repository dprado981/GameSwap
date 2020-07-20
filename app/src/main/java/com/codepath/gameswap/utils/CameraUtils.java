package com.codepath.gameswap.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.ImageDecoder;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class CameraUtils {
    public static final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 101;
    public static final int PICK_IMAGE_ACTIVITY_REQUEST_CODE = 102;
    public static final int JPEG_COMPRESSION_FACTOR = 20;
    public static final String PHOTO_FILE_NAME = "photo.jpg";

    public static Bitmap adjustRotation(Bitmap bitmap, File photoFile) throws IOException {
        ExifInterface ei = new ExifInterface(photoFile.getAbsolutePath());
        int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_UNDEFINED);
        switch (orientation) {
            case ExifInterface.ORIENTATION_ROTATE_90:
                return rotateImage(bitmap, 90);
            case ExifInterface.ORIENTATION_ROTATE_180:
                return rotateImage(bitmap, 180);
            case ExifInterface.ORIENTATION_ROTATE_270:
                return rotateImage(bitmap, 270);
            case ExifInterface.ORIENTATION_NORMAL:
            default:
                return bitmap;
        }
    }

    public static Bitmap rotateImage(Bitmap source, float angle) {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(),
                matrix, true);
    }

    public static void compressBitmap(Bitmap bitmap, File targetFile) throws IOException {
        FileOutputStream fileOutputStream;
        fileOutputStream = new FileOutputStream(targetFile);
        bitmap.compress(Bitmap.CompressFormat.JPEG, CameraUtils.JPEG_COMPRESSION_FACTOR, fileOutputStream);
        fileOutputStream.flush();
        fileOutputStream.close();
    }

    public static Bitmap loadFromUri(Context context, Uri photoUri) {
        Bitmap image = null;
        try {
            // check version of Android on device
            if(Build.VERSION.SDK_INT > 27){
                // on newer versions of Android, use the new decodeBitmap method
                ImageDecoder.Source source = ImageDecoder.createSource(context.getContentResolver(), photoUri);
                image = ImageDecoder.decodeBitmap(source);
            } else {
                // support older versions of Android by using getBitmap
                image = MediaStore.Images.Media.getBitmap(context.getContentResolver(), photoUri);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return image;
    }
}
