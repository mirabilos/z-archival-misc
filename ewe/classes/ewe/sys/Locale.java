/*********************************************************************************
 *  Ewe Virtual Machine - Version 1.14, January 24, 2002                         *
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
import ewe.reflect.*;
import ewe.util.Utils;
//##################################################################
public class Locale{
//##################################################################
/**
* Do not use or move this variable.
**/
protected int myLocalID;
/**
* Use with getString()/fromString() -
* The full name of a month. forValue parameter must be in the range 1 to 12.
**/
public static final int MONTH = 1;
/**
* Use with getString()/fromString() -
* The short name of a month. forValue parameter must be in the range 1 to 12.
**/
public static final int SHORT_MONTH = 2;
/**
* Use with getString()/fromString() -
* The full name of a DAY IN THE WEEK. forValue parameter must be in the range 1 to 7.
* A value of 1 implies the first day of the week. This may be the equivalent of Monday
* or Sunday (or Saturday?).
**/
public static final int DAY_OF_WEEK = 3;
/**
* Use with getString()/fromString() -
* The short name of a DAY IN THE WEEK. forValue parameter must be in the range 1 to 7.
* A value of 1 implies the first day of the week. This may be the equivalent of Monday
* or Sunday (or Saturday?).
**/
public static final int SHORT_DAY_OF_WEEK = 4;
/**
* Use with getString()/fromString() -
* Gets the currencty symbol.
**/
public static final int CURRENCY = 5;
/**
* Use with getString()/fromString() -
* The full name of the AM/PM. forValue parameter must be in the range 0 to 1.
**/
public static final int AM_PM = 6;
/**
* Use with getString()/fromString() -
* The full name of the day which is equivalent to Monday to Sunday, where 1 == Monday, 2 == Tuesday.
* etc. forValue parameter must be in the range 1 (Monday) to 7 (Sunday).
**/
public static final int DAY = 7;
/**
* Use with getString()/fromString() -
* The short name of the day which is equivalent to Monday to Sunday, where 1 == Monday, 2 == Tuesday.
* etc. forValue parameter must be in the range 1 (Monday) to 7 (Sunday).
**/
public static final int SHORT_DAY = 8;
/**
* Use with getString() -
* The index of the first day of the week where 1 = Monday, 2 = Tuesday, ... 7 = Sunday.
* The value returned by getString() will be a text formatted decimal value, use ewe.sys.Convert.toInt() to
* convert it to an integer.
**/
public static final int FIRST_DAY_OF_WEEK = 9;
/**
* Use with getString() -
* The full name of the locale language.
**/
public static final int LANGUAGE = 10;
/**
* Use with getString() -
* The ISO English name of the locale language.
**/
public static final int LANGUAGE_ENGLISH = 11;
/**
* Use with getString() -
* The full native name of the locale language.
**/
public static final int LANGUAGE_NATIVE = 12;
/**
* Use with getString() -
* The ISO two letter (lowercase) name of the locale language.
**/
public static final int LANGUAGE_SHORT = 13;
/**
* Use with getString() -
* The character(s) for the time separator.
**/
public static final int TIME_SEPARATOR = 14;
/**
* Use with getString() -
* The character(s) for the date separator.
**/
public static final int DATE_SEPARATOR = 15;
/**
* Use with getString() -
* The format for the time display.
**/
public static final int TIME_FORMAT = 16;
/**
* Use with getString() -
* The format for the short date display.
**/
public static final int SHORT_DATE_FORMAT = 17;
/**
* Use with getString() -
* The format for the long date display.
**/
public static final int LONG_DATE_FORMAT = 18;
/**
* Use with getString() -
* The correct format for displaying month and year only.
**/
public static final int MONTH_YEAR_FORMAT = 19;
/**
* Use with getString() -
* The full name of the locale country.
**/
public static final int COUNTRY = 20;
/**
* Use with getString() -
* The full English name of the locale country.
**/
public static final int COUNTRY_ENGLISH = 21;
/**
* Use with getString() -
* The full native name of the locale country.
**/
public static final int COUNTRY_NATIVE = 22;
/**
* Use with getString() -
* The <b>three</b> letter (uppercase) ISO country code.
**/
public static final int COUNTRY_SHORT = 23;
/**
* Parameter for format() or parse() method, used when formating/parsing a numeric value.
**/
public static final int FORMAT_PARSE_NUMBER = 0x10;
/**
* Parameter for format() or parse() method, used when formating/parsing a currency value.
**/
public static final int FORMAT_PARSE_CURRENCY = 0x20;
/**
* Parameter for format() or parse() method, used when formating/parsing a date value.
* @deprecated use a Time value and set its format to be the value returned by getString() for
* TIME_FORMAT or DATE_FORMAT or LONG_DATE_FORMAT, and then request that Time value to
* convert itself to a String or to read from a String.
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
* This is the native Win32 definition of a Neutral locale ID.
**/
public static final int LOCALE_NEUTRAL = 0;
/**
* This is the native Win32 definition of the user default locale ID.
**/
public static final int LOCALE_USER_DEFAULT = 1024;
/**
* This is the native Win32 definition of the system default locale ID.
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
private String forcedLanguage = null, forcedCountry = null;

private static native String getDefaultLanguage();
/**
* This creates a Locale without an ID. Don't attempt to use it unless you
* call set(int id) on it.
**/
//===================================================================
public Locale(boolean dontSetId)
//===================================================================
{

}

/**
 * Create a Locale for the specified language and optional country.
 * Additional verbose
 * @param language The two letter lower case language specifier (e.g. "en").
 * @param country The three letter upper case country specifier (e.g. "GBR"). This may be null.
 * @param forceSupport If the specified language/country is not supported usually an IllegalArgumentException
 * will be thrown. However if this is true then a Locale will still be created, but it will only
 * be able to report its COUNTRY_SHORT and LANGUAGE_SHORT values (which will be same as the
 * language and country parameters).
 * @exception IllegalArgumentException If the specified language/country is not supported and
 * forceSupport is false.
 */
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

private static String setDefaultLanguage;
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
	if (setDefaultID == -1 && !forceSupport) return false;
	setDefaultLanguage = language;
	if (country != null) setDefaultLanguage += "-"+country;
	Vm.l = new Locale();
	return true;
}
/**
* Creates a new default Locale object.
**/
//===================================================================
public Locale()
//===================================================================
{
	this(LOCALE_USER_DEFAULT);
}
/**
* Creates a new Locale object with the specified ID. This should be used
* only with LOCALE_USER_DEFAULT and LOCALE_SYSTEM_DEFAULT.
*
* @param id the ID to use for this locale.
* @see #set
**/
//===================================================================
public Locale(int id) {set(id);}
//===================================================================

//-------------------------------------------------------------------
private void force(String lg, String ct)
//-------------------------------------------------------------------
{
	forcedLanguage = lg;
	forcedCountry = ct;
	myLocalID = -1;
}
//-------------------------------------------------------------------
private boolean set(String lang)
//-------------------------------------------------------------------
{
		if (lang != null)
			if (lang.length() != 0){
				String [] l = ewe.util.mString.split(lang,'-');
				if (l.length >= 1){
					String lg = l[0];
					String ct = null;
					if (l.length >= 2)
						ct = l[1];
					int nid = createID(lg,ct,0);
					if (nid != -1){
						myLocalID = nid;
						return true;
					}else{
						force(lg,ct);
						return true;
					}
				}
			}
		return false;
}

/**
* Set the ID for this locale. Effectively this changes the locale information
* you will get from this locale on systems that support this.
*
* @param id the ID to use for this locale.
**/
//===================================================================
public void set(int id)
//===================================================================
{
	forcedLanguage = forcedCountry = null;
	if (id == LOCALE_USER_DEFAULT){
		String lang = setDefaultLanguage;
		if (lang == null) lang = getDefaultLanguage();
		if (set(lang)) return;
	}
	myLocalID = id;
}
static final String countryConversion =
"AFGAFALBALDZADZASMASANDADAGOAOAIAAIATGAGARGARARMAMABWAWAUSAUAUTATAZEAZBHSBSBHRBHBGDBDBRBBBBLRBYBELBEBLZBZBENBJBMUBMBTNBTBOLBOBIHBABWABWBRABRVGBVGBRNBNBGRBGBFABFBDIBIKHMKHCMRCMCANCACPVCVCYMKYCAFCFTCDTDCHLCLCHNCNHKGHKCOLCOCOMKMCOGCGCOKCKCRICRCIVCIHRVHRCUBCUCYPCYCZECZPRKKPCODCDDNKDKDJIDJDMADMDOMDOTMPTPECUECEGYEGSLVSVGNQGQERIERESTEEETHETFROFKFLKFOFJIFJFINFIFRAFRGUFGFPYFPFGABGAGMBGMGEOGEDEUDEGHAGHGIBGIGRCGRGRLGLGRDGDGLPGPGUMGUGTMGTGINGNGNBGWGUYGYHTIHTVATVAHNDHKHUNHUISLISINDINIDNIDIRNIRIRQIQIRLIEISRILITAITJAMJMJPNJPJORJOKAZKZKENKEKIRKIKWTKWKGZKGLAOLALVALVLBNLBLSOLSLBRLRLBYLYLIELILTULTLUXLUMDGMGMWIMWMYSMYMDVMVMLIMLMLTMTMHLMHMTQMQMRTMRMUSMUMEXMXFSMFMMCOMCMNGMNMSRMSMARMAMOZMZMMRMMNAMNANRUNRNPLNPNLDNLANTANNCLNCNZLNZNICNINERNENGANGNIUNUNFKNFMNPMPNORNOOMNOMPAKPKPLWPWPSEPSPANPAPNGPGPRYPYPERPEPHLPHPCNPNPOLPLPRTPTPRIPRQATQAKORKRMDAMDREUREROMRORUSRURWARWSHNSHKNAKNLCALCSPMPMVCTVCWSMWSSMRSMSTPSTSAUSASENSNSYCSCSLESLSGPSGSVKSKSVNSISLBSBSOMSOZAFZAESPESLKALKSDNSDSURSRSJMSJSWZSZSWESECHECHSYRSYTWNTWTJKTJTHATHMKDMKTGOTGTKLTKTONTOTTOTTTUNTNTURTRTKMTMTCATCTUVTVUGAUGUKRUAAREAEGBRGBTZATZUSAUSVIRVIURYUYUZBUZVUTVUVENVEVNMVNWLFWFESHEHYEMYEYUGYUZMBZMZWEZW"
;//.toCharArray();
/**
* This will convert a three letter (uppercase) ISO country code to the two letter (uppercase) ISO country code
* OR the other way. It will return an empty string if no conversion was found.
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
* This is an options for createFor() it tells the system
* to create a Locale that always reports the specified language and country
* even if the Locale is not actually supported by the underlying system.
**/
public static final int FORCE_CREATION = 0x1;

/**
* This should create a locale ID based on
* the ISO two character language and country codes. It will return
* -1 if it cannot create the ID. If you are creating an ID so that you can
* create a new Locale, then it is better to use createFor().
*
* @param language the ISO-639 two character language specifier.
* @param country the ISO-3166 <b>three</b> character country specifier - this can
* be null and the first locale with the specified language will be used.
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
		if (all[i] == defaultID) continue;
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
* @param language the ISO-639 two character language specifier.
* @param country the ISO-3166 <b>three</b> character country specifier - this can
* be null and the first locale with the specified language will be used.
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
/**
* This is used to retrieve locale specific information as a String.
* Numeric values will be returned as text encoded.
*
* @param what should be one of MONTH, SHORT_MONTH, etc.
* @param forValue should be a value relative to the "what" parameter.
* 						 e.g. if (what == MONTH) value should be a number for 1 to 12.
* @param options options for getting the information. None are defined yet.
*
**/
//===================================================================
public String getString(int what,int forValue,int options)
//===================================================================
{
	if (what == LANGUAGE_SHORT && forcedLanguage != null) return forcedLanguage;
	if (what == COUNTRY_SHORT && forcedLanguage != null)
		return forcedCountry == null ? "" : forcedCountry;
	String ret = nativeGetString(what,forValue,options);
	// This cannot work! Creates a stack overflow as toLowerCase needs a Locale.
	//if (what == LANGUAGE_SHORT && ret != null) ret = ret.toLowerCase();
	return ret;
}
//-------------------------------------------------------------------
private native String nativeGetString(int what,int forValue,int options);
//-------------------------------------------------------------------

/**
* This is the reverse of getString() - it takes a locale dependent string
* and returns the value associated with it. You use this to parse
* locale dependent strings.
*
* @param what should be one of MONTH, SHORT_MONTH, DAY, SHORT_DAY or AM_PM
* @param str should be a string to parse.
* @param options options for getting the information. None are defined yet.
* @return
**/
//===================================================================
public native int fromString(int what,String str,int options);
//===================================================================
/**
* This is for formatting number/currency values etc. "value" should either be
* a Long for holding integer values or a Double for holding floating
* point values. "layout" will in most cases be a String holding a layout pattern.
* @param what one of FORMAT_PARSE_NUMBER or FORMAT_PARSE_CURRENCY constants.
* @param value this should be a ewe.sys.Long() for all integer values or a ewe.sys.Double() for
* all floating point values.
* @param layout This should be String that holds the following characters:<br>
* '$' indicates that a currency symbol should be used. <br>
* ',' indicates that thousands groupings should be used. <br>
* '.' separates formatting before the decimal point and after the decimal point.<br>
* '0' before the decimal point indicates the number of digits before the decimal point.<br>
* '0' after the decimal point indicates the number of digits after the decimal point.<br>
* '#' after the decimal point indicates no fixed number of digits after the decimal point.<br>
* <p>
* Examples include: ",.#", "$,0000.00"
* @return The formatted number/currency.
*/
//===================================================================
public native String format(int what,Object value,Object layout);
//===================================================================
/**
* This is for parse number/currency values etc. "value" should either be
* a Long for holding integer values or a Double for holding floating
* point values. "layout" will in most cases be a String holding a layout pattern.
* @param what one of FORMAT_PARSE_NUMBER or FORMAT_PARSE_CURRENCY constants.
* @param value this should be a ewe.sys.Long() for all integer values or a ewe.sys.Double() for
* all floating point values.
* @param layout This should be String that holds the following characters:<br>
* '$' indicates that a currency symbol should be used. <br>
* ',' indicates that thousands groupings should be used. <br>
* '.' separates formatting before the decimal point and after the decimal point.<br>
* '0' before the decimal point indicates the number of digits before the decimal point.<br>
* '0' after the decimal point indicates the number of digits after the decimal point.<br>
* '#' after the decimal point indicates no fixed number of digits after the decimal point.<br>
* <p>
* Examples include: ",.#", "$,0000.00"
* @return true if successful.
*/
//===================================================================
public native boolean parse(String source,int what,Object value,Object layout);
//===================================================================
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
* Get a locale specific string comparer.
*
* @param options IGNORE_CASE, IGNORE_NONSPACE, IGNORE_SYMBOLS possibly OR'ed together.
* @return A Comparer.
*/
//===================================================================
public ewe.util.Comparer getStringComparer(int options)
//===================================================================
{
	return new localeStringComparer(this,options);
}
/**
* Get an array of all supported Locale IDs
* @param options None defined yet.
* @return An array of all supported Locale IDs.
*/
//===================================================================
public static native int [] getAllIDs(int options);
//===================================================================
/**
* Compare two characters.
* @param one A character
* @param two A character
* @param options IGNORE_CASE.
* @return Greater than 0 if one is greater than two, less than 0 if two is less than one, 0 if they are equal.
*/
//===================================================================
public native int compare(char one,char two,int options);
//===================================================================


/**
 * Compare two sections of character arrays.
* @param one The first array.
* @param oneOffset The start index.
* @param oneLength The number of characters to compare.
* @param two The second array.
* @param twoOffset The start index.
* @param twoLength The number of characters to compare.
* @param options IGNORE_CASE, IGNORE_NONSPACE, IGNORE_SYMBOLS, HAS_WILD_CARDS.
* @return Greater than 0 if one is greater than two, less than 0 if two is less than one, 0 if they are equal.
*/
//===================================================================
public native int compare(char[] one,int oneOffset,int oneLength,char[] two,int twoOffset,int twoLength,int options);
//===================================================================
/**
 * Compare two strings.
 * @param one The first String.
 * @param two The second String
* @param options IGNORE_CASE, IGNORE_NONSPACE, IGNORE_SYMBOLS, HAS_WILD_CARDS.
* @return Greater than 0 if one is greater than two, less than 0 if two is less than one, 0 if they are equal.
 */
//===================================================================
public int compare(String one,String two,int options)
//===================================================================
{
	return compare(Vm.getStringChars((String)one),0,((String)one).length(),
	Vm.getStringChars((String)two),0,((String)two).length(),options);
}
/**
* This converts the character to either upper case or lower case.
**/
//===================================================================
public native char changeCase(char c,boolean toUpper);
//===================================================================
/**
* This converts the character array to either upper case or lower case.
**/
//===================================================================
public native void changeCase(char [] ch,int start,int length,boolean toUpper);
//===================================================================

private static Object tcf;
private static Type tcfType;
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
	//returnNullIfNotFound = false;
	if (tcfType == null) tcfType = new Type("ewe.io.TreeConfigFile");
	if (tcfType.exists()){
		if (tcf == null) tcf = tcfType.newInstance();
		if (tcf != null){
			Object mod = tcfType.invoke(tcf,"findOrMake(Ljava/lang/String;)Lewe/io/TreeConfigFile;",new Object[]{"_config/"+moduleName+".cfg"});
			if (mod != null){
				LocalResource lr = (LocalResource)tcfType.invoke(mod,"getLocalResourceObject(Lewe/sys/Locale;Ljava/lang/String;)Lewe/sys/LocalResource;",new Object[]{this,moduleName});
				if (lr != null) return lr;
			}
		}
	}
	/*
	ewe.io.TreeConfigFile tcf = ewe.io.TreeConfigFile.getConfigFile("_config/"+moduleName+".cfg");
	if (tcf != null){
		LocalResource lr = tcf.getLocalResourceObject(this,moduleName);
		if (lr != null) return lr;
	}
	*/
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
	//LocalResource lr = ewex.registry.RegistryLocalResource.getLocalResourceObject(this,moduleName);
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

//===================================================================
public localeStringComparer(Locale locale,int options)
//===================================================================
{
	myLocale = locale;
	this.options = options;
}
//===================================================================
public int compare(Object one,Object two)
//===================================================================
{
	return myLocale.compare((String)one,(String)two,options);
}
//##################################################################
}
//##################################################################


