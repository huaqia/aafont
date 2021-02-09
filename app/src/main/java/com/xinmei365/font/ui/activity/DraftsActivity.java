package com.xinmei365.font.ui.activity;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SimpleItemAnimator;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.xinmei365.font.R;
import com.xinmei365.font.model.DraftData;
import com.xinmei365.font.ui.adapter.DraftAdapter;
import com.xinmei365.font.utils.FileUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

import butterknife.BindView;

public class DraftsActivity extends BaseActivity {
    @BindView(R.id.close)
    AppCompatImageView mClose;
    @BindView(R.id.recycler_view)
    RecyclerView mRecyclerView;

    private DraftAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        File jsonFile = new File(FileUtils.getFileDir(getApplicationContext(), "note"), "note.json");
        Gson gson = new Gson();
        ArrayList<DraftData> noteDatas;
        if (jsonFile.exists()) {
            String json = FileUtils.readFileToString(jsonFile);
            if (json != null) {
                noteDatas = gson.fromJson(json, new TypeToken<ArrayList<DraftData>>(){}.getType());
            } else {
                noteDatas = null;
            }
        } else {
            noteDatas = null;
        }
        if (noteDatas != null) {
            Collections.sort(noteDatas, new Comparator<DraftData>() {
                @Override
                public int compare(DraftData o1, DraftData o2) {
                    if (o2.getTime() != null && o1.getTime() != null) {
                        return o2.getTime().compareTo(o1.getTime());
                    } else {
                        return 0;
                    }
                }
            });
            final StaggeredGridLayoutManager layoutManager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
            layoutManager.setGapStrategy(StaggeredGridLayoutManager.GAP_HANDLING_NONE);
            mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                    super.onScrollStateChanged(recyclerView, newState);
                    if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                        layoutManager.invalidateSpanAssignments();
                        mRecyclerView.invalidateItemDecorations();
                    }
                }
            });
            mRecyclerView.getItemAnimator().setAddDuration(0);
            mRecyclerView.getItemAnimator().setChangeDuration(0);
            mRecyclerView.getItemAnimator().setMoveDuration(0);
            mRecyclerView.getItemAnimator().setRemoveDuration(0);
            ((SimpleItemAnimator)mRecyclerView.getItemAnimator()).setSupportsChangeAnimations(false);
            mRecyclerView.setLayoutManager(layoutManager);
            mAdapter = new DraftAdapter(DraftsActivity.this, 2, noteDatas);
            mRecyclerView.setAdapter(mAdapter);
        }
    }

    @Override
    protected void setMyContentView() {
        setContentView(R.layout.activity_draft);
    }
}