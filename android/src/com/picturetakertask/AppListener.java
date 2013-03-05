package com.picturetakertask;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.os.SystemClock;
import com.commonsware.cwac.wakeful.WakefulIntentService;

/**
 * This schedules the task to occur at regular intervals. 
 * 
 * Based on WakfulIntentServiceCopyright (c) 2009-11 CommonsWare, LLC,
 * licensed under the Apache License, Version 2.0
 */


public class AppListener implements WakefulIntentService.AlarmListener {
  public void scheduleAlarms(AlarmManager mgr, PendingIntent pi,
                             Context ctxt) {
    mgr.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                            SystemClock.elapsedRealtime()+10000,
                            15000, pi);
  }

  public void sendWakefulWork(Context ctxt) {
    WakefulIntentService.sendWakefulWork(ctxt, AppService.class);
  }

  public long getMaxAge() {
	  return(150000);
  }
}
