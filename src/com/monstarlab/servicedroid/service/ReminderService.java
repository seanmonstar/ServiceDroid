package com.monstarlab.servicedroid.service;

import java.text.ParseException;
import java.util.Date;

import com.monstarlab.servicedroid.receiver.NotificationReceiver;
import com.monstarlab.servicedroid.util.TimeUtil;

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
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
		int month = TimeUtil.getCurrentMonth();
		int day = TimeUtil.getCurrentDay();
		int year = TimeUtil.getCurrentYear();
		
		if(day > 1) {
			month++;
			day = 1;
		}
		
		if(month > 12) {
			month = 1;
			year++;
		}
		
		Date d = null;
		long time = 0;
		try {
			d = mTimeHelper.parseDateText(""+year+"-"+TimeUtil.pad(month)+"-"+TimeUtil.pad(day));
			time = d.getTime();
		} catch (ParseException ex) {
			time = TimeUtil.getCurrentTime();
		}
		
		return time;
	}

	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}

}
