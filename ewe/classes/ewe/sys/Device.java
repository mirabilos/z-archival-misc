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
import ewe.fx.*;
import ewe.io.*;
import ewe.ui.Gui;
/**
* This class provides useful utilities for monitoring/controlling the device
* or for retrieving native resources for the device.
**/
//##################################################################
public class Device implements Runnable, VmConstants{
//##################################################################

private Device(){}
/**
See if a VM_FLAG_XXX value is set for the current VM on this device.
**/
//===================================================================
public static boolean hasFlag(int vmFlag)
//===================================================================
{
	return (Vm.getParameter(VM_FLAGS) & vmFlag) != 0;
}
/**
* Return if this device is considered a Mobile Device.
**/
//===================================================================
public static boolean isMobile()
//===================================================================
{
	return Vm.isMobile();
}
/**
Returns if this device is considered a SmartPhone Device - one that has
a phone style keyboard, 4 way navigation keys, an Action key, a Back/Cancel key
and at least two "Soft" keys.
**/
//===================================================================
public static boolean isSmartPhone()
//===================================================================
{
	return Gui.isSmartPhone;
}
/**
 * Get the native drawing surface for a Window.
 * @param window The window.
 * @return a native 32-bit pointer that can be used in ENI to directly access the window drawing
 * surface, or 0 if that is not availalbe on the running platform.
 */
//===================================================================
public static int getNativeDrawingSurface(ewe.ui.Window window)
//===================================================================
{
	Object nv = window.getInfo(window.INFO_NATIVE_WINDOW,null,null,window.NATIVE_WINDOW_GET_DRAWING_SURFACE);
	if (!(nv instanceof Long)) return 0;
	return (int)((Long)nv).value;
}
/**
 * Get the native window control associated with the Ewe Window.
 * @param window The window.
 * @return a native 32-bit pointer that can be used in ENI to directly access the Window control which
 * contains the Ewe window. Under Win32 this will be the Window handle.
 */
//===================================================================
public static int getNativeWindowControl(ewe.ui.Window window)
//===================================================================
{
	Object nv = window.getInfo(window.INFO_NATIVE_WINDOW,null,null,window.NATIVE_WINDOW_GET_CONTAINING_WINDOW);
	if (!(nv instanceof Long)) return 0;
	return (int)((Long)nv).value;
}
/**
 * This locates the x and y offset of the specified Control in its containing Window's drawing
 * surface.
 * @param c The control.
 * @return A Point that specifies the x and y offset of the specified Control in its containing Window's drawing
 * surface.
 */
//===================================================================
public static ewe.fx.Point getPositionInNativeDrawingSurface(ewe.ui.Control c)
//===================================================================
{
	ewe.ui.Window w = c.getWindow();
	if (w == null) return null;
	return (ewe.fx.Point)w.getInfo(w.INFO_POSITION_IN_NATIVE_DRAWING_SURFACE,c,null,0);
}

public static final int STATE_UNKNOWN = 0;
public static final int STATE_AWAKE = 0x1;
public static final int STATE_SUSPENDED = 0x2;
public static final int STATE_OFF = 0x3;

public static final int SCREEN_UNKNOWN = 0;
public static final int SCREEN_OFF = 0x1;
public static final int SCREEN_LOW = 0x2;
public static final int SCREEN_HIGH = 0x3;
public static final int SCREEN_NORMAL = 0x4;

/**
* This is a possible parameter for getBatteryState().
**/
public static final int BATTERY_MAIN = 1;
/**
* This is a possible parameter for getBatteryState().
**/
public static final int BATTERY_BACKUP = 2;
/**
* This is a possible parameter for getBatteryState().
**/
public static final int BATTERY_AC = 3;

public static final int BATTERY_OPTION_POWER_LEFT = 0x100;
public static final int BATTERY_OPTION_FLAGS = 0x200;
/**
* This is a possible return value for getBatteryState.
**/
public static final int BATTERY_STATE_UNKNOWN = -1;
/**
 * Return the state of the battery.
 * @param battery one of the BATTERY_XXX OR'ed with one of the BATTERY_OPTION_XXX flags.
 * @return if the battery parameter is OR'ed with BATTERY_OPTION_POWER_LEFT, a value between 0 (empty) and 100 (fully charged) or BATTERY_STATE_UNKNOWN if not known. BATTERY_AC will return
 * either 0 or 100. if the battery parameter is OR'ed with BATTERY_OPTION_FLAGS, a value with some of the BATTERY_STATE_XXX flags set, or BATTERY_STATE_UNKNOWN if not known.
 */
//===================================================================
public static int getBatteryState(int battery)
//===================================================================
{
	try{
		return nativeGetBatteryState(battery);
	}catch(Throwable t){
		return -1;
	}
}
private static native int nativeGetSetState(int state,boolean isGet);
/**
* Set the powered-on state of the device.
* @param state one of the STATE_XXX values.
* @return true if the state could be set, false if not.
*/
//===================================================================
public static boolean setState(int state)
//===================================================================
{
	try{
		return nativeGetSetState(state,false) != 0;
	}catch(Throwable t){
		return false;
	}
}
/**
 * Get the powered-on state of the device. This will be one of the STATE_XXX values.
 */
//===================================================================
public static int getState()
//===================================================================
{
	try{
		return nativeGetSetState(0,true);
	}catch(Throwable t){
		return STATE_UNKNOWN;
	}
}
private static native int nativeGetSetScreen(int state,boolean isGet);
/**
 * Set the screen state.
 * @param screen one of the SCREEN_XXX values.
 * @return true if it could be set, false if not.
 */
//===================================================================
public static boolean setScreen(int screen)
//===================================================================
{
	try{
		return nativeGetSetScreen(screen,false) != 0;
	}catch(Throwable t){
		return false;
	}
}
/**
* Get the state of the screen. It will be one of the SCREEN_XXX values.
**/
//===================================================================
public static int getScreen()
//===================================================================
{
	try{
		return nativeGetSetScreen(0,true);
	}catch(Throwable t){
		return SCREEN_UNKNOWN;
	}
}
/*
public final static int ALARM_DEFAULT = 1;
public final static int ALARM_EMAIL = 2;
public final static int ALARM_APPOINTMENT = 3;
public final static int ALARM_ATTENTION = 4;
*/
/**
 * Sound one of the device's alarms.
 * @param alarm one of the ALARM_XXX values.
 * @return true if it could be sounded, false if not.
 */
	/*
public final static boolean soundAlarm(int alarm)
{
	try{
		return nativeSoundAlarm(alarm);
	}catch(Throwable t){
		return false;
	}
}
private static native boolean nativeSoundAlarm(int alarm);
*/
private static native int nativeGetBatteryState(int state);


/**
* Get the device screen size.
**/
//===================================================================
public static ewe.fx.Dimension getScreenSize()
//===================================================================
{
	Dimension d = new Dimension();
 	Rect s = (Rect)ewe.ui.Window.getGuiInfo(ewe.ui.Window.INFO_SCREEN_RECT,null,new Rect(),0);
	d.width = s.width;
	d.height = s.height;
	return d;
}
/**
* Indicates that either a mouse or pen is present.
**/
public static final int FEATURE_MOUSE_OR_PEN = 1;
/**
* Indicates that a mouse (as distinct from a pen) is present.
**/
public static final int FEATURE_MOUSE = 2;
/**
* Indicates that a keyboard is present.
**/
public static final int FEATURE_KEYBOARD = 3;
/**
* Indicates that a direction (up/down/left/right) pad is present.
**/
public static final int FEATURE_DIRECTION_PAD = 4;
/**
* Indicates that application custom buttons are present.
**/
public static final int FEATURE_CUSTOM_BUTTONS = 5;
/**
* Indicates that the device has full audio capabilities.
**/
public static final int FEATURE_FULL_AUDIO = 6;
/**
 * Check if the device has a particular feature.
 * @param feature one of the FEATURE_XXX values.
 * @return true if the feature is present, false if not.
 */
//===================================================================
public static boolean hasFeature(int feature)
//===================================================================
{
	try{
		return nativeHasFeature(feature);
	}catch(Throwable t){
		int flags = Vm.getParameter(Vm.VM_FLAGS);
		switch(feature){
			case FEATURE_MOUSE_OR_PEN:
				return true;
			case FEATURE_MOUSE:
				return (flags & Vm.VM_FLAG_NO_MOUSE_POINTER) == 0;
			case FEATURE_KEYBOARD:
				return (flags & Vm.VM_FLAG_NO_KEYBOARD) == 0;
			case FEATURE_DIRECTION_PAD:
				return (flags & Vm.VM_FLAG_NO_KEYBOARD) == 0;
			case FEATURE_CUSTOM_BUTTONS:
				return false;//(flags & Vm.VM_FLAG_NO_KEYBOARD) == 0;
			case FEATURE_FULL_AUDIO:
				return true;
		}
		return false;
	}
}
private static native boolean nativeHasFeature(int feature);

private static native boolean nativeResetIdleState();


public static int idleTimeOut = 10000;

private static Lock idleLock;
private static Device idleState;
private static int idleCount = 0;
/**
This is used internally.
**/
//===================================================================
public void run()
//===================================================================
{
	while(true){
		idleLock.synchronize(); try{
			if (idleCount == 0) {
				idleState = null;
				return;
			}
			nativeResetIdleState();
		}finally{
			idleLock.release();
		}
		mThread.nap(idleTimeOut);
	}
}
/**
Enter or leave a period where you wish for the Device to not power down due
to being in an idle state.
* @param startPrevent true to enter the protected period, false to leave it.
* @return true if you <b>can</b> prevent the idle state, false if you cannot.
*/
//===================================================================
public static boolean preventIdleState(boolean startPrevent)
//===================================================================
{
	try{
		if (!nativeResetIdleState()) return false;
	}catch(Throwable t){
		return false;
	}
	if (idleLock == null) idleLock = new Lock();
	idleLock.synchronize(); try{
		//
		if (startPrevent) idleCount++;
		else if (idleCount != 0) idleCount--;
		//
		if (idleCount > 0 && idleState == null) {
			idleState = new Device();
			new mThread(idleState).start();
		}
	}finally{
		idleLock.release();
	}
	return true;
}
public static ewe.fx.IImage folderUp = new ewe.fx.mImage("ewe/FolderUp.bmp",Color.White);
public static ewe.fx.IImage drive = new ewe.fx.mImage("ewe/Drive.bmp",Color.White);
public static ewe.fx.IImage computer = new ewe.fx.mImage("ewe/Computer.bmp",Color.White);
public static ewe.fx.IImage palm = new ewe.fx.mImage("ewe/Palm.bmp",new Color(0,255,0));
public static ewe.fx.IImage handHeld = new ewe.fx.mImage("ewe/HandHeld.bmp",new Color(0,255,0));

//##################################################################
}
//##################################################################

