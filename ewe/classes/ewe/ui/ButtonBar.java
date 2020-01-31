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
/**
* A ButtonBar is a simple CellPanel which allows you to easily add
* buttons which will be displayed with equal widths.
**/
//##################################################################
public class ButtonBar extends CellPanel{
//##################################################################
{
	equalWidths = true;
	//defaultAddMeCellConstraints = HSTRETCH;
}
//==================================================================
public ButtonBar(){}
//==================================================================
//==================================================================
public mButton add(String name)
//==================================================================
{
	mButton b = new mButton(name);
	addNext(b);
	return b;
}
//==================================================================
public mButton [] add(String [] names)
//==================================================================
{
	int num = names.length;
	mButton [] b = new mButton[num];
	for (int i = 0; i<num; i++)
		b[i] = add(names[i]);
	return b;
}
//##################################################################
}
//##################################################################

