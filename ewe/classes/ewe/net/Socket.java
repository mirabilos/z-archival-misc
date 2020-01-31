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
package ewe.net;
import ewe.io.*;
import ewe.sys.TimeOut;
import ewe.sys.Vm;
/**
 *A Socket is used to make a TCP/IP streaming connection to a remote host.
 *<p>For devices which have an infra red port and support IrSock (e.g. WinCE devices)
 * specifying a host name of "infra-red" will tell the socket to connect to any available
 * device on the infra-red port.
 */

public class Socket extends SocketBase implements StreamCanPause, OverridesClose
{
private static final int CHECK_CONNECT = 0;
private static final int CHECK_READ = 1;
private static final int CHECK_WRITE = 2;

/**
 * Opens a socket. This method establishes a socket connection by
 * looking up the given host and performing the 3 way TCP/IP handshake.
 * This is the blocking version of the constructor - may be used in a
 * thread without blocking other threads, however there is no
 * way to abort the process.<p>If you want to connect to a device via
 * the infra-red port, set the host to be "infra-red".
 * @param host the host name or IP address to connect to.
 * @param port the port number to connect to.
  */
//===================================================================
public Socket(String host, int port) throws IOException, UnknownHostException
//===================================================================
{
	this(host,port,null,0);
}
/**
 * Opens a socket. This method establishes a socket connection by
 * looking up the given host and performing the 3 way TCP/IP handshake.
 * This is the blocking version of the constructor - may be used in a
 * thread without blocking other threads, however there is no
 * way to abort the process.<p>If you want to connect to a device via
 * the infra-red port, set the host to be "infra-red".
 * @param host the host address to connect to.
 * @param port the port number to connect to.
  */
//===================================================================
public Socket(InetAddress host,int port) throws IOException
//===================================================================
{
	this(host.getHostName(),port,null,0);
}
/**
 * Opens a socket. This method establishes a socket connection by
 * looking up the given host and performing the 3 way TCP/IP handshake.
 * This is the blocking version of the constructor - may be used in a
 * thread without blocking other threads, however there is no
 * way to abort the process.<p>If you want to connect to a device via
 * the infra-red port, set the host to be "infra-red".
 * @param host the host name or IP address to connect to.
 * @param port the port number to connect to.
 * @param localHost the local host address to bind the socket to locally, or null for no local binding.
 * @param localPort the local port number to bind the socket to locally, or 0 for no local binding.
  */
//===================================================================
public Socket(String host, int port,InetAddress localHost,int localPort) throws IOException, UnknownHostException
//===================================================================
{
	this(host,port,localHost,localPort,null);
	if (!isOpen()){
		if ("unknown host".equals(error)) throw new UnknownHostException(host);
		throw new IOException(error);
	}
}
/**
 * Opens a socket. This method establishes a socket connection by
 * looking up the given host and performing the 3 way TCP/IP handshake.
 * This is the blocking version of the constructor - may be used in a
 * thread without blocking other threads, however there is no
 * way to abort the process.<p>If you want to connect to a device via
 * the infra-red port, set the host to be "infra-red".
 * @param host the host address to connect to.
 * @param port the port number to connect to.
 * @param localHost the local host address to bind the socket to locally, or null for no local binding.
 * @param localPort the local port number to bind the socket to locally, or 0 for no local binding.
  */
//===================================================================
public Socket(InetAddress host,int port,InetAddress localHost,int localPort) throws IOException
//===================================================================
{
	this(host.getHostName(),port,localHost,localPort);
}
/**
 * Opens a socket. This method establishes a socket connection by
 * looking up the given host and performing the 3 way TCP/IP handshake.
 * <p>
 * This is the non-blocking version of the constructor. It will return
 * immediately and you must check the IOHandle for successful connection. Passing a null
 * handle will make this a blocking call. The IOHandle can be used to abort the
 * connection by calling its stop() method.<p>If you want to connect to a device via
 * the infra-red port, set the host to be "infra-red".
 * @param host the host name or IP address to connect to
 * @param port the port number to connect to
 * @param handle a handle to use for checking for successful connection.
 */
//===================================================================
public Socket(String host, int port,IOHandle handle)
//===================================================================
{
	this(host,port,null,0,handle);
}
/**
 * Opens a socket. This method establishes a socket connection by
 * looking up the given host and performing the 3 way TCP/IP handshake.
 * <p>
 * This is the non-blocking version of the constructor. It will return
 * immediately and you must check the IOHandle for successful connection. Passing a null
 * handle will make this a blocking call. The IOHandle can be used to abort the
 * connection by calling its stop() method.<p>If you want to connect to a device via
 * the infra-red port, set the host to be "infra-red".
 * @param host the host name or IP address to connect to
 * @param port the port number to connect to
 * @param localHost the local host address to bind the socket to locally, or null for no local binding.
 * @param localPort the local port number to bind the socket to locally, or 0 for no local binding.
 * @param handle a handle to use for checking for successful connection.
 */
//===================================================================
public Socket(String host, int port,InetAddress localHost, int localPort, IOHandle handle)
//===================================================================
{
	remoteHost = host;
	remotePort = port;
 	if (handle == null) doConnect(host,port,localHost,localPort,null);
	else new socketConnector(this,host,port,localHost,localPort,handle).startTask();
}

//-------------------------------------------------------------------
boolean doConnect(String host,int port,InetAddress localHost,int localPort,IOHandle handle)
//-------------------------------------------------------------------
{
	boolean isInfraRed = host.startsWith("infra-red");
	if (handle != null) handle.errorCode = 0;
	if (!isInfraRed)
		if (!InetAddress.isANetAddress(host)){
			try{
				//remoteHost = InetAddress.getByName(host);
				IOHandle lookup = InetAddress.getAllByName(host,null);
				if (handle != null) lookup.waitOn(lookup.Success,new ewe.sys.TimeOut(250),handle,ewe.sys.TimeOut.Forever);
				else lookup.waitOn(lookup.Success);
				remoteHost = ((InetAddress [])lookup.returnValue)[0];
				host = ((InetAddress)remoteHost).getHostAddress();
			}catch(Exception e){
				error = "unknown host";
				if (handle != null) {
					handle.errorObject = getException(null);
					handle.errorCode = handle.IO_ERROR;
					handle.set(handle.Failed);
				}
				close();
				return false;
			}
		}
	error = null;
	_nativeCreate(host,port,localHost == null ? null : localHost.getHostAddress(),localPort);
	if (error != null){
		if (handle != null) {
			handle.errorObject = getException(null);
			handle.errorCode = handle.IO_ERROR;
			handle.set(handle.Failed);
		}
		close();
		return false;
	}
	int nt = napTime;
	int ni = napIterations;
	napTime = 100;
	if (isInfraRed) napIterations = 0;
	try{
	TimeOut t = Vm.isMobile() && !isInfraRed ? new TimeOut(3000) : TimeOut.Forever;
	while(true){
		int ret = checkIO(CHECK_CONNECT);
		if (ret == 1){
			if (handle != null) handle.set(handle.Succeeded);
			return true;
		}else if (t.hasExpired() || ret == -1){
			if (ret != -1) error = "Connection timed out.";
			if (handle != null) {
				handle.errorObject = getException(null);
				handle.errorCode = handle.IO_ERROR;
				handle.set(handle.Failed);
			}
			close();
			return false;
		}
		if (handle != null){
			if (handle.pleaseAbort){
				handle.errorCode = handle.IO_ABORTED;
				handle.set(handle.Aborted);
				return false;
			}
		}
		if (napIterations == 0) ewe.sys.mThread.nap(napTime);
		else nap();
	}
	}finally{
		napIterations = ni;
		napTime = nt;
	}
}
//Native create always opens the socket for non-blocking IO.
//-------------------------------------------------------------------
private native void _nativeCreate(String host, int port, String localHost, int localPort);
//-------------------------------------------------------------------

/**
* This returns:
* 1 - The socket is ready for the IO operation.
* 0 - The socket is not ready for the IO operaton.
* -1 - There is an error in the socket.
**/
//-------------------------------------------------------------------
private native int checkIO(int checkType);
//-------------------------------------------------------------------

//===================================================================
//public native boolean closeStream() throws IOException;
//===================================================================
/**
 * Closes the socket. Returns true if the operation is successful
 * and false otherwise.
 */
//===================================================================
public native boolean close();
//===================================================================


/**
 * Returns true if the socket is open and false otherwise. This can
 * be used to check if opening the socket was successful.
 */
//===================================================================
public native boolean isOpen();
//===================================================================


/**
 * Sets the timeout value for read operations. The value specifies
 * the number of milliseconds to wait from the time of last activity
 * before timing out a read operation. Passing a value of 0 sets
 * no timeout causing any read operation to return immediately with
 * or without data. The default timeout is 1500 milliseconds. This
 * method returns true if successful and false if the value passed
 * is negative or the socket is not open. Calling this method
 * currently has no effect under Win32 or WindowsCE. The
 * read timeout under those platforms will remain the system default.
 * @param millis timeout in milliseconds
 */
/*
//===================================================================
public native boolean setReadTimeout(int millis);
//===================================================================
*/
/**
* Get the remote port the socket is connected to.
**/
public int getPort() {return remotePort;}
/**
* Get the remote host the socket is connected to.
**/
public InetAddress getInetAddress()
{
	if (remoteHost instanceof InetAddress) return (InetAddress)remoteHost;
	else try{
		return InetAddress.getByName(ewe.util.mString.toString(remoteHost));
	}catch(Exception e){
		return null;
	}
}
/**
* Get the address of the local host the socket is bound to.
**/
public InetAddress getLocalAddress()
{
	try{
		String got = nativeGetLocalHost();
		return got == null ? null : InetAddress.getByName(got);
	}catch(Exception e){
		return null;
	}
}
private native String nativeGetLocalHost();
/**
* Get the local port the socket is bound to.
**/
public native int getLocalPort();

/**
* This returns:
* >0 = Number of bytes read.
* 0 = No bytes ready to read.
* -1 = Stream closed.
* -2 = IO error.
**/
//===================================================================
public native int nonBlockingRead(byte []buf,int start,int count);
//===================================================================
/**
* This returns:
* >0 = Number of bytes written.
* 0 = No bytes could be written yet.
* -1 = Stream closed.
* -2 = IO error.
**/
//===================================================================
public native int nonBlockingWrite(byte []buf,int start,int count);
//===================================================================

//===================================================================
public boolean flushStream() throws ewe.io.IOException {return true;}
//===================================================================

//===================================================================
public native int pauseUntilReady(int pauseType, int time);
//===================================================================
/*
{
	return 0;
}
*/
/**
 * Get an InputStream for reading from the connected Socket.
 */
//===================================================================
public ewe.io.InputStream getInputStream()
//===================================================================
{
	if (inputStream == null) inputStream = new SocketInputStream();
	return inputStream;
}
/**
 * Get an OutputStream for writing to the connected Socket.
 */
//===================================================================
public ewe.io.OutputStream getOutputStream()
//===================================================================
{
	if (outputStream == null) outputStream = new SocketOutputStream();
	return outputStream;
}
//===================================================================
public OutputStream toOutputStream()
//===================================================================
{
	return getOutputStream();
}
//===================================================================
public InputStream toInputStream()
//===================================================================
{
	return getInputStream();
}
//-------------------------------------------------------------------
private native int getSetSockParameter(int par,boolean b,int i,boolean isGet);
//-------------------------------------------------------------------

//-------------------------------------------------------------------
protected int getSocketParameter(int par)
//-------------------------------------------------------------------
{
	return getSetSockParameter(par,false,0,true);
}
//-------------------------------------------------------------------
protected int setSocketParameter(int par,boolean booleanValue,int intValue)
//-------------------------------------------------------------------
{
	return getSetSockParameter(par,booleanValue,intValue,false);
}

}

//##################################################################
class socketConnector extends ewe.sys.TaskObject{
//##################################################################

String host;
int port;
Socket sock;
InetAddress localHost;
int localPort;

//===================================================================
public socketConnector(Socket sock,String host,int port,InetAddress localHost,int localPort,IOHandle handle)
//===================================================================
{
	super(handle);
	this.host = host;
	this.port = port;
	this.sock = sock;
	this.localHost = localHost;
	this.localPort = localPort;
}

//-------------------------------------------------------------------
protected void doRun()
//-------------------------------------------------------------------
{
	sock.doConnect(host,port,localHost,localPort,(IOHandle)handle);
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


