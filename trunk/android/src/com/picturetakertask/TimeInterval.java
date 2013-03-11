package com.picturetakertask;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

/**
 * Handles the storage and custom display of time intervals.
 */
public class TimeInterval {
	
	//time interval in milliseconds
	private long timeInMillis;
	//number of hours in interval
	private long hr;
	//number of minutes in interval
	private long min;
	//number of seconds in interval
	private long sec;
	
	/**
	 * Constructor
	 * 
	 * @param long time interval in milliseconds
	 */
	public TimeInterval(long timeInMillis)
	{
		setValue(timeInMillis);
	}
	
	/**
	 * Constructor
	 * 
	 * @param long hr number of hours in interval
	 * @param long min number of minutes in interval
	 * @param long sec number of seconds in interval
	 */
	public TimeInterval(long hr, long min, long sec)
	{
		setValue(hr, min, sec);
	}
	
	/**
	 * Set time interval
	 * 
	 * @param long time interval in milliseconds 
	 */
	public void setValue(long timeInMillis)
	{
		this.timeInMillis = timeInMillis;
		setHrMinSecFromMillis();
	}
	
	/**
	 * Set time interval
	 * 
	 * @param long hr number of hours in interval
	 * @param long min number of minutes in interval
	 * @param long sec number of seconds in interval
	 */
	public void setValue(long hr, long min, long sec)
	{
		this.hr = hr;
		this.min = min;
		this.sec = sec;
		setMillisFromHrMinSec();
	}
	
	/**
	 * Return time interval in milliseconds
	 * 
	 * @return long timeInMillis
	 */
	public long getTimeInMillis()
	{
		return timeInMillis;
	}
	
	/**
	 * Return number of hours in time interval
	 * 
	 * @return long hr
	 */
	public long getHr()
	{
		return hr;
	}
	
	/**
	 * Return number of minutes in time interval
	 * 
	 * @return long min
	 */
	public long getMin()
	{
		return min;
	}
	
	/**
	 * Return number of seconds in time interval
	 * 
	 * @return long sec
	 */
	public long getSec()
	{
		return sec;
	}
	
	/**
	 * Split the time interval in milliseconds into hours, minutes and seconds
	 */
	private void setHrMinSecFromMillis()
	{
		hr = TimeUnit.MILLISECONDS.toHours(timeInMillis);
        min = TimeUnit.MILLISECONDS.toMinutes(timeInMillis - TimeUnit.HOURS.toMillis(hr));
        sec = TimeUnit.MILLISECONDS.toSeconds(timeInMillis - TimeUnit.HOURS.toMillis(hr) - TimeUnit.MINUTES.toMillis(min));
	}
	
	/**
	 * Construct the time interval in milliseconds from the number of hours, minutes and seconds in the interval
	 */
	private void setMillisFromHrMinSec()
	{
		timeInMillis = hr * 3600000 + min * 60000 + sec * 1000;
	}

	/**
	 * Format the interval for display
	 * 
	 * @return String The formatted interval
	 */
	public String format()
	{
        ArrayList<String> list = new ArrayList<String>();
        if (hr > 0)
        {
        	list.add(hr + " hours");
        }
        if (min > 0)
        {
        	list.add(min + " min");
        }
        if (sec > 0)
        {
        	list.add(sec + " sec");
        }
        if (list.size() == 0)
        {
        	list.add("0");
        }
        StringBuilder builder = new StringBuilder();
        for(int i = 0; i < list.size(); i++) {
        	if (i > 0)
        	{
        		builder.append(" ");
        	}
            builder.append(list.get(i));
        }
        return builder.toString();
	}
}
