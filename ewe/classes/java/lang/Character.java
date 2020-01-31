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
package java.lang;

/**
* Under Ewe the Character class is ONLY used for certain character operation and tests, and for
* storing/retrieving a character value
**/
//##################################################################
public class Character{
//##################################################################

private char theCharacter;

//===================================================================
public Character(char value)
//===================================================================
{
	theCharacter = value;
}
/**
 * Return the character this object represents.
 * @return
 */
//===================================================================
public char charValue()
//===================================================================
{return theCharacter;}

//===================================================================
public boolean equals(Object other)
//===================================================================
{
	if (other instanceof Character) return theCharacter == ((Character)other).theCharacter;
	return super.equals(other);
}

//===================================================================
public int hashCode()
//===================================================================
{
	return theCharacter;
}

//-------------------------------------------------------------------
private static native int charOperation(char ch,int testOrConversion);
//-------------------------------------------------------------------
public static boolean isUpperCase(char ch)
{
	return charOperation(ch,1) != 0;
}
public static boolean isLowerCase(char ch)
{
	return charOperation(ch,2) != 0;
}
public static boolean isSpaceChar(char ch)
{
	return charOperation(ch,3) != 0;
}
public static boolean isWhitespace(char ch)
{
	return charOperation(ch,4) != 0;
}
public static boolean isDigit(char ch)
{
	return charOperation(ch,5) != 0;
}
public static boolean isLetter(char ch)
{
	return charOperation(ch,6) != 0;
}
public static boolean isLetterOrDigit(char ch)
{
	return charOperation(ch,7) != 0;
}
public static boolean isTitleCase(char ch)
{
	return charOperation(ch,8) != 0;
}
public static char toUpperCase(char ch)
{
	return (char)(charOperation(ch,101) & 0xffff);
}
public static char toLowerCase(char ch)
{
	return (char)(charOperation(ch,102) & 0xffff);
}
public static char toTitleCase(char ch)
{
	return (char)(charOperation(ch,103) & 0xffff);
}
  /**
   * Smallest value allowed for radix arguments in Java. This value is 2.
   *
   * @see #digit(char, int)
   * @see #forDigit(int, int)
   * @see Integer#toString(int, int)
   * @see Integer#valueOf(String)
   */
  public static final int MIN_RADIX = 2;

  /**
   * Largest value allowed for radix arguments in Java. This value is 36.
   *
   * @see #digit(char, int)
   * @see #forDigit(int, int)
   * @see Integer#toString(int, int)
   * @see Integer#valueOf(String)
   */
  public static final int MAX_RADIX = 36;

  /**
   * The minimum value the char data type can hold.
   * This value is <code>'\\u0000'</code>.
   */
  public static final char MIN_VALUE = '\u0000';

  /**
   * The maximum value the char data type can hold.
   * This value is <code>'\\uFFFF'</code>.
   */
  public static final char MAX_VALUE = '\uFFFF';

  /**
   * Class object representing the primitive char data type.
   *
   * @since 1.1
   */
  public static final Class TYPE =Class.forPrimitive('C');

  /**
   * Converts the wrapped character into a String.
   *
   * @return a String containing one character -- the wrapped character
   *         of this instance
   */
  public String toString()
  {
    // Package constructor avoids an array copy.
    return new String(new char[] { theCharacter }, 0, 1);//, true);
  }

  /**
   * Returns a String of length 1 representing the specified character.
   *
   * @param ch the character to convert
   * @return a String containing the character
   * @since 1.4
   */
  public static String toString(char ch)
  {
    // Package constructor avoids an array copy.
    return new String(new char[] { ch }, 0, 1);//, true);
  }

	public static char forDigit(int digit, int radix)
  {
    if (radix < MIN_RADIX || radix > MAX_RADIX
        || digit < 0 || digit >= radix)
      return '\0';
    return Number.digits[digit];
  }

   public static int digit(char ch, int radix)
  {
    if (radix < MIN_RADIX || radix > MAX_RADIX)
      return -1;
		if (ch >= '0' && ch <= '9') return ch-'0';
		if (ch >= 'a' && ch <= 'z') return 10+ch-'a';
		if (ch >= 'A' && ch <= 'Z') return 10+ch-'A';
    return -1;
  }
//##################################################################
}
//##################################################################

