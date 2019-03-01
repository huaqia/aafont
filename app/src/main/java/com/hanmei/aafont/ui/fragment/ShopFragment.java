package com.hanmei.aafont.ui.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.hanmei.aafont.R;
import com.hanmei.aafont.model.Background;
import com.hanmei.aafont.model.Card;
import com.hanmei.aafont.model.Filter;
import com.hanmei.aafont.model.Font;
import com.hanmei.aafont.ui.activity.BgAreaActivity;
import com.hanmei.aafont.ui.activity.CardAreaActivity;
import com.hanmei.aafont.ui.activity.FilterAreaActivity;
import com.hanmei.aafont.ui.activity.FontAreaActivity;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FindListener;

public class ShopFragment extends BaseFragment {
    @BindView(R.id.font_recycler_view)
    RecyclerView mFontRecyclerView;
    @BindView(R.id.card_recycler_view)
    RecyclerView mCardRecyclerView;
    @BindView(R.id.filter_recycler_view)
    RecyclerView mFilterRecyclerView;
    @BindView(R.id.bg_recycler_view)
    RecyclerView mBgRecyclerView;
    @BindView(R.id.enter_font_area)
    LinearLayout mEnterFontArea;
    @BindView(R.id.enter_card_area)
    LinearLayout mEnterCardArea;
    @BindView(R.id.enter_filter_area)
    LinearLayout mEnterFilterArea;
    @BindView(R.id.enter_bg_area)
    LinearLayout mEnterBgArea;

    private FilterAdapter mFilterAdapter;
    private CardAdapter mCardAdapter;
    private FontAdapter mFontAdapter;
    private BgAdapter mBgAdapter;

    private List<Filter> mFilters = new ArrayList<>();
    private List<Card> mCards = new ArrayList<>();
    private List<Font> mFonts = new ArrayList<>();
    private List<Background> mBgs = new ArrayList<>();

    @Override
    public View createMyView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_shop, container, false);
    }

    @Override
    public void init() {
        super.init();
        {
            mFilterAdapter = new FilterAdapter();
            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false);
            mFilterRecyclerView.setLayoutManager(linearLayoutManager);
            BmobQuery<Filter> query = new BmobQuery<>();
            query.order("-createdAt");
            query.setLimit(10);
            query.findObjects(new FindListener<Filter>() {
                @Override
                public void done(List<Filter> list, BmobException e) {
                    if (e == null) {
                        mFilters = list;
                        mFilterRecyclerView.setAdapter(mFilterAdapter);
                    }
                }
            });
            mEnterFilterArea.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    startActivity(new Intent(getActivity(), FilterAreaActivity.class));
                }
            });
        }
        {
            mCardAdapter = new CardAdapter();
            final GridLayoutManager layoutManager = new GridLayoutManager(getActivity(), 2, GridLayoutManager.HORIZONTAL, false);
            mCardRecyclerView.setLayoutManager(layoutManager);
            BmobQuery<Card> query = new BmobQuery<>();
            query.order("-createdAt");
            query.setLimit(20);
            query.findObjects(new FindListener<Card>() {
                @Override
                public void done(List<Card> list, BmobException e) {
                    if (e == null) {
                        mCards = list;
                        mCardRecyclerView.setAdapter(mCardAdapter);
                    }
                }
            });
            mEnterCardArea.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    startActivity(new Intent(getActivity(), CardAreaActivity.class));
                }
            });
        }
        {
            mFontAdapter = new FontAdapter();
            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false);
            mFontRecyclerView.setLayoutManager(linearLayoutManager);
            BmobQuery<Font> query = new BmobQuery<>();
            query.order("-createdAt");
            query.setLimit(10);
            query.findObjects(new FindListener<Font>() {
                @Override
                public void done(List<Font> list, BmobException e) {
                    if (e == null) {
                        mFonts = list;
                        mFontRecyclerView.setAdapter(mFontAdapter);
                    }
                }
            });
            mEnterFontArea.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    startActivity(new Intent(getActivity(), FontAreaActivity.class));
                }
            });
        }
        {
            mBgAdapter = new BgAdapter();
            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false);
            mBgRecyclerView.setLayoutManager(linearLayoutManager);
            BmobQuery<Background> query = new BmobQuery<>();
            query.order("-createdAt");
            query.setLimit(10);
            query.findObjects(new FindListener<Background>() {
                @Override
                public void done(List<Background> list, BmobException e) {
                    if (e == null) {
                        mBgs = list;
                        mBgRecyclerView.setAdapter(mBgAdapter);
                    }
                }
            });
            mEnterBgArea.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    startActivity(new Intent(getActivity(), BgAreaActivity.class));
                }
            });
        }
    }

    class FilterAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            return new FilterViewHolder(inflater.inflate(R.layout.item_shop_filter, parent, false));
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
            if (holder instanceof FilterViewHolder) {
                FilterViewHolder viewHolder = (FilterViewHolder)holder;
                AppCompatTextView name = viewHolder.mName;
                AppCompatImageView preview = viewHolder.mPreview;
                final Filter filter = mFilters.get(position);
                name.setText(filter.getName());
                Glide.with(holder.itemView.getContext())
                        .load(filter.getPreview().getUrl())
                        .fitCenter()
                        .diskCacheStrategy(DiskCacheStrategy.RESULT)
                        .into(preview);
                preview.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                    }
                });
            }
        }

        @Override
        public int getItemCount() {
            return mFilters.size();
        }
    }

    class FilterViewHolder extends RecyclerView.ViewHolder {
        public AppCompatTextView mName;
        public AppCompatImageView mPreview;

        public FilterViewHolder(View itemView) {
            super(itemView);
            mName = (AppCompatTextView) itemView.findViewById(R.id.name);
            mPreview = (AppCompatImageView) itemView.findViewById(R.id.preview);
        }
    }

    class CardAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            return new CardViewHolder(inflater.inflate(R.layout.item_shop_card, parent, false));
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
            if (holder instanceof CardViewHolder) {
                CardViewHolder viewHolder = (CardViewHolder)holder;
                AppCompatTextView name = viewHolder.mName;
                AppCompatImageView preview = viewHolder.mPreview;
                final Card card = mCards.get(position);
                name.setText(card.getName());
                String url = card.getPreview().getUrl();
                Glide.with(holder.itemView.getContext())
                        .load(url)
                        .fitCenter()
                        .diskCacheStrategy(DiskCacheStrategy.RESULT)
                        .into(preview);
            }
        }

        @Override
        public int getItemCount() {
            return mCards.size();
        }
    }

    class CardViewHolder extends RecyclerView.ViewHolder {
        public AppCompatTextView mName;
        public AppCompatImageView mPreview;

        public CardViewHolder(View itemView) {
            super(itemView);
            mName = (AppCompatTextView) itemView.findViewById(R.id.name);
            mPreview = (AppCompatImageView) itemView.findViewById(R.id.preview);
        }
    }

    class FontAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            return new FontViewHolder(inflater.inflate(R.layout.item_shop_font, parent, false));
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
            if (holder instanceof FontViewHolder) {
                FontViewHolder viewHolder = (FontViewHolder)holder;
                AppCompatTextView name = viewHolder.mName;
                AppCompatImageView preview = viewHolder.mPreview;
                final Font font = mFonts.get(position);
                name.setText(font.getName());
                String url = font.getPreview().getUrl();
                Glide.with(holder.itemView.getContext())
                        .load(url)
                        .fitCenter()
                        .diskCacheStrategy(DiskCacheStrategy.RESULT)
                        .into(preview);
            }
        }

        @Override
        public int getItemCount() {
            return mFonts.size();
        }
    }

    class FontViewHolder extends RecyclerView.ViewHolder {
        public AppCompatTextView mName;
        public AppCompatImageView mPreview;

        public FontViewHolder(View itemView) {
            super(itemView);
            mName = (AppCompatTextView) itemView.findViewById(R.id.name);
            mPreview = (AppCompatImageView) itemView.findViewById(R.id.preview);
        }
    }

    class BgAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            return new BgViewHolder(inflater.inflate(R.layout.item_shop_bg, parent, false));
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
            if (holder instanceof BgViewHolder) {
                BgViewHolder viewHolder = (BgViewHolder)holder;
                AppCompatImageView preview = viewHolder.mPreview;
                final Background bg = mBgs.get(position);
                String url = bg.getContent().getUrl();
                Glide.with(holder.itemView.getContext())
                        .load(url)
                        .fitCenter()
                        .diskCacheStrategy(DiskCacheStrategy.RESULT)
                        .into(preview);
            }
        }

        @Override
        public int getItemCount() {
            return mBgs.size();
        }
    }

    class BgViewHolder extends RecyclerView.ViewHolder {
        public AppCompatImageView mPreview;

        public BgViewHolder(View itemView) {
            super(itemView);
            mPreview = (AppCompatImageView) itemView.findViewById(R.id.preview);
        }
    }
}
