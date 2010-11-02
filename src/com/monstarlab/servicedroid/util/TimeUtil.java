package com.monstarlab.servicedroid.util;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import android.content.Context;

public class TimeUtil {
	
    //SQLite will return format like "2010-04-16 20:56:21"
	private DateFormat mDateTimeParser;
	private DateFormat mDateParser;
	private DateFormat mDateFormatter;
	
	public static final int HOUR = 3600;
	public static final int MIN = 60;
	
	public TimeUtil(Context ctx) {
		mDateParser = new SimpleDateFormat("yyyy-MM-dd");
		mDateTimeParser = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        mDateFormatter = android.text.format.DateFormat.getDateFormat(ctx);
	}
	
	/**
	 * 
	 * @param text String A date in "yyyy-MM-dd HH:mm:ss" format.
	 * @return String A the date formatted to the user's preferences, or the untouched date if errored.
	 */
	public String normalizeDate(String text) {
		try {
			return formatDate(parseDateText(text));
		} catch (ParseException e) {
			return text;
		}

	}
	
	public Date parseDateText(String text) throws ParseException {
		try {
			return mDateTimeParser.parse(text);
		} catch(ParseException e) {
			return mDateParser.parse(text);
		}
	}
	
	public String formatDate(Date d) {
		return mDateFormatter.format(d);
	}
	
	public static String toTimeString(int time) {
		StringBuilder out = new StringBuilder();
		
		int hours = getHours(time);
		if(hours > 0) {
			out.append(hours);
			if(hours > 1) {
				out.append("hrs");
			} else {
				out.append("hr");
			}
		}
		
		int mins = getMins(time);
		if(mins > 0) {
			out.append(" ");
			out.append(mins);
			if(mins > 1) {
				out.append("mins");
			} else {
				out.append("min");
			}
		}
		
		if(out.toString() == "") {
			out.append("0mins");
		}
		
		return out.toString();
	}
	
	public static int toTimeInt(int hours, int mins) {
		return (hours * HOUR) + (mins * MIN);
	}
	
	public static int getHours(int time) {
		return time / HOUR;
	}
	
	public static int getMins(int time) {
		return (time - (getHours(time) * HOUR)) / MIN;
	}
	
	public static String pad(int number) {
		if(number < 10) {
			return "0"+number;
		} else {
			return ""+number;
		}
	}
	
	public static int getCurrentYear() {
		Calendar c = Calendar.getInstance();
		return c.get(Calendar.YEAR);
	}
	
	/*
	 * returns Month (1-12)
	 */
	public static int getCurrentMonth() {
		Calendar c = Calendar.getInstance();
		return c.get(Calendar.MONTH) + 1;
	}
	
	public static int getCurrentDay() {
		Calendar c = Calendar.getInstance();
		return c.get(Calendar.DATE);
	}
	
	public static long getCurrentTime() {
		Calendar c = Calendar.getInstance();
		return c.getTimeInMillis();
	}
}
