package com.monstarlab.servicedroid.model;

import java.io.IOException;

import android.app.backup.BackupAgent;
import android.app.backup.BackupDataInput;
import android.app.backup.BackupDataOutput;
import android.os.ParcelFileDescriptor;

public class ServiceDroidBackupAgent extends BackupAgent {

	@Override
	public void onBackup(ParcelFileDescriptor arg0, BackupDataOutput arg1,
			ParcelFileDescriptor arg2) throws IOException {
		// TODO Auto-generated method stub

	}

	@Override
	public void onRestore(BackupDataInput arg0, int arg1,
			ParcelFileDescriptor arg2) throws IOException {
		// TODO Auto-generated method stub

	}

}
