package au.com.sharonblain.featured;

import java.util.ArrayList;

import ImageCache.ImageLoader;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ViewFlipper;
import au.com.sharonblain.longhairhow2.R;

public class GridAdapter  extends ArrayAdapter<String>{
	
	private final Context context;
	
	public ArrayList<String> titles ;
	public ArrayList<String> images;
	
	public GridAdapter(Context context, ArrayList<String> titles) {
		
		super(context, R.layout.item_photo);
		
		this.context = context;
		this.titles = titles;

	}
	
	public void updateAdapter(ArrayList<String> titles, ArrayList<String> icons, 
								ArrayList<String> prices, ArrayList<String> vid_url, ArrayList<String> descriptions) {
		this.titles = titles;
		this.images = icons ;	
		 
	}
	
	@Override
	public View getView(final int position, View view, ViewGroup parent) {

		ViewHolder holder = null;
		
		if (view == null) {
			LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	 		view = inflater.inflate(R.layout.item_photo, parent, false);
			holder = new ViewHolder();
			view.setTag(holder);			
		} 
		else
	    {
	        holder = (ViewHolder) view.getTag();
	    }
		
		holder.flipper = (ViewFlipper) view.findViewById(R.id.flipper) ;
		holder.txtTitle = (TextView) view.findViewById(R.id.label_hair_name) ;
        
		holder.txtTitle.setText(titles.get(position)) ;
		String _temp = images.get(position) ;
		_temp.replace(" ", "").replace(",", "") ;
		
		String icon_urls[] = _temp.trim().split("\\^") ;
		
		if ( holder.images == null )
		{
			holder.images = new ImageView[icon_urls.length] ;
			for ( int i = 0 ; i < icon_urls.length ; i++ )
			{
				ImageLoader loader = new ImageLoader(context) ;
				
				if ( holder.images[i] == null )
				{
					holder.images[i] = new ImageView(context) ;
					
					if ( icon_urls[i] != null && icon_urls.length > 1 )
						loader.DisplayImage(icon_urls[i], holder.images[i]) ;
					
				}
				holder.flipper.addView(holder.images[i]) ;
			}
		}
		
		holder.flipper.setInAnimation(context, R.drawable.view_translation_in_left);
		holder.flipper.setOutAnimation(context, R.drawable.view_translation_in_right);
		
		holder.flipper.startFlipping();
		
		return view;
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
	
	
	static class ViewHolder {
		ViewFlipper flipper ;
		TextView txtTitle ;
		ImageView images[] ;
		
	}
}
