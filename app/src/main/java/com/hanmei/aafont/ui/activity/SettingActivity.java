package com.hanmei.aafont.ui.activity;

import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

import com.hanmei.aafont.ActivityCollector;
import com.hanmei.aafont.BuildConfig;
import com.hanmei.aafont.R;
import com.hanmei.aafont.utils.BackendUtils;

import butterknife.OnClick;

public class SettingActivity extends BaseActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_set);
        toolbar.setTitle(R.string.setting_title);
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.drawable.back);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        TextView version_info = (TextView)findViewById(R.id.version_info);
        version_info.setText(String.format("%1$s %2$s", getString(R.string.version_info), BuildConfig.VERSION_NAME));
    }

    @Override
    protected void setMyContentView() {
        setContentView(R.layout.activity_setting);
    }

    @OnClick({R.id.push_notice, R.id.change_passwd, R.id.help_feedback, R.id.give_star, R.id.btn_finish_setting, R.id.btn_logout})
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
            case R.id.btn_finish_setting:
                finish();
                break;
            case R.id.btn_logout:
                BackendUtils.logOut();
                ActivityCollector.finishAll();
                startActivity(new Intent(this, LoginActivity.class));
                break;
        }
    }
}
