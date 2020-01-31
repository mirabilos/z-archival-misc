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
* A BasicStream is a non-blocking Streaming interface. The only blocking method in it
* is close(). You would hardly use this interface directly, it is generally only used
* by native methods that require a non-blocking interface, since native methods cannot
* pause a running Thread without blocking the entire VM.
**/
//##################################################################
public interface BasicStream{
//##################################################################
/**
* This is the non-blocking read operation. It should never attempt to
* wait() or sleep() in a Coroutine. It should return as quickly as possible.
* This makes it safe to be called from within a native method.
* Note that this should NEVER be called with a count of zero.
* @param buff Destination byte array to hold incoming data.
* @param start Starting index in buff for incoming data.
* @param count Maximum number of bytes to read - should never be zero.
* @return
*  greater than 0 = Number of bytes read. <br>
*  0 = No bytes available to read at this time.<br>
* -1 = Stream end reached no further bytes to read.<br>
* -2 = IO Error.<br>
**/
//===================================================================
public int nonBlockingRead(byte []buff,int start,int count);
//===================================================================
/**
* This is the non-blocking write operation. It should never attempt to
* wait() or sleep() in a Coroutine. It should return as quickly as possible.
* This makes it safe to be called from within a native method.
* @param buff Source byte array holding data to be written.
* @param start Starting index in buff for data to be written.
* @param count Number of bytes to write - should never be zero.
* @return
* greater than 0 = Number of bytes actually written.<br>
*  0 = No bytes could be written yet - but the stream is still open.<br>
* -1 = Stream has been closed - no further writes are possible.<br>
* -2 = IO error .<br>
**/
//===================================================================
public int nonBlockingWrite(byte []buff,int start,int count);
//===================================================================
/**
 * Closes the stream. Returns true if the operation is successful
 * and false otherwise.
 */
//===================================================================
public boolean close();
//===================================================================
/**
* Returns if the stream is open or not.
**/
//===================================================================
public boolean isOpen();
//===================================================================
/**
* This is non-blocking. It returns true if the flush completed, false if it did not,
* or throws an exception on error.
**/
//===================================================================
public boolean flushStream() throws ewe.io.IOException;
//===================================================================
/**
* This is non-blocking. It returns true if the close completed, false if it did not,
* or throws an exception on error.
**/
//===================================================================
public boolean closeStream() throws ewe.io.IOException;
//===================================================================

//##################################################################
}
//##################################################################

