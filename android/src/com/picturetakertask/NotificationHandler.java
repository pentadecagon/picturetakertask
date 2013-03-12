package com.picturetakertask;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;

/**
 * Handles all notifications.
 */

public class NotificationHandler {

	//the notification manager
	public static NotificationManager notificationManager = null;
	
	//the builder, which we use to build the notification
	private static NotificationCompat.Builder mBuilder = null;
	
	//an arbitrary, but unique, notification ID
	private static final int notificationId = 9898;
	
	//context used to create this notification handler
	private Context ctxt = null;
	
	/**
	 * Constructor
	 */
	public NotificationHandler(Context ctxt)
	{
		this.ctxt = ctxt;
		
		if (notificationManager == null)
		{
			notificationManager =
				    (NotificationManager) ctxt.getSystemService(Context.NOTIFICATION_SERVICE);
		}
	}
	
	/**
	 * Prepare (but don't show, yet) the notification
	 */
	public void initializeNotification()
	{
		mBuilder = new NotificationCompat.Builder(ctxt);
		Log.d("picturetakertask", "called NotificationHandler.initializeNotification");
		//add main body of notification, whose click leads to the main action
		mBuilder.setSmallIcon(R.drawable.clock)
		        .setContentTitle("Picture Taker Task")
		        .setContentText("Running...");
		
		//add a "stop task" sub-action
		Intent stopTaskIntent = new Intent(ctxt, CancelActivity.class);
		PendingIntent stopTaskPendingIntent = PendingIntent.getActivity(ctxt, 0, stopTaskIntent, 0);
		mBuilder.addAction(R.drawable.cancel, "stop task", stopTaskPendingIntent);
	}
	
	/**
	 * Get a navigation-adjusted pending intent from an original intent.
	 * 
	 * The pending intent is adjusted from the original intent so that the navigation will
	 * behave as expected (clicking the back button takes user back to the launcher screen).
	 * 
	 * @param Intent intent The original intent
	 * @param Class intentClass Activity class associated with the original intent
	 * 
	 * @return PendingIntent pendingIntent Pending intent with navigation adjusted
	 */
	private PendingIntent getPendingIntentFromIntent(Intent intent, Class intentClass)
	{
		TaskStackBuilder stackBuilder = TaskStackBuilder.create(ctxt);
		// Adds the back stack for the Intent (but not the Intent itself)
		stackBuilder.addParentStack(intentClass);
		// Adds the Intent that starts the Activity to the top of the stack
		stackBuilder.addNextIntent(intent);
		PendingIntent pendingIntent =
		        stackBuilder.getPendingIntent(
		            0,
		            PendingIntent.FLAG_UPDATE_CURRENT
		        );
		return pendingIntent;
	}
	
	/**
	 * Initialize the progress bar that will be displayed on the notification
	 */
	public void initializeProgressBar()
	{
		Log.d("picturetakertask", "called NotificationHandler.initializeProgressBar");
		mBuilder.setProgress(100, 0, false);
		Notification notif =  mBuilder.build();
		//set the default sound for the notification
		notif.defaults |= Notification.DEFAULT_SOUND;
		notificationManager.notify(notificationId, notif);
	}
	
	/**
	 * Update the progress bar that will be displayed on the notification
	 */
	public void updateProgressBar(int progress)
	{
		Log.d("picturetakertask", "called NotificationHandler.updateProgressBar");
		mBuilder.setProgress(100, progress, false);
		notificationManager.notify(notificationId, mBuilder.build());	
	}
	
	/**
	 * Adjust the notification to its final state when the task has finished
	 */
	public void finalizeNotification()
	{
		// When the progress bar is finished, update the notification
		mBuilder = new NotificationCompat.Builder(ctxt);
		mBuilder.setSmallIcon(R.drawable.clock)
        .setContentTitle("Picture Taker Task")
        .setContentText("Done! Click to view pics.");

        //update the onclick intent of the notification to lead to the gallery album where the pictures are stored
        Intent galleryIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        PendingIntent galleryPendingIntent = getPendingIntentFromIntent(galleryIntent, MainActivity.class);
        mBuilder.setContentIntent(galleryPendingIntent);
        //set intent to be cancelled as soon as the user clicks on it
        mBuilder.setAutoCancel(true);
        //display the updated notification
        Notification notif =  mBuilder.build();
        //set the default sound for the notification
		notif.defaults |= Notification.DEFAULT_SOUND;
        notificationManager.notify(notificationId, notif);   
	}
	
}
