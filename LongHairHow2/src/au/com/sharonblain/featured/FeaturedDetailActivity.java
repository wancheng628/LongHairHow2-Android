package au.com.sharonblain.featured;

import java.util.Date;

import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
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
import android.util.DisplayMetrics;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewTreeObserver;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;
import au.com.sharonblain.longhairhow2.ProfileActivity;
import au.com.sharonblain.longhairhow2.R;
import au.com.sharonblain.request_server.AsyncResponse;
import au.com.sharonblain.request_server.GlobalVariable;
import au.com.sharonblain.request_server.HttpPostTask;
import au.com.sharonblain.uservideo.VideoStreamActivity;

@SuppressWarnings("deprecation")
public class FeaturedDetailActivity extends Activity implements AsyncResponse {

	private HttpPostTask httpTask = new HttpPostTask() ;
	private ProgressDialog _dialog_progress ;
	
	private TextView title ;
	private VideoView video ;
	private ExpandableHeightGridView grid ;
	private TextView label_cart ;
	private TextView mDescriptionTextView ;
	
	private String[] arr_images ;
	private String[] arr_titles ;
	private String[] arr_prices ;
	private String[] arr_vids ;
	private boolean[] arr_charged ;
	private boolean[] arr_purchased ;
	
	private String b_id ;
	
	private DisplayMetrics dm;
	private DisplayImageOptions options;
	private MediaController media_Controller;
	
	int total_videos = 0;
	int nRequestKind = 1 ;
	
	@Override
	protected void onResume() {
		video.resume();
	    super.onResume();
	}

	@Override
	protected void onPause() {
		video.suspend();
	    super.onPause();
	}

	@Override
	protected void onDestroy() {
		video.stopPlayback();
	    super.onDestroy();
	}
	
	private void getLayoutObjects()
	{
		title = (TextView)findViewById(R.id.label_type) ;
		video = (VideoView)findViewById(R.id.video_view) ;
		grid = (ExpandableHeightGridView)findViewById(R.id.grid_photos) ;
		mDescriptionTextView = (TextView)findViewById(R.id.label_description) ;
		label_cart = (TextView)findViewById(R.id.label_cart_info) ;
		label_cart.setTypeface(GlobalVariable.tf_medium) ;
		TextView label_photo_purchase = (TextView)findViewById(R.id.label_photo_purchase) ;
		label_photo_purchase.setTypeface(GlobalVariable.tf_light) ;
		title.setTypeface(GlobalVariable.tf_medium) ;
		mDescriptionTextView.setTypeface(GlobalVariable.tf_light) ;
		
		title.setText(getIntent().getExtras().getString("title")) ;
		mDescriptionTextView.setText(getIntent().getExtras().getString("description")) ;
		
	}
	
	private void setVideoController() {
		
		media_Controller = new MediaController(FeaturedDetailActivity.this); 
        dm = new DisplayMetrics();
        
        this.getWindowManager().getDefaultDisplay().getMetrics(dm); 
        int width = dm.widthPixels; 
        
        video.setMinimumWidth(width); 
        video.setMinimumHeight(width); 
        video.setMediaController(media_Controller);
        video.setVideoPath(GlobalVariable.API_URL + getIntent().getExtras().getString("video")) ;
        video.start() ;
        
	}
	
	private void setActionBar()
	{
		TextView mTitleTextView = (TextView)findViewById(R.id.label_type);
        mTitleTextView.setText(getIntent().getExtras().getString("title")) ;
        mTitleTextView.setTypeface(GlobalVariable.tf_light) ;
 
        ImageView profile_photo = (ImageView)findViewById(R.id.img_profile_photo);
        
        if ( GlobalVariable.profile_photo_path != null && GlobalVariable.profile_photo_path.length() > 1  && !GlobalVariable.profile_photo_path.equals("/user/images/"))
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
        	Bitmap bmp = ImageLoader.getInstance().loadImageSync(GlobalVariable.API_URL + GlobalVariable.profile_photo_path, targetSize, options);
        	profile_photo.setImageBitmap(GlobalVariable.getCircularBitmap(bmp)) ;
        	try {
        		ImageLoader.getInstance().destroy() ;
        	} catch (NullPointerException e) {
        		e.printStackTrace() ;
        	}
        	
        }
        
        profile_photo.setOnClickListener(new OnClickListener() {
 
            @Override
            public void onClick(View view) {
            	Intent intent = new Intent(FeaturedDetailActivity.this, ProfileActivity.class) ;
				startActivity(intent) ;
            }
        });
 
	}
	
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_featured_detail);
        
        _dialog_progress = new ProgressDialog(FeaturedDetailActivity.this) ;
        total_videos = 0 ;
        
        getLayoutObjects() ;		
        setActionBar() ;
        
        b_id = getIntent().getExtras().getString("b_id") ;
        String _image_urls = getIntent().getExtras().getString("images") ;
        String _titles = getIntent().getExtras().getString("titles") ;
        String price = getIntent().getExtras().getString("prices") ;
        String _v_ids = getIntent().getExtras().getString("v_ids") ;
        
        if ( _image_urls != null & _image_urls.length() > 1 )
        {
        	_image_urls = _image_urls.replace(" ", "") ;
        	arr_images = _image_urls.split("\\^") ;
        }
        
        if ( _titles != null & _titles.length() > 1 )
        {
        	_titles = _titles.replace(" ", "") ;
        	arr_titles = _titles.split("\\^") ;
        }
        
        if ( _v_ids != null & _v_ids.length() > 1 )
        {
        	_v_ids = _v_ids.replace(" ", "") ;
        	arr_vids = _v_ids.split("\\^") ;
        }
        
        for ( int i = 0 ; i < arr_titles.length ; i++ ) {
        	String[] _temp ;
        	if ( arr_titles[i].contains("-") )
        		_temp = arr_titles[i].split("\\-") ;
        	else
        		_temp = arr_titles[i].split("\\:") ;
        	
        	if ( _temp.length > 1 )
        		arr_titles[i] = _temp[1] ;
        }
        
        JSONObject objPrice = null ;
		try {
			objPrice = new JSONObject(price);
			arr_prices = new String[6] ;
	        
	        for ( int i = 1 ; i < 7 ; i++ )
	        {
	        	JSONObject _temp = new JSONObject(objPrice.getString(String.valueOf(i))) ;
	        	arr_prices[i-1] = _temp.getString("price") ;
	        }
		} catch (JSONException e) {
			e.printStackTrace();
		}
        
        arr_charged = new boolean[6] ;
        for ( int i = 0 ; i < 6 ; i++ )
        	arr_charged[i] = false ;
        
        arr_purchased = new boolean[6] ;
        for ( int i = 0 ; i < 6 ; i++ )
        {
        	if ( GlobalVariable.purchasedVideos.contains(arr_vids[i]))
        		arr_purchased[i] = true ;
        	else
        		arr_purchased[i] = false ;
        }
        
        grid.setAdapter(new GridDetailAdapter(this, arr_titles, arr_images, arr_purchased));
        grid.setExpanded(true);
        
        setVideoController() ;
        
        grid.getViewTreeObserver().addOnGlobalLayoutListener(
    	    new ViewTreeObserver.OnGlobalLayoutListener() {

    	       @Override
    	        public void onGlobalLayout() {
    	            grid.getViewTreeObserver().removeGlobalOnLayoutListener(this);
    	            
    	        }
    	    });
        
        grid.setOnItemClickListener(new OnItemClickListener() {
        	public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
        		if ( arr_purchased[position] == false )
        		{
        			TextView label_photo_purchase = (TextView)v.findViewById(R.id.label_photo_purchase) ;
                    arr_charged[position] = !arr_charged[position] ;
                    
                    if ( arr_charged[position] == true )
                    {
                    	label_photo_purchase.setText("REMOVE") ;
                    	total_videos = total_videos + 1 ;
                    }
                    else
                    {
                    	label_photo_purchase.setText("ADD") ;
                    	total_videos = total_videos - 1 ;
                    }
                    
                    String res = "" ;
					if ( total_videos > 1 )
						res = String.format("BUY %d videos @ AU$ %.02f", total_videos, total_videos * 9.99) ;
					else
						res = String.format("%d video @ AU$ %.02f", total_videos, total_videos * 9.99) ;
					
                    label_cart.setText( res ) ;
        		}
        		else
        		{
        			Intent intent = new Intent(FeaturedDetailActivity.this, VideoStreamActivity.class) ;
        			intent.putExtra("address", GlobalVariable.API_URL + "/vid/stream.php?user_id=" + GlobalVariable.user_id + 
    						"&accessToken=" + GlobalVariable.accessToken +
    						"&video_id=" + arr_vids[position]) ;
        			
        			intent.putExtra("title", arr_titles[position]) ;
        			startActivity(intent) ;
        			
        		}
            }
		}) ;
        
        label_cart.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if ( total_videos > 1 )
					sendCartRequest() ;
			}
		}) ;
        
	}
	
	private void sendCartRequest()
	{
		nRequestKind = 1 ;
		if ( _dialog_progress == null || !_dialog_progress.isShowing() )
		{
			_dialog_progress = ProgressDialog.show(FeaturedDetailActivity.this, "Loading...", 
	    				"Please wait...", true);			
		}	
    	
    	MultipartEntityBuilder builder = MultipartEntityBuilder.create() ;
		builder.addTextBody("action", "/sales/putCustom", ContentType.TEXT_PLAIN);
		builder.addTextBody("user_id", GlobalVariable.user_id, ContentType.TEXT_PLAIN);
		builder.addTextBody("accessToken", GlobalVariable.accessToken, ContentType.TEXT_PLAIN);
		builder.addTextBody("email", GlobalVariable.email, ContentType.TEXT_PLAIN);
		builder.addTextBody("b_id", b_id, ContentType.TEXT_PLAIN);
		builder.addTextBody("platform", "android", ContentType.TEXT_PLAIN);
		builder.addTextBody("price", String.format("%.02f", total_videos*9.99), ContentType.TEXT_PLAIN);
		
		String temp = "[" ;
		for ( int i = 0 ; i < arr_vids.length ; i++ )
		{
			if ( arr_charged[i]  )
				temp = temp + String.format("%c%s%c,", 0x22, arr_vids[i], 0x22) ;
		}
		
		temp = temp.substring(0, temp.length() - 1) ;
		temp = temp + "]" ;
		
		builder.addTextBody("v_ids", temp, ContentType.TEXT_PLAIN) ;
		GlobalVariable.request_url = GlobalVariable.API_URL + "/sales/putCustom" ;
		
		nRequestKind = 1 ;
		
		httpTask = new HttpPostTask() ;
		httpTask.delegate = FeaturedDetailActivity.this ;
		httpTask.execute(builder) ;
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
		
		GlobalVariable.request_url = GlobalVariable.API_URL + "/common/access-token/grant" ;
		
		httpTask = new HttpPostTask() ;
		httpTask.delegate = this;
		httpTask.execute(params) ;
    }
	
	@Override
	public void processFinish(String output) {
		httpTask.delegate = FeaturedDetailActivity.this ;
        
		if ( (_dialog_progress != null) && (_dialog_progress.isShowing()) )
		{
			try {
				_dialog_progress.dismiss() ;
				_dialog_progress = null ;
			} catch (NullPointerException e)
			{
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
						//JSONArray result = jsonObj.getJSONArray("results") ;
						String res = "" ;
						if ( total_videos > 1 )
							res = String.format("BUY %d videos @ AU$ %.02f", total_videos, total_videos * 9.99) ;
						else
							res = String.format("%d video @ AU$ %.02f", total_videos, total_videos * 9.99) ;
						
						Toast.makeText(FeaturedDetailActivity.this, res, Toast.LENGTH_LONG).show() ;
					}
				} catch (JSONException e) {
					e.printStackTrace();
					
					getAccessToken() ;
				}
			}
		}
		else if ( nRequestKind == 2 ) {
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
		
		sendCartRequest() ;
    }
	
}
