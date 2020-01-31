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
package ewe.applet;

//##################################################################
public abstract class InputBuffer implements Runnable{
//##################################################################

protected byte [] input = new byte[2048];
int available, requested;
int istart;

boolean closed = false;
boolean ended = false;
ewe.io.IOException error;
Thread reader;

//===================================================================
public void startRunning()
//===================================================================
{
	(reader = new Thread(this)).start();
}

/**
* This is none blocking.<p>
* This returns: 0 = none available now, -1 = stream end reached, >0 = bytes read.
**/
//===================================================================
public synchronized int readBytes(byte [] bytes,int start,int count) throws ewe.io.IOException
//===================================================================
{
	if (error != null) throw error;
	if (available > 0) {
		if (count > available) count = available;
		ewe.sys.Vm.copyArray(input,istart,bytes,start,count);
		istart += count;
		available -= count;
		return count;
	}else if (closed)
		throw new ewe.io.IOException("Input closed.");
	else if (ended)
		return -1;
	else if (requested != 0) //Made a request but no bytes available yet.
		return 0;
	else {
		requested = count;
		notifyAll();
		return 0;
	}
}

//-------------------------------------------------------------------
protected abstract void doClose() throws ewe.io.IOException;
//-------------------------------------------------------------------
/**
* This is a blocking close.
**/
//===================================================================
public synchronized void close() throws ewe.io.IOException
//===================================================================
{
	error = null;
	closed = true;
	try{
		doClose();
	}finally{
		notifyAll();
	}
	if (error != null) throw error;
}
//===================================================================
public void run()
//===================================================================
{
	try{
		while(!closed){
			//ewe.sys.Vm.debug("Input thread started.");
			synchronized(this){
				if (available != 0 || requested == 0 || ended)
					try{
						//ewe.sys.Vm.debug("Waiting for input request.");
						wait();
						//ewe.sys.Vm.debug("Awoken!");
					}catch(Exception e){}
				if (available != 0 || requested == 0 || ended) continue;
			}
			try{
				int toRead = requested;
				if (toRead > input.length) toRead = input.length;
				int read = doRead(input,0,input.length);
				synchronized(this){
					if (read < 0) {
						ended = true;
						return;
					}
					available = read;
					istart = 0;
				}
			}catch(ewe.io.IOException e){
				synchronized(this){
					error = e;
				}
			}finally{
				synchronized(this){
					requested = 0;
				}
			}
		}
	}finally{
		//ewe.sys.Vm.debug("Leaving input loop!");
		synchronized(this){
			reader = null;
			notifyAll();
			/*
			try{
				doClose();
			}catch(ewe.io.IOException e){
				error = e;
			}
			*/
		}
	}
}
/**
* This should return >0 = Number of bytes read, -1 = End of input stream or throw an IOException.
**/
//-------------------------------------------------------------------
protected abstract int doRead(byte [] bytes,int start,int count) throws ewe.io.IOException;
//-------------------------------------------------------------------

//##################################################################
}
//##################################################################
