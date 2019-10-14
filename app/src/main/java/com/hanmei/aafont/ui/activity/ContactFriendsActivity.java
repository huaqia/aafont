package com.hanmei.aafont.ui.activity;

import android.Manifest;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Process;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.widget.AppCompatImageButton;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.hanmei.aafont.R;
import com.hanmei.aafont.model.User;
import com.hanmei.aafont.utils.Constant;
import com.hanmei.aafont.utils.PermissionUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FindListener;
import de.hdodenhof.circleimageview.CircleImageView;

public class ContactFriendsActivity extends BaseActivity {
    @BindView(R.id.progress_bar)
    ProgressBar mProgressBar;
    @BindView(R.id.recycler_view)
    RecyclerView mRecyclerView;

    private final String requiredPermission = Manifest.permission.READ_CONTACTS;

    private ContactFriendsAdapter mAdapter;
    private Handler mHandler;
    private Boolean mNeedRequestPermissions;
    private Dialog mDialog;
    private Thread mThread;

    List<Pair<User, String>> mContactFriends = new ArrayList<>();

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
                onBackPressed();
            }
        });
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        mRecyclerView.setLayoutManager(linearLayoutManager);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.addItemDecoration(new DividerItemDecoration(this, linearLayoutManager.getOrientation()));
        mNeedRequestPermissions = true;
        mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case Constant.PERMISSION_GRANTED: {
                        loadContactFriends();
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
                            mAdapter = new ContactFriendsAdapter();
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
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        mRecyclerView.setVisibility(View.VISIBLE);
    }

    @Override
    protected void setMyContentView() {
        setContentView(R.layout.activity_contact_friends);
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

    class ContactFriendsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            return new ContactFriendsViewHolder(inflater.inflate(R.layout.item_contact_friend, parent, false));
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
            if (holder instanceof ContactFriendsViewHolder) {
                final ContactFriendsViewHolder finalHolder = (ContactFriendsViewHolder)holder;
                Pair<User, String> pair = mContactFriends.get(position);
                User user = pair.first;
                String name = pair.second;
                Glide.with(holder.itemView.getContext())
                        .load(user.getAvatar().getUrl())
                        .fitCenter()
                        .diskCacheStrategy(DiskCacheStrategy.RESULT)
                        .into(finalHolder.mPreview);
                finalHolder.mName.setText(user.getUsername());
                finalHolder.mContactName.setText(getString(R.string.contact_name, name));
            }
        }

        @Override
        public int getItemCount() {
            return mContactFriends.size();
        }
    }

    class ContactFriendsViewHolder extends RecyclerView.ViewHolder {
        public AppCompatTextView mName;
        public AppCompatTextView mContactName;
        public CircleImageView mPreview;
        public AppCompatImageButton force;
        public AppCompatImageButton unforce;
        public ContactFriendsViewHolder(View itemView) {
            super(itemView);
            mName = (AppCompatTextView)itemView.findViewById(R.id.name);
            mContactName = (AppCompatTextView)itemView.findViewById(R.id.contact_friend_name);
            mPreview = (CircleImageView) itemView.findViewById(R.id.preview);
            force = (AppCompatImageButton)itemView.findViewById(R.id.btn_force);
            unforce = (AppCompatImageButton)itemView.findViewById(R.id.btn_force_yet);

        }
    }

    private void loadContactFriends() {
        abortLoading();

        ContactFriendsRunnable runnable = new ContactFriendsRunnable();
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

    private class ContactFriendsRunnable implements Runnable {
        @Override
        public void run() {
            android.os.Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);

            Message message;
            if (mAdapter == null) {
                message = mHandler.obtainMessage();
                message.what = Constant.FETCH_STARTED;
                message.sendToTarget();
            }

            if (Thread.interrupted()) {
                return;
            }

            boolean failed = false;
            try {
                Uri uri = ContactsContract.Contacts.CONTENT_URI;
                ContentResolver contentResolver = getApplicationContext().getContentResolver();
                Cursor cursor = contentResolver.query(uri, null, null, null, null);
                if (cursor == null) {
                    failed = true;
                } else {
                    final Map<String, String> contacts = new HashMap<>();
                    final List<String> numbers = new ArrayList<>();
                    while (cursor.moveToNext()) {
                        String contactId = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID));
                        String name = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
                        Cursor phones = contentResolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
                                ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = " + contactId, null, null);
                        if (phones != null) {
                            while (phones.moveToNext()) {
                                String phoneNumber = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                                contacts.put(phoneNumber, name);
                                if (!numbers.contains(phoneNumber)) {
                                    numbers.add(phoneNumber);
                                }
                            }
                            phones.close();
                        }
                    }
                    cursor.close();
                    BmobQuery<User> query = new BmobQuery<>();
                    query.findObjects(new FindListener<User>() {
                        @Override
                        public void done(List<User> list, BmobException e) {
                            if (e == null) {
                                for (User user : list) {
                                    if (numbers.contains(user.getMobilePhoneNumber())) {
                                        String name = contacts.get(user.getMobilePhoneNumber());
                                        if (name != null) {
                                            mContactFriends.add(Pair.create(user, name));
                                        }
                                    }
                                }
                                Message message = mHandler.obtainMessage();
                                message.what = Constant.FETCH_COMPLETED;
                                message.sendToTarget();
                            }
                        }
                    });
                }
            } catch (Throwable throwable) {
                failed = true;
            }

            if (failed) {
                message = mHandler.obtainMessage();
                message.what = Constant.ERROR;
                message.sendToTarget();
            }

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
