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
package ewe.ui;

//##################################################################
public class DataChangeEvent extends Event{
//##################################################################

public static final int DATA_CHANGED = 1000;
/**
* A field name which caused the data change.
**/
public String fieldName;
/**
* This can be used to point to an object which gives an indication as to the cause of the change.
* The Editor object when generating a data change will set this to be the DataChangeEvent that it
* received from one of its fields.
**/
public Object cause;

public DataChangeEvent(int type,Control target)
	{
	this.type = type;
	this.target = target;
	timeStamp = ewe.sys.Vm.getTimeStamp();
	}
//##################################################################
}
//##################################################################

