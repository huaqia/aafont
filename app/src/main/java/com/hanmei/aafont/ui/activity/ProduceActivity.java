package com.hanmei.aafont.ui.activity;

import android.Manifest;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Point;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.Image;
import android.media.ImageReader;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.util.Size;
import android.util.SparseIntArray;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.hanmei.aafont.R;
import com.hanmei.aafont.filter.FilterFactory;
import com.hanmei.aafont.filter.FilterItem;
import com.hanmei.aafont.filter.FilterSDK;
import com.hanmei.aafont.filter.GPUImageFilter;
import com.hanmei.aafont.filter.MagicEngine;
import com.hanmei.aafont.ui.adapter.FilterAdapter;
import com.hanmei.aafont.ui.widget.CameraSurfaceView;
import com.hanmei.aafont.utils.BitmapUtils;
import com.hanmei.aafont.utils.FileUtils;
import com.hanmei.aafont.utils.PermissionUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;

public class ProduceActivity extends BaseActivity {
    @BindView(R.id.surface_view)
    CameraSurfaceView mCameraSurfaceView;
    @BindView(R.id.filter_recycler_view)
    RecyclerView mFilterRecyclerView;
    @BindView(R.id.change_size)
    ImageView mChangeSizeAction;
    @BindView(R.id.set_mask)
    ImageView mSetMaskAction;
    @BindView(R.id.to_album)
    ImageView mAlbumAction;
    @BindView(R.id.to_capture)
    ImageView mCaptureAction;
    @BindView(R.id.to_change_camera)
    ImageView mChangeCameraAction;
    @BindView(R.id.take_script_notice)
    LinearLayout mScriptNotice;
    @BindView(R.id.take_pic)
    AppCompatTextView mTakePic;
    @BindView(R.id.take_video)
    AppCompatTextView mTakeVideo;
    @BindView(R.id.take_script)
    AppCompatTextView mTakeScript;
    @BindView(R.id.change_size_layout)
    LinearLayout mChangeSizeLayout;
    @BindView(R.id.size_9_16)
    LinearLayout mSize916;
    @BindView(R.id.size_1_1)
    LinearLayout mSize11;
    @BindView(R.id.surface_area)
    FrameLayout mSurfaceArea;

    /**
     * Conversion from screen rotation to JPEG orientation.
     */
    private static final SparseIntArray ORIENTATIONS = new SparseIntArray();
    private static final int REQUEST_CAMERA_PERMISSION = 1;
    private static final int REQUEST_WRITE_PERMISSION = 2;

    private static final int MODE_PIC = 1;
    private static final int MODE_VIDEO = 2;
    private static final int MODE_SCRIPT = 3;

    static {
        ORIENTATIONS.append(Surface.ROTATION_0, 90);
        ORIENTATIONS.append(Surface.ROTATION_90, 0);
        ORIENTATIONS.append(Surface.ROTATION_180, 270);
        ORIENTATIONS.append(Surface.ROTATION_270, 180);
    }

    /**
     * Tag for the {@link Log}.
     */
    private static final String TAG = "ProduceActivity";

    /**
     * Camera state: Showing camera preview.
     */
    private static final int STATE_PREVIEW = 0;

    /**
     * Camera state: Waiting for the focus to be locked.
     */
    private static final int STATE_WAITING_LOCK = 1;

    /**
     * Camera state: Waiting for the exposure to be precapture state.
     */
    private static final int STATE_WAITING_PRECAPTURE = 2;

    /**
     * Camera state: Waiting for the exposure state to be something other than precapture.
     */
    private static final int STATE_WAITING_NON_PRECAPTURE = 3;

    /**
     * Camera state: Picture was taken.
     */
    private static final int STATE_PICTURE_TAKEN = 4;

    /**
     * {@link TextureView.SurfaceTextureListener} handles several lifecycle events on a
     * {@link TextureView}.
     */
    private final CameraSurfaceView.SurfaceTextureListener mSurfaceTextureListener
            = new CameraSurfaceView.SurfaceTextureListener() {

        @Override
        public void onAvailable() {
            openCamera();
        }
    };

    /**
     * ID of the current {@link CameraDevice}.
     */
    private int mCameraId;

    /**
     * A {@link CameraCaptureSession } for camera preview.
     */
    private CameraCaptureSession mCaptureSession;

    /**
     * A reference to the opened {@link CameraDevice}.
     */
    private CameraDevice mCameraDevice;

    /**
     * The {@link Size} of camera preview.
     */
    private Size mPreviewSize;

    private FilterAdapter mAdapter;
    private Dialog mDialog;
    private int mCurrentMode;
    private MagicEngine mMagicEngine;
    private boolean mIsRecording = false;

    /**
     * {@link CameraDevice.StateCallback} is called when {@link CameraDevice} changes its state.
     */
    private final CameraDevice.StateCallback mStateCallback = new CameraDevice.StateCallback() {

        @Override
        public void onOpened(@NonNull CameraDevice cameraDevice) {
            // This method is called when the camera is opened.  We start camera preview here.
            mCameraOpenCloseLock.release();
            mCameraDevice = cameraDevice;
            createCameraPreviewSession();
        }

        @Override
        public void onDisconnected(@NonNull CameraDevice cameraDevice) {
            mCameraOpenCloseLock.release();
            cameraDevice.close();
            mCameraDevice = null;
        }

        @Override
        public void onError(@NonNull CameraDevice cameraDevice, int error) {
            mCameraOpenCloseLock.release();
            cameraDevice.close();
            mCameraDevice = null;
            finish();
        }

    };

    /**
     * An additional thread for running tasks that shouldn't block the UI.
     */
    private HandlerThread mBackgroundThread;

    /**
     * A {@link Handler} for running tasks in the background.
     */
    private Handler mBackgroundHandler;

    /**
     * An {@link ImageReader} that handles still image capture.
     */
    private ImageReader mImageReader;

    /**
     * This is the output file for our picture.
     */
    private File mFile;

    /**
     * This a callback object for the {@link ImageReader}. "onImageAvailable" will be called when a
     * still image is ready to be saved.
     */
    private final ImageReader.OnImageAvailableListener mOnImageAvailableListener
            = new ImageReader.OnImageAvailableListener() {

        @Override
        public void onImageAvailable(ImageReader reader) {
            if (!PermissionUtils.isPermissionGranted(getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                requestWritePermission();
                return;
            }
            mFile = new File(getExternalFilesDir(null), "pic-" + System.currentTimeMillis() / 1000 + ".jpg");
            mBackgroundHandler.post(new ImageSaver(reader.acquireNextImage(), mFile, mCameraSurfaceView.getFilter()));
        }
    };

    /**
     * {@link CaptureRequest.Builder} for the camera preview
     */
    private CaptureRequest.Builder mPreviewRequestBuilder;

    /**
     * {@link CaptureRequest} generated by {@link #mPreviewRequestBuilder}
     */
    private CaptureRequest mPreviewRequest;

    /**
     * The current state of camera state for taking pictures.
     *
     * @see #mCaptureCallback
     */
    private int mState = STATE_PREVIEW;

    /**
     * A {@link Semaphore} to prevent the app from exiting before closing the camera.
     */
    private Semaphore mCameraOpenCloseLock = new Semaphore(1);

    /**
     * Whether the current camera device supports Flash or not.
     */
    private boolean mFlashSupported;

    /**
     * Orientation of the camera sensor
     */
    private int mSensorOrientation;

    private CameraRatio mCameraRatio = CameraRatio.WIDTH_HEIGHT_9_16;

    public enum CameraRatio {
        WIDTH_HEIGHT_9_16, WIDTH_HEIGHT_1_1
    }

    /**
     * A {@link CameraCaptureSession.CaptureCallback} that handles events related to JPEG capture.
     */
    private CameraCaptureSession.CaptureCallback mCaptureCallback
            = new CameraCaptureSession.CaptureCallback() {

        private void process(CaptureResult result) {
            switch (mState) {
                case STATE_PREVIEW: {
                    // We have nothing to do when the camera preview is working normally.
                    break;
                }
                case STATE_WAITING_LOCK: {
                    Integer afState = result.get(CaptureResult.CONTROL_AF_STATE);
                    if (afState == null) {
                        captureStillPicture();
                    } else if (CaptureResult.CONTROL_AF_STATE_FOCUSED_LOCKED == afState
                            || CaptureResult.CONTROL_AF_STATE_NOT_FOCUSED_LOCKED == afState
                            || CaptureResult.CONTROL_AF_STATE_INACTIVE == afState
                            || CaptureResult.CONTROL_AF_STATE_PASSIVE_SCAN == afState) {
                        Integer aeState = result.get(CaptureResult.CONTROL_AE_STATE);
                        if (aeState == null ||
                                aeState == CaptureResult.CONTROL_AE_STATE_CONVERGED) {
                            mState = STATE_PICTURE_TAKEN;
                            captureStillPicture();
                        } else {
                            runPrecaptureSequence();
                        }
                    }
                    break;
                }
                case STATE_WAITING_PRECAPTURE: {
                    // CONTROL_AE_STATE can be null on some devices
                    Integer aeState = result.get(CaptureResult.CONTROL_AE_STATE);
                    if (aeState == null ||
                            aeState == CaptureResult.CONTROL_AE_STATE_PRECAPTURE ||
                            aeState == CaptureRequest.CONTROL_AE_STATE_FLASH_REQUIRED) {
                        mState = STATE_WAITING_NON_PRECAPTURE;
                    }
                    break;
                }
                case STATE_WAITING_NON_PRECAPTURE: {
                    // CONTROL_AE_STATE can be null on some devices
                    Integer aeState = result.get(CaptureResult.CONTROL_AE_STATE);
                    if (aeState == null || aeState != CaptureResult.CONTROL_AE_STATE_PRECAPTURE) {
                        mState = STATE_PICTURE_TAKEN;
                        captureStillPicture();
                    }
                    break;
                }
            }
        }

        @Override
        public void onCaptureProgressed(@NonNull CameraCaptureSession session,
                                        @NonNull CaptureRequest request,
                                        @NonNull CaptureResult partialResult) {
            process(partialResult);
        }

        @Override
        public void onCaptureCompleted(@NonNull CameraCaptureSession session,
                                       @NonNull CaptureRequest request,
                                       @NonNull TotalCaptureResult result) {
            process(result);
        }

    };

    private FilterAdapter.onFilterChangeListener onFilterChangeListener = new FilterAdapter.onFilterChangeListener() {

        @Override
        public void onFilterChanged(FilterItem filterItem) {
            mCameraSurfaceView.setFilter(filterItem);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mCurrentMode = MODE_PIC;
        FilterSDK.init(getApplicationContext());
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        linearLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        mFilterRecyclerView.setLayoutManager(linearLayoutManager);
        mAdapter = new FilterAdapter(getApplicationContext(), FilterFactory.getPortraitFilterItem());
        mFilterRecyclerView.setAdapter(mAdapter);
        mAdapter.setOnFilterChangeListener(onFilterChangeListener);
        mCameraId = CameraCharacteristics.LENS_FACING_FRONT;
        Toolbar toolbar = findViewById(R.id.toolbar_set);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.drawable.back);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        MagicEngine.Builder builder = new MagicEngine.Builder();
        mMagicEngine = builder.build(mCameraSurfaceView);
        mChangeSizeAction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mChangeSizeLayout.getVisibility() == View.VISIBLE) {
                    mChangeSizeLayout.setVisibility(View.GONE);
                } else {
                    mChangeSizeLayout.setVisibility(View.VISIBLE);
                }
            }
        });
        mSize916.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                closeCamera();
                mCameraRatio = CameraRatio.WIDTH_HEIGHT_9_16;
                mPreviewSize = new Size(mSurfaceArea.getWidth(), mSurfaceArea.getHeight());
                mCameraSurfaceView.setAspectRatio(mPreviewSize.getWidth(), mPreviewSize.getHeight());
                openCamera();
//                setUpCameraOutputs();
//                mCameraSurfaceView.setAspectRatio(mPreviewSize.getWidth(), mPreviewSize.getHeight());
//                closeCamera();
//                FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) mCameraSurfaceView.getLayoutParams();
//                params.width = FrameLayout.LayoutParams.MATCH_PARENT;
//                params.height = FrameLayout.LayoutParams.MATCH_PARENT;
//                mCameraSurfaceView.setLayoutParams(params);
//                openCamera();
//                mCameraSurfaceView.setAspectRatio(params.width, params.height);
            }
        });
        mSize11.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                closeCamera();
                mCameraRatio = CameraRatio.WIDTH_HEIGHT_1_1;
                Point screenSize = new Point();
                getWindowManager().getDefaultDisplay().getSize(screenSize);
                mPreviewSize = new Size(screenSize.x, screenSize.x);
                mCameraSurfaceView.setAspectRatio(mPreviewSize.getWidth(), mPreviewSize.getHeight());
                openCamera();
//                setUpCameraOutputs();
//                mCameraSurfaceView.setAspectRatio(mPreviewSize.getWidth(), mPreviewSize.getHeight());
//                closeCamera();
//                FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) mCameraSurfaceView.getLayoutParams();
//                params.width = mCameraSurfaceView.getWidth();
//                params.height = mCameraSurfaceView.getWidth();
//                mCameraSurfaceView.setLayoutParams(params);
//                openCamera();
//                mCameraSurfaceView.setAspectRatio(params.width, params.height);
            }
        });
        mSetMaskAction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mCurrentMode != MODE_SCRIPT) {
                    if (mFilterRecyclerView.getVisibility() == View.VISIBLE) {
                        mFilterRecyclerView.setVisibility(View.GONE);
                    } else {
                        mFilterRecyclerView.setVisibility(View.VISIBLE);
                    }
                }
            }
        });
        String firstPic = FileUtils.findFirstPicInSystemPhotoPath();
        if (firstPic != null) {
            Glide.with(getApplicationContext())
                    .load(firstPic)
                    .fitCenter()
                    .diskCacheStrategy(DiskCacheStrategy.RESULT)
                    .into(mAlbumAction);
        }
        mAlbumAction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), ImageSelectActivity.class);
                intent.putExtra("album", "Camera");//Environment.DIRECTORY_DCIM);
                startActivity(intent);
            }
        });
        mCaptureAction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mCurrentMode == MODE_PIC) {
                    lockFocus();
                } else if (mCurrentMode == MODE_VIDEO) {
                    takeVideo();
                }
            }
        });
        mChangeCameraAction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                closeCamera();
                mCameraId = mCameraId == CameraCharacteristics.LENS_FACING_BACK ? CameraCharacteristics.LENS_FACING_FRONT : CameraCharacteristics.LENS_FACING_BACK;
                openCamera();
            }
        });
        mTakePic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mCurrentMode = MODE_PIC;
                mChangeSizeLayout.setVisibility(View.GONE);
                mScriptNotice.setVisibility(View.GONE);
                mTakePic.setTextColor(getResources().getColor(R.color.white));
                mTakeVideo.setTextColor(getResources().getColor(R.color.divider_color));
                mTakeScript.setTextColor(getResources().getColor(R.color.divider_color));
                RelativeLayout.LayoutParams takePicLayoutParams = (RelativeLayout.LayoutParams)mTakePic.getLayoutParams();
                takePicLayoutParams.addRule(RelativeLayout.CENTER_IN_PARENT);
                takePicLayoutParams.removeRule(RelativeLayout.START_OF);
                takePicLayoutParams.removeRule(RelativeLayout.END_OF);
                mTakePic.setLayoutParams(takePicLayoutParams);
                RelativeLayout.LayoutParams takeVideoLayoutParams = (RelativeLayout.LayoutParams)mTakeVideo.getLayoutParams();
                takeVideoLayoutParams.removeRule(RelativeLayout.CENTER_IN_PARENT);
                takeVideoLayoutParams.removeRule(RelativeLayout.START_OF);
                takeVideoLayoutParams.addRule(RelativeLayout.END_OF, R.id.take_pic);
                mTakeVideo.setLayoutParams(takeVideoLayoutParams);
                RelativeLayout.LayoutParams takeScriptLayoutParams = (RelativeLayout.LayoutParams)mTakeScript.getLayoutParams();
                takeScriptLayoutParams.removeRule(RelativeLayout.CENTER_IN_PARENT);
                takeScriptLayoutParams.removeRule(RelativeLayout.START_OF);
                takeScriptLayoutParams.addRule(RelativeLayout.END_OF, R.id.take_video);
                mTakeScript.setLayoutParams(takeScriptLayoutParams);
            }
        });
        mTakeVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mCurrentMode = MODE_VIDEO;
                mChangeSizeLayout.setVisibility(View.GONE);
                mScriptNotice.setVisibility(View.GONE);
                mTakePic.setTextColor(getResources().getColor(R.color.divider_color));
                mTakeVideo.setTextColor(getResources().getColor(R.color.white));
                mTakeScript.setTextColor(getResources().getColor(R.color.divider_color));
                RelativeLayout.LayoutParams takeVideoLayoutParams = (RelativeLayout.LayoutParams)mTakeVideo.getLayoutParams();
                takeVideoLayoutParams.addRule(RelativeLayout.CENTER_IN_PARENT);
                takeVideoLayoutParams.removeRule(RelativeLayout.START_OF);
                takeVideoLayoutParams.removeRule(RelativeLayout.END_OF);
                mTakeVideo.setLayoutParams(takeVideoLayoutParams);
                RelativeLayout.LayoutParams takePicLayoutParams = (RelativeLayout.LayoutParams)mTakePic.getLayoutParams();
                takePicLayoutParams.removeRule(RelativeLayout.CENTER_IN_PARENT);
                takePicLayoutParams.addRule(RelativeLayout.START_OF, R.id.take_video);
                takePicLayoutParams.removeRule(RelativeLayout.END_OF);
                mTakePic.setLayoutParams(takePicLayoutParams);
                RelativeLayout.LayoutParams takeScriptLayoutParams = (RelativeLayout.LayoutParams)mTakeScript.getLayoutParams();
                takeScriptLayoutParams.removeRule(RelativeLayout.CENTER_IN_PARENT);
                takeScriptLayoutParams.removeRule(RelativeLayout.START_OF);
                takeScriptLayoutParams.addRule(RelativeLayout.END_OF, R.id.take_video);
                mTakeScript.setLayoutParams(takeScriptLayoutParams);
            }
        });
        mTakeScript.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mCurrentMode = MODE_SCRIPT;
                mFilterRecyclerView.setVisibility(View.GONE);
                mChangeSizeLayout.setVisibility(View.GONE);
                mScriptNotice.setVisibility(View.VISIBLE);
                mTakePic.setTextColor(getResources().getColor(R.color.divider_color));
                mTakeVideo.setTextColor(getResources().getColor(R.color.divider_color));
                mTakeScript.setTextColor(getResources().getColor(android.R.color.white));
                RelativeLayout.LayoutParams takeScriptLayoutParams = (RelativeLayout.LayoutParams)mTakeScript.getLayoutParams();
                takeScriptLayoutParams.addRule(RelativeLayout.CENTER_IN_PARENT);
                takeScriptLayoutParams.removeRule(RelativeLayout.START_OF);
                takeScriptLayoutParams.removeRule(RelativeLayout.END_OF);
                mTakeScript.setLayoutParams(takeScriptLayoutParams);
                RelativeLayout.LayoutParams takeVideoLayoutParams = (RelativeLayout.LayoutParams)mTakeVideo.getLayoutParams();
                takeVideoLayoutParams.addRule(RelativeLayout.CENTER_IN_PARENT);
                takeVideoLayoutParams.addRule(RelativeLayout.START_OF, R.id.take_script);
                takeVideoLayoutParams.removeRule(RelativeLayout.END_OF);
                mTakeVideo.setLayoutParams(takeVideoLayoutParams);
                RelativeLayout.LayoutParams takePicLayoutParams = (RelativeLayout.LayoutParams)mTakePic.getLayoutParams();
                takePicLayoutParams.removeRule(RelativeLayout.CENTER_IN_PARENT);
                takePicLayoutParams.addRule(RelativeLayout.START_OF, R.id.take_video);
                takePicLayoutParams.removeRule(RelativeLayout.END_OF);
                mTakePic.setLayoutParams(takePicLayoutParams);
            }
        });
    }

    @Override
    protected void setMyContentView() {
        setContentView(R.layout.activity_produce);
    }

    @Override
    public void onResume() {
        super.onResume();
        startBackgroundThread();
        if (mCameraSurfaceView.getSurfaceTexture() != null) {
            openCamera();
        } else {
            mCameraSurfaceView.setSurfaceTextureListener(mSurfaceTextureListener);
        }
    }

    @Override
    public void onPause() {
        closeCamera();
        stopBackgroundThread();
        super.onPause();
    }

    /**
     * Shows a {@link Toast} on the UI thread.
     *
     * @param text The message to show
     */
    private void showToast(final String text) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private Size chooseOptimalSize(Size[] choices, int width, int height, Size largest)
    {
        List<Size> bigEnough = new ArrayList<Size>();
        for (Size size : choices)
        {
            if (size.getHeight() == size.getWidth() * height / width)
            {
                bigEnough.add(size);
            }
        }

        if (bigEnough.size() > 0)
        {
            return Collections.max(bigEnough, new CompareSizesByArea());
        } else
        {
            return largest;
        }
    }

    private void requestCameraPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA)) {
            MaterialDialog dialog = new MaterialDialog.Builder(this)
                    .title(R.string.permission_notice)
                    .positiveText("ok")
                    .onPositive(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                            ActivityCompat.requestPermissions(getParent(), new String[]{Manifest.permission.CAMERA},
                                    REQUEST_CAMERA_PERMISSION);
                        }
                    })
                    .negativeText("cancel")
                    .onNegative(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                            dialog.dismiss();
                        }
                    })
                    .build();
            showDialog(dialog);
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
        }
    }

    private void requestWritePermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            MaterialDialog dialog = new MaterialDialog.Builder(this)
                    .title(R.string.permission_notice)
                    .positiveText("ok")
                    .onPositive(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                            ActivityCompat.requestPermissions(getParent(), new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                    REQUEST_WRITE_PERMISSION);
                        }
                    })
                    .negativeText("cancel")
                    .onNegative(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                            dialog.dismiss();
                        }
                    })
                    .build();
            showDialog(dialog);
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_WRITE_PERMISSION);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults.length != 1 || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                MaterialDialog dialog = new MaterialDialog.Builder(this)
                        .title(R.string.camera_permission_notice)
                        .positiveText("ok")
                        .build();
                showDialog(dialog);
            } else {
                realOpenCamera();
            }
        } else if (requestCode == REQUEST_WRITE_PERMISSION) {
            if (grantResults.length != 1 || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                MaterialDialog dialog = new MaterialDialog.Builder(this)
                        .title(R.string.write_permission_notice)
                        .positiveText("ok")
                        .build();
            } else {
                if (mCurrentMode == MODE_PIC) {
                    lockFocus();
                } else if (mCurrentMode == MODE_VIDEO) {
                    takeVideo();
                }
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    private void setUpCameraOutputs() {
        CameraManager manager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        try {
            CameraCharacteristics characteristics
                    = manager.getCameraCharacteristics(String.valueOf(mCameraId));

            StreamConfigurationMap map = characteristics.get(
                    CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);

            // For still image captures, we use the largest available size.
            Size largest = Collections.max(
                    Arrays.asList(map.getOutputSizes(ImageFormat.JPEG)),
                    new CompareSizesByArea());

            Size size = largest;
            if (mCameraRatio == CameraRatio.WIDTH_HEIGHT_9_16) {
                size = chooseOptimalSize(map.getOutputSizes(ImageFormat.JPEG), 9, 16, largest);
            } else if (mCameraRatio == CameraRatio.WIDTH_HEIGHT_1_1) {
                size = chooseOptimalSize(map.getOutputSizes(ImageFormat.JPEG), 1, 1, largest);
            }
            int orientation = getResources().getConfiguration().orientation;
            if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
                mImageReader = ImageReader.newInstance(size.getWidth(), size.getHeight(), ImageFormat.JPEG, /*maxImages*/2);
            } else {
                mImageReader = ImageReader.newInstance(size.getHeight(), size.getWidth(), ImageFormat.JPEG, /*maxImages*/2);
            }

            mImageReader.setOnImageAvailableListener(mOnImageAvailableListener, mBackgroundHandler);

            //noinspection ConstantConditions
            mSensorOrientation = characteristics.get(CameraCharacteristics.SENSOR_ORIENTATION);

            Point screenSize = new Point();
            getWindowManager().getDefaultDisplay().getSize(screenSize);
            int screen_w = screenSize.x;
            if (mCameraRatio == CameraRatio.WIDTH_HEIGHT_9_16) {
                mPreviewSize = new Size(mSurfaceArea.getWidth(), mSurfaceArea.getHeight());
            } else if (mCameraRatio == CameraRatio.WIDTH_HEIGHT_1_1) {
                mPreviewSize = new Size(screen_w, screen_w);
            }

            if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
                mCameraSurfaceView.setViewPortSize(mPreviewSize.getHeight(), mPreviewSize.getWidth());
            } else {
                mCameraSurfaceView.setViewPortSize(mPreviewSize.getWidth(), mPreviewSize.getHeight());
            }

            // Check if the flash is supported.
            Boolean available = characteristics.get(CameraCharacteristics.FLASH_INFO_AVAILABLE);
            mFlashSupported = available == null ? false : available;
        } catch (CameraAccessException e) {
            e.printStackTrace();
        } catch (NullPointerException e) {
            // Currently an NPE is thrown when the Camera2API is used but not supported on the
            // device this code runs.
            MaterialDialog dialog = new MaterialDialog.Builder(this)
                    .title(R.string.camera_error)
                    .positiveText("ok")
                    .build();
        }
    }

    private void openCamera() {
        if (!PermissionUtils.isPermissionGranted(getApplicationContext(), Manifest.permission.CAMERA)) {
            requestCameraPermission();
            return;
        }
        realOpenCamera();
    }

    private void realOpenCamera() {
        setUpCameraOutputs();
        CameraManager manager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        try {
            if (!mCameraOpenCloseLock.tryAcquire(2500, TimeUnit.MILLISECONDS)) {
                throw new RuntimeException("Time out waiting to lock camera opening.");
            }
            manager.openCamera(String.valueOf(mCameraId), mStateCallback, mBackgroundHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            throw new RuntimeException("Interrupted while trying to lock camera opening.", e);
        }
    }

    /**
     * Closes the current {@link CameraDevice}.
     */
    private void closeCamera() {
        try {
            mCameraOpenCloseLock.acquire();
            if (null != mCaptureSession) {
                mCaptureSession.close();
                mCaptureSession = null;
            }
            if (null != mCameraDevice) {
                mCameraDevice.close();
                mCameraDevice = null;
            }
            if (null != mImageReader) {
                mImageReader.close();
                mImageReader = null;
            }
        } catch (InterruptedException e) {
            throw new RuntimeException("Interrupted while trying to lock camera closing.", e);
        } finally {
            mCameraOpenCloseLock.release();
        }
    }

    /**
     * Starts a background thread and its {@link Handler}.
     */
    private void startBackgroundThread() {
        mBackgroundThread = new HandlerThread("CameraBackground");
        mBackgroundThread.start();
        mBackgroundHandler = new Handler(mBackgroundThread.getLooper());
    }

    /**
     * Stops the background thread and its {@link Handler}.
     */
    private void stopBackgroundThread() {
        mBackgroundThread.quitSafely();
        try {
            mBackgroundThread.join();
            mBackgroundThread = null;
            mBackgroundHandler = null;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * Creates a new {@link CameraCaptureSession} for camera preview.
     */
    private void createCameraPreviewSession() {
        try {
            SurfaceTexture texture = mCameraSurfaceView.getSurfaceTexture();
            assert texture != null;

            // We configure the size of default buffer to be the size of camera preview we want.
            if (mCameraRatio == CameraRatio.WIDTH_HEIGHT_9_16) {
                texture.setDefaultBufferSize(mPreviewSize.getWidth(), mPreviewSize.getWidth() * 16 / 9);
            } else if (mCameraRatio == CameraRatio.WIDTH_HEIGHT_1_1) {
                texture.setDefaultBufferSize(mPreviewSize.getWidth(), mPreviewSize.getWidth());
            }

            // This is the output Surface we need to start preview.
            Surface surface = new Surface(texture);

            // We set up a CaptureRequest.Builder with the output Surface.
            mPreviewRequestBuilder
                    = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            mPreviewRequestBuilder.addTarget(surface);

            // Here, we create a CameraCaptureSession for camera preview.
            mCameraDevice.createCaptureSession(Arrays.asList(surface, mImageReader.getSurface()),
                    new CameraCaptureSession.StateCallback() {

                        @Override
                        public void onConfigured(@NonNull CameraCaptureSession cameraCaptureSession) {
                            // The camera is already closed
                            if (null == mCameraDevice) {
                                return;
                            }

                            // When the session is ready, we start displaying the preview.
                            mCaptureSession = cameraCaptureSession;
                            try {
                                // Auto focus should be continuous for camera preview.
                                mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AF_MODE,
                                        CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
                                // Flash is automatically enabled when necessary.
                                setAutoFlash(mPreviewRequestBuilder);

                                // Finally, we start displaying the camera preview.
                                mPreviewRequest = mPreviewRequestBuilder.build();
                                mCaptureSession.setRepeatingRequest(mPreviewRequest,
                                        mCaptureCallback, mBackgroundHandler);
                            } catch (CameraAccessException e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onConfigureFailed(
                                @NonNull CameraCaptureSession cameraCaptureSession) {
                            showToast("Failed");
                        }
                    }, null
            );
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }


    /**
     * Lock the focus as the first step for a still image capture.
     */
    private void lockFocus() {
        try {
            // This is how to tell the camera to lock focus.
            mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AF_TRIGGER,
                    CameraMetadata.CONTROL_AF_TRIGGER_START);
            // Tell #mCaptureCallback to wait for the lock.
            mState = STATE_WAITING_LOCK;
            mCaptureSession.capture(mPreviewRequestBuilder.build(), mCaptureCallback,
                    mBackgroundHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void takeVideo() {
        if(mIsRecording) {
            mMagicEngine.stopRecord();
        }else {
            mMagicEngine.startRecord();
        }
        mIsRecording = !mIsRecording;
    }

    /**
     * Run the precapture sequence for capturing a still image. This method should be called when
     * we get a response in {@link #mCaptureCallback} from {@link #lockFocus()}.
     */
    private void runPrecaptureSequence() {
        try {
            // This is how to tell the camera to trigger.
            mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AE_PRECAPTURE_TRIGGER,
                    CaptureRequest.CONTROL_AE_PRECAPTURE_TRIGGER_START);
            // Tell #mCaptureCallback to wait for the precapture sequence to be set.
            mState = STATE_WAITING_PRECAPTURE;
            mCaptureSession.capture(mPreviewRequestBuilder.build(), mCaptureCallback,
                    mBackgroundHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    /**
     * Capture a still picture. This method should be called when we get a response in
     * {@link #mCaptureCallback} from both {@link #lockFocus()}.
     */
    private void captureStillPicture() {
        try {
            if (null == mCameraDevice) {
                return;
            }
            // This is the CaptureRequest.Builder that we use to take a picture.
            final CaptureRequest.Builder captureBuilder =
                    mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
            captureBuilder.addTarget(mImageReader.getSurface());

            // Use the same AE and AF modes as the preview.
            captureBuilder.set(CaptureRequest.CONTROL_AF_MODE,
                    CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
            setAutoFlash(captureBuilder);

            // Orientation
            int rotation = getWindowManager().getDefaultDisplay().getRotation();
            captureBuilder.set(CaptureRequest.JPEG_ORIENTATION, getOrientation(rotation));

            CameraCaptureSession.CaptureCallback CaptureCallback
                    = new CameraCaptureSession.CaptureCallback() {

                @Override
                public void onCaptureCompleted(@NonNull CameraCaptureSession session,
                                               @NonNull CaptureRequest request,
                                               @NonNull TotalCaptureResult result) {
                    if (mFile != null) {
                        //showToast("Saved: " + mFile);
                        Intent intent = new Intent(getApplicationContext(), EditActivity.class);
                        intent.putExtra("url", mFile.getAbsolutePath());
                        startActivity(intent);
                        Log.d(TAG, mFile.toString());
                    }
                    else{
                        Log.d(TAG, "onCaptureCompleted file is null");
                    }
                    unlockFocus();
                }
            };

            mCaptureSession.stopRepeating();
            mCaptureSession.abortCaptures();
            mCaptureSession.capture(captureBuilder.build(), CaptureCallback, null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    /**
     * Retrieves the JPEG orientation from the specified screen rotation.
     *
     * @param rotation The screen rotation.
     * @return The JPEG orientation (one of 0, 90, 270, and 360)
     */
    private int getOrientation(int rotation) {
        // Sensor orientation is 90 for most devices, or 270 for some devices (eg. Nexus 5X)
        // We have to take that into account and rotate JPEG properly.
        // For devices with orientation of 90, we simply return our mapping from ORIENTATIONS.
        // For devices with orientation of 270, we need to rotate the JPEG 180 degrees.
        return (ORIENTATIONS.get(rotation) + mSensorOrientation + 270) % 360;
    }

    /**
     * Unlock the focus. This method should be called when still image capture sequence is
     * finished.
     */
    private void unlockFocus() {
        try {
            // Reset the auto-focus trigger
            mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AF_TRIGGER,
                    CameraMetadata.CONTROL_AF_TRIGGER_CANCEL);
            setAutoFlash(mPreviewRequestBuilder);
            mCaptureSession.capture(mPreviewRequestBuilder.build(), mCaptureCallback,
                    mBackgroundHandler);
            // After this, the camera will go back to the normal state of preview.
            mState = STATE_PREVIEW;
            mCaptureSession.setRepeatingRequest(mPreviewRequest, mCaptureCallback,
                    mBackgroundHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void setAutoFlash(CaptureRequest.Builder requestBuilder) {
        if (mFlashSupported) {
            requestBuilder.set(CaptureRequest.CONTROL_AE_MODE,
                    CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH);
        }
    }

    /**
     * Saves a JPEG {@link Image} into the specified {@link File}.
     */
    private static class ImageSaver implements Runnable {

        /**
         * The JPEG image
         */
        private final Image mImage;
        /**
         * The file we save the image into.
         */
        private final File mFile;
        private final GPUImageFilter mFilter;

        ImageSaver(Image image, File file, GPUImageFilter filter) {
            mImage = image;
            mFile = file;
            mFilter = filter;
        }

        @Override
        public void run() {
            ByteBuffer buffer = mImage.getPlanes()[0].getBuffer();
            byte[] bytes = new byte[buffer.remaining()];
            buffer.get(bytes);
            if (mFilter != null) {
                Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                mImage.close();
                BitmapUtils.saveBitmap(FilterSDK.sContext, bitmap, mFilter, mFile);
            } else {
                FileOutputStream output = null;
                try {
                    output = new FileOutputStream(mFile);
                    output.write(bytes);
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    mImage.close();
                    if (null != output) {
                        try {
                            output.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }

    }

    /**
     * Compares two {@code Size}s based on their areas.
     */
    static class CompareSizesByArea implements Comparator<Size> {

        @Override
        public int compare(Size lhs, Size rhs) {
            // We cast here to ensure the multiplications won't overflow
            return Long.signum((long) lhs.getWidth() * lhs.getHeight() -
                    (long) rhs.getWidth() * rhs.getHeight());
        }

    }

    public void dismissDialog() {
        if (mDialog != null && mDialog.isShowing()) {
            mDialog.dismiss();
        }
        mDialog = null;
    }

    public void showDialog(@NonNull Dialog dialog) {
        dismissDialog();
        this.mDialog = dialog;
        mDialog.show();
    }
}
