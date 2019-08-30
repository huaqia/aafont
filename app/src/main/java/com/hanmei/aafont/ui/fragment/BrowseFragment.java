package com.hanmei.aafont.ui.fragment;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.AppCompatTextView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.hanmei.aafont.R;
import com.hanmei.aafont.model.SpeakWord;
import com.hanmei.aafont.model.User;
import com.hanmei.aafont.ui.adapter.BrowsePageAdapter;
import com.hanmei.aafont.utils.BackendUtils;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.BmobUser;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FindListener;
import cn.bmob.v3.listener.UpdateListener;

public class BrowseFragment extends BaseFragment {

    private static final String TAG = "BrowseFragment";
    @BindView(R.id.left_browse)
    AppCompatImageView mLeft;
    @BindView(R.id.right_browse)
    AppCompatImageView mRight;
    @BindView(R.id.speak_browse_looper)
    ViewPager mSpeakViewPage;

    private ArrayList<SpeakWord> mSpeakList = new ArrayList<>();
    private List<String> mCardDataList = new ArrayList<>();
    private List<String> mUsernameDataList = new ArrayList<>();
    private ArrayList<ArrayList<String>> mLikeIdList = new ArrayList<>();

    @Override
    public View createMyView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_browse, container, false);
    }

    @Override
    public void init() {
        super.init();
        fetchData();
        mLeft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mSpeakViewPage.arrowScroll(View.FOCUS_LEFT);
            }
        });
        mRight.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View view) {
                mSpeakViewPage.arrowScroll(View.FOCUS_RIGHT);
            }
        });
    }
    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case 1:
                    setData(msg.obj);
                    break;
            }
        }
    };

    private void setData(final Object object){
        mSpeakList = (ArrayList<SpeakWord>) object;
        Log.e(TAG , mSpeakList.size() + "");
        for (int i = 0; i < mSpeakList.size(); i++) {
            mCardDataList.add(mSpeakList.get(i).getContent().getUrl());
            mUsernameDataList.add(mSpeakList.get(i).getUser().getUsername());
            mLikeIdList.add(mSpeakList.get(i).getLikeId());
        }
        BrowsePageAdapter browseAdapter = new BrowsePageAdapter(getActivity(), R.layout.item_speak_browse, mCardDataList,mUsernameDataList,mLikeIdList) {

            @Override
            public void bindView(View view, Object photo, Object name, Object likeId) {
                AppCompatImageView cardImage = view.findViewById(R.id.card_image);
                AppCompatTextView username = view.findViewById(R.id.browse_user_name);
                AppCompatImageView isLike = view.findViewById(R.id.browse_islike_img);

                Glide.with(view.getContext())
                        .load(photo)
                        .fitCenter()
                        .diskCacheStrategy(DiskCacheStrategy.RESULT)
                        .into(cardImage);
                username.setText(name.toString());
                final User currentUser = BmobUser.getCurrentUser(User.class);
                final ArrayList<String> bLikeId = (ArrayList<String>) likeId;
                if (bLikeId!= null && bLikeId.contains(currentUser.getObjectId())) {
                    isLike.setImageResource(R.drawable.liked);
                } else {
                    isLike.setImageResource(R.drawable.to_like);
                }
                isLike.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                    }
                });
            }
        };
        mSpeakViewPage.setAdapter(browseAdapter);
    }

    private void fetchData() {
        BmobQuery<SpeakWord> query = new BmobQuery<>();
        query.order("-createAt");
        query.include("user");
        query.findObjects(new FindListener<SpeakWord>() {
            @Override
            public void done(List<SpeakWord> list, BmobException e) {
                if (e == null) {
                    if (list.size() > 0) {
                        mSpeakList.clear();
                    }
                    Message msg = new Message();
                    msg.what = 1;
                    msg.obj = list;
                    mHandler.sendMessage(msg);
                } else {
                    Log.e(TAG, e.toString());
                }
            }
        });
    }
}
