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
package ewe.io;
import ewe.ui.*;
import ewe.fx.*;
import ewe.util.*;
import ewe.sys.Time;
import ewe.sys.Locale;

//##################################################################
class FileComparer extends ewe.util.FileComparer implements java.io.FilenameFilter{
//##################################################################
//===================================================================
public FileComparer(File parent,Locale locale,int options,String mask)
//===================================================================
{
	super(parent,locale,options,mask);
}
//============================================================
public boolean accept(java.io.File dir,String name)
//============================================================
{
	java.io.File f = new java.io.File(dir,name);
	if (f.isDirectory()){
		if ((options & File.LIST_FILES_ONLY) != 0) return false;
		if ((options & File.LIST_ALWAYS_INCLUDE_DIRECTORIES) != 0) return true;
	}else{
		if ((options & File.LIST_DIRECTORIES_ONLY) != 0) return false;
	}
	return matches(name);
}
//##################################################################
}
//##################################################################
