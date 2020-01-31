/*********************************************************************************
 *  Ewe Virtual Machine - Version 1.14, January 24, 2002                         *
 *  Copyright (C) 1999-2002 Michael L Brereton <michael_brereton@ewesoft.com>    *
 *  Copyright (C) 1998, 1999 Wabasoft <www.wabasoft.com>                         *
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
import ewe.util.WeakSet;
import ewe.util.ObjectFinder;
import ewe.util.Tag;
import ewe.fx.*;
/**
 * PenEvent is a pen down, up, move or drag event.
 * <p>
 * A mouse drag occurs when a mouse button is pressed down and the
 * mouse is moved. A mouse move occurs when the mouse is moved without
 * a button being held down.
 */

public class PenEvent extends Event implements ObjectFinder
{
/** The event type for a pen or mouse down event. */
public static final int PEN_DOWN = 200;
/** The event type for a pen or mouse move event. */
public static final int PEN_MOVE = 201;
/** The event type for a pen or mouse up event. */
public static final int PEN_UP = 202;
/** The event type for a pen or mouse drag event. */
public static final int PEN_DRAG = 203;
/** The event type for a pen or mouse move on event. */
public static final int PEN_MOVED_ON = 204;
/** The event type for a pen or mouse move off event. */
public static final int PEN_MOVED_OFF = 205;

public static final int RIGHT_BUTTON = 0x8;
public static final int MIDDLE_BUTTON = 0x10;
public static final int FROM_OTHER_WINDOW = 0x20;
public static final int TRANSFERRED_PRESS = 0x40;

public static final int WANT_PEN_MOVE_TAG = 0x80000000;
public static final int WANT_PEN_MOVED_ONOFF = 0x1;
public static final int WANT_PEN_MOVED_INSIDE = 0x2;
public static final int WANT_PEN_MOVED_OUTSIDE = 0x4;
public static final int WHEN_NOT_ON_TOP_FRAME = 0x8;

public static final int SCROLL_UP = 206;
public static final int SCROLL_DOWN = 207;

//-------------------------------------------------------------------
static WeakSet moved = new WeakSet();
//-------------------------------------------------------------------

static SingleContainer theTip;

//-------------------------------------------------------------------
private static void makeTip()
//-------------------------------------------------------------------
{
	if (theTip != null) return;
	theTip = new SingleContainer();//MessageArea("Hello");
	theTip.borderWidth = 3;
	theTip.borderStyle = theTip.BDR_OUTLINE|theTip.BF_RECT;
	theTip.backGround = new ewe.fx.Color(255,255,200);
	theTip.foreGround = theTip.borderColor = Color.Black;
	theTip.modify(theTip.AlwaysRecalculateSizes,0);
}
//public static Control tipDisplayed = null;
public static ToolTip currentTip = new ToolTip();
public static Control lastTipFor = null;

static Control look = null;

/**
* This is the tool tip delay time in milliseconds. By default it is 500.
**/
public static int TipDelay = 500;

/**
*
**/
//===================================================================
public static boolean tipIsDisplayed() {return currentTip.forWho != null;}
//===================================================================

//===================================================================
public static boolean refreshTip(ISurface surface)
//===================================================================
{
	if (currentTip.forWho == null) return false;
	try{
		if (currentTip.window == surface)
			theTip.repaintNow();
	}catch(Exception e){}
	return true;
}
//===================================================================
public boolean lookingFor(Object what)
//===================================================================
{
	return what == look;
}

//-------------------------------------------------------------------
static Tag getListener(Control who,PenEvent ev)
//-------------------------------------------------------------------
{
	if (ev == null) ev = new PenEvent();
	look = who;
	Object got = moved.find(ev);
	if (got == null) return null;
	return ((Control)got).tags.get(WANT_PEN_MOVE_TAG,null);
}
//-------------------------------------------------------------------
protected static void setOptions(Control who,int toSet,int toClear)
//-------------------------------------------------------------------
{
	int opts = getOptionsFor(who);
	opts &= ~toClear;
	opts |= toSet;
	who.tags.set(WANT_PEN_MOVE_TAG,new ewe.sys.Long().set(opts));
}
//-------------------------------------------------------------------
static void setCursor(Control who,int cursor)
//-------------------------------------------------------------------
{
	wantPenMoved(who,WANT_PEN_MOVED_ONOFF,true);
	setOptions(who,cursor << 16,0xffff0000);
	if (lastMovedIn == who){
		ewe.sys.Vm.setCursor(who,cursor);
	}
}

/**
* It is necessary to call this method if a Control wishes to receive Pen/Mouse movement
* notifications.
* @param who The control requesting notifications.
* @param options any of the WANT_PEN_MOVED_XXX options ORed together.
* @param enable true to enable notification, false to disable them.
*/
//===================================================================
public static void wantPenMoved(Control who,int options,boolean enable)
//===================================================================
{
	if ((ewe.sys.Vm.getParameter(ewe.sys.Vm.VM_FLAGS) & ewe.sys.Vm.VM_FLAG_NO_MOUSE_POINTER) != 0) enable = false;
	if (moved == null) moved = new WeakSet();
	if (!enable) moved.remove(who);
	else moved.add(who);
	if (who.tags == null) who.tags = new ewe.util.TagList();
	setOptions(who,options,0);
}
//-------------------------------------------------------------------
static int getOptionsFor(Control who)
//-------------------------------------------------------------------
{
	if (who.tags == null) return 0;
	Tag tg = who.tags.get(WANT_PEN_MOVE_TAG,null);
	if (tg == null) return 0;
	return (int)((ewe.sys.Long)tg.value).value;
}
static Control lastMovedIn = null;

//-------------------------------------------------------------------
static boolean onScreen(Control who)
//-------------------------------------------------------------------
{
	for (Control c = who; !(c instanceof Window); c = c.parent)
		if (c == null) return false;
	return true;
}
//-------------------------------------------------------------------
static boolean isIn(PenEvent pe,Control who,int x,int y)
//-------------------------------------------------------------------
{
	if (pe.type == PEN_MOVED_OFF) return false;
	if (who.getWindow() != pe.window) return false;
	if ((getOptionsFor(who) & WHEN_NOT_ON_TOP_FRAME) == 0)
		if (!who.amOnTopFrame()) return false;
	ewe.fx.Rect r = Gui.getRectInWindow(who,ewe.fx.Rect.buff,true);
	if (r == null) return false;
	if (r.width < 1 || r.height < 1) return false;
	if  (!(x >= r.x && x < r.x+r.width) || !(y >= r.y && y < r.y+r.height)) return false;
	if (!(who instanceof Container)) return true;
	Control ch = ((Container)who).findChild(x,y);
	if (ch != who && ch != null){
		return false;
	}else
		return true;
}

//===================================================================
public static void resetCursor()
//===================================================================
{
	if (lastMovedIn == null) ewe.sys.Vm.setCursor(0);
	else{
		int opts = getOptionsFor(lastMovedIn);
		int cr = opts >> 16;
		if (cr != 0) {
			lastCursor = ewe.sys.Vm.setCursor(lastMovedIn,cr);
		}
	}
}
/**
* This is called by the VM when the WAIT_CURSOR has been removed.
**/
//===================================================================
public static void notWaiting()
//===================================================================
{
	resetCursor();
}
//===================================================================
public static void topFrameChanging()
//===================================================================
{
	if (lastMovedIn == null) return;
	int opts = getOptionsFor(lastMovedIn);
	int cr = opts >> 16;
	if (cr != 0) ewe.sys.Vm.setCursor(lastMovedIn,lastCursor);
	PenEvent pe = new PenEvent();
	pe.type = PEN_MOVED_OFF;
	pe.target = lastMovedIn;
	lastMovedIn = null;
	mApp.mainApp.dispatch(pe);
}

static int hoverTimer = 0, forgetLast = 0;
static Point lastPen = new Point();

//##################################################################
static class tipTicker implements ewe.sys.TimerProc{
//##################################################################

//===================================================================
public void ticked(int id,int howLate)
//===================================================================
{
	if (id == hoverTimer) showTip(false);
	else if (id == forgetLast) if (currentTip.forWho == null) lastTipFor = null;
}

//##################################################################
}
//##################################################################

public static boolean tipIsVisible = false;
//-------------------------------------------------------------------
static void showTip(boolean immediate)
//-------------------------------------------------------------------
{
	showTip(immediate,null);
}
//===================================================================
public static void showTip(boolean immediate,Object tooltip)
//===================================================================
{
	makeTip();
	if (lastMovedIn == null) return;
	Frame f = Gui.windowFrame(lastMovedIn);
	if (f == null) return;

	Point p = lastMovedIn.getPosInParent(f);
	Object t = tooltip;
	if (t == null) t = lastMovedIn.getToolTip(lastPen.x-p.x,lastPen.y-p.y);
	if (t == null) return;

	currentTip.forWho = lastMovedIn;
	if (t instanceof ToolTip){
		currentTip.persists = ((ToolTip)t).persists;
		t = currentTip.tip = ((ToolTip)t).tip;
	}else{
		Dimension dim = lastMovedIn.getSize(null);
		currentTip.persists = dim.height <= 32 || dim.width <= 32;
		currentTip.tip = t;
	}
	if (immediate & !currentTip.persists){
		currentTip.forWho = null;
		return;
	}
	if (t instanceof Control)
		theTip.setControl((Control)t);
	else if (t instanceof IImage)
		theTip.setControl(new ImageControl((IImage)t));
	else {
		Control c = new MessageArea(t.toString());
		theTip.setControl(c);
	}
	f.add(theTip);
	currentTip.window = f.getWindow();

	theTip.make(true);
	Dimension d = theTip.getPreferredSize(null);
	int y = lastPen.y+24;
	if (y+d.height > f.height) y = lastPen.y-d.height-2;
	if (y < 0) y = 0;
	int x = lastPen.x-d.width/2;
	if (x+d.width > f.width) x = f.width-d.width-2;
	if (x < 0) x = 0;
	theTip.setRect(x,y,d.width,d.height);
	theTip.repaintNow();
	theTip.shown();
	tipIsVisible = true;
}
static ewe.sys.TimerProc ticker = new tipTicker();
//===================================================================
static void removeTip()
//===================================================================
{
	tipIsVisible = false;
	if (currentTip.forWho != null){
		Frame f = Gui.windowFrame(theTip);
		if (f != null){
			Point p = Gui.getPosInParent(theTip,f);
			Rect r = new Rect(p.x,p.y,theTip.width,theTip.height);
			theTip.formClosing();
			f.remove(theTip);
			f.repaintNow(null,r);
		}
		lastTipFor = currentTip.forWho;
		currentTip.forWho = null;
	}else{
		lastTipFor = null;
	}
	if (hoverTimer != 0) mApp.mainApp.cancelTimer(hoverTimer);
	hoverTimer = 0;
}

//-------------------------------------------------------------------
private static void requestHover()
//-------------------------------------------------------------------
{
	if (hoverTimer != 0) {
		mApp.mainApp.cancelTimer(hoverTimer);
	}
	hoverTimer = mApp.mainApp.requestTick(ticker,TipDelay);
}
//-------------------------------------------------------------------
private static int lastCursor = 0;

//-------------------------------------------------------------------
static boolean handlePenMove(Window win,PenEvent pe,int x,int y)
//-------------------------------------------------------------------
{
	if (lastMovedIn != null){
		if (!isIn(pe,lastMovedIn,x,y)){
			removeTip();
			pe.type = PEN_MOVED_OFF;
			forgetLast = mApp.mainApp.requestTick(ticker,250);
			pe.target = lastMovedIn;
			pe.x = x; pe.y = y;
			int opts = getOptionsFor(lastMovedIn);
			//ewe.sys.Vm.debug("Setting off:"+lastCursor);
			ewe.sys.Vm.setCursor(lastMovedIn,lastCursor);
			lastMovedIn = null;
			win.dispatch(pe);
			handlePenMove(win,pe,x,y);
			return true;
		}
	}else{//This does PEN_MOVED_ON
		Object [] all = moved.getRefs();
		for (int i = 0; i<all.length; i++){
			if (all[i] == null) continue;
			Control c = (Control)all[i];
			int opts = getOptionsFor(c);
			if ((opts & WANT_PEN_MOVED_ONOFF) != 0){
				if (isIn(pe,c,x,y)){
					forgetLast = 0;
					lastMovedIn = c;
					lastPen.set(x,y);
					pe.type = PEN_MOVED_ON;
					pe.target = lastMovedIn;
					pe.x = x; pe.y = y;
					win.dispatch(pe);
					if (lastTipFor != null)
						if (lastTipFor.getParent() == c.getParent()) showTip(true);
						else requestHover();
					else requestHover();
					int cr = opts >> 16;
					if (cr != 0) {
						lastCursor = ewe.sys.Vm.setCursor(c,cr);
					}
					handlePenMove(win,pe,x,y);
					return true;
				}
			}
		}
	}
	// This does PEN_MOVE
	Object [] all = moved.getRefs();
		for (int i = 0; i<all.length; i++){
			if (all[i] == null) continue;
			Control c = (Control)all[i];
			if (!onScreen(c)) continue;
			int opts = getOptionsFor(c);
			boolean in = isIn(pe,c,x,y);
			if (in){
				if (currentTip.forWho != null && !currentTip.persists)
					removeTip();
				if (currentTip.forWho == null){
					requestHover();
					lastPen.set(x,y);
				}
			}
			if ((((opts & WANT_PEN_MOVED_INSIDE) != 0) && in) || ((opts & WANT_PEN_MOVED_OUTSIDE) != 0)){
				pe.type = PEN_MOVE;
				pe.target = c;
				pe.x = x; pe.y = y;
				win.dispatch(pe);
			}
		}
	return false;
}
/** The x location of the event. */
public int x;

/** The y location of the event. */
public int y;

/**
 * The state of the modifier keys when the event occured. This is a
 * OR'ed combination of the modifiers present in the IKeys interface.
 * @see IKeys
 */
public int modifiers;
}


