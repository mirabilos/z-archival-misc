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
package ewe.sys;

import java.awt.Component;
import java.awt.Cursor;
import java.io.File;

import ewe.applet.Applet;
import ewe.fx.Color;
import ewe.fx.Graphics;
import ewe.fx.Image;
import ewe.fx.Point;
import ewe.fx.Rect;
import ewe.fx.mImage;
import ewe.io.BufferedReader;
import ewe.io.IOException;
import ewe.io.IOTransfer;
import ewe.io.InputStreamReader;
import ewe.io.MemoryFile;
import ewe.io.PrintWriter;
import ewe.io.RandomAccessStream;
import ewe.io.Stream;
import ewe.ui.mApp;
import ewe.util.Vector;
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

private Vm()
	{
	}

//-------------------------------------------------------------------
private static ewe.data.PropertyList properties = new ewe.data.PropertyList();
//-------------------------------------------------------------------

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
/*
public static native boolean copyArray(Object srcArray, int srcStart,
	Object dstArray, int dstStart, int length);
*/
public static boolean copyArray(Object srcArray, int srcStart,
	Object dstArray, int dstStart, int length)
	{
	/*
	if (length < 0)
		return false;
	try
		{
		*/
		System.arraycopy(srcArray, srcStart, dstArray, dstStart, length);
		/*
		}
	catch (Exception e)
		{
		return false;
		}
		*/
	return true;
	}
/**
* This is exactly the same as copyArray() except that it is a void
* method and that it mirrors the standard System.arraycopy.
**/
public static void arraycopy(Object srcArray, int srcStart,
	Object dstArray, int dstStart, int length)
{
	System.arraycopy(srcArray,srcStart,dstArray,dstStart,length);
}

/**
 * Returns true if the system supports a color display and false otherwise.
 */
//public static native boolean isColor();
public static boolean isColor()
	{
	return ewe.applet.Applet.currentApplet.isColor;
	}
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

static boolean gotFirstStamp = false;
static long firstStamp = 0;
/**
 * Returns a time stamp in milliseconds. The time stamp is the time
 * in milliseconds since some arbitrary starting time fixed when
 * the VM starts executing. The maximum time stamp value is (1 << 63) and
 * when it is reached, the timer will reset to 0 and will continue counting
 * from there.
 */
public static long getTimeStampLong()
{
	if (!gotFirstStamp) {
		gotFirstStamp = true;
		firstStamp = System.currentTimeMillis();
		return 0;
	}else
		return System.currentTimeMillis()-firstStamp;
}


/** Returns the platform the Virtual Machine is running under as a string. */
//public static native String getPlatform();
public static String getPlatform()
	{
	return "Java";
	}


/**
 * Returns the username of the user running the Virutal Machine. Because of
 * Java's security model, this method will return null when called in a Java
 * applet. This method will also return null under most WinCE devices (that
 * will be fixed in a future release).
 */
//public static native String getUserName();
public static String getUserName()
	{
	if (!ewe.applet.Applet.currentApplet.isApplication)
		return null;
	return getProperty("user.name");
	}


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
 * This example executes the web clipper program under PalmOS, telling
 * it to display a web page by using launchCode 54 (CmdGoToURL).
 * <pre>
 * Vm.exec("Clipper", "http://www.yahoo.com", 54, true);
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
 * Under PalmOS, the wait parameter is ignored since executing another
 * program terminates the running program.
 *
 * @param command the command to execute
 * @param args command arguments
 * @param launchCode launch code for PalmOS applications
 * @param wait whether to wait for the command to complete execution before returning
 */
//public static native int exec(String command, String args, int launchCode, boolean wait);
public static int exec(String command, String args, int launchCode, boolean wait)
	{
	java.lang.Runtime runtime = java.lang.Runtime.getRuntime();
	int status = -1;
	try	{
		java.lang.Process p = runtime.exec(command + " " + args);
		if (wait)
			status = p.waitFor();
		else
			status = 0;
		}
	catch (Exception e) {}
	return status;
	}
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
	try{
		return new ewe.sys.javaProcess(Runtime.getRuntime().exec(command,env));
	}catch(java.io.IOException e){
		throw new ewe.io.IOException(e.getMessage());
	}
}
/**
 * Execute a new process.
 * @param command The command and argument list as a single string. This will be split up using spaces as a separator and the resulting
 * String array is then passed to exec(String [] command,String [] env). The first command must be the command to execute.
 * @param env A new set of environment variables to use for the execution. Each must be in the form "name=value".
 *              this parameter can be null.
 * @return a Process object which can be used for monitoring the status of the executing process.
 * @exception ewe.io.IOException if an error occurs while attempting to execute the program.
 */
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
* @return The path to the Ewe VM executable file, or NULL if it is not found.
*/
//===================================================================
public static String getPathToEweVM()
//===================================================================
{
	try{
		if (!ewex.registry.Registry.isNativeInitialized()) return "C:/Program Files/Ewe/Ewe.exe";
		ewex.registry.RegistryKey eweDll = ewex.registry.Registry.getLocalKey(ewex.registry.Registry.HKEY_CLASSES_ROOT,"EweFile10\\DLL",true,false);
		if (eweDll == null) return null;
		String eweLocation = (String)eweDll.getValue(null);
		if (eweLocation == null) return null;
		return eweLocation.substring(0,eweLocation.length()-3)+"exe";
	}catch(Throwable t){
		return null;
	}
}
/**
 * Execute a Ewe file using a new Ewe VM. If the to the Ewe.exe could not
 * be found, then this will return false.
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
//public static native int setDeviceAutoOff(int seconds);
public static int setDeviceAutoOff(int seconds)
	{
	return 0;
	}


/**
 * Causes the VM to pause execution for the given number of milliseconds.
 * @param millis time to sleep in milliseconds
 */
//public static native void sleep(int millis);
public static void sleep(int millis)
	{
	try
		{
		Thread.currentThread().sleep(millis);
		}
	catch (Exception e) {}
	}
public static final int SIP_IS_ON = 0x1;

//public static native void setSIP(int mode);
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
public final static int SIP_FREEZE = 0x8;
public final static int SIP_UNFREEZE = 0x10;
public final static int SIP_CURRENT = -1;
/** If VmOptions.useSIP is false, this will override it so that a setSIP() with this
 * option will activate the SIP.
 */
public final static int SIP_OVERRIDE_USE_SIP = 0x20;
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

/**
* This turns the SIP on PalmPC/PocketPC devices. The mode is a combination of the following
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
static boolean sipOn = false;
static int SimulateSip = 0;
static boolean SipLocked = false, SipFrozen = false;

public static int getSIP()
{
	return sipOn ? SIP_IS_ON : 0;
}
public static void setSIP(int mode)
{
	setSIP(mode,ewe.ui.Window.getActiveWindow());
}
public static void setSIP(int mode,ewe.ui.Window window)
{
	if (window == null) window = mApp.mainApp;
	int width = 0;
	int type = ewe.ui.SIPEvent.SIP_HIDDEN;
	int par = mode;
	if (SipFrozen && ((par & SIP_UNFREEZE) == 0)) return;
	if ((par & SIP_FREEZE) != 0) {
		SipLocked = false;
		SipFrozen = true;
	}else if ((par & SIP_UNFREEZE) != 0){
		SipFrozen = SipLocked = false;
		return;
	}
	if ((par & (SIP_ON|SIP_LEAVE_BUTTON)) == 0) {
		//
		// Must be requesting to switch it off.
		//
		if (((par & SIP_LOCK) == 0) && SipLocked) return;
		SipLocked = false;
		if (!sipOn) return;
		sipOn = false;
	}else{
		//
		//Must be requesting to switch it on.
		//
		SipLocked = (par & SIP_LOCK) != 0;
		if((par & SIP_ON) != 0){
			if (sipOn) return;
			sipOn = true;
			Rect r = window.getWindowRect(new Rect(),true);
			//GetClientRect(curHWnd,&r);
			type = ewe.ui.SIPEvent.SIP_SHOWN;
			width = r.width;
		}else
			return;
	}
	if (SimulateSip == 0) return;
	window._postEvent(type,214,width,214,0,getTimeStamp());
}

private static ewe.util.Vector sounds;


/**
 * @deprecated use SoundClip instead.
 */
//===================================================================
public static int playSound(String sound,int options)
//===================================================================
{
/*
	if (sound == null) return 0;
	if (sounds != null)
		for (int i = 0; i<sounds.size(); i+= 2)
			if (sound.equals((String)sounds.get(i))){
				((ewe.fx.SoundClip)sounds.get(i+1)).play();
				return 1;
			}
	ewe.fx.SoundClip clip = new ewe.fx.SoundClip(sound);
	sounds = ewe.util.Vector.push(sounds,clip);
	sounds = ewe.util.Vector.push(sounds,sound);
	clip.play();
	*/
	return 1;
/*
	AudioClip ac = getAudioClip(sound);
	if (ac != null) ac.play();
	return 1;
*/
}

static boolean showedKeyError = false;
public static int getAsyncKeyState(int keyCode)
{
	if (keyCode == ewe.ui.IKeys.PEN){
		if (ewe.applet.WinCanvas.getMouseState() != 0)
			return 0x8000;
		else
			return 0;
	}
	else{
		if (!showedKeyError)
			System.out.println("Warning: getAsyncKeyState() not supported under Java.");
		showedKeyError = true;
	}
	return 0;
}


/**
 * @deprecated use ewe.ui.Window.captureAppKeys() instead.
 */
public static int captureAppKeys(int options)
{
	return 1;
}
/*
public static native int playSound(String sound,int options);

public static native int getAsyncKeyState(int keyCode);

public static native int captureAppKeys(int options);

public static native int setCursor(int type);

public static native int messageBox(String text,String caption,int type)

*/
public static int messageBox(String text,String caption,int type)
{
	throw new RuntimeException("MessageBox not supported!");
}
//static boolean showedCursorError = false;
/*
public static int setCursor(int type)
{
	Component c = Applet.getDisplayed();

	if (type == WAIT_CURSOR)
		c.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
	else
		c.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
	return NO_CURSOR;
}
*/
//-------------------------------------------------------------------
private static int whichCursor(Object value)
//-------------------------------------------------------------------
{
	for (int i = 0; i<cursors.size(); i++){
		if (cursors.get(i) == value) return i+1;
	}
	return 0;
}
//===================================================================
public static void showWait(boolean show)
//===================================================================
{
	if (show && (getParameter(VM_FLAGS) & VM_FLAG_NO_MOUSE_POINTER) != 0)
		ewe.ui.Window.applicationToFront();
	setCursor(show ? WAIT_CURSOR : -2);
}
//===================================================================
public static void showWait(ewe.ui.Control c,boolean show)
//===================================================================
{
	setCursor(c,show ? WAIT_CURSOR : -2);
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
private static Object lastCursor;
//-------------------------------------------------------------------
private static Object setCursorHandle(ewe.ui.Window ct,Object cursor)
//-------------------------------------------------------------------
{
	Object ret = lastCursor;
	if (cursor == null) {
		//debug("Null cursor");
		cursor = Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR);
	}
	//Component c = Applet.getDisplayed();
	for (int i = 0; i<Applet.windows.size(); i++){
		Component c = (Component)Applet.windows.elementAt(i);
		if (c != null)
			c.setCursor((Cursor)cursor);
	}

	lastCursor = cursor;
	return ret;
}
//===================================================================
static int setCursor(ewe.ui.Control c,int type,boolean inCallback)
//===================================================================
{
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
		Object cursor = null;
		if (type > 0 && type <= cursors.size()){
			cursor = cursors.get(type-1);
		}
		ewe.ui.Window win = c == null ? null : c.getWindow();
		if (win != null) win.currentCursor = cursor;
		cursor = setCursorHandle(win,cursor);
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
		ccb.execute(tg,true);
		//doingSet = false;
		return 0;
	}
}/*
//===================================================================
static int setCursor(int type,boolean inCallback)
//===================================================================
{
	if (inCallback || Coroutine.getCurrent() == null){
		Object cursor = null;
		if (type > 0 && type <= cursors.size()){
			cursor = cursors.get(type-1);
		}
		cursor = setCursorHandle(cursor);
		return whichCursor(cursor);
	}else{
		CursorCallBack ccb = new CursorCallBack();
		callInSystemQueue(ccb,new ewe.sys.Long().set(type));
		while(!ccb.did) Coroutine.sleep(0);
		return ccb.old;
	}
}
*/
//===================================================================
public static int createCursor(Object cursor)
//===================================================================
{
	if (cursor == null) return 0;
	cursors.add(cursor);

	return whichCursor(cursor);
}

/**
* This is the only one apart from DEFAULT_CURSOR you should use directly with
* ewe.sys.Vm.setCursor(). All others should be used with Control.setCursor().
**/
public static final int WAIT_CURSOR  = 1;
public static final int IBEAM_CURSOR  = 2;
public static final int HAND_CURSOR  = 3;
public static final int CROSS_CURSOR  = 4;
public static final int LEFT_RIGHT_CURSOR  = 5;
public static final int UP_DOWN_CURSOR  = 6;
/**
* This cursor is an arrow and an hour glass. Use this when an individual component
* will not respond because it is busy, but other components will work.
**/
public static final int BUSY_CURSOR = 7;
public static final int DRAG_SINGLE_CURSOR = 8;
public static final int DRAG_MULTIPLE_CURSOR = 9;
public static final int COPY_SINGLE_CURSOR = 10;
public static final int COPY_MULTIPLE_CURSOR = 11;
public static final int DONT_DROP_CURSOR = 12;
public static final int MOVE_CURSOR = 13;
public static final int RESIZE_CURSOR = 14;
public static final int INVISIBLE_CURSOR = 15;

public static final int NO_CURSOR = 0;
public static final int DEFAULT_CURSOR = 0;

private static ewe.util.Vector cursors = new ewe.util.Vector();
static
{
	cursors.add(getCursorHandle(WAIT_CURSOR));
	for (int i = WAIT_CURSOR+1; i<=INVISIBLE_CURSOR; i++){
		Object toAdd = getCursorHandle(i);
		if (toAdd == null)
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
			case LEFT_RIGHT_CURSOR:
				toAdd = new mImage("ewe/LeftRightCursor.bmp","ewe/LeftRightCursorMask.bmp").toCursor(new Point(10,4));
				break;
			case UP_DOWN_CURSOR:
				toAdd = new mImage("ewe/UpDownCursor.bmp","ewe/UpDownCursorMask.bmp").toCursor(new Point(4,10));
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
		cursors.add(toAdd);
	}
}
private static Object getCursorHandle(int which)
{
	Object ret = null;
	switch(which){
		case WAIT_CURSOR: return Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR);
		case HAND_CURSOR: return Cursor.getPredefinedCursor(Cursor.HAND_CURSOR);
		case CROSS_CURSOR: return Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR);
		case IBEAM_CURSOR: return Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR);
		//case UP_DOWN_CURSOR:
		//case LEFT_RIGHT_CURSOR:
		case MOVE_CURSOR:
			return Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR);
		case RESIZE_CURSOR: return Cursor.getPredefinedCursor(Cursor.SE_RESIZE_CURSOR);
	}
	return ret;
}

/*
 *  flag values for fuSound and fdwSound arguments on [snd]PlaySound
 */
public static final int SND_SYNC            = 0x0000  ; /* play synchronously (default) */
public static final int SND_ASYNC           = 0x0001  ; /* play asynchronously */
public static final int SND_NODEFAULT       = 0x0002  ; /* silence (!default) if sound not found */
public static final int SND_MEMORY          = 0x0004  ; /* pszSound points to a memory file */
public static final int SND_LOOP            = 0x0008  ; /* loop the sound until next sndPlaySound */
public static final int SND_NOSTOP          = 0x0010  ; /* don't stop any currently playing sound */

public static final int SND_NOWAIT	= 0x00002000 ; /* don't wait if the driver is busy */
public static final int SND_ALIAS       = 0x00010000 ; /* name is a registry alias */
public static final int SND_ALIAS_ID	= 0x00110000 ; /* alias is a predefined ID */
public static final int SND_FILENAME    = 0x00020000 ; /* name is file name */
public static final int SND_RESOURCE    = 0x00040004 ; /* name is resource name or atom */


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

private static int curId = 0;
public static int getNewId() {return ++curId;}
/**
* Request a timer tick to be sent to the TimerProc target. It returns a unique
* timer id which will be sent to the target on each tick. If repeat is false only one
* tick will be sent.
* @param target The TimerProc object to receive the ticks.
* @param interval The time in milliseconds.
* @param repeat true if the timer should receive ticks at regular intervals until cancelTimer is called.
* @return
*/
public static int requestTick(TimerProc target,int interval,boolean repeat)
{
	return new TimerEntry(target,interval,repeat).id;
}
public static void cancelTimer(int timerId)
{
	TimerEntry te = TimerEntry.findTimer(timerId);
	if (te != null) te.cancel();
}
/**
* Request a timer tick to be sent to the TimerProc target. It returns a unique
* timer id which will be sent to the target on each tick.
**/
public static int requestTimer(TimerProc target,int interval)
{
	return requestTick(target,interval,true);
}


/**
* This cancels the current timer and then requests a new one with a new interval. The
* returned value is the id of the new timer.
**/
public static int changeTimer(int timerId,TimerProc target,int newInterval)
{
	cancelTimer(timerId);
	return requestTimer(target,newInterval);
}
//===================================================================
public static int setParameter(int ParameterId,int value)
//===================================================================
{
int ret = 0;
	if (ParameterId == TIMER_TICK){
		ret = mApp.tickTime;
		mApp.tickTime = value;
	}else if (ParameterId == SIMULATE_SIP){
		SimulateSip = value;
	}else if (ParameterId == SET_NO_WINDOWS){
		ewe.applet.Applet.myVmFlags |= VM_FLAG_NO_WINDOWS;
	}
	return ret;
}
/**
* This gets paramter values. It only works currently for VM_FLAGS.
**/
//===================================================================
public static int getParameter(int parameterID)
//===================================================================
{
	if (parameterID == VM_FLAGS) {
		int ret = ewe.applet.Applet.myVmFlags;
		if (ewe.applet.Applet.applet != null)
			if (!ewe.applet.Applet.applet.isApplication)
				ret |= VM_FLAG_IS_APPLET;
		return ret;
	}else if (parameterID == VM_PATH_SEPARATOR){
		return java.io.File.pathSeparatorChar;
	}else if (parameterID == VM_FILE_SEPARATOR){
		return java.io.File.separatorChar;
	}else if (parameterID == SIMULATE_SIP){
		return SimulateSip;
	}
		return 0;
}
/**
 * Returns true if this VM is running on what is considered a Mobile platform. This is a convenienc
 * method that uses the getParameter() method.
 */
public static boolean isMobile()
{
	return ((getParameter(VM_FLAGS) & VM_FLAG_IS_MOBILE) != 0);
}
/**
* Used with getParameter() it gets VM flag bits.
**/
public static final int VM_FLAGS = 4;
/**
* This is a VM flag bit which indicates that the main program class was
* loaded from a class file rather than from a ewe file. This only works
* from the desktop.
**/
public static final int VM_FLAG_USING_CLASSES = 0x1;
/**
* This is a VM flag bit which indicates that the VM is running on a Mobile PC.
**/
public static final int VM_FLAG_IS_MOBILE = 0x2;
/**
* This is a VM flag bit which indicates that the VM is running on a slow machine.
**/
public static final int VM_FLAG_SLOW_MACHINE = 0x4;
/**
* This is a VM flag bit which indicates that the VM is running on a system with a monochrome display.
**/
public static final int VM_FLAG_IS_MONOCHROME = 0x8;
/**
* This is a VM flag bit which indicates that the VM is running on a system that has no keyboard.
**/
public static final int VM_FLAG_NO_KEYBOARD = 0x10;
/**
* This is a VM flag bit which indicates that the VM is running on a system that has no mouse pointer.
**/
public static final int VM_FLAG_NO_MOUSE_POINTER = 0x20;
/**
* This is a VM flag bit which indicates that the VM is running as an Applet in a browser.
**/
public static final int VM_FLAG_IS_APPLET = 0x40;
/**
* This is a VM flag bit which indicates that multiple native windows are not supported.
**/
public static final int VM_FLAG_NO_WINDOWS = 0x80;
/**
* This is a VM flag bit which indicates that the platform has a SipButton at the bottom that may overwrite
* program controls (only PocketPC has this).
**/
public static final int VM_FLAG_SIP_BUTTON_ON_SCREEN = 0x100;
/**
* This is a VM flag bit which indicates that the platform does not want CR characters in its text files. Generally
* this indicates a Unix system.
**/
public static final int VM_FLAG_NO_CR = 0x200;
/**
* This is a VM flag bit which indicates that the program was run with a command line requesting the screen
* to be rotated by 90 degrees.
**/
public static final int VM_FLAG_ROTATE_SCREEN = 0x400;
/**
* This is a VM flag bit which indicates that the program is running on the Sharp Zaurus Jeode(R) EVM. If it is
* then a number of bug fixes must be implemented and other flags set.
**/
public static final int VM_FLAG_IS_ZAURUS_EVM = 0x800;
/**
* This is a VM flag bit which indicates that the program is running on low-memory device
* such as the Casio BE300. Your application should minimize on the complexity of its user
* interface in such cases.
**/
public static final int VM_FLAG_LOW_MEMORY = 0x1000;

/**
* Used with setParameter() it adjusts the minimum timer tick.
**/
public static final int TIMER_TICK = 1;
/**
* Used with setParameter() it switches on (1) or off (0) SIP simulation for
* Java and Waba on the PC.
**/
public static final int SIMULATE_SIP = 2;
/**
* Used with setParameter(), it sets the maximum number of combined timer/callback/coroutine
* entries the system can handle. By default this value is 20. If you are setting this
* value you must do it before using any tiemrs/callbacks/coroutines.
**/
public static final int MAX_TIMER_ENTRIES = 3;
/**
* Used with setParameter() it switches on (1) or off (0) simulation of single
* windowing.
**/
public static final int SET_NO_WINDOWS = 4;
/**
* Used with setParameter() it switches on (1) or off (0) simulation of single
* windowing.
**/
public static final int SET_ALWAYS_SHOW_SIP_BUTTON = 5;
/**
* Used with getParameter() or setParameter() it determines whether the VM will use the SIP (Supplementary Input Panel).
* When used as ParameterId with setParameter() a value of 0 will disable the use of the SIP and a value of 1 will enable it.
**/
public static final int SET_USE_SIP = 10;


public static int getMessage(SystemMessage dest,boolean peek,boolean remove)
{
	return Applet.currentApplet.mainWindow.getMessage(dest,peek,remove);
}
public static int callInSystemQueue(CallBack who,Object data)
{
	return Applet.currentApplet.mainWindow.callBackInMessageThread(who,data);
}
public static int callInSystemQueue(ewe.ui.Window w,CallBack who,Object data)
{
	return Applet.currentApplet.mainWindow.callBackInMessageThread(who,data);
}

/**
 * This checks if the current thread is being run within the System Queue.
 * @return true if the current thread is in the System Queue, false otherwise.
 */
public static boolean amInSystemQueue()
{
	if (vmInSystemQueue) return true;
	return ewe.ui.Window.amInMessageThread();
}
private static boolean vmInSystemQueue = false;

public static int readResource(String fileName,String resourceName,int options)
{
	return -1;
}

public static int releaseResource(String resourceName)
{
	return 0;
}

public static void gc() {System.gc();}

//-------------------------------------------------------------------
static Locale l;
//-------------------------------------------------------------------
/**
* Return a Local object appropriate for the location.
**/
//===================================================================
public static Locale getLocale() {if (l == null) l = new Locale(); return l;}
//===================================================================
/**
* Force a halt of the application due to a critical error.
**/
//===================================================================
public static void applicationError(String error)
//===================================================================
{
	throw new RuntimeException("Critical Application error: "+error);
}
/**
* Using the native VM this will expose the char array which represents the String. Use this
* with care! If you write into it using the native VM you will change the String which is SUPPOSED
* to be immutable.
**/
//===================================================================
public static char [] getStringChars(String str)
//===================================================================
{
	if (str == null) return null;
	return str.toCharArray();
}
/**
* Create a new String that uses the specified character array <b>without</b>
* creating a new character array for the String.<p>Use this with care. After
* creating the String you may be able to change it since you may have direct
* access to the String's characters. However under some systems (e.g. Java) this method
* may allocate a new copy of the array so do not assume that the provided
* character array <b>will</b> be used directly.
**/
//===================================================================
public static String createStringWithChars(char[] chars)
//===================================================================
{
	return new String(chars);
}
//===================================================================
public static String mutateString(String original,char [] chars,int start,int length,boolean useThisArray)
//===================================================================
{
	return new String(chars,start,length);
}
//===================================================================
public static void debug(String message,int options)
//===================================================================
{
	System.out.println(message);
}
//===================================================================
public static void debug(String message) {debug(message,0);}
//===================================================================
//===================================================================
public static void debugObject(Object data,int options){}
//===================================================================

//===================================================================
public static int countObjects(boolean doGCFirst) {return -1;}
//===================================================================
//===================================================================
public static int getUsedMemory(boolean doGCFirst) {return -1;}
//===================================================================
//===================================================================
public static Object [] getReferencedObjects() {return new Object[0];}
//===================================================================
/**
* This returns the amount of class memory used in bytes.
**/
//===================================================================
public static int getClassMemory(){return -1;}
//===================================================================
/**
* This reads in a resource or a file with the specified path into the provided ByteArray.
* It returns: 0 = Could not load. 1 = Loaded.
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
/* Old version.
//===================================================================
public synchronized static int loadResourceOrFile(String path,ewe.util.ByteArray dest)
//===================================================================
{
	try{
		Stream st = ewe.applet.Applet.openStreamForReading(path);
		if (st == null) return 0;
		MemoryFile mf = new MemoryFile();
		if (!(new IOTransfer().run(st,mf))) return 0;
		st.close();
		dest.data = mf.data.data;
		dest.length = mf.data.length;
	}catch(Exception e){
		e.printStackTrace();
	}
	return 1;
}
*/
/**
* This will attempt to provide a RandomAccessStream which can be used to read from
* a resource that is either stored in a ewe file, or resides on a sever (for Applet versions)
* or is a File. If the resource is in a ewe file or is on a server, then you will only be
* able to read from the stream.
*@deprecated
**/
//===================================================================
public static ewe.io.RandomAccessStream openRandomAccessStream(String path,int mode)
//===================================================================
{
	boolean isFile = false;
	boolean isApplication = true;
	if (ewe.applet.Applet.currentApplet != null)
		isApplication = ewe.applet.Applet.currentApplet.isApplication;
	if (isApplication){
		ewe.io.File f = new ewe.io.File(path);
		if (f.canRead())try{
			isFile = true;
			return f.toRandomAccessStream(mode == ewe.io.File.READ_ONLY ? "r" : "rw");
		}catch(Exception e){
			return null;
		}
		String pd = f.getProgramDirectory();
		if (pd != null){
			f = new ewe.io.File(pd+"/"+path);
			if (f.canRead()) return new ewe.io.RandomAccessFile(f,mode);
		}
	}
	Stream st = ewe.applet.Applet.openStreamForReading(path);
	if (st == null) return null;
	MemoryFile mf = new MemoryFile();
	if (!(new IOTransfer().run(st,mf))) return null;
	/*
	if (!(new IOTransfer().start(st,mf).waitOnFlags(ewe.sys.Handle.Success,ewe.sys.TimeOut.Forever)))
		return null;
	*/
	st.close();
	mf.seek(0);
	return mf;
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
* This will attempt to open a class dependant resource for reading. If the provided class
* was loaded by the system loader or by a ClassLoader which is not an instance of ewe.util.mClassLoader,
* then this will call openRandomAccessStream(). Otherwise the openResource() method in the mClassLoader is
* called.
**/
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
**/
//===================================================================
public static byte [] readResource(Class aClass,String resourceName)
//===================================================================
{
	return ewe.io.IO.readAllBytes(openResource(aClass,resourceName),null);
}

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
		String version = System.getProperties().getProperty("java.version");
		if (version == null) throw new UnsatisfiedLinkError();
		String [] v = ewe.util.mString.split(version,'.');
		if (v.length < 2) throw new UnsatisfiedLinkError();
		int f = Convert.toInt(v[0]);
		if (f < 1) throw new UnsatisfiedLinkError();
		if (f == 1)
			if (Convert.toInt(v[1]) < 2)
				throw new UnsatisfiedLinkError();
		System.loadLibrary(name);
}
//===================================================================
public static void load(String libraryName)
throws SecurityException, UnsatisfiedLinkError
//===================================================================
{
	System.load(libraryName);
}


	/*
	String nn = name+".dll";
	RandomAccessStream ras = openRandomAccessStream(nn,RandomAccessFile.READ_ONLY);
	if (ras == null)
	ewe.io.File f = new ewe.io.File("").createTempFile(nn,null);
	if (f != null){
		Stream out = new FileOutputStream(f);
		new IOTransfer().run(ras,out);
		ras.close();
		out.close();
	}
	return nativeLoadLibrary(f.getFullPath());
	*/
static {
	//loadLibrary("java_ewe");
}
//===================================================================
public static boolean nativeLoadLibrary(String dllName)
//===================================================================
{
	try{
		System.loadLibrary(dllName);
		return true;
	}catch(Throwable e){
		//e.printStackTrace();
		return false;
	}
}

static String clipText;
//===================================================================
public static void setClipboardText(String text)
//===================================================================
{
	clipText = text;
	try{
		if (text == null) text = "";
		java.awt.Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new java.awt.datatransfer.StringSelection(text),null);
	}catch(Throwable e){
	}
}

//===================================================================
public static String getClipboardText(String defaultText)
//===================================================================
{
	try{
		return (String)java.awt.Toolkit.getDefaultToolkit().getSystemClipboard().getContents(new Object()).getTransferData(java.awt.datatransfer.DataFlavor.stringFlavor);
	}catch(Throwable t){
		//System.out.println(e);
		if (clipText == null) return defaultText;
		return clipText;
	}
}
public static String [] getProgramArguments() {return ewe.applet.Applet.programArguments;}
public static int toInt(Object obj)
{
	return identityHashCode(obj);
}
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
	ewe.applet.Applet applet = ewe.applet.Applet.applet;
	if (applet == null)
		throw new IllegalStateException("To run a Ewe application you must use the command line:\n\tjava -cp ewe.jar Ewe YourClassName\n");
	if (applet.isApplication) return null;
	ewe.data.PropertyList pl = new ewe.data.PropertyList();
	java.net.URL url = applet.getCodeBase();
	pl.set("applet",applet);
	pl.set("hostName",url.getHost());
	pl.set("hostPort",ewe.sys.Convert.toString(url.getPort()));
	pl.set("codeBase",url.toString());
	pl.set("documentBase",ewe.util.mString.toString(applet.getDocumentBase()));
	return  pl;
}
private static ewe.io.File fileObject;
/**
 * Set the File object to be used to represent the local file system.
 * @param fileObject the File object to be used to represent the local file system.
 * @exception SecurityException If the security system does not allow this. Currently
 * this operation is not allowed to be done more than once.
 */
//===================================================================
public static void setFileObject(ewe.io.File fileObject) throws SecurityException
//===================================================================
{
	if (Vm.fileObject != null) throw new SecurityException("File Object already set.");
	Vm.fileObject = fileObject;
}

//===================================================================
public static ewe.io.File newFileObject()
//===================================================================
{
	if (fileObject != null) return fileObject.getNew("");
	if (ewe.applet.Applet.fileSystem != null) return ewe.applet.Applet.fileSystem.getFile();
	return new ewe.io.File("");
}
//===================================================================
public static String getStackTrace(Throwable t)
//===================================================================
{
	return getStackTrace(t,-1);
}
//===================================================================
public static void printStackTrace(Throwable th,PrintWriter out)
//===================================================================
{
	out.println(getStackTrace(th));
}

//===================================================================
public static String getFullStackTrace(Throwable th)
//===================================================================
{
	String got = "";
	for (Throwable t = th;t != null; t = getCause(t)){
		if (t != th) got += "\nCaused by: ";
		got += getAStackTrace(t);
	}
	return got;
}
//===================================================================
public static String getAStackTrace(Throwable t)
//===================================================================
{
	if (t == null) return null;
	java.io.StringWriter sw = new java.io.StringWriter();
	t.printStackTrace(new java.io.PrintWriter(sw));
	java.io.BufferedReader br = new java.io.BufferedReader(new java.io.StringReader(sw.toString()));
	String got = "";
	for (int i = 0;;i++){
		try{
			String line = br.readLine();
			if (line == null) break;
			if (line.startsWith("Caused by:")) break;
			if (i != 0) got += "\n";
			got += line;
		}catch(Exception e){break;}
	}
	return got;
}
//===================================================================
public static String getStackTrace(Throwable t,int lines)
//===================================================================
{
	if (t == null) return null;
	java.io.StringWriter sw = new java.io.StringWriter();
	java.io.PrintWriter pw = new java.io.PrintWriter(sw);
	t.printStackTrace(pw);
	java.io.BufferedReader br = new java.io.BufferedReader(new java.io.StringReader(sw.toString()));
	String got = "";
	for (int i = 0;lines < 0 || i<lines; i++){
		try{
			String line = br.readLine();
			if (line == null) break;
			if (i != 0) got += "\n";
			got += line;
		}catch(Exception e){break;}
	}
	return got;
}

//
// To compile these two you must rename java/_lang2 to java/lang
//
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
	try{
		return target.initCause(cause);
	}catch(NoSuchMethodError e){
		return target;
	}
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
	try{
		return target.getCause();
	}catch(NoSuchMethodError e){
		return null;
	}
}


//===================================================================
public static Object getSyncObject()
//===================================================================
{
	return Coroutine.lockObject;
}
/**
* This works similar to the true java.lang.System.getProperty(). You can get
* the value of an OS environment variable by using "os.env.variable_name" where
* variable_name is the name of the environment variable.
**/
//===================================================================
public static String getProperty(String propertyName,String defaultValue)
//===================================================================
{

	String got = properties.getString(propertyName,null);
	if (got != null) return got;
	got = getProperty(propertyName);
	if (got != null) return got;
	return defaultValue;
}
//===================================================================
public static boolean setProperty(String propertyName,String value)
//===================================================================
{
	properties.set(propertyName,value);
	return true;
}
/**
 * Get an OS environment variable name.
 * @param envVariableName the name of the variable.
 * @param defValue the default value to return if it is not found.
 * @return the value or the default value if it is not found.
 */
public static String getenv(String envVariableName,String defValue)
{
	String s = System.getenv(envVariableName);
	if (s == null) s = defValue;
	return s;
}

//-------------------------------------------------------------------
private static String getProperty(String propertyName)
//-------------------------------------------------------------------
{
	if (propertyName.equals("this.ewe.path"))
		return Applet.eweFile;
	else try{
		return System.getProperty(propertyName);
	}catch(Throwable t){
		return null;
	}
}
//-------------------------------------------------------------------
static void preloadVM(boolean doLoad)
//-------------------------------------------------------------------
{
	throw new NoSuchMethodError();
}
//===================================================================
public static int identityHashCode(Object obj) {return System.identityHashCode(obj);}
//===================================================================
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
	if (ewe.ui.mApp.mainApp != null) return;
	if (programArguments == null) programArguments = new String[0];
	if (startClassName == null){
		try{
			String st = getStackTrace(new Exception()).replace('/','.');
			int idx = st.indexOf("ewe.sys.Vm.startEwe");
			while(true){
				idx = st.indexOf('\n',idx);
				if (idx == -1) throw new RuntimeException();
				idx = st.indexOf("at ",idx);
				if (idx == -1) throw new RuntimeException();
				int s = idx+3;
				while (!Character.isJavaIdentifierStart(st.charAt(s))) s++;
				int e = s+1;
				while (Character.isJavaIdentifierPart(st.charAt(e)) || (st.charAt(e) == '.')) e++;
				startClassName = st.substring(s,e);
				startClassName = startClassName.substring(0,startClassName.lastIndexOf('.'));
				if (!startClassName.equals("ewe.sys.Vm")) break;
			}
		}catch(RuntimeException e){
			throw new RuntimeException("Cannot determine calling class.");
		}
	}
	String [] args = new String[programArguments.length+1];
	args[0] = startClassName;
	copyArray(programArguments,0,args,1,programArguments.length);
	try{
		ewe.applet.Applet.main(args);
		try{
			synchronized(Thread.currentThread()){
				Thread.currentThread().wait();
			}
		}catch(InterruptedException e){

		}
	}catch(Exception e){
		e.printStackTrace();
	}
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
	return new Task(){
		Handle h = new Handle();
		public Handle getHandle() {return h;}
		public Handle stopTask(int reason){return h;}
		public Handle startTask(){
			h.set(h.Running);
			new Thread(){
				public void run(){
					try{
						cb.callBack(h);
					}finally{
						h.setFlags(h.Stopped,h.Running);
					}
				}
			}.start();
			return h;
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

private static native MemoryStatus nativeGetMemory(MemoryStatus ms);

private static Stream stdin, stdout, stderr;
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
private static ewe.io.Stream nativeGetStream(ewe.io.Stream alreadyGot,int which)
//-------------------------------------------------------------------
{
	if (alreadyGot != null) return alreadyGot;
	switch(which){
		case STANDARD_INPUT: return new ewe.applet.JavaInputStream(System.in);
		case STANDARD_OUTPUT: return new ewe.applet.JavaOutputStream(System.out);
		case STANDARD_ERROR: return new ewe.applet.JavaOutputStream(System.err);
	}
	return null;
}
public static void main(String args)
{

}

/*
//==================================================================
public void _onTimerTick()
//==================================================================
{
	if (timers.size() != 0) {
		tickNow.clear();
		int curTime = Vm.getTimeStamp();
		for (int i = 0; i<timers.size(); i++){
			TimerEntry te = (TimerEntry)timers.get(i);
			if (te.willTick(curTime)) tickNow.add(te);
		}
		for (int i = 0; i<tickNow.size(); i++){
			TimerEntry te = (TimerEntry)tickNow.get(i);
			te.tick(Vm.getTimeStamp());
			if (!te.repeat) timers.remove(te);
		}
	}
	_setTimerInterval(tickTime);
	if (timers != null) super._onTimerTick();
}
//==================================================================
public void _setTimerInterval(int time)
//==================================================================

{
	if (time > tickTime || time == 0) time = tickTime;
	//super._setTimerInterval(time);
}
*/

//##################################################################
static class TimerEntry {
//##################################################################
TimerProc proc;
int id, millis;
boolean repeat;
int nextTime;
int lastTime;

static Vector entries = new Vector();
static java.lang.Thread tickThread;
//===================================================================
static TimerEntry findTimer(int id)
//===================================================================
{
	for (int i = 0; i<entries.size(); i++){
		TimerEntry te = (TimerEntry)entries.get(i);
		if (te.id == id) return te;
	}
	return null;
}
//===================================================================
public TimerEntry(TimerProc p,int m,boolean rep)
//===================================================================
{
	proc = p; millis = m; repeat = rep;
	if (proc == null) return;
	id = getNewId();
	while (findTimer(id) != null) id = Vm.getNewId();
	getNextTime(getTimeStamp(),0);
	synchronized(entries){
		if (tickThread == null) {
		tickThread = new java.lang.Thread(){
	public void run(){
	try{
		Vector tickNow = new Vector();
		int ticked = 0;
		while(true){
			synchronized(Vm.getSyncObject()){
				int curTime = Vm.getTimeStamp();
				tickNow.clear();
				for (int i = 0; i<entries.size(); i++){
					TimerEntry te = (TimerEntry)entries.get(i);
					if (te.willTick(curTime)) tickNow.add(te);
				}
				for (int i = 0; i<tickNow.size(); i++){
					TimerEntry te = (TimerEntry)tickNow.get(i);
					te.tick(Vm.getTimeStamp());
					if (!te.repeat) te.cancel();
				}
				curTime = Vm.getTimeStamp();
			}
			synchronized(entries){
				int waitFor = -1;
				boolean has = false;
				int curTime = Vm.getTimeStamp();
				for (int i = 0; i<entries.size(); i++){
					TimerEntry te = (TimerEntry)entries.get(i);
					int toWait = te.nextTime-curTime;
					if (!has || waitFor > toWait) waitFor = toWait;
					has = true;
				}
				if (has && waitFor <= 0) continue;
				try{
					if (waitFor < 0) entries.wait();
					else entries.wait(waitFor);
					/*
				if (waitFor < 0) //Nothing to wait for.
					waitFor = 10000;
				Thread.currentThread().sleep(waitFor);
					*/
				}catch(InterruptedException e){
				}
			}
		}
		}finally{
			tickThread = null;
		}
	}
};
			tickThread.start();
		}
		entries.add(this);
		entries.notifyAll();
	}
	//nextTime = Vm.getTimeStamp();
}

//===================================================================
public void getNextTime(int now,int howLate)
//===================================================================
{
	lastTime = now;
	int it = millis;
	if (howLate <= 0) it -= howLate;
	else it = it-(howLate%it);
	nextTime = now+it;
}

//===================================================================
public boolean willTick(int curTime)
//===================================================================
{
	return (curTime >= nextTime);
}
//===================================================================
public void tick(int now)
//===================================================================
{
	int late = now-nextTime;
	int elapsed = now-lastTime;
	getNextTime(now,late);
	vmInSystemQueue = true;
	proc.ticked(id,elapsed);
	vmInSystemQueue = false;
}
public String toString() {return ""+id+","+millis;}

//===================================================================
public void cancel()
//===================================================================
{
	entries.remove(this);
}
//##################################################################
}
//##################################################################

}
