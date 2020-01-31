/* $MirOS: contrib/hosted/ewe/extrajdk/ewe/sys/Coroutine.java,v 1.2 2008/05/02 20:52:04 tg Exp $ */

/*********************************************************************************
 *  Ewe Virtual Machine - Version 1.14, January 24, 2002                         *
 *  Copyright (c) 2008 Thorsten “mirabilos” Glaser                               *
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
//##################################################################
public final class Coroutine extends Thread{
//##################################################################
//Do not use or move these two.
protected int context;
protected Runnable runnable;
private int lastYield;

public static Object lockObject = new Lock();
static {
	((Lock)lockObject).isEweLock = true;
}

protected boolean woke = false, interrupted = false;
protected static int running = 0;
int flags;
//=============================

final int FLAG_DAEMON = 0x1;
final int FLAG_MTHREAD = 0x1;

/**
* If this is true then any uncaught throwable/exception that causes the Coroutine to exit
* will be displayed. By default it is false.
**/
public boolean showExceptionTrace = false;

//-------------------------------------------------------------------
private void _nativeCreate(int stackSize) { setDaemon((flags & FLAG_DAEMON) != 0); start();}
//-------------------------------------------------------------------

//===================================================================
public Coroutine(Runnable r,int stackSize)
//===================================================================
{
/*
	runnable = r;
	if (r instanceof mThread) {
		thread = (mThread) r;
		if (thread.isDaemon()) flags |= FLAG_DAEMON;
	}
	_nativeCreate(stackSize);
*/
	runnable = r instanceof mThread ? r : new mThread(this,r);
	flags |= FLAG_MTHREAD;
	thread = (mThread)runnable;
	if (thread.isDaemon()) flags |= FLAG_DAEMON;
	_nativeCreate(stackSize);
}
/**
* Create a Coroutine with the default stack size.
**/
//===================================================================
public Coroutine(Runnable r) {this(r,100);}
//===================================================================

/**
* Creates a Coroutine which whill run the specified runMethod on the specified
* target object. The targetData parameter is passed directly to the target object and is
* not used by the Coroutine. The target method must take one argument (i.e. the targetData).
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
//setup(target,runMethod,targetData);
}
/**
* Creates a Coroutine which whill run the specified runMethod on the specified
* target object. The targetData parameter is passed directly to the target object and is
* not used by the Coroutine. The target method must be void and take one object argument (i.e. the targetData).
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

private Object target;
private Method runMethod;
private Object targetData;

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

//===================================================================
public void run()
//===================================================================
{
	synchronized(lockObject){
		try{
			try{
				running++;
				if (runnable != this){
					runnable.run();
				}
				else{
					if (runMethod != null){
						Wrapper parameters[] = new Wrapper[1];
						parameters[0]= new Wrapper();
						parameters[0].setObject(targetData);
						JavaMethod.invoke(runMethod,target,parameters,null);
						if (runMethod.invocationError != null) throw runMethod.invocationError;
						//if (showExceptionTrace && runMethod.invocationError != null)
							//runMethod.invocationError.printStackTrace();
					}
				}
			}finally{
				lockObject.notifyAll();
				running--;
			}
		}catch(Throwable t){
			if (runnable instanceof mThread)
				((mThread)runnable).exceptionThrown(t);
			else if (showExceptionTrace)
				t.printStackTrace();
		}finally{
			if (runnable instanceof mThread)
				((mThread)runnable).ended();
		}
	}
}
//===================================================================
public final boolean isRunning() {return isAlive();}
//===================================================================
/**
* Find the Coroutine which is running the current thread. If this thread
* is not being run by a Coroutine, it will return null.
**/
//===================================================================
public static Coroutine getCurrent()
//===================================================================
{
	Thread t = currentThread();
	if (t instanceof Coroutine) return (Coroutine)t;
	return null;
}
/**
* Puts the current Coroutine thread to sleep, thereby allowing other coroutines
* as well as message/timer/callback threads to run. This method is made static as
* it can ONLY be run correctly from a running Coroutine ON that same Coroutine.
*
* @param howLong - The length of time to sleep. If howLong == -1 it will sleep
* until wakeup() is envoked on it.
**/
//===================================================================
public static final int sleep(int howLong)
//===================================================================
{
	checkInCoroutine("Coroutine.sleep() can only be called from within a running Coroutine");
	int waitTime = howLong;
	if (howLong == 0) howLong = 1;
	Coroutine t = getCurrent();
	t.lastYield = ewe.sys.Vm.getTimeStamp();
	t.woke = t.interrupted = false;
	long quit = System.currentTimeMillis();
	long now = quit;
	if (waitTime > 0) quit += waitTime;
	while(true){
		try{
			if (howLong < 0) lockObject.wait();
			else lockObject.wait(howLong);
		}catch(Exception e){}
		if (t.woke || t.interrupted) break;
		if (howLong < 0) continue;
		if (waitTime == 0) break;
		howLong = (int)(quit-System.currentTimeMillis());
		if (howLong <= 0) break;
	}
	int ret = 1;
	if (t.woke) ret = 0;
	if (t.interrupted) ret = -1;
	t.interrupted = t.woke = false;
	return ret;
}
//===================================================================
public static final void yield()
//===================================================================
{
	Coroutine t = getCurrent();
	if (t == null) return;
	sleep(0);
	/*
	t.woke = t.interrupted = false;
	long quit = System.currentTimeMillis();
	long now = quit;
	if (howLong > 0) quit += howLong;
	while(true){
		try{
			if (howLong < 0) lockObject.wait();
			else lockObject.wait(howLong);
		}catch(Exception e){}
		if (t.woke || t.interrupted) break;
		if (howLong < 0) continue;
		howLong = (int)(quit-System.currentTimeMillis());
		if (howLong <= 0) break;
	}
	int ret = 1;
	if (t.woke) ret = 0;
	if (t.interrupted) ret = -1;
	t.interrupted = t.woke = false;
	return ret;
	*/
}

//-------------------------------------------------------------------
private static void checkInCoroutine(String message)
//-------------------------------------------------------------------
{
	if (Coroutine.getCurrent() == null) throw new RuntimeException(message);
}
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
public static final int join(Coroutine other,int timeout)
//===================================================================
{
	checkInCoroutine("Coroutine.join() can only be called from within a running Coroutine");
	Coroutine t = getCurrent();
	t.woke = t.interrupted = false;
	if (other == getCurrent()) throw new RuntimeException("a Coroutine cannot join itself");
	if (other == null) throw new NullPointerException();
	if (timeout == 0) timeout = 1;
	long quit = System.currentTimeMillis();
	if (timeout >= 0) quit += timeout;
	while(other.isAlive()){
		try{
			if (timeout < 0) lockObject.wait();
			else lockObject.wait(timeout);
		}catch(Exception e){}
		if (t.interrupted || t.woke) break;
		if (!other.isAlive()) break;
		if (timeout < 0) continue;
		if (quit <= System.currentTimeMillis()) return 0;
	}
	int ret = (t.interrupted || t.woke) ? -1 : 1;
	t.interrupted = t.woke = false;
	return ret;
}
/**
* Tells the Coroutine to stop sleeping if it was in a sleep(). Otherwise (if it
* were in join()) or it had exited it will have no effect.
@deprecated - Use interrupt() instead. interrupt() will interrupt a thread waiting on join()
**/
//===================================================================
public final void wakeup()
//===================================================================
{
	synchronized(lockObject) {
		woke = true;
		lockObject.notifyAll();
	}
}
//===================================================================
public final void interrupt()
//===================================================================
{
	synchronized(lockObject) {
		interrupted = true;
		lockObject.notifyAll();
	}
}

//===================================================================
public final static int count()
//===================================================================
{
	return running;
}

boolean wakeUpNow = false;
private int napCount = 0;
private static int sysNapCount = 0;
/**
* This calls sleep(time) every "iterations" calls of this method.
**/
//===================================================================
public static final int nap(int iterations,int time)
//===================================================================
{
	if (iterations <= 0) return 0;
	Coroutine c = Coroutine.getCurrent();
	if (c == null) {
		sysNapCount++;
		if (sysNapCount >= iterations)
			sysNapCount = 0;
		ewe.sys.Vm.sleep(time);
		return 0;
	}
	c.napCount++;
	if (c.napCount >= iterations) {
		c.napCount = 0;
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
/*
//===================================================================
public static final int sleep(Object waitObject,Method method,int sleepTime)
//===================================================================
{
	final Coroutine t = getCurrent();
	t.wakeUpNow = false;
	new Thread(){
		public void run(){
			try{
				Wrapper [] w = new Wrapper(){new Wrapper().setInt(sleepTime)};
				method.invoke(waitObject,w,null);
			}catch(Throwable t){

			}finally{
				synchronized(lockObject){
					t.wakeUpNow = true;
					lockObject.notifyAll();
				}
			}
		}
	}.start();
	while (!t.wakeUpNow){
		try{
			lockObject.wait();
		}catch(InterruptedException e){
		}
	}
	return 0;
}
//===================================================================
public static final int sleep(Object waitObject,String method,int sleepTime)
//===================================================================
{
	Reflect r = Reflect.getForObject(target);
	if (r == null) return;
	Method md = r.getMethod(methodName,"(Ljava/lang/Object;)V",0);
	if (md == null) return;
*/
//##################################################################
}
//##################################################################
