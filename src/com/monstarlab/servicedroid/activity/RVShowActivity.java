package com.monstarlab.servicedroid.activity;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.ListView;
import android.widget.TextView;

import com.monstarlab.servicedroid.R;
import com.monstarlab.servicedroid.model.Models.Calls;

public class RVShowActivity extends Activity {
	
	private static final String TAG = "RVShowActivity";
	
	private static final int MENU_EDIT = Menu.FIRST;
	
	private static final String[] PROJECTION = new String[] { Calls._ID, Calls.NAME, Calls.ADDRESS, Calls.NOTES };
	private static final int NAME_COLUMN = 1;
	private static final int ADDRESS_COLUMN = 2;
	private static final int NOTES_COLUMN = 3;
	
	private Uri mUri;
	private TextView mNameText;
	private TextView mAddressText;
	private Cursor mCursor;

	private ListView mListView;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		final Intent intent = getIntent();
		//final String action = intent.getAction();
		mUri = intent.getData();
		
		
		setContentView(R.layout.call_show);
		
		mNameText = (TextView) findViewById(R.id.name);
		mAddressText = (TextView) findViewById(R.id.address);
		mListView = (ListView) findViewById(R.id.rv_data);
		mListView.setEmptyView((TextView) findViewById(android.R.id.empty));
		
		mCursor = managedQuery(mUri, PROJECTION, null, null, null);
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		
		//mListView.
		
		if(mCursor != null) {
			mCursor.moveToFirst();
			
			String name = mCursor.getString(NAME_COLUMN);
			String address = mCursor.getString(ADDRESS_COLUMN);
			//String notes = mCursor.getString(NOTES_COLUMN);
			
			mNameText.setText(name);
			mAddressText.setText(address);
			
		}
		
		
	}
	
	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        boolean result = super.onCreateOptionsMenu(menu);
        menu.add(0, MENU_EDIT, 1, "Edit Call");
        return result;
    }
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case MENU_EDIT:
			editCall();
			break;
		}
		
		return super.onOptionsItemSelected(item);
	}

	private void editCall() {
		// TODO Auto-generated method stub
		
	}

}
