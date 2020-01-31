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
package ewe.ui;
import ewe.fx.*;
import ewe.sys.*;
import ewe.util.*;
import ewe.reflect.*;

//##################################################################
public class DateTimeInput extends TextDisplayButton{
//##################################################################
{
	setMenu(getClipboardMenu(null));
}
/**
 * The Locale associated with this input.
 */
public Locale locale = new Locale();
/**
 * The Popup Form attached to this input. You do not need to use this.
 */
//public ControlPopupForm attachedTo;
/**
* Set this false if you only want simple single line input for the date/time.
**/
public boolean useFullPopup = true;

private boolean is24hours = false;
private boolean showSeconds = false;
private boolean isTime = false;
private boolean showCalendar = true;//false;
private ControlPopupForm popup = null;
private String format = null;

//-------------------------------------------------------------------
protected static String getTimeFormatFor(boolean showSeconds, boolean is24hours)
//-------------------------------------------------------------------
{
	String format = is24hours ? "HH:mm" : "h:mm";
	if (showSeconds) format += ":ss";
	if (!is24hours) format += " tt";
	return format;
}

//===================================================================
public String getFormat()
//===================================================================
{
	if (format != null) return format;
	if (isTime) return getTimeFormatFor(showSeconds, is24hours);
	else return "d-MMM-yyyy";
}

//===================================================================
public DateTimeInput()
//===================================================================
{
	this(false);
}
//===================================================================
public DateTimeInput(boolean isTime)
//===================================================================
{
	this.isTime = isTime;
	if (isTime) setTimeFormat(false,false);
	else setDateFormat("d-MMM-yyyy");
}
//===================================================================
public void setShowCalendar(boolean showCalendar)
//===================================================================
{
	this.showCalendar = showCalendar;
	ControlPopupForm p2 = DateUpDownInput.getPopup(showCalendar);
	if (p2 != popup){
		if (popup != null) popup.detachFrom(this);
		popup = p2;
		popup.attachTo(this);
	}
}
/**
 * Set the date format string. This marks the input as being a date input rather than a
 * time input.
 * @param dateFormat the new date format.
 */
//===================================================================
public void setDateFormat(String dateFormat)
//===================================================================
{
	isTime = false;
	format = dateFormat;
	setShowCalendar(showCalendar);
}
/**
 * Set the format for display/input of a Time of day value.
 * @param is24hours show 24 hour time.
 * @param showSeconds show the seconds value.
 */
//===================================================================
public void setTimeFormat(boolean showSeconds,boolean is24hours)
//===================================================================
{
	isTime = true;
	this.is24hours = is24hours;
	this.showSeconds = showSeconds;
	format = getTimeFormatFor(showSeconds,is24hours);
	ControlPopupForm p2 = TimeUpDownInput.getPopup(showSeconds,is24hours);
	if (p2 != popup){
		if (popup != null) popup.detachFrom(this);
		popup = p2;
		popup.attachTo(this);
	}
}
/**
* This variable holds the time value being displayed/edited. You can change
* the format of it and the format of the display will also change.
**/
//===================================================================
public Time time = new Time();
//===================================================================

//-------------------------------------------------------------------
protected void calculateSizes()
//-------------------------------------------------------------------
{
	text = time.toString(locale)+"      ";
	super.calculateSizes();
	text = time.toString(locale);
}
/**
 * Set the date. Value must be a Time object value.
 */
//===================================================================
public void setValue(Value value)
//===================================================================
{
	if (value instanceof Time) setTime((Time)value);
}
/**
 * Get the date. Value must be a Time object value.
 */
//===================================================================
public void getValue(Value value)
//===================================================================
{
	if (value instanceof Time) getTime((Time)value);
}
/**
 * Set the time.
 */
//===================================================================
public void setTime(Time time)
//===================================================================
{
	if (time == null) return;
	//ewe.sys.Vm.debug("Time value: "+time.getClass());
	this.time = (Time)time.getCopy();
	if (time instanceof TimeOfDay && !isTime)
		setTimeFormat(showSeconds,is24hours);
	else if (time instanceof DayOfYear && isTime)
		setDateFormat("d-MMM-yyyy");
	this.time.format = getFormat();
	//this.time.setTime(time.getTime());
	text = this.time.toString(locale);
	repaintNow();
}
/**
 * Get the time.
 * @param dest an optional destination Time object.
 * @return the destination Time object, or a new one if dest is null.
 */
//===================================================================
public Time getTime(Time dest)
//===================================================================
{
	if (dest == null) return (Time)time.getCopy();//dest = new Time();
	dest.setTime(this.time.getTime());
	return dest;
}
/*
//===================================================================
public void doPaint(Graphics g,Rect area)
//===================================================================
{
	text = time.toString(locale);
	super.doPaint(g,area);
}
*/
//===================================================================
public boolean acceptsData(Object data,DragContext how)
//===================================================================
{
	if (!canEdit()) return false;
	if (data instanceof Time) return true;
	Object s = toTextData(data);
	return s instanceof String;
}
//===================================================================
public boolean takeData(Object data,DragContext how)
//===================================================================
{
	if (data instanceof Time){
		if (((Time)data).isValid()) {
			setTime((Time)data);
			//time.copyFrom((Time)data);
			//return true;
		}
	}else if ((data = toTextData(data)) instanceof String){
		Time t2 = new Time();
		t2.format = getFormat();
		t2.fromString(data.toString());
		if (t2.isValid()) {
			setTime(t2);
			//time.copyFrom(t2);
			//return true;
		}
	}
	return false;
}
//-------------------------------------------------------------------
protected Object getDataToCopy()
//-------------------------------------------------------------------
{
	return time.getCopy();
}

/*
//===================================================================
public Object clipboardTransfer(Object clip,boolean toClipboard,boolean cut)
//===================================================================
{
	Object ret = clip;
	if (toClipboard || cut){
		Time t = (Time)time.getCopy();
		t.format = time.format;
		ret = t;
	}
	if (!toClipboard && clip != null){
		if (clip instanceof Time){
			if (((Time)clip).isValid()) time.copyFrom((Time)clip);
		}else if ((clip = toTextData(clip)) instanceof String){
			Time t2 = new Time();
			t2.format = time.format;
			t2.fromString(clip.toString());
			if (t2.isValid()) time.copyFrom(t2);
		}
		repaintNow();
	}
	return ret;
}
*/
/*
//=================================================================
public static void main(String[] args)
//=================================================================
{
	ewe.sys.Vm.startEwe(args);
	Form f = new Form();
	InputStack is = new InputStack();
	DateTimeInput dti = new DateTimeInput();
	//dti.setTimeFormat(true,true);
	is.add(dti,"Appointment:");
	dti.setTime(new TimeOfDay(11,32,0));
	f.addLast(is).setCell(HSTRETCH);
	f.setPreferredSize(240,320);
	f.execute();
	ewe.sys.Vm.exit(0);
}
*/
//##################################################################
}
//##################################################################

