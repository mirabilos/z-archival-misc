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
* @deprecated.
**/
//##################################################################
public class mGraphics extends Graphics{
//##################################################################

protected int tx = 0, ty = 0;
//===================================================================
public mGraphics(ISurface is) {super(is);}
//===================================================================

//===================================================================
public void translate(int x,int y)
//===================================================================
{
	tx += x;
	ty += y;
	super.translate(x,y);
}
//===================================================================
public void reset()
//===================================================================
{
	super.reset();
	if (tx != 0 && ty != 0) translate(-tx,-ty);
	tx = ty = 0;
}

//===================================================================
public void setClip(int x,int y,int width,int height)
//===================================================================
{
	super.setClip(x,y,width,height);
	//empty = (width <= 0 || height <= 0);
}
//===================================================================
public void clearClip()
//===================================================================
{
	super.clearClip();
	//empty = false;
}
//##################################################################
}
//##################################################################

