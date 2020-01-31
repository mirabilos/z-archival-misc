/* $MirOS: contrib/hosted/ewe/extrajdk/ewe/ui/Window.java,v 1.2 2008/05/02 20:52:04 tg Exp $ */

/*********************************************************************************
 *  Ewe Virtual Machine - Version 1.14, January 24, 2002                         *
 *  Copyright (c) 2008 Thorsten “mirabilos” Glaser                               *
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

import ewe.fx.*;
import ewe.sys.*;

import ewe.applet.Applet;
import ewe.applet.WinCanvas;


/**
 * Window is a "floating" top-level window. This class is not functional
 * enough to be used for dialogs and other top-level windows, it
 * currently exists only as a base class for the MainWindow class.
 */

public class Window extends Container implements ISurface, WindowConstants, ewe.data.HasProperties
{
public WindowCreationData creationData = new WindowCreationData();
public static ewe.util.WeakSet all = new ewe.util.WeakSet();

public WinCanvas _winCanvas;
public java.awt.Window jWindow;
protected static KeyEvent _keyEvent = new KeyEvent();
protected static PenEvent _penEvent = new PenEvent();
protected static ControlEvent _controlEvent = new ControlEvent();
boolean needsPaint;
int paintX, paintY, paintWidth, paintHeight;
Graphics _g;
Control _focus;
static boolean _inPenDrag, _inMouseMove, _inMouseSize;
private ewe.data.PropertyList properties = new ewe.data.PropertyList();

public ewe.data.PropertyList getProperties() {return properties;}
Image savedImage;
/** Constructs a window. */
/*
public Window()
	{
	_nativeCreate();
	}

private native void _nativeCreate();
*/

public Frame contents = new Frame();

//Used by GUI.
ewe.util.Vector frames = new ewe.util.Vector();

static int curWindow = 0;

protected int myFlags = FLAG_IS_VISIBLE|FLAG_HAS_TITLE|FLAG_HAS_CLOSE_BUTTON|FLAG_CAN_RESIZE|FLAG_CAN_MAXIMIZE|FLAG_CAN_MINIMIZE;

public Window()
{
	all.add(this);
	if (globalDrawFlat) modify(DrawFlat,0);
	name = "Window: "+(++curWindow);
}
static ewe.util.Vector openWindows = new ewe.util.Vector();
Window containing;
public boolean canDisplay = false, firstDisplay = false;

/**
This returns whether the system is within a native input or native dialog.
If this is the case, then no Gui input will be sent to the Ewe Gui system.
**/
//===================================================================
public static boolean inNativeInput(){return false;}
//===================================================================
/**
 * This starts a native text input for a control.
 * @param tip the TextInputParameters to use.
 * @return true if the input started successfully, false if it could not start because
 * another native input is already active.
 * @throws NoSuchMethodError if native text input is not possible at all.
 */
//===================================================================
public boolean textInput(final TextInputParameters tip)
throws NoSuchMethodError
//===================================================================
{
	throw new NoSuchMethodError();
}
/**
* This gets all the open windows, wether they are visible or not.
**/
//===================================================================
public static ewe.util.Iterator getOpenWindows()
//===================================================================
{
	return openWindows.iterator();
}

/**
This returns true if the Window has been created (its native component has been created)
and has not been closed.
**/
//===================================================================
public boolean isCreated()
//===================================================================
{
	for (int i = 0; i<openWindows.size(); i++)
		if (openWindows.get(i) == this) return true;
	return false;
}
/**
* If this Window is contained in another Window, this will return the containing Window. Calling getParent() on
* a Window should always return null.
**/
//===================================================================
public Window getContainingWindow()
//===================================================================
{
	return containing;
}

//===================================================================
public boolean create(final Rect location,final String title,final int flagsToSet,final int flagsToClear,final Object extra)
//===================================================================
{
	text = title;
	if (text == null) text = "";
	boolean ret = new ewe.sys.MessageThreadTask(){
		protected boolean doTask(Object data){
			add(contents);
			contents.make(false);
			windowThread.making(Window.this);
			WindowCreationData wcd = (extra instanceof WindowCreationData) ?
				(WindowCreationData)extra : null;
			if (wcd != null) {
				containing = (Window)wcd.parentWindow;
				wcd.setup();
			}
			createNativeWindow(location,title,flagsToSet,flagsToClear,wcd);
			firstDisplay = true;
			openWindows.add(Window.this);
			if ((flagsToClear & FLAG_IS_VISIBLE) == 0){
				activeWindow = Window.this;
				checkAppKeys();
			}
			return true;
		}
	}.execute(null);
	if ((flagsToSet & FLAG_IS_MODAL) != 0)
		new windowThread(this,true);
	return ret;
}

//===================================================================
protected static Dimension getScreenSize()
//===================================================================
{
	java.awt.Dimension d = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
	return new Dimension(d.width,d.height);
}
//-------------------------------------------------------------------
protected int defaultValue(int value,int def)
//-------------------------------------------------------------------
{
	return (value <= 0) ? def : value;
}
private void setEweWindowIcon()
{
	setWindowIcon(new Image("ewe/ewesmall.bmp").toIcon(null));
}
private boolean setWindowIcon(Object what)
{
	if (what instanceof java.awt.Image){
		if (jWindow instanceof java.awt.Frame)
			((java.awt.Frame)jWindow).setIconImage((java.awt.Image)what);
		return true;
	}
	return false;
}
//-------------------------------------------------------------------
protected boolean createNativeWindow(Rect location,String title,int flagsToSet,int flagsToClear,Object extra)
//-------------------------------------------------------------------
{
	x = 0;
	y = 0;
	width = Applet.currentApplet.width;
	height = Applet.currentApplet.height;
	myFlags |= flagsToSet;
	myFlags &= ~flagsToClear;
	if (title == null) myFlags &= ~FLAG_HAS_TITLE;
	if (location == null) location = new Rect(-1,-1,-1,-1);
	Window parent = extra instanceof Window ? (Window)extra : null;
	/*
	if (Applet.currentApplet.mainWindow == null)
		jWindow = Applet.getDisplayed();
	else
		{
		java.awt.Dialog dialog = new java.awt.Dialog((java.awt.Frame)null);
		dialog.setLayout(new java.awt.BorderLayout());
		dialog.setSize(width, height);
		jWindow = dialog;
		// NOTE: testing has shown under some JavaVMs show() will
		// cause onPaint() to be called immediately and the
		// winCanvas below may not get initially painted
		dialog.show();
		}
	*/
	if (this instanceof mApp)
		if (Applet.shouldUseFrame){
			if (true || ((myFlags & FLAG_HAS_TITLE) != 0)) {
				jWindow = new ewe.applet.Frame(false);
				((ewe.applet.Frame)jWindow).window = this;
				((ewe.applet.Frame)jWindow).setTitle(title);
				((ewe.applet.Frame)jWindow).setResizable(((myFlags & FLAG_CAN_RESIZE) != 0));
			}else {
		 		jWindow = new ewe.applet.JWindow(false,title,parent);
				((ewe.applet.JWindow)jWindow).window = this;
		 		//jWindow = new java.awt.Window(new java.awt.Frame(title));
				myFlags &= ~FLAG_HAS_CLOSE_BUTTON;
			}
			Applet.currentApplet.frame = jWindow;
			_winCanvas = new WinCanvas(this);
			jWindow.add(_winCanvas);
			Rect r = new Rect(-1,-1,-1,-1);
			if (jWindow instanceof java.awt.Window){
				Dimension d = getScreenSize();
				//System.out.println("Dim: "+Applet.mainWidth+", "+Applet.mainHeight+", "+Applet.currentApplet.width+Applet.currentApplet.height);
				//System.out.println("Location: "+location);
				if (Applet.mainWidth != 0) d.width = Applet.mainWidth;
				if (Applet.mainHeight != 0) d.height = Applet.mainHeight;
				if (location != null){
					if (location.width == -1/* && mApp.mainApp == this*/)
						location.width = supportsMultiple() || Applet.mainWidth == 0 || Applet.mainHeight == 0 ? defaultValue(Applet.currentApplet.width,240) : d.width;
					if (location.width > 0) r.width = location.width;
					if (location.height == -1/* && mApp.mainApp == this*/)
						location.height = supportsMultiple() || Applet.mainWidth == 0 || Applet.mainHeight == 0 ? defaultValue(Applet.currentApplet.height, 320) : d.height;
					if (location.height > 0) r.height = location.height;
					if (r.width > d.width) r.width = r.height = -1;
					if (r.height > d.height) r.height = r.width = -1;
					r.x = location.x;
					r.y = location.y;
				}
				if (d.width >= 640 && d.height >= 480) d.width = d.height = 0;
				if (r.width == -1) r.width =  defaultValue(d.width,240);
				if (r.height == -1) r.height = defaultValue(d.height,320);
				d = getScreenSize();
				if (r.x < 0) r.x = (d.width-r.width)/2;
				if (r.y < 0) r.y = (d.height-r.height)/2;
				if (ewe.sys.Vm.getParameter(ewe.sys.Vm.SIMULATE_SIP) != 0)
					r.x = r.y = 0;
				jWindow.setBounds(r.x,r.y,r.width,r.height);
				_winCanvas.preferredSize = new java.awt.Dimension(r.width,r.height);
				width = r.width; height = r.height;
				((java.awt.Window)jWindow).pack();
				if ((myFlags & FLAG_IS_VISIBLE) != 0)
					((java.awt.Window)jWindow).show();
			}
		}else{
			myFlags = FLAG_IS_VISIBLE;
			_winCanvas = new WinCanvas(this);
			_winCanvas.preferredSize = new java.awt.Dimension(Applet.currentApplet.getSize().width,Applet.currentApplet.getSize().height);
			Applet.getDisplayed().add(_winCanvas);
			appResized(_winCanvas.preferredSize.width,_winCanvas.preferredSize.height,0);
			_winCanvas.repaint();
		}
	else{ //Not an mApp
			boolean modal = ((myFlags & FLAG_IS_MODAL) != 0);
			if (true || ((myFlags & FLAG_HAS_TITLE) != 0)) {
				jWindow = new ewe.applet.Frame(modal);
				((ewe.applet.Frame)jWindow).window = this;
				((ewe.applet.Frame)jWindow).setTitle(title);
				((ewe.applet.Frame)jWindow).setResizable(((myFlags & FLAG_CAN_RESIZE) != 0));
			}else {
		 		jWindow = new ewe.applet.JWindow(modal,title,parent);
				((ewe.applet.JWindow)jWindow).window = this;
				//((ewe.applet.JWindow)jWindow).setResizable(true);
				myFlags &= ~FLAG_HAS_CLOSE_BUTTON;
			}
			_winCanvas = new WinCanvas(this);
			jWindow.add(_winCanvas);
			Rect r = new Rect(-1,-1,width,height);
			if (jWindow instanceof java.awt.Window){
				Dimension d = getScreenSize();
				if (Applet.mainWidth != 0) d.width = Applet.mainWidth;
				if (Applet.mainHeight != 0) d.height = Applet.mainHeight;
				if (location != null){
					if (location.width > 0) r.width = location.width;
					if (location.height > 0) r.height = location.height;
					if (r.width > d.width) r.height = r.width = -1;
					if (r.height > d.height) r.height = r.width = -1;
					r.x = location.x;
					r.y = location.y;
				}
				if (d.width >= 640 && d.height >= 480) d.width = d.height = 0;
				if (r.width == -1) r.width =  defaultValue(d.width,240);
				if (r.height == -1) r.height = defaultValue(d.height,320);
				d = getScreenSize();
				if (r.x < 0) r.x = (d.width-r.width)/2;
				if (r.y < 0) r.y = (d.height-r.height)/2;
				if (ewe.sys.Vm.getParameter(ewe.sys.Vm.SIMULATE_SIP) != 0)
					r.x = r.y = 0;
				jWindow.setBounds(r.x,r.y,r.width,r.height);
				_winCanvas.preferredSize = new java.awt.Dimension(r.width,r.height);
				width = r.width; height = r.height;
				((java.awt.Window)jWindow).pack();
				if ((myFlags & FLAG_IS_VISIBLE) != 0)
					((java.awt.Window)jWindow).show();
			}
	}
	ewe.applet.Applet.windows.addElement(_winCanvas);
	_winCanvas.requestFocus();
	if (Applet.tempF != null && this instanceof mApp) Applet.tempF.setVisible(false);
	//if ((myFlags & FLAG_IS_VISIBLE) != 0){
//		while(!((ewe.applet.EweWindow)jWindow).wasShown()) ewe.sys.Vm.sleep(100);
	//}
	boolean didWindowIcon = false;
	if (extra instanceof WindowCreationData){
		WindowCreationData wcd = (WindowCreationData)extra;
		didWindowIcon = setWindowIcon(wcd.nativeWindowIcon);
	}
	if (!didWindowIcon) setEweWindowIcon();
	return true;
	}
public java.awt.Graphics createAWTGraphics()
	{
	return _winCanvas.getGraphics();
	}

//===================================================================
public boolean close()
//===================================================================
{
	_focus = null;
	if (lastWindow == _winCanvas) lastWindow = null;
	if (activeWindow == this) activeWindow = null;
	if (Gui.sipWindow == this) Gui.sipWindow = null;
	openWindows.remove(this);
	windowThread.closing(Window.this);
	ewe.applet.Applet.windows.removeElement(_winCanvas);
	shutdown();
	if (jWindow != null) jWindow.setVisible(false);
	canDisplay = false;
	return true;
}
//===================================================================
protected void shutdownAll()
//===================================================================
{
	for (int i = 0; i<openWindows.size(); i++)
		try{
			((Window)openWindows.get(i)).shutdown();
		}catch(Throwable t){}
}

/**
 * This will attempt to bring the topmost window of the application to the front.
 * @return true if successful.
 */
//===================================================================
public static boolean applicationToFront()
//===================================================================
{
	if (windowThread.eventThread != null){
		if (windowThread.eventThread.modalWindow != null)
			return windowThread.eventThread.modalWindow.toFront();
		else for (int i = openWindows.size()-1; i>= 0; i--){
			Window w = (Window)openWindows.get(i);
			if ((w.getWindowFlags() & FLAG_IS_VISIBLE) == 0) continue;
			return w.toFront();
		}
		if (MainWindow._mainWindow != null)
			MainWindow._mainWindow.toFront();
	}
	return false;
}
//===================================================================
public boolean toFront()
//===================================================================
{
	if ((getWindowFlags() & FLAG_IS_VISIBLE) != 0)
			{
				if (getState() == STATE_MINIMIZED)
					setState(STATE_NORMAL);
			}
	if (jWindow != null) jWindow.toFront();
	activeWindow = this;
	return true;
}

/**
* This returns the currently active window.
**/
//===================================================================
public static Window getActiveWindow()
//===================================================================
{
	return activeWindow;
}

/**
* This is the Window of the application that is currently active.
**/
private static Window activeWindow;
private static boolean capturedKeys = false;
private static void captureAppKeys(int options,Window win){}
/**
* This will capture the WindowCE special App keys (the non-cursor buttons generally
* found on the bottom). Calling this with options set to 1 will cause WinCE to
* not capture the App keys as ordinary keypresses, instead of special application
* launch buttons. These keypresses are then passed via a normal key event to your
* application with the APP0 to APP15 key codes. Calling this with options set to 0 will
* cause WinCE to no longer capture these keys and they will be re-associated with
* their application launch functionality.
* @param options A value of 1 causes WinCE to capture the keys. A value of 0 cause WinCE to
* release the keys.
* @return The same value as options.
*/
//===================================================================
public static int captureAppKeys(int options)
//===================================================================
{
	if (options != 0) capturedKeys = true;
	else capturedKeys = false;
	captureAppKeys(options, capturedKeys ? activeWindow : null);
	return options;
}
//-------------------------------------------------------------------
private static void checkAppKeys()
//-------------------------------------------------------------------
{
	if (capturedKeys) captureAppKeys(1,activeWindow);
}


//===================================================================
protected void shutdown(){}
//===================================================================
/**
 * Sets focus to the given control. When a user types a key, the control with
 * focus get the key event. At any given time, only one control in a window
 * can have focus. Calling this method will cause a FOCUS_OUT control event
 * to be posted to the window's current focus control (if one exists)
 * and will cause a FOCUS_IN control event to be posted to the new focus
 * control.
 */
public void setFocus(Control c)
	{
	/*
	if (_focus != null)
		{
		_controlEvent.type = ControlEvent.FOCUS_OUT;
		_controlEvent.target = _focus;
		_focus.postEvent(_controlEvent);
		}
		*/
	_focus = c;
	/*
	if (c != null)
		{
		_controlEvent.type = ControlEvent.FOCUS_IN;
		_controlEvent.target = c;
		c.postEvent(_controlEvent);
		}
		*/
	}

/**
 * Returns the focus control for this window.
 * @see ewe.ui.Window#setFocus
 */
public Control getFocus()
	{
	return _focus;
	}

/**
 * Adds a damage rectangle to the current list of areas that need
 * repainting.
 */
protected void damageRect(int x, int y, int width, int height)
	{
	if (needsPaint)
		{
		int ax = x + width;
		int ay = y + height;
		int bx = paintX + paintWidth;
		int by = paintY + paintHeight;
		if (paintX < x)
			x = paintX;
		if (paintY < y)
			y = paintY;
		if (ax > bx)
			width = ax - x;
		else
			width = bx - x;
		if (ay > by)
			height = ay - y;
		else
			height = by - y;
		}
	paintX = x;
	paintY = y;
	paintWidth = width;
	paintHeight = height;
	needsPaint = true;
	}

//MLB
protected static SIPEvent _sipEvent = new SIPEvent();
//-------------------------------------------------------------------
static void dragOverNew(Control newControl)
//-------------------------------------------------------------------
{
	if (lastDraggedOver != newControl)
		if (lastDraggedOver != null)
			lastDraggedOver.dataDraggedOff(dragAndDropData);
	lastDraggedOver = newControl;
}
static Control lastDraggedOver;

public static Object dragAndDropData;
public static int dragAndDropCursor;
public static Object dragAndDropSource;

//===================================================================
public void releaseMouseCapture(){}
//===================================================================

//===================================================================
public boolean beginDragAndDrop(Control source,Object data,int dragCursor)
//===================================================================
{
	dragAndDropData = data;
	dragAndDropCursor = dragCursor;
	dragAndDropSource = source;
	releaseMouseCapture();
	return true;
}

//===================================================================
public boolean beginDragAndDrop(Control source,Object data,boolean isMultiple,boolean isCopy)
//===================================================================
{

	return beginDragAndDrop(source,data,
		isMultiple ?
			isCopy ?
				ewe.sys.Vm.COPY_MULTIPLE_CURSOR : ewe.sys.Vm.DRAG_MULTIPLE_CURSOR
		:
			isCopy ?
				ewe.sys.Vm.COPY_SINGLE_CURSOR : ewe.sys.Vm.DRAG_SINGLE_CURSOR);
}

Point mouseOffset = null;
static Point dragxy = new Point();
java.awt.Rectangle myBounds = null;

protected static WinCanvas lastWindow = null;

//-------------------------------------------------------------------
protected Window tryWindow(WinCanvas cv,int x,int y,Point where)
//-------------------------------------------------------------------
{
	if (cv == null || mouseOffset == null) return null;
	java.awt.Window win = cv.win.jWindow;
	if (win == null) return null;
	int absX = x+mouseOffset.x;
	int absY = y+mouseOffset.y;
	java.awt.Rectangle wr = win.getBounds();
	if (wr.contains(absX,absY)){
		int cx = absX-wr.x, cy = absY-wr.y;
		java.awt.Insets in = win.getInsets();
		cx -= in.left; cy -= in.top;
		Window other = null;
		if (win instanceof ewe.applet.JWindow) other = ((ewe.applet.JWindow)win).window;
		else other = ((ewe.applet.Frame)win).window;
		Control target = other.findChild(cx,cy);
		if (target != other && target != null){
			if (cv != lastWindow) win.toFront();
			lastWindow = cv;
			where.x = cx; where.y = cy;
			return other;
		}
	}
	return null;
}

public static Point downPoint = new Point();

public static boolean inEventThread = false;
//===================================================================
public void transferPenPress(PenEvent ev,Control toWho,int dx,int dy)
//===================================================================
{
	_focus = toWho;
	Control.firstPress = true;
	ev.x += dx;
	ev.y += dy;
	ev.modifiers |= ev.TRANSFERRED_PRESS;
	toWho.postEvent(ev);
}
//-------------------------------------------------------------------
void makeModal()
//-------------------------------------------------------------------
{
	new windowThread(this,true);
}
//-------------------------------------------------------------------
void closeModal()
//-------------------------------------------------------------------
{
	windowThread.closing(this);
}
private static boolean firstStart = true;
//-------------------------------------------------------------------
public static void restart()
//-------------------------------------------------------------------
{
	//eventThread.exitAll(); - has not been tested, better not to use it.
	try{
		if (!firstStart) {
			//ewe.sys.Vm.debug("Restarting!");
			windowThread.events = null;
			windowThread.eventThread = new windowThread(null,false);
		}
	}finally{
		firstStart = false;
	}
}
static int nativeDialogCount = 0;

public static void enterNativeDialog()
{
	synchronized(ewe.sys.Vm.getSyncObject()){
		nativeDialogCount++;
	}
}
public static void exitNativeDialog()
{
	synchronized(ewe.sys.Vm.getSyncObject()){
		if (nativeDialogCount > 0)
			nativeDialogCount--;
	}
}

	//##################################################################
	static class windowThread implements Runnable{
	//##################################################################
	Window modalWindow = null;
	boolean modalMade = false;
	windowThread previousThread;
	//Coroutine myCoroutine;
	mThread myThread;
	boolean shouldStop = false;
	//boolean waitingOnEvent = false;
	ewe.sys.Lock waitEventLock = new ewe.sys.Lock();
	//-------------------------------------------------------------------
	void wakeup()
	//-------------------------------------------------------------------
	{
		waitEventLock.holdAndNotify(TimeOut.Forever,true);
	}

	//static Object lock = Applet.uiLock;//new Object();
	static windowThread eventThread = new windowThread(null,false);
	static queuedEvent events;

	//##################################################################
	static class queuedEvent{
	//##################################################################

	int type;
	int key;
	int x;
	int y;
	int modifiers;
	int timeStamp;
	queuedEvent next;
	Window window;

	/**
	* Note that this is called OUTSIDE of the COROUTINE thread! Must be very carefull with synchronizing it!
	**/
	//===================================================================
	public queuedEvent(Window window,int type, int key, int x, int y, int modifiers, int timeStamp)
	//===================================================================
	{
		this.type = type;
		this.key = key;
		this.x = x;
		this.y = y;
		this.modifiers = modifiers;
		this.timeStamp = timeStamp;
		this.window = window;
		if (events == null) events = this;
		else{
			queuedEvent e = events;
			for (;e.next != null; e = e.next);
			e.next = this;
		}
		//ewe.sys.Vm.debug("queuedEvent!");
		//synchronized(lock){
			if (eventThread != null){
				//ewe.sys.Vm.debug("Wake: "+eventThread.hashCode());
				eventThread.wakeup();
			}else{
				//ewe.sys.Vm.debug("No thread!");
			}
			/*
				if (eventThread.waitingOnEvent)
						eventThread.myCoroutine.wakeup();
			*/
		//}
	}

	//##################################################################
	}
	//##################################################################

	//===================================================================
	public windowThread(Window newModalWindow,boolean made)
	//===================================================================
	{
			modalWindow = newModalWindow;
			previousThread = eventThread;
			modalMade = made;
			eventThread = this;
			//myCoroutine = new Coroutine(this);
			myThread = new mThread(this);
			myThread.start();
	}

	//===================================================================
	public static void making(Window win)
	//===================================================================
	{
			if (eventThread != null)
				if (eventThread.modalWindow == win)
					eventThread.modalMade = true;
	}
	//===================================================================
	public static void closing(Window win)
	//===================================================================
	{
		for (windowThread wt = eventThread; wt != null; wt = wt.previousThread)
			if (wt.modalWindow == win){
				if (wt == eventThread) wt.exit();
				else wt.shouldStop = true;
				break;
			}
	/*
			if (eventThread != null)
				if (eventThread.modalWindow == win)
					eventThread.exit();
	*/
	}
	//===================================================================
	public void exitAll()
	//===================================================================
	{
		if (previousThread != null)
			previousThread.exitAll();
		previousThread = null;
		shouldStop = true;
		wakeup();
	}
	//===================================================================
	public void exit()
	//===================================================================
	{
			shouldStop = true;
			wakeup();//if (waitingOnEvent) myCoroutine.wakeup();
			if (eventThread == this) {
				eventThread = previousThread;
				if (eventThread == null) eventThread = new windowThread(null,false);
				if (eventThread.modalWindow != null)
					applicationToFront();
			}
	}

	//===================================================================
	public void run()
	//===================================================================
	{
		if (Gui.setSipTo != -1) {
			ewe.sys.Vm.setSIP(Gui.setSipTo);
			Gui.setSipTo = -1;
		}
		//myCoroutine = Coroutine.getCurrent();
		myThread = mThread.currentThread();
		try{
			//for(;!shouldStop;Coroutine.sleep(-1)){
			while(!shouldStop){
				//waitingOnEvent = false;
				waitEventLock.synchronize(); try{
					if (events == null || eventThread != this){
						//ewe.sys.Vm.debug("Sleep: "+hashCode());
						waitEventLock.wait(TimeOut.Forever);
						//ewe.sys.Vm.debug("Awake: "+hashCode());
					}
				/*
				}catch(Error e){
					e.printStackTrace();
					throw e;
				}catch(RuntimeException re){
					re.printStackTrace();
					throw re;
				*/
				}finally{
					//ewe.sys.Vm.debug("Out:"+hashCode());
					waitEventLock.release();
				}
				if (eventThread != this) continue;
				try{
					if (shouldStop) break;
					for(queuedEvent e = events; e != null; e = events){
						try{
							events = e.next;
							e.next = null;
							if (nativeDialogCount > 0) continue;
							if (modalWindow != null && modalWindow != e.window){
								int type = e.type;
								//if (type == PenEvent.PEN_UP || type == PenEvent.PEN_DOWN) ewe.sys.Vm.debug(type+" - out of modal!");
								if (modalMade && (type == PenEvent.PEN_UP || type == PenEvent.PEN_DOWN || type == PenEvent.PEN_MOVE || type == WindowEvent.CLOSE)){
										if ((activeWindow != modalWindow) || (type == PenEvent.PEN_DOWN)){
											//ewe.sys.Vm.debug("To front!");
											//ewe.sys.Vm.debug(modalWindow.toString());
											modalWindow.toFront();
										}
								}
								if (type == WindowEvent.ACTIVATE || type == WindowEvent.DEACTIVATE){
									e.window.doPostEvent(e.type,e.key,e.x,e.y,e.modifiers,e.timeStamp);
									if (type == WindowEvent.ACTIVATE)
										modalWindow.repaintNow();
								}
								if (type == SIPEvent.SIP_SHOWN || type == SIPEvent.SIP_HIDDEN)
									modalWindow.doPostEvent(e.type,e.key,e.x,e.y,e.modifiers,e.timeStamp);
								continue;
							}
							e.window.doPostEvent(e.type,e.key,e.x,e.y,e.modifiers,e.timeStamp);
							if (eventThread != this || shouldStop){
								//if (!shouldStop) waitingOnEvent = true;
								break;
							}
						}finally{
							Gui.setSipTo = -1;
							ewe.sys.Vm.freezeSIP(false,0,modalWindow);
						}
					}
					if (shouldStop) break;
					//waitingOnEvent = true;
				}catch(Throwable t){
					ewe.sys.Vm.debug(ewe.sys.Vm.getStackTrace(t,5));
					t.printStackTrace();
					//waitingOnEvent = true;
				}
			}
		}finally{
			if (eventThread == this) {
				eventThread = previousThread;
				if (eventThread != null) eventThread.wakeup();
				//if (eventThread.waitingOnEvent)
					//eventThread.myThread.wakeup();
			}
		/*
				if (eventThread == this) {
					eventThread = previousThread;
					if (eventThread == null) eventThread = new windowThread(null,false);
					else if (eventThread.waitingOnEvent)
						eventThread.myCoroutine.wakeup();
				}
		*/
		}
	}

	//##################################################################
	}
	//##################################################################
//===================================================================
public static boolean nextMouseIsMove()
//===================================================================
{
	if (windowThread.events == null) return false;
	return (windowThread.events.type == PenEvent.PEN_MOVE);
}
//===================================================================
public static void clearQueuedMoves()
//===================================================================
{
	while(windowThread.events != null){
		if (windowThread.events.type == PenEvent.PEN_MOVE){
			windowThread.queuedEvent n = windowThread.events.next;
			boolean quit = n == null;
			if (!quit)
				if (n.type != PenEvent.PEN_MOVE)
					quit = true;
			if (quit) break;
			windowThread.events = windowThread.events.next;
		}else
			break;
	}
}

/**
 * This is called when a WindowEvent occurs on a mobile device.
	This can be used to trap, say a WindowEvent.CLOSE message from the mobile OS and allow
	for the application to save its state. This is needed because WinCE will automatically close
	applications if memory starts to run low. It will also forcibly terminate the application if
	it does not exit immediately. By default this will call the closeMobileApp() of the main mApp
	of the application if the message is a CLOSE message.
 */
//-------------------------------------------------------------------
protected void mobileWindowEvent(int type,int key,int x,int y,int modifiers,int timeStamp)
//-------------------------------------------------------------------
{
	if (type == WindowEvent.CLOSE){
		if (mApp.mainApp != null)
			mApp.mainApp.closeMobileApp(this,modifiers);
	}
}
/**
* This is called directly by the VM within a native Window Message handler. It will
* be called if the icon placed by the window is pressed. By default this brings the application to the front.
**/
//===================================================================
public void iconPressed() {applicationToFront();}
//===================================================================
/**
* This is called directly by the VM within a native Window Message handler. It is therefore
* in the same thread as the message queue. This will then queue the event in the Coroutine
* responsible for dispatching events. However if the event is a WindowEvent type, and the
* application is a mobile application, it will also call mobileWindowEvent() before queueing
* the event.
**/
//===================================================================
public void _postEvent(int type, int key, int x, int y, int modifiers, int timeStamp)
//===================================================================
{
	//ewe.sys.Vm.debug("Posting: "+type);
	if (type == WindowEvent.ACTIVATE){
		//ewe.sys.Vm.debug("Active: "+name);
		activeWindow = this;
		Frame.checkCurrentSip();
		checkAppKeys();
		if (_focus != null)
			if (_focus.hasModifier(ShowSIP,false) && ((getWindowFlags() & FLAG_SHOW_SIP_BUTTON) == 0))
				ewe.sys.Vm.setSIP(2,this);
	}
	if (type >= WindowEvent.CLOSE && type <= WindowEvent.HIBERNATE)
		if ((ewe.sys.Vm.getParameter(ewe.sys.Vm.VM_FLAGS) & ewe.sys.Vm.VM_FLAG_IS_MOBILE) != 0)
			synchronized(Applet.uiLock){
				WinCanvas.inCallback = true;
				mobileWindowEvent(type,key,x,y,modifiers,timeStamp);
				WinCanvas.inCallback = false;
			}
	//synchronized(windowThread.lock){
		new windowThread.queuedEvent(this,type,key,x,y,modifiers,timeStamp);
	//}
}

private boolean inKeyPress;

//===================================================================
public void doPostEvent(int type, int key, int x, int y, int modifiers, int timeStamp)
//===================================================================
{
		inEventThread = true;
		Gui.setSipTo = -1;
		Event event = null;
		Control target = _focus;
	try{
		if (target == null) target = this;
		if (type != PenEvent.PEN_MOVE) PenEvent.removeTip();
		if (type == WindowEvent.CLOSE || type == WindowEvent.ACTIVATE || type == WindowEvent.DEACTIVATE){
			target = this;
			ewe.ui.WindowEvent we = new ewe.ui.WindowEvent();
			we.type = type;
			event = we;
			event.window = this;
		}else
		if (type == KeyEvent.KEY_PRESS || type == KeyEvent.KEY_RELEASE)
			{
			mApp.lastEvent = ewe.sys.Vm.getTimeStamp();
			_keyEvent.type = type;
			_keyEvent.key = key;
			_keyEvent.modifiers = modifiers;
			if (type == KeyEvent.KEY_PRESS)
				if (inKeyPress)
					_keyEvent.modifiers |= IKeys.REPEATED;
				else
					inKeyPress = true;
			else
				inKeyPress = false;
			event = _keyEvent;
			event.window = this;
			if (_focus == null)
				_focus = this;
			if (_keyEvent.key == IKeys.SOFTKEY1 || _keyEvent.key == IKeys.SOFTKEY2){
				if (SoftKeyBar.onKeyEvent(_keyEvent)) return;
			}
			}

		//MLB
		else if (type == _sipEvent.SIP_SHOWN || type == _sipEvent.SIP_HIDDEN){
			_sipEvent.type = type;
			_sipEvent.visibleWidth = type == _sipEvent.SIP_SHOWN ? x : width;
			_sipEvent.visibleHeight = type == _sipEvent.SIP_SHOWN ? y : height;
			event = _sipEvent;
			event.window = this;
			if (type == _sipEvent.SIP_SHOWN) {
				_sipEvent.handlingSipOn = true;
				firstResizeLock.synchronize(); try{
					while(true){
						if (gotFirstResize) break;
						try{
							firstResizeLock.waitOn(1000);
						}catch(InterruptedException e){}
						gotFirstResize = true;
						break;
					}
				}finally{
					firstResizeLock.release();
				}
			}
			handleSipEvent(_sipEvent);
			_sipEvent.handlingSipOn = false;
			return;
		}
		//END MLB
		else
			{
			_penEvent.window = this;
			// set focus to new control
			if (type == PenEvent.PEN_DOWN)
				{
				downPoint.set(x,y);
				lastWindow = _winCanvas;
				if (jWindow != null) {
					java.awt.Insets in = jWindow.getInsets();
					mouseOffset = new Point(in.left,in.top);
					myBounds = jWindow.getBounds();
					mouseOffset.x += myBounds.x;
					mouseOffset.y += myBounds.y;
				}
				mApp.lastEvent = ewe.sys.Vm.getTimeStamp();
				Control c = findChild(x, y);
				target = c;
				if (c != _focus && !c.hasModifier(Disabled,true))
					setFocus(c);
				_inPenDrag = true;
				}
			else if (type == PenEvent.PEN_MOVE){
				if (_inMouseMove || _inMouseSize){
					return;
				}
				if (_inPenDrag){
					type = PenEvent.PEN_DRAG;
					if (dragAndDropData != null){
						if ((modifiers & PenEvent.FROM_OTHER_WINDOW) != 0){
							target = findChild(x,y);
						}else{
							Window shouldGet = tryWindow(lastWindow,x,y,dragxy);
							if (shouldGet == this){
								target = findChild(x,y);
							}else if (shouldGet != null){
								shouldGet.doPostEvent(PenEvent.PEN_MOVE,key,dragxy.x,dragxy.y,modifiers|PenEvent.FROM_OTHER_WINDOW,timeStamp);
								return;
							}else{
								for (int i = 0; i<Applet.windows.size(); i++){
									WinCanvas win = (WinCanvas)Applet.windows.elementAt(i);
									if (win == lastWindow) continue;
									if (!win.getParent().isVisible()) continue;
									shouldGet = tryWindow(win,x,y,dragxy);
									if (shouldGet == this){
										target = findChild(x,y);
										break;
									}else if (shouldGet != null){
										shouldGet.doPostEvent(PenEvent.PEN_MOVE,key,dragxy.x,dragxy.y,modifiers|PenEvent.FROM_OTHER_WINDOW,timeStamp);
										return;
									}
								}
								target = this;
							}
						}
					}
				}else {
					_penEvent.timeStamp = timeStamp;
					_penEvent.modifiers = modifiers;
					_penEvent.type = type;
					PenEvent.handlePenMove(this,_penEvent,x,y);
					return;
				}
			}else if (type == PenEvent.PEN_UP){
				Control.firstPress = true;
				if (_inMouseMove||_inMouseSize){
					PenEvent.resetCursor();
					int dx = x-downPoint.x;
					int dy = y-downPoint.y;
					if (_inMouseMove){
						Rect r = getWindowRect(new Rect(),false);
						r.x += dx;
						r.y += dy;
						setWindowRect(r,false);
					}else{
						if (x >= 4 && y >= 4){
							Rect r = getWindowRect(new Rect(),true);
							r.width = x;
							r.height = y;
							setWindowRect(r,true);
						}
					}
					_inMouseMove = _inMouseSize = false;
					return;
				}
				if (_inPenDrag){
					if (dragAndDropData != null){
						if ((modifiers & PenEvent.FROM_OTHER_WINDOW) != 0){
							target = findChild(x,y);
						}else{
							Window shouldGet = tryWindow(lastWindow,x,y,dragxy);
							if (shouldGet == this){
								target = findChild(x,y);
							}else if (shouldGet != null){
								shouldGet.doPostEvent(PenEvent.PEN_UP,key,dragxy.x,dragxy.y,modifiers|PenEvent.FROM_OTHER_WINDOW,timeStamp);
								return;
							}else{
								for (int i = 0; i<Applet.windows.size(); i++){
									WinCanvas win = (WinCanvas)Applet.windows.elementAt(i);
									if (win == lastWindow) continue;
									if (!win.getParent().isVisible()) continue;
									shouldGet = tryWindow(win,x,y,dragxy);
									if (shouldGet == this){
										target = findChild(x,y);
										break;
									}else if (shouldGet != null){
										shouldGet.doPostEvent(PenEvent.PEN_UP,key,dragxy.x,dragxy.y,modifiers|PenEvent.FROM_OTHER_WINDOW,timeStamp);
										return;
									}
								}
								target = this;
							}
						}
					}
				}
				_inPenDrag = false;
				if (target == null || target == this)
					if (dragAndDropData != null){
						dragOverNew(null);
						((Control)dragAndDropSource).dataTransferCancelled(dragAndDropData);
						dragAndDropData = null;
						PenEvent.resetCursor();
					}
			}

			_penEvent.type = type;
			_penEvent.x = x;
			_penEvent.y = y;

			// translate x, y to coordinate system of target
			Control c = target;//_focus;
			while (c != null)
				{
				_penEvent.x -= c.x;
				_penEvent.y -= c.y;
				c = c.parent;
				}

			_penEvent.modifiers = modifiers;
			event = _penEvent;
			}
		event.target = target;//_focus;
		event.timeStamp = timeStamp;
		if (event.target != null){
			if (event == _keyEvent){
				if (_keyEvent.key == IKeys.PDA_CANCEL && _keyEvent.type == _keyEvent.KEY_PRESS && event.target != null){
					_keyEvent.key = IKeys.ESCAPE;
					if (!((Control)event.target).isSomeonesHotKey(_keyEvent))
					doPostEvent(WindowEvent.CLOSE,0,0,0,0,timeStamp);
					return;
				}
			}
			((Control)event.target).postEvent(event);
		}
		if (type == PenEvent.PEN_UP){
			setFocus(Gui.focusedControl());
			dragAndDropData = null;
		}
		if (needsPaint)
			_doPaint(paintX, paintY, paintWidth, paintHeight);
	}finally{
		if (event != null) {
			event.window = null;
			event.target = null;
		}
		inEventThread = false;
		if (Gui.setSipTo != -1) {
			//Vm.debug("Window Setting to: "+Gui.setSipTo);
			ewe.sys.Vm.setSIP(Gui.setSipTo);
		}else{
			//Vm.debug("Is -1");
		}
	}
	}

//-------------------------------------------------------------------
void dispatch(PenEvent ev)
//-------------------------------------------------------------------
{
	Control c = (Control)ev.target;
	while (c != null){
		ev.x -= c.x;
		ev.y -= c.y;
		c = c.parent;
	}
	if (ev.target != null)((Control)ev.target).postEvent(ev);
	if (needsPaint)
		_doPaint(paintX, paintY, paintWidth, paintHeight);
}
/**
* This gets set to be true when the VM calls _doPaint() on the Window.
**/
public boolean wasPainted = false;
Lock paintNotify;
/**
 * Wait until the Window receives its first Paint message.
 * This will only work correctly from an mThread.
 * @param timeOut The length of time in milliseconds to wait.
 * @return true if the paint message was received within the timeOut period, false if not.
 */
//===================================================================
public boolean waitUntilPainted(int timeOut)
//===================================================================
{
	if (wasPainted) return true;
	if (!ewe.sys.mThread.inThread()) return false;
	paintNotify = new ewe.sys.Lock();
	paintNotify.synchronize(); try{
		try{
			if (paintNotify.waitOn(timeOut))
				return true;
		}catch(Exception e){

		}
		return false;
	}finally{
		paintNotify.release();
		paintNotify = null;
	}
}
/**
 * Called by the VM to repaint an area.
 */
public void _doPaint(int x, int y, int width, int height)
	{
	repaintNow(null,new Rect(x,y,width,height));
	wasPainted = true;
	if (paintNotify != null)
 		if (paintNotify.grab())try{
			paintNotify.notifyAllWaiting();
		}finally{
			paintNotify.release();
		}
	return;
	}
//MLB
//
public int getMessage(ewe.sys.SystemMessage message,boolean peek,boolean remove)
{
	return _winCanvas.getMessage(message,peek,remove);
}
public int callBackInMessageThread(CallBack who,Object data)
{
	return _winCanvas.callBackInMessageThread(who,data);
}
public static boolean amInMessageThread()
{
	return WinCanvas.amInMessageThread();
}
public void doTimerTick()
{
	_winCanvas.postTimerMessage();
}
/**
* This method is used to get extended information about the Window or the GUI system in general.
* It is used with the INFO_ specifiers and options. It will return the resultDestination on success
* or null on failure.
* See interface WindowConstants for values for infoCode and options.
**/
//===================================================================
public Object getInfo(int infoCode,Object sourceParameter,Object resultDestination,int options)
//===================================================================
{
	if (infoCode == INFO_POSITION_IN_NATIVE_DRAWING_SURFACE){
		ewe.fx.Point p = new Point();
		Control c = (Control)sourceParameter;
		if (c.getWindow() != this) return null;
		Rect r = Gui.getRectInWindow((Control)sourceParameter,new Rect(),false,p);
		nativeGetInfo(infoCode,null,p,0);
		return p;
	}
	return nativeGetInfo(infoCode,sourceParameter,resultDestination,options);
}
/**
* This method is used to set extended information about the Window or the GUI system in general.
* It is used with the INFO_ specifiers and options. It will return true on success, false on failure.
* See interface WindowConstants for values for infoCode and options.
**/
//===================================================================
public boolean setInfo(int infoCode,Object sourceParameter,Object resultDestination,int options)
//===================================================================
{
	return nativeSetInfo(infoCode,sourceParameter,resultDestination,options);
}
//-------------------------------------------------------------------
private Object nativeGetInfo(int infoCode,Object sourceParameters,Object resultDestination,int options)
//-------------------------------------------------------------------
{
	java.awt.Component c = this.jWindow;
	if (c == null) c = _winCanvas;
	Object dest = resultDestination;
	switch(infoCode){
	case INFO_NATIVE_WINDOW:
		if (options == NATIVE_WINDOW_GET_DRAWING_SURFACE) return _winCanvas;
		else return c;
	case INFO_WINDOW_FLAGS:
		int fl = myFlags & ~(FLAG_MAXIMIZE|FLAG_MINIMIZE|FLAG_STATE_KNOWN);
		fl |= FLAG_HAS_CLOSE_BUTTON;
		if (c instanceof java.awt.Frame){
			try{
				java.awt.Frame f = (java.awt.Frame)c;
				fl |= FLAG_STATE_KNOWN;
				if (f.getState() == f.ICONIFIED) fl |= FLAG_MINIMIZE;
			}catch(Throwable t){}
		}
		if (dest instanceof ewe.sys.Long) ((ewe.sys.Long)dest).set(fl);
		return dest;
	case INFO_PARENT_RECT:
	case INFO_SCREEN_RECT:
		if (dest instanceof ewe.fx.Rect){
			Dimension d = getScreenSize();
			if (ewe.applet.Applet.mainWidth != 0) d.width = ewe.applet.Applet.mainWidth;
			if (ewe.applet.Applet.mainHeight != 0) d.height = ewe.applet.Applet.mainHeight;
			((Rect)dest).set(0,0,d.width,d.height);
		}
		return dest;
	case INFO_WINDOW_RECT:
	case INFO_CLIENT_RECT:
		if (dest instanceof ewe.fx.Rect){
			java.awt.Rectangle got = c.getBounds();
			java.awt.Insets in = (c instanceof java.awt.Window) ? ((java.awt.Window)c).getInsets() : new java.awt.Insets(0,0,0,0);
			if (infoCode == INFO_CLIENT_RECT){
				got.x = in.left;
				got.y = in.top;
				got.width -= in.left+in.right;
				got.height -= in.top+in.bottom;
			}
			((Rect)dest).set(got.x,got.y,got.width,got.height);
		}
		return dest;
	case INFO_FLAGS_FOR_SIZE:
		if (sourceParameters instanceof ewe.fx.Rect && dest instanceof ewe.sys.Long){
			ewe.fx.Rect src = (ewe.fx.Rect)sourceParameters;
			((ewe.sys.Long)dest).set(((FLAG_HAS_TITLE|FLAG_HAS_CLOSE_BUTTON)|src.x) & ~src.y);
		}
		return dest;
	}
	return null;
}
//===================================================================
public static Object getGuiInfo(int infoCode,Object sourceParameter,Object resultDestination,int options)
//===================================================================
{
	return nativeGetGuiInfo(infoCode,sourceParameter,resultDestination,options);
}

//===================================================================
public static Object nativeGetGuiInfo(int infoCode,Object sourceParameter,Object dest,int options)
//===================================================================
{
	switch(infoCode){
		case INFO_SCREEN_RECT:
		if (dest instanceof ewe.fx.Rect){
			Dimension d = getScreenSize();
			if (ewe.applet.Applet.mainWidth != 0) d.width = ewe.applet.Applet.mainWidth;
			if (ewe.applet.Applet.mainHeight != 0) d.height = ewe.applet.Applet.mainHeight;
			((Rect)dest).set(0,0,d.width,d.height);
		}
		return dest;
	}
	return null;
}
//===================================================================
public void windowBoundsChanged(java.awt.Window win)
//===================================================================
{
	java.awt.Insets in = win.getInsets();
	java.awt.Rectangle d = win.getBounds();
	appResized(d.width-in.left-in.right,d.height-in.top-in.bottom,0);
}
//-------------------------------------------------------------------
private boolean nativeSetInfo(int infoCode,Object sourceParameters,Object resultDestination,int options)
//-------------------------------------------------------------------
{
	Object source = sourceParameters;
	java.awt.Window c = this.jWindow;
	switch(infoCode){
	case INFO_TITLE:
		if (c instanceof java.awt.Frame){
				java.awt.Frame f = (java.awt.Frame)c;
				f.setTitle((String)sourceParameters);
		}
		return true;
	case INFO_WINDOW_FLAGS:
		if (source instanceof ewe.sys.Long) {
			int flags = (int)((ewe.sys.Long)source).value;
			if ((options & OPTION_FLAG_SET) != 0){
				myFlags |= flags;
				if ((flags & FLAG_IS_VISIBLE) != 0 && c != null)
					c.setVisible(true);
				if (c instanceof java.awt.Frame){
					try{
						java.awt.Frame f = (java.awt.Frame)c;
						if ((flags & FLAG_MINIMIZE) != 0) f.setState(f.ICONIFIED);
						else if ((flags & FLAG_RESTORE) != 0) f.setState(f.NORMAL);
					}catch(Throwable t){}
				}
			}else{
				myFlags &= ~flags;
				if ((flags & FLAG_IS_VISIBLE) != 0 && c != null)
					c.setVisible(false);
			}
		}
		return true;
	case INFO_CLIENT_RECT:
		if (source instanceof ewe.fx.Rect){
			java.awt.Insets in = c.getInsets();
			java.awt.Rectangle r = c.getBounds();
			Rect s = (Rect)source;
			s.x = r.x; s.y = r.y;
			s.width += in.left+in.right;
			s.height += in.top+in.bottom;
		}
		/* FALLTHROUGH */
	case INFO_WINDOW_RECT:
		if (source instanceof ewe.fx.Rect){
			Rect s = (Rect)source;
			java.awt.Rectangle r = new java.awt.Rectangle(s.x,s.y,s.width,s.height);
			c.setBounds(r);//(got.x,got.y,got.width,got.height);
			//windowBoundsChanged(c);
		}
		return true;
	case INFO_WINDOW_ICON:
		return setWindowIcon(source);
	}
	return false;
}
/*
static Var WindowGetInfo(Var stack[])
{
	Var v;
	WObject dest = stack[3].obj;
	int options = stack[4].intValue;
	v.obj = 0;
	switch(stack[1].intValue){
	case INFO_WINDOW_FLAGS:
		if (dest != 0) setLong(dest,MainWinFlags);
		v.obj = dest;
		return v;
	case INFO_PARENT_RECT: {
		WObject r = stack[3].obj;
		if (r == 0) return v;
		WOBJ_RectWidth(r) = GetSystemMetrics(SM_CXSCREEN);
		WOBJ_RectHeight(r) = GetSystemMetrics(SM_CYSCREEN);
		WOBJ_RectX(r) = WOBJ_RectY(r) = 0;
		v.obj = r;
		return v;
						   }
	case INFO_WINDOW_RECT: {
		WObject r = stack[3].obj;
		RECT rect;
		if (r == 0) return v;
		GetWindowRect(mainHWnd,&rect);
		WOBJ_RectWidth(r) = rect.right-rect.left;
		WOBJ_RectHeight(r) = rect.bottom-rect.top;
		WOBJ_RectX(r) = rect.left;
		WOBJ_RectY(r) = rect.top;
		v.obj = r;
		return v;
								}
	}
	return v;
}

int64 getLong(WObject obj);

static Var WindowSetInfo(Var stack[])
{
	Var v;
	int options = stack[4].intValue;
	v.obj = 0;

	switch(stack[1].intValue){
	case INFO_WINDOW_FLAGS: {
		int flags = (int)getLong(stack[2].obj);
		//DWORD newFlags = SWP_NOZORDER|SWP_ASYNCWINDOWPOS|SWP_NOSIZE|SWP_NOMOVE;
		if (options & OPTION_FLAG_SET){
			MainWinFlags |= flags;
			if (flags & FLAG_IS_VISIBLE) ShowWindow(mainHWnd,SW_SHOW);
		}else{
			MainWinFlags &= ~flags;
			if (flags & FLAG_IS_VISIBLE) ShowWindow(mainHWnd,SW_HIDE);
		}
		v.intValue = 1;
		return v;
								 }
	case INFO_PARENT_RECT: return v;
	case INFO_WINDOW_RECT: {
		WObject r = stack[2].obj;
		RECT rect;
		if (r == 0) return v;
		rect.left = WOBJ_RectX(r);
		rect.top = WOBJ_RectY(r);
		rect.bottom = rect.top+WOBJ_RectHeight(r);
		rect.right = rect.left+WOBJ_RectWidth(r);
		MoveWindow(mainHWnd,WOBJ_RectX(r),WOBJ_RectY(r),WOBJ_RectWidth(r),WOBJ_RectHeight(r),0);
		v.intValue = 1;
		return v;
				}
	}
	return v;
}
*/
static int visibleWidth, visibleHeight;
/**
* This will be called if the main window is resized.
**/
//===================================================================
public void resizeTo(int newWidth,int newHeight)
//===================================================================
{
	super.resizeTo(newWidth,newHeight);
	if (contents != null) contents.setRect(0,0,this.width,this.height);
}
private boolean gotFirstResize = false;
private Lock firstResizeLock = new Lock();
//===================================================================
public void appResized(int newWidth,int newHeight,int type)
//===================================================================
{
	if (newWidth == 0 || newHeight == 0) return;
	//visibleWidth = newWidth;
	//visibleHeight = newHeight;
	resizeTo(newWidth,newHeight);
	//ewe.sys.Vm.debug("appResized: "+newWidth+", "+newHeight);
	/*
	ewe.sys.Vm.debug("About to repaintNow()");
	ewe.sys.Vm.sleep(1000);
	repaintNow();
	ewe.sys.Vm.debug("Did repaintNow()");
	ewe.sys.Vm.sleep(1000);
	*/
	if (firstResizeLock.grab())try{
		//if (!gotFirstResize) new Exception().printStackTrace();
		gotFirstResize = true;
		firstResizeLock.notifyAllWaiting();
	}finally{
		firstResizeLock.release();
	}
}

protected ewe.sys.Lock closeLock = new ewe.sys.Lock();
//==================================================================
public void onEvent(Event ev)
//==================================================================
{
	//System.out.println(this+" "+ev);
	if (ev.type == WindowEvent.CLOSE){
		Frame f = Gui.topFrame(this);
		if (f instanceof FormFrame && f.isModal){
			((FormFrame)f).myForm.exit(-1);
			return;
		}
		ewe.util.Iterator it = contents.getChildren();
		while (it.hasNext()){
			Object c = it.next();
			if (c instanceof FormFrame){
				((FormFrame)c).myForm.exit(-1);
			}
		}
	}else if (ev.type == WindowEvent.ACTIVATE){
		if (_focus != null) Gui.takeFocus(_focus,ByRequest);
		Gui.notifyTop(this,true);
	}else if (ev.type == WindowEvent.DEACTIVATE){
		Gui.notifyTop(this,false);
	}else{
		super.onEvent(ev);
	}
}
//===================================================================
public Rect centerWindow(int width,int height)
//===================================================================
{
	Rect got = (Rect)getInfo(INFO_PARENT_RECT,null,new Rect(),0);
	if (got != null) {
		got.x = (got.width-width)/2;
		got.y = (got.height-height)/2;
	}else{
		got = new Rect();
		got.x = got.y = 0;
	}
	got.width = width;
	got.height = height;
	return got;
}
/**
* Center the main window. If width or height is -1 then the screen will not
* be resized.
**/
//===================================================================
public boolean centerWindow(int width,int height,boolean showIt)
//===================================================================
{
	Rect now = new Rect(0,0,width,height);
	if (width == -1 || height == -1) now = (Rect)getInfo(INFO_WINDOW_RECT,null,now,0);
	Rect got = (Rect)getInfo(INFO_PARENT_RECT,null,new Rect(),0);
	if (got != null) {
		now.x = (got.width-now.width)/2;
		now.y = (got.height-now.height)/2;
		setInfo(INFO_WINDOW_RECT,now,null,0);
	}
	if (showIt) setWindowFlags(FLAG_IS_VISIBLE);
	return true;
}
//===================================================================
public Rect getWindowRect(Rect dest,boolean clientArea)
//===================================================================
{
	return (Rect)getInfo(clientArea ?  INFO_CLIENT_RECT : INFO_WINDOW_RECT,null,dest,0);
}
//===================================================================
public void setWindowRect(Rect where,boolean clientArea)
//===================================================================
{
	setInfo(clientArea ?  INFO_CLIENT_RECT : INFO_WINDOW_RECT,where,null,0);
}
//===================================================================
public int getWindowFlags()
//===================================================================
{
	getInfo(INFO_WINDOW_FLAGS,null,ewe.sys.Long.l1.set(0),0);
	return (int)ewe.sys.Long.l1.value;
}
//===================================================================
public boolean setWindowFlags(int flags)
//===================================================================
{
	return setInfo(INFO_WINDOW_FLAGS,ewe.sys.Long.l1.set(flags),null,OPTION_FLAG_SET);
}
//===================================================================
public boolean clearWindowFlags(int flags)
//===================================================================
{
	return setInfo(INFO_WINDOW_FLAGS,ewe.sys.Long.l1.set(flags),null,OPTION_FLAG_CLEAR);
}
/**
* This returns null if the control is not covered by the SIP. If it is
* covered it returns a Rect representing the uncovered area of the screen.
**/
/*
//===================================================================
public Rect checkSipCoverage(Control who)
//===================================================================
{
	Rect r = Gui.getAppRect(who);
	Window w = who.getWindow();
	if (w == null) return null;
	Rect r2 = w.getWindowRect(new Rect(),true);
	//ewe.sys.Vm.debug(r2+", "+r+", "+visibleHeight);
	if (r2.y+r.y+r.height < visibleHeight || visibleHeight == 0) return null;
	return r.set(0,0,visibleWidth,visibleHeight);
}
*/
/**
* This returns null if the control is not covered by the SIP. If it is
* covered it returns a Rect representing the uncovered area of the screen.
**/
//===================================================================
public Rect checkSipCoverage(Control who)
//===================================================================
{
	if (visibleHeight == 0) return null;
	Rect r = Gui.getRectInWindow(who,new Rect(),true);
	if (r == null) return null;
	Window w = who.getWindow();
	if (w == null) return null;
	Frame f = who.getFrame();
	if (f.isPopup()) return null;
	Rect r2 = w.getWindowRect(new Rect(),true);
	Rect r3 = w.getWindowRect(new Rect(),false);
	r2.x += r3.x; r2.y += r3.y;
	if (r2.y+r.y+r.height < visibleHeight) return null;
	return r.set(0,0,visibleWidth,visibleHeight);

/*
	Rect r = Gui.getRectInWindow(who,new Rect(),true);
	if (r == null) return null;
	Window w = who.getWindow();
	if (w == null) return null;
	//Rect r2 = w.getWindowRect(new Rect(),true);
	if (r.y+r.height < visibleHeight || visibleHeight == 0) return null;
	return r.set(0,0,visibleWidth,visibleHeight);
*/
}
private Handle sipThread;
private mImage sipOverLay;
private ImageBuffer sipBuffer;

//-------------------------------------------------------------------
private void showSipCoverage()
//-------------------------------------------------------------------
{
	if (visibleHeight != 0 && Vm.getParameter(Vm.SIMULATE_SIP) != 0){
		Rect r2 = getWindowRect(new Rect(),true);
		Rect r3 = getWindowRect(new Rect(),false);
		r2.x += r3.x; r2.y += r3.y;
		int yy = visibleHeight-r2.y;
		if (yy > height) yy = height;
		if (yy >= height) return;
		int h = height-yy;
		if (h <= 26) return;
		Graphics gr = getGraphics();
		if (gr == null) return;
		if (sipBuffer == null) sipBuffer = new ImageBuffer();
		Graphics gg = sipBuffer.get(width,h,true);
		if (sipOverLay == null || sipOverLay.getHeight() != h){
			int hh = h-26;
			PixelBuffer pb = new PixelBuffer(width,hh);
			Graphics g = pb.getDrawingBuffer(null,null,0.5);
			g.setColor(Color.LightBlue);
			g.fillRect(0,0,width,hh);
			g.setPen(new Pen(Color.Black,Pen.SOLID,3));
			g.drawLine(0,1,width,1);
			g.drawLine(0,hh-2,width,hh-2);
			g.setPen(new Pen(Color.Black,Pen.DASH,3));
			g.drawLine(0,hh/2,width,hh/2);
			pb.putDrawingBuffer(pb.PUT_SET);
			sipOverLay = pb.toMImage();
		}
		gg.translate(0,-yy);
		repaintNow(gg,new Rect(0,yy,width,h));
		gg.translate(0,yy);
		//gg.drawLine(0,0,width,h);
		/*
		if (visibleHeight != 0){
			gr.setColor(Color.LightBlue);
			gr.fillRect(0,visibleHeight,width,height-visibleHeight);
		}
		*/
		sipOverLay.draw(gg,0,0,0);
		gr.drawImage(sipBuffer.image,0,yy);
		gr.free();
	}
}

//===================================================================
public void handleSipEvent(SIPEvent ev)
//===================================================================
{
	visibleWidth = ev.type == ev.SIP_SHOWN ? ev.visibleWidth : 0;//width;
	visibleHeight = ev.type == ev.SIP_SHOWN ? ev.visibleHeight : 0;//height;
	Control c = Gui.focusedControl();
	if (c != null){
		if (contains(c)){
			Frame f = c.getFrame();
			if (f != null)
				if (!f.isPopup())
					f.onEvent(ev);
			if (ev.type == ev.SIP_SHOWN)
				if (c instanceof EditControl)
					((EditControl)c).checkSipCoverage();
		}
	}
	Frame.checkSip(ev);
	if (visibleHeight != 0 && (Vm.getParameter(Vm.SIMULATE_SIP) != 0)){
		if (sipThread == null){
			sipThread = new TaskObject(){
				protected void doRun(){
					while(!shouldStop){
						showSipCoverage();
						sleep(250);
					}
				}
			}.startTask();
		}
	}else if (sipThread != null){
		sipThread.stop(0);
		sipThread = null;
		repaintNow();
	}
}
//===================================================================
public boolean doSpecialOp(int operationAndOptions,Object data)
//===================================================================
{
	switch(operationAndOptions & 0xff){
	 	case SPECIAL_MOUSE_MOVE:
		case SPECIAL_MOUSE_RESIZE:
			_inPenDrag = false;
			Control.firstPress = true;
			if (!doSpecialOperation(operationAndOptions,data)){
				if ((operationAndOptions & 0xff) == SPECIAL_MOUSE_MOVE){
					_inMouseMove = true;
					ewe.sys.Vm.setCursor(ewe.sys.Vm.MOVE_CURSOR);
				}else{
					_inMouseSize = true;
					ewe.sys.Vm.setCursor(ewe.sys.Vm.RESIZE_CURSOR);
				}
			}
	}
	return false;
}
//-------------------------------------------------------------------
private boolean doSpecialOperation(int operationAndOptions,Object data){return false;}
//-------------------------------------------------------------------
//-------------------------------------------------------------------
public Object currentCursor;
//-------------------------------------------------------------------
/**
* Returns whether multiple windows are supported on this platform.
**/
//===================================================================
public static boolean supportsMultiple()
//===================================================================
{
	//if (true) return false;
	return (ewe.sys.Vm.getParameter(ewe.sys.Vm.VM_FLAGS) & ewe.sys.Vm.VM_FLAG_NO_WINDOWS) == 0;
}
/**
 * Set the Maximize\Minimized\Normal state of the window.
 * @param state Use one of STATE_MAXIMIZED, STATE_MINIMIZED or STATE_NORMAL.
 */
//===================================================================
public void setState(int state)
//===================================================================
{
	int flag = FLAG_RESTORE;
	if (state == STATE_MAXIMIZED) flag = FLAG_MAXIMIZE;
	else if (state == STATE_MINIMIZED) flag = FLAG_MINIMIZE;
	setWindowFlags(flag);
}
/**
 * Return the Maximize\Minimized\Normal state of the window.
* @return one of STATE_MAXIMIZED, STATE_MINIMIZED, STATE_NORMAL or STATE_UNKNOWN
*/
//===================================================================
public int getState()
//===================================================================
{
	int flags = getWindowFlags();
	if ((flags & FLAG_STATE_KNOWN) == 0) return STATE_UNKNOWN;
	if ((flags & FLAG_MAXIMIZE) != 0) return STATE_MAXIMIZED;
	if ((flags & FLAG_MINIMIZE) != 0) return STATE_MINIMIZED;
	return STATE_NORMAL;
}
/**
This provides access to the underlying native Window.
@return
<p>Under Java, this will return the Window Component - which is either a java.awt.Window or java.awt.Frame or a Canvas
within the Applet (if it is being displayed in an Applet).
<p>Under Win32, it will return a ewe.sys.Long object which has its value set to be HWND value for the Window.
<p>Under Linux/Zaurus (Qt or other library) it will return a pointer to whatever structure represents a Window.
 */
//===================================================================
public Object getNativeWindow()
//===================================================================
{
	java.awt.Component c = this.jWindow;
	if (c == null) c = _winCanvas;
	return c;
}
/**
* This has the same effect as setTitle().
**/
//===================================================================
public void setText(String text)
//===================================================================
{
	super.setText(text);
	setTitle(text);
}
//===================================================================
public void setTitle(String title)
//===================================================================
{
	if (text.equals(title)) return;
	setInfo(INFO_TITLE,title,null,0);
	text = title;
}
//===================================================================
public void setIcon(Object icon)
//===================================================================
{
	if (icon instanceof IImage)
		icon = PixelBuffer.toIcon((IImage)icon);
	setInfo(INFO_WINDOW_ICON,icon,null,0);
}
//===================================================================
public String getTitle()
//===================================================================
{
	return text;
}
/**
 * Override this to handle native window events before the EweVM event handler does. If the
 * method returns true then the Ewe VM will not handle the message.
 * @param nativeWindowHandle The native window handle.
 * @param nativeMessage The native message sent to the window.
 * @param wParam The wParam of the message.
 * @param lParam The lParam of the message.
 * @return true if you don't want the Ewe VM to handle the message.
 */
protected boolean handleNativeMessage(int nativeWindowHandle, int nativeMessage, int wParam, int lParam)
{
	return false;
}
protected final boolean wantToHandle(int nativeMessage){return false;}

	//##################################################################
	public static class TaskBarIconInfo{
	//##################################################################
	public Object nativeIcon;
	public String tip;
	public TaskBarIconInfo(){}
	public TaskBarIconInfo(Object icon,String iconTip){
		nativeIcon = icon;
		if (nativeIcon instanceof IImage) nativeIcon = PixelBuffer.toIcon((IImage)nativeIcon);
		tip = iconTip;
	}
	public TaskBarIconInfo(String pathToIcon,String pathToMask,String iconTip)
	{
		this(new ewe.fx.Image(pathToIcon).toIcon(new ewe.fx.Image(pathToMask)),iconTip);
	}
	//##################################################################
		}
	//##################################################################

}
