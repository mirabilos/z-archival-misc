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

//##################################################################
public class Long extends ewe.data.DataObject implements Value{
//##################################################################
public long value;

public static final Class TYPE = java.lang.Long.TYPE;
public static final Long l1 = new Long();
public static final Long l2 = new Long();
public static final Long l3 = new Long();

public Long set(long value) {this.value = value; return this;}
public Long set(Long value) {return set(value == null ? 0 : value.value);}

public static final int ZERO_FILL = 0x1;
public static final int TRUNCATE = 0x2;
public static final int HEX = 0x4;

//===================================================================
public void copyFrom(Object other)
//===================================================================
{
	if (other instanceof Long)
		set((Long)other);
}
//===================================================================
//public String toString() {return toString(0,0);}
public String toString() {return ewe.sys.Convert.toString(value);}//toString(0,0);}
//===================================================================

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
//-------------------------------------------------------------------
private String cantFit(int length)
//-------------------------------------------------------------------
{
	StringBuffer sb = new StringBuffer();
	for (int i = 0; i<length; i++) sb.append('#');
	return sb.toString();
}

char hex[] = {'0','1','2','3','4','5','6','7','8','9','A','B','C','D','E','F'};
//-------------------------------------------------------------------
static int lengthOf(long value)
//-------------------------------------------------------------------
{
	if (value < 0) value = -value;
	int len = 1;
	if (value != 0){
		long cur = value;
		while(true){
			cur /= 10;
			if (cur == 0) break;
			len++;
		}
	}
	return len;
}
//============================================================
public String toString(int totalLength,int options)
//============================================================
{
	long val = value;
	boolean neg = val<0;
	long pre;
	int frontLength = 1,digits = 0;
	int needLength = 0;
	String ret = "";
	long cur;
	long div = 10;
	boolean zeroFill = (options & ZERO_FILL) != 0;
	if ((options & HEX) != 0){
		long mask = 0xf000000000000000l;
		digits = 0;
		char padChar = zeroFill ? '0' : ' ';
		for (digits = 16; digits >0; digits--){
			if ((mask & val) != 0) break;
			mask >>= 4;
		}
		if (digits == 0) digits = 1;
		if (digits > totalLength && totalLength > 0) return cantFit(totalLength);
		if (totalLength <= 0) totalLength = digits;
		for (int i = 0; i<totalLength; i++){
			ret = (char)(digits > 0 ? hex[(int)(val & 0xf)]:padChar)+ret;
			val >>= 4;
			digits--;
		}
		return ret;
	}
	if (neg) val *= -1;
	pre = val;
	if (pre != 0){
		cur = pre;
		while(true){
			cur -= cur%div;
			if (cur == 0) break;
			div *= 10;
			frontLength++;
		}
	}
	digits = frontLength;
	if (neg) frontLength++;
	needLength = frontLength;
	//if (decimal > 0) needLength += decimal+1;
	if (needLength > totalLength && totalLength > 0) return cantFit(totalLength);

	if (totalLength > 0){
		int padLength = totalLength-frontLength;
		char padChar = zeroFill ? '0':' ';
		int i = 0;
		if (zeroFill && neg) ret += '-';
		for (i = 0; i<padLength; i++) ret += padChar;
		if (!zeroFill && neg) ret += '-';
	}else{
		if (neg) ret += '-';
	}
	for (cur = pre, div /= 10;div > 0; div /= 10){
		int dig = (int)(cur/div);
		cur -= dig*div;
		ret += (char)('0'+dig);
	}
	return ret;
}
//===================================================================
public void fromString(String str)
//===================================================================
{
	value = 0;
	try{
		java.text.NumberFormat nm = java.text.NumberFormat.getNumberInstance();
		nm.setParseIntegerOnly(true);
		value = nm.parse(str).longValue();
	}catch(Exception e){}
}

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
//##################################################################
}
//##################################################################
