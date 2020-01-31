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
* This is a CellPanel which provides up and down arrow buttons
* or left and right buttons
* which will appear if necessary.
*
* Do NOT use this to scroll a panel of components. Instead, put the
* the panel of components in a ScrollableHolder and THEN add that
* ScrollableHolder to the ScrollablePanel.
*
* You CAN add a fully implemented ScrollClient to the ScrollablePanel
* without using a ScrollableHolder.
**/

//##################################################################
public class UpDownScroller extends ScrollablePanel{
//##################################################################
/**
* The up button. This is ALWAYS present even if it not actually visible.
**/
public mButton upButton;
/**
* The down button. This is ALWAYS present even if it not actually visible.
**/
public mButton downButton;

/**
* This creates the scroller with up/down buttons. If you want left/right
* buttons use UpDownScroller(ScrollClient client,boolean horizontal).
**/
public UpDownScroller(ScrollClient client)
{
	this(client,false);
}


protected mButton addArrow(int direction,boolean horizontal)
{
	mButton b = new ArrowButton(direction);
	addNext(b).setCell(horizontal ? VSTRETCH : HSTRETCH);
	if (!horizontal) endRow();
	b.shouldRepeat = true;
	b.modify(NoFocus|DrawFlat|Invisible|ShrinkToNothing,TakesKeyFocus);
	b.setBorder(BDR_NOBORDER,0);
	return b;
}

public int direction;

//==================================================================
public UpDownScroller(ScrollClient client,boolean horizontal)
//==================================================================
{
	super(client);
	client = this.client;
	upButton = addArrow(horizontal ? Left : Up,horizontal);
	Control c = (Control)client;
	addNext(c);
	if (!horizontal) endRow();
	downButton = addArrow(horizontal ? Right : Down,horizontal);
	direction = horizontal ? Horizontal : Vertical;
}
/**
* Set options for the ScrollablePanel. The only option currently
* supported is Permanent.
**/
//==================================================================
public void setOptions(int options)
//==================================================================
{
	super.setOptions(options);
	if ((options & Permanent) != 0) options |= AlwaysShowHorizontalScrollers|AlwaysShowVerticalScrollers;

	if (direction == Horizontal && ((options & AlwaysShowHorizontalScrollers) != 0)){
		upButton.modify(0,Invisible|ShrinkToNothing);
		downButton.modify(0,Invisible|ShrinkToNothing);
		alwaysShow = true;
	}
	if (direction == Vertical && ((options & AlwaysShowVerticalScrollers) != 0)){
		upButton.modify(0,Invisible|ShrinkToNothing);
		downButton.modify(0,Invisible|ShrinkToNothing);
		alwaysShow = true;
	}
	if (direction == Horizontal && ((options & NeverShowHorizontalScrollers) != 0)){
		neverShow = true;
	}
	if (direction == Vertical && ((options & NeverShowVerticalScrollers) != 0)){
		neverShow = true;
	}
}
//==================================================================
public void checkScrolls(int width,int height,boolean reDraw)
//==================================================================
{
	if (blockUpdate) return;
	if (width == 0 || height == 0) return;
	int dimension = direction == Vertical ? height : width;
	boolean need = client.needScrollBar(direction,dimension);

	if (!alwaysShow && !neverShow) {
		boolean wasVisible = !upButton.hasModifier(ShrinkToNothing,false);
		if (wasVisible)
			if (direction == Horizontal && !hscrollerChanges) need = true;
			else if (direction == Vertical && !vscrollerChanges) need = true;

		boolean changed = wasVisible != need;
		blockUpdate = true;
		if (changed) {
			if (wasVisible){
				upButton.modify(vanish,0);
				downButton.modify(vanish,0);
				if (client.getCurrent(direction) != 0) client.doScroll(direction,TrackTo,0);
			}else{
				upButton.modify(0,vanish);
				downButton.modify(0,vanish);
			}
			relayout(reDraw);
		}
		blockUpdate = false;
	}else{
	/*
		if (!needHorizontal && (client.getCurrent(Horizontal) != 0))
			client.doScroll(Horizontal,TrackTo,0);
		if (!needVertical && (client.getCurrent(Vertical) != 0))
			client.doScroll(Vertical,TrackTo,0);
	*/
	}
	updateScroll(direction);
}
//-------------------------------------------------------------------
private void checkButton(int direction,Control button)
//-------------------------------------------------------------------
{
	int pos = client.getCurrent(this.direction);
	boolean on = !button.hasModifier(Disabled,false);
	boolean canGo = client.canGo(this.direction,direction,pos);
	if (canGo != on){
		if (on) button.modify(Disabled,0);
		else button.modify(0,Disabled);
		button.repaintNow();
	}
}
//==================================================================
public void updateScroll(int which)
//==================================================================
{
	if (which == direction){
		checkButton(Higher,downButton);
		checkButton(Lower,upButton);
	}
}
/*
//==================================================================
public void setScroll(int which,int v,int a,int c)
//==================================================================
{
	mScrollBar sb = vbar;
	if (which == Horizontal) sb = hbar;
	sb.set(v,a,c,true);
}
*/
//===================================================================
public void modifyScrollers(int set,int clear)
//===================================================================
{
	upButton.modify(set,clear);
	downButton.modify(set,clear);
}
//===================================================================
public void onControlEvent(ControlEvent ev)
//===================================================================
{
	if (ev.target == downButton && ev.type == ev.PRESSED){
		client.doScroll(direction,ScrollHigher,1);
	}else if (ev.target == upButton && ev.type == ev.PRESSED){
		client.doScroll(direction,ScrollLower,1);
	}else
		super.onControlEvent(ev);
}
public void make(boolean remake)
{
	super.make(remake);
	//upButton.backGround = downButton.backGround = getBackground();
}
//===================================================================
public void dismantle(Control downTo)
//===================================================================
{
	if (this == downTo) return;
	super.dismantle(downTo);
	upButton = downButton = null;
}

//##################################################################
}
//##################################################################

