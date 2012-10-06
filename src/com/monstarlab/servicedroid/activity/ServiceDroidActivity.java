package com.monstarlab.servicedroid.activity;

import com.actionbarsherlock.app.SherlockActivity;
import com.monstarlab.servicedroid.R;
import com.monstarlab.servicedroid.service.ReminderService;
import com.monstarlab.servicedroid.util.Changelog;

import android.app.Activity;
import android.view.View;
import android.content.Intent;
import android.os.Bundle;

public class ServiceDroidActivity extends SherlockActivity implements View.OnClickListener {
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.main);
        
        findViewById(R.id.time).setOnClickListener(this);
        findViewById(R.id.calls).setOnClickListener(this);
        findViewById(R.id.stats).setOnClickListener(this);
        
        showWhatsNew();
        
        //setup reminders
        setupReminderService();
    }
	
	public void onClick(View view) {
		Intent i = null;
		
		switch (view.getId()) {
		
		case R.id.time:
			i = new Intent(this, TimeActivity.class);
			break;
		
		case R.id.calls:
			i = new Intent(this, CallsActivity.class);
			break;
			
		case R.id.stats:
			i = new Intent(this, StatisticsActivity.class);
			break;
			
		default:
			// tapped something else? whatevs...
		}
		
		if (i != null) {
			startActivity(i);
		}
	}
	
	private void setupReminderService() {
		Intent i = new Intent(this, ReminderService.class);
		startService(i);
	}
	
	private void showWhatsNew() {
		Changelog.showFirstTime(this);
	}

	
}