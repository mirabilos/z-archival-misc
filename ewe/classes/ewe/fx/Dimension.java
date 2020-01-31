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
public class Dimension implements ewe.util.Encodable{
//##################################################################
public int width, height; // Do not move these two!
public Dimension(){}
public Dimension(int w,int h) {width = w; height = h;}
public String toString() {return "("+width+","+height+")";}
//===================================================================
public Dimension set(int w,int h) {width = w; height = h; return this;}
public Dimension set(Dimension r) {width = r.width; height = r.height; return this;}
public Dimension set(Rect r) {width = r.width; height = r.height; return this;}
public static Dimension unNull(Dimension r) {return r == null ? new Dimension():r;}
//===================================================================
public static Dimension buff = new Dimension();
public boolean equals(Object other)
{
	if (!(other instanceof Dimension)) return super.equals(other);
	Dimension r = (Dimension)other;
	return (r.width == width && r.height == height);
}
//##################################################################
}
//##################################################################

