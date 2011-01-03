package com.monstarlab.servicedroid.activity;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import android.app.AlertDialog;
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
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.AdapterContextMenuInfo;

import com.monstarlab.servicedroid.R;
import com.monstarlab.servicedroid.model.Models.Calls;
import com.monstarlab.servicedroid.model.Models.ReturnVisits;
import com.monstarlab.servicedroid.util.LocationUtil;
import com.monstarlab.servicedroid.util.TimeUtil;

public class ReturnVisitsActivity extends ListActivity implements Observer {

	private static final String TAG = "ReturnVisitsActivity";

	private static final int MENU_ADD = Menu.FIRST;
	private static final int MENU_SORT_ALPHA = Menu.FIRST + 1;
	private static final int MENU_SORT_TIME = Menu.FIRST + 2;
	private static final int MENU_ADD_ANON_PLACEMENTS = Menu.FIRST + 3;
	private static final int EDIT_ID = Menu.FIRST + 4;
	private static final int RETURN_ID = Menu.FIRST + 5;
	private static final int DIRECTIONS_ID = Menu.FIRST + 6;
	private static final int DELETE_ID = Menu.FIRST + 7;

	private static final String[] PROJECTION = new String[] { Calls._ID,
			Calls.NAME, Calls.ADDRESS, Calls.IS_STUDY, Calls.LAST_VISITED,
			Calls.TYPE, Calls.LOCATION };

	private static final int SORT_ALPHA = 0;
	private static final int SORT_TIME = 1;
	private static final int SORT_DISTANCE = 2;

	private AlertDialog mSortOptionsDialog;

	private int mSortState;

	private LocationUtil locationUtil;

	private boolean mIsAnonCall = false;
	private Cursor mListCursor;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Intent intent = getIntent();
		if (intent.getData() == null) {
			intent.setData(Calls.CONTENT_URI);
		}

		this.setContentView(R.layout.calls);

		locationUtil = new LocationUtil(this);
		locationUtil.addObserver(this);

		// initial sort state will be alphabetically
		mSortState = SORT_ALPHA;

		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("Sort Options");
		builder.setSingleChoiceItems(R.array.sort_by, 0,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int item) {
						mSortState = item;
						// don't need to call fillData here since it is called
						//every time the window gets focus
						//fillData();
						dialog.cancel();
					}
				});
		mSortOptionsDialog = builder.create();

		//fillData();
		registerForContextMenu(getListView());
	}

	protected void fillData() {
		String sortBy = null;
		switch (mSortState) {
		case SORT_ALPHA:
			sortBy = Calls.NAME;
			break;
		case SORT_TIME:
			sortBy = Calls.LAST_VISITED;
			break;
		case SORT_DISTANCE:
			sortBy = Calls.LAST_VISITED;
			break;
		}

		mListCursor = managedQuery(getIntent().getData(), PROJECTION, null,
				null, sortBy);

		if (mSortState == SORT_DISTANCE) {
			String[] from = new String[] { Calls.NAME, Calls.ADDRESS,
					Calls.IS_STUDY, "distance" };
			int[] to = new int[] { R.id.name, R.id.address, R.id.icon,
					R.id.distance };


			// re-compute distance from current location
			mListCursor.moveToFirst();
			List<HashMap<String, String>> fillMaps = new ArrayList<HashMap<String, String>>();

			while (mListCursor.isAfterLast() == false) {
				int colidx = mListCursor.getColumnIndex(Calls.LOCATION);
				String loc = mListCursor.getString(colidx);
				String name = mListCursor.getString(mListCursor
						.getColumnIndex(Calls.NAME));
				String isstudy = mListCursor.getString(mListCursor
						.getColumnIndex(Calls.IS_STUDY));
				String address = mListCursor.getString(mListCursor
						.getColumnIndex(Calls.ADDRESS));
				int id = mListCursor.getInt(mListCursor
						.getColumnIndex(Calls._ID));

				double dist;
				if (loc != null) {
					String[] arloc = loc.split(",");
					double lat = Double.valueOf(arloc[0].trim()).doubleValue();
					double lon = Double.valueOf(arloc[1].trim()).doubleValue();
					dist = locationUtil.getDistanceToLocation(lat, lon);
				} else {
					dist = -1;
				}
				DecimalFormat twoDForm = new DecimalFormat("#.#");
				dist = Double.valueOf(twoDForm.format(dist));

				HashMap<String, String> map = new HashMap<String, String>();
				map.put(Calls._ID, String.valueOf(id));
				map.put(Calls.NAME, name);
				map.put(Calls.ADDRESS, address);
				map.put(Calls.IS_STUDY, isstudy);
				String sDist;
				if (dist == -1) {
					sDist = getString(R.string.distance_unknown);
				} else {
					sDist = dist + " " + getString(R.string.distance_unit);
				}
				map.put("distance", sDist);
				fillMaps.add(map);
				mListCursor.moveToNext();
			}

			class DistanceComparator implements Comparator {
				
				String mUnknown = getString(R.string.distance_unknown);
				String mUnit = getString(R.string.distance_unit);
				
				@SuppressWarnings("unchecked")
				public int compare(Object o1, Object o2) {
					String dist1 = ((HashMap<String, String>) o1).get("distance");
					String dist2 = ((HashMap<String, String>) o2).get("distance");
					if (dist1 == mUnknown){
						return -1;
					}
					if (dist2 == mUnknown){
						return 1;
					}
					float fdist1 = Float.parseFloat(dist1.substring(0, mUnit.length() - 1));
					float fdist2 = Float.parseFloat(dist2.substring(0, mUnit.length() - 1));
					if (fdist1 < fdist2)
						return -1;
					if (fdist1 < fdist2)
						return 1;
					return 0;
				}
			}
			Collections.sort(fillMaps, new DistanceComparator());
			
			// fill in the grid_item layout
			SimpleAdapter rvs = new SimpleAdapter(this, fillMaps,
					R.layout.call_row_dist, from, to) {

				@Override
				public void setViewImage(ImageView v, String value) {
					if (Integer.parseInt(value) > 0) {
						v.setVisibility(View.VISIBLE);
					} else {
						v.setVisibility(View.GONE);
					}
				}
			};

			setListAdapter(rvs);
		} else {
			String[] from = new String[] { Calls.NAME, Calls.ADDRESS,
					Calls.IS_STUDY };
			int[] to = new int[] { R.id.name, R.id.address, R.id.icon};
			SimpleCursorAdapter rvs = new SimpleCursorAdapter(this,
					R.layout.call_row, mListCursor, from, to) {

				@Override
				public void setViewImage(ImageView v, String value) {
					if (Integer.parseInt(value) > 0) {
						v.setVisibility(View.VISIBLE);
					} else {
						v.setVisibility(View.GONE);
					}
				}
			};
			setListAdapter(rvs);
		}

		getAnonCall();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		boolean result = super.onCreateOptionsMenu(menu);
		menu.add(0, MENU_ADD, 1, R.string.add_call).setIcon(
				android.R.drawable.ic_menu_add);

		menu.add(0, MENU_SORT_ALPHA, 2, R.string.sort).setIcon(
				android.R.drawable.ic_menu_sort_alphabetically);

		return result;
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		super.onPrepareOptionsMenu(menu);

		// adding Anonymous Placements depends on Cursor
		if (mIsAnonCall) {
			menu.removeItem(MENU_ADD_ANON_PLACEMENTS);
		} else {
			if (menu.findItem(MENU_ADD_ANON_PLACEMENTS) == null) {
				menu.add(0, MENU_ADD_ANON_PLACEMENTS, 3,
						R.string.anon_placement).setIcon(
						android.R.drawable.ic_menu_add);
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
			// mSortState = SORT_ALPHA;
			// fillData();
			mSortOptionsDialog.show();
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
		if (!mIsAnonCall) {
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
		Cursor c = getContentResolver().query(getIntent().getData(),
				PROJECTION, Calls.TYPE + "=?",
				new String[] { "" + Calls.TYPE_ANONYMOUS }, null);

		if (c.getCount() > 0) {
			mIsAnonCall = true;
		}

		c.close();
		c = null;
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);

		AdapterContextMenuInfo info = ((AdapterContextMenuInfo) menuInfo);
		Log.d(TAG, "Menu position id is " + info.position);
		mListCursor.moveToPosition(info.position);

		String name = mListCursor.getString(1);
		int type = mListCursor.getInt(5);

		menu.setHeaderTitle(name);

		switch (type) {

		default:
			menu.add(0, RETURN_ID, 1, R.string.make_return);
			menu.add(0, DIRECTIONS_ID, 2, R.string.directions);
			// falls through

		case Calls.TYPE_ANONYMOUS:
			menu.add(0, EDIT_ID, 0, R.string.edit);
			menu.add(0, DELETE_ID, 3, R.string.delete_call);
		}

	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item
				.getMenuInfo();

		long id;

		Object tmpdata = getListView().getItemAtPosition(info.position);
		if (tmpdata instanceof HashMap) {
			HashMap<String, String> data = (HashMap<String, String>) getListView()
					.getItemAtPosition(info.position);
			String tmp = data.get(Calls._ID);
			id = Long.valueOf(tmp);
		} else {
			id = info.id;
		}

		switch (item.getItemId()) {
		case EDIT_ID:
			editCall(id);
			return true;
		case RETURN_ID:
			returnOnCall(id);
			return true;

		case DIRECTIONS_ID:
			getDirections(info.position);
			return true;

		case DELETE_ID:
			deleteCall(id);

			// fillData();
			return true;
		}

		return super.onContextItemSelected(item);
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		// super.onListItemClick(l, v, position, id);
		Object tmpdata = getListView().getItemAtPosition(position);
		if (tmpdata instanceof HashMap) {
			HashMap<String, String> data = (HashMap<String, String>) getListView()
					.getItemAtPosition(position);
			String tmp = data.get(Calls._ID);
			editCall(Long.valueOf(tmp));
		} else {
			editCall(id);
		}

	}

	protected void getDirections(int index) {
		if (mListCursor != null) {
			if (mListCursor.getCount() > 0) {

				mListCursor.moveToPosition(index);
				String addr = mListCursor.getString(2);
				Intent i = new Intent(Intent.ACTION_VIEW,
						Uri.parse("http://maps.google.com/maps?daddr=" + addr));
				startActivity(i);
			}
		}

	}

	protected void deleteCall(long id) {
		Uri callUri = ContentUris.withAppendedId(getIntent().getData(), id);
		getContentResolver().delete(callUri, null, null);

		getAnonCall();
	}

	protected void editCall(long id) {
		Uri uri = ContentUris.withAppendedId(getIntent().getData(), id);
		Intent i = new Intent(Intent.ACTION_EDIT, uri, this,
				RVShowActivity.class);
		startActivity(i);
	}

	protected void createCall() {
		Intent i = new Intent(Intent.ACTION_INSERT, getIntent().getData(),
				this, RVEditActivity.class);
		startActivity(i);
	}

	protected void returnOnCall(long id) {
		ContentValues values = new ContentValues();
		values.put(ReturnVisits.CALL_ID, id);
		getContentResolver().insert(ReturnVisits.CONTENT_URI, values);

		String name = getCallName(id);
		String text = getString(R.string.return_visit_success, name);
		Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT)
				.show();
	}

	protected String getCallName(long id) {
		Uri uri = ContentUris.withAppendedId(getIntent().getData(), id);
		Cursor c = getContentResolver().query(uri, new String[] { Calls.NAME },
				null, null, null);
		if (c != null) {
			c.moveToFirst();
			String name = c.getString(0);
			c.close();
			return name;
		}
		return "";
	}

	public void onWindowFocusChanged(boolean hasWindowFocus){
		if (hasWindowFocus == true){
			fillData();
		}
	}
	
	@Override
	// observer update function
	// called when location changes
	public void update(Observable observable, Object data) {
		fillData();
	}

}
