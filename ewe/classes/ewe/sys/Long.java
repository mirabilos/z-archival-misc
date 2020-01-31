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
package ewe.sys;
/**
The ewe.sys.Long class was originally used to wrap java integral values instead of
the standard java wrapper objects (java.lang.Integer, java.lang.Long, java.lang.Short, etc.)
<p>
*/
//##################################################################
public class Long extends ewe.data.DataObject implements Value{
//##################################################################
	/**
	 * This is the same value as java.lang.Long.TYPE - it is the Class
	 * that represents the primitive type long.
	 */
public static final Class TYPE = java.lang.Long.TYPE;
public long value; //This must be first.

public static final Long l1 = new Long();
public static final Long l2 = new Long();
public static final Long l3 = new Long();

public Long set(long value) {this.value = value; return this;}
public Long set(Long value) {return set(value == null ? 0 : value.value);}

/**
* This is an option for toString().
**/
public static final int ZERO_FILL = 0x1;
/**
* This is an option for toString().
**/
public static final int TRUNCATE = 0x2;
/**
* This is an option for toString().
**/
public static final int HEX = 0x4;

//===================================================================
public void copyFrom(Object other)
//===================================================================
{
	if (other instanceof Long)
		set(((Long)other).value);
}
//===================================================================
public String toString() {return ewe.sys.Convert.toString(value);}//toString(0,0);}
//===================================================================
public int compareTo(Object other)
//===================================================================
{
	if (!(other instanceof Long)) return 1;
	long v2 = ((Long)other).value;
	if (value == v2) return 0;
	else if (value > v2) return 1;
	return -1;
}
/**
 * Convert to a String.
 * @param length The length of the output String.
 * @param options Can be a combination of HEX (output in hexadecimal), ZERO_FILL (left pad to
	length with zeros), TRUNCATE (truncate to be no longer than the specified length).
 * @return The value converted to a String.
 */
//===================================================================
public native String toString(int length,int options);
//===================================================================
public native void fromString(String str);
//===================================================================
//===================================================================
public boolean equals(Object other)
//===================================================================
{
	if (!(other instanceof ewe.sys.Long)) return super.equals(other);
	return value == ((ewe.sys.Long)other).value;
}
//===================================================================
public int hashCode()
//===================================================================
{
	return (int)value;
}
/**
 * Place the value in the standard java Wrapper (e.g. java.lang.Integer, java.lang.Long, etc)
 * into this ewe.sys.Long.
 * @param javaWrapper one of the java.lang primitive wrapper values representing an integer type.
 * @return this ewe.sys.Long after the value has been placed in it.
* @exception IllegalArgumentException if the javaWrapper parameter is not an integral or boolean value.
*/
//===================================================================
public ewe.sys.Long fromJavaWrapper(Object javaWrapper) throws IllegalArgumentException
//===================================================================
{
	if (javaWrapper == null) return set(0L);
	if (javaWrapper instanceof	java.lang.Long)
		return set(((java.lang.Long)javaWrapper).longValue());
	if (javaWrapper instanceof java.lang.Integer)
		return set(((java.lang.Integer)javaWrapper).longValue());
	if (javaWrapper instanceof java.lang.Short)
		return set(((java.lang.Short)javaWrapper).longValue());
	if (javaWrapper instanceof java.lang.Byte)
		return set(((java.lang.Byte)javaWrapper).longValue());
	if (javaWrapper instanceof java.lang.Character)
		return set(((java.lang.Character)javaWrapper).charValue());
	if (javaWrapper instanceof java.lang.Boolean)
		return set(((java.lang.Boolean)javaWrapper).booleanValue() ? 1 : 0);
	throw new IllegalArgumentException();
}
//##################################################################
}
//##################################################################

