/*
Copyright (c) 2001 Michael L Brereton  All rights reserved.

This software is furnished under the Gnu General Public License, Version 2, June 1991,
and may be used only in accordance with the terms of that license. This source code
must be distributed with a copy of this license. This software and documentation,
and its copyrights are owned by Michael L Brereton and are protected by copyright law.

If this notice is followed by a Wabasoft Copyright notice, then this software
is a modified version of the original as provided by Wabasoft. Wabasoft also
retains all rights as stipulated in the Gnu General Public License. These modifications
were made to the Version 1.0 source code release of Waba, throughout 2000 and up to May
2001.

THIS SOFTWARE AND REFERENCE MATERIALS ARE PROVIDED "AS IS" WITHOUT WARRANTY
AS TO THEIR PERFORMANCE, MERCHANTABILITY, FITNESS FOR ANY PARTICULAR PURPOSE,
OR AGAINST INFRINGEMENT. MICHAEL L BRERETON ASSUMES NO RESPONSIBILITY FOR THE USE OR
INABILITY TO USE THIS SOFTWARE. MICHAEL L BRERETON SHALL NOT BE LIABLE FOR INDIRECT,
SPECIAL OR CONSEQUENTIAL DAMAGES RESULTING FROM THE USE OF THIS PRODUCT.

MICHAEL L BRERETON SHALL HAVE NO LIABILITY OR RESPONSIBILITY FOR SOFTWARE ALTERED,
MODIFIED, OR CONVERTED BY YOU OR A THIRD PARTY, DAMAGES RESULTING FROM
ACCIDENT, ABUSE OR MISAPPLICATION, OR FOR PROBLEMS DUE TO THE MALFUNCTION OF
YOUR EQUIPMENT OR SOFTWARE NOT SUPPLIED BY MICHAEL L BRERETON.
*/

package ewe.io;

//##################################################################
public class DataInputStream extends FilterInputStream implements DataInput{
//##################################################################

/**
* Set this to false if you don't want a close of this DataInputStream to close
* the underlying Stream.
**/
public boolean closeUnderlying = true;

protected byte[] buffer = new byte[8];
protected ewe.sys.Lock lock = new ewe.sys.Lock();

//===================================================================
public DataInputStream(Stream s)
//===================================================================
{
	super(s);
}
//===================================================================
public DataInputStream(InputStream s)
//===================================================================
{
	super(s);
	if (s == null) throw new NullPointerException();
}
//
// All read methods eventually end up here.
//
//===================================================================
public int read(byte [] bytes,int offset,int length) throws IOException
//===================================================================
{
	lock.synchronize(); try{
		return super.read(bytes,offset,length);
	}finally{
		lock.release();
	}
}
//
// This is a Stream method.
//
//===================================================================
public int read() throws IOException
//===================================================================
{
	lock.synchronize(); try{
		int got = super.read(buffer,0,1);
		if (got == -1) return -1;
		return (int)buffer[0] & 0xff;
	}finally{lock.release();}
}
//
// This is a Stream method.
//
//===================================================================
public int read(byte [] bytes) throws IOException
//===================================================================
{
	return read(bytes,0,bytes.length);
}
//
// Used for DataInput reading.
//
//-------------------------------------------------------------------
protected void readAll(byte [] bytes,int offset,int length) throws IOException, EOFException
//-------------------------------------------------------------------
{
	if (length == 0) return;
	lock.synchronize(); try{
		while(length > 0){
			int got = read(bytes,offset,length);
			if (got < 0) throw new EOFException();
			offset += got;
			length -= got;
		}
	}finally{
		lock.release();
	}
}
//-------------------------------------------------------------------
protected int readAnInt(int numBytes) throws IOException
//-------------------------------------------------------------------
{
	lock.synchronize(); try{
		readAll(buffer,0,numBytes);
		return ewe.util.Utils.readInt(buffer,0,numBytes);
	}finally{
		lock.release();
	}
}
//-------------------------------------------------------------------
protected long readALong() throws IOException
//-------------------------------------------------------------------
{
	lock.synchronize(); try{
		readAll(buffer,0,8);
		return ewe.util.Utils.readLong(buffer,0);
	}finally{
		lock.release();
	}
}
//===================================================================
public byte readByte() throws IOException
//===================================================================
{
	return (byte)(readAnInt(1) & 0xff);
}
//===================================================================
public short readShort() throws IOException
//===================================================================
{
	return (short)(readAnInt(2) & 0xffff);
}
//===================================================================
public char readChar() throws IOException
//===================================================================
{
	return (char)(readAnInt(2) & 0xffff);
}
//===================================================================
public int readInt() throws IOException
//===================================================================
{
	return readAnInt(4);
}
//===================================================================
public long readLong() throws IOException
//===================================================================
{
	return readALong();
}
//===================================================================
public int readUnsignedByte() throws IOException
//===================================================================
{
	return ((int)readByte() & 0xff);
}
//===================================================================
public boolean readBoolean() throws IOException
//===================================================================
{
	return readByte() != 0;
}
//===================================================================
public float readFloat() throws IOException
//===================================================================
{
	return ewe.sys.Convert.toFloatBitwise(readInt());
}
//===================================================================
public double readDouble() throws IOException
//===================================================================
{
	return ewe.sys.Convert.toDoubleBitwise(readLong());
}
//===================================================================
public void readFully(byte [] data,int offset,int length) throws IOException
//===================================================================
{
	readAll(data,offset,length);
}
//===================================================================
public void readFully(byte [] data) throws IOException
//===================================================================
{
	readAll(data,0,data.length);
}
//===================================================================
public int readUnsignedShort() throws IOException
//===================================================================
{
	return ((int)readShort() & 0xffff);
}
	//-------------------------------------------------------------------
 static String convertFromUTF (byte[] buf)
	//-------------------------------------------------------------------
    throws EOFException, UTFDataFormatException
  {
    // Give StringBuffer an initial estimated size to avoid
    // enlarge buffer frequently
    StringBuffer strbuf = new StringBuffer (buf.length / 2 + 2);

    for (int i = 0; i < buf.length; )
      {
	if ((buf [i] & 0x80) == 0)		// bit pattern 0xxxxxxx
	  strbuf.append ((char) (buf [i++] & 0xFF));
	else if ((buf [i] & 0xE0) == 0xC0)	// bit pattern 110xxxxx
	  {
	    if (i + 1 >= buf.length
		|| (buf [i + 1] & 0xC0) != 0x80)
	      throw new UTFDataFormatException ();

	    strbuf.append((char) (((buf [i++] & 0x1F) << 6)
				  | (buf [i++] & 0x3F)));
	  }
	else if ((buf [i] & 0xF0) == 0xE0)	// bit pattern 1110xxxx
	  {
	    if (i + 2 >= buf.length
		|| (buf [i + 1] & 0xC0) != 0x80
		|| (buf [i + 2] & 0xC0) != 0x80)
	      throw new UTFDataFormatException ();

	    strbuf.append ((char) (((buf [i++] & 0x0F) << 12)
				   | ((buf [i++] & 0x3F) << 6)
				   | (buf [i++] & 0x3F)));
	  }
	else // must be ((buf [i] & 0xF0) == 0xF0 || (buf [i] & 0xC0) == 0x80)
	  throw new UTFDataFormatException ();	// bit patterns 1111xxxx or
						// 		10xxxxxx
      }

    return strbuf.toString ();
  }

//===================================================================
public String readUTF() throws IOException
//===================================================================
{
  final int UTFlen = readUnsignedShort ();
  byte[] buf = new byte [UTFlen];
  // This blocks until the entire string is available rather than
  // doing partial processing on the bytes that are available and then
  // blocking.  An advantage of the latter is that Exceptions
  // could be thrown earlier.  The former is a bit cleaner.
  readAll(buf, 0, UTFlen);
  return convertFromUTF (buf);
}
byte [] skipBuffer;
//===================================================================
public int skipBytes(int length) throws IOException
//===================================================================
{
	lock.synchronize(); try{
		int bufferSize = length;
		if (bufferSize > 1024) bufferSize = 1024;
		if (skipBuffer == null || bufferSize > skipBuffer.length) skipBuffer = new byte[bufferSize];
		int skipped = 0;
		while(length > 0){
			int toRead = length;
			if (toRead > skipBuffer.length) toRead = skipBuffer.length;
			int read = read(skipBuffer,0,toRead);
			if (read < 0) return skipped;
			skipped += read;
			length -= read;
		}
		return skipped;
	}finally{
		lock.release();
	}
}
//===================================================================
public void close() throws IOException
//===================================================================
{
	if (closeUnderlying) super.close();
	//return true;
}
//##################################################################
}
//##################################################################

