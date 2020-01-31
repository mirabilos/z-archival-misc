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
import ewe.sys.*;
import ewe.util.ByteArray;

/**
* A BlockingStreamObject is a full implementation of a Stream but where there are no
* actual non-blocking methods. That is, the nonBlocking() methods will still be there but
* they actually will block the calling thread.<p>
*
* The only methods you need to override are:<br>
*
* <b>int read(byte[] buffer,int offset,int length) throws IOException;</b><br>
* <b>void write(byte[] buffer,int offset,int length) throws IOException;</b><br>
* <b>boolean closeStream() throws IOException;</b><br>
* <b>boolean flushStream() throws IOException;</b><br>
**/
//##################################################################
public class BlockingStreamObject extends BasicStreamObject{
//##################################################################
//===================================================================
public int read(byte[] data,int offset,int length) throws IOException
//===================================================================
{
	throw new IOException("Cannot read from this stream.");
}
//===================================================================
public void write(byte[] data,int offset,int length) throws IOException
//===================================================================
{
	throw new IOException("Cannot write to this stream.");
}
//===================================================================
public final boolean close()
//===================================================================
{
	try{
		while(!closeStream()) mThread.yield();
		return true;
	}catch(IOException e){
		error = e.getMessage();
		return false;
	}
}
//===================================================================
public final void flush() throws IOException
//===================================================================
{
	while(!flushStream()) mThread.yield();
}
//===================================================================
public final int nonBlockingRead(byte[] data,int offset,int length)
//===================================================================
{
	try{
		return read(data,offset,length);
	}catch(IOException e){
		error = e.getMessage();
		return -2;
	}
}
//===================================================================
public final int nonBlockingWrite(byte[] data,int offset,int length)
//===================================================================
{
	try{
		write(data,offset,length);
		return length;
	}catch(IOException e){
		error = e.getMessage();
		return -2;
	}
}
//===================================================================
public boolean isOpen()
//===================================================================
{
	return !closed;
}
//===================================================================
public boolean flushStream() throws IOException
//===================================================================
{
	return true;
}
//===================================================================
public boolean closeStream() throws IOException
//===================================================================
{
	closed = true;
	return true;
}
//===================================================================
public final int readBytes(byte[] data, int offset, int length)
//===================================================================
{
	try{
		int got = read(data,offset,length);
		if (got == -1) return 0;
		return got;
	}catch(IOException e){
		error = e.getMessage();
		return -1;
	}
}
//===================================================================
public final int writeBytes(byte[] data, int offset, int length)
//===================================================================
{
	try{
		write(data,offset,length);
		return length;
	}catch(IOException e){
		error = e.getMessage();
		return -1;
	}
}
//===================================================================
public final IOHandle readBytes(final byte[] data,final int offset,final int length,IOHandle h,final boolean readAll)
//===================================================================
{
	if (h == null) h = new IOHandle();
	new ewe.sys.TaskObject(h){
		protected void dorun(){
			int didRead = 0, need = length, off = offset;
			IOHandle ioh = (IOHandle)handle;
			try{
				while(need>0){
					int got = read(data,off,need);
					if (got == -1){
						if (readAll || didRead == 0){
							ioh.errorCode = ioh.STREAM_END_REACHED;
							ioh.failed(new IOException("Stream Ended."));
							return;
						}
						break;
					}
					didRead += got;
					off += got;
					need -= got;
				}
				ioh.bytesTransferred = didRead;
				ioh.set(ioh.Succeeded);
			}catch(IOException e){
				ioh.errorCode = ioh.IO_ERROR;
				ioh.failed(e);
			}
		}
	}.startTask();
	return h;
}
//===================================================================
public final IOHandle writeBytes(final byte[] data,final int offset,final int length,IOHandle h)
//===================================================================
{
	if (h == null) h = new IOHandle();
	new ewe.sys.TaskObject(h){
		protected void dorun(){
			IOHandle ioh = (IOHandle)handle;
			try {
				write(data,offset,length);
				ioh.bytesTransferred = length;
				ioh.set(ioh.Succeeded);
			}catch(IOException e){
				ioh.errorCode = ioh.IO_ERROR;
				ioh.failed(e);
			}
		}
	}.startTask();
	return h;
}
//##################################################################
}
//##################################################################

