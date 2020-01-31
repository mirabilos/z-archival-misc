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
package ewe.applet;
import java.util.*;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.File;
//#####################################################################
public class FileClassInfoLoader extends PathClassInfoLoader{
//#####################################################################
/**
*
*/
protected boolean
//============================================================
  classExists(String fullPathName)
//============================================================
{
	File f = new File(fullPathName);
	return f.exists();
}
/**
*
*/
protected boolean
//============================================================
  getBytesFromPath(ClassInfo ci,String fullPathName)
//============================================================
{
	//System.out.println("getBytesFromPath(ci,"+fullPathName+");");
	try {
		File f = new File(fullPathName);
		if (!f.exists()) return false;
		int len = (int)f.length();
		DataInputStream is = new DataInputStream(new FileInputStream(fullPathName));
		ci.bytes = new byte[len];
		is.readFully(ci.bytes);
		is.close();
	}catch(Exception e){
		if (ci.bytes == null) ci.error = e;
	}
	return (ci.bytes != null);
}

/**
*
*/
public static void main(String args[])
{
	mClassLoader cl = new mClassLoader();
	FileClassInfoLoader fci = new FileClassInfoLoader();
	fci.paths.addElement("c:/html/otherclasses");
	cl.classInfoLoaders.addElement(fci);
	Object obj = cl.getObject(args[0]);
	System.out.println("Got->"+obj);
}

//############################################################
}
//############################################################
