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
package ewe.sys;

/**
* This class can be thrown by a method if its operations, under some circumstances
* may take a long time. Instead of hanging the calling thread, it can throw this
* exception, which contains a Handle that can be used to monitor the state of the
* operation and retrieve the results.
* <p>However Handles cannot be used by non-mThread threads and so a static method
* is provided that will provide a SlowTaskException ONLY if the current thread is a
* mThread thread. The method that wants to throw such an exception can then call this
* static method and check if a SlowTaskException was returned. If one was, then it can
* start up a new Thread to perform the task, and use the handle to report the progress
* of the operation. If a SlowTaskException was not returned, then it should just go ahead
* and do the task as normal.
**/
//##################################################################
public class SlowTaskException extends RuntimeException {
//##################################################################

private Handle handle;

//===================================================================
public SlowTaskException(Handle h)
//===================================================================
{
	super();
	handle = h;
}
//===================================================================
public SlowTaskException(String message,Handle h)
//===================================================================
{
	super(message);
	handle = h;
}
/**
 * Get the handle for the task being performed.
 */
//===================================================================
public Handle getHandle()
//===================================================================
{
	return handle;
}
/**
 * This returns a new SlowTaskException ONLY if the calling thread is an mThread thread.
 * @param message An optional message for the exception.
 * @return a new SlowTaskException ONLY if the calling thread is an mThread thread.
 */
//===================================================================
public static SlowTaskException getNew(String message)
//===================================================================
{
	return getNew(message,null);
}
/**
 * This returns a new SlowTaskException ONLY if the calling thread is an mThread thread.
 * @param message An optional message for the exception.
 * @param h A Handle to be used for the task.
 * @return a new SlowTaskException ONLY if the calling thread is an mThread thread.
*/
//===================================================================
public static SlowTaskException getNew(String message,Handle h)
//===================================================================
{
	if (Coroutine.getCurrent() == null) return null;
	if (h == null) h = new Handle();
	return message == null ? new SlowTaskException(h) : new SlowTaskException(message,h);
}
/**
 * This returns a new SlowTaskException ONLY if the calling thread is an mThread thread.
 * @return a new SlowTaskException ONLY if the calling thread is an mThread thread.
 */
//===================================================================
public static SlowTaskException getNew()
//===================================================================
{
	return getNew(null);
}
//===================================================================
public SlowTaskException(Throwable cause) {super(cause);}
//===================================================================
public SlowTaskException(String message,Throwable cause) {super(message,cause);}
//===================================================================

//##################################################################
}
//##################################################################

