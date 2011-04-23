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
import android.app.DatePickerDialog;
import android.app.Dialog;
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
	
	private static final int DIALOG_DATE = 1;
	
	private static final String[] PROJECTION = new String[]{ ReturnVisits._ID, ReturnVisits.CALL_ID, ReturnVisits.DATE };
	
	private int mState;
	private Uri mUri;
	private int mCallId;
	private boolean mIsCancelled = false;
	private TimeUtil mTimeHelper;
	private Cursor mCursor;
	private Button mDateBtn;

	private String mDate;
	
	
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
			values.put(ReturnVisits.DATE, TimeUtil.getCurrentTimeSQLText());
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
		
		mDateBtn = (Button) findViewById(R.id.date);
		mDateBtn.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				showDialog(DIALOG_DATE);
			}
			
		});
		
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
				setDate(date);
				
			}
			
		} else {
			setDate(TimeUtil.getCurrentDateSQLText());
		}
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		
		if(mCursor != null) {
			
			
			if(isFinishing() &&  mIsCancelled) {
				//they pressed the delete button, so DELETE!
				setResult(RESULT_CANCELED);
				deleteEntry();
			
			
			} else {
				//save the current changes to the Provider
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
					c.close();
					c = null;
				}
				
			}
		}
	}
	
	@Override
	protected Dialog onCreateDialog(int id) {
		Dialog dialog;
		switch(id) {
	    case DIALOG_DATE:
	    	dialog = makeDateDialog();
	        break;
	    default:
	        dialog = null;
	    }
		return dialog;
	}
	
	private Dialog makeDateDialog() {
		int year = 0, month = 0, day = 0;
		if(mDate != null) {
			
			try {
				Date d = mTimeHelper.parseDateText(mDate);
				year = d.getYear() + 1900;
				month = d.getMonth();
				day = d.getDate();
			} catch (ParseException e) {
				// oh well, default to today
			}
		}
		
		if (year == 0) {
			year = TimeUtil.getCurrentYear();
			month = TimeUtil.getCurrentMonth() - 1;
			day = TimeUtil.getCurrentDay();
		}
		
		return new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
			
			@Override
			public void onDateSet(DatePicker view, int y, int m, int d) {
				String date = y + "-" + TimeUtil.pad(m) + "-" + d;
				setDate(date);
			}

		}, year, month, day);
	}

	private String getDate() {
		return mDate;
	}
	
	private void setDate(String date) {
		mDate = date;
		mDateBtn.setText(date);
	}
	
	private void deleteEntry() {
		if(mCursor != null) {
			mCursor.close();
			mCursor = null;
			getContentResolver().delete(mUri, null, null);
		}
	}
	
}
