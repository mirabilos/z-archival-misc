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
public class DataOutputStream extends FilterOutputStream implements DataOutput{
//##################################################################
/**
* Set this to false if you don't want a close of this DataOutputStream to close
* the underlying Stream.
**/
public boolean closeUnderlying = true;
/**
* The number of bytes written to the Stream.
**/
protected int written = 0;

protected byte[] buffer = new byte[8];
protected ewe.sys.Lock lock = new ewe.sys.Lock();

/**
* Returns the number of bytes written to the Stream.
**/
//===================================================================
public int size()
//===================================================================
{
	return written;
}

//===================================================================
public DataOutputStream(Stream s)
//===================================================================
{
	super(s);
	if (s == null) throw new NullPointerException();
}

//===================================================================
public DataOutputStream(OutputStream s)
//===================================================================
{
	super(s);
	if (s == null) throw new NullPointerException();
}
//===================================================================
public void close() throws IOException
//===================================================================
{
	if (closeUnderlying) super.close();
}
//
// All write methods eventually end up here.
//
//===================================================================
public void write(byte [] bytes,int offset,int length) throws IOException
//===================================================================
{
	lock.synchronize(); try{
		super.write(bytes,offset,length);
		written += length;
	}finally{
		lock.release();
	}
}
//===================================================================
public void write(int aByte) throws IOException
//===================================================================
{
	lock.synchronize(); try{
		buffer[0] = (byte)(aByte & 0xff);
		write(buffer,0,1);
	}finally{
		lock.release();
	}
}
//===================================================================
public void write(byte [] bytes) throws IOException
//===================================================================
{
	write(bytes,0,bytes.length);
}
//-------------------------------------------------------------------
protected void writeIntBytes(int value,int numBytes) throws IOException
//-------------------------------------------------------------------
{
	lock.synchronize(); try{
		ewe.util.Utils.writeInt(value,buffer,0,numBytes);
		write(buffer,0,numBytes);
	}finally{ lock.release();}
}

//===================================================================
public void writeBytes(String text) throws IOException
//===================================================================
{
	write(ewe.util.mString.toAscii(text));
}
//===================================================================
public void writeByte (int value) throws IOException
//===================================================================
{
  write(value & 0xff);
}
//===================================================================
public void writeChar(int aCharacter) throws IOException
//===================================================================
{
	writeIntBytes(aCharacter,2);
}
//===================================================================
public void writeShort(int value) throws IOException
//===================================================================
{
	writeIntBytes(value,2);
}
//===================================================================
public void writeInt(int value) throws IOException
//===================================================================
{
	writeIntBytes(value,4);
}
 /**
   * This method writes a Java boolean value to an output stream.  If
   * <code>value</code> is <code>true</code>, a byte with the value of
   * 1 will be written, otherwise a byte with the value of 0 will be
   * written.
   *
   * The value written can be read using the <code>readBoolean</code>
   * method in <code>DataInput</code>.
   *
   * @param value The <code>boolean</code> value to write to the stream
   *
   * @exception IOException If an error occurs
   *
   * @see DataInput#readBoolean
   */
  public final void writeBoolean (boolean value) throws IOException
  {
    write(value ? 1 : 0);
  }

  /**
   * This method writes a Java byte value to an output stream.  The
   * byte to be written will be in the lowest 8 bits of the
   * <code>int</code> value passed.
   *
   * The value written can be read using the <code>readByte</code> or
   * <code>readUnsignedByte</code> methods in <code>DataInput</code>.
   *
   * @param value The <code>byte</code> to write to the stream, passed as
   * the low eight bits of an <code>int</code>.
   *
   * @exception IOException If an error occurs
   *
   * @see DataInput#readByte
   * @see DataInput#readUnsignedByte
   */
 /**
   * This method writes a Java long value to an output stream.  The 8 bytes
   * of the passed value will be written "big endian".  That is, with
   * the high byte written first in the following manner:
   * <p>
   * <code>byte0 = (byte)((value & 0xFF00000000000000L) >> 56);<br>
   * byte1 = (byte)((value & 0x00FF000000000000L) >> 48);<br>
   * byte2 = (byte)((value & 0x0000FF0000000000L) >> 40);<br>
   * byte3 = (byte)((value & 0x000000FF00000000L) >> 32);<br>
   * byte4 = (byte)((value & 0x00000000FF000000L) >> 24);<br>
   * byte5 = (byte)((value & 0x0000000000FF0000L) >> 16);<br>
   * byte6 = (byte)((value & 0x000000000000FF00L) >> 8);<br>
   * byte7 = (byte)(value & 0x00000000000000FFL);</code>
   * <p>
   *
   * The value written can be read using the <code>readLong</code>
   * method in <code>DataInput</code>.
   *
   * @param value The <code>long</code> value to write to the stream
   *
   * @exception IOException If an error occurs
   *
   * @see DataInput#readLong
   */
  public void writeLong (long value) throws IOException
  {
		lock.synchronize(); try{
			ewe.util.Utils.writeLong(value,buffer,0);
			write(buffer,0,8);
		}finally{ lock.release();}
		/*
    write ((byte) (0xff & (value >> 56)));
    write ((byte) (0xff & (value>> 48)));
    write ((byte) (0xff & (value>> 40)));
    write ((byte) (0xff & (value>> 32)));
    write ((byte) (0xff & (value>> 24)));
    write ((byte) (0xff & (value>> 16)));
    write ((byte) (0xff & (value>>  8)));
    write ((byte) (0xff & value));
		*/
  }

  /**
   * This method writes a Java <code>float</code> value to the stream.  This
   * value is written by first calling the method
   * <code>Float.floatToIntBits</code>
   * to retrieve an <code>int</code> representing the floating point number,
   * then writing this <code>int</code> value to the stream exactly the same
   * as the <code>writeInt()</code> method does.
   *
   * The value written can be read using the <code>readFloat</code>
   * method in <code>DataInput</code>.
   *
   * @param value The <code>float</code> value to write to the stream
   *
   * @exception IOException If an error occurs
   *
   * @see writeInt
   * @see DataInput#readFloat
   * @see Float#floatToIntBits
   */
  public final void writeFloat (float value) throws IOException
  {
    writeInt (ewe.sys.Convert.toIntBitwise(value));
  }
  /**
   * This method writes a Java <code>double</code> value to the stream.  This
   * value is written by first calling the method
   * <code>Double.doubleToLongBits</code>
   * to retrieve an <code>long</code> representing the floating point number,
   * then writing this <code>long</code> value to the stream exactly the same
   * as the <code>writeLong()</code> method does.
   *
   * The value written can be read using the <code>readDouble</code>
   * method in <code>DataInput</code>.
   *
   * @param value The <code>double</code> value to write to the stream
   *
   * @exception IOException If an error occurs
   *
   * @see writeLong
   * @see DataInput#readDouble
   * @see Double#doubleToLongBits
   */
  public void writeDouble (double value) throws IOException
  {
    writeLong (ewe.sys.Convert.toLongBitwise(value));
  }
  /**
   * This method writes all the characters of a <code>String</code> to an
   * output stream as an array of <code>char</code>'s. Each character
   * is written using the method specified in the <code>writeChar</code>
   * method.
   *
   * @param value The <code>String</code> to write to the stream
   *
   * @exception IOException If an error occurs
   *
   * @see writeChar
   */
  public void writeChars (String value) throws IOException
  {
		write(ewe.util.mString.toBytes(value));
  }
  /**
   * This method writes a Java <code>String</code> to the stream in a modified
   * UTF-8 format.  First, two bytes are written to the stream indicating the
   * number of bytes to follow.  Note that this is the number of bytes in the
   * encoded <code>String</code> not the <code>String</code> length.  Next
   * come the encoded characters.  Each character in the <code>String</code>
   * is encoded as either one, two or three bytes.  For characters in the
   * range of <code>\u0001</code> to <\u007F>, one byte is used.  The character
   * value goes into bits 0-7 and bit eight is 0.  For characters in the range
   * of <code>\u0080</code> to <code>\u007FF</code>, two bytes are used.  Bits
   * 6-10 of the character value are encoded bits 0-4 of the first byte, with
   * the high bytes having a value of "110".  Bits 0-5 of the character value
   * are stored in bits 0-5 of the second byte, with the high bits set to
   * "10".  This type of encoding is also done for the null character
   * <code>\u0000</code>.  This eliminates any C style NUL character values
   * in the output.  All remaining characters are stored as three bytes.
   * Bits 12-15 of the character value are stored in bits 0-3 of the first
   * byte.  The high bits of the first bytes are set to "1110".  Bits 6-11
   * of the character value are stored in bits 0-5 of the second byte.  The
   * high bits of the second byte are set to "10".  And bits 0-5 of the
   * character value are stored in bits 0-5 of byte three, with the high bits
   * of that byte set to "10".
   *
   * The value written can be read using the <code>readUTF</code>
   * method in <code>DataInput</code>.
   *
   * @param value The <code>String</code> to write to the output in UTF format
   *
   * @exception IOException If an error occurs
   *
   * @see DataInput#readUTF
   */
  public final void writeUTF (String value) throws IOException
  {
		lock.synchronize(); try{
	    int len = value.length();
	    int sum = 0;

	    for (int i = 0; i < len && sum <= 65535; ++i)
	      {
		char c = value.charAt(i);
		if (c >= '\u0001' && c <= '\u007f')
		  sum += 1;
		else if (c == '\u0000' || (c >= '\u0080' && c <= '\u07ff'))
		  sum += 2;
		else
		  sum += 3;
	      }

	    if (sum > 65535)
	      throw new UTFDataFormatException ();

	    writeShort (sum);

	    for (int i = 0; i < len; ++i)
	      {
		char c = value.charAt(i);
		if (c >= '\u0001' && c <= '\u007f')
		  write (c);
		else if (c == '\u0000' || (c >= '\u0080' && c <= '\u07ff'))
		  {
		    write (0xc0 | (0x1f & (c >> 6)));
		    write (0x80 | (0x3f & c));
		  }
		else
		  {
		    // JSL says the first byte should be or'd with 0xc0, but
		    // that is a typo.  Unicode says 0xe0, and that is what is
		    // consistent with DataInputStream.
				// MLB - I concur with that.
		    write (0xe0 | (0x0f & (c >> 12)));
		    write (0x80 | (0x3f & (c >> 6)));
		    write (0x80 | (0x3f & c));
		  }
     }
		}finally{lock.release();}
  }


//##################################################################
}
//##################################################################


