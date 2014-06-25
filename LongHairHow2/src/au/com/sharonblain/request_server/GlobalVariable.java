package au.com.sharonblain.request_server;

import android.app.Application;
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
    
    public static GlobalVariable getInstance() {
        return singleton;
    }
    @Override
    public void onCreate() {
        super.onCreate();
        singleton = this;
    }
}