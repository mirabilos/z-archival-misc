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
//MLB July-2000
//##################################################################
public class Point implements ewe.util.Encodable{
//##################################################################
//Dont move these variables.
public int x,y;
//--------------------------
public Point() {this(0,0);}
public Point(int xx,int yy) {x = xx; y = yy;}
public void move(int toX,int toY) {x = toX; y = toY;}
public void translate(int dx,int dy) {x += dx; y += dy;}
public String toString() {return "("+x+","+y+")";}
//===================================================================
public Point set(int x,int y) {this.x = x; this.y = y; return this;}
public Point set(Point r) {return set(r.x,r.y);}
public Point set(Rect r) {return set(r.x,r.y);}
public static Point unNull(Point r) {return r == null ? new Point():r;}
//===================================================================
public boolean equals(Object other)
//===================================================================
{
	if (!(other instanceof Point)) return false;
	Point p = (Point)other;
	return p.x == x && p.y == y;
}
//===================================================================

//##################################################################
}
//##################################################################

