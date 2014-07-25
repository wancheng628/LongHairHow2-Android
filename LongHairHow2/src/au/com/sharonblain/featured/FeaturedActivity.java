package au.com.sharonblain.featured;

import java.util.ArrayList;
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
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import au.com.sharonblain.longhairhow2.R;
import au.com.sharonblain.request_server.AsyncResponse;
import au.com.sharonblain.request_server.GlobalVariable;
import au.com.sharonblain.request_server.HttpPostTask;

public class FeaturedActivity extends Activity implements AsyncResponse {

	private HttpPostTask httpTask = new HttpPostTask() ;
	private ProgressDialog _dialog_progress ;
	
	private ImageView imgProfilePhoto ;
	private TextView label_type ;
	private GridView grid ;
	private int nRequestKind ;
	private DisplayImageOptions options;
	
	private ArrayList<String> b_id, bun_vid_url, bun_image, bun_description, bun_title, prices ;
	private GridAdapter adapter ;
	private ArrayList<String> v_id, vid_url, vid_title, vid_image ;
	
	private Button btn_featured, btn_popular ;
	
	private void getLayoutObjects()
	{
		imgProfilePhoto = (ImageView)findViewById(R.id.img_profile_photo) ;
        label_type = (TextView)findViewById(R.id.label_type) ;
        grid = (GridView)findViewById(R.id.grid_photos) ;
        
        label_type.setTypeface(GlobalVariable.tf_light) ;
        btn_featured = (Button)findViewById(R.id.btn_featured) ;
        btn_popular = (Button)findViewById(R.id.btn_popular) ;
        
        btn_featured.setTypeface(GlobalVariable.tf_light) ;
        btn_popular.setTypeface(GlobalVariable.tf_light) ;
	}
	
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_featured);
        
        _dialog_progress = new ProgressDialog(FeaturedActivity.this) ;
        getLayoutObjects() ;
        
        btn_featured.setSelected(true) ;
        btn_popular.setSelected(false) ;
        
        v_id = new ArrayList<String>() ;
        vid_url = new ArrayList<String>() ;
        vid_title = new ArrayList<String>() ;
        vid_image = new ArrayList<String>() ;
        
        btn_featured.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				btn_featured.setSelected(true) ;
				btn_popular.setSelected(false) ;
				
				_sendFeaturedRequest("/front-page/get") ;
			}
		}) ;
        
        btn_popular.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				btn_featured.setSelected(false) ;
				btn_popular.setSelected(true) ;
				
				_sendFeaturedRequest("/popular/get") ;
			}
		}) ;
        
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
        
        _sendFeaturedRequest("/front-page/get") ;
        
        grid.setOnItemClickListener(new OnItemClickListener() {
            
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				
				Intent myIntent = new Intent(FeaturedActivity.this, FeaturedDetailActivity.class);
				myIntent.putExtra("b_id", b_id.get(arg2)) ;
				myIntent.putExtra("video", bun_vid_url.get(arg2)) ;
				myIntent.putExtra("title", bun_title.get(arg2)) ;
				myIntent.putExtra("prices", prices.get(arg2)) ;
				myIntent.putExtra("images", vid_image.get(arg2)) ;
				myIntent.putExtra("titles", vid_title.get(arg2)) ;				
				myIntent.putExtra("description", bun_description.get(arg2)) ;
				myIntent.putExtra("v_ids", v_id.get(arg2)) ;
				startActivity(myIntent);				
			}
        });
	}
	
	private void _sendFeaturedRequest(String action)
	{
		nRequestKind = 1 ;
		
		if ( adapter != null )
		{
			adapter.clear() ;
			v_id = new ArrayList<String>() ;
			vid_url = new ArrayList<String>() ;
			vid_title = new ArrayList<String>() ;
			vid_image = new ArrayList<String>() ;
			
			grid.setAdapter(adapter) ;
		}
		
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
		builder.addTextBody("front_type", "hot", ContentType.TEXT_PLAIN);
		
		GlobalVariable.request_url = "http://longhairhow2.com/api/front-page/get" ;
		
		httpTask = new HttpPostTask() ;
		httpTask.delegate = FeaturedActivity.this ;
		httpTask.execute(builder) ;
	}
	
	@Override
	public void processFinish(String output) throws IllegalArgumentException {
		httpTask.delegate = FeaturedActivity.this ;
        
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
						
						bun_vid_url = new ArrayList<String>() ;
						bun_image = new ArrayList<String>() ;
						bun_description = new ArrayList<String>() ;
						bun_title = new ArrayList<String>() ;
						prices = new ArrayList<String>() ;
						b_id = new ArrayList<String>() ;
						
						for ( int i = 0 ; i < result.length() ; i++ )
						{
							b_id.add(result.getJSONObject(i).getString("b_id")) ;
							bun_vid_url.add(result.getJSONObject(i).getString("bun_vid_url")) ;
							bun_image.add(result.getJSONObject(i).getString("bun_image")) ;
							bun_description.add(result.getJSONObject(i).getString("description")) ;
							bun_title.add(result.getJSONObject(i).getString("title")) ;
							prices.add(result.getJSONObject(i).getString("price")) ;
							
							JSONArray res_videos = result.getJSONObject(i).getJSONArray("videos") ;
							
							String _temp_id = "", _temp_url = "", _temp_title = "", _temp_image = "" ;
							
							for ( int j = 0 ; j < res_videos.length() ; j++ )
							{
								_temp_id = _temp_id + res_videos.getJSONObject(j).getString("v_id") + "^";
								_temp_url = _temp_url + res_videos.getJSONObject(j).getString("vid_url") + "^" ;
								_temp_title = _temp_title + res_videos.getJSONObject(j).getString("vid_title") + "^" ;
								_temp_image = _temp_image + "http://longhairhow2.com/api" + 
												res_videos.getJSONObject(j).getString("vid_image") + "^" ;
							}
							
							v_id.add(_temp_id) ;
							vid_url.add(_temp_url) ;
							vid_title.add(_temp_title) ;
							vid_image.add(_temp_image) ;
						}
						
						adapter = new GridAdapter(FeaturedActivity.this, vid_title) ;
						adapter.updateAdapter(bun_title, vid_image, prices, vid_url, bun_description) ;
						grid.setAdapter(adapter) ;
					}
					else
					{
						getAccessToken() ;
						
					}
				
				} catch (JSONException e) {
					e.printStackTrace();
					
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
						
						_sendFeaturedRequest("/front-page/get") ;
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
}
