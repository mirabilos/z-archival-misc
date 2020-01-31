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
import ewe.fx.Dimension;
import ewe.util.Iterator;
import ewe.util.ObjectIterator;
/**
* A SingleContainer is used to hold a single component which can be
* changed dynamically. It is difficult to change the contents of a
* CellPanel dynamically, but any elements which do need to be changed
* can have a SingleContainer placed in it, and the contents of the
* SingleContainer can change. Call setControl() to change the contents.
**/
//##################################################################
public class SingleContainer extends Container{
//##################################################################

public Control client;

//===================================================================
public void resizeTo(int width,int height)
//===================================================================
{
	super.resizeTo(width,height);
	if (client != null) client.setRect(borderWidth,borderWidth,width-borderWidth*2,height-borderWidth*2);
}
/**
* This changes the control. It does a make() on the control after it has
* been added. It will <b>not</b> repaint the control after - call repaintNow()
* for that.
* @param who The new control to display.
**/
//===================================================================
public void setControl(Control who)
//===================================================================
{
	if (client != null) remove(client);
	add(who);
	who.make(false);
	client = who;
	client.setRect(borderWidth,borderWidth,width-borderWidth*2,height-borderWidth*2);
}
/**
* This changes the control. It does a make() on the control after it has
* been added. It will <b>not</b> repaint the control after - call repaintNow()
* for that.
* @param who The new control to display.
* @param repaint if true a repaint will be done.
*/
//===================================================================
public void setControl(Control who,boolean repaint)
//===================================================================
{
	setControl(who);
	if (repaint) repaintNow();
}
//===================================================================
public Dimension getPreferredSize(Dimension ps)
//===================================================================
{
	if (client != null) {
		ps = client.getPreferredSize(ps);
		ps.width += borderWidth*2;
		ps.height += borderWidth*2;
		return ps;
	}
	if (ps == null) ps = new Dimension(0,0);
	return ps.set(borderWidth*2,borderWidth*2);
}
//===================================================================
public void make(boolean reMake)
//===================================================================
{
	if (client != null) client.make(reMake);
}

//===================================================================
public void dismantle(Control downTo)
//===================================================================
{
	if (this == downTo) return;
	super.dismantle(downTo);
	client = null;
}
//===================================================================
public void formClosing()
//===================================================================
{
	super.formClosing();
}
//===================================================================
public Iterator getSubControls()
//===================================================================
{
	return new ObjectIterator(client);
}

//##################################################################
}
//##################################################################

