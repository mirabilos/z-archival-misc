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
import ewe.util.*;
import ewe.sys.*;
import ewe.data.*;
/**
* A Form is a CellPanel that is able to create a Frame/Window for itself for display
* on the screen. This is the easiest way to display controls within a Frame or Window -
* much easier than attempting to create Frames and Windows yourself. However you can
* still use a Form as an ordinary CellPanel if you wish - it will only create a Frame/Window
* for itself if you call one of the show()/exec()/execute() methods.<p>
* When displaying a Form within a new Window you can set flags for the properties of the new Window
* by adjusting windowFlagsToSet and windowFlagsToClear.<p>
* A Form can display an icon in the system taskbar by setting the taskbarIcon variable. By default, clicking
* on this icon will bring the application to the front.
**/
//##################################################################
public class Form extends FormBase implements ewe.data.HasProperties{
//##################################################################
//public static String globalIconName;
public static IImage globalIcon;
/**
* This is the default title that will be assigned to a new Form.
**/
public static String untitledTitle = "untitled";

public boolean exitSystemOnClose = false;
/**
* This tells the Form to dismantle itself when it closes.
**/
public boolean dismantleOnClose = false;
public boolean resizable = false;
/**
* This option tells the Form to keep a reference to the Frame it creates for
* itself to save time if it is closed and then re-shown.
**/
public boolean keepFrame = false;
/**
* If this is true then the Frame containing the Form will resize itself to avoid
* the SIP when the SIP is turned on.
**/
public boolean resizeOnSIP = false;
public boolean moveable = !Gui.isPDA;//true;
public boolean hasTopBar = !Gui.isSmartPhone;//true;
public boolean noBorder = Gui.isSmartPhone;//false;
/**
* Set this true to accept files dropped from the file manager.
**/
public boolean acceptsDroppedFiles = false;
/**
* This is used to monitor the state of the Form. It is only valid when show()/execute() is
* called on the Form.
**/
public Handle handle;

public Control
	yes = null, no = null, cancel = null, ok = null,
	apply = null, reset = null, deflt = null, back = null;

public IImage windowIcon;
/**
* Set this to be greater than zero to limit the number of buttons displayed at the bottom of the Form.
**/
public int buttonsPerRow = 0;
/**
* If this is set to a control then this control will appear to the right of the
* title bar. If it is activated it will cause the form to exit
* with the value IDOK.
*/
public Control titleOK = null;
/**
* If this is set to a control then this control will appear to the right of the
* title bar. If it is activated it will cause the form to exit
* with the value IDCANCEL.
*/
public Control titleCancel = null;
/**
* The buttons to be displayed at the bottom of the Form (if any) are stored here.
**/
protected Vector buttons = null;
/**
* Use this to add controls to the top of the form, below the
* title bar. It is initially null, but you can create a CellPanel,
* add controls to it and set topControls to point to it.
* This panel will stretch horizontally across the top of the
* form but it will not stretch vertically. You can use it to
* add a menu bar, for example, to your form.
*/
public CellPanel topControls;
/**
* Use this to add controls to the title bar of the form.
* It is initially null, but you can create a CellPanel, add controls
* to it and then set titleControls to point to it. These controls
* will appear to the right of the title, but to the left of any
* titleOK or titleCancel buttons.
*/
public CellPanel titleControls;
/**
* This is the panel that will contain any of the special buttons you add.
*/
public CellPanel buttonsPanel;
/**
* This explicitly sets the control which will first get focus when the Form is displayed.
**/
public Control firstFocus;
/**
* This is the value that was sent to close(int exitCode) or
* exit(int exitCode).
*/
public int exitValue;
/**
* The title of the form.
*/
public String title = untitledTitle;
/**
If this is not null, then the window that the Form is displayed in will have its title
set to this value regardless of whether it is the top level form or not.
**/
public String windowTitle = null;
/**
* This is set true when doButtons() is called to add close buttons to the
* form. If it is false when the form is displayed, then the Form will
* automatically add an OK button to the title of the form.
*/
public boolean exitButtonDefined = false;
/**
* These are flags that you want to set for any new Window being created to display this Form.
It should consist of WindowConstants.FLAG_xxx values OR'ed together.
**/
public int windowFlagsToSet = 0;
/**
* These are flags that you want to clear for any new Window being created to display this Form.
It should consist of WindowConstants.FLAG_xxx values OR'ed together.
**/
public int windowFlagsToClear = 0;
/**
* Set this to be a valid Window.TaskBarIconInfo if you want to display an icon in the taskbar
* when this Form is displayed. It will replace the application icon with this and then restore it
* when it closes. If there is no application icon, it will add a new icon.
**/
public Window.TaskBarIconInfo taskbarIcon;

private boolean pushedSoftkey;

//===================================================================
public Form()
//===================================================================
{
	moveable = resizable =  Gui.screenIs(Gui.DESKTOP_SCREEN);
	//if (globalIconName != null) windowIcon = new Image(globalIconName);
	windowIcon = globalIcon;
}
/**
* This is called by the exit() method to see if the form can be
* closed. It should return true if it can be closed and false
* if it cannot.
*/
//-------------------------------------------------------------------
protected boolean canExit(int exitCode)
//-------------------------------------------------------------------
{
	return true;
}
/**
* This will call the canExit() method and if it returns true, it will
* close the form.
*/
//==================================================================
public boolean exit(int exitCode)
//==================================================================
{
	Control c = Gui.focusedControl();
	if (c != null && c.isChildOf(this)) Gui.takeFocus(null,ByRequest);
	if (!canExit(exitCode)) return false;
	close(exitCode);
	return true;
}
/**
 * Tests if the Form has an exit button defined for it.
 * @return true if the Form has an exit button defined for it.
 */
//==================================================================
public boolean hasExitButton()
//==================================================================
{
	return (titleOK != null || titleCancel != null || exitButtonDefined);
}
//===================================================================
public MenuItem makeMenuItemForForm(String label, String action, String iconName, Object maskOrColor)
//===================================================================
{
	return makeMenuItemForForm(label,action,ImageCache.cache.get(iconName,maskOrColor));
}
//===================================================================
public MenuItem makeMenuItemForForm(String label, String action, IImage icon)
//===================================================================
{
	if (action == null) action = label;
	MenuItem mi = new MenuItem(label);
	mi.action = action;
	mi.label = label;
	if (icon != null) {
		mi.image = new IconAndText(icon,label,getFontMetrics());
		((IconAndText)mi.image).textColor = null;
	}
	return mi;
}

//===================================================================
public Control makeButtonForForm(String text, IImage icon, int hotkey)
//===================================================================
{
 	Control c = getButton(text).setHotKey(0,hotkey);
	c.image = icon;
	if (c instanceof mButton) ((mButton)c).textPosition = RIGHT;
	if (hasModifier(MouseSensitive,false)) c.modify(MouseSensitive,0);
	return c;
}

//===================================================================
public Control makeButtonForForm(String text, String iconName, Object maskOrColor, int hotkey)
//===================================================================
{
	return makeButtonForForm(text,ImageCache.cache.get(iconName,maskOrColor),hotkey);
}

/**
Create and return the default OK or Cancel button.
* @param whichButton one of OKB, DEFOKB, CANCELB, or DEFCANCELB.
* @param textToUse the text to use with the button, or null to use the icon only.
* @return the button created. You should assign this button to either the "ok" or "cancel"
field.
*/
//===================================================================
public Control makeDefaultButton(int whichButton, String textToUse)
//===================================================================
{
	if ((whichButton & (OKB|DEFOKB)) != 0)
		return makeButtonForForm(textToUse,tick,IKeys.ENTER);
	else if ((whichButton & (CANCELB|DEFCANCELB)) != 0)
		return makeButtonForForm(textToUse,cross,IKeys.ESCAPE);
	else
		return null;
}
/**
Create and return the default OK or Cancel button.
* @param whichButton one of OKB, DEFOKB, CANCELB, or DEFCANCELB.
* @param useText true to use the standard OK/Cancel lables, false to use the icon only.
* @return the button created. You should assign this button to either the "ok" or "cancel"
field.
**/
//===================================================================
public Control makeDefaultButton(int whichButton, boolean useText)
//===================================================================
{
	if ((whichButton & (OKB|DEFOKB)) != 0)
		return makeButtonForForm(useText ? "OK" : null,tick,IKeys.ENTER);
	else if ((whichButton & (CANCELB|DEFCANCELB)) != 0)
		return makeButtonForForm(useText ? "Cancel" : null,cross,IKeys.ESCAPE);
	else
		return null;
}
//===================================================================
public boolean placeCancelOnLeft()
//===================================================================
{
	return reverse;
}
//-------------------------------------------------------------------
private Control addButton(int what,int mask,String text,IImage icon,int hot)
//-------------------------------------------------------------------
{
	int whichButton = (what & mask);
	if (whichButton == 0) return null;
	Control c = makeButtonForForm(text,icon,hot);
	if (c == null) return null;
	exitButtonDefined = true;
	if (buttons == null) buttons = new Vector();
	buttons.add(c);
	return c;
}

private static boolean reverse = (Gui.getGuiFlags() & Window.GUI_FLAG_REVERSE_OK_CANCEL) != 0;
/**
* This is used to add a row of buttons at the bottom of the form, using
* The value of "which" can be the logical OR of any of the pre-defined
* button values (e.g. YESB|NOB|OKB).
*/
//==================================================================
public void doButtons(int which)
//==================================================================
{
	boolean first = !reverse;

	if ((which & (YESB|NOB)) == (YESB|NOB))
		if ((which & (CANCELB|DEFCANCELB)) != 0)
			if (!first){
				cancel = addButton(which,DEFCANCELB|CANCELB,"Cancel",cross,IKeys.ESCAPE);
				no = addButton(which,NOB,"No",stop,'n');
				yes = addButton(which,YESB,"Yes",tick,IKeys.ENTER);
			}
	for (int i = 0; i<2; i++){
		if (first){
			if (ok == null) ok = addButton(which,OKB,"OK",tick,'o');
			if (ok == null) ok = addButton(which,DEFOKB,"OK",tick,IKeys.ENTER);
			if (yes == null) yes = addButton(which,YESB,"Yes",tick,reverse ? IKeys.ENTER : 'y');
		}else{
			if (no == null) no = addButton(which,NOB,"No",stop,reverse ? IKeys.ESCAPE : 'n');
			if (cancel == null) cancel = addButton(which,CANCELB,"Cancel",cross,'c');
			if (cancel == null) cancel = addButton(which,DEFCANCELB,"Cancel",cross,IKeys.ESCAPE);
		}
		first = !first;
	}
	if ((which & CANCELB) == 0)
	if (back == null) back = addButton(which,BACKB,"<< Back",null,'b');
}
/**
* This is used to add custom buttons to the bottom of the bar.
*/
//==================================================================
public void addButton(Control b)
//==================================================================
{
	if (b == null) return;
	if (buttons == null) buttons = new Vector();
	if (hasModifier(MouseSensitive,false)) b.modify(MouseSensitive,0);
	buttons.add(b);
}
/**
* Used to create a new Button for the Form. By default it simply creates an mButton().
**/
//==================================================================
public Control getButton(String text)
//==================================================================
{
	return new mButton(text);
}

public static int BUTTONS_TO_SOFT_KEY_FIRST_BUTTON_SEPARATE = 0x1;
public static int BUTTONS_TO_SOFT_KEY_MENU_ALWAYS = 0x2;
public static int BUTTONS_TO_SOFT_KEY_USE_FIRST_SOFT_KEY = 0x4;

//===================================================================
public static Vector gatherButtons(Container buttons,Vector destination)
//===================================================================
{
	if (destination == null) destination = new Vector();
	for (Iterator it = buttons.getSubControls(); it.hasNext();){
		Object c = it.next();
		if (c instanceof mButton) destination.add(c);
		else if (c instanceof Container) gatherButtons((Container)c, destination);
	}
	return destination;
}
/**
Add a container which contains mButton objects to the SoftKeyBar.
**/
//===================================================================
public boolean buttonsToSoftKeyBar(Container buttons, String menuName, int options)
//===================================================================
{
	if (buttons == null) return false;
	return buttonsToSoftKeyBar(gatherButtons(buttons,null),menuName,options);
}
/**
Add a Vector of  mButton objects to a Menu.
**/
//===================================================================
public Menu buttonsToMenu(Vector buttons, Menu destination)
//===================================================================
{
	if (destination == null) destination = new Menu();
	Menu m = destination;
	if (buttons == null || buttons.size() == 0) return m;
	for (int i = 0; i<buttons.size(); i++){
		mButton mb = (mButton)buttons.get(i);
		m.addItem(createMenuItem(mb));
	}
	return m;
}

/**
Add a Vector of  mButton objects to the SoftKeyBar.
**/
//===================================================================
public boolean buttonsToSoftKeyBar(Vector buttons, String menuName, int options)
//===================================================================
{
	boolean firstButtonSeparate = (options & BUTTONS_TO_SOFT_KEY_FIRST_BUTTON_SEPARATE) != 0;
	boolean menuAlways = (options & BUTTONS_TO_SOFT_KEY_MENU_ALWAYS) != 0;
	int dest = (options & BUTTONS_TO_SOFT_KEY_USE_FIRST_SOFT_KEY) != 0 ? 1 : 2;
	//
	if (!SoftKeyBar.usingSoftKeys()) return false;
	if (buttons == null || buttons.size() == 0) return false;
	//
	SoftKeyBar sk = makeSoftKeys();
	//
	if (firstButtonSeparate){
		sk.setKey(1,(mButton)buttons.get(0));
		((mButton)buttons.get(0)).addListener(this);
		if (buttons.size() == 1) return true;
	}
	//
	Menu m = null;
	for (int i = firstButtonSeparate ? 1 : 0; i<buttons.size(); i++){
		mButton mb = (mButton)buttons.get(i);
		mb.addListener(this);
		if (i == buttons.size()-1 && m == null && !menuAlways){
			sk.setKey(dest,mb);
			return true;
		}else{
			if (m == null) m = new Menu();
			m.addItem(sk.createMenuItem(mb));
		}
	}
	sk.setKey(dest,menuName,m);
	return true;
}
/**
 * This is called before the Form is displayed to setup the buttons as requested by doButtons().
	If no exit button has been defined for the Form, then titleOK is set to a new button.
 */
//-------------------------------------------------------------------
protected void checkButtons()
//-------------------------------------------------------------------
{
	if (buttons != null) {
		if (buttons.size() != 0){
			if (Gui.isSmartPhone && getSoftKeyBarFor(null) == null){
				buttonsToSoftKeyBar(buttons,
					(no != null && cancel != null) ? "No/Cancel" : "Actions"
					,BUTTONS_TO_SOFT_KEY_FIRST_BUTTON_SEPARATE);
			}else{
				CellPanel p = new CellPanel();
				p.defaultTags.set(INSETS,new Insets(0,1,0,1));
				p.modify(AlwaysEnabled|NotAnEditor,0); // Just in case a dialog pops up with global disabling.
				for (int i = 0; i<buttons.size(); i++){
					p.addNext((Control)buttons.get(i));
					if ((buttonsPerRow > 0) && (((i+1)%buttonsPerRow) == 0))
						p.endRow();
				}
				p.endRow();
				CellPanel p2 = buttonsPanel = new CellPanel();
				p.defaultTags.set(INSETS,new Insets(2,2,2,2));
				p2.addLast(p).setControl(CENTER);// p2.borderStyle = Graphics.EDGE_SUNKEN;
			}
		}
	}
	if (!hasExitButton()){
		if (Gui.isSmartPhone){
			if (getSoftKeyBarFor(null) == null){
				SoftKeyBar sk = makeSoftKeys();
				sk.setKey(1,"Close|"+EXIT_IDCANCEL,close,null);
			}
		}else{
			titleOK = new mButton(close);//getButton("OK");
			titleOK.backGround = Color.DarkBlue;
			((mButton)titleOK).insideColor = getBackground();
		}
	}
	if (titleOK != null) titleOK.modify(AlwaysEnabled|NotAnEditor,0);
	if (titleCancel != null) titleCancel.modify(AlwaysEnabled|NotAnEditor,0);
}

//==================================================================
public void make(boolean reMake)
//==================================================================
{
	if (made) return;
	if (buttonsPanel != null) {
		addLast(buttonsPanel).setCell(HSTRETCH);
	}
	super.make(reMake);
	//toControls();
}
/**
* This is used to actually create the frame for the form. The
* implementation of this is simply: {return new FormFrame(this,options);}
* but it can be overridden to return a different FormFrame.
*/
//-------------------------------------------------------------------
protected FormFrame makeFrame(int options) {return new FormFrame(this,options);}
//-------------------------------------------------------------------
/**
* This is used to do a customized setup of the FormFrame created for the Form.
**/
//-------------------------------------------------------------------
protected void setupFrame(FormFrame f,int options){}
//-------------------------------------------------------------------
//-------------------------------------------------------------------
private static boolean waitShown = false;
private static boolean wasWaiting = false;

/**
 * This displays a wait cursor until the next Form is shown.
 */
//===================================================================
public static void showWait()
//===================================================================
{
	if (waitShown) return;
	ewe.sys.Vm.showWait(true);
	waitShown = true;
}
/**
* This requests that a wait Cursor be shown IF it was shown before the last
* Form was displayed - otherwise it has no effect. continueWait() is called
* automatically when a MessageBox exits.
**/
//===================================================================
public static void continueWait()
//===================================================================
{
	if (wasWaiting) showWait();
	wasWaiting = false;
}
/**
* This cancels the display of the wait Cursor IF it was requested.
**/
//===================================================================
public static void cancelWait()
//===================================================================
{
	if (waitShown) ewe.sys.Vm.showWait(false);
	wasWaiting = waitShown = false;
}

public static void mb(String text)
{
	ewe.sys.Vm.messageBox("Debug",text,ewe.sys.Vm.MB_OK);
}
/**
 * All show()/exec()/execute() methods eventually call this method.
	It is used to create and display the Frame/Window for the Form. It also creates the Handle
	for the Form and sets it to Handle.Running.
 * @param parent The parent Frame (may be null).
 * @param listener A listener for listening to Form events (may be null).
 * @param modal true if the Form is to be displayed modally.
 * @param options one of the Gui.XXX_FRAME options along with the Gui.NEW_WINDOW option.
 * @return The FormFrame created and displayed.
 */
//-------------------------------------------------------------------
protected FormFrame doShowExec(Frame parent,EventListener listener,boolean modal,int options)
//-------------------------------------------------------------------
{
	options = Gui.fixShowOptions(parent,options);
	FormFrame f = getFormFrame(options);
	f.resizeOnSIP = resizeOnSIP;
	f.backGround = getBackground();
	setupFrame(f,options);
	f.addListener(this);
	checkButtons();
	pushedSoftkey = false;
	if (softkeyBars != null){
		SoftKeyBar got = (SoftKeyBar)softkeyBars.get("default");
		SoftKeyBar.push(got);
		pushedSoftkey = true;
	}else{
	}
	if (modal) Gui.execFrame(f,parent,options);
	else Gui.showFrame(f,parent,options);
	Window w = getWindow();
	w.name = title;
	if (windowTitle != null){
		w.name = windowTitle;
		w.setTitle(windowTitle);
	}
	if (listener != null) addListener(listener);
	Control c = firstFocus;
	if (c == null) focusFirst();
	else Gui.takeFocus(c,ByRequest);
	if (taskbarIcon != null) {
		if (mApp.taskbarWindow == null)
			mApp.taskbarWindow = new TaskbarWindow(mApp.mainApp.windowTitle);
		String nm = getClass().getName();
		getProperties().set("oldIcon",mApp.taskbarWindow.getIcon());
		mApp.taskbarWindow.addIcon(nm,taskbarIcon.nativeIcon);
		mApp.taskbarWindow.setIconAndTip(nm,taskbarIcon.tip);
	}
	//
	if (waitShown) ewe.sys.Vm.showWait(false);
	wasWaiting = waitShown;
	waitShown = false;
	//
	formShown();
	if (handle == null) handle = new Handle();
	handle.set(Handle.Running);
	return f;
}
/**
 * Change the title of the Form on-screen. This will only work if the Form was shown()/executed()
 * so that it is considered the top-level Form.
 * @param newTitle The new title.
 */
//===================================================================
public void setTitle(String newTitle)
//===================================================================
{
	title = newTitle;
	if (title == null) title = "";
	try{
		Window w = getWindow();
		FormFrame ff = (FormFrame)getFrame();
		if (ff != null && ff.myForm != this) return;
		if (ff != null && ff.closeWindow && w != null) w.setTitle(title);
		ff.title.setText(title);
		ff.titleBar.repaintNow();
	}catch(Exception e){}
}
/**
 * Change the title of the top-level Form on-screen even if this Form is not a top-level form.
 * @param newTitle The new title.
 */
//===================================================================
public void setTopLevelTitle(String newTitle)
//===================================================================
{
	try{
		FormFrame ff = (FormFrame)getFrame();
		ff.myForm.setTitle(newTitle);
	}catch(Exception e){}
}
/**
* Call this ONLY after a show() call. It waits until the Form has been painted.
* If the form is being placed in a new Window this will occur when the Window manager
* sends a PAINT message to the Window. If the Form is within an already displayed Frame
* then this will return immediately.
**/
//===================================================================
public boolean waitUntilPainted(int timeOut)
//===================================================================
{
	Frame f = getFrame();
	if (f == null) return false;
	if (!Gui.isWindowFrame(f)) return true;
	Window w = f.getWindow();
	if (w == null) return true;
	return w.waitUntilPainted(timeOut);
}
/**
* This is called to indicate that the Form has been made and has been displayed on the screen.
**/
//-------------------------------------------------------------------
protected void formShown()
//-------------------------------------------------------------------
{
}
/**
* This is called to indicate that the Form is about to close and it causes a FormEvent.CLOSED event to be posted.
* If you override it you should call super.formClosing(). There is no way of stopping the
* Form closing at this point.
**/
//-------------------------------------------------------------------
protected void formClosing()
//-------------------------------------------------------------------
{
	if (taskbarIcon != null && mApp.taskbarWindow != null)
		mApp.taskbarWindow.restoreIcon(getProperties().getValue("oldIcon",null));
	postEvent(new FormEvent(FormEvent.CLOSED,this));
	super.formClosing();
}
/**
* This waits indefinitely for the form to close and returns the exitValue.
* @return the exitValue of the Form.
* @exception IllegalStateException if the Form has no handle (i.e. has not been opened).
*/
//===================================================================
public final int waitUntilClosed() throws IllegalStateException
//===================================================================
{
	Handle handle = this.handle;
	if (handle == null) throw new IllegalStateException();
	handle.waitOnFlags(Handle.Stopped,TimeOut.Forever);
	if (!(handle.returnValue instanceof ewe.sys.Long)) return exitValue;
	return (int)((ewe.sys.Long)handle.returnValue).value;
}

/**
 * Use this to wait on the Handle of a Form, without keeping a reference to the Form.
 * This allows the Handle to be transferred to another Form while letting this Form
 * be destroyed/garbage collected.
 * @param h The handle of the Form (from the "handle" variable).
 * @param t A TimeOut to wait form.
 * @return The final exited value of the Form as placed in the Handle.
 * @exception InterruptedException If the Thread was interrupted.
 * @exception TimedOutException If the TimeOut expired.
 */
//===================================================================
public static final int waitUntilClosed(Handle h,TimeOut t) throws InterruptedException, TimedOutException
//===================================================================
{
	if (!h.waitUntilStopped(TimeOut.Forever))
		throw new TimedOutException();
	if (!(h.returnValue instanceof ewe.sys.Long)) return 0;
	return (int)((ewe.sys.Long)h.returnValue).value;
}
/**
 * Use this to wait on the Handle of a Form, without keeping a reference to the Form.
 * This allows the Handle to be transferred to another Form while letting this Form
 * be destroyed/garbage collected.
 * @param h The handle of the Form (from the "handle" variable).
 * @return The final exited value of the Form as placed in the Handle.
 */
//===================================================================
public static final int waitUntilClosed(Handle h)
//===================================================================
{
	h.waitOnFlags(Handle.Stopped,TimeOut.Forever);
	if (!(h.returnValue instanceof ewe.sys.Long)) return 0;
	return (int)((ewe.sys.Long)h.returnValue).value;
}
/**
* This waits and returns true if it closed before the timeout.
* @exception IllegalStateException if the Form has no handle (i.e. has not been opened).
**/
//===================================================================
public final boolean waitUntilClosed(TimeOut howLong) throws IllegalStateException
//===================================================================
{
	if (handle == null) throw new IllegalStateException();
	return handle.waitOnFlags(Handle.Stopped,howLong);
}
/**
 * This closes the Form but does not set the handle to a stopped state, instead it returns
 * the still running handle. This allows the handle to be transferred to another Form using
 * setHandle(), so that any Threads waiting for this Form to close will then wait for the
 * other Form to close instead.<p>
 * Transferring the handle to another Form is done using the exec(Handle) method or
 * exec(Handle,Parent,int) method.
* @param dismantle dismantle this Form after closing.
* @param showWait call the showWait() method to display a wait cursor until the next Form is
* displayed.
* @return The running handle for this open Form.
*/
//===================================================================
public Handle closeForTransfer(boolean dismantle,boolean showWait)
//===================================================================
{
	Handle h = handle;
	handle = null;
	close(0);
	if (dismantle) this.dismantle();
	if (showWait) this.showWait();
	return h;
}
/**
* This closes the Form but does not set the handle to a stopped state, instead it returns
 * the still running handle. This allows the handle to be transferred to another Form using
 * setHandle(), so that any Threads waiting for this Form to close will then wait for the
 * other Form to close instead.<p>
 * Transferring the handle to another Form is done using the exec(Handle) method or
 * exec(Handle,Parent,int) method.<p>
This Form will be dismantled after closing and the wait cursor will be displayed until the
new Form is displayed.
* @return The running handle for this open Form.
 */
//===================================================================
public Handle closeForTransfer()
//===================================================================
{
	return closeForTransfer(true,true);
}
/**
* This displays the Form modally within an already displayed parent Frame and then waits for
* the Form to close.
* @param parent The parent Frame for the Form. A null parent will indicate that a new Window
* should be created.
* @param options this should be one of:
* <br>Gui.CENTER_FRAME - Size the Frame to the preferred size of its contents and then
* display it centered in the parent frame (this is the most common option).
* <br>Gui.FILL_FRAME - Fill the entire parent frame.
* <br>Gui.FILL_WIDTH - Fill the entire width of the parent frame.
* <br>Gui.FILL_HEIGHT - Fill the entire height of the parent frame.
* <br><b>If options is 0 then the Frame will not be sized, positioned or painted</b> although it will
* be added as a child of the parent Frame. In this case you must size and position the returned
* FormFrame yourself and then call repaintNow().
* @return The return value of the Form as sent to exit() or close().
*/
//===================================================================
public final int execute(Frame parent,int options)
//===================================================================
{
	exec(parent,options);
	return waitUntilClosed();
}
/**
* This displays the Form modally in a new Window and waits for the Form to close. If the platform does not support
* multiple windows it will be displayed centered within the main application window.
* @return The return value of the Form as sent to exit() or close().
*/
//===================================================================
public final int execute()
//===================================================================
{
	exec();
	return waitUntilClosed();
}
/**
* This will display the form either modally or not.
**/
/*
//===================================================================
public final FormFrame exec(boolean modally)
//===================================================================
{
	if (modally) return exec();
	else return show();
}
*/
/**
* This displays the Form modally in a new Window. If the platform does not support
* multiple windows it will be displayed centered within the main application window.
* @return The FormFrame that was created and displayed.
*/
//===================================================================
public final FormFrame exec()
//===================================================================
{
	return doShowExec(null,null,true,Gui.NEW_WINDOW);//defaultShowOptions);
}
/**
* This displays the Form modally within an already displayed parent Frame.
* @param parent The parent Frame for the Form. A null parent will indicate that a new Window
* should be created.
* @param options this should be one of:
* <br>Gui.CENTER_FRAME - Size the Frame to the preferred size of its contents and then
* display it centered in the parent frame (this is the most common option).
* <br>Gui.FILL_FRAME - Fill the entire parent frame.
* <br>Gui.FILL_WIDTH - Fill the entire width of the parent frame.
* <br>Gui.FILL_HEIGHT - Fill the entire height of the parent frame.
* <br><b>If options is 0 then the Frame will not be sized, positioned or painted</b> although it will
* be added as a child of the parent Frame. In this case you must size and position the returned
* FormFrame yourself and then call repaintNow().
* @return The FormFrame that was created and displayed.
*/
//===================================================================
public final FormFrame exec(Frame parent,int options)
//===================================================================
{
	if (parent == null) return doShowExec(parent,null,true,Gui.NEW_WINDOW);//defaultShowOptions);
	else return doShowExec(parent,null,true,options);
}
/**
* This displays the Form non-modally in a new Window. If the platform does not support
* multiple windows it will be displayed centered within the main application window.
* @return The FormFrame that was created and displayed.
*/
//===================================================================
public final FormFrame show()
//===================================================================
{
	return doShowExec(null,null,false,Gui.NEW_WINDOW);//defaultShowOptions);
}
/**
* This displays the Form non-modally within an already displayed parent Frame.
* @param parent The parent Frame for the Form. A null parent will indicate that a new Window
* should be created.
* @param options this should be one of:
* <br>Gui.CENTER_FRAME - Size the Frame to the preferred size of its contents and then
* display it centered in the parent frame (this is the most common option).
* <br>Gui.FILL_FRAME - Fill the entire parent frame.
* <br>Gui.FILL_WIDTH - Fill the entire width of the parent frame.
* <br>Gui.FILL_HEIGHT - Fill the entire height of the parent frame.
* <br><b>If options is 0 then the Frame will not be sized, positioned or painted</b> although it will
* be added as a child of the parent Frame. In this case you must size and position the returned
* FormFrame yourself and then call repaintNow().
* @return The FormFrame that was created and displayed.
*/
//===================================================================
public final FormFrame show(Frame parent,int options)
//===================================================================
{
	if (parent == null) return doShowExec(parent,null,false,Gui.NEW_WINDOW);//defaultShowOptions);
	else return doShowExec(parent,null,false,options);
}
/**
* @deprecated
*/
//===================================================================
public final FormFrame exec(Frame parent,EventListener listener) {return exec(parent,listener,defaultShowOptions);}//Gui.CENTER_FRAME);}
//===================================================================
/**
* @deprecated
*/
//===================================================================
public final FormFrame exec(Frame parent,EventListener listener,int options)
//===================================================================
{
	return doShowExec(parent,listener,true,options);
}
//==================================================================
/**
* @deprecated
*/
//===================================================================
public final FormFrame show(int options) {return show(null,null,options);}
//===================================================================
/**
* @deprecated
*/
//===================================================================
public FormFrame exec(int options) {return exec(null,(EventListener)null,options);}
//===================================================================
/**
* @deprecated
**/
//===================================================================
public final int execute(int options) {return execute(null,options);}
//===================================================================
/**
* @deprecated
*/
//===================================================================
public final FormFrame show(Frame parent) {return show(null,null);}
//===================================================================
/**
* @deprecated
*/
//===================================================================
public final FormFrame show(Frame parent,EventListener listener) {return show(parent,listener,defaultShowOptions);}//Gui.CENTER_FRAME);}
//===================================================================
/**
* @deprecated
*/
//===================================================================
public final FormFrame show(Frame parent,EventListener listener,int options)
//===================================================================
{
	return doShowExec(parent,listener,false,options);
}
/**
* The Frame the Form is currently displayed in.
**/
protected FormFrame formFrame;
/**
* This is called when the Form is about to be displayed. It creates and
* returns a FormFrame with the contents set to be the Form.
*/
//==================================================================
public FormFrame getFormFrame(int options)
//==================================================================
{
	//checkButtons();
	if (keepFrame && formFrame != null) return formFrame;
	return formFrame = makeFrame(options);
}
//===================================================================
public void dismantle(Control stopAt)
//===================================================================
{
	if (stopAt == this) return;
	if (!keepFrame && formFrame != null)
		formFrame = null;
	if (softkeyBars != null){
		softkeyBars.clear();
		softkeyBars = null;
	}
	super.dismantle(stopAt);
}

//==================================================================
//public void ticked(int id,int late) {close(0);}
//==================================================================
/**
 * This is used to exec a Form using the Handle from another Form (usually got
 * from closeForTransfer()).
 * @param transferredHandle The Handle to use for this Form.
 * @return The FormFrame created.
 */
//===================================================================
public FormFrame exec(Handle transferredHandle)
//===================================================================
{
	FormFrame ff = exec();
	handle = transferredHandle;
	return ff;
}
/**
 * This is used to exec a Form using the Handle from another Form (usually got
 * from closeForTransfer()).
 * @param transferredHandle The Handle to use for this Form.
  @param parent The parent Frame to exec() this Form in.
* @param options this should be one of:
* <br>Gui.CENTER_FRAME - Size the Frame to the preferred size of its contents and then
* display it centered in the parent frame (this is the most common option).
* <br>Gui.FILL_FRAME - Fill the entire parent frame.
* <br>Gui.FILL_WIDTH - Fill the entire width of the parent frame.
* <br>Gui.FILL_HEIGHT - Fill the entire height of the parent frame.
* <br><b>If options is 0 then the Frame will not be sized, positioned or painted</b> although it will
* be added as a child of the parent Frame. In this case you must size and position the returned
* FormFrame yourself and then call repaintNow().
* @return The FormFrame created.
*/
//===================================================================
public FormFrame exec(Handle transferredHandle,Frame parent,int options)
//===================================================================
{
	FormFrame ff = exec(parent,null,options);
	handle = transferredHandle;
	return ff;
}
/**
 * Display the Form modally in a new Window and return the Handle that can be used to monitor the Form's open
 * state using waitUntilClosed().
 * @param dismantleOnClose The state to set the dismantleOnClose flag.
 * @return the Handle that can be used to monitor the Form's open
 * state using waitUntilClosed().
 */
//===================================================================
public Handle exec(boolean dismantleOnClose)
//===================================================================
{
	this.dismantleOnClose = dismantleOnClose;
	exec();
	return handle;
}
/**
 * Display the Form modally in a parent Frame and return the Handle that can be used to monitor the Form's open
 * state using waitUntilClosed().
 * @param dismantleOnClose The state to set the dismantleOnClose flag.
* @param parent The parent Frame. If this is null then the Form will be displayed in a new Window.
* @param options this should be one of:
* <br>Gui.CENTER_FRAME - Size the Frame to the preferred size of its contents and then
* display it centered in the parent frame (this is the most common option).
* <br>Gui.FILL_FRAME - Fill the entire parent frame.
* <br>Gui.FILL_WIDTH - Fill the entire width of the parent frame.
* <br>Gui.FILL_HEIGHT - Fill the entire height of the parent frame.
* <br><b>If options is 0 then the Frame will not be sized, positioned or painted</b> although it will
* be added as a child of the parent Frame. In this case you must size and position the returned
* FormFrame yourself and then call repaintNow().
 * @return the Handle that can be used to monitor the Form's open
 * state using waitUntilClosed().
*/
//===================================================================
public Handle exec(boolean dismantleOnClose,Frame parent,int options)
//===================================================================
{
	this.dismantleOnClose = dismantleOnClose;
	exec(parent,options);
	return handle;
}
//===================================================================
public Form getTopmostForm()
//===================================================================
{
	return Gui.getTopmostForm(this);
}
/**
* If this Form is embedded within another Form, then this will close
* the topmost Form.
**/
//===================================================================
public void closeAll(int exitCode)
//===================================================================
{
	getTopmostForm().close(exitCode);
}
/**
* This can be used to stop the Handle of a Form to indicate that it should be considered
* closed.
* @param h The Handle of the open Form.
* @param retCode The exit code the Form should report.
*/
//===================================================================
public static void stopFormHandle(Handle h,int retCode)
//===================================================================
{
	if (h == null) return;
	h.returnValue = new ewe.sys.Long().set(retCode);
	h.set(Handle.Stopped);
}
/**
* This closes the form unconditionally and sets the exitValue to be exitCode.
*/
//==================================================================
public void close(int exitCode)
//==================================================================
{
	exitValue = exitCode;
	//if (exitValue != IDCANCEL) fromControls();
	Control p = null;
	Frame f = getFrame();
	if (f != null) Gui.hideFrame(f);
	if (pushedSoftkey) SoftKeyBar.pop();
	formClosing();
	if (exitSystemOnClose) mApp.mainApp.exit(exitCode);
	if (listeners != null) listeners.clear();
	for (Control c = this; c != null; c = c.getParent())
		if (c instanceof Form)
			stopFormHandle(((Form)c).handle,exitCode);
	if (this instanceof MessageBox) continueWait();
	if (dismantleOnClose) dismantle();
}

private Hashtable softkeyBars;

/**
Create a SoftKeyBar that applies to the entire Form and set the left
and right key functions.
* @return the SoftKeyBar.
*/
//===================================================================
public SoftKeyBar makeSoftKeys(Object left, String leftLabel, Object right, String rightLabel)
//===================================================================
{
	SoftKeyBar sk = makeSoftKeys();
	if (left != null) sk.setKey(1,left,leftLabel);
	if (right != null) sk.setKey(2,right,rightLabel);
	return sk;
}
/**
Create a SoftKeyBar that applies to the entire Form and return it for
you to set the keys in it. If you want a SoftKey which changes for
a particular control, use setSoftKeyBarFor().
**/
//===================================================================
public SoftKeyBar makeSoftKeys()
//===================================================================
{
	SoftKeyBar sb = getSoftKeyBarFor(null);
	if (sb == null) setSoftKeyBarFor(null,sb = new SoftKeyBar());
	return sb;
}
/**
 * Set the SoftKeyBar for a particular Control or the default SoftKeyBar.
 * @param c the particular Control or null to set the default SoftKeyBar.
 * @param bar The SoftKeyBar to use.
 */
//===================================================================
public void setSoftKeyBarFor(Control c, SoftKeyBar bar)
//===================================================================
{
	if (c == this) c = null;
	if (softkeyBars == null) softkeyBars = new Hashtable();
	if (bar != null) bar.addListener(this);
	softkeyBars.put(c == null || c == this ? "default" : (Object)c,bar);
}
/**
 * Set the SoftKeyBar for a particular set of Controls.
 * @param controls a Vector holding a set of Controls.
 * @param bar The SoftKeyBar to use for each of the Controls.
 */
//===================================================================
public void setSoftKeyBarForAll(Vector controls, SoftKeyBar bar)
//===================================================================
{
	if (softkeyBars == null) softkeyBars = new Hashtable();
	if (bar != null) bar.addListener(this);
	for (int i = 0; i<controls.size(); i++){
		Control c = (Control)controls.get(i);
		softkeyBars.put(c == null ? "default" : (Object)c,bar);
	}
}
/**
 * Get the SoftKeyBar for a particular Control or the default SoftKeyBar.
 * @param c the particular Control or null to get the default SoftKeyBar.
 */
//===================================================================
public SoftKeyBar getSoftKeyBarFor(Control c)
//===================================================================
{
	if (softkeyBars == null) return null;
	return (SoftKeyBar)softkeyBars.get(c == null || c == this ? "default" : (Object)c);
}
/**
 * Remove the SoftKeyBar for a particular Control or the default SoftKeyBar.
 * @param c the particular Control or null to remove the default SoftKeyBar.
 */
//===================================================================
public void removeSoftKeyBarFor(Control c)
//===================================================================
{
	if (softkeyBars == null) return;
	softkeyBars.remove(c == null ? "default" : (Object)c);
}
//public boolean
/**
* Make sure you call super.onControlEvent() if you override
* this method.
*/
//==================================================================
public void onControlEvent(ControlEvent ev)
//==================================================================
{
	if (ev.type == ev.PRESSED){
		boolean gotIt = true;
		if (ev.target == yes) exit(IDYES);
		else if (ev.target == no) exit(IDNO);
		else if (ev.target == ok) exit(IDOK);
		else if (ev.target == cancel) exit(IDCANCEL);
		else if (ev.target == back) exit(IDBACK);
		else if (ev.action != null) gotIt = handleAction(ev);
		else gotIt = false;
		if (gotIt) ev.consumed = true;
		super.onControlEvent(ev);
	}else if (ev.type == ev.FOCUS_IN){
		if (ev.target instanceof Control && ((Control)ev.target).isChildOf(this)){
			if (windowTitle != null){
				Window w = getWindow();
				if (w != null) w.setTitle(windowTitle);
			}
			if (softkeyBars != null){
				SoftKeyBar skb = (SoftKeyBar)softkeyBars.get(ev.target);
				if (skb == null) skb = (SoftKeyBar)softkeyBars.get("default");
				if (skb != null) skb.display();
				//if (skb == null) skb = SoftKeyBar.empty;
				//skb.display();
			}
		}
		super.onControlEvent(ev);
	}else
		super.onControlEvent(ev);
}
/**
Fix for action event handling bug in 1.43
**/
/*
//==================================================================
public void onControlEvent(ControlEvent ev)
//==================================================================
{
	if (ev.type == ev.PRESSED){
		if (!(
				ev.target == yes ||
				ev.target == no ||
				ev.target == ok ||
				ev.target == cancel ||
				ev.target == back) && ev.action != null){
					handleAction(ev.action);
					ev.action = null;
		}
	}
	super.onControlEvent(ev);
}
*/
/**
Handle an action type event. This method is called by the default handleAction(ControlEvent ev) method and also by
the default menuItemSelected() method and onSoftKey() method.
By default this method checks if the action is equal to any of the EXIT_XXX constants
and if it is it will call exit() with the appropriate exit code.
* @param action the action value.
* @return true to tell the Form the event has been handled, false if not.
*/
//===================================================================
public boolean handleAction(String action)
//===================================================================
{
	if (action.equals(EXIT_IDCANCEL)) exit(IDCANCEL);
	else if (action.equals(EXIT_IDOK))exit(IDOK);
	else if (action.equals(EXIT_IDYES))exit(IDYES);
	else if (action.equals(EXIT_IDNO))exit(IDNO);
	else if (action.equals(EXIT_IDBACK))exit(IDBACK);
	else return false;
	return true;
}
/**
Handle a ControlEvent that is of type PRESSED and for which the "action" field is
not null. This method is called by the default onControlEvent(). By default this method
returns a call to handleAction(String action).
* @param ev The Control Event.
* @return true to tell the Form the event has been handled and not to pass the event
to its parents, false to continue normal event dispatching.
*/
//===================================================================
public boolean handleAction(ControlEvent ev)
//===================================================================
{
	return handleAction(ev.action);
}
//===================================================================
public void menuItemSelected(MenuItem selected)
//===================================================================
{
	if (selected.action != null) handleAction(selected.action);
}
/**
 * The default onEvent for a Form will call this method
 * if a SoftKeyEvent is sent to the Form. By default it calls handleAction(String action).
 * @param whichKey the SoftKey that was pressed, either 1 or 2.
 * @param action the action associated with the key or menu item.
 * @param selected if a MenuItem was selected, this will be the selected item.
 */
//===================================================================
public void onSoftKey(int whichKey, String action, MenuItem selected)
//===================================================================
{
	if (selected == null) handleAction(action);
	else menuItemSelected(selected);
}
/**
 * This method is called on a SoftKeyEvent. By default it will call se.fireProxyAction()
 * and then call onSoftKey(int,String,MenuItem);
 * @param se the SoftKeyEvent.
 */
//===================================================================
public void onSoftKey(SoftKeyEvent se)
//===================================================================
{
	se.fireProxyAction();
	onSoftKey(se.whichKey, se.action, se.selectedItem);
}
//===================================================================
public void onEvent(Event ev)
//===================================================================
{
	if (ev instanceof DataChangeEvent){
		onDataChangeEvent((DataChangeEvent)ev);
	}else if (ev instanceof WindowEvent){
		WindowEvent we = (WindowEvent)ev;
		if (ev.type == WindowEvent.DATA_DROPPED){
			Rect r = Gui.getRectInWindow(this,null,true);
			if (r != null && r.isIn(we.x,we.y)){
				if (we.data instanceof String) filesDropped(new String[]{(String)we.data});
				else if (we.data instanceof String []) filesDropped((String [])we.data);
			}
		}
	}else if (ev instanceof SoftKeyEvent){
		onSoftKey((SoftKeyEvent)ev);
	}else if (ev instanceof MenuEvent && !(ev instanceof ListEvent) && ev.type == MenuEvent.SELECTED){
		MenuEvent me = (MenuEvent)ev;
		if (me.selectedItem instanceof MenuItem){
			MenuItem mi = (MenuItem)me.selectedItem;
			if (mi.data instanceof Control){
				((Control)mi.data).notifyAction();
			}else menuItemSelected(mi);
		}
	}
	super.onEvent(ev);
}
//===================================================================
public void onDataChangeEvent(DataChangeEvent ev){}
//===================================================================
/**
 * This is called when files are dropped on the Form and acceptsDroppedFiles is true.
 * @param fileNames the list of fileNames dropped.
 */
//===================================================================
public void filesDropped(String [] fileNames) {}
//===================================================================
/*
//-------------------------------------------------------------------
protected Vector fields, fieldNames;
//-------------------------------------------------------------------

//===================================================================
public Object addField(Object component,String fieldName)
//===================================================================
{
	if (fields == null) {
		fields = new Vector();
		fieldNames = new Vector();
	}
	fields.add(component);
	fieldNames.add(fieldName);
	return component;
}
//===================================================================
public Object findField(String fieldName)
//===================================================================
{
	if (fieldNames == null) return null;
	for (int i = 0; i<fieldNames.size(); i++) {
		if (fieldName.equals((String)fieldNames.get(i)))
			return fields.get(i);
	}
	return null;
}
//===================================================================
public String findFieldName(Object component)
//===================================================================
{
	if (fieldNames == null) return null;
	for (int i = 0; i<fieldNames.size(); i++) {
		if (component == fields.get(i))
			return (String)fieldNames.get(i);
	}
	return null;
}
*/

//##################################################################
// Object editing stuff.
//##################################################################
/**
* The object being edited by the Form. Don't set this directly
*/
//protected Object myObject;
/**
* Set the object being edited.
*/
/*
public void setObject(Object obj)
{
	myObject = obj;
	if (made)
		toControls();
}
*/
/**
* Get the object being edited.
*/
/*
public Object getObject()
{
	return myObject;
}
*/
/**
* Set the controls to the contents of the current object.
*/
//public final void toControls() {toControls(myObject);}
/**
* Get the contents of the current object from the controls.
*/
//public final void fromControls() {fromControls(myObject);}
/**
*
*/
//public void toControls(Object obj) {postEvent(FormEvent.TO_CONTROLS,obj);}
/**
*
*/
//public void fromControls(Object obj) {postEvent(FormEvent.FROM_CONTROLS,obj);}
/**
*
*/
//protected void postEvent(int what,Object obj) {FormEvent fe = new FormEvent(what,this); fe.formObject = obj; postEvent(fe);}
/**
* The properties of the Form.
*/
protected PropertyList pl;
//===================================================================
public PropertyList getProperties()
//===================================================================
{
	if (pl == null) pl = new PropertyList();
	return pl;
}
/**
* A special method used by ewe.reflect.FieldTransfer() to transfer data to and from controls and properties.
**/
//===================================================================
public boolean _getSetField(String fieldName,ewe.reflect.Wrapper wrapper,boolean isGet)
//===================================================================
{
	return PropertyList.getSetProperties(this,fieldName,wrapper,isGet);
}

/**
* Returns true if tools are best added to the bottom of Forms on this platform.
**/
//===================================================================
public boolean toolsOnBottom()
//===================================================================
{
	return SipButton.hasSipButton() && !(mApp.rotated || mApp.counterRotated);
}

private CellPanel sipFiller;

void fillSip(int desktopHeight) throws Exception
{
	if (sipFiller == null) throw new NullPointerException();
	Window w = getWindow();
	Point p = Gui.getPosInParent(sipFiller,w);
	int y = p.y;
	Rect r = w.getWindowRect(new Rect(),false); y += r.y;
	w.getWindowRect(r,true); y += r.y;
	int need = y-desktopHeight;
	if (need >= 0)
		sipFiller.setFixedSize(sipFiller.width,sipFiller.height+need);
	((CellPanel)sipFiller.parent).relayout(true);
}
void unfillSip() throws Exception
{
	sipFiller.setFixedSize(sipFiller.width,0);
	((CellPanel)sipFiller.parent).relayout(true);
}
/**
 * A very useful method for creating a Form with a toolbar or menubar. It places it in
	the most appropriate place for the platform. If the platform supports a SIP button on the
	screen (e.g. PocketPC) then a SipButton placeholder will be put in the toolbar and the
	SIP button will always be visible once the Form is displayed.
 * @return An array of CellPanels. The panel at index 0 will be the panel you should use for
	your toolbar. The panel at index 1 will be the panel for adding your main controls.
 */
//===================================================================
public CellPanel [] addToolbar()
//===================================================================
{
	CellPanel [] ret = new CellPanel[2];
	ret[0] = new CellPanel();
	ret[1] = new CellPanel();
	if (toolsOnBottom()){
		sipFiller = new CellPanel();
		addLast(ret[1]);
		addLast(sipFiller).setCell(HSTRETCH).setPreferredSize(10,0);
		addLast(ret[0]).setCell(HSTRETCH);
		ret[0].setBorder((EDGE_ETCHED & ~BF_RECT)|BF_TOP,2);
		/*
		new mThread(){
			public void run(){
				try{
					sleep(3000);
					fillSip();
				}catch(Exception e){}
			}
		}.start();
		*/
	}else{
		ret[0].setBorder((EDGE_ETCHED & ~BF_RECT)|BF_BOTTOM,2);
		addLast(ret[0]).setCell(HSTRETCH);
		addLast(ret[1]);
	}
	ret[0] = SipButton.placeIn(ret[0]);
	return ret;
}

/**
 * A very useful method for creating a Form with a tabbed panel. It places it in
	the most appropriate place for the platform.
* @param useExpandingTabs set this true if you wish for the created tabbed panel to be the autoexpanding type.
 * @return An array of Controls. The Control at index 0 will be the tabbed panel you should use. If you
	specified an expanding tabbed panel, then the panel at index 1 will be the panel for adding your main controls - otherwise
	it will be null.
*/
//===================================================================
public Control [] addTabbedPanel(boolean useExpandingTabs)
//===================================================================
{
	boolean tb = toolsOnBottom();
	Control [] ret = new Control[2];
	mTabbedPanel t;
	if (useExpandingTabs){
		Object [] all = mTabbedPanel.getExpandingTabbedPanel(!tb);
		t = (mTabbedPanel)all[0];
		SplittablePanel s = (SplittablePanel)all[1];
		addLast(s);
		CellPanel cv = (CellPanel)all[2];
		if (tb){
			CellPanel actual = new CellPanel();
			cv.addLast(actual);
			sipFiller = new CellPanel();
			cv.addLast(sipFiller).setCell(HSTRETCH).setPreferredSize(10,0);
			cv = actual;
		}
		ret[1] = cv;
	}else{
		t = new mTabbedPanel();
		t.tabLocation = tb ? t.SOUTH : t.NORTH;
		addLast(t);
	}
	ret[0] = t;
	SipButton.placeIn(t);
	return ret;
}

/**
* This will set the title of the Form if it has not already been set.
**/
//===================================================================
public void defaultTitleTo(String newTitle)
//===================================================================
{
	if (title.equals(untitledTitle)) title = newTitle;
}

/**
 * This creates a new Window for the Form on request (when being shown). You can use this to
	setup the window icon.
 * @return a new Window for the Form.
 */
//===================================================================
public Window createWindow()
//===================================================================
{
	Window w = new Window();
	return w;
}
//-------------------------------------------------------------------
protected Control getFirstFocus()
//-------------------------------------------------------------------
{
	if (firstFocus == null) return super.getFirstFocus();
	else return firstFocus;
}

//===================================================================
public void shown()
//===================================================================
{
	super.shown();
	if (windowIcon != null){
		Window w = getWindow();
		FormFrame ff = (FormFrame)getFrame();
		if (ff != null){
			if (ff.myForm != this) return;
			if (ff != null && ff.closeWindow && w != null) w.setIcon(windowIcon);
		}
	}
	if (acceptsDroppedFiles){
		Window w = getWindow();
		if (w != null){
			w.addListener(this);
			w.setInfo(Window.INFO_ACCEPT_DROPPED_FILES,null,null,1);
		}
	}
}

/**
* This modifies the setup for the Form to be one suitable to run full screen on a SmartPhone
* compatible IF the application actually is running on a SmartPhone device.
* <p>
* Setup the title and other Form options before calling this method.
*
* @return true if the application IS running on a SmartPhone device.
*/
//===================================================================
public boolean modifyForSmartPhone()
//===================================================================
{
	if (!Gui.isSmartPhone) return false;
	windowTitle = title;
	moveable = resizable = hasTopBar = false;
	windowFlagsToClear |= Window.FLAG_HAS_CLOSE_BUTTON;
	return true;
}
/**
Set OK and Cancel keys appropriate for the platform and return this Form.
**/
//===================================================================
public Form setOKCancel()
//===================================================================
{
	Gui.setOKCancel(this);
	return this;
}
//===================================================================
public MenuItem createMenuItem(String label, String action, String iconName, Object maskOrColor)
//===================================================================
{
	return createMenuItem(label,action,ImageCache.cache.get(iconName,maskOrColor));
}
//===================================================================
public MenuItem createMenuItem(String label, String action, IImage icon)
//===================================================================
{
	if (action == null) action = label;
	MenuItem mi = new MenuItem();
	mi.action = action;
	mi.label = label;
	if (icon != null) {
		mi.image = new IconAndText(icon,label,getFontMetrics());
		((IconAndText)mi.image).textColor = null;
	}
	return mi;
}
/**
Return a MenuItem that will be used as a proxy for a specific buton. When
the menu item is selected the specified button will fire of an action event (ControlEvent.PRESSED)
**/
//===================================================================
public MenuItem createMenuItem(mButton button)
//===================================================================
{
	MenuItem mi = createMenuItem(button.text, button.action, button.image);
	mi.data = button;
	button.addListener(this);
	return mi;
}
//##################################################################
}
//##################################################################

