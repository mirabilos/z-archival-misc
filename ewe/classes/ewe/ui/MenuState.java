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
/*
// header - edit "Data/yourJavaHeader" to customize
// contents - edit "EventHandlers/Java file/onCreate" to customize
//
*/
public class MenuState implements EventListener, PopupController
{
public Menu menu;
public Frame parent;
public CarrierFrame frame;
public Control control;
public boolean opened = false;
public boolean outsideOfControl = true;
public boolean isOpen() {return opened;}
public boolean autoSelectFirst = false;

public MenuState()
{
}
//-------------------------------------------------------------------
protected void showIt()
//-------------------------------------------------------------------
{
}
//===================================================================
public void doShowMenu(Point p)
//===================================================================
{
	doShowMenu(p,true,outsideOfControl ? control.getDim(null) : null);
/*
	ewe.sys.Vm.setSIP(0);
	opened = true;
	menu.selectedIndex = -1;
	menu.borderWidth = 0;
	menu.firstItem = 0;
	Rect r = control.getDim(null);
	Frame p = control.getFrame();
	frame = menu.getCarrierFrame(control,0,0,false,false,p);
	Point myLoc = Gui.getPosInParent(control,p);
	frame.putInFrame(myLoc.x,myLoc.y+r.height,myLoc.y-1);
	showIt();
*/
}
static Point where = new Point();

public void doShowMenu(Point point,boolean center,Rect exclusion)
{
	if (!control.checkMenu(menu)) return;
	where.set(point.x,point.y);
	//if (!control.hasModifier(control.KeepSIP,false)) ewe.sys.Vm.setSIP(0);
	if (exclusion == null) exclusion = new Rect(where.x,where.y,2,12);
	opened = true;
	menu.selectedIndex = -1;
	menu.borderWidth = 0;
	menu.firstItem = 0;
	menu.autoSelectFirst = autoSelectFirst;
	Rect r = control.getDim(null);
	Frame p = Gui.getPopupMenuParentFrame(control);
	menu.popupController = this;
	frame = menu.getCarrierFrame(control,0,0,false,false,p);
	Point myLoc = Gui.getPosInParent(control,p);
	Rect fd = frame.getDim(null);
	if (control != null)
		if ((control.getModifiers(false) & Control.MakeMenuAtLeastAsWide) != 0){
			control.getSize(Dimension.buff);
			if (fd.width < Dimension.buff.width) {
				fd.width = Dimension.buff.width;
				frame.resizeTo(fd.width,fd.height);
			}
		}
	int xp = where.x+myLoc.x;
	if (center) xp -= fd.width/2;
	int yp = where.y+myLoc.y;
	if (yp < exclusion.y+exclusion.height+myLoc.y)
		yp = exclusion.y+exclusion.height+myLoc.y;
	int of = frame.modify(Control.Invisible,0);
	int oc = control.modify(Control.Invisible,0);
	frame.putInFrame(xp,yp,exclusion.y+myLoc.y);
	Gui.takeFocus(menu,Control.ByRequest);
	//if (autoSelectFirst && menu.selectedIndex == -1)
		//menu.selectNext();
	menu.inheritModifiers(control,true,control.AlwaysEnabled|control.NotAnEditor|control.KeepImage,true);
	control.restore(oc,Control.Invisible);
	control.repaintNow();
	frame.modify(0,Control.Invisible);
	frame.repaintNow();
	frame.addListener(this);
	menu.addListener(this);
	Control.popupBeep();
	control.postEvent(new ControlEvent(ControlEvent.MENU_SHOWN,control));
	showIt();
}
protected ewe.sys.Lock lock = new ewe.sys.Lock();

//===================================================================
public void closePopup(int why, int flags)
//===================================================================
{
	closeMenu();
	control.postEvent(new ControlEvent(ControlEvent.POPUP_CLOSED,control));
}
//===================================================================
public void closeMenu()
//===================================================================
{
	if (!lock.grab()) return;
	else try{
		menu.noMenu();
		Gui.hideFrame(frame);
		control.deactivate();
		frame.removeListener(this);
		menu.removeListener(this);
		frame.popupController = null;
		frame = null;
		opened = false;
	}finally{
		lock.release();
	}
	//inPress = pressState = false;
	//control.repaintNow();
}
//------------------------------------------------------------------
public void onEvent(Event ev)
//------------------------------------------------------------------
{
	Control c = control;
	if ((ev.type == MenuEvent.SELECTED || ev.type == MenuEvent.ABORTED) && ev.target == menu){
		Object it = ((MenuEvent)ev).selectedItem;
		if (it instanceof MenuItem) c.lastSelected = (MenuItem)it;
		closeMenu();
		c.onEvent(ev); //This lets the control process it internally.
		ev.target = c;
		c.postEvent(ev); //This lets other controls process it.
		//MainWindow._mainWindow.setFocus(control);
		if (!c.hasModifier(c.NoFocus,false))
			Gui.takeFocus(c,control.ByRequest);
		c.popupMenuClosed(menu);
		c.postEvent(new ControlEvent(ControlEvent.POPUP_CLOSED,c));
	}else if (ev.type == FrameEvent.PRESSED_OUTSIDE && (ev instanceof FrameEvent)){
		//System.out.println("Outside!");
		Point onScreen = (Point)((FrameEvent)ev).data;
		if (true || !Gui.getAppRect(c).isInside(onScreen.x,onScreen.y)){
			closeMenu();
			MenuEvent me = new MenuEvent(MenuEvent.ABORTED,menu,null);
			c.onEvent(me);
			c.popupMenuClosed(menu);
			c.postEvent(new ControlEvent(ControlEvent.POPUP_CLOSED,control));
		}
	}
}

}
