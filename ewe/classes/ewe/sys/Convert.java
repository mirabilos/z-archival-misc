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
package ewe.sys;

/**
 * Convert is used to convert between objects and basic types.
 */

public class Convert
{
protected Convert()
	{
	}

/**
* Convert a string of characters to a int value returning zero if
* the format is bad. See parseInt() for a better conversion method.
**/
public static int toInt(String s)
{
	return (int)toLong(Vm.getStringChars(s),0,s.length());
}
/**
* Convert a string of characters to a float value returning zero if
* the format is bad. See parseDouble() for a better conversion method.
**/
public static float toFloat(String s)
{
	return (float)toDouble(Vm.getStringChars(s),0,s.length());
}
/**
* Convert a string of characters to a double value returning zero if
* the format is bad. See parseDouble() for a better conversion method.
**/
public static double toDouble(String s)
{
	return toDouble(Vm.getStringChars(s),0,s.length());
}
/**
* Convert a string of characters to long value returning zero if
* the format is bad. See parseLong() for a better conversion method.
**/
public static long toLong(String s)
{
	return toLong(Vm.getStringChars(s),0,s.length());
}
/**
* Convert a string of characters to an int value returning zero if
* the format is bad. See parseInt() for a better conversion method.
**/
public static int toInt(char [] chars,int start,int length)
{
	return (int)toLong(chars,start,length);
}
/**
* Convert a string of characters to a double value returning zero if
* the format is bad. See parseDouble() for a better conversion method.
**/
public static float toFloat(char [] chars,int start,int length)
{
	return (float)toDouble(chars,start,length);
}
/**
* Convert a string of characters to a double value returning zero if
* the format is bad. See parseDouble() for a better conversion method.
**/
public native static double toDouble(char [] chars,int start,int length);
/**
* Convert a string of characters to a long value returning zero if
* the format is bad. See parseLong() for a better conversion method.
**/
public native static long toLong(char [] chars,int start,int length);

/** Converts the given boolean to a String. */
public native static String toString(boolean b);


/** Converts the given char to a String. */
public native static String toString(char c);
/**
* Get the 32-bit bitwise equivalent of the float value.
 * @param f the float value.
 * @param rawBits true if you want the raw bits, false to convert all NaN values
 * into a single standard NaN value.
 */
public native static int toIntBitwise(float f,boolean rawBits);
/**
* Get the raw 32-bit bitwise equivalent of the float value.
**/
public static int toIntBitwise(float f)
{
	return toIntBitwise(f,true);
}
/** Converts the given IEEE 754 bit representation of a float to a float. */
public native static float toFloatBitwise(int i);
/**
* Get the 64-bit bitwise equivalent of the double value.
 * @param d the double value.
 * @param rawBits true if you want the raw bits, false to convert all NaN values
 * into a single standard NaN value.
 */
public native static long toLongBitwise(double d, boolean rawBits);
/**
* Get the raw 64-bit bitwise equivalent of the double value.
**/
public static long toLongBitwise(double d)
{
	return toLongBitwise(d,true);
}
/**
* Convert the 64-bit bitwise equivalent of a double value back to the double value.
**/
public native static double toDoubleBitwise(long l);


/** Converts the given float to a String. */
public static String toString(float f) {return formatDouble((double)f);}
/** Converts the given int to a String. */
public static String toString(int i) {return formatInt(i);}
/** Converts the given double to a String. */
public static String toString(double d) {return formatDouble(d);}
/** Converts the given long to a String. */
public static String toString(long l) {return formatLong(l);}

public static boolean toBoolean(String s)
{
	char c = toChar(s);
	return c == 't' || c == 'T' || c == 'Y' || c == 'y';
}

public static char toChar(String s)
{
	return (s == null ? (char)0: s.length() == 0 ? (char)0: s.charAt(0));
}
/**
 * Convert a String to an integer using the specified radix. The String must have only
 * decimal digits or letters (for radix > 10) and must start with a '-' if it is negative.
 * @param str The characters to parse.
 * @param offset The index of the first character to parse.
 * @param length The number of characters to parse.
 * @param radix The radix ( >1 and < 37 )
 * @return the parsed integer.
 * @exception NumberFormatException if the String value is not a parsable integer.
 * @exception IllegalArgumentException if the radix is not in the correct range.
 */
//===================================================================
public native static int parseInt(char[] str, int offset, int length, int radix) throws NumberFormatException;
//===================================================================
/**
 * Convert a String to an integer using the specified radix. The String must have only
 * decimal digits or letters (for radix > 10) and must start with a '-' if it is negative.
 * @param str The characters to parse.
 * @param offset The index of the first character to parse.
 * @param length The number of characters to parse.
 * @param radix The radix ( >1 and < 37 )
 * @return the parsed integer.
 * @exception NumberFormatException if the String value is not a parsable integer.
 * @exception IllegalArgumentException if the radix is not in the correct range.
 */
//===================================================================
public native static long parseLong(char[] str, int offset, int length, int radix) throws NumberFormatException;
//===================================================================
/**
 * Convert a String to an integer. The String must have only
 * decimal digits and must start with a '-' if it is negative.
 * @param str The characters to parse.
 * @return the parsed integer.
 * @exception NumberFormatException if the String value is not a parsable integer.
 */
//===================================================================
public static int parseInt(String str) throws NumberFormatException
//===================================================================
{
	return parseInt(ewe.sys.Vm.getStringChars(str),0,str.length(),10);
}
/**
 * Convert a String to an integer using the specified radix. The String must have only
 * decimal digits or letters (for radix > 10) and must start with a '-' if it is negative.
 * @param str The characters to parse.
 * @param radix The radix ( >1 and < 37 )
 * @return the parsed integer.
 * @exception NumberFormatException if the String value is not a parsable integer.
 * @exception IllegalArgumentException if the radix is not in the correct range.
 */
//===================================================================
public static int parseInt(String str, int radix)
//===================================================================
{
	return parseInt(ewe.sys.Vm.getStringChars(str),0,str.length(),radix);
}
/**
 * Convert a String to a long value. The String must have only
 * decimal digits and must start with a '-' if it is negative.
 * @param str The characters to parse.
 * @return the parsed integer.
 * @exception NumberFormatException if the String value is not a parsable integer.
 */
//===================================================================
public static long parseLong(String str) throws NumberFormatException
//===================================================================
{
	return parseLong(ewe.sys.Vm.getStringChars(str),0,str.length(),10);
}
/**
 * Convert a String to a long value using the specified radix. The String must have only
 * decimal digits or letters (for radix > 10) and must start with a '-' if it is negative.
 * @param str The characters to parse.
 * @param radix The radix ( >1 and < 37 )
 * @return the parsed integer.
 * @exception NumberFormatException if the String value is not a parsable integer.
 * @exception IllegalArgumentException if the radix is not in the correct range.
 */
//===================================================================
public static long parseLong(String str, int radix)
//===================================================================
{
	return parseLong(ewe.sys.Vm.getStringChars(str),0,str.length(),radix);
}
/**
 * Parse a double precision floating point decimal number.
 * @param chars The characters to parse.
 * @param offset The index of the first character.
 * @param length The number of characters to parse.
 * @return the double value of the string.
 * @exception NumberFormatException if the string is not parsable.
 */
//===================================================================
public static native double parseDouble(char[] chars, int offset, int length) throws NumberFormatException;
//===================================================================

/**
 * Parse a double precision floating point decimal number.
 * @param value The characters to parse.
 * @return the double value of the string.
 * @exception NumberFormatException if the string is not parsable.
 */
//===================================================================
public static double parseDouble(String value) throws NumberFormatException
//===================================================================
{
	return parseDouble(ewe.sys.Vm.getStringChars(value),0,value.length());
}
/**
* This is an option for formatInt and formatLong. It specifies that the number should
* be formatted as unsigned. This is only valid for radixes of 2, 4, 8 and 16.
**/
public static final int FORMAT_UNSIGNED = 0x1;
/**
* This is an option for formatXXX. It specifies that any non-digit characters
* should be in upper case. This is generally only useful in non-decimal radixes. For
* formatDouble() this specifies that any 'e' character should be in uppercase.
**/
public static final int FORMAT_UPPERCASE = 0x2;
/**
 * Convert an integer into a String of characters or calculate the number of characters needed
 * to convert the integer into the String of characters. To calculate the number of characters
 * needed use a null "dest" parameter.
 * @param value the value to convert.
 * @param dest the destination character array. If this is null the
 * @param offset the start index in the character array.
 * @param radix the radix (>1 and <37).
 * @param options a combination of the FORMAT_XXX options.
 * @exception IllegalArgumentException if the radix is not in the correct range.
 * @return the number of characters used or needed to format the value.
 */
//===================================================================
public static native int formatInt(int value, char[] dest, int offset, int radix, int options);
//===================================================================
/**
 * Convert an integer into a String of characters or calculate the number of characters needed
 * to convert the integer into the String of characters. To calculate the number of characters
 * needed use a null "dest" parameter.
 * @param value the value to convert.
 * @param dest the destination character array. If this is null the
 * @param offset the start index in the character array.
 * @return the number of characters used or needed to format the value.
 */
//===================================================================
public static int formatInt(int value, char [] dest, int offset)
//===================================================================
{
	return formatInt(value,dest,offset,10,0);
}
/**
 * Convert an integer into a String of characters.
 * @param value the value to convert.
 * @param radix the radix (>1 and <37).
 * @return a new String with the converted integer value.
 * @exception IllegalArgumentException if the radix is not in the correct range.
 */
//===================================================================
public static String formatInt(int value,int radix)
//===================================================================
{
	char [] ch = new char[formatInt(value,null,0,radix,0)];
	formatInt(value,ch,0,radix,0);
	return Vm.createStringWithChars(ch);
}
/**
 * Convert an integer into a String of characters.
 * @param value the value to convert.
 * @return a new String with the converted integer value.
 */
//===================================================================
public static String formatInt(int value)
//===================================================================
{
	return formatInt(value,10);
}
/**
 * Convert a long value into a String of characters or calculate the number of characters needed
 * to convert the long into the String of characters. To calculate the number of characters
 * needed use a null "dest" parameter.
 * @param value the value to convert.
 * @param dest the destination character array. If this is null the
 * @param offset the start index in the character array.
 * @param radix the radix (>1 and <37).
 * @param options a combination of the FORMAT_XXX options.
 * @exception IllegalArgumentException if the radix is not in the correct range.
 * @return the number of characters used or needed to format the value.
 */
//===================================================================
public static native int formatLong(long value, char[] dest, int offset, int radix, int options);
//===================================================================
/**
 * Convert a long value into a String of characters or calculate the number of characters needed
 * to convert the integer into the String of characters. To calculate the number of characters
 * needed use a null "dest" parameter.
 * @param value the value to convert.
 * @param dest the destination character array. If this is null the
 * @param offset the start index in the character array.
 * @return the number of characters used or needed to format the value.
 */
//===================================================================
public static int formatLong(long value, char [] dest, int offset)
//===================================================================
{
	return formatLong(value,dest,offset,10,0);
}
/**
 * Convert a long value into a String of characters.
 * @param value the value to convert.
 * @param radix the radix (>1 and <37).
 * @return a new String with the converted integer value.
 * @exception IllegalArgumentException if the radix is not in the correct range.
 */
//===================================================================
public static String formatLong(long value,int radix)
//===================================================================
{
	char [] ch = new char[formatLong(value,null,0,radix,0)];
	formatLong(value,ch,0,radix,0);
	return Vm.createStringWithChars(ch);
}
/**
 * Convert a long value into a String of characters.
 * @param value the value to convert.
 * @return a new String with the converted integer value.
 */
//===================================================================
public static String formatLong(long value)
//===================================================================
{
	return formatLong(value,10);
}
/**
 * Convert a double value into a String of characters or calculate the number of characters needed
 * to convert the long into the String of characters. To calculate the number of characters
 * needed use a null "dest" parameter.
 * @param value the value to convert.
 * @param dest the destination character array. If this is null the
 * @param offset the start index in the character array.
 * @param options a combination of the FORMAT_XXX options.
 * @return the number of characters used or needed to format the value.
 */
//===================================================================
public static native int formatDouble(double value, char[] dest, int offset, int options);
//===================================================================
/**
 * Convert a double value into a String of characters or calculate the number of characters needed
 * to convert the integer into the String of characters. To calculate the number of characters
 * needed use a null "dest" parameter.
 * @param value the value to convert.
 * @param dest the destination character array. If this is null the
 * @param offset the start index in the character array.
 * @return the number of characters used or needed to format the value.
 */
//===================================================================
public static int formatDouble(double value, char [] dest, int offset)
//===================================================================
{
	return formatDouble(value,dest,offset,0);
}
/**
 * Convert a double value into a String of characters.
 * @param value the value to convert.
 * @param radix the radix (>1 and <37).
 * @return a new String with the converted integer value.
 * @exception IllegalArgumentException if the radix is not in the correct range.
 */
//===================================================================
public static String formatDouble(double value)
//===================================================================
{
	char [] ch = new char[formatDouble(value,null,0,0)];
	formatDouble(value,ch,0,0);
	return Vm.createStringWithChars(ch);
}
/**
* Convert the value to an unsigned hex string.
**/
//===================================================================
public static String intToHexString(int value)
//===================================================================
{
	char [] ch = new char[formatInt(value,null,0,16,FORMAT_UNSIGNED)];
	formatInt(value,ch,0,16,FORMAT_UNSIGNED);
	return Vm.createStringWithChars(ch);
}
/**
* Convert the value to an unsigned hex string.
**/
//===================================================================
public static String longToHexString(long value)
//===================================================================
{
	char [] ch = new char[formatLong(value,null,0,16,FORMAT_UNSIGNED)];
	formatLong(value,ch,0,16,FORMAT_UNSIGNED);
	return Vm.createStringWithChars(ch);
}

}


