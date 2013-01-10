package com.monstarlab.servicedroid.fragment;

import java.util.Calendar;
import java.util.Date;

import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.widget.ShareActionProvider;
import com.monstarlab.servicedroid.R;
import com.monstarlab.servicedroid.activity.ServiceDroidActivity;
import com.monstarlab.servicedroid.model.Models.Literature;
import com.monstarlab.servicedroid.model.Models.Placements;
import com.monstarlab.servicedroid.model.Models.ReturnVisits;
import com.monstarlab.servicedroid.model.Models.TimeEntries;
import com.monstarlab.servicedroid.service.BackupService;
import com.monstarlab.servicedroid.util.TimeUtil;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

public class StatisticsFragment extends SherlockFragment {
	
	private static final String TAG = "StatisticsActivity";
	
	private static final int TIME_PERIOD_MONTH = 0;
	private static final int TIME_PERIOD_YEAR = 1;
	
	//private TimeUtil mTimeHelper;
	
	//private static String[] CallsProjection = new String[] { Calls._ID, Calls.BIBLE_STUDY };
	private static String[] BibleStudiesProjection = new String[] { ReturnVisits._ID, ReturnVisits.DATE, ReturnVisits.IS_BIBLE_STUDY, ReturnVisits.CALL_ID };
	private static String[] TimeProjection = new String[] { TimeEntries._ID, TimeEntries.DATE, TimeEntries.LENGTH };
	private static String[] RVProjection = new String[] { ReturnVisits._ID, ReturnVisits.DATE, ReturnVisits.CALL_ID };
	private static String[] PlacementsProjection = new String[] { Placements._ID, Placements.DATE, Literature.WEIGHT };
	
	private TextView mTimePeriodDisplay;
	private TextView mHoursDisplay;
	private TextView mRvsDisplay;
	private TextView mMagsDisplay;
	private TextView mBrochuresDisplay;
	private TextView mBooksDisplay;
	private TextView mBibleStudiesDisplay;
	private TextView mHintView;
	private ImageButton mQuickEmailBtn;
	
	private int mCurrentMonth = TimeUtil.getCurrentMonth();
	private int mCurrentYear = TimeUtil.getCurrentYear(); // 1 - 12
	private int mTimeSpan = TIME_PERIOD_MONTH;
	
	private static int SERVICE_YEAR_START = 9; // Month, September
	
	private static final int SWIPE_MIN_DISTANCE = 120;
    private static final int SWIPE_MAX_OFF_PATH = 250;
    private static final int SWIPE_THRESHOLD_VELOCITY = 200;

	public static final int DIALOG_ROUND_ID = 1;
	
    private GestureDetector mGestureDetector;

	private ShareActionProvider mShareProvider;

	private Button mExtraTimeBtn;

	private ContentResolver mContentResolver;

	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        mContentResolver = getActivity().getContentResolver();
    }
	
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    	View rootView = inflater.inflate(R.layout.stats, container, false);
    	
    	mTimePeriodDisplay = (TextView) rootView.findViewById(R.id.stats_timeperiod);
        
        mHoursDisplay = (TextView) rootView.findViewById(R.id.hours);
        mRvsDisplay = (TextView) rootView.findViewById(R.id.rvs);
        mMagsDisplay = (TextView) rootView.findViewById(R.id.magazines);
        mBrochuresDisplay = (TextView) rootView.findViewById(R.id.brochures);
        mBooksDisplay = (TextView) rootView.findViewById(R.id.books);
        mBibleStudiesDisplay = (TextView) rootView.findViewById(R.id.bible_studies);
        mHintView = (TextView) rootView.findViewById(R.id.hint);
        mExtraTimeBtn = (Button) rootView.findViewById(R.id.round_or_carry);
        mExtraTimeBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				promptExtraTime();
			}
        	
        });
        
    	return rootView;
    }
	
	
	@Override
	public void onResume() {
		super.onResume();
		
		fillData();
		toggleHintView();
	}



	protected void fillData() {
		mTimePeriodDisplay.setText(getTimePeriodText());
		mHoursDisplay.setText(getHours());
		mRvsDisplay.setText(getRVs());
		mMagsDisplay.setText(getMagazines());
		mBrochuresDisplay.setText(getBrochures());
		mBooksDisplay.setText(getBooks());
		mBibleStudiesDisplay.setText(getBibleStudies());
		
		toggleExtraTimeButton();
		setShareIntent();
	}
	
	protected void toggleExtraTimeButton() {
		// Extra Time, and on Monthly view. No need when looking at Service Year
		if (getTimeSpan() == TIME_PERIOD_MONTH && hasExtraTime()) {
			mExtraTimeBtn.setVisibility(View.VISIBLE);
		} else {
			mExtraTimeBtn.setVisibility(View.INVISIBLE);
		}
	}
	
	protected String getTimePeriodText() {
		if(mTimeSpan == TIME_PERIOD_MONTH) {
			String[] months = getResources().getStringArray(R.array.months_array);
			return months[mCurrentMonth-1] + " " + mCurrentYear;
		} else {
			return getString(R.string.service_year) + " " + mCurrentYear;
		}
	}
	
	protected String getBibleStudies() {
		Cursor c = mContentResolver.query(ReturnVisits.BIBLE_STUDIES_CONTENT_URI, BibleStudiesProjection, ReturnVisits.IS_BIBLE_STUDY + "=1 and " + getTimePeriodWhere(ReturnVisits.DATE), getTimePeriodArgs(mCurrentYear, mCurrentMonth), null);
		String numOfRVs = "0";
		if(c != null) {
			c.moveToFirst();
			numOfRVs = "" + c.getCount();
			c.close();
			c = null;
		}
		
		return numOfRVs;
	}



	protected String getBooks() {
		Cursor c = mContentResolver.query(Placements.BOOKS_CONTENT_URI, PlacementsProjection, getTimePeriodWhere(ReturnVisits.DATE), getTimePeriodArgs(mCurrentYear, mCurrentMonth), null);
		int sum = 0;
		if(c != null) {
			sum = c.getCount();
			c.close();
			c = null;
		}
		return ""+sum;
	}



	protected String getBrochures() {
		Cursor c = mContentResolver.query(Placements.BROCHURES_CONTENT_URI, PlacementsProjection, getTimePeriodWhere(ReturnVisits.DATE), getTimePeriodArgs(mCurrentYear, mCurrentMonth), null);
		int sum = 0;
		if(c != null) {
			sum = c.getCount();
			c.close();
			c = null;
		}
		return ""+sum;
	}



	protected String getMagazines() {
		Cursor c = mContentResolver.query(Placements.MAGAZINES_CONTENT_URI, PlacementsProjection, getTimePeriodWhere(ReturnVisits.DATE), getTimePeriodArgs(mCurrentYear, mCurrentMonth), null);
		int sum = 0;
		if(c != null) {
			c.moveToFirst();
			int weightCol = c.getColumnIndex(Literature.WEIGHT);
			while(!c.isAfterLast()) {
				sum += c.getInt(weightCol);
				c.moveToNext(); 
			}
			c.close();
			c = null;
		}
		return ""+sum;
	}



	protected int getHoursSum() {
		
		Cursor c = mContentResolver.query(TimeEntries.CONTENT_URI, TimeProjection, getTimePeriodWhere(ReturnVisits.DATE), getTimePeriodArgs(mCurrentYear, mCurrentMonth), null);
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
		return sum;
	}
	
	protected String getHours() {
		return TimeUtil.toTimeString(getHoursSum(), getResources());
	}
	
	protected String getRVs() {
		Cursor c = mContentResolver.query(ReturnVisits.CONTENT_URI, RVProjection, getTimePeriodWhere(ReturnVisits.DATE), getTimePeriodArgs(mCurrentYear, mCurrentMonth), null);
		String numOfRVs = "0";
		if(c != null) {
			c.moveToFirst();
			numOfRVs = "" + c.getCount();
			c.close();
			c = null;
		}
		
		return numOfRVs;
	}
	
	protected void moveTimePeriodBackward() {
		if(mTimeSpan == TIME_PERIOD_MONTH) {
			mCurrentMonth--;
			if(mCurrentMonth <= 0) {
				mCurrentMonth = 12;
				mCurrentYear--;
			}
		} else {
			mCurrentYear--;
		}
	}
	
	protected void moveTimePeriodForward() {
		if(mTimeSpan == TIME_PERIOD_MONTH) {
			mCurrentMonth++;
			if(mCurrentMonth > 12) {
				mCurrentMonth = 1;
				mCurrentYear++;
			}
		} else {
			mCurrentYear++;
		}
	}
	
	protected String getTimePeriodWhere(String dateField) {
		return "("+dateField + " between ? and ?)";
	}
	
	protected String[] getTimePeriodArgs(int year, int month) {
		if(mTimeSpan == TIME_PERIOD_YEAR) {
			month = SERVICE_YEAR_START; // service year is from Sept (9) - Aug (8)
			year = year - 1; //year is always in the future
		}
		
		Calendar cal = Calendar.getInstance();
		cal.set(year, month - 1, 1, 0, 0, 0);
		String[] args = new String[2];

		//beginning of month
		Date start = cal.getTime();
		args[0] = TimeUtil.getSQLTextFromDate(start);
		
		if(mTimeSpan == TIME_PERIOD_MONTH) {
			cal.add(Calendar.MONTH, 1);
		} else {
			cal.add(Calendar.YEAR, 1);
		}
		
		cal.add(Calendar.SECOND, -1);
		
		Date end = cal.getTime();
		
		//end of month
		args[1] = TimeUtil.getSQLTextFromTime(end);
		
		return args;
	}
	
	protected void setTimeSpan(int span) {
		mTimeSpan = span;
		
		//we need to make sure to move the Year around to show the correct service year
		if(span == TIME_PERIOD_YEAR) {
			if(mCurrentMonth >= SERVICE_YEAR_START) {
				mCurrentYear++;
			}
		} else {
			if(mCurrentMonth >= SERVICE_YEAR_START) {
				mCurrentYear--;
			}
		}
		
		//invalidateOptionsMenu();
		fillData();
	}
	
	protected int getTimeSpan() {
		return mTimeSpan;
	}
	
	protected void toggleTimeSpan() {
		if (getTimeSpan() == TIME_PERIOD_MONTH) {
			setTimeSpan(TIME_PERIOD_YEAR);
		} else if (getTimeSpan() == TIME_PERIOD_YEAR) {
			setTimeSpan(TIME_PERIOD_MONTH);
		}
	}
	
	
	@Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.stats, menu);
		mShareProvider = (ShareActionProvider) menu.findItem(R.id.menu_send).getActionProvider();
		setShareIntent();
    }
	


	@Override
	public boolean onOptionsItemSelected(MenuItem item) {		
		
		switch(item.getItemId()) {
		case R.id.menu_time_period:
			toggleTimeSpan();
			break;	
		
		case R.id.menu_backup:
			backupData();
			break;
		}
		
		
		return super.onOptionsItemSelected(item);
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		Dialog dialog;
	    switch(id) {
	    case DIALOG_ROUND_ID:
	    	dialog = makeRoundTimeDialog();
	        break;
	    default:
	        dialog = null;
	    }
	    return dialog;
	}
	
	protected Dialog makeRoundTimeDialog() {

		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		
		builder.setTitle(getHours());
		builder.setMessage(getString(R.string.round_time_prompt))
	       .setCancelable(true)
	       .setPositiveButton(getString(R.string.round_time_prompt_round), new DialogInterface.OnClickListener() {
	           public void onClick(DialogInterface dialog, int id) {
	        	   roundUpTime();
	           }
	       })
	       .setNegativeButton(getString(R.string.round_time_prompt_carry), new DialogInterface.OnClickListener() {
	           public void onClick(DialogInterface dialog, int id) {
		           carryOverTime();
	           }
	       });
		return builder.create();
	}
	
	protected void roundUpTime() {
		//add another time entry of 60 - mins, on last day of month
		int minutes = TimeUtil.getMins(getHoursSum());
		Calendar cal = Calendar.getInstance();
		cal.set(mCurrentYear, mCurrentMonth, 1);
		cal.add(Calendar.DATE, -1);
		
		Date lastDayOfMonth = cal.getTime();
		
		ContentValues values = new ContentValues();
		values.put(TimeEntries.LENGTH, TimeUtil.toTimeInt(0, 60 - minutes));
		values.put(TimeEntries.DATE, TimeUtil.getSQLTextFromDate(lastDayOfMonth));
		
		mContentResolver.insert(TimeEntries.CONTENT_URI, values);
		fillData();
	}

	protected void carryOverTime() {
		//place time entry of `mins` in next month
		int minutes = TimeUtil.getMins(getHoursSum());
		Calendar cal = Calendar.getInstance();
		cal.set(mCurrentYear, mCurrentMonth - 1, 1, 0, 0, 0);
		cal.add(Calendar.MONTH, 1);
		
		Date nextMonth = cal.getTime();
		
		// Save the all extra minutes into the next month
		ContentValues values = new ContentValues();
		values.put(TimeEntries.LENGTH, TimeUtil.toTimeInt(0, minutes));
		values.put(TimeEntries.DATE, TimeUtil.getSQLTextFromDate(nextMonth));
		values.put(TimeEntries.NOTE, getString(R.string.carry_over));
		
		mContentResolver.insert(TimeEntries.CONTENT_URI, values);

		//somehow remove those mins from this month
		// Collect all time entries, and peel off minutes until we get to 0 extra mins, and save the Entries back
		int minutesLeft = minutes;
		Cursor cursor = mContentResolver.query(TimeEntries.CONTENT_URI, TimeProjection, getTimePeriodWhere(ReturnVisits.DATE), getTimePeriodArgs(mCurrentYear, mCurrentMonth), TimeEntries.LENGTH +" desc");
		if(cursor.getCount() > 0) {
			cursor.moveToFirst();
			int curTime = 0;
			while(!cursor.isAfterLast() && minutesLeft > 0) {
				curTime = cursor.getInt(2) / TimeUtil.MIN;
				if(curTime >= minutesLeft) {
					curTime -= minutesLeft;
					minutesLeft = 0;
				} else {
					minutesLeft -= curTime;
					curTime = 0;
				}
				
				//update the time entry if curTime > 0, else remove the entry
				Uri curUri = ContentUris.withAppendedId(TimeEntries.CONTENT_URI, cursor.getInt(0));
				if(curTime > 0) {
					ContentValues upValues = new ContentValues();
					upValues.put(TimeEntries.LENGTH, TimeUtil.toTimeInt(0, curTime));
					mContentResolver.update(curUri, upValues, null, null);
				} else {
					mContentResolver.delete(curUri, null, null);
				}
				
			
				cursor.moveToNext();
			}
		}
		cursor.close();
		cursor = null;
		
		fillData();
	}
	
	protected void promptExtraTime() {
		showDialog(DIALOG_ROUND_ID);
	}
	
	private void setShareIntent() {
		if (mShareProvider != null) {
			Intent i = new Intent(Intent.ACTION_SEND);
			i.setType("text/plain");
			i.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.service_time_for, mCurrentMonth + "/" + mCurrentYear));  
			i.putExtra(Intent.EXTRA_TEXT, getStatsTextForTimePeriod());
			mShareProvider.setShareIntent(i);
		}
	}
	
	protected void backupData() {
		Intent i = new Intent(BackupService.ACTION_BACKUP_IMMEDIATELY, null, getActivity(), BackupService.class);
		getActivity().startService(i);
	}
	
	protected boolean hasExtraTime() {
		int seconds = getHoursSum();
		int hours = TimeUtil.getHours(seconds);
		int minutes = TimeUtil.getMins(seconds);
		
		//make sure Hours greater than 1. don't want to bother someone if they're submitting under an hour
		//since the Society points out that infirm publishers can still report as small as 15 minutes.
		//asking them to round or carry over every time would be discouraging...
		if(minutes > 0 && hours > 1) {
			return true;
		}
		
		return false;
	}



	protected String getStatsTextForTimePeriod() {
		StringBuilder sb = new StringBuilder();
		
		sb.append(getString(R.string.service_time_for, getTimePeriodText() + "\n\n"));
		sb.append(getString(R.string.hours) + ": " + getHours() + "\n");
		sb.append(getString(R.string.magazines) + ": " + getMagazines() + "\n");
		sb.append(getString(R.string.brochures) + ": " + getBrochures() + "\n");
		sb.append(getString(R.string.books) + ": " + getBooks() + "\n");
		sb.append(getString(R.string.rvs) + ": " + getRVs() + "\n");
		sb.append(getString(R.string.bible_studies) + ": " + getBibleStudies() + "\n");	
		
		return sb.toString();
	}
	
	protected void toggleHintView() {
		//show the email hint 2 days before then end of the month
		// and 5 days from the start

		int day = TimeUtil.getCurrentDay();
		Calendar c = Calendar.getInstance();
		c.add(Calendar.MONTH, 1);
		c.set(Calendar.DATE, 1);
		c.add(Calendar.DATE, -1);

		int lastDayOfMonth = c.get(Calendar.DATE);
		if (day <= 5 || day > lastDayOfMonth - 2) {
			mHintView.setVisibility(View.VISIBLE);
		}

	}
	

}
