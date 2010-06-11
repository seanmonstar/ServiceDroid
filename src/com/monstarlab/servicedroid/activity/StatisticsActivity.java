package com.monstarlab.servicedroid.activity;

import android.app.Activity;
import android.database.Cursor;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.monstarlab.servicedroid.model.Models.ReturnVisits;
import com.monstarlab.servicedroid.util.TimeUtil;
import com.monstarlab.servicedroid.R;

public class StatisticsActivity extends Activity {
	
	//private static final int MENU_ADD = Menu.FIRST;
	
	//private TimeUtil mTimeHelper;
	
	private static String[] RVProjection = new String[] { ReturnVisits._ID, ReturnVisits.DATE, ReturnVisits.CALL_ID };
	
	private TextView mHoursDisplay;
	private TextView mRvsDisplay;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.stats);
        
       // mTimeHelper = new TimeUtil(this);
        
        mHoursDisplay = (TextView)findViewById(R.id.hours);
        mRvsDisplay = (TextView)findViewById(R.id.rvs);
    }
	
	
	
	@Override
	protected void onResume() {
		super.onResume();
		
		fillData();
	}



	protected void fillData() {
		fillHoursSum();
		fillRVs();
		
	}
	
	protected void fillHoursSum() {
		String sum = TimeUtil.toTimeString(0); //TODO - pass in a month value
		mHoursDisplay.setText(sum);
	}
	
	protected void fillRVs() {
		Cursor c = getContentResolver().query(ReturnVisits.CONTENT_URI, RVProjection, null, null, null); //TODO - pass in month value
		if(c != null) {
			c.moveToFirst();
			//mRvsDisplay.setText(c.getCount()); doesnt work.
		}
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
