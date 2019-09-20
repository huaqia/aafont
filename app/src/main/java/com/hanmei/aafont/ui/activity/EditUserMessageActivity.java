package com.hanmei.aafont.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.AlertDialogLayout;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.AppCompatEditText;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.hanmei.aafont.R;
import com.hanmei.aafont.model.Relation;
import com.hanmei.aafont.model.User;

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
    @BindView(R.id.image_user_icon)
    CircleImageView mUserIcon;
    @BindView(R.id.edit_user_name)
    AppCompatEditText mUserName;
    @BindView(R.id.group_sex)
    RadioGroup mSexGroup;
    @BindView(R.id.edit_birthday)
    LinearLayout mUserBirthday;
    @BindView(R.id.edit_brief_introduction)
    AppCompatEditText mUserIntroduction;
    @BindView(R.id.setting_btn)
    AppCompatButton mSetButton;
    private boolean gender = true;
    private Context context;

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
        context = getApplicationContext();

        Glide.with(getApplicationContext())
                .load(currentUser.getAvatar().getUrl())
                .fitCenter()
                .diskCacheStrategy(DiskCacheStrategy.RESULT)
                .into(mUserIcon);

        mSexGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.sex_man:
                        gender = true;
                        break;
                    case R.id.sex_woman:
                        gender = false;
                        break;
                }
            }
        });

        mSetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String intro = mUserIntroduction.getText().toString();
                String username = mUserName.getText().toString();
                if (intro == null) {
                    Toast.makeText(context, "简介不能为空", Toast.LENGTH_SHORT).show();
                } else if (username == null) {
                    Toast.makeText(context, "姓名不能为空", Toast.LENGTH_SHORT).show();
                } else {
                    User newUser = new User();
                    newUser.setIntro(mUserIntroduction.getText().toString().equals("") ? mUserIntroduction.getHint().toString() : mUserIntroduction.getText().toString());
                    newUser.setUsername(mUserName.getText().toString().equals("") ? mUserName.getHint().toString() : mUserName.getText().toString());
                    newUser.setGender(gender);
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
                    Toast.makeText(context, "修改个人信息成功", Toast.LENGTH_SHORT).show();
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

    Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case 1:
                    User user = (User) msg.obj;
                    mUserIntroduction.setHint(user.getIntro());
                    break;
            }
        }
    };

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
    public void onClick(){
        showTypeWindow();
    }

    private void showTypeWindow() {
        LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View vPopupWindow = inflater.inflate(R.layout.dialog_select_photo, null, false);//引入弹窗布局
        PopupWindow popupWindow= new PopupWindow(vPopupWindow, ActionBar.LayoutParams.MATCH_PARENT, ActionBar.LayoutParams.WRAP_CONTENT, true);
    }

}
