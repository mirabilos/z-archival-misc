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

/**
* This is a special control which contains a native window within it. This only
* works for Win32, not Java. The native window will always be sized to fit the
* container exactly.
**/
//##################################################################
public class WindowContainer extends Control{
//##################################################################

private Window window;
{
	backGround = Color.White;
}
//===================================================================
public static boolean isSupported() {return true;}
//===================================================================

//===================================================================
protected Window createWindow()
//===================================================================
{
	return new Window();
}
//===================================================================
public Window getContainedWindow()
//===================================================================
{
	if (window == null){
		window = createWindow();
		if (window.creationData == null) window.creationData = new WindowCreationData();
		window.creationData.parentWindow = getWindow();
		Rect r = Gui.getAppRect(this);
		window.contents.backGround = Color.White;
		window.create(r,null,0,Window.FLAG_HAS_TITLE|Window.FLAG_CAN_RESIZE,getWindow());
	}
	return window;
}
//===================================================================
public Frame getContainedWindowFrame()
//===================================================================
{
	return Gui.windowFrame(getContainedWindow());
}
//===================================================================
public void resizeTo(int width,int height)
//===================================================================
{
	super.resizeTo(width,height);
	if (window != null){
		Rect r = Gui.getAppRect(this);
		window.setWindowRect(r,false);
	}
}
//##################################################################
}
//##################################################################

