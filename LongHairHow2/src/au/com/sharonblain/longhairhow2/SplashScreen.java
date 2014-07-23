package au.com.sharonblain.longhairhow2;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import au.com.sharonblain.request_server.GlobalVariable;
 
public class SplashScreen extends Activity {
 
    // Splash screen timer
    private static int SPLASH_TIME_OUT = 500;
 
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
 
        GlobalVariable.accessToken = "" ;
        GlobalVariable.tf_light = Typeface.createFromAsset(getAssets(), "DINPro-Light.otf");
        GlobalVariable.tf_medium = Typeface.createFromAsset(getAssets(), "DINPro-Medium.otf");
        
        new Handler().postDelayed(new Runnable() {
            
             // Showing splash screen with a timer. This will be useful when you
             // want to show case your app logo / company
 
            @Override
            public void run() {
                // This method will be executed once the timer is over
                // Start your app main activity
                Intent i = new Intent(SplashScreen.this, FirstActivity.class);
                startActivity(i);
 
                // close this activity
                //finish();
            }
        }, SPLASH_TIME_OUT);
        
        
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
}