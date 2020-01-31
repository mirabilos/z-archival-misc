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
import ewe.util.WeakCache;
/**
* A server socket is used to wait for incoming TCP/IP connections.
*<p>
* This operates similar to the java.net.ServerSocket.
* <p>
If you want to accept connections from a device via
* the infra-red port, set the host to be "infra-red" and set the port
* to be 0 when creating the ServerSocket.
*/
//##################################################################
public class ServerSocket{
//##################################################################
int dontUseThisVariable1;
int dontUseThisVariable2;
protected Object localHost;
protected int localPortNotUsed;
int dontUseThisVariable3;
int dontUseThisVariable4;
int dontUseThisVariable5;
int dontUseThisVariable6;


public static final int CHECK_LISTEN = 0;
public static final int CHECK_ACCEPT = 1;

//Native create always opens the socket for non-blocking IO.
//-------------------------------------------------------------------
private native String _nativeCreate(final int port,final int backlog,final String dotAddress);
//-------------------------------------------------------------------

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
//-------------------------------------------------------------------
private void throwOpenException(String defMessage) throws IOException
//-------------------------------------------------------------------
{
	Object got = getError();
	if (got instanceof String)
		throw new IOException((String)got);
	else if (got instanceof IOException)
		throw ((IOException)got);
	else if (got instanceof Throwable)
		throw new IOException(((Throwable)got).getMessage());
	else
		throw new IOException(defMessage);
}
/**
* Creates a server socket at the specified port in blocking mode.
* This method blocks the current thread until the ServerSocket is created or an exception is thrown.
* <p>If you want to connect to accept connections from a device via
* the infra-red port, set the host to be "infra-red" and set the port
* to be 0 when creating the ServerSocket.
 * @param address The address of the local machine to listen to.
* @param port The port number to listen to. If this is 0, then a new and unused port number will be used.
	Use getLocalPort() to determine which port was chosen for the ServerSocket.
* @param backlog The number of backlogged connections to accept.
**/
//===================================================================
public ServerSocket(int port,int backlog,InetAddress address) throws IOException
//===================================================================
{
	this(port,backlog,address == null ? null : address.getHostName(),null);
	dontUseThisVariable1 = dontUseThisVariable2 = 0;
	if (!isOpen())
		throwOpenException("Could not create server socket.");;
		//throw new IOException("Could not create server socket.");
}
/**
* Create a server socket on the local host at the specified port number.
*
* <p>
* This method blocks the current thread until the ServerSocket is created or an exception is thrown.
* @param port The port to listen for connections on. If the port number is zero a new
* port number will be assigned. To get the port number actually used, call getLocalPort().
* @param backlog The number of backlogged connections to accept.
* @exception IOException If an error occurs while trying to connect to the specified port.
*/
//===================================================================
public ServerSocket(int port,int backlog) throws IOException
//===================================================================
{
	this(port,backlog,null);
}
/**
 * Create a ServerSocket to listen on the local host for a specific address, on a specific port.
* This method blocks the current thread until the ServerSocket is created or an exception is thrown.
 * @param host The host name to listen to. If this is "infra-red" then the ServerSocket will listen
	on the infra-red port on the specified port.
 * @param port The port number to listen to. If this is 0, then a new and unused port number will be used (This
	also works for "infra-red").
	Use getLocalPort() to determine which port was chosen for the ServerSocket.
 * @exception IOException If an error occurs while trying to connect to the specified port.
 */
//===================================================================
public ServerSocket(String host,int port) throws IOException
//===================================================================
{
	this(port,0,host,null);
	if (!isOpen())
		throwOpenException("Could not create server socket.");;
		//throw new IOException("Could not create server socket.");
}
/**
* Creates a server socket at the specified port in non-blocking mode.
 * Provide the constructor with a new IOHandle and then use waitOnFlags() on the Handle to be set to Succeeded
 * or Failed.
 * @param port The port number to listen to. If this is 0, then a new and unused port number will be used.
	Use getLocalPort() to determine which port was chosen for the ServerSocket.
* @param handle if this is not null then the method will return immediately
and you can use the handle to wait on the handle for success or failure. If handle <b>is</b>
null, then the method will not return until the open operation succeeds or fails, blocking
all other Threads until it is done.
**/
//===================================================================
public ServerSocket(int port,IOHandle handle)
//===================================================================
{
	this(port,0,null,handle);
}
/**
 * Create a ServerSocket to listen on the local host  on a specific port.
* This method blocks the current thread until the ServerSocket is created or an exception is thrown.
 * @param port The port number to listen to. If this is 0, then a new and unused port number will be used.
	Use getLocalPort() to determine which port was chosen for the ServerSocket.
 * @exception IOException If an error occurs while trying to connect to the specified port.
 */
//===================================================================
public ServerSocket(int port) throws IOException
//===================================================================
{
	this(port,0,null);
}
/**
<b>You should use one of the other ServerSocket constructors which throw an IOException
on failure.</b><p>
This method starts the process for opening a ServerSocket at the specified IP address for the local host.
If the <b>handle</b> parameter is not null, then the method will return immediately
and you can use the handle to wait on the handle for success or failure. If handle <b>is</b>
null, then the method will not return until the open operation succeeds or fails, blocking
all other Threads until it is done. The isOpen() method can then be used to determine
if the ServerSocket was created successfully.<p>
* The address may be a name for the local host or an IP address for the
* local host. Since the Local host may have multiple addresses you can
* use InetAddress.getAllByName(InetAddress.getLocalHostName(),null) to
* get a list of available InetAddresses for the local host. You can then
* specify precisely which one you want to use by passing the value
* returned by InetAddress.getHostAddress() to the constructor for ServerSocket.<p>

* @param port The port to listen on. If this is zero then any available port is used.
You can call getLocalPort() after a successful.
* @param backlog the backlog of pending connections allowed on the ServerSocket.
* @param address the local host address to bind to, or null to bind to any on the local host.
* @param handle if this is not null then the method will return immediately
and you can use the handle to wait on the handle for success or failure. If handle <b>is</b>
null, then the method will not return until the open operation succeeds or fails, blocking
all other Threads until it is done.
*/
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
boolean doConnect(int port,int backlog,String host,IOHandle handle)
//-------------------------------------------------------------------
{
	if (handle != null) handle.errorCode = 0;
	localHost = host;
	if (localHost == null) localHost = InetAddress.getLocalHostName();
	if (!localHost.toString().startsWith("infra-red") && host != null){
		if (!InetAddress.isANetAddress(host)){
			try{
				localHost = InetAddress.getByName(host);
				host = ((InetAddress)localHost).getHostAddress();
			}catch(UnknownHostException e){
				if (handle != null) {
					handle.errorCode = handle.IO_ERROR;
					handle.set(handle.Failed);
				}
				setError(e);
				try{close();}catch(IOException e2){}
				return false;
			}
		}
	}else if (localHost.toString().startsWith("infra-red")){
		localHost = new InetAddress(host);
	}
	String err = _nativeCreate(port,backlog,host);
	if (err != null){
		SocketException se = new SocketException(err);
		if (handle != null) {
			handle.error = err;
			handle.errorCode = handle.IO_ERROR;
			handle.errorObject = se;
		}
		setError(se);
		return false;
	}
	while(true){
		int ret = checkIO(CHECK_LISTEN);
		if (ret == 1){
			if (handle != null) handle.set(handle.Succeeded);
			return true;
		}else if (ret == -1){
			if (handle != null) {
				handle.errorCode = handle.IO_ERROR;
				handle.set(handle.Failed);
			}
			setError(new IOException("listen() failed."));
			try{close();}catch(IOException e){}
			return false;
		}
		if (handle != null){
			if (handle.pleaseAbort){
				handle.errorCode = handle.IO_ABORTED;
				setError(new IOException("Connection aborted."));
				return false;
			}
		}
		if (ewe.sys.Coroutine.getCurrent() != null) ewe.sys.Coroutine.sleep(100);
		else ewe.sys.Vm.sleep(100);
	}
}
//-------------------------------------------------------------------
private native int checkIO(int which);
//-------------------------------------------------------------------
//-------------------------------------------------------------------
private native Socket getAcceptedSocket();
//-------------------------------------------------------------------
//===================================================================
public native void close() throws IOException;
//===================================================================
//===================================================================
public native boolean isOpen();
//===================================================================
/**
* This gets the local port the socket is listening on.
**/
//===================================================================
public native int getLocalPort();
//===================================================================
public InetAddress getInetAddress()
//===================================================================
{
	if (localHost instanceof InetAddress) return (InetAddress)localHost;
	else try{
		return InetAddress.getByName(ewe.util.mString.toString(localHost));
	}catch(Exception e){
		return null;
	}
}
/**
* This will accept an incoming TCP/IP connection - blocking version.
* This will block current Coroutine but allow others to
* continue. The only way to stop the accept is to close the socket.
**/
//===================================================================
public Socket accept() throws IOException
//===================================================================
{
	IOHandle h = new IOHandle();
	doAccept(h = new IOHandle());
	if ((h.check() & h.Success) != 0) return (Socket)h.returnValue;
	else throw new IOException("Socket closed.");
}
/**
* This will accept an incoming TCP/IP connection - non-blocking version.
* When the return ed handle status has the Success flag set, the
* handle.returnValue member will have a new open Socket to be used for IO.
* You can abort the accept by calling the stop() method of the returned IOHandle.
* @param nullHandle should always be null - since a new Handle is always returned.
* @return an IOHandle that you can use to monitor or stop the accept.
*/
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
/*
//===================================================================
public IOHandle accept(IOHandle handle)
//===================================================================
{
	if (handle == null) handle = new IOHandle();
	new socketAcceptor(this,handle).startTask();
	return handle;
}
*/
//-------------------------------------------------------------------
int doAccept(IOHandle handle)
//-------------------------------------------------------------------
{
	if (handle != null) handle.errorCode = 0;
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
			try{close();}catch(IOException e){}
			return -1;
		}
		if (handle != null){
			if (handle.pleaseAbort){
				handle.errorCode = handle.IO_ABORTED;
				return 0;
			}
		}
		if (ewe.sys.Coroutine.getCurrent() != null) ewe.sys.Coroutine.sleep(pauseUntilAccepted(-1));
		else ewe.sys.Vm.sleep(100);
	}
}

//-------------------------------------------------------------------
protected native int pauseUntilAccepted(int howLong);
//-------------------------------------------------------------------
//{return howLong;}
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


