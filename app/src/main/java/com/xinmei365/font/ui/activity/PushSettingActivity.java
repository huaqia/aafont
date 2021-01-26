package com.xinmei365.font.ui.activity;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.SwitchCompat;
import android.view.View;
import android.widget.CompoundButton;

import com.xinmei365.font.R;
import com.xinmei365.font.model.User;
import com.xinmei365.font.utils.BackendUtils;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.BmobUser;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FindListener;
import cn.bmob.v3.listener.UpdateListener;

public class PushSettingActivity extends BaseActivity {
    @BindView(R.id.close)
    AppCompatImageView mClose;
    @BindView(R.id.like_notice)
    SwitchCompat mLikeNotice;
    @BindView(R.id.favorite_notice)
    SwitchCompat mFavoriteNotice;
    @BindView(R.id.follow_notice)
    SwitchCompat mFollowNotice;
    @BindView(R.id.comment_notice)
    SwitchCompat mCommentNotice;
//    @BindView(R.id.at_notice)
//    SwitchCompat mAtNotice;

    private String mUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        mUserId = BmobUser.getCurrentUser(User.class).getObjectId();
        setCheckedStatus();
        setOnCheckedChangeListeners();
    }

    @Override
    protected void setMyContentView() {
        setContentView(R.layout.activity_push_setting);
    }

    private void setCheckedStatus() {
        BmobQuery<User> queryU = new BmobQuery<>();
        queryU.addWhereEqualTo("objectId" , mUserId);
        queryU.findObjects(new FindListener<User>() {
            @Override
            public void done(List<User> list, BmobException e) {
                if (e == null) {
                    User user = list.get(0);
                    ArrayList<String> channels = user.getChannels();
                    boolean likeNotice = true;
                    boolean favoriteNotice = true;
                    boolean followNotice = true;
                    boolean commentNotice = true;
                    if (channels != null) {
                        if (channels.contains("LIKE")) {
                            likeNotice = false;
                        }
                        if (channels.contains("FAVORITE")) {
                            favoriteNotice = false;
                        }
                        if (channels.contains("FOLLOW")) {
                            followNotice = false;
                        }
                        if (channels.contains("COMMENT")) {
                            commentNotice = false;
                        }
                    }
                    mLikeNotice.setChecked(likeNotice);
                    mFavoriteNotice.setChecked(favoriteNotice);
                    mFollowNotice.setChecked(followNotice);
                    mCommentNotice.setChecked(commentNotice);
                } else {
                    BackendUtils.handleException(e, PushSettingActivity.this);
                }
            }
        });
    }

    private void updateChannels(final String channel, final boolean isChecked) {
        BmobQuery<User> queryU = new BmobQuery<>();
        queryU.addWhereEqualTo("objectId" , mUserId);
        queryU.findObjects(new FindListener<User>() {
            @Override
            public void done(List<User> list, BmobException e) {
                if (e == null) {
                    if (list.size() == 1) {
                        User user = list.get(0);
                        final ArrayList<String> channels = new ArrayList<>();
                        if (user.getChannels() != null) {
                            channels.addAll(user.getChannels());
                        }
                        if (isChecked) {
                            channels.remove(channel);
                        } else {
                            if (!channels.contains(channel)) {
                                channels.add(channel);
                            }
                        }
                        user.setChannels(channels);
                        user.update(new UpdateListener() {
                            @Override
                            public void done(BmobException e) {
                                BackendUtils.handleException(e, PushSettingActivity.this);
                            }
                        });
                    }
                } else {
                    BackendUtils.handleException(e, PushSettingActivity.this);
                }
            }
        });

    }

    private void setOnCheckedChangeListeners() {
        final Context context = getApplicationContext();
        mLikeNotice.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, final boolean isChecked) {
                updateChannels("LIKE", isChecked);
            }
        });
        mFavoriteNotice.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                updateChannels("FAVORITE", isChecked);
            }
        });
        mFollowNotice.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                updateChannels("FOLLOW", isChecked);
            }
        });
        mCommentNotice.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                updateChannels("COMMIT", isChecked);
            }
        });
//        mAtNotice.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
//            @Override
//            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
//                updateChannels("AT", isChecked);
//            }
//        });
    }
}
