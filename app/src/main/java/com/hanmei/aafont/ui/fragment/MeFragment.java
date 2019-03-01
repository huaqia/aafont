package com.hanmei.aafont.ui.fragment;

import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.hanmei.aafont.R;
import com.hanmei.aafont.ui.activity.SettingActivity;
import com.hanmei.aafont.utils.BackendUtils;

import butterknife.BindView;
import butterknife.OnClick;

public class MeFragment extends BaseFragment {
    @BindView(R.id.profile_name)
    TextView mName;

    @Override
    public View createMyView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_me, container, false);
    }

    @Override
    public void init() {
        mName.setText(BackendUtils.getUsername());
    }

    @OnClick({R.id.iv_setting, R.id.iv_more})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.iv_setting:
                startActivity(new Intent(getActivity().getApplicationContext(), SettingActivity.class));
                break;
            case R.id.iv_more: {
                new AsyncTask<Void, Void, Void>() {
                    @Override
                    protected Void doInBackground(Void... params) {
                        Uri pictureUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                        ContentResolver contentResolver = getActivity().getApplicationContext().getContentResolver();
                        //只查询jpeg和png的图片
                        Cursor cursor = contentResolver.query(pictureUri, null,
                                MediaStore.Images.Media.MIME_TYPE + "=? or "
                                        + MediaStore.Images.Media.MIME_TYPE + "=?",
                                new String[]{"image/jpeg", "image/png"}, MediaStore.Images.Media.DATE_MODIFIED);
                        if (cursor == null || cursor.getCount() == 0) {
                        } else {
                            while (cursor.moveToNext()) {
                                //获取图片的路径
                                String path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
                                try {
                                    //获取该图片的父路径名
//                                    String parentName = new File(path).getParentFile().getName();
//                                    //根据父路径名将图片放入到groupMap中
//                                    if (!mGroupMap.containsKey(parentName)) {
//                                        List<String> chileList = new ArrayList<>();
//                                        chileList.add(path);
//                                        mGroupMap.put(parentName, chileList);
//                                    } else {
//                                        mGroupMap.get(parentName).add(path);
//                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                            cursor.close();
                        }
                        return null;
                    }
                }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
                break;
        }
    }
}
