package com.monstarlab.servicedroid.service;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;

import com.monstarlab.servicedroid.R;
import com.monstarlab.servicedroid.activity.ServiceDroidActivity;
import com.monstarlab.servicedroid.activity.StatisticsActivity;
import com.monstarlab.servicedroid.model.BackupWorker;
import com.monstarlab.servicedroid.util.TimeUtil;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

public class BackupService extends Service {
	
	public static final String ACTION_BACKUP = "Backup";
	public static final String ACTION_BACKUP_IMMEDIATELY = "BackupImmediately";
	public static final String ACTION_RESTORE = "Restore";
	
	private static final String TAG = "BackupService";
	
	private static final String FILE_NAME = "ServiceDroidBackup.sdml";
	private static final String DIRECTORY = "backups";
	
	private static final long SCHEDULE_DELAY = 1000 * 60 * 5;
	
	private static final boolean DO_NOTIFY = true;
	private static final boolean DONT_NOTIFY = false;
	
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
		if (intent == null) {
			Log.e(TAG, "Intent was null. Need an intent to determine backup or restore action.");
			stopSelf();
			return;
		}
		
		final String action = intent.getAction();
		if (ACTION_BACKUP.equals(action)) {
			onBackup();
		} else if (ACTION_BACKUP_IMMEDIATELY.equals(action)) {
			backupImmediately();
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
		doBackup(DONT_NOTIFY);
	}
	
	protected void doBackup(final boolean notifyOnSuccess) {
		
		//actually doing the backup should be in a separate thread, so we don't bog down the UI at all
		new Thread(new Runnable() {
			
			public void run() {
				//find backup file on SD card
				//write SDML to backup file, overwriting if need be
				writeToSDCard(new BackupWorker().backup(getContentResolver()));
				
				if (notifyOnSuccess) {
					notifySuccess();
				}
				
				stopSelf();
			}
			
		}).start();
	}
	
	protected void backupImmediately() {
		doBackup(DO_NOTIFY);
	}
	
	protected void notifySuccess() {
		final Context ctx = getApplicationContext();
		final String text = getString(R.string.backup_success, "/" + DIRECTORY + "/" + FILE_NAME);
		mHandler.post(new Runnable() {

			@Override
			public void run() {
				Toast.makeText(ctx, text, Toast.LENGTH_LONG).show();
			}
			
		});
		
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
				
				boolean success = new BackupWorker().restore(getContentResolver(), readFromSDCard());
				if (!success) {
					notifyRestoreFailure();
				}
				stopSelf();
			}
			
		}).start();
	}
	
	protected void notifyRestoreFailure() {
		final Context ctx = getApplicationContext();
		final String text = getString(R.string.restore_failure);
		mHandler.post(new Runnable() {

			@Override
			public void run() {
				NotificationManager nm = (NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);
				
				CharSequence tickerText = "Restore failed";
				long when = TimeUtil.getCurrentTime();
				
				Notification notification = new Notification(R.drawable.icon, tickerText, when);
				Intent intent = new Intent(ctx, ServiceDroidActivity.class);
				PendingIntent pendingIntent = PendingIntent.getActivity(ctx, 0, intent, 0);
				
				CharSequence contentText = text;
				
				notification.setLatestEventInfo(ctx, tickerText, contentText, pendingIntent);
				
				notification.flags |= Notification.FLAG_AUTO_CANCEL;
				
				nm.notify(2, notification);
			}
			
		});
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
