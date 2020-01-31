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
* You should not use this directly except to set properties such as the thickness
* and arrow direction. Instead use a SplittablePanel to get two
* empty CellPanels with a splitter between them. Then you can configure how the
* panel operates using SplittablePanel.setSplitter().
**/
//##################################################################
public class PanelSplitter extends Control {
//##################################################################
/**
* A value used with setOpenCloseTypes().
**/
public static final int BEFORE = 0x1;
/**
* A value used with setOpenCloseTypes().
**/
public static final int AFTER = 0x2;
/**
* A value used with setOpenCloseTypes().
**/
public static final int MIN_SIZE = 0x4;
/**

* A value used with setOpenCloseTypes().
**/
public static final int PREFERRED_SIZE = 0x8;
/**
* A value used with setOpenCloseTypes().
**/
public static final int HIDDEN = 0x10;

public int openType = AFTER|MIN_SIZE;
public int closeType = BEFORE|MIN_SIZE;

/**
* This is a possible state of the splitter.
**/
public static final int OPENED = 1;
/**
* This is a possible state of the splitter.
**/
public static final int CLOSED = 2;
/**
* This will either be OPENED, or CLOSED or 0 (indicating neither opened nor closed).
**/
public int state = 0;

/**
* If this is true, then a click on the splitter when the splitter is neither opened nor closed will
* cause it to go into the Closed state. If it is false then a click will cause it to go into the Opened state.
* By default it is true.
**/
public boolean doCloseFirst = true;

//===================================================================
public PanelSplitter setOpenCloseTypes(int openType,int closeType)
//===================================================================
{
	this.openType = openType;
	this.closeType = closeType;
	return this;
}


/**
 * Open or close the splitter. Opening is a movement to the right or downwards.
 * The limits of the open or close is defined by the openType and closeType values.
 * @param open if this is true then an open will be performed, otherwise a close will be performed.
 */
//===================================================================
public void doOpenClose(boolean open)
//===================================================================
{
	if (type == VERTICAL) {
		int b = before.width, a = after.width;
		int ch = 0;
		if (open){
			if ((openType & BEFORE) != 0){
				//HIDDEN and MIN_SIZE don't make sense here.
				//if (openType & HIDDEN) ch = -b;
				ch = before.getPreferredSize(null).width-b;
			}else{
				if ((openType & HIDDEN) != 0) ch = a;
				else if ((openType & MIN_SIZE) != 0) ch = a-after.getMinimumSize(null).width;
				else
					ch = a-after.getPreferredSize(null).width;
			}
		}else{
			if ((closeType & AFTER) != 0){
				//HIDEN and MIN_SIZE don't make sense here.
				ch = after.getPreferredSize(null).width-a;
			}else{
				if ((closeType & HIDDEN) != 0) ch = b;
				else if ((closeType & MIN_SIZE) != 0) ch = b-before.getMinimumSize(null).width;
				else
					ch = b-before.getPreferredSize(null).width;
			}
			ch = -ch;
		}
		((CellPanel)parent).splitterSetTo(this,ch,0);
	}
	if (type == HORIZONTAL) {
		int b = before.height, a = after.height;
		int ch = 0;
		if (open){
			if ((openType & BEFORE) != 0){
				//HIDDEN and MIN_SIZE don't make sense here.
				//if (openType & HIDDEN) ch = -b;
				ch = before.getPreferredSize(null).height-b;
			}else{
				if ((openType & HIDDEN) != 0) ch = a;
				else if ((openType & MIN_SIZE) != 0) {
					ch = a-after.getMinimumSize(null).height;
					//ewe.sys.Vm.debug("MS: "+after.getMinimumSize(null).height);
				}
				else
					ch = a-after.getPreferredSize(null).height;
			}
		}else{
			if ((closeType & AFTER) != 0){
				//HIDEN and MIN_SIZE don't make sense here.
				ch = after.getPreferredSize(null).height-a;
			}else{
				if ((closeType & HIDDEN) != 0) ch = b;
				else if ((closeType & MIN_SIZE) != 0) {
					ch = b-before.getMinimumSize(null).height;
					//ewe.sys.Vm.debug("MS: "+before.getMinimumSize(null).height);
				}else
					ch = b-before.getPreferredSize(null).height;
			}
			ch = -ch;
		}

		((CellPanel)parent).splitterSetTo(this,0,ch);
	}
	state = open ? OPENED : CLOSED;
}
boolean doMaximize = true, doPreferredSize = false, doHide = false;
/**
* This is the control before (to the left or above) the splitter.
**/
public Control before;
/**
* This is the control after (to the right or below) the splitter.
**/
public Control after;
/**
* This should be VERTICAL or HORIZONTAL.
**/
public int type;
public static int VERTICAL = 1, HORIZONTAL = 2;
public int thickness = 8;
public int arrowPosition = 0;
public static IImage updown = ImageCache.cache.get("ewe/UpDown.bmp",Color.White);
public static IImage leftright = ImageCache.cache.get("ewe/LeftRight.bmp",Color.White);
{
	modify(NoFocus|WantDrag|NotAnEditor,0);
	startDragResolution = 8;
	//backGround = new Color(255,0,0);
}
protected boolean expandBefore;
//==================================================================
PanelSplitter(int type)
//==================================================================
{
	this.type = type;
	if (type == VERTICAL) setCursor(mApp.rotated||mApp.counterRotated ? ewe.sys.Vm.UP_DOWN_CURSOR : ewe.sys.Vm.LEFT_RIGHT_CURSOR);
	else setCursor(mApp.rotated||mApp.counterRotated ? ewe.sys.Vm.LEFT_RIGHT_CURSOR : ewe.sys.Vm.UP_DOWN_CURSOR);
	//if (type == VERTICAL) defaultAddMeCellConstraints = mPanel.VSTRETCH;
	//else defaultAddMeCellConstraints = mPanel.HSTRETCH;
}

//==================================================================
public void calculateSizes()
//==================================================================
{
	preferredWidth = preferredHeight = thickness;
	if (arrowPosition != 0)
		if (type == HORIZONTAL) preferredHeight += 10;
		else preferredWidth += 10;
}

//==================================================================
public void doPaint(Graphics g,Rect area)
//==================================================================
{
	if (width == 0 || height == 0) return;
	int flags = getModifiers(true);
	if ((g == null) || !((flags & Invisible) == 0)) return;
	doBackground(g);
	boolean flat = ((flags & DrawFlat) != 0);
	Rect r = getRect(null);
	r.x = r.y = 0;
	Rect r2 = new Rect(r.x,r.y,r.width,r.height);
	int h = r.height, w = r.width;
	int thick = 0;
	if (type == HORIZONTAL) {
		if (arrowPosition != 0) r.height -= 10;
		thick = r.height;
		if (arrowPosition == Up) r.y = h-thick;
	}
	if (type == VERTICAL) {
		if (arrowPosition != 0) r.width -= 10;
		thick = r.width;
		if (arrowPosition == Left) r.x = w-thick;
	}
	//debugControl.setText(""+r+"=>"+r2);
	if ((mInput.inputEdge & EDGE_ETCHED) == EDGE_ETCHED) {
		g.draw3DRect(r,mInput.inputEdge/*Graphics.EDGE_ETCHED*/,flat,
		getBackground(),//Color.LightGray,
		Color.White);
		/*if (thick >= 4 && !flat){
			r.x++; r.y++; r.width-=2; r.height-=2;
			g.draw3DRect(r,Graphics.EDGE_ETCHED,flat,Color.DarkGray,Color.White);
		}*/
	}else if (Color.getMonochrome() && flat){
		g.setColor(Color.Black);
		g.drawRect(r.x+1,r.y+1,r.width-2,r.height-2);
	}else{
		g.draw3DRect(r,Graphics.EDGE_RAISED,flat,
		getBackground(),
		//Color.DarkGray,
		Color.White);
		if (thick >= 4 && !flat){
			r.x++; r.y++; r.width-=2; r.height-=2;
			g.draw3DRect(r,Graphics.EDGE_SUNKEN,flat,
			getBackground(),
			//Color.DarkGray,
			Color.White);
		}
	}
	if (arrowPosition != 0) {
		g.setColor(getForeground());
		r.width = r.height = 10;
		if (type == HORIZONTAL) r.x = (w-8)/2 - 4;
		else r.y = (h-8)/2 - 4;
		if (arrowPosition == Up) r.y = 1;
		else if (arrowPosition == Down) r.y = h-8-1;
		else if (arrowPosition == Left) r.x = 1;
		else if (arrowPosition == Right) r.x = w-8-1;

		IImage im = leftright;
		if (type == HORIZONTAL) im = updown;
		im.draw(g,r.x,r.y,0);
		/*
		int dir = Left;
		if (type == HORIZONTAL) dir = Up;
		g.drawArrow(r,dir);
		if (type == HORIZONTAL) r.x += 8;
		else r.y += 8;
		dir = Right;
		if (type == HORIZONTAL) dir = Down;
		g.drawArrow(r,dir);
		*/
	}
}

protected static ImageBuffer dragImage = new ImageBuffer();
protected Point myLoc, curLoc, startPoint;
protected Graphics screen;
protected int maxChange, minChange;
protected Control be, ae;
//==================================================================
public void changeIt(int change)
//==================================================================
{
	if (change < minChange) change = minChange;
	if (change > maxChange) change = maxChange;
	if (type == HORIZONTAL) {
		//FIX
		//be.heightAdjust += change;
		//ae.heightAdjust -= change;
		((CellPanel)parent).splitterSetTo(this,0,change);
	}else{
		//FIX
		//be.widthAdjust += change;
		//ae.widthAdjust -= change;
		((CellPanel)parent).splitterSetTo(this,change,0);
	}
	state = 0;
	//ae.panel.relayout(true);
}
//==================================================================
protected void checkLimits()
//==================================================================
{
	be = before;
 	ae = after;
	Dimension d = new Dimension();
	if (type == HORIZONTAL) {
	//FIX
		maxChange = after.height-after.getMinimumSize(d).height;
		minChange = -(before.height-before.getMinimumSize(d).height);
		//maxChange = -ae.limitHeightAdjustChange(-100000);
		//minChange = be.limitHeightAdjustChange(-100000);
	}else{
	//FIX
		maxChange = after.width-after.getMinimumSize(d).width;
		minChange = -(before.width-before.getMinimumSize(d).width);
		//maxChange = -ae.limitWidthAdjustChange(-100000);
		//minChange = be.limitWidthAdjustChange(-100000);
	}
}
//==================================================================
public void penPressed(Point where)
//==================================================================
{
	startPoint = new Point(where.x,where.y);
	checkLimits();
}
//==================================================================
public void showPreferredSize(Control ex)
//==================================================================
{
	Dimension r = ex.getPreferredSize(null);
	Dimension d = ex.getSize(null);
	int change = r.width - d.width;
	if (type == HORIZONTAL) change = r.height-d.height;
	if (ex == after) change = -change;
	changeIt(change);
}
//==================================================================
public void resize(int toWhat)
//==================================================================
{
	checkLimits();
	if (toWhat == Minimize) changeIt(maxChange);
	else/* if (doMaximize) changeIt(minChange);
	else if (doPreferredSize) */
	showPreferredSize(after);
}
//==================================================================
public void penClicked(Point where)
//==================================================================
{
	boolean doOpen = false;
	if (!doCloseFirst) doOpen = state != OPENED;
	else doOpen = state == CLOSED;
	doOpenClose(doOpen);
/*
	if (doHide) {
		Dimension d = after.getSize(null);
		int ch = d.width;
		if (type == HORIZONTAL) ch = d.height;
		if (ch == 0) showPreferredSize(after);
		else changeIt(maxChange);
	}else if (doMaximize)
		if (maxChange == 0) changeIt(minChange);
		else changeIt(maxChange);
	else if (doPreferredSize) {
		Control ex = before;
		if (!expandBefore) ex = after;
		expandBefore = !expandBefore;
		showPreferredSize(ex);
	}
	*/
}
//int bm, am;

//==================================================================
public void startDragging(DragContext dc)
//==================================================================
{
	//bm = before.modify(Invisible,0);
	//am = after.modify(Invisible,0);

	Image got = new Image(width,height);
	Graphics g = new Graphics(got);
	int old = arrowPosition;
	arrowPosition = 0;
	repaintNow(g,null);
	arrowPosition = old;
	g.free();
	DragContext.ImageDragInWindow id = dc.startImageDrag(got,new Point(width/2,height/2),this);
	Rect r = Gui.getRectInWindow(parent,null,false);
	id.dragLimits = r;
	dc.imageDrag();
	/*
	Graphics g = dragImage.get(width,height,true);
	g.setColor(Color.Black);
	g.fillRect(0,0,width,height);
	myLoc = getPosInParent(getWindow());
	curLoc = new Point(myLoc.x,myLoc.y);
	draggedTo(dc.curPoint,false);
	*/
}
//==================================================================
public void stopDragging(DragContext dc)
//==================================================================
{
	dc.stopImageDrag();
	//before.restore(bm,Invisible);
	//after.restore(am,Invisible);
	DragContext.ImageDragInWindow id = dc.getImageDrag();
	((Image)id.image).free();
	if (type == HORIZONTAL)
		changeIt(id.imagePos.y-height/2);
	else
		changeIt(id.imagePos.x-width/2);
/*
	draggedTo(dc.curPoint,true);
	before.restore(bm,Invisible);
	after.restore(am,Invisible);
	screen.setDrawOp(screen.DRAW_OVER);
	screen.free();
	screen = null;
	if (type == HORIZONTAL) changeIt(curLoc.y-myLoc.y);
	else changeIt(curLoc.x-myLoc.x);
	*/
}
/*
//==================================================================
protected void draggedTo(Point where,boolean dropped)
//==================================================================
{
	if (screen == null) {
		screen = getWindow().getGraphics();//FIX mApp.appFrame.getGraphics();
		screen.setDrawOp(screen.DRAW_XOR);
	}else{
		drawNow();
	}
	int ch = where.y-startPoint.y;
	if (type == VERTICAL) ch = where.x-startPoint.x;
	if (ch < minChange) ch = minChange;
	if (ch > maxChange) ch = maxChange;
	if (type == VERTICAL) curLoc.x = myLoc.x+ch;
	else curLoc.y = myLoc.y+ch;
	if (!dropped) drawNow();
	else state = 0;
}
protected void drawNow()
//==================================================================
{
	screen.drawImage(dragImage.image,curLoc.x,curLoc.y);
}
*/
//==================================================================
public void dragged(DragContext dc)
//==================================================================
{
	dc.imageDrag();
	//draggedTo(dc.curPoint,false);
}
//##################################################################
}
//##################################################################

