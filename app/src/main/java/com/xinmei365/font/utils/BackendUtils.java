package com.xinmei365.font.utils;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.widget.Toast;

import com.xinmei365.font.ActivityCollector;
import com.xinmei365.font.model.Feedback;
import com.xinmei365.font.model.PushMessage;
import com.xinmei365.font.model.User;
import com.xinmei365.font.ui.activity.LoginActivity;
import com.xinmei365.font.ui.activity.UserActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import cn.bmob.newim.BmobIM;
import cn.bmob.newim.bean.BmobIMConversation;
import cn.bmob.newim.bean.BmobIMMessage;
import cn.bmob.newim.bean.BmobIMUserInfo;
import cn.bmob.newim.core.BmobIMClient;
import cn.bmob.newim.listener.MessageSendListener;
import cn.bmob.v3.Bmob;
import cn.bmob.v3.BmobInstallationManager;
import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.BmobSMS;
import cn.bmob.v3.BmobUser;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.CountListener;
import cn.bmob.v3.listener.FindListener;
import cn.bmob.v3.listener.LogInListener;
import cn.bmob.v3.listener.QueryListener;
import cn.bmob.v3.listener.SaveListener;
import cn.bmob.v3.listener.UpdateListener;

public class BackendUtils {
    private static final String TAG = "BackendUtils";

    private static String sUserName;
    private static String sObjectId;

    public static void init(Context context) {
        Bmob.resetDomain("http://ztgjsdk.aafont.com.cn/8/");
        Bmob.initialize(context, Constant.BMOB_KEY);
    }

    public static User getCurrentUser() {
        return BmobUser.getCurrentUser(User.class);
    }

    public static String getUsername() {
        User user = BmobUser.getCurrentUser(User.class);
        if (user != null) {
            sUserName = user.getUsername();
        }
        return sUserName;
    }

    public static String getObjectId() {
        User user = BmobUser.getCurrentUser(User.class);
        if (user != null) {
            sObjectId = user.getObjectId();
        }
        return sObjectId;
    }

    public static void signUp(String nickName, String name, String phone, String password, final DoneCallback callback) {
        User user = new User();
        user.setNickName(nickName);
        user.setUsername(name);
        if (!TextUtils.isEmpty(phone)) {
            user.setMobilePhoneNumber(phone);
        }
        user.setPassword(password);
        user.signUp(new SaveListener<User>() {
            @Override
            public void done(User user, final BmobException e) {
                if (e == null) {
                    final String id = BmobInstallationManager.getInstallationId();
                    user.setInstallationId(id);
                    user.update(new UpdateListener() {
                        @Override
                        public void done(BmobException e2) {
                            callback.onDone(e);
                        }
                    });
                } else {
                    callback.onDone(e);
                }
            }
        });

    }

    public static boolean isLogin() {
        User user = BmobUser.getCurrentUser(User.class);
        if (user == null) {
            return false;
        } else {
            return true;
        }
    }

    public static void loginByAccount(String userName, String password, final DoneCallback callback) {
        BmobUser.loginByAccount(userName, password, new LogInListener<User>() {
            @Override
            public void done(final User user, BmobException e) {
                callback.onDone(e);
            }
        });
    }

    public static void logOut() {
        BmobUser.logOut();
    }

    public static void requestSMSCode(String phoneNumber, final DoneCallback callback) {
        BmobSMS.requestSMSCode(phoneNumber, "Register", new QueryListener<Integer>() {
            @Override
            public void done(Integer integer, BmobException e) {
                callback.onDone(e);
            }
        });
    }

    public static void resetPasswordBySMSCode(String code, String newPassword, final DoneCallback callback) {
        BmobUser.resetPasswordBySMSCode(code, newPassword, new UpdateListener() {
            @Override
            public void done(BmobException e) {
                callback.onDone(e);
            }
        });
    }

    public static void checkUserPhoneNumber(final Context context, String phoneNumber, final CountCallback callback) {
        BmobQuery<BmobUser> query = new BmobQuery<>();
        query.addWhereEqualTo("mobilePhoneNumber", phoneNumber);
        query.findObjects(new FindListener<BmobUser>() {
            @Override
            public void done(List<BmobUser> list, BmobException e) {
                if (list != null) {
                    callback.onDone(e, list.size());
                } else {
                    callback.onDone(e, 0);
                }
            }
        });

    }

    public static void updateCurrentUserPassword(String oldPassword, String newPassword, final DoneCallback callback) {
        BmobUser.updateCurrentUserPassword(oldPassword, newPassword, new UpdateListener() {
            @Override
            public void done(BmobException e) {
                callback.onDone(e);
            }
        });
    }

    public static void verifySmsCode(String phoneNumber, String smsCode, final DoneCallback callback) {
        BmobSMS.verifySmsCode(phoneNumber, smsCode, new UpdateListener() {
            @Override
            public void done(BmobException e) {
                callback.onDone(e);
            }
        });

    }

    public static void saveFeedback(String content, String contactInfo, final DoneCallback callback) {
        User user = BmobUser.getCurrentUser(User.class);
        if (user != null) {
            Feedback feedback = new Feedback();
            feedback.setContent(content);
            feedback.setContactInfo(contactInfo);
            feedback.setUser(BmobUser.getCurrentUser(User.class));
            feedback.save(new SaveListener<String>() {
                @Override
                public void done(String s, BmobException e) {
                    callback.onDone(e);
                }
            });
        } else {
            callback.onDone(new BmobException(211, new Throwable()));
        }
    }

    public static void pushMessage(final Context context, User user, final String channel, final Map<String, Object> extraMap) {
//        if (!user.getObjectId().equals(BmobUser.getCurrentUser(User.class).getObjectId())) {
//            return;
//        }
        BmobQuery<User> queryU = new BmobQuery<>();
        queryU.addWhereEqualTo("objectId" , user.getObjectId());
        queryU.findObjects(new FindListener<User>() {
            @Override
            public void done(List<User> list, BmobException e) {
                if (e == null) {
                    User user = list.get(0);
                    ArrayList<String> channels = user.getChannels();
                    if (channels == null || !channels.contains(channel)) {
                        try {
                            BmobIMUserInfo info = new BmobIMUserInfo(user.getObjectId(), user.getNickName(), user.getAvatar());
                            BmobIMConversation conversationEntrance = BmobIM.getInstance().startPrivateConversation(info, true, null);
                            BmobIMConversation messageManager = BmobIMConversation.obtain(BmobIMClient.getInstance(), conversationEntrance);
                            PushMessage msg = new PushMessage();
                            msg.setContent(channel);
                            msg.setExtraMap(extraMap);
                            messageManager.sendMessage(msg, new MessageSendListener() {
                                @Override
                                public void done(BmobIMMessage bmobIMMessage, BmobException e) {
                                }
                            });
                        } catch (IllegalArgumentException exception) {
                            MiscUtils.makeToast(context, "连接服务器异常，请稍后再试！", false);
                        }
                    }
                } else {
                    if (e.getErrorCode() == 9016) {
                        MiscUtils.makeToast(context, "网络被外星人劫持了，请稍后再试…", true);
                    } else {
                        Log.e(TAG, e.toString());
                    }
                }
            }
        });
    }

    public static void countForCondition(Map<String, Map<String, String>> conditions, final CountCallback callback) {
        BmobQuery<User> query = new BmobQuery<>();
        for (String key : conditions.keySet()) {
            if (key.equals("equal")) {
                Map<String, String> equalConditions = conditions.get(key);
                for (String equalKey : equalConditions.keySet()) {
                    query.addWhereEqualTo(equalKey, equalConditions.get(equalKey));
                }
            }
        }
        query.count(User.class, new CountListener() {
            @Override
            public void done(Integer integer, BmobException e) {
                if (integer != null) {
                    callback.onDone(e, integer);
                } else {
                    callback.onDone(e, 0);
                }
            }
        });
    }

    public static void handleException(BmobException e, final Context context) {
        if (context == null) {
            return;
        }
        if (context instanceof Activity) {
            if (((Activity) context).isDestroyed() || ((Activity) context).isDestroyed()) {
                return;
            }
        }
        if (e != null) {
            if (e.getErrorCode() == 211) {
                MiscUtils.showAlertDialog(context, "登录已经超时，请重新登录。", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        BmobIM.getInstance().disConnect();
                        BackendUtils.logOut();
                        ActivityCollector.finishAll();
                        context.startActivity(new Intent(context, LoginActivity.class));
                    }
                });
            } else if (e.getErrorCode() == 9016) {
                MiscUtils.makeToast(context, "网络被外星人劫持了，请稍后再试…", true);
            }
        }
    }

    public interface DoneCallback {
        void onDone(BmobException e);
    }

    public interface CountCallback {
        void onDone(BmobException e, int code);
    }
}
