package au.com.sharonblain.uservideo;

import android.media.MediaPlayer;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.net.Uri;
import android.os.Bundle;
import android.app.Activity;
import android.app.ProgressDialog;
import android.util.Log;
import android.widget.MediaController;
import android.widget.VideoView;
import au.com.sharonblain.longhairhow2.R;

public class VideoStreamActivity extends Activity{

	private ProgressDialog pDialog;
	private VideoView videoview;
	private int position = 0;
	private MediaController mediaControls;
	
	@Override
	protected void onResume() {
		videoview.resume();
	    super.onResume();
	}

	@Override
	protected void onPause() {
		videoview.suspend();
	    super.onPause();
	}

	@Override
	protected void onDestroy() {
		videoview.stopPlayback();
	    super.onDestroy();
	}
	
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stream_video) ;
        
        if (mediaControls == null) {
        	mediaControls = new MediaController(VideoStreamActivity.this);
        }

        videoview = (VideoView) findViewById(R.id.VideoView);
        pDialog = new ProgressDialog(VideoStreamActivity.this);
        
        pDialog.setTitle(getIntent().getExtras().getString("title"));
        pDialog.setMessage("Buffering...");
        pDialog.setIndeterminate(false);
        pDialog.setCancelable(false);
        pDialog.show();
 
        try {
        	videoview.setMediaController(mediaControls) ;
        	Uri video = Uri.parse(getIntent().getExtras().getString("address"));
            videoview.setVideoURI(video);
 
        } catch (Exception e) {
            Log.e("Error", e.getMessage());
            e.printStackTrace();
        }
 
        videoview.requestFocus();
        videoview.setOnPreparedListener(new OnPreparedListener() {
            
        	public void onPrepared(MediaPlayer mp) {
        		if ( (pDialog != null) && (pDialog.isShowing()) )
        		{
        			try {
        				pDialog.dismiss();
        			} catch (NullPointerException e) {
        				e.printStackTrace() ;
        			} catch (IllegalArgumentException e) {
        				e.printStackTrace() ;
        			}
        		}
                videoview.seekTo(position);

                if (position == 0) {
                	videoview.start();
                } else {
                	videoview.pause();
                }
            }
        });
        
        videoview.setOnErrorListener(new OnErrorListener() {
			
			@Override
			public boolean onError(MediaPlayer arg0, int arg1, int arg2) {
				if ( (pDialog != null) && (pDialog.isShowing()) )
        		{
        			try {
        				pDialog.dismiss();
        			} catch (NullPointerException e) {
        				e.printStackTrace() ;
        			} catch (IllegalArgumentException e) {
        				e.printStackTrace() ;
        			}
        		}
				return false;
			}
		}) ;
	}
	
	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {
		super.onSaveInstanceState(savedInstanceState);
	
		savedInstanceState.putInt("Position", videoview.getCurrentPosition());
		videoview.pause();
	
	}
	
	@Override
	public void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
	    position = savedInstanceState.getInt("Position");
	    videoview.seekTo(position);
	}	
}
