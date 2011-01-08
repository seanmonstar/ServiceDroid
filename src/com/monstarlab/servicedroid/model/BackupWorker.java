package com.monstarlab.servicedroid.model;

import com.monstarlab.servicedroid.model.Models.BibleStudies;
import com.monstarlab.servicedroid.model.Models.Calls;
import com.monstarlab.servicedroid.model.Models.Literature;
import com.monstarlab.servicedroid.model.Models.Placements;
import com.monstarlab.servicedroid.model.Models.ReturnVisits;
import com.monstarlab.servicedroid.model.Models.TimeEntries;
import com.monstarlab.servicedroid.util.ServiceDroidDocument;

import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;


/**
 * 
 * @author Sean McArthur
 * @description BackupWorker does the work of serializing data to and from the database to 
 * a BackupAgent or BackupService.
 *
 */
public class BackupWorker {
	
	private static final String[] TIME_ENTRIES_PROJECTION = new String[] { TimeEntries._ID, TimeEntries.LENGTH, TimeEntries.DATE };
	private static final String[] CALLS_PROJECTION = new String[] { Calls._ID, Calls.NAME, Calls.ADDRESS, Calls.NOTES, Calls.DATE, Calls.TYPE };
	private static final String[] RETURN_VISITS_PROJECTION = new String[] { ReturnVisits._ID, ReturnVisits.DATE, ReturnVisits.CALL_ID };
	private static final String[] LITERATURE_PROJECTION = new String[] { Literature._ID, Literature.PUBLICATION, Literature.TITLE, Literature.TYPE };
	private static final String[] PLACEMENTS_PROJECTION = new String[] { Placements._ID, Placements.CALL_ID, Placements.LITERATURE_ID };
	private static final String[] BIBLE_STUDIES_PROJECTION = new String[] { BibleStudies._ID, BibleStudies.DATE_START, BibleStudies.DATE_END, BibleStudies.CALL_ID };
	
	public BackupWorker() {
		
	}
	
	public String backup(ContentResolver resolver) {
		//get all time entries, calls, return visits, placements, and bible studies
		//convert them to XML
		ServiceDroidDocument doc = new ServiceDroidDocument();
		
		pushDataOntoDocument(doc, "TimeEntry", resolver, TimeEntries.CONTENT_URI, TIME_ENTRIES_PROJECTION, null, null, TimeEntries._ID);
		pushDataOntoDocument(doc, "Call", resolver, Calls.CONTENT_URI, CALLS_PROJECTION, null, null, Calls._ID);
		pushDataOntoDocument(doc, "ReturnVisit", resolver, ReturnVisits.CONTENT_URI, RETURN_VISITS_PROJECTION, null, null, ReturnVisits._ID);
		pushDataOntoDocument(doc, "Literature", resolver, Literature.CONTENT_URI, LITERATURE_PROJECTION, null, null, Literature._ID);
		pushDataOntoDocument(doc, "Placement", resolver, Placements.CONTENT_URI, PLACEMENTS_PROJECTION, null, null, Placements._ID);
		pushDataOntoDocument(doc, "BibleStudy", resolver, BibleStudies.CONTENT_URI, BIBLE_STUDIES_PROJECTION, null, null, BibleStudies._ID);
		
		
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
	
	public void restore(String xml) {
		//receive XML, so decode it
		//insert into DB
	}
	
}
