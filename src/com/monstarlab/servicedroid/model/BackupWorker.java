package com.monstarlab.servicedroid.model;

import java.util.HashMap;

import com.monstarlab.servicedroid.model.Models.Calls;
import com.monstarlab.servicedroid.model.Models.Literature;
import com.monstarlab.servicedroid.model.Models.Placements;
import com.monstarlab.servicedroid.model.Models.ReturnVisits;
import com.monstarlab.servicedroid.model.Models.TimeEntries;
import com.monstarlab.servicedroid.util.ServiceDroidDocument;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.provider.BaseColumns;
import android.text.TextUtils;


/**
 * 
 * @author Sean McArthur
 * @description BackupWorker does the work of serializing data to and from the database to 
 * a BackupAgent or BackupService.
 *
 */
public class BackupWorker {
	
	private static final String TIME_ENTRY_TAG = "TimeEntry";
	private static final String CALL_TAG = "Call";
	private static final String RETURN_VISIT_TAG = "ReturnVisit";
	private static final String LITERATURE_TAG = "Literature";
	private static final String PLACEMENT_TAG = "Placement";
	
	private static final String[] TIME_ENTRIES_PROJECTION = new String[] { TimeEntries._ID, TimeEntries.LENGTH, TimeEntries.DATE, TimeEntries.NOTE };
	private static final String[] CALLS_PROJECTION = new String[] { Calls._ID, Calls.NAME, Calls.ADDRESS, Calls.NOTES, Calls.DATE, Calls.TYPE };
	private static final String[] RETURN_VISITS_PROJECTION = new String[] { ReturnVisits._ID, ReturnVisits.DATE, ReturnVisits.IS_BIBLE_STUDY, ReturnVisits.NOTE, ReturnVisits.CALL_ID };
	private static final String[] LITERATURE_PROJECTION = new String[] { Literature._ID, Literature.PUBLICATION, Literature.TITLE, Literature.TYPE, Literature.WEIGHT };
	private static final String[] PLACEMENTS_PROJECTION = new String[] { Placements._ID, Placements.CALL_ID, Placements.LITERATURE_ID, Placements.DATE };
	
	private HashMap<String, String> mCallIDReplacements;
	private HashMap<String, String> mLiteratureIDReplacements;
	
	public BackupWorker() {
		
	}
	
	public String backup(ContentResolver resolver) {
		//get all time entries, calls, return visits, placements, and bible studies
		//convert them to XML
		ServiceDroidDocument doc = new ServiceDroidDocument();
		
		pushDataOntoDocument(doc, TIME_ENTRY_TAG, resolver, TimeEntries.CONTENT_URI, TIME_ENTRIES_PROJECTION, "not("+ TimeEntries.LENGTH + " is null)", null, TimeEntries._ID);
		pushDataOntoDocument(doc, CALL_TAG, resolver, Calls.CONTENT_URI, CALLS_PROJECTION, null, null, Calls._ID);
		pushDataOntoDocument(doc, RETURN_VISIT_TAG, resolver, ReturnVisits.CONTENT_URI, RETURN_VISITS_PROJECTION, null, null, ReturnVisits._ID);
		pushDataOntoDocument(doc, LITERATURE_TAG, resolver, Literature.CONTENT_URI, LITERATURE_PROJECTION, Literature._ID + "> 4", null, Literature._ID); // IDs 1-4 are inserted on DB creation.
		pushDataOntoDocument(doc, PLACEMENT_TAG, resolver, Placements.CONTENT_URI, PLACEMENTS_PROJECTION, null, null, Placements._ID);
		
		
		return doc.toString();
	}
	
	protected void pushDataOntoDocument(ServiceDroidDocument doc, String tag, ContentResolver resolver,
			Uri uri, String[] projection, String where, String[] whereArgs, String order) {
		
		Cursor c = resolver.query(uri, projection, where, whereArgs, order);
		if(c.getCount() > 0) {
			c.moveToFirst();
			while(!c.isAfterLast()) {
				String[] values = new String[projection.length];
				for (int i = 0; i < values.length; i++) {
					values[i] = c.getString(c.getColumnIndex(projection[i]));
				}
				doc.addNode(tag, projection, values);
				c.moveToNext();
			}
		}
		c.close();
	}
	
	public boolean restore(ContentResolver resolver, String xml) {
		//receive XML, so decode it
		if (TextUtils.isEmpty(xml)) {
			return false;
		}
		
		ServiceDroidDocument doc = new ServiceDroidDocument(xml);
		if (!doc.isValid()) {
			return false;
		}
		
		mCallIDReplacements = new HashMap<String, String>();
		mLiteratureIDReplacements = new HashMap<String, String>();
		
		// IDs 1-4 are pre-inserted brochures and books
		for (int i = 1; i < 5; i++) {
			mLiteratureIDReplacements.put(""+i, ""+i);
		}
		
		//insert into DB
		// IMPORTANT: order matters here!
		insertDataFromDocument(doc, TIME_ENTRY_TAG, resolver, TimeEntries.CONTENT_URI);
		insertDataFromDocument(doc, CALL_TAG, resolver, Calls.CONTENT_URI);
		insertDataFromDocument(doc, RETURN_VISIT_TAG, resolver, ReturnVisits.CONTENT_URI);
		insertDataFromDocument(doc, LITERATURE_TAG, resolver, Literature.CONTENT_URI);
		insertDataFromDocument(doc, PLACEMENT_TAG, resolver, Placements.CONTENT_URI);
		
		return true;
		
	}
	
	protected void insertDataFromDocument(ServiceDroidDocument doc, String tag, ContentResolver resolver, Uri contentUri) {
		boolean isCall = (tag == CALL_TAG);
		boolean isLiterature = (tag == LITERATURE_TAG);
		
		for (int nodeIndex = 0, numOfNodes = doc.getNumberOfTag(tag); nodeIndex < numOfNodes; nodeIndex++) {
			String[][] data = doc.getDataFromNode(tag, nodeIndex);
			ContentValues values = new ContentValues();
			String oldID = null;
			
			
			for (int attrIndex = 0, numOfAttrs = data[0].length; attrIndex < numOfAttrs; attrIndex++) {
				String key = data[0][attrIndex];
				String value = data[1][attrIndex];
				String newID = null;
				
				//always strip the ID away, we dont want it on insert
				if (key.equals(BaseColumns._ID)) { /* BaseColumns._ID is null, but _i is the same on all Models */
					oldID = value;
					continue;
				}
				
				//CALL_ID and LITERATURE_ID should be replaced
				if(key.equals(ReturnVisits.CALL_ID)) {
					newID = mCallIDReplacements.get(key);
				} else if (key.equals(Placements.LITERATURE_ID)) {
					newID = mLiteratureIDReplacements.get(key);
				}
				
				if (newID != null) {
					value = newID;
				}
				
				
				values.put(key, value);
			}
			
			
			//keep track of inserted Calls and Literature to replace foreign keys above
			Uri insertedUri = resolver.insert(contentUri, values);
			if (oldID != null) {
				if (isCall) {
					mCallIDReplacements.put(oldID, insertedUri.getPathSegments().get(1));
				} else if (isLiterature) {
					mLiteratureIDReplacements.put(oldID, insertedUri.getPathSegments().get(1));
				}
			}
		}
	}
	
}
