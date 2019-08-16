package com.hanmei.aafont.ui.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;

import com.hanmei.aafont.R;
import com.hanmei.aafont.utils.BackendUtils;
import com.hanmei.aafont.video.WindowSurface;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import butterknife.BindView;
import butterknife.OnClick;

public class SplashActivity extends BaseActivity {
    @BindView(R.id.splash_bg)
    ImageView mSplashBg;

    private static final String sUrl = "http://upaicdn.xinmei365.com/newwfs/support/Splash.jpg";

    private Handler mHandler = new Handler();
    private Runnable mGotoMainRunnable = new Runnable() {
        @Override
        public void run() {
            jumpinto();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        int flag = WindowManager.LayoutParams.FLAG_FULLSCREEN;
        getWindow().setFlags(flag , flag);
        new FetchDataTask().execute(sUrl);
        mHandler.postDelayed(mGotoMainRunnable, 3000);
    }

    @Override
    protected void setMyContentView() {
        setContentView(R.layout.layout_launcher);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mHandler.removeCallbacks(mGotoMainRunnable);
    }

    @OnClick(R.id.slip_bt)
    public void slipClick()
    {
        jumpinto();
    }

    private synchronized void jumpinto() {
        if (BackendUtils.getCurrentUser() != null) {
            startActivity(new Intent(this, MainActivity.class));
        } else {
            startActivity(new Intent(this, LoginActivity.class));
        }
        finish();
    }

    public class FetchDataTask extends AsyncTask<String, Void, Bitmap> {
        @Override
        protected Bitmap doInBackground(String... strings) {
            Bitmap bitmap = null;
            try {
                HttpURLConnection connection = (HttpURLConnection)(new URL(strings[0])).openConnection();
                connection.setConnectTimeout(100);
                connection.setReadTimeout(100);
                InputStream is = connection.getInputStream();
                bitmap = BitmapFactory.decodeStream(is);
            } catch (IOException e) {
                e.printStackTrace();
            }

            return bitmap;
        }

        @Override
        protected void onPostExecute(Bitmap result) {
            super.onPostExecute(result);
            if (mSplashBg != null) {
                if (result != null) {
                    mSplashBg.setImageBitmap(result);
                } else {
                    // mSplashBg.setImageResource(R.mipmap.launcher_logo);
                }
            }
        }
    }

}
