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
public class DateDisplayInput extends mButton{
//##################################################################

{
	insideColor = Color.White;
	borderStyle = mInput.inputEdge|BF_EXACT;//EDGE_SUNKEN|BF_EXACT;
	flatInside = true;
	anchor = NORTHWEST;
	setMenu(getClipboardMenu(null));
	holdDownPause = 250;
}
/**
 * The Locale associated with this input.
 */
public Locale locale = new Locale();
/**
 * The Popup Form attached to this input. You do not need to use this.
 */
public ControlPopupForm attachedTo;
/**
* Set this false if you only want simple single line input for the date/time.
**/
public boolean useFullPopup = true;

//===================================================================
public DateDisplayInput()
//===================================================================
{
	DateChooserPopup.popup.attachTo(this);
	attachedTo = DateChooserPopup.popup;
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
	if (!(value instanceof Time)) return;
	time = (Time)((Time)value).getCopy();
	repaintNow();
}
/**
 * Get the date. Value must be a Time object value.
 */
//===================================================================
public void getValue(Value value)
//===================================================================
{
	if (!(value instanceof Time)) return;
	((Time)value).copyFrom(time);
}
//===================================================================
public void doPaint(Graphics g,Rect area)
//===================================================================
{
	text = time.toString(locale);
	super.doPaint(g,area);
}

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
			time.copyFrom((Time)data);
			return true;
		}
	}else if ((data = toTextData(data)) instanceof String){
		Time t2 = new Time();
		t2.format = time.format;
		t2.fromString(data.toString());
		if (t2.isValid()) {
			time.copyFrom(t2);
			return true;
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
//##################################################################
}
//##################################################################

