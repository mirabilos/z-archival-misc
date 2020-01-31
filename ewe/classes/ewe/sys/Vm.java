/*
Note - This is the Linux version of Vm.java
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
 *  if not, please downlgetoad it at: www.ewesoft.com/LGPL.txt                      *
 *                                                                               *
 *********************************************************************************/
package ewe.sys;
import ewe.fx.*;
import ewe.io.*;
/**
 * Vm contains various system level methods.
 * <p>
 * This class contains methods to copy arrays, obtain a timestamp,
 * sleep and get platform and version information.
 */

// NOTE:
// In the future, these methods may include getting unique object id's,
// getting object classes, sleep (for single threaded apps),
// getting amount of memory used/free, etc.
// The reason these methods should appear in this class and not somewhere
// like the Object class is because each method added to the Object class
// adds one more method to every object in the system.

public class Vm implements VmConstants
{
/**
* Use with getStandardStream().
**/
public static final int STANDARD_INPUT = 0;
/**
* Use with getStandardStream().
**/
public static final int STANDARD_OUTPUT = 1;
/**
* Use with getStandardStream().
**/
public static final int STANDARD_ERROR = 2;

private static PrintWriter _out;
private static PrintWriter _err;
private static BufferedReader _in;
//===================================================================
public static PrintWriter out()
//===================================================================
{
	if (_out == null){
		Stream o = getStandardStream(STANDARD_OUTPUT);
		if (o == null) _out = new PrintWriter(ewe.ui.Console.getAppConsole().getWriter());
		//new PrintWriter(new VmDebugStream(),true);
		else _out = new PrintWriter(o,true);
	}
	return _out;
}
//===================================================================
public static BufferedReader in()
//===================================================================
{
	if (_in == null){
		Stream i = getStandardStream(STANDARD_INPUT);
		if (i == null) _in = new BufferedReader(ewe.ui.Console.getAppConsole().getReader());
		else _in = new BufferedReader(new InputStreamReader(i));
	}
	return _in;
}
//===================================================================
public static PrintWriter err()
//===================================================================
{
	if (_err == null){
		Stream e = getStandardStream(STANDARD_ERROR);
		if (e == null) _err = new PrintWriter(ewe.ui.Console.getAppConsole().getWriter());
			//new PrintWriter(new VmDebugStream(),true);
		else _err = new PrintWriter(e,true);
	}
	return _err;
}
//-------------------------------------------------------------------
private static boolean usingConsole = false;
//-------------------------------------------------------------------
/**
 * This tells the VM to use the application Console for out(), in() and err() instead of
 * standard output, input and error (if they exist).
 */
//===================================================================
public static void useConsoleForIO()
//===================================================================
{
	if (usingConsole) return;
	_out = new PrintWriter(ewe.ui.Console.getAppConsole().getWriter());
	_in = new BufferedReader(ewe.ui.Console.getAppConsole().getReader());
	_err = new PrintWriter(ewe.ui.Console.getAppConsole().getWriter());
	usingConsole = true;
}

//-------------------------------------------------------------------
private static ewe.data.PropertyList properties = new ewe.data.PropertyList();
//-------------------------------------------------------------------

private Vm()
	{
	}

/**
 * Copies the elements of one array to another array. This method returns
 * true if the copy is successful. It will return false if the array types
 * are not compatible or if either array is null. If the length parameter
 * would cause the copy to read or write past the end of one of the arrays,
 * an index out of range error will occur. If false is returned then no
 * copying has been performed.
 * @param srcArray the array to copy elements from
 * @param srcStart the starting position in the source array
 * @param dstArray the array to copy elements to
 * @param dstStart the starting position in the destination array
 * @param length the number of elements to copy
 */
public static native boolean copyArray(Object srcArray, int srcStart,
	Object dstArray, int dstStart, int length);
/**
* This is exactly the same as copyArray() except that it is a void
* method and that it mirrors the standard System.arraycopy.
**/
public static void arraycopy(Object srcArray, int srcStart,
	Object dstArray, int dstStart, int length)
{
	copyArray(srcArray,srcStart,dstArray,dstStart,length);
}



/**
 * Returns true if the system supports a color display and false otherwise.
 */
public static native boolean isColor();


/**
 * Returns a time stamp in milliseconds. The time stamp is the time
 * in milliseconds since some arbitrary starting time fixed when
 * the VM starts executing. The maximum time stamp value is (1 << 30) and
 * when it is reached, the timer will reset to 0 and will continue counting
 * from there.
 */
public static int getTimeStamp()
{
	return (int)getTimeStampLong() & 0x3fffffff;
}
/**
 * Returns a time stamp in milliseconds. The time stamp is the time
 * in milliseconds since some arbitrary starting time fixed when
 * the VM starts executing. The maximum time stamp value is (1 << 63) and
 * when it is reached, the timer will reset to 0 and will continue counting
 * from there.<p>
 * Because of the arbitray nature of the initial value you should only use getTimeStampLong()
 * for calculating differences between calls to getTimeStampLong()
 */
public static native long getTimeStampLong();



/** Returns the platform the Virtual Machine is running under as a string. */
public static native String getPlatform();


/**
 * Returns the username of the user running the Virutal Machine. Because of
 * Java's security model, this method will return null when called in a Java
 * applet. This method will also return null under most WinCE devices (that
 * will be fixed in a future release).
 */
public static native String getUserName();


/**
 * Returns the version of the Waba Virtual Machine. The major version is
 * base 100. For example, version 1.0 has value 100. Version 2.0 has a
 * version value of 200. A beta 0.8 VM will have version 80.
 */
public static int getVersion()
	{
	return 149;
	}
/**
 * Executes a command.
 * <p>
 * As an example, the following call could be used to run the command
 * "scandir /p mydir" under Java, Win32 or WinCE:
 * <pre>
 * int result = Vm.exec("scandir", "/p mydir", 0, true);
 * </pre>
 * This example executes the Scribble program under PalmOS:
 * <pre>
 * Vm.exec("Scribble", null, 0, false);
 * </pre>
 * The args parameter passed to this method is the arguments string
 * to pass to the program being executed.
 * <p>
 * The launchCode parameter is only used under PalmOS. Under PalmOS, it is
 * the launch code value to use when the Vm calls SysUIAppSwitch().
 * If 0 is passed, the default launch code (CmdNormalLaunch) is used to
 * execute the program.
 * <p>
 * The wait parameter passed to this method determines whether to execute
 * the command asynchronously. If false, then the method will return without
 * waiting for the command to complete execution. If true, the method will
 * wait for the program to finish executing and the return value of the
 * method will be the value returned from the application under Java, Win32
 * and WinCE.
 * <p>
 *
 * @param command the command to execute
 * @param args command arguments
 * @param launchCode launch code for PalmOS applications
 * @param wait whether to wait for the command to complete execution before returning
 */
//===================================================================
public static native int exec(String command, String args, int launchCode, boolean wait);
//===================================================================
/**
 * Execute a program.
 * @param command The full path to the executable file. Don't put quotes within it.
 * @param args Program arguments.
 * @return True if the system reports that the program was executed. False otherwise.
 */
//===================================================================
public static boolean execute(String command, String args)
//===================================================================
{
	return exec(command,args,0,false) != -1;
}
//===================================================================
public static String [] splitCommand(String args,String prepend)
//===================================================================
{
	ewe.util.Vector v = new ewe.util.Vector();
	if (prepend != null) v.add(prepend);
	if (args != null){
		char [] chars = getStringChars(args);
		int st = 0;
		char quote = 0;
		for (int i = 0; i<chars.length; i++){
			char c = chars[i];
			if (c == '"' || c == '\''){
				if (quote == 0) quote = c;
				else if (quote == c) quote = 0;
				continue;
			}
			if ((c == ' ' || c == '\t') && (quote == 0)) {
				if (st == i) {
					st = i+1;
					continue;
				}else{
					v.add(args.substring(st,i));
					st = i+1;
				}
			}
		}
		if (st != chars.length) v.add(args.substring(st,chars.length));
	}
	String [] ret = new String[v.size()];
	//ewe.sys.Vm.debug(v.toString());
	v.copyInto(ret);
	return ret;
}

//===================================================================
public static ewe.sys.Process exec(String []command,String []env) throws ewe.io.IOException
//===================================================================
{
	nativeProcess p = new nativeProcess();
	p.exec(command,env);
	return p;
}
//===================================================================
public static ewe.sys.Process exec(String command,String []env) throws ewe.io.IOException
//===================================================================
{
	return exec(splitCommand(command,null),env);
}
//===================================================================
public static ewe.sys.Process exec(String []command) throws ewe.io.IOException
//===================================================================
{
	return exec(command,null);
}
//===================================================================
public static ewe.sys.Process exec(String command) throws ewe.io.IOException
//===================================================================
{
	return exec(command,null);
}
/**
 * Execute a ".ewe" file using the installed Ewe VM.
 * @param pathToEweFile
 * @param args Additional application arguments.
 * @return A Process object for monitoring the progress of the application.
 * @exception ewe.io.IOException If the Ewe VM could not be executed.
 */
//===================================================================
public static ewe.sys.Process execEwe(String pathToEweFile,String args) throws ewe.io.IOException
//===================================================================
{
		String eweLocation = getPathToEweVM();
		if (eweLocation == null) throw new ewe.io.IOException("Cannot locate the Ewe VM");
		if (pathToEweFile.charAt(0) != '"') pathToEweFile = '"'+pathToEweFile+'"';
		if (args != null) pathToEweFile += " "+args;
		return exec(splitCommand(pathToEweFile,eweLocation));
}
/**
 * Execute the Ewe VM with the supplied parameters.
 * @param arguments An argument list for the VM. The first command must be the command to execute.
 * @param env A new set of environment variables to use for the execution. Each must be in the form "name=value".
 *              this parameter can be null.
 * @return A Process object for monitoring the progress of the application.
 * @exception ewe.io.IOException If the Ewe VM could not be executed.
 */
//===================================================================
public static ewe.sys.Process execEwe(String [] arguments, String [] env) throws ewe.io.IOException
//===================================================================
{
		String eweLocation = getPathToEweVM();
		if (eweLocation == null) throw new ewe.io.IOException("Cannot locate the Ewe VM");
		String [] args = new String[arguments == null ? 1 : arguments.length+1];
		args[0] = getPathToEweVM();
		if (arguments != null)
			copyArray(arguments,0,args,1,arguments.length);
		return exec(args,env);
}

/**
 * Return the path to the registered Ewe VM executable file.
* @return The path to the Ewe VM executable file, or null if it is not found.
*/
//===================================================================
public static String getPathToEweVM()
//===================================================================
{
/*
	String got = getProperty("PATH_TO_EWE_DIR",null);
	if (got == null) return null;
	return got+"/ewe";
*/
	return ewe.ui.mApp.vmOptions.pathToEwe+"/ewe";
/*
	try{
		if (!ewex.registry.Registry.isInitialized(false)) return null;
		ewex.registry.RegistryKey eweDll = ewex.registry.Registry.getLocalKey(ewex.registry.Registry.HKEY_CLASSES_ROOT,"EweFile10\\DLL",true,false);
		if (eweDll == null) return null;

		String eweLocation = (String)eweDll.getValue(null);
		if (eweLocation == null) return null;
		return eweLocation.substring(0,eweLocation.length()-3)+"exe";
	}catch(Throwable t){
		return null;
	}
*/
}
/**
 * Execute a Ewe file using a new Ewe VM.
 * @param pathToEweFile The full path to the ".ewe" file.
 * @param args Optional additional arguments for the application.
 * @return true if the Ewe VM was executed.
 */
//===================================================================
public static boolean executeEwe(String pathToEweFile,String args)
//===================================================================
{
	try{
		String eweLocation = getPathToEweVM();
		if (pathToEweFile.charAt(0) != '"') pathToEweFile = '"'+pathToEweFile+'"';
		if (args != null) pathToEweFile += " "+args;
		return execute(eweLocation,pathToEweFile);
	}catch(Throwable t){
		return false;
	}
}

/**
 * This is used to run a new Ewe VM with the given argument list.
 * @param args The full argument list for the VM.
 * @return true if the Ewe VM was reported as being executed.
 */
//===================================================================
public static boolean runEweVM(String args)
//===================================================================
{
		String eweLocation = getPathToEweVM();
		return execute(eweLocation,args);
}
/**
 * Sets the device's "auto-off" time. This is the time in seconds where, if no
 * user interaction occurs with the device, it turns off. To keep the device always
 * on, pass 0. This method only works under PalmOS. The integer returned is
 * the previous auto-off time in seconds.
 */
public static native int setDeviceAutoOff(int seconds);


/**
 * Causes the VM to pause execution for the given number of milliseconds.
 * @param millis time to sleep in milliseconds
 */
public static native void sleep(int millis);

/**
* See documentation of SetSIP()
**/
public final static int SIP_ON = 0x1;
/**
* See documentation of SetSIP()
**/

public final static int SIP_LEAVE_BUTTON = 0x2;
/**
* See documentation of SetSIP()
**/
public final static int SIP_LOCK = 0x4;
/**
* This turns the SIP on PalmPC/PocketPC devices.
* @param mode a combination of the following
* bit flags.<br>
* SIP_ON = If this bit is set the SIP will be switched on and the SIP button will be made visible.
* If this bit is clear the SIP will be switched off (except in cases where SIP_LOCK is used).<br>
* SIP_LEAVE_BUTTON = If this bit is set when the SIP_ON bit is clear, the SIP will be switched off,
* but the SIP button will be left on. This only works for PocketPC.<br>
* SIP_LOCK = If this bit is set when SIP_ON is also set, the SIP will be switched on and will not
* be switched off unless this method is called with SIP_ON clear AND with SIP_LOCK on. This is
* used to keep the SIP visible even under circumstances where it is normally hidden (e.g. when
* switching from a text control to a non-text control). This can be used to avoid excessive
* SIP showing and hiding.
**/
public static native void setSIP(int mode,ewe.ui.Window forWindow);
/**
* This turns the SIP on PalmPC/PocketPC devices.
* @param mode a combination of the following
* bit flags.<br>
* SIP_ON = If this bit is set the SIP will be switched on and the SIP button will be made visible.
* If this bit is clear the SIP will be switched off (except in cases where SIP_LOCK is used).<br>
* SIP_LEAVE_BUTTON = If this bit is set when the SIP_ON bit is clear, the SIP will be switched off,
* but the SIP button will be left on. This only works for PocketPC.<br>
* SIP_LOCK = If this bit is set when SIP_ON is also set, the SIP will be switched on and will not
* be switched off unless this method is called with SIP_ON clear AND with SIP_LOCK on. This is
* used to keep the SIP visible even under circumstances where it is normally hidden (e.g. when
* switching from a text control to a non-text control). This can be used to avoid excessive
* SIP showing and hiding.
**/
public static void setSIP(int mode)
{
	setSIP(mode,ewe.ui.Window.getActiveWindow());
}
public final static int SIP_FREEZE = 0x8;
public final static int SIP_UNFREEZE = 0x10;
public final static int SIP_OVERRIDE_USE_SIP = 0x20;
public final static int SIP_CURRENT = -1;
/**
 * This temporarily holds the SIP in a particular mode (open or closed) until the current event thread
 * has completed.
 * @param freezeOrUnfreeze true to freeze the SIP either on (if sipOnOrOff is SIP_ON) or off (if sipOnOrOff is 0).
	Set to false to unfreeze the SIP. This is done automatically at the end of the current event thread.
 * @param sipOnOrOff This is only valid if freezeOrUnfreeze is true, in which case it tells if the SIP should be open
	(SIP_ON) or closed (0).
 * @param window The window of the control that is changing the SIP.
 */
//===================================================================
public static void freezeSIP(boolean freezeOrUnfreeze,int sipOnOrOff,ewe.ui.Window window)
//===================================================================
{
	if (freezeOrUnfreeze && !ewe.ui.Window.inEventThread) return;
	if (window == null) window = ewe.ui.Window.getActiveWindow();
	if (!freezeOrUnfreeze) sipOnOrOff = 0;
	else if (sipOnOrOff == SIP_CURRENT) sipOnOrOff = getSIP();
	setSIP((sipOnOrOff & SIP_ON) | (freezeOrUnfreeze ? SIP_FREEZE : SIP_UNFREEZE));
}
/**
* This will freeze the SIP in it's current state until the end of the processing of the current Event.
**/
//===================================================================
public static void freezeSIP(ewe.ui.Control forWho)
//===================================================================
{
	freezeSIP(true,SIP_CURRENT,forWho == null ? null : forWho.getWindow());
}

public static final int SIP_IS_ON = 0x1;
/**
 * Get the state of the SIP.
 * @returns SIP_IS_ON or 0. Use SIP_IS_ON with the & operator to determine if the SIP is shown.
 */
//===================================================================
public static native int getSIP();
//===================================================================
/**
 * Play a .wav file resource.
 * @param sound The name of the .wav sound to play.
 * @param options One of the SND_ variables.
 * @deprecated use SoundClip instead.
 * @return
 */
public static native int playSound(String sound,int options);


/**
 * This is used to get the pressed state of a particular key at the time the method is called.
 * @param keyCode One of the ewe.ui.IKeys constants.
 * @return If the key is pressed the value will have bit 0x8000 set and if the key has been pressed



 * since the last call the 0x0001 bit will be set.
 * @see ewe.ui.IKeys
 */

public static native int getAsyncKeyState(int keyCode);

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
* @deprecated use ewe.ui.Window.captureAppKeys() instead.
* @return The same value as options.
*/
public static int captureAppKeys(int options)
{
	return ewe.ui.Window.captureAppKeys(options);
}
/**
* This causes a "native" message box to be displayed - halting ewe programs until
* the user dismisses it. Useful for a variety of purposes including ensuring that

* a particular question or message is _definitely_ answered by the user - since it
* is not possible for a ewe application to programatically dismiss a native message box.
* This can therefore be used for security purposes in future versions.
* Use one of the vm.MB_ options for the type. It will return one of the vm.ID values
**/
public static native int messageBox(String caption,String text,int type);
/**
* This gets paramter values. It only works currently for parameterID as VM_FLAGS.
**/
public static native int getParameter(int parameterID);
/**

 * Returns true if this VM is running on what is considered a Mobile platform. This is a convenienc
 * method that uses the getParameter() method.
 */
public static boolean isMobile()
{
	return ((getParameter(VM_FLAGS) & VM_FLAG_IS_MOBILE) != 0);
}
/**
* This sets VM parameters.
* @param ParameterId Currently only the value VM_FLAGS and VM_USE_SIP is supported.
* @param value The new value to set.
* @return the value that was set.
*/
public static native int setParameter(int ParameterId,int value);


//-------------------------------------------------------------------
private static int whichCursor(long value)
//-------------------------------------------------------------------
{
	for (int i = 0; i<cursors.size(); i++){
		ewe.sys.Long l = (ewe.sys.Long)cursors.get(i);
		if (l != null)
			if (l.value == value) return i+1;
	}
	return 0;
}

//===================================================================
public static void showWait(ewe.ui.Control c,boolean show)
//===================================================================
{
 setCursor(c,show ? WAIT_CURSOR : -2);
}
//===================================================================
public static void showWait(boolean show)
//===================================================================
{
	if (show && (getParameter(VM_FLAGS) & VM_FLAG_NO_MOUSE_POINTER) != 0)
		ewe.ui.Window.applicationToFront();
	setCursor(show ? WAIT_CURSOR : -2);
}
/**
* Set the cursor.
**/
//===================================================================
public static int setCursor(int type)
//===================================================================
{
	return setCursor(null,type,false);
}
//===================================================================
public static int setCursor(ewe.ui.Control c,int type)
//===================================================================
{
	return setCursor(c,type,false);
}

//private static boolean doingSet = false;
private static int waiting = 0;
//===================================================================
static int setCursor(ewe.ui.Control c,int type,boolean inCallback)
//===================================================================
{
	if (cursors == null) makeCursors();
	boolean refresh = false;
	if (inCallback || Coroutine.getCurrent() == null){
		if (type == -2) {
			if (waiting > 0) {
				waiting--;
				if (waiting == 0) refresh = true;
			}
			if (waiting != 0) return 0;
		}else if (type == WAIT_CURSOR){
			waiting++;
		}else if (type == -1){
			return -1;
		}else if (waiting != 0)
			return -1;
		long cursor = 0;
		Object cursorObj = null;
		if (type > 0 && type <= cursors.size()){
			ewe.sys.Long l = (ewe.sys.Long)cursors.get(type-1);
			if (l != null) {
				cursor = l.value;
				cursorObj = l;
			}
		}
		ewe.ui.Window w = c == null ? null : c.getWindow();
		cursor = setCursorHandle(w,(int)cursor);
		if (w != null) w.currentCursor = cursorObj;
		int was = whichCursor(cursor);
		if (type == 0 && refresh) ewe.ui.PenEvent.notWaiting();
		return 0;
	}else{
		Object cursor = null;
		if (type >= 0 && type <= cursors.size()){
			if (type == 0) cursor = null;
			else cursor = cursors.get(type-1);
			ewe.ui.Window win = c == null ? null : c.getWindow();
			//ewe.sys.Vm.debug(cursor+", "+win);
			if (win != null)
				if (cursor == null && win.currentCursor == null) return 0;
				else if (cursor != null)
					if (cursor.equals(win.currentCursor))
						return 0;
		}
		//if (doingSet) return -1;
		//doingSet = true;
		CursorCallBack ccb = new CursorCallBack();
		ewe.util.Tag tg = new ewe.util.Tag();
		tg.tag = type; tg.value = c;
		ccb.execute(tg);
		//doingSet = false;
		return 0;
	}
}

/**
* Use this to create a new Mouse cursor. The parameter you pass to it must
* have been created by <b>ewe.fx.mImage.toCursor(Point hotSpot)</b><br>
* If it is successful an int value is returned which you can the use in
* <b>ewe.ui.Control.setCursor(int cursor)</b>, otherwise it will return 0
* which represents the default cursor.
**/
//===================================================================
public static int createCursor(Object cursor)
//===================================================================
{
	if (cursor == null) return 0;
	if (cursors == null) makeCursors();
	cursors.add(cursor);
	return cursors.size();
}

private static ewe.util.Vector cursors;

private static void makeCursors()
{
	cursors = new ewe.util.Vector();
	cursors.add(new ewe.sys.Long().set(getCursorHandle(WAIT_CURSOR)));
	if ((getParameter(VM_FLAGS) & VM_FLAG_NO_MOUSE_POINTER) == 0)
	for (int i = WAIT_CURSOR+1; i<=INVISIBLE_CURSOR; i++){
		Object toAdd = new ewe.sys.Long().set(getCursorHandle(i));
		if (((ewe.sys.Long)toAdd).value == 0)
		switch(i){
			case INVISIBLE_CURSOR:
				Image im = new Image(32,32);
				Graphics g = new Graphics(im);
				g.setColor(Color.White);
				g.fillRect(0,0,32,32);
				g.free();
				mImage mi = new mImage(); mi.setImage(im,new Image(im,0));
				toAdd = mi.toCursor(new Point(0,0));
				break;
			case HAND_CURSOR:
				toAdd = new mImage("ewe/HandCursor.bmp","ewe/HandCursorMask.bmp").toCursor(new Point(8,0));
				break;
			case BUSY_CURSOR:
				toAdd = new mImage("ewe/BusyCursor.bmp","ewe/BusyCursorMask.bmp").toCursor(new Point(0,0));
				break;
			case DRAG_SINGLE_CURSOR:
				toAdd = new mImage("ewe/DragSingleCursor.bmp","ewe/DragSingleCursorMask.bmp").toCursor(new Point(0,0));
				break;
			case DRAG_MULTIPLE_CURSOR:
				toAdd = new mImage("ewe/DragMultipleCursor.bmp","ewe/DragMultipleCursorMask.bmp").toCursor(new Point(0,0));
				break;
			case COPY_SINGLE_CURSOR:
				toAdd = new mImage("ewe/CopySingleCursor.bmp","ewe/CopySingleCursorMask.bmp").toCursor(new Point(0,0));
				break;
			case COPY_MULTIPLE_CURSOR:
				toAdd = new mImage("ewe/CopyMultipleCursor.bmp","ewe/CopyMultipleCursorMask.bmp").toCursor(new Point(0,0));
				break;
			case DONT_DROP_CURSOR:
				toAdd = new mImage("ewe/DontDropCursor.bmp","ewe/DontDropCursorMask.bmp").toCursor(new Point(0,0));
				break;

		}
		cursors.add(toAdd == null ? new ewe.sys.Long() : toAdd);
	}
}
private static native int getCursorHandle(int which);
private static native int setCursorHandle(ewe.ui.Window window,int cursor);


//..................................................................
public static final int MB_OK                       = 0x00000000;
public static final int MB_OKCANCEL                 = 0x00000001;
public static final int MB_ABORTRETRYIGNORE         = 0x00000002;
public static final int MB_YESNOCANCEL              = 0x00000003;
public static final int MB_YESNO                    = 0x00000004;
public static final int MB_RETRYCANCEL              = 0x00000005;




public static final int MB_ICONHAND                 = 0x00000010;
public static final int MB_ICONQUESTION             = 0x00000020;
public static final int MB_ICONEXCLAMATION          = 0x00000030;
public static final int MB_ICONASTERISK             = 0x00000040;

public static final int MB_ICONWARNING              = MB_ICONEXCLAMATION;
public static final int MB_ICONERROR                = MB_ICONHAND;

public static final int MB_ICONINFORMATION          = MB_ICONASTERISK;
public static final int MB_ICONSTOP                 = MB_ICONHAND;

public static final int MB_DEFBUTTON1               = 0x00000000;
public static final int MB_DEFBUTTON2               = 0x00000100;
public static final int MB_DEFBUTTON3               = 0x00000200;

public static final int MB_APPLMODAL                = 0x00000000;
public static final int MB_SYSTEMMODAL              = 0x00001000;
public static final int MB_TASKMODAL                = 0x00002000;

public static final int  IDOK                =1;
public static final int  IDCANCEL            =2;
public static final int  IDABORT             =3;
public static final int  IDRETRY             =4;
public static final int  IDIGNORE            =5;
public static final int  IDYES               =6;
public static final int  IDNO                =7;

public static native int getNewId();
/**
* Request a timer tick to be sent to the TimerProc target. It returns a unique
* timer id which will be sent to the target on each tick. If repeat is false only one
* tick will be sent.
* @param target The TimerProc object to receive the ticks.
* @param interval The time in milliseconds.
* @param repeat true if the timer should receive ticks at regular intervals until cancelTimer is called.
* @return
*/
public static native int requestTick(TimerProc target,int interval,boolean repeat);
/**
* Request a timer tick to be sent to the TimerProc target. It returns a unique
* timer id which will be sent to the target on each tick.
**/
public static int requestTimer(TimerProc target,int interval)
{
	return requestTick(target,interval,true);
}
/**
* This cancels timer ticks for the specified timerID.
**/
public static native void cancelTimer(int timerId);

/**
* This cancels the current timer and then requests a new one with a new interval. The
* returned value is the id of the new timer.
**/
public static int changeTimer(int timerId,TimerProc target,int newInterval)
{
	cancelTimer(timerId);
	return requestTimer(target,newInterval);
}
/**
* Used with setParameter() it adjusts the minimum timer tick.
**/
public static final int TIMER_TICK = 1;
/**
* Used with setParameter() it switches on (1) or off (0) SIP simulation for
* Java and ewe on the PC.
**/
public static final int SIMULATE_SIP = 2;
/**
* Obsolete - Used with setParameter(), it sets the maximum number of combined timer/callback/coroutine
* entries the system can handle. Actually the VM now handles any number of these types of entries.
**/
public static final int MAX_TIMER_ENTRIES = 3;

/**
* Used with setParameter() and a value of 1, it tells the VM to
* act as if multiple windows are not supported.
**/
public static final int SET_NO_WINDOWS = 4;
public static final int SET_ALWAYS_SHOW_SIP_BUTTON = 5;
public static final int SET_USE_SIP = 10;

public static native int getMessage(SystemMessage dest,boolean peek,boolean remove);
/**
* This causes the callBack() method to be invoked on the CallBack object that will be in the OS's native
* message queue.
* @param who The CallBack object.
* @param data Optional data to send.
* @return not defined.
*/
public static native int callInSystemQueue(CallBack who,Object data);
//===================================================================
public static int callInSystemQueue(ewe.ui.Window w,CallBack who,Object data)
//===================================================================
{
	return callInSystemQueue(who,data);
}
/**
 * This checks if the current thread is being run within the System Queue.
 * @return true if the current thread is in the System Queue, false otherwise.
 */
public static native boolean amInSystemQueue();

/**
 * This reads a Win32 specific resource.
 * @deprecated
 * @param fileName
 * @param resourceName
 * @param options
 * @return
 */
public static native int readResource(String fileName,String resourceName,int options);

/**
 * This releases a resource read in by readResource.
 * @deprecated
 * @param resourceName
 * @return
 */
public static native int releaseResource(String resourceName);



/**
* This reads in a resource or a file with the specified path into the provided ByteArray.
* It returns: 0 = Could not load. 1 = Loaded from Resource. 2 = Loaded from File. < 0 = Error.
* <p>This method has been deprecated. Instead use openResource() or readResource().
* @deprecated
**/
//===================================================================
public static int loadResourceOrFile(String path,ewe.util.ByteArray dest)
//===================================================================
{
	byte [] got = readResource(null,path);
	if (got == null) return 0;
	dest.data = got;
	dest.length = got.length;
	return 1;
}
//-------------------------------------------------------------------
static native boolean getResource(String path,int [] specs);
//-------------------------------------------------------------------
static native boolean getResourceData(int memory,int memoryLocation,byte [] dest,int offset,int length);
//-------------------------------------------------------------------
/**
* Do a garbage collection.
**/
public static native void gc();
/**
* Load a Dynamic Link Library which may have the native code for a ewe object.
* @param name The name of the library - do NOT include ".dll"
* @return true if the library was loaded, false if not.
*/
/*
//===================================================================
public static boolean loadLibrary(String name)
//===================================================================
{
	//if (true)return false;
	String nn = name+".so";
	RandomAccessStream ras = openRandomAccessStream(nn,RandomAccessFile.READ_ONLY);
	if (ras instanceof RandomAccessFile) {
		ras.close();
		ras = null;
	}
	if (ras != null) try{
		File f = new File("");
		f = f.createTempFile(nn,null);
		if (f != null){
			Stream out = f.getOutputStream();
			if (out != null){
				new IOTransfer().run(ras,out);
				out.close();
				if (nativeLoadLibrary(f.getFullPath())) return true;
			}
		}
	}finally{
		ras.close();
	}
	if (nativeLoadLibrary(nn)) return true;
	return nativeLoadLibrary(name);
}
*/
/**
* Load a Dynamic Link Library which may have the native code for a ewe object.
* @param name The name of the library - do NOT include ".dll" or ".so"
* @return true if the library was loaded, false if not.
* @deprecated use loadDynamicLibrary() instead.
*/
//===================================================================
public static boolean loadLibrary(String name)
//===================================================================
{
	try{
		loadDynamicLibrary(name);
		return true;
	}catch(SecurityException e){
		return false;
	}catch(UnsatisfiedLinkError u){
		return false;
	}
}
/**
* Load a Dynamic Link Library which may have the native code for a ewe object.
* @param name The name of the library - do NOT include ".dll" or ".so"
* @return true if the library was loaded, false if not.
*/
//===================================================================
public static void loadDynamicLibrary(String name)
throws SecurityException, UnsatisfiedLinkError
//===================================================================
{
	try{
		load(name+".so");
		return;
	}catch(SecurityException e){
	}catch(UnsatisfiedLinkError u){
	}
	load(name);
}
//===================================================================
public static void load(String libraryName)
throws SecurityException, UnsatisfiedLinkError
//===================================================================
{
	String nn = libraryName;
	RandomAccessStream ras = openRandomAccessStream(nn,RandomAccessFile.READ_ONLY);
	if (ras instanceof RandomAccessFile) {
		ras.close();
		ras = null;
	}
	if (ras != null) try{
		File f = new File("");
		f = f.createTempFile(nn,null);
		if (f != null){
			Stream out = f.getOutputStream();
			if (out != null){
				new IOTransfer().run(ras,out);
				out.close();
				if (nativeLoadLibrary(f.getFullPath())) return;
			}
		}
	}catch(Exception e){
	}finally{
		ras.close();
	}
	if (nativeLoadLibrary(nn)) return;
	throw new UnsatisfiedLinkError("Library: "+libraryName+" not found or is invalid.");
}


private static ewe.io.File fileObject;
/**
 * Set the File object to be used to represent the local file system.
 * @param fileObject the File object to be used to represent the local file system.
 * @exception SecurityException If the security system does not allow this. Currently
 * this operation is not allowed to be done more than once.
 */
//===================================================================
public static void setFileObject(File fileObject) throws SecurityException
//===================================================================
{
	if (Vm.fileObject != null) throw new SecurityException("File Object already set.");
	Vm.fileObject = fileObject;
}
/**
 * Get a new ewe.io.File object which can be used to represent a file on the file system
 * being used by the VM.
 * @return A new ewe.io.File object.
 */
//===================================================================
public static ewe.io.File newFileObject()
//===================================================================
{
	if (fileObject != null) return fileObject.getNew("");
	return new ewe.io.File("");
}
//-------------------------------------------------------------------
protected static native boolean nativeLoadLibrary(String dllName);
//-------------------------------------------------------------------
/**
* Get a default locale object.
**/
public static Locale getLocale() {if (l == null) l = new Locale(); return l;}
/**
* Force a halt of the application due to a critical error.
**/
//===================================================================
public static native void applicationError(String error);
//===================================================================
/**
* Using the native VM this will expose the char array which represents the String. Use this
* with care! If you write into it using the native VM you will change the String which is SUPPOSED
* to be immutable. Under a JavaVM this will return a COPY of the string arrays.
**/
//===================================================================
public static native char [] getStringChars(String str);
//===================================================================
/**
* Create a new String that uses the specified character array <b>without</b>
* creating a new character array for the String.<p>Use this with care. After
* creating the String you may be able to change it since you may have direct
* access to the String's characters. However under some systems (e.g. Java) this method
* may allocate a new copy of the array so do not assume that the provided
* character array <b>will</b> be used directly.
**/
//===================================================================
public static native String createStringWithChars(char[] chars);
//===================================================================

/**
**/
//===================================================================

public static native String mutateString(String str,char [] newChars,int start,int length,boolean useThisArray);
//===================================================================

/**
* Print a message on the console. There are no options defined yet.

**/
//===================================================================
public static native void debug(String message,int options);
//===================================================================


//===================================================================
public static native void debugObject(Object data,int options);

//===================================================================

/**
* Print a message on the console with no option.
**/
//===================================================================
public static void debug(String message) {debug(message,0);}
//===================================================================
/**
* This returns a count of allocated objects. Set doGCFirst to true to
* do a garbage collection before counting. The absolute value returned
* by this will be of little use, but you can use it to ensure that your
* application is not constantly creating objects which never get gc'ed.
**/
//===================================================================
public static native int countObjects(boolean doGCFirst);
//===================================================================


/**
 * Return an array that contains all reachable objects. A gc() is done
	before retrieving the objects. The returned array will not contain
	the array itself. Under Java this returns an empty array.
 * @return an array of reachable objects.
 */
//===================================================================
public static native Object [] getReferencedObjects();
//===================================================================


/**
* This returns the amount of object memory used in bytes.
* @param doGCFirst Set this true if you want a gc() to be called first.
* @return The amount of used object memory. Under Java this will return
* -1.
*/
//===================================================================
public static native int getUsedMemory(boolean doGCFirst);
//===================================================================
/**
* This returns the amount of class memory used in bytes. Under Java this will return -1
**/
//===================================================================
public static native int getClassMemory();
//===================================================================

static Locale l;
/**
* A Vector where you can placed objects that you don't want gc'd.
**/
public static ewe.util.Vector dontGc = new ewe.util.Vector();
/**
* This will attempt to provide a RandomAccessStream which can be used to read from
* a resource that is either stored in a ewe file, or resides on a sever (for Applet versions)
* or is a File. If the resource is in a ewe file or is on a server, then you will only be
* able to read from the stream.
* @param path The path to the resource or file.
* @param mode One of the RandomAccessStream constants: READ_ONLY or READ_WRITE
* @return A RandomAccessStream if successful.
*/
//===================================================================

public static ewe.io.RandomAccessStream openRandomAccessStream(String path,int mode)
//===================================================================
{
	int [] got = new int[2];
	if (getResource(path,got)) return new resourceRandomAccessStream(got);

	ewe.io.File f = new ewe.io.File(path);
	if (f.canRead()) return new ewe.io.RandomAccessFile(f,mode);
	String paths = getProperty("java.class.path",null);
	if (paths != null){
		String [] all = ewe.util.mString.split(paths,':');
		for (int i = 0; i<all.length; i++){
			ewe.io.File dir = new ewe.io.File(all[i]);
			if (!dir.isDirectory()) continue;
			f = new ewe.io.File(dir,path);
			if (f.canRead()) return new ewe.io.RandomAccessFile(f,mode);
		}
	}
	String pd = f.getProgramDirectory();
	if (pd != null){
		ewe.io.File dir = new ewe.io.File(pd);
		if (dir.isDirectory()){
			f = new ewe.io.File(dir,path);
			if (f.canRead()) return new ewe.io.RandomAccessFile(f,mode);
		}
	}
	return null;
}
/**
* This will attempt to provide a RandomAccessStream which can be used to read from
* a resource that is either stored in a ewe file, or resides on a sever (for Applet versions)
* or is a File. If the resource is in a ewe file or is on a server, then you will only be
* able to read from the stream.
* @param path The path to the resource or file.
* @param mode One of the RandomAccessStream constants: READ_ONLY or READ_WRITE
* @throws an IOException if the file could not be opened
**/
//===================================================================
public static ewe.io.RandomAccessStream openRandomAccessStream(String path,String mode)
throws ewe.io.IOException
//===================================================================
{
	int md = ewe.io.File.convertMode(mode);
	ewe.io.RandomAccessStream ras = openRandomAccessStream(path,md);
	if (ras == null) throw new IOException(path+" not found.");
	return ras;
}

/**
 * Memory map, if possible, a file on disk for later reading and possibly writing.
 * @param path the path to the file.
 * @param mode must be either "r" for read-only access and "rw" for read-write access.
 * @param options any of the MEMORY_MAP_XXX values ORed together.
 * @return a RandomAccessStream for reading from the memory mapped file.
 * @throws UnsupportedOperationException if Memory Mapping is not supported
 * or if the mode requested is not supported or if options requested could not be
 * supported and MEMORY_MAP_STRICT was specified.
 * @throws SystemResourceException if there is not enough system resources to memory map.
 * @throws IOException if there was an IO error mapping the file.
 * @throws IllegalArgumentException if the mode is not "r" or "rw".
 */
public static RandomAccessStream memoryMapFile(String path, String mode, int options)
throws UnsupportedOperationException, SystemResourceException, IOException, IllegalArgumentException
{
	if (path == null) throw new NullPointerException();
	if (mode.equals("rw")) throw new UnsupportedOperationException("Writing to a memory mapped file is not supported.");
	if (!mode.equals("r")) throw new IllegalArgumentException("Invalid R/W mode.");
	if (!new File(path).exists()) throw new IOException("File not found: "+path);
	throw new UnsupportedOperationException("Memory Mapping not supported.");
}
/**
* This will attempt to open a class dependant resource for reading. If the provided class is null or
* was loaded by the system loader or was loaded by a ClassLoader which is not an instance of ewe.util.mClassLoader,
* then this will call openRandomAccessStream(). Otherwise the openResource() method in the mClassLoader is
* called.
* @param aClass The class requesting the resource. If it is null it will assume that it is a standard application resource.
* @param resourceName The name of the resource.
* @return A Stream that can be used to read the resource.
*/
//===================================================================
public static ewe.io.Stream openResource(Class aClass,String resourceName)
//===================================================================
{
	if (aClass != null)
		try{
			ClassLoader cl = aClass.getClassLoader();
			if (cl instanceof ewe.util.mClassLoader)
				return ((ewe.util.mClassLoader)cl).openResource(resourceName);
		}catch(Throwable t){
		}
	return openRandomAccessStream(resourceName,ewe.io.File.READ_ONLY);
}
/**
* This opens a Stream to a resource using openResource and then reads in all the
* bytes. It will return null if the resource could not be found or read.
* @param aClass The class requesting the resource. If it is null it will assume that it is a standard application resource.
* @param resourceName The name of the resource.
* @return the bytes of the resource.
*/
//===================================================================
public static byte [] readResource(Class aClass,String resourceName)
//===================================================================
{
	return ewe.io.IO.readAllBytes(openResource(aClass,resourceName),null);
}

/**
 * Sets the system clipboard text.
 * @param text New text to place in clipboard.

 */
//===================================================================
public static native void setClipboardText(String text);

/**
 * Get the text in the system clipboard.
 * @param defaultText Text to return if there is no text in the system clipboard.
 * @return The text in the system clipboard, or defaultText if there is no text in the system clipboard.


 */
//===================================================================
public static native String getClipboardText(String defaultText);
//===================================================================
/**
 * Get the program arguments.
 * @return The program arguments
 */
//===================================================================
public static native String [] getProgramArguments();
//===================================================================


/**
 * This returns the hashcode for the object as if the Object.hashCode() method was called regardless
 * of any overriding hashCode() methods.
 * @deprecated See identityHashCode()
 * @param obj The object.
 * @return The identity hashcode for the object.
 */
//===================================================================
public static native int toInt(Object obj);
//===================================================================
/**
 * This returns the hashcode for the object as if the Object.hashCode() method was called regardless
 * of any overriding hashCode() methods.
 * @param obj The object.
 * @return The identity hashcode for the object.
 */
//===================================================================
public static int identityHashCode(Object obj) {return toInt(obj);}
//===================================================================

/**
* This returns properties for the current Applet <b>if</b> it is being run as an
* Applet. If it is being run as an application this will return null.
* <p>
* The available properties will be:
* <br><b>applet</b> - The Applet object itself.
* <br><b>hostName</b> - A string representing the host computer name.
* <br><b>hostPort</b> - A string representing the host computer port.
* <br><b>codeBase</b> - A string giving the codebase of the applet.
* <br><b>documentBase</b> - A string giving the documentbase of the applet.
**/
//===================================================================
public static ewe.data.PropertyList getAppletProperties()
//===================================================================
{
	return null;
}


private native static String nativeGetStackTrace(Throwable t);

/**
 * This gets a String representation of the full stack trace for a Throwable, but not for any
 * of its possible chained exceptions.
 * @param th The Throwable
 * @return a String representation of the full stack trace for the Throwable.
 */
//===================================================================
public static String getAStackTrace(Throwable th)
//===================================================================
{
	String got = th.toString();
	String nst = nativeGetStackTrace(th);
	if (nst != null) got += nst;
	return got;
}
/**
 * Get a String representation of the stack trace for a Throwable object. This trace will
 * display partial traces of all chained exception as well. To get the full trace for any
 * particular Throwable you should call getAStackTrace().
 * @param th The throwable object.
 * @return A String representation of the stack trace.
 */
//===================================================================
public static String getStackTrace(Throwable th)
//===================================================================
{
	String got = "";
	for (Throwable t = th;t != null; t = getCause(t)){
		if (t != th) got += "\nCaused by: ";
		got += t.toString();
		if (t != th) got += "\n\t... more";
		else{
			String nst = nativeGetStackTrace(t);
			if (nst != null) got += nst;
		}
	}
	return got;
}
//===================================================================
public static void printStackTrace(Throwable th,PrintWriter out)
//===================================================================
{
	out.println(getStackTrace(th));
}
/**
 * This gets the fully expanded stack trace for the Throwable and all chained throwables.
 * @param th The throwable.
 * @return the fully expanded stack trace for the Throwable and all chained throwables.
 */
//===================================================================
public static String getFullStackTrace(Throwable th)
//===================================================================
{
	String got = "";
	for (Throwable t = th;t != null; t = getCause(t)){
		if (t != th) got += "\nCaused by: ";
		got += t.toString();
		String nst = nativeGetStackTrace(t);
		if (nst != null) got += nst;
	}
	return got;
}
/**
 * This provides a fully portable method of setting the "cause" of a Throwable for
 * exception chaining. Under Java this will only have an effect on Java 1.4 or higher. Under
 * earlier versions of Java this call will not do anything.
 * @param target The target Throwable.
 * @param cause The cause for the target.
 * @return The target Throwable.
 * @exception IllegalArgumentException If the cause is the same as the target.
 * @exception IllegalStateException If the cause for the target has already been set.
 */
//===================================================================
public static Throwable setCause(Throwable target,Throwable cause) throws IllegalArgumentException, IllegalStateException
//===================================================================
{
	return target.initCause(cause);
}
/**
 * This provides a fully portable method of getting the "cause" of a Throwable for
 * exception chaining. Under Java this will only have an effect on Java 1.4 or higher. Under
 * earlier versions of Java this call will always return null.
 * @param target The target Throwable.
 * @return The cause for the Throwable.
 */
//===================================================================
public static Throwable getCause(Throwable target)
//===================================================================
{
	return target.getCause();
}
/**
 * Get a String representation of the stack trace for a Throwable object.
 * @param t The throwable object.
 * @param lines The number of lines to retrieve. If lines <= 0 then all lines will be retrieved
 * @return A String representation of the stack trace.
 */
//===================================================================
public static String getStackTrace(Throwable t,int lines)
//===================================================================
{

	String got = getStackTrace(t);
	if (got == null || lines <= 0) return null;
	int prev = 0;
	for (int i = 0; i<lines; i++){
		int found = got.indexOf('\n',prev);
		if (found == -1) return got;
		prev = found+1;
	}
	return got.substring(0,prev+1);
}
private static Object syncObject = new Lock();
static {
	((Lock)syncObject).isEweLock = true;
}
/**
* This returns an object that you can synchronize with in an object "finalize()" method.
* This is the <b>only</b> use for this object - it ensures that your application remains
* single threaded when running under Java. This is because the "finalize()" method can
* be called at any point, which may not be synchronized with the ewe application. Under
* a native Ewe VM "finalize()" will always be called in sync with your application so
* the object returned is not actually used (the synchronized() keyword is ignored in
* a Ewe VM).
**/
//===================================================================
public static Object getSyncObject()
//===================================================================
{
	return syncObject;
}
public static String getenv(String envVariableName,String defaultValue)
{
	return getProperty(envVariableName,defaultValue);
}
/**
* This works similar to the true java.lang.System.getProperty().
**/
//===================================================================
public static String getProperty(String propertyName,String defaultValue)
//===================================================================
{
	String got = properties.getString(propertyName,null);
	if (got != null) return got;
	if (propertyName.equals("file.separator")) return "/";
	else if (propertyName.equals("path.separator")) return ":";
	else if (propertyName.equals("line.separator")) return "\n";
	got = getProperty(propertyName);
	if (got != null && propertyName.equals("this.exe.path")){
		try{
			String pte = getPathToEweVM();
			File mine = File.getNewFile(got);
			File vm = File.getNewFile(pte);
			if (vm.equals(mine)) got = null;
		}catch(Exception e){

		}
	}
	if (got != null) return got;
	return defaultValue;
}
/**
* This only has an effect on the running program. It will override system values
* for this VM instance but will not affect other VMs or any OS values.
**/
//===================================================================
public static boolean setProperty(String propertyName,String value)
//===================================================================
{
	properties.set(propertyName,value);
	return true;
}


//===================================================================
private static native String getProperty(String propertyName);
//===================================================================
/**
 * Exit the application.
 * @param retCode The return code.
 */
//===================================================================
public static void exit(int retCode)
//===================================================================
{
	ewe.ui.mApp.mainApp.exit(retCode);
}
/**
* This is used to start the Ewe library running from a static main() method. Always have it
* as the first line of code in your main method. You should pass the name of the class to begin running
* AND the program arguments as supplied to the main() method.
**/
//===================================================================
public static void startEwe(String [] programArguments,String startClassName)
//===================================================================
{
	//Under a native Ewe VM, this will do nothing.
}
/**
* This is used to start the Ewe library running from a static main() method. Always have it
* as the first line of code in your main method. This calls startEwe(programArguments,startClassName)
* with a startClassName of null. This tells the system to use the stack trace to figure out which class
* called the method.
**/
//===================================================================
public static void startEwe(String [] programArguments) {startEwe(programArguments,null);}
//===================================================================
/*

native static Handle nativeDoPause(int howLong);

public static void pause(int howLong)
{
	debug("Pausing...");
	if (nativeDoPause(howLong).waitOnFlags(Handle.Succeeded,TimeOut.Forever))
		debug("Waited successfully!");
	else
		debug("Could not wait!");
}

public static void main(String args[])
{
	startEwe(args);
	new Coroutine(new Runnable(){
		public void run(){
			while(true){
				debug("Tick!");
				Coroutine.sleep(250);
			}
		}
	});

	pause(3000);
	callInSystemQueue(new CallBack(){
		public void callBack(Object data){
			pause(3000);
		}
	},null);
	Coroutine.sleep(-1);
	exit(0);
}
*/
static private Stream stdin, stdout, stderr;
/**
 * Return a Stream to the standard Input, Output or Error streams.
 * @param which one of STANDARD_INPUT, STANDARD_OUTPUT or STANDARD_ERROR
 * @return an open Stream OR null if the stream is not available
 */
//===================================================================
public static ewe.io.Stream getStandardStream(int which)
//===================================================================
{
	switch(which){
		case STANDARD_INPUT: return (stdin = nativeGetStream(stdin,which));
		case STANDARD_OUTPUT: return (stdout = nativeGetStream(stdout,which));
		case STANDARD_ERROR: return (stderr = nativeGetStream(stderr,which));
	}
	return null;
}
//-------------------------------------------------------------------
private static native ewe.io.Stream nativeGetStream(ewe.io.Stream alreadyGot,int which);
//-------------------------------------------------------------------
/**
 * Get a MemoryStatus object representing the state of the system's physical memory.
 * @return A MemoryStatus object if available on the system, or null if not.
 */
public static MemoryStatus getSystemMemoryStatus()
{
	try{
		return nativeGetMemory(new MemoryStatus());
	}catch(Error e){
		return null;
	}catch(Exception e2){
		return null;
	}
}

private static native MemoryStatus nativeGetMemory(MemoryStatus ms);

/**
Create a Task that can call a full system blocking method without
blocking the VM. Under a native Ewe VM this is not possible and this will
still block the VM, but under a Java VM the Ewe VM will not be blocked.
<p>
The task should be implemented in a CallBack Object, where the callBack(Object data)
method will hold the blocking code and the "data" parameter will be a Handle that
the task can use to set the status of the task.
**/
//===================================================================
public static Task makeBlockingTask(final CallBack cb)
//===================================================================
{
	return new TaskObject(){
		protected void doRun(){
			cb.callBack(handle);
		}
	};
}
/**
Run a task that will display a native dialog box of some kind while it runs.
The method returns immediately, returning the Handle of the nativeDialogTask.
**/
//===================================================================
public static Handle runNativeDialog(Task nativeDialogTask)
//===================================================================
{
	ewe.ui.Window.enterNativeDialog();
	final Handle h = nativeDialogTask.startTask();
	new TaskObject(){
		protected void doRun(){
			try{
				h.waitUntilStopped();
			}catch(Exception e){
			}finally{
				ewe.ui.Window.exitNativeDialog();
			}
		}
	}.startTask();
	return h;
}

}

//##################################################################
class resourceRandomAccessStream extends ewe.io.RandomStreamObject implements ewe.io.OverridesClose{
//##################################################################

int memory, size;
int pos = 0;

public boolean flushStream() throws ewe.io.IOException {return true;}

//-------------------------------------------------------------------
resourceRandomAccessStream(int [] specs)
//-------------------------------------------------------------------
{
	memory = specs[0];
	size = specs[1];
}
public boolean canWrite() {return false;}
/**
 * Closes the file. Returns true if the operation is successful and false
 * otherwise.
 */
//===================================================================
public boolean close() {return closed = true;}
//===================================================================
/**
 * Returns true if the file is open for reading or writing and
 * false otherwise. This can be used to check if opening or
 * creating a file was successful.
 */
//===================================================================
public boolean isOpen() {return !closed;}
//===================================================================

//===================================================================
public int getFilePosition() {return pos;}
//===================================================================

//===================================================================
public boolean seek(int position)
//===================================================================

{
	if (position > size || position < 0) return false;
	pos = position;
	return true;
}

//===================================================================
public int getLength() {return size;}
//===================================================================

/**
* This returns:
* >0 = Number of bytes read.
* 0 = No bytes ready to read.
* -1 = End of file.
* -2 = IO error.
**/
//-------------------------------------------------------------------
public int nonBlockingRead(byte []buf,int start,int count)
//-------------------------------------------------------------------
{
	if (pos >= size || closed) return READWRITE_CLOSED;
	if (count == 0) return 0;
	if (count > size-pos) count = size-pos;
	Vm.getResourceData(memory,pos,buf,start,count);
	pos += count;
	return count;
}
/**
* This returns:
* >0 = Number of bytes written.
* 0 = No bytes could be written yet.
* -1 = File Closed
* -2 = IO error.
**/
//-------------------------------------------------------------------
public int nonBlockingWrite(byte []buf,int start,int count)
//-------------------------------------------------------------------
{
	return READWRITE_ERROR;
}

//##################################################################
}

//##################################################################

//##################################################################
class nativeProcess extends Process{
//##################################################################
//Don't move this.
int nativeObject;

public void exec(String [] command,String [] env) throws ewe.io.IOException
{
/* This is how it works under Win32
	String pathAndExe = command[0];
	StringBuffer sb = new StringBuffer();
	for (int i = 1; i<command.length; i++){
		sb.append(command[i]);
		if (i != command.length-1) sb.append(' ');
	}
	String [] all = new String[2];
	all[0] = pathAndExe;
	all[1] = sb.toString();
*/
	String s = command[0];
	//ewe.sys.Vm.debug("Execing: "+new ewe.util.Vector(command));
	if (s.startsWith("\"")) s = s.substring(1);
	if (s.endsWith("\"")) s = s.substring(0,s.length()-1);
	command[0] = s;
	Object msg = processOperation(EXEC,command,env);
	if (msg instanceof String) throw new ewe.io.IOException((String)msg+" "+command[0]);
}
/**
 * Kills the process.
 */
public void destroy()
{
	processOperation(DESTROY,null,null);
}
/**
 * Wait until the process exits.
 */
public void waitFor()
{
	waitFor(TimeOut.Forever);
}
/**
* Wait until the process exits.
* @param t The length of time to wait.
* @return true if the process did exit, false if the timeout expired before the process exited.
*/
public boolean waitFor(TimeOut t)
{
	if (Coroutine.getCurrent() == null)
		throw new RuntimeException("Only an mThread can wait on a Process.");
	do{
		ewe.sys.Long l = (ewe.sys.Long)processOperation(CHECK_EXIT,ewe.sys.Long.l1,null);
		if (l == null) Coroutine.sleep(100);
		else return true;
	}while(!t.hasExpired());
	return false;
}
/**
 * Get the exit value of the process.
 * @return the exit value of the process.
 * @exception IllegalThreadStateException if the process is still running.
 */
public int exitValue() throws IllegalThreadStateException
{
	ewe.sys.Long l = (ewe.sys.Long)processOperation(CHECK_EXIT,ewe.sys.Long.l1,null);
	if (l == null) throw new IllegalThreadStateException("Process still running.");
	return (int)l.value;
}
/**
 * Return an input Stream to read from the standard error output of the process.
 */
public Stream getErrorStream()
{
	return (Stream)processOperation(GET_ERROR,null,null);
}
/**
 * Return an input Stream to read from the standard output of the process.
 */
public Stream getOutputStream()
{
	return (Stream)processOperation(GET_OUTPUT,null,null);
}
/**
 * Return an output Stream to write to the standard input of the process.
 */
public Stream getInputStream()
{
	return (Stream)processOperation(GET_INPUT,null,null);
}

private final static int EXEC = 0;
private final static int CHECK_EXIT = 1;
private final static int GET_ERROR = 2;
private final static int GET_INPUT = 3;
private final static int GET_OUTPUT = 4;
private final static int FINALIZE = 5;
private final static int DESTROY = 6;

//-------------------------------------------------------------------
private native Object processOperation(int operation,Object one,Object two);
//-------------------------------------------------------------------

public void finalize(){
	processOperation(FINALIZE,null,null);
}

//##################################################################
}
//##################################################################
//##################################################################
class pipeStream extends StreamObject implements OverridesClose{
//##################################################################
int nativeObject;

private native void nativeClose();
private native int readWrite(byte [] data,int start,int count,boolean isRead);
public boolean flushStream() throws ewe.io.IOException {return true;}

//===================================================================
public boolean close()
//===================================================================
{
	nativeClose();
	return true;//Stack overflow->super.close();
}

//===================================================================
public int nonBlockingRead(byte [] data,int start,int count)
//===================================================================
{
	if (count == 0) return 0;
	if (start < 0 || start+count > data.length) throw new IndexOutOfBoundsException();
	return readWrite(data,start,count,true);
}
//===================================================================
public int nonBlockingWrite(byte [] data,int start,int count)
//===================================================================
{
	if (count == 0) return 0;
	if (start < 0 || start+count > data.length) throw new IndexOutOfBoundsException();
	return readWrite(data,start,count,false);
}


//##################################################################
}
//##################################################################

