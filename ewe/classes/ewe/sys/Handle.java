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
import java.lang.reflect.Method;

import ewe.sys.Reflection;
import ewe.util.Vector;
import ewe.util.TagList;
//##################################################################
public class Handle implements TimerProc,Task{
//##################################################################
/**
* This is not to be written to! It is used when interfacing with native
* threads.
**/
protected int nativeResult;//This should not be moved! It must be first.
/**
* If the process is supposed to produce or return a value, it should be
* put here.
**/
public Object returnValue; //This should not be moved! It must be second.
/**
* This gives an indication of the progress of the process. It should
* be in the range 0.0 to 1.0 (1.0 meaning complete).
**/
public float progress = 0.0f; //This should not be moved! It must be third.

public static final int
	Changed = 0x80000000,
	Stopped = 0x40000000,
	Success = 0x20000000,
	Failure = 0x10000000,
	Running = 0x08000000,
	Aborted = 0x04000000,
	Succeeded = Success|Stopped,
	Failed = Failure|Stopped;
/** An option for waitOnFlags() - may also be used with or without OPTION_TIMEOUT_IF_NOT_SET */
public static final int OPTION_TIMEOUT_IF_NOT_SET = 0x02000000;
/** An option for waitOnFlags() - almost always used with OPTION_TIMEOUT_IF_NOT_SET */
public static final int OPTION_DISCARD_IF_NOT_SET = 0x01000000;
/**
* This is the Task associated with the handle (if any).
**/
public Task task;
/**
* This is the state of the handle.
**/
protected int state;
/**
* This is a text version of what the process is currently doing.
**/
public String doing = "";
/**
* This is an application specific error code.
**/
public int errorCode;
/**
* If the process stopped due to an error. A description of the error should
* go here. Optional.
**/
public String error;
/**
* This can be used for more detailed error handling.
**/
public Object errorObject;

private int nonce = 0;
private ewe.util.Vector callBacks;
private Lock lock = new Lock();
/**
* This is the time that the task started at. Calling resetTime() will set it to be the current
* time and will set the progress to 0.0
**/
public long startTime;
/**
* This gets set to true if the stop() method is called, in addition to it calling the stop() method of the
* associated task.
**/
public boolean shouldStop = false;
/**
* This is the reason given to the stop() method.
**/
public int stopReason = 0;
/**
* If this is set to 0 then a call to changed() is done everytime setProgress() is set, otherwise
* changed is called only when the progress has increased past the last setProgress() by this value.
**/
public float progressResolution = 0;
private float nextProgress = 0;
/**
* Create a Handle that has its status preset. If status has the Failure flag set and returnValueOrErrorString
* is a String, then the "error" variable is set to this value. Otherwise it sets returnValue to be the parameter.
**/
private boolean shouldDiscard;

/**
 * This interface is used by an Object that knows how to discard of a particular
 * Object.
 */
public static interface ObjectDiscarder {

	/**
	 * Discard, in this Thread, the provided Object.
	 * @param obj the Object to be discarded.
	 * @return true if this ObjectDiscarder knows how to discard of it,
	 * false if not.
	 */
	public boolean discard(Object obj);
}

private static Vector discarders;

/**
 * Add an ObjectDiscarder to the global set of ObjectDiscarders.
 * @param discarder the ObjectDiscarder to add.
 */
public static synchronized void addDiscarder(ObjectDiscarder discarder)
{
	if (discarder == null) return;
	if (discarders == null) discarders = new Vector();
	discarders.add(discarder);
}
private static void discardOf(Object obj)
{
	if (discarders == null) return;
	for (int i = 0; i<discarders.size(); i++){
		ObjectDiscarder od = (ObjectDiscarder)discarders.get(i);
		try{
			if (od.discard(obj)) break;
		}catch(Throwable t){}
	}
}
protected void discard(Object obj)
{
	try{
		if (obj == null) return;
		Class c = obj.getClass();
		Method m = Reflection.getMethod(c,"free()V",false);
		if (m == null) m = Reflection.getMethod(c,"dispose()V",false);
		if (m == null) m = Reflection.getMethod(c,"close()V",false);
		if (m != null) Reflection.invoke(obj,m,null,null);
		discardOf(obj);
	}catch(Throwable t){}
}
/**
 * Tell the Handle that current or future returnValues should be discarded.
 * This will only work correctly if setResult() or setSuccess() is used to set
 * the return value. If a returnValue has already been set, it will be immediately
 * discarded.
 * @param inThisThread if this is true and there is a returnValue to discard,
 * then the discard() method will be called in this Thread. Otherwise it will
 * be called in a separate Thread.
 */
public void discardResult(boolean inThisThread)
{
	Object rv = null;
	lock.synchronize(); try{
		shouldDiscard = true;
		rv = returnValue;
		returnValue = null;
	}finally{
		lock.release();
	}
	if (rv != null){
		final Object toD = rv;
		if (inThisThread) discard(toD);
		else new mThread(){
			public void run(){
				discard(toD);
			}
		}.start();
	}
}
/**
 * Tell the Handle that current or future returnValues should be discarded.
 * This will only work correctly if setResult() or setSuccess() is used to set
 * the return value. If a returnValue has already been set, it will be immediately
 * discarded in a separate Thread.
 */
public void discardResult()
{
	discardResult(false);
}

//===================================================================
public Handle(int status,Object returnValueOrErrorString)
//===================================================================
{
	this((Task)null);
	if (((status & Failure) != 0) && (returnValueOrErrorString instanceof String))
		error = (String)returnValueOrErrorString;
	else
		returnValue = returnValueOrErrorString;
	set(status);
}

/**
 * Create a Handle that has its status preset to Failed.
 * @param error An error
 * @return
 */
//===================================================================
public Handle(Throwable error)
//===================================================================
{
	this((Task)null);
	fail(error);
}
//===================================================================
public Handle() {this((Task)null);}
//===================================================================
public Handle(Task p)
//===================================================================
{
	task = p;
	if (p == null) task = this;
}
//===================================================================
public final Task getTask() {return task;}
//===================================================================
/**
* Get a new Handle that will be stopped if this handle is stopped.
**/
//===================================================================
public Handle getSubHandle(String doing)
//===================================================================
{
	Handle h = new Handle();
	stopAlsoIfStopped(h);
	return h.startDoing(doing);
}
//===================================================================
public Handle getSubHandle()
//===================================================================
{
	return getSubHandle("");
}
/**
* Set the Stopped and Failure bits of this Handle and set the errorObject
* to be the specified Throwable.
**/
//===================================================================
public void fail(Throwable t)
//===================================================================
{
	error = t.getMessage();
	errorObject = t;
	setFlags(Failed,0);
}
/**
Set the returnValue of the Handle and then set the Stopped and Success bits - this
is the same as setResult(). If
the state already has the Stopped or Failure bits set, then this will return false.
*/
//===================================================================
public boolean succeed(Object returnValue)
//===================================================================
{
	boolean ret = false, dc = false;
	synchronized(this){
		if ((state & (Stopped|Failure)) != 0) ret = false;
		else{
			this.returnValue = returnValue;
			setFlags(Succeeded,0);
		}
		dc = (shouldDiscard && returnValue != null);
	}
	if (dc) discard(returnValue);
	return ret;
}
/**
Set the returnValue of the Handle and then set the Stopped and Success bits - this
is the same as succeed(). If
the state already has the Stopped or Failure bits set, then this will return false.
*/
public boolean setResult(Object returnValue)
{
	return succeed(returnValue);
}

/**
* This notifies any waiters that something about the handle may have changed. It causes a yield()
* on the current mThread.
**/
//===================================================================
public final void changed()
//===================================================================
{
	synchronized(ewe.sys.Vm.getSyncObject()){ // This allows a Java thread to control the handle.
		doChangeTo(state);
		mThread.yield();
	}
}
//-------------------------------------------------------------------
final void doChangeTo(int newstate)
//-------------------------------------------------------------------
{
	boolean locked = false;
	if (Coroutine.getCurrent() == null)
		locked = lock.grab();
	else{
		lock.synchronize();
		locked = true;
	}
	if (locked) try{
		state = newstate & ~Changed;
		nonce++;
		//lock.notifyAllWaiting();
		//ewe.sys.Vm.debug("Changing to: "+state);
		lock.notifyWaitingOnHandle(state);

		checkCallBacks(true);
	}finally{
		lock.release();
	}
	else
		new mThread(new HandleStatus(this,newstate)).start();
}
/**
* Everytime it is called it will ensure
* that any waiting Coroutines get notified so they can check to see if the
* state they are waiting for has been achieved.
**/
//===================================================================
public final void set(int newstate)
//===================================================================
{
	synchronized(ewe.sys.Vm.getSyncObject()){ // This allows a Java thread to control the handle.
		if (newstate != state) doChangeTo(newstate);
	}
}

/**
 * This sets and clears specific flags (bits) in the handle state.
 * Everytime it is called it will ensure
 * that any waiting Coroutines get notified so they can check to see if the
 * state they are waiting for has been achieved.
 * @param switchOn Flag bits to switch on.
 * @param switchOff Flag bits to swtich off.
 */
//===================================================================
public final void setFlags(int switchOn,int switchOff)
//===================================================================
{
	synchronized(ewe.sys.Vm.getSyncObject()){ // This allows a Java thread to control the handle.
		set((state & ~switchOff)|switchOn);
	}
}
//-------------------------------------------------------------------
private void callBackRemoved()
//-------------------------------------------------------------------
{
		if (callBacks.size() == 0) {
			callBacks = null;
			Vm.cancelTimer(callBackId);
			callBackId = 0;
		}
}
//-------------------------------------------------------------------
private void checkCallBacks(boolean dueToChange)
//-------------------------------------------------------------------
{
	if (callBacks == null) return;
	for (int i = 0; i<callBacks.size(); i++){
		Object obj = callBacks.get(i);
		if (!(obj instanceof HandleStatus)) continue;
		HandleStatus hs = (HandleStatus)obj;
		hs.currentState = state;
		int st = state & hs.waitingFor;
		if (dueToChange && ((hs.waitingFor & Changed) != 0));
		else if (hs.waitForAll && (st == hs.waitingFor));
		else if (!hs.waitForAll && (st != 0));
		else if ((state & Stopped) != 0){
			hs.stopped = true;
		}else if (hs.timeOut.hasExpired()) hs.timedOut = true;
		else continue;
		if (hs != null) {
			callBacks.del(i);
			i--;
			Vm.callInSystemQueue(hs.callBack,hs);
		}
	}
	callBackRemoved();
}

private int callBackId;
//===================================================================
public void ticked(int id,int elapsed)
//===================================================================
{
	if (id == callBackId) checkCallBacks(false);
}

/**
* This returns the state of the handle.
**/
//===================================================================
public final int check() {return state;}
//===================================================================
//-------------------------------------------------------------------
private int doWaitOnFlagsLocked(int flags,TimeOut t,boolean allFlags) throws HandleStoppedException, InterruptedException
//-------------------------------------------------------------------
{
	int state = this.state;
	mThread curThread = mThread.currentThread();
	boolean timeoutIfNotDone = (flags & OPTION_TIMEOUT_IF_NOT_SET) != 0;
	boolean discardIfNotDone = (flags & OPTION_DISCARD_IF_NOT_SET) != 0;
	flags &= ~(OPTION_TIMEOUT_IF_NOT_SET|OPTION_DISCARD_IF_NOT_SET);
	//
	// Uncomment the line below to disable mThread.handleState usage.
	// curThread = null;
	//
	while(true){
		int st = state & flags;
		if (st == flags && allFlags) return state;
		if (st != 0 && !allFlags) return state;
		if ((state & Stopped) != 0) {
			throw new HandleStoppedException();//return 0;
		}
		if (t.hasExpired()) {
			if (discardIfNotDone) discardResult(false);
			if (timeoutIfNotDone) {
				timeout();
				throw new HandleStoppedException();
			}else
				return 0;
		}
		int last = nonce;
		if (Coroutine.getCurrent() == null)
			throw new RuntimeException("Only an mThread can wait on a Handle on the Ewe VM.");
		lock.waitOn(t); // Will be notified IF it has changed or timed out.
		state = this.state;
		if (curThread != null) state |= curThread.handleState;
		if (((flags & Changed) != 0) && nonce != last) return state; //If waiting on a change.
	}
}
//-------------------------------------------------------------------
private int doWaitOnFlags(int flags,TimeOut t,boolean allFlags) throws HandleStoppedException, InterruptedException
//-------------------------------------------------------------------
{
	if (flags == 0) throw new IllegalArgumentException("flags must not be 0.");
	if (t == null) t = TimeOut.Forever;
	if (lock.lock(t)) try{
		return doWaitOnFlagsLocked(flags,t,allFlags);
	}finally{
		lock.release();
	}else
		return 0;
}
/**
 * This causes the current mThread to wait until the status of the Handle has ALL
 * the flag bits set as specified by the "flags" parameter, or until the Timout specified
 * expires, or until the Handle has the Stopped bit
 * set (in which case an Exception is thrown).
 * @param flags The flag bits to wait for. Must NOT be zero; an IllegalArgumentException is thrown if it is.
 * @param t The TimeOut to wait for.
 * @return true if the flags were all set within the timeout period, false if not.
 * @exception HandleStoppedException If the handle has the Stopped flag set before all the flags were set.
 * @exception InterruptedException If the Thread was interrupted.
 */
//===================================================================
public final boolean waitOn(int flags,TimeOut t) throws HandleStoppedException, InterruptedException
//===================================================================
{
	return (doWaitOnFlags(flags,t,true) != 0);
}
/**
 * This causes the current mThread to wait until the status of the Handle has ALL
 * the flag bits set as specified by the "flags" parameter, or until the Timout specified
 * expires, or until the Handle has the Stopped bit
 * set (in which case an Exception is thrown).
 * @param flags The flag bits to wait for. Must NOT be zero; an IllegalArgumentException is thrown if it is.
 * @param t The time in milliseconds to wait.
 * @return true if the flags were all set within the timeout period, false if not.
 * @exception HandleStoppedException If the handle has the Stopped flag set before all the flags were set.
 * @exception InterruptedException If the Thread was interrupted.
 */
//===================================================================
public final boolean waitOn(int flags,int t) throws HandleStoppedException, InterruptedException
//===================================================================
{
	return (doWaitOnFlags(flags,new TimeOut(t),true) != 0);
}
/**
 * This causes the current mThread to wait indefinitely until the status of the Handle has ALL
 * the flag bits set as specified by the "flags" parameter, or until the Handle has the Stopped bit
 * set (in which case an Exception is thrown).
 * @param flags The flag bits to wait for. Must NOT be zero; an IllegalArgumentException is thrown if it is.
 * @exception HandleStoppedException If the handle has the Stopped flag set before all the flags were set.
 * @exception InterruptedException If the Thread was interrupted.
 */
//===================================================================
public final void waitOn(int flags) throws HandleStoppedException, InterruptedException
//===================================================================
{
	doWaitOnFlags(flags,TimeOut.Forever,true);
}
/**
 * This causes the current mThread to wait until the status of the Handle has at least one of
 * the flag bits set as specified by the "flags" parameter set, or until the Timout specified
 * expires, or until the Handle has the Stopped bit
 * set (in which case an Exception is thrown).
 * @param flags The flag bits to wait for. Must NOT be zero; an IllegalArgumentException is thrown if it is.
 * @param t The TimeOut to wait for.
 * @return true if any of the flags were all set within the timeout period, false if not.
 * @exception HandleStoppedException If the handle has the Stopped flag set before all the flags were set.
 * @exception InterruptedException If the Thread was interrupted.
 */
//===================================================================
public final boolean waitOnAny(int flags,TimeOut t) throws HandleStoppedException, InterruptedException
//===================================================================
{
	return (doWaitOnFlags(flags,t,false) != 0);
}
/**
 * This causes the current mThread to wait until the status of the Handle has at least one of
 * the flag bits set as specified by the "flags" parameter set, or until the Timout specified
 * expires, or until the Handle has the Stopped bit
 * set (in which case an Exception is thrown).
 * @param flags The flag bits to wait for. Must NOT be zero; an IllegalArgumentException is thrown if it is.
 * @param t The time in milliseconds to wait.
 * @return true if any of the flags were all set within the timeout period, false if not.
 * @exception HandleStoppedException If the handle has the Stopped flag set before all the flags were set.
 * @exception InterruptedException If the Thread was interrupted.
*/
//===================================================================
public final boolean waitOnAny(int flags,int t) throws HandleStoppedException, InterruptedException
//===================================================================
{
	return (doWaitOnFlags(flags,new TimeOut(t),false) != 0);
}
/**
 * This causes the current mThread to wait indefinitely until the status of the Handle has at least
 * one of the flag bits set as specified by the "flags" parameter, or until the Handle has the Stopped bit
 * set (in which case an Exception is thrown).
 * @param flags The flag bits to wait for. Must NOT be zero; an IllegalArgumentException is thrown if it is.
 * @exception HandleStoppedException If the handle has the Stopped flag set before all the flags were set.
 * @exception InterruptedException If the Thread was interrupted.
 */
//===================================================================
public final void waitOnAny(int flags) throws HandleStoppedException, InterruptedException
//===================================================================
{
	doWaitOnFlags(flags,TimeOut.Forever,false);
}
/**
 * Wait until the Handle has stopped.
 * @param howLong How long to wait for.
 * @return true if the Handle stopped within the TimeOut period, false if not.
 * @exception InterruptedException if the Thread was interrupted.
 */
//===================================================================
public final boolean waitUntilStopped(TimeOut howLong) throws InterruptedException
//===================================================================
{
	try{
		return waitOn(Handle.Stopped,howLong);
	}catch(HandleStoppedException e){return true;}
}
/**
 * Wait until the Handle has stopped.
 * @exception InterruptedException if the Thread was interrupted.
 */
//===================================================================
public final void waitUntilStopped() throws InterruptedException
//===================================================================
{
	try{
		waitOn(Handle.Stopped,TimeOut.Forever);
	}catch(HandleStoppedException e){}


}
/**
 * Checks to see if all the specified flags have been set.
 * @param flags The flag bits to check for.
 * @return true if the bits are all set, false if not.
 * @exception HandleStoppedException If the handle has the Stopped flag set AND the handle does
 * not have the specified flag bits set.
 */
//===================================================================
public final boolean check(int flags) throws HandleStoppedException
//===================================================================
{
	int s = check();
	if ((s & flags) == flags) return true;
	if ((s & Stopped) == Stopped) throw new HandleStoppedException();
	return false;
}
/**
 * Checks to see if any of the specified flags have been set.
 * @param flags The flag bits to check for.
 * @return true if any of the bits are set, false if not.
 * @exception HandleStoppedException If the handle has the Stopped flag set AND the handle does
 * not have any of the specified flag bits set.
 */
//===================================================================
public final boolean checkAny(int flags) throws HandleStoppedException
//===================================================================
{
	int s = check();
	if ((s & flags) != 0) return true;
	if ((s & Stopped) == Stopped) throw new HandleStoppedException();
	return false;
}
/**
* @deprecated - use waitOn(int flags,TimeOut t) instead.
**/
//===================================================================
public final boolean waitOnFlags(int flags,TimeOut t)
//===================================================================
{
	try{
		return (doWaitOnFlags(flags,t,true) != 0);
	}catch(HandleStoppedException e){return false;}
	catch(InterruptedException e2) {return false;}
}
/**
* @deprecated - use waitOnAny(int flags,TimeOut t) instead.
**/
//===================================================================
public final int waitOnAnyFlag(int flags,TimeOut t)
//===================================================================

{
	try{
		return doWaitOnFlags(flags,t,false);
	}catch(HandleStoppedException e){return 0;}
	catch(InterruptedException e2) {return 0;}
}
//-------------------------------------------------------------------
protected final HandleStatus doCallBackOnFlags(CallBack cb,int flags,TimeOut t,boolean doAll)
//-------------------------------------------------------------------
{
	if (t == null) t = TimeOut.Forever;
	HandleStatus hs = new HandleStatus();
	hs.callBack = cb;
	hs.timeOut = t;
	hs.waitingFor = flags;
	hs.waitForAll = doAll;
	hs.handle = this;
	hs.currentState = state;
	callBacks = Vector.add(callBacks,hs);
	if (callBackId == 0) callBackId = Vm.requestTimer(this,100);
	return hs;
}
//===================================================================
public final HandleStatus callBackOnFlags(CallBack cb,int flags,TimeOut t)
//===================================================================
{
	return doCallBackOnFlags(cb,flags,t,true);
}
//===================================================================
public final HandleStatus callBackOnAnyFlag(CallBack cb,int flags,TimeOut t)
//===================================================================
{
	return doCallBackOnFlags(cb,flags,t,false);
}
//===================================================================
public final void cancelCallBack(CallBack cb)
//===================================================================
{
	if (callBacks == null) return;
	callBacks.remove(cb);
	callBackRemoved();
}

/**
* This tells the handle to ask its associated task to stop. It does not
* guarantee to immediately stop the task. The task, because it may be
* an asynchronous task, will do so on its own time.
**/
//===================================================================
public void stop(int reason)
//===================================================================
{
	shouldStop = true;
	stopReason = reason;
	if (task != null) task.stopTask(reason);
	if (stops == null) return;
	Object[] others = stops.getRefs();
	for (int i = 0; i<others.length; i++){
		Handle h = (Handle)others[i];
		if (h != null) h.stop(reason);
	}
}
/**
* This tells the handle to ask its associated task to start. It does not
* guarantee to immediately start the task. The task, because it may be
* an asynchronous task, will do so on its own time.
**/
//===================================================================
public void start()
//===================================================================
{
	if (task != null) task.startTask();
}
/**
* Task implementation - This will return itself.
**/
//===================================================================
public Handle getHandle() {return this;}
//===================================================================
/**
* Task implementation - this will set the state of the handle to Running.
**/
//===================================================================
public Handle startTask() {if ((state & (Running|Stopped)) == 0) set(Running); return this;}
//===================================================================
/**
* Task implementation - this will do nothing!

**/
//===================================================================
public Handle stopTask(int reason) {return this;/*if ((state & Stopped) == 0) set(Stopped); return this;*/}
//===================================================================
/**
 * This causes a CallBack object to be called when the handle is reported as having stopped. If a non-null
 * timeout is provided then it will only wait for that length of time.
 * @param cb the CallBack object to be called when the handle has stopped. The callBack() method will be called
	with this handle as the parameter. However if the timeout expired before this handle stopped,
	then the parameter to the callBack() method will be null. Note that the callBack() method is called within
	a Coroutine.
 * @param timeout An optional time to wait. If this is null it will wait forever.
 */
//===================================================================
public void callWhenStopped(final CallBack cb,final TimeOut timeout)
//===================================================================
{
	if (cb != null)
	new mThread(new Runnable(){
		public void run(){
			if (waitOnFlags(Stopped,timeout == null ? TimeOut.Forever : timeout))
				cb.callBack(this);
			else
				cb.callBack(null);
		}
	}).start();
}
/**
* This calls callWhenStopped() with a null timeout.
**/
//===================================================================
public void callWhenStopped(final CallBack cb)
//===================================================================
{
	callWhenStopped(cb,null);
}


/**
* Returns whether the Stopped bit has been set.
*/
public boolean hasStopped()
{
	return (check() & Stopped) != 0;
}
/**
*@deprecated - use waitOn(int flags,TimeOut checkTimeOut,Handle masterHandle,TimeOut masterTimeOut) instead.
*/
//===================================================================
public boolean waitOnFlags(int flags,TimeOut checkTimeOut,Handle masterHandle,TimeOut masterTimeOut)
//===================================================================
{
	while(true){
		if (masterTimeOut.hasExpired() || masterHandle.shouldStop) return false;
		if (waitOnFlags(flags,checkTimeOut.reset())) return true;
		if ((state & Stopped) != 0) return false;
	}
}
/**
 * This is used by one task to check on the progress of another. It is best explained via
 * the source code:
 * <pre>
public boolean waitOn(int flags,TimeOut checkTimeOut,Handle masterHandle,TimeOut masterTimeOut) throws HandleStoppedException
{
	while(true){
		if (masterTimeOut.hasExpired() || masterHandle.shouldStop) return false;
		if (waitOn(flags,checkTimeOut.reset())) return true;
	}
}
 * </pre>
 * @param flags The bits to check for.
 * @param checkTimeOut How long to check this Handle for each iteration.
 * @param masterHandle The master handle controlling the operation.
 * @param masterTimeOut The length of time the masterHandle is waiting for.
 * @return true if all the flags were set within the masterTimeOut period, false if the masterTiimeOut expired or if the shouldStop flag
 * of the masterHandle is set.
 * @exception HandleStoppedException if this handle has its Stopped bits set.
 * @exception InterruptedException If the Thread was interrupted.
 */
//===================================================================
public boolean waitOn(int flags,TimeOut checkTimeOut,Handle masterHandle,TimeOut masterTimeOut) throws HandleStoppedException, InterruptedException
//===================================================================
{
	if (masterHandle != null)
		while(true){
			if (masterTimeOut.hasExpired() || masterHandle.shouldStop) return false;
			if (waitOn(flags,checkTimeOut.reset())) return true;
		}
	else
		return waitOn(flags,masterTimeOut);
}
/**
 * This is used by one task to check on the progress of another. It is best explained via
 * the source code:
 * <pre>
public boolean waitOnAny(int flags,TimeOut checkTimeOut,Handle masterHandle,TimeOut masterTimeOut) throws HandleStoppedException
{
	while(true){
		if (masterTimeOut.hasExpired() || masterHandle.shouldStop) return false;
		if (waitOnAny(flags,checkTimeOut.reset())) return true;
	}
}
 * </pre>
 * @param flags The bits to check for.
 * @param checkTimeOut How long to check this Handle for each iteration.
 * @param masterHandle The master handle controlling the operation.
 * @param masterTimeOut The length of time the masterHandle is waiting for.
 * @return true if any of the flags were set within the masterTimeOut period, false if the masterTiimeOut expired or if the shouldStop flag
 * of the masterHandle is set.
 * @exception HandleStoppedException if this handle has its Stopped bits set.
 * @exception InterruptedException If the Thread was interrupted.
 */
//===================================================================
public boolean waitOnAny(int flags,TimeOut checkTimeOut,Handle masterHandle,TimeOut masterTimeOut) throws HandleStoppedException, InterruptedException
//===================================================================
{
	while(true){
		if (masterTimeOut.hasExpired() || masterHandle.shouldStop) return false;
		if (waitOnAny(flags,checkTimeOut.reset())) return true;
	}
}
//===================================================================
public void resetProgress(float progressResolution)
//===================================================================
{
	progress = nextProgress = 0;
	this.progressResolution = progressResolution;
}
//===================================================================
public void setProgress(float progress)
//===================================================================
{
	synchronized(ewe.sys.Vm.getSyncObject()){ // This allows a Java thread to control the handle.
		boolean doChange = false;
		doChange = progress == 1.0f || progress < 0 || progress < this.progress || progress >= nextProgress;
		this.progress = progress;
		if (doChange) {
			if (progressResolution > 0 && progress >= 0)
				while(nextProgress < progress)
					nextProgress += progressResolution;
			else
				nextProgress = progress;
			changed();
		}
	}
}
/**
* Mark the handle as failed and set the errorObject to be error.
**/
//===================================================================
public void failed(Object error)
//===================================================================
{
	errorObject = error;
	set(Failed);
}
//===================================================================
public String getErrorText(String defaultText)
//===================================================================
{
	if (error != null) return error;
	if (errorObject instanceof String) return (String)errorObject;
	String ret = null;
	if (errorObject instanceof Throwable) ret = ((Throwable)errorObject).getMessage();
	if (ret == null) ret = defaultText;
	return ret;
}

private static Time time;
/**
 * This method does the following:<ol>
 <li>It sets the doing field to the specified parameter.
 <li>It sets the startTime to the current time.
 <li>It sets the progress to 0.0
 <li>It calls changed() on the handle.
 </ol>
 * @param doing the new value for the doing field.
 */
//===================================================================
public void resetTime(String doing)
//===================================================================
{
	synchronized(ewe.sys.Vm.getSyncObject()){ // This allows a Java thread to control the handle.
		this.doing = doing;
		if (time == null) time = new Time();
		startTime = time.setToCurrentTime().getTime();
		progress = nextProgress = 0;
		changed();
	}
}
/**
 * A convenience method that calls resetTime(doing) and then returns itself.
 * @param doing The current task.
 * @return itself.
 */
//===================================================================
public Handle startDoing(String doing)
//===================================================================
{
	resetTime(doing);
	return this;
}
private TagList tags;
/**
* Use this for passing custom data to and from the processes.
* @param tag a custom integer tag.
* @param value a value to pass.
* @return this Handle.
*/
//===================================================================
public Handle setTag(int tag,Object value)
//===================================================================
{
	synchronized(ewe.sys.Vm.getSyncObject()){ // This allows a Java thread to control the handle.
		if (tags == null) tags = new TagList();
		tags.set(tag,value);
		return this;
	}
}
/**
* Use this for passing custom data to and from the processes.
* @param tag a custom integer tag.
* @param defaultValue the default value.
* @return the Object associated with the tag or the defaultValue if the tag is not set.
*/
//===================================================================
public Object getTag(int tag,Object defaultValue)
//===================================================================
{
	if (tags == null) return defaultValue;
	return tags.getValue(tag,defaultValue);
}
ewe.util.WeakSet stops;
/**
* If the stop() method of this handle is called it will call stop()
* on the other handle.
**/
//===================================================================
public void stopAlsoIfStopped(Handle otherHandle)
//===================================================================
{
	if (otherHandle == null) return;
	if (stops == null) stops = new ewe.util.WeakSet();
	stops.add(otherHandle);
}
/**
* Use this for passing custom data to and from the processes.
 * @param tag a custom integer tag to clear.
 * @return this Handle.
 */
//===================================================================
public Handle clearTag(int tag)
//===================================================================
{
	synchronized(ewe.sys.Vm.getSyncObject()){ // This allows a Java thread to control the handle.
		if (tags != null) tags.clear(tag);
		tags = null;
		return this;
	}
}
/**
* Set the Stopped and Failure bits of this Handle and set the errorObject
* to be the specified Throwable. If the state already
* has the Stopped or Succeess bits set, then this will return false.
**/
//===================================================================
public synchronized boolean setFail(Throwable t)
//===================================================================
{
	if ((state & (Stopped|Failure)) != 0) return false;
	fail(t);
	return true;
}

/**
 * This calls stop() and sets the status of the handle to Failed and sets error to be a TimedOutException
 * assuming that the Handle has not already stopped.
 * @param timeoutMessage a message for the TimedOutException().
 * @return true if it timed out before it was stopped another way, false if it
 * was already stopped.
 */
public synchronized boolean timeout(String timeoutMessage)
{
	stop(0);
	return setFail(timeoutMessage == null ? new TimedOutException() : new TimedOutException(timeoutMessage));
}
/**
 * This calls stop() and sets the status of the handle to Failed and sets error to be a TimedOutException
 * assuming that the Handle has not already stopped.
 * @return true if it timed out before it was stopped another way, false if it
 * was already stopped.
 */
public synchronized boolean timeout()
{
	return timeout(null);
}
/**
 * Wait for a result to be set via setResult() or succeed() or until it has timed out.
 * If the result was set within the timeout period this will return true and you should
 * access returnValue to get the return value. If
 * the handle was stopped due to failure this will throw a HandleStoppedException. If
 * the timeout period expired then this will return null if timeoutIfNotDone is false,
 * and it will cause timeout() and discardResult() to be called if timeoutIfNotDone is true.<p>
 * @param waitFor The length of time to wait. Use TimeOut.Immediate to check
 * without waiting, or TimeOut.Forever to wait indefinitely.
 * @param timeoutIfNotDone if this is true and the result is not available during
 * the TimeOut period then timeout() will be called and this will throw a HandleStopped
 * exception.
 * @return true if successful, false if the TimeOut period expired, but
 * timeoutIfNotDone is false.
 * @throws HandleStoppedException if the Handle was stopped for any reason other than
 * success.
 * @throws InterruptedException if the Thread was interrupted.
 */
/*
public boolean waitOnSuccess(TimeOut waitFor, boolean timeoutIfNotDone) throws HandleStoppedException, InterruptedException
{
	int f = Success;
	if (timeoutIfNotDone) f |= OPTION_TIMEOUT_IF_NOT_SET|OPTION_DISCARD_IF_NOT_SET;
	return waitOn(f,waitFor);
}
*/
/**
 * Wait for a specific length of time for a returnValue to be set and the handle state to
 * be set to Succeess via the setResult() or succeed() calls - but stop() the Handle if
 * the result is not available during the timeout period. This method should only be
 * called once, because if the result is not available the Handle will be stopped via
 * a call to timeout() and discardResult().
 * @param waitFor the length of time to wait for.
 * @return true if Success was set, false if not (in which case it never will be).
 */
public boolean waitOnSuccess(TimeOut waitFor)
{
	while(true){
		try{
			return waitOn(Success|OPTION_TIMEOUT_IF_NOT_SET|OPTION_DISCARD_IF_NOT_SET,waitFor);
		}catch(HandleStoppedException e2){
			return false;
		}catch(InterruptedException e){}
	}
}
/**
 * This calls waitOnSuccess(waitFor) and then returns the returnValue if it returned
 * true, or null if it returned false.
 * @param waitFor the length of time to wait for.
 * @return returnValue if successful, null if not.
 */
public Object waitOnResult(TimeOut waitFor)
{
	return waitOnSuccess(waitFor) ? returnValue : null;
}

//##################################################################
}
//##################################################################

