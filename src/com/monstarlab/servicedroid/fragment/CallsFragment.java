package com.monstarlab.servicedroid.fragment;

import android.app.Activity;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.ImageView;
import android.support.v4.widget.SimpleCursorAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;

import com.actionbarsherlock.app.SherlockListFragment;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.monstarlab.servicedroid.R;
import com.monstarlab.servicedroid.activity.ServiceDroidActivity;
import com.monstarlab.servicedroid.model.Models.Calls;
import com.monstarlab.servicedroid.model.Models.Placements;
import com.monstarlab.servicedroid.model.Models.ReturnVisits;
import com.monstarlab.servicedroid.util.TimeUtil;

public class CallsFragment extends SherlockListFragment implements LoaderManager.LoaderCallbacks<Cursor> {
	
	private static final int MENU_ADD = Menu.FIRST;
	private static final int MENU_SORT_ALPHA = Menu.FIRST + 1;
	private static final int MENU_SORT_TIME = Menu.FIRST + 2;
	private static final int MENU_ADD_ANON_PLACEMENTS = Menu.FIRST + 3;
	
	private static final int EDIT_ID  = Menu.FIRST + 4;
	private static final int RETURN_ID  = Menu.FIRST + 5;
	private static final int DIRECTIONS_ID  = Menu.FIRST + 6;
	private static final int DELETE_ID = Menu.FIRST + 7;
	
	private static final int SORT_ALPHA = 0;
	private static final int SORT_TIME = 1;
	
	private static final int CURSOR_ALL = 1;
	private static final int CURSOR_ANON = 2;
	
	private static final Uri CONTENT_URI = Calls.CONTENT_URI;
	
	private static final String[] PROJECTION = new String[] { 
		Calls._ID,
		Calls.NAME,
		Calls.ADDRESS,
		Calls.IS_STUDY,
		Calls.LAST_VISITED,
		Calls.TYPE
	};
	
	private static final String PREFS_NAME = "Calls";
	private static final String PREFS_SORT_KEY = "sortOrder";
	
	private SimpleCursorAdapter mAdapter;
	private int mSortState;
	private TextView mHeaderText;
	private boolean mHasAnonCall;
	private Callbacks mCallbacks;
	private long mJustDeletedCall;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
		// default sort state will be alphabetically, unless set otherwise
        loadSortState();
	}
	
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.calls, container, false);
		mHeaderText = (TextView) rootView.findViewById(R.id.header);
		return rootView;
	}
	
	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		registerForContextMenu(getListView());
		//getListView().setOnScrollListener(this);
		
		

		String[] from = new String[]{ Calls.NAME, Calls.ADDRESS, Calls.IS_STUDY };
		int[] to = new int[]{ R.id.name, R.id.address, R.id.icon };
		
		mAdapter = new SimpleCursorAdapter(getActivity(), R.layout.call_row, null, from, to, 0) {
			
			// To show or hide the Bible Study image.
			@Override
			public void setViewImage(ImageView v, String value) {
				if(Integer.parseInt(value) > 0) {
					v.setVisibility(View.VISIBLE);
				} else {
					v.setVisibility(View.GONE);
				}
			}
			
		};
		setListAdapter(mAdapter);

		getLoaderManager().initLoader(CURSOR_ALL, null, this);
		getLoaderManager().initLoader(CURSOR_ANON, null, this);
	}
	
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		if (!(activity instanceof Callbacks)) {
			throw new IllegalStateException(
					"Activity must implement fragment's callbacks.");
		}

		mCallbacks = (Callbacks) activity;
	}

	@Override
	public void onDetach() {
		super.onDetach();
		mCallbacks = sDummyCallbacks;
	}
	
	@Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
    	inflater.inflate(R.menu.calls, menu);
    }
	
	@Override
	public void onPrepareOptionsMenu(Menu menu) {
		super.onPrepareOptionsMenu(menu);
		
		//adding Anonymous Placements depends on Cursor
        if(mHasAnonCall) {
        	menu.removeItem(MENU_ADD_ANON_PLACEMENTS);
        } else {
        	if(menu.findItem(MENU_ADD_ANON_PLACEMENTS) == null) {
        		menu.add(0, MENU_ADD_ANON_PLACEMENTS, 3, R.string.anon_placement).setIcon(R.drawable.menu_add);
        	}
        }
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_add:
			mCallbacks.onCreateCall();
			break;
		case R.id.menu_sort:
			toggleSortOrder();
			break;
		case MENU_ADD_ANON_PLACEMENTS:
			createAnonCall();
			break;
		}
		
		return super.onOptionsItemSelected(item);
	}
	
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		
		AdapterContextMenuInfo info = ((AdapterContextMenuInfo)menuInfo);
		Cursor c = mAdapter.getCursor();
		if (c != null) {
			c.moveToPosition(info.position);
			
			
			String name = c.getString(1);
			int type = c.getInt(5);
			
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
		
	}
	
	@Override
	public boolean onContextItemSelected(android.view.MenuItem item) {
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
		
		switch(item.getItemId()) {
		case EDIT_ID:
			mCallbacks.onEditCall(info.id);
			return true;
		case RETURN_ID:
			returnOnCall(info.id);
			return true;
			
		case DIRECTIONS_ID:
			Cursor c = mAdapter.getCursor();
			if(c != null) {
				if(c.getCount() > 0) {
					
					c.moveToPosition(info.position);
					String addr = c.getString(2);
					mCallbacks.onGetDirections(addr);
				}
			}
			return true;
			
		case DELETE_ID:
			deleteCall(info.id);
			
			//fillData();
			return true;
		}
		
		
		return super.onContextItemSelected(item);
	}
	
	protected void fillData() {
		getLoaderManager().restartLoader(CURSOR_ALL, null, this);
		getLoaderManager().restartLoader(CURSOR_ANON, null, this);
	}
	
	protected void toggleSortOrder() {
		if (mSortState == SORT_ALPHA) {
			mSortState = SORT_TIME;
		} else if (mSortState == SORT_TIME) {
			mSortState = SORT_ALPHA;
		}
		saveSortState();
		fillData();
	}
	
	protected void loadSortState() {
		SharedPreferences settings = getActivity().getSharedPreferences(PREFS_NAME, 0);		
		mSortState = settings.getInt(PREFS_SORT_KEY, SORT_ALPHA);
	}
	
	protected void saveSortState() {
		SharedPreferences settings = getActivity().getSharedPreferences(PREFS_NAME, 0);
		Editor editor = settings.edit();
		editor.putInt(PREFS_SORT_KEY, mSortState);
		editor.commit();
	}
	
	protected void returnOnCall(long id) {
		ContentValues values = new ContentValues();
		values.put(ReturnVisits.CALL_ID, id);
		getActivity().getContentResolver().insert(ReturnVisits.CONTENT_URI, values);
		
		String name = getCallName(id);
		String text = getString(R.string.return_visit_success, name);
		Toast.makeText(getActivity(), text, Toast.LENGTH_SHORT).show();
	}
	
	protected void deleteCall(long id) {
		Uri callUri = ContentUris.withAppendedId(CONTENT_URI, id);
		getActivity().getContentResolver().delete(callUri, null, null);
		
		mJustDeletedCall = id;
		
		DeleteCallDialogFragment dialog = DeleteCallDialogFragment.create(id);
		dialog.show(getFragmentManager(), "DeleteCallDialogFragment");
		
		fillData();
	}
	
	protected String getCallName(long id) {
		Cursor c = mAdapter.getCursor();
		if(c != null) {
			c.moveToFirst();
			while (!c.isAfterLast()) {
				if (c.getInt(0) == id) {
					return c.getString(1);
				}
				c.moveToNext();
			}
		}
		return "";
	}
	
	public void deleteCallRelatedData() {
		if (mJustDeletedCall == 0) {
			return;
		}
		
		String[] whereArgs = new String[] { String.valueOf(mJustDeletedCall) };
		getActivity().getContentResolver().delete(ReturnVisits.CONTENT_URI, ReturnVisits.CALL_ID+"=?", whereArgs);
		getActivity().getContentResolver().delete(Placements.CONTENT_URI, Placements.CALL_ID+"=?", whereArgs);
	}
	

	protected void createAnonCall() {
		if(!mHasAnonCall) {
			ContentValues values = new ContentValues();
			values.put(Calls.NAME, getString(R.string.anon_placement));
			values.put(Calls.TYPE, Calls.TYPE_ANONYMOUS);
			values.put(Calls.DATE, TimeUtil.getCurrentTimeSQLText());
			getActivity().getContentResolver().insert(CONTENT_URI, values);
			
			fillData();
		}
	}
	
	
	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		//super.onListItemClick(l, v, position, id);
		mCallbacks.onEditCall(id);
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		if (id == CURSOR_ALL) {
			String sortBy = null;
			String sortText = getString(R.string.sorted_alpha);
			if(mSortState == SORT_ALPHA) {
				sortBy = Calls.NAME;
			} else if (mSortState == SORT_TIME) {
				sortBy = Calls.LAST_VISITED;
				sortText = getString(R.string.sorted_time);
			}
			mHeaderText.setText(sortText);
			
			return new CursorLoader(getActivity(), CONTENT_URI, PROJECTION, null, null, sortBy);
		} else if (id == CURSOR_ANON) {
			return new CursorLoader(getActivity(), CONTENT_URI, PROJECTION, Calls.TYPE + "=?", new String[] { ""+Calls.TYPE_ANONYMOUS }, null);
		}
		return null;
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
		int id = loader.getId();
		if (id == CURSOR_ALL) {
			mAdapter.swapCursor(cursor);
		} else if (id == CURSOR_ANON) {
			if (cursor != null) {
				mHasAnonCall = cursor.getCount() > 0;
				cursor.close();
			}
		}
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		// This is called when the last Cursor provided to onLoadFinished()
        // above is about to be closed.  We need to make sure we are no
        // longer using it.
		if (loader.getId() == CURSOR_ALL) {
			mAdapter.swapCursor(null);
		}

	}
	

	
	public interface Callbacks {
		public void onCreateCall();
		public void onEditCall(long id);
		public void onGetDirections(String addr);
	}

	private static Callbacks sDummyCallbacks = new Callbacks() {
		@Override
		public void onCreateCall() {}
		@Override
		public void onEditCall(long id) {}
		@Override
		public void onGetDirections(String addr) {}
	};
}
