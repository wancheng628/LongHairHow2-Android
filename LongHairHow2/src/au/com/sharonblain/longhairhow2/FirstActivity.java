package au.com.sharonblain.longhairhow2;

import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.view.View;
import android.view.View.OnClickListener;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.StringTokenizer;
import java.util.TimeZone;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import com.facebook.android.AsyncFacebookRunner;
import com.facebook.android.AsyncFacebookRunner.RequestListener;
import com.facebook.android.DialogError;
import com.facebook.android.Facebook;
import com.facebook.android.Facebook.DialogListener;
import com.facebook.android.FacebookError;
import com.squareup.picasso.Picasso;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;

import android.util.Log;
import android.util.TypedValue;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.Toast;
import au.com.sharonblain.request_server.AsyncResponse;
import au.com.sharonblain.request_server.GlobalVariable;
import au.com.sharonblain.request_server.HttpPostTask;

public class FirstActivity extends Activity implements AsyncResponse {

	private HelperUtils utils;
    private ArrayList<String> imagePaths = new ArrayList<String>();
    private GridViewImageAdapter adapter;
    private GridView gridView;
    private int columnWidth;
    
    private SharedPreferences prefs ;
    private Date cur_sydney_time ;
    
    private String access_token_url = "http://longhairhow2.com/api/common/access-token/grant" ;
    private HttpPostTask httpTask = new HttpPostTask() ;
    private int _request_kind ;
    
    private ProgressDialog _dialog_progress ;
    
    private static String APP_ID = "296311230523628";
    //private static String APP_SECRET = "0b6facc6007b9118d3680c1dbfb2a077";
    private Facebook facebook;
    private AsyncFacebookRunner mAsyncRunner;
    private SharedPreferences mPrefs;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_first);
 
        httpTask.delegate = FirstActivity.this ;
        _dialog_progress = new ProgressDialog(FirstActivity.this) ;
        
        gridView = (GridView) findViewById(R.id.grid_view);
        utils = new HelperUtils(this);
        InitilizeGridLayout();

        imagePaths = utils.getFilePaths();
        adapter = new GridViewImageAdapter(FirstActivity.this, imagePaths, columnWidth);
        gridView.setAdapter(adapter);
        
        facebook = new Facebook(APP_ID);
        mAsyncRunner = new AsyncFacebookRunner(facebook);
        
        ImageView btnFBLogin = (ImageView) findViewById(R.id.imgFacebookLogin);
        btnFBLogin.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				loginToFacebook();
			}
		}) ;
        
        ImageView btnArtLogin = (ImageView) findViewById(R.id.imgArtLogin);
        btnArtLogin.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				final CharSequence[] items = {"Login", "Register", "CANCEL"} ;

                AlertDialog.Builder builder = new AlertDialog.Builder(FirstActivity.this);
                builder.setTitle("Login/Register")
                	.setItems(items, new DialogInterface.OnClickListener() {
                		public void onClick(DialogInterface dialog, int which) {
                			switch ( which ) {
                				case 0:{
                					login() ;
                					break ;
                				}
                        	   
                				case 1:{
                					register() ;
                					break ;
                				}
                        	   
                				case 2:{
                					dialog.dismiss() ;
                					break ;
                				}
                			}
                		}

                		private void register() {
                			
                			GlobalVariable.tempBirthday = "" ;
                			GlobalVariable.tempCountry = "" ;
                			GlobalVariable.tempGender = "" ;
                			
                			Intent myIntent = new Intent(FirstActivity.this, RegisterActivity.class);
							startActivity(myIntent);
                		}

						private void login() {
							Intent myIntent = new Intent(FirstActivity.this, LoginActivity.class);
							startActivity(myIntent);
						}
                });
                
                AlertDialog p = builder.create() ;
                p.show() ;
			}
		}) ;
        
        prefs = PreferenceManager.getDefaultSharedPreferences(this); 
        
        if ( GlobalVariable.accessToken.length() < 1 )
        	getAccessToken(true) ;
        
        Button btnSkip = (Button)findViewById(R.id.btnSkip) ;
        btnSkip.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				
			}
		}) ;
        
    }
    
    public void loginToFacebook() {
        mPrefs = getPreferences(MODE_PRIVATE);
        String access_token = mPrefs.getString("access_token", null);
        long expires = mPrefs.getLong("access_expires", 0);
     
        if (access_token != null) {
            facebook.setAccessToken(access_token);
        }
     
        if (expires != 0) {
            facebook.setAccessExpires(expires);
        }
     
        if (!facebook.isSessionValid()) {
            facebook.authorize(this, new String[] { "email", "publish_stream" }, new DialogListener() {
            	@Override
            	public void onCancel() {
            		// Function to handle cancel event
                }
     
                @Override
                public void onComplete(Bundle values) {
                	
                	if ( !_dialog_progress.isShowing() )
                		_dialog_progress = ProgressDialog.show(FirstActivity.this, "Connecting Facebook...", "Loading profile informations... Please wait a sec.", true);
                	
                    getProfileInformation() ;
                }
                						
                @Override
				public void onFacebookError(FacebookError e) {
					
				}

				@Override
				public void onError(DialogError e) {
					
				}
            });
        }
    }
    
    public void getProfileInformation() {
        mAsyncRunner.request("me", new RequestListener() {
            @SuppressWarnings("unchecked")
			@Override
            public void onComplete(String response, Object state) {
                Log.d("Profile", response);
                
                _request_kind = 2 ;
                
                String json = response;
                try {
                    JSONObject profile = new JSONObject(json);
                    String url_profile_photo = "https://graph.facebook.com/" + profile.getString("id") + "/picture?type=large" ;
                    Bitmap _profile_photo = Picasso.with(FirstActivity.this).load(Uri.parse(url_profile_photo)).get() ;
                    
                    ArrayList<NameValuePair> params = new ArrayList<NameValuePair>() ;
    				params.add(new BasicNameValuePair("action", "/user/login"));
    				params.add(new BasicNameValuePair("user_id", "-10"));
    				params.add(new BasicNameValuePair("accessToken", GlobalVariable.accessToken));
    				
    				if (profile.has("email"))
    					params.add(new BasicNameValuePair("email", profile.getString("email")));
    				if (profile.has("id"))
    					params.add(new BasicNameValuePair("fb_id", profile.getString("id")));
    				if (profile.has("first_name"))
    					params.add(new BasicNameValuePair("f_name", profile.getString("first_name")));
    				if (profile.has("last_name"))
    					params.add(new BasicNameValuePair("l_name", profile.getString("last_name")));
    				if (profile.has("locale"))
    				{
    					StringTokenizer tempStringTokenizer = new StringTokenizer(profile.getString("locale"),"_");
    					String l="", c = "";
    				    if(tempStringTokenizer.hasMoreTokens())
    				    	l = tempStringTokenizer.nextElement().toString();
    				    if(tempStringTokenizer.hasMoreTokens())
    				    	c = tempStringTokenizer.nextElement().toString();
    				    Locale p = new Locale(l,c);
    				    
    					params.add(new BasicNameValuePair("country", p.getCountry()));
    				}    					
    				if (profile.has("gender"))
    					params.add(new BasicNameValuePair("gender", profile.getString("gender")));
    				if (profile.has("birthday"))
    					params.add(new BasicNameValuePair("dob", profile.getString("birthday")));
    				
    				GlobalVariable.request_url = "http://longhairhow2.com/api/user/login" ;
    				GlobalVariable.request_register = 1 ;
    				GlobalVariable.photo = _profile_photo ;
    				
    				httpTask = new HttpPostTask() ;
    				httpTask.delegate = FirstActivity.this ;
    				httpTask.execute(params) ;
    				
    				File file = new File(Environment.getExternalStorageDirectory(),  ("longHair_profile.jpg"));
    			    if(file.exists()){
    				    file.delete();				    
    			    }
     
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (IOException e) {
					e.printStackTrace();
				}
            }
            
            @Override
            public void onFacebookError(FacebookError e, Object state) {
            }

			@Override
			public void onIOException(IOException e, Object state) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void onFileNotFoundException(FileNotFoundException e,
					Object state) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void onMalformedURLException(MalformedURLException e,
					Object state) {
				// TODO Auto-generated method stub
				
			}
        });
    }
    
    @SuppressWarnings("unchecked")
	private void getAccessToken(Boolean _first)
    {
    	if ( !_dialog_progress.isShowing() )
    		_dialog_progress = ProgressDialog.show(this, "Connecting Server...", 
    				"Getting Access Token... Please wait a sec.", true);
    	
    	if ( !_first )
    	{
    		SharedPreferences.Editor editor = prefs.edit();
    		editor.putString("user_id","-10");
    		editor.commit();
    	}
    	
    	getSydneyTime() ;
    	
    	String _userid = prefs.getString("user_id", "-10") ;
    	_request_kind = 1 ;
    	ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("action", "/common/access-token/grant"));
		params.add(new BasicNameValuePair("user_id", _userid));
		GlobalVariable.request_url = access_token_url ;
		
		httpTask = new HttpPostTask() ;
		httpTask.delegate = this;
		httpTask.execute(params) ;
    }
    
    private void setAccessToken(JSONObject jsonObj) throws JSONException
    {
    	JSONObject result = jsonObj.getJSONObject("results") ;
		
		GlobalVariable.accessToken = result.getString("accessToken") ;
		GlobalVariable.validity = result.getString("validity") ;
		GlobalVariable.user_id = result.getString("user_id") ;
    }
    
    private void getSydneyTime()
    {
    	TimeZone tz = TimeZone.getTimeZone("GMT+10:00");
    	Calendar c = Calendar.getInstance(tz);
    	cur_sydney_time = c.getTime() ;
    }
 
    @SuppressLint("SimpleDateFormat")
	private Date getDateFromString(String str_date)
    {
    	Date _date = null ;
		SimpleDateFormat  format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss") ;  
		
		try {  
			_date = format.parse(str_date) ;     					    
		} catch (ParseException e) {  
		    e.printStackTrace() ;  
		}
		
		return _date ;
		
    }
    
    public void processFinish(String output){
    	
    	if ( _dialog_progress.isShowing() )
			_dialog_progress.dismiss() ;
    	
    	if ( _request_kind == 1 )		// Get Access Token
    	{
    		if (output.length() > 0) {
    			try {
    				JSONObject jsonObj = new JSONObject(output) ;
    				if (jsonObj.get("type").equals("Success"))
    				{
    					JSONObject result = jsonObj.getJSONObject("results") ;
    					Date validity = getDateFromString(result.getString("validity")) ;
    					
    					if ( validity.after(cur_sydney_time) )
    					{
    						Toast.makeText(FirstActivity.this, "Success, Valid - Access token : " + result.getString("accessToken"), Toast.LENGTH_LONG).show() ;
    						GlobalVariable.f_valid = true ;
    						setAccessToken(jsonObj) ;
    					}
    					else
    					{
    						Toast.makeText(FirstActivity.this, "Date Expired", Toast.LENGTH_LONG).show() ;
    						
    						GlobalVariable.f_valid = false ;
    						getAccessToken(false) ;
    					}
    						
    				}
    				else if (jsonObj.get("type").equals("Error"))
    				{
    					Toast.makeText(FirstActivity.this, "ERROR, Restarting the getAccessToken", Toast.LENGTH_LONG).show() ;
    					GlobalVariable.f_valid = false ;
    					getAccessToken(false) ;
    				}
    			
    			} catch (JSONException e) {
    				e.printStackTrace();
    				GlobalVariable.f_valid = false ;
    				getAccessToken(false) ;
    			}
    		} else {
    			Toast.makeText(FirstActivity.this, "Couldn't get any data from the url", Toast.LENGTH_LONG).show() ;
    			GlobalVariable.f_valid = false ;
    			getAccessToken(false) ;
    		}
    	}
    	
    	if ( _request_kind == 2 )		// Get Access Token
    	{
    		if (output.length() > 0) {
    			try {
    				JSONObject jsonObj = new JSONObject(output) ;
    				Toast.makeText(FirstActivity.this, jsonObj.getString("type") + " - " + jsonObj.getString("message"), Toast.LENGTH_LONG).show() ;
    				
    			}catch (JSONException e) {
    				e.printStackTrace();
    				GlobalVariable.f_valid = false ;
    				
    			}
    		} else {
    			Toast.makeText(FirstActivity.this, "Couldn't get any data from the url", Toast.LENGTH_LONG).show() ;
    			GlobalVariable.f_valid = false ;
    			
    		}
    	}
    	
    }
    
    private void InitilizeGridLayout() {
        Resources r = getResources();
        float padding = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                AppConstants.GRID_PADDING, r.getDisplayMetrics());
 
        columnWidth = (int) ((utils.getScreenWidth() - ((AppConstants.NUM_OF_COLUMNS + 1) * padding)) / AppConstants.NUM_OF_COLUMNS);
 
        gridView.setNumColumns(AppConstants.NUM_OF_COLUMNS);
        gridView.setColumnWidth(columnWidth);
        gridView.setStretchMode(GridView.NO_STRETCH);
        gridView.setPadding((int) padding, (int) padding, (int) padding,
                (int) padding);
        gridView.setHorizontalSpacing((int) padding);
        gridView.setVerticalSpacing((int) padding);
        
        gridView.setEnabled(false) ;
        gridView.setVerticalScrollBarEnabled(false) ;
    }
    
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        // Checks the orientation of the screen
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            Toast.makeText(this, "landscape", Toast.LENGTH_SHORT).show();
        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT){
            Toast.makeText(this, "portrait", Toast.LENGTH_SHORT).show();
        }
    }

    
}
