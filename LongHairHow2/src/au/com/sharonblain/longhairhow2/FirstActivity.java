package au.com.sharonblain.longhairhow2;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.view.View;
import android.view.View.OnClickListener;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.StringTokenizer;

import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.facebook.android.AsyncFacebookRunner;
import com.facebook.android.AsyncFacebookRunner.RequestListener;
import com.facebook.android.DialogError;
import com.facebook.android.Facebook;
import com.facebook.android.Facebook.DialogListener;
import com.facebook.android.FacebookError;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Configuration;
import android.content.res.Resources;

import android.util.Base64;
import android.util.Log;
import android.util.TypedValue;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.Toast;
import au.com.sharonblain.request_server.AsyncResponse;
import au.com.sharonblain.request_server.GlobalVariable;
import au.com.sharonblain.request_server.HttpPostTask;

@SuppressWarnings("deprecation")
public class FirstActivity extends Activity implements AsyncResponse {

	private HelperUtils utils;
    private ArrayList<String> imagePaths = new ArrayList<String>();
    private GridViewImageAdapter adapter;
    private GridView gridView;
    private int columnWidth;
    
    private SharedPreferences prefs ;
    
    private String access_token_url = "http://longhairhow2.com/api/common/access-token/grant" ;
    private HttpPostTask httpTask = new HttpPostTask() ;
    private int _request_kind ;
    
    private ProgressDialog _dialog_progress ;
    
    private static String APP_ID = "296311230523628";
    //private static String APP_SECRET = "0b6facc6007b9118d3680c1dbfb2a077";
    private Facebook facebook;
    private AsyncFacebookRunner mAsyncRunner;
    private SharedPreferences mPrefs;
    
    private JSONObject profile ;
    
    class DownloadFileFromURL extends AsyncTask<String, String, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();                
        }

        @Override
        protected String doInBackground(String... f_url) {
            int count;
            try {
                URL url = new URL(f_url[0]);
                URLConnection conection = url.openConnection();
                conection.connect();

                int lenghtOfFile = conection.getContentLength();
                InputStream input = new BufferedInputStream(url.openStream(), 8192);
                OutputStream output = new FileOutputStream(Environment.getExternalStorageDirectory().toString() + "/longhair_temp.jpg");

                byte data[] = new byte[1024];
                long total = 0;

                while ((count = input.read(data)) != -1) {
                    total += count;
                    publishProgress("" + (int) ((total * 100) / lenghtOfFile));
                    output.write(data, 0, count);
                }

                output.flush();
                output.close();
                input.close();

            } catch (Exception e) {
                Log.e("Error: ", e.getMessage());
            }

            return null;
        }

        @Override
        protected void onPostExecute(String file_url) {
            
            MultipartEntityBuilder builder = MultipartEntityBuilder.create(); 
            builder.addTextBody("action", "/user/login", ContentType.TEXT_PLAIN) ;
			builder.addTextBody("user_id", "-10", ContentType.TEXT_PLAIN);
			builder.addTextBody("accessToken", GlobalVariable.accessToken, ContentType.TEXT_PLAIN);
			
			_request_kind = 2 ;
			
			try {
				if (profile.has("email"))
					builder.addTextBody("email", profile.getString("email"), ContentType.TEXT_PLAIN);
				if (profile.has("id"))
					builder.addTextBody("fb_id", profile.getString("id"), ContentType.TEXT_PLAIN);
				if (profile.has("first_name"))
					builder.addTextBody("f_name", profile.getString("first_name"), ContentType.TEXT_PLAIN);
				if (profile.has("last_name"))
					builder.addTextBody("l_name", profile.getString("last_name"), ContentType.TEXT_PLAIN);
				if (profile.has("locale"))
				{
					StringTokenizer tempStringTokenizer = new StringTokenizer(profile.getString("locale"),"_");
					String l="", c = "";
				    if(tempStringTokenizer.hasMoreTokens())
				    	l = tempStringTokenizer.nextElement().toString();
				    if(tempStringTokenizer.hasMoreTokens())
				    	c = tempStringTokenizer.nextElement().toString();
				    Locale p = new Locale(l,c);
				    
				    builder.addTextBody("country", p.getCountry(), ContentType.TEXT_PLAIN);
				}
				
				if (profile.has("gender"))
					builder.addTextBody("gender", profile.getString("gender"), ContentType.TEXT_PLAIN);
				if (profile.has("birthday"))
					builder.addTextBody("dob", profile.getString("birthday"), ContentType.TEXT_PLAIN);
				else
					builder.addTextBody("dob", "2000-01-01", ContentType.TEXT_PLAIN);
				
				GlobalVariable.request_url = "http://longhairhow2.com/api/user/login" ;
				GlobalVariable.request_register = 1 ;
				GlobalVariable.profile_photo_path = Environment.getExternalStorageDirectory().toString() + "/longhair_temp.jpg" ;
				File file = new File(GlobalVariable.profile_photo_path) ;
				
				builder.addPart("profile_pic", new FileBody(file)) ;
				httpTask = new HttpPostTask() ;
				httpTask.delegate = FirstActivity.this ;
				httpTask.execute(builder) ;
				
			} catch (JSONException e) {
				e.printStackTrace();
				GlobalVariable.f_valid = false ;
				getAccessToken(false) ;
			}			
        }
    }
    
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
							if ( GlobalVariable.user_id == null )
								GlobalVariable.user_id = "-10" ;
							
							if ( Integer.parseInt(GlobalVariable.user_id) > 0 )
							{
								Intent myIntent = new Intent(FirstActivity.this, MainActivity.class);
								startActivity(myIntent);
							}
							else
							{
								Intent myIntent = new Intent(FirstActivity.this, LoginActivity.class);
								startActivity(myIntent);
							}
							
						}
                });
                
                AlertDialog p = builder.create() ;
                p.show() ;
			}
		}) ;
        
        prefs = PreferenceManager.getDefaultSharedPreferences(this); 
        
        if ( GlobalVariable.accessToken != null )
        {
        	if ( GlobalVariable.accessToken.length() < 1 )
            	getAccessToken(true) ;
        }
        
        Button btnSkip = (Button)findViewById(R.id.btnSkip) ;
        btnSkip.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				Intent myIntent = new Intent(FirstActivity.this, MainActivity.class);
				startActivity(myIntent);
			}
		}) ;
        
    }
    
    public void loginToFacebook() {
    	try {

			PackageInfo info = getPackageManager().getPackageInfo(	 "com.example.map_project",	 PackageManager.GET_SIGNATURES);
			for (Signature signature : info.signatures)
			{
				MessageDigest md = MessageDigest.getInstance("SHA");	 md.update(signature.toByteArray());
				Log.d("KeyHash:", Base64.encodeToString(md.digest(),	 Base64.DEFAULT));
			}

			} catch (NameNotFoundException e) {
			} catch (NoSuchAlgorithmException e) {
		}
    	
    	if ( GlobalVariable.fb_id != null && GlobalVariable.fb_id.equals("null") )
    	{
    		if ( Integer.parseInt(GlobalVariable.fb_id) > 0 )
    		{
    			Toast.makeText(FirstActivity.this, "You're already signed with facebook.", Toast.LENGTH_LONG).show() ;
        		return ;
    		}    		
    	}
    	
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
            facebook.authorize(FirstActivity.this, new String[] { "email"}, new DialogListener() {
            	@Override
            	public void onCancel() {
            		// Function to handle cancel event
                }
     
                @Override
                public void onComplete(Bundle values) {
                	
                	if ( _dialog_progress == null || !_dialog_progress.isShowing() )
                	{
                		_dialog_progress = ProgressDialog.show(FirstActivity.this, "Connecting Facebook...", "Loading profile informations... Please wait a sec.", true);                		
                	}
                	
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
        
    public void logoutFromFacebook() {
        mAsyncRunner.logout(this, new RequestListener() {
            @Override
            public void onComplete(String response, Object state) {
                Log.d("Logout from Facebook", response);
                if (Boolean.parseBoolean(response) == true) {
                    // User successfully Logged out
                }
            }
     
            @Override
            public void onIOException(IOException e, Object state) {
            }
     
            @Override
            public void onFileNotFoundException(FileNotFoundException e,
                    Object state) {
            }
     
            @Override
            public void onMalformedURLException(MalformedURLException e,
                    Object state) {
            }
     
            @Override
            public void onFacebookError(FacebookError e, Object state) {
            }
        });
    }
    
    public void getProfileInformation() {
        mAsyncRunner.request("me", new RequestListener() {
            @Override
            public void onComplete(String response, Object state) {
                _request_kind = 2 ;
                String json = response;
                try {
                    profile = new JSONObject(json);
                    String url_profile_photo = "https://graph.facebook.com/" + profile.getString("id") + "/picture?type=large" ;
                    
                    new DownloadFileFromURL().execute(url_profile_photo);
                   
                } catch (JSONException e) {
                    e.printStackTrace();
                } 
            }
            
            @Override
            public void onFacebookError(FacebookError e, Object state) {
            }

			@Override
			public void onIOException(IOException e, Object state) {
				
			}

			@Override
			public void onFileNotFoundException(FileNotFoundException e,
					Object state) {
				
			}

			@Override
			public void onMalformedURLException(MalformedURLException e,
					Object state) {
				
			}
        });
    }
    
    private void getAccessToken(Boolean _first)
    {
    	if ( _dialog_progress == null || !_dialog_progress.isShowing() )
    	{
    		_dialog_progress = ProgressDialog.show(this, "Connecting Server...", "Getting Access Token... Please wait a sec.", true);    		
    	}	
    	
    	if ( !_first )
    	{
    		SharedPreferences.Editor editor = prefs.edit();
    		editor.putString("user_id","-10");
    		editor.commit();
    	}
    	
    	GlobalVariable.getSydneyTime() ;
    	
    	String _userid = prefs.getString("user_id", "-10") ;
    	_request_kind = 1 ;
    	
    	MultipartEntityBuilder builder = MultipartEntityBuilder.create();    
    	builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
		builder.addTextBody("action", "/common/access-token/grant", ContentType.TEXT_PLAIN) ;
		builder.addTextBody("user_id", _userid, ContentType.TEXT_PLAIN) ;
		
		GlobalVariable.request_url = access_token_url ;
		
		httpTask = new HttpPostTask() ;
		httpTask.delegate = this;
		httpTask.execute(builder) ;
    }
    
    private void setAccessToken(JSONObject jsonObj) throws JSONException
    {
    	JSONObject result = jsonObj.getJSONObject("results") ;
		
		GlobalVariable.accessToken = result.getString("accessToken") ;
		GlobalVariable.validity = result.getString("validity") ;
		GlobalVariable.user_id = result.getString("user_id") ;
    }
    
    public void processFinish(String output) throws IllegalArgumentException{
    	
    	if ( (_dialog_progress != null) && (_dialog_progress.isShowing()) )
		{
			try {
				_dialog_progress.dismiss() ;
				_dialog_progress = null;
		    } catch (Exception e) {
		        e.printStackTrace() ;
		    }
		}
    	
    	if ( _request_kind == 1 )		// Get Access Token
    	{
    		if (output.length() > 0) {
    			try {
    				JSONObject jsonObj = new JSONObject(output) ;
    				if (jsonObj.get("type").equals("Success"))
    				{
    					JSONObject result = jsonObj.getJSONObject("results") ;
    					Date validity = GlobalVariable.getDateFromString(result.getString("validity")) ;
    					
    					if ( validity.after(GlobalVariable.cur_sydney_time) )
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
    	
    	if ( _request_kind == 2 )		// Get Facebook profile
    	{
    		if (output.length() > 0) {
    			JSONObject jsonObj;
				try {
					jsonObj = new JSONObject(output);

	    			if (jsonObj.get("type").equals("Success"))
					{
	    				String _result = jsonObj.getString("results") ;
	    				JSONArray arr_result = new JSONArray(_result) ;
	    				JSONObject result = arr_result.getJSONObject(0) ;
	    				
	    				GlobalVariable.f_name = result.getString("f_name") ;
	    				GlobalVariable.l_name = result.getString("l_name") ;
	    				GlobalVariable.email = result.getString("email") ;
	    				GlobalVariable.country = result.getString("country") ;
	    				
	    				if (result.getString("u_id") != null && result.getString("u_id").length() > 0)
	    					GlobalVariable.user_id = result.getString("u_id") ;
	    				
	    				Intent myIntent = new Intent(FirstActivity.this, MainActivity.class);
						startActivity(myIntent);
        			}	
	    			else
	    			{
	    				Toast.makeText(FirstActivity.this, jsonObj.getString("type") + " - " + jsonObj.getString("message"), Toast.LENGTH_LONG).show() ;
	    			}
					
				} catch (JSONException e1) {
					e1.printStackTrace();
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
