/****************************************************************

Sample code for Ewe Application Development.

Updated March 2003, for 1.26 release.

****************************************************************/

package samples.sockets;
import ewe.data.*;
import ewe.ui.*;
import ewe.net.*;

//##################################################################
public class SocketDemo extends Editor{
//##################################################################

//===================================================================
public SocketDemo()
//===================================================================
{
	windowFlagsToSet &= ~Window.FLAG_MAXIMIZE_ON_PDA;
	title = "Socket Demo";
	resizable = false;
	ButtonBar bb = new ButtonBar();
	addField(bb.add("Server Socket"),"server");
	addField(bb.add("Client Socket"),"client");
	addLast(bb);
	bb = new ButtonBar();
	addField(bb.add("Datagram Socket"),"datagram");
	addField(cancel = bb.add("Exit"),"exit");
	addLast(bb);
}
//===================================================================
void showTelnet(Socket sock,boolean server)
//===================================================================
{
	Telnet t = new Telnet();
	t.title = server ? "Server Side" : "Client Side";
	t.connect(sock);
	t.show();
}
//===================================================================
public void action(ewe.reflect.FieldTransfer ft,Editor ed)
//===================================================================
{
	if (ft.fieldName.equals("server")){
		new ewe.sys.TaskObject(){
			protected void doRun(){
				try{
				SocketMaker sm = new SocketMaker(0,true,"Server Socket Maker");
				sm.port = 2000;
				Object got = sm.getSocket(true,true);
				if (!(got instanceof ServerSocket)) {
					String err = sm.getError();
					if (err != null) new MessageBox("Error",err,MBOK).execute();
					return;
				}
				ServerSocket ss = (ServerSocket)got;
				while(true){
					Socket sock = sm.getConnection(ss,null,true,-1);
					if (sock == null) {
						try{ss.close();}catch(Exception e){};
						return;
					}
					showTelnet(sock,true);
					//new ConnectedSocket(sock,true).getEditor(0).show();
				}
				}catch(Exception e){
					e.printStackTrace();
				}
			}
		}.startTask();
	}else if (ft.fieldName.equals("client")){
		new ewe.sys.TaskObject(){
			protected void doRun(){
				SocketMaker sm = new SocketMaker(0,false,"Client Socket Maker");
				sm.port = 2000;
				Object got = sm.getSocket(true,true);
				if (!(got instanceof Socket)) {
					new MessageBox("Error","Could not connect to server!",MBOK).execute();
					return;
				}
				showTelnet((Socket)got,false);
			}
		}.startTask();
	}else if (ft.fieldName.equals("datagram")){
		try{
			InputObject io = new InputObject("Local Address|localAddress$|Local Port|localPort$I",30);
			PropertyList pl = io.input(null,"Please specify port",null);
			if (pl == null) return;
			String host = pl.getString("localAddress",null);
			int port = pl.getInt("localPort",0);
			DatagramSocket ds = host == null ? new DatagramSocket(port) : new DatagramSocket(port,host);
			new ConnectedSocket(ds).getEditor(0).show();
		}catch(Exception e){
			new ReportException(e,null,null,false).execute();
		}
	}
}
/*
//-------------------------------------------------------------------
protected abstract Stream getConnection(Editor ed);
//-------------------------------------------------------------------
 */
//##################################################################
}
//##################################################################
