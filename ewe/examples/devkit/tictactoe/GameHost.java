package samples.tictactoe;
import ewe.fx.Point;

//##################################################################
public interface GameHost extends TTTConstants{
//##################################################################

public void addClient(GameClient client,int type);
public void play(GameClient client,Point where);

//##################################################################
}
//##################################################################
