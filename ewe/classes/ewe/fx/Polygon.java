package ewe.fx;

//##################################################################
public class Polygon implements Area{
//##################################################################

public int [] xPoints;
public int [] yPoints;
public int numPoints;

protected Rect bounds;

//===================================================================
public Polygon(int [] xPoints, int [] yPoints, int numPoints)
//===================================================================
{
	this.xPoints = xPoints;
	this.yPoints = yPoints;
	this.numPoints = numPoints;
}

//===================================================================
public Polygon getCopy()
//===================================================================
{
	int [] xp = new int[numPoints];
	int [] yp = new int[numPoints];
	ewe.sys.Vm.copyArray(xPoints,0,xp,0,numPoints);
	ewe.sys.Vm.copyArray(yPoints,0,yp,0,numPoints);
	return new Polygon(xp,yp,numPoints);
}
//===================================================================
public Rect getRect(Rect dest)
//===================================================================
{
	Rect r = Rect.unNull(dest);
	if (bounds != null) return r.set(bounds);
	bounds = new Rect();

	int [] xx = xPoints;
	int [] yy = yPoints;
	int x = 0, y = 0;
	for (int i = 0; i<numPoints; i++){
		if (i == 0 || xx[i] > x) x = xx[i];
		if (i == 0 || yy[i] > y) y = yy[i];
	}
	int sx = 0, sy = 0;
	for (int i = 0; i<numPoints; i++){
		if (i == 0 || xx[i] < sx) sx = xx[i];
		if (i == 0 || yy[i] < sy) sy = yy[i];
	}
	return r.set(bounds.set(sx,sy,x-sx+1,y-sy+1));
}
private static Rect buff, buff2;

//===================================================================
public boolean isIn(int x,int y)
//===================================================================
{
	buff = getRect(buff);
	if (!buff.isIn(x,y)) return false;
	return isIn(xPoints,yPoints,numPoints,x,y);
}

//-------------------------------------------------------------------
native static boolean nativeIsIn(int [] xPoints, int []yPoints, int numPoints,int x,int y);
//-------------------------------------------------------------------
static boolean isIn(int [] xPoints, int []yPoints, int numPoints,int x,int y)
//-------------------------------------------------------------------
{
	if (hasNative){
		try{
			return nativeIsIn(xPoints,yPoints,numPoints,x,y);
		}catch(UnsatisfiedLinkError e){
			hasNative = false;
		}catch(SecurityException se){
			hasNative = false;
		}
	}
	int [] xx = xPoints;
	int [] yy = yPoints;
	int num = 0;
	for (int i = 0; i<numPoints; i++){
		int x1 = xx[i], y1 = yy[i];
		int x2, y2;
		if (i == numPoints-1) x2 = xx[0];
		else x2 = xx[i+1];
		if (i == numPoints-1) y2 = yy[0];
		else y2 = yy[i+1];

		if ((y1 <= y && y2 > y) || (y2 <= y && y1 > y)){
			if ((y2-y1) == 0) num++;
			else{
				int xi = x1+((y-y1)*(x2-x1))/(y2-y1);
				if (xi >= x) {
					num++;
				}
			}
		}
	}
	return (num & 1) == 1;
}

//===================================================================
static int [] getLineParameters(int i,int [] xPoints, int [] yPoints,int numPoints,int [] p)
//===================================================================
{
		int [] xx = xPoints;
		int [] yy = yPoints;
		int x1 = xx[i], y1 = yy[i];
		int x2, y2;
		if (i == numPoints-1) x2 = xx[0];
		else x2 = xx[i+1];
		if (i == numPoints-1) y2 = yy[0];
		else y2 = yy[i+1];
		if (p == null) p = new int[4];
		p[0] = x1; p[1] = y1;
		p[2] = x2; p[3] = y2;
		return p;
}

static int [] p1, p2;

static boolean hasNative = true;

//-------------------------------------------------------------------
static native boolean nativeIntersects(int [] xPoints, int [] yPoints, int num, int [] xPoints2, int [] yPoints2, int num2);
//-------------------------------------------------------------------

//-------------------------------------------------------------------
static boolean intersects(int [] xPoints, int [] yPoints, int num, int [] xPoints2, int [] yPoints2, int num2)
//-------------------------------------------------------------------
{
	if (hasNative){
		try{
			return nativeIntersects(xPoints,yPoints,num,xPoints2,yPoints2,num2);
		}catch(UnsatisfiedLinkError e){
			hasNative = false;
		}catch(SecurityException se){
			hasNative = false;
		}
	}
	for (int i = 0; i<num; i++)
		if (isIn(xPoints2,yPoints2,num2,xPoints[i],yPoints[i])) return true;
	for (int i = 0; i<num2; i++)
		if (isIn(xPoints,yPoints,num,xPoints2[i],yPoints2[i])) return true;

	for (int i = 0; i<num; i++){
		p1 = getLineParameters(i,xPoints,yPoints,num,p1);
		for (int j = 0; j<num2; j++){
			p2 = getLineParameters(j,xPoints2,yPoints2,num2,p2);
			if (intersects(
				p1[0],p1[1],p1[2],p1[3],
				p2[0],p2[1],p2[2],p2[3]
			)) return true;
		}
	}
	return false;
}
//-------------------------------------------------------------------
static boolean intersects(int xa1, int ya1, int xa2, int ya2, int xb1, int yb1, int xb2, int yb2)
//-------------------------------------------------------------------
{
	if (xa1 == xb1 && ya1 == yb1) return true;
	if (xa1 == xb2 && ya1 == yb2) return true;
	if (xa2 == xb1 && ya2 == yb1) return true;
	if (xa2 == xb2 && ya2 == yb2) return true;

	double ma = (xa1 == xa2) ? 0 : (ya1-ya2)/(double)(xa1-xa2);
	double mb = (xb1 == xb2) ? 0 : (yb1-yb2)/(double)(xb1-xb2);
	double ca = (double)ya1-ma*xa1;
	double cb = (double)yb1-mb*xb1;

	if (xa1 == xa2) {//Vertical
		if (xb1 == xb2) {//Also vertical
			if (xa1 == xb1)
				if (
						(ya1 <= yb1 && yb1 <= ya2) || (ya1 >= yb1 && yb1 >= ya2) ||
					 	(ya1 <= yb2 && yb2 <= ya2) || (ya1 >= yb2 && yb2 >= ya2) ||
						(yb1 <= ya1 && ya1 <= yb2) || (yb1 >= ya1 && ya1 >= yb2) ||
					 	(yb1 <= ya2 && ya2 <= yb2) || (yb1 >= ya2 && ya2 >= yb2)
						)
						return true;
		}else{
			if ((xb1 <= xa1 && xa1 <= xb2) || (xb1 >= xa1 && xa1 >= xb2)){
				double yi = mb*xa1+cb;
				if ((ya1 <= yi && yi <= ya2) || (ya1 >= yi && yi >= ya2))
					return true;
			}
		}
	}else if (xb1 == xb2) {//Vertical
			if ((xa1 <= xb1 && xb1 <= xa2) || (xa1 >= xb1 && xb1 >= xa2)){
				double yi = ma*xb1+ca;
				if ((yb1 <= yi && yi <= yb2) || (yb1 >= yi && yi >= yb2))
					return true;
			}
	}else{
		if (ma == mb){//Parallel
			if (ca == cb){
				if (
						((ya1 <= yb1 && yb1 <= ya2) || (ya1 >= yb1 && yb1 >= ya2) ||
					 	(ya1 <= yb2 && yb2 <= ya2) || (ya1 >= yb2 && yb2 >= ya2) ||
						(yb1 <= ya1 && ya1 <= yb2) || (yb1 >= ya1 && ya1 >= yb2) ||
					 	(yb1 <= ya2 && ya2 <= yb2) || (yb1 >= ya2 && ya2 >= yb2))
							&&
						((xa1 <= xb1 && xb1 <= xa2) || (xa1 >= xb1 && xb1 >= xa2) ||
					 	(xa1 <= xb2 && xb2 <= xa2) || (xa1 >= xb2 && xb2 >= xa2) ||
						(xb1 <= xa1 && xa1 <= xb2) || (xb1 >= xa1 && xa1 >= xb2) ||
					 	(xb1 <= xa2 && xa2 <= xb2) || (xb1 >= xa2 && xa2 >= xb2))
						)
						{
						//ewe.sys.Vm.debug("("+xa1+", "+ya1+")-"+"("+xa2+", "+ya2+")"+"("+xb1+", "+yb1+")-"+"("+xb2+", "+yb2+")");
						//ewe.sys.Vm.debug(xa1+", "+xa2+": "+xb1+", "+xb2);
						return true;
						}
			}
		}else{
			double xi = ((cb-ca)/(ma-mb));
			if ((xa1 <= xi && xi <= xa2) || (xa1 >= xi && xi >= xa2))
				if ((xb1 <= xi && xi <= xb2) || (xb1 >= xi && xi >= xb2)){
					//if ((xa1 <= xi && xi <= xa2) || (xa1 >= xi && xi >= xa2))
						//if ((xb1 <= xi && xi <= xb2) || (xb1 >= xi && xi >= xb2))
							//ewe.sys.Vm.debug(xi+" = "+xa1+", "+xa2+": "+xb1+", "+xb2);
					return true;
				}
		}
	}
	return false;
}
static int [] rx, ry;
static int [] mrx, mry;
//===================================================================
public boolean intersects(Area other)
//===================================================================
{
	buff = getRect(buff);
	buff2 = other.getRect(buff2);
	int yi, xi;
	if (!buff.intersects(buff2)) return false;
	if (other instanceof Polygon){
		Polygon pg = (Polygon)other;
		return intersects(xPoints,yPoints,numPoints,pg.xPoints,pg.yPoints,pg.numPoints);
	}else{
		if (rx == null){
			rx = new int[5];
			ry = new int[5];
			mrx = new int[5];
			mry = new int[5];
		}
		rx[0] = rx[3] = rx[4] = buff2.x;
		rx[1] = rx[2] = buff2.x+buff2.width;
		ry[0] = ry[1] = ry[4] = buff2.y;
		ry[2] = ry[3] = buff2.y+buff2.height;
		return intersects(xPoints,yPoints,numPoints,rx,ry,5);
	}
}

Polygon last = null;
int lastDx;
int lastDy;
/**
* Turn this polygon into a translation of the other polygon, by the specified
* dx and dy values. This is efficient to call multiple times - if the polygon is the
* same as the last one AND dx and dy are the same as the last translation, this polygon
* is unchanged.
* @param other The other polygon.
* @param dx The x translation.
* @param dy The y translation.
* @return this polygon after translation.
*/
//===================================================================
public Polygon translate(Polygon other,int dx,int dy)
//===================================================================
{
	if (last == other && dx == lastDx && dy == lastDy) return this;
	last = other;
	lastDx = dx;
	lastDy = dy;
	numPoints = other.numPoints;
	if (xPoints == null || 	(xPoints.length < numPoints))
		xPoints = new int[numPoints];
	if (yPoints == null || 	(yPoints.length < numPoints))
		yPoints = new int[numPoints];
	for (int i = 0; i<numPoints; i++){
		xPoints[i] = other.xPoints[i]+dx;
		yPoints[i] = other.yPoints[i]+dy;
	}
	bounds = null;
	return this;
}
/**
* Create a new Polygon which is a (possibly) translated version of the original. Note that
* this keeps a reference to the original polygon.
**/
//===================================================================
public Polygon(Polygon other,int dx,int dy)
//===================================================================
{
	translate(other,dx,dy);
}
/**
* Draw the polygon onto a Graphics object. You should set the pen and brush
* first.
**/
//===================================================================
public void draw(Graphics g,int x,int y,boolean fill)
//===================================================================
{
	g.translate(x,y);
	if (fill) g.fillPolygon(xPoints,yPoints,numPoints);
	else g.drawPolygon(xPoints,yPoints,numPoints);
	g.translate(-x,-y);
}
/**
* Convert this Polygon to a black and white Image which fits exactly around the bounding Rect for the polygon.
**/
//===================================================================
public Image toImage()
//===================================================================
{
	Rect r = getRect(null);
	//ewe.sys.Vm.debug(r.toString());
	Image i = new Image(r.width,r.height);
	Graphics g = new Graphics(i);
	g.setColor(Color.White);
	g.fillRect(0,0,r.width,r.height);
	g.setColor(Color.Black);
	draw(g,-r.x,-r.y,true);
	g.free();
	return i;
}
/**
* Convert this Polygon to a Mask which fits exactly around the bounding Rect for the polygon.
**/
//===================================================================
public Mask toMask()
//===================================================================
{
	Image i = toImage();
	Mask m = new Mask(i);
	i.free();
	return m;
}
//##################################################################
}
//##################################################################

