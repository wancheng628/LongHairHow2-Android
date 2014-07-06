package au.com.sharonblain.search;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Iterator;

import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.StringBody;
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

@SuppressWarnings("deprecation")
public class SearchListActivity extends Activity implements AsyncResponse  {

	private ProgressDialog _dialog_progress ;
	private HttpPostTask httpTask = new HttpPostTask() ;
	private ListView list ;
	private ArrayList<BunImageDetailArray> detail_list ;
	
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
				
				startActivity(myIntent);
			}
        });
		
		getSearchResult() ;
	}
	
	private void getSearchResult()
	{
		if ( !_dialog_progress.isShowing() )
    		_dialog_progress = ProgressDialog.show(SearchListActivity.this, "Connecting Server...", 
    				"Please wait a sec.", true);
    	
    	MultipartEntity params = new MultipartEntity();
		try {
			params.addPart("action", new StringBody("/vid/search"));
			params.addPart("user_id", new StringBody(GlobalVariable.user_id));
			params.addPart("accessToken", new StringBody(GlobalVariable.accessToken));
			params.addPart("query", new StringBody(getIntent().getExtras().getString("query"))) ;
			
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		
		GlobalVariable.request_url = "http://longhairhow2.com/api/vid/search" ;
		
		httpTask = new HttpPostTask() ;
		httpTask.delegate = SearchListActivity.this ;
		httpTask.execute(params) ;
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
					JSONObject result = jsonObj.getJSONObject("results") ;
					
					ArrayList<BunImageArray> dataArr = new ArrayList<BunImageArray>() ;
					/*
					for ( int i = 0 ; i < result.length() ; i++ )
					{
						JSONObject jObject = result.
						BunImageArray _item = new BunImageArray(jObject.getString("title"), jObject.getString("description"), jObject.getString("bun_image")) ;
						dataArr.add(_item) ;
					}*/
					
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
				            	
				            	JSONArray temp = jObject.getJSONArray("videos") ;
				            	
				            	String _temp_title = "" ;
				            	String _temp_images = "" ;
				            	
				            	for ( int j = 0 ; j < temp.length() ; j++ )
				            	{
				            		JSONObject jObject2 = temp.getJSONObject(j) ;
				            		
				            		if ( key.equals(jObject2.getString("v_id")) )
				            		{
				            			BunImageArray _item = new BunImageArray(jObject2.getString("vid_title"), "Found in collection " + 
				            					jObject2.getString("vid_title"), "http://longhairhow2.com/api" + jObject2.getString("vid_image")) ;
										dataArr.add(_item) ;
				            		}
				            		
				            		_temp_title = _temp_title + jObject2.getString("vid_title") + "^" ;
				            		_temp_images =  _temp_images + "http://longhairhow2.com/api" + jObject2.getString("vid_image") + "^" ;
				            	}
				            	
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
				}
			
			} catch (JSONException e) {
				e.printStackTrace();
				
			}
		} else {
			Log.e("ServiceHandler", "Couldn't get any data from the url") ;
			
		}
	}
	
	public class BunImageDetailArray {
		String images ;
		String titles ;
		String title ;
		String descritpion ;
		String video ;
	}

}
