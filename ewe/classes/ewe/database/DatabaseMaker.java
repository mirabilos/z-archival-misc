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
import ewe.io.File;
import ewe.io.IOException;

//##################################################################
public interface DatabaseMaker{
//##################################################################
/**
 * This should open the specified database. If mode is "rw" and the Database
 * does not exist, it should create it and initialize it as being empty.
 * @param name The name for the database (either with our without a file extension).
 * @param mode The mode for opening, either "r" or "rw" or "a".
 * @return The open Database.
 * @exception IOException if the database was not found or could not be opened or initialized.
 */
//===================================================================
public Database openDatabase(String name, String mode) throws IOException;
//===================================================================
/**
 * This should open the specified database. If mode is "rw" and the Database
 * does not exist, it should create it and initialize it as being empty.
 * @param name A File object referring to the Database (either with our without a file extension).
 * @param mode The mode for opening, either "r" or "rw".
 * @return The open Database.
 * @exception IOException if the database was not found or could not be opened or initialized.
 */
//===================================================================
public Database openDatabase(File name, String mode) throws IOException;
//===================================================================
/**
 * This should open the specified database. If mode is "rw" and the Database
 * does not exist, it should create it and initialize it as being empty.
 * @param name The name for the database (either with our without a file extension).
 * @param mode The mode for opening, either "r" or "rw" or "a".
 * @return The open Database.
 * @exception IOException if the database was not found or could not be opened or initialized.
 */
//===================================================================
public Database openDatabase(String name, String mode, boolean ignoreInconsistentState) throws IOException;
//===================================================================
/**
 * This should open the specified database. If mode is "rw" and the Database
 * does not exist, it should create it and initialize it as being empty.
 * @param name A File object referring to the Database (either with our without a file extension).
 * @param mode The mode for opening, either "r" or "rw".
 * @return The open Database.
 * @exception IOException if the database was not found or could not be opened or initialized.
 */
//===================================================================
public Database openDatabase(File name, String mode, boolean ignoreInconsistentState) throws IOException;
//===================================================================

//===================================================================
public boolean databaseExists(String name);
//===================================================================
//===================================================================
public boolean databaseExists(File name);
//===================================================================
//===================================================================
public boolean databaseIsValid(String name);
//===================================================================
public boolean databaseIsValid(File name);
//===================================================================
public boolean canOpenForWriting(String name);
//===================================================================
public boolean canOpenForWriting(File name);
//===================================================================

//##################################################################
}
//##################################################################

