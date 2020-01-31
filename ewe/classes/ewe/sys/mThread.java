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
import ewe.security.mSecurityManager;
/**
* The mThread class is used as a substitute for the java.lang.Thread class and is used
* in Ewe as a convenient way of converting simple Java algorithms that use Threads to
* work in Ewe. A simple change of "Thread" to "mThread" will take care of most conversions.
* <p>
* An important thing to note is that this is not a true pre-emptive thread. This is simply
* a Runnable object that is run by a Coroutine. Therefore other mThreads or Coroutines will
* not be active while an mThread is running. Calling sleep() within an mThread will allow
* other Coroutines to run.
* <p>
**/
//##################################################################
public class mThread implements Runnable{
//##################################################################
public static final int MAX_PRIORITY = 10;
public static final int MIN_PRIORITY = 1;
public static final int NORM_PRIORITY = 5;

Coroutine myCoroutine;
protected Runnable myRunnable;
protected String name = "Unnamed";
protected int priority = NORM_PRIORITY;
protected boolean daemon = false;
private mThreadGroup group;
//
// Used by Handle.
//
int handleState = 0;
/**
* Get the priority level of this thread.
**/
//===================================================================
public int getPriority() {return priority;}
//===================================================================

//===================================================================
public final mThreadGroup getThreadGroup()
//===================================================================
{
	return group;
}
/**
 * Set the priority level of this thread.
 */
//===================================================================
public final void setPriority(int newPriority)
//===================================================================
{
	if (group != null)
		if (group.maxPriority != MAX_PRIORITY)
			if (newPriority > group.maxPriority)
				newPriority = group.maxPriority;
	priority = newPriority;
}
/**
 * Set the name of this thread.
 */
//===================================================================
public void setName(String name) {this.name = name;}
//===================================================================
/**
 * Get the name of this thread.
 */
//===================================================================
public String getName() {return name;}
//===================================================================
/**
 * Checks if the thread is alive. A thread is alive if it has been started
	and has not died.
 * @return true if the thread has been started and has not died.
 */
//===================================================================
public final boolean isAlive()
//===================================================================
{
	if (myCoroutine == null) return false;
	return myCoroutine.isRunning();
}
/**
 * Checks if this thread is a daemon thread.
 */
//===================================================================
public final boolean isDaemon()
//===================================================================
{
	return daemon;
}
/**
 * Sets whether this thread is to be a daemon thread or not - but as of 1.43 setting
 * an thread to be a daemon thread has no effect on how the thread is treated or how it operates.
 * This method is added simply to make porting Java applications easier.
 * @param daemon true to mark this thread as a daemon thread. False otherwise.
 * @exception IllegalThreadStateException if the thread has been already started.
 */
//===================================================================
public final void setDaemon(boolean daemon)
//===================================================================
{
	if (myCoroutine != null) throw new IllegalThreadStateException("Thread already started.");
	this.daemon = daemon;
}

//-------------------------------------------------------------------
mThread(Coroutine from,Runnable run)
//-------------------------------------------------------------------
{
	myCoroutine = from;
	myRunnable = run;
}

//===================================================================
public mThread()
//===================================================================
{
	this(null,null,null);
}
//===================================================================
public mThread(mThreadGroup group,Runnable run,String name)
//===================================================================
{
	if (name == null) name = "Unnamed";
	myRunnable = run;
	this.name = name;
	this.group = mThreadGroup.verifyThreadGroup(group);
	if (this.group != null) this.group.add(this);
}
//===================================================================
public mThread(Runnable run)
//===================================================================
{
	this(null,run,null);
}
//===================================================================
public mThread(Runnable run,String name)
//===================================================================
{
	this(null,run,name);
}
//===================================================================
public mThread(String name)
//===================================================================
{
	this(null,null,name);
}
//===================================================================
public mThread(mThreadGroup group,String name)
//===================================================================
{
	this(group,null,name);
}
//===================================================================
public mThread(mThreadGroup group,Runnable run)
//===================================================================
{
	this(group,run,null);
}

/**
* This is used with native methods where the native method must run in its own native
* thread. This call will put the current mThread to sleep (allowing others to run) indefinitely
* until it is interrupted or until the native thread wakes it up to indicate that its task
* is complete.<p>This method is the same as waitForResume(-1).
* @exception InterruptedException if interrupt() was called on the thread.
*/
//===================================================================
public static void waitForResume() throws InterruptedException
//===================================================================
{
	waitForResume(-1);
}
/**
* This is used with native methods where the native method must run in its own native
* thread. This call will put the current mThread to sleep (allowing others to run) for a maximum
* length of time
* until it is interrupted or until the native thread wakes it up to indicate that its task
* is complete.
* @param millis The maximum length of time to wait, or -1 to wait indefinitely.
* @return true if the native thread woke up the mThread, false if it waited until the maximum time expired.
* @exception InterruptedException if interrupt() was called on the thread.
*/
//===================================================================
public static boolean waitForResume(long millis) throws InterruptedException
//===================================================================
{
	int ret = Coroutine.sleep((int)millis);
	if (ret == 0) return true;
	else if (ret == -1) throw new InterruptedException();
	else return false;
}
/**
 * Cause the current mThread to sleep for the specified length of time,
 * unless interrupted by another thread.
 * @param millis The length of time in milliseconds to sleep. A sleep of -1 will
 * cause
 * @exception InterruptedException - if interrupted by another thread.
 */
//===================================================================
public static void sleep(long millis) throws InterruptedException
//===================================================================
{
	_sleep(millis);
}
/**
 * Cause the current mThread to sleep for the specified length of time,
 * unless interrupted by another thread.
 * @param millis The length of time in milliseconds to sleep.
 * @param nanos An additional time in nanoseconds to sleep.
 * @exception InterruptedException - if interrupted by another thread.
 */
//===================================================================
public static void sleep(long millis,int nanos) throws InterruptedException
//===================================================================
{
	_sleep(millis);
}
//===================================================================
private static void _sleep(long millis) throws InterruptedException
//===================================================================
{
	if (Coroutine.sleep((int)millis) == -1) throw new InterruptedException();
}
//===================================================================
public void run()
//===================================================================
{
	if (myRunnable != null) myRunnable.run();
}
/**
* Give other threads a chance to run.
**/
//===================================================================
public static void yield()
//===================================================================
{
	Coroutine.yield();
}
//===================================================================
public static void yield(int everyMillis)
//===================================================================
{
	Coroutine.yield(everyMillis);
}
//===================================================================
public void interrupt()
//===================================================================
{
	if (myCoroutine == null) return;
	myCoroutine.interrupt();
}
/**
* Start the mThread running at the soonest opportunity. This causes the
* run() method to be called.
**/
//===================================================================
public void start()
//===================================================================
{
	if (myCoroutine != null) return;
	myCoroutine = new Coroutine(this);
}
/**
* Waits until this mThread has died.
**/
//===================================================================
private void _join(long howLong) throws InterruptedException
//===================================================================
{
	if (myCoroutine == null) return;
	if (Coroutine.join(myCoroutine,(int)howLong) == -1) throw new InterruptedException();
}

/**
 * Wait for this mThread to die.
 * @exception InterruptedException if interrupt() was called on it.
 */
//===================================================================
public void join() throws InterruptedException
//===================================================================
{
	_join(-1);
}
/**
 * Wait for this mThread to die for at most millis milliseconds.
 * @param millis The maximum time to wait for the mThread to die. A value
	 of 0 means to wait forever.
 * @exception InterruptedException if interrupt() was called on it.

 */
//===================================================================
public void join(long millis) throws InterruptedException
//===================================================================
{
	if (millis <= 0) millis = -1;
	_join(millis);
}
/**
 * Wait for this mThread to die for at most millis milliseconds.
 * @param millis The maximum time to wait for the mThread to die. A value
	 of 0 means to wait forever.
* @param nano An additional number of nanoseconds to wait.
 * @exception InterruptedException if interrupt() was called on it.
*/
//===================================================================
public void join(long millis,int nano) throws InterruptedException
//===================================================================
{
	if (millis <= 0) millis = -1;
	_join(millis);
}
/**
* This will cause the running mThread to sleep for time (in milliseconds) every
* iterations calls to this method. Therefore nap(10,100) will cause the current
* mThread to sleep for 100 milliseconds every 10 times this method is called. This
* is useful to place within loops that require a lot of time to complete.
**/
//===================================================================
public static final int nap(int iterations,int time)
//===================================================================
{
	return Coroutine.nap(iterations,time);
}
/**
* If the current thread is a Coroutine thread, it will cause a sleep for the
* specified length of time, otherwise this call is ignored.
* @param time the length of time to nap for - it can be 0, but should not be < 0.
* @return true if a sleep occured, false if it did not.
*/
//===================================================================
public static final boolean nap(int time)
//===================================================================
{
	if (time < 0) time = 0;
	if (Coroutine.getCurrent() == null) return false;
	Coroutine.sleep(time);
	return true;
}
/**
 * Return the current running mThread. This will return an mThread even if this
 * thread is not running in a mThread. That is because there is a "simulated" mThread
 * that is used to represent non-mThread threads. If you want to know if the current
 * thread actually is running in an mThread, then call inThread()
 */
//===================================================================
public static mThread currentThread()
//===================================================================
{
	return Coroutine.getThread();
}
/**
* This returns true if a the current thread is a true mThread (i.e. is being
* run in a Coroutine).
**/
//===================================================================
public static boolean inThread()
//===================================================================
{
	return Coroutine.getCurrent() != null;
}
//-------------------------------------------------------------------
void ended()
//-------------------------------------------------------------------
{
	if (group != null) group.ended(this);
}
//-------------------------------------------------------------------
void exceptionThrown(Throwable t)
//-------------------------------------------------------------------
{
	if (group != null) group.uncaughtException(this,t);
}
//##################################################################
}
//##################################################################

