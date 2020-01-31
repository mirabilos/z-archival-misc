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
* This is used for asynchronous IO operations. See the Stream interface for
* examples.
**/
//##################################################################
public class IOHandle extends ewe.sys.Handle{
//##################################################################
/**
* This is a general IO_ERROR.
**/
public static final int IO_ERROR = 1;
/**
* This is returned during a read operation when a read request is made but
* the end of the input stream has been reached.
**/
public static final int STREAM_END_REACHED = 2;
/**
* This is returned during a read/write operation when a read/write request is made but
* the stream has been closed.
**/
public static final int STREAM_CLOSED = 3;
/**
* This is returned during a read/write operation when a read/write request is made but
* the operation has been aborted by a call to stop() on this handle.
**/
public static final int IO_ABORTED = 4;
/**
* This reports the number of bytes transferred.
**/
public int bytesTransferred;
/**
* This can be used to request an abort - but it is not for general use.
**/
public boolean pleaseAbort;
/**
* This is used in the case of RandomAccessStreams.
**/
public long ioLocation = -1;

//===================================================================
public void stop(int reason)
//===================================================================
{
	pleaseAbort = true;
	super.stop(reason);
}
//##################################################################
}
//##################################################################

