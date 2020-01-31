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
import java.net.*;
import java.io.*;

//##################################################################
public abstract class StreamOutputBuffer implements Runnable{
//##################################################################

byte [] toOutput;
boolean closed = false;

//===================================================================
public void startRunning()
//===================================================================
{
	new Thread(this).start();
}
//===================================================================
public synchronized int writeBytes(byte [] bytes,int start,int count)
//===================================================================
{
	if (closed) return -1;
	if (toOutput == null) {
		toOutput = new byte[count];
		ewe.sys.Vm.copyArray(bytes,start,toOutput,0,count);
		notifyAll();
		return count;
	}else
		return 0;
}

//-------------------------------------------------------------------
protected void closeOutputChannel()
//-------------------------------------------------------------------
{

}
//===================================================================
public synchronized void close()
//===================================================================
{
	closed = true;
	notifyAll();
}

Object flushLock = new Object();

//===================================================================
public void writeLoop()
//===================================================================
{
	while(!closed || (toOutput != null)){
		synchronized(this){
			if (toOutput == null)
				try{
					wait();
				}catch(Exception e){}
		}
		if (toOutput != null){
			try{
				doWrite(toOutput,0,toOutput.length);
			}catch(Exception e){
				synchronized(this){
					toOutput = null;
					closed = true;
				}
				synchronized(flushLock){
					flushLock.notifyAll();
				}
			}
			synchronized(this){
				toOutput = null;
			}
		}
	}
	closeOutputChannel();
}

//-------------------------------------------------------------------
protected abstract void doWrite(byte [] bytes,int start,int count) throws ewe.io.IOException;
//===================================================================

//This should only be called by the Ewe thread.
//===================================================================
public void flush()
//===================================================================
{
	if (closed) return;
	synchronized(flushLock){
		while(toOutput != null){
			try{
				flushLock.wait();
			}catch(InterruptedException e){
			}
		}
	}
	return;
}
//===================================================================
public void run() {writeLoop();}
//===================================================================
//##################################################################
}
//##################################################################
