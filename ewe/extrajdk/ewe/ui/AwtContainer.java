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

//##################################################################
public class AwtContainer extends Control{
//##################################################################

java.awt.Component component;

//===================================================================
public AwtContainer(Object awtControl)
//===================================================================
{
	component = (java.awt.Component)awtControl;
}

//-------------------------------------------------------------------
protected void calculateSizes()
//-------------------------------------------------------------------
{
	java.awt.Dimension d = component.getPreferredSize();
	preferredHeight = d.height;
	preferredWidth = d.width;
	super.calculateSizes();
}

//===================================================================
public void make(boolean reMake)
//===================================================================
{
	ewe.applet.WinCanvas.place(getWindow(),component,null);
}

//===================================================================
public void setRect(int xx,int yy,int wwidth,int hheight)
//===================================================================
{
	super.setRect(xx,yy,wwidth,hheight);
	ewe.applet.WinCanvas.place(getWindow(),component,Gui.getAppRect(this));
}
//##################################################################
}
//##################################################################
