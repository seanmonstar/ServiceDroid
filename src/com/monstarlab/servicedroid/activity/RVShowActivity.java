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
import android.text.TextUtils;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;

import com.monstarlab.servicedroid.R;
import com.monstarlab.servicedroid.model.Models.BibleStudies;
import com.monstarlab.servicedroid.model.Models.Calls;
import com.monstarlab.servicedroid.model.Models.Literature;
import com.monstarlab.servicedroid.model.Models.Placements;
import com.monstarlab.servicedroid.model.Models.ReturnVisits;
import com.monstarlab.servicedroid.util.Changelog;
import com.monstarlab.servicedroid.util.TimeUtil;

public class RVShowActivity extends Activity implements OnItemClickListener {
	
	private static final String TAG = "RVShowActivity";
	
	private static final int MENU_EDIT = Menu.FIRST;
	private static final int MENU_PLACEMENT = Menu.FIRST + 1;
	private static final int MENU_RETURN = Menu.FIRST + 2;
	private static final int MENU_STUDY = Menu.FIRST + 3;
	
	
	private static final String[] PROJECTION = new String[] { Calls._ID, Calls.NAME, Calls.ADDRESS, Calls.NOTES, Calls.DATE, Calls.TYPE };
	private static final String[] BIBLE_STUDY_PROJECTION = new String[] { BibleStudies._ID, BibleStudies.DATE_START, BibleStudies.DATE_END, BibleStudies.CALL_ID };
	private static final String[] PLACEMENTS_PROJECTION = new String[] { Placements._ID, Placements.LITERATURE_ID, Placements.CALL_ID, Literature.PUBLICATION, Placements.DATE };
	private static final int ID_COLUMN = 0;
	private static final int NAME_COLUMN = 1;
	private static final int ADDRESS_COLUMN = 2;
	private static final int NOTES_COLUMN = 3; 
	private static final int DATE_COLUMN = 4;
	private static final int TYPE_COLUMN = 5;

	private static final int DIALOG_PLACEMENT_ID = 0;
	private static final int DIALOG_BIBLESTUDYDELETE_ID = 1;

	private static final int EDIT_ID = 0;
	private static final int DELETE_ID = 1;

	
	
	private Uri mUri;
	private TextView mNameText;
	private TextView mAddressText;
	private TextView mLastVisitText;
	private TextView mNotesText;
	private Cursor mCursor;
	private TimeUtil mTimeHelper;
	private CheckBox mBibleStudyCheckbox;

	private ListView mListView;

	private Cursor mPlacementsCursor;
	
	private int mCallType = Calls.TYPE_ADDED;

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
		

		
		mListView = (ListView) findViewById(R.id.placements_list);
		mListView.setEmptyView((TextView) findViewById(android.R.id.empty));
        mListView.setOnCreateContextMenuListener(this);
        mListView.setScrollBarStyle(ListView.SCROLLBARS_OUTSIDE_OVERLAY);
        mListView.setOnItemClickListener(this);
		
		mBibleStudyCheckbox = (CheckBox) findViewById(R.id.is_bible_study);
		mBibleStudyCheckbox.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				updateStudyStatus(((CheckBox)v).isChecked());
			}
		});
		
		mTimeHelper = new TimeUtil(this);
		
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		
		refreshCallData();
		refreshPlacementList();
		
	}
	
	private void refreshCallData() {
		mCursor = managedQuery(mUri, PROJECTION, null, null, null);
		if(mCursor != null) {
			if(mCursor.getCount() == 1) {
			
				mCursor.moveToFirst();
				
				String name = mCursor.getString(NAME_COLUMN);
				String address = mCursor.getString(ADDRESS_COLUMN);
				String notes = mCursor.getString(NOTES_COLUMN);
				mCallType = mCursor.getInt(TYPE_COLUMN);
				boolean isBibleStudy = isBibleStudy();
				
				//user created Calls show all data
				//system created Calls might behave differently
				switch(mCallType) {
				
				case Calls.TYPE_ANONYMOUS:
					mNameText.setText(name);
					mBibleStudyCheckbox.setVisibility(View.GONE);
					mAddressText.setVisibility(View.GONE);
					mNotesText.setVisibility(View.GONE);
					mLastVisitText.setVisibility(View.GONE);
					break;
				default:
					mNameText.setText(name);
					mAddressText.setText(address);
					mNotesText.setText(notes);
					mBibleStudyCheckbox.setChecked(isBibleStudy);
					updateLastVisited();
					break;
				
				}
				
				
				
				
				
			} else {
				finish();
			}
		}
	}
	
	private boolean isBibleStudy() {
		boolean isStudy = false;
		
		if(mUri != null) {
			Cursor c = getCurrentBibleStudyCursor();
			if(c.getCount() > 0) {
				//if we got results, then there is a bible study
				isStudy = true;
			}
			c.close();
			c = null;
		}
		
		return isStudy;
	}
	
	private Cursor getCurrentBibleStudyCursor() {
		String callId = mUri.getPathSegments().get(1);
		
		// isBibleStudy if Bible Study exists and hasn't ended yet
		String where = BibleStudies.CALL_ID + "=? and " + BibleStudies.DATE_END + " isnull";
		String[] whereArgs = new String[] { callId };
		return getContentResolver().query(BibleStudies.CONTENT_URI, BIBLE_STUDY_PROJECTION, where, whereArgs, null);
	}
	
	private void refreshPlacementList() {
		mPlacementsCursor = managedQuery(Placements.DETAILS_CONTENT_URI, PLACEMENTS_PROJECTION, Placements.CALL_ID + "=?", new String[] { mUri.getPathSegments().get(1) }, "placements.date DESC");
		
		if(mPlacementsCursor != null) {
			String[] from = new String[]{ Literature.PUBLICATION, Placements.DATE };
			int[] to = new int[]{ R.id.name, R.id.date };
			
			SimpleCursorAdapter adapter = new SimpleCursorAdapter(this, R.layout.placement_row, mPlacementsCursor, from, to) {
				@Override
	        	public void setViewText(TextView v, String text) {
	        		if (v.getId() == R.id.date) {
						text = mTimeHelper.normalizeDate(text);
	        			v.setText(text);
	        		} else {
	        			super.setViewText(v, text);
	        		}
	        		
	        	}
			};
			
			mListView.setAdapter(adapter);
		}
	}
	
	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        boolean result = super.onCreateOptionsMenu(menu);
        
        
        menu.add(0, MENU_EDIT, 1, R.string.edit).setIcon(android.R.drawable.ic_menu_edit);
        
        
        menu.add(0, MENU_RETURN, 3, "Return").setIcon(android.R.drawable.ic_menu_myplaces);
        
        menu.add(0, MENU_PLACEMENT, 2, R.string.placement).setIcon(android.R.drawable.ic_menu_agenda);
        
        return result;
    }
	
	@Override
	protected Dialog onCreateDialog(int id) {
		Dialog dialog;
	    switch(id) {
	    case DIALOG_PLACEMENT_ID:
	    	dialog = makePlacementDialog();
	        break;
	    case DIALOG_BIBLESTUDYDELETE_ID:
	    	dialog = makeBibleStudyDeleteDialog();
	    	break;
	    default:
	        dialog = null;
	    }
	    return dialog;
	}
	
	private Dialog makeBibleStudyDeleteDialog() {

		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		
		//builder.setTitle(getString(R.string.placement));
		builder.setMessage(getString(R.string.bible_study_prompt))
	       .setCancelable(false)
	       .setPositiveButton(getString(R.string.bible_study_prompt_finish), new DialogInterface.OnClickListener() {
	           public void onClick(DialogInterface dialog, int id) {
	               finishBibleStudy(); 
	           }
	       })
	       .setNegativeButton(getString(R.string.bible_study_prompt_delete), new DialogInterface.OnClickListener() {
	           public void onClick(DialogInterface dialog, int id) {
	                deleteBibleStudy();
	           }
	       });
		return builder.create();
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
			String callId = mUri.getPathSegments().get(1);
			
			if(isStudy) {
				Cursor c = getCurrentBibleStudyCursor();
				ContentValues values = new ContentValues();
				if(c.getCount() == 0) {
					//create a new record
					values.put(BibleStudies.CALL_ID, callId);
					getContentResolver().insert(BibleStudies.CONTENT_URI, values);
				}
				c.close();
				c = null;
				
				showStudyToast(isStudy);
			} else {
				
				//ask user to delete whole record, or just end study
				showDialog(DIALOG_BIBLESTUDYDELETE_ID);
				
			}
			
			
			
			
			
			
			
		}
	}
	
	private void showStudyToast(boolean isStudy) {
		String name = mCursor.getString(NAME_COLUMN);
		String status = isStudy ? "now" : "no longer";
		String text = name + " is " + status + " a bible study.";
		Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT).show();
	}
	
	private void finishBibleStudy() {
		//put an end date on current record, if it exists
		Cursor c = getCurrentBibleStudyCursor();
		ContentValues values = new ContentValues();
		
		String endTime = TimeUtil.getCurrentTimeSQLText();
		if(c.getCount() > 0) {
			c.moveToFirst();
			int bsID = c.getInt(0);
			Uri bsUri = ContentUris.withAppendedId(BibleStudies.CONTENT_URI, bsID);
			
			values.put(BibleStudies.DATE_END, endTime);
			getContentResolver().update(bsUri, values, null, null);
		}
		
		c.close();
		c = null;
		showStudyToast(false);
	}
	
	private void deleteBibleStudy() {
		//remove the bibly study completely
		Cursor c = getCurrentBibleStudyCursor();
		
		if(c.getCount() > 0) {
			c.moveToFirst();
			int bsID = c.getInt(0);
			Uri bsUri = ContentUris.withAppendedId(BibleStudies.CONTENT_URI, bsID);
			getContentResolver().delete(bsUri, null, null);
		}
		
		c.close();
		c = null;
		showStudyToast(false);
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
	
	private void editPlacement(long id) {
		Uri uri = ContentUris.withAppendedId(Placements.CONTENT_URI, id);
		Intent i = new Intent(Intent.ACTION_EDIT, uri, this, PlacementActivity.class);
        startActivity(i);
	}
	
	private void deletePlacement(long id) {
		Uri entryUri = ContentUris.withAppendedId(Placements.CONTENT_URI, id);
		getContentResolver().delete(entryUri, null, null);
		
		refreshPlacementList();
	}
	
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		
		String title = ((TextView)((AdapterContextMenuInfo)menuInfo).targetView.findViewById(R.id.name)).getText().toString();
		
		menu.setHeaderTitle(title);
		menu.add(0, EDIT_ID, 0, R.string.edit);
		menu.add(0, DELETE_ID, 0, R.string.delete_placement);
	}
	
	@Override
	public boolean onContextItemSelected(MenuItem item) {
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
		switch(item.getItemId()) {
		
		case EDIT_ID:
			editPlacement(info.id);
			return true;
		
		case DELETE_ID:
			
			deletePlacement(info.id);
			return true;
		}
		
		
		return super.onContextItemSelected(item);
	}





	@Override
	public void onItemClick(AdapterView<?> parent, View view, int pos, long id) {
		editPlacement(id);
	}

}
