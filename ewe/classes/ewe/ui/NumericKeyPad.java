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
import ewe.util.*;
//##################################################################
public class NumericKeyPad extends InputKeyPad{
//##################################################################

protected Control [] numbers = new Control[10];

protected Control point, back;
//===================================================================
public NumericKeyPad()
//===================================================================
{
	//......................................................
	// Place your keys into the "keys" mPanel (which is initially empty).
	//......................................................
	keys.equalWidths = true;
	for (int r = 0; r<3; r++){
		for (int c = 0; c<3; c++) {
			int which = (c+7-r*3);
			mButton b = new mButton(""+which);
			b.setHotKey(IKeys.INVISIBLE,(char)('0'+which));
			numbers[which] = b;
			keys.addNext(b);
		}
		keys.endRow();
	}
	numbers[0] = new mButton("0").setHotKey(IKeys.INVISIBLE,'0');
	keys.addNext(numbers[0]);
	top.addNext(back = new mButton(ewe.fx.ImageCache.cache.get("ewe/leftarrowsmall.bmp",ewe.fx.Color.White)),VSTRETCH,FILL|CENTER)
		.setHotKey(0,KeyEvent.getBackKey(true));
	curText.alignment = Right;
	curText.anchor = NORTHEAST;
}


public Vector extras = new Vector();
//===================================================================
public void addDecimalPoint() {keys.addNext(point = new mButton(".")).setHotKey(0,'.');}
//===================================================================
public void addKey(String what)
//===================================================================
{
	mButton b = new mButton(what);
	b.setHotKey(IKeys.INVISIBLE,what.charAt(0));
	keys.addNext(b);
	extras.add(b);
}
//-------------------------------------------------------------------
protected void append(String what)
//-------------------------------------------------------------------
{
	if (what.equals(".") && (mString.indexOf(value,'.') != -1)) return;
	super.append(what);
}
/**
* This handles the button press.
**/
//-------------------------------------------------------------------
protected void pressed(Control b)
//-------------------------------------------------------------------
{
	String value = this.value;
	//......................................................
	// Check for number keys.
	//......................................................
	for (int i = 0; i<10; i++)
		if (numbers[i] == b) {
			append(""+i);
			return;
		}
	//......................................................
	// Check for backspace.
	//......................................................
	if (b == back) {
		if (value.length() <= 1) doClear();
		else setValue(value.substring(0,value.length()-1));
	}
	//......................................................
	// Check for double zero.
	//......................................................
	if (b == point) append(".");
	//......................................................
	// Make sure you call super.pressed() to handle the Clear, Reset, OK and Cancel.
	//......................................................
	if (extras.contains(b)) append(((Control)b).getText());
	super.pressed(b);
}
//-------------------------------------------------------------------
protected void doClear()
//-------------------------------------------------------------------
{
	setValue("0");
}
//-------------------------------------------------------------------
protected void doReset()
//-------------------------------------------------------------------
{
	setValue(client == null ? "0" : client.getText());
}
//##################################################################
}
//##################################################################

