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

/**
* This class is used as base for the Java and abstract versions of File.
**/
//##################################################################
public abstract class FileSys{
//##################################################################
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
	File f = (File)this;
	if (suffix == null) suffix = "tmp";
	if (prefix == null) prefix = "temp";
	if (dir == null){
		String d = (String)f.getNew("").getInfo(f.INFO_TEMPORARY_DIRECTORY,null,null,0);
		if (d == null) return null;
		dir = f.getNew(d);
		if (!dir.isDirectory()) return null;
	}
	while(true){
		File tryFile = f.getNew(dir,prefix+(++curTemp)+"."+suffix);
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
	File f = (File)this;
	File temp = createTempFile(null,null,dir);
	if (temp == null) return null;
	if (dir == null) dir = temp.getParentFile();
	File want = f.getNew(dir,fileName);
	want.delete();
	if (!temp.rename(fileName)) return null;
	File other = f.getNew(dir,fileName);
	other.deleteOnExit();
	return want;
}
/*
This creates a temporary stream for reading/writing. However, once the
public RandomAccessStream createTempStream(String prefix,String suffix,File dir)
{
}

public RandomAccessStream createTempStream(String prefix)
{
	return createTempStream(prefix,null,dir);
}
*/

/**
* The file system is a DOS/Windows type system.
**/
public static final int DOS_SYSTEM = 1;
/**
* The file system is a Unix/Linux type system.
**/
public static final int UNIX_SYSTEM = 2;
/**
* The file system is a PalmOS type system - whatever that may be.
**/
public static final int PALM_SYSTEM = 3;
/**
* The file system is temporary memory based system.
**/
public static final int MEMORY_SYSTEM = 4;

//##################################################################
}
//##################################################################

