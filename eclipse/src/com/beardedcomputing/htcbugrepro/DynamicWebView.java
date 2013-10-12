package com.beardedcomputing.htcbugrepro;

import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.Color;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebSettings.PluginState;
import android.widget.LinearLayout;
import android.util.Log;

@SuppressLint("SetJavaScriptEnabled")
public class DynamicWebView extends Activity {
	private Handler handler;
	private WebView webview;
	private static final String TAG = "DynamicWebView";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_dynamic_web_view);
		
		handler = new Handler();
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.dynamic_web_view, menu);
		return true;
	}
	
	@SuppressWarnings("deprecation")
	@Override
	public void onStart() {
		super.onStart();
		
		if (webview != null) {
			return;
		}
		
		this.findViewById(R.id.progressBar1).setVisibility(View.GONE);
		
		webview = instantiateWebView();
		disableHardwareAccel(webview);
		webview.resumeTimers();
		
		LinearLayout layout = (LinearLayout) this.findViewById(R.id.webviewLayout);
		layout.addView(webview, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
		layout.getRootView().requestLayout();
		
		webview.setBackgroundColor(Color.WHITE);
       	webview.getSettings().setJavaScriptEnabled(true); // receives warning about XSS vuln., suppressed above
        webview.getSettings().setLoadWithOverviewMode(true);
        webview.getSettings().setUseWideViewPort(true);
        webview.getSettings().setSavePassword(false); // deprecated in 18 due to password save going away in 19
        webview.getSettings().setSaveFormData(false);
        webview.getSettings().setJavaScriptCanOpenWindowsAutomatically(false);
        webview.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
        webview.getSettings().setSupportMultipleWindows(false);
        webview.getSettings().setPluginState(PluginState.OFF); // deprecated in 18 due to plugins going away in 19
        webview.getSettings().setBuiltInZoomControls(true);
        webview.getSettings().setDisplayZoomControls(false);
        
        handler.post(new Runnable() {
			@Override
        	public void run() {
        		webview.loadUrl("file:///android_asset/index.html");
        	}
        });
        
        Log.d(TAG, "added WebView to layout, loading page");
	}
    
    private void enableHardwareAccel() {
		Log.i(TAG, "enabling hardware accel. for window");
		this.getWindow().setFlags(
			    WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED,
			    WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED);
    }

    private void disableHardwareAccel(WebView view) {
		Log.d(TAG, "will enable hardware accel. for window but disable for webview");
		enableHardwareAccel();
		Log.d(TAG, "selectively disabling hardware accel. for webview");
		view.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
    }
	
    private WebView instantiateWebView() {
    	WebView wv = new WebView(this);
		
		//Workaround for https://code.google.com/p/android/issues/detail?id=7189
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.GINGERBREAD)
		{
			Log.d(TAG, "enabling newage HTC bugfix");
			wv.setOnTouchListener(new View.OnTouchListener() {
	            @Override
	            public boolean onTouch(View v, MotionEvent event) {
	                switch (event.getAction()) {
	                    case MotionEvent.ACTION_DOWN:
	                    case MotionEvent.ACTION_UP:
	                    	Log.d(TAG, "applied newage HTC bugfix");
	                        if (!v.hasFocus()) {
	                            v.requestFocus();
	                        }
	                        break;
	                }
	                return false;
	            }
	        });
		}
		
		return wv;
    }
}
