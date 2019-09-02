package com.hanmei.aafont.ui.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.hanmei.aafont.R;

import butterknife.BindView;
import butterknife.OnClick;

public class SpeakFragment extends BaseFragment {

    @BindView(R.id.speak_create)
    LinearLayout mCreateTv;
    @BindView(R.id.speak_push)
    LinearLayout mPushTv;
    @BindView(R.id.speak_browse)
    LinearLayout mBrowseTv;

    private Fragment mFragment;
    private CreateFragment mCreateFragment;
    private PushFragment mPushFragment;
    private BrowseFragment mBrowseFragment;

    @Override
    public View createMyView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_speak, container, false);
    }

    @Override
    public void init() {
        super.init();
        setDefaultFragment();
    }

    @OnClick({R.id.speak_create, R.id.speak_push, R.id.speak_browse})
    public void tableClick(View view) {
        switch (view.getId()) {
            case R.id.speak_create:
                if (mCreateFragment == null) {
                    mCreateFragment = new CreateFragment();
                }
                switchFragment(mCreateFragment);
                break;
            case R.id.speak_push:
                if (mPushFragment == null) {
                    mPushFragment = new PushFragment();
                }
                switchFragment(mPushFragment);
                break;
            case R.id.speak_browse:
                if (mBrowseFragment == null) {
                    mBrowseFragment = new BrowseFragment();
                }
                switchFragment(mBrowseFragment);
                break;
            default:
                break;

        }
    }

    private void switchFragment(Fragment fragment) {
        if (mFragment != fragment) {
            FragmentManager fm = getChildFragmentManager();
            FragmentTransaction transaction = fm.beginTransaction();
            if (!fragment.isAdded()) {
                transaction.hide(mFragment).add(R.id.my_speak_frame, fragment).commit();
            } else {
                transaction.hide(mFragment).show(fragment).commit();
            }
            mFragment = fragment;
        }
    }

    private void setDefaultFragment() {
        FragmentManager fm = getChildFragmentManager();
        FragmentTransaction transaction = fm.beginTransaction();
        mBrowseFragment = new BrowseFragment();
        transaction.replace(R.id.my_speak_frame, mBrowseFragment);
        transaction.commit();
        mFragment = mBrowseFragment;
    }
}
