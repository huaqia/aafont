package com.xinmei365.font.ui.widget;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;
import android.widget.ProgressBar;

import com.xinmei365.font.filter.GPUImage;
import com.xinmei365.font.filter.GPUImage.ScaleType;
import com.xinmei365.font.filter.GPUImageFilter;
import com.xinmei365.font.utils.PhotoUtils;
import com.xinmei365.font.utils.Rotation;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.nio.IntBuffer;
import java.util.concurrent.Semaphore;

@SuppressLint({"NewApi"})
public class GPUImageView extends FrameLayout {
    private GLSurfaceView mGLSurfaceView;
    private GPUImage mGPUImage;
    private GPUImageFilter mFilter;
    public GPUImageView.Size mForceSize = null;
    private float mRatio = 0.0F;
    private OnMeasureListener mMeasureListener;
    private ScaleType mScaleType;

    public GPUImageView(Context context) {
        super(context);
        this.init(context, (AttributeSet)null);
    }

    public GPUImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        this.mGLSurfaceView = new GPUImageView.GPUImageGLSurfaceView(context, attrs);
        this.addView(this.mGLSurfaceView);
        this.mGPUImage = new GPUImage(this.getContext());
        this.mGPUImage.setGLSurfaceView(this.mGLSurfaceView);
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (this.mRatio != 0.0F) {
            int newHeight;
            int newWidth;
            if (mScaleType == ScaleType.CENTER_INSIDE) {
                int width = this.mGPUImage.getOutputWidth();
                int height = this.mGPUImage.getOutputHeight();
                if ((float) width / this.mRatio < (float) height) {
                    newWidth = width;
                    newHeight = Math.round((float) width / this.mRatio);
                } else {
                    newHeight = height;
                    newWidth = Math.round((float) height * this.mRatio);
                }
            } else {
                int width = MeasureSpec.getSize(widthMeasureSpec);
                int height = MeasureSpec.getSize(heightMeasureSpec);
                if ((float) width / this.mRatio > (float) height) {
                    newWidth = width;
                    newHeight = Math.round((float) width / this.mRatio);
                } else {
                    newHeight = height;
                    newWidth = Math.round((float) height * this.mRatio);
                }
            }

            int newWidthSpec = MeasureSpec.makeMeasureSpec(newWidth, MeasureSpec.EXACTLY);
            int newHeightSpec = MeasureSpec.makeMeasureSpec(newHeight, MeasureSpec.EXACTLY);
            super.onMeasure(newWidthSpec, newHeightSpec);
            if (mMeasureListener != null) {
                mMeasureListener.onMeasure(newWidth, newHeight);
            }
        } else {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        }

    }

    public void setOnMeasureListener(OnMeasureListener listener) {
        mMeasureListener = listener;
    }

    public interface OnMeasureListener {
        void onMeasure(int width, int height);
    }

    public GPUImage getGPUImage() {
        return this.mGPUImage;
    }

    public void setRatio(float ratio) {
        this.mRatio = ratio;
        this.mGLSurfaceView.requestLayout();
        this.mGPUImage.deleteImage();
    }

    public void setScaleType(ScaleType scaleType) {
        mScaleType = scaleType;
        this.mGPUImage.setScaleType(scaleType);
    }

    public void setRotation(Rotation rotation) {
        this.mGPUImage.setRotation(rotation);
        this.requestRender();
    }

    public void setFilter(GPUImageFilter filter) {
        if (filter == null) {
            filter = new GPUImageFilter();
        }

        this.mFilter = filter;
        this.mGPUImage.setFilter(filter);
        this.requestRender();
    }

    public GPUImageFilter getFilter() {
        return this.mFilter;
    }

    public void setImage(String path) {
        this.mGPUImage.deleteImage();
        (new GPUImageView.LoadImageTask(path)).execute(new Void[0]);
    }

    public Bitmap getCurrentBitMap() {
        return this.mGPUImage.getCurrentBitMap();
    }

    public void setImage(Bitmap bitmap) {
        this.mGPUImage.setImage(bitmap);
    }

    public void setImage(Uri uri) {
        this.mGPUImage.setImage(uri);
    }

    public void setImage(File file) {
        this.mGPUImage.setImage(file);
    }

    public void requestRender() {
        this.mGLSurfaceView.requestRender();
    }

    public void saveToPictures(String folderName, String fileName, GPUImageView.OnPictureSavedListener listener) {
        (new GPUImageView.SaveTask(folderName, fileName, listener)).execute(new Void[0]);
    }

    public void saveToPictures(String folderName, String fileName, int width, int height, GPUImageView.OnPictureSavedListener listener) {
        (new GPUImageView.SaveTask(folderName, fileName, width, height, listener)).execute(new Void[0]);
    }

    public Bitmap capture(int width, int height) throws InterruptedException {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            throw new IllegalStateException("Do not call this method from the UI thread!");
        } else {
            this.mForceSize = new GPUImageView.Size(width, height);
            final Semaphore waiter = new Semaphore(0);
            this.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                public void onGlobalLayout() {
                    if (Build.VERSION.SDK_INT < 16) {
                        GPUImageView.this.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                    } else {
                        GPUImageView.this.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    }

                    waiter.release();
                }
            });
            this.post(new Runnable() {
                public void run() {
                    GPUImageView.this.addView(GPUImageView.this.new LoadingView(GPUImageView.this.getContext()));
                    GPUImageView.this.mGLSurfaceView.requestLayout();
                }
            });
            waiter.acquire();
            this.mGPUImage.runOnGLThread(new Runnable() {
                public void run() {
                    waiter.release();
                }
            });
            this.requestRender();
            waiter.acquire();
            Bitmap bitmap = this.capture();
            this.mForceSize = null;
            this.post(new Runnable() {
                public void run() {
                    GPUImageView.this.mGLSurfaceView.requestLayout();
                }
            });
            this.requestRender();
            this.postDelayed(new Runnable() {
                public void run() {
                    GPUImageView.this.removeViewAt(1);
                }
            }, 300L);
            return bitmap;
        }
    }

    public Bitmap capture() throws InterruptedException {
        final Semaphore waiter = new Semaphore(0);
        final int width = this.mGLSurfaceView.getMeasuredWidth();
        final int height = this.mGLSurfaceView.getMeasuredHeight();
        final int[] pixelMirroredArray = new int[width * height];
        this.mGPUImage.runOnGLThread(new Runnable() {
            public void run() {
                IntBuffer pixelBuffer = IntBuffer.allocate(width * height);
                GLES20.glReadPixels(0, 0, width, height, 6408, 5121, pixelBuffer);
                int[] pixelArray = pixelBuffer.array();

                for(int i = 0; i < height; ++i) {
                    for(int j = 0; j < width; ++j) {
                        pixelMirroredArray[(height - i - 1) * width + j] = pixelArray[i * width + j];
                    }
                }

                waiter.release();
            }
        });
        this.requestRender();
        waiter.acquire();
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        bitmap.copyPixelsFromBuffer(IntBuffer.wrap(pixelMirroredArray));
        return bitmap;
    }

    public void onPause() {
        this.mGLSurfaceView.onPause();
    }

    public void onResume() {
        this.mGLSurfaceView.onResume();
    }

    private class GPUImageGLSurfaceView extends GLSurfaceView {
        public GPUImageGLSurfaceView(Context context) {
            super(context);
        }

        public GPUImageGLSurfaceView(Context context, AttributeSet attrs) {
            super(context, attrs);
        }

        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            if (GPUImageView.this.mForceSize != null) {
                super.onMeasure(MeasureSpec.makeMeasureSpec(GPUImageView.this.mForceSize.width, MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(GPUImageView.this.mForceSize.height, MeasureSpec.EXACTLY));
            } else {
                super.onMeasure(widthMeasureSpec, heightMeasureSpec);
            }

        }
    }

    private class LoadImageTask extends AsyncTask<Void, Void, Bitmap> {
        private String mPath;

        public LoadImageTask(String path) {
            this.mPath = path;
        }

        protected Bitmap doInBackground(Void... params) {
            return PhotoUtils.getBitmap(this.mPath);
        }

        protected void onPostExecute(Bitmap bitmap) {
            super.onPostExecute(bitmap);
            if (bitmap != null) {
                float width = (float) bitmap.getWidth();
                float height = (float) bitmap.getHeight();
                float ratio = width / height;
                GPUImageView.this.setRatio(ratio);
                GPUImageView.this.setImage(bitmap);
            }
        }
    }

    private class LoadingView extends FrameLayout {
        public LoadingView(Context context) {
            super(context);
            this.init();
        }

        public LoadingView(Context context, AttributeSet attrs) {
            super(context, attrs);
            this.init();
        }

        public LoadingView(Context context, AttributeSet attrs, int defStyle) {
            super(context, attrs, defStyle);
            this.init();
        }

        private void init() {
            ProgressBar view = new ProgressBar(this.getContext());
            view.setLayoutParams(new LayoutParams(-2, -2, 17));
            this.addView(view);
            this.setBackgroundColor(-1);
        }
    }

    public interface OnPictureSavedListener {
        void onPictureSaved(Uri var1);
    }

    private class SaveTask extends AsyncTask<Void, Void, Void> {
        private final String mFolderName;
        private final String mFileName;
        private final int mWidth;
        private final int mHeight;
        private final GPUImageView.OnPictureSavedListener mListener;
        private final Handler mHandler;

        public SaveTask(String folderName, String fileName, GPUImageView.OnPictureSavedListener listener) {
            this(folderName, fileName, 0, 0, listener);
        }

        public SaveTask(String folderName, String fileName, int width, int height, GPUImageView.OnPictureSavedListener listener) {
            this.mFolderName = folderName;
            this.mFileName = fileName;
            this.mWidth = width;
            this.mHeight = height;
            this.mListener = listener;
            this.mHandler = new Handler();
        }

        protected Void doInBackground(Void... params) {
            try {
                Bitmap result = this.mWidth != 0 ? GPUImageView.this.capture(this.mWidth, this.mHeight) : GPUImageView.this.capture();
                this.saveImage(this.mFolderName, this.mFileName, result);
            } catch (InterruptedException var3) {
                var3.printStackTrace();
            }

            return null;
        }

        private void saveImage(String folderName, String fileName, Bitmap image) {
            File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
            File file = new File(path, folderName + "/" + fileName);

            try {
                file.getParentFile().mkdirs();
                image.compress(Bitmap.CompressFormat.JPEG, 80, new FileOutputStream(file));
                MediaScannerConnection.scanFile(GPUImageView.this.getContext(), new String[]{file.toString()}, (String[])null, new MediaScannerConnection.OnScanCompletedListener() {
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

    public static class Size {
        int width;
        int height;

        public Size(int width, int height) {
            this.width = width;
            this.height = height;
        }
    }
}