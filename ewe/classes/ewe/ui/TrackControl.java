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
public abstract class TrackControl extends Control implements ITrack{
//##################################################################
{
	modify(WantDrag|WantHoldDown,0);
}
public boolean followTracking = false;
{
 followTracking = true;//((ewe.sys.Vm.getParameter(ewe.sys.Vm.VM_FLAGS) & ewe.sys.Vm.VM_FLAG_SLOW_MACHINE) == 0);
}
//==================================================================
public abstract Rect getThumbArea();
public abstract void setThumbPos(int x,int y);


/**
 * This is called when the "thumb" area is moved. The method setThumbPos() will be
 * called before this is done. You are supposed to internally store the location
 * of the thumb area and be able to generate an appropriate value for getTrackTo()
 * for your particular application. For example you may generate a value between
 * 0 and 100 depending on the thumb position.
 *
 * @return an appropriate application specific value depending on the current position
 * of the thumb.
 */
public abstract int getTrackTo();
//==================================================================
public void setFollowTracking(boolean ft) {followTracking = ft;}

protected static final int LessThanThumb = 1, MoreThanThumb = 2, OnThumb = 3;
public int type = Horizontal;
//==================================================================
public int whichArea(Point p)
//==================================================================
{
	Rect t = getThumbArea();
	if (type == Horizontal){
		if (p.x < t.x) return LessThanThumb;
		else if (p.x >= t.x+t.width) return MoreThanThumb;
		else return OnThumb;
	}else{
		if (p.y < t.y) return LessThanThumb;
		else if (p.y >= t.y+t.height) return MoreThanThumb;
		else return OnThumb;
	}
}
int pressedArea;
boolean amTracking = false;

//==================================================================
public void generate(int what,int value){}
//==================================================================

//==================================================================
public void generatePage()
//==================================================================
{
	if (pressedArea == LessThanThumb) generate(PageLower,1);
	else if (pressedArea == MoreThanThumb) generate(PageHigher,1);
}
//==================================================================
public void penPressed(Point p)
//==================================================================
{
	pressedArea = whichArea(p);
	generatePage();
}
//==================================================================
public void penHeld(Point p)
//==================================================================
{
	int nowPressed = whichArea(p);
	if (nowPressed != pressedArea) return;
	generatePage();
	wantAnotherPenHeld = true;
}
//==================================================================
public void startDragging(DragContext dc)
//==================================================================
{
	if (pressedArea != OnThumb) return;
	Rect r = getThumbArea();
	dc.start.translate(-r.x,-r.y);
	amTracking = true;
}
//==================================================================
public void stopDragging(DragContext dc)
//==================================================================
{
	generate(TrackTo,getTrackTo());
	amTracking = false;
}
//==================================================================
public void dragged(DragContext dc)
//==================================================================
{
	if (pressedArea != OnThumb) return;
	//if (!isOnMe(dc.curPoint)) return;
	Rect r = getThumbArea();
	int x = r.x, y = r.y;
	setThumbPos(dc.curPoint.x-dc.start.x,dc.curPoint.y-dc.start.y);
	r = getThumbArea();
	if (r.x == x && r.y == y) return;
	if (followTracking) generate(TrackTo,getTrackTo());
	repaintNow();
}


//##################################################################
}
//##################################################################


