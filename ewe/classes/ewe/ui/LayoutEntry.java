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

	//##################################################################
	class LayoutEntry {
	//##################################################################

	public int row,column;
	public int width,height;
	public LayoutEntry topLeft;
	public Control control;
	//===================================================================
	public Dimension getPreferredSize(Dimension d)
	//===================================================================
	{
		return d.set(50,20);//control.getPreferredSize(d);
	}
	//===================================================================
	public LayoutEntry getSubEntry()
	//===================================================================
	{
		LayoutEntry le = new LayoutEntry();
		le.row = row; le.column = column;
		le.width = width; le.height = height;
		le.control = control;
		le.topLeft = this;
		return le;
	}
	//##################################################################
	}
	//##################################################################

