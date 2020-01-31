/****************************************************************

Sample code for Ewe Application Development.

Updated March 2003, for 1.26 release.

****************************************************************/

package samples.terminal;
import ewe.io.Stream;
import ewe.sys.Lock;
import ewe.sys.mThread;
import ewe.ui.AppForm;
import ewe.ui.CellPanel;
import ewe.ui.Editor;
import ewe.ui.Event;
import ewe.ui.Gui;
import ewe.ui.MessageBox;
import ewe.ui.ScrollBarPanel;
import ewe.ui.mButton;
import ewe.ui.mCheckBox;

//##################################################################
public class Terminal extends AppForm{
//##################################################################
public boolean localEcho = true;
public boolean addLF = true;
public boolean autoWrap = true;
public boolean stripCR = true;
protected ConsoleTerminal terminal;
protected Stream stream;
mButton connect, disconnect;

//-------------------------------------------------------------------
protected ConsoleTerminal makeConsoleTerminal()
//-------------------------------------------------------------------
{
	return new ConsoleTerminal();
}
//===================================================================
public Terminal()
//===================================================================
{
	super(true,true);
	title = "Terminal";
	CellPanel cp = new CellPanel();
	addField(cp.addNext(new mCheckBox("Local Echo")),"localEcho");
	addField(cp.addNext(new mCheckBox("Add LF")),"addLF");
	if (Gui.screenIs(Gui.PDA_SCREEN)) cp.endRow();
	addField(cp.addNext(new mCheckBox("Auto Wrap")),"autoWrap");
	addField(cp.addNext(new mCheckBox("Strip CR")),"stripCR");
	addExpandingTool(cp,"ewe/SmallConfig.bmp","ewe/SmallConfigMask.bmp","Tools");
	cp.modifyAll(NoFocus,0,true);
	terminal = makeConsoleTerminal();
	terminal.addListener(this);
	addField(connect = addToolButton("samples/terminal/connect.png",null,"Connect"),"connect");
	addField(disconnect = addToolButton("samples/terminal/disconnect.png",null,"Disconnect"),"disconnect");
	cancel = addToolButton("ewe/exitsmall.bmp",ewe.fx.Color.White,"Exit");
	terminal.localEcho = localEcho;
	terminal.addLF = addLF;
	data.addLast(new ScrollBarPanel(terminal));
	data.setBorder(EDGE_ETCHED,2);
	stateChanged();
	changeParameters();
	tools.modifyAll(NoFocus,0,true);
}
//-------------------------------------------------------------------
void changeParameters()
//-------------------------------------------------------------------
{
	terminal.localEcho = localEcho;
	terminal.addLF = addLF;
	terminal.wrapLength = (autoWrap ? -1 : 0);
	terminal.stripCR = stripCR;
}

//-------------------------------------------------------------------
protected Stream getConnection(Editor ed){return null;}
//-------------------------------------------------------------------

//-------------------------------------------------------------------
protected void connectionBroken()
//-------------------------------------------------------------------
{
	new MessageBox("Disconnected","Connection lost.",MessageBox.MBOK).execute();
}
//-------------------------------------------------------------------
protected void disconnected()
//-------------------------------------------------------------------
{
}
protected void connected()
{
}
//===================================================================
public void fieldChanged(String fieldName,Editor ed)
//===================================================================
{
	changeParameters();
}
//===================================================================
public void action(String fieldName,Editor ed)
//===================================================================
{
	if (fieldName.equals("connect")){
		Stream s = getConnection(ed);
		if (s != null)
			connect(s);
	}else if (fieldName.equals("disconnect")){
		disconnect();
	}
}
//===================================================================
public void connect(Stream stream)
//===================================================================
{
	this.stream = stream;
	terminal.clear(true);
	terminal.connect(stream);
	stateChanged();
	connected();
}
Lock closedLock = new Lock();

//===================================================================
public void disconnect()
//===================================================================
{
	closedLock.synchronize(); try{
		stream = null;
		terminal.closeIO();
		stateChanged();
		disconnected();
	}finally{
		closedLock.notifyAllWaiting();
		closedLock.release();
	}
}
//-------------------------------------------------------------------
void stateChanged()
//-------------------------------------------------------------------
{
	if (stream != null){
		connect.modify(Disabled,0);
		disconnect.modify(0,Disabled);
	}else{
		disconnect.modify(Disabled,0);
		connect.modify(0,Disabled);
	}
	connect.repaintNow();
	disconnect.repaintNow();
}

//===================================================================
public void onEvent(Event ev)
//===================================================================
{
	if (ev instanceof TerminalEvent){
		if (ev.type == TerminalEvent.CONNECTION_BROKEN){
			if (stream != null) connectionBroken();
			disconnect();
		}
	}else
		super.onEvent(ev);
}
//===================================================================
public void close(int exitCode)
//===================================================================
{
	closedLock.synchronize(); try{
		if (stream != null){
			stream.close();
			stream = null;
			try{
				closedLock.waitOn();
				mThread.nap(500);
			}catch(InterruptedException e){}
		}
	}finally{
		closedLock.release();
	}
	super.close(exitCode);
}
//##################################################################
}
//##################################################################
