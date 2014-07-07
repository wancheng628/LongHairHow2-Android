package au.com.sharonblain.request_server;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import android.annotation.SuppressLint;
import android.app.Application;
import android.graphics.Bitmap;

public class GlobalVariable extends Application {
    private static GlobalVariable singleton ;
    public static String request_url ;			// For HttpPostTask class's url
    
    public static String accessToken ;
    public static String validity ;
    public static String user_id ;
    public static Boolean f_valid ;
    
    public static String f_name ;
    public static String l_name ;
    public static String email ;
    public static String country ;
    public static String dob ;
    public static String fb_id ;
    public static Bitmap photo ;
    public static String profile_photo_path ;
    
    public static int request_register ;
    
    public static String tempCountry ;
    public static String tempGender ;
    public static String tempBirthday ;
    
    public static Date cur_sydney_time ;
    
    @SuppressLint("SimpleDateFormat")
	public static Date getDateFromString(String str_date)
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
    
    public static GlobalVariable getInstance() {
        return singleton;
    }
    
    @Override
    public void onCreate() {
        super.onCreate();
        singleton = this;
    }
    
    public static void getSydneyTime()
    {
    	TimeZone tz = TimeZone.getTimeZone("GMT+10:00");
    	Calendar c = Calendar.getInstance(tz);
    	GlobalVariable.cur_sydney_time = c.getTime() ;
    }
}