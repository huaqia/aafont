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
import com.google.gson.Gson;
import com.xinmei365.font.MyApplication;
import com.xinmei365.font.R;
import com.xinmei365.font.model.DraftData;
import com.xinmei365.font.model.EffectData;
import com.xinmei365.font.ui.activity.PublishActivity;
import com.xinmei365.font.utils.BitmapUtils;
import com.xinmei365.font.utils.DensityUtils;
import com.xinmei365.font.utils.FileUtils;
import com.xinmei365.font.utils.MiscUtils;

import java.io.File;
import java.util.ArrayList;

public class DraftAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private Context mContext;
    private ArrayList<DraftData> mNoteDatas;
    private int mColumns;
    private int mParentWidth;
    private int mMargin;

    public DraftAdapter(Context context, int columns, ArrayList<DraftData> data) {
        mContext = context;
        mColumns = columns;
        mMargin = DensityUtils.dip2px(mContext, 3.33f);
        mNoteDatas = data;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        mParentWidth = parent.getWidth();
        return new DraftViewHolder(inflater.inflate(R.layout.item_draft, parent, false));
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position) {
        if (holder instanceof DraftViewHolder) {
            final DraftViewHolder viewHolder = (DraftViewHolder)holder;
            final DraftData noteData = mNoteDatas.get(position);
            final ArrayList<String> savedUrls = noteData.getSavedUrls();
            if (savedUrls != null && savedUrls.size() > 0) {
                String url = savedUrls.get(0);
                float firstRatio = BitmapUtils.getBitmapRatio(url);
                float limitRatio = (float)4 / 3;
                ViewGroup.LayoutParams params = (ViewGroup.LayoutParams) viewHolder.draft_pic.getLayoutParams();
                params.width = (mParentWidth - mMargin * 4) / mColumns;
                if (Float.compare(firstRatio, limitRatio) > 0) {
                    params.height = (int)(params.width * limitRatio);
                } else {
                    params.height = (int)(params.width * firstRatio);
                }
                viewHolder.draft_pic.setLayoutParams(params);
                Glide.with(MyApplication.getInstance())
                        .load(url)
                        .skipMemoryCache(true)
                        .diskCacheStrategy(DiskCacheStrategy.NONE)
                        .into(viewHolder.draft_pic);
            }
            String title = noteData.getTitle();
            if (TextUtils.isEmpty(title)) {
                title = noteData.getIntro();
            }
            if (TextUtils.isEmpty(title)) {
                viewHolder.draft_title.setVisibility(View.GONE);
            } else {
                viewHolder.draft_title.setVisibility(View.VISIBLE);
                viewHolder.draft_title.setText(title);
            }
            String time = noteData.getTime();
            if (time != null) {
                viewHolder.draft_time.setText(time);
            }
            viewHolder.to_delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    MiscUtils.showAskDialog(mContext, "确定要删除该草稿？" , new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            mNoteDatas.remove(position);
                            notifyDataSetChanged();
                            Gson gson = new Gson();
                            String json = gson.toJson(mNoteDatas);
                            File jsonFile = new File(FileUtils.getFileDir(mContext, "note"), "note.json");
                            FileUtils.saveStringToFile(json, jsonFile);
                        }
                    });
                }
            });
            final ArrayList<EffectData> effectDatas = (ArrayList<EffectData>)noteData.getEffectDatas();
            viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(mContext, PublishActivity.class);
                    intent.putStringArrayListExtra("urls", noteData.getUrls());
                    intent.putStringArrayListExtra("savedUrls", savedUrls);
                    intent.putExtra("effect", effectDatas);
                    intent.putExtra("title", noteData.getTitle());
                    intent.putExtra("intro", noteData.getIntro());
                    intent.putExtra("type", noteData.getType());
                    intent.putExtra("draftIndex", position);
                    mContext.startActivity(intent);
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return mNoteDatas.size();
    }

    static class DraftViewHolder extends RecyclerView.ViewHolder {
        private AppCompatImageView draft_pic;
        private AppCompatTextView draft_title;
        private AppCompatTextView draft_time;
        private AppCompatTextView to_delete;

        public DraftViewHolder(View itemView) {
            super(itemView);
            draft_pic = (AppCompatImageView) itemView.findViewById(R.id.draft_pic);
            draft_title = (AppCompatTextView) itemView.findViewById(R.id.draft_title);
            draft_time = (AppCompatTextView) itemView.findViewById(R.id.draft_time);
            to_delete = (AppCompatTextView) itemView.findViewById(R.id.to_delete);
        }
    }
}
