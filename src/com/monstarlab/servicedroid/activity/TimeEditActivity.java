package com.monstarlab.servicedroid.activity;

import java.text.ParseException;
import java.util.Date;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TimePicker;

import com.monstarlab.servicedroid.R;
import com.monstarlab.servicedroid.model.Models.TimeEntries;
import com.monstarlab.servicedroid.util.TimeUtil;

public class TimeEditActivity extends Activity {

	private static final String TAG = "TimeEditActivity";
	
	private static final int STATE_EDIT = 0;
	private static final int STATE_INSERT = 1;
	
	private DatePicker mDateText;
	private TimePicker mLengthText;
	private Long mRowId;
	
	private TimeUtil mTimeHelper;
	private int mState;
	private Uri mUri;

	private Cursor mCursor;
	
	private static final String[] PROJECTION = new String[] { TimeEntries._ID, TimeEntries.LENGTH, TimeEntries.DATE };
	private static final int LENGTH_COLUMN = 1;
	private static final int DATE_COLUMN = 2;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		//load in the values, if editting
		final Intent intent = getIntent();
		final String action = intent.getAction();
		if(Intent.ACTION_EDIT.equals(action)) {
			mState = STATE_EDIT;
			mUri = intent.getData();
		} else if (Intent.ACTION_INSERT.equals(action)) {
			mState = STATE_INSERT;
			ContentValues v = new ContentValues();
			v.put(TimeEntries.DATE, TimeUtil.getCurrentTimeSQLText());
			mUri = getContentResolver().insert(intent.getData(), v);
			
			if(mUri == null) {
				Log.e(TAG, "Failed to insert a blank row into " + getIntent().getData());
				finish();
				return;
			}
			
			setResult(RESULT_OK, (new Intent()).setAction(mUri.toString()));
		} else {
			Log.e(TAG, "Unknown action, exiting");
			finish();
			return;
		}
		
		
		setContentView(R.layout.time_edit);
		
		mDateText = (DatePicker) findViewById(R.id.date);
		mLengthText = (TimePicker) findViewById(R.id.length);
		mLengthText.setIs24HourView(true);
		
		Button confirmButton = (Button) findViewById(R.id.confirm);
		
		mTimeHelper = new TimeUtil(this);
		
		
		mCursor = managedQuery(mUri, PROJECTION, null, null, null);
		
		confirmButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View view) {

				finish();
			}
			
		});
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		
		//this happens on wakeup. even at startup.
		
		if(mCursor != null) {
			mCursor.moveToFirst();
			
			Integer length = mCursor.getInt(LENGTH_COLUMN);
			String date = mCursor.getString(DATE_COLUMN);
			//mRowId = extras.getLong(TimeEntryAdapter.KEY_ID);
			
			if(length != null) {
				mLengthText.setCurrentHour(TimeUtil.getHours(length));
				mLengthText.setCurrentMinute(TimeUtil.getMins(length));
			} else {
				mLengthText.setCurrentHour(0);
				mLengthText.setCurrentMinute(0);
			}
			
			if(date != null) {
				int year, month, day;
				try {
					Date d = mTimeHelper.parseDateText(date);
					year = d.getYear() + 1900;
					month = d.getMonth();
					day = d.getDate();
				} catch (ParseException e) {
					e.printStackTrace();
					year = TimeUtil.getCurrentYear();
					month = TimeUtil.getCurrentMonth() - 1;
					day = TimeUtil.getCurrentDay();
				}
				mDateText.updateDate(year, month, day);
			}
		} else {
			mLengthText.setCurrentHour(0);
			mLengthText.setCurrentMinute(0);
		}
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		//this happens whenever the Activity is losing focus, like hitting the Home button,
		//or when the user has clicked Confirm
		
		
		if(mCursor != null) {
			int length = getTime();
			String date = getDate();
			
			//when finishing, if no time was spent, just ditch the time period. its useless anyways
			if(isFinishing() && length == 0) {
				setResult(RESULT_CANCELED);
				deleteEntry();
			
			//save the current changes to the Provider
			} else {
				ContentValues values = new ContentValues();
				values.put(TimeEntries.LENGTH, length);
				values.put(TimeEntries.DATE, date);
				
				getContentResolver().update(mUri, values, null, null);
			}
		}
	}
	
	private void deleteEntry() {
		if(mCursor != null) {
			mCursor.close();
			mCursor = null;
			getContentResolver().delete(mUri, null, null);
		}
	}
	
	private String getDate() {
		return new StringBuilder()
			.append(mDateText.getYear())
			.append("-")
			.append(pad(mDateText.getMonth() + 1))
			.append("-")
			.append(pad(mDateText.getDayOfMonth()))
			.toString();
	}
	
	private int getTime() {
		int hours = mLengthText.getCurrentHour();
		int mins = mLengthText.getCurrentMinute();
		return TimeUtil.toTimeInt(hours, mins);
	}
	
	private static String pad(int c) {
		if(c >= 10)
			return String.valueOf(c);
		else 
			return "0" + String.valueOf(c);
	}
	
	

}
