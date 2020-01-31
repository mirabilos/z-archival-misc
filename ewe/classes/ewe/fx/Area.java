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
* An Area represents an arbitrarily shaped 2D area.
**/
//##################################################################
public interface Area{
//##################################################################
/**
* Check if the point is in the area.
**/
public boolean isIn(int x,int y);
/**
* See if this Area intersects another. This intersection is not necessarily an intersection
* of the bounding rectangles.
**/
public boolean intersects(Area other);
/**
 * Get the bounding rectangle of the area.
 * @param dest an optional destination Rect.
 * @return The dest Rect or a newly created Rect if dest is null.
 */
public Rect getRect(Rect dest);

//##################################################################
}
//##################################################################

