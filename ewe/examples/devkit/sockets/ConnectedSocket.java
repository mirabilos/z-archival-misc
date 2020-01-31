/****************************************************************

Sample code for Ewe Application Development.

Updated March 2003, for 1.26 release.

****************************************************************/

package samples.sockets;
import ewe.data.LiveObject;
import ewe.net.DatagramPacket;
import ewe.net.DatagramSocket;
import ewe.net.InetAddress;
import ewe.net.Socket;
import ewe.net.SocketException;
import ewe.sys.Lock;
import ewe.sys.TaskObject;
import ewe.sys.mThread;
import ewe.ui.ButtonBar;
import ewe.ui.CellPanel;
import ewe.ui.Control;
import ewe.ui.Editor;
import ewe.ui.InputStack;
import ewe.ui.Menu;
import ewe.ui.MenuItem;
import ewe.ui.MessageBox;
import ewe.ui.ReportException;
import ewe.ui.ScrollBarPanel;
import ewe.ui.ScrollClient;
import ewe.ui.mButton;
import ewe.ui.mTextPad;
//##################################################################
public class ConnectedSocket extends LiveObject{
//##################################################################

public String status = "Not connected.";
public String received = "", message = "Hello there!";
public Socket sock;
public DatagramSocket ds;
public String destination = "127.0.0.1";
public int destPort = 2000;

//===================================================================
public ConnectedSocket(Socket sock,boolean fromServer)
//===================================================================
{
	this.sock = sock;
	try{
		if (true) sock.setSoLinger(true,1);
	}catch(SocketException se){
		se.printStackTrace();
	}
	status = (sock.isOpen()) ? "Connected" : "Not connected";
	if (fromServer) status += " - Server side.";
	else status += " - Client side.";
}
//===================================================================
public ConnectedSocket(DatagramSocket ds)
//===================================================================
{
	this.ds = ds;
	status = "DatagramSocket Ready";
}
//===================================================================
public void addToPanel(CellPanel cp,Editor f,int which)
//===================================================================
{
	f.title = ds == null ? "Connected Socket" : "Datagram Socket: "+ds.getLocalPort();
	f.titleCancel = new mButton(f.cross);
	InputStack is = new InputStack();
	cp.addLast(is).setCell(cp.HSTRETCH);
	is.addInputs(f,"Status:|status|To Send:|message");
	if (ds != null) is.addInputs(f,"Dest.|destination|Port|destPort");
	((Control)f.findFieldTransfer("status").dataInterface).modify(cp.DisplayOnly,0);
	ButtonBar bb = new ButtonBar();
	Control b;
	cp.addLast(bb).setCell(cp.HSTRETCH);
	f.addField(bb.add("Send"),"send");
	b = f.addField(bb.add("Receive"),"recv");
	Menu menu = new Menu(new String[]{"Receive One Message","Continuous Receive"},"Receive");
	b.setMenu(menu);
	bb.endRow();
	f.addField(bb.add("Disconnect"),"disconnect");
	f.addField(bb.add("Socket Info"),"info");
	cp.addLast(new ScrollBarPanel((ScrollClient)f.addField(new mTextPad(5,40),"received")));
	f.addField(b,"receiveItem");
	//new messageListener(this,f).startTask();
	//new testTask(this,f).startTask();
}

public MenuItem receiveItem = new MenuItem();

Lock lock = new Lock();

//===================================================================
public void doDisconnect(Editor f)
//===================================================================
{
	if (sock == null && ds == null) return;
	if (!lock.grab()) return;
	if (sock != null) sock.close();
	else ds.close();
	lock.release();
	status = "Not connected.";
	f.toControls("status");
	//sock = null;
	//ds = null;
	//f.exit(0);
}
//===================================================================
public void action(ewe.reflect.FieldTransfer ft,Editor f)
//===================================================================
{
	action(ft.fieldName,f);
}
//===================================================================
public void action(final String nm,final Editor f)
//===================================================================
{
	if (nm.equals("info")){
		String info;
		if (sock != null) {
			info = "Remote Host: "+sock.getInetAddress()+"\nRemote Port: "+sock.getPort();
			info += "\n\nLocal Host: "+sock.getLocalAddress()+"\nLocal Port: "+sock.getLocalPort();
			try{
				info += "\n\nRx Buff: "+sock.getReceiveBufferSize()+"\nTx Buf: "+sock.getSendBufferSize();
			}catch(SocketException e){}
		}
		else {
			info = "Local Host: "+ds.getLocalAddress()+"\nLocal Port: "+ds.getLocalPort();
			try{
				ds.setReceiveBufferSize(8000);
				ds.setSendBufferSize(7000);
				info += "\n\nRx Buff: "+ds.getReceiveBufferSize()+"\nTx Buf: "+ds.getSendBufferSize();
			}catch(SocketException e){}
		}
		new MessageBox("Socket Info",info,MessageBox.MBOK).execute();
	}else if (nm.equals("send")){
		byte [] toSend = ewe.util.mString.toAscii(message+"\n");
		if (sock != null){
			if (sock.writeBytes(toSend,0,toSend.length) != toSend.length){
				new MessageBox("Closed","Socket closed or error!",0).execute();
				doDisconnect(f);
			}// This will be called in a coroutine - so this will not block other co-routines.
		}else if (ds != null){
			try{
				InetAddress got = InetAddress.getByName(destination);
				DatagramPacket dp = new DatagramPacket(toSend,toSend.length,got,destPort);
				ds.send(dp);
			}catch(Exception e){
				new ReportException(e,null,null,false).execute();
			}
		}
	}else if (nm.equals("disconnect")){
		doDisconnect(f);
	}if (nm.equals("recv")){
		f.modifyFields(nm,true,f.Disabled,0,true);
		new mThread(){
			public void run(){
				ConnectedSocket form = ConnectedSocket.this;
				Editor editor = f;
				byte [] buff = new byte[1024];
				int got = 0;
				if (sock != null){
					got = form.sock.readBytes(buff,0,buff.length);
				}else if (ds != null){
					try{
						DatagramPacket dp = new DatagramPacket(buff,buff.length);
						form.ds.receive(dp);
						form.received += dp.getAddress().getHostAddress()+":"+dp.getPort()+"\n";
						got = dp.getLength();
					}catch(Exception e){
						e.printStackTrace();
						got = -2;
					}
				}
				if (got == 0){
					new MessageBox("Closed","Socket closed!",0).execute();
				}else if (got < 0){
					new MessageBox("Closed","Socket error!",0).execute();
				}
				editor.modifyFields(nm,false,editor.Disabled,0,true);
				if (got < 1) {
					form.doDisconnect(editor);
					return;
				}
				String str = ewe.util.mString.fromAscii(buff,0,got);
				form.received += str;
				editor.toControls("received");
				}
		}.start();
	}else if (nm.equals("recvAlways")){
		f.modifyFields("recv",true,f.Disabled,0,true);
		new messageListener(this,f).startTask();
	}
}
//===================================================================
public void fieldChanged(ewe.reflect.FieldTransfer ft,Editor f)
//===================================================================
{
	if (ft.fieldName.equals("receiveItem")){
		if (receiveItem.label.startsWith("Receive")) action("recv",f);
		else action("recvAlways",f);
	}
}
//##################################################################
}
//##################################################################

//##################################################################
class messageListener extends TaskObject{
//##################################################################

ConnectedSocket form;
Editor editor;
//===================================================================
public messageListener(ConnectedSocket scf,Editor f)
//===================================================================
{
	form = scf;
	editor = f;
}

//-------------------------------------------------------------------
protected void doRun()
//-------------------------------------------------------------------
{
	byte [] buff = new byte[1024];
	form.received = "";
	editor.toControls("received");
	while(true){
		int got = 0;
		if (form.sock != null){
			got = form.sock.readBytes(buff,0,buff.length);
		}else if (form.ds != null){
			try{
				DatagramPacket dp = new DatagramPacket(buff,buff.length);
				form.ds.receive(dp);
				form.received += dp.getAddress().getHostAddress()+":"+dp.getPort()+"\n";
				got = dp.getLength();
			}catch(Exception e){
				e.printStackTrace();
				got = -2;
			}
		}
		if (got == 0){
			new MessageBox("Closed","Socket closed!",0).execute();
		}else if (got < 0){
			new MessageBox("Closed","Socket error!",0).execute();
		}
		if (got < 1) {
			form.doDisconnect(editor);
			return;
		}
		String str = ewe.util.mString.fromAscii(buff,0,got);
		form.received += str;
		editor.toControls("received");
	}
}
//##################################################################
}
//##################################################################

//##################################################################
class testTask extends TaskObject{
//##################################################################

ConnectedSocket form;
Editor editor;
//===================================================================
testTask(ConnectedSocket form,Editor editor)
//===================================================================
{
	this.form = form;
	this.editor = editor;
}

protected void doRun()
{
	for (int i = 0; i<60; i++){
		form.status = "Count: "+i;
		editor.toControls("status");
		sleep(1000);
	}
}
//##################################################################
}
//##################################################################
