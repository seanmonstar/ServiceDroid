package com.monstarlab.servicedroid.fragment;

import java.util.ArrayList;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.app.LoaderManager;

import com.actionbarsherlock.app.SherlockListFragment;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.monstarlab.servicedroid.R;
import com.monstarlab.servicedroid.fragment.CallsFragment.Callbacks;
import com.monstarlab.servicedroid.model.Models.Calls;
import com.monstarlab.servicedroid.model.Models.Tags;
import com.monstarlab.servicedroid.model.Models.TimeEntries;
import com.monstarlab.servicedroid.util.TimeUtil;

public class TagsFragment extends SherlockListFragment {

	//TODO: create view
	private Cursor mCursor;
	private static final String[] PROJECTION = new String[] { Tags._ID, Tags.TITLE };
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
		fillData();
	}
	
	protected void fillData() {
		mCursor = getActivity().managedQuery(Tags.CONTENT_URI, PROJECTION, null, null, Tags.TITLE + " ASC");
		TagsAdapter adapter = new TagsAdapter(getActivity(), R.layout.tag_row, mCursor, new String[] { Tags.TITLE }, new int[] { R.id.tag_title });
		setListAdapter(adapter);
	}
	
	@Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
    	inflater.inflate(R.menu.tags, menu);
    }
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_add:
			createTag();
			break;
		}
		
		return super.onOptionsItemSelected(item);
	}
	
	private void createTag() {
		TagEditDialogFragment dialog = new TagEditDialogFragment();
		dialog.show(getFragmentManager(), "TagEditDialogFragment");
	}
	
	public void saveTag(String name) {
		ContentValues values = new ContentValues();
		values.put(Tags.TITLE, name);
		getActivity().getContentResolver().insert(Tags.CONTENT_URI, values);
	}
	
	public void saveTag(long id, String name) {
		Uri uri = ContentUris.withAppendedId(Tags.CONTENT_URI, id);
		ContentValues values = new ContentValues();
		values.put(Tags.TITLE, name);
		getActivity().getContentResolver().update(uri, values, null, null);
	}
	
	//TODO: show a Toast in CallsFragment of what current filter is
	
	protected class TagsAdapter extends SimpleCursorAdapter {
		
		private LayoutInflater mInflater;
		
		public TagsAdapter(Context context, int layout, Cursor c,
				String[] from, int[] to) {
			super(context, layout, c, from, to);
			// TODO Auto-generated constructor stub
			
			mInflater = LayoutInflater.from(context);
		}
		
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {		
			ViewHolder holder;
			
			if (convertView == null) {
				holder = new ViewHolder();
				convertView = mInflater.inflate(R.layout.tag_row, null);
				holder.checkbox = (CheckBox) convertView.findViewById(R.id.tag_checked);
				holder.title = (TextView) convertView.findViewById(R.id.tag_title);
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}
			
			
			Cursor c = (Cursor) this.getItem(position);
			holder.title.setText(c.getString(1));
			return convertView;
		}
		
		class ViewHolder {
			public CheckBox checkbox;
			public TextView title;
		}
		
	}
	
	public interface Callbacks {
		public void onTagChecked(int id, String name);
		public void onTagUnchecked(int id, String name);
	}

	private static Callbacks sDummyCallbacks = new Callbacks() {
		@Override
		public void onTagChecked(int id, String name) {}
		@Override
		public void onTagUnchecked(int id, String name) {}
	};

}
