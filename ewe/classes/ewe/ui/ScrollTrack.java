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
public class ScrollTrack extends TrackControl{
//##################################################################
public int minThumb = 10;
//==================================================================
public void doPaint(Graphics g,Rect area)
//==================================================================
{
	int flags = getModifiers(true);
	doBackground(g);
	boolean flat = ((flags & DrawFlat) != 0);
	//g.draw3DButton(getDim(null),true,null,flat,true);
	boolean soft = ((ButtonObject.buttonEdge & BF_SOFT) == BF_SOFT);
	boolean etched = ((standardEdge & EDGE_ETCHED) == EDGE_ETCHED);
	if (globalPalmStyle){
		//g.setPen(new Pen(Color.Black,Pen.SOLID,1));
		g.setColor(Color.Black);
		if (type == Horizontal) {
			g.drawLine(0,height/2,width-1,height/2);
			g.drawLine(0,height/2-2,0,height/2+2);
			g.drawLine(width-1,height/2-2,width-1,height/2+2);
		}else{
			g.drawLine(width/2,0,width/2,height-1);
			g.drawLine(width/2-2,0,width/2+2,0);
			g.drawLine(width/2-2,height-1,width/2+2,height-1);
		}
	}else
	g.draw3DRect(
		getDim(Rect.buff),
		soft ? ButtonObject.buttonEdge & ~BF_BUTTON : (etched ? standardEdge :
		Graphics.EDGE_SUNKEN|((standardEdge & BDR_OUTLINE) != 0 ? BDR_OUTLINE : 0)) ,
		flat,
		soft ? getBackground() : Color.LighterGray,//etched ? Color.White : null,
		Color.Black);
	//g.draw3DButton(,false,null,flat,true);
	g.draw3DRect(
		getThumbArea(Rect.buff),
		//etched ? Graphics.EDGE_BUMP :
		ButtonObject.buttonEdge,//standardEdge,
		//Graphics.EDGE_RAISED|((standardEdge & BDR_OUTLINE) != 0 ? BDR_OUTLINE : 0) ,
		flat,
		flat ? getForeground() : getBackground(),
		Color.Black);
		/*
	if (flat){
		g.setColor(getForeground());
		g.fillRect(Rect.buff.x,Rect.buff.y,Rect.buff.width,Rect.buff.height);
	}
	*/
}
/**
Create a new ScrollTrack.
@param type Either IScroll.Horizontal or IScroll.Vertical
*/
//===================================================================
public ScrollTrack(int type)
//===================================================================
{
	this(type, 0);
}

/** The options specified in the constructor.**/
protected int options;

/**
Create a new ScrollTrack.
@param type Either IScroll.Horizontal or IScroll.Vertical
@param trackOptions Any of the IScroll.OPTION_XXX values OR'ed together
*/
//===================================================================
public ScrollTrack(int type, int trackOptions)
//===================================================================
{
	this.type = type;
	this.options = trackOptions;
	//if ((options & OPTION_INDICATOR_ONLY) != 0)
		//modify(SmallControl|DrawFlat,0);
}

//==================================================================
protected void calculateSizes()
//==================================================================
{
	boolean etched = ((standardEdge & EDGE_ETCHED) == EDGE_ETCHED);
	if (etched && minThumb < 14) minThumb = 14;
	int w = 30, h = 15;
	if (hasModifier(SmallControl,true)) {
		w = 15;
		h = 10;
	}
	if (type == Horizontal) {
		preferredWidth = w;
		preferredHeight = h;
	}else if (type == Vertical) {
		preferredWidth = h;
		preferredHeight = w;
	}
}

Point thumbPos = new Point();
int thumbThickness = 10;

public Rect getThumbArea() {return getThumbArea(null);}
public Rect getThumbArea(Rect r)
{
	r = Rect.unNull(r);
	if (thumbThickness == 0) return r.set(-100,-100,1,1);
	int w = thumbThickness, h = thumbThickness;
	if (type == Horizontal) h = getDim(r).height;
	else w = getDim(r).width;
	r.set(thumbPos.x,thumbPos.y,w,h);
	boolean etched = ((standardEdge & EDGE_ETCHED) == EDGE_ETCHED);
	if (etched) {
		if (type == Horizontal || true){
			r.y+=2;
			r.height-=4;
		}
		if (type == Vertical || true){
			r.x+=2;
			r.width-=4;
		}
	}
	return r;
}
public void setThumbPos(int x,int y)
{
	Rect r = getDim(null);
	if (type == Horizontal) {
		y = 0;
		int max = r.width-thumbThickness;
		if (x > max) x = max;
		if (x < 0) x = 0;
	}else{
 		x = 0;
		int max = r.height-thumbThickness;
		if (y > max) y = max;
		if (y < 0) y = 0;
	}
	thumbPos.move(x,y);
}
public void generate(int what,int value)
{
	postEvent(new ScrollEvent(this,what,value));
	//System.out.println(what+","+value);
}

protected int visible, actual, current, scrollable;

//==================================================================
public void recalculate()
//==================================================================
{
	Rect r = getRect();
	//if (visible >= actual) thumbThickness = 0;

	if (actual <= visible && current > 0){
		actual += current;
		//System.out.println("Compensating: "+visible+", "+actual);
	}
	scrollable = actual-visible;
	int max = r.width;
	if (type == Vertical) max = r.height;
	//System.out.println(visible+" "+actual+" "+current+" "+max);
	if (scrollable == 0) scrollable = 10;
	if (actual == 0) actual = 10;
	thumbThickness = (visible*max)/actual;
	//System.out.println(visible+" "+actual+" "+current);
	if (thumbThickness < minThumb) thumbThickness = minThumb;
	int pos = (int)(((double)current/scrollable)*(max-thumbThickness));
	if (!amTracking)
		if (type == Vertical) setThumbPos(0,pos);
		else setThumbPos(pos,0);
}
//==================================================================
public void resizeTo(int width,int height)
//==================================================================
{
	super.resizeTo(width,height);
	recalculate();
}
/**
 * Set the postion and size of the "thumb" control in the track, using units of the users choice.
 * For example, if the ScrollTrack is being used to scroll a number of lines of text
 * in a Text editor - then each unit may represent one line of text. If some kind of Image
 * is being scrolled, then each unit may represent one pixel.
 * @param vs (visible) this represents the number of units that are "visible" on screen.
 * For instance the number of lines that are displayed on screen of a text editor,
 * or pixel lines in an image.
 * @param ac (actual) this represents the total number of units contained in the data being displayed.
 * For instance the total number of lines in a text editor, or total number of pixel
 * lines in an image.
 * @param cur (current) this represents the current unit position being viewed.
 * For instance the line number of the line at the top of the display in a text editor,
 * or the pixel line number (Y-co-ordinate) of the line at the top of the display in an
 * image.
 * @param repaint true to have the control repaint itself, false if not.
 */
//==================================================================
public void setPositions(int vs,int ac,int cur,boolean repaint)
//==================================================================
{
	if (visible == vs && actual == ac && current == cur) return;
	if (vs > ac) vs = ac;
	visible = vs;
	actual = ac;
	current = cur;
	recalculate();
	if (!amTracking && repaint) repaintNow();
}
//==================================================================
public int getTrackTo()
//==================================================================
{
	Rect r = getRect();
	int max = r.width, cp = thumbPos.x;
	if (type == Vertical) {
		max = r.height;
		cp = thumbPos.y;
	}
	int avail = max-thumbThickness;
	if (avail == 0 || cp == 0) return 0;
	if (cp == avail) return scrollable;
	return (int)(((double)cp/avail)*scrollable);
}
//##################################################################
}
//##################################################################

