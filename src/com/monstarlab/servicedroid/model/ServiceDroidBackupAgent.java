package com.monstarlab.servicedroid.model;

import java.io.IOException;

import android.app.backup.BackupAgent;
import android.app.backup.BackupDataInput;
import android.app.backup.BackupDataOutput;
import android.os.ParcelFileDescriptor;
import android.util.Log;

public class ServiceDroidBackupAgent extends BackupAgent {

	@Override
	public void onBackup(ParcelFileDescriptor oldState, BackupDataOutput data, ParcelFileDescriptor newState) throws IOException {
		Log.i("backup agent", "POOOP");
		//determine if backup should happen based on oldState
		boolean doBackup = (oldState == null); //no old state means first time backing up
		if (!doBackup) {
			//is an old state, so find out if old state is the same as current state
			doBackup = true;
		}
		
		//do backup
		if (doBackup) {
			BackupWorker bw = new BackupWorker();
			String serializedData = bw.backup(getContentResolver());
			
		}
		
		//record a newState
	}

	@Override
	public void onRestore(BackupDataInput data, int appVersionCode, ParcelFileDescriptor newState) throws IOException {
		// TODO Auto-generated method stub

	}

}
