package au.com.sharonblain.featured;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.StringBody;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.squareup.picasso.Picasso;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import au.com.sharonblain.longhairhow2.R;
import au.com.sharonblain.request_server.AsyncResponse;
import au.com.sharonblain.request_server.GlobalVariable;
import au.com.sharonblain.request_server.HttpPostTask;

@SuppressWarnings("deprecation")
public class FeaturedActivity extends Activity implements AsyncResponse {

	private HttpPostTask httpTask = new HttpPostTask() ;
	private ProgressDialog _dialog_progress ;
	
	private ImageView imgProfilePhoto ;
	private TextView label_type ;
	private Switch swh_popular ;
	private GridView grid ;
	private Boolean f_featured ;
	
	private ArrayList<String> bun_vid_url, bun_image, bun_description, bun_title, prices ;
	
	private GridAdapter adapter ;
	private ArrayList<String> v_id, vid_url, vid_title, vid_image ;
	
	private void getLayoutObjects()
	{
		imgProfilePhoto = (ImageView)findViewById(R.id.img_profile_photo) ;
        label_type = (TextView)findViewById(R.id.label_type) ;
        swh_popular = (Switch)findViewById(R.id.switch_popular) ;
        grid = (GridView)findViewById(R.id.grid_photos) ;
	}
	
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_featured);
        
        _dialog_progress = new ProgressDialog(FeaturedActivity.this) ;
        getLayoutObjects() ;
        
        v_id = new ArrayList<String>() ;
        vid_url = new ArrayList<String>() ;
        vid_title = new ArrayList<String>() ;
        vid_image = new ArrayList<String>() ;
        
        f_featured = swh_popular.isChecked() ;
        
        if ( GlobalVariable.profile_photo_path != null && GlobalVariable.profile_photo_path.length() > 1 )
        	Picasso.with(FeaturedActivity.this).load(Uri.parse(GlobalVariable.profile_photo_path)).into(imgProfilePhoto) ;
        else
        	imgProfilePhoto.setImageBitmap(null) ;
        
        swh_popular.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
				if ( arg1 )
				{
					label_type.setText("Popular") ;
					f_featured = false ;	
					_sendFeaturedRequest("/popular/get") ;
					
				}
				else
				{
					label_type.setText("Featured") ;
					f_featured = true ;			
					_sendFeaturedRequest("/front-page/get") ;
				}
				
				Log.d("Featured", String.valueOf(f_featured)) ;
			}
		}) ;
        
        _sendFeaturedRequest("/front-page/get") ;
        
        grid.setOnItemClickListener(new OnItemClickListener() {
            
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				
				Intent myIntent = new Intent(FeaturedActivity.this, FeaturedDetailActivity.class);
				
				myIntent.putExtra("video", bun_vid_url.get(arg2)) ;
				myIntent.putExtra("title", bun_title.get(arg2)) ;
				myIntent.putExtra("prices", prices.get(arg2)) ;
				myIntent.putExtra("images", vid_image.get(arg2)) ;
				myIntent.putExtra("titles", vid_title.get(arg2)) ;				
				myIntent.putExtra("description", bun_description.get(arg2)) ;
				
				startActivity(myIntent);
				
			}
        });
	}
	
	private void _sendFeaturedRequest(String action)
	{
		if ( adapter != null )
		{
			adapter.clear() ;
			v_id = new ArrayList<String>() ;
			vid_url = new ArrayList<String>() ;
			vid_title = new ArrayList<String>() ;
			vid_image = new ArrayList<String>() ;
			
			grid.setAdapter(adapter) ;
		}
		
		if ( !_dialog_progress.isShowing() )
    		_dialog_progress = ProgressDialog.show(this, "Connecting Server...", 
    				"Please wait a sec.", true);
    	
    	MultipartEntity params = new MultipartEntity();
		try {
			params.addPart("action", new StringBody(action));
			params.addPart("user_id", new StringBody(GlobalVariable.user_id));
			params.addPart("accessToken", new StringBody(GlobalVariable.accessToken));
			params.addPart("front_type", new StringBody("hot"));
			
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		
		GlobalVariable.request_url = "http://longhairhow2.com/api/front-page/get" ;
		
		httpTask = new HttpPostTask() ;
		httpTask.delegate = FeaturedActivity.this ;
		httpTask.execute(params) ;
	}
	
	@Override
	public void processFinish(String output) {
		httpTask.delegate = FeaturedActivity.this ;
        
		if (_dialog_progress.isShowing())
        	_dialog_progress.dismiss() ;
        
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
					
					for ( int i = 0 ; i < result.length() ; i++ )
					{
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
					Toast.makeText(FeaturedActivity.this, jsonObj.getString("type") + " - " + jsonObj.getString("message"), Toast.LENGTH_LONG).show() ;
				}
			
			} catch (JSONException e) {
				e.printStackTrace();
				
			}
		} else {
			Log.e("ServiceHandler", "Couldn't get any data from the url") ;			
		}        
	}
}
