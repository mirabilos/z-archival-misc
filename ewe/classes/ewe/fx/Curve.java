package ewe.fx;

/**
* This class provides methods for calculating points and bounds for
* linear, quadratic and cubic curves.<p>
A Curve object is stateless but the methods are not static so that a
better implementation can be created and substituted for the static variable
"curve" if necessary.
**/
//##################################################################
public class Curve{
//##################################################################


public static Curve curve = new Curve();

static double [] quads, cubes;

public static final int MAX_POINTS = 100;

private static boolean hasNative = true;

//-------------------------------------------------------------------
private static native void nativeCalculatePoints(double [] quads, double [] cubes, int maxPoints);
private static native int nativeCalculateCurves(double [] factors, float [] points, Object destX, Object destY, int offset, int options, int numPoints);
//-------------------------------------------------------------------

//-------------------------------------------------------------------
private static void calculatePoints()
//-------------------------------------------------------------------
{
	quads = new double[3*MAX_POINTS];
	cubes = new double[4*MAX_POINTS];
	if (hasNative) try{
		nativeCalculatePoints(quads,cubes,MAX_POINTS);
		return;
	}catch(UnsatisfiedLinkError e){
		hasNative = false;
	}catch(SecurityException se){
		hasNative = false;
	}
	double dp = (double)1.0/(double)MAX_POINTS;
	double t = 0.0;
	for (int i = 0; i<MAX_POINTS; i++){
		quads[i*3] = Math.pow(1.0-t,2);
		quads[i*3+1] = 2*t*(1.0-t);
		quads[i*3+2] = t*t;
		t += dp;
	}
	dp = (double)1.0/(double)MAX_POINTS;
	t = 0.0;
	for (int i = 0; i<MAX_POINTS; i++){
		cubes[i*4] = 	(1.0-t)*(1.0-t)*(1.0-t);
		cubes[i*4+1] = 3*t*(1.0-t)*(1.0-t);
		cubes[i*4+2] = 3*(1.0-t)*t*t;
		cubes[i*4+3] = t*t*t;
		t += dp;
	}
}
/**
* This calculates the points on a quad curve, for either the x or y co-ordinate. This is called
* by the other calculateQuads method. If the destination object is null this should return the
* number of points in the curve.<p>
* This method does NOT include the last point on the curve, but DOES include the first one.
**/
//-------------------------------------------------------------------
protected int calculateLines(float startx,float starty,float endx,float endy,Object destx,Object desty,int offset,int options)
//-------------------------------------------------------------------
{
	int [] dxi = destx instanceof int [] ? (int [])destx : null;
	float [] dxf = destx instanceof float [] ? (float [])destx : null;
	int [] dyi = desty instanceof int [] ? (int [])desty : null;
	float [] dyf = desty instanceof float [] ? (float [])desty : null;

	int numPoints = 1;

	if (dxi == null && dxf == null) return numPoints;

	if (dxi != null) dxi[offset] = (int)startx;
	else dxf[offset] = (float)startx;

	if (dyi != null) dyi[offset] = (int)starty;
	else dyf[offset] = (float)starty;
	offset++;
	return numPoints;
}

private static float [] pars = new float[8];
/**
* This calculates the points on a quad curve, for either the x or y co-ordinate. This is called
* by the other calculateQuads method. If the destination object is null this should return the
* number of points in the curve.<p>
* This method does NOT include the last point on the curve, but DOES include the first one.
**/
//-------------------------------------------------------------------
protected int calculateQuads(float startx,float starty,float controlx,float controly,float endx,float endy,Object destx,Object desty,int offset,int options)
//-------------------------------------------------------------------
{
	if (quads == null) calculatePoints();
	int numPoints = 20;

	if (hasNative) try{
		pars[0] = startx; pars[1] = starty;
		pars[2] = controlx; pars[3] = controly;
		pars[4] = endx; pars[5] = endy;
		if (destx instanceof int []) options |= 0x80000000;
		return nativeCalculateCurves(quads,pars,destx,desty,offset,options,numPoints);
	}catch(UnsatisfiedLinkError e){
		hasNative = false;
	}catch(SecurityException se){
		hasNative = false;
	}

	int [] dxi = destx instanceof int [] ? (int [])destx : null;
	float [] dxf = destx instanceof float [] ? (float [])destx : null;
	int [] dyi = desty instanceof int [] ? (int [])desty : null;
	float [] dyf = desty instanceof float [] ? (float [])desty : null;


	if (dxi == null && dxf == null) return numPoints;

	int t = 0;
	int dt = ((quads.length/3)/numPoints)*3;
	int skipped = 0;
	double sx = startx, ex = endx, c1x = controlx;
	double sy = starty, ey = endy, c1y = controly;

	for (int i = 0; i<numPoints; i++){
		double x = (quads[t]*sx+quads[t+1]*c1x+quads[t+2]*ex);
		if (dxi != null) dxi[offset] = (int)x;
		else dxf[offset] = (float)x;

		double y = (quads[t]*sy+quads[t+1]*c1y+quads[t+2]*ey);
		if (dyi != null) dyi[offset] = (int)y;
		else dyf[offset] = (float)y;


		if (offset != 0 && dxi != null){
			if (dxi[offset-1] == dxi[offset] && dyi[offset-1] == dyi[offset]){
				offset--;
				skipped++;
			}else{
				if (offset > 1){
					if (dxi[offset-1] == dxi[offset] && dxi[offset-2] == dxi[offset]){
						offset--;
						skipped++;
						dyi[offset] = dyi[offset+1];
					}
					else if (dyi[offset-1] == dyi[offset] && dyi[offset-2] == dyi[offset]){
						offset--;
						skipped++;
						dxi[offset] = dxi[offset+1];
						/*
					}else if ((dxi[offset]-dxi[offset-1] == dxi[offset-1]-dxi[offset-2])
					 && (dyi[offset]-dyi[offset-1] == dyi[offset-1]-dyi[offset-2]))
						{
						offset--;
						skipped++;
						dyi[offset] = dyi[offset+1];
						*/
					}
				}
			}
		}

		offset++;
		t += dt;

	}
	return numPoints-skipped;
}
//-------------------------------------------------------------------
protected int calculateCubes(float startx,float starty,float control1x,float control1y,float control2x,float control2y,float endx,float endy,Object destx,Object desty,int offset,int options)
//-------------------------------------------------------------------
{
	if (quads == null) calculatePoints();
	int numPoints = 20;
	if (hasNative) try{
		pars[0] = startx; pars[1] = starty;
		pars[2] = control1x; pars[3] = control1y;
		pars[4] = control2x; pars[5] = control2y;
		pars[6] = endx; pars[7] = endy;
		if (destx instanceof int []) options |= 0x80000000;
		options |= 0x40000000;
		return nativeCalculateCurves(cubes,pars,destx,desty,offset,options,numPoints);
	}catch(UnsatisfiedLinkError e){
		hasNative = false;
	}catch(SecurityException se){
		hasNative = false;
	}


	int [] dxi = destx instanceof int [] ? (int [])destx : null;
	float [] dxf = destx instanceof float [] ? (float [])destx : null;
	int [] dyi = desty instanceof int [] ? (int [])desty : null;
	float [] dyf = desty instanceof float [] ? (float [])desty : null;

	if (dxi == null && dxf == null) return numPoints;

	int t = 0;
	int dt = ((cubes.length/4)/numPoints)*4;
	double sx = startx, ex = endx, c1x = control1x, c2x = control2x;
	double sy = starty, ey = endy, c1y = control1y, c2y = control2y;
	int skipped = 0;
	for (int i = 0; i<numPoints; i++){
		double x = (cubes[t]*sx+cubes[t+1]*c1x+cubes[t+2]*c2x+cubes[t+3]*ex);
		if (dxi != null) dxi[offset] = (int)x;
		else dxf[offset] = (float)x;
		double y = (cubes[t]*sy+cubes[t+1]*c1y+cubes[t+2]*c2y+cubes[t+3]*ey);
		if (dyi != null) dyi[offset] = (int)y;
		else dyf[offset] = (float)y;

		if (offset > 0 && dxi != null){
			if (dxi[offset-1] == dxi[offset] && dyi[offset-1] == dyi[offset]){
				offset--;
				skipped++;
			}else{
				if (offset > 1){
					if (dxi[offset-1] == dxi[offset] && dxi[offset-2] == dxi[offset]){
						offset--;
						skipped++;
						dyi[offset] = dyi[offset+1];
					}
					else if (dyi[offset-1] == dyi[offset] && dyi[offset-2] == dyi[offset]){
						offset--;
						skipped++;
						dxi[offset] = dxi[offset+1];
						/*
					}else if ((dxi[offset]-dxi[offset-1] == dxi[offset-1]-dxi[offset-2])
					 && (dyi[offset]-dyi[offset-1] == dyi[offset-1]-dyi[offset-2]))
						{
						offset--;
						skipped++;
						dyi[offset] = dyi[offset+1];
						*/
					}

				}
			}
		}
		offset++;
		t += dt;
	}
	return numPoints-skipped;
}
/**
 * Calculate the line points for the line. This is trivial, since it is only the first point
 * that is ever stored.
 * @param xpoints The X coordinates for the curve.
	The first one should be the start point, the second should be the end point.
 * @param ypoints The Y coordinates for the curve.
	The first one should be the start point, the second should be the end point.
	one should be the end point.
 * @param pointsOffset The offset of the points in the xpoints and ypoints array.
 * @param destX Either an int[] or float[] object to hold the destination X points. Can be
 * null if you only are interested in the number of points that will be calculated.
 * @param destY Either an int[] or float[] object to hold the destination Y points. Can be
 * null if you only are interested in the number of points that will be calculated.
 * @param offset The offset into the destX and destY arrays for the points to go.
 * @param options No options are currently defined.
 * @return The number of points that were, or will be put in the destinations.
 */
//===================================================================
public int calculateLines(float[] xpoints, float[] ypoints, int pointsOffset, Object destX, Object destY, int offset, int options)
//===================================================================
{
	return calculateLines(xpoints[pointsOffset], ypoints[pointsOffset], xpoints[pointsOffset+1], ypoints[pointsOffset+1],  destX, destY, offset, options);
}
/**
 * Calculate the curve points for the curve.
 * @param xpoints The X coordinates for the curve.
	The first one should be the start point, the second should be the control point and the third
	one should be the end point.
 * @param ypoints The Y coordinates for the curve.
	The first one should be the start point, the second should be the control point and the third
	one should be the end point.
 * @param pointsOffset The offset of the points in the xpoints and ypoints array.
 * @param destX Either an int[] or float[] object to hold the destination X points. Can be
 * null if you only are interested in the number of points that will be calculated.
 * @param destY Either an int[] or float[] object to hold the destination Y points. Can be
 * null if you only are interested in the number of points that will be calculated.
 * @param offset The offset into the destX and destY arrays for the points to go.
 * @param options No options are currently defined.
 * @return The number of points that were, or will be put in the destinations.
 */
//===================================================================
public int calculateQuads(float[] xpoints, float[] ypoints, int pointsOffset, Object destX, Object destY, int offset, int options)
//===================================================================
{
	return calculateQuads(xpoints[pointsOffset],ypoints[pointsOffset], xpoints[pointsOffset+1],ypoints[pointsOffset+1], xpoints[pointsOffset+2], ypoints[pointsOffset+2], destX, destY, offset, options);
}
/**
 * Calculate the curve points for the curve.
 * @param xpoints The X coordinates for the curve.
	The first one should be the start point, the second should be the control point and the third
	one should be the end point.
 * @param ypoints The Y coordinates for the curve.
	The first one should be the start point, the second should be the control point and the third
	one should be the end point.
 * @param pointsOffset The offset of the points in the xpoints and ypoints array.
 * @param destX Either an int[] or float[] object to hold the destination X points. Can be
 * null if you only are interested in the number of points that will be calculated.
 * @param destY Either an int[] or float[] object to hold the destination Y points. Can be
 * null if you only are interested in the number of points that will be calculated.
 * @param offset The offset into the destX and destY arrays for the points to go.
 * @param options No options are currently defined.
 * @return The number of points that were, or will be put in the destinations.
 */
//===================================================================
public int calculateCubes(float[] xpoints, float[] ypoints, int pointsOffset, Object destX, Object destY, int offset, int options)
//===================================================================
{
	return calculateCubes(xpoints[pointsOffset],ypoints[pointsOffset],xpoints[pointsOffset+1],ypoints[pointsOffset+1], xpoints[pointsOffset+2],ypoints[pointsOffset+2], xpoints[pointsOffset+3],ypoints[pointsOffset+3], destX, destY, offset, options);
}
/*
//===================================================================
static void drawQuads(Graphics g,Point start, Point end, Point control, int [] x, int [] y, int options)
//===================================================================
{
	if (x == null) x = new int[100];
	if (y == null) y = new int[100];
	int num = calculateQuads(start,end,control,x,y,0,options);
	g.drawLines(x,y,num);
}
//-------------------------------------------------------------------
static int calculateCubes(int start,int end,int control1,int control2,int [] dest,int offset,int options)
//-------------------------------------------------------------------
{
	if (quads == null) calculatePoints();
	int numPoints = 20;
	int t = 0;
	int dt = ((cubes.length/4)/numPoints)*4;
	double s = start, e = end, c1 = control1, c2 = control2;
	for (int i = 0; i<numPoints; i++){
		dest[offset++] = (int)(cubes[t]*s+cubes[t+1]*c1+cubes[t+2]*c2+cubes[t+3]*e);
		t += dt;
	}
	dest[offset++] = end;
	return numPoints+1;
}

//===================================================================
static int calculateCubes(Point start, Point end, Point control1, Point control2, int [] x, int [] y,int offset, int options)
//===================================================================
{
	calculateCubes(start.x, end.x, control1.x, control2.x, x, offset, options);
	return calculateCubes(start.y, end.y, control1.y, control2.y, y, offset, options);
}
*/

//##################################################################
}
//##################################################################

