package au.com.sharonblain.longhairhow2;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.ProgressBar;

@SuppressLint("SetJavaScriptEnabled")
public class MoreWebViewActivity extends Activity {
	
	private WebView mWebview ;
	private String url ;
	private ProgressBar progress;
	private Activity activity;
	
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState) ;
        setContentView(R.layout.activity_more_webview) ;
    
        activity = this ;
        
        url = getIntent().getExtras().getString("url") ;
        mWebview = (WebView)findViewById(R.id.webView1) ;
        mWebview.getSettings().setJavaScriptEnabled(true) ;
        progress = (ProgressBar) findViewById(R.id.progressBar1);
        progress.setVisibility(View.GONE);
        
        mWebview.setWebViewClient(new MyWebViewClient());
        mWebview.setWebChromeClient(new WebChromeClient() {
        	public void onProgressChanged(WebView view, final int progress)
            {
                activity.setTitle("Loading...");
                activity.setProgress(progress * 100);
                MoreWebViewActivity.this.progress.setProgress(progress );

                if ( progress >= 100 )
                	activity.setTitle(R.string.app_name) ;
            }
        }) ;
        
        mWebview.getSettings().setJavaScriptEnabled(true);
        mWebview.loadUrl(url);    
        
        ImageView imgBack = (ImageView)findViewById(R.id.img_back) ;
        ImageView imgForward = (ImageView)findViewById(R.id.img_forward) ;
        ImageView imgRefresh = (ImageView)findViewById(R.id.img_refresh) ;
        ImageView imgStop = (ImageView)findViewById(R.id.img_stop) ;
        
        imgBack.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				mWebview.goBack() ;
			}
		}) ;
        
        imgForward.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				mWebview.goForward() ;
			}
		}) ;
        
        imgRefresh.setOnClickListener(new OnClickListener() {
					
			@Override
			public void onClick(View arg0) {
				mWebview.reload() ;
			}
		}) ;
		
        imgStop.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				finish() ;
			}
		}) ;
	}
	
	private class MyWebViewClient extends WebViewClient {	
		@Override
	    public boolean shouldOverrideUrlLoading(WebView view, String url) {
	        view.loadUrl(url);
	        return true;
	    }

		@Override
		public void onPageFinished(WebView view, String url) {
			 progress.setVisibility(View.GONE);
			 MoreWebViewActivity.this.progress.setProgress(100);
			 super.onPageFinished(view, url);
		}

		@Override
		public void onPageStarted(WebView view, String url, Bitmap favicon) {
			 progress.setVisibility(View.VISIBLE);
			 MoreWebViewActivity.this.progress.setProgress(0);
			 super.onPageStarted(view, url, favicon);
		}
		
	}


	public void setValue(int progress) {
		this.progress.setProgress(progress);		
	}
}
