package au.com.sharonblain.longhairhow2;

import java.util.ArrayList;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
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
import android.app.ProgressDialog;
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
        
        final EditText txtEmail = (EditText)findViewById(R.id.txtEmail) ;
        final EditText txtPassword = (EditText)findViewById(R.id.txtPassword) ;
        
        Button btnLogin = (Button)findViewById(R.id.btnLogin) ;
        btnLogin.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				if ( GlobalVariable.f_valid == false )
				{
					Toast.makeText(LoginActivity.this, "Your access token is invalid now.", Toast.LENGTH_LONG).show() ;
				}
				else if ( txtEmail.getText().toString().length() < 1 )
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
		}) ;
        
    }
    
    @SuppressWarnings("unchecked")
	protected void login(String _email, String _password) {
    	
    	if ( !_dialog_progress.isShowing() )
    		_dialog_progress = ProgressDialog.show(this, "Connecting Server...", 
    				"Please wait a sec.", true);
    	
		ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("action", "/user/login"));
		params.add(new BasicNameValuePair("user_id", "-10"));
		params.add(new BasicNameValuePair("accessToken", GlobalVariable.accessToken));
		params.add(new BasicNameValuePair("email", _email));
		params.add(new BasicNameValuePair("pwd", _password));
		
		GlobalVariable.request_url = "http://longhairhow2.com/api/user/login" ;
		
		httpTask = new HttpPostTask() ;
		httpTask.delegate = LoginActivity.this ;
		httpTask.execute(params) ;
		
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
	public void processFinish(String output) {
		if ( _dialog_progress.isShowing() )
			_dialog_progress.dismiss() ;
		
		if (output.length() > 0) {
			try {
				JSONObject jsonObj = new JSONObject(output) ;
				if (jsonObj.get("type").equals("Success"))
				{
					JSONObject result = jsonObj.getJSONObject("results") ;
					GlobalVariable.f_name = result.getString("f_name") ;
					GlobalVariable.l_name = result.getString("l_name") ;
					GlobalVariable.email = result.getString("email") ;
					GlobalVariable.country = result.getString("country") ;
					
					Toast.makeText(LoginActivity.this, jsonObj.getString("type") + " - " + jsonObj.getString("message"), Toast.LENGTH_LONG).show() ;
				}
				else
				{
					Toast.makeText(LoginActivity.this, jsonObj.getString("type") + " - " + jsonObj.getString("message"), Toast.LENGTH_LONG).show() ;
				}
			
			} catch (JSONException e) {
				e.printStackTrace();
				
			}
		} else {
			Log.e("ServiceHandler", "Couldn't get any data from the url") ;
			
		}
		
	}
}
