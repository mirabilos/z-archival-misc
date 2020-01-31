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
* This provides a full RandomAccessStream implementation from either a BasicRandomAccessStream
* implementation <b>or</b> from a RandomStream object.
**/
//##################################################################
public class RandomStreamAdapter extends RandomStreamObject implements BufferedStream, StreamCanPause{
//##################################################################

protected RandomStream rs;
protected BasicRandomAccessStream stream;

ByteArray pushed = new ByteArray();

//===================================================================
public int pauseUntilReady(int pauseFor,int value)
//===================================================================
{
	if (!(stream instanceof StreamCanPause)) return 0;
	return ((StreamCanPause)stream).pauseUntilReady(pauseFor,value);
}
/**
* Push back some bytes into the input stream. There is no limit on the
* number of bytes which can be pushed back. This can be called multiple
* times.
**/
//===================================================================
public boolean pushback(byte [] bytes,int start,int count)
//===================================================================
{
	if (count <= 0 || bytes == null) return true;
	pushed.makeSpace(0,count);
	ewe.sys.Vm.copyArray(bytes,start,pushed.data,0,count);
	//ewe.sys.Vm.debug("Pushed a byte: "+pushed.data[0]);
	return true;
}
//-------------------------------------------------------------------
protected RandomStreamAdapter(){}
//-------------------------------------------------------------------
//===================================================================
public RandomStreamAdapter(BasicRandomAccessStream basicStream)
//===================================================================
{
	stream = basicStream;
}

//===================================================================
public RandomStreamAdapter(RandomStream rs)
//===================================================================
{
	this.rs = rs;
}
//===================================================================
public boolean canWrite()
//===================================================================
{
	if (stream != null) return stream.canWrite();
	else if (rs != null) return rs.canWrite();
	else return false;
}
//===================================================================
public boolean isOpen()
//===================================================================
{
	if (stream != null) return stream.isOpen();
	return true;
}
/**
* This calls the nonBlockingRead() of the client BasicStream.
**/
//===================================================================
public int nonBlockingRead(byte []buff,int start,int count)
//===================================================================
{
	if (pushed.length == 0) {
		if (stream != null) return stream.nonBlockingRead(buff,start,count);
		else if (rs != null){
			try{
				return rs.read(buff,start,count);
			}catch(Exception e){
				error = e.getMessage();
				return -2;
			}
		}else
			return -2;
	}
	if (count > pushed.length) count = pushed.length;
	ewe.sys.Vm.copyArray(pushed.data,0,buff,start,count);
	pushed.length -= count;
	if (pushed.length != 0)
		ewe.sys.Vm.copyArray(pushed.data,count,pushed.data,0,pushed.length);
	return count;
}
/**
* This calls the nonBlockingWrite() of the client BasicStream.
**/
//===================================================================
public int nonBlockingWrite(byte []buff,int start,int count)
//===================================================================
{
	if (stream != null) return stream.nonBlockingWrite(buff,start,count);
	else if (rs == null) return -2;
	try{
		rs.write(buff,start,count);
		//ewe.sys.Vm.debug("Writing: "+count);
		return count;
	}catch(Exception e){
		error = e.getMessage();
		return -2;
	}
}
//===================================================================
public boolean flushStream() throws ewe.io.IOException
//===================================================================
{
	if (stream != null) return stream.flushStream();
	else if (rs != null) {
		rs.flush();
		return true;
	}else
		return true;
}
//===================================================================
public boolean closeStream() throws ewe.io.IOException
//===================================================================
{
	closed = true;
	if (stream != null) return stream.closeStream();
	if (rs != null) rs.close();
	return true;
}
//===================================================================
public boolean setStreamLength(long length) throws IOException
//===================================================================
{
	if (stream != null) return stream.setStreamLength(length);
	else if (rs != null) rs.setLength(length);
	else throw new IOException();
	return true;
}
//===================================================================
public long getStreamLength() throws IOException
//===================================================================
{
	if (stream != null) return stream.getStreamLength();
	else if (rs != null) return rs.length();
	else return -1;
}
//===================================================================
public long tellPosition() throws IOException
//===================================================================
{
	if (stream != null) return stream.tellPosition();
	else if (rs != null) return rs.tell();
	else return -1;
}
//===================================================================
public boolean seekPosition(long where) throws IOException
//===================================================================
{
	if (stream != null) return stream.seekPosition(where);
	else if (rs != null) rs.seek(where);
	return true;
}
//===================================================================
public int nonBlockingRead(long location,byte[] dest,int offset,int length) throws IOException
//===================================================================
{
	if (stream != null) return stream.nonBlockingRead(location,dest,offset,length);
	else return super.nonBlockingRead(location,dest,offset,length);
}
//===================================================================
public int nonBlockingWrite(long location,byte[] src,int offset,int length) throws IOException
//===================================================================
{
	if (stream != null) return stream.nonBlockingWrite(location,src,offset,length);
	else return super.nonBlockingWrite(location,src,offset,length);
}
//##################################################################
}
//##################################################################

