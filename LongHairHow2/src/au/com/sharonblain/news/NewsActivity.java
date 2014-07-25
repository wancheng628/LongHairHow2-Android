package au.com.sharonblain.news;

import java.util.Date;

import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.assist.ImageSize;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;

import android.app.Activity;
import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnGroupExpandListener;
import android.widget.ImageView;

import au.com.sharonblain.longhairhow2.R;
import au.com.sharonblain.request_server.AsyncResponse;
import au.com.sharonblain.request_server.GlobalVariable;
import au.com.sharonblain.request_server.HttpPostTask;

public class NewsActivity extends Activity implements AsyncResponse{

	private DisplayImageOptions options;
	private HttpPostTask httpTask = new HttpPostTask() ;
	private ProgressDialog _dialog_progress ;
	private int nRequestKind ;
	private NewsItem[] news ;
	private ExpandableListView list_news ;
	private int lastExpandedPosition = -1;
	
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_news);
        
        if (android.os.Build.VERSION.SDK_INT > 9) {
    	    StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
    	    StrictMode.setThreadPolicy(policy);
    	}
        
        _dialog_progress = new ProgressDialog(NewsActivity.this) ;
        
        setProfilePhoto() ; 
        GetNewsRequest("/blog/getAll") ;
        
        list_news = (ExpandableListView) findViewById(R.id.list_news);
        list_news.setOnGroupExpandListener(new OnGroupExpandListener() {

            @Override
            public void onGroupExpand(int groupPosition) {
                if (lastExpandedPosition != -1
                        && groupPosition != lastExpandedPosition) {
                	list_news.collapseGroup(lastExpandedPosition);
                	
                	View view = list_news.getChildAt(groupPosition) ;
                	if ( view != null )
                	{
                		ImageView img_check = (ImageView)view.findViewById(R.id.img_check) ;
                		if ( img_check != null )
                			img_check.setImageDrawable(getResources().getDrawable(R.drawable.unchecked)) ;
                	}
                }
                lastExpandedPosition = groupPosition;
                list_news.smoothScrollToPosition(lastExpandedPosition) ;
            }
        });
	}

	@Override
	public void processFinish(String output) throws IllegalArgumentException {
		httpTask.delegate = NewsActivity.this ;
        
		if ( (_dialog_progress != null) && (_dialog_progress.isShowing()) )
		{
			try {
				_dialog_progress.dismiss() ;
				_dialog_progress = null;
		    } catch (Exception e) {
		        // nothing
		    }
		}
        
		if ( nRequestKind == 1 )
		{
			if (output.length() > 0) {
				try {
					JSONObject jsonObj = new JSONObject(output) ;
					
					if (jsonObj.get("type").equals("Success"))
					{
						JSONArray result = jsonObj.getJSONArray("results") ;
						news = new NewsItem[result.length()] ;
						
						for ( int i = 0 ; i < result.length() ; i++ )
						{
							JSONObject object = result.getJSONObject(i) ;
							NewsItem item = new NewsItem(object.getString("bp_id"), object.getString("title"), object.getString("body")) ;
							news[i] = item ;
						}
						
						DisplayMetrics metrics = new DisplayMetrics();
					    getWindowManager().getDefaultDisplay().getMetrics(metrics);
					    int width = metrics.widthPixels; 
					    
						final ExpandableListAdapter expListAdapter = new ExpandableListAdapter(NewsActivity.this, news) ;
				        list_news.setAdapter(expListAdapter);
				        list_news.setIndicatorBounds(width - GetPixelFromDips(50), width - GetPixelFromDips(10));  

					}
					else
					{
						getAccessToken() ;
					}
				
				} catch (JSONException e) {
					e.printStackTrace() ;
					getAccessToken() ;
				}
			} else {
				Log.e("ServiceHandler", "Couldn't get any data from the url") ;	
				getAccessToken() ;				
			}
		}
		else
		{
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
						
						GetNewsRequest("/blog/getAll") ;
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
	
	public int GetPixelFromDips(float pixels) {
	    // Get the screen's density scale 
	    final float scale = getResources().getDisplayMetrics().density;
	    // Convert the dps to pixels, based on density scale
	    return (int) (pixels * scale + 0.5f);
	}
	
	private void GetNewsRequest(String action) {
		nRequestKind = 1 ;
		if ( _dialog_progress == null || !_dialog_progress.isShowing() )
		{
			_dialog_progress = ProgressDialog.show(this, "Loading...", 
	    				"Please wait...", true);			
		}	
    	
    	MultipartEntityBuilder builder = MultipartEntityBuilder.create();
		builder.addTextBody("action", action, ContentType.TEXT_PLAIN);
		if ( GlobalVariable.user_id == null || GlobalVariable.user_id.length() < 1 )
			GlobalVariable.user_id = "-10" ;
		
		builder.addTextBody("user_id", GlobalVariable.user_id, ContentType.TEXT_PLAIN);
		if ( GlobalVariable.accessToken != null )
			builder.addTextBody("accessToken", GlobalVariable.accessToken, ContentType.TEXT_PLAIN);
		
		GlobalVariable.request_url = "http://longhairhow2.com/api/blog/getAll" ;
		
		httpTask = new HttpPostTask() ;
		httpTask.delegate = NewsActivity.this ;
		httpTask.execute(builder) ;
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
    		try {
    			_dialog_progress = ProgressDialog.show(this, "Loading...", "Please wait...", true);
    		}
    		catch(NullPointerException e) {
    			e.printStackTrace() ;
    		}
    	}			
    	
    	GlobalVariable.getSydneyTime() ;
    	MultipartEntityBuilder params = MultipartEntityBuilder.create() ;
		params.addTextBody("action", "/common/access-token/grant", ContentType.TEXT_PLAIN);
		params.addTextBody("user_id", GlobalVariable.user_id, ContentType.TEXT_PLAIN);
		
		GlobalVariable.request_url = "http://longhairhow2.com/api/common/access-token/grant" ;
		
		httpTask = new HttpPostTask() ;
		httpTask.delegate = this;
		httpTask.execute(params) ;
    }
	
	private void setProfilePhoto() {
		ImageView imgProfilePhoto = (ImageView)findViewById(R.id.img_profile_photo) ;
        if ( GlobalVariable.profile_photo_path != null && GlobalVariable.profile_photo_path.length() > 1 )
        {
        	options = new DisplayImageOptions.Builder()
    		.showImageForEmptyUri(R.drawable.default_user_icon)
    		.showImageOnFail(R.drawable.default_user_icon)
    		.resetViewBeforeLoading(true)
    		.cacheOnDisk(true)
    		.imageScaleType(ImageScaleType.EXACTLY)
    		.bitmapConfig(Bitmap.Config.RGB_565)
    		.considerExifParams(true)
    		.displayer(new FadeInBitmapDisplayer(300))
    		.build();
        	
        	ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(getApplicationContext())
        	.defaultDisplayImageOptions(options)
        	.build();
        	ImageLoader.getInstance().init(config);
        	ImageSize targetSize = new ImageSize(160, 160);
        	
        	if (android.os.Build.VERSION.SDK_INT > 9) {
        	    StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        	    StrictMode.setThreadPolicy(policy);
        	}
        	
        	Bitmap bmp = ImageLoader.getInstance().loadImageSync("http://longhairhow2.com/api" + GlobalVariable.profile_photo_path, targetSize, options);
        	imgProfilePhoto.setImageBitmap(GlobalVariable.getCircularBitmap(bmp)) ;
        	
        	imgProfilePhoto.getLayoutParams().width = 80 ;
        	imgProfilePhoto.getLayoutParams().height = 80 ;
        	
        	try {
        		ImageLoader.getInstance().destroy() ;
        	} catch (NullPointerException e) {
        		e.printStackTrace() ;
        	}
        }
	}
}
