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
public class PullDownMenu extends mButton{
//##################################################################
{
	modify(DrawFlat|MakeMenuAtLeastAsWide|NoFocus,0);
	setBorder(BDR_NOBORDER,0);
}

//==================================================================
public PullDownMenu(String title,Menu m) {m.text = text = title; setMenu(m);}
//==================================================================

//===================================================================
public void doPenPress(Point p)
//===================================================================
{
	if (startDropMenu(p))
		transferPenPress(getMenu(),0,0);
}

//===================================================================
public void doAction(int how) {startDropMenu(new Point(1,1));}
//===================================================================

//==================================================================
public void __doPaint(Graphics g,Rect area)
//==================================================================
{
	//update(ButtonObject.obj);
	//if (menuFrame != null) ButtonObject.obj.pressed = true;
	ButtonObject.obj.paint(g);
}
//===================================================================
public boolean willShowFrame(PenEvent ev)
//===================================================================
{
	if ((ev.type == ev.PEN_DOWN) && !menuIsActive()) return true;
	return super.willShowFrame(ev);
}
//##################################################################
}
//##################################################################


