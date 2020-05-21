package com.xinmei365.font.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;

import com.xinmei365.font.filter.GPUImage;
import com.xinmei365.font.filter.GPUImageFilter;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.egl.EGLContext;
import javax.microedition.khronos.egl.EGLDisplay;

public class BitmapUtils {
    public static Bitmap ensureBitmapSize(Bitmap origin) {
        int maxSize = getMaxTextureSize();
        Bitmap resizeBitmap;
        if (origin != null && (origin.getWidth() > maxSize || origin.getHeight() > maxSize)) {
            try {
                if (origin.getWidth() > origin.getHeight()) {
                    resizeBitmap = Bitmap.createScaledBitmap(origin, maxSize, origin.getHeight() * maxSize / origin.getWidth(), true);
                } else {
                    resizeBitmap = Bitmap.createScaledBitmap(origin, origin.getWidth() * maxSize / origin.getHeight(), maxSize, true);
                }
                return resizeBitmap;
            } catch (OutOfMemoryError error) {
                origin = null;
            }
        }
        return origin;
    }

    public static int getMaxTextureSize() {
        // Safe minimum default size
        final int IMAGE_MAX_BITMAP_DIMENSION = 2048;

        // Get EGL Display
        EGL10 egl = (EGL10) EGLContext.getEGL();
        EGLDisplay display = egl.eglGetDisplay(EGL10.EGL_DEFAULT_DISPLAY);

        // Initialise
        int[] version = new int[2];
        egl.eglInitialize(display, version);

        // Query total number of configurations
        int[] totalConfigurations = new int[1];
        egl.eglGetConfigs(display, null, 0, totalConfigurations);

        // Query actual list configurations
        EGLConfig[] configurationsList = new EGLConfig[totalConfigurations[0]];
        egl.eglGetConfigs(display, configurationsList, totalConfigurations[0], totalConfigurations);

        int[] textureSize = new int[1];
        int maximumTextureSize = 0;

        // Iterate through all the configurations to located the maximum texture size
        for (int i = 0; i < totalConfigurations[0]; i++) {
            // Only need to check for width since opengl textures are always squared
            egl.eglGetConfigAttrib(display, configurationsList[i], EGL10.EGL_MAX_PBUFFER_WIDTH, textureSize);

            // Keep track of the maximum texture size
            if (maximumTextureSize < textureSize[0])
                maximumTextureSize = textureSize[0];
        }

        // Release
        egl.eglTerminate(display);

        // Return largest texture size found, or default
        return Math.max(maximumTextureSize, IMAGE_MAX_BITMAP_DIMENSION);
    }

    public static void saveBitmap(Context context, Bitmap bitmap, GPUImageFilter filter, File file) {
        GPUImage gpuImage = new GPUImage(context);
        gpuImage.setFilter(filter);
        bitmap = ensureBitmapSize(bitmap);
        gpuImage.setImage(bitmap);
        bitmap = gpuImage.getBitmapWithFilterApplied();
        FileOutputStream output = null;
        try {
            output = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, output);
            output.flush();
            bitmap.recycle();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (null != output) {
                try {
                    output.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static interface LoadImageCallback {
        public void callback(Bitmap result);
    }

    public static void asyncLoadImage(Context context, Uri imageUri, LoadImageCallback callback) {
        new LoadImageUriTask(context, imageUri, callback).execute();
    }

    private static class LoadImageUriTask extends AsyncTask<Void, Void, Bitmap> {
        private final Uri         imageUri;
        private final Context     context;
        private LoadImageCallback callback;

        public LoadImageUriTask(Context context, Uri imageUri, LoadImageCallback callback) {
            this.imageUri = imageUri;
            this.context = context;
            this.callback = callback;
        }

        @Override
        protected Bitmap doInBackground(Void... params) {
            try {
                InputStream inputStream;
                if (imageUri.getScheme().startsWith("http")
                        || imageUri.getScheme().startsWith("https")) {
                    inputStream = new URL(imageUri.toString()).openStream();
                } else {
                    inputStream = context.getContentResolver().openInputStream(imageUri);
                }
                return BitmapFactory.decodeStream(inputStream);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Bitmap result) {
            super.onPostExecute(result);
            callback.callback(result);
        }
    }

    //异步加载缩略图
    public static void asyncLoadSmallImage(Context context, Uri imageUri, LoadImageCallback callback) {
        new LoadSmallPicTask(context, imageUri, callback).execute();
    }

    private static class LoadSmallPicTask extends AsyncTask<Void, Void, Bitmap> {

        private final Uri         imageUri;
        private final Context     context;
        private LoadImageCallback callback;

        public LoadSmallPicTask(Context context, Uri imageUri, LoadImageCallback callback) {
            this.imageUri = imageUri;
            this.context = context;
            this.callback = callback;
        }

        @Override
        protected Bitmap doInBackground(Void... params) {
            return getResizedBitmap(context, imageUri, 100, 100);
        }

        @Override
        protected void onPostExecute(Bitmap result) {
            super.onPostExecute(result);
            callback.callback(result);
        }

    }

    //得到指定大小的Bitmap对象
    public static Bitmap getResizedBitmap(Context context, Uri imageUri, int width, int height) {
        InputStream inputStream = null;
        try {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;

            inputStream = context.getContentResolver().openInputStream(imageUri);
            BitmapFactory.decodeStream(inputStream, null, options);

            options.outWidth = width;
            options.outHeight = height;
            options.inJustDecodeBounds = false;
            try {
                if (inputStream != null)
                    inputStream.close();
            } catch (IOException e) {
            }
            inputStream = context.getContentResolver().openInputStream(imageUri);
            return BitmapFactory.decodeStream(inputStream, null, options);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (inputStream != null)
                    inputStream.close();
            } catch (IOException e) {
            }
        }
        return null;
    }

    public static float getBitmapRatio(String path) {
        if (path == null) {
            return 0;
        }
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, options);
        return (float)options.outHeight / options.outWidth;
    }
}