package au.com.sharonblain.featured;

import com.squareup.picasso.Picasso;

import ImageCache.ImageLoader;
import android.app.ActionBar;
import android.app.Activity;
import android.app.ProgressDialog;
import android.net.Uri;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
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

public class FeaturedDetailActivity extends Activity implements AsyncResponse {

	private HttpPostTask httpTask = new HttpPostTask() ;
	private ProgressDialog _dialog_progress ;
	
	private TextView title ;
	private ImageView profile_photo ;
	private VideoView video ;
	private ExpandableHeightGridView grid ;
	private ImageView btnCart ;
	//private TextView label_cart ;
	private TextView mTitleTextView ;
	private TextView mDescriptionTextView ;
	private String[] arr_images ;
	private String[] arr_titles ;
	
	DisplayMetrics dm;
	MediaController media_Controller;
	
	private void getLayoutObjects()
	{
		title = (TextView)findViewById(R.id.label_type) ;
		profile_photo = (ImageView)findViewById(R.id.img_profile_photo) ;
		video = (VideoView)findViewById(R.id.video_view) ;
		grid = (ExpandableHeightGridView)findViewById(R.id.grid_photos) ;
		mDescriptionTextView = (TextView)findViewById(R.id.label_description) ;
		btnCart = (ImageView)findViewById(R.id.btnAddCart) ;
		//label_cart = (TextView)findViewById(R.id.label_cart_info) ;
		
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
		ActionBar mActionBar = getActionBar();
        mActionBar.setDisplayShowHomeEnabled(false);
        mActionBar.setDisplayShowTitleEnabled(false);
        LayoutInflater mInflater = LayoutInflater.from(this);
 
        View mCustomView = mInflater.inflate(R.layout.custom_action_bar, null);
        mTitleTextView = (TextView) mCustomView.findViewById(R.id.title_text);
        mTitleTextView.setText(getIntent().getExtras().getString("title")) ;
 
        ImageButton imageButton = (ImageButton) mCustomView
                .findViewById(R.id.imageButton);
        
        ImageLoader p = new ImageLoader(FeaturedDetailActivity.this) ;
        
        if ( GlobalVariable.profile_photo_path != null && GlobalVariable.profile_photo_path.length() > 1 )
        	p.DisplayImage(GlobalVariable.profile_photo_path, imageButton) ;
        
        imageButton.setOnClickListener(new OnClickListener() {
 
            @Override
            public void onClick(View view) {
                Toast.makeText(getApplicationContext(), "Refresh Clicked!",
                        Toast.LENGTH_LONG).show();
            }
        });
 
        mActionBar.setCustomView(mCustomView);
        mActionBar.setDisplayShowCustomEnabled(true);
	}
	
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_featured_detail);
        
        _dialog_progress = new ProgressDialog(FeaturedDetailActivity.this) ;
        
        getLayoutObjects() ;		
        setVideoController() ;
        setActionBar() ;
        
        String _image_urls = getIntent().getExtras().getString("images") ;
        if ( _image_urls != null & _image_urls.length() > 1 )
        {
        	_image_urls = _image_urls.replace(" ", "") ;
        	arr_images = _image_urls.split("\\^") ;
        }
        
        String _titles = getIntent().getExtras().getString("titles") ;
        if ( _titles != null & _titles.length() > 1 )
        {
        	_titles = _titles.replace(" ", "") ;
        	arr_titles = _titles.split("\\^") ;
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
        
        grid.setAdapter(new GridDetailAdapter(this, arr_titles, arr_images));
        grid.setExpanded(true);
        
        btnCart.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				
			}
		}) ;
        
	}
	
	@Override
	public void processFinish(String output) {
		httpTask.delegate = FeaturedDetailActivity.this ;
        
		if (_dialog_progress.isShowing())
        	_dialog_progress.dismiss() ;
	}
}
