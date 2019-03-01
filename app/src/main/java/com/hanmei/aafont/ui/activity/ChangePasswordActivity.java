package com.hanmei.aafont.ui.activity;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.Toast;

import com.hanmei.aafont.R;
import com.hanmei.aafont.utils.BackendUtils;
import com.hanmei.aafont.utils.NetworkUtils;

import butterknife.BindView;
import butterknife.OnClick;

public class ChangePasswordActivity extends BaseActivity {
    @BindView(R.id.resetPwd_old_password)
    EditText mOldPassword;
    @BindView(R.id.resetPwd_set_password)
    EditText mNewPassword;
    @BindView(R.id.resetPwd_again_password)
    EditText mNewAgainPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_reset_password);
        toolbar.setNavigationIcon(R.drawable.back);
        toolbar.setTitle("重置密码");
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }

    @Override
    protected void setMyContentView() {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_change_pwd);
    }

    @OnClick(R.id.resetPws_sure)
    public void resetSureClick(View view) {
        final Context context = getApplicationContext();
        if(NetworkUtils.isNetworkAvailable(context)){
            String oldPassword = mOldPassword.getText().toString();
            String newPassword = mNewPassword.getText().toString();
            String newAgainPassword = mNewAgainPassword.getText().toString();
            if(TextUtils.isEmpty(oldPassword) || TextUtils.isEmpty(newPassword) || TextUtils.isEmpty(newAgainPassword)){
                Toast.makeText(context,"密码不能为空",Toast.LENGTH_SHORT).show();
            } else if(newPassword.equals(newAgainPassword)) {
                BackendUtils.updateCurrentUserPassword(oldPassword, newPassword, new BackendUtils.DoneCallback() {
                    @Override
                    public void onDone(boolean success, int code) {
                        if (success) {
                            Toast.makeText(context,"密码修改成功",Toast.LENGTH_SHORT).show();
                            finish();
                        } else {
                            Toast.makeText(context,"旧密码错误",Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            } else {
                Toast.makeText(context,"两次输入的密码不一致",Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(getApplicationContext(),"未检测到网络",Toast.LENGTH_SHORT).show();
        }
    }
}
