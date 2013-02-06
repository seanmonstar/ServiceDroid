package com.monstarlab.servicedroid.fragment;

import android.content.Context;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.app.LoaderManager;

import com.actionbarsherlock.app.SherlockListFragment;
import com.monstarlab.servicedroid.R;
import com.monstarlab.servicedroid.model.Models.Tags;
import com.monstarlab.servicedroid.model.Models.TimeEntries;
import com.monstarlab.servicedroid.util.TimeUtil;

public class TagsFragment extends SherlockListFragment {

	//TODO: create view
	
	private LayoutInflater mInflater;
	private Cursor mCursor;
	private static final String[] PROJECTION = new String[] { Tags._ID, Tags.TITLE };
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		mInflater = LayoutInflater.from(getActivity());
		
		fillData();
	}
	
	protected void fillData() {
		mCursor = getActivity().managedQuery(Tags.CONTENT_URI, PROJECTION, null, null, Tags.TITLE + " ASC");
		TagsAdapter adapter = new TagsAdapter(getActivity(), R.layout.tag_row, mCursor, new String[] { Tags.TITLE }, new int[] { R.id.tag_title });
		setListAdapter(adapter);
	}
	
	//TODO: add button
	
	//TODO: show a Toast in CallsFragment of what current filter is
	
	protected class TagsAdapter extends SimpleCursorAdapter {

		public TagsAdapter(Context context, int layout, Cursor c,
				String[] from, int[] to) {
			super(context, layout, c, from, to);
			// TODO Auto-generated constructor stub
		}
		
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View view = super.getView(position, convertView, parent);
			
			return view;
		}
		
	}

}
