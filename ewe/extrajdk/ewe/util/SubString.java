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

//##################################################################
public class SubString implements ewe.util.Comparable{
//##################################################################
/* Do not move these, they are used by native methods.*/
public char [] data = null;
public int start = 0;
public int length = 0;
/*.....................................................*/

//===================================================================
public String toString()
//===================================================================
{
	if (data == null) return null;
	return new String(data,start,length);
}

//===================================================================
public boolean isNull() {return data == null;}
public int length() {return length;}
//===================================================================
public char charAt(int idx) {return data[idx+start];}
//===================================================================
public void copyInto(char [] dest,int offset)
//===================================================================
{
	copyInto(dest,offset,length,0);
}
//===================================================================
public void copyInto(char [] dest,int offset,int len,int startPoint)
//===================================================================
{
	if (data == null) return;
	ewe.sys.Vm.copyArray(data,startPoint+start,dest,offset,len);
}
//===================================================================
public void copyInto(byte [] dest,int offset,boolean ascii)
//===================================================================
{
	copyInto(dest,offset,length,0,ascii);
}
//===================================================================
public void copyInto(byte [] dest,int offset,int len,int startPoint,boolean ascii)
//===================================================================
{
	if (data == null) return;
	if (ascii)
		for (int i = 0; i<len; i++)
			dest[i+offset] = (byte)(data[start+startPoint+i]&0xff);
	else
		for (int i = 0; i<len; i++){
			dest[i*2+offset] = (byte)((data[start+startPoint+i] >> 8)&0xff);
			dest[i*2+offset+1] = (byte)(data[start+startPoint+i]&0xff);
	}
}
//===================================================================
public SubString set(char [] dt,int st,int len)
//===================================================================
{
	data = dt; start = st; length = len;
	return this;
}
//===================================================================
public SubString set(String string)
//===================================================================
{
	if (string == null) return set(string,0,0);
	else return set(string,0,string.length());
}
//===================================================================
public SubString set(String string,int st,int len)
//===================================================================
{
	if (string != null){
		data = new char[len];
		start = 0;
		length = len;
		string.getChars(st,st+len,data,0);
	}else{
		data = null;
		start = length = 0;
	}
	return this;
}
//===================================================================
public SubString(){}
//===================================================================
/**
* This will split this substring and place the results as a series of
* SubString objects in the destination list. If the destination contains
* SubString objects already, then, each be used as a destination. If there
* is not enough SubStrings in the destination then more will be added. If
* there is too many they will NOT be removed. The return value is the number
* of substrings found.
**/
//===================================================================
public int split(char separator,Vector destination)
//===================================================================
{
	return split(separator,destination,0);
}
//===================================================================
public int split(char separator,Vector destination,int destinationStart)
//===================================================================
{
	boolean space = separator == ' ';
	if (data == null) return 0;
	if (length == 0) return 0;
	int found = 0;
	int end = start+length;
	if (space) {
		for (int i = start; i<end;){
			while(i<end && Character.isWhitespace(data[i])) i++;
			if (i>=end) break;
			int st = i;
			while(i<end && !Character.isWhitespace(data[i])) i++;
			SubString d;
			if (found+destinationStart >= destination.size()) {
				d = new SubString();
				destination.add(d);
			}else
				d = (SubString)destination.get(found+destinationStart);
			d.set(data,st,i-st);
			found++;
		}
	}else{
		for (int i = 0;;){
			int st = i;
			while(i<end && data[i] != separator) i++;
			SubString d;
			if (found+destinationStart >= destination.size()) {
				d = new SubString();
				destination.add(d);
			}else d = (SubString)destination.get(found+destinationStart);
			d.set(data,st,i-st);
			found++;
			if (i>=end) break;
			i++;
		}
	}
	return found;
}
private static Vector strs = new Vector();
//===================================================================
public static Vector split(String what,char separator,Vector dest)
//===================================================================
{
	if (dest == null) dest = new Vector();
	dest.clear();
	SubString ss = new SubString().set(what);
	strs = new Vector();
	int got = ss.split(separator,strs,0);
	for (int i = 0; i<got; i++)
		dest.add(((SubString)strs.get(i)).toString());
	return dest;
}

public static final int IGNORE_CASE = 0x1;
public static final int STARTS_WITH = 0x2;
public static final int BACKWARDS = 0x4; //Sometimes a backwards check is faster.

/**
* This compares the character sequence given by the "small" parameters
* to the character sequence given by the "big" parameters.
**/
//===================================================================
public static boolean equals(
	char [] big,int bigStart,int bigLength,
	char [] small,int smallStart,int smallLength,
	int options)
//===================================================================
{
	boolean sw = (options & STARTS_WITH) != 0;
	boolean ig = (options & IGNORE_CASE) != 0;
	boolean back = (options & BACKWARDS) != 0;

	if (bigLength < smallLength) return false;
	if (!sw && (bigLength != smallLength)) return false;
	if (!back){
		int s = smallStart, end = smallLength+smallStart;
		int b = bigStart;
		if (!ig){
			while(s<end)
				if (small[s++] != big[b++]) return false;
		}else{
			while(s<end){
				char c1 = small[s++];
				char c2 = big[b++];
				if (c1 >= 'a' && c1 <= 'z') c1 ^= 0x20;
				if (c2 >= 'a' && c2 <= 'z') c2 ^= 0x20;
				if (c1 != c2) return false;
			}
		}
	}else{
		int s = smallStart+smallLength-1, end = smallStart;
		int b = bigStart+smallLength-1;
		if (!ig){
			while(s>=end)
				if (small[s--] != big[b--]) return false;
		}else{
			while(s>=end){
				char c1 = small[s--];
				char c2 = big[b--];
				if (c1 >= 'a' && c1 <= 'z') c1 ^= 0x20;
				if (c2 >= 'a' && c2 <= 'z') c2 ^= 0x20;
				if (c1 != c2) return false;
			}
		}
	}
	return true;
}

//===================================================================
public static int indexOf(char what,char [] str,int strStart,int strLength,int start,int options)
//===================================================================
{
	if ((options & BACKWARDS) == 0){
		int s = start, end = strStart+strLength;
		while(s<end)
			if (str[s++] == what) return s-1;
		return -1;
	}else{
		int s = start, end = strStart;
		while(s>=end)
			if (str[s--] == what) return s+1;
		return -1;
	}
}
/**
* This does a numeric char for char comparison.
**/
//===================================================================
public static int compare(char [] one,int oneStart,int oneLength,char [] two,int twoStart,int twoLength)
//===================================================================
{
	if (one == two) return 0;
	if (one == null) return -1;
	else if (two == null) return 1;

	int max = oneLength > twoLength ? twoLength : oneLength;
	for (int i = 0; i<max; i++)
		if (one[oneStart+i] > two[twoStart+i]) return 1;
		else if (one[oneStart+i] < two[twoStart+i]) return -1;
	if (oneLength > twoLength) return 1;
	else if (oneLength < twoLength) return -1;
	else return 0;
}

//===================================================================
public int compareTo(Object other)
//===================================================================
{
	return compareTo(other,0);
}
//===================================================================
public int compareTo(Object other,int options)
//===================================================================
{
	int opt = options == IGNORE_CASE ? ewe.sys.Locale.IGNORE_CASE : 0;
	if (other instanceof SubString){
		SubString o = (SubString)other;
		if (data == null)
			if (o.data == null) return 0;
			else return -1;
		return ewe.sys.Vm.getLocale().compare(data,start,length,o.data,o.start,o.length,opt);
	}
	if (data == null) return -1;
	if (other instanceof String){
		char [] c = ewe.sys.Vm.getStringChars((String)other);
		return ewe.sys.Vm.getLocale().compare(data,start,length,c,0,c.length,opt);
	}else
		return 1;
}
//===================================================================
public boolean equals(Object other)
//===================================================================
{
	return compareTo(other,0) == 0;
}

//===================================================================
public int hashCode()
//===================================================================
{
	if (data == null) return 0;
	else return toString().hashCode();
}
/**
 * Get a new SubString that is a substring of this SubString, optionally copying the data
 * out into a new array.
 * @param start The first index in this SubString.
 * @param length The number of characters.
 * @param destination The destination SubString. If this is null a new one will be created.
 * @param copyDataInto If this is null then the new SubString will reference the original
 * character array for its data. If it is not null then the characters will be copied into
 * this array. if this array is not big enough, a new one will be created and that one will
 * be used instead.
 * @return A new SubString (or the provided destination) representing part or all of the original
 * substring.
 */
//===================================================================
public SubString substring(int start,int length,SubString destination,char[] copyDataInto)
//===================================================================
{
	if (start < 0 || start+length > this.length) throw new IndexOutOfBoundsException();
	if (destination == null) destination = new SubString();
	if (copyDataInto != null){
		if (copyDataInto.length < length) copyDataInto = new char[length];
		ewe.sys.Vm.copyArray(data,this.start+start,copyDataInto,0,length);
		destination.data = copyDataInto;
		destination.start = 0;
		destination.length = length;
	}else{
		destination.data = data;
		destination.start = this.start+start;
		destination.length = length;
	}
	return destination;
}
/**
 * Get a new SubString that is a substring of this SubString.
 * @param start The first index in this SubString.
 * @param length The number of characters.
 * @param destination The destination SubString. If this is null a new one will be created.
 * @return A new SubString (or the provided destination) representing part or all of the original
 * substring.
 */
//===================================================================
public SubString substring(int start,int length,SubString destination)
//===================================================================
{
	return substring(start,length,destination,null);
}
/**
 * Get a new SubString that is a substring of this SubString.
 * @param start The first index in this SubString.
 * @param length The number of characters.
 * @return A new String representing part or all of the original
 * substring.
 */
//===================================================================
public String substring(int start,int length)
//===================================================================
{
	if (data == null) return null;
	return new String(data,start+this.start,length);
}
/**
* Get a copy of the SubString.
 * @param destination The destination SubString. If this is null a new one will be created.
 * @param copyDataInto If this is null then the new SubString will reference the original
 * character array for its data. If it is not null then the characters will be copied into
 * this array. if this array is not big enough, a new one will be created and that one will
 * be used instead.
 * @return A new SubString (or the provided destination) representing part or all of the original
 * substring.
**/
//===================================================================
public SubString getCopy(SubString destination,char[] copyDataInto)
//===================================================================
{
	return substring(0,length,destination,copyDataInto);
}

//===================================================================
public void toUpperCase()
//===================================================================
{
	if (data == null) return;
	ewe.sys.Vm.getLocale().changeCase(data,start,length,true);
}
//===================================================================
public void toLowerCase()
//===================================================================
{
	if (data == null) return;
	ewe.sys.Vm.getLocale().changeCase(data,start,length,false);
}
//===================================================================
public boolean startsWith(String startWith)
//===================================================================
{
	if (data == null) return false;
	char [] sc = ewe.sys.Vm.getStringChars(startWith);
	int len = sc.length;
	if (len > length) return false;
	return ewe.sys.Vm.getLocale().compare(data,start,len,sc,0,len,0) == 0;
}
//===================================================================
public boolean endsWith(String endWith)
//===================================================================
{
	if (data == null) return false;
	char [] ec = ewe.sys.Vm.getStringChars(endWith);
	int len = ec.length;
	if (len > length) return false;
	return ewe.sys.Vm.getLocale().compare(data,start+length-len,len,ec,0,len,0) == 0;
}
/**
* Trim the SubString so there are no leading or trailing spaces or Tabs.
**/
//===================================================================
public void trim()
//===================================================================
{
	if (data == null) return;
	for (;length > 0;length--)
		if (data[start] != ' ' && data[start] != '\t')
			break;
		else
			start++;
	for (;length > 0;length--)
		if (data[start+length-1] != ' ' && data[start+length-1] != '\t')
			break;
}
//===================================================================
public void replace(char[] what,char[] with)
//===================================================================
{
	if (what.length == 0) return;
	int whatLen = what.length;
	int withLen = with.length;
	int dif = withLen-whatLen;
	int max = start+length-(whatLen-1);
	for (int s = start; s<max; s++){
		if (data[s] == what[0]){
			boolean found = true;
			if (whatLen != 1)
				for(int i = 1; i<whatLen; i++){
					if (data[s+i] != what[i]){
						found = false;
						break;
					}
				}
			if (!found) continue;
			//
			// Expand char array if necessary.
			//
			if (dif > 0 && start+length+dif > data.length){
				char [] newChar = new char[start+length+dif*20];
				ewe.sys.Vm.copyArray(data,start,newChar,start,length);
				data = newChar;
			}
			//
			// Move data.
			//
			if (dif != 0)
				ewe.sys.Vm.copyArray(data,s+whatLen,data,s+withLen,length-(s+whatLen-start));
			//
			// Copy first character.
			//
			if (withLen == 1) data[s] = with[0];
			//
			// Copy others.
			//
			else if (withLen > 1) ewe.sys.Vm.copyArray(with,0,data,s,withLen);
			//
			length += dif;
			max += dif;
			s--;
		}
	}
}
//##################################################################
}
//##################################################################
