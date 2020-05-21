package com.xinmei365.font.ui.adapter;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.xinmei365.font.R;
import com.xinmei365.font.filter.FilterItem;

import java.util.List;

public class EffectFilterAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    List<FilterItem> mFilters;
    OnClickListener mOnClickListener;
    private int mCurrentFilterIndex = 0;

    public EffectFilterAdapter(List<FilterItem> filters, OnClickListener listener) {
        mFilters = filters;
        mOnClickListener = listener;
    }

    public void setCurrentFilterIndex(int index) {
        mCurrentFilterIndex = index;
    }
    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        return new EffectFilterViewHolder(inflater.inflate(R.layout.item_edit_filter, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, final int position) {
        if (holder instanceof EffectFilterViewHolder) {
            EffectFilterViewHolder filterViewHolder = (EffectFilterViewHolder)holder;
            AppCompatTextView name = filterViewHolder.mName;
            final AppCompatImageView preview = filterViewHolder.mPreview;
            final FilterItem filter = mFilters.get(position);
            name.setText(filter.mName);
            preview.setImageResource(filter.mIcon);
            Context context = name.getContext();
            if (position == mCurrentFilterIndex) {
                filterViewHolder.mPreviewHighlight.setVisibility(View.VISIBLE);
                filterViewHolder.mName.setTextColor(context.getResources().getColor(R.color.colorActiveState));
            } else {
                filterViewHolder.mPreviewHighlight.setVisibility(View.GONE);
                filterViewHolder.mName.setTextColor(context.getResources().getColor(R.color.colorNormalState));
            }
            preview.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mCurrentFilterIndex = position;
                    notifyDataSetChanged();
                    if (mOnClickListener != null) {
                        mOnClickListener.onClick(position);
                    }
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return mFilters.size();
    }

    private static class EffectFilterViewHolder extends RecyclerView.ViewHolder {
        private AppCompatTextView mName;
        private AppCompatImageView mPreview;
        private AppCompatImageView mPreviewHighlight;

        public EffectFilterViewHolder(View itemView) {
            super(itemView);
            mName = (AppCompatTextView) itemView.findViewById(R.id.filter_name);
            mPreview = (AppCompatImageView) itemView.findViewById(R.id.filter_preview);
            mPreviewHighlight = (AppCompatImageView) itemView.findViewById(R.id.filter_preview_highlight);
        }
    }

    public interface OnClickListener {
        void onClick(int index);
    }
}
