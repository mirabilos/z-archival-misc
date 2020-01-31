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

//##################################################################
public class Insets implements ewe.util.Encodable{
//##################################################################
public int top, bottom, left, right;
//===================================================================
public Insets(int t,int l,int b,int r)
//===================================================================
{
	top = t;
	bottom = b;
	left = l;
	right = r;
}
/**
* Apply the specified Insets to the Rect r. If the Insets is null nothing is done.
**/
//===================================================================
public static void apply(Insets in,Rect r)
//===================================================================
{
	if (in == null) return;
	r.x += in.left; r.y += in.top;
	r.width -= (in.left+in.right);
	r.height -= (in.top+in.bottom);
}
/**
* Apply this Insets to the Rect r.
**/
//===================================================================
public void apply(Rect r) {apply(this,r);}
//===================================================================

//##################################################################
}
//##################################################################

