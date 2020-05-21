package com.xinmei365.font.ui.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;

import com.xinmei365.font.ActivityCollector;
import com.xinmei365.font.R;
import com.xinmei365.font.utils.DensityUtils;
import com.xinmei365.font.utils.ToastUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import butterknife.ButterKnife;

public abstract class BaseActivity extends AppCompatActivity {
    private ProgressDialog mProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DensityUtils.setNewDensity(getResources().getDisplayMetrics());
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        getWindow().setStatusBarColor(getResources().getColor(R.color.white));
        ActivityCollector.addActivity(this);
        setMyContentView();
        ButterKnife.bind(this);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setIndeterminate(true);
        mProgressDialog.setCancelable(false);
    }

    protected abstract void setMyContentView();

    @Override
    protected void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onStop() {
        EventBus.getDefault().unregister(this);
        super.onStop();
    }

    @Subscribe
    public void onEvent(Boolean empty){
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ActivityCollector.removeActivity(this);
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
            mProgressDialog = null;
        }
        ToastUtils.cancelAllToast();
    }

    public void showMessage(String msg) {
        ToastUtils.show(msg, ToastUtils.Style.INFO);
    }

    public void showAlertMsg(String msg) {
        ToastUtils.show(msg, ToastUtils.Style.ALERT);
    }

    protected void showProgressBar(boolean show) {
        showProgressBar(show, "");
    }
    protected void showProgressBar(boolean show, String message) {
        if (mProgressDialog == null) {
            return;
        }
        if (show) {
            mProgressDialog.setMessage(message);
            mProgressDialog.show();
        } else {
            mProgressDialog.hide();
        }
    }

    public void hideSoftInputView() {
        InputMethodManager manager = ((InputMethodManager) this.getSystemService(Activity.INPUT_METHOD_SERVICE));
        if (getWindow().getAttributes().softInputMode != WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN) {
            if (getCurrentFocus() != null)
                manager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }
}
