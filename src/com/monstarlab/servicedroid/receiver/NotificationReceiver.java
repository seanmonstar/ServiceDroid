package com.monstarlab.servicedroid.receiver;

import com.monstarlab.servicedroid.R;
import com.monstarlab.servicedroid.activity.StatisticsActivity;
import com.monstarlab.servicedroid.util.TimeUtil;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class NotificationReceiver extends BroadcastReceiver {

	public static final int TYPE_REMINDER = 0;

	@Override
	public void onReceive(Context context, Intent intent) {
		switch(Integer.parseInt(intent.getType())) {
		
		case TYPE_REMINDER:
			showReminder(context);
		default:
			break;
		
		}

	}

	private void showReminder(Context context) {
		NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		
		CharSequence tickerText = context.getString(R.string.send_reminder); //TODO - pull from R.string;
		long when = TimeUtil.getCurrentTime();
		
		Notification notification = new Notification(R.drawable.icon, tickerText, when);
		Intent intent = new Intent(context, StatisticsActivity.class);
		PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
		
		CharSequence contentText = context.getString(R.string.time_due);
		
		notification.setLatestEventInfo(context, tickerText, contentText, pendingIntent);
		
		notification.flags |= Notification.FLAG_AUTO_CANCEL;
		
		nm.notify(TYPE_REMINDER, notification);
	}

}
