package com.monstarlab.servicedroid.service;

import java.util.Calendar;
import com.monstarlab.servicedroid.receiver.NotificationReceiver;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

public class ReminderService extends Service {
	
	private static final String TAG = "ReminderService";
	
	/*@Override
	public void onStart(Intent intent, int startId) {
	    handleCommand(intent);
	}*/

	public int onStartCommand(Intent intent, int flags, int startId) {
	    return 2; // START_NOT_STICKY
	}
	
	@Override
	public void onCreate() {
		scheduleReminderNotification();
		stopSelf();
	}
	
	private void scheduleReminderNotification() {
		Intent intent = new Intent(this, NotificationReceiver.class);
		intent.setType(Integer.toString(NotificationReceiver.TYPE_REMINDER));
		intent.setAction(Integer.toString(NotificationReceiver.TYPE_REMINDER));

		
		AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);
		PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, intent, 0);
		
		//first day of the month, around 5pm
		long time = getFirstDayOfMonth();

		am.set(AlarmManager.RTC_WAKEUP, time, pendingIntent);
	}
	
	private long getFirstDayOfMonth() {
		long time = 0L;
		
		Calendar c = Calendar.getInstance();
		
		
		if ((c.get(Calendar.DATE) == 1) && (c.get(Calendar.HOUR_OF_DAY) < 17)) {
			//if its the 1st, schedule for today
		} else {
			//else schedule for next month
			c.add(Calendar.MONTH, 1); //next month
			c.set(Calendar.DATE, 1); //beginning of month
		}
		
		c.set(Calendar.HOUR_OF_DAY, 17); //at 5pm
		c.set(Calendar.MINUTE, 0);
		
		time = c.getTimeInMillis();
		
		return time;
	}

	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}

}
