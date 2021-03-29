package com.xinmei365.font.ui.adapter;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.xinmei365.font.MyApplication;
import com.xinmei365.font.R;
import com.xinmei365.font.model.Note;
import com.xinmei365.font.model.User;
import com.xinmei365.font.ui.activity.NoteDetailActivity;
import com.xinmei365.font.ui.activity.UserActivity;
import com.xinmei365.font.utils.BackendUtils;
import com.xinmei365.font.utils.MiscUtils;

import java.util.ArrayList;
import java.util.List;

import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.BmobUser;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FindListener;
import cn.bmob.v3.listener.UpdateListener;
import de.hdodenhof.circleimageview.CircleImageView;

public class RecommendAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final String TAG = "RecommendAdapter";
    public static final int TYPE_NORMAL = 0;
    public static final int TYPE_HEADER = 1;

    private Context mContext;
    private List<User> mUsers;
    private int mFollowColor;
    private int mFollowedColor;
    private View mHeaderView;

    public void setData(List<User> users) {
        mUsers = users;
        notifyDataSetChanged();
    }

    public RecommendAdapter(Context context) {
        mContext = context;
        mFollowColor = context.getResources().getColor(R.color.colorNormalState);
        mFollowedColor = context.getResources().getColor(R.color.colorActiveState);
    }

    public View getHeaderView() {
        return mHeaderView;
    }

    public void setHeaderView(View headerView) {
        mHeaderView = headerView;
        notifyItemInserted(0);
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (mHeaderView != null && viewType == TYPE_HEADER) {
            return new Holder(mHeaderView);
        }
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        return new RecommmendViewHolder(inflater.inflate(R.layout.item_recommend, parent, false));
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position) {
        if(getItemViewType(position) == TYPE_HEADER) {
            return;
        }
        final int pos = getRealPosition(holder);
        if (holder instanceof RecommmendViewHolder) {
            final RecommmendViewHolder viewHolder = (RecommmendViewHolder)holder;
            final User user = mUsers.get(pos);
            final String userId = user.getObjectId();
            if (user.getAvatar() != null) {
                Glide.with(MyApplication.getInstance())
                        .load(user.getAvatar())
                        .fitCenter()
                        .diskCacheStrategy(DiskCacheStrategy.RESULT)
                        .into(viewHolder.mUserIcon);
            }
            viewHolder.mUserName.setText(user.getNickName());
            viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(mContext, UserActivity.class);
                    intent.putExtra("id", userId);
                    mContext.startActivity(intent);
                }
            });
            viewHolder.mFocusAction.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    final User currentUser = BmobUser.getCurrentUser(User.class);
                    BmobQuery<User> query = new BmobQuery<>();
                    query.addWhereEqualTo("objectId" , currentUser.getObjectId());
                    query.findObjects(new FindListener<User>() {
                        @Override
                        public void done(List<User> list, BmobException e) {
                            if (e == null) {
                                final ArrayList<String> focusIdList = new ArrayList<>();
                                if (list.size() == 1) {
                                    final User user = list.get(0);
                                    if (user.getFocusIds() != null) {
                                        focusIdList.addAll(user.getFocusIds());
                                    }
                                    if (user.getFocusIds() == null || !user.getFocusIds().contains(userId)) {
                                        focusIdList.add(userId);
                                        setFollowAction(viewHolder.mFocusAction, false, user);
                                        BackendUtils.pushMessage(mContext, user, "FOLLOW", null);
                                        user.setFocusIds(focusIdList);
                                        user.update(new UpdateListener() {
                                            @Override
                                            public void done(BmobException e) {
                                                BackendUtils.handleException(e, mContext);
                                            }
                                        });
                                    } else {
                                        MiscUtils.showAskDialog(mContext, "确定不再关注这位了吗？" , new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                focusIdList.remove(userId);
                                                setFollowAction(viewHolder.mFocusAction, true, user);
                                                BackendUtils.pushMessage(mContext, user, "FOLLOW", null);
                                                user.setFocusIds(focusIdList);
                                                user.update(new UpdateListener() {
                                                    @Override
                                                    public void done(BmobException e) {
                                                        BackendUtils.handleException(e, mContext);
                                                    }
                                                });
                                            }
                                        });
                                    }
                                }
                            } else {
                                BackendUtils.handleException(e, mContext);
                            }
                        }
                    });
                }
            });
            fetchNotes(user.getObjectId(), viewHolder);
            if (pos == 0) {
                viewHolder.gang.setVisibility(View.GONE);
            } else {
                viewHolder.gang.setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (mHeaderView == null) {
            return TYPE_NORMAL;
        }
        if (position == 0) {
            return TYPE_HEADER;
        }
        return TYPE_NORMAL;
    }

    public int getRealPosition(RecyclerView.ViewHolder holder) {
        int position = holder.getLayoutPosition();
        return mHeaderView == null ? position : position - 1;
    }

    @Override
    public int getItemCount() {
        if (mUsers == null) {
            if (mHeaderView == null) {
                return 0;
            } else {
                return 1;
            }
        } else {
            if (mHeaderView == null) {
                return mUsers.size();
            } else {
                return mUsers.size() + 1;
            }
        }
    }

    private void fetchNotes(final String userId, final RecommmendViewHolder viewHolder) {
        BmobQuery<Note> query = new BmobQuery<>();
        query.include("user");
        query.addWhereEqualTo("userId", userId);
        query.order("-hot");
        query.setLimit(3);
        query.findObjects(new FindListener<Note>() {
            @Override
            public void done(List<Note> list, BmobException e) {
                if (e == null) {
                    if (list.size() > 0) {
                        final Note note = list.get(0);
                        final String url = note.getPics().get(0);
                        Glide.with(MyApplication.getInstance())
                                .load(url)
                                .centerCrop()
                                .diskCacheStrategy(DiskCacheStrategy.RESULT)
                                .into(viewHolder.mNoteLeftImage);
                        if (TextUtils.isEmpty(note.getTitle())) {
                            if (TextUtils.isEmpty(note.getIntro())) {
                                viewHolder.mNoteLeftTitle.setVisibility(View.GONE);
                            } else {
                                viewHolder.mNoteLeftTitle.setText(note.getIntro());
                            }
                        } else {
                            viewHolder.mNoteLeftTitle.setText(note.getTitle());
                        }
                        viewHolder.mNoteLeftImage.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Intent intent = new Intent(mContext, NoteDetailActivity.class);
                                intent.putExtra("note", note);
                                mContext.startActivity(intent);
                            }
                        });
                        viewHolder.mNoteLeftTitle.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Intent intent = new Intent(mContext, NoteDetailActivity.class);
                                intent.putExtra("note", note);
                                mContext.startActivity(intent);
                            }
                        });
                    }
                    if (list.size() > 1) {
                        final Note note = list.get(1);
                        final String url = note.getPics().get(0);
                        Glide.with(MyApplication.getInstance())
                                .load(url)
                                .centerCrop()
                                .diskCacheStrategy(DiskCacheStrategy.RESULT)
                                .into(viewHolder.mNoteMiddleImage);
                        if (TextUtils.isEmpty(note.getTitle())) {
                            if (TextUtils.isEmpty(note.getIntro())) {
                                viewHolder.mNoteMiddleTitle.setVisibility(View.GONE);
                            } else {
                                viewHolder.mNoteMiddleTitle.setText(note.getIntro());
                            }
                        } else {
                            viewHolder.mNoteMiddleTitle.setText(note.getTitle());
                        }
                        viewHolder.mNoteMiddleImage.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Intent intent = new Intent(mContext, NoteDetailActivity.class);
                                intent.putExtra("note", note);
                                mContext.startActivity(intent);
                            }
                        });
                        viewHolder.mNoteMiddleTitle.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Intent intent = new Intent(mContext, NoteDetailActivity.class);
                                intent.putExtra("note", note);
                                mContext.startActivity(intent);
                            }
                        });
                    }
                    if (list.size() > 2) {
                        final Note note = list.get(2);
                        final String url = note.getPics().get(0);
                        Glide.with(MyApplication.getInstance())
                                .load(url)
                                .centerCrop()
                                .diskCacheStrategy(DiskCacheStrategy.RESULT)
                                .into(viewHolder.mNoteRightImage);
                        if (TextUtils.isEmpty(note.getTitle())) {
                            if (TextUtils.isEmpty(note.getIntro())) {
                                viewHolder.mNoteRightTitle.setVisibility(View.GONE);
                            } else {
                                viewHolder.mNoteRightTitle.setText(note.getIntro());
                            }
                        } else {
                            viewHolder.mNoteRightTitle.setText(note.getTitle());
                        }
                        viewHolder.mNoteRightImage.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Intent intent = new Intent(mContext, NoteDetailActivity.class);
                                intent.putExtra("note", note);
                                mContext.startActivity(intent);
                            }
                        });
                        viewHolder.mNoteRightTitle.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Intent intent = new Intent(mContext, NoteDetailActivity.class);
                                intent.putExtra("note", note);
                                mContext.startActivity(intent);
                            }
                        });
                    }
                } else {
                    BackendUtils.handleException(e, mContext);
                }
            }
        });
    }

    private void setFollowAction(AppCompatTextView view, boolean follow, User user) {
        if (follow) {
            view.setText(R.string.follow);
            view.setTextColor(mFollowedColor);
            view.setBackgroundResource(R.drawable.ic_follow);
        } else {
            view.setText(R.string.unfollow);
            view.setTextColor(mFollowColor);
            view.setBackgroundResource(R.drawable.ic_followed);
            BackendUtils.pushMessage(mContext, user, "FOLLOW", null);
        }
    }

    static class Holder extends RecyclerView.ViewHolder {
        public Holder(View itemView) {
            super(itemView);
        }
    }

    static class RecommmendViewHolder extends RecyclerView.ViewHolder {
        private View gang;
        public CircleImageView mUserIcon;
        public AppCompatTextView mUserName;
        public AppCompatTextView mFocusAction;
        public AppCompatImageView mNoteLeftImage;
        public AppCompatTextView mNoteLeftTitle;
        public AppCompatImageView mNoteMiddleImage;
        public AppCompatTextView mNoteMiddleTitle;
        public AppCompatImageView mNoteRightImage;
        public AppCompatTextView mNoteRightTitle;
        public RecommmendViewHolder(View itemView) {
            super(itemView);
            gang = (View) itemView.findViewById(R.id.gang);
            mUserIcon = (CircleImageView) itemView.findViewById(R.id.user_icon);
            mUserName = (AppCompatTextView) itemView.findViewById(R.id.user_name);
            mFocusAction = (AppCompatTextView) itemView.findViewById(R.id.focus_action);
            mNoteLeftImage = (AppCompatImageView) itemView.findViewById(R.id.note_left_image);
            mNoteLeftTitle = (AppCompatTextView) itemView.findViewById(R.id.note_left_title);
            mNoteMiddleImage = (AppCompatImageView) itemView.findViewById(R.id.note_middle_image);
            mNoteMiddleTitle = (AppCompatTextView) itemView.findViewById(R.id.note_middle_title);
            mNoteRightImage = (AppCompatImageView) itemView.findViewById(R.id.note_right_image);
            mNoteRightTitle = (AppCompatTextView) itemView.findViewById(R.id.note_right_title);
        }
    }
}
