package com.monstarlab.servicedroid.model;

import android.app.backup.BackupManager;
import android.content.Context;

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
