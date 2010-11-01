package com.monstarlab.servicedroid.activity;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

public class PlacementActivity extends Activity {

	private static final int STATE_INSERT = 0;
	private static final int STATE_EDIT = 1;
	private static final String TAG = "PlacementActivity";
	private int mState;
	private Uri mUri;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
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
		
		
	}
	
	@Override
	protected void onResume() {
		super.onResume();
	}
	
	@Override
	protected void onPause() {
		super.onPause();
	}
	
}
