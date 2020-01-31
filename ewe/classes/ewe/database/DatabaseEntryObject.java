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
import ewe.sys.Time;
import ewe.util.ByteArray;
import ewe.util.SubString;
import ewe.util.CharArray;
import ewe.util.ByteEncodable;
import ewe.util.Hashtable;
import ewe.util.Debug;
import ewe.io.IOException;
import ewe.sys.Locale;
import ewe.sys.TimeOfDay;
import ewe.sys.DayOfYear;
import ewe.math.BigDecimal;
import ewe.sys.Decimal;
import ewe.sys.Convert;
import ewe.reflect.Wrapper;

//##################################################################
public abstract class DatabaseEntryObject implements DatabaseEntry{
//##################################################################
//
// Do not move this, it must be first.
//
protected ByteArray myData;
protected DatabaseObject database;
protected boolean isDeleted = false;
protected FoundEntries modifyingInside;
protected Locale locale;
//ByteArray myEncryptedData;
private Hashtable fields;

static final ewe.sys.Long longValue = new ewe.sys.Long();
static final ewe.sys.Double doubleValue = new ewe.sys.Double();
static final ewe.sys.Time timeValue = new ewe.sys.Time();
static final ewe.sys.Decimal decimalValue = new ewe.sys.Decimal();

static final SubString substring = new SubString();
static final ByteArray byteArray = new ByteArray();
/**
 * If the value is a String, convert it to an Object suitable for the specified type.
 * @param value the provided Object value, which may be a String.
 * @param type the type of the field being set.
 * @return a static (possibly shared) object value holding the appropriate data.
 */
//-------------------------------------------------------------------
protected static Object convertStringToStaticObjectValue(Object value, int type)
//-------------------------------------------------------------------
{
	if (!(value instanceof String) || (type == STRING)) return value;
	String s = (String)value;
	switch(type){
		case BOOLEAN:
			return longValue.set(Convert.toBoolean(s) ? 1 : 0);
		case INTEGER:
			return longValue.set(Convert.toInt(s));
		case LONG:
			return longValue.set(Convert.toLong(s));
		case DOUBLE:
			return doubleValue.set(Convert.toDouble(s));
		case DECIMAL:
			try{
				return new Decimal(s);
			}catch(Exception e){
				return new Decimal(0);
			}
		case TIME: case DATE_TIME: case DATE: case TIMESTAMP:
			timeValue.fromString(s);
			return timeValue;
		default:
			throw new IllegalArgumentException();
	}
}

//-------------------------------------------------------------------
protected DatabaseEntryObject(DatabaseObject database)
//-------------------------------------------------------------------
{
	this.database = database;
	locale = database == null ? ewe.sys.Vm.getLocale() : database.getLocale();
}

//===================================================================
public abstract Object getFieldValue(int fieldID, int type, Object data);
//===================================================================

/**
 * Get the database associated with the FoundEntries.
 */
//===================================================================
public Database getDatabase() {return database;}
//===================================================================
//===================================================================
public boolean isADeletedEntry()
//===================================================================
{
	return isDeleted;
}
//===================================================================
public void reset()
//===================================================================
{
	isDeleted = false;
}

//===================================================================
public void save() throws IllegalStateException, IOException
//===================================================================
{
	if (isADeletedEntry()) throw new IllegalStateException();
	boolean isNew = !isSaved();
	Time atime = database.now.setToCurrentTime();
	if (database.hasField(FLAGS_FIELD)){
		int value = getField(FLAGS_FIELD,0);
		value &= ~FLAG_SYNCHRONIZED;
		setField(FLAGS_FIELD,value);
	}
	if (database.hasField(MODIFIED_FIELD)){
		setField(MODIFIED_FIELD,atime);
	}
	if (database.hasField(MODIFIED_BY_FIELD)){
		setField(MODIFIED_BY_FIELD,database.getIdentifier());
	}
	if (isNew){
		if (database.hasField(OID_FIELD))
			setField(OID_FIELD,database.getNewOID());
		if (database.hasField(CREATED_FIELD))
			setField(CREATED_FIELD,atime);
	}
	store();
}
//===================================================================
public void delete() throws IOException
//===================================================================
{
	if (isADeletedEntry()) erase();
	else {
		if (!isSaved()) return;
		long oid = hasField(OID_FIELD) ? getField(OID_FIELD,(long)0) : -1;
		if (oid != -1) markAsDeleted();
		else erase();
	}
}

//-------------------------------------------------------------------
protected void markAsDeleted() throws IOException
//-------------------------------------------------------------------
{
	database.markAsDeleted(this);
}
//===================================================================
public void store() throws IllegalStateException, IOException
//===================================================================
{
	if (isADeletedEntry()) throw new IllegalStateException();
	database.store(this);
}
//===================================================================
public void erase() throws IOException
//===================================================================
{
	database.erase(this);
}
//===================================================================
public void revert() throws IllegalStateException, IOException
//===================================================================
{
	if (isADeletedEntry() || !isSaved()) throw new IllegalStateException();
	load();
}
//-------------------------------------------------------------------
protected void load() throws IOException
//-------------------------------------------------------------------
{
	clearDataAndSpecialFields();
	database.load(this);
	isDeleted = false;
}
/**
 * Get the data from the entry into a data object.
 * @param destination a destination object. If this is null a new one will be created if
 * possible.
 * @return the destination or new object.
 * @exception IllegalArgumentException if the destination object is not the right type.
 * @exception IllegalStateException if a new object was requested but could not be created.
 */
//===================================================================
public Object getData(Object destination) throws IllegalArgumentException, IllegalStateException
//===================================================================
{
	return database.getData(this,destination);
}
/**
 * Get the data from the entry, creating a new data object.
 * @return the new data object.
 * @exception IllegalStateException if a new object could not be created.
 */
//===================================================================
public Object getData() throws IllegalStateException
//===================================================================
{
	return database.getData(this,null);
}
/**
* Set the data in the entry from the data object.
* @param data the data to set.
* @exception IllegalArgumentException if the data object is the wrong type.
*/
//===================================================================
public void setData(Object data) throws IllegalArgumentException
//===================================================================
{
	clearFields();
	database.setData(this,data);
}
//===================================================================
public DatabaseEntry getNew()
//===================================================================
{
	return database.getNewData();
}
//===================================================================
public int compareTo(DatabaseEntry other,int sortID) throws IllegalArgumentException
//===================================================================
{
	return compareTo(other,database.toCriteria(sortID),false);
}
/**
* This is used as a mechanism for automatic data transfer between a DatabaseEntry and
* a user interface object.
**/
//===================================================================
public boolean _getSetField(String fieldName,ewe.reflect.Wrapper value,boolean isGet)
//===================================================================
{
	if (isGet) value.zero();
	return _getSetValue(fieldName,value,isGet);
}
/**
* This is used as a mechanism for automatic data transfer between a DatabaseEntry and an
* object that has the same field names.
**/
//===================================================================
public boolean _getSetValue(String fieldName,ewe.reflect.Wrapper value,boolean isGet)
//===================================================================
{
	//if (!isGet) ewe.sys.Vm.debug("Setting: "+fieldName+" to: "+value);
	int got = getFieldInfo(fieldName);
	if (got == 0) return false;
	//
	int id = (got >> 16) & 0xffff;
	int type = got & 0xffff;
	//
	if (type == 0) {
		if (isGet) return false;
		switch(value.getType()){
			case Wrapper.CHAR:
			case Wrapper.INT:
			case Wrapper.SHORT:
			case Wrapper.BYTE: type = INTEGER; break;
			case Wrapper.FLOAT:
			case Wrapper.DOUBLE: type = DOUBLE; break;
			case Wrapper.BOOLEAN: type = BOOLEAN; break;
			case Wrapper.LONG: type = LONG; break;
			default:
				Object obj = value.getObject();
				if (obj instanceof String) type = STRING;
				else if (obj instanceof SubString) type = STRING;
				else if (obj instanceof CharArray) type = STRING;
				else if (obj instanceof DayOfYear) type = DATE;
				else if (obj instanceof TimeOfDay) type = TIME;
				else if (obj instanceof TimeStamp) type = TIMESTAMP;
				else if (obj instanceof Time) type = DATE_TIME;
				else if (obj instanceof byte[]) type = BYTE_ARRAY;
				else if (obj instanceof ByteArray) type = BYTE_ARRAY;
				else if (obj instanceof BigDecimal) type = DECIMAL;
				else if (obj instanceof Decimal) type = DECIMAL;
				else return false;
		}
	}
	//
	if (isGet){
		switch(type){
			case INTEGER: value.setInt(getField(id,0)); break;
			case DOUBLE: value.setDouble(getField(id,(double)0.0)); break;
			case LONG: value.setLong(getField(id,(long)0)); break;
			case BOOLEAN: value.setBoolean(getField(id,false)); break;
			case STRING:
				Object obj = value.getObject();
				if (obj instanceof CharArray) value.setObject(getField(id,(CharArray)obj));
				else value.setObject(getField(id,(String)null));
				break;//allowNullStrings ? null : "")); break;
			case DATE_TIME: case TIME: case DATE: case TIMESTAMP:
			case DECIMAL:
			case BYTE_ARRAY:
			case JAVA_OBJECT:
				value.setObject(getFieldValue(id,type,value.getObject()));
				break;
			default:
				return false;
		}
	}else{
		switch(type){
			case INTEGER: setField(id,value.getInt()); break;
			case DOUBLE: setField(id,value.getDouble()); break;
			case LONG: setField(id,value.getLong()); break;
			case BOOLEAN: setField(id,value.getBoolean()); break;
			case STRING:
			case DATE_TIME: case DATE: case TIME: case TIMESTAMP:
			case DECIMAL:
			case BYTE_ARRAY:
			case JAVA_OBJECT:
					setFieldValue(id,type,value.getObject()); break;
			default:
				return false;
		}
	}
	return true;
}
//===================================================================
public void setFieldValue(int fieldID, Object data)
//===================================================================
{
	setFieldValue(fieldID,fieldToType(fieldID),data);
}
//===================================================================
public Object getFieldValue(int fieldID, Object data)
//===================================================================
{
	return getFieldValue(fieldID,fieldToType(fieldID),data);
}

//===================================================================
public void setField(int fieldID,int value)
//===================================================================
{
	setFieldValue(fieldID,INTEGER,longValue.set(value));
}
//===================================================================
public void setField(int fieldID,long value)
//===================================================================
{
	setFieldValue(fieldID,LONG,longValue.set(value));
}
//===================================================================
public void setField(int fieldID,boolean value)

//===================================================================
{
	setFieldValue(fieldID,BOOLEAN,longValue.set(value ? 1 : 0));
}
//===================================================================
public void setField(int fieldID,double value)
//===================================================================
{
	setFieldValue(fieldID,DOUBLE,doubleValue.set(value));
}
//===================================================================
public void setField(int fieldID,Time time)
//===================================================================
{
	setFieldValue(fieldID,DATE_TIME,time);
}
//===================================================================
public void setField(int fieldID,TimeOfDay time)
//===================================================================
{
	setFieldValue(fieldID,TIME,time);
}
//===================================================================
public void setField(int fieldID,DayOfYear date)
//===================================================================
{
	setFieldValue(fieldID,DATE,date);
}
//===================================================================
public void setField(int fieldID,TimeStamp timestamp)
//===================================================================
{
	setFieldValue(fieldID,TIMESTAMP,timestamp);
}
//===================================================================
public void setField(int fieldID,BigDecimal value)
//===================================================================
{
	setFieldValue(fieldID,DECIMAL,value);
}
//===================================================================
public void setField(int fieldID,Decimal value)
//===================================================================
{
	setFieldValue(fieldID,DECIMAL,value);
}
//===================================================================
public void setField(int fieldID,ByteArray bytes)
//===================================================================
{
	setFieldValue(fieldID,BYTE_ARRAY,bytes);
}
//===================================================================
public void setField(int fieldID,byte[] bytes)
//===================================================================
{
	ByteArray ba = bytes == null ? null : byteArray;
	if (ba != null){
		ba.data = bytes;
		ba.length = bytes.length;
	}
	setFieldValue(fieldID,BYTE_ARRAY,ba);
}
//===================================================================
public void setField(int fieldID,SubString chars)
//===================================================================
{
	setFieldValue(fieldID,STRING,chars);
}
//===================================================================
public void setField(int fieldID,CharArray chars)
//===================================================================
{
	if (chars == null) setFieldValue(fieldID,STRING,null);
	else{
		substring.data = chars.data;
		substring.start = 0;
		substring.length = chars.length;
		setFieldValue(fieldID,STRING,substring);
	}
}
//===================================================================
public void setField(int fieldID,String chars)
//===================================================================
{
	SubString ss = chars == null ? null : substring;
	if (ss != null){
		ss.data = ewe.sys.Vm.getStringChars(chars);
		ss.start = 0;
		ss.length = ss.data.length;
	}
	setFieldValue(fieldID,STRING,ss);
}

//===================================================================
public void setField(int fieldID,ByteEncodable value)
//===================================================================
{
	if (value == null) setFieldValue(fieldID,BYTE_ARRAY,null);
	byteArray.clear();
	value.encodeBytes(byteArray);
	setFieldValue(fieldID,BYTE_ARRAY,byteArray);
}

//===================================================================
public void setObjectField(int fieldID,Object value)
//===================================================================
{
	setFieldValue(fieldID,JAVA_OBJECT,value);
}
//===================================================================
public Object getObjectField(int fieldID,Object dest)
//===================================================================
{
	return getFieldValue(fieldID,JAVA_OBJECT,dest);
}
//===================================================================
public int getField(int fieldID,int defaultValue)
//===================================================================
{
	ewe.sys.Long lv = (ewe.sys.Long)getFieldValue(fieldID,INTEGER,longValue);
	if (lv == null) return defaultValue;
	return (int)lv.value;
}
//===================================================================
public long getField(int fieldID,long defaultValue)
//===================================================================
{
	ewe.sys.Long lv = (ewe.sys.Long)getFieldValue(fieldID,LONG,longValue);
	if (lv == null) return defaultValue;
	return lv.value;
}
//===================================================================
public boolean getField(int fieldID, boolean defaultValue)
//===================================================================
{
	ewe.sys.Long lv = (ewe.sys.Long)getFieldValue(fieldID,BOOLEAN,longValue);
	if (lv == null) return defaultValue;
	return lv.value != 0;
}
//===================================================================
public double getField(int fieldID, double defaultValue)
//===================================================================
{
	ewe.sys.Double lv = (ewe.sys.Double)getFieldValue(fieldID,DOUBLE,doubleValue);
	if (lv == null) return defaultValue;
	return lv.value;
}
//===================================================================
public Time getField(int fieldID, Time dest)
//===================================================================
{
	if (dest == null) dest = new Time();
	return (Time)getFieldValue(fieldID,DATE_TIME,dest);
}
//===================================================================
public String getField(int fieldID, String defaultValue)
//===================================================================
{
	String got = (String)getFieldValue(fieldID,STRING,null);
	if (got == null) return defaultValue;
	return got;
}
//===================================================================
public CharArray getField(int fieldID, CharArray dest)
//===================================================================
{
	if (dest == null) dest = new CharArray();
	return (CharArray)getFieldValue(fieldID,STRING,dest);
}
//===================================================================
public byte[] getFieldBytes(int fieldID)
//===================================================================
{
	ByteArray ret = (ByteArray)getFieldValue(fieldID,BYTE_ARRAY,byteArray);
	if (ret == null) return null;
	return ret.toBytes();
}
//===================================================================
public ByteArray getField(int fieldID,ByteArray dest)
//===================================================================
{
	if (dest == null) dest = new ByteArray();
	return (ByteArray)getFieldValue(fieldID,BYTE_ARRAY,dest);
}
//===================================================================
public DayOfYear getField(int fieldID, DayOfYear dest)
//===================================================================
{
	if (dest == null) dest = new DayOfYear();
	return (DayOfYear)getFieldValue(fieldID,DATE,dest);
}
//===================================================================
public TimeOfDay getField(int fieldID, TimeOfDay dest)
//===================================================================
{
	if (dest == null) dest = new TimeOfDay();
	return (TimeOfDay)getFieldValue(fieldID,TIME,dest);
}
//===================================================================
public TimeStamp getField(int fieldID, TimeStamp dest)
//===================================================================
{
	if (dest == null) dest = new TimeStamp();
	return (TimeStamp)getFieldValue(fieldID,TIMESTAMP,dest);
}
//===================================================================
public BigDecimal getField(int fieldID, BigDecimal defaultValue)
//===================================================================
{
	if (defaultValue == null) defaultValue = BigDecimal.valueOf(0);
	Decimal bd = (Decimal)getFieldValue(fieldID,DECIMAL,null);
	if (bd == null) return defaultValue;
	else return bd.getBigDecimal();
}
//===================================================================
public Decimal getField(int fieldID,Decimal dest)
//===================================================================
{
	if (dest == null) dest = new Decimal();
	return (Decimal)getFieldValue(fieldID,DECIMAL,dest);
}
//===================================================================
public int[] getAssignedFields()
//===================================================================
{
	int num = countAssignedFields();
	int [] ret = new int[num];
	getAssignedFields(ret,0);
	return ret;
}
/**
Return the field info as the id|type.
**/
//-------------------------------------------------------------------
protected int getFieldInfo(String fieldName)
//-------------------------------------------------------------------
{
	if (fields != null) {
		ewe.sys.Long found = (ewe.sys.Long)fields.get(fieldName);
		if (found != null) return (int)found.value;
	}
	int id = database.findField(fieldName);
	int type = id == 0 ? 0 : database.getFieldType(id);
	int got = ((id & 0xffff)<<16)|(type & 0xffff);
	if (fields == null) fields = new Hashtable();
	fields.put(fieldName,new ewe.sys.Long().set(got));
	return got;
}
//-------------------------------------------------------------------
protected int fieldToID(String fieldName)
//-------------------------------------------------------------------
{
	int got = getFieldInfo(fieldName);
	return (got >> 16) & 0xffff;
}
//-------------------------------------------------------------------
protected abstract int discoverType(int field);
//-------------------------------------------------------------------
//-------------------------------------------------------------------
protected String getFieldName(int id)
//-------------------------------------------------------------------
{
	return database.getFieldName(id);
}
//-------------------------------------------------------------------
protected int fieldToType(int field)
//-------------------------------------------------------------------
{
	int type = database.getFieldType(field);
	if (type != 0) return type;
	return discoverType(field);

}
//-------------------------------------------------------------------
protected String dump()
//-------------------------------------------------------------------
{
	int [] all = getAssignedFields();
	StringBuffer sb = new StringBuffer();
	if (all.length != 0)
		for (int i = 0; i<all.length; i++){
			sb.append(getFieldName(all[i])+" = ");
			Object fv = getFieldValue(all[i],fieldToType(all[i]),null);
			sb.append(""+fv);
			if (i != all.length-1) sb.append(", ");
		}
	else
		sb.append("<empty>");
	return sb.toString();
}
//===================================================================
public String toString()
//===================================================================
{
	return dump();
}
//===================================================================
public void duplicateFrom(DatabaseEntry other)
//===================================================================
{
	byte[] all = other.encode();
	decode(all,0,all.length);
}
//===================================================================
public byte[] encode()
//===================================================================
{
	try{
		ByteArray got = encode(null,null);
		return got.toBytes();
	}catch(Exception e){
		return null;
	}
}
//===================================================================
public boolean decode(byte[] source,int offset,int length)
//===================================================================
{
	try{
		decode(source,offset,length,null);
		return true;
	}catch(Exception e){
		return false;
	}
}
//##################################################################
}
//##################################################################

