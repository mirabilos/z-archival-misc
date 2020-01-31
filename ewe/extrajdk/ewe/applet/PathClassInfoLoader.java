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

//#####################################################################
public abstract class PathClassInfoLoader implements ClassInfoLoader{
//#####################################################################
/**
* This is a list of all the paths where the classes may be.
**/
public Vector paths = new Vector();
public char pathSeparator = '/';
/**
* Checks to see if
*/
protected abstract boolean
//============================================================
  classExists(String fullPathName);
//============================================================
/**
*
*/
protected abstract boolean
//============================================================
  getBytesFromPath(ClassInfo ci,String fullPathName);
//============================================================
/**
*
*/
protected final boolean
//------------------------------------------------------------
  tryPath(ClassInfo ci,String path)
//------------------------------------------------------------
{
	path = path.replace('\\','/');
	if (!path.endsWith("/")) path = path+'/';
	String fullName = path+ci.fullName.replace('.',pathSeparator)+".class";
	//System.out.println("Trying: "+fullName);
	if (!classExists(fullName)) return false;
	return getBytesFromPath(ci,fullName);
}
//==================================================================
public final boolean getClassBytes(ClassInfo ci)
//==================================================================
{
	for(Enumeration e = paths.elements();e.hasMoreElements();)
		if (tryPath(ci,(String)e.nextElement())) return true;
	return false;
}
//==================================================================
/**
* This trys to get the bytes for the class. Only this, and nothing more.
*/
//==================================================================
public ClassInfo getClassInfo(String fullClassName)
//==================================================================
{
	ClassInfo ci = new ClassInfo(fullClassName,null);
	ci.fullName = fullClassName;
	if (getClassBytes(ci)) return ci;
	return null;
}
//==================================================================

//#####################################################################
}
//#####################################################################
