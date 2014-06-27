package au.com.sharonblain.request_server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.impl.client.DefaultHttpClient;

import android.os.AsyncTask;
import android.os.StrictMode;
import android.util.Log;

@SuppressWarnings("deprecation")
public class HttpPostTask extends AsyncTask<MultipartEntity, String, String> 
{     
	public AsyncResponse delegate = null ;
	private String header_key = "SBHAIR-APPLICATION-ID" ;
	private String header_value = "sbhair-android-v1.0" ;

	@Override
	protected String doInBackground(MultipartEntity... param) {
		MultipartEntity param2 = param[0] ;
		InputStream is = null;
	    String json = "";
	    
    	StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        
        // Making HTTP request
        try {
            DefaultHttpClient httpClient = new DefaultHttpClient();
            HttpPost httpPost = new HttpPost(GlobalVariable.request_url);
            httpPost.setHeader(header_key, header_value) ;
            httpPost.setEntity(param2);
            /*
            if ( GlobalVariable.request_register == 1 )
            {
            	Bitmap bitmap = GlobalVariable.photo ;
            	String sendImg = "" ;
            	if ( bitmap != null )
            	{
            		ByteArrayOutputStream stream = new ByteArrayOutputStream();
            		bitmap.compress(Bitmap.CompressFormat.JPEG, 90, stream);
                    
                    byte[] byteArray=stream.toByteArray();
                    sendImg = Base64.encodeToString(byteArray,Base64.DEFAULT);
                    
                    //httpPost.addHeader("content-type", "multipart/form-data");
                	//httpPost.addHeader("ENCTYPE", "multipart/form-data");
                	
                    param2.add(new BasicNameValuePair("profile_pic", sendImg));
            	}
                
                GlobalVariable.request_register = 0 ;
            }
           
            httpPost.setEntity(new UrlEncodedFormEntity(param2)) ;
             */
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