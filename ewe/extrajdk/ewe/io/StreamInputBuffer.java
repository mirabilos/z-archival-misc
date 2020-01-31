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
public abstract class StreamInputBuffer implements Runnable{
//##################################################################

protected byte [] input = new byte[2048];
int istart;
int available;

boolean closed = false;

//===================================================================
public void startRunning()
//===================================================================
{
	new Thread(this).start();
}
//===================================================================
public synchronized int readBytes(byte [] bytes,int start,int count)
//===================================================================
{
	if (available > 0) {
		if (count > available) count = available;
		ewe.sys.Vm.copyArray(input,istart,bytes,start,count);
		istart += count;
		available -= count;
		notifyAll();
		return count;
	}else if (closed)
		return -1;
	else
		return 0;
}

//===================================================================
public synchronized void close()
//===================================================================
{
	closed = true;
	notifyAll();
}
//-------------------------------------------------------------------
protected void endReached()
//-------------------------------------------------------------------
{

}
//===================================================================
public void readLoop()
//===================================================================
{
	while(!closed){
		synchronized(this){
			if (available != 0)
				try{
					wait();
				}catch(Exception e){}
			if (available != 0) continue;
		}
		int read = doRead(input,0,input.length);
		if (read < 0) {
			close();
			break;
		}
		synchronized(this){
			available = read;
			istart = 0;
		}
	}
}
//-------------------------------------------------------------------
protected abstract int doRead(byte [] bytes,int start,int count);
//-------------------------------------------------------------------
//===================================================================
public void run() {readLoop();}
//===================================================================
//##################################################################
}
//##################################################################
