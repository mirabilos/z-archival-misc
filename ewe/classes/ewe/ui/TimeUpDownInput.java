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
import ewe.fx.Font;
import ewe.fx.Color;
import ewe.util.Range;
import ewe.util.Vector;
import ewe.sys.Time;
/**
This inputs a time of day value using a set of UpDownInputs.
**/
//##################################################################
public class TimeUpDownInput extends Holder{
//##################################################################

UpDownInput hour, minute, second, ampm;
private static Range hourRange = new Range(1,12);
private static Range hour24Range = new Range(0,23);
private static Range minuteRange = new Range(0,59);
private static Vector ampmRange = new Vector(new String[]{"am","pm"});
protected boolean is24Hours = false;

//-------------------------------------------------------------------
mLabel getLabel(String what)
//-------------------------------------------------------------------
{
	mLabel l = new mLabel(what);
	l.spacing = 1;
	setControl(DONTFILL|CENTER);
	return l;
}
//===================================================================
public TimeUpDownInput(boolean showSeconds, boolean hours24)
//===================================================================
{
	is24Hours = hours24;
	addNext(hour = new UpDownInput(2));
	addNext(getLabel(":"));
	addNext(minute = new UpDownInput(2));
	if (showSeconds){
		addNext(getLabel(":"));
 		addNext(second = new UpDownInput(2));
	}
	if (!hours24){
		addNext(getLabel(" "));
 		addNext(ampm = new UpDownInput(2));
	}
	hour.integerValues = hours24 ? hour24Range : hourRange;
	hour.integerDigits = 2;
	hour.zeroFillInteger = hours24;
	minute.integerValues = minuteRange;
	minute.integerDigits = 2;
	minute.zeroFillInteger = true;
	if (ampm != null) {
		ampm.textValues = ampmRange;
		ampm.hasSplit = false;
	}
	if (second != null){
 		second.integerValues = minuteRange;
		second.integerDigits = 2;
		second.zeroFillInteger = true;
	}
	setTime(new Time());
}


/**
Set the Time the input displays.
@param t The Time to display.
*/
//===================================================================
public void setTime(Time t)
//===================================================================
{
	if (is24Hours){
		hour.setInt(t.hour);
	}else{
		if (t.hour < 12) ampm.setText("am");
		else ampm.setText("pm");
		if (t.hour == 0) hour.setInt(12);
		else if (t.hour > 12) hour.setInt(t.hour-12);
		else hour.setInt(t.hour);
	}
	minute.setInt(t.minute);
	if (second != null) second.setInt(t.second);
}
/**
Get the time displayed/entered.
@param dest an optional destination Time which may be null.
@return the destination Time or a new Time if dest is  null.
*/
//===================================================================
public Time getTime(Time dest)
//===================================================================
{
	if (dest == null) dest = new Time();
	dest.hour = hour.getInt();
	if (!is24Hours){
		if (ampm.getText().equals("pm")) dest.hour += 12;
		if (dest.hour == 24) dest.hour = 12;
		else if (dest.hour == 12) dest.hour = 0;
	}
	dest.minute = minute.getInt();
	dest.second = second == null ? 0 : second.getInt();
	dest.update();
	return dest;
}

private static ControlPopupForm pop, pop24, popS, pop24S;

//===================================================================
public static ControlPopupForm getPopup(boolean showSeconds, boolean hours24)
//===================================================================
{
	if (showSeconds)
		if (hours24)
			return pop24S == null ? (pop24S = new TimeUpDownInputPopup(showSeconds,hours24)) : pop24S;
		else
			return popS == null ? (popS = new TimeUpDownInputPopup(showSeconds,hours24)) : popS;
	else
		if (hours24)
			return pop24 == null ? (pop24 = new TimeUpDownInputPopup(showSeconds,hours24)) : pop24;
		else
			return pop == null ? (pop = new TimeUpDownInputPopup(showSeconds,hours24)) : pop;
}

//##################################################################
public static class TimeUpDownInputPopup extends ControlPopupForm{
//##################################################################
TimeUpDownInput input;
//===================================================================
public TimeUpDownInputPopup(boolean showSeconds, boolean hours24)
//===================================================================
{
	//putByClient = false;
	setBorder(BDR_OUTLINE|BF_RECT,1);
	backGround = Color.White;
	addMainControls(input = new TimeUpDownInput(showSeconds,hours24));
	addCloseControls();
}
//===================================================================
public void onControlEvent(ControlEvent ev)
//===================================================================
{
	if (ev.target == input){
		if (ev.type == ev.PRESSED)
			exit(IDOK);
		else if (ev.type == ev.CANCELLED)
			exit(IDCANCEL);
	}
	else super.onControlEvent(ev);
}
//-------------------------------------------------------------------
protected void transferToClient(Control client)
//-------------------------------------------------------------------
{
	if (client instanceof DateTimeInput)
		((DateTimeInput)client).setTime(input.getTime(null));
}
/**
* This is called by setFor(Control who) and gives you an opportunity to
* modify the Form based on the client control.
* @param who The new client control.
*/
//------------------------------------------------------------------
protected void startingInput(Control who)
//-------------------------------------------------------------------
{
	if (who instanceof DateTimeInput)
		input.setTime(((DateTimeInput)client).getTime(null));
}
//##################################################################
}
//##################################################################
/*
//=================================================================
public static void main(String[] args)
//=================================================================
{
	ewe.sys.Vm.startEwe(args);
	Form f = new Form();
	f.addLast(new TimeUpDownInput(false,false));
	f.execute();
	ewe.sys.Vm.exit(0);
}
*/
//##################################################################
}
//##################################################################

