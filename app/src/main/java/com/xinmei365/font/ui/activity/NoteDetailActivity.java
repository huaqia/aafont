package com.xinmei365.font.ui.activity;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.AppCompatEditText;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.AppCompatTextView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.ExpandableListView;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.xinmei365.font.MyApplication;
import com.xinmei365.font.R;
import com.xinmei365.font.model.Comment;
import com.xinmei365.font.model.EffectData;
import com.xinmei365.font.model.Note;
import com.xinmei365.font.model.Reply;
import com.xinmei365.font.model.User;
import com.xinmei365.font.ui.adapter.CommentExpandAdapter;
import com.xinmei365.font.ui.adapter.FragmentViewPagerAdapter;
import com.xinmei365.font.ui.fragment.DetailPicFragment;
import com.xinmei365.font.ui.widget.CommentExpandAbleListView;
import com.xinmei365.font.utils.BackendUtils;
import com.xinmei365.font.utils.DensityUtils;
import com.xinmei365.font.utils.FileUtils;
import com.xinmei365.font.utils.MiscUtils;
import com.xinmei365.font.utils.RomUtils;
import com.xinmei365.font.utils.TrackerUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import butterknife.BindView;
import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.BmobUser;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FindListener;
import cn.bmob.v3.listener.SaveListener;
import cn.bmob.v3.listener.UpdateListener;
import de.hdodenhof.circleimageview.CircleImageView;

public class NoteDetailActivity extends BaseActivity {
    @BindView(R.id.image_select_close)
    AppCompatImageView mClose;
    @BindView(R.id.focus_action)
    AppCompatTextView mFocusAction;
    @BindView(R.id.more_action)
    AppCompatImageView mMoreAction;
    @BindView(R.id.user_icon)
    CircleImageView mUserIcon;
    @BindView(R.id.user_name)
    AppCompatTextView mUserName;
    @BindView(R.id.nestedScrollView)
    NestedScrollView mNestedScrollView;
    @BindView(R.id.page_num)
    AppCompatTextView mPageNumber;
    @BindView(R.id.dynamic_banner)
    ViewPager mBanner;
    @BindView(R.id.note_title)
    AppCompatTextView mTitle;
    @BindView(R.id.download_font)
    AppCompatTextView mDownloadFont;
    @BindView(R.id.note_intro)
    AppCompatTextView mIntro;
    @BindView(R.id.note_time)
    AppCompatTextView mTime;
    @BindView(R.id.comment_icon)
    CircleImageView mCommentIcon;
    @BindView(R.id.to_comment)
    AppCompatTextView mToComment;
    @BindView(R.id.comment_title)
    AppCompatTextView mCommentTitle;
    @BindView(R.id.comment_view)
    CommentExpandAbleListView mCommentView;
    @BindView(R.id.action_comment)
    LinearLayout mActionComment;
    @BindView(R.id.btn_like)
    LinearLayout mBtnLike;
    @BindView(R.id.to_like)
    AppCompatImageView mToLike;
    @BindView(R.id.like_count)
    AppCompatTextView mLikeCount;
    @BindView(R.id.btn_favorite)
    LinearLayout mBtnFavorite;
    @BindView(R.id.to_favorite)
    AppCompatImageView mToFavorite;
    @BindView(R.id.favorite_count)
    AppCompatTextView mFavoriteCount;
    @BindView(R.id.btn_msg)
    LinearLayout mBtnMsg;
    @BindView(R.id.msg_count)
    AppCompatTextView mMsgCount;

    private Note mNote;
    private Dialog mMoreDialog;
    private CommentExpandAdapter mCommentAdapter;
    private ArrayList<Comment> mComments = new ArrayList<>();
    private Context mContext;
    private FragmentViewPagerAdapter mFragmentAdapter;
    private ArrayList<Fragment> mFragments;
    private int mFollowColor;
    private int mFollowedColor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;
        mFollowColor = getResources().getColor(R.color.colorNormalState);
        mFollowedColor = getResources().getColor(R.color.colorActiveState);
        mClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        if (getIntent() != null) {
            Intent intent = getIntent();
            mNote = (Note)intent.getSerializableExtra("note");
            refreshNoteInfo();
        }
        mMoreAction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showMoreDialog();
            }
        });
    }

    @Override
    protected void setMyContentView() {
        setContentView(R.layout.activity_note_detail);
    }

    private void refreshNoteInfo() {
        FrameLayout.LayoutParams params = (FrameLayout.LayoutParams)mBanner.getLayoutParams();
        float maxRatio = Float.parseFloat(mNote.getMaxRatio());
        int maxWidth = DensityUtils.getScreenW(mContext);
        params.height = (int)(maxWidth * maxRatio);
        mBanner.setLayoutParams(params);
        mFragments = new ArrayList<>();
        final ArrayList<String> pics = mNote.getPics();
        ArrayList<ArrayList<EffectData.TagData>> tagDatas = mNote.getTagDatas();
        for (int i = 0; i < pics.size(); i++) {
            DetailPicFragment fragment = new DetailPicFragment();
            fragment.setPath(pics.get(i));
            fragment.setHeight(params.height);
            if (tagDatas != null && tagDatas.size() > i) {
                fragment.setTagDatas(tagDatas.get(i));
            }
            fragment.setContext(this);
            mFragments.add(fragment);
        }
        mFragmentAdapter = new FragmentViewPagerAdapter(getSupportFragmentManager(), mBanner, mFragments);
        mPageNumber.setText(String.format("1/%d", pics.size()));
        mFragmentAdapter.setOnExtraPageChangeListener(new FragmentViewPagerAdapter.OnExtraPageChangeListener() {
            public void onExtraPageScrolled(int i, float v, int i2) {
            }

            public void onExtraPageSelected(int i) {
                mPageNumber.setText(String.format("%d/%d", i + 1, pics.size()));
            }

            public void onExtraPageScrollStateChanged(int i) {
            }
        });
//        mBanner.setAdapter(mFragmentAdapter);
        mBanner.setOffscreenPageLimit(9);
//        mBanner.setCurrentItem(0);
        final User noteUser = mNote.getUser();
        final String noteUserId = noteUser.getObjectId();
        mUserName.setText(noteUser.getNickName());
        if (!TextUtils.isEmpty(mNote.getTitle())) {
            mTitle.setVisibility(View.VISIBLE);
            mTitle.setText(mNote.getTitle());
        } else {
            mTitle.setVisibility(View.GONE);
        }
        final String noteType = mNote.getType();
        if (RomUtils.isOppo() && !TextUtils.isEmpty(mNote.getOppoFontId())) {
            mDownloadFont.setVisibility(View.VISIBLE);
            mDownloadFont.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String typeStr = "font";
                    if (noteType.equals("主题")) {
                        typeStr = "theme";
                    } else if (noteType.equals("壁纸")) {
                        typeStr = "wallpaper";
                    }
                    Map<String, String> map = new HashMap<>();
                    map.put("type", "oppo");
                    map.put("name", mNote.getOppoFontId());
                    TrackerUtils.onEvent(mContext, "jump_theme_store", map);
                    try {
                        Intent new_intent = new Intent(Intent.ACTION_VIEW, Uri.parse(String.format("oaps://theme/detail?rtp=%s&id=%s", typeStr, mNote.getOppoFontId()))); //oaps://theme/detail?rtp=font&id=2246947&openinsystem=true&from=h5
                        startActivity(new_intent);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        } else if (RomUtils.isVivo() && !TextUtils.isEmpty(mNote.getVivoFontId())) {
            mDownloadFont.setVisibility(View.VISIBLE);
            mDownloadFont.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String typeStr = "4";
                    if (noteType.equals("主题")) {
                        typeStr = "1";
                    }
                    Map<String, String> map = new HashMap<>();
                    map.put("type", "vivo");
                    map.put("name", mNote.getVivoFontId());
                    TrackerUtils.onEvent(mContext, "jump_theme_store", map);
                    try {
                        Intent new_intent = new Intent(Intent.ACTION_VIEW, Uri.parse(String.format("newthemedetail://newthemehost?pkg=com.bbk.theme&restype=%s&id=%s", typeStr, mNote.getVivoFontId())));
                        startActivity(new_intent);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        } else if (RomUtils.isEmui() && !TextUtils.isEmpty(mNote.getHuaweiFontId())) {
            mDownloadFont.setVisibility(View.VISIBLE);
            mDownloadFont.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Map<String, String> map = new HashMap<>();
                    map.put("type", "huawei");
                    map.put("name", mNote.getHuaweiFontId());
                    TrackerUtils.onEvent(mContext, "jump_theme_store", map);
                    try {
                        Intent new_intent = new Intent(Intent.ACTION_VIEW, Uri.parse(String.format("https://url.cloud.huawei.com/%s", mNote.getHuaweiFontId())));
                        startActivity(new_intent);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        } else if (RomUtils.isMiui() && !TextUtils.isEmpty(mNote.getXiaomiFontId())) {
            mDownloadFont.setVisibility(View.VISIBLE);
            mDownloadFont.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Map<String, String> map = new HashMap<>();
                    map.put("type", "xiaomi");
                    map.put("name", mNote.getXiaomiFontId());
                    TrackerUtils.onEvent(mContext, "jump_theme_store", map);
                    try {
                        Intent new_intent = new Intent(Intent.ACTION_VIEW, Uri.parse(String.format("http://zhuti.xiaomi.com/detail/%s", mNote.getXiaomiFontId())));
                        startActivity(new_intent);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        }
        if (!TextUtils.isEmpty(mNote.getIntro())) {
            mIntro.setVisibility(View.VISIBLE);
            mIntro.setText(mNote.getIntro());
        } else {
            mIntro.setVisibility(View.GONE);
        }
        mTime.setText(mNote.getUpdatedAt());
        if (noteUser.getAvatar() != null) {
            Glide.with(MyApplication.getInstance())
                    .load(noteUser.getAvatar())
                    .fitCenter()
                    .diskCacheStrategy(DiskCacheStrategy.RESULT)
                    .into(mUserIcon);
        }
        final User currentUser = BmobUser.getCurrentUser(User.class);
        mUserIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (noteUserId.equals(currentUser.getObjectId())) {

                } else {
                    Intent intent = new Intent(getApplicationContext(), UserActivity.class);
                    intent.putExtra("id", noteUserId);
                    startActivity(intent);
                }
            }
        });
        if (currentUser.getAvatar() != null) {
            Glide.with(MyApplication.getInstance())
                    .load(currentUser.getAvatar())
                    .fitCenter()
                    .diskCacheStrategy(DiskCacheStrategy.RESULT)
                    .into(mCommentIcon);
        }
        if (!mNote.getUser().getObjectId().equals(currentUser.getObjectId())) {
            mMoreAction.setVisibility(View.GONE);
            mFocusAction.setVisibility(View.VISIBLE);
            BmobQuery<User> userQuery = new BmobQuery<>();
            userQuery.addWhereEqualTo("objectId" , currentUser.getObjectId());
            userQuery.findObjects(new FindListener<User>() {
                @Override
                public void done(List<User> list, BmobException e) {
                    if (e == null) {
                        boolean follow = true;
                        if (list.size() == 1) {
                            User user = list.get(0);
                            ArrayList<String> relations = user.getFocusIds();
                            if (relations != null && relations.contains(noteUserId)) {
                                follow = false;
                            }
                        }
                        setFollowAction(follow);
                        mFocusAction.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                setFollow(noteUserId);
                            }
                        });
                    } else {
                        BackendUtils.handleException(e, getApplicationContext());
                    }
                }
            });
        } else {
            mMoreAction.setVisibility(View.VISIBLE);
            mFocusAction.setVisibility(View.GONE);
        }
        ArrayList<String> likeIds = mNote.getLikeIds();
        if (likeIds != null && likeIds.contains(currentUser.getObjectId())) {
            mToLike.setImageResource(R.drawable.ic_liked);
        } else {
            mToLike.setImageResource(R.drawable.ic_to_like);
        }
        if (likeIds != null && likeIds.size() > 0) {
            mLikeCount.setText(likeIds.size() + "");
        } else {
            mLikeCount.setText("赞");
        }
        mBtnLike.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ArrayList<String> likeIds = new ArrayList<>();
                final User currentUser = BmobUser.getCurrentUser(User.class);
                if (mNote.getLikeIds() != null) {
                    likeIds.addAll(mNote.getLikeIds());
                }
                final boolean isToAdd;
                if (!likeIds.contains(currentUser.getObjectId())) {
                    likeIds.add(currentUser.getObjectId());
                    isToAdd = true;
                    mToLike.setImageResource(R.drawable.ic_liked);
                } else {
                    isToAdd = false;
                    likeIds.remove(currentUser.getObjectId());
                    mToLike.setImageResource(R.drawable.ic_to_like);
                }
                if (likeIds.size() > 0) {
                    mLikeCount.setText(likeIds.size() + "");
                } else {
                    mLikeCount.setText("赞");
                }
                mNote.setLikeIds(likeIds);
                mNote.update(new UpdateListener() {
                    @Override
                    public void done(BmobException e) {
                        if (e == null) {
                            if (isToAdd) {
                                Map<String, Object> map = new HashMap<>();
                                map.put("noteId", mNote.getObjectId());
                                BackendUtils.pushMessage(getApplicationContext(), mNote.getUser(), "LIKE", map);
                            }
                        } else {
                            BackendUtils.handleException(e, getApplicationContext());
                        }
                    }
                });
            }
        });
        ArrayList<String> favoriteIds = mNote.getFavoriteIds();
        if (favoriteIds != null && favoriteIds.contains(currentUser.getObjectId())) {
            mToFavorite.setImageResource(R.drawable.ic_favorited);
        } else {
            mToFavorite.setImageResource(R.drawable.ic_to_favorite);
        }
        if (favoriteIds != null && favoriteIds.size() > 0) {
            mFavoriteCount.setText(favoriteIds.size() + "");
        } else {
            mFavoriteCount.setText("收藏");
        }
        mBtnFavorite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ArrayList<String> favoriteIds = new ArrayList<>();
                final User currentUser = BmobUser.getCurrentUser(User.class);
                if (mNote.getFavoriteIds() != null) {
                    favoriteIds.addAll(mNote.getFavoriteIds());
                }
                final boolean isToAdd;
                if (!favoriteIds.contains(currentUser.getObjectId())) {
                    favoriteIds.add(currentUser.getObjectId());
                    isToAdd = true;
                    mToFavorite.setImageResource(R.drawable.ic_favorited);
                } else {
                    favoriteIds.remove(currentUser.getObjectId());
                    isToAdd = false;
                    mToFavorite.setImageResource(R.drawable.ic_to_favorite);
                }
                if (favoriteIds.size() > 0) {
                    mFavoriteCount.setText(favoriteIds.size() + "");
                } else {
                    mFavoriteCount.setText("收藏");
                }
                mNote.setFavoriteIds(favoriteIds);
                mNote.update(new UpdateListener() {
                    @Override
                    public void done(BmobException e) {
                        if (e == null) {
                            if (isToAdd) {
                                Map<String, Object> map = new HashMap<>();
                                map.put("noteId", mNote.getObjectId());
                                BackendUtils.pushMessage(getApplicationContext(), mNote.getUser(), "FAVORITE", map);
                            }
                        } else {
                            BackendUtils.handleException(e, getApplicationContext());
                        }
                    }
                });
            }
        });
        mBtnMsg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showCommentDialog(mNote);
            }
        });
        mToComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showCommentDialog(mNote);
            }
        });
        mCommentView.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {
            @Override
            public boolean onGroupClick(ExpandableListView expandableListView, View view, int groupPosition, long l) {
                showReplyDialog(mNote, groupPosition, -1);
                return true;
            }
        });
        mCommentView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView expandableListView, View view, int i, int i1, long l) {
                showReplyDialog(mNote, i, i1);
                return false;
            }
        });
        mCommentAdapter = new CommentExpandAdapter(mContext);
        mCommentView.setAdapter(mCommentAdapter);
        mCommentView.setGroupIndicator(null);
        refreshCommentData();
        mActionComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showCommentDialog(mNote);
            }
        });
    }

    private void refreshCommentData() {
        BmobQuery<Comment> commentQuery = new BmobQuery<>();
        commentQuery.addWhereEqualTo("noteId", mNote.getObjectId());
        commentQuery.include("user");
        commentQuery.order("-createdAt");
        commentQuery.findObjects(new FindListener<Comment>() {
            @Override
            public void done(List<Comment> list, BmobException e) {
                if (e == null) {
                    if (list.size() > 0) {
                        mComments.clear();
                        mComments.addAll(list);
                        mCommentAdapter.setData(mComments);
                        mCommentAdapter.notifyDataSetChanged();
                        for(int i = 0; i < mComments.size(); i++){
                            mCommentView.expandGroup(i);
                        }
                        mNestedScrollView.scrollTo(0, 0);
                        int count = 0;
                        for(int i = 0; i < mComments.size(); i++){
                            ArrayList<Reply> replyIds = mComments.get(i).getReplyIds();
                            if (replyIds != null) {
                                count += replyIds.size();
                            }
                            count += 1;
                        }
                        mCommentTitle.setText("共" + count + "条评论");
                        mMsgCount.setText(count + "");
                    } else {
                        mCommentTitle.setVisibility(View.GONE);
                        mMsgCount.setText("评论");
                    }
                } else {
                    BackendUtils.handleException(e, getApplicationContext());
                }
            }
        });
    }
    private void showCommentDialog(final Note note) {
        final Dialog dialog = new Dialog(this, R.style.input_dialog);
        final View commentView = LayoutInflater.from(this).inflate(R.layout.comment_dialog_layout, null);
        Window dialogWindow = dialog.getWindow();
        dialogWindow.setGravity(Gravity.BOTTOM);
        dialog.setContentView(commentView);
        WindowManager.LayoutParams lp = dialogWindow.getAttributes();
        lp.width = (int) getResources().getDisplayMetrics().widthPixels;
        commentView.measure(0, 0);
        lp.height = commentView.getMeasuredHeight();
        lp.dimAmount = 0.75f;
        dialogWindow.setAttributes(lp);

        final AppCompatImageView mComment = (AppCompatImageView) commentView.findViewById(R.id.comment_bt);
        final AppCompatEditText mCommentContent = (AppCompatEditText) commentView.findViewById(R.id.comment_ed);
        mCommentContent.requestFocus();
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                InputMethodManager inputManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                inputManager.showSoftInput(mCommentContent, 0);
            }
        }, 100);


        mCommentContent.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (!TextUtils.isEmpty(charSequence)) {
                    mComment.setImageResource(R.drawable.ic_comment_send_enable);
                } else {
                    mComment.setImageResource(R.drawable.ic_comment_send_disable);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });

        mComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String commentContent = mCommentContent.getText().toString().trim();
                if (!TextUtils.isEmpty(commentContent)) {
                    dialog.dismiss();
                    Comment comment = new Comment();
                    comment.setContent(commentContent);
                    comment.setUser(BmobUser.getCurrentUser(User.class));
                    comment.setNoteId(note.getObjectId());
                    comment.save(new SaveListener<String>() {
                        @Override
                        public void done(String s, BmobException e) {
                            if (e == null) {
                                refreshCommentData();
                                Toast.makeText(getApplicationContext(), "评论成功", Toast.LENGTH_SHORT).show();
                                Map<String, Object> map = new HashMap<>();
                                map.put("noteId", mNote.getObjectId());
                                map.put("content", commentContent);
                                BackendUtils.pushMessage(getApplicationContext(), mNote.getUser(), "COMMENT", map);
                            } else {
                                BackendUtils.handleException(e, getApplicationContext());
                            }
                        }
                    });
                } else {
                    Toast.makeText(getApplicationContext(), "评论内容不能为空", Toast.LENGTH_SHORT).show();
                }
            }
        });
        dialog.show();
    }

    private void showReplyDialog(final Note note, final int position, final int subPosition) {
        final Dialog dialog = new Dialog(this, R.style.input_dialog);
        final View commentView = LayoutInflater.from(this).inflate(R.layout.comment_dialog_layout, null);
        Window dialogWindow = dialog.getWindow();
        dialogWindow.setGravity(Gravity.BOTTOM);
        dialog.setContentView(commentView);
        WindowManager.LayoutParams lp = dialogWindow.getAttributes();
        lp.width = (int) getResources().getDisplayMetrics().widthPixels;
        commentView.measure(0, 0);
        lp.height = commentView.getMeasuredHeight();
        lp.dimAmount = 0.75f;
        dialogWindow.setAttributes(lp);

        final AppCompatImageView mComment = (AppCompatImageView) commentView.findViewById(R.id.comment_bt);
        final AppCompatEditText mCommentContent = (AppCompatEditText) commentView.findViewById(R.id.comment_ed);
        mCommentContent.requestFocus();
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                InputMethodManager inputManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                inputManager.showSoftInput(mCommentContent, 0);
            }
        }, 100);
        if (subPosition == -1) {
            mCommentContent.setHint("回复" + mComments.get(position).getUser().getNickName() + "的评论:");
        } else {
            mCommentContent.setHint("回复" + mComments.get(position).getReplyIds().get(subPosition).getUser().getNickName() + "的评论:");
        }
        mCommentContent.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (!TextUtils.isEmpty(charSequence)) {
                    mComment.setImageResource(R.drawable.ic_comment_send_enable);
                } else {
                    mComment.setImageResource(R.drawable.ic_comment_send_disable);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });
        mComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String replyContent = mCommentContent.getText().toString().trim();
                if (!TextUtils.isEmpty(replyContent)) {
                    dialog.dismiss();
                    final Comment comment = mComments.get(position);
                    final Reply reply = new Reply();
                    reply.setUser(BmobUser.getCurrentUser(User.class));
                    if (subPosition != -1) {
                        reply.setReplyUser(comment.getReplyIds().get(subPosition).getUser());
                    }
                    reply.setContent(replyContent);
                    reply.save(new SaveListener<String>() {
                        @Override
                        public void done(String s, BmobException e) {
                            if (e == null) {
                                ArrayList<Reply> replyIds = comment.getReplyIds();
                                if (replyIds == null) {
                                    replyIds = new ArrayList<>();
                                }
                                replyIds.add(reply);
                                comment.setReplyIds(replyIds);
                                comment.update(new UpdateListener() {
                                    @Override
                                    public void done(BmobException e) {
                                        if (e == null) {
                                            refreshCommentData();
                                            Toast.makeText(getApplicationContext(), "评论成功", Toast.LENGTH_SHORT).show();
                                            Map<String, Object> map = new HashMap<>();
                                            map.put("noteId", mNote.getObjectId());
                                            map.put("content", replyContent);
                                            BackendUtils.pushMessage(getApplicationContext(), mNote.getUser(), "COMMENT", map);
                                        } else {
                                            BackendUtils.handleException(e, getApplicationContext());
                                        }
                                    }
                                });
                            } else {
                                BackendUtils.handleException(e, getApplicationContext());
                            }
                        }
                    });
                } else {
                    Toast.makeText(getApplicationContext(), "回复内容不能为空", Toast.LENGTH_SHORT).show();
                }
            }
        });
        dialog.show();
    }

    private void initMoreDialog(){
        mMoreDialog = new Dialog(this,R.style.dialog_bottom_full);
        mMoreDialog.setCanceledOnTouchOutside(true);
        mMoreDialog.setCancelable(true);
        Window window = mMoreDialog.getWindow();
        window.setGravity(Gravity.BOTTOM);
        window.setWindowAnimations(R.style.more_animation);
        View view = View.inflate(this, R.layout.dialog_more,null);
        view.findViewById(R.id.close).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mMoreDialog != null && mMoreDialog.isShowing()){
                    mMoreDialog.dismiss();
                }
            }
        });
        view.findViewById(R.id.more_edit).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AsyncTask<Void, Void, ArrayList<String>>() {
                    @Override
                    protected void onPreExecute() {
                        super.onPreExecute();
                        showProgressBar(true, "图片处理中...");
                    }
                    @Override
                    protected ArrayList<String> doInBackground(Void... params) {
                        ArrayList<String> urls = new ArrayList<>();
                        for (Fragment fragment : mFragments) {
                            Bitmap bitmap = ((DetailPicFragment) fragment).getBitmap();
                            File file = new File(FileUtils.getFileDir(mContext, "download"), System.currentTimeMillis() + ".png");
                            try {
                                bitmap.compress(Bitmap.CompressFormat.PNG, 80, new FileOutputStream(file));
                            } catch (FileNotFoundException e) {
                                e.printStackTrace();
                            }
                            urls.add(file.getAbsolutePath());
                        }
                        return urls;
                    }
                    @Override
                    protected void onPostExecute(ArrayList<String> urls) {
                        Intent intent = new Intent(getApplicationContext(), PublishActivity.class);
                        intent.putExtra("noteId", mNote.getObjectId());
                        intent.putStringArrayListExtra("urls", urls);
                        intent.putStringArrayListExtra("savedUrls", urls);
                        intent.putExtra("title", mNote.getTitle());
                        intent.putExtra("intro", mNote.getIntro());
                        intent.putExtra("type", mNote.getType());
                        startActivity(intent);
                        dismissMoreDialog();
                        showProgressBar(false);
                    }
                }.execute();
            }
        });
        view.findViewById(R.id.more_delete).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MiscUtils.showAskDialog(NoteDetailActivity.this, "确定要删除该条笔记？" , new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mNote.delete(new UpdateListener() {
                            @Override
                            public void done(BmobException e) {
                                if (e == null) {
                                    LocalBroadcastManager.getInstance(NoteDetailActivity.this).sendBroadcast(new Intent("android.intent.action.NOTE_LIST_CHANGE"));
                                    Toast.makeText(getApplicationContext(), "删除成功", Toast.LENGTH_SHORT).show();
                                    finish();
                                } else {
                                    BackendUtils.handleException(e, getApplicationContext());
                                }
                            }
                        });
                    }
                });
            }
        });
        window.setContentView(view);
        window.setLayout(WindowManager.LayoutParams.MATCH_PARENT,WindowManager.LayoutParams.WRAP_CONTENT);
    }

    private void setFollowAction(boolean follow) {
        if (follow) {
            mFocusAction.setText(R.string.follow);
            mFocusAction.setTextColor(mFollowedColor);
            mFocusAction.setBackgroundResource(R.drawable.ic_follow);
        } else {
            mFocusAction.setText(R.string.unfollow);
            mFocusAction.setTextColor(mFollowColor);
            mFocusAction.setBackgroundResource(R.drawable.ic_followed);
            BackendUtils.pushMessage(getApplicationContext(), mNote.getUser(), "FOLLOW", null);
        }
    }

    public void showMoreDialog(){
        if (mMoreDialog == null){
            initMoreDialog();
        }
        mMoreDialog.show();
    }

    public void dismissMoreDialog() {
        if (mMoreDialog != null && mMoreDialog.isShowing()) {
            mMoreDialog.dismiss();
        }
        mMoreDialog = null;
    }

    private void setFollow(final String noteUserId) {
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
                        if (user.getFocusIds() == null || !user.getFocusIds().contains(noteUserId)) {
                            focusIdList.add(noteUserId);
                            setFollowAction(false);
                        } else {
                            focusIdList.remove(noteUserId);
                            setFollowAction(true);
                        }
                        user.setFocusIds(focusIdList);
                        user.update(new UpdateListener() {
                            @Override
                            public void done(BmobException e) {
                            }
                        });
                    }
                } else {
                    BackendUtils.handleException(e, getApplicationContext());
                }
            }
        });
    }
}
