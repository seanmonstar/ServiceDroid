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

import com.monstarlab.servicedroid.model.Models.Calls;
import com.monstarlab.servicedroid.model.Models.Literature;
import com.monstarlab.servicedroid.model.Models.Placements;
import com.monstarlab.servicedroid.model.Models.ReturnVisits;
import com.monstarlab.servicedroid.model.Models.TimeEntries;

public class ServiceProvider extends ContentProvider {
	
	private static final String TAG = "ServiceProvider";
	
	private static final String DATABASE_NAME = "servoid"; //TODO - change to R.app_name
    private static final int DATABASE_VERSION = 13; //TODO - once DB is finalized, set back to 1.
    private static final String TIME_ENTRIES_TABLE = "time_entries";
    private static final String CALLS_TABLE = "calls";
    private static final String RETURN_VISITS_TABLE = "return_visits";
    private static final String LITERATURE_TABLE = "literature";
    private static final String PLACEMENTS_TABLE = "placements";
    
    private static HashMap<String, String> sTimeProjectionMap;
    private static HashMap<String, String> sCallProjectionMap;
    private static HashMap<String, String> sRVProjectionMap;
    private static HashMap<String, String> sLiteratureProjectionMap;
    private static HashMap<String, String> sPlacementProjectionMap;
    
    private static final int TIME_ENTRIES = 1;
    private static final int TIME_ENTRY_ID = 2;
    private static final int CALLS = 3;
    private static final int CALLS_ID = 4;
    private static final int RETURN_VISITS = 5;
    private static final int RETURN_VISITS_ID = 6;
    private static final int PLACEMENTS = 7;
    private static final int PLACEMENTS_ID = 8;
    private static final int LITERATURE = 9;
    private static final int LITERATURE_ID = 10;
    
    private static final UriMatcher sUriMatcher;
    
    static {
    	sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    	sUriMatcher.addURI(Models.AUTHORITY, "time_entries", TIME_ENTRIES);
    	sUriMatcher.addURI(Models.AUTHORITY, "time_entries/#", TIME_ENTRY_ID);
    	sUriMatcher.addURI(Models.AUTHORITY, "calls", CALLS);
    	sUriMatcher.addURI(Models.AUTHORITY, "calls/#", CALLS_ID);
    	sUriMatcher.addURI(Models.AUTHORITY, "returnvisits", RETURN_VISITS);
    	sUriMatcher.addURI(Models.AUTHORITY, "returnvisits/#", RETURN_VISITS_ID);
    	sUriMatcher.addURI(Models.AUTHORITY, "placements", PLACEMENTS);
    	sUriMatcher.addURI(Models.AUTHORITY, "placements/#", PLACEMENTS_ID);
    	
    	sTimeProjectionMap = new HashMap<String, String>();
    	sTimeProjectionMap.put(TimeEntries._ID, TimeEntries._ID);
    	sTimeProjectionMap.put(TimeEntries.LENGTH, TimeEntries.LENGTH);
    	sTimeProjectionMap.put(TimeEntries.DATE, TimeEntries.DATE);
    	
    	sCallProjectionMap = new HashMap<String, String>();
    	sCallProjectionMap.put(Calls._ID, Calls._ID);
    	sCallProjectionMap.put(Calls.NAME, Calls.NAME);
    	sCallProjectionMap.put(Calls.ADDRESS, Calls.ADDRESS);
    	sCallProjectionMap.put(Calls.NOTES, Calls.NOTES);
    	sCallProjectionMap.put(Calls.BIBLE_STUDY, Calls.BIBLE_STUDY);
    	
    	sRVProjectionMap = new HashMap<String, String>();
    	sRVProjectionMap.put(ReturnVisits._ID, ReturnVisits._ID);
    	sRVProjectionMap.put(ReturnVisits.DATE, ReturnVisits.DATE);
    	sRVProjectionMap.put(ReturnVisits.CALL_ID, ReturnVisits.CALL_ID);
    	
    	sLiteratureProjectionMap = new HashMap<String, String>();
    	sLiteratureProjectionMap.put(Literature._ID, Literature._ID);
    	sLiteratureProjectionMap.put(Literature.TITLE, Literature.TITLE);
    	
    	sPlacementProjectionMap = new HashMap<String, String>();
    	sPlacementProjectionMap.put(Placements._ID, Placements._ID);
    	sPlacementProjectionMap.put(Placements.DATE, Placements.DATE);
    	sPlacementProjectionMap.put(Placements.CALL_ID, Placements.CALL_ID);
    	sPlacementProjectionMap.put(Placements.LITERATURE_ID, Placements.LITERATURE_ID);
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
			
			db.execSQL("create table " +  CALLS_TABLE + " (" 
				+ Calls._ID + " integer primary key autoincrement,"
			    + Calls.NAME + " varchar(128),"
			    + Calls.ADDRESS + " varchar(128),"
			    + Calls.BIBLE_STUDY + " boolean default false,"
			    + Calls.DATE + " date default current_timestamp,"
			    + Calls.NOTES + " text );");
			
			db.execSQL("create table " +  RETURN_VISITS_TABLE + " (" 
				+ ReturnVisits._ID + " integer primary key autoincrement,"
			    + ReturnVisits.DATE + " date default current_timestamp,"
			    + ReturnVisits.CALL_ID + " integer references calls(id) )");
			
			db.execSQL("create table " +  LITERATURE_TABLE + " (" 
				+ Literature._ID + " integer primary key autoincrement,"
			    + Literature.TITLE + " varchar(256))");
			
			db.execSQL("create table " +  PLACEMENTS_TABLE + " (" 
				+ Placements._ID + " integer primary key autoincrement,"
			    + Placements.DATE + " date default current_timestamp,"
			    + Placements.CALL_ID + " integer references calls(id),"
			    + Placements.LITERATURE_ID + " integer references literature(id))");
			
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
                    + newVersion + ", which will destroy all old data");
            db.execSQL("DROP TABLE IF EXISTS " + TIME_ENTRIES_TABLE);
            db.execSQL("DROP TABLE IF EXISTS " + CALLS_TABLE);
            db.execSQL("DROP TABLE IF EXISTS " + RETURN_VISITS_TABLE);
            db.execSQL("DROP TABLE IF EXISTS " + PLACEMENTS_TABLE);
            db.execSQL("DROP TABLE IF EXISTS " + LITERATURE_TABLE);
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
		case CALLS:
			count = db.delete(CALLS_TABLE, where, whereArgs);
			break;
		case CALLS_ID:
			String callId = uri.getPathSegments().get(1);
			count = db.delete(CALLS_TABLE, Calls._ID + "=" + callId + (!TextUtils.isEmpty(where) ? " AND ( " + where + ")" : ""), whereArgs);
			break;
			
		case RETURN_VISITS:
			count = db.delete(RETURN_VISITS_TABLE, where, whereArgs);
			break;
		case RETURN_VISITS_ID:
			String rvId = uri.getPathSegments().get(1);
			count = db.delete(RETURN_VISITS_TABLE, Calls._ID + "=" + rvId + (!TextUtils.isEmpty(where) ? " AND ( " + where + ")" : ""), whereArgs);
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
		case CALLS:
			return Calls.CONTENT_TYPE;
		case CALLS_ID:
			return Calls.CONTENT_ITEM_TYPE;
			
		case RETURN_VISITS:
			return ReturnVisits.CONTENT_TYPE;
		case RETURN_VISITS_ID:
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
		case CALLS:
			tableName = CALLS_TABLE;
			nullColumn = Calls.NAME;
			contentUri = Calls.CONTENT_URI;
			break;
		case RETURN_VISITS:
			tableName = RETURN_VISITS_TABLE;
			nullColumn = ReturnVisits.DATE;
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
		case CALLS_ID:
			qb.appendWhere(Calls._ID + "=" + uri.getPathSegments().get(1));
			//falls through
		case CALLS:
			qb.setTables(CALLS_TABLE);
			qb.setProjectionMap(sCallProjectionMap);
			if(TextUtils.isEmpty(orderBy)) {
				orderBy = Calls.DEFAULT_SORT_ORDER;
			}
			break;
			
		case RETURN_VISITS_ID:
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
		case CALLS:
			count = db.update(CALLS_TABLE, values, where, whereArgs);
			break;
		case CALLS_ID:
			String callId = uri.getPathSegments().get(1);
			count = db.update(CALLS_TABLE, values, Calls._ID + "=" + callId + (!TextUtils.isEmpty(where) ? " AND ( " + where + " )" : ""), whereArgs);
			break;
		case RETURN_VISITS:
			count = db.update(RETURN_VISITS_TABLE, values, where, whereArgs);
			break;
		case RETURN_VISITS_ID:
			String rvId = uri.getPathSegments().get(1);
			count = db.update(RETURN_VISITS_TABLE, values, Calls._ID + "=" + rvId + (!TextUtils.isEmpty(where) ? " AND ( " + where + " )" : ""), whereArgs);
			break;
		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}
		
		getContext().getContentResolver().notifyChange(uri, null);
		return count;
	}

}
