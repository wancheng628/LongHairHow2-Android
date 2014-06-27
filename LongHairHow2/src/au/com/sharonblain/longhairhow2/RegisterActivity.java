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
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.MediaStore.Images.Media;
import android.provider.Settings.Global;
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
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
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
	
	private ImageView img_title ;
	
	private RelativeLayout layout_register2 ;
	private ImageView imgPhoto ;
	private Bitmap profile_photo ;
	private Uri capturedImageUri ;
	
	private RelativeLayout layout_register3 ;
	private ProgressDialog _dialog_progress ;
	
	private String birthday = "", country = "", gender = "" ;
	
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
        
        final TextView txtEmail = (TextView)findViewById(R.id.txtEmail) ;
        final TextView txtDesirePass = (TextView)findViewById(R.id.txtDesirePass) ;
        final TextView txtRepeatPass = (TextView)findViewById(R.id.txtRepeatPass) ;
        final TextView txtFirstName = (TextView)findViewById(R.id.txtFirstName) ;
        final TextView txtLastName = (TextView)findViewById(R.id.txtLastName) ;
        
        final ImageView btnPrevStep = (ImageView)findViewById(R.id.imgPreviousStep) ;
        
        layout_register1.setVisibility(View.VISIBLE) ;
        btnPrevStep.setVisibility(View.GONE) ;
        
        label_dob = (TextView)findViewById(R.id.label_dob) ;
        label_country = (TextView)findViewById(R.id.label_countries) ;
        label_gender = (TextView)findViewById(R.id.label_gender) ;
        img_title = (ImageView)findViewById(R.id.imgTitle) ;
        
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
			
			@SuppressWarnings("deprecation")
			@Override
			public void onClick(View arg0) {
				showDialog(0) ;
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
				else if ( label_dob.getText().toString().equals("YYYY/MM/DD") )
				{
					Toast.makeText(RegisterActivity.this, "Please select your birthday", Toast.LENGTH_LONG).show() ;
				}
				else if ( label_gender.getText().toString().equals("Male or Female") )
				{
					Toast.makeText(RegisterActivity.this, "Please select your gender", Toast.LENGTH_LONG).show() ;
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
        
        File file = new File(Environment.getExternalStorageDirectory(),  ("longHair_profile.jpg"));
	    if(file.exists()){
	    	try {
				profile_photo = Media.getBitmap(getContentResolver(), Uri.fromFile(file) );
				if ( profile_photo != null )
				{
					profile_photo = Bitmap.createScaledBitmap(profile_photo, 150, 200, true);
					imgPhoto.setImageBitmap(profile_photo);	
				}
	        		
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}   
	    }
	    
        final Button btnTakePhoto= (Button)layout_register2.findViewById(R.id.btnTakePhoto) ;
        final Button btnChoosePhotoFromLibrary= (Button)layout_register2.findViewById(R.id.btnChoosePhotoFromLibrary) ;
        
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
				File file = new File(Environment.getExternalStorageDirectory(),  ("longHair_profile.jpg"));
			    if(!file.exists()){
				    try {
				        file.createNewFile();
				    } catch (IOException e) {
				        e.printStackTrace();
				    }
				    }else{
				       file.delete();
				    try {
				       file.createNewFile();
				    } catch (IOException e) {
				        e.printStackTrace();
				    }
			    }
			    
			    capturedImageUri = Uri.fromFile(file);
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
			
			@SuppressWarnings("unchecked")
			@Override
			public void onClick(View arg0) {
				
				if ( !_dialog_progress.isShowing() )
		    		_dialog_progress = ProgressDialog.show(RegisterActivity.this, "Connecting Server...", 
		    				"Please wait a sec.", true);
		    	
				ArrayList<NameValuePair> params = new ArrayList<NameValuePair>() ;
				params.add(new BasicNameValuePair("action", "/user/register"));
				params.add(new BasicNameValuePair("user_id", "-10"));
				params.add(new BasicNameValuePair("accessToken", GlobalVariable.accessToken));
				params.add(new BasicNameValuePair("email", txtEmail.getText().toString()));
				params.add(new BasicNameValuePair("pwd", md5(txtDesirePass.getText().toString())));
				params.add(new BasicNameValuePair("f_name", txtFirstName.getText().toString()));
				params.add(new BasicNameValuePair("l_name", txtLastName.getText().toString()));
				params.add(new BasicNameValuePair("country", label_country.getText().toString()));
				params.add(new BasicNameValuePair("gender", gender));
				params.add(new BasicNameValuePair("dob", birthday));
				
				GlobalVariable.request_url = "http://longhairhow2.com/api/user/register" ;
				GlobalVariable.request_register = 1 ;
				GlobalVariable.photo = profile_photo ;
				httpTask = new HttpPostTask() ;
				httpTask.delegate = RegisterActivity.this ;
				httpTask.execute(params) ;
				
			}
		}) ;
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
	            	File file = new File(Environment.getExternalStorageDirectory(),  ("longHair_profile.jpg"));
	            	profile_photo = Media.getBitmap(getContentResolver(), Uri.fromFile(file) );
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
	            InputStream imageStream = null ;
				try {
					imageStream = getContentResolver().openInputStream(selectedImage);
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				}
	            
				profile_photo = BitmapFactory.decodeStream(imageStream);
				profile_photo = Bitmap.createScaledBitmap(profile_photo, 150, 200, true);
	            imgPhoto.setImageBitmap(profile_photo);
	        }
    	}
    }
        
    private void setTitleImage() {
    	String filePath = "@drawable/sbregistersteptitle_" + String.valueOf(nStep) ;
		Log.d("File Path", filePath) ;
		Resources res = RegisterActivity.this.getResources() ;
		int imgId = res.getIdentifier(filePath, "drawable", RegisterActivity.this.getPackageName()) ;
        img_title.setImageResource(imgId) ;
    }
    
    @Override
    @Deprecated
    protected Dialog onCreateDialog(int id) {
    	return new DatePickerDialog(this, datePickerListener, year, month, day);
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

	@Override
	public void processFinish(String output) {
		if ( _dialog_progress.isShowing() )
			_dialog_progress.dismiss() ;
		
		if (output.length() > 0) {
			try {
				JSONObject jsonObj = new JSONObject(output) ;
				if (jsonObj.get("type").equals("Success"))
				{
					Toast.makeText(RegisterActivity.this, jsonObj.getString("type") + " - " + jsonObj.getString("message"), Toast.LENGTH_LONG).show() ;
				}
				else
				{
					Toast.makeText(RegisterActivity.this, jsonObj.getString("type") + " - " + jsonObj.getString("message"), Toast.LENGTH_LONG).show() ;
				}
			
			} catch (JSONException e) {
				e.printStackTrace();
				
			}
		} else {
			Toast.makeText(RegisterActivity.this, "Couldn't get any data from the url", Toast.LENGTH_LONG).show() ;			
		}		
		
		GlobalVariable.tempBirthday = "" ;
		GlobalVariable.tempCountry = "" ;
		GlobalVariable.tempGender = "" ;
		finish() ;
	}
}
