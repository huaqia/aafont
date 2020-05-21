package com.xinmei365.font.filter;

import android.graphics.Bitmap;
import android.opengl.GLSurfaceView;
import android.util.Log;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.egl.EGLContext;
import javax.microedition.khronos.egl.EGLDisplay;
import javax.microedition.khronos.egl.EGLSurface;
import javax.microedition.khronos.opengles.GL10;

import static javax.microedition.khronos.egl.EGL10.EGL_ALPHA_SIZE;
import static javax.microedition.khronos.egl.EGL10.EGL_BLUE_SIZE;
import static javax.microedition.khronos.egl.EGL10.EGL_DEFAULT_DISPLAY;
import static javax.microedition.khronos.egl.EGL10.EGL_DEPTH_SIZE;
import static javax.microedition.khronos.egl.EGL10.EGL_GREEN_SIZE;
import static javax.microedition.khronos.egl.EGL10.EGL_HEIGHT;
import static javax.microedition.khronos.egl.EGL10.EGL_NONE;
import static javax.microedition.khronos.egl.EGL10.EGL_NO_CONTEXT;
import static javax.microedition.khronos.egl.EGL10.EGL_RED_SIZE;
import static javax.microedition.khronos.egl.EGL10.EGL_STENCIL_SIZE;
import static javax.microedition.khronos.egl.EGL10.EGL_WIDTH;
import static javax.microedition.khronos.opengles.GL10.GL_RGBA;
import static javax.microedition.khronos.opengles.GL10.GL_UNSIGNED_BYTE;

public class PixelBuffer {
    static final String TAG = "PixelBuffer";
    static final boolean LIST_CONFIGS = false;
    GLSurfaceView.Renderer mRenderer;
    int mWidth;
    int mHeight;
    Bitmap mBitmap;
    EGL10 mEGL;
    EGLDisplay mEGLDisplay;
    EGLConfig[] mEGLConfigs;
    EGLConfig mEGLConfig;
    EGLContext mEGLContext;
    EGLSurface mEGLSurface;
    GL10 mGL;
    String mThreadOwner;

    public PixelBuffer(int width, int height) {
        this.mWidth = width;
        this.mHeight = height;
        int[] version = new int[2];
        int[] attribList = new int[]{12375, this.mWidth, 12374, this.mHeight, 12344};
        this.mEGL = (EGL10)EGLContext.getEGL();
        this.mEGLDisplay = this.mEGL.eglGetDisplay(EGL10.EGL_DEFAULT_DISPLAY);
        this.mEGL.eglInitialize(this.mEGLDisplay, version);
        this.mEGLConfig = this.chooseConfig();
        int EGL_CONTEXT_CLIENT_VERSION = 12440;
        int[] attrib_list = new int[]{EGL_CONTEXT_CLIENT_VERSION, 2, 12344};
        this.mEGLContext = this.mEGL.eglCreateContext(this.mEGLDisplay, this.mEGLConfig, EGL10.EGL_NO_CONTEXT, attrib_list);
        this.mEGLSurface = this.mEGL.eglCreatePbufferSurface(this.mEGLDisplay, this.mEGLConfig, attribList);
        this.mEGL.eglMakeCurrent(this.mEGLDisplay, this.mEGLSurface, this.mEGLSurface, this.mEGLContext);
        this.mGL = (GL10)this.mEGLContext.getGL();
        this.mThreadOwner = Thread.currentThread().getName();
    }

    public void setRenderer(GLSurfaceView.Renderer renderer) {
        this.mRenderer = renderer;
        if (!Thread.currentThread().getName().equals(this.mThreadOwner)) {
            Log.e("PixelBuffer", "setRenderer: This thread does not own the OpenGL context.");
        } else {
            this.mRenderer.onSurfaceCreated(this.mGL, this.mEGLConfig);
            this.mRenderer.onSurfaceChanged(this.mGL, this.mWidth, this.mHeight);
        }
    }

    public Bitmap getBitmap() {
        if (this.mRenderer == null) {
            Log.e("PixelBuffer", "getBitmap: Renderer was not set.");
            return null;
        } else if (!Thread.currentThread().getName().equals(this.mThreadOwner)) {
            Log.e("PixelBuffer", "getBitmap: This thread does not own the OpenGL context.");
            return null;
        } else {
            this.mRenderer.onDrawFrame(this.mGL);
            this.mRenderer.onDrawFrame(this.mGL);
            this.convertToBitmap();
            return this.mBitmap;
        }
    }

    public void destroy() {
        this.mRenderer.onDrawFrame(this.mGL);
        this.mRenderer.onDrawFrame(this.mGL);
        this.mEGL.eglMakeCurrent(this.mEGLDisplay, EGL10.EGL_NO_SURFACE, EGL10.EGL_NO_SURFACE, EGL10.EGL_NO_CONTEXT);
        this.mEGL.eglDestroySurface(this.mEGLDisplay, this.mEGLSurface);
        this.mEGL.eglDestroyContext(this.mEGLDisplay, this.mEGLContext);
        this.mEGL.eglTerminate(this.mEGLDisplay);
    }

    private EGLConfig chooseConfig() {
        int[] attribList = new int[]{12325, 0, 12326, 0, 12324, 8, 12323, 8, 12322, 8, 12321, 8, 12352, 4, 12344};
        int[] numConfig = new int[1];
        this.mEGL.eglChooseConfig(this.mEGLDisplay, attribList, (EGLConfig[])null, 0, numConfig);
        int configSize = numConfig[0];
        this.mEGLConfigs = new EGLConfig[configSize];
        this.mEGL.eglChooseConfig(this.mEGLDisplay, attribList, this.mEGLConfigs, configSize, numConfig);
        return this.mEGLConfigs[0];
    }

    private void listConfig() {
        Log.i("PixelBuffer", "Config List {");
        EGLConfig[] var4;
        int var3 = (var4 = this.mEGLConfigs).length;

        for(int var2 = 0; var2 < var3; ++var2) {
            EGLConfig config = var4[var2];
            int d = this.getConfigAttrib(config, 12325);
            int s = this.getConfigAttrib(config, 12326);
            int r = this.getConfigAttrib(config, 12324);
            int g = this.getConfigAttrib(config, 12323);
            int b = this.getConfigAttrib(config, 12322);
            int a = this.getConfigAttrib(config, 12321);
            Log.i("PixelBuffer", "    <d,s,r,g,b,a> = <" + d + "," + s + "," + r + "," + g + "," + b + "," + a + ">");
        }

        Log.i("PixelBuffer", "}");
    }

    private int getConfigAttrib(EGLConfig config, int attribute) {
        int[] value = new int[1];
        return this.mEGL.eglGetConfigAttrib(this.mEGLDisplay, config, attribute, value) ? value[0] : 0;
    }

    private void convertToBitmap() {
        IntBuffer ib = IntBuffer.allocate(this.mWidth * this.mHeight);
        IntBuffer ibt = IntBuffer.allocate(this.mWidth * this.mHeight);
        this.mGL.glReadPixels(0, 0, this.mWidth, this.mHeight, 6408, 5121, ib);

        for(int i = 0; i < this.mHeight; ++i) {
            for(int j = 0; j < this.mWidth; ++j) {
                ibt.put((this.mHeight - i - 1) * this.mWidth + j, ib.get(i * this.mWidth + j));
            }
        }

        this.mBitmap = Bitmap.createBitmap(this.mWidth, this.mHeight, Bitmap.Config.ARGB_8888);
        this.mBitmap.copyPixelsFromBuffer(ibt);
    }
}