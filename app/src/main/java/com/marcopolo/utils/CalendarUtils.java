package com.marcopolo.utils;

import java.util.Calendar;
import java.util.Date;

public class CalendarUtils {
	
	private static CalendarUtils calendarUtils = null;
	//private Calendar calendar;
	public static CalendarUtils getInstance(){
		if (calendarUtils == null) {
			calendarUtils = new CalendarUtils();
		}
		return calendarUtils;
	}
	
	public CalendarBean getCalendarData(Calendar calendar){
		CalendarBean dateTimeObject = new CalendarBean();
		dateTimeObject._milliSecond = calendar.getTimeInMillis();
		dateTimeObject._date = calendar.get(Calendar.DAY_OF_MONTH);
		dateTimeObject._hour = calendar.get(Calendar.HOUR_OF_DAY);
		dateTimeObject._minute = calendar.get(Calendar.MINUTE);
		dateTimeObject._month = calendar.get(Calendar.MONTH);
		return dateTimeObject;
	}
	
	
	public CalendarBean getCalendarData(Calendar calendar, Date date){
		calendar.setTime(date);
		CalendarBean dateTimeObject = new CalendarBean();
		dateTimeObject._milliSecond = calendar.getTimeInMillis();
		dateTimeObject._date = calendar.get(Calendar.DAY_OF_MONTH);
		dateTimeObject._hour = calendar.get(Calendar.HOUR_OF_DAY);
		dateTimeObject._minute = calendar.get(Calendar.MINUTE);
		dateTimeObject._month = calendar.get(Calendar.MONTH);
		return dateTimeObject;
	}
	
	
	/**
	 * @param
	 * 		Return true if less than current time else false if greater than or equal to current time
	 */
	public boolean isLessThanCurrentTime(Calendar calendar){
		Calendar currentTime = Calendar.getInstance();
		int value = calendar.compareTo(currentTime);// 0 if equals, 1 if greater than, -1 if less than
		if (value < 0) {
			return true;
		}
		return false;
	}
	
	/**
	 * @param
	 * 		Return true if A is Less Than B else false if greater than or equal to current time
	 */
	public boolean is_A_LessThan_B(Calendar a, Calendar b){
		int value = a.compareTo(b);
		if (value < 0) {
			return true;
		}
		return false;
	}
	
	public boolean isEqualTo(Calendar a, Calendar b){
		return a.equals(b);
	}
}
