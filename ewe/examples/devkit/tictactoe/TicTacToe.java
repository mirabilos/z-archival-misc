package samples.tictactoe;
import ewe.net.ServerSocket;
import ewe.ui.Form;
import ewe.ui.MessageBox;

//##################################################################
public class TicTacToe implements TTTConstants{
//##################################################################

//===================================================================
public static void main(String args[])
//===================================================================
{

	ewe.sys.Vm.startEwe(args);
	if (true){
		RemoteGameHostClient.makeConnection();
		return;
	}
	//
	// Create a LocalGameHost. This may then have a RemoteGameHostClient associated with it
	// and so act as a Remote GameHost.
	//
	LocalGameHost lgh = new LocalGameHost();
	ServerSocket ss = null;
	int run = new MessageBox("Run Game Server","Run the Game Server?",MessageBox.MBYESNOCANCEL).execute();
	if (run == Form.IDCANCEL) ewe.sys.Vm.exit(0);
	else if (run == Form.IDYES){
		ss = RemoteGameHostClient.setupNewHost(lgh);
		if (ss == null) lgh = null;
	}else
		lgh = null;
	//
	// Now add boards to the local server or a remote server.
	//
	int i = 0;
	for (i = 0; i<4; i++){
		if (new MessageBox("Add Player","Add a new player?",MessageBox.MBYESNO).execute() != MessageBox.IDYES)
			break;
		Board b = new Board();
		GameHost gh = lgh;
		//
		// If there is no local GameHost, then attempt to connect to one.
		//
		if (gh == null) {
			String hostName = "192.168.0.52";
			int port = 3000;
			gh = RemoteGameHostClient.connectTo(b,hostName,port);
			if (gh == null) break;
		}
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
		if (finalType == -1) break;
		//
		// Setup and run the board.
		//
		b.setupBoard(4,200);
		b.host = gh;
		b.run(finalType);
	}
	if (i == 0 && ss == null) //No board windows are up, no server socket is running.
		ewe.sys.Vm.exit(0);
}

//##################################################################
}
//##################################################################
