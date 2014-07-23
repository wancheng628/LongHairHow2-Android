package au.com.sharonblain.search;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Date;

import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.SearchView;
import android.widget.SearchView.OnQueryTextListener;
import android.widget.Toast;
import au.com.sharonblain.longhairhow2.R;
import au.com.sharonblain.request_server.AsyncResponse;
import au.com.sharonblain.request_server.GlobalVariable;
import au.com.sharonblain.request_server.HttpPostTask;

@SuppressLint("SetJavaScriptEnabled")
@SuppressWarnings("deprecation")
public class SearchActivity extends Activity implements AsyncResponse {

	private WebView webview ;
	private ProgressDialog _dialog_progress ;
	private HttpPostTask httpTask = new HttpPostTask() ;
	private String htmlString ;
	private int nRequestKind ;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_search);
	
		_dialog_progress = new ProgressDialog(SearchActivity.this) ;
		webview = (WebView)findViewById(R.id.webView1) ;
		httpTask.delegate = SearchActivity.this ;
		
		SearchView searchView = (SearchView)findViewById(R.id.searchView1) ;
		searchView.setOnQueryTextListener(new OnQueryTextListener() {
			
			@Override
			public boolean onQueryTextSubmit(String arg0) {
				
				Intent myIntent = new Intent(SearchActivity.this, SearchListActivity.class);
            	myIntent.putExtra("query", arg0) ;
				startActivity(myIntent);
				
				return false;
			}
			
			@Override
			public boolean onQueryTextChange(String arg0) {
				return false;
			}
		}) ;
		
		getTagCloud() ;
	}

	protected void getTagCloud() {
    	
		nRequestKind = 1 ;
    	if ( _dialog_progress == null || !_dialog_progress.isShowing() ) {
    			_dialog_progress = ProgressDialog.show(this, "Connecting Server...", 
        				"Please wait a sec.", true);    		
    	}
    		
    	
    	MultipartEntityBuilder params = MultipartEntityBuilder.create() ;
		params.addTextBody("action", "/tags/get", ContentType.TEXT_PLAIN);
		params.addTextBody("user_id", GlobalVariable.user_id, ContentType.TEXT_PLAIN);
		params.addTextBody("accessToken", GlobalVariable.accessToken, ContentType.TEXT_PLAIN);
		
		GlobalVariable.request_url = "http://longhairhow2.com/api/tags/get" ;
		
		httpTask = new HttpPostTask() ;
		httpTask.delegate = SearchActivity.this ;
		httpTask.execute(params) ;
		
	}

	private String capitalizaeString(String s)
	{
		final StringBuilder result = new StringBuilder(s.length());
		String[] words = s.split("\\s");
		for(int i=0,l=words.length;i<l;++i) {
		  if(i>0) result.append(" ");      
		  result.append(Character.toUpperCase(words[i].charAt(0)))
		        .append(words[i].substring(1));

		}
		
		return result.toString() ;
	}
	
	private String getAssetsContent()
	{
		StringBuilder returnString = new StringBuilder();
	    InputStream fIn = null;
	    InputStreamReader isr = null;
	    BufferedReader input = null;
	    try {
	        fIn = getResources().getAssets()
	                .open("tag-cloud.html", Context.MODE_WORLD_READABLE);
	        isr = new InputStreamReader(fIn);
	        input = new BufferedReader(isr);
	        String line = "";
	        while ((line = input.readLine()) != null) {
	            returnString.append(line);
	        }
	    } catch (Exception e) {
	        e.getMessage();
	    } finally {
	        try {
	            if (isr != null)
	                isr.close();
	            if (fIn != null)
	                fIn.close();
	            if (input != null)
	                input.close();
	        } catch (Exception e2) {
	            e2.getMessage();
	        }
	    }
	    
	    return returnString.toString() ;
	}
	
	@Override
	public void processFinish(String output) {
		if ( (_dialog_progress != null) && (_dialog_progress.isShowing()) )
		{
			try {
				_dialog_progress.dismiss() ;
				_dialog_progress = null ;
			} catch (NullPointerException e) {
				e.printStackTrace() ;
			} catch (IllegalArgumentException e) {
				e.printStackTrace() ;
			}
		}
		
		if ( nRequestKind == 1 ) {
			String wordInJSON = "[" ;
			
			if (output.length() > 0) {
				try {
					JSONObject jsonObj = new JSONObject(output) ;
					if (jsonObj.get("type").equals("Success"))
					{
						JSONArray result = jsonObj.getJSONArray("results") ;
						for ( int i = 0 ; i < result.length() ; i++ )
						{
							JSONObject jObject = result.getJSONObject(i) ;
							String _capitalizedString = capitalizaeString(jObject.getString("tag")) ;
							String _temp = String.format("{text:\"%s\",weight:%s,link:\"cclick-event://%s\"},", 
									_capitalizedString, jObject.getString("num"), jObject.getString("tag").toLowerCase()) ;
							
							wordInJSON = wordInJSON + _temp ;
						}
						
						wordInJSON = wordInJSON.substring(0, wordInJSON.length()-1) ;
						wordInJSON = wordInJSON + "]" ;
						
						htmlString = getAssetsContent() ;
						htmlString = htmlString.replace("INSERT-JSON-ARRAY-OF-WORDS", wordInJSON) ;
						
						startWebView(htmlString);
					}
					else
					{
						Toast.makeText(SearchActivity.this, jsonObj.getString("type") + " - " + jsonObj.getString("message"), Toast.LENGTH_LONG).show() ;
						getAccessToken() ;
					}
				
				} catch (JSONException e) {
					e.printStackTrace();
					
				}
			} else {
				Log.e("ServiceHandler", "Couldn't get any data from the url") ;
				
			}
		}
		else {
			try {
				JSONObject jsonObj = new JSONObject(output) ;
				if (jsonObj.get("type").equals("Success"))
				{
					JSONObject result = jsonObj.getJSONObject("results") ;
					Date validity = GlobalVariable.getDateFromString(result.getString("validity")) ;
					
					if ( validity.after(GlobalVariable.cur_sydney_time) )
					{
						GlobalVariable.f_valid = true ;
						setAccessToken(jsonObj) ;
						
						getTagCloud() ;
					}
					else
					{
						GlobalVariable.f_valid = false ;
						getAccessToken() ;
					}
						
				}
				else if (jsonObj.get("type").equals("Error"))
				{
					GlobalVariable.f_valid = false ;
					getAccessToken() ;
				}
			
			} catch (JSONException e) {
				e.printStackTrace();
				GlobalVariable.f_valid = false ;
				getAccessToken() ;
			}
		}
	}
	
	private void setAccessToken(JSONObject jsonObj) throws JSONException
    {
    	JSONObject result = jsonObj.getJSONObject("results") ;
		
		GlobalVariable.accessToken = result.getString("accessToken") ;
		GlobalVariable.validity = result.getString("validity") ;
		GlobalVariable.user_id = result.getString("user_id") ;
    }
	
	private void getAccessToken()
    {
		nRequestKind = 2 ;
		
    	if ( _dialog_progress == null || !_dialog_progress.isShowing() )
    	{
    		_dialog_progress = ProgressDialog.show(this, "Connecting Server...", 
        				"Getting Access Token... Please wait a sec.", true);    		
    	}	
    	
    	GlobalVariable.getSydneyTime() ;
    	
    	MultipartEntityBuilder params = MultipartEntityBuilder.create() ;
		params.addTextBody("action", "/common/access-token/grant", ContentType.TEXT_PLAIN) ;
		params.addTextBody("user_id", GlobalVariable.user_id, ContentType.TEXT_PLAIN) ;
		
		GlobalVariable.request_url = "http://longhairhow2.com/api/common/access-token/grant" ;
		
		httpTask = new HttpPostTask() ;
		httpTask.delegate = this;
		httpTask.execute(params) ;
    }
	
	private void startWebView(String url) {
        
        webview.setWebViewClient(new WebViewClient() {      
            ProgressDialog progressDialog;
          
            public boolean shouldOverrideUrlLoading(WebView view, String url) {              
                
            	url = url.replace("cclick-event://", "") ;
            	url = url.replace("%20", " ") ;
            	
            	Intent myIntent = new Intent(SearchActivity.this, SearchListActivity.class);
            	myIntent.putExtra("query", url) ;
				startActivity(myIntent);
            	
                return true;
            }
        
            //Show loader on url load
            public void onLoadResource (WebView view, String url) {
                if (progressDialog == null) {
                    // in standard case YourActivity.this
                    progressDialog = new ProgressDialog(SearchActivity.this);
                    progressDialog.setMessage("Loading...");
                    progressDialog.show();
                }
            }
            public void onPageFinished(WebView view, String url) {
                try{
                	if ( progressDialog != null ) {
                		if (progressDialog.isShowing()) {
                            progressDialog.dismiss();
                            progressDialog = null;
                        }
                	}                
                }catch(Exception exception){
                    exception.printStackTrace();
                }
            }
             
        }); 
          
        webview.getSettings().setJavaScriptEnabled(true); 
        webview.loadDataWithBaseURL("file:///android_asset/", url, "text/html", "UTF-8", null);
    }
     
    @Override
    public void onBackPressed() {
        if(webview.canGoBack()) {
            webview.loadDataWithBaseURL("file:///android_asset/", htmlString, "text/html", "UTF-8", null);
        } else {
            // Let the system handle the back button
            super.onBackPressed();
        }
    }
}
