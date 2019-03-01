package com.hanmei.aafont.utils;

import android.content.Context;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;

import java.io.File;

public final class FileUtils {
    public static File getFileDir(@NonNull Context context) {
        File[] files = null;
        try {
            files = ContextCompat.getExternalFilesDirs(context, null);
        } catch (Throwable e) {
        }
        File file;
        if (files != null && files.length > 0) {
            file = files[0];
            createFolderIfNecessary(file);
            if (isFolderExist(file)) {
                return file;
            }
        }
        file = context.getFilesDir();
        createFolderIfNecessary(file);
        return file;
    }

    public static File getFileDir(@NonNull Context context, @NonNull String dirName) {
        File dir = getFileDir(context);
        dir = new File(dir, dirName);

        createFolderIfNecessary(dir);

        return dir;
    }

    public static boolean createFolderIfNecessary(File folder) {
        if (folder != null) {
            if (!folder.exists() || !folder.isDirectory()) {
                return folder.mkdirs();
            }
            return true;
        }
        return false;
    }

    public static boolean isFileExist(File file) {
        return file != null && file.exists() && file.isFile();
    }

    public static boolean isFolderExist(File folder) {
        return folder != null && folder.exists() && folder.isDirectory();
    }

    public static String findFirstPicInSystemPhotoPath() {
        File systemPhotoDir = new File(new File(Environment.getExternalStorageDirectory(), "DCIM"), "Camera");
        if (systemPhotoDir.exists() && systemPhotoDir.isDirectory()) {
            for (File file : systemPhotoDir.listFiles()) {
                String filePath = file.getAbsolutePath();
                if (filePath.endsWith(".png") || filePath.endsWith(".jpg") || filePath.endsWith(".jpeg")) {
                    return filePath;
                }
            }
        }
        return null;
    }
}
