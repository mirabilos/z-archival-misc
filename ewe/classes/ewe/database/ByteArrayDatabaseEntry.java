/* $MirOS: contrib/hosted/ewe/classes/ewe/database/ByteArrayDatabaseEntry.java,v 1.2 2008/05/02 20:51:59 tg Exp $ */

/*********************************************************************************
 *  Ewe Virtual Machine - Version 1.14, January 24, 2002                         *
 *  Copyright (c) 2008 Thorsten “mirabilos” Glaser                               *
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
import ewe.io.DataProcessor;
import ewe.io.IO;
import ewe.io.IOException;
import ewe.math.BigDecimal;
import ewe.sys.Convert;
import ewe.sys.DayOfYear;
import ewe.sys.Decimal;
import ewe.sys.Locale;
import ewe.sys.TimeOfDay;
import ewe.sys.Vm;
import ewe.util.ByteArray;
import ewe.util.ByteEncoder;
import ewe.util.CharArray;
import ewe.util.SubString;
import ewe.util.Utils;
/*
* Each record is a sequence of fields. Each field is:
* <p>
* byte 1 = Type/Flags; Bits 0-3 = Type of data, Bit 7 and 5 = Must be zero, Bits 6 and 4 must be 1. A value
* of zero for all bits indicates the end of record marker.
* byte 2,3 = ID; A value of 0 indicates an invalid or deleted field.<br>
* <p>
* If the first byte in the record data has Bit 7 set, then the record is assumed to be encrypted
* and the length of the encrypted data will be given by the first four bytes (including the first byte)
* with the top bit clear. After decrypting the data the first byte must be valid (i.e. 0101<type>). If it is
* not it is considered an error.
* <p>
*/

//##################################################################
public abstract class ByteArrayDatabaseEntry extends DatabaseEntryObject {
//##################################################################
static byte [] buffer = new byte[8];

protected final static int TYPE_MASK = 0xf;

//-------------------------------------------------------------------
protected ByteArrayDatabaseEntry(DatabaseObject database)
//-------------------------------------------------------------------
{
	super(database);
}

//-------------------------------------------------------------------
private int getFullSize(int type, int location)
//-------------------------------------------------------------------
{
		switch(type & TYPE_MASK){
			case BOOLEAN: return 4;
			case INTEGER: return 7;
			case DOUBLE:
			case DATE_TIME: case DATE: case TIME: case TIMESTAMP:
			case LONG: return 11;
			case STRING: return 3+2+Utils.readInt(myData.data,location+3,2);
			case DECIMAL:
			case BYTE_ARRAY:
			case JAVA_OBJECT:
				int size = Utils.readInt(myData.data,location+3,4);
				if (size == -1) size = 0;
				return 7+size;
			default: return 0;
		}
}
//===================================================================
public void decode(byte[] source,int offset,int length,DataProcessor decryptor) throws IOException
//===================================================================
{
	if (myData == null) myData = new ByteArray();
	try{
		if ((source[0] & 0x80) != 0){
			if (decryptor == null) throw new EncryptedDataException();
			myData.clear();
			int len = Utils.readInt(source,offset,4) & 0x7fffffff;
			try{
				IO.processAll(decryptor,source,offset+4,len,myData);
			}catch(IOException e){
				throw new EncryptedDataException();
			}
		}else{
			myData.clear();
			myData.append(source,offset,length);
		}
		decodeRecord();
	}catch(IndexOutOfBoundsException e){
		throw new BadRecordFormatException();
	}
}

//-------------------------------------------------------------------
protected int locateField(int id, int idType)
//-------------------------------------------------------------------
{
	if (myData == null) clear();
	try{
		int max = myData.length;
		byte[] data = myData.data;
		for (int i = 0; i < max;){
			int type = data[i] & TYPE_MASK;
			if (type == 0) break; //End of record.
			if (((data[i+1] << 8 | data[i+2]) & 0xffff) == id)
				if (idType == 0 || type == idType) return i;
			i += getFullSize(type,i);
		}
		return -1;
	}catch(IndexOutOfBoundsException e){
		return -1;
	}
}
//===================================================================
public void clearFields()
//===================================================================
{
	if (myData == null) clear();
	try{
		for (int i = 0; i < myData.length;){
			int type = myData.data[i] & TYPE_MASK;
			if (type == 0) break; //End of record.
			int id = ((myData.data[i+1] << 8 | myData.data[i+2]) & 0xffff);
			if (id >= FIRST_SPECIAL_FIELD) i += getFullSize(type,i);
			else deleteFieldAt(i);
		}
	}catch(IndexOutOfBoundsException e){
	}
}
//===================================================================
public void clearField(int fieldID)
//===================================================================
{
	int where = locateField(fieldID,0);
	if (where == -1) return;
	deleteFieldAt(where);
}
//===================================================================
public boolean hasField(int fieldID)
//===================================================================
{
	return locateField(fieldID,0) != -1;
}
//===================================================================
public int countAssignedFields()
//===================================================================
{
		if (myData == null) clear();
		int max = myData.length;
		byte[] data = myData.data;
		int count = 0;
		for (int i = 0; i < max;){
			int type = data[i] & TYPE_MASK;
			if (type == 0) break; //End of record.
			if (data[i+1] != 0 || data[i+2] != 0) count++;
			i += getFullSize(type,i);
		}
		return count;
}
//===================================================================
public int getAssignedFields(int[] dest, int offset)
//===================================================================
{
	if (myData == null) clear();
	int max = myData.length;
	byte[] data = myData.data;
	int count = 0;
	for (int i = 0; i < max;){
		int type = data[i] & TYPE_MASK;
		if (type == 0) break; //End of record.
		if (data[i+1] != 0 || data[i+2] != 0)
			dest[offset+(count++)] = ((myData.data[i+1] << 8 | myData.data[i+2]) & 0xffff);
		i += getFullSize(type,i);
	}
	return count;
}

//-------------------------------------------------------------------
private void deleteFieldAt(int location)
//-------------------------------------------------------------------
{
	int max = myData.length;
	byte[] data = myData.data;
	int type = data[location] & TYPE_MASK;
	int toDelete = getFullSize(type,location);
	if (toDelete != 0){
		Vm.copyArray(data,location+toDelete,data,location,max-location-toDelete);
		myData.length -= toDelete;
	}
}
//-------------------------------------------------------------------
private int typeAt(int location)
//-------------------------------------------------------------------
{
	if (location == -1) return 0;
	return myData.data[location] & TYPE_MASK;
}

//-------------------------------------------------------------------
protected int discoverType(int id)
//-------------------------------------------------------------------
{
	return typeAt(locateField(id,0));
}

//-------------------------------------------------------------------
private ByteArray clear()
//-------------------------------------------------------------------
{
	if (myData == null) myData = new ByteArray();
	myData.clear();
	buffer[0] = 0;
	myData.append(buffer,0,1);
	return myData;
}
//===================================================================
public void clearDataAndSpecialFields()
//===================================================================
{
	clear();
}
//===================================================================
public void setFieldValue(int id, int type, Object value)
//===================================================================
{
	int where = locateField(id,0), need = 0;
	if (where != -1) deleteFieldAt(where);
	//
	// myData.length should never be 0, there should always be at least a single
	// 0 value in it.
	//
	if (myData.length != 0) myData.length--;
	buffer[0] = (byte) ((type & TYPE_MASK)|0x50);
	buffer[1] = (byte)(id >> 8);
	buffer[2] = (byte)(id & 0xff);
	myData.append(buffer,0,3);
	value = convertStringToStaticObjectValue(value,type);
	ewe.sys.Long ln = value instanceof ewe.sys.Long ? (ewe.sys.Long)value : null;
	int toAppend = 0;
	switch(type){
		case BOOLEAN: buffer[0] = (byte)ln.value; toAppend = 1; break;
		case INTEGER: Utils.writeInt((int)ln.value,buffer,0,4); toAppend = 4; break;
		case LONG: Utils.writeLong(ln.value,buffer,0); toAppend = 8; break;
		case DATE_TIME: case TIME: case DATE: case TIMESTAMP:
			Utils.writeLong( value == null ? -1L: ((ewe.sys.Time)value).getEncodedTime(),buffer,0); toAppend = 8; break;
		case DOUBLE: Utils.writeLong(ewe.sys.Convert.toLongBitwise(((ewe.sys.Double)value).value),buffer,0); toAppend = 8; break;

		case JAVA_OBJECT:
			if (value != null){
				ByteArray ba = byteArray;
				ba.clear();
				ByteEncoder.encodeObject(ba,value);
				value = ba;
			}
			/* FALLTHROUGH */
		case BYTE_ARRAY:
			ByteArray ba = value instanceof ByteArray ? (ByteArray)value : null;
			Utils.writeInt(ba == null ? -1 : ba.length,buffer,0,4);
			myData.append(buffer,0,4);
			if (ba != null) myData.append(ba.data,0,ba.length);
			break;
		case DECIMAL:
			BigDecimal bg =
				(value instanceof BigDecimal) ? (BigDecimal)value : ((Decimal)value).getBigDecimal();
			//if (bg == null) bg = bg.valueOf(0);
			need = bg.write(null,0);
			//need = bg == null ? -1 : bg.write(null,0);
			Utils.writeInt(need,buffer,0,4);
			myData.append(buffer,0,4);
			if (need > 0){
				int putHere = myData.length;
				myData.makeSpace(myData.length,need);
				bg.write(myData.data,putHere);
			}
			break;
		case STRING:
			SubString ss = null;
			if (value instanceof String){
				String s = (String)value;
				ss = substring;
				ss.data = ewe.sys.Vm.getStringChars(s);
				ss.start = 0;
				ss.length = s.length();
			}else if (value instanceof CharArray){
				CharArray c = (CharArray)value;
				ss = substring;
				ss.data = c.data;
				ss.start = 0;
				ss.length = c.length;
			}else if (value instanceof SubString){
				ss = (SubString)value;
			}else{
				String s = "";
				ss = substring;
				ss.data = ewe.sys.Vm.getStringChars(s);
				ss.start = 0;
				ss.length = s.length();
			}
			need =
				ss == null ?
					EncodedUTF8String.store(null,0,0,null,0):
					EncodedUTF8String.store(ss.data,ss.start,ss.length,null,0);
			int putHere = myData.length;
			myData.makeSpace(myData.length,need);
			if (ss == null)
					EncodedUTF8String.store(null,0,0,myData.data,putHere);
			else
					EncodedUTF8String.store(ss.data,ss.start,ss.length,myData.data,putHere);
			break;
	}
	if (toAppend != 0) myData.append(buffer,0,toAppend);
	buffer[0] = 0;
	myData.append(buffer,0,1);
}
//===================================================================
public Object getFieldValue(int id, int type, Object dest)
//===================================================================
{
	int where = locateField(id,type);
	if (where == -1) return null;
	byte [] data = myData.data;
	where += 3;
	if (dest == null){
		switch(type){
			case BOOLEAN: case INTEGER: case LONG:
				dest = new ewe.sys.Long(); break;
			case DOUBLE:
				dest = new ewe.sys.Double(); break;
		}
	}
	ewe.sys.Long ln = dest instanceof ewe.sys.Long ? (ewe.sys.Long)dest : null;
	long value;
	switch(type){
		case BOOLEAN: ln.set(data[where]); return ln;
		case INTEGER: ln.set(Utils.readInt(data,where,4)); return ln;
		case LONG:
			ln.set(Utils.readLong(data,where));
			return ln;
		case DATE_TIME: case DATE: case TIME: case TIMESTAMP:
			value = Utils.readLong(data,where);
			if (value == -1) return null;
			if (dest == null) {
				if (type == DATE_TIME)
					dest = new ewe.sys.Time();
				else if (type == DATE)
					dest = new DayOfYear();
				else if (type == TIME)
					dest = new TimeOfDay();
				else
					dest = new TimeStamp();
			}
			((ewe.sys.Time)dest).setEncodedTime(value);
			return dest;
		case DOUBLE:
			value = Utils.readLong(data,where);
			((ewe.sys.Double)dest).set(ewe.sys.Convert.toDoubleBitwise(value));
			return dest;
		case BYTE_ARRAY:
			int size = Utils.readInt(data,where,4);
			if (dest == null) dest = new ByteArray();
			ByteArray ba = dest instanceof ByteArray ? (ByteArray)dest : byteArray;
			if (size == -1) ba = null;
			else{
				ba.clear();
				ba.append(data,where+4,size);
			}
			return ba;
		case JAVA_OBJECT:
			size = Utils.readInt(data,where,4);
			if (size == -1) return null;
			try{
				return ByteEncoder.decodeObject(data,where+4,size,dest);
			}catch(Exception e){
				return null;
			}
		case DECIMAL:
			size = Utils.readInt(data,where,4);
			if (size <= 0) return null;
			else {
				BigDecimal bd = new BigDecimal(data,where+4,size);
				if (dest == null) dest = new Decimal(bd);
				else ((Decimal)dest).setBigDecimal(bd);
				return dest;
			}
		case STRING:
			if (dest == null) return EncodedUTF8String.load(data,where);
			if (dest instanceof CharArray) return EncodedUTF8String.load(data,where,(CharArray)dest);
			if (dest instanceof SubString) return EncodedUTF8String.load(data,where,(SubString)dest);
			/*
			int need = EncodedUTF8String.load(data,where,null,0);
			if (need == -1) return null;
			if (dest instanceof CharArray){
				CharArray ca = (CharArray)dest;
				ca.ensureCapacity(need);
				EncodedUTF8String.load(data,where,ca.data,0);
				ca.length = need;
				return ca;
			}
			if (dest instanceof SubString || dest == null){
				char [] got = new char[need];
				EncodedUTF8String.load(data,where,got,0);
				if (dest instanceof SubString){
					SubString ss = (SubString)dest;
					ss.data = got;
					ss.start = 0;
					ss.length = need;
					return dest;
				}
				return ewe.sys.Vm.createStringWithChars(got);
			}
			*/
			throw new IllegalArgumentException(dest.getClass().getName());
		default:
			return null;
	}
}
/**
* This will place the encoded record data in the destination ByteArray, <b>clearing it first</b>
* and possibly encrypting it first (if encryptor is not null). The returned value is always
* a copy of the record's data.
* @param destination The destination for the data. If it is null a new one will be created.
* @param encryptor An optional encryptor for the data.
* @return The destination byte array with the data for the record starting at index 0.
* @exception IOException if there is an error encrypting the data.
*/
//===================================================================
public ByteArray encode(ByteArray destination,DataProcessor encryptor) throws IOException
//===================================================================
{
	if (myData == null) clear();
	if (destination == null) destination = new ByteArray();
	destination.clear();
	if (encryptor != null){
		byteArray.clear();
		int where = destination.length;
		IO.processAll(encryptor,myData.data,0,myData.length,byteArray);
		destination.makeSpace(where,4);
		int len = byteArray.length|0x80000000;
		Utils.writeInt(len,destination.data,where,4);
		destination.append(byteArray.data,0,byteArray.length);
	}else{
		destination.append(myData.data,0,myData.length);
	}
	return destination;
}
//-------------------------------------------------------------------
private static CharArray strOne, strTwo;
//-------------------------------------------------------------------
private static int compareField(Locale locale,byte[] dataOne,int whereOne,byte[] dataTwo,int whereTwo,int type,int options, boolean hasWildCards)
//-------------------------------------------------------------------
{
	long one = 0, two = 0;
	switch(type){
		case LONG: case DOUBLE: case DATE_TIME: case DATE: case TIME: case TIMESTAMP:
			one = Utils.readLong(dataOne,whereOne);
			two = Utils.readLong(dataTwo,whereTwo);
	}
	switch(type){
		case BOOLEAN: return Utils.readInt(dataOne,whereOne,1)-Utils.readInt(dataTwo,whereTwo,1);
		case INTEGER: return Utils.readInt(dataOne,whereOne,4)-Utils.readInt(dataTwo,whereTwo,4);
		case LONG:
			{
			long value = one-two;
			if (value > 0) return 1;
			else if (value < 0) return -1;
			return 0;
			}
		case DOUBLE:
			{
			double value = Convert.toDoubleBitwise(one)-Convert.toDoubleBitwise(two);
			if (value > 0) return 1;
			else if (value < 0) return -1;
			return 0;
			}
		case DATE_TIME:
		case TIMESTAMP:
			return ewe.sys.Time.compareEncodedTimes(one,two,(options & SORT_TIME_ONLY) != 0,(options & SORT_DATE_ONLY) != 0);
		case DATE:
			return ewe.sys.Time.compareEncodedTimes(one,two,false,true);
		case TIME:
			return ewe.sys.Time.compareEncodedTimes(one,two,true,false);
		case DECIMAL:
			try{
				return ewe.math.BigDecimal.compareEncoded(
					dataOne,whereOne+4,Utils.readInt(dataOne,whereOne,4),
					dataTwo,whereTwo+4,Utils.readInt(dataTwo,whereTwo,4));
			}catch(Exception e){
				e.printStackTrace();
			}
			break;
		case STRING:
			strOne = EncodedUTF8String.load(dataOne,whereOne,strOne);
			strTwo = EncodedUTF8String.load(dataTwo,whereTwo,strTwo);
			int opts = 0;
			if ((options & SORT_IGNORE_CASE) != 0) opts |= Locale.IGNORE_CASE;
			if (hasWildCards) opts |= Locale.HAS_WILD_CARDS;
			return locale.compare(strOne.data,0,strOne.length,strTwo.data,0,strTwo.length,opts);
	}
	return (0);
}
/**
 * Compare this DatabaseEntry to another.
 * @param criteria a list of criteria (as returned by toCriteria(field,type,options)).
 * @return less than 0 if this entry is less than the other, greater than 0 if this entry is
 * greater than the other, 0 if this entry is equal to the other.
 */
//===================================================================
public int compareTo(DatabaseEntry otherEntry, int [] criteria, boolean hasWildCards)
//===================================================================
{
	RecordDatabaseEntry other = (RecordDatabaseEntry)otherEntry;
	if (other == null) return 1;
	for (int i = 0; i<criteria.length; i++){
		int f = criteria[i] & 0xffff;
		int t = (criteria[i] >> 16) & 0xff;
		int o = (criteria[i] >> 24) & 0xff;
		int me = locateField(f,t);
		int him = other.locateField(f,t);
		if (me == -1)
			if (him == -1) continue;
			else return ((o & SORT_UNKNOWN_IS_LESS_THAN_KNOWN) != 0) ? -1 : 1;
		else
			if (him == -1) return ((o & SORT_UNKNOWN_IS_LESS_THAN_KNOWN) != 0) ? 1 : -1;
			else{
				int ret = compareField(locale,myData.data,me+3,other.myData.data,him+3,t,o,hasWildCards);
				if (ret != 0) return ret;
			}
	}
	return 0;
}
/**
 * Produce an object that this DatabaseEntry uses when comparing a field for a particluar
 * search value or mask. For example you could provide a String value of "N*" when searching
 * for a type STRING. The returned object is used by the
 * @param type the type of the data.
 * @param mask The mask object.
 * @return an object that you can pass to compare()
 */
	/*
//===================================================================
public Object toSearchObject(int field, int type, int options, Object mask)
//===================================================================
{
	Tag t = new Tag();
	t.tag = DatabaseUtils.toCriteria(field,type,options);
	t.value = mask;
	return t;
}
*/
/**
 * Compare this object against a set of search objects.
 * @param searchObject a list of searchObjects, each of which
 * must have been created using toSearchObject().
 * @return less than 0 if this entry is less than the searchObjects, greater than 0 if this entry is
 * greater than the searchObject, 0 if this entry is equal to the searchObjects.
 */
	/*
//===================================================================
public int compare(Object[] searchObject)
//===================================================================
{
	return 0;
}
*/
//===================================================================
public void reset()
//===================================================================
{
	clear();
	super.reset();
}
/**
* Get the ByteArray that holds the record's encoded data. This is used
* for saving the data. This is the records actual data, not a copy of it, so do not write
* to the returned ByteArray.
**/
//-------------------------------------------------------------------
protected ByteArray getDataForSaving()
//-------------------------------------------------------------------
{
	if (myData == null) return clear();
	return myData;
}
/**
* Get the ByteArray that holds the record's encoded data. This is used
* for reading in data to the Entry and will clear the current data.
* After placing the data in the ByteArray, call decode() to decode and validate the data.
**/
//-------------------------------------------------------------------
protected ByteArray getDataForLoading()
//-------------------------------------------------------------------
{
	clear();
	myData.length = 0;
	return myData;
}
//-------------------------------------------------------------------
void decodeRecord() throws IOException
//-------------------------------------------------------------------
{
	try{
		int max = myData.length;
		byte[] data = myData.data;
		int i = 0;
		while(i < max){
			int type = data[i] & TYPE_MASK;
			if (type == 0) {
				myData.length = i+1;
				//ewe.sys.Vm.debug("Length truncated to: "+myData.length+", from: "+max);
				return;
			}
			int size = getFullSize(type,i);
			if (size == 0) throw new BadRecordFormatException();
			i += size;
		}
		if (i != max) throw new BadRecordFormatException();
		buffer[0] = 0;
		myData.append(buffer,0,1);
	}catch(IndexOutOfBoundsException e){
		throw new BadRecordFormatException();
	}
}
/*
static String allLastNames[] =
{
"Brereton","Mahabir","Charles","Wallace","Monsegue","Raymond","Granger",
"Rousea","Rampersad","Joseph","Khan","Hay","DeLima","Farfan","Che Ting",
};

public static String allFirstNames[] =
{
"Michael","Asha","Peter","Jean-Anne","Valerie","Norma","Pat","Ken",
"Jill","Margaret","Abraham","Karen","Krystal","Jennifer","George","Rita",
"James","Louis","Lia","Raymon","Antonio",
"Vincent","Damian","Anne","Mary","Sylvia",
"Scott","Deborah","Samuel","Che","Andre"
};

static int rand()
{
	return ewe.sys.Math.rand();
}
static CharArray names = new CharArray();
static String space = " ";
//===================================================================
public static CharArray getRandomFirstName(int size)
//===================================================================
{
	names.length  = 0;
	for (int i = 0; i<size; i++){
		if (i != 0) names.append(space);
		names.append(allFirstNames[rand()%allFirstNames.length]);
	}
	return names;
}

static int oneDay = 24*60*60*1000;
static Time time = new Time();
static long now = time.getTime();

//===================================================================
public static DatabaseEntry getRandomContact(boolean longNames,DatabaseEntry entry)
//===================================================================
{
	entry.clear();
	entry.setField(OID_FIELD,DatabaseUtils.getNewOID());
	entry.setField(1,allLastNames[rand()%allLastNames.length]);
	entry.setField(2,getRandomFirstName(longNames ? 3 : 1));
	entry.setField(3,rand()%100);
	long newTime = now+(long)((Math.random()-0.5)*10000L*oneDay);
	entry.setField(4,time.setTime(newTime));
	return entry;
}
//=================================================================
public static void main(String[] args) throws IOException
//=================================================================
{
	ewe.sys.Vm.startEwe(args);
	Math.random();
	time.setTime(now);
	ewe.sys.Math.srand(ewe.sys.Vm.getTimeStamp());
	DatabaseEntry db = new DatabaseEntry();
	names.data = new char[1000];
	db.clear();
	db.myData.data = new byte[4000];
	db.clear();
	RandomAccessDatabaseStream s = new RandomAccessDatabaseStream(new ewe.io.RandomAccessFile("records.dat","rw"),"rw");
	final RecordFile rf = new RecordFile();
	rf.setDatabaseStream(s);
	if (args.length != 0 && args[0].equals("-write")){
		int thousands = 1;
		if (args.length > 1){
			thousands = ewe.sys.Convert.toInt(args[1]);
			if (thousands == 0) thousands = 1;
		}
		rf.initialize();
		int num = ewe.sys.Vm.countObjects(false);
		for (int j = 0; j<thousands; j++){
			for (int i = 0; i<1000; i++){
				rf.addRecord(getRandomContact(true,db));
			}
			ewe.sys.Vm.debug("Did: "+(j+1)*1000);
		}
		int more = ewe.sys.Vm.countObjects(false)-num;
		//ewe.sys.Vm.debug("More: "+more);
	}
	final FoundEntries fe = rf.getEntries();
	final DatabaseTableModel dtm = new DatabaseTableModel(rf);
	final DatabaseEntry de = fe.getNew();
	dtm.setEntries(fe);
	dtm.setFields(de,OID_FIELD+"$J;25;r;;F;HexDisplay,1$,2$,3$I;5;;E,4$Lewe/sys/Time;30",null);
	ewe.ui.Form f = dtm.getTableForm(null);
	final ewe.ui.ProgressAndControl pbf = new ewe.ui.ProgressAndControl();
	f.addLast(pbf).setCell(f.HSTRETCH);
	pbf.controls.addNext(new ewe.ui.mButton("Clear deleted"){
		public void doAction(int how){
			try{
				rf.eraseDeletedEntries();
				ewe.sys.Vm.debug("Deleted erased.");
			}catch(Exception e){
				e.printStackTrace();
			}
		}
	});
	pbf.controls.addNext(new ewe.ui.mButton("List deleted"){
		public void doAction(int how){
			try{
				ewe.util.IntArray del = rf.getDeletedEntries(null);
				Time when = new Time();
				when.format = "ddd MMM yyyy, HH:mm:ss.SSSS";
				ewe.sys.Vm.debug("Deleted -----------");
				for (int i = 0; i<del.length; i++){
					long oid = rf.getDeletedEntry(del.data[i],when);
					ewe.sys.Vm.debug(Convert.longToHexString(oid)+", "+when);
				}
				ewe.sys.Vm.debug("-------------------");
			}catch(Exception e){
				e.printStackTrace();
			}
		}
	});
	pbf.controls.addNext(new ewe.ui.mButton("Delete it"){
		public void doAction(int how){
			try{
				fe.delete(0);
				dtm.entriesChanged();
			}catch(Exception e){
				e.printStackTrace();
			}
		}
	});
	pbf.controls.addNext(new ewe.ui.mButton("Sort by Name"){
		public void doAction(int how){
			new ewe.sys.TaskObject(){
				protected void doRun(){

				//	int[] criteria = new int[2];
				//	criteria[0] = de.toCriteria(1,STRING,0);
				//	criteria[1] = de.toCriteria(2,STRING,0);


			//int[] criteria = new int[1];
			//criteria[0] = de.toCriteria(4,DATE_TIME,0);

					int[] criteria = new int[1];
					criteria[0] = de.toCriteria(OID_FIELD,LONG,0);

					ewe.sys.Handle h = fe.sort(criteria,false);
					h.doing = "Sorting";
					pbf.startTask(h,null);
					try{
						//ewe.sys.Vm.debug("Sorting...");
						h.waitOn(h.Success);
						dtm.entriesChanged();
						//ewe.sys.Vm.debug("Done...");
					}catch(ewe.sys.HandleStoppedException e){
						//ewe.sys.Vm.debug("Failed...");
					}catch(InterruptedException e){
						//ewe.sys.Vm.debug("Interrupted");
					}
					pbf.endTask();
				}
			}.startTask();
		}
	});
	f.execute();
	//new ewesoft.apps.HexView(db.getDataForSaving()).execute();
	ewe.sys.Vm.exit(0);
}
*/


//##################################################################
}
//##################################################################
