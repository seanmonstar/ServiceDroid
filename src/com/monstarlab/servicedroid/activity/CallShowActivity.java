package com.monstarlab.servicedroid.activity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

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
import android.text.method.ScrollingMovementMethod;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.SimpleAdapter;
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
import com.monstarlab.servicedroid.util.TimeUtil;

public class CallShowActivity extends Activity implements OnItemClickListener {
	
	private static final String TAG = "CallShowActivity";
	
	private static final int MENU_EDIT = Menu.FIRST;
	private static final int MENU_PLACEMENT = Menu.FIRST + 1;
	private static final int MENU_RETURN = Menu.FIRST + 2;
	private static final int MENU_DIRECTIONS = Menu.FIRST + 3;
	
	
	private static final String[] CALLS_PROJECTION = new String[] { Calls._ID, Calls.NAME, Calls.ADDRESS, Calls.NOTES, Calls.DATE, Calls.TYPE  };
	private static final String[] PLACEMENTS_PROJECTION = new String[] { Placements._ID, Placements.LITERATURE_ID, Placements.CALL_ID, Literature.PUBLICATION, Placements.DATE };
	private static final String[] RETURN_VISITS_PROJECTION = new String[]{ ReturnVisits._ID, ReturnVisits.CALL_ID, ReturnVisits.DATE };
	
	private static final int ID_COLUMN = 0;
	private static final int NAME_COLUMN = 1;
	private static final int ADDRESS_COLUMN = 2;
	private static final int NOTES_COLUMN = 3; 
	private static final int DATE_COLUMN = 4;
	private static final int TYPE_COLUMN = 5;

	private static final int DIALOG_PLACEMENT_ID = 0;
	private static final int DIALOG_BIBLESTUDYDELETE_ID = 1;

	private static final int EDIT_PLACEMENT_ID = 0;
	private static final int DELETE_PLACEMENT_ID = 1;
	private static final int EDIT_RV_ID = 2;
	private static final int DELETE_RV_ID = 3;

	private static final String HISTORY_LOG_TITLE = "title";
	private static final String HISTORY_LOG_DATE = "date";
	private static final String HISTORY_LOG_TYPE = "type";
	private static final String HISTORY_LOG_ID = "_id";

	
	
	private Uri mUri;
	private TextView mNameText;
	private TextView mAddressText;
	private TextView mLastVisitText;
	private TextView mNotesText;
	private Cursor mCallCursor;
	private TimeUtil mTimeHelper;
	

	private ListView mListView;
	private ArrayList<HashMap<String, String>> mHistoryMaps;
	
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
		mNotesText.setMovementMethod(ScrollingMovementMethod.getInstance());

		
		mListView = (ListView) findViewById(R.id.placements_list);
		mListView.setEmptyView(findViewById(android.R.id.empty));
        mListView.setOnCreateContextMenuListener(this);
        mListView.setScrollBarStyle(View.SCROLLBARS_OUTSIDE_OVERLAY);
        mListView.setOnItemClickListener(this);
		
		mTimeHelper = new TimeUtil(this);
		
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		
		refreshCallData();
		refreshHistoryLog();
		
	}
	
	private void refreshCallData() {
		mCallCursor = managedQuery(mUri, CALLS_PROJECTION, null, null, null);
		if(mCallCursor != null) {
			if(mCallCursor.getCount() == 1) {
			
				mCallCursor.moveToFirst();
				

				String name = mCallCursor.getString(NAME_COLUMN);
				String address = mCallCursor.getString(ADDRESS_COLUMN);
				String notes = mCallCursor.getString(NOTES_COLUMN);
				mCallType = mCallCursor.getInt(TYPE_COLUMN);
				
				//user created Calls show all data
				//system created Calls might behave differently
				switch(mCallType) {
				
				case Calls.TYPE_ANONYMOUS:
					mNameText.setText(name);
					mAddressText.setVisibility(View.GONE);
					mNotesText.setVisibility(View.GONE);
					mLastVisitText.setVisibility(View.GONE);
					break;
				default:
					mNameText.setText(name);
					mAddressText.setText(address);
					mNotesText.setText(notes);
					updateLastVisited();
					break;
				
				}
				
				
				
				
				
			} else {
				finish();
			}
		}
	}
	
	private void refreshHistoryLog() {
		String callId =  mUri.getPathSegments().get(1);
		
		// get all placements and return visits
		Cursor placementsCursor = getContentResolver().query(Placements.DETAILS_CONTENT_URI, PLACEMENTS_PROJECTION, Placements.CALL_ID + "=?", new String[] { callId }, "placements.date DESC");
		Cursor rvCursor = getContentResolver().query(ReturnVisits.CONTENT_URI, RETURN_VISITS_PROJECTION, ReturnVisits.CALL_ID + "=?", new String[]{ callId } , ReturnVisits.DATE + " DESC");
		
		
		//jam both into an array
		mHistoryMaps = new ArrayList<HashMap<String, String>>();
		
		if (placementsCursor != null) {
			if (placementsCursor.getCount() > 0) {
				placementsCursor.moveToFirst();
				int publicationIndex = placementsCursor.getColumnIndex(Literature.PUBLICATION);
				int dateIndex = placementsCursor.getColumnIndex(Placements.DATE);
				
				while (!placementsCursor.isAfterLast()) {
					HashMap<String, String> map = new HashMap<String, String>();
					map.put(HISTORY_LOG_TITLE, placementsCursor.getString(publicationIndex));
					map.put(HISTORY_LOG_DATE, placementsCursor.getString(dateIndex));
					map.put(HISTORY_LOG_TYPE, Placements.CONTENT_ITEM_TYPE);
					map.put(HISTORY_LOG_ID, placementsCursor.getString(0));
					mHistoryMaps.add(map);
					placementsCursor.moveToNext();
				}
			}
			placementsCursor.close();
		}
		
		if (rvCursor != null) {
			if(rvCursor.getCount() > 0) {
		
				rvCursor.moveToFirst();
				int dateIndex = rvCursor.getColumnIndex(Placements.DATE);
				
				while (!rvCursor.isAfterLast()) {
					HashMap<String, String> map = new HashMap<String, String>();
					map.put(HISTORY_LOG_TITLE, getString(R.string.rv));
					map.put(HISTORY_LOG_DATE, rvCursor.getString(dateIndex));
					map.put(HISTORY_LOG_TYPE, ReturnVisits.CONTENT_ITEM_TYPE);
					map.put(HISTORY_LOG_ID, rvCursor.getString(0));
					mHistoryMaps.add(map);
					rvCursor.moveToNext();
				}
			}
			rvCursor.close();
		}

		//sort the array via date (sigh)
		Collections.sort(mHistoryMaps, new Comparator<HashMap<String, String>>() {
			public int compare(HashMap<String, String> o1, HashMap<String, String> o2) {
				// negative for reverse order
				return -o1.get(HISTORY_LOG_DATE).compareTo(o2.get(HISTORY_LOG_DATE));
			}
		});
		
		
		//make the ArrayAdapter and link to the listView
		
		String[] from = new String[]{ HISTORY_LOG_TITLE, HISTORY_LOG_DATE };
		int[] to = new int[]{ R.id.name, R.id.date };
		
		SimpleAdapter adapter = new SimpleAdapter(this, mHistoryMaps, R.layout.placement_row, from, to) {
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
	
	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        boolean result = super.onCreateOptionsMenu(menu);
        
        switch(mCallType) {

        default:
        	menu.add(0, MENU_EDIT, 1, R.string.edit).setIcon(android.R.drawable.ic_menu_edit);
            menu.add(0, MENU_RETURN, 3, R.string.make_return).setIcon(android.R.drawable.ic_menu_myplaces);
            menu.add(0, MENU_DIRECTIONS, 4, R.string.directions).setIcon(android.R.drawable.ic_menu_directions);
            //falls through
            
        case Calls.TYPE_ANONYMOUS:
        	menu.add(0, MENU_PLACEMENT, 2, R.string.placement).setIcon(android.R.drawable.ic_menu_agenda);
        	//menu.add(0, MENU_DELETE, 4, R.string.delete).setIcon(android.R.drawable.ic_menu_delete);
        	break;
        
        }
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
		case MENU_DIRECTIONS:
			getDirections();
			break;
		}
		
		return super.onOptionsItemSelected(item);
	}

	protected void getDirections() {
		Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse("http://maps.google.com/maps?daddr="+mAddressText.getText().toString()));
		startActivity(i);
		
	}

	protected void editCall() {
		Intent i = new Intent(Intent.ACTION_EDIT, mUri, this, CallEditActivity.class);
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
		
		if(mCallCursor != null) {
			mCallCursor.moveToFirst();
			//adding Call ID so we know what Call to associate the placement with
			i.putExtra(Calls._ID, mCallCursor.getInt(ID_COLUMN));
		}
		
		i.putExtra("type", Literature.TYPE_BOOK);
		startActivity(i);
		
	}

	protected void makeBrochurePlacement() {
		Intent i = new Intent(Intent.ACTION_INSERT, Placements.CONTENT_URI, this, PlacementActivity.class);
		
		if(mCallCursor != null) {
			mCallCursor.moveToFirst();
			//adding Call ID so we know what Call to associate the placement with
			i.putExtra(Calls._ID, mCallCursor.getInt(ID_COLUMN));
		}
		
		i.putExtra("type", Literature.TYPE_BROCHURE);
		startActivity(i);
		
	}

	protected void makeMagazinePlacement() {
		Intent i = new Intent(Intent.ACTION_INSERT, Placements.CONTENT_URI, this, PlacementActivity.class);
		
		if(mCallCursor != null) {
			mCallCursor.moveToFirst();
			//adding Call ID so we know what Call to associate the placement with
			i.putExtra(Calls._ID, mCallCursor.getInt(ID_COLUMN));
		}
		
		i.putExtra("type", Literature.TYPE_MAGAZINE);
		startActivity(i);
		
	}


	protected void returnOnCall() {
		if(mCallCursor != null) {
			mCallCursor.moveToFirst();
			Intent i = new Intent(Intent.ACTION_INSERT, ReturnVisits.CONTENT_URI, this, ReturnVisitActivity.class);
			//adding Call ID so we know what Call to associate the placement with
			i.putExtra(Calls._ID, mCallCursor.getInt(ID_COLUMN));
			startActivity(i);
			
			/*mCallCursor.moveToFirst();
		
			ContentValues values = new ContentValues();
			values.put(ReturnVisits.CALL_ID, mCallCursor.getInt(ID_COLUMN));
			getContentResolver().insert(ReturnVisits.CONTENT_URI, values);
			
			
			String name = mCallCursor.getString(NAME_COLUMN);
			String text = getString(R.string.return_visit_success, name);
			Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT).show();
			updateLastVisited();*/
		}
	}
	
	protected void updateLastVisited() {
		if(mCallCursor != null) {
			//check the most recent Return Visit			
			String lastVisit = getLatestReturn();
			
			//if there has been no return visit
			if(lastVisit == null) {
				//show the date the Call was first found home
				mCallCursor.moveToFirst();
				lastVisit = mCallCursor.getString(DATE_COLUMN);
			}
			mLastVisitText.setText(getString(R.string.last_visited, mTimeHelper.normalizeDate(lastVisit)));
		}
	}
	
	protected String getLatestReturn() {
		String date = null;
		if(mCallCursor != null) {
			mCallCursor.moveToFirst();
			
			Cursor c = getContentResolver().query(ReturnVisits.CONTENT_URI, new String[] { ReturnVisits.DATE }, "call_id = ?", new String[] { mCallCursor.getString(ID_COLUMN) }, "date desc");
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
		
		refreshHistoryLog();
	}
	
	private void editReturnVisit(long id) {
		Uri uri = ContentUris.withAppendedId(ReturnVisits.CONTENT_URI, id);
		Intent i = new Intent(Intent.ACTION_EDIT, uri, this, ReturnVisitActivity.class);
        startActivity(i);
	}
	
	private void deleteReturnVisit(long id) {
		Uri entryUri = ContentUris.withAppendedId(ReturnVisits.CONTENT_URI, id);
		getContentResolver().delete(entryUri, null, null);
		
		refreshHistoryLog();
	}
	
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		
		AdapterContextMenuInfo info = (AdapterContextMenuInfo)menuInfo;
		int pos = info.position;
		if (pos < 0 || pos >= mHistoryMaps.size()) {
			return; // sanity check. this shouldn't happen, tho :)
		}
		
		HashMap<String, String> map = mHistoryMaps.get(pos);
		String type = map.get(HISTORY_LOG_TYPE);
		
		
		
		String title = map.get(HISTORY_LOG_TITLE);
		
		menu.setHeaderTitle(title);
		if (type == Placements.CONTENT_ITEM_TYPE) {
			menu.add(0, EDIT_PLACEMENT_ID, 0, R.string.edit);
			menu.add(0, DELETE_PLACEMENT_ID, 0, R.string.delete_placement);
		} else if (type == ReturnVisits.CONTENT_ITEM_TYPE) {
			menu.add(0, EDIT_RV_ID, 0, R.string.edit);
			menu.add(0, DELETE_RV_ID, 0, R.string.delete_rv);
		}
		
	}
	
	@Override
	public boolean onContextItemSelected(MenuItem item) {
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
		
		int pos = info.position;
		if (pos < 0 || pos >= mHistoryMaps.size()) {
			return false; // sanity check. this shouldn't happen, tho :)
		}
		
		HashMap<String, String> map = mHistoryMaps.get(pos);
		int id = Integer.parseInt(map.get(HISTORY_LOG_ID));
		
		switch(item.getItemId()) {
		
		case EDIT_PLACEMENT_ID:
			editPlacement(id);
			return true;
		
		case DELETE_PLACEMENT_ID:
			deletePlacement(id);
			return true;
			
		case EDIT_RV_ID:
			editReturnVisit(id);
			return true;
			
		case DELETE_RV_ID:
			deleteReturnVisit(id);
			return true;
			
		}
		
		
		return super.onContextItemSelected(item);
	}





	@Override
	public void onItemClick(AdapterView<?> parent, View view, int pos, long id) {
		if (pos < 0 || pos >= mHistoryMaps.size()) {
			return; // sanity check. this shouldn't happen, tho :)
		}
		
		HashMap<String, String> map =  mHistoryMaps.get(pos);
		String type = map.get(HISTORY_LOG_TYPE);
		String _id = map.get(HISTORY_LOG_ID);
		
		if (type == Placements.CONTENT_ITEM_TYPE) {
			editPlacement(Integer.parseInt(_id));
		} else if (type == ReturnVisits.CONTENT_ITEM_TYPE) {
			editReturnVisit(Integer.parseInt(_id));
		}
		
	}

}
