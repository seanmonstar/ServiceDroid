package com.monstarlab.servicedroid.activity;

import java.text.ParseException;
import java.util.Date;


import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.monstarlab.servicedroid.model.Models.Calls;
import com.monstarlab.servicedroid.model.Models.Literature;
import com.monstarlab.servicedroid.model.Models.Placements;
import com.monstarlab.servicedroid.util.TimeUtil;

import com.monstarlab.servicedroid.R;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.TextView;


public class PlacementActivity extends SherlockActivity {

	private static final String TAG = "PlacementActivity";
	
	private static final int STATE_INSERT = 0;
	private static final int STATE_EDIT = 1;

	private static final int MENU_DELETE = Menu.FIRST;
	
	private static final int YEAR_CUTOFF = 1999;
	
	private static final String[] PROJECTION = new String[] { Placements._ID, Placements.CALL_ID, Placements.LITERATURE_ID, Placements.DATE, Placements.QUANTITY };
	private static final String[] LITERATURE_PROJECTION = new String[] { Literature._ID, Literature.TITLE, Literature.PUBLICATION, Literature.TYPE };
	
	private static final int DIALOG_CREATE_ID = 0;
	private static final int DIALOG_DATE_ID = 1;

	
	private int mState;
	private Uri mUri;
	private Cursor mCursor;
	private int mPlacementType;
	private boolean mIsCancelled;
	
	private Spinner mPublicationSpinner;
	private Spinner mMonthSpinner;
	private Spinner mYearSpinner;
    private EditText mQuantityText;
	
	private String mMagazine;
	private String mBook;
	private int mCallId;
	private int mLitID;
	private int mMonth;
	private int mYear;
	private int mPlaceYear;
	private int mPlaceMonth;
	private int mPlaceDay;
	
	private TimeUtil mTimeHelper;
	private Button mDateBtn;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		mTimeHelper = new TimeUtil(this);
		
		//load in the values, if editting
		final Intent intent = getIntent();
		final String action = intent.getAction();
		if (Intent.ACTION_EDIT.equals(action)) {
			mState = STATE_EDIT;
			mUri = intent.getData();
			mCallId = intent.getIntExtra(Calls._ID, 0);
			retrievePlacementDetails(mUri);
			
		} else if (Intent.ACTION_INSERT.equals(action)) {
			mState = STATE_INSERT;
			mCallId = intent.getIntExtra(Calls._ID, 0);
			if(mCallId == 0) {
				Log.e(TAG, "Call ID wasn't passed, exiting");
				finish();
				return;
			}
			ContentValues values = new ContentValues();
			values.put(Placements.CALL_ID, mCallId);
			values.put(Placements.DATE, TimeUtil.getCurrentDateSQLText());
			mUri = getContentResolver().insert(intent.getData(), values);
			mPlacementType = intent.getIntExtra("type", Literature.TYPE_MAGAZINE);
			
			if(mUri == null) {
				Log.e(TAG, "Failed to insert a blank row into " + getIntent().getData());
				finish();
				return;
			}
			
			setResult(RESULT_OK, (new Intent()).setAction(mUri.toString()));
		} else {
			mState = -1;
			Log.e(TAG, "Unknown action, exiting");
			finish();
			return;
		}
		
		//setup view
		if (mPlacementType == Literature.TYPE_MAGAZINE) {
			setupMagazineView();
		} else  {
			setupNonPeriodicalView();
		}
		
		setupDateButton();
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mQuantityText = (EditText) findViewById(R.id.quantity);
        mQuantityText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() == 0) {
                    mQuantityText.setText("0");
                } else {
                    saveQuantity();
                }
            }
        });
		
		Button confirm = (Button) findViewById(R.id.confirm);
		confirm.setOnClickListener(new View.OnClickListener() {

			public void onClick(View view) {
				finish();
			}
			
		});
		
		Button cancel = (Button) findViewById(R.id.cancel);
		cancel.setOnClickListener(new View.OnClickListener() {

			public void onClick(View view) {
				mIsCancelled = true;
				finish();
			}
		});
		
		
	}
	
	private void setupDateButton() {
		mDateBtn = (Button) findViewById(R.id.date);
		
		mDateBtn.setOnClickListener(new View.OnClickListener() {

			public void onClick(View view) {
				showDialog(DIALOG_DATE_ID);
			}
			
		});
	}

	private void retrievePlacementDetails(Uri uri) {
		long placementId = Long.parseLong(uri.getPathSegments().get(1));
		
		
		Cursor c = getContentResolver().query(ContentUris.withAppendedId(Placements.DETAILS_CONTENT_URI, placementId), new String[] { Placements._ID, Literature.TYPE, Literature.PUBLICATION }, null, null, null);
		if(c != null) {
			if(c.getCount() > 0) {
				c.moveToFirst();
				mPlacementType = c.getInt(1);
				if(mPlacementType == Literature.TYPE_MAGAZINE) {
					//set magazine, month, and year
					String publication = c.getString(2);
					int lastSpaceIndex = publication.lastIndexOf(" ");
					mMagazine = publication.substring(0, lastSpaceIndex);
					mMonth =  Integer.parseInt(publication.substring(lastSpaceIndex+1, publication.indexOf("/")));
					mYear = Integer.parseInt(publication.substring(publication.indexOf("/")+1));
				} else {
					//set book
					mBook = c.getString(2);
				}
			}			
			c.close();
		}
		
		
	}

	@Override
	protected void onResume() {
		super.onResume();
		mCursor = managedQuery(mUri, PROJECTION, null, null, null);
		if(mCursor != null) {
			
			//grab date
			if(mCursor.getCount() > 0) {
				mCursor.moveToFirst();
				String date = mCursor.getString(3);
				try {
					Date d = mTimeHelper.parseDateText(date);
					mPlaceYear = d.getYear() + 1900;
					mPlaceMonth = d.getMonth();
					mPlaceDay = d.getDate();
				} catch (ParseException e) {
					mPlaceYear = TimeUtil.getCurrentYear();
					mPlaceMonth = TimeUtil.getCurrentMonth() - 1;
					mPlaceDay = TimeUtil.getCurrentDay();
				}
				updateDateButton();
                mQuantityText.setText("" + mCursor.getInt(mCursor.getColumnIndex(Placements.QUANTITY)));
			}
			
			if (mPlacementType == Literature.TYPE_MAGAZINE) {
				
				
			} else {
				updateNonPeriodicalSpinner();
			}
			
		}
	}
	
	private void updateDateButton() {
		if(mCursor != null) {
			if(mCursor.getCount() > 0) {
				mCursor.moveToFirst();
				String date = buildDateString();
				mDateBtn.setText(mTimeHelper.normalizeDate(date));
			}
		}
		
		
	}

	@Override
	protected void onPause() {
		super.onPause();
		
		if(mCursor != null) {
			
			int publication = mLitID;
			
			
			
			
			if(isFinishing() && (publication == 0)) {
				//when finishing, if no publication was picked, just ditch the whole thing. its useless anyways
				setResult(RESULT_CANCELED);
				deleteEntry();
			
			} else if (isFinishing() &&  mIsCancelled) {
				//if cancelled, don't delete, but dont save either
				setResult(RESULT_CANCELED);
			} else {
				//save the current changes to the Provider
				ContentValues values = new ContentValues();
				values.put(Placements.LITERATURE_ID, publication);
				
				getContentResolver().update(mUri, values, null, null);
				
				Intent i = getIntent();
			 	i.setAction(Intent.ACTION_EDIT);
			 	i.setData(mUri);
			}
		}
	}
	
	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        boolean result = super.onCreateOptionsMenu(menu);
 
        menu.add(0, MENU_DELETE, 1, R.string.delete_placement).setIcon(android.R.drawable.ic_menu_delete);
            
        return result;
    }
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			Intent i = new Intent(this, CallShowActivity.class);
			i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			Uri uri =  ContentUris.withAppendedId(Calls.CONTENT_URI, mCallId);
			i.setData(uri);
			startActivity(i);
			return true;
		case MENU_DELETE:
			mLitID = 0;
			finish();
			break;
		}
		
		return super.onOptionsItemSelected(item);
	}
	
	@Override
	protected Dialog onCreateDialog(int id) {
		Dialog dialog;
		switch(id) {
	    case DIALOG_CREATE_ID:
	    	dialog = makeCreateLiteratureDialog();
	        break;
	    case DIALOG_DATE_ID:
	    	dialog = makeDateDialog();
	    	break;
	    default:
	        dialog = null;
	    }
		return dialog;
	}
	
	private Dialog makeDateDialog() {
		return new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
			
			public void onDateSet(DatePicker view, int year, int month, int day) {
				mPlaceYear = year;
				mPlaceMonth = month;
				mPlaceDay = day;
				savePlaceDate();
				
			}

		}, mPlaceYear, mPlaceMonth, mPlaceDay);
	}

	private Dialog makeCreateLiteratureDialog() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);

		builder.setTitle(R.string.create_placement);

        // Set an EditText view to get user input 
        final EditText input = new EditText(this);
        builder.setView(input);

        builder.setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
	        public void onClick(DialogInterface dialog, int whichButton) {
			    String value = input.getText().toString();
			    createLiterature(value);
			    updateNonPeriodicalSpinner();
	        }
        });

        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
		    public void onClick(DialogInterface dialog, int whichButton) {
		        dialog.cancel();
		    }
        });
		return builder.create();
	}
	
	private void createLiterature(String publication) {
		ContentValues values = new ContentValues();
		values.put(Literature.PUBLICATION, publication);
		values.put(Literature.TYPE, mPlacementType);
		
		getContentResolver().insert(Literature.CONTENT_URI, values);
		mBook = publication;
	}

	private void setupMagazineView() {
		setContentView(R.layout.place_magazine);
		
		final String watchtower = getString(R.string.watchtower);
		final String awake = getString(R.string.awake);
		final String combo = getString(R.string.combo);
		
		final String[] magChoices = new String[] { watchtower, awake, combo };
		
		mPublicationSpinner = (Spinner) findViewById(R.id.magazine);
		ArrayAdapter<CharSequence> adapter = new ArrayAdapter<CharSequence>(this, android.R.layout.simple_spinner_item, magChoices);
	    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
	    mPublicationSpinner.setAdapter(adapter);
	    
	    if(mMagazine == null) {
	    	mMagazine = combo;
	    }
	    if(mMonth == 0) {
	    	mMonth = TimeUtil.getCurrentMonth();
	    }
	    if(mYear == 0) {
	    	mYear = TimeUtil.getCurrentYear();
	    }
	    
	    mPublicationSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {

			public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
				mMagazine = parent.getItemAtPosition(pos).toString();
				saveSelectedLiterature();
			}

			public void onNothingSelected(AdapterView<?> parent) {
				
			}
	    	
		});
	    
	    int startingSelection = mMonth % 2 == 0 ? 0 : 1;
	    for (int i = 0; i < magChoices.length; i++) {
	    	if (mMagazine.equals(magChoices[i])) {
	    		startingSelection = i;
	    		break;
	    	}
	    }
	    
	    mPublicationSpinner.setSelection(startingSelection);
	    
	    
	    
	    mMonthSpinner = (Spinner) findViewById(R.id.month);
	    adapter = ArrayAdapter.createFromResource(this, R.array.months_array, android.R.layout.simple_spinner_item);
	    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
	    mMonthSpinner.setAdapter(adapter);
	    mMonthSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {

			public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
				mMonth = pos + 1;
				saveSelectedLiterature();
			}

			public void onNothingSelected(AdapterView<?> parent) {
				
			}
	    	
		});
	    mMonthSpinner.setSelection(mMonth - 1);
	    
	    
	    mYearSpinner = (Spinner) findViewById(R.id.year);
	    String[] years = getYearRange();
	    adapter = new ArrayAdapter<CharSequence>(this, android.R.layout.simple_spinner_item, years);
	    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
	    mYearSpinner.setAdapter(adapter);
	    mYearSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {

			public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
				mYear = Integer.parseInt(parent.getItemAtPosition(pos).toString());
				saveSelectedLiterature();
			}

			public void onNothingSelected(AdapterView<?> parent) {
				
			}
	    	
		});
	    
	    int yearsDiff = mYear - YEAR_CUTOFF;
	    // Ex: [2013, 2012, 2011, 2010, ...
		mYearSpinner.setSelection(years.length - yearsDiff);
	    
	}
	
	private String[] getYearRange() {
		int latestYear = getLatestYear();
		
		// 2013 - 2000
		int numOfYears = latestYear - YEAR_CUTOFF; 
		
		String[] years = new String[numOfYears];		
		
		for (int i = 0; i < years.length; i++) {
			years[i] = "" + (latestYear - i);
		}
		
		return years;
	}
	
	private int getLatestYear() {
		int month = TimeUtil.getCurrentMonth();
		int year = TimeUtil.getCurrentYear();
		
		if (month >= TimeUtil.OCTOBER) {
			year += 1;
		}
		
		return year;
	}
	
	private void setupNonPeriodicalView() {
		setContentView(R.layout.place_book);

		TextView title = (TextView) findViewById(R.id.title);

		switch (mPlacementType) {
			case Literature.TYPE_VIDEO:
				title.setText(R.string.video);
				break;
			case Literature.TYPE_BOOK:
				title.setText(R.string.book);
				break;
			case Literature.TYPE_BROCHURE:
				title.setText(R.string.brochure);
				break;
			case Literature.TYPE_TRACT:
				title.setText(R.string.tract);
				break;
			default:
				Log.e(TAG, "Unknown placement type");
				break;
		}
		
		mPublicationSpinner = (Spinner) findViewById(R.id.placement);
		mPublicationSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {

			public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
				String item = parent.getItemAtPosition(pos).toString();
				int length = parent.getAdapter().getCount();
				if(pos == length - 1) {
					// Last item is "Other..."
					showDialog(DIALOG_CREATE_ID);
				} else {
					mBook = item;
					saveSelectedLiterature();
				}
				
			}

			public void onNothingSelected(AdapterView<?> parent) {
				
			}
	    	
		});
	}
	
	private void updateNonPeriodicalSpinner() {
		Cursor c = getContentResolver().query(Literature.CONTENT_URI, LITERATURE_PROJECTION, Literature.TYPE + "=?", new String[] { ""+mPlacementType }, null);
		if(c != null) {
			int length = c.getCount() + 1;
			String[] publications = new String[length];
			int index = 0;
			int selectedIndex = -1;
			if(c.getCount() > 0) {
				c.moveToFirst();
				String title = null;
				while(!c.isAfterLast()) {
					title = c.getString(2); // 2 == PUBLICATION
					publications[index] = title;
					if(!TextUtils.isEmpty(mBook) && mBook.equals(title)) {
						selectedIndex = index;
					}
					c.moveToNext(); 
					index++;
				}
			}
			publications[index] = getString(R.string.other);
			c.close();
			c = null;
			
			ArrayAdapter<CharSequence> adapter = new ArrayAdapter<CharSequence>(this, android.R.layout.simple_spinner_item, publications);
			adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
			
			mPublicationSpinner.setAdapter(adapter);
			
			if(selectedIndex != -1) {
				mPublicationSpinner.setSelection(selectedIndex);
			}
		}
	}
	
	private String buildDateString() {
		return ""+mPlaceYear + "-" + TimeUtil.pad(mPlaceMonth + 1) + "-" + TimeUtil.pad(mPlaceDay);
	}
	
	private void savePlaceDate() {
		ContentValues values = new ContentValues();
		String date = buildDateString();
		
		values.put(Placements.DATE, date);
		getContentResolver().update(mUri, values, null, null);
		updateDateButton();
	}
	
	private void saveSelectedLiterature() {
		String publication = null;
		int newId = 0;
		Cursor c = null;
		String where = null;
		String[] whereArgs = null;
		if(mPlacementType == Literature.TYPE_MAGAZINE) {
			//query for magazine of same issue
			publication = mMagazine+" "+mMonth+"/"+mYear;
			where = Literature.TYPE +"=? and " + Literature.PUBLICATION + "=?";
			whereArgs = new String[] { ""+mPlacementType, publication };
			c = getContentResolver().query(Literature.CONTENT_URI, LITERATURE_PROJECTION, where, whereArgs, null);
			if(c != null) {
				if(c.getCount() > 0) {
					c.moveToFirst();
					newId = c.getInt(0);
				}
				c.close();
				c = null;
			}
			//if not found, create it
			if(newId == 0) {
				ContentValues values = new ContentValues();
				values.put(Literature.TYPE, mPlacementType);
				values.put(Literature.PUBLICATION, publication);
				
				//adjust WEIGHT for combo placements
				if (mMagazine.equals(getString(R.string.combo))) {
					values.put(Literature.WEIGHT, 2);
				}
				
				Uri insertedLit = getContentResolver().insert(Literature.CONTENT_URI, values);
				newId = Integer.parseInt(insertedLit.getPathSegments().get(1));
			}
			//store the Lit id for Placement use
			mLitID = newId;
		} else {
			//get id of selected book/brochure
			where = Literature.TYPE +"=? and " + Literature.PUBLICATION + "=?";
			whereArgs = new String[] { ""+mPlacementType, mBook };
			c = getContentResolver().query(Literature.CONTENT_URI, LITERATURE_PROJECTION, where, whereArgs, null);
			if(c != null) {
				if(c.getCount() > 0) {
					c.moveToFirst();
					newId = c.getInt(0);
				}
				c.close();
				c = null;
			}
			//store Lit id for Placement use
			mLitID = newId;
		}
	}

    protected void saveQuantity() {
        int quantity = Integer.parseInt(mQuantityText.getText().toString());
        if (quantity < 1) {
            mQuantityText.setText("1");
        } else {
            ContentValues values = new ContentValues();
            values.put(Placements.QUANTITY, quantity);
            getContentResolver().update(mUri, values, null, null);
        }
    }

    public void decrementQuantity(View view) {
        int count = Integer.parseInt(mQuantityText.getText().toString());
        if (count > 1) {
            count--;
            mQuantityText.setText("" + count);
        }
    }

    public void incrementQuantity(View view) {
        int count = Integer.parseInt(mQuantityText.getText().toString());
        count++;
        mQuantityText.setText("" + count);
    }

	private void deleteEntry() {
		if(mCursor != null) {
			mCursor.close();
			mCursor = null;
			getContentResolver().delete(mUri, null, null);
		}
	}
	
}
