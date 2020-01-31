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
package ewe.graphics;
import ewe.graphics.pagedisplay.*;
import ewe.ui.*;
import ewe.fx.*;

/**
* A PageDisplay displays a set of PageDisplayUnit objects. These represent any data that
* can be displayed within a rectangle, however, unlike Containers, no scroll bars or
* active controls can be displayed within the page, and no clipping is done to ensure the data stays
* within its bounds.
**/
//##################################################################
public class PageDisplay extends Canvas{
//##################################################################
public PageDisplayUnit display;

private Graphics g;
private Rect area;
private int dx;
private int dy;
private PageDisplayUnit unit;

{
	isFullScrollClient = true;
	PenEvent.wantPenMoved(this,PenEvent.WANT_PEN_MOVED_INSIDE,true);
	modify(WantHoldDown,0);
}
//public boolean canScreenScroll() {return false;}
//-------------------------------------------------------------------
void paintUnit()
//-------------------------------------------------------------------
{
	//ewe.sys.Vm.debug("Check: "+unit.y+", "+dy+" "+area.toString()+" "+unit.getClass());
	if (unit.x+dx >= area.x+area.width || unit.x+dx+unit.width <= area.x) return;
	if (unit.y+dy >= area.y+area.height || unit.y+dy+unit.height <= area.y) return;
	//ewe.sys.Vm.debug("Yes, paint: "+unit.x+", "+unit.y+" "+unit.getClass());
	PageDisplayUnit u = unit;
	u.doPaint(g);
	if (u.firstChild != null){
		dx += u.x;
		dy += u.y;
		g.translate(u.x,u.y);
		for (unit = u.firstChild; unit != null; unit = unit.nextSibling)
			paintUnit();
		g.translate(-u.x,-u.y);
		dx -= u.x;
		dy -= u.y;
	}
	unit = u;
}

ewe.sys.Lock paintLock = new ewe.sys.Lock();

//===================================================================
public void doPaint(Graphics g,Rect area)
//===================================================================
{
	if (paintLock.grab()) try{
		if (g == null) return;
		doBackground(g);
		if (display == null) return;

		dx = 0; dy = 0;//origin.x; dy = origin.y;
		g.translate(-origin.x,-origin.y);
		this.area = new Rect(area.x+origin.x,area.y+origin.y,area.width,area.height);
		this.g = g;
		unit = display;
		//ewe.sys.Vm.debug("---------------");
		paintUnit();
		//ewe.sys.Vm.debug(dx+","+dy+" "+origin.x+", "+origin.y);
		g.translate(origin.x,origin.y);

	}finally{
		paintLock.release();
	}
}
/**
 * Set the unit for the display. It will reset the origin and redisplay the new one.
 * @param unit The new unit.
 */
//===================================================================
public void setDisplay(PageDisplayUnit unit)
//===================================================================
{
	display = unit;
	unit.display = this;
	displayChanged();
	setOrigin(0,0,null);
	repaintNow();
}
/**
 * Call this if the "display" unit has changed somehow and the scroll bars need updating.
 */
//===================================================================
public void displayChanged()
//===================================================================
{
	virtualSize = display == null ? new Rect(0,0,0,0) : new Rect(0,0,display.width,display.height);
}
/**
* This will be called by default on a right-click. It will call getMenuFor(int x,int y) on the
* display units.
**/
//===================================================================
public boolean doMenu(Point where)
//===================================================================
{
	Menu m = display.getMenuFor(where.x+origin.x,where.y+origin.y);
	if (m != null){
		setMenu(m);
		menuState.outsideOfControl = false;
		return tryStartMenu(where);
	}
	return false;
}
/**
 * This is called when the user selects an item from the popup menu.
 * @param selectedItem The item selected.
 */
//===================================================================
public void popupMenuEvent(Object selectedItem)
//===================================================================
{
	if (clipItems == null) return;
	if (selectedItem == clipItems[0]) setClipObject(clipboardTransfer(getClipObject(),true,false));
	else if (selectedItem == clipItems[1]) setClipObject(clipboardTransfer(getClipObject(),true,true));
	else if (selectedItem == clipItems[2]) clipboardTransfer(getClipObject(),false,false);
	//else if (selectedItem == clipItems[3]) setClipObject(clipboardTransfer(getClipObject(),false,true));
	if (selectedItem == clipItems[1] || selectedItem == clipItems[2]) {
		repaintNow();
		notifyDataChange();
	}
}

//===================================================================
public PageDisplayUnit getHotUnit(int x,int y)
//===================================================================
{
	return display.getHotUnit(x,y);
}

//===================================================================
public boolean isHot(int x,int y)
//===================================================================
{
	return getHotUnit(x,y) != null;
}
boolean wasHot = false;
//===================================================================
public void onPenEvent(PenEvent ev)
//===================================================================
{
	if (ev.type == PenEvent.PEN_MOVE){
		if (isHot(ev.x+origin.x,ev.y+origin.y)) {
			if (!wasHot) setCursor(ewe.sys.Vm.HAND_CURSOR);
			wasHot = true;
		}else{
			if (wasHot) setCursor(0);
			wasHot = false;
		}
	}else
		super.onPenEvent(ev);
}
/**
 * This queries all the PageDisplayUnits to get any tool-tip for specified point.
 * @return An acceptable tool-tip, or null.
 */
//===================================================================
public Object getToolTip(int x,int y)
//===================================================================
{
	return display.getTipFor(x+origin.x,y+origin.y);
}
//===================================================================
public void popupMenuEvent(MenuEvent ev)
//===================================================================
{
	if (ev.selectedItem != null){
		PageDisplayUnit pdu = PageDisplayUnit.getTaggedUnit(ev.menu);
		if (pdu != null) pdu.menuItemSelected(ev.menu,ev.selectedItem);
	}
}
//===================================================================
public void refresh(int x,int y,int width,int height)
//===================================================================
{
	repaintNow(null,new Rect(x-origin.x,y-origin.y,width,height));
}
//===================================================================
public void penHeld(Point p)
//===================================================================
{
	doMenu(p);
}
//===================================================================
public void penPressed(Point p)
//===================================================================
{
	display.penPressed(p.x+origin.x,p.y+origin.y);
}
//===================================================================
public void penReleased(Point p)
//===================================================================
{
	display.penReleased(p.x+origin.x,p.y+origin.y);
}
//===================================================================
public void penClicked(Point p)
//===================================================================
{
	display.penClicked(p.x+origin.x,p.y+origin.y);
}
//===================================================================
public void penDoubleClicked(Point p)
//===================================================================
{
	if (!display.penDoubleClicked(p.x+origin.x,p.y+origin.y));
		display.penClicked(p.x+origin.x,p.y+origin.y);
}
/*
public static void main(String args[])
{
	ewe.sys.Vm.startEwe(args);
	Form f = new Form();
	f.title = "Page Display";
	PageDisplay pg = new PageDisplay();
	PageDisplayUnit top = new PageDisplayUnit();
	pg.setDisplay(top);
	PageDisplayUnit u = null;

	u = new ImageUnit(new Image("solitaire/Marble.bmp")){
		Image mm = new Image("solitaire/Marble.bmp");
		Image mk = new Image("solitaire/MarbleMask.bmp");
		{
			new ewe.sys.mThread(){
				public void run(){
					while(true){
						nap(250);
						setImage(mm);
						refresh();
						nap(250);
						setImage(mk);
						refresh();
					}
				}
			}.start();
		}
		protected Menu getDefaultMenuFor(int x,int y){
			return new Menu(new String[]{"Hello","There"},"Menu");
		}
		public void menuItemSelected(Menu m,Object selectedItem){
			ewe.sys.Vm.debug("Chose: "+selectedItem);
		}
//===================================================================
public boolean penDoubleClicked(int x,int y) {ewe.sys.Vm.debug("Ouch!"); return true;}
//===================================================================

	};
	u.x = 100; u.y = 200;
	top.addChild(u);
	u.flags |= u.IsHot;
	u.tip = "This image is red hot!";

	u = new PageDisplayUnit();
	u.x = u.y = 50;
	PageDisplayUnit i = new ImageUnit(new Image("solitaire/Marble.bmp"));
	u.addChild(i);
	i = new ImageUnit(new Image("solitaire/Marble.bmp"));
	i.x = 40; i.y = 30;
	u.addChild(i);
	top.addChild(u);

	top.calculateSize();
	top.height = 300;
	pg.displayChanged();
	f.addLast(new ScrollBarPanel(pg));
	f.setPreferredSize(300,300);
	f.execute();
	ewe.sys.Vm.exit(0);
}
*/
//##################################################################
}
//##################################################################

