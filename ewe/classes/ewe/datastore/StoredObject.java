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

//##################################################################
public abstract class StoredObject extends ewe.data.LiveTreeNode{
//##################################################################

DataStorage storage;
DataEntry entry;

//===================================================================
public DataStorage getStorage() {return storage;}
//===================================================================
public DataEntry getEntry() {return entry;}
//===================================================================
/**
* This writes the object to the DataStorage. If it is a new one and has
* no DataEntry associated with it, you must use DataEntry.saveObject() instead.
**/
//===================================================================
public void save(String name) throws ewe.io.IOException
//===================================================================
{
	if (entry == null) return;
	if (name == null) name = entry.getName();
	entry.saveObject(name,this);
}
/**
* This writes the object to the DataStorage. If it is a new one and has
* no DataEntry associated with it, you must use DataEntry.saveObject() instead.
* This saves the data using the old name if there is one.
**/
//===================================================================
public void save() throws ewe.io.IOException
//===================================================================
{
	if (entry != null)
		save(entry.getName());
}
//===================================================================
public ewe.sys.Locale getLocale()
//===================================================================
{
	if (storage == null) return ewe.sys.Vm.getLocale();
	return storage.locale;
}
//===================================================================
public void setLocale(ewe.sys.Locale locale)
//===================================================================
{
	if (storage != null) storage.locale = locale;
}
//##################################################################
}
//##################################################################

