/* $MirOS: contrib/hosted/ewe/classes/ewe/sys/Time.java,v 1.2 2008/05/02 20:52:01 tg Exp $ */

/*********************************************************************************
 *  Ewe Virtual Machine - Version 1.14, January 24, 2002                         *
 *  Copyright (c) 2008 Thorsten “mirabilos” Glaser                               *
 *  Copyright (C) 1999-2002 Michael L Brereton <michael_brereton@ewesoft.com>    *
 *  Copyright (C) 1998, 1999 Wabasoft <www.wabasoft.com>                         *
 *  All Rights Reserved                                                          *
 *                                                                               *
 *  This library and virtual machine is free software; you can redistribute      *
 *  it and/or modify it under the terms of the Amended GNU Lesser General        *
 *  Public License distributed with this software.                               *
 *                                                                               *
 *  Under this license, linking this library or part thereof with other files to *
 *  produce an executable does not in itself require the executable to be        *
 *  covered by the GNU Lesser General Public License.                            *
 *                                                                               *
 *  This library and virtual machine is distributed in the hope that it will     *
 *  be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of    *
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.                         *
 *                                                                               *
 *  You should have received a copy of the License along with this software;     *
 *  if not, please download it at: www.ewesoft.com/LGPL.txt                      *
 *                                                                               *
 *********************************************************************************/
package ewe.sys;
/**
* Time identifies a date and time, storing information to millisecond precision only. A Time
* object is displayed as a string in a manner specified by the "format" string. If this value is
* null it will be formatted as by the "defaultFormat" string.<p>
* The format string uses the convention as the java.util.SimpleDateFormat with some specifiers left
* out. What is included are:<p>
* <pre>
* Symbol   Meaning                 Presentation        Example
* ------   -------                 ------------        -------
* y        year                    (Number)            1996
* M        month in year           (Text & Number)     July & 07
* d        day in month            (Number)            10
* h        hour in am/pm (1~12)    (Number)            12
* H        hour in day (0~23)      (Number)            0
* m        minute in hour          (Number)            30
* s        second in minute        (Number)            55
* S        millisecond             (Number)            978
* E        day in week             (Text)              Tuesday
* a        am/pm marker            (Text)              PM
* '        escape for text
* </pre>
* <p>
* Time also implements Textable and getText()/setText() encodes the object in a numeric form that is independant of the format
* used for toString()/fromString().
* </p>
**/
public class Time
extends ewe.data.DataObject implements Value, ewe.util.Textable //MLB
{
/** The year as its full set of digits (year 2010 is 2010). */
public int year;

/** The month in the range of 1 to 12. */
public int month;

/** The day in the range of 1 to the last day in the month. */
public int day;

/** The hour in the range of 0 to 23. */
public int hour;

/** The minute in the range of 0 to 59. */
public int minute;

/** The second in the range of 0 to 59. */
public int second;

/** Milliseconds in the range of 0 to 999. */
public int millis;

/** The day of the week in the range 1 to 7, with 1 being Monday and 7 being Sunday*/
public int dayOfWeek;
/**
* The date format to use for I/O with this Time object.
**/
public String format = null;
/**
* The default format to use if one is not specified.
**/
public static String defaultFormat =
	new Locale().getString(Locale.LONG_DATE_FORMAT,0,0);

//"EEE d-MMMM-yyyy h:mm:ss a";

public String _fields =
"year,month,day,hour,minute,second,millis,dayOfWeek,format";

/**
 * Constructs a time object set to the current date and time.
 */
public Time()
	{
	_nativeCreate();
	}

private native void _nativeCreate();

protected static long epoch;
static {
	epoch = (long)getDays(1,1,1970)*24L*60L*60L*1000L;
	//ewe.sys.Vm.debug("Epoch: "+epoch);
}

//===================================================================
public Time(int day,int month,int year)
//===================================================================
{
	_nativeCreate();
	this.day = day;
	this.month = month;
	this.year = year;
	minute = second = hour = millis = 0;
	update();
}

//===================================================================
public static long convertSystemTime(long time,boolean toSystem)
//===================================================================
{
	return time;
}
//===================================================================
public boolean isValid() {return day > 0;}
//===================================================================

//===================================================================
public static boolean isLeapYear(int year)
//===================================================================
{
	if ((year % 4) != 0) return false;
	if (((year % 100) == 0) && ((year % 400) != 0)) return false;
	return true;
}
static int dim[] = new int[]{31,28,31,30,31,30,31,31,30,31,30,31};

//-------------------------------------------------------------------
private static int getDays(int day,int month, int year)
//-------------------------------------------------------------------
{
	int ret = -1;
	if (year < 1600) return ret;
	int yearsPast = (year-1600-1);
	int leaps = yearsPast/4-(yearsPast/100)+(yearsPast/400);
	if (yearsPast >= 1) leaps++;
	if (month > 2 && isLeapYear(year)) leaps++;
	int daysPast = leaps;
	for (int i = 1; i<month; i++)
		daysPast += dim[i-1];
	daysPast += day-1;
	daysPast += (yearsPast+1)*365;
	return daysPast;
}
private static int getDays(Time t)
{
	return getDays(t.day,t.month,t.year);
}
//===================================================================
public static DateChange dateDifference(Time later,Time earlier,DateChange destination) throws IllegalArgumentException
//===================================================================
{
	if (destination == null) destination = new DateChange();
	else destination.set(0);
	int ld = getDays(later), ed = getDays(earlier);
	if (ld == -1 || ed == -1) throw new IllegalArgumentException();
	destination.totalDays = ld-ed;
	if (destination.totalDays < 0) {
		destination.totalDays = -destination.totalDays;
		destination.negative = true;
		Time t = later;
		later = earlier;
		earlier = t;
	}
	destination.years = later.year-earlier.year;
	if (later.month > earlier.month){
		destination.months = later.month-earlier.month;
	}else if (later.month < earlier.month) {
		destination.years--;
		destination.months = later.month+12-earlier.month;
	}
	if (later.day > earlier.day){
		destination.days = later.day-earlier.day;
	}else if (later.day < earlier.day){
		destination.months--;
		if (destination.months < 0) {
			destination.months = 11;
			destination.years--;
		}
		int d = earlier.day, m = later.month, y = later.year;
		m--; if (m < 1) {
			m = 12;
			y--;
		}
		destination.days = getDays(later)-getDays(d,m,y);
		if (destination.days < 1) destination.days = 1;
	}
	return destination;
}
//===================================================================
public DateChange difference(Time earlier,DateChange destination) throws IllegalArgumentException
//===================================================================
{
	return dateDifference(this,earlier,destination);
}
/*
//===================================================================
public void advance(DateChange change)
//===================================================================
{
	if (change.totalDays != 0){
		setTime(getTime()+((long)change.totalDays)*24*60*60*1000);
		int d = getDays(this)+totalDays;
		setTime(((long)d));
	}else{
		year += change.year;
		month += change.month;
		day += change.day;
		update();
	}
}
//===================================================================
public void retreat(DateChange change)
//===================================================================
{
	if (change.totalDays != 0){
		setTime(getTime()+((long)change.totalDays)*24*60*60*1000);
		int d = getDays(this)+totalDays;
		setTime(((long)d));
	}else{
		year += change.year;
		month += change.month;
		day += change.day;
		update();
	}
}
*/
private static int[] centuries;
private static int maxCentury;
private static int todayPast;
private static Time today;
private static void makeTimes()
{
	if (centuries == null){
		centuries = new int[5];
		centuries[0] = getDays(1,1,maxCentury = 2100);
		centuries[1] = getDays(1,1,2000);
		centuries[2] = getDays(1,1,1900);
		centuries[3] = getDays(1,1,1800);
		centuries[4] = getDays(1,1,1700);
		todayPast = getDays(today = new Time());
	}
}
/**
* This converts the time into a 64-bit time value and represents the number of milliseconds
* since Jan 1, 1970. Although this is the epoch used, it is bad practice to assume that this
* millisecond value is portable. Its purpose is to allow calculations of time past between
* two time values. For a fully portable 64-bit time, use getEncodedTime().
**/
//===================================================================
public long getTime()
//===================================================================
{
	long daysPast = getDays(day,month,year);
	if (daysPast == -1) return -1;
	long ret = daysPast*24*60*60+(long)hour*60*60+(long)minute*60+(long)second;
	ret *= 1000;
	ret += millis;
	return ret-epoch;
	//ewe.sys.Vm.debug("Get: "+day+"/"+month+"/"+year);
}
/**
* This converts a 64-bit absolute time value as provided by getTime() (which represents the number of milliseconds
* since Jan 1, 1970). Although this is the epoch used, it is bad practice to assume that this
* millisecond value is portable. Its purpose is to allow calculations of time past between
* two time values or to move from one Time to another by adding or subtracting millisecond values
* from it. For a fully portable 64-bit time, use setEncodedTime().
* <p>
* This returns this Time value if the time is valid and null if it is not.
**/
//===================================================================
public Time setTime(long source)
//===================================================================
{
	source += epoch;
	if (source < 0) return null;
	millis = (int)(source % 1000);
	source /= 1000;
	second = (int)(source % 60);
	source /= 60;
	minute = (int)(source % 60);
	source /= 60;
	hour = (int)(source % 24);
	source /= 24;
	int daysPast = (int)source;
	dayOfWeek = (((((daysPast%7))+6)-1)%7)+1;
	if (centuries == null) makeTimes();
	if (daysPast == todayPast){
		day = today.day;
		month = today.month;
		year = today.year;
		dayOfWeek = today.dayOfWeek;
	}
	year = maxCentury;
	for (int i = 0; i<centuries.length; i++){
		if (source >= centuries[i]){
			source -= centuries[i];
			break;
		}else
			year -= 100;
	}
	nativeSetTime((int)source,year);
	return this;
}
//-------------------------------------------------------------------
private native void nativeSetTime(int source,int year);
//-------------------------------------------------------------------
/*
{
	for (;;year++){
		boolean leap = true;
		if ((year % 4) != 0) leap = false;
		else
			if (((year % 100) == 0) && ((year % 400) != 0)) leap =  false;
		int total = leap ? 366 : 365;
		if (source >= total) {
			source -= total;
			continue;
		}else{
			this.month = 1;
			source++;
			for (int m = 1; m<=12; m++){
				int dm = dim[m-1];
				if (leap && m == 2) dm++;
				if (source > dm) {
					this.month++;
					source -= dm;
				}else{
					this.day = (int)source;
					//if (day < 1) day = 1;
					this.year = year;
					return;
				}
			}
			//ewe.sys.Vm.debug("Should not be here!");
			return;
		}
	}
}
*/
/**
* Update values like dayOfWeek from the year, month, day and time values. It should also
* check for validity. You should call isValid() after doing update.
**/
//===================================================================
public Time update()
//===================================================================
{
	int dy = day;
	setTime(getTime());
	if (day != dy) {
		//ewe.sys.Vm.debug("Was: "+dy+" is: "+day);
		day = 0;
	}
	return this;
}
//===================================================================
public Time setFormat(String format)
//===================================================================
{
	this.format = format;
	return this;
}
//===================================================================
public String getFormat()
//===================================================================
{
	return format != null ? format : getDefaultFormat();
}
//===================================================================
public String getDefaultFormat()
//===================================================================
{
	return defaultFormat;
}

//===================================================================
public void parse(String dateValue,String dateFormat) throws IllegalArgumentException
//===================================================================
{
	if (dateFormat == null) dateFormat = getFormat();
	if (fromString(dateValue,this,dateFormat,ewe.sys.Vm.getLocale())){
		update();
		if (isValid())
			return;
	}
	throw new IllegalArgumentException();
}
//===================================================================
public void parse(String dateValue) throws IllegalArgumentException
//===================================================================
{
	parse(dateValue,null);
}
//===================================================================
public String format(String dateFormat)
//===================================================================
{
	return toString(this,dateFormat,ewe.sys.Vm.getLocale());
}
//===================================================================
public void fromString(String source)
//===================================================================
{
	fromString(source,Vm.getLocale());
}
//===================================================================
public void fromString(String source,Locale locale)
//===================================================================
{
	setTime(0);
	if (!fromString(source,this,(format == null ? getDefaultFormat() : format),locale))
		day = 0;
	else
		update();
}

public static final int SECOND = 1;
public static final int MINUTE = 2;
public static final int HOUR = 3;
public static final int DAY = 4;
public static final int MONTH = 5;
public static final int YEAR = 6;

/**
* Rounds this time down.
* @param roundTo should be SECOND, MINUTE, HOUR, ...
**/
//===================================================================
public Time roundTo(int roundTo)
//===================================================================
{
		millis = 0;
		if (roundTo == SECOND) return update();
		second = 0;
		if (roundTo == MINUTE) return update();
		minute = 0;
		if (roundTo == HOUR) return update();
		hour = 0;
		if (roundTo == DAY) return update();
		day = 1;
		if (roundTo == MONTH) return update();
		month = 1;
		return update();
}
//===================================================================
public int compareTo(Object other)
//===================================================================
{
	if (!(other instanceof Time)) return 1;
	long me = getTime();
	long you = ((Time)other).getTime();
	if (me > you) return 1;
	else if (me < you) return -1;
	return 0;
}
//===================================================================
public boolean before(Time other)
//===================================================================
{
	return compareTo(other) < 0;
}
//===================================================================
public boolean after(Time other)
//===================================================================
{
	return compareTo(other) > 0;
}
//===================================================================
public String toString()
//===================================================================
{
	return toString(Vm.getLocale());
}
//===================================================================
public String toString(Locale locale)
//===================================================================
{
	return toString(this,(format == null ? getDefaultFormat() : format),locale);
}
//===================================================================
public static String toString(Time t,String format,Locale locale)
//===================================================================
{
	if (locale == null) locale = Vm.getLocale();
	if (format == null) format = locale.getString(Locale.SHORT_DATE_FORMAT,0,0);
	if (format == null) return "";
	StringBuffer sb = new StringBuffer();
	char spec = 0;
	int count = 0;
	int l = format.length();
	for(int i = 0; i<l+1; i++){
		char ch = i == l ? 0 : format.charAt(i);
		if (ch == spec) count++;
		else {
			//System.out.println(count+":"+spec);
			if (spec != 0){
				String add = "????";
				int h = t.hour;
				if (count < 2) count = 0;
				if (count >= 3 && spec == 'd') spec = 'E';
				switch(spec){
				case 'y':
					int y = t.year;
					if (count < 4) y = y%100;
					add = Long.l1.set(y).toString(count,Long.ZERO_FILL);
					break;
				case 'M':
					int m = t.month;
					//if (count < 2) count = 0;
					if (count >= 3) add = locale.getString(count == 3 ? locale.SHORT_MONTH:locale.MONTH,m,0);
					else add = Long.l1.set(m).toString(count,Long.ZERO_FILL);
					break;
				case 'd':
					//if (count < 2) count = 0;
					add = Long.l1.set(t.day).toString(count,Long.ZERO_FILL);
					break;
				case 'h':
					h = h%12;
					if (h == 0) h = 12;
					/* FALLTHROUGH */
				case 'H':
					//if (count < 2) count = 0;
					add = Long.l1.set(h).toString(count,Long.ZERO_FILL);
					break;
				case 'm':
					//if (count < 2) count = 0;
					add = Long.l1.set(t.minute).toString(count,Long.ZERO_FILL);
					break;
				case 'E':
					add = locale.getString(count == 3 ? locale.SHORT_DAY : locale.DAY,t.dayOfWeek,0);
					break;
				case 's':
					add = Long.l1.set(t.second).toString(count,Long.ZERO_FILL);
					break;
				case 'S':
					add = Long.l1.set(t.millis).toString(count,Long.ZERO_FILL);
					break;
				case 't':
				case 'a':
					add = locale.getString(locale.AM_PM,t.hour >= 12 ? 1 : 0,0);
					break;
				}
				sb.append(add);
				spec = 0;
				count = 0;
			}
			if (ch == 0) break;
			if ((ch | 0x20) >= 'a' && (ch | 0x20) <= 'z') {
				spec = ch;
				count = 1;
				continue;
			}else if (ch == '\''){
				int did = 0;
				for (i++;i<l;i++){
					char c2 = format.charAt(i);
					if (c2 == '\'') {
						if (did == 0) sb.append('\'');
						break;
					}else{
						sb.append(c2);
						did++;
					}
				}
				continue;
			}
			sb.append(ch);
		}
	}
	return sb.toString();
}
//===================================================================
public static boolean fromString(String source,Time t,String format,Locale locale)
//===================================================================
{
	if (locale == null) locale = Vm.getLocale();
	StringBuffer sb = new StringBuffer();
	char spec = 0;
	int count = 0;
	int len = format.length();
	int src = 0;

	for(int i = 0; i<len+1; i++){
		char ch = i == len ? 0 : format.charAt(i);
		if (ch == spec){
			count++;
		}else{
			if (spec != 0){
				boolean digit = false;
				int sl = source.length();
				for(;src<sl;src++) if (source.charAt(src) != ' ') break;
				int st;
				int stop = sl;
				if (count >= 2) stop = src+count;
				for (st = src; src<sl && src<stop; src++){
					char sc = source.charAt(src);
					if (st == src) digit = (sc >= '0' && sc <= '9');
					else if (digit) {
						if (sc < '0'|| sc > '9') break;
					}else{
						if ((sc | 0x20) < 'a' || (sc | 0x20) > 'z') break;
					}
				}
				String data = st < sl ? source.substring(st,src) : "";
				int val = Convert.toInt(data);
				if (count >= 3 && spec == 'd') spec = 'E';
				switch(spec){
					case 'y':
						if (!digit) return false;
						t.year = val;
						if (t.year < 1000)
							if (count < 4)
								if (t.year <= 49) t.year += 2000;
								else t.year += 1900;
						break;
					case 'M':
						if (count > 2){
							if (digit) return false;
							val = locale.fromString(count == 3 ? locale.SHORT_MONTH:locale.MONTH,data,0);
						}else{
							if (!digit) return false;
						}
						if (val < 1 || val > 12) return false;
						t.month = val;
						break;
					case 'd':
						if (!digit) return false;
						t.day = val;
						break;
					case 'H':
					case 'h':
						if (val < 0 || val > 23) return false;
						t.hour = val;
						break;
					case 'm':
						if (val < 0 || val > 59) return false;
						t.minute = val;
						break;
					case 's':
						if (val < 0 || val > 59) return false;
						t.second = val;
						break;
					case 'S':
						if (val < 0 || val > 999) return false;
						t.millis = val;
						break;
					case 't':
					case 'a':
						val = locale.fromString(locale.AM_PM,data,0);
						if (val == -1) return false;
						if (val == 0) {
							if (t.hour == 12) t.hour = 0;
							else if (t.hour > 12) return false;
						}else{
							if (t.hour < 12) t.hour += 12;
						}
						break;
				}
			}
			//spec == 0;
			spec = 0;
			count = 0;
// Starting a new sequence
			if (ch == 0) break;
			if ((ch | 0x20) >= 'a' && (ch | 0x20) <= 'z') {
				spec = ch;
				count = 1;
				continue;
			}else if (ch == '\''){
				int did = 0;
				for (i++;i<len;i++){
					char c2 = format.charAt(i);
					if (c2 == '\'') {
						if (did == 0) src++;
						break;
					}else{
						src++;
						did++;
					}
				}
				continue;
			}
			//System.out.println("Skipping: "+ch+" as "+(char)source.charAt(src));
			src++;
		}
	}
	return true;
}
/**
* Find the number of days in a month.
**/
//===================================================================
public static int numberOfDays(int month,int year)
//===================================================================
{
	Time t = new Time();
	for (int i = 31; i>1; i--){
		t.day = i;
		t.month = month;
		t.year = year;
		t.minute = t.hour = t.second = 0;
		t.update();
		if (t.isValid()) {
			//System.out.println(t);
			return i;
		}
	}
	return 0;
}
/**
* This returns the index of the day in the week. The input parameter is a value of
* 1 = Monday to 7 = Sunday. The output value is 1 = First day of week to 7 = Last day of week.
**/
//===================================================================
public static int indexOfDayInWeek(int dayOfWeek,Locale locale)
//===================================================================
{
	int firstDay = Convert.toInt(locale.getString(Locale.FIRST_DAY_OF_WEEK,0,0));
	//System.out.println("dayOfWeek: "+dayOfWeek+" firstDay: "+firstDay);
	int diff = dayOfWeek-firstDay;
	if (diff < 0) diff += 7;
	return diff+1;
}
/**
* This converts to a 64-bit encoded values saving the year, month, day, hours, min, sec, millisec in
* a platform independent manner. This value should not be used for calculations but only for storage
* or transmission.
**/
//===================================================================
public long getEncodedTime()
//===================================================================
{
	long lowWord = (millis & 0x3ff)| ((second << 10) & 0xfc00) | ((minute << 16) & 0x3f0000) | ((hour << 22) & 0x7c00000);
	long highWord = (day & 0x1f) | ((month << 5) & 0x1e0) | ((year << 9) & 0x1fffe00);
	return highWord << 32 | lowWord;
}
/**
* This converts from a 64-bit encoded values saving the year, month, day, hours, min, sec, millisec in
* a platform independent manner. This value should not be used for calculations but only for storage
* or transmission.
**/
//===================================================================
public Time setEncodedTime(long from)
//===================================================================
{
	long lowWord = from & 0xffffffff;
	millis = (int)(lowWord & 0x3ff);
	lowWord = lowWord >> 10; second = (int)(lowWord & 0x3f);
	lowWord = lowWord >> 6; minute = (int)(lowWord & 0x3f);
	lowWord = lowWord >> 6; hour = (int)(lowWord & 0x1f);

	long highWord = (from >> 32) & 0xffffffff;
	day = (int)(highWord & 0x1f);
	highWord >>= 5; month = (int)(highWord & 0xf);
	highWord >>= 4; year = (int)(highWord & 0xffff);

	update();
	return isValid() ? this : null;
}
/**
* Compare two encoded times.
* @param one An encoded time from getEncodedTime().
* @param two An encoded time from getEncodedTime().
* @param ignoreDate if this is true then the date portion will be ignored.
* @param ignoreTime if this is true then the time portion will be ignored.
* @return less than 0 if one is less than two, greater than 0 if one is greater than two, 0 if
* they are equal.
*/
//===================================================================
public static int compareEncodedTimes(long one, long two, boolean ignoreDate, boolean ignoreTime)
//===================================================================
{
	if (!ignoreDate){
		int hiOne = (int) (one >> 32);
		int hiTwo = (int) (two >> 32);
		int o = hiOne & 0x01fffe00;
		int t = hiTwo & 0x01fffe00;
		if (o > t) return 1;
		else if (o < t) return -1;
		o = hiOne & 0x1e0;
		t = hiTwo & 0x1e0;
		if (o > t) return 1;
		else if (o < t) return -1;
		o = hiOne & 0x1f;
		t = hiTwo & 0x1f;
		if (o > t) return 1;
		else if (o < t) return -1;
	}
	if (!ignoreTime){
		int hiOne = (int)one;
		int hiTwo = (int)two;
		int o = hiOne & 0x7c00000;
		int t = hiTwo & 0x7c00000;
		if (o > t) return 1;
		else if (o < t) return -1;
		o = hiOne & 0x3f0000;
		t = hiTwo & 0x3f0000;
		if (o > t) return 1;
		else if (o < t) return -1;
		o = hiOne & 0xfc00;
		t = hiTwo & 0xfc00;
		if (o > t) return 1;
		else if (o < t) return -1;
		o = hiOne & 0x3ff;
		t = hiTwo & 0x3ff;
		if (o > t) return 1;
		else if (o < t) return -1;
	}
	return 0;
}
//===================================================================
public String getText()
//===================================================================
{
	return ewe.sys.Convert.toString(getEncodedTime());
}
//===================================================================
public void setText(String text)
//===================================================================
{
	setEncodedTime(ewe.sys.Convert.toLong(text));
}
//===================================================================
public boolean equals(Object other)
//===================================================================
{
	if (!(other instanceof Time)) return super.equals(other);
	return getTime() == ((Time)other).getTime();
}
//===================================================================
public int hashCode()
//===================================================================
{
	return (int)getTime();
}
/**
 * Set the time to be the current time.
 * @return this Time.
 */
//===================================================================
public Time setToCurrentTime()
//===================================================================
{
	_nativeCreate();
	return this;
}
}
