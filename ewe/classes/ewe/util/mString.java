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
* This provides many additional utility String manipulation routines.
**/
//##################################################################
public class mString {
//##################################################################
/**
* Splits up a String using the specified separator and add the substrings
* to the destination Vector. It returns the destination vector and will
* create a new one if the provided one is null.
**/
//===================================================================
public static Vector split(String what,char separator,Vector dest)
//===================================================================
{
	return SubString.split(what,separator,dest);
/*
	if (dest == null) dest = new Vector();
	dest.clear();
	if (what == null) return dest;
	if (what.length() == 0) return dest;
	StringBuffer sb = new StringBuffer();
	for (int i = 0;i<what.length(); i++){
		char c = what.charAt(i);
		if (c == separator) {
			dest.add(sb.toString());
			sb = new StringBuffer();
		}else{
			sb.append(c);
		}
	}
	String s = sb.toString();
	dest.add(s);
	return dest;
*/
}
/**
* Splits up a String using the specified separator and returns an
* array of sub Strings.
**/
//===================================================================
public static String [] split(String what,char separator)
//===================================================================
{
	Vector v = split(what,separator,null);
	String [] dest = new String[v.size()];
	v.copyInto(dest);
	return dest;
}
/**
* This splits a String using '|' as the separator.
**/
//===================================================================
public static String [] split(String what)
//===================================================================
{
	return split(what,'|');
}
//===================================================================
public static void toBytes(String what,byte [] dest,int offset) {toBytes(what,dest,offset,false);}
//===================================================================
public static void toBytes(String what,byte [] dest,int offset,boolean ascii)
//===================================================================
{
	if (what == null) return;
	int l = what.length();
	char [] chars = what.toCharArray();
	for (int i = 0; i<l; i++) {
		char ch = chars[i];
		if (ascii) dest[i+offset] = (byte)(ch & 0xff);
		else{
			dest[i*2+offset+1] = (byte)(ch & 0xff);
			dest[i*2+offset] = (byte)((ch >> 8)&0xff);
		}
	}
}
//===================================================================
public static byte [] toAscii(String what)
//===================================================================
{
	if (what == null) return new byte[0];
	byte [] dest = new byte[what.length()];
	toBytes(what,dest,0,true);
	return dest;
}
//===================================================================
public static String fromAscii(byte [] bytes,int start,int length)
//===================================================================
{
	if (length <= 0) return "";
	char [] c = new char[length];
	for (int i = 0; i<length; i++)
		c[i] = (char)((int)bytes[start+i] & 0xff);
	return new String(c);
}
//===================================================================
public static byte [] toBytes(String what)
//===================================================================
{
	if (what == null) return new byte[0];
	byte [] dest = new byte[what.length()*2];
	toBytes(what,dest,0);
	return dest;
}
//===================================================================
public static String fromBytes(byte [] bytes,int offset,int stringLength)
//===================================================================
{
	char [] ch = new char[stringLength];
	for (int i = 0; i<stringLength; i++){
		char c = (char)bytes[i*2+offset+1];
		c |= (char)(bytes[i*2+offset]<<8)&0xff;
		ch[i] = c;
	}
	return new String(ch);
}
//===================================================================
public static String fromBytes(byte [] bytes) {return fromBytes(bytes,0,bytes.length/2);}
//===================================================================
public static String toString(Object what)
//===================================================================
{
	if (what == null) return "";
	else if (what instanceof String) return (String)what;
	else if (what instanceof Object[]) return new Vector((Object[])what).toString();
	else if (ewe.reflect.Array.isArray(what))
		return ewe.reflect.Array.getComponentType(what)+"["+ewe.reflect.Array.getLength(what)+"]";
	else return what.toString();
}
public static final int Uppercase = 1, Lowercase = 2;
//==================================================================
public static void changeCase(char [] chars,int toWhat)
//==================================================================
{
	int l = chars.length;
	for (int i = 0; i<l; i++) {
		char ch = chars[i];
		if (toWhat == Uppercase)
			if (ch >= 'a' && ch <= 'z') chars[i] = (char)(ch+'A'-'a');
		if (toWhat == Lowercase)
			if (ch >= 'A' && ch <= 'Z') chars[i] = (char)(ch+'a'-'A');
	}
}
//==================================================================
public static String changeCase(String what,int toWhat)
//==================================================================
{
	if (what == null) return null;
	char [] chars = what.toCharArray();
	changeCase(chars,toWhat);
	return new String(chars);
}
//==================================================================
public static int compare(String one,String two) {return compare(one,two,false);}
public static int compare(String one,String two,boolean ignoreCase)
//==================================================================
{
	if (one == null)
		if (two == null) return 0;
		else return -1;
	else
		if (two == null) return 1;

	char [] ones = one.toCharArray();
	char [] twos = two.toCharArray();

	if (ignoreCase) {
		changeCase(ones,Uppercase);
		changeCase(twos,Uppercase);
	}
	int len = ones.length, len2 = twos.length;
	for (int i = 0; i<len; i++) {
		if (i >= len2) return 1;
		int diff = ones[i]-twos[i];
		if (diff != 0) return diff;
	}
	if (len < len2) return -1;
	else if (len > len2) return 1;
	else return 0;
}
//===================================================================
public static int indexOf(String s,char what)
//===================================================================
{
	if (s == null) return -1;
	int w = (int)what;
	for (int i = 0; i<s.length(); i++)
		if (s.charAt(i) == w) return i;
	return -1;
}
/**
* This returns the string to the left of the specified character (not including the character).
* If the character is not found then it will return the entire string.
**/
//===================================================================
public static String leftOf(String s,char c)
//===================================================================
{
	int idx = s.lastIndexOf(c);
	if (idx == -1) return s;
	else return s.substring(0,idx);
}
/**
* This returns the string to the right of the specified character (not including the character).
* If the character is not found then it will return an empty string.
**/
//===================================================================
public static String rightOf(String s,char c)
//===================================================================
{
	int idx = s.lastIndexOf(c);
	if (idx == -1) return "";
	else return s.substring(idx+1);
}
/**
* This removes a trailing "/" or "\" character.
**/
//===================================================================
public static String removeTrailingSlash(String s)
//===================================================================
{
	if (s == null) return s;
	char [] all = ewe.sys.Vm.getStringChars(s);
	int len = all.length;
	if (len <= 0) return s;
	if (all[len-1] == '/' || all[len-1] == '\\')
		return new String(all,0,len-1);
	else
		return s;
}
//===================================================================
public static String [] splitCommand(String args,String prepend)
//===================================================================
{
	ewe.util.Vector v = new ewe.util.Vector();
	if (prepend != null) v.add(prepend);
	if (args != null){
		char [] chars = ewe.sys.Vm.getStringChars(args);
		int st = 0;
		char quote = 0;
		for (int i = 0; i<chars.length; i++){
			char c = chars[i];
			if (c == '"' || c == '\''){
				if (quote == 0) quote = c;
				else if (quote == c) quote = 0;
				continue;
			}
			if ((c == ' ' || c == '\t') && (quote == 0)) {
				if (st == i) {
					st = i+1;
					continue;
				}else{
					v.add(args.substring(st,i));
					st = i+1;
				}
			}
		}
		if (st != chars.length) v.add(args.substring(st,chars.length));
	}
	String [] ret = new String[v.size()];
	//ewe.sys.Vm.debug(v.toString());
	v.copyInto(ret);
	return ret;
}
/**
* Copy the characters from the String into the destination in the most
* memory effecient way.
* @param data The string data.
* @param dataStart The starting character in the String.
* @param destination The destination character array.
* @param destinationStart The starting point in the destination array.
* @param length The number of characters to copy.
*/
//===================================================================
public static void copyInto(String data,int dataStart,char[] destination,int destinationStart,int length)
//===================================================================
{
	char [] dc = ewe.sys.Vm.getStringChars(data);
	ewe.sys.Vm.copyArray(dc,dataStart,destination,destinationStart,length);
}
/**
* Copy the characters from the String into the destination in the most
* memory effecient way.
* @param data The string data.
* @param destination The destination character array.
* @param destinationStart The starting point in the destination array.
 */
//===================================================================
public static void copyInto(String data,char[] destination,int destinationStart)
//===================================================================
{
	copyInto(data,0,destination,destinationStart,data.length());
}
//##################################################################
}
//##################################################################

