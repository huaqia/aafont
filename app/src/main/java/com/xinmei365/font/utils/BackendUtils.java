package com.xinmei365.font.utils;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.widget.Toast;

import com.xinmei365.font.ActivityCollector;
import com.xinmei365.font.model.Feedback;
import com.xinmei365.font.model.PushMessage;
import com.xinmei365.font.model.User;
import com.xinmei365.font.ui.activity.LoginActivity;

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
    public static void init(Context context) {
        Bmob.resetDomain("http://ztgjsdk.aafont.com.cn/8/");
        Bmob.initialize(context, Constant.BMOB_KEY);
    }

    public static User getCurrentUser() {
        return BmobUser.getCurrentUser(User.class);
    }

    public static String getUsername() {
        return BmobUser.getCurrentUser(User.class).getUsername();
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
            public void done(User user, BmobException e) {
                if (e == null) {
                    final String id = BmobInstallationManager.getInstallationId();
                    user.setInstallationId(id);
                    user.update(new UpdateListener() {
                        @Override
                        public void done(BmobException e) {
                            callback.onDone(true, 0);
                        }
                    });
                } else {
                    if (e.getErrorCode() == 202) {
                        callback.onDone(false, 0);
                    }
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
                callback.onDone(e == null, 0);
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
                callback.onDone(e == null, 0);
            }
        });
    }

    public static void resetPasswordBySMSCode(String code, String newPassword, final DoneCallback callback) {
        BmobUser.resetPasswordBySMSCode(code, newPassword, new UpdateListener() {
            @Override
            public void done(BmobException e) {
                callback.onDone(e == null, 0);
            }
        });
    }

    public static void checkUserPhoneNumber(final Context context, String phoneNumber, final DoneCallback callback) {
        BmobQuery<BmobUser> query = new BmobQuery<>();
        query.addWhereEqualTo("mobilePhoneNumber", phoneNumber);
        query.findObjects(new FindListener<BmobUser>() {
            @Override
            public void done(List<BmobUser> list, BmobException e) {
                if (e == null) {
                    callback.onDone(e == null, list.size());
                } else {
                    if (e.getErrorCode() == 9016) {
                        Toast toast = Toast.makeText(context, "网络被外星人劫持了，请稍后再试…",Toast.LENGTH_SHORT);
                        toast.setGravity(Gravity.CENTER, 0, 0);
                        toast.show();
                    } else {
                        Log.e(TAG, e.toString());
                    }
                }
            }
        });

    }

    public static void updateCurrentUserPassword(String oldPassword, String newPassword, final DoneCallback callback) {
        BmobUser.updateCurrentUserPassword(oldPassword, newPassword, new UpdateListener() {
            @Override
            public void done(BmobException e) {
                if (e == null) {
                    callback.onDone(true, 0);
                } else {
                    if (e.getErrorCode() == 210) {
                        callback.onDone(false, 0);
                    }
                }
            }
        });
    }

    public static void verifySmsCode(String phoneNumber, String smsCode, final DoneCallback callback) {
        BmobSMS.verifySmsCode(phoneNumber, smsCode, new UpdateListener() {
            @Override
            public void done(BmobException e) {
                callback.onDone(e == null, 0);
            }
        });

    }

    public static void saveFeedback(String content, String contactInfo, final DoneCallback callback) {
        Feedback feedback = new Feedback();
        feedback.setContent(content);
        feedback.setContactInfo(contactInfo);
        feedback.setUser(BmobUser.getCurrentUser(User.class));
        feedback.save(new SaveListener<String>() {
            @Override
            public void done(String s, BmobException e) {
                callback.onDone(e == null, 0);
            }
        });
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
                    }
                } else {
                    if (e.getErrorCode() == 9016) {
                        Toast toast = Toast.makeText(context, "网络被外星人劫持了，请稍后再试…",Toast.LENGTH_SHORT);
                        toast.setGravity(Gravity.CENTER, 0, 0);
                        toast.show();
                    } else {
                        Log.e(TAG, e.toString());
                    }
                }
            }
        });
    }

    public static void countForCondition(Map<String, Map<String, String>> conditions, final DoneCallback callback) {
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
                callback.onDone(e == null, integer);
            }
        });
    }

    public static void handleException(BmobException e, final Context context) {
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
                Toast toast = Toast.makeText(context, "网络被外星人劫持了，请稍后再试…",Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.CENTER, 0, 0);
                toast.show();
            }
        }
    }

    public interface DoneCallback {
        void onDone(boolean success, int code);
    }
}
