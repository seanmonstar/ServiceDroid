package com.monstarlab.servicedroid.activity;

import android.app.ListActivity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.SimpleCursorAdapter;

import com.monstarlab.servicedroid.R;
import com.monstarlab.servicedroid.model.Models.ReturnVisits;

public class ReturnVisitsActivity extends ListActivity {
	
	private static final int MENU_ADD = Menu.FIRST;
	//private static final int MENU_ADD = Menu.FIRST + 1;
	
	private static final String[] PROJECTION = new String[] { ReturnVisits._ID, ReturnVisits.NAME, ReturnVisits.ADDRESS };
    
    //private Cursor mCursor;
	
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

	protected void createCall() {
		Intent i = new Intent(Intent.ACTION_INSERT, getIntent().getData(), this, RVEditActivity.class);
		startActivity(i);
	}

	
}
