package com.xinmei365.font.ui.activity;

import android.os.Build;
import android.os.Bundle;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.AppCompatTextView;
import android.view.View;

import com.xinmei365.font.BuildConfig;
import com.xinmei365.font.R;

import butterknife.BindView;

public class AboutActivity extends BaseActivity {
    @BindView(R.id.close)
    AppCompatImageView mClose;
    @BindView(R.id.version)
    AppCompatTextView mVersion;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mVersion.setText("v" + BuildConfig.VERSION_NAME);
        mClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    @Override
    protected void setMyContentView() {
        setContentView(R.layout.activity_about);
    }
}
