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
public class TableCellAttributes implements UIConstants{
//##################################################################

public int row;
public int col;
public boolean isSelected;
public int borderStyle = EDGE_SUNKEN;//BDR_OUTLINE|BF_RECT;
public Color borderColor = Color.Black;
public Color fillColor = Color.White;
public Color foreground = Color.Black;
public boolean clipCellData = false;
/**
* Setting hSpan to >1 will allow the data to spill over to the next cells.
* However you must set the span for the following cells to zero.
* Setting it to -1 will allow it to take up the entire remainder of the row.
**/
public int hSpan = 1;
/**
* This is not used yet.
**/
public int vSpan = 1;
/**
* text may be a single String OR an array of Strings (one for each line).
**/
public Object text = "Cell";
/**
* If this is true, the TableModel will arrange the text to fit within the
* bounds of the cell. THIS IS NOT IMPLEMENTED!
**/
public boolean tryFitText = false;
/**
* This is alternate non-textual data to display.
**/
public Object data = null;

public int alignment = CENTER;
public int anchor = CENTER;
public boolean flat = false;
public FontMetrics fontMetrics = null;
public int drawImageOptions = 0;
//##################################################################
}
//##################################################################

