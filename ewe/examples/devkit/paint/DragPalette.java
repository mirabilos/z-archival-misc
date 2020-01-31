/****************************************************************

Sample code for Ewe Application Development.

Updated March 2003, for 1.26 release.

****************************************************************/

package samples.paint;
import ewe.fx.Color;
import ewe.fx.IImage;
import ewe.fx.ImageCache;
import ewe.fx.Point;
import ewe.ui.Canvas;
import ewe.ui.Control;
import ewe.ui.DragContext;
import ewe.ui.Gui;

//##################################################################
public class DragPalette extends Canvas{
//##################################################################

{
	modify(WantDrag,0);
	dragTime = 0;
}

IImage small, big;
public void startDragging(DragContext dc)
{
	small = ImageCache.cache.get("samples/paint/painticon.bmp","samples/paint/painticonmask.bmp");
	big =  ImageCache.cache.get("samples/paint/bigpainticon.bmp",Color.White);
	dc.startImageDrag(small,new Point(8,8),this);
}
public void dragged(DragContext dc)
{
	Point p = Gui.getPosInParent(this,getWindow());
	p.x += dc.curPoint.x;
	p.y += dc.curPoint.y;
	Control c = getWindow().findChild(p.x,p.y);
	if (c instanceof PaintCanvas) dc.imageDrag(big,new Point(16,16));
	else dc.imageDrag(small,new Point(8,8));
}
public void stopDragging(DragContext dc)
{
	dc.stopImageDrag();
}
//##################################################################
}
//##################################################################
