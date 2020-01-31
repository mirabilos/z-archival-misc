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
* This provides a full stream from a BasicStream implementation. This allows you to define
* that absolute bare minimum for Stream functionality in an object (which is quite easy
* to do) and then use a StreamAdapter to convert it to a full stream. Use this if your
* object cannot extend a StreamObject - which is the preferred way to create a new Stream
* class.<p>
* This also provides a Stream implementation from an InputStream or OutputStream.
**/
//##################################################################
public class StreamAdapter extends StreamObject implements BufferedStream, StreamCanPause{
//##################################################################
//
// Don't move these 4 or add more variables.
//
protected BasicStream stream;
protected OutputStream out;
protected InputStream in;
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
protected StreamAdapter(){}
//-------------------------------------------------------------------
//===================================================================
public StreamAdapter(BasicStream basicStream)
//===================================================================
{
	stream = basicStream;
}

//===================================================================
public StreamAdapter(InputStream in)
//===================================================================
{
	this.in = in;
}

//===================================================================
public StreamAdapter(OutputStream out)
//===================================================================
{
	this.out = out;
}
//===================================================================
public StreamAdapter(InputStream in, OutputStream out)
//===================================================================
{
	this.in = in;
	this.out = out;
}
//===================================================================
public boolean isOpen()
//===================================================================
{
	if (stream != null) return stream.isOpen();
	return true;
}
//===================================================================
public boolean closeStream() throws ewe.io.IOException
//===================================================================
{
	if (stream != null) return stream.closeStream();
	if (out != null) out.close();
	if (in != null) in.close();
	return true;
}
/**
* This calls the nonBlockingRead() of the client BasicStream.
**/
//===================================================================
public int nonBlockingRead(byte []buff,int start,int count)
//===================================================================
{/*
	if (getClass().getName().endsWith("StreamAdapter") && count > 4) {
		ewe.sys.Vm.debug("NBR: "+pushed.length);
		new Exception().printStackTrace();
	}
	*/
	if (pushed.length == 0) {
		if (stream != null) return stream.nonBlockingRead(buff,start,count);
		else if (in != null){
			try{
				return in.read(buff,start,count);
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
	else if (out == null) return -2;
	try{
		out.write(buff,start,count);
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
	else if (out != null) {
		out.flush();
		return true;
	}else
		return true;
}
/*
//===================================================================
public void flush() throws ewe.io.IOException
//===================================================================
{
	if (stream != null) stream.
	if (stream instanceof BufferedStream)
		return ((BufferedStream)stream).flush();
	else if (out != null) try{
		out.flush();
	}catch(Exception e){
		error = e.getMessage();
		return false;
	}
	return true;
}
*/
//##################################################################
}
//##################################################################

