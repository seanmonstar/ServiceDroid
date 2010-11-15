package com.monstarlab.servicedroid.util;

import com.monstarlab.servicedroid.R;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;

public class HelpDialog {
	
	
	
	public static final int PLACEMENTS = 1;
	
	private static final String PREFS_NAME = "HelpDialogs";
	
	private static final String LAST_VERSION = "lastVersion";
	
	private static final int STATUS_NEW = 1;
	private static final int STATUS_UPDATED = 2;

	public static void showFirstTime(Context c, int message) {
		if (hasSeenMessage(c, message)) {
			// do nothing
		} else {
			final AlertDialog.Builder builder = new AlertDialog.Builder(c);
	        
			int status = isNewOrUpdated(c);
			int title = status == STATUS_NEW ? R.string.whats_new : R.string.tips;
			
			builder.setTitle(title);
	
	        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
	            @Override
	            public void onClick(DialogInterface dialog, int which) {
	                
	            }
	        });
	        /*builder.setNegativeButton(R.string.DLG_decline, new DialogInterface.OnClickListener() {
	            @Override
	            public void onClick(DialogInterface dialog, int which) {
	                
	            }
	        });*/
	        builder.setMessage(getMessage(message, status));
	        builder.show();
		}
	}
	
	private static int isNewOrUpdated(Context c) {
		SharedPreferences settings = c.getSharedPreferences(PREFS_NAME, 0);
	    int lastVersion = settings.getInt(LAST_VERSION, -1);
		
	    return lastVersion == -1 ? STATUS_NEW : STATUS_UPDATED;
	}
	
	private static boolean hasSeenMessage(Context c, int message) {
		SharedPreferences settings = c.getSharedPreferences(PREFS_NAME, 0);
	    return settings.getBoolean("dialog_"+message, false);
	}
	
	private static String getMessage(int message, int status) {
		
		
		
		return "Pressing MENU will let you add placements or return visits to this call.";
	}
	
}
