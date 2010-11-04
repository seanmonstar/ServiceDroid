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
		
	}
	
	public static final class BibleStudies implements BaseColumns {

		private BibleStudies() {}
		
		public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/biblestudies");
		
		public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.monstarlab.biblestudy";
		
		public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.monstarlab.biblestudy";
		
		public static final String DEFAULT_SORT_ORDER = "date_start ASC";
		
		/**
		 * 	type: date
		 */
		public static final String DATE_START = "date_start";
		
		/**
		 * 	type: date
		 */
		public static final String DATE_END = "date_end";
		
		/**
		 * 	type: varchar(128)
		 */
		public static final String CALL_ID = "call_id";
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
		
		public static final Uri DETAILS_CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/placements/details");
		public static final Uri MAGAZINES_CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/placements/magazines");
		public static final Uri BROCHURES_CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/placements/brochures");
		public static final Uri BOOKS_CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/placements/books");
		
		public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.monstarlab.placement";
		
		public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.monstarlab.placement";
		
		public static final String DEFAULT_SORT_ORDER = "date ASC";
		
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
		
		public static final int TYPE_MAGAZINE = 0;
		public static final int TYPE_BROCHURE = 1;
		public static final int TYPE_BOOK = 2;
		
		/**
		 * 	type: varchar
		 */
		public static final String TITLE = "title";
		
		/**
		 * 	type: int
		 */
		public static final String TYPE = "type";
		
		/**
		 * 	type: varchar
		 */
		public static final String PUBLICATION = "publication";
		
	}
	
	
	
}
