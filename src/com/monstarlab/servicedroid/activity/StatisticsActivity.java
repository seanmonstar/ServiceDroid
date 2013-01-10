package com.monstarlab.servicedroid.activity;

import java.util.Calendar;
import java.util.Date;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.widget.ShareActionProvider;
import com.monstarlab.servicedroid.fragment.StatisticsFragment;
import com.monstarlab.servicedroid.model.Models.Literature;
import com.monstarlab.servicedroid.model.Models.Placements;
import com.monstarlab.servicedroid.model.Models.ReturnVisits;
import com.monstarlab.servicedroid.model.Models.TimeEntries;
import com.monstarlab.servicedroid.service.BackupService;
import com.monstarlab.servicedroid.util.TimeUtil;
import com.monstarlab.servicedroid.R;


public class StatisticsActivity extends SherlockActivity {

	

	@Override
    public void onCreate(Bundle savedInstanceState) {
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {	
		switch(item.getItemId()) {

		case android.R.id.home:
			Intent i = new Intent(this, ServiceDroidActivity.class);
			i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(i);
			return true;
		
		}
		
		return super.onOptionsItemSelected(item);
	}
	
	@Override
	protected Dialog onCreateDialog(int id) {
		Dialog dialog;
	    switch(id) {
	    case StatisticsFragment.DIALOG_ROUND_ID:
	    	dialog = makeRoundTimeDialog();
	        break;
	    default:
	        dialog = null;
	    }
	    return dialog;
	}
	
}
