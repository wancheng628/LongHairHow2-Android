package au.com.sharonblain.uservideo;

import com.squareup.picasso.Picasso;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import au.com.sharonblain.longhairhow2.R;
import au.com.sharonblain.request_server.GlobalVariable;
import au.com.sharonblain.uservideo.UserVideoActivity.UserVideoItem;

public class VideoAdapter extends BaseAdapter {
	private Context context;
	private final UserVideoItem[] videoItems;
 
	public VideoAdapter(Context context, UserVideoItem[] values) {
		this.context = context;
		this.videoItems = values;
	}
 
	public View getView(int position, View convertView, ViewGroup parent) {
 
		LayoutInflater inflater = (LayoutInflater) context
			.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 
		View gridView;
 
		if (convertView == null) {
 			gridView = new View(context);
 			gridView = inflater.inflate(R.layout.item_uservideo, parent, false);			
		} else {
			gridView = (View) convertView;
		}
 
		TextView title = (TextView) gridView.findViewById(R.id.label_video_title);
		ImageView photo = (ImageView) gridView.findViewById(R.id.img_video_photo);
		
		title.setTypeface(GlobalVariable.tf_light) ;
		try {
			title.setText(videoItems[position].vid_title) ;
			Picasso.with(context).load(Uri.parse(videoItems[position].vid_image)).resize(300, 300).into(photo) ;
		} catch (NullPointerException ex) { 
		    System.out.println(String.valueOf(position) + ":" + String.valueOf(videoItems.length)) ; 
		}
		
		int nWidth = parent.getWidth() / 2 ;
 		
		photo.getLayoutParams().width = nWidth ;
		photo.getLayoutParams().height = nWidth ;
		
		return gridView;
	}
 
	@Override
	public int getCount() {
		return videoItems.length;
	}
 
	@Override
	public Object getItem(int position) {
		return null;
	}
 
	@Override
	public long getItemId(int position) {
		return 0;
	}
 
}
