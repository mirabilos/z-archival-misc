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

//##################################################################
public interface WindowConstants{
//##################################################################
/**
* Use this to get or set the window location
* and size on the screen. When calling getInfo you must provide a Rect object
* as the result destination. When calling setInfo you must provide a Rect object
* as the sourceParameter.
**/
public static final int INFO_WINDOW_RECT = 1;
/**
* Use this to get or set the window (i.e. native application window) flags. This
* includes the FLAG_ values. When calling getInfo you must provide a Long object as
* the result destination. When calling setInfo you must provide a Long object as the
* sourceParameter.
**/
public static final int INFO_WINDOW_FLAGS = 2;
/**
* Use this to get or set the parent's client screen rectangle. Only the width and height values
* are relevant. When calling getInfo you must provide a Rect object
* as the result destination.
**/
public static final int INFO_PARENT_RECT = 3;
/**
* Use this to get the Rect of the client area, but the x and y co-ordinates will always be 0,0.
**/
public static final int INFO_CLIENT_RECT = 4;

public static final int FLAG_HAS_CLOSE_BUTTON = 0x1;
public static final int FLAG_IS_VISIBLE = 0x2;
public static final int FLAG_IS_ICONIZED = 0x4;
public static final int FLAG_HAS_TITLE = 0x8;
public static final int FLAG_CAN_MAXIMIZE = 0x10;
public static final int FLAG_CAN_MINIMIZE = 0x20;
public static final int FLAG_CAN_RESIZE  = 0x40;
public static final int FLAG_IS_MODAL = 0x80;
public static final int FLAG_DONT_CLEAR_BACKGROUND = 0x100;
public static final int FLAG_HAS_TASKBAR_ENTRY = 0x200;
public static final int FLAG_SHOW_SIP_BUTTON = 0x400;
public static final int FLAG_MAXIMIZE_ON_PDA = 0x800;
public static final int FLAG_MAXIMIZE = 0x1000;
public static final int FLAG_MINIMIZE = FLAG_IS_ICONIZED;
static final int FLAG_MAIN_WINDOW_ROTATED = 0x2000;
public static final int FLAG_FULL_SCREEN = 0x4000;
public static final int FLAG_ALWAYS_ON_TOP = 0x10000;
public static final int FLAG_VISIBLE_ON_TO_FRONT = 0x20000;
static final int FLAG_MAIN_WINDOW_COUNTER_ROTATED = 0x40000;


static final int FLAG_RESTORE = 0x8000;
static final int FLAG_STATE_KNOWN = 0x8000;


public static final int FLAG_IS_DEFAULT_SIZE = 0x80000000;
/**
* Use this with setInfo(INFO_WINDOW_FLAGS) to specify that the provided flags should be set.
**/
public static final int OPTION_FLAG_SET = 0x1;
/**
* Use this with setInfo(INFO_WINDOW_FLAGS) to specify that the provided flags should be cleared.
**/
public static final int OPTION_FLAG_CLEAR = 0x2;
/**
* This is used to get the window flags for a particular size.
**/
public static final int INFO_FLAGS_FOR_SIZE = 5;
/**
* An operation for Window.doSpecialOp().
**/
public static final int SPECIAL_MOUSE_MOVE = 1;
/**
* An operation for Window.doSpecialOp().
**/
public static final int SPECIAL_MOUSE_RESIZE = 2;
/**
* An operation for Window.doSpecialOp().
**/
public static final int SPECIAL_MOUSE_CAPTURE = 3;
/**
* An operation for Window.doSpecialOp().
**/
public static final int SPECIAL_MOUSE_RELEASE = 4;
/**
* An operation for Window.doSpecialOp().
**/
public static final int SPECIAL_RESTART_GUI = 5;

static final int SPECIAL_ROTATE_SCREEN = 6;
/**
* Use this to get or set the user's screen rectangle. When calling getInfo you must provide a Rect object
* as the result destination. When calling setInfo you must provide a Rect object
* as the sourceParameter.
**/
public static final int INFO_SCREEN_RECT = 6;
/**
* Use this along with a Window.TaskBarIconInfo class to set the taskbar icon info for a window.
**/
public static final int INFO_TASKBAR_ICON = 7;
/**
* This can be used with INFO_TASKBAR_ICON to specify that you are modifying the ICON only.
**/
public static final int OPTION_TASKBAR_ICON_MODIFY_ICON = 0x1;
/**
* This can be used with INFO_TASKBAR_ICON to specify that you are modifying the TIP only.
**/
public static final int OPTION_TASKBAR_ICON_MODIFY_TIP = 0x2;
/**
* Use this along with an Icon Object to set the window icon for a window.
**/
public static final int INFO_WINDOW_ICON = 8;
/**
* Used with setState() and getState().
**/
public static final int STATE_MAXIMIZED = 0x1;
/**
* Used with setState() and getState().
**/
public static final int STATE_MINIMIZED = 0x2;
/**
* Used with setState() and getState().
**/
public static final int STATE_NORMAL = 0x0;
/**
* This may be returned by getState() if the window state cannot be determined.
**/
public static final int STATE_UNKNOWN = -1;
/**
* This is a parameter for Window.getGuiInfo(), use it with a ewe.sys.Long object as the
* destination parameters.
2**/
public static final int INFO_GUI_FLAGS = 9;
/**
* A flag returned from Window.getGuiInfo() with the GUI_FLAGS parameter. Indicates that
* the OS supports placing icons in a taskbar.
**/
public static final int GUI_FLAG_HAS_TASKBAR = 0x1;
/**
* A flag returned from Window.getGuiInfo() with the GUI_FLAGS parameter. Indicates that
* Cancel buttons should come before OK buttons.
**/
public static final int GUI_FLAG_REVERSE_OK_CANCEL = 0x2;
/**
* This is a parameter for Window.setInfo(), Window.getInfo() use it with a ewe.sys.Long object as the
* destination parameters.
**/
static final int INFO_TITLE = 10;
static final int INFO_DROPPED_DATA = 11;
static final int INFO_FONT = 12;

/**
* Used with Window.getInfo().
* This is used for retrieving a reference to the native window associated with the window.
* Do not a source or destination parameter and one of the NATIVE_WINDOW_GET_CONTAINING_WINDOW or
* NATIVE_WINDOW_GET_DRAWING_SURFACE as the option.
* <p>
* On a native Ewe VM the returned value will be a ewe.sys.Long() object that contains the
* a 32-bit pointer to a platform specific structure or value as defined in "eni.h". On a Java
* VM this will return a java.awt object representing the Window.
* <p>
* NATIVE_WINDOW_GET_CONTAINING_WINDOW will return a reference to the native Window that acts as
* the container for the Ewe window.<p>
* NATIVE_WINDOW_GET_DRAWING_SURFACE will return a reference to the native object that you can
* draw to in order to draw directly to the window surface. Under native VMs this is usually
* the same as the native Window itself, while under Java it is a java.awt object that may be
* different to the Window.
**/
public static final int INFO_NATIVE_WINDOW = 13;
/**
* This is an option for INFO_NATIVE_WINDOW.
**/
public static final int NATIVE_WINDOW_GET_CONTAINING_WINDOW = 0;
/**
* This is an option for INFO_NATIVE_WINDOW.
**/
public static final int NATIVE_WINDOW_GET_DRAWING_SURFACE = 1;
/**
* Used with Window.getInfo().
* This is used for retrieving a the x,y position of a particular Ewe Control within the native
* windows drawing surface. This will allow native code to draw dirctly on to that control.<p>
* Pass the Control as the source parameter and a null object as the destination parameter. A new
* ewe.fx.Point() object will be returned containing the x,y position, or null if the control is
* not in the Window.
*/
public static final int INFO_POSITION_IN_NATIVE_DRAWING_SURFACE = 14;
/**
* This is used for setInfo() only. Set the options to 1 to accept dropped files, 0 to reject dropped files.
**/
static final int INFO_ACCEPT_DROPPED_FILES = 11;
//##################################################################
}
//##################################################################

