/* java.util.zip.ZipEntry
   Copyright (C) 2001 Free Software Foundation, Inc.

This file is part of Jazzlib.

Jazzlib is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; either version 2, or (at your option)
any later version.

Jazzlib is distributed in the hope that it will be useful, but
WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
General Public License for more details.

You should have received a copy of the GNU General Public License
along with GNU Classpath; see the file COPYING.  If not, write to the
Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA
02111-1307 USA.

As a special exception, if you link this library with other files to
produce an executable, this library does not by itself cause the
resulting executable to be covered by the GNU General Public License.
This exception does not however invalidate any other reasons why the
executable file might be covered by the GNU General Public License. */

package ewe.zip;
import ewe.sys.Time;
//import java.util.Calendar;
//import java.util.TimeZone;
//import java.util.Date;

/**
 * This class represents a member of a zip archive.  ZipFile and
 * ZipInputStream will give you instances of this class as information
 * about the members in an archive.  On the other hand ZipOutputStream
 * needs an instance of this class to create a new member.
 *
 * @author Jochen Hoenicke
 */
public class ZipEntry extends ewe.data.LiveTreeNode// implements Cloneable
{
  private static int KNOWN_SIZE   = 1;
  private static int KNOWN_CSIZE  = 2;
  private static int KNOWN_CRC    = 4;
  private static int KNOWN_TIME   = 8;

 /**
   * Compression method.  This method doesn't compress at all.
   */
  public final static int STORED      =  0;
  /**
   * Compression method.  This method uses the Deflater.
   */
  public final static int DEFLATED    =  8;
  //private static Calendar cal = Calendar.getInstance();

	private Time cal = new Time();
  private String name;
  private int size;
  private int compressedSize;
  private long crc;
  private long time;
  private short known = 0;
  private short method = -1;
  private byte[] extra = null;
  private String comment = null;

	boolean isDir = false;
	ZipEntry linkTo = null;

  int zipFileIndex = -1;  /* used by ZipFile */
  int flags;              /* used by ZipOutputStream */
  int offset;             /* used by ZipFile and ZipOutputStream */

  /**
   * Creates a zip entry with the given name.
   * @param name the name. May include directory components separated
   * by '/'.
   */
  public ZipEntry(String name)
  {
    //if (name == null)
      //throw new NullPointerException();
    this.name = name;
  }

  /**
   * Creates a copy of the given zip entry.
   * @param e the entry to copy.
   */
  public ZipEntry(ZipEntry e)
  {
    name = e.name;
    known = e.known;
    size = e.size;
    compressedSize = e.compressedSize;
    crc = e.crc;
    time = e.time;
    method = e.method;
    extra = e.extra;
    comment = e.comment;
  }

  void setDOSTime(int dostime)
  {
		cal.millis = 0;
    cal.second = 2 * (dostime & 0x1f);
    cal.minute = (dostime >> 5) & 0x3f;
    cal.hour = (dostime >> 11) & 0x1f;
    cal.day = (dostime >> 16) & 0x1f;
    cal.month = ((dostime >> 21) & 0xf);
    cal.year = ((dostime >> 25) & 0x7f) + 1980; /* since 1900 */

    // Guard against invalid or missing date causing
    // IndexOutOfBoundsException.
//    try
//      {
//	synchronized(cal)
//	  {
	    //cal.set(year, mon, day, hrs, min, sec);
			time = cal.getTime()/1000;
//	  }
	known |= KNOWN_TIME;
//      }
//    catch (RuntimeException ex)
//      {
	/* Ignore illegal time stamp */
//	known &= ~KNOWN_TIME;
//      }
  }

  int getDOSTime()
  {
    if ((known & KNOWN_TIME) == 0)
      return 0;
//    synchronized(cal)
//      {
			cal.setTime(time*1000);
	return (cal.year - 1980 & 0x7f) << 25
	  | ((cal.month)) << 21
	  | (cal.day) << 16
	  | (cal.hour) << 11
	  | (cal.minute) << 5
	  | (cal.second) >> 1;
     // }
  }

  /**
   * Creates a copy of this zip entry.
   */
  /**
   * Clones the entry.
   */
		/*
  public Object clone()
  {
    try
      {
	return super.clone();
      }
    catch (CloneNotSupportedException ex)
      {
	throw new InternalError();
      }
  }
*/
  /**
   * Returns the entry name.  The path components in the entry are
   * always separated by slashes ('/').
   */
  public String getName()
  {
    return name;
  }

  /**
   * Sets the time of last modification of the entry.
   * @time the time of last modification of the entry.
   */
  public void setTime(Time t)
  {
		if (t == null) return;
		time = t.getTime()/1000;
    this.known |= KNOWN_TIME;
  }

  /**
   * Gets the time of last modification of the entry.
   * @return the time of last modification of the entry, or -1 if unknown.
   */
  public Time getTime(Time t)
  {
		if ((known & KNOWN_TIME) == 0) return null;
		if (t == null) t = new Time();
		t.setTime(time*1000);
		return t;
  }

  /**
   * Sets the size of the uncompressed data.
   * @exception IllegalArgumentException if size is not in 0..0xffffffffL
   */
  public void setSize(int size)
  {
  //  if ((size & 0xffffffff00000000L) != 0)
	//throw new IllegalArgumentException();
    this.size = size;
    this.known |= KNOWN_SIZE;
  }

  /**
   * Gets the size of the uncompressed data.
   * @return the size or -1 if unknown.
   */
  public int getSize()
  {
    return (known & KNOWN_SIZE) != 0 ? size : -1;
  }

  /**
   * Sets the size of the compressed data.
   * @exception IllegalArgumentException if size is not in 0..0xffffffffL
   */
  public void setCompressedSize(int csize)
  {
  //  if ((csize & 0xffffffff00000000L) != 0)
	//throw new IllegalArgumentException();
    this.compressedSize = csize;
    this.known |= KNOWN_CSIZE;
  }

  /**
   * Gets the size of the compressed data.
   * @return the size or -1 if unknown.
   */
  public int getCompressedSize()
  {
    return (known & KNOWN_CSIZE) != 0 ? compressedSize : -1;
  }

  /**
   * Sets the crc of the uncompressed data.
   * @exception IllegalArgumentException if crc is not in 0..0xffffffffL
   */
  public void setCrc(int crc)
  {
    //if ((crc & 0xffffffff00000000L) != 0)
	//throw new IllegalArgumentException();
    this.crc = crc & 0x00000000ffffffffL;
    this.known |= KNOWN_CRC;
  }

  /**
   * Gets the crc of the uncompressed data.
   * @return the crc (lower 32 bits) or -1 if unknown.
   */
  public long getCrc()
  {
    return (known & KNOWN_CRC) != 0 ? crc & 0x00000000ffffffffL  : -1L;
  }

  /**
   * Sets the compression method.  Only DEFLATED and STORED are
   * supported.
   * @exception IllegalArgumentException if method is not supported.
   * @see ZipOutputStream#DEFLATED
   * @see ZipOutputStream#STORED
   */
  public boolean setMethod(int method)
  {
    if (method != ZipOutputStream.STORED
	&& method != ZipOutputStream.DEFLATED)
	return false;//throw new IllegalArgumentException();
    this.method = (short) method;
		return true;
  }

  /**
   * Gets the compression method.
   * @return the compression method or -1 if unknown.
   */
  public int getMethod()
  {
    return method;
  }

  /**
   * Sets the extra data.
   */
  public void setExtra(byte[] extra)
  {
    if (extra.length > 0xffff) return;
   //   throw new IllegalArgumentException();
    this.extra = extra;
  //  try
    //  {
	int pos = 0;
	while (pos < extra.length)
	  {
	    int sig = (extra[pos++] & 0xff)
	      | (extra[pos++] & 0xff) << 8;
	    int len = (extra[pos++] & 0xff)
	      | (extra[pos++] & 0xff) << 8;
	    if (sig == 0x5455)
	      {
		/* extended time stamp */
		int flags = extra[pos];
		if ((flags & 1) != 0)
		  {
		    time = ((extra[pos+1] & 0xff)
			    | (extra[pos+2] & 0xff) << 8
			    | (extra[pos+3] & 0xff) << 16
			    | (extra[pos+4] & 0xff) << 24);
		    known |= KNOWN_TIME;
		  }
	      }
	    pos += len;
	  }
  //    }
    //catch (ArrayIndexOutOfBoundsException ex)
      //{
	//throw new IllegalArgumentException("Malformed extra field");
    //  }
  }

  /**
   * Gets the extra data.
   * @return the extra data or null if not set.
   */
  public byte[] getExtra()
  {
    return extra;
  }

  /**
   * Sets the entry comment.
   * @exception IllegalArgumentException if comment is longer than 0xffff.
   */
  public void setComment(String comment)
  {
    if (comment.length() > 0xffff) return;
      //throw new IllegalArgumentException();
    this.comment = comment;
  }

  /**
   * Gets the comment.
   * @return the comment or null if not set.
   */
  public String getComment()
  {
    return comment;
  }

  /**
   * Gets true, if the entry is a directory.  This is solely
   * determined by the name, a trailing slash '/' marks a directory.
   */
  public boolean isDirectory()
  {
    int nlen = name.length();
    return nlen > 0 && name.charAt(nlen - 1) == '/';
  }

  /**
   * Gets the string representation of this ZipEntry.  This is just
   * the name as returned by getName().
   */
  public String toString()
  {
    return name;
  }

	public int compareTo(Object other)
	{
		if (!(other instanceof ZipEntry)) return 1;
		return ewe.sys.Vm.getLocale().compare(getName(),((ZipEntry)other).getName(),0);
	}


	public ewe.util.SubString myFileName;

  /**
   * Gets the hashCode of this ZipEntry.  This is just the hashCode
   * of the name.  Note that the equals method isn't changed, though.
   */
		/*
  public int hashCode()
  {
    return name.hashCode();
  }
	*/
}


