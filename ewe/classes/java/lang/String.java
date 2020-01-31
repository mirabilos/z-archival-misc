/*********************************************************************************
 *  Ewe Virtual Machine - Version 1.14, January 24, 2002                         *
 *  Copyright (C) 1999-2002 Michael L Brereton <michael_brereton@ewesoft.com>    *
 *  Copyright (C) 1998, 1999 Wabasoft <www.wabasoft.com>                         *
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
package java.lang;
import ewe.io.IO;
import ewe.io.IOException;
import ewe.io.TextCodec;
import ewe.io.UnsupportedEncodingException;
import ewe.sys.Vm;
import ewe.util.ByteArray;
import ewe.util.CharArray;
import ewe.util.SubString;
/**
 * String is an array of characters.
 * <p>
 * As with all classes in the ewe.lang package, you can't reference the
 * String class using the full specifier of ewe.lang.String.
 * The ewe.lang package is implicitly imported.
 * Instead, you should simply access the String class like this:
 * <pre>
 * String s = new String("Hello");
 * </pre>
 */

public final class String
{
//
// Do not move this or add more.
//
char chars[];

/** Creates an empty string. */
public String()
	{
	chars = new char[0];
	}

/**
 * Creates a string from an array of strings. This method is declared protected and
 * for internal use only. The StringBuffer class should be used to create a string
 * from an array of strings.
 */
	/*
String(String s[], int count)
	{
	int len = 0;
	for (int i = 0; i < count; i++)
		len += s[i].chars.length;
	char c[] = new char[len];
	int j = 0;
	for (int i = 0; i < count; i++)
		{
		int slen = s[i].chars.length;
		ewe.sys.Vm.copyArray(s[i].chars, 0, c, j, slen);
		j += slen;
		}
	chars = c;
	}
*/
/** Creates a copy of the given string. */
public String(String s)
	{
	chars = s.chars;
	}

/** Creates a string from the given character array. */
public String(char c[])
	{
	this(c, 0, c.length);
	}

/**
 * Creates a string from a portion of the given character array.
 * @param c the character array
 * @param offset the position of the first character in the array
 * @param count the number of characters
 */
public String(char c[], int offset, int count)
	{
	char chars[] = new char[count];
	ewe.sys.Vm.copyArray(c, offset, chars, 0, count);
	this.chars = chars;
	}

/** Returns the length of the string in characters. */
public int length()
	{
	return chars.length;
	}

/** Returns the character at the given position. */
public char charAt(int i)
	{
	return chars[i];
	}

/** Concatenates the given string to this string and returns the result. */
public String concat(String s)
	{
	if (s == null || s.chars.length == 0)
		return this;
	return this + s;
	}

/**
  * Returns this string as a character array. The array returned is
  * allocated by this method and is a copy of the string's internal character
  * array.
  */
public char[] toCharArray()
	{
	int length = length();
	char chars[] = new char[length];
	ewe.sys.Vm.copyArray(this.chars, 0, chars, 0, length);
	return chars;
	}

public void getChars( int srcBegin, int srcEnd, char dst[], int dstBegin )
{
	ewe.sys.Vm.copyArray(this.chars,srcBegin,dst,dstBegin,srcEnd-srcBegin);
}

public static String copyValueOf(char [] data,int start,int length)
{
	return new String(data,start,length);
}
public static String copyValueOf(char [] data)
{
	return new String(data);
}

public static String valueOf(char [] data)
{
	return new String(data);
}
public static String valueOf(char [] data,int start,int length)
{
	return new String(data,start,length);
}
/** Converts the given boolean to a String. */
public static String valueOf(boolean b)
	{
	return ewe.sys.Convert.toString(b);
	}

/** Converts the given char to a String. */
public static String valueOf(char c)
	{
	return ewe.sys.Convert.toString(c);
	}

/** Converts the given int to a String. */
public static String valueOf(int i)
	{
	return ewe.sys.Convert.toString(i);
	}

/** Converts the given float to a String. */
public static String valueOf(float f)
	{
	return ewe.sys.Convert.toString(f);
	}

public static String valueOf(double d)
	{
	return ewe.sys.Convert.toString(d);
	}
public static String valueOf(long l)
  {
	return ewe.sys.Convert.toString(l);
	}
public static String valueOf(Object obj)
{
	if (obj == null) return "null";
	else return obj.toString();
}
/** Returns this string. */
public String toString()
	{
	return this;
	}

/**
 * Returns a substring of the string. The start value is included but
 * the end value is not. That is, if you call:
 * <pre>
 * string.substring(4, 6);
 * </pre>
 * a string created from characters 4 and 5 will be returned.
 * @param start the first character of the substring
 * @param end the character after the last character of the substring
 */
public String substring(int start, int end)
	{
	return new String(chars, start, end - start);
	}

/**
* Get a substring from the specified index to the end of the string.
**/
public String substring(int start)
{
	return substring(start,chars.length);
}
/**
 * Returns true if the given string is equal to this string and false
 * otherwise. If the object passed is not a string, false is returned.
 */
/*
public boolean equals(Object obj)
	{
	//MLB
	if (obj == this) return true;
	if (obj instanceof String)
		{
		String s = (String)obj;
		if (chars.length != s.chars.length)
			return false;
		for (int i = 0; i < chars.length; i++)
			if (chars[i] != s.chars[i])
				return false;
		}
	return true;
	}
*/
//===================================================================
public boolean startsWith(String prefix)
//===================================================================
{
	if (prefix.chars.length <= 0) return true;
	return equals(prefix,0,0);
}
//===================================================================
public boolean startsWith(String prefix, int offset)
//===================================================================
{
	if (prefix.chars.length <= 0) return true;
	if (offset+prefix.chars.length > chars.length) return false;
	return equals(prefix,offset,0);
}
//===================================================================
public String replace(char oldChar,char newChar)
//===================================================================
{
	String s = new String();
	char [] newChars = s.chars = new char[chars.length];
	for (int i = 0; i<newChars.length; i++){
		char c = chars[i];
		newChars[i] = c == oldChar ? newChar : c;
	}
	return s;
}
//===================================================================
public boolean equals(Object obj)
//===================================================================
{
	if (!(obj instanceof String)) return false;
	return equals((String)obj,0,2);
	/*
	if (obj == this) return true;
	if (!(obj instanceof String) || obj == null) return false;
	String s = (String)obj;
	if (s.chars.length != chars.length) return false;
	return startsWith(s);
	*/
}
//===================================================================
public boolean equalsIgnoreCase(String other)
//===================================================================
{
	return equals(other,0,3);
}
/*
Flags for native equals:
 1 = IgnoreCase
 2 = Must be full length.
*/
//-------------------------------------------------------------------
private native boolean equals(String other,int myStart,int options);
//-------------------------------------------------------------------

//===================================================================
public boolean endsWith(String other)
//===================================================================
{
	if (other == null) throw new NullPointerException();
	if (other.chars.length <= 0) return true;
	return equals(other,chars.length-other.chars.length,2);
}

//===================================================================
public int indexOf(String other){return indexOf(other,0);}
public int indexOf(String other,int start)
//===================================================================
{
	if (other == null) throw new NullPointerException();
	char [] oc = other.chars;
	if (oc.length <= 0) return start;//-1;
	if (oc.length > chars.length) return -1;
	for (int s = start; s<chars.length; s++){
		int idx = SubString.indexOf(oc[0],chars,0,chars.length,s,0);
		if (idx == -1) return -1;
		if (SubString.equals(chars,idx,chars.length-idx,oc,0,oc.length,SubString.STARTS_WITH)) return idx;
		s = idx;
	}
	return -1;
}
//===================================================================
public int lastIndexOf(String other){return lastIndexOf(other,chars.length-1);}
public int lastIndexOf(String other,int start)
//===================================================================
{
	if (other == null) throw new NullPointerException();
	char [] oc = other.chars;
	if (oc.length <= 0) return start;//-1;
	if (oc.length > chars.length) return -1;
	for (int s = start; s>=0; s--){
		int idx = lastIndexOf(oc[0],s);
		if (idx == -1) return -1;
		if (SubString.equals(chars,idx,chars.length-idx,oc,0,oc.length,SubString.STARTS_WITH)) return idx;
		s = idx;
	}
	return -1;
}
//===================================================================
public int indexOf(int ch) {return indexOf(ch,0);}
public int indexOf(int ch,int start)
//===================================================================
{
	return SubString.indexOf((char)ch,chars,0,chars.length,start,0);
}
//===================================================================
public int lastIndexOf(int ch) {return lastIndexOf(ch,chars.length-1);}
public int lastIndexOf(int ch,int start)
//===================================================================
{
	return SubString.indexOf((char)ch,chars,0,chars.length,start,SubString.BACKWARDS);
}

//===================================================================
public int compareTo(String other)
//===================================================================
{
	if (other == null) return 1;
	return SubString.compare(chars,0,chars.length,other.chars,0,other.chars.length);
}
//===================================================================
public String trim()
//===================================================================
{
	char [] all = ewe.sys.Vm.getStringChars(this);
	int s, e;
	for (s = 0; s<all.length; s++)
		if (all[s] > ' ' || all[s] < 0) break;
	if (s == all.length) return "";
	for (e = all.length; e > s; e--)
		if (all[e-1] > ' ' || all[e-1] < 0) break;
	return substring(s,e);
}
//-------------------------------------------------------------------
private String changeMyCase(boolean toUpper)
//-------------------------------------------------------------------
{
	ewe.sys.Vm.getLocale().changeCase(chars,0,chars.length,toUpper);
	return this;
}
//===================================================================
public String toUpperCase(){return new String(chars).changeMyCase(true);}
//===================================================================
public String toLowerCase(){return new String(chars).changeMyCase(false);}
//===================================================================
public int hashCode()
//===================================================================
{
	return ewe.util.Utils.makeHashCode(chars,0,chars.length);
}
//===================================================================
public String(byte [] utf8Bytes)
//===================================================================
{
	this(utf8Bytes,0,utf8Bytes.length);
}
//===================================================================
public String(byte [] utf8Bytes,int start,int length)
//===================================================================
{
	int size = ewe.util.Utils.sizeofJavaUtf8String(utf8Bytes,start,length);
	chars = new char [size];
	ewe.util.Utils.decodeJavaUtf8String(utf8Bytes,start,length,chars,0);
}

//===================================================================
public boolean regionMatches(boolean ignoreCase,int toffset,String other,int oofset,int len)
//===================================================================
{
	if (toffset+len > chars.length || oofset+len > other.chars.length || oofset < 0 || toffset < 0)
		return false;
	return SubString.equals(chars,toffset,len,other.chars,oofset,len,ignoreCase ? SubString.IGNORE_CASE : 0);
}
//===================================================================
public boolean regionMatches(int toffset,String other,int oofset,int len)
//===================================================================
{
	return regionMatches(false,toffset,other,oofset,len);
}
/**
 * Convert the characters into bytes according to the default enocoding format.
 * Under a
 * native Ewe VM this will be UTF8 encoding.
 * @return an array of bytes representing the String.
 */
//===================================================================
public byte [] getBytes()
//===================================================================
{
	return ewe.util.Utils.encodeJavaUtf8String(this);
}
public byte[] getBytes(String encoding) throws UnsupportedEncodingException
{
	TextCodec tc = IO.getCodec(encoding);
	if (tc == null) throw new UnsupportedEncodingException();
	try{
		ByteArray ba = tc.encodeText(chars,0,chars.length,true,null);
		return ba.toBytes();
	}catch(IOException e){
		return null;
	}
}
public String(byte[] bytes, int offset, int length, String encoding) throws UnsupportedEncodingException
{
	TextCodec tc = IO.getCodec(encoding);
	if (tc == null) throw new UnsupportedEncodingException();
	try{
		CharArray ca = tc.decodeText(bytes,offset,length,true,null);
		chars = new char[ca.length];
		if (ca.length != 0) System.arraycopy(ca.data,0,chars,0,ca.length);
	}catch(IOException e){
		RuntimeException ex = new RuntimeException("Badly encoded bytes");
		Vm.setCause(ex,e);
		throw ex;
	}
}
public String(byte[] bytes, String encoding) throws UnsupportedEncodingException
{
	this(bytes,0,bytes.length,encoding);
}
public String(StringBuffer buffer)
{
	this(buffer.toString());
}

}

