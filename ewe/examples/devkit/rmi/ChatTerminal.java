package samples.rmi;
import ewe.net.Socket;
import ewe.ui.CellPanel;
import ewe.ui.Console;
import ewe.ui.Editor;
import ewe.ui.InputBox;
import ewe.ui.MessageBox;
import ewe.ui.ReportException;
import ewe.ui.ScrollBarPanel;
import ewe.ui.mButton;
import ewe.ui.mInput;
/**
* A simple terminal based Chat client. It can also be used to spawn a new Window
* which will join the Chat under a different name.
**/
//##################################################################
public class ChatTerminal extends Editor implements ChatClient{
//##################################################################

public String message = "Hello!", host = "localhost";
public Console messages;
public String myName = "Unnamed";
public ChatServer server;

//===================================================================
public ChatTerminal()
//===================================================================
{
	CellPanel cp = new CellPanel();
	mInput mi = new mInput();
	mi.wantReturn = true;
	cp.addNext(addField(mi,"message"));
	cp.addNext(addField(new mButton("Send"),"send")).setCell(DONTSTRETCH);
	addLast(cp).setCell(HSTRETCH);
	addLast(new ScrollBarPanel(messages = new Console()));
	cp = new CellPanel();
	mi = new mInput();
	mi.wantReturn = true;
	cp.addNext(addField(mi,"host"));
	cp.addNext(addField(new mButton("Join"),"join")).setCell(DONTSTRETCH);
	addLast(cp).setCell(HSTRETCH);
	messages.setTextSize(40,20);
	messages.wrapLength = -1;
}
//===================================================================
public void action(String fieldName,Editor ed)
//===================================================================
{
	if (fieldName.equals("send") || fieldName.equals("message")){
		//messages.append(message+"\n",true);
		//messagePosted(null,message);
		server.postMessage(this,message);
		message = "";
		ed.toControls("message");
	}else if (fieldName.equals("join")){
		join(host);
	}
}
//===================================================================
public void setName(String name)
//===================================================================
{
	myName = name;
	setTitle("Chat: "+name);
}
//===================================================================
public void messagePosted(String from, String message)
//===================================================================
{
	if (from == null)
		messages.append("----------\n"+message+"\n----------\n",true);
	else
		messages.append(from+" > "+message+"\n",true);
}

/**
* Create a new ChatTerminal and attempt to join the server on the specified
* host computer name.
**/
//===================================================================
public static ChatTerminal join(String host)
//===================================================================
{
	if (host == null) host = new InputBox("Host").input("localhost",20);
	if (host == null) return null;
	try{
		Socket s = new Socket(host,ChatServerObject.chatPort);
		ChatTerminal ct = new ChatTerminal();
		ChatServerProxy csp = new ChatServerProxy(s,ct);
		return join(csp,ct);
	}catch(Exception e){
		new ReportException(e).execute();
		return null;
	}
}
/**
* Attempt to join the specified ChatServer.
**/
//===================================================================
public static ChatTerminal join(ChatServer cs,ChatTerminal ct)
//===================================================================
{
	while(true){
		String name = new InputBox("Name").input("unnamed",20);
		if (name == null) return null;
		String msg = cs.join(ct,name);
		if (msg == null){
			ct.setName(name);
			ct.server = cs;
			numTerminals++;
			ct.show();
			return ct;
		}else{
			new MessageBox("Join Error",msg,MBOK).execute();
		}
	}
}
static int numTerminals = 0;
//-------------------------------------------------------------------
protected void formClosing()
//-------------------------------------------------------------------
{
	super.formClosing();
	new ewe.sys.mThread(){
		public void run(){
			server.leave(ChatTerminal.this);
			numTerminals--;
			if (numTerminals <= 0 && !ChatServerObject.amRunningSocketServer) ewe.sys.Vm.exit(0);
		}
	}.start();
}
//##################################################################
}
//##################################################################
