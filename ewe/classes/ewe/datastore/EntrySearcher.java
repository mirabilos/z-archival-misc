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
import ewe.sys.*;

/**
* This class is used to quickly search through a set of DatabaseEntry items in
* a Database.
* <p>
* You specify at construction which fields you are interested in. Then, after the task
* is started, for each entry that data will be fetched and stored in an appropriate object
* in an array of Objects - one for each field specified. That array is then passed to the
* doEntry() method.
**/
//##################################################################
public abstract class EntrySearcher extends TaskObject{
//##################################################################

protected Database table;
protected IntArray ids;
protected int [] fields;
protected ByteArray buffer = new ByteArray();
protected DatabaseEntry ded = new DatabaseEntry();
protected Object [] data;
/**
* Create a new EntrySearcher based on the entries in the FoundEntries object.
**/
//===================================================================
public EntrySearcher(FoundEntries fe,int [] fieldIds)
//===================================================================
{
	this(fe.table,fe.ids,fieldIds);
}
/**
* Create a new EntrySearcher for the specified entryIds in the specified tables.
**/
//===================================================================
public EntrySearcher(Database table,IntArray entryIds,int [] fieldIds)
//===================================================================
{
	this.table = table;
	this.ids = entryIds;
	this.fields = fieldIds;
	int len = fieldIds.length;
	data = new Object[len];
	for (int i = 0; i<len; i++){
		int type = table.getFieldType(fieldIds[i]);
		switch(type){
			case 0: break;
			case Database.STRING:
				data[i] = new SubString(); break;
			case Database.DATE_TIME:
				data[i] = new Time(); break;
			case Database.BYTE_ARRAY:
				data[i] = new ByteArray(); break;
			default:
				data[i] = new ewe.sys.Long(); break;
		}
	}
}
/**
* Override this to process the data for each entry.
**/
//-------------------------------------------------------------------
protected abstract void doEntry(Object [] fieldData,int fieldId) throws Exception;
//-------------------------------------------------------------------
/**
* This is the loop that fetches the field data for each entry.
**/
//-------------------------------------------------------------------
protected boolean doAllFields()
//-------------------------------------------------------------------
{
	for (int i = 0; i<ids.length; i++){
		try{
			if (handle.shouldStop){
				handle.set(handle.Failed|handle.Aborted);
				return false;
			}
			DatabaseEntry dt = table.getData(ids.data[i],buffer,ded);
			if (dt != null){
				for (int f = 0; f<fields.length; f++)
					if (data[f] != null)
						dt.getFieldValue(fields[f],data[f]);
				doEntry(data,ids.data[i]);
			}
		}catch(Exception e){
			handle.errorObject = e;
			handle.set(Handle.Failed);
			return false;
		}
		handle.progress = (i+1)/(float)ids.length;
		handle.changed();
		nap();
	}
	return true;
}
/**
* Override this to return a custom returnValue in the Handle for the task (optional).
**/
//-------------------------------------------------------------------
protected Object getReturnValue() {return null;}
//-------------------------------------------------------------------

//-------------------------------------------------------------------
protected void doRun()
//-------------------------------------------------------------------
{
	if (doAllFields()){
		handle.returnValue = getReturnValue();
		handle.set(handle.Succeeded);
	}
}
//##################################################################
}
//##################################################################

