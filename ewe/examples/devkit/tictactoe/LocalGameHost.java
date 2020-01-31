package samples.tictactoe;
import ewe.fx.Point;
import ewe.util.Vector;

//##################################################################
public class LocalGameHost implements GameHost{
//##################################################################

public Vector clients = new Vector();

	//##################################################################
	class Client{
	//##################################################################
	public GameClient client;
	public int type;
	//##################################################################
	}
	//##################################################################

//===================================================================
public Client findClient(int type) throws IllegalArgumentException
//===================================================================
{
	for (int i = 0; i<clients.size(); i++){
		Client c = (Client)clients.get(i);
		if (c.type == type) return c;
	}
	return null;
}
//===================================================================
public Client findClient(GameClient cl) throws IllegalArgumentException
//===================================================================
{
	for (int i = 0; i<clients.size(); i++){
		Client c = (Client)clients.get(i);
		if (c.client == cl) return c;
	}
	return null;
}
//===================================================================
public void addClient(GameClient client,int type) throws IllegalArgumentException
//===================================================================
{
	if (type != Observer && findClient(type) != null)
		throw new IllegalArgumentException("That player type already exists");
	Client c = new Client();
	c.type = type;
	c.client = client;
	clients.add(c);
}
//===================================================================
public void play(GameClient c,final Point where)
//===================================================================
{
	Client cl = findClient(c);
	if (cl == null)
		throw new IllegalArgumentException("That player type does not exist");
	final int played = cl.type;
	int other = cl.type == Nought ? Cross : Nought;
	cl = findClient(other);
	if (cl == null)
		throw new IllegalArgumentException("The other player does not exist");
	cl.client.played(played,where);
	new ewe.sys.mThread(){
		public void run(){
			for (int i = 0; i<clients.size(); i++){
				Client c = (Client)clients.get(i);
				if (c.type == Observer){
					c.client.played(played,where);
				}
			}
		}
	}.start();
}

//##################################################################
}
//##################################################################
