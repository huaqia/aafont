package com.xinmei365.font.social;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;

import com.xinmei365.font.MyApplication;
import com.tencent.connect.UserInfo;
import com.tencent.connect.common.Constants;
import com.tencent.connect.share.QQShare;
import com.tencent.connect.share.QzoneShare;
import com.tencent.tauth.IUiListener;
import com.tencent.tauth.Tencent;
import com.tencent.tauth.UiError;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by xinmei024 on 15/9/22.
 *
 * @author ningso
 * @Email ningso.ping@gmail.com
 */

public final class QQSDKShare {

    private final static String QQ_KEY = "100736522";

    private QQStatusListener mQQStatusListener;
    private IUiListener mLoginListener;

    private static int shareType = QQShare.SHARE_TO_QQ_TYPE_DEFAULT;
    // QZone分享， SHARE_TO_QQ_TYPE_DEFAULT 图文，SHARE_TO_QQ_TYPE_IMAGE 纯图
    // QzoneShare.SHARE_TO_QZONE_TYPE_IMAGE_TEXT;

    private static String mTitle = null;
    private static String mTargetUrl = null;
    private static String mSummary = null;
    private static String mImageUrl = "http://upaicdn.xinmei365.com/newwfs/font_ad/90.png";
    private static int mExtarFlag = 0x00;
    private static boolean mQZONEFlag = true;

    private static QQSDKType mType = QQSDKType.SHARE_TO_QQ;

    public enum QQSDKType {
        SHARE_TO_QQ, SHARE_TO_QZONE
    }

    private static volatile QQSDKShare INSTANCE;
    private static volatile Tencent mQQTencent;

    public static QQSDKShare getInstance() {
        if (INSTANCE == null) {
            synchronized (QQSDKShare.class) {
                if (INSTANCE == null) {
                    INSTANCE = new QQSDKShare();
                    INSTANCE.initTencent();

                }
            }
        }
        return INSTANCE;
    }

    private void initTencent() {
        mQQTencent = Tencent.createInstance(QQ_KEY, MyApplication.getInstance());
    }

    public Tencent getTencent() {
        if (mQQTencent == null) {
            mQQTencent = Tencent.createInstance(QQ_KEY, MyApplication.getInstance());
        }
        return mQQTencent;
    }

    public static class Builder {

        public com.tencent.tauth.IUiListener IUiListener;

        public Builder setQQSDKType(QQSDKType type) {
            mType = type;
            return this;
        }

        public Builder setShareType(int type) {
            shareType = type;
            return this;
        }

        public Builder setTitle(String title) {
            mTitle = title;
            return this;
        }

        public Builder setTargetUrl(String targetUrl) {
            mTargetUrl = targetUrl;
            return this;
        }

        public Builder setSummary(String summary) {
            mSummary = summary;
            return this;
        }

        public Builder setQZONEFlag(boolean qzoneflag) {
            mQZONEFlag = qzoneflag;
            return this;
        }

        public Builder setImageUrl(String imageUrl) {
            if (!TextUtils.isEmpty(imageUrl)) {
                mImageUrl = imageUrl;
            } else {
                mImageUrl = "http://upaicdn.xinmei365.com/newwfs/font_ad/90.png";
            }
            return this;
        }

        public Builder setIUiListener(IUiListener IUiListener) {
            this.IUiListener = IUiListener;
            return this;
        }

        public QQSDKShare share(Activity activity) {
            QQSDKShare qqShare = new QQSDKShare();
            qqShare.share(activity, IUiListener);
            return qqShare;
        }
    }

    private void share(Activity activity, IUiListener listener) {
        // 是否弹窗
        if (mQZONEFlag) {
            // 倒数第二位置为1, 其他位不变
            mExtarFlag |= QQShare.SHARE_TO_QQ_FLAG_QZONE_ITEM_HIDE;
        } else {
            // 倒数第二位置为0, 其他位不变
            mExtarFlag &= (0xFFFFFFFF - QQShare.SHARE_TO_QQ_FLAG_QZONE_ITEM_HIDE);
        }
        if (mType == QQSDKType.SHARE_TO_QQ) {
            if (mTitle == null || mSummary == null) {
                return;
            }
            // 分享到QQ
            shareToQQ(activity, mTitle, mTargetUrl, mSummary, mImageUrl, listener);
        } else if (mType == QQSDKType.SHARE_TO_QZONE) {
            if (mTitle == null || mSummary == null) {
                return;
            }
            // 分享到QQ空间
            shareToQZONE(activity, mTitle, mTargetUrl, mSummary, mImageUrl, listener);

        }
    }

    public void qqLogin(Activity activity, QQStatusListener listener) {

        mQQStatusListener = listener;

        mQQStatusListener.start();

        final Tencent tencent = getInstance().getTencent();

        if (tencent.isSessionValid()) {
            qqLogout();
        }

        if (mLoginListener == null) {

            mLoginListener = new IUiListener() {

                @Override
                public void onError(UiError arg0) {
                    mQQStatusListener.faild();
                }

                @Override
                public void onComplete(final Object loginResponse) {
                    // TODO: 16/1/13 这部分服务端也可以处理,其实拿到的东西是一样的
                    JSONObject jsonObject = (JSONObject) loginResponse;
                    try {
                        int ret = jsonObject.getInt("ret");
                        if (ret == 0) {
                            String openID = jsonObject.optString("openid");
                            String accessToken = jsonObject.optString("access_token");
                            String expires = jsonObject.optString("expires_in");
                            tencent.setOpenId(openID);
                            tencent.setAccessToken(accessToken, expires);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    UserInfo mInfo = new UserInfo(MyApplication.getInstance(), tencent.getQQToken());
                    mInfo.getUserInfo(new IUiListener() {

                        @Override
                        public void onError(UiError arg0) {
                            mQQStatusListener.faild();
                        }

                        @Override
                        public void onComplete(Object userResponse) {
                            JSONObject user = (JSONObject) userResponse;
                            try {
                                user.put("login", loginResponse);
                                mQQStatusListener.login(user);
                            } catch (JSONException e) {
                                e.printStackTrace();
                                mQQStatusListener.faild();
                            }
                        }

                        @Override
                        public void onCancel() {
                            mQQStatusListener.faild();
                        }
                    });

                }

                @Override
                public void onCancel() {
                    mQQStatusListener.faild();
                }
            };
        }
        // 应用需要获得哪些API的权限，由“，”分隔。例如：SCOPE = “get_user_info,add_t”；所有权限用“all
        try {
            tencent.login(activity, "all", mLoginListener);
        } catch (Exception e) {
            mQQStatusListener.faild();
        }
    }


    public void onActivityResultData(int requestCode, int resultCode, Intent data) {
        if (requestCode == Constants.REQUEST_LOGIN) {
            Tencent.onActivityResultData(requestCode, resultCode, data, mLoginListener);
        }
    }

    public boolean qqLogout() {
        getTencent().logout(MyApplication.getInstance());
        return false;
    }

    public interface QQStatusListener {
        void start();

        void login(JSONObject response);

        void faild();
    }

    private void shareToQQ(final Activity activity, String title, String targetUrl, String summary, String imageUrl, final IUiListener listener) {
        final QQShare mQQShare = new QQShare(activity, getTencent().getQQToken());

        final Bundle params = new Bundle();
        if (shareType != QQShare.SHARE_TO_QQ_TYPE_IMAGE) {
            params.putString(QQShare.SHARE_TO_QQ_TITLE, title);
            if (targetUrl != null) {
                params.putString(QQShare.SHARE_TO_QQ_TARGET_URL, targetUrl);
            }
            params.putString(QQShare.SHARE_TO_QQ_SUMMARY, summary);
        }
        if (!TextUtils.isEmpty(imageUrl)) {
            params.putString(
                    shareType == QQShare.SHARE_TO_QQ_TYPE_IMAGE ? QQShare.SHARE_TO_QQ_IMAGE_LOCAL_URL
                            : QQShare.SHARE_TO_QQ_IMAGE_URL, imageUrl);
        }
        params.putString(QQShare.SHARE_TO_QQ_APP_NAME, "字体管家");
        params.putInt(QQShare.SHARE_TO_QQ_KEY_TYPE, shareType);
        params.putInt(QQShare.SHARE_TO_QQ_EXT_INT, mExtarFlag);
        new Thread(new Runnable() {

            @Override
            public void run() {
                mQQShare.shareToQQ(activity, params, listener);
            }
        }).start();
    }

    private void shareToQZONE(final Activity activity, String title, String targetUrl, String summary, String imageUrl, final IUiListener listener) {

        final QzoneShare mQzoneShare = new QzoneShare(activity, getTencent().getQQToken());
        final Bundle params = new Bundle();
        params.putInt(QzoneShare.SHARE_TO_QZONE_KEY_TYPE, shareType);
        params.putString(QzoneShare.SHARE_TO_QQ_TITLE, title);
        params.putString(QzoneShare.SHARE_TO_QQ_SUMMARY, summary);
        if (shareType != QzoneShare.SHARE_TO_QZONE_TYPE_APP) {
            // app分享不支持传目标链接
            params.putString(QzoneShare.SHARE_TO_QQ_TARGET_URL, targetUrl);
        }
        // 支持传多个imageUrl
        // String imageUrl = "XXX";
        if (!TextUtils.isEmpty(imageUrl)) {
            params.putString(QzoneShare.SHARE_TO_QQ_IMAGE_URL, imageUrl);
            ArrayList<String> urls = new ArrayList<>();
            urls.add(imageUrl);
            params.putStringArrayList(QzoneShare.SHARE_TO_QQ_IMAGE_URL, urls);
        }
        new Thread(new Runnable() {

            @Override
            public void run() {
                mQzoneShare.shareToQzone(activity, params, listener);
            }
        }).start();
    }
}
