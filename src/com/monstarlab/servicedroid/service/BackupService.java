package com.monstarlab.servicedroid.service;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Result;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.drive.Contents;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveApi;
import com.google.android.gms.drive.MetadataChangeSet;
import com.monstarlab.servicedroid.R;
import com.monstarlab.servicedroid.activity.ServiceDroidActivity;
import com.monstarlab.servicedroid.model.BackupWorker;
import com.monstarlab.servicedroid.util.TimeUtil;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

public class BackupService extends Service implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
	
	public static final String ACTION_BACKUP = "Backup";
	public static final String ACTION_BACKUP_IMMEDIATELY = "BackupImmediately";
	public static final String ACTION_RESTORE = "Restore";
	
	private static final String TAG = "BackupService";
	
	private static final String FILE_NAME = "ServiceDroidBackup.sdml";
	private static final String DIRECTORY = "backups";
	
	private static final long SCHEDULE_DELAY = 1000 * 60 * 5;
	
	private static final boolean DO_NOTIFY = true;
	private static final boolean DONT_NOTIFY = false;

    private GoogleApiClient mGoogleApiClient;
    protected static final int RESOLVE_CONNECTION_REQUEST_CODE = 1;

    private static boolean USE_DRIVE = false;

	private Handler mHandler = new Handler();
	private Runnable mScheduler = new Runnable() {
		public void run() {
			doBackup();
		}
	};
	
	@Override
	public void onCreate() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Drive.API)
                .addScope(Drive.SCOPE_APPFOLDER)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
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
        mGoogleApiClient.connect();
		
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
				if (USE_DRIVE) {

                } else {
                    backupToSDCard(notifyOnSuccess);
                }
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

			public void run() {
				Toast.makeText(ctx, text, Toast.LENGTH_LONG).show();
			}
			
		});
		
	}

    protected void backupToSDCard(boolean notify) {
        //find backup file on SD card
        //write SDML to backup file, overwriting if need be
        writeToSDCard(new BackupWorker().backup(getContentResolver()));

        if (notify) {
            notifySuccess();
        }

        stopSelf();
    }

    protected void backupToGoogleDrive(boolean notify) {
        writeToGoogleDrive(new BackupWorker().backup(getContentResolver()));


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

    protected void writeToGoogleDrive(final String raw) {
        Drive.DriveApi.newContents(mGoogleApiClient).setResultCallback(new ResultCallback<DriveApi.ContentsResult>() {
            @Override
            public void onResult(DriveApi.ContentsResult contentsResult) {
                Contents contents = contentsResult.getContents();

                try {
                    contents.getOutputStream().write(raw.getBytes());
                } catch (IOException e) {
                    Log.e(TAG, "writing contents failed", e);
                    return;
                }
                MetadataChangeSet changeSet = new MetadataChangeSet.Builder()
                        .setTitle("ServiceDroidBackup.sdbackup")
                        .setMimeType("text/plain")
                        .build();
                Drive.DriveApi.getAppFolder(mGoogleApiClient)
                        .createFile(mGoogleApiClient, changeSet, contents)
                        .setResultCallback(writeCallback);
            }
        });

    }

    private ResultCallback writeCallback = new ResultCallback() {
        @Override
        public void onResult(Result result) {
            if (result.getStatus().isSuccess()) {
                Log.i(TAG, "BACKUP COMPLETE");
            } else {
                Log.e(TAG, "BACKUP EXPLODED");
            }
            stopSelf();
        }
    };
	
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
			Log.e(TAG, "error reading ServiceDroidBackup.sdml");
		}
		return sb.toString();
	}

    @Override
    public void onConnected(Bundle connectionHint) {
        USE_DRIVE = true;
        Log.i(TAG, "GoogleApiClient onConnected");
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

        if (connectionResult.hasResolution()) {
            Log.w(TAG, "Google Drive failed, but there is resolution: " + connectionResult);
        } else {
            //GooglePlayServicesUtil.getErrorDialog(connectionResult.getErrorCode(), this, 0).show();
            Log.e(TAG, "Google Drive failed! " + connectionResult);
        }
    }

    @Override
    public void onConnectionSuspended(int cause) {
        Log.i(TAG, "GoogleApiClient connection suspended");
    }
	
	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}

}
