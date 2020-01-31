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
import ewe.net.*;
import ewe.util.*;
import ewe.ui.*;
import ewe.sys.Vm;
import ewex.registry.*;
import ewe.reflect.Type;
/**
* This class provides services to allow ewe applications on a desktop to communicate
* with ewe applications on a mobile pc when they are connected. On a Windows PC this
* connection is provided by the ActiveSync service.<p>
* When such a connection is made native applications on the desktop and mobile PC
* are run and this provides a constant streaming connection between the two devices.
* The RemoteConnection class then provides an API to access the services provided by the underlying
* connection.
**/
//##################################################################
public class RemoteConnection extends ewe.util.Errorable{
//##################################################################

static boolean testingRemote = false;
Socket sock;
/*

The ports go like this:

The ActiveSync server listens to clients on 270. It connects via RAPI unless run with the
/ZI option, in which canse it connects via TCP/IP to the device on port 271.

The RemoteConnection server running on the Zaurus mobile device listens to clients on 270 and
listens to incoming connection from the desktop server on port 271.

The RemoteConnection server running on the desktop (which is used for Zaurus only) listens to clients on 271.
It connects via TCP/IP to the device on port 271.

This allows the RemoteConnection server to co-exist with the ActiveSync server.

The activeSync server only runs when the device is connected, so its client port will always
be checked first.

When run as an emulator it works like this:

The first (desktop) instance listens to clients on 270 and will attempt to connect to this host
on port 272.

The second (emulated mobile) instance listens to clients on 271 and listens to incoming server
connections on port 272.
*/
static int rcport = 270;
static{
	if (testingRemote && ewe.sys.Vm.isMobile()) rcport = 272;
}
/**
* This is a time in milliseconds for timing out a connection between the ewe
* application and the underlying RemoteConnection.
**/
public int connectTimeOut = 2000;

//private boolean initError = false;
private String initError = null;
private boolean onEmulator = false;


//##################################################################
class PostedService{
//##################################################################

String host;
int port;

//##################################################################
}
//##################################################################

Hashtable services, pendingConnections;
boolean debug = false;
Socket CurConnection;
String CurAddress;

//-------------------------------------------------------------------
void handleClient(Socket sock) throws IOException
//-------------------------------------------------------------------
{
	try{
		int command = readAnInt(sock);
		if (debug) ewe.sys.Vm.debug("Command: "+command);
		switch(command){
			case 1: //CLIENT_HOST_CONNECT
			{
				int port = readAnInt(sock);
				String name = readAString(sock);
				writeAnInt(sock,2); //Tell it to connect directly
				writeAnInt(sock,port);
				writeAString(sock,name);
				break;
			}
			case 12: //CLIENT_GET_REGISTRY_DATA
			{
				try{
					String name = readAString(sock);
					TransferBlock tb = new TransferBlock();
					tb.source = tb.getNewID();
					tb.command = 12; //GET_REGISTRY_DATA
					tb.writeStringAlone(name);
					tb.addToWait();
					tb.write(CurConnection);
					TransferBlock reply = tb.waitReply(new ewe.sys.TimeOut(10000));
					if (reply == null) throw new IOException();
					if (reply.command != 2) throw new IOException();
					String data = reply.readStringAlone();
					writeAnInt(sock,1);
					writeAString(sock,data);
					if (debug) ewe.sys.Vm.debug("Registry data: "+data);
					break;
				}catch(Exception e){
					//e.printStackTrace();
					writeAnInt(sock,0);
					break;
				}

			}
			case 2: //CLIENT_SERVICE_CONNECT
			{
				try{
					String name = readAString(sock);
					TransferBlock tb = new TransferBlock();
					tb.source = tb.getNewID();
					tb.command = 1; //CONNECT_REQUEST
					tb.parameter = 0; //CONNECT_TO_SERVICE
					tb.writeStringAlone(name);
					tb.addToWait();
					tb.write(CurConnection);
					TransferBlock reply = tb.waitReply(new ewe.sys.TimeOut(10000));
					if (reply == null) throw new IOException();
					if (reply.command != 11) //CONNECT_DO_DIRECT
						throw new IOException();
					writeAnInt(sock,2); //Tell it to connect directly
					int port = reply.parameter;
					String host = reply.readStringAlone();
					host = CurAddress;
					writeAnInt(sock,port);
					writeAString(sock,host);
					if (debug) ewe.sys.Vm.debug("Connect directly to: "+host+":"+port);
					break;
				}catch(Exception e){
					//e.printStackTrace();
					writeAnInt(sock,0);
					break;
				}
			}
/*
				if (command == 2){
				}
*/
			case 5: //Get status
				writeAnInt(sock,ewe.sys.Vm.isMobile() ? 2 : 1);
				break;
			case 3: //SERVICE_NOTIFY
			{
				String name = readAString(sock);
				int port = readAnInt(sock);
				String host = readAString(sock);
				PostedService ps = new PostedService();
				ps.host = host;
				ps.port = port;
				services.put(name,ps);
				writeAnInt(sock,1);
				if (debug) ewe.sys.Vm.debug("Posted: "+name+" @ "+host+":"+port);
				break;
			}
			case 4: //Run app
			case 7: //Run ewe
			{
				if (CurConnection == null){
					writeAnInt(sock,0);
					break;
				}
				String app = readAString(sock);
				if (app.length() == 0) {
					writeAnInt(sock,0);
				}else{
					TransferBlock tb = new TransferBlock();
					tb.command = command == 4 ? 8 : 9;
					tb.writeString(app);
					try{
						tb.write(CurConnection);
					}catch(Exception e){
						writeAnInt(sock,0);
						break;
					}
					writeAnInt(sock,1);
				}
			}
		}
	}finally{
		sock.close();
	}
}

//-------------------------------------------------------------------
String getUnquotedEweFile(String command)
//-------------------------------------------------------------------
{
	String [] comms = ewe.util.mString.splitCommand(command,null);
	if (comms == null) return null;
	for (int i = 0; i<comms.length; i++){
		char [] chars = ewe.sys.Vm.getStringChars(comms[i]);
		if (chars.length <= 4) continue;
		int st = 0, len = chars.length;
		if (chars[0] == '"') {
			st++;
			len--;
		}
		if (chars[chars.length-1] == '"'){
			len--;
		}
		String s = new String(chars,st,len);
		if (s.toLowerCase().endsWith(".ewe")) return s;
	}
	return null;
}
//-------------------------------------------------------------------
void handleRemoteCommand(TransferBlock tb) throws ewe.io.IOException
//-------------------------------------------------------------------
{
	switch(tb.command){
			case 10: // CLOSE_NOW
				throw new ewe.io.IOException();
			case 1: //CONNECT_REQUEST
			{
				TransferBlock tb2 = new TransferBlock();
				tb2.source = tb.source;
				int port = tb.parameter;
				String name = tb.readStringAlone();
				if (port == 0){
					PostedService ps = (PostedService)services.get(name);
					if (ps == null && name.equals("_TestService_")) {
						String pte = ewe.sys.Vm.getPathToEweVM();
						if (pte != null){
							if (!pte.startsWith("\"")) pte = "\""+pte+"\"";
							pte += " ewe.io.TestService";
							if (debug) ewe.sys.Vm.debug("Going to exec: "+pte);
							ewe.sys.Vm.exec(pte);
							for (int i = 0; i<5; i++){
								ewe.sys.mThread.nap(1000);
								ps = (PostedService)services.get(name);
								if (ps != null) break;
							}
						}
					}
					if (ps == null){
						tb2.command = 3; //CONNECTION_REFUSED
						tb2.write(CurConnection);
						break;
					}else{
						port = ps.port;
						name = ps.host;
					}
				}
				if (port != 0){
					tb2.command = 11; //CONNECT_DO_DIRECT
					tb2.parameter = port;
					tb2.writeStringAlone(name);
					tb2.write(CurConnection);
				}
				break;
			}
			case 12: //GET_REGISTRY_DATA
			{
				TransferBlock tb2 = new TransferBlock();
				tb2.source = tb.source;
				String name = tb.readStringAlone();
				try{
					RegistryKey key = Registry.getLocalKey(0,name,false,false);
					String vname = null;
					if (key == null){
						int idx = name.lastIndexOf('\\');
						if (idx == -1) throw new Exception();
						vname = name.substring(idx+1);
					 	name = name.substring(0,idx);
						key = Registry.getLocalKey(0,name,false,false);
					}
					String got = key.getValue(vname).toString();
					if (got == null) throw new Exception();
					tb2.command = 2;
					tb2.writeStringAlone(got);
					tb2.write(CurConnection);
					break;
				}catch(Exception e){
					tb2.command = 3;
					tb2.write(CurConnection);
					break;
				}
			}
			case 8:
			case 9:
			{
				try{
					String comm = tb.readString();
					if (tb.command == 8)
						ewe.sys.Vm.exec(comm);
					else{
						String eweFile = getUnquotedEweFile(comm);
						if (eweFile != null)
							if (!File.getNewFile(eweFile).exists()){
								if (debug) ewe.sys.Vm.debug(eweFile+" does not exist.");
								break;
							}
						String pte = ewe.sys.Vm.getPathToEweVM();
						if (pte == null) break;
						if (!pte.startsWith("\"")) pte = "\""+pte+"\"";
						if (debug) ewe.sys.Vm.debug("Going to exec: "+pte+" "+comm);
						ewe.sys.Vm.exec(pte+" "+comm);
					}
				}catch(Exception e){}
				break;
			}
			default:
			{
				if (pendingConnections == null) break;
				TransferBlock found = (TransferBlock)pendingConnections.get(new ewe.sys.Long().set(tb.source));
				if (found == null) break;
				found.acceptReply(tb);
				break;
			}
	}
}
//-------------------------------------------------------------------
void startClient() throws IOException
//-------------------------------------------------------------------
{
	services = new Hashtable();
	final ServerSocket ss = new ServerSocket(null,rcport);
	clientSocket = ss;
	new ewe.sys.mThread(){
		public void run(){
			try{
				while(true){
					final Socket s = ss.accept();
					new ewe.sys.mThread(){
						public void run(){
							try{
								handleClient(s);
							}catch(Exception e){
								//e.printStackTrace();
							}
						}
					}.start();
				}
			}catch(Exception e){
				return;
			}finally{
				try{ss.close();}catch(Exception e2){}
			}
		}
	}.start();
}

/*
	final ServerSocket ts = new ServerSocket(null,testPort);
	new ewe.sys.mThread(){
		public void run(){
			try{
				while(true){
					Socket s = ts.accept();
					s.close();
				}
			}catch(IOException e){}
		}
	}.start();
*/

//-------------------------------------------------------------------
void runServer(final Socket sock)
//-------------------------------------------------------------------
{
	try{
		CurConnection = sock;
		CurAddress = sock.getInetAddress().getHostAddress();
		new TransferBlock().write(sock);
		final ewe.sys.TimeOut time = new ewe.sys.TimeOut(10000);
		if (!testingRemote) new ewe.sys.mThread(){
			public void run(){
				while(true){
					nap(time.remaining());
					if (!time.hasExpired()) continue;
					sock.close();
					break;
				}
			}
		}.start();
		while(true){
				TransferBlock tb = new TransferBlock();
				tb.read(sock);
				time.reset();
				if (tb.command != 0 && debug) ewe.sys.Vm.debug(tb.toString());
				if (tb.command == 0) tb.write(sock);
				else{
					handleRemoteCommand(tb);
				}
		}
	}catch(IOException e){
		sock.close();
	}finally{
		CurConnection = null;
	}
}

static ServerSocket clientSocket;

//-------------------------------------------------------------------
static void exit(TaskbarWindow tb)
//-------------------------------------------------------------------
{
	try{
		clientSocket.close();
	}catch(Exception e){
	}
	tb.close();
	ewe.sys.mThread.nap(1000);
	ewe.sys.Vm.exit(0);
}
//-------------------------------------------------------------------
private static String input(String prompt, String initial, int width)
//-------------------------------------------------------------------
{
		Type ty = new Type("ewe.ui.InputBox");
		if (!ty.exists()) return null;
		Object obj = ty.newInstance("(Ljava/lang/String;)V",new Object[]{prompt});
		if (obj == null) return null;
		return (String)ty.invoke(obj,"input(Ljava/lang/String;I)Ljava/lang/String;",new Object[]{initial,new Integer(width)});
}
//-------------------------------------------------------------------
RemoteConnection(final TaskbarWindow tb,final boolean isEmulated) throws IOException
//-------------------------------------------------------------------
{
	blockLock = new ewe.sys.Lock();
	String zip = isEmulated ? "127.0.0.1" : null;
	if (zip == null)
	try{
		zip = (String)ewex.registry.Registry.getLocalKey(ewex.registry.Registry.HKEY_LOCAL_MACHINE,"Software\\EweSoft\\EweSync",true,true).getValue("ZaurusIP");
		if (zip == null) throw new NullPointerException();
	}catch(Exception e){
		zip = "192.168.129.201";
		zip = input("Zaurus IP Address",zip,20);
		if (zip == null) {
			exit(tb);
			return;
		}
	}
	final String zaurusIP = zip;
	//
	// The desktop Zaurus server listens on 271, as does the emulated mobile.
	//
	if (!isEmulated || ewe.sys.Vm.isMobile()) rcport = 271;
	startClient();
	if (isEmulated && !ewe.sys.Vm.isMobile()){
		ewe.sys.Vm.execEwe(new String[]{"/r ewe.io.RemoteConnection emulator"},null);
		ewe.sys.mThread.nap(5000); //Very important to sleep until the other starts.
	}
	new ewe.sys.mThread(){
		public void run(){
			try{
				if (!isEmulated || !ewe.sys.Vm.isMobile()){
					while(true){
						//ewe.sys.Vm.debug("Checking...");
						tb.setIconAndTip("Disconnected","EweSync Server Disconnected.");
						Socket got = null;
						try{
							got = new Socket(zaurusIP,isEmulated ? 272 : 271);
						}catch(IOException e){
							nap(1000);
							continue;
						}
						//
						// Here we start the sync processes.
						//
						try{
							String keyn = "Software\\EweSoft\\EweSync\\";
							keyn += isEmulated ? "EmulatorAutoStart" : "AutoStart";
							ewex.registry.RegistryKey key = ewex.registry.Registry.getLocalKey(ewex.registry.Registry.HKEY_LOCAL_MACHINE,keyn,true,false);
							for (int i = 0;;i++){
								StringBuffer sb = new StringBuffer();
								Object com = key.getValue(i,sb);
								if (!(com instanceof String)) break;
								try{
									ewe.sys.Vm.exec((String)com);
								}catch(IOException e){}
							}
						}catch(Exception e){}
						//
						tb.setIconAndTip("Connected","EweSync Server Connected.");
						//ewe.sys.Vm.debug("Running...");
						runServer(got);
					}
				}else{
					ServerSocket server = new ServerSocket(null,272);
					tb.setIconAndTip("Disconnected","EweSync Mobile Emulator Disconnected.");
					Socket sock = server.accept();
					server.close();
					tb.setIconAndTip("Connected","EweSync Mobile Emulator Connected.");
					runServer(sock);
					exit(tb);
				}
				}catch(Exception e){
					e.printStackTrace();
				}
			}
	}.start();
}
//-------------------------------------------------------------------
RemoteConnection(final Control display) throws IOException
//-------------------------------------------------------------------
{
	blockLock = new ewe.sys.Lock();
	int port = 271;
	startClient();
	final ServerSocket ss = new ServerSocket(null,port);
	new ewe.sys.mThread(){
		public void run(){
			while(true){
				try{
					if (display != null) display.setText("Waiting for connection...");
					final Socket sock = ss.accept();
					if (sock == null) break;
					if (display != null) display.setText("Connected to remote.");
					runServer(sock);
				}catch(Exception e){
				}
			}
		}
	}.start();
}

private native int getServicePort();

//-------------------------------------------------------------------
protected RemoteConnection()
//-------------------------------------------------------------------
{
	//PrintWriter pw = ewe.sys.Vm.out();
	int gp = 0;
	try{
		gp = getServicePort();
	}catch(Error e){
		gp = 0;
	}
	//pw.println("Port: "+gp);
	if (gp != 0) rcport = gp;
	if ((Vm.getParameter(Vm.VM_FLAGS) & Vm.VM_FLAG_IS_MOBILE) != 0){
		int st = getStatus();
		//pw.println("Status: "+st);
		if (st == IS_MOBILE) return;
		rcport = 271;
		onEmulator = true;
		//ewe.sys.Vm.messageBox("getStatus()",st+" on "+rcport,0);
		//pw.println("Trying - "+rcport);
		if (getStatus() == IS_MOBILE) return;
		initError = "Could not communicate with RemoteConnection agent.";
		return;
	}else{
		//ewe.sys.Vm.messageBox("RC","I am not mobile!",0);
	}
	int st = getStatus();
	if (st != IS_MOBILE && st != IS_DESKTOP){
		rcport = 271;
		if (getStatus() != IS_DESKTOP){
			initError = "Could not communicate with RemoteConnection agent.";
			return;
		}
	}else if (rcport != 271 && !testingRemote){
		rcport = 271;
		onEmulator = (getStatus() == IS_MOBILE);
		rcport = 270;
	}
	return;
}
private static RemoteConnection curConnection;
/**
* Use this to get an instance of a RemoteConnection. This will re-use the
* previous one.
* @return A RemoteConnection instance.
* @exception IOException if there is an error getting the connection.
*/
//===================================================================
public static RemoteConnection getConnection() throws IOException
//===================================================================
{
	if (curConnection != null) {
		int st = curConnection.getStatus();
		if (st == IS_MOBILE || st == IS_DESKTOP)
			return curConnection;
	}
	RemoteConnection rc = new RemoteConnection();
	if (rc.initError != null) throw new ewe.io.IOException(rc.initError);
	return curConnection = rc;
}
/**
* Use this to get a new instance of a RemoteConnection.
* @return A new RemoteConnection instance.
* @exception IOException if there is an error getting the connection.
*/
//===================================================================
public static RemoteConnection getNewConnection() throws IOException
//===================================================================
{
	curConnection = null;
	return getNewConnection();
}
/**
* @deprecated use getConnection() instead.
**/
//===================================================================
public static RemoteConnection getInstance()
//===================================================================
{
	try{
		return getConnection();
	}catch(IOException e){
		return null;
	}
}
/**
* This is used when using the EweSync emulator. This allows you to write and
* test applications which use the remote connection API without needing a Mobile PC.
* Both the PC side and Mobile side application can run on the desktop with the emulator
* providing a connection between the two.<br>
* This method call tells the RemoteConnection object that it is to connect to the emulator
* as if it were a mobile PC. This method should be called by the class which is to be run
* on the mobile PC in the final application.
*
* If this method is called while running on a Mobile PC it will have no effect and so it is
* safe to leave in the method call when deploying the application.
* @returns
* IS_MOBILE = This remote connection has been successfully set up as the mobile pc. This is
* the only return value which should be considered successful.<br>
* -1 = Failed to connect to the underlying connection (true connection or emulator).<br>
* IS_DESKTOP = This remote connection can only be setup as a desktop. This should be considered
* a failure condition.<br>
**/
/*
//===================================================================
public int emulateMobile()
//===================================================================
{
	int st = getStatus();
	if (st != IS_DESKTOP) {
		//ewe.sys.Vm.debug("First getStatus(): "+st,0);
		return st;
	}
	rcport = 271;
	return getStatus();
}
*/
//-------------------------------------------------------------------
private Socket getSock(int command,String firstString)
//-------------------------------------------------------------------
{
	IOHandle connect = new IOHandle();
	Socket sock = new Socket(/*InetAddress.getLocalHostName()*/"127.0.0.1",rcport,connect);
	try{
		sock.setTcpNoDelay(true);
	}catch(SocketException e){}
	if (!connect.waitOnFlags(connect.Success,new ewe.sys.TimeOut(connectTimeOut)))
	/*if (!sock.isOpen())*/ return (Socket)returnError("Couldn't connect to remote services.",null);
	if (command == 0) return sock;
	if (!writeInt(sock,command)) return null;
	if (firstString == null) return sock;
	if (!writeString(sock,firstString)) return null;
	return sock;
}
/**
* This executes a command line on the remote machine - it works on the desktop or mobile pc.
* If there are any spaces in the path to the executable file, then that path should be enclosed
* in double quotation marks. e.g.:<br>
* <pre>
* runRemoteApp("\"\\Program Files\\Calculator.exe\"");
* </pre>
* @param commandLine The command line to run, including program arguments.
* @return true if the request was successfully sent across, false if not. A return value of
* true does not indicate that the remote executable was sucessfully run.
* @deprecated use execRemote() instead.
**/
//===================================================================
public boolean runRemoteApp(String commandLine)
//===================================================================
{
	Socket sock = getSock(4,commandLine);
	if (sock == null) return false;
	return getReply(sock,true);
}
/**
* This executes the ewe vm on the remote machine with the provided program arugments. You should
* pass at least one ewe file as a program argument. If the path for that ewe file contains spaces,
* then you should enclose it in double quotation marks.
* <pre>
* runRemoteEwe("\"\\Program Files\\MyApp.ewe\"");
* </pre>
* @param commandLine The command line to run, including program arguments.
* @return true if the request was successfully sent across, false if not. A return value of
* true does not indicate that the remote executable was sucessfully run.
* @deprecated use execRemote() instead.
**/
//===================================================================
public boolean runRemoteEwe(String eweCommandLine)
//===================================================================
{
	Socket sock = getSock(7,eweCommandLine);
	if (sock == null) return false;
	return getReply(sock,true);
}
/**
 * Run command lines on the remote.
 * @param commandLines The line to run or remote Registry value that stores the command to be run.
 * @param execOptions This can be any of MOBILE_GET_COMMAND_FROM_REGISTRY,  MOBILE_IS_EWE_APPLICATION, OR'ed together.
 * @exception ewe.io.IOException If the command could not be executed.
 */
//===================================================================
public void execRemote(String [] commandLines,int execOptions) throws ewe.io.IOException
//===================================================================
{
	for (int i = 0; i<commandLines.length; i++){
		String toRun = commandLines[i];
		String error = "Could not run mobile application: "+toRun;
		if ((execOptions & MOBILE_GET_COMMAND_FROM_REGISTRY) != 0){
			toRun = getRegistryValue(toRun);
			if (toRun == null) throw new IOException(error);
		}
		Socket sock = getSock(((execOptions & MOBILE_IS_EWE_APPLICATION) != 0) ? 7 : 4, toRun);
		if (sock == null) throw new IOException(error);
		if (!getReply(sock,true)) throw new IOException(error);
	}
}
/**
 * Run a command line on the remote.
 * @param commandLine The line to run or remote Registry value that stores the command to be run.
 * @param execOptions This can be any of MOBILE_GET_COMMAND_FROM_REGISTRY,  MOBILE_IS_EWE_APPLICATION, OR'ed together.
 * @exception ewe.io.IOException If the command could not be executed.
 */
//===================================================================
public void execRemote(String commandLine,int execOptions) throws ewe.io.IOException
//===================================================================
{
	execRemote(new String[]{commandLine},execOptions);
}
//-------------------------------------------------------------------
boolean getReply(Socket sock,boolean close)
//-------------------------------------------------------------------
{
	if (readInt(sock) != 1) {
		sock.close();
		return returnError("Remote services reported an error.",false);
	}
	if (close) sock.close();
	return true;
}

//-------------------------------------------------------------------
Object [] getConnectReply(Socket sock,boolean close)
//-------------------------------------------------------------------
{
	try{
		int read = readInt(sock);
		if (read == 1) return new Object[]{sock};
		if (read == 2) {
			int port = readInt(sock);
			String hostName = readString(sock);
			//ewe.sys.Vm.debug("I should connect directly to: "+hostName+":"+port);
			//new ewe.ui.MessageBox("Direct!",hostName+":"+port,0).execute();
			return new Object[]{hostName,new ewe.sys.Long().set(port)};
		}
		return null;
	}finally{
		if (close) sock.close();
	}
}

//-------------------------------------------------------------------
Socket getConnectedSocket(Socket sock)
//-------------------------------------------------------------------
{
	Object [] ret = getConnectReply(sock,false);
	if (ret == null) return null;
	if (ret[0] instanceof Socket) return (Socket)ret[0];
	if (ret[0] instanceof String){
		sock.close();
		try{
			Socket got = new Socket((String)ret[0],(int)((ewe.sys.Long)ret[1]).value);
			return got;
		}catch(IOException e){
			return null;
		}
	}
	return null;
}
//-------------------------------------------------------------------
int getReplyValue(Socket sock,boolean close)
//-------------------------------------------------------------------
{
	int ret = readInt(sock);
	if (close) sock.close();
	return ret;
}
/**
* This is used by an application to notify the connection that it has created
* and is listening to a TCP/IP server port on the specified hostName and port number.
* The server port is then associated with the provided service name. A remote ewe application
* could then connect to the named service by using connectToService().
* @param service The service name to post. Note that this IS case sensitive.
* @param hostName The name of the host used for the connection.
* @param port The port number of the connection.
* @return true if successful, false if not.
**/
//===================================================================
public boolean postService(String service,String hostName,int port)
//===================================================================
{
	Socket sock = getSock(3,service);
	if (sock == null) return false;
	if (!writeInt(sock,port)) return false;
	if (!writeString(sock,hostName)) return false;
	return getReply(sock,true);
}
/**
* This is used by an application to notify the connection that it has created
* and is listening to a TCP/IP server port on the specified hostName and port number.
* The server port is then associated with the provided service name. A remote ewe application
* could then connect to the named service by using connectToService().
* @param service The service name to post. Note that this IS case sensitive.
* @param serverSocket The socket being listened to.
* @return true if successful, false if not.
 */
//===================================================================
public boolean postService(String service,ServerSocket serverSocket)
//===================================================================
{
	return postService(service,serverSocket.getInetAddress().getHostAddress(),serverSocket.getLocalPort());
}
/**
 * This creates a ServerSocket on a new port and then posts the service to the RemoteConnecion.
 * @param serviceName The name of the service to be posted.
 * @return The ServerSocket created.
 * @exception ewe.io.IOException If ther ServerSocket could not be created or if the service
 * could not be posted.
 */
//===================================================================
public static ServerSocket createService(String serviceName) throws ewe.io.IOException
//===================================================================
{
	ServerSocket ss = new ServerSocket(null,0);
	try{
		RemoteConnection rc = getConnection();
		if (!rc.postService(serviceName,ss)) throw new ewe.io.IOException("Could not post service: "+rcport);
		return ss;
	}catch(Exception e){
		try{
			ss.close();
		}catch(Exception e2){}
		if (e instanceof IOException) throw (IOException)e;
		else if (e instanceof RuntimeException) throw (RuntimeException)e;
		else throw new IOException(e.getMessage());
	}
}
/**
* This is used by an application to connect to a host and port which is accessible from the
* remote machine (including the remote machine itself).
* @return A socket which can be used to communicate with the specified port if successful, null
* if not.
**/
//===================================================================
public Socket connectToHost(String hostName,int port)
//===================================================================
{
	Socket sock = getSock(1,null);
	if (sock == null) return null;
	if (!writeInt(sock,port)) return null;
	if (!writeString(sock,hostName)) return null;
	return getConnectedSocket(sock);
}
/**
* This is used by an application to connect to a service on the remote machine. If the service
* has not been posted or is not running, the underlying service on the remote machine will look
* up in the registry (or equivalent) under the AutoStart, EmulatorAutoStart and Services keys to
* see if the service has been registered (the EweConfig app is used to register services) and if it has it
* will attempt to start the service.
* @return A socket which can be used to communicate with the specified port if successful, null
* if not.
**/
//===================================================================
public Socket connectToService(String service)
//===================================================================
{
	Socket sock = getSock(2,service);
	if (sock == null) return null;
	return getConnectedSocket(sock);
}
//===================================================================
public String getRegistryValue(String keyName)
//===================================================================
{
	Socket sock = getSock(12,keyName);
	if (sock == null) return null;
	if (!getReply(sock,false)) return null;
	String ret = readString(sock);
	sock.close();
	return ret;
}
/**
* This is returned by getStatus().
**/
public static final int IS_DESKTOP = 1;
/**
* This is returned by getStatus().
**/
public static final int IS_MOBILE = 2;
/**
* This is used to check the RemoteConnection status.
*@return
* -1 = Remote Connection server is not running.<br>
* 0 = Remote Connection is running but there is an internal error.<br>
* IS_DESKTOP = Remote Connection is running and OK and this process is running on the Desktop.<br>
* IS_MOBILE = Remote Connection is running and OK and this process is running on a Mobile PC.<br>
**/
//===================================================================
public int getStatus()
//===================================================================
{
	Socket sock = getSock(5,null);
	if (sock == null) return -1;
	int ret = getReplyValue(sock,true);
	return ret;
}
/**
* This returns true if the RemoteConnection is running on an EweSync Emulator.
**/
//===================================================================
public boolean isEmulated()
//===================================================================
{
	return onEmulator;
}
//-------------------------------------------------------------------
boolean myWrite(Socket sock,byte [] toWrite)
//-------------------------------------------------------------------
{
	if (sock.writeBytes(toWrite,0,toWrite.length) != toWrite.length)
		return returnError("Error writing to remote services.",false);
	return true;
}
//-------------------------------------------------------------------
boolean writeInt(Socket sock,int out)
//-------------------------------------------------------------------
{
	byte [] toGo = new byte[4];
	Utils.writeInt(out,toGo,0,4);
	return myWrite(sock,toGo);
}
//-------------------------------------------------------------------
String readString(Socket sock)
//-------------------------------------------------------------------
{
	try{
		return readAString(sock);
	}catch(IOException e){
		return null;
	}
}
//-------------------------------------------------------------------
String readAString(Socket sock) throws IOException
//-------------------------------------------------------------------
{
	int len = readAnInt(sock);
	if (len <= 0) return "";
	byte [] got = new byte[len];
	ewe.io.IO.readAll(sock,got);
	return Utils.decodeJavaUtf8String(got,0,got.length);
}
//-------------------------------------------------------------------
boolean writeString(Socket sock,String out)
//-------------------------------------------------------------------
{
	try{
		writeAString(sock,out);
		return true;
	}catch(IOException e){
		return false;
	}
}
//-------------------------------------------------------------------
void writeAString(Socket sock,String out) throws IOException
//-------------------------------------------------------------------
{
	if (out == null) out = "";
	byte [] got = Utils.encodeJavaUtf8String(out);
	writeAnInt(sock,got.length);
	if (got.length != 0) sock.write(got);
}
//-------------------------------------------------------------------
int readAnInt(Stream s) throws IOException
//-------------------------------------------------------------------
{
	byte [] toGo = new byte[4];
	IO.readAll(s,toGo,0,4);
	return Utils.readInt(toGo,0,4);
}
//-------------------------------------------------------------------
void writeAnInt(Socket sock,int out) throws IOException
//-------------------------------------------------------------------
{
	byte [] toGo = new byte[4];
	Utils.writeInt(out,toGo,0,4);
	sock.write(toGo);
}

//-------------------------------------------------------------------
int readInt(Socket sock)
//-------------------------------------------------------------------
{
	byte [] toGo = new byte[4];
	int read = sock.readBytes(toGo,0,4);
	if (read != 4) return -1;
	return Utils.readInt(toGo,0,4);
}
/**
* This registers an auto-start entry which will start when ewesync begins.
* The name parameter must be unique and non-null.
* If register is false it will de-register the auto-start.
* If you are de-registering the application (i.e. register is false) then
* path may be null.
**/
/*
//===================================================================
public boolean registerAutoStart(String name,String path,boolean register)
//===================================================================
{
	if (name == null) return false;
	if (path == null && register) return false;
	if (path == null) path = "";
	Socket sock = getSock(6,name);
	if (sock == null) return false;
	if (!writeString(sock,path)) return false;
	if (!writeInt(sock,register ? 1 : 0)) return false;
	return getReply(sock,true);
}
*/


public static final int AUTO_START_IS_EWE_APPLICATION = 0x1;
public static final int AUTO_START_EMULATOR = 0x2;
public static final int AUTO_START_SERVICE = 0x4;


/**
 * Use this to register an AutoStart entry. When an successful EweSync connection from the
	desktop to the mobile device is made, then this will cause registered AutoStart entries
	to run.
 * @param entryName A unique name for the entry. You can use spaces or special characters,
	but not the backslash (\) character.
 * @param commandLine The command line to run. This should be a full command line, or it can
	be a command line to the EweVM if the AUTO_START_IS_EWE_APPLICATION option is chosen.
 * @param typeAndOptions This can be a combination of AUTO_START_IS_EWE_APPLICATION with
	AUTO_START_EMULATOR or AUTO_START_SERVICE. If neither AUTO_START_EMULATOR nor AUTO_START_SERVICE
	is selected, then it is assumed that the entry is in the normal (default) auto start section.
 * @exception IOException If the information could not be registerd.
 */
//===================================================================
public static void registerAutoStart(String entryName,String commandLine,int typeAndOptions)
throws IOException
//===================================================================
{
	String keyName = "AutoStart";
	if ((typeAndOptions & AUTO_START_EMULATOR) != 0)
		keyName = "EmulatorAutoStart";
	else if ((typeAndOptions & AUTO_START_SERVICE) != 0)
		keyName = "Services";

	RegistryKey rk = Registry.getLocalKey(Registry.HKEY_LOCAL_MACHINE,"Software\\EweSoft\\EweSync\\"+keyName,true,true);
	if (rk == null) {
		throw new IOException("Could not register entry.");
	}

	if ((typeAndOptions & AUTO_START_IS_EWE_APPLICATION) != 0){
		String vm = ewe.sys.Vm.getPathToEweVM();
		if (vm == null) {
			throw new IOException("Could not determine path of local Ewe VM");
		}
		if (vm.charAt(0) != '"') vm = '"'+vm+'"';
		commandLine = vm+" "+commandLine;
	}
	if (!rk.setValue(entryName,commandLine)) {
		throw new IOException("Could not register entry.");
	}
}
/**
 * This registers a normal autostart entry specifying the entry name and a command line that is
	 taken to be a Ewe application.
 * @param entryName A unique name for the entry. You can use spaces or special characters,
	but not the backslash (\) character.
 * @param commandLine The command line to run with the Ewe VM.
 * @exception IOException If the information could not be registerd.
 */
//===================================================================
public static void registerAutoStartEweApp(String entryName,String eweCommandLine)
throws IOException
//===================================================================
{
	registerAutoStart(entryName,eweCommandLine,AUTO_START_IS_EWE_APPLICATION);
}
/*
//-------------------------------------------------------------------
protected static IOHandle returnHandleError(String error,IOHandle h)
//-------------------------------------------------------------------
{
	if (h == null) h = new IOHandle();
	h.error = error;
	h.set(h.Failed);
	return h;
}
*/
public static final int MOBILE_IS_EWE_APPLICATION = 1;
public static final int MANUAL_MOBILE_START_ON_EMULATOR = 2;
public static final int MOBILE_GET_COMMAND_FROM_REGISTRY = 4;

/**
This is a convenience method for doing standard application synchronization that can be
be run on both the desktop and mobile device.<p>
If it is running on the desktop it will do the following:
<pre>
1. Create a server socket on an arbitrary port.
2. Post the service name and the socket port.
3. Execute the remote command line, unless the MANUAL_MOBILE_START_ON_EMULATOR
		option is selected in which case a MessageBox is shown if the connection
		is running on the Ewesync Emulator and you must start the mobile app yourself.

		If the MOBILE_GET_COMMAND_FROM_REGISTRY option is used, then the command line supplied should
		be the name of a value in the registry key - this value will be retrieved and then run.
		If MOBILE_IS_EWE_APPLICATION option is used then the value retrieved from the registry
		should be the location of a .ewe file.

4. Wait for a connection.
</pre><p>
If it is running on the mobile this method will:
<pre>
1.  Attempt to connect to the serviceName on the remote host.
</pre>

 * @param serviceName The unique service name to be used.
 * @param commandLine A command line to pass to the Ewe VM on the remote device.
 * @param howLong The length of time to wait for a connection.
 * @param makeOptions This can be any of MANUAL_MOBILE_START_ON_EMULATOR, MOBILE_IS_EWE_APPLICATION OR'ed together.
 * @return The connected Socket.
 * @exception ewe.sys.TimedOutException If no connection was made during the Timeout period.
 * @exception ewe.io.IOException If there was an error making the connection.
 */
//===================================================================
public Socket makeSyncConnection(String serviceName,String commandLine,ewe.sys.TimeOut howLong,int makeOptions)
throws ewe.sys.TimedOutException, ewe.io.IOException, InterruptedException
//===================================================================
{
	return makeSyncConnection(serviceName,commandLine == null ? null : new String[]{commandLine},howLong,makeOptions);
}
/**
This is a convenience method for doing standard application synchronization that can be
be run on both the desktop and mobile device.<p>
If it is running on the desktop it will do the following:
<pre>
1. Create a server socket on an arbitrary port.
2. Post the service name and the socket port.
3. Execute the remote command lines, unless the MANUAL_MOBILE_START_ON_EMULATOR
		option is selected in which case a MessageBox is shown if the connection
		is running on the Ewesync Emulator and you must start the mobile app yourself.

		If the MOBILE_GET_COMMAND_FROM_REGISTRY option is used, then the command lines supplied should
		be the name of a value in the registry key - this value will be retrieved and then run.
		If MOBILE_IS_EWE_APPLICATION option is used then the value retrieved from the registry
		should be the location of a .ewe file.

4. Wait for a connection.
</pre><p>
If it is running on the mobile this method will:
<pre>
1.  Attempt to connect to the serviceName on the remote host.
</pre>

 * @param serviceName The unique service name to be used.
 * @param commandLines An array of command lines to pass to the Ewe VM on the remote device.
 * @param howLong The length of time to wait for a connection.
 * @param makeOptions This can be any of MANUAL_MOBILE_START_ON_EMULATOR, MOBILE_IS_EWE_APPLICATION OR'ed together.
 * @return The connected Socket.
 * @exception ewe.sys.TimedOutException If no connection was made during the Timeout period.
 * @exception ewe.io.IOException If there was an error making the connection.
 */
//===================================================================
public Socket makeSyncConnection(String serviceName,String [] commandLines,ewe.sys.TimeOut howLong,int makeOptions)
throws ewe.sys.TimedOutException, ewe.io.IOException, InterruptedException
//===================================================================
{
	int st = getStatus();

	if (st == IS_MOBILE){
		Socket desktop = connectToService(serviceName);
		if (desktop == null) throw new IOException("Could not connect to desktop.");
		else return desktop;
	}else if (st == IS_DESKTOP){
			ServerSocket ss = createService(serviceName);
			try{
				IOHandle handle = ss.accept(null);
				ewe.ui.MessageBox start = null;
				if (commandLines == null || (isEmulated() && ((makeOptions & MANUAL_MOBILE_START_ON_EMULATOR) != 0)))
					(start = new ewe.ui.MessageBox("Start Mobile App.","Please start the mobile application.",ewe.ui.MessageBox.MBOK)).exec();
				else{
					execRemote(commandLines,makeOptions);
				/*
					for (int i = 0; i<commandLines.length; i++){
						String toRun = commandLines[i];
						if ((makeOptions & MOBILE_GET_COMMAND_FROM_REGISTRY) != 0){
							toRun = getRegistryValue(toRun);
							if (toRun == null) throw new IOException("Could not run mobile application: "+commandLines[i]);
						}
						boolean ran;
						if ((makeOptions & MOBILE_IS_EWE_APPLICATION) != 0)
							ran = runRemoteEwe(toRun);
						else
							ran = runRemoteApp(toRun);
						if (!ran) throw new IOException("Could not run mobile application: "+commandLines[i]);
					}
				*/
				}
				try{
					if (!handle.waitOn(handle.Success,howLong))
						throw new ewe.sys.TimedOutException("Mobile application did not connect.");
				}catch(ewe.sys.HandleStoppedException e){
					throw new ewe.io.IOException("Could not connect to mobile.");
				}finally{
					if (start != null) start.exit(0);
				}
				return (Socket)handle.returnValue;
			}finally{
				ss.close();
			}
	}else{
		throw new IOException("There is a problem with the remote connection.");
	}

}
/*
* This is a convenience method for doing standard application synchronization. If it
* is running on the desktop this method will:
* <pre>
* 1. Create a server socket on an arbitrary port.
* 2. Post the service name and the socket port.
* 3. Execute the remote command line.
* 4. Wait for a connection.
* </pre><p>If it is running on the mobile this method will:
* <pre>
* 1. Attempt to connect to the serviceName on the remote host.
* </pre>
* <p>
* The howLong parameter applies only to the desktop side and specifies how long it
* should wait for a client to connect after running it. On the mobile side the RemoteConnection
* will timeout itself if it determines that there is not service to connect to.
* @deprecated use makeSyncConnection() instead.
*/
//===================================================================
public Socket getSyncConnection(String serviceName,String remoteCommandLine,boolean remoteIsEweApp,ewe.sys.TimeOut howLong)
//===================================================================
{
	int st = getStatus();
	if (st == IS_MOBILE){
		Socket desktop = connectToService(serviceName);
		if (desktop != null) return desktop;
		return (Socket)returnError("Could not connect to desktop!",null);
	}else if (st == IS_DESKTOP){
		try{
			String name = InetAddress.getLocalHostName();
			//ewe.sys.Vm.debug("Name: "+name);
			ServerSocket ss = new ServerSocket(0,0,name,null);
			if (!ss.isOpen())
				return (Socket)returnError("Could not listen on a port!",null);
			postService(serviceName,name,ss.getLocalPort());
			if (remoteIsEweApp) runRemoteEwe(remoteCommandLine);
			else runRemoteApp(remoteCommandLine);
			IOHandle handle = ss.accept(null);
			boolean connected = handle.waitOnFlags(handle.Success,howLong);
			ss.close();
			if (!connected)
				return (Socket)returnError("The mobile device did not connect!",null);
			return (Socket)handle.returnValue;
		}catch(Exception e){
			return (Socket)returnError("Could not listen on a port!",null);
		}
	}else{
		return (Socket)returnError("There was a problem connecting to the Remote Services.",null);
	}
}

ewe.sys.Lock blockLock;
static int lastID = 0;

	//##################################################################
	class TransferBlock{
	//##################################################################

	public int source, destination, command, parameter, length;
	byte [] data = new byte[0];

	ewe.sys.Lock replyLock = new ewe.sys.Lock();

	TransferBlock reply;

	//===================================================================
	void addToWait()
	//===================================================================
	{
		if (pendingConnections == null) pendingConnections = new Hashtable();
		pendingConnections.put(new ewe.sys.Long().set(source),this);
	}
	//===================================================================
	TransferBlock waitReply(ewe.sys.TimeOut timeout)
	//===================================================================
	{
		if (debug) ewe.sys.Vm.debug("Waiting on connect reply: "+source);
		//pendingConne
		replyLock.synchronize(); try{
			while(reply == null){
				try{
					if (!replyLock.waitOn(timeout))
						break;
				}catch(Exception e){}
			}
			if (debug) ewe.sys.Vm.debug("Got reply: "+reply);
			return reply;
		}finally{replyLock.release();}
	}
	//===================================================================
	void acceptReply(TransferBlock replied)
	//===================================================================
	{
		replyLock.synchronize(); try{
			reply = replied;
			replyLock.notifyAllWaiting();
		}finally{replyLock.release();}
	}
	//===================================================================
	int getNewID() {return ++lastID;}
	//===================================================================

	//===================================================================
	public void read(Socket from) throws ewe.io.IOException
	//===================================================================
	{
		blockLock.synchronize(); try{
			source = RemoteConnection.this.readAnInt(from);
			destination = RemoteConnection.this.readAnInt(from);
			command = RemoteConnection.this.readAnInt(from);
			parameter = RemoteConnection.this.readAnInt(from);
			length = RemoteConnection.this.readAnInt(from);
			data = new byte[length];
			if (length != 0) ewe.io.IO.readAll(from,data,0,length);
		}finally{
			blockLock.release();
		}
	}

	//===================================================================
	public void write(Socket to)throws ewe.io.IOException
	//===================================================================
	{
		blockLock.synchronize(); try{
			if (debug) ewe.sys.Vm.debug("Writing to: "+to);
			length = data.length;
			writeAnInt(to,source);
			writeAnInt(to,destination);
			writeAnInt(to,command);
			writeAnInt(to,parameter);
			writeAnInt(to,length);
			to.write(data,0,length);
			//if (debug) ewe.sys.Vm.debug("Written: "+command+", "+source+", "+length);
		}finally{
			blockLock.release();
		}
	}
	int curOffset = 0;
	//===================================================================
	public int readInt()
	//===================================================================
	{
		curOffset += 4;
		return Utils.readInt(data,curOffset-4,4);
	}
	//===================================================================
	public String readString()
	//===================================================================
	{
		int len = readInt();
		curOffset += len;
		return Utils.decodeJavaUtf8String(data,curOffset-len,len);
	}
	//===================================================================
	public void writeInt(int value)
	//===================================================================
	{
		ByteArray ba = new ByteArray(data);
		byte [] buff = new byte[4];
		Utils.writeInt(value,buff,0,4);
		ba.append(buff,0,buff.length);
		data = ba.toBytes();
	}
	//===================================================================
	public int writeStringAlone(String string)
	//===================================================================
	{
		byte [] buff = string == null ? new byte[0] : Utils.encodeJavaUtf8String(string);
		ByteArray ba = new ByteArray(data);
		ba.append(buff,0,buff.length);
		data = ba.toBytes();
		return buff.length;
	}
	//===================================================================
	public String readStringAlone()
	//===================================================================
	{
		return Utils.decodeJavaUtf8String(data,0,length);
	}
	//===================================================================
	public void writeString(String string)
	//===================================================================
	{
		byte [] buff = string == null ? new byte[0] : Utils.encodeJavaUtf8String(string);
		writeInt(buff.length);
		ByteArray ba = new ByteArray(data);
		ba.append(buff,0,buff.length);
		data = ba.toBytes();
	}
	//===================================================================
	public String toString()
	//===================================================================
	{
		return ""+command+", "+parameter;
	}
	//##################################################################
	}
	//##################################################################

//===================================================================
public static void main(String args[])
//===================================================================
{
	ewe.sys.Vm.startEwe(args);
	/*
	try{
		ewex.registry.RegistryServer.startServer();
	}catch(Exception e){
		//e.printStackTrace();
	}
	*/
	if (args.length != 0){
		if (args[0].equals("test")){
			try{
				RemoteConnection rc = getConnection();
				/*
				String toRun = new ewe.ui.InputBox("Enter Ewe Command").input("-o \"/home/Anything.ewe\"",40);
				if (toRun != null)
					rc.runRemoteEwe(toRun);
				new ewe.ui.MessageBox("Sent","The request has been sent.",0).execute();
				*/
				while(true){
					String toAsk =
						input("Enter RegistryKey","HKEY_CLASSES_ROOT\\EweFile10",40);
					if (toAsk != null){
						String got = rc.getRegistryValue(toAsk);
						new ewe.ui.MessageBox("Reply",got == null ? "NULL" : got,0).execute();
					}else break;
				}

			}catch(IOException e){
				new ewe.ui.MessageBox("Error","Could not get RemoteConnection",0).execute();
			}
			ewe.sys.Vm.exit(0);
		}else if (args[0].equals("zaurus") || args[0].equals("emulator")){
			try{
				boolean isZaurus = args[0].equals("zaurus");
				final String title = isZaurus ? "Zaurus" : "Emulated";
				ewe.ui.TaskbarWindow tb = new ewe.ui.TaskbarWindow(title+" EweSync Server",new ewe.fx.Dimension(16,16),30){
					ewe.sys.Lock exitLock = new ewe.sys.Lock();
					public void iconPressed(){
						final TaskbarWindow tt = this;
						new ewe.sys.mThread(){
							public void run(){
								if (!exitLock.grab()) return;
								try{
								if (new MessageBox(title+" EweSync Server","Do you want to stop the server?",MessageBox.MBYESNO).execute() == MessageBox.IDYES){
									RemoteConnection.exit(tt);
								}
								}finally{
									exitLock.release();
								}
							}
						}.start();
					}
				};
				tb.addIcon("Disconnected","ewe/ewediscsmall.bmp","ewe/ewesmallmask.bmp");
				tb.addIcon("Connected","ewe/ewesmall.bmp","ewe/ewesmallmask.bmp");
				tb.setIconAndTip("Disconnected","EweSync Server Starting...");
				new RemoteConnection(tb,!isZaurus);
				return ;
			}catch(IOException e){
				e.printStackTrace();
				//ewe.sys.Vm.exit(0);
			}
		}
	}else
	try{
		Form ed = new Form();
		ed.taskbarIcon = new Window.TaskBarIconInfo("ewe/ewesmall.bmp","ewe/ewesmallmask.bmp","EweSync Server");
		ed.title = "EweSync Server";
		TextMessage tm = new TextMessage("The EweSync Server is now running.\n\nClose this window to stop the server.");
		tm.alignment = tm.anchor = tm.CENTER;
		ed.addLast(tm);
		ed.addNext(ed.cancel = new mButton("Stop EweSync Server")).setControl(ed.DONTFILL);
		//ed.addLast(new mButton("Hide Window")).setControl(ed.DONTFILL);
		ed.endRow();
		mLabel status = new mLabel(" ");
		status.setBorder(status.BF_TOP|status.BDR_RAISEDINNER|status.BDR_SUNKENOUTER,2);
		status.alignment = status.anchor = status.CENTER;
		status.modify(status.DisplayOnly|status.NotEditable,0);
		ed.addNext(status).setCell(status.HSTRETCH);
		ed.windowFlagsToSet |= Window.FLAG_MAXIMIZE_ON_PDA;//|Window.FLAG_VISIBLE_ON_TO_FRONT;
		//ed.windowFlagsToClear |= Window.FLAG_IS_VISIBLE;
		new RemoteConnection(status);
		ed.execute();
	}catch(Exception e){
		new ewe.ui.ReportException(e,null,null,false).execute();
	}
	ewe.sys.Vm.exit(0);
}
//##################################################################
}
//##################################################################

