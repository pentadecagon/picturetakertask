package com.picturetakertask;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.InputFilter;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

/**
 * Custom text field with popup dialog for setting a time interval
 */
public class TimeInputText extends EditText{

	public TimeInterval timeInterval;
	
	public TimeInputText(Context context)
	{
		super(context);
		init();
	}
	
	public TimeInputText(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		init();
	}
	
	public TimeInputText(Context context, AttributeSet attrs, int defStyle)
	{
		super(context, attrs, defStyle);
		init();
	}

	private void init()
	{
		this.setFocusable(false);
		this.setClickable(true);
		this.setOnClickListener(new OnClickListener(){
		        @Override
		        public void onClick(View arg0) {
		        	DialogFragment newFragment = new TimeInputDialog();
		    	    newFragment.show(((MainActivity) getContext()).getSupportFragmentManager(), "time input");
		        }
		    });
	}
	
	/**
	 * Set and display the value of the time interval
	 * 
	 * @param TimeInterval timeInterval
	 */
	public void setValue(TimeInterval timeInterval)
	{
		this.timeInterval = timeInterval;
		setText(timeInterval.format());
	}

	/**
	 * Custom time interval dialog that pops up when the field is clicked
	 */
	public class TimeInputDialog extends DialogFragment {
		
		//edit text fields containing the number of hours, minutes and seconds in the interval
		private EditText etHours, etMin, etSec;
		
		/**
		 * Called when the dialog is created
		 */
		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		    // Get the layout inflater
		    LayoutInflater inflater = getActivity().getLayoutInflater();

		    View v = inflater.inflate(R.layout.dialog_time_interval, null);
		    
		    TextView description = (TextView) v.findViewById(R.id.description);
		    description.setText(TimeInputText.this.getTag()+"");

		    etHours = (EditText) v.findViewById(R.id.editTextHours);
		    etHours.setText(timeInterval.getHr()+"");
		    etHours.setFilters(new InputFilter[]{ new TimeInputFilter("0", "99")});
		    
		    etMin = (EditText) v.findViewById(R.id.editTextMin);
		    etMin.setText(timeInterval.getMin()+"");
		    etMin.setFilters(new InputFilter[]{ new TimeInputFilter("0", "59")});
		    
		    etSec = (EditText) v.findViewById(R.id.editTextSec);
		    etSec.setText(timeInterval.getSec()+"");
		    etSec.setFilters(new InputFilter[]{ new TimeInputFilter("0", "59")});
		    
		    // Inflate and set the layout for the dialog
		    // Pass null as the parent view because its going in the dialog layout
		    builder.setView(v)
		    // Add action buttons
		           .setPositiveButton("OK", new DialogInterface.OnClickListener() {
		               public void onClick(DialogInterface dialog, int id) {
		            		long hr = getLongValue(etHours.getText().toString());
			       			long min  = getLongValue(etMin.getText().toString());
			       			long sec = getLongValue(etSec.getText().toString());
			       			timeInterval.setValue(hr, min, sec);
			       			setText(timeInterval.format());
		               }
		           })
		           .setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
		               public void onClick(DialogInterface dialog, int id) {
		               }
		           });      
		    return builder.create();
		}
	}
	
	/**
	 * Get the long value from the text field input
	 * 
	 * @param String str The text field input
	 * @return long The long value extracted from the input
	 */
	private static long getLongValue(String str)
	{
		if (str.equals(""))
		{
			return 0L;
		}
		return Long.parseLong(str);
	}
}
