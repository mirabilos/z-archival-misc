package samples.tictactoe;
import ewe.graphics.*;
import ewe.fx.*;
import ewe.ui.*;
//##################################################################
public class Board extends InteractivePanel implements GameClient, TTTConstants{
//##################################################################

/**
* The simplest TTT board is 3 by 3 square.
**/
public int gridSize = 3;
public int lineThickness = 3;
public int pieceSize = 0;

//===================================================================
public void setupBoard(int grid,int screenSize)
//===================================================================
{
	gridSize = grid;
	Image b = new Image(screenSize,screenSize);
	Graphics g = new Graphics(b);
	g.setColor(Color.White);
	pieceSize = (screenSize-(lineThickness*(grid-1)))/grid;
	g.fillRect(0,0,screenSize,screenSize);
	g.setPen(new Pen(new Color(0,0,255),Pen.SOLID,lineThickness));
	int pos = pieceSize+lineThickness/2;
	for (int i = 0; i<grid-1; i++){
		g.drawLine(pos,0,pos,screenSize);
		pos += pieceSize+lineThickness;
	}
	pos = pieceSize+lineThickness/2;
	for (int i = 0; i<grid-1; i++){
		g.drawLine(0,pos,screenSize,pos);
		pos += pieceSize+lineThickness;
	}
	setAndSizeToBackgroundImage(b);
	setPreferredSize(screenSize,screenSize);
	//
	for (int r = 0; r<grid; r++)
		for (int c = 0; c<grid; c++){
			Piece p = new Piece(Piece.Empty);
			p.piecePoint.set(c,r);
			p.location.set(c*(pieceSize+lineThickness),r*(pieceSize+lineThickness),pieceSize,pieceSize);
			p.properties &= ~p.IsNotHot;
			addImage(p);
		}
}

//===================================================================
public void played(int pieceType,Point location)
//===================================================================
{
	Piece empty = null;
	for (int i = 0; i<images.size(); i++){
		if (images.get(i) instanceof Piece){
			Piece p = (Piece)images.get(i);
			if (p.myType == p.Empty && location.equals(p.piecePoint)){
				empty = p;
				break;
			}
		}
	}
	Piece p = empty;
	if (empty != null) {
		empty.myType = pieceType;
		empty.properties |= empty.IsNotHot;
	}else{
		p = new Piece(pieceType);
 		p.location.set(location.x*(pieceSize+lineThickness),location.y*(pieceSize+lineThickness),pieceSize,pieceSize);
		p.properties  |= empty.IsNotHot;
	}
	p.refresh();

	if (pieceType != playerType && playerType != Observer)
		myTurn = true;
	else
		myTurn = false;
}

public int playerType = Piece.Nought;
public boolean selfPlay = false;
public boolean myTurn = false;

GameHost host = null;

//===================================================================
public void imageClicked(AniImage which,Point where)
//===================================================================
{
	if (!myTurn) return;
	if (which instanceof Piece){
		Piece p = (Piece)which;
		if (p.myType == p.Empty){
			played(playerType,p.piecePoint);
			if (host != null) host.play(this,p.piecePoint);
			if (selfPlay)
				playerType = playerType == p.Nought ? p.Cross : p.Nought;
		}
	}
}

//===================================================================
public void run(int type)
//===================================================================
{
	playerType = type;
	Form f = new Form();
	f.addLast(getScrollablePanel());
	if (type == Nought) {
		f.title = "Noughts";
		f.exitSystemOnClose = true;
		myTurn = true;
	}
	else if (type == Cross) {
		f.title = "Crosses";
		f.exitSystemOnClose = true;
		myTurn = false;
	}else{
		f.title = "Observer";
		f.exitSystemOnClose = true;
		myTurn = false;
	}
	f.show();
}
//===================================================================
public static void main(String args[])
//===================================================================
{
	ewe.sys.Vm.startEwe(args);
	LocalGameHost lch = new LocalGameHost();
	for (int i = 0; i<4; i++){
		Form f = new Form();
		Board b = new Board();
		b.setupBoard(4,200);
		f.addLast(b.getScrollablePanel());
		if (i == 0){
			f.exitSystemOnClose = true;
			f.title = "Nought";
			b.playerType = Nought;
			b.myTurn = true;
		}else if (i == 2){
			f.exitSystemOnClose = true;
			f.title = "Cross";
			b.playerType = Cross;
			b.myTurn = false;
		}else{
			f.exitSystemOnClose = false;
			f.title = "Observer";
			b.playerType = Observer;
			b.myTurn = false;
		}
		lch.addClient(b,b.playerType);
		b.host = lch;
		f.show();
	}
	//ewe.sys.Vm.exit(0);
}
//##################################################################
}
//##################################################################
