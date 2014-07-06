package au.com.sharonblain.longhairhow2;

import android.app.TabActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TabHost;
import android.widget.TextView;
import au.com.sharonblain.featured.FeaturedActivity;
import au.com.sharonblain.search.SearchActivity;
import au.com.sharonblain.yourvideo.YourVideoActivity;

@SuppressWarnings("deprecation")
public class MainActivity extends TabActivity {
	/** Called when the activity is first created. */	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		setTabs() ;
	}
	private void setTabs()
	{
		addTab("Featured", R.drawable.tab_featured, FeaturedActivity.class);
		addTab("Search", R.drawable.tab_search, SearchActivity.class);
		addTab("Your Videos", R.drawable.tab_video, YourVideoActivity.class);
		addTab("News", R.drawable.tab_news, FeaturedActivity.class);
		addTab("More", R.drawable.tab_more, FeaturedActivity.class);
		
		//To add more tabs just use addTab() method here like previous line.
	}
	
	private void addTab(String labelId, int drawableId, Class<?> c)
	{
		TabHost tabHost = getTabHost();
		Intent intent = new Intent(this, c);
		TabHost.TabSpec spec = tabHost.newTabSpec("tab" + labelId);	
		
		View tabIndicator = LayoutInflater.from(this).inflate(R.layout.tab_indicator, getTabWidget(), false);
		ImageView icon = (ImageView) tabIndicator.findViewById(R.id.icon);
		icon.setImageResource(drawableId);
		
		TextView title = (TextView)tabIndicator.findViewById(R.id.title) ;
		title.setText(labelId);
		spec.setIndicator(tabIndicator);
		spec.setContent(intent);
		tabHost.addTab(spec);
		
		tabHost.getTabWidget().setStripEnabled(false);
	}
}