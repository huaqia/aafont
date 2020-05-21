package com.xinmei365.font.social;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.format.Time;
import android.util.Log;
import android.widget.Toast;

import com.xinmei365.font.MyApplication;
import com.xinmei365.font.utils.Constant;
import com.tencent.mm.opensdk.modelmsg.SendAuth;
import com.tencent.mm.opensdk.modelmsg.SendMessageToWX;
import com.tencent.mm.opensdk.modelmsg.WXAppExtendObject;
import com.tencent.mm.opensdk.modelmsg.WXImageObject;
import com.tencent.mm.opensdk.modelmsg.WXMediaMessage;
import com.tencent.mm.opensdk.modelmsg.WXTextObject;
import com.tencent.mm.opensdk.modelmsg.WXWebpageObject;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;

import org.apache.commons.io.IOUtils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.RandomAccessFile;
import java.net.URL;

/**
 * Created by xinmei024 on 15/9/22.
 *
 * @author ningso
 * @Email ningso.ping@gmail.com
 */
public class WechatShare {
    private static final int MAX_DECODE_PICTURE_SIZE = 1920 * 1440;

    public enum Type {
        TEXT, IMAGE, MUSIC, VIDEO, WEBPAGE, APPMSG, EMOJIMSG;
    }

    public enum ImageType {
        RAW, LOCATION, WEBIMAGE;
    }

    private static IWXAPI api;
    private static Type shareType = Type.TEXT;
    private static ImageType imageType = null;
    // shareToFriends true则需要分享到朋友圈
    private static boolean shareToFriends = false;
    private static int thumb_size = 128;
    private static Bitmap shareBimap = null;
    private static String imagePath = null;
    private static String imageUrl = null;
    private static String mPageUrl = null;
    private static String mTitle = null;
    private static String mDesc = null;
    private static String mExtInfo = null;
    private static String mPath = null;
    private static Bitmap mThumbBitmap = null;

    public static class Builder {

        public Builder setType(Type type) {
            shareType = type;
            return this;
        }

        public Builder setToFriends(boolean tofriends) {
            shareToFriends = tofriends;
            return this;
        }

        public Builder setThumbSize(int thumbsize) {
            thumb_size = thumbsize;
            return this;
        }

        public Builder setImageType(ImageType imgtype) {
            imageType = imgtype;
            return this;
        }

        public Builder setBitmap(Bitmap bitmap) {
            shareBimap = bitmap;
            return this;
        }

        public Builder setImagePath(String path) {
            imagePath = path;
            return this;
        }

        public Builder setImageUrl(String url) {
            imageUrl = url;
            return this;
        }

        public Builder setPageUrl(String pageurl) {
            mPageUrl = pageurl;
            return this;
        }

        public Builder setTitle(String title) {
            mTitle = title;
            return this;
        }

        public Builder setPath(String path) {
            mPath = path;
            return this;
        }

        public Builder setDescription(String description) {
            mDesc = description;
            return this;
        }

        public Builder setExtInfo(String extinfo) {
            mExtInfo = extinfo;
            return this;
        }

        public Builder setThumbBitmap(Bitmap thumbbitmap) {
            mThumbBitmap = thumbbitmap;
            return this;
        }

        private WechatShare create() {
            WechatShare wxShare = new WechatShare();
            api = WXAPIFactory.createWXAPI(MyApplication.getInstance(), Constant.WEIXIN_APP_ID, true);
            // 将该app注册到微信
            api.registerApp(Constant.WEIXIN_APP_ID);
            return wxShare;
        }

        public WechatShare share() {
            WechatShare weixinShare = create();
            weixinShare.share();
            return weixinShare;
        }

        public WechatShare login() {
            WechatShare weixinShare = create();
            weixinShare.weixinLogin();
            return weixinShare;
        }
    }

    private boolean shareText(String shareMessage) {
        if (shareMessage == null || shareMessage.length() == 0) {
            return false;
        }
        // 初始化一个WXTextObject对象
        WXTextObject textObj = new WXTextObject();
        textObj.text = shareMessage;

        // 用WXTextObject对象初始化一个WXMediaMessage对象
        WXMediaMessage msg = new WXMediaMessage();
        msg.mediaObject = textObj;
        // 发送文本类型的消息时，title字段不起作用
        // msg.title = "Will be ignored";
        msg.description = shareMessage;

        // 构造一个Req
        SendMessageToWX.Req req = new SendMessageToWX.Req();
        req.transaction = buildTransaction("text"); // transaction字段用于唯一标识一个请求
        req.message = msg;
        req.scene =
                shareToFriends ? SendMessageToWX.Req.WXSceneTimeline
                        : SendMessageToWX.Req.WXSceneSession;

        // 调用api接口发送数据到微信
        return api.sendReq(req);
    }

    private boolean shareRawImage(Bitmap shareBitmap) {
        if (shareBitmap == null) {
            return false;
        }

        WXImageObject wxio = new WXImageObject(shareBitmap);

        return shareImage(wxio, shareBitmap);
    }

    private boolean shareLocalImage(String path) {
        if (path == null) {
            return false;
        }
        File file = new File(path);
        if (!file.exists()) {
            Toast.makeText(MyApplication.getInstance(), "文件不存在", Toast.LENGTH_SHORT).show();
            return false;
        }

        WXImageObject imgObj = new WXImageObject();
        imgObj.setImagePath(path);
        Bitmap bmp = BitmapFactory.decodeFile(path);

        return shareImage(imgObj, bmp);
    }

    private boolean shareWebImage(String url) {
        try {
            WXImageObject imgObj = new WXImageObject();
            imgObj.imagePath = url;
            Bitmap bmp = BitmapFactory.decodeStream(new URL(url).openStream());
            return shareImage(imgObj, bmp);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private boolean shareImage(WXImageObject wxio, Bitmap tempBitmap) {

        WXMediaMessage wxmm = new WXMediaMessage();
        wxmm.mediaObject = wxio;
        wxmm.description = "字体管家";

        Bitmap thumbBmp = Bitmap.createScaledBitmap(tempBitmap, thumb_size, thumb_size, true);
        tempBitmap.recycle();
        wxmm.thumbData = bmpToByteArray(thumbBmp, true); // 设置缩略图

        SendMessageToWX.Req req = new SendMessageToWX.Req();
        req.transaction = buildTransaction("img");
        req.message = wxmm;
        req.scene =
                shareToFriends ? SendMessageToWX.Req.WXSceneTimeline
                        : SendMessageToWX.Req.WXSceneSession;
        return api.sendReq(req);
    }

    private boolean shareWebPage(String pageUrl, String title, String desc, Bitmap thumbBitmap) {
        if (thumbBitmap == null) {
            return false;
        }

        WXWebpageObject webpage = new WXWebpageObject();
        webpage.webpageUrl = pageUrl;
        WXMediaMessage msg = new WXMediaMessage(webpage);
        msg.title = title;
        msg.description = desc;
        msg.thumbData = bmpToByteArray(thumbBitmap, true);

        SendMessageToWX.Req req = new SendMessageToWX.Req();
        req.transaction = buildTransaction("webpage");
        req.message = msg;
        req.scene =
                shareToFriends ? SendMessageToWX.Req.WXSceneTimeline
                        : SendMessageToWX.Req.WXSceneSession;
        return api.sendReq(req);
    }

    private boolean shareAPPMessage(String title, String desc, String extinfo, String path) {
        final WXAppExtendObject appdata = new WXAppExtendObject();
        appdata.fileData = readFromFile(path, 0, -1);
        appdata.extInfo = extinfo;

        final WXMediaMessage msg = new WXMediaMessage();
        msg.setThumbImage(extractThumbNail(path, thumb_size, thumb_size, true));
        msg.title = title;
        msg.description = desc;
        msg.mediaObject = appdata;

        SendMessageToWX.Req req = new SendMessageToWX.Req();
        req.transaction = buildTransaction("appdata");
        req.message = msg;
        req.scene =
                shareToFriends ? SendMessageToWX.Req.WXSceneTimeline
                        : SendMessageToWX.Req.WXSceneSession;
        return api.sendReq(req);
    }

    private boolean share() {
        if (shareType == null) {
            return false;
        }

        if (shareType == Type.TEXT) {
            return shareText(mTitle);
        } else if (shareType == Type.IMAGE) {
            if (imageType == ImageType.RAW) {
                return shareRawImage(shareBimap);
            } else if (imageType == ImageType.LOCATION) {
                return shareLocalImage(imagePath);
            } else if (imageType == ImageType.WEBIMAGE) {
                return shareWebImage(imageUrl);
            } else {
                return false;
            }
        } else if (shareType == Type.WEBPAGE) {
            return shareWebPage(mPageUrl, mTitle, mDesc, mThumbBitmap);
        } else if (shareType == Type.APPMSG) {
            return shareAPPMessage(mTitle, mDesc, mExtInfo, mPath);
        }

        return false;
    }

    public void weixinLogin() {

        SendAuth.Req req = new SendAuth.Req();
        req.scope = "snsapi_userinfo";
        req.state = createState();
        api.sendReq(req);
    }

    public static String createState() {
        Time time = new Time();
        time.setToNow();
        String wxstateparam = "wx" + time.year + time.month + MyApplication.getInstance().getPackageName();
        String wxstate = String.valueOf("font" + wxstateparam.hashCode());
        return wxstate;

    }
    public static String buildTransaction(final String type) {
        return (type == null) ? String.valueOf(System.currentTimeMillis()) : type + System.currentTimeMillis();
    }

    public static byte[] bmpToByteArray(final Bitmap bmp, final boolean needRecycle) {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.PNG, 100, output);
        if (needRecycle) {
            bmp.recycle();
        }

        byte[] result = output.toByteArray();
        try {
            output.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }

    public static Bitmap extractThumbNail(final String path, final int height, final int width, final boolean crop) {
        BitmapFactory.Options options = new BitmapFactory.Options();

        try {
            options.inJustDecodeBounds = true;
            Bitmap tmp = BitmapFactory.decodeFile(path, options);
            if (tmp != null) {
                tmp.recycle();
                tmp = null;
            }

            Log.d("extractThumbNail", "extractThumbNail: round=" + width + "x" + height + ", crop=" + crop);
            final double beY = options.outHeight * 1.0 / height;
            final double beX = options.outWidth * 1.0 / width;
            Log.d("extractThumbNail", "extractThumbNail: extract beX = " + beX + ", beY = " + beY);
            options.inSampleSize = (int) (crop ? (beY > beX ? beX : beY) : (beY < beX ? beX : beY));
            if (options.inSampleSize <= 1) {
                options.inSampleSize = 1;
            }

            // NOTE: out of memory error
            while (options.outHeight * options.outWidth / options.inSampleSize > MAX_DECODE_PICTURE_SIZE) {
                options.inSampleSize++;
            }

            int newHeight = height;
            int newWidth = width;
            if (crop) {
                if (beY > beX) {
                    newHeight = (int) (newWidth * 1.0 * options.outHeight / options.outWidth);
                } else {
                    newWidth = (int) (newHeight * 1.0 * options.outWidth / options.outHeight);
                }
            } else {
                if (beY < beX) {
                    newHeight = (int) (newWidth * 1.0 * options.outHeight / options.outWidth);
                } else {
                    newWidth = (int) (newHeight * 1.0 * options.outWidth / options.outHeight);
                }
            }

            options.inJustDecodeBounds = false;

            Log.i("extractThumbNail", "bitmap required size=" + newWidth + "x" + newHeight + ", orig=" + options.outWidth + "x" + options.outHeight + ", sample=" + options.inSampleSize);
            Bitmap bm = BitmapFactory.decodeFile(path, options);
            if (bm == null) {
                Log.e("extractThumbNail", "bitmap decode failed");
                return null;
            }

            Log.i("extractThumbNail", "bitmap decoded size=" + bm.getWidth() + "x" + bm.getHeight());
            final Bitmap scale = Bitmap.createScaledBitmap(bm, newWidth, newHeight, true);
            if (scale != null) {
                bm.recycle();
                bm = scale;
            }

            if (crop) {
                final Bitmap cropped = Bitmap.createBitmap(bm, (bm.getWidth() - width) >> 1, (bm.getHeight() - height) >> 1, width, height);
                if (cropped == null) {
                    return bm;
                }

                bm.recycle();
                bm = cropped;
                Log.i("extractThumbNail", "bitmap croped size=" + bm.getWidth() + "x" + bm.getHeight());
            }
            return bm;

        } catch (final OutOfMemoryError e) {
            Log.e("extractThumbNail", "decode bitmap failed: " + e.getMessage());
            options = null;
        }

        return null;
    }

    public static byte[] readFromFile(String fileName, int offset, int len) {
        if (fileName == null) {
            return null;
        }

        File file = new File(fileName);
        if (!file.exists()) {
            Log.i("readFromFile", "readFromFile: file not found");
            return null;
        }

        if (len == -1) {
            len = (int) file.length();
        }

        Log.d("readFromFile", "readFromFile : offset = " + offset + " len = " + len + " offset + len = " + (offset + len));

        if (offset < 0) {
            Log.e("readFromFile", "readFromFile invalid offset:" + offset);
            return null;
        }
        if (len <= 0) {
            Log.e("readFromFile", "readFromFile invalid len:" + len);
            return null;
        }
        if (offset + len > (int) file.length()) {
            Log.e("readFromFile", "readFromFile invalid file len:" + file.length());
            return null;
        }

        byte[] b = null;
        RandomAccessFile in = null;
        try {
            in = new RandomAccessFile(fileName, "r");
            b = new byte[len]; // 创建合适文件大小的数组
            in.seek(offset);
            in.readFully(b);
            in.close();

        } catch (Exception e) {
            Log.e("readFromFile", "readFromFile : errMsg = " + e.getMessage());
            e.printStackTrace();
        } finally {
            IOUtils.closeQuietly(in);
        }
        return b;
    }

}
