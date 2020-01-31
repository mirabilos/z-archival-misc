/*********************************************************************************
 *  Ewe Virtual Machine - Version 1.14, January 24, 2002                         *
 *  Copyright (C) 1999-2002 Michael L Brereton <michael_brereton@ewesoft.com>    *
 *  Copyright (C) 1998, 1999 Wabasoft <www.wabasoft.com>                         *
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
import java.io.*;
/**
 * RandomAccessFile is what you should use for file I/O.
 */

public class RandomAccessFile extends RandomStreamObject //implements OverridesClose - no it doesn't
implements DataOutput, DataInput, FastStream
{
/** Read-only open mode. */
public static final int READ_ONLY  = 1;
/** Write-only open mode. */
public static final int WRITE_ONLY = 2;
/** Read-write open mode. */
public static final int READ_WRITE = 3; // READ | WRITE
/** Create open mode. Used to create a file if one does not exist.
@deprecated WRITE_ONLY and READ_WRITE mode will create a file if one does not exist.*/
public static final int CREATE = 4;

String path;
int mode;
java.io.RandomAccessFile openFile;
boolean closed = false;

protected DataOutput dos;
protected DataInput dis;

//-------------------------------------------------------------------
protected RandomAccessFile(){}
//-------------------------------------------------------------------

/**
 * Opens a file with the given name and mode. If mode is CREATE, the
 * file will be created if it does not exist. The DONT_OPEN mode
 * allows the exists(), rename(), delete(), listDir(), createDir()
 * and isDir() methods to be called without requiring the file to be
 * open for reading or writing.
 * @param path the file's path
 * @param mode one of DONT_OPEN, READ_ONLY, WRITE_ONLY, READ_WRITE or CREATE
 */
//===================================================================
public RandomAccessFile(File f,int mode)
//===================================================================
{
	_nativeCreate(f.getFullPath(),mode);
	dos = new DataOutputStream(this);
	dis = new DataInputStream(this);
}
/**
 * Open a new RandomAccessFile in read or read-write mode.
 * @param file the file to open. This must be a disk based File.
 * @param mode must be "r" for Read-only mode, or "rw" for Read-Write mode. The "rw" mode
	will attempt to create a file if one does not exist.
 * @exception IOException if the file could not be opened.
 */
//===================================================================
public RandomAccessFile(File file,String mode) throws IOException
//===================================================================
{
	this(file.getAbsolutePath(),mode);
}
/**
 * Open a new RandomAccessFile in read or read-write mode.
 * @param file the name of the file to open. This must refer to a file on a disk.
 * @param mode must be "r" for Read-only mode, or "rw" for Read-Write mode. The "rw" mode
	will attempt to create a file if one does not exist.
 * @exception IOException if the file could not be opened.
 */
//===================================================================
public RandomAccessFile(String file,String mode) throws IOException
//===================================================================
{
	_nativeCreate(file,File.convertMode(mode));
	if (openFile == null) throw new IOException(error == null ? "Could not open: "+file : error);
	dos = new DataOutputStream(this);
	dis = new DataInputStream(this);
}
//===================================================================
public boolean canWrite()
//===================================================================
{
	return mode != RandomAccessFile.READ_ONLY;
}

//===================================================================
public RandomAccessFile(java.io.RandomAccessFile rf)
//===================================================================
{
	mode = READ_WRITE;
	openFile = rf;
	dos = new DataOutputStream(this);
	dis = new DataInputStream(this);
}
//-------------------------------------------------------------------
private void _nativeCreate(String file,int mode)
//-------------------------------------------------------------------
{
	this.mode = mode;
	try{
		String fmode = "rw";
		if (mode == READ_ONLY) fmode = "r";
		openFile = new java.io.RandomAccessFile(file,fmode);
	}catch(java.io.IOException e){
		openFile = null;
		error = e.getMessage();
	}
}
/**
 * Closes the file. Returns true if the operation is successful and false
 * otherwise.
 */
public boolean closeStream() throws ewe.io.IOException
{
	try{
		if (openFile != null) openFile.close();
	}catch(java.io.IOException e){
		throw new ewe.io.IOException(e.getMessage());
	}
	openFile = null;
	return true;
}


/**
 * Returns true if the file is open for reading or writing and
 * false otherwise. This can be used to check if opening or
 * creating a file was successful.
 */
public boolean isOpen()
{
	return openFile != null;
}

/** Returns the length of the file in bytes. If the file is not open
  * 0 will be returned.
  */
public int getLength()
{
	if (openFile != null) try {return (int)openFile.length();}catch(Exception e){}
	return 0;//(int)file.length();
}
/**
* This returns:
* >0 = Number of bytes read.
* 0 = No bytes ready to read.
* -1 = End of file.
* -2 = IO error.
**/
//-------------------------------------------------------------------
public int nonBlockingRead(byte []buf,int start,int count)
//-------------------------------------------------------------------
{
	if (openFile == null) return -1;
	try{
		return openFile.read(buf,start,count);
	}catch(Exception e){
		error = e.getMessage();
		return -1;
	}
}

/**
* This returns:
* >0 = Number of bytes written.
* 0 = No bytes could be written yet.
* -1 = File Closed
* -2 = IO error.
**/
//-------------------------------------------------------------------
public int nonBlockingWrite(byte []buf,int start,int count)
//-------------------------------------------------------------------
{
	if (openFile == null) return -1;
	try{
		openFile.write(buf,start,count);
		return count;
	}catch(Exception e){
		error = e.getMessage();
		return -1;
	}
}
/**
 * Sets the file pointer for read and write operations to the given
 * position. The position passed is an absolute position, in bytes,
 * from the beginning of the file. To set the position to just after
 * the end of the file, you can call:
 * <pre>
 * file.seek(file.getLength());
 * </pre>
 * True is returned if the operation is successful and false otherwise.
 */
public boolean seek(int pos)
{
	if (openFile == null) return false;
	try{
		//ewe.sys.Vm.debug("Seek - "+hashCode()+": "+pos);
		openFile.seek(pos);
		return true;
	}catch(Exception e){
		error = e.getMessage();
		return false;
	}
}

public int getFilePosition()
{
	if (openFile == null) return -1;
	try{
		int got = (int)openFile.getFilePointer();
		//ewe.sys.Vm.debug("Tell - "+hashCode()+": "+got);
		return got;
	}catch(Exception e){
		error = e.getMessage();
		return -1;
	}
}

public boolean flushStream() throws ewe.io.IOException
{
	if (openFile == null) return true;
	return true;
}

//===================================================================
public boolean setStreamLength(long length) throws IOException
//===================================================================
{
	if (openFile == null) throw new IOException("File closed.");
	try{
		openFile.setLength(length);
		return true;
	}catch(NoSuchMethodError err){
		throw new IOException("The File length could not be set!");
	}catch(java.io.IOException e){
		throw new IOException(e.getMessage());
	}
}

public void writeBoolean(boolean value) throws IOException
{
	dos.writeBoolean(value);
}
public void writeByte(int value) throws IOException
{
	dos.writeByte(value);
}
public void writeFloat(float value) throws IOException
{
	dos.writeFloat(value);
}
public void writeLong(long value) throws IOException
{
	dos.writeLong(value);
}
public void writeShort(int value) throws IOException
{
	dos.writeShort(value);
}
public void writeDouble(double value) throws IOException
{
	dos.writeDouble(value);
}
public void writeBytes(String value) throws IOException
{
	dos.writeBytes(value);
}
public void writeChars(String value) throws IOException
{
	dos.writeChars(value);
}
public void writeChar(int value) throws IOException
{
	dos.writeChar(value);
}
public void writeUTF(String value) throws IOException
{
	dos.writeUTF(value);
}
public void writeInt(int value) throws IOException
{
	dos.writeInt(value);
}
  public boolean readBoolean() throws EOFException, IOException
	{return dis.readBoolean();}
  public byte readByte() throws EOFException, IOException
	{return dis.readByte();}
 public  int readUnsignedByte() throws EOFException, IOException
	{return dis.readUnsignedByte();}
 public  char readChar() throws EOFException, IOException
	{return dis.readChar();}
 public  short readShort() throws EOFException, IOException
	{return dis.readShort();}
 public  int readUnsignedShort() throws EOFException, IOException
	{return dis.readUnsignedShort();}
 public  int readInt() throws EOFException, IOException
	{return dis.readInt();}
 public  long readLong() throws EOFException, IOException
	{return dis.readLong();}
public   float readFloat() throws EOFException, IOException
	{return dis.readFloat();}
 public  double readDouble() throws EOFException, IOException
	{return dis.readDouble();}
 public  String readUTF() throws EOFException, UTFDataFormatException, IOException
	{return dis.readUTF();}
  public  void readFully(byte[] buf) throws EOFException, IOException
	{dis.readFully(buf);}
 public  void readFully(byte[] buf, int offset, int len)  throws EOFException, IOException
	{dis.readFully(buf,offset,len);}
 public  int skipBytes(int numBytes) throws EOFException, IOException
	{return dis.skipBytes(numBytes);}

/**
* This attempts read using a fast native method. Failing that a normal read is done. If the
* native method is used the method <b>will</b> block the entire VM until the data is read,
* so you should use this with care.
* @param data The destination for the data.
* @param offset The index in the destination for the data.
* @param length The number of bytes to read.
* @param readAll if this is true then the method will not return until a full length number of
* bytes have been read. If the stream ends before this then an IOException is thrown.
* @return the number of bytes read.
* @exception IOException
*/
//===================================================================
public int quickRead(byte[] data,int offset,int length,boolean readAll) throws IOException
//===================================================================
{
	if (length <= 0) return 0;
	/*
	try{
		int ret = nativeRead(data,offset,length,readAll);
		if (ret < 0) throw new IOException();
		if (ret > 0) return ret;
	}catch(UnsatisfiedLinkError e){}
	*/
	int read = 0;
	while(length > 0){
		int got = read(data,offset,length);
		if (got == -1)
			if (readAll) throw new IOException("Stream ended.");
			else return read == 0 ? -1 : read;
		read += got;
		if (!readAll) break;
		offset += got;
		length -= got;
	}
	return read == 0 ? -1 : read;
}
/**
* This attempts write using a fast native method. Failing that a normal write is done. If the
* native method is used the method <b>will</b> block the entire VM until the data is written,
* so you should use this with care.
* @param data The source of the data bytes.
* @param offset The index in the source of the data.
* @param length The number of bytes to write.
* @exception IOException
*/
//===================================================================
public void quickWrite(byte[] data,int offset,int length) throws IOException
//===================================================================
{
	if (length <= 0) return;
	/*
	try{
		int ret = nativeWrite(data,offset,length);
		if (ret < 0) throw new IOException();
		if (ret > 0) return;
	}catch(UnsatisfiedLinkError e){}
	*/
	write(data,offset,length);
}

}
