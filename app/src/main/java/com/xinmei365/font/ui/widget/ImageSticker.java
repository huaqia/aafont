package com.xinmei365.font.ui.widget;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.support.annotation.IntRange;
import android.support.annotation.NonNull;
import android.util.Pair;

import com.xinmei365.font.model.StickerFactory;

import java.util.List;

public class ImageSticker extends Sticker {

    private Activity context;
    private  String stickerName;
    private Drawable drawable;
    private Rect realBounds;
    private int screenWidth;
    private int stickerId;

    public ImageSticker(Activity context, final String stickerName, final int stickerId, int screenWidth){
        this.context = context;
        this.screenWidth = screenWidth;
        this.stickerId = stickerId;
        this.stickerName = stickerName;
        if(stickerId != -1){
            this.drawable = context.getResources().getDrawable(stickerId);
        } else {
            String[] names = stickerName.split("_");
            List<Pair<String, List<Pair<String, Integer>>>> stickerss = StickerFactory.getStickerss();
            for (Pair<String, List<Pair<String, Integer>>> stickers : stickerss) {
                if (stickers.first.equals(names[0])) {
                    int index = Integer.parseInt(names[1]) - 1;
                    this.drawable = context.getResources().getDrawable(stickers.second.get(index).second);
                }
            }
        }
        realBounds = new Rect(0, 0, getWidth(), getHeight());
    }
    @NonNull
    @Override public Drawable getDrawable() {
        return drawable;
    }

    @Override public ImageSticker setDrawable(@NonNull Drawable drawable) {
        this.drawable = drawable;
        return this;
    }

    @Override public void draw(@NonNull Canvas canvas) {
        canvas.save();
        canvas.concat(getMatrix());
        drawable.setBounds(realBounds);
        drawable.draw(canvas);
        canvas.restore();
    }

    @NonNull @Override public ImageSticker setAlpha(@IntRange(from = 0, to = 255) int alpha) {
        drawable.setAlpha(alpha);
        return this;
    }

    @Override public int getWidth() {
        return screenWidth/4;
    }

    @Override public int getHeight() {
        return screenWidth/4;
    }

    @Override
    public int getMinWidth() {
        return screenWidth/10;
    }

    @Override
    public int getMinHeight() {
        return 0;
    }

    @Override public void release() {
        super.release();
        if (drawable != null) {
            drawable = null;
        }
    }

    public String getStickerName() {
        return stickerName;
    }
}
