package au.com.sharonblain.longhairhow2;

import java.util.ArrayList;
 
import android.app.Activity;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
 
public class GridViewImageAdapter extends BaseAdapter {
 
    private Activity _activity;
    private ArrayList<String> _filePaths = new ArrayList<String>();
    private int imageWidth;
 
    public GridViewImageAdapter(Activity activity, ArrayList<String> filePaths,
            int imageWidth) {
        this._activity = activity;
        this._filePaths = filePaths;
        this.imageWidth = imageWidth;
    }
 
    @Override
    public int getCount() {
        return this._filePaths.size();    	
    }
 
    @Override
    public Object getItem(int position) {
        return this._filePaths.get(position);
    }
 
    @Override
    public long getItemId(int position) {
        return position;
    }
 
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ImageView imageView;
        if (convertView == null) {
            imageView = new ImageView(_activity);
        } else {
            imageView = (ImageView) convertView;
        }
 
        Resources res = _activity.getResources() ;
        int imgId = res.getIdentifier(_filePaths.get(position), "drawable", _activity.getPackageName());
        
        Bitmap bm = decodeSampledBitmapFromResource(res, imgId, imageWidth, imageWidth);
        bm = SetBrightness(bm, -70) ;
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        imageView.setLayoutParams(new GridView.LayoutParams(imageWidth, imageWidth*469/420));        	
        imageView.setImageBitmap(bm) ;
        imageView.setEnabled(false) ;
        imageView.setVerticalScrollBarEnabled(false) ;
        // image view click listener
        //imageView.setOnClickListener(new OnImageClickListener(position));
 
        return imageView;
    }
    
    public Bitmap SetBrightness(Bitmap src, int value) {
        // original image size
        int width = src.getWidth();
        int height = src.getHeight();
        // create output bitmap
        Bitmap bmOut = Bitmap.createBitmap(width, height, src.getConfig());
        // color information
        int A, R, G, B;
        int pixel;
      
        // scan through all pixels
        for(int x = 0; x < width; ++x) {
            for(int y = 0; y < height; ++y) {
                // get pixel color
                pixel = src.getPixel(x, y);
                A = Color.alpha(pixel);
                R = Color.red(pixel);
                G = Color.green(pixel);
                B = Color.blue(pixel);
      
                // increase/decrease each channel
                R += value;
                if(R > 255) { R = 255; }
                else if(R < 0) { R = 0; }
      
                G += value;
                if(G > 255) { G = 255; }
                else if(G < 0) { G = 0; }
      
                B += value;
                if(B > 255) { B = 255; }
                else if(B < 0) { B = 0; }
      
                // apply new pixel color to output bitmap
                bmOut.setPixel(x, y, Color.argb(A, R, G, B));
            }
        }
      
        // return final image
        return bmOut;
    }
 
    public static int calculateInSampleSize(BitmapFactory.Options options,
            int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            // Calculate ratios of height and width to requested height and
            // width
            final int heightRatio = Math.round((float) height
                    / (float) reqHeight);
            final int widthRatio = Math.round((float) width / (float) reqWidth);

            // Choose the smallest ratio as inSampleSize value, this will
            // guarantee
            // a final image with both dimensions larger than or equal to the
            // requested height and width.
            inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
        }

        return inSampleSize;
    }

    public static Bitmap decodeSampledBitmapFromResource(Resources res,
            int resId, int reqWidth, int reqHeight) {

        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(res, resId, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth,
                reqHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeResource(res, resId, options);
    } 
    
    class OnImageClickListener implements OnClickListener {
 
        int _postion;
 
        // constructor
        public OnImageClickListener(int position) {
            this._postion = position;
        }
 
        @Override
        public void onClick(View v) {
            // on selecting grid view image
            // launch full screen activity
            //Intent i = new Intent(_activity, FullScreenViewActivity.class);
            //i.putExtra("position", _postion);
            //_activity.startActivity(i);
        }
 
    }
 
}
