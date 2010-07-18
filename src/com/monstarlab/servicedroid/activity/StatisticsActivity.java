package com.monstarlab.servicedroid.activity;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.monstarlab.servicedroid.model.Models.ReturnVisits;
import com.monstarlab.servicedroid.model.Models.TimeEntries;
import com.monstarlab.servicedroid.util.TimeUtil;
import com.monstarlab.servicedroid.R;

public class StatisticsActivity extends Activity {
	
	private static final int MENU_MONTH = Menu.FIRST;
	private static final int MENU_YEAR = Menu.FIRST + 1;
	private static final int MENU_EMAIL = Menu.FIRST + 2;
	
	//private TimeUtil mTimeHelper;
	
	private static String[] TimeProjection = new String[] { TimeEntries._ID, TimeEntries.DATE, TimeEntries.LENGTH };
	private static String[] RVProjection = new String[] { ReturnVisits._ID, ReturnVisits.DATE, ReturnVisits.CALL_ID };
	
	private TextView mTimePeriodDisplay;
	private TextView mHoursDisplay;
	private TextView mRvsDisplay;
	private TextView mMagsDisplay;
	private TextView mBrochuresDisplay;
	private TextView mBooksDisplay;
	private TextView mBibleStudiesDisplay;
	
	private String mCurrentTimePeriod = "07/2010"; //TODO - should be changeable
	private int mTimeSpan = MENU_MONTH;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.stats);
        
       // mTimeHelper = new TimeUtil(this);
        mTimePeriodDisplay = (TextView)findViewById(R.id.stats_timeperiod);
        mHoursDisplay = (TextView)findViewById(R.id.hours);
        mRvsDisplay = (TextView)findViewById(R.id.rvs);
        mMagsDisplay = (TextView)findViewById(R.id.magazines);
        mBrochuresDisplay = (TextView)findViewById(R.id.brochures);
        mBooksDisplay = (TextView)findViewById(R.id.books);
        mBibleStudiesDisplay = (TextView)findViewById(R.id.bible_studies);
    }
	
	
	
	@Override
	protected void onResume() {
		super.onResume();
		
		fillData();
	}



	protected void fillData() {
		mTimePeriodDisplay.setText(mCurrentTimePeriod);
		mHoursDisplay.setText(getHoursSum());
		mRvsDisplay.setText(getRVs());
		mMagsDisplay.setText(getMagazines());
		mBrochuresDisplay.setText(getBrochures());
		mBooksDisplay.setText(getBooks());
		mBibleStudiesDisplay.setText(getBibleStudies());
	}
	
	protected String getBibleStudies() {
		// TODO Auto-generated method stub
		return "N/A";
	}



	protected String getBooks() {
		// TODO Auto-generated method stub
		return "N/A";
	}



	protected String getBrochures() {
		// TODO Auto-generated method stub
		return "N/A";
	}



	protected String getMagazines() {
		// TODO Auto-generated method stub
		return "N/A";
	}



	protected String getHoursSum() {
		Cursor c = getContentResolver().query(TimeEntries.CONTENT_URI, TimeProjection, inTimePeriod(mCurrentTimePeriod, TimeEntries.DATE), null, null); //TODO - pass in a month value
		int sum = 0;
		if(c != null) {
			c.moveToFirst();
			while(!c.isAfterLast()) {
				sum += c.getInt(2);
				c.moveToNext(); 
			}
			c.close();
			c = null;
		}
		return TimeUtil.toTimeString(sum);
	}
	
	protected String getRVs() {
		Cursor c = getContentResolver().query(ReturnVisits.CONTENT_URI, RVProjection, inTimePeriod(mCurrentTimePeriod, ReturnVisits.DATE), null, null); //TODO - pass in month value
		String numOfRVs = "0";
		if(c != null) {
			c.moveToFirst();
			numOfRVs = "" + c.getCount();
			c.close();
			c = null;
		}
		
		return numOfRVs;
	}
	
	protected void setTimePeriod() {
		
	}
	
	protected String inTimePeriod(String period, String dateField) {
		return dateField + "";
	}
	
	protected void setTimeSpan(int span) {
		mTimeSpan = span;
	}
	
	
	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
		boolean result = super.onCreateOptionsMenu(menu);
		menu.add(0, MENU_MONTH, 1, R.string.monthly).setIcon(android.R.drawable.ic_menu_month);
		menu.add(0, MENU_YEAR, 1, R.string.service_year).setIcon(android.R.drawable.ic_menu_my_calendar);
		menu.add(0, MENU_EMAIL, 1, R.string.send).setIcon(android.R.drawable.ic_menu_send);
		return result;
    }
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {		
		switch(item.getItemId()) {
		
		case MENU_MONTH:
			setTimeSpan(MENU_MONTH);
			break;
		case MENU_YEAR:
			setTimeSpan(MENU_YEAR);
			break;
		
		case MENU_EMAIL:
			sendEmail();
		
		}
		
		return super.onOptionsItemSelected(item);
	}
	
	protected void sendEmail() {
		Intent i = new Intent(Intent.ACTION_SEND, Uri.parse("content://com.android.email.provider"));
		i.setType("text/plain"); //use this line for testing in the emulator  
		//i.setType("message/rfc822"); //for device
		i.putExtra(Intent.EXTRA_EMAIL, new String[] {});  
		i.putExtra(Intent.EXTRA_SUBJECT, "Service Time for " + mCurrentTimePeriod);  
		i.putExtra(Intent.EXTRA_TEXT, "body goes here");
		startActivity(Intent.createChooser(i, "Send by..."));
	}
	
}
