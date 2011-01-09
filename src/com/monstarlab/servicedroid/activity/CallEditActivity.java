package com.monstarlab.servicedroid.activity;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.monstarlab.servicedroid.R;
import com.monstarlab.servicedroid.model.Models.Calls;

public class CallEditActivity extends Activity {

	private static final String TAG = "CallEditActivity";
	
	private static final int STATE_EDIT = 0;
	private static final int STATE_INSERT = 1;
	
	private EditText mNameText;
	private EditText mAddressText;
	private EditText mNotesText;
	//private Long mRowId;
	private Uri mUri;
	private int mState;
	private boolean mIsCancelled;

	private Cursor mCursor;
	
	private static final String[] PROJECTION = new String[] { Calls._ID, Calls.NAME, Calls.ADDRESS, Calls.NOTES };
	private static final int NAME_COLUMN = 1;
	private static final int ADDRESS_COLUMN = 2;
	private static final int NOTES_COLUMN = 3;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		//load in the values, if editting
		final Intent intent = getIntent();
		final String action = intent.getAction();
		if(Intent.ACTION_EDIT.equals(action)) {
			mState = STATE_EDIT;
			mUri = intent.getData();
		} else if (Intent.ACTION_INSERT.equals(action)) {
			mState = STATE_INSERT;
			mUri = getContentResolver().insert(intent.getData(), null);
			
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
		
		setContentView(R.layout.call_edit);
		
		mNameText = (EditText) findViewById(R.id.name);
		mAddressText = (EditText) findViewById(R.id.address);
		mNotesText = (EditText) findViewById(R.id.notes);
		
		mCursor = managedQuery(mUri, PROJECTION, null, null, null);
		
		
		Button confirmButton = (Button) findViewById(R.id.confirm);
		confirmButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View arg0) {
				finish();
			}
			
		});
		
		/*Button cancelButton = (Button) findViewById(R.id.cancel);
		cancelButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				mIsCancelled = true;
				finish();
				
			}
		});*/
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		
		if(mCursor != null) {
			mCursor.moveToFirst(); //make sure we're on the only position
			
			String name = mCursor.getString(NAME_COLUMN);
			String address = mCursor.getString(ADDRESS_COLUMN);
			String notes = mCursor.getString(NOTES_COLUMN);
			
			mNameText.setText(name);
			mAddressText.setText(address);
			mNotesText.setText(notes);
		}
		
		
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		//this happens whenever the Activity is losing focus, like hitting the Home button,
		//or when the user has clicked Confirm
		
		
		if(mCursor != null) {
			String name = mNameText.getText().toString();
			String address = mAddressText.getText().toString();
			String notes = mNotesText.getText().toString();
			
			
			if(isFinishing() && TextUtils.isEmpty(name)) {
				//when finishing, if no Name, its useless anyways
				setResult(RESULT_CANCELED);
				deleteRV();
			} else if (isFinishing() && mIsCancelled) {
				//if we cancelled, just dont save the changes
				setResult(RESULT_CANCELED);
			} else {
				//save the current changes to the Provider
				ContentValues values = new ContentValues();
				values.put(Calls.NAME, name);
				values.put(Calls.ADDRESS, address);
				values.put(Calls.NOTES, notes);
				
				getContentResolver().update(mUri, values, null, null);
			}
		}
	}

	private void deleteRV() {
		if(mCursor != null) {
			mCursor.close();
			mCursor = null;
			getContentResolver().delete(mUri, null, null);
		}
	}
}
