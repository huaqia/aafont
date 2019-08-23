package com.hanmei.aafont.ui.fragment;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.AppCompatImageView;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.hanmei.aafont.R;
import com.hanmei.aafont.model.DailyFont;
import com.hanmei.aafont.model.Product;
import com.hanmei.aafont.utils.FileUtils;
import com.hanmei.aafont.utils.RomUtils;

import java.io.File;
import java.util.List;

import butterknife.BindView;
import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.datatype.BmobFile;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.DownloadFileListener;
import cn.bmob.v3.listener.FindListener;

public class FontFragment extends BaseFragment {
    @BindView(R.id.font_pic)
    AppCompatImageView mFontPic;
    @BindView(R.id.left_arrow)
    AppCompatImageView mLeftArrow;
    @BindView(R.id.right_arrow)
    AppCompatImageView mRightArrow;
    @BindView(R.id.handle_action)
    LinearLayout mHandleAction;
    @BindView(R.id.save_pic)
    AppCompatButton mSavePic;
    @BindView(R.id.goto_store)
    AppCompatButton mGotoStore;

    private List<DailyFont> mList;

    private int mCurentIdx;

    @Override
    public View createMyView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_font, container, false);
    }

    @Override
    public void init() {
        super.init();
        BmobQuery<DailyFont> query = new BmobQuery<>();
        query.order("-createdAt");
        query.findObjects(new FindListener<DailyFont>() {
            @Override
            public void done(List<DailyFont> list, BmobException e) {
                if (e == null) {
                    if (list.size() > 0) {
                        mHandleAction.setVisibility(View.VISIBLE);
                        mList = list;
                        mCurentIdx = 0;
                        handleData();
                        mLeftArrow.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                if (mCurentIdx > 0) {
                                    mCurentIdx -= 1;
                                }
                                handleData();
                            }
                        });
                        mRightArrow.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                if (mCurentIdx < mList.size() - 1) {
                                    mCurentIdx += 1;
                                }
                                handleData();
                            }
                        });
                        mSavePic.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                BmobFile file = mList.get(mCurentIdx).getContent();
                                File saveFile = new File(FileUtils.getFileDir(getContext(), "daily_font"), file.getFilename());
                                file.download(saveFile, new DownloadFileListener() {
                                    @Override
                                    public void done(String s, BmobException e) {
                                        Toast.makeText(getContext(), R.string.save_success, Toast.LENGTH_SHORT).show();
                                    }

                                    @Override
                                    public void onProgress(Integer integer, long l) {

                                    }
                                });
                            }
                        });
                        mGotoStore.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Intent intent = new Intent();
                                if (RomUtils.isVivo()) {
                                } else if (RomUtils.isOppo()) {
                                    intent.setComponent(new ComponentName("com.nearme.themespace", "com.nearme.themespace.activities.FontDetailActivity"));
                                    intent.putExtra("fontId", "d51cbbbf629849d08b5b7de8f707ee56");
                                } else if (RomUtils.isEmui()) {
                                    intent.setComponent(new ComponentName("com.huawei.android.thememanager", "com.huawei.android.thememanager.SearchActivity"));
                                } else if (RomUtils.isMiui()) {
                                    intent.setComponent(new ComponentName("com.android.thememanager", "com.android.thememanager.search.ThemeSearchActivity"));
                                } else {
                                    intent.setComponent(new ComponentName("com.samsung.android.themestore", "com.samsung.android.themestore.MainActivity"));
                                }
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                            }
                        });
                    }
                }
            }
        });
    }

    private void handleData() {
        Glide.with(getContext())
                .load(mList.get(mCurentIdx).getContent().getUrl())
                .fitCenter()
                .diskCacheStrategy(DiskCacheStrategy.RESULT)
                .into(mFontPic);
        if (mCurentIdx > 0) {
            mLeftArrow.setVisibility(View.VISIBLE);
        } else {
            mLeftArrow.setVisibility(View.GONE);
        }
        if (mCurentIdx < mList.size() - 1) {
            mRightArrow.setVisibility(View.VISIBLE);
        } else {
            mRightArrow.setVisibility(View.GONE);
        }
    }
}
