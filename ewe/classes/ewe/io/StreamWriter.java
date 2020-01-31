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

/**
* @deprecated - use PrintWriter instead.
**/

//##################################################################
public class StreamWriter extends ewe.util.Errorable{
//##################################################################

Stream stream;
OutputStream output;
boolean closed;

/**
* This is the "UTF8" constant string.
**/
//===================================================================
public static final String UTF8 = "UTF8";
//===================================================================

/**
* Set this to be a text encoding form. Either null for pure ASCII or "UTF8" for Java UTF8
* encoding. By default it is UTF8.
**/
//===================================================================
public String encoding = UTF8;
//===================================================================
/**

* This determines whether a CR is sent before a LF character. By default it is true.
**/
//===================================================================
public boolean useCR = ((ewe.sys.Vm.getParameter(ewe.sys.Vm.VM_FLAGS) & ewe.sys.Vm.VM_FLAG_NO_CR) == 0);
//===================================================================

//-------------------------------------------------------------------
private static final byte [] crlf = {(byte)13,(byte)10};
//-------------------------------------------------------------------
/**
* Output a blank line.
**/
//===================================================================
public boolean println()
//===================================================================
{
 	try{
		if (stream != null)
			if (useCR)
				stream.write(crlf,0,2);
			else
				stream.write(crlf,1,1);
		else
			if (useCR)
				output.write(crlf,0,2);
			else
				output.write(crlf,1,1);
	}catch(IOException e){
		return false;
	}
	return true;
}
/**
* Output a String.
**/
//===================================================================
public boolean print(String what)
//===================================================================
{
	if (what == null) return true;
	if (what.length() == 0) return true;
	if (encoding == null) encoding = new String();
	byte [] b;
 	if (encoding.equals(UTF8))
		b = ewe.util.Utils.encodeJavaUtf8String(what);
	else
		b = ewe.util.mString.toAscii(what);

	int sent = 0;
 	try{
		if (stream != null){
			stream.write(b,0,b.length);
			sent = b.length;
		}else {
			output.write(b,0,b.length);
			sent = b.length;
		}
	}catch(IOException e){
		sent = -1;
	}
	return (sent == b.length);
}
/**
* Output a String followed by CR/LF.
**/
//===================================================================
public boolean println(String what)
//===================================================================
{
	if (!print(what)) return false;
	return println();
}

//===================================================================
public StreamWriter(Stream s)
//===================================================================
{
	stream = s;
}
//===================================================================
public StreamWriter(String path,boolean append) throws ewe.io.IOException
//===================================================================
{
	this(ewe.sys.Vm.newFileObject().getNew(path).toWritableStream(append));
}
//===================================================================
public StreamWriter(OutputStream out)
//===================================================================
{
	output = out;
}
//===================================================================
public boolean isOpen()
//===================================================================
{
	if (output != null) return !closed;
	if (stream == null) return false;
	return stream.isOpen();
}
//===================================================================
public boolean close()
//===================================================================
{
	if (stream != null) return stream.close();
	if (output != null)
	try{
		output.close();
	}catch(IOException e){
		return false;
	}
	return true;
}
//===================================================================
public boolean flush()
//===================================================================
{
	try{
		if (stream != null) stream.flush();
		else if (output != null) output.flush();
		return true;
	}catch(IOException e){
		return false;
	}
}

//##################################################################
}
//##################################################################

