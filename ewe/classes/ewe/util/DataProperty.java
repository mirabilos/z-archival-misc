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
* This class is not currently used.
**/
//##################################################################
public class DataProperty{
//##################################################################
/**
* This is the type of the property.
**/
public int type;
/**
* This is the id of the property.
**/
public int id;
/**
* This will be set the appropriate value if the value is an integer type
* or a boolean type (0 = false, non-zero = true).
**/
public int value;
/**
* This will be set to the appropriate value if the value is an 8-byte
* value.
**/
public int highValue;
/**
* This will be set to the appropriate value if it is a String or byte array.
**/
public Object data;
/**
* If the data is a byte array this marks the start of the data for this
* property in the array. See arrayLength member.
**/
public int arrayStart;
/**
* If the data is a byte array this marks the length of the data for this
* property in the array. See arrayStart member.
**/
public int arrayLength;

//##################################################################
}
//##################################################################

