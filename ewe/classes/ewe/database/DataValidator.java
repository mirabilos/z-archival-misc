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

//##################################################################
public interface DataValidator{
//##################################################################
/**
This method can be used to either alter data before it is saved into the database or
to abort a particular operation by throwing an InvalidDataException.
<p>This method is called under four circumstances.
<nl>
<li>When a new DatabaseEntry is being saved - in which case the newData parameter
holds the new data while the oldData parameter will be null.
<li>When a DatabaseEntry is being modified - in which case the newData parameter
holds the new data while the oldData parameter will hold the old data.
<li>When a DatabaseEntry is being deleted - in which case the newData parameter
will be null while the oldData parameter will hold the old data.
<li>When a DatabaseEntry is being read - in which case the newData parameter refers
to the same object as the oldData parameter (i.e. newData == oldData).
</nl>
<p>
 * @param database The Database that holds the data.
 * @param newData The data being saved or changed or read. If you need to modify the data,
 * then modify this parameter.
 * @param oldData The original data before the change or deletion.
 * @exception InvalidDataException If the operation should be disallowed due to bad data.
 */
//===================================================================
public void validateEntry(Database database,DatabaseEntry newData,DatabaseEntry oldData) throws InvalidDataException, IOException;
//===================================================================

//##################################################################
}
//##################################################################


