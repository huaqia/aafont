package com.hanmei.aafont.ui.activity;

import android.os.Bundle;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.hanmei.aafont.R;
import com.hanmei.aafont.model.Product;
import com.hanmei.aafont.model.User;

import java.io.File;

import butterknife.BindView;
import cn.bmob.v3.BmobUser;
import cn.bmob.v3.datatype.BmobFile;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.SaveListener;
import cn.bmob.v3.listener.UpdateListener;
import cn.bmob.v3.listener.UploadFileListener;

public class PublishActivity extends BaseActivity {
    @BindView(R.id.last_pic)
    AppCompatImageView mLastPic;

    @BindView(R.id.only_self_switch)
    RadioButton mOnlySelfSwitch;

    @BindView(R.id.btn_publish_picture)
    Button mPublishPicture;

    private boolean mOnlySelfSwitchChecked;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_set);
        toolbar.setTitle(R.string.publish_picture);
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.drawable.back);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        final String url = getIntent().getExtras().getString("url");
        Glide.with(getApplicationContext())
                .load(url)
                .fitCenter()
                .diskCacheStrategy(DiskCacheStrategy.RESULT)
                .into(mLastPic);
        mOnlySelfSwitchChecked = true;
        mOnlySelfSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mOnlySelfSwitchChecked) {
                    mOnlySelfSwitch.setChecked(false);
                    mOnlySelfSwitchChecked = false;
                } else {
                    mOnlySelfSwitch.setChecked(true);
                    mOnlySelfSwitchChecked = true;
                }
            }
        });
        mPublishPicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final BmobFile bf = new BmobFile(new File(url));
                bf.uploadblock(new UploadFileListener() {
                    @Override
                    public void done(BmobException e) {
                        Product product = new Product();
                        product.setUser(BmobUser.getCurrentUser(User.class));
                        product.setContent(bf);
                        product.save(new SaveListener<String>() {
                            @Override
                            public void done(String s, BmobException e) {

                            }
                        });
                    }
                });
            }
        });
    }

    @Override
    protected void setMyContentView() {
        setContentView(R.layout.activity_publish);
    }
}
