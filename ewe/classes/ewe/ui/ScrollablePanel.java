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
* This is a CellPanel which is used as the base for ScrollServer implementations.
*
*<p>
**/
//##################################################################
public abstract class ScrollablePanel extends CellPanel implements ScrollServer{
//##################################################################
/**
* The client which the panel contains.
**/
public ScrollClient client;
/**
* This is an option for setOptions()
* - it keeps both horizontal and vertical scrollers permanently displayed.
**/
public static int Permanent = 0x100;
/**
* This is an option for setOptions()
- it keeps horizontal scrollers always displayed.
**/
public static int AlwaysShowHorizontalScrollers = 0x200;
/**
* This is an option for setOptions()
- it keeps vertical scrollers always displayed.
**/
public static int AlwaysShowVerticalScrollers = 0x400;
/**
* This is an option for setOptions()
- it prevents horizontal scrollers from being displayed.
**/
public static int NeverShowHorizontalScrollers = 0x800;
/**
* This is an option for setOptions()
- it prevents vertical scrollers from being displayed.
**/
public static int NeverShowVerticalScrollers = 0x1000;

protected int options;
/**
* This is true so long as neither AlwaysShowHorizontalScrollers nor NeverShowHorizontalScrollers are selected as options.
**/
protected boolean hscrollerChanges = true;
/**
* This is true so long as neither AlwaysShowVerticalScrollers nor NeverShowVerticalScrollers are selected as options.
**/
protected boolean vscrollerChanges = true;

/**
* @deprecated - use setClientConstraints() instead.
* Set this true if you want the client to be stretched if the
* ScrollablePanel is stretched to be bigger than the preferred size
* of the component. By default this is true.
**/
public boolean stretchComponent = true;
/**
* @deprecated - use setClientConstraints() instead.
* Set this true if you want the client to be shrunk if the
* ScrollablePanel is shrunk to be smaller than the preferred size
* of the component. By default this is false. If this is set
* true then the scrollbars will never appear.
**/
public boolean shrinkComponent = false;

protected boolean alwaysShow = false;
protected boolean neverShow = false;
// This is used to indicate if clientConstraints are used.
private boolean clientConstraintsSet = false;
/*
This should be any of the HEXPAND, HCONTRACT, VEXPAND, VCONTRACT values ORed together.
By default it is HEXPAND|VEXPAND meaning the client will be expanded bigger than
its preferred size, but will not be contracted smaller than its preferred size.
*/
private int clientConstraints = HEXPAND|VEXPAND;
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
/**
* Only use this if the ScrollabePanel contains a ScrollableHolder. This
* will change the contents of the ScrollableHolder.
**/
//==================================================================
public void changeContents(Control what)
//==================================================================
{
	if (client instanceof ScrollableHolder)
		((ScrollableHolder)client).changeContents(what);
}
//==================================================================
public ScrollablePanel(ScrollClient client)
//==================================================================
{
	if (client instanceof Canvas) client = ((Canvas)client).getScrollClient();
	this.client = client;
	client.setServer(this);
}
/**
* Set options for the ScrollablePanel. Call this after creating the ScrollBar, but only
* call it once.
**/
//==================================================================
public void setOptions(int options)
//==================================================================
{
	this.options = options;
	if ((options & Permanent) != 0) options |= AlwaysShowHorizontalScrollers|AlwaysShowVerticalScrollers;

	hscrollerChanges = ((options & (AlwaysShowHorizontalScrollers|NeverShowHorizontalScrollers)) == 0);
	vscrollerChanges = ((options & (AlwaysShowVerticalScrollers|NeverShowVerticalScrollers)) == 0);

	if ((options & (AlwaysShowHorizontalScrollers|AlwaysShowVerticalScrollers)) == (AlwaysShowHorizontalScrollers|AlwaysShowVerticalScrollers))
		alwaysShow = true;
	else
		if ((options & (NeverShowHorizontalScrollers|NeverShowVerticalScrollers)) == (NeverShowHorizontalScrollers|NeverShowVerticalScrollers))
			neverShow = true;
	if (!clientConstraintsSet){
		setClientConstraints(getClientConstraints());
	}
	if ((options & NeverShowHorizontalScrollers) != 0) clientConstraints |= HCONTRACT;
	if ((options & NeverShowVerticalScrollers) != 0) clientConstraints |= VCONTRACT;
}

public static int vanish = Invisible|ShrinkToNothing;

protected static boolean blockUpdate = false;

//==================================================================
public void resizeTo(int w,int h)
//==================================================================
{
	//checkScrolls(w,h,false);
	super.resizeTo(w,h);
	checkScrolls(w-borderWidth*2,h-borderWidth*2,false);
}
//===================================================================
public abstract void checkScrolls(int width,int height,boolean redraw);
//===================================================================
public void checkScrolls() {checkScrolls(width-borderWidth*2,height-borderWidth*2,true);}
//===================================================================

//-------------------------------------------------------------------
private void updateClient()
//-------------------------------------------------------------------
{
	if (client instanceof ScrollableHolder) {
		ScrollableHolder sh = (ScrollableHolder)client;
		//sh.stretchComponent = stretchComponent;
		//sh.shrinkComponent = shrinkComponent;
		//if (clientConstraintsSet) sh.setClientConstraints(clientConstraints);
		sh.setClientConstraints(getClientConstraints());
	}
}
//==================================================================
public void make(boolean remake)
//==================================================================
{
	updateClient();
	super.make(remake);
}
//==================================================================
public void redisplay()
//==================================================================
{
	super.redisplay();
	updateClient();
	((Control)client).redisplay();
	checkScrolls(width-borderWidth*2,height-borderWidth*2,false);
	updateScroll(Horizontal);
	updateScroll(Vertical);
}
/**
* This will reset the origin of a client (if it is a ScrollableHolder)
* to (0,0) if it is not already so. If it is already so, it has no
* effect.
*/
//==================================================================
public void reset()
//==================================================================
{
	if (client instanceof ScrollableHolder){
		ScrollableHolder sh = (ScrollableHolder)client;
		if (sh.origin.x == 0 && sh.origin.y == 0) return;
		sh.setOrigin(0,0,null);
	}
}
//===================================================================
public abstract void modifyScrollers(int set,int clear);
//===================================================================

//===================================================================
public void dismantle(Control downTo)
//===================================================================
{
	if (this == downTo) return;
	super.dismantle(downTo);
	client.setServer(null);
	client = null;
}
/*
public void setRect(int x,int y,int w,int h)
{
	System.out.println("Set rect!");
	super.setRect(x,y,w,h);
}
*/
//##################################################################
}
//##################################################################

