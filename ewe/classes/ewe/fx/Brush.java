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
package ewe.fx;
/**
* A Brush is used with certain draw operations in ewe.fx.Graphics. See the
* Graphics.setBrush(Brush b) method.
**/
//##################################################################
public class Brush{
//##################################################################
/**
* The color for the Brush.
**/
public Color color;
/**
* The style for the Brush.
**/
public int style;
/**
 * Create a new Brush Object.
 * @param color The color for the Brush.
 * @param style The style for the Brush. Currently only SOLID is supported.
 */
//===================================================================
public Brush(Color color,int style)
//===================================================================
{
	this.color = color;
	this.style = style;
	if (color == null) color = Color.Black;
	if (style == 0) style = SOLID;
}
/**
* A Brush style.
**/
public static final int SOLID = 1;
//##################################################################
}
//##################################################################

