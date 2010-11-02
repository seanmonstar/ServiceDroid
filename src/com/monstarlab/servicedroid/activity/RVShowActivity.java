package com.monstarlab.servicedroid.activity;

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
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.monstarlab.servicedroid.R;
import com.monstarlab.servicedroid.model.Models.Calls;
import com.monstarlab.servicedroid.model.Models.Literature;
import com.monstarlab.servicedroid.model.Models.Placements;
import com.monstarlab.servicedroid.model.Models.ReturnVisits;
import com.monstarlab.servicedroid.util.TimeUtil;

public class RVShowActivity extends Activity {
	
	private static final String TAG = "RVShowActivity";
	
	private static final int MENU_EDIT = Menu.FIRST;
	private static final int MENU_PLACEMENT = Menu.FIRST + 1;
	private static final int MENU_RETURN = Menu.FIRST + 2;
	private static final int MENU_STUDY = Menu.FIRST + 3;
	
	
	private static final String[] PROJECTION = new String[] { Calls._ID, Calls.NAME, Calls.ADDRESS, Calls.NOTES, Calls.DATE, Calls.BIBLE_STUDY };
	private static final int ID_COLUMN = 0;
	private static final int NAME_COLUMN = 1;
	private static final int ADDRESS_COLUMN = 2;
	private static final int NOTES_COLUMN = 3; 
	private static final int DATE_COLUMN = 4;
	private static final int STUDY_COLUMN = 5;

	private static final int DIALOG_PLACEMENT_ID = 0;
	
	private Uri mUri;
	private TextView mNameText;
	private TextView mAddressText;
	private TextView mLastVisitText;
	private TextView mNotesText;
	private Cursor mCursor;
	private TimeUtil mTimeHelper;
	private CheckBox mBibleStudyCheckbox;

	//private ListView mListView;
	
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		final Intent intent = getIntent();
		//final String action = intent.getAction();
		mUri = intent.getData();
		
		
		setContentView(R.layout.call_show);
		
		mNameText = (TextView) findViewById(R.id.name);
		mAddressText = (TextView) findViewById(R.id.address);
		mLastVisitText = (TextView) findViewById(R.id.lastVisit);
		mNotesText = (TextView) findViewById(R.id.notes);
		
		//mListView = (ListView) findViewById(R.id.rv_data);
		//mListView.setEmptyView((TextView) findViewById(android.R.id.empty));
		
		mBibleStudyCheckbox = (CheckBox) findViewById(R.id.is_bible_study);
		mBibleStudyCheckbox.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				updateStudyStatus(((CheckBox)v).isChecked());
			}
		});
		
		mTimeHelper = new TimeUtil(this);
		
		mCursor = managedQuery(mUri, PROJECTION, null, null, null);
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		
		//mListView.
		
		if(mCursor != null) {
			mCursor.moveToFirst();
			
			String name = mCursor.getString(NAME_COLUMN);
			String address = mCursor.getString(ADDRESS_COLUMN);
			String notes = mCursor.getString(NOTES_COLUMN);
			boolean isBibleStudy = ( mCursor.getInt(STUDY_COLUMN) != 0 );
			
			updateLastVisited();
			
			
			mNameText.setText(name);
			mAddressText.setText(address);
			mNotesText.setText(notes);
			mBibleStudyCheckbox.setChecked(isBibleStudy);
		}
		
		
	}
	
	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        boolean result = super.onCreateOptionsMenu(menu);
        menu.add(0, MENU_EDIT, 1, R.string.edit).setIcon(android.R.drawable.ic_menu_edit);
        
        menu.add(0, MENU_PLACEMENT, 2, R.string.placement).setIcon(android.R.drawable.ic_menu_agenda);
        menu.add(0, MENU_RETURN, 3, "Return").setIcon(android.R.drawable.ic_menu_myplaces);
        //menu.add(0, MENU_STUDY, 4, R.string.bible_study).setIcon(android.R.drawable.ic_menu_search);
        return result;
    }
	
	@Override
	protected Dialog onCreateDialog(int id) {
		Dialog dialog;
	    switch(id) {
	    case DIALOG_PLACEMENT_ID:
	    	dialog = makePlacementDialog();
	        break;
	    default:
	        dialog = null;
	    }
	    return dialog;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case MENU_EDIT:
			editCall();
			break;
		case MENU_PLACEMENT:
			showDialog(DIALOG_PLACEMENT_ID);
			break;
		case MENU_RETURN:
			returnOnCall();
			break;
		case MENU_STUDY:
			
			break;
		}
		
		return super.onOptionsItemSelected(item);
	}

	protected void editCall() {
		Intent i = new Intent(Intent.ACTION_EDIT, mUri, this, RVEditActivity.class);
        startActivity(i);
	}
	
	protected Dialog makePlacementDialog() {
		final CharSequence[] items = { getString(R.string.magazine), getString(R.string.brochure), getString(R.string.book) };
		final int MAGAZINE = 0;
		final int BROCHURE = 1;
		final int BOOK = 2;

		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		
		builder.setTitle(getString(R.string.placement));
		builder.setItems(items, new DialogInterface.OnClickListener() {
		    public void onClick(DialogInterface dialog, int itemIndex) {
		        switch(itemIndex) {
		        case MAGAZINE:
		        	makeMagazinePlacement();
		        	break;
		        case BROCHURE:
		        	makeBrochurePlacement();
		        	break;
		        case BOOK:
		        	makeBookPlacement();
		        	break;
		        default:
		        	break;
		        }
		    }
		});
		return builder.create();

	}
	
	protected void makeBookPlacement() {
		Intent i = new Intent(Intent.ACTION_INSERT, Placements.CONTENT_URI, this, PlacementActivity.class);
		
		if(mCursor != null) {
			mCursor.moveToFirst();
			i.putExtra(Calls._ID, mCursor.getInt(ID_COLUMN));
		}
		
		i.putExtra("type", Literature.TYPE_BOOK);
		
		
		
		startActivity(i);
		
	}

	protected void makeBrochurePlacement() {
		Intent i = new Intent(Intent.ACTION_INSERT, Placements.CONTENT_URI, this, PlacementActivity.class);
		
		if(mCursor != null) {
			mCursor.moveToFirst();
			i.putExtra(Calls._ID, mCursor.getInt(ID_COLUMN));
		}
		
		i.putExtra("type", Literature.TYPE_BROCHURE);
		
		
		
		startActivity(i);
		
	}

	protected void makeMagazinePlacement() {
		Intent i = new Intent(Intent.ACTION_INSERT, Placements.CONTENT_URI, this, PlacementActivity.class);
		
		if(mCursor != null) {
			mCursor.moveToFirst();
			i.putExtra(Calls._ID, mCursor.getInt(ID_COLUMN));
		}
		
		i.putExtra("type", Literature.TYPE_MAGAZINE);
		
		
		
		startActivity(i);
		
	}
	
	protected void updateStudyStatus(boolean isStudy) {
		if(mUri != null) {
			ContentValues values = new ContentValues();
			values.put(Calls.BIBLE_STUDY, isStudy);
			getContentResolver().update(mUri, values, null, null);
			
			
			String name = mCursor.getString(NAME_COLUMN);
			String status = isStudy ? "now" : "no longer";
			String text = name + " is " + status + " a bible study.";
			Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT).show();
		}
	}

	protected void returnOnCall() {
		if(mCursor != null) {
			mCursor.moveToFirst();
		
			ContentValues values = new ContentValues();
			values.put(ReturnVisits.CALL_ID, mCursor.getInt(ID_COLUMN));
			getContentResolver().insert(ReturnVisits.CONTENT_URI, values);
			
			
			String name = mCursor.getString(NAME_COLUMN);
			String text = "You made a return visit on " + name;
			Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT).show();
			updateLastVisited();
		}
	}
	
	protected void updateLastVisited() {
		if(mCursor != null) {
			//check the most recent Return Visit			
			String lastVisit = getLatestReturn();
			
			//if there has been no return visit
			if(lastVisit == null) {
				//show the date the Call was first found home
				mCursor.moveToFirst();
				lastVisit = mCursor.getString(DATE_COLUMN);
			}
			
			mLastVisitText.setText("Last Visited: " + mTimeHelper.normalizeDate(lastVisit));
		}
	}
	
	protected String getLatestReturn() {
		String date = null;
		if(mCursor != null) {
			mCursor.moveToFirst();
			
			Cursor c = getContentResolver().query(ReturnVisits.CONTENT_URI, new String[] { ReturnVisits.DATE }, "call_id = ?", new String[] { mCursor.getString(ID_COLUMN) }, "date desc");
			if(c != null) {
				if(c.getCount() > 0) {
					c.moveToFirst();
					
					date = c.getString(0);
				}
				c.close();
				c = null;
			}
		}
		
		return date;
	}

}
