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
* Use this to implement a RandomAccessStream object. To do this you must override:
* setStreamLength(), getStreamLength(), seekPosition(), tellPosition() and the other
* methods specified in StreamObject.
**/

//##################################################################
public abstract class RandomStreamObject extends StreamObject implements RandomAccessStream{
//##################################################################
//
// Do not add instance variables to this class.
//
//===================================================================
public boolean setStreamLength(long length) throws IOException
//===================================================================
{
	throw new IOException("The Stream length could not be set!");
}
//===================================================================
public void setLength(long length) throws IOException
//===================================================================
{
	while(true){
		if (setStreamLength(length)) return;
		nap();
	}
}
//===================================================================
public long length() throws IOException
//===================================================================
{
	while(true){
		long got = getStreamLength();
		if (got != -1) return got;
		nap();
	}
}
//===================================================================
public int getLength()
//===================================================================
{
	try{
		return (int)length();
	}catch(IOException e){
		error = e.getMessage();
		return 0;
	}
}
//===================================================================
public long tell() throws IOException
//===================================================================
{
	while(true){
		long got = tellPosition();
		if (got != -1) return got;
		nap();
	}
}
//===================================================================
public int getFilePosition()
//===================================================================
{
	try{
		return (int)tell();
	}catch(IOException e){
		error = e.getMessage();
		return 0;
	}
}
//===================================================================
public void seek(long position) throws IOException
//===================================================================
{
	while(true){
		if (seekPosition(position)) return;
		nap();
	}
}
//===================================================================
public boolean seek(int position)
//===================================================================
{
	try{
		seek((long)position);
		return true;
	}catch(IOException e){
		error = e.getMessage();
		return false;
	}
}
/**
 * Tell the Stream to move to the specific position. This is a non-blocking call.
 * @param pos The position to seek to.
 * @return true if the seek completed successfully, false if the seek did not complete yet.
 * @exception IOException if an error occured during the seek.
 */
//===================================================================
public boolean seekPosition(long pos) throws IOException
//===================================================================
{
	if (!seek((int)pos)) throwIOException(null);
	return true;
}
/**
 * Retrieve the file position. This is non-blocking
 * @return the position of the stream or -1 if the position is not known yet.
 * @exception IOException if an error occured while getting the position.
 */
//===================================================================
public long tellPosition() throws IOException
//===================================================================
{
	long got = getFilePosition();
	if (got < 0) throwIOException(null);
	return got;
}
/**
 * Gets the length of the open stream. This is non-blocking.
 * @return the length of the stream or -1 if the length is not known yet.
 * @exception IOException if an error occured while getting the length.
 */
//===================================================================
public long getStreamLength() throws IOException
//===================================================================
{
	long got = getLength();
	if (got < 0) throwIOException(null);
	return got;
}
//===================================================================
public int nonBlockingRead(long location,byte[] dest,int offset,int length) throws IOException
//===================================================================
{
	if (!seekPosition(location)) return 0;
	int ret = nonBlockingRead(dest,offset,length);
	if (ret < -1) throwIOException(null);
	return ret;
}
//===================================================================
public int nonBlockingWrite(long location,byte[] src,int offset,int length) throws IOException
//===================================================================
{
	if (!seekPosition(location)) return 0;
	int ret = nonBlockingWrite(src,offset,length);
	if (ret < -0) throwIOException(null);
	return ret;
}
//===================================================================
public RandomStream toRandomStream()
//===================================================================
{
	return new RandomStream(this);
}

//##################################################################
}
//##################################################################

