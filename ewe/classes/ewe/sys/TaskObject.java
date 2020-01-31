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
* A TaskObject is an implementation of a Task that uses an mThread to
* run asynchronously.<p>
* To override a TaskObject you  need only override the doRun() method. Within
* that method you can use the "handle" variable to modify the handle associated with
* the task. This handle will automatically be set to "Running" before doRun() is called
* and will be set to "Stopped" when doRun() exits.
* <p>
**/
//##################################################################
public class TaskObject implements Task, Runnable{
//##################################################################
/**
* This is the handle used by the TaskObject.
**/
protected Handle handle;
/**
* This will be set true if the stop() method is called.
**/
public boolean shouldStop;

/**
* If this is true, then a call to stopTask() will request the running thread
* to be interrupted.
**/
protected boolean interruptThreadToStop;
/**
* This is the thread in which the task is running.
**/
protected mThread myThread;
/**
* Create a new TaskObject and assign a new Handle to the handle member.
**/
//-------------------------------------------------------------------
protected TaskObject()
//-------------------------------------------------------------------
{
	this(new Handle());
}
/**
* Create a new TaskObject using a particular Handle.
**/
//-------------------------------------------------------------------
protected TaskObject(Handle h)
//-------------------------------------------------------------------
{
	if (h == null) h = new Handle(this);
	else h.set(0);
	h.task = this;
	handle = h;
}
/**
* This sets the handle member to the Running state and then calls doStart().
* @return a Handle that can be used to monitor the state of the Task.
*/
//===================================================================
public Handle startTask()
//===================================================================
{
	if ((handle.check() & (handle.Stopped|handle.Running)) != 0) return handle;
	handle.setFlags(handle.Running,0);
	doStart();
	return handle;
}

/**
 * Request that the TaskObject stop running.
 * @param reason some task specific reason for stopping. There are no pre-defined reasons.
 * @return the Handle for the TaskObject.
 */
//===================================================================
public Handle stopTask(int reason)
//===================================================================
{
	doStop(reason);
	return handle;
}
/**
 * Get the Handle for the TaskObject.
 * @return the Handle for the TaskObject.
 */
//===================================================================
public Handle getHandle() {return handle;}
//===================================================================
/**
* This is called to start the TaskObject running. By default it creates
* an mThread which then calls the run() method, which in turn calls
* the doRun() method. If you want to use an mThread to execute this Task
* then do not override this method, instead override the doRun() method.
* <p>
* If you want to use a different method for Task execution (e.g. Timer ticks
* or Callbacks) then override this method to start the task running.
**/
//-------------------------------------------------------------------
protected void doStart() {(myThread = new mThread(this)).start();}
//-------------------------------------------------------------------
/**
* This method, by default, sets the shouldStop variable to true. The Task
* should therefore always check this variable when coming out of a sleep()
* to see whether it should continue or stop.
* <p>
* You can override this to provide a different method of stopping the
* task.
 * @param reason some task specific reason for stopping. There are no pre-defined reasons.
*/
//-------------------------------------------------------------------
protected void doStop(int reason)
//-------------------------------------------------------------------
{
	shouldStop = true;
	if (interruptThreadToStop && myThread != null)
		myThread.interrupt();
}
/**
* This calls the doRun() method and then sets the handle flags such that
* Running is off and Stopped is on after doRun() returns. You should
* therefore override doRun() instead of run().
**/
//===================================================================
public final void run()
//===================================================================
{
	//ewe.sys.Vm.debug("Start: "+getClass().getName());
	try{
		myThread = mThread.currentThread();
		handle.setFlags(Handle.Running,Handle.Stopped);
		doRun();
	}finally{
		handle.setFlags(Handle.Stopped,Handle.Running);
		//ewe.sys.Vm.debug("End: "+getClass().getName());
	}
}
/**
* Override this method if you want to use an mThread to run your Task. Otherwise
* if you are using timers or callbacks, override the doStart() method.
**/
//-------------------------------------------------------------------
protected void doRun(){}
//-------------------------------------------------------------------
/**
* This puts the current mThread thread to sleep.
**/
//-------------------------------------------------------------------
protected final static void sleep(int time)
//-------------------------------------------------------------------
{
	try{
		mThread.sleep(time);
	}catch(InterruptedException e){}
}
/**
* Everytime the nap() method is called it calls Coroutine.nap(napIterations,napTime).
**/
public int napIterations = 10;
/**
* Everytime the nap() method is called it calls Coroutine.nap(napIterations,napTime).
**/
public int napTime = 0;
/**
 * Set the napIterations and napTime variables. Everytime the nap() method is called it calls Coroutine.nap(napIterations,napTime).
 * @param napIterations
 * @param napTime
 * @return This TaskObject.
 */
//===================================================================
public TaskObject setNapTime(int napIterations,int napTime)
//===================================================================
{
	this.napIterations = napIterations;
	this.napTime = napTime;
	return this;
}
/**
* Nap for the nap time of this task object.
**/
//-------------------------------------------------------------------
protected void nap()
//-------------------------------------------------------------------
{
	Coroutine.nap(napIterations,napTime);
}
//===================================================================
public ewe.ui.ProgressBarForm start(String title)
//===================================================================
{
	ewe.ui.ProgressBarForm pbf = new ewe.ui.ProgressBarForm();
	pbf.showMainTask = pbf.showSubTask = pbf.horizontalLayout = false;
	pbf.showStop = true;
	pbf.setTask(startTask(),title);
	return pbf;
}
/**
* This starts the task and displays a progress bar.
**/
//===================================================================
public Handle exec(String title,ewe.ui.Frame parent)
//===================================================================
{
	start(title).exec(parent,0);
	return handle;
}
/**
* This starts the task and displays a progress bar.
**/
//===================================================================
public Handle show(String title,ewe.ui.Frame parent)
//===================================================================
{
	start(title).show(parent,0);
	return handle;
}
/**
 * Wait indefinitely for flags to be set on another handle. If stopTask() is called on the task,
 * or stop() is called on the handle of the task, then the wait will be interrupted and false will be returned.
 * If the otherHandle stops before the flags are set then false will also be returned.
 * <p><b>Only call this from within the TaskObject's thread.</b>
 * @param otherHandle The other handle to wait for.
 * @param flags The flags to wait to be set.
 * @param copyProgress set this true if you want the progress of the otherHandle to be
 * reflected in this handle.
 * @return true if the flags were set, false if stopTask() or handle.stop() was called, or if
 * the flags were not set. You should check handle.shouldStop to determine what to do next.
 */
//===================================================================
public boolean waitOn(Handle otherHandle, int flags, boolean copyProgress)
//===================================================================
{
	boolean is = interruptThreadToStop;
	int copyFlag = (copyProgress) ? handle.Changed : 0;
	interruptThreadToStop = true;
	try{
		while(!handle.shouldStop && !shouldStop){
			try{
				otherHandle.waitOnAny(flags|copyFlag);
				if (copyProgress) handle.setProgress(otherHandle.progress);
				if ((otherHandle.check() & flags) == flags) return true;
			}catch(InterruptedException ie){
			}catch(HandleStoppedException hse){
				return false;
			}
		}
		return false;
	}finally{
		interruptThreadToStop = is;
	}
}
/**
 * Wait indefinitely for any of a set of flags to be set on another handle. If stopTask() is called on the task,
 * or stop() is called on the handle of the task, then the wait will be interrupted and false will be returned.
 * If the otherHandle stops before the flags are set then false will also be returned.
 * <p><b>Only call this from within the TaskObject's thread.</b>
 * @param otherHandle The other handle to wait for.
 * @param flags The flags to wait to be set.
 * @param copyProgress set this true if you want the progress of the otherHandle to be
 * reflected in this handle.
 * @return true if the flags were set, false if stopTask() or handle.stop() was called, or if
 * the flags were not set. You should check handle.shouldStop to determine what to do next.
 */
//===================================================================
public boolean waitOnAny(Handle otherHandle, int flags, boolean copyProgress)
//===================================================================
{
	boolean is = interruptThreadToStop;
	interruptThreadToStop = true;
	int copyFlag = (copyProgress) ? handle.Changed : 0;
	try{
		while(!handle.shouldStop && !shouldStop){
			try{
				otherHandle.waitOnAny(flags|copyFlag);
				if (copyProgress) handle.setProgress(otherHandle.progress);
				if ((otherHandle.check() & flags) != 0) return true;
			}catch(InterruptedException ie){
			}catch(HandleStoppedException hse){
				return false;
			}
		}
		return false;
	}finally{
		interruptThreadToStop = is;
	}
}
/**
 * This can be used after a call to waitOn() or waitOnAny() has failed.
 * If the shouldStop variable OR if the wasWaitingOn handle has the Aborted flag set,
 * then the handle for this task will be set to Aborted and true will be returned.
 * Otherwise this task's handle will be set to Failure, and its errorObject set to the
 * same error object as wasWaitingOn. If stopWaitingOn is true then the stop() metod
 * will be called on wasWaitingOn.
 * <p><b>Only call this from within the TaskObject's thread.</b>
 * @param wasWaitingOn The Handle that this task was waiting on.
 * @param stopWaitingOn If this is true then wasWaitingOn will have its stop() method called.
 * @return true if this task or the wasWaitingOn task was aborted, false if wasWaitingOn had
 * failed.
 */
//===================================================================
public boolean checkAbortFail(Handle wasWaitingOn,boolean stopWaitingOn)
//===================================================================
{
	try{
		if (shouldStop || ((wasWaitingOn.check() & Handle.Aborted) != 0)){
			handle.set(Handle.Aborted);
			return true;
		}
		handle.errorObject = wasWaitingOn.errorObject;
		handle.set(Handle.Failure);
		return false;
	}finally{
		if (stopWaitingOn) wasWaitingOn.stop(0);
	}
}
/**
 * Call this to check on the result of a waitOn() or waitOnAny() and automatically set the
 * Failure or Abort flag of the handle of this task, based on the failure/success of waitOn.
 * <p><b>Only call this from within the TaskObject's thread.</b>
 * @param resultOfWait The result of the waitOn() or waitOnAny() call.
 * @param wasWaitingOn The hande the task was waiting on.
 * @param stopWaitingOn If this is true then the task will be stopped on failure.
 * @return will always be resultOfWait.
 */
//===================================================================
public boolean checkFailure(boolean resultOfWait,Handle wasWaitingOn,boolean stopWaitingOn)
//===================================================================
{
	if (resultOfWait) return true;
	checkAbortFail(wasWaitingOn,stopWaitingOn);
	return false;
}
/**
 * This calls waitOn(waitFor,Handle.Success,copyProgress) and then calls checkFailure() on it.
 * This will basically wait until either the waitFor handle has the Success flag set or until
 * it has stopped without that flag being set (which usually indicates failure) or until the
 * task has the stop method called (by shouldStop being set true).
 * In the case of failure this task's handle will be set to either Failure or Aborted depending
 * on whether the stop() method was called on this task's handle or whether the waitFor handle
 * failed or aborted.
 * <p><b>Only call this from within the TaskObject's thread.</b>
 * @param waitFor The handle to wait for.
 * @param copyProgress True if you want this task's handle to reflect the progress of the waitFor handle.
 * @return true on success, false on failure.
 */
//===================================================================
public boolean waitOnSuccess(Handle waitFor,boolean copyProgress)
//===================================================================
{
	return checkFailure(waitOn(waitFor,Handle.Success,copyProgress),waitFor,true);
}
/**
* Yield to another thread or task.
**/
//===================================================================
public void yield()
//===================================================================
{
	mThread.yield();
}
/**
* Yield to another thread or task every certain number of milliseconds.
**/
//===================================================================
public void yield(int everyMilliseconds)
//===================================================================
{
	mThread.yield(everyMilliseconds);
}
//##################################################################
}
//##################################################################

