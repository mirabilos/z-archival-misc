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
import ewe.sys.Lock;
import ewe.sys.TimeOut;

/**
* @deprecated use ewe.util.ObjectBuffer instead.
**/
//##################################################################
public class Buffer{
//##################################################################

/*
* SANITY CHECK! There must be NO blocking calls while the lock is being
* held. This allows the close() method to work correctly even when being
* called from a non-coroutine thread. i.e. the grab() method should always
* work.
*/

Lock lock = new Lock();
Object transfer;
boolean closed;

/**
* Set this true for use as an array transfer buffer. Any incoming arrays are merged with
* the waiting array. This can be used for any type of array.
**/
public boolean appendArrays = false;

//===================================================================
public Buffer(){}
//===================================================================
public Buffer(boolean appendArrays)
//===================================================================
{
	this.appendArrays = appendArrays;
}
/**
* This will put the object into the buffer and it will not return until
* it has done so, or until the timeout has expired or until the buffer has been closed.
* This can be called by a non-coroutine thread if TimeOut is TimeOut.Immediate.
**/
//===================================================================
public boolean put(Object toAdd,TimeOut howLong)
//===================================================================
{
	if (closed) return false;
	if (howLong == null) howLong = TimeOut.Forever;
	if (!lock.hold(howLong)) return false;
	else try{
	// Lock is held from this point on. Remember to release from all exit paths.
		if (appendArrays){
			transfer = ewe.util.Utils.appendArray(transfer,toAdd);
			lock.notifyAllWaiting();
			return true;
		}
		while(transfer != null)
			if (!lock.wait(howLong))
				break;
		if (transfer != null) return false;
		transfer = toAdd;
		lock.notifyAllWaiting();
	}finally{
		lock.release();
	}
	return true;
}
/**
* This will get an object from the buffer and it will not return until
* it has done so, or until the buffer has been closed, or the timeout
* has expired. This can be called by a non-coroutine thread if TimeOut is TimeOut.Immediate.
**/
//===================================================================
public Object get(TimeOut howLong)
//===================================================================
{
	if (closed) return null;
	if (howLong == null) howLong = TimeOut.Forever;
	if (!lock.hold(howLong)) return null;
	else try{
// Lock is held from this point on. Remember to release from all exit paths.
		while(transfer == null){
			if (closed) return null;
			if (!lock.wait(howLong))
				break;
		}
		if (transfer == null){
			return null;
		}
		Object ret = transfer;
		transfer = null;
		lock.notifyAllWaiting();
		return ret;
	}finally{
		lock.release();
	}
}

//===================================================================
public void close()
//===================================================================
{
	if (!lock.grab())
		throw new RuntimeException("grab() failed in Buffer.close()");
	else try{
		closed = true;
		lock.notifyAllWaiting();
	}finally{
		lock.release();
	}
}

//===================================================================
public boolean isClosed()
//===================================================================
{
	return closed;
}
//##################################################################
}
//##################################################################

