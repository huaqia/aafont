package com.hanmei.aafont.ui.fragment;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
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
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
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

    public Bitmap bitmap;

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
                                String galleryPath = Environment.getExternalStorageDirectory() + File.separator + Environment.DIRECTORY_DCIM + File.separator + "Camera" + File.separator;
                                BmobFile file = mList.get(mCurentIdx).getContent();
                                //File saveFile = new File(FileUtils.getFileDir(getContext(), "daily_font"), file.getFilename());
                                File saveFile = null;
                                try{
                                    saveFile = new File(galleryPath , file.getFilename());
                                    if (!saveFile.exists()){
                                        saveFile.mkdir();
                                    }
                                    FileOutputStream fos = new FileOutputStream(saveFile.toString());
                                    if (fos != null){
                                        bitmap.compress(Bitmap.CompressFormat.PNG , 90 , fos);
                                        fos.flush();
                                        fos.close();
                                    }
                                }catch (FileNotFoundException e){
                                    e.printStackTrace();
                                }catch (IOException e){
                                    e.printStackTrace();
                                }
                                MediaStore.Images.Media.insertImage(getContext().getContentResolver() , bitmap , saveFile.toString() ,null);
                                Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                                Uri uri = Uri.fromFile(saveFile);
                                intent.setData(uri);
                                getContext().sendBroadcast(intent);
//                                file.download(saveFile, new DownloadFileListener() {
//                                    @Override
//                                    public void done(String s, BmobException e) {
//                                    }
//                                    @Override
//                                    public void onProgress(Integer integer, long l) {
//                                    }
//                                });
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
