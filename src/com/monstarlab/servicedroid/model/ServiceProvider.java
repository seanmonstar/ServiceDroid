package com.monstarlab.servicedroid.model;

import java.util.HashMap;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import com.monstarlab.servicedroid.model.Models.ReturnVisits;
import com.monstarlab.servicedroid.model.Models.TimeEntries;

public class ServiceProvider extends ContentProvider {
	
	private static final String TAG = "ServiceProvider";
	
	private static final String DATABASE_NAME = "servoid"; //TODO - change to R.app_name
    private static final int DATABASE_VERSION = 9; //TODO - once DB is finalized, set back to 1.
    private static final String TIME_ENTRIES_TABLE = "time_entries";
    private static final String RETURN_VISITS_TABLE = "return_visits";
    
    private static HashMap<String, String> sTimeProjectionMap;
    private static HashMap<String, String> sRVProjectionMap;
    
    private static final int TIME_ENTRIES = 1;
    private static final int TIME_ENTRY_ID = 2;
    private static final int RETURN_VISITS = 3;
    private static final int RETURN_VISIT_ID = 4;
    
    private static final UriMatcher sUriMatcher;
    
    static {
    	sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    	sUriMatcher.addURI(Models.AUTHORITY, "time_entries", TIME_ENTRIES);
    	sUriMatcher.addURI(Models.AUTHORITY, "time_entries/#", TIME_ENTRY_ID);
    	sUriMatcher.addURI(Models.AUTHORITY, "return_visits", RETURN_VISITS);
    	sUriMatcher.addURI(Models.AUTHORITY, "return_visits/#", RETURN_VISIT_ID);
    	
    	sTimeProjectionMap = new HashMap<String, String>();
    	sTimeProjectionMap.put(TimeEntries._ID, TimeEntries._ID);
    	sTimeProjectionMap.put(TimeEntries.LENGTH, TimeEntries.LENGTH);
    	sTimeProjectionMap.put(TimeEntries.DATE, TimeEntries.DATE);
    	
    	sRVProjectionMap = new HashMap<String, String>();
    	sRVProjectionMap.put(ReturnVisits._ID, ReturnVisits._ID);
    	sRVProjectionMap.put(ReturnVisits.NAME, ReturnVisits.NAME);
    	sRVProjectionMap.put(ReturnVisits.ADDRESS, ReturnVisits.ADDRESS);
    	sRVProjectionMap.put(ReturnVisits.NOTES, ReturnVisits.NOTES);
    }
    
    private static class DatabaseHelper extends SQLiteOpenHelper {

		public DatabaseHelper(Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			db.execSQL("create table " + TIME_ENTRIES_TABLE + " (" 
			    + TimeEntries._ID + " integer primary key autoincrement,"
			    + TimeEntries.LENGTH + " integer,"
			    + TimeEntries.DATE + " date not null default current_timestamp);");
			
			db.execSQL("create table " +  RETURN_VISITS_TABLE + " (" 
				+ ReturnVisits._ID + " integer primary key autoincrement,"
			    + ReturnVisits.NAME + " varchar(128),"
			    + ReturnVisits.ADDRESS + " varchar(128),"
			    + ReturnVisits.NOTES + " text );");
			
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
                    + newVersion + ", which will destroy all old data");
            db.execSQL("DROP TABLE IF EXISTS " + TIME_ENTRIES_TABLE);
            db.execSQL("DROP TABLE IF EXISTS " + RETURN_VISITS_TABLE);
            onCreate(db);
			
		}
    	
    }
    
    private DatabaseHelper mDbHelper;
    
    @Override
    public boolean onCreate() {
    	mDbHelper = new DatabaseHelper(getContext());
    	return true;
    }

	@Override
	public int delete(Uri uri, String where, String[] whereArgs) {
		SQLiteDatabase db = mDbHelper.getWritableDatabase();
		int count;
		switch(sUriMatcher.match(uri)) {
		case TIME_ENTRIES:
			count = db.delete(TIME_ENTRIES_TABLE, where, whereArgs);
			break;
		case TIME_ENTRY_ID:
			String tId = uri.getPathSegments().get(1);
			count = db.delete(TIME_ENTRIES_TABLE, TimeEntries._ID + "=" + tId + (!TextUtils.isEmpty(where) ? " AND ( " + where + ")" : ""), whereArgs);
			break;
		case RETURN_VISITS:
			count = db.delete(RETURN_VISITS_TABLE, where, whereArgs);
			break;
		case RETURN_VISIT_ID:
			String rvId = uri.getPathSegments().get(1);
			count = db.delete(RETURN_VISITS_TABLE, ReturnVisits._ID + "=" + rvId + (!TextUtils.isEmpty(where) ? " AND ( " + where + ")" : ""), whereArgs);
			break;
			
		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}
		
		getContext().getContentResolver().notifyChange(uri, null);
		return count;
	}

	@Override
	public String getType(Uri uri) {
		switch(sUriMatcher.match(uri)) {
		case TIME_ENTRIES:
			return TimeEntries.CONTENT_TYPE;
		case TIME_ENTRY_ID:
			return TimeEntries.CONTENT_ITEM_TYPE;
		case RETURN_VISITS:
			return ReturnVisits.CONTENT_TYPE;
		case RETURN_VISIT_ID:
			return ReturnVisits.CONTENT_ITEM_TYPE;
			
		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}
	}

	@Override
	public Uri insert(Uri uri, ContentValues initialValues) {
		//do some table specific setup
		
		String tableName;
		String nullColumn;
		Uri contentUri;
		
		switch(sUriMatcher.match(uri)) {
		case TIME_ENTRIES:
			tableName = TIME_ENTRIES_TABLE;
			nullColumn = TimeEntries.LENGTH;
			contentUri = TimeEntries.CONTENT_URI;
			break;
		case RETURN_VISITS:
			tableName = RETURN_VISITS_TABLE;
			nullColumn = ReturnVisits.NAME;
			contentUri = ReturnVisits.CONTENT_URI;
			break;
		default:
			throw new IllegalArgumentException("Unknown URI + "+uri);
		}
		
		
		ContentValues values;
        if (initialValues != null) {
            values = new ContentValues(initialValues);
        } else {
            values = new ContentValues();
        }
		
		//the inserting is the same across the board
		SQLiteDatabase db = mDbHelper.getWritableDatabase();
		long rowId = db.insert(tableName, nullColumn, values);
		if(rowId > 0) {
			//insert worked
			Uri insertedUri = ContentUris.withAppendedId(contentUri, rowId);
			getContext().getContentResolver().notifyChange(insertedUri, null);
			return insertedUri;
		}
		
		//insert failed
		throw new SQLException("Failed to insert row into " + uri);
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,	String sortOrder) {
		SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
		
		//setup for various tables
		
		String orderBy = null;
		if(!TextUtils.isEmpty(sortOrder)) {
			orderBy = sortOrder;
		}
		switch(sUriMatcher.match(uri)) {
		case TIME_ENTRY_ID:
			qb.appendWhere(TimeEntries._ID + "=" + uri.getPathSegments().get(1));
			//falls through
		case TIME_ENTRIES:
			qb.setTables(TIME_ENTRIES_TABLE);
			qb.setProjectionMap(sTimeProjectionMap);
			if(TextUtils.isEmpty(orderBy)) {
				orderBy = TimeEntries.DEFAULT_SORT_ORDER;
			}
			break;
		case RETURN_VISIT_ID:
			qb.appendWhere(ReturnVisits._ID + "=" + uri.getPathSegments().get(1));
			//falls through
		case RETURN_VISITS:
			qb.setTables(RETURN_VISITS_TABLE);
			qb.setProjectionMap(sRVProjectionMap);
			if(TextUtils.isEmpty(orderBy)) {
				orderBy = ReturnVisits.DEFAULT_SORT_ORDER;
			}
			break;
		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}
		
		//standard query stuff
		SQLiteDatabase db = mDbHelper.getWritableDatabase();
		Cursor c = qb.query(db, projection, selection, selectionArgs, null, null, orderBy);
		
		c.setNotificationUri(getContext().getContentResolver(), uri);
		return c;
	}

	@Override
	public int update(Uri uri, ContentValues values, String where, String[] whereArgs) {
		SQLiteDatabase db = mDbHelper.getWritableDatabase();
		int count;
		switch(sUriMatcher.match(uri)) {
		case TIME_ENTRIES:
			count = db.update(TIME_ENTRIES_TABLE, values, where, whereArgs);
			break;
		case TIME_ENTRY_ID:
			String tId = uri.getPathSegments().get(1);
			count = db.update(TIME_ENTRIES_TABLE, values, TimeEntries._ID + "=" + tId + (!TextUtils.isEmpty(where) ? " AND ( " + where + ")" : ""), whereArgs);
			break;
		case RETURN_VISITS:
			count = db.update(RETURN_VISITS_TABLE, values, where, whereArgs);
			break;
		case RETURN_VISIT_ID:
			String rvId = uri.getPathSegments().get(1);
			count = db.update(RETURN_VISITS_TABLE, values, ReturnVisits._ID + "=" + rvId + (!TextUtils.isEmpty(where) ? " AND ( " + where + " )" : ""), whereArgs);
			break;
		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}
		
		getContext().getContentResolver().notifyChange(uri, null);
		return count;
	}

}
