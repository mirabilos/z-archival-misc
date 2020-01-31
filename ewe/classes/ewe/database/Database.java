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
import ewe.io.IOException;
import ewe.sys.Handle;
import ewe.sys.Time;
import ewe.io.DataProcessor;
import ewe.sys.Locale;
import ewe.reflect.Reflect;
import ewe.util.ObjectFinder;
import ewe.util.Comparer;
import ewe.util.Iterator;
import ewe.util.EventDispatcher;
import ewe.data.HasProperties;
import ewe.io.Stream;

/**
* A Database represents a single relational database table along with associated indexes
* and other meta data.
* Note that a full application database will usually consist of a number of individual
* Database objects, since each one represents a single data table.<p>
* The interface specifies the functions a Database provides, not how it is implemented.
* However the Ewe library provides a Database implementation that can be implemented on
* any object that provides RandomAccessStream functionality (such as a RandomAccessFile).
* This allows a Database to be setup in memory as easily as on disk.<p>
*
* Databases can be fully secure using Ewe encryption or any other type of encryption.
**/
//##################################################################
public interface Database extends DatabaseTypes, HasProperties{
//##################################################################
/**
 * Set/Clear/Retrieve Database options.
 * @param optionsToSet any of the DatabaseTypes.OPTION_XXXX values OR'ed together.
 * @param optionsToClear any of the DatabaseTypes.OPTION_XXXX values OR'ed together.
 * @return the value of the set options after the operation. If optionsToSet and optionsToClear
 * are both 0, then the method will simply return the value of the options.
 */
public int setOptions(int optionsToSet, int optionsToClear);

/**
* Use this when the Database is initialized to set the decryptor and encryptor
* for the data. It must be used <b>before</b> any data is added.
* Once it has been set it cannot be changed later.
**/
//===================================================================
public void setEncryption(DataProcessor decryptor, DataProcessor encryptor) throws IOException;
//===================================================================
/**
 * Use this when the Database is initialized to set the encryption key for the database.
 * @param password a string to be used as the password. Choose a mix of numeric and letters
 * for better security.
 * @exception IllegalArgumentException
 * @exception IOException
 */
//===================================================================
public void setPassword(String password) throws IllegalArgumentException, IOException;
//===================================================================
/**
* Use this when opening the database and accessing the data. The specified decryptor/encryptor
* must match those used when the setEncryption() method was used to set the encryption for the database.
* The Database may validate
* the data at this point and may throw an IOException if it is determined that these are
* invalid for the database.
* @param decryptor The decryptor. Necessary for reading and writing if the Database is encrypted.
* @param encryptor The encryptor. Necessary for writing only if the Database is encrypted.
* @exception IOException
*/
//===================================================================
public boolean useEncryption(DataProcessor decryptor, DataProcessor encryptor) throws IOException;
//===================================================================
/**
* Use this when opening the database and accessing the data. The specified password
* must match those used when the setPassword() method was used to set the encryption for the database.
* The Database may validate
* the data at this point and may throw an IOException if it is determined that the password is
* invalid for the database.
* @exception IOException
*/
//===================================================================
public boolean usePassword(Object key) throws IOException;
//===================================================================
/**
 * Set the locale being used with the Database.
 */
//===================================================================
public Locale getLocale();
//===================================================================
/**
 * Set the locale to be used with the Database.
 */
//===================================================================
public void setLocale(Locale locale);
//===================================================================
/**
* This stores the entry as it is without setting any Created/Modified values or any other
* value in the entry. If the entry has not been stored before, it is assumed to be a new record,
* otherwise it is treated as a modified record and its stored value will be udpated if
* necessary.
**/
//===================================================================
//public void storeEntry(DatabaseEntry entry) throws IOException;
//===================================================================
/**
* This loads the entry from the database at the specified location.
* @param location The location to read from.
* @param entry The destination entry. If it is null a new one will be created.
* @return the destination entry.
* @exception IOException if it could not be loaded.
*/
//===================================================================
//public DatabaseEntry loadEntry(DatabaseEntry entry) throws IOException;
//===================================================================
/**
* This saves the Entry in the database, setting the Created/Modified values and adjusting
* the syncrhonized flag if necessary.
**/
//===================================================================
//public void saveEntry(DatabaseEntry entry) throws IOException;
//===================================================================
/**
* This deletes the Entry from the database, moving it to a deleted section if the database
* is set up for synchronization.
**/
//===================================================================
//public void deleteEntry(DatabaseEntry entry) throws IOException;
//===================================================================
/**
* This erases the Entry from the database completely.
**/
//===================================================================
//public void eraseEntry(DatabaseEntry entry) throws IOException;
//===================================================================

/***********************************************************************

These deal with deleted items. Only the OID and the time of deletion are kept.

***********************************************************************/
/**
 * Returns the number of entries marked as deleted.
 * @exception IOException on error.
 */
//===================================================================
public int countDeletedEntries() throws IOException;
//===================================================================
/**
 * Get an array of object IDs representing all the deleted entries. You
 * can then call getDeletedEntry(OID) to get the DatabaseEntry OR you
 * can call getTimeOfDeletion(OID,Time) to find out when it was deleted.
 * @return an array of OIDs representing all the deleted entries.
 * @exception IOException
 */
//===================================================================
public long [] getDeletedEntries() throws IOException;
//===================================================================
/**
 * Erase all the entries marked as deleted. You can also delete them
 * individually using eraseEntry().
 * @exception IOException
 */
//===================================================================
public void eraseDeletedEntries() throws IOException;
//===================================================================
/**
* Get the DatabaseEntry of the deleted OID. This will not hold the original information
* in the entry. It will just hold the OID and the MODIFIED_FIELD - which will be the
* date of deletion.
* @param OID The OID to look for.
* @return The DatabaseEntry or null if not found.
* @exception IOException
*/
//===================================================================
public DatabaseEntry getDeletedEntry(long OID,DatabaseEntry dest) throws IOException;
//===================================================================
/**
 * Return the time of deletion of the specified OID.
 * @param OID The OID of the deleted entry.
 * @param destination an optional destination time. If this is null the a new Time will
 * be created.
 * @return the time of deletion or null if the OID was not found in the deleted list.
 * @exception IOException
 */
//===================================================================
public Time getTimeOfDeletion(long OID,Time destination) throws IOException;
//===================================================================
/**
 * Erase the deleted entry for the specified OID.
 * @param OID the OID of the deleted entry.
 * @return true if it was deleted, false if that OID was not found i the deleted list.
 * @exception IOException
 */
//===================================================================
public boolean eraseDeletedEntry(long OID) throws IOException;
//===================================================================
/**
* Get all OIDs of all entries deleted since the Time t. If t is null then
* all the deleted OIDs are returned. Entries deleted AT the specified time
* are not returned.
**/
//===================================================================
public long [] getDeletedSince(Time t) throws IOException;
//===================================================================
/**
* This is used to set the class of the object used for data transfer to
* and from the table. If you call setFields(Object objectOrClass,String fields)
* then you do not need to call this method as it will be done for you.
* @param objectOrClass
*/
//===================================================================
public void setObjectClass(Object objectOrClass) throws IllegalArgumentException;
//===================================================================
/**
 * If setObjectClass() was used on the database to specify a Java class to
 * be used as a data interface, then this will return a Reflect object
 * representing that class.
 * @return
 */
//===================================================================
public Reflect getObjectClass();
//===================================================================
/**
* Create and return a new instance of the object class assigned to the
* database.
* @return a new instance of the object class assigned to the
* database.
* @exception IllegalStateException if no object class is specified or the
* object could not be instantiated.
*/
//===================================================================
public Object getNewDataObject() throws IllegalStateException;
//===================================================================
/**
 * Return if the specified object is an instance of the data transfer object
 * assigned in setObjectClass();
 * @param data the object to check.
 * @return true if the specified object is an instance of the data transfer object
 * assigned in setObjectClass();
 */
//===================================================================
public boolean isInstanceOfDataObject(Object data);
//===================================================================
/**
* This should return true if the Database is open in read/write mode, or false
* if it is open in read-only mode.
**/
//===================================================================
public boolean isOpenForReadWrite();
//===================================================================


/**
 * Set the sorts of the Database as specified by the object provided.
 * The object must declare a public variable named "_sorts" of type String.
 * @param objectOrClass the Class of the object or an instance of the object.
 * @return an array if IDs, one for each sort.
 * @exception IllegalArgumentException if a sort conflicts with an already saved sort.
 */
//===================================================================
public int [] setSorts(Object objectOrClass) throws IllegalArgumentException;
//===================================================================
/**
 * Set the sorts of the Database as specified by the object provided.
 * @param objectOrClass the Class of the object or an instance of the object.
 * @param sorts a String specifying the sort criteria to use.
 * @return an array if IDs, one for each sort.
 * @exception IllegalArgumentException if a sort conflicts with an already saved sort.
 */
//===================================================================
public int [] setSorts(Object objectOrClass,String sorts) throws IllegalArgumentException;
//===================================================================
/**
 * This works the same as setSorts() except that no error is thrown if
 * any of the sorts already exists and has the same criteria of the already declared sort.
 * This means that this method can be called repeatedly with the same data without
 * causing an error.
 * @param objectOrClass the Class of the object or an instance of the object.
 * @param sorts a String specifying the sort criteria to use.
 * @return an array if IDs, one for each sort.
 * @exception IllegalArgumentException
 */
//===================================================================
public int [] ensureSorts(Object objectOrClass,String sorts) throws IllegalArgumentException;
//===================================================================
/**
 * This works the same as setSorts() except that no error is thrown if
 * any of the sorts already exists and has the same criteria of the already declared sort,
 * and also any old sorts of the same name will be replaced by the new sort.
 * @param objectOrClass the Class of the object or an instance of the object.
 * @param sorts a String specifying the sort criteria to use.
 * @return an array if IDs, one for each sort.
 * @exception IllegalArgumentException
 */
//===================================================================
public int [] overrideSorts(Object objectOrClass,String sorts) throws IllegalArgumentException;
//===================================================================
/**
 * Similar to setFields() except that no error is thrown if any fields
 * of the same name and type already exist. This means that this method can
 * be called repeatedly with the same data without causing an error.
 * @param objectOrClass The object or class of the object to be used for data transfer.
 * @param fields A comma separated list of fields.
 * @param headers An optional comma separated list of headers.
 * @exception IllegalArgumentException
 */
//===================================================================
public int [] ensureFields(Object objectOrClass,String fields,String headers) throws IllegalArgumentException;
//===================================================================
/**
 * Similar to setFields() except that no error is thrown if any fields
 * of the same name and type already exist. This means that this method can
 * be called repeatedly with the same data without causing an error. This will also override
 * any previous fields with the same name and different type.
 * @param objectOrClass The object or class of the object to be used for data transfer.
 * @param fields A comma separated list of fields.
 * @param headers An optional comma separated list of headers.
 * @exception IllegalArgumentException
 */
//===================================================================
public int [] overrideFields(Object objectOrClass,String fields,String headers) throws IllegalArgumentException;
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
 * @param fields A comma separated list of fields.
 * @return an array of integers representing the field IDs.
 */
//===================================================================
public int [] setFields(Object objectOrClass,String fields) throws IllegalArgumentException;
//===================================================================
/**
 * Set the fields for the Database, which must match fields in the objectOrClassOrReflect
 * transfer object.
 * @param objectOrClass The object or class of the object to be used for data transfer.
 * @param fields A comma separated list of fields.
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
/**
 * Get the name for a specified field ID.
 */
//===================================================================
public String getFieldName(int fieldID);
//===================================================================
/**
 * Get the header for a specified field ID.
 */
//===================================================================
public String getFieldHeader(int fieldID);
//===================================================================
/**
 * Set the header for a specified field ID.
 * @return true if successful.
 */
//===================================================================
public boolean setFieldHeader(int fieldID,String newHeader);
//===================================================================
/**
 * Get the name of a Sort.
 */
//===================================================================
public String getSortName(int sortID);
//===================================================================
/**
 * Get the fields by which the Sort will sort.
 * @return an array of 1 or more fields to sort by.
 */
//===================================================================
public int [] getSortFields(int sortID);
//===================================================================
/**
 * Get the options associated with the sortID.
 */
//===================================================================
public int getSortOptions(int sortID);
//===================================================================
/**
* Convert the sortID to a Criteria array, used by a number of
* Database functions.
**/
//===================================================================
public int[] toCriteria(int sortID) throws IllegalArgumentException;
//===================================================================
//public boolean isDescending(int sortID) throws IllegalArgumentException;
//===================================================================
/**
 * Get the type of a field.
 */
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
* Used by modifyField() - this modifies and INTEGER field to be auto-incremental.
* The modifierData you would use would be an ewe.sys.Long object which contains the initial value
**/
public static final int FIELD_MODIFIER_INTEGER_AUTO_INCREMENT = 0x1;
/**
 * Modify a field in some way.
 * @param fieldID The id of the field to modify.
 * @param modifier one of the FIELD_MODIFIER_XXX values appropriate for the field type.
 * @param modifierData Some data dependant on the field modifier used.
 * @exception IllegalArgumentException if the modifier is not appropriate for the field or
 * if the data is not the correct type for the modifier.
 */
//===================================================================
public void modifyField(int fieldID, int modifier, Object modifierData) throws IllegalArgumentException, IOException;
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
public int addSort(String sortName,int options,String fieldList)
throws IllegalArgumentException;
//===================================================================
//===================================================================
//public boolean sortEntries(Handle h,IntArray ia,int sortID,boolean descending) throws IOException, IllegalArgumentException;
//===================================================================
/**
* Remove a field.
* Note that if this Database is a live one (i.e. it exists within an open database), then
* calling this method may take time as all of the records in the table will have this
* field removed from them. This does not happen with addField().
* @param fieldID the ID of the field to remove.
*/
//===================================================================
public void removeField(int fieldID);
//===================================================================
/**
 * Remove a Sort.
 * @param sortID the ID of the sort to remove.
 */
//===================================================================
public void removeSort(int sortID);
//===================================================================
/**
* This is used by FoundEntries, you would not have to use it directly.
**/
//===================================================================
//public DatabaseEntry getData(int id,ewe.util.ByteArray buffer,DatabaseEntry dest) throws IOException;
//===================================================================
/**
* This is used by FoundEntries, you would not have to use it directly.
**/
//===================================================================
//public Object getFieldData(int id,int fieldID,Object dest) throws IOException;
//===================================================================
/**
 * Return an empty DatabaseEntry to be used with this database. This method may be
 * used in "Append" mode to get a DatabaseEntry which you can use to append entries to
 * the database.
 */
//===================================================================
public DatabaseEntry getNewData();
//===================================================================
/**
Use this method in "Append" mode to add a data item to the Databse. In "Append" mode
you add data directly to the database and then do "reIndex()" at the end.
**/
//===================================================================
public void append(DatabaseEntry de) throws IOException;
//===================================================================
/**
In "Append" mode, after all the data has been added, call this method to re-index the
database.<p>
If you close the database without doing reIndex, it will be reIndexed automatically the
next time it is opened.
* @param h a Handle that can be used to monitor or abort the operation.
* @return true if the re-indexing ended successfully, false if it was aborted because
* the stop() method was called on the Handle h.
* @exception IOException if there was an error writing to the database.
*/
//===================================================================
public boolean reIndex(Handle h) throws IOException;
//===================================================================

/**
 * Sets the fields in the DatabaseEntry from the fields in the data object - which must not be null.
 * @param ded The DatabaseEntry to set.
 * @param data The data object, which must be of the type objectClass or an exception will be thrown.
* @exception IllegalStateException If the object is the wrong type.
 */
//===================================================================
//public void setData(DatabaseEntry ded,Object data) throws IllegalStateException;
//===================================================================
/**
 * Gets the fields from the DatabaseEntry to the fields in the data object. If the object is null
 * @param ded The DatabaseEntry to set.
 * @param data The data object, which must be of the type objectClass or an exception will be thrown.
* @return The data object or the new object if "data" was null.
* @exception IllegalStateException If the object is the wrong type or a new object could not be created.
*/
//===================================================================
//public Object getData(DatabaseEntry ded,Object data) throws IllegalStateException;
//===================================================================
/**
* Get a new DatabaseEntry using a buffer and destination entry.
**/
//===================================================================
//public DatabaseEntry getNewData(ByteArray buffer,DatabaseEntry dest);
//===================================================================

/**
* This is made public for convenience but you would not likely use it directly. Instead
* you would use the reSort() method of the FoundEntries object.
**/
//===================================================================
//public int sortFieldData(int sortID,ewe.sys.Locale locale,FoundEntries entries) throws IOException;
//===================================================================

/***********************************************************************

These deal with FoundEntries items. Only the OID and the time of deletion are kept.

***********************************************************************/
/**
* This returns an empty FoundEntries object for this Database
**/
//===================================================================
public FoundEntries getEmptyEntries();
//===================================================================

/**
* Get an unsorted FoundEntries representing all the entries in the database.
 * @exception DataTooBigException if the number of entries cannot fit in a FoundEntries
 * object. In this case the method should be called again with a finder that is more restrictive
 * of the included entries.
 * @deprecated use getFoundEntries(Handle h,int sortID) with a zero sortID instead.
**/
//===================================================================
public FoundEntries getEntries() throws IOException, DataTooBigException;
//===================================================================
/**
 * Get all the entries in the Database sorted by the specified sort ID.
* @param sortID The sortID to use. If this is 0 the entries are not sorted.
 * @return A new FoundEntries object.
 * @exception IOException
 * @exception DataTooBigException if the number of entries cannot fit in a FoundEntries
 * object. In this case the method should be called again with a finder that is more restrictive
 * of the included entries.
 * @deprecated use getFoundEntries(Handle h,int sortID) instead.
 */
//===================================================================
public FoundEntries getEntries(int sortID) throws IOException, DataTooBigException;
//===================================================================
/**
 * Get all the entries in the Database sorted by the specified sort ID.
 * This is done in the same thread as the calling thread, but uses a handle
 * to report its progress to other threads. This allows you to put this call
 * in its own thread while allowing other threads to run.
 * @param h a handle that other threads can use to monitor the operation progress.
 * @param sortID The sortID to use. If this is 0 the entries are not sorted.
 * @return A new FoundEntries object.
 * @exception IOException
 * @exception DataTooBigException if the number of entries cannot fit in a FoundEntries
 * object. In this case the method should be called again with a finder that is more restrictive
 * of the included entries.
 */
//===================================================================
public FoundEntries getFoundEntries(Handle h,int sortID) throws IOException, DataTooBigException;
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
 * @exception IOException
 * @exception DataTooBigException if the number of entries cannot fit in a FoundEntries
 * object. In this case the method should be called again with a finder that is more restrictive
 * of the included entries.
 */
//===================================================================
 // public FoundEntries getFoundEntries(Handle h,int sortID,Object searchData,Comparer comparer) throws IOException, DataTooBigException;
//===================================================================
/**
 * Get a subset of the entries in the Database sorted by the specific sort ID and
 * which the EntrySelector considers to be equal to the searchData.
 * @param h an optional Handle used to monitor and possibly abort the process.
 * @param sortID The sortID to use. If this is 0 the entries are not sorted.
 * @param selector An object used to select the data according to certain criteria.
 * @return A new FoundEntries object containing references to only those entries that are considered
	equal to the searchData by the comparer. If the handle is stopped then null will be returned.
 * @exception IOException
 * @exception DataTooBigException if the number of entries cannot fit in a FoundEntries
 * object. In this case the method should be called again with a finder that is more restrictive
 * of the included entries.
 */
//===================================================================
  public FoundEntries getFoundEntries(Handle h,int sortID,EntrySelector selector) throws IOException, DataTooBigException;
//===================================================================
/**
 * Get a subset of the entries in the Database sorted by the specific sort ID and
 * which the ObjectFinder considers to be what is being looked for.
 * @param h an optional Handle used to monitor and possibly abort the process.
 * @param sortID The sortID to use. If this is 0 the entries are not sorted.
 * @param finder This object will have its lookingFor(Object obj) method called for each
	entry in the database. If the finder returns true, then that entry will be placed in the FoundEntries
	returned.
 * @return A new FoundEntries object containing references to only those entries that are considered
	 to be what the finder is looking for. If the handle is stopped then null will be returned.
 * @exception IOException
 * @exception DataTooBigException if the number of entries cannot fit in a FoundEntries
 * object. In this case the method should be called again with a finder that is more restrictive
 * of the included entries.
 */
//===================================================================
public FoundEntries getFoundEntries(Handle h,int sortID,ObjectFinder finder) throws IOException, DataTooBigException;
//===================================================================
/**
 * Get a subset of the entries in the Database sorted by the specific sort ID and
 * which match the primarySearchFields data.
 * @param h an optional Handle used to monitor and possibly abort the process.
 * @param sortID The sortID to use. If this is 0 the entries are not sorted.
 * @param primarySearchFields data that will be used to select entries in the Database.
 * @return A new FoundEntries object containing references to only those entries that are considered
	 to match the search data. If the handle is stopped then null will be returned.
 * @exception IOException
 * @exception DataTooBigException if the number of entries cannot fit in a FoundEntries
 * object. In this case the method should be called again with a finder that is more restrictive
 * of the included entries.
 */
//===================================================================
public FoundEntries getFoundEntries(Handle h,int sortID,Object primarySearchFields) throws IOException, DataTooBigException;
//===================================================================
/**
 * Get all the entries in the Database sorted by the specific comparer.
 * @param h an optional Handle used to monitor and possibly abort the process.
 * @param comparer This is used to sort the entries.
 * @return A new FoundEntries object containing the entries sorted according to the comparer.
 * If the handle is stopped then null will be returned.
 * @exception IOException
 * @exception DataTooBigException if the number of entries cannot fit in a FoundEntries
 * object. In this case the method should be called again with a finder that is more restrictive
 * of the included entries.
 */
//===================================================================
public FoundEntries getFoundEntries(Handle h,Comparer comparer) throws IOException, DataTooBigException;
//===================================================================
/**
 * Get all the entries in the database sorted according to the specified index.
 * @param h an optional Handle used to monitor and possibly abort the process.
 * @param indexName a valid index name.
 * @return A new FoundEntries object containing the entries sorted according to the index.
 * If the handle is stopped then null will be returned.
 * @exception IllegalArgumentException
 * @exception IOException
 * @exception DataTooBigException if the number of entries cannot fit in a FoundEntries
 * object. In this case the method should be called again with a finder that is more restrictive
 * of the included entries.
 */
//===================================================================
public FoundEntries getFoundEntries(Handle h,String indexName) throws IllegalArgumentException, IOException, DataTooBigException;
//===================================================================
/**
 * Get all the entries in the Database sorted by the specified sort ID in a new thread.
 * @param sortID The sortID to use. If this is 0 the entries are not sorted.
 * @return a Handle that can be used to monitor and abort the process. When the Handle
 * reports Success, the returnValue of the Handle will hold a new FoundEntries object containing
 * the found and sorted entries.
 */
//===================================================================
public Handle getFoundEntries(int sortID);
//===================================================================
/**
 * Get a subset the entries in the Database sorted by the specified sort ID in a new thread.
 * @param sortID The sortID to use. If this is 0 the entries are not sorted.
 * @param selector An object used to select the data according to certain criteria.
 * @return a Handle that can be used to monitor and abort the process. When the Handle
 * reports Success, the returnValue of the Handle will hold a new FoundEntries object containing
 * the found and sorted entries.
 */
//===================================================================
public Handle getFoundEntries(int sortID,EntrySelector selector);
//===================================================================
/**
 * Get a subset of the entries in the Database sorted by the specific sort ID and
 * which the ObjectFinder considers to be what is being looked for in a new thread.
 * @param sortID The sortID to use. If this is 0 the entries are not sorted.
 * @param finder This object will have its lookingFor(Object obj) method called for each
	entry in the database. If the finder returns true, then that entry will be placed in the FoundEntries
	returned.
 * @return a Handle that can be used to monitor and abort the process. When the Handle
 * reports Success, the returnValue of the Handle will hold a new FoundEntries object containing
 * the found and sorted entries.
 */
//===================================================================
public Handle getFoundEntries(int sortID,ObjectFinder finder);
//===================================================================
/**
 * Get a subset of the entries in the Database sorted by the specific sort ID and
 * which match the primarySearchFields data in a new thread.
 * @param sortID The sortID to use. If this is 0 the entries are not sorted.
 * @param primarySearchFields data that will be used to select entries in the Database.
 * @return a Handle that can be used to monitor and abort the process. When the Handle
 * reports Success, the returnValue of the Handle will hold a new FoundEntries object containing
 * the found and sorted entries.
 */
//===================================================================
public Handle getFoundEntries(int sortID,Object primarySearchFields);
//===================================================================
/**
 * Get all the entries in the Database sorted by the specified Comparer in a new thread.
 * @param comparer This is used to sort the entries.
 * @return a Handle that can be used to monitor and abort the process. When the Handle
 * reports Success, the returnValue of the Handle will hold a new FoundEntries object containing
 * the found and sorted entries.
 */
//===================================================================
public Handle getFoundEntries(Comparer comparer);
//===================================================================
/**
 * Get all the entries in the Database sorted by the specified index in a new thread.
 * @param indexName a valid index name.
 * @return a Handle that can be used to monitor and abort the process. When the Handle
 * reports Success, the returnValue of the Handle will hold a new FoundEntries object containing
 * the sorted entries.
 */
//===================================================================
public Handle getFoundEntries(String indexName);
//===================================================================

//===================================================================
//public Handle getFoundEntries(int sortID,Object searchData,Comparer comparer);
//===================================================================

/***********************************************************************

General file handling.

***********************************************************************/
/**
 * Save the Database specs to permanent storage. Call this after changing fields and sorts.
 * @exception IOException if an error occurs.
 */
//===================================================================
public void save() throws IOException;
//===================================================================
/**
* Close the database.
* @exception IOException on error.
**/
//===================================================================
public void close() throws IOException;
//===================================================================
/**
 * Delete the database.
 * @exception IOException if the database could not be deleted.
 */
//===================================================================
public void delete() throws IOException;
//===================================================================
/**
 * Rename the database.
 * @param newName the new name for the database.
 * @exception IOException if the database could not be renamed.
 */
//===================================================================
public void rename(String newName) throws IOException;
//===================================================================
/**
 * Get the time of the last modification made to the database.
* @return the time of the last modification made to the database or null
* if it could not be determined.
* @exception IOException on error.
*/
//===================================================================
public Time getModifiedTime() throws IOException;
//===================================================================
/**
 * Set the time of the last modification made to the database.
 * @param t the modified time.
 * @return true if the operation was successful, false if the modification time
 * cannot be set.
 * @exception IOException on error.
 */
//===================================================================
public boolean setModifiedTime(Time t) throws IOException;
//===================================================================

/***********************************************************************

These deal with synchronization.

***********************************************************************/

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
public void enableSynchronization(int syncOptions) throws IOException;
//===================================================================
/**
* This tells the database to include the OID_FIELD, FLAGS_FIELD, CREATED_FIELD and MODIFIED_FIELD
* information with each record so that it can be synchronized with a other databases.
* This is only guaranteed to work when you first initialize a Database. Some implementations
* may allow you to do this even if records already exists, but if it does not then an IOException
* will be thrown. This will save the database.
**/
//===================================================================
public boolean enableSynchronization(Handle h,int syncOptions) throws IOException;
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
public int getIdentifier() throws IOException;
//===================================================================

//===================================================================
public void setSynchronizedTime(int remoteDatabaseID,Time syncTime) throws IOException;
//===================================================================

//===================================================================
public Time getSynchronizedTime(int remoteDatabaseID) throws IOException;
//===================================================================

/**
* This will return the number of entries in the database if known. If it is unknown
* it will return -1 and you can call either estimateEntriesCount() or countEntries() to
* to get an entry count.
* @return the number of entries in the database if known, or -1 if unknown.
* @exception IOException on error.
*/
//===================================================================
public long getEntriesCount() throws IOException;
//===================================================================
/**
 * If getEntriesCount() returns -1, indicating that the number of entries is unknown,
 * then this method will attempt to estimate the entries count via a quick non-blocking method
 * without resorting to counting each entry. This method may also return -1, indicating that
 * the database cannot even estimate the number of entries and a countEntries() will need to be
 * done if you want any indication of the size of the database.
 * @return the estimate of the number of entries in the database, or the exact number if it is known, or -1
 * if it cannot even estimate the number of entries.
 * @exception IOException on error.
 */
//===================================================================
public long estimateEntriesCount() throws IOException;
//===================================================================
/**
 * If getEntriesCount() returns -1, indicating that the number of entries is unknown,
 * then this method will count the entries by counting each one if necessary. Since
 * this may take some time it is done in a separate thread and a Handle is returned
 * that can monitor and stop the count if necessary. On completion the handle's returnValue
 * will be an ewe.sys.Long() object holding the count of the entries.
* @return a Handle which you can monitor and stop the count. On successful completion the
* returnValue will hold the number of entries.
*/
//===================================================================
public Handle countEntries();
//===================================================================
/**
* This returns an Iterator that you can use to go through the entries in
* the database.<p>
* The getNext() method of the returned iterator may throw a DatabaseIOException
* if there is an IO error when retrieving the next entry.<p>
**/
//===================================================================
public Iterator entries() throws IOException;
//===================================================================
/**
* This returns an Iterator that you can use to go through the entries in
* the database. Only the entries that the ObjectFinder returns true for lookingFor() will
* be provided by the getNext() method of the iterator.<p>
* The getNext() method of the returned iterator may throw a DatabaseIOException
* if there is an IO error when retrieving the next entry.<p>
**/
//===================================================================
public Iterator entries(ObjectFinder entry) throws IOException;
//===================================================================
/**
* This returns an Iterator that you can use to go through the entries in
* the database. Only the entries that the Comparer returns 0 for when comparing
* the entry with the searchData will
* be provided by the getNext() method of the iterator.<p>
* The getNext() method of the returned iterator may throw a DatabaseIOException
* if there is an IO error when retrieving the next entry.<p>
**/
//===================================================================
public Iterator entries(Object searchData, Comparer comparer) throws IOException;
//===================================================================
/**
* This tells the Database to keep an index for a particular sort ID and optional
* distinct name. If an index of the same name and sortID is already being kept
* then this will simply return true immediately. Otherwise the index will be created.<p>
* Creating a new index will usually involve sorting all the entries of the database
* so this may take some time to complete and you can monitor and abort the operation
* using the Handle parameter.
* @param h An optional handle that you can use to monitor and control any index creation
operation.
* @param sortID A valid sort ID for the database.
* @param name A distinct name for the index. If it is null then the same name as the sort ID
will be used.
* @return true if the operation completed successfully, false if it was aborted.
* @exception IOException If an IO error occured using the database.
* @exception IllegalArgumentException if the sort ID is invalid.
*/
//===================================================================
public boolean indexBy(Handle h,int sortID,String name) throws IOException, IllegalArgumentException;
//===================================================================
/**
* This tells the Database to keep an index using a particular Comparer class and
* distinct name. If an index of the same name is already being kept
* then this will simply return true immediately. Otherwise the index will be created.<p>
* Creating a new index will usually involve sorting all the entries of the database
* so this may take some time to complete and you can monitor and abort the operation
* using the Handle parameter.
* @param h An optional handle that you can use to monitor and control any index creation
operation.
* @param databaseEntryComparer A valid object that implements DatabaseEntryComparer
* @param name A distinct name for the index, which must not be null.
* @return true if the operation completed successfully, false if it was aborted.
* @exception IOException If an IO error occured using the database.
* @exception IllegalArgumentException if the comparer or name is invalid.
*/
//===================================================================
public boolean indexBy(Handle h,Class databaseEntryComparer,String name) throws IOException, IllegalArgumentException;
//===================================================================
/**
 * This is a convenience method for the other indexBy method. It will simply call
indexBy(Handle h, int sortID, String name) using a null "h" and "name" parameters.<p>
* @param sortID A valid sort ID for the database.
* @return true if the operation completed successfully, false if it was aborted.
* @exception IOException If an IO error occured using the database.
* @exception IllegalArgumentException if the sort ID is invalid.
 */
//===================================================================
public void indexBy(int sortID) throws IOException, IllegalArgumentException;
//===================================================================
/**
 * This returns if the Database has at least one index that uses the specified sort ID.
 */
//===================================================================
public boolean isIndexedBy(int sortID);
//===================================================================
/**
 * This returns all the indexes used by the Database. You can
 * use getIndexSort(String indexName) to find out which sort ID is being used
 * by each of the indexes returned.
 * @return An array of index names.
 */
//===================================================================
public IndexEntry [] getIndexes();
//===================================================================

/*
//===================================================================
public DatabaseIndex openIndex(String name) throws IOException;
//===================================================================
public DatabaseIndex saveAsIndex(String name,FoundEntries fe) throws IOException;
//===================================================================
public String [] listIndexes() throws IOException;
//===================================================================
public PropertyList getIndexProperties(String name) throws IOException, IllegalArgumentException;
//===================================================================
*/
/**
 * Get the current changed state of the Database. You can check
 * if the Database have been changed later by calling hasChangedSince().
 * @return a reference to its current state.
 */
//===================================================================
public long getCurrentState();
//===================================================================
/**
 * Return if the Database has changed since the previous state.
 */
//===================================================================
public boolean hasChangedSince(long previousState);
//===================================================================
/**
 * Mark the Database as having been changed in some way.
 */
//===================================================================
public void change();
//===================================================================
/**
* Get an EventDispatcher which you can use to attach EventListeners to, for
* listening to the events being generated by the Database.
**/
//===================================================================
public EventDispatcher getEventDispatcher();
//===================================================================
/**
 * Open a Stream into the database to save some sort of meta-data. Calling this method does not
 * guarantee a consistent state if an error occurs. That is to say, if an error occurs before
 * close() is called on the Stream, then it is possible that incomplete data will be saved.
 * @param name The name of the meta-data Stream.
 * @param append Set this true if you want to add to the data already stored in the stream.
 * @return A Stream that you can write meta-data to.
 * @exception IOException if an error occurs.
 */
//===================================================================
public Stream openStreamForWriting(String name,boolean append) throws IOException;
//===================================================================
/**
 * Open a Stream into the database to save some sort of meta-data. Calling this method will
 * guarantee a consistent state if an error occurs. That is to say, if an error occurs before
 * close() is called on the Stream, then the new data is lost and the original data (if present)
 * will still be accessible for reading the next time the Database is opened. If the close()
 * method completes successfully, then the new data will then be available for reading.
 * @param name The name of the meta-data Stream.
 * @return A Stream that you can write meta-data to.
 * @exception IOException if an error occurs.
 */
//===================================================================
public Stream openStreamForReplacing(String name) throws IOException;
//===================================================================
/**
 * Open a Stream into the database to read some sort of meta-data.
 * @param name The name of the meta-data Stream.
 * @return A Stream that you can write meta-data to.
 * @exception IOException if an error occurs or if the Stream does not exist.
 */
//===================================================================
public Stream openStreamForReading(String name) throws IOException;
//===================================================================
/**
 * Delete a Stream from the Database.
 * @param name The name of the meta-data Stream.
 * @exception IOException if an error occurs.
* @return true if the stream was found and deleted, false if it was not found.
* @exception IOException if an error occurs.
*/
//===================================================================
public boolean deleteStream(String name) throws IOException;
//===================================================================
/**
 * Returns the length of a meta-data Stream stored in the Database, or -1 if the named Stream
 * does not exist. You can use this method to test for the existance of a Stream.
 * @param name the name of the Stream.
 * @return the length of a meta-data Stream stored in the Database, or -1 if the named Stream
* @exception IOException if an error occurs.
 */
//===================================================================
public long getStreamLength(String name) throws IOException;
//===================================================================
/**
* Set a DataValidator to validate data before it is saved in the Database. Call save()
* on the Database to save the validator.
* @param validator A validator or null to clear the validator.
* @exception IOException if there was an error saving the validator.
*/
//===================================================================
public void setDataValidator(DataValidator validator);
//===================================================================

//===================================================================
public void readMetaData(Object metaLocation,int metaOffset,byte[] data,int offset,int length) throws IOException;
//===================================================================
public void writeMetaData(Object metaLocation,int metaOffset,byte[] data,int offset,int length) throws IOException;
//===================================================================
public Object getMetaData(String name,int length,boolean mustExist) throws IOException, IllegalArgumentException;
//===================================================================
public boolean deleteMetaData(String name) throws IOException;
//===================================================================
public int readMetaDataInt(Object metaLocation,int offset) throws IOException;
//===================================================================
public void writeMetaDataInt(Object metaLocation,int offset,int value) throws IOException;
//===================================================================
public int metaDataLength(String name) throws IOException;
//===================================================================


/**
This method tells the database that it will be used for read-only lookups and that it can
close its underlying file if it wishes - and later re-open it when lookups are done without
re-reading the database info.<p>
Not all Databases may support this and if it does not, it will return false - although normal
database operations will not be affected. In other words - if this method returns false, the
application can continue as if it returned true - but its operations will not be optimized
in this way.
 * @return true if this optimization is enabled, false if not.
 * @exception IOException if an IO error occurs.
 */
//===================================================================
public boolean enableLookupMode() throws IOException;
//===================================================================
/**
 * This is used with enableLookupMode() - it tells the database that data is about to be read in
 * and if the underlying file is closed - then it should be re-opened and kept open until closeLookup()
 * is called. Each call to openLookup() should have a corresponding call to closeLookup().
 * @exception IOException if an IO error occurs.
 */
//===================================================================
public void openLookup() throws IOException;
//===================================================================
/**
 * This is used with enableLookupMode() - it tells the database that data reading is complete
 * and the underlying file may be closed.
 * Each call to openLookup() should have a corresponding call to closeLookup().
 * @exception IOException if an IO error occurs.
 */
//===================================================================
public void closeLookup() throws IOException;
//===================================================================

//##################################################################
}
//##################################################################

