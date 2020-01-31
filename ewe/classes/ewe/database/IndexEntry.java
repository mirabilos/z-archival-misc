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
import ewe.util.Encodable;
import ewe.reflect.Reflect;
import ewe.util.Comparer;

//##################################################################
public class IndexEntry implements Encodable{
//##################################################################

public String comparerClassName;
public int sortID;
public String name;

private Reflect comparerClass;

//===================================================================
public boolean hasCustomComparer()
//===================================================================
{
	return (comparerClassName != null && comparerClassName.length() != 0);
}
//===================================================================
public Class getCustomComparerClass()
//===================================================================
{
	if (comparerClassName == null || comparerClassName.length() == 0)
		return null;
	if (comparerClass == null) comparerClass = Reflect.loadForName(comparerClassName);
	return comparerClass == null ? null : comparerClass.getReflectedClass();
}
//===================================================================
public DatabaseEntryComparer getCustomComparerInstance(Database forDatabase)
//===================================================================
{
	Class c = getCustomComparerClass();
	if (c == null) return null;
	try{
		DatabaseEntryComparer dc = (DatabaseEntryComparer)c.newInstance();
		dc.setDatabase(forDatabase);
		return dc;
	}catch(Exception e){
		return null;
	}
}
//-------------------------------------------------------------------
IndexEntry getCopy()
//-------------------------------------------------------------------
{
	IndexEntry ie = new IndexEntry();
	ie.comparerClassName = comparerClassName;
	ie.sortID = sortID;
	ie.name = name;
	ie.comparerClass = comparerClass;
	return ie;
}
//===================================================================
public String toString()
//===================================================================
{
	return name;
}
//##################################################################
}
//##################################################################

