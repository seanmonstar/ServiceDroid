package com.monstarlab.servicedroid.activity;

import java.text.ParseException;
import java.util.Date;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TimePicker;

import com.monstarlab.servicedroid.R;
import com.monstarlab.servicedroid.model.Models.TimeEntries;
import com.monstarlab.servicedroid.util.TimeUtil;

public class TimeEditActivity extends Activity {

	private static final String TAG = "TimeEditActivity";
	
	private static final int STATE_EDIT = 0;
	private static final int STATE_INSERT = 1;
	
	private static final int MENU_DELETE = Menu.FIRST;
	
	private static final int DIALOG_DATE = 0;
	private static final int DIALOG_LENGTH = 1;
	
	private boolean mIsCancelled;
	
	private Button mDateBtn;
	private Button mLengthBtn;
	private EditText mNoteText;

	private int mLength;
	private String mDate;
	
	private TimeUtil mTimeHelper;
	private int mState;
	private Uri mUri;

	private Cursor mCursor;
	
	private static final String[] PROJECTION = new String[] { TimeEntries._ID, TimeEntries.LENGTH, TimeEntries.DATE, TimeEntries.NOTE };
	private static final int LENGTH_COLUMN = 1;
	private static final int DATE_COLUMN = 2;
	private static final int NOTE_COLUMN = 3;

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
			v.put(TimeEntries.DATE, TimeUtil.getCurrentDateSQLText());
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
		
		this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
		setContentView(R.layout.time_edit);
		
		mTimeHelper = new TimeUtil(this);
		
		mDateBtn = (Button) findViewById(R.id.date);
		mDateBtn.setOnClickListener(new View.OnClickListener() {

			public void onClick(View view) {

				showDialog(DIALOG_DATE);
			}
			
		});
		
		mLengthBtn = (Button) findViewById(R.id.length);
		mLengthBtn.setOnClickListener(new View.OnClickListener() {

			public void onClick(View view) {

				showDialog(DIALOG_LENGTH);
			}
			
		});
		
		//mLengthText.setIs24HourView(true);
		mNoteText = (EditText) findViewById(R.id.notes);

		Button confirmButton = (Button) findViewById(R.id.confirm);
		confirmButton.setOnClickListener(new View.OnClickListener() {

			public void onClick(View view) {

				finish();
			}
			
		});
		
		Button cancelButton = (Button) findViewById(R.id.cancel);
		cancelButton.setOnClickListener(new View.OnClickListener() {
			
			

			public void onClick(View v) {
				mIsCancelled = true;
				finish();
			}
		});
		
		mCursor = managedQuery(mUri, PROJECTION, null, null, null);
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		
		//this happens on wakeup. even at startup.
		
		if(mCursor != null) {
			mCursor.moveToFirst();
			
			Integer length = mCursor.getInt(LENGTH_COLUMN);
			String date = mCursor.getString(DATE_COLUMN);
			String note = mCursor.getString(NOTE_COLUMN);
			
			setDate(date);
			setLength(length);
			
			
			mNoteText.setText(note);
		} else {
			setDate(TimeUtil.getCurrentDateSQLText());
			setLength(0);
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
			String note = getNote();
			
			boolean finished = isFinishing();
			
			if(finished && (length == 0)) {
				//when finishing, if no time was spent, just ditch the time period. its useless anyways
				setResult(RESULT_CANCELED);
				deleteEntry();
			} else if(finished && mIsCancelled) {
				// if the cancel button was pressed, don't delete, but don't save either.
				setResult(RESULT_CANCELED);
			
			} else {
				//save the current changes to the Provider
				ContentValues values = new ContentValues();
				values.put(TimeEntries.LENGTH, length);
				values.put(TimeEntries.DATE, date);
				values.put(TimeEntries.NOTE, note);
				
				getContentResolver().update(mUri, values, null, null);
				
				// set data on Intent in case the orientation has changed
				// https://github.com/seanmonstar/ServiceDroid/issues/issue/58
				Intent i = getIntent();
				i.setAction(Intent.ACTION_EDIT);
				i.setData(mUri);
			}
		}
	}
	
	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        boolean result = super.onCreateOptionsMenu(menu);
 
        menu.add(0, MENU_DELETE, 1, R.string.delete_time).setIcon(android.R.drawable.ic_menu_delete);
            
        return result;
    }
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case MENU_DELETE:
			setLength(0);
			finish();
			break;
		}
		
		return super.onOptionsItemSelected(item);
	}
	
	@Override
	protected Dialog onCreateDialog(int id) {
		Dialog dialog;
		switch(id) {
	    case DIALOG_DATE:
	    	dialog = makeDateDialog();
	        break;
	    case DIALOG_LENGTH:
	    	dialog = makeLengthDialog();
	    	break;
	    default:
	        dialog = null;
	    }
		return dialog;
	}

	private Dialog makeLengthDialog() {
		int hrs = 0, mins = 0;
		if (mLength > 0) {
			hrs = TimeUtil.getHours(mLength);
			if (hrs > 23) {
				hrs = 23;
			}
			mins = TimeUtil.getMins(mLength);
		}
		
		final Resources r = getResources();
		
		return new TimeLengthPickerDialog(this, new TimePickerDialog.OnTimeSetListener() {
			
			public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
				setLength(TimeUtil.toTimeInt(hourOfDay, minute));
			}
			
		}, hrs, mins, r);
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
			
			public void onDateSet(DatePicker view, int y, int m, int d) {
				String date = y + "-" + pad(m+1) + "-" + pad(d);
				setDate(date);
			}

		}, year, month, day);
	}

	private void deleteEntry() {
		if(mCursor != null) {
			mCursor.close();
			mCursor = null;
			getContentResolver().delete(mUri, null, null);
		}
	}
	
	private void setDate(String date) {
		mDate = date;
		mDateBtn.setText(date);
	}
	
	private void setLength(int length) {
		mLength = length;
		mLengthBtn.setText(TimeUtil.toTimeString(length, getResources()));
	}
	
	private String getDate() {
		return mDate;
	}
	
	private int getTime() {
		/*mLengthText.clearFocus();
		int hours = mLengthText.getCurrentHour();
		int mins = mLengthText.getCurrentMinute();
		return TimeUtil.toTimeInt(hours, mins);*/
		return mLength;
	}
	
	private String getNote() {
		return mNoteText.getText().toString();
	}
	
	private static String pad(int c) {
		if(c >= 10)
			return String.valueOf(c);
		else 
			return "0" + String.valueOf(c);
	}
	
	
	class TimeLengthPickerDialog extends TimePickerDialog {

		private Resources mResources;
		
		public TimeLengthPickerDialog(Context context, OnTimeSetListener callBack, int hourOfDay, int minute, boolean is24HourView) {
			super(context, callBack, hourOfDay, minute, is24HourView);
		}
		
		public TimeLengthPickerDialog(Context context, OnTimeSetListener callBack, int hourOfDay, int minute, Resources r) {
			this(context, callBack, hourOfDay, minute, true);
			mResources = r;
			updateTitle(hourOfDay, minute);
		}
		
		@Override
		public void onTimeChanged(TimePicker view, int hourOfDay, int minute) {
			updateTitle(hourOfDay, minute);
		}
		
		private void updateTitle(int h, int m) {
			this.setTitle(TimeUtil.toTimeString(TimeUtil.toTimeInt(h, m), mResources));
		}
			
		
	}

}
