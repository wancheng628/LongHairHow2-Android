package au.com.sharonblain.request_server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.impl.client.DefaultHttpClient;

import android.os.AsyncTask;
import android.os.StrictMode;

@SuppressWarnings("deprecation")
public class HttpGetTask extends AsyncTask<MultipartEntity, String, String> 
{     
	public AsyncResponse delegate = null ;
	
	@Override
	protected String doInBackground(MultipartEntity... param) {
		
    	StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        String responseStr = "" ;
        
        HttpClient httpClient = new DefaultHttpClient();  
        HttpGet httpGet = new HttpGet(GlobalVariable.request_url);
        
        try {
            HttpResponse response = httpClient.execute(httpGet);
            StatusLine statusLine = response.getStatusLine();
            if (statusLine.getStatusCode() == HttpStatus.SC_OK) {
                HttpEntity entity = response.getEntity();
                if(entity != null)
                {
                	InputStream inputStream = response.getEntity().getContent() ;
                	if (inputStream != null) {
                        Writer writer = new StringWriter();

                        char[] buffer = new char[1024];
                        try {
                            Reader reader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"), 1024);
                            int n;
                            while ((n = reader.read(buffer)) != -1) {
                                writer.write(buffer, 0, n);
                            }
                        } finally {
                            inputStream.close();
                        }
                        return writer.toString();
                    } else {
                        return "";
                    }
                }
                
            } else {
                return null ;
            }
        } catch (ClientProtocolException e) {
            // handle exception
        } catch (IOException e) {
            // handle exception
        }
        
        return responseStr; 
	}
	
	@Override
	protected void onPostExecute(String page)
    {       
		delegate.processFinish(page);
    }
}