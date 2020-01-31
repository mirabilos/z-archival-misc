import ewe.fx.Color;
import ewe.fx.Graphics;
import ewe.fx.IImage;
import ewe.fx.Image;
import ewe.fx.Pen;
import ewe.fx.PixelBuffer;
import ewe.fx.Point;
import ewe.fx.Rect;
import ewe.fx.mImage;
import ewe.graphics.AniImage;
import ewe.graphics.ImageDragContext;
import ewe.graphics.InteractivePanel;
import ewe.sys.Handle;
import ewe.sys.Task;
import ewe.ui.Form;
import ewe.ui.Gui;
import ewe.ui.IKeys;
import ewe.ui.KeyEvent;
import ewe.ui.MessageBox;
//##################################################################
public class SolitaireBoard extends InteractivePanel {
//##################################################################

int armWidth, boardWidth, board[], piecesLeft;
final static int Illegal = -1, Empty = 0, Occupied = 1;
public final static int PlayMode = 1, ComposeMode = 2;

public int mode = PlayMode;

public static AniImage piece, groove, fadedPiece;

Point cursorPos;
Cursor cursor;

{
	modify(TakesKeyFocus,0);
	autoScrolling = false;
	quickDragging = false;//true;
}

//===================================================================
Cursor makeCursor(IImage piece)
//===================================================================
{
	return new Cursor();
	/*
	PixelBuffer pb = new PixelBuffer(piece.getWidth(), piece.getHeight());
	Graphics g = pb.getDrawingBuffer(null,null,.5);
	g.setPen(new Pen(new Color(0,100,0),Pen.SOLID,2));
	g.drawRect(0,0,piece.getWidth(), piece.getHeight());
	pb.putDrawingBuffer(pb.PUT_SET);
	return new AniImage(pb.toDrawing());
	*/
}
//-------------------------------------------------------------------
void updateCursor()
//-------------------------------------------------------------------
{
	images.moveOnTop(cursor);
	cursor.refresh();
}
//===================================================================
void setCursor(int x, int y)
//===================================================================
{
	if (cursor == null) cursor = makeCursor(piece);
	if (cursorPos == null) cursorPos = new Point();
	Point p = putImageAt(cursor,x,y);
	cursor.move(p.x,p.y);
	cursor.properties &= ~cursor.IsMoveable;
	cursor.properties |= mImage.IsNotHot;
	if (images.find(cursor) == -1) addImage(cursor);
	cursorPos.set(x,y);
	updateCursor();
}
//===================================================================
void animateSetCursor(int x, int y)
//===================================================================
{
	if (cursor == null) setCursor(x,y);
	else{
		images.moveOnTop(cursor);
		Point now = new Point(cursor.location.x, cursor.location.y);
		Point end = putImageAt(cursor,x,y);
		Task t = cursor.animateMoveTo(end.x,end.y,250);
		Handle h = t.startTask();
		try{
			h.waitUntilStopped();
		}catch(Exception e){
		}
		cursorPos.set(x,y);
	}
}
//===================================================================
public boolean imageMovedOn(AniImage image)
//===================================================================
{
	if (image != background) {
		setCursor(ewe.sys.Vm.HAND_CURSOR);
	}
	return super.imageMovedOn(image);
}
//===================================================================
public boolean imageMovedOff(AniImage image)
//===================================================================
{
	if (image != background) {
		if (movingOnTo == null || movingOnTo == background)
			setCursor(0);
	}
	return super.imageMovedOff(image);
}
//==================================================================
boolean isOnBoard(int x,int y)
//==================================================================
{
	if (x < 0 || x >= boardWidth) return false;
	if (y < 0 || y >= boardWidth) return false;
	return true;
}
//==================================================================
int stateOf(int x,int y)
//==================================================================
{
	return board[y*boardWidth+x];
}
//===================================================================
boolean isOnGrooveOrPiece(int x, int y)
//===================================================================
{
	return isOnBoard(x,y) && stateOf(x,y) != Illegal;
}
//==================================================================
void setState(int x,int y,int state)
//==================================================================
{
	board[y*boardWidth+x] = state;
}

//==================================================================
void makeBoard(int aw)
//==================================================================
{
	boolean specialBoard = false; // For testing.
	armWidth = aw;
	boardWidth = armWidth*armWidth;
	piecesLeft = (boardWidth*5)-1;
	board = new int[boardWidth*boardWidth];
	int oc = Occupied, em = Empty;
	for (int i = 0; i<boardWidth*boardWidth; i++) board[i] = Illegal;
	if (specialBoard) oc = Empty;
	for (int i = 0; i<armWidth; i++)
		for (int j = 0; j<boardWidth; j++)
			setState(j,i+armWidth,oc);
	for (int i = 0; i<armWidth; i++)
		for (int j = 0; j<boardWidth; j++)
			setState(i+armWidth,j,oc);
	if (specialBoard){
		setState(3,0,Occupied);
		setState(3,1,Occupied);
		setState(3,5,Occupied);
		setState(3,4,Occupied);
		piecesLeft = 4;
	}
	//
	setState(boardWidth/2,boardWidth/2,em);
}

int pieceWidth;
AniImage background;

//==================================================================
Point putImageAt(AniImage what,int x,int y)
//==================================================================
{
	int sp = (pieceWidth-what.getDim(null).width)/2;
	Point p =  new Point(x*pieceWidth+sp,y*pieceWidth+sp);
	return p;
}
//==================================================================
public void makeBackground()
//==================================================================
{
	int w = pieceWidth*boardWidth;
	Image i = new Image(w,w);
	Graphics g = new Graphics(i);
	g.setColor(getBackground());
	g.fillRect(0,0,w,w);
//	groove.drawMode = g.DRAW_OR;
	for (int x = 0; x<boardWidth; x++)
		for (int y = 0; y<boardWidth; y++)
			if (stateOf(x,y) != Illegal) {
				Point p = putImageAt(groove,x,y);
				groove.draw(g,p.x,p.y,0);
			}
	g.free();
	i.freeze();
	background = new AniImage();
	background.setImage(i);
	background.properties |= background.IsNotHot;
	backgroundImage = background.image;//addImage(background);
}
//==================================================================
public void fillPieces()
//==================================================================
{
	for (int x = 0; x<boardWidth; x++)
		for (int y = 0; y<boardWidth; y++)
			if (stateOf(x,y) == Occupied) {
				Point p = putImageAt(groove,x,y);
				AniImage i = (AniImage)piece.getCopy();
				i.setLocation(p.x,p.y);
				i.properties |= i.IsMoveable;
				addImage(i);
			}
}

//==================================================================
public SolitaireBoard(boolean small)
//==================================================================
{
	try{
	if (!small) {
		piece = new AniImage("images/Marble.png");
		groove = new AniImage("images/Groove.png");
	}else {
		piece = new AniImage("images/SmallMarble.png");
		groove = new AniImage("images/SmallGroove.png");
	}
	PixelBuffer pb = new PixelBuffer(piece);
	pb.scaleAlpha(0.75);
	Image im = pb.toImage();
	im.freeze();
	fadedPiece = new AniImage(im);
	pieceWidth = piece.getDim(null).width;
	//checkId = mApp.requestTimer(this,250);
	}catch(Exception e){ e.printStackTrace();}
}
//===================================================================
public void make(boolean reMake)
//===================================================================
{
	super.make(reMake);
	newBoard();
}
//==================================================================
public void newBoard()
//==================================================================
{
	pauseSnapShots = true;
	images.clear();
	makeBoard(3);
	makeBackground();
	fillPieces();
	reported = false;
	virtualSize = new Rect(0,0,background.location.height,background.location.height);
	takeSnapShot();
	setCursor(3,0);
}

//==================================================================
public void clearBoard()
//==================================================================
{
	for (int i = 0; i<board.length; i++)
		if (board[i] != Illegal) board[i] = Empty;
	images.clear();
	addImage(cursor);
	piecesLeft = 0;
	takeSnapShot();
	refresh();
}
//==================================================================
protected void calculateSizes()
//==================================================================
{
	virtualSize = new Rect(0,0,0,0);
	preferredWidth = preferredHeight = virtualSize.width = virtualSize.height =
	background.getDim(null).width;
}

Point draggingPiece;
//============================================================
public void draggingStarted(ImageDragContext dc)
//============================================================
{
	setCursor(ewe.sys.Vm.INVISIBLE_CURSOR);
	if (cursor != null) {
		cursor.properties |= cursor.IsInvisible;
		cursor.refreshNow();
	}
	showPieces("Dragging started.");
	draggingPiece = null;
	AniImage i = dc.image;
	if (i == null) return;
	i.change(fadedPiece);
	images.moveOnTop(i);
	draggingPiece = whichPiece(i.location.x,i.location.y);
	showPieces("Dragging started again.");
}
//==================================================================
public boolean imageDragged(ImageDragContext dc,Point where)
//==================================================================
{
	showPieces("Dragging...");
	return super.imageDragged(dc,where);
}
//==================================================================
void occupy(AniImage i,Point p)
//==================================================================
{
	setState(p.x,p.y,Occupied);
	p = putImageAt(i,p.x,p.y);
	i.move(p.x,p.y);
}
//==================================================================
void showPieces(String message)
//==================================================================
{
	/*
	System.out.println(message);
	for (int i = 0; i<images.size(); i++){
		AniImage im = (AniImage)images.get(i);
		Point at = whichPiece(im.location.x,im.location.y);
		System.out.println(im+" A: "+im.location.x+", "+im.location.y+" P: "+at.x+", "+at.y);
	}
	*/
}
//==================================================================
AniImage pieceAt(Point p)
//==================================================================
{
	for (int i = 0; i<images.size(); i++){
		AniImage im = (AniImage)images.get(i);
		Point at = whichPiece(im.location.x,im.location.y);
		if (at.x == p.x && at.y == p.y && im != cursor) return im;
	}
	return null;
}
void jumpedOver(Point start, Point over)
{
	setState(start.x,start.y,Empty);
	setState(over.x,over.y,Empty);
	AniImage jumped = pieceAt(over);
	removeImage(jumped);
	refresh(jumped.location,null);
	piecesLeft--;
	if (piecesLeft == 1 && mode == PlayMode)
		new MessageBox("Winner","Congratulations!\nYou won!",Form.MBOK).execute();
	else{
		boolean cantMove = true;
 		for (int x = 0; x<boardWidth && cantMove; x++)
			for (int y = 0; y<boardWidth && cantMove; y++)
				if (canMoveSomewhere(x,y))
					cantMove = false;
		if (cantMove)
			new MessageBox("Game Over","Game Over!",Form.MBOK).execute();
	}
}
//==================================================================
public boolean check(AniImage i,Point p)
//==================================================================
{
	Point s = draggingPiece;
	Point over = new Point(p.x,p.y);
	if (!isOnBoard(p.x,p.y)) return false;
	if (stateOf(p.x,p.y) != Empty) return false;
	int dx = p.x-s.x;
	int dy = p.y-s.y;
	boolean isValid = false;
	if ((dx == 2 || dx == -2) && (dy == 0)) isValid = true;
	if ((dy == 2 || dy == -2) && (dx == 0)) isValid = true;
	if (!isValid) return false;
	over.x = s.x+(dx/2);
	over.y = s.y+(dy/2);
	if (stateOf(over.x,over.y) == Empty) return false;
	occupy(i,p);
	dropImage(i);
	jumpedOver(s,over);
	return true;
}
//============================================================
public void draggingStopped(ImageDragContext dc)
//============================================================
{
	setCursor(ewe.sys.Vm.HAND_CURSOR);
	showPieces("Stopped dragging.");
	try{
		AniImage i = dc.image;
		if (i == null) return;
		i.change(piece);
		int ix = i.location.x, iy = i.location.y;
		int w = i.getDim(null).width;
		if (check(i,whichPiece(ix,iy))) return;
		if (check(i,whichPiece(ix+w,iy))) return;
		if (check(i,whichPiece(ix,iy+w))) return;
		if (check(i,whichPiece(ix+w,iy+w))) return;
		occupy(i,draggingPiece);
	}finally{
		showPieces("Erased piece.");
	}
}
//===================================================================
public Point toBoardLocation(int pieceX, int pieceY)
//===================================================================
{
	return new Point(pieceX*pieceWidth+pieceWidth/2, pieceY*pieceWidth+pieceWidth/2);
}
//==================================================================
public Point whichPiece(int boardX,int boardY)
//==================================================================
{
	int x = boardX/pieceWidth;
	int y = boardY/pieceWidth;
	return new Point(x,y);
}

boolean reported = false;
int checkId;
//==================================================================
public void changeMode(int newMode)
//==================================================================
{
	if (newMode == mode) return;
	piecesLeft = 0;
	for (int i = 0; i<images.size(); i++){
		AniImage im = (AniImage)images.get(i);
		if (im == background || im == cursor) continue;
		im.properties ^= im.IsMoveable;
		piecesLeft++;
	}
	mode = newMode;
	reported = false;
	takeSnapShot();
}

//==================================================================
public boolean imagePressed(AniImage image,Point where)
//==================================================================
{
	if (mode == PlayMode) return super.imagePressed(image,where);
	else {
		if (image != null) {
			removeImage(image);
			Point pc = whichPiece(image.location.x,image.location.y);
			setState(pc.x,pc.y,Empty);
			refresh(image.location,null);
			piecesLeft--;
		}else{
			Point pc = whichPiece(where.x,where.y);
			if (!isOnBoard(pc.x,pc.y)) return true;
			if (stateOf(pc.x,pc.y) == Illegal) return true;
			AniImage i = (AniImage)piece.getCopy();
			occupy(i,pc);
			addImage(i);
			piecesLeft++;
			i.updated();
			refresh(i.location,null);
		}
	}
	return true;
}
/*
//==================================================================
public void ticked(int id,int elapsed)
//==================================================================
{

	if (id != checkId) super.ticked(id,elapsed);
	else{
		if (piecesLeft == 1 && !reported && mode == PlayMode) {
			reported = true;
			//Vm.playSound("\\Program Files\\waba\\Solitaire\\Jungle Error.wav",Vm.SND_ASYNC|Vm.SND_FILENAME);

			MessageBox mb = new MessageBox("Winner","Congratulations!\nYou won!",0);
			//mb.icon = new mImage("Smiley.bmp","SmileyMask.bmp");
			mb.exec(null,null);
		}
	}
}
*/
/*
public void onKeyEvent(KeyEvent ev)
{
	super.onKeyEvent(ev);

	if (ev.type == ev.KEY_RELEASE || ev.type == ev.KEY_PRESS){
		MessageBox mb = new MessageBox("Key!","Key: "+ev.key,0);
		mb.exec(null,null);
	}
}
*/
//===================================================================
boolean tryMoveCursor(int dx, int dy)
//===================================================================
{
	if (cursorPos == null){
		animateSetCursor(3,0);
		return true;
	}else{
		if (!isOnGrooveOrPiece(cursorPos.x+dx,cursorPos.y+dy)) return false;
		animateSetCursor(cursorPos.x+dx,cursorPos.y+dy);
		return true;
	}
}
//===================================================================
boolean isLegalMove(int boardX,int boardY,int dx, int dy)
//===================================================================
{
	int destX = boardX+dx*2, destY = boardY+dy*2;
	if (!isOnBoard(destX,destY)) return false;
	if (stateOf(destX,destY) != Empty) return false;
	if (stateOf(boardX+dx,boardY+dy) != Occupied) return false;
	return true;
}
//===================================================================
boolean canMoveSomewhere(int boardX, int boardY)
//===================================================================
{
	if (!isOnBoard(boardX, boardY)) return false;
	if (stateOf(boardX, boardY) != Occupied) return false;
	if (isLegalMove(boardX,boardY,-1,0)) return true;
	if (isLegalMove(boardX,boardY,1,0)) return true;
	if (isLegalMove(boardX,boardY,0,-1)) return true;
	if (isLegalMove(boardX,boardY,0,1)) return true;
	return false;
}
//===================================================================
int getPossibleMoves(int boardX, int boardY)
//===================================================================
{
	if (!isOnBoard(boardX, boardY)) return 0;
	if (stateOf(boardX, boardY) != Occupied) return 0;
	int ret = 0;
	if (isLegalMove(boardX,boardY,-1,0)) ret |= 1;
	if (isLegalMove(boardX,boardY,1,0)) ret |= 2;
	if (isLegalMove(boardX,boardY,0,-1)) ret |= 4;
	if (isLegalMove(boardX,boardY,0,1)) ret |= 8;
	return ret;
}
//===================================================================
boolean tryMovePiece(int dx, int dy)
//===================================================================
{
	if (!isLegalMove(cursorPos.x,cursorPos.y,dx,dy)){
		Gui.flashMessage("Cannot move to that location.",1000,this,0);
		return false;
	}
	pickedToMove = false;
	int destX = cursorPos.x+dx*2, destY = cursorPos.y+dy*2;
	AniImage got = pieceAt(cursorPos);
	Point dest = putImageAt(got,destX,destY);
	got.change(fadedPiece);
	images.moveOnTop(got);
	if (cursor != null){
		cursor.properties |= cursor.IsInvisible;
		updateCursor();
	}
	Task t = got.animateMoveTo(dest.x,dest.y,250);
	Handle h = t.startTask();
	try{
		h.waitUntilStopped();
	}catch(Exception e){}
	got.change(piece);
	occupy(got,new Point(destX,destY));
	got.refresh();
	Point over = new Point(cursorPos.x+dx, cursorPos.y+dy);
	jumpedOver(cursorPos,over);
	if (cursor != null){
		cursor.type = 0;
		cursor.properties &= ~cursor.IsInvisible;
		cursor.changed();
		setCursor(destX,destY);
	}
	return true;
}
boolean pickedToMove = false;
//===================================================================
public void onKeyEvent(KeyEvent ev)
//===================================================================
{
	if (ev.type == ev.KEY_PRESS){
		/*
		if (ev.key == '`'){
			Menu m = new Menu();
			final Vector ti = new Vector();
			ti.addAll(new String[]{"One","Two","Three and it's pretty long","Four","Five","Six"});
			m.itemList.items = ti;
			//m.exec(this,null,null,false,null);
			m.exec(this,null,new Point(2,10),null);
		}
		*/
		if (pickedToMove == false){
			if (cursor != null) cursor.properties &= ~cursor.IsInvisible;
			if (ev.isActionKey()){
				if (cursorPos == null) setCursor(3,0);
				else if (mode == ComposeMode){
					AniImage toMove = pieceAt(cursorPos);
					imagePressed(toMove,toBoardLocation(cursorPos.x,cursorPos.y));
					return;
				}else if (!canMoveSomewhere(cursorPos.x, cursorPos.y))
					Gui.flashMessage("Cannot move that piece!",1000,this,0);
				else {
					int where = getPossibleMoves(cursorPos.x, cursorPos.y);
					switch(where){
						case 1: tryMovePiece(-1,0); return;
						case 2: tryMovePiece(1,0); return;
						case 4: tryMovePiece(0,-1); return;
						case 8: tryMovePiece(0,1); return;

					default:
						pickedToMove = true;
						AniImage toMove = pieceAt(cursorPos);
						toMove.change(fadedPiece);
						toMove.refresh();
						cursor.type = where;
						cursor.changed();
						updateCursor();
					}
				}
			}else
			switch(ev.key){
				case IKeys.UP: tryMoveCursor(0,-1); return;
				case IKeys.DOWN: tryMoveCursor(0,1); return;
				case IKeys.LEFT: tryMoveCursor(-1,0); return;
				case IKeys.RIGHT: tryMoveCursor(1,0); return;
			}
		}else{ //Have picked to move!
			if (ev.isActionKey()){
				if (cursor != null){
					cursor.type = 0;
					cursor.changed();
					AniImage toMove = pieceAt(cursorPos);
					toMove.change(piece);
					updateCursor();
				}
				pickedToMove = false;
			}else
			switch(ev.key){
				case IKeys.UP: tryMovePiece(0,-1); return;
				case IKeys.DOWN: tryMovePiece(0,1); return;
				case IKeys.LEFT: tryMovePiece(-1,0); return;
				case IKeys.RIGHT: tryMovePiece(1,0); return;
			}
		}
	}else
		super.onKeyEvent(ev);
}
//##################################################################
class Cursor extends AniImage{
//##################################################################

int type = 0;

public Cursor()
{
	location.width = piece.getWidth();
	location.height = piece.getHeight();
	type = 0;
	if (Gui.hasPen) properties |= IsInvisible;
}
Color color = new Color(0,200,0);
Rect t = new Rect();
//===================================================================
public void doDraw(Graphics g, int options)
//===================================================================
{
	int hh = location.height/2-3, hw = location.width/2-3;
	Rect r = g.reduceClip(0,0,location.width,location.height,null);
	if (type == 0){
		g.setPen(new Pen(color,Pen.SOLID,2));
		g.drawRect(0,0,location.width, location.height);
		g.restoreClip(r);
	}else{
		g.setColor(color);
		if ((type & 1) != 0)
			g.drawHorizontalTriangle(t.set(0,(location.height-hh)/2,hw,hh),true);
		if ((type & 2) != 0)
			g.drawHorizontalTriangle(t.set(location.width-hw,(location.height-hh)/2,hw,hh),false);
		if ((type & 4) != 0)
			g.drawVerticalTriangle(t.set((location.height-hh)/2,0,hw,hh),true);
		if ((type & 8) != 0)
			g.drawVerticalTriangle(t.set((location.height-hh)/2,location.height-hh,hw,hh),false);
	}
}

//##################################################################
}
//##################################################################

//##################################################################
}
//##################################################################
