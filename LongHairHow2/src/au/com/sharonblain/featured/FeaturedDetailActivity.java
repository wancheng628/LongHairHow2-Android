package au.com.sharonblain.featured;

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
import com.squareup.picasso.Picasso;

import android.app.ActionBar;
import android.app.Activity;
import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewTreeObserver;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;
import au.com.sharonblain.longhairhow2.R;
import au.com.sharonblain.request_server.AsyncResponse;
import au.com.sharonblain.request_server.GlobalVariable;
import au.com.sharonblain.request_server.HttpPostTask;

@SuppressWarnings("deprecation")
public class FeaturedDetailActivity extends Activity implements AsyncResponse {

	private HttpPostTask httpTask = new HttpPostTask() ;
	private ProgressDialog _dialog_progress ;
	
	private TextView title ;
	private ImageView profile_photo ;
	private VideoView video ;
	private ExpandableHeightGridView grid ;
	private ImageView btnCart ;
	private TextView label_cart ;
	private TextView mTitleTextView ;
	private TextView mDescriptionTextView ;
	
	private String[] arr_images ;
	private String[] arr_titles ;
	private String[] arr_prices ;
	private String[] arr_vids ;
	private Boolean[] arr_charged ;
	
	private String b_id ;
	
	private DisplayMetrics dm;
	private DisplayImageOptions options;
	private MediaController media_Controller;
	
	int total_videos = 0;
	int nRequestKind = 1 ;
	
	private void getLayoutObjects()
	{
		title = (TextView)findViewById(R.id.label_type) ;
		profile_photo = (ImageView)findViewById(R.id.img_profile_photo) ;
		video = (VideoView)findViewById(R.id.video_view) ;
		grid = (ExpandableHeightGridView)findViewById(R.id.grid_photos) ;
		mDescriptionTextView = (TextView)findViewById(R.id.label_description) ;
		btnCart = (ImageView)findViewById(R.id.btnAddCart) ;
		label_cart = (TextView)findViewById(R.id.label_cart_info) ;
		label_cart.setTypeface(GlobalVariable.tf_medium) ;
		TextView label_photo_purchase = (TextView)findViewById(R.id.label_photo_purchase) ;
		label_photo_purchase.setTypeface(GlobalVariable.tf_light) ;
		title.setTypeface(GlobalVariable.tf_medium) ;
		mDescriptionTextView.setTypeface(GlobalVariable.tf_light) ;
		
		title.setText(getIntent().getExtras().getString("title")) ;
		mDescriptionTextView.setText(getIntent().getExtras().getString("description")) ;
		
        if ( GlobalVariable.profile_photo_path != null && GlobalVariable.profile_photo_path.length() > 1 )
        	Picasso.with(FeaturedDetailActivity.this).load(Uri.parse(GlobalVariable.profile_photo_path)).into(profile_photo) ;
        else
        	profile_photo.setImageBitmap(null) ;
        
	}
	
	private void setVideoController() {
		
		media_Controller = new MediaController(FeaturedDetailActivity.this); 
        dm = new DisplayMetrics();
        
        this.getWindowManager().getDefaultDisplay().getMetrics(dm); 
        int width = dm.widthPixels; 
        
        video.setMinimumWidth(width); 
        video.setMinimumHeight(width); 
        video.setMediaController(media_Controller);
        video.setVideoPath("http://longhairhow2.com/api" + getIntent().getExtras().getString("video")) ;
        video.start() ;
        
	}
	
	private void setActionBar()
	{
		LayoutInflater mInflater = LayoutInflater.from(this);
 
        View mCustomView = mInflater.inflate(R.layout.custom_action_bar, null);
        mTitleTextView = (TextView) mCustomView.findViewById(R.id.title_text);
        mTitleTextView.setText(getIntent().getExtras().getString("title")) ;
        mTitleTextView.setTypeface(GlobalVariable.tf_light) ;
 
        ImageButton imageButton = (ImageButton) mCustomView
                .findViewById(R.id.imageButton);
        
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
        	Bitmap bmp = ImageLoader.getInstance().loadImageSync("http://longhairhow2.com/api" + GlobalVariable.profile_photo_path, targetSize, options);
        	imageButton.setImageBitmap(GlobalVariable.getCircularBitmap(bmp)) ;
        	
        	imageButton.getLayoutParams().width = 80 ;
        	imageButton.getLayoutParams().height = 80 ;
        }
        
        imageButton.setOnClickListener(new OnClickListener() {
 
            @Override
            public void onClick(View view) {
                Toast.makeText(getApplicationContext(), "Refresh Clicked!",
                        Toast.LENGTH_LONG).show();
            }
        });
 
        try {
        	ActionBar mActionBar = getActionBar();
        	
        	if ( mActionBar != null )
        	{
        		mActionBar.setDisplayShowHomeEnabled(false);
                mActionBar.setDisplayShowTitleEnabled(false);
                
                mActionBar.setCustomView(mCustomView);
                mActionBar.setDisplayShowCustomEnabled(true);
        	}            
            
        } catch (NullPointerException e) {
        	e.printStackTrace() ;
        }
        
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
        
        JSONArray arrPrice;
		try {
			arrPrice = new JSONArray(price);
			arr_prices = new String[6] ;
	        
	        for ( int i = 0 ; i < arrPrice.length() ; i++ )
	        {
	        	JSONObject obj = (JSONObject)arrPrice.get(i) ;
	        	arr_prices[i] = obj.getString("price") ;
	        }
		} catch (JSONException e) {
			e.printStackTrace();
		}
        
        arr_charged = new Boolean[6] ;
        for ( int i = 0 ; i < 6 ; i++ )
        	arr_charged[i] = false ;
        
        grid.setAdapter(new GridDetailAdapter(this, arr_titles, arr_images));
        grid.setExpanded(true);
        
        grid.getViewTreeObserver().addOnGlobalLayoutListener(
    	    new ViewTreeObserver.OnGlobalLayoutListener() {

    	       @Override
    	        public void onGlobalLayout() {
    	            grid.getViewTreeObserver().removeGlobalOnLayoutListener(this);
    	            setVideoController() ;
    	        }
    	    });
        
        grid.setOnItemClickListener(new OnItemClickListener() {
        	public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
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
                
                label_cart.setText( String.valueOf(total_videos) + " Video in Cart") ;
            }
		}) ;
        
        btnCart.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				sendCartRequest() ;
			}
		}) ;
        
	}
	
	private void sendCartRequest()
	{
		nRequestKind = 1 ;
		if ( _dialog_progress == null || !_dialog_progress.isShowing() )
		{
			_dialog_progress = ProgressDialog.show(FeaturedDetailActivity.this, "Connecting Server...", 
	    				"Please wait a sec.", true);			
		}	
    	
    	MultipartEntityBuilder builder = MultipartEntityBuilder.create() ;
		builder.addTextBody("action", "/sales/putCustom", ContentType.TEXT_PLAIN);
		builder.addTextBody("user_id", GlobalVariable.user_id, ContentType.TEXT_PLAIN);
		builder.addTextBody("accessToken", GlobalVariable.accessToken, ContentType.TEXT_PLAIN);
		builder.addTextBody("email", GlobalVariable.email, ContentType.TEXT_PLAIN);
		builder.addTextBody("b_id", b_id, ContentType.TEXT_PLAIN);
		builder.addTextBody("platform", "android", ContentType.TEXT_PLAIN);
		builder.addTextBody("price", String.format("%.02f", total_videos*0.99), ContentType.TEXT_PLAIN);
		
		String temp = "[" ;
		for ( int i = 0 ; i < arr_vids.length ; i++ )
		{
			if ( arr_charged[i]  )
				temp = temp + String.format("%c%s%c,", 0x22, arr_vids[i], 0x22) ;
		}
		
		temp = temp.substring(0, temp.length() - 1) ;
		temp = temp + "]" ;
		
		builder.addTextBody("v_ids", temp, ContentType.TEXT_PLAIN) ;
		GlobalVariable.request_url = "http://longhairhow2.com/api/sales/putCustom" ;
		
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
    			_dialog_progress = ProgressDialog.show(this, "Connecting Server...", "Getting Access Token... Please wait a sec.", true);
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
						Toast.makeText(FeaturedDetailActivity.this, "Purchase Successfully Recorded.", Toast.LENGTH_LONG).show() ;
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
