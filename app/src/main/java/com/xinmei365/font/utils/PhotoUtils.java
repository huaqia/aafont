package com.xinmei365.font.utils;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.media.MediaScannerConnection;
import android.os.Environment;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class PhotoUtils {
    public PhotoUtils() {
    }

    public static Bitmap getBitmap(String filePath) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(filePath, options);
        options.inSampleSize = 1;
        options.inJustDecodeBounds = false;
        Bitmap bm = BitmapFactory.decodeFile(filePath, options);
        if (bm == null) {
            return null;
        } else {
            int degree = readPictureDegree(filePath);
            bm = rotateBitmap(bm, degree);
            return bm;
        }
    }

    private static int readPictureDegree(String path) {
        short degree = 0;

        try {
            ExifInterface exifInterface = new ExifInterface(path);
            int orientation = exifInterface.getAttributeInt("Orientation", 1);
            switch(orientation) {
                case 3:
                    degree = 180;
                case 4:
                case 5:
                case 7:
                default:
                    break;
                case 6:
                    degree = 90;
                    break;
                case 8:
                    degree = 270;
            }
        } catch (IOException var4) {
            var4.printStackTrace();
        }

        return degree;
    }

    private static Bitmap rotateBitmap(Bitmap bitmap, int rotate) {
        if (bitmap == null) {
            return null;
        } else {
            int w = bitmap.getWidth();
            int h = bitmap.getHeight();
            Matrix mtx = new Matrix();
            mtx.postRotate((float)rotate);
            return Bitmap.createBitmap(bitmap, 0, 0, w, h, mtx, true);
        }
    }

    public static String getApplicationName(Context mContext) {
        PackageManager packageManager = null;
        ApplicationInfo applicationInfo = null;

        try {
            packageManager = mContext.getApplicationContext().getPackageManager();
            applicationInfo = packageManager.getApplicationInfo(mContext.getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException var4) {
            applicationInfo = null;
        }

        String applicationName = (String)packageManager.getApplicationLabel(applicationInfo);
        return applicationName;
    }

    public static String saveAsBitmap(Context context, Bitmap bitmap) {
        String folderName = getApplicationName(context);
        if (folderName == null || folderName.equals("")) {
            folderName = "CameraSDK";
        }

        File parentpath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        String fileName = System.currentTimeMillis() + ".jpg";
        fileName = folderName + "/" + fileName;
        File file = new File(parentpath, fileName);
        file.getParentFile().mkdirs();

        try {
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, new FileOutputStream(file));
            MediaScannerConnection.scanFile(context, new String[]{file.toString()}, (String[])null, (MediaScannerConnection.OnScanCompletedListener)null);
        } catch (FileNotFoundException var7) {
            var7.printStackTrace();
        }

        bitmap.recycle();
        return file.getAbsolutePath();
    }

    public static Bitmap rotateImage(Bitmap bit, int degrees) {
        Matrix matrix = new Matrix();
        matrix.postRotate((float)degrees);
        Bitmap tempBitmap = Bitmap.createBitmap(bit, 0, 0, bit.getWidth(), bit.getHeight(), matrix, true);
        return tempBitmap;
    }

    public static Bitmap reverseImage(Bitmap bit, int x, int y) {
        Matrix matrix = new Matrix();
        matrix.postScale((float)x, (float)y);
        Bitmap tempBitmap = Bitmap.createBitmap(bit, 0, 0, bit.getWidth(), bit.getHeight(), matrix, true);
        return tempBitmap;
    }

    public static Bitmap ResizeBitmap(Bitmap bitmap, int scale) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        Matrix matrix = new Matrix();
        matrix.postScale((float)(1 / scale), (float)(1 / scale));
        Bitmap resizedBitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, true);
        bitmap.recycle();
        return resizedBitmap;
    }
}