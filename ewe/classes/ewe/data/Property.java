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
package ewe.data;
import ewe.util.*;
import ewe.sys.Convert;
//#####################################################################
public class Property extends DataObject implements Encodable{
//#####################################################################
public String name;
public Object value;
public String _fields = "name,value";

//===================================================================
public Property(){}
//===================================================================

//==================================================================
public Property(String theName,Object theValue)
//==================================================================
{
	name = theName;
	value = theValue;
	if (name == null) name = "";
}
/*
//==================================================================
public Object getCopy()
//==================================================================
{
	Object v = value;
	if (v instanceof Copyable && v != null) v = ((Copyable)v).getCopy();
	return new Property(name,v);
}
*/
//==================================================================
public String toString() {return name+"="+value;}
//==================================================================
public int compareTo(Object other)
//==================================================================
{
	String os = null;
	if (other == null) return 1;
	if (other instanceof String) os = (String)other;
	else if (other instanceof Property) os = ((Property)other).name;
	if (name.equalsIgnoreCase(os)) return 0;
	return 1;
}
/*
public String [] getLevels()
{
	return Fields.toStringArray(Fields.split(name,"."));
}
public String getLevel(int level)
{
	String [] s = getLevels();
	if (level >= s.length) return "";
	return s[level];
}
*/
//==================================================================
public static Object getValue(HasProperties obj,String name,Object def)
//==================================================================
{
	if (obj == null) return def;
	return obj.getProperties().getValue(name,def);
}
//===================================================================
public void set(ewe.reflect.Wrapper v)
//===================================================================
{
	if (v == null) value = null;
	else switch(v.getType()){
		case 'Z' : value = Convert.toString(v.getBoolean()); break;
		case 'B' : value = Convert.toString(v.getByte()); break;
		case 'C' : value = Convert.toString(v.getChar()); break;
		case 'S' : value = Convert.toString(v.getShort()); break;
		case 'I' : value = Convert.toString(v.getInt()); break;
		case 'J' : value = Convert.toString(v.getLong()); break;
		case 'F' : value = Convert.toString(v.getFloat()); break;
		case 'D' : value = Convert.toString(v.getDouble()); break;
		case '[' : case 'L' : value = v.getObject(); break;
	}
}
//===================================================================
public void get(ewe.reflect.Wrapper v)
//===================================================================
{
	if (v == null) return;
	if (v.getType() == 'L') {
		v.setObject(value);
		return;
	}
	String val = mString.toString(value);
	switch(v.getType()){
		case 'Z' : v.setBoolean(Convert.toBoolean(val)); break;
		case 'B' : v.setByte((byte)Convert.toInt(val)); break;
		case 'C' : v.setChar(Convert.toChar(val)); break;
		case 'S' : v.setShort((short)Convert.toInt(val)); break;
		case 'I' : v.setInt(Convert.toInt(val)); break;
		case 'J' : v.setLong(Convert.toLong(val)); break;
		case 'F' : v.setFloat(Convert.toFloat(val)); break;
		case 'D' : v.setDouble(Convert.toDouble(val)); break;
	}
}

//#####################################################################
}
//#####################################################################

