package au.com.sharonblain.longhairhow2;

import java.util.ArrayList;

import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.res.Resources;

import au.com.sharonblain.request_server.AsyncResponse;
import au.com.sharonblain.request_server.GlobalVariable;
import au.com.sharonblain.request_server.HttpPostTask;

public class MoreActivity extends Activity implements AsyncResponse{

	private HelperUtils utils;
    private ArrayList<String> imagePaths = new ArrayList<String>();
    private GridViewImageAdapter adapter;
    private GridView gridView;
    private int columnWidth;
    private ProgressDialog _dialog_progress ;
	private HttpPostTask httpTask = new HttpPostTask() ;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_more);
 
        httpTask.delegate = MoreActivity.this ;
        _dialog_progress = new ProgressDialog(MoreActivity.this) ;
        gridView = (GridView) findViewById(R.id.grid_view2);
        utils = new HelperUtils(this);
        InitilizeGridLayout();

        imagePaths = utils.getFilePaths();
        adapter = new GridViewImageAdapter(MoreActivity.this, imagePaths, columnWidth);
        gridView.setAdapter(adapter);
        
        setTypeFaceTextView() ;
        setEvents() ;
    }
    
	private void setEvents() {
		ImageView label_sharon = (ImageView)findViewById(R.id.img_sharon) ;
		ImageView label_gold = (ImageView)findViewById(R.id.img_gold) ;
		ImageView label_capps = (ImageView)findViewById(R.id.img_capps) ;
		
		final Intent intent = new Intent(MoreActivity.this, MoreWebViewActivity.class) ;
		
		label_sharon.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				intent.putExtra("url", "http://www.sharonblain.com/") ;
				startActivity(intent) ;
			}
		}) ;
		
		label_gold.setOnClickListener(new OnClickListener() {
					
			@Override
			public void onClick(View arg0) {
				intent.putExtra("url", "http://www.goldwell.com.au/") ;
				startActivity(intent) ;
			}
		}) ;
		
		label_capps.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				intent.putExtra("url", "http://www.connectedapps.com.au/") ;
				startActivity(intent) ;
			}
		}) ;
	}

	private void setTypeFaceTextView() {
		TextView label_sharon = (TextView)findViewById(R.id.label_sharon) ;
        TextView label_gold = (TextView)findViewById(R.id.label_goldwell) ;
        TextView label_capps = (TextView)findViewById(R.id.label_capps) ;
        
        label_sharon.setTypeface(GlobalVariable.tf_light) ;
        label_gold.setTypeface(GlobalVariable.tf_light) ;
        label_capps.setTypeface(GlobalVariable.tf_light) ;
	}

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
	public void processFinish(String output) throws IllegalArgumentException {
		if ( (_dialog_progress != null) && (_dialog_progress.isShowing()) )
		{
			try {
				_dialog_progress.dismiss() ;
				_dialog_progress = null;
		    } catch (Exception e) {
		        // nothing
		    }
		}
		
		if (output.length() > 0) {
			
		} else {
			Log.e("ServiceHandler", "Couldn't get any data from the url") ;
			
		}
		
	}
}
