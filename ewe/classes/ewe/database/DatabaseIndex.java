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
import ewe.data.HasProperties;
import ewe.sys.Time;

//##################################################################
interface DatabaseIndex extends HasProperties, FoundEntries{
//##################################################################
/**
* Get an ordinary FoundEntries from the entries in the index.
**/
//===================================================================
public FoundEntries getEntries();
//===================================================================
public String getName();
//===================================================================

/**
Return the number of entries in the index. This must match the number
of entries in the Database, otherwise something is wrong.
**/
//===================================================================
public int size();
//===================================================================

/**
 * Save changes to the properties of the index.
 * @exception IOException on error.
 */
//===================================================================
//public void save() throws IOException;
//===================================================================
/**
 * Close the stored file associated with the DatabaseIndex.
 * @exception IOException on error.
 */
//===================================================================
//public void close() throws IOException;
//===================================================================
/**
 * Delete the stored file associated with the DatabaseIndex, closing it first.
 * @exception IOException on error.
 */
//===================================================================
//public void delete() throws IOException;
//===================================================================
/**
 * Rename the index.
 * @param newName the new name of the index.
 * @exception IOException on error.
 */
//===================================================================
//public void rename(String newName) throws IOException;
//===================================================================
/**
 * Set the index to be equal to a new FoundEntries.

 This causes the index to be written from scratch and so may take some time.
 It is therefore run in a background thread and a handle to the thread is returned.
 The handle can be used to monitor and abort the save progress if necessary.

 * @param fe the new set of entries for the index.
 * @exception IOException on error.
 */
//===================================================================
//public Handle setTo(FoundEntries fe) throws IOException;
//===================================================================
/**
 * Check if the index could benefit significantly from compacting.
 * @return true if the index could benefit significantly from compacting.
 * @exception IOException on error.
 */
//===================================================================
public boolean needsCompacting() throws IOException;
//===================================================================
/**
* Compact the index so that it uses less space. This will generally
* only affect its space on disk, not in memory.
**/
//===================================================================
public boolean compact(Handle h) throws IOException;
//===================================================================

//===================================================================
//public Time getModifiedTime(Time destination) throws IOException;
//===================================================================

//===================================================================
//public boolean setModifiedTime(Time destination) throws IOException;
//===================================================================
/**
 * Set the modified time of the index so that it is equal to or more recent
 * than that of its database.
 * @return true if this could be done.
 * @exception IOException on error.
 */
//===================================================================
//public boolean markAsUpdated() throws IOException;
//===================================================================

//##################################################################
}
//##################################################################

