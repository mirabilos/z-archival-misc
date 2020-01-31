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
/**
* DayOfYear is a type of Time object which is only concerned with
* the day of a specified year. Essentially the difference between
* this and a Time object is that in comparing two DayOfYear objects, only
* the year, month and day will be compared. All other data will be ignored.
**/
//##################################################################
public class DayOfYear extends Time{
//##################################################################
public static String doyDefaultFormat = new Locale().getString(Locale.LONG_DATE_FORMAT,0,0);
//===================================================================
public DayOfYear() {super(); format = doyDefaultFormat;}
//===================================================================
public DayOfYear(int day,int month,int year)
//===================================================================
{
	super(day,month,year);
	format = doyDefaultFormat;
}
//===================================================================
public String getDefaultFormat() {return doyDefaultFormat;}
//===================================================================
//===================================================================
public int compareTo(Object other)
//===================================================================
{
	if (other == null) return 1;
	if (other instanceof Time){
		Time t = (Time)other;
		int d = year-t.year;
		if (d != 0) return d;
		d = month-t.month;
		if (d != 0) return d;
		d = day-t.day;
		return d;
	}
	return super.compareTo(other);
}
//##################################################################
}
//##################################################################

