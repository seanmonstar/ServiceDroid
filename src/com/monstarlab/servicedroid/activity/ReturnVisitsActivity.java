package com.monstarlab.servicedroid.activity;

import android.app.ListActivity;
import android.content.ContentUris;
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
import android.widget.AdapterView.AdapterContextMenuInfo;

import com.monstarlab.servicedroid.R;
import com.monstarlab.servicedroid.model.Models.ReturnVisits;

public class ReturnVisitsActivity extends ListActivity {
	
	private static final int MENU_ADD = Menu.FIRST;
	private static final int DELETE_ID = Menu.FIRST + 1;
	
	private static final String[] PROJECTION = new String[] { ReturnVisits._ID, ReturnVisits.NAME, ReturnVisits.ADDRESS };
	
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        Intent intent = getIntent();
		if(intent.getData() == null) {
			intent.setData(ReturnVisits.CONTENT_URI);
		}
        
        this.setContentView(R.layout.calls);
        
        
        fillData();
        registerForContextMenu(getListView());
    }
	

	protected void fillData() {
		Cursor c = managedQuery(getIntent().getData(), PROJECTION, null, null, null);
		
		String[] from = new String[]{ ReturnVisits.NAME, ReturnVisits.ADDRESS };
		int[] to = new int[]{ R.id.name, R.id.address };
		
		SimpleCursorAdapter rvs = new SimpleCursorAdapter(this, R.layout.call_row, c, from, to);
		
		setListAdapter(rvs);
	}
	
	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        boolean result = super.onCreateOptionsMenu(menu);
        menu.add(0, MENU_ADD, 1, "Add Call");
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
        menu.add(0, DELETE_ID, 0, R.string.delete_call);
	}
	
	@Override
	public boolean onContextItemSelected(MenuItem item) {
		switch(item.getItemId()) {
		case DELETE_ID:
			AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
			Uri callUri = ContentUris.withAppendedId(getIntent().getData(), info.id);
			getContentResolver().delete(callUri, null, null);
			//fillData();
			return true;
		}
		
		
		return super.onContextItemSelected(item);
	}
	
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		//super.onListItemClick(l, v, position, id);
		Uri uri = ContentUris.withAppendedId(getIntent().getData(), id);
		Intent i = new Intent(Intent.ACTION_EDIT, uri, this, RVEditActivity.class);
        startActivity(i);
	}
	
	
	protected void createCall() {
		Intent i = new Intent(Intent.ACTION_INSERT, getIntent().getData(), this, RVEditActivity.class);
		startActivity(i);
	}

	
}
