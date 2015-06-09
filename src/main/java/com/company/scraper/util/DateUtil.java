package com.company.scraper.util;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


public final class DateUtil {

	private static final Logger log = LogManager.getLogger(DateUtil.class);

	private static final String DATE_PATTERN_HH_MM = "HH:mm";
	private static final String DATE_PATTERN_YYYY_MMM_DD_HH_MM = "yyyy MMM dd HH:mm";

	private static final SimpleDateFormat SDF_YYYY_MMM_DD_HH_MM = new SimpleDateFormat(DATE_PATTERN_YYYY_MMM_DD_HH_MM);
	private static final SimpleDateFormat SDF_HH_MM = new SimpleDateFormat(DATE_PATTERN_HH_MM);

	public static final TimeZone UTC_PLUS_3= TimeZone.getTimeZone("GMT+3");
	public static final TimeZone DEFAULT_TIMEZONE= TimeZone.getDefault();

	public static long convertServerDateTo(long time, TimeZone to) {
		return convertTime(time, DEFAULT_TIMEZONE, to);
	}

	public static long convertDatetoServerDate(long time, TimeZone from) {
		return convertTime(time, from, DEFAULT_TIMEZONE);
	}

	public static String formatServerDateTo(long time, TimeZone to) {
		return formatDateToYYYYMMMDDHHmm(convertTime(time, DEFAULT_TIMEZONE, to));
	}

	public static String formatDateToServerDate(long time, TimeZone from) {
		return formatDateToYYYYMMMDDHHmm(convertTime(time, from, DEFAULT_TIMEZONE));
	}

	private static long convertTime(long time, TimeZone from, TimeZone to) {
		return time + getTimeZoneOffset(time, from, to);
	}

	private static long getTimeZoneOffset(long time, TimeZone from, TimeZone to) {
		int fromOffset = from.getOffset(time);
		int toOffset = to.getOffset(time);
		int diff = 0;

		if (fromOffset >= 0){
			if (toOffset > 0){
				toOffset = -1*toOffset;
			} else {
				toOffset = Math.abs(toOffset);
			}
			diff = (fromOffset+toOffset)*-1;
		} else {
			if (toOffset <= 0){
				toOffset = -1*Math.abs(toOffset);
			}
			diff = (Math.abs(fromOffset)+toOffset);
		}

		return diff;
	}

	/**
	 * Convert date in milliseconds to HH:mm format
	 * 
	 * 
	 * @param ms - date in milliseconds
	 * @return formatted date in HH:mm format
	 */
	public static final String formatDateToHHmm(long ms){
		return SDF_HH_MM.format(ms);
	}

	/**
	 * Convert date in milliseconds to yyyy MMM dd HH:mm format
	 * 
	 * @param ms - date in milliseconds
	 * @return
	 */
	public static final String formatDateToYYYYMMMDDHHmm(long ms){
		return SDF_YYYY_MMM_DD_HH_MM.format(ms);
	}

	/**
	 * Calculate the next day shift by adding 1 day to the current date
	 * and setting the startTime parameter as hour and minute
	 * 
	 * @param startTime - HH:mm format
	 * @param timeZone - time zone of the startTime
	 * @return date in milliseconds
	 */
	public static final synchronized long getNextDaySchedule(String startTime, TimeZone from, TimeZone to){
		long result = 0;

		result = getScheduleFromTimeZoneToTimeZone(startTime, from, to);

		Calendar c = Calendar.getInstance();
		c.setTimeInMillis(result);
		c.set(Calendar.DATE, c.get(Calendar.DATE) + 1);

		return result;
	}

	/**
	 * Calculate the next day shift by adding 1 day to the current date
	 * and setting the startTime parameter as hour and minute
	 * 
	 * @param startTime - HH:mm format
	 * @param from - convert from which time zone
	 * @param to - convert to which time zone
	 * 
	 * @return date in milliseconds
	 */
	public static final synchronized long getNextDayScheduleInServerTime(String startTime, TimeZone from){
		String st[] = startTime.split(":");

		long configureTimeZoneDate = convertTime(System.currentTimeMillis(), DEFAULT_TIMEZONE, from);

		Calendar c = Calendar.getInstance();
		c.clear();
		c.setTimeInMillis(configureTimeZoneDate);
		c.set(Calendar.DATE, c.get(Calendar.DATE)+1);
		int hour = Integer.parseInt(st[0]);
		int minute = Integer.parseInt(st[1]);

		if(hour > 11){
			c.set(Calendar.HOUR, hour-12);
			c.set(Calendar.AM_PM, Calendar.PM);
		}else{
			c.set(Calendar.AM_PM, Calendar.AM);
		}
		c.set(Calendar.MINUTE, minute);
		c.set(Calendar.SECOND, c.getActualMinimum(Calendar.SECOND));
		c.set(Calendar.MILLISECOND, c.getActualMinimum(Calendar.MILLISECOND));

		long serverTime = convertTime(c.getTimeInMillis(), from, DEFAULT_TIMEZONE);
		return serverTime;
	}

	/**
	 * Get the schedule converted to server time zone
	 * 
	 * @param startTime - HH:mm format
	 * @param from - convert from which time zone
	 * @return date in milliseconds
	 */
	public static final long getScheduleInServerTime(String startTime, TimeZone from){
		String st[] = startTime.split(":");

		long configureTimeZoneDate = convertTime(System.currentTimeMillis(), DEFAULT_TIMEZONE, from);

		Calendar c = Calendar.getInstance();
		c.clear();
		c.setTimeInMillis(configureTimeZoneDate);

		int hour = Integer.parseInt(st[0]);
		int minute = Integer.parseInt(st[1]);

		if(hour > 11){
			c.set(Calendar.HOUR, hour-12);
			c.set(Calendar.AM_PM, Calendar.PM);
		}else{
			c.set(Calendar.AM_PM, Calendar.AM);
		}
		c.set(Calendar.MINUTE, minute);
		c.set(Calendar.SECOND, c.getActualMinimum(Calendar.SECOND));
		c.set(Calendar.MILLISECOND, c.getActualMinimum(Calendar.MILLISECOND));

		long serverTime = convertTime(c.getTimeInMillis(), from, DEFAULT_TIMEZONE);
		return serverTime;
	}

	public static final long getConfiguredSchedule(String startTime, TimeZone from){
		String st[] = startTime.split(":");

		Calendar c = Calendar.getInstance(from);

		int hour = Integer.parseInt(st[0]);
		int minute = Integer.parseInt(st[1]);

		if(hour > 11){
			c.set(Calendar.HOUR, hour-12);
			c.set(Calendar.AM_PM, Calendar.PM);
		}else{
			c.set(Calendar.AM_PM, Calendar.AM);
		}
		c.set(Calendar.MINUTE, minute);
		c.set(Calendar.SECOND, c.getActualMinimum(Calendar.SECOND));
		c.set(Calendar.MILLISECOND, c.getActualMinimum(Calendar.MILLISECOND));
		
		return c.getTimeInMillis();
	}

	/**
	 * Get the schedule converted to the given time zone
	 * 
	 * @param startTime - HH:mm format
	 * @param from - convert to which time zone
	 * @return date in milliseconds
	 */
	public static final synchronized long getServerTimeScheduleIn(String startTime, TimeZone to){
		String st[] = startTime.split(":");
		Calendar c = Calendar.getInstance();
		c.set(Calendar.HOUR, Integer.parseInt(st[0]));
		c.set(Calendar.MINUTE, Integer.parseInt(st[1]));
		c.set(Calendar.SECOND, c.getActualMinimum(Calendar.SECOND));
		c.set(Calendar.MILLISECOND, c.getActualMinimum(Calendar.MILLISECOND));

		long ms = convertTime(c.getTimeInMillis(), DEFAULT_TIMEZONE, to);
		c.setTimeInMillis(ms);

		return c.getTimeInMillis();
	}

	/**
	 * Converts the given startTime in milliseconds from the give time zone to the given time zone
	 * 
	 * @param startTime - HH:mm format
	 * @param from - Time Zone to convert from
	 * @param to - Time Zone to convert to
	 * @return converted time in ms
	 */
	public static final synchronized long getScheduleFromTimeZoneToTimeZone(String startTime, TimeZone from, TimeZone to){
		String st[] = startTime.split(":");
		Calendar c = Calendar.getInstance(from);
		c.set(Calendar.HOUR, Integer.parseInt(st[0]));
		c.set(Calendar.MINUTE, Integer.parseInt(st[1]));

		long ms = convertTime(c.getTimeInMillis(), from, to);

		c.setTimeInMillis(ms);

		return c.getTimeInMillis();
	}

	/**
	 * Get the difference between two dates
	 * 
	 * @param date1 the oldest date
	 * @param date2 the newest date
	 * @param timeUnit the unit in which you want the difference
	 * @return the difference value, in the provided unit
	 */
	public static long getDateDiff(Date date1, Date date2, TimeUnit timeUnit) {
		long diffInMillies = date2.getTime() - date1.getTime();
		return timeUnit.convert(diffInMillies,TimeUnit.MILLISECONDS);
	}

	public static long convertToDate(String dateStr){
		DateFormat format = new SimpleDateFormat(DATE_PATTERN_YYYY_MMM_DD_HH_MM, Locale.ENGLISH);
		Date date = null;
		try {
			date = format.parse(dateStr);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return date.getTime();
	}
}