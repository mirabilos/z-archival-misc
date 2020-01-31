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
import ewe.reflect.*;

//##################################################################
public class TextDecoder{
//##################################################################
Vector all = new Vector();
//===================================================================
public TextDecoder(String str)
//===================================================================
{
	SubString ss = new SubString().set(str);
	ss.split('&',all);
	int num = all.size();
	for (int i = 0; i<num; i++){
		SubString sub = (SubString)all.get(i);
		int end = sub.start+sub.length;
		int did = 0;
		boolean hitEquals = false;
		for (int s = sub.start, d = sub.start; s<end;){
			char c = sub.data[s++];
			if (hitEquals){
				if (c == '+') c = ' ';
				else if (c == '%' && s+1<end) {
					char h = sub.data[s++];
					if (h >= 'a' && h <= 'z') h ^= 0x20; // Convert to caps.
					if (h >= '0' && h <= '9') h -= '0';
					else h -= 'A'-(char)10;
					c = (char)((h & 0xf) << 4);
					h = sub.data[s++];
					if (h >= 'a' && h <= 'z') h ^= 0x20; // Convert to caps.
					if (h >= '0' && h <= '9') h -= '0';
					else h -= 'A'-(char)10;
					c |= (h & 0xf);
					did+=1;
				}
			}else if (c == '=') hitEquals = true;
			sub.data[d++] = c;
		}
		sub.length -= did*2;
	}
}

//===================================================================
public static String decode(char [] data,int start,int length)
//===================================================================
{
	char [] nd = new char[data.length];
	int d = 0;
	for (int i = 0; i<length; i++){
		char c = data[start+i];
		if (c == '+') c = ' ';
		else if (c == '%' && i<length-2) {
			char h = data[start+(++i)];
			if (h >= 'a' && h <= 'z') h ^= 0x20; // Convert to caps.
			if (h >= '0' && h <= '9') h -= '0';
			else h -= 'A'-(char)10;
			c = (char)((h & 0xf) << 4);
			h = data[start+(++i)];
			if (h >= 'a' && h <= 'z') h ^= 0x20; // Convert to caps.
			if (h >= '0' && h <= '9') h -= '0';
			else h -= 'A'-(char)10;
			c |= (h & 0xf);
		}
		nd[d++] = c;
	}
	return new String(nd,0,d);
}
//===================================================================
public int size() {return all.size();}
//===================================================================
/**
 * Get the index of the named value.
 * @param name The name of the value.
 * @return The index of the value, or -1 if it is not present.
 */
//===================================================================
public int getIndexOf(String name)
//===================================================================
{
	int num = all.size();
	for (int i = 0; i<num; i++){
		SubString sub = (SubString)all.get(i);
		if (isNamed(sub,name)) return i;
	}
	return -1;
}

/**
 * Get the value of the name.
  * @param name The name of the value.
  * @return The value or null if it is not set.
 */
//===================================================================
public String getValue(String name)
//===================================================================
{
	int num = all.size();
	for (int i = 0; i<num; i++){
		SubString sub = (SubString)all.get(i);
		if (isNamed(sub,name)) return getValueOf(sub);
	}
	return null;
}

//-------------------------------------------------------------------
int getEqualIndex(int index)
//-------------------------------------------------------------------
{
	if (index < 0 || index > size()) return -1;
	SubString sub = (SubString)all.get(index);
	return SubString.indexOf('=',sub.data,sub.start,sub.length,sub.start,0);
}
/**
 * The name of the value at the specified index.
 * @param index
 * @return The name at the index.
 */
//===================================================================
public String getName(int index)
//===================================================================
{
	int id = getEqualIndex(index);
	if (id == -1) return null;
	SubString sub = (SubString)all.get(index);
	return decode(sub.data,sub.start,id-sub.start);
}
/**
 * The value at the specified index.
 * @param index
 * @return  The value at the specified index.
 */
//===================================================================
public String getValue(int index)
//===================================================================
{
	int id = getEqualIndex(index);
	if (id == -1) return null;
	SubString sub = (SubString)all.get(index);
	return new String(sub.data,id+1,sub.length-(id-sub.start)-1);
}
//===================================================================
public static boolean isNamed(SubString sub,String name)
//===================================================================
{
	int id = SubString.indexOf('=',sub.data,sub.start,sub.length,sub.start,0);
	if (id == -1) return false;
	String nameIs = decode(sub.data,sub.start,id-sub.start);
	return name.equals(nameIs);
}
//===================================================================
public static String getValueOf(SubString sub)
//===================================================================
{
	int id = SubString.indexOf('=',sub.data,sub.start,sub.length,sub.start,0);
	if (id == -1) return null;
	return new String(sub.data,id+1,sub.length-(id-sub.start)-1);
}

//===================================================================
public String toString()
//===================================================================
{
	String s = "";
	for (int i = 0; i<all.size(); i++){
		s += "["+all.get(i)+"]";
	}
	return s;
}

//===================================================================
public void setValue(String name,String value)
//===================================================================
{
	deleteValue(name);
	String s = name+"="+value;
	all.add(new SubString().set(s));
}
//===================================================================
public void deleteValue(String name)
//===================================================================
{
	int idx = getIndexOf(name);
	if (idx == -1) return;
	all.del(idx);
}
/**
 * Re-encode the decoded text into a String.
 * @return The encoded string.
 */
//===================================================================
public String encode()
//===================================================================
{
	TextEncoder te = new TextEncoder();
	int num = all.size();
	for (int i = 0; i<num; i++){
		String name = getName(i);
		String value = getValue(i);
		te.addValue(name,value);
	}
	return te.toString();
}
//##################################################################
}
//##################################################################

