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
* This can interactively create a Socket or ServerSocket for connection to a server or client.
**/
//##################################################################
public class SocketMaker extends LiveObject{
//##################################################################

/**
* This should be used with the "type" member.
**/
public static final int LOCAL_SOCKET = 0;
/**
* This should be used with the "type" member.
**/
public static final int INFRA_RED = 1;
/**
* This should be used with the "type" member.
**/
public static final int REMOTE_SERVICE = 2;
/**
* This should be used with the "type" member. This one should not be used if you
* are creating a server.
**/
public static final int REMOTE_SOCKET = 3;
/**
* By default this is LOCAL_SOCKET
**/
public int type = LOCAL_SOCKET;

public static String AnyHostName = "Any";
/**
* The name of the host to connect to. Not used for INFRA_RED.
**/
public String hostName = AnyHostName;
/**
* The port number to connect to. Not used for INFRA_RED. If isServer is true, then this can be zero
* and its value can be determined after calling getSocket().
**/
public int port = 0;
/**
* Use this if you want to connect to a remote service (via the RemoteConnection) or a providing a
* remote service.
**/
public String serviceName = "A Service";
/**
* This says whether a ServerSocket is to be created.
**/
public boolean isServer = false;
/**
* This is the timeout time in seconds to wait for a successful connection.
**/
public int timeout = 30;
/**
* This is the title of the form that is displayed if you are doing an interactive
* connection.
**/
public String title = null;
/**
* This allows users to select the type of connection.
**/
public boolean canSelectType = true;

private String error = null;


/**
 * This MAY return additional information if there was an error during socket creation.
 * @return An error description or null if there was no error.
 */
//===================================================================
public String getError()
//===================================================================
{
	return error;
}
/**
* Creates a SocketMaker with the specified type, wether it is a server and the title
* of the dialog box if it is displayed.
**/
//===================================================================
public SocketMaker(int type,boolean isServer,String title)
//===================================================================
{
	this.type = type;
	this.isServer = isServer;
	this.title = title;
	hostName = isServer ? AnyHostName : InetAddress.getLocalHostName();
}
CardPanel cards;
//-------------------------------------------------------------------
InputStack getStack(int which,Editor f)
//-------------------------------------------------------------------
{
	String [] waits = ewe.util.mString.split("5|10|30|60|120|300",'|');
	if (which != type && !canSelectType) return null;
	InputStack is = new InputStack();
	cards.addItem(is,Convert.toString(which),null);
	is.inputLength = 20;
	is.doubleLined = true;
	is.add(f.addField(new mComboBox(waits,0),"timeout"),"Time out (seconds):");
	return is;
}
final int LowDetail = 1;
final int WantDetails = 100;
//===================================================================
public void addToPanel(CellPanel cp,Editor f,int which)
//===================================================================
{
	if (title == null) f.hasTopBar = false;
	else f.title = title;
	f.modify(f.MouseSensitive,0);
	CardPanel cards = new CardPanel();
	this.cards = cards;
	InputStack is;
	if (type == REMOTE_SOCKET && isServer) type = LOCAL_SOCKET;
	//......................................................
	// Local Socket.
	//......................................................
	is = getStack(LOCAL_SOCKET,f);
	if (is != null){
		if (isServer)
			is.add(f.addField(new mComboBox(new String[]{AnyHostName,InetAddress.getLocalHostName()},0),"hostName"),"Host Name:");
		else
			is.add(f.addField(new mComboBox(new String[]{InetAddress.getLocalHostName()},0),"hostName"),"Host Name:");
		is.add(f.addField(new mInput(),"port"),"Host Port Number:");
	}
	//......................................................
	// InfraRed Socket.
	//......................................................
	is = getStack(INFRA_RED,f);
	if (is != null)
		is.add(f.addField(new mInput(),"port"),"Host Port Number:");
	//......................................................
	// Remote Service.
	//......................................................
	is = getStack(REMOTE_SERVICE,f);
	if (is != null)
		is.add(f.addField(new mInput(),"serviceName"),"Service:");
	//......................................................
	// Top part.
	//......................................................
	is = new InputStack();
	String [] types = new String[]{"TCP/IP Port","Infra-Red Port","Remote Service","Remote TCP/IP"};
	if (isServer) {
		String [] ty = new String[types.length-1];
		for (int i = 0; i<ty.length; i++) ty[i] = types[i];
		types = ty;
		if (type > types.length-1) type = 0;
	}



	if (canSelectType)
		is.add(f.addField(new mChoice(types,type),"type"),"Type:");
	else
		is.add(new mLabel(types[type]),"Type:");
	cp.addLast(is).setCell(cp.HSTRETCH);
	if (which == LowDetail && false){
		Control c = f.addField(new mButton("Edit"),"details");
		Gui.iconize(c,"ewe/editsmall.bmp",ewe.fx.Color.White,true,null);
		f.addButton(c);//.setCell(cp.HSTRETCH);
	}else
		cp.addLast(cards).setCell(cp.HSTRETCH);
	f.addField(cards,"cards");
	typeChanged(f);
}

//-------------------------------------------------------------------
void typeChanged(Editor f)
//-------------------------------------------------------------------
{
	MultiPanel cards = (MultiPanel)f.findFieldTransfer("cards").dataInterface;
	if (type == REMOTE_SOCKET) cards.select(""+LOCAL_SOCKET);
	else cards.select(""+type);
}
//===================================================================
public void fieldChanged(ewe.reflect.FieldTransfer ft,Editor f)
//===================================================================
{
	if (ft.fieldName.equals("type"))
		typeChanged(f);
}
//===================================================================
public void action(String fieldName,Editor f)
//===================================================================
{
	if (fieldName.equals("details")) f.exit(WantDetails);
}
boolean shouldStop = false;
Form cancelForm;
Handle cancelHandle;

/*
//===================================================================
public Object getSocket(boolean showSelectBox,boolean showCancel)
//===================================================================
{
	return getSocket(showSelectBox,showCancel,true);
}
*/
/**
* This is used to get either a Socket or ServerSocket using the parameters
* setup for this SocketMaker. If showSelectBox is true, then a dialog box will
* appear for the user to change the parameters of the socket. If showCancel is true,
* a box will appear during socket creation and connection so the user can cancel
* the operation.
* <p>
* The method will return a Socket (isServer is false) or a ServerSocket (isServer is true)
* or null if the socket could not be created or if the user canceled the operation.
**/
//===================================================================
public Object getSocket(boolean showSelectBox,boolean showCancel)
//===================================================================
{
	return getSocket(null,showSelectBox ? 0 : -1,showCancel);
}

//-------------------------------------------------------------------
private boolean doInitialFrame(Frame parent,int options)
//-------------------------------------------------------------------
{
	if (options != -1){
		if (options == 0) options = Gui.CENTER_FRAME;
		int ed = LowDetail;
		while(true){
			Editor f = getEditor(ed);
			f.doButtons(f.DEFOKB|f.DEFCANCELB);
			//Gui.setOKCancel(f);
			int ret = f.execute(parent,options);
			if (ret == f.IDCANCEL) return false;
			if (ret != WantDetails) break;
			ed = 0;
		}
	}
	return true;
}
//===================================================================
public boolean showInitialFrame(Frame parent, int options)
//===================================================================
{
	return doInitialFrame(parent,options);
}
//-------------------------------------------------------------------
private ServerSocket createServer(IOHandle h)
//-------------------------------------------------------------------
{
		ServerSocket ss = null;
		RemoteConnection rc = null;
		if (type == LOCAL_SOCKET){
			//hostName = InetAddress.getLocalHostName();
			ss = new ServerSocket(port,0,hostName.equals(AnyHostName) ? null : hostName,h);
		}else if (type == INFRA_RED){
			ss = new ServerSocket(port,0,"infra-red",h);
		}else if (type == REMOTE_SERVICE){
			try{
				rc = RemoteConnection.getConnection();
				ss = new ServerSocket(0,0,hostName.equals(AnyHostName) ? null : hostName,h);
			}catch(IOException e){
				error = e.getMessage();
			}
		}
		if (ss == null) return null;
		if (!h.waitOnFlags(h.Success,new TimeOut(5000))){
			try{
				ss.close();
			}catch(Exception e){
			}
			if (h.error != null) error = h.error;
			else if (h.errorObject instanceof Throwable)
				error = ((Throwable)h.errorObject).getMessage();
			return null;
		}
		if (type == LOCAL_SOCKET || type == REMOTE_SERVICE)
			port = ss.getLocalPort() ;
		if (type == REMOTE_SERVICE)
			rc.postService(serviceName,ss.getInetAddress().getHostName(),port);
		return ss;
}
//===================================================================
public Object [] getTwoWayConnection(boolean showSelectBox)
//===================================================================
{
	Handle h = getTwoWayConnection(showSelectBox,true);
	if (h == null) return null;
	if (!h.waitOnFlags(h.Success,TimeOut.Forever)) return null;
	return (Object [])h.returnValue;
}
//===================================================================
public Handle getTwoWayConnection(boolean showSelectBox,boolean showCancel)
//===================================================================
{
	return getTwoWayConnection(null,showSelectBox ? 0 : -1,showCancel);
}
//===================================================================
public Handle getTwoWayConnection(Frame parent,int options,boolean showCancel)
//===================================================================
{
	boolean offset = false;
	final Object [] ret = new Object[2];
	if (!doInitialFrame(parent,options)) return null;
	final Handle h = new Handle();
	String realHost = hostName;
	hostName = AnyHostName;
	ServerSocket gs = createServer(new IOHandle());
	if (gs == null){
 		if (offset){
			port++;
			gs = createServer(new IOHandle());
			port--;
	  }
		if (gs == null) return new Handle(Handle.Failed,"Can't create ServerSocket");
	}else{
		if (offset) port++;
	}
	hostName = realHost;
	final ServerSocket ss = gs;
	h.returnValue = ret;
	h.set(h.Running);
	Handle server =
	new TaskObject(){
		protected void doRun(){
				try{
					IOHandle sh = ss.accept(null);
					//ewe.sys.Vm.debug("Accepting at: "+ss.getLocalPort());
					if (sh.waitOnFlags(handle.Success,new TimeOut(500),h,new TimeOut(timeout*1000))){
						//ewe.sys.Vm.debug("ServerSocket succeeded!");
						Socket in = (Socket)sh.returnValue;
						ret[0] = in;
						int got = h.check();
						if ((got & h.Stopped) == 0){
							got |= 0x1;
							if ((got & 0x3) == 0x3) h.set(h.Succeeded);
							else h.set(got);
						}
					}else{
						//ewe.sys.Vm.debug("ServerSocket failed!");
						h.shouldStop = true;
						h.set(h.Failed);
					}
				}finally{
					try{ss.close();}catch(Exception e){}
				}
		}
	}.startTask();
	Handle client =
	new TaskObject(){
		protected void doRun(){
			TimeOut total = new TimeOut(timeout*1000);
			RemoteConnection rc = null;
			while(!handle.shouldStop){
					IOHandle sh = new IOHandle();
					Socket s = null;
					if (type == LOCAL_SOCKET){
						s = new Socket(hostName,port,sh);
						//ewe.sys.Vm.debug("Connect: "+hostName+", "+port);
					}else if (type == INFRA_RED){
						s = new Socket("infra-red",port,sh);
					}else if (type == REMOTE_SERVICE || type == REMOTE_SOCKET){
						rc = RemoteConnection.getInstance();
						if (rc != null){
							rc.connectTimeOut = timeout*1000;
							s = (type == REMOTE_SERVICE) ?
								rc.connectToService(serviceName):rc.connectToHost(hostName,port);
							if (s != null) sh.set(sh.Succeeded);
						}
					}
					//ewe.sys.Vm.debug("Client connecting to: "+port);
					if (s == null) sh.set(sh.Failed);
					if (sh.waitOnFlags(handle.Success,new TimeOut(500),h,total)){
						//ewe.sys.Vm.debug("Client connected!");
						Socket out = s;
						ret[1] = out;
						int got = h.check();
						if ((got & h.Stopped) == 0){
							got |= 0x2;
							if ((got & 0x3) == 0x3) h.set(h.Succeeded);
							else h.set(got);
						}else{
							try{s.close();}catch(Exception e){}
						}
						break;
					}else{
						try{s.close();}catch(Exception e){}
						if (total.hasExpired()) {
							h.shouldStop = true;
							h.set(h.Failed);
							break;
						}else{
							//ewe.sys.Vm.debug("Client failed, will try again...");
							sleep(250);//Try again later.
						}
					}
			}
		}
	}.startTask();
	if (showCancel){
	ProgressBarForm pbf = new ProgressBarForm();
	pbf.showMainTask = true;
	pbf.showSubTask = pbf.horizontalLayout = false;
	pbf.showBar = false;
	pbf.showStop = true;
	pbf.setTask(h,"Connecting...");
	pbf.execute(parent,Gui.CENTER_FRAME);
  }
/*
	if (!h.waitOnFlags(h.Succeeded,TimeOut.Forever)){
		server.stop(0);
		client.stop(0);
		return null;
	}
*/
	return h;
}
/*
//===================================================================
public static Object makeInfraRedConnection(Frame parent,port)
throws IOException
//===================================================================
{
	SocketMaker sm = new SocketMaker(
	return sm.makeClientServerConnection(parent,Gui.CENTER_FRAME,true);
}
*/

//===================================================================
public static Object makeInfraRedConnection(int port,int timeoutInSeconds,boolean showCancel)
throws IOException
//===================================================================
{
	SocketMaker sm = new SocketMaker(INFRA_RED,true,"Make connection");
	sm.port = port;
	sm.timeout = timeoutInSeconds;
	return sm.makeClientServerConnection(null,0,showCancel);
}
boolean forceToBeClient;
boolean forceToBeServer;

//===================================================================
public Object makeClientServerConnection(Frame parent,int options,boolean showCancel)
throws IOException
//===================================================================
{
	Handle h = getClientServerConnection(parent,options,showCancel);
	try{
		h.waitOn(h.Succeeded);
		Object [] got = (Object [])h.returnValue;
		if (got[1] == null) return got[0];
		else return got;
	}catch(Exception e){
		throw new IOException("Could not connect!");
	}
}

//===================================================================
public Handle getClientServerConnection(Frame parent,int options,boolean showCancel)
//===================================================================
{
	final boolean offset = false;
	//final boolean debug = true;
	boolean amMobile = ewe.sys.Vm.isMobile();
	boolean noC = ((type == REMOTE_SERVICE || type == REMOTE_SOCKET) && !amMobile);
	boolean noS = ((type == REMOTE_SERVICE || type == REMOTE_SOCKET) && amMobile);
	if (!noC && !noS){
		if (forceToBeClient) noS = true;
		if (forceToBeServer) noC = true;
	}
	final boolean noClient = noC,  noServer = noS;

	final Object [] ret = new Object[2];
	//if (!doInitialFrame(parent,options)) return null;
	final Handle h = new Handle();
	String realHost = hostName;
	hostName = AnyHostName;
	ServerSocket gs = null;
	//
	// If you are connecting across the Ewesync connection, then the desktop automatically
	// becomes the server and the mobile becomes the host. Therefore the mobile will not
	// create a server socket.
	//
	if (!noServer){
		gs = createServer(new IOHandle());
		if (gs == null){
	 		if (offset){
				port++;
				gs = createServer(new IOHandle());
				port--;
		  }
			if (gs == null) return new Handle(Handle.Failed,"Can't create ServerSocket");
		}else{
			if (offset) port++;
		}
	}
	//ewe.sys.Vm.debug("Server connected: "+gs.getLocalPort());
	hostName = realHost;
	final ServerSocket ss2 = gs;
	h.returnValue = ret;
	h.set(h.Running);
	//Handle server =
	final int myTime = (int)ewe.sys.Vm.getTimeStampLong();
	final ewe.sys.Lock ssLock = new ewe.sys.Lock();
	//
	// Server section.
	// Use the server socket to accept an incoming connection and then
	// wait to see what timestamp value we receive. If the received value is greater than ours
	// then we close both that incoming connection AND our server socket and we will act as the
	// client only. The socket created in the client section will be used to communicate with
	// the remote server.
	//
	if (gs == null){ // No server created, then just do the client section.
		h.set(h.check() | 0x1);
	}else
	new TaskObject(){
		protected void doRun(){
					ServerSocket ss = ss2;
					try{
						IOHandle sh = ss.accept(null);
						if (sh.waitOn(handle.Success,new TimeOut(500),h,new TimeOut(timeout*1000))){
							//ewe.sys.Vm.debug("Accepted connection!");
							Socket in = (Socket)sh.returnValue;
							byte [] time = new byte[4];
							IO.readFully(in,time);
							//ewe.sys.Vm.debug("OK, got data!");
							int tm = ewe.util.Utils.readInt(time,0,4);
							if (!noClient){
								if (tm > myTime){
									//ewe.sys.Vm.debug("Remote will be the server.");
									try{
										in.close();
										ss.close();
									}catch(Exception e){}
									ss = null;
								}else if (tm < myTime){
									//ewe.sys.Vm.debug("I will be the server.");
								}else{
									//ewe.sys.Vm.debug("Neither will be server.");
									throw new IOException("Please try again.");
								}
							}
							if (ss != null){
								//ewe.sys.Vm.debug("Fine, I will be the server!");
								ret[0] = in;
								ret[1] = ss;
							}
							//
							// Indicate that the server has already processed the client.
							//
							int got = h.check();
							if ((got & h.Stopped) == 0){
								got |= 0x1;
								h.set(got);
							}
						}else{
							throw new IOException("Timed out!");
							//ewe.sys.Vm.debug("ServerSocket failed!");
						}
					}catch(Exception e){
						//e.printStackTrace();
						try{ss.close();}catch(Exception e2){}
						h.shouldStop = true;
						h.set(h.Failed);
					}finally{
						ssLock.synchronize(); try{
							ssLock.notifyAllWaiting();
						}finally{ssLock.release();}
					}
		}
	}.startTask();

	//
	// Client section.
	// Make a connection to the other one and then send our timestamp down the line.
	// Then wait until the server section is complete and decide whether to close my
	// connection or not.
	//

	//Handle client =

		// I'm the desktop so I will not create a client socket, I'll just wait until
		// the server has completed.
  new TaskObject(){
		protected void doRun(){
			if (offset) mThread.nap(5000);
			Socket made = null;
			TimeOut total = new TimeOut(timeout*1000);
			RemoteConnection rc = null;
			try{
				if (noClient){
					return; //The finally section will wait for the server to complete.
				}
				while(!handle.shouldStop && !h.shouldStop){
						//
						// Attempt to make a connection. The handle sh will hold the progress.
						//
						IOHandle sh = new IOHandle();
						Socket s = null;
						if (type == LOCAL_SOCKET){
							s = new Socket(hostName,port,sh);
							//ewe.sys.Vm.debug("Connect: "+hostName+", "+port);
						}else if (type == INFRA_RED){
							s = new Socket("infra-red",port,sh);
						}else if (type == REMOTE_SERVICE || type == REMOTE_SOCKET){
							rc = RemoteConnection.getInstance();
							if (rc != null){
								rc.connectTimeOut = timeout*1000;
								s = (type == REMOTE_SERVICE) ?
									rc.connectToService(serviceName):rc.connectToHost(hostName,port);
								if (s != null) sh.set(sh.Succeeded);
							}
						}
						//
						//ewe.sys.Vm.debug("Client connecting to: "+port);
						//
						//ewe.sys.Vm.messageBox("Client","Client: "+s,0);
						if (s == null) sh.set(sh.Failed);
						//
						try{
							if (sh.waitOn(handle.Success,new TimeOut(500),h,total)){
								byte [] time = new byte[4];
								ewe.util.Utils.writeInt(myTime,time,0,4);
								//ewe.sys.Vm.messageBox("Client","Written data!",0);
								s.write(time);
								made = s;
								//ewe.sys.Vm.debug("Client connected!");
								break;
							}else{//Handle expired - no connection.
								try{s.close();}catch(Exception e){}
								if (total.hasExpired()) {//Total time-out, do not reattempt.
									h.shouldStop = true;
									h.set(h.Failed);
									break;
								}else{
									//ewe.sys.Vm.debug("Client failed, will try again...");
									sleep(250);//Try again later.
								}
							}
						}catch(HandleStoppedException he){
								//ewe.sys.Vm.debug("Client failed, will try again...");
								sleep(250);//Try again later.
						}catch(Exception e){
							//e.printStackTrace();
							s.close();
							h.set(h.Failed);
							break;
						}
				}
			}finally{
				if ((h.check() & h.Stopped) == 0){
					ssLock.synchronize(); try{
						if ((h.check() & 1) == 0) //Wait for server to complete and notify ssLock.
							try{
								ssLock.waitOn();
							}catch(Exception e){}
						if ((h.check() & h.Stopped) == 0){
							if (made != null){
								if (ret[0] == null) ret[0] = made;
								else made.close();
							}
							h.returnValue = ret;
							h.set(h.Succeeded);
						}
					}finally{ssLock.release();}
				}
				//ewe.sys.Vm.debug("Client leaving!");
			}
		}
	}.startTask();
	if (showCancel){
	ProgressBarForm pbf = new ProgressBarForm();
	pbf.showMainTask = true;
	pbf.showSubTask = pbf.horizontalLayout = false;
	pbf.showBar = false;
	pbf.showStop = true;
	pbf.setTask(h,"Connecting...");
	pbf.execute(parent,Gui.CENTER_FRAME);
  }
/*
	if (!h.waitOnFlags(h.Succeeded,TimeOut.Forever)){
		server.stop(0);
		client.stop(0);
		return null;
	}
*/
	return h;
}


/**
* This is the same as the other getSocket() except it will execute the dialog box
* with the specified parent and options (e.g. Gui.FILL_FRAME).
**/
//===================================================================
public Object getSocket(Frame parent,int options,boolean showCancel)
//===================================================================
{
	error = null;
	if (!doInitialFrame(parent,options)) return null;
	boolean closeCancel = true;
	cancelForm = null;
	cancelHandle = null;
	IOHandle h = new IOHandle();
	RemoteConnection rc = null;
	if (isServer){
		ServerSocket ss = createServer(h);
		/*
		if (type == LOCAL_SOCKET){
			//hostName = InetAddress.getLocalHostName();
			ss = new ServerSocket(port,0,hostName.equals(AnyHostName) ? null : hostName,h);
		}else if (type == INFRA_RED){
			ss = new ServerSocket(port,0,"infra-red",h);
		}else if (type == REMOTE_SERVICE){
			rc = RemoteConnection.getInstance();
			if (rc != null && !shouldStop){
				//hostName = InetAddress.getLocalHostName();
				ss = new ServerSocket(0,0,hostName.equals(AnyHostName) ? null : hostName,h);
			}
		}
		*/
		cancelHandle = h;
		if (ss != null){
			if (!h.waitOnFlags(h.Success,new TimeOut(timeout*500))){
				if (showCancel && (h.check() & h.Stopped) == 0){
					cancelForm = new myCancelForm(null);
					cancelForm.show();
				}
			}
			if (h.waitOnFlags(h.Success,new TimeOut(timeout*1000))){
				if (cancelForm != null && closeCancel) cancelForm.exit(0);
				if (type == LOCAL_SOCKET || type == REMOTE_SERVICE)
					port = ss.getLocalPort() ;
				return ss;
			}
			try{ss.close();}catch(Exception e){}
			return null;
		}
	}else{
		Socket s = null;
		if (type == LOCAL_SOCKET){
			s = new Socket(hostName,port,h);
			//ewe.sys.Vm.debug("Connect: "+hostName+", "+port);
		}else if (type == INFRA_RED){
			s = new Socket("infra-red",port,h);
		}else if (type == REMOTE_SERVICE || type == REMOTE_SOCKET){
			try{
				rc = RemoteConnection.getConnection();
				rc.connectTimeOut = timeout*1000;
				s = (type == REMOTE_SERVICE) ?
					rc.connectToService(serviceName):rc.connectToHost(hostName,port);
				if (s != null) h.set(h.Succeeded);
				error = "Could not connect.";
			}catch(IOException e){
				error = e.getMessage();
			}
		}
		cancelHandle = h;
		if (s != null){
			if (h.waitOnFlags(h.Success,new TimeOut(2000))){
				if (cancelForm != null && closeCancel) cancelForm.exit(0);
				return s;
			}
			if (showCancel){
				cancelForm = new myCancelForm(null);
				cancelForm.show();
			}
			if (h.waitOnFlags(h.Success,new TimeOut(timeout*1000))){
				if (cancelForm != null && closeCancel) cancelForm.exit(0);
				return s;
			}
			h.stop(0);
		}
	}
	if (cancelForm != null) cancelForm.exit(0);
	return null;
}
/**
* This waits for an incoming connection on the ServerSocket for the timeout period (given in seconds).
* It will optionally show a cancel box. It will return a connected socket or null on failure. It will
* not close the server socket. If timeoutInSeconds is -1, then there will be no timeout.
**/
//===================================================================
public Socket getConnection(ServerSocket ss,Frame parent,boolean showCancel,int timeoutInSeconds)
//===================================================================
{
	IOHandle h = ss.accept(null);
	TimeOut t = timeoutInSeconds >= 0 ? new TimeOut(timeoutInSeconds*1000) : TimeOut.Forever;
	cancelForm = null;
	cancelHandle = h;
	if (showCancel){
		cancelForm = new myCancelForm(ss);
		cancelForm.show();
	}
	if (!h.waitOnFlags(h.Success,t)){
		if (cancelForm != null) cancelForm.exit(0);
		try{
			ss.close();
		}catch(Exception e){
		}
		//ewe.sys.Vm.debug("No-one connected.");
		return null;
	}
	if (cancelForm != null) cancelForm.exit(0);
	//ewe.sys.Vm.debug("Got connection!");
	return (Socket)h.returnValue;
}
/**
* This gets a fully connected Socket. This first calls getSocket() with the same
* parameter and if isServer
* is true (getSocket() returns a ServerSocket), it will wait until a client connects
* and then return connected socket and close the ServerSocket. If isServer is false then
* it returns the Socket returned by getSocket() which will be a socket connected to a
* server.
**/
//===================================================================
public Socket getConnection(boolean showSelectBox,boolean showCancel)
//===================================================================
{
	return getConnection(null,showSelectBox ? 0 : -1,showCancel);
}
/**
* This is the same as the other getConnection() except it will execute the select connection dialog box
* with the specified parent and options (e.g. Gui.FILL_FRAME).
**/
//===================================================================
public Socket getConnection(Frame parent,int options,boolean showCancel)
//===================================================================
{
	Object got = getSocket(parent,options,showCancel);
	cancelForm = null;
	cancelHandle = null;
	if (got instanceof Socket) {
		//ewe.sys.Vm.debug("Got a socket!");
		return (Socket)got;
	}
	else if (got instanceof ServerSocket){
		ServerSocket ss = (ServerSocket)got;
		Socket connected = getConnection(ss,parent,showCancel,timeout);
		try{ss.close();}catch(Exception e){}
		return connected;
	}else{
		//ewe.sys.Vm.debug("Got nothing!");
		return null;
	}
}

	//##################################################################
	class myCancelForm extends Form{
	//##################################################################
				myCancelForm(ServerSocket ss)
				{
					modify(MouseSensitive,0);
					setBorder(mInput.inputEdge|BF_RECT,2);
					hasTopBar = false;
					this.title = "Connecting...";
					String specs = "";
					if (type == LOCAL_SOCKET || type == REMOTE_SERVICE || type == INFRA_RED)
						specs =
							ss == null ?
							"Host: "+hostName+"\nPort: "+port+"\n":
							"Host: "+ss.getInetAddress()+"\nPort: "+ss.getLocalPort()+"\n";
					if (type == REMOTE_SERVICE) specs += "Service: "+serviceName+"\n";
					//else if (type == INFRA_RED) specs += "Infra-Red Port";
					addLast(new MessageArea(isServer ?
						(ss == null ? "Creating server socket..." :
						specs+"\nWaiting for connection..."
						) :
						"Connecting to destination..."));
					doButtons(DEFCANCELB);
				}
				public void formClosing()

				{
					if (exitValue == IDCANCEL){
						shouldStop = true;
						if (cancelHandle != null){
							cancelHandle.stop(0);
						}
					}
				}

	//##################################################################
	}
	//##################################################################

/**
* This method creates two Sockets on the local host connected together.
**/
//===================================================================
public static Socket [] pipe() throws ewe.io.IOException
//===================================================================
{
	final ewe.net.ServerSocket ss = new ewe.net.ServerSocket(null,0);
	int port = ss.getLocalPort();
	final Socket [] ret = new Socket[2];
	mThread acc = new ewe.sys.mThread(){
		public void run(){
			try{
				ret[1] = ss.accept();
			}catch(Exception e){
			}finally{
				try{
					ss.close();
				}catch(Exception e){}
			}
		}
	};
	acc.start();
	ret[0] = new ewe.net.Socket("localhost",port);
	try{
		acc.join();
	}catch(InterruptedException e){}
	if (ret[1] == null) throw new ewe.io.IOException("Could not connect.");
	return ret;
}

//##################################################################
}
//##################################################################

