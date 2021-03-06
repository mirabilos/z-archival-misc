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
public class WindowEvent extends Event{
//##################################################################
/**
* This event is generated when either the close button for the window is pressed
* OR the system wants to close your application. It is not possible to distinguish
* between the two - however a Close message sent by the system will always be directed
* to the mApp for your application. For mobile applications it is best to have a separate Exit button
* or function to allow the user to explicitly close the application, and then assume that
* any CLOSE message would therefore come from the system. Note that for WinCE applications
* it is assumed that the application will save its state somehow, so that the next time it
* runs it will continue from where it left off.
**/
public static final int CLOSE = 50;
/**
* This indicates that the window is now active.
**/
public static final int ACTIVATE = 51;
/**
* This indicates that the window is no longer active.
**/
public static final int DEACTIVATE = 52;
/**
* This indicates that the system (WinCE) wishes a Window to minimize its memory footprint.
* This is usually called when you switch to a different application, AND may be called several
* times. It may be directed to any open window of your application.
**/
public static final int HIBERNATE = 53;
/**
* This is a request from the VM for the application to bring itself to the front - displaying whatever
* it considers to be the current active window.
**/
public static final int APP_TO_FRONT = 54;
/**
* This bit is used as a flag to mApp.closeMobileApp() if the system <b>knows</b> for sure that
* the CLOSE event was generated by the user pressing one of the system close buttons. This currently only
* applies to the 'OK' button on WinCE. It is not possible to tell the difference between a close generated
* by the 'X' button or by the system on WinCE.
**/
public static final int FLAG_CLOSE_BY_USER = 0x1;
/**
* This event occurs when the user drops data picked up from another application or from the OS shell.
* The data picked up is placed in the data member.
* That member will be either:<p><ul>
* <li>A String - representing a single dropped file.</li>
* <li>An Array of Strings - each representing a single dropped file.</li>
* <li>Some other type of application specific data.</li>
* </ul>
**/
public static final int DATA_DROPPED = 55;
/**
* In the case of a DATA_DROPPED event this will be
* the x-coordinate where the data was dropped.
**/
public int x;
/**
* In the case of a DATA_DROPPED event this will be
* the y-coordinate where the data was dropped.
**/
public int y;
/**
/**
* In the case of a DATA_DROPPED event
* this will hold data that has been dropped into the window.
* This will be either:<p><ul>
* <li>A String - representing a single dropped file.</li>
* <li>An Array of Strings - each representing a single dropped file.</li>
* <li>Some other type of application specific data.</li>
* </ul>
**/
public Object data;
//##################################################################
}
//##################################################################

