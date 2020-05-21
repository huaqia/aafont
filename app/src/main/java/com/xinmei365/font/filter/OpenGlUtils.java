package com.xinmei365.font.filter;

import android.graphics.Bitmap;
import android.hardware.Camera;
import android.opengl.GLES20;
import android.opengl.GLUtils;
import android.util.Log;

import java.nio.IntBuffer;

public class OpenGlUtils {
    public static final int NO_TEXTURE = -1;

    public OpenGlUtils() {
    }

    public static int loadTexture(Bitmap img, int usedTexId) {
        return loadTexture(img, usedTexId, true);
    }

    public static int loadTexture(Bitmap img, int usedTexId, boolean recycle) {
        int[] textures = new int[1];
        if (usedTexId == -1) {
            GLES20.glGenTextures(1, textures, 0);
            GLES20.glBindTexture(3553, textures[0]);
            GLES20.glTexParameterf(3553, 10240, 9729.0F);
            GLES20.glTexParameterf(3553, 10241, 9729.0F);
            GLES20.glTexParameterf(3553, 10242, 33071.0F);
            GLES20.glTexParameterf(3553, 10243, 33071.0F);
            GLUtils.texImage2D(3553, 0, img, 0);
        } else {
            GLES20.glBindTexture(3553, usedTexId);
            GLUtils.texSubImage2D(3553, 0, 0, 0, img);
            textures[0] = usedTexId;
        }

        if (recycle) {
            img.recycle();
        }

        return textures[0];
    }

    public static int loadTexture(IntBuffer data, Camera.Size size, int usedTexId) {
        int[] textures = new int[1];
        if (usedTexId == -1) {
            GLES20.glGenTextures(1, textures, 0);
            GLES20.glBindTexture(3553, textures[0]);
            GLES20.glTexParameterf(3553, 10240, 9729.0F);
            GLES20.glTexParameterf(3553, 10241, 9729.0F);
            GLES20.glTexParameterf(3553, 10242, 33071.0F);
            GLES20.glTexParameterf(3553, 10243, 33071.0F);
            GLES20.glTexImage2D(3553, 0, 6408, size.width, size.height, 0, 6408, 5121, data);
        } else {
            GLES20.glBindTexture(3553, usedTexId);
            GLES20.glTexSubImage2D(3553, 0, 0, 0, size.width, size.height, 6408, 5121, data);
            textures[0] = usedTexId;
        }

        return textures[0];
    }

    public static int loadTextureAsBitmap(IntBuffer data, Camera.Size size, int usedTexId) {
        Bitmap bitmap = Bitmap.createBitmap(data.array(), size.width, size.height, Bitmap.Config.ARGB_8888);
        return loadTexture(bitmap, usedTexId);
    }

    public static int loadShader(String strSource, int iType) {
        int[] compiled = new int[1];
        int iShader = GLES20.glCreateShader(iType);
        GLES20.glShaderSource(iShader, strSource);
        GLES20.glCompileShader(iShader);
        GLES20.glGetShaderiv(iShader, 35713, compiled, 0);
        if (compiled[0] == 0) {
            Log.d("Load Shader Failed", "Compilation\n" + GLES20.glGetShaderInfoLog(iShader));
            return 0;
        } else {
            return iShader;
        }
    }

    public static int loadProgram(String strVSource, String strFSource) {
        int[] link = new int[1];
        int iVShader = loadShader(strVSource, 35633);
        if (iVShader == 0) {
            Log.d("Load Program", "Vertex Shader Failed");
            return 0;
        } else {
            int iFShader = loadShader(strFSource, 35632);
            if (iFShader == 0) {
                Log.d("Load Program", "Fragment Shader Failed");
                return 0;
            } else {
                int iProgId = GLES20.glCreateProgram();
                GLES20.glAttachShader(iProgId, iVShader);
                GLES20.glAttachShader(iProgId, iFShader);
                GLES20.glLinkProgram(iProgId);
                GLES20.glGetProgramiv(iProgId, 35714, link, 0);
                if (link[0] <= 0) {
                    Log.d("Load Program", "Linking Failed");
                    return 0;
                } else {
                    GLES20.glDeleteShader(iVShader);
                    GLES20.glDeleteShader(iFShader);
                    return iProgId;
                }
            }
        }
    }

    public static float rnd(float min, float max) {
        float fRandNum = (float)Math.random();
        return min + (max - min) * fRandNum;
    }
}