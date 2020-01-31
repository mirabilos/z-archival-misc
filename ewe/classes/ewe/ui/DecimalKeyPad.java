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

//##################################################################
public class DecimalKeyPad extends InputKeyPad{
//##################################################################
public static DecimalKeyPad keypad = new DecimalKeyPad();

//==================================================================
public static void attach(Control who) {keypad.attachTo(who);}
//==================================================================

mButton one, ten, hundred, thousand;

//==================================================================
public DecimalKeyPad()
//==================================================================
{
	keys.addNext(one = new mButton("+1"));
	keys.addNext(ten = new mButton("+10"));
	keys.addNext(hundred = new mButton("+100"));
	keys.addNext(thousand = new mButton("+1000"));
	curText.alignment = Gui.Right;
	curText.anchor = Gui.NORTHEAST;
	modify(DrawFlat,0);
}
//==================================================================
protected void doClear() {curText.setText("0");}
//==================================================================
protected void pressed(Control who)
//==================================================================
{
	int val = ewe.sys.Convert.toInt(curText.getText());
	if (who == one) val += 1;
	if (who == ten) val += 10;
	if (who == hundred) val += 100;
	if (who == thousand) val += 1000;
	curText.setText(""+val);
	super.pressed(who);
}

//##################################################################
}
//##################################################################

