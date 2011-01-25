package com.monstarlab.servicedroid.util;

import com.monstarlab.servicedroid.R;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.text.TextUtils;
import android.webkit.WebView;

public class Changelog {
	
	//private static final int V_1_0 = 1;
	//private static final int V_1_1 = 2;
	//private static final int V_1_2 = 3;
	//private static final int V_1_2_1 = 4; //just bug fixes, no changelog needed
	//private static final int V_1_2_2 = 5; //just bug fixes, no changelog needed
	private static final int V_1_3 = 6; //just bug fixes, no changelog needed
	
	private static final int CURRENT_VERSION = V_1_3;

	private static final String PREFS_NAME = "Changelog";
	private static final String PREFS_CHANGELOG_VERSION = "lastSeenChangelogVersion";

	public static void showFirstTime(final Context c) {
		if (hasSeenMessage(c)) {
			// do nothing
		} else {
			final AlertDialog.Builder builder = new AlertDialog.Builder(c);
			final WebView webView = new WebView(c);

			int title = R.string.whats_new;
			
			builder.setTitle(title);
			builder.setView(webView);
			builder.setCancelable(false);
	
	        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
	            @Override
	            public void onClick(DialogInterface dialog, int which) {
	                markMessageSeen(c);
	            }
	        });
	        String message = getMessage(c);
	        if(TextUtils.isEmpty(message)) {
	        	return;
	        }

	        webView.loadDataWithBaseURL(null, message, "text/html", "UTF-8", "about:blank");
	        builder.show();
		}
	}
	
	/*private static int isNewOrUpdated(Context c) {
		SharedPreferences settings = c.getSharedPreferences(PREFS_NAME, 0);
	    int lastVersion = settings.getInt(LAST_VERSION, -1);
		
	    return lastVersion == -1 ? STATUS_NEW : STATUS_UPDATED;
	}*/
	
	/*private static int getVersion(Context c) {
		int versionCode = 0;
		try {
			//current version
	        PackageInfo packageInfo = c.getPackageManager().getPackageInfo(c.getPackageName(), 0);
	        versionCode = packageInfo.versionCode; 
		} catch (NameNotFoundException e) {
			//then just return 0
		}
		
		return versionCode;
	}*/
	
	private static boolean hasSeenMessage(Context c) {
		SharedPreferences settings = c.getSharedPreferences(PREFS_NAME, 0);
		return settings.getInt(PREFS_CHANGELOG_VERSION, 0) >= CURRENT_VERSION;
	}
	
	private static void markMessageSeen(Context c) {
		SharedPreferences settings = c.getSharedPreferences(PREFS_NAME, 0);
		Editor editor=settings.edit();
        editor.putInt(PREFS_CHANGELOG_VERSION, CURRENT_VERSION);
        editor.commit();
	}
	
	private static String getMessage(Context c) {
		StringBuilder message = new StringBuilder();
		
		String[] features = c.getResources().getStringArray(R.array.changelog);
		message.append("<html><body>");
		
		message.append("<b>v1.3</b>");
		message.append("<ul>");
		for(int i = 0; i < features.length; i++) {
			message.append("<li>" + features[i] + "</li>");
		}
		message.append("</ul>");
		
		message.append("<b>" + c.getString(R.string.enjoy_servicedroid) + "</b>");
		message.append("<ul>");
		message.append("<li>" + c.getString(R.string.give_review) + "</li>");
		message.append("</ul>");
		
		message.append("</body></html>");
		return message.toString();
	}
	
}
