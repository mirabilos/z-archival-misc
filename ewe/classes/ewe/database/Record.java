package ewe.database;
import ewe.util.Hashtable;
import ewe.sys.TimeOfDay;
import ewe.sys.DayOfYear;
import ewe.sys.Time;
import ewe.sys.Decimal;
import ewe.util.CharArray;
import ewe.util.ByteArray;
import ewe.util.Copyable;
import ewe.data.PropertyList;
/**
A Record is a convenience class that can be used to quickly read or write
individual fields from a DatabaseEntry, automataically re-using
wrapper data objects. This reduces the amount of memory used and object
creation for each read or write of an item in the database.
<p>
The wrapper data objects used for each type of field are:<p>
<pre>
INTEGER, LONG, BOOLEAN - ewe.sys.Long
DOUBLE - ewe.sys.Double
DECIMAL - ewe.sys.Decimal
BYTE_ARRAY - ewe.util.ByteArray
STRING - ewe.util.CharArray
DATE - ewe.sys.DayOfYear
TIME - ewe.sys.TimeOfDay
DATE_TIME - ewe.sys.Time
TIMESTAMP - ewe.database.TimeStamp
</pre><p>
Fields of type JAVA_OBJECT use the java objects themselves instead of
wrapper values.<p>
**/
//##################################################################
public class Record implements DatabaseTypes{
//##################################################################

private Database db;
private int[] fields;
private int[] types;
private Object[] datas;
private Object[] values;

//===================================================================
public Database getDatabase()
//===================================================================
{
	return db;
}
/**
Create a Record for use with the specified Database. All fields will
be read in from DatabaseEntry objects.
**/
//===================================================================
public Record(Database db)
//===================================================================
{
	this(db,null);
}
/**
Create a Record for use with the specified Database using but where only the specified
fields will be read in from DatabaseEntry objects.
**/
//===================================================================
public Record(Database db, int[] fields)
//===================================================================
{
	this.db = db;
	selectFields(fields);
}
/**
Select the fields to be used by the Record - each of which must be a valid
field for the Database used by the Record.
**/
//===================================================================
public void selectFields(int[] fields) throws IllegalArgumentException
//===================================================================
{
	this.fields = fields == null ? db.getFields() : fields;
	int[] f = this.fields;
	datas = new Object[f.length];
	values = new Object[f.length];
	types = new int[f.length];
	for (int i = 0; i<f.length; i++){
		switch(types[i] = db.getFieldType(f[i])){
			case INTEGER: case LONG: case BOOLEAN:
				datas[i] = new ewe.sys.Long(); break;
			case DOUBLE:
				datas[i] = new ewe.sys.Double(); break;
			case BYTE_ARRAY:
				datas[i] = new ByteArray(); break;
			case STRING:
				datas[i] = new CharArray(); break;
			case DATE:
				datas[i] = new DayOfYear(); break;
			case TIME:
				datas[i] = new TimeOfDay(); break;
			case DATE_TIME:
				datas[i] = new Time(); break;
			case TIMESTAMP:
				datas[i] = new TimeStamp(); break;
			case DECIMAL:
				datas[i] = new Decimal(); break;
			case JAVA_OBJECT:
				datas[i] = null; break;
			default: throw new IllegalArgumentException();
		}
	}
}
/**
Return the fields that are being used with this Record.
**/
//===================================================================
public int[] getFields()
//===================================================================
{
	return (int[])fields.clone();
}
/**
This clears the data held by the Record, but still keeps the data objects used to
hold the data. This is used to reset a Record before setting individual fields prior
to a writeTo() operation. You would probably prefer to use the zero() method instead
as this initializes all fields with what is considered zero data (e.g. Strings are
set empty, decimals are set to zero, etc).
**/
//===================================================================
public void reset()
//===================================================================
{
	for (int i = 0; i<fields.length; i++)
		values[i] = null;
}
/**
This sets all the data held by the Record to a zero value.
This is used to reset a Record before setting individual fields prior
to a writeTo() operation.
**/
//===================================================================
public void zero()
//===================================================================
{
	for (int i = 0; i<fields.length; i++)
		zeroData(values[i] = datas[i]);
}
/**
This method is used to retrieve the data object used to wrap the field data, and
at the same time mark that field as having its data set, for a later writeTo()
operation. After calling this method you should set the object data appropriately.
* @param fieldID the field ID to look for.
* @return the data object representing the field read in by readFrom().
* @exception IllegalArgumentException if the fieldID is not being read in by this Record.
**/
//===================================================================
public Object setField(int fieldID)
throws IllegalArgumentException
//===================================================================
{
	for (int i = 0; i<fields.length; i++)
		if (fields[i] == fieldID)
			return values[i] = datas[i];
	throw new IllegalArgumentException();
}
/**
This method should normally be used only if the field type is JAVA_OBJECT. It sets
the field data and
at the same time mark that field as having its data set, for a later writeTo()
operation.
* @param fieldID the field ID to look for.
* @param fieldData data object representing the field read in by readFrom().
* @exception IllegalArgumentException if the fieldID is not being read in by this Record.
**/
//===================================================================
public void setField(int fieldID, Object fieldData)
throws IllegalArgumentException
//===================================================================
{
	for (int i = 0; i<fields.length; i++)
		if (fields[i] == fieldID){
			values[i] = fieldData;
			return;
		}
	throw new IllegalArgumentException();
}
/**
Read the field values from the DatabaseEntry into the data objects being kept by
the Record object. Any field that is being read by the Record but is not present
in the DatabaseEntry will have a null data value associated with it.
All field with values in the DatabaseEntries are read into their wrapper objects.
**/
//===================================================================
public void readFrom(DatabaseEntry de)
//===================================================================
{
	for (int i = 0; i<fields.length; i++)
		values[i] = de.getFieldValue(fields[i],types[i],datas[i]);
}
/**
Set the field values int the DatabaseEntry from the set data objects being kept by
the Record object.
**/
//===================================================================
public void writeTo(DatabaseEntry de)
//===================================================================
{
	for (int i = 0; i<fields.length; i++)
		if (values[i] != null)
			de.setFieldValue(fields[i],types[i],values[i]);
}
/**
Call this after a readFrom(). It will return the value of the specified fieldID.
Note that this value is only valid until the next call of readFrom() - unless that value
is of type JAVA_OBJECT, in which case it will continue to be valid.<p>
To keep a persistent copy of the data call copyData(Object data). Calling copyData()
on an encoded JAVA_OBJECT is safe, it will simply return the same value.
* @param fieldID the field ID to look for.
* @return the data object representing the field read in by readFrom(). If there was no
* value for the field in the DatabaseEntry, null will be returned.
* @exception IllegalArgumentException if the fieldID is not being read in by this Record.
*/
//===================================================================
public Object getField(int fieldID) throws IllegalArgumentException
//===================================================================
{
	for (int i = 0; i<fields.length; i++)
		if (fields[i] == fieldID)
			return values[i];
	throw new IllegalArgumentException();
}
/**
Find the ID of a specified field given its name - throwing an IllegalArgumentException
if the ID is not found.
**/
//===================================================================
public int getID(String fieldName) throws IllegalArgumentException
//===================================================================
{
	int id = db.findField(fieldName);
	if (id == 0) throw new IllegalArgumentException();
	return id;
}
/**
Return a copy of the data returned by getField() - unless that data is of type JAVA_OBJECT
in which case a copy will only be returned if it implements ewe.util.Copyable.
**/
//===================================================================
public Object copyData(Object theData)
//===================================================================
{
	if (theData instanceof Copyable)
		return ((Copyable)theData).getCopy();
	else
		return theData;
}
/**
Sets the data value to whatever is considered to be a zero value.
**/
//===================================================================
public void zeroData(Object theData)
//===================================================================
{
	if (theData == null) return;
	else if (theData instanceof ByteArray) ((ByteArray)theData).length = 0;
	else if (theData instanceof CharArray) ((CharArray)theData).length = 0;
	else if (theData instanceof ewe.sys.Long) ((ewe.sys.Long)theData).set(0);
	else if (theData instanceof ewe.sys.Double) ((ewe.sys.Double)theData).set(0);
	else if (theData instanceof ewe.sys.Decimal) ((ewe.sys.Decimal)theData).setDouble(0);
	else if (theData instanceof ewe.sys.Time) ((ewe.sys.Time)theData).setToCurrentTime();
}
/**
Copy a set of fields from this Record into the destination DatabaseEntry (which
may be from a different Database) into fields with IDs specified by destFields,
and which are of the same type as the source fields.

* @param sourceFields An array of Field IDs which must be within the field list for
this Record object.
* @param destFields An array of Field IDs, equal in length to the sourceFields array
but with values which correspond to destination fields in the destination DatabaseEntry.
* @param dest the destination DatabaseEntry (which may be from a different Database).
* @exception IllegalArgumentException if any of the sourceFields IDs is not in this Record
or if any of the destination field IDs are invalid or of the wrong type.
*/
//===================================================================
public void transferTo(int[] sourceFields, int[] destFields, DatabaseEntry dest)
throws IllegalArgumentException
//===================================================================
{
	for (int i = 0; i<sourceFields.length; i++){
		int idx = -1;
		int s = sourceFields[i];
		for (int f = 0; f<fields.length; f++){
			if (fields[f] == s){
				idx = f;
				break;
			}
		}
		if (idx == -1) throw new IllegalArgumentException();
		dest.setFieldValue(destFields[i],types[idx],values[idx]);
	}
}
/**
Place the field values (or copies of the values) in the Record into a Hashtable,
keyed by the field name.
* @param destination the destination Hashtable, or null to create a new one.
* @param copyData set this true to save copies of the field data.
* @return the destination Hashtable or a new Hashtable.
*/
//===================================================================
public Hashtable toHashtable(Hashtable destination, boolean copyData)
//===================================================================
{
	if (destination == null) destination = new Hashtable();
	for (int i = 0; i<fields.length; i++){
		Object got = values[i];
		if (copyData) got = copyData(got);
		destination.put(db.getFieldName(fields[i]),got);
	}
	return destination;
}
/**
Place copies of the field values in the Record into a new Hashtable, keyed by the
field name.
**/
//===================================================================
public Hashtable toHashtable()
//===================================================================
{
	return toHashtable(null,true);
}
/**
Place the field values (or copies of the values) in the Record into a PropertyList,
keyed by the field name.
* @param destination the destination PropertyList, or null to create a new one.
* @param copyData set this true to save copies of the field data.
* @return the destination PropertyList or a new PropertyList.
*/
//===================================================================
public PropertyList toPropertyList(PropertyList destination, boolean copyData)
//===================================================================
{
	if (destination == null) destination = new PropertyList();
	for (int i = 0; i<fields.length; i++){
		Object got = values[i];
		if (copyData) got = copyData(got);
		destination.set(db.getFieldName(fields[i]),got);
	}
	return destination;
}
/**
Place copies of the field values in the Record into a new PropertyList, keyed by the
field name.
**/
//===================================================================
public PropertyList toPropertyList()
//===================================================================
{
	return toPropertyList(null,true);
}
/*
//=================================================================
public static void main(String[] args)
//=================================================================
{
	ewe.sys.Vm.startEwe(args);
	Time t = new DayOfYear(22,11,2034);
	ewe.sys.Vm.debug(t.toString()+", "+t.getClass());
	t = (Time)t.getCopy();
	ewe.sys.Vm.debug(t.toString()+", "+t.getClass());
	//ewe.sys.Vm.exit(0);
}
*/
//##################################################################
}
//##################################################################

