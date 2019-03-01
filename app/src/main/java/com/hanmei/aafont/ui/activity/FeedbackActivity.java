package com.hanmei.aafont.ui.activity;

import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.hanmei.aafont.R;
import com.hanmei.aafont.utils.BackendUtils;

import butterknife.BindView;

public class FeedbackActivity extends BaseActivity {
    @BindView(R.id.feedback_content)
    EditText mContent;
    @BindView(R.id.btn_commit)
    Button mCommit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_set);
        toolbar.setTitle(R.string.help_feedback);
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.drawable.back);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        mCommit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String commit = mContent.getText().toString();
                if (TextUtils.isEmpty(commit)) {
                    Toast.makeText(getApplicationContext(),"不能提交空反馈!",Toast.LENGTH_SHORT).show();
                } else {
                    BackendUtils.saveFeedback(commit, new BackendUtils.DoneCallback() {
                        @Override
                        public void onDone(boolean success, int code) {
                            if (success) {
                                Toast.makeText(getApplicationContext(),"成功提交反馈!",Toast.LENGTH_SHORT).show();
                                finish();
                            }
                        }
                    });
                }
            }
        });
    }

    @Override
    protected void setMyContentView() {
        setContentView(R.layout.activity_feedback);
    }
}
