package au.com.sharonblain.longhairhow2;

import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;
import au.com.sharonblain.request_server.AsyncResponse;
import au.com.sharonblain.request_server.GlobalVariable;
import au.com.sharonblain.request_server.HttpPostTask;
 
public class SplashScreen extends Activity implements AsyncResponse {
 
    // Splash screen timer
    private static int SPLASH_TIME_OUT = 500;
    private ProgressDialog _dialog_progress ;
    private SharedPreferences prefs ;
    private HttpPostTask httpTask = new HttpPostTask() ;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
 
        _dialog_progress = new ProgressDialog(SplashScreen.this) ;
        GlobalVariable.accessToken = "" ;
        GlobalVariable.tf_light = Typeface.createFromAsset(getAssets(), "DINPro-Light.otf");
        GlobalVariable.tf_medium = Typeface.createFromAsset(getAssets(), "DINPro-Medium.otf");
        
        prefs = getSharedPreferences("user_info", Context.MODE_PRIVATE) ;
        httpTask.delegate = SplashScreen.this ;
        
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                
            	if (( prefs.getString("prev_password", "") != null && prefs.getString("prev_password", "").length() > 0 ) && 
        				( prefs.getString("prev_email", "") != null && prefs.getString("prev_email", "").length() > 0 ))
        		{
        			login(prefs.getString("prev_email", ""), prefs.getString("prev_password", "")) ;
        		}
            	else if ( prefs.getString("fb_id", "") != null && prefs.getString("fb_id", "").length() > 0 )
            	{
            		GlobalVariable.f_name = prefs.getString("f_name", "") ;
            		GlobalVariable.l_name = prefs.getString("l_name", "") ;
            		GlobalVariable.email = prefs.getString("email", "") ;
            		GlobalVariable.country = prefs.getString("country", "Australia") ;
            		GlobalVariable.dob = prefs.getString("dob", "") ;
            		GlobalVariable.fb_id = prefs.getString("fb_id", "") ;
            		GlobalVariable.tempGender = prefs.getString("tempGender", "M") ;
            		GlobalVariable.profile_photo_path = prefs.getString("profile_photo_path", "") ;
            		GlobalVariable.user_id = prefs.getString("user_id", "-10") ;
            		
            		Intent i = new Intent(SplashScreen.this, MainActivity.class);
                    startActivity(i);
            	}
            	else
            	{
            		Intent i = new Intent(SplashScreen.this, FirstActivity.class);
                    startActivity(i);
            	}
                
 
                // close this activity
                //finish();
            }
        }, SPLASH_TIME_OUT);
        
        
    }
    
    protected void login(String _email, String _password) {
    	
    	if ( _dialog_progress == null || !_dialog_progress.isShowing() )
    	{
    		_dialog_progress = ProgressDialog.show(this, "Loading...", "Please wait...", true);    		
    	}			
    	
    	MultipartEntityBuilder builder = MultipartEntityBuilder.create() ;
    	builder.addTextBody("action", "/user/login", ContentType.TEXT_PLAIN);
    	builder.addTextBody("user_id", "-10", ContentType.TEXT_PLAIN);
    	if ( GlobalVariable.accessToken == null )
    		GlobalVariable.accessToken = "" ;
    	
    	builder.addTextBody("accessToken", GlobalVariable.accessToken, ContentType.TEXT_PLAIN);
    	builder.addTextBody("email", _email, ContentType.TEXT_PLAIN);
    	builder.addTextBody("pwd", GlobalVariable.md5(_password), ContentType.TEXT_PLAIN);			
		
		GlobalVariable.request_url = GlobalVariable.API_URL + "/user/login" ;
		
		httpTask = new HttpPostTask() ;
		httpTask.delegate = SplashScreen.this ;
		httpTask.execute(builder) ;
		
	}
 
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
    	  super.onConfigurationChanged(newConfig);

    	  if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
    		  this.findViewById(R.layout.activity_splash).setBackgroundResource(R.drawable.splash_landscape) ;
    	  } else {
    		  this.findViewById(R.layout.activity_splash).setBackgroundResource(R.drawable.splash_portrait) ;
    	  }
    	}

	@Override
	public void processFinish(String output) {
		if ( (_dialog_progress != null) && (_dialog_progress.isShowing()) )
		{
			try {
				_dialog_progress.dismiss() ;
				_dialog_progress = null;
		    } catch (Exception e) {
		        // nothing
		    }
		}
		
		if (output.length() > 0) {
			try {
				JSONObject jsonObj = new JSONObject(output) ;
				if (jsonObj.get("type").equals("Success"))
				{
					JSONArray result = jsonObj.getJSONArray("results") ;
					JSONObject _result = result.getJSONObject(0) ;
					
					GlobalVariable.user_id = _result.getString("u_id") ;
					GlobalVariable.f_name = _result.getString("f_name") ;
					GlobalVariable.l_name = _result.getString("l_name") ;
					GlobalVariable.email = _result.getString("email") ;
					GlobalVariable.country = _result.getString("country") ;
					GlobalVariable.dob = _result.getString("dob") ;
					GlobalVariable.fb_id = _result.getString("fb_id") ;
					GlobalVariable.tempGender = _result.getString("gender") ;
					GlobalVariable.profile_photo_path = _result.getString("profile_pic") ;

					Intent myIntent = new Intent(SplashScreen.this, MainActivity.class);
					startActivity(myIntent);
				}
				else
				{
					Toast.makeText(SplashScreen.this, jsonObj.getString("type") + " - " + jsonObj.getString("message"), Toast.LENGTH_LONG).show() ;
				}
			
			} catch (JSONException e) {
				e.printStackTrace();
				
			}
		} else {
			Log.e("ServiceHandler", "Couldn't get any data from the server.") ;
			
		}
	}
}