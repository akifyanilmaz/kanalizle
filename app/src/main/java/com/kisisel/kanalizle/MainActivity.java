package com.kisisel.kanalizle;

import android.app.Activity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;

public class MainActivity extends Activity {
    WebView web;
    FrameLayout root;
    View customView;
    WebChromeClient.CustomViewCallback callback;

    static final String FULLSCREEN_JS =
        "(function(){var n=0;var t=setInterval(function(){"
      + "var v=document.querySelector('video');"
      + "if(v){try{v.muted=false;v.play();}catch(e){}"
      + "if(!document.getElementById('__kf')){var s=document.createElement('style');s.id='__kf';"
      + "s.textContent='video{position:fixed!important;top:0!important;left:0!important;"
      + "width:100vw!important;height:100vh!important;max-width:none!important;max-height:none!important;"
      + "z-index:2147483647!important;object-fit:contain!important;background:#000!important}';"
      + "document.head.appendChild(s);}}"
      + "if(++n>40)clearInterval(t);},1000);})();";

    static final String PLAY_JS =
        "(function(){var v=document.querySelector('video');if(v){try{v.muted=false;v.play();}catch(e){}}})();";

    boolean onList() {
        String u = web.getUrl();
        return u == null || u.startsWith("file:");
    }

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        root = new FrameLayout(this);
        root.setBackgroundColor(0xFF000000);
        web = new WebView(this);
        root.addView(web, new FrameLayout.LayoutParams(-1, -1));
        setContentView(root);

        WebSettings s = web.getSettings();
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);
        s.setMediaPlaybackRequiresUserGesture(false);
        s.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);

        web.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView v, String url) {
                if (url != null && !url.startsWith("file:")) {
                    v.evaluateJavascript(FULLSCREEN_JS, null);
                }
            }
        });
        web.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onShowCustomView(View v, CustomViewCallback cb) {
                customView = v; callback = cb;
                root.addView(v);
                web.setVisibility(View.GONE);
            }
            @Override
            public void onHideCustomView() {
                if (customView == null) return;
                root.removeView(customView);
                customView = null;
                web.setVisibility(View.VISIBLE);
                if (callback != null) callback.onCustomViewHidden();
            }
        });
        web.loadUrl("file:///android_asset/index.html");
        web.requestFocus();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent e) {
        if (!onList() && customView == null &&
            (keyCode == KeyEvent.KEYCODE_DPAD_CENTER || keyCode == KeyEvent.KEYCODE_ENTER
             || keyCode == KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE)) {
            web.evaluateJavascript(PLAY_JS, null);
            web.evaluateJavascript(FULLSCREEN_JS, null);
        }
        return super.onKeyDown(keyCode, e);
    }

    @Override
    public void onBackPressed() {
        if (customView != null) {
            web.getWebChromeClient().onHideCustomView();
        } else if (web.canGoBack()) {
            web.goBack();
        } else {
            super.onBackPressed();
        }
    }
}
