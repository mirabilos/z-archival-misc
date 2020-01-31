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
public class HyperlinkLabel extends mLabel{
//##################################################################
{
	setCursor(ewe.sys.Vm.HAND_CURSOR);
	modify(MouseSensitive,0);
}
public HyperlinkLabel(String text) {super(text);}
public HyperlinkLabel(String text,boolean hasHotKey) {super(text,hasHotKey);}
public HyperlinkLabel(int rows,int cols) {super(rows,cols);}

public static Color defaultActiveColor = new Color(0,0,255);
/**
* This is the color for the label when the cursor moves over it. It is initially set
* to defaultActiveColor. You can set it to any color you want.
**/
public Color activeColor = defaultActiveColor;
/**
* This indicates that an action should occur when the pen/mouse is first pressed on the control. If it
* is false, the an action only occurs when the pen/mouse is released after having been pressed on the
* control. By default it is true.
**/
public boolean actionOnPress = false;

public void doPaint(Graphics g,Rect area)
{
	Color prev = foreGround;
	if ((penStatus & PenIsOn) != 0) {
		foreGround = activeColor;
	}
	super.doPaint(g,area);
	foreGround = prev;
}

public void penClicked(Point where)
{
	if (isOnMe(where) && !actionOnPress)
		notifyAction();
}
public void penPressed(Point where)
{
	if (actionOnPress)
		notifyAction();
}
//##################################################################
}
//##################################################################

