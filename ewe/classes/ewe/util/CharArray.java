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
package ewe.util;
/**
* A CharArray is a utility class that represents a number of text characters.
* It is generally used as a buffer for text storage.<p>
* Associated with the CharArray is a char [] value called "data" which holds
* the data characters (starting from index 0) and the "length" variable specifies how
* many of those characters are considered valid (which may be less than the actual
* length of the data variable).
**/
//##################################################################
public class CharArray implements Encodable, Copyable{
//##################################################################
/**
* These are the data characters.
**/
public char [] data = new char[0];//Do not move this variable.
/**
* This denotes the number of valid characters in the data array.
**/
public int length = 0;//Do not move this variable.
/**
Return a copy of this CharArray which has its own copy of the data.
**/
//===================================================================
public Object getCopy()
//===================================================================
{
	CharArray ba = new CharArray();
	ba.length = length;
	ba.data = new char[length];
	if (length != 0) ewe.sys.Vm.copyArray(data,0,ba.data,0,length);
	return ba;
}
/**
* This adds space to the CharArray at the specified index. After this is
* done you can safely write into the data starting at index "where" for
* a length of numcharacters.
**/
//===================================================================
public boolean makeSpace(int where,int numcharacters)
//===================================================================
{
	if (numcharacters <= 0) return true;
	if (length+numcharacters > data.length) {
		int expansion = data.length;
		if (expansion > 100000) expansion = 100000;
		if ((length+numcharacters) > (expansion+data.length))
			expansion = 1+length+numcharacters-data.length;
		int newLen = data.length+expansion;
		//if (true) newLen = length+numcharacters;
		char [] nd = new char[newLen];
		ewe.sys.Vm.copyArray(data,0,nd,0,length);
		data = nd;
	}
	if (where < length)
		ewe.sys.Vm.copyArray(data,where,data,where+numcharacters,length-where);
	length += numcharacters;
	return true;
}
/**
* This writes into the data at the specific location. Make sure you have called
* makeSpace() before if necessary.
**/
//===================================================================
public void write(int where,char [] src,int start,int length)
//===================================================================
{
	ewe.sys.Vm.copyArray(src,start,data,where,length);
}
/**
* Insert characters into the CharArray.
 * @param where The index in this CharArray where the data will go.
 * @param src The source data characters.
 * @param start The start index in the source data.
 * @param length The number of characters to copy.
 */
//===================================================================
public void insert(int where, char[] src,int start,int length)
//===================================================================
{
	if (makeSpace(where,length)) write(where,src,start,length);
}
/**
* Add characters to the end of the CharArray.
 * @param src The source data characters.
 * @param start The start index in the source data.
 * @param length The number of characters to copy.
 */
//===================================================================
public void append(char [] src,int start,int length)
//===================================================================
{
	insert(this.length,src,start,length);
}
//===================================================================
public void append(String src)
//===================================================================
{
	append(ewe.sys.Vm.getStringChars(src),0,src.length());
}
/**
* This will ensure that the size of the "data" character array is at least
* newCapacity. If the size is already greater than or equal to newCapacity, then
* this will have no effect.
* @param newCapacity The minimum size of the data array.
*/
//===================================================================
public void ensureCapacity(int newCapacity)
//===================================================================
{
	if (data.length >= newCapacity) return;
	if (newCapacity < (data.length*2+2)) newCapacity = data.length*2+2;
	char [] nd = new char[newCapacity];
	if (length > 0) ewe.sys.Vm.copyArray(data,0,nd,0,length);
	data = nd;
}
//===================================================================
public String toString()
//===================================================================
{
	return new String(data,0,length);
}
/**
Set the data in the CharArray to be a copy of the data in the string,
re-using the internal char array if possible.
**/
//===================================================================
public void copyFrom(String data)
//===================================================================
{
	length = 0;
	if (data != null) append(data);
}
/**
Set the data in the CharArray to be a copy of the data provided
re-using the internal char array if possible.
**/
//===================================================================
public void copyFrom(char[] data, int offset, int length)
//===================================================================
{
	this.length = 0;
	if (data != null) append(data,offset,length);
}
/**
Set the data in the CharArray to be a copy of the data in the SubString,
re-using the internal char array if possible.
**/
//===================================================================
public void copyFrom(SubString data)
//===================================================================
{
	this.length = 0;
	if (data != null) append(data.data,data.start,data.length);
}
//##################################################################
}
//##################################################################

