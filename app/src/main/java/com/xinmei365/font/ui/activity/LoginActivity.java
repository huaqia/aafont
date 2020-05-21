package com.xinmei365.font.ui.activity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.Toast;

import com.xinmei365.font.R;
import com.xinmei365.font.social.BindHelper;
import com.xinmei365.font.utils.BackendUtils;
import com.xinmei365.font.utils.ErrorCode;
import com.xinmei365.font.utils.NetworkUtils;

import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
//        if (true) {
            String userName = mUserName.getText().toString();
            String password = mPassword.getText().toString();
//            String userName = "QQ_B0F0FD93A8FFE3BD7344FFF71C0D3514";
//            String password = "40d0ec5d95cf54d37c525b3d2cf2f67d";
//            String userName = "QQ_2ADF20C7B982E11416863128F91E82DE";
//            String password = "e168f4487f897cd706a1395f023f169f";
            BackendUtils.loginByAccount(userName, password, new BackendUtils.DoneCallback() {
                @Override
                public void onDone(boolean success, int code) {
                    if (success) {
                        Toast.makeText(context, "登录成功", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                        startActivity(intent);
                        LoginActivity.this.finish();
                    } else {
                        Toast.makeText(context, "用户名或密码错误", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        } else if (!isNetworkAvailable) {
            Toast.makeText(context, "未连接网络！！", Toast.LENGTH_SHORT).show();
        }
    }

    @OnClick(R.id.text_login_register)
    public void registerClick(View v) {
        startActivity(new Intent(this, RegisterActivity.class));
    }

    @OnClick(R.id.text_forget_pwd)
    public void forgetPwdClick(View v) {
        startActivity(new Intent(this, ForgetPwdActivity.class));
    }

    @OnClick(R.id.img_weibo_login)
    public void weiboLoginClick(View v) {
        final Context context = getApplication();
        boolean isNetworkAvailable = NetworkUtils.isNetworkAvailable(context);
        if (isNetworkAvailable) {
            BindHelper.getInstance(this).bindWeibo(new BindStatus(0));
        } else {
            Toast.makeText(context, "未连接网络！！", Toast.LENGTH_SHORT).show();
        }
    }

    @OnClick(R.id.img_wechat_login)
    public void weCatLoginClick(View v) {
        final Context context = getApplication();
        boolean isNetworkAvailable = NetworkUtils.isNetworkAvailable(context);
        if (isNetworkAvailable) {
            Intent textShareIntent = new Intent(Intent.ACTION_SEND);
            textShareIntent.setType("*/*");
            final PackageManager packageManager = getPackageManager();
            List<ResolveInfo> shareResolveInfos = packageManager.queryIntentActivities(textShareIntent, 0);
            boolean hasWechat = false;
            for (ResolveInfo resolveInfo : shareResolveInfos) {
                String packageName = resolveInfo.activityInfo.packageName;
                if ("com.tencent.mm".equals(packageName)) {
                    hasWechat = true;
                    BindHelper.getInstance(this).bindWeiXin(new BindStatus(1));
                    break;
                }
            }
            if (!hasWechat) {
                new AlertDialog.Builder(this)
                        .setTitle(R.string.app_name)
                        .setMessage("请下载微信客户端")
                        .setNegativeButton(R.string.cancel, null)
                        .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                Uri uri = Uri.parse("market://details?id=com.tencent.mm");
                                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(Intent.createChooser(intent, "下载微信"));
                            }
                        })
                        .show();
            }
        } else {
            Toast.makeText(context, "未连接网络！！", Toast.LENGTH_SHORT).show();
        }
    }

    @OnClick(R.id.img_qq_login)
    public void qqLoginClick(View v) {
        final Context context = getApplication();
        boolean isNetworkAvailable = NetworkUtils.isNetworkAvailable(context);
        if (isNetworkAvailable) {
            BindHelper.getInstance(this).bindQQ(new BindStatus(2));
        } else {
            Toast.makeText(context, "未连接网络！！", Toast.LENGTH_SHORT).show();
        }
    }

    private boolean checkInput() {
        String userName = mUserName.getText().toString();
        String password = mPassword.getText().toString();

        if (userName.length() > 0 && password.length() > 0) {
            return true;
        } else if (userName.length() <= 0) {
            Toast.makeText(getApplicationContext(), "用户名不能为空", Toast.LENGTH_SHORT).show();

        } else {
            Toast.makeText(getApplicationContext(), "密码不能为空", Toast.LENGTH_SHORT).show();
        }
        return false;
    }

    class BindStatus implements BindHelper.BindStatusListener {
        private int mType;

        public BindStatus(int type) {
            mType = type;
        }

        private String md5(String string) {
            byte[] hash;
            try {
                hash = MessageDigest.getInstance("MD5").digest(string.getBytes("UTF-8"));
            } catch (NoSuchAlgorithmException e) {
                throw new RuntimeException("Huh, MD5 should be supported?", e);
            } catch (UnsupportedEncodingException e) {
                throw new RuntimeException("Huh, UTF-8 should be supported?", e);
            }

            StringBuilder hex = new StringBuilder(hash.length * 2);
            for (byte b : hash) {
                if ((b & 0xFF) < 0x10) hex.append("0");
                hex.append(Integer.toHexString(b & 0xFF));
            }
            return hex.toString();
        }

        @Override
        public void succeed(JSONObject response) throws Exception {
            final String openId;
            String prefix = "";
            if (mType == 0) {
                prefix = "WB_";
                openId = response.optString("openid");
            } else if (mType == 1) {
                prefix = "WX_";
                openId = response.optString("openid");
            } else {
                prefix = "QQ_";
                openId = response.optJSONObject("login").optString("openid");
            }
            final String userName = prefix + openId;
            final String nickName = response.optString("nickname");
            Map<String, String> equalConditions = new HashMap<>();
            equalConditions.put("username", userName);
            Map<String, Map<String, String>> conditions = new HashMap<>();
            conditions.put("equal", equalConditions);
            BackendUtils.countForCondition(conditions, new BackendUtils.DoneCallback() {
                @Override
                public void onDone(boolean success, int code) {
                    if (success) {
                        if (code <= 0) {
                            BackendUtils.signUp(nickName, userName, "", md5(openId), new BackendUtils.DoneCallback() {
                                @Override
                                public void onDone(boolean success, int code) {
                                    if (success) {
                                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                        startActivity(intent);
                                        finish();
                                    }
                                }
                            });
                        } else {
                            BackendUtils.loginByAccount(userName, md5(openId), new BackendUtils.DoneCallback() {
                                @Override
                                public void onDone(boolean success, int code) {
                                    if (success) {
                                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                        startActivity(intent);
                                        LoginActivity.this.finish();
                                    }
                                }
                            });
                        }
                    }
                }
            });
        }

        @Override
        public void start() {
        }

        @Override
        public void failed(int errorCode, String msg) {
            if (msg == null) {
                if (errorCode == ErrorCode.RESTERROR) {
                    msg = "授权失败,重试一下呗~";
                } else if (errorCode == ErrorCode.HTTPERROR) {
                    msg = "请检查一下网络吧";
                }
            }
//            showAlertMsg(msg);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        BindHelper.getInstance(this).onActivityResult(requestCode, resultCode, data);
    }
}
