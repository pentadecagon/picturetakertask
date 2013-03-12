package com.picturetakertask;

import com.commonsware.cwac.wakeful.WakefulIntentService;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.Toast;

/**
 * Task object that stores the parameters needed for running the task, e.g. duration & interval,
 * & triggers/ cancels/ expires the scheduled task
 */

public class Task {
	//whether or not the task is running
	public boolean isRunning = false;
	//task duration in millis
	public long duration = 0;
	//interval between repeats of the task in millis
	public long interval = 0;
	//user-defined delay before the task starts in millis
	public long delay = 0;
	
	//default duration if we don't find any in the shared preferences
	private static final int DURATION_DEFAULT_VALUE = 15000;
	//default interval if we don't find any in the shared preferences
	private static final int INTERVAL_DEFAULT_VALUE = 5000;
	//default delay if we don't find any in the shared preferences
	private static final int DELAY_DEFAULT_VALUE = 0;
	
	//filename of shared preferences file
	public static final String TASK_SETTINGS_SHARED_PREFS_NAME = "PictureTakerTaskSharedPrefs";
	
	//activity that owns the object
	private Context context;
	
    /**
     * Constructor
     * 
     * @param Context context Activity that owns the object
     */
	public Task(Context context)
	{
		this.context = context;
		setConfigFromSharedPreferences();
	}
	
    /**
     * Update the internal config from the shared preferences store on the device
     */
	public void setConfigFromSharedPreferences()
	{
		Log.d("picturetaker", "called setConfigFromSharedPreferences");
		
		//get task preferences
		SharedPreferences prefs = context.getSharedPreferences(TASK_SETTINGS_SHARED_PREFS_NAME, Context.MODE_PRIVATE);
		isRunning = prefs.getBoolean("isRunning", false);
		duration = prefs.getLong("duration", 0);
		interval = prefs.getLong("interval", 0);
		delay = prefs.getLong("delay", 0);
		
		Log.d("picturetaker", "setSharedPreferencesFromConfig: recovered preferences\nisRunning="+
				isRunning+"\nduration="+duration+"\ninterval="+interval+"\ndelay="+delay);
		
		if (duration < 1)
		{
			//set default values
			duration = DURATION_DEFAULT_VALUE;
			interval = INTERVAL_DEFAULT_VALUE;
			delay = DELAY_DEFAULT_VALUE;
		}
	}
	
    /**
     * Update the shared preferences store on the device from the interval config
     */
	public void updateSharedPreferencesFromConfig()
	{
		SharedPreferences settings = context.getSharedPreferences(TASK_SETTINGS_SHARED_PREFS_NAME, Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean("isRunning", isRunning);
        editor.putLong("duration", duration);
        editor.putLong("interval", interval);
        editor.putLong("delay", delay);
        editor.commit();
        
		Log.d("picturetaker", "updateSharedPreferences: set preferences\nisRunning="+
				isRunning+"\nduration="+duration+",\ninterval="+interval+",\ndelay="+delay);
	}
	
    /**
     * Trigger the scheduled task
     */
	public void startPictureTakingService()
	{
		Log.d("picturetaker", "called startPictureTakingService with interval="+interval);
		
		isRunning = true;
		
		updateSharedPreferencesFromConfig();
		
		WakefulIntentService.scheduleAlarms(new AppListener(interval, delay),
				context, true);
		
		Toast.makeText(context, "Picture taking task is active!!!",
		       Toast.LENGTH_LONG).show();
		
		//show notification
		NotificationHandler notificationHandler = new NotificationHandler(context);
		notificationHandler.initializeNotification();
		notificationHandler.initializeProgressBar();
	}
	
    /**
     * Cancel the scheduled task
     */
	public void cancelPictureTakingService()
	{
		Log.d("picturetaker", "called cancelPictureTakingService");
	
		//cancel notification
		if (NotificationHandler.notificationManager != null)
		{
			Log.d("picturetakertask", "Task.cancelPictureTakingService: canceling notification");
			NotificationHandler.notificationManager.cancelAll();
		}
		
		Toast.makeText(context, "Picture taking task is canceled!!!",
  				Toast.LENGTH_LONG).show();
		
		endPictureTakingService();

	}
	
    /**
     * Expire the scheduled task (for use when its duration has expired)
     */
	public void expirePictureTakingService()
	{
		Log.d("picturetaker", "called expirePictureTakingService");

		//finalize notification
		NotificationHandler notificationHandler = new NotificationHandler(context);
		notificationHandler.finalizeNotification();
		  
		Toast.makeText(context, "Picture taking task has expired!!!",
  				Toast.LENGTH_LONG).show();
		
		endPictureTakingService();
	}
	
    /**
     * End the scheduled task "silently" (without showing any notifications)
     */
	public void endPictureTakingServiceSilently()
	{
		Log.d("picturetaker", "called endPictureTakingServiceSilently");
		
		//cancel notification
		if (NotificationHandler.notificationManager != null)
		{
			Log.d("picturetakertask", "Task.endPictureTakingServiceSilently: canceling notification");
			NotificationHandler.notificationManager.cancelAll();
		}
		
		endPictureTakingService();
	}
	
    /**
     * End the scheduled task
     */
	private void endPictureTakingService()
	{
		Log.d("picturetaker", "called endPictureTakingService");
		
		isRunning = false;
		
		updateSharedPreferencesFromConfig();
		
		WakefulIntentService.cancelAlarms(context);
	}

    /**
     * Get the task's progress so far.
     * 
     * @double progress A number between 0 and 1 (0 = task has just started, 1 = task is finished)
     */
	public double getProgress()
	{
		if (duration < 1)
		{
			throw new RuntimeException("duration is 0 in Task.getProgress. This should never happen because duration should have been"+
							" have been written to the shared preferences by this point. cannot continue.");
		}
		
		SharedPreferences prefs = context.getSharedPreferences(TASK_SETTINGS_SHARED_PREFS_NAME, MainActivity.MODE_PRIVATE);
		long startTime = prefs.getLong("startTime", 0);
		if (startTime < 1)
		{
			throw new RuntimeException("startTime is 0 in Task.getProgress. This should never happen because startTime should have been"+
							" have been written to the shared preferences by this point. cannot continue.");
		}

		double progress = ((double) (System.currentTimeMillis() - startTime)/ (double) duration);
		if (progress < 0.0)
		{
			progress = 0.0;
		}
		Log.d("picturetakertask", "exiting Task.getProgress with progress="+progress);
		return progress;
	}
	
    /**
     * Get what the task's progress will be on the next iteration.
     * 
     * Useful for looking into the future and seeing if the task will have expired by the next iteration.
     * 
     * @double progress A number between 0 and 1 (0 = task has just started, 1 = task is finished)
     */
	public double getProgressAtNextIteration()
	{
		if (interval < 1)
		{
			throw new RuntimeException("interval is 0 in Task.getProgressAtNextIteration. This should never happen because interval should have been"+
							" have been written to the shared preferences by this point. cannot continue.");
		}
		
		if (duration < 1)
		{
			throw new RuntimeException("duration is 0 in Task.getProgressAtNextIteration. This should never happen because duration should have been"+
							" have been written to the shared preferences by this point. cannot continue.");
		}
		
		SharedPreferences prefs = context.getSharedPreferences(TASK_SETTINGS_SHARED_PREFS_NAME, MainActivity.MODE_PRIVATE);
		long startTime = prefs.getLong("startTime", 0);
		if (startTime < 1)
		{
			throw new RuntimeException("startTime is 0 in Task.getProgressAtNextIteration. This should never happen because startTime should have been"+
							" have been written to the shared preferences by this point. cannot continue.");
		}

		double progress =  ((double) (System.currentTimeMillis() + interval - startTime)/ (double) duration);
		if (progress < 0.0)
		{
			progress = 0.0;
		}
		Log.d("picturetakertask", "exiting Task.getProgressAtNextIteration with progress="+progress);
		return progress;
	}

}
