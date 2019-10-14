package com.hanmei.aafont.ui.activity;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.AppCompatEditText;
import android.support.v7.widget.AppCompatRadioButton;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.hanmei.aafont.R;
import com.hanmei.aafont.model.User;

import java.io.File;
import java.util.Calendar;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;
import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.BmobUser;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FindListener;
import cn.bmob.v3.listener.UpdateListener;
import de.hdodenhof.circleimageview.CircleImageView;

public class EditUserMessageActivity extends BaseActivity {

    private static final String TAG = "EditUserMessageActivity";
    private static final String PACKAGE_URL_SCHEME = "package:";
    public static String Permisson = "当前应用缺少必要的权限。请点击\"设置\" -打开所需权限";
    @BindView(R.id.image_user_icon)
    CircleImageView mUserIcon;
    @BindView(R.id.edit_user_name)
    AppCompatEditText mUserName;
    @BindView(R.id.group_sex)
    RadioGroup mSexGroup;
    @BindView(R.id.birthday)
    AppCompatTextView mBirthday;
    @BindView(R.id.edit_brief_introduction)
    AppCompatEditText mUserIntroduction;
    @BindView(R.id.setting_btn)
    AppCompatButton mSetButton;
    @BindView(R.id.sex_man)
    AppCompatRadioButton mSexMan;
    @BindView(R.id.sex_woman)
    AppCompatRadioButton mSexWoman;
    RequestListener mRequestListener = new RequestListener() {
        @Override
        public boolean onException(Exception e, Object model, Target target, boolean isFirstResource) {
            Log.e(TAG, "onException: " + e.toString() + "  model:" + model + " isFirstResource: " + isFirstResource);
            return false;
        }

        @Override
        public boolean onResourceReady(Object resource, Object model, Target target, boolean isFromMemoryCache, boolean isFirstResource) {
            Log.e(TAG, "model:" + model + " isFirstResource: " + isFirstResource);
            return false;
        }
    };
    Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    User user = (User) msg.obj;
                    mUserIntroduction.setHint(user.getIntro());
                    mBirthday.setText(user.getBirthday());
                    int sex = user.getSex();
                    if (sex == 0) {
                        mSexMan.setChecked(true);
                        mSexWoman.setChecked(false);
                    } else if (sex == 1) {
                        mSexMan.setChecked(false);
                        mSexWoman.setChecked(true);
                    }
                    break;
            }
        }
    };
    private int mSex;
    private Context mContext;
    private View mContentView;
    private PopupWindow mPopupWindow;
    private Bitmap mIcon;
    private int mYear, mMonth, mDay;
    private DatePickerDialog.OnDateSetListener mDateSetListener = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
            if (view.isShown()) {
                mYear = year;
                String month;
                String day;
                if (monthOfYear < 9) {
                    mMonth = monthOfYear + 1;
                    month = "0" + mMonth;
                } else {
                    mMonth = monthOfYear + 1;
                    month = String.valueOf(mMonth);
                }
                if (dayOfMonth < 10) {
                    mDay = dayOfMonth;
                    day = "0" + mDay;
                } else {
                    mDay = dayOfMonth;
                    day = String.valueOf(mDay);
                }
                mMonth = monthOfYear;

                String mBirthDayText = String.format(mYear + "年" + month + "月" + day + "日");
                User newu = new User();
                newu.setBirthday(mBirthDayText);
                final User currentUser = BmobUser.getCurrentUser(User.class);
                newu.update(currentUser.getObjectId(), new UpdateListener() {
                    @Override
                    public void done(BmobException e) {
                        if (e == null) {
                            Log.e(TAG, "更新个人信息成功");
                        } else {
                            Log.e(TAG, "更新个人信息失败" + e.toString());
                        }
                    }
                });

            }
        }
    };

    public static void startActionCapture(Activity activity, File file, int requestCode) {
        if (activity == null) {
            return;
        }
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, getUriForFile(activity, file));
        activity.startActivityForResult(intent, requestCode);
    }

    private static Uri getUriForFile(Context context, File file) {
        if (context == null || file == null) {
            throw new NullPointerException();
        }
        Uri uri;
        if (Build.VERSION.SDK_INT >= 24) {
            uri = FileProvider.getUriForFile(context.getApplicationContext(), "com.hanmei.aafont.fileProvider", file);
        } else {
            uri = Uri.fromFile(file);
        }
        return uri;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_set);
        toolbar.setTitle(R.string.setting_user_title);
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.drawable.back);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        final User currentUser = BmobUser.getCurrentUser(User.class);
        mUserName.setHint(currentUser.getUsername());
        mContext = getApplicationContext();

        Glide.with(mContext)
                .load(currentUser.getAvatar().getUrl())
                .fitCenter()
                .diskCacheStrategy(DiskCacheStrategy.RESULT)
                .listener(mRequestListener)
                .into(mUserIcon);

        mSexGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.sex_man:
                        mSex = 0;
                        break;
                    case R.id.sex_woman:
                        mSex = 1;
                        break;
                }
            }
        });

        mBirthday.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ShowBirthDialog();
            }
        });

        mSetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String intro = mUserIntroduction.getText().toString();
                String username = mUserName.getText().toString();
                if (intro == null) {
                    Toast.makeText(mContext, "简介不能为空", Toast.LENGTH_SHORT).show();
                } else if (username == null) {
                    Toast.makeText(mContext, "姓名不能为空", Toast.LENGTH_SHORT).show();
                } else {
                    User newUser = new User();
                    newUser.setIntro(mUserIntroduction.getText().toString().equals("") ? mUserIntroduction.getHint().toString() : mUserIntroduction.getText().toString());
                    newUser.setUsername(mUserName.getText().toString().equals("") ? mUserName.getHint().toString() : mUserName.getText().toString());
                    newUser.setSex(mSex);
                    newUser.update(currentUser.getObjectId(), new UpdateListener() {
                        @Override
                        public void done(BmobException e) {
                            if (e == null) {
                                Log.e(TAG, "更新个人信息成功");
                            } else {
                                Log.e(TAG, "更新个人信息失败" + e.toString());
                            }
                        }
                    });
                    currentUser.setUsername(mUserName.getText().toString().equals("") ? mUserName.getHint().toString() : mUserName.getText().toString());
                    currentUser.update(new UpdateListener() {
                        @Override
                        public void done(BmobException e) {
                            if (e == null) {
                                Log.e(TAG, "本地信息更新成功");
                            } else {
                                Log.e(TAG, "本地信息更新失败" + e.toString());
                            }
                        }
                    });
                    Toast.makeText(mContext, "修改个人信息成功", Toast.LENGTH_SHORT).show();
                }
                Intent intent = new Intent("android.intent.action.CART_BROADCAST");
                intent.putExtra("data", "refresh");
                LocalBroadcastManager.getInstance(EditUserMessageActivity.this).sendBroadcast(intent);
                sendBroadcast(intent);
                finish();
            }
        });

        initData(currentUser);
    }

    private void ShowBirthDialog() {
        Calendar c = Calendar.getInstance();
        mYear = c.get(Calendar.YEAR);
        mMonth = c.get(Calendar.MONTH);
        mDay = c.get(Calendar.DAY_OF_MONTH);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            new DatePickerDialog(EditUserMessageActivity.this, R.style.DatePickThemeDialog, mDateSetListener, mYear, mMonth, mDay).show();
        } else {
            new DatePickerDialog(EditUserMessageActivity.this, mDateSetListener, mYear, mMonth, mDay).show();
        }
    }

    private void initData(User currentUser) {
        BmobQuery<User> query = new BmobQuery<>();
        query.addWhereEqualTo("username", currentUser.getUsername());
        query.order("-createdAt");
        query.findObjects(new FindListener<User>() {
            @Override
            public void done(List<User> list, BmobException e) {
                if (e == null) {
                    User user = list.get(0);
                    Message msg = mHandler.obtainMessage();
                    msg.what = 1;
                    msg.obj = user;
                    mHandler.sendMessage(msg);
                }
            }
        });
    }

    @Override
    protected void setMyContentView() {
        setContentView(R.layout.activity_edit_user);
    }

    @OnClick(R.id.image_user_icon)
    public void onClick() {
        showTypeWindow();
    }

    private void showTypeWindow() {
        if (mPopupWindow != null && mPopupWindow.isShowing()) {
            mPopupWindow.dismiss();
            return;
        } else {
            if (ContextCompat.checkSelfPermission(EditUserMessageActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                Log.e(TAG, "拥有读写权限");
                initmPopupWindowView();
            } else {
                if (ActivityCompat.shouldShowRequestPermissionRationale(EditUserMessageActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    ActivityCompat.requestPermissions(EditUserMessageActivity.this, new String[]{
                            Manifest.permission.WRITE_EXTERNAL_STORAGE
                    }, 140);
                } else {
                    showPermissionDialog();
                }
            }
        }
    }

    public void initmPopupWindowView() {
        mContentView = LayoutInflater.from(EditUserMessageActivity.this).inflate(R.layout.dialog_select_photo, null);
        mPopupWindow = new PopupWindow(mContentView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        mPopupWindow.setFocusable(true);
        mPopupWindow.setBackgroundDrawable(new BitmapDrawable());
        mPopupWindow.setOutsideTouchable(true);
        mPopupWindow.setTouchable(true);
        mPopupWindow.setAnimationStyle(R.style.select_photo_animation);
        View parentView = LayoutInflater.from(this).inflate(R.layout.activity_edit_user, null);
        mPopupWindow.showAtLocation(parentView, Gravity.BOTTOM, 0, 0);

        TextView photoGraph = (TextView) mContentView.findViewById(R.id.photograph);
        TextView albums = (TextView) mContentView.findViewById(R.id.albums);
        LinearLayout cancel = (LinearLayout) mContentView.findViewById(R.id.cancel);

        photoGraph.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                        Toast.makeText(EditUserMessageActivity.this, "请开启相机权限后重试", Toast.LENGTH_SHORT).show();
                    } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        String CAMERA_PERMISSION = Manifest.permission.CAMERA;
                        if (ContextCompat.checkSelfPermission(EditUserMessageActivity.this, CAMERA_PERMISSION) == PackageManager.PERMISSION_GRANTED) {
                            try {
                                startActionCapture(EditUserMessageActivity.this, new File(Environment.getExternalStorageDirectory(), "icon.jpg"), 2);
                                mPopupWindow.dismiss();
                            } catch (Exception e) {
                                Log.e(TAG, e.toString());
                                Toast.makeText(EditUserMessageActivity.this, "相机无法启动，请先开启相机权限" + e.toString(), Toast.LENGTH_LONG).show();
                            }
                        } else {
                            if (ActivityCompat.shouldShowRequestPermissionRationale(EditUserMessageActivity.this, CAMERA_PERMISSION)) {
                                ActivityCompat.requestPermissions(EditUserMessageActivity.this, new String[]{CAMERA_PERMISSION}, 110);
                            } else {
                                showPermissionDialog();
                            }
                        }
                    }
                } catch (Exception e) {
                    Log.e(TAG, e.toString());
                }
                if (mPopupWindow != null) {
                    mPopupWindow.dismiss();
                }
            }
        });

        albums.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                        Toast.makeText(EditUserMessageActivity.this, "请开启相机权限后重试", Toast.LENGTH_SHORT).show();
                    } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        String CAMERA_PERMISSION = Manifest.permission.CAMERA;
                        if (ContextCompat.checkSelfPermission(EditUserMessageActivity.this, CAMERA_PERMISSION) == PackageManager.PERMISSION_GRANTED) {
                            Intent intent = new Intent(Intent.ACTION_PICK, null);
                            intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
                            startActivityForResult(intent, 1);
                        } else {
                            if (ActivityCompat.shouldShowRequestPermissionRationale(EditUserMessageActivity.this, CAMERA_PERMISSION)) {
                                ActivityCompat.requestPermissions(EditUserMessageActivity.this, new String[]{CAMERA_PERMISSION}, 110);
                            } else {
                                showPermissionDialog();
                            }
                        }
                    }
                } catch (Exception e) {
                    Log.e(TAG, e.toString());
                }

                if (mPopupWindow != null) {
                    mPopupWindow.dismiss();
                }
            }
        });

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPopupWindow.dismiss();
            }
        });
    }

    private void showPermissionDialog() {
        android.app.AlertDialog.Builder builder = new AlertDialog.Builder(EditUserMessageActivity.this);
        builder.setTitle("帮助");
        builder.setMessage(Permisson);
        //拒绝
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        builder.setNegativeButton("设置", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                intent.setData(Uri.parse(PACKAGE_URL_SCHEME + getPackageName()));
                startActivity(intent);
            }
        });
        builder.setCancelable(false);
        builder.show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 110:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    try {
                        startActionCapture(EditUserMessageActivity.this, new File(Environment.getExternalStorageDirectory(), "icon.jpg"), 2);
                        mPopupWindow.dismiss();
                    } catch (Exception e) {
                        Log.e(TAG, e.toString());
                        Toast.makeText(EditUserMessageActivity.this, "相机无法启动，请先开启相机权限" + e.toString(), Toast.LENGTH_LONG).show();
                    }
                    //点击空白区域
                    if(mPopupWindow!=null){
                        mPopupWindow.dismiss();
                    }

                } else {
                    showPermissionDialog();
                }
                break;
            case 120:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Intent intent = new Intent(Intent.ACTION_PICK, null);
                    intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
                    startActivityForResult(intent, 1);
                    //点击空白区域
                    if(mPopupWindow!=null){
                        mPopupWindow.dismiss();
                    }
                } else {
                    showPermissionDialog();
                }
                break;
            case 140:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    initmPopupWindowView();;
                    //点击空白区域
                    if(mPopupWindow!=null){
                        mPopupWindow.dismiss();
                    }

                } else {
                    showPermissionDialog();
                }
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case 1:
                if (resultCode == RESULT_OK) {
                    Log.e(TAG, "第一步");
                    cropPhoto(data.getData());
                }
                break;
            case 2:
                Uri uri;
                File temp = new File(Environment.getExternalStorageDirectory() + "/icon.jpg");
                if (resultCode == RESULT_OK) {
                    if (Build.VERSION.SDK_INT >= 24) {
                        uri = FileProvider.getUriForFile(getApplicationContext(), "com.hanmei.aafont.fileProvider", temp);
                    } else {
                        uri = Uri.fromFile(temp);
                    }
                    cropPhoto(uri);
                }
            case 3:
                Log.e(TAG, "到这里了" + data);
                if (data != null) {
                    Log.e(TAG, "第三步");
                    Bundle extras = data.getExtras();
                    mIcon = extras.getParcelable("data'");
                    Log.e(TAG, extras.toString());
                    if (mIcon != null) {
                        setPicToView(mIcon);
                        Log.e(TAG, "第四步");
                        mUserIcon.setImageBitmap(mIcon);
                    }
                }
        }
    }

    public void cropPhoto(Uri uri) {
        Log.e(TAG, "第二步" + uri.toString());
        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setDataAndType(uri, "image/*");
        intent.putExtra("crop", true);
        intent.putExtra("aspectX", 1);
        intent.putExtra("aspectY", 1);
        intent.putExtra("outputX", 250);
        intent.putExtra("outputY", 250);
        intent.putExtra("return-data", true);
        Log.e(TAG , intent.toString());
        startActivityForResult(intent, 3);
    }

    public void setPicToView(Bitmap bitmap) {

    }

}
