package com.xinmei365.font.ui.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.AppCompatEditText;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.AppCompatTextView;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.TextView;

import com.xinmei365.font.R;
import com.xinmei365.font.helper.RecordShop;
import com.xinmei365.font.helper.RecordUser;
import com.xinmei365.font.model.Search;
import com.xinmei365.font.model.User;
import com.xinmei365.font.ui.adapter.TagAdapter;
import com.xinmei365.font.ui.widget.FlowLayout;
import com.xinmei365.font.ui.widget.TagFlowLayout;
import com.xinmei365.font.utils.BackendUtils;
import com.xinmei365.font.utils.MiscUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import butterknife.BindView;
import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.BmobUser;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FindListener;
import cn.bmob.v3.listener.SaveListener;
import cn.bmob.v3.listener.UpdateListener;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

public class SearchActivity extends BaseActivity {
    @BindView(R.id.cancel)
    AppCompatTextView mCancel;
    @BindView(R.id.edit_query)
    AppCompatEditText mSearchEdit;
    @BindView(R.id.fl_search_records)
    TagFlowLayout mSearchLayout;
    @BindView(R.id.clear_all_records)
    AppCompatImageView mClearAllImage;
    @BindView(R.id.clear_all_text)
    AppCompatTextView mClearAllText;
    @BindView(R.id.refresh_hot_records)
    AppCompatImageView mRefreshHotImage;
    @BindView(R.id.refresh_hot_txt)
    AppCompatTextView mRefreshHotText;
    @BindView(R.id.hot_search_records)
    TagFlowLayout mHotLayout;

    private RecordUser mRecordUser;
    private final int DEFAULT_RECORD_NUMBER = 10;
    private List<String> mRecordList = new ArrayList<>();
    private List<Search> mHotList = new ArrayList<>();
    private List<Search> mUsedHotList = new ArrayList<>();
    private TagAdapter mRecordAdapter;
    private TagAdapter mHotAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        String key = getIntent().getStringExtra("key");
        mSearchEdit.setText(key);
        mSearchEdit.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                if (i == EditorInfo.IME_ACTION_SEARCH){
                    String record = mSearchEdit.getText().toString();
                    if (!TextUtils.isEmpty(record)){
                        updateServer(record);
                        mRecordUser.addRecords(record);
                        Intent intent = new Intent(getApplicationContext(), SearchResultActivity.class);
                        intent.putExtra("key", record);
                        startActivity(intent);
                    }
                    return true;
                }
                return false;
            }
        });

        String username = BackendUtils.getUsername();
        mRecordUser = new RecordUser(this , username);

        initData();
        fetchHotList();

        mRecordAdapter = new TagAdapter<String>(mRecordList) {
            @Override
            public View getView(FlowLayout parent, int position, String str) {
                AppCompatTextView tv = (AppCompatTextView) LayoutInflater.from(SearchActivity.this).inflate(R.layout.item_search_history_tv , mSearchLayout,false);
                tv.setText(str);
                return tv;
            }
        };
        mSearchLayout.setAdapter(mRecordAdapter);
        mSearchLayout.setOnTagClickListener(new TagFlowLayout.OnTagClickListener() {
            @Override
            public void onTagClick(View view, int position, FlowLayout parent) {
                String tagName = mRecordList.get(position);
                updateServer(tagName);
                mSearchEdit.setText("");
                mSearchEdit.setText(tagName);
                mSearchEdit.setSelection(mSearchEdit.length());
                Intent intent = new Intent(getApplicationContext(), SearchResultActivity.class);
                intent.putExtra("key", tagName);
                startActivity(intent);
            }
        });

        mSearchLayout.setOnLongClickListener(new TagFlowLayout.OnLongClickListener() {
            @Override
            public void onLongClick(View view, final int position) {
                MiscUtils.showAskDialog(SearchActivity.this, "确定要删除该条历史记录？" , new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mRecordUser.deleteRecord(mRecordList.get(position));
                    }
                });
            }
        });

        mClearAllImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MiscUtils.showAskDialog(SearchActivity.this, "确定要删除全部历史记录？", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        mSearchLayout.setLimit(true);
                        mRecordUser.deleteUsernameAllRecords();
                    }
                });
            }
        });

        mClearAllText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MiscUtils.showAskDialog(SearchActivity.this, "确定要删除全部历史记录？", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        mSearchLayout.setLimit(true);
                        mRecordUser.deleteUsernameAllRecords();
                    }
                });
            }
        });

        mRecordUser.setNotifyDataChanged(new RecordShop.NotifyDataChanged() {
            @Override
            public void notifyDataChanged() {
                initData();
            }
        });
    }

    private void fetchHotList() {
        BmobQuery<Search> query = new BmobQuery<>();
        query.order("-count");
        query.setLimit(50);
        query.findObjects(new FindListener<Search>() {
            @Override
            public void done(final List<Search> list, BmobException e) {
                if (e == null) {
                    if (list.size() > 0) {
                        mHotList.clear();
                        mHotList.addAll(list);
                        mUsedHotList = getRandomSearchs(10, mHotList);
                        mHotAdapter = new TagAdapter<Search>(mUsedHotList) {
                            @Override
                            public View getView(FlowLayout parent, int position, Search search) {
                                AppCompatTextView tv = (AppCompatTextView) LayoutInflater.from(SearchActivity.this).inflate(R.layout.item_search_history_tv , mSearchLayout,false);
                                tv.setText(search.getName());
                                return tv;
                            }
                        };
                        mHotLayout.setAdapter(mHotAdapter);
                        mHotLayout.setOnTagClickListener(new TagFlowLayout.OnTagClickListener() {
                            @Override
                            public void onTagClick(View view, int position, FlowLayout parent) {
                                String tagName = mUsedHotList.get(position).getName();
                                updateServer(tagName);
                                mSearchEdit.setText("");
                                mSearchEdit.setText(tagName);
                                mSearchEdit.setSelection(mSearchEdit.length());
                                Intent intent = new Intent(getApplicationContext(), SearchResultActivity.class);
                                intent.putExtra("key", tagName);
                                startActivity(intent);
                            }
                        });
                        mRefreshHotImage.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                mUsedHotList = getRandomSearchs(10, mHotList);
                                mHotAdapter.setData(mUsedHotList);
                                mHotAdapter.notifyDataChanged();
                            }
                        });
                        mRefreshHotText.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                mUsedHotList = getRandomSearchs(10, mHotList);
                                mHotAdapter.setData(mUsedHotList);
                                mHotAdapter.notifyDataChanged();
                            }
                        });
                    }
                } else {
                    BackendUtils.handleException(e, SearchActivity.this);
                }
            }
        });
    }

    public List<Search> getRandomSearchs(int n, List<Search> data) {
        Random random = new Random();
        int count = Math.min(n, data.size());
        List<Search> newData = new ArrayList<>();
        for (Search item : data) {
            newData.add(item);
        }
        ArrayList<Search> result = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            int index = random.nextInt(newData.size());
            result.add(newData.get(index));
            newData.remove(index);
        }
        return result;
    }

    private void updateServer(final String name) {
        BmobQuery<Search> query = new BmobQuery<>();
        query.addWhereEqualTo("name", name);
        query.findObjects(new FindListener<Search>() {
            @Override
            public void done(List<Search> list, BmobException e) {
                if (e == null) {
                    if (list.size() == 1) {
                        Search search = list.get(0);
                        search.setCount(search.getCount() + 1);
                        search.update(new UpdateListener() {
                            @Override
                            public void done(BmobException e) {
                                BackendUtils.handleException(e, SearchActivity.this);
                            }
                        });
                    } else {
                        Search search = new Search();
                        search.setName(name);
                        search.setCount(1);
                        search.save(new SaveListener<String>() {
                            @Override
                            public void done(String s, BmobException e) {
                                BackendUtils.handleException(e, SearchActivity.this);
                            }
                        });
                    }
                } else {
                    BackendUtils.handleException(e, SearchActivity.this);
                }
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
                        mRecordList.clear();
                        mRecordList = s;
                        if (mRecordAdapter != null) {
                            mRecordAdapter.setData(mRecordList);
                            mRecordAdapter.notifyDataChanged();
                        }
                    }
                });
    }

    @Override
    protected void setMyContentView() {
        setContentView(R.layout.activity_search);
    }
}
