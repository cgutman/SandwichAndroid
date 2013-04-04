package com.sandwich.player;

import com.sandwich.R;
import com.sandwich.SpinnerDialog;

import android.app.Activity;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.net.Uri;
import android.widget.MediaController;
import android.widget.VideoView;

public class VideoPlayer implements SandwichPlayer,OnErrorListener,OnPreparedListener {
	private Activity activity;
	private VideoView player;
	private SpinnerDialog waitDialog;
	
	public VideoPlayer(Activity activity)
	{
		this.activity = activity;
		this.player = (VideoView)activity.findViewById(R.id.videoView);
	}
	
	public void initialize(Uri mediaPath)
	{ 
		MediaController controller = new MediaController(activity);

		// Attach the controller to the video view
		controller.setAnchorView(player);
		
		// Setup the video player
		player.setMediaController(controller);
		player.setVideoURI(mediaPath);
		player.setOnErrorListener(this);
		player.setOnPreparedListener(this);
	}
	
	public void start()
	{
		// Enable wakelock
		player.setKeepScreenOn(true);
		
		// Start the media
		waitDialog = SpinnerDialog.displayDialog(activity, "Please Wait", "Loading Media", true);
		player.start();
	}
	
	public void stop()
	{
		// Dismiss the wait dialog
		if (waitDialog != null)
		{
			waitDialog.dismiss();
			waitDialog = null;
		}
		
		// Disable wakelock
		player.setKeepScreenOn(false);
		
		// Stop the media
		player.stopPlayback();
	}
	
	public void release()
	{
		// Release the player
		player = null;
	}
	
	@Override
	public boolean onError(MediaPlayer player, int what, int extra)
	{
		// Dismiss the wait dialog
		if (waitDialog != null)
		{
			waitDialog.dismiss();
			waitDialog = null;
		}
		
		// Close the player
		activity.finish();
		return true;
	}

	@Override
	public void onPrepared(MediaPlayer arg0)
	{
		// Dismiss the wait dialog
		if (waitDialog != null)
		{
			waitDialog.dismiss();
			waitDialog = null;
		}
	}
}
