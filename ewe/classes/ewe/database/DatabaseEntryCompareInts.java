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
package ewe.database;
import ewe.util.CompareInts;
import ewe.io.IOException;
import ewe.sys.Vm;

//##################################################################
public class DatabaseEntryCompareInts implements CompareInts{
//##################################################################
private int[] criteria;
private Database database;
private DatabaseEntry entryOne, entryTwo;
private int lastOne = 0, lastTwo = 0;
//===================================================================
public DatabaseEntryCompareInts(Database database,int[] criteria)
//===================================================================
{
	this.criteria = criteria;
	this.database = database;
	entryOne = database.getNewData();
	entryTwo = database.getNewData();
}
//===================================================================
public int compare(int one, int two)
//===================================================================
{
	if (one == two) return 0;
	if (one <= 0) return -1;
	if (two <= 0) return 1;
	return 0;
	/*
	try{
		if (lastOne != one)
			database.load(lastOne = one,entryOne);
		if (lastTwo != two)
			database.load(lastTwo = two,entryTwo);
	}catch(IOException e){
		throw new DatabaseIOException(e);
	}
	return entryOne.compareTo(entryTwo,criteria);
	*/
}

//##################################################################
}
//##################################################################


