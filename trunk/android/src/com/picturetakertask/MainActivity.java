package com.picturetakertask;

import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.commonsware.cwac.wakeful.AlarmReceiver;

/**
 * Start off the action.
 */

public class MainActivity extends FragmentActivity {

	//task object that stores the parameters needed for running the task, e.g. duration & interval,
	//& triggers the scheduled task
	private Task task;

	//layout to be displayed when the task is running
	RelativeLayout runningLayout;
	//layout to be displayed when the task is not running
	RelativeLayout readyLayout;
	
	//edit duration field
	private TimeInputText editDuration;
	//edit interval field
	private TimeInputText editInterval;
	//edit delay field
	private TimeInputText editDelay;
	
	private PreviewWindow mPreview = null;

	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.main);
	    
	    initHandlesAndListeners();

	}
  
	@Override
	protected void onResume() {
		super.onResume();
		Log.d("picturetaker", "called MainActivity.onResume");

		//add preview window, which activates camera
		mPreview = new PreviewWindow(this);
		FrameLayout preview = (FrameLayout) findViewById(R.id.preview_window);
        preview.addView(mPreview);
		
		//task.setConfigFromSharedPreferences();
        task = new Task(this);
        
		//double check that the task status is as expected
		checkTaskStatusAndAdjustIfNecessary();

		updateUi();
		
		//populate the form
		populateForm();
		
	}

	@Override
	protected void onPause() {
		
		//remove preview window, which deactivates camera
		FrameLayout preview = (FrameLayout) findViewById(R.id.preview_window);
        preview.removeView(mPreview);
        mPreview = null;
		
		super.onPause();
		Log.d("picturetaker", "called MainActivity.onPause");
	}
	
	/**
	 * Initialize the handles and listeners used by this activity
	 */
	private void initHandlesAndListeners()
	{
		runningLayout = (RelativeLayout) findViewById (R.id.running_layout);
		readyLayout = (RelativeLayout) findViewById (R.id.ready_layout);
		
		//create handles to the input fields that set intervals
		editDuration = (TimeInputText) findViewById(R.id.edit_duration);
		editInterval = (TimeInputText) findViewById(R.id.edit_interval);
		editDelay = (TimeInputText) findViewById(R.id.edit_delay);
		
		// Add a listener to the start button
		Button startButton = (Button) findViewById(R.id.button_start);
		startButton.setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						task.duration = editDuration.timeInterval.getTimeInMillis();
						task.interval = editInterval.timeInterval.getTimeInMillis();
						task.delay = editDelay.timeInterval.getTimeInMillis();

						//start the task
						task.startPictureTakingService();
						
						//finish the activity
						finish();
					}
				}
			);
		    
		// Add a listener to the cancel button
		Button stopButton = (Button) findViewById(R.id.button_cancel);
		stopButton.setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick(View v) {

						//stop the task
						task.cancelPictureTakingService();
						
						//update the UI to reflect the fact that the task has stopped
						updateUi();
					}
				}
			);

	}

	/**
	 * Double-checks task status for anomalies.
	 * 
	 * When we start up, check the value of "isRunning" (whether the task is running or not) from the
	 * shared preferences and check whether the task really is running/ not running (e.g. maybe it was
	 * stopped unexpectedly) and update the shared preferences if necessary
	 */
	private void checkTaskStatusAndAdjustIfNecessary()
	{
		Log.d("picturetaker", "called checkTaskStatusAndAdjustIfNecessary");

		//double check that the task status is correct
		long now = System.currentTimeMillis();
		SharedPreferences lastAlarmPrefs = this.getSharedPreferences("com.commonsware.cwac.wakeful.WakefulIntentService", MODE_PRIVATE);
	    long lastAlarm = lastAlarmPrefs.getLong("lastAlarm", 0);
	    
	    //if task is supposed to be running but the last alarm was a suspiciously long time ago, stop the current task and set status to not running
	    if (task.isRunning && ((now - lastAlarm) > (AppListener.MIN_DELAY + task.delay + 2.0*task.interval)))
	    {
	    	Log.d("picturetaker", "checkTaskStatusAndAdjustIfNecessary: last alarm was suspiciously long ago so stopping task");
	    	task.endPictureTakingServiceSilently();
	    }
	    
	    //check if we can detect the running task
	    Intent i=new Intent(this, AlarmReceiver.class);
	    PendingIntent pi = PendingIntent.getBroadcast(this, 0, i, PendingIntent.FLAG_NO_CREATE);
	    
	    if (!task.isRunning && pi != null)
	    {
	    	Log.d("picturetaker", "checkTaskStatusAndAdjustIfNecessary: task is running when it should not be so stopping task");
	    	//if task is supposed to be deactivated but we can detect it, cancel it
	    	task.endPictureTakingServiceSilently();
	    } else if (task.isRunning && pi == null)
	    {
	    	//if task is supposed to be running but we cannot detect it, update the config
	    	Log.d("picturetaker", "checkTaskStatusAndAdjustIfNecessary: task is supposed to be running but we cannot detect it");
	    	task.isRunning = false;
	    	task.updateSharedPreferencesFromConfig();
	    }
	}
	
	/**
	 * Populate the form.
	 * 
	 * Populate the form where the user can set up a scheduled task, and the non-editable "info" version that is shown
	 * whenever the task is running
	 */
	private void populateForm()
	{
		//form fields that are shown when task is not running
		TimeInterval durationT = new TimeInterval(task.duration);
		TimeInterval intervalT = new TimeInterval(task.interval);
		TimeInterval delayT = new TimeInterval(task.delay);
		editDuration.setValue(durationT);
		editInterval.setValue(intervalT);
		editDelay.setValue(delayT);
		
		//info fields that are shown when the task is running
		((TextView) findViewById (R.id.info_duration)).setText(durationT.format());			
		((TextView) findViewById (R.id.info_interval)).setText(intervalT.format());
		((TextView) findViewById (R.id.info_delay)).setText(delayT.format());
	}

	/**
	 * Update the UI to reflect the task status
	 */
	private void updateUi()
	{
		if (task.isRunning)
		{
			runningLayout.setVisibility(View.VISIBLE);
			readyLayout.setVisibility(View.GONE);
		} else
		{
			runningLayout.setVisibility(View.GONE);
			readyLayout.setVisibility(View.VISIBLE);
		}
	}
}
