/* java.util.zip.InflaterInputStream
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
//import java.io.FilterInputStream;
//import java.io.InputStream;
//import java.io.IOException;

/**
 * This filter stream is used to decompress data compressed in the "deflate"
 * format. The "deflate" format is described in RFC 1951.
 *
 * This stream may form the basis for other decompression filters, such
 * as the <code>GZIPInputStream</code>.
 *
 * @author John Leuner
 * @since JDK 1.1
 */

public class InflaterInputStream extends StreamObject {//extends FilterInputStream {

  //Variables

  /**
   * Decompressor for this filter
   */

  protected Inflater inf;

  /**
   * Byte array used as a buffer
   */

  protected byte[] buf;

  /**
   * Size of buffer
   */

  protected int len;


  //We just use this if we are decoding one byte at a time with the read() call

  private byte[] onebytebuffer = new byte[1];

	protected BasicStream in;

  //Constructors


  /**
   * Create an InflaterInputStream with the default decompresseor
   * and a default buffer size.
   *
   * @param in the InputStream to read bytes from
   */
	//===================================================================
  public InflaterInputStream(BasicStream in)
	//===================================================================
  {
    this(in, new Inflater(false));
  }
	//===================================================================
	public InflaterInputStream(BasicStream in,boolean noWrap)
	//===================================================================
	{
		this(in,new Inflater(noWrap));
	}
	//===================================================================
public InflaterInputStream(ewe.io.InputStream input,boolean noWrap)
//===================================================================
{
	this(new ewe.io.StreamAdapter(input),noWrap);
}
//===================================================================
public InflaterInputStream(ewe.io.InputStream input)
//===================================================================
{
	this(input,false);
}

	//===================================================================
	public void setBufferSize(int size)
	//===================================================================
	{
		if (size <= 0) size = 1024;
		buf = new byte[size];
	}

  /**
   * Create an InflaterInputStream with the specified decompresseor
   * and a default buffer size.
   *
   * @param in the InputStream to read bytes from
   * @param inf the decompressor used to decompress data read from in
   */

	//-------------------------------------------------------------------
 protected InflaterInputStream(BasicStream in, Inflater inf)
	//-------------------------------------------------------------------
  {
    this(in, inf, 4096);
  }

  /**
   * Create an InflaterInputStream with the specified decompresseor
   * and a specified buffer size.
   *
   * @param in the InputStream to read bytes from
   * @param inf the decompressor used to decompress data read from in
   * @param size size of the buffer to use
   */

	//-------------------------------------------------------------------
  protected InflaterInputStream(BasicStream in, Inflater inf, int size)
	//-------------------------------------------------------------------
  {
    //super (in);
		this.in = in;
    this.inf = inf;
    this.len = 0;

    if (size <= 0) return;
      //throw new IllegalArgumentException("size <= 0");
    buf = new byte[size]; //Create the buffer
  }

  //Methods

  /**
   * Returns 0 once the end of the stream (EOF) has been reached.
   * Otherwise returns 1.
   */

  public int available() //throws IOException
  {
    return inf.finished() ? 0 : 1;
  }

  /**
   * Closes the input stream
   */
  public boolean closeStream() throws ewe.io.IOException
  {
    return in.closeStream();
  }
	public boolean flushStream() throws ewe.io.IOException
	{
		return true;
	}
	public boolean isOpen() {return in.isOpen();}
  /**
   * Fills the buffer with more data to decompress.
   */
  protected int fill() //throws IOException
  {
    len = in.nonBlockingRead(buf, 0, buf.length);
    if (len <= 0) return len;
      //throw new ZipException("Deflated stream ends early.");
    inf.setInput(buf, 0, len);
		return len;
  }
	public int nonBlockingWrite(byte [] buff,int start,int count) {return -2;}
  /**
   * Reads one byte of decompressed data.
   *
   * The byte is in the lower 8 bits of the int.
   */
  public int read()// throws IOException
  {
    int nread = nonBlockingRead(onebytebuffer, 0, 1); //read one byte
    if(nread > 0)
      return onebytebuffer[0] & 0xff;
    return -1;
  }

  /**
   * Decompresses data into the byte array
   *
   *
   * @param b the array to read and decompress data into
   * @param off the offset indicating where the data should be placed
   * @param len the number of bytes to decompress
   */
	boolean finished;
	//===================================================================
  public int nonBlockingRead(byte[] b, int off, int len) //throws IOException
	//===================================================================
  {
		if (finished) return READWRITE_CLOSED;
    for (;;)
      {
	int count;
	//try
	//  {
	    count = inf.inflate(b, off, len);
	//  }
	//catch(DataFormatException dfe)
	//  {
	//    throw new ZipException(dfe.getMessage());
	//  }
	if (count > 0)
	  return count;
	if (count == -1) return -2;
	if (inf.needsDictionary()) return -2;
	  //throw new ZipException("Need a dictionary");
	if (inf.finished()){
		finished = true;
		byte [] remaining = inf.getRemainingBytes();
		if (in instanceof ewe.io.BufferedStream && remaining.length != 0){
			//ewe.sys.Vm.debug("Pushing back: "+remaining.length);
			//ewe.sys.Vm.debug("First byte: "+(int)remaining[0]);
			((ewe.io.BufferedStream)in).pushback(remaining,0,remaining.length);
		}
	  return -1;
	}else if (inf.needsInput()){
			int got = fill();
			if (got <= 0) return got;
			return 0;
	}else
		return -2;
	 // throw new InternalError("Don't know what to do");
      }
  }

  /**
   * Skip specified number of bytes of uncompressed data
   *
   * @param n number of bytes to skip
   */
  byte[] tmp = new byte[0];
  public int skip(int n)// throws IOException
  {
    if (n <= 0) return -1;
      //throw new IllegalArgumentException();
    int len = 2048;
    if (n < len) len = n;
		if (tmp.length < len) tmp = new byte[len];
    return nonBlockingRead(tmp,0,len);
  }
}
