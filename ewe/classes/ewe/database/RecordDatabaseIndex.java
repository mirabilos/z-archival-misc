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
import ewe.sys.Handle;
import ewe.sys.Vm;
import ewe.util.IntArray;
/*
import ewe.sys.Lock;
import ewe.util.CompareInts;
import ewe.util.Utils;
*/
import ewe.data.PropertyList;
import ewe.io.IOException;

//##################################################################
public class RecordDatabaseIndex extends RecordFoundEntries implements DatabaseIndex{
//##################################################################
public String name;

//===================================================================
public String getName()
//===================================================================
{
	return name;
}

protected PropertyList pl = new PropertyList();
{
	isAllInclusive = true;
}
//===================================================================
public boolean needsCompacting() throws IOException
//===================================================================
{
	return needCompact;
}
//===================================================================
public boolean compact(Handle h) throws IOException
//===================================================================
{
	if (!((DatabaseObject)database).saveIndex(h,(DatabaseIndex)this))
		return false;
	needCompact = false;
	return true;
}

//===================================================================
public PropertyList getProperties()
//===================================================================
{
	return pl;
}
//-------------------------------------------------------------------
protected RecordDatabaseIndex(Database database,String name)
//-------------------------------------------------------------------
{
	super(database);
	this.name = name;
}
//===================================================================
public FoundEntries getEntries()
//===================================================================
{
	RecordFoundEntries fe = (RecordFoundEntries)getNewFoundEntries();
	fe.ids.length = ids.length;
	fe.ids.data = new int[ids.length];
	Vm.copyArray(ids.data,0,fe.ids.data,0,ids.length);
	return fe;
}
//##################################################################
}
//##################################################################

