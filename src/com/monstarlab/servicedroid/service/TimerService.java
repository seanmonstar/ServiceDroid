package com.monstarlab.servicedroid.service;

import com.monstarlab.servicedroid.util.TimeUtil;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.SystemClock;

public class TimerService extends Service {

	private TimeUtil mTimeHelper;
	private Handler mHandler;
	private long mStartTime;

	/*@Override
	public void onStart(Intent intent, int startId) {
	    handleCommand(intent);
	}*/

	public int onStartCommand(Intent intent, int flags, int startId) {
	    return 1; // START_STICKY
	}
	
	public void onCreate() {
		mTimeHelper = new TimeUtil(this);
		
		//show a notification
		showTimerNotification();
		//create a timer
		
		
		//update timer with time
	}
	
	private void showTimerNotification() {
		
	}
	
	private void createTimer() {
		
	}
	
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
	
	private Runnable mUpdateTimeTask = new Runnable() {
		public void run() {
			final long start = mStartTime;
			long millis = SystemClock.uptimeMillis() - start;
			

			

			mHandler.postAtTime(this, millis + 1000);
		}
	};

}
