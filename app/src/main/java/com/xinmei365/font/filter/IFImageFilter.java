package com.xinmei365.font.filter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;

import java.util.ArrayList;
import java.util.List;

public class IFImageFilter extends GPUImageFilter {
    private int filterInputTextureUniform2;
    private int filterInputTextureUniform3;
    private int filterInputTextureUniform4;
    private int filterInputTextureUniform5;
    private int filterInputTextureUniform6;
    public int filterSourceTexture2 = -1;
    public int filterSourceTexture3 = -1;
    public int filterSourceTexture4 = -1;
    public int filterSourceTexture5 = -1;
    public int filterSourceTexture6 = -1;
    private List<Integer> mResIds;
    private Context mContext;

    public IFImageFilter(Context context, String fragmentShaderString) {
        super("attribute vec4 position;\nattribute vec4 inputTextureCoordinate;\n \nvarying vec2 textureCoordinate;\n \nvoid main()\n{\n    gl_Position = position;\n    textureCoordinate = inputTextureCoordinate.xy;\n}", fragmentShaderString);
        this.mContext = context;
    }

    public void onInit() {
        super.onInit();
        this.filterInputTextureUniform2 = GLES20.glGetUniformLocation(this.getProgram(), "inputImageTexture2");
        this.filterInputTextureUniform3 = GLES20.glGetUniformLocation(this.getProgram(), "inputImageTexture3");
        this.filterInputTextureUniform4 = GLES20.glGetUniformLocation(this.getProgram(), "inputImageTexture4");
        this.filterInputTextureUniform5 = GLES20.glGetUniformLocation(this.getProgram(), "inputImageTexture5");
        this.filterInputTextureUniform6 = GLES20.glGetUniformLocation(this.getProgram(), "inputImageTexture6");
        this.initInputTexture();
    }

    public void onDestroy() {
        super.onDestroy();
        int[] arrayOfInt5;
        if (this.filterSourceTexture2 != -1) {
            arrayOfInt5 = new int[]{this.filterSourceTexture2};
            GLES20.glDeleteTextures(1, arrayOfInt5, 0);
            this.filterSourceTexture2 = -1;
        }

        if (this.filterSourceTexture3 != -1) {
            arrayOfInt5 = new int[]{this.filterSourceTexture3};
            GLES20.glDeleteTextures(1, arrayOfInt5, 0);
            this.filterSourceTexture3 = -1;
        }

        if (this.filterSourceTexture4 != -1) {
            arrayOfInt5 = new int[]{this.filterSourceTexture4};
            GLES20.glDeleteTextures(1, arrayOfInt5, 0);
            this.filterSourceTexture4 = -1;
        }

        if (this.filterSourceTexture5 != -1) {
            arrayOfInt5 = new int[]{this.filterSourceTexture5};
            GLES20.glDeleteTextures(1, arrayOfInt5, 0);
            this.filterSourceTexture5 = -1;
        }

        if (this.filterSourceTexture6 != -1) {
            arrayOfInt5 = new int[]{this.filterSourceTexture6};
            GLES20.glDeleteTextures(1, arrayOfInt5, 0);
            this.filterSourceTexture6 = -1;
        }

    }

    protected void onDrawArraysPre() {
        super.onDrawArraysPre();
        if (this.filterSourceTexture2 != -1) {
            GLES20.glActiveTexture(33987);
            GLES20.glBindTexture(3553, this.filterSourceTexture2);
            GLES20.glUniform1i(this.filterInputTextureUniform2, 3);
        }

        if (this.filterSourceTexture3 != -1) {
            GLES20.glActiveTexture(33988);
            GLES20.glBindTexture(3553, this.filterSourceTexture3);
            GLES20.glUniform1i(this.filterInputTextureUniform3, 4);
        }

        if (this.filterSourceTexture4 != -1) {
            GLES20.glActiveTexture(33989);
            GLES20.glBindTexture(3553, this.filterSourceTexture4);
            GLES20.glUniform1i(this.filterInputTextureUniform4, 5);
        }

        if (this.filterSourceTexture5 != -1) {
            GLES20.glActiveTexture(33990);
            GLES20.glBindTexture(3553, this.filterSourceTexture5);
            GLES20.glUniform1i(this.filterInputTextureUniform5, 6);
        }

        if (this.filterSourceTexture6 != -1) {
            GLES20.glActiveTexture(33991);
            GLES20.glBindTexture(3553, this.filterSourceTexture6);
            GLES20.glUniform1i(this.filterInputTextureUniform6, 7);
        }

    }

    public void addInputTexture(int resId) {
        if (this.mResIds == null) {
            this.mResIds = new ArrayList();
        }

        this.mResIds.add(resId);
    }

    public void initInputTexture() {
        if (this.mResIds != null) {
            if (this.mResIds.size() > 0) {
                this.runOnDraw(new Runnable() {
                    public void run() {
                        Bitmap b = BitmapFactory.decodeResource(IFImageFilter.this.mContext.getResources(), (Integer)IFImageFilter.this.mResIds.get(0));
                        IFImageFilter.this.filterSourceTexture2 = OpenGlUtils.loadTexture(b, -1, true);
                    }
                });
            }

            if (this.mResIds.size() > 1) {
                this.runOnDraw(new Runnable() {
                    public void run() {
                        Bitmap b = BitmapFactory.decodeResource(IFImageFilter.this.mContext.getResources(), (Integer)IFImageFilter.this.mResIds.get(1));
                        IFImageFilter.this.filterSourceTexture3 = OpenGlUtils.loadTexture(b, -1, true);
                    }
                });
            }

            if (this.mResIds.size() > 2) {
                this.runOnDraw(new Runnable() {
                    public void run() {
                        Bitmap b = BitmapFactory.decodeResource(IFImageFilter.this.mContext.getResources(), (Integer)IFImageFilter.this.mResIds.get(2));
                        IFImageFilter.this.filterSourceTexture4 = OpenGlUtils.loadTexture(b, -1, true);
                    }
                });
            }

            if (this.mResIds.size() > 3) {
                this.runOnDraw(new Runnable() {
                    public void run() {
                        Bitmap b = BitmapFactory.decodeResource(IFImageFilter.this.mContext.getResources(), (Integer)IFImageFilter.this.mResIds.get(3));
                        IFImageFilter.this.filterSourceTexture5 = OpenGlUtils.loadTexture(b, -1, true);
                    }
                });
            }

            if (this.mResIds.size() > 4) {
                this.runOnDraw(new Runnable() {
                    public void run() {
                        Bitmap b = BitmapFactory.decodeResource(IFImageFilter.this.mContext.getResources(), (Integer)IFImageFilter.this.mResIds.get(4));
                        IFImageFilter.this.filterSourceTexture6 = OpenGlUtils.loadTexture(b, -1, true);
                    }
                });
            }

        }
    }
}