package com.picturetakertask;

import android.app.Activity;
import android.os.Bundle;

/**
 * Cancels the activity.
 * 
 * This activity is supposed to be called from the notification.
 * Note: this activity is completely invisible (as defined in the android manifest).
 */

public class CancelActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}
	
	@Override
	protected void onResume() {
		super.onResume();

		//cancel the task
		Task task = new Task(this);
		task.cancelPictureTakingService();
		
		//finish the activity
		finish();
	}

}