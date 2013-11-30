package com.monstarlab.servicedroid.activity;

import java.util.ArrayList;
import java.util.HashMap;

import android.content.ContentUris;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.MenuItem;
import com.monstarlab.servicedroid.R;
import com.monstarlab.servicedroid.fragment.CallsFragment;
import com.monstarlab.servicedroid.fragment.DeleteCallDialogFragment;
import com.monstarlab.servicedroid.fragment.TagEditDialogFragment;
import com.monstarlab.servicedroid.fragment.TagsFragment;
import com.monstarlab.servicedroid.model.Models.Calls;


public class CallsActivity extends SherlockFragmentActivity 
			implements CallsFragment.Callbacks,
				TagsFragment.Callbacks,
				DeleteCallDialogFragment.DeleteCallDialogListener,
				TagEditDialogFragment.TagEditDialogFragmentListener {
	
	private static final String TAG = "CallsActivity";

	private ArrayList<Fragment> mFragments = new ArrayList<Fragment>();
	private TagsFragment mTagsFragment;
	private CallsFragment mCallsFragment;
	private ViewPager mViewPager;

	private CallsFragmentAdapter mSectionsPagerAdapter;
	
	private HashMap<Integer, String> mCheckedTags = new HashMap<Integer, String>();
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setContentView(R.layout.activity_calls);
        setupFragments();
    }
	
    private void setupFragments() {
    	mSectionsPagerAdapter = new CallsFragmentAdapter(getSupportFragmentManager());
    	
    	//mFragments.add(new PostListFragment());
    	mTagsFragment = new TagsFragment();
    	mFragments.add(mTagsFragment);
    	
    	mCallsFragment = new CallsFragment();
    	mFragments.add(mCallsFragment);
    	
    	mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mSectionsPagerAdapter);
    	

        mViewPager.setCurrentItem(1);
    }
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			Intent i = new Intent(this, ServiceDroidActivity.class);
			i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(i);
			return true;
		}
		
		return super.onOptionsItemSelected(item);
	}
	
	@Override
	public void onDialogPositiveClick() {
		 mCallsFragment.deleteCallRelatedData();
	}
	
	@Override
	public void onDialogNegativeClick() {
		//do nothing
	}


	@Override
	public void onGetDirections(String addr) {
		Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse("http://maps.google.com/maps?daddr="+addr));
		startActivity(i);
	}
	
	@Override
	public void onEditCall(long id) {
		Uri uri = ContentUris.withAppendedId(Calls.CONTENT_URI, id);
		Intent i = new Intent(Intent.ACTION_EDIT, uri, this, CallShowActivity.class);
        startActivity(i);
	}
	
	@Override
	public void onCreateCall() {
		Intent i = new Intent(Intent.ACTION_INSERT, Calls.CONTENT_URI, this, CallEditActivity.class);
		startActivity(i);
	}

	
	private class CallsFragmentAdapter extends FragmentPagerAdapter {

		public CallsFragmentAdapter(FragmentManager fm) {
			super(fm);
		}
	
		@Override
		public Fragment getItem(int position) {
			return mFragments.get(position);
		}
	
		@Override
		public int getCount() {
			return mFragments.size();
		}
		
		
	}


	@Override
	public void onTagDialogPositiveClick(long id, String name) {
		// TODO Auto-generated method stub
		if (id != 0) {
			mTagsFragment.saveTag(id, name);
		} else {
			mTagsFragment.saveTag(name);
		}
	}

	@Override
	public void onTagDialogNegativeClick() {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void onTagChecked(int id, String name) {
		mCheckedTags.put(id, name);
		mCallsFragment.filter(mCheckedTags);
	}
	
	@Override
	public void onTagUnchecked(int id, String name) {
		mCheckedTags.remove(id);
		mCallsFragment.filter(mCheckedTags);
	}
	
}
