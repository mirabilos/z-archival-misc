/*********************************************************************************
 *  Ewe Virtual Machine - Version 1.14, January 24, 2002                         *
 *  Copyright (C) 1999-2002 Michael L Brereton <michael_brereton@ewesoft.com>    *
 *  Copyright (C) 1998, 1999 Wabasoft <www.wabasoft.com>                         *
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
package ewe.sys;
import ewe.fx.*;
import ewe.io.*;

//##################################################################
class VmDebugStream extends Writer{
//##################################################################

private StringBuffer buff = new StringBuffer();
private boolean closed;
/**
* Write a number of bytes of data to the output stream. This will block until all the bytes are written.
* @param buffer the source buffer for the data.
* @param start The start offset in the buffer.
* @param length The number of bytes to write.
* @exception IOException if an I/O error occurs during writing.
*/
//===================================================================
public void write(char buffer[],int start,int length) throws IOException
//===================================================================
{
	if (closed) throw new IOException();
	for (int i = start+length-1; i>=start; i--){
		if (buffer[i] == '\n'){
			StringBuffer toGo = new StringBuffer();
			toGo.append(buff);
			buff.setLength(0);
			if (i > start) toGo.append(buffer,start,(i-start));
			Vm.debug(toGo.toString());
			if (i < start+length-1) {
				buff.append(buffer,i+1,length-i-1);
			}
			return;
		}
	}
	buff.append(buffer,start,length);
}
//===================================================================
public void close() throws IOException
//===================================================================
{
	if (buff.length() != 0) Vm.debug(buff.toString());
	buff.setLength(0);
	closed = true;
}
//===================================================================
public void flush() throws IOException
//===================================================================
{

}
//##################################################################
}
//##################################################################

