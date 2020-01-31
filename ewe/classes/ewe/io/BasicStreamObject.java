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
import ewe.sys.Handle;
import ewe.util.Errorable;

//##################################################################
public abstract class BasicStreamObject extends Errorable implements Stream, Streamable{
//##################################################################
protected boolean closed = false; //Don't add more variables.
//===================================================================
public ewe.sys.Handle toStream(boolean randomString, String mode)
//===================================================================
{
	return new ewe.sys.Handle(Handle.Succeeded,this);
}
//===================================================================
public String getName(){ return "Unnamed Stream";}
//===================================================================
//===================================================================
public int read(byte buff[]) throws IOException {return read(buff,0,buff.length);}
//===================================================================
//===================================================================
public void write(byte buff[]) throws IOException {write(buff,0,buff.length);}
//===================================================================
//===================================================================
public int read() throws IOException
//===================================================================
{
	byte inbuff[] = new byte[1];
	if (read(inbuff,0,1) == -1) return -1;
	return inbuff[0] & 0xff;
}
//===================================================================
public void write(int value) throws IOException
//===================================================================
{
	byte outbuff[] = new byte[1];
	outbuff[0] = (byte)(value & 0xff);
	write(outbuff,0,1);
}
//===================================================================
public IOException getException(String defaultText)
//===================================================================
{
	String err = error;
	if (err == null) err = defaultText;
	if (err == null) return new IOException();
	else return new IOException(err);
}
//===================================================================
public void throwIOException(String defaultText) throws IOException
//===================================================================
{
	throw getException(defaultText);
}
//===================================================================
public OutputStream toOutputStream() throws IllegalStateException
//===================================================================
{
	return new OutputStream(this);
}
//===================================================================
public InputStream toInputStream() throws IllegalStateException
//===================================================================
{
	return new InputStream(this);
}
//##################################################################
}
//##################################################################

