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
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.LinearLayoutManager;
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
import com.xinmei365.font.R;
import com.xinmei365.font.ui.widget.RatioImageView;
import com.xinmei365.font.utils.Constant;
import com.xinmei365.font.utils.FinishActivityManager;
import com.xinmei365.font.utils.PermissionUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;

import butterknife.BindView;

public class AlbumSelectActivity extends BaseActivity {
    @BindView(R.id.image_select_close)
    AppCompatImageView mClose;
    @BindView(R.id.progress_bar)
    ProgressBar mProgressBar;
    @BindView(R.id.recycler_view)
    RecyclerView mRecyclerView;
    @BindView(R.id.album_name)
    AppCompatTextView mAlbumName;
    @BindView(R.id.to_image)
    AppCompatImageView mToImage;

    private final String requiredPermission = Manifest.permission.READ_EXTERNAL_STORAGE;
    private final String[] projection = new String[]{ MediaStore.Images.Media.BUCKET_DISPLAY_NAME, MediaStore.Images.Media.DATA };

    private AlbumSelectAdapter mAdapter;

    private Handler mHandler;
    private Boolean mNeedRequestPermissions;
    private Dialog mDialog;
    private ArrayList<Album> mAlbums;
    private Thread mThread;
    private ContentObserver mObserver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.VERTICAL, false));
        mNeedRequestPermissions = true;
        String album = getIntent().getExtras().getString("album");
        mAlbumName.setText(album);
        mAlbumName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        mToImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
//                mRecyclerView.setVisibility(View.INVISIBLE);
//                Intent intent = new Intent(getApplicationContext(), ImageSelectActivity.class);
//                intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
//                startActivity(intent);
            }
        });
        mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case Constant.PERMISSION_GRANTED: {
                        loadAlbums();
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
                        /*
                        If adapter is null, this implies that the loaded images will be shown
                        for the first time, hence send FETCH_COMPLETED message.
                        However, if adapter has been initialised, this thread was run either
                        due to the activity being restarted or content being changed.
                         */
                        if (mAdapter == null) {
                            mAdapter = new AlbumSelectAdapter();
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
    protected void setMyContentView() {
        setContentView(R.layout.activity_album_select);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        mRecyclerView.setVisibility(View.VISIBLE);
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
                loadAlbums();
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

    class AlbumSelectAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            return new AlbumSelectViewHolder(inflater.inflate(R.layout.item_album_select, parent, false));
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
            if (holder instanceof AlbumSelectViewHolder) {
                final AlbumSelectViewHolder finalHolder = (AlbumSelectViewHolder)holder;
                String url = mAlbums.get(position).coverPath;
                Glide.with(holder.itemView.getContext())
                        .load(url)
                        .fitCenter()
                        .diskCacheStrategy(DiskCacheStrategy.RESULT)
                        .into(finalHolder.mAlbumIcon);
                final String albumName = mAlbums.get(position).name;
                finalHolder.mAlbumName.setText(albumName);
                finalHolder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        finish();
                        Intent intent = new Intent(getApplicationContext(), ImageSelectActivity.class);
                        intent.putExtra("album", albumName);
                        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                        startActivity(intent);
                    }
                });
            }
        }

        @Override
        public int getItemCount() {
            return mAlbums.size();
        }
    }

    class AlbumSelectViewHolder extends RecyclerView.ViewHolder {
        public RatioImageView mAlbumIcon;
        public AppCompatTextView mAlbumName;
        public AlbumSelectViewHolder(View itemView) {
            super(itemView);
            mAlbumIcon = (RatioImageView)itemView.findViewById(R.id.image_thumbnail);
            mAlbumName = (AppCompatTextView)itemView.findViewById(R.id.album_name);
        }
    }

    private void loadAlbums() {
        abortLoading();

        AlbumLoaderRunnable runnable = new AlbumLoaderRunnable();
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

    private class AlbumLoaderRunnable implements Runnable {
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

            Cursor cursor = getApplicationContext().getContentResolver()
                    .query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, projection,
                            null, null, MediaStore.Images.Media.DATE_ADDED);

            if (cursor == null) {
                message = mHandler.obtainMessage();
                message.what = Constant.ERROR;
                message.sendToTarget();
                return;
            }

            File file;
            ArrayList<Album> temp = new ArrayList<>(cursor.getCount());
            HashSet<String> albumSet = new HashSet<>();

            if (cursor.moveToLast()) {
                do {
                    if (Thread.interrupted()) {
                        return;
                    }

                    String album = cursor.getString(cursor.getColumnIndex(projection[0]));
                    String image = cursor.getString(cursor.getColumnIndex(projection[1]));

                    file = new File(image);
                    if (file.exists() && !albumSet.contains(album)) {
                        temp.add(new Album(album, image));
                        albumSet.add(album);
                    }

                } while (cursor.moveToPrevious());
            }
            cursor.close();

            if (mAlbums == null) {
                mAlbums = new ArrayList<>();
            }
            mAlbums.clear();
            mAlbums.addAll(temp);

            message = mHandler.obtainMessage();
            message.what = Constant.FETCH_COMPLETED;
            message.sendToTarget();

            Thread.interrupted();
        }
    }

    static class Album {
        public String name;
        public String coverPath;

        public Album(String name, String coverPath) {
            this.name = name;
            this.coverPath = coverPath;
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
