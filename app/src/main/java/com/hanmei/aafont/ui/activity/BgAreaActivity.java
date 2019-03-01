package com.hanmei.aafont.ui.activity;

import android.os.Bundle;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.hanmei.aafont.R;
import com.hanmei.aafont.model.Background;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FindListener;

public class BgAreaActivity extends BaseActivity {
    @BindView(R.id.recycler_view)
    RecyclerView mRecyclerView;
    @BindView(R.id.to_my_bg)
    AppCompatImageView mToMyBg;

    private BgAdapter mBgAdapter;
    private List<Background> mBgs = new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Toolbar toolbar = findViewById(R.id.toolbar_set);
        toolbar.setTitle(R.string.bg_area);
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.drawable.back);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        final GridLayoutManager layoutManager = new GridLayoutManager(this, 3);
        mRecyclerView.setLayoutManager(layoutManager);
        mBgAdapter = new BgAdapter();
        BmobQuery<Background> query = new BmobQuery<>();
        query.order("-createdAt");
        query.findObjects(new FindListener<Background>() {
            @Override
            public void done(List<Background> list, BmobException e) {
                if (e == null) {
                    mBgs = list;
                    mRecyclerView.setAdapter(mBgAdapter);
                }
            }
        });
        mToMyBg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
    }

    @Override
    protected void setMyContentView() {
        setContentView(R.layout.activity_bg_area);
    }

    class BgAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            return new BgViewHolder(inflater.inflate(R.layout.item_bg_area, parent, false));
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
            if (holder instanceof BgViewHolder) {
                BgViewHolder viewHolder = (BgViewHolder)holder;
                AppCompatImageView preview = viewHolder.mPreview;
                final Background bg = mBgs.get(position);
                String url = bg.getContent().getUrl();
                Glide.with(holder.itemView.getContext())
                        .load(url)
                        .fitCenter()
                        .diskCacheStrategy(DiskCacheStrategy.RESULT)
                        .into(preview);
            }
        }

        @Override
        public int getItemCount() {
            return mBgs.size();
        }
    }

    class BgViewHolder extends RecyclerView.ViewHolder {
        public AppCompatImageView mPreview;

        public BgViewHolder(View itemView) {
            super(itemView);
            mPreview = (AppCompatImageView) itemView.findViewById(R.id.preview);
        }
    }
}
