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
import ewe.sys.*;
import ewe.util.Vector;
import ewe.util.mString;
import ewe.util.FileComparer;
import ewe.fx.Color;
import ewe.reflect.Type;
import ewe.ui.Form;
/**
* This class is used as base for the Java and abstract versions of File.
**/
//##################################################################
public abstract class FileBase extends FileSys implements ewe.util.Textable, Streamable, FilePermissions{
//##################################################################

protected String name; //Do not move this. It must be first.

public static final int OpenFolderIcon = 1;
public static final int ClosedFolderIcon = 2;
public static final int PageIcon = 3;
public static final int FileIcon = 5;
public static final int DriveIcon = 4;

private static ewe.fx.IImage openFolder;
private static ewe.fx.IImage closedFolder;
private static ewe.fx.IImage page;
private static ewe.fx.IImage drive;

/**
* Get one of the icons: OpenFolderIcon, ClosedFolderIcon and PageIcon.
**/
//===================================================================
public static ewe.fx.IImage getIcon(int whichIcon)
//===================================================================
{
	switch(whichIcon){
		case OpenFolderIcon:
			if (openFolder == null) openFolder = new ewe.fx.mImage("ewe/OpenFolder.bmp",ewe.fx.Color.White);
			return openFolder;
		case ClosedFolderIcon:
			if (closedFolder == null) closedFolder = new ewe.fx.mImage("ewe/ClosedFolder.bmp",ewe.fx.Color.White);
			return closedFolder;
		case PageIcon: case FileIcon:
			if (page == null) page = new ewe.fx.mImage("ewe/Page.bmp","ewe/PageMask.bmp");
			return page;
		case DriveIcon:
			if (drive == null) drive = new ewe.fx.mImage("ewe/Drive.bmp",ewe.fx.Color.White);
			return drive;
		default:
			return null;
	}
}
/** Read-only open mode. */
public static final int READ_ONLY  = RandomAccessFile.READ_ONLY;
/** Write-only open mode. */
public static final int WRITE_ONLY = RandomAccessFile.WRITE_ONLY;
/** Read-write open mode. */
public static final int READ_WRITE = RandomAccessFile.READ_WRITE; // READ | WRITE
/** Create open mode. Used to create a file if one does not exist.
@deprecated READ_WRITE will create a file if one does not exist.*/
public static final int CREATE = RandomAccessFile.CREATE;

protected FileBase(){}
/**
* Modifies the File to point to a different file on the file system. This
* means that ewe.io.File objects are not immutable, but this avoids a lot
* of object creation when dealing with large directories.
**/
//===================================================================
public abstract void set(File directory,String path);
//===================================================================
//-------------------------------------------------------------------
protected abstract File getNewInstance();
//-------------------------------------------------------------------
/**
* Get a new File object given the directory and new path. This is for objects which
* inherit from File. The default implementation of this will call getNewInstance()
* and then call set(File directory,String path).
* If you do not override getNew() you must override getNewInstance().
**/
//===================================================================
public File getNew(File directory,String path)
//===================================================================
{
	File nw = getNewInstance();
	nw.set(directory,path);
	return nw;
}
/**
 * Creates a directory. Returns true if the operation is successful and false
 * otherwise.
 */
//===================================================================
public abstract boolean createDir();
//===================================================================
/**
 * Deletes the file or directory. Returns true if the operation is
 * successful and false otherwise.
 */
//===================================================================
public abstract boolean delete();
//===================================================================
/** Returns true if the file exists and false otherwise. */
//===================================================================
public abstract boolean exists();
//===================================================================
/** Returns true if the file is a directory and false otherwise. */
//===================================================================
public abstract boolean isDirectory();
//===================================================================
/**
* Return the fully qualified pathname of the file. Note that this
* will never end with a "/" EXCEPT for the root directory on a disk.
**/
//===================================================================
public abstract String getFullPath();
//===================================================================
/**
* This tells the system to delete the file when the program exits.
**/
//===================================================================
public abstract void deleteOnExit();
//===================================================================

/**
 * Lists the files contained in a directory. The strings returned are the
 * names of the files and directories contained within this directory.
 * This method returns null if the directory can't be read or if the
 * operation fails. If mask is null or "*" then all files will be listed.
	<p>
	This method calls the asynchronous listFiles() method and then waits for
	the handle to complete. You should override the listFiles() method for
	classes that inherit from FileBase.
* @param mask A file mask.
* @param listAndSortOptions Use the LIST_XXX values OR'ed together.
* @return An array of file names.
*/
//===================================================================
public String [] list(String mask,int listAndSortOptions)
//===================================================================
{
	Handle got = listFiles(mask,listAndSortOptions);
	if (got == null) return null;
	if (!got.waitOnFlags(Handle.Succeeded,TimeOut.Forever)) return null;
	return (String [])got.returnValue;
}
/**
 * Lists the files contained in a directory using a complex mask - which
 * may actually consist of multiple masks (e.g. "*.jpg,*.gif - Image Files.").
* @param compositeMask A possibly complex file mask.
* @param listAndSortOptions Use the LIST_XXX values OR'ed together.
* @return An array of file names.
*/
//===================================================================
public String [] listMultiple(String compositeMask, int listAndSortOptions)
//===================================================================
{
	int idx = compositeMask.indexOf(' ');
	String mask = idx == -1 ? compositeMask : compositeMask.substring(0,idx);
	String[] found;
	if ((mask.indexOf(',') == -1 && mask.indexOf(';') == -1)){
		found = list(mask,listAndSortOptions|LIST_ALWAYS_INCLUDE_DIRECTORIES);
	}else{
		char c = mask.indexOf(',') == -1 ? ';' : ',';
		String masks [] = mString.split(mask,c);
		String dirs [] = new String[0];
		if ((listAndSortOptions & LIST_FILES_ONLY) == 0)
			dirs = list("*.*",LIST_DIRECTORIES_ONLY);
		if ((listAndSortOptions & LIST_DIRECTORIES_ONLY) == 0)
			found = list("*.*",File.LIST_FILES_ONLY|listAndSortOptions);
		else
			found = dirs;

		ewe.util.FileComparer [] fcs = new ewe.util.FileComparer[masks.length];

		for (int i = 0; i<masks.length; i++)
			fcs[i] = new FileComparer((File)this,ewe.sys.Vm.getLocale(),listAndSortOptions,masks[i]);
		int left = found.length;
		for (int i = 0; i<found.length; i++){
			boolean matched = false;
			for (int f = 0; f<fcs.length; f++){
				if (fcs[f].matches(found[i])){
					matched = true;
					break;
				}
			}
			if (!matched) {
				found[i] = null;
				left--;
			}
		}
		String [] isMatching = new String[dirs.length+left];
		ewe.sys.Vm.copyArray(dirs,0,isMatching,0,dirs.length);
		for (int i = 0, d = dirs.length; i<found.length; i++)
			if (found[i] != null)
				isMatching[d++] = found[i];
		found = isMatching;
	}
	return found;
}
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
public abstract Handle listFiles(String mask,int listAndSortOptions);
//===================================================================

public static final int LIST_DESCENDING = 0x1;
public static final int LIST_DIRECTORIES_LAST = 0x2;
public static final int LIST_DIRECTORIES_ONLY = 0x4;
public static final int LIST_FILES_ONLY = 0x8;
public static final int LIST_BY_DATE = 0x10;
public static final int LIST_BY_TYPE = 0x20;
public static final int LIST_DONT_SORT = 0x40;
/**
* This option treats directories and files as being the same.
**/
public static final int LIST_IGNORE_DIRECTORY_STATUS = 0x80;
/**
* This option lists all directories even if they don't match the supplied
* mask.
**/
public static final int LIST_ALWAYS_INCLUDE_DIRECTORIES = 0x100;
/**
* This option request that only a check for matching children is done, rather
* than a listing. It will cause list() to behave as follows:
* No matching children = return null.
* At least one child = return a String array of 0 length.
**/
public static final int LIST_CHECK_FOR_ANY_MATCHING_CHILDREN = 0x200;
public static final int LIST_BY_SIZE = 0x400;
/**
* This gets/sets the modified time of the file.
* should not exist.
**/
//-------------------------------------------------------------------
protected abstract void getSetModified(Time time,boolean doGet);
//-------------------------------------------------------------------
/**
* This moves/renames the file to the destination new File. The new File
* should not exist.
**/
//===================================================================
public abstract boolean move(File newFile);
//===================================================================

private static char dirSep = 0, pathSep = 0;
/**
* This converts any '/' directory separators to the one that is native to the running OS (ie '\' on Windows)
* and any ';' path separtors to the one that is native to the running OS (ie ':' on Unix/Linux).<p>
* Your initial constructed path(s) SHOULD use '/' for directories and ';' for path separators.<p>
* This is generally only necessary for a few system dependant functions and is not necessary for
* standard file access.
**/
//===================================================================
public static String toSystemDependantPath(String path)
//===================================================================
{
	if (path == null) return path;
	if (dirSep == 0){
		try{
			String got = ewe.sys.Vm.getProperty("file.separator","/");
			dirSep = got.charAt(0);
			got = ewe.sys.Vm.getProperty("path.separator",";");
			pathSep = got.charAt(0);
		}catch(Exception e){
			if (dirSep == 0) dirSep = '/';
			if (pathSep == 0) pathSep = ';';
		}
	}
	if (dirSep != '/') path = path.replace('/',dirSep);
	if (pathSep != ';') path = path.replace(';',pathSep);
	return path;
}
//===================================================================
public boolean renameTo(File newFile)
//===================================================================
{
	return move(newFile);
}
private Time tempTime = new Time();

//===================================================================
public long lastModified()
//===================================================================
{
	getSetModified(tempTime,true);
	return tempTime.getTime();
}
//===================================================================
public boolean setLastModified(long time)
//===================================================================
{
	tempTime.setTime(time);
	getSetModified(tempTime,false);
	return true;
}
/**
* Lists all files on the directory that this File object
* represents, sorted by name - with directories listed first.
* This simply calls the list(String mask,int listAndSortOptions) method
* with the parameters null and 0.
**/
//===================================================================
public String [] list()
//===================================================================
{
	return list(null,0);
}

/**
 * List all files in this directory using the specified filter.
 * @param filter The filter to use for accepting/rejecting files.
 * @return An array of file names that satisfy the filter.
 */
//===================================================================
public String [] list(FilenameFilter filter)
//===================================================================
{
	if (filter == null) throw new NullPointerException();
	String [] got = list();
	if (got == null) return null;
	Vector v = new Vector();
	for (int i = 0; i<got.length; i++)
		if (filter.accept((File)this,got[i])) v.add(got[i]);
	String [] ret = new String[v.size()];
	v.copyInto(ret);
	return ret;
}
/**
* Get a new File object given the directory and new path. This is for objects which
* inherit from File.
**/
//===================================================================
public File getNew(String path) {return getNew(null,path);}
//===================================================================
public File getChild(String path) {return getNew((File)this,path);}
//===================================================================
/** Returns true if the file exists and can be read. */
//===================================================================
public boolean canRead()
//===================================================================
{
	return (exists() && !isDirectory());
}
//===================================================================
public boolean canWrite()
//===================================================================
{
	return (exists() && !isDirectory());
}
//===================================================================
public boolean isFile()
//===================================================================
{
	return (exists() && !isDirectory());
}
/** Return the file's path, as specified when created. */
//===================================================================
public String getCreationName() { return name; }
//===================================================================

//===================================================================
public boolean isHidden() {return false;}
//===================================================================

//-------------------------------------------------------------------
private static int getPastSeparator(String str,int start)
//-------------------------------------------------------------------
{
	int i;
	for (i = start; i>=0; i--){
		char c = str.charAt(i);
		if (c == '\\' || c == '/' || c == ':') continue;
		else break;
	}
	if (i<0) return i;
	for (i--;i>=0; i--){
		char c = str.charAt(i);
		if (c == '\\' || c == '/' || c == ':') return i;
	}
	return -1;
}
//-------------------------------------------------------------------
private static int getParentEnd(String str)
//-------------------------------------------------------------------
{
	if (str == null) return -1;
	int len = str.length();
	if (len == 0) return -1;
	for (int i = len-1; i>=0; i--){
		char c = str.charAt(i);
		if (c == '\\' || c == '/' || c == ':'){
			if (i == len-1) return getPastSeparator(str,i);
			else return i;
		}else
			if (i == 0) return -1;
	}
	return -1;
}
/** Return only the filename and extension of the file. **/
//===================================================================
public String getFileExt()
//===================================================================
{
	return getFileExt(getFullPath());
}
/** Return only the drive and path of the file. **/
//===================================================================
public String getDrivePath()
//===================================================================
{
	return getDrivePath(getFullPath());
}
//===================================================================
public static String getFileExt(String str)
//===================================================================
{
	//str = str.substring(str.indexOf(':')+1).replace('\\','/');
	int p = getParentEnd(str);
	if (p == -1) return str;
	return str.substring(p+1,str.length());
}
//===================================================================
public static String getDrivePath(String str)
//===================================================================
{
	String fe = getFileExt(str);
	if (fe.length() <= str.length())
		return str.substring(0,str.length()-fe.length());
	else
		return new String();
}

/**
 * Utility to create a path name given a parent and child.
 * @param parent The parent File - which may be null.
 * @param child The child name.
 * @return The correct path name representing the child within the parent directory.
 */
//===================================================================
public static String makePath(File parent,String child)
//===================================================================
{
	return makePath(parent == null ? "" : parent.getFullPath(),child);
}
/**
 * Utility to create a path name given a parent and child.
 * @param parent The parent path - which may be null.
 * @param child The child name.
 * @return The correct path name representing the child within the parent directory.
 */
//===================================================================
public static String makePath(String parent,String child)
//===================================================================
{
	if (parent == null) parent = new String();
	if (parent.length() != 0){
		char c = parent.charAt(parent.length()-1);
		if (c == '/' || c == ':' || c == '\\')
			;
		else parent += "/";
	}
	if (child == null) child = new String();
	return parent+child;
}

//===================================================================
public String getParent()
//===================================================================
{
	String str = getFullPath();
	//str = str.substring(str.indexOf(':')+1).replace('\\','/');
	int p = getParentEnd(str);
	if (p == -1) return null;
	char c = (p == 0) ? str.charAt(p) : str.charAt(p-1);
	if (c == '\\' || c == '/' || c == ':') p++;
	return str.substring(0,p);
}
/**
* Get a File object representing the parent direcotry of this File. This is provided
* so you don't have to decypher the path. Returns null if there is no parent.
**/
//===================================================================
public File getParentFile()
//===================================================================
{
	String got = getParent();
	if (got == null) return null;
	return getNew(null,got);



}
//-------------------------------------------------------------------
private static int curTemp = 1000;
//-------------------------------------------------------------------
/**
* This creates an empty temporary file with the specified prefix and suffix. If the
* suffix is null then ".tmp" will be used. If the dir is null, the system temporary
* folder will be used. Note that this method may return a file name that is different
* from the requested file name. This depends on the underlying system and how it provides
* temporary file support.
**/
//===================================================================
public File createTempFile(String prefix,String suffix,File dir)
//===================================================================
{
	if (suffix == null) suffix = "tmp";
	if (prefix == null) prefix = "temp";
	if (dir == null){
		String d = (String)getNew("").getInfo(INFO_TEMPORARY_DIRECTORY,null,null,0);
		if (d == null) return null;
		dir = getNew(d);
		if (!dir.isDirectory()) return null;
	}
	while(true){
		String child = prefix+(++curTemp)+"."+suffix;
		File tryFile = getNew(dir,child);
		if (!tryFile.exists()){
			Stream raf = tryFile.getOutputStream();
			if (raf != null) raf.close();
			tryFile.deleteOnExit();
			return tryFile;
		}
	}
}
/**
* This creates an empty file with a specific name in a temporary directory. If the file
* already exists in the directory it will be deleted and the new one will be created. If
* dir is null then the file will be created in the system temporary directory.
**/
//===================================================================
public File createTempFile(String fileName,File dir)
//===================================================================
{
	File temp = createTempFile(null,null,dir);
	if (temp == null) return null;
	if (dir == null) dir = temp.getParentFile();
	File want = getNew(dir,fileName);
	want.delete();
	if (!temp.rename(fileName)) return null;
	File other = getNew(dir,fileName);
	other.deleteOnExit();
	return want;
}
/**
* This renames the file in place. It does not move or change the
* directory of the file. The newName parameter is a single name without
* directory specifiers within it.
**/
//===================================================================
public boolean rename(String newName)
//===================================================================
{
	File f = getParentFile();
	if (f == null) return false;
	return move(getNew(f,newName));
}
//===================================================================
/**
 * Return the Time the File was last modified.
 * @param dest an optional destination Time. If it is null a new one will be
 * created and returned.
 * @return the destination Time or a new one if it was null.
 */
public Time getModified(Time dest)
//===================================================================
{
	if (dest == null) dest = new Time();
	getSetModified(dest,true);
	return dest;
}
//===================================================================
/**
 * Set the last modified time for the file.
 * @param modifiedTime the time to set as the last modified time.
 */
public void setModified(Time modifiedTime)
//===================================================================
{
	if (modifiedTime == null) modifiedTime = new Time();
	getSetModified(modifiedTime,false);
}
/**
* This requests the icon for the file - returns an IImage object.
* sourceParameters - none, resultDestination - unused,
* options - INFO_ICON_SMALL, INFO_ICON_MEDIUM, INFO_ICON_LARGE.
* Returns null if no icon could be found.
**/
public static final int INFO_ICON = 0x1;
public static final int INFO_ICON_SMALL = 0x1;
public static final int INFO_ICON_MEDIUM = 0x2;
public static final int INFO_ICON_LARGE = 0x3;
/**
* This requests the names of the root directory of all drives - returns an array of Strings.
* sourceParameters - none, resultDestination - unused,
* options - none defined yet.
* If this is not supported (e.g. under WinCE) it will return null and this implies that there
* is only one root i.e. "/"
**/
public static final int INFO_ROOT_LIST = 0x2;
/**
* NOT IMPLEMENTED YET.
* If this File is considered a link/shortcut to another file or directory, this will return
* the name of the target file/directory, otherwise it will return null.
* sourceParamerters - none, resultDestination - unused,
* options - none defined yet.
**/
public static final int INFO_LINK_DESTINATION = 0x3;
/**
* This returns a String which is the directory where the program is installed. Under Java,
* if this cannot be determined, then the current working directory is returned. This is provided
* because there is no concept of "Current Working Directory" in WindowsCE.
**/
public static final int INFO_PROGRAM_DIRECTORY = 0x4;
/**
* This returns a String which is the directory where temporary files can be created.
**/
public static final int INFO_TEMPORARY_DIRECTORY = 0x5;
/**
* This returns a String which is the name of the computer.
**/
public static final int INFO_DEVICE_NAME = 0x6;
/**
* This gets a set of File or device specific flags. For FLAG_SLOW_ACCESS indicates that directory
* access/listing on this device is very slow (e.g. over a serial line).
**/
public static final int INFO_FLAGS = 0x7;

public static final int FLAG_SLOW_LIST = 0x10;
public static final int FLAG_SLOW_CHILD_COUNT = 0x20;
/**
* This is a flag returned from getInfo(INFO_FLAGS);
**/
public static final int FLAG_SLOW_ACCESS = FLAG_SLOW_LIST|FLAG_SLOW_CHILD_COUNT;
/**
* This is a flag returned from getInfo(INFO_FLAGS);
* @deprecated - use getPermissionsAndFlags() and changePermissionsAndFlags()
**/
public static final int FLAG_READ_ONLY = 0x2;
/**
* This is a flag returned from getInfo(INFO_FLAGS);
**/
public static final int FLAG_FILE_SYSTEM_IS_READ_ONLY = 0x4;
/**

* This is a flag returned from getInfo(INFO_FLAGS);
**/
public static final int FLAG_CASE_SENSITIVE = 0x8;

/**
* This returns an IImage representing the device on which the file is stored.
**/
public static final int INFO_DEVICE_ICON = 0x8;
/**
* This returns a Long specifying the number of free bytes on the disk, or null if it cannot be determined.
**/
public static final int INFO_FREE_DRIVE_SPACE = 9;
/**

* This returns a Long specifying the total number of bytes on the the disk, or null if it cannot be determined.
**/
public static final int INFO_TOTAL_DRIVE_SPACE = 10;
/**
* This should return an array of Strings giving the details to display about a file in the filechooser box. It
* should not include the name of the file - which is always displayed.
**/
public static final int INFO_DETAIL_NAMES = 11;
/**
* This should return EITHER an array of ints giving the width of each detail column (except for name) or an
* array of Strings which represent the widest string expected for each column. You should provide a PropertyList
* with an entry for "fontMetrics" being the FontMetrics being used by the table.
**/
public static final int INFO_DETAIL_WIDTHS = 12;
/**
* This should return a String representing a "tool-tip" to display for a file in the Filechooser box.
**/
public static final int INFO_TOOL_TIP = 13;
/**
* This should return a String a particular detail for the file.
**/
public static final int INFO_DETAILS = 14;
/**
* This is used to get/set the owner of a file. The object returned with getInfo() is dependent
* on the operating system, but if toString() is called on the returned object it will return
* a value that equates to the user name of the owner (as on Unix) systems. If getInfo() returns
* null this indicates that the OS does not keep owner information on files.
**/
public static final int INFO_OWNER = 15;
/**
* This is used to get/set the three file times for a file - the creation date, the last modification date
* and the last access date, in that order. <p>For a getInfo() operation the resultDestination should be an
* array of 3 Time objects, initially all null. On return the dates that are available on that system will
* be placed in the array in the order shown. If any of the values are null then that date is unknown on the system.
* <p>With a setInfo() operation the sourceParameter should again be an array of 3 Time objects, with
* with any value that you do not wish to set being null.
**/
public static final int INFO_FILE_TIMES = 16;
/**
* This is used to get/set the group of a file. The object returned with getInfo() is dependent
* on the operating system, but if toString() is called on the returned object it will return
* a value that equates to the user name of the group (as on Unix) systems. If getInfo() returns
* null this indicates that the OS does not keep group information on files.
**/
public static final int INFO_GROUP = 17;
/**
* Used for getting the file system type. Use null sourceParameters and resultDestination.
* The value returned by getInfo() will be a ewe.sys.Long which holds one of the FileSys.XXX_SYSTEM
* values - or null if the type could not be determined.
**/
public static final int INFO_SYSTEM_TYPE = 18;

public Object getInfo(int infoCode) {return getInfo(infoCode,null,null,0);}
/**
* A quick way to get the flags from getInfo().
**/
//===================================================================
public int getFlags()
//===================================================================
{
	getInfo(INFO_FLAGS,null,ewe.sys.Long.l1.set(0),0);
	return (int)ewe.sys.Long.l1.value;
}
/**
* This method is used to get extended information about the File or the File system in general.
* It is used with the INFO_ specifiers and options.
* @param infoCode one of the INFO_ codes.
* @param sourceParameters A (possibly null) object giving more details for the information to get. The Object
used depends on the infoCode.
* @param resultDestination A (possibly null) object to be used as the destination for the results. The Object
used depends on the infoCode.
* @param options Options or possibly an index value for the info to get.
* @return An Object with the required information. If it is a Throwable object
* then it indicates an error. If it is a Handle, this indicates that the operation requires time
* to complete and that the actual result will be placed in the Handle when done. If it is null
* this indicates a general failure.
*/
//===================================================================
public abstract Object getInfo(int infoCode,Object sourceParameters,Object resultDestination,int options);
//===================================================================
/**
 * This is used to set extended information about the File or the File system in general.
 * It is used with some of the INFO_ values.
 * @param infoCode One of a few INFO_values.
 * @param sourceParameters The information to set. The object used is dependent on the infoCode.
 * @param options Options or possible an index value for the info to set.
 * @return A Handle that can be used to monitor the progress or success of the operation. Call
 * check() on the handle to see if the Handle.Success or Handle.Failure flags are set. If the Handle.Running
 * flag is set, then the operation has not yet completed.
 */
//===================================================================
public Handle setInfo(int infoCode,Object sourceParameters,int options)
//===================================================================
{
	return new Handle(Handle.Failed,null);
}
/**
* This method is used to set extended information about the File or the File system in general.
* It is used with the INFO_ specifiers and options.
* @deprecated use the other setInfo() instead.
**/
//===================================================================
public boolean setInfo(int infoCode,Object sourceParameters,Object resultDesination,int options)
//===================================================================
{
	return false;
}
/**
* A quick way to get the program directory for the application.
**/
//===================================================================
public static String getProgramDirectory()
//===================================================================
{
	File f = ewe.sys.Vm.newFileObject();
	String s = (String)f.getInfo(INFO_PROGRAM_DIRECTORY,null,null,0);
	if ((Vm.getParameter(Vm.VM_FLAGS) & Vm.VM_FLAG_IS_APPLET) != 0) return s;
	return f.getNew(s).getFullPath();
}
/**
 * List all the root files in the default file system. This will return null
 * if the roots could not be determined (e.g. under Java 1.1)
 * @return an array of File objects representing the available roots.
 */
//===================================================================
public static File [] listRoots()
//===================================================================
{
	File f = ewe.sys.Vm.newFileObject();
	String [] s = (String [])f.getInfo(INFO_ROOT_LIST,null,null,0);
	if (s == null) return null;
	File [] ret = new File[s.length];
	for (int i = 0; i<ret.length; i++)
		ret[i] = f.getNew(s[i]);
	return ret;
}
/**
* Create a directory and all necessary parent directories. Returns false if it could
* not create the directory, true if it can or if it already exists.
**/
//===================================================================
public boolean mkdirs()
//===================================================================
{
	if (isDirectory()) return true;
	Vector v = new Vector();
	File cur = (File)this;
	for (; cur != null; cur = cur.getParentFile())
		if (cur.isDirectory()) break;
		else v.add(cur);
	if (cur == null) return false;
	for (int i = v.size(); i>0;)

		if (!((File)v.get(--i)).createDir()) return false;
	return isDirectory();
}

//===================================================================
public boolean mkdir()
//===================================================================
{
	return createDir();
}
//===================================================================
public String toString()
//===================================================================
{
	String got = getCreationName();
	if (got != null) got = got.replace('\\','/');
	return got;
}
//===================================================================
public static String fixupPath(String path)
//===================================================================
{
	if (path == null) return "";
	path = path.replace('\\','/');
		int where = path.length()-1;
		while(true){
			if (where < 0) break;
			where = path.lastIndexOf("/..",where);
			if (where <= 0) break;
			if (where < path.length()-3)
				if (path.charAt(where+3) != '/'){
					where--;
					continue;
				}
			int before = path.lastIndexOf('/',where-1);
			if (before == -1) before = where;
			String np = path.substring(0,before)+path.substring(where+3);
			path = np;
			where = path.length()-1;
			continue;
		}
		where = path.length()-1;
		while(true){
			if (where < 0) break;
			where = path.lastIndexOf("/.",where);
			if (where <= 0) break;
			if (where < path.length()-2)
				if (path.charAt(where+2) != '/'){
					where--;
					continue;
				}
			String np = path.substring(0,where)+path.substring(where+2);
			//System.out.println(np);
			path = np;
			where = path.length()-1;
			continue;
		}
	if (path.indexOf(':') != -1){
		char [] chars = ewe.sys.Vm.getStringChars(path);
		int i = 0;
		for (i = 0; chars[i] == '/' || chars[i] == '\\'; i++)
			;
		if (i != 0) path = path.substring(i);
	}
	return path;
}

//===================================================================
public static String removeTrailingSlash(String path)
//===================================================================
{
	if (path == null) return path;
	int l = path.length();
	if (l == 0 || l == 1) return path;
	char last = path.charAt(l-1);
	char sl = path.charAt(l-2);
	if (last != '/' && last != '\\') return path;
	if (sl == last || sl == ':') return path;
	return path.substring(0,l-1);
}
/**
* This checks to see if the two files refer to the same object in the file
* system. Currently it does that by comparing their full paths.
**/
//===================================================================
public boolean equals(Object other)
//===================================================================
{
	if (this == other) return true;
	if (!(other instanceof FileBase)) return false;
	FileBase f = (FileBase)other;
	return filePathsAreEqual(fixupPath(getFullPath()),fixupPath(f.getFullPath()));
}
//===================================================================
public int hashCode()
//===================================================================
{
	return fixupPath(getFullPath()).hashCode();
}
//===================================================================
public boolean filePathsAreEqual(String one,String two)
//===================================================================
{
	if ((getFlags() & FLAG_CASE_SENSITIVE) == 0) return one.equalsIgnoreCase(two);
	else return one.equals(two);
}
public String getText() {return getFullPath();}
public void setText(String text) {set(null,text);}

/**
* This differs from getFullPath() in that this will convert all '\' characters
* to '/' characters.
**/
//===================================================================
public String getAbsolutePath()
//===================================================================
{
	return fixupPath(getFullPath());
}
//===================================================================
public File getAbsoluteFile()
//===================================================================
{
	return getNew(null,fixupPath(getFullPath()));
}
//===================================================================
public String getCanonicalPath() throws IOException
//===================================================================
{
	return fixupPath(getFullPath());
}
//===================================================================
public File getCanonicalFile() throws IOException
//===================================================================
{
	return getNew(null,fixupPath(getFullPath()));
}
//===================================================================
public String getName()
//===================================================================
{
	return getFileExt();
}
//===================================================================
public String getPath()
//===================================================================
{
	return getDrivePath();
}

/**
 * This tells the File system to consider any cached directory invalid so
 * so that a list() call will do a true lookup.

 * @return A Handle indicating the progress of the refresh.
 */
//===================================================================
public Handle refresh()
//===================================================================
{
	return new Handle(Handle.Succeeded,null);
}
/**
 * Returns true if the other file system is considered the same as this one.
 * By default this compares the class names of this File and the other File and returns
	true if they are equal.
 * @param other another File object representing a File on a possibly different file system.
 * @return true if the two File objects are considered to be on the same system.
 */
//===================================================================
public boolean isSameFileSystem(File other)
//===================================================================
{
	return other.getClass().getName().equals(getClass().getName());
}

/**
 * Checks if the volume which contains the other File is considered the same as the one that
	contains this File.
 * Generally move() operations will not succeed across volumes.
 * @param other another File object representing a File on a possibly different volume.
 * @return true if the two File objects are considered to be on the same file system and the same volume.
 */
//===================================================================
public boolean isSameVolume(File other)
//===================================================================
{
	if (!isSameFileSystem(other)) return false;
	return true;
}

//===================================================================
public static File getTrueParent(File parent,String child,File aFile)
//===================================================================
{
	if (child.indexOf('/') != -1 || child.indexOf('\\') != -1){
		//ewe.sys.Vm.debug("Child: "+child);
		return aFile.getNew(parent,child).getParentFile();
	}
	return parent;
}
//===================================================================
public static String getTrueChild(File parent,String child,File aFile)
//===================================================================
{
	if (child.indexOf('/') != -1 || child.indexOf('\\') != -1)
		return aFile.getNew(parent,child).getName();
	return child;
}
/**
 * A utility method to convert from "r" or "rw" to READ_ONLY or READ_WRITE mode. It will throw
 * an IllegalArgumentException if the mode is not one of these two.
 * @param mode must be "r" or "rw"
 * @return READ_ONLY or READ_WRITE.
 * @exception IllegalArgumentException if mode is not "r" or "rw"
 */
//===================================================================
public static int convertMode(String mode)
//===================================================================
{
	if ("rw".equals(mode)) return READ_WRITE;
	else if (!"r".equals(mode)) throw new IllegalArgumentException("mode must be \"r\" or \"rw\"");
	return READ_ONLY;
}

//===================================================================
public Handle toStream(boolean isRandom,String type) throws IllegalArgumentException
//===================================================================
{
	try{
		if (isRandom)
			return new Handle(Handle.Succeeded,toRandomAccessStream(type));
		else if (type.equals("r"))
			return new Handle(Handle.Succeeded,toReadableStream());
		else if (type.equals("a"))
			return new Handle(Handle.Succeeded,toWritableStream(true));
		else if (type.equals("w"))
			return new Handle(Handle.Succeeded,toWritableStream(false));
		else
			throw new IllegalArgumentException("type must be \"r\" or \"a\" or \"w\"");
	}catch(IllegalArgumentException e){
		throw e;
	}catch(IOException io){
		return new Handle(io);
	}
}
/**
 * Create and return a Stream to use for reading from the File.
 * @return An open Steam that can be used for reading from the File.
 * @exception IOException if an open stream could not be created.
 */
//===================================================================
public Stream toReadableStream() throws IOException
//===================================================================
{
	RandomAccessStream ras = toRandomAccessStream("r");
	ras.seek(0);
	return ras;
}
/**
 * Create and return a Stream to use for writing to the File.
 * If the file does not exist it will be created.
 * @param append set this true if you want to append to the existing file.
 * @return An open Steam that can be used for reading from the File.
 * @exception IOException if an open stream could not be created, or if the file exists but could not
 * be written to or erased (if not appending), or if you requested append but append mode is not supported
 * for this File.
*/
//===================================================================
public Stream toWritableStream(boolean append) throws IOException
//===================================================================
{
	if (exists() && !append)
		if (!delete()) throw new IOException("Could not open/delete file for writing: "+this);
	RandomAccessStream ras = toRandomAccessStream("rw");
	ras.seek(ras.getLength());
	return ras;
}
/**
 * Get a stream to read from the File.
 * @return A stream to read from, or null if a stream could not be opened.
 * @deprecated use toReadableStream() instead.
 */
//===================================================================
public final Stream getInputStream()
//===================================================================
{
	try{
		return toReadableStream();
	}catch(Exception e){
		return null;
	}
}
/**
 * Get a stream to write to the File.
 * @return A stream to write to, or null if a stream could not be opened.
 * @deprecated use toWritableStream() instead.
 */
//===================================================================
public final Stream getOutputStream()
//===================================================================
{
	try{
		return toWritableStream(false);
	}catch(Exception e){
		return null;
	}
}
/**
 * Get a RandomAccessStream for the file.
 * @deprecated use toRandomAccessStream() instead.
 */
//===================================================================
public final RandomAccessStream getRandomAccessStream(int mode)
//===================================================================
{
	try{
		return toRandomAccessStream(mode == READ_ONLY ? "r" : "rw");
	}catch(Exception e){
		return null;
	}
}
/**
 * Create and return a RandomAccessStream for reading/writing to the data associated with this File object.
 * @param mode must be "r" or "rw".
 * @return An open RandomAccessStream
 * @exception IOException if an open stream could not be created.
 * @exception IllegalArgument exception if mode is not "r" or "rw"
 */
//===================================================================
public RandomAccessStream toRandomAccessStream(String mode) throws IOException
//===================================================================
{
	return new RandomAccessFile((File)this,mode);
}

/**
 * Get a new File object for the default file system used by the VM.
 */
//===================================================================
public static File getNewFile()
//===================================================================
{
	return ewe.sys.Vm.newFileObject();
}
/**
 * Get a new File object for the default file system used by the VM.
 */
//===================================================================
public static File getNewFile(String name)
//===================================================================
{
	File f = ewe.sys.Vm.newFileObject();
	f.set(null,name);
	return f;
}
/**
 * Get a new File object for the default file system used by the VM.
 */
//===================================================================
public static File getNewFile(File directory,String name)
//===================================================================
{
	File f = ewe.sys.Vm.newFileObject();
	f.set(directory,name);
	return f;
}

//===================================================================
/**
* Get ther permissions/flags for this File - see ewe.io.FilePermissions for a list of
* the available flag permissions.
* @param interestedFlags The flags whose values you are interested in.
* @return The permission/flag types ORed together.
* @exception IOException if the file does not exist or some other error occured.
* @exception IllegalArgumentException if one of the flags you are interested in is not supported
* on this file system.
*/
//===================================================================
public int getPermissionsAndFlags(int interestedFlags) throws IOException, IllegalArgumentException
//===================================================================
{
	return (getSetPermissionsAndFlags(true,interestedFlags,0) & interestedFlags);
}
/**
* Change ther permissions/flags for this File - see ewe.io.FilePermissions for a list of
* the available flag permissions.
* @param valuesToSet The permissions/flags to set.
* @param valuesToClear The permissions/flags to clear.
* @return true if all the requested changes were made, false if not.
* @exception IOException if the file does not exist or some other IO error occured.
* @exception IllegalArgumentException if one of the flag values is not supported.
*/
//===================================================================
public boolean changePermissionsAndFlags(int valuesToSet, int valuesToClear) throws IOException, IllegalArgumentException
//===================================================================
{
	int val = getSetPermissionsAndFlags(false,valuesToSet,valuesToClear);
	if ((val & valuesToSet) != valuesToSet) return false;
	if ((val & valuesToClear) != 0) return false;
	return true;
}
/**
This is used to implement getPermissionsAndFlags() and changePermissionsAndFlags(). It should
work like this:
<p>
If isGet is true, then the valuesToSetOrGet parameter will hold the flags that the user is
interested in. If any of these flags are invalid on this system an IllegalArgumentException should be thrown.
Otherwise the current state of the flags should be returned - it is OK to return extra flags
even if they are not specified as flags the user is interested in.
<p>
If isGet is false, then the valuesToSetOrGet parameter will hold the flags to set and the valuesToClear parameter
will hold the flags to clear. If any of these are not valid then an IllegalArgumentException
should be thrown. Otherwise the flags should be changed and then the state of the flags AFTER
the change has been made should be returned.
**/
//-------------------------------------------------------------------
protected int getSetPermissionsAndFlags(boolean isGet, int valuesToSetOrGet, int valuesToClear) throws IOException, IllegalArgumentException
//-------------------------------------------------------------------
{
	return 0;
}
//===================================================================
public abstract int getLength();
//===================================================================

private static String spacing = "   ";
//-------------------------------------------------------------------
private static String addTime(String prompt,Time t)
//-------------------------------------------------------------------
{
	if (t == null) return "";
	t.format = ewe.sys.Vm.getLocale().getString(Locale.LONG_DATE_FORMAT,0,0);
	String ret = prompt+"\n"+spacing+t.toString()+"\n"+spacing;
	t.format = ewe.sys.Vm.getLocale().getString(Locale.TIME_FORMAT,0,0);
	ret += t.toString()+"\n";
	return ret;
}
private static final String [] permissions = new String[]
{"Read","Write","Execute","Read","Write","Execute","Read","Write","Execute","Hidden","Archive","System","Read-only","ROM","ROM-Module"};
private static final int [] permissionValues = new int []
{
OWNER_READ,OWNER_WRITE,OWNER_EXECUTE,GROUP_READ,GROUP_WRITE,GROUP_EXECUTE,OTHER_READ,OTHER_WRITE,OTHER_EXECUTE,
FLAG_HIDDEN,FLAG_ARCHIVE,FLAG_SYSTEM,FLAG_READONLY,FLAG_ROM,FLAG_ROMMODULE
};

//-------------------------------------------------------------------
private static String getFlag(int flag,int presentFlag)
//-------------------------------------------------------------------
{

	if ((flag & presentFlag) == 0) return "";
	for (int i = 0; i<permissionValues.length; i++)
		if (permissionValues[i] == flag) return permissions[i]+" ";
	return "";
}
//===================================================================
public String getPropertiesString() throws IOException
//===================================================================
{
	String ret = "";
	ret += "Name:\n"+spacing+getName()+"\n";
	String s = getParent();
	if (s != null) ret += "Location:\n"+spacing+s+"\n";

	if (!isDirectory()) ret += "Size:\n"+spacing+Vm.getLocale().format(Locale.FORMAT_PARSE_NUMBER,ewe.sys.Long.l1.set(getLength()),",")+" bytes\n";
	Time [] times = (Time [])getInfo(INFO_FILE_TIMES,null,new Time[3],0);
	if (times == null) {
		times = new Time[3];
		times[1] = getModified(new Time());
	}
	if (times != null){
		ret += addTime("Created:",times[0]);
		ret += addTime("Modified:",times[1]);
		ret += addTime("Accessed:",times[2]);
	}
	Object owner = getInfo(INFO_OWNER,null,null,0);
	if (owner != null) ret += "Owner:\n"+spacing+owner+"\n";
	Object group = getInfo(INFO_GROUP,null,null,0);
	if (group != null) ret += "Group:\n"+spacing+group+"\n";

	int value;
	try{
		value = getPermissionsAndFlags(ALL_UNIX_PERMISSIONS);
		if ((value & (OWNER_READ|OWNER_WRITE|OWNER_EXECUTE)) != 0)
			ret += "Owner Permissions:\n"+spacing+getFlag(OWNER_READ,value)+getFlag(OWNER_WRITE,value)+getFlag(OWNER_EXECUTE,value)+"\n";
		if ((value & (GROUP_READ|GROUP_WRITE|GROUP_EXECUTE)) != 0)
			ret += "Group Permissions:\n"+spacing+getFlag(GROUP_READ,value)+getFlag(GROUP_WRITE,value)+getFlag(GROUP_EXECUTE,value)+"\n";
		if ((value & (OTHER_READ|OTHER_WRITE|OTHER_EXECUTE)) != 0)
			ret += "Other Permissions:\n"+spacing+getFlag(OTHER_READ,value)+getFlag(OTHER_WRITE,value)+getFlag(OTHER_EXECUTE,value)+"\n";
	}catch(IllegalArgumentException e){
		try{
			value = getPermissionsAndFlags(ALL_DOS_FLAGS);
			if (value != 0)
				ret += "Attributes:\n"+spacing+getFlag(FLAG_READONLY,value)+getFlag(FLAG_ARCHIVE,value)+getFlag(FLAG_HIDDEN,value)+getFlag(FLAG_SYSTEM,value)+getFlag(FLAG_ROM,value)+getFlag(FLAG_ROMMODULE,value)+"\n";
		}catch(IllegalArgumentException e2){
			try{
				value = getPermissionsAndFlags(FLAG_READONLY);
				if (value != 0)
					ret += "Attributes:\n"+spacing+getFlag(FLAG_READONLY,value)+"\n";
			}catch(IllegalArgumentException e3){}
		}
	}

	return ret;
}

public static final char separatorChar = (char)ewe.sys.Vm.getParameter(ewe.sys.Vm.VM_FILE_SEPARATOR);
public static final char pathSeparatorChar = (char)ewe.sys.Vm.getParameter(ewe.sys.Vm.VM_PATH_SEPARATOR);
public static final String separator = new String(new char[]{separatorChar});
public static final String pathSeparator = new String(new char[]{pathSeparatorChar});

/*
//=================================================================
public static void main(String[] args)
//=================================================================
{
	ewe.sys.Vm.startEwe(args);
	File f = new File("F:\\Projects");
	FileChooserParameters fcp = new FileChooserParameters();
	if (f.executeFileChooser(fcp)){
		ewe.sys.Vm.debug(fcp.toString());
	}

	ewe.sys.Vm.exit(0);
}
*/
//##################################################################
}
//##################################################################

