package com.hanmei.aafont.ui.fragment;

import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.LocalBroadcastManager;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.hanmei.aafont.R;
import com.hanmei.aafont.model.Product;
import com.hanmei.aafont.model.Relation;
import com.hanmei.aafont.model.User;
import com.hanmei.aafont.ui.activity.EditUserMessageActivity;
import com.hanmei.aafont.ui.activity.SettingActivity;
import com.hanmei.aafont.utils.BackendUtils;

import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;
import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.BmobUser;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.CountListener;
import cn.bmob.v3.listener.FindListener;
import de.hdodenhof.circleimageview.CircleImageView;

import static cn.bmob.v3.Bmob.getApplicationContext;

public class MeFragment extends BaseFragment {
    @BindView(R.id.profile_name)
    TextView mName;
    @BindView(R.id.force_num)
    TextView mFocusText;
    @BindView(R.id.follow_num)
    TextView mFollowText;
    @BindView(R.id.work_num)
    TextView mWorkText;
    @BindView(R.id.profile_description)
    TextView mDescription;
    @BindView(R.id.profile_image)
    CircleImageView mUserIcon;

    private Dialog mMoreDialog;

    @Override
    public View createMyView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_me, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        LocalBroadcastManager broadcastManager = LocalBroadcastManager.getInstance(getActivity());
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.intent.action.CART_BROADCAST");
        BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String msg = intent.getStringExtra("data");
                if ("refresh".equals(msg)){
                    refresh();
                }
            }
        };
        broadcastManager.registerReceiver(broadcastReceiver , intentFilter);
    }

    private void refresh(){
        init();
    }

    @Override
    public void init() {
        initDate();
    }

    Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case 1:
                    Relation relation = (Relation) msg.obj;
                    if (relation.getFocusIds() == null){
                        mFocusText.setText("0");
                    }else {
                        mFocusText.setText(relation.getFocusIds().size()+"");
                    }
                    if (relation.getFollowIds()==null){
                        mFollowText.setText("0");
                    }
                    else{
                        mFollowText.setText(relation.getFollowIds().size()+"");
                    }
                    break;
                case 2:
                    int integer = (int)msg.obj;
                    mWorkText.setText(integer > 0 ? integer + "" : "0");
                    break;
                case 3:
                    User user = (User)msg.obj;
                    mDescription.setText(user.getIntro());
                    mName.setText(user.getUsername());

                    Glide.with(getApplicationContext())
                            .load(user.getAvatar().getUrl())
                            .fitCenter()
                            .diskCacheStrategy(DiskCacheStrategy.RESULT)
                            .into(mUserIcon);
                    break;
            }
        }
    };

    private void initDate(){
        final User currentUser = BmobUser.getCurrentUser(User.class);
        BmobQuery<Relation> query = new BmobQuery<>();
        query.addWhereEqualTo("user" , currentUser);
        query.order("-createdAt");
        query.findObjects(new FindListener<Relation>() {
            @Override
            public void done(List<Relation> list, BmobException e) {
                if (e == null){
                    if (list.size() == 1){
                        Relation relation = list.get(0);
                        Message msg =  mHandler.obtainMessage();
                        msg.what = 1;
                        msg.obj = relation;
                        mHandler.sendMessage(msg);
                    }
                }
            }
        });

        BmobQuery<Product> queryP = new BmobQuery<>();
        queryP.addWhereEqualTo("user" , currentUser);
        queryP.order("-createdAt");
        queryP.count(Product.class, new CountListener() {
            @Override
            public void done(Integer integer, BmobException e) {
                if (e == null){
                    Message msg =  mHandler.obtainMessage();
                    msg.what = 2;
                    msg.obj = integer;
                    mHandler.sendMessage(msg);
                }
            }
        });

        BmobQuery<User> queryU = new BmobQuery<>();
        queryU.addWhereEqualTo("objectId" , currentUser.getObjectId());
        queryU.order("-createdAt");
        queryU.findObjects(new FindListener<User>() {
            @Override
            public void done(List<User> list, BmobException e) {
                if (e == null){
                    User user = list.get(0);
                    Message msg = mHandler.obtainMessage();
                    msg.what = 3;
                    msg.obj = user;
                    mHandler.sendMessage(msg);
                }
            }
        });
    }

    private void initMoreDialog(){
        mMoreDialog = new Dialog(getActivity(),R.style.dialog_bottom_full);
        mMoreDialog.setCanceledOnTouchOutside(true);
        mMoreDialog.setCancelable(true);
        Window window = mMoreDialog.getWindow();
        window.setGravity(Gravity.BOTTOM);
        window.setWindowAnimations(R.style.more_animation);
        View view = View.inflate(getActivity() , R.layout.dialog_more,null);
        view.findViewById(R.id.btn_delete).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mMoreDialog != null && mMoreDialog.isShowing()){
                    mMoreDialog.dismiss();
                }
            }
        });
        view.findViewById(R.id.create_word).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        view.findViewById(R.id.update_word).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        window.setContentView(view);
        window.setLayout(WindowManager.LayoutParams.MATCH_PARENT,WindowManager.LayoutParams.WRAP_CONTENT);
    }

    private void showDialog(){
        if (mMoreDialog == null){
            initMoreDialog();
        }
        mMoreDialog.show();
    }

    @OnClick({R.id.iv_setting, R.id.iv_more , R.id.profile_edit})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.iv_setting:
                startActivity(new Intent(getActivity().getApplicationContext(), SettingActivity.class));
                break;
            case R.id.iv_more: {
                showDialog();
//                new AsyncTask<Void, Void, Void>() {
//                    @Override
//                    protected Void doInBackground(Void... params) {
//                        Uri pictureUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
//                        ContentResolver contentResolver = getActivity().getApplicationContext().getContentResolver();
//                        //只查询jpeg和png的图片
//                        Cursor cursor = contentResolver.query(pictureUri, null,
//                                MediaStore.Images.Media.MIME_TYPE + "=? or "
//                                        + MediaStore.Images.Media.MIME_TYPE + "=?",
//                                new String[]{"image/jpeg", "image/png"}, MediaStore.Images.Media.DATE_MODIFIED);
//                        if (cursor == null || cursor.getCount() == 0) {
//                        } else {
//                            while (cursor.moveToNext()) {
//                                //获取图片的路径
//                                String path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
//                                try {
//                                    //获取该图片的父路径名
//                                    String parentName = new File(path).getParentFile().getName();
//                                    //根据父路径名将图片放入到groupMap中
//                                    if (!mGroupMap.containsKey(parentName)) {
//                                        List<String> chileList = new ArrayList<>();
//                                        chileList.add(path);
//                                        mGroupMap.put(parentName, chileList);
//                                    } else {
//                                        mGroupMap.get(parentName).add(path);
//                                    }
//                                } catch (Exception e) {
//                                    e.printStackTrace();
//                                }
//                            }
//                            cursor.close();
//                        }
//                        return null;
//                    }
//                }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                break;
        }
            case R.id.profile_edit:
                startActivity(new Intent(getActivity().getApplicationContext() , EditUserMessageActivity.class));
        default:
                break;
        }
    }
}
