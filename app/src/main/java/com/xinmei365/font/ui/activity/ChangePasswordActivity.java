package com.xinmei365.font.ui.activity;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.Toast;

import com.xinmei365.font.R;
import com.xinmei365.font.utils.BackendUtils;
import com.xinmei365.font.utils.MiscUtils;
import com.xinmei365.font.utils.NetworkUtils;

import butterknife.BindView;
import butterknife.OnClick;
import cn.bmob.v3.exception.BmobException;

public class ChangePasswordActivity extends BaseActivity {
    @BindView(R.id.close)
    AppCompatImageView mClose;
    @BindView(R.id.resetPwd_old_password)
    EditText mOldPassword;
    @BindView(R.id.resetPwd_set_password)
    EditText mNewPassword;
    @BindView(R.id.resetPwd_again_password)
    EditText mNewAgainPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        mClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    @Override
    protected void setMyContentView() {
        setContentView(R.layout.activity_change_pwd);
    }

    @OnClick(R.id.resetPws_sure)
    public void resetSureClick(View view) {
        final Context context = getApplicationContext();
        if(NetworkUtils.isNetworkAvailable(context)){
            String oldPassword = mOldPassword.getText().toString();
            String newPassword = mNewPassword.getText().toString();
            String newAgainPassword = mNewAgainPassword.getText().toString();
            if (TextUtils.isEmpty(oldPassword) || TextUtils.isEmpty(newPassword) || TextUtils.isEmpty(newAgainPassword)){
                MiscUtils.makeToast(this, "密码不能为空", false);
            } else if (NetworkUtils.isPasswordNumber(newPassword)) {
                MiscUtils.makeToast(this, "新密码位数不正确，请重新输入", false);
            } else if(!newPassword.equals(newAgainPassword)) {
                MiscUtils.makeToast(this, "两次输入的密码不一致", false);
            } else {
                BackendUtils.updateCurrentUserPassword(oldPassword, newPassword, new BackendUtils.DoneCallback() {
                    @Override
                    public void onDone(BmobException e) {
                        if (e == null) {
                            MiscUtils.makeToast(ChangePasswordActivity.this, "密码修改成功", false);
                            finish();
                        } else if (e.getErrorCode() == 210) {
                            MiscUtils.makeToast(ChangePasswordActivity.this, "旧密码错误", false);
                        } else {
                            BackendUtils.handleException(e, ChangePasswordActivity.this);
                        }
                    }
                });
            }
        } else {
            MiscUtils.makeToast(this, "未检测到网络", false);
        }
    }
}
