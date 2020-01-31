/*
Note - This is the Linux version of Window.java
*/
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

import ewe.fx.*;
import ewe.sys.*;


/**

Window is a "floating" or "child" top-level window, it is associated with a "native" window instance. Under Java
the native window will be an instance of a java.awt.Window, under Win32 it will be Win32 Window.<p>
A native window is not created in the constructor, but by the create() method - in which case it is considered open.
It is closed using the close() method and must not be re-opened after closing. A Window's visible state may be changed
dynamically using setWindowFlags() as needed.<p>
<b>mApp</b> is an instance of MainWindow which itself is an instanceof Window. The main mApp window (the one created
when your application starts) should <b>never</b> be closed, although it may be made invisible and you do not need to
add controls to it.<p>

During window creation certain window flags are set or cleared by default depending on the native platform. However you
can explicitly specify which flags should be set and cleared using the <b>flagsToSet</b> and <b>flagsToClear</b> parameters
in the <b>create()</b> method.<p>

The easiest way to display a new pop-up window is to create a Form object and call Form.exec(Gui.NEW_WINDOW) or Form.show(Gui.NEW_WINDOW).
This sizes the window to fit the form an displays the window and form. You can select the window flags to set and clear with
the <b>windowFlagsToSet</b> and <b>windowFlagsToClear</b> variables in the Form object.<p>

In reality, a Window is used to display multiple Frames within it. Any control to be displayed must be placed in a Frame and
that Frame must be displayed by the Gui class using the Gui.execFrame() or Gui.showFrame() methods.
The Gui class displays a Frame by making it the child of a currently on-screen Frame. If the parent on-screen Frame is null
then it is made the child of the <b>contents</b> Frame object of a Window. This effectively makes it the top-level Frame within
that Window. This scheme allows for multiple-frames (which look like native windows) within a single Window (native window)
object. This allows for multiple-frame application to be run on a platform which may not support multiple-windows (e.g.
OS'es like PalmOS).<p>

Calling the Form.show() or Form.exec() methods prompts the Form to create its own Frame and it then uses Gui to show
the Frame in the destination Window. If it specifies no parent then it will be made the top-level Frame in the destination
Window. If Gui.NEW_WINDOW is specified as an option for show() or exec() then a new Window will be created and the Form's
Frame will be displayed in the new Window.<p>

A Window always contains a <b>contents</b> Frame object which is used as the parent frame for any other Frames to be
displayed within it. A Window is always the top level control in any on-screen control tree. That is to say, calling
getParent() on a Window will always return null. To iterate through all the open (on-screen, visible or invisible) Windows
in an application you  must call Window.getOpenWindows().<p>

On some platforms like Win32 (but not on Java) it is possible to place a native window within another native window and
that is supported in Ewe. The create() method takes an extra Object parameter which, if set to a Window object, will
create the new native window as a native child window of the native window represented by the Window object. The new
window that is created will NOT have the parent Window object as the parent control (i.e. calling getParent() on the
new window will NOT return the old window). In this case you must call getContainingWindow() to get the containing window.<p>

Creating a Window that is embedded in another Window is best achieved using a WindowContainer object. This Control is
associated with a new Window which is always sized to be the same size as the WindowContainer object itself. You can
then create a Form object and specify Gui.NEW_WINDOW as the exec()/show() options AND specify WindowContainer.getContinedWindowFrame()
as the parent frame. However you should call WindowContainer.isSupported() to see if this is supported on the running
platform first.<p>

 */

public class Window extends Container implements ISurface, WindowConstants, ewe.data.HasProperties
{
public WindowCreationData creationData = new WindowCreationData();
protected static KeyEvent _keyEvent = new KeyEvent();
protected static PenEvent _penEvent = new PenEvent();
protected static ControlEvent _controlEvent = new ControlEvent();
boolean needsPaint;
int paintX, paintY, paintWidth, paintHeight;
Graphics _g;
Control _focus;
static boolean _inPenDrag,_inMouseMove,_inMouseSize;
public Frame contents = new Frame();
//Used by GUI.
ewe.util.Vector frames = new ewe.util.Vector();
public boolean canDisplay, firstDisplay;

private ewe.data.PropertyList properties = new ewe.data.PropertyList();

public ewe.data.PropertyList getProperties() {return properties;}

Image savedImage;

/** Constructs a window. */
public Window()
	{
	contents.contentsOnly = true;
	contents.contents = null;
	if (globalDrawFlat) modify(DrawFlat,0);
	_nativeCreate();
	}

//-------------------------------------------------------------------
private native void _nativeCreate();
//-------------------------------------------------------------------

static ewe.util.Vector openWindows = new ewe.util.Vector();
Window containing;
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
* If this Window is contained in another Window, this will return the containing Window. Calling getParent() on
* a Window should always return null.
**/
//===================================================================
public Window getContainingWindow()
//===================================================================
{
	return containing;
}
/**
* This creates the native window associated with this Window object.
**/
//===================================================================
public boolean create(final Rect location,final String title,final int flagsToSet,final int flagsToClear,final Object extra)
//===================================================================
{
	text = title;

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
			canDisplay = firstDisplay = true;
			openWindows.add(Window.this);
			if ((flagsToClear & FLAG_IS_VISIBLE) == 0){
				activeWindow = Window.this;
				checkAppKeys();
			}
			return true;
		}
	}.execute(null,true);
	if ((flagsToSet & FLAG_IS_MODAL) != 0)
		new windowThread(this,true);

	return ret;
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
* This closes the window and destroys the native Window component. Do not re-use the window after closing it.
**/
//===================================================================
public boolean close()
//===================================================================
{
	_focus = null;
	if (lastWindow == this) lastWindow = null;
	if (inFront == this) inFront = null;
	if (activeWindow == this) activeWindow = null;
	if (Gui.sipWindow == this) Gui.sipWindow = null;
	clearWindowFlags(FLAG_IS_VISIBLE);
	/*
	_sipEvent.window = null;
	_keyEvent.window = null;
	_controlEvent.window = null;
	_penEvent.window = null;
	*/
	//ewe.sys.Vm.debug("Closing: "+hashCode());
	openWindows.remove(this);
	windowThread.closing(Window.this);
	return new ewe.sys.MessageThreadTask(){
		protected boolean doTask(Object data){
			shutdown();
			closeWindow();
			canDisplay =  false;
			return true;
		}
	}.execute(null,false);
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
* This returns the currently active window.
**/
//===================================================================
public static Window getActiveWindow()
//===================================================================
{
	return activeWindow;
}
/**
* This brings this window to the front.
**/
//===================================================================
public boolean toFront()
//===================================================================
{
	return new ewe.sys.MessageThreadTask(){
		protected boolean doTask(Object data){
			if ((getWindowFlags() & FLAG_IS_VISIBLE) != 0)
			{
				if (getState() == STATE_MINIMIZED)
					setState(STATE_NORMAL);
			}
			else if ((getWindowFlags() & FLAG_VISIBLE_ON_TO_FRONT) != 0){
				setWindowFlags(FLAG_IS_VISIBLE);
				setState(STATE_NORMAL);
			}
			windowToFront();
			activeWindow = Window.this;
			return true;
		}
	}.execute(null,true);
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
/**
* This is called either before the window is closed or before the application exits.
**/
//-------------------------------------------------------------------
protected void shutdown()
//-------------------------------------------------------------------
{
	if (taskbarIcon != null){
		setInfo(INFO_TASKBAR_ICON,null,null,0);
	}
}
//-------------------------------------------------------------------
protected native boolean createNativeWindow(Rect location,String title,int flagsToSet,int flagsToClear,Object extra);
//-------------------------------------------------------------------
//-------------------------------------------------------------------
protected native boolean closeWindow();
//-------------------------------------------------------------------
protected native boolean windowToFront();
//-------------------------------------------------------------------
//private native boolean _nativeTextInput(TextInputParameters tip);

private static Control inputingText;
/**
This returns whether the system is within a native input or native dialog.
If this is the case, then no Gui input will be sent to the Ewe Gui system.
**/
//===================================================================
public static boolean inNativeInput()
//===================================================================
{
	return inputingText != null || nativeDialogCount != 0;
}
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
	//if (true)
	throw new NoSuchMethodError();
	/*
	if (inputingText != null) return false;
	inputingText = tip.control;
	boolean ret = new ewe.sys.MessageThreadTask(){
		protected boolean doTask(Object data){
			if (_nativeTextInput(tip)){
				enterNativeDialog();
				return true;
			}
			return false;
		}
	}.execute(null);
	if (!ret) inputingText = null;
	return ret;
	*/
}

/**
* This is the Window of the application that is currently active.
**/
private static Window activeWindow;
private static boolean capturedKeys = false;
private native static void captureAppKeys(int options,Window win);
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
/**
 * This is only to be used by the Window itself or the Gui class. To set the focus for a Control you should call
 * Gui.takeFocus(Control c,int how).
 **/
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
public static Object dragAndDropSource;
public static int dragAndDropCursor;
//===================================================================
public native void releaseMouseCapture();
//===================================================================
/**
* This is called by a control to start a drag and drop operation. You should not call this directly,
* rather you should call Control.startDragAndDrop() from Control
**/
//===================================================================
public boolean beginDragAndDrop(Control source,Object data,int dragCursor)
//===================================================================
{
	dragAndDropData = data;
	dragAndDropCursor = dragCursor;
	dragAndDropSource = source;
	//releaseMouseCapture();
	return true;
}

/**
* This is called by a control to start a drag and drop operation. You should not call this directly,
* rather you should call Control.startDragAndDrop() from Control
**/
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

protected static Window inFront;
Point mouseOffset = null;
static Point dragxy = new Point();


protected static Window lastWindow = null;

//-------------------------------------------------------------------
protected Window tryWindow(Window win,int x,int y,Point where)
//-------------------------------------------------------------------
{
	if (win == null || mouseOffset == null) return null;
	int absX = x+mouseOffset.x;
	int absY = y+mouseOffset.y;
	Rect wr = win.getWindowRect(new Rect(),false);
	//ewe.sys.Vm.debug("Check: "+wr+", "+absX+","+absY);
	if (wr.isIn(absX,absY)){
		//ewe.sys.Vm.debug("OK, now in.");
		int cx = absX-wr.x, cy = absY-wr.y;
		Rect in = win.getWindowRect(new Rect(),true);
//		ewe.sys.Vm.debug("client: "+in);
		cx -= in.x; cy -= in.y;
		Control target = win.findChild(cx,cy);
		if (target != win && target != null){
			if (win != lastWindow) win.toFront();
			lastWindow = win;
			where.x = cx; where.y = cy;
			return win;
		}
	}
	return null;
}


/**
* This is the point at which the mouse was first pressed in a current press and hold/drag operation.
**/
public static Point downPoint = new Point();
int lastMoveX = -1, lastMoveY = -1;
/**
 * Called by the VM to post key and pen events.
 */
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
static int nativeDialogCount = 0;

public static void enterNativeDialog()
{
	nativeDialogCount++;
}
public static void exitNativeDialog()
{
	if (nativeDialogCount > 0)
		nativeDialogCount--;
}

	//##################################################################
	static class windowThread implements Runnable{
	//##################################################################
	Window modalWindow = null;
	boolean modalMade = false;
	windowThread previousThread;
	//Coroutine myThread;
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
	Object data;
	//===================================================================
	public queuedEvent(Window window,int type, int key, int x, int y, int modifiers, int timeStamp, Object data)
	//===================================================================
	{
		this.type = type;
		this.key = key;
		this.x = x;
		this.y = y;
		this.modifiers = modifiers;
		this.timeStamp = timeStamp;
		this.window = window;
		this.data = data;
		if (window == null) new Exception().printStackTrace();
		if (events == null) events = this;
		else{
			queuedEvent e = events;
			for (;e.next != null; e = e.next);
			e.next = this;
		}
		if (eventThread != null)
			eventThread.wakeup();
			/*
			Lock l = eventThread.waitEventLock;
			if (l.grab()) try{
				l.notifyAllWaiting();
			}finally{
				l.release();
			}
			*/
			/*
			if (eventThread.waitingOnEvent)
				eventThread.myThread.wakeup();

				}
			*/
	}
	public String toString()
	{
		return "QE("+type+")->"+window+"["+window.hashCode()+"]";
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
		myThread = new mThread(this); //new Coroutine(this);
		myThread.start();
	}

	public String toString()
	{
		return "windowThread->"+modalWindow;
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
	public void exit()
	//===================================================================
	{
		shouldStop = true;
		wakeup();
		//if (waitingOnEvent) myThread.wakeup();
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
			ewe.sys.Vm.setSIP(Gui.setSipTo,activeWindow);
			Gui.setSipTo = -1;
		}
		myThread = mThread.currentThread();//Coroutine.getCurrent();
		try{
			//for(;!shouldStop;Coroutine.sleep(-1)){
			while(!shouldStop){
				//waitingOnEvent = false;
				waitEventLock.synchronize(); try{
					if (events == null || eventThread != this){
						waitEventLock.wait(TimeOut.Forever);

					}
				}finally{
					waitEventLock.release();
				}
				if (eventThread != this) continue;
				try{
					if (shouldStop) break;
					for(queuedEvent e = events; e != null; e = events){
						try{
							events = e.next;
							e.next = null;
							if (nativeDialogCount > 0 && !(e.type >= TextEvent.TEXT_EVENT_FIRST && e.type <= TextEvent.TEXT_EVENT_LAST)) continue;
							if (modalWindow != null && modalWindow != e.window){
								int type = e.type;
								//if (type == PenEvent.PEN_UP || type == PenEvent.PEN_DOWN) ewe.sys.Vm.debug(type+" - out of modal!");
								if (modalMade && (type == PenEvent.PEN_UP || type == PenEvent.PEN_DOWN || type == PenEvent.PEN_MOVE || type == WindowEvent.CLOSE)){
									if ((activeWindow != modalWindow) || (type == PenEvent.PEN_DOWN)){
										modalWindow.toFront();
									}
								}
								if (type == WindowEvent.ACTIVATE || type == WindowEvent.DEACTIVATE){
									e.window.doPostEvent(e.type,e.key,e.x,e.y,e.modifiers,e.timeStamp,e.data);
								}if (type == SIPEvent.SIP_SHOWN || type == SIPEvent.SIP_HIDDEN)
									modalWindow.doPostEvent(e.type,e.key,e.x,e.y,e.modifiers,e.timeStamp,e.data);
								continue;
							}
							e.window.doPostEvent(e.type,e.key,e.x,e.y,e.modifiers,e.timeStamp,e.data);
							if (shouldStop || eventThread != this) break;
						}finally{
							Gui.setSipTo = -1;
							ewe.sys.Vm.freezeSIP(false,0,modalWindow);
						}
					}
					if (shouldStop) break;
					//waitingOnEvent = true;
				}catch(Throwable t){
					ewe.sys.Vm.debug(ewe.sys.Vm.getStackTrace(t,10));
					//t.printStackTrace();
					//waitingOnEvent = true;
				}
				/*

				try{
					myThread.sleep(10000000);
				}catch(InterruptedException e){
				}

				*/
			}
		}finally{
			if (eventThread == this) {
				eventThread = previousThread;
				if (eventThread != null) eventThread.wakeup();
			}
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
//===================================================================
public void _textEvent(int type, int flags, Object text, int timeStamp)
//===================================================================
{
	new windowThread.queuedEvent(this,type,0,0,0,flags,timeStamp,text);
}
/**
* This is called directly by the VM within a native Window Message handler. It is therefore
* in the same thread as the message queue. This will then queue the event in the mThread
* responsible for dispatching events. However if the event is a WindowEvent type, and the
* application is a mobile application, it will also call mobileWindowEvent() before queueing
* the event.
**/
//===================================================================
public void _postEvent(int type, int key, int x, int y, int modifiers, int timeStamp)
//===================================================================
{
	//if ((modifiers & PenEvent.FROM_OTHER_WINDOW) != 0) ewe.sys.Vm.debug("FROM OTHER: "+timeStamp);
	//ewe.sys.Vm.debug("_postEvent to "+hashCode()+": "+type+", "+key+", "+x+", "+y+", "+modifiers);
	if (nativeDialogCount != 0) return;
	if (type == WindowEvent.ACTIVATE){
		activeWindow = this;
		checkAppKeys();
		Frame.checkCurrentSip();
		if (_focus != null)
			if (_focus.hasModifier(ShowSIP,false) && _focus.canEdit() && ((getWindowFlags() & FLAG_SHOW_SIP_BUTTON) == 0))
				ewe.sys.Vm.setSIP(2,this);

	}
	if (type >= WindowEvent.CLOSE && type <= WindowEvent.HIBERNATE)
		if ((ewe.sys.Vm.getParameter(ewe.sys.Vm.VM_FLAGS) & ewe.sys.Vm.VM_FLAG_IS_MOBILE) != 0)
			mobileWindowEvent(type,key,x,y,modifiers,timeStamp);

	if (type == WindowEvent.APP_TO_FRONT) applicationToFront();
	else{
		new windowThread.queuedEvent(this,type,key,x,y,modifiers,timeStamp,null);
	}
}
private boolean inKeyPress;
//===================================================================
public void doPostEvent(int type, int key, int x, int y, int modifiers, int timeStamp)
//===================================================================
{
	doPostEvent(type,key,x,y,modifiers,timeStamp,null);
}

//===================================================================
public void doPostEvent(int type, int key, int x, int y, int modifiers, int timeStamp, Object data)
//===================================================================
	{
	inEventThread = true;
	Gui.setSipTo = -1;
	Event event = null;
	Control target = _focus;
	try{
	if (target == null) target = this;
	if (type != PenEvent.PEN_MOVE) PenEvent.removeTip();

	if (type == WindowEvent.CLOSE || type == WindowEvent.ACTIVATE || type == WindowEvent.DEACTIVATE || type == WindowEvent.HIBERNATE){
		target = this;
		//if (type == WindowEvent.CLOSE) ewe.sys.Vm.messageBox("Close!","Closing: "+getClass().getName(),0);
		//if (type == WindowEvent.HIBERNATE) ewe.sys.Vm.messageBox("Hibernate!","Hibernating: "+getClass().getName(),0);
		WindowEvent we = new WindowEvent();
		we.type = type;
		event = we;
		event.window = this;
	}else if (type >= TextEvent.TEXT_EVENT_FIRST && type <= TextEvent.TEXT_EVENT_LAST){
		TextEvent te = new TextEvent();
		target = inputingText;
		if (target == null) return;
		if (type == TextEvent.TEXT_ENTERED){
			exitNativeDialog();
			inputingText = null;
		}
		te.type = type;
		te.entered = TextInputParameters.fixEditedText((String)data);
		te.flags = modifiers;
		event = te;
	}else if (type == KeyEvent.KEY_PRESS || type == KeyEvent.KEY_RELEASE)
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
		if (_keyEvent.key == IKeys.SOFTKEY1 || _keyEvent.key == IKeys.SOFTKEY2)
			if (SoftKeyBar.onKeyEvent(_keyEvent)) return;
		}
	//MLB
	else if (type == _sipEvent.SIP_SHOWN || type == _sipEvent.SIP_HIDDEN){
		_sipEvent.type = type;
		_sipEvent.visibleWidth = type == _sipEvent.SIP_SHOWN ? x : width;
		_sipEvent.visibleHeight = type == _sipEvent.SIP_SHOWN ? y : height;
		_sipEvent.desktopHeight = type  == _sipEvent.SIP_SHOWN ? key : height;
		event = _sipEvent;
		event.window = this;
		handleSipEvent(_sipEvent);
		return;
	}
	//END MLB
	else
		{
		_penEvent.window = this;

		// set focus to new control
		if (type == PenEvent.SCROLL_DOWN || type == PenEvent.SCROLL_UP){
			mApp.lastEvent = ewe.sys.Vm.getTimeStamp();
			Control c = findChild(x, y);
			if (c != null)
				if (c.amOnTopFrame()){
					target = c;
					if (c != _focus && !c.hasModifier(Disabled,true))
						setFocus(c);
				}else
					return;
		}else if (type == PenEvent.PEN_DOWN)
			{
			downPoint.set(x,y);
			lastWindow = this;
			inFront = this;
			Rect r = getWindowRect(Rect.buff,false);
			mouseOffset = new Point(r.x,r.y);
			r = getWindowRect(Rect.buff,true);
			mouseOffset.y += r.x; mouseOffset.y += r.y;
			//toFront();
			mApp.lastEvent = ewe.sys.Vm.getTimeStamp();
			Control c = findChild(x, y);
			target = c;
			if (c != _focus && !c.hasModifier(Disabled,true))
				setFocus(c);
			_inPenDrag = true;
			}
		else if (type == PenEvent.PEN_MOVED_OFF){
			//ewe.sys.Vm.debug("Off!");
			_penEvent.window = this;
			_penEvent.type = PenEvent.PEN_MOVED_OFF;
			if (_inMouseMove || _inMouseSize) return;
			PenEvent.handlePenMove(this,_penEvent,x,y);
			target = this;
		}else if (type == PenEvent.PEN_MOVE){
			if (_inMouseMove || _inMouseSize) return;
			if (lastMoveX == x && lastMoveY == y && x != -1 && y != -1) return;
			lastMoveX = x; lastMoveY = y;
			if (_inPenDrag){
				type = PenEvent.PEN_DRAG;
				/*
				if (inFront != this) {
					inFront = this;
					toFront();
				}*/
				if (dragAndDropData != null){
					if ((modifiers & PenEvent.FROM_OTHER_WINDOW) != 0){
						target = findChild(x,y);
					}else{
						Rect r = getWindowRect(Rect.buff,false);
						mouseOffset = new Point(r.x,r.y);
						r = getWindowRect(Rect.buff,true);
						mouseOffset.y += r.x; mouseOffset.y += r.y;
						Window shouldGet = tryWindow(lastWindow,x,y,dragxy);
						if (shouldGet == this){
							target = findChild(x,y);
						}else if (shouldGet != null){
							shouldGet._postEvent(PenEvent.PEN_MOVE,key,dragxy.x,dragxy.y,modifiers|PenEvent.FROM_OTHER_WINDOW,timeStamp);
							return;
						}else{
							for (int i = 0; i<openWindows.size(); i++){
								Window win = (Window)openWindows.get(i);
								if (win == lastWindow) continue;
								if ((win.getWindowFlags() & FLAG_IS_VISIBLE) == 0) continue;
								shouldGet = tryWindow(win,x,y,dragxy);
								if (shouldGet == this){
									target = findChild(x,y);

									break;
								}else if (shouldGet != null){
									//ewe.sys.Vm.debug("OK, shifting now!");
									shouldGet._postEvent(PenEvent.PEN_MOVE,key,dragxy.x,dragxy.y,modifiers|PenEvent.FROM_OTHER_WINDOW,timeStamp);
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
							shouldGet._postEvent(PenEvent.PEN_UP,key,dragxy.x,dragxy.y,modifiers|PenEvent.FROM_OTHER_WINDOW,timeStamp);

							return;
						}else{
							for (int i = 0; i<openWindows.size(); i++){

								Window win = (Window)openWindows.get(i);
								if (win == lastWindow) continue;
								if ((win.getWindowFlags() & FLAG_IS_VISIBLE) == 0) continue;
								shouldGet = tryWindow(win,x,y,dragxy);
								if (shouldGet == this){
									target = findChild(x,y);
									break;
								}else if (shouldGet != null){
									shouldGet._postEvent(PenEvent.PEN_UP,key,dragxy.x,dragxy.y,modifiers|PenEvent.FROM_OTHER_WINDOW,timeStamp);
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
		//if (event.type == PenEvent.PEN_UP) ewe.sys.Vm.debug(event.target.getClass().toString());
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
		dragAndDropData = null;
		setFocus(Gui.focusedControl());
	}
	if (needsPaint)
		_doPaint(paintX, paintY, paintWidth, paintHeight);
	//}catch(Throwable t){
//		new ReportException(t,null,null,false).exec();
	}finally{
		if (event != null) {
			event.window = null;
			event.target = null;
		}
		inEventThread = false;
		if (Gui.setSipTo != -1) {
			ewe.sys.Vm.setSIP(Gui.setSipTo,activeWindow);
			//new Exception().printStackTrace();
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
	/*
	if (mApp.mainApp != null) {
	}
	if (_g == null)
		_g = new Graphics(this);


	// clear background
	_g.setClip(x, y, width, height);
	if (Vm.isColor())
		_g.setColor(200, 200, 200);
	else
		_g.setColor(255, 255, 255);
	_g.fillRect(x, y, width, height);
	onPaint(_g);
	_g.clearClip();
	paintChildren(_g, x, y, width, height);

	if (needsPaint)
		{
		int ax = x + width;
		int ay = y + height;
		int bx = paintX + paintWidth;
		int by = paintY + paintHeight;
		if (x <= paintX && y <= paintY && ax >= bx && ay >= by)
			needsPaint = false;
		}
		*/
	}
//MLB
/**
* This method is used to get extended information about the Window or the GUI system in general.
* It is used with the INFO_ specifiers and options. It will return the resultDestination on success
* or null on failure. See interface WindowConstants for values for infoCode and options.
**/
//===================================================================
public Object getInfo(int infoCode,Object sourceParameter,Object resultDestination,int options)
//===================================================================
{
	if (infoCode == INFO_TASKBAR_ICON){
		return taskbarIcon;
	}else if (infoCode == INFO_NATIVE_WINDOW){
		resultDestination = new ewe.sys.Long();
	}else if (infoCode == INFO_POSITION_IN_NATIVE_DRAWING_SURFACE){
		ewe.fx.Point p = new Point();
		Control c = (Control)sourceParameter;
		if (c.getWindow() != this) return null;
		Rect r = Gui.getRectInWindow((Control)sourceParameter,new Rect(),false,p);
		nativeGetInfo(infoCode,null,p,0);
		return p;
	}
	return nativeGetInfo(infoCode,sourceParameter,resultDestination,options);
}

//===================================================================
public static Object getGuiInfo(int infoCode,Object sourceParameter,Object resultDestination,int options)
//===================================================================
{
	Object d = null;
	switch(infoCode){
		case INFO_SCREEN_RECT: d = (Rect)resultDestination; break;
		case INFO_GUI_FLAGS: d = (ewe.sys.Long)resultDestination; break;
		default: return null;
	}
	return nativeGetGuiInfo(infoCode,sourceParameter,resultDestination,options);
}

//===================================================================
public static native Object nativeGetGuiInfo(int infoCode,Object sourceParameter,Object resultDestination,int options);
//===================================================================

/**
* This method is used to set extended information about the Window or the GUI system in general.
* It is used with the INFO_ specifiers and options. It will return true on success, false on failure.
* See interface WindowConstants for values for infoCode and options.
* @see ewe.ui.WindowConstants
**/
//===================================================================
public boolean setInfo(final int infoCode,final Object sourceParameter,final Object resultDestination,int options)
//===================================================================
{
	if (infoCode == INFO_TASKBAR_ICON){
		if (taskbarIcon != null) { //Modify existing one if sourceParameter is not NULL.
			options |= 0x80000000;
			if ((options & (OPTION_TASKBAR_ICON_MODIFY_ICON|OPTION_TASKBAR_ICON_MODIFY_TIP)) == 0)
				options |= OPTION_TASKBAR_ICON_MODIFY_ICON|OPTION_TASKBAR_ICON_MODIFY_TIP;
		}else options &= ~0x80000000;
		taskbarIcon = (TaskBarIconInfo)sourceParameter;
	}
	final int opts = options;
	return new MessageThreadTask(){
		protected boolean doTask(Object data){
			return nativeSetInfo(infoCode,sourceParameter,resultDestination,opts);
		}
	}.execute(null,true);//infoCode != INFO_TASKBAR_ICON && infoCode != INFO_WINDOW_ICON);
	//return nativeSetInfo(infoCode,sourceParameter,resultDestination,options);
}
//-------------------------------------------------------------------
private native Object nativeGetInfo(int infoCode,Object sourceParameters,Object resultDesination,int options);
//-------------------------------------------------------------------
private native boolean nativeSetInfo(int infoCode,Object sourceParameters,Object resultDesination,int options);
//-------------------------------------------------------------------
/**
* This is the visible width and height of the screen.
**/
static int visibleWidth, visibleHeight;

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

private TaskBarIconInfo taskbarIcon;

//===================================================================
public void resizeTo(int newWidth,int newHeight)
//===================================================================
{
	super.resizeTo(newWidth,newHeight);
	if (contents != null) contents.setRect(0,0,this.width,this.height);
}
/**
* This will be called if the main window is resized.
**/
//===================================================================
public void appResized(int newWidth,int newHeight,int type)
//===================================================================
{
	savedImage = null;
	//ewe.sys.Vm.debug("Appresized: "+newWidth+", "+newHeight);
	if (newWidth == 0 || newHeight == 0) return;
	//visibleWidth = newWidth;
	//visibleHeight = newHeight;
	resizeTo(newWidth,newHeight);
	repaintNow();
}
protected ewe.sys.Lock closeLock = new ewe.sys.Lock();

//==================================================================
public void onEvent(Event ev)
//==================================================================
{
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
/**
* This centers the window on the screen or within its containing window.
**/
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
/**
* Get the window full or client rectangle. Do not pass a null destination rectangle.
**/
//===================================================================
public Rect getWindowRect(Rect dest,boolean clientArea)
//===================================================================
{
	return (Rect)getInfo(clientArea ? INFO_CLIENT_RECT : INFO_WINDOW_RECT,null,dest,0);
}
/**
* Set the window full or client rectangle. Do not pass a null destination rectangle.

**/
//===================================================================
public void setWindowRect(Rect where,boolean clientArea)
//===================================================================
{
	setInfo(clientArea ? INFO_CLIENT_RECT : INFO_WINDOW_RECT,where,null,0);
}
/**
* Get the window flags. This is only meaningful after a create() call.
**/
//===================================================================
public int getWindowFlags()
//===================================================================
{
	getInfo(INFO_WINDOW_FLAGS,null,ewe.sys.Long.l1.set(0),0);
	return (int)ewe.sys.Long.l1.value;
}
/**
* Set specified window flags. This is only meaningful after a create() call.
**/
//===================================================================
public boolean setWindowFlags(int flags)
//===================================================================
{
	return setInfo(INFO_WINDOW_FLAGS,ewe.sys.Long.l1.set(flags),null,OPTION_FLAG_SET);
}
/**
* Clear specified window flags. This is only meaningful after a create() call.
**/
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
//===================================================================
public Rect checkSipCoverage(Control who)
//===================================================================
{
	Rect r = Gui.getRectInWindow(who,new Rect(),true);
	if (r == null) return null;
	Window w = who.getWindow();
	if (w == null) return null;
	Frame f = who.getFrame();
	if (f.isPopup()) return null;
	Rect r2 = w.getWindowRect(new Rect(),true);
	Rect r3 = w.getWindowRect(new Rect(),false);
	r2.x += r3.x; r2.y += r3.y;
	if (visibleHeight == 0) return null;
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
/**
* This handles special features like dragging/resizing the window by a Ewe DragPanel. This is necessary
* if you create a window which does not have a native title bar.
**/
//===================================================================
public boolean doSpecialOp(final int operationAndOptions,final Object data)
//===================================================================
{
	int op = operationAndOptions & 0xff;
	if (op != SPECIAL_MOUSE_MOVE && op != SPECIAL_MOUSE_RESIZE)
		return doSpecialOperation(operationAndOptions,data);
	else
	return new ewe.sys.MessageThreadTask(){
		protected boolean doTask(Object data){
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
	}}.execute(data,true);
}
//-------------------------------------------------------------------
private native boolean doSpecialOperation(int operationAndOptions,Object data);
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
	setInfo(INFO_TITLE,title,null,0);
	text = title;
}
//===================================================================
public String getTitle()
//===================================================================
{
	return text;
}
//===================================================================
public void setIcon(Object icon)
//===================================================================
{
	if (icon instanceof IImage)
		icon = PixelBuffer.toIcon((IImage)icon);
	setInfo(INFO_WINDOW_ICON,icon,null,0);
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
	return new ewe.sys.Long().set(nativeGetNativeWindow());
}

//-------------------------------------------------------------------
private native long nativeGetNativeWindow();
//-------------------------------------------------------------------
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
/*
private boolean inUpdate = false;

//===================================================================
public void windowUpdated()
//===================================================================
{
	if (inUpdate) return;
	if (!PenEvent.tipIsDisplayed()  && topImages == null) return;
	try{
		inUpdate = true;
		if (PenEvent.tipIsDisplayed()) PenEvent.theTip.repaintNow();
		if (topImages != null){
			Graphics g = new Graphics(this);
			for (int i = 0; i<topImages.size(); i++){
				mImage im = (mImage)topImages.get(i);
				im.draw(g,im.location.x,im.location.y,0);
			}
			g.flush();

			g.free();
		}
	}finally{
		inUpdate = false;
	}
}
private ewe.util.Vector topImages;
//===================================================================
public void addTopImage(mImage image)
//===================================================================
{
	topImages = ewe.util.Vector.add(topImages,image);
}
//===================================================================
public void removeTopImage(mImage image)
//===================================================================
{
	if (topImages != null) {

		topImages.remove(image);
		if (topImages.size() == 0) topImages = null;
	}
}
*/
}


