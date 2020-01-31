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
import ewe.sys.Vm;

//##################################################################
public class Menu extends ChoiceControl implements ScrollClient{
//##################################################################
{
	modify(AlwaysRecalculateSizes,0);
	text = "";
	useMenuItems = true;
	displayRows = 0;
}
public static boolean use3DPopup = true;
public static int border3D = EDGE_RAISED|BDR_OUTLINE;
protected Menu parentMenu = null;
public boolean followPen = true;
public PopupController popupController;
public static int defaultUseScrollBarSize = 10;
public static Color defaultBackground = Color.White;
/**
	If this is true then when the menu is first displayed, the first item will be selected.
**/
public boolean autoSelectFirst = false;
/**
* If this is true then when you press a key, each item will not be searched to see if the key
* relates to that item. By default this is false, except for SimpleList.
**/
public boolean dontSearchForKeys = false;
/**
* If this value is -1, then the defaultUseScrollBarSize will be used, if it is zero then scroll bars will
* never be used and if it is greater than zero it specifies how many items required to switch from up/down
* button scrolling to using a full scroll bar.
**/
public int useScrollBarSize = -1;
//------------------------------------------------------------------
protected int getMenuWidth() {return getMenuWidth(false,true);}
//===================================================================
public Menu() {}
//===================================================================
/**
 * Create a Menu that is a copy of the other menu.
 * @param other The other menu
 */
//===================================================================
public Menu(Menu other)
//===================================================================
{
	text = other.text;
	int s = other.items.size();
	for (int i = 0; i<s; i++)
		items.add(other.items.get(i));
}
//===================================================================
public Menu(String [] items,String title)
//===================================================================
{
	super();
	text = title;
	addItems(items);
}
//===================================================================
public Menu(MenuItem [] items,String title)
//===================================================================
{
	super();
	text = title;
	this.items.addAll(items);
}
//===================================================================
public Rect getTextRect(int idx,Rect dest)
//===================================================================
{
	int xx = xOffset-xShift;
	if (xx < 0) xx = 0;
	Rect r = getItemRect(idx,dest);
	r.width -= spacing*2+xx;
	r.x += spacing+xx;
	return r;
}
//-------------------------------------------------------------------
protected int whichItem(int x,int y)
//-------------------------------------------------------------------
{
	int h = getItemHeight();//+spacing;
	if (y < spacing) return -1;
	int it = (y-spacing)/h;
	return it;
}
//===================================================================
public Rect getItemRect(int idx,Rect dest)
//===================================================================
{
	int h = getItemHeight();
	int y = (idx-firstItem)*h+spacing;
	return Rect.unNull(dest).set(0,y,width,h);
}
//==================================================================
private void moveSelected(int dir)
//==================================================================
{
	for (int s = selectedIndex+dir; s<itemsSize(); s+=dir){
		MenuItem mi = getItemAt(s);
		if ((mi.modifiers & (mi.Disabled|mi.Separator)) != 0) continue;
		selectedIndex = s;
		return;
	}
}
//==================================================================
protected boolean allowNotOnMeSelection() {return true;}
//==================================================================
protected void selectNewItem(Point p)
//==================================================================
{
	int flags = getModifiers(true);
	int old = selectedIndex;
	int it = whichItem(p.x,p.y);
	if ((it < 0 || it >= getScreenRows()+1) && !allowNotOnMeSelection()) return;
	if (it < 0 && selectedIndex > 0) moveSelected(-1);
	else if (it >= getScreenRows()+1 && selectedIndex<itemsSize()-1) moveSelected(1);
	else if (it >= 0 && it < getScreenRows()+1) selectedIndex = firstItem+it;
	if (selectedIndex < 0 || selectedIndex >= itemsSize()) selectedIndex = old;
	if (old == selectedIndex) return;
	MenuItem mi = getItemAt(selectedIndex);
	if ((mi.modifiers & (mi.Disabled|mi.Separator)) != 0) {
		selectedIndex = old;
		return;
	}else if (mi.subMenu == null && !(((flags & (NotEditable|DisplayOnly)) == 0) || ((flags & NotAnEditor) != 0))){
		selectedIndex = old;
		return;
	}
	int last = firstItem+getScreenRows()-1;
	int lastFirst = firstItem;
	if (selectedIndex < firstItem) firstItem = selectedIndex;
	if (selectedIndex > last) firstItem += selectedIndex-last;
	if (firstItem != lastFirst) repaintDataNow();
	pressedNewSelected(old);
}
//==================================================================
protected void pressedNewSelected(int oldSel)
//==================================================================
{
	noMenu();
	selectOrUnselect(oldSel);
	selectOrUnselect(selectedIndex);
	if (ss != null) ss.updateScroll(IScroll.Vertical);
	//repaintNow();
	MenuItem mi = getItemAt(selectedIndex);
	if (mi.subMenu != null){
		doDropMenu(mi.subMenu);
	}
}
protected int pressedItem;
protected boolean wasSelected = false;
//==================================================================
public void penPressed(Point p)
//==================================================================
{
	if (menuIsActive()) menuState.closeMenu();
	if ((currentPenEvent.modifiers & PenEvent.TRANSFERRED_PRESS) != 0) {
		return;
	}
	pressedItem = whichItem(p.x,p.y);
	if (pressedItem != -1){
		pressedItem += firstItem;
		if (pressedItem > itemsSize()) pressedItem = -1;
		wasSelected = (pressedItem != -1 && isSelected(pressedItem));
	}
	selectNewItem(p);
}
//==================================================================
public void dragged(DragContext dc)
//==================================================================
{
	if (menuIsActive()) return;
	if (!isOnMe(dc.curPoint) && (menuFrame != null)) return;
	selectNewItem(dc.curPoint);
}
//===================================================================
public void onPenEvent(PenEvent ev)
//===================================================================
{
	if (ev.type == ev.PEN_MOVE){
		if (!followPen || needScrollBar(IScroll.Vertical,height)) return;
		//if (menuIsActive()) return;
		Point p = new Point(ev.x,ev.y);
		if (!isOnMe(p)) return;
		selectNewItem(p);
		return;
	}
	super.onPenEvent(ev);
}
//==================================================================
public void penReleased(Point p)
//==================================================================
{
	if (menuIsActive()) return;
	if (!isOnMe(p)) return;
	int which = whichItem(p.x,p.y);
	if (which == -1) return;
	//selectNewItem(p);
	//repaintNow();
	//===================================================================
	if (selectedIndex != -1 && which+firstItem == selectedIndex){
	//===================================================================
		MenuItem mi = getItemAt(selectedIndex);
		if (mi.subMenu == null) notifyAction();
	}
}
/**
* If this is true then the same Frame will be used for the Menu everytime it is shown. By default this
* is true. Set it to false if you intend to modify the contents of the menu and so may need a new Frame
* each time it is displayed.
**/
public boolean keepFrame = true;
protected CarrierFrame myFrame;

//==================================================================
public void onKeyEvent(KeyEvent ev)
//==================================================================
{
	if (ev.type != ev.KEY_PRESS) return;
	int sz = itemsSize();
	if (ev.key == IKeys.UP/* || ev.key == IKeys.BACKSPACE*/){
		selectPrev();
	}else if (ev.key == IKeys.RIGHT){
		if (!trySubMenu())
			postEvent(new MenuEvent(MenuEvent.NEXT_MENU_RIGHT,this,null));
	}else if (ev.key == IKeys.LEFT){
		if (parentMenu == null)
			postEvent(new MenuEvent(MenuEvent.NEXT_MENU_LEFT,this,null));
		else
			parentMenu.noMenu();
	}else if (ev.key == IKeys.DOWN){
		selectNext();
	}else if (ev.isCancelKey() || ev.isBackKey() ||(ev.key == IKeys.MENU && !isAList)){
		postEvent(new MenuEvent(MenuEvent.ABORTED,this,null));
	}else if (ev.key == IKeys.MENU){
		int item = selectedIndex;
		if (item != -1){
			Rect r = getItemRect(item,null);
			if (r != null)
				if (doMenu(new Point(r.x,r.y)))
					return;
		}
		super.onKeyEvent(ev);
	}else{
		if (!dontSearchForKeys){
			for (int i = 0; i<sz; i++){
				MenuItem m = getItemAt(i);
				if (ev.isHotKey(m.hotkey)){
					select(i);
					makeItemVisible(i);
					ev.consumed = true;
					if (m.subMenu == null)
						notifyAction();
					else
						trySubMenu();
					return;
				}
			}
			if ((ev.modifiers & IKeys.SPECIAL) == 0){
				int ky = findKeyed(selectedIndex+1,ev.key);
				if (ky != -1){
					select(ky);
					makeItemVisible(ky);
					ev.consumed = true;
					if (isAList)
						notifySelection(selectedIndex,ListEvent.SELECTED);
					return;
				}
			}
		}
		super.onKeyEvent(ev);
	}
	ev.consumed = true;
}
//===================================================================
public boolean trySubMenu()
//===================================================================
{
	if (selectedIndex == -1) return false;
	MenuItem mi = getItemAt(selectedIndex);
	if (mi.subMenu != null){
		return doDropMenu(mi.subMenu);
	}
	return false;
}
//===================================================================
public void doActionKey(int key)
//===================================================================
{
	if ((getModifiers(true) & (DisplayOnly|NotEditable)) != 0) return;
	if (selectedIndex == -1) return;
	if (trySubMenu()) return;
	doAction(ByKeyboard);
	notifyAction();
}
//==================================================================
public CarrierFrame getCarrierFrame(Control owner,int maxWidth,int maxHeight,boolean shrinkWidth,boolean shrinkHeight,Frame parent)
//==================================================================
{
	return getCarrierFrame(owner,maxWidth,maxHeight,shrinkWidth,shrinkHeight,parent,null);
}
//==================================================================
public CarrierFrame getCarrierFrame(Control owner,int maxWidth,int maxHeight,boolean shrinkWidth,boolean shrinkHeight,Frame parent,String title)
//==================================================================
{
	if (autoSelectFirst && selectedIndex == -1) selectNext();
	PenEvent.wantPenMoved(this,PenEvent.WANT_PEN_MOVED_INSIDE|PenEvent.WHEN_NOT_ON_TOP_FRAME,true);
	int is = itemsSize();
	//if (displayRows <= 0) displayRows = is;
	//else if (displayRows > is) displayRows = is;
	//displayRows = itemsSize();
	CarrierFrame f = myFrame;
	boolean sb = false;
	int usb = useScrollBarSize;
	if (usb < 0) usb = defaultUseScrollBarSize;
	if ((is > usb) && (usb != 0)) sb = true;
	if (f == null || !keepFrame) {
		myFrame = f = new ScrollableCarrierFrame(this,owner,sb,title);
		f.addListener(this);
		//f.isPopup = true;
		f.borderWidth = 1;
		f.name = "Menu Frame: "+text;
		f.backGround = backGround = defaultBackground;
		if ((owner instanceof Menu) && !(owner instanceof BasicList)){
			f.borderWidth = owner.getFrame().borderWidth;
			f.borderStyle = owner.getFrame().borderStyle;
			f.backGround = backGround = owner.backGround;
			spacing = ((Menu)owner).spacing;
		}else if (Menu.use3DPopup && !(owner instanceof mChoice) && !(owner instanceof mComboBox)){
			f.borderWidth = 4;
			f.borderStyle = border3D;
			f.backGround = backGround = Color.LightGray;
			spacing = 0;
		}
	}
	if (f instanceof ScrollableCarrierFrame)
		((ScrollableCarrierFrame)f).allowTitleExpansion = is <= 5;
	f.popupController = popupController;
	f.modify(KeepSIP,0);
	Gui.execFrame(f,parent,Gui.PUTTING_POPUP);
	//ewe.sys.Vm.debug("Got: "+maxWidth+", "+maxHeight+", "+shrinkWidth+", "+shrinkHeight);
	f.fitClientAndFrame(maxWidth,maxHeight,shrinkWidth,shrinkHeight);
	return f;
}
/*
//==================================================================
protected Object getItemToAdd(Object what)
//==================================================================
{
	Object m = super.getItemToAdd(what);
	if (m instanceof MenuItem) return m;
	if (m instanceof String) {
		MenuItem mi = new MenuItem();
		mi.label = (String)m;
		return mi;
	}
	return null;
}
*/

Menu menu;
CarrierFrame menuFrame;

/*
static int lastOp = -1;
static boolean stopped = false;
private static void test(int which)
{
	if (stopped) return;
	if (which == lastOp) {
		new Exception("Two "+(which == 1 ? "Drops!" : "Hides!")).printStackTrace();
		stopped = true;
	}
	else ewe.sys.Vm.debug((which == 1 ? "Drop." : "Hide."));
	lastOp = which;
}
*/
ewe.sys.Lock dropLock = new ewe.sys.Lock();

//------------------------------------------------------------------
protected boolean doDropMenu(Menu who)
//------------------------------------------------------------------
{
	//test(1);
	if (!dropLock.grab()) return false;
	try{
		menu = who;
		menu.popupController = getFrame().popupController;
		menu.parentMenu = this;
		menu.autoSelectFirst = autoSelectFirst;
		menu.inheritModifiers(this,true,AlwaysEnabled|NotAnEditor|KeepImage,true);
		menu.selectedIndex = -1;
		menu.borderWidth = 0;
		menu.firstItem = 0;
		menu.shortenItems = shortenItems;
		menu.useScrollBarSize = useScrollBarSize;
		Rect r = getDim(null);
		Frame p = getFrame().getFrame();
		menuFrame = menu.getCarrierFrame(this,0,0,false,false,p);
		Rect frameSize = menuFrame.getDim(null);
		Point myLoc = Gui.getPosInParent(this,p);
		myLoc.y += (selectedIndex-firstItem)*getItemHeight()+spacing;
		int xpos = myLoc.x+r.width;
		int parentWidth = p.getDim(null).width;
		int leftSpace = myLoc.x;
		int rightSpace = parentWidth-myLoc.x-r.width;
		if ((rightSpace < frameSize.width) && (leftSpace >= frameSize.width))
			xpos = myLoc.x-frameSize.width;
		menuFrame.putInFrame(xpos,myLoc.y);//+r.height);
		//repaintNow();
		menuFrame.repaintNow();
		menuFrame.addListener(this);
		menu.addListener(this);
		Gui.takeFocus(menu,ByRequest);
	}finally{
		dropLock.release();
	}
	return true;
}
//==================================================================
public void postEvent(Event ev)
//==================================================================
{
	if (ev.type == ControlEvent.PRESSED && ev.target == this) {
		Object it = getSelectedItem();
		ev = new MenuEvent(MenuEvent.SELECTED,this,it);
		if (it instanceof MenuItem) lastSelected = (MenuItem)it;
	}
	super.postEvent(ev);
	if (ev instanceof MenuEvent) notifyDataChange();
}
//==================================================================
public void postEventNormally(Event ev)
//==================================================================
{
	super.postEvent(ev);
}
//------------------------------------------------------------------
protected void noMenu()
//------------------------------------------------------------------
{
	if (!dropLock.grab()) return;
	try{
		if (menuFrame != null) {
			if (menu != null) menu.noMenu();
			Gui.hideFrame(menuFrame);
		}
		menuFrame = null;
		menu = null;
	}finally{
		dropLock.release();
	}
}
//------------------------------------------------------------------
public void onEvent(Event ev)
//------------------------------------------------------------------
{
	if ((ev.type == MenuEvent.SELECTED || ev.type == MenuEvent.ABORTED) && ev.target == menu){
		Object it = ((MenuEvent)ev).selectedItem;
		if (it instanceof MenuItem) lastSelected = (MenuItem)it;
		noMenu();
		ev.target = this;
		//if (ev.type != MenuEvent.ABORTED)
		postEvent(ev);
		//postEvent
	}else if ((ev.type == MenuEvent.NEXT_MENU_RIGHT || ev.type == MenuEvent.NEXT_MENU_LEFT) && (ev.target == menu)){
		postEvent(new MenuEvent(ev.type,this,null));
	}else if (getFrame() != ev.target && ev.type == FrameEvent.PRESSED_OUTSIDE && (ev instanceof FrameEvent)){
		Point onScreen = (Point)((FrameEvent)ev).data;
		noMenu();
		if (!Gui.getAppRect(this).isInside(onScreen.x,onScreen.y))
			getFrame().pressedOutside(onScreen);
		else {
			int oldSel = selectedIndex;
			selectedIndex = -1;
			selectOrUnselect(oldSel);
		}
	}else if (ev.type == FrameEvent.CLOSED && (ev instanceof FrameEvent)){
		if (!keepFrame) {
			myFrame.dismantle(this);
			myFrame = null;
		}
	}else if (ev.target != menu)
		super.onEvent(ev);
}

//-------------------------------------------------------------------
protected boolean canScreenScroll()
//-------------------------------------------------------------------
{
	return true;
}
//-------------------------------------------------------------------
boolean vscroll(int was,int now)
//-------------------------------------------------------------------
{
	if (was == now) return false;
	if (!canScreenScroll()) return true;
	else{
		int dx = now > was ? 1 : -1, difference = 0;
		int h = getItemHeight();
		difference = (now-was)*dx;
		difference *= h;
		if (difference < 0) return true;
		Rect r = new Rect(spacing,spacing,width-spacing*2,height-spacing*2);
		if (r.height > difference){
			if (now > was)
				scrollAndRepaint(r.x,r.y+difference,r.width,r.height-difference,r.x,r.y);
			else
				scrollAndRepaint(r.x,r.y,r.width,r.height-difference,r.x,r.y+difference);
			return false;
		}else
			return true;
	}
}

//==================================================================
public void doScroll(int which,int action,int value)
//==================================================================
{
	if (which == IScroll.Vertical) {
		int was = firstItem;
		if (action == IScroll.ScrollHigher) firstItem++;
		else if (action == IScroll.ScrollLower) firstItem--;
		else if (action == IScroll.PageHigher) firstItem += getScreenRows();
		else if (action == IScroll.PageLower) firstItem -= getScreenRows();
		else if (action == IScroll.TrackTo) firstItem = value;
		if (firstItem > itemsSize()-getScreenRows()) firstItem = itemsSize()-getScreenRows();
		if (firstItem < 0) firstItem = 0;
		if (vscroll(was,firstItem)) repaintDataNow();
		if (ss != null) ss.updateScroll(which);
		return;
	}else {
		if (action == IScroll.ScrollHigher) xShift += 5;
		else if (action == IScroll.ScrollLower) xShift -= 5;
		else if (action == IScroll.PageHigher) xShift += width-5;
		else if (action == IScroll.PageLower) xShift -= width-5;
		else if (action == IScroll.TrackTo) xShift = value;
		if (mw == 0) mw = getMenuWidth();
		if (xShift+width > mw) xShift = mw-width;
		if (xShift < 0) xShift = 0;
	}
	repaintDataNow();
	if (ss != null) ss.updateScroll(which);
}
//public void setServer(ScrollServer server) {ss = server;}
public int getActual(int which)
{
	if (which == IScroll.Vertical) return itemsSize();
	else {
		if (mw == 0) mw = getMenuWidth();
		return mw;
	}
}
public int getVisible(int which,int forSize)
{
	if (which == IScroll.Vertical) {
		int h = getItemHeight();
		if (h != 0) return (forSize-spacing*2)/h;
		else return 5;
	}
	else return forSize;
}
public int getCurrent(int which)
{
	if (which == IScroll.Vertical) return firstItem;
	else return xShift;
}
public boolean needScrollBar(int which,int forSize)
{
	return getVisible(which,forSize) < getActual(which);
}
public boolean canGo(int orientation,int direction,int position)
{
	if (orientation == Vertical){
		if (direction == Higher){
			return (position < itemsSize()-getScreenRows());
		}else if (direction == Lower){
			//if (position <= 0) ((Object)null).toString();
			return position > 0;
		}
	}
	return true;
}
//ScrollServer ss;
int mw = 0;
//==================================================================
public void updateItems()
//==================================================================
{
	mw = getMenuWidth();
	if (ss != null) {
		ss.checkScrolls();
		//ss.updateScroll(Vertical);
		//ss.updateScroll(Horizontal);
	}
	super.updateItems();
}



public boolean doMenu(Point p)
{
	if (getMenu() != null && !menuIsActive()){
		int it = whichItem(p.x,p.y);
		if (it < 0 || it >= getScreenRows()+1) return false;
		menuState.doShowMenu(p,true,getItemRect(it+firstItem,null));
		return true;
	}
	return false;
}

//===================================================================
public void exec(Control owner,Point where,EventListener listener)
//===================================================================
{
	Frame p = owner.getWindow().contents;
	if (listener != null) addListener(listener);
	where = new Point(where.x,where.y);
	Point pp = Gui.getPosInParent(owner,p);
	CarrierFrame frame = getCarrierFrame(owner,0,0,false,false,p);
	frame.putInFrame(where.x+pp.x,where.y+pp.y,0);
	Gui.takeFocus(this,Control.ByRequest);
	frame.repaintNow();
}
//===================================================================
public void close()
//===================================================================
{
	Gui.hideFrame(getFrame());
}
//##################################################################
}
//##################################################################


