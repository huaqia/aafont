package com.hanmei.aafont.ui.fragment;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.BottomSheetDialog;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.AppCompatEditText;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;
import android.widget.LinearLayout;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.hanmei.aafont.R;
import com.hanmei.aafont.model.Comment;
import com.hanmei.aafont.model.Product;
import com.hanmei.aafont.model.User;
import com.hanmei.aafont.ui.activity.MainActivity;
import com.hanmei.aafont.ui.adapter.CommentExpandAdapter;
import com.hanmei.aafont.ui.adapter.HeadAdapter;
import com.hanmei.aafont.utils.BackendUtils;
import com.hanmei.aafont.utils.TimeUtils;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.listener.OnLoadmoreListener;
import com.scwang.smartrefresh.layout.listener.OnRefreshListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import butterknife.BindView;
import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.BmobUser;
import cn.bmob.v3.datatype.BmobDate;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FindListener;
import cn.bmob.v3.listener.SaveListener;
import cn.bmob.v3.listener.UpdateListener;

public class FollowFragment extends BaseFragment {
    private static final int PULL_REFRESH = 0;
    private static final int LOAD_MORE = 1;

    private static final int TYPE_HEAD = 0;
    private static final int TYPE_CHILD = 1;
    private static final int TYPE_FOOTER = 2;

    private static final int PAGE_LIMIT = 4;

    private static final String TAG = "FollowFragment";

    @BindView(R.id.recycler_view)
    RecyclerView mRecyclerView;
    @BindView(R.id.swipeLayout_follow)
    SmartRefreshLayout mSwipeRefreshLayout;

    private List<Product> mProducts = new ArrayList<>();
    private ArrayList<Comment> mComments = new ArrayList<>();
    private boolean mHasFooter;
    private FollowAdapter mAdapter;
    private CommentExpandAdapter mCommentAdapter;
    private String mLastTime;
    private AppCompatImageView[] mTips;
    private ArrayList<AppCompatImageView> mViewList;
    private AppCompatButton mComment;
    private AppCompatEditText mCommentContent;
    private int[] mImgIdUrl;
    private Context mContext;

    @Override
    public View createMyView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_follow, container, false);
    }

    @Override
    public void init() {
        super.init();
        this.mContext = getActivity();
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
        mRecyclerView.setLayoutManager(linearLayoutManager);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.addItemDecoration(new DividerItemDecoration(getActivity(), linearLayoutManager.getOrientation()));
        mAdapter = new FollowAdapter();
        mRecyclerView.setAdapter(mAdapter);
        mSwipeRefreshLayout.autoRefresh();
        mSwipeRefreshLayout.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh(RefreshLayout refreshlayout) {
                fetchData(PULL_REFRESH);
            }
        });
        mSwipeRefreshLayout.setOnLoadmoreListener(new OnLoadmoreListener() {
            @Override
            public void onLoadmore(RefreshLayout refreshlayout) {
                fetchData(LOAD_MORE);
            }
        });
    }

    private void fetchData(final int type) {
        BmobQuery<Product> query = new BmobQuery<>();
        query.include("user");
        query.order("-createdAt");
        query.setLimit(PAGE_LIMIT);
        if (type == LOAD_MORE && mLastTime != null) {
            Date date = null;
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            try {
                date = dateFormat.parse(mLastTime);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            if (date != null) {
                query.addWhereLessThanOrEqualTo("createdAt", new BmobDate(date));
            }
        }
        query.findObjects(new FindListener<Product>() {
            @Override
            public void done(List<Product> list, BmobException e) {
                if (e == null) {
                    if (list.size() > 0) {
                        if (type == PULL_REFRESH) {
                            mProducts.clear();
                        }
                        mProducts.addAll(list);
                        if (list.size() < PAGE_LIMIT) {
                            mHasFooter = true;
                            mSwipeRefreshLayout.setEnableLoadmore(false);
                        } else {
                            mHasFooter = false;
                            mSwipeRefreshLayout.setEnableLoadmore(true);
                        }
                        mAdapter.notifyDataSetChanged();
                        mLastTime = list.get(list.size() - 1).getCreatedAt();
                    } else if (type == LOAD_MORE) {
                        mHasFooter = true;
                        mSwipeRefreshLayout.setEnableLoadmore(false);
                        mAdapter.notifyDataSetChanged();
                    } else {
                        mSwipeRefreshLayout.setEnableLoadmore(false);
                    }
                } else {
                    Log.e(TAG, e.toString());
                }
                if (type == PULL_REFRESH) {
                    mSwipeRefreshLayout.finishRefresh();
                } else {
                    mSwipeRefreshLayout.finishLoadmore();
                }
            }
        });
    }

    private void goToChoiceFragment() {
        ((MainActivity) getActivity()).goToChoiceFragment();
    }


    class FollowAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        private Handler handler = new Handler() {
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case 0:
                        mComments = (ArrayList<Comment>) msg.obj;
                        break;
                }
            }
        };

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            if (viewType == TYPE_HEAD) {
                return new HeadViewHolder(inflater.inflate(R.layout.item_header, parent, false));
            } else if (viewType == TYPE_CHILD) {
                return new FollowViewHolder(inflater.inflate(R.layout.item_follow, parent, false));
            } else {
                return new FooterViewHolder(inflater.inflate(R.layout.item_footer, parent, false));
            }
        }

        @Override
        public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position) {
            if (holder instanceof FollowViewHolder) {
                final FollowViewHolder followViewHolder = (FollowViewHolder) holder;
                if (position >= 1) {
                    final Product product = mProducts.get(position - 1);
                    String url = product.getContent().getUrl();
                    Glide.with(holder.itemView.getContext())
                            .load(url)
                            .fitCenter()
                            .diskCacheStrategy(DiskCacheStrategy.RESULT)
                            .into(followViewHolder.mThumbnailIcon);
                    followViewHolder.mUserName.setText(product.getUser().getUsername());
                    followViewHolder.mProductTime.setText(TimeUtils.getTimeFormatText(TimeUtils.getSimpleDateFormat(product.getCreatedAt())));
                    final int finalPosition = position - 1;
                    ArrayList<String> likeIdList = product.getLikeId();
                    final User currentUser = BmobUser.getCurrentUser(User.class);
                    if (likeIdList != null && likeIdList.contains(currentUser.getObjectId())) {
                        followViewHolder.mToLike.setImageResource(R.drawable.liked);
                    } else {
                        followViewHolder.mToLike.setImageResource(R.drawable.to_like);
                    }
                    if (likeIdList != null && likeIdList.size() > 0) {
                        followViewHolder.mLikeArea.setVisibility(View.VISIBLE);
                        followViewHolder.mLikeInfo.setText(getString(R.string.like_count, likeIdList.size()));
                    } else {
                        followViewHolder.mLikeArea.setVisibility(View.GONE);
                    }
                    followViewHolder.mToLike.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            ArrayList<String> likeIdList = new ArrayList<>();
                            final User currentUser = BmobUser.getCurrentUser(User.class);
                            if (product.getLikeId() != null) {
                                likeIdList.addAll(product.getLikeId());
                            }
                            if (!likeIdList.contains(currentUser.getObjectId())) {
                                likeIdList.add(currentUser.getObjectId());
                            } else {
                                likeIdList.remove(currentUser.getObjectId());
                            }
                            if (product.getUser().getObjectId() != currentUser.getObjectId()) {
                                BackendUtils.pushMessage(product.getUser(), "LIKE", "消息内容");
                            }
                            product.setLikeId(likeIdList);
                            mProducts.set(finalPosition, product);
                            mAdapter.notifyItemChanged(position);
                            product.update(new UpdateListener() {
                                @Override
                                public void done(BmobException e) {

                                }
                            });
                        }
                    });
                    followViewHolder.mCommentListView.setGroupIndicator(null);

                    BmobQuery<Comment> query = new BmobQuery<>();
                    query.addWhereEqualTo("product", product.getObjectId());
                    query.include("user");
                    query.order("-createdAt");
                    query.findObjects(new FindListener<Comment>() {
                        @Override
                        public void done(List<Comment> list, BmobException e) {
                            if (e == null) {
                                Log.e(TAG, "查询成功" + list.size());
                                if (list.size() >= 0) {
                                    mComments.clear();
                                }
                                mComments.addAll(list);

                                Message message = handler.obtainMessage();
                                message.what = 0;
                                message.obj = list;
                                handler.sendMessage(message);

                                mCommentAdapter = new CommentExpandAdapter(mContext, mComments);
                                followViewHolder.mCommentListView.setAdapter(mCommentAdapter);
                                Log.e(TAG, "mComments的数量" + mComments.size());
                            } else {
                                Log.e(TAG, e.toString());
                            }
                        }
                    });

                    Log.e(TAG, "mComments的数量" + mComments.size());
                    for (int i = 0; i < mComments.size(); i++) {
                        followViewHolder.mCommentListView.expandGroup(i);
                    }
                    followViewHolder.mCommentListView.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {
                        @Override
                        public boolean onGroupClick(ExpandableListView expandableListView, View view, int groupPosition, long l) {
                            boolean isExpanded = followViewHolder.mCommentListView.isGroupExpanded(groupPosition);
                            Log.e(TAG, "onGroupClick:当前的评论Id>>>" + mComments.get(groupPosition).getObjectId());
                            if (isExpanded) {
                                expandableListView.collapseGroup(groupPosition);
                            } else {
                                expandableListView.expandGroup(groupPosition, true);
                            }
                            return true;
                        }
                    });
                    followViewHolder.mToComment.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            final Dialog dialog = new BottomSheetDialog(getContext());
                            final View commentView = LayoutInflater.from(getContext()).inflate(R.layout.comment_dialog_layout, null);
                            dialog.setContentView(commentView);

                            mComment = (AppCompatButton) commentView.findViewById(R.id.comment_bt);
                            mCommentContent = (AppCompatEditText) commentView.findViewById(R.id.comment_ed);

                            View parent = (View) commentView.getParent();
                            BottomSheetBehavior behavior = BottomSheetBehavior.from(parent);
                            commentView.measure(0, 0);
                            behavior.setPeekHeight(commentView.getMeasuredHeight());

                            mComment.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    String commentContent = mCommentContent.getText().toString().trim();
                                    if (!TextUtils.isEmpty(commentContent)) {
                                        dialog.dismiss();
                                        Comment comment = new Comment();
                                        comment.setUser(currentUser);
                                        comment.setContent(commentContent);
                                        comment.setProduct(product);
                                        comment.save(new SaveListener<String>() {
                                            @Override
                                            public void done(String s, BmobException e) {
                                                if (e == null) {
                                                    Log.e(TAG, "添加成功");
                                                    fetchData(PULL_REFRESH);
                                                } else {
                                                    Log.e(TAG, "添加失败" + e.toString());
                                                }
                                            }
                                        });
                                        mCommentAdapter.notifyDataSetChanged();
                                    } else {
                                        Log.e(TAG, "评论内容为空");
                                    }
                                }
                            });
                            dialog.show();
                        }
                    });

                }
            } else if (holder instanceof FooterViewHolder) {

            } else if (holder instanceof HeadViewHolder) {

                HeadViewHolder headViewHolder = (HeadViewHolder) holder;
                mImgIdUrl = new int[]{R.drawable.item1, R.drawable.item2, R.drawable.item3};
                mTips = new AppCompatImageView[mImgIdUrl.length];
                headViewHolder.mViewGroup.removeAllViews();
                for (int i = 0; i < mTips.length; i++) {
                    AppCompatImageView imageView = new AppCompatImageView(getActivity());
                    mTips[i] = imageView;
                    if (i == 0) {
                        mTips[i].setBackgroundResource(R.drawable.head_item_chooice);
                    } else {
                        mTips[i].setBackgroundResource(R.drawable.head_item_unchooice);
                    }
                    headViewHolder.mViewGroup.addView(imageView);
                }

                mViewList = new ArrayList<AppCompatImageView>();

                for (int i = 0; i < mImgIdUrl.length; i++) {
                    AppCompatImageView imageView = new AppCompatImageView(getActivity());
                    imageView.setBackgroundResource(mImgIdUrl[i]);
                    mViewList.add(imageView);
                }

                headViewHolder.mViewPage.setAdapter(new HeadAdapter(mViewList));
                headViewHolder.mViewPage.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                    @Override
                    public void onPageScrolled(int arg0, float arg1, int arg2) {

                    }

                    @Override
                    public void onPageSelected(int arg0) {
                        for (int i = 0; i < mTips.length; i++) {
                            if (i == arg0) {
                                mTips[i].setBackgroundResource(R.drawable.head_item_chooice);
                            } else {
                                mTips[i].setBackgroundResource(R.drawable.head_item_unchooice);
                            }
                        }
                    }

                    @Override
                    public void onPageScrollStateChanged(int arg0) {

                    }
                });
            }
        }

        @Override
        public int getItemCount() {
            if (mHasFooter) {
                return mProducts.size();
            } else {
                return mProducts.size() - 1;
            }
        }

        @Override
        public int getItemViewType(int position) {
            if (position == 0) {
                return TYPE_HEAD;
            } else {
                if (position < mProducts.size() - 1) {
                    return TYPE_CHILD;
                } else {
                    return TYPE_FOOTER;
                }
            }

        }
    }

    class HeadViewHolder extends RecyclerView.ViewHolder {
        public ViewPager mViewPage;
        public ViewGroup mViewGroup;

        public HeadViewHolder(@NonNull View itemView) {
            super(itemView);
            mViewPage = (ViewPager) itemView.findViewById(R.id.item_head_page);
            mViewGroup = (ViewGroup) itemView.findViewById(R.id.item_head_page_spot);

        }
    }

    class FollowViewHolder extends RecyclerView.ViewHolder {
        public AppCompatImageView mThumbnailIcon;
        public AppCompatTextView mUserName;
        public AppCompatTextView mProductTime;
        public LinearLayout mLikeArea;
        public AppCompatTextView mLikeInfo;
        public AppCompatImageView mToLike;
        public AppCompatImageView mToComment;
        public AppCompatImageView mToStore;
        public AppCompatImageView mToShare;
        public ExpandableListView mCommentListView;

        public FollowViewHolder(View itemView) {
            super(itemView);
            mThumbnailIcon = (AppCompatImageView) itemView.findViewById(R.id.thumbnail_icon);
            mUserName = (AppCompatTextView) itemView.findViewById(R.id.user_name);
            mProductTime = (AppCompatTextView) itemView.findViewById(R.id.product_time);
            mLikeArea = (LinearLayout) itemView.findViewById(R.id.like_area);
            mLikeInfo = (AppCompatTextView) itemView.findViewById(R.id.like_info);
            mToLike = (AppCompatImageView) itemView.findViewById(R.id.to_like);
            mToComment = (AppCompatImageView) itemView.findViewById(R.id.to_comment);
            mToStore = (AppCompatImageView) itemView.findViewById(R.id.to_store);
            mToShare = (AppCompatImageView) itemView.findViewById(R.id.to_share);
            mCommentListView = (ExpandableListView) itemView.findViewById(R.id.detail_page_lv_comment);
        }
    }

    class FooterViewHolder extends RecyclerView.ViewHolder {
        public AppCompatTextView mGoto;

        public FooterViewHolder(View itemView) {
            super(itemView);
            mGoto = (AppCompatTextView) itemView.findViewById(R.id.go_to);
            mGoto.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    goToChoiceFragment();
                }
            });
        }
    }
}
