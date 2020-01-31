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

//##################################################################
public class DataEntry{
//##################################################################
int myLocation;
int next, prev, parent, children, dataLocation, allocatedLength;
//This is not stored in the database.
//int lastChild;
DataEntry copiedFrom;
DataStorage storage;
//-------------------------------------------------------------------
DataEntry(DataStorage s) {storage = s;}
//-------------------------------------------------------------------

/**
* This returns the uniqute integer ID of this entry. This can be used
* later to retrieve this entry again by calling get(int ID) on the root
* entry.
**/
//===================================================================
public int id() {return myLocation;}
//===================================================================
/**
* Get next sibling entry.
**/
//===================================================================
public DataEntry getNext(){return storage.getEntryAt(next);}
//===================================================================
/**
* Get prev sibling entry.
**/
//===================================================================
public DataEntry getPrev(){return storage.getEntryAt(prev);}
//===================================================================
/**
* Get parent entry.
**/
//===================================================================
public DataEntry getParent(){return storage.getEntryAt(parent);}
//===================================================================
/**
* Get first child entry.
**/
//===================================================================
public DataEntry getFirstChild(){return storage.getEntryAt(children);}
//===================================================================
/**
* Get last child entry.
**/
//===================================================================
public DataEntry getLastChild()
//===================================================================
{
	//if (lastChild != 0) return storage.getEntryAt(lastChild);
	int where = storage.getLastChild(myLocation);
	if (where == 0) return null;
	return storage.getEntryAt(where);
}
/**
* Get the DataEntry with the specified ID.
**/
//===================================================================
public DataEntry get(int entryID)
//===================================================================
{
	return storage.getEntryAt(entryID);
}
//===================================================================
/**
* This moves the entire entry and all its children so that it is now a child of the
* specified parent, inserted immediately before the "before" entry (which must be a
* child of the parent). If "before" is null it will be moved to be the last child of the parent.
**/
public boolean moveTo(DataEntry parent,DataEntry before) {return storage.move(this,parent,before);}
/**
* This replaces an old DataEntry with this one. This effectively disconnects the old one from the
* tree - taking all it's children - and places this entry in the old one's place. The old entry
* is deleted.
**/
public boolean replace(DataEntry old) {return storage.replace(this,old);}
/**
* Delete an entry and all its children.
* @deprecated - use delete() instead.
**/
//===================================================================
public boolean deleteAll() {return delete();}
//===================================================================
/**
* This disconnects the entry from the tree. It is not marked as free and can have children added/removed
* from it etc. It can be re-inserted into the tree by using moveTo() or replace().
**/
//===================================================================
public boolean disconnect() {return storage.remove(this);}
//===================================================================

/**
* This disconnects this entry from the tree and then marks it and all its children as free space. Do not attempt to use
* it afterwards.
**/
//===================================================================
public boolean delete()
//===================================================================
{
	DataEntry next;
	for (DataEntry de = getFirstChild(); de != null; de = next){
		next = de.getNext();
		if (!de.delete()) return false;
	}
	return storage.delete(this);
}

//-------------------------------------------------------------------
DataEntry getCopy()
//-------------------------------------------------------------------
{
	DataEntry de = new DataEntry(storage);
	de.myLocation = myLocation;
	de.next = next;
	de.prev = prev;
	de.parent = parent;
	de.children = children;
	de.dataLocation = dataLocation;
	de.allocatedLength = allocatedLength;
	de.copiedFrom = this;
	return de;
}
//===================================================================
/**
* Raw data write. Writes the data to the database. It will replace the data in the
* same location if the current data space is big enough.
**/
//-------------------------------------------------------------------
boolean writeData(byte [] data) {return writeData(data,0,data.length);}
//-------------------------------------------------------------------
/**
* Raw data write. Writes the data to the database. It will replace the data in the
* same location if the current data space is big enough.
**/
//-------------------------------------------------------------------
boolean writeData(byte [] data,int offset,int length)
//-------------------------------------------------------------------
{
	return storage.writeEntryData(this,data,offset,length);
}
/**
* Raw data read. Reads the data from the database. It will return a new ByteArray
* if the provided one is null.
**/
//-------------------------------------------------------------------
ByteArray readData(ByteArray dest)
//-------------------------------------------------------------------
{
	return storage.readEntryData(this,dest);
}

static DataEntryData temp = new DataEntryData();
static ByteArray buffer = new ByteArray();
/**
* This calls getData() with null "buffer" and "dest" arguments.
**/
//===================================================================
public DataEntryData getData() throws ewe.io.IOException {return getData(null,null);}
//===================================================================
/**
* This will get the DataEntryData which holds the saved data associated
* with the Entry. If the entry has no data yet - this will return a
* new empty DataEntryData that is unsaved. Otherwise it will return a
* DataEntryData which represents the data as saved in the DataStorage.
* Remember that any changes made to the DataEntryData will only be saved
* to the DataStorage when you call "save()" on the DataEntryData.
**/
//===================================================================
public DataEntryData getData(ByteArray buffer,DataEntryData dest) throws ewe.io.IOException
//===================================================================
{
	return (DataEntryData)storage.table.getData(myLocation,buffer,dest);
}
/**
* Checks if the name of this data entry matches the supplied name. This
* is not case sensitive.
**/
//===================================================================
public boolean isNamed(String name) throws ewe.io.IOException
//===================================================================
{
	DataEntryData ded = getData(buffer,temp);
	return ded.isNamed(name);
}
/**
* Set and save the name of this DataEntry.
**/
//===================================================================
public void setName(String name) throws ewe.io.IOException
//===================================================================
{
	DataEntryData ded = getData(buffer,temp);
	ded.setName(name);
	ded.save();
}
/**
* Gets the name of this DataEntry if any.
**/
//===================================================================
public String getName() throws ewe.io.IOException
//===================================================================
{
	DataEntryData ded = getData(buffer,temp);
	return ded.getName();
}
/**
* This saves an object as the data for this entry with the specified name
* (which may be null). The code for this is:
* <pre>
* {
* DataEntryData ded = getData(buffer,temp);
* ded.setName(name);
* ded.setObject(obj);
* if (!ded.save()) return false;
* if (obj instanceof StoredObject){
* ((StoredObject)obj).entry = this;
* ((StoredObject)obj).storage = storage;
* }
* return true;
* }
*</pre>
**/
//===================================================================
public void saveObject(String name,Object obj) throws ewe.io.IOException
//===================================================================
{
	DataEntryData ded = getData(buffer,temp);
	ded.setName(name);
	ded.setObject(obj);
	ded.save();
	if (obj instanceof StoredObject){
		((StoredObject)obj).entry = this;
		((StoredObject)obj).storage = storage;
	}
}
/**
* This loads an object from the data entry's data. If dest is null then
* a new one will be created. This will only work under Java if a public
* default (no parameter) constructor exists for the object. The code
* for this method is:
* <pre>
* {
* Object ret = getData(buffer,temp).getObject(dest);
* if (ret instanceof StoredObject) {
*  ((StoredObject)ret).entry = this;
*  ((StoredObject)ret).storage = storage;
* }
* return ret;
* }
* </pre>
*
**/
//===================================================================
public Object loadObject(Object dest) throws ewe.io.IOException
//===================================================================
{
	Object ret = getData(buffer,temp).getObject(dest);
	if (ret instanceof StoredObject) {
		((StoredObject)ret).entry = this;
		((StoredObject)ret).storage = storage;
	}
	return ret;
}
/**
* This saves an object as a new child of this entry with the specified name.
* If the replace flag is true and one of the same name already exists - it
* will be replaced with the new one. If replace is false a new entry will
* be created even if one with the same name already exists.
**/
//===================================================================
public DataEntry saveChild(String name,Object obj,boolean replace) throws ewe.io.IOException
//===================================================================
{
	DataEntry parent = this;
	int idx = name.lastIndexOf('/');
	if (idx != -1) {
		parent = find(name.substring(0,idx),true); //Find or create the parent.
		if (parent == null) return null;
		name = name.substring(idx+1,name.length());
	}
	DataEntry de = null;
	if (replace) de = parent.find(name,true);
	if (de == null) de = parent.makeNewChild();
	de.saveObject(name,obj);
	return de;
}
/**
* This makes AND saves a new child entry under this entry. The new entry
* does not have any data (nor name) associated with it.
**/
//===================================================================
public DataEntry makeNewChild(boolean addAsFirstChild)
//===================================================================
{
	DataEntry ch = storage.makeNewEntry();
	if (ch == null) return null;
	if (!ch.moveTo(this,addAsFirstChild ? getFirstChild() : null)) return null;
	return ch;
}
/**
* This makes AND saves a new child entry under this entry. The new entry
* does not have any data (nor name) associated with it.
**/
//===================================================================
public DataEntry makeNewChild()
//===================================================================
{
	return makeNewChild(false);
}
/**
* This creates and saves a new DataEntry with the specified path name - relative to this DataEntry.
* The path name can have '/' characters (but not '\') to specify parent entry names. If
* these parent entries do not exist they will be created. This is similar to the java.io.File.mkdirs()
* command. In this case, this will always create a new entry even if one with the same
* path already exists. Remember that entries can have the same name, even among sibilings.
* The new entry does not have any data associated with it - only a name.
**/
//===================================================================
public DataEntry createAnother(String path) throws ewe.io.IOException
//===================================================================
{
	if (path.startsWith("/")) path = path.substring(1,path.length());
	if (path.endsWith("/")) path = path.substring(0,path.length()-1);
	Vector v = mString.split(path,'/',null);
	DataEntry p = this;
	for (int i = 0; i<v.size(); i++){
		String name = (String)v.get(i);
		DataEntry found = null;
		if (i == v.size()-1){
			DataEntry ch = p.makeNewChild();
			ch.setName(name);
			return ch;
		}
		for (DataEntry ch = p.getFirstChild(); ch != null; ch = ch.getNext()){
			if (ch.isNamed(name)){
				found = ch;
				break;
			}
		}
		if (found == null){
			DataEntry ch = p.makeNewChild();
			ch.setName(name);
			found = ch;
		}
		p = found;
	}
	return p;
}
/**
* This finds an entry with the specified path, relative to this entry. If it is
* not found and create is true - a new one will be created, named appropriately and
* then returned.
**/
//===================================================================
public DataEntry find(String path,boolean create) throws ewe.io.IOException
//===================================================================
{
	if (path.startsWith("/")) path = path.substring(1,path.length());
	if (path.endsWith("/")) path = path.substring(0,path.length()-1);
	Vector v = mString.split(path,'/',null);
	DataEntry p = this;
	for (int i = 0; i<v.size(); i++){
		String name = (String)v.get(i);
		DataEntry found = null;
		for (DataEntry ch = p.getFirstChild(); ch != null; ch = ch.getNext()){
			if (ch.isNamed(name)){
				found = ch;
				break;
			}
		}
		if (found == null){
			if (!create) return null;
			DataEntry ch = p.makeNewChild();
			if (ch == null) return null;
			ch.setName(name);
			found = ch;
		}
		p = found;
	}
	return p;
}

/**
* Saves an object. It first tries to text encode the object.
**/
/*
//===================================================================
public boolean writeObject(String name,Object obj,ByteArray buffer)
//===================================================================
{
	DataEntry parent = this;
	int idx = name.lastIndexOf('/');
	if (idx != -1) {
		parent = find(name.substring(0,idx),true); //Find or create the parent.
		if (parent == null) return null;
		name = name.substring(idx+1,name.length());
	}
	if (replace) de = parent.find(name,true);
	else de = parent.makeNewChild();
	if (!de.write(new Data().setName(name).setObject(what),null)) return null;
	return de;
}
*/
/*
//===================================================================
public boolean write(Data dt,ByteArray buffer)
//===================================================================
{
	if (dt == null) return writeData(null,0,0);
	if (buffer != null) dt.buffer = buffer;
	int size = dt.write();
	return writeData(dt.buffer.data,0,size);
}
//===================================================================
public Data read(Data dest,byte [] buffer)
//===================================================================
{
	if (dest == null) dest = new Data();
	if (buffer != null) dest.buffer = buffer;
	if (dataLocation == 0 || allocatedLength == 0) return null;
	if (dest.buffer != null)
		if (dest.buffer.length < allocatedLength)
			dest.buffer = null;
	if (dest.buffer == null) dest.buffer = new byte[allocatedLength];
	storage.readData(this,dest.buffer,0);
	dest.read(dest.buffer,0);
	return dest;
}
//===================================================================
public DataEntry save(String name,Object what,boolean replace)
//===================================================================
{
	DataEntry de = null;
	DataEntry parent = this;
	int idx = name.lastIndexOf('/');
	if (idx != -1) {
		parent = find(name.substring(0,idx),true); //Find or create the parent.
		if (parent == null) return null;
		name = name.substring(idx+1,name.length());
	}
	if (replace) de = parent.find(name,true);
	else de = parent.makeNewChild();
	if (!de.write(new Data().setName(name).setObject(what),null)) return null;
	return de;
}
//===================================================================
public Object getObject(Object dest)
//===================================================================
{
	Data dt = read(null,null);
	if (dt == null) return null;
	Object ret = dt.getObject(dest);
	return ret;
}
*/

/**
* This is hardly used. It writes the internal information about the
* entry to the storae.
**/
//===================================================================
public boolean write()
//===================================================================
{
	return storage.writeEntryAt(this,myLocation);
}
//===================================================================
public boolean hasChildren()
//===================================================================
{
	return storage.hasChildren(this);
}
/**
* This gets all the IDs of all the children of this entry. The IDs can
* be used to get the DataEntry objects themselves by calling: get(int ID)
* on this DataEntry or on the root entry (in fact on any entry from the same
* DataStorage since the IDs are unique within the DataStorage). In fact, the
* ID of an entry is the location in the RandomAccessStream of the entry itself.
**/
//===================================================================
public IntArray getAllChildIds(IntArray dest)
//===================================================================
{
	return storage.getAllChildIds(this,dest);
}
/**
* This gets a DataTable object which is stored at this entry - assuming that
* such an object is in fact stored there. It will return null if no DataTable
* is stored in this entry.
**/
//===================================================================
public DataTable getDataTable()  throws ewe.io.IOException
//===================================================================
{
	Object got = loadObject(new DataTable());
	if (!(got instanceof DataTable)) return null;
	return (DataTable)got;
}
/**
* This gets a new DataTable that you intend to setup and store at this entry.
**/
//===================================================================
public DataTable getNewDataTable()
//===================================================================
{
	DataTable table = new DataTable(storage,this);
	return table;
}

//##################################################################
}
//##################################################################


