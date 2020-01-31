package samples.tictactoe;
import ewe.graphics.*;
import ewe.fx.*;

//##################################################################
public class Piece extends AniImage implements TTTConstants{
//##################################################################

public int myType;

public static Pen nPen = new Pen(new Color(255,0,0),Pen.SOLID,3);
public static Pen cPen = new Pen(new Color(0,200,0),Pen.SOLID,3);

public Point piecePoint = new Point();

//===================================================================
public Piece(int type)
//===================================================================
{
	myType = type;
}

//===================================================================
public void doDraw(Graphics g,int options)
//===================================================================
{
	if (myType == Cross){
		g.setPen(cPen);
		g.drawLine(2,2,location.width-4,location.height-4);
		g.drawLine(location.width-4,2,2,location.height-4);
	}else if (myType == Nought){
		g.setPen(nPen);
		g.drawEllipse(2,2,location.width-4,location.height-4);
	}
}
//##################################################################
}
//##################################################################
