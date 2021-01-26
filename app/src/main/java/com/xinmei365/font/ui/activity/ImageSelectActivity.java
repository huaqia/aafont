package com.xinmei365.font.ui.activity;

import android.Manifest;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.ContentObserver;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Process;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.xinmei365.font.MyApplication;
import com.xinmei365.font.R;
import com.xinmei365.font.ui.widget.RatioImageView;
import com.xinmei365.font.utils.Constant;
import com.xinmei365.font.utils.FinishActivityManager;
import com.xinmei365.font.utils.PermissionUtils;

import java.io.File;
import java.util.ArrayList;

import butterknife.BindView;

public class ImageSelectActivity extends BaseActivity {
    @BindView(R.id.image_select_close)
    AppCompatImageView mClose;
    @BindView(R.id.progress_bar)
    ProgressBar mProgressBar;
    @BindView(R.id.recycler_view)
    RecyclerView mRecyclerView;
    @BindView(R.id.album_name)
    AppCompatTextView mAlbumName;
    @BindView(R.id.to_album)
    AppCompatImageView mToAlbum;
    @BindView(R.id.press_next)
    AppCompatButton mPressNext;
    @BindView(R.id.selected_images)
    RecyclerView mSelectedImages;

    private final String requiredPermission = Manifest.permission.READ_EXTERNAL_STORAGE;
    private final String[] projection = new String[]{ MediaStore.Images.Media._ID, MediaStore.Images.Media.DISPLAY_NAME, MediaStore.Images.Media.DATA };

    private ImageSelectAdapter mAdapter;
    private ImageResultAdapter mResultAdapter;

    private Handler mHandler;
    private Boolean mNeedRequestPermissions;
    private Dialog mDialog;
    private ArrayList<String> mImageUrls;
    private Thread mThread;
    private ContentObserver mObserver;
    private String mAlbum;
    private ArrayList<String> mSelectUrls;
    private int mStartIndex;
    private boolean mIsChat;
    private int mMaxPictures;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FinishActivityManager.getManager().finishAllActivity();
            }
        });
        mSelectUrls = new ArrayList<>();
        mAlbum = getIntent().getExtras().getString("album");
        mStartIndex = getIntent().getExtras().getInt("start");
        mIsChat = getIntent().getExtras().getBoolean("chat");
        if (mIsChat) {
            mMaxPictures = 3;
        } else {
            mMaxPictures = 9;
        }
        final GridLayoutManager layoutManager = new GridLayoutManager(this, 3);
        mRecyclerView.setLayoutManager(layoutManager);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        mSelectedImages.setLayoutManager(linearLayoutManager);
        mResultAdapter = new ImageResultAdapter();
        mSelectedImages.setAdapter(mResultAdapter);
        mNeedRequestPermissions = true;
        mAlbumName.setText(mAlbum);
        mAlbumName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), AlbumSelectActivity.class);
                intent.putExtra("album", mAlbum);
                intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(intent);
            }
        });
        mToAlbum.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), AlbumSelectActivity.class);
                intent.putExtra("album", mAlbum);
                intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(intent);
            }
        });
        mPressNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mSelectUrls.size() == 0) {
                    Toast.makeText(getApplicationContext(), "请选择图片", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (mIsChat || mStartIndex != 0) {
                    Intent intent = new Intent();
                    intent.putExtra("urls", mSelectUrls);
                    setResult(RESULT_OK, intent);
                }
                else {
                    Intent intent = new Intent(getApplicationContext(), EditActivity.class);
                    intent.putExtra("urls", mSelectUrls);
                    startActivity(intent);
                }
                finish();
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
//        mRecyclerView.setVisibility(View.VISIBLE);
        mAlbum = intent.getExtras().getString("album");
        mAlbumName.setText(mAlbum);
        loadImages();
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
                Glide.with(MyApplication.getInstance())
                        .load(url)
                        .fitCenter()
                        .diskCacheStrategy(DiskCacheStrategy.RESULT)
                        .into(finalHolder.mImageSelect);
                int selectIndex = mSelectUrls.indexOf(url);
                if (selectIndex >= 0) {
                    finalHolder.mSelectIndex.setText(String.format("%d", mStartIndex + selectIndex + 1));
                    finalHolder.mSelectIndex.setBackgroundResource(R.drawable.ic_album_selected);
                } else {
                    finalHolder.mSelectIndex.setText("");
                    finalHolder.mSelectIndex.setBackgroundResource(R.drawable.ic_album_unselected);
                }
                finalHolder.mImageSelect.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (mSelectUrls.contains(url)) {
                            mSelectUrls.remove(url);
                        } else {
                            if (mStartIndex + mSelectUrls.size() >= mMaxPictures) {
                                Toast.makeText(getApplicationContext(), "最多选择" + mMaxPictures + "张图片", Toast.LENGTH_SHORT).show();
                                return;
                            }
                            mSelectUrls.add(url);
                        }
                        if (mSelectUrls.size() > 0) {
                            mPressNext.setText(String.format("确定(%d/9)", mSelectUrls.size()));
                            mPressNext.setBackgroundResource(R.drawable.ic_ok);
                            mSelectedImages.setVisibility(View.VISIBLE);
                            mResultAdapter.notifyDataSetChanged();
                        } else {
                            mPressNext.setText("确定(0/9)");
                            mPressNext.setBackgroundResource(R.drawable.ic_nok);
                            mSelectedImages.setVisibility(View.GONE);
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
        public AppCompatTextView mSelectIndex;
        public ImageSelectViewHolder(View itemView) {
            super(itemView);
            mImageSelect = (RatioImageView)itemView.findViewById(R.id.image_thumbnail);
            mSelectIndex = (AppCompatTextView)itemView.findViewById(R.id.image_select_index);
        }
    }

    class ImageResultAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            return new ImageResultViewHolder(inflater.inflate(R.layout.item_image_result, parent, false));
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
            if (holder instanceof ImageResultViewHolder) {
                final ImageResultViewHolder finalHolder = (ImageResultViewHolder)holder;
                final String url = mSelectUrls.get(position);
                Glide.with(MyApplication.getInstance())
                        .load(url)
                        .fitCenter()
                        .diskCacheStrategy(DiskCacheStrategy.RESULT)
                        .into(finalHolder.mImageSelect);
                finalHolder.mImageDelete.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        mSelectUrls.remove(url);
                        mResultAdapter.notifyDataSetChanged();
                        mAdapter.notifyDataSetChanged();
                        if (mSelectUrls.size() > 0) {
                            mPressNext.setText(String.format("确定(%d/9)", mSelectUrls.size()));
                            mPressNext.setBackgroundResource(R.drawable.ic_ok);
                        } else {
                            mPressNext.setText("确定(0/9)");
                            mPressNext.setBackgroundResource(R.drawable.ic_nok);
                            mSelectedImages.setVisibility(View.GONE);
                        }
                    }
                });
            }
        }

        @Override
        public int getItemCount() {
            return mSelectUrls.size();
        }
    }

    class ImageResultViewHolder extends RecyclerView.ViewHolder {
        public RatioImageView mImageSelect;
        public AppCompatImageView mImageDelete;
        public ImageResultViewHolder(View itemView) {
            super(itemView);
            mImageSelect = (RatioImageView)itemView.findViewById(R.id.image_thumbnail);
            mImageDelete = (AppCompatImageView)itemView.findViewById(R.id.image_result_delete);
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
