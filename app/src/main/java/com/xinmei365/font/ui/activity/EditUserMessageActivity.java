package com.xinmei365.font.ui.activity;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.widget.AppCompatEditText;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.AppCompatRadioButton;
import android.support.v7.widget.AppCompatTextView;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.xinmei365.font.MyApplication;
import com.xinmei365.font.R;
import com.xinmei365.font.model.User;
import com.xinmei365.font.utils.BackendUtils;

import java.io.File;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;
import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.BmobUser;
import cn.bmob.v3.datatype.BmobFile;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FindListener;
import cn.bmob.v3.listener.UpdateListener;
import cn.bmob.v3.listener.UploadFileListener;
import de.hdodenhof.circleimageview.CircleImageView;

public class EditUserMessageActivity extends BaseActivity {

    private static final String TAG = "EditUserMessageActivity";
    private static final String PACKAGE_URL_SCHEME = "package:";
    public static String Permisson = "当前应用缺少必要的权限。请点击\"设置\" -打开所需权限";

    @BindView(R.id.close)
    AppCompatImageView mClose;
    @BindView(R.id.image_user_icon)
    CircleImageView mUserIcon;
    @BindView(R.id.edit_user_name)
    AppCompatEditText mUserName;
    @BindView(R.id.group_gender)
    RadioGroup mGenderGroup;
//    @BindView(R.id.birthday)
//    AppCompatTextView mBirthday;
    @BindView(R.id.edit_brief_introduction)
    AppCompatEditText mUserIntroduction;
    @BindView(R.id.save_action)
    AppCompatTextView mSave;
    @BindView(R.id.gender_man)
    AppCompatRadioButton mGenderMan;
    @BindView(R.id.gender_woman)
    AppCompatRadioButton mGenderWoman;

    //调用相机返回图片文件
    private File tempFile;
    //最后显示的图片
    private String mFile;

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

    private int mGender;
    private Context mContext;
    private View mContentView;
    private PopupWindow mPopupWindow;

//    private int mYear, mMonth, mDay;
//    private DatePickerDialog.OnDateSetListener mDateSetListener = new DatePickerDialog.OnDateSetListener() {
//        @Override
//        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
//            if (view.isShown()) {
//                mYear = year;
//                String month;
//                String day;
//                if (monthOfYear < 9) {
//                    mMonth = monthOfYear + 1;
//                    month = "0" + mMonth;
//                } else {
//                    mMonth = monthOfYear + 1;
//                    month = String.valueOf(mMonth);
//                }
//                if (dayOfMonth < 10) {
//                    mDay = dayOfMonth;
//                    day = "0" + mDay;
//                } else {
//                    mDay = dayOfMonth;
//                    day = String.valueOf(mDay);
//                }
//                String mBirthDayText = mYear + "年" + month + "月" + day + "日";
//                final User currentUser = BmobUser.getCurrentUser(User.class);
//                User newu = new User();
//                newu.update(currentUser.getObjectId(), new UpdateListener() {
//                    @Override
//                    public void done(BmobException e) {
//                        if (e == null) {
//                            Log.e(TAG, "更新个人信息成功");
//                        } else {
//                            Log.e(TAG, "更新个人信息失败" + e.toString());
//                        }
//                    }
//                });
//
//            }
//        }
//    };

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
            uri = FileProvider.getUriForFile(context.getApplicationContext(), "com.xinmei365.font.fileProvider", file);
        } else {
            uri = Uri.fromFile(file);
        }
        return uri;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        final User currentUser = BmobUser.getCurrentUser(User.class);
        mUserName.setText(currentUser.getNickName());
        mContext = getApplicationContext();

        if (currentUser.getAvatar() != null) {
            Glide.with(MyApplication.getInstance())
                    .load(currentUser.getAvatar())
                    .fitCenter()
                    .diskCacheStrategy(DiskCacheStrategy.RESULT)
                    .listener(mRequestListener)
                    .into(mUserIcon);
        }

        mGenderGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.gender_man:
                        mGender = 0;
                        break;
                    case R.id.gender_woman:
                        mGender = 1;
                        break;
                }
            }
        });

//        mBirthday.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                ShowBirthDialog();
//            }
//        });

        mSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String intro = mUserIntroduction.getText().toString();
                String username = mUserName.getText().toString();
                if (intro == null) {
                    Toast.makeText(mContext, "简介不能为空", Toast.LENGTH_SHORT).show();
                } else if (username == null) {
                    Toast.makeText(mContext, "姓名不能为空", Toast.LENGTH_SHORT).show();
                } else {
                    currentUser.setIntro(mUserIntroduction.getText().toString());
                    currentUser.setNickName(mUserName.getText().toString());
                    currentUser.setGender(mGender);
                    if (mFile != null) {
                        final BmobFile file = new BmobFile(new File(mFile));
                        file.upload(new UploadFileListener() {
                            @Override
                            public void done(BmobException e) {
                                if (e == null) {
                                    currentUser.setAvatar(file.getUrl());
                                    currentUser.update(new UpdateListener() {
                                        @Override
                                        public void done(BmobException e) {
                                            if (e == null) {
                                                Toast.makeText(mContext, "修改个人信息成功", Toast.LENGTH_SHORT).show();
                                            } else {
                                                if (e.getErrorCode() == 211 || e.getErrorCode() == 9016) {
                                                    BackendUtils.handleException(e, mContext);
                                                } else {
                                                    Toast.makeText(mContext, "修改个人信息失败", Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                            finish();
                                        }
                                    });
                                } else {
                                    BackendUtils.handleException(e, mContext);
                                }
                            }
                        });
                    } else {
                        currentUser.update(new UpdateListener() {
                            @Override
                            public void done(BmobException e) {
                                if (e == null) {
                                    Toast.makeText(mContext, "修改个人信息成功", Toast.LENGTH_SHORT).show();
                                } else {
                                    if (e.getErrorCode() == 211 || e.getErrorCode() == 9016) {
                                        BackendUtils.handleException(e, mContext);
                                    } else {
                                        Toast.makeText(mContext, "修改个人信息失败", Toast.LENGTH_SHORT).show();
                                    }
                                }
                                finish();
                            }
                        });
                    }
                }
            }
        });

        initData(currentUser);
    }

//    private void ShowBirthDialog() {
//        Calendar c = Calendar.getInstance();
//        mYear = c.get(Calendar.YEAR);
//        mMonth = c.get(Calendar.MONTH);
//        mDay = c.get(Calendar.DAY_OF_MONTH);
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//            new DatePickerDialog(EditUserMessageActivity.this, R.style.DatePickThemeDialog, mDateSetListener, mYear, mMonth, mDay).show();
//        } else {
//            new DatePickerDialog(EditUserMessageActivity.this, mDateSetListener, mYear, mMonth, mDay).show();
//        }
//    }

    private void initData(User currentUser) {
        BmobQuery<User> query = new BmobQuery<>();
        query.addWhereEqualTo("objectId" , currentUser.getObjectId());
        query.findObjects(new FindListener<User>() {
            @Override
            public void done(List<User> list, BmobException e) {
                if (e == null) {
                    User user = list.get(0);
                    mUserIntroduction.setText(user.getIntro());
                    int gender = user.getGender();
                    if (gender == 0) {
                        mGenderMan.setChecked(true);
                        mGenderWoman.setChecked(false);
                    } else if (gender == 1) {
                        mGenderMan.setChecked(false);
                        mGenderWoman.setChecked(true);
                    }
                } else {
                    BackendUtils.handleException(e, getApplicationContext());
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
                initPopupWindowView();
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

    public void initPopupWindowView() {
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
                    Uri contentUri = null;
                    tempFile = new File(Environment.getExternalStorageDirectory().getPath() , "icon.jpg");
                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        contentUri = FileProvider.getUriForFile(EditUserMessageActivity.this , getPackageName() + ".provider" , tempFile);
                        Log.e("getPicFromCamera" , contentUri.toString());
                        }
                    else {
                        contentUri = Uri.fromFile(tempFile);
                        intent.putExtra(MediaStore.EXTRA_OUTPUT , contentUri);
                        }
                    intent.putExtra(MediaStore.EXTRA_OUTPUT , contentUri);
                    startActivityForResult(intent , 2);
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
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
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
        AlertDialog.Builder builder = new AlertDialog.Builder(EditUserMessageActivity.this);
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
                    initPopupWindowView();;
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
                    cropPhoto(data.getData());
                }
                break;
            case 2:
                if (resultCode == RESULT_OK) {
                    if (Build.VERSION.SDK_INT > Build.VERSION_CODES.N){
                        Uri contentUri = FileProvider.getUriForFile(EditUserMessageActivity.this , getPackageName() + ".provider" , tempFile);
                        cropPhoto(contentUri);
                    }else {
                        cropPhoto(Uri.fromFile(tempFile));
                    }
                }
                break;
            case 3:
                if (data != null) {
                    Bitmap photo = BitmapFactory.decodeFile(mFile);
                    mUserIcon.setImageBitmap(photo);
                }
        }
    }

    public void cropPhoto(Uri uri) {
        if (uri == null){
            Log.e("tag" , "uri is null");
        }
        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        intent.setDataAndType(uri, "image/*");
        intent.putExtra("crop", true);
        intent.putExtra("aspectX", 1);
        intent.putExtra("aspectY", 1);
        intent.putExtra("outputX", 250);
        intent.putExtra("outputY", 250);
        intent.putExtra("scale" , true);
        intent.putExtra("return-data", false);
        intent.putExtra("outputFormat" , Bitmap.CompressFormat.JPEG.toString());
        intent.putExtra("noFaceDetection" ,true);
        File out = new File(getPath());
        if (!out.getParentFile().exists()){
            out.getParentFile().mkdir();
        }
        intent.putExtra(MediaStore.EXTRA_OUTPUT , Uri.fromFile(out));
        startActivityForResult(intent, 3);
    }

    public String getPath(){
        if (mFile == null){
            mFile = Environment.getExternalStorageDirectory() + "/" + "head.png";
        }
        return mFile;
    }

    public void setPicToView(Bitmap bitmap) {

    }

}
