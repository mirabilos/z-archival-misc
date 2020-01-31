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
import ewe.util.ByteArray;

//##################################################################
public interface Database{
//##################################################################
/**
* This is the Integer (32-bit) field type.
**/
public static final int INTEGER = 1;
/**
* This is the Long Integer (64-bit) field type.
**/
public static final int LONG = 2;
/**
* This is the Boolean field type.
**/
public static final int BOOLEAN = 3;
/**
* This is the String field type.
**/
public static final int STRING = 4;
/**
* This is the double precision floating point (64-bit) type. No single-precision
* floating point type is provided.
**/
public static final int DOUBLE = 5;
/**
* This is the byte array type.
**/
public static final int BYTE_ARRAY = 6;
/**
* This is for a date/time value (saved as a 64-bit integer).
**/
public static final int DATE_TIME = 7;
/**
* This is an option for a Sort.
**/
public static final int SORT_DESCENDING = 0x1;
/**
* This is an option for a Sort.
**/
public static final int SORT_IGNORE_CASE = 0x2;
/**
* This is an option for a Sort.
**/
public static final int SORT_UNKNOWN_FIRST = 0x4;
/**
* This is an option for a Sort.
**/
public static final int SORT_DATE_ONLY = 0x8;
/**
* This is an option for a Sort.
**/
public static final int SORT_TIME_ONLY = 0x10;
/**
* This is the reserved "EntryName" field. It is of type String. It is not added unless
* you request it.
**/
public static final int NAME_FIELD = 241;
/**
* This is the reserved "EntryOID" field. It is a 64-bit value (LONG) which
* should be unique to the entry. This is not added unless you request it.
**/
public static final int OID_FIELD = 242;
/**
* This is the reserved "CreatedDate" field. It is a 64-bit value representing
* when the table entry was created. This is not added unless you request it.
**/
public static final int CREATED_FIELD = 243;
/**
* This is the reserved "ModifiedDate" field. It is a 64-bit value representing
* when the table entry was created. This is not added unless you request it.
**/
public static final int MODIFIED_FIELD = 244;

/**
* This is the reserved "EntryFlags" field. It is a 32-bit value representing
* flags pertaining to the entry.
**/
public static final int FLAGS_FIELD = 245;
/**
* This is used with the FLAGS_FIELD and will specify that the entry has not been
* modified since last synchronized.
**/
public static final int FLAG_SYNCHRONIZED = 0x4000000;
/**
* This is the reserved "ObjectText" field. It is a String representing the
* text encoded data of a stored object.
**/
public static final int OBJECT_TEXT_FIELD = 246;
/**
* This is the reserved "ObjectBytes" field. It is a byte array representing the byte
* encoded data of a stored object.
**/
public static final int OBJECT_BYTES_FIELD = 247;
/**
* This is the reserved "ModifiedByWho" field. It is a 32-bit value representing
* the ID of the database which modified the entry. This is not added unless you request it.
**/
public static final int MODIFIED_BY_FIELD = 248;

/**
* This is used to set the class of the object used for data transfer to
* and from the table. If you call setFields(Object objectOrClass,String fields)
* then you do not need to call this method as it will be done for you.
* @param objectOrClass
*/
//===================================================================
public void setObjectClass(Object objectOrClass) throws IllegalArgumentException;
//===================================================================
public ewe.reflect.Reflect getObjectClass();
//===================================================================
public int [] setSorts(Object objectOrClass) throws IllegalArgumentException;
//===================================================================
//===================================================================
public int [] setSorts(Object objectOrClass,String sorts) throws IllegalArgumentException;
//===================================================================
/**
 * Set the fields for the Database, which must match fields in the objectOrClass
 * transfer object.
 * @param objectOrClass The object or class of the object to be used for data transfer.
 * @return an array of integers representing the field IDs.
 */
//===================================================================
public int [] setFields(Object objectOrClass) throws IllegalArgumentException;
//===================================================================
/**
 * Set the fields for the Database, which must match fields in the objectOrClass
 * transfer object.
 * @param objectOrClass The object or class of the object to be used for data transfer.
 * @param fields A comma separated list of fields. The types of these fields must be one of:
 <ul><li>int<li>long<li>double<li>boolean<li>String<li>Date<li>byte []</ul>
 * @return an array of integers representing the field IDs.
 */
//===================================================================
public int [] setFields(Object objectOrClass,String fields) throws IllegalArgumentException;
//===================================================================
/**
 * Set the fields for the Database, which must match fields in the objectOrClassOrReflect
 * transfer object.
 * @param objectOrClass The object or class of the object to be used for data transfer.
 * @param fields A comma separated list of fields. The types of these fields must be one of:
 <ul><li>int<li>long<li>double<li>boolean<li>String<li>Date<li>byte []</ul>
 * @param headers An optional comma separated list of headers.
 * @return an array of integers representing the field IDs.
 */
//===================================================================
public int [] setFields(Object objectOrClass,String fields,String headers) throws IllegalArgumentException;
//===================================================================
/**
* Find the FieldID of a named field. This is NOT case sensistive.
* @param fieldName The name of the field.
* @return The field id, or 0 if not found.
*/
//===================================================================
public int findField(String fieldName);
//===================================================================
/**
* Get an array of fieldIds for an array of field names.
**/
//===================================================================
public int [] findFields(String [] fieldNames);
//===================================================================
/**
* Get an array of fieldIds for a comma separated list of field names.
**/
//===================================================================
public int [] findFields(String fieldNames);
//===================================================================
//===================================================================
public String getFieldName(int fieldID);
//===================================================================
//===================================================================
public String getFieldHeader(int fieldID);
//===================================================================
//===================================================================
public boolean setFieldHeader(int fieldID,String newHeader);
//===================================================================

//===================================================================
public String getSortName(int sortID);
//===================================================================
public int [] getSortFields(int sortID);
//===================================================================
public int getSortOptions(int sortID);
//===================================================================

//===================================================================
public int getFieldType(int fieldID);
//===================================================================
/**
* Get the IDs of all the fields.
**/
//===================================================================
public int [] getFields();
//===================================================================
/**
* Get the IDs of all the sorts.
**/
//===================================================================
public int [] getSorts();
//===================================================================
/**
* Add a new field. This will return the ID of the new field or throw an exception on failure.
* This will method will fail if:<br>
* 1. An invalid fieldType is specified.<br>
* 2. An already used fieldName is specified.<br>
* 3. There are already the maximum of 240 fields.<br>
* @param fieldName The name of the new field.
* @param fieldType The type of the new field.
* @return the ID of the newly added field.
* @exception IllegalArgumentException
*/
//===================================================================
public int addField(String fieldName,int fieldType) throws IllegalArgumentException;
//===================================================================
/**
* Get the ID of a named sort criteria.
* @param sortName The name of the sort criteria.
* @return The ID of the sort criteria or 0 if it is not found.
*/
//===================================================================
public int findSort(String sortName);
//===================================================================
/**
* Create a new sort criteria that sorts using one field only. Returns the ID of the new sort.
**/
//===================================================================
public int addSort(String sortName,int options,int field)
throws IllegalArgumentException;
//===================================================================
/**
* Create a new sort criteria allowing you to specify up to four fields.
* Set any unused sort fields to zero. Returns the ID of the new sort.
**/
//===================================================================
public int addSort(String sortName,int options,int field1,int field2,int field3,int field4)
throws IllegalArgumentException;
//===================================================================
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
public int addSort(String sortName,int options,String fieldList) throws IllegalArgumentException;
//===================================================================
/**
* Note that if this Database is a live one (i.e. it exists within an open database), then
* calling this method may take time as all of the records in the table will have this
* field removed from them. This does not happen with addField().
**/
//===================================================================
public boolean removeField(int fieldID);
//===================================================================
//===================================================================
public boolean removeSort(int sortID);
//===================================================================
/**
 * Save the Database specs to permanent storage. Call this after making
 * changes or additions to the fields and sorts.
 * @exception ewe.io.IOException if an error occurs.
 */
//===================================================================
public void save() throws ewe.io.IOException;
//===================================================================
/**
* This is used by FoundEntries, you would not have to use it directly.
**/
//===================================================================
public DatabaseEntry getData(int id,ewe.util.ByteArray buffer,DatabaseEntry dest) throws ewe.io.IOException;
//===================================================================
/**
* This is used by FoundEntries, you would not have to use it directly.
**/
//===================================================================
public Object getFieldData(int id,int fieldID,Object dest) throws ewe.io.IOException;
//===================================================================
/**
 * Sets the fields in the DatabaseEntry from the fields in the data object - which must not be null.
 * @param ded The DatabaseEntry to set.
 * @param data The data object, which must be of the type objectClass or an exception will be thrown.
* @exception IllegalStateException If the object is the wrong type.
 */
//===================================================================
public void setData(DatabaseEntry ded,Object data) throws IllegalStateException;
//===================================================================
/**
 * Gets the fields from the DatabaseEntry to the fields in the data object. If the object is null
 * @param ded The DatabaseEntry to set.
 * @param data The data object, which must be of the type objectClass or an exception will be thrown.
* @return The data object or the new object if "data" was null.
* @exception IllegalStateException If the object is the wrong type or a new object could not be created.
*/
//===================================================================
public Object getData(DatabaseEntry ded,Object data) throws IllegalStateException;
//===================================================================
/**
* This gets a new DatabaseEntry that is not yet stored in the database but
* can be used to add data to in order to store in the database.
**/
//===================================================================
public DatabaseEntry getNewData();
//===================================================================
/**
* Get a new DatabaseEntry using a buffer and destination entry.
**/
//===================================================================
public DatabaseEntry getNewData(ByteArray buffer,DatabaseEntry dest);
//===================================================================

//===================================================================
public ewe.sys.Locale getLocale();
//===================================================================
public void setLocale(ewe.sys.Locale locale);
//===================================================================

/**
* This is made public for convenience but you would not likely use it directly. Instead
* you would use the reSort() method of the FoundEntries object.
**/
//===================================================================
public int sortFieldData(int sortID,ewe.sys.Locale locale,FoundEntries entries) throws ewe.io.IOException;
//===================================================================
/**
* This returns an empty FoundEntries object.
**/
//===================================================================
public FoundEntries getEmptyEntries();
//===================================================================

/**
 * Get all the entries in the Database sorted by the specified sort ID.
 * @param sortID The sortID to use. If this is 0 the entries are not sorted.
 * @return A new FoundEntries object.
 * @exception ewe.io.IOException
 */
//===================================================================
public FoundEntries getEntries(int sortID) throws ewe.io.IOException;
//===================================================================
/**
 * Get a subset of the entries in the Database sorted by the specific sort ID and
 * which the Comparer considers to be equal to the searchData.
 * @param sortID The sortID to use. If this is 0 the entries are not sorted.
 * @param searchData This data is passed to the Comparer when each entry is compared.
 * @param comparer This object will have its compare(Object one,Object two) method called
	for each entry in the database. The searchData parameter is passed as the "one" parameter
 and a DatabaseEntry will be created, read and passed as the "two" parameter. If the comparer
	returns a value of 0, indicating equivalence, then that entry will be placed in the FoundEntries
	object returned.
 * @return A new FoundEntries object containing references to only those entries that are considered
	equal to the searchData by the comparer.
 * @exception ewe.io.IOException
 */
//===================================================================
public FoundEntries getEntries(int sortID,Object searchData,ewe.util.Comparer comparer) throws ewe.io.IOException;
//===================================================================
/**
 * Get a subset of the entries in the Database sorted by the specific sort ID and
 * which the ObjectFinder considers to be what is being looked for.
 * @param sortID The sortID to use. If this is 0 the entries are not sorted.
 * @param finder This object will have its lookingFor(Object obj) method called for each
	entry in the database. If the finder returns true, then that entry will be placed in the FoundEntries
	returned.
 * @return A new FoundEntries object containing references to only those entries that are considered
	 to be what the finder is looking for.
 * @exception ewe.io.IOException
 */
//===================================================================
public FoundEntries getEntries(int sortID,ewe.util.ObjectFinder finder) throws ewe.io.IOException;
//===================================================================

/**
* This saves the Entry in the database, setting the Created/Modified values and adjusting
* the syncrhonized flag if necessary.
**/
//===================================================================
public void saveEntry(DatabaseEntry entry) throws ewe.io.IOException;
//===================================================================
/**
* This deletes the Entry from the database, moving it to a deleted section if the database
* is set up for synchronization.
**/
//===================================================================
public void deleteEntry(DatabaseEntry entry) throws ewe.io.IOException;
//===================================================================
/**
* Get all OIDs of all entries deleted since the Time t. If t is null then
* all the deleted OIDs are returned.
**/
//===================================================================
public long [] getDeletedSince(ewe.sys.Time t) throws ewe.io.IOException;
//===================================================================
/**
* Get the DatabaseEntry of the deleted OID. This may not hold the original information
* in the entry. It may just hold the OID and the MODIFIED_FIELD - which will be the
* date of deletion.
**/
//===================================================================
public DatabaseEntry getDeletedEntry(long OID) throws ewe.io.IOException;
//===================================================================
/**
* This erases the Entry from the database completely.
**/
//===================================================================
public void eraseEntry(DatabaseEntry entry) throws ewe.io.IOException;
//===================================================================
/**
* This stores the entry as it is without setting any Created/Modified values or any other
* value in the entry.
**/
//===================================================================
public void storeEntry(DatabaseEntry entry) throws ewe.io.IOException;
//===================================================================

//===================================================================
public void close() throws ewe.io.IOException;
//===================================================================
/**
* Use this to add one of the reserved fields (the XXXX_FIELD) values.
**/
//===================================================================
public void addSpecialField(int fieldCode) throws IllegalArgumentException;
//===================================================================
/**
* This tells the database to include the OID_FIELD, FLAGS_FIELD, CREATED_FIELD and MODIFIED_FIELD
* information with each record so that it can be synchronized with a other databases.
* This is only guaranteed to work when you first initialize a Database. Some implementations
* may allow you to do this even if records already exists, but if it does not then an IOException
* will be thrown. This will save the database.
**/
//===================================================================
public void enableSynchronization(int syncOptions) throws ewe.io.IOException;
//===================================================================
/**
This is a sync option that
suggests that the database use a sync model that uses less memory but may be slower.
**/
public static final int SYNC_SLOW = 0x1;
/**
This is a sync option that
tells the database to store the creation date along with every record.
**/
public static final int SYNC_STORE_CREATION_DATE = 0x2;
/**
This is a sync option that
tells the database to store the modification date along with every record. If this is not
selected, only the FLAG_SYNCHRONIZED bit is used to determine the sync'ed state of a record.
**/
public static final int SYNC_STORE_MODIFICATION_DATE = 0x4;
/**
* This is a sync option that tells the database to save the MODIFIED_BY data with
* each record. This data is necessary if you are allowing synchronization of one (mobile) database
* with any number of other desktop databases.
**/
public static final int SYNC_STORE_MODIFIED_BY = 0x8;

public static final String OidSortName = "_ByOID";
public static final String SyncSortName = "_BySync";
public static final String ModifiedSortName = "_ByModified";
public static final String ModifiedBySortName = "_ByModifiedBy";
public static final String CreatedSortName = "_ByCreated";

/**
* Get a unique identifier for this database - used for synchronizing.
**/
//===================================================================
public int getIdentifier() throws ewe.io.IOException;
//===================================================================
public void setSynchronizedTime(int remoteDatabaseID,ewe.sys.Time syncTime) throws ewe.io.IOException;
//===================================================================
public ewe.sys.Time getSynchronizedTime(int remoteDatabaseID) throws ewe.io.IOException;
//===================================================================

public static final String [] reservedFieldNames =
{"EntryName","EntryOID","CreatedDate","ModifiedDate","EntryFlags","ObjectText","ObjectBytes","ModifiedBy"};
public static final String [] reservedFieldHeaders =
{"Entry Name","Entry OID","Created Date","Modified Date","Entry Flags","Object Text","Object Bytes","Modified By"};
public static final int [] reservedFieldIDs =
{NAME_FIELD,OID_FIELD,CREATED_FIELD,MODIFIED_FIELD,FLAGS_FIELD,OBJECT_TEXT_FIELD,OBJECT_BYTES_FIELD,MODIFIED_BY_FIELD};
public static final int [] reservedFieldTypes =
{STRING,LONG,DATE_TIME,DATE_TIME,INTEGER,STRING,BYTE_ARRAY,INTEGER};
/**
 * Delete the database.
 * @exception IOException if the database could not be deleted.
 */
//===================================================================
public void delete() throws ewe.io.IOException;
//===================================================================
/**
 * Rename the database.
 * @param newName the new name for the database.
 * @exception ewe.io.IOException if the database could not be renamed.
 */
//===================================================================
public void rename(String newName) throws ewe.io.IOException;
//===================================================================

//##################################################################
}
//##################################################################

