package com.hanmei.aafont.ui.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.hanmei.aafont.R;
import com.hanmei.aafont.filter.FilterItem;

import java.util.List;

public class FilterAdapter extends RecyclerView.Adapter<FilterAdapter.FilterHolder> {

    private List<FilterItem> filters;
    private Context context;
    private int selected = 0;

    public FilterAdapter(Context context, List<FilterItem> filters) {
        this.filters = filters;
        this.context = context;
    }

    @Override
    public FilterHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_filter,
                parent, false);
        FilterHolder viewHolder = new FilterHolder(view);
        viewHolder.filterName = (TextView) view
                .findViewById(R.id.filter_thumb_name);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(FilterHolder holder, final int position) {
        final FilterItem filterItem = filters.get(position);
        holder.filterName.setText(filterItem.mName);

        if (position == selected) {
            holder.filterName.setTextColor(context.getResources().getColor(R.color.divider_color));
        } else {
            holder.filterName.setTextColor(context.getResources().getColor(R.color.white));
        }

        holder.filterName.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (selected == position) {
                    onFilterChangeListener.onFilterChanged(filterItem);
                    return;
                }
                int lastSelected = selected;
                selected = position;
                notifyItemChanged(lastSelected);
                notifyItemChanged(position);
                onFilterChangeListener.onFilterChanged(filterItem);
            }
        });
    }

    @Override
    public int getItemCount() {
        return filters == null ? 0 : filters.size();
    }

    class FilterHolder extends RecyclerView.ViewHolder {
        TextView filterName;

        public FilterHolder(View itemView) {
            super(itemView);
        }
    }

    public interface onFilterChangeListener {
        void onFilterChanged(FilterItem filterItem);
    }

    private onFilterChangeListener onFilterChangeListener;

    public void setOnFilterChangeListener(onFilterChangeListener onFilterChangeListener) {
        this.onFilterChangeListener = onFilterChangeListener;
    }
}
