package com.monstarlab.servicedroid.service;

import com.monstarlab.servicedroid.model.BackupWorker;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;

public class BackupService extends Service {
	
	public static final String ACTION_BACKUP = "Backup";
	public static final String ACTION_RESTORE = "Restore";
	
	private static final long SCHEDULE_DELAY = 1000 * 60 * 2;
	
	private Handler mHandler = new Handler();
	private Runnable mScheduler = new Runnable() {
		public void run() {
			doBackup();
		}
	};
	
	@Override
	public void onCreate() {
		
	}
	
	@Override
	public void onStart(Intent intent, int startId) {
		handleCommand(intent);
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
	    handleCommand(intent);
		return 1; // START_STICKY
	}
	
	protected void handleCommand(Intent intent) {
		final String action = intent.getAction();
		if (ACTION_BACKUP.equals(action)) {
			onBackup();
		} else if (ACTION_RESTORE.equals(action)) {
			onRestore();
		}
	}
	
	protected void onBackup() {
		//schedule a backup for shortly in the future, allowing for any more DB writes
		//to keep pushing the backup forward
		mHandler.removeCallbacks(mScheduler);
		mHandler.postDelayed(mScheduler, SCHEDULE_DELAY);
	}
	
	protected void doBackup() {
		
		//actually doing the backup should be in a separate thread, so we don't bog down the UI at all
		new Thread(new Runnable() {
			
			public void run() {
				//find backup file on SD card
				//write SDML to backup file, overwriting if need be
				
				new BackupWorker().backup(getContentResolver());
				stopSelf();
			}
			
		}).start();
	}
	
	protected void onRestore() {
		// no need to schedule Restore, in fact we want it done ASAP
		doRestore();
	}
	
	protected void doRestore() {
		
		//actually doing the restore should be in a separate thread, so we don't bog down the UI at all
		new Thread(new Runnable() {
			
			public void run() {
				//find backup file on SD card
				
				//read SDML from backup file
				
				//new BackupWorker().restore(getContentResolver(), sdml);
				stopSelf();
			}
			
		});
	}
	
	
	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}

}
