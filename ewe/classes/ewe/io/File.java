/*********************************************************************************
 *  Ewe Virtual Machine - Version 1.14, January 24, 2002                         *
 *  Copyright (C) 1999-2002 Michael L Brereton <michael_brereton@ewesoft.com>    *
 *  Copyright (C) 1998, 1999 Wabasoft <www.wabasoft.com>                         *
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
package ewe.io;
import ewe.sys.Time;
import ewe.sys.Handle;
import ewe.reflect.Type;
import ewe.ui.Form;
/**
 * File is a file or directory (similar to the Java File) - it is not used for I/O.
 * Use RandomAccessFile for file I/O or File.getInputStream(), File.getOutputStream() instead.
 * <p>
 * The File class will not work under the PalmPilot since it does not
 * contain a filesystem.
 * <p>
 * Note that ewe.io.File objects ARE mutable. The set() method may be used to change
 * the file which is being referred to.
 */
public class File extends FileBase
{

//-------------------------------------------------------------------
protected File(){};
//-------------------------------------------------------------------

//-------------------------------------------------------------------
protected File getNewInstance()
//-------------------------------------------------------------------
{
	return new File();
}
/**
 * Create a File Object which represents a file on the file system. The file name
 * may specify a file or directory which may or may not exist.
 *
 * Please note that under WindowsCE there is no concept of "Current Working Directory".
 * All file names must have a full path. If you want a file or directory to co-exist within
 * the same directory that your ewe program was installed by the user, then use the call:
 *
 * String pd = (String)new File("").getInfo(File.INFO_PROGRAM_DIRECTORY,null,null,0);
 * File programDirectory = new File(pd);
 *
 * Then use programDirectory as the parent directory for your data files/directories.
 */
//===================================================================
public File(String path) {this((File)null,path);}
//===================================================================
/**
 * Create a File Object which represents a file on the file system under the
 * specified parent directory. The resulting file may be a file or directory
 * which may or may not exist.
 *
 * The parent directory MAY be null - in which case the path must specify the file fully.
 *
 * Please note that under WindowsCE there is no concept of "Current Working Directory".
 * All file names must have a full path. If you want a file or directory to co-exist within
 * the same directory that your ewe program was installed by the user, then use the call:
 *
 * String pd = File.getProgramDirectory();
 * File programDirectory = new File(pd);
 *
 * Then use programDirectory as the parent directory for your data files/directories.
 */
//===================================================================
public File(File parent,String path)
//===================================================================
{
	if (parent == null && path == null) return;
	name = _nativeCreate(parent,path);
}
/**
 * Create a File Object which represents a file on the file system under the
 * specified parent directory. The resulting file may be a file or directory
 * which may or may not exist.
 *
 * The parent directory MAY be null - in which case the path must specify the file fully.
 *
 * Please note that under WindowsCE there is no concept of "Current Working Directory".
 * All file names must have a full path. If you want a file or directory to co-exist within
 * the same directory that your ewe program was installed by the user, then use the call:
 *
 * String pd = File.getProgramDirectory();
 * File programDirectory = new File(pd);
 *
 * Then use programDirectory as the parent directory for your data files/directories.
 */
//===================================================================
public File(String parent,String path)
//===================================================================
{
	this(parent == null ? null : new File(parent),path);
}
/**
* Modifies the File to point to a different file on the file system. This
* means that ewe.io.File objects are not immutable, but this avoids a lot
* of object creation when dealing with large directories.
**/
//===================================================================
public void set(File directory,String path) {name = _nativeCreate(directory,path);}
//===================================================================
//-------------------------------------------------------------------
private native String _nativeCreate(File directory,String path);
//-------------------------------------------------------------------
/**
 * Creates a directory. Returns true if the operation is successful and false
 * otherwise.
 */
//===================================================================
public native boolean createDir();
//===================================================================
/**
 * Deletes the file or directory. Returns true if the operation is
 * successful and false otherwise.
 */
//===================================================================
public native boolean delete();
//===================================================================
/** Returns true if the file exists and false otherwise. */
//===================================================================
public native boolean exists();
//===================================================================
/**
	Returns the length of the file in bytes.
*/
//===================================================================
public native int getLength();
//===================================================================
/**
 * Lists the files contained in a directory. The strings returned are the
 * names of the files and directories contained within this directory.
 * This method returns null if the directory can't be read or if the
 * operation fails. If mask is null or "*" then all files will be listed.
 */
//-------------------------------------------------------------------
private native String [] doList(String mask,int listAndSortOptions);
//-------------------------------------------------------------------
/**
 * List files asynchronously. NOTE: version 1.16 of Ewe does not list the files
 * asynchronously - it is synchronous. Therefore it always returns a Handle that
 * is completed.<p>
	By default this method calls the doList() method and then returns a completed Handle
	that either indicates success or failure. Methods that inherit from FileBase are encouraged
	to provide a better version of this (preferrable one that spawns a thread).
 * @param mask The a file maks using '*' characters.
 * @param listAndSortOptions LIST_ and SORT_ options ORed together.
 * @return A Handle which can be used to monitor the progress of the operation.
 */
//===================================================================
public Handle listFiles(String mask,int listAndSortOptions)
//===================================================================
{
	String [] got = doList(mask,listAndSortOptions);
	if (got == null) return new Handle(Handle.Failed,null);
	else return new Handle(Handle.Succeeded,got);
}

/**
 * Return the length of the file in bytes.
 */
//===================================================================
public long length()
//===================================================================
{
	return getLength();
}
/**
* Return the fully qualified pathname of the file. Note that this
* will never end with a "/" EXCEPT for the root directory on a disk.
**/
//===================================================================
public native String getFullPath();
//===================================================================
/** Returns true if the file is a directory and false otherwise. */
//===================================================================
public native boolean isDirectory();
//===================================================================
/**
* This moves/renames the file to the destination newFile. The newFile
* should not exist.
**/
//===================================================================
public native boolean move(File newFile);
//===================================================================
/**
* This tells the system to delete the file when the program exits.
**/
//===================================================================
public native void deleteOnExit();
//===================================================================

//-------------------------------------------------------------------
protected native void getSetModified(Time time,boolean doGet);
//-------------------------------------------------------------------
/**
* This method is used to get extended information about the File or the File system in general.
* It is used with the INFO_ specifiers and options.
**/
//===================================================================
public Object getInfo(int infoCode,Object sourceParameters,Object resultDestination,int options)
//===================================================================
{
	switch(infoCode){
	case INFO_TEMPORARY_DIRECTORY:
		Object ret = nativeGetInfo(infoCode,sourceParameters,resultDestination,options);
		if (ret != null) return ret;
		return nativeGetInfo(INFO_PROGRAM_DIRECTORY,sourceParameters,resultDestination,options);
	case INFO_DEVICE_NAME:
		return "My Computer";
	case INFO_FLAGS:
	case INFO_SYSTEM_TYPE:
		if (!(resultDestination instanceof ewe.sys.Long))
			resultDestination = new ewe.sys.Long();
		((ewe.sys.Long)resultDestination).value = (infoCode == INFO_SYSTEM_TYPE) ? DOS_SYSTEM : 0;
		nativeGetInfo(infoCode,sourceParameters,resultDestination,options);
		return resultDestination;
	case INFO_FILE_TIMES:
		if (!(resultDestination instanceof Time []))
			resultDestination = new Time[3];
		Time [] times = (Time [])resultDestination;
		for (int i = 0; i<3; i++)
			((Time[])resultDestination)[i] = new Time();
		ret = nativeGetInfo(infoCode,sourceParameters,resultDestination,options);
		if (ret == null) {
			times[0] = times[2] = null;
			times[1] = getModified(times[1]);
		}
		return times;
	case INFO_ROOT_LIST:
		//if (true) return null;
		String[] got = (String[])nativeGetInfo(infoCode,sourceParameters,resultDestination,options);
		if (got == null) return got;
		for (int i = 0; i<got.length; i++) got[i] = got[i].toUpperCase();
		return got;
	}
	return nativeGetInfo(infoCode,sourceParameters,resultDestination,options);
}
//-------------------------------------------------------------------
protected native Object nativeGetInfo(int infoCode,Object sourceParameters,Object resultDestination,int options);
//-------------------------------------------------------------------

//===================================================================
public Handle setInfo(int infoCode,Object sourceParameters,int options)
//===================================================================
{
	try{
		switch(infoCode){
		case INFO_OWNER: case INFO_GROUP:
			if (!(sourceParameters instanceof String)) throw new Exception();
		}
		return new Handle(nativeSetInfo(infoCode,sourceParameters,options) ? Handle.Succeeded : Handle.Failed,null);
	}catch(Exception e){
		return new Handle(Handle.Failed,null);
	}
}
//-------------------------------------------------------------------
protected native boolean nativeSetInfo(int infoCode,Object sourceParameters,int options);
//-------------------------------------------------------------------
//-------------------------------------------------------------------
protected int getSetPermissionsAndFlags(boolean isGet, int valuesToSetOrGet, int valuesToClear) throws IOException, IllegalArgumentException
//-------------------------------------------------------------------
{
	int val = nativeGetSetPermissionsAndFlags(isGet, valuesToSetOrGet, valuesToClear);
	if (val == -1) throw new IOException("File not found: "+getFullPath());
	else if (val == -2) throw new IllegalArgumentException("Flag/Permission not supported on this platform.");
	else return val;
}

//-------------------------------------------------------------------
private native int nativeGetSetPermissionsAndFlags(boolean isGet, int valuesToSetOrGet, int valuesToClear);
//-------------------------------------------------------------------
//-------------------------------------------------------------------
private static String fileChooserClass = null;
//-------------------------------------------------------------------

//===================================================================
public static void setFileChooserClass(String className)
//===================================================================
{
	fileChooserClass = className;
}
//===================================================================
public static String getFileChooserClass()
//===================================================================
{
	if (fileChooserClass == null) {
		Type ty = new Type("ewe.filechooser.FileChooser");
		if (ty.exists() && !ewe.ui.Gui.isSmartPhone) return "ewe.filechooser.FileChooser";
		ty = new Type("ewe.io.SimpleFileChooser");
		if (ty.exists()) return "ewe.io.SimpleFileChooser";
		return null;
	}
	return fileChooserClass;
}
//===================================================================
public boolean executeFileChooser(FileChooserParameters fcp)
throws IllegalStateException
//===================================================================
{
	Type ty = new Type(getFileChooserClass());
	if (!ty.exists()) throw new IllegalStateException();
	fcp.defaultTo(fcp.FILE_MODEL,this);
	String p = getParent();
	if (p == null) p = toString();
	if (isDirectory()) p = toString();
	fcp.defaultTo(fcp.START_LOCATION,p);
	Form f = (Form)ty.newInstance("(Lewe/io/FileChooserParameters;)V",new Object[]{fcp});
	if (f == null) throw new IllegalStateException();
	return f.execute() != f.IDCANCEL;
}

}


