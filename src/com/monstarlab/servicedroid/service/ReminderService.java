package com.monstarlab.servicedroid.service;

import java.util.Calendar;
import com.monstarlab.servicedroid.receiver.NotificationReceiver;
import com.monstarlab.servicedroid.util.TimeUtil;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class ReminderService extends Service {
	
	private TimeUtil mTimeHelper;
	
	/*@Override
	public void onStart(Intent intent, int startId) {
	    handleCommand(intent);
	}*/

	public int onStartCommand(Intent intent, int flags, int startId) {
	    return 2; // START_NOT_STICKY
	}
	
	@Override
	public void onCreate() {
		mTimeHelper = new TimeUtil(this);
		scheduleReminderNotification();
		
	}
	
	private void scheduleReminderNotification() {
		Intent intent = new Intent(this, NotificationReceiver.class);
		intent.setType(Integer.toString(NotificationReceiver.TYPE_REMINDER));
		intent.setAction(Integer.toString(NotificationReceiver.TYPE_REMINDER));

		
		AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);
		PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, intent, 0);
		
		//first day of the month, around noonish
		long time = getFirstDayOfMonth();
		
		am.set(AlarmManager.RTC_WAKEUP, time, pendingIntent);
	}
	
	private long getFirstDayOfMonth() {
		long time = 0L;
		
		Calendar c = Calendar.getInstance();
		c.set(Calendar.DATE, 1); //beginning of month
		c.add(Calendar.MONTH, 1); //next month
		c.set(Calendar.HOUR_OF_DAY, 12); //at 12 noon?
		c.set(Calendar.MINUTE, 0);
		
		time = c.getTimeInMillis();
		
		return time;
	}

	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}

}
