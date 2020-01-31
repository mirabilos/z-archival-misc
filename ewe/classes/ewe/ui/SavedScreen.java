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

//##################################################################
public class SavedScreen{
//##################################################################

public Rect savedArea = new Rect(0,0,0,0);
public Window window;
/**
* Should create a SavedScreen by calling mGraphics.saveScreen(Rect area);
*
*/
//==================================================================
SavedScreen(Window w,Rect area,boolean doCapture)
//==================================================================
{
	savedArea.set(area);
	window = w;
	if (doCapture) capture();
}

Image image;
boolean actuallyCaptured = false;

//==================================================================
boolean capture()
//==================================================================
{
	if (image != null) image.free();
	image = new Image(savedArea.width,savedArea.height);
	Graphics g = Graphics.createNew(image);
	g.copyRect(window,savedArea.x,savedArea.y,savedArea.width,savedArea.height,0,0);
	g.free();
	actuallyCaptured = true;
	return true;
}
//==================================================================
public boolean restore()
//==================================================================
{
	if (actuallyCaptured && (image != null)) {
		Graphics g = Graphics.createNew(window);
		g.drawImage(image,savedArea.x,savedArea.y);
		g.free();
		image.free();
		image = null;
		return true;
	}else{
		Gui.refreshScreen(window,savedArea);
		return true;
	}
}
//==================================================================
public void free()
//==================================================================
{
	if (image != null) image.free();
	image = null;
	actuallyCaptured = false;
}
//##################################################################
}
//##################################################################

