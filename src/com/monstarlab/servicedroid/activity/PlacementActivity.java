package com.monstarlab.servicedroid.activity;

import com.monstarlab.servicedroid.model.Models.Calls;
import com.monstarlab.servicedroid.model.Models.Literature;
import com.monstarlab.servicedroid.model.Models.Placements;
import com.monstarlab.servicedroid.model.Models.TimeEntries;
import com.monstarlab.servicedroid.util.TimeUtil;

import com.monstarlab.servicedroid.R;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.AdapterView.OnItemSelectedListener;

public class PlacementActivity extends Activity {

	private static final int STATE_INSERT = 0;
	private static final int STATE_EDIT = 1;
	private static final String TAG = "PlacementActivity";
	
	private static final String[] PROJECTION = new String[] { Placements._ID, Placements.CALL_ID, Placements.LITERATURE_ID, Placements.DATE };
	private static final String[] LITERATURE_PROJECTION = new String[] { Literature._ID, Literature.TITLE, Literature.PUBLICATION, Literature.TYPE };
	
	private static final String[] YEARS = new String[] { "2011","2010","2009","2008","2007","2006","2005","2004","2003","2002","2001","2000" };
	
	private int mState;
	private Uri mUri;
	private Cursor mCursor;
	private int mPlacementType;
	private String mMagazine;
	
	private Spinner mPublicationSpinner;
	private Spinner mMonthSpinner;
	private Spinner mYearSpinner;
	
	private String mIssue;
	private String mTitle;
	private int mCallId;
	private int mLitID;
	private int mMonth;
	private int mYear;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		//load in the values, if editting
		final Intent intent = getIntent();
		final String action = intent.getAction();
		if (Intent.ACTION_EDIT.equals(action)) {
			mState = STATE_EDIT;
			mUri = intent.getData();
			mPlacementType = 0; //TODO - query from DB
			
		} else if (Intent.ACTION_INSERT.equals(action)) {
			mState = STATE_INSERT;
			mCallId = intent.getIntExtra(Calls._ID, 0);
			if(mCallId == 0) {
				Log.e(TAG, "Call ID wasn't passed, exiting");
				finish();
				return;
			}
			ContentValues values = new ContentValues();
			values.put(Placements.CALL_ID, mCallId);
			mUri = getContentResolver().insert(intent.getData(), values);
			mPlacementType = intent.getIntExtra("type", Literature.TYPE_MAGAZINE);
			
			if(mUri == null) {
				Log.e(TAG, "Failed to insert a blank row into " + getIntent().getData());
				finish();
				return;
			}
			
			setResult(RESULT_OK, (new Intent()).setAction(mUri.toString()));
		} else {
			Log.e(TAG, "Unknown action, exiting");
			finish();
			return;
		}
		
		//setup view
		if (mPlacementType == Literature.TYPE_MAGAZINE) {
			setupMagazineView();
			
		} else if (mPlacementType == Literature.TYPE_BROCHURE) {
			setContentView(R.layout.place_book);
		} else if (mPlacementType == Literature.TYPE_BOOK) {
			setContentView(R.layout.place_book);
		}
		
		
		Button confirm = (Button) findViewById(R.id.confirm);
		confirm.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View view) {
				finish();
			}
			
		});
		
		mCursor = managedQuery(mUri, PROJECTION, null, null, null);
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		
		if(mCursor != null) {
			
			if (mPlacementType == Literature.TYPE_MAGAZINE) {
				
				
			} else if (mPlacementType == Literature.TYPE_BROCHURE) {
				
			} else if (mPlacementType == Literature.TYPE_BOOK) {
				
			}
			
		}
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		
		if(mCursor != null) {
			
			int publication = mLitID;
			
			
			
			
			//when finishing, if no publication was picked, just ditch the whole thing. its useless anyways
			if(isFinishing() && publication == 0) {
				setResult(RESULT_CANCELED);
				deleteEntry();
			
			//save the current changes to the Provider
			} else {
				ContentValues values = new ContentValues();
				values.put(Placements.LITERATURE_ID, publication);
				getContentResolver().update(mUri, values, null, null);
			}
		}
	}
	
	private void setupMagazineView() {
		setContentView(R.layout.place_magazine);
		
		mPublicationSpinner = (Spinner) findViewById(R.id.magazine);
		ArrayAdapter<CharSequence> adapter = new ArrayAdapter<CharSequence>(this, android.R.layout.simple_spinner_item, new String[] { getString(R.string.watchtower), getString(R.string.awake) });
	    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
	    mPublicationSpinner.setAdapter(adapter);
	    
	    mMagazine = getString(R.string.watchtower);
	    mMonth = TimeUtil.getCurrentMonth();
	    mYear = TimeUtil.getCurrentYear();
	    
	    mPublicationSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
				mMagazine = parent.getItemAtPosition(pos).toString();
				saveSelectedLiterature();
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {
				// TODO Auto-generated method stub
				
			}
	    	
		});
	    
	    mMonthSpinner = (Spinner) findViewById(R.id.month);
	    adapter = ArrayAdapter.createFromResource(this, R.array.months_array, android.R.layout.simple_spinner_item);
	    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
	    mMonthSpinner.setAdapter(adapter);
	    mMonthSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
				mMonth = pos + 1;
				saveSelectedLiterature();
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {
				// TODO Auto-generated method stub
				
			}
	    	
		});
	    mMonthSpinner.setSelection(mMonth - 1);
	    
	    
	    mYearSpinner = (Spinner) findViewById(R.id.year);
	    adapter = new ArrayAdapter<CharSequence>(this, android.R.layout.simple_spinner_item, YEARS);
	    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
	    mYearSpinner.setAdapter(adapter);
	    mYearSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
				mYear = Integer.parseInt(parent.getItemAtPosition(pos).toString());
				saveSelectedLiterature();
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {
				// TODO Auto-generated method stub
				
			}
	    	
		});
	    
	    int latestYear = 2011;
		mYearSpinner.setSelection(latestYear - mYear);
	    
	}
	
	private void saveSelectedLiterature() {
		String publication = null;
		int newId = 0;
		if(mPlacementType == Literature.TYPE_MAGAZINE) {
			//query for magazine of same issue
			publication = mMagazine+" "+mMonth+"/"+mYear;
			String where = Literature.TYPE +"=? and " + Literature.PUBLICATION + "=?";
			String[] whereArgs = new String[] { ""+mPlacementType, publication };
			Cursor c = getContentResolver().query(Literature.CONTENT_URI, LITERATURE_PROJECTION, where, whereArgs, null);
			if(c != null) {
				if(c.getCount() > 0) {
					c.moveToFirst();
					newId = c.getInt(0);
				}
				c.close();
				c = null;
			}
			//if not found, create it
			if(newId == 0) {
				ContentValues values = new ContentValues();
				values.put(Literature.TYPE, mPlacementType);
				values.put(Literature.PUBLICATION, publication);
				Uri insertedLit = getContentResolver().insert(Literature.CONTENT_URI, values);
				newId = Integer.parseInt(insertedLit.getPathSegments().get(1));
			}
			//store the Lit id for Placement use
			mLitID = newId;
			Log.i(TAG, "Literature Pub: "+publication);
			Log.i(TAG, "Literature ID: "+mLitID);
		} else {
			
		}
	}

	private void deleteEntry() {
		if(mCursor != null) {
			mCursor.close();
			mCursor = null;
			getContentResolver().delete(mUri, null, null);
		}
	}
	
}
