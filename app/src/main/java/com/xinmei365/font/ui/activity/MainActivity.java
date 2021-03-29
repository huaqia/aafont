package com.xinmei365.font.ui.activity;

import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.os.Bundle;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.AppCompatTextView;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import butterknife.BindView;
import butterknife.OnClick;
import cn.bmob.newim.BmobIM;
import cn.bmob.newim.bean.BmobIMUserInfo;
import cn.bmob.newim.core.ConnectionStatus;
import cn.bmob.newim.event.MessageEvent;
import cn.bmob.newim.event.OfflineMessageEvent;
import cn.bmob.newim.listener.ConnectListener;
import cn.bmob.v3.BmobUser;
import cn.bmob.v3.exception.BmobException;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.xinmei365.font.R;
import com.xinmei365.font.model.DraftData;
import com.xinmei365.font.model.RefreshEvent;
import com.xinmei365.font.model.User;
import com.xinmei365.font.ui.fragment.ForumFragment;
import com.xinmei365.font.ui.fragment.HomeFragment;
import com.xinmei365.font.ui.fragment.MeFragment;
import com.xinmei365.font.ui.fragment.MessageFragment;
import com.xinmei365.font.utils.Constant;
import com.xinmei365.font.utils.FileUtils;
import com.xinmei365.font.utils.MiscUtils;
import com.xinmei365.font.utils.PermissionUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;

public class MainActivity extends BaseActivity {
    @BindView(R.id.text_home)
    AppCompatTextView mHomeText;
    @BindView(R.id.icon_home)
    AppCompatImageView mHomeImg;
    @BindView(R.id.text_forum)
    AppCompatTextView mForumText;
    @BindView(R.id.icon_forum)
    AppCompatImageView mForumImg;
    @BindView(R.id.text_message)
    AppCompatTextView mMessageText;
    @BindView(R.id.icon_message)
    AppCompatImageView mMessageImg;
    @BindView(R.id.text_me)
    AppCompatTextView mMeText;
    @BindView(R.id.icon_me)
    AppCompatImageView mMeImg;
    private Dialog mDialog;
    private Fragment mFragmentContent;
    private HomeFragment mHomeFragment;
    private ForumFragment mForumFragment;
    private MessageFragment mMessageFragment;
    private MeFragment mMeFragment;

    private Boolean mNeedRequestPermissions;
    private Dialog mCreateDialog;

    private int mTabNormalColor;
    private int mTabSelectedColor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mNeedRequestPermissions = true;
        mTabNormalColor = getResources().getColor(R.color.main_tab_normal_color);
        mTabSelectedColor = getResources().getColor(R.color.main_tab_selected_color);
        setDefaultFragment();
        final User user = BmobUser.getCurrentUser(User.class);
        if (user != null && !TextUtils.isEmpty(user.getObjectId()) &&
                BmobIM.getInstance().getCurrentStatus().getCode() != ConnectionStatus.CONNECTED.getCode()) {
            BmobIM.connect(user.getObjectId(), new ConnectListener() {
                @Override
                public void done(String uid, BmobException e) {
                    if (e == null) {
                        BmobIM.getInstance().updateUserInfo(new BmobIMUserInfo(user.getObjectId(),
                                        user.getNickName(), user.getAvatar()));
                        EventBus.getDefault().post(new RefreshEvent());
                    } else {
                        MiscUtils.makeToast(MainActivity.this, e.getMessage(), false);
                    }
                }
            });

        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        final Set<String> permissions = new HashSet<>();
        if (mNeedRequestPermissions) {
            boolean shouldShowRequestPermissionRationale = false;
            for (String p : Constant.PERMISSIONS_SHOULD_GRANT) {
                if (!PermissionUtils.isPermissionGranted(getApplicationContext(), p)) {
                    permissions.add(p);
                    if (!shouldShowRequestPermissionRationale && ActivityCompat.shouldShowRequestPermissionRationale(this, p)) {
                        shouldShowRequestPermissionRationale = true;
                    }
                }
            }
            if (permissions.size() > 0) {
                if (shouldShowRequestPermissionRationale) {
                    MaterialDialog dialog = new MaterialDialog.Builder(this)
                            .title(R.string.permission_notice)
                            .positiveText("ok")
                            .onPositive(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                    requestPermissions(permissions);
                                }
                            })
                            .negativeText("cancel")
                            .onNegative(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                    dialog.dismiss();
                                    mNeedRequestPermissions = false;
                                }
                            })
                            .build();
                    showDialog(dialog);
                } else {
                    requestPermissions(permissions);
                }
            } else {
                mNeedRequestPermissions = false;
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions
            , @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (permissions.length == 0) {
            return;
        }

        Boolean needRequestPermissions = false;
        if (requestCode == PermissionUtils.REQUEST_CODE_PERMISSIONS_ALL) {
            for (int i = 0; i < grantResults.length; i++) {
                if (grantResults[i] == PackageManager.PERMISSION_DENIED &&
                        ActivityCompat.shouldShowRequestPermissionRationale(this, permissions[i])) {
                    needRequestPermissions = true;
                    break;
                }
            }
        }
        mNeedRequestPermissions = needRequestPermissions;
    }

    @Override
    protected void setMyContentView() {
        setContentView(R.layout.activity_main);
    }

    @Subscribe
    public void onEventMainThread(RefreshEvent event) {
        // todo red point
    }

    @Subscribe
    public void onEventMainThread(OfflineMessageEvent event) {
        // todo red point
    }

    @Subscribe
    public void onEventMainThread(MessageEvent event) {
        // todo red point
    }

    @OnClick({R.id.main_home, R.id.main_forum, R.id.main_message, R.id.main_me})
    public void tableClick(View view) {
        switch (view.getId()) {
            case R.id.main_home:
                resetComponentState();
                mHomeText.setTextColor(mTabSelectedColor);
                mHomeImg.setImageResource(R.drawable.ic_tab_home_selected);
                if (mHomeFragment == null) {
                    mHomeFragment = new HomeFragment();
                }
                switchFragment(mHomeFragment);
                break;
            case R.id.main_forum:
                resetComponentState();
                mForumText.setTextColor(mTabSelectedColor);
                mForumImg.setImageResource(R.drawable.ic_tab_forum_selected);
                if (mForumFragment == null) {
                    mForumFragment = new ForumFragment();
                }
                switchFragment(mForumFragment);
//                BackendUtils.pushMessage(BackendUtils.getCurrentUser(), "FOLLOW", "消息内容");
                break;
            case R.id.main_message:
                resetComponentState();
                mMessageText.setTextColor(mTabSelectedColor);
                mMessageImg.setImageResource(R.drawable.ic_tab_message_selected);
                if (mMessageFragment == null) {
                    mMessageFragment = new MessageFragment();
                }
                switchFragment(mMessageFragment);
                break;
            case R.id.main_me:
                resetComponentState();
                mMeText.setTextColor(mTabSelectedColor);
                mMeImg.setImageResource(R.drawable.ic_tab_me_selected);
                if (mMeFragment == null) {
                    mMeFragment = new MeFragment();
                }
                switchFragment(mMeFragment);
                break;
            default:
                break;
        }
    }

    private void resetComponentState() {
        mHomeText.setTextColor(mTabNormalColor);
        mHomeImg.setImageResource(R.drawable.ic_tab_home);
        mForumText.setTextColor(mTabNormalColor);
        mForumImg.setImageResource(R.drawable.ic_tab_forum);
        mMessageText.setTextColor(mTabNormalColor);
        mMessageImg.setImageResource(R.drawable.ic_tab_message);
        mMeText.setTextColor(mTabNormalColor);
        mMeImg.setImageResource(R.drawable.ic_tab_me);
    }

    private void switchFragment(Fragment fragment) {
        if (mFragmentContent != fragment) {
            FragmentManager fm = getSupportFragmentManager();
            FragmentTransaction transaction = fm.beginTransaction();
            if (!fragment.isAdded()) {
                transaction.hide(mFragmentContent).add(R.id.main_frame, fragment).commit();
            } else {
                transaction.hide(mFragmentContent).show(fragment).commit();
            }
            mFragmentContent = fragment;
        }
    }

    private void setDefaultFragment() {
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction transaction = fm.beginTransaction();
        mHomeFragment = new HomeFragment();
        transaction.replace(R.id.main_frame, mHomeFragment);
        transaction.commit();
        mFragmentContent = mHomeFragment;
        mHomeText.setTextColor(mTabSelectedColor);
        mHomeImg.setImageResource(R.drawable.ic_tab_home_selected);
    }

    public void dismissDialog() {
        if (mDialog != null && mDialog.isShowing()) {
            mDialog.dismiss();
        }
        mDialog = null;
    }

    public void showDialog(@NonNull Dialog dialog) {
        dismissDialog();
        this.mDialog = dialog;
        mDialog.show();
    }

    private void requestPermissions(Set<String> permissions) {
        PermissionUtils.requestPermissions(this, permissions);
    }


    private void initCreateDialog() {
        mCreateDialog = new Dialog(this,R.style.dialog_bottom_full);
        mCreateDialog.setCanceledOnTouchOutside(true);
        mCreateDialog.setCancelable(true);
        Window window = mCreateDialog.getWindow();
//        window.setBackgroundDrawable(new BitmapDrawable(getResources(), FastBlurUtility.getBlurBackgroundDrawer(this)));
        window.setGravity(Gravity.BOTTOM);
        window.setWindowAnimations(R.style.more_animation);
        View view = View.inflate(this, R.layout.dialog_create,null);
        view.findViewById(R.id.close).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mCreateDialog != null && mCreateDialog.isShowing()){
                    mCreateDialog.dismiss();
                }
            }
        });
        view.findViewById(R.id.create_album).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismissCreateDialog();
                Intent intent = new Intent(getApplicationContext(), ImageSelectActivity.class);
                intent.putExtra("album", "Camera");//Environment.DIRECTORY_DCIM);
                startActivity(intent);
            }
        });
        view.findViewById(R.id.update_picture).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismissCreateDialog();
                startActivity(new Intent(getApplicationContext(), ProduceActivity.class));
            }
        });
        view.findViewById(R.id.edit_draft).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                File jsonFile = new File(FileUtils.getFileDir(getApplicationContext(), "note"), "note.json");
                Gson gson = new Gson();
                ArrayList<DraftData> noteDatas;
                if (jsonFile.exists()) {
                    String json = FileUtils.readFileToString(jsonFile);
                    if (json != null) {
                        noteDatas = gson.fromJson(json, new TypeToken<ArrayList<DraftData>>(){}.getType());
                    } else {
                        noteDatas = null;
                    }
                } else {
                    noteDatas = null;
                }
                if (noteDatas != null && noteDatas.size() > 0) {
                    Collections.sort(noteDatas, new Comparator<DraftData>() {
                        @Override
                        public int compare(DraftData o1, DraftData o2) {
                            if (o2.getTime() != null && o1.getTime() != null) {
                                return o2.getTime().compareTo(o1.getTime());
                            } else {
                                return 0;
                            }
                        }
                    });
                    final DraftData noteData = noteDatas.get(0);
                    Intent intent = new Intent(getApplicationContext(), PublishActivity.class);
                    intent.putStringArrayListExtra("urls", noteData.getUrls());
                    intent.putStringArrayListExtra("savedUrls", noteData.getSavedUrls());
                    intent.putExtra("effect", noteData.getEffectDatas());
                    intent.putExtra("title", noteData.getTitle());
                    intent.putExtra("intro", noteData.getIntro());
                    intent.putExtra("type", noteData.getType());
                    intent.putExtra("draftIndex", 0);
                    startActivity(intent);
                } else {
                    MiscUtils.makeToast(MainActivity.this, "目前没有草稿可以编辑", false);
                }
            }
        });
        window.setContentView(view);
        window.setLayout(WindowManager.LayoutParams.MATCH_PARENT,WindowManager.LayoutParams.WRAP_CONTENT);
    }

    public void showCreateDialog(){
        if (mCreateDialog == null){
            initCreateDialog();
        }
        mCreateDialog.show();
    }

    public void dismissCreateDialog() {
        if (mCreateDialog != null && mCreateDialog.isShowing()) {
            mCreateDialog.dismiss();
        }
        mCreateDialog = null;
    }

}
