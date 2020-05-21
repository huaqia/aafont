package com.xinmei365.font.ui.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.xinmei365.font.R;
import com.xinmei365.font.model.Note;
import com.xinmei365.font.model.User;
import com.xinmei365.font.ui.activity.NoteDetailActivity;
import com.xinmei365.font.utils.BackendUtils;
import com.xinmei365.font.utils.DensityUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.bmob.v3.BmobUser;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.UpdateListener;
import de.hdodenhof.circleimageview.CircleImageView;

public class NoteAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private Context mContext;
    private List<Note> mNotes;
    private int mColumns;
    private int mParentWidth;
    private int mMargin;

    public void setData(List<Note> notes) {
        mNotes = notes;
        notifyDataSetChanged();
    }

    public NoteAdapter(Context context, int columns) {
        mContext = context;
        mColumns = columns;
        mMargin = DensityUtils.dip2px(mContext, 3.33f);
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        mParentWidth = parent.getWidth();
        return new NoteViewHolder(inflater.inflate(R.layout.item_note, parent, false));
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position) {
        if (holder instanceof NoteViewHolder) {
            final NoteViewHolder viewHolder = (NoteViewHolder) holder;
//            viewHolder.setIsRecyclable(false);
            final Note note = mNotes.get(position);
            float firstRatio = Float.parseFloat(note.getFirstRatio());
            float limitRatio = (float)4 / 3;
            ViewGroup.LayoutParams params = (ViewGroup.LayoutParams) viewHolder.mNoteImage.getLayoutParams();
            params.width = (mParentWidth - mMargin * 4) / mColumns;
            if (Float.compare(firstRatio, limitRatio) > 0) {
                params.height = (int)(params.width * limitRatio);
            } else {
                params.height = (int)(params.width * firstRatio);
            }
            viewHolder.mNoteImage.setLayoutParams(params);
            final String url = note.getPics().get(0);
            Glide.with(mContext)
                    .load(url)
                    .asBitmap()
                    .into(new SimpleTarget<Bitmap>() {
                        @Override
                        public void onResourceReady(Bitmap bitmap, GlideAnimation<? super Bitmap> glideAnimation) {
                            viewHolder.mNoteImage.setImageBitmap(bitmap);
                        }
                    });
            User noteUser = note.getUser();
            if (noteUser.getAvatar() != null) {
                Glide.with(holder.itemView.getContext())
                        .load(noteUser.getAvatar())
                        .fitCenter()
                        .diskCacheStrategy(DiskCacheStrategy.RESULT)
                        .into(viewHolder.mUserIcon);
            }
            if (TextUtils.isEmpty(note.getTitle())) {
                if (TextUtils.isEmpty(note.getIntro())) {
                    viewHolder.mNoteTitle.setVisibility(View.GONE);
                } else {
                    viewHolder.mNoteTitle.setText(note.getIntro());
                }
            } else {
                viewHolder.mNoteTitle.setText(note.getTitle());
            }
            viewHolder.mUserName.setText(noteUser.getNickName());
//                viewHolder.mProductTime.setText(TimeUtils.getTimeFormatText(TimeUtils.getSimpleDateFormat(product.getCreatedAt())));
//            final int finalPosition = position;
            ArrayList<String> likeIdList = note.getLikeIds();
            final User currentUser = BmobUser.getCurrentUser(User.class);
            if (likeIdList != null && likeIdList.contains(currentUser.getObjectId())) {
                viewHolder.mToLike.setImageResource(R.drawable.ic_liked);
            } else {
                viewHolder.mToLike.setImageResource(R.drawable.ic_to_like);
            }
            if (likeIdList != null && likeIdList.size() > 0) {
                viewHolder.mLikeCount.setText(likeIdList.size() + "");
            } else {
                viewHolder.mLikeCount.setText("赞");
            }
            StaggeredGridLayoutManager.LayoutParams layoutParams = (StaggeredGridLayoutManager.LayoutParams) viewHolder.itemView.getLayoutParams();
            layoutParams.setMargins(15, 15, 15, 15);
            viewHolder.mToLike.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    ArrayList<String> likeIdList = new ArrayList<>();
                    final User currentUser = BmobUser.getCurrentUser(User.class);
                    if (note.getLikeIds() != null) {
                        likeIdList.addAll(note.getLikeIds());
                    }
                    if (!likeIdList.contains(currentUser.getObjectId())) {
                        likeIdList.add(currentUser.getObjectId());
                        viewHolder.mToLike.setImageResource(R.drawable.ic_liked);
                    } else {
                        likeIdList.remove(currentUser.getObjectId());
                        viewHolder.mToLike.setImageResource(R.drawable.ic_to_like);
                    }
                    if (likeIdList != null && likeIdList.size() > 0) {
                        viewHolder.mLikeCount.setText(likeIdList.size() + "");
                    } else {
                        viewHolder.mLikeCount.setText("赞");
                    }
                    Map<String, Object> map = new HashMap<>();
                    map.put("noteId", note.getObjectId());
                    BackendUtils.pushMessage(mContext, note.getUser(), "LIKE", map);
                    note.setLikeIds(likeIdList);
//                        mNotes.set(finalPosition, product);
//                        mAdapter.notifyItemChanged(position);
                    note.update(new UpdateListener() {
                        @Override
                        public void done(BmobException e) {
                            BackendUtils.handleException(e, mContext);
                        }
                    });
                }
            });
            viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(mContext, NoteDetailActivity.class);
                    intent.putExtra("note", note);
                    mContext.startActivity(intent);
                }
            });
        }
    }

    @Override
    public int getItemViewType(int position) {
        return -1;
    }

    @Override
    public int getItemCount() {
        if (mNotes == null) {
            return 0;
        } else {
            return mNotes.size();
        }
    }

    static class NoteViewHolder extends RecyclerView.ViewHolder {
        public AppCompatImageView mNoteImage;
        public AppCompatTextView mNoteTitle;
        public CircleImageView mUserIcon;
        public AppCompatTextView mUserName;
        public AppCompatImageView mToLike;
        public AppCompatTextView mLikeCount;

        public NoteViewHolder(View itemView) {
            super(itemView);
            mNoteImage = (AppCompatImageView) itemView.findViewById(R.id.note_pic);
            mNoteTitle = (AppCompatTextView) itemView.findViewById(R.id.note_title);
            mUserIcon = (CircleImageView) itemView.findViewById(R.id.profile_image);
            mUserName = (AppCompatTextView) itemView.findViewById(R.id.user_name);
            mToLike = (AppCompatImageView) itemView.findViewById(R.id.to_like);
            mLikeCount = (AppCompatTextView) itemView.findViewById(R.id.like_count);
        }
    }
}

