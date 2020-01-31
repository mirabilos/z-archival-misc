package samples.sockets;
import ewe.io.Stream;
import ewe.net.Socket;
import ewe.net.SocketMaker;
import ewe.ui.Editor;
import ewe.ui.MessageBox;

//##################################################################
public class Telnet extends samples.terminal.Terminal{
//##################################################################
//-------------------------------------------------------------------
protected Stream getConnection(Editor ed)
//-------------------------------------------------------------------
{
	SocketMaker sm = new SocketMaker(0,false,"Client Socket Maker");
	sm.port = 2000;
	Object got = sm.getSocket(true,true);
	if (!(got instanceof Socket)) {
		new MessageBox("Error","Could not connect to server!",MBOK).execute();
		return null;
	}
	return (Stream)got;
}
//##################################################################
}
//##################################################################
