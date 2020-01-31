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
import ewe.util.*;
import ewe.ui.*;
import ewe.data.*;
/**
* This class is used to display a list of items stored in a Database within a DataStorage
* object.
**/
//##################################################################
public abstract class DatabaseList extends SimpleList{
//##################################################################
/**
* These are the items which are to be displayed in the list.
**/
public FoundEntries items;
/**
* This is the DataStorage which contains the data stored in the DatabaseList.
**/
public DataStorage storage;
/**
* This is the Database which contains the items in the list.
**/
public Database table;

private static final String noItems = "You must set the \"items\" variable before using the DatabaseList";
//===================================================================
public int getItemCount()
//===================================================================
{
	if (items == null) throw new NullPointerException(noItems);
	return items.size();
}
//===================================================================
public Object getObjectAt(int idx)
//===================================================================
{
	if (items == null) throw new NullPointerException(noItems);
	if (idx > items.size()) return null;
	try{
		return items.get(idx);
	}catch(ewe.io.IOException e){
		return "IOError!";
	}
}
//===================================================================
public String getObjectName(Object obj)
//===================================================================
{
	if (obj == null) return "";
	else if (obj instanceof LiveData) return ((LiveData)obj).getName();
	else return obj.toString();
}
/**
* This must be overriden. It converts a DataEntryData object (which represents
* a single item in a Database within a DataStorage) to a string to be displayed
* in the list.
**/
//-------------------------------------------------------------------
protected abstract String makeDisplayItem(DataEntryData ded);
//-------------------------------------------------------------------

//===================================================================
public String getDisplayItem(int idx)
//===================================================================
{
	DataEntryData ded = (DataEntryData)getObjectAt(idx);
	if (ded == null) return "";
	return makeDisplayItem(ded);
}
/**
* Create the DatabaseList based on a DataStorage and a Database stored
* within the DataStorage. After creating it you must set the <b>items</b>
* variable to be a FoundEntries object read from the Database before
* the Database can be used.
**/
//===================================================================
public DatabaseList(DataStorage ds,Database dt)
//===================================================================
{
	this.storage = ds;
	this.table = dt;
}
//##################################################################
}
//##################################################################

