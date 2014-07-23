package au.com.sharonblain.uservideo;

import java.util.Date;

import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;
import au.com.sharonblain.longhairhow2.R;
import au.com.sharonblain.request_server.AsyncResponse;
import au.com.sharonblain.request_server.GlobalVariable;
import au.com.sharonblain.request_server.HttpPostTask;

public class UserVideoActivity extends Activity implements AsyncResponse {

	private ProgressDialog _dialog_progress ;
	private HttpPostTask httpTask = new HttpPostTask() ;
	private int nRequestKind = 1 ;
	private String selected_vid_id = "" ;
	
	private GridView grid ;
	
	public class UserVideoItem {
		
		String vid_title ;
		String vid_url ;
		String v_id ;
		String p_id ;
		String vid_image ;
		
	}
	
	private UserVideoItem[] videos ;
	
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
						videos = new UserVideoItem[result.length()] ;
						 
						for ( int i = 0 ; i < result.length() ; i++ )
						{
							try {
								JSONObject vids = result.getJSONObject(i) ;
								JSONArray vids_array = vids.getJSONArray("vids") ;
								JSONObject vid = vids_array.getJSONObject(0) ;
								
								UserVideoItem temp = new UserVideoItem() ;
								temp.p_id = vid.getString("p_id") ;
								temp.v_id = vid.getString("v_id") ;
								temp.vid_image = "http://longhairhow2.com/api" + vid.getString("vid_image") ;
								temp.vid_title = vids.getString("title") ;
								temp.vid_url = "http://longhairhow2.com/api" + vid.getString("vid_url") ;
							
								videos[i] = temp ;
							}catch (JSONException e) {
								e.printStackTrace();								
							}
						}
						
						VideoAdapter adapter = new VideoAdapter(UserVideoActivity.this, videos) ;
						grid.setAdapter(adapter) ;
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
				Log.e("ServiceHandler", "Couldn't get any data from the url") ;
				
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
    		_dialog_progress = ProgressDialog.show(this, "Connecting Server...", "Getting Access Token... Please wait a sec.", true);    		
    	}
    				
    	
    	GlobalVariable.getSydneyTime() ;
    	
    	MultipartEntityBuilder builder = MultipartEntityBuilder.create() ;
    	builder.addTextBody("action", "/common/access-token/grant", ContentType.TEXT_PLAIN);
    	builder.addTextBody("user_id", GlobalVariable.user_id, ContentType.TEXT_PLAIN);
		
		GlobalVariable.request_url = "http://longhairhow2.com/api/common/access-token/grant" ;
		
		httpTask = new HttpPostTask() ;
		httpTask.delegate = this;
		httpTask.execute(builder) ;
    }
	
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState) ;
        setContentView(R.layout.activity_uservideo) ;
        
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
				myIntent.putExtra("address", "http://longhairhow2.com/api/vid/stream.php?user_id=" + GlobalVariable.user_id + 
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
    			_dialog_progress = ProgressDialog.show(this, "Connecting Server...", 
        				"Please wait a sec.", true) ;
    		}catch (NullPointerException e) {
    			e.printStackTrace() ;
    		}
    	}
    		
    	MultipartEntityBuilder builder = MultipartEntityBuilder.create() ;
    	builder.addTextBody("action", "/user/bundles/get", ContentType.TEXT_PLAIN) ;
    	builder.addTextBody("user_id", GlobalVariable.user_id, ContentType.TEXT_PLAIN) ;
    	builder.addTextBody("u_id", GlobalVariable.user_id, ContentType.TEXT_PLAIN) ;
    	builder.addTextBody("accessToken", GlobalVariable.accessToken, ContentType.TEXT_PLAIN) ;
		
		GlobalVariable.request_url = "http://longhairhow2.com/api/user/bundles/get" ;
		
		httpTask = new HttpPostTask() ;
		httpTask.delegate = UserVideoActivity.this ;
		httpTask.execute(builder) ;		
	}
}
