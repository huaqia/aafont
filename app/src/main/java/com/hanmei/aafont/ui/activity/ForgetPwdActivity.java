package com.hanmei.aafont.ui.activity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.hanmei.aafont.R;
import com.hanmei.aafont.utils.BackendUtils;
import com.hanmei.aafont.utils.NetworkUtils;

import butterknife.BindView;
import butterknife.OnClick;

public class ForgetPwdActivity extends BaseActivity {
    private static final int CHECK_MOBILE_EXIST = 0X11;
    private static final int VERIFY_CODE_SUCCESS = 0X12;
    private static final int VERIFY_CODE_FAILURE = 0X13;
    private static final int RESET_PASSWORD_SUCCESS = 0x21;

    @BindView(R.id.resetPwd_mobile_edt)
    EditText mMobileNumber;

    @BindView(R.id.resetPwd_code_edt)
    EditText mVerifyCode;

    @BindView(R.id.resetPwd_get_code_btn)
    Button mGetCode;

    @BindView(R.id.resetPwd_set_password)
    EditText mSetPwd;

    @BindView(R.id.resetPwd_again_password)
    EditText mAgainPwd;

    private MyCountTimer mTimer;
    private Handler mHandler=new Handler(){

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case CHECK_MOBILE_EXIST:
                    mTimer=new MyCountTimer(60000,1000);
                    mTimer.start();
                    getVerifyCode();
                    break;
                case VERIFY_CODE_SUCCESS:
                    break;
                case RESET_PASSWORD_SUCCESS:
                    resultSuccessDialog();
                    break;
            }
            super.handleMessage(msg);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_reset_password);
        toolbar.setNavigationIcon(R.drawable.back);
        toolbar.setTitle("找回密码");
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
        setContentView(R.layout.activity_forget_pwd);
    }

    @OnClick(R.id.resetPwd_get_code_btn)
    public void getCodeClick(View view) {
        final Context context = getApplicationContext();
        if(NetworkUtils.isNetworkAvailable(context)) {
            final String mobile = mMobileNumber.getText().toString();
            if (TextUtils.isEmpty(mobile)) {
                Toast.makeText(context,"手机号不能为空，请输入",Toast.LENGTH_SHORT).show();
            } else if(!NetworkUtils.isNumber(mobile)){
                Toast.makeText(context,"手机号格式不正确",Toast.LENGTH_SHORT).show();
            } else {
                BackendUtils.checkUserPhoneNumber(mobile, new BackendUtils.DoneCallback() {
                    @Override
                    public void onDone(boolean success, int code) {
                        if (success) {
                            if (code <= 0) {
                                Toast.makeText(context, "该手机号码未注册或绑定账户", Toast.LENGTH_SHORT).show();
                            } else {
                                Message msg = new Message();
                                msg.what = CHECK_MOBILE_EXIST;
                                mHandler.handleMessage(msg);
                            }
                        } else {
                            Toast.makeText(context,"服务器错误，请稍后重试！！",Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        } else {
            Toast.makeText(context,"未检测到网络！！",Toast.LENGTH_SHORT).show();
        }
    }

    @OnClick(R.id.resetPws_sure)
    public void resetSureClick(View view) {
        final Context context = getApplicationContext();
        if(NetworkUtils.isNetworkAvailable(context)){
            String code = mVerifyCode.getText().toString();
            String setPwd = mSetPwd.getText().toString();
            String againPwd = mAgainPwd.getText().toString();
            if(TextUtils.isEmpty(setPwd)||TextUtils.isEmpty(againPwd)){
                Toast.makeText(context,"密码不能为空",Toast.LENGTH_SHORT).show();
            } else if(TextUtils.isEmpty(code)){
                Toast.makeText(context,"验证码不能为空",Toast.LENGTH_SHORT).show();
            } if(setPwd.equals(againPwd)) {
                BackendUtils.resetPasswordBySMSCode(code, setPwd, new BackendUtils.DoneCallback() {
                    @Override
                    public void onDone(boolean success, int code) {
                        if (success) {
                            Message msg = new Message();
                            msg.what = RESET_PASSWORD_SUCCESS;
                            mHandler.handleMessage(msg);
                        } else {
                            Toast.makeText(context, "密码重置失败，请稍后尝试！", Toast.LENGTH_SHORT).show();
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

    private void getVerifyCode() {
        BackendUtils.requestSMSCode(mMobileNumber.getText().toString(), new BackendUtils.DoneCallback() {
            @Override
            public void onDone(boolean success, int code) {
                if (!success) {
                    mTimer.cancel();
                    Toast.makeText(getApplicationContext(), "获取验证码失败，请稍后重试", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    class MyCountTimer extends CountDownTimer {
        public MyCountTimer(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);
        }
        @Override
        public void onTick(long millisUntilFinished) {
            mGetCode.setText((millisUntilFinished / 1000) + "s后重发");
        }
        @Override
        public void onFinish() {
            mGetCode.setText("重新发送");
            mGetCode.setEnabled(true);
        }
    }

    private void resultSuccessDialog(){
        new AlertDialog.Builder(ForgetPwdActivity.this)
                .setTitle("提示")
                .setMessage("修改密码成功！请重新登录")
                .setPositiveButton("确定", new DialogInterface.OnClickListener(){
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent=new Intent(ForgetPwdActivity.this,LoginActivity.class);
                        intent.putExtra(RegisterActivity.FROM_REGISTER_USERNAME, mMobileNumber.getText().toString());
                        intent.putExtra(RegisterActivity.FROM_REGISTER_PWD, mSetPwd.getText().toString());
                        startActivity(intent);
                        ForgetPwdActivity.this.finish();
                    }
                }).create().show();
    }
}
