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
import ewe.io.File;
/**
* This class provides static methods that provide fully portable means of manipulating
* databases.
**/
//##################################################################
public class DataStore{
//##################################################################

//===================================================================
static Database setupNew(String name,Object initializer,ewe.io.DataProcessor decryptor,ewe.io.DataProcessor encryptor) throws ewe.io.IOException
//===================================================================
{
	String pd = File.getProgramDirectory();
	if (pd == null) throw new ewe.io.IOException("Database not found: "+name);
	ewe.io.File f = File.getNewFile(pd);
	if (!f.isDirectory()) throw new ewe.io.IOException("Database not found: "+name);
	f = f.getChild(name);
	ewe.io.RandomAccessStream ras = f.toRandomAccessStream("rw");
	try{
		DataStorage ds = new DataStorage(ras,decryptor,encryptor);
		ds.setDatastoreFile(f);
		DataEntry root = ds.getRootEntry();
		DataEntry child = root.find("Database",true);
		if (child == null) throw new ewe.io.IOException("Can't create database in file.");
		DataTable dt = child.getNewDataTable();
		if (initializer != null && dt.getFields().length == 0){
			dt.setFields(initializer);
			dt.setSorts(initializer);
		}
		dt.save();
		return dt;
	}catch(Throwable t){
		ras.close();
		if (t instanceof ewe.io.IOException) throw (ewe.io.IOException)t;
		else if (t instanceof RuntimeException) throw (RuntimeException)t;
		else throw (ewe.io.IOException)ewe.sys.Vm.setCause(new ewe.io.IOException("Database not found in file."),t);
	}
}
/**
 * Open a Database given the database name. This method assumes that a single
 * database exists within a single stored entity on the device - which is the most
 * portable assumption to make. On systems that support
 * a file system (e.g. Win32/WinCE/Linux) the stored entity will be a DataStorage formatted file
 * with a DataTable stored in it. On systems that do not (e.g. PalmOS) the stored entity
 * will be a standard Palm database file (".pdb" file).
 *
 * @param name A name for the database. Avoid using fully qualified paths and extensions. On
 * simple OSes like the PalmOS, the database name will be mapped to the appropriate database
	on the device. On devices with a file system a ".dat" extension will be added (if it is not
	already present in the name) and the file will be searched for as a file or resource.
 * @param mode A string that should be "r" or "rw".
	 @param initializer An optional object that can be used to initialize the fields and sorts of the database,
		if the database is being opened and created for the first time. If it is null then no initialization
		of a new database is done.
* @param decryptor An optional decryptor used to decode the database if it is encrypted.
* @param encryptor An optional encryptor used to encoded the database - used only when writing.
 * @return An open Database object.
* @exception ewe.io.IOException If there is an error opening the file.
* @exception IllegalArgumentException
*/
//===================================================================
public static Database openDatabase(String name,String mode,Object initializer,ewe.io.DataProcessor decryptor,ewe.io.DataProcessor encryptor) throws ewe.io.IOException, IllegalArgumentException
//===================================================================
{
	if (!name.toUpperCase().endsWith(".DAT")) name += ".dat";
	File d = File.getNewFile(File.getProgramDirectory()).getChild(name);
	ewe.io.RandomAccessStream ras = null;
	try{
		ras = ewe.sys.Vm.openRandomAccessStream(d.getFullPath(),mode);
		//ewe.sys.Vm.debug("I opened: "+name);
	}catch(ewe.io.IOException e){
		if (!mode.equals("rw")) throw new ewe.io.IOException("Database not found: "+name);
		//ewe.sys.Vm.debug("I am setting up: "+name);
		//File f = new File(File.getProgramDirectory()).getChild(name);
		//ewe.sys.Vm.debug("Exists: "+f+" = "+f.exists());
		return setupNew(name,initializer,decryptor,encryptor);
	}
	DataStorage ds = new DataStorage(ras,decryptor,encryptor);
	ds.setDatastoreFile(d);
	try{
		DataEntry root = ds.getRootEntry();
		DataEntry child = root.find("Database",false);
		DataTable dt = null;
		if (child == null) {
			if (!mode.equals("rw")) {
				ds.close();
				throw new ewe.io.IOException("Database not found in file.");
			}
				child = root.makeNewChild();
				dt = child.getNewDataTable();
				child.saveObject("Database",dt);
		}else{
				dt = child.getDataTable();
				if (dt == null) throw new ewe.io.IOException("Database not found in file.");
		}
		if (initializer != null && dt.getFields().length == 0){
			dt.setFields(initializer);
			dt.setSorts(initializer);
			dt.save();
		}
		return dt;
	}catch(Throwable t){
		ds.close();
		if (t instanceof ewe.io.IOException) throw (ewe.io.IOException)t;
		else if (t instanceof RuntimeException) throw (RuntimeException)t;
		else throw (ewe.io.IOException)ewe.sys.Vm.setCause(new ewe.io.IOException("Database not found in file."),t);
	}
}
/**
 * Open a Database given the database name. This method assumes that a single
 * database exists within a single stored entity on the device - which is the most
 * portable assumption to make. On systems that support
 * a file system (e.g. Win32/WinCE/Linux) the stored entity will be a DataStorage formatted file
 * with a DataTable stored in it. On systems that do not (e.g. PalmOS) the stored entity
 * will be a standard Palm database file (".pdb" file).
 *
 * @param name A name for the database. Avoid using fully qualified paths and extensions. On
 * simple OSes like the PalmOS, the database name will be mapped to the appropriate database
	on the device. On devices with a file system a ".dat" extension will be added (if it is not
	already present in the name) and the file will be searched for as a file or resource.
 * @param mode A string that should be "r" or "rw".
 * @return An open Database object.
* @exception ewe.io.IOException If there is an error opening the file.
* @exception IllegalArgumentException
*/
//===================================================================
public static Database openDatabase(String name,String mode) throws ewe.io.IOException, IllegalArgumentException
//===================================================================
{
	return openDatabase(name,mode,null,null,null);
}
/**
 * Open a Database given the database name. This method assumes that a single
 * database exists within a single stored entity on the device - which is the most
 * portable assumption to make. On systems that support
 * a file system (e.g. Win32/WinCE/Linux) the stored entity will be a DataStorage formatted file
 * with a DataTable stored in it. On systems that do not (e.g. PalmOS) the stored entity
 * will be a standard Palm database file (".pdb" file).
 *
 * @param name A name for the database. Avoid using fully qualified paths and extensions. On
 * simple OSes like the PalmOS, the database name will be mapped to the appropriate database
	on the device. On devices with a file system a ".dat" extension will be added (if it is not
	already present in the name) and the file will be searched for as a file or resource.
 * @param mode A string that should be "r" or "rw".
 * @return An open Database object.
* @exception ewe.io.IOException If there is an error opening the file.
* @exception IllegalArgumentException
*/
//===================================================================
public static Database openDatabase(String name,String mode,Object initializer) throws ewe.io.IOException, IllegalArgumentException
//===================================================================
{
	return openDatabase(name,mode,initializer,null,null);
}
/**
 * This is used to initialize a database if it has not yet been initialized. If the database
 * already exists it will return null, otherwise it will attempt to create the database and
 * return it so that you can initialize it.
 * @param name A name for the database. Avoid using fully qualified paths and extensions. On
 * simple OSes like the PalmOS, the database name will be mapped to the appropriate database
	on the device. On devices with a file system a ".dat" extension will be added (if it is not
	already present in the name) and the file will be searched for as a file or resource.
	 @param initializer An optional object that can be used to initialize the fields and sorts of the database.
* @param decryptor An optional decryptor used to decode the database if it is encrypted.
* @param encryptor An optional encryptor used to encoded the database - used only when writing.
 * @return If the database is already initialized it will return null, otherwise it will create
 * the storage for the database and return the database uninitialized except for fields and sorts as specified
	by the optional intializer object.
 * You should then setup the database and then call save() on it.
 * @exception ewe.io.IOException If the database cannot be created, or is improperly formatted.
 */
//===================================================================
public static Database initializeDatabase(String name,Object initializer,ewe.io.DataProcessor decryptor,ewe.io.DataProcessor encryptor) throws ewe.io.IOException
//===================================================================
{
	if (!name.toUpperCase().endsWith(".DAT")) name += ".dat";
	File d = File.getNewFile(File.getProgramDirectory()).getChild(name);
	ewe.io.RandomAccessStream ras = null;
	try{
		ras = ewe.sys.Vm.openRandomAccessStream(d.getFullPath(),"r");
		ras.close();
		return null;
	}catch(Exception e){
	}
	return setupNew(name,initializer,decryptor,encryptor);
}
/**
 * This is used to initialize a database if it has not yet been initialized. If the database
 * already exists it will return null, otherwise it will attempt to create the database and
 * return it so that you can initialize it.
 * @param name A name for the database. Avoid using fully qualified paths and extensions. On
 * simple OSes like the PalmOS, the database name will be mapped to the appropriate database
	on the device. On devices with a file system a ".dat" extension will be added (if it is not
	already present in the name) and the file will be searched for as a file or resource.
	 @param initializer An optional object that can be used to initialize the fields and sorts of the database.
 * @return If the database is already initialized it will return null, otherwise it will create
 * the storage for the database and return the database uninitialized except for fields and sorts as specified
	by the optional intializer object.
 * You should then setup the database and then call save() on it.
 * @exception ewe.io.IOException If the database cannot be created, or is improperly formatted.
 */
//===================================================================
public static Database initializeDatabase(String name,Object initializer) throws ewe.io.IOException
//===================================================================
{
	return initializeDatabase(name,initializer,null,null);
}
//##################################################################
}
//##################################################################

