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
import ewe.sys.TimeOfDay;
import ewe.sys.DayOfYear;
import ewe.math.BigDecimal;
import ewe.sys.Decimal;
import ewe.util.ByteArray;
import ewe.util.SubString;
import ewe.util.CharArray;
import ewe.util.ByteEncodable;
import ewe.io.IOException;
import ewe.io.DataProcessor;
//##################################################################
public interface DatabaseEntry extends DatabaseTypes{
//##################################################################
/**
 * Get the database associated with the FoundEntries.
 */
//===================================================================
public Database getDatabase();
//===================================================================

//===================================================================
public boolean isSaved();
public boolean isADeletedEntry();
public boolean hasField(int ID);
public int countAssignedFields();
public int getAssignedFields(int[] dest,int offset);
public int[] getAssignedFields();
//===================================================================

//===================================================================
public void setField(int fieldID,int value);
//===================================================================
//===================================================================
public void setField(int fieldID,long value);
//===================================================================
//===================================================================
public void setField(int fieldID,boolean value);
//===================================================================
//===================================================================
public void setField(int fieldID,double value);
//===================================================================
//===================================================================
public void setField(int fieldID,TimeOfDay value);
//===================================================================
//===================================================================
public void setField(int fieldID,DayOfYear value);
//===================================================================
//===================================================================
public void setField(int fieldID,TimeStamp value);
//===================================================================
//===================================================================
public void setField(int fieldID,Time time);
//===================================================================
//===================================================================
public void setField(int fieldID,ByteArray bytes);
//===================================================================
//===================================================================
public void setField(int fieldID,byte[] bytes);
//===================================================================
//===================================================================
public void setField(int fieldID,SubString chars);
//===================================================================
//===================================================================
public void setField(int fieldID,CharArray chars);
//===================================================================
//===================================================================
public void setField(int fieldID,String chars);
//===================================================================
public void setField(int fieldID,BigDecimal value);
//===================================================================
public void setField(int fieldID,Decimal value);
//===================================================================
public void setObjectField(int fieldID,Object value);
//===================================================================

//===================================================================
public void setFieldValue(int fieldID, int type, Object data);
public void setFieldValue(int fieldID, Object data);
//===================================================================
public Object getFieldValue(int fieldID, int type, Object data);
public Object getFieldValue(int fieldID, Object data);
//===================================================================
//===================================================================
public int getField(int fieldID,int defaultValue);
//===================================================================
//===================================================================
public long getField(int fieldID,long defaultValue);
//===================================================================
//===================================================================
public boolean getField(int fieldID, boolean defaultValue);
//===================================================================
//===================================================================
public double getField(int fieldID, double defaultValue);
//===================================================================
//===================================================================
public Time getField(int fieldID, Time dest);
//===================================================================
//===================================================================
public DayOfYear getField(int fieldID, DayOfYear dest);
//===================================================================
//===================================================================
public TimeOfDay getField(int fieldID, TimeOfDay dest);
//===================================================================
//===================================================================
public TimeStamp getField(int fieldID, TimeStamp dest);
//===================================================================
//===================================================================
public BigDecimal getField(int fieldID, BigDecimal defaultValue);
//===================================================================
//===================================================================
public Decimal getField(int fieldID, Decimal dest);
//===================================================================
//===================================================================
public String getField(int fieldID, String defaultValue);
//===================================================================
//===================================================================
public CharArray getField(int fieldID, CharArray dest);
//===================================================================
//===================================================================
public ByteArray getField(int fieldID,ByteArray dest);
//===================================================================
public Object getObjectField(int fieldID,Object dest);
//===================================================================
public byte[] getFieldBytes(int fieldID);
//===================================================================
/**
* Get the ByteArray that holds the record's encoded data. This is used
* for saving the data. This is the records actual data, not a copy of it, so do not write
* to the returned ByteArray.
**/
//===================================================================
//public ByteArray getDataForSaving();
//===================================================================
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
//public ByteArray getDataForSaving(ByteArray destination,DataProcessor encryptor) throws IOException
//===================================================================
/**
* Get the ByteArray that holds the record's encoded data. This is used
* for reading in data to the Entry and will clear the current data.
* After placing the data in the ByteArray, call decode() to decode and validate the data.
**/
//===================================================================
//public ByteArray getDataForLoading()
//===================================================================
/**
 * This will save or add the entry into the database. The database may decide to add or alter
 * fields within the entry (e.g. the modified time field).
 * @exception IllegalStateException if the entry is a deleted entry or otherwise cannot be saved.
 * @exception IOException on error.
 */
//===================================================================
public void save() throws IllegalStateException, IOException;
//===================================================================
/**
 * This will store and add (if necessary) the entry into the database with <b>no</b> modifications.
 * @exception IllegalStateException if the entry is a deleted entry or otherwise cannot be saved.
 * @exception IOException on error.
 */
//===================================================================
public void store() throws IllegalStateException, IOException;
//===================================================================
/**
 * This will delete the entry from the database. The database may decide
 * to mark the entry as deleted, saving the object ID and delete time.
 * @exception IOException on error.
 */
//===================================================================
public void delete() throws IOException;
//===================================================================
/**
 * This will erase the entry from the database. The database will not
 * mark it as deleted.
 */
//===================================================================
public void erase() throws IOException;
//===================================================================
/**
 * Reload the entries data.
 * @exception IllegalStateException if the entry's data could not be reloaded, because
 * it was deleted, erased or reset.
 * @exception IOException
 */
//===================================================================
public void revert() throws IllegalStateException, IOException;
//===================================================================
/**
* Reset the entry to be an empty entry, as if it had just been returned by Database.getNewData()
**/
//===================================================================
public void reset();
//===================================================================
/**
* Set the specified field to be unassigned.
**/
//===================================================================
public void clearField(int fieldID);
//===================================================================
/**
* Clears the data fields but not the special fields.
**/
//===================================================================
public void clearFields();
//===================================================================
/**
* Clears the data fields and the special fields. Use this with care.
**/
//===================================================================
public void clearDataAndSpecialFields();
//===================================================================

/**
* Compare this entry with another based on the sortID stored in the database (ignoring
* the SORT_DESCENDING option).
**/
//===================================================================
public int compareTo(DatabaseEntry other, int sortID) throws IllegalArgumentException;
//===================================================================
/**
* Compare this entry with another based on an arbitrary sort criteria.
**/
//===================================================================
public int compareTo(DatabaseEntry other,int[] sortCriteria,boolean hasWildCards) throws IllegalArgumentException;
//===================================================================
/**
 * Get the data from the entry into a data object.
 * @param destination a destination object. If this is null a new one will be created if
 * possible.
 * @return the destination or new object.
 * @exception IllegalArgumentException if the destination object is not the right type.
 * @exception IllegalStateException if a new object was requested but could not be created.
 */
//===================================================================
public Object getData(Object destination) throws IllegalArgumentException, IllegalStateException;
//===================================================================
/**
 * Get the data from the entry, creating a new data object.
 * @return the new data object.
 * @exception IllegalStateException if a new object could not be created.
 */
//===================================================================
public Object getData() throws IllegalStateException;
//===================================================================
/**
* Set the data in the entry from the data object.
* @param data the data to set.
* @exception IllegalArgumentException if the data object is the wrong type.
*/
//===================================================================
public void setData(Object data) throws IllegalArgumentException;
//===================================================================

//===================================================================
public void pointTo(DatabaseEntry other) throws IllegalArgumentException;
//===================================================================

//===================================================================
public boolean isPointingTo(DatabaseEntry other) throws IllegalArgumentException;
//===================================================================
/**
 * Decode an encoded DatabaseEntry.
 * @param data the encoded data bytes.
 * @param offset the start of the data.
 * @param length the number of data bytes.
 * @param decryptor an optional decryptor for decoding.
 * @exception IOException if the data is invalid or if the decryptor failed during decryption.
 */
//===================================================================
public void decode(byte[] data, int offset, int length, DataProcessor decryptor)
throws IOException;
//===================================================================
/**
 * Encode and append the data to the supplied ByteArray object.
 * @param dest the destination ByteArray object.
 * @param encryptor an optional encryptor for encrypting the data.
 * @return the destination ByteArray object or a new one if dest is null.
 * @exception IOException if the encryptor failed during decryption.
 */
//===================================================================
public ByteArray encode(ByteArray dest,DataProcessor encryptor)
throws IOException;
//===================================================================

//===================================================================
public boolean decode(byte[] data,int offset,int length);
//===================================================================
//===================================================================
public byte[] encode();
//===================================================================
/**
 * Duplicate all set fields in this DatabaseEntry with those from the other.
 */
//===================================================================
public void duplicateFrom(DatabaseEntry other);
//===================================================================

//##################################################################
}
//##################################################################


