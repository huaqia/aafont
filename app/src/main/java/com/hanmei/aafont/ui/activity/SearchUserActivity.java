package com.hanmei.aafont.ui.activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.widget.AppCompatEditText;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.AppCompatTextView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.LinearLayout;

import com.hanmei.aafont.R;
import com.hanmei.aafont.helper.RecordShop;
import com.hanmei.aafont.helper.RecordUser;
import com.hanmei.aafont.model.User;
import com.hanmei.aafont.ui.adapter.TagAdapter;
import com.hanmei.aafont.ui.widget.FlowLayout;
import com.hanmei.aafont.ui.widget.TagFlowLayout;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import cn.bmob.v3.BmobUser;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

public class SearchUserActivity extends BaseActivity {

    @BindView(R.id.edit_query)
    AppCompatEditText mSearchEdit;
    @BindView(R.id.fl_search_records)
    TagFlowLayout mSearchLayout;
    @BindView(R.id.clear_all_records)
    AppCompatImageView mClearAllImage;
    @BindView(R.id.iv_arrow)
    AppCompatImageView mArrowImage;
    @BindView(R.id.iv_search)
    AppCompatTextView mSearchText;
    @BindView(R.id.iv_clear_search)
    AppCompatImageView mClearImage;
    @BindView(R.id.ll_history_content)
    LinearLayout mHistoryContent;
    @BindView(R.id.iv_back)
    AppCompatImageView mBackImage;

    private RecordUser mRecordUser;
    private final int DEFAULT_RECORD_NUMBER = 10;
    private List<String> recordList = new ArrayList<>();
    private TagAdapter mRecordAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String username = BmobUser.getCurrentUser(User.class).getUsername();
        mRecordUser = new RecordUser(this , username);

        initData();

        mRecordAdapter = new TagAdapter<String>(recordList) {
            @Override
            public View getView(FlowLayout parent, int position, String str) {
                AppCompatTextView tv = (AppCompatTextView) LayoutInflater.from(SearchUserActivity.this).inflate(R.layout.item_search_history_tv , mSearchLayout,false);
                tv.setText(str);
                return tv;
            }
        };
        mSearchLayout.setAdapter(mRecordAdapter);
        mSearchLayout.setOnTagClickListener(new TagFlowLayout.OnTagClickListener() {
            @Override
            public void onTagClick(View view, int position, FlowLayout parent) {
                mSearchEdit.setText("");
                mSearchEdit.setText(recordList.get(position));
                mSearchEdit.setSelection(mSearchEdit.length());
            }
        });

        mSearchLayout.setOnLongClickListener(new TagFlowLayout.OnLongClickListener() {
            @Override
            public void onLongClick(View view, final int position) {
                showDialog("确定要删除该条历史记录？" , new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mRecordUser.deleteRecord(recordList.get(position));
                    }
                });
            }
        });

        mSearchLayout.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                boolean isOverFlow = mSearchLayout.isOverFlow();
                boolean isLimit = mSearchLayout.isLimit();
                if (isLimit && isOverFlow){
                    mArrowImage.setVisibility(View.VISIBLE);
                }else {
                    mArrowImage.setVisibility(View.GONE);
                }
            }
        });

        mArrowImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mSearchLayout.setLimit(false);
                mRecordAdapter.notifyDataChanged();
            }
        });

        mClearAllImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDialog("确定要删除全部历史记录？", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        mSearchLayout.setLimit(true);
                        mRecordUser.deleteUsernameAllRecords();
                    }
                });
            }
        });

        mSearchText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String record = mSearchEdit.getText().toString();
                if (!TextUtils.isEmpty(record)){
                    mRecordUser.addRecords(record);
                }
            }
        });

        mClearImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mSearchEdit.setText("");
            }
        });

        mRecordUser.setNotifyDataChanged(new RecordShop.NotifyDataChanged() {
            @Override
            public void notifyDataChanged() {
                initData();
            }
        });

        mBackImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void initData() {
        Observable.create(new ObservableOnSubscribe<List<String>>() {
            @Override
            public void subscribe(ObservableEmitter<List<String>> emitter) throws Exception {
                emitter.onNext(mRecordUser.getRecordsByNumber(DEFAULT_RECORD_NUMBER));
            }
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<List<String>>() {
                    @Override
                    public void accept(List<String> s) throws Exception {
                        recordList.clear();
                        recordList = s;
                        if (null == recordList || recordList.size() == 0) {
                            mHistoryContent.setVisibility(View.GONE);
                        } else {
                            mHistoryContent.setVisibility(View.VISIBLE);
                        }
                        if (mRecordAdapter != null) {
                            mRecordAdapter.setData(recordList);
                            mRecordAdapter.notifyDataChanged();
                        }
                    }
                });
    }

    @Override
    protected void setMyContentView() {
        setContentView(R.layout.item_search_user);
    }

    private void showDialog(String dialogTitle , DialogInterface.OnClickListener onClickListener){
        AlertDialog.Builder builder = new AlertDialog.Builder(SearchUserActivity.this);
        builder.setMessage(dialogTitle);
        builder.setPositiveButton("确定"  , onClickListener);
        builder.setNegativeButton("取消" , null);
        builder.create().show();
    }
}
