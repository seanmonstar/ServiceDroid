package com.monstarlab.servicedroid.fragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.actionbarsherlock.app.SherlockDialogFragment;
import com.monstarlab.servicedroid.R;

public class TagEditDialogFragment extends SherlockDialogFragment {
	
	public static final String TAG_ID = "tagId";
	public static final String TAG_NAME = "tagName";

	public static TagEditDialogFragment create() {
		TagEditDialogFragment fragment = new TagEditDialogFragment();
		
		return fragment;
	}
	
	public static TagEditDialogFragment create(long id, String existingName) {
		TagEditDialogFragment fragment = new TagEditDialogFragment();
		Bundle args = new Bundle();
		args.putLong(TAG_ID, id);
		args.putString(TAG_NAME, existingName);
		fragment.setArguments(args);
		return fragment;
	}
	
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		final Bundle args = getArguments();
		
		
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		
		builder.setTitle("Tag");
		final EditText input = new EditText(getActivity());
		if (args != null) {
			input.setText(args.getString(TAG_NAME));
		}
		
		LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
		        LinearLayout.LayoutParams.MATCH_PARENT,
		        LinearLayout.LayoutParams.MATCH_PARENT);
		input.setLayoutParams(lp);
		builder.setView(input);
		
		builder.setPositiveButton("Save", new DialogInterface.OnClickListener() {
	           public void onClick(DialogInterface dialog, int id) {
	               
	               String name = input.getText().toString();
	               long tagId = 0;
	               if (args != null) { 
	            	   tagId = args.getLong(TAG_ID);
	               }
	               
	        	   mListener.onTagDialogPositiveClick(tagId, name);
	           }
	       })
	       .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
	           public void onClick(DialogInterface dialog, int id) {
	        	   mListener.onTagDialogNegativeClick();
	           }
	       });
		return builder.create();
	}
	
	@Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        // Verify that the host activity implements the callback interface
        try {
            // Instantiate the NoticeDialogListener so we can send events to the host
            mListener = (TagEditDialogFragmentListener) activity;
        } catch (ClassCastException e) {
            // The activity doesn't implement the interface, throw exception
            throw new ClassCastException(activity.toString()
                    + " must implement TagEditDialogFragmentListener");
        }
    }
	
	private TagEditDialogFragmentListener mListener;
	
	public interface TagEditDialogFragmentListener {
		public void onTagDialogPositiveClick(long id, String name);
        public void onTagDialogNegativeClick();
    }
}
