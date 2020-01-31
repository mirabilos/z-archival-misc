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
/**
* This is a CheckBox which is displayed as a standard push button.
**/
//##################################################################
public class ButtonCheckBox extends mCheckBox {
//##################################################################
{
	borderWidth = 1;
	modify(0,PreferredSizeOnly);
	borderStyle &= ~BDR_NOBORDER;
	//defaultAddMeCellConstraints = defaultAddMeControlConstraints = 0;
}
//==================================================================
protected void calculateSizes()
//==================================================================
{
	ButtonObject.obj.update(this);
	Dimension d = ButtonObject.obj.calculateSize(new Dimension());
	preferredWidth = d.width; preferredHeight = d.height;
}
//==================================================================
public void doPaint(Graphics g,Rect area)
//==================================================================
{
	int flags = getModifiers(true);
	if (!((flags & Invisible) == 0)) return;
	ButtonObject.obj.update(this);
	ButtonObject.obj.pressed = ButtonObject.obj.pressed || getState();
	ButtonObject.obj.paint(g);
}
//==================================================================
protected void doPaintData(Graphics g)
//==================================================================
{
	doPaint(g,getDim(null));
}
//==================================================================
public ButtonCheckBox(){super();}
public ButtonCheckBox(String txt) {super(txt);}
public ButtonCheckBox(IImage img) {this(""); image = img;}
//==================================================================


//##################################################################
}
//##################################################################

