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
	private static final int DURATION_DEFAULT_VALUE = 180000;
	//default interval if we don't find any in the shared preferences
	private static final int INTERVAL_DEFAULT_VALUE = 20000;
	//default delay if we don't find any in the shared preferences
	private static final int DELAY_DEFAULT_VALUE = 1000;
	
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
	}
	
    /**
     * Update the internal config from the shared preferences store on the device
     */
	public void setConfigFromSharedPreferences()
	{
		Log.d("picturetaker", "called updateSharedPreferencesFromConfig");
		
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
	}
	
    /**
     * Cancel the scheduled task
     */
	public void cancelPictureTakingService()
	{
		Log.d("picturetaker", "called cancelPictureTakingService");
		
		isRunning = false;
		
		updateSharedPreferencesFromConfig();
		
		WakefulIntentService.cancelAlarms(context);
		  
		Toast.makeText(context, "Picture taking task is canceled!!!",
  				Toast.LENGTH_LONG).show();
	}
	
    /**
     * Expire the scheduled task (for use when its duration has expired)
     */
	public void expirePictureTakingService()
	{
		Log.d("picturetaker", "called expirePictureTakingService");
		
		isRunning = false;
		
		updateSharedPreferencesFromConfig();
		
		WakefulIntentService.cancelAlarms(context);
		  
		Toast.makeText(context, "Picture taking task has expired!!!",
  				Toast.LENGTH_LONG).show();
	}
	
    /**
     * Check if the task's duration is over
     * 
     * @return boolean
     */
	public boolean checkIfTaskHasExpired()
	{
		SharedPreferences prefs = context.getSharedPreferences(TASK_SETTINGS_SHARED_PREFS_NAME, MainActivity.MODE_PRIVATE);

		long duration = prefs.getLong("duration", 0);
		if (duration < 1)
		{
			throw new RuntimeException("duration is 0 in AppListener.checkIfTaskHasExpired. This should never happen because duration should have been"+
							" have been written to the shared preferences by this point. cannot continue.");
		}
		
		long startTime = prefs.getLong("startTime", 0);
		if (startTime < 1)
		{
			throw new RuntimeException("startTime is 0 in AppListener.checkIfTaskHasExpired. This should never happen because startTime should have been"+
							" have been written to the shared preferences by this point. cannot continue.");
		}
		
		boolean taskHasExpired = ((System.currentTimeMillis() - startTime) > duration);
		return taskHasExpired;	
	}
	
    /**
     * Check if the task will expire before the next repeat
     * 
     * @return boolean
     */
	public boolean checkIfTaskWillExpireBeforeNextIteration()
	{
		SharedPreferences prefs = context.getSharedPreferences(TASK_SETTINGS_SHARED_PREFS_NAME, MainActivity.MODE_PRIVATE);

		long interval = prefs.getLong("interval", 0);
		if (interval < 1)
		{
			throw new RuntimeException("interval is 0 in AppListener.checkIfTaskWillExpireBeforeNextIteration. This should never happen because interval should have been"+
							" have been written to the shared preferences by this point. cannot continue.");
		}
		
		long duration = prefs.getLong("duration", 0);
		if (duration < 1)
		{
			throw new RuntimeException("duration is 0 in AppListener.checkIfTaskWillExpireBeforeNextIteration. This should never happen because duration should have been"+
							" have been written to the shared preferences by this point. cannot continue.");
		}
		
		long startTime = prefs.getLong("startTime", 0);
		if (startTime < 1)
		{
			throw new RuntimeException("startTime is 0 in AppListener.checkIfTaskWillExpireBeforeNextIteration. This should never happen because startTime should have been"+
							" have been written to the shared preferences by this point. cannot continue.");
		}
		
		boolean taskWillExpire = ((System.currentTimeMillis() + interval - startTime) > duration);
		return taskWillExpire;	
	}
}
