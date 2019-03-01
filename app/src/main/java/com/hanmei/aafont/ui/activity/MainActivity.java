package com.hanmei.aafont.ui.activity;

import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import butterknife.BindView;
import butterknife.OnClick;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.hanmei.aafont.R;
import com.hanmei.aafont.ui.fragment.FindFragment;
import com.hanmei.aafont.ui.fragment.HomeFragment;
import com.hanmei.aafont.ui.fragment.MeFragment;
import com.hanmei.aafont.ui.fragment.NoticeFragment;
import com.hanmei.aafont.utils.BackendUtils;
import com.hanmei.aafont.utils.Constant;
import com.hanmei.aafont.utils.PermissionUtils;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class MainActivity extends BaseActivity {
    private Dialog mDialog;
    @BindView(R.id.image_home)
    ImageView mHomeImg;
    @BindView(R.id.image_find)
    ImageView mFindImg;
    @BindView(R.id.image_notice)
    ImageView mNoticeImg;
    @BindView(R.id.image_me)
    ImageView mMeImg;

    private Fragment mFragmentContent;
    private HomeFragment mHomeFragment;
    private FindFragment mFindFragment;
    private NoticeFragment mNoticeFragment;
    private MeFragment mMeFragment;

    private Boolean mNeedRequestPermissions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        BackendUtils.subscribe(Arrays.asList("MESSAGE", "LIKE", "COMMENT", "AT", "FOLLOW"), new BackendUtils.DoneCallback() {
            @Override
            public void onDone(boolean success, int code) {
            }
        });
        setDefaultFragment();
        mNeedRequestPermissions = true;
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

    @OnClick({R.id.image_home, R.id.image_find, R.id.image_create, R.id.image_notice, R.id.image_me})
    public void tableClick(View view) {
        switch (view.getId()) {
            case R.id.image_home:
                resetComponentState();
                mHomeImg.setImageResource(R.drawable.home_pressed);
                if (mHomeFragment == null) {
                    mHomeFragment = new HomeFragment();
                }
                switchFragment(mHomeFragment);
                break;
            case R.id.image_find:
                resetComponentState();
                mFindImg.setImageResource(R.drawable.find_pressed);
                if (mFindFragment == null) {
                    mFindFragment = new FindFragment();
                }
                switchFragment(mFindFragment);
//                BackendUtils.pushMessage(BackendUtils.getCurrentUser(), "FOLLOW", "消息内容");
                break;
            case R.id.image_create:
                startActivity(new Intent(this, ProduceActivity.class));
                break;
            case R.id.image_notice: {
                resetComponentState();
                mNoticeImg.setImageResource(R.drawable.notice_pressed);
                if (mNoticeFragment == null) {
                    mNoticeFragment = new NoticeFragment();
                }
                switchFragment(mNoticeFragment);
//                Intent intent = new Intent();
//                intent.setComponent(new ComponentName("com.nearme.themespace", "com.nearme.themespace.activities.FontDetailActivity"));
//                intent.putExtra("fontId", "d51cbbbf629849d08b5b7de8f707ee56");
//                startActivity(intent);
//                Intent intent = new Intent();
//                if (RomUtils.isVivo()) {
//                } else if (RomUtils.isOppo()) {
//                } else if (RomUtils.isEmui()) {
//                    intent.setComponent(new ComponentName("com.huawei.android.thememanager", "com.huawei.android.thememanager.SearchActivity"));
//                } else if (RomUtils.isMiui()) {
//                    intent.setComponent(new ComponentName("com.android.thememanager", "com.android.thememanager.search.ThemeSearchActivity"));
//                }
////        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                startActivity(intent);
            }
                break;
            case R.id.image_me:
                resetComponentState();
                mMeImg.setImageResource(R.drawable.me_pressed);
                if (mMeFragment == null) {
                    mMeFragment = new MeFragment();
                }
                switchFragment(mMeFragment);
                break;
            default:
                break;
        }
    }

    public void goToChoiceFragment() {
        resetComponentState();
        mFindImg.setImageResource(R.drawable.find_pressed);
        if (mFindFragment == null) {
            mFindFragment = new FindFragment();
        }
        switchFragment(mFindFragment);
        mFindFragment.goToChoiceFragment();
    }

    private void resetComponentState() {
        mHomeImg.setImageResource(R.drawable.home);
        mFindImg.setImageResource(R.drawable.find);
        mNoticeImg.setImageResource(R.drawable.notice);
        mMeImg.setImageResource(R.drawable.me);
    }

    private void switchFragment(Fragment fragment){
        if (mFragmentContent != fragment) {
            FragmentManager fm = getSupportFragmentManager();
            FragmentTransaction transaction = fm.beginTransaction();
            if (!fragment.isAdded()) {
                transaction.hide(mFragmentContent).add(R.id.main_frame,fragment).commit();
            } else {
                transaction.hide(mFragmentContent).show(fragment).commit();
            }
            mFragmentContent = fragment;
        }
    }

    private void setDefaultFragment(){
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction transaction =fm.beginTransaction();
        mHomeFragment = new HomeFragment();
        transaction.replace(R.id.main_frame, mHomeFragment);
        transaction.commit();
        mFragmentContent = mHomeFragment;
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
}
