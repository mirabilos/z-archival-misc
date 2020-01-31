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
* This is a CarrierFrame which scrolls its contents.
**/
//##################################################################
public class ScrollableCarrierFrame extends CarrierFrame {
//##################################################################
/**
* This is the ScrollablePanel which the frame contains.
**/
public ScrollablePanel sp;
/**
* This is the ScrollableHolder which is within the ScrollablePanel sp.
**/
public ScrollClient scrollClient;

/**
* If a title is used, this will be the control that holds the title text. Usually it
* will be an mLabel unless makeTitleControl() is changed.
**/
public Control titleControl;
/**
* Create the ScrollableCarrierFrame.
**/
//==================================================================
public ScrollableCarrierFrame(Control client,Control owner)
//==================================================================
{
	this(client,owner,false);
}
//==================================================================
public ScrollableCarrierFrame(Control client,Control owner,boolean useScrollBars)
//==================================================================
{
	this(client,owner,useScrollBars,null);
}
//==================================================================
public ScrollableCarrierFrame(Control client,Control owner,boolean useScrollBars,String title)
//==================================================================
{
	super(owner);
	this.client = client;
	if (client != null) font = client.getFont();
	Control ts = client;
	if (!(ts instanceof ScrollClient)){
		ScrollableHolder sh = new ScrollableHolder(client);
		backGround = client.getBackground();
		foreGround = client.getForeground();
		sh.stretchComponent = true;
		sh.modify(TakeControlEvents,0);
		ts = sh;
	}
	scrollClient = (ScrollClient)ts;
	sp = useScrollBars ? (ScrollablePanel)new ScrollBarPanel((ScrollClient)ts) : (ScrollablePanel)new UpDownScroller((ScrollClient)ts);
	content = sp;
	sp.modify(TakeControlEvents,0);
	sp.modifyScrollers(DrawFlat/*|SpecialBackground*/,0);
	//sp.backGround = Color.White;
	if (title != null){
		titleControl = makeTitleControl(title);
		if (titleControl != null)
		contents.addLast(titleControl).setCell(HSTRETCH);
	}
	contents.addLast(sp);
}
//-------------------------------------------------------------------
protected Control makeTitleControl(String title)
//-------------------------------------------------------------------
{
	if (title == null) return null;
	mLabel l = new mLabel(title);
	l.anchor = CENTER;
	l.backGround = Color.LightGray;
	l.setBorder(BDR_OUTLINE|BF_BOTTOM,1);
	l.font = getFont().changeStyle(Font.BOLD);
	return l;
}
//-------------------------------------------------------------------
protected Rect getContentsSize()
//-------------------------------------------------------------------
{
	Rect r = super.getContentsSize();
	if (allowTitleExpansion && titleControl != null){
		Dimension d = getPreferredSize(null);
		r.height = d.height;
	}
	return r;
	/*
		return new Rect(getPreferredSize(null));
	else
		return super.getContentsSize();
		*/
}

/**
Set this true if the addition of a title should allow the entire frame to increase its
size. This is used, for example, if a menu is being shown, but the menu is less than 5 items high.
**/
public boolean allowTitleExpansion = false;

//##################################################################
}
//##################################################################

