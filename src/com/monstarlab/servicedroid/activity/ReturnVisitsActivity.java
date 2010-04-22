package com.monstarlab.servicedroid.activity;

import android.app.ListActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

public class ReturnVisitsActivity extends ListActivity {
	
	private static final int MENU_ADD = Menu.FIRST;
	//private static final int MENU_ADD = Menu.FIRST + 1;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //this.setContentView(R.layout.calls);
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
			addCall();
			break;
		}
		
		return super.onOptionsItemSelected(item);
	}

	protected void addCall() {
		// TODO Auto-generated method stub
		
	}
	
}
