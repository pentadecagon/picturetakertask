package com.picturetakertask;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.commonsware.cwac.wakeful.WakefulIntentService;

/**
 * This schedules the task to occur at regular intervals. 
 * 
 * Based on WakfulIntentService, copyright (c) 2009-11 CommonsWare, LLC,
 * licensed under the Apache License, Version 2.0
 */


public class AppListener implements WakefulIntentService.AlarmListener {

	public long interval = 0;
	public long delay = 0;
	
	public static final long MIN_DELAY = 1500;
	
    /**
     * Basic constructor: will be called from scheduled task
     */
	public AppListener()
	{
		Log.d("camera", "called AppListener basic constructor");
	}
	
    /**
     * Constructor
     * 
     * @param long interval The interval between repeats of the task in millis
     * @param long delay The user-defined delay before the task starts in millis
     */
	public AppListener(long interval, long delay)
	{
		Log.d("camera", "called AppListener constructor with interval="+interval);
		
		this.interval = interval;
		this.delay = delay;
	}

    /**
     * Schedules the repeating task
     */
	public void scheduleAlarms(AlarmManager mgr, PendingIntent pi,
                             Context ctxt) {
		Log.d("camera", "called scheduleAlarms with interval="+interval);

		if (interval < 1)
		{
			throw new RuntimeException("interval is 0 in AppListener.scheduleAlarms. This should never happen because interval should"+
					" be passed through the constructor AppListener(long interval) and set as a class variable. cannot continue.");
		}
		//use a basic delay of MIN_DELAY so the activity has time to shut down properly
		long startTime = System.currentTimeMillis()+MIN_DELAY+delay;
		//write start time to shared prefs
		SharedPreferences settings = ctxt.getSharedPreferences(Task.TASK_SETTINGS_SHARED_PREFS_NAME, MainActivity.MODE_PRIVATE);
		SharedPreferences.Editor editor = settings.edit();
        editor.putLong("startTime", startTime);
        editor.commit();
		
		mgr.setRepeating(AlarmManager.RTC_WAKEUP,
							startTime,
                            interval, pi);
	}

    /**
     * Triggers the actual work done by the repeating task
     */
	public void sendWakefulWork(Context ctxt) {
		Log.d("camera", "AppListener: called sendWakefulWork");

		 Task task = new Task(ctxt);
		 task.setConfigFromSharedPreferences();
		//check if task has expired
		if (task.checkIfTaskHasExpired())
		{
			//end the task
			Log.d("camera", "AppListener: task has expired so going to end it");
			task.expirePictureTakingService();
			
		} else
		{
			//continue the task
			Log.d("camera", "AppListener: task has not expired yet so going to continue it");
			WakefulIntentService.sendWakefulWork(ctxt, AppService.class);
		}
	}

    /**
     * Gives max time period for task to have stopped before it is rescheduled
     * 
     * Required by base class but not used in this implementation
     */
	public long getMaxAge() {
		if (interval < 1)
		{
			throw new RuntimeException("interval is 0 in AppListener.getMaxAge. This should never happen because interval should"+
					" be passed through the constructor AppListener(long interval) and set as a class variable. cannot continue.");
		}
		Log.d("camera", "AppListener: getMaxAge returning "+(2 * interval));
		return(2 * interval);
	}
	

}
