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
package ewe.io;
import ewe.util.ByteArray;
/* Written using "Java Class Libraries", 2nd edition, ISBN 0-201-31002-3
 * "The Java Language Specification", ISBN 0-201-63451-1
 * Status:  Complete to version 1.1.
 */

/**
  * This class allows data to be written to a byte array buffer and
  * and then retrieved by an application.   The internal byte array
  * buffer is dynamically resized to hold all the data written.  Please
  * be aware that writing large amounts to data to this stream will
  * cause large amounts of memory to be allocated.
  * <p>
  * The size of the internal buffer defaults to 32 and it is resized
  * by doubling the size of the buffer.  This default size can be
  * overridden by using the
  * <code>gnu.java.io.ByteArrayOutputStream.initialBufferSize</code>
  * property.
  * <p>
  * There is a constructor that specified the initial buffer size and
  * that is the preferred way to set that value because it it portable
  * across all Java class library implementations.
  * <p>
  * Note that this class also has methods that convert the byte array
  * buffer to a <code>String</code> using either the system default or an
  * application specified character encoding.  Thus it can handle
  * multibyte character encodings.
  *
  * @author Aaron M. Renn (arenn@urbanophile.com)
  * @author Tom Tromey <tromey@cygnus.com>
  * @date September 24, 1998
  */
public class ByteArrayOutputStream extends OutputStream
{
  /**
   * This method initializes a new <code>ByteArrayOutputStream</code>
   * with the default buffer size of 32 bytes.  If a different initial
   * buffer size is desired, see the constructor
   * <code>ByteArrayOutputStream(int size)</code>.  For applications
   * where the source code is not available, the default buffer size
   * can be set using the system property
   * <code>gnu.java.io.ByteArrayOutputStream.initialBufferSize</code>
   */
  public ByteArrayOutputStream ()
  {
    this (initial_buffer_size);
  }

  /**
   * This method initializes a new <code>ByteArrayOutputStream</code> with
   * a specified initial buffer size.
   *
   * @param size The initial buffer size in bytes
   */
  public ByteArrayOutputStream (int size)
  {
    buf = new byte[size];
    count = 0;
  }

  /**
   * This method discards all of the bytes that have been written to
   * the internal buffer so far by setting the <code>count</code>
   * variable to 0.  The internal buffer remains at its currently
   * allocated size.
   */
  public synchronized void reset ()
  {
    count = 0;
  }

  /**
   * This method returns the number of bytes that have been written to
   * the buffer so far.  This is the same as the value of the protected
   * <code>count</code> variable.  If the <code>reset</code> method is
   * called, then this value is reset as well.  Note that this method does
   * not return the length of the internal buffer, but only the number
   * of bytes that have been written to it.
   *
   * @return The number of bytes in the internal buffer
   *
   * @see #reset()
   */
  public int size ()
  {
    return count;
  }

  /**
   * This method returns a byte array containing the bytes that have been
   * written to this stream so far.  This array is a copy of the valid
   * bytes in the internal buffer and its length is equal to the number of
   * valid bytes, not necessarily to the the length of the current
   * internal buffer.  Note that since this method allocates a new array,
   * it should be used with caution when the internal buffer is very large.
   */
  public synchronized byte[] toByteArray ()
  {
    byte[] ret = new byte[count];
    System.arraycopy(buf, 0, ret, 0, count);
    return ret;
  }
	/**
	 * Place the data into a ByteArray or return a new ByteArray containing the data.
	 * The internal data buffer used by this Stream is used as the data buffer in the
	 * destination ByteArray. Therefore no new array or copying of array is done with this
	 * method.
	 * @param dest A destination ByteArray or null to create a new ByteArray.
	 * @return The destination ByteArray or a new ByteArray.
	 */
	//===================================================================
	public ByteArray toByteArray(ByteArray dest)
	//===================================================================
	{
		if (dest == null) dest = new ByteArray();
		dest.data = buf;
		dest.length = count;
		return dest;
	}
  /**
   * Returns the bytes in the internal array as a <code>String</code>.  The
   * bytes in the buffer are converted to characters using the system default
   * encoding.  There is an overloaded <code>toString()</code> method that
   * allows an application specified character encoding to be used.
   *
   * @return A <code>String</code> containing the data written to this
   * stream so far
   */
  public String toString ()
  {
    return new String (buf, 0, count);
  }

  /**
   * Returns the bytes in the internal array as a <code>String</code>.  The
   * bytes in the buffer are converted to characters using the specified
   * encoding.
   *
   * @param enc The name of the character encoding to use
   *
   * @return A <code>String</code> containing the data written to this
   * stream so far
   *
   * @exception UnsupportedEncodingException If the named encoding is
   * not available
   */
  public String toString (String enc) throws UnsupportedEncodingException
  {
		return IO.newString(buf,0,count,enc);
  }

  /**
   * This method returns the bytes in the internal array as a
   * <code>String</code>.  It uses each byte in the array as the low
   * order eight bits of the Unicode character value and the passed in
   * parameter as the high eight bits.
   * <p>
   * This method does not convert bytes to characters in the proper way and
   * so is deprecated in favor of the other overloaded <code>toString</code>
   * methods which use a true character encoding.
   *
   * @param hibyte The high eight bits to use for each character in
   * the <code>String</code>
   *
   * @return A <code>String</code> containing the data written to this
   * stream so far
   *
   * @deprecated
   */
		/*
  public String toString (int hibyte)
  {
    return new String (buf, 0, count, hibyte);
  }
*/
  // Resize buffer to accommodate new bytes.
  private void resize (int add)
  {
    if (count + add >= buf.length)
      {
	int newlen = buf.length * 2;
	if (count + add > newlen)
	  newlen = count + add;
	byte[] newbuf = new byte[newlen];
	System.arraycopy(buf, 0, newbuf, 0, count);
	buf = newbuf;
      }
  }

  /**
   * This method writes the writes the specified byte into the internal
   * buffer.
   *
   * @param oneByte The byte to be read passed as an int
   */
  public synchronized void write (int oneByte)
  {
    resize (1);
    buf[count++] = (byte) oneByte;
  }

  /**
   * This method writes <code>len</code> bytes from the passed in array
   * <code>buf</code> starting at index <code>offset</code> into the
   * internal buffer.
   *
   * @param buffer The byte array to write data from
   * @param offset The index into the buffer to start writing data from
   * @param add The number of bytes to write
   */
  public synchronized void write (byte[] buffer, int offset, int add)
  {
    // If ADD < 0 then arraycopy will throw the appropriate error for
    // us.
    if (add >= 0)
      resize (add);
    System.arraycopy(buffer, offset, buf, count, add);
    count += add;
  }

  /**
   * This method writes all the bytes that have been written to this stream
   * from the internal buffer to the specified <code>OutputStream</code>.
   *
   * @param out The <code>OutputStream</code> to write to
   *
   * @exception IOException If an error occurs
   */
  public synchronized void writeTo (OutputStream out) throws IOException
  {
    out.write(buf, 0, count);
  }

  /**
   * The internal buffer where the data written is stored
   */
  protected byte[] buf;

  /**
   * The number of bytes that have been written to the buffer
   */
  protected int count;

  /**
   * The default initial buffer size.  Specified by the JCL.
   */
  private static final int DEFAULT_INITIAL_BUFFER_SIZE = 32;

  // The default buffer size which can be overridden by the user.
  private static final int initial_buffer_size = 32;
/*
  static
  {
    int r
      = Integer.getInteger ("gnu.java.io.ByteArrayOutputStream.initialBufferSize",
			    DEFAULT_INITIAL_BUFFER_SIZE).intValue ();
    if (r <= 0)
      r = DEFAULT_INITIAL_BUFFER_SIZE;
    initial_buffer_size = r;
  }
	*/
/*
//=================================================================
public static void main(String[] args) throws IOException
//=================================================================
{
	ewe.sys.Vm.startEwe(args);
	ewe.fx.Image im = new ewe.fx.Image("SmallAnimal.bmp");
	ByteArrayRandomStream rs = new ByteArrayRandomStream();
	new ewe.fx.PNGEncoder().writeImage(rs.toWritableStream(),im);
	//ByteArrayRandomStream rs = new ByteArrayRandomStream(os.toByteArray(null),"r");
	new ewesoft.apps.HexView(rs.toRandomAccessStream()).execute();
	ewe.fx.Image i2 = new ewe.fx.Image(rs.toByteArray(null),0);
	ewe.ui.Form f = new ewe.ui.Form();
	f.addLast(new ewe.ui.ImageControl(i2));
	f.execute();
	ewe.sys.Vm.exit(0);
}
*/
}
/*
package ewe.io;
import ewe.util.ByteArray;

//##################################################################
public class ByteArrayOutputStream extends OutputStream{
//##################################################################

//===================================================================
public ByteArrayOutputStream()
//===================================================================
{
	this(256);
}
//===================================================================
public ByteArrayOutputStream(int capacity)
//===================================================================
{
	super(new MemoryFile());
	if (capacity < 0) capacity = 256;
	((MemoryFile)stream).data.data = new byte[capacity];
}
//===================================================================
public byte [] toByteArray()
//===================================================================
{
	return ((MemoryFile)stream).data.toBytes();
}
//##################################################################
}
//##################################################################
*/

