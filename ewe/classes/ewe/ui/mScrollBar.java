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
public class mScrollBar extends Holder implements ITrack{
//##################################################################

public Control less, more;
public ScrollTrack track;
{
	modify(NotAnEditor|NoFocus,0);
}

/**
 * Set the sizes for the scroll bar buttons and thumb bar.
 * @param buttonWidth the preferred button width.
 * @param buttonHeight the preferred button height.
 * @param minThumb the preferred minimum thumb bar thickness.
 */
public void setPreferredSizes(int buttonWidth, int buttonHeight, int minThumb)
{
	if (less != null) less.setPreferredSize(buttonWidth,buttonHeight);
	if (more != null) more.setPreferredSize(buttonWidth,buttonHeight);
	if (track != null) track.minThumb = minThumb;
}
/**
Create a new ScrollBar.
@param type either IScroll.Vertical or IScroll.Horizontal
@param options any of the IScroll.OPTION_XXX values ORed together.
*/
//==================================================================
public mScrollBar(int type)
//==================================================================
{
	this(type,0);
}
private boolean indicatorOnly;
/**
Create a new ScrollBar.
@param type either IScroll.Vertical or IScroll.Horizontal
@param options any of the IScroll.OPTION_XXX values ORed together.
*/
//==================================================================
public mScrollBar(int type, int options)
//==================================================================
{
	if ((options & IScroll.OPTION_INDICATOR_ONLY) != 0){
		modify(/*DrawFlat|*/SmallControl,0);
		indicatorOnly = true;
	}
	boolean ind = indicatorOnly;
	mButton l = null, m = null;
	if (type == Horizontal){
		if (!ind) addNext(l = new ArrowButton(Left)).setCell(VSTRETCH);
		addNext(track = new ScrollTrack(Horizontal,options));
		if (!ind) addLast(m = new ArrowButton(Right)).setCell(VSTRETCH);
	}else{
		if (!ind) addLast(l = new ArrowButton(Up)).setCell(HSTRETCH);
		addLast(track = new ScrollTrack(Vertical,options));
		if (!ind) addLast(m = new ArrowButton(Down)).setCell(HSTRETCH);
	}
	if (!ind){
		l.shouldRepeat = l.actionOnPress =
		m.shouldRepeat = m.actionOnPress = true;
		less = l; more = m;
	}
	modifyAll(NotAnEditor|NoFocus,TakesKeyFocus,true);
	/*
	mButton l,m;
	if (type == Horizontal){
		addNext(l = new ArrowButton(Left)).setCell(VSTRETCH);
		addNext(track = new ScrollTrack(Horizontal));
		addLast(m = new ArrowButton(Right)).setCell(VSTRETCH);
	}else{
		addLast(l = new ArrowButton(Up)).setCell(HSTRETCH);
		addLast(track = new ScrollTrack(Vertical));
		addLast(m = new ArrowButton(Down)).setCell(HSTRETCH);
	}
	l.shouldRepeat = l.actionOnPress =
	m.shouldRepeat = m.actionOnPress = true;
	less = l; more = m;
	modifyAll(NotAnEditor|NoFocus,TakesKeyFocus,true);
	*/
}

//===================================================================
public void setFollowTracking(boolean ft)
//===================================================================
{
	track.setFollowTracking(ft);
}
//==================================================================
public void onEvent(Event ev)
//==================================================================
{
	if (ev.target == less && ev.type == ControlEvent.PRESSED) track.generate(ScrollLower,1);
	else if (ev.target == more && ev.type == ControlEvent.PRESSED) track.generate(ScrollHigher,1);
	else super.onEvent(ev);
}
//===================================================================
public void onPenEvent(PenEvent ev)
//===================================================================
{
	if (ev.type == PenEvent.SCROLL_UP || ev.type == PenEvent.SCROLL_DOWN){
		track.generate((ev.type == PenEvent.SCROLL_UP) ? ScrollLower : ScrollHigher,1);
	}else
		super.onPenEvent(ev);
}
//==================================================================
public void set(int vs,int ac,int cur) {set(vs,ac,cur,true);}
public void set(int vs,int ac,int cur,boolean repaint)
//==================================================================
{
	track.setPositions(vs,ac,cur,repaint);
}
//==================================================================
public int modify(int set,int clear)
//==================================================================
{
	int obj = super.modify(set,clear);
	if (less != null) less.modify(set,clear);
	if (more != null) more.modify(set,clear);
	if (track != null) track.modify(set,clear);
	return obj;
}
/*
public Rect getPreferredSize()
{
	Rect r = super.getPreferredSize();
	//System.out.println(Geometry.toString(((mControl)client).getPreferredSize()));
	System.out.println("SB:"+Geometry.toString(r));
	return r;
}
*/
//##################################################################
}
//##################################################################


