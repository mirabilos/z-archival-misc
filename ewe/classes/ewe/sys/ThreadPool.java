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
import ewe.util.*;

/**
* A ThreadPool represents a pool of running Threads that are waiting for tasks to run.
* You call addTask() to add a Task Object to the pool and an idle Thread is assigned
* to run the task. If no idle Thread is available a new one is created and used. ThreadPools
* can greatly improve performance in server applications since it reduces the overhead of
* creating new Threads. This has a greater effect under a true Java VM since Ewe Threads are
* very lightweight and really don't take any more system resources than any other object.
* <p>
* You can specify a minimum number of idle Threads and Threads are added to the pool
* to make up to that minimum.
* <p>
* You can specify a maximum number of Threads and if the maximum is reached all new tasks
* will have to wait until older ones are finished and their Threads are freed. If all
* Threads are idle and there are more idle threads than the specified minimum, the Threads
* will eventually terminate and be discarded until only the minimum number of Threads
* remain.
**/
//##################################################################
public class ThreadPool extends TaskObject{
//##################################################################
/**
* The minimum number of allowed threads. By default this is 10.
**/
public int minThreads = 10;
/**
* The maximum number of allowed threads. If this is <0 there is no maximum (the default).
**/
public int maxThreads = -1;
/**
* How long (in <b>seconds</b>) before an idle thread decides to exit (if there are no tasks
* to run and if the number of threads is already greater than the minimum). By default it
* is 5.
**/
public int waitTime = 5;

int totalCreated = 0;
/**
 * Create a ThreadPool with a minimum of 10 threads and no maximum.
 */
//==================================================================
public ThreadPool() {this(10,-1);}
//==================================================================
/**
 * Create a ThreadPool specifying the minimum and maximum number of threads.
 */
//==================================================================
public ThreadPool(int min,int max)
//==================================================================
{
	minThreads = min;
	maxThreads = max;
	makeUpToMinimum();
	startTask();
	mThread.yield();
}
Lock waitOn = new Lock();
Vector waitingJobs = new Vector(), runningJobs = new Vector();
int active, waiting;

//------------------------------------------------------------------
void addToPool()
//------------------------------------------------------------------
{
	waitOn.synchronize();try{
		poolThread t = new poolThread();
		t.index = ++totalCreated;
		t.startTask();
		active++;
		//System.out.println("Added: "+t.index);
	}finally{waitOn.release();}
}
/**
* Add threads until the minimum has been achieved. If you have increased the minimum
* then you can optionally call this.
**/
//==================================================================
public void	makeUpToMinimum()
//==================================================================
{
	waitOn.synchronize();try{
		if (minThreads < 0) minThreads = 4;
		int toAdd = minThreads-active;
		//System.out.println(toAdd);
		for (int i = 0; i<toAdd; i++) addToPool();
	}finally{waitOn.release();}
}

//-------------------------------------------------------------------
protected void doRun()
//-------------------------------------------------------------------
{
	//new addJobThread().start();
	try{
		while(!shouldStop || runningJobs.size() != 0){
			if (!shouldStop) makeUpToMinimum();
			checkTimeOuts();
			sleep(1000);
		}
	}finally{
		waitOn.synchronize();try{
			waitOn.notifyAllWaiting();
		}finally{waitOn.release();}
	}
}
//-------------------------------------------------------------------
void checkTimeOuts()
//-------------------------------------------------------------------
{
	waitOn.synchronize();try{
		try{
			for (Iterator it = runningJobs.iterator(); it.hasNext();){
				poolThread pt = (poolThread)it.next();
				pt.checkTimeOut();
			}
		}catch(Exception e){}
	}finally{waitOn.release();}
}
/**
* Use this to add a task to run. This should be a Runnable, including a TaskObject.
* @param toRun The task to run.
* @param timeOutInSeconds A maximum length of time for the task to run for. If this is
* zero or less then the task will have no maximum time.
* @exception IllegalStateException if the ThreadPool is closed.
*/
//==================================================================
public void addTask(Runnable toRun,int timeOutInSeconds)  throws IllegalStateException
//==================================================================
{
	if (shouldStop) throw new IllegalStateException("The ThreadPool is closed.");
	if (toRun == null) return;
	waitOn.synchronize();try{
		Tag t = new Tag();
		t.tag = timeOutInSeconds;
		t.value = toRun;
		waitingJobs.add(t);
		//System.out.println("Waiting threads: "+waiting);
		//System.out.println("W: "+waiting+" A: "+active);
		if ((waiting == 0) && ((active < maxThreads) || (maxThreads <= 0)))
			addToPool();
		if (waiting != 0) waiting--;
		waitOn.notifyWaiting();
	}finally{waitOn.release();}
}
/**
* Use this to add a task to run. This should be a Runnable, including a TaskObject.
* @param toRun The task to run.
* @exception IllegalStateException if the ThreadPool is closed.
*/
//==================================================================
public void addTask(Runnable toRun) throws IllegalStateException
//==================================================================
{
	addTask(toRun,-1);
}
static int numberAlive = 0;
/**
* After calling this no more tasks can be added and any spare threads
* will eventually die.
**/
//===================================================================
public void close()
//===================================================================
{
	handle.stop(0);
}
//##################################################################
class poolThread extends TaskObject{
//##################################################################

int index;
TimeOut curTimeout;
Handle curHandle;

poolThread()
{
	numberAlive++;
	//ewe.sys.Vm.debug("Starting: "+numberAlive);
}

void checkTimeOut()
{
	if (curTimeout == null || curHandle == null) return;
	if (curTimeout.hasExpired()) {
		//ewe.sys.Vm.debug("Have to timeout!");
		curHandle.stop(0);
		curTimeout = null;
	}
}
protected void doRun()
{
	try{
		boolean idle = false;
		while(true) {
			Runnable toRun = null;
			waitOn.synchronize(); try{
				if (waitingJobs.size() == 0) {
					if (ThreadPool.this.shouldStop || shouldStop || ((active > minThreads) && idle)) {
						active--;
						return;
					}
					idle = true;
					waiting++;
					try {
						waitOn.waitOn(waitTime*1000);
					}catch(Exception e){}
					if (waitingJobs.size() == 0) waiting--;
					continue;
				}else {
					Tag got = (Tag)waitingJobs.get(0);
					waitingJobs.del(0);
					curTimeout = (got.tag <= 0) ? null : new TimeOut(got.tag*1000);
					curHandle = (got.value instanceof Task) ? ((Task)got.value).getHandle() : null;
					runningJobs.add(this);
					toRun = (Runnable)got.value;
				}
			}finally{waitOn.release();}
			if (toRun != null) {
				idle = false;
				try{
					toRun.run();
				}catch(RuntimeException e){
				}finally{
					waitOn.synchronize(); try{
						runningJobs.remove(this);
					}finally{waitOn.release();}
				}
			}
		}
	}finally{
		numberAlive--;
		//ewe.sys.Vm.debug("Leaving: "+numberAlive);
	}
}
//##################################################################
}
//##################################################################


//##################################################################
}
//##################################################################

