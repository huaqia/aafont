package com.xinmei365.font.utils;

import android.content.Context;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

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

    public static String readFileToString(File file) {
        InputStream in = null;
        String contents = null;
        try {
            int length = (int) file.length();
            byte[] bytes = new byte[length];
            in = new FileInputStream(file);
            in.read(bytes);
            contents = new String(bytes);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return contents;
    }

    public static void saveStringToFile(String string, File file) {
        OutputStream out = null;
        try {
            out = new FileOutputStream(file);
            out.write(string.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
