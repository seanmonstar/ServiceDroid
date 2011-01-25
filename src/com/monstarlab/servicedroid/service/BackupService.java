package com.monstarlab.servicedroid.service;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;

import com.monstarlab.servicedroid.model.BackupWorker;

import android.app.Service;
import android.content.Intent;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

public class BackupService extends Service {
	
	public static final String ACTION_BACKUP = "Backup";
	public static final String ACTION_RESTORE = "Restore";
	
	private static final String TAG = "BackupService";
	
	private static final String FILE_NAME = "ServiceDroidBackup.sdml";
	private static final String DIRECTORY = "backups";
	
	private static final long SCHEDULE_DELAY = 1000 * 60 * 5;
	
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
				
				writeToSDCard(new BackupWorker().backup(getContentResolver()));
				stopSelf();
			}
			
		}).start();
	}
	
	protected void writeToSDCard(String raw) {
		File root = Environment.getExternalStorageDirectory();
		File dir = new File(root, DIRECTORY);
		File file = new File(dir, FILE_NAME);
		
		try {
			dir.mkdirs();
			FileOutputStream os = new FileOutputStream(file);
			os.write(raw.getBytes());
			os.close();
			
		} catch (IOException e) {
			// likely because SD card is mounted, otherwise unwriteable...
			// oh well, so the backup doesn't happen *this* time. we'll live.
			Log.w(TAG, "error writing to file");
		}
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
				
				new BackupWorker().restore(getContentResolver(), readFromSDCard());
				stopSelf();
			}
			
		}).start();
	}
	
	protected String readFromSDCard() {
		File root = Environment.getExternalStorageDirectory();
		File dir = new File(root, DIRECTORY);
		File file = new File(dir, FILE_NAME);
		StringBuilder sb = new StringBuilder();
		try {
			BufferedReader in = new BufferedReader(new FileReader(file));
			String line = null;
			while ((line = in.readLine()) != null) {
				sb.append(line);
			}
			
			in.close();			
		} catch (IOException e) {
			// likely because SD card is mounted, otherwise unwriteable...
			// Restore failing, but there *might* be a restore file there,
			// should we try again a little later? much later and we risk data
			// contamination
			Log.w(TAG, "error writing to file");
		}
		return sb.toString();
	}
	
	
	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}

}
