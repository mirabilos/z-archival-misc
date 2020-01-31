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
package ewe.fx.print;
import ewe.fx.Rect;

//##################################################################
public class PointRect{
//##################################################################

public double x;
public double y;
public double width;
public double height;

//-------------------------------------------------------------------
private static int roundUp(double value)
//-------------------------------------------------------------------
{
	int v = (int)value;
	if ((double)v != value) v++;
	return v;
}
//===================================================================
public Rect scaleToRect(Rect destination, double pointsToPixelsX, double pointsToPixelsY)
//===================================================================
{
	if (destination == null) destination = new Rect();
	destination.x = (int)(x*pointsToPixelsX);
	destination.y = (int)(y*pointsToPixelsY);
	destination.width = width == 0 ? 0 : roundUp(width*pointsToPixelsX);
	destination.height = height == 0 ? 0 : roundUp(height*pointsToPixelsY);
	return destination;
}
/**
Sets the dimensions of this PointRect and returns itself.
**/
//===================================================================
public PointRect set(double x, double y, double width, double height)
//===================================================================
{
	this.x = x; this.y = y; this.width = width; this.height = height;
	return this;
}
/**
Sets the x and y of this PointRect, the others are set to zero and returns itself.
**/
//===================================================================
public PointRect set(double x, double y)
//===================================================================
{
	this.x = x; this.y = y; this.width = 0; this.height = 0;
	return this;
}
public PointRect() {}
public PointRect(double x, double y) {this.x = x; this.y = y;}
public PointRect(double x, double y,double width, double height) {this.x = x; this.y = y; this.width = width; this.height = height;}
/**
Returns true if the specified rectangle in Points intersects with this PageRect.
* @param x the x co-ordinate in Points (1/72 of an inch).
* @param y the y co-ordinate in Points (1/72 of an inch).
* @param width the width in Points (1/72 of an inch).
* @param height the height in Points (1/72 of an inch).
* @return true if the specified rectangle in Points intersects with this PageRect.
*/
//===================================================================
public boolean isWithin(double x, double y, double width, double height)
//===================================================================
{
	double left = this.x, right = this.x+this.width, top = this.y, bottom = this.y+this.height;
	double tleft = x, tright = x+width, ttop = y, tbottom = y+height;

	if (right < tleft) return false;
	if (left > tright) return false;
	if (bottom < ttop) return false;
	if (top > tbottom) return false;
	return true;
}
public String toString(){return "("+x+","+y+","+width+","+height+")";}

//##################################################################
}
//##################################################################

