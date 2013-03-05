package com.picturetakertask;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.commonsware.cwac.wakeful.WakefulIntentService;

/**
 * Start off the action.
 */

public class MainActivity extends Activity {

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.main);
    
    // Add a listener to the Capture button
    Button startButton = (Button) findViewById(R.id.button_start);
    startButton.setOnClickListener(
            new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                	startPictureTakingService();
                }
            }
        );
    
    Button stopButton = (Button) findViewById(R.id.button_cancel);
    stopButton.setOnClickListener(
            new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                	stopPictureTakingService();
                }
            }
        );

  }
  
  private void startPictureTakingService()
  {
	  WakefulIntentService.scheduleAlarms(new AppListener(),
              this, false);
		
		Toast.makeText(this, "Picture taking task is active!!!",
		       Toast.LENGTH_LONG).show();
		
		//finish the activity
		finish();
  }
  
  private void stopPictureTakingService()
  {
	  WakefulIntentService.cancelAlarms(this);
	  
	  Toast.makeText(this, "Picture taking task is canceled!!!",
		       Toast.LENGTH_LONG).show();
  }
}
