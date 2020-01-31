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
* A Pen is used with certain draw operations in ewe.fx.Graphics. See the
* Graphics.setPen(Pen p) method.
**/
//##################################################################
public class Pen{
//##################################################################
public Color color;
public int style;
public int thickness;
public float miterLimit;

/**
 * Create a new Pen object.
 * @param color The color for the Pen object.
 * @param style The style for the Pen object. This should be one of the constants SOLID, DASH, DOT, DASHDOT, DASHDOTDOT or NULL, optionally ORed with
 * one of the CAP_XXX values and JOIN_XXX values.
 * @param thickness The thickness of the Pen (in pixels).
 * @param miterLimit A miterlimit for end joins.
 */
//===================================================================
public Pen(Color color,int style,int thickness,float miterLimit)
//===================================================================
{this.color = color; this.style = style; this.thickness = thickness; this.miterLimit = miterLimit;}
/**
 * Create a new Pen object.
 * @param color The color for the Pen object.
 * @param style The style for the Pen object. This should be one of the constants SOLID, DASH, DOT, DASHDOT, DASHDOTDOT or NULL, optionally ORed with
 * one of the CAP_XXX values and JOIN_XXX values.
 * @param thickness The thickness of the Pen (in pixels).
 */
//===================================================================
public Pen(Color color,int style,int thickness)
//===================================================================
{this(color,style,thickness,0);}


public static final int SOLID           = 0;
public static final int DASH            = 1;       /* -------  */
public static final int DOT             = 2;       /* .......  */
public static final int DASHDOT         = 3;       /* _._._._  */
public static final int DASHDOTDOT      = 4;       /* _.._.._  */
public static final int NULL 						= 5;

public static final int CAP_BUTT = 0x100;
public static final int CAP_SQUARE = 0x200;
public static final int CAP_ROUND = 0x300;

public static final int JOIN_BEVEL = 0x1000;
public static final int JOIN_MITER = 0x2000;
public static final int JOIN_ROUND = 0x3000;
//##################################################################
}
//##################################################################

