package com.monstarlab.servicedroid.activity;

import android.app.Activity;
import android.content.ContentUris;
import android.content.ContentValues;
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
import android.widget.Toast;

import com.monstarlab.servicedroid.R;
import com.monstarlab.servicedroid.model.Models.Calls;
import com.monstarlab.servicedroid.model.Models.ReturnVisits;

public class RVShowActivity extends Activity {
	
	private static final String TAG = "RVShowActivity";
	
	private static final int MENU_EDIT = Menu.FIRST;
	private static final int MENU_PLACEMENT = Menu.FIRST + 1;
	private static final int MENU_RETURN = Menu.FIRST + 2;
	private static final int MENU_STUDY = Menu.FIRST + 3;
	
	
	private static final String[] PROJECTION = new String[] { Calls._ID, Calls.NAME, Calls.ADDRESS, Calls.NOTES };
	private static final int ID_COLUMN = 0;
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
        menu.add(0, MENU_EDIT, 1, R.string.edit).setIcon(android.R.drawable.ic_menu_edit);
        
        menu.add(0, MENU_PLACEMENT, 2, "Placement").setIcon(android.R.drawable.ic_menu_agenda);
        menu.add(0, MENU_RETURN, 3, "Return").setIcon(android.R.drawable.ic_menu_myplaces);
        menu.add(0, MENU_STUDY, 4, "Bible Study").setIcon(android.R.drawable.ic_menu_search);
        return result;
    }
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case MENU_EDIT:
			editCall();
			break;
		case MENU_PLACEMENT:
			makePlacement();
			break;
		case MENU_RETURN:
			returnOnCall();
			break;
		case MENU_STUDY:
			
			break;
		}
		
		return super.onOptionsItemSelected(item);
	}

	protected void editCall() {
		Intent i = new Intent(Intent.ACTION_EDIT, mUri, this, RVEditActivity.class);
        startActivity(i);
	}
	
	protected void makePlacement() {
		//TODO - make a placement
	}
	
	protected void returnOnCall() {
		if(mCursor != null) {
			mCursor.moveToFirst();
		
			ContentValues values = new ContentValues();
			values.put(ReturnVisits.CALL_ID, mCursor.getInt(ID_COLUMN));
			getContentResolver().insert(ReturnVisits.CONTENT_URI, values);
			
			
			String name = mCursor.getString(NAME_COLUMN);
			String text = "You made a return visit on " + name;
			Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT).show();
		}
	}

}
