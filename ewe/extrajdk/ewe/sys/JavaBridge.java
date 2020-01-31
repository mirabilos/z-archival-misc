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
import ewe.fx.Image;
import ewe.io.File;
import ewe.net.ServerSocket;

//##################################################################
public class JavaBridge{
//##################################################################
/**
Create a Task that can call a full system blocking method without
blocking the VM. Under a native Ewe VM this is not possible and this will
still block the VM, but under a Java VM the Ewe VM will not be blocked.
<p>
The task should be implemented in a CallBack Object, where the callBack(Object data)
method will hold the blocking code and the "data" parameter will be a Handle that
the task can use to set the status of the task.
<p>
This method calls the method of the same name in ewe.sys.Vm.
**/
//===================================================================
public static Task makeBlockingTask(CallBack cb)
//===================================================================
{
	return Vm.makeBlockingTask(cb);
}
/**
Run a task that will display a native dialog box of some kind while it runs.
The method returns immediately, returning the Handle of the nativeDialogTask.
<p>
This method calls the method of the same name in ewe.sys.Vm.
**/
//===================================================================
public static Handle runNativeDialog(Task nativeDialogTask)
//===================================================================
{
	return Vm.runNativeDialog(nativeDialogTask);
}


/**
This method returns a native file dialog box and a Task that can be used
to execute the file dialog using runNativeDialog(), or null if none is
available on the current platform.<p>
The second object in the returned array is always a Task object for running the
native file dialog box.<p>
Under a Java 1.2 system, a javax.swing.JFileChooser is returned as the first object in the returned
array. Under a Java 1.1 system, a java.awt.FileDialog is returned as the first object in the
array.
**/
//===================================================================
public static Object[] getNativeFileChooser()
//===================================================================
{
	try{
		javax.swing.UIManager.setLookAndFeel(javax.swing.UIManager.getSystemLookAndFeelClassName());
 		final javax.swing.JFileChooser chooser = new javax.swing.JFileChooser();
		Object[] ret = new Object[2];
		ret[0] = chooser;
		ret[1] = makeBlockingTask(new CallBack(){
			public void callBack(Object data){
				Handle h = (Handle)data;
				h.returnValue = new Integer(chooser.showDialog(null,null));
			}
		});
		return ret;
	}catch(Exception e){
		return null;
	}
}
/**
Create a Ewe Object that is the equivalent of a Java Object.<p>
The Objects that can be converted are:<p>
<ul>
<li>java.awt.Image - to ewe.fx.Image</li>
<li>java.io.InputStream - to ewe.io.InputStream</li>
<li>java.io.OutputStream - to ewe.io.OutputStream</li>
<li>java.io.File - to ewe.io.File</li>
<li>java.net.Socket - to ewe.net.Socket</li>
<li>java.net.DatagramSocket - to ewe.net.DatagramSocket</li>
<li>java.net.ServerSocket - to ewe.net.ServerSocket</li>
<li>java.net.InetAddress - to ewe.net.InetAddr</li>
</ul>
<p>
Any other type of object will throw an IllegalArgumentException
**/
//===================================================================
public static Object createEweObject(Object javaObject)
//===================================================================
{
	if (javaObject instanceof java.awt.Image)
		return new Image(javaObject,0);
	if (javaObject instanceof java.io.InputStream)
		return new ewe.io.InputStream(new ewe.applet.JavaInputStream((java.io.InputStream)javaObject));
	if (javaObject instanceof java.io.OutputStream)
		return new ewe.applet.JavaOutputStream((java.io.OutputStream)javaObject);
	if (javaObject instanceof java.io.File)
		return new ewe.io.File((java.io.File)javaObject);
	if (javaObject instanceof java.io.RandomAccessFile)
		return new ewe.io.RandomAccessFile((java.io.RandomAccessFile)javaObject);
	if (javaObject instanceof java.net.Socket)
		return new ewe.net.Socket((java.net.Socket)javaObject);
	if (javaObject instanceof java.net.DatagramSocket)
		return new ewe.net.DatagramSocket((java.net.DatagramSocket)javaObject);
	if (javaObject instanceof java.net.ServerSocket)
		return new ewe.net.ServerSocket((java.net.ServerSocket)javaObject);
	if (javaObject instanceof java.net.InetAddress)
		return new ewe.net.InetAddress((java.net.InetAddress)javaObject);
	if (javaObject instanceof java.lang.Thread){
		final Thread jt = (Thread)javaObject;
		return makeBlockingTask(new CallBack(){
			public void callBack(Object data){
				jt.start();
				while(true){
					try{
						jt.join();
					}catch(InterruptedException e){}
				}
			}
		});
	}
	throw new IllegalArgumentException();
}
/**
Create a ewe.net.ServerSocket from a java.net.ServerSocket.
**/
//===================================================================
public static ServerSocket makeServerSocket(Object serverSocket)
//===================================================================
{
	return null;
}
/**
Create a ewe.io.File from a java.io.File
**/
//===================================================================
public static File makeFile(Object file)
//===================================================================
{
	return null;
}
//##################################################################
}
//##################################################################
