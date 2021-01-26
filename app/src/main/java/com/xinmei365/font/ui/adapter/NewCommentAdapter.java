package com.xinmei365.font.ui.adapter;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.xinmei365.font.MyApplication;
import com.xinmei365.font.R;
import com.xinmei365.font.model.Note;
import com.xinmei365.font.model.User;
import com.xinmei365.font.ui.activity.NoteDetailActivity;
import com.xinmei365.font.ui.activity.UserActivity;
import com.xinmei365.font.utils.BackendUtils;
import com.xinmei365.font.utils.DatabaseUtils;
import com.xinmei365.font.utils.MiscUtils;
import com.xinmei365.font.utils.TimeUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FindListener;
import de.hdodenhof.circleimageview.CircleImageView;

public class NewCommentAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private Context mContext;
    private ArrayList<HashMap<String, String>> mMsgs;

    public NewCommentAdapter(Context context) {
        mContext = context;
    }

    public void setData(ArrayList<HashMap<String, String>> msgs) {
        mMsgs = msgs;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        return new NewCommentViewHolder(inflater.inflate(R.layout.item_new_comment, parent, false));
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position) {
        if (holder instanceof NewCommentViewHolder) {
            final NewCommentViewHolder viewHolder = (NewCommentViewHolder)holder;
            final HashMap<String, String> msg = mMsgs.get(position);
            final String userId = msg.get("userId");
            if (userId != null) {
                BmobQuery<User> userQuery = new BmobQuery<>();
                userQuery.addWhereEqualTo("objectId", userId);
                userQuery.findObjects(new FindListener<User>() {
                    @Override
                    public void done(List<User> list, BmobException e) {
                        if (e == null) {
                            if (list.size() == 1) {
                                User user = list.get(0);
                                if (user.getAvatar() == null) {
                                    viewHolder.mUserIcon.setImageResource(R.drawable.avatar);
                                } else {
                                    Glide.with(MyApplication.getInstance())
                                            .load(user.getAvatar())
                                            .fitCenter()
                                            .diskCacheStrategy(DiskCacheStrategy.RESULT)
                                            .into(viewHolder.mUserIcon);
                                }
                                viewHolder.mUserName.setText(user.getNickName());
                            }
                        } else {
                            BackendUtils.handleException(e, mContext);
                        }
                    }
                });
            }
            String timeStr = msg.get("time");
            long time = -1;
            if (timeStr != null) {
                time = Long.parseLong(timeStr);
            }
            if (time != -1) {
                final String type = msg.get("type");
                if (type != null) {
                    viewHolder.mNotice.setText(" 评论了你的笔记 " + TimeUtils.getTime(time));
                }
            }
            HashMap<String, String> info = new Gson().fromJson(msg.get("extra"), new TypeToken<HashMap<String, String>>(){}.getType());
            if (info != null) {
                final String noteId = info.get("noteId");
                if (noteId != null) {
                    BmobQuery<Note> query = new BmobQuery<>();
                    query.addWhereEqualTo("objectId", noteId);
                    query.findObjects(new FindListener<Note>() {
                        @Override
                        public void done(List<Note> list, BmobException e) {
                            if (e == null) {
                                if (list.size() == 1) {
                                    final Note note = list.get(0);
                                    ArrayList<String> pics = note.getPics();
                                    if (pics != null) {
                                        Glide.with(MyApplication.getInstance())
                                                .load(pics.get(0))
                                                .fitCenter()
                                                .diskCacheStrategy(DiskCacheStrategy.RESULT)
                                                .into(viewHolder.mNoteIcon);
                                    }
                                    viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
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
                final String content = info.get("content");
                if (content != null) {
                    viewHolder.mContent.setText(content);
                }
            }
            viewHolder.mUserIcon.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(mContext, UserActivity.class);
                    intent.putExtra("id", userId);
                    mContext.startActivity(intent);
                }
            });
            viewHolder.mUserName.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(mContext, UserActivity.class);
                    intent.putExtra("id", userId);
                    mContext.startActivity(intent);
                }
            });
            String idStr = msg.get("id");
            if (idStr != null) {
                final int id = Integer.parseInt(idStr);
                viewHolder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View view) {
                        MiscUtils.showAskDialog(mContext, "确定要删除这个纪录？", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                mMsgs.remove(position);
                                DatabaseUtils.deleteMessage(mContext, id);
                                notifyDataSetChanged();
                            }
                        });
                        return false;
                    }
                });
            }
        }
    }

    @Override
    public int getItemCount() {
        if (mMsgs == null) {
            return 0;
        } else {
            return mMsgs.size();
        }
    }

    static class NewCommentViewHolder extends RecyclerView.ViewHolder {
        public CircleImageView mUserIcon;
        public AppCompatTextView mUserName;
        public AppCompatTextView mNotice;
        public AppCompatImageView mNoteIcon;
        public AppCompatTextView mContent;

        public NewCommentViewHolder(View itemView) {
            super(itemView);
            mUserIcon = (CircleImageView) itemView.findViewById(R.id.profile_image);
            mUserName = (AppCompatTextView) itemView.findViewById(R.id.user_name);
            mNotice = (AppCompatTextView) itemView.findViewById(R.id.notice);
            mNoteIcon = (AppCompatImageView) itemView.findViewById(R.id.note_icon);
            mContent = (AppCompatTextView) itemView.findViewById(R.id.content);
        }
    }
}
