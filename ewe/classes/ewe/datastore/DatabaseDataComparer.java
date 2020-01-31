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
package ewe.datastore;

/**
* This class can be used when searching for records in a FoundEntries. It assumes that you
* are using a data object for data transfer to and from the Database. The method you must implement
* is the compareData() method. For each checked record this will be passed the searchData which you provide (which
* is itself usually an instance of the same data object) and the record data placed in another
* data object.
**/
//##################################################################
public abstract class DatabaseDataComparer implements ewe.util.Comparer, ewe.util.ObjectFinder{
//##################################################################

Database database;
Object data;
//===================================================================
public DatabaseDataComparer(FoundEntries fe,Object dataObject)
//===================================================================
{
	database = fe.getDatabase();
	data = dataObject;
}
//===================================================================
public DatabaseDataComparer(FoundEntries fe)
//===================================================================
{
	this(fe,null);
}
//===================================================================
public final int compare(Object one,Object two)
//===================================================================
{
	data = database.getData((DatabaseEntry)two,data);
	return compareData(one,data);
}
/**
 * You must override this method OR override the other compareData() that takes one argument if
 * you will be using a null searchData in the for the findXXX() method.
 * @param searchData This is the searchData that you supplied to the findXXX() method of
	FoundEntries.
 * @param databaseRecordData This is the data stored at a particular record that has been
	read into an instance of a data object being used by the Database.
 * @return if the searchData is considered greater than the database record data it should
	return a value greater than 0, if it is less it should return a value less than 0, otherwise
	it should return 0 to indicate equality.
 */
//-------------------------------------------------------------------
protected int compareData(Object searchData,Object databaseRecordData)
//-------------------------------------------------------------------
{
	if (searchData == null) return compareData(databaseRecordData);
	else throw new IllegalStateException("You must override the compareData() method.");
}
/**
 * You must override this OR override the other compareData() that takes one argument.
 * @param searchData This is the searchData that you supplied to the findXXX() method of
	FoundEntries.
 * @param databaseRecordData This is the data stored at a particular record that has been
	read into an instance of a data object being used by the Database.
 * @return if the searchData is considered greater than the database record data it should
	return a value greater than 0, if it is less it should return a value less than 0, otherwise
	it should return 0 to indicate equality.
 */
//-------------------------------------------------------------------
protected int compareData(Object databaseRecordData)
//-------------------------------------------------------------------
{
	throw new IllegalStateException("You must override the compareData() method.");
}

//===================================================================
public boolean lookingFor(Object lookData)
//===================================================================
{
	data = database.getData((DatabaseEntry)lookData,data);
	return compareData(data) == 0;
}
//##################################################################
}
//##################################################################

