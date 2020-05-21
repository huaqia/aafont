package com.hanmei.aafont.ui.activity;

import android.Manifest;
import android.app.ActivityManager;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.ContentObserver;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Process;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.hanmei.aafont.R;
import com.hanmei.aafont.ui.widget.RatioImageView;
import com.hanmei.aafont.utils.Constant;
import com.hanmei.aafont.utils.FinishActivityManager;
import com.hanmei.aafont.utils.PermissionUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import butterknife.BindView;

public class ImageSelectActivity extends BaseActivity {
    @BindView(R.id.progress_bar)
    ProgressBar mProgressBar;
    @BindView(R.id.recycler_view)
    RecyclerView mRecyclerView;
    @BindView(R.id.to_album)
    AppCompatImageView mToAlbum;
    @BindView(R.id.to_camera)
    AppCompatImageView mToCamera;
    @BindView(R.id.press_next)
    AppCompatTextView mPressNext;

    private final String requiredPermission = Manifest.permission.READ_EXTERNAL_STORAGE;
    private final String[] projection = new String[]{ MediaStore.Images.Media._ID, MediaStore.Images.Media.DISPLAY_NAME, MediaStore.Images.Media.DATA };

    private ImageSelectAdapter mAdapter;

    private Handler mHandler;
    private Boolean mNeedRequestPermissions;
    private Dialog mDialog;
    private ArrayList<String> mImageUrls;
    private Thread mThread;
    private ContentObserver mObserver;
    private String mAlbum;
    private String mSelectUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_set);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.drawable.back);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FinishActivityManager.getManager().finishAllActivity();
            }
        });
        mAlbum = getIntent().getExtras().getString("album");
        final GridLayoutManager layoutManager = new GridLayoutManager(this, 3);
        mRecyclerView.setLayoutManager(layoutManager);
        mNeedRequestPermissions = true;
        mToAlbum.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mRecyclerView.setVisibility(View.INVISIBLE);
                Intent intent = new Intent(getApplicationContext(), AlbumSelectActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(intent);
            }
        });
        mToCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FinishActivityManager.getManager().finishAllActivity();
            }
        });
        mPressNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), EditActivity.class);
                intent.putExtra("url", mSelectUrl);
                startActivity(intent);
            }
        });
        mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case Constant.PERMISSION_GRANTED: {
                        loadImages();
                        break;
                    }

                    case Constant.PERMISSION_DENIED: {
                        mProgressBar.setVisibility(View.INVISIBLE);
                        mRecyclerView.setVisibility(View.INVISIBLE);
                        break;
                    }

                    case Constant.FETCH_STARTED: {
                        mProgressBar.setVisibility(View.VISIBLE);
                        mRecyclerView.setVisibility(View.INVISIBLE);
                        break;
                    }

                    case Constant.FETCH_COMPLETED: {
                        if (mAdapter == null) {
                            mAdapter = new ImageSelectAdapter();
                            mRecyclerView.setAdapter(mAdapter);
                        } else {
                            mAdapter.notifyDataSetChanged();
                        }
                        mProgressBar.setVisibility(View.INVISIBLE);
                        mRecyclerView.setVisibility(View.VISIBLE);

                        break;
                    }

                    case Constant.ERROR: {
                        mProgressBar.setVisibility(View.INVISIBLE);
                    }

                    default: {
                        super.handleMessage(msg);
                    }
                }
            }
        };
        FinishActivityManager.getManager().addActivity(this);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        mRecyclerView.setVisibility(View.VISIBLE);
    }

    @Override
    protected void setMyContentView() {
        setContentView(R.layout.activity_image_select);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mNeedRequestPermissions) {
            if (!PermissionUtils.isPermissionGranted(getApplicationContext(), requiredPermission)) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, requiredPermission)) {
                    MaterialDialog dialog = new MaterialDialog.Builder(this)
                            .title(R.string.permission_notice)
                            .positiveText("ok")
                            .onPositive(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                    requestPermission(requiredPermission);
                                }
                            })
                            .negativeText("cancel")
                            .onNegative(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                    dialog.dismiss();
                                    mNeedRequestPermissions = false;
                                }
                            })
                            .build();
                    showDialog(dialog);
                } else {
                    requestPermission(requiredPermission);
                }
                return;
            }
            mNeedRequestPermissions = false;
            Message message = mHandler.obtainMessage();
            message.what = Constant.PERMISSION_GRANTED;
            message.sendToTarget();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        mObserver = new ContentObserver(mHandler) {
            @Override
            public void onChange(boolean selfChange) {
                loadImages();
            }
        };
        getContentResolver().registerContentObserver(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, false, mObserver);
    }

    @Override
    protected void onStop() {
        super.onStop();
        getContentResolver().unregisterContentObserver(mObserver);
        mObserver = null;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mHandler.removeCallbacksAndMessages(null);
        FinishActivityManager.getManager().finishActivity();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions
            , @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (permissions.length == 0) {
            return;
        }

        Boolean needRequestPermissions = false;
        if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Message message = mHandler.obtainMessage();
            message.what = Constant.PERMISSION_GRANTED;
            message.sendToTarget();
        } else if (ActivityCompat.shouldShowRequestPermissionRationale(this, permissions[0])) {
            needRequestPermissions = true;
        } else {
            Message message = mHandler.obtainMessage();
            message.what = Constant.PERMISSION_DENIED;
            message.sendToTarget();
        }
        mNeedRequestPermissions = needRequestPermissions;
    }

    class ImageSelectAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            return new ImageSelectViewHolder(inflater.inflate(R.layout.item_image_select, parent, false));
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
            if (holder instanceof ImageSelectViewHolder) {
                final ImageSelectViewHolder finalHolder = (ImageSelectViewHolder)holder;
                final String url = mImageUrls.get(position);
                Glide.with(holder.itemView.getContext())
                        .load(url)
                        .fitCenter()
                        .diskCacheStrategy(DiskCacheStrategy.RESULT)
                        .into(finalHolder.mImageSelect);
                if (url.equals(mSelectUrl)) {
                    finalHolder.mSelectBg.setVisibility(View.VISIBLE);
                } else {
                    finalHolder.mSelectBg.setVisibility(View.GONE);
                }
                finalHolder.mImageSelect.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (finalHolder.mSelectBg.getVisibility() == View.VISIBLE) {
                            mSelectUrl = null;
                            mToCamera.setVisibility(View.VISIBLE);
                            mPressNext.setVisibility(View.GONE);
                        } else {
                            mSelectUrl = url;
                            mToCamera.setVisibility(View.GONE);
                            mPressNext.setVisibility(View.VISIBLE);
                        }
                        notifyDataSetChanged();
                    }
                });
            }
        }

        @Override
        public int getItemCount() {
            return mImageUrls.size();
        }
    }

    class ImageSelectViewHolder extends RecyclerView.ViewHolder {
        public RatioImageView mImageSelect;
        public AppCompatImageView mSelectBg;
        public ImageSelectViewHolder(View itemView) {
            super(itemView);
            mImageSelect = (RatioImageView)itemView.findViewById(R.id.image_thumbnail);
            mSelectBg = (AppCompatImageView)itemView.findViewById(R.id.image_select_bg);
        }
    }

    private void loadImages() {
        abortLoading();

        ImageLoaderRunnable runnable = new ImageLoaderRunnable();
        mThread = new Thread(runnable);
        mThread.start();
    }

    private void abortLoading() {
        if (mThread == null) {
            return;
        }

        if (mThread.isAlive()) {
            mThread.interrupt();
            try {
                mThread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private class ImageLoaderRunnable implements Runnable {
        @Override
        public void run() {
            android.os.Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);

            Message message;
            if (mAdapter == null) {
                message = mHandler.obtainMessage();
                /*
                If the adapter is null, this is first time this activity's view is
                being shown, hence send FETCH_STARTED message to show progress bar
                while images are loaded from phone
                 */
                message.what = Constant.FETCH_STARTED;
                message.sendToTarget();
            }

            if (Thread.interrupted()) {
                return;
            }

            File file;

            Cursor cursor = getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, projection,
                    MediaStore.Images.Media.BUCKET_DISPLAY_NAME + " =?", new String[]{ mAlbum }, MediaStore.Images.Media.DATE_ADDED);

            if (cursor == null) {
                message = mHandler.obtainMessage();
                message.what = Constant.ERROR;
                message.sendToTarget();
                return;
            }

            ArrayList<String> temp = new ArrayList<>(cursor.getCount());

            if (cursor.moveToLast()) {
                do {
                    if (Thread.interrupted()) {
                        return;
                    }

                    String path = cursor.getString(cursor.getColumnIndex(projection[2]));

                    file = new File(path);
                    if (file.exists()) {
                        temp.add(path);
                    }

                } while (cursor.moveToPrevious());
            }
            cursor.close();

            if (mImageUrls == null) {
                mImageUrls = new ArrayList<>();
            }
            mImageUrls.clear();
            mImageUrls.addAll(temp);

            message = mHandler.obtainMessage();
            message.what = Constant.FETCH_COMPLETED;
            message.sendToTarget();

            Thread.interrupted();
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

    private void requestPermission(String permission) {
        ActivityCompat.requestPermissions(this, new String[]{permission}, PermissionUtils.REQUEST_CODE_PERMISSIONS_ALL);
    }
}
