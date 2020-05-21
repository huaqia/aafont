package com.hanmei.aafont.ui.activity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Layout;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.hanmei.aafont.R;
import com.hanmei.aafont.filter.FilterFactory;
import com.hanmei.aafont.filter.FilterItem;
import com.hanmei.aafont.filter.GPUImageFilter;
import com.hanmei.aafont.model.Background;
import com.hanmei.aafont.model.Card;
import com.hanmei.aafont.model.Filter;
import com.hanmei.aafont.model.Font;
import com.hanmei.aafont.ui.widget.BitmapStickerIcon;
import com.hanmei.aafont.ui.widget.DeleteIconEvent;
import com.hanmei.aafont.ui.widget.FlipHorizontallyEvent;
import com.hanmei.aafont.ui.widget.GPUImageView;
import com.hanmei.aafont.ui.widget.Sticker;
import com.hanmei.aafont.ui.widget.StickerIconEvent;
import com.hanmei.aafont.ui.widget.StickerView;
import com.hanmei.aafont.ui.widget.TextSticker;
import com.hanmei.aafont.ui.widget.ZoomIconEvent;
import com.hanmei.aafont.utils.BitmapUtils;
import com.hanmei.aafont.utils.FileUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import butterknife.BindView;
import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FindListener;

public class EditActivity extends BaseActivity {
    private static final int EDIT_MODE_FILTER = 0;
    private static final int EDIT_MODE_CARD = 1;
    private static final int EDIT_MODE_FONT = 2;
    private static final int EDIT_MODE_BG = 3;

    @BindView(R.id.pic_area)
    RelativeLayout mPic;

    @BindView(R.id.image_area)
    GPUImageView mImage;

    @BindView(R.id.frame_pic)
    AppCompatImageView mFramePic;

    @BindView(R.id.sticker_view)
    StickerView mStickerView;

    @BindView(R.id.recycler_view)
    RecyclerView mRecycleView;

    @BindView(R.id.filter_icon)
    AppCompatImageView mFilterIcon;

    @BindView(R.id.card_icon)
    AppCompatImageView mCardIcon;

    @BindView(R.id.font_icon)
    AppCompatImageView mFontIcon;

    @BindView(R.id.bg_icon)
    AppCompatImageView mBgIcon;

    @BindView(R.id.save_pic)
    AppCompatTextView mSavePic;

    private FilterAdapter mFilterAdapter;
    private CardAdapter mCardAdapter;
    private FontAdapter mFontAdapter;
    private BgAdapter mBgAdapter;

    private Bitmap mBitmap;
    private Bitmap mSmallBitmap;

    private int mEditMode;

    private int mScreenWidth;
    private int mStickerIconWidth;
    private int mScreenHeight;

    private Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            return false;
        }
    });

    List<Filter> mFilters = new ArrayList<>();
    List<Card> mCards = new ArrayList<>();
    List<Font> mFonts = new ArrayList<>();
    List<Background> mBgs = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_set);
        toolbar.setTitle("");
        toolbar.setNavigationIcon(R.drawable.back);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        final String url = getIntent().getExtras().getString("url");
        Uri uri = Uri.parse("file://" + url);
        BitmapUtils.asyncLoadImage(this, uri, new BitmapUtils.LoadImageCallback() {
            @Override
            public void callback(Bitmap result) {
                mBitmap = result;
                mImage.setImage(mBitmap);
            }
        });

        mEditMode = EDIT_MODE_FILTER;
        mFilterAdapter = new FilterAdapter();
        mRecycleView.setAdapter(mFilterAdapter);
        BitmapUtils.asyncLoadSmallImage(this, uri, new BitmapUtils.LoadImageCallback() {
            @Override
            public void callback(Bitmap result) {
                mSmallBitmap = result;
                if (mEditMode == EDIT_MODE_FILTER) {
//                    mRecycleView.setAdapter(mFilterAdapter);
                    mFilterAdapter.notifyDataSetChanged();
                }
            }
        });
        {
            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
            mRecycleView.setLayoutManager(linearLayoutManager);
            BmobQuery<Filter> query = new BmobQuery<>();
            query.order("-createdAt");
            query.findObjects(new FindListener<Filter>() {
                @Override
                public void done(List<Filter> list, BmobException e) {
                    if (e == null) {
                        mFilters = list;
                        if (mEditMode == EDIT_MODE_FILTER && mSmallBitmap != null) {
                            mFilterAdapter.notifyDataSetChanged();
                        }
                    }
                }
            });
        }
        {
            mCardAdapter = new CardAdapter();
            BmobQuery<Card> query = new BmobQuery<>();
            query.order("-createdAt");
            query.findObjects(new FindListener<Card>() {
                @Override
                public void done(List<Card> list, BmobException e) {
                    if (e == null) {
                        mCards = list;
                        if (mEditMode == EDIT_MODE_CARD) {
                            mCardAdapter.notifyDataSetChanged();
                        }
                    }
                }
            });
        }
        {
            mFontAdapter = new FontAdapter();
            BmobQuery<Font> query = new BmobQuery<>();
            query.order("-createdAt");
            query.findObjects(new FindListener<Font>() {
                @Override
                public void done(List<Font> list, BmobException e) {
                    if (e == null) {
                        mFonts = list;
                        if (mEditMode == EDIT_MODE_FONT) {
                            mFontAdapter.notifyDataSetChanged();
                        }
                    }
                }
            });
        }
        {
            mBgAdapter = new BgAdapter();
            BmobQuery<Background> query = new BmobQuery<>();
            query.order("-createdAt");
            query.findObjects(new FindListener<Background>() {
                @Override
                public void done(List<Background> list, BmobException e) {
                    if (e == null) {
                        mBgs = list;
                        if (mEditMode == EDIT_MODE_BG) {
                            mBgAdapter.notifyDataSetChanged();
                        }
                    }
                }
            });
        }
        mFilterIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mEditMode = EDIT_MODE_FILTER;
                mRecycleView.setAdapter(mFilterAdapter);
            }
        });
        mCardIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mEditMode = EDIT_MODE_CARD;
                mRecycleView.setAdapter(mCardAdapter);
            }
        });
        mFontIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mEditMode = EDIT_MODE_FONT;
                mRecycleView.setAdapter(mFontAdapter);
            }
        });
        mBgIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mEditMode = EDIT_MODE_BG;
                mRecycleView.setAdapter(mBgAdapter);
            }
        });
        mSavePic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Bitmap bmp = Bitmap.createBitmap(1080, 1920, Bitmap.Config.ARGB_8888);
                bmp.eraseColor(Color.parseColor("#00000000"));
                Canvas canvas = new Canvas(bmp);
//                GPUImage gpuImage = new GPUImage(getApplicationContext());
//                gpuImage.setFilter(null);
//                Bitmap bitmap = BitmapUtils.ensureBitmapSize(mBitmap);
//                gpuImage.setImage(bitmap);
//                bitmap = gpuImage.getBitmapWithFilterApplied(true);
                Bitmap bitmap = mImage.getGPUImage().getBitmapWithFilterApplied(true);
//                canvas.drawBitmap(bitmap, (float)(1080 - bitmap.getWidth()) / 2, (float)(1920 - bitmap.getHeight()) / 2 ,null);
                mPic.draw(canvas);
                Bitmap bmp3 = Bitmap.createBitmap(1080, 1920, Bitmap.Config.ARGB_8888);
                bmp3.eraseColor(Color.parseColor("#00000000"));
                Canvas canvas3 = new Canvas(bmp3);
                mStickerView.draw(canvas3);
                Bitmap bmp2 = Bitmap.createBitmap(1080, 1920, Bitmap.Config.ARGB_8888);
                bmp2.eraseColor(Color.parseColor("#00000000"));
                Canvas canvas2 = new Canvas(bmp2);
                canvas2.drawBitmap(bitmap, (float)(1080 - bitmap.getWidth()) / 2, (float)(1920 - bitmap.getHeight()) / 2 ,null);
                canvas2.drawBitmap(bmp, (float)(1080 - bmp.getWidth()) / 2, (float)(1920 - bmp.getHeight()) / 2 ,null);
                canvas2.drawBitmap(bmp3, (float)(1080 - bmp3.getWidth()) / 2, (float)(1920 - bmp3.getHeight()) / 2 ,null);
                File file = new File(FileUtils.getFileDir(getApplicationContext(), "publish"), "result.png");
                FileOutputStream out = null;
                try {
                    out = new FileOutputStream(file);
                    bmp2.compress(Bitmap.CompressFormat.PNG, 90, out);
                    out.flush();
                    out.close();
                } catch (Exception e) {
                }
                Intent intent = new Intent(getApplicationContext(), PublishActivity.class);
                intent.putExtra("url", file.getAbsolutePath());
                startActivity(intent);
            }
        });
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        mScreenWidth = dm.widthPixels;
        mScreenHeight = dm.heightPixels;
        BitmapStickerIcon deleteIcon = new BitmapStickerIcon(ContextCompat.getDrawable(this,
                R.drawable.sticker_ic_close_white_18dp),
                BitmapStickerIcon.LEFT_TOP);
        deleteIcon.setIconEvent(new DeleteIconEvent());
        mStickerIconWidth = deleteIcon.getWidth();
        BitmapStickerIcon zoomIcon = new BitmapStickerIcon(ContextCompat.getDrawable(this,
                R.drawable.sticker_ic_scale_white_18dp),
                BitmapStickerIcon.RIGHT_BOTOM);
        zoomIcon.setIconEvent(new ZoomIconEvent());

        BitmapStickerIcon flipIcon = new BitmapStickerIcon(ContextCompat.getDrawable(this,
                R.drawable.sticker_ic_flip_white_18dp),
                BitmapStickerIcon.RIGHT_TOP);
        flipIcon.setIconEvent(new FlipHorizontallyEvent());

        BitmapStickerIcon heartIcon =
                new BitmapStickerIcon(ContextCompat.getDrawable(this, R.drawable.ic_favorite_white_24dp),
                        BitmapStickerIcon.LEFT_BOTTOM);
//    heartIcon.setIconEvent(new HelloIconEvent());
        heartIcon.setIconEvent(new StickerIconEvent() {
            @Override
            public void onActionDown(StickerView stickerView, MotionEvent event) {

            }

            @Override
            public void onActionMove(StickerView stickerView, MotionEvent event) {

            }

            @Override
            public void onActionUp(StickerView stickerView, MotionEvent event) {
                showTextInputDialog(false);
            }
        });
        mStickerView.setScreenWidthAndHeight(mScreenWidth, mScreenHeight);
        mStickerView.setDrawableStickerIcons(Arrays.asList(deleteIcon, zoomIcon, flipIcon));
        mStickerView.setTextStickerIcons(Arrays.asList(zoomIcon, deleteIcon, heartIcon));
        mStickerView.setLocked(false);
        mStickerView.setConstrained(true);
        mStickerView.setOnStickerOperationListener(new StickerView.OnStickerOperationListener() {
            @Override
            public void onStickerAdded(@NonNull Sticker sticker) {
            }

            @Override
            public void onStickerClicked(@NonNull Sticker sticker) {
                //stickerView.removeAllSticker();
                if (sticker instanceof TextSticker) {
                    ((TextSticker) sticker).setTextColor(Color.RED);
                    mStickerView.replace(sticker);
                    mStickerView.invalidate();
                }
            }

            @Override
            public void onStickerDeleted(@NonNull Sticker sticker) {
            }

            @Override
            public void onStickerDragFinished(@NonNull Sticker sticker) {
            }

            @Override
            public void onStickerTouchedDown(@NonNull Sticker sticker) {
            }

            @Override
            public void onStickerZoomStart(@NonNull Sticker sticker) {

            }

            @Override
            public void onStickerZoomFinished(@NonNull Sticker sticker) {
            }

            @Override
            public void onStickerFlipped(@NonNull Sticker sticker) {
            }

            @Override
            public void onStickerDoubleTapped(@NonNull Sticker sticker) {
                if (sticker instanceof TextSticker) {
                    showTextInputDialog(false);
                }
            }

            @Override
            public void onStickerNotClicked() {
                mStickerView.invalidate();
            }
        });

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mHandler.removeCallbacksAndMessages(null);
    }

    private void showTextInputDialog(final Boolean isNew) {
        //实例化布局
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_custom_content, null);
        //找到并对自定义布局中的控件进行操作的示例
        final EditText et = view.findViewById(R.id.et_input);
        String hint = "请输入";
        et.setHint(hint);
        if (!isNew) {
            if (mStickerView.getCurrentSticker() instanceof TextSticker) {
                TextSticker textSticker = (TextSticker) mStickerView.getCurrentSticker();
                String text = textSticker.getText();
                et.setText(text);
                et.setSelection(text.length());
            }
        }
        //创建对话框
        final android.support.v7.app.AlertDialog dialog = new android.support.v7.app.AlertDialog.Builder(this).create();
        dialog.setIcon(R.mipmap.ic_launcher);//设置图标
        dialog.setView(view);//添加布局
        //设置按键
        dialog.setButton(android.support.v7.app.AlertDialog.BUTTON_POSITIVE, "确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (et.getText() != null && !TextUtils.isEmpty(et.getText().toString())) {
                    dialog.dismiss();
                    if (isNew) {
                        final TextSticker sticker = new TextSticker(getApplicationContext(), et.getText().toString(), (int) (mScreenWidth - mStickerIconWidth));
                        sticker.setTextColor(Color.BLUE);
                        sticker.setTextAlign(Layout.Alignment.ALIGN_NORMAL);
                        sticker.setDrawableSticker(false);
                        sticker.resizeText();
                        mStickerView.addSticker(sticker);
                    } else {
                        if (mStickerView.getCurrentSticker() instanceof TextSticker) {
                            TextSticker textSticker = (TextSticker) mStickerView.getCurrentSticker();

                            final TextSticker sticker = new TextSticker(getApplicationContext(), et.getText().toString(), (int) (mScreenWidth - mStickerIconWidth));
                            sticker.setTextColor(Color.BLUE);
                            sticker.setTextAlign(Layout.Alignment.ALIGN_NORMAL);
                            sticker.setDrawableSticker(false);
                            sticker.resizeText();
                            mStickerView.remove(textSticker);
                            mStickerView.addSticker(sticker);
                            sticker.setMatrix(textSticker.getMatrix());
                            mStickerView.invalidate();
                        }
                    }
                } else {
                    Toast.makeText(getApplicationContext(), "请输入文字", Toast.LENGTH_SHORT).show();
                }
            }
        });
        dialog.setButton(AlertDialog.BUTTON_NEGATIVE, "取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                hideKeyboard(EditActivity.this);
            }
        });
        dialog.show();

        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                et.setFocusable(true);
                et.setFocusableInTouchMode(true);
                et.requestFocus();
                InputMethodManager inputManager = (InputMethodManager) getApplicationContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                inputManager.showSoftInput(et, 0);
            }
        }, 200);
    }

    public static void hideKeyboard(AppCompatActivity activity) {
        try {
            InputMethodManager inputManager = (InputMethodManager) activity
                    .getSystemService(Context.INPUT_METHOD_SERVICE);
            inputManager.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(),
                    InputMethodManager.HIDE_NOT_ALWAYS);

        } catch (Exception e) {
        }
    }

    @Override
    protected void setMyContentView() {
        setContentView(R.layout.activity_edit);
    }

    class FilterAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            return new FilterViewHolder(inflater.inflate(R.layout.item_edit_filter, parent, false));
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
            if (holder instanceof FilterViewHolder) {
                FilterViewHolder filterViewHolder = (FilterViewHolder)holder;
                AppCompatTextView name = filterViewHolder.mName;
                AppCompatImageView action = filterViewHolder.mAction;
                GPUImageView preview = filterViewHolder.mPreview;
                if (position == 0) {
                    name.setText(R.string.item_store);
                    preview.setVisibility(View.GONE);
                    action.setImageResource(R.drawable.edit_item_store);
                } else if (position == 1){
                    name.setText(R.string.item_edit_mask_none);
                    preview.setVisibility(View.GONE);
                    action.setImageResource(R.drawable.edit_item_none);
                    action.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            mImage.setFilter(new GPUImageFilter());
                        }
                    });
                } else if (position == 2){
                    name.setText(R.string.item_edit_mask_edit);
                    preview.setVisibility(View.GONE);
                    action.setImageResource(R.drawable.edit_item_none);
                } else {
                    final Filter filter = mFilters.get(position - 3);
                    name.setText(filter.getName());
                    action.setVisibility(View.GONE);
                    preview.setImage(mSmallBitmap);
                    final List<FilterItem> filters = FilterFactory.getPortraitFilterItem();
                    preview.setFilter(filters.get(1).instantiate());
                    preview.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            mImage.setFilter(filters.get(1).instantiate());
                        }
                    });
                }
            }
        }

        @Override
        public int getItemCount() {
            return mFilters.size() + 3;
        }
    }

    class FilterViewHolder extends RecyclerView.ViewHolder {
        public AppCompatTextView mName;
        public AppCompatImageView mAction;
        public GPUImageView mPreview;

        public FilterViewHolder(View itemView) {
            super(itemView);
            mName = (AppCompatTextView) itemView.findViewById(R.id.filter_name);
            mAction = (AppCompatImageView) itemView.findViewById(R.id.filter_action);
            mPreview = (GPUImageView) itemView.findViewById(R.id.filter_preview);
        }
    }

    class CardAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            return new CardViewHolder(inflater.inflate(R.layout.item_edit_card, parent, false));
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
            if (holder instanceof CardViewHolder) {
                if (position == 0) {
                    Glide.with(holder.itemView.getContext())
                            .load(R.drawable.edit_item_store)
                            .fitCenter()
                            .diskCacheStrategy(DiskCacheStrategy.RESULT)
                            .into(((CardViewHolder) holder).mCardIcon);
                } else {
                    final Card card = mCards.get(position - 1);
                    String url = card.getPreview().getUrl();
                    Glide.with(holder.itemView.getContext())
                            .load(url)
                            .fitCenter()
                            .diskCacheStrategy(DiskCacheStrategy.RESULT)
                            .into(((CardViewHolder) holder).mCardIcon);
                }
            }
        }

        @Override
        public int getItemCount() {
            return mCards.size() + 1;
        }
    }

    class CardViewHolder extends RecyclerView.ViewHolder {
        public AppCompatImageView mCardIcon;

        public CardViewHolder(View itemView) {
            super(itemView);
            mCardIcon = (AppCompatImageView) itemView.findViewById(R.id.card_icon);
        }
    }

    class FontAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            return new FontViewHolder(inflater.inflate(R.layout.item_edit_font, parent, false));
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
            if (holder instanceof FontViewHolder) {
                if (position == 0) {
                    ((FontViewHolder)holder).mName.setText(R.string.item_store);
                    ((FontViewHolder)holder).mPreview.setImageResource(R.drawable.edit_item_store);
                } else {
                    final Font font = mFonts.get(position - 1);
                    String name = font.getName();
                    ((FontViewHolder)holder).mName.setText(name);
                    String preview = font.getPreview().getUrl();
                    Glide.with(holder.itemView.getContext())
                            .load(preview)
                            .fitCenter()
                            .diskCacheStrategy(DiskCacheStrategy.RESULT)
                            .into(((FontViewHolder) holder).mPreview);
                    ((FontViewHolder) holder).mPreview.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            final TextSticker sticker = new TextSticker(getApplicationContext(), "请输入文字", (int) (mScreenWidth - mStickerIconWidth));
                            sticker.setTextColor(Color.BLUE);
                            sticker.setTextAlign(Layout.Alignment.ALIGN_NORMAL);
                            sticker.setDrawableSticker(false);
                            sticker.resizeText();
                            mStickerView.addSticker(sticker);
                        }
                    });
                }
            }
        }

        @Override
        public int getItemCount() {
            return mFonts.size() + 1;
        }
    }

    class FontViewHolder extends RecyclerView.ViewHolder {
        public AppCompatTextView mName;
        public AppCompatImageView mPreview;

        public FontViewHolder(View itemView) {
            super(itemView);
            mName = (AppCompatTextView) itemView.findViewById(R.id.font_name);
            mPreview = (AppCompatImageView) itemView.findViewById(R.id.font_preview);
        }
    }

    class BgAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            return new BgViewHolder(inflater.inflate(R.layout.item_edit_bg, parent, false));
        }

        @Override
        public int getItemCount() {
            return mBgs.size() + 2;
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
            if (holder instanceof BgViewHolder) {
                if (position == 0) {
                    Glide.with(holder.itemView.getContext())
                            .load(R.drawable.edit_item_store)
                            .fitCenter()
                            .diskCacheStrategy(DiskCacheStrategy.RESULT)
                            .into(((BgViewHolder) holder).mBgIcon);
                } else if (position == 1){
                    Glide.with(holder.itemView.getContext())
                            .load(R.drawable.edit_item_none)
                            .fitCenter()
                            .diskCacheStrategy(DiskCacheStrategy.RESULT)
                            .into(((BgViewHolder) holder).mBgIcon);
                } else {
                    final Background bg = mBgs.get(position - 2);
                    final String url = bg.getContent().getUrl();
                    Glide.with(holder.itemView.getContext())
                            .load(url)
                            .fitCenter()
                            .diskCacheStrategy(DiskCacheStrategy.RESULT)
                            .into(((BgViewHolder) holder).mBgIcon);
                    final BgViewHolder final_holder = (BgViewHolder) holder;
                    ((BgViewHolder) holder).mBgIcon.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Glide.with(final_holder.itemView.getContext())
                                    .load(url)
                                    .fitCenter()
                                    .diskCacheStrategy(DiskCacheStrategy.RESULT)
                                    .into(mFramePic);
                        }
                    });
                }
            }
        }
    }

    class BgViewHolder extends RecyclerView.ViewHolder {
        public AppCompatImageView mBgIcon;

        public BgViewHolder(View itemView) {
            super(itemView);
            mBgIcon = (AppCompatImageView) itemView.findViewById(R.id.bg_icon);
        }
    }
}
