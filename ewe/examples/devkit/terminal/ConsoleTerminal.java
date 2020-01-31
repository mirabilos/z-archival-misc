/****************************************************************

Sample code for Ewe Application Development.

Updated March 2003, for 1.30release.

****************************************************************/

package samples.terminal;
import ewe.io.IOHandle;
import ewe.io.Stream;
import ewe.sys.TaskObject;
import ewe.ui.Console;
import ewe.ui.EventListener;
import ewe.util.EventDispatcher;

//##################################################################
public class ConsoleTerminal extends Console{
//##################################################################

protected Stream stream;
protected boolean closed = false;
public boolean addLF = false;
public boolean stripCR = false;
protected EventDispatcher dispatcher;

//===================================================================
public void addListener(EventListener e)
//===================================================================
{
	if (dispatcher == null) dispatcher = new EventDispatcher();
	dispatcher.addListener(e);
}
//===================================================================
public void removeListener(EventListener e)
//===================================================================
{
	if (dispatcher != null) dispatcher.removeListener(e);
}
//===================================================================
public ConsoleTerminal()
//===================================================================
{
	localEcho = true;
	setPreferredSize(480,320);
}

//===================================================================
public void connect(Stream s)
//===================================================================
{
	closed = false;
	this.stream = s;
	final IOHandle[] streams = connectTo(s);
	new TaskObject(){
		protected void doRun(){
			try{
				streams[0].waitUntilStopped();
				if (!closed) {
					connectionBroken();
					closeIO();
				}
				streams[1].waitUntilStopped();
			}catch(Exception e){

			}
		}
	}.startTask();
}
//-------------------------------------------------------------------
protected void connectionBroken()
//-------------------------------------------------------------------
{
	if (dispatcher == null) return;
	dispatcher.dispatch(new TerminalEvent(this,TerminalEvent.CONNECTION_BROKEN));
}

//===================================================================
public void closeIO()
//===================================================================
{
	super.closeIO();
	if (stream != null) stream.close();
	closed = true;
}
//===================================================================
public void formClosing()
//===================================================================
{
	if (!closed){
		closed = true;
		closeIO();
		if (dispatcher != null) dispatcher.close();
	}
	super.formClosing();
}
//===================================================================
public void append(char[] data,int start,int length,boolean updateDisplay)
//===================================================================
{
	if (!stripCR) super.append(data,start,length,updateDisplay);
	else{
		char[] n = new char[length];
		int did = 0;
		for (int i = 0; i<length; i++){
			if (data[start+i] == '\r') continue;
			n[did++] = data[start+i];
		}
		super.append(n,0,did,updateDisplay);
	}
}
//===================================================================
public void sendChar(char ch)
//===================================================================
{
	if ((ch == '\r' || ch == '\n') && addLF) {
		super.sendChar('\r');
		super.sendChar('\n');
	}else
		super.sendChar(ch);
}
/*
//=================================================================
public static void main(String[] args) throws IOException
//=================================================================
{
	ewe.sys.Vm.startEwe(args);
	Socket s = new Socket("localhost",Convert.toInt(args[0]));
	Form f = new Form();
	f.title = "Telnet: "+args[0];
	f.addLast(new ScrollBarPanel(new ConsoleTerminal(s)));
	f.execute();
	ewe.sys.Vm.exit(0);
}
*/
//##################################################################
}
//##################################################################
