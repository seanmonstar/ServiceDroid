package com.monstarlab.servicedroid.model;

import android.annotation.TargetApi;
import android.app.backup.BackupManager;
import android.content.Context;

@TargetApi(8)
public class WrapManager {

	private BackupManager mBackupManager;
	
	static {
		try {
			Class.forName("android.app.backup.BackupManager");
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}
	
	public static void checkAvailable() {}
	
	public WrapManager(Context ctx) {
		mBackupManager = new BackupManager(ctx);
	}
	
	public void dataChanged() {
		mBackupManager.dataChanged();
	}
	
}
