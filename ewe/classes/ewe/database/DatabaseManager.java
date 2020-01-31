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
import ewe.io.FileOutputStream;
import ewe.io.IOException;
import ewe.io.PrintWriter;
import ewe.sys.Lock;
import ewe.sys.Time;

//##################################################################
public class DatabaseManager{
//##################################################################

private static DatabaseMaker defaultMaker;
private static Lock logLock = new Lock();

//===================================================================
public static String getVersion(){return "1.00";}
//===================================================================

//===================================================================
public static PrintWriter openLog() throws IOException
//===================================================================
{
	logLock.synchronize();
	try{
		return new PrintWriter(new FileOutputStream("/EweDBLog.txt",true));
	}catch(IOException e){
		try{
			return new PrintWriter(new FileOutputStream("EweDBLog.txt",true));
		}catch(IOException e2){
			logLock.release();
			throw e2;
		}
	}
}
//===================================================================
public static void closeLog(PrintWriter log) throws IOException
//===================================================================
{
	try{
		log.close();
	}finally{
		logLock.release();
	}
}
//===================================================================
public static boolean logError(String toLog)
//===================================================================
{
	try{
		PrintWriter pw = openLog();
		try{
			pw.println(new Time().format("dd-MMM-yyyy, hh:mm:ss"));
			pw.println(toLog);
			pw.println("------------------");
		}finally{closeLog(pw);}
		return true;
	}catch(IOException e){
		return false;
	}
}
//===================================================================
public static DatabaseMaker getDefaultDatabaseMaker()
//===================================================================
{
	if (defaultMaker == null) defaultMaker = new DefaultDatabaseMaker();
	return defaultMaker;
}
//===================================================================
public static void setDefaultDatabaseMaker(DatabaseMaker maker)
//===================================================================
{
	defaultMaker = maker;
}
//===================================================================
static Database setupNew(DatabaseMaker maker,String name,Object initializer,ewe.io.DataProcessor decryptor,ewe.io.DataProcessor encryptor) throws ewe.io.IOException
//===================================================================
{
/*
	String pd = File.getProgramDirectory();
	if (pd == null) throw new ewe.io.IOException("Database not found: "+name);
	File f = File.getNewFile(pd);
	if (!f.isDirectory()) throw new ewe.io.IOException("Database not found: "+name);
	f = f.getChild(name);
*/
	Database db = maker.openDatabase(name,"rw");
	if (encryptor != null && decryptor != null) db.setEncryption(decryptor,encryptor);
	if (initializer != null){
		db.ensureFields(initializer,null,null);
		db.ensureSorts(initializer,null);
		if (initializer instanceof DataValidator)
			db.setDataValidator((DataValidator)initializer);
	}
	db.save();
	return db;
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
public static Database openDatabase(DatabaseMaker maker,String name,String mode,Object initializer,ewe.io.DataProcessor decryptor,ewe.io.DataProcessor encryptor) throws ewe.io.IOException, IllegalArgumentException
//===================================================================
{
	if (maker == null) maker = getDefaultDatabaseMaker();
	Database db = maker.openDatabase(name,mode);
	if (decryptor != null)
		db.useEncryption(decryptor,encryptor);
	if (initializer != null){
		db.ensureFields(initializer,null,null);
		db.ensureSorts(initializer,null);
		db.save();
	}
	return db;
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
public static Database openDatabase(DatabaseMaker maker,String name,String mode) throws ewe.io.IOException, IllegalArgumentException
//===================================================================
{
	return openDatabase(maker,name,mode,null,null,null);
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
public static Database openDatabase(DatabaseMaker maker,String name,String mode,Object initializer) throws ewe.io.IOException, IllegalArgumentException
//===================================================================
{
	return openDatabase(maker,name,mode,initializer,null,null);
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
public static Database initializeDatabase(DatabaseMaker maker, String name,Object initializer,ewe.io.DataProcessor decryptor,ewe.io.DataProcessor encryptor) throws ewe.io.IOException
//===================================================================
{
	if (maker == null) maker = getDefaultDatabaseMaker();
	if (maker.databaseExists(name)) return null;
	return setupNew(maker,name,initializer,decryptor,encryptor);
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
public static Database initializeDatabase(DatabaseMaker maker,String name,Object initializer) throws ewe.io.IOException
//===================================================================
{
	return initializeDatabase(maker,name,initializer,null,null);
}

//===================================================================
public static boolean databaseExists(DatabaseMaker maker,String name)
//===================================================================
{
	if (maker == null) maker = getDefaultDatabaseMaker();
	return maker.databaseExists(name);
}
//===================================================================
public static boolean databaseIsValid(DatabaseMaker maker,String name)
//===================================================================
{
	if (maker == null) maker = getDefaultDatabaseMaker();
	return maker.databaseIsValid(name);
}

/*
private static DatabaseObject toDebug;
private static Object ia;
public static void startDebug(Database db,Object[] intArrays)
{
	toDebug = (DatabaseObject)db;
	logError("Starting deep debug!");
	Vm.debugObject(ia = intArrays,1000);
	Vm.debug("IAs are: "+
			Integer.toHexString(System.identityHashCode(intArrays[0]))+
			", "+
			Integer.toHexString(System.identityHashCode(intArrays[1])));
}
public static void deepDebug(String msg)
{
	if (toDebug == null) return;
	toDebug.traceFounds(msg);
}
public static void deepDebug(int value)
{
	logError("VM debug: "+value);
	if (toDebug == null) return;
	toDebug.traceFounds("VM("+value+")");
}
*/
//##################################################################
}
//##################################################################


