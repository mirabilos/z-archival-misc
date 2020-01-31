package ewe.sys;

/**
* A DateChange object is used when calculating differences between two
* Dates or when adding/subtracting time periods to an existing date.<p>
**/

//##################################################################
public class DateChange{
//##################################################################
boolean negative;
/**
 * This is only valid when used to calculate the difference between two dates.
 * @return Whether the time difference was negative.
 */
//===================================================================
public boolean isNegative()
//===================================================================
{
	return negative;
}
/**
* The number of years difference.
**/
public int years;
/**
* The number of months difference.
**/
public int months;
/**
* The number of days difference.
**/
public int days;
/**
If this is used, then the years, months and days values are ignored, when passing
this Object as a parameter for changing a date. If this object is used to pass the
return value of a difference calculation, then this value will also be valid as the
total number of days that span the number of years, months and days.
**/
public int totalDays;
/**
 * Set the years, months and days.
 * @return Itself
 */
//===================================================================
public DateChange set(int years,int months,int days)
//===================================================================
{
	this.years = years;
	this.months = months;
	this.days = days;
	this.totalDays = 0;
	negative = false;
	return this;
}
/**
 * Set the totalDays.
 * @return Itself
 */
//===================================================================
public DateChange set(int totalDays)
//===================================================================
{
	years = months = days = 0;
	this.totalDays = totalDays;
	negative = false;
	return this;
}
public String toString()
{
	return ((isNegative() ? "- " : "+ ")+"Years: "+years+", Months: "+months+", Days: "+days+", Total: "+totalDays);
}
//##################################################################
}
//##################################################################

