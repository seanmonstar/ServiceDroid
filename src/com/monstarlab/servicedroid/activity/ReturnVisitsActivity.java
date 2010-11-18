package com.monstarlab.servicedroid.activity;

import android.app.ListActivity;
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
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.AdapterContextMenuInfo;

import com.monstarlab.servicedroid.R;
import com.monstarlab.servicedroid.model.Models.Calls;
import com.monstarlab.servicedroid.model.Models.ReturnVisits;

public class ReturnVisitsActivity extends ListActivity {
	
	private static final String TAG = "ReturnVisitsActivity";
	
	private static final int MENU_ADD = Menu.FIRST;
	private static final int MENU_SORT_ALPHA = Menu.FIRST + 1;
	private static final int MENU_SORT_TIME = Menu.FIRST + 2;
	private static final int EDIT_ID  = Menu.FIRST + 3;
	private static final int RETURN_ID  = Menu.FIRST + 4;
	private static final int DELETE_ID = Menu.FIRST + 5;
	
	private static final String[] PROJECTION = new String[] { Calls._ID, Calls.NAME, Calls.ADDRESS };
	
	private static final int SORT_ALPHA = 0;
	private static final int SORT_TIME = 1;

	private int mSortState;
	
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        Intent intent = getIntent();
		if(intent.getData() == null) {
			intent.setData(Calls.CONTENT_URI);
		}
        
        this.setContentView(R.layout.calls);
        
        //initial sort state will be alphabetically
        mSortState = SORT_ALPHA;
        
        fillData();
        registerForContextMenu(getListView());
    }
	

	protected void fillData() {
		String sortBy = null;
		if(mSortState == SORT_ALPHA) {
			sortBy = Calls.NAME;
		} else if (mSortState == SORT_TIME) {
			sortBy = Calls.DATE;
		}
		
		Cursor c = managedQuery(getIntent().getData(), PROJECTION, null, null, sortBy);
		
		String[] from = new String[]{ Calls.NAME, Calls.ADDRESS };
		int[] to = new int[]{ R.id.name, R.id.address };
		
		SimpleCursorAdapter rvs = new SimpleCursorAdapter(this, R.layout.call_row, c, from, to);
		
		setListAdapter(rvs);
	}
	
	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        boolean result = super.onCreateOptionsMenu(menu);
        menu.add(0, MENU_ADD, 1, "Add Call").setIcon(android.R.drawable.ic_menu_add);
        return result;
    }
	
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		super.onPrepareOptionsMenu(menu);
		
		//menu depends on if how data is sorted
		if(mSortState == SORT_ALPHA) {
			menu.removeItem(MENU_SORT_ALPHA);
			if(menu.findItem(MENU_SORT_TIME) == null) {
				menu.add(0, MENU_SORT_TIME, 2, R.string.sort).setIcon(android.R.drawable.ic_menu_recent_history);
			}
		} else if(mSortState == SORT_TIME){
			menu.removeItem(MENU_SORT_TIME);
			if(menu.findItem(MENU_SORT_ALPHA) == null) {
				menu.add(0, MENU_SORT_ALPHA, 2, R.string.sort).setIcon(android.R.drawable.ic_menu_sort_alphabetically);
			}
		}
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case MENU_ADD:
			createCall();
			break;
		case MENU_SORT_ALPHA:
			mSortState = SORT_ALPHA;
			fillData();
			break;
		case MENU_SORT_TIME:
			mSortState = SORT_TIME;
			fillData();
			break;
		}
		
		return super.onOptionsItemSelected(item);
	}
	
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		
		String name = ((TextView)((AdapterContextMenuInfo)menuInfo).targetView.findViewById(R.id.name)).getText().toString();
		
		menu.setHeaderTitle(name);
		menu.add(0, EDIT_ID, 0, R.string.edit);
		menu.add(0, RETURN_ID, 1, R.string.make_return);
        menu.add(0, DELETE_ID, 2, R.string.delete_call);
	}
	
	@Override
	public boolean onContextItemSelected(MenuItem item) {
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
		
		switch(item.getItemId()) {
		case EDIT_ID:
			editCall(info.id);
			return true;
		case RETURN_ID:
			returnOnCall(info.id);
			return true;
		case DELETE_ID:
			deleteCall(info.id);
			
			//fillData();
			return true;
		}
		
		
		return super.onContextItemSelected(item);
	}
	
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		//super.onListItemClick(l, v, position, id);
		editCall(id);
	}
	
	protected void deleteCall(long id) {
		Uri callUri = ContentUris.withAppendedId(getIntent().getData(), id);
		getContentResolver().delete(callUri, null, null);
	}
	
	protected void editCall(long id) {
		Uri uri = ContentUris.withAppendedId(getIntent().getData(), id);
		Intent i = new Intent(Intent.ACTION_EDIT, uri, this, RVShowActivity.class);
        startActivity(i);
	}
	
	
	protected void createCall() {
		Intent i = new Intent(Intent.ACTION_INSERT, getIntent().getData(), this, RVEditActivity.class);
		startActivity(i);
	}
	
	protected void returnOnCall(long id) {
		ContentValues values = new ContentValues();
		values.put(ReturnVisits.CALL_ID, id);
		getContentResolver().insert(ReturnVisits.CONTENT_URI, values);
		
		String name = getCallName(id);
		String text = "You made a return visit on " + name;
		Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT).show();
	}
	
	protected String getCallName(long id) {
		Uri uri = ContentUris.withAppendedId(getIntent().getData(), id);
		Cursor c = getContentResolver().query(uri, new String[] { Calls.NAME }, null, null, null);
		if(c != null) {
			c.moveToFirst();
			String name = c.getString(0);
			c.close();
			return name;
		}
		return "";
	}

	
}
