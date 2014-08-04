package au.com.sharonblain.longhairhow2;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.assist.ImageSize;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Bitmap;

import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Parcelable;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.ConsoleMessage;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings.PluginState;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import au.com.sharonblain.request_server.GlobalVariable;

@SuppressLint("SetJavaScriptEnabled")
public class ProfileActivity extends Activity {
	private DisplayImageOptions options;
	final Activity activity = this;
    public Uri imageUri;
     
    private static final int FILECHOOSER_RESULTCODE   = 2888;
    private ValueCallback<Uri> mUploadMessage;
    private Uri mCapturedImageURI = null;
    private WebView webview ;
    
    @Override
    public void onBackPressed() {
        Intent intent = new Intent(ProfileActivity.this, MainActivity.class) ;
        startActivity(intent) ;
        finish() ;
    }
    
	private void setProfilePhoto()
	{
		TextView mTitleTextView = (TextView)findViewById(R.id.label_type);
        mTitleTextView.setText(GlobalVariable.l_name) ;
        mTitleTextView.setTypeface(GlobalVariable.tf_light) ;
 
        ImageView profile_photo = (ImageView)findViewById(R.id.img_profile_photo);
        
        if ( GlobalVariable.profile_photo_path != null && GlobalVariable.profile_photo_path.length() > 1 && !GlobalVariable.profile_photo_path.equals("/user/images/") )
        {
        	options = new DisplayImageOptions.Builder()
    		.showImageForEmptyUri(R.drawable.default_user_icon)
    		.showImageOnFail(R.drawable.default_user_icon)
    		.resetViewBeforeLoading(true)
    		.cacheOnDisk(true)
    		.imageScaleType(ImageScaleType.EXACTLY)
    		.bitmapConfig(Bitmap.Config.RGB_565)
    		.considerExifParams(true)
    		.displayer(new FadeInBitmapDisplayer(300))
    		.build();
        	
        	ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(getApplicationContext())
        	.defaultDisplayImageOptions(options)
        	.build();
        	ImageLoader.getInstance().init(config);
        	ImageSize targetSize = new ImageSize(160, 160);
        	Bitmap bmp = ImageLoader.getInstance().loadImageSync(GlobalVariable.API_URL + GlobalVariable.profile_photo_path, targetSize, options);
        	profile_photo.setImageBitmap(GlobalVariable.getCircularBitmap(bmp)) ;
        	try {
        		ImageLoader.getInstance().destroy() ;
        	} catch (NullPointerException e) {
        		e.printStackTrace() ;
        	}
        	
        }         
	}
	
	@SuppressLint("JavascriptInterface")
	@SuppressWarnings("deprecation")
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        
        if (android.os.Build.VERSION.SDK_INT > 9) {
    	    StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
    	    StrictMode.setThreadPolicy(policy);
    	}
        
        setProfilePhoto() ;
        webview = (WebView)findViewById(R.id.webView1) ;
        webview.clearFormData() ;
        String htmlString = getAssetsContent() ;
        
        htmlString = htmlString.replace("INSERT-PROFILE-PIC-LOCATION", GlobalVariable.API_URL + GlobalVariable.profile_photo_path) ;
        
        if ( GlobalVariable.user_id != null )
        	htmlString = htmlString.replace("INSERT-USER-ID", GlobalVariable.user_id) ;
        if ( GlobalVariable.accessToken != null )
        	htmlString = htmlString.replace("INSERT-ACCESS-TOKEN", GlobalVariable.accessToken) ;
        
        if ( GlobalVariable.f_name != null )
        	htmlString = htmlString.replace("INSERT-USER-FNAME", GlobalVariable.f_name) ;
        
        if ( GlobalVariable.dob != null )
        	htmlString = htmlString.replace("INSERT-USER-DOB", GlobalVariable.dob) ;
        
        if ( GlobalVariable.l_name != null )
        	htmlString = htmlString.replace("INSERT-USER-LNAME", GlobalVariable.l_name) ;
        
        if ( GlobalVariable.email != null )
        	htmlString = htmlString.replace("INSERT-USER-EMAIL", GlobalVariable.email) ;
        
        htmlString = htmlString.replace("INSERT-FORM-ACTION-HERE", GlobalVariable.API_URL + "/user/set.php") ;
        String _temp = "<select name='gender'>" ;
        
        if ( GlobalVariable.tempGender.equals("M") )
        {
        	_temp = _temp.concat("<option value='M' selected>Male</option>") ;
        	_temp = _temp.concat("<option value='F'>Female</option>") ;
        }
        else
        {
        	_temp = _temp.concat("<option value='M'>Male</option>") ;
        	_temp = _temp.concat("<option value='F' selected>Female</option>") ;
        }
        
        _temp = _temp.concat("</select>") ;
        
        htmlString = htmlString.replace("INSERT-GENDER-SELECT-HERE", _temp) ;
        
        _temp = "<select name='country'>" ;
        _temp = _temp.concat(String.format("<option value='%s'>%s</option>", GlobalVariable.country, GlobalVariable.country)) ;
        JSONArray arr_country = new JSONArray() ;
        
        try {
        	arr_country = new JSONArray("[{'name':'Afghanistan','code':'AF'},{'name':'AlandIslands','code':'AX'},{'name':'Albania','code':'AL'},{'name':'Algeria','code':'DZ'},{'name':'AmericanSamoa','code':'AS'},{'name':'AndorrA','code':'AD'},{'name':'Angola','code':'AO'},{'name':'Anguilla','code':'AI'},{'name':'Antarctica','code':'AQ'},{'name':'AntiguaandBarbuda','code':'AG'},{'name':'Argentina','code':'AR'},{'name':'Armenia','code':'AM'},{'name':'Aruba','code':'AW'},{'name':'Australia','code':'AU'},{'name':'Austria','code':'AT'},{'name':'Azerbaijan','code':'AZ'},{'name':'Bahamas','code':'BS'},{'name':'Bahrain','code':'BH'},{'name':'Bangladesh','code':'BD'},{'name':'Barbados','code':'BB'},{'name':'Belarus','code':'BY'},{'name':'Belgium','code':'BE'},{'name':'Belize','code':'BZ'},{'name':'Benin','code':'BJ'},{'name':'Bermuda','code':'BM'},{'name':'Bhutan','code':'BT'},{'name':'Bolivia','code':'BO'},{'name':'BosniaandHerzegovina','code':'BA'},{'name':'Botswana','code':'BW'},{'name':'BouvetIsland','code':'BV'},{'name':'Brazil','code':'BR'},{'name':'BritishIndianOceanTerritory','code':'IO'},{'name':'BruneiDarussalam','code':'BN'},{'name':'Bulgaria','code':'BG'},{'name':'BurkinaFaso','code':'BF'},{'name':'Burundi','code':'BI'},{'name':'Cambodia','code':'KH'},{'name':'Cameroon','code':'CM'},{'name':'Canada','code':'CA'},{'name':'CapeVerde','code':'CV'},{'name':'CaymanIslands','code':'KY'},{'name':'CentralAfricanRepublic','code':'CF'},{'name':'Chad','code':'TD'},{'name':'Chile','code':'CL'},{'name':'China','code':'CN'},{'name':'ChristmasIsland','code':'CX'},{'name':'Cocos(Keeling)Islands','code':'CC'},{'name':'Colombia','code':'CO'},{'name':'Comoros','code':'KM'},{'name':'Congo','code':'CG'},{'name':'Congo,TheDemocraticRepublicofthe','code':'CD'},{'name':'CookIslands','code':'CK'},{'name':'CostaRica','code':'CR'},{'name':'CoteDIvoire','code':'CI'},{'name':'Croatia','code':'HR'},{'name':'Cuba','code':'CU'},{'name':'Cyprus','code':'CY'},{'name':'CzechRepublic','code':'CZ'},{'name':'Denmark','code':'DK'},{'name':'Djibouti','code':'DJ'},{'name':'Dominica','code':'DM'},{'name':'DominicanRepublic','code':'DO'},{'name':'Ecuador','code':'EC'},{'name':'Egypt','code':'EG'},{'name':'ElSalvador','code':'SV'},{'name':'EquatorialGuinea','code':'GQ'},{'name':'Eritrea','code':'ER'},{'name':'Estonia','code':'EE'},{'name':'Ethiopia','code':'ET'},{'name':'FalklandIslands(Malvinas)','code':'FK'},{'name':'FaroeIslands','code':'FO'},{'name':'Fiji','code':'FJ'},{'name':'Finland','code':'FI'},{'name':'France','code':'FR'},{'name':'FrenchGuiana','code':'GF'},{'name':'FrenchPolynesia','code':'PF'},{'name':'FrenchSouthernTerritories','code':'TF'},{'name':'Gabon','code':'GA'},{'name':'Gambia','code':'GM'},{'name':'Georgia','code':'GE'},{'name':'Germany','code':'DE'},{'name':'Ghana','code':'GH'},{'name':'Gibraltar','code':'GI'},{'name':'Greece','code':'GR'},{'name':'Greenland','code':'GL'},{'name':'Grenada','code':'GD'},{'name':'Guadeloupe','code':'GP'},{'name':'Guam','code':'GU'},{'name':'Guatemala','code':'GT'},{'name':'Guernsey','code':'GG'},{'name':'Guinea','code':'GN'},{'name':'Guinea-Bissau','code':'GW'},{'name':'Guyana','code':'GY'},{'name':'Haiti','code':'HT'},{'name':'HeardIslandandMcdonaldIslands','code':'HM'},{'name':'HolySee(VaticanCityState)','code':'VA'},{'name':'Honduras','code':'HN'},{'name':'HongKong','code':'HK'},{'name':'Hungary','code':'HU'},{'name':'Iceland','code':'IS'},{'name':'India','code':'IN'},{'name':'Indonesia','code':'ID'},{'name':'Iran,IslamicRepublicOf','code':'IR'},{'name':'Iraq','code':'IQ'},{'name':'Ireland','code':'IE'},{'name':'IsleofMan','code':'IM'},{'name':'Israel','code':'IL'},{'name':'Italy','code':'IT'},{'name':'Jamaica','code':'JM'},{'name':'Japan','code':'JP'},{'name':'Jersey','code':'JE'},{'name':'Jordan','code':'JO'},{'name':'Kazakhstan','code':'KZ'},{'name':'Kenya','code':'KE'},{'name':'Kiribati','code':'KI'},{'name':'Korea,DemocraticPeopleSRepublicof','code':'KP'},{'name':'Korea,Republicof','code':'KR'},{'name':'Kuwait','code':'KW'},{'name':'Kyrgyzstan','code':'KG'},{'name':'LaoPeopleSDemocraticRepublic','code':'LA'},{'name':'Latvia','code':'LV'},{'name':'Lebanon','code':'LB'},{'name':'Lesotho','code':'LS'},{'name':'Liberia','code':'LR'},{'name':'LibyanArabJamahiriya','code':'LY'},{'name':'Liechtenstein','code':'LI'},{'name':'Lithuania','code':'LT'},{'name':'Luxembourg','code':'LU'},{'name':'Macao','code':'MO'},{'name':'Macedonia,TheFormerYugoslavRepublicof','code':'MK'},{'name':'Madagascar','code':'MG'},{'name':'Malawi','code':'MW'},{'name':'Malaysia','code':'MY'},{'name':'Maldives','code':'MV'},{'name':'Mali','code':'ML'},{'name':'Malta','code':'MT'},{'name':'MarshallIslands','code':'MH'},{'name':'Martinique','code':'MQ'},{'name':'Mauritania','code':'MR'},{'name':'Mauritius','code':'MU'},{'name':'Mayotte','code':'YT'},{'name':'Mexico','code':'MX'},{'name':'Micronesia,FederatedStatesof','code':'FM'},{'name':'Moldova,Republicof','code':'MD'},{'name':'Monaco','code':'MC'},{'name':'Mongolia','code':'MN'},{'name':'Montserrat','code':'MS'},{'name':'Morocco','code':'MA'},{'name':'Mozambique','code':'MZ'},{'name':'Myanmar','code':'MM'},{'name':'Namibia','code':'NA'},{'name':'Nauru','code':'NR'},{'name':'Nepal','code':'NP'},{'name':'Netherlands','code':'NL'},{'name':'NetherlandsAntilles','code':'AN'},{'name':'NewCaledonia','code':'NC'},{'name':'NewZealand','code':'NZ'},{'name':'Nicaragua','code':'NI'},{'name':'Niger','code':'NE'},{'name':'Nigeria','code':'NG'},{'name':'Niue','code':'NU'},{'name':'NorfolkIsland','code':'NF'},{'name':'NorthernMarianaIslands','code':'MP'},{'name':'Norway','code':'NO'},{'name':'Oman','code':'OM'},{'name':'Pakistan','code':'PK'},{'name':'Palau','code':'PW'},{'name':'PalestinianTerritory,Occupied','code':'PS'},{'name':'Panama','code':'PA'},{'name':'PapuaNewGuinea','code':'PG'},{'name':'Paraguay','code':'PY'},{'name':'Peru','code':'PE'},{'name':'Philippines','code':'PH'},{'name':'Pitcairn','code':'PN'},{'name':'Poland','code':'PL'},{'name':'Portugal','code':'PT'},{'name':'PuertoRico','code':'PR'},{'name':'Qatar','code':'QA'},{'name':'Reunion','code':'RE'},{'name':'Romania','code':'RO'},{'name':'RussianFederation','code':'RU'},{'name':'RWANDA','code':'RW'},{'name':'SaintHelena','code':'SH'},{'name':'SaintKittsandNevis','code':'KN'},{'name':'SaintLucia','code':'LC'},{'name':'SaintPierreandMiquelon','code':'PM'},{'name':'SaintVincentandtheGrenadines','code':'VC'},{'name':'Samoa','code':'WS'},{'name':'SanMarino','code':'SM'},{'name':'SaoTomeandPrincipe','code':'ST'},{'name':'SaudiArabia','code':'SA'},{'name':'Senegal','code':'SN'},{'name':'SerbiaandMontenegro','code':'CS'},{'name':'Seychelles','code':'SC'},{'name':'SierraLeone','code':'SL'},{'name':'Singapore','code':'SG'},{'name':'Slovakia','code':'SK'},{'name':'Slovenia','code':'SI'},{'name':'SolomonIslands','code':'SB'},{'name':'Somalia','code':'SO'},{'name':'SouthAfrica','code':'ZA'},{'name':'SouthGeorgiaandtheSouthSandwichIslands','code':'GS'},{'name':'Spain','code':'ES'},{'name':'SriLanka','code':'LK'},{'name':'Sudan','code':'SD'},{'name':'Suriname','code':'SR'},{'name':'SvalbardandJanMayen','code':'SJ'},{'name':'Swaziland','code':'SZ'},{'name':'Sweden','code':'SE'},{'name':'Switzerland','code':'CH'},{'name':'SyrianArabRepublic','code':'SY'},{'name':'Taiwan,ProvinceofChina','code':'TW'},{'name':'Tajikistan','code':'TJ'},{'name':'Tanzania,UnitedRepublicof','code':'TZ'},{'name':'Thailand','code':'TH'},{'name':'Timor-Leste','code':'TL'},{'name':'Togo','code':'TG'},{'name':'Tokelau','code':'TK'},{'name':'Tonga','code':'TO'},{'name':'TrinidadandTobago','code':'TT'},{'name':'Tunisia','code':'TN'},{'name':'Turkey','code':'TR'},{'name':'Turkmenistan','code':'TM'},{'name':'TurksandCaicosIslands','code':'TC'},{'name':'Tuvalu','code':'TV'},{'name':'Uganda','code':'UG'},{'name':'Ukraine','code':'UA'},{'name':'UnitedArabEmirates','code':'AE'},{'name':'UnitedKingdom','code':'GB'},{'name':'UnitedStates','code':'US'},{'name':'UnitedStatesMinorOutlyingIslands','code':'UM'},{'name':'Uruguay','code':'UY'},{'name':'Uzbekistan','code':'UZ'},{'name':'Vanuatu','code':'VU'},{'name':'Venezuela','code':'VE'},{'name':'VietNam','code':'VN'},{'name':'VirginIslands,British','code':'VG'},{'name':'VirginIslands,U.S.','code':'VI'},{'name':'WallisandFutuna','code':'WF'},{'name':'WesternSahara','code':'EH'},{'name':'Yemen','code':'YE'},{'name':'Zambia','code':'ZM'},{'name':'Zimbabwe','code':'ZW'}]") ;
        	for ( int i = 0 ; i < arr_country.length() ; i++ )
            {
            	JSONObject obj = arr_country.getJSONObject(i) ;
            	if ( obj.getString("name").equals(GlobalVariable.country) )
            		_temp = _temp.concat(String.format("<option value='%s' selected>%s</option>", obj.getString("name"), obj.getString("name"))) ;
            	else
            		_temp = _temp.concat(String.format("<option value='%s'>%s</option>", obj.getString("name"), obj.getString("name"))) ;
            }
		} catch (JSONException e) {
			e.printStackTrace();
		}
        
        class MyJavaScriptInterface
        {
            @SuppressWarnings("unused")
            public void processHTML(String html)
            {
                Log.d("AAA", html) ;
                String jsonStr = html.replace("<head></head><body style=\"color:white !important;\">", "").replace("</body>", "") ;
                try {
					JSONObject jsonObj = new JSONObject(jsonStr) ;
					JSONArray jsonArr = jsonObj.getJSONArray("results") ;
					jsonObj = jsonArr.getJSONObject(0) ;
					
					GlobalVariable.f_name = jsonObj.getString("f_name");
					GlobalVariable.l_name = jsonObj.getString("l_name");
					GlobalVariable.email = jsonObj.getString("email");
					GlobalVariable.country = jsonObj.getString("country");
					GlobalVariable.dob = jsonObj.getString("dob");
					GlobalVariable.fb_id = jsonObj.getString("fb_id");
					GlobalVariable.profile_photo_path = jsonObj.getString("profile_pic");
					GlobalVariable.tempGender = jsonObj.getString("gender");
					GlobalVariable.user_id = jsonObj.getString("u_id") ;
					
					Intent intent = new Intent(ProfileActivity.this, MainActivity.class) ;
			        startActivity(intent) ;
			        finish() ;
					
				} catch (JSONException e) {
					e.printStackTrace();
				}
            }
        }
        
        htmlString = htmlString.replace("INSERT-COUNTRY-SELECT-HERE", _temp) ;
        
        webview.getSettings().setJavaScriptEnabled(true);
        webview.getSettings().setLoadWithOverviewMode(true);
        webview.setScrollBarStyle(WebView.SCROLLBARS_OUTSIDE_OVERLAY);
        webview.setScrollbarFadingEnabled(false);
        webview.getSettings().setBuiltInZoomControls(true);
        webview.getSettings().setPluginState(PluginState.ON);
        webview.getSettings().setAllowFileAccess(true);
        webview.getSettings().setSupportZoom(true);
        webview.loadData(htmlString, "text/html; charset=utf-8", "UTF-8");
        webview.addJavascriptInterface(new MyJavaScriptInterface(), "HTMLOUT");
        startWebView() ; 
        
        TextView btn_logout = (TextView)findViewById(R.id.btn_logout) ;
        btn_logout.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {				
				new AlertDialog.Builder(ProfileActivity.this)
		        .setIcon(android.R.drawable.ic_dialog_alert)
		        .setTitle("Are you sure?")
		        .setMessage("You will have to log in again to view videos.")
		        .setPositiveButton("Yes", new DialogInterface.OnClickListener()
			    {
			        @Override
			        public void onClick(DialogInterface dialog, int which) {
			        	SharedPreferences prefs = getSharedPreferences("user_info", Context.MODE_PRIVATE) ;
						prefs.edit().clear().commit() ;
						
						GlobalVariable.user_id = "-10" ;
						Intent intent = new Intent(ProfileActivity.this, FirstActivity.class) ;
						startActivity(intent) ;
						finish() ;
			        }	
			    })
			    
			    .setNegativeButton("No", null)
			    .show();
			}
		}) ;
	}
		
	@SuppressLint("JavascriptInterface")
	private void startWebView() {
        webview.setWebViewClient(new WebViewClient() {      
            ProgressDialog progressDialog;
          
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) { 
                if(url.contains("set.php")){ 
                    view.getContext().startActivity( new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
                    return true;                     
                } else {
                    view.loadUrl(url); 
                    return true;
                }                   
            }
             
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {

            	if (progressDialog == null) {
                    
                    progressDialog = new ProgressDialog(ProfileActivity.this);
                    progressDialog.setMessage("Loading...");
                    progressDialog.show();
                }
            } 
             
            public void onLoadResource (WebView view, String url) {
             
                if (progressDialog == null) {
                     
                    progressDialog = new ProgressDialog(ProfileActivity.this);
                    progressDialog.setMessage("Loading...");
                    progressDialog.show();
                }
            }
             
            public void onPageFinished(WebView view, String url) {
                 
                try{
                    if (progressDialog.isShowing()) {
                        progressDialog.dismiss();
                        progressDialog = null;
                    }
                    
                }catch(Exception exception){
                    exception.printStackTrace();
                }
                
                if ( url.equals("http://longhairhow2.com/api/user/set.php") )
                {
                	view.loadUrl("javascript:window.HTMLOUT.processHTML(document.documentElement.innerHTML);") ;
                }
            }
            
        }); 
          
        webview.setWebChromeClient(new WebChromeClient() {
             
            public void openFileChooser(ValueCallback<Uri> uploadMsg, String acceptType){  
                
                mUploadMessage = uploadMsg;
                try{    
                 
                    File imageStorageDir = new File(
                                           Environment.getExternalStoragePublicDirectory(
                                           Environment.DIRECTORY_PICTURES)
                                           , "AndroidExampleFolder");
                                            
                    if (!imageStorageDir.exists()) {
                        imageStorageDir.mkdirs();
                    }
                     
                    File file = new File(
                                    imageStorageDir + File.separator + "IMG_"
                                    + String.valueOf(System.currentTimeMillis()) 
                                    + ".jpg");
                                     
                    mCapturedImageURI = Uri.fromFile(file); 
                     
                    final Intent captureIntent = new Intent(
                                                  android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                                                   
                    captureIntent.putExtra(MediaStore.EXTRA_OUTPUT, mCapturedImageURI);
                    
                    Intent i = new Intent(Intent.ACTION_GET_CONTENT); 
                    i.addCategory(Intent.CATEGORY_OPENABLE);
                    i.setType("image/*");
                     
                    Intent chooserIntent = Intent.createChooser(i, "Image Chooser");
                    chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS
                                           , new Parcelable[] { captureIntent });
                    startActivityForResult(chooserIntent, FILECHOOSER_RESULTCODE);
                     
                  }
                 catch(Exception e){
                     Toast.makeText(getBaseContext(), "Exception:"+e, 
                                Toast.LENGTH_LONG).show();
                 }
                 
            }
             
            @SuppressWarnings("unused")
			public void openFileChooser(ValueCallback<Uri> uploadMsg){
                openFileChooser(uploadMsg, "");
            }
             
            @SuppressWarnings("unused")
			public void openFileChooser(ValueCallback<Uri> uploadMsg, 
                                       String acceptType, 
                                       String capture) {
                                        
                openFileChooser(uploadMsg, acceptType);
            }
 
 
 
            public boolean onConsoleMessage(ConsoleMessage cm) {  
                   
                onConsoleMessage(cm.message(), cm.lineNumber(), cm.sourceId());
                return true;
            }
             
            public void onConsoleMessage(String message, int lineNumber, String sourceID) {
                 
            }
        });   
    }
	
    @Override 
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) { 
         
    	if(requestCode==FILECHOOSER_RESULTCODE)  
    	{  
            if (null == this.mUploadMessage) {
                return;
            }
 
            Uri result=null;
            
            try{
            	if (resultCode != RESULT_OK) {
            		result = null;
                } else {
                    result = intent == null ? mCapturedImageURI : intent.getData(); 
                } 
            }
            catch(Exception e)
            {
                Toast.makeText(getApplicationContext(), "activity :"+e,
                 Toast.LENGTH_LONG).show();
            }
             
            mUploadMessage.onReceiveValue(result);
            mUploadMessage = null;
    	}
    }
    
	@Override
	public void onConfigurationChanged(Configuration newConfig){        
	    super.onConfigurationChanged(newConfig);
	}
	
	@SuppressWarnings("deprecation")
	private String getAssetsContent()
	{
		StringBuilder returnString = new StringBuilder();
	    InputStream fIn = null;
	    InputStreamReader isr = null;
	    BufferedReader input = null;
	    try {
	        fIn = getResources().getAssets().open("user-profile.html", Context.MODE_WORLD_READABLE);
	        isr = new InputStreamReader(fIn);
	        input = new BufferedReader(isr);
	        String line = "";
	        while ((line = input.readLine()) != null) {
	            returnString.append(line);
	        }
	    } catch (Exception e) {
	        e.getMessage();
	    } finally {
	        try {
	            if (isr != null)
	                isr.close();
	            if (fIn != null)
	                fIn.close();
	            if (input != null)
	                input.close();
	        } catch (Exception e2) {
	            e2.getMessage();
	        }
	    }
	    
	    return returnString.toString() ;
	}
}
