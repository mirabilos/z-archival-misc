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
import ewe.reflect.*;
/**
* A Coroutine is a non-preemptive implementation of a multi-threading. Only
* one Coroutine will ever by running at a time and control is only passed to
* other Coroutines when Coroutine.sleep(int millis) is called.
* <p>
* Ewe is single-threaded and only one callback or coroutine thread
* is ever running at one time (although the Vm itself may have multiple threads
* servicing these various functions). However callback threads are
* NOT Coroutine threads.
	<p>
	Avoid creating Coroutines directly - instead use the ewe.sys.mThread object instead. This
	object has a very similar API to a standard Java Thread and uses a Coroutine as its "engine"
	when started.
	<p>
	You have to explicitly create Coroutine threads yourself
* by calling the constructor and passing it a Runnable object.
**/
//##################################################################
public final class Coroutine implements Runnable{
//##################################################################
//Do not use or move these six.
protected int context;
protected Runnable runnable;
private Method runMethod;
private Object target;
private Object targetData;
int flags;
private int lastYield;
//=============================

final int FLAG_DAEMON = 0x1;
final int FLAG_MTHREAD = 0x2;
/**
* If this is true then any uncaught throwable/exception that causes the Coroutine to exit
* will be displayed. By default it is false.
**/
public boolean showExceptionTrace = false;
/**
* @deprecated - use an mThread object instead.
**/
/*
//===================================================================
public Coroutine(Runnable r,int stackSize)
//===================================================================
{
	runnable = r;
	if (r instanceof mThread) {
		flags |= FLAG_MTHREAD;
		thread = (mThread)r;
		if (thread.isDaemon()) flags |= FLAG_DAEMON;
	}
	_nativeCreate(stackSize);
}
*/
//===================================================================
public Coroutine(Runnable r,int stackSize)
//===================================================================
{
	runnable = r instanceof mThread ? r : new mThread(this,r);
	flags |= FLAG_MTHREAD;
	thread = (mThread)runnable;
	if (thread.isDaemon()) flags |= FLAG_DAEMON;
	_nativeCreate(stackSize);
}
/**
* @deprecated use an mThread object instead.
**/
//===================================================================
public Coroutine(Runnable r) {this(r,100);}
//===================================================================
/**
* @deprecated use an mThread object instead.
**/
//===================================================================
public Coroutine(final Object target,final ewe.reflect.Method runMethod,final Object targetData)
//===================================================================
{
	this(new Runnable(){
		public void run(){
			try{
				Wrapper [] wr = new Wrapper[1];
				wr[0] = new Wrapper().setObject(targetData);
				runMethod.invoke(target,wr,new Wrapper());
				if (runMethod.invocationError != null) throw runMethod.invocationError;
			}catch(Throwable t){
				t.printStackTrace();
			}
		}});
	/*
	if (runMethod == null) return;
	setup(target,runMethod,targetData);
	*/
}
/**
* @deprecated use an mThread object instead.
**/
//===================================================================
public Coroutine(Object target,String methodName,Object targetData)
//===================================================================
{
	this (target,Reflect.getForObject(target).getMethod(methodName,"(Ljava/lang/Object;)V",0),targetData);
	/*
	Reflect r = Reflect.getForObject(target);
	if (r == null) return;
	Method md = r.getMethod(methodName,"(Ljava/lang/Object;)V",0);
	if (md == null) return;
	setup(target,md,targetData);
	*/
}

//===================================================================
private void setup(Object target,ewe.reflect.Method runMethod,Object targetData)
//===================================================================
{
	this.target = target;
	this.targetData = targetData;
	this.runMethod = runMethod;
	runnable = this;
	_nativeCreate(100);
}
/**
* This will actually not get called!
**/
//===================================================================
public final void run(){}
//===================================================================
/* This cannot be done. Cannot "wait" inside of a native method.
{
	if ((target != null) && (runMethod != null)){
		Wrapper parameters[] = new Wrapper[1];
		parameters[0]= new Wrapper();
		parameters[0].setObject(targetData);
		runMethod.invoke(target,parameters,null);
	}
}
*/
//===================================================================
protected final native void _nativeCreate(int stackSize);
//===================================================================

/**
* Returns whether the Coroutine is still running. i.e. the run() method of
* the runnable object assigned to the Coroutine has not yet exited.
**/
//===================================================================
public final boolean isRunning() {return context != 0;}
//===================================================================
/**
* Find the Coroutine which is running the current thread. If this thread
* is not being run by a Coroutine, it will return null.
**/
//===================================================================
public static final native Coroutine getCurrent();
//===================================================================

//===================================================================
public static final void yield()
//===================================================================
{
	if (getCurrent() == null) return;
	sleep(0);
}
/**
* Puts the current Coroutine thread to sleep, thereby allowing other coroutines
* as well as message/timer/callback threads to run. This method is made static as
* it can ONLY be run correctly from a running Coroutine ON that same Coroutine.
*
* This method may NOT be called by a Coroutine from within a native method - or
* from within a method which has a native method in the call chain.
*
* @param howLong - The length of time in milliseconds to sleep. If howLong == -1 it will sleep
* until interrupt() is envoked on it.
* @return 1 if it slept() and then woke up after the timeout time, or 0 if it was woken by wakeup() or -1 if it was interrupted.
*/
//===================================================================
public static final native int sleep(int howLong);
//===================================================================
/**
* Tells the current Coroutine thread to sleep until the other Coroutine thread
* has exited. It returns true if the other had exited or false if it had not. If
* timeout is -1 then it will wait forever. This waiting will NOT be affected by
* wakeup(), but will be by interrupt();
* @param other The other Coroutine to wait for.
* @param a time in milliseconds to wait for. If it is -1 then it will wait forever or
until interrupted
* @return
<br>1 if the other Coroutine exited.
<br>0 if the a timeout occured before the other Coroutine exited.
<br>-1 if an interrupt() was called on this Coroutine.
*/
//===================================================================
public static final native int join(Coroutine other,int millis);
//===================================================================
/**
* Tells the Coroutine to stop sleeping if it was in a sleep(). Otherwise, if it
* were in join() or it had exited it will have no effect.
* @deprecated - use interrupt() instead, interrupt() will interrupt join() calls.
*/
//===================================================================
public final native void wakeup();
//===================================================================
/**
* Interrupts the Coroutine if it was in a sleep() or a join(). Otherwise, if it
* were had exited it will have no effect.
**/
//===================================================================
public final native void interrupt();
//===================================================================

/**
* This returns a count of all running Coroutines. Coroutines which are not running
* are not counted and will eventually be garbage collected.
**/
//===================================================================
public static final native int count();
//===================================================================

private int napCount = 0;
private static int sysNapCount = 0;
/**
* This calls sleep(time) every "iterations" calls of this method.
**/
//===================================================================
public static final int nap(int iterations,int time)
//===================================================================
{
	//ewe.sys.Vm.debug(iterations+", s = "+sysNapCount);
	if (iterations <= 0) return 0;
	Coroutine c = Coroutine.getCurrent();
	if (c == null) {
		sysNapCount++;
		if (sysNapCount >= iterations){
			sysNapCount = 0;
			ewe.sys.Vm.sleep(time);
		}
		return 0;
	}
	//ewe.sys.Vm.debug(iterations+", c = "+c.napCount);
	c.napCount++;
	if (c.napCount >= iterations) {
		c.napCount = 0;
		//ewe.sys.Vm.debug("Napping: "+time);
		if (time != 0) return sleep(time);
		yield();
		return 1;
	}
	return 0;
}

//===================================================================
static void yield(int everyMillis)
//===================================================================
{
	Coroutine c = getCurrent();
	if (c == null) return;
	int now = ewe.sys.Vm.getTimeStamp();
	if (now-c.lastYield > everyMillis || now < c.lastYield) yield();
}

private static mThread nonCoroutine;
private mThread thread;

//===================================================================
static mThread getThread()
//===================================================================
{
	Coroutine c = getCurrent();
	if (c == null) {
		if (nonCoroutine == null) nonCoroutine = new mThread();
		return nonCoroutine;
	}
	if (c.thread != null) return c.thread;
	if (c.runnable instanceof mThread)
		return c.thread = (mThread)c.runnable;
	c.thread = new mThread();
	c.thread.myCoroutine = c;
	return c.thread;
}
//##################################################################
}
//##################################################################

