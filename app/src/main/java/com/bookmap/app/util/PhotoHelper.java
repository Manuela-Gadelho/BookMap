package com.bookmap.app.util;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.widget.ImageView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.core.content.FileProvider;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Helper class for handling photo capture from camera and gallery selection.
 * Manages file creation, image compression, and loading into views.
 */
public class PhotoHelper {

    public static final int REQUEST_CAMERA = 2001;
    public static final int REQUEST_GALLERY = 2002;

    private static final int MAX_IMAGE_SIZE = 1024;
    private static final int COMPRESSION_QUALITY = 85;

    private final Context context;
    private String currentPhotoPath;

    public PhotoHelper(Context context) {
        this.context = context;
    }

    public String getCurrentPhotoPath() {
        return currentPhotoPath;
    }

    public void setCurrentPhotoPath(String path) {
        this.currentPhotoPath = path;
    }

    public Intent createCameraIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        File photoFile = createImageFile();
        if (photoFile != null) {
            Uri photoURI = FileProvider.getUriForFile(context,
                    context.getPackageName() + ".fileprovider", photoFile);
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
            return takePictureIntent;
        }
        return null;
    }

    public Intent createGalleryIntent() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        return intent;
    }

    public File createImageFile() {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "BOOKMAP_" + timeStamp;
        File storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        if (storageDir != null && !storageDir.exists()) {
            storageDir.mkdirs();
        }
        try {
            File image = File.createTempFile(imageFileName, ".jpg", storageDir);
            currentPhotoPath = image.getAbsolutePath();
            return image;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public String processGalleryResult(Uri imageUri) {
        if (imageUri == null) return null;
        try {
            InputStream inputStream = context.getContentResolver().openInputStream(imageUri);
            if (inputStream == null) return null;

            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
            inputStream.close();

            if (bitmap == null) return null;

            Bitmap resized = resizeBitmap(bitmap);
            File outputFile = createImageFile();
            if (outputFile == null) return null;

            FileOutputStream fos = new FileOutputStream(outputFile);
            resized.compress(Bitmap.CompressFormat.JPEG, COMPRESSION_QUALITY, fos);
            fos.close();

            if (bitmap != resized) bitmap.recycle();
            resized.recycle();

            return currentPhotoPath;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public String processCameraResult() {
        if (currentPhotoPath == null) return null;
        File file = new File(currentPhotoPath);
        if (!file.exists()) return null;

        Bitmap bitmap = BitmapFactory.decodeFile(currentPhotoPath);
        if (bitmap == null) return null;

        Bitmap resized = resizeBitmap(bitmap);
        try {
            FileOutputStream fos = new FileOutputStream(file);
            resized.compress(Bitmap.CompressFormat.JPEG, COMPRESSION_QUALITY, fos);
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (bitmap != resized) bitmap.recycle();
        resized.recycle();

        return currentPhotoPath;
    }

    public static void loadImageIntoView(ImageView imageView, String photoPath) {
        if (photoPath == null || photoPath.isEmpty()) return;
        File file = new File(photoPath);
        if (!file.exists()) return;

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(photoPath, options);

        int scaleFactor = Math.max(1,
                Math.min(options.outWidth / MAX_IMAGE_SIZE, options.outHeight / MAX_IMAGE_SIZE));

        options.inJustDecodeBounds = false;
        options.inSampleSize = scaleFactor;

        Bitmap bitmap = BitmapFactory.decodeFile(photoPath, options);
        if (bitmap != null) {
            imageView.setImageBitmap(bitmap);
        }
    }

    private Bitmap resizeBitmap(Bitmap original) {
        int width = original.getWidth();
        int height = original.getHeight();

        if (width <= MAX_IMAGE_SIZE && height <= MAX_IMAGE_SIZE) {
            return original;
        }

        float ratio = Math.min(
                (float) MAX_IMAGE_SIZE / width,
                (float) MAX_IMAGE_SIZE / height);

        int newWidth = Math.round(width * ratio);
        int newHeight = Math.round(height * ratio);

        return Bitmap.createScaledBitmap(original, newWidth, newHeight, true);
    }

    public static boolean deletePhoto(String photoPath) {
        if (photoPath == null || photoPath.isEmpty()) return false;
        File file = new File(photoPath);
        return file.exists() && file.delete();
    }
}
