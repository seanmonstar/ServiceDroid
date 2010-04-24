package com.monstarlab.servicedroid.activity;

import com.monstarlab.servicedroid.R;
import com.monstarlab.servicedroid.model.TimeEntryAdapter;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

public class ReturnVisitsActivity extends ListActivity {
	
	private static final int MENU_ADD = Menu.FIRST;
	//private static final int MENU_ADD = Menu.FIRST + 1;
	
	private static final int ACTIVITY_CREATE=0;
    private static final int ACTIVITY_EDIT=1;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.calls);
        fillData();
    }
	

	protected void fillData() {
		// TODO Auto-generated method stub
		
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
		Intent i = new Intent(this, RVEditActivity.class);
		startActivityForResult(i, ACTIVITY_CREATE);
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
		super.onActivityResult(requestCode, resultCode, intent);
		
		//intent is null if user pressed BACK
		if(intent != null) {
			Bundle extras = intent.getExtras();
			switch(requestCode) {
			case ACTIVITY_CREATE:
				fillData();
				break;
			case ACTIVITY_EDIT:
				fillData();
				break;
			}
		}
	}

	
}
