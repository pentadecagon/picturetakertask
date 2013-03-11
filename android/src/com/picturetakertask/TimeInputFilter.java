package com.picturetakertask;

import android.text.InputFilter;
import android.text.Spanned;
import android.util.Log;

/**
 * Filter for the input into the time interval dialog
 */
public class TimeInputFilter implements InputFilter {
	 
	private int min, max;
 
	public TimeInputFilter(int min, int max) {
		this.min = min;
		this.max = max;
	}
 
	public TimeInputFilter(String min, String max) {
		this.min = Integer.parseInt(min);
		this.max = Integer.parseInt(max);
	}
 
	@Override
	public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {	
		try {
			String inputStr;
			if ((dstart + dend) < (start + end))
			{
				inputStr = source.toString() + dest.toString();
			} else
			{
				inputStr = dest.toString() + source.toString();
			}
			if (inputStr.length() > 2)
			{
				return "";
			}
			int input = Integer.parseInt(inputStr);
			Log.d("camera", "TimeInputFilter: input="+input);
			if (isInRange(min, max, input))
				return null;
		} catch (NumberFormatException nfe) { }		
		return "";
	}
 
	private boolean isInRange(int a, int b, int c) {
		return b > a ? c >= a && c <= b : c >= b && c <= a;
	}
}