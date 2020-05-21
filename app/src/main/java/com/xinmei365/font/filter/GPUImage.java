package com.xinmei365.font.filter;

import android.annotation.TargetApi;
import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.ConfigurationInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.hardware.Camera;
import android.media.ExifInterface;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.opengl.GLSurfaceView;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.view.Display;
import android.view.WindowManager;

import com.xinmei365.font.utils.Rotation;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Iterator;
import java.util.List;

/**
 * The main accessor for GPUImage functionality. This class helps to do common
 * tasks through a simple interface.
 */
public class GPUImage {
    private final Context mContext;
    private final GPUImageRenderer mRenderer;
    private GLSurfaceView mGlSurfaceView;
    private GPUImageFilter mFilter;
    private Bitmap mCurrentBitmap;
    private GPUImage.ScaleType mScaleType;

    public GPUImage(Context context) {
        this.mScaleType = GPUImage.ScaleType.CENTER_CROP;
        if (!this.supportsOpenGLES2(context)) {
            throw new IllegalStateException("OpenGL ES 2.0 is not supported on this phone.");
        } else {
            this.mContext = context;
            this.mFilter = new GPUImageFilter();
            this.mRenderer = new GPUImageRenderer(this.mFilter);
        }
    }

    private boolean supportsOpenGLES2(Context context) {
        ActivityManager activityManager = (ActivityManager)context.getSystemService(Context.ACTIVITY_SERVICE);
        ConfigurationInfo configurationInfo = activityManager.getDeviceConfigurationInfo();
        return configurationInfo.reqGlEsVersion >= 131072;
    }

    public void setGLSurfaceView(GLSurfaceView view) {
        this.mGlSurfaceView = view;
        this.mGlSurfaceView.setEGLContextClientVersion(2);
        this.mGlSurfaceView.setEGLConfigChooser(8, 8, 8, 8, 16, 0);
        this.mGlSurfaceView.getHolder().setFormat(-2);
        this.mGlSurfaceView.setRenderer(this.mRenderer);
        this.mGlSurfaceView.setRenderMode(0);
        this.mGlSurfaceView.requestRender();
    }

    public void requestRender() {
        if (this.mGlSurfaceView != null) {
            this.mGlSurfaceView.requestRender();
        }

    }

    public void setFilter(GPUImageFilter filter) {
        this.mFilter = filter;
        this.mRenderer.setFilter(this.mFilter);
        this.requestRender();
    }

    public void setImage(Bitmap bitmap) {
        this.mCurrentBitmap = bitmap;
        this.mRenderer.setImageBitmap(bitmap, false);
        this.requestRender();
    }

    public void setScaleType(GPUImage.ScaleType scaleType) {
        this.mScaleType = scaleType;
        this.mRenderer.setScaleType(scaleType);
        this.mRenderer.deleteImage();
        this.mCurrentBitmap = null;
        this.requestRender();
    }

    public void setRotation(Rotation rotation) {
        this.mRenderer.setRotation(rotation);
    }

    public void deleteImage() {
        this.mRenderer.deleteImage();
        this.mCurrentBitmap = null;
        this.requestRender();
    }

    public void setImage(Uri uri) {
        (new GPUImage.LoadImageUriTask(this, uri)).execute(new Void[0]);
    }

    public void setImage(File file) {
        (new GPUImage.LoadImageFileTask(this, file)).execute(new Void[0]);
    }

    private String getPath(Uri uri) {
        String[] projection = new String[]{"_data"};
        Cursor cursor = this.mContext.getContentResolver().query(uri, projection, (String)null, (String[])null, (String)null);
        int pathIndex = cursor.getColumnIndexOrThrow("_data");
        String path = null;
        if (cursor.moveToFirst()) {
            path = cursor.getString(pathIndex);
        }

        cursor.close();
        return path;
    }

    public Bitmap getCurrentBitMap() {
        return this.mCurrentBitmap;
    }

    public Bitmap getBitmapWithFilterApplied() {
        return this.getBitmapWithFilterApplied(this.mCurrentBitmap);
    }

    public Bitmap getBitmapWithFilterApplied(Bitmap bitmap) {
        if (this.mGlSurfaceView != null) {
            this.mRenderer.deleteImage();
            this.mRenderer.runOnDraw(new Runnable() {
                public void run() {
                    synchronized(GPUImage.this.mFilter) {
                        GPUImage.this.mFilter.destroy();
                        GPUImage.this.mFilter.notify();
                    }
                }
            });
            synchronized(this.mFilter) {
                this.requestRender();

                try {
                    this.mFilter.wait();
                } catch (InterruptedException var5) {
                    var5.printStackTrace();
                }
            }
        }

        GPUImageRenderer renderer = new GPUImageRenderer(this.mFilter);
        renderer.setRotation(Rotation.NORMAL, this.mRenderer.isFlippedHorizontally(), this.mRenderer.isFlippedVertically());
        renderer.setScaleType(this.mScaleType);
        PixelBuffer buffer = new PixelBuffer(bitmap.getWidth(), bitmap.getHeight());
        buffer.setRenderer(renderer);
        renderer.setImageBitmap(bitmap, false);
        Bitmap result = buffer.getBitmap();
        this.mFilter.destroy();
        renderer.deleteImage();
        buffer.destroy();
        this.mRenderer.setFilter(this.mFilter);
        if (this.mCurrentBitmap != null) {
            this.mRenderer.setImageBitmap(this.mCurrentBitmap, false);
        }

        this.requestRender();
        return result;
    }

    public static void getBitmapForMultipleFilters(Bitmap bitmap, List<GPUImageFilter> filters, GPUImage.ResponseListener<Bitmap> listener) {
        if (!filters.isEmpty()) {
            GPUImageRenderer renderer = new GPUImageRenderer((GPUImageFilter)filters.get(0));
            renderer.setImageBitmap(bitmap, false);
            PixelBuffer buffer = new PixelBuffer(bitmap.getWidth(), bitmap.getHeight());
            buffer.setRenderer(renderer);
            Iterator var6 = filters.iterator();

            while(var6.hasNext()) {
                GPUImageFilter filter = (GPUImageFilter)var6.next();
                renderer.setFilter(filter);
                listener.response(buffer.getBitmap());
                filter.destroy();
            }

            renderer.deleteImage();
            buffer.destroy();
        }
    }

    /** @deprecated */
    @Deprecated
    public void saveToPictures(String folderName, String fileName, GPUImage.OnPictureSavedListener listener) {
        this.saveToPictures(this.mCurrentBitmap, folderName, fileName, listener);
    }

    /** @deprecated */
    @Deprecated
    public void saveToPictures(Bitmap bitmap, String folderName, String fileName, GPUImage.OnPictureSavedListener listener) {
        (new GPUImage.SaveTask(bitmap, folderName, fileName, listener)).execute(new Void[0]);
    }

    public void runOnGLThread(Runnable runnable) {
        this.mRenderer.runOnDrawEnd(runnable);
    }

    public int getOutputWidth() {
        if (this.mRenderer != null && this.mRenderer.getFrameWidth() != 0) {
            return this.mRenderer.getFrameWidth();
        } else if (this.mCurrentBitmap != null) {
            return this.mCurrentBitmap.getWidth();
        } else {
            WindowManager windowManager = (WindowManager)this.mContext.getSystemService(Context.WINDOW_SERVICE);
            Display display = windowManager.getDefaultDisplay();
            return display.getWidth();
        }
    }

    public int getOutputHeight() {
        if (this.mRenderer != null && this.mRenderer.getFrameHeight() != 0) {
            return this.mRenderer.getFrameHeight();
        } else if (this.mCurrentBitmap != null) {
            return this.mCurrentBitmap.getHeight();
        } else {
            WindowManager windowManager = (WindowManager)this.mContext.getSystemService(Context.WINDOW_SERVICE);
            Display display = windowManager.getDefaultDisplay();
            return display.getHeight();
        }
    }

    private class LoadImageFileTask extends GPUImage.LoadImageTask {
        private final File mImageFile;

        public LoadImageFileTask(GPUImage gpuImage, File file) {
            super(gpuImage);
            this.mImageFile = file;
        }

        protected Bitmap decode(BitmapFactory.Options options) {
            return BitmapFactory.decodeFile(this.mImageFile.getAbsolutePath(), options);
        }

        protected int getImageOrientation() throws IOException {
            ExifInterface exif = new ExifInterface(this.mImageFile.getAbsolutePath());
            int orientation = exif.getAttributeInt("Orientation", 1);
            switch(orientation) {
                case 1:
                    return 0;
                case 2:
                case 4:
                case 5:
                case 7:
                default:
                    return 0;
                case 3:
                    return 180;
                case 6:
                    return 90;
                case 8:
                    return 270;
            }
        }
    }

    private abstract class LoadImageTask extends AsyncTask<Void, Void, Bitmap> {
        private final GPUImage mGPUImage;
        private int mOutputWidth;
        private int mOutputHeight;

        public LoadImageTask(GPUImage gpuImage) {
            this.mGPUImage = gpuImage;
        }

        protected Bitmap doInBackground(Void... params) {
            if (GPUImage.this.mRenderer != null && GPUImage.this.mRenderer.getFrameWidth() == 0) {
                try {
                    synchronized(GPUImage.this.mRenderer.mSurfaceChangedWaiter) {
                        GPUImage.this.mRenderer.mSurfaceChangedWaiter.wait(3000L);
                    }
                } catch (InterruptedException var4) {
                    var4.printStackTrace();
                }
            }

            this.mOutputWidth = GPUImage.this.getOutputWidth();
            this.mOutputHeight = GPUImage.this.getOutputHeight();
            return this.loadResizedImage();
        }

        protected void onPostExecute(Bitmap bitmap) {
            super.onPostExecute(bitmap);
            this.mGPUImage.deleteImage();
            this.mGPUImage.setImage(bitmap);
        }

        protected abstract Bitmap decode(BitmapFactory.Options var1);

        private Bitmap loadResizedImage() {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            this.decode(options);

            int scale;
            for(scale = 1; this.checkSize(options.outWidth / scale > this.mOutputWidth, options.outHeight / scale > this.mOutputHeight); ++scale) {
            }

            --scale;
            if (scale < 1) {
                scale = 1;
            }

            options = new BitmapFactory.Options();
            options.inSampleSize = scale;
            options.inPreferredConfig = Bitmap.Config.RGB_565;
            options.inPurgeable = true;
            options.inTempStorage = new byte['耀'];
            Bitmap bitmap = this.decode(options);
            if (bitmap == null) {
                return null;
            } else {
                bitmap = this.rotateImage(bitmap);
                bitmap = this.scaleBitmap(bitmap);
                return bitmap;
            }
        }

        private Bitmap scaleBitmap(Bitmap bitmap) {
            int width = bitmap.getWidth();
            int height = bitmap.getHeight();
            int[] newSize = this.getScaleSize(width, height);
            Bitmap workBitmap = Bitmap.createScaledBitmap(bitmap, newSize[0], newSize[1], true);
            if (workBitmap != bitmap) {
                bitmap.recycle();
                bitmap = workBitmap;
                System.gc();
            }

            if (GPUImage.this.mScaleType == GPUImage.ScaleType.CENTER_CROP) {
                int diffWidth = newSize[0] - this.mOutputWidth;
                int diffHeight = newSize[1] - this.mOutputHeight;
                workBitmap = Bitmap.createBitmap(bitmap, diffWidth / 2, diffHeight / 2, newSize[0] - diffWidth, newSize[1] - diffHeight);
                if (workBitmap != bitmap) {
                    bitmap.recycle();
                    bitmap = workBitmap;
                }
            }

            return bitmap;
        }

        private int[] getScaleSize(int width, int height) {
            float withRatio = (float)width / (float)this.mOutputWidth;
            float heightRatio = (float)height / (float)this.mOutputHeight;
            boolean adjustWidth = GPUImage.this.mScaleType == GPUImage.ScaleType.CENTER_CROP ? withRatio > heightRatio : withRatio < heightRatio;
            float newWidth;
            float newHeight;
            if (adjustWidth) {
                newHeight = (float)this.mOutputHeight;
                newWidth = newHeight / (float)height * (float)width;
            } else {
                newWidth = (float)this.mOutputWidth;
                newHeight = newWidth / (float)width * (float)height;
            }

            return new int[]{Math.round(newWidth), Math.round(newHeight)};
        }

        private boolean checkSize(boolean widthBigger, boolean heightBigger) {
            if (GPUImage.this.mScaleType == GPUImage.ScaleType.CENTER_CROP) {
                return widthBigger && heightBigger;
            } else {
                return widthBigger || heightBigger;
            }
        }

        private Bitmap rotateImage(Bitmap bitmap) {
            if (bitmap == null) {
                return null;
            } else {
                Bitmap rotatedBitmap = bitmap;

                try {
                    int orientation = this.getImageOrientation();
                    if (orientation != 0) {
                        Matrix matrix = new Matrix();
                        matrix.postRotate((float)orientation);
                        rotatedBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
                        bitmap.recycle();
                    }
                } catch (IOException var5) {
                    var5.printStackTrace();
                }

                return rotatedBitmap;
            }
        }

        protected abstract int getImageOrientation() throws IOException;
    }

    private class LoadImageUriTask extends GPUImage.LoadImageTask {
        private final Uri mUri;

        public LoadImageUriTask(GPUImage gpuImage, Uri uri) {
            super(gpuImage);
            this.mUri = uri;
        }

        protected Bitmap decode(BitmapFactory.Options options) {
            try {
                InputStream inputStream;
                if (!this.mUri.getScheme().startsWith("http") && !this.mUri.getScheme().startsWith("https")) {
                    inputStream = GPUImage.this.mContext.getContentResolver().openInputStream(this.mUri);
                } else {
                    inputStream = (new URL(this.mUri.toString())).openStream();
                }

                return BitmapFactory.decodeStream(inputStream, (Rect)null, options);
            } catch (Exception var3) {
                var3.printStackTrace();
                return null;
            }
        }

        protected int getImageOrientation() throws IOException {
            Cursor cursor = GPUImage.this.mContext.getContentResolver().query(this.mUri, new String[]{"orientation"}, (String)null, (String[])null, (String)null);
            if (cursor != null && cursor.getCount() == 1) {
                cursor.moveToFirst();
                int orientation = cursor.getInt(0);
                cursor.close();
                return orientation;
            } else {
                return 0;
            }
        }
    }

    public interface OnPictureSavedListener {
        void onPictureSaved(Uri var1);
    }

    public interface ResponseListener<T> {
        void response(T var1);
    }

    /** @deprecated */
    @Deprecated
    private class SaveTask extends AsyncTask<Void, Void, Void> {
        private final Bitmap mBitmap;
        private final String mFolderName;
        private final String mFileName;
        private final GPUImage.OnPictureSavedListener mListener;
        private final Handler mHandler;

        public SaveTask(Bitmap bitmap, String folderName, String fileName, GPUImage.OnPictureSavedListener listener) {
            this.mBitmap = bitmap;
            this.mFolderName = folderName;
            this.mFileName = fileName;
            this.mListener = listener;
            this.mHandler = new Handler();
        }

        protected Void doInBackground(Void... params) {
            Bitmap result = GPUImage.this.getBitmapWithFilterApplied(this.mBitmap);
            this.saveImage(this.mFolderName, this.mFileName, result);
            return null;
        }

        private void saveImage(String folderName, String fileName, Bitmap image) {
            File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
            File file = new File(path, folderName + "/" + fileName);

            try {
                file.getParentFile().mkdirs();
                image.compress(Bitmap.CompressFormat.JPEG, 80, new FileOutputStream(file));
                MediaScannerConnection.scanFile(GPUImage.this.mContext, new String[]{file.toString()}, (String[])null, new MediaScannerConnection.OnScanCompletedListener() {
                    public void onScanCompleted(String path, final Uri uri) {
                        if (SaveTask.this.mListener != null) {
                            SaveTask.this.mHandler.post(new Runnable() {
                                public void run() {
                                    SaveTask.this.mListener.onPictureSaved(uri);
                                }
                            });
                        }

                    }
                });
            } catch (FileNotFoundException var7) {
                var7.printStackTrace();
            }

        }
    }

    public static enum ScaleType {
        CENTER_INSIDE,
        CENTER_CROP;

        private ScaleType() {
        }
    }
}