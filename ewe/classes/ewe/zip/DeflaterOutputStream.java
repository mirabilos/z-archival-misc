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
public class DeflaterOutputStream extends ewe.io.StreamObject implements ZipConstants, ewe.io.OverridesClose{
//##################################################################
/**
* Don't use this. It is a placeholder for a native resource.
*/
protected Object reserved; //Don't move. Should be the first variable.
protected ByteArray byteIn = new ByteArray();
protected ByteArray byteOut = new ByteArray();
protected ewe.io.BasicStream output;
/**
* The number of bytes written to the deflater.
**/
public int inputBytes;
/**
* The number of bytes writeen out by the deflater.
**/
public int outputBytes;
//===================================================================
public DeflaterOutputStream(ewe.io.BasicStream output)
//===================================================================
{
	this(output,Z_DEFAULT_COMPRESSION);
}
//===================================================================
public DeflaterOutputStream(ewe.io.BasicStream output,int level)
//===================================================================
{
	this(output,level,false);
}
//===================================================================
public DeflaterOutputStream(ewe.io.BasicStream output,int level,boolean noWrap)
//===================================================================
{
	this.output = output;
	deflateInit(level,noWrap);
	setBufferSize(1024);
}
//===================================================================
public DeflaterOutputStream(ewe.io.OutputStream output)
//===================================================================
{
	this(new ewe.io.StreamAdapter(output));
}
//===================================================================
public DeflaterOutputStream(ewe.io.OutputStream output,int level,boolean noWrap)
//===================================================================
{
	this(new ewe.io.StreamAdapter(output),level,noWrap);
}
/**
* Don't call this after starting to write output!
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
protected native int deflateInit(int level,boolean noWrap);
protected native int deflate(ByteArray in,ByteArray out,boolean done);
protected native int deflateEnd();
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
public boolean close()
//===================================================================
{
	try{
		if (closed) return true;
		int ret = Z_OK;
		do{
			while (byteOut.length != 0){ //There is data waiting in the output buffer.
				int sent = output.nonBlockingWrite(byteOut.data,0,byteOut.length);
				if (sent < 0) {
					deflateEnd();
					closed = true;
					return false;
				}
				outputBytes += sent;
				byteOut.length -= sent;
				if (byteOut.length != 0) ewe.sys.Vm.copyArray(byteOut.data,sent,byteOut.data,0,byteOut.length);
			}
			if (ret != Z_OK) break;
			ret = deflate(byteIn,byteOut,true);
		}while(true);
		deflateEnd();
		closed = true;
		return ret == Z_STREAM_END;
	}finally{
		if (output != null) output.close();
	}
}

public boolean flushStream() throws ewe.io.IOException {return true;}

//===================================================================
public int nonBlockingWrite(byte [] buff,int start,int count)
//===================================================================
{
	if (closed) return READWRITE_CLOSED;
	while(true){
		if (byteOut.length != 0){ //There is data waiting in the output buffer.
			int sent = output.nonBlockingWrite(byteOut.data,0,byteOut.length);
			if (sent <= 0) return sent;
			outputBytes += sent;
			byteOut.length -= sent;
			if (byteOut.length != 0) ewe.sys.Vm.copyArray(byteOut.data,sent,byteOut.data,0,byteOut.length);
			return 0;
		}
		if (count > byteIn.data.length) count = byteIn.data.length;
		ewe.sys.Vm.copyArray(buff,start,byteIn.data,0,count);
		byteIn.length = count;
		while(byteIn.length != 0){
			int ret = deflate(byteIn,byteOut,false);
			if (ret == Z_STREAM_END) {
				deflateEnd();
				closed = true;
				return READWRITE_CLOSED;
			}
			if (ret != Z_OK) {
				return READWRITE_ERROR;
			}
			while (byteOut.length != 0){ //There is data waiting in the output buffer.
				int sent = output.nonBlockingWrite(byteOut.data,0,byteOut.length);
				if (sent < 0) return sent;
				if (sent != 0) {
					outputBytes += sent;
					byteOut.length -= sent;
					if (byteOut.length != 0) ewe.sys.Vm.copyArray(byteOut.data,sent,byteOut.data,0,byteOut.length);
				}else{
					nap();
				}
			}
		}
		//ewe.sys.Vm.debug("Wrote: "+count,0);
		inputBytes += count;
		return count;
	}
}

//##################################################################
}
//##################################################################

