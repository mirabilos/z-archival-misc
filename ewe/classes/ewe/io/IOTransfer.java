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
package ewe.io;
import ewe.sys.*;
/**
* This class is used to copy from one stream to another asynchronously.
**/
//##################################################################
public class IOTransfer extends TaskObject{
//##################################################################

public Stream in;
public Stream out;
public int bufferSize = 1024*10;
public int sleepTime = 0;
/**
* This is how long it waits (in milliseconds) between checking the task Handle for an abort request. By default it is 100.
**/
public int checkTime = 100;
/**
* This is the number of bytes copied so far.
**/
public int copied = 0;
/**
* If you know how many bytes are to be copied, then set this value.
**/
public int totalToCopy = 0;
/**
* Set this to true if you want to stop after totalToCopy has been reached.
**/
public boolean stopAfterTotalToCopy = false;
/**
* After calling this constructor you can call start(Stream in,Stream out) to
* begin the transfer operation running asynchronously.
**/
//===================================================================
public IOTransfer(){}
//===================================================================
/**
* After calling this constructor you can call startTask() to begin the
* transfer operation running asynchronously.
**/
//===================================================================
public IOTransfer(Stream in,Stream out)
//===================================================================
{
	this(in,out,0);
}
/**
* After calling this constructor you can call startTask() to begin the
* transfer operation running asynchronously.
**/
//===================================================================
public IOTransfer(Stream in,Stream out,int totalToCopy)
//===================================================================
{
	this.in = in;
	this.out = out;
	this.totalToCopy = totalToCopy;
}
/**
* Start the transfer asynchronously and return a Handle.
**/
//===================================================================
public Handle start(Stream in,Stream out)
//===================================================================
{
	this.in = in;
	this.out = out;
	return startTask();
}
/**
* This runs it synchronously. It will not return until it is done.
* @deprecated - use transfer(in,out) instead.
**/
//===================================================================
public boolean run(Stream in,Stream out)
//===================================================================
{
	this.in = in;
	this.out = out;
	return transfer();
}
/**
* Copy from the "in" stream to the "out" stream. The streams are NOT closed.
**/
//===================================================================
public void transfer(Stream in,Stream out) throws IOException
//===================================================================
{
	if (bufferSize < 1) bufferSize = 1;
	byte [] buff = new byte[bufferSize];
	while(!shouldStop){
		/**
		* This readBytes method will block the current Coroutine until at
		* least one byte is read. It will let other Coroutines run if it
		* has to wait.
		**/
		int read = in.read(buff,0,buff.length);
		if (read == -1) break;
		if (read == 0) continue;
		/**
		* This writeBytes method will block the current Coroutine until
		* all bytes are written. It will let other Coroutines run if it
		* has to wait.
		**/
		out.write(buff,0,read);
		copied += read;
		/**
		* Allow other threads to have some time to execute.
		**/
	}
	out.flush();
}

protected boolean transferred = false;
/**
* This is the method that runs in its own Coroutine thread.
**/
//-------------------------------------------------------------------
protected boolean transfer()
//-------------------------------------------------------------------
{
	if (bufferSize < 1) bufferSize = 1;
	byte [] buff = new byte[bufferSize];
	while(!shouldStop){
		/**
		* This readBytes method will block the current Coroutine until at
		* least one byte is read. It will let other Coroutines run if it
		* has to wait.
		**/
		int read = in.readBytes(buff,0,buff.length);
		if (read == 0) break;
		else if (read < 0) return false;
		/**
		* This writeBytes method will block the current Coroutine until
		* all bytes are written. It will let other Coroutines run if it
		* has to wait.
		**/
		if (out.writeBytes(buff,0,read) != read) return false;
		copied += read;
		/**
		* Allow other threads to have some time to execute.
		**/
	}
	try{
		out.flush();
	}catch(Exception e){
		return false;
	}
	return true;
}
/**
* This is the method that runs in its own Coroutine thread.
**/
//-------------------------------------------------------------------
protected void doRun()
//-------------------------------------------------------------------
{
	if (bufferSize < 1) bufferSize = 1;
	byte [] buff = new byte[bufferSize];
	IOHandle rw = new IOHandle();
	TimeOut check = new TimeOut(checkTime);
	while(!shouldStop){
		/**
		* This readBytes method will block the current Coroutine until at
		* least one byte is read. It will let other Coroutines run if it
		* has to wait.
		**/
		int toRead = buff.length;
		if (totalToCopy > 0 && stopAfterTotalToCopy){
			toRead = totalToCopy-copied;
			if (toRead > buff.length) toRead = buff.length;
		}
		int read = IO.checkReadWrite(in.readBytes(buff,0,toRead,rw,false),check,handle,TimeOut.Forever);
		if (read == 0) break;
		else if (read < 0){
			handle.set(handle.Failed);
			return;
		}
		/**
		* This writeBytes method will block the current Coroutine until
		* all bytes are written. It will let other Coroutines run if it
		* has to wait.
		**/
		if (IO.checkReadWrite(out.writeBytes(buff,0,read,rw),check,handle,TimeOut.Forever) != read){
			handle.set(handle.Failed);
			return;
		}
		copied += read;
		if (totalToCopy > 0 && stopAfterTotalToCopy && copied >= totalToCopy)
			break;

		if (handle != null) {
			if (totalToCopy > 0) handle.setProgress((float)copied/(float)totalToCopy);
			else handle.setProgress(-1);
			//handle.changed();
		}
		/**
		* Allow other threads to have some time to execute.
		**/
		if (sleepTime >= 0)
			if (Coroutine.getCurrent() == null) ewe.sys.Vm.sleep(sleepTime);
			else Coroutine.sleep(sleepTime);
	}
	if (shouldStop) handle.set(handle.Stopped|handle.Aborted);
	else {
		try{
			out.flush();
		}catch(Exception e){
			handle.set(handle.Failed);
			return;
		}
		handle.progress = 1.0f;
		handle.set(handle.Succeeded);
		if (Coroutine.getCurrent() != null) Coroutine.sleep(0);
	}
}

//===================================================================
public void closeStreams()
//===================================================================
{
	try{
		if (in != null) in.close();
	}catch(Exception e){

	}
	try{
		if (out != null) out.close();
	}catch(Exception e){

	}
}
//##################################################################
}
//##################################################################


