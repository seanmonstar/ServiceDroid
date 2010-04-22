package com.monstarlab.servicedroid.model;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DataAdapter {

    private static final String TAG = "DataAdapter";
    
    protected DatabaseHelper mDbHelper;
    protected SQLiteDatabase mDb;
    


    private static final String DATABASE_NAME = "servoid";
    private static final int DATABASE_VERSION = 5;
    
    /**
     * Database creation sql statement
     */
    protected String mDATABASE_CREATE;
    protected String mTABLE;
    protected String[] mCOLUMNS;

    protected final Context mCtx;

    private static class DatabaseHelper extends SQLiteOpenHelper {

        private static String CREATE_SQL;
        private static String TABLE;
    	
    	DatabaseHelper(Context context, String table, String create) {
    		super(context, DATABASE_NAME, null, DATABASE_VERSION);
    		CREATE_SQL = create;
    		TABLE = table;
        }

        @Override
        public void onCreate(SQLiteDatabase db) {

            db.execSQL(CREATE_SQL);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
                    + newVersion + ", which will destroy all old data");
            db.execSQL("DROP TABLE IF EXISTS " + TABLE);
            onCreate(db);
        }
    }

    public DataAdapter(Context ctx, String create, String table, String[] columns) {
        this.mCtx = ctx;
        mDATABASE_CREATE = create;
        mTABLE = table;
        mCOLUMNS = columns;
    }

    public DataAdapter open() throws SQLException {
        mDbHelper = new DatabaseHelper(mCtx, mTABLE, mDATABASE_CREATE);
        mDb = mDbHelper.getWritableDatabase();
        return this;
    }
    
    public void close() {
        mDbHelper.close();
    }

    public long create(ContentValues values) {
        return mDb.insert(mTABLE, null, values);
    }

    public boolean delete(long rowId) {

        return mDb.delete(mTABLE, mCOLUMNS[0] + "=" + rowId, null) > 0;
    }

    public Cursor fetchAll() {

        return mDb.query(mTABLE, mCOLUMNS, null, null, null, null, null);
    }
    
    public Cursor find(String where, String[] whereArgs, String orderBy) {
    	return mDb.query(mTABLE, mCOLUMNS, where, whereArgs, null, null, orderBy);
    }

    public Cursor fetch(long rowId) throws SQLException {

        Cursor mCursor = mDb.query(true, mTABLE, mCOLUMNS, mCOLUMNS[0] + "=" + rowId, null, null, null, null, null);
        if (mCursor != null) {
            mCursor.moveToFirst();
        }
        return mCursor;

    }

    
    public boolean update(String where, ContentValues values) {
        return mDb.update(mTABLE, values, where, null) > 0;
    }
}
