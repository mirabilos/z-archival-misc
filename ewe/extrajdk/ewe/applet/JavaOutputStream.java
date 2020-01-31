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
 *  produce an executable does not out itself require the executable to be        *
 *  covered by the GNU Lesser General Public License.                            *
 *                                                                               *
 *  This library and virtual machine is distributed out the hope that it will     *
 *  be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of    *
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.                         *
 *                                                                               *
 *  You should have received a copy of the License along with this software;     *
 *  if not, please download it at: www.ewesoft.com/LGPL.txt                      *
 *                                                                               *
 *********************************************************************************/
package ewe.applet;

//##################################################################
public class JavaOutputStream extends ewe.io.StreamObject{
//##################################################################

java.io.OutputStream out;
public OutputBuffer buffer;

//-------------------------------------------------------------------
protected void doClose() throws ewe.io.IOException
//-------------------------------------------------------------------
{
	try{
		JavaOutputStream.this.out.close();
	}catch(Exception e){
		throw new ewe.io.IOException(e.getMessage());
	}
}
//===================================================================
public JavaOutputStream(java.io.OutputStream out)
//===================================================================
{
	this.out = out;
	buffer = new OutputBuffer(){
		protected void doClose() throws ewe.io.IOException
		{
			JavaOutputStream.this.doClose();
		}
		protected void doWrite(byte [] bytes,int start,int count) throws ewe.io.IOException{
			try{
				JavaOutputStream.this.out.write(bytes,start,count);
				JavaOutputStream.this.out.flush();
				//ewe.sys.Vm.debug("Sent: "+count);
			}catch(Exception e){
				//e.printStackTrace();
				throw new ewe.io.IOException(e.getMessage());
			}
		}
		protected void doFlush(){}

	};
	buffer.startRunning();
}


public int nonBlockingRead(byte[] buff,
                           int start,
                           int count)
{
	error = "Cannot read from this stream.";
	return -2;
}
public int nonBlockingWrite(byte[] buff,
                           int start,
                           int count)
{
	try{
		if (buffer.writeBytes(buff,start,count))
			return count;
		else
			return 0;
	}catch(ewe.io.IOException e){
		error = e.getMessage();
		return -2;
	}
}
//===================================================================
public boolean flushStream() throws ewe.io.IOException
//===================================================================
{
	if (out == null) return true;
	buffer.flush();
	return true;
}
//===================================================================
public boolean closeStream() throws ewe.io.IOException
//===================================================================
{
	if (out == null) return true;
	try{
		buffer.flush();
		buffer.close();
		out = null;
		return true;
	}catch(Exception e){
		throw new ewe.io.IOException(e.getMessage());
	}
}

//##################################################################
}
//##################################################################
