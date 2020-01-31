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
* TimeOfDay is a type of Time object which is only concerned with
* the time of an unspecified day. Essentially the difference between
* this and a Time object is that in comparing two TimeOfDay objects, only
* the hours, minutes, seconds, milliseconds will be compared. The year, month
* and day will be ignored.
**/
//##################################################################
public class TimeOfDay extends Time{
//##################################################################
public static String todDefaultFormat = "h:mm tt";//new Locale().getString(Locale.TIME_FORMAT,0,0);
//===================================================================
public TimeOfDay() {super(); format = todDefaultFormat;}
//===================================================================
public TimeOfDay(int hour,int minute,int second)
//===================================================================
{
	super();
	this.hour = hour;
	this.minute = minute;
	this.second = second;
	millis = 0;
	update();
	format = todDefaultFormat;
}
//===================================================================
public String getDefaultFormat() {return todDefaultFormat;}
//===================================================================

//===================================================================
public int compareTo(Object other)
//===================================================================
{
	if (other == null) return 1;
	if (other instanceof Time){
		Time t = (Time)other;
		int d = hour-t.hour;
		if (d != 0) return d;
		d = minute-t.minute;
		if (d != 0) return d;
		d = second-t.second;
		if (d != 0) return d;
		d = millis-t.millis;
		return d;
	}
	return super.compareTo(other);
}

//##################################################################
}
//##################################################################

