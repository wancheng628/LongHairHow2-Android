package au.com.sharonblain.longhairhow2;

import java.util.ArrayList;

import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.Toast;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;

import au.com.sharonblain.request_server.AsyncResponse;
import au.com.sharonblain.request_server.GlobalVariable;
import au.com.sharonblain.request_server.HttpPostTask;

public class LoginActivity extends Activity implements AsyncResponse{

	private HelperUtils utils;
    private ArrayList<String> imagePaths = new ArrayList<String>();
    private GridViewImageAdapter adapter;
    private GridView gridView;
    private int columnWidth;
    private ProgressDialog _dialog_progress ;
	private HttpPostTask httpTask = new HttpPostTask() ;
	private EditText txtEmail ;
	private EditText txtPassword ;
	
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
 
        httpTask.delegate = LoginActivity.this ;
        _dialog_progress = new ProgressDialog(LoginActivity.this) ;
        gridView = (GridView) findViewById(R.id.grid_view);
        utils = new HelperUtils(this);
        InitilizeGridLayout();

        imagePaths = utils.getFilePaths();
        adapter = new GridViewImageAdapter(LoginActivity.this, imagePaths, columnWidth);
        gridView.setAdapter(adapter);
        
        txtEmail = (EditText)findViewById(R.id.txtEmail) ;
        txtPassword = (EditText)findViewById(R.id.txtPassword) ;
        
        SharedPreferences prefs = getSharedPreferences("user_info", Context.MODE_PRIVATE) ;
		
        if ( prefs.getString("prev_password", "") != null && prefs.getString("prev_password", "").length() > 0 )
			txtPassword.setText(prefs.getString("prev_password", "")) ;
		
		if ( prefs.getString("prev_email", "") != null && prefs.getString("prev_email", "").length() > 0 )
			txtEmail.setText(prefs.getString("prev_email", "")) ;
		
		txtEmail.setTypeface(GlobalVariable.tf_medium) ;
		txtPassword.setTypeface(GlobalVariable.tf_medium) ;
		
		Button btnLogin = (Button)findViewById(R.id.btnLogin) ;
		btnLogin.setTypeface(GlobalVariable.tf_medium) ;
		
		btnLogin.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				try {
					if ( GlobalVariable.f_valid == null )
						GlobalVariable.f_valid = false ;
					
					if ( txtEmail.getText().toString().length() < 1 )
					{
						Toast.makeText(LoginActivity.this, "Please type your email.", Toast.LENGTH_LONG).show() ;
					}
					else if ( txtPassword.getText().toString().length() < 1 )
					{
						Toast.makeText(LoginActivity.this, "Please type your password.", Toast.LENGTH_LONG).show() ;
					}
					else
					{
						login( txtEmail.getText().toString(), txtPassword.getText().toString() ) ;
					}
				}
				catch (NullPointerException e)
				{
					e.printStackTrace() ;
				}
			}
		}) ;        
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
		httpTask.delegate = LoginActivity.this ;
		httpTask.execute(builder) ;
		
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

	@SuppressWarnings("deprecation")
	@Override
	public void processFinish(String output) throws IllegalArgumentException {
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
					
					if (_result.getString("f_name") != null && _result.getString("f_name").length() > 0)
    					GlobalVariable.f_name = _result.getString("f_name") ;
					
    				if (_result.getString("l_name") != null && _result.getString("l_name").length() > 0)
    					GlobalVariable.l_name = _result.getString("l_name") ;
    				if (_result.getString("email") != null && _result.getString("email").length() > 0)
    					GlobalVariable.email = _result.getString("email") ;
    				if (_result.getString("country") != null && _result.getString("country").length() > 0)
    					GlobalVariable.country = _result.getString("country") ;
    				if (_result.getString("dob") != null && _result.getString("dob").length() > 0)
    					GlobalVariable.dob = _result.getString("dob") ;
    				if (_result.getString("fb_id") != null && _result.getString("fb_id").length() > 0)
    					GlobalVariable.fb_id = _result.getString("fb_id") ;
    				if (_result.getString("gender") != null && _result.getString("gender").length() > 0)
    					GlobalVariable.tempGender = _result.getString("gender") ;
    				if (_result.getString("profile_pic") != null && _result.getString("profile_pic").length() > 0)
    					GlobalVariable.profile_photo_path = _result.getString("profile_pic") ;
    				
    				if (_result.getString("u_id") != null && _result.getString("u_id").length() > 0)
    					GlobalVariable.user_id = _result.getString("u_id") ;
    				
					SharedPreferences prefs = getSharedPreferences("user_info", Context.MODE_PRIVATE) ;
					SharedPreferences.Editor editor = prefs.edit();
		    		editor.putString("prev_password",txtPassword.getText().toString()) ;
		    		editor.putString("prev_email",txtEmail.getText().toString()) ;
		    		editor.commit();
		    		
					Intent myIntent = new Intent(LoginActivity.this, MainActivity.class);
					startActivity(myIntent);
				}
				else
				{
					AlertDialog alertDialog = new AlertDialog.Builder(this).create();
					alertDialog.setTitle(jsonObj.getString("type"));
					alertDialog.setMessage(jsonObj.getString("message"));
					alertDialog.setButton("OK", new DialogInterface.OnClickListener() {
					public void onClick(final DialogInterface dialog, final int which) {
						
						}
					});
					alertDialog.setIcon(R.drawable.ic_launcher);
					alertDialog.show();
					
				}
			
			} catch (JSONException e) {
				e.printStackTrace();
				
			}
		} else {
			Log.e("ServiceHandler", "Couldn't get any data from the server.") ;
			
		}
		
	}
}
