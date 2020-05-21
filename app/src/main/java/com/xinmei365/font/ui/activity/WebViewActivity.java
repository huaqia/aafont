package com.xinmei365.font.ui.activity;

import android.content.Intent;
import android.net.http.SslError;
import android.os.Bundle;
import android.webkit.SslErrorHandler;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.xinmei365.font.R;

import butterknife.BindView;

public class WebViewActivity extends BaseActivity {
    @BindView(R.id.webView)
    WebView mWebView;

    @Override
    protected void setMyContentView() {
        setContentView(R.layout.activity_webview);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        String url = intent.getStringExtra("url");
        mWebView.setWebViewClient(new WebViewClient(){
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return true;
            }
            @Override
            public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
                //handler.cancel(); // Android默认的处理方式
                handler.proceed();  // 接受所有网站的证书
                //handleMessage(Message msg); // 进行其他处理
            }
        });
        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.getSettings().setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        mWebView.loadUrl(url);
    }
}