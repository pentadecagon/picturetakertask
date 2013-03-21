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
        Intent galleryIntent = new Intent();
        galleryIntent.setType("image/*");
        galleryIntent.setAction(Intent.ACTION_PICK);
        galleryIntent.addCategory(Intent.CATEGORY_DEFAULT);
        PendingIntent galleryPendingIntent =  PendingIntent.getActivity(ctxt, 0, galleryIntent, 0);
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
