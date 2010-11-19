package com.monstarlab.servicedroid.util;

import com.monstarlab.servicedroid.R;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.text.TextUtils;
import android.webkit.WebView;

public class Changelog {
	
	private static final String LAST_VERSION = "lastVersion";
	
	private static final int STATUS_NEW = 1;
	private static final int STATUS_UPDATED = 2;
	
	
	private static final int V_1_0 = 1;
	private static final int V_1_1 = 2;

	private static final String PREFS_NAME = "Changelog";

	public static void showFirstTime(Context c) {
		if (hasSeenMessage(c)) {
			// do nothing
		} else {
			final AlertDialog.Builder builder = new AlertDialog.Builder(c);
			final WebView webView = new WebView(c);

			int title = R.string.whats_new;
			
			builder.setTitle(title);
			builder.setView(webView);
	
	        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
	            @Override
	            public void onClick(DialogInterface dialog, int which) {
	                //TODO - mark message as shown
	            }
	        });
	        
	        String message = getMessage(c);
	        if(TextUtils.isEmpty(message)) {
	        	return;
	        }

	        //builder.setMessage(message);
	        webView.loadData(message, "text/html", "utf-8");
	        builder.show();
		}
	}
	
	/*private static int isNewOrUpdated(Context c) {
		SharedPreferences settings = c.getSharedPreferences(PREFS_NAME, 0);
	    int lastVersion = settings.getInt(LAST_VERSION, -1);
		
	    return lastVersion == -1 ? STATUS_NEW : STATUS_UPDATED;
	}*/
	
	private static int getVersion(Context c) {
		int versionCode = 0;
		try {
			//current version
	        PackageInfo packageInfo = c.getPackageManager().getPackageInfo(c.getPackageName(), 0);
	        versionCode = packageInfo.versionCode; 
		} catch (NameNotFoundException e) {
			//then just return 0
		}
		
		return versionCode;
	}
	
	private static boolean hasSeenMessage(Context c) {
		int versionCode = getVersion(c);
		
		if(versionCode > 0) {
			SharedPreferences settings = c.getSharedPreferences(PREFS_NAME, 0);
		    return settings.getBoolean("hasSeenChangelogVersion" + versionCode, false);
		} else {
			return true; // failed getting versionCode, show nothing
		}
	}
	
	private static String getMessage(Context c) {
		StringBuilder message = new StringBuilder();
		
		String[] features = new String[] { 
			"Start a timer from the Time view when you enter service.",
			"Switch between alphabetic sorting and since last visited sorting of calls.",
			"The list of calls shows who is a Bible Study."
		}; 
		message.append("<html><body>");
		
		message.append("<b>v1.1</b>");
		message.append("<ul>");
		for(int i = 0; i < features.length; i++) {
			message.append("<li>" + features[i] + "</li>");
		}
		message.append("</ul>");
		
		message.append("<b>Enjoy ServiceDroid?</b>");
		message.append("<ul>");
		message.append("<li>Consider giving a nice <a href=\"market://details?id=com.monstarlab.servicedroid\">review</a>.</li>");
		message.append("</ul>");
		
		message.append("</body></html>");
		return message.toString();
	}
	
}
