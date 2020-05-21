package com.xinmei365.font.ui.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.umeng.analytics.MobclickAgent;
import com.xinmei365.font.ui.activity.BaseActivity;

import butterknife.ButterKnife;

public abstract class BaseFragment extends Fragment {
    BaseActivity baseActivity;

    @Override
    public void onResume() {
        super.onResume();
        MobclickAgent.onResume(getContext());
    }

    @Override
    public void onPause() {
        super.onPause();
        MobclickAgent.onPause(getContext());
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        baseActivity = (BaseActivity) context;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = createMyView(inflater, container, savedInstanceState);
        ButterKnife.bind(this, view);
        init();
        return view;
    }

    public abstract View createMyView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState);

    public void init() {
    }
}
