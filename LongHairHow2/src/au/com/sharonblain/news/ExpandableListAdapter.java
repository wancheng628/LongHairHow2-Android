package au.com.sharonblain.news;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import au.com.sharonblain.longhairhow2.R;
import au.com.sharonblain.request_server.GlobalVariable;

@SuppressLint("SetJavaScriptEnabled")
public class ExpandableListAdapter extends BaseExpandableListAdapter {
 
    private Activity context;
    NewsItem[] news ;
    
    public ExpandableListAdapter(Activity context, NewsItem[] news) {
        this.context = context ;
        this.news = news ;
    }
 
    public Object getChild(int groupPosition, int childPosition) {
        return news[groupPosition] ;
    }
 
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }
     
    public View getChildView(final int groupPosition, final int childPosition,
            boolean isLastChild, View convertView, ViewGroup parent) {
        LayoutInflater inflater = context.getLayoutInflater();
         
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.item_news_child, null);
        }
        
        WebView webview = (WebView)convertView.findViewById(R.id.webView1) ;
        webview.clearFormData() ;
        String htmlString = getAssetsContent() ;
        htmlString = htmlString.replace("INSERT-BLOG-CONTENT-HERE", news[groupPosition].getBody()) ;
        htmlString = htmlString.replace("INSERT-BLOG-TITLE-HERE", news[groupPosition].getTitle()) ;
        webview.getSettings().setJavaScriptEnabled(true);
        webview.loadData(htmlString, "text/html; charset=utf-8", "UTF-8");
        
        return convertView;
    }
 
    @SuppressWarnings("deprecation")
	private String getAssetsContent()
	{
		StringBuilder returnString = new StringBuilder();
	    InputStream fIn = null;
	    InputStreamReader isr = null;
	    BufferedReader input = null;
	    try {
	        fIn = context.getResources().getAssets()
	                .open("blog-page.html", Context.MODE_WORLD_READABLE);
	        isr = new InputStreamReader(fIn);
	        input = new BufferedReader(isr);
	        String line = "";
	        while ((line = input.readLine()) != null) {
	            returnString.append(line);
	        }
	    } catch (Exception e) {
	        e.getMessage();
	    } finally {
	        try {
	            if (isr != null)
	                isr.close();
	            if (fIn != null)
	                fIn.close();
	            if (input != null)
	                input.close();
	        } catch (Exception e2) {
	            e2.getMessage();
	        }
	    }
	    
	    return returnString.toString() ;
	}
    
    public int getChildrenCount(int groupPosition) {
        return 1 ;
    }
 
    public Object getGroup(int groupPosition) {
        return news[groupPosition] ;
    }
 
    public int getGroupCount() {
        return this.news.length ;
    }
 
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }
 
    public View getGroupView(int groupPosition, boolean isExpanded,
            View convertView, ViewGroup parent) {
        
    	if (convertView == null) {
            LayoutInflater infalInflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = infalInflater.inflate(R.layout.item_news_group,
                    null);
        }
        
        TextView label_title = (TextView) convertView.findViewById(R.id.label_title);
        label_title.setTypeface(GlobalVariable.tf_light) ;
        label_title.setText(news[groupPosition].getTitle()) ;
        
        TextView label_never = (TextView)convertView.findViewById(R.id.label_never) ;
        label_never.setTypeface(GlobalVariable.tf_light) ;
        
        ImageView img_check = (ImageView)convertView.findViewById(R.id.img_check) ;
        img_check.setImageDrawable(context.getResources().getDrawable(R.drawable.checkmark)) ;
        return convertView;
    }
 
    public boolean hasStableIds() {
        return true;
    }
 
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }
}