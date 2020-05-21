package com.xinmei365.font.social;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

import com.xinmei365.font.MyApplication;
import com.xinmei365.font.utils.Constant;
import com.xinmei365.font.utils.ErrorCode;
import com.sina.weibo.sdk.auth.AuthInfo;
import com.sina.weibo.sdk.auth.Oauth2AccessToken;
import com.sina.weibo.sdk.auth.WeiboAuthListener;
import com.sina.weibo.sdk.auth.sso.SsoHandler;
import com.sina.weibo.sdk.exception.WeiboException;
import com.sina.weibo.sdk.net.RequestListener;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;

import okhttp3.Call;


public class BindHelper {

    private BindStatusListener mBindStatusListener;
    private static WeakReference<Activity> mActivity;
    private static volatile BindHelper INSTANCE;

    public static final String BINDQQ = "bindQQ";
    public static final String BINDWX = "bindWeixin";
    public static final String BINDWEIBO = "bindWeibo";
    public static final String BINDPHONE = "bindPhone";
    public static final String BINDDEVICE = "bindDevice";
    public static final String BINDICON = "icon";

    public static BindHelper getInstance(final Activity _activity) {
        if (INSTANCE == null) {
            synchronized (BindHelper.class) {
                if (INSTANCE == null) {
                    INSTANCE = new BindHelper();
                }
            }
        }
        mActivity = new WeakReference<>(_activity);
        return INSTANCE;
    }


    public void bindQQ(BindStatusListener _listener) {

        Log.d("sharesdk qq", "qq has start bind");
        mBindStatusListener = null;
        mBindStatusListener = _listener;

        QQSDKShare.getInstance().qqLogin(mActivity.get(), new QQSDKShare.QQStatusListener() {

            @Override
            public void start() {
                if (mBindStatusListener != null) {
                    mBindStatusListener.start();
                }
            }

            @Override
            public void login(JSONObject response) {
                try {
                    if (mBindStatusListener != null) {
                        try {
                            mBindStatusListener.succeed(response);
                        } catch (Exception e) {
                            if (mBindStatusListener != null) {
                                mBindStatusListener.failed(ErrorCode.RESTERROR, null);
                            }
                            e.printStackTrace();
                        }
                        String iconString = response.optString("figureurl_2");
                        if (iconString == null) {
                            response.getString("figureurl_qq_2");
                        }
                        response.put(BindHelper.BINDICON, iconString);
                    }
                } catch (Exception ignored) {

                }
            }

            @Override
            public void faild() {
                if (mBindStatusListener != null) {
                    mBindStatusListener.failed(ErrorCode.RESTERROR, null);
                }
            }
        });
    }

    // 绑定过程若需增加自己等待,可以mBindStatusLisener.start();通知UI


    private SsoHandler mSsoHandler = null;

    public void bindWeibo(BindStatusListener _listener) {

        Log.d("sharesdk sinaweibo", "sinaweibo has start bind ");
        mBindStatusListener = null;
        mBindStatusListener = _listener;

        if (mBindStatusListener != null) {
            mBindStatusListener.start();
        }

        // 创建授权认证信息
        AuthInfo mAuthInfo = new AuthInfo(MyApplication.getInstance(), Constant.WEIBO_APP_KEY, Constant.WEIBO_REDIRECT_URL, Constant.WEIBO_SCOPE);
        mSsoHandler = new SsoHandler(mActivity.get(), mAuthInfo);
        mSsoHandler.authorize(new WeiboAuthListener() {
            @Override
            public void onComplete(final Bundle bundle) {
                Oauth2AccessToken accessToken = Oauth2AccessToken.parseAccessToken(bundle);
                if (accessToken != null && accessToken.isSessionValid()) {
                    AccessTokenKeeper.writeSinaAccessToken(MyApplication.getInstance(), accessToken);

                    // 获取用户信息接口
                    UsersAPI mUsersAPI = new UsersAPI(MyApplication.getInstance(), Constant.WEIBO_APP_KEY, accessToken);
                    long uid = Long.parseLong(accessToken.getUid());
                    mUsersAPI.show(uid, new RequestListener() {
                        @Override
                        public void onComplete(String response) {
                            if (!TextUtils.isEmpty(response)) {
                                // 调用 User#parse 将JSON串解析成User对象
//                                User user = User.parse(response);
//                                if (user != null) {
//                                    //
//                                }
                            }
                        }

                        @Override
                        public void onWeiboException(WeiboException e) {
                        }
                    });
                    //关注一个用户
//                    FriendshipsAPI friendshipsAPI = new FriendshipsAPI(FontApp.getInstance(), GlobalConfig.WEIBO_APP_KEY, accessToken);
//                    friendshipsAPI.create(Long.parseLong("3175041753"), null, null);

                    if (mBindStatusListener != null) {
                        try {
                            JSONObject obj = new JSONObject();
                            obj.put("nickname", bundle.getString("userName"));
                            obj.put("openid", accessToken.getUid());
                            mBindStatusListener.succeed(obj);
                        } catch (Exception e) {
                            if (mBindStatusListener != null) {
                                mBindStatusListener.failed(ErrorCode.RESTERROR, "授权失败,请重试");
                            }
                        }
                    }
                } else {
                    if (mBindStatusListener != null) {
                        mBindStatusListener.failed(ErrorCode.RESTERROR, "授权失败");
                    }
                }

            }

            @Override
            public void onWeiboException(WeiboException e) {
                if (mBindStatusListener != null) {
                    mBindStatusListener.failed(ErrorCode.RESTERROR, e.getMessage());
                }
            }

            @Override
            public void onCancel() {
                if (mBindStatusListener != null) {
                    mBindStatusListener.failed(ErrorCode.RESTERROR, "用户取消了授权");
                }
            }
        });
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        ssoAuthorizeCallBack(requestCode, resultCode, data);
        QQSDKShare.getInstance().onActivityResultData(requestCode, resultCode, data);
    }

    public void ssoAuthorizeCallBack(int requestCode, int resultCode, Intent data) {
        if (mSsoHandler != null) {
            mSsoHandler.authorizeCallBack(requestCode, resultCode, data);
        }
    }

    public void bindWeiXin(BindStatusListener _listener) {

        mBindStatusListener = null;
        mBindStatusListener = _listener;
        if (mBindStatusListener != null) {
            mBindStatusListener.start();
        }
        try {
            WechatShare.Builder builder = new WechatShare.Builder();
            builder.login();
        } catch (Exception e) {
            if (mBindStatusListener != null) {
                mBindStatusListener.failed(ErrorCode.RESTERROR, null);
            }
        }
    }

    public void weiXinFinished(int errorCode, final String code) {
        if (errorCode == 0) {
            Uri.Builder builder = Uri.parse("https://api.weixin.qq.com/sns/oauth2/access_token").buildUpon();
            builder.appendQueryParameter("appid", Constant.WEIXIN_APP_ID);
            builder.appendQueryParameter("secret", Constant.WEIXIN_APP_SECRET);
            builder.appendQueryParameter("code", code);
            builder.appendQueryParameter("grant_type", "authorization_code");
            //https://api.weixin.qq.com/sns/oauth2/access_token?appid=APPID&secret=SECRET&code=CODE&grant_type=authorization_code
            OkHttpUtils.get()
                    .url(builder.toString())
                    .build()
                    .execute(new StringCallback() {
                        @Override
                        public void onError(Call request, Exception e,int id) {
                            if (mBindStatusListener != null) {
                                mBindStatusListener.failed(ErrorCode.RESTERROR, null);
                            }
                        }

                        @Override
                        public void onResponse(String response,int id) {
                            try {
                                final JSONObject loginResponse = new JSONObject(response);
                                if (loginResponse.has("access_token")) {
                                    Uri.Builder builder = Uri.parse("https://api.weixin.qq.com/sns/userinfo").buildUpon();
                                    builder.appendQueryParameter("access_token", loginResponse.optString("access_token"));
                                    builder.appendQueryParameter("openid", loginResponse.optString("openid"));
                                    OkHttpUtils.get()
                                            .url(builder.toString())
                                            .build()
                                            .execute(new StringCallback() {
                                                @Override
                                                public void onError(Call request, Exception e,int id) {
                                                    if (mBindStatusListener != null) {
                                                        mBindStatusListener.failed(ErrorCode.RESTERROR, null);
                                                    }
                                                }

                                                @Override
                                                public void onResponse(String response,int id) {
                                                    if (mBindStatusListener != null) {
                                                        try {
                                                            JSONObject responseJson = new JSONObject(response);
                                                            responseJson.put("login", loginResponse);
                                                            mBindStatusListener.succeed(responseJson);
                                                        } catch (Exception e) {
                                                            e.printStackTrace();
                                                            mBindStatusListener.failed(ErrorCode.RESTERROR, null);
                                                        }
                                                    }
                                                }
                                            });
                                } else if (mBindStatusListener != null) {
                                    mBindStatusListener.failed(ErrorCode.RESTERROR, null);
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                                if (mBindStatusListener != null) {
                                    mBindStatusListener.failed(ErrorCode.RESTERROR, null);
                                }
                            }
                        }
                    });
        } else {
            if (mBindStatusListener != null) {
                String errorMsg = null;
                if (errorCode == -2) {
                    errorMsg = "用户取消了授权";
                }
                if (errorCode == -4) {
                    errorMsg = "用户拒绝授权";
                }
                mBindStatusListener.failed(ErrorCode.RESTERROR, errorMsg);
            }
        }

    }

    public static String getSocialByType(int type) {
        String social = null;
        switch (type) {
            case 1:
                social = BindHelper.BINDQQ;
                break;
            case 2:
                social = BindHelper.BINDWEIBO;
                break;
            case 4:
                // 手机登录
                social = BindHelper.BINDPHONE;
                break;
            case 5:
                // 一键登录
                social = BindHelper.BINDDEVICE;
                break;
            case 6:
                // 一键登录
                social = BindHelper.BINDWX;
                break;
        }
        return social;
    }

    // 认证通过后,执行report操作 除微信
    private void authSuccess(final JSONObject raw) {
        String typeStr = raw.optString("social");
        String token = raw.optString("token");
        String openid = raw.optString("openid");
        String expires_in = raw.optString("expires_in");

        final int type = Integer.parseInt(typeStr);

//        OkHttpUtils.post()
//                .url(UrlConstants.getHostAddress() + UrlConstants.PERSONACENTER_LOGIN)
//                .addParams("access_token", token)
//                .addParams("open_id", openid)
//                .addParams("expires_in", expires_in)
//                .addParams("user_type", String.valueOf(type))
//                .addParams("device_id", DataCenter.get().getAppInfo().getDeviceId())
//                .build()
//                .execute(new StringCallback() {
//                             @Override
//                             public void onError(Call request, Exception e,int id) {
//                                 if (mBindStatusListener != null) {
//                                     mBindStatusListener.failed(ErrorCode.HTTPERROR, null);
//                                 }
//                             }
//
//                             @Override
//                             public void onResponse(String response,int id) {
//                                 if (mBindStatusListener != null) {
//                                     try {
//                                         JSONObject responseObject = new JSONObject(response);
//                                         if (responseObject.optInt("errorCode", -1) == 0) {
//                                             mBindStatusListener.succeed(responseObject.getJSONObject("data"));
//                                         } else {
//                                             mBindStatusListener.failed(ErrorCode.RESTERROR, responseObject.optString("errorMsg"));
//                                         }
//                                     } catch (Exception e) {
//                                         e.printStackTrace();
//                                         mBindStatusListener.failed(ErrorCode.RESTERROR, null);
//                                     }
//                                 }
//                             }
//                         }
//
//                );

    }


    public void onDestroy() {
        mBindStatusListener = null;
    }

    public interface BindStatusListener {

        void start();

        void succeed(JSONObject response) throws Exception;

        void failed(int errorCode, String msg);
    }

//    public static void clearAllStatus() {
//        QQSDKShare.getInstance().qqLogout();
//        AccessTokenKeeper.clear(FontApp.getInstance());
//    }
}
