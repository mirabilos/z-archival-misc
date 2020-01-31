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
public abstract class OutputBuffer implements Runnable{
//##################################################################

protected byte [] output = new byte[2048];
protected byte [] waiting = null;
int toGo = 0;
boolean closed = false;
ewe.io.IOException error;
Thread writer;

//===================================================================
public void startRunning()
//===================================================================
{
	(writer = new Thread(this)).start();
}

/**
* This is none blocking.<p>
* This returns: true if the bytes were sent, false if not.
**/
//===================================================================
public synchronized boolean writeBytes(byte [] bytes,int start,int count) throws ewe.io.IOException
//===================================================================
{
	if (error != null) throw error;
	if (closed)
		throw new ewe.io.IOException("Output closed.");
	else if (waiting != null) return false;
	if (count > output.length) output = new byte[count];
	ewe.sys.Vm.copyArray(bytes,start,output,0,count);
	if (count > 0){
		waiting = output;
		toGo = count;
		notifyAll();
	}
	return true;
}

//-------------------------------------------------------------------
protected abstract void doClose() throws ewe.io.IOException;
//-------------------------------------------------------------------
//===================================================================
public synchronized void close() throws ewe.io.IOException
//===================================================================
{
	flush();
	error = null;
	closed = true;
	try{
		if (writer == null) {
			doClose();
			return;
		}
	}finally{
		notifyAll();
	}
	while(writer != null){
		try{
			wait();
		}catch(Exception e){}
	}
	if (error != null) throw error;
}
//===================================================================
public void run()
//===================================================================
{
	try{
		while(!closed){
			synchronized(this){
				if (waiting == null)
					try{
						wait();
					}catch(Exception e){}
				if (waiting == null) continue;
			}
			try{
				doWrite(waiting,0,toGo);
			}catch(ewe.io.IOException e){
				synchronized(this){
					error = e;
				}
			}finally{
				synchronized(this){
					waiting = null;
					notifyAll();
				}
			}
		}
	}finally{
		synchronized(this){
			writer = null;
			notifyAll();
			try{
				doClose();
			}catch(ewe.io.IOException e){
				error = e;
			}
		}
	}
}
/**
* This should write out ALL the bytes.
**/
//-------------------------------------------------------------------
protected abstract void doWrite(byte [] bytes,int start,int count) throws ewe.io.IOException;
protected abstract void doFlush() throws ewe.io.IOException;
//===================================================================
public synchronized void flush() throws ewe.io.IOException
//===================================================================
{
	if (writer == null) return;
	while(waiting != null){
		try{
			wait();
		}catch(Exception e){}
	}
	doFlush();
	if (error != null) throw error;
}
//##################################################################
}
//##################################################################
