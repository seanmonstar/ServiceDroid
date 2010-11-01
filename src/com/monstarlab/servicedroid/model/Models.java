package com.monstarlab.servicedroid.model;

import android.net.Uri;
import android.provider.BaseColumns;

public final class Models {

	public static final String AUTHORITY = "com.monstarlab.servicedroid";
	
	private Models() {}
	
	public static final class TimeEntries implements BaseColumns {
		
		private TimeEntries() {}
		
		public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/time_entries");
		
		public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.monstarlab.timeentry";
		
		public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.monstarlab.timeentry";
		
		public static final String DEFAULT_SORT_ORDER = "date ASC";
		
		/**
		 * 	type: date
		 */
		public static final String DATE = "date";
		
		/**
		 * 	type: integer
		 */
		public static final String LENGTH = "length";
	}
	
	public static final class Calls implements BaseColumns {
		
		private Calls() {}
		
		public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/calls");
		
		public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.monstarlab.call";
		
		public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.monstarlab.call";
		
		public static final String DEFAULT_SORT_ORDER = "name ASC";
		
		/**
		 * 	type: varchar(128)
		 */
		public static final String NAME = "name";
		
		/**
		 * 	type: varchar(128)
		 */
		public static final String ADDRESS = "address";
		
		/**
		 * 	type: text
		 */
		public static final String NOTES = "notes";
		
		/**
		 * 	type: date
		 */
		public static final String DATE = "date";
		
		/**
		 * 	type: boolean
		 */
		public static final String BIBLE_STUDY = "bible_study"; // TODO- rename to is_bible_study
	}
	
	public static final class ReturnVisits implements BaseColumns {
		
		private ReturnVisits() {}
		
		public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/returnvisits");
		
		public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.monstarlab.returnvisit";
		
		public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.monstarlab.returnvisit";
		
		public static final String DEFAULT_SORT_ORDER = "date DESC";
		
		/**
		 * 	type: date
		 */
		public static final String DATE = "date";
		
		/**
		 * 	type: varchar(128)
		 */
		public static final String CALL_ID = "call_id";
		
	}
	
	public static final class Placements implements BaseColumns {
		
		private Placements() {}
		
		public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/placements");
		
		public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.monstarlab.placement";
		
		public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.monstarlab.placement";
		
		public static final String DEFAULT_SORT_ORDER = "_id ASC";
		
		/**
		 * 	type: date
		 */
		public static final String DATE = "date";
		
		/**
		 * 	type: date
		 */
		public static final String CALL_ID = "call_id";
		
		/**
		 * 	type: date
		 */
		public static final String LITERATURE_ID = "literature_id";
		
	}
	
	public static final class Literature implements BaseColumns {
		
		private Literature() {}
		
		public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/literature");
		
		public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.monstarlab.literature";
		
		public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.monstarlab.literature";
		
		public static final String DEFAULT_SORT_ORDER = "_id ASC";
		
		/**
		 * 	type: date
		 */
		public static final String TITLE = "title";
		
	}
	
	
	
}
