package com.hanmei.aafont.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
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

public class LoginActivity extends BaseActivity {
    @BindView(R.id.edt_login_username)
    EditText mUserName;

    @BindView(R.id.edt_login_password)
    EditText mPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getIntent() != null) {
            Intent intent = getIntent();
            String username = intent.getStringExtra(RegisterActivity.FROM_REGISTER_USERNAME);
            String pwd = intent.getStringExtra(RegisterActivity.FROM_REGISTER_PWD);
            if (!TextUtils.isEmpty(pwd)) {
                mPassword.setText(pwd);
            }
            if (!TextUtils.isEmpty(username)) {
                mUserName.setText(username);
            }
        }
    }

    @Override
    protected void setMyContentView() {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_login);
    }

    @OnClick(R.id.btn_login)
    public void loginClick(View v) {
        final Context context = getApplicationContext();
        boolean isNetworkAvailable = NetworkUtils.isNetworkAvailable(context);
        if (isNetworkAvailable && checkInput()) {
            String userName = mUserName.getText().toString();
            String password = mPassword.getText().toString();
            BackendUtils.loginByAccount(userName, password, new BackendUtils.DoneCallback() {
                @Override
                public void onDone(boolean success, int code) {
                    if (success) {
                        Toast.makeText(context,"登录成功",Toast.LENGTH_SHORT).show();
                        Intent intent=new Intent(LoginActivity.this, MainActivity.class);
                        startActivity(intent);
                        LoginActivity.this.finish();
                    } else {
                        Toast.makeText(context,"用户名或密码错误",Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }else if (!isNetworkAvailable) {
            Toast.makeText(context,"未连接网络！！",Toast.LENGTH_SHORT).show();
        }
    }

    @OnClick(R.id.text_login_register)
    public void registerClick(View v) {
        startActivity(new Intent( this, RegisterActivity.class) );
    }

    @OnClick(R.id.text_forget_pwd)
    public void forgetPwdClick(View v) {
        startActivity(new Intent( this, ForgetPwdActivity.class) );
    }

    private boolean checkInput() {
        String userName = mUserName.getText().toString();
        String password = mPassword.getText().toString();

        if (userName.length() > 0 && password.length() > 0 ) {
            return true;
        } else if (userName.length() <= 0) {
            Toast.makeText(getApplicationContext(),"用户名不能为空",Toast.LENGTH_SHORT).show();

        } else {
            Toast.makeText(getApplicationContext(),"密码不能为空",Toast.LENGTH_SHORT).show();
        }
        return false;
    }
}
