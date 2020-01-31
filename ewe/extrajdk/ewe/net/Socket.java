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

/**
 * Socket is a TCP/IP network socket.
 * <p>
 * Under Java and Windows CE, if no network is present, the socket
 * constructor may hang for an extended period of time due to the
 * implementation of sockets in the underlying OS. This is a known
 * problem.
 * <p>
 * Here is an example showing data being written and read from a socket:
 *
 * <pre>
 * Socket socket = new Socket("www.yahoo.com", 80);
 * if (!socket.isOpen())
 *   return;
 * byte buf[] = new byte[10];
 * buf[0] = 3;
 * buf[1] = 7;
 * socket.writeBytes(buf, 0, 2);
 * int count = socket.readBytes(buf, 0, 10);
 * if (count == 10)
 *   ...
 * socket.close();
 * </pre>
 */

public class Socket extends SocketBase
{

private static final int CHECK_CONNECT = 0;
private static final int CHECK_READ = 1;
private static final int CHECK_WRITE = 2;

private java.net.Socket jsock;
//private java.io.BufferedOutputStream output;
//private java.io.InputStream input;
socketOutput sockOutput;
socketInput sockInput;
private Throwable error;
/**
 * Opens a socket. This method establishes a socket connection by
 * looking up the given host and performing the 3 way TCP/IP handshake.
 * @param host the host name or IP address to connect to
 * @param port the port number to connect to
 *
 * This is the blocking version of the constructor.
 */
//===================================================================
public Socket(String host, int port) throws ewe.io.IOException, ewe.net.UnknownHostException
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
 * @param host the host name or IP address to connect to.
 * @param port the port number to connect to.
 * @param localHost the local host address to bind the socket to locally, or null for no local binding.
 * @param localPort the local port number to bind the socket to locally, or 0 for no local binding.
  */
//===================================================================
public Socket(String host, int port,InetAddress localHost,int localPort) throws ewe.io.IOException, UnknownHostException
//===================================================================
{
	this(host,port,localHost,localPort,null);
	if (!isOpen()){
		if (error instanceof java.net.UnknownHostException)
			throw new ewe.net.UnknownHostException(error.getMessage());
		else
			throw new ewe.io.IOException(error.getMessage());
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
public Socket(InetAddress host,int port,InetAddress localHost,int localPort) throws ewe.io.IOException
//===================================================================
{
	this(host.getHostName(),port,localHost,localPort);
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
public Socket(InetAddress host,int port) throws ewe.io.IOException
//===================================================================
{
	this(host.getHostName(),port,null,0);
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
private boolean failedToConnect = false;
//-------------------------------------------------------------------

//===================================================================
public Socket(java.net.Socket s)
//===================================================================
{
	createFrom(s);
}
//-------------------------------------------------------------------
void createFrom(java.net.Socket s)
//-------------------------------------------------------------------
{
	synchronized(this){
		try{
			if (s == null) throw new Exception();
			jsock = s;
			//input = s.getInputStream();
			//if (input != null) input = new BufferedInputStream(input,1024*10);
			//output = new BufferedOutputStream(s.getOutputStream(),1024*10);
			sockInput = new socketInput(jsock);
			sockOutput = new socketOutput(jsock);
			//sockOutput.startRunning();
			//sockInput.startRunning();
		}catch(Exception e){
			jsock = null;
			//input = null;
			//output = null;
			failedToConnect = true;
		}
	}
}
//Native create always opens the socket for non-blocking IO.
//-------------------------------------------------------------------
private void _nativeCreate(final String host,final int port,final InetAddress localHost, final int localPort)
//-------------------------------------------------------------------
{
	new Thread(){
		public void run(){
			try{
				java.net.Socket sk = localHost == null ?
						new java.net.Socket(host,port) :
						new java.net.Socket(host,port,(java.net.InetAddress)localHost.nativeAddress,localPort);
				createFrom(sk);
			}catch(Exception e){
				error = e;
				createFrom(null);
			}
		}
	}.start();
}
//-------------------------------------------------------------------
protected boolean doConnect(String host,final int port,InetAddress localHost,int localPort,IOHandle handle)
//-------------------------------------------------------------------
{
	if (handle != null) handle.errorCode = 0;
	_nativeCreate(host,port,localHost,localPort);
	while(true){
		int ret = checkIO(CHECK_CONNECT);
		if (ret == 1){
			if (handle != null) handle.set(handle.Succeeded);
			return true;
		}else if (ret == -1){
			if (handle != null) {
				handle.errorCode = handle.IO_ERROR;
				handle.errorObject = getException("Could not connect");
				handle.set(handle.Failed);
			}
			close();
			return false;
		}
		if (handle != null){
			if (handle.pleaseAbort){
				handle.errorCode = handle.IO_ABORTED;
				handle.errorObject = getException("Aborted");
				handle.set(handle.Aborted|handle.Stopped);
				return false;
			}
		}
		nap();
	}
}

/**
* This returns:
* 1 - The socket is ready for the IO operation.
* 0 - The socket is not ready for the IO operaton.
* -1 - There is an error in the socket.
**/
//-------------------------------------------------------------------
private int checkIO(int checkType)
//-------------------------------------------------------------------
{
	switch(checkType){
		case CHECK_CONNECT:
			if (failedToConnect == true) return -1;
			synchronized(this){
				return jsock == null ? 0 : 1;
			}
			/*
		case CHECK_READ:
			try{
				int av = (input.available() < 1) ? 0 : 1;
				//System.out.println(av);
				return av;
			}catch(Exception e){
				e.printStackTrace();
				return -1;
			}
		default:
			if (output == null) return 0;
			try{
				return 1;
			}catch(Exception e){
				return -1;
			}
			*/
	}
	return -1;
}
public boolean closeStream() throws ewe.io.IOException
{
	if (jsock == null) return true;
	jsock = null;
	//ewe.sys.Vm.debug("Closing sockInput");
	if (sockInput != null) sockInput.close();
	//ewe.sys.Vm.debug("Closing sockOutput");
	if (sockOutput != null) sockOutput.close();
	//ewe.sys.Vm.debug("Leaving closeStream()");
	return true;
}


/**
 * Returns true if the socket is open and false otherwise. This can
 * be used to check if opening the socket was successful.
 */
public boolean isOpen() {return jsock != null;}


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
public native boolean setReadTimeout(int millis);

//-------------------------------------------------------------------
public int nonBlockingRead(byte []buff,int start,int count)
//-------------------------------------------------------------------
{
	if (sockInput == null) return -1;
	return sockInput.nonBlockingRead(buff,start,count);
}
//-------------------------------------------------------------------
public int nonBlockingWrite(byte []buff,int start,int count)
//-------------------------------------------------------------------
{
	if (sockOutput == null) return -1;
	return sockOutput.nonBlockingWrite(buff,start,count);
}
//===================================================================
public boolean flushStream() throws ewe.io.IOException
//===================================================================
{
	if (sockOutput != null) return sockOutput.flushStream();
	return true;
}
/**
* Get the remote port the socket is connected to.
**/
public int getPort() {return jsock == null ? 0 : jsock.getPort();}
/**
* Get the remote host the socket is connected to.
**/
public InetAddress getInetAddress()
{
	return jsock == null ? null : new InetAddress(jsock.getInetAddress());
}
/**
* Get the address of the local host the socket is bound to.
**/
public InetAddress getLocalAddress()
{
	return jsock == null ? null : new InetAddress(jsock.getLocalAddress());
}
/**
* Get the local port the socket is bound to.
**/
public int getLocalPort()
{
	return jsock == null ? 0 : jsock.getLocalPort();
}
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
public ewe.io.OutputStream toOutputStream()
//===================================================================
{
	return getOutputStream();
}
//===================================================================
public ewe.io.InputStream toInputStream()
//===================================================================
{
	return getInputStream();
}

//-------------------------------------------------------------------
protected int getSocketParameter(int par)
throws ewe.net.SocketException
//-------------------------------------------------------------------
{
	if (true) return 0;
	try{
		switch(par){
				case SO_LINGER:
					return jsock.getSoLinger();
				case TCP_NODELAY:
					return jsock.getTcpNoDelay() ? 1 : 0;
				case SO_TIMEOUT:
					return jsock.getSoTimeout();
				case RX_BUFFERSIZE:
					return jsock.getReceiveBufferSize();
				case TX_BUFFERSIZE:
					return jsock.getSendBufferSize();
				case SO_KEEPALIVE:
					return jsock.getKeepAlive() ? 1 : 0;
		}
	return 0;
	}catch(NoSuchMethodError e){
		return 0;
	}catch(java.net.SocketException e){
		throw new ewe.net.SocketException(e.getMessage());
	}
}
//-------------------------------------------------------------------
protected int setSocketParameter(int par,boolean booleanValue,int intValue)
throws ewe.net.SocketException
//-------------------------------------------------------------------
{
	if (true) return 0;
	try{
		switch(par){
			case SO_LINGER:
				jsock.setSoLinger(booleanValue,intValue);
				return 1;
			case TCP_NODELAY:
				jsock.setTcpNoDelay(booleanValue);
				return 1;
			case SO_TIMEOUT:
				jsock.setSoTimeout(intValue);
				return 1;
			case RX_BUFFERSIZE:
				jsock.setReceiveBufferSize(intValue);
				return 1;
			case TX_BUFFERSIZE:
				jsock.setSendBufferSize(intValue);
				return 1;
			case SO_KEEPALIVE:
				jsock.setKeepAlive(booleanValue);
				return 1;
		}
	return 0;
	}catch(NoSuchMethodError e){
		return 0;
	}catch(java.net.SocketException e){
		throw new ewe.net.SocketException(e.getMessage());
	}
}



	//##################################################################
	class socketOutput extends ewe.applet.JavaOutputStream{
	//##################################################################
	java.net.Socket sock;

	//-------------------------------------------------------------------
	protected void doClose() throws ewe.io.IOException
	//-------------------------------------------------------------------
	{
		ewe.io.IOException error = null;
		/*
		try{
			super.doClose();
		}catch(ewe.io.IOException e){
			error = e;
		}
		*/
		try{
			synchronized(Socket.this){
				Socket.this.sockOutput = null;
				//ewe.sys.Vm.debug("Closing Output...");
				if (Socket.this.sockInput == null){
					//ewe.sys.Vm.debug("Closing Socket.");
					sock.close();
				}
			}
		}catch(java.io.IOException e){
			error = new ewe.io.IOException(e.getMessage());
		}
		if (error != null) throw error;
	}
	//===================================================================
	public socketOutput(java.net.Socket jsock) throws java.io.IOException
	//===================================================================
	{
		super(new java.io.BufferedOutputStream(jsock.getOutputStream()));
		sock = jsock;
	}
	/*
	//-------------------------------------------------------------------
	protected void doWrite(byte [] bytes,int start,int count) throws ewe.io.IOException
	//-------------------------------------------------------------------
	{
		try{
			if (output != null) {
				output.write(bytes,start,count);
				output.flush();
			}
		}catch(java.io.IOException e){
			throw new ewe.io.IOException(e.getMessage());
		}
	}

	protected void closeOutputChannel()
	{
		try{
			output.close();
		}catch(Exception e){

		}
		try{
			sock.close();
		}catch(Exception e){

		}
	}
		*/
	//##################################################################
	}
	//##################################################################
	//##################################################################
	class socketInput extends ewe.applet.JavaInputStream{
	//##################################################################
	java.net.Socket sock;
	//-------------------------------------------------------------------
	protected void doClose() throws ewe.io.IOException
	//-------------------------------------------------------------------
	{
		ewe.io.IOException error = null;
		/*
		try{
			super.doClose();
		}catch(ewe.io.IOException e){
			error = e;
		}
		*/
		try{
			synchronized(Socket.this){
				sockInput = null;
				//ewe.sys.Vm.debug("Closing Input...");
				//new Exception().printStackTrace();
				if (sockOutput == null){
					//ewe.sys.Vm.debug("Closing Socket.");
					sock.close();
				}
			}
		}catch(java.io.IOException e){
			error = new ewe.io.IOException(e.getMessage());
		}
		if (error != null) throw error;
	}
	//===================================================================
	public socketInput(java.net.Socket jsock) throws java.io.IOException
	//===================================================================
	{
		super(new java.io.BufferedInputStream(jsock.getInputStream()));
		sock = jsock;
	}
	/*
	public socketInput()
	{
		this.input = new byte[1024*10];
	}
	//-------------------------------------------------------------------
	protected int doRead(byte [] bytes,int start,int count)
	//-------------------------------------------------------------------
	{
		try{
			int ret = -1;
			if (Socket.this.input != null)
				ret = Socket.this.input.read(bytes,start,count);
			return ret;
		}catch(Exception e){
			Socket.this.input = null;
			socketInput.this.close();
			return -1;
		}
	}
	*/
	//##################################################################
	}
	//##################################################################

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
