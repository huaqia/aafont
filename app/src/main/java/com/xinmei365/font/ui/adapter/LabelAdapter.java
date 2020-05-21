package com.xinmei365.font.ui.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.xinmei365.font.R;
import com.xinmei365.font.model.Label;
import com.xinmei365.font.utils.BackendUtils;

import java.util.List;

import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.UpdateListener;

public class LabelAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int TYPE_CHILD = 0;
    private static final int TYPE_FOOTER = 1;

    private Context mContext;
    private List<Label> mLabels;
    OnLabelClickListener mClickListener;

    public LabelAdapter(Context context) {
        mContext = context;
    }

    public void setData(List<Label> labels) {
        mLabels = labels;
        notifyDataSetChanged();
    }

    public void setClickListener(OnLabelClickListener clickListener) {
        mClickListener = clickListener;
    }
    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if (viewType == TYPE_CHILD) {
            return new LabelViewHolder(inflater.inflate(R.layout.item_label, parent, false));
        } else {
            return new FooterViewHolder(inflater.inflate(R.layout.item_label_footer, parent, false));
        }
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position) {
        if (holder instanceof LabelViewHolder) {
            final LabelViewHolder viewHolder = (LabelViewHolder) holder;
            final Label label = mLabels.get(position);
            final String name = label.getName();
            viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    label.setCount(label.getCount() + 1);
                    label.update(new UpdateListener() {
                        @Override
                        public void done(BmobException e) {
                            BackendUtils.handleException(e, mContext);
                        }
                    });
                    if (mClickListener != null) {
                        mClickListener.onClick(name);
                    }
                }
            });
            viewHolder.mLabelName.setText(name);
            final int usedCount = label.getCount();
            viewHolder.mLabelUsedCount.setText(String.format("%d个人使用过", usedCount));
        }
    }

    @Override
    public int getItemCount() {
        if (mLabels == null) {
            return 1;
        } else {
            return mLabels.size() + 1;
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (mLabels == null) {
            return TYPE_FOOTER;
        } else {
            if (position < mLabels.size()) {
                return TYPE_CHILD;
            } else {
                return TYPE_FOOTER;
            }
        }
    }

    static class LabelViewHolder extends RecyclerView.ViewHolder {
        public AppCompatTextView mLabelName;
        public AppCompatTextView mLabelUsedCount;

        public LabelViewHolder(View itemView) {
            super(itemView);
            mLabelName = (AppCompatTextView) itemView.findViewById(R.id.label_name);
            mLabelUsedCount = (AppCompatTextView) itemView.findViewById(R.id.label_use_count);
        }
    }

    public interface OnLabelClickListener {
        void onClick(String name);
    }

    class FooterViewHolder extends RecyclerView.ViewHolder {

        public FooterViewHolder(View itemView) {
            super(itemView);
        }
    }
}