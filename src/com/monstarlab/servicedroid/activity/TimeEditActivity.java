package com.monstarlab.servicedroid.activity;

import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TimePicker;

import com.monstarlab.servicedroid.model.TimeEntryAdapter;
import com.monstarlab.servicedroid.util.TimeUtil;
import com.monstarlab.servicedroid.R;

public class TimeEditActivity extends Activity {

	private DatePicker mDateText;
	private TimePicker mLengthText;
	private Long mRowId;
	
	private TimeUtil mTimeHelper;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.time_edit);
		
		mDateText = (DatePicker) findViewById(R.id.date);
		mLengthText = (TimePicker) findViewById(R.id.length);
		mLengthText.setIs24HourView(true);
		
		Button confirmButton = (Button) findViewById(R.id.confirm);
		
		mTimeHelper = new TimeUtil(this);
		
		//load in the values, if editting
		mRowId = null;		
		Bundle extras = getIntent().getExtras();
		if(extras != null) {
			Integer length = extras.getInt(TimeEntryAdapter.KEY_LENGTH);
			String date = extras.getString(TimeEntryAdapter.KEY_DATE);
			mRowId = extras.getLong(TimeEntryAdapter.KEY_ID);
			
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
					Calendar c = Calendar.getInstance();
					year = c.get(Calendar.YEAR);
					month = c.get(Calendar.MONTH);
					day = c.get(Calendar.DATE);
				}
				mDateText.updateDate(year, month, day);
			}
		}
		
		confirmButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View view) {
				Bundle bundle = new Bundle();
				
				bundle.putString(TimeEntryAdapter.KEY_DATE, getDate());
				bundle.putInt(TimeEntryAdapter.KEY_LENGTH, getTime());
				if(mRowId != null) {
					bundle.putLong(TimeEntryAdapter.KEY_ID, mRowId);
				}
				
				Intent rIntent = new Intent();
				rIntent.putExtras(bundle);
				setResult(RESULT_OK, rIntent);
				finish();
			}
			
		});
	}
	
	public String getDate() {
		return new StringBuilder()
			.append(mDateText.getYear())
			.append("-")
			.append(pad(mDateText.getMonth() + 1))
			.append("-")
			.append(mDateText.getDayOfMonth())
			.toString();
	}
	
	public int getTime() {
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
