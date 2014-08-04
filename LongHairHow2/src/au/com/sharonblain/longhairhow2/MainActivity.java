package au.com.sharonblain.longhairhow2;

import android.app.TabActivity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TabHost;
import android.widget.TabHost.OnTabChangeListener;
import android.widget.TextView;
import au.com.sharonblain.featured.FeaturedActivity;
import au.com.sharonblain.news.NewsActivity;
import au.com.sharonblain.request_server.GlobalVariable;
import au.com.sharonblain.search.SearchActivity;
import au.com.sharonblain.uservideo.UserVideoActivity;

@SuppressWarnings("deprecation")
public class MainActivity extends TabActivity {
	/** Called when the activity is first created. */
	
	private TabHost tabHost ;
	boolean firstStatus = true ;
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		setTabs() ;
		
	}
	
	@Override
	public void onBackPressed() {
	}
	
	private void setTabs()
	{
		addTab("Featured", R.drawable.tab_featured, FeaturedActivity.class);
		addTab("Search", R.drawable.tab_search, SearchActivity.class);
		addTab("Your Videos", R.drawable.tab_video, UserVideoActivity.class);
		addTab("News", R.drawable.tab_news, NewsActivity.class);
		addTab("More", R.drawable.tab_more, MoreActivity.class);
		
		//To add more tabs just use addTab() method here like previous line.
		
		tabHost.setOnTabChangedListener(new OnTabChangeListener() {

		    @Override
		    public void onTabChanged(String tabId) {

		        for (int i = 0; i < tabHost.getTabWidget().getChildCount(); i++) {
		        	TextView tv = (TextView) tabHost.getTabWidget().getChildAt(i).findViewById(R.id.title); //Unselected Tabs
		            tv.setTextColor(Color.parseColor("#888888"));
		        }

		        TextView tv = (TextView) tabHost.getCurrentTabView().findViewById(R.id.title); //for Selected Tab
		        tv.setTextColor(Color.BLACK) ;

		    }
		});
	}
	
	private void addTab(String labelId, int drawableId, Class<?> c)
	{
		tabHost = getTabHost();
		Intent intent = new Intent(this, c);
		TabHost.TabSpec spec = tabHost.newTabSpec("tab" + labelId);	
		
		View tabIndicator = LayoutInflater.from(this).inflate(R.layout.tab_indicator, getTabWidget(), false);
		ImageView icon = (ImageView) tabIndicator.findViewById(R.id.icon);
		icon.setImageResource(drawableId);
		
		TextView title = (TextView)tabIndicator.findViewById(R.id.title) ;
		title.setText(labelId);
		title.setTypeface(GlobalVariable.tf_medium) ;
		
		if ( firstStatus )
		{
			title.setTextColor(Color.parseColor("#000000")) ;
			firstStatus = false ;
		}
		
		spec.setIndicator(tabIndicator);
		spec.setContent(intent);
		tabHost.addTab(spec);
		
		tabHost.getTabWidget().setStripEnabled(false);
	}
	
}