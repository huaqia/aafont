package com.xinmei365.font.ui.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.listener.OnLoadmoreListener;
import com.scwang.smartrefresh.layout.listener.OnRefreshListener;
import com.xinmei365.font.R;
import com.xinmei365.font.model.Type;
import com.xinmei365.font.utils.BackendUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import butterknife.BindView;
import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.datatype.BmobDate;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FindListener;
import cn.bmob.v3.listener.UpdateListener;

public class TypeSelectActivity extends BaseActivity {
    private static final int PULL_REFRESH = 0;
    private static final int LOAD_MORE = 1;

    private static final int PAGE_LIMIT = 10;

    @BindView(R.id.image_select_close)
    AppCompatImageView mClose;
    @BindView(R.id.recycler_view)
    RecyclerView mRecyclerView;
    @BindView(R.id.swipeLayout_follow)
    SmartRefreshLayout mSwipeRefreshLayout;

    private List<Type> mTypes = new ArrayList<>();
    private String mLastTime;
    TypeAdapter mAdapter;

    @Override
    protected void setMyContentView() {
        setContentView(R.layout.activity_type_select);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        mRecyclerView.setLayoutManager(linearLayoutManager);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.addItemDecoration(new DividerItemDecoration(this, linearLayoutManager.getOrientation()));
        mAdapter = new TypeAdapter();
        mRecyclerView.setAdapter(mAdapter);
        mSwipeRefreshLayout.autoRefresh();
        mSwipeRefreshLayout.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh(RefreshLayout refreshlayout) {
                fetchData(PULL_REFRESH);
            }
        });
        mSwipeRefreshLayout.setOnLoadmoreListener(new OnLoadmoreListener() {
            @Override
            public void onLoadmore(RefreshLayout refreshlayout) {
                fetchData(LOAD_MORE);
            }
        });
    }

    private void fetchData(final int type) {
        BmobQuery<Type> query = new BmobQuery<>();
        query.order("-createdAt");
        query.setLimit(PAGE_LIMIT);
        if (type == LOAD_MORE && mLastTime != null) {
            Date date = null;
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            try {
                date = dateFormat.parse(mLastTime);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            if (date != null) {
                query.addWhereLessThanOrEqualTo("createdAt", new BmobDate(date));
            }
        }
        query.findObjects(new FindListener<Type>() {
            @Override
            public void done(List<Type> list, BmobException e) {
                if (e == null) {
                    if (type == PULL_REFRESH) {
                        mTypes.clear();
                    }
                    if (list.size() > 0) {
                        mTypes.addAll(list);
                        if (list.size() < PAGE_LIMIT) {
                            mSwipeRefreshLayout.setEnableLoadmore(false);
                        } else {
                            mSwipeRefreshLayout.setEnableLoadmore(true);
                        }
                        mAdapter.setData(mTypes);
                        mLastTime = list.get(list.size() - 1).getCreatedAt();
                    } else {
                        mSwipeRefreshLayout.setEnableLoadmore(false);
                        mAdapter.notifyDataSetChanged();
                    }
                } else {
                    BackendUtils.handleException(e, TypeSelectActivity.this);
                }
                if (type == PULL_REFRESH) {
                    mSwipeRefreshLayout.finishRefresh();
                } else {
                    mSwipeRefreshLayout.finishLoadmore();
                }
            }
        });
    }

    public class TypeAdapter extends RecyclerView.Adapter<TypeAdapter.TypeHolder> {
        private List<Type> mTypes;

        public void setData(List<Type> types) {
            mTypes = types;
            mAdapter.notifyDataSetChanged();
        }

        @NonNull
        @Override
        public TypeHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            return new TypeHolder(inflater.inflate(R.layout.item_type_select, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull TypeHolder holder, @SuppressLint("RecyclerView") final int position) {
            final Type type = mTypes.get(position);
            String typeName = type.getName();
            holder.mTypeName.setText(typeName);
            if (typeName.equals("壁纸")) {
                holder.mTypeLeading.setImageResource(R.drawable.ic_type_select_wp);
            } else if (typeName.equals("字体")) {
                holder.mTypeLeading.setImageResource(R.drawable.ic_type_select_font);
            } else if (typeName.equals("主题")) {
                holder.mTypeLeading.setImageResource(R.drawable.ic_type_select_theme);
            }
            final int count = type.getCount();
            holder.mUseCount.setText(String.format("%d个人标记过", count));
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent();
                    intent.putExtra("type", type.getName());
                    setResult(RESULT_OK, intent);
                    finish();
                    type.setCount(count + 1);
                    type.update(new UpdateListener() {
                        @Override
                        public void done(BmobException e) {
                            BackendUtils.handleException(e, TypeSelectActivity.this);
                        }
                    });
                }
            });
        }

        @Override
        public int getItemCount() {
            if (mTypes == null) {
                return 0;
            } else {
                return mTypes.size();
            }
        }

        class TypeHolder extends RecyclerView.ViewHolder {
            public AppCompatImageView mTypeLeading;
            public AppCompatTextView mTypeName;
            public AppCompatTextView mUseCount;

            public TypeHolder(View view) {
                super(view);
                mTypeLeading = (AppCompatImageView) view.findViewById(R.id.type_leading);
                mTypeName = (AppCompatTextView) view.findViewById(R.id.type_name);
                mUseCount = (AppCompatTextView) view.findViewById(R.id.use_count);
            }
        }
    }
}
