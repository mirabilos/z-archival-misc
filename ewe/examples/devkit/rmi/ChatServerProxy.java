package samples.rmi;

import ewe.io.RemoteCall;
import ewe.io.RemoteCallTask;
import ewe.io.RemoteCallException;
import ewe.reflect.Wrapper;

/**
* This is a Proxy that is used on both the Server and Client side. On the Server side
* it acts as ChatClient and on the Client side it acts as a ChatServer.
**/
//########################################################################
public class ChatServerProxy
extends RemoteCallTask
implements ChatServer, ChatClient{
//########################################################################

ChatServer server;
ChatClient client;

/**
* Called by the server when a connection has been made to the client.
**/
//===================================================================
public ChatServerProxy(ewe.io.Stream connection, ChatServer localServer)
//===================================================================
{
	super(connection);
	this.server = localServer;
}
/**
* Called by the client when a connection has been made to the server.
**/
//===================================================================
public ChatServerProxy(ewe.io.Stream connection, ChatClient localClient)
//===================================================================
{
	super(connection);
	this.client = localClient;
}
/*
This is called on the client side by the remote server. It is called by the
messagePosted() method of the Proxy on the remote server.
*/
//===================================================================
	public void messagePostedRMI(String p1, String p2)
//===================================================================
{
		client.messagePosted(p1,p2);
}
/*
This is called on the server side by the local server, to send to the client on the other side.
*/
//===================================================================
	public void messagePosted(String p1, String p2)
//===================================================================
{
		RemoteCall c = newCall("messagePostedRMI(Ljava/lang/String;Ljava/lang/String;)V");
		c.add(p1).add(p2);
		c.callAsync(); // I'm not interested in receiving a reply.
}
/*
These are called on the server side from the remote client. Note that the
ChatClient parameter is ignored since that would be the remote ChatClient and a
reference to it will be useless on this side of the connection. Instead this Proxy
presents itself to the local server as the ChatClient.
*/
//===================================================================
	public String joinRMI(samples.rmi.ChatClient p1, String p2)
//===================================================================
{
	return server.join(this,p2);
}
//===================================================================
	public void leaveRMI(samples.rmi.ChatClient p1)
//===================================================================
{
	server.leave(this);
}
//===================================================================
	public void postMessageRMI(samples.rmi.ChatClient p1, String p2)
//===================================================================
{
	server.postMessage(this,p2);
}
/*
These are called on the client side to send to the server side.
*/
//===================================================================
	public String join(samples.rmi.ChatClient p1, String p2)
//===================================================================
{
	try{
		RemoteCall c = newCall("joinRMI(Lsamples/rmi/ChatClient;Ljava/lang/String;)Ljava/lang/String;");
		c.add(p1).add(p2);
		Wrapper w = c.call();
		return (String)w.getObject();
	}catch(RemoteCallException rce){
		Throwable t = rce.getException();
//
// Replace this exception handling.
//
		throw new RuntimeException(t == null ? rce.getMessage() : t.getMessage());
	}
}
//===================================================================
	public void leave(samples.rmi.ChatClient p1)
//===================================================================
{
	try{
		RemoteCall c = newCall("leaveRMI(Lsamples/rmi/ChatClient;)V");
		c.add(p1);
		c.call();
	}catch(RemoteCallException rce){
		Throwable t = rce.getException();
//
// Replace this exception handling.
//
		throw new RuntimeException(t == null ? rce.getMessage() : t.getMessage());
	}
}
//===================================================================
	public void postMessage(samples.rmi.ChatClient p1, String p2)
//===================================================================
{
	try{
		RemoteCall c = newCall("postMessageRMI(Lsamples/rmi/ChatClient;Ljava/lang/String;)V");
		c.add(p1).add(p2);
		c.call();
	}catch(RemoteCallException rce){
		Throwable t = rce.getException();
//
// Replace this exception handling.
//
		throw new RuntimeException(t == null ? rce.getMessage() : t.getMessage());
	}
}
//===================================================================
	protected void connectionClosed()
//===================================================================
{
	super.connectionClosed();
// Handle a closed connection here.
}
//########################################################################
}
//########################################################################
