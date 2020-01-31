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
import ewe.io.File;
import ewe.io.PrintWriter;
import ewe.util.*;
import ewe.sys.Lock;
import ewe.sys.Time;
import ewe.sys.Vm;

//##################################################################
public class Gui implements UIConstants{
//##################################################################

/**
* If you show/exec a Frame with a null parent frame then if NullParentFrameToMainApp
* is true, it will use the main frame of the mApp of the program. Otherwise it will
* show the Frame in a new window. By default it is true.
**/
public static boolean NullParentFrameToMainApp = true;

//public static Control newButton(String text) {return new mButton(text);}
public static Rect fullScreen = new Rect();
//===================================================================
public static Rect getSize(FontMetrics fm,String st,int xGap,int yGap)
//===================================================================
{
	Rect r = new Rect(0,0,0,0);
	r.width = fm.getTextWidth(st)+xGap*2;
	r.height = fm.getHeight()+yGap*2;
	return r;
}
//===================================================================
public static Rect getSize(FontMetrics fm,String [] lines,int xGap,int yGap)
//===================================================================
{
	int w = 0, h = 0;
	int fh = fm.getHeight(), leading = fm.getLeading();
	for (int i = 0; i<lines.length; i++){
		int wd = fm.getTextWidth(lines[i]);
		if (wd > w) w = wd;
		if (i != 0) h += leading;
		h += fh;
	}
	h += yGap*2;
	w += xGap*2;
	return new Rect(0,0,w,h);
}
//===================================================================
public static Rect getAverageSize(FontMetrics fm,int rows,int columns,int xGap,int yGap)
//===================================================================
{
	Rect r = new Rect(0,0,0,0);
	r.width = fm.getCharWidth('0')*columns+xGap*2;
	r.height = fm.getHeight()*rows;
	if (rows != 1) r.height += fm.getLeading()*(rows-1);
	r.height += yGap*2;
	return r;
}
//===================================================================
public static Point centerText(FontMetrics fm,String st,int width,int height)
//===================================================================
{
	Rect r = getSize(fm,st,0,0);
	return new Point((width-r.width)/2,(height-r.height)/2);
}
//private static Vector frames = new Vector();

//==================================================================
public static Frame topFrame(Control who)
//==================================================================
{
	if (who == null) return null;
	Window w = (who instanceof Window) ? (Window)who : who.getWindow();
	if (w == null) return null;
	if (w.frames.size() == 0) return null;
	return (Frame)w.frames.get(w.frames.size()-1);
}
/**
* This returns the very top frame in a window.
**/
//===================================================================
public static Frame windowFrame(Control who)
//===================================================================
{
	if (who == null) return mApp.mainApp.contents;
	Window w = who.getWindow();
	if (w == null) return null;
	return w.contents;
}
/**
* This returns the area on the Window of the control that is not obscured by the SIP. This is relative to the
* client area of the Window. If no area is visible it will return null;
**/
//===================================================================
public static Rect visibleWindowClientArea(Control who)
//===================================================================
{
	Window w = who.getWindow();
	if (w == null) return null;
	Rect r = w.getWindowRect(new Rect(),false);
	int x = r.x, y = r.y;
	Rect rc = w.getWindowRect(new Rect(),true);
	int cx = rc.x, cy = rc.y;
	rc.x += x; rc.y += y;
	if (Window.visibleWidth != 0){
		r.set(0,0,Window.visibleWidth,Window.visibleHeight);
		rc.getIntersection(r,rc);
	}
	rc.x -= (x+cx); rc.y -= (y+cy);
	if (rc.width <= 0 || rc.height <= 0) return null;
	return rc;
}
//-------------------------------------------------------------------
static void sendFrameEvent(Control c,int which)
//-------------------------------------------------------------------
{
	if (!(c instanceof Frame)) return;
	c.postEvent(new FrameEvent(which,c));
}

/**
Set this true if you wish to send FrameEvent.NOT_ON_TOP and FrameEvent.NOW_ON_TOP
events.
**/
public static boolean sendFrameOnTopEvents = false;

//-------------------------------------------------------------------
static void notifyTop(Window w,boolean onTop)
//-------------------------------------------------------------------
{
	int which = onTop ? FrameEvent.NOW_ON_TOP : FrameEvent.NOT_ON_TOP;
	if (w == null) return;
	if (w.frames.size() != 0){
 		Control c = (Control)w.frames.get(w.frames.size()-1);
		if (c instanceof FormFrame){
			Form ff = ((FormFrame)c).myForm;
			if (ff.windowTitle != null)
				w.setTitle(ff.windowTitle);
		}
		if (sendFrameOnTopEvents) sendFrameEvent(c,which);
	}else{
		if (sendFrameOnTopEvents)
			for (Iterator it = w.contents.getChildren(); it.hasNext();)
				sendFrameEvent((Control)it.next(),which);
	}
}
//===================================================================
public static void frameOnTop(Control c)
//===================================================================
{
	Frame f = c.getFrame();
	Window w = c.getWindow();
	if (f != null && w != null) {
		PenEvent.topFrameChanging();
		Frame top = topFrame(c);
		notifyTop(w,false);
		w.frames.remove(f);
		w.frames.add(f);
		notifyTop(w,true);
	}
}
//==================================================================
public static Control focusedControl()
//==================================================================
{
	if (inFocus.size() == 0) return null;
	return (Control)inFocus.get(inFocus.size()-1);
}
//-------------------------------------------------------------------
protected static void doExecFrame(Frame f,Container parent,int options)
//-------------------------------------------------------------------
{
	Window w = parent.getWindow();
	Vector frames = w.frames;
	PenEvent.topFrameChanging();
	if (frames.find(f) != -1) return;
	f.doSaveScreen = true;
	Control c = focusedControl();
	if (c != null) c.lostFocus(ByRequest);
	//inFocus.add(null);
	notifyTop(w,false);
	frames.add(f);
	notifyTop(w,true);
}
//==================================================================
public static void hideFrame(Frame f)
//==================================================================
{
	//System.out.println("Hiding:"+f);
	//System.out.println("1");
	try{
	//ewe.sys.Vm.debug("Hiding: "+inFocus.toString());
	if (mApp.mainApp._focus != null)
		if (mApp.mainApp._focus.isChildOf(f))
			mApp.mainApp._focus = null;
	Window w = f.getWindow();
	if (w == null) return;
	Vector frames = w.frames;
	if (f == null) {
		//new Exception().printStackTrace();
		return;
	}
	if (f.parentFrame != null) f.parentFrame.removeChildFrame(f);
	Rect r = getAppRect(f);
	if (r == null) return;
	if (frames.find(f) != -1) {
		//System.out.println("2");
		notifyTop(w,false);
		frames.remove(f);
		if (frames.size() != 0);
		notifyTop(w,true);
		int sz = inFocus.size();
		if (sz != 0)  {
			sz--;
			Control old = (Control)inFocus.get(sz);
			inFocus.del(sz);
			sz--;
			boolean tookOff = false;
			if (sz >= 0){
				Control c = (Control)inFocus.get(sz);
				if (c != null) {
					if (old != null) takeFocusOff(old,ByFrameChange,c);
					//ewe.sys.Vm.debug("Returning to: "+c.getClass());
					if (c.hasModifier(c.ShowSIP,false) && !f.isPopup() && c.canEdit()) changeSip(1);
					else if (!c.hasModifier(c.KeepSIP,false) && !(f instanceof CarrierFrame) && !Frame.hasSipResized(w)) changeSip(0);
					setFocusOn(c,ByFrameChange,old);
				}
			}
			if (!tookOff && old != null) takeFocusOff(old,ByFrameChange,null);
		}
	}
	Container c = f.getParent();
	if (c != null) {
		removeFrom(c,f);
		if (f.controlsToRefresh != null)
			for (int i = 0; i<f.controlsToRefresh.size(); i++)
				((Control)f.controlsToRefresh.get(i)).repaintNow();
		//c.repaint();
	}
	if (f.savedScreen != null) {
		f.savedScreen.restore();
		f.eraseSavedScreen();
	}
	else refreshScreen(w,r);
	f.postEvent(new FrameEvent(FrameEvent.CLOSED,f));
	if (f.closeWindow) {
		w.close();
	}else{
		if (f.isModal) w.closeModal();
	}
	f.hidden();
	}finally{
		//ewe.sys.Vm.debug(""+f.listeners+", "+f.getParent()+", "+f.parentFrame);
		//ewe.sys.Vm.debug("Hidden: "+inFocus.toString());
	}
}
//==================================================================
public static SavedScreen saveScreen(Window w,Rect area,boolean doCapture)
//==================================================================
{
//Disabled this because it messes with the SIP
	if (true || mApp.platform.equals("Java") || mApp.platform.equals("PalmOS")) return new SavedScreen(w,area,false);
	else return new SavedScreen(w,area,doCapture);
}
/*
//==================================================================
public static void error(String what)
//==================================================================
{
	new mWaba.gui.control.MessageBox("Error",what,0).exec(null,null);
}
*/
/**
* This gets the rect of the control relative to the top left of the controls
* containing window. If it is not displayabled, then null will be returned. The
* rect returned may not be the full size of the control as it may be cut off by
* its parents.
**/
//===================================================================
public static Rect getRectInWindow(Control ct,Rect dest,boolean onlyIfVisible)
//===================================================================
{
	return getRectInWindow(ct,dest,onlyIfVisible,null);
}
/**
* This gets the rect of the control relative to the top left of the controls
* containing window. If it is not displayabled, then null will be returned. The
* rect returned may not be the full size of the control as it may be cut off by
* its parents.
**/
//===================================================================
public static Rect getRectInWindow(Control ct,Rect dest,boolean onlyIfVisible,Point actualPosition)
//===================================================================
{
	if (ct == null) return null;
	if (actualPosition != null) actualPosition.set(ct.x,ct.y);
	dest = Rect.unNull(dest);
	Rect par = new Rect();
	dest.set(ct.x,ct.y,ct.width,ct.height);
	if (ct.width < 1 || ct.height < 1) return null;
	if (onlyIfVisible)
		if ((ct.modifiers & Invisible) != 0)
			return null;
	Control c = ct;
	for (c = c.getParent(); c != null; c = c.getParent()){
		if (onlyIfVisible)
			if ((c.modifiers & Invisible) != 0)
				return null;
		c.getDim(par);
		par.getIntersection(dest,dest);
		if (dest.width < 1 || dest.height < 1) return null;
	 	dest.x += c.x;
	  dest.y += c.y;
		if (actualPosition != null) {
			actualPosition.x += c.x;
			actualPosition.y += c.y;
		}
	 	if (c instanceof Window){
			if (!((Window)c).firstDisplay){//canDisplay) {
				return null;
			}else{
				break;
			}
	 	}
	}
	//if (c == null && ct instanceof mTextPad) new Exception("Not in window!").printStackTrace();
	return dest;
}

/**
* This gets the rect of the control relative to the top left of the controls
* containing window.
**/
//===================================================================
public static Rect getAppRect(Control ct,Rect dest)
//===================================================================
{
	Rect r = dest;
	if (r == null) r = new Rect();
	if (ct == null) return r;
	r.set(ct.x,ct.y,ct.width,ct.height);
	Control c = ct;
	for (c = c.getParent(); c != null; c = c.getParent()){
		r.x += c.x; r.y += c.y;
	}
	return r;
}
//==================================================================
public static Rect getAppRect(Control ct)
//==================================================================
{
	return getAppRect(ct,null);
}

/**
*
**/
//===================================================================
public static void captureControl(Control ct,Graphics dest,Rect dim)
//===================================================================
{
	int x = 0, y = 0, w = dim.width, h = dim.height;
	int ox = dim.x, oy = dim.y;
	for (Control c = ct; c != null; c = c.getParent()){
		c.getRect(dim);
		x += dim.x; y += dim.y;
	}
	dim.set(ox,oy,w,h);
	dest.copyRect(ct.getWindow(),x,y,w,h,0,0);
}
//==================================================================
public static void removeFrom(Container c,Control ctrl)
//==================================================================
{
	//BETA6
	//if (c instanceof Control) ((Control)c).remove(ctrl);
	//BETA7
	c.remove(ctrl);
}
//==================================================================
public static boolean pressedOutsideTopFrame(Control onWho,Point whereOnWho)
//==================================================================
{
	if (onWho == null) return true;
	Frame f2 = onWho.getFrame();
	if (f2 == null) return true;
	if (f2.isControlPanel) return true;
	Frame f = topFrame(onWho);
	if (f == null) return true;
	Rect r = getAppRect(onWho);
	if (f.wantPressedOutside) {
		Point p = new Point(whereOnWho.x+r.x,whereOnWho.y+r.y);
		f.pressedOutside(p);
		return !f.capturePressedOutside;
	}
	Sound.beep();
	return false;
}
public static final int CENTER_FRAME = 0x1;
public static final int FILL_FRAME = 0x2;
/**
* This should not be OR'ed with FILL_HEIGHT. Use FILL_FRAME instead.
**/
public static final int FILL_WIDTH = 0x4;
/**
* This should not be OR'ed with FILL_WIDTH. Use FILL_FRAME instead.
**/
public static final int FILL_HEIGHT = 0x8;
public static final int NEW_WINDOW = 0x10;
static final int ALL_DISPLAY_OPTIONS = 0x1f;

public static final int PUTTING_POPUP = 0x20;

//-------------------------------------------------------------------
protected static Container setupNewWindow(Frame f,Container parent,boolean modal)
//-------------------------------------------------------------------
{
	int toSet = modal ? Window.FLAG_IS_MODAL:0;
	int toClear = 0;
	Window w = null;
	FormFrame ff = null;
	if (f instanceof FormFrame){
		ff = (FormFrame)f;
		Form form = ff.myForm;
		w = form.createWindow();
		f.make(false);
		toSet |= form.windowFlagsToSet;
		toClear |= form.windowFlagsToClear;
		if (!form.resizable) toClear |= Window.FLAG_CAN_RESIZE|Window.FLAG_CAN_MAXIMIZE|Window.FLAG_CAN_MINIMIZE;
		else toSet |= Window.FLAG_CAN_RESIZE|Window.FLAG_CAN_MAXIMIZE|Window.FLAG_CAN_MINIMIZE;
	}else{
		w = new Window();
		f.make(false);
	}
	Dimension d = f.getPreferredSize(null);
	Rect sz = new Rect(toSet,toClear,d.width,d.height);
	ewe.sys.Long flags = (ewe.sys.Long)w.getInfo(w.INFO_FLAGS_FOR_SIZE,sz,new ewe.sys.Long(),0);
	int flg = (int)flags.value;
	//flg &= ~toClear;
	Control top = null;
	boolean shrankIt = false;
	if (true && ((flg & Window.FLAG_HAS_TITLE) != 0)) {
		if (ff == null || !ff.hasExtraTitleControls)
			top = f.top;
	}
	if (true && (top != null)){
		top.modify(f.ShrinkToNothing,0);
		f.relayoutMe(false);
		d = f.getPreferredSize(d);
		shrankIt = true;
	}
	sz.set(-1,-1,d.width,d.height);
	if ((toSet & Window.FLAG_IS_DEFAULT_SIZE) != 0)
		sz.width = sz.height = -1;
	if (w.creationData == null && parent != null)
		w.creationData = new WindowCreationData();
	if (parent != null) w.creationData.parentWindow = parent.getWindow();
	if (!w.create(sz,f.name,toSet,toClear,w.creationData))
		throw new RuntimeException("Could not create native window!");
	//
	if (shrankIt)
		if ((w.getWindowFlags() & Window.FLAG_HAS_TITLE) == 0){
			//ewe.sys.Vm.messageBox("UnShrinking Title!","I will un-shrink the title.",ewe.sys.Vm.MB_OK);
			f.top.modify(0,f.ShrinkToNothing);
			f.relayoutMe(false);
		}
	//
	f.closeWindow = true;
	return w.contents;
}

private static int windowCheck = -1;
private static boolean noWindows = false;
/**
This forces the system to act as if multiple windows are not supported.
This cannot be undone.
**/
//===================================================================
public static void setNoMultipleWindows()
//===================================================================
{
	windowCheck = 0;
	noWindows = true;
	Vm.setParameter(Vm.SET_NO_WINDOWS,1);
	if (mApp.mainApp != null && mApp.mainApp.isCreated())
		mApp.mainApp.setWindowFlags(Window.FLAG_IS_VISIBLE);
}
//-------------------------------------------------------------------
private static void checkWindows()
//-------------------------------------------------------------------
{
	if (windowCheck == -1)
		windowCheck = Window.supportsMultiple() && !noWindows ? 1 : 0;
	if (windowCheck == 0) NullParentFrameToMainApp = true;
}

private static Frame allParentFrame;
/**
This returns the parent frame for a control's popup menu.
**/
//===================================================================
public static Frame getPopupMenuParentFrame(Control c)
//===================================================================
{
	if (allParentFrame != null) return allParentFrame;
	return c.getWindow().contents;
}
/**
Set the system to believe that multiple windows are not possible and that
all new Frames should be shown in the specified Frame. If the specified Frame is
null then the system will revert back to the normal operation.
**/
//===================================================================
public static void setAllParentFrame(Frame f)
//===================================================================
{
	allParentFrame = f;
	if (f == null){
		windowCheck = -1;
		checkWindows();
		screenSize = (Rect)Window.getGuiInfo(Window.INFO_SCREEN_RECT,null,new Rect(),0);
	}else{
		windowCheck = 0;
		NullParentFrameToMainApp = true;
		screenSize = f.getDim(new Rect());
	}
}
//-------------------------------------------------------------------
static Form getTopmostForm(Control c)
//-------------------------------------------------------------------
{
	Form top = null;
	for (; c != null; c = c.getParent())
		if (c == allParentFrame) break;
		else if (c instanceof Form) top = (Form)c;
	return top;
}
/**
 * Close all Frames denoted as popup frames in the Window containing the control.
 * @param window Either the window or a Control within the window.
 * @return true if all popups were closed successfully, false if not.
 */
//===================================================================
public static boolean closePopups(Control window)
//===================================================================
{
	return closePopups(window,0,0);
}
//===================================================================
public static boolean closePopups(Control window,int why, int flags)
//===================================================================
{
	Frame last = null;
	while(true){
		Frame f = topFrame(window);
		if (f == null) return true;
		if (f == last) return false;
		if (f.popupController == null) return true;
		last = f;
		f.popupController.closePopup(why,flags);
	}
}
//-------------------------------------------------------------------
protected static Container checkParent(Container parent,int options)
//-------------------------------------------------------------------
{
	checkWindows();
	if (windowCheck == 0) options &= ~NEW_WINDOW;

	if (parent == null && ((options & NEW_WINDOW) == 0)){
		if (NullParentFrameToMainApp) {
			parent = allParentFrame;
			if (parent == null){
				parent = mApp.appFrame;
				if ((mApp.mainApp.getWindowFlags() & Window.FLAG_IS_VISIBLE) == 0) parent = null;
			}
		}
	}
	if ((options & PUTTING_POPUP) == 0)
		closePopups(parent);

	return parent;
}
/**
* This will add the frame to the container and call a make() on it.
* This also sets the frame to be modal.
* If you specify an option of FILL_FRAME, CENTER_FRAME or FILL_WIDTH or FILL_HEIGHT, then the
* frame will be positioned and displayed on screen. If not
* you will have to call setRect()/repaintNow() on the frame to cause it
* to be positioned and painted.
*/
//==================================================================
public static void execFrame(Frame f,Container parent,int options)
//==================================================================
{
	if (f instanceof FormFrame)
		if (((FormFrame)f).myForm instanceof MessageBox)
			ewe.sys.Vm.freezeSIP(parent);
	if (f.popupController != null) options |= PUTTING_POPUP;
	f.isModal = true;
	parent = checkParent(parent,options);
	if (parent == null) options = NEW_WINDOW;
	if (windowCheck == 0) options &= ~NEW_WINDOW;
	inFocus.add(null);
	if ((options & NEW_WINDOW) != 0){
		parent = setupNewWindow(f,parent,true);
		options = FILL_FRAME;
	}else{
		parent.getWindow().makeModal();
	}
	doExecFrame(f,parent,options);
	showFrame(f,parent,options);
}

//===================================================================
public static int fixShowOptions(Container parent,int options)
//===================================================================
{
	checkWindows();
	if ((options & NEW_WINDOW) != 0 && (options & 0xf) == 0) options |= CENTER_FRAME;
	if (windowCheck == 0) options &= ~NEW_WINDOW;
	if ((options & NEW_WINDOW) != 0) return options;
	if (checkParent(parent,options) == null) return NEW_WINDOW;
	if (windowCheck == 0) {
		NullParentFrameToMainApp = true;
		options &= ~NEW_WINDOW;
	}
	return options;
}
//===================================================================
public static boolean dontPaintNextFrame = true;
//===================================================================
/**
* This will add the frame to the container and call a make() on it.
* This does not set the frame to be modal.
* If you specify an option of FILL_FRAME or CENTER_FRAME or FILL_WIDTH or FILL_HEIGHT, then the
* frame will be positioned and displayed on screen. If not
* you will have to call setRect()/repaintNow() on the frame to cause it
* to be positioned and painted.
*/
//==================================================================
public static void showFrame(Frame f,Container parent,int options)
//==================================================================
{
	if (mApp.mainApp.isLocked()) return;
	Form form = null;
	if (f instanceof FormFrame){
		form = ((FormFrame)f).myForm;
		if (form instanceof MessageBox)
			ewe.sys.Vm.freezeSIP(parent);
	}
	if (f.popupController != null) options |= PUTTING_POPUP;
	parent = checkParent(parent,options);
	if (parent == null) options = NEW_WINDOW;
	if (windowCheck == 0) options &= ~NEW_WINDOW;
	if ((options & NEW_WINDOW) != 0){
		parent = setupNewWindow(f,parent,false);
		options = FILL_FRAME;
	}else if (form != null && parent != null){
		if ((form.windowFlagsToSet & Window.FLAG_MAXIMIZE) != 0)
			options |= FILL_FRAME;
		else if ((form.windowFlagsToSet & Window.FLAG_MAXIMIZE_ON_PDA) != 0 && screenIs(PDA_SCREEN)){
			options |= FILL_FRAME;
		}
	}
	//parent = mApp.appFrame;
	//if (parent instanceof Control) parent.add(f);//((Control)parent).addToFront(f);
	//else
 	if (f != parent){
		parent.addDirectly(f);
		Frame pf = parent instanceof Frame ? (Frame)parent : null;
		if (pf == null) pf = parent.getFrame();
		if (pf == null) pf = mApp.appFrame;
		if (pf != null && pf != f) pf.addChildFrame(f);
	}
	f.displayOptions = options;
	f.make(false);
	if ((options & ALL_DISPLAY_OPTIONS) != 0){
		Dimension r = f.getPreferredSize(null);
		Rect cr = parent.getRect(null);
		/*
		if (parent == mApp.appFrame && f.resizeOnSIP) {
			cr.width = mApp.mainApp.visibleWidth;
			cr.height = mApp.mainApp.visibleHeight;
		}
		*/
		/*
		if (form != null){
			if ((form.windowFlagsToSet & Window.FLAG_MAXIMIZE) != 0) options |= FILL_FRAME;
		}
		*/
		if ((options & (FILL_FRAME|FILL_WIDTH)) != 0) r.width = cr.width;
		if ((options & (FILL_FRAME|FILL_HEIGHT)) != 0) r.height = cr.height;
		if (r.width > cr.width) r.width = cr.width;
		if (r.height > cr.height) r.height = cr.height;
		//System.out.println(Geometry.toString(cr));
		f.setRect((cr.width-r.width)/2,(cr.height-r.height)/2,r.width,r.height);
		//if (!dontPaintNextFrame) refreshScreen(f); //Why this one? Safer?

		if (!dontPaintNextFrame) {
			f.repaintNow();
			f.shown();
		}else
			f.shown();
		notifyTop(parent.getWindow(),true);
	}
	dontPaintNextFrame = false;
}

//==================================================================
public static void execFrame(Frame f,Container parent) {execFrame(f,parent,0);}
public static void showFrame(Frame f,Container parent) {showFrame(f,parent,0);}
//==================================================================
//==================================================================
public static Point getPosInParent(Control c,Container parent)
//==================================================================
{
	if (c == parent) return new Point(0,0);
	Rect r = c.getRect();
	Point p = new Point(r.x,r.y);
	for (c = c.getParent(); c != parent && c != null; c = c.getParent()){
		r = c.getRect();
		p.translate(r.x,r.y);
	}
	return p;
}

protected static Vector inFocus = new Vector();
//protected static Control inFocus = null;

static int setSipTo = -1;

private static int newSip = -1;
private static int focusCount = 0;
static Window sipWindow = null;

//-------------------------------------------------------------------
private static void changeSip(int newSip)
//-------------------------------------------------------------------
{
	//Vm.messageBox("SIP",newSip == 0 ? "Off" : "On",Vm.MB_OK);
	//Vm.debug(newSip == 0 ? "Off" : "On");
	/*
	try{
		String s = Vm.getStackTrace(new Exception("Gui Sip: "+newSip));
		File out = new File("/SipLog.txt");
		PrintWriter pw = new PrintWriter(out.toWritableStream(true));
		pw.println(new Time().format("HH:mm:ss"));
		pw.println(s);
		pw.println();
		pw.close();
	}catch(Exception e){}
	*/
	//if (true) new Exception().printStackTrace();
	if (Window.inEventThread) {
		//ewe.sys.Vm.debug("setSipTo: "+newSip);
		//if (newSip == 1) ewe.sys.Vm.debug(ewe.sys.Vm.getStackTrace(new Exception(),5));
		//if (newSip == 0 && Vm.getSIP() != 0) new Exception().printStackTrace();
		setSipTo = newSip;
	}
	else
		ewe.sys.Vm.setSIP(newSip);
}


/**
* If this is true, each time a control is activated, it acts as if it were activated by a
* keyboard selection (i.e. the used TAB to access it), unless the control was activated by
* a pen/mouse press. This only works on devices that have no mouse pointer and is used to
* facilitate one handed entry on devices that have a Jog Wheel or other such input device.
**/
static boolean showActiveControlOnPDA = true;

private static Rect fullRect = new Rect(), cRect = new Rect();

//-------------------------------------------------------------------
private static void setFocusOn(Control c, int how, Control old)
//-------------------------------------------------------------------
{
	c.gotFocus(how);
	//if (c instanceof mCheckBox) new Exception().printStackTrace();
	//MainWindow._mainWindow.setFocus(c);//DONT PUT THIS. CAUSES EVENT DIRECTION PROBLEMS.
	if (focusedControl() == c){
		Window w = c.getWindow();
		if (w != null) w.setFocus(c);
		ControlEvent ce = new ControlEvent(ControlEvent.FOCUS_IN,c);
		ce.oldOrNewFocus = old;
		c.postEvent(ce);
	}
}
//-------------------------------------------------------------------
private static void takeFocusOff(Control c, int how, Control newFocus)
//-------------------------------------------------------------------
{
	c.lostFocus(how);
	//MainWindow._mainWindow.setFocus(c);//DONT PUT THIS. CAUSES EVENT DIRECTION PROBLEMS.
	ControlEvent ce = new ControlEvent(ControlEvent.FOCUS_OUT,c);
	ce.oldOrNewFocus = newFocus;
	c.postEvent(ce);
}
/**
 * This tells the Gui to assign focus to a particular control. This will also handle removing
 * the focus from any previously focused control. You can also use a null control to remove
 * focus altogether.
 * @param c The control to receive the focus, or null to remove the focus altogether.
 * @param how Should be Control.ByRequest for a programmatic focus change. But it can also be:
	 	Control.ByKeyboard, Control.ByMouse, Control.ByPen (same as ByMouse), Control.ByFrameChange.
 */
//==================================================================
public static void takeFocus(Control c,int how)
//==================================================================
{
	if (showActiveControlOnPDA && hasMousePointer && how != Control.ByMouse) {
		how = ByKeyboard;
	}
	try{
		if (focusCount == 0) newSip = -1;
		focusCount++;
		if (c != null){
			if (!c.amOnTopFrame()) {
				return;
			}
			sipWindow = c.getWindow();
		}
		//if (c != null) ewe.sys.Vm.debug(c.getClass().toString());
		int sz = inFocus.size();
		if (sz == 0) {
			inFocus.add(null);
		}
		else sz--;
		Control cur = (Control)inFocus.get(sz);
		if (cur == c) return;
		inFocus.set(sz,c);
		if (cur != null) {
			takeFocusOff(cur,how,c);
		}
		if (c != null) {
		//Auto Scrolling - seems to work
			if (c.parent != null && !c.parent.dontAutoScroll){
				boolean didScroll = false;
				Container useParent = c.parent;
				cRect.set(c.x,c.y,c.width,c.height);
				if (c.promptControl != null && c.promptControl.parent != null){
					Control cp = c.promptControl;
					useParent = cp.parent;
					if (useParent != c.parent){
						Point pp = Gui.getPosInParent(c,useParent);
						if (pp != null) {
							cRect.x += pp.x;
							cRect.y += pp.y;
						}
					}
					cRect.unionWith(fullRect.set(cp.x, cp.y, cp.width, cp.height));
					if (useParent.scrollToVisible(cRect.x,cRect.y,cRect.width,cRect.height))
						didScroll = true;
					cRect.set(c.x,c.y,c.width,c.height);
					if (useParent != c.parent){
						Point pp = Gui.getPosInParent(c,useParent);
						if (pp != null) {
							cRect.x += pp.x;
							cRect.y += pp.y;
						}
					}
				}

				if (useParent.scrollToVisible(cRect.x, cRect.y, cRect.width, cRect.height)){
					didScroll = true;
				}
				if (didScroll) c.parent.repaintNow();
			}
			if (c.hasModifier(c.ShowSIP,false) && c.canEdit()) newSip = 1;
			else if (!c.hasModifier(c.KeepSIP,false) && !Frame.hasSipResized(sipWindow)) {
				Frame cf = c.getFrame();
				if (cf != null)
					if (!cf.isPopup())
						cf = null;
				if (cf == null) newSip = 0;
			}
			setFocusOn(c,how,cur);
		}else{
			newSip = 0;
		}
	}finally{
		focusCount--;
		if (focusCount == 0 && newSip != -1){
			changeSip(newSip);
		}
	}
		//if (Control.debugFlag) ((Object)null).toString();
}
//===================================================================
public static void moveFrameTo(Frame f,Rect where)
//===================================================================
{
	Rect now = getAppRect(f);
	int old = f.modify(Invisible,0);
	refreshScreen(f.getWindow(),now);
	f.setRect(where);
	f.restore(old,Invisible);
	f.repaintNow();
}
//==================================================================
public static void refreshScreen(Window w){refreshScreen(w,(Rect)null);}
public static void refreshScreen(Window w,Rect area)
//==================================================================
{
	if (w != null)
		w.repaintNow(null,area);
}
//==================================================================
public static void refreshScreen(Control c)
//==================================================================
{
	refreshScreen(c.getWindow(),getAppRect(c));
}
//===================================================================
public static void screenResized(int width,int height)
//===================================================================
{

}
/**
* This will setup an the title bar on a Form if the main ewe window does
* not have a title and a close button.
**/
//===================================================================
public static void setAppFormTitle(Form form,String title)
//===================================================================
{
	setFormTitle(mApp.mainApp,form,title);
}
/**
* This will setup an the title bar on a Form if the main ewe window does
* not have a title and a close button.
**/
//===================================================================
public static void setFormTitle(Window w,Form form,String title)
//===================================================================
{
	boolean setTitle = true;
	if (w != null){
		int flag = w.getWindowFlags();
		if ((flag & (w.FLAG_HAS_TITLE|w.FLAG_HAS_CLOSE_BUTTON)) == (w.FLAG_HAS_TITLE|w.FLAG_HAS_CLOSE_BUTTON))
			setTitle = false;
	}
	if (setTitle) {
		form.hasTopBar = true;
		form.title = title;
		form.exitButtonDefined = false;
	}else{
		form.hasTopBar = false;
	}
}
/**
* This will set the OK/Cancel of a form to be either in the title bar (for mobile devices)
* or on the bottom for desktop devices.
**/
//===================================================================
public static void setOKCancel(Form f)
//===================================================================
{
	if (isSmartPhone){
		SoftKeyBar sb = new SoftKeyBar();
		sb.setKey(1,"OK|"+Form.EXIT_IDOK,f.tick,null);
		sb.setKey(2,"Cancel|"+Form.EXIT_IDCANCEL,f.cross,null);
		f.setSoftKeyBarFor(null,sb);
	}else if ((ewe.sys.Vm.getParameter(ewe.sys.Vm.VM_FLAGS) & ewe.sys.Vm.VM_FLAG_IS_MOBILE) != 0){
		if ((f.windowFlagsToSet & Window.FLAG_MAXIMIZE_ON_PDA) != 0)
			f.doButtons(f.OKB|f.CANCELB);
		else{
			f.titleCancel = new mButton(f.cross);
			f.titleOK = new mButton(f.tick);
			f.hasTopBar = true;
			f.windowFlagsToClear |= Window.FLAG_HAS_TITLE;
		}
	}else
		f.doButtons(f.OKB|f.CANCELB);
}
/**
* This will iconize a button/control that has its text already set. If an icon is found
* successfully it will be displayed. If leaveText is true then the text is displayed with
* the icon, otherwise only the icon is displayed.
**/
//===================================================================
public static boolean iconize(Control c,String image,Object maskOrColor,boolean leaveText,FontMetrics fm)
//===================================================================
{
	return iconize(c,ImageCache.cache.get(image,maskOrColor),leaveText,fm);
}
/**
* This will iconize a button/control that has its text already set. If an icon is found
* successfully it will be displayed. If leaveText is true then the text is displayed with
* the icon, otherwise only the icon is displayed.

**/
//===================================================================
public static boolean iconize(Control c,IImage image,boolean leaveText,FontMetrics fm)
//===================================================================
{
	if (image == null) return false;
	if (image.getWidth() == 0) return false;
	if (fm == null) fm = c.getFontMetrics();
	if (leaveText) {
		c.image = new IconAndText(image,c.makeHot(c.text),fm);
		((IconAndText)c.image).textColor = null;
	}else c.image = image;
	c.text = "";
	return true;
}
/**
* This returns true if the Frame is the main frame of a window.
**/
//===================================================================
public static boolean isWindowFrame(Frame f)
//===================================================================
{
	if (f == null) return false;
	return f.closeWindow;
}
//===================================================================
public static Dimension getPreferredDialogSize()
//===================================================================
{
	Dimension d = new Dimension(500,300);
	/*
	Rect r = (Rect)mApp.mainApp.getInfo(Window.INFO_PARENT_RECT,null,new Rect(),0);
	if (r.height <= 320 || r.width <= 240) {
		d.width = r.width;
		d.height = r.height;
	}else{
		d.width = 500;
		d.height = 400;
	}
	*/
	return d;
}

/**
* This is the size of the user's screen, but will not be set until the screenIs() method is called.
* You can always get the size of the screen by doing:
* <pre>
* Rect s = (Rect)Window.getGuiInfo(Window.INFO_SCREEN_RECT,null,new Rect(),0);
* </pre>
**/
public static Rect screenSize;
/**
* A possible flag for screenIs.
**/
public static final int BIG_SCREEN = 1;
/**
* A possible flag for screenIs.
**/
public static final int WIDE_SCREEN = 2;
/**
* A possible flag for screenIs.
**/
public static final int LONG_SCREEN = 3;
/**
* A possible flag for screenIs.
**/
public static final int DESKTOP_SCREEN = 4;
/**
* A possible flag for screenIs.
**/
public static final int DESKTOP_WIDTH = 5;
/**
* A possible flag for screenIs.
**/
public static final int DESKTOP_HEIGHT = 6;
/**
* A possible flag for screenIs.
**/
public static final int PDA_SCREEN = 7;
/**
* Check if the user screen is of a certain type.
* @param flags One of the XXX_SCREEN or DESKTOP_XXX constants.
* @return true if the screen is considered to be of the type specified by the flag.
*/
//===================================================================
public static boolean screenIs(int flags)
//===================================================================
{
	//if (true) return false;
	if (screenSize == null)
		screenSize = (Rect)Window.getGuiInfo(Window.INFO_SCREEN_RECT,null,new Rect(),0);

	if (flags == BIG_SCREEN)
		return screenSize.width >= 600 && screenSize.height >= 300;
	else if (flags == WIDE_SCREEN)
		return screenSize.width >= 600 || screenSize.width > screenSize.height;
	else if (flags == LONG_SCREEN)
		return screenSize.height >= 290 || screenSize.height > screenSize.width;
	else if (flags == DESKTOP_SCREEN)
		return screenSize.height >= 480 && screenSize.width >= 640;
	else if (flags == DESKTOP_WIDTH)
		return screenSize.width >= 640;
	else if (flags == DESKTOP_HEIGHT)
		return screenSize.height >= 480;
	else if (flags == PDA_SCREEN)
		return screenSize.width <= 350 && screenSize.height <= 350;
	return false;
}
public static Rect rp = new Rect();
//===================================================================
public static boolean requestPaint(Control who)
//===================================================================
{
	Window w = who.getWindow();
	if (w == who || w == null) return true;
	//
	// FIXME! Something more effecient?
	//
	//
	if (who.getFrame() == w.contents) return true;
	if (who.getParent() == w) return true;
	int x = 0, y = 0;
	Frame top = (w.frames.size() != 0) ? (Frame)w.frames.get(w.frames.size()-1) : null;
	if (top == null) return true;
	for (Control c = top; c != null; c = c.getParent())
		if (c == who) return true;
	if (top.controlsToRefresh != null)
		if (top.controlsToRefresh.find(who) != -1)
			return false;
	for (Control c = who; c != null; c = c.getParent()){
		if (c == top) return true;
		x += c.x;
		y += c.y;
	}
	if (!getAppRect(top,rp).intersects(Rect.buff.set(x,y,who.width,who.height))) return true;
	if (top.controlsToRefresh == null) top.controlsToRefresh = new Vector();
	top.controlsToRefresh.add(who);
	return false;

/*
	if (who instanceof Window) return true;
	Window w = who.getWindow();
	if (w == null) return true;
	if (who.parent == w.contents || who.parent == w) return true;
	Frame top = topFrame(who);
	if (top == null) return true;
	if ((top.getModifiers(true) & top.Invisible) != 0) return true;
	for (Control c = who; c != null; c = c.parent){
		if (c == top) return true;
		if (c.parent == null && !(c instanceof Window)) return false; //Not on screen.
	}
	//I am not on the top frame, so save a refresh of me for later.
	//ewe.sys.Vm.debug("No can do: "+who.getClass());
	if (top.controlsToRefresh == null)
		top.controlsToRefresh = new Vector();
	if (top.controlsToRefresh.find(who) == -1)
		top.controlsToRefresh.add(who);
	return false;
*/
}
//===================================================================
public static void refreshTopFrame(Control who)
//===================================================================
{
	if (who instanceof Window) return;
	Window w = who.getWindow();
	if (w == null) return;
	if (who.parent == w.contents || who.parent == w) return;
	Control top = topFrame(who);
	if (top == null) return;
	for (Control c = who; c != null; c = c.parent){
		if (c == top) return;
		if (c.parent == null && !(c instanceof Window)) return; //Not on screen.
	}
	top.repaintNow();
}
/**
* Used with setStyle.
**/
public static final int STYLE_ETCHED = 1;
/**
* Used with setStyle.
**/
public static final int STYLE_3D = 0;
/**
* Used with setStyle.
**/
public static final int STYLE_SOFT = 2;
/**
* Used with setStyle.
**/
public static final int STYLE_PALM = 3;
/**
 * Set the style of Controls within an application.
 * This sets the look of the Controls within an application to be either a Windows style 3D look
	or a Java style etched look. To make all of your controls look flat set the "Control.globalDrawFlat"
	variable true. You should also call Color.setMonochrome(true) if you want high-contrast flat controls.
 * @param style STYLE_ETCHED or STYLE_3D
 */
//===================================================================
public static final void setStyle(int style)
//===================================================================
{
	Control.globalDrawFlat = Control.globalPalmStyle = false;
	if (style == STYLE_ETCHED || style == STYLE_SOFT){
		Control.standardBorder =  ButtonObject.buttonEdge = ButtonObject.checkboxEdge = mInput.inputEdge = EDGE_ETCHED|((style == STYLE_SOFT) ? BF_SOFT : 0);
		Control.standardEdge = EDGE_ETCHED;
	}else if (style == STYLE_PALM){
		mInput.inputEdge = BDR_OUTLINE|BF_BOTTOM;
	  ButtonObject.buttonEdge = BF_SOFT|BF_RECT;
		Control.globalDrawFlat = Control.globalPalmStyle = true;
	}else{
		Control.standardBorder = EDGE_ETCHED;
		ButtonObject.buttonEdge = Control.standardEdge = EDGE_RAISED|
		(((ewe.sys.Vm.getParameter(ewe.sys.Vm.VM_FLAGS) & ewe.sys.Vm.VM_FLAG_NO_MOUSE_POINTER) != 0) ? Control.BDR_OUTLINE : 0);
		ButtonObject.checkboxEdge = mInput.inputEdge = EDGE_SUNKEN;
	}
	ButtonObject.buttonEdge |= Control.BF_BUTTON;
	if (style == STYLE_PALM) Color.setMonochrome(true);
	else Color.setMonochrome(false);
}
/**
 * Given text in the format "Text$hotkey" this returns the hotkey character.
 * @param text The text for the control with the included hotkey.
 * @return The hotkey character.
 */
//===================================================================
public final static char getHotKeyFrom(String text)
//===================================================================
{
	if (text == null) return 0;
	int idx = text.lastIndexOf('$');
	if (idx < 0 || idx != text.length()-2) return 0;
	return text.charAt(idx+1);
}

/**
 * Given text in the format "Text$hotkey" this returns the text string without the hotkey character.
 * @param text The text for the control with the included hotkey.
 * @return The text without the hotkey character.
 */
//===================================================================
public final static String getTextFrom(String text)
//===================================================================
{
	if (text == null) return null;
	int idx = text.lastIndexOf('$');
	if (idx < 0 || idx != text.length()-2) return text;
	return text.substring(0,idx);
}

/**
 * Convert a string with a hotkey to be a "true" hotkey encoded String which, when displayed by
	* a Graphics object, will have its hotkey underlined. Note that this is very different to the
	* human readable hotkey encoding where a $ is placed as the second to last character and the
	* hotkey is the last character. A hotkey encoded String is one
	* where the last character is a null character. The character immediately before the null is then taken
	to be the hot key.
 * @param label The pure String.
 * @param hotKey The hotkey (if any) to apply to the String.
 * @return The displayable String which will be displayed with it's hotkey underlined by a Graphics object.
 */
//===================================================================
public static final String makeHot(String label,int hotKey)
//===================================================================
{
	if (noHotLabels || label == null) return label;
	if ((hotKey & (IKeys.INVISIBLE<<24)) != 0) return label;
	if ((hotKey & 0xffff) == 0 || label.length() == 0) return label;
	return label+(char)(hotKey & 0xffff)+(char)0;
}


/**
 * Convert a '$' formatted hotkey into a "true" hotkey encoded String. A hotkey encoded String is one
	* where the last character is a null character. The character immediately before the null is then taken
	to be the hot key.
 * @param label The '$' formatted hotkey.
 * @return The displayable String which will be displayed with it's hotkey underlined by a Graphics object.
 */
//===================================================================
public static final String makeHot(String label)
//===================================================================
{
	if (label.indexOf('$') == -1) return label;
	return makeHot(getTextFrom(label),getHotKeyFrom(label));
}
/**
* This gets a Graphics object for a window. If the Window is rotated it will return a RotatedGraphics
* object.
**/
//===================================================================
public static Graphics getGraphics(Window window)
//===================================================================
{
	if (window == null) return null;
	if (!mApp.rotated && !mApp.counterRotated) return new Graphics(window);
	if (window.savedImage == null)
		window.savedImage = new Image(window.width,window.height,Image.RGB_IMAGE);
	return new RotatedGraphics(window,window.savedImage,mApp.rotated ? 90 : 270);
}

/**
 * Set a pluggable look and feel style manager for the GUI components. As of version
 * 1.2 this is not supported by any Ewe VM but eventually desktop versions will support
 * this feature.
 * @param manager The StyleManager to use.
 * @exception UnsupportedOperationException if a pluggable look and feel style manager is not supported
 * on this VM.
* @return true if the manager was set, false if style managers are not supported on this platform.
*/
//===================================================================
public static boolean setStyleManager(StyleManager manager)
//===================================================================
{
	return false;
}

/**
 * Return the current pluggable look and feel style manager. On VMs that do not support pluggable
 * look and feel style managers, this will always return null.
 * @return the current pluggable look and feel style manager.
 */
//===================================================================
public static StyleManager getStyleManager()
//===================================================================
{
	return null;
}
static final int anchors[] =
{'W',UIConstants.WEST,'E',UIConstants.EAST,'N',UIConstants.NORTH,'S',UIConstants.SOUTH,'H',UIConstants.HEXPAND,'h',UIConstants.HCONTRACT,'V',UIConstants.VEXPAND,'v',UIConstants.VCONTRACT,'F',UIConstants.FILL};
static final int aligns[] = {'L',UIConstants.LEFT,'R',UIConstants.RIGHT};
//-------------------------------------------------------------------
static int decode(String specs,int [] codes)
//-------------------------------------------------------------------
{
	int ret = 0;
	for (int i = 0; i<codes.length; i+=2)
		if (specs.indexOf((char)codes[i]) != -1) ret |= codes[i+1];
	return ret;
}
public static int decodeAnchor(String specs) {return decode(specs,anchors);}
public static int decodeAlignment(String specs) {return decode(specs,aligns);}

//===================================================================
public static int getGuiFlags()
//===================================================================
{
	ewe.sys.Long ret = (ewe.sys.Long)Window.getGuiInfo(Window.INFO_GUI_FLAGS,null,new ewe.sys.Long(),0);
	if (ret == null) return 0;
	return (int)ret.value;
}
/**
 * This method does a relayout and resize on a Frame and its containing Window (if the Frame contained within a native
 * Window). The Frame will be resized to its new preferred size.
 * @param f The Frame to relayout - which usually is obtained by calling getFrame() on a Form
 * or other control.
 * @param overrideMaximize if this is true then the relayout will occur even if the Window
 * is in a maximized or minimized state.
 */
//===================================================================
public static void relayoutFrameAndWindow(Frame f, boolean overrideMaximize)
//===================================================================
{
	f.relayout(false);
	try{
		if (Gui.isWindowFrame(f)){
			Window w = f.getWindow();
			int fl = w.getWindowFlags();
			boolean isMaxMin = (fl & (w.FLAG_MAXIMIZE|w.FLAG_MINIMIZE)) != 0;
			if (overrideMaximize || !isMaxMin){
				fl &= ~(w.FLAG_MAXIMIZE|w.FLAG_MINIMIZE|w.FLAG_MAXIMIZE_ON_PDA|w.FLAG_IS_DEFAULT_SIZE);
				Rect r = w.getWindowRect(new Rect(),true);
				Dimension d = f.getPreferredSize(null);
				r.x = fl;
				r.width = d.width; r.height = d.height;
				ewe.sys.Long l = (ewe.sys.Long)w.getInfo(w.INFO_FLAGS_FOR_SIZE,r,new ewe.sys.Long(),0);
				if ((l.value & w.FLAG_IS_DEFAULT_SIZE) != 0){
					w.setState(w.STATE_MAXIMIZED);
				}else{
					w.setState(w.STATE_NORMAL);
					r.width = d.width; r.height = d.height;
					w.setWindowRect(r,true);
				}
			}else{
				f.repaintNow(); //Maximized.
			}
		}else{
			Rect r = f.getRect(null);
			Dimension d = f.getPreferredSize(null);
			r.width = d.width; r.height = d.height;
			Gui.moveFrameTo(f,r);
		}
	}catch(Exception e){
		f.repaintNow();
	}
}

public static boolean hasMousePointer = (Vm.getParameter(Vm.VM_FLAGS) & (Vm.VM_FLAG_NO_MOUSE_POINTER)) == (Vm.VM_FLAG_NO_MOUSE_POINTER);
public static boolean hasPen = (Vm.getParameter(Vm.VM_FLAGS) & (Vm.VM_FLAG_NO_PEN)) == 0;
public static boolean hasKeyboard = (Vm.getParameter(Vm.VM_FLAGS) & (Vm.VM_FLAG_NO_KEYBOARD)) == 0;
public static boolean isSmartPhone = (Vm.getParameter(Vm.VM_FLAGS) & (Vm.VM_FLAG_NO_PEN|Vm.VM_FLAG_HAS_SOFT_KEYS)) == (Vm.VM_FLAG_NO_PEN|Vm.VM_FLAG_HAS_SOFT_KEYS);
public static boolean isPDA = (Vm.getParameter(Vm.VM_FLAGS) & (Vm.VM_FLAG_NO_MOUSE_POINTER)) == (Vm.VM_FLAG_NO_MOUSE_POINTER);

/**
By default, this is set to: !hasKeyboard || isSmartPhone || isPDA<p>
but you can change it as you wish.
**/
public static boolean noHotLabels = !hasKeyboard || isSmartPhone || isPDA;

/** An option for flashMessage. Will also beep when the message is shown. **/
public static final int FLASH_BEEP = 0x1;
/**
Display a short message on the screen, and keep it on the screen until
flashMessageOff() is called.
* @param message A single-line message to display.
* @param parent The parent Control.
* @return an Object that you should use with flashMessageOff() when you
* want to remove the message.
*/
//===================================================================
public static Object flashMessageOn(String message, Control parent)
//===================================================================
{
	Form f = new Form();
	f.backGround = Color.LightBlue;
	f.foreGround = Color.Black;
	f.addNext(new mLabel(message));
	f.hasTopBar = false;
	f.resizable = false;
	f.setBorder(EDGE_BUMP|BDR_OUTLINE,2);
	f.exitButtonDefined = true;
	f.exec(parent.getFrame(),CENTER_FRAME|PUTTING_POPUP);
	return f;
}
/**
Remove the short message as displayed by flashMessageOn().
* @param message The object returned by flashMessageOn().
*/
//===================================================================
public static void flashMessageOff(Object message)
//===================================================================
{
	if (message instanceof Form)
		((Form)message).close(0);
}
//===================================================================
public static void flashMessage(String message, Control parent)
//===================================================================
{
	flashMessage(message,750,parent,FLASH_BEEP);
}

//===================================================================
public static void flashMessage(String message,final int timeInMillis, Control parent,int flash_options)
//===================================================================
{
/*
	flashMessage(message,timeInMillis,parent,flash_options,null);
}
//===================================================================
public static void flashMessage(String message,final int timeInMillis, Control parent,int flash_options, Control dontCover)
//===================================================================
{
*/
	//
	// FIXME dont cover the dontCover
	//
	final Form f = (Form)flashMessageOn(message,parent);
	if ((flash_options & FLASH_BEEP) != 0) Sound.beep();
	new ewe.sys.TaskObject(){
		protected void doRun(){
			sleep(timeInMillis);
			//lockEvents();
			try{
				f.close(0);
			}finally{
				//unlockEvents();
			}
		}
	}.startTask();
	f.waitUntilClosed();
}
private static Lock repaintScreenLock = new Lock();
static Boolean repainting(Graphics g,Boolean value)
{
	//
	// value == null indicates that we are starting the repainting.
	//
	if (value == null){
		if (g != null && g.getSurfaceType() != ISurface.WINDOW_SURFACE) return Boolean.FALSE;
		try{
			repaintScreenLock.synchronize();
			return Boolean.TRUE;
		}catch(IllegalThreadStateException e){
			return null;
		}
		//
		// Value is not null but is FALSE indicates that we were allowed to paint but
		// we did not need to acquire the lock.
		//
	}else if (!value.booleanValue()){
		return null;
		//
		// Value is not null but is TRUE indicates that we were allowed to paint and
		// we did acquire the lock.
		//
	}else{
		repaintScreenLock.release();
		return null;
	}
}
//static{setStyle(STYLE_ETCHED);}
//##################################################################
}
//##################################################################

