package au.com.sharonblain.uservideo;

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
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;
import au.com.sharonblain.longhairhow2.ProfileActivity;
import au.com.sharonblain.longhairhow2.R;
import au.com.sharonblain.request_server.AsyncResponse;
import au.com.sharonblain.request_server.GlobalVariable;
import au.com.sharonblain.request_server.HttpPostTask;

public class UserVideoActivity extends Activity implements AsyncResponse {

	private ProgressDialog _dialog_progress ;
	private HttpPostTask httpTask = new HttpPostTask() ;
	private int nRequestKind = 1 ;
	private String selected_vid_id = "" ;
	private DisplayImageOptions options;
	private GridView grid ;
	private TextView label_novideo ;
	
	public class UserVideoItem {
		
		String vid_title ;
		String vid_url ;
		String v_id ;
		String p_id ;
		String vid_image ;
		
	}
	
	private UserVideoItem[] videos ;
	
	@Override
	public void onBackPressed() {
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
			if (output.length() > 0) {
				try {
					JSONObject jsonObj = new JSONObject(output) ;
					if (jsonObj.get("type").equals("Success"))
					{
						JSONArray result = jsonObj.getJSONArray("results") ;
						JSONObject addedVids = new JSONObject() ;
						JSONArray vids = new JSONArray() ;
						for ( int i = 0 ; i < result.length() ; i++ )
						{
							JSONObject video = result.getJSONObject(i) ;
							if ( video.has("vids") )
							{
								JSONArray temp_vids = video.getJSONArray("vids") ;
								
								for ( int j = 0 ; j < temp_vids.length() ; j++ )
								{
									JSONObject vidIn = temp_vids.getJSONObject(j) ;
									
									if ( addedVids.has(vidIn.getString("v_id")) == false )
									{
										JSONObject newVidDict = new JSONObject(vidIn.toString()) ;
										if ( video.has("valid_until") )
											newVidDict.put("valid_until", video.getString("valid_until")) ;
										else
											newVidDict.put("valid_until", "") ;
										
										vids.put(newVidDict) ;
										addedVids.put(vidIn.getString("v_id"), vidIn) ;
									}
								}
							}														
						}
						
						this.videos = new UserVideoItem[vids.length()] ;
						
						for ( int i = 0 ; i < vids.length() ; i++ )
						{
							JSONObject item = vids.getJSONObject(i) ;
							UserVideoItem temp = new UserVideoItem() ;
							temp.p_id = item.getString("p_id") ;
							temp.v_id = item.getString("v_id") ;
							temp.vid_image = GlobalVariable.API_URL + item.getString("vid_image") ;
							temp.vid_title = item.getString("vid_title") ;
							temp.vid_url = GlobalVariable.API_URL + item.getString("vid_url") ;
							
							this.videos[i] = temp ;
						}
						
						if ( this.videos.length < 1 )
						{
							label_novideo.setVisibility(View.VISIBLE) ;
							label_novideo.setTypeface(GlobalVariable.tf_medium) ;
							grid.setVisibility(View.GONE) ;
						}
						else
						{
							label_novideo.setVisibility(View.GONE) ;
							grid.setVisibility(View.VISIBLE) ;
							VideoAdapter adapter = new VideoAdapter(UserVideoActivity.this, this.videos) ;
							grid.setAdapter(adapter) ;
						}
						
					}
					else
					{
						Toast.makeText(UserVideoActivity.this, jsonObj.getString("type") + " - " + jsonObj.getString("message"), Toast.LENGTH_LONG).show() ;
						getAccessToken() ;
					}
				
				} catch (JSONException e) {
					e.printStackTrace();
					
				}
			} else {
				Log.e("ServiceHandler", "Couldn't get any data from the server.") ;
				
			}
		}
		else if ( nRequestKind == 2 ){
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
						getPurchasedVideos() ;						
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
    		_dialog_progress = ProgressDialog.show(this, "Loading...", "Please wait...", true);    		
    	}
    				
    	
    	GlobalVariable.getSydneyTime() ;
    	
    	MultipartEntityBuilder builder = MultipartEntityBuilder.create() ;
    	builder.addTextBody("action", "/common/access-token/grant", ContentType.TEXT_PLAIN);
    	builder.addTextBody("user_id", GlobalVariable.user_id, ContentType.TEXT_PLAIN);
		
		GlobalVariable.request_url = GlobalVariable.API_URL + "/common/access-token/grant" ;
		
		httpTask = new HttpPostTask() ;
		httpTask.delegate = this;
		httpTask.execute(builder) ;
    }
	
	private void setProfilePhoto() {
		ImageView imgProfilePhoto = (ImageView)findViewById(R.id.img_profile_photo) ;
        if ( GlobalVariable.profile_photo_path != null && GlobalVariable.profile_photo_path.length() > 1 && !GlobalVariable.profile_photo_path.equals("/user/images/") )
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
        	
        	Bitmap bmp = ImageLoader.getInstance().loadImageSync(GlobalVariable.API_URL + GlobalVariable.profile_photo_path, targetSize, options);
        	imgProfilePhoto.setImageBitmap(GlobalVariable.getCircularBitmap(bmp)) ;
        	
        	try {
        		ImageLoader.getInstance().destroy() ;
        	} catch (NullPointerException e) {
        		e.printStackTrace() ;
        	}
        }
        
        imgProfilePhoto.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(UserVideoActivity.this, ProfileActivity.class) ;
				startActivity(intent) ;
			}
		}) ;
	}
	
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState) ;
        setContentView(R.layout.activity_uservideo) ;
        
        if (android.os.Build.VERSION.SDK_INT > 9) {
    	    StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
    	    StrictMode.setThreadPolicy(policy);
    	}
        
        label_novideo = (TextView)findViewById(R.id.label_novideo) ;
        setProfilePhoto() ;
        _dialog_progress = new ProgressDialog(UserVideoActivity.this) ;
		httpTask.delegate = UserVideoActivity.this ;
		
		grid = (GridView)findViewById(R.id.grid1) ;
		getPurchasedVideos() ;
		
		grid.setOnItemClickListener(new OnItemClickListener() {
            
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				
				selected_vid_id = videos[arg2].v_id ;
				Intent myIntent = new Intent(UserVideoActivity.this, VideoStreamActivity.class) ;
				myIntent.putExtra("address", GlobalVariable.API_URL + "/vid/stream.php?user_id=" + GlobalVariable.user_id + 
						"&accessToken=" + GlobalVariable.accessToken +
						"&video_id=" + selected_vid_id) ;
				myIntent.putExtra("title", videos[arg2].vid_title) ;
				startActivity(myIntent) ;
			}
        });
	}
		
	protected void getPurchasedVideos() {
    	
		nRequestKind = 1 ;
		
    	if ( _dialog_progress == null || !_dialog_progress.isShowing() )
    	{
    		try {
    			_dialog_progress = ProgressDialog.show(this, "Loading...", 
        				"Please wait...", true) ;
    		}catch (NullPointerException e) {
    			e.printStackTrace() ;
    		}
    	}
    		
    	MultipartEntityBuilder builder = MultipartEntityBuilder.create() ;
    	builder.addTextBody("action", "/user/bundles/get", ContentType.TEXT_PLAIN) ;
    	builder.addTextBody("user_id", GlobalVariable.user_id, ContentType.TEXT_PLAIN) ;
    	builder.addTextBody("u_id", GlobalVariable.user_id, ContentType.TEXT_PLAIN) ;
    	builder.addTextBody("accessToken", GlobalVariable.accessToken, ContentType.TEXT_PLAIN) ;
		
		GlobalVariable.request_url = GlobalVariable.API_URL + "/user/bundles/get" ;
		
		httpTask = new HttpPostTask() ;
		httpTask.delegate = UserVideoActivity.this ;
		httpTask.execute(builder) ;		
	}
}
