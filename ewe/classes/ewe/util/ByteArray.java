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
* A ByteArray is a utility class that represents a number of data bytes.
* It is generally used as a buffer for data storage.<p>
* Associated with the ByteArray is a byte [] value called "data" which holds
* the data bytes (starting from index 0) and the "length" variable specifies how
* many of those bytes are considered valid (which may be less than the actual
* length of the data variable).
**/
//##################################################################
public class ByteArray implements Copyable, Encodable{
//##################################################################
/**
* The data bytes.
**/
public byte [] data = new byte[0]; //Do not move this variable! It must be first.
/**
* The number of valid bytes in the data.
**/
public int length = 0;//Do not move this variable! It must be second.
/**
* Get a copy of this ByteArray with its own copy of this data.
**/
//===================================================================
public Object getCopy()
//===================================================================
{
	ByteArray ba = new ByteArray();
	ba.length = length;
	ba.data = new byte[length];
	if (length != 0) ewe.sys.Vm.copyArray(data,0,ba.data,0,length);
	return ba;
}
/**
* This adds space to the ByteArray at the specified index, increasing the
* length value by numBytes. After this is
* done you can safely write into the data starting at index "where" for
* a length of numBytes.
**/
//===================================================================
public boolean makeSpace(int where,int numBytes)
//===================================================================
{
	if (numBytes <= 0) return true;
	if (length+numBytes > data.length) {
		int expansion = data.length;
		if (expansion > 100000) expansion = 100000;
		if ((length+numBytes) > (expansion+data.length))
			expansion = 1+length+numBytes-data.length;
		int newLen = data.length+expansion;
		//if (true) newLen = length+numBytes;
		byte [] nd = new byte[newLen];
		ewe.sys.Vm.copyArray(data,0,nd,0,length);
		data = nd;
	}
	if (where < length)
		ewe.sys.Vm.copyArray(data,where,data,where+numBytes,length-where);
	length += numBytes;
	return true;
}
/**
* This writes into the data at the specific location. Make sure you have called
* makeSpace() before if necessary.
**/
//===================================================================
public void write(int where,byte [] src,int start,int length)
//===================================================================
{
	ewe.sys.Vm.copyArray(src,start,data,where,length);
}
/**
* Insert bytes into the ByteArray.
 * @param where The index in this ByteArray where the data will go.
 * @param src The source data bytes.
 * @param start The start index in the source data.
 * @param length The number of bytes to copy.
 */
//===================================================================
public void insert(int where, byte[] src,int start,int length)
//===================================================================
{
	if (makeSpace(where,length)) write(where,src,start,length);
}
/**
* Add bytes to the end of the ByteArray.
 * @param src The source data bytes.
 * @param start The start index in the source data.
 * @param length The number of bytes to copy.
 */
//===================================================================
public void append(byte [] src,int start,int length)
//===================================================================
{
	insert(this.length,src,start,length);
}
static byte [] single;
/**
 * Append a single byte of data.
 * @param data The byte to append.
 */
//===================================================================
public void append(byte data)
//===================================================================
{
	if (single == null) single = new byte[1];
	single[0] = data;
	append(single,0,1);
}
/**
* Insert an integer value as up to four bytes in big-endian format.
* @param where the index at which to insert the integer.
* @param value the value to insert.
* @param numBytes the number of bytes (starting from the least significant) to insert.
*/
//===================================================================
public void insertInt(int where,int value,int numBytes)
//===================================================================
{
	makeSpace(where,numBytes);
	Utils.writeInt(value,data,where,numBytes);
}
/**
* Append an integer value as up to four bytes in big-endian format.
* @param value the value to append.
* @param numBytes the number of bytes (starting from the least significant) to insert.
*/
//===================================================================
public void appendInt(int value,int numBytes)
//===================================================================
{
	insertInt(length,value,numBytes);
}
/**
* Append a four-byte integer value.
* @param value the value to append.
*/
//===================================================================
public void appendInt(int value)
//===================================================================
{
	insertInt(length,value,4);
}
/**
* Insert a long value as eight bytes in big-endian format.
* @param where the index at which to insert the integer.
* @param value the value to insert.
*/
//===================================================================
public void insertLong(int where,long value)
//===================================================================
{
	makeSpace(where,8);
	Utils.writeLong(value,data,where);
}
/**
* Append a long value as eight bytes in big-endian format.
* @param value the value to append.
*/
//===================================================================
public void appendLong(long value)
//===================================================================
{
	insertLong(length,value);
}
/**
* This removes bytes from within the data.
**/
//===================================================================
public boolean delete(int where,int numBytes)
//===================================================================
{
	if (numBytes <= 0) return true;
	if (where+numBytes > length) numBytes = length-where;
	if (numBytes > 0){
		ewe.sys.Vm.copyArray(data,where+numBytes,data,where,length-(where+numBytes));
		length -= numBytes;
	}
	return true;
}
/**
* This sets the length of the array back to zero without releasing the
* data bytes. This means it can be used again without creating any
* new objects.
**/
//===================================================================
public void clear() {length = 0;}
//===================================================================
/**
* This sets the length of the valid data within the array, but does NOT extend OR truncate
* the array. If the supplied len is greater than the length of the byte array, the value
* will be reduced to the length of the byte array.
**/
//===================================================================
public void setLength(int len) {if (len > data.length) len = data.length; if (len < 0) len = 0; length = len;}
//===================================================================
/**
* Return a copy of the data in the ByteArray as a byte array of the exact length of the data.
**/
//===================================================================
public byte [] toBytes()
//===================================================================
{
	byte [] got = new byte[length];
	ewe.sys.Vm.copyArray(data,0,got,0,length);
	return got;
}
//===================================================================
public ByteArray() {}
//===================================================================
/**
* This will actually use the "data" byte array as the data for the byte array. It will not copy the data.
**/
//===================================================================
public ByteArray(byte [] data)
//===================================================================
{
	this.data = data;
	this.length = data.length;
}
/**
Copy the bytes from the supplied array, re-using the interal byte array if possible.
**/
//===================================================================
public void copyFrom(byte[] data, int offset, int length)
//===================================================================
{
	this.length = 0;
	if (data != null) append(data,offset,length);
}
/**
Copy the bytes from the supplied array, re-using the interal byte array if possible.
**/
//===================================================================
public void copyFrom(byte[] data)
//===================================================================
{
	this.length = 0;
	if (data != null) append(data,0,data.length);
}
//##################################################################
}
//##################################################################


