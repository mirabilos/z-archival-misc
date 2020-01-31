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
import ewe.sys.*;
import ewe.util.*;
import ewe.reflect.*;

//##################################################################
public class mApp extends MainWindow implements EventListener, Runnable, FieldListener{
//##################################################################

public static mApp mainApp;
public static int tickTime = 100;
public static int lastEvent = ewe.sys.Vm.getTimeStamp();
/*
This is the main frame for the mApp and is usually setup automatically by the VM.
If the operating system does not support multiple windows then this frame is used
as the main Frame for all child frames of the application.
*/
public static Frame appFrame;//,mainFrame = new CarrierFrame();
public static String platform;
public static Font guiFont;
public static String [] programArguments;
public static Object runObject;
protected static Method mainMethod;
public static Class runClass;
public static VMOptions vmOptions;
/**
* Set this to true before calling setupMainWindow() to display the application rotated by 90 degrees
* clockwise (which puts the controls on the right - when the user rotates it back by 90 degrees anti-clockwise).
**/
public static boolean rotated = (Vm.getParameter(Vm.VM_FLAGS) & Vm.VM_FLAG_ROTATE_SCREEN) != 0;
/**
* Set this to true before calling setupMainWindow() to display the application rotated by 90 degrees
* anti-clockwise (which puts the controls on the left - when the user rotates it back by 90 degrees clockwise).
**/
public static boolean counterRotated = (Vm.getParameter(Vm.VM_FLAGS) & Vm.VM_FLAG_COUNTER_ROTATE_SCREEN) != 0;
/**
* If this is set to a valid icon in the setupMainWindow() method, then the icon will
* display in the taskbar as long as this application runs. Pressing the icon will
* bring the application to the front unless you override iconPressed().
* <p>
* The value that you place here must be the result of ewe.fx.Image.toIcon(ewe.fx.Image iconMask).
* Note that the iconMask must be stored as a monochrome ".bmp" file under Win32/WinCE.
<p>
An example is:
<pre>
taskbarIcon = new ewe.fx.Image("ewe/ewesmall.bmp").toIcon(new ewe.fx.Image("ewe/ewesmallmask.bmp"));
</pre>
**/
public static Object taskbarIcon;
/**
* If you set taskbarIcon in the setupMainWindow() method, then this will be set to a TaskbarWindow
* that will be created. You can always add and display icons on the window later.
**/
public static TaskbarWindow taskbarWindow;

public void doPaint(Graphics g,Rect r){}

protected boolean windowInitiallyVisible = true;
protected String windowTitle = "Ewe Application";
protected Rect windowRect = null;
protected int windowFlagsToSet = 0;
protected int windowFlagsToClear = 0;
protected boolean checkMonochrome = false;

//===================================================================
public void run()
//===================================================================
{
	if (mainMethod != null){
		new mThread(){
			public void run(){
				mainMethod.invoke(null,new Wrapper[]{new Wrapper().setArray(programArguments)},new Wrapper());
				if (mainMethod.invocationError != null){
					mainMethod.invocationError.printStackTrace();
					try{
						int got = Vm.messageBox("Exception in main()","Exception thrown in main(): \n"+mainMethod.invocationError.toString()+"\n\nStop the application?",Vm.MB_YESNO);
						if (got == Vm.IDYES) {
							//Vm.messageBox("Exiting!","Exiting the application",Vm.MB_OK);
							exit(0);
						}
					}catch(Exception e){
						Vm.debug("Exiting application due to exception thrown in main().");
						exit(0);
					}
				}
			}
		}.start();
		/*
		Coroutine c = new Coroutine(null,mainMethod,programArguments);
		c.showExceptionTrace = true;
		*/
		return;
	/*
		Wrapper [] w = new Wrapper[1];
		w[0] = new Wrapper();
		w[0].setObject(programArguments);
		mainMethod.invoke(null,w,null);
		if (mainMethod.invocationError != null){
			mainMethod.invocationError.printStackTrace();
			Coroutine.sleep(5000);
		}
		return;
	*/
	}

	if (runObject instanceof Runnable)
	try{
		((Runnable)runObject).run();
	}catch(Throwable t){
		t.printStackTrace();
		Coroutine.sleep(5000);
	}else{
		ewe.sys.Vm.debug(runClass+" cannot be run by the VM.");
		Coroutine.sleep(5000);
	}
	exit(0);
}
//==================================================================
protected boolean canExit(int code)
//==================================================================
{
	return true;
}
//==================================================================
public void tryExit(int code)
//==================================================================
{
	if (canExit(code)) exit(code);
}

private boolean lockState = false;
//==================================================================
public final boolean isLocked() {return lockState;}
//==================================================================

private boolean doStartMessage = false;//true;

/**
* This is used if the mApp you are creating is not necessarily the only mApp
* that will be created.<br>If the parameter is true then no application setup
* will be done. If the parameter is false then setup will be done IF this is
* the first instance of an mApp being created.
**/
//===================================================================

public mApp(boolean dontSetupAtAll)
//===================================================================
{
	if (mainApp == null && !dontSetupAtAll) fullSetup(true);
}
//===================================================================
public mApp()
//===================================================================
{
	fullSetup(true);
}
/**
* This is used by the system when constructing for a specified run object. The value
* passed should be null because it is not used. The setRunObject() is actually used
* to specify the object to run.
**/
//===================================================================
public mApp(Class runClass)
//===================================================================
{
	mApp.runClass = runClass;
	fullSetup(false);
}
//===================================================================
public void setRunObject(Object obj)
//===================================================================
{
	runObject = obj;
}
//===================================================================
protected void fullSetup(boolean visible)
//===================================================================
{
	if (true){
		mainApp = this;
		programArguments = ewe.sys.Vm.getProgramArguments();
		if (!visible) windowFlagsToClear = FLAG_IS_VISIBLE;
		if (vmOptions.singleWindowed) Vm.setParameter(Vm.SET_NO_WINDOWS,1);
		if (vmOptions.fixedSIPButton) Vm.setParameter(Vm.SET_ALWAYS_SHOW_SIP_BUTTON,1);
		setupMainWindow();
		Gui.fullScreen = (Rect)getInfo(INFO_PARENT_RECT,null,Gui.fullScreen,0);
		platform = ewe.sys.Vm.getPlatform();
		char pf = platform.charAt(0);
		//if (pf == 'j' || pf == 'J') setRect(0,0,240,320);

		//_setTimerInterval(tickTime);
		Frame f = contents;
		f.contentsOnly = true;
		show(f,true);
		if (checkMonochrome)
			if ((pf == 'p' || pf == 'P') || ((Vm.getParameter(Vm.VM_FLAGS) & Vm.VM_FLAG_IS_MONOCHROME) != 0)){
					globalDrawFlat = true;
					//appFrame.
					modify(DrawFlat,0);
					Color.setMonochrome(true);
			}
		//visibleWidth = width;
		//visibleHeight = height;
		if ((windowFlagsToSet & FLAG_FULL_SCREEN) != 0){
			SoftKeyBar.hide();
		}
		setup();
	}
}

public static final int ROTATE_NORMAL = 0;
public static final int ROTATE_CLOCKWISE = 1;
public static final int ROTATE_COUNTER_CLOCKWISE = 2;

private static boolean couldMove = Graphics.canMove;
private static boolean couldCopy = Graphics.canCopy;

//===================================================================
public static boolean rotateScreen(int type)
//===================================================================
{
	rotated = counterRotated = false;
	Graphics.canMove = couldMove;
	Graphics.canCopy = couldCopy;
	if (type == ROTATE_CLOCKWISE) rotated = true;
	if (type == ROTATE_COUNTER_CLOCKWISE) counterRotated = true;
	if (rotated || counterRotated)
		Graphics.canCopy = Graphics.canMove = false;
	if (!didSetup) return true;
	mainApp.doSpecialOp(SPECIAL_ROTATE_SCREEN|(type << 8),null);
	return true;
}

private static boolean didSetup = false;
/**
This is called by the system during the constructor to setup this mApp as the
application's Main Window.
<p>
The Main Window is always present even if it is invisible. When run as an Applet in
a web page the application's Main Window is usually shown within the applet area on
the page.
<p>
Override this method to setup options for the application's Main Window and then call
super setupMainWindow() to continue the setup.
**/
//-------------------------------------------------------------------
protected void setupMainWindow()
//-------------------------------------------------------------------
{
	//ewe.sys.Vm.debug("Can copy: "+Graphics.canCopy);
	if (rotated) {
		Graphics.canCopy = Graphics.canMove = false;
		windowFlagsToSet |= FLAG_MAIN_WINDOW_ROTATED;
	}else if (counterRotated) {
		Graphics.canCopy = Graphics.canMove = false;
		windowFlagsToSet |= FLAG_MAIN_WINDOW_COUNTER_ROTATED;
	}//else
	if (!supportsMultiple()){
		windowFlagsToSet |= FLAG_IS_VISIBLE;
		windowFlagsToClear &= ~FLAG_IS_VISIBLE;
	}
	//createNativeWindow(windowRect,windowTitle,windowFlagsToSet,windowFlagsToClear,null);
	create(windowRect,windowTitle,windowFlagsToSet,windowFlagsToClear,null);
	didSetup = true;
	canDisplay = true;
	if (taskbarIcon != null)
		taskbarWindow = new TaskbarWindow(windowTitle+"- Taskbar Window",taskbarIcon,windowTitle);
	//centerMainWindow(-1,-1,windowInitiallyVisible);
	//if (windowFlags == 0)
//		windowFlags =
}
//-------------------------------------------------------------------
protected void sizeToFit(Control what)
//-------------------------------------------------------------------
{
	what.make(false);
	ewe.fx.Dimension d = what.getPreferredSize(null);
	windowRect = new ewe.fx.Rect(-1,-1,d.width+4,d.height+20);
}
//===================================================================
public void onStart() throws Exception
//===================================================================
{
	if (runClass == null) startup();
	else{
		Reflect r = new Reflect(runClass);
		mainMethod = r.getMethod("main","([Ljava/lang/String;)V",Method.DECLARED);
		if (mainMethod != null)
			if (!Modifier.isStatic(mainMethod.getModifiers()))
				mainMethod = null;
		if (mainMethod != null) startup();
		else{
			//try{
				runObject = runClass.newInstance();

			//}catch(Exception e){
				//e.printStackTrace();
				//runObject = null;
			//}
			if (runObject == null){
				ewe.sys.Vm.debug(runClass+" has no static void main(String []) method and cannot be instantiated.");
				ewe.sys.Vm.sleep(5000);
				exit(0);
			}
			Type ty = new Type("ewe.data.LiveObject");
			if (ty.isInstance(runObject)){
				Form ed = (Form)ty.invoke(runObject,"runAsApp()Lewe/ui/Form;",new Object[0]);
				if (ed == null) startup();
				else ed.show();
			/*
			if (runObject instanceof ewe.data.LiveData){
				Editor ed = ((ewe.data.LiveData)runObject).getEditor(0);
				if (ed != null) {
					ed.enableScrolling(true);
					ed.exitSystemOnClose = true;
					ed.show();
				}else startup();
			*/
			}else if (runObject instanceof Form){
				((Form)runObject).exitSystemOnClose = true;
				((Form)runObject).show();
			}else
				startup();
		}
	}
}
/**
This is called at the end of the fullSetup() method, so by this time the Main Window
is created and displayed if it was set to be visible.
**/
//-------------------------------------------------------------------
protected void setup(){}
//-------------------------------------------------------------------
/**
* This is called if this mApp was selected as the class to run for the application.
* By default this creates an mThread that then calls this mApp's run() method - you should
* override that method instead.
**/
//-------------------------------------------------------------------
protected void startup()
//-------------------------------------------------------------------
{
	new mThread(this).start();
}
private Form startMessage;
//==================================================================
public final void showStartMessage()
//==================================================================
{
	//startMessage = new mWaba.gui.control.MessageBox("mWaba Demo","mWaba AWT & Toolkit\nUnlicensed Demo Beta-1.0\nNovember 1999\nmichael_brereton@hotmail.com",0);
	//startMessage.exec(null,this);
}
//==================================================================
public void setRect(int x,int y,int w,int h)
//==================================================================

{
	super.setRect(x,y,w,h);
	if (appFrame != null) appFrame.setRect(this.x,this.y,this.width,this.height);
}
/**
This is called after setupMainWindow() is called - so by this time the Main Window has
already been created and is now visible if it was not set to be invisible.
**/
//==================================================================
public void show(Frame f,boolean fullSize)
//==================================================================
{
	appFrame = f;
	f.backGround = Color.LightGray;
	appFrame.name = "Main App Frame";
	if (fullSize) f.setPreferredSize(width,height);
	Gui.dontPaintNextFrame = true;
	Gui.showFrame(f,this,Gui.FILL_FRAME);
	Gui.dontPaintNextFrame = false;
	if ((Vm.getParameter(Vm.VM_FLAGS) & Vm.VM_FLAG_HAS_SOFT_KEYS) != 0)
		SoftKeyBar.setupScreen();
}
//==================================================================
//Note this will fail after 12 days.
//===========================================================
/**
* This results in a single call to the ticked() method of the TimerProc, unless
* cancelTimer() is called first.
*/
public final static int requestTick(TimerProc who,int milli) {return requestTimer(who,milli,false);}
/**
* This results in regular calls to the ticked() method of the TimerProc until
* cancelTimer() is called.
*/
public final static int requestTimer(TimerProc who,int milli) {return requestTimer(who,milli,true);}
private final static int requestTimer(TimerProc who,int milli,boolean repeat)
//===========================================================
{
	return Vm.requestTick(who,milli,repeat);
	/*
	if (who == null) return 0;
	int id = Vm.getNewId();
	while (findTimer(id) != null) id = Vm.getNewId();
	TimerEntry te;
	timers.add(te = new TimerEntry(who,milli,repeat,id));
	return id;
	*/
}
/*
private static void showTimers()
{
	String out = "[";
	for (int i = 0; i<timers.size(); i++) out += timers.get(i)+" ";
	out += "]";
	System.out.println(out);
}
*/
/*
//==================================================================
private final static TimerEntry findTimer(int id)
//==================================================================
{
	for (int i = 0; i<timers.size(); i++){
		TimerEntry te = (TimerEntry)timers.get(i);
		if (te.id == id) return te;
	}
	return null;
}
*/
//===========================================================
public final static void cancelTimer(int timerId)
//===========================================================
{
	Vm.cancelTimer(timerId);
	/*
	TimerEntry te = findTimer(timerId);
	if (te != null) timers.remove(te);
	*/
}
//BETA7
/*
Timer myTimer = null;
//==================================================================
public void setTimerInterval(int millis)
//==================================================================
{
	//BETA6
	//super.setTimerInterval(millis);
	//BETA7
	System.out.println("Setting timer");
	if (myTimer != null) removeTimer(myTimer);
	myTimer = addTimer(1000);//millis);
}
*/

//-------------------------------------------------------------------
protected boolean canClose()
//-------------------------------------------------------------------
{
	//new MessageBox("Closing","Now closing!",0).execute();
	return true;
}

//===================================================================
void closeApp(Object par)
//===================================================================
{
	boolean cl = false;
	if (closeLock.grab()) try{
		cl = canClose();
	}finally{
		closeLock.release();
	}
	if (cl) exit(0);
}
//==================================================================
public void onEvent(Event ev)
//==================================================================
{
	//System.out.println(this+" "+ev);
	if (ev instanceof FormEvent && ev.type == FormEvent.CLOSED && ev.target == startMessage) {
		lockState = false;
		startup();
	}else if (ev.type == WindowEvent.CLOSE){
		new mThread(){
			public void run(){
				closeApp(null);
			}
		}.start();
	}else
	//BETA7
	/*
	if (ev.type == ControlEvent.TIMER) onTimerTick();
	else
	*/
		super.onEvent(ev);

}
/**
 * This is called when a WindowEvent occurs on a mobile device.
	This can be used to trap, say a WindowEvent.CLOSE message from the mobile OS and allow
	for the application to save its state. This is needed because WinCE will automatically close
	applications if memory starts to run low. It will also forcibly terminate the application if
	it does not exit immediately. By default this will call the closeMobileApp() method if the event
	is a WindowEvent.CLOSE event.
 */
//-------------------------------------------------------------------
//protected void mobileWindowEvent(int type,int key,int x,int y,int modifiers,int timeStamp)
//-------------------------------------------------------------------
//{
//}
/**
* This will be called by mobileWindowEvent() if a Close message is sent by the OS and the
* application is running on a mobile device. This is because mobile OS's such as WinCE will
* send Close messages to applications when there is a shortage of free memory to run new
* applications. By default this simply closes the application immediately.
You should save the state of the application if necessary and then call exit()
* to quit the application.
* @param targetWindow The window that received the CLOSE event.
* @param flags This will have the Window.FLAG_CLOSE_BY_USER bit set if the system <b>knows</b> for
* certain that the user pressed a system button to generate the Window.CLOSE event. This is only true
* for the 'OK' button under Windows CE. It is not possible to tell the difference between the user pressing
* an 'X' button and the system generating a CLOSE message.
* @return
*/
//===================================================================
public void closeMobileApp(Window targetWindow,int flags)
//===================================================================
{
	if (targetWindow == this){
		exit(0);
	}
	else if (targetWindow != null)
		if ((flags & WindowEvent.FLAG_CLOSE_BY_USER) == 0)
			if ((targetWindow.getWindowFlags() & FLAG_HAS_CLOSE_BUTTON) == 0){
				exit(0);
			}
}
static ewe.util.Hashtable fonts = new ewe.util.Hashtable();
static {
	setupFonts();
}


/**
 * Call this method to alert the application that fonts have been changed other than through setFont().

 */
//===================================================================
public static void fontsChanged()
//===================================================================
{
	guiFont = findFont("gui");
}
/**
 * Add a Font to the application font library. This is basically a Hashtable
	of Fonts which the ewe library and your application can use. The ewe UI library
	uses the Font which is named as "gui" as the default font for controls.
 * @param font The Font to add.
 * @param name The name of the Font. Important font names include "gui", "system", "fixed", "text", "small" and "big"
 */
//===================================================================
public static void addFont(Font font,String name)
//===================================================================
{
	name = name.toLowerCase();
	if (name.equals("gui")) guiFont = font;
	fonts.put(name,font);
}
/**
* Find a font in the application Font library. This is basically a Hashtable
	of Fonts which the ewe library and your application can use. The ewe UI library
	uses the Font which is named as "gui" as the default font for controls.
* @param name The name of the Font to look for.  Important font names include "gui", "system", "fixed", "text", "small" and "big"
* @return The Font found. If no Font is found for that name, the "system" font is returned.
*/
//===================================================================
public static Font findFont(String name)
//===================================================================
{
	return findFont(name,true);
}
/**
* Find a font in the application Font library. This is basically a Hashtable
	of Fonts which the ewe library and your application can use. The ewe UI library
	uses the Font which is named as "gui" as the default font for controls.
* @param name The name of the Font to look for.  Important font names include "gui", "system", "fixed", "text", "small" and "big"
* @param doDefault If this is true and no font is found for the name, the "system" font is returned.
If it is false and no font is found for the name, null will be returned.
* @return The Font found.
*/
//===================================================================
public static Font findFont(String name,boolean doDefault)
//===================================================================
{
	Font f = (Font)fonts.get(name.toLowerCase());
	if (f != null) return f;
	f = (Font)fonts.get("system");
	if (f == null) f = new Font("Helvetica",Font.PLAIN,12);
	return f;
}

/**
 * Get an Iterator for all the entries in the fonts hashtable.
 * Each item returned by the Iterator will be ewe.util.Map.MapEntry object that
	can be used to get the font name (the "key" for the entry) and the font itself (the "value" for the entry).
	This can be used to change all font entries as necessary. If you do change the fonts using the entries
	then you should call the fontsChanged() method.
 * @return an Iterator for all the entries in the fonts hashtable.
 */
//===================================================================
public static ewe.util.Iterator getFonts()
//===================================================================
{
	return fonts.entries();
}
/**
 * This is used by the mApp to setup its font list. You should not need to call this, it is done automatically.
	This sets up the "standard" mApp fonts - include "system", "gui", "text", "big" and "small".
 */
//===================================================================
public static void setupFonts()
//===================================================================
{
	//Type t = new Type("ewe.sys.VMOptions");
	//vmOptions = t.newInstance();
	vmOptions = new VMOptions();
	vmOptions.readAndApply();
	/*
	if (vmOptions == null){
		Font sys = new Font("helvetica",Font.PLAIN,ewe.sys.Vm.isMobile() ? 10 : 12);
		addFont(sys,"system");
		addFont(sys,"text");
		addFont(sys.changeStyle(sys.PLAIN),"gui");
		addFont(sys.changeNameAndSize("monospaced",sys.getSize()+2),"fixed");
		addFont(sys.changeNameAndSize(null,sys.getSize()-2),"small");
		addFont(sys.changeNameAndSize(null,sys.getSize()+2),"big");
	}else{
		t.invoke(vmOptions,"readAndApply()V",new Object[0]);
	}
	*/
}

protected boolean monoMode = false;

/**
 * @deprecated
 */
//==================================================================
public void changeMonoMode()
//==================================================================
{
	monoMode = !monoMode;
	Color.setMonochrome(monoMode);
	repaintNow();
}
protected Graphics myGraphics;
//==================================================================
public Graphics getGraphics(boolean newOne)
//==================================================================
{
	return new Graphics(this);//Graphics.createNew(this);
	/*
	Graphics g = myGraphics;
	if (newOne || g == null) g = Graphics.createNew(this);
	g.reset();
	if (!newOne) myGraphics = g;
	if (g != null) g.setDrawOp(g.DRAW_OVER);
	return g;
	*/
}
/**
* This implements the action() method in FieldListener. By default it
* does nothing.
**/
//===================================================================
public void action(ewe.reflect.FieldTransfer ft,Editor ed) {}
//===================================================================
/**
* This implements the fieldChanged() method in FieldListener. By default it
* does nothing.
**/
//===================================================================
public void fieldChanged(ewe.reflect.FieldTransfer ft,Editor ed) {}
//===================================================================
/**
* This implements the fieldEvent() method in FieldListener. By default it
* does nothing.
**/
//===================================================================
public void fieldEvent(ewe.reflect.FieldTransfer ft,Editor ed,Object event) {}
//===================================================================
//===================================================================

/**
* This calls <b>runApplet(String args[],String extraArgs [])</b> with null extraArgs.
**/
//===================================================================
public static void runApplet(String args[]) {runApplet(args,null);}
//===================================================================
/**
* This is used if the mApp is executed directly by a pure Java interpreter. It must
* be called inside of the <b>static void main(String args[])</b> method in your mApp.
* You must define this if you are creating a build to be used with Microsoft's Jexegen
* which cannot save a command line inside of the generated executable.<br>
* Your <b>main</b> method should pass its arguments to this method along with any
* extra arguments as needed. Thse two argument lists are then combined to form a final
* argument list which is then passed to the main() method of ewe.applet.Applet.<br>
* For example, here is the main() method of the Solitaire application.
*<br><pre>
* public static void main(String args[])
* {
* 	runApplet(new String[]{"solitaire/Solitaire"},args);
* }
*</pre>
**/
//===================================================================
public static void runApplet(String args[],String extraArgs [])
//===================================================================
{
	if (args == null) return;
	if (extraArgs != null){
		String [] args2 = new String[args.length+extraArgs.length];
		ewe.sys.Vm.copyArray(args,0,args2,0,args.length);
		ewe.sys.Vm.copyArray(extraArgs,0,args2,args.length,extraArgs.length);
		args = args2;
	}
	Reflect r = ewe.reflect.Reflect.getForName("ewe/applet/Applet");
	if (r == null) return;
	Object obj = r.newInstance();
	if (obj == null) return;
	Method m = r.getMethod("main","([Ljava/lang/String;)V",0);
	if (m == null) return;
	Wrapper [] appArgs = new Wrapper[1];
	appArgs[0] = new Wrapper().setObject(args);
	m.invoke(obj,appArgs,null);
}

//##################################################################
}
//##################################################################


