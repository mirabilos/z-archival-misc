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
import ewe.io.FileNotFoundException;
import ewe.io.ReadOnlyException;
import ewe.io.File;
import ewe.io.RandomAccessStream;
import ewe.io.RandomStreamAdapter;
import ewe.io.CompressedRandomStream;
import ewe.util.Debug;
//##################################################################
class DefaultDatabaseMaker implements DatabaseMaker{
//##################################################################

//-------------------------------------------------------------------
protected File databaseNameToFile(String name)
//-------------------------------------------------------------------
{
	if (!name.toUpperCase().endsWith(".DB")) name += ".db";
	if (name.charAt(0) == '\\' || name.charAt(0) == '/' || name.indexOf(':') != -1)
		return File.getNewFile(name);
	else
		return File.getNewFile(File.getProgramDirectory()).getChild(name);
}
/**
 * This should open the specified database. If mode is "rw" and the Database
 * does not exist, it should create it and initialize it as being empty.
 * @param name The name for the database (either with our without a file extension).
 * @param mode The mode for opening, either "r" or "rw".
 * @return The open Database.
 * @exception IOException if the database was not found or could not be opened or initialized.
 */
//===================================================================
public Database openDatabase(String name, String mode) throws IOException, ReadOnlyException
//===================================================================
{
	return openDatabase(name,mode,false);
}
/**
 * This should open the specified database. If mode is "rw" and the Database
 * does not exist, it should create it and initialize it as being empty.
 * @param name A File object referring to the Database (either with our without a file extension).
 * @param mode The mode for opening, either "r" or "rw".
 * @return The open Database.
 * @exception IOException if the database was not found or could not be opened or initialized.
 */
//===================================================================
public Database openDatabase(File name, String mode) throws IOException, ReadOnlyException
//===================================================================
{
	return openDatabase(name,mode,false);
}
/**
 * This should open the specified database. If mode is "rw" and the Database
 * does not exist, it should create it and initialize it as being empty.
 * @param name The name for the database (either with our without a file extension).
 * @param mode The mode for opening, either "r" or "rw".
 * @return The open Database.
 * @exception IOException if the database was not found or could not be opened or initialized.
 */
//===================================================================
public Database openDatabase(String name, String mode, boolean ignoreInconsistentState) throws IOException, ReadOnlyException
//===================================================================
{
	return openDatabase(databaseNameToFile(name),mode,ignoreInconsistentState);
}
/**
 * This should open the specified database. If mode is "rw" and the Database
 * does not exist, it should create it and initialize it as being empty.
 * @param name A File object referring to the Database (either with our without a file extension).
 * @param mode The mode for opening, either "r" or "rw".
 * @return The open Database.
 * @exception IOException if the database was not found or could not be opened or initialized.
 */
//===================================================================
public Database openDatabase(File name, String mode, boolean ignoreInconsistentState) throws IOException, ReadOnlyException
//===================================================================
{
	if (!name.exists()){
		if (!mode.equalsIgnoreCase("rw")) throw new FileNotFoundException();
		else return new RecordFile(name,mode,ignoreInconsistentState);
	}
	if (CompressedRandomStream.isCompressedRandomStream(name)){
		if (!mode.equalsIgnoreCase("r")) throw new ReadOnlyException("Compressed Database can only be opened in read-only mode.");
		return new RecordFile(new RandomStreamAdapter(new CompressedRandomStream(
			name.toRandomAccessStream("r"))
			),"r",ignoreInconsistentState);
	}
	return new RecordFile(name,mode,ignoreInconsistentState);
}

//===================================================================
public boolean databaseExists(String name)
//===================================================================
{
	return databaseExists(databaseNameToFile(name));
}
//===================================================================
public boolean databaseExists(File name)
//===================================================================
{
	if (!name.exists()) return false;
	return true;
}
//===================================================================
public boolean databaseIsValid(String name)
//===================================================================
{
	return databaseIsValid(databaseNameToFile(name));
}
//===================================================================
public boolean databaseIsValid(File name)
//===================================================================
{
	if (!name.exists()) return false;
	try{
		Database db = openDatabase(name,"r");
		db.close();
	}catch(Exception e){
		return false;
	}
	return true;
}
//===================================================================
public boolean canOpenForWriting(String name)
//===================================================================
{
	return canOpenForWriting(databaseNameToFile(name));
}
//===================================================================
public boolean canOpenForWriting(File name)
//===================================================================
{
	try{
		if (!CompressedRandomStream.isCompressedRandomStream(name)) return true;
		return false;
	}catch(IOException e){
		return false;
	}
}
//##################################################################
}
//##################################################################

