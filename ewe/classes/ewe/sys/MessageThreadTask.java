/*********************************************************************************
 *  Ewe Virtual Machine - Version 1.14, January 24, 2002                         *
 *  Copyright (C) 1999-2002 Michael L Brereton <michael_brereton@ewesoft.com>    *
 *  Copyright (C) 1998, 1999 Wabasoft <www.wabasoft.com>                         *
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
import ewe.ui.Window;
/**
* This class is used to execute a method within the System Message Thread (which
* is needed to do certain operations on certain operating systems).<p>
* To use it you simply override doTask(). This method will get called in the System Message Thread
* when the execute() method of the MessageThreadTask is executed.<p>
* If execute() is called when the system is already in the Message Thread, then the doTask()
* method is called immediately. Other wise, the current Coroutine is halted and a CallBack is
* requested, which will then execute doTask(). After doTask() has been executed the current
* Coroutine continues and returns the value returned by doTask().
**/
//##################################################################
public abstract class MessageThreadTask implements CallBack{
//##################################################################
private boolean succeeded = false;
private boolean didIt = false;
private Window window;
/**
* This is the only thing to override. Do the task and return true
* if successful or false otherwise.
**/
protected abstract boolean doTask(Object data);
private Lock myLock = null;
/**
This is the number of milliseconds to wait before the callBack() completes.
By default it is 5000 (5 seconds). If you set it to a value < 0 then there
will be no timeout.
**/
public int timeout = 5000;

//===================================================================
public final void callBack(Object data){
//===================================================================
	succeeded = doTask(data);
	didIt = true;
	myLock.grab();
	myLock.notifyAllWaiting();
	myLock.release();
}


/**
 * Execute the doTask() method in a System Thread.
 * @param data The data to be passed to doTask().
 * @return the value returned by doTask().
 */
//===================================================================
public final boolean execute(Object data)
//===================================================================
{
	return execute(data,false);
}
/**
 * Execute the doTask() method in a System Thread or in the current thread - depending on the value
 * of alwaysExecuteNow.
 * @param data The data to be passed to doTask().
 * @param alwaysExecuteNow If this is true then doTask() will always be executed in the current thread.
 * @return The value returned by doTask().
 */
//===================================================================
public final boolean execute(Object data,boolean alwaysExecuteNow)
//===================================================================
{
	if (Vm.amInSystemQueue() || alwaysExecuteNow) return doTask(data);
	if (Coroutine.getCurrent() == null)
		throw new EventDirectionException("This task cannot be done within a Timer Tick.");
	myLock = new Lock();
	myLock.synchronize(); try{
		Vm.callInSystemQueue(window,this,data);
		while(!didIt){
			if (timeout >= 0) myLock.waitOn(timeout);
			else myLock.waitOn();
			if (!didIt) return false;
		}
	}catch(InterruptedException e){
	}finally{
		myLock.release();
	}
	//while(!didIt) Coroutine.sleep(0);
	return succeeded;
}

//===================================================================
public MessageThreadTask(Window w)
//===================================================================
{
	window = w;
}
//===================================================================
public MessageThreadTask()
//===================================================================
{
	this(null);
}
//##################################################################
}
//##################################################################


