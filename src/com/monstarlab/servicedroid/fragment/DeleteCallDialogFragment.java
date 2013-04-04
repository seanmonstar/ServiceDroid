package com.monstarlab.servicedroid.fragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;

import com.actionbarsherlock.app.SherlockDialogFragment;
import com.monstarlab.servicedroid.R;

public class DeleteCallDialogFragment extends SherlockDialogFragment {

	public static final String CALL_ID = "callId";
	
	public static DeleteCallDialogFragment create(long id) {
		DeleteCallDialogFragment dialog = new DeleteCallDialogFragment();
		Bundle fragmentArgs = new Bundle();
		fragmentArgs.putLong(CALL_ID, id);
		dialog.setArguments(fragmentArgs);
		return dialog;
	}
	
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

		builder.setTitle(getString(R.string.delete_call_title))
			.setMessage(getString(R.string.delete_call_prompt))
	       .setCancelable(false)
	       .setPositiveButton(R.string.delete_call_prompt_yes, new DialogInterface.OnClickListener() {
	           public void onClick(DialogInterface dialog, int id) {
	               mListener.onDialogPositiveClick();
	           }
	       })
	       .setNegativeButton(R.string.delete_call_prompt_no, new DialogInterface.OnClickListener() {
	           public void onClick(DialogInterface dialog, int id) {
	        	   mListener.onDialogNegativeClick();
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
            mListener = (DeleteCallDialogListener) activity;
        } catch (ClassCastException e) {
            // The activity doesn't implement the interface, throw exception
            throw new ClassCastException(activity.toString()
                    + " must implement DeleteCallDialogListener");
        }
    }


	private DeleteCallDialogListener mListener;
	
	public interface DeleteCallDialogListener {
		public void onDialogPositiveClick();
        public void onDialogNegativeClick();
    }
}
