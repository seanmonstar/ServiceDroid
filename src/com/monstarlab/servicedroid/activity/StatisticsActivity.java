package com.monstarlab.servicedroid.activity;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.View.OnTouchListener;
import android.widget.TableLayout;
import android.widget.TextView;

import com.monstarlab.servicedroid.model.Models.BibleStudies;
import com.monstarlab.servicedroid.model.Models.Placements;
import com.monstarlab.servicedroid.model.Models.ReturnVisits;
import com.monstarlab.servicedroid.model.Models.TimeEntries;
import com.monstarlab.servicedroid.util.TimeUtil;
import com.monstarlab.servicedroid.R;

public class StatisticsActivity extends Activity implements OnTouchListener {
	
	private static final String TAG = "StatisticsActivity";
	
	private static final int MENU_MONTH = Menu.FIRST;
	private static final int MENU_YEAR = Menu.FIRST + 1;
	private static final int MENU_EMAIL = Menu.FIRST + 2;
	
	private static final int REPORT_TIME_NOTIFICATION = 1;
	
	//private TimeUtil mTimeHelper;
	
	//private static String[] CallsProjection = new String[] { Calls._ID, Calls.BIBLE_STUDY };
	private static String[] BibleStudiesProjection = new String[] { BibleStudies._ID };
	private static String[] TimeProjection = new String[] { TimeEntries._ID, TimeEntries.DATE, TimeEntries.LENGTH };
	private static String[] RVProjection = new String[] { ReturnVisits._ID, ReturnVisits.DATE, ReturnVisits.CALL_ID };
	private static String[] PlacementsProjection = new String[] { Placements._ID, Placements.DATE };
	
	private TextView mTimePeriodDisplay;
	private TextView mHoursDisplay;
	private TextView mRvsDisplay;
	private TextView mMagsDisplay;
	private TextView mBrochuresDisplay;
	private TextView mBooksDisplay;
	private TextView mBibleStudiesDisplay;
	
	private int mCurrentMonth = TimeUtil.getCurrentMonth();
	private int mCurrentYear = TimeUtil.getCurrentYear();
	//private int mTimeSpan = MENU_MONTH;
	
	
	private static final int SWIPE_MIN_DISTANCE = 120;
    private static final int SWIPE_MAX_OFF_PATH = 250;
    private static final int SWIPE_THRESHOLD_VELOCITY = 200;
    private GestureDetector gestureDetector;
	
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
        
        // Gesture detection
	    gestureDetector = new GestureDetector(new MyGestureDetector());
	    TableLayout table = (TableLayout) findViewById(R.id.statstable);
	    table.setOnTouchListener(this);
	    
	    //setup reminder...
	    //scheduleReminder();
    }
	
	
	
	@Override
	protected void onResume() {
		super.onResume();
		
		fillData();
	}



	protected void fillData() {
		mTimePeriodDisplay.setText("" + mCurrentMonth + "/" + mCurrentYear);
		mHoursDisplay.setText(getHoursSum());
		mRvsDisplay.setText(getRVs());
		mMagsDisplay.setText(getMagazines());
		mBrochuresDisplay.setText(getBrochures());
		mBooksDisplay.setText(getBooks());
		mBibleStudiesDisplay.setText(getBibleStudies());
	}
	
	protected String getBibleStudies() {
		//all Bible Studies started before next month, and not ended before this month
		String where = BibleStudies.DATE_START + "<? and (" + BibleStudies.DATE_END+" isnull or "+BibleStudies.DATE_END + ">?)";
		
		String[] args = getTimePeriodArgs(mCurrentYear, mCurrentMonth);
		String thisMonth = args[0];
		String nextMonth = args[1];
		
		String[] whereArgs = new String[] { nextMonth, thisMonth };
		Cursor c = getContentResolver().query(BibleStudies.CONTENT_URI, BibleStudiesProjection, where, whereArgs, null);
		int sum = 0;
		if(c != null) {
			c.moveToFirst();
			sum = c.getCount();
			c.close();
			c = null;
		}
		return "" + sum;
	}



	protected String getBooks() {
		Cursor c = getContentResolver().query(Placements.BOOKS_CONTENT_URI, PlacementsProjection, getTimePeriodWhere(ReturnVisits.DATE), getTimePeriodArgs(mCurrentYear, mCurrentMonth), null);
		int sum = 0;
		if(c != null) {
			sum = c.getCount();
			c.close();
			c = null;
		}
		return ""+sum;
	}



	protected String getBrochures() {
		Cursor c = getContentResolver().query(Placements.BROCHURES_CONTENT_URI, PlacementsProjection, getTimePeriodWhere(ReturnVisits.DATE), getTimePeriodArgs(mCurrentYear, mCurrentMonth), null);
		int sum = 0;
		if(c != null) {
			sum = c.getCount();
			c.close();
			c = null;
		}
		return ""+sum;
	}



	protected String getMagazines() {
		Cursor c = getContentResolver().query(Placements.MAGAZINES_CONTENT_URI, PlacementsProjection, getTimePeriodWhere(ReturnVisits.DATE), getTimePeriodArgs(mCurrentYear, mCurrentMonth), null);
		int sum = 0;
		if(c != null) {
			sum = c.getCount();
			c.close();
			c = null;
		}
		return ""+sum;
	}



	protected String getHoursSum() {
		
		Cursor c = getContentResolver().query(TimeEntries.CONTENT_URI, TimeProjection, getTimePeriodWhere(ReturnVisits.DATE), getTimePeriodArgs(mCurrentYear, mCurrentMonth), null);
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
		Cursor c = getContentResolver().query(ReturnVisits.CONTENT_URI, RVProjection, getTimePeriodWhere(ReturnVisits.DATE), getTimePeriodArgs(mCurrentYear, mCurrentMonth), null);
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
	
	protected void moveBackwardOneMonth() {
		mCurrentMonth--;
		if(mCurrentMonth <= 0) {
			mCurrentMonth = 12;
			mCurrentYear--;
		}
	}
	
	protected void moveForwardOneMonth() {
		mCurrentMonth++;
		if(mCurrentMonth >= 12) {
			mCurrentMonth = 1;
			mCurrentYear++;
		}
	}
	
	protected String getTimePeriodWhere(String dateField) {
		return "("+dateField + " between ? and ?)"; // "dateField between YYYY-MM-01 and date('YYYY-MM-01','+1 month','-1 day');"
	}
	
	protected String[] getTimePeriodArgs(int year, int month) {
		String[] args = new String[2];
		//beginning of month
		args[0] = year + "-" + TimeUtil.pad(month) + "-01";
		
		//end of month
		//TODO - possibly fix date?
		args[1] = year + "-" + TimeUtil.pad(month+1) + "-01";
		
		return args;
	}
	
	/*protected void setTimeSpan(int span) {
		mTimeSpan = span;
	}*/
	
	
	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
		boolean result = super.onCreateOptionsMenu(menu);
		//menu.add(0, MENU_MONTH, 1, R.string.monthly).setIcon(android.R.drawable.ic_menu_month);
		//menu.add(0, MENU_YEAR, 1, R.string.service_year).setIcon(android.R.drawable.ic_menu_my_calendar);
		menu.add(0, MENU_EMAIL, 1, R.string.send).setIcon(android.R.drawable.ic_menu_send);
		return result;
    }
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {		
		switch(item.getItemId()) {
		
		/*case MENU_MONTH:
			setTimeSpan(MENU_MONTH);
			break;
		case MENU_YEAR:
			setTimeSpan(MENU_YEAR);
			break;*/
		
		case MENU_EMAIL:
			sendEmail();
		
		}
		
		return super.onOptionsItemSelected(item);
	}
	
	protected void sendEmail() {
		Intent i = new Intent(Intent.ACTION_SEND, Uri.parse("content://com.android.email.provider"));
		//i.setType("text/plain"); //use this line for testing in the emulator  
		i.setType("message/rfc822"); //for device
		i.putExtra(Intent.EXTRA_EMAIL, new String[] {});  
		i.putExtra(Intent.EXTRA_SUBJECT, "Service Time for " + mCurrentMonth + "/" + mCurrentYear);  
		i.putExtra(Intent.EXTRA_TEXT, getStatsTextForTimePeriod());
		startActivity(Intent.createChooser(i, "Send by..."));
	}
	
	protected String getStatsTextForTimePeriod() {
		StringBuilder sb = new StringBuilder();
		
		//TODO - use strings.xml to allow for internationalization
		sb.append("Here is my Service Record for " + mCurrentMonth + "/" + mCurrentYear + "\n\n");
		sb.append("Hours: " + getHoursSum() + "\n");
		sb.append("Magazines: " + getMagazines() + "\n");
		sb.append("Brochures: " + getBrochures() + "\n");
		sb.append("Books: " + getBooks() + "\n");
		sb.append("Return Visits: " + getRVs() + "\n");
		sb.append("Bible Studies: " + getBibleStudies() + "\n");
		
		
		
		return sb.toString();
	}

	
	class MyGestureDetector extends SimpleOnGestureListener {
	    @Override
	    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
	        try {
	            if (Math.abs(e1.getY() - e2.getY()) > SWIPE_MAX_OFF_PATH)
	                return false;
	            // right to left swipe
	            if(e1.getX() - e2.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
	            	//left
	            	moveForwardOneMonth();
	            	fillData();
	            }  else if (e2.getX() - e1.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
	                //right
	            	moveBackwardOneMonth();
	            	fillData();
	            }
	        } catch (Exception e) {
	            // nothing
	        }
	        return false;
	    }
	    
	    @Override
	    public boolean onDown(MotionEvent event) {
	    	return true;
	    }
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
        if (gestureDetector.onTouchEvent(event)) {
            return true;
        }
        return false;
    }
	
}
