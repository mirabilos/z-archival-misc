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
import ewe.util.Comparer;

//##################################################################
public abstract class EntryComparer implements DatabaseEntryComparer{
//##################################################################
private DatabaseEntry entry1, entry2;
private Object data1,data2;
protected Database db;
/**
* If this is set true, then when compareEntries() is called the two objects
* will be of type DatabaseEntries. If it is false then the entries will be converted
* to the Database's data object. This is false by default.
**/
protected boolean compareAsDatabaseEntries = false;

//===================================================================
public final int compare(Object one,Object two)
//===================================================================
{
	int ret = 0;
	if (compareAsDatabaseEntries){
		if (!(one instanceof DatabaseEntry) || !(two instanceof DatabaseEntry))
			if (entry1 == null) {
				entry1 = db.getNewData();
				entry2 = db.getNewData();
			}
		DatabaseEntry o = ((one == null) || (one instanceof DatabaseEntry)) ? (DatabaseEntry)one : entry1;
		if (o == entry1) entry1.setData(one);
		DatabaseEntry t = ((two == null) || (two instanceof DatabaseEntry)) ? (DatabaseEntry)two : entry2;
		if (t == entry2) entry2.setData(two);
		return compareEntries(o,t);
	}else{
		if ((one instanceof DatabaseEntry) || (two instanceof DatabaseEntry))
			if (data1 == null) {
				data1 = db.getNewDataObject();
				data2 = db.getNewDataObject();
			}
		Object o = (one instanceof DatabaseEntry) ? data1 : one;
		if (o == data1) ((DatabaseEntry)one).getData(data1);
		Object t = (two instanceof DatabaseEntry) ? data2 : two;
		if (t == data2) ((DatabaseEntry)two).getData(data2);
		return compareEntries(o,t);
	}
}
/**
* Override this to compare to entries. If you are doing a findFirst() or findLast() or
* a search(), then the first argument will always be the searchData that you provided.
**/
//-------------------------------------------------------------------
protected abstract int compareEntries(Object one, Object two);
//-------------------------------------------------------------------

//-------------------------------------------------------------------
protected EntryComparer(){}
//-------------------------------------------------------------------
//===================================================================
public void setDatabase(Database db)
//===================================================================
{
	this.db = db;
}
//===================================================================
public EntryComparer(Database db) throws IllegalArgumentException
//===================================================================
{
	setDatabase(db);
}

//##################################################################
}
//##################################################################

