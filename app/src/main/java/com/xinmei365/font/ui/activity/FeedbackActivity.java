package com.xinmei365.font.ui.activity;

import android.os.Bundle;
import android.support.v7.widget.AppCompatEditText;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.AppCompatTextView;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.xinmei365.font.R;
import com.xinmei365.font.utils.BackendUtils;

import butterknife.BindView;

public class FeedbackActivity extends BaseActivity {
    @BindView(R.id.close)
    AppCompatImageView mClose;
    @BindView(R.id.feedback_content)
    AppCompatEditText mContent;
    @BindView(R.id.feedback_contact_info)
    AppCompatEditText mContactInfo;
    @BindView(R.id.comment_action)
    AppCompatTextView mCommit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        mCommit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String commit = mContent.getText().toString();
                String contactInfo = mContactInfo.getText().toString();
                if (TextUtils.isEmpty(commit)) {
                    Toast.makeText(getApplicationContext(),"不能提交空反馈!",Toast.LENGTH_SHORT).show();
                } else {
                    BackendUtils.saveFeedback(commit, contactInfo, new BackendUtils.DoneCallback() {
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
