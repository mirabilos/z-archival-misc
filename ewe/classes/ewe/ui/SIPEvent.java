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
public class SIPEvent extends Event{
//##################################################################
/** The event type for when the SIP is shown. */
public static final int SIP_SHOWN = 900;
/** The event type for when the SIP is hidden. */
public static final int SIP_HIDDEN = 901;

/**
* The width of the area of the screen still visible after the SIP is shown. It
* is assumed that the SIP is at the bottom of the screen.
**/
public int visibleWidth;
/**
* The height of the area of the screen still visible after the SIP is shown. It
* is assumed that the SIP is at the bottom of the screen.
**/
public int visibleHeight;
/**
* This is the visible desktop (i.e. the visibleHeight - taskbarHeight) if the taskbar is on top.
**/
public int desktopHeight;

public static boolean handlingSipOn;
//##################################################################
}
//##################################################################

