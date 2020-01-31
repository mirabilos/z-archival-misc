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
public interface CellConstants{
//##################################################################
//..................................................................
// Alignment and stretch positions. How an object is aligned in the cell.
//..................................................................
public static final int CELLFLAG = 0x10000000;
public static final int LEFT = 0x1, RIGHT = 0x2, TOP = 0x4, BOTTOM = 0x8;
public static final int HEXPAND = 0x10, HCONTRACT = 0x20, VEXPAND = 0x40, VCONTRACT = 0x80;
public static final int HGROW = 0x100, HSHRINK = 0x200, VGROW = 0x400, VSHRINK = 0x800;
public static final int CELLMASK = 0xef00, CONTROLMASK = 0xff;
public static final int DONTCHANGE = 0x1000;
public static final int INITIALLY_CLOSED = 0x2000;
public static final int INITIALLY_MINIMIZED = 0x4000;
public static final int INITIALLY_PREFERRED_SIZE = 0x8000;
//..................................................................
public static final int
	HSTRETCH = HGROW|HSHRINK, VSTRETCH = VGROW|VSHRINK, GROW = HGROW|VGROW, SHRINK = VSHRINK|HSHRINK, STRETCH = HSTRETCH|VSTRETCH;
public static final int
	HFILL = HEXPAND|HCONTRACT, VFILL = VEXPAND|VCONTRACT, FILL = HFILL|VFILL;
public static final int DONTSTRETCH = CELLFLAG;
public static final int DONTFILL = CELLFLAG;
public static final int HCENTER = CELLFLAG,VCENTER = CELLFLAG, CENTER = CELLFLAG;
//..................................................................
public static final int NORTH = TOP;
public static final int NORTHWEST = TOP|LEFT;
public static final int NORTHEAST = TOP|RIGHT;
public static final int SOUTH = BOTTOM;
public static final int SOUTHWEST = BOTTOM|LEFT;
public static final int SOUTHEAST = BOTTOM|RIGHT;
public static final int WEST = LEFT;
public static final int EAST = RIGHT;
//......................................................
// Used in the set(int,Object) method of Constraint
//......................................................
/** A Tag type for use in Control.setTag() - use a ewe.fx.Dimension object as the "value" object. **/
public static final int PREFERREDSIZE = 1;
/** A Tag type for use in Control.setTag() - use a ewe.fx.Dimension object as the "value" object. **/
public static final int  MINIMUMSIZE = 2;
/** A Tag type for use in Control.setTag() - use a ewe.fx.Dimension object as the "value" object. **/
public static final int MAXIMUMSIZE = 3;
/** A Tag type for use in Control.setTag() - use a ewe.fx.Insets object as the "value" object. **/
public static final int INSETS = 4;
/** @deprecated **/
public static final int BORDER = 5;
/** A Tag type for use in Control.setTag() - use a ewe.fx.Dimension object as the "value" object. **/
public static final int SPAN = 6;
/** A Tag type for use in Control.setTag() - use a ewe.fx.Dimension object as the "value" object. **/
public static final int FIXEDSIZE = 7;
/** A Tag type for use in Control.setTag() - use a ewe.fx.Dimension object as the "value" object. **/
public static final int TEXTSIZE = 8;
/** A Tag type for use in Control.setTag(), used to set the absolute location of a Control
within a Panel - use a ewe.fx.Rect object as the "value" object. **/
public static final int RECT = 9;

//public static final int
//public static final int
//public static final int
//public static final int
//public static final int
//public static final int
//public static final int
//public static final int
//public static final int
//public static final int
//public static final int
//public static final int
//public static final int
//public static final int
//public static final int
//public static final int

//##################################################################
}
//##################################################################

