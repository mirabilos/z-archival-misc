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
* A Canvas is a general Control that is the best control to extend if you are designing
* a control that may be used as Container or that may need to be scrolled.
**/
//##################################################################
public class Canvas extends Container implements ScrollClient{
//##################################################################
//==================================================================
//public boolean hasBorder = true;
private Image buffer = null;
public Rect virtualSize = null;
//==================================================================
public Dimension getDisplayedSize(Dimension dest) {return getSize(dest);}
//==================================================================

//==================================================================
public Dimension getMySize(Dimension dest)
//==================================================================
{
	dest = Dimension.unNull(dest);
	if (virtualSize != null) return dest.set(virtualSize);
	else return getSize(dest);
}
//==================================================================
public Image getBuffer()
//==================================================================
{
	Dimension size = getMySize(null);
	int w = size.width, h = size.height;
	if (w < 1) w = 1; if (h < 1) h = 1;
	if (buffer == null) buffer = new Image(w,h);
	if (buffer.getWidth() != size.width || buffer.getHeight() != size.height){
		buffer.free();
		buffer = new Image(w,h);
	}
	return buffer;
}

//==============================================================
/**
* This is the origin for the Canvas. If it is null then it should be considered to be (0,0)
**/
public Point origin;// = new Point(0,0);//(10,10);
//==================================================================

//==================================================================
//public Graphics getControlGraphics() {return super.getGraphics();}
//==================================================================
//public Graphics
//==============================================================
//	getGraphics()
//==============================================================
//{
//	Graphics g = super.getGraphics();
//	if (g != null) g.translate(-origin.x,-origin.y);
//	return g;
//}
//==================================================================
public void reduceClip(Graphics g,Rect r)
//==================================================================
{
/*
	Rect full = new Rect(0,0,width,height);
	full.x += origin.x; full.y += origin.y;
	Rect inter = full.getIntersection(r,null);
	//System.out.println(Geometry.toString(r)+" = "+Geometry.toString(inter));
	g.setClip(inter.x,inter.y,inter.width,inter.height);
*/
	if (r == null || g == null) return;
	Rect full = g.getClip(Rect.buff);
	Rect inter = full == null ? r : full.getIntersection(r,full);
	if (inter != null)
		g.setClip(inter.x,inter.y,inter.width,inter.height);
}

//private Rect visibleArea = new Rect(0,0,0,0);

//==================================================================
public Rect getVisibleArea(Rect dest)
//==================================================================
{
	dest = getRect(dest);
	if (origin == null) dest.x = dest.y = 0;
	else{
		dest.x = origin.x;
		dest.y = origin.y;
	}
	//visibleArea.set(dest);
	return dest;
}
//==================================================================
public void fixOrigin()
//==================================================================
{
	Dimension _rect = getDisplayedSize(null);
	Dimension size = getMySize(null);
	if (origin == null) origin = new Point();
	if (size.width-origin.x < width) origin.x = size.width-width;
	if (size.height-origin.y < height) origin.y = size.height-height;
	if (origin.x < 0) origin.x = 0;
	if (origin.y < 0) origin.y = 0;
}
//==================================================================
public void onScroll(int movedX,int movedY){}
public void onSetOrigin()
//==================================================================
{
	//ewe.sys.Vm.debug(origin.x+", "+origin.y);
}
static Point tempMoved = new Point();
/**
*
**/
//==================================================================
public boolean setOrigin(int nx,int ny,Point moved)
//==================================================================
{
	if (origin == null) origin = new Point();
	int x = origin.x, y = origin.y;
	if (moved == null) moved = tempMoved;
	origin.x = nx; origin.y = ny;
	fixOrigin();
	onSetOrigin();
	moved.x = origin.x-x; moved.y = origin.y-y;
	boolean didMove = ((moved.x != 0) || (moved.y != 0));
	if (didMove) {
		onScroll(moved.x,moved.y);
		if (moved.x != 0) updateScrollServer(Horizontal);
		if (moved.y != 0) updateScrollServer(Vertical);
	}
	return didMove;
}
//==================================================================
public int getVisible(int which,int forSize) {if (which == Horizontal) return forSize; else return forSize;}
public int getActual(int which) {if (which == Horizontal) return getMySize(null).width; else return getMySize(null).height;}
public int getCurrent(int which) {if (which == Horizontal) return origin == null ? 0 : origin.x; else return origin == null ? 0 : origin.y;}
public boolean needScrollBar(int which,int forSize)
{
	return getVisible(which,forSize) < getActual(which);
}
public boolean canGo(int orientation,int direction,int position)
{
	return true;
}
//==================================================================
public void checkScrolls()
//==================================================================
{
	if (ss == null) return;
	ss.checkScrolls();
}
//==================================================================
public void updateScrollServer(int which)
//==================================================================
{
	if (ss == null) return;
	ss.updateScroll(which);
}
/**
* Scroll the panel and save in moved, the amount of distance moved in x and y.
* Scroll the panel - this does not update the ScrollBars. To do so you should call checkScrolls().
 * @param dx The x distance to move.
 * @param dy The y distance to move.
 * @param moved The actual x and y distance moved. This may be different to dx and dy since the
 * the Canvas may limit the location the origin can actually be.
 * @return true if a scroll was done, false if not.
	 */
//============================================================
public boolean scroll(int dx,int dy,Point moved)
//============================================================
{
	if (moved == null) moved = new Point();
	if (origin == null) origin = new Point();
	boolean m = setOrigin(origin.x+dx,origin.y+dy,moved);
	if (canScreenScroll()){
		scrollAndRepaint(0,0,width,height,-moved.x,-moved.y);
	}else
		repaintNow();
	return m;
}

/**
* This should return true if pixel scrolling of the on-screen data is allowed.
**/
//===================================================================
public boolean canScreenScroll()
//===================================================================
{
	return true;
}
/**
* Scroll the panel - this does not update the ScrollBars. To do so you should call checkScrolls().
 * @param dx The x distance to move.
 * @param dy The y distance to move.
 * @return true if a scroll was done, false if not.
 */
//============================================================
public boolean	scroll(int dx,int dy)
//============================================================
{
	return scroll(dx,dy,null);
}
//==================================================================
protected int getPercent(int size,int percent,int minimum)
//==================================================================
{
	int val = (percent*size)/100;
	if (val < minimum) val = minimum;
	return val;
}
//==================================================================
//public void setServer(ScrollServer server) {ss = server;}
//==================================================================
public int scrollPercent = 30;
//==================================================================
public void doScroll(int which,int action,int value)
//==================================================================
{
	if (origin == null) origin = new Point();
	Dimension d = getDisplayedSize(null);
	if (which == Horizontal){
		if (action == ScrollLower) scroll(-getPercent(d.width,scrollPercent,1),0);
		else if (action == ScrollHigher) scroll(getPercent(d.width,scrollPercent,1),0);
		else if (action == PageLower) scroll(-getPercent(d.width,90,1),0);
		else if (action == PageHigher) scroll(getPercent(d.width,90,1),0);
		else if (action == TrackTo) scroll(value-origin.x,0);
	}else{
		if (action == ScrollLower) scroll(0,-getPercent(d.height,scrollPercent,1));
		else if (action == ScrollHigher) scroll(0,getPercent(d.height,scrollPercent,1));
		else if (action == PageLower) scroll(0,-getPercent(d.height,90,1));
		else if (action == PageHigher) scroll(0,getPercent(d.height,90,1));
		else if (action == TrackTo) scroll(0,value-origin.y);
	}
	//repaintNow();
}
//==================================================================
public void doBorder(Graphics g)
//==================================================================
{
	int flags = getModifiers(true);
	if (borderStyle != 0) g.draw3DRect(new Rect(0,0,width,height),borderStyle,((flags & DrawFlat) != 0),null,borderColor);
	else super.doBorder(g);
	/*
	mColor.setColor(g,mColor.Black);
	g.drawLine(0,0,width-1,0);
	g.drawLine(0,height-1,width-1,height-1);
	g.drawLine(0,0,0,height-1);
	g.drawLine(width-1,0,width-1,height-1);
	*/
}
/*
//==================================================================
public void doPaint(Graphics g,Rect area)
//==================================================================
{
	int flags = getModifiers(true);
	if (!((flags & Invisible) == 0)) return;
	//doBackground(g);
	//if (hasBorder)
	doBorder(g);
	//System.out.println("Painting: "+this);
}
*/

/**
* Overriding classes must set this true if the new class fully implements the ScrollClient
* interface (which Canvas does not).
**/
protected boolean isFullScrollClient = true;
/**
* Return a fully implemented ScrollClient for this Control. This will return this Control
* if it fully implements ScrollClient otherwise it will return a ScrollableHolder containing
* this Control.
**/
//===================================================================
public ScrollClient getScrollClient()
//===================================================================
{
	return isFullScrollClient ? this : new ScrollableHolder(this);
}
/**
* Return a ScrollablePanel (usually a ScrollBarPanel) which contains this Control.
**/
//===================================================================
public ScrollablePanel getScrollablePanel()
//===================================================================
{
	return new ScrollBarPanel(getScrollClient());
}

//===================================================================
public boolean scrollToVisible(int x, int y, int width, int height)
//===================================================================
{
	if (parent instanceof ScrollServer){
		if (origin == null) return false;
		int dx = 0, dy = 0;

		if (x < 0 || width >= this.width) dx = x;
		else if (x+width >= this.width) dx = -(this.width-width-x);

		if (y < 0 || height >= this.height) dy = y;
		else if (y+height >= this.height) dy = -(this.height-height-y);

		boolean ret = dx != 0 || dy != 0 ? scroll(dx,dy) : false;
		if (ret) checkScrolls();
		return ret;
	}else if (parent != null){
		return parent.scrollToVisible(x+this.x,y+this.y, width, height);
	}else{
		return false;
	}
}
//##################################################################
}
//##################################################################



