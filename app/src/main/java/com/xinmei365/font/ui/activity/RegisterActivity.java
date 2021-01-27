package com.xinmei365.font.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.AppCompatEditText;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.AppCompatTextView;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import com.xinmei365.font.R;
import com.xinmei365.font.utils.BackendUtils;
import com.xinmei365.font.utils.MiscUtils;
import com.xinmei365.font.utils.NetworkUtils;

import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.OnClick;
import cn.bmob.v3.exception.BmobException;

public class RegisterActivity extends BaseActivity {
    public static final String FROM_REGISTER_USERNAME = "from_username";
    public static final String FROM_REGISTER_PWD = "from_login_pwd";
    private static final int GET_CODE_SUCCESS = 0x100;
    private static final int GET_CODE_FAILURE = 0x101;
    private static final int VERIFY_CODE_SUCCESS = 0x200;
    private static final int VERIFY_CODE_FAILURE = 0x201;
    private static final int Is_Mobile_Have = 0x31;
    private static final int Is_Mobile_unHave = 0x32;

    @BindView(R.id.close)
    AppCompatImageView mClose;

    @BindView(R.id.edt_register_name)
    AppCompatEditText mName;

    @BindView(R.id.edt_register_mobile)
    AppCompatEditText mMobileNumber;

    @BindView(R.id.edt_register_set_pwd)
    AppCompatEditText mPassword;

    @BindView(R.id.edt_register_verify_code)
    AppCompatEditText mVerifyCode;

    @BindView(R.id.btn_get_rerify_code)
    AppCompatTextView mGetCode;

    private MyCountTimer mTimer;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case GET_CODE_SUCCESS:
                    break;
                case VERIFY_CODE_SUCCESS:
                    final String name = mName.getText().toString();
                    final String phone = mMobileNumber.getText().toString();
                    final String password = mPassword.getText().toString();
                    BackendUtils.signUp(name, phone, phone, password, new BackendUtils.DoneCallback() {
                        @Override
                        public void onDone(BmobException e) {
                            if (e == null) {
                                MiscUtils.makeToast(RegisterActivity.this, "注册成功,请登录", false);
                                Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                                intent.putExtra(FROM_REGISTER_USERNAME, phone);
                                intent.putExtra(FROM_REGISTER_PWD, password);
                                startActivity(intent);
                                finish();
                            } else if (e.getErrorCode() == 202) {
                                MiscUtils.makeToast(RegisterActivity.this, "该手机号已经注册", false);
                            } else {
                                BackendUtils.handleException(e, RegisterActivity.this);
                            }
                        }
                    });
                    break;

                case VERIFY_CODE_FAILURE:
                    MiscUtils.makeToast(RegisterActivity.this, "验证码错误", false);
                    break;

                case Is_Mobile_Have:
                    MiscUtils.makeToast(RegisterActivity.this, "手机号已存在,请重新输入", false);
                    break;
                case Is_Mobile_unHave:
                    mTimer = new MyCountTimer(60000, 1000);
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
        mClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        mHandler.removeCallbacksAndMessages(null);
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
            BackendUtils.countForCondition(conditions, new BackendUtils.CountCallback() {
                @Override
                public void onDone(BmobException e, int code) {
                    if (e == null) {
                        if (code <= 0) {
                            Message msg = new Message();
                            msg.what = Is_Mobile_unHave;
                            mHandler.sendMessage(msg);
                        } else {
                            Message msg = new Message();
                            msg.what = Is_Mobile_Have;
                            mHandler.sendMessage(msg);
                        }
                    } else {
                        BackendUtils.handleException(e, RegisterActivity.this);
                    }
                }
            });
        }
    }

    @OnClick(R.id.btn_register_next_step)
    public void nextStepClick(View view) {
        if (checkInput()) {
            String code = mVerifyCode.getText().toString();
            if (TextUtils.isEmpty(code)) {
                MiscUtils.makeToast(this, "验证码不能为空", false);
            } else if (NetworkUtils.isPasswordNumber(mPassword.getText().toString())) {
                verifyCode(code);
            } else {
                MiscUtils.makeToast(this, "密码位数不正确，请重新输入", false);
            }
        }
    }

    private boolean checkInput() {
        Context context = getApplicationContext();
        //网络是否连接
        if (NetworkUtils.isNetworkAvailable(context)) {
            String name = mName.getText().toString();
            if (TextUtils.isEmpty(name)) {
                MiscUtils.makeToast(this, "昵称不能为空", false);
            } else {
                String mobileNumber = mMobileNumber.getText().toString();
                if (TextUtils.isEmpty(mobileNumber)) {
                    MiscUtils.makeToast(this, "手机号不能为空", false);
                    //检查手机号是否为空
                } else if (NetworkUtils.isMobileNumber(mobileNumber)) {
                    return true;
                } else {
                    MiscUtils.makeToast(this, "手机号格式不正确", false);
                }
            }
        } else {
            MiscUtils.makeToast(this, "未连接网络！！", false);
        }
        return false;
    }

    private void getVerifyCode() {
        BackendUtils.requestSMSCode(mMobileNumber.getText().toString(), new BackendUtils.DoneCallback() {
            @Override
            public void onDone(BmobException e) {
                if (e == null) {
                    Message msg = new Message();
                    msg.what = GET_CODE_SUCCESS;
                    mHandler.handleMessage(msg);
                } else {
                    mTimer.cancel();
                    MiscUtils.makeToast(RegisterActivity.this, "获取验证码失败", false);
                    mGetCode.setEnabled(true);
                    mGetCode.setText(R.string.text_get_verify_code);
                }
            }
        });
    }

    private void verifyCode(String code) {
        BackendUtils.verifySmsCode(mMobileNumber.getText().toString(), code, new BackendUtils.DoneCallback() {
            @Override
            public void onDone(BmobException e) {
                if (e == null) {
                    Message msg = new Message();
                    msg.what = VERIFY_CODE_SUCCESS;
                    mHandler.handleMessage(msg);
                } else {
                    Message msg = new Message();
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
            mGetCode.setClickable(true);
        }
    }
}
