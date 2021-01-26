package com.xinmei365.font.ui.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.AppCompatTextView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.xinmei365.font.MyApplication;
import com.xinmei365.font.R;
import com.xinmei365.font.model.Note;
import com.xinmei365.font.model.User;
import com.xinmei365.font.ui.activity.EditUserMessageActivity;
import com.xinmei365.font.ui.activity.MainActivity;
import com.xinmei365.font.ui.activity.RelatedUsersActivity;
import com.xinmei365.font.ui.activity.SettingActivity;
import com.xinmei365.font.ui.adapter.PageAdapter;
import com.xinmei365.font.utils.BackendUtils;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;
import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.BmobUser;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.CountListener;
import cn.bmob.v3.listener.FindListener;
import de.hdodenhof.circleimageview.CircleImageView;

public class MeFragment extends BaseFragment {
    private static final String TAG = "MeFragment";

    private static String WROTE = "笔记";
    private static String FAVORITE = "收藏";
    private static String LIKE = "赞过";
    @BindView(R.id.user_name)
    AppCompatTextView mName;
    @BindView(R.id.app_id)
    AppCompatTextView mAppId;
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
    @BindView(R.id.edit_intro)
    AppCompatTextView mEditIntro;
    @BindView(R.id.tabLayout)
    TabLayout mTabLayout;
    @BindView(R.id.viewpager)
    ViewPager mViewPager;
    private NoteFragment mNoteFragment;
    private NoteFragment mFavoriteFragment;
    private NoteFragment mPraiseFragment;
    private PageAdapter mPageAdapter;
    private List<Fragment> mFragments = new ArrayList<>();
    private List<String> mTitles = new ArrayList<>();

    @Override
    public View createMyView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_me, container, false);
    }

    @Override
    public void init() {
        initData();
        final User currentUser = BmobUser.getCurrentUser(User.class);
        String currentUserId = currentUser.getObjectId();
        mNoteFragment = new NoteFragment();
        mNoteFragment.setId(currentUserId);
        mFavoriteFragment = new NoteFragment();
        mFavoriteFragment.setFavoriteId(currentUserId);
        mPraiseFragment = new NoteFragment();
        mPraiseFragment.setLikeId(currentUserId);
        mFragments.add(mNoteFragment);
        mFragments.add(mFavoriteFragment);
        mFragments.add(mPraiseFragment);
        mTitles.add(WROTE);
        mTitles.add(FAVORITE);
        mTitles.add(LIKE);
        mPageAdapter = new PageAdapter(getChildFragmentManager(), mFragments, mTitles);
        mViewPager.setAdapter(mPageAdapter);
        //一次加载所有的页面,默认是一次加载两个页面，所以在切换的时候会有一点问题
        mViewPager.setOffscreenPageLimit(mTitles.size());
        mTabLayout.setupWithViewPager(mViewPager);
        mTabLayout.setTabMode(TabLayout.MODE_FIXED);
        mViewPager.setCurrentItem(0);
    }

    @Override
    public void onResume() {
        super.onResume();
        initData();
    }

    private void updateBaseInfo(final User currentUser) {
        BmobQuery<User> queryU = new BmobQuery<>();
        queryU.addWhereEqualTo("objectId" , currentUser.getObjectId());
        queryU.findObjects(new FindListener<User>() {
            @Override
            public void done(List<User> list, BmobException e) {
                if (e == null){
                    User user = list.get(0);
                    String intro = user.getIntro();
                    if (TextUtils.isEmpty(intro)) {
                        mEditIntro.setText("点击此处填写个人简介...");
                        mEditIntro.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                startActivity(new Intent(getContext() , EditUserMessageActivity.class));
                            }
                        });
                    } else {
                        mEditIntro.setText(user.getIntro());
                        mEditIntro.setOnClickListener(null);
                    }
                    mName.setText(user.getNickName());
                    mAppId.setText("字体管家号:" + user.getAppId());
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
                } else {
                    BackendUtils.handleException(e, getContext());
                }
            }
        });
    }

    private void initData(){
        final User currentUser = BmobUser.getCurrentUser(User.class);
        BmobQuery<User> userQuery = new BmobQuery<>();
        userQuery.addWhereEqualTo("objectId" , currentUser.getObjectId());
        userQuery.findObjects(new FindListener<User>() {
            @Override
            public void done(List<User> list, BmobException e) {
                if (e == null) {
                    if (list.size() == 1) {
                        User user = list.get(0);
                        ArrayList<String> ids = user.getFocusIds();
                        if (ids == null) {
                            mFocusText.setText("0");
                        } else {
                            ids.remove(currentUser.getObjectId());
                            mFocusText.setText(ids.size() + "");
                        }
                    } else {
                        mFocusText.setText("0");
                    }
                    mFocusArea.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Intent intent = new Intent(getContext(), RelatedUsersActivity.class);
                            intent.putExtra("type", 1);
                            startActivity(intent);
                        }
                    });
                } else {
                    BackendUtils.handleException(e, getContext());
                    mFocusText.setText("0");
                    mFocusArea.setOnClickListener(null);
                }
            }
        });
        userQuery = new BmobQuery<>();
        ArrayList<String> ids = new ArrayList<>();
        ids.add(currentUser.getObjectId());
        userQuery.addWhereContainsAll("focusIds" , ids);
        userQuery.addWhereNotEqualTo("objectId", currentUser.getObjectId());
        userQuery.count(User.class, new CountListener() {
            @Override
            public void done(Integer integer, BmobException e) {
                if (e == null){
                    mFollowText.setText(integer + "");
                    mFollowArea.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Intent intent = new Intent(getContext(), RelatedUsersActivity.class);
                            intent.putExtra("type", 2);
                            startActivity(intent);
                        }
                    });
                } else {
                    BackendUtils.handleException(e, getContext());
                    mFollowText.setText("0");
                    mFocusArea.setOnClickListener(null);
                }
            }
        });
        BmobQuery<Note> queryP = new BmobQuery<>();
        queryP.addWhereEqualTo("user" , currentUser);
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
                    BackendUtils.handleException(e, getContext());
                    mWorkText.setText("0");
                }
            }
        });
        updateBaseInfo(currentUser);
    }

    @OnClick({R.id.iv_setting, R.id.profile_edit, R.id.create_new})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.iv_setting:
                startActivity(new Intent(getContext(), SettingActivity.class));
                break;
            case R.id.profile_edit:
                startActivity(new Intent(getContext() , EditUserMessageActivity.class));
                break;
            case R.id.create_new:
                ((MainActivity)getActivity()).showCreateDialog();
                break;
        default:
                break;
        }
    }
}
