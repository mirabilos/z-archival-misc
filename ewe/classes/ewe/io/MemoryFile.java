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
import ewe.util.*;
import ewe.sys.Vm;

/**
* This is used for creating a RandomAccessStream from a byte array or from a
* file on disk.
**/
//##################################################################
public class MemoryFile extends RandomStreamObject implements FastStream{
//##################################################################

int ptr = 0;
protected int mode;
public ByteArray data = new ByteArray();

//===================================================================
public boolean isOpen() {return !closed;}
//===================================================================
public boolean flushStream() throws ewe.io.IOException {return true;}

//-------------------------------------------------------------------
public int nonBlockingRead(byte buf[], int start, int count)
//-------------------------------------------------------------------
{
	if (closed || ptr >= data.length) return -1;
	if (ptr+count > data.length) count = data.length-ptr;
	if (count < 0) count = 0;
	if (count != 0) Vm.copyArray(data.data,ptr,buf,start,count);
	ptr += count;
	return count;
}

//-------------------------------------------------------------------
public int nonBlockingWrite(byte buf[], int start, int count)
//-------------------------------------------------------------------
{
	if (closed) return -1;
	if (mode == RandomAccessFile.READ_ONLY) return -2;
	if (count <= 0) return 0;
	if (ptr+count > data.length) data.makeSpace(ptr,ptr+count-data.length);
	Vm.copyArray(buf,start,data.data,ptr,count);
	ptr += count;
	return count;
}

//===================================================================
public boolean canWrite()
//===================================================================
{
	return mode != RandomAccessFile.READ_ONLY;
}
/** Returns the length of the file in bytes. If the file is not open
  * 0 will be returned.
  */
public int getLength()
{
	return data.length;
}

public boolean seek(int pos)
{
	if (closed) return false;
	ptr = pos;
	return true;
}

public int getFilePosition()
{
	if (closed) return -1;
	return ptr;
}

//===================================================================
public MemoryFile() {mode = RandomAccessFile.READ_WRITE;}
//===================================================================

/**
* This opens it in READ_ONLY mode.
**/
//===================================================================
public MemoryFile(ByteArray dataToUse)
//===================================================================
{
	this.mode = RandomAccessFile.READ_ONLY;
	data = dataToUse;
}
//===================================================================
public MemoryFile(ByteArray dataToUse,String mode)
//===================================================================
{
	this.mode = FileBase.convertMode(mode);
	data = dataToUse;
}
/**
* @deprecated - use new MemoryFile(byte [] bytes,int start,int length,String mode) instead.
**/
//===================================================================
public MemoryFile(byte [] bytes,int start,int length,int mode)
//===================================================================
{
	this.mode = mode;
	if (length < 0) length = 0;
	data.data = new byte[length];
	data.length = length;
	if (length != 0)
		ewe.sys.Vm.copyArray(bytes,start,data.data,0,length);
}

/**
 * Create a memory file from an array of bytes.
 * @param bytes
 * @param start
 * @param length
 * @param mode
 */
//===================================================================
public MemoryFile(byte[] bytes,int start,int length,String mode)
//===================================================================
{
	this.mode = File.convertMode(mode);
	if (length < 0) length = 0;
	data.data = new byte[length];
	data.length = length;
	if (length != 0)
		ewe.sys.Vm.copyArray(bytes,start,data.data,0,length);
}
/**
* @deprecated - use new MemoryFile(Stream is,String mode) instead.
**/
//===================================================================
public MemoryFile(String path,int mode)
//===================================================================
{
	this.mode = mode;
	if (mode == RandomAccessFile.READ_WRITE || mode == RandomAccessFile.READ_ONLY) {
		File file = ewe.sys.Vm.newFileObject().getNew(path);
		RandomAccessStream f = file.getRandomAccessStream(mode);
		int len = f.getLength();
		data.data = new byte[len];
		data.length = len;
		int did = 0;
		while(did < len){
			int got = f.readBytes(data.data,did,(len-did));
			if (got != -1) did += got;
			else break;
		}
		f.close();
	}
}
/**
* This creates a MemoryFile from an input stream. All bytes from the stream are
* read in and written to the memory file. Alterations to the returned MemoryFile
* will not affect the original data source. The original Stream is closed.
*
* @deprecated - use new MemoryFile(Stream is,String mode) instead.
**/
//===================================================================
public static MemoryFile createFrom(Stream is,StringBuffer error)
//===================================================================
{
	if (error == null) error = new StringBuffer();
	error.setLength(0);
	MemoryFile mf = new MemoryFile();
	IOTransfer io = new IOTransfer();
	if (!io.run(is,mf)){
		error.append("Error reading from stream.");
		is.close();
		return null;
	}
	is.close();
	mf.seek(0);
	return mf;
}
/**
* This creates a MemoryFile from an input File. All bytes from the stream are
* read in and written to the memory file. Alterations to the returned MemoryFile
* will not affect the original data source. The original File is closed.
**/
//===================================================================
public MemoryFile(File in,String mode, boolean useFastStream) throws IOException
//===================================================================
{
	data = IO.readAllBytes(in,null,useFastStream);
	seek(0);
	this.mode = File.convertMode(mode);
}

/**
* This creates a MemoryFile from an input stream. All bytes from the stream are
* read in and written to the memory file. Alterations to the returned MemoryFile
* will not affect the original data source. The original Stream is closed.
**/
//===================================================================
public MemoryFile(Stream is,String mode) throws IOException
//===================================================================
{
	try{
		IOTransfer io = new IOTransfer();
		io.transfer(is,this);
	}finally{
		is.close();
	}
	seek(0);
	this.mode = File.convertMode(mode);
}

//===================================================================
public boolean setStreamLength(long length) throws IOException
//===================================================================
{
	if (closed) throw new IOException("Stream is not open.");
	if (length > data.length) {
		data.makeSpace(data.length,(int)(length-data.length));
	}else if (length < data.length){
		data.delete(data.length, (int)(data.length-length));
		if (ptr > data.length) ptr = data.length;
	}
	return true;
}
//===================================================================
public int quickRead(byte[] data,int offset,int length,boolean readAll) throws IOException
//===================================================================
{
	if (length <= 0) return 0;
	int read = 0;
	while(length > 0){
		int got = nonBlockingRead(data,offset,length);
		if (got == -1)
			if (readAll) throw new IOException("Stream ended.");
			else return read == 0 ? -1 : read;
		else if (got < 0) throwIOException(null);
		read += got;
		if (!readAll) break;
		offset += got;
		length -= got;
	}
	return read == 0 ? -1 : read;
}
//===================================================================
public void quickWrite(byte[] data,int offset,int length) throws IOException
//===================================================================
{
	if (length <= 0) return;
	int ret = nonBlockingWrite(data,offset,length);
	if (ret != length) throwIOException(null);
}

//##################################################################
}
//##################################################################

