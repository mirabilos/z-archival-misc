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
* Similar to the standard Java Iterator, it will return a set of Objects
* sequentially. When hasNext() returns false, there are no more Objects
* to retrieve.
**/
//##################################################################
public interface Iterator{
//##################################################################
/**
* Returns whether there are more Objects to retrieve.
**/
public boolean hasNext();
/**
* Returns the next Object in the sequence.
* @return the next Object in the sequence.
* @exception NoSuchElementException if there are no more elements left.
*/
public Object next() throws NoSuchElementException;
/**
* Returns the next Object in the sequence (optional operation). If this operation is not
supported an UnsupportedOperationException will be thrown (or a RuntimeException) on a Java 1.1
system.
* @exception UnsupportedOperationException if the implementation does not support this operaton
*/
public void remove();

//##################################################################
}
//##################################################################

