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
* This class is used to create a "sub-stream" of data int another Stream. The
* partial stream can limit the amount of data which can be written to the original
* Stream. Note the following:
*
* 1. Calling close() on a PartialOutputStream does not close the original stream.
* 2. Setting a limit of -1 will not impose any limit on the number of bytes which
* can be read.
* 3. Input begins at the current point in the source input stream.
**/

//##################################################################
public class PartialOutputStream extends StreamAdapter{
//##################################################################
int limit;
int filepos;
/**
* Creates a new PartialOutputStream with no limit.
**/
//===================================================================
public PartialOutputStream(BasicStream output)
//===================================================================
{
	this(output,-1);
}
/**
* Creates a new PartialOutputStream with the specified limit. If the limit
* is -1, then there will be no limit imposed
**/
//===================================================================
public PartialOutputStream(BasicStream output,int limit)
//===================================================================
{
	super(output);
	this.limit = limit;
	filepos = -1;
}
//===================================================================
public PartialOutputStream(RandomAccessStream raf, int start, int limit)
//===================================================================
{
 this(raf,limit);
 if (start < 0) start = 0;
 filepos = start;
}
//===================================================================
public int nonBlockingRead(byte [] buff,int start,int length) {return READWRITE_ERROR;}
//===================================================================

//===================================================================
public int nonBlockingWrite(byte [] buff,int offset,int count)
//===================================================================
{
	if (limit == 0) return READWRITE_CLOSED;
	if (limit < -1) return READWRITE_ERROR;
	if (limit != -1)
		if (count > limit) count = limit;
	if (filepos >= 0)
		((RandomAccessStream)stream).seek(filepos);
	int got = super.nonBlockingWrite(buff,offset,count);
	if (got == 0) return 0;
	if (got == READWRITE_CLOSED){
		limit = 0;
		return READWRITE_CLOSED;
	}
	if (got > 0) {
		if (limit > 0){
			limit -= got;
			if (limit < 0) limit = 0;
		}
		if (filepos >= 0) filepos += got;
		return got;
	}
	// An error occured.
	limit = -2;
	return READWRITE_ERROR;
}
/**
* This will not close the underlying stream.
**/
//===================================================================
public boolean closeStream() throws ewe.io.IOException
//===================================================================
{
	return closed = true;
}
//===================================================================
public boolean isOpen()
//===================================================================
{
	return !closed;
}

//===================================================================
public boolean pushback(byte [] bytes,int start,int count) {return false;}
//===================================================================

//##################################################################
}
//##################################################################

