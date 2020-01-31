/* Time.java -- Wrapper around java.util.Date
   Copyright (C) 1999, 2000, 2002, 2003 Free Software Foundation, Inc.

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


package ewex.sql;

/**
 * This class is a wrapper around java.util.Date to allow the JDBC
 * driver to identify the value as a SQL Time.
 *
 * @author Aaron M. Renn (arenn@urbanophile.com)
 */
public class Time extends ewe.sys.TimeOfDay
{
	{
		format = "HH:mm:ss";
	}
  /**
   * This method always throws an IllegalArgumentException.
   *
   * @throws IllegalArgumentException when it's called.
   * @deprecated
   */
  public int getDate() throws IllegalArgumentException
  {
    throw new IllegalArgumentException();
  }
  /**
   * This method always throws an IllegalArgumentException.
   *
   * @throws IllegalArgumentException when it's called.
   * @deprecated
   */
  public int getDay() throws IllegalArgumentException
  {
    throw new IllegalArgumentException();
  }
  /**
   * This method always throws an IllegalArgumentException.
   *
   * @throws IllegalArgumentException when it's called.
   * @deprecated
   */
  public int getMonth() throws IllegalArgumentException
  {
    throw new IllegalArgumentException();
  }
  /**
   * This method always throws an IllegalArgumentException.
   *
   * @throws IllegalArgumentException when it's called.
   * @deprecated
   */
  public int getYear() throws IllegalArgumentException
  {
    throw new IllegalArgumentException();
  }
  /**
   * This method always throws an IllegalArgumentException.
   *
   * @throws IllegalArgumentException when it's called.
   * @deprecated
   */
  public void setDate(int newValue) throws IllegalArgumentException
  {
    throw new IllegalArgumentException();
  }
  /**
   * This method always throws an IllegalArgumentException.
   *
   * @throws IllegalArgumentException when it's called.
   * @deprecated
   */
  public void setMonth(int newValue) throws IllegalArgumentException
  {
    throw new IllegalArgumentException();
  }
  /**
   * This method always throws an IllegalArgumentException.
   *
   * @throws IllegalArgumentException when it's called.
   * @deprecated
   */
  public void setYear(int newValue) throws IllegalArgumentException
  {
    throw new IllegalArgumentException();
  }
  /**
   * This method returns a new instance of this class by parsing a
   * date in JDBC format into a Java date.
   *
   * @param str The string to parse.
   * @return The resulting <code>java.sql.Time</code> value.
   *
   * @deprecated
   */
  public static Time valueOf (String str)
  {
		Time t = new Time();
		t.fromString(str);
		return t;
	}
	//-------------------------------------------------------------------
	public Time(){}
	//-------------------------------------------------------------------
	  /**
    * This method initializes a new instance of this class with the
    * specified year, month, and day.
    *
    * @param hour The hour for this Time (0-23)
    * @param minute The minute for this time (0-59)
    * @param second The second for this time (0-59)
    * @deprecated
    */
  public Time(int hour, int minute, int second)
  {
		super(hour,minute,second);
  }

  /**
   * This method initializes a new instance of this class with the
   * specified time value representing the number of seconds since
   * Jan 1, 1970 at 12:00 midnight GMT.
   *
   * @param time The time value to intialize this <code>Time</code> to.
   */
  public Time(long date)
  {
		setTime(date);
  }
}

