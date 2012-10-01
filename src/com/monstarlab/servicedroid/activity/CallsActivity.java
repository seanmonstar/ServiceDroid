package com.monstarlab.servicedroid.activity;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
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
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.AdapterContextMenuInfo;

import com.monstarlab.servicedroid.R;
import com.monstarlab.servicedroid.model.Models.Calls;
import com.monstarlab.servicedroid.model.Models.Placements;
import com.monstarlab.servicedroid.model.Models.ReturnVisits;
import com.monstarlab.servicedroid.util.TimeUtil;

public class CallsActivity extends ListActivity {
	
	private static final String TAG = "CallsActivity";
	
	private static final int MENU_ADD = Menu.FIRST;
	private static final int MENU_SORT_ALPHA = Menu.FIRST + 1;
	private static final int MENU_SORT_TIME = Menu.FIRST + 2;
	private static final int MENU_ADD_ANON_PLACEMENTS = Menu.FIRST + 3;
	
	private static final int EDIT_ID  = Menu.FIRST + 4;
	private static final int RETURN_ID  = Menu.FIRST + 5;
	private static final int DIRECTIONS_ID  = Menu.FIRST + 6;
	private static final int DELETE_ID = Menu.FIRST + 7;
	
	private static final int DELETE_CALL_DIALOG = 0;
	
	private static final String[] PROJECTION = new String[] { Calls._ID, Calls.NAME, Calls.ADDRESS, Calls.IS_STUDY, Calls.LAST_VISITED, Calls.TYPE };
	
	private static final int SORT_ALPHA = 0;
	private static final int SORT_TIME = 1;

	private int mSortState;
	private long mJustDeletedCall;

	private boolean mIsAnonCall = false;
	private Cursor mListCursor;

	private ImageButton mQuickAddBtn;
	private TextView mHeaderText;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        Intent intent = getIntent();
		if(intent.getData() == null) {
			intent.setData(Calls.CONTENT_URI);
		}
        
        this.setContentView(R.layout.calls);
        
        mHeaderText = (TextView)findViewById(R.id.header);
        mQuickAddBtn = (ImageButton)findViewById(R.id.btn_add);
        mQuickAddBtn.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				createCall();
			}
        	
        });
        
        //initial sort state will be alphabetically
        mSortState = SORT_ALPHA;
        
        fillData();
        registerForContextMenu(getListView());
    }
	

	protected void fillData() {
		String sortBy = null;
		String sortText = getString(R.string.sorted_alpha);
		if(mSortState == SORT_ALPHA) {
			sortBy = Calls.NAME;
		} else if (mSortState == SORT_TIME) {
			sortBy = Calls.LAST_VISITED;
			sortText = getString(R.string.sorted_time);
		}
		
		mHeaderText.setText(sortText);
		
		mListCursor = managedQuery(getIntent().getData(), PROJECTION, null, null, sortBy);
		
		String[] from = new String[]{ Calls.NAME, Calls.ADDRESS, Calls.IS_STUDY };
		int[] to = new int[]{ R.id.name, R.id.address, R.id.icon };
		
		SimpleCursorAdapter rvs = new SimpleCursorAdapter(this, R.layout.call_row, mListCursor, from, to) {
			
			@Override
			public void setViewImage(ImageView v, String value) {
				if(Integer.parseInt(value) > 0) {
					v.setVisibility(View.VISIBLE);
				} else {
					v.setVisibility(View.GONE);
				}
			}
			
		};
		
		setListAdapter(rvs);
		
		getAnonCall();
	}
	
	private Dialog makeDeleteCallDialog() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);

		//builder.setTitle(getString(R.string.placement));
		builder.setMessage(getString(R.string.delete_call_prompt))
	       .setCancelable(false)
	       .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
	           public void onClick(DialogInterface dialog, int id) {
	               deleteCallRelatedData();
	           }
	       })
	       .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
	           public void onClick(DialogInterface dialog, int id) {
	                //do nothing
	           }
	       });
		return builder.create();
	}
	
	private void deleteCallRelatedData() {
		if (mJustDeletedCall == 0) {
			return;
		}
		
		String[] whereArgs = new String[] { String.valueOf(mJustDeletedCall) };
		getContentResolver().delete(ReturnVisits.CONTENT_URI, ReturnVisits.CALL_ID+"=?", whereArgs);
		getContentResolver().delete(Placements.CONTENT_URI, Placements.CALL_ID+"=?", whereArgs);
	}
	
	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        boolean result = super.onCreateOptionsMenu(menu);
        menu.add(0, MENU_ADD, 1, R.string.add_call).setIcon(android.R.drawable.ic_menu_add);
        return result;
    }
	
	@Override
	protected Dialog onCreateDialog(int id) {
		Dialog dialog;
	    switch(id) {
	    case DELETE_CALL_DIALOG:
	    	dialog = makeDeleteCallDialog();
	        break;
	    default:
	        dialog = null;
	    }
	    return dialog;
	}


	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		super.onPrepareOptionsMenu(menu);
		
		//menu depends on if how data is sorted
		if(mSortState == SORT_ALPHA) {
			menu.removeItem(MENU_SORT_ALPHA);
			if(menu.findItem(MENU_SORT_TIME) == null) {
				menu.add(0, MENU_SORT_TIME, 2, R.string.sort).setIcon(android.R.drawable.ic_menu_recent_history);
			}
		} else if(mSortState == SORT_TIME){
			menu.removeItem(MENU_SORT_TIME);
			if(menu.findItem(MENU_SORT_ALPHA) == null) {
				menu.add(0, MENU_SORT_ALPHA, 2, R.string.sort).setIcon(android.R.drawable.ic_menu_sort_alphabetically);
			}
		}
		
		//adding Anonymous Placements depends on Cursor
        if(mIsAnonCall) {
        	menu.removeItem(MENU_ADD_ANON_PLACEMENTS);
        } else {
        	if(menu.findItem(MENU_ADD_ANON_PLACEMENTS) == null) {
        		menu.add(0, MENU_ADD_ANON_PLACEMENTS, 3, R.string.anon_placement).setIcon(android.R.drawable.ic_menu_add);
        	}
        }
		
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case MENU_ADD:
			createCall();
			break;
		case MENU_SORT_ALPHA:
			mSortState = SORT_ALPHA;
			fillData();
			break;
		case MENU_SORT_TIME:
			mSortState = SORT_TIME;
			fillData();
			break;
		case MENU_ADD_ANON_PLACEMENTS:
			createAnonCall();
			break;
		}
		
		return super.onOptionsItemSelected(item);
	}

	protected void createAnonCall() {
		if(!mIsAnonCall) {
			ContentValues values = new ContentValues();
			values.put(Calls.NAME, getString(R.string.anon_placement));
			values.put(Calls.TYPE, Calls.TYPE_ANONYMOUS);
			values.put(Calls.DATE, TimeUtil.getCurrentTimeSQLText());
			getContentResolver().insert(getIntent().getData(), values);
			
			getAnonCall();
		}
	}
	
	protected void getAnonCall() {
		mIsAnonCall = false;
		Cursor c = getContentResolver().query(getIntent().getData(), PROJECTION, Calls.TYPE + "=?", new String[] { ""+Calls.TYPE_ANONYMOUS }, null);
		
		if(c.getCount() > 0) {
			mIsAnonCall = true;
		}
		
		c.close();
		c = null;
	}


	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		
		AdapterContextMenuInfo info = ((AdapterContextMenuInfo)menuInfo);
		Log.d(TAG, "Menu position id is " + info.position);
		mListCursor.moveToPosition(info.position);
		
		
		String name = mListCursor.getString(1);
		int type = mListCursor.getInt(5);
		
		menu.setHeaderTitle(name);
		
		switch(type) {
		
		default:
			menu.add(0, RETURN_ID, 1, R.string.make_return);
			menu.add(0, DIRECTIONS_ID, 2, R.string.directions);
			//falls through
			
		case Calls.TYPE_ANONYMOUS:
			menu.add(0, EDIT_ID, 0, R.string.edit);
			menu.add(0, DELETE_ID, 3, R.string.delete_call);
		}
		
	}
	
	@Override
	public boolean onContextItemSelected(MenuItem item) {
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
		
		switch(item.getItemId()) {
		case EDIT_ID:
			editCall(info.id);
			return true;
		case RETURN_ID:
			returnOnCall(info.id);
			return true;
			
		case DIRECTIONS_ID:
			getDirections(info.position);
			return true;
			
		case DELETE_ID:
			deleteCall(info.id);
			
			//fillData();
			return true;
		}
		
		
		return super.onContextItemSelected(item);
	}
	
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		//super.onListItemClick(l, v, position, id);
		editCall(id);
	}
	
	protected void getDirections(int index) {
		if(mListCursor != null) {
			if(mListCursor.getCount() > 0) {
				
				mListCursor.moveToPosition(index);
				String addr = mListCursor.getString(2);
				Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse("http://maps.google.com/maps?daddr="+addr));
				startActivity(i);
			}
		}
		
		
	}
	
	protected void deleteCall(long id) {
		Uri callUri = ContentUris.withAppendedId(getIntent().getData(), id);
		getContentResolver().delete(callUri, null, null);
		mJustDeletedCall = id;
		showDialog(DELETE_CALL_DIALOG);
		
		getAnonCall();
	}
	
	protected void editCall(long id) {
		Uri uri = ContentUris.withAppendedId(getIntent().getData(), id);
		Intent i = new Intent(Intent.ACTION_EDIT, uri, this, CallShowActivity.class);
        startActivity(i);
	}
	
	
	protected void createCall() {
		Intent i = new Intent(Intent.ACTION_INSERT, getIntent().getData(), this, CallEditActivity.class);
		startActivity(i);
	}
	
	protected void returnOnCall(long id) {
		ContentValues values = new ContentValues();
		values.put(ReturnVisits.CALL_ID, id);
		getContentResolver().insert(ReturnVisits.CONTENT_URI, values);
		
		String name = getCallName(id);
		String text = getString(R.string.return_visit_success, name);
		Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT).show();
	}
	
	protected String getCallName(long id) {
		Uri uri = ContentUris.withAppendedId(getIntent().getData(), id);
		Cursor c = getContentResolver().query(uri, new String[] { Calls.NAME }, null, null, null);
		if(c != null) {
			c.moveToFirst();
			String name = c.getString(0);
			c.close();
			return name;
		}
		return "";
	}

	
}
