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
import ewe.ui.*;
import ewe.sys.*;
import ewe.data.*;
/**
* This can be used to easily create a Client/Server connection over TCP/IP or
* through the EweSync RemoteConnection.
**/
//##################################################################
public abstract class ClientServerConnector extends TaskObject{
//##################################################################

/**
* This is a type to be used in the constructor.
**/
public static final int TCP_SOCKET = SocketMaker.LOCAL_SOCKET;
/**
* This is a type to be used in the constructor.
**/
public static final int REMOTE_TCP_SOCKET = SocketMaker.REMOTE_SOCKET;
/**
* This is a type to be used in the constructor.
**/
public static final int INFRA_RED = SocketMaker.INFRA_RED;
/**
* This is a type to be used in the constructor.
**/
public static final int REMOTE_SERVICE = SocketMaker.REMOTE_SERVICE;

public int type;
public String hostOrService;
public int port;
/**
* Set this true to let the user change the connection settings.
**/
public boolean showSelectionScreen = false;
/**
* This defaults to 30 seconds.
**/
public int timeoutInSeconds = 30;
/**
* Set this true if you don't want to show the cancel box.
**/
public boolean dontShowCancel = false;
/**
* You can optionanlly set this. If it is false then the cancel box will be displayed in its own
* window.
**/
public Frame parentFrame;
/**
* If this is set true then after the initial connection, no more connections
* will be accepted and the server socket will be closed.
**/
public boolean acceptOnlyOneConnection = false;

private boolean forceToBeClient = false;
private boolean forceToBeServer = false;

/**
 * Create a negotiated client-server connection between two initially peer entities.
	<p>
	This type of connection will be initiated at both entities and a negotiation will be done
	when both connect to decide which will act as the server and which as the client. This is
	not recommended on all platforms since the TCP/IP implementation on some (e.g. Win98) is
	flawed and this may not always work. The type of connection where one is definitely the
	client and the other the server is usually better.<p>

	Note that doing this over the REMOTE_SERVICE will usually work correctly since in this case
	the desktop will always be the server and the mobile device will always be the client.

 * @param type either TCP_SOCKET, REMOTE_TCP_SOCKET, REMOTE_SERVICE or INFRA_RED

 * @param hostOrService for TCP_SOCKET or REMOTE_TCP_SOCKET it should be the host name,
	for REMOTE_SERVICE (over the EweSync connection) it should be the service name, and for
	INFRA_RED it should be null.

 * @param port The port number to use. This should not be 0.

 */
//===================================================================
public ClientServerConnector(int type, String hostOrService, int port)
//===================================================================
{
	this.type = type;
	this.hostOrService = hostOrService;
	this.port = port;
}
/**
* This is for Infra-Red connections only - it creates a negotiated client-server connection over
* the infra-red port.
**/
//===================================================================
public ClientServerConnector(int port)
//===================================================================
{
	this(INFRA_RED,null,port);
}

/**
 * This creates a client-server connection where this entity will act as either the server or client as specified
	by the amServer parameter.

 * @param amServer if this is true then this entity is the server, otherwise it is the client.

 * @param type either TCP_SOCKET, REMOTE_TCP_SOCKET, REMOTE_SERVICE or INFRA_RED

 * @param hostOrService for TCP_SOCKET or REMOTE_TCP_SOCKET it should be the host name,
	for REMOTE_SERVICE (over the EweSync connection) it should be the service name, and for
	INFRA_RED it should be null.

 * @param port The port number to use. This should not be 0.

 */
//===================================================================
public ClientServerConnector(boolean amServer,int type, String hostOrService, int port)
//===================================================================
{
	this.type = type;
	this.hostOrService = hostOrService;
	this.port = port;
	forceToBeClient = !amServer;
	forceToBeServer = amServer;
}
/**
* This is for Infra-Red connections only - it creates a client-server connection over the
* infra-red port where this entity will act as either the server or client as specified
	by the amServer parameter.

 * @param amServer if this is true then this entity is the server, otherwise it is the client.

 @param port the port number to use (which should not be 0).

**/
//===================================================================
public ClientServerConnector(boolean amServer,int port)
//===================================================================
{
	this(amServer,INFRA_RED,null,port);
}
/**
* This is the server that will be listened to after the initial connection. This will only
* be valid if this entity is the server.
**/
public ServerSocket server;

protected Handle connecting;

/**
* If the connector is waiting for extra client connections this will close the server
* socket. It is safe to call this even if this is the client.
**/
//===================================================================
public void stopServer()
//===================================================================
{
	if (server != null) try{
		server.close();
	}catch(IOException e){}
}
//-------------------------------------------------------------------
protected void doStop(int reason)
//-------------------------------------------------------------------
{
	super.doStop(reason);
	stopServer();
	if (connecting != null) connecting.stop(reason);
}
/**
* This will be set if a connection is made and this connector is considered the client.
**/
public boolean isClient = false;
/**
* This will be set if a connection is made and this connector is considered the server.
**/
public boolean isServer = false;

/**
* This will be created during the connection setup.
**/
protected SocketMaker socketMaker;

/**
* This is used to make another connection to the server process on the same server port
* where the initial connection was made.
* @return A connected Socket.
* @exception IOException if a connection could not be made.
* @exception IllegalStateException if this connector is not the client.
*/
//===================================================================
public Socket connectToServer() throws IOException
//===================================================================
{
	if (!isClient) throw new IllegalStateException("Not the client.");
	if (type == INFRA_RED){
		return new Socket("infra-red",port);
	}else if (type == REMOTE_SERVICE || type == REMOTE_TCP_SOCKET){
		RemoteConnection rc = RemoteConnection.getConnection();
		rc.connectTimeOut = timeoutInSeconds;
		return (type == REMOTE_SERVICE) ?
			rc.connectToService(hostOrService):rc.connectToHost(hostOrService,port);
	}else {//if (type == TCP_SOCKET){
		return new Socket(hostOrService,port);
	}
}
/**
* Start the connection process and immediately return a Handle to indicate its progress.
**/
//===================================================================
public Handle connect()
//===================================================================
{
	return startTask();
}
/**
 * Start the connecton and wait until the connection is made.
 * @return true if this becomes the server, false if the client.
 * @exception IOException if the connection failed.
 */
//===================================================================
public boolean waitOnConnection() throws IOException
//===================================================================
{
	Handle h = connect();
	try{
		h.waitOn(h.Succeeded);
		return isServer;
	}catch(Exception e){
		if (h.errorObject instanceof Throwable) throw new IOException(((Throwable)h.errorObject).getMessage());
		else throw new IOException("Could not connect.");
	}
}
/**
* This gets called on the first connection and if it is determined that I am the client. You can make further
* connections to the server by calling connectToServer(), but this method will not be called again.
**/
//-------------------------------------------------------------------
protected abstract void client(Socket s); //{}
//-------------------------------------------------------------------

/**
 * This gets called every time the client connects - including the initial connection.
 * @param client The client that is connected.
 * @param firstConnection this is true if this is the initial connection.
 */
//-------------------------------------------------------------------
protected abstract void server(Socket client,boolean firstConnection); //{}
//-------------------------------------------------------------------

//-------------------------------------------------------------------
protected void doRun()
//-------------------------------------------------------------------
{
	boolean smServer = !forceToBeClient;
	String smTitle = "Client Server";
	if (forceToBeClient) smTitle = "Connect to server...";
	else if (forceToBeServer) smTitle = "Listen for client...";
	SocketMaker sm = new SocketMaker(type,smServer,smTitle);
	sm.forceToBeClient = forceToBeClient;
	sm.forceToBeServer = forceToBeServer;
	if (type == REMOTE_SERVICE) sm.serviceName = hostOrService;
	else if (type != INFRA_RED) sm.hostName = hostOrService;
	sm.port = port;
	sm.timeout = timeoutInSeconds;
	if (showSelectionScreen)
		if (!sm.showInitialFrame(parentFrame,Gui.CENTER_FRAME)){
			handle.set(Handle.Stopped|Handle.Aborted);
			return;
		}else{
			port = sm.port;
			type = sm.type;
			if (type == REMOTE_SERVICE) hostOrService = sm.serviceName;
			else hostOrService = sm.hostName;
			timeoutInSeconds = sm.timeout;
		}
	try{
		Object make = sm.makeClientServerConnection(parentFrame,Gui.CENTER_FRAME,!dontShowCancel);
		if (make instanceof Socket){
			isClient = true;
			client((Socket)make);
			handle.set(Handle.Succeeded);
		}else{
			Object [] m = (Object [])make;
			server = (ServerSocket)m[1];
			isServer = true;
			server((Socket)m[0],true);
			handle.set(Handle.Succeeded);
			if (acceptOnlyOneConnection) stopServer();
			else{
				while(true){
					try{
						Socket s = server.accept();
						server(s,false);
					}catch(IOException e){
						break;
					}
				}
			}
		}
	}catch(IOException e){
		handle.errorObject = e;
		handle.set(Handle.Failed);
		return;
	}
}
//##################################################################
}
//##################################################################

