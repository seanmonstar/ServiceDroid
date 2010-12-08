package com.monstarlab.servicedroid.activity;

import com.monstarlab.servicedroid.R;
import com.monstarlab.servicedroid.service.ReminderService;
import com.monstarlab.servicedroid.util.Changelog;

import android.app.Activity;
import android.app.TabActivity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TabHost;

public class ServiceDroidActivity extends TabActivity implements TabHost.OnTabChangeListener {
    
	private TabHost mTabHost;
	
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        mTabHost = getTabHost();
        mTabHost.setOnTabChangedListener(this);
        
        setupTimeActivity();
        setupReturnVisitsActivity();
        setupStatisticsActivity();
        
        showWhatsNew();
        
        //setup reminders
        setupReminderService();
    }

	public void setupTimeActivity() {
		Intent intent = new Intent(this, TimeActivity.class);
        mTabHost.addTab(mTabHost.newTabSpec("time")
                .setIndicator(getString(R.string.time), getResources().getDrawable(R.drawable.clock))
                .setContent(intent));
	}
	
	public void setupReturnVisitsActivity() {
		Intent intent = new Intent(this, ReturnVisitsActivity.class);
        mTabHost.addTab(mTabHost.newTabSpec("rvs")
                .setIndicator(getString(R.string.callbook), getResources().getDrawable(R.drawable.home))
                .setContent(intent));
	}
	
	public void setupStatisticsActivity() {
		Intent intent = new Intent(this, StatisticsActivity.class);
        mTabHost.addTab(mTabHost.newTabSpec("stats")
                .setIndicator(getString(R.string.stats), getResources().getDrawable(R.drawable.calendar))
                .setContent(intent));
	}
	
	public void launchTimeView() {
		Intent i = new Intent(this, TimeActivity.class);
		this.startActivity(i);
	}

	public void onTabChanged(String tabId) {
		Activity activity = getLocalActivityManager().getActivity(tabId);
        if (activity != null) {
            activity.onWindowFocusChanged(true);
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