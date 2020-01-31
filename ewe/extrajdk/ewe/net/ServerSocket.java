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
package ewe.net;
import ewe.io.*;
import java.net.*;
import java.io.*;

//##################################################################
public class ServerSocket{
//##################################################################

public static final int CHECK_LISTEN = 0;
public static final int CHECK_ACCEPT = 1;

java.net.ServerSocket jsock;
Object localHost;
Throwable connectionError;

//-------------------------------------------------------------------
private boolean failedToConnect = false;
//-------------------------------------------------------------------

//===================================================================
public ServerSocket(java.net.ServerSocket s)
//===================================================================
{
	createFrom(s);
}
//-------------------------------------------------------------------
void createFrom(java.net.ServerSocket s)
//-------------------------------------------------------------------
{
	try{
		jsock = s;
		if (jsock == null) throw new Exception();
	}catch(Exception e){
		failedToConnect = true;
	}
}
//Native create always opens the socket for non-blocking IO.
//-------------------------------------------------------------------
private void _nativeCreate(final int port,final int backlog,final String address)
//-------------------------------------------------------------------
{
	try{
	final InetAddress ia = address == null ? null : InetAddress.getByName(address);
	localHost = ia;
	if (localHost == null) localHost = InetAddress.getLocalHostName();
	new Thread(){
		public void run(){
			try{
				int bl = backlog;
				if (ia != null){
					if (bl == 0) bl = 100;
					createFrom(new java.net.ServerSocket(port,bl,(java.net.InetAddress)ia.nativeAddress));
				}else if (bl != 0)
					createFrom(new java.net.ServerSocket(port,bl));
				else
					createFrom(new java.net.ServerSocket(port));
				/*
				new Thread(){
					public void run(){
						try{
							while(true){
								try{
									java.net.Socket sock = jsock.accept();
									if (sock != null){
										Socket s = new Socket(sock);
										while(acceptedSocket != null) try{sleep(100);}catch(Exception e){};
										acceptedSocket = s;
									}else{
										System.out.println("Got null socket from accept()");
										return;
									}
								}catch(Exception e){
									//e.printStackTrace();
									return;
								}
							}
						}finally{
							acceptClosed = true;
						}

					}
				}.start();
				*/
			}catch(Exception e){
				connectionError = e;
				//System.out.println(e);
				//e.printStackTrace();
				createFrom(null);
			}
		}
	}.start();
	}catch(ewe.net.UnknownHostException e){
		failedToConnect = true;
		connectionError = e;
		return;
	}
}
/*
private static WeakCache errors;
//-------------------------------------------------------------------
private void setError(Object err)
//-------------------------------------------------------------------
{
	if (errors == null) errors = new WeakCache();
	errors.put(this,err);
}
//-------------------------------------------------------------------
private Object getError()
//-------------------------------------------------------------------
{
	if (errors == null) return null;
	return errors.get(this);
}
*/
//-------------------------------------------------------------------
private void throwOpenException(String defMessage) throws ewe.io.IOException
//-------------------------------------------------------------------
{
	Object got = connectionError;
	if (got instanceof String)
		throw new ewe.io.IOException((String)got);
	else if (got instanceof ewe.io.IOException)
		throw ((ewe.io.IOException)got);
	else if (got instanceof Throwable)
		throw new ewe.io.IOException(((Throwable)got).getMessage());
	else
		throw new ewe.io.IOException(defMessage);
}

/**
* Creates a server socket at the specified port in blocking mode. This may be
* used in a Coroutine without blocking other Coroutines.
* <p>If you want to connect to accept connections from a device via
* the infra-red port, set the host to be "infra-red" and set the port
* to be 0 when creating the ServerSocket.
**/
//===================================================================
public ServerSocket(int port,int backlog,InetAddress address) throws ewe.io.IOException
//===================================================================
{
	this(port,backlog,address == null ? null : address.getHostName(),null);
	if (!isOpen())
		throwOpenException("Could not create server socket.");;
}
//===================================================================
public ServerSocket(String host,int port) throws ewe.io.IOException
//===================================================================
{
	this(port,0,host,null);
	if (!isOpen())
		throwOpenException("Could not create server socket.");;
}

//===================================================================
public ServerSocket(int port) throws ewe.io.IOException
//===================================================================
{
	this(port,0,null);
}
//===================================================================
public ServerSocket(int port,int backlog) throws ewe.io.IOException
//===================================================================
{
	this(port,backlog,null);
}

//===================================================================
public ServerSocket(int port,IOHandle handle) {this(port,0,null,handle);}
//===================================================================
//===================================================================
public ServerSocket(int port,int backlog,String address,IOHandle handle)
//===================================================================
{
	//if (backlog == 0) backlog = 100;
	//if (address == null) address = InetAddress.getLocalHost();
	if (handle == null) doConnect(port,backlog,address,handle);
	else new serverSocketConnector(this,port,backlog,address,handle).startTask();
}
//-------------------------------------------------------------------
protected boolean doConnect(int port,int backlog,String address,IOHandle handle)
//-------------------------------------------------------------------
{
	if (handle != null) handle.errorCode = 0;
	_nativeCreate(port,backlog,address);
	while(true){
		int ret = checkIO(CHECK_LISTEN);
		if (ret == 1){
			if (handle != null) handle.set(handle.Succeeded);
			return true;
		}else if (ret == -1){
			if (handle != null) {
				if (connectionError != null) {
					handle.errorObject = connectionError;
					handle.error = connectionError.getMessage();
				}
				handle.errorCode = handle.IO_ERROR;
				handle.set(handle.Failed);
			}
			try{close();}catch(Exception e){}
			return false;
		}
		if (handle != null){
			if (handle.pleaseAbort){
				handle.errorCode = handle.IO_ABORTED;
				return false;
			}
		}
		if (ewe.sys.Coroutine.getCurrent() != null) ewe.sys.Coroutine.sleep(100);
		else ewe.sys.Vm.sleep(100);
	}
}

Object acceptedSocket = null;
boolean acceptClosed = false;

//-------------------------------------------------------------------
protected int checkIO(int which)
//-------------------------------------------------------------------
{
	if (which == CHECK_LISTEN){
		if (failedToConnect) return -1;
		else if (jsock != null) return 1;
		else return 0;
	}else{
		if (acceptClosed) return -1;
		if (acceptedSocket == null) return 0;
		return 1;
	}
}

//-------------------------------------------------------------------
protected Socket getAcceptedSocket()
//-------------------------------------------------------------------
{
	Socket s = (Socket)acceptedSocket;
	acceptedSocket = null;
	return s;
}
//===================================================================
public void close() throws ewe.io.IOException
//===================================================================
{
	if (jsock == null) return;
	try{
		jsock.close();
	}catch(Exception e){
		throw new ewe.io.IOException(e.getMessage());
	}
	jsock = null;
}
//===================================================================
public boolean isOpen()
//===================================================================
{
	return jsock != null;
}
/**
* This gets the local port the socket is listening on.
**/
//===================================================================
public int getLocalPort()
//===================================================================
{
	if (jsock == null) return 0;
	return jsock.getLocalPort();
}
//===================================================================
public InetAddress getInetAddress()
//===================================================================
{
	if (jsock == null) return null;
	if (localHost instanceof InetAddress) return (InetAddress)localHost;
	else try{
		return InetAddress.getByName(ewe.util.mString.toString(localHost));
	}catch(Exception e){
		return null;
	}
}
/**
* Blocking version. Will block current Coroutine or thread.
**/
//===================================================================
public Socket accept() throws ewe.io.IOException
//===================================================================
{
	IOHandle h = new IOHandle();
	doAccept(h = new IOHandle());
	if ((h.check() & h.Success) != 0) return (Socket)h.returnValue;
	else throw new ewe.io.IOException("Socket closed.");
}

//===================================================================
public IOHandle accept(IOHandle nullHandle)
//===================================================================
{
	nullHandle = new IOHandle(){
		public void stop(int reason){
			try{
				close();
			}catch(Exception e){}
			super.stop(reason);
		}
	};
	new socketAcceptor(this,nullHandle).startTask();
	return nullHandle;
}

//-------------------------------------------------------------------
protected int doAccept(IOHandle handle)
//-------------------------------------------------------------------
{
	if (handle != null) handle.errorCode = 0;
	final ewe.sys.Coroutine cr = ewe.sys.Coroutine.getCurrent();
	new Thread(){
		public void run(){
			try{
				java.net.Socket sock = jsock.accept();
				if (sock != null){
					Socket s = new Socket(sock);
					while(acceptedSocket != null) try{sleep(100);}catch(Exception e){};
					acceptedSocket = s;
					if (cr != null) cr.interrupt();
				}else{
					throw new Exception("Could not accept!");
				}
			}catch(Exception e){
				acceptClosed = true;
				if (cr != null) cr.interrupt();
				return;
			}
		}
	}.start();
	while(true){
		int ret = checkIO(CHECK_ACCEPT);
		if (ret == 1){
			if (handle != null) {
				handle.set(handle.Succeeded);
				handle.returnValue = getAcceptedSocket();
			}
			return ret;
		}else if (ret == -1){
			if (handle != null) {
				handle.errorCode = handle.IO_ERROR;
				handle.set(handle.Failed);
			}
			try{close();}catch(Exception e){}
			return -1;
		}
		if (handle != null){
			if (handle.pleaseAbort){
				handle.errorCode = handle.IO_ABORTED;
				return 0;
			}
		}
		if (cr != null) {
			cr.sleep(-1);
		}
		else ewe.sys.Vm.sleep(100);
	}
}
//##################################################################
}
//##################################################################

//##################################################################
class serverSocketConnector extends ewe.sys.TaskObject{
//##################################################################

String address;
int port;
ServerSocket sock;
int backlog;
//===================================================================
public serverSocketConnector(ServerSocket sock,int port,int backlog,String address,IOHandle handle)
//===================================================================
{
	super(handle);
	this.address = address;
	this.port = port;
	this.sock = sock;
	this.backlog = backlog;
}

//-------------------------------------------------------------------
protected void doRun()
//-------------------------------------------------------------------
{
	sock.doConnect(port,backlog,address,(IOHandle)handle);
	if (handle.errorCode == IOHandle.IO_ABORTED) handle.set(handle.Failed|handle.Aborted);
	else if (handle.errorCode != 0) handle.set(handle.Failed);
	else handle.set(handle.Succeeded);
}
//-------------------------------------------------------------------
protected void doStop(int reason)
//-------------------------------------------------------------------
{
	((IOHandle)handle).pleaseAbort = true;
}
//##################################################################
}
//##################################################################

//##################################################################
class socketAcceptor extends ewe.sys.TaskObject{
//##################################################################

ServerSocket sock;
//===================================================================
public socketAcceptor(ServerSocket sock,IOHandle handle)
//===================================================================
{
	super(handle);
	this.sock = sock;
}

//-------------------------------------------------------------------
protected void doRun()
//-------------------------------------------------------------------
{
	sock.doAccept((IOHandle)handle);
	if (handle.errorCode == IOHandle.IO_ABORTED) handle.set(handle.Failed|handle.Aborted);
	else if (handle.errorCode != 0) {
		handle.set(handle.Failed);
	}else handle.set(handle.Succeeded);
}
//-------------------------------------------------------------------
protected void doStop(int reason)
//-------------------------------------------------------------------
{
	((IOHandle)handle).pleaseAbort = true;
}
//##################################################################
}
//##################################################################
