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
import ewe.sys.Time;
import ewe.reflect.Reflect;
/**
* Each field value is stored as follows in the byte array.
* byte 1 = ID; A value of 0 indicates an invalid or deleted field.
* byte 2 = Reserved;
* byte 3,4 = Length of Data; A short integer value indicating the length of the data.
* byte[length of data] - The data itself.
**/
//##################################################################
public class DatabaseEntry{
//##################################################################

/**
* This is true by default - it allows the storing and retrieving of null String values. If it
* is false then all stored null strings will be returned as empty non-null strings.
**/
public static boolean allowNullStrings = true;

Database table;
FoundEntries found;
public ByteArray buffer;
int stored;

public final ewe.sys.Long longValue = new ewe.sys.Long();
public final ewe.sys.Double doubleValue = new ewe.sys.Double();
public final SubString substring = new SubString();

//-------------------------------------------------------------------
DatabaseEntry set(DataTable table,ByteArray buffer,int stored)
//-------------------------------------------------------------------
{
	this.table = table;
	this.buffer = buffer;
	this.stored = stored;
	return this;
}

//-------------------------------------------------------------------
protected void updateBufferSize()
//-------------------------------------------------------------------
{
	Utils.writeInt(buffer.length-4,buffer.data,0,4);
}
//===================================================================
public void clearBuffer()
//===================================================================
{
	buffer.clear();
	buffer.makeSpace(0,4);
	updateBufferSize();
}
//===================================================================
public void decodeBuffer()
//===================================================================
{
	if (buffer.length == 0) clearBuffer();
	else{
		buffer.length = Utils.readInt(buffer.data,0,4)+4;
		// Delete any zero'ed fields.
		// Fields get zero'ed when a field is removed from the data table.
		while(deleteFieldValue(0));
	}
}
/**
* This uses the data in the provided byte array for decoding the data in the database entry.
* This is useful during synchronization - since it allows you to transmit the database entry
* as its bytes only and decode it on the receiving end using this method.
**/
//===================================================================
public void decodeFrom(byte [] encoded)
//===================================================================
{
	decodeFrom(buffer = new ByteArray(encoded));
}
/**
* This uses the data in the provided buffer for decoding the data in the database entry.
* This is useful during synchronization - since it allows you to transmit the database entry
* as its bytes only and decode it on the receiving end using this method.
**/
//===================================================================
public void decodeFrom(ByteArray buffer)
//===================================================================
{
	this.buffer = buffer;
	decodeBuffer();
}
//===================================================================
public byte [] toBytes()
//===================================================================
{
	if (buffer == null) return new byte[0];
	return buffer.toBytes();
}
/**
* This copies the data from the other DatabaseEntry, but does not copy the
* OID, modified date or created date.
**/
//===================================================================
public void copyFrom(DatabaseEntry other)
//===================================================================
{
	Time created = other.hasField(Database.CREATED_FIELD) ?
		other.getField(Database.CREATED_FIELD,new Time()) :
		null;
	Time modified = other.hasField(Database.MODIFIED_FIELD) ?
		other.getField(Database.MODIFIED_FIELD,new Time()) :
		null;
	long oid = other.hasField(Database.OID_FIELD) ?
		other.getField(Database.OID_FIELD,(long)0) :
		-1;

	duplicateFrom(other);

	if (created != null) setField(Database.CREATED_FIELD,created);
	if (modified != null) setField(Database.MODIFIED_FIELD,modified);
	if (oid != -1) setField(Database.OID_FIELD,oid);
}
/**
* This duplicates the data from the other DatabaseEntry, <b>including</b> the
* OID, modified date or created date.
**/
//===================================================================
public void duplicateFrom(DatabaseEntry other)
//===================================================================
{
	buffer = new ByteArray(other.buffer.toBytes());
	decodeBuffer();
}
/**
* This returns true if a value is saved in the database for the specified fieldID
**/
//===================================================================
public boolean hasField(int fieldID)
//===================================================================
{
	return findField(fieldID) != -1;
}
//===================================================================
public int findField(String fieldName)
//===================================================================
{
	Database t = table;
	if (t == null) return 0;
	return t.findField(fieldName);
}
//-------------------------------------------------------------------
protected int findField(int fieldID)
//-------------------------------------------------------------------
{
	if (buffer == null) return -1;
	int i = 4;
	while (i<buffer.length-3){
		int id = buffer.data[i] & 0xff;
		if (id == fieldID) return i;
		int length = Utils.readInt(buffer.data,i+2,2);
		i += length+4;
	}
	return -1;
}
//-------------------------------------------------------------------
protected void deleteField(int location)
//-------------------------------------------------------------------
{
	int length = Utils.readInt(buffer.data,location+2,2);
	buffer.delete(location,length+4);
	updateBufferSize();
}
//-------------------------------------------------------------------
protected int fieldSize(int fieldID,Object value)
//-------------------------------------------------------------------
{
	int type = table.getFieldType(fieldID);
	switch(type){
		case Database.INTEGER:
			if (!(value instanceof ewe.sys.Long)) return 0;
			return 4+4;
		case Database.BOOLEAN:
			if (!(value instanceof ewe.sys.Long)) return 0;
			return 4+1;
		case Database.LONG:
			if (!(value instanceof ewe.sys.Long)) return 0;
			return 4+8;
		case Database.DOUBLE:
			if (!(value instanceof ewe.sys.Double)) return 0;
			return 4+8;
		case Database.DATE_TIME:
			if (!(value instanceof ewe.sys.Time)) return 0;
			return 4+8;
		case Database.STRING:
			if (value instanceof String) {
				String str = (String)value;
				return 4+Utils.sizeofJavaUtf8String(ewe.sys.Vm.getStringChars(str),0,str.length());
			}
			if (value instanceof SubString) {
				SubString ss = (SubString)value;
				return 4+Utils.sizeofJavaUtf8String(ss.data,ss.start,ss.length);
			}
			if (value instanceof char []) {
				char [] ch = (char [])value;
				return 4+Utils.sizeofJavaUtf8String(ch,0,ch.length);
			}
			return 0;
		case Database.BYTE_ARRAY:
			if (value instanceof ByteArray) return 4+((ByteArray)value).length;
			if (value instanceof byte []) return 4+((byte [])value).length;
			return 0;
		default: return 0;
	}
}
//===================================================================
public boolean setFieldValue(int fieldID,Object value)
//===================================================================
{
	deleteFieldValue(fieldID);
	if (value == null) return true;
	int size = fieldSize(fieldID,value);
	if (size == 0) return false;
	//......................................................
	// Now append the data.
	//......................................................
	int where = buffer.length;
	buffer.makeSpace(where,size);
	writeField(where,fieldID,value,size-4);
	return true;
}
//===================================================================
public boolean deleteFieldValue(int fieldID)
//===================================================================
{
	int where = findField(fieldID);
	if (where == -1) return false;
	deleteField(where);
	return true;
}

//-------------------------------------------------------------------
int findAll(int [] dest)
//-------------------------------------------------------------------
{
	if (buffer == null) return 0;
	int i = 4;
	int found = 0;
	while (i<buffer.length){
		int id = buffer.data[i] & 0xff;
		if (id == 0) continue;
		if (dest != null) dest[found] = id;
		found++;
		int length = Utils.readInt(buffer.data,i+2,2);
		i += length+4;
	}
	return found;
}
//===================================================================
public int [] getAssignedFields()
//===================================================================
{
	int num = findAll(null);
	int [] ret = new int[num];
	if (num != 0) findAll(ret);
	return ret;
}
/**
* This is used to get a field value specifying a default value.
**/
//===================================================================
public Object getFieldValue(int fieldID,Object destination,Object defaultValue)
//===================================================================
{
	Object ret = getFieldValue(fieldID,destination);
	if (ret == null) ret = defaultValue;
	return ret;
}
/**
* This is used to get a field value. If no value is found for the field
* null will be returned.
**/
//===================================================================
public Object getFieldValue(int fieldID,Object destination)
//===================================================================
{
	return getFieldValue(fieldID,table.getFieldType(fieldID),destination);
}
//===================================================================
public int getField(int fieldID,int defaultValue)
//===================================================================
{
	Object ret = getFieldValue(fieldID,null);
	if (!(ret instanceof ewe.sys.Long)) return defaultValue;
	return (int)((ewe.sys.Long)ret).value;
}
//===================================================================
public long getField(int fieldID,long defaultValue)
//===================================================================
{
	Object ret = getFieldValue(fieldID,null);
	if (!(ret instanceof ewe.sys.Long)) return defaultValue;
	return ((ewe.sys.Long)ret).value;
}
//===================================================================
public String getFieldString(int fieldID,String defaultValue)
//===================================================================
{
	Object ret = getFieldValue(fieldID,null);
	if (ret == null) return defaultValue;
	return ret.toString();
}
//===================================================================
public boolean getField(int fieldID,boolean defaultValue)
//===================================================================
{
	int ret = getField(fieldID,defaultValue ? 1 : 0);
	return ret != 0;
}
//===================================================================
public float getField(int fieldID,float defaultValue)
//===================================================================
{
	Object ret = getFieldValue(fieldID,null);
	if (!(ret instanceof ewe.sys.Double)) return defaultValue;
	return (float)((ewe.sys.Double)ret).value;
}
//===================================================================
public double getField(int fieldID,double defaultValue)
//===================================================================
{
	Object ret = getFieldValue(fieldID,null);
	if (!(ret instanceof ewe.sys.Double)) return defaultValue;
	return ((ewe.sys.Double)ret).value;
}
//===================================================================
public boolean setField(int fieldID,String value)
//===================================================================
{
	return setFieldValue(fieldID,value);
}
//===================================================================
public boolean setField(int fieldID,int value)
//===================================================================
{
	return setFieldValue(fieldID,new ewe.sys.Long().set(value));
}
//===================================================================
public boolean setField(int fieldID,float value)
//===================================================================
{
	return setFieldValue(fieldID,new ewe.sys.Double().set(value));
}
//===================================================================
public boolean setField(int fieldID,boolean value)
//===================================================================
{
	return setFieldValue(fieldID,new ewe.sys.Long().set(value ? 1 : 0));
}
//===================================================================
public boolean setField(int fieldID,double value)
//===================================================================
{
	return setFieldValue(fieldID,new ewe.sys.Double().set(value));
}
//===================================================================
public boolean setField(int fieldID,long value)
//===================================================================
{
	return setFieldValue(fieldID,new ewe.sys.Long().set(value));
}
//===================================================================
public boolean setField(int fieldID,Time value)
//===================================================================
{
	return setFieldValue(fieldID,value);
}
//===================================================================
public Time getField(int fieldID,Time dest)
//===================================================================
{
	if (dest == null) dest = new Time();
	return (Time)getFieldValue(fieldID,dest);
}
/**
* You would hardly use this. Use one of the other getFieldValues().
**/
//===================================================================
public Object getFieldValue(int fieldID,int type,Object destination)
//===================================================================
{
	int found = findField(fieldID);
	if (found == -1) return null;
	return readField(found,type,destination);
}
/**
* Set the name of the table entry.
**/
//===================================================================
public boolean setName(String name)
//===================================================================
{
	boolean s = setFieldValue(table.NAME_FIELD,name);
	return s;
}
/**
* Get the name of the table entry.
**/
//===================================================================
public String getName()
//===================================================================
{
	Object got = getFieldValue(table.NAME_FIELD,null);
	if (got == null) return null;
	else return got.toString();
}
/**
* This checks the name of the entry in the most effecient manner.
**/
//FIX make this more effecient.
//===================================================================
public boolean isNamed(String name)
//===================================================================
{
	String n = getName();
	if (n == null) return false;
	return table.getLocale().compare(name,n,ewe.sys.Locale.IGNORE_CASE) == 0;
}
//-------------------------------------------------------------------
protected void writeLong(int where,Object value,int length)
//-------------------------------------------------------------------
{
	ewe.sys.Long l = (ewe.sys.Long)value;
	if (length <= 4){
		Utils.writeInt((int)l.value,buffer.data,where,length);
	}
	else {
		Utils.writeInt((int)((l.value >> 32) & 0xffffffffL),buffer.data,where,4);
		Utils.writeInt((int)(l.value & 0xffffffffL),buffer.data,where+4,4);
	}
}
//-------------------------------------------------------------------
protected void readLong(int where,ewe.sys.Long value,int length)
//-------------------------------------------------------------------
{
	if (length <= 4) value.set((long)Utils.readInt(buffer.data,where,length) & 0xffffffffL);
	else {
		long high = (long)Utils.readInt(buffer.data,where,4) & 0xffffffffL;
		long low = (long)Utils.readInt(buffer.data,where+4,4) & 0xffffffffL;
		value.set(high << 32 | low);
	}
}
//-------------------------------------------------------------------
protected Object readField(int where,int type,Object value)
//-------------------------------------------------------------------
{
	int fieldID = (int)buffer.data[where] & 0xff;
	int dataLength = Utils.readInt(buffer.data,where+2,2);
	int len = 0;
	switch(type){
		case Database.INTEGER: len = 4; break;
		case Database.BOOLEAN: len = 1; break;
		case Database.DATE_TIME:
		case Database.DOUBLE:
		case Database.LONG: len = 8; break;

	}
	if (len != 0) {
		readLong(where+4,doubleToLong,len);
		if (type == Database.DOUBLE) {
			if (value == null || !(value instanceof ewe.sys.Double)) value = new ewe.sys.Double();
			((ewe.sys.Double)value).value = ewe.sys.Convert.toDoubleBitwise(doubleToLong.value);
			//((ewe.sys.Double)value).fromBits(doubleToLong);
		}else if (type == Database.LONG || type == Database.INTEGER || type == Database.BOOLEAN){
			if (value == null || !(value instanceof ewe.sys.Long)) value = new ewe.sys.Long();
			((ewe.sys.Long)value).set(doubleToLong);
		}else if (type == Database.DATE_TIME){
			if (value == null || !(value instanceof ewe.sys.Time)) value = new ewe.sys.Time();
			((ewe.sys.Time)value).setEncodedTime(doubleToLong.value);
		}
		return value;
	}
	switch(type){
		case Database.STRING:
			SubString ss = (value == null || !(value instanceof SubString)) ? new SubString() : (SubString)value;
			int numChars = Utils.sizeofJavaUtf8String(buffer.data,where+4,dataLength);
			if (ss.data != null) if (ss.data.length < numChars) ss.data = null;
			if (ss.data == null) ss.data = new char[numChars];
			Utils.decodeJavaUtf8String(buffer.data,where+4,dataLength,ss.data,0);
			ss.start = 0;
			ss.length = numChars;
			return ss;
		case Database.BYTE_ARRAY:
			ByteArray ba = (value == null || !(value instanceof ByteArray)) ? new ByteArray() : (ByteArray)value;
			ba.clear();
			ba.makeSpace(0,dataLength);
			ewe.sys.Vm.copyArray(buffer.data,where+4,ba.data,0,dataLength);
			return ba;
		default:
			return null;
	}
}
private ewe.sys.Long doubleToLong = new ewe.sys.Long();
//-------------------------------------------------------------------
protected void writeField(int where,int fieldID,Object value,int dataLength)
//-------------------------------------------------------------------
{
	buffer.data[where] = (byte)(fieldID & 0xff);
	buffer.data[where+1] = 0;
	Utils.writeInt(dataLength,buffer.data,where+2,2);
	int type = table.getFieldType(fieldID);
	int len = 0;
	switch(type){
		case Database.INTEGER: len = 4; break;
		case Database.BOOLEAN: len = 1; break;
		case Database.DATE_TIME: doubleToLong.value = ((ewe.sys.Time)value).getEncodedTime(); value = doubleToLong; len = 8; break;
		case Database.DOUBLE:
			doubleToLong.value = ewe.sys.Convert.toLongBitwise(((ewe.sys.Double)value).value);
			//((ewe.sys.Double)value).toBits(doubleToLong);
			value = doubleToLong; len = 8; break;
		case Database.LONG: len = 8; break;
	}
	if (len != 0) {
		writeLong(where+4,value,len);
		updateBufferSize();
		return;
	}
	switch(type){
		case Database.STRING:
			if (value instanceof String) {
				String str = (String)value;
				Utils.encodeJavaUtf8String(ewe.sys.Vm.getStringChars(str),0,str.length(),buffer.data,where+4);
			}
			if (value instanceof SubString) {
				SubString ss = (SubString)value;
				Utils.encodeJavaUtf8String(ss.data,ss.start,ss.length,buffer.data,where+4);
			}
			if (value instanceof char []) {
				char [] ch = (char [])value;
				Utils.encodeJavaUtf8String(ch,0,ch.length,buffer.data,where+4);
			}
			updateBufferSize();
			return;
		case Database.BYTE_ARRAY:
			if (value instanceof byte [])
				ewe.sys.Vm.copyArray((byte [])value,0,buffer.data,where+4,((byte[])value).length);
			else if (value instanceof ByteArray){
				ByteArray v = (ByteArray)value;
				ewe.sys.Vm.copyArray(v.data,0,buffer.data,where+4,v.length);
			}
			updateBufferSize();
			return;
	}
}
/**
* This saves the data back to the data store.
**/
//===================================================================
public void save() throws ewe.io.IOException
//===================================================================
{
	table.saveEntry(this);
}
/**
* This removes the data's entry from the data store.
**/
//===================================================================
public void delete() throws ewe.io.IOException
//===================================================================
{
	table.deleteEntry(this);
}
//===================================================================
public void erase() throws ewe.io.IOException
//===================================================================
{
	table.eraseEntry(this);
}
//===================================================================
public boolean setObject(Object obj)
//===================================================================
{
	String str = toText(obj);
	if (str == null) return false;
	return setFieldValue(DataTable.OBJECT_TEXT_FIELD,str);
}
//===================================================================
public Object getObject(Object dest)
//===================================================================
{
	Object saved = getFieldValue(DataTable.OBJECT_TEXT_FIELD,null);
	if (saved == null) return null;
	return fromText(saved.toString(),dest);
}
//===================================================================
public static String toText(Object obj)
//===================================================================
{
	if (obj == null) return "";
	String s = TextEncoder.toString(obj);
	Reflect r = Reflect.getForObject(obj);
	if (r == null) return "";
	return r.getClassName()+"="+s;
}
//===================================================================
public static Object fromText(String s,Object dest)
//===================================================================
{
	if (s == null) return null;
	int idx = s.indexOf('=');
	if (idx == -1) return null;
	String cl = s.substring(0,idx);
	s = s.substring(idx+1,s.length());
	if (cl.equals("java/lang/String")) return s;
	if (dest == null){
		Reflect r = Reflect.getForName(cl);
		if (r == null) return null;
		dest = r.newInstance();
		if (dest == null) return null;
	}
	TextEncoder.fromString(dest,s);
	return dest;
}
//-------------------------------------------------------------------
static boolean deleteFieldFromFile(int id,ewe.io.RandomAccessStream stream,int entryLocation)
//-------------------------------------------------------------------
{
	//if (stream instanceof ewe.io.RandomAccessFile && false) return nativeDeleteFieldFromFile(id,stream,entryLocation);
	byte [] buff = new byte[4];
	if (!stream.seek(entryLocation+16)) return false;
	if (stream.readBytes(buff,0,4) != 4) return false;
	int data = Utils.readInt(buff,0,4);
	if (data == 0) return true;
	if (!stream.seek(data)) return false;
	if (stream.readBytes(buff,0,4) != 4) return false;
	int max = Utils.readInt(buff,0,4)+data+4;
	for (int loc = data+4; loc<max;){
		if (!stream.seek(loc)) return false;
		if (stream.readBytes(buff,0,4) != 4) return false;
		int length = Utils.readInt(buff,2,2);
		if (((int)buff[0] & 0xff) == id){
			buff[0] = 0;
			if (!stream.seek(loc)) return false;
			if (stream.writeBytes(buff,0,4) != 4) return false;
		}
		loc = length+4+loc;
	}
	return true;
}
//-------------------------------------------------------------------
static native boolean nativeDeleteFieldFromFile(int id,ewe.io.RandomAccessStream stream,int entryLocation);
//-------------------------------------------------------------------

/**
* This is used as a mechanism for automatic data transfer between a DataEntryData and an
* object that has the same field names.
**/
//===================================================================
public boolean _getSetValue(String fieldName,ewe.reflect.Wrapper value,boolean isGet)
//===================================================================
{
	if (table == null) return false;
	int id = table.findField(fieldName);
	if (id == 0) return false;
	int type = table.getFieldType(id);
	if (isGet){
		switch(type){
			case DataTable.INTEGER: value.setInt(getField(id,0)); break;
			case DataTable.DOUBLE: value.setDouble(getField(id,(double)0.0)); break;
			case DataTable.LONG: value.setLong(getField(id,(long)0)); break;
			case DataTable.STRING: value.setObject(getFieldString(id,allowNullStrings ? null : "")); break;
			case DataTable.BOOLEAN: value.setBoolean(getField(id,false)); break;
			case DataTable.DATE_TIME: value.setObject(getFieldValue(id,value.getObject())); break;
			case DataTable.BYTE_ARRAY:
				value.setObject(getFieldValue(id,value.getObject()));
				ByteArray ba = (ByteArray)value.getObject();
				break;
			default:
				return false;
		}
	}else{
		switch(type){
			case DataTable.INTEGER: setField(id,value.getInt()); break;
			case DataTable.DOUBLE: setField(id,value.getDouble()); break;
			case DataTable.LONG: setField(id,value.getLong()); break;
			case DataTable.BOOLEAN: setField(id,value.getBoolean()); break;
			case DataTable.STRING:
			case DataTable.DATE_TIME:
			case DataTable.BYTE_ARRAY:
					setFieldValue(id,value.getObject()); break;
			default:
				return false;
		}
	}
	return true;
}
//##################################################################
}
//##################################################################

