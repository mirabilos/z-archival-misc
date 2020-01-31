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
import ewe.util.Vector;
import ewe.util.EventDispatcher;

//##################################################################
public class SoftKeyEvent extends Event{
//##################################################################
public static final int ACTION = 350;

{
	type = ACTION;
}
/**
This is the bar that generated the event.
**/
public SoftKeyBar bar;
/**
This is the SoftKey that generated the event, starting from a value
of 1 for the left key.
**/
public int whichKey;
/**
This is the action for the button or for the selected menu item if available.
**/
public String action;
/**
If the event was caused by a menu item selection, then this will be that selected item.
**/
public MenuItem selectedItem;
/**
If the SoftKey button or menu item was a proxy for another control (usually a button)
then this will be set to be that Control.
**/
public Control proxy;
/**
If the SoftKeyEvent was generated by a button or menu item that is associated with
a proxy control, then call doAction() on that proxy and then return true. Otherwise
do nothing and return false.
*/
//===================================================================
public boolean fireProxyAction()
//===================================================================
{
	if (proxy == null) return false;
	proxy.notifyAction();
	return true;
}

//##################################################################
}
//##################################################################
