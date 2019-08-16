package com.hanmei.aafont.utils;

import android.content.Context;
import android.os.Message;

import com.hanmei.aafont.model.Feedback;
import com.hanmei.aafont.model.Installation;
import com.hanmei.aafont.model.User;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import cn.bmob.push.BmobPush;
import cn.bmob.v3.Bmob;
import cn.bmob.v3.BmobInstallation;
import cn.bmob.v3.BmobInstallationManager;
import cn.bmob.v3.BmobPushManager;
import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.BmobSMS;
import cn.bmob.v3.BmobUser;
import cn.bmob.v3.InstallationListener;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.CountListener;
import cn.bmob.v3.listener.FindListener;
import cn.bmob.v3.listener.LogInListener;
import cn.bmob.v3.listener.PushListener;
import cn.bmob.v3.listener.QueryListener;
import cn.bmob.v3.listener.SaveListener;
import cn.bmob.v3.listener.UpdateListener;
import io.reactivex.functions.Consumer;

public class BackendUtils {
    public static void init(Context context) {
        Bmob.initialize(context, Constant.BMOB_KEY);
        BmobInstallationManager.getInstance().initialize(new InstallationListener<BmobInstallation>() {
            @Override
            public void done(BmobInstallation bmobInstallation, BmobException e) {
                if (e == null) {
                } else {
                }
            }
        });
        BmobPush.startWork(context);
    }
    public static User getCurrentUser() {
        return BmobUser.getCurrentUser(User.class);
    }

    public static String getUsername() {
        return BmobUser.getCurrentUser(User.class).getUsername();
    }

    public static void signUp(String name, String phone, String password, final DoneCallback callback) {
        User user = new User();
        user.setUsername(name);
        user.setMobilePhoneNumber(phone);
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
        if(user == null){
            return false;
        }else{
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

    public static void checkUserPhoneNumber(String phoneNumber, final DoneCallback callback) {
        BmobQuery<BmobUser> query=new BmobQuery<>();
        query.addWhereEqualTo("mobilePhoneNumber", phoneNumber);
        query.findObjects(new FindListener<BmobUser>() {
            @Override
            public void done(List<BmobUser> list, BmobException e) {
                callback.onDone(e == null, list.size());
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

    public static void saveFeedback(String content, final DoneCallback callback) {
        Feedback feedback = new Feedback();
        feedback.setContent(content);
        feedback.setUser(BmobUser.getCurrentUser(User.class));
        feedback.save(new SaveListener<String>() {
            @Override
            public void done(String s, BmobException e) {
                callback.onDone(e == null, 0);
            }
        });
    }

    public static void subscribe(String channel, final DoneCallback callback) {
        BmobInstallationManager.getInstance().subscribe(channel, new InstallationListener<BmobInstallation>() {
            @Override
            public void done(BmobInstallation bmobInstallation, BmobException e) {
                callback.onDone(e == null, 0);
            }
        });
    }

    public static void subscribe(List<String> channels, final DoneCallback callback) {
        BmobInstallationManager.getInstance().subscribe(channels, new InstallationListener<BmobInstallation>() {
            @Override
            public void done(BmobInstallation bmobInstallation, BmobException e) {
                callback.onDone(e == null, 0);
            }
        });
    }

    public static void unsubscribe(String channel, final DoneCallback callback) {
        BmobInstallationManager.getInstance().unsubscribe(channel, new InstallationListener<BmobInstallation>() {
            @Override
            public void done(BmobInstallation bmobInstallation, BmobException e) {
                callback.onDone(e == null, 0);
            }
        });
    }

    public static void unsubscribe(List<String> channels, final DoneCallback callback) {
        BmobInstallationManager.getInstance().unsubscribe(channels, new InstallationListener<BmobInstallation>() {
            @Override
            public void done(BmobInstallation bmobInstallation, BmobException e) {
                callback.onDone(e == null, 0);
            }
        });
    }

    public static void pushMessage(User user, String channel, String content) {
        BmobPushManager bmobPushManager = new BmobPushManager();
        BmobQuery<BmobInstallation> query = BmobInstallation.getQuery();
        List<String> channels = new ArrayList<>();
        channels.add(channel);
        query.addWhereContainedIn("channels", channels);
        query.addWhereEqualTo("installationId", user.getInstallationId());
        bmobPushManager.setQuery(query);
        bmobPushManager.pushMessage(content, new PushListener() {
            @Override
            public void done(BmobException e) {
                if (e==null){
                }else {
                }
            }
        });
    }

    public static int dip2px(Context context,float dpValue)
    {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int)(dpValue * scale +0.5f);
    }

    public static int px2dip(Context context,float pxValue)
    {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int)(pxValue / scale + 0.5f);
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

    public interface DoneCallback {
        void onDone(boolean success, int code);
    }
}
