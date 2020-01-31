/* $MirOS: contrib/hosted/ewe/classes/ewe/zip/ZipOutputStream.java,v 1.2 2008/05/02 20:05:44 tg Exp $ */

/* java.util.zip.ZipOutputStream
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
import ewe.util.Vector;
/*
import java.io.OutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Vector;
import java.util.Enumeration;
*/
/**
 * This is a FilterOutputStream that writes the files into a zip
 * archive one after another.  It has a special method to start a new
 * zip entry.  The zip entries contains information about the file name
 * size, compressed size, CRC, etc.
 *
 * It includes support for STORED and DEFLATED entries.
 *
 * This class is not thread safe.
 *
 * @author Jochen Hoenicke
 */
public class ZipOutputStream extends StreamObject implements ZipConstants
{
	//public boolean debugMe;
  private Vector entries = new Vector();
  private CRC32 crc = new CRC32();
  private ZipEntry curEntry = null;

  private int curMethod;
  private int size;
  private int offset = 0;

  private byte[] zipComment = new byte[0];
  private int defaultMethod = DEFLATED;
	private Stream out;
	private Stream dataBytes;
  /**
   * Our Zip version is hard coded to 1.0 resp. 2.0
   */
  private final static int ZIP_STORED_VERSION   = 10;
  private final static int ZIP_DEFLATED_VERSION = 20;

  /**
   * Compression method.  This method doesn't compress at all.
   */
  public final static int STORED      =  ZipEntry.STORED;
  /**
   * Compression method.  This method uses the Deflater.
   */
  public final static int DEFLATED    =  ZipEntry.DEFLATED;

	public int level = Z_DEFAULT_COMPRESSION;
  /**
   * Creates a new Zip output stream, writing a zip archive.
   * @param out the output stream to which the zip archive is written.
   */
  public ZipOutputStream(BasicStream out)
  {
		this.out = (out instanceof Stream) ? (Stream)out : new StreamAdapter(out);
  }


	//-------------------------------------------------------------------
	byte [] toUtf(String str)
	//-------------------------------------------------------------------
	{
		char [] chars = ewe.sys.Vm.getStringChars(str);
		int len = ewe.util.Utils.sizeofJavaUtf8String(chars,0,chars.length);
		byte [] ret = new byte[len];
		ewe.util.Utils.encodeJavaUtf8String(chars,0,chars.length,ret,0);
		return ret;
	}
  /**
   * Set the zip file comment.
   * @param comment the comment.
   * @exception IllegalArgumentException if UTF8 encoding of comment is
   * longer than 0xffff bytes.
   */
  public boolean setComment(String comment)
  {
    byte[] commentBytes = toUtf(comment);
    if (commentBytes.length > 0xffff)
			return returnError("Comment too long.",false);
    zipComment = commentBytes;
		return true;
  }

  /**
   * Sets default compression method.  If the Zip entry specifies
   * another method its method takes precedence.
   * @param method the method.
   * @exception IllegalArgumentException if method is not supported.
   * @see #STORED
   * @see #DEFLATED
   */
  public boolean setMethod(int method)
  {
    if (method != STORED && method != DEFLATED)
      return returnError("Method not supported.",false);
    defaultMethod = method;
		return true;
  }

  /**
   * Sets default compression level.  The new level will be activated
   * immediately.
   * @exception IllegalArgumentException if level is not supported.
   * @see Deflater
   */
  public void setLevel(int level)
  {
    this.level = level;
  }
	private static byte [] buff = new byte[2];

  /**
   * Write an unsigned short in little endian byte order.
   */
  private final boolean writeLeShort(int value)// throws IOException
  {
		buff[0] = (byte)(value & 0xff);
		buff[1] = (byte)((value >> 8) & 0xff);
    if (out.writeBytes(buff,0,2) != 2) return false;
		return true;
  }

  /**
   * Write an int in little endian byte order.
   */
  private final boolean writeLeInt(int value)// throws IOException
  {
    if (!writeLeShort(value)) return false;
    return writeLeShort(value >> 16);
  }

  /**
   * Starts a new Zip entry. It automatically closes the previous
   * entry if present.  If the compression method is stored, the entry
   * must have a valid size and crc, otherwise all elements (except
   * name) are optional, but must be correct if present.  If the time
   * is not set in the entry, the current time is used.
   * @param entry the entry.
   * @exception IOException if an I/O error occured.
   * @exception IllegalStateException if stream was finished
   */
  public boolean putNextEntry(ZipEntry entry)// throws IOException
  {
    if (entries == null)
			return returnError("ZipOutputStream was finished",false);
      //throw new IllegalStateException("ZipOutputStream was finished");

    int method = entry.getMethod();
    int flags = 0;
    if (method == -1)
      method = defaultMethod;

    if (method == STORED)
      {
	if (entry.getCompressedSize() >= 0)
	  {
	    if (entry.getSize() < 0)
	      entry.setSize(entry.getCompressedSize());
	    else if (entry.getSize() != entry.getCompressedSize())
	      return returnError("Method STORED, but compressed size != size",false);
	  }
	else
	  entry.setCompressedSize(entry.getSize());

	if (entry.getSize() < 0)
	  return returnError("Method STORED, but size not set",false);
	if (entry.getCrc() < 0)
	  return returnError("Method STORED, but crc not set",false);
      }
    else if (method == DEFLATED)
      {
	if (entry.getCompressedSize() < 0
	    || entry.getSize() < 0 || entry.getCrc() < 0)
	  flags |= 8;
      }

    if (curEntry != null)
      closeEntry();

    if (entry.getTime(null)== null)
      entry.setTime(new ewe.sys.Time());

    entry.flags = flags;
    entry.offset = offset;
    entry.setMethod(method);
    curMethod = method;
    /* Write the local file header */
    writeLeInt(L_HDR_SIG);
    writeLeShort(method == STORED
		 ? ZIP_STORED_VERSION : ZIP_DEFLATED_VERSION);
    writeLeShort(flags);
    writeLeShort(method);
    writeLeInt(entry.getDOSTime());
    if ((flags & 8) == 0)
      {
	writeLeInt((int)entry.getCrc());
	writeLeInt((int)entry.getCompressedSize());
	writeLeInt((int)entry.getSize());
      }
    else
      {
	writeLeInt(0);
	writeLeInt(0);
	writeLeInt(0);
      }
    byte[] name = toUtf(entry.getName());
    if (name.length > 0xffff)
      return returnError("Name too long.",false);
    byte[] extra = entry.getExtra();
    if (extra == null)
      extra = new byte[0];
    writeLeShort(name.length);
    writeLeShort(extra.length);
		if (name.length != 0)
  	  if (out.writeBytes(name,0,name.length) != name.length)
				return false;
		if (extra.length != 0)
  	  if (out.writeBytes(extra,0,extra.length) != extra.length)
				return false;
    offset += L_SIZE + name.length + extra.length;

    /* Activate the entry. */

    curEntry = entry;
    crc.reset();
    if (method == DEFLATED)
			dataBytes = new DeflaterOutputStream(new PartialOutputStream(out),level,true);
		else{
			dataBytes = new PartialOutputStream(out);
		}
    size = 0;
		return true;
  }

  /**
   * Closes the current entry.
   * @exception IOException if an I/O error occured.
   * @exception IllegalStateException if no entry is active.
   */
  public boolean closeEntry()// throws IOException
  {
    if (curEntry == null)
			return returnError("No open entry",false);
      //throw new IllegalStateException("No open entry");

    /* First finish the deflater, if appropriate */
		if (dataBytes instanceof DeflaterOutputStream)
			if (!dataBytes.close())
				return false;
		int csize = (dataBytes instanceof DeflaterOutputStream) ?
			((DeflaterOutputStream)dataBytes).outputBytes : size;

		dataBytes = null;

    if (curEntry.getSize() < 0)
      curEntry.setSize(size);
    else if (curEntry.getSize() != size)
      return returnError("size was "+size
			     +", but I expected "+curEntry.getSize(),false);

    if (curEntry.getCompressedSize() < 0)
      curEntry.setCompressedSize(csize);
    else if (curEntry.getCompressedSize() != csize)
      return returnError("compressed size was "+csize
			     +", but I expected "+curEntry.getCompressedSize(),false);
    if (curEntry.getCrc() < 0)
      curEntry.setCrc(crc.getValue());
    else if (curEntry.getCrc() != (crc.getValue() & 0x00000000ffffffffL))
			return returnError("crc was " + crc.getValue()
			     + ", but I expected "
			     + curEntry.getCrc(),false);

    offset += csize;

    /* Now write the data descriptor entry if needed. */
    if (curMethod == DEFLATED && (curEntry.flags & 8) != 0)
      {
	writeLeInt(D_HDR_SIG);
	writeLeInt((int)curEntry.getCrc());
	writeLeInt((int)curEntry.getCompressedSize());
	writeLeInt((int)curEntry.getSize());
	offset += D_SIZE;
      }

    entries.add(curEntry);
    curEntry = null;
		return true;
  }

  /**
   * Writes the given buffer to the current entry.
   * @exception IOException if an I/O error occured.
   * @exception IllegalStateException if no entry is active.
   */
  public int nonBlockingWrite(byte[] b, int off, int len)
  {
    if (curEntry == null || dataBytes == null)
      return returnError("No open entry.",READWRITE_ERROR);
		int ret = dataBytes.nonBlockingWrite(b,off,len);
		if (ret == 0) return 0;
		if (ret < 0) return returnError("Output error.",READWRITE_ERROR);
    crc.update(b, off, ret);
    size += ret;
		return ret;
  }

  /**
   * Finishes the stream.  This will write the central directory at the
   * end of the zip file and flush the stream.
   * @exception IOException if an I/O error occured.
   */
  public boolean finish() //throws IOException
  {
    if (entries == null)
      return true;

    if (curEntry != null){
      if (!closeEntry())
				return false;
		}

    int numEntries = 0;
    int sizeEntries = 0;

    ewe.util.Iterator enumiter = entries.iterator();
    while (enumiter.hasNext())
      {
	ZipEntry entry = (ZipEntry) enumiter.next();

	int method = entry.getMethod();
	writeLeInt(C_HDR_SIG);
	writeLeShort(method == STORED
		     ? ZIP_STORED_VERSION : ZIP_DEFLATED_VERSION);
	writeLeShort(method == STORED
		     ? ZIP_STORED_VERSION : ZIP_DEFLATED_VERSION);
	writeLeShort(entry.flags);
	writeLeShort(method);
	writeLeInt(entry.getDOSTime());
	writeLeInt((int)entry.getCrc());
	writeLeInt((int)entry.getCompressedSize());
	writeLeInt((int)entry.getSize());
	byte[] name = toUtf(entry.getName());
	if (name.length > 0xffff)
	  return returnError("Name too long.",false);
	byte[] extra = entry.getExtra();
	if (extra == null)
	  extra = new byte[0];
	String strComment = entry.getComment();
	byte[] comment = strComment != null
	  ? toUtf(strComment) : new byte[0];
	if (comment.length > 0xffff)
	  return returnError("Comment too long.",false);

	writeLeShort(name.length);
	writeLeShort(extra.length);
	writeLeShort(comment.length);
	writeLeShort(0); /* disk number */
	writeLeShort(0); /* internal file attr */
	writeLeInt(0);   /* external file attr */
	writeLeInt(entry.offset);

	if (name.length != 0) out.writeBytes(name,0,name.length);
	if (extra.length != 0) out.writeBytes(extra,0,extra.length);
	if (comment.length != 0) out.writeBytes(comment,0,comment.length);
	numEntries++;
	sizeEntries += C_SIZE + name.length + extra.length + comment.length;
      }

    writeLeInt(EC_HDR_SIG);
    writeLeShort(0); /* disk number */
    writeLeShort(0); /* disk with start of central dir */
    writeLeShort(numEntries);
    writeLeShort(numEntries);
    writeLeInt(sizeEntries);
    writeLeInt(offset);
    writeLeShort(zipComment.length);
		if (zipComment.length != 0) out.writeBytes(zipComment,0,zipComment.length);
    entries = null;
		try{out.flush();}catch(Exception e){}
		boolean ret = out.close();
		return ret;
  }
	public boolean flushStream() throws ewe.io.IOException {return true;}
}
