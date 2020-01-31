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
import ewe.util.Utils;
import ewe.util.CharArray;
import ewe.util.SubString;
//##################################################################
public class EncodedUTF8String{
//##################################################################
public byte [] data = null;
public int start;
/**
* Store a String as UTF, prepending the lengh of the String as a short integer and
* appending a zero byte, or find out the number of bytes required.
** @param chars the characters to write.
* @param offset the first character to write.
* @param length the number of characters to write.
* @param dest the destination byte array, or null to just find out the number of bytes needed.
* @param offset the location in the destinatin to write to.
* @return the number of bytes required. A null char array will require 2 bytes, an empty
* string will require 3 bytes.
*/
//===================================================================
public static int store(char[] chars, int offset, int length, byte[] dest, int destOffset)
//===================================================================
{
	int len = chars == null ? 2 : Utils.sizeofJavaUtf8String(chars, offset, length)+3;
	if (dest != null){
		Utils.writeInt(len-2,dest,destOffset,2);
		if (chars != null) {
			Utils.encodeJavaUtf8String(chars, offset, length, dest, destOffset+2);
			dest[destOffset+len-1] = 0;
		}
	}
	return len;
}
/**
 * Load a Utf8 String as saved by store().
 * @param source The byte encoded String.
 * @param sourceOffset the start of the byte encoded String.
 * @param chars the destination for the String characters.
 * @param offset the offset in the destination for the characters.
 * @return The number of characters loaded or needed if chars is null. If this return -1
 * then this indicates a null String which is different to an empty String which returns 0.
 */
//===================================================================
public static int load(byte[] source, int sourceOffset, char[] chars, int offset)
//===================================================================
{
	if (source == null) return -1;
	int size = Utils.readInt(source,sourceOffset,2);
	if (size == 0) return -1;
	if (chars != null)
		Utils.decodeJavaUtf8String(source, sourceOffset+2, size-1, chars, offset);
	return Utils.sizeofJavaUtf8String(source,sourceOffset+2,size-1);
	//return size-1;
}
/**
* Store a String as UTF, prepending the lengh of the String as a short integer and
* appending a zero byte, or find out the number of bytes required.
*
* @param theString the String to write.
* @param dest the destination byte array, or null to just find out the number of bytes needed.
* @param offset the location in the destinatin to write to.
* @return the number of bytes required. A null String will require 2 bytes, an empty
* String will require 3 bytes.
*/
//===================================================================
public static int store(String theString, byte[] dest, int offset)
//===================================================================
{
	return store(
		theString == null ? null : ewe.sys.Vm.getStringChars(theString),
		0, theString == null ? 0 : theString.length(), dest, offset);
}
//===================================================================
public static byte [] store(String theString)
//===================================================================
{
	int len = store(theString, null, 0);
	byte [] ret = new byte[len];
	store(theString,ret,0);
	return ret;
}

/**
 * Load a Utf8 String as saved by storeUtf8String() as a char array.
 * @param source The byte encoded String.
 * @param sourceOffset the start of the byte encoded String.
 * @return The char array holding the characters, or null if a null string was stored.
 */
//===================================================================
public static char[] loadChars(byte[] source,int sourceOffset)
//===================================================================
{
	int len = load(source, sourceOffset, null, 0);
	if (len == -1) return null;
	char [] ch = new char[len];
	load(source, sourceOffset, ch, 0);
	return ch;
}
/**
 * Load a Utf8 String as saved by storeUtf8String().
 * @param source The byte encoded String.
 * @param sourceOffset the start of the byte encoded String.
 * @return The String saved, which may be null.
 */
//===================================================================
public static String load(byte [] source, int sourceOffset)
//===================================================================
{
	char[] ch = loadChars(source,sourceOffset);
	return ch == null ? null : ewe.sys.Vm.createStringWithChars(ch);
}
/**
 * Load a Utf8 String as saved by storeUtf8String() and return it in a CharArray
 * @param source The byte encoded String.
 * @param sourceOffset the start of the byte encoded String.
 * @param dest the CharArray to store in or a new one if dest is null. The CharArray
 * will be cleared before the data is written to it.
 * @return The  saved, which may be null.
 */
//===================================================================
public static CharArray load(byte [] source, int sourceOffset,CharArray dest)
//===================================================================
{
	if (dest == null) dest = new CharArray();
	dest.length = 0;
	int len = load(source,sourceOffset,null,0);
	if (len == -1) return dest;
	if (dest.data == null || dest.data.length < len) dest.data = new char[len];
	load(source,sourceOffset,dest.data,0);
	dest.length = len;
	return dest;
}
/**
 * Load a Utf8 String as saved by storeUtf8String() and return it in a CharArray
 * @param source The byte encoded String.
 * @param sourceOffset the start of the byte encoded String.
 * @param dest the SubString to store in or a new one if dest is null.
 * @return The  String as a SubString saved, which may be null.
 */
//===================================================================
public static SubString load(byte [] source, int sourceOffset,SubString dest)
//===================================================================
{
	char[] ret = loadChars(source, sourceOffset);
	if (ret == null) return null;
	if (dest == null) dest = new SubString();
	dest.data = ret;
	dest.start = 0;
	dest.length = ret.length;
	return dest;
}
//===================================================================
public String toString()
//===================================================================
{
	return load(data,start);
}
//===================================================================
public EncodedUTF8String set(byte[] source, int sourceStart)
//===================================================================
{
	data = source;
	this.start = sourceStart;
	return this;
}
//##################################################################
}
//##################################################################

