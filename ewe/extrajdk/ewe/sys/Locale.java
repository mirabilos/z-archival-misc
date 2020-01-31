/* $MirOS: contrib/hosted/ewe/extrajdk/ewe/sys/Locale.java,v 1.2 2008/05/02 20:52:04 tg Exp $ */

/*********************************************************************************
 *  Ewe Virtual Machine - Version 1.14, January 24, 2002                         *
 *  Copyright (c) 2008 Thorsten “mirabilos” Glaser                               *
 *  Copyright (C) 1999-2002 Michael L Brereton <michael_brereton@ewesoft.com>    *
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
import java.text.CollationKey;
import java.text.Collator;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import ewe.reflect.Method;
import ewe.reflect.Reflect;
import ewe.reflect.Wrapper;
import ewe.util.Utils;
//##################################################################
public class Locale{
//##################################################################
protected java.util.Locale myLocale;
/**
* The full name of a month. Associated values must be in the range 1 to 12.
**/
public static final int MONTH = 1;
/**
* The short name of a month. Associated values must be in the range 1 to 12.
**/
public static final int SHORT_MONTH = 2;
/**
* The full name of a DAY IN THE WEEK. Associated values must be in the range 1 to 7.
* A value of 1 implies the first day of the week. This may be the equivalent of Monday
* or Sunday or Saturday(?).
**/
public static final int DAY_OF_WEEK = 3;
/**
* The short name of a DAY IN THE WEEK. Associated values must be in the range 1 to 7.
* A value of 1 implies the first day of the week. This may be the equivalent of Monday
* or Sunday or Saturday(?).
**/
public static final int SHORT_DAY_OF_WEEK = 4;
public static final int CURRENCY = 5;
/**
* The full name of the AM/PM. Associated values must be in the range 0 to 1.
**/
public static final int AM_PM = 6;
/**
* The full name of the day which is equivalent to Monday - Sunday, where 1 == Monday, 2 == Tuesday
* etc.
**/
public static final int DAY = 7;
/**
* The short name of the day which is equivalent to Monday - Sunday, where 1 == Monday, 2 == Tuesday
* etc.
**/
public static final int SHORT_DAY = 8;
/**
* The index of the first day of the week. 1 = Monday, 2 = Tuesday, etc.
**/
public static final int FIRST_DAY_OF_WEEK = 9;
/**
* The full name of the locale language.
**/
public static final int LANGUAGE = 10;
/**
* The ISO English name of the locale language.
**/
public static final int LANGUAGE_ENGLISH = 11;
/**
* The full native name of the locale language.
**/
public static final int LANGUAGE_NATIVE = 12;
/**
* The ISO short name of the locale language.
**/
public static final int LANGUAGE_SHORT = 13;
/**
* The character(s) for the time separator.
**/
public static final int TIME_SEPARATOR = 14;
/**
* The character(s) for the date separator.
**/
public static final int DATE_SEPARATOR = 15;
/**
* The format for the time display.
**/
public static final int TIME_FORMAT = 16;
/**
* The format for the short date display.
**/
public static final int SHORT_DATE_FORMAT = 17;
/**
* The format for the long date display.
**/
public static final int LONG_DATE_FORMAT = 18;
/**
* The correct format for display month and year only.
**/
public static final int MONTH_YEAR_FORMAT = 19;
/**
* The full name of the locale country.
**/
public static final int COUNTRY = 20;
/**
* The full English name of the locale country.
**/
public static final int COUNTRY_ENGLISH = 21;
/**
* The full native name of the locale country.
**/
public static final int COUNTRY_NATIVE = 22;
/**
* The short name of the locale country.
**/
public static final int COUNTRY_SHORT = 23;
/**
* Parameter for format() or parse() method, used when formating a numeric value.
**/
public static final int FORMAT_PARSE_NUMBER = 0x10;
/**
* Parameter for format() or parse() method, used when formating a currency value.
**/
public static final int FORMAT_PARSE_CURRENCY = 0x20;
/**
* Parameter for format() or parse() method, used when formating a date value.
**/
public static final int FORMAT_PARSE_DATE = 0x30;
/**
* Option for format() method when using FORMAT_NUMBER or FORMAT_CURRENCY.
* It specifies that values after the decimal point should not be displayed.
**/
//public static final int NO_DECIMAL_VALUE = 0x100;
/**
* Option for format() method when using FORMAT_NUMBER or FORMAT_CURRENCY.
* It specifies that values before the decimal point should not be grouped.
* e.g. in English, display 1000000 instead of 1,000,000
**/
//public static final int NO_GROUPINGS = 0x20;
/**
* Option for format() method when using  FORMAT_CURRENCY.
* It specifies that currency symbol should not be displayed.
**/
//public static final int NO_CURRENCY_SYMBOL = 0x40;
/**
* Option for format() method when using  FORMAT_PARSE_CURRENCY or FORMAT_PARSE_NUMBER
* It specifies not to round up the last decimal digit. This option cannot be placed
* in the string format specifier.
**/
public static final int NO_ROUNDING = 0x100;
/**
* Option for format() method when using  FORMAT_CURRENCY.
* It specifies that a standard numeric negative notation should
* be used instead of the currency negative notation.
**/
//public static final int NO_CURRENCY_NEGATIVE_NOTATION = 0x100;
/**
* This is the native Win32 definition of a Neutral locale.
**/
public static final int LOCALE_NEUTRAL = 0;
/**
* This is the native Win32 definition of the user default locale.
**/
public static final int LOCALE_USER_DEFAULT = 1024;
/**
* This is the native Win32 definition of the system default locale.
**/
public static final int LOCALE_SYSTEM_DEFAULT = 2048;
/**
* This is the ID to use as the default locale identifier. It is initially
* set to LOCALE_USER_DEFAULT, but it can be changed to any other.
**/
public static int defaultID = LOCALE_USER_DEFAULT;
/**
* This is the value that will be returned by getString() in some instances if the
* value is not known.
**/
public static final String unknownString = "???";

/**
* This checks if the value is either null or the unknownString.
**/
//===================================================================
public static boolean unknown(String value)
//===================================================================
{
	return (value == null || unknownString.equals(value));
}
//===================================================================
public static Locale getDefault()
//===================================================================
{
	return Vm.getLocale();
}

static java.util.Locale [] all = java.text.DateFormat.getAvailableLocales();
static int allLocales [] = new int[all.length];
static {
	for (int i = 0; i<all.length; i++) allLocales[i] = i;
}

static final String countryConversion =
"AFGAFALBALDZADZASMASANDADAGOAOAIAAIATGAGARGARARMAMABWAWAUSAUAUTATAZEAZBHSBSBHRBHBGDBDBRBBBBLRBYBELBEBLZBZBENBJBMUBMBTNBTBOLBOBIHBABWABWBRABRVGBVGBRNBNBGRBGBFABFBDIBIKHMKHCMRCMCANCACPVCVCYMKYCAFCFTCDTDCHLCLCHNCNHKGHKCOLCOCOMKMCOGCGCOKCKCRICRCIVCIHRVHRCUBCUCYPCYCZECZPRKKPCODCDDNKDKDJIDJDMADMDOMDOTMPTPECUECEGYEGSLVSVGNQGQERIERESTEEETHETFROFKFLKFOFJIFJFINFIFRAFRGUFGFPYFPFGABGAGMBGMGEOGEDEUDEGHAGHGIBGIGRCGRGRLGLGRDGDGLPGPGUMGUGTMGTGINGNGNBGWGUYGYHTIHTVATVAHNDHKHUNHUISLISINDINIDNIDIRNIRIRQIQIRLIEISRILITAITJAMJMJPNJPJORJOKAZKZKENKEKIRKIKWTKWKGZKGLAOLALVALVLBNLBLSOLSLBRLRLBYLYLIELILTULTLUXLUMDGMGMWIMWMYSMYMDVMVMLIMLMLTMTMHLMHMTQMQMRTMRMUSMUMEXMXFSMFMMCOMCMNGMNMSRMSMARMAMOZMZMMRMMNAMNANRUNRNPLNPNLDNLANTANNCLNCNZLNZNICNINERNENGANGNIUNUNFKNFMNPMPNORNOOMNOMPAKPKPLWPWPSEPSPANPAPNGPGPRYPYPERPEPHLPHPCNPNPOLPLPRTPTPRIPRQATQAKORKRMDAMDREUREROMRORUSRURWARWSHNSHKNAKNLCALCSPMPMVCTVCWSMWSSMRSMSTPSTSAUSASENSNSYCSCSLESLSGPSGSVKSKSVNSISLBSBSOMSOZAFZAESPESLKALKSDNSDSURSRSJMSJSWZSZSWESECHECHSYRSYTWNTWTJKTJTHATHMKDMKTGOTGTKLTKTONTOTTOTTTUNTNTURTRTKMTMTCATCTUVTVUGAUGUKRUAAREAEGBRGBTZATZUSAUSVIRVIURYUYUZBUZVUTVUVENVEVNMVNWLFWFESHEHYEMYEYUGYUZMBZMZWEZW"
;//.toCharArray();
/**
* This will convert a three letter (uppercase) ISO country code to the two letter (uppercase) ISO country code
* OR the other way. It will return an empty string if no conversion is possible.
**/
//===================================================================
public static String convertCountryCode(String countryCode)
//===================================================================
{
	if (countryCode == null) return "";
	if (countryCode.length() != 2 && countryCode.length() != 3) return "";
	boolean isThree = countryCode.length() == 3;
	int idx = -1;
	while(true){
		if (idx+1 >= countryConversion.length()) return "";
		idx = countryConversion.indexOf(countryCode,idx+1);
		if (idx == -1) return "";
		if (isThree && (idx%5 != 0)) continue;
		if (!isThree){
			if (idx < 3) continue;
			if (((idx-3) % 5) != 0) continue;
		}
		if (isThree) return countryConversion.substring(idx+3,idx+5);
		else return countryConversion.substring(idx-3,idx);
	}
}

/**
* This creates a Locale without an ID. Don't attempt to use it unless you
* call set(int id) on it.
**/
//===================================================================
public Locale(boolean dontSetId)
//===================================================================
{

}
private static String setDefaultLanguage, setDefaultCountry;
private static int setDefaultID = -1;
/**
 * This attempts to set what the default language and (optionally) country for the
 * application.
 * @param language The two letter language code.
 * @param country The three letter country code (optional).
 * @param options If FORCE_CREATION is used as an option BUT the specified language/country does is not supported,
 * then the default locale will
 * still report this language and country as its language and country
 * but will not be able to provide any other info or services.
 * @return true if successful, false if the specified language and country is not supported AND
 * forceSupport is false.
 */
//===================================================================
public static boolean setDefault(String language, String country, int options)
//===================================================================
{
	int setDefaultID = createID(language,country,0);
	boolean forceSupport = (options & FORCE_CREATION) != 0;
	if (setDefaultID == -1){
		if (!forceSupport) return false;
		setDefaultLanguage = language;
		setDefaultCountry = country;
	}else
		setDefaultLanguage = setDefaultCountry = null;
	Vm.l = new Locale();
	return true;
}
//===================================================================
public Locale(String language, String country, boolean forceSupport) throws IllegalArgumentException
//===================================================================
{
	this(true);
	int id = createID(language,country,0);
	if (id != -1){
		set(id);
		return;
	}
	if (!forceSupport) throw new IllegalArgumentException("That Locale is not supported.");
	force(language,country);
}
//===================================================================
public Locale()
//===================================================================
{
	this(LOCALE_USER_DEFAULT);
}
//===================================================================
public Locale(int id)
//===================================================================
{
	set(id);
}
//===================================================================
public void set(int id)
//===================================================================
{
	myLocale = idToLocale(id,this);
}

//-------------------------------------------------------------------
private void force(String lang, String country)
//-------------------------------------------------------------------
{
	forcedLanguage = lang;
	forcedCountry = country;
	myLocale = null;
}
//-------------------------------------------------------------------
static java.util.Locale idToLocale(int id,Locale dest)
//-------------------------------------------------------------------
{
	dest.forcedLanguage = dest.forcedCountry = null;
	if (id < 0 || id > allLocales.length) {
		if (ewe.applet.Applet.localLanguage == null && setDefaultLanguage == null)
			return java.util.Locale.getDefault();
		else {
			String defLanguage = setDefaultLanguage, defCountry = setDefaultCountry;
			if (defLanguage == null) {
				defLanguage = ewe.applet.Applet.localLanguage;
				defCountry = ewe.applet.Applet.localCountry;
			}
			int lid = createID(defLanguage,defCountry,0);
			if (lid == -1) {
				if (dest != null){
					dest.forcedLanguage = defLanguage;
					dest.forcedCountry = defCountry;
					return null;
				}
				return java.util.Locale.getDefault();
			}
			else return all[lid];
		}
	}
	return (all[id]);
}
/**
* (Not implemented) This should create a locale based on
* the ISO two character language and country codes. It will return
* -1 if it cannot create the ID.
*
* @param language the ISO-639 two character language specifier.
* @param country the ISO-3166 <b>three</b> character country specifier.
* @param options options for creating the ID. None are defined yet.
* @return the ID if successful, -1 if not.
**/
//===================================================================
public static int createID(String language,String country,int options)
//===================================================================
{
	int [] all = getAllIDs(0);
	if (all == null) return -1;
	Locale l = new Locale(true);
	if (country != null)
		if (country.trim().length() == 0) country = null;
	for (int i = 0; i<all.length; i++){
		l.set(all[i]);
		String lg = l.getString(LANGUAGE_SHORT,0,0);
		if (lg == null) continue;
		if (!language.equalsIgnoreCase(lg)) continue;
		if (country == null) return all[i];
		String ct = l.getString(COUNTRY_SHORT,0,0);
		if (country.equalsIgnoreCase(ct)) return all[i];
	}
	return -1;
}
/**
* This is an options for createFor() it tells the system
* to create a Locale that always reports the specified language and country
* even if the Locale is not actually supported by the underlying system.
**/
public static final int FORCE_CREATION = 0x1;
/**
* @param language the ISO-639 two character language specifier.
* @param country the ISO-3166 <b>three</b> character country specifier.
* @param options options for creating the ID. The only supported option is FORCE_CREATION
* @return a new Locale if successful, null if not.
**/
//===================================================================
public static Locale createFor(String language,String country,int options)
//===================================================================
{
	int id = createID(language,country,options);
	if (id != -1) return new Locale(id);
	if ((options & FORCE_CREATION) == 0) return null;
	Locale l = new Locale(true);
	l.force(language,country);
	return l;
}
static SimpleDateFormat months = new SimpleDateFormat("MMMM");
static SimpleDateFormat monthsshort = new SimpleDateFormat("MMM");
static SimpleDateFormat days = new SimpleDateFormat("EEEE");
static SimpleDateFormat daysshort = new SimpleDateFormat("EEE");

static Date tempDate = new Date();
//-------------------------------------------------------------------
String getMonth(int which,boolean abbr)
//-------------------------------------------------------------------
{
	tempDate.setDate(1);
	tempDate.setMonth(which-1);
	try{
		SimpleDateFormat df = abbr ? new SimpleDateFormat("MMM",myLocale) : new SimpleDateFormat("MMMM",myLocale);
		return df.format(tempDate);
	}catch(Exception e){
		return "???";
	}
}
//-------------------------------------------------------------------
int getFirstDayOfWeek()
//-------------------------------------------------------------------
{
	int firstDayOfWeek = Time.calendarDayToWabaDay(java.util.Calendar.getInstance(myLocale).getFirstDayOfWeek());
	firstDayOfWeek--;
	return firstDayOfWeek;
}
//-------------------------------------------------------------------
String getDay(int which,boolean abbr)
//-------------------------------------------------------------------
{
	tempDate.setDate(1);
	java.util.Calendar c = java.util.Calendar.getInstance(myLocale);
	c.setTime(tempDate);
	int fm = Time.calendarDayToWabaDay(c.get(c.DAY_OF_WEEK));
	int diff  = ((which-fm)+7)%7;
	tempDate.setDate(1+diff);
	try{
		SimpleDateFormat df = abbr ? new SimpleDateFormat("EEE",myLocale) : new SimpleDateFormat("EEEE",myLocale);
		return df.format(tempDate);
	}catch(Exception e){
		return "???";
	}
}
/*
static String [] months = new String[13];
static String [] monthsshort = new String[13];
static String [] days = new String[7];
static String [] daysshort = new String[7];

static int firstDayOfWeek = -1;
static
{
	firstDayOfWeek = Time.calendarDayToWabaDay(java.util.Calendar.getInstance().getFirstDayOfWeek());
	firstDayOfWeek--;

	SimpleDateFormat df = new SimpleDateFormat("MMMM");
	Date dt = new Date();

	dt.setDate(1);
	for (int i = 0; i<months.length; i++){
		dt.setMonth(i);
		try{
			months[i] = df.format(dt);
		}catch(Exception e){
			months[i] = "";
		}
	}
	dt.setDate(1);
	df = new SimpleDateFormat("MMM");
	for (int i = 0; i<monthsshort.length; i++){
		dt.setMonth(i);
		try{
			monthsshort[i] = df.format(dt);
		}catch(Exception e){
			monthsshort[i] = "";
		}
	}

	java.util.Calendar c = java.util.Calendar.getInstance();
	df = new SimpleDateFormat("EEEE");
	for (int i = 0; i<days.length; i++){
		dt.setDate(i+1);
		c.setTime(dt);
		int which = Time.calendarDayToWabaDay(c.get(c.DAY_OF_WEEK));
		try{
			days[which-1] = df.format(dt);
		}catch(Exception e){
			days[which-1] = "";
		}
	}
	df = new SimpleDateFormat("EEE");
	for (int i = 0; i<daysshort.length; i++){
		dt.setDate(i+1);
		c.setTime(dt);
		int which = Time.calendarDayToWabaDay(c.get(c.DAY_OF_WEEK));
		try{
			daysshort[which-1] = df.format(dt);
		}catch(Exception e){
			daysshort[which-1] = "";
		}
	}
}
*/
private String forcedLanguage,forcedCountry;
/**
* "what" should be one of MONTH, SHORT_MONTH, etc..
* "value" should be a value relative to the "what".
* e.g. if (what == MONTH) value should be a number for 1 to 12.
**/
//===================================================================
public String getString(int what,int forValue,int options)
//===================================================================
{
	if (what == LANGUAGE_SHORT && forcedLanguage != null) return forcedLanguage;
	if (what == COUNTRY_SHORT && forcedLanguage != null)
		return forcedCountry == null ? "" : forcedCountry;
	if (myLocale == null) return unknownString;
	switch(what){
		case(MONTH): return getMonth(forValue,false);
		case(SHORT_MONTH): return getMonth(forValue,true);
		case(DAY): return getDay(forValue,false);
		case(SHORT_DAY): return getDay(forValue,true);

		case DAY_OF_WEEK:
			if (forValue >= 1 && forValue <= 7) {
				forValue = (((forValue-1)+getFirstDayOfWeek())%7)+1;
				return getString(DAY,forValue,options);
			}
			break;
		case SHORT_DAY_OF_WEEK:
			if (forValue >= 1 && forValue <= 7) {
				forValue = (((forValue-1)+getFirstDayOfWeek())%7)+1;
				return getString(SHORT_DAY,forValue,options);
			}
			break;
		case FIRST_DAY_OF_WEEK:
			return ""+(getFirstDayOfWeek()+1);

		case(CURRENCY): return "$";
		case(AM_PM): return (forValue == 0 ? "AM" : "PM");
		case(LANGUAGE_ENGLISH):
			return (myLocale.getDisplayLanguage(myLocale.US));
		case(LANGUAGE):
		case(LANGUAGE_NATIVE):
			return (myLocale.getDisplayLanguage());
		case(LANGUAGE_SHORT):
			return (myLocale.getLanguage());
		case(COUNTRY_ENGLISH):
			return (myLocale.getDisplayCountry(myLocale.US));
		case(COUNTRY):
		case(COUNTRY_NATIVE):
			return (myLocale.getDisplayCountry());
		case(COUNTRY_SHORT):
			return (convertCountryCode(myLocale.getCountry()));
		case(LONG_DATE_FORMAT): return ("dddd d MMMM yyyy");
		case(SHORT_DATE_FORMAT): return ("dd/MM/yy");
		case(MONTH_YEAR_FORMAT): return("MM/yy");
		case(DATE_SEPARATOR): return "/";
		case(TIME_SEPARATOR): return ":";
		case(TIME_FORMAT): return "HH:mm:ss";
	}
	return "???";
}
//===================================================================
public int fromString(int what,String str,int options)
//===================================================================
{
	char [] chars = str.toCharArray();
	switch(what){
	/*
		case(MONTH):
		case(SHORT_MONTH):
		for (int i = 0; i<months.length; i++){
			if (SubString.equals(months[i].toCharArray(),0,months[i].length(),chars,0,chars.length,
			SubString.IGNORE_CASE|SubString.STARTS_WITH))
				return i+1;
		}
		return -1;
		case(DAY):
		case(SHORT_DAY):
		for (int i = 0; i<days.length; i++){
			if (SubString.equals(days[i].toCharArray(),0,days[i].length(),chars,0,chars.length,
			SubString.IGNORE_CASE|SubString.STARTS_WITH))
				return i+1;
		}
		return -1;
	*/
		case(CURRENCY): if ("$".equalsIgnoreCase(str)) return 1; else return 0;
		case(AM_PM): if ("AM".equalsIgnoreCase(str)) return 0;
		             else if ("PM".equalsIgnoreCase(str)) return 1;
									else return -1;
		default: return -1;
	}
}

//===================================================================
public static int [] getAllIDs(int options)
//===================================================================
{
	return allLocales;
}
//-------------------------------------------------------------------
NumberFormat parseNumberLayout(Object layout,boolean currencyRequest)
//-------------------------------------------------------------------
{
/*
	nf->minDecimalDigits = nf->maxDecimalDigits =
	nf->maxIntegerDigits = nf->minIntegerDigits = 0;
*/
	if (layout == null || !(layout instanceof String))
		layout = (currencyRequest) ? "$,.":"#.#";
	String ly = (String)layout;
	NumberFormat nf = null;
	if (ly.length() != 0)
		if (currencyRequest && (ly.charAt(0) == '$'))
			nf = NumberFormat.getCurrencyInstance(myLocale);
	if (nf == null) nf = NumberFormat.getNumberInstance(myLocale);

	boolean afterDec = false;
	int mxd = 0, mnd = 0, mxi = 0, mni = 0;
	nf.setGroupingUsed(false);

	for (int i = 0; i<ly.length(); i++){
		char ch = ly.charAt(i);
		switch(ch){
		case ',': nf.setGroupingUsed(true); break;
		case '.': afterDec = true; mxd = mnd = -1; break;
		case '#': if (afterDec) mnd = 0; break;
		case '0': if (afterDec) {
								mxd++; mnd++;
								if (mxd == 0){
									mxd++;
									mnd++;
								}
						  }else{
								mni++;
							}
				  break;
		default: break;
		}
	}
	if (mnd != -1) nf.setMinimumFractionDigits(mnd);
	if (mxd != -1) nf.setMaximumFractionDigits(mxd);
	if (mni != 0) nf.setMinimumIntegerDigits(mni);
	if (mxi != 0) nf.setMaximumIntegerDigits(mxi);
	return nf;
}
//===================================================================
public boolean parse(String source,int what,Object value,Object layout)
//===================================================================
{
	int options = what;
	what &= 0xf0;
	switch(what){
		case FORMAT_PARSE_NUMBER: case FORMAT_PARSE_CURRENCY:
		NumberFormat nm = parseNumberLayout(layout,(what == FORMAT_PARSE_CURRENCY));
		try{
			java.lang.Number n = nm.parse(source);
			if (value instanceof ewe.sys.Long) ((ewe.sys.Long)value).value = n.longValue();
			if (value instanceof ewe.sys.Double) ((ewe.sys.Double)value).value = n.doubleValue();
			return true;
		}catch(Exception e){
			return false;
		}
		case FORMAT_PARSE_DATE:
		if (value instanceof ewe.sys.Time)
			if (layout == null || layout instanceof String)
				return Time.fromString(source,(ewe.sys.Time)value,(String)layout,this);
	}
	return false;
}
/**
* This is for formatting currency values etc. "value" should either be
* a Long for holding integer values or a Double for holding floating
* point values.
**/
//===================================================================
public String format(int what,Object value,Object layout)
//===================================================================
{
	int options = what;
	what &= 0xf0;
	switch(what){
		case FORMAT_PARSE_NUMBER:
		case FORMAT_PARSE_CURRENCY:
			NumberFormat nm = parseNumberLayout(layout,(what == FORMAT_PARSE_CURRENCY));
			double v = 0;
			if (value instanceof ewe.sys.Long) v = (double)((ewe.sys.Long)value).value;
			if (value instanceof ewe.sys.Double) v = (double)((ewe.sys.Double)value).value;
			return nm.format(v);
		case FORMAT_PARSE_DATE:
			if (value instanceof ewe.sys.Time)
				if (layout == null || layout instanceof String)
					return Time.toString((ewe.sys.Time)value,(String)layout,this);
	}
	return "???";
}
/**
* An option for getStringComparer.
**/
public final static int IGNORE_CASE = 0x1;
/**
* An option for getStringComparer.
**/
public final static int IGNORE_NONSPACE = 0x2;
/**
* An option for getStringComparer.
**/
public final static int IGNORE_SYMBOLS = 0x4;
/**
* A compare option.
**/
public final static int HAS_WILD_CARDS = 0x8;
/**
* Get a locale specific string comparer. Options can be:
* IGNORE_CASE, IGNORE_NONSPACE, IGNORE_SYMBOLS
**/
//===================================================================
public ewe.util.Comparer getStringComparer(int options)
//===================================================================
{
	return new localeStringComparer(this,options);
}
private static final char [] cc1 = new char[1], cc2 = new char[1];
/**
* Compare two characters. options can be IGNORE_CASE.
**/
//===================================================================
public int compare(char one,char two,int options)
//===================================================================
{
	cc1[0] = one; cc2[0] = two;
	return compare(cc1,0,1,cc2,0,1,options);
}
CollationKey[] primaryKeys = new CollationKey[512];
CollationKey[] secondaryKeys = new CollationKey[512];
Collator primary, secondary;

//===================================================================
CollationKey getKeyFor(char ch,int options)
//===================================================================
{
	//
	if (primary == null) {
		primary = Collator.getInstance(myLocale);
		primary.setStrength(Collator.PRIMARY);
		secondary = Collator.getInstance(myLocale);
		secondary.setStrength(Collator.TERTIARY);
	}
	//
	Collator collator = ((options & IGNORE_CASE) != 0) ? primary : secondary;
	CollationKey[] ks = ((options & IGNORE_CASE) != 0) ? primaryKeys : secondaryKeys;
	if (ks.length <= ch){
		CollationKey[] nk = new CollationKey[ch+256];
		Vm.copyArray(ks,0,nk,0,ks.length);
		ks = nk;
		if ((options & IGNORE_CASE) != 0) primaryKeys = ks;
		else secondaryKeys = ks;
	}
	if (ks[ch] == null) ks[ch] = collator.getCollationKey(new String(new char[]{ch}));
	return ks[ch];
}
/**
* Compare two character arrays. options can be IGNORE_CASE.
**/
//===================================================================
public int compare(char [] one,int oneOffset,int oneLength,char [] two,int twoOffset,int twoLength,int options)
//===================================================================
{
	if ((options & HAS_WILD_CARDS) == 0)
		return compare(new String(one,oneOffset,oneLength),new String(two,twoOffset,twoLength),options);
	for (int i = 0; i<oneLength && i<twoLength; i++){
		if (one[i] == '*' || two[i] == '*') return 0;
		if (one[i] == '?' || two[i] == '?') continue;
		int ret = getKeyFor(one[i],options).compareTo(getKeyFor(two[i],options));
		if (ret != 0) return ret;
	}
	return oneLength-twoLength;
}
//===================================================================
public int compare(String one,String two,int options)
//===================================================================
{
	if ((options & HAS_WILD_CARDS) != 0)
		return compare(Vm.getStringChars(one),0,one.length(), Vm.getStringChars(two),0,two.length(), options);
	ewe.util.Comparer cc = new localeStringComparer(this,options);
	return cc.compare(one,two);
}
/**
* This converts the character to either upper case or lower case.
**/
//===================================================================
public char changeCase(char c,boolean toUpper)
//===================================================================
{
	if (toUpper) return Character.toUpperCase(c);
	else return Character.toLowerCase(c);
}
//===================================================================
public void changeCase(char [] ch,int start,int length,boolean toUpper)
//===================================================================
{
	for (int i = 0; i<length; i++)
		ch[i+start] = changeCase(ch[i+start],toUpper);
}
/**
* This returns an object which implements LocalResource. If local resources for the
* specified moduleName is not found it will return null if returnNullIfNotFound is true, otherwise
* it will return a LocalResource object that always returns the default object value for each call to
* getLocalResource().
**/
//===================================================================
public LocalResource getLocalResource(String moduleName,boolean returnNullIfNotFound)
//===================================================================
{
	ewe.io.TreeConfigFile tcf = ewe.io.TreeConfigFile.getConfigFile("_config/"+moduleName+".cfg");
	if (tcf != null){
		LocalResource lr = tcf.getLocalResourceObject(this,moduleName);
		if (lr != null) return lr;
	}
	Reflect r = Reflect.getForName("ewex/registry/RegistryLocalResource");
	if (r != null){
		Method md = r.getMethod("getLocalResourceObject","(Lewe/sys/Locale;Ljava/lang/String;)Lewe/sys/LocalResource;",0);
		if (md != null){
			Wrapper [] wr2 = new Wrapper[2];
			wr2[0] = new Wrapper().setObject(this);
			wr2[1] = new Wrapper().setObject(moduleName);
			Wrapper ret = md.invoke(null,wr2,new Wrapper());
			if (ret != null){
				if (ret.getObject() instanceof LocalResource)
					return (LocalResource)ret.getObject();
			}
		}
	}
	//LocalResource lr = ewex.registry.RegistryLocalResource.(this,moduleName);
	//if (lr != null || returnNullIfNotFound) return lr;
	if (returnNullIfNotFound) return null;
	return new LocalResource(){
		public Object get(int id,Object data){return data;}
		public Object get(String id,Object data){return data;}
	};
}
//===================================================================
public String toString()
//===================================================================
{
	String lc = getString(LANGUAGE_ENGLISH,0,0);
	if (unknown(lc)) lc = "";
	lc += "("+getString(LANGUAGE_SHORT,0,0)+")";
	String c = getString(COUNTRY_ENGLISH,0,0);
	boolean didDash = false;
	if (!unknown(c))
		if (c.length() != 0){
			lc += "-"+c;
			didDash = true;
		}
	c = getString(COUNTRY_SHORT,0,0);
	if (!unknown(c))
		if (c.length() != 0){
			if (!didDash) lc += "-";
			lc += "("+c+")";
		}
	return lc;
}
/**
Get the calendar layout for the month/year that the specified Time is currently set for.
@param time the Time specifying the month and year required.
@param dayOfWeekLayout an array of 7 ints which will contain values from 1 = Monday to 7 = Sunday
in the appropriate location for the day of the week for the locale. The value at index 0
will be the first day of the week in this locale (which may be Monday, Sunday or possibly Saturday).
@param dayOfMonthLayout an array of 35 or 42 integers where the values 1 to 31 will be stored with
1 being stored within the range 0 to 6 depending on what day it is in. The values 2, 3 etc
will follow sequentially from 1. It is possible that the values 31 and 30 may wrap around
and be placed at indexes 0 and 1 if wrapAround is true. Locations where there are no days will be set to 0.
@param wrapAround if this is true then if the index of values 31 and/or 30 exceed 35 then they
will be wrapped to 0.
@return the number of days in the month or 0 if there is a problem with the supplied Time.
*/
//===================================================================
public int getCalendarForMonth(Time time, int[] dayOfWeekLayout, int[] dayOfMonthLayout, boolean wrapAround)
//===================================================================
{
	int num = Time.numberOfDays(time.month,time.year);
	int fdw = Convert.toInt(getString(FIRST_DAY_OF_WEEK,0,0));
	//
	for (int i = 0; i<7; i++){
		dayOfWeekLayout[i] = fdw;
		fdw++;
		if (fdw > 7) fdw = 1;
	}
	//
	int was = time.day;
	time.day = 1;
	time.update();
	int start = Time.indexOfDayInWeek(time.dayOfWeek,this)-1;
	time.day = was;
	time.update();
	//
	Utils.zeroArrayRegion(dayOfMonthLayout,0,dayOfMonthLayout.length);
	for (int i = 0; i<num; i++){
		dayOfMonthLayout[start++] = i+1;
		if (wrapAround && start == 35) start = 0;
	}
	//
	return num;
}
//##################################################################
}
//##################################################################
//##################################################################
class localeStringComparer implements ewe.util.Comparer{
//##################################################################
Locale myLocale;
int options;
Collator collator;
//===================================================================
public localeStringComparer(Locale locale,int options)
//===================================================================
{
	myLocale = locale;
	this.options = options;
	collator  = Collator.getInstance(myLocale.myLocale);
	//options |= Locale.IGNORE_CASE;
	if ((options & myLocale.IGNORE_CASE) != 0){
		collator.setStrength(collator.PRIMARY);
	}else{
		collator.setStrength(collator.TERTIARY);
	}
}

//-------------------------------------------------------------------
public int compare(Object one,Object two)
//-------------------------------------------------------------------
{
	if (one == two) return 0;
	else if (one == null) return -1;
	else if (two == null) return 1;
	else {
		String s1 = (String)one;
		String s2 = (String)two;
		int ret = collator.compare(s1,s2);
		return ret;
	}
	//((String)one).compareTo((String)two);
}
//##################################################################
}
//##################################################################
