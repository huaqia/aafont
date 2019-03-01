package com.hanmei.aafont.ui.widget;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.opengl.EGL14;
import android.util.AttributeSet;

import com.hanmei.aafont.filter.FilterItem;
import com.hanmei.aafont.filter.OesTextureFilter;
import com.hanmei.aafont.utils.CameraInfo;
import com.hanmei.aafont.utils.MagicParams;
import com.hanmei.aafont.utils.OpenGlUtils;
import com.hanmei.aafont.utils.Rotation;
import com.hanmei.aafont.utils.TextureRotationUtil;
import com.hanmei.aafont.video.TextureMovieEncoder;

import java.io.File;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class CameraSurfaceView extends BaseSurfaceView {

    private OesTextureFilter mOesTextureFilter;

    public SurfaceTexture getSurfaceTexture() {
        return mSurfaceTexture;
    }

    private SurfaceTexture mSurfaceTexture;

    private boolean recordingEnabled;
    private int recordingStatus;

    private static final int RECORDING_OFF = 0;
    private static final int RECORDING_ON = 1;
    private static final int RECORDING_RESUMED = 2;
    private static TextureMovieEncoder videoEncoder = new TextureMovieEncoder();

    private File outputFile;

    private int mRatioWidth = 0;
    private int mRatioHeight = 0;

    public CameraSurfaceView(Context context) {
        this(context, null);
    }

    public CameraSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.getHolder().addCallback(this);
        outputFile = new File(MagicParams.videoPath,MagicParams.videoName);
        recordingStatus = -1;
        recordingEnabled = false;
        scaleType = ScaleType.CENTER_CROP;
        gLTextureBuffer.put(TextureRotationUtil.getRotation(Rotation.NORMAL, false, true))
                .position(0);
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        super.onSurfaceCreated(gl, config);
        recordingEnabled = videoEncoder.isRecording();
        if (recordingEnabled) {
            recordingStatus = RECORDING_RESUMED;
        } else {
            recordingStatus = RECORDING_OFF;
        }
        if (mOesTextureFilter == null)
            mOesTextureFilter = new OesTextureFilter();
        mOesTextureFilter.init();
//        if (textureId == OpenGlUtils.NO_TEXTURE) {
        textureId = OpenGlUtils.getExternalOESTextureID();
        if (textureId != OpenGlUtils.NO_TEXTURE) {
            mSurfaceTexture = new SurfaceTexture(textureId);
            if (mSurfaceTextureListener != null) {
                mSurfaceTextureListener.onAvailable();
            }
            mSurfaceTexture.setOnFrameAvailableListener(onFrameAvailableListener);
        }
//        }
    }

    public void setSurfaceTextureListener(SurfaceTextureListener surfaceTextureListener) {
        mSurfaceTextureListener = surfaceTextureListener;
    }

    private SurfaceTextureListener mSurfaceTextureListener;

    public void setViewPortSize(int width, int height) {
        imageWidth = width;
        imageHeight = height;
    }

    public interface SurfaceTextureListener {
        void onAvailable();
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        super.onSurfaceChanged(gl, width, height);
    }

    public void changeRecordingState(boolean isRecording) {
        recordingEnabled = isRecording;
    }

    protected void onFilterChanged() {
        super.onFilterChanged();
        mOesTextureFilter.onInputSizeChanged(imageWidth, imageHeight);
        mOesTextureFilter.onDisplaySizeChanged(surfaceWidth, surfaceHeight);
        mOesTextureFilter.initFrameBuffers(imageWidth, imageHeight);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        super.onDrawFrame(gl);
        if (mSurfaceTexture == null)
            return;
        mSurfaceTexture.updateTexImage();
        if (recordingEnabled) {
            switch (recordingStatus) {
                case RECORDING_OFF:
                    videoEncoder.setPreviewSize(imageWidth, imageHeight);
                    videoEncoder.setTextureBuffer(gLTextureBuffer);
                    videoEncoder.setCubeBuffer(gLCubeBuffer);
                    videoEncoder.startRecording(new TextureMovieEncoder.EncoderConfig(
                            outputFile, imageWidth, imageHeight,
                            1000000, EGL14.eglGetCurrentContext()));
                    recordingStatus = RECORDING_ON;
                    break;
                case RECORDING_RESUMED:
                    videoEncoder.updateSharedContext(EGL14.eglGetCurrentContext());
                    recordingStatus = RECORDING_ON;
                    break;
                case RECORDING_ON:
                    break;
                default:
                    throw new RuntimeException("unknown status " + recordingStatus);
            }
        } else {
            switch (recordingStatus) {
                case RECORDING_ON:
                case RECORDING_RESUMED:
                    videoEncoder.stopRecording();
                    recordingStatus = RECORDING_OFF;
                    break;
                case RECORDING_OFF:
                    break;
                default:
                    throw new RuntimeException("unknown status " + recordingStatus);
            }
        }
        float[] mtx = new float[16];
        mSurfaceTexture.getTransformMatrix(mtx);
        mOesTextureFilter.setTextureTransformMatrix(mtx);
        int id = textureId;
        if (mFilter == null) {
            mOesTextureFilter.onDrawFrame(textureId, gLCubeBuffer, gLTextureBuffer);
        } else {
            id = mOesTextureFilter.onDrawToTexture(textureId);
            mFilter.onDrawFrame(id, gLCubeBuffer, gLTextureBuffer);
        }
        videoEncoder.setTextureId(id);
        videoEncoder.frameAvailable(mSurfaceTexture);
    }

    private SurfaceTexture.OnFrameAvailableListener onFrameAvailableListener = new SurfaceTexture.OnFrameAvailableListener() {

        @Override
        public void onFrameAvailable(SurfaceTexture surfaceTexture) {
            requestRender();
        }
    };

    @Override
    public void setFilter(final FilterItem filterItem) {
        super.setFilter(filterItem);
        videoEncoder.setFilter(mFilter);
    }

    /**
     * Sets the aspect ratio for this view. The size of the view will be measured based on the ratio
     * calculated from the parameters. Note that the actual sizes of parameters don't matter, that
     * is, calling setAspectRatio(2, 3) and setAspectRatio(4, 6) make the same result.
     *
     * @param width  Relative horizontal size
     * @param height Relative vertical size
     */
    public void setAspectRatio(int width, int height) {
        if (width < 0 || height < 0) {
            throw new IllegalArgumentException("Size cannot be negative.");
        }
        mRatioWidth = width;
        mRatioHeight = height;
        requestLayout();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);
        if (0 == mRatioWidth || 0 == mRatioHeight) {
            setMeasuredDimension(width, height);
        } else {
            if (width < height * mRatioWidth / mRatioHeight) {
                setMeasuredDimension(width, width * mRatioHeight / mRatioWidth);
            } else {
                setMeasuredDimension(height * mRatioWidth / mRatioHeight, height);
            }
        }
    }
}