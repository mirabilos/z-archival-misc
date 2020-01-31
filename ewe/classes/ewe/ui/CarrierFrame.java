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
* This is a frame optimized for use for simple single element popup
* controls. For example it is used by a PullDownMenu to display the
* menu.
**/
//##################################################################
public class CarrierFrame extends Frame{
//##################################################################

{
	contentsOnly = true;
	wantPressedOutside = true;
	doSaveScreen = true;
	modify(Invisible/*|Transparent*/,0);
}

protected Control client,content,owner;
//-------------------------------------------------------------------
protected CarrierFrame(Control owner)
//-------------------------------------------------------------------
{
	this.owner = owner;
}
/**
* Create a new CarrierFrame. It is initally invisible and will not
* become visible until putInFrame() is called.
* client is the control which it will contain.
* owner is the control which caused the popup to be shown.
**/
//==================================================================
public CarrierFrame(Control client,Control owner)
//==================================================================
{
	this(owner);
	this.client = client;
	contents.addLast(client);
	content = contents;
}
//===================================================================
public void dismantle(Control downTo)
//===================================================================
{
	if (this == downTo) return;
	super.dismantle(downTo);
	client = content = owner = null;
}

//==================================================================
public boolean hasModifier(int what,boolean shouldInherit)
//==================================================================
{
	if ((modifiers & what) == what) return true;
	if (!shouldInherit) return false;
	if (owner != null) return owner.hasModifier(what,shouldInherit);
	return false;
}

//==================================================================
protected Rect getContentsSize()
//==================================================================
{
	return new Rect(content.getPreferredSize(null));
}
/**
* This will fit the client and the frame within the specified maxWidth and maxHeight.
*
* It MUST be called after calling Gui.execFrame(carrierFrame,parentFrame). This method
* adds it to the parentFrame (which should be the Frame of the owner control).
*
* If shrinkWidth is true then the frame will shrink to the width of the frame contents.
* If shrinkHeight is true then the frame will shrink to the height of the frame contents.
* If width/height is zero, then the frame will be the width/height of the client with
* no restriction on maximum or minimum size.
**/
//==================================================================
public void fitClientAndFrame(int width,int height,boolean shrinkWidth,boolean shrinkHeight)
//==================================================================
{
	int w = width, h = height;
	Rect r = getContentsSize();
	if (shrinkWidth || w == 0) if (r.width+borderWidth*2 < w || w == 0) w = r.width+borderWidth*2;
	if (shrinkHeight || h == 0) if (r.height+borderWidth*2 < h || h == 0) h = r.height+borderWidth*2;
	Frame f = getFrame();
	if (f != null){
		Rect p = f.getDim(null);
		if (w > p.width) w = p.width;
		if (h > p.height) h = p.height;
	}
	resizeTo(w,h);
}
/**
* This will position this carrier frame in the parent frame. It will
* attempt to position it at px and py, but it will automatically
* reposition it if necessary to be within the bounds of the parent
* frame.
*
* This should be called AFTER calling fitClientAndFrame, which itself
* should be called AFTER calling Gui.exec();
**/
//==================================================================
public void putInFrame(int px,int py) {putInFrame(px,py,py);}
public void putInFrame(int px,int py,int aboveY)
//==================================================================
{
	int x = px, y = py;
	if (x < 0) x = 0;
	if (y < 0) y = 0;
	Rect me = getDim(null);
	Frame f = getFrame();
	if (f != null){
		Rect p = f.getDim(null);
		int extra = x+me.width-p.width;
		if (extra > 0) x -= extra;
		extra = y+me.height-p.height;
		if (extra > 0) {
			if ((aboveY != py) && (aboveY >= me.height)) y = aboveY-me.height;
			else y -= extra;
		}
	}
	Window w = getWindow();
	Point p = Gui.getPosInParent(this,w);
	Rect r = Gui.visibleWindowClientArea(this);//w.getWindowRect(new Rect(),true);
	if (y+/*p.y+*/me.height >= r.height){
		y = r.height-me.height;//-p.y;
		if (y < 0) {
			y = 0;
			me.height = r.height;
		}
	}
	setRect(x,y,me.width,me.height);
	modify(0,Invisible);
}
/**
* This will place the frame at the y position and centre it horizontally at the x position.
* It will also make sure it fits within the parent frame.
* This calls putInFrame() and so the same rules apply.
**/
public void centreAtInFrame(int x,int y,int py)
{
	Rect me = getDim(null);
	int cx = x-me.width/2;
	putInFrame(cx,y,py);
}
//##################################################################
}
//##################################################################



