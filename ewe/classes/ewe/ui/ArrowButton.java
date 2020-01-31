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
/**
* This is a button which displays an arrow in its center.
**/
//##################################################################
public class ArrowButton extends mButton {
//##################################################################
/**
* This specifies the arrow direction. It can be Up, Down, Left or Right.
**/
public int style = Up;
//==================================================================
public ArrowButton(int st){style = st;}
//==================================================================
//==================================================================
protected void calculateSizes()
//==================================================================
{
	super.calculateSizes();
	preferredWidth = preferredHeight = 15;
	if (hasModifier(SmallControl,true)) preferredWidth = preferredHeight = 7;
}

//==================================================================
public void doPaint(Graphics g,Rect area)
//==================================================================
{
	if (globalPalmStyle) borderStyle = BDR_NOBORDER;
	int flags = getModifiers(true);
	if (!((flags & Invisible) == 0)) return;
	int spacing = 4;
	Rect d = getDim(null);
	boolean af = (flags & DrawFlat) != 0, as = (flags & SmallControl) != 0;
	if (af) spacing = 2;
	if (as) {
		spacing = 0;
		//mColor.setColor(g,getBackground());
		//g.fillRect(0,0,d.width,d.height);
	}
	ButtonObject.obj.update(this);
	ButtonObject.obj.paint(g);
	boolean soft = ButtonObject.obj.soft;
	//drawButton(g);
	d.width -= spacing*2; d.height -= spacing*2;
	d.x += spacing; d.y += spacing;
	if (pressState && !as && !af && !soft) { d.x++;d.y++;}
	Color c = getImageColor();
	if (!(((flags & (NotEditable|DisplayOnly)) == 0) || ((flags & NotAnEditor) != 0)) || !(((flags & Disabled) == 0)  || ((flags & AlwaysEnabled) != 0))) c = Color.DarkGray;
	g.setColor(c);
	if (style == Left || style == Right) {
		g.drawHorizontalTriangle(d,style == Left);
		return;
	}else{
		g.drawVerticalTriangle(d,style == Up);
		return;
	}
	/*
	Color c = getImageColor();
	if (!(((flags & (NotEditable|DisplayOnly)) == 0) || ((flags & NotAnEditor) != 0)) || !(((flags & Disabled) == 0)  || ((flags & AlwaysEnabled) != 0))) c = Color.DarkGray;
	g.setColor(c);
	if (pressState && !as && !af) {
		d.x += 2;
		d.y += 2;
	}
	g.drawDiamond(d,style);
	*/
}

//##################################################################
}
//##################################################################

