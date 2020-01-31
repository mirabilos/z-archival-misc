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

//##################################################################
public class TableEvent extends ControlEvent{
//##################################################################
public static final int SELECTION_CHANGED = 50000;
public static final int CELL_CLICKED = 50001;
public static final int CELL_DOUBLE_CLICKED = 50002;
public static final int FLAG_SELECTED_BY_ARROWKEY = 1;
//==================================================================
public TableEvent(int type,Control target)
//==================================================================
{
	super(type,target);
}
/**
* For CELL_CLICKED, the row which was clicked.
**/
public int row;
/**
* For CELL_CLICKED, the cell which was clicked.
**/
public int col;
/**
* For CELL_CLICKED, the data associated with the cell that was clicked.
**/
public Object cellData;
//##################################################################
}
//##################################################################

