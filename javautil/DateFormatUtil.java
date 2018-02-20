package org.cat.util;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class DateFormatUtil {
	public static final String UnitePattern_day = "yyyy-MM-dd";
	public static final String UnitePattern_minute = "yyyy-MM-dd HH:mm";
	public static final String UnitePattern_second = "yyyy-MM-dd HH:mm:ss";
	public static final String UnitePattern_nano = "yyyy-MM-dd HH:mm:ss.SSS";
	public static final String UnitePattern_time = UnitePattern_nano;
	public static final String UnitePattern_onlytime = "HH:mm:ss";
	public static final long Day_millis = 24 * 60 * 60 * 1000;

	private static Map<String, DateFormat> dateFormatCache = new HashMap<String, DateFormat>();

	public static String formatToDay(Date date) {
		return format(date, UnitePattern_day);
	}

	public static String formatToDay() {
		return formatToDay(new Date());
	}

	public static String formatToMinute(Date date) {
		return format(date, UnitePattern_minute);
	}

	public static String formatToMinute() {
		return formatToMinute(new Date());
	}

	public static String formatToSecond(Date date) {
		return format(date, UnitePattern_second);
	}

	public static String formatToSecond() {
		return formatToSecond(new Date());
	}

	public static String formatToUniteTime(Date date) {
		return format(date, UnitePattern_time);
	}

	public static String formatToUniteTime() {
		return formatToUniteTime(new Date());
	}

	public static String formatToOnlytime(Date date) {
		return format(date, UnitePattern_onlytime);
	}

	public static String formatToOnlytime() {
		return formatToOnlytime(new Date());
	}

	public static String format(Date date, String pattern) {
		if (date == null) {
			return null;
		}
		return getFormat(pattern).format(date);
	}

	private static DateFormat getFormat(String pattern) {
		DateFormat format = dateFormatCache.get(pattern);
		if (format == null) {
			format = new SimpleDateFormat(pattern);
			dateFormatCache.put(pattern, format);
		}
		return format;
	}

	public static Date parse(String date, String pattern) {
		try {
			return getFormat(pattern).parse(date);
		} catch (ParseException e) {
			return null;
		}
	}

	public static long addTimeByDay(long time, long day) {
		return time + day * Day_millis;
	}

	public static long diffDay(long stime, long etime) {
		return (etime - stime) / (24 * 60 * 60 * 1000);
	}
	
}
