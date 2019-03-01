package com.hanmei.aafont.ui.activity;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.widget.SwitchCompat;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.RadioGroup;

import com.hanmei.aafont.R;
import com.hanmei.aafont.utils.BackendUtils;
import com.hanmei.aafont.utils.SharedPreferencesUtils;

import java.util.Arrays;

import butterknife.BindView;

public class PushSettingActivity extends BaseActivity {
    @BindView(R.id.push_notice)
    SwitchCompat mPushNotice;
    @BindView(R.id.private_notice)
    RadioGroup mPrivateNotice;
    @BindView(R.id.like_notice)
    RadioGroup mLikeNotice;
    @BindView(R.id.comment_notice)
    RadioGroup mCommentNotice;
    @BindView(R.id.at_notice)
    RadioGroup mAtNotice;
    @BindView(R.id.fans_notice)
    RadioGroup mFansNotice;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_set);
        toolbar.setTitle(R.string.push_setting_title);
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.drawable.back);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        setCheckedStatus();
        setOnCheckedChangeListeners();
    }

    @Override
    protected void setMyContentView() {
        setContentView(R.layout.activity_push_setting);
    }

    private void setCheckedStatus() {
        Context context = getApplicationContext();
        int privateSetting = SharedPreferencesUtils.getInt(context, SharedPreferencesUtils.PREF_PRIVATE_SETTING, 0);
        switch (privateSetting) {
            case 1:
                mPrivateNotice.check(R.id.private_follow);
                break;
            case 2:
                mPrivateNotice.check(R.id.private_close);
                break;
            default:
                mPrivateNotice.check(R.id.private_all);
                break;
        }
        int likeSetting = SharedPreferencesUtils.getInt(context, SharedPreferencesUtils.PREF_LIKE_SETTING, 0);
        switch (likeSetting) {
            case 1:
                mLikeNotice.check(R.id.like_follow);
                break;
            case 2:
                mLikeNotice.check(R.id.like_close);
                break;
            default:
                mLikeNotice.check(R.id.like_all);
                break;
        }
        int commentSetting = SharedPreferencesUtils.getInt(context, SharedPreferencesUtils.PREF_COMMENT_SETTING, 0);
        switch (commentSetting) {
            case 1:
                mCommentNotice.check(R.id.comment_follow);
                break;
            case 2:
                mCommentNotice.check(R.id.comment_close);
                break;
            default:
                mCommentNotice.check(R.id.comment_all);
                break;
        }
        int atSetting = SharedPreferencesUtils.getInt(context, SharedPreferencesUtils.PREF_AT_SETTING, 0);
        switch (atSetting) {
            case 1:
                mAtNotice.check(R.id.at_follow);
                break;
            case 2:
                mAtNotice.check(R.id.at_close);
                break;
            default:
                mAtNotice.check(R.id.at_all);
                break;
        }
        int fansSetting = SharedPreferencesUtils.getInt(context, SharedPreferencesUtils.PREF_FANS_SETTING, 0);
        switch (fansSetting) {
            case 1:
                mFansNotice.check(R.id.fans_follow);
                break;
            case 2:
                mFansNotice.check(R.id.fans_close);
                break;
            default:
                mFansNotice.check(R.id.fans_all);
                break;
        }
        boolean pushNotice = SharedPreferencesUtils.getBoolean(context, SharedPreferencesUtils.PREF_PUSH_NOTICE, true);
        mPushNotice.setChecked(pushNotice);
        applyPushNoticeChecked(pushNotice);
    }

    private void setOnCheckedChangeListeners() {
        mPushNotice.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                applyPushNoticeChecked(isChecked);
                SharedPreferencesUtils.setBoolean(getApplicationContext(), SharedPreferencesUtils.PREF_PUSH_NOTICE, isChecked);
            }
        });
        mPrivateNotice.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                checkPrivateSetting(i, true);
            }
        });
        mLikeNotice.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                checkLikeSetting(i, true);
            }
        });
        mCommentNotice.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                checkCommentSetting(i, true);
            }
        });
        mAtNotice.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                checkAtSetting(i, true);
            }
        });
        mFansNotice.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                checkFansSetting(i, true);
            }
        });
    }

    private void applyPushNoticeChecked(boolean isChecked) {
        if (isChecked) {
            checkPrivateSetting(mPrivateNotice.getCheckedRadioButtonId(), false);
            checkLikeSetting(mLikeNotice.getCheckedRadioButtonId(), false);
            checkCommentSetting(mCommentNotice.getCheckedRadioButtonId(), false);
            checkAtSetting(mAtNotice.getCheckedRadioButtonId(), false);
            checkFansSetting(mFansNotice.getCheckedRadioButtonId(), false);
        } else {
            BackendUtils.unsubscribe(Arrays.asList("MESSAGE", "LIKE", "COMMENT", "AT", "FOLLOW"), new BackendUtils.DoneCallback() {
                @Override
                public void onDone(boolean success, int code) {
                }
            });
        }
    }

    private void checkPrivateSetting(int checkedId, boolean save) {
        int value = 0;
        if (checkedId == R.id.private_close) {
            if (mPushNotice.isChecked()) {
                BackendUtils.unsubscribe("MESSAGE", new BackendUtils.DoneCallback() {
                    @Override
                    public void onDone(boolean success, int code) {
                    }
                });
            }
            value = 2;
        } else {
            if (mPushNotice.isChecked()) {
                BackendUtils.subscribe("MESSAGE", new BackendUtils.DoneCallback() {
                    @Override
                    public void onDone(boolean success, int code) {
                    }
                });
            }
            if (checkedId == R.id.private_follow) {
                value = 1;
            }
        }
        if (save) {
            SharedPreferencesUtils.setInt(getApplicationContext(), SharedPreferencesUtils.PREF_PRIVATE_SETTING, value);
        }
    }

    private void checkLikeSetting(int checkedId, boolean save) {
        int value = 0;
        if (checkedId == R.id.like_close) {
            if (mPushNotice.isChecked()) {
                BackendUtils.unsubscribe("LIKE", new BackendUtils.DoneCallback() {
                    @Override
                    public void onDone(boolean success, int code) {
                    }
                });
            }
            value = 2;
        } else {
            if (mPushNotice.isChecked()) {
                BackendUtils.subscribe("LIKE", new BackendUtils.DoneCallback() {
                    @Override
                    public void onDone(boolean success, int code) {
                    }
                });
            }
            if (checkedId == R.id.like_follow) {
                value = 1;
            }
        }
        if (save) {
            SharedPreferencesUtils.setInt(getApplicationContext(), SharedPreferencesUtils.PREF_LIKE_SETTING, value);
        }
    }

    private void checkCommentSetting(int checkedId, boolean save) {
        int value = 0;
        if (checkedId == R.id.comment_close) {
            if (mPushNotice.isChecked()) {
                BackendUtils.unsubscribe("COMMENT", new BackendUtils.DoneCallback() {
                    @Override
                    public void onDone(boolean success, int code) {
                    }
                });
            }
            value = 2;
        } else {
            if (mPushNotice.isChecked()) {
                BackendUtils.subscribe("COMMENT", new BackendUtils.DoneCallback() {
                    @Override
                    public void onDone(boolean success, int code) {
                    }
                });
            }
            if (checkedId == R.id.comment_follow) {
                value = 1;
            }
        }
        if (save) {
            SharedPreferencesUtils.setInt(getApplicationContext(), SharedPreferencesUtils.PREF_COMMENT_SETTING, value);
        }
    }

    private void checkAtSetting(int checkedId, boolean save) {
        int value = 0;
        if (checkedId == R.id.at_close) {
            if (mPushNotice.isChecked()) {
                BackendUtils.unsubscribe("AT", new BackendUtils.DoneCallback() {
                    @Override
                    public void onDone(boolean success, int code) {
                    }
                });
            }
            value = 2;
        } else {
            if (mPushNotice.isChecked()) {
                BackendUtils.subscribe("AT", new BackendUtils.DoneCallback() {
                    @Override
                    public void onDone(boolean success, int code) {
                    }
                });
            }
            if (checkedId == R.id.at_follow) {
                value = 1;
            }
        }
        if (save) {
            SharedPreferencesUtils.setInt(getApplicationContext(), SharedPreferencesUtils.PREF_AT_SETTING, value);
        }
    }

    private void checkFansSetting(int checkedId, boolean save) {
        int value = 0;
        if (checkedId == R.id.fans_close) {
            if (mPushNotice.isChecked()) {
                BackendUtils.unsubscribe("FOLLOW", new BackendUtils.DoneCallback() {
                    @Override
                    public void onDone(boolean success, int code) {
                    }
                });
            }
            value = 2;
        } else {
            if (mPushNotice.isChecked()) {
                BackendUtils.subscribe("FOLLOW", new BackendUtils.DoneCallback() {
                    @Override
                    public void onDone(boolean success, int code) {
                    }
                });
            }
            if (checkedId == R.id.fans_follow) {
                value = 1;
            }
        }
        if (save) {
            SharedPreferencesUtils.setInt(getApplicationContext(), SharedPreferencesUtils.PREF_FANS_SETTING, value);
        }
    }
}
