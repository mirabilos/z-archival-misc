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
import ewe.sys.Vm;

//##################################################################
public class VerticalScrollPanel extends ScrollBarPanel{
//##################################################################

//-------------------------------------------------------------------
private final static int indicatorOnly()
//-------------------------------------------------------------------
{
	return (Vm.getParameter(Vm.VM_FLAGS) & Vm.VM_FLAG_NO_PEN) == 0 ? 0 : OPTION_INDICATOR_ONLY;
}
//===================================================================
public VerticalScrollPanel(ScrollClient client, boolean permanentScroller)
//===================================================================
{
	super(client,indicatorOnly() | (permanentScroller ? AlwaysShowVerticalScrollers : 0));
	setClientConstraints(VEXPAND|HEXPAND|HCONTRACT);
}

//===================================================================
public void setClientConstraints(int constraints)
//===================================================================
{
	constraints |= HEXPAND|HCONTRACT;
	super.setClientConstraints(constraints);
}
//===================================================================
public VerticalScrollPanel(ScrollClient client)
//===================================================================
{
	this(client,false);
}
//##################################################################
}
//##################################################################

