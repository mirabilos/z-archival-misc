package ewe.graphics;
import ewe.fx.*;

//##################################################################
public class PolygonImage extends AniImage{
//##################################################################

/**
* If this is true then the image will be drawn filled. This is only applied
* if the image is considered a polygon.
**/
public boolean fill = false;

public Pen pen;
public Brush brush;

Polygon polygon;

//===================================================================
public PolygonImage(Polygon polygon)
//===================================================================
{
	this(polygon,new Pen(Color.Black,Pen.SOLID,1),new Brush(Color.Black,Brush.SOLID));
}
//===================================================================
public PolygonImage(Polygon polygon,Pen pen,Brush brush)
//===================================================================
{
	this.polygon = polygon;
	this.pen = pen;
	this.brush = brush;
	hotPolygon = polygon;
	Rect r = polygon.getRect(null);
	if (r.x < 0){
		int [] x = polygon.xPoints;
		for (int i = 0; i<polygon.numPoints; i++)
			x[i] -= r.x;
		r.x = 0;
	}
	if (r.y < 0){
		int [] y = polygon.yPoints;
		for (int i = 0; i<polygon.numPoints; i++)
			y[i] -= r.y;
		r.y = 0;
	}
	location.width = r.x+r.width;
	location.height = r.y+r.height;
}

//===================================================================
public void doDraw(Graphics g,int options)
//===================================================================
{
	if (pen != null) g.setPen(pen);
	if (brush != null){
		g.setBrush(brush);
		g.fillPolygon(polygon.xPoints,polygon.yPoints,polygon.numPoints);
	}else
		g.drawPolygon(polygon.xPoints,polygon.yPoints,polygon.numPoints);
	Rect r = polygon.getRect(null);
	//g.drawRect(0,0,location.width,location.height);
	//g.drawRect(r.x < 0 ? 0 : r.x,r.y < 0 ? 0 : r.y,r.width,r.height);
}
/**
* Returns if the point is on the hot area of the image.
*/
/*
public boolean
//==============================================================
	onHotArea(int x,int y)
//==============================================================
{
	if ((properties & IsNotHot) != 0) return false;
	return polygon.isIn(x-location.x,y-location.y);
}
*/

//##################################################################
}
//##################################################################

