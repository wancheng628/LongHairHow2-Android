package au.com.sharonblain.request_server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;

import android.os.AsyncTask;
import android.os.StrictMode;
import android.util.Log;

public class HttpPostTask extends AsyncTask<ArrayList<NameValuePair>, String, String> 
{     
	public AsyncResponse delegate = null ;
	private String header_key = "SBHAIR-APPLICATION-ID" ;
	private String header_value = "sbhair-android-v1.0" ;

	@Override
	protected String doInBackground(ArrayList<NameValuePair>... param) {
		ArrayList<NameValuePair> param2 = param[0] ;
		InputStream is = null;
	    String json = "";
	    
    	StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        
        // Making HTTP request
        try {
            DefaultHttpClient httpClient = new DefaultHttpClient();
            HttpPost httpPost = new HttpPost(GlobalVariable.request_url);
            httpPost.setHeader(header_key, header_value) ;
            httpPost.setEntity(new UrlEncodedFormEntity(param2)) ;
            HttpResponse httpResponse = httpClient.execute(httpPost);
            HttpEntity httpEntity = httpResponse.getEntity();
           
            is = httpEntity.getContent();
 
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
 
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(
                    is, "utf-8"), 8);
            StringBuilder sb = new StringBuilder();
            String line = null;
            while ((line = reader.readLine()) != null) {
                sb.append(line + "");
            }
            is.close();
            json = sb.toString();
            Log.e("JSON", json);
        } catch (Exception e) {
            Log.e("Buffer Error", "Error converting result " + e.toString());
        }
 
        return json; 
	}
	
	@Override
	protected void onPostExecute(String page)
    {       
		delegate.processFinish(page);
    }
}