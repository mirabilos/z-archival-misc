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
import ewe.io.*;

//##################################################################
class javaProcess extends ewe.sys.Process{
//##################################################################
java.lang.Process process;

javaProcess(java.lang.Process process)
{
	this.process = process;
}
/**
 * Kills the process.
 */
public  void destroy()
{
	process.destroy();
}
/**
 * Wait until the process exits.
 */
public void waitFor()
{
	waitFor(TimeOut.Forever);
}
/**
* Wait until the process exits.
* @param t The length of time to wait.
* @return true if the process did exit, false if the timeout expired before the process exited.
*/
public  boolean waitFor(TimeOut time)
{
	if (Coroutine.getCurrent() == null)
		throw new RuntimeException("Only an mThread can wait on a process.");
	final Handle h = new Handle();
	h.set(Handle.Running);
	Thread t = new java.lang.Thread(){
		public void run(){
			try{
				process.waitFor();
				h.set(Handle.Succeeded);
			}catch(Exception e){
				h.set(Handle.Failed);
			}
		}
	};
	t.start();
	try{
		boolean got = h.waitOn(h.Success,time);
		if (!got) t.interrupt();
		return got;
	}catch(Exception e){
		return false;
	}
}
/**
 * Get the exit value of the process.
 * @return the exit value of the process.
 * @exception IllegalThreadStateException if the process is still running.
 */
public  int exitValue() throws IllegalThreadStateException
{
	return process.exitValue();
}
/**
 * Return an input Stream to read from the standard error output of the process.
 */
public Stream getErrorStream()
{
	return new ewe.applet.JavaInputStream(process.getErrorStream());
}
/**
 * Return an output Stream to write to the standard input of the process.
 */
public Stream getOutputStream()
{
	return new ewe.applet.JavaOutputStream(process.getOutputStream());
}
/**
 * Return an input Stream to read from the standard output of the process.
 */
public Stream getInputStream()
{
	return new ewe.applet.JavaInputStream(process.getInputStream());
}
//##################################################################
}
//##################################################################
