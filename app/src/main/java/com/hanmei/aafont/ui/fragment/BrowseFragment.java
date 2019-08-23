package com.hanmei.aafont.ui.fragment;

import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.AppCompatImageView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.hanmei.aafont.R;
import com.hanmei.aafont.model.SpeakWord;
import com.hanmei.aafont.ui.adapter.BrowsePageAdapter;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FindListener;

public class BrowseFragment extends BaseFragment {

    private static final String TAG = "BrowseFragment";
    @BindView(R.id.left_browse)
    AppCompatImageView mLeft;
    @BindView(R.id.right_browse)
    AppCompatImageView mRight;
    @BindView(R.id.speak_browse_looper)
    ViewPager mSpeakViewPage;
    private ArrayList<SpeakWord> mSpeakList = new ArrayList<>();
    private List<String> mDataList = new ArrayList<>();

    @Override
    public View createMyView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_browse, container, false);
    }

    @Override
    public void init() {
        super.init();
        BmobQuery<SpeakWord> query = new BmobQuery<>();
        query.order("-createAt");
        query.findObjects(new FindListener<SpeakWord>() {
            @Override
            public void done(List<SpeakWord> list, BmobException e) {
                if (e == null) {
                    if (list.size() > 0) {
                        Log.e(TAG, list.size() + "mListSize");
                        mSpeakList.addAll(list);
                        Log.e(TAG, mSpeakList.size() + "mSpeakSize1");
                    }
                } else {
                    Log.e(TAG, e.toString());
                }
            }
        });
        for (int i = 0; i < mSpeakList.size(); i++) {
            mDataList.add(mSpeakList.get(i).getContent().getUrl());

        }
        BrowsePageAdapter<String> browseAdapter = new BrowsePageAdapter<String>(getActivity(), R.layout.item_speak_browse, mDataList) {
            @Override
            public void bindView(View view, String data) {
                AppCompatImageView cardImage = view.findViewById(R.id.card_image);
                Glide.with(view.getContext())
                        .load(data)
                        .fitCenter()
                        .diskCacheStrategy(DiskCacheStrategy.RESULT)
                        .into(cardImage);
            }
        };
        mSpeakViewPage.setAdapter(browseAdapter);

    }
}
