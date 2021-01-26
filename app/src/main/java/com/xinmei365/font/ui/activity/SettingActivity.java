package com.xinmei365.font.ui.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.xinmei365.font.ActivityCollector;
import com.xinmei365.font.R;
import com.xinmei365.font.model.DraftData;
import com.xinmei365.font.model.User;
import com.xinmei365.font.utils.BackendUtils;
import com.xinmei365.font.utils.FileUtils;
import com.xinmei365.font.utils.MiscUtils;

import java.io.File;
import java.util.ArrayList;

import butterknife.BindView;
import butterknife.OnClick;
import cn.bmob.newim.BmobIM;
import cn.bmob.v3.BmobUser;

public class SettingActivity extends BaseActivity {
    @BindView(R.id.close)
    AppCompatImageView mClose;
    @BindView(R.id.my_draft)
    RelativeLayout mMyDraft;
    @BindView(R.id.my_message)
    RelativeLayout mMyMessage;
    @BindView(R.id.change_passwd)
    RelativeLayout mChangePasswd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        String phoneNumber = BmobUser.getCurrentUser(User.class).getMobilePhoneNumber();
        if (TextUtils.isEmpty(phoneNumber)) {
            mChangePasswd.setVisibility(View.GONE);
        }

//        TextView version_info = (TextView)findViewById(R.id.version_info);
//        version_info.setText("酒店");
//        Typeface typeface = Typeface.createFromAsset(getAssets(), "font/good.ttf");
//        version_info.setTypeface(typeface);
//        TextView version_info2 = (TextView)findViewById(R.id.version_info2);
//        version_info2.setText(String.format("%1$s", BuildConfig.VERSION_NAME));
//
//        TextView version_info3 = (TextView)findViewById(R.id.version_info3);
//        version_info3.setText("酒店");
//        Typeface typeface2 = Typeface.createFromAsset(getAssets(), "font/33.ttf");
//        version_info3.setTypeface(typeface2);
//        TextView version_info4 = (TextView)findViewById(R.id.version_info4);
//        version_info4.setText(String.format("%1$s", BuildConfig.VERSION_NAME));
    }

    @Override
    protected void setMyContentView() {
        setContentView(R.layout.activity_setting);
    }

    @Override
    protected void onResume() {
        super.onResume();
        File jsonFile = new File(FileUtils.getFileDir(getApplicationContext(), "note"), "note.json");
        Gson gson = new Gson();
        final ArrayList<DraftData> noteDatas;
        if (jsonFile.exists()) {
            String json = FileUtils.readFileToString(jsonFile);
            if (json != null) {
                noteDatas = gson.fromJson(json, new TypeToken<ArrayList<DraftData>>(){}.getType());
            } else {
                noteDatas = null;
            }
        } else {
            noteDatas = null;
        }
        mMyDraft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (noteDatas != null && noteDatas.size() > 0) {
                    startActivity(new Intent(getApplicationContext(), DraftsActivity.class));
                } else {
                    MiscUtils.makeToast(SettingActivity.this, "没有草稿", false);
                }
            }
        });
        mMyMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(), MessageCenterActivity.class));
            }
        });
    }

    @OnClick({R.id.push_notice, R.id.change_passwd, R.id.help_feedback, R.id.give_star, R.id.about,R.id.logout})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.push_notice:
                startActivity(new Intent(this, PushSettingActivity.class));
                break;
            case R.id.change_passwd:
                startActivity(new Intent(this, ChangePasswordActivity.class));
                break;
            case R.id.help_feedback:
                startActivity(new Intent(this, FeedbackActivity.class));
                break;
            case R.id.give_star:
                try {
                    Uri uri = Uri.parse("market://details?id=" + getPackageName());
                    Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(Intent.createChooser(intent, "评分"));
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case R.id.about:
                startActivity(new Intent(this, AboutActivity.class));
                break;
            case R.id.logout:
                BmobIM.getInstance().disConnect();
                BackendUtils.logOut();
                ActivityCollector.finishAll();
                startActivity(new Intent(this, LoginActivity.class));
                break;
        }
    }
}
