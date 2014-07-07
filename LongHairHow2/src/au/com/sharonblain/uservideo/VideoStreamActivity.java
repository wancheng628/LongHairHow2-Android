package au.com.sharonblain.uservideo;

import android.media.MediaPlayer;
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
	
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stream_video) ;
        
        videoview = (VideoView) findViewById(R.id.VideoView);
        pDialog = new ProgressDialog(VideoStreamActivity.this);
        
        pDialog.setTitle(getIntent().getExtras().getString("title"));
        pDialog.setMessage("Buffering...");
        pDialog.setIndeterminate(false);
        pDialog.setCancelable(false);
        pDialog.show();
 
        try {
            // Start the MediaController
            MediaController mediacontroller = new MediaController(
            		VideoStreamActivity.this);
            mediacontroller.setAnchorView(videoview);
            // Get the URL from String VideoURL
            Uri video = Uri.parse(getIntent().getExtras().getString("address"));
            videoview.setMediaController(mediacontroller);
            videoview.setVideoURI(video);
 
        } catch (Exception e) {
            Log.e("Error", e.getMessage());
            e.printStackTrace();
        }
 
        videoview.requestFocus();
        videoview.setOnPreparedListener(new OnPreparedListener() {
            // Close the progress bar and play the video
            public void onPrepared(MediaPlayer mp) {
                pDialog.dismiss();
                videoview.start();
            }
        });
	}
	
}
