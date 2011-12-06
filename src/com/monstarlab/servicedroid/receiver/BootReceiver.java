package com.monstarlab.servicedroid.receiver;

import com.monstarlab.servicedroid.service.ReminderService;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class BootReceiver extends BroadcastReceiver {

	private static final String TAG = "BootReceiver";
	
	@Override
	public void onReceive(Context context, Intent intent) {
		Log.d(TAG, "Boot received. Starting Reminder Service.");
		Intent i = new Intent(context, ReminderService.class);
		context.startService(i);
	}

}
