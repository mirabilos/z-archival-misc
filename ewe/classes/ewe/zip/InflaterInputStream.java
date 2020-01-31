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
package ewe.zip;
import ewe.util.ByteArray;

//##################################################################
public class InflaterInputStream extends ewe.io.StreamObject{
//##################################################################
/**
* Don't use this. It is a placeholder for a native resource.
*/
protected Object reserved; //Don't move. Should be the first variable.
protected ByteArray byteIn = new ByteArray();
protected ByteArray byteOut = new ByteArray();
protected ewe.io.BasicStream input;
//===================================================================
public InflaterInputStream(ewe.io.BasicStream input)
//===================================================================
{
	this(input,false);
}
//===================================================================
public InflaterInputStream(ewe.io.BasicStream input,boolean noWrap)
//===================================================================
{
	this.input = input;
	inflateInit(noWrap);
	setBufferSize(1024*10);
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
/**
* Don't call this after starting to read input!
**/
//===================================================================
public void setBufferSize(int size)
//===================================================================
{
	if (size <= 0) size = 1024;
	byteIn.data = new byte[size];
	byteOut.data = new byte[size];
}
//-------------------------------------------------------------------
protected native int inflateInit(boolean noWrap);
protected native int inflate(ByteArray in,ByteArray out);
protected native int inflateEnd();
//-------------------------------------------------------------------

protected static final int Z_OK            = 0;
protected static final int Z_STREAM_END    = 1;
protected static final int Z_NEED_DICT     = 2;
protected static final int Z_ERRNO         = (-1);
protected static final int Z_STREAM_ERROR  = (-2);
protected static final int Z_DATA_ERROR    = (-3);
protected static final int Z_MEM_ERROR     = (-4);
protected static final int Z_BUF_ERROR     = (-5);
protected static final int Z_VERSION_ERROR = (-6);

boolean finished = false;

//===================================================================
public int nonBlockingRead(byte [] buff,int start,int count)
//===================================================================
{
	if (closed) return READWRITE_CLOSED;
	while(true){
		if (byteOut.length != 0){ //There is data waiting in the output buffer.
			int len = byteOut.length;
			if (len > count) len = count;
			ewe.sys.Vm.copyArray(byteOut.data,0,buff,start,len);
			if (len < byteOut.length)
				ewe.sys.Vm.copyArray(byteOut.data,len,byteOut.data,0,byteOut.length-len);
			byteOut.length -= len;
			return len;
		}
		if (finished) { //No data waiting to go and no further input needed.
			closed = true;
			return READWRITE_CLOSED;
		}
		int got = inflate(byteIn,byteOut);
		if (got != Z_OK && got != Z_STREAM_END && got != Z_BUF_ERROR){
			return READWRITE_ERROR;
		}
		if (got == Z_STREAM_END){
			finished = true;
			if (input instanceof ewe.io.BufferedStream && byteIn.length > 0){
				//ewe.sys.Vm.debug("pushing back: "+byteIn.length);
				//ewe.sys.Vm.debug("First byte: "+(int)byteIn.data[0]);
				((ewe.io.BufferedStream)input).pushback(byteIn.data,0,byteIn.length);
			}
			inflateEnd();
		}
		if (byteOut.length != 0) continue;
		else{ //If there is no data in the input buffer.
			if (!input.isOpen() || finished){
				closed = true;
				return READWRITE_CLOSED;
			}
			got = input.nonBlockingRead(byteIn.data,0,byteIn.data.length);
			if (got == 0) return 0;
			if (got < 0) {
				//input.close();
				closed = true;
				return got;
			}
			byteIn.length = got;
		}
	}
}

//===================================================================
public boolean closeStream() throws ewe.io.IOException
//===================================================================
{
	inflateEnd();
	if (input != null) input.closeStream();
	//ewe.sys.Vm.debug("Closing!",0);
	return super.closeStream();
}
public boolean flushStream() throws ewe.io.IOException {return true;}
//##################################################################
}
//##################################################################

