package au.com.sharonblain.yourvideo;

import java.io.UnsupportedEncodingException;

import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.StringBody;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import au.com.sharonblain.longhairhow2.R;
import au.com.sharonblain.request_server.AsyncResponse;
import au.com.sharonblain.request_server.GlobalVariable;
import au.com.sharonblain.request_server.HttpPostTask;

@SuppressWarnings("deprecation")
public class YourVideoActivity extends Activity implements AsyncResponse {

	private ProgressDialog _dialog_progress ;
	private HttpPostTask httpTask = new HttpPostTask() ;
	
	@Override
	public void processFinish(String output) {
		if ( _dialog_progress.isShowing() )
			_dialog_progress.dismiss() ;
		
		if (output.length() > 0) {
			try {
				JSONObject jsonObj = new JSONObject(output) ;
				if (jsonObj.get("type").equals("Success"))
				{
					JSONArray result = jsonObj.getJSONArray("results") ;					
				}
				else
				{
					Toast.makeText(YourVideoActivity.this, jsonObj.getString("type") + " - " + jsonObj.getString("message"), Toast.LENGTH_LONG).show() ;
				}
			
			} catch (JSONException e) {
				e.printStackTrace();
				
			}
		} else {
			Log.e("ServiceHandler", "Couldn't get any data from the url") ;
			
		}
	}
	
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState) ;
        setContentView(R.layout.activity_yourvideo) ;
        
        _dialog_progress = new ProgressDialog(YourVideoActivity.this) ;
		httpTask.delegate = YourVideoActivity.this ;
		
		getPurchasedVideos() ;
	}
	
	protected void getPurchasedVideos() {
    	
    	if ( !_dialog_progress.isShowing() )
    		_dialog_progress = ProgressDialog.show(this, "Connecting Server...", 
    				"Please wait a sec.", true);
    	
    	MultipartEntity params = new MultipartEntity();
		try {
			params.addPart("action", new StringBody("/user/bundles/get”"));
			params.addPart("user_id", new StringBody("37"));
			params.addPart("u_id", new StringBody("37"));
			params.addPart("accessToken", new StringBody(GlobalVariable.accessToken));
			
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		
		
		GlobalVariable.request_url = "http://longhairhow2.com/api/user/bundles/get" ;
		
		httpTask = new HttpPostTask() ;
		httpTask.delegate = YourVideoActivity.this ;
		httpTask.execute(params) ;
		
	}

}
