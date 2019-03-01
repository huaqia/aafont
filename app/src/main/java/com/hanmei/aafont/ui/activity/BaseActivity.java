package com.hanmei.aafont.ui.activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.hanmei.aafont.ActivityCollector;

import butterknife.ButterKnife;

public abstract class BaseActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityCollector.addActivity(this);
        setMyContentView();
        ButterKnife.bind(this);
    }

    protected abstract void setMyContentView();

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ActivityCollector.removeActivity(this);
    }
}
