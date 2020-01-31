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

//##################################################################
public interface Task{
//##################################################################
/**
* This returns the handle associated with the Task.
**/
public Handle getHandle();
/**
* This starts the Task. It should create a handle and return it. If it is called
* more than once, only the first call should start the Task. Subsequent calls
* should just return the already created handle.
**/
public Handle startTask();
/**
* This requests the Task to stop. The Task will not necessarily top because
* of this call. The Stopped flag will be set in the status of the handle when the
* Task is actually stopped.
**/
public Handle stopTask(int reason);
/**
* This should only be called from the Handle.
* This gives the opportunity for the application or VM to execute part of the
* Task. It must NEVER make any blocking calls.
*
* If this Task represents a true thread in the application or VM, then this
* method should just return immediately.
**/
//public void doNonBlockingOperation();
/**
* This should only be called from the Handle.
* This gives the opportunity for the application or VM to execute part of the
* Task. If it makes any blocking calls it should make best effort attempts not
* to block longer than the timeout specified.
*
* If this Task represents a true thread in the application or VM, then this
* method may just return (not Taskor friendly) or call an appropriate VM call
* which does Taskor friendly waiting until some part of the Task has been
* done.
**/
//public void doBlockingOperation(TimeOut t);

//##################################################################
}
//##################################################################

