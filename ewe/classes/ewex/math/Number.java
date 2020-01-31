/* Number.java =- abstract superclass of numeric objects
   Copyright (C) 1998, 2001, 2002 Free Software Foundation, Inc.

This file is part of GNU Classpath.

GNU Classpath is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; either version 2, or (at your option)
any later version.

GNU Classpath is distributed in the hope that it will be useful, but
WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
General Public License for more details.

You should have received a copy of the GNU General Public License
along with GNU Classpath; see the file COPYING.  If not, write to the
Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA
02111-1307 USA.

Linking this library statically or dynamically with other modules is
making a combined work based on this library.  Thus, the terms and
conditions of the GNU General Public License cover the whole
combination.

As a special exception, the copyright holders of this library give you
permission to link this library with independent modules to produce an
executable, regardless of the license terms of these independent
modules, and to copy and distribute the resulting executable under
terms of your choice, provided that you also meet, for each linked
independent module, the terms and conditions of the license of that
module.  An independent module is a module which is not derived from
or based on this library.  If you modify this library, you may extend
this exception to your version of the library, but you are not
obligated to do so.  If you do not wish to do so, delete this
exception statement from your version. */


package ewex.math;
import ewe.sys.Convert;
//import java.io.Serializable;

/**
 * Number is a generic superclass of all the numeric classes, including
 * the wrapper classes {@link Byte}, {@link Short}, {@link Integer},
 * {@link Long}, {@link Float}, and {@link Double}.  Also worth mentioning
 * are the classes in {@link java.math}.
 *
 * It provides ways to convert numeric objects to any primitive.
 *
 * @author Paul Fisher
 * @author John Keiser
 * @author Warren Levy
 * @author Eric Blake <ebb9@email.byu.edu>
 * @since 1.0
 * @status updated to 1.4
 */
public abstract class Number //implements Serializable
{
  /**
   * Compatible with JDK 1.1+.
   */
  //private static final long serialVersionUID = -8742448824652078965L;

  /**
   * Table for calculating digits, used in Character, Long, and Integer.
   */
  static final char[] digits = {
    '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
    'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j',
    'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't',
    'u', 'v', 'w', 'x', 'y', 'z'
  };

  /**
   * The basic constructor (often called implicitly).
   */
  public Number()
  {
  }

  /**
   * Return the value of this <code>Number</code> as an <code>int</code>.
   *
   * @return the int value
   */
  public abstract int intValue();

  /**
   * Return the value of this <code>Number</code> as a <code>long</code>.
   *
   * @return the long value
   */
  public abstract long longValue();

  /**
   * Return the value of this <code>Number</code> as a <code>float</code>.
   *
   * @return the float value
   */
  public abstract float floatValue();

  /**
   * Return the value of this <code>Number</code> as a <code>double</code>.
   *
   * @return the double value
   */
  public abstract double doubleValue();

  /**
   * Return the value of this <code>Number</code> as a <code>byte</code>.
   *
   * @return the byte value
   */
  public byte byteValue()
  {
    return (byte) intValue();
  }

  /**
   * Return the value of this <code>Number</code> as a <code>short</code>.
   *
   * @return the short value
   */
  public short shortValue()
  {
    return (short) intValue();
  }

	public final static double NEGATIVE_INFINITY = new ewe.sys.Double().setSpecial(ewe.sys.Double.NEGATIVE_INFINITY).value;
	public final static double POSITIVE_INFINITY = new ewe.sys.Double().setSpecial(ewe.sys.Double.POSITIVE_INFINITY).value;
	public final static double DOUBLE_MAX_VALUE = ewe.sys.Convert.toDoubleBitwise(0x7fefffffffffffffL);
	public final static double DOUBLE_MIN_VALUE = ewe.sys.Convert.toDoubleBitwise(0x1L);
	public final static int INTEGER_MAX_VALUE =  2147483647;
	public final static int INTEGER_MIN_VALUE = -2147483648;
	public final static long LONG_MAX_VALUE =  0x7fffffffffffffffL;
	public final static long LONG_MIN_VALUE = 0x8000000000000000L;

	public static int digit(char ch,int radix)
	{
		int ret = 0;
		if (ch >= '0' && ch <= '9') ret = ch-'0';
		else if (ch >= 'a' && ch <= 'z') ret = ch-'a'+10;
		else if (ch >= 'A' && ch <= 'Z') ret = ch-'A'+10;
		else return -1;
		if (ret >= radix) return -1;
		return ret;
	}

	public static char forDigit(int digit,int radix)
	{
		if (digit > radix) return '?';
		if (digit < 10) return (char)('0'+digit);
		else return (char)('a'+(digit-10));
	}

	private static final ewe.sys.Double doubleBuffer = new ewe.sys.Double();

	public static boolean isInfinite(double value)
	{
		return doubleBuffer.set(value).is(doubleBuffer.INFINITY);
	}
	public static boolean isNaN(double value)
	{
		return doubleBuffer.set(value).is(doubleBuffer.NAN);
	}

	static StringBuffer sb;

	//===================================================================
	public static String integerToString(int value, int radix)
	//===================================================================
	{
		if (value == 0) return "0";
		if (radix == 10) return Convert.toString(value);
		return longToString((long)value,radix);
	}
	//===================================================================
	public static String longToString(long value, int radix)
	//===================================================================
	{
		if (value == 0) return "0";
		if (radix == 10) return Convert.toString(value);
		boolean neg = value < 0;
		if (neg) value = -value;
		if (sb == null) sb = new StringBuffer();
		sb.setLength(0);
		for(;value != 0;value /= radix)
			sb.append(forDigit((int)(value%radix),radix));
		if (neg) sb.append('-');
		return sb.reverse().toString();
	}
	  /**
   * Helper for parsing longs.
   *
   * @param str the string to parse
   * @param radix the radix to use, must be 10 if decode is true
   * @param decode if called from decode
   * @return the parsed long value
   * @throws NumberFormatException if there is an error
   * @throws NullPointerException if decode is true and str is null
   * @see #parseLong(String, int)
   * @see #decode(String)
   */
  private static long parseLong(String str, int radix, boolean decode)
  {
    if (! decode && str == null)
      throw new NumberFormatException();
    int index = 0;
    int len = str.length();
    boolean isNeg = false;
    if (len == 0)
      throw new NumberFormatException();
    int ch = str.charAt(index);
    if (ch == '-')
      {
        if (len == 1)
          throw new NumberFormatException();
        isNeg = true;
        ch = str.charAt(++index);
      }
    if (decode)
      {
        if (ch == '0')
          {
            if (++index == len)
              return 0;
            if ((str.charAt(index) & ~('x' ^ 'X')) == 'X')
              {
                radix = 16;
                index++;
              }
            else
              radix = 8;
          }
        else if (ch == '#')
          {
            radix = 16;
            index++;
          }
      }
    if (index == len)
      throw new NumberFormatException();

    long max = LONG_MAX_VALUE / radix;
    // We can't directly write `max = (MAX_VALUE + 1) / radix'.
    // So instead we fake it.
    if (isNeg && LONG_MAX_VALUE % radix == radix - 1)
      ++max;

    long val = 0;
    while (index < len)
      {
	if (val < 0 || val > max)
	  throw new NumberFormatException();

        ch = digit(str.charAt(index++), radix);
        val = val * radix + ch;
        if (ch < 0 || (val < 0 && (! isNeg || val != LONG_MIN_VALUE)))
          throw new NumberFormatException();
      }
    return isNeg ? -val : val;
  }

	public static long parseLong(String str, int radix)
  {
    return parseLong(str, radix, false);
  }
	public static int parseInt(String str, int radix)
	{
		return (int)parseLong(str,radix,false);
	}

}