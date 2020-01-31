/****************************************************************

Sample code for Ewe Application Development.

Updated March 2003, for 1.26 release.

****************************************************************/

package samples.paint;
import ewe.fx.Color;
import ewe.fx.Graphics;
import ewe.fx.IImage;
import ewe.fx.Image;
import ewe.fx.Pen;
import ewe.fx.Point;
import ewe.fx.Rect;
import ewe.ui.Canvas;
import ewe.ui.DragContext;
import ewe.util.Vector;

//##################################################################
public class PaintCanvas extends Canvas{
//##################################################################

{
	modify(WantDrag,0);
	dragTime = 0;
}

public Vector undos = new Vector();

public IImage currentImage;
public Graphics currentGraphics;

Graphics onScreen;

PaintForm paintForm;

public static int maxUndos = 3;

public boolean changed = false;

//===================================================================
public void clearUndo()
//===================================================================
{
	while(true){
		IImage prev = (IImage)Vector.pop(undos);
		if (prev == null) return;
		prev.free();
	}
}
//===================================================================
public void saveUndo()
//===================================================================
{
	if (currentImage == null) return;
	while (undos.size() >= maxUndos){
		IImage i = (IImage)undos.get(undos.size()-1);
		i.free();
		undos.remove(i);
	}
	Vector.push(undos,new Image((Image)currentImage,0));
}
//===================================================================
public void undo()
//===================================================================
{
	lastOp = 0;
	IImage prev = (IImage)Vector.pop(undos);
	if (prev != null){
		paintBackground(currentGraphics,currentImage.getWidth(),currentImage.getHeight());
		prev.draw(currentGraphics,0,0,0);
		repaintNow();
	}
}
//===================================================================
public PaintCanvas(PaintForm form)
//===================================================================
{
	paintForm = form;
}
//===================================================================
public void paintBackground(Graphics g,int w,int h)
//===================================================================
{
	g.setColor(Color.White);
	g.fillRect(0,0,w,h);
}

//===================================================================
public void updateGraphics(Graphics g)
//===================================================================
{
	g.setPen(new Pen(paintForm.getChosenColor(),Pen.SOLID,paintForm.getPenThickness()));
}
//===================================================================
public void setImageSize(int w,int h,Image newImage)
//===================================================================
{
	if (currentImage != null){
		if (w < currentImage.getWidth()) w = currentImage.getWidth();
		if (h < currentImage.getHeight()) h = currentImage.getHeight();
	}
	if (w < 1 || h < 1) return;
	Image ni = new Image(w,h,Image.RGB_IMAGE);
	Graphics g = new Graphics(ni);
	paintBackground(g,w,h);
	if (newImage != null) g.drawImage(newImage,0,0);

	if (currentImage != null) {
		if (newImage == null) currentImage.draw(g,0,0,0);
		currentImage.free();
		currentGraphics.free();
		if (onScreen != null) {
			onScreen.free();
			onScreen = null;
		}
	}
	currentImage = ni;
	currentGraphics = g;
	updateGraphics(currentGraphics);
	clearUndo();
}
//===================================================================
public void setImage(Image newImage)
//===================================================================
{
	changed = false;
	setImageSize(newImage.getWidth(),newImage.getHeight(),newImage);
	repaintNow();
}
//===================================================================
public void newImage()
//===================================================================
{
	paintBackground(currentGraphics,width,height);
	changed = false;
	repaintNow();
}
//===================================================================
public void resizeTo(int w,int h)
//===================================================================
{
	super.resizeTo(w,h);
	setImageSize(w,h,null);
}
//===================================================================
public void shown()
//===================================================================
{
	setImageSize(width,height,null);
	super.shown();
}
//===================================================================
public void doPaint(Graphics g,Rect where)
//===================================================================
{
	if (currentImage != null){
		currentImage.draw(g,0,0,0);
	}
}

public Color penColor = Color.Black;

//===================================================================
public void startDragging(DragContext dc)
//===================================================================
{
	changed = true;
	saveUndo();
	lastOp = DRAW;
	updateGraphics(currentGraphics);
	dc.prevPoint = new Point(dc.start.x,dc.start.y);
	dragged(dc);
}
Rect repaintRect = new Rect();


private int lastOp = 0;
private final int DOT = 1;
private final int DRAW = 2;
//===================================================================
public void penClicked(Point p)
//===================================================================
{
	changed = true;
	if (lastOp != DOT) saveUndo();
	lastOp = DOT;
	updateGraphics(currentGraphics);
	int pt = paintForm.getPenThickness();
	currentGraphics.fillEllipse(p.x-pt/2,p.y-pt/2,pt,pt);
	repaintNow(null,repaintRect.set(p.x-pt/2-2,p.y-pt/2-2,pt+4,pt+4));
}
//===================================================================
public void dragged(DragContext dc)
//===================================================================
{
	if (onScreen == null) onScreen = getGraphics();
	//currentGraphics.setDrawOp(currentGraphics.DRAW_XOR);
	currentGraphics.drawLine(dc.prevPoint.x,dc.prevPoint.y,dc.curPoint.x,dc.curPoint.y);
	repaintRect.setCorners(dc.prevPoint.x,dc.prevPoint.y,dc.curPoint.x,dc.curPoint.y);
	int penThickness = paintForm.getPenThickness();
	repaintRect.x -= penThickness; repaintRect.y -= penThickness;
	repaintRect.width += penThickness*2; repaintRect.height += penThickness*2;
	//repaintNow(null,repaintRect);
	Rect r = onScreen.reduceClip(repaintRect);
	doPaint(onScreen,repaintRect);
	onScreen.flush();
	onScreen.restoreClip(r);
	onScreen.free();
	onScreen = null;
	//dc.prevPoint.set(dc.curPoint);
}
//##################################################################
}
//##################################################################
