package com.xinmei365.font.ui.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.AppCompatEditText;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.xinmei365.font.MyApplication;
import com.xinmei365.font.R;
import com.xinmei365.font.model.DraftData;
import com.xinmei365.font.model.EffectData;
import com.xinmei365.font.model.Note;
import com.xinmei365.font.model.User;
import com.xinmei365.font.ui.widget.StickerView;
import com.xinmei365.font.utils.BackendUtils;
import com.xinmei365.font.utils.BitmapUtils;
import com.xinmei365.font.utils.FileUtils;
import com.xinmei365.font.utils.MiscUtils;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import butterknife.BindView;
import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.BmobUser;
import cn.bmob.v3.datatype.BmobFile;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FindListener;
import cn.bmob.v3.listener.SaveListener;
import cn.bmob.v3.listener.UpdateListener;
import cn.bmob.v3.listener.UploadBatchListener;

public class PublishActivity extends BaseActivity {
    @BindView(R.id.image_select_close)
    AppCompatImageView mClose;
    @BindView(R.id.pics)
    RecyclerView mPics;
    @BindView(R.id.title_string)
    AppCompatEditText mTitle;
    @BindView(R.id.intro_string)
    AppCompatEditText mIntro;
    @BindView(R.id.btn_publish_note)
    AppCompatButton mPublishNote;
    @BindView(R.id.add_tag)
    AppCompatImageView mAddTag;
    @BindView(R.id.select_type)
    RelativeLayout mSelectType;
    @BindView(R.id.note_type)
    AppCompatTextView mNoteType;
    @BindView(R.id.save_draft)
    AppCompatButton mSaveDraft;
    @BindView(R.id.save_album_btn)
    AppCompatImageView mSaveAlbumBtn;
    @BindView(R.id.save_album_text)
    AppCompatTextView mSaveAlbumText;

    private PicAdapter mAdapter;
    private ArrayList<String> mUrls;
    private ArrayList<String> mSavedUrls;
    private ArrayList<EffectData> mEffectDatas;
    private int mDraftIndex;
    private String mNoteId;
    private boolean mSaveAlbum;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        mUrls = getIntent().getStringArrayListExtra("urls");
        mSavedUrls = getIntent().getStringArrayListExtra("savedUrls");
        mEffectDatas = (ArrayList<EffectData>)getIntent().getSerializableExtra("effect");
        mDraftIndex = getIntent().getIntExtra("draftIndex", -1);
        mAdapter = new PicAdapter();
        mPics.setAdapter(mAdapter);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        mPics.setLayoutManager(linearLayoutManager);
        final String title = getIntent().getExtras().getString("title");
        mTitle.setText(title);
        final String intro = getIntent().getExtras().getString("intro");
        mIntro.setText(intro);
        final String type = getIntent().getExtras().getString("type");
        if (type != null) {
            mNoteType.setText(type);
        } else {
            mNoteType.setText("字体");
        }
        mNoteId = getIntent().getExtras().getString("noteId");
        mSelectType.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivityForResult(new Intent(getApplicationContext(), TypeSelectActivity.class), 100);
            }
        });
        mPublishNote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mSaveAlbum) {
                    for (String url : mSavedUrls) {
                        ContentValues values = new ContentValues();
                        values.put(MediaStore.Images.Media.DATA, url);
                        values.put(MediaStore.Images.Media.MIME_TYPE, "image/png");
                        Uri uri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
                        Intent scannerIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, uri);
                        sendBroadcast(scannerIntent);
                    }
                }
                showProgressBar(true, "笔记上传中...");
                BmobFile.uploadBatch(mSavedUrls.toArray(new String[]{}), new UploadBatchListener() {
                    @Override
                    public void onSuccess(List<BmobFile> files, final List<String> urls) {
                        if (urls.size() == mSavedUrls.size()) {
                            if (mNoteId == null) {
                                Note note = new Note();
                                User currentUser = BmobUser.getCurrentUser(User.class);
                                note.setUser(currentUser);
                                fillNoteData(note, (ArrayList<String>) urls);
                                note.save(new SaveListener<String>() {
                                    @Override
                                    public void done(String s, BmobException e) {
                                        if (e == null) {
                                            showProgressBar(false);
                                            LocalBroadcastManager.getInstance(PublishActivity.this).sendBroadcast(new Intent("android.intent.action.NOTE_LIST_CHANGE"));
                                            MiscUtils.makeToast(PublishActivity.this, "发布成功！", false);
                                            finish();
                                        } else {
                                            BackendUtils.handleException(e, PublishActivity.this);
                                        }
                                    }
                                });
                            } else {
                                BmobQuery<Note> query = new BmobQuery<>();
                                query.addWhereEqualTo("objectId", mNoteId);
                                query.findObjects(new FindListener<Note>() {
                                    @Override
                                    public void done(List<Note> list, BmobException e) {
                                        if (e == null) {
                                            if (list.size() == 1) {
                                                Note note = list.get(0);
                                                fillNoteData(note, (ArrayList<String>) urls);
                                                note.update(new UpdateListener() {
                                                    @Override
                                                    public void done(BmobException e) {
                                                        if (e == null) {
                                                            showProgressBar(false);
                                                            LocalBroadcastManager.getInstance(PublishActivity.this).sendBroadcast(new Intent("android.intent.action.NOTE_LIST_CHANGE"));
                                                            MiscUtils.makeToast(PublishActivity.this, "发布成功！", false);
                                                            finish();
                                                        } else {
                                                            BackendUtils.handleException(e, PublishActivity.this);
                                                        }
                                                    }
                                                });
                                            }
                                        } else {
                                            BackendUtils.handleException(e, PublishActivity.this);
                                        }
                                    }
                                });
                            }
                        }
                    }

                    @Override
                    public void onProgress(int curIndex, int curPercent, int total,
                                           int totalPercent) {
                    }

                    @Override
                    public void onError(int statuscode, String errormsg) {
                        showProgressBar(false);
                    }
                });
            }
        });
        mSaveDraft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MiscUtils.showAskDialog(PublishActivity.this, "确定要保存笔记到草稿？" , new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        DraftData noteData = new DraftData();
                        noteData.setTitle(mTitle.getText().toString());
                        noteData.setIntro(mIntro.getText().toString());
                        noteData.setType(mNoteType.getText().toString());
                        noteData.setTime(new SimpleDateFormat("MM-dd HH:mm").format(new Date()));
                        noteData.setUrls(mUrls);
                        noteData.setSavedUrls(mSavedUrls);
                        if (mEffectDatas == null) {
                            mEffectDatas = new ArrayList<>();
                        }
                        noteData.setEffectDatas(mEffectDatas);
                        File jsonFile = new File(FileUtils.getFileDir(getApplicationContext(), "note"), "note.json");
                        Gson gson = new Gson();
                        ArrayList<DraftData> noteDatas = new ArrayList<>();
                        if (jsonFile.exists()) {
                            String json = FileUtils.readFileToString(jsonFile);
                            if (json != null) {
                                noteDatas = gson.fromJson(json, new TypeToken<ArrayList<DraftData>>(){}.getType());
                            }
                        }
                        if (noteDatas != null) {
                            Collections.sort(noteDatas, new Comparator<DraftData>() {
                                @Override
                                public int compare(DraftData o1, DraftData o2) {
                                    if (o2.getTime() != null && o1.getTime() != null) {
                                        return o2.getTime().compareTo(o1.getTime());
                                    } else {
                                        return 0;
                                    }
                                }
                            });
                            if (mDraftIndex == -1) {
                                noteDatas.add(noteData);
                            } else {
                                noteDatas.remove(mDraftIndex);
                                noteDatas.add(mDraftIndex, noteData);
                            }
                        }
                        String json = gson.toJson(noteDatas);
                        FileUtils.saveStringToFile(json, jsonFile);
                        finish();
                        MiscUtils.makeToast(PublishActivity.this, "保存草稿成功！", false);
                    }
                });
            }
        });
        mAddTag.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(PublishActivity.this, LabelActivity.class);
                startActivityForResult(intent, 500);
            }
        });
        mSaveAlbum = false;
        mSaveAlbumBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mSaveAlbum) {
                    mSaveAlbum = false;
                    mSaveAlbumBtn.setImageResource(R.drawable.ic_publish_save_album_unselected);
                    mSaveAlbumText.setTextColor(getResources().getColor(R.color.colorNormalState));
                } else {
                    mSaveAlbum = true;
                    mSaveAlbumBtn.setImageResource(R.drawable.ic_publish_save_album_selected);
                    mSaveAlbumText.setTextColor(getResources().getColor(R.color.colorActiveState));
                }
            }
        });
        mSaveAlbumText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
    }

    private void fillNoteData(Note note, ArrayList<String> urls) {
        float maxRatio = -1f;
        float firstRatio = -1f;
        float limitRatio = (float)4 / 3;
        for (int i = 0; i < mSavedUrls.size(); i++) {
            String saveUrl = mSavedUrls.get(i);
            float picRatio = BitmapUtils.getBitmapRatio(saveUrl);
            if (i == 0) {
                firstRatio = picRatio;
            }
            if (Float.compare(picRatio, limitRatio) > 0) {
                maxRatio = limitRatio;
                break;
            }
            if (Float.compare(picRatio, maxRatio) > 0) {
                maxRatio = picRatio;
            }
        }
        note.setTitle(mTitle.getText().toString());
        note.setIntro(mIntro.getText().toString());
        note.setType(mNoteType.getText().toString());
        ArrayList<ArrayList<EffectData.TagData>> tagDatas = new ArrayList<>();
        if (mEffectDatas != null) {
            for (EffectData effectData : mEffectDatas) {
                tagDatas.add(effectData.getTagDatas());
            }
        }
        note.setTagData(tagDatas);
        note.setMaxRatio(Float.toString(maxRatio));
        note.setFirstRatio(Float.toString(firstRatio));
        note.setPics(urls);
    }
    @Override
    protected void setMyContentView() {
        setContentView(R.layout.activity_publish);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != Activity.RESULT_OK) {
            return;
        }
        if (requestCode == 100) {
            String type = data.getStringExtra("type");
            mNoteType.setText(type);
        } else if (requestCode == 200) {
            ArrayList<String> urls = data.getStringArrayListExtra("urls");
            if (urls != null) {
                Intent intent = new Intent(getApplicationContext(), EditActivity.class);
                intent.putExtra("urls", urls);
                intent.putExtra("add", true);
                startActivityForResult(intent, 300);
            }
        } else if (requestCode == 300) {
            ArrayList<String> urls = data.getStringArrayListExtra("urls");
            ArrayList<String> savedUrls = data.getStringArrayListExtra("savedUrls");
            if (urls != null) {
                ArrayList<EffectData> effectDatas = (ArrayList<EffectData>)data.getSerializableExtra("effect");
                if (mEffectDatas == null) {
                    mEffectDatas = new ArrayList<>();
                }
                mEffectDatas.addAll(effectDatas);
                mUrls.addAll(urls);
                mSavedUrls.addAll(savedUrls);
                mAdapter.notifyDataSetChanged();
            }
        } else if (requestCode == 400) {
            ArrayList<String> urls = data.getStringArrayListExtra("urls");
            ArrayList<String> savedUrls = data.getStringArrayListExtra("savedUrls");
            if (urls != null) {
                ArrayList<EffectData> effectDatas = (ArrayList<EffectData>)data.getSerializableExtra("effect");
                if (mEffectDatas == null) {
                    mEffectDatas = new ArrayList<>();
                }
                mEffectDatas.clear();
                mEffectDatas.addAll(effectDatas);
                mUrls.clear();
                mUrls.addAll(urls);
                mSavedUrls.clear();
                mSavedUrls.addAll(savedUrls);
                mAdapter.notifyDataSetChanged();
            }
        } else if (requestCode == 500) {
            String text = "#" + data.getStringExtra("text");
            int index = mIntro.getSelectionStart();
            Editable edit = mIntro.getEditableText();
            if (index < 0 || index >= edit.length()) {
                edit.append(text);
            } else {
                edit.insert(index,text);
            }
        }
    }

    public class PicAdapter extends RecyclerView.Adapter<PicAdapter.PicHolder> {
        @NonNull
        @Override
        public PicHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            return new PicHolder(inflater.inflate(R.layout.item_publish_pic, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull final PicHolder holder, @SuppressLint("RecyclerView") final int position) {
            if (position < mSavedUrls.size()) {
//                holder.image.setOnMeasureListener(new GPUImageView.OnMeasureListener() {
//                    @Override
//                    public void onMeasure(int width, int height) {
//                        if (mEffectDatas != null) {
//                            EffectData effectData = mEffectDatas.get(position);
//                            if (effectData != null) {
//                                String filter = effectData.getFilter();
//                                List<FilterItem> filters = FilterFactory.getPortraitFilterItem();
//                                if (filter != null) {
//                                    int filterIndex = 0;
//                                    for (int i = 0; i < filters.size(); i++) {
//                                        FilterItem item = filters.get(i);
//                                        if (item.mName.equals(filter)) {
//                                            filterIndex = i;
//                                            break;
//                                        }
//                                    }
//                                    holder.image.setFilter(filters.get(filterIndex).instantiate());
//                                }
//                                ArrayList<EffectData.StickerData> stickerDatas = effectData.getStickerDatas();
//                                if (stickerDatas != null) {
//                                    ArrayList<Sticker> stickers = new ArrayList<>();
//                                    int bigImageWidth = DensityUtils.getScreenW(PublishActivity.this);
//                                    for (EffectData.StickerData stickerData : stickerDatas) {
//                                        ImageSticker sticker = new ImageSticker(PublishActivity.this, 0, "", stickerData.index, width);
//                                        float transX = stickerData.matrixValues[Matrix.MTRANS_X];
//                                        float transY = stickerData.matrixValues[Matrix.MTRANS_Y];
//                                        float[] values = stickerData.matrixValues.clone();
//                                        values[Matrix.MTRANS_X] = transX * width / bigImageWidth;
//                                        Matrix matrix = new Matrix();
//                                        matrix.setValues(values);
//                                        sticker.setMatrix(matrix);
//                                        stickers.add(sticker);
//                                    }
//                                    holder.stickerView.clearStickerIcons();
//                                    holder.stickerView.setStickers(stickers);
//                                }
//                            }
//                        }
//                    }
//                });
//                holder.image.setImage(mUrls.get(position));
                Glide.with(MyApplication.getInstance())
                        .load(mSavedUrls.get(position))
                        .fitCenter()
                        .skipMemoryCache(true)
                        .diskCacheStrategy(DiskCacheStrategy.NONE)
                        .into(holder.image);
                holder.btn.setVisibility(View.VISIBLE);
                holder.btn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (mSavedUrls.size() > 1) {
                            mUrls.remove(position);
                            mSavedUrls.remove(position);
                            mAdapter.notifyDataSetChanged();
                        } else {
                            MiscUtils.makeToast(PublishActivity.this, "至少要有一张图片哟", false);
                        }
                    }
                });
                holder.image.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(getApplicationContext(), EditActivity.class);
                        intent.putExtra("urls", mUrls);
                        intent.putExtra("savedUrls", mSavedUrls);
                        intent.putExtra("modify", true);
                        intent.putExtra("startIdx", position);
                        if (mEffectDatas == null) {
                            mEffectDatas = new ArrayList<>();
                        }
                        intent.putExtra("effect", mEffectDatas);
                        startActivityForResult(intent, 400);
                    }
                });
            } else {
//                Uri imageUri = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://"
//                        + getResources().getResourcePackageName(R.drawable.add_pic) + "/"
//                        + getResources().getResourceTypeName(R.drawable.add_pic) + "/"
//                        + getResources().getResourceEntryName(R.drawable.add_pic));
//                holder.image.setImage(imageUri);
                holder.image.setImageResource(R.drawable.ic_publish_pic_add);
                holder.image.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (mSavedUrls.size() >= 9) {
                            MiscUtils.makeToast(PublishActivity.this, "最多只能选择9张图片", false);
                        } else {
                            Intent intent = new Intent(getApplicationContext(), ImageSelectActivity.class);
                            intent.putExtra("album", "Camera");//Environment.DIRECTORY_DCIM);
                            intent.putExtra("start", mSavedUrls.size());
                            startActivityForResult(intent, 200);
                        }
                    }
                });
                holder.btn.setVisibility(View.GONE);
            }
        }

        @Override
        public int getItemCount() {
            return mSavedUrls.size() + 1;
        }

        class PicHolder extends RecyclerView.ViewHolder {
//            public GPUImageView image;
            public ImageView image;
            public StickerView stickerView;
            public ImageView btn;

            public PicHolder(View view) {
                super(view);
//                image = (GPUImageView) view.findViewById(R.id.iv);
                image = (ImageView) view.findViewById(R.id.iv);
                btn = (ImageView) view.findViewById(R.id.child_delete);
//                image.setScaleType(GPUImage.ScaleType.CENTER_CROP);
                image.setScaleType(ImageView.ScaleType.CENTER_CROP);
                stickerView = (StickerView)view.findViewById(R.id.sticker_view);
            }
        }
    }
}
