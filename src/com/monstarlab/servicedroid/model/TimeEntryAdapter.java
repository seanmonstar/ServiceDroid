package com.monstarlab.servicedroid.model;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;

public class TimeEntryAdapter extends DataAdapter {

    private static final String TAG = "TimeEntryAdapter";
    


    
    public static final String KEY_ID = "_id";
    public static final String KEY_LENGTH = "length";
    public static final String KEY_DATE = "date";

    /**
     * Constructor - takes the context to allow the database to be
     * opened/created
     * 
     * @param ctx the Context within which to work
     */
    public TimeEntryAdapter(Context ctx) {
        super(ctx, "create table time_entries" 
			    + " (" + KEY_ID + " integer primary key autoincrement,"
			    + " " + KEY_LENGTH + " integer not null,"
			    + " " + KEY_DATE + " date not null default current_timestamp);", "time_entries", new String[] { KEY_ID, KEY_LENGTH, KEY_DATE });
    }

    public TimeEntryAdapter open() throws SQLException {
    	return (TimeEntryAdapter)super.open();
    }
    
    
    public long create(int length) {
    	ContentValues values = new ContentValues();
    	values.put(KEY_LENGTH, length);
    	return super.create(values);
	}
    
    public long create(int length, String date) {
    	ContentValues values = new ContentValues();
    	values.put(KEY_LENGTH, length);
    	values.put(KEY_DATE, date);
    	return super.create(values);
    }

	public boolean update(long id, int length, String date) {
		ContentValues values = new ContentValues();
		values.put(KEY_LENGTH, length);
		values.put(KEY_DATE, date);
		
		return super.update(KEY_ID+"="+id, values);
	}
	
	public Cursor findByMonth(int month) {
		return null;
	}

	public Cursor findThisMonth() {
		//Calendar.getInstance().get(Calendar.MONTH);
		return super.find("strftime('%m', "+KEY_DATE+") = strftime('%m', 'now')", new String[]{}, KEY_DATE + " asc");
	}
	
	public int getMonthlySum() {
		Cursor c = this.findThisMonth();
		int LENGTH_INDEX = c.getColumnIndex(KEY_LENGTH);
		int sum = 0;
		
		while(!c.isLast()) {
			c.moveToNext();
			sum += c.getInt(LENGTH_INDEX);
		}
		
		return sum;
	}

}
