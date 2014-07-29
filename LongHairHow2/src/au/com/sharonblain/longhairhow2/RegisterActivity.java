package au.com.sharonblain.longhairhow2;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.MediaStore.Images.Media;
import android.text.Html;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import au.com.sharonblain.request_server.AsyncResponse;
import au.com.sharonblain.request_server.GlobalVariable;
import au.com.sharonblain.request_server.HttpPostTask;

public class RegisterActivity extends Activity implements AsyncResponse{

	private HelperUtils utils;
    private ArrayList<String> imagePaths = new ArrayList<String>();
    private GridViewImageAdapter adapter;
    private GridView gridView;
    private int columnWidth;
    
    private HttpPostTask httpTask = new HttpPostTask() ;
	private int nStep ;
	
	private RelativeLayout layout_register1 ;
	private Calendar cal;
	private int day;
	private int month;
	private int year;
	 
	private TextView label_dob ;
	private TextView label_country ;
	private TextView label_gender ;
	private TextView label_next ;
	private ImageView img_title ;
	
	private RelativeLayout layout_register2 ;
	private ImageView imgPhoto ;
	private Bitmap profile_photo ;
	private Uri capturedImageUri ;
	
	private RelativeLayout layout_register3 ;
	private ProgressDialog _dialog_progress ;
	
	private String birthday = "", country = "", gender = "" ;
	private File photo_file ;
	private int nRequestKind = 1 ;
	
	TextView txtEmail ;
    TextView txtDesirePass ;
    TextView txtRepeatPass ;
    TextView txtFirstName ;
    TextView txtLastName ;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
 
        nStep = 1 ;
        
        cal = Calendar.getInstance();
        day = cal.get(Calendar.DAY_OF_MONTH);
        month = cal.get(Calendar.MONTH);
        year = cal.get(Calendar.YEAR);
        
        httpTask.delegate = RegisterActivity.this ;
        gridView = (GridView) findViewById(R.id.grid_view);
        utils = new HelperUtils(this);
        InitilizeGridLayout();

        _dialog_progress = new ProgressDialog(RegisterActivity.this) ;
        
        imagePaths = utils.getFilePaths();
        adapter = new GridViewImageAdapter(RegisterActivity.this, imagePaths, columnWidth);
        gridView.setAdapter(adapter);
        
        layout_register1 = (RelativeLayout)findViewById(R.id.layout_register_1) ;
        final RelativeLayout layout_prevstep = (RelativeLayout)findViewById(R.id.layout_prevstep) ;
        final LinearLayout layout_dob = (LinearLayout)findViewById(R.id.layout_dob) ;
        final LinearLayout layout_country = (LinearLayout)findViewById(R.id.layout_country) ;
        final LinearLayout layout_gender = (LinearLayout)findViewById(R.id.layout_gender) ;
        final LinearLayout layout_next = (LinearLayout)findViewById(R.id.layout_nextbutton) ;
        
        txtEmail = (TextView)findViewById(R.id.txtEmail) ;
        txtDesirePass = (TextView)findViewById(R.id.txtDesirePass) ;
        txtRepeatPass = (TextView)findViewById(R.id.txtRepeatPass) ;
        txtFirstName = (TextView)findViewById(R.id.txtFirstName) ;
        txtLastName = (TextView)findViewById(R.id.txtLastName) ;
        
        txtEmail.setTypeface(GlobalVariable.tf_medium) ;
        txtDesirePass.setTypeface(GlobalVariable.tf_medium) ;
        txtRepeatPass.setTypeface(GlobalVariable.tf_medium) ;
        txtFirstName.setTypeface(GlobalVariable.tf_medium) ;
        txtLastName.setTypeface(GlobalVariable.tf_medium) ;
        
        final ImageView btnPrevStep = (ImageView)findViewById(R.id.imgPreviousStep) ;
        
        layout_register1.setVisibility(View.VISIBLE) ;
        btnPrevStep.setVisibility(View.GONE) ;
        
        label_dob = (TextView)findViewById(R.id.label_dob) ;
        label_country = (TextView)findViewById(R.id.label_countries) ;
        label_gender = (TextView)findViewById(R.id.label_gender) ;
        img_title = (ImageView)findViewById(R.id.imgTitle) ;
        label_next = (TextView)findViewById(R.id.label_next) ;
        
        label_dob.setTypeface(GlobalVariable.tf_medium) ;
        label_country.setTypeface(GlobalVariable.tf_medium) ;
        label_gender.setTypeface(GlobalVariable.tf_medium) ;
        label_next.setTypeface(GlobalVariable.tf_medium) ;
        
        TextView label_final_step = (TextView)findViewById(R.id.label_final_step) ;
        label_final_step.setTypeface(GlobalVariable.tf_medium) ;
        layout_prevstep.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				nStep = nStep - 1 ;
				if ( nStep == 1 )
				{
					layout_prevstep.setVisibility(View.GONE) ;
					layout_register2.setVisibility(View.GONE) ;
					layout_register1.setVisibility(View.VISIBLE) ;
					
					int resourceId = getResources().getIdentifier( "@drawable/sbregistersteptitle_1", "drawable", getPackageName() );
					img_title.setImageResource(resourceId) ;
				}
				else if ( nStep == 2 )
				{
					layout_register3.setVisibility(View.GONE) ;
					layout_register2.setVisibility(View.VISIBLE) ;
					
					int resourceId = getResources().getIdentifier( "@drawable/sbregistersteptitle_2", "drawable", getPackageName() );
					img_title.setImageResource(resourceId) ;
				}
			}
		}) ;
        
        layout_dob.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				DatePickerDialog dlgDate = new DatePickerDialog(RegisterActivity.this, datePickerListener, year, month, day);
				dlgDate.show() ;
			}
		}) ;
        
        
        if ( GlobalVariable.tempBirthday.length() > 1 )
        	label_dob.setText(GlobalVariable.tempBirthday) ;
        if ( GlobalVariable.tempCountry.length() > 1 )
        	label_country.setText(GlobalVariable.tempCountry) ;
        
        if (GlobalVariable.tempGender.equals("M"))
        	label_gender.setText("Male") ;
        else if ( GlobalVariable.tempGender.equals("F") )
        	label_gender.setText("Female") ;
        else
        	label_gender.setText("Male or Female") ;
        
        layout_country.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				Locale[] locales = Locale.getAvailableLocales();
		        final ArrayList<String> countries = new ArrayList<String>();
		        for (Locale locale : locales) {
		            String country = locale.getDisplayCountry();
		            GlobalVariable.tempCountry = country ;
		            if (country.trim().length()>0 && !countries.contains(country)) {
		                countries.add(country);
		            }
		        }
		        Collections.sort(countries);
		        
		        CharSequence[] items = countries.toArray(new CharSequence[countries.size()]);
		        AlertDialog.Builder builder = new AlertDialog.Builder(RegisterActivity.this);
		        builder.setTitle("Select your country.");
		        builder.setItems(items, new DialogInterface.OnClickListener() {
		            public void onClick(DialogInterface dialog, int item) {
		                country = countries.get(item).toString() ;
		                label_country.setText(country) ;
		            }
		        });
		        AlertDialog alert = builder.create();
		        alert.show();
			}
		}) ;
        
        layout_gender.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				final CharSequence[] items = {"Male", "Female"} ;
				AlertDialog.Builder builder = new AlertDialog.Builder(RegisterActivity.this);
		        builder.setTitle("Set your gender.");
		        builder.setItems(items, new DialogInterface.OnClickListener() {
		            public void onClick(DialogInterface dialog, int item) {
		            	if ( item == 0 )
		            	{
		            		gender = "M" ;
		            		label_gender.setText("Male") ;
		            		GlobalVariable.tempGender = gender ;
		            	}
		            	else
		            	{
		            		gender = "F" ;
		            		label_gender.setText("Female") ;
		            		GlobalVariable.tempGender = gender ;
		            	}
		            	
		            	
		            }
		        });
		        AlertDialog alert = builder.create();
		        alert.show();
			}
		}) ;
        
        layout_next.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				
				if ( txtEmail.getText().toString().length() < 1 )
				{
					Toast.makeText(RegisterActivity.this, "Please input the email address", Toast.LENGTH_LONG).show() ;
				}
				else if ( !isEmailValid(txtEmail.getText().toString()) )
				{
					Toast.makeText(RegisterActivity.this, "Invalid password", Toast.LENGTH_LONG).show() ;
				}
				else if ( txtDesirePass.getText().toString().length() < 6 )
				{
					Toast.makeText(RegisterActivity.this, "password length must be more than 6 letters", Toast.LENGTH_LONG).show() ;
				}
				else if ( txtRepeatPass.getText().toString().length() < 6 )
				{
					Toast.makeText(RegisterActivity.this, "password length must be more than 6 letters", Toast.LENGTH_LONG).show() ;
				}
				else if ( !txtDesirePass.getText().toString().equals(txtRepeatPass.getText().toString()) )
				{
					Toast.makeText(RegisterActivity.this, "Not same password", Toast.LENGTH_LONG).show() ;
				}
				else if ( txtFirstName.getText().toString().length() < 1 )
				{
					Toast.makeText(RegisterActivity.this, "Please input the first name", Toast.LENGTH_LONG).show() ;
				}
				else
				{
					nStep = 2 ;
					setTitleImage() ;
					layout_prevstep.setVisibility(View.VISIBLE) ;
					layout_register1.setVisibility(View.GONE) ;
					layout_register2.setVisibility(View.VISIBLE) ;
					
					btnPrevStep.setVisibility(View.VISIBLE) ;
					Resources res = getResources();
					int resourceId = res.getIdentifier( "@drawable/step1backbutton", "drawable", getPackageName() );
					btnPrevStep.setImageResource( resourceId );	
				}			
			}
		}) ;
        
        layout_register2 = (RelativeLayout)findViewById(R.id.layout_register_2) ;
        final LinearLayout layout_accept = (LinearLayout)layout_register2.findViewById(R.id.layout_accept) ;
        imgPhoto= (ImageView)layout_register2.findViewById(R.id.imgPhoto) ;
        
        photo_file = new File(Environment.getExternalStorageDirectory(),  ("longHair_profile.jpg"));
	    if(photo_file.exists()){
	    	try {
				profile_photo = Media.getBitmap(getContentResolver(), Uri.fromFile(photo_file) );
				if ( profile_photo != null )
				{
					profile_photo = Bitmap.createScaledBitmap(profile_photo, 150, 200, true);
					imgPhoto.setImageBitmap(profile_photo);	
				}
	        		
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}   
	    }
	    
        final Button btnTakePhoto= (Button)layout_register2.findViewById(R.id.btnTakePhoto) ;
        final Button btnChoosePhotoFromLibrary= (Button)layout_register2.findViewById(R.id.btnChoosePhotoFromLibrary) ;
        
        btnTakePhoto.setTypeface(GlobalVariable.tf_medium) ;
        btnChoosePhotoFromLibrary.setTypeface(GlobalVariable.tf_medium) ;
        
        layout_accept.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				nStep = 3 ;
				setTitleImage() ;
				layout_register2.setVisibility(View.GONE) ;
				layout_register3.setVisibility(View.VISIBLE) ;
				
				Resources res = getResources();
				int resourceId = res.getIdentifier( "@drawable/step2backbutton", "drawable", getPackageName() );
				btnPrevStep.setImageResource( resourceId );				
			}
		}) ;
        
        btnTakePhoto.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				photo_file = new File(Environment.getExternalStorageDirectory(),  ("longHair_profile.jpg"));
			    if(!photo_file.exists()){
				    try {
				    	photo_file.createNewFile();
				    } catch (IOException e) {
				        e.printStackTrace();
				    }
				    }else{
				    	photo_file.delete();
				    try {
				    	photo_file.createNewFile();
				    } catch (IOException e) {
				        e.printStackTrace();
				    }
			    }
			    
			    capturedImageUri = Uri.fromFile(photo_file);
			    Log.d("Photo URI", capturedImageUri.getPath()) ;
			    Intent i = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
			    i.putExtra(MediaStore.EXTRA_OUTPUT, capturedImageUri);
			    startActivityForResult(i, 111);
			}
		}) ;
        
        btnChoosePhotoFromLibrary.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
				photoPickerIntent.setType("image/*");
				startActivityForResult(photoPickerIntent, 112);  
			}
		}) ;
        
        layout_register3 = (RelativeLayout)findViewById(R.id.layout_register_3) ;
        final TextView label_policy = (TextView)layout_register3.findViewById(R.id.label_policy) ;
        label_policy.setTypeface(GlobalVariable.tf_light) ;
        label_policy.setMovementMethod(new ScrollingMovementMethod());
        
        InputStream inputStream = getResources().openRawResource(R.drawable.privacy_policy);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        int i;
        try {
        	i = inputStream.read();
        	while (i != -1)
        	{
    			byteArrayOutputStream.write(i);
    			i = inputStream.read();
        	}
        	inputStream.close();
         	} catch (IOException e) {
         		e.printStackTrace();
         	}
        
        label_policy.setText(Html.fromHtml(byteArrayOutputStream.toString())) ;
       	
        final LinearLayout layout_register = (LinearLayout)layout_register3.findViewById(R.id.layout_accept) ;
        layout_register.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				
				if ( _dialog_progress == null || !_dialog_progress.isShowing() )
				{
					_dialog_progress = ProgressDialog.show(RegisterActivity.this, "Loading...", 
			    				"Please wait...", true);					
				}
				
				if ( GlobalVariable.accessToken == null || GlobalVariable.accessToken.length() < 1 )
				{
					getAccessToken() ;					
				}
				else
				{
					Register() ;
				}
			}
		}) ;
    }
    
    private void Register() {
    	
    	MultipartEntityBuilder builder = MultipartEntityBuilder.create() ;
		builder.addTextBody("action", "/user/register", ContentType.TEXT_PLAIN) ;
		builder.addTextBody("user_id", "-10", ContentType.TEXT_PLAIN);
		
    	builder.addTextBody("accessToken", GlobalVariable.accessToken, ContentType.TEXT_PLAIN);
		builder.addTextBody("email", txtEmail.getText().toString(), ContentType.TEXT_PLAIN);
		builder.addTextBody("pwd", md5(txtDesirePass.getText().toString()), ContentType.TEXT_PLAIN);
		builder.addTextBody("f_name", txtFirstName.getText().toString(), ContentType.TEXT_PLAIN);
		builder.addTextBody("l_name", txtLastName.getText().toString(), ContentType.TEXT_PLAIN);
		builder.addTextBody("country", label_country.getText().toString(), ContentType.TEXT_PLAIN);
		builder.addTextBody("gender", gender, ContentType.TEXT_PLAIN);
		builder.addTextBody("dob", birthday, ContentType.TEXT_PLAIN);
		
		//photo_file = new File(Environment.getExternalStorageDirectory(), "longHair_profile.jpg");
		if ( photo_file != null && photo_file.exists() )
			builder.addPart("profile_pic", new FileBody(photo_file)) ;
						
		GlobalVariable.request_url = GlobalVariable.API_URL + "/user/register" ;
		GlobalVariable.request_register = 1 ;
		
		httpTask = new HttpPostTask() ;
		httpTask.delegate = RegisterActivity.this ;
		httpTask.execute(builder) ;
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
    		_dialog_progress = ProgressDialog.show(this, "Loading...", "Please wait...", true);    		
    	}
    	
    	GlobalVariable.getSydneyTime() ;
    	
    	MultipartEntityBuilder builder = MultipartEntityBuilder.create() ;
    	builder.addTextBody("action", "/common/access-token/grant", ContentType.TEXT_PLAIN);
    	builder.addTextBody("user_id", GlobalVariable.user_id, ContentType.TEXT_PLAIN);
		
		GlobalVariable.request_url = GlobalVariable.API_URL + "/common/access-token/grant" ;
		
		httpTask = new HttpPostTask() ;
		httpTask.delegate = this;
		httpTask.execute(builder) ;
    }
	
    public static final String md5(final String s) {
        final String MD5 = "MD5";
        try {
            // Create MD5 Hash
            MessageDigest digest = java.security.MessageDigest
                    .getInstance(MD5);
            digest.update(s.getBytes());
            byte messageDigest[] = digest.digest();

            // Create Hex String
            StringBuilder hexString = new StringBuilder();
            for (byte aMessageDigest : messageDigest) {
                String h = Integer.toHexString(0xFF & aMessageDigest);
                while (h.length() < 2)
                    h = "0" + h;
                hexString.append(h);
            }
            return hexString.toString();

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return "";
    }
    
    public static boolean isEmailValid(String email) {
        boolean isValid = false;

        String expression = "^[\\w\\.-]+@([\\w\\-]+\\.)+[A-Z]{2,4}$";
        CharSequence inputStr = email;

        Pattern pattern = Pattern.compile(expression, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(inputStr);
        if (matcher.matches()) {
            isValid = true;
        }
        return isValid;
    }
    
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {  
    	if (resultCode == RESULT_OK) {
	        if (requestCode == 111) {  
	            try {
	            	photo_file = new File(Environment.getExternalStorageDirectory(), "longHair_profile.jpg");
	            	profile_photo = Media.getBitmap(getContentResolver(), Uri.fromFile(photo_file) );
	            	profile_photo = Bitmap.createScaledBitmap(profile_photo, 150, 200, true);
	            	imgPhoto.setImageBitmap(profile_photo);
			        
			        } catch (FileNotFoundException e) {
			        	e.printStackTrace();
			        } catch (IOException e) {
			        	e.printStackTrace();
			    }
	        }  
	        if (requestCode == 112)
	        {
	        	Uri selectedImage = data.getData();
	        	photo_file = new File(getRealPathFromURI(selectedImage));
	            InputStream imageStream = null ;
				try {
					imageStream = getContentResolver().openInputStream(selectedImage);
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				}
	            
				if ( imageStream != null )
				{
					profile_photo = BitmapFactory.decodeStream(imageStream);
					profile_photo = Bitmap.createScaledBitmap(profile_photo, 150, 200, true);
		            imgPhoto.setImageBitmap(profile_photo);
				}				
	        }
    	}
    }
    
    private String getRealPathFromURI(Uri contentURI) {
        String result;
        Cursor cursor = getContentResolver().query(contentURI, null, null, null, null);
        if (cursor == null) { // Source is Dropbox or other similar local file path
            result = contentURI.getPath();
        } else { 
            cursor.moveToFirst(); 
            int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA); 
            result = cursor.getString(idx);
            cursor.close();
        }
        return result;
    }
        
    private void setTitleImage() {
    	String filePath = "@drawable/sbregistersteptitle_" + String.valueOf(nStep) ;
		Log.d("File Path", filePath) ;
		Resources res = RegisterActivity.this.getResources() ;
		int imgId = res.getIdentifier(filePath, "drawable", RegisterActivity.this.getPackageName()) ;
        img_title.setImageResource(imgId) ;
    }
    
    private DatePickerDialog.OnDateSetListener datePickerListener = new DatePickerDialog.OnDateSetListener() {
    	public void onDateSet(DatePicker view, int selectedYear, int selectedMonth, int selectedDay) {
    		birthday = String.format("%04d/%02d/%02d", selectedYear, (selectedMonth + 1), selectedDay ) ;
    		GlobalVariable.tempBirthday = birthday ;
    		label_dob.setText(birthday) ;
    	}
    };
    
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
	
	protected void login(String _email, String _password) {
    	
		nRequestKind = 3 ;
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
		httpTask.delegate = RegisterActivity.this ;
		httpTask.execute(builder) ;
		
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
		
		if ( nRequestKind == 1 )
		{
			if (output.length() > 0) {
				try {
					JSONObject jsonObj = new JSONObject(output) ;
					if (jsonObj.get("type").equals("Success"))
					{
						//Register() ;
						
						
						AlertDialog alertDialog = new AlertDialog.Builder(this).create();
						alertDialog.setTitle(jsonObj.getString("type"));
						alertDialog.setMessage(jsonObj.getString("message"));
						alertDialog.setButton("OK", new DialogInterface.OnClickListener() {
						public void onClick(final DialogInterface dialog, final int which) {
							login(txtEmail.getText().toString(), txtDesirePass.getText().toString()) ;
							
							}
						});
						alertDialog.setIcon(R.drawable.ic_launcher);
						alertDialog.show();
						
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
				
				AlertDialog alertDialog = new AlertDialog.Builder(this).create();
				alertDialog.setTitle("Error");
				alertDialog.setMessage("Couldn't get any data from the server.");
				alertDialog.setButton("OK", new DialogInterface.OnClickListener() {
				public void onClick(final DialogInterface dialog, final int which) {
					getAccessToken() ;
					}
				});
				alertDialog.setIcon(R.drawable.ic_launcher);
				alertDialog.show();
			}		
			
			GlobalVariable.tempBirthday = "" ;
			GlobalVariable.tempCountry = "" ;
			GlobalVariable.tempGender = "" ;
			
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
		else if ( nRequestKind == 3 )
		{
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

						SharedPreferences prefs = getSharedPreferences("user_info", Context.MODE_PRIVATE) ;
						SharedPreferences.Editor editor = prefs.edit();
			    		editor.putString("prev_password",txtDesirePass.getText().toString()) ;
			    		editor.putString("prev_email",txtEmail.getText().toString()) ;
			    		editor.commit();
			    		
						Intent myIntent = new Intent(RegisterActivity.this, MainActivity.class);
						startActivity(myIntent);
						finish() ;
					}
					else
					{
						Toast.makeText(RegisterActivity.this, jsonObj.getString("type") + " - " + jsonObj.getString("message"), Toast.LENGTH_LONG).show() ;
					}
				
				} catch (JSONException e) {
					e.printStackTrace();
					
				}
			} else {
				Log.e("ServiceHandler", "Couldn't get any data from the server.") ;
				
			}
		}
	}
}
