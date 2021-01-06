/**
 * Author : czy
 * Date : 2019年4月24日 下午8:10:13
 * Title : com.riozenc.cfs.common.MonUtils.java
 *
**/
package config.util;

import com.riozenc.titanTool.common.date.DateUtil;

import java.util.Calendar;
import java.util.Date;

public class MonUtils {
	public static String getMon() {
		return DateUtil.getDate("yyyyMM");
	}

	public static String getMon(String date) {
		if (date == null) {
			return DateUtil.getDate("yyyyMM");
		}
		return date;
	}

	public static String getLastMon(String date) {
		Calendar calendar = Calendar.getInstance();
		calendar.set(Calendar.YEAR, Integer.parseInt(date.substring(0, 4)));
		calendar.set(Calendar.MONTH, Integer.parseInt(date.substring(4)) - 1);
		calendar.set(Calendar.DAY_OF_MONTH, 1);
		calendar.add(Calendar.MONTH, -1);
		return DateUtil.getDate(calendar.getTime(), "yyyyMM");
	}

	public static String getNextMon(String date) {
		Calendar calendar = Calendar.getInstance();
		calendar.set(Calendar.YEAR, Integer.parseInt(date.substring(0, 4)));
		calendar.set(Calendar.MONTH, Integer.parseInt(date.substring(4)) - 1);
		calendar.set(Calendar.DAY_OF_MONTH, 1);
		calendar.add(Calendar.MONTH, 1);
		return DateUtil.getDate(calendar.getTime(), "yyyyMM");
	}

	public static Date getDateByMon(String mon, Integer day) {

		Calendar calendar = Calendar.getInstance();
		calendar.set(Calendar.YEAR, Integer.parseInt(mon.substring(0, 4)));
		calendar.set(Calendar.MONTH, Integer.parseInt(mon.substring(4)) - 1);
		calendar.set(Calendar.DAY_OF_MONTH, day);

		return calendar.getTime();

	}

	public static int getMonthDays(int mon) {
		Calendar calendar = Calendar.getInstance();
		calendar.set(Calendar.YEAR, mon / 100);
		calendar.set(Calendar.MONTH, mon % 100 - 1);
		calendar.set(Calendar.DAY_OF_MONTH, 1);
		return calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
	}

}
