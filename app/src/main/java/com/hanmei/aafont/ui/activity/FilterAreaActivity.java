package com.hanmei.aafont.ui.activity;

import android.os.Bundle;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.hanmei.aafont.R;
import com.hanmei.aafont.model.Filter;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FindListener;

public class FilterAreaActivity extends BaseActivity {
    @BindView(R.id.recycler_view)
    RecyclerView mRecyclerView;
    @BindView(R.id.to_my_filter)
    AppCompatImageView mToMyFilter;

    private FilterAdapter mFilterAdapter;
    private List<Filter> mFilters = new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Toolbar toolbar = findViewById(R.id.toolbar_set);
        toolbar.setTitle(R.string.filter_area);
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.drawable.back);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        mRecyclerView.setLayoutManager(linearLayoutManager);
        mFilterAdapter = new FilterAdapter();
        BmobQuery<Filter> query = new BmobQuery<>();
        query.order("-createdAt");
        query.findObjects(new FindListener<Filter>() {
            @Override
            public void done(List<Filter> list, BmobException e) {
                if (e == null) {
                    mFilters = list;
                    mRecyclerView.setAdapter(mFilterAdapter);
                }
            }
        });
        mToMyFilter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
    }

    @Override
    protected void setMyContentView() {
        setContentView(R.layout.activity_filter_area);
    }

    class FilterAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            return new FilterViewHolder(inflater.inflate(R.layout.item_filter_area, parent, false));
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
            if (holder instanceof FilterViewHolder) {
                FilterViewHolder viewHolder = (FilterViewHolder)holder;
                AppCompatTextView name = viewHolder.mName;
                AppCompatImageView preview = viewHolder.mPreview;
                final Filter font = mFilters.get(position);
                name.setText(font.getName());
                String url = font.getPreview().getUrl();
                Glide.with(holder.itemView.getContext())
                        .load(url)
                        .fitCenter()
                        .diskCacheStrategy(DiskCacheStrategy.RESULT)
                        .into(preview);
            }
        }

        @Override
        public int getItemCount() {
            return mFilters.size();
        }
    }

    class FilterViewHolder extends RecyclerView.ViewHolder {
        public AppCompatTextView mName;
        public AppCompatImageView mPreview;

        public FilterViewHolder(View itemView) {
            super(itemView);
            mName = (AppCompatTextView) itemView.findViewById(R.id.name);
            mPreview = (AppCompatImageView) itemView.findViewById(R.id.preview);
        }
    }
}
