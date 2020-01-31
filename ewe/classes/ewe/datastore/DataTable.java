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
import ewe.io.*;
import ewe.sys.*;
import ewe.reflect.*;
//##################################################################
public class DataTable extends StoredObject implements Stringable, Database{
//##################################################################

Vector fields = new Vector();
Vector sorts = new Vector();

Reflect objectClass;
DataEntry entries;

//===================================================================
DataEntry locateEntries(boolean forWriting) throws ewe.io.IOException
//===================================================================
{
	if (entries != null) return entries;
	if (entry == null || storage == null) return null;
	if (entries == null) entries = entry.find("Entries",false);
	if (entries == null)
			if (storage.getAllChildIds(entry,null).length == 0)
				try{
					entries = entry.find("Entries",true);
				}catch(Exception e){
				}
	if (entries == null) entries = entry;
	return entries;
}
//-------------------------------------------------------------------
private FieldSortEntry find(int id,Vector v)
//-------------------------------------------------------------------
{
	int sz = v.size();
	for (int i = 0; i<sz; i++){
		FieldSortEntry fe = (FieldSortEntry)v.get(i);
		if (fe.id == id) return fe;
	}
	return null;
}
//-------------------------------------------------------------------
private FieldSortEntry find(String name,Vector v)
//-------------------------------------------------------------------
{
	int sz = v.size();
	for (int i = 0; i<sz; i++){
		FieldSortEntry fe = (FieldSortEntry)v.get(i);
		if (fe.name.equalsIgnoreCase(name)) return fe;
	}
	return null;
}
//-------------------------------------------------------------------
private boolean deleteId(int id,Vector v)
//-------------------------------------------------------------------
{
	int sz = v.size();
	for (int i = 0; i<sz; i++){
		FieldSortEntry fe = (FieldSortEntry)v.get(i);
		if (fe.id == id){
			v.del(i);
			return true;
		}
	}
	return true;
}
//-------------------------------------------------------------------
private FieldSortEntry findField(int fieldID) {return find(fieldID,fields);}
private FieldSortEntry findSort(int fieldID) {return find(fieldID,sorts);}
//-------------------------------------------------------------------

//-------------------------------------------------------------------
private int findReservedFieldIndex(int fieldID)
//-------------------------------------------------------------------
{
	for (int i = 0; i<reservedFieldIDs.length; i++)
		if (reservedFieldIDs[i] == fieldID) return i;
	return -1;
}
/**
* This is used to set the class of the object used for data transfer to
* and from the table. If you call setFields(Object objectOrClass,String fields)
* then you do not need to call this method as it will be done for you.
* @param objectOrClass
*/
//===================================================================
public void setObjectClass(Object objectOrClass) throws IllegalArgumentException
//===================================================================
{
	objectClass = Reflect.toReflect(objectOrClass);
	if (objectClass == null) throw new IllegalArgumentException();
}
//===================================================================
public Reflect getObjectClass()
//===================================================================
{
	return objectClass;
}
//===================================================================
int toType(Wrapper w)
//===================================================================
{
	switch(w.getType()){
		case 'I': return INTEGER;
		case 'D': return DOUBLE;
		case 'Z': return BOOLEAN;
		case 'J': return LONG;
		case '[':
		case 'L':
			{
			Reflect r = Reflect.getForObject(w.getObject());
			if (r == null) return 0;
			if (r.getClassName().equals("[B")) return BYTE_ARRAY;
			if (r.isTypeOf("Ljava/lang/String;")) return STRING;
			if (r.isTypeOf("Lewe/sys/Time;")) return DATE_TIME;
			if (r.isTypeOf("Lewe/util/ByteArray;")) return BYTE_ARRAY;
		}
	}
	return 0;
}
//===================================================================
int toType(FieldTransfer ft,Object data)
//===================================================================
{
	String fieldType = ft.transferType;
	switch(fieldType.charAt(0)){
		case 'I': return INTEGER;
		case 'D': return DOUBLE;
		case 'Z': return BOOLEAN;
		case 'J': return LONG;
		case '[': if(fieldType.equals("[B")) return BYTE_ARRAY;
		case 'L':{
			if (fieldType.equals("Ljava/lang/String;")) return STRING;
			if (Reflect.isTypeOf(fieldType,"Lewe/sys/Time;")) return DATE_TIME;
			if (Reflect.isTypeOf(fieldType,"Lewe/util/ByteArray;")) return BYTE_ARRAY;
			/*
			if (Reflect.isTypeOf(fieldType,"Lewe/data/EditableData;")){
				if (data == null) return 0;
				ewe.data.EditableData obj = (ewe.data.EditableData)ft.getFieldValue(data);
				if (obj == null) return 0;
				Wrapper w = new Wrapper();
				obj.toSaveableData(w);
				return toType(w);
			}
			*/
		}
	}
	return 0;
}
//-------------------------------------------------------------------
FieldTransfer getFieldTransfer(String field,Object data,Object di) throws IllegalArgumentException
//-------------------------------------------------------------------
{
	if (objectClass == null) throw new IllegalArgumentException();
	FieldTransfer ft = new FieldTransfer(objectClass,data,field,di,null);
	if (ft.isValid())
		if (toType(ft,data) != 0)
			return ft;
	throw new IllegalArgumentException(field+" is not a valid field");
}
Vector fieldTransfers;

//===================================================================
public int [] setSorts(Object objectOrClass) throws IllegalArgumentException
//===================================================================
{
	String sorts = ewe.data.LiveObject.appendAllFields("_sorts",objectOrClass,false);
	return setSorts(objectOrClass,sorts);
}

//===================================================================
public int [] setSorts(Object objectOrClass,String sorts) throws IllegalArgumentException
//===================================================================
{
	String [] all = mString.split(sorts,'|');
	int [] ret = new int[all.length/2];
	for (int i = 0; i<all.length-1; i+=2){
		String name = ewe.util.mString.leftOf(all[i],'$');
		String opts = ewe.util.mString.rightOf(all[i],'$');
		int options = 0;
		for (int j = 0; j<opts.length(); j++)
			switch(opts.charAt(j)){
				case 'i': case 'I': options |= SORT_IGNORE_CASE; break;
				case 'd': case 'D': options |= SORT_DESCENDING; break;
				case 't': case 'T': options |= SORT_DATE_ONLY; break;
				case 'm': case 'M': options |= SORT_TIME_ONLY; break;
				case 'u': case 'U': options |= SORT_UNKNOWN_FIRST; break;
			}
		ret[i/2] = addSort(name,options,all[i+1]);
	}
	return ret;
}
/**
 * Set the fields for the DataTable, which must match fields in the objectOrClass
 * transfer object.
 * @param objectOrClass The object or class of the object to be used for data transfer.
 * @return an array of integers representing the field IDs.
 */
//===================================================================
public int [] setFields(Object objectOrClass) throws IllegalArgumentException
//===================================================================
{
	return setFields(objectOrClass,ewe.data.LiveObject.getFieldList(objectOrClass,false),null);
}
/**
 * Set the fields for the DataTable, which must match fields in the objectOrClass
 * transfer object.
 * @param objectOrClass The object or class of the object to be used for data transfer.
 * @param fields A comma separated list of fields. The types of these fields must be one of:
 <ul><li>int<li>long<li>double<li>boolean<li>String<li>Date<li>byte []</ul>
 * @return an array of integers representing the field IDs.
 */
//===================================================================
public int [] setFields(Object objectOrClass,String fields) throws IllegalArgumentException
//===================================================================
{
	return setFields(objectOrClass,fields,null);
}
/**
 * Set the fields for the DataTable, which must match fields in the objectOrClassOrReflect
 * transfer object.
 * @param objectOrClass The object or class of the object to be used for data transfer.
 * @param fields A comma separated list of fields. The types of these fields must be one of:
 <ul><li>int<li>long<li>double<li>boolean<li>String<li>Date<li>byte []</ul>
 * @param headers An optional comma separated list of headers.
 * @return an array of integers representing the field IDs.
 */
//===================================================================
public int [] setFields(Object objectOrClass,String fields,String headers) throws IllegalArgumentException
//===================================================================
{
	DatabaseEntry ded = new DataEntryData();
	setObjectClass(objectOrClass);
	if (fieldTransfers == null) fieldTransfers = new Vector();
	String [] s = mString.split(fields,',');
	String [] h = headers == null ? null : mString.split(headers,',');
	int [] ret = new int[s.length];
	Object data = Reflect.toNonReflect(objectOrClass);
	for (int i = 0; i<s.length; i++){
		FieldTransfer ft = getFieldTransfer(s[i],data,ded);
		fieldTransfers.add(ft);
		ret[i] = addField(s[i],toType(ft,data));
		//ewe.sys.Vm.debug("Field: "+s[i]+" is: "+getFieldType(ret[i]));
		if (h != null)
			findField(ret[i]).header = h[i];
		//ewe.sys.Vm.debug(s[i]+" = "+toType(ft));
	}
	return ret;
}
/**
* Find the FieldID of a named field. This is NOT case sensistive.
* @param fieldName The name of the field.
* @return The field id, or 0 if not found.
*/
//===================================================================
public int findField(String fieldName)
//===================================================================
{
	FieldSortEntry fe = find(fieldName,fields);
	if (fe != null) return fe.id;
	//......................................................
	// See if it is a reserved field.
	//......................................................
	for (int i = 0; i<reservedFieldNames.length; i++)
		if (reservedFieldNames[i].equalsIgnoreCase(fieldName))
			return reservedFieldIDs[i];
	return 0;
}

/**
* Similar to findField(), find the FieldID of a named field. This is NOT case sensistive.
* @param fieldName The name of the field.
* @return The field id.
* @exception IllegalArgumentException if the field is not found.
*/
//===================================================================
public int getField(String fieldName) throws IllegalArgumentException
//===================================================================
{
	int ret = findField(fieldName);
	if (ret == 0) throw new IllegalArgumentException("Field: "+fieldName+" not found.");
	return ret;
}
/**
* Get an array of fieldIds for an array of field names.
**/
//===================================================================
public int [] findFields(String [] fieldNames)
//===================================================================
{
	int [] ret = new int[fieldNames.length];
	for (int i = 0; i<ret.length; i++) ret[i] = findField(fieldNames[i]);
	return ret;
}
/**
* Get an array of fieldIds for a comma separated list of field names.
**/
//===================================================================
public int [] findFields(String fieldNames)
//===================================================================
{
	return findFields(mString.split(fieldNames,','));
}
//===================================================================
public String getFieldName(int fieldID)
//===================================================================
{
	FieldSortEntry t = findField(fieldID);
	if (t != null) return t.name;
	int idx = findReservedFieldIndex(fieldID);
	if (idx == -1) return null;
	return reservedFieldNames[idx];
}
//===================================================================
public String getFieldHeader(int fieldID)
//===================================================================
{
	FieldSortEntry t = findField(fieldID);
	if (t != null) return t.header;
	int idx = findReservedFieldIndex(fieldID);
	if (idx == -1) return null;
	return reservedFieldHeaders[idx];
}
//===================================================================
public boolean setFieldHeader(int fieldID,String newHeader)
//===================================================================
{
	FieldSortEntry t = findField(fieldID);
	if (t == null) return false;
	t.header = newHeader;
	return true;
}

/**
 * This converts a name with underscores to a header with capital letters and spaces where
 * the underscores were.
 * @param name
 * @return The converted name.
 */
//===================================================================
public static String nameToHeader(String name)
//===================================================================
{
	return ewe.ui.InputStack.nameToPrompt(name);
}
//===================================================================
public String getSortName(int sortID)
//===================================================================
{
	FieldSortEntry t = findSort(sortID);
	if (t != null) return t.name;
	return null;
}
//===================================================================
public int [] getSortFields(int sortID)
//===================================================================
{
	FieldSortEntry t = findSort(sortID);
	if (t != null) return null;
	IntArray ia = new IntArray();
	if (t.field1 != 0) ia.append(t.field1);
	if (t.field2 != 0) ia.append(t.field2);
	if (t.field3 != 0) ia.append(t.field3);
	if (t.field4 != 0) ia.append(t.field4);
	return ia.toIntArray();
}
//===================================================================
public int getSortOptions(int sortID)
//===================================================================
{
	FieldSortEntry t = findSort(sortID);
	if (t != null) return 0;
	return t.type;
}
//===================================================================
public int getFieldType(int fieldID)
//===================================================================
{
	FieldSortEntry t = findField(fieldID);
	if (t != null) return t.type;
	int idx = findReservedFieldIndex(fieldID);
	if (idx == -1) return 0;
	return reservedFieldTypes[idx];
}

//-------------------------------------------------------------------
private int [] getIds(Vector v)
//-------------------------------------------------------------------
{
	int sz = v.size();
	int [] ret = new int[sz];
	for (int i = 0; i<sz; i++)
		ret[i] = ((FieldSortEntry)v.get(i)).id;
	return ret;
}
//===================================================================
public int [] getFields() {return getIds(fields);}
public int [] getSorts() {return getIds(sorts);}
//===================================================================
//-------------------------------------------------------------------
private FieldSortEntry assignNewID(String name,int typeOptions,Vector dest,int max)
throws IllegalArgumentException
//-------------------------------------------------------------------
{
	if (find(name,dest) != null) throw new IllegalArgumentException("Field/Sort: "+name+" already used.");
	int fc = dest.size();
	if (fc < max)
		for (int i = fc+1; i>0; i--){
			if (find(i,dest) == null){
				FieldSortEntry fe = new FieldSortEntry();
				fe.name = name;
				fe.header = nameToHeader(name);
				fe.id = i;
				fe.type = typeOptions;
				dest.add(fe);
				return fe;
			}
		}
	throw new IllegalArgumentException("Too many fields/sorts.");
}
/**
* Add a new field. This will return the ID of the new field or throw an exception on failure.
* This will method will fail if:
* 1. An invalid fieldType is specified.
* 2. An already used fieldName is specified.
* 3. There are already the maximum of 240 fields.
**/
//===================================================================
public int addField(String fieldName,int fieldType) throws IllegalArgumentException
//===================================================================
{
	if (fieldType <= 0) throw new IllegalArgumentException();
	FieldSortEntry fe = assignNewID(fieldName,fieldType,fields,240);
	if (fe == null) return 0;
	return fe.id;
}

//===================================================================
public int findSort(String sortName)
//===================================================================
{
	FieldSortEntry fe = find(sortName,sorts);
	if (fe != null) return fe.id;
	else return 0;
}
/**
* Create a new sort criteria. Returns the ID of the new sort.
**/
//===================================================================
public int addSort(String sortName,int options,int field) throws IllegalArgumentException
//===================================================================
{
	return addSort(sortName,options,field,0,0,0);
}
/**
* Create a new sort criteria allowing you to specify up to four fields.
* Set any unused sort fields to zero. Returns the ID of the new sort.
**/
//===================================================================
public int addSort(String sortName,int options,int field1,int field2,int field3,int field4)
throws IllegalArgumentException
//===================================================================
{
	FieldSortEntry fe = assignNewID(sortName,options,sorts,240);
	if (fe == null) return 0;
	fe.field1 = field1;
	fe.field2 = field2;
	fe.field3 = field3;
	fe.field4 = field4;
	return fe.id;
}
/**
* Create a new sort criteria allowing you to specify up to four fields.
 * @param sortName The name of the sort.
 * @param options Sorting options.
 * @param fieldList A comma separated field list.
 * @return The ID of the new sort.
 * @exception IllegalArgumentException if any of the fields could not be found, or if too many
 * fields are specified.
 */
//===================================================================
public int addSort(String sortName,int options,String fieldList) throws IllegalArgumentException
//===================================================================
{
	int [] all = findFields(fieldList);
	if (all.length > 4) throw new IllegalArgumentException("Only four fields can be specified for sorting.");
	for (int i = 0; i<all.length; i++)
		if (all[i] == 0) throw new IllegalArgumentException("Illegal field specified.");
	return addSort(sortName,options,
		all.length < 1 ? 0 : all[0],
		all.length < 2 ? 0 : all[1],
		all.length < 3 ? 0 : all[2],
		all.length < 4 ? 0 : all[3]);
}
/**
* Note that if this DataTable is a live one (i.e. it exists within an open database), then
* calling this method may take time as all of the records in the table will have this
* field removed from them. This does not happen with addField().
**/
//===================================================================
public boolean removeField(int fieldID)
//===================================================================
{
	if (!deleteId(fieldID,fields)) return false;
	if (entry != null){
		IntArray ia = storage.getAllChildIds(entry,null);
		for (int i = 0; i<ia.length; i++)
			DatabaseEntry.deleteFieldFromFile(fieldID,storage.stream,ia.data[i]);
	}
	return true;
}
//===================================================================
public boolean removeSort(int sortID) {return deleteId(sortID,sorts);}
//===================================================================
//===================================================================
public DatabaseEntry getNewData() {return getNewData(null,null);}
//===================================================================
//===================================================================
public DatabaseEntry getNewData(ByteArray buffer,DatabaseEntry dest)
//===================================================================
{
	if (buffer == null) buffer = new ByteArray();
	if (dest == null) dest = new DataEntryData();
	dest.set(this,buffer,0);
	dest.clearBuffer();
	return dest;
}
/*
//-------------------------------------------------------------------
DatabaseEntry readData(DataEntry location,ByteArray buffer,DatabaseEntry dest)
//-------------------------------------------------------------------
{
	if (buffer == null) buffer = new ByteArray();
	buffer.clear();
	if (dest == null) dest = new DatabaseEntry();
	dest.set(this,buffer,location.myLocation);
	if (location.dataLocation == 0) dest.clearBuffer();
	else {
		location.readData(buffer);
		dest.decodeBuffer();
	}
	return dest;
}
//===================================================================
public DatabaseEntry getData(int id,ByteArray buffer,DatabaseEntry dest)
//===================================================================
{
	DataEntry de = storage.getEntryAt(id);
	if (de == null) return null;
	return readData(de,buffer,dest);
}
*/
//===================================================================
public DatabaseEntry getData(int id,ByteArray buffer,DatabaseEntry dest) throws ewe.io.IOException
//===================================================================
{
	if (buffer == null) buffer = new ByteArray();
	buffer.clear();
	if (dest == null) dest = new DataEntryData();
	dest.set(this,buffer,id);
	if (storage.getDataEntryData(id,buffer) == null) storage.throwException("Error reading database");
	dest.decodeBuffer();
	/*
	String debug = "";
	if (dest.hasField(OID_FIELD)){
		ewe.sys.Vm.debug(""+dest.getField(OID_FIELD,(long)0));
	}
	*/
	/*
	String debug = "";
	Time t = new Time();
	t.format = "dd-MMM-yy hh:mm:ss";
	if (hasField(CREATED_FIELD)){
		debug += dest.getField(CREATED_FIELD,t);
		debug += " ";
	}
	if (hasField(MODIFIED_FIELD)){
		debug += dest.getField(MODIFIED_FIELD,t);
		debug += " ";
	}
	if (debug.length() != 0) ewe.sys.Vm.debug(debug);
	else ewe.sys.Vm.debug("Nope!");
	*/
	return dest;
}
//===================================================================
public Object getFieldData(int id,int fieldID,Object dest) throws ewe.io.IOException
//===================================================================
{
	DatabaseEntry ded = getData(id,null,null);
	if (ded == null) return null;
	return ded.getFieldValue(fieldID,dest);
}
//-------------------------------------------------------------------
DataEntry getNewEntry() throws ewe.io.IOException
//-------------------------------------------------------------------
{
	DataEntry where = locateEntries(true);
	if (where == null) throw new RuntimeException("This DataTable is not stored in a DataStorage");
	DataEntry ret = where.makeNewChild(true);
	if (ret == null) storage.throwException("Couldn't create a new entry in database");
	return ret;
}

//-------------------------------------------------------------------
boolean hasField(int fieldID)
//-------------------------------------------------------------------
{
	return findField(fieldID) != null;
}

DataTableSpecs specs = null;
DataEntry specsEntry = null;

//-------------------------------------------------------------------
private DataTableSpecs getSpecs() throws ewe.io.IOException
//-------------------------------------------------------------------
{
	if (specs == null && entry != null){
		specsEntry = entry.find("Specs",false);
		DataTableSpecs ds = new DataTableSpecs();
		if (specsEntry == null){
			specsEntry = entry.find("Specs",true);
			ds.myOID = ds.getNewOID();
			specsEntry.saveObject("Specs",ds);
			specs = ds;
		}else{
			specsEntry.loadObject(ds);
			specs = ds;
		}
	}
	return specs;
}
//-------------------------------------------------------------------
private void saveSpecs() throws ewe.io.IOException
//-------------------------------------------------------------------
{
	if (specs == null) return;
	specsEntry.saveObject("Specs",specs);
}
//===================================================================
public long getNewOID() throws ewe.io.IOException
//===================================================================
{
	Time t = new Time();
	int val = (t.month*31+t.day)*24*60*60;
	val += t.hour*60*60;
	val += t.minute*60;
	val += t.second;
	val <<= 5;
	val |= ((int)(java.lang.Math.random() * 32)) % 32;

	int val2 = (t.millis << 22) | (int)(4194304 * java.lang.Math.random());
	return (long)val << 32 | (long)val2;
/*
	specs = getSpecs();
	if (specs == null) return 0;
	specs.lastAssigned++;
	specsEntry.saveObject("Specs",specs);
	return (long)specs.myOID << 32 | (long)specs.lastAssigned;
	*/
}
//===================================================================
public int getIdentifier() throws ewe.io.IOException
//===================================================================
{
	specs = getSpecs();
	if (specs == null) return 0;
	return specs.myOID;
}
//===================================================================
public void setSynchronizedTime(int remoteDatabaseID,ewe.sys.Time syncTime) throws ewe.io.IOException
//===================================================================
{
	specs = getSpecs();
	if (specs == null) return;
	specs.setSynchronizedTime(remoteDatabaseID,syncTime);
	saveSpecs();
}
//===================================================================
public ewe.sys.Time getSynchronizedTime(int remoteDatabaseID) throws ewe.io.IOException
//===================================================================
{
	specs = getSpecs();
	if (specs == null) return null;
	return specs.getSynchronizedTime(remoteDatabaseID);
}

//===================================================================
public void storeEntry(DatabaseEntry entry) throws ewe.io.IOException
//===================================================================
{
	boolean isNew = entry.stored == 0;
	DataEntry de = isNew ? getNewEntry() : storage.getEntryAt(entry.stored);
	entry.stored = de.id();
	DataStorage.test(de.writeData(entry.buffer.data,0,entry.buffer.length),storage,false);
}
//===================================================================
public void saveEntry(DatabaseEntry entry) throws ewe.io.IOException
//===================================================================
{
	boolean isNew = entry.stored == 0;
	Time atime = new Time();
	if (hasField(FLAGS_FIELD)){
		int value = entry.getField(FLAGS_FIELD,0);
		value &= ~FLAG_SYNCHRONIZED;
		entry.setField(FLAGS_FIELD,value);
	}
	if (hasField(MODIFIED_FIELD)){
		entry.setField(MODIFIED_FIELD,atime);
	}
	if (hasField(MODIFIED_BY_FIELD)){
		entry.setField(MODIFIED_BY_FIELD,getIdentifier());
	}
	if (isNew){
		if (hasField(OID_FIELD))
			entry.setField(OID_FIELD,getNewOID());
		if (hasField(CREATED_FIELD))
			entry.setField(CREATED_FIELD,atime);
	}
	storeEntry(entry);
}
//===================================================================
public void deleteEntry(DatabaseEntry entry) throws ewe.io.IOException
//===================================================================
{
	if (entry.stored == 0 || this.entry == null) return;
	long oid = entry.hasField(OID_FIELD) ? entry.getField(OID_FIELD,(long)0) : -1;
	eraseEntry(entry);
	if (oid != -1){
		DataEntry deleted = this.entry.find("Deleted",true);
		entry = getNewData();
		entry.setField(OID_FIELD,oid);
		entry.setField(MODIFIED_FIELD,new Time());
		DataEntry de = deleted.makeNewChild(true);
		DataStorage.test(de.writeData(entry.buffer.data,0,entry.buffer.length),storage,false);
	}
}
//===================================================================
public long [] getDeletedSince(ewe.sys.Time t) throws ewe.io.IOException
//===================================================================
{
	if (entry == null) return new long[0];
	Vector v = new Vector();
	DataEntry deleted = this.entry.find("Deleted",true);
	for (DataEntry de = deleted.getFirstChild(); de != null; de = de.getNext()){
		DatabaseEntry dbe = de.getData();
		if (!dbe.hasField(OID_FIELD)) continue;
		if (t != null)
			if (dbe.hasField(MODIFIED_FIELD))
				if (t.compareTo((Time)dbe.getField(MODIFIED_FIELD,new Time())) >= 0)
					continue;
		v.add(new ewe.sys.Long().set(dbe.getField(OID_FIELD,(long)0)));
	}
	long [] ret = new long[v.size()];
	for (int i = 0; i<ret.length; i++)
		ret[i] = ((ewe.sys.Long)v.get(i)).value;
	return ret;
}
//===================================================================
public DatabaseEntry getDeletedEntry(long OID) throws ewe.io.IOException
//===================================================================
{
	if (entry == null) return null;
	DataEntry deleted = this.entry.find("Deleted",true);
	for (DataEntry de = deleted.getFirstChild(); de != null; de = de.getNext()){
		DatabaseEntry dbe = de.getData();
		if (!dbe.hasField(OID_FIELD)) continue;
		if (dbe.getField(OID_FIELD,(long)0) == OID)
			return dbe;
	}
	return null;
}
//===================================================================
public void eraseEntry(DatabaseEntry entry) throws ewe.io.IOException
//===================================================================
{
	if (entry.stored == 0) return;
	DataEntry de = storage.getEntryAt(entry.stored);
	DataStorage.test(de.delete(),storage,false);
}
//int doSortFields(SortData sd,WObject sortEntry,WObject locale,WObject stream,WObject intArray)

protected static boolean hasNative = true;
/**
* This is made public for convenience but you would not likely use it directly. Instead
* you would use the reSort() method of the FoundEntries object.
**/
//===================================================================
public int sortFieldData(int sortID,ewe.sys.Locale locale,FoundEntries entries) throws ewe.io.IOException
//===================================================================
{
	FieldSortEntry fse = find(sortID,sorts);
	entries.fs = fse;
	if (fse == null) return 1;
	fse.type1 = getFieldType(fse.field1);
	fse.type2 = getFieldType(fse.field2);
	fse.type3 = getFieldType(fse.field3);
	fse.type4 = getFieldType(fse.field4);
	if ((storage.stream instanceof RandomAccessFile) && hasNative){
		try{
			native_sortFieldData(fse,getLocale(),(RandomAccessFile)storage.stream,entries.ids,storage.decryptor);
			return 1;
		}catch(Error e){
			hasNative = false;
		}catch(SecurityException e){
			hasNative = false;
		}
	}
	DatabaseFieldComparer dfc = new DatabaseFieldComparer(this,fse);
	Utils.sort(entries.ids.data,entries.ids.length,dfc,(fse.type & SORT_DESCENDING) != 0);
	if (dfc.error != null) throw dfc.error;
	return 1;
}
//-------------------------------------------------------------------
native int native_sortFieldData(FieldSortEntry fse,ewe.sys.Locale locale,RandomAccessFile stream,IntArray dest,DataProcessor decryptor);
//-------------------------------------------------------------------

/**
* This returns an empty FoundEntries object.
**/
//===================================================================
public FoundEntries getEmptyEntries()
//===================================================================
{
	FoundEntries found = new FoundEntries();
	found.table = this;
	return found;
}
//===================================================================
public FoundEntries getEntries(int sortID) throws ewe.io.IOException
//===================================================================
{
	DataEntry entry = locateEntries(false);
	FoundEntries found = new FoundEntries();
	found.table = this;
	entry.getAllChildIds(found.ids);
	sortFieldData(sortID,getLocale(),found);
	return found;
}
//===================================================================
public FoundEntries getEntries(int sortID,Object searchData,Comparer comparer) throws ewe.io.IOException
//===================================================================
{
	FoundEntries found = getEntries(sortID);
	if (searchData == null) return found;
	if (comparer == null) comparer = found.getFieldComparer();
	return found.getSubSet(found.findAll(searchData,comparer,null));
}
//===================================================================
public FoundEntries getEntries(int sortID,ObjectFinder finder) throws ewe.io.IOException
//===================================================================
{
	FoundEntries found = getEntries(sortID);
	if (finder == null) return found;
	return found.getSubSet(found.filterAll(finder,null));
}
//-------------------------------------------------------------------
private String outputFieldSort(Vector v,boolean sorts)
//-------------------------------------------------------------------
{
	String out = "";
	for (Iterator it = v.iterator(); it.hasNext();){
		FieldSortEntry fe = (FieldSortEntry)it.next();
		out += (fe.name+"�"+fe.header)+"|"+fe.id+"|"+fe.type+"|";
		out += fe.field1+"|"+fe.field2+"|"+fe.field3+"|"+fe.field4+"|";
	}
	return out;
}
//-------------------------------------------------------------------
private void inputFieldSort(Vector v,boolean sorts,String str)
//-------------------------------------------------------------------
{
	String [] fds = mString.split(str,'|');
	int add = 3;
	if (sorts || true) add += 4;
	v.clear();
	for (int i = 0; i<fds.length+1-add; i += add){
		FieldSortEntry fse = new FieldSortEntry();
		fse.name = mString.leftOf(fds[i],'�');
		fse.header = mString.rightOf(fds[i],'�');
		if (fse.header.length() == 0) fse.header = fse.name;
		fse.id = ewe.sys.Convert.toInt(fds[i+1]);
		fse.type = ewe.sys.Convert.toInt(fds[i+2]);
		if (sorts){
			fse.field1 = ewe.sys.Convert.toInt(fds[i+3]);
			fse.field2 = ewe.sys.Convert.toInt(fds[i+4]);
			fse.field3 = ewe.sys.Convert.toInt(fds[i+5]);
			fse.field4 = ewe.sys.Convert.toInt(fds[i+6]);
		}
		if (fse.id == 0) continue;
		v.add(fse);
	}
}
//===================================================================
public String textEncode()
//===================================================================
{
	return toString();
}
//===================================================================
public void textDecode(String from)
//===================================================================
{
	fromString(from);
}
//===================================================================
public String toString()
//===================================================================
{
	TextEncoder te = new TextEncoder();
	te.addValue("Fields",outputFieldSort(fields,false));
	te.addValue("Sorts",outputFieldSort(sorts,true));
	if (objectClass != null) te.addValue("ObjectClass",objectClass.getClassName());
	return te.toString();
}
//===================================================================
public void fromString(String str)
//===================================================================
{
	TextDecoder td = new TextDecoder(str);
	inputFieldSort(fields,false,td.getValue("Fields"));
	inputFieldSort(sorts,true,td.getValue("Sorts"));
	fieldTransfers = null;
	String oc = td.getValue("ObjectClass");
	if (oc != null){
		if ((objectClass = Reflect.getForName(oc)) != null){
			Object got = objectClass.newInstance();
			fieldTransfers = new Vector();
			int [] all = getFields();
			DatabaseEntry ded = new DataEntryData();
			for (int i = 0; i<all.length; i++){
				try{
					FieldTransfer ft = getFieldTransfer(getFieldName(all[i]),got,ded);
					if (toType(ft,got) == getFieldType(all[i])){
						fieldTransfers.add(ft);
					}
				}catch(IllegalArgumentException e){
				}
			}
		}
	}
}
//-------------------------------------------------------------------
Iterator prepareFieldTransfer(Object data)
//-------------------------------------------------------------------
{
	Reflect r = Reflect.getForObject(data);
	if (!Reflect.getForObject(data).isTypeOf(objectClass)){
		throw new RuntimeException("The objectClass for the DataTable has not been set or is incompatible with the data object.");
	}
	if (fieldTransfers == null) new ObjectIterator(null);
	return fieldTransfers.iterator();
}
//-------------------------------------------------------------------
void getSetFields(DatabaseEntry ded,Object data,boolean isGet)
//-------------------------------------------------------------------
{
	for (Iterator i = prepareFieldTransfer(data); i.hasNext();){
		FieldTransfer ft = (FieldTransfer)i.next();
		ft.dataInterface = ded;
		try{
			ft.transfer(data,isGet ? ft.TO_OBJECT : ft.FROM_OBJECT);
		}catch(Exception e){
			String s = ewe.sys.Vm.getStackTrace(e,4);
			ewe.sys.Vm.debug(ft.fieldName+": "+s);
		}
	}
}
/**
 * Sets the fields in the DatabaseEntry from the fields in the data object - which must not be null.
 * @param ded The DatabaseEntry to set.
 * @param data The data object, which must be of the type objectClass or an exception will be thrown.
 */
//-------------------------------------------------------------------
public void setData(DatabaseEntry ded,Object data)
//-------------------------------------------------------------------
{
	getSetFields(ded,data,false);
}
/**
 * Gets the fields from the DatabaseEntry to the fields in the data object - which must not be null.
 * @param ded The DatabaseEntry to set.
 * @param data The data object, which must be of the type objectClass or an exception will be thrown.
 */
//-------------------------------------------------------------------
public Object getData(DatabaseEntry ded,Object data)
//-------------------------------------------------------------------
{
	if (data == null && objectClass != null) data = objectClass.newInstance();
	if (data == null || objectClass == null) throw new IllegalStateException();
	getSetFields(ded,data,true);
	return data;
}

//-------------------------------------------------------------------
DataTable(DataStorage storage,DataEntry entry)
//-------------------------------------------------------------------
{
	this.storage = storage;
	this.entry = entry;
}
//-------------------------------------------------------------------
public DataTable() {this(null,null);}
//-------------------------------------------------------------------

//===================================================================
public String printField(int fieldID)
//===================================================================
{
	String name = getFieldName(fieldID);
	if (name == null) return "-- null --";
	return fieldID+" = "+name+", "+getFieldType(fieldID);
}

//===================================================================
public String printAllFields()
//===================================================================
{
	StringBuffer sb = new StringBuffer();
	int [] got = getFields();
	for (int i = 0; i<got.length; i++){
		sb.append(printField(got[i]));
		sb.append("\n");
	}
	return sb.toString();
}
/*
//===================================================================
public static void main(String args[])
//===================================================================
{
	ewe.sys.Vm.startEwe(args);
	DataTable dt = new DataTable();
	Object data = new tests.LiveTest();
	dt.setFields(data);
	String s = dt.toString();
	dt = new DataTable();
	dt.fromString(s);
	DatabaseEntry ded = dt.getNewData();
	dt.setData(ded,data);
}
*/

//===================================================================
public void close() throws ewe.io.IOException
//===================================================================
{
	storage.close();
}

//===================================================================
public void addSpecialField(int id) throws IllegalArgumentException
//===================================================================
{
	if (find(id,fields) != null) return;
	int idx = findReservedFieldIndex(id);
	if (idx == -1) throw new IllegalArgumentException();
	FieldSortEntry fe = new FieldSortEntry();
	fe.id = id; fe.type = reservedFieldTypes[idx];
	fe.name = reservedFieldNames[idx];
	fields.add(fe);
}
//===================================================================
public void enableSynchronization(int syncOptions) throws ewe.io.IOException
//===================================================================
{
	addSpecialField(OID_FIELD);
	addSort(OidSortName,0,OID_FIELD);
	addSpecialField(FLAGS_FIELD);
	addSort(SyncSortName,0,FLAGS_FIELD,OID_FIELD,0,0);

	if ((syncOptions & SYNC_STORE_CREATION_DATE) != 0){
		addSpecialField(CREATED_FIELD);
		addSort(CreatedSortName,0,CREATED_FIELD,OID_FIELD,0,0);
	}

	if ((syncOptions & SYNC_STORE_MODIFICATION_DATE) != 0) {
		addSpecialField(MODIFIED_FIELD);
		addSort(ModifiedSortName,0,MODIFIED_FIELD,OID_FIELD,0,0);
	}

	if ((syncOptions & SYNC_STORE_MODIFIED_BY) != 0) {
		addSpecialField(MODIFIED_BY_FIELD);
		addSort(ModifiedBySortName,0,MODIFIED_BY_FIELD,OID_FIELD,0,0);
	}

	save();
}
//===================================================================
public void delete() throws IOException
//===================================================================
{
	if (storage != null) storage.delete();
	else throw new IOException("Could not delete database.");
}
//===================================================================
public void rename(String newName) throws IOException
//===================================================================
{
	if (storage != null) storage.rename(newName);
	else throw new IOException("Could not rename database.");
}
//##################################################################
}
//##################################################################


