package com.monstarlab.servicedroid.activity;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.monstarlab.servicedroid.util.TimeUtil;
import com.monstarlab.servicedroid.R;

public class StatisticsActivity extends Activity {
	
	//private static final int MENU_ADD = Menu.FIRST;
	
	//private TimeUtil mTimeHelper;
	
	private TextView mHoursDisplay;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.stats);
        
       // mTimeHelper = new TimeUtil(this);
        
        mHoursDisplay = (TextView)findViewById(R.id.hours);
        
        fillData();
    }
	
	
	
	@Override
	protected void onResume() {
		super.onResume();
		
		fillData();
	}



	protected void fillData() {
		fillHoursSum();
		
		
	}
	
	protected void fillHoursSum() {
		String sum = TimeUtil.toTimeString(0); //TODO - pass in a month value
		mHoursDisplay.setText(sum);
	}
	
	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return super.onCreateOptionsMenu(menu);
    }
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {		
		return super.onOptionsItemSelected(item);
	}
	
}
