package com.picturetakertask;

/**
 * This does the work every time the phone wakes up.
 * 
 * Based on WakfulIntentServiceCopyright (c) 2009-11 CommonsWare, LLC,
 * licensed under the Apache License, Version 2.0
 */

import android.content.Intent;
import android.util.Log;

import com.commonsware.cwac.wakeful.WakefulIntentService;

public class AppService extends WakefulIntentService {

  public AppService() {
    super("AppService");
    Log.d("camera", "called AppService constructor");
    
  }
  
  @Override
  protected void doWakefulWork(Intent intent) {
    Log.i("AppService", "called AppService.doWakefulWork");
    Intent i = new Intent(this, AutoTakePictureActivity.class);
    i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
	startActivity(i);
  }
}
