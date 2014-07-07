package au.com.sharonblain.search;

import java.util.List;

import com.squareup.picasso.Picasso;

import android.app.Activity;
import android.content.Context;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import au.com.sharonblain.longhairhow2.R;

public class BunImageListAdapter extends BaseAdapter {

	private Activity _context ;
	private List<BunImageArray> searchList ;
	
	public BunImageListAdapter(Activity context, List<BunImageArray> _list)
	{
		this._context = context ;
		this.searchList = _list ;
	}
	
    @Override
    public int getCount() {
        return searchList.size();
    }

    @Override
    public BunImageArray getItem(int arg0) {
        return searchList.get(arg0);
    }

    @Override
    public long getItemId(int arg0) {
        return arg0;
    }

    @Override
    public View getView(int arg0, View arg1, ViewGroup arg2) {

    	View temp = arg1 ;
    	
    	LayoutInflater inflater = (LayoutInflater) _context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    	ViewHolder holder ;
    	
        if( temp == null )
        {
        	temp = inflater.inflate(R.layout.item_search, arg2,false);
            
        	holder = new ViewHolder() ;
            holder.name = (TextView)temp.findViewById(R.id.label_search_name);
            holder.description = (TextView)temp.findViewById(R.id.label_search_description);
            holder.photo = (ImageView)temp.findViewById(R.id.img_search_photo) ;            
            temp.setTag(holder) ;
            
        } else {
			holder = (ViewHolder) temp.getTag() ;
		}
		
        BunImageArray chapter = searchList.get(arg0);

        holder.name.setText(chapter.getName());
        holder.description.setText(chapter.getDescription());
        
        DisplayMetrics displaymetrics = new DisplayMetrics();
        _context.getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
        int height = displaymetrics.heightPixels;
        
        //ImageLoader p = new ImageLoader(_context) ;
 		//p.DisplayImage(chapter.getImageUrl(), holder.photo) ;
 		Picasso.with(_context).load(chapter.getImageUrl()).resize(height/10, height/10).into(holder.photo) ;
        return temp ;
        
    }
    
    static class ViewHolder {
    	  TextView name;
    	  TextView description;
    	  ImageView photo;
    	  int position;
    }

} 