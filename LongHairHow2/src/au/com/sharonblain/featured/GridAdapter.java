package au.com.sharonblain.featured;

import java.util.ArrayList;
import com.squareup.picasso.Picasso;
import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ViewFlipper;
import au.com.sharonblain.longhairhow2.R;
import au.com.sharonblain.request_server.GlobalVariable;

public class GridAdapter  extends ArrayAdapter<String>{
	
	private final Context context;
	
	public ArrayList<String> titles ;
	public ArrayList<String> image_urls;
	
	public GridAdapter(Context context, ArrayList<String> titles) {
		
		super(context, R.layout.item_photo);
		
		this.context = context;
		this.titles = titles;

	}
	
	public void updateAdapter(ArrayList<String> titles, ArrayList<String> icons, 
								ArrayList<String> prices, ArrayList<String> vid_url, ArrayList<String> descriptions) {
		this.titles = titles;
		this.image_urls = icons ;	
		 
	}
	
	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {

		//ViewHolder holder = new ViewHolder();
		TextView txtTitle ;
		ViewFlipper flipper ;
		View gridView;
		
		if (convertView == null) {
			//holder = new ViewHolder(); 
			LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convertView = inflater.inflate(R.layout.item_photo, null);
			gridView = convertView ;
		    	
		} 
		else
	    {
			gridView= convertView;
	    }
		
		flipper = (ViewFlipper) convertView.findViewById(R.id.flipper) ;
		txtTitle = (TextView) convertView.findViewById(R.id.label_hair_name) ;
        txtTitle.setTypeface(GlobalVariable.tf_medium) ;
        
		if ( this.titles != null )
		{
			txtTitle.setText(this.titles.get(position)) ;
		}
		
		if ( this.image_urls != null )
		{
			String _temp = this.image_urls.get(position) ;
			_temp.replace(" ", "").replace(",", "") ;
			String icon_urls[] = _temp.trim().split("\\^") ;
			
			ImageView image_views[] ;
			image_views = new ImageView[icon_urls.length] ;
			
			flipper.removeAllViews() ;
			flipper.stopFlipping() ;
			for ( int i = 0 ; i < icon_urls.length ; i++ )
			{
				image_views[i] = new ImageView(context) ;
					
				if ( icon_urls[i] != null && icon_urls[i].length() > 1 )
					Picasso.with(context).load(Uri.parse(icon_urls[i])).into(image_views[i]) ;
				
				flipper.addView(image_views[i]) ;
			}
			
			flipper.setInAnimation(context, R.drawable.view_translation_in_left);
			flipper.setOutAnimation(context, R.drawable.view_translation_in_right);
			flipper.startFlipping();
		}
		
		return gridView;
	}
		
	@Override
	public int getCount() {
		return this.titles.size() ;		
	}

	@Override
	public String getItem(int arg0) {
		return this.titles.get(arg0) ;
	}

	@Override
	public long getItemId(int arg0) {
		return 0;
	}
	
	/*
	static class ViewHolder {
		ViewFlipper flipper ;
		TextView txtTitle ;
		//ImageView images[] ;
		
	}*/
}
