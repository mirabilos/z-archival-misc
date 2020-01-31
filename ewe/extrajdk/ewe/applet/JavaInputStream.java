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
package ewe.applet;

//##################################################################
public class JavaInputStream extends ewe.io.StreamObject{
//##################################################################

java.io.InputStream in;
public InputBuffer buffer;

//-------------------------------------------------------------------
protected void doClose() throws ewe.io.IOException
//-------------------------------------------------------------------
{
	try{
		in.close();
	}catch(Exception e){
		throw new ewe.io.IOException(e.getMessage());
	}
}
//===================================================================
public JavaInputStream(java.io.InputStream in)
//===================================================================
{
	this.in = in;
	buffer = new InputBuffer(){
		protected void doClose() throws ewe.io.IOException
		{
			JavaInputStream.this.doClose();
		}
		protected int doRead(byte [] bytes,int start,int count) throws ewe.io.IOException{
			try{
				//ewe.sys.Vm.debug("Reading...");
				int ret = JavaInputStream.this.in.read(bytes,start,count);
				//ewe.sys.Vm.debug("Read: "+ret);
				return ret;
			}catch(Exception e){
				//e.printStackTrace();
				throw new ewe.io.IOException(e.getMessage());
			}
		}
	};
	buffer.startRunning();
}

//===================================================================
public int nonBlockingRead(byte[] buff,
//===================================================================
                           int start,
                           int count)
{
	try{
		return buffer.readBytes(buff,start,count);
	}catch(ewe.io.IOException e){
		error = e.getMessage();
		return -2;
	}
}
//===================================================================
public int nonBlockingWrite(byte[] buff,
//===================================================================
                           int start,
                           int count)
{
	error = "Cannot write to this stream.";
	return -2;
}

//===================================================================
public boolean flushStream() throws ewe.io.IOException
//===================================================================
{
	return true;
}
//===================================================================
public boolean isOpen() {return in != null;}
//===================================================================
public boolean closeStream() throws ewe.io.IOException
//===================================================================
{
	if (in == null) return true;
	try{
		//ewe.sys.Vm.debug("Closing buffer!");
		buffer.close();
		in = null;
		return true;
	}catch(Exception e){
		throw new ewe.io.IOException(e.getMessage());
	}
}

//##################################################################
}
//##################################################################
