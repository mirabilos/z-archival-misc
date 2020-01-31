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
import ewe.util.Vector;
/**
* This allows only a single mThread/Coroutine to hold a Lock at a time.
**/
//##################################################################
public class Lock{
//##################################################################
//Don't move this.
boolean isEweLock = false;
/**
* If this is true then a thread can call hold()/grab() on a lock
* multiple times - defaults to true.
* There must be a matching release() call for every hold()/grab() call.
**/
public boolean multipleEntry = true;
private int entered = 0;
private Coroutine owner = null;
private Vector waitingToHold;
private Vector waitingForNotify;
private Vector waitingForReacquire;


/**
 * This attempts to hold the lock, waiting an indefinite time to do so. Unlike
 * the hold/grab methods, this method returns no value and will throw a run-time exception
 * if the lock could not be held for any reason (since this will represent a severe error).
 */
//===================================================================
public void synchronize()
//===================================================================
{
	while(true){
		try{
			if (!doHold(TimeOut.Forever,true)) throw new RuntimeException("synchronize() failure!");
			return;
		}catch(InterruptedException e){
			 throw new RuntimeException("synchronize() failure!");
		}
	}
}
/**
* This tries to get ownership of the lock, but will not wait
* if it cannot get ownership immediately. It returns true if it got ownership,
* false if it did not. This method CAN be called by threads of execution which
* are NOT mThreads since it never attempts to wait for ownership. There must
* be a matching release() for every grab().
* @return true if the lock was acquired - false if not.
*/
//===================================================================
public boolean grab()
//===================================================================
{
	try{
		return doHold(null,false);
	}catch(InterruptedException e){
		return false;
	}
}
/**
* Use this to get exclusive ownership of the Lock. It returns true
* if it managed to gain ownership within the TimOut period, false if it
* did not. If the calling mThread already owns the lock AND multipleEntry
* is set true, then it will immediately return true. There must be a matching
* release() call for every hold()/grab() call.
* @param t A Timeout specifying how long to wait to acquire the lock. Set to TimeOut.Forever to wait
indefinitely.
* @return true if the lock was acquired - false if not.
* @deprecated use lock(TimeOut t) instead.
*/
//===================================================================
public boolean hold(TimeOut t)
//===================================================================
{
	try{
		return doHold(t,true);
	}catch(InterruptedException e){

		return false;
	}
}
/**
 * This does a hold() with a TimeOut of TimeOut.Forever.
* Use this to get exclusive ownership of the Lock. It returns true
* if it managed to gain ownership within the TimOut period, false if it
* did not. If the calling mThread already owns the lock AND multipleEntry
* is set true, then it will immediately return true. There must be a matching
* release() call for every hold()/grab() call.
* @return true if the lock was acquired - false if not.
* @deprecated use lock() instead.
 */
//===================================================================
public boolean hold()
//===================================================================
{
	try{
		return doHold(TimeOut.Forever,true);
	}catch(InterruptedException e){
		return false;
	}
}
/**
 * Attempt to acquire ownership of the Lock, waiting for a specific length of time.
 * A non-mThread thread can call this <b>if</b> the TimeOut is TimeOut.Immediate, in which
 * case this acts as a grab() call.
 * @param t The length of time to wait for.
 * @return true if it acquired the lock, false if it did not.
 * @exception InterruptedException If the Thread was interrupted.
 */
//===================================================================
public boolean lock(TimeOut t) throws InterruptedException
//===================================================================
{
	return doHold(t,true);
}
/**
 * Attempt to acquire ownership of the Lock, waiting indefinitely.
 * @exception InterruptedException If the Thread was interrupted while waiting for the lock.
 */
//===================================================================
public void lock() throws InterruptedException
//===================================================================
{
	doHold(TimeOut.Forever,true);
}
//-------------------------------------------------------------------
private boolean own(Coroutine newOwner)
//-------------------------------------------------------------------
{
	owner = newOwner;
	entered++;
	return true;
}
//-------------------------------------------------------------------
private boolean doHold(TimeOut t,boolean waitForIt) throws InterruptedException
//-------------------------------------------------------------------
{
	Coroutine cr = Coroutine.getCurrent();
	if (entered == 0) return own(cr);

	if (t == null) t = TimeOut.Forever;
	if (cr == owner){
 		if (multipleEntry) {
			entered++;
			return true;
		}else{
			if (cr != null)
				if ((t.remaining() == t.Infinite) && waitForIt)
					throw new IllegalThreadStateException("Lock Deadlock!");
		}
	}
//......................................................
// Have to wait.
//......................................................
	if (!waitForIt || t == TimeOut.Immediate) return false;

	if (cr == null)
		throw new IllegalThreadStateException("Only an mThread can wait to hold a Lock.");
	waitingToHold = Vector.add(waitingToHold,cr);
	while(true){
		boolean interrupted = Coroutine.sleep(t.remaining()) == -1;
		if (owner == cr) return true;
		if (t.hasExpired() || interrupted) {
			waitingToHold.remove(cr);
			if (interrupted) throw new InterruptedException();
			return false;
		}
	}
}
//-------------------------------------------------------------------
private void wakeWaiting()
//-------------------------------------------------------------------
{
	Coroutine cr = (Coroutine)Vector.pop(waitingForReacquire);
	if (cr == null) cr = (Coroutine)Vector.pop(waitingToHold);
	if (cr != null){
		own(cr);
		cr.wakeup();
	}
}
/**
* Release the lock. This can only be called by the owning thread.
* This is the same as unlock() except unlock will throw an exception
* if the calling thread is not the lock owner.
**/
//===================================================================
public boolean release()
//===================================================================
{
	Coroutine cr = Coroutine.getCurrent();
	if (cr != owner || entered <= 0) return false;
	entered--;
	if (entered == 0){
		owner = null;
		wakeWaiting();
	}
	return true;
}
/**
* Release the lock. This can only be called by the owning thread.
* This is the same as unlock() except unlock will throw an exception
* if the calling thread is not the lock owner.
**/
//===================================================================
public void unlock() throws IllegalThreadStateException
//===================================================================
{
	if (!release()) throw new IllegalThreadStateException("The calling thread does not own the lock.");
}
/**
* This causes the current mThread, which must own the lock, to release
* it and wait until notify() is called on the lock. It returns true
* if it was notified before the TimeOut expired, false otherwise. Once
* it has been notified or the TimeOut has expired it will wait until it
* can reacquire the lock before returning.<p>
*
* This can ONLY be called by a Coroutine/mThread.
* @param howLong The length of time to wait. You can use TimeOut.Forever to wait indefinitely.
* @return true if it was notified before timing out, false if it timed out before being notified.
* @deprecated - use waitOn(TimeOut howLong) instead.
*/
//===================================================================
public boolean wait(TimeOut howLong)
//===================================================================
{
	try{
		return waitOn(howLong);
	}catch(InterruptedException e){
		return false;
	}
}
/**
* This causes the current mThread, which must own the lock, to release
* it and wait until notifyWaiting() is called on the lock.  Once
* it has been notified or the TimeOut has expired or it was interrupted it will wait until it
* can reacquire the lock before returning.
* @param milliSeconds The length of time to wait in milliseconds.
* @return true if it was notified, false if it timed out before being notified.
* @exception InterruptedException If the Thread was interrupted while waiting.
*/
//===================================================================
public boolean waitOn(int milliSeconds) throws InterruptedException
//===================================================================
{
	return waitOn(new TimeOut(milliSeconds));
}
/**
* This causes the current mThread, which must own the lock, to release
* it and wait indefinitely until notifyWaiting() is called on the lock.  Once
* it has been notified or it was interrupted it will wait until it
* can reacquire the lock before returning.
* @exception InterruptedException If the Thread was interrupted while waiting.
*/
//===================================================================
public void waitOn() throws InterruptedException
//===================================================================
{
	waitOn(TimeOut.Forever);
}
/**
* This causes the current mThread, which must own the lock, to release
* it and wait until notifyWaiting() is called on the lock.  Once
* it has been notified or the TimeOut has expired or it was interrupted it will wait until it
* can reacquire the lock before returning.
* @param howLong The length of time to wait. You can use TimeOut.Forever to wait indefinitely.
* @return true if it was notified, false if it timed out before being notified.
* @exception InterruptedException If the Thread was interrupted while waiting.
*/
//===================================================================
public boolean waitOn(TimeOut howLong) throws InterruptedException
//===================================================================
{
	if (howLong == null) howLong = TimeOut.Forever;
	Coroutine c = Coroutine.getCurrent();
	if (c == null || owner != c)
		throw new IllegalMonitorStateException("Only a Coroutine holding the lock can call wait().");
//..................................................................
	int num = entered;
	entered = 0;
	owner = null;
	wakeWaiting();
//..................................................................
	waitingForNotify = Vector.add(waitingForNotify,c);
	boolean wasInterrupted = Coroutine.sleep(howLong.remaining()) == -1;
	boolean notified = waitingForNotify.find(c) == -1;
	if (!notified && waitingForNotify != null)
		waitingForNotify.remove(c);
	if (entered != 0){
		waitingForReacquire = Vector.add(waitingForReacquire,c);
		Coroutine.sleep(-1);
	}
	owner = c;
	entered = num;
	if (wasInterrupted) throw new InterruptedException();
	return notified;
}

//-------------------------------------------------------------------
private void doNotify(boolean all)
//-------------------------------------------------------------------
{
	doNotify(all,0);
}
//-------------------------------------------------------------------
private void doNotify(boolean all,int handleState)
//-------------------------------------------------------------------
{
	Coroutine c = Coroutine.getCurrent();
	if (owner != c)
		throw new IllegalMonitorStateException("Only a Coroutine holding the lock can call notify() or notifyAll().");
	Coroutine cr;
	while((cr = (Coroutine)Vector.pop(waitingForNotify)) != null){
		mThread m = cr.getThread();
		if (m != null) {
			m.handleState = handleState;
		}
		cr.wakeup();
		if (!all) break;
	}
}
/**
* This causes one Coroutine which is waiting on the lock to wakeup and reacquire
* ownership of the lock.
**/
//===================================================================
public void notifyWaiting() {doNotify(false);}
//===================================================================
/**
* This causes all Coroutines which are waiting on the lock to wakeup and attempt to reacquire
* ownership of the lock.
**/
//===================================================================
public void notifyAllWaiting() {doNotify(true);}
//===================================================================
/**
* Used by Handle.
**/
//-------------------------------------------------------------------
void notifyWaitingOnHandle(int state) {doNotify(true,state);}
//-------------------------------------------------------------------
/**
 * This grabs the lock, notifies waiting threads and then releases the lock. Since the grab() method
 * is used, it will never block.
 * @param doNotifyAll if this is true then a notifyAllWaiting() will be done, otherwise a notifyWaiting() will be done.
 * @return true on success, false on failure.
 */
//===================================================================
public boolean grabAndNotify(boolean doNotifyAll)
//===================================================================
{
	if (!grab()) return false;
	try{
		doNotify(doNotifyAll);
	}finally{
		release();
	}
	return true;
}
/**
 * This grabs the lock, notifies waiting threads and then releases the lock. This may block while the thread attempts
 * to acquire the lock.
 * @param doNotifyAll if this is true then a notifyAllWaiting() will be done, otherwise a notifyWaiting() will be done.
 * @return true on success, false on failure.
 */
//===================================================================
public boolean holdAndNotify(TimeOut howLong,boolean doNotifyAll)
//===================================================================
{
	if (!hold(howLong)) return false;
	try{
		doNotify(doNotifyAll);
	}finally{
		release();
	}
	return true;
}

//##################################################################
}
//##################################################################

