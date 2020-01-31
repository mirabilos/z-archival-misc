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
public class FontInput extends TextDisplayButton{
//##################################################################
{
	setMenu(getClipboardMenu(null));
}

public static final int OPTION_NO_NAME = FontUpDownChooser.OPTION_NO_NAME;
public static final int OPTION_NO_SIZE = FontUpDownChooser.OPTION_NO_SIZE;
public static final int OPTION_NO_STYLE = FontUpDownChooser.OPTION_NO_STYLE;
public static final int OPTION_NO_SAMPLE =FontUpDownChooser.OPTION_NO_SAMPLE;

private int options = 0;
private ControlPopupForm popup = null;
private Font f =mApp.mainApp.getFont();

//===================================================================
public void fromFont(Font font)
//===================================================================
{
	f = font;
	setText(fontToString());
}
//===================================================================
public Font toFont()
//===================================================================
{
	return f;
}
//===================================================================
public String fontToString()
//===================================================================
{
	String ret = "";
	if ((options & OPTION_NO_NAME) == 0) ret += f.getName();
	if ((options & OPTION_NO_SIZE) == 0) {
		if (ret.length() != 0) ret += ",";
		ret += f.getSize();
	}
	if ((options & OPTION_NO_STYLE) == 0) {
		if (ret.length() != 0) ret += ",";
		ret += FontUpDownChooser.getStyleName(f.getStyle());
	}
	return ret;
}
//===================================================================
public FontInput()
//===================================================================
{
	this(0);
}
//===================================================================
public FontInput(int options)
//===================================================================
{
	setOptions(options);
	setText(fontToString());
}
//===================================================================
public void setOptions(int options)
//===================================================================
{
	this.options = options;
	ControlPopupForm p2 = FontUpDownChooser.getPopup(options);
	if (p2 != popup){
		if (popup != null) popup.detachFrom(this);
		popup = p2;
		popup.attachTo(this);
	}
}

//===================================================================
public void setData(Object data)
//===================================================================
{
	//ewe.sys.Vm.debug("setData(): "+data);
	if (data instanceof Font) fromFont((Font)data);
}
//===================================================================
public Object getData()
//===================================================================
{
	//ewe.sys.Vm.debug("getData(): "+toFont());
	return toFont();
}
/*
//=================================================================
public static void main(String[] args)
//=================================================================
{
	ewe.sys.Vm.startEwe(args);
	Form f = new Form();
	InputStack is = new InputStack();
	FontInput dti = new FontInput(0*OPTION_NO_STYLE);
	//dti.setTimeFormat(true,true);
	is.add(dti,"Appointment:");
	//dti.setTime(new TimeOfDay(11,32,0));
	f.addLast(is).setCell(HSTRETCH);
	f.setPreferredSize(240,320);
	f.execute();
	ewe.sys.Vm.exit(0);
}
*/
//##################################################################
}
//##################################################################

