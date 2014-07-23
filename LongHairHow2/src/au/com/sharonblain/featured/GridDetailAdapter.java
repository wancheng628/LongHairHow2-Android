package au.com.sharonblain.featured;

import ImageCache.ImageLoader;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import au.com.sharonblain.longhairhow2.R;
import au.com.sharonblain.request_server.GlobalVariable;

public class GridDetailAdapter extends BaseAdapter {
	private Context context;
	private final String[] image_urls ;
	private final String[] titles ;
	
	public GridDetailAdapter(Context context, String[] titles, String[] urls) {
		this.context = context;
		this.image_urls = urls;
		this.titles = titles;
	}
 
	@Override
	public int getCount() {
		return image_urls.length;
	}
 
	@Override
	public Object getItem(int position) {
		return null;
	}
 
	@Override
	public long getItemId(int position) {
		return 0;
	}

	@Override
	public View getView(int arg0, View arg1, ViewGroup arg2) {
		View gridView;
		ImageView img_photo ;
		
		if (arg1 == null) {
	 		LayoutInflater inflater = (LayoutInflater) context
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	 		gridView = inflater.inflate(R.layout.item_photo_detail, null);
	 		
	 	} else {
			gridView = (View) arg1;
		}
		
		TextView title = (TextView)gridView.findViewById(R.id.label_detail_title) ;
 		title.setText(titles[arg0]) ;
 		title.setTypeface(GlobalVariable.tf_light) ;
 		
 		img_photo = (ImageView)gridView.findViewById(R.id.img_detail_photo) ;
 		
 		ImageLoader p = new ImageLoader(context) ;
 		p.DisplayImage(image_urls[arg0], img_photo) ;
 		
		int nWidth = arg2.getWidth() / 3 ;
 		
		img_photo = (ImageView)gridView.findViewById(R.id.img_detail_photo) ;
 		img_photo.getLayoutParams().width = nWidth ;
 		img_photo.getLayoutParams().height = nWidth ;
 		
		return gridView;			
	} 
}