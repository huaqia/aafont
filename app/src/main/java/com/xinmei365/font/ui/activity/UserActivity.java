package com.xinmei365.font.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.AppCompatTextView;
import android.view.View;
import android.widget.LinearLayout;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.xinmei365.font.MyApplication;
import com.xinmei365.font.R;
import com.xinmei365.font.model.Note;
import com.xinmei365.font.model.User;
import com.xinmei365.font.ui.adapter.PageAdapter;
import com.xinmei365.font.ui.fragment.NoteFragment;
import com.xinmei365.font.utils.BackendUtils;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import cn.bmob.newim.BmobIM;
import cn.bmob.newim.bean.BmobIMConversation;
import cn.bmob.newim.bean.BmobIMUserInfo;
import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.BmobUser;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.CountListener;
import cn.bmob.v3.listener.FindListener;
import cn.bmob.v3.listener.UpdateListener;
import de.hdodenhof.circleimageview.CircleImageView;

public class UserActivity extends BaseActivity {
    private static String NOTE = "笔记";
    private static String FAVORITE = "收藏";

    @BindView(R.id.image_select_close)
    AppCompatImageView mClose;
    @BindView(R.id.user_name)
    AppCompatTextView mName;
    @BindView(R.id.app_id)
    AppCompatTextView mAppId;
    @BindView(R.id.intro_string)
    AppCompatTextView mIntro;
    @BindView(R.id.focus_action)
    AppCompatTextView mFocusAction;
    @BindView(R.id.focus_num)
    AppCompatTextView mFocusText;
    @BindView(R.id.focus_area)
    LinearLayout mFocusArea;
    @BindView(R.id.follow_num)
    AppCompatTextView mFollowText;
    @BindView(R.id.follow_area)
    LinearLayout mFollowArea;
    @BindView(R.id.work_num)
    AppCompatTextView mWorkText;
    @BindView(R.id.profile_image)
    CircleImageView mUserIcon;
    @BindView(R.id.gender_icon)
    AppCompatImageView mGenderIcon;
    @BindView(R.id.chat_icon)
    AppCompatImageView mChatIcon;
    @BindView(R.id.viewpager)
    ViewPager mViewPager;
    @BindView(R.id.tabLayout)
    TabLayout mTabLayout;

    private List<Fragment> mFragments = new ArrayList<>();
    private List<String> mTitles = new ArrayList<>();
    private NoteFragment mNoteFragment;
    private NoteFragment mFavoriteFragment;
    private PageAdapter mPageAdapter;
    private String mUserId;

    @Override
    protected void setMyContentView() {
        setContentView(R.layout.activity_user);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        Intent intent = getIntent();
        String userId = intent.getStringExtra("id");
        mUserId = userId;
        initData();
        mNoteFragment = new NoteFragment();
        mNoteFragment.setId(userId);
        mFragments.add(mNoteFragment);
        mFavoriteFragment = new NoteFragment();
        mFavoriteFragment.setFavoriteId(userId);
        mFragments.add(mFavoriteFragment);
        mTitles.add(NOTE);
        mTitles.add(FAVORITE);
        mPageAdapter = new PageAdapter(getSupportFragmentManager(), mFragments, mTitles);
        mViewPager.setAdapter(mPageAdapter);
        mTabLayout.setupWithViewPager(mViewPager);
        mTabLayout.setTabMode(TabLayout.MODE_FIXED);
    }

    private void initData() {
        BmobQuery<User> userQuery = new BmobQuery<>();
        userQuery.addWhereEqualTo("objectId" , mUserId);
        userQuery.findObjects(new FindListener<User>() {
            @Override
            public void done(List<User> list, BmobException e) {
                if (e == null) {
                    if (list.size() == 1) {
                        final User user = list.get(0);
                        ArrayList<String> ids = user.getFocusIds();
                        if (ids == null) {
                            mFocusText.setText("0");
                        } else {
                            mFocusText.setText(ids.size() + "");
                            ids.remove(mUserId);
                        }
                        mFocusArea.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Intent intent = new Intent(getApplicationContext(), RelatedUsersActivity.class);
                                intent.putExtra("type", 3);
                                intent.putExtra("userId", mUserId);
                                startActivity(intent);
                            }
                        });
                        mName.setText(user.getNickName());
                        mAppId.setText("字体管家号:" + user.getAppId());
                        if (user.getIntro() != null) {
                            mIntro.setText(user.getIntro());
                        }
                        if (user.getGender() == 0) {
                            mGenderIcon.setImageResource(R.drawable.ic_sex_boy);
                        } else {
                            mGenderIcon.setImageResource(R.drawable.ic_sex_girl);
                        }
                        if (user.getAvatar() != null) {
                            Glide.with(MyApplication.getInstance())
                                    .load(user.getAvatar())
                                    .fitCenter()
                                    .diskCacheStrategy(DiskCacheStrategy.RESULT)
                                    .into(mUserIcon);
                        }
                        mChatIcon.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                BmobIMUserInfo info = new BmobIMUserInfo(user.getObjectId(), user.getNickName(), user.getAvatar());
                                BmobIMConversation conversationEntrance = BmobIM.getInstance().startPrivateConversation(info, null);
                                if (conversationEntrance != null) {
                                    Intent intent = new Intent(UserActivity.this, ChatActivity.class);
                                    intent.putExtra("conversation", conversationEntrance);
                                    startActivity(intent);
                                }
                            }
                        });
                    } else {
                        mFocusText.setText("0");
                        mFocusArea.setOnClickListener(null);
                    }
                } else {
                    BackendUtils.handleException(e, UserActivity.this);
                    mFocusText.setText("0");
                    mFocusArea.setOnClickListener(null);
                }
            }
        });
        userQuery = new BmobQuery<>();
        ArrayList<String> ids = new ArrayList<>();
        ids.add(mUserId);
        userQuery.addWhereContainsAll("focusIds" , ids);
        userQuery.addWhereNotEqualTo("objectId", mUserId);
        userQuery.count(User.class, new CountListener() {
            @Override
            public void done(Integer integer, BmobException e) {
                if (e == null){
                    mFollowText.setText(integer + "");
                    mFollowArea.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Intent intent = new Intent(getApplicationContext(), RelatedUsersActivity.class);
                            intent.putExtra("type", 4);
                            intent.putExtra("userId", mUserId);
                            startActivity(intent);
                        }
                    });
                } else {
                    BackendUtils.handleException(e, UserActivity.this);
                    mFollowText.setText("0");
                    mFocusArea.setOnClickListener(null);
                }
            }
        });
        BmobQuery<Note> queryP = new BmobQuery<>();
        queryP.addWhereEqualTo("user" , mUserId);
        queryP.order("-createdAt");
        queryP.findObjects(new FindListener<Note>() {
            @Override
            public void done(List<Note> list, BmobException e) {
                if (e == null){
                    int count = 0;
                    for (Note note : list) {
                        if (note.getFavoriteIds() != null) {
                            count += note.getFavoriteIds().size();
                        }
                        if (note.getLikeIds() != null) {
                            count += note.getLikeIds().size();
                        }
                    }
                    mWorkText.setText(count + "");
                } else {
                    BackendUtils.handleException(e, UserActivity.this);
                    mWorkText.setText("0");
                }
            }
        });
        final User currentUser = BmobUser.getCurrentUser(User.class);
        if (!mUserId.equals(currentUser.getObjectId())) {
            BmobQuery<User> query = new BmobQuery<>();
            query.addWhereEqualTo("objectId" , currentUser.getObjectId());
            query.findObjects(new FindListener<User>() {
                @Override
                public void done(List<User> list, BmobException e) {
                    if (e == null) {
                        boolean follow = true;
                        if (list.size() == 1) {
                            User user = list.get(0);
                            ArrayList<String> relations = user.getFocusIds();
                            if (relations != null && relations.contains(mUserId)) {
                                follow = false;
                            }
                        }
                        if (follow) {
                            mFocusAction.setText(R.string.follow);
                            mFocusAction.setBackgroundResource(R.drawable.ic_ok);
                        } else {
                            mFocusAction.setText(R.string.unfollow);
                            mFocusAction.setBackgroundResource(R.drawable.ic_nok);
                        }
                    } else {
                        BackendUtils.handleException(e, UserActivity.this);
                    }
                }
            });
        }
        mFocusAction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final User currentUser = BmobUser.getCurrentUser(User.class);
                BmobQuery<User> query = new BmobQuery<>();
                query.addWhereEqualTo("objectId" , currentUser.getObjectId());
                query.findObjects(new FindListener<User>() {
                    @Override
                    public void done(List<User> list, BmobException e) {
                        if (e == null) {
                            ArrayList<String> focusIdList = new ArrayList<>();
                            if (list.size() == 1) {
                                User user = list.get(0);
                                if (user.getFocusIds() != null) {
                                    focusIdList.addAll(user.getFocusIds());
                                }
                                if (user.getFocusIds() == null || !user.getFocusIds().contains(mUserId)) {
                                    focusIdList.add(mUserId);
                                    mFocusAction.setText(R.string.unfollow);
                                    mFocusAction.setBackgroundResource(R.drawable.ic_nok);
                                    BackendUtils.pushMessage(UserActivity.this, user, "FOLLOW", null);
                                } else {
                                    focusIdList.remove(mUserId);
                                    mFocusAction.setText(R.string.follow);
                                    mFocusAction.setBackgroundResource(R.drawable.ic_ok);
                                }
                                user.setFocusIds(focusIdList);
                                user.update(new UpdateListener() {
                                    @Override
                                    public void done(BmobException e) {
                                        BackendUtils.handleException(e, UserActivity.this);
                                    }
                                });
                            }
                        } else {
                            BackendUtils.handleException(e, UserActivity.this);
                        }
                    }
                });
            }
        });
    }
}
