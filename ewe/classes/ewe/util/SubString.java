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

This is a very specialized Utility class used for bulk processing of
text strings, while minimizing overheads of new Object creation.<p>
A SubString consists of a reference to a character array
(which may be shared by other SubStrings), a start index within the array and a
length value for the sub-string.<p>
You can manipulate a SubString in many of the same ways as you do a String, but
unlike a String, a SubString is mutable (since you have full access to its data
and its start and length values).

**/
//##################################################################
public class SubString implements ewe.util.Comparable{
//##################################################################
/* Do not move these, they are used by native methods.*/
public char [] data = null;
public int start = 0;
public int length = 0;
/*.....................................................*/
/**
* Return a String representation of this SubString.
**/
//===================================================================
public String toString()
//===================================================================
{
	if (data == null) return null;
	return new String(data,start,length);
}


/**
 * Return if this SubString is null (i.e. it's data is null).
 */
//===================================================================
public boolean isNull() {return data == null;}
//===================================================================
/**
* Get the length of the SubString.
**/
//===================================================================
public int length() {return length;}
//===================================================================
/**
* Return the character at a particular index.
**/
//===================================================================
public char charAt(int idx) {return data[idx+start];}
//===================================================================
/**
* Copy all the characters of this substring into a character array.
* @param dest The destination character array.
* @param offset The offset in the destination to start copying into.
*/
//===================================================================
public void copyInto(char[] dest,int offset)
//===================================================================
{
	copyInto(dest,offset,length,0);
}
/**
 * Copy a subset of this SubStrings data into a character array.
* @param dest The destination character array.
* @param offset The offset in the destination to start copying into.
 * @param len The number of characters to copy.
 * @param startPoint The first index in this substring to copy from.
 */
//===================================================================
public void copyInto(char[] dest,int offset,int len,int startPoint)
//===================================================================
{
	if (data == null) return;
	ewe.sys.Vm.copyArray(data,startPoint+start,dest,offset,len);
}
/**
* Copy all the characters of this substring into a byte array.
* @param dest The destination byte array.
* @param offset The offset in the destination to start copying into.
* @param ascii if this is true then only the low byte is copied into the destination.
* If you want to do Utf8 encoding, then see the encode method.
*/
//===================================================================
public void copyInto(byte[] dest,int offset,boolean ascii)
//===================================================================
{
	copyInto(dest,offset,length,0,ascii);
}
/**
 * Copy a subset of this SubStrings data into a byte array.
* @param dest The destination byte array.
* @param offset The offset in the destination to start copying into.
 * @param len The number of characters to copy.
 * @param startPoint The first index in this substring to copy from.
* @param ascii if this is true then only the low byte is copied into the destination.
* If you want to do Utf8 encoding, then see the encode method.
*/
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
/**
* Create a SubString based on a substring of the text in the provided String.
* A copy of the String data is made for this SubString.
**/
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
/**
Get a new SubString(). Objects which override SubString can return a different
object.
**/
//===================================================================
public SubString getNew()
//===================================================================
{
	return new SubString();
}
/**
* Create a new SubString - use one of the set() methods to setup the
* text for the SubString.
**/
//===================================================================
public SubString(){}
//===================================================================
/**
* This will split this substring and place the results as a series of
* SubString objects in the destination list. If the destination contains
* SubString objects already, then each will be used in sequence as a destination. If there
* are not enough SubStrings in the destination then more will be added. If
* there are too many SubStrings extra ones will NOT be removed. The return value is the number
* of substrings found.
* @param separator The separator character. A separator of ' ' (a space) will
* also use the TAB ('\t') as a separator. A space separator also has the property
* that a sequence of more than one space or TAB characters will be considered as just one
* separator. So a line like "hello    there how    are you" will be separated into the five
* words exactly.
* @param destination This is the destination vector (which must NOT be null) in which SubString
* objects representing each separated text is placed.
* @return The number of found SubStrings in the text.
*/
//===================================================================
public int split(char separator,Vector destination)
//===================================================================
{
	return split(separator,destination,0);
}
/**
* This will split this substring and place the results as a series of
* SubString objects in the destination list. If the destination contains
* SubString objects already, then each will be used in sequence as a destination. If there
* are not enough SubStrings in the destination then more will be added. If
* there are too many SubStrings extra ones will NOT be removed. The return value is the number
* of substrings found.
* @param separator The separator character. A separator of ' ' (a space) will
* also use the TAB ('\t') as a separator. A space separator also has the property
* that a sequence of more than one space or TAB characters will be considered as just one
* separator. So a line like "hello    there how    are you" will be separated into the five
* words exactly.
* @param destination This is the destination vector (which must NOT be null) in which SubString
* objects representing each separated text is placed.
* @param destinationStart. This tells which index in the destination Vector to start placing
* SubString values into.
* @return The number of found SubStrings in the text.
*/
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
			while(i<end && (data[i] == ' ' || data[i] == '\t')) i++;
			if (i>=end) break;
			int st = i;
			while(i<end && (data[i] != ' ' && data[i] != '\t')) i++;
			SubString d;
			if (found+destinationStart >= destination.size()) {
				d = getNew();
				destination.add(d);
			}else
				d = (SubString)destination.get(found+destinationStart);
			d.set(data,st,i-st);
			found++;
		}
	}else{
		for (int i = start;;){
			int st = i;
			while(i<end && data[i] != separator) i++;
			SubString d;
			if (found+destinationStart >= destination.size()) {
				d = getNew();
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


/**
 * Split a String into smaller sub Strings placed in an output Vector.
 * @param what The String to split.
 * @param separator The separator character.
 * @param dest A destination Vector (which may be null).
 * @return A Vector containing the exact number of Strings found in the String.
 */
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

/**
* An option for equals().
**/
public static final int IGNORE_CASE = 0x1;
/**
* An option for equals().
**/
public static final int STARTS_WITH = 0x2;
/**
* An option for equals().
**/
public static final int BACKWARDS = 0x4; //Sometimes a backwards check is faster.

/**
* This compares the character sequence given by the "small" parameters
* to the character sequence given by the "big" parameters.
**/
//===================================================================
public static native boolean equals(
	char [] big,int bigStart,int bigLength,
	char [] small,int smallStart,int smallLength,
	int options);
//===================================================================
/**
* Find the index of a character within an array of characters, looking either backwards or
* forwards.
* @param what The character to look for.
* @param str The array of characters to look in.
* @param strStart The start of the source characters in the array.
* @param strLength The number of source characters in the array.
* @param start The location in str to start looking from (which should be >= strStart and < strStart+strLength).
* This character is always included in the search whether going backwards or forwards.
* @param options This can be 0 or BACKWARDS to start searching backwards.
* @return The index of the character in the string, or -1 if it is not found.
*/
//===================================================================
public static native int indexOf(char what,char[] str,int strStart,int strLength,int start,int options);
//===================================================================
/**
* This does a numeric char for char comparison.
**/
//===================================================================
public static native int compare(char [] one,int oneStart,int oneLength,char [] two,int twoStart,int twoLength);
//===================================================================
/**
* Compare this SubString with either another SubString or a String.
* @param other Either another SubString or a String.
* @return greater than 1 if this SubString is considered greater than the Object,
* less than 1 if this SubString is considered less than the Object,  and 0 if this
* SubString is considered equal to the Object.
**/
//===================================================================
public int compareTo(Object other)
//===================================================================
{
	return compareTo(other,0);
}
/**
* Compare this SubString with either another SubString or a String.
* @param other Either another SubString or a String.
* @param options This can be IGNORE_CASE or 0.
* @return greater than 1 if this SubString is considered greater than the Object,
* less than 1 if this SubString is considered less than the Object,  and 0 if this
* SubString is considered equal to the Object.
*/
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
/**
* Compare this SubString with either another SubString or a String.
* @param other Either another SubString or a String.
* @return true if the text represented by this SubString and the other are equal.
*/
//===================================================================
public boolean equals(Object other)
//===================================================================
{
	return compareTo(other,0) == 0;
}
/**
* As specified in the hashCode() contract, this returns the same hashCode that a String
* with the same text would return.
**/
//===================================================================
public int hashCode()
//===================================================================
{
	if (data == null) return 0;
	return ewe.util.Utils.makeHashCode(data,start,length);
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
	if (destination == null) destination = getNew();
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

/*
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
*/

/**
* Check if the SubString startsWith a particular String.
**/
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
/**
* Check if the SubString endsWith a particular String.
**/
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
			//s--;
			// This bypasses the new characters added.
			s += withLen-1;
		}
	}
}
//##################################################################
}
//##################################################################


