package ewe.sys;

/**
* A time mask is a type of Time object that is used when comparing
* Time/Date values and which does not validate its date
* when being encoded and decoded. This allows it to have zero values for
* certain fields where these values would not normally be allowed (e.g. day or month).
* This allows a zero value to be considered a "Don't Care" value.
**/
//##################################################################
public class TimeMask extends Time{
//##################################################################

//===================================================================
public TimeMask(){}
//===================================================================

//===================================================================
public TimeMask setDateOnly(int day,int month,int year)
//===================================================================
{
	this.day = day;
	this.month = month;
	this.year = year;

	millis = second = minute = hour = 0;
	return this;
}
//===================================================================
public TimeMask setTimeOnly(int hours,int minutes,int seconds)
//===================================================================
{
	hour = hours;
	minute = minutes;
	second = seconds;
	millis = day = month = year = 0;
	return this;
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

	return this;
}



//##################################################################
}
//##################################################################

