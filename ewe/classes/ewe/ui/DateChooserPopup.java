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
public class DateChooserPopup extends ControlPopupForm{
//##################################################################

Control active;
//===================================================================
DateChooser dateChooser;// = new DateChooser(null);
public static DateChooserPopup popup = new DateChooserPopup();
//===================================================================
public DateChooserPopup()
//===================================================================
{
}
//===================================================================
public void setFor(Control who)
//===================================================================
{
	active = who;
	DateDisplayInput ddi = (DateDisplayInput)active;
	if (dateChooser == null) {
		dateChooser = new DateChooser(ddi.locale);
		addLast(dateChooser);
	}
	dateChooser.reset(ddi.time);
	//font = mApp.findFont("small",true);
	super.setFor(who);
	//startingInput(who);
	//popup();
	//doInit();
}
//===================================================================
public void onEvent(Event ev)
//===================================================================
{
	if (ev instanceof FormEvent){
		if (ev.type == FormEvent.CLOSED){
			if (dateChooser.exitValue == IDOK){
				Time t = dateChooser.getTime();
				if (t.isValid()){
					((DateDisplayInput)active).setValue(t);
					((DateDisplayInput)active).notifyDataChange();
				}
			}
		}
	}
	super.onEvent(ev);
}

//===================================================================
public void make(boolean remake)
//===================================================================
{
	super.make(remake);
	modifyAll(SendUpKeyEvents,0,true);
}
//-------------------------------------------------------------------
protected Rect checkSize(Rect intended,boolean willFit)
//-------------------------------------------------------------------
{
	DateDisplayInput ddi = (DateDisplayInput)active;
	if (ddi.useFullPopup && willFit && !(ddi.time instanceof TimeOfDay)) {
		dateChooser.setSize(false);
		return intended;
	}else{
		Rect r = Gui.getRectInWindow(client,new Rect(),false);
		dateChooser.setSize(true);
		return r;
	}
}
//##################################################################
}
//##################################################################

