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
public class TimeOut{
//##################################################################
/**
* This is the value for an infinite timeout in milliseconds, equal to -1.
**/
public static final int Infinite = -1;
/**
* This is the value for a zero timeout in milliseconds, equal to 0.
**/
public static final int Zero = 0;

protected int started, interval;
public boolean expired = false;
//==================================================================
public static TimeOut Forever, Immediate;
static {
	Forever = new TimeOut(Infinite);
	Immediate = new TimeOut(Zero);
}
//==================================================================

//===================================================================
public static int now() {return Vm.getTimeStamp() & 0x3fffffff;}
//===================================================================

//-------------------------------------------------------------------
private static int now(int from)
//-------------------------------------------------------------------
{
	int n = now();
	if (n < from) n += 0x40000000;
	return n;
}
//===================================================================
public TimeOut reset() {started = now(); expired = false; return this;}
//===================================================================

/**
* Has this TimeOut expired yet?
**/
//===================================================================
public boolean hasExpired()
//===================================================================
{
	if (interval == 0) return true;
	if (interval < 0) return false;
	if (expired) return true;
	return (expired = now(started)-started >= interval);
}
/**
* How many milliseconds remaining? If it returns -1 it means that it
* will never timeout.
**/
//===================================================================
public int remaining()
//===================================================================
{
	if (interval == 0) return 0;
	if (interval < 0) return Infinite;
	if (expired) return 0;
	int r = interval+started-now(started);
	if (r < 0) r = 0;
	return r;
}
/**
* How many milliseconds has elapsed since the TimeOut was started/reset.
**/
//===================================================================
public int elapsed()
//===================================================================
{
	return now(started)-started;
}
/**
* Force it to expire.
**/
//===================================================================
public void expire() {expired = true;}
//===================================================================
/**
* Create a timeout with the specified number of milliseconds.
**/
//===================================================================
public TimeOut(int time)
//===================================================================
{
	interval = time;
	if (interval < 0) interval = Infinite;
	if (interval > 0x3fffffff)
		throw new RuntimeException("TimeOut interval of: "+interval+" is too large for this OS.");
	reset();
}
/**
* Get a copy of this TimeOut, i.e. one which has the same interval.
**/
//===================================================================
public TimeOut getNew()
//===================================================================
{
	return new TimeOut(interval);
}
/**
* Causes an error in Waba - use blockUntilExpired().
*
* This will block the current thread until the TimeOut expires. It will
* not block any other threads.
*
* For single threaded applications such as Waba, calling this method should result
* in an Application error, since it WILL in fact block the application.
* This should only be implemented on multi-threaded applications like pure Java.
*
* Calling this method when the TimeOut expiration is set to Infinite should also
* generate an application error.
**/
//===================================================================
//public void waitUntilExpired()
//===================================================================
//{
//	Vm.applicationError("TimeOut.waitUntilExpired() not implemented on Waba.");
//}
/**
* This will block the current thread until the TimeOut expires. It -MAY-
* block any other threads.
*
* For single threaded applications such as Waba, calling this method WILL block
* other threads (i.e. the entire application). If the underlying VM is multithreaded
* it should not block any OS threads.
**/
//===================================================================
//public void blockUntilExpired()
//===================================================================
//{
//	if (interval < 0) {
		//Vm.applicationError("Attempting to blockUntilExpired on an Infinite TimeOut.");
		//return;
	//}
	//while(!hasExpired())
//		Vm.sleep(remaining());
//}

//===================================================================
public void wait(Object obj)
//===================================================================
{
	throw new RuntimeException("Cannot wait() on an object in Ewe.");
}

//===================================================================
public void multiThreadedWait(Object obj)
//===================================================================
{
	if (obj == null) return;
	int time = (int)remaining();
	if (time == 0) return;
	try{
	/*
		if (time == Infinite) obj.wait();
		else obj.wait(time);
	*/
	}catch(Exception e){}
}

//##################################################################
}
//##################################################################


