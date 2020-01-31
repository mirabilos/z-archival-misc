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
import ewe.util.Iterator;
import ewe.util.ObjectIterator;

//##################################################################
public class ScrollableHolder extends Canvas {
//##################################################################

{
	origin = new Point(0,0);
	//modify(TakeControlEvents,0);
}
boolean first = true;
Control control;
/** @deprecated - use setClientConstraints() instead. **/
public boolean stretchComponent = true;
/** @deprecated - use setClientConstraints() instead. **/
public boolean shrinkComponent = false;

// This is used to indicate if clientConstraints are used.
private boolean clientConstraintsSet = false;
/*
This should be any of the HEXPAND, HCONTRACT, VEXPAND, VCONTRACT values ORed together.
By default it is HEXPAND|VEXPAND meaning the client will be expanded bigger than
its preferred size, but will not be contracted smaller than its preferred size.
*/
private int clientConstraints = HEXPAND|VEXPAND;

//===================================================================
public Iterator getSubControls()
//===================================================================
{
	return new ObjectIterator(control);
}
/**
This returns the clientConstraints, which will be any of the HEXPAND, HCONTRACT, VEXPAND, VCONTRACT values ORed together.
By default it is HEXPAND|VEXPAND meaning the client will be expanded bigger than
its preferred size, but will not be contracted smaller than its preferred size.
*/
//===================================================================
public int getClientConstraints()
//===================================================================
{
	if (clientConstraintsSet) return this.clientConstraints;
	int clientConstraints = 0;
	if (stretchComponent) clientConstraints |= HEXPAND|VEXPAND;
	if (shrinkComponent) clientConstraints |= HCONTRACT|VCONTRACT;
	return clientConstraints;
}
/**
Set the clientConstraints value specifying the behavior horizontally and vertically.
@param hExpand if this is true then the client control will be expanded horizontally.
@param hContract  if this is true then the client control will be contracted horizontally.
@param vExpand  if this is true then the client control will be expanded vertically.
@param vContract  if this is true then the client control will be contracted vertically.
*/
//===================================================================
public void setClientConstraints(boolean hExpand, boolean hContract, boolean vExpand, boolean vContract)
//===================================================================
{
	int value = 0;
	if (hExpand) value |= HEXPAND;
	if (hContract) value |= HCONTRACT;
	if (vExpand) value |= VEXPAND;
	if (vContract) value |= VCONTRACT;
	setClientConstraints(value);
}
/**
Set the clientConstraints value specifying the behavior horizontally and vertically.
@param constraints any of the HEXPAND, HCONTRACT, VEXPAND, VCONTRACT values ORed together.
*/
//===================================================================
public void setClientConstraints(int constraints)
//===================================================================
{
	clientConstraints = constraints;
	clientConstraintsSet = true;
}
//==================================================================
public ScrollableHolder(Control holding)
//==================================================================
{
	control = holding;
}
//===================================================================
public boolean needScrollBar(int which,int visible)
//===================================================================
{
	if (control == null) return false;
	if (clientConstraintsSet){
		if ((clientConstraints & (HCONTRACT|VCONTRACT)) == (HCONTRACT|VCONTRACT)) return false;
	}else{
		if (shrinkComponent) return false;
	}
	if (control == null || shrinkComponent) return false;
	Dimension d = control.getPreferredSize(null);
	if (which == Horizontal) return d.width > visible;
	else return d.height > visible;
}
//===================================================================
public boolean canGo(int orientation,int direction,int position)
//===================================================================
{
	return true;
}
//===================================================================
public void dismantle(Control downTo)
//===================================================================
{
	if (this == downTo) return;
	super.dismantle(downTo);
	control = null;
}

//==================================================================
public void make(boolean remake)
//==================================================================
{
	super.make(remake);
	if (control == null) return;
	changeContents(control);
}
//==================================================================
public void changeContents(Control what)
//==================================================================
{
	origin.x = origin.y = 0;
	if (control != null) remove(control);
	add(control = what);
	//BUG HERE!!!!
	control.make(false);
	if (!first) {
		redisplay();
		Rect r = getDim(null);
		resizeTo(r.width,r.height);
		checkScrolls();
		//updateScrollServer(Horizontal);
		//updateScrollServer(Vertical);
	}
	first = false;
}
//==================================================================
public void onSetOrigin()
//==================================================================
{
	if (control != null) {
		Rect r = control.getRect();
		control.setRect(-origin.x,-origin.y,r.width,r.height);
	}
	super.onSetOrigin();
}
//==================================================================
public void resizeTo(int w,int h)
//==================================================================
{
	super.resizeTo(w,h);
	if (control == null) return;
	Dimension r = control.getPreferredSize(null);
	if (!clientConstraintsSet){
		if ((r.width < width) && stretchComponent) r.width = width;
		if ((r.height < height) && stretchComponent) r.height = height;
		if ((r.width > width) && shrinkComponent) r.width = width;
		if ((r.height > height) && shrinkComponent) r.height = height;
	}else{
		if ((r.width > width) && (clientConstraints & HCONTRACT) != 0) r.width = width;
		if ((r.height > height) && (clientConstraints & VCONTRACT) != 0) r.height = height;
		if ((r.width < width) && (clientConstraints & HEXPAND) != 0) r.width = width;
		if ((r.height < height) && (clientConstraints & VEXPAND) != 0) r.height = height;
	}
	control.setRect(-origin.x,-origin.y,r.width,r.height);
	virtualSize = control.getDim(null);
}

//==================================================================
protected void calculateSizes()
//==================================================================
{
	if (control != null) {
		Dimension d = control.getPreferredSize(null);
		preferredWidth = d.width; preferredHeight = d.height;
	}
}
//==================================================================
public void doPaint(Graphics g,Rect where)
//==================================================================
{
	//new Exception().printStackTrace();
	super.doPaint(g,where);
}
//##################################################################
}
//##################################################################

