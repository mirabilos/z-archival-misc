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

//##################################################################
public class WindowCreationData{
//##################################################################
//The next 4 variables must come first.
private boolean doRegisterClass = false;
/**
* Under Win32 this must be a unique name for this class of window. Windows of the same class will share
* the same icon. If this is not set, the icon will not be changed.
**/
public String nativeWindowClassName = null;
/**
* This must be an icon created by ewe.fx.Image.toIcon(). Note that Java does not seem able to
* display the transparent areas of the icon correctly.
**/
public Object nativeWindowIcon = null;
/**
* Used by WindowContainer only.
**/
public Object parentWindow = null;
//-------------------------------------

static ewe.util.Hashtable classes = new ewe.util.Hashtable();

//-------------------------------------------------------------------
void setup()
//-------------------------------------------------------------------
{
	if (nativeWindowClassName == null) return;
	if (classes.get(nativeWindowClassName) != null) return;
	classes.put(nativeWindowClassName,new Object());
	doRegisterClass = true;
}


//##################################################################
}
//##################################################################

