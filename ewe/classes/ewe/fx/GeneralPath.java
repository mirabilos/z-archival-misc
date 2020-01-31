package ewe.fx;

/**
A GeneralPath is used for creating lines by appending sequences of straight lines,
quadratic curves and cubic (Bezier) curves.<p>

After you create the GeneralPath you can convert it to a Polygon for drawing on a
Graphics context, or to a sequence of floating point co-ordinates for other
purposes.<p>

You create the path by starting with an initial moveTo(xstart,ystart) to start at that
point. You then do sequences of lineTo(), quadTo() or curveTo() calls to add segments
to the path. You can call closePath() at the end (optionally) to create a closed path.
**/

//##################################################################
public class GeneralPath{
//##################################################################
/**
* This is an option for getLines().
**/
public static final int GET_LINES_AS_INTS = 0x1;

static final int MOVETO = 1;
static final int LINETO = 2;
static final int QUADTO = 3;
static final int CURVETO = 4;
static final int CLOSE = 5;
//
float [] x;
float [] y;
int [] types;
//
int numTypes, numPoints;



/**
 * Create an empty GeneralPath with a pre-allocated capacity. The capacity of the path
 * will always be expanded as needed as segments are added.
 * @param capacity The initial capacity of the GeneralPath.
 */
//===================================================================
public GeneralPath(int capacity)
//===================================================================
{
	x = new float[capacity];
	y = new float[capacity];
	types = new int[capacity];
}
/**
 * Create an empty GeneralPath with a default pre-allocated capacity. The capacity of the path
 * will always be expanded as needed as segments are added.
 */
//===================================================================
public GeneralPath()
//===================================================================
{
	this(10);
}
/**
* This returns the number of points that has been added to the GeneralPath so far.
**/
//===================================================================
public int countPoints()
//===================================================================
{
	return numPoints;
}
/**
* This returns the array of x co-ordinates of the points added to the GeneralPath.
* This can be used to transform the GeneralPath in some way.
* The number of valid
* points within this array can be determined using countPoints().
**/
//===================================================================
public float [] getXPoints()
//===================================================================
{
	return x;
}
/**
* This returns the array of y co-ordinates of the points added to the GeneralPath.
* This can be used to transform the GeneralPath in some way.
* The number of valid
* points within this array can be determined using countPoints().
**/
//===================================================================
public float [] getYPoints()
//===================================================================
{
	return y;
}
//===================================================================
void makeRoom(int points)
//===================================================================
{
	if (numPoints+points > x.length){
		float [] xx = new float[x.length+10];
		ewe.sys.Vm.copyArray(x,0,xx,0,x.length);
		float [] yy = new float[x.length+10];
		ewe.sys.Vm.copyArray(y,0,yy,0,x.length);
		x = xx;
		y = yy;
	}
	if (numTypes == types.length){
		int [] t = new int[types.length+10];
		ewe.sys.Vm.copyArray(types,0,t,0,types.length);
		types = t;
	}
}
/**
* This method indicates that a line should be drawn from the most recent added point to the most
* recent moveTo() point. This will have no effect if this cannot be done.
**/
//===================================================================
public void closePath()
//===================================================================
{
	makeRoom(1);
	this.x[numPoints] = 100; //Arbitrary, won't be used.
	this.y[numPoints] = 100; //Arbitrary, won't be used.
	numPoints++;
	types[numTypes++] = CLOSE;
}
/**
 * If the last operation in this GeneralPath is a closePath(), this will remove it, thereby
 * opening the path again.
 * @return true if the last operation was a closePath(), false otherwise.
 */
//===================================================================
public boolean openPath()
//===================================================================
{
	if (numTypes == 0 || types[numTypes-1] != CLOSE) return false;
	numPoints--;
	numTypes--;
	return true;
}
/**
* Move the current point to the specified x,y location.
* @param x The x-coordinate of the new point.
* @param y The y-coordinate of the new point.
*/
//===================================================================
public void moveTo(float x, float y)
//===================================================================
{
	makeRoom(1);
	this.x[numPoints] = x;
	this.y[numPoints] = y;
	numPoints++;
	types[numTypes++] = MOVETO;
}
/**
 * Draw a line from the last point to the co-ordinates (x,y).
* @param x The x-coordinate of the end point.
* @param y The y-coordinate of the end point.
*/
//===================================================================
public void lineTo(float x, float y)
//===================================================================
{
	moveTo(x,y);
	types[numTypes-1] = LINETO;
}
/**
 * Draw a quadratic curve from the last point to the co-ordinates (x2,y2) using
 * (x1,y1) as the curve's control point.
* @param x1 The x-coordinate of the control point.
* @param y1 The y-coordinate of the control point.
* @param x2 The x-coordinate of the end point.
* @param y2 The y-coordinate of the end point.
* @return
*/
//===================================================================
public void quadTo(float x1, float y1,float x2,float y2)
//===================================================================
{
	makeRoom(2);
	this.x[numPoints] = x1;
	this.y[numPoints] = y1;
	numPoints++;
	this.x[numPoints] = x2;
	this.y[numPoints] = y2;
	numPoints++;
	types[numTypes++] = QUADTO;
}
/**
 * Draw a Bezier curve from the last point to the co-ordinates (x3,y3) using
 * (x1,y1) and (x2,y2) as the curve's control points.
* @param x1 The x-coordinate of the first control point.
* @param y1 The y-coordinate of the first control point.
* @param x2 The x-coordinate of the second control point.
* @param y2 The y-coordinate of the second control point.
* @param x3 The x-coordinate of the end point.
* @param y3 The y-coordinate of the end point.
 */
//===================================================================
public void curveTo(float x1, float y1,float x2,float y2,float x3,float y3)
//===================================================================
{
	makeRoom(3);
	this.x[numPoints] = x1;
	this.y[numPoints] = y1;
	numPoints++;
	this.x[numPoints] = x2;
	this.y[numPoints] = y2;
	numPoints++;
	this.x[numPoints] = x3;
	this.y[numPoints] = y3;
	numPoints++;
	types[numTypes++] = CURVETO;
}
/**
* Convert the GeneralPath to an sequence of lines that fit the path.
* @param destx The destination for the x-coordinates. This should be an array of int or array of float values and must match the type of desty.
	This can be null to indicate that you want a count of the maximum number of points that will be generated.
* @param desty The destination for the y-coordinates. This should be an array of int or array of float values and must match the type of destx.
	This can be null to indicate that you want a count of the maximum number of points that will be generated.
* @param offset The start point in the destination arrays for the points.
* @param includeLastPoint If this is true then the last point (which will be the same as the
start point for a closed polygon) will be included, otherwise it will be omitted. If you are
going to be using this data in Graphics.drawPolygon() then set this to false, since that method
will automatically close the polygon by drawing back to the first point.
* @param options No options are defined as yet.
* @return The number of points needed or used.
*/
//===================================================================
public int getLines(Object destx, Object desty, int offset, boolean includeLastPoint, int options)
//===================================================================
{
	int numPoints = 0;
	int curP = 0;
	float mx = 0, my = 0;
	boolean hadMove = false;
	for (int i = 0; i<numTypes; i++){
		switch(types[i]){
			case MOVETO:
				mx = x[curP];
				my = y[curP];
				hadMove = true;
				break;
			case LINETO:
				numPoints += Curve.curve.calculateLines(x,y,curP,destx,desty,offset+numPoints,0);
				curP++;
				break;
			case QUADTO:
				numPoints += Curve.curve.calculateQuads(x,y,curP,destx,desty,offset+numPoints,0);
				curP += 2;
				break;
			case CURVETO:
				numPoints += Curve.curve.calculateCubes(x,y,curP,destx,desty,offset+numPoints,0);
				curP += 3;
				break;
			case CLOSE:
				if (hadMove){
					hadMove = false;
					if (destx instanceof int []){
						((int [])destx)[offset+numPoints] = (int)x[curP];
						((int [])desty)[offset+numPoints] = (int)y[curP];
					}else if (destx instanceof float[]){
						((float [])destx)[offset+numPoints] = x[curP];
						((float [])desty)[offset+numPoints] = y[curP];
					}
					curP++;
					x[curP] = mx;
					y[curP] = my;
					numPoints++;
				}else{
					curP++;
					x[curP] = x[curP-1];
					y[curP] = y[curP-1];
				}
				break;
		}
	}
	if (includeLastPoint) {
		if (destx instanceof float[]) ((float[])destx)[offset+numPoints] = x[curP];
		else if (destx instanceof int[]) ((int[])destx)[offset+numPoints] = (int)x[curP];
		if (desty instanceof float[]) ((float[])desty)[offset+numPoints] = y[curP];
		else if (desty instanceof int[]) ((int[])desty)[offset+numPoints] = (int)y[curP];
		numPoints++;
	}
	return numPoints;
}
/**
 * Append another path to this path.
 * @param path The other path to add to this path.
 * @param connect If this is true then a line segment will be placed from the last point
	of this path to the first point of the other path IF the first operation of the other GeneralPath
	is a moveTo().
 */
//===================================================================
public void append(GeneralPath path,boolean connect)
//===================================================================
{
	append(path,connect,false);
}
/**
 * Append another path to this path.
 * @param path The other path to add to this path.
 * @param connect If this is true then a line segment will be placed from the last point
	of this path to the first point of the other path IF the first operation of the other GeneralPath
	is a moveTo().
 * @param relative If this is true then all co-ordinates of the other path will be considered
	relative to the first point of this path.
 */
//===================================================================
public void append(GeneralPath path,boolean connect,boolean relative)
//===================================================================
{
	int curP = 0;
	float dx = 0, dy = 0;
	if (relative && numPoints != 0){
		dx = x[numPoints-1];
		dy = y[numPoints-1];
	}
	for (int i = 0; i<path.numTypes; i++){
		switch(path.types[i]){
			case MOVETO:
				if (i == 0 && numPoints != 0)
					if (types[numTypes-1] != CLOSE)
						if (path.x[curP] == x[numPoints-1] && path.y[curP] == y[numPoints-1]){
							break;
						}
				if (i == 0 && connect && numPoints != 0)
					lineTo(path.x[curP]+dx,path.y[curP]+dy);
				else
					moveTo(path.x[curP]+dx,path.y[curP]+dy);
				break;
			case LINETO:
				lineTo(path.x[curP]+dx,path.y[curP]+dy);
				break;
			case QUADTO:
				quadTo(path.x[curP]+dx,path.y[curP]+dy,path.x[curP+1]+dx,path.y[curP+1]+dy);
				curP++;
				break;
			case CURVETO:
				curveTo(path.x[curP]+dx,path.y[curP]+dy,path.x[curP+1]+dx,path.y[curP+1]+dy,path.x[curP+2]+dx,path.y[curP+2]+dy);
				curP += 2;
				break;
			case CLOSE:
				closePath();
				break;
		}
		curP++;
	}
}
//Object [] myLines;

/**
* Get the bounding Rectangle for the GeneralPath.
* @param dest An array of 4 floats - if it is null a new array will be created and returned. dest[0] will hold the leftmost x value, dest[1] the topmost y value,
* dest[2] the width (distance from the leftmost to the rightmost x value) and dest[3] the height (distance
* from the topmost to the bottommost y value).
* @returns the destination array or a new array.
*/
//===================================================================
public float [] getBoundingRect(float [] dest)
//===================================================================
{
	if (dest == null) dest = new float[4];
	Object [] both = getLines(true,0);
	float [] xx = (float [])both[0];
	float [] yy = (float [])both[1];
	float x = 0, y = 0;
	for (int i = 0; i<xx.length; i++){
		if (i == 0 || xx[i] > x) x = xx[i];
		if (i == 0 || yy[i] > y) y = yy[i];
	}
	float sx = 0, sy = 0;
	for (int i = 0; i<xx.length; i++){
		if (i == 0 || xx[i] < sx) sx = xx[i];
		if (i == 0 || yy[i] < sy) sy = yy[i];
	}
	dest[0] = sx;
	dest[1] = sy;
	dest[2] = x-sx;
	dest[3] = y-sy;
	return dest;
}
private static float [] r;
/**
* Get the bounds of the path as a Rect - which holds only integer values.
**/
//===================================================================
public Rect getBounds(Rect dest)
//===================================================================
{
	r = getBoundingRect(r);
	dest = Rect.unNull(dest);
	dest.x = (int)r[0];
	dest.y = (int)r[1];
	dest.width = (int)(r[2]+r[0])-dest.x+1;
	dest.height = (int)(r[3]+r[1])-dest.y+1;
	return dest;
}
/**
* This returns the lines as integer or floating point values.
* @param includeLastPoint If this is true then the last point (which will be the same as the
start point for a closed polygon) will be included, otherwise it will be omitted. If you are
going to be using this data in Graphics.drawPolygon() then set this to false, since that method
will automatically close the polygon by drawing back to the first point.
* @param options GET_LINES_AS_INTS to return the lines as int values, otherwise it will be returned as floats.
* @return An Object array containing two arrays - the array at index 0 is the array of X co-ordinates,
the array at index 1 is the array of Y co-ordinates.
*/
//===================================================================
public Object [] getLines(boolean includeLastPoint, int options)
//===================================================================
{
	//if (myLines != null) return myLines;
	int pt = getLines(null,null,0,true,0);
	int opt = pt;
	boolean ints = ((options & GET_LINES_AS_INTS) != 0);
	Object xx = ints ? (Object)new int[pt] : (Object)new float[pt];
	Object yy = ints ? (Object)new int[pt] : (Object)new float[pt];
	pt = getLines(xx,yy,0,true,0);
	Object [] both = new Object[2];
	if (opt != pt){
		both[0] = ints ? (Object)new int[pt] : (Object)new float[pt];
		both[1] = ints ? (Object)new int[pt] : (Object)new float[pt];
		ewe.sys.Vm.copyArray(xx,0,both[0],0,pt);
		ewe.sys.Vm.copyArray(yy,0,both[1],0,pt);
	}else{
		both[0] = xx;
		both[1] = yy;
	}
	/*
	if (xx instanceof int []){
		int [] x2 = (int[])xx;
		int [] y2 = (int[])yy;
		for (int i = 1; i<pt; i++){
			if (x2[i] == x2[i-1] && y2[i] == y2[i-1])
		}
	}
	*/
	//ewe.sys.Vm.debug("Points: "+pt);
	return both;
}
/**
* Convert the GeneralPath to a Polygon object - which stores all co-ordinates
* as integer values.
**/
//===================================================================
public Polygon toPolygon()
//===================================================================
{
	Object [] both = getLines(true,GET_LINES_AS_INTS);
	int [] xx = (int [])both[0];
	int [] yy = (int [])both[1];
	return new Polygon(xx,yy,xx.length);
}
/**
* Get a copy of this GeneralPath. You can then manipulate the copy without affecting this
* path.
**/
//===================================================================
public GeneralPath getCopy()
//===================================================================
{
	GeneralPath gp = new GeneralPath();
	gp.x = new float[numPoints]; if (numPoints != 0) ewe.sys.Vm.copyArray(x,0,gp.x,0,numPoints);
	gp.y = new float[numPoints]; if (numPoints != 0) ewe.sys.Vm.copyArray(y,0,gp.y,0,numPoints);
	gp.types = new int[numTypes]; if (numTypes != 0) ewe.sys.Vm.copyArray(types,0,gp.types,0,numTypes);
	gp.numTypes = numTypes;
	gp.numPoints = numPoints;
	return gp;
}
/**
* A Transform operation. For this operation the X and Y co-ordinate parameters specify distances
to translate each point in the x and y plane. The factor parameter is not use.
**/
public static final int TRANSFORM_TRANSLATE = 1;
/**
* A Transform operation. For this operation the X and Y co-ordinate parameters specify the center
point for the rotation. The factor parameter specifies the degrees in which to rotate the path.
**/
public static final int TRANSFORM_ROTATE = 2;
/**
* A Transform operation. For this operation the X co-ordinate parameter specifies the vertical plane about which
* the mirror will be done. The Y co-ordinate and factor parameters are not used.
**/
public static final int TRANSFORM_HORIZONTAL_MIRROR = 3;
/**
* A Transform operation. For this operation the Y co-ordinate parameter specifies the horizontal plane about which
* the mirror will be done. The X co-ordinate and factor parameters are not used.
**/
public static final int TRANSFORM_VERTICAL_MIRROR = 4;
/**
* A Transform operation. For this operation the X co-ordinate parameter specifies the vertical plane about which
* the stretching will be done. The factor parameter specifies the degree to which the path will be stretched. The Y co-ordinate is not used.
**/
public static final int TRANSFORM_HORIZONTAL_STRETCH = 5;
/**
* A Transform operation. For this operation the Y co-ordinate parameter specifies the horizontal plane about which
* the stretching will be done. The factor parameter specifies the degree to which the path will be stretched. The X co-ordinate is not used.
**/
public static final int TRANSFORM_VERTICAL_STRETCH = 6;
/**
* A Transform operation. For this operation the X and Y co-ordinate parameters specify the center point for the scaling. The factor parameter specifies
* how much it will be scaled (this is effectively a horizontal and vertical stretch done to the same factor).
**/
public static final int TRANSFORM_SCALE = 7;

private static boolean hasNative = true;

//-------------------------------------------------------------------
//native private static void nativeTransform(int op, int numPoints, float [] sx, float [] sy, float x, float y, double factor, float [] destX, float [] destY);
//-------------------------------------------------------------------

//-------------------------------------------------------------------
private static void transform(int op, int numPoints, float [] sx, float [] sy, float x, float y, double factor, float [] destX, float [] destY)
//-------------------------------------------------------------------
{
	if (op == TRANSFORM_ROTATE) factor = -(factor*Math.PI)/180;
	/*
	if (hasNative) try{
		nativeTransform(op,numPoints,sx,sy,x,y,factor,destX,destY);
		return;
	}catch(UnsatisfiedLinkError e){
		hasNative = false;
	}
	*/
	int i;
	switch(op){
		case TRANSFORM_ROTATE:{
			double s = Math.sin(factor);
			double c = Math.cos(factor);
			for (i = 0; i<numPoints; i++){
				destX[i] = (float)(((sx[i]-x)*c+(sy[i]-y)*s)+x);
				destY[i] = (float)((-(sx[i]-x)*s+(sy[i]-y)*c)+y);
			}
			break;
		}
		case TRANSFORM_TRANSLATE:{
			for (i = 0; i<numPoints; i++){
				destX[i] = sx[i]+x;
				destY[i] = sy[i]+y;
			}
			break;
		}
		case TRANSFORM_HORIZONTAL_MIRROR:{
			for (i = 0; i<numPoints; i++){
				destX[i] = 2*x-sx[i];
			}
			break;
		}
		case TRANSFORM_VERTICAL_MIRROR:{
			for (i = 0; i<numPoints; i++){
				destY[i] = 2*y-sy[i];
			}
			break;
		}
		case TRANSFORM_VERTICAL_STRETCH:{
			for (i = 0; i<numPoints; i++){
				destY[i] = (float)(y+(sy[i]-y)*factor);
			}
			break;
		}
		case TRANSFORM_HORIZONTAL_STRETCH:{
			for (i = 0; i<numPoints; i++){
				destX[i] = (float)(x+(sx[i]-x)*factor);
			}
			break;
		}
		case TRANSFORM_SCALE:{
			for (i = 0; i<numPoints; i++){
				destX[i] = (float)(x+(sx[i]-x)*factor);
				destY[i] = (float)(y+(sy[i]-y)*factor);
			}
			break;
		}
	}
}


private static float[] xs;
private static float[] ys;
private static int[] ts;

//-------------------------------------------------------------------
private void makeTemps()
//-------------------------------------------------------------------
{
	if (xs == null || xs.length < numPoints){
		xs = new float[numPoints];
		ys = new float[numPoints];
	}
	if (ts == null || ts.length < numTypes){
		ts = new int[numTypes];
	}
}
//-------------------------------------------------------------------
private void fromTemps(boolean andTypes)
//-------------------------------------------------------------------
{
	ewe.sys.Vm.copyArray(xs,0,x,0,numPoints);
	ewe.sys.Vm.copyArray(ys,0,y,0,numPoints);
	if (andTypes)
		ewe.sys.Vm.copyArray(ts,0,types,0,numTypes);
}
/**
* This reverses the sub-path direction. It should only be used for open segments
* that start with a moveTo(). The path will also start with a moveTo() to the last
* end point and ignore the first operation (which should be a moveTo()).
* @return This GeneralPath with its points reversed.
*/
//===================================================================
public GeneralPath reverse()
//===================================================================
{
	makeTemps();
	int sp = numPoints-1, dp = 0;
	for (int i = 0; i<numTypes; i++){
		xs[dp] = x[sp];
		ys[dp++] = y[sp--];
		if (i == 0){
			ts[i] = MOVETO;
		}else{
			ts[i] = types[numTypes-i];
			int num = 0;
			if (ts[i] == QUADTO) num = 1;
			else if (ts[i] == CURVETO) num = 2;
			for (int j = 0; j<num; j++){
				xs[dp] = x[sp];
				ys[dp++] = y[sp--];
			}
		}
	}
	fromTemps(true);
	return this;
}
/**
 * @param transformOperation The selected operation - should be one of the TRANSFORM_XXX values.
 * @param x The x-coordinate to be used by the transform. Its use depends on the transform selected.
 * @param y The y-coordinate to be used by the transform. Its use depends on the transform selected.
 * @param factor A value representing some paramter used for the transform. For example for TRANSFORM_ROTATE
	it will be the number of degrees to rotate the path.
 * @return This GeneralPath after transformation.
 */
//===================================================================
public GeneralPath transform(int transformOperation,float x,float y,double factor)
//===================================================================
{
	makeTemps();
	transform(transformOperation,numPoints,this.x,this.y,x,y,factor,xs,ys);
	fromTemps(false);
	return this;
}
/**
This does an Affine Transform on the GeneralPath to produce a new GeneralPath. All the other
predefined transforms are also affine transforms and can actually be done though this method
as well.
<p>
For each x and y point in the GeneralPath, the corresponding x' and y' points in the new GeneralPath
will be given by:

<pre>
x' = m00*x + m01*y + m02;
y' = m10*x + m11*y + m12;
</pre>

@returns this GeneralPath
*/
//===================================================================
public GeneralPath transform(double m00,double m10,double m01, double m11, double m02, double m12)
//===================================================================
{
	for (int i = 0; i<numPoints; i++){
		double xx = m00*x[i]+m01*y[i]+m02;
		double yy = m10*x[i]+m11*y[i]+m12;
		x[i] = (float)xx;
		y[i] = (float)yy;
	}
	return this;
}
/*
//===================================================================
public void drawLines(Graphics g,int x,int y)
//===================================================================
{
	Object [] both = getLines(true,0);
	g.translate(x,y);
	int [] xx = (int [])both[0];
	int [] yy = (int [])both[1];

	g.drawLines(xx,yy,xx.length);
	g.translate(-x,-y);
}

//===================================================================
public void drawPolygon(Graphics g,int x,int y,boolean fill)
//===================================================================
{
	Object [] both = getLines(false,0);
	g.translate(x,y);
	int [] xx = (int [])both[0];
	int [] yy = (int [])both[1];

	if (fill) g.fillPolygon(xx,yy,xx.length);
	else g.drawPolygon(xx,yy,xx.length);

	g.translate(-x,-y);
}
//===================================================================
public boolean isIn(int x,int y)
//===================================================================
{
	Object [] both = getLines(true,0);
	int [] xx = (int [])both[0];
	int [] yy = (int [])both[1];
	int num = 0;
	for (int i = 0; i<xx.length; i++){
		int x1 = xx[i], y1 = yy[i];
		int x2, y2;
		if (i == xx.length-1) x2 = xx[0];
		else x2 = xx[i+1];
		if (i == yy.length-1) y2 = yy[0];
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
*/
//##################################################################
}
//##################################################################


