package com.monstarlab.servicedroid.activity;

import java.text.ParseException;
import java.util.Date;

import com.monstarlab.servicedroid.R;
import com.monstarlab.servicedroid.model.Models.Calls;
import com.monstarlab.servicedroid.model.Models.Literature;
import com.monstarlab.servicedroid.model.Models.Placements;
import com.monstarlab.servicedroid.model.Models.ReturnVisits;
import com.monstarlab.servicedroid.model.Models.TimeEntries;
import com.monstarlab.servicedroid.util.TimeUtil;

import android.app.Activity;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.Toast;

public class ReturnVisitActivity extends Activity {
	
	private static final String TAG = "CallShowActivity";
	private static final int STATE_INSERT = 1;
	private static final int STATE_EDIT = 2;
	private static final String[] PROJECTION = new String[]{ ReturnVisits._ID, ReturnVisits.CALL_ID, ReturnVisits.DATE };
	private int mState;
	private Uri mUri;
	private int mCallId;
	private boolean mIsCancelled = false;
	private TimeUtil mTimeHelper;
	private Cursor mCursor;
	private DatePicker mDatePicker;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		mTimeHelper = new TimeUtil(this);
		
		//load in the values, if editting
		final Intent intent = getIntent();
		final String action = intent.getAction();
		if (Intent.ACTION_EDIT.equals(action)) {
			mState = STATE_EDIT;
			mUri = intent.getData();			
		} else if (Intent.ACTION_INSERT.equals(action)) {
			mState = STATE_INSERT;
			mCallId = intent.getIntExtra(Calls._ID, 0);
			if(mCallId == 0) {
				Log.e(TAG, "Call ID wasn't passed, exiting");
				finish();
				return;
			}
			ContentValues values = new ContentValues();
			values.put(ReturnVisits.CALL_ID, mCallId);
			mUri = getContentResolver().insert(intent.getData(), values);
			
			if(mUri == null) {
				Log.e(TAG, "Failed to insert a blank row into " + getIntent().getData());
				finish();
				return;
			}
			
			setResult(RESULT_OK, (new Intent()).setAction(mUri.toString()));
		} else {
			mState = -1;
			Log.e(TAG, "Unknown action, exiting");
			finish();
			return;
		}
		
		setContentView(R.layout.return_visit);
		
		mDatePicker = (DatePicker) findViewById(R.id.date);
		
		Button confirm = (Button) findViewById(R.id.confirm);
		confirm.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View view) {
				finish();
			}
			
		});
		
		Button cancel = (Button) findViewById(R.id.cancel);
		cancel.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View view) {
				mIsCancelled = true;
				finish();
			}
		});
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		mCursor = managedQuery(mUri, PROJECTION, null, null, null);
		if(mCursor != null) {
			
			//grab date
			if(mCursor.getCount() > 0) {
				mCursor.moveToFirst();
				String date = mCursor.getString(2);
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
				
				mDatePicker.updateDate(year, month, day);
			}
			
		}
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		
		if(mCursor != null) {
			
			//when finishing, if no publication was picked, just ditch the whole thing. its useless anyways
			if(isFinishing() &&  mIsCancelled) {
				setResult(RESULT_CANCELED);
				deleteEntry();
			
			//save the current changes to the Provider
			} else {
				ContentValues values = new ContentValues();
				values.put(ReturnVisits.DATE, getDate());
				getContentResolver().update(mUri, values, null, null);
				
				// make a toast if creating a new Return Visit
				if(mState == STATE_INSERT) {
					Cursor c = getContentResolver().query(ContentUris.withAppendedId(Calls.CONTENT_URI, mCallId), new String[]{ Calls._ID, Calls.NAME }, null, null, null);
					if (c.getCount() > 0) {
						c.moveToFirst();
						String name = c.getString(1);
						String text = getString(R.string.return_visit_success, name);
						Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT).show();
					}
					
				}
				
			}
		}
	}
	
	private String getDate() {
		return new StringBuilder()
			.append(mDatePicker.getYear())
			.append("-")
			.append(TimeUtil.pad(mDatePicker.getMonth() + 1))
			.append("-")
			.append(TimeUtil.pad(mDatePicker.getDayOfMonth()))
			.toString();
	}
	
	private void deleteEntry() {
		if(mCursor != null) {
			mCursor.close();
			mCursor = null;
			getContentResolver().delete(mUri, null, null);
		}
	}
	
}
