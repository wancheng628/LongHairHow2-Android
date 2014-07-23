package au.com.sharonblain.search;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;

import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.Toast;
import au.com.sharonblain.featured.FeaturedDetailActivity;
import au.com.sharonblain.longhairhow2.R;
import au.com.sharonblain.request_server.AsyncResponse;
import au.com.sharonblain.request_server.GlobalVariable;
import au.com.sharonblain.request_server.HttpPostTask;

public class SearchListActivity extends Activity implements AsyncResponse  {

	private ProgressDialog _dialog_progress ;
	private HttpPostTask httpTask = new HttpPostTask() ;
	private ListView list ;
	private ArrayList<BunImageDetailArray> detail_list ;
	private int nRequestKind ;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_search_list);
		
		_dialog_progress = new ProgressDialog(SearchListActivity.this) ;
		httpTask.delegate = SearchListActivity.this ;
		
		detail_list = new ArrayList<SearchListActivity.BunImageDetailArray>() ;
		
		list = (ListView)findViewById(R.id.search_list) ;
		list.setOnItemClickListener(new OnItemClickListener() {
            
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				
				Intent myIntent = new Intent(SearchListActivity.this, FeaturedDetailActivity.class);
				
				myIntent.putExtra("title", detail_list.get(arg2).title) ;
				myIntent.putExtra("description", detail_list.get(arg2).descritpion) ;
				myIntent.putExtra("video", detail_list.get(arg2).video) ;
				myIntent.putExtra("images", detail_list.get(arg2).images) ;
				myIntent.putExtra("titles", detail_list.get(arg2).titles) ;
				myIntent.putExtra("b_id", detail_list.get(arg2).b_id) ;
				myIntent.putExtra("prices", detail_list.get(arg2).prices) ;
				myIntent.putExtra("v_ids", detail_list.get(arg2).v_ids) ;
				
				startActivity(myIntent);
			}
        });
		
		getSearchResult() ;
	}
	
	private void getSearchResult()
	{
		nRequestKind = 1 ;
		
		if ( _dialog_progress == null || !_dialog_progress.isShowing() )
		{
			_dialog_progress = ProgressDialog.show(SearchListActivity.this, "Connecting Server...", "Please wait a sec.", true);			
		}
    				
    	
    	MultipartEntityBuilder params = MultipartEntityBuilder.create() ;
		params.addTextBody("action", "/vid/search", ContentType.TEXT_PLAIN) ;
		params.addTextBody("user_id", GlobalVariable.user_id, ContentType.TEXT_PLAIN) ;
		params.addTextBody("accessToken", GlobalVariable.accessToken, ContentType.TEXT_PLAIN) ;
		params.addTextBody("query", getIntent().getExtras().getString("query"), ContentType.TEXT_PLAIN) ;
		
		GlobalVariable.request_url = "http://longhairhow2.com/api/vid/search" ;
		
		httpTask = new HttpPostTask() ;
		httpTask.delegate = SearchListActivity.this ;
		httpTask.execute(params) ;
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
    		_dialog_progress = ProgressDialog.show(this, "Connecting Server...", 
        				"Getting Access Token... Please wait a sec.", true);    		
    	}	
    	
    	GlobalVariable.getSydneyTime() ;
    	
    	MultipartEntityBuilder params = MultipartEntityBuilder.create() ;
		params.addTextBody("action", "/common/access-token/grant", ContentType.TEXT_PLAIN) ;
		params.addTextBody("user_id", GlobalVariable.user_id, ContentType.TEXT_PLAIN) ;
		
		GlobalVariable.request_url = "http://longhairhow2.com/api/common/access-token/grant" ;
		
		httpTask = new HttpPostTask() ;
		httpTask.delegate = this;
		httpTask.execute(params) ;
    }
	
	@Override
	public void processFinish(String output) {
		if ( (_dialog_progress != null) && (_dialog_progress.isShowing()) )
		{
			try {
				_dialog_progress.dismiss() ;
				_dialog_progress = null ;
			} catch (NullPointerException e) {
				e.printStackTrace() ;
			} catch (IllegalArgumentException e) {
				e.printStackTrace() ;
			}
		}
		
		if ( nRequestKind == 1 ) {
			if (output.length() > 0) {
				try {
					JSONObject jsonObj = new JSONObject(output) ;
					if (jsonObj.get("type").equals("Success"))
					{
						JSONObject result = jsonObj.getJSONObject("results") ;
						
						ArrayList<BunImageArray> dataArr = new ArrayList<BunImageArray>() ;
											
						@SuppressWarnings("unchecked")
						Iterator<String> iter = result.keys() ;
					    while (iter.hasNext()) {
					        String key = iter.next() ;
					        try {
					            JSONArray jArray = result.getJSONArray(key) ;
					            
					            for ( int i = 0 ; i < jArray.length() ; i++ )
					            {
					            	JSONObject jObject = jArray.getJSONObject(i) ; 
					            	
					            	BunImageDetailArray _temp = new BunImageDetailArray() ;
					            	
					            	_temp.descritpion = jObject.getString("description") ;
					            	_temp.title = jObject.getString("title") ;
					            	_temp.video = jObject.getString("bun_vid_url") ;
					            	_temp.b_id = jObject.getString("b_id") ;
					            	_temp.prices = jObject.getString("price") ;
					            	
					            	JSONArray temp = jObject.getJSONArray("videos") ;
					            	
					            	String _temp_title = "" ;
					            	String _temp_images = "" ;
					            	String _temp_vid = "" ;
					            	
					            	for ( int j = 0 ; j < temp.length() ; j++ )
					            	{
					            		JSONObject jObject2 = temp.getJSONObject(j) ;
					            		_temp_vid = _temp_vid + jObject2.getString("v_id") + "^" ;
					            		
					            		if ( key.equals(jObject2.getString("v_id")) )
					            		{
					            			BunImageArray _item = new BunImageArray(jObject2.getString("vid_title"), "Found in collection " + 
					            					jObject2.getString("vid_title"), "http://longhairhow2.com/api" + jObject2.getString("vid_image")) ;
											dataArr.add(_item) ;
											
					            		}
					            		
					            		_temp_title = _temp_title + jObject2.getString("vid_title") + "^" ;
					            		_temp_images =  _temp_images + "http://longhairhow2.com/api" + jObject2.getString("vid_image") + "^" ;
					            	}
					            	
					            	_temp.v_ids = _temp_vid ;
					            	_temp.titles = _temp_title ;
					            	_temp.images = _temp_images ;
					            	
					            	detail_list.add(_temp) ;
					            }
										
					        } catch (JSONException e) {
					            // Something went wrong!
					        }
					    }
					    
					    BunImageListAdapter adapter = new BunImageListAdapter(SearchListActivity.this, dataArr) ;
						list.setAdapter(adapter) ;
					}
					else
					{
						Toast.makeText(SearchListActivity.this, jsonObj.getString("type") + " - " + jsonObj.getString("message"), Toast.LENGTH_LONG).show() ;
						getAccessToken() ;
					}
				
				} catch (JSONException e) {
					e.printStackTrace();
					
				}
			} else {
				Log.e("ServiceHandler", "Couldn't get any data from the url") ;
				
			}
		}
		else {
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
						
						getSearchResult() ;
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
	}
	
	public class BunImageDetailArray {
		String images ;
		String titles ;
		String title ;
		String descritpion ;
		String video ;
		String b_id ;
		String v_ids ;
		String prices ;
	}

}
