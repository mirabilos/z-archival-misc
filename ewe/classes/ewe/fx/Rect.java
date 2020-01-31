/*********************************************************************************
 *  Ewe Virtual Machine - Version 1.14, January 24, 2002                         *
 *  Copyright (C) 1999-2002 Michael L Brereton <michael_brereton@ewesoft.com>    *
 *  Copyright (C) 1998, 1999 Wabasoft <www.wabasoft.com>                         *
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
 * Rect is a rectangle.
 */

public class Rect implements Cloneable, Area, ewe.util.Encodable
{
/** x position */
public int x;
/** y position */
public int y;
/** rectangle width */
public int width;
/** rectangle height */
public int height;

/** Constructs a rectangle with the given x, y, width and height. */
public Rect(int x, int y, int width, int height)
	{
	this.x = x;
	this.y = y;
	this.width = width;
	this.height = height;
	}

//MLB July-2000
//===================================================================
public Rect(){this(0,0,0,0);}
public Rect(Dimension d){this(0,0,d.width,d.height);}
//===================================================================
//===================================================================
public Rect set(Rect r) {return set(r.x,r.y,r.width,r.height);}
public Rect set(int x,int y,int w,int h) {this.x = x; this.y = y; this.width = w; this.height = h; return this;}
public static Rect unNull(Rect r) {return r == null ? new Rect(0,0,0,0):r;}
//===================================================================
public String toString(){return "("+x+","+y+","+width+","+height+")";}
//===================================================================
private static Rect rect = new Rect(0,0,0,0);
public static Rect buff = new Rect(0,0,0,0);
//==================================================================
public  boolean intersects(Rect r)
//==================================================================
{
	if (width < 0 || height < 0 || r.width < 0 || r.height < 0) return false;
	int left = x, right = x+width-1, top = y, bottom = y+height-1;
	int tleft = r.x, tright = r.x+r.width-1, ttop = r.y, tbottom = r.y+r.height-1;

	if (right < tleft) return false;
	if (left > tright) return false;
	if (bottom < ttop) return false;
	if (top > tbottom) return false;
	return true;
/*
	getIntersection(r,rect);
	return !(rect.width == 0 || rect.height == 0);
*/
}
//===================================================================
public void unionWith(Rect other)
//===================================================================
{
	int xx = other.x, yy = other.y, ww = other.width, hh = other.height;
	if (xx < x) {
		width += x-xx;
		x = xx;
	}
	if (xx+ww > x+width) width += xx+ww-(x+width);
	if (yy < y) {
		height += y-yy;
		y = yy;
	}
	if (yy+hh > y+height) height += yy+hh-(y+height);
}

/**
 * Get the intersection between this Rect and another Rect.
 * @param r The other Rect.
 * @param dest The destination for the intersection Rect values. This <b>can</b> be
 * either this Rect or the "r" Rect or it can be null.
 * @return The dest rect or a new Rect if dest is null.
 */
//==================================================================
public Rect getIntersection(Rect r,Rect dest)
//==================================================================
{
	dest = unNull(dest);
	int x = 0,y = 0,width = 0,height = 0;
	Rect r1 = this, r2 = r;
	Rect left = r1,right = r2;
	if (r2.x < r1.x) {
		left = r2;
		right = r1;
	}
	x = right.x;
	width = left.x+left.width-right.x;
	if (width > 0){
		if (width > right.width) width = right.width;
		Rect above = r1,below = r2;
		if (r2.y < r1.y){
			above = r2;
			below = r1;
		}
		y = below.y;
		height = above.y+above.height-below.y;
		if (height > 0) {
			if (height > below.height) height = below.height;
			return dest.set(x,y,width,height);
		}
	}
	return dest.set(0,0,0,0);
}
//==================================================================
public Rect getAddition(Rect r2,Rect dest)
//==================================================================
{
	Rect r1 = this;
	dest = unNull(dest);
	int sx = r1.x;
	if (r2.x < sx) sx = r2.x;
	int sy = r1.y;
	if (r2.y < sy) sy = r2.y;
	int ex = r1.x+r1.width;
	if (r2.x+r2.width > ex) ex = r2.x+r2.width;
	int ey = r1.y+r1.height;
	if (r2.y+r2.height > ey) ey = r2.y+r2.height;
	dest.x = sx; dest.y = sy; dest.width = ex-sx; dest.height=ey-sy;
	return dest;
}
//==================================================================
public boolean isInside(int x,int y)
//==================================================================
{
	Rect rect = this;
	if (x < rect.x || x >= rect.x+rect.width) return false;
	if (y < rect.y || y >= rect.y+rect.height) return false;
	return true;
}
public boolean isIn(int x,int y) {return isInside(x,y);}
public boolean intersects(Area other) {if (other instanceof Rect) return intersects((Rect)other); else return other.intersects((Area)this);}
public Rect getRect(Rect dest)
{
	return unNull(dest).set(this);
}
public boolean equals(Object other)
{
	if (!(other instanceof Rect)) return super.equals(other);
	Rect r = (Rect)other;
	return (r.x == x && r.y == y && r.width == width && r.height == height);
}

public Rect setCorners(int x1,int y1,int x2,int y2)
{
	int t;
	if (x1 > x2) {t = x1; x1 = x2; x2 = t;}
	if (y1 > y2) {t = y1; y1 = y2; y2 = t;}
	x = x1; y = y1;
	width = x2-x1+1; height = y2-y1+1;
	return this;
}

}


