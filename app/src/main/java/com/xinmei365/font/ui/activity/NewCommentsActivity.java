package com.xinmei365.font.ui.activity;

import android.os.Bundle;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.xinmei365.font.R;
import com.xinmei365.font.ui.adapter.NewCommentAdapter;
import com.xinmei365.font.utils.DatabaseUtils;

import java.util.ArrayList;
import java.util.HashMap;

import butterknife.BindView;

public class NewCommentsActivity extends BaseActivity {
    @BindView(R.id.close)
    AppCompatImageView mClose;
    @BindView(R.id.title)
    AppCompatTextView mTitle;
    @BindView(R.id.recycler_view)
    RecyclerView mRecyclerView;

    private NewCommentAdapter mAdapter;

    @Override
    protected void setMyContentView() {
        setContentView(R.layout.activity_message_list);
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
        mTitle.setText("收到的评论");
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        mRecyclerView.setLayoutManager(linearLayoutManager);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.addItemDecoration(new DividerItemDecoration(this, linearLayoutManager.getOrientation()));
        mAdapter = new NewCommentAdapter(this);
        mRecyclerView.setAdapter(mAdapter);
        ArrayList<HashMap<String, String>> msgs = DatabaseUtils.queryAllMsg(getApplicationContext(), new String[]{"COMMENT"});
        mAdapter.setData(msgs);
    }
}

