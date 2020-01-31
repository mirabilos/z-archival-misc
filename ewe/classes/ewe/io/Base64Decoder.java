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
import ewe.util.*;

//##################################################################
public class Base64Decoder extends Base64Codec implements DataProcessor{
//##################################################################
//===================================================================
public void closeProcess(){}
//===================================================================
public int getBlockSize() {return 4;}
//===================================================================
public int getMaxBlockSize() {return -1;}
//===================================================================
public ByteArray processBlock(byte [] source,int offset,int length,boolean isLast,ByteArray dest) throws IOException
//===================================================================
{
	dest = decode(source,offset,length,dest);
	if (dest == null) throw new IOException(Base64Encoder.encodingError);
	return dest;
}
//##################################################################
}
//##################################################################


