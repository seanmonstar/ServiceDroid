package com.monstarlab.servicedroid.activity;

import java.text.ParseException;
import java.util.Date;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.MenuItem;
import com.monstarlab.servicedroid.R;
import com.monstarlab.servicedroid.model.Models.Calls;
import com.monstarlab.servicedroid.model.Models.ReturnVisits;
import com.monstarlab.servicedroid.util.TimeUtil;

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
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Toast;

public class ReturnVisitActivity extends SherlockActivity {
	
	private static final String TAG = "CallShowActivity";
	
	private static final int STATE_INSERT = 1;
	private static final int STATE_EDIT = 2;
	
	private static final int DIALOG_DATE = 1;
	
	private static final String[] PROJECTION = new String[]{ ReturnVisits._ID, ReturnVisits.CALL_ID, ReturnVisits.DATE, ReturnVisits.IS_BIBLE_STUDY, ReturnVisits.NOTE };
	
	private int mState;
	private Uri mUri;
	private int mCallId;
	private boolean mIsCancelled = false;
	private TimeUtil mTimeHelper;
	private Cursor mCursor;
	private Button mDateBtn;
	private CheckBox mBibleStudyCheckbox;
	private EditText mNotesText;
	
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
			mCallId = intent.getIntExtra(Calls._ID, 0);
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
			values.put(ReturnVisits.DATE, TimeUtil.getCurrentDateSQLText());
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
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		
		mDateBtn = (Button) findViewById(R.id.date);
		mDateBtn.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
				showDialog(DIALOG_DATE);
			}
			
		});
		
		mBibleStudyCheckbox = (CheckBox) findViewById(R.id.is_bible_study);
		mNotesText = (EditText) findViewById(R.id.notes);
		
		Button confirm = (Button) findViewById(R.id.confirm);
		confirm.setOnClickListener(new View.OnClickListener() {

			public void onClick(View view) {
				finish();
			}
			
		});
		
		Button cancel = (Button) findViewById(R.id.cancel);
		cancel.setOnClickListener(new View.OnClickListener() {

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
				setDate(mCursor.getString(2));
				mBibleStudyCheckbox.setChecked(mCursor.getInt(3) == 1);
				mNotesText.setText(mCursor.getString(4));
				
				
			}
			
		} else {
			setDate(TimeUtil.getCurrentDateSQLText());
		}
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		
		if(mCursor != null) {
			
			
			if(isFinishing() && (mState == STATE_INSERT) && mIsCancelled) {
				//they pressed the Cancel button when this a new visit, so delete
				setResult(RESULT_CANCELED);
				deleteEntry();
			
			} else if (isFinishing() && mIsCancelled) {
				//they pressed the Cancel button for editing a visit, just dont update
				setResult(RESULT_CANCELED);
			} else {
				//save the current changes to the Provider
				ContentValues values = new ContentValues();
				values.put(ReturnVisits.DATE, getDate());
				values.put(ReturnVisits.IS_BIBLE_STUDY, mBibleStudyCheckbox.isChecked());
				values.put(ReturnVisits.NOTE, mNotesText.getText().toString());
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
				
				// set data on Intent in case the orientation has changed
				// https://github.com/seanmonstar/ServiceDroid/issues/issue/58
				Intent i = getIntent();
				i.setAction(Intent.ACTION_EDIT);
				i.setData(mUri);
				
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
			
			public void onDateSet(DatePicker view, int y, int m, int d) {
				String date = y + "-" + TimeUtil.pad(m+1) + "-" + TimeUtil.pad(d);
				setDate(date);
			}

		}, year, month, day);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			Intent i = new Intent(this, CallShowActivity.class);
			i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			Uri uri =  ContentUris.withAppendedId(Calls.CONTENT_URI, mCallId);
			i.setData(uri);
			startActivity(i);
			return true;
		}
		
		return super.onOptionsItemSelected(item);
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
