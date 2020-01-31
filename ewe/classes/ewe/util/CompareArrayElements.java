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
import ewe.io.*;
/**
* This is an implementation of CompareInts which compares Objects in
* an array. CompareInts objects are used for the Utils.sort() methods.
**/
//##################################################################
public class CompareArrayElements implements CompareInts{
//##################################################################
Object [] items;
Comparer comparer;
/**
* Creates a new CompareArrayElements which uses the specified Comparer to
* compare objects in the specified array.
* @param items the items to compare.
* @param comparer a Comparer to compare the objects in the array. If this is null
* then it is assumed that the objects implement the Comparable interface.
* @return
*/
//===================================================================
public CompareArrayElements(Object[] items,Comparer comparer)
//===================================================================
{
	this.items = items;
	this.comparer = comparer;
}
/**
* Compare the two integer values provided by treating them as indexes
* into the array and then using the Comparer to compare the Objects
* found at those indexes.
**/
//===================================================================
public int compare(int one,int two)
//===================================================================
{
	if (comparer != null) return comparer.compare(items[one],items[two]);
	else if (items[one] != null)
		return ((Comparable)items[one]).compareTo(items[two]);
	else if (items[two] != null)
		return -((Comparable)items[two]).compareTo(items[one]);
	else
		return 0;
}
//##################################################################
}
//##################################################################

