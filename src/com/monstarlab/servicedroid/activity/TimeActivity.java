package com.monstarlab.servicedroid.activity;

import java.util.Calendar;
import java.util.Date;

import android.app.ListActivity;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.ContextMenu;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.View.OnTouchListener;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.AdapterView.AdapterContextMenuInfo;

import com.monstarlab.servicedroid.R;
import com.monstarlab.servicedroid.activity.StatisticsActivity.MyGestureDetector;
import com.monstarlab.servicedroid.model.Models.TimeEntries;
import com.monstarlab.servicedroid.service.TimerService;
import com.monstarlab.servicedroid.util.TimeUtil;

public class TimeActivity extends ListActivity implements OnTouchListener {
	
	private static final String TAG = "TimeActivity";
	
	public static final int INSERT_ID = Menu.FIRST;
	public static final int START_ID = Menu.FIRST + 1;
	public static final int STOP_ID = Menu.FIRST + 2;
	private static final int EDIT_ID = Menu.FIRST + 3;
	private static final int DELETE_ID = Menu.FIRST + 4;
	
	//private static final int ACTIVITY_CREATE=0;
    //private static final int ACTIVITY_EDIT=1;
    
    //private static final String TIMER = "Timer";
    
    private static final int SHOW_TIMER_NOTIFICATION = 1;
    
    private static final String[] PROJECTION = new String[] { TimeEntries._ID, TimeEntries.LENGTH, TimeEntries.DATE, TimeEntries.NOTE };
    
    private int mCurrentMonth = TimeUtil.getCurrentMonth();
    private int mCurrentYear = TimeUtil.getCurrentYear();
	private TimeUtil mTimeHelper;
	private Cursor mCursor;
	private Boolean mTiming = false;
	//private Long mTimerStart;
	//private Handler mTimer = new Handler();
	//private TextView mTimerView;
	
	private static final int SWIPE_MIN_DISTANCE = 120;
    private static final int SWIPE_MAX_OFF_PATH = 250;
    private static final int SWIPE_THRESHOLD_VELOCITY = 200;
	
    private GestureDetector mGestureDetector;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		
		Intent intent = getIntent();
		if(intent.getData() == null) {
			intent.setData(TimeEntries.CONTENT_URI);
		}
		
		
		mTiming = TimerService.isRunning;
		if(!mTiming) {
			checkForTimer();
		}

		setContentView(R.layout.time);
		
		mTimeHelper = new TimeUtil(this);
		
		
		setHeaderText();
        
		fillData();
		
		registerForContextMenu(getListView());
		
		// Gesture detection
	    mGestureDetector = new GestureDetector(new MyGestureDetector());
	    getListView().setOnTouchListener(this);
	    findViewById(android.R.id.empty).setOnTouchListener(this);
	    findViewById(R.id.header).setOnTouchListener(this);
	    
	}
	
	public void fillData() {
		 // Get this month's entries from the database and create the item list
		mCursor = managedQuery(getIntent().getData(), PROJECTION, TimeEntries.LENGTH + " > 0 and " + TimeEntries.DATE + " between ? and ?", getTimePeriodArgs(mCurrentYear, mCurrentMonth), TimeEntries.DATE + " ASC");

        String[] from = new String[] { TimeEntries.DATE, TimeEntries.LENGTH, TimeEntries.NOTE };
        int[] to = new int[] { R.id.date, R.id.length, R.id.notes };
 
        // Now create an cursor adapter and set it to display using our row
        // overriding setViewText to format the Date string
        final Resources r = getResources();
        SimpleCursorAdapter entries = new SimpleCursorAdapter(this, R.layout.time_row, mCursor, from, to) {
        	
        	@Override
        	public void setViewText(TextView v, String text) {
        		if(v.getId() == R.id.date){
					text = mTimeHelper.normalizeDate(text);
        			v.setText(text);
        		} else if(v.getId() == R.id.length) {
        			if(TextUtils.isEmpty(text)) {
        				text = "0";
        			}
        			
        			text = TimeUtil.toTimeString(Integer.parseInt(text), r);
        			
        			v.setText(text);
        			
        		} else {
        			super.setViewText(v, text);
        		}
        		
        	}
        	
        };
        
        setListAdapter(entries);
	}
	
	protected void setHeaderText() {
		TextView view = (TextView) findViewById(R.id.header);
		String[] months = getResources().getStringArray(R.array.months_array);
		
		
		view.setText(months[mCurrentMonth-1] + " " + mCurrentYear);
	}
	
	protected String[] getTimePeriodArgs(int year, int month) {
		Calendar cal = Calendar.getInstance();
		cal.set(year, month - 1, 1);
		String[] args = new String[2];

		//beginning of month
		Date startOfMonth = cal.getTime();
		args[0] = TimeUtil.getSQLTextFromDate(startOfMonth);
		
		
		cal.add(Calendar.MONTH, 1);
		cal.add(Calendar.DATE, -1);
		Date endOfMonth = cal.getTime();
		
		//end of month
		args[1] = TimeUtil.getSQLTextFromDate(endOfMonth);
		
		return args;
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		
		menu.add(0, INSERT_ID, 1, R.string.add_time)
			.setShortcut('3', 'a')
			.setIcon(android.R.drawable.ic_menu_add);
		
		//Start/Stop Time happens onPrepare
		
				
		return true;
	}
	
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		super.onPrepareOptionsMenu(menu);
		
		//menu depends on if user has start Service Timer
		if(mTiming) {
			menu.removeItem(START_ID);
			if(menu.findItem(STOP_ID) == null) {
				menu.add(0, STOP_ID, 2, R.string.stop_time).setIcon(android.R.drawable.ic_menu_close_clear_cancel);
			}
		} else {
			menu.removeItem(STOP_ID);
			if(menu.findItem(START_ID) == null) {
				menu.add(0, START_ID, 2, R.string.start_time).setIcon(android.R.drawable.ic_menu_recent_history);
			}
		}
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()) {
		case INSERT_ID:
			//this.startActivity(new Intent(Intent.ACTION_INSERT, this.getIntent().getData()));
			createEntry();
			return true;
		case START_ID:
			startTimer();
			return true;
		case STOP_ID:
			stopTimer();
			return true;
		}
		
		return super.onOptionsItemSelected(item);
	}
	
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		
		String title = ((TextView)((AdapterContextMenuInfo)menuInfo).targetView.findViewById(R.id.length)).getText().toString();
		
		menu.setHeaderTitle(title);
		menu.add(0, EDIT_ID, 0, R.string.edit);
		menu.add(0, DELETE_ID, 0, R.string.delete_time);
	}
	
	@Override
	public boolean onContextItemSelected(MenuItem item) {
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
		switch(item.getItemId()) {
		
		case EDIT_ID:
			editEntry(info.id);
			return true;
		
		case DELETE_ID:
			
			deleteEntry(info.id);
			return true;
		}
		
		
		return super.onContextItemSelected(item);
	}
	
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		editEntry(id);
	}
	
	private void editEntry(long id) {
		Uri uri = ContentUris.withAppendedId(getIntent().getData(), id);
		Intent i = new Intent(Intent.ACTION_EDIT, uri, this, TimeEditActivity.class);
        startActivity(i);
	}
	
	private void createEntry() {
		Intent i = new Intent(Intent.ACTION_INSERT, getIntent().getData(), this, TimeEditActivity.class);
		startActivity(i);
	}
	
	private void deleteEntry(long id) {
		Uri entryUri = ContentUris.withAppendedId(getIntent().getData(), id);
		getContentResolver().delete(entryUri, null, null);
	}
	
	private void startTimer() {
		mTiming = true;
		
		// create a TimeEntry with 0 length, at this very second.
		ContentValues values = new ContentValues();
		values.put(TimeEntries.DATE, TimeUtil.getCurrentTimeSQLText());
		getContentResolver().insert(getIntent().getData(), values);
		
		Intent i = new Intent(this, TimerService.class);
		startService(i);
	}
	
	private void stopTimer() {
		if(!mTiming) return;
		mTiming = false;
		Intent i = new Intent(this, TimerService.class);
		stopService(i);
	}
	
	private void checkForTimer() {
		Cursor c = getContentResolver().query(TimeEntries.CONTENT_URI, PROJECTION, TimeEntries.LENGTH + " is null or " + TimeEntries.LENGTH + "=0", null, null);
		if(c.getCount() > 0) {
			mTiming = true;
			Intent i = new Intent(this, TimerService.class);
			startService(i);
			
			//remove any extras
			if(c.getCount() > 1) {
				c.moveToFirst();
				c.moveToNext();
				while(!c.isAfterLast()) {
					getContentResolver().delete(ContentUris.withAppendedId(getIntent().getData(), c.getInt(0)), null, null);
				}
			}
		}
		c.close();
	}
	
	protected void moveTimePeriodBackward() {
		mCurrentMonth--;
		if(mCurrentMonth <= 0) {
			mCurrentMonth = 12;
			mCurrentYear--;
		}
	}
	
	protected void moveTimePeriodForward() {
		mCurrentMonth++;
		if(mCurrentMonth > 12) {
			mCurrentMonth = 1;
			mCurrentYear++;
		}
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
	            	moveTimePeriodForward();
	            	fillData();
	            	setHeaderText();
	            }  else if (e2.getX() - e1.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
	                //right
	            	moveTimePeriodBackward();
	            	fillData();
	            	setHeaderText();
	            } else {
	            	// vertical distance wasn't over MAX_OFF_PATH
	            	// but horizontal wasnt enough for a SWIPE
	            	// so don't consume event, cause we aren't doing anything else with it
	            	return false;
	            }
	        } catch (Exception e) {
	            // nothing
	        }
	        // actually, we should just never "consume" the event.
	        // always let the list handle the event too.
	        return false;
	    }
	    
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		boolean gestureConsumed = mGestureDetector.onTouchEvent(event);
		if (v instanceof ListView) {
			// this lets normal clicks, and longpresses still work for the listview
			return gestureConsumed;
		} else {
			// otherwise, this lets the swipes work on the empty view
			return true;
		}
    }

}
