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
package ewe.util;
/**
* A Range represents a consecutive sequence of indexes.
**/
//##################################################################
public class Range implements Copyable{
//##################################################################
/**
* This is the first index in the range.
**/
public int first;
/**
* This is the last index in the range (inclusive).
**/
public int last;
public Range(int first,int last){set(first,last);}
//===================================================================
public Range set(int first,int last) {this.first = first; this.last = last; return this;}
//===================================================================
public Range set(Range r) {return set(r.first,r.last);}
//===================================================================
public boolean equals(Object other)
//===================================================================
{
	if (!(other instanceof Range)) return false;
	Range p = (Range)other;
	return p.first == first && p.last == last;
}
//===================================================================
public String toString() {return "("+first+"->"+last+")";}
//===================================================================
public Object getCopy()
//===================================================================
{
	return new Range(first,last);
}
//##################################################################
}
//##################################################################

