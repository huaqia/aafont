package com.hanmei.aafont.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.hanmei.aafont.R;
import com.hanmei.aafont.utils.BackendUtils;
import com.hanmei.aafont.utils.NetworkUtils;

import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.OnClick;

public class RegisterActivity extends BaseActivity {
    private static final int GET_CODE_SUCCESS = 0x100;
    private static final int GET_CODE_FAILURE = 0x101;
    private static final int VERIFY_CODE_SUCCESS = 0x200;
    private static final int VERIFY_CODE_FAILURE = 0x201;
    private static final int Is_Mobile_Have = 0x31;
    private static final int Is_Mobile_unHave = 0x32;

    public static final String FROM_REGISTER_USERNAME = "from_username";
    public static final String FROM_REGISTER_PWD = "from_login_pwd";

    @BindView(R.id.edt_register_name)
    EditText mName;

    @BindView(R.id.edt_register_mobile)
    EditText mMobileNumber;

    @BindView(R.id.edt_register_set_pwd)
    EditText mPassword;

    @BindView(R.id.edt_register_verify_code)
    EditText mVerifyCode;

    @BindView(R.id.btn_get_rerify_code)
    Button mGetCode;

    private MyCountTimer mTimer;

    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch(msg.what){
                case GET_CODE_SUCCESS:
                    break;
                case VERIFY_CODE_SUCCESS:
                    final String name = mName.getText().toString();
                    final String phone = mMobileNumber.getText().toString();
                    final String password = mPassword.getText().toString();
                    BackendUtils.signUp(name, phone, password, new BackendUtils.DoneCallback() {
                        @Override
                        public void onDone(boolean success, int code) {
                            if (success) {
                                Toast.makeText(getApplicationContext(),"注册成功,请登录",Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                                intent.putExtra(FROM_REGISTER_USERNAME, phone);
                                intent.putExtra(FROM_REGISTER_PWD, password);
                                startActivity(intent);
                                finish();
                            } else {
                                Toast.makeText(getApplicationContext(),"该手机号已经注册",Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                    break;

                case VERIFY_CODE_FAILURE:
                    Toast.makeText(getApplicationContext(),"验证码错误",Toast.LENGTH_SHORT).show();
                    break;

                case Is_Mobile_Have:
                    Toast.makeText(getApplicationContext(),"手机号已存在,请重新输入",Toast.LENGTH_SHORT).show();
                    break;
                case Is_Mobile_unHave:
                    mTimer = new MyCountTimer(60000,1000);
                    mTimer.start();
                    mGetCode.setEnabled(false);
                    getVerifyCode();
                    break;
            }
            super.handleMessage(msg);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_register);
        toolbar.setNavigationIcon(R.drawable.back);
        toolbar.setTitle("注册");
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
        setContentView(R.layout.activity_register);
    }

    @OnClick(R.id.btn_get_rerify_code)
    public void getVerifyCodeClick(View view) {
        if (checkInput()) {
            Map<String, String> equalConditions = new HashMap<>();
            equalConditions.put("mobilePhoneNumber", mMobileNumber.getText().toString());
            Map<String, Map<String, String>> conditions = new HashMap<>();
            conditions.put("equal", equalConditions);
            BackendUtils.countForCondition(conditions, new BackendUtils.DoneCallback() {
                @Override
                public void onDone(boolean success, int code) {
                    if (success) {
                        if (code <= 0) {
                            Message msg = new Message();
                            msg.what = Is_Mobile_unHave;
                            mHandler.sendMessage(msg);
                        } else {
                            Message msg = new Message();
                            msg.what = Is_Mobile_Have;
                            mHandler.sendMessage(msg);
                        }
                    }
                }
            });
        }
    }

    @OnClick(R.id.btn_register_next_step)
    public void nextStepClick(View view) {
        if (checkInput()) {
            String code = mVerifyCode.getText().toString();
            if(TextUtils.isEmpty(code)){
                Toast.makeText(getApplicationContext(),"验证码不能为空",Toast.LENGTH_SHORT).show();
            }else if(NetworkUtils.isPasswordNumber(mPassword.getText().toString())){
                verifyCode(code);
            }else{
                Toast.makeText(getApplicationContext(),"密码位数不正确，请重新输入",Toast.LENGTH_SHORT).show();
            }
        }
    }

    private boolean checkInput(){
        Context context = getApplicationContext();
        if (NetworkUtils.isNetworkAvailable(context)) {
            String name = mName.getText().toString();
            if (TextUtils.isEmpty(name)) {
                Toast.makeText(context,"昵称不能为空",Toast.LENGTH_SHORT).show();
            } else {
                String mobileNumber = mMobileNumber.getText().toString();
                if (TextUtils.isEmpty(mobileNumber)) {
                    Toast.makeText(context, "手机号不能为空", Toast.LENGTH_SHORT).show();
                } else if (NetworkUtils.isMobileNumber(mobileNumber)) {
                    return true;
                } else {
                    Toast.makeText(context, "手机号格式不正确", Toast.LENGTH_SHORT).show();
                }
            }
        } else {
            Toast.makeText(context,"未连接网络！！",Toast.LENGTH_SHORT).show();
        }
        return false;
    }

    private void getVerifyCode(){
        BackendUtils.requestSMSCode(mMobileNumber.getText().toString(), new BackendUtils.DoneCallback() {
            @Override
            public void onDone(boolean success, int code) {
                if (success) {
                    Message msg=new Message();
                    msg.what = GET_CODE_SUCCESS;
                    mHandler.handleMessage(msg);
                } else {
                    mTimer.cancel();
                    Toast.makeText(getApplicationContext(),"获取验证码失败",Toast.LENGTH_SHORT).show();
                    mGetCode.setEnabled(true);
                    mGetCode.setText(R.string.text_get_verify_code);
                }
            }
        });
    }

    private void verifyCode(String code){
        BackendUtils.verifySmsCode(mMobileNumber.getText().toString(), code, new BackendUtils.DoneCallback() {
            @Override
            public void onDone(boolean success, int code) {
                if (success) {
                    Message msg=new Message();
                    msg.what = VERIFY_CODE_SUCCESS;
                    mHandler.handleMessage(msg);
                } else {
                    Message msg=new Message();
                    msg.what = VERIFY_CODE_FAILURE;
                    mHandler.handleMessage(msg);
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
}
