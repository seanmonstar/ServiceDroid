package com.monstarlab.servicedroid.service;

import com.monstarlab.servicedroid.R;
import com.monstarlab.servicedroid.activity.TimeActivity;
import com.monstarlab.servicedroid.model.Models.TimeEntries;
import com.monstarlab.servicedroid.util.TimeUtil;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;

public class TimerService extends Service {

	private static final int SHOW_TIMER_NOTIFICATION = 21;
	private TimeUtil mTimeHelper;
	private Handler mHandler = new Handler();;
	private long mStartTime;
	private long mRunTime = 0L;
	
	private Notification mNotification;
	
	public static boolean isRunning = false;

	/*@Override
	public void onStart(Intent intent, int startId) {
	    handleCommand(intent);
	}*/

	public int onStartCommand(Intent intent, int flags, int startId) {
	    return 1; // START_STICKY
	}
	
	@Override
	public void onCreate() {
		isRunning = true;
		mTimeHelper = new TimeUtil(this);
		
		//show a notification
		createNotification();
		showTimerNotification();
		//create a timer
		createTimer();
		
		//update timer with time
	}
	
	@Override
	public void onDestroy() {
		removeTimer();
		((NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE)).cancel(SHOW_TIMER_NOTIFICATION);
		
		saveTime();
		isRunning = false;
	}
	
	private void createNotification() {
		int icon = R.drawable.icon;    // icon from resources
		CharSequence tickerText = "Service Timer Active";              // ticker-text
		long when = System.currentTimeMillis();         // notification time
		
		mNotification = new Notification(icon, tickerText, when);
	}
	
	private void showTimerNotification() {
		
		CharSequence contentText = "Time in service: " + TimeUtil.toTimeString((int) mRunTime);      // expanded message text
		CharSequence contentTitle = "ServiceDroid";  // expanded message title
		Intent notificationIntent = new Intent(this, TimeActivity.class);
		PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);

		// the next two lines initialize the Notification, using the configurations above
		
		
		mNotification.flags |= Notification.FLAG_ONGOING_EVENT;
		mNotification.flags |= Notification.FLAG_NO_CLEAR;
		
		mNotification.setLatestEventInfo(getApplicationContext(), contentTitle, contentText, contentIntent);
		
		((NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE)).notify(SHOW_TIMER_NOTIFICATION, mNotification);
	}
	
		
	private void createTimer() {
		if (mStartTime == 0L) {
            mStartTime = System.currentTimeMillis();
            mHandler.removeCallbacks(mUpdateTimeTask);
            mHandler.postDelayed(mUpdateTimeTask, 100);
       }
	}
	
	private void removeTimer() {
		mHandler.removeCallbacks(mUpdateTimeTask);
	}
	
	private void saveTime() {
		
		if(mRunTime >= 60) {
			ContentValues values = new ContentValues();
			values.put(TimeEntries.LENGTH, mRunTime);
			values.put(TimeEntries.DATE, TimeUtil.getCurrentTimeSQLText());
			getContentResolver().insert(TimeEntries.CONTENT_URI, values);
		}
		
	}
	
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
	
	private Runnable mUpdateTimeTask = new Runnable() {
		public void run() {
			final long start = mStartTime;
			int newTime = (int)( System.currentTimeMillis() - start) / 1000;
			
			if(mRunTime + 60 <= newTime) {
				//every 60 seconds, update notification
				mRunTime = newTime;
				showTimerNotification();
			} 
			
			mHandler.postDelayed(this, 1000);
		}
	};

}
