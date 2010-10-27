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
import android.widget.Toast;
import android.widget.AdapterView.AdapterContextMenuInfo;

import com.monstarlab.servicedroid.R;
import com.monstarlab.servicedroid.model.Models.Calls;
import com.monstarlab.servicedroid.model.Models.ReturnVisits;

public class ReturnVisitsActivity extends ListActivity {
	
	private static final String TAG = "ReturnVisitsActivity";
	
	private static final int MENU_ADD = Menu.FIRST;
	private static final int EDIT_ID  = Menu.FIRST + 1;
	private static final int RETURN_ID  = Menu.FIRST + 2;
	private static final int DELETE_ID = Menu.FIRST + 3;
	
	private static final String[] PROJECTION = new String[] { Calls._ID, Calls.NAME, Calls.ADDRESS };
	
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        Intent intent = getIntent();
		if(intent.getData() == null) {
			intent.setData(Calls.CONTENT_URI);
		}
        
        this.setContentView(R.layout.calls);
        
        
        fillData();
        registerForContextMenu(getListView());
    }
	

	protected void fillData() {
		Cursor c = managedQuery(getIntent().getData(), PROJECTION, null, null, null);
		
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
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case MENU_ADD:
			createCall();
			break;
		}
		
		return super.onOptionsItemSelected(item);
	}
	
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		//menu.setHeaderTitle(title);
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
		
		//TODO - show Toast notification
		
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
