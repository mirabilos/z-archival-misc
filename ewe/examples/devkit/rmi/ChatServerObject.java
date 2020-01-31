package samples.rmi;
import ewe.data.Property;
import ewe.data.PropertyList;
import ewe.io.IOException;
import ewe.net.ServerSocket;
import ewe.net.Socket;
import ewe.sys.TaskObject;
import ewe.sys.ThreadPool;


//##################################################################
public class ChatServerObject implements ChatServer{
//##################################################################
/**
* The Chat server port.
**/
public static final int chatPort = 2000;
/**
* This is true if the current application is running the Chat server for the local host.
**/
public static boolean amRunningSocketServer = false;
/**
* This is an active ChatServer.
**/
public static ChatServer localServer = new ChatServerObject();
/**
* A list of my clients.
**/
PropertyList clients = new PropertyList();
/**
* A ThreadPool for sending messages to the clients.
**/
ThreadPool threadPool = new ThreadPool();

//===================================================================
public ChatServerObject()
//===================================================================
{
	try{
		//
		// Try to get a ServerSocket for the chat port.
		//
		final ServerSocket ss = new ServerSocket(chatPort);
		amRunningSocketServer = true;
		new TaskObject(){
			protected void doRun(){
				try{
					ewe.sys.Vm.debug("Server listening on: "+ss.getInetAddress()+", "+ss.getLocalPort());
					while(true){
						Socket s = ss.accept();
						ewe.sys.Vm.debug("Accepted!");
						new ChatServerProxy(s,ChatServerObject.this);
					}
				}catch(Exception e){
					e.printStackTrace();
				}
			}
		}.startTask();
	}catch(IOException e){
	//
	// Could not listen on the chat port - there must be another chat server already there.
	//
	}
}
//-------------------------------------------------------------------
Property findClient(ChatClient client)
//-------------------------------------------------------------------
{
	if (client == null) return null;
	for (int i = 0; i<clients.size(); i++){
		Property p = (Property)clients.get(i);
		if (p.value == client) return p;
	}
	return null;
}
//===================================================================
public String join(ChatClient client,String name)
//===================================================================
{
	Property p = clients.get(name);
	if (p == null) {
		clients.set(name,client);
		postMessage(null,name+" has entered.");
		return null;
	}
	return "Name already in use.";
}
//===================================================================
public void leave(ChatClient client)
//===================================================================
{
	Property p = findClient(client);
	if (p == null) return;
	postMessage(null,p.name+" has left.");
}
//-------------------------------------------------------------------
void doPostMessage(ChatClient fromWho, String message)
//-------------------------------------------------------------------
{
	Property p = findClient(fromWho);
	String name = p == null ? null : p.name;
	for (int i = 0; i<clients.size(); i++){
		p = (Property)clients.get(i);
		ChatClient c = (ChatClient)p.value;
		try{
			c.messagePosted(name ,message);
		}catch(Exception e){}
	}
}
//===================================================================
public void postMessage(final ChatClient fromWho, final String message)
//===================================================================
{
	threadPool.addTask(new TaskObject(){
		protected void doRun(){
			doPostMessage(fromWho,message);
		}
	});
}
//##################################################################
}
//##################################################################
