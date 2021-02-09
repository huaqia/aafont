package com.xinmei365.font.ui.activity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.AppCompatEditText;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.xinmei365.font.R;
import com.xinmei365.font.utils.BackendUtils;
import com.xinmei365.font.utils.MiscUtils;
import com.xinmei365.font.utils.NetworkUtils;

import butterknife.BindView;
import butterknife.OnClick;
import cn.bmob.v3.exception.BmobException;

public class ForgetPwdActivity extends BaseActivity {
    private static final int CHECK_MOBILE_EXIST = 0X11;
    private static final int VERIFY_CODE_SUCCESS = 0X12;
    private static final int VERIFY_CODE_FAILURE = 0X13;
    private static final int RESET_PASSWORD_SUCCESS = 0x21;

    @BindView(R.id.close)
    AppCompatImageView mClose;

    @BindView(R.id.resetPwd_mobile_edt)
    AppCompatEditText mMobileNumber;

    @BindView(R.id.resetPwd_code_edt)
    AppCompatEditText mVerifyCode;

    @BindView(R.id.resetPwd_get_code_btn)
    AppCompatTextView mGetCode;

    @BindView(R.id.resetPwd_set_password)
    AppCompatEditText mSetPwd;

    @BindView(R.id.resetPwd_again_password)
    AppCompatEditText mAgainPwd;

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
        setContentView(R.layout.activity_forget_pwd);
    }

    @OnClick(R.id.resetPwd_get_code_btn)
    public void getCodeClick(View view) {
        final Context context = getApplicationContext();
        if(NetworkUtils.isNetworkAvailable(context)) {
            final String mobile = mMobileNumber.getText().toString();
            if (TextUtils.isEmpty(mobile)) {
                MiscUtils.makeToast(this, "手机号不能为空，请输入", false);
            } else if(!NetworkUtils.isNumber(mobile)){
                MiscUtils.makeToast(this, "手机号格式不正确", false);
            } else {
                BackendUtils.checkUserPhoneNumber(this, mobile, new BackendUtils.CountCallback() {
                    @Override
                    public void onDone(BmobException e, int code) {
                        if (e == null) {
                            if (code <= 0) {
                                MiscUtils.makeToast(ForgetPwdActivity.this, "该手机号码未注册或绑定账户", false);
                            } else {
                                Message msg = new Message();
                                msg.what = CHECK_MOBILE_EXIST;
                                mHandler.handleMessage(msg);
                            }
                        } else {
                            BackendUtils.handleException(e, ForgetPwdActivity.this);
                        }
                    }
                });
            }
        } else {
            MiscUtils.makeToast(this, "未检测到网络！！", false);
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
                MiscUtils.makeToast(this, "密码不能为空", false);
            } else if(TextUtils.isEmpty(code)){
                MiscUtils.makeToast(this, "验证码不能为空", false);
            } if(setPwd.equals(againPwd)) {
                BackendUtils.resetPasswordBySMSCode(code, setPwd, new BackendUtils.DoneCallback() {
                    @Override
                    public void onDone(BmobException e) {
                        if (e == null) {
                            Message msg = new Message();
                            msg.what = RESET_PASSWORD_SUCCESS;
                            mHandler.handleMessage(msg);
                        } else {
                            MiscUtils.makeToast(ForgetPwdActivity.this, "密码重置失败，请稍后尝试！", false);
                        }
                    }
                });
            } else {
                MiscUtils.makeToast(this, "两次输入的密码不一致", false);
            }
        } else {
            MiscUtils.makeToast(this, "未检测到网络", false);
        }
    }

    private void getVerifyCode() {
        BackendUtils.requestSMSCode(mMobileNumber.getText().toString(), new BackendUtils.DoneCallback() {
            @Override
            public void onDone(BmobException e) {
                if (e != null) {
                    mTimer.cancel();
                    MiscUtils.makeToast(ForgetPwdActivity.this, "获取验证码失败，请稍后重试", false);
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
