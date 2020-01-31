package samples.tictactoe;
import ewe.fx.Point;
import ewe.io.IOException;
import ewe.io.RemoteCallException;
import ewe.io.RemoteCallTask;
import ewe.io.Stream;
import ewe.net.ClientServerConnector;
import ewe.net.ServerSocket;
import ewe.net.Socket;
import ewe.net.SocketMaker;
import ewe.ui.MessageBox;

//##################################################################
public class RemoteGameHostClient extends RemoteCallTask
implements GameHost, GameClient
{
//##################################################################

GameHost myHost;
GameClient myClient;

//-------------------------------------------------------------------
void toRuntimeException(RemoteCallException e)
//-------------------------------------------------------------------
{
	Throwable t = e.getException();
	if (t instanceof RuntimeException) throw (RuntimeException)t;
	throw new RuntimeException(e.getMessage());
}

//===================================================================
public void play(GameClient client, Point location)
//===================================================================
{
	try{
		newCall("hostPlay(Lewe/fx/Point;)V").add(location).call();
	}catch(RemoteCallException e){toRuntimeException(e);}
}
//===================================================================
public void hostPlay(ewe.fx.Point location)
//===================================================================
{
	myHost.play(this,location);
}
//===================================================================
public void addClient(GameClient c,int type)
//===================================================================
{
	try{
		newCall("hostAddClient(I)V").add(type).call();
	}catch(RemoteCallException e){toRuntimeException(e);}
}
//===================================================================
public void hostAddClient(int type)
//===================================================================
{
	myHost.addClient(this,type);
}



//===================================================================
public void played(int type, Point where)
//===================================================================
{
	try{
		newCall("clientPlayed(ILewe/fx/Point;)V").add(type).add(where).call();
	}catch(RemoteCallException e){toRuntimeException(e);}
}
//===================================================================
public void clientPlayed(int type, ewe.fx.Point where)
//===================================================================
{
	myClient.played(type,where);
}

//===================================================================
public RemoteGameHostClient(Stream s, GameClient client)
//===================================================================
{
	super(s);
	myClient = client;
}
//===================================================================
public RemoteGameHostClient(Stream s, GameHost host)
//===================================================================
{
	super(s);
	myHost = host;
}

//===================================================================
public static ewe.sys.Handle acceptNewConnections(final LocalGameHost lgh,final ServerSocket ss)
//===================================================================
{
	return new ewe.sys.TaskObject(){
		protected void doStop(int reason){
			super.doStop(reason);
			try{
				ss.close();
			}catch(Exception e){}
		}
		protected void doRun(){
			while(true){
				try{
					Socket s = ss.accept();
					//
					// When an incoming connection is accepted, start the RemoteCallTask running
					// in Host mode.
					new RemoteGameHostClient(s,lgh);
				}catch(IOException e){
					return;
				}
			}
		}
	}.startTask();
}
//===================================================================
public static ServerSocket setupNewHost(final LocalGameHost lgh)
//===================================================================
{
	//
	// Get an interactive ServerSocket maker. It will default to port 3000
	//
	SocketMaker sm = new SocketMaker(!ewe.sys.Vm.isMobile() ? SocketMaker.LOCAL_SOCKET : SocketMaker.INFRA_RED,true,"Setup Game Host");
	sm.port = 3000;
	sm.serviceName = "TicTacToe";
	final ServerSocket ss = (ServerSocket)sm.getSocket(true,true);
	if (ss == null) return null;
	//
	// Now display a dialog box and start the main server loop.
	//
	MessageBox mb = new MessageBox("Server is Running",
		"The Game Host is running on:\nHost: "+ss.getInetAddress().getHostName()+
		"\nPort: "+ss.getLocalPort()+"\n\nClose this box to stop server.",MessageBox.MBOK);
	mb.exitSystemOnClose = true;
	mb.show(); // Display non-modally. It will immediately return.
	mb.waitUntilPainted(1000);
	//
	acceptNewConnections(lgh,ss);
	return ss;
}

//===================================================================
public static GameHost connectTo(GameClient gc, String hostName, int port)
//===================================================================
{
	//
	// Get an interactive Socket (client) maker. It will default to port 3000
	//
	SocketMaker sm = new SocketMaker(!ewe.sys.Vm.isMobile() ? SocketMaker.LOCAL_SOCKET : SocketMaker.INFRA_RED,false,"Connect to Host");
	sm.port = port;
	sm.hostName = hostName;
	sm.serviceName = "TicTacToe";
	Socket s = sm.getConnection(true,true);
	if (s == null) return null;
	//
	// Start the RemoteCallTask running in client mode.
	//
	return new RemoteGameHostClient(s,gc);
}

//===================================================================
static boolean setupBoard(Board b,GameHost gh)
//===================================================================
{
	//
	// Now try to add the player. First as a 'O', then as an 'X' and finally as an Observer.
	//
	int finalType = -1;
	if (finalType == -1) try{
		gh.addClient(b,Nought);
		finalType = Nought;
	}catch(IllegalArgumentException e){}
	//
	if (finalType == -1) try{
		gh.addClient(b,Cross);
		finalType = Cross;
	}catch(IllegalArgumentException e){}
	//
	if (finalType == -1) try{
		gh.addClient(b,Observer);
		finalType = Observer;
	}catch(IllegalArgumentException e){}
	//
	if (finalType == -1) return false;
	//
	// Setup and run the board.
	//
	b.setupBoard(4,200);
	b.host = gh;
	b.run(finalType);
	return true;
}
//-------------------------------------------------------------------
static void addBoard(Socket sock)
//-------------------------------------------------------------------
{
	Board b = new Board();
	GameHost gh = new RemoteGameHostClient(sock,b);
	setupBoard(b,gh);
}
//===================================================================
public static void makeConnection()
//===================================================================
{
	try{
		final LocalGameHost lgh = new LocalGameHost();
		int did = new MessageBox("Be the Server","Do you want to be the server?",MessageBox.MBYESNOCANCEL).execute();
		if (did == MessageBox.IDCANCEL) return;
		boolean server = did == MessageBox.IDYES;
		int type = ewe.sys.Vm.isMobile() ? ClientServerConnector.INFRA_RED : ClientServerConnector.TCP_SOCKET;
		ClientServerConnector cc = new ClientServerConnector(server,type,"127.0.0.1",2000){
			{
				showSelectionScreen = true;
			}
			protected void client(Socket sock){
				addBoard(sock);
			}
			protected void server(Socket sock,boolean firstOne){
				if (firstOne) setupBoard(new Board(),lgh);
				new RemoteGameHostClient(sock,lgh);
			}
		};
		if (cc.waitOnConnection()){
			//ewe.sys.Vm.debug("I am the server!");
		}else{
			//ewe.sys.Vm.debug("I am the client!");
		}
		if (new MessageBox("Add Player","Add a new player?",MessageBox.MBYESNO).execute() == MessageBox.IDYES){
			if (cc.isServer){
				setupBoard(new Board(),lgh);
			}else{
				addBoard(cc.connectToServer());
			}
		}
	}catch(IOException e){
		new ewe.ui.ReportException(e,null,null,false).execute();
		ewe.sys.Vm.exit(0);
	}
	/*
	SocketMaker sm = new SocketMaker(sm.LOCAL_SOCKET,true,"Client Server");
	sm.timeout = 60;
	sm.hostName = "127.0.0.1";
	sm.port = 2000;
	Board b = new Board();
	try{
		Object got = sm.makeClientServerConnection(null,0,true);
		if (got instanceof Socket){
			lgh = null;
			GameHost gh = new RemoteGameHostClient((Socket)got,b);
			setupBoard(b,gh);
		}else{
			Object [] s = (Object [])got;
			new RemoteGameHostClient((Socket)s[0],lgh);
			acceptNewConnections(lgh,(ServerSocket)s[1]);
			setupBoard(b,lgh);
		}
		if (new MessageBox("Add Player","Add a new player?",MessageBox.MBYESNO).execute() != MessageBox.IDYES)
			break;
		Board b = new Board();
		GameHost gh = lgh;
		if (gh == null) gh = connectTo(b,"127.0.0.1",2000)

	*/
}
//##################################################################
}
//##################################################################
