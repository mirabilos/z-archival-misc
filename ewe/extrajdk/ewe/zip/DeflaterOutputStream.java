/* java.util.zip.DeflaterOutputStream
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
import ewe.io.*;
//import java.io.FilterOutputStream;
//import java.io.OutputStream;
//import java.io.IOException;

/* Written using on-line Java Platform 1.2 API Specification
 * and JCL book.
 * Believed complete and correct.
 */

/**
 * This is a special FilterOutputStream deflating the bytes that are
 * written through it.  It uses the Deflater for deflating.
 *
 * A special thing to be noted is that flush() doesn't flush
 * everything in Sun's JDK, but it does so in jazzlib. This is because
 * Sun's Deflater doesn't have a way to flush() everything, without
 * finishing the stream.
 *
 * @author Tom Tromey, Jochen Hoenicke
 * @date Jan 11, 2001
 */
public class DeflaterOutputStream extends StreamObject implements ZipConstants
{
  /**
   * This buffer is used temporarily to retrieve the bytes from the
   * deflater and write them to the underlying output stream.
   */
  protected byte[] buf;

  /**
   * The deflater which is used to deflate the stream.
   */
  protected Deflater def;

  /**
   * Deflates everything in the def's input buffers.  This will call
   * <code>def.deflate()</code> until all bytes from the input buffers
   * are processed.
   */
  protected void deflate ()// throws IOException
  {
    while (! def.needsInput () && ! def.finished ())
      {
	int len = def.deflate (buf, 0, buf.length);

	//	System.err.println ("DOS deflated " + len + " out of " + buf.length);
	if (len <= 0)
	  break;
	output.writeBytes(buf, 0, len);
	outputBytes += len;
      }

    if (! def.needsInput () && ! def.finished ())
      throw new InternalError ("Can't deflate all input?");
  }

Stream output;
/**
* The number of bytes written to the deflater.
**/
public int inputBytes;
/**
* The number of bytes writeen out by the deflater.
**/
public int outputBytes;
//===================================================================
public DeflaterOutputStream(ewe.io.BasicStream output)
//===================================================================
{
	this(output,Z_DEFAULT_COMPRESSION);
}
//===================================================================
public DeflaterOutputStream(ewe.io.BasicStream output,int level)
//===================================================================
{
	this(output,level,false);
}
//===================================================================
public DeflaterOutputStream(ewe.io.BasicStream output,int level,boolean noWrap)
//===================================================================
{
	def = new Deflater(level,noWrap);
	this.output = (output instanceof Stream) ? (Stream)output : new StreamAdapter(output);
	buf = new byte[1024];
}
//===================================================================
public DeflaterOutputStream(ewe.io.OutputStream output)
//===================================================================
{
	this(new ewe.io.StreamAdapter(output));
}
//===================================================================
public DeflaterOutputStream(ewe.io.OutputStream output,int level,boolean noWrap)
//===================================================================
{
	this(new ewe.io.StreamAdapter(output),level,noWrap);
}
/**
* Don't call this after starting to write output!
**/
//===================================================================
public void setBufferSize(int size)
//===================================================================
{
	if (size <= 0) size = 1024;
	buf = new byte[size];
}

/**
 * Flushes the stream by calling flush() on the deflater and then
 * on the underlying stream.  This ensures that all bytes are
 * flushed.  This function doesn't work in Sun's JDK, but only in
 * jazzlib.
 */

  public boolean flushStream() throws ewe.io.IOException
  {
    def.flush ();
    deflate ();
		return true;
  }

  /**
   * Finishes the stream by calling finish() on the deflater.  This
   * was the only way to ensure that all bytes are flushed in Sun's
   * JDK.
   */
 // public
	private
	void finish ()// throws IOException
  {
    def.finish ();
    deflate ();
    //out.flush ();
  }

  /**
   * Calls finish () and closes the stream.
   */
  public boolean closeStream () throws ewe.io.IOException
  {
    finish();
		return output.close();
    //out.close();
  }

  /**
   * Writes a single byte to the compressed output stream.
   * @param bval the byte value.
   */
		/*
  public void write (int bval) throws IOException
  {
    byte[] b = new byte[1];
    b[0] = (byte) bval;
    write (b, 0, 1);
  }
*/
  /**
   * Writes a len bytes from an array to the compressed stream.
   * @param buf the byte array.
   * @param off the offset into the byte array where to start.
   * @param len the number of bytes to write.
   */
  public int nonBlockingWrite(byte[] buf, int off, int len)// throws IOException
  {
    //    System.err.println("DOS with off " + off + " and len " + len);
    def.setInput (buf, off, len);
    deflate ();
		inputBytes += len;
		return len;
  }

}
