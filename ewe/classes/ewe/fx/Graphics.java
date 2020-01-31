/* $MirOS: contrib/hosted/ewe/classes/ewe/fx/Graphics.java,v 1.2 2008/05/02 20:52:00 tg Exp $ */

/*********************************************************************************
 *  Ewe Virtual Machine - Version 1.14, January 24, 2002                         *
 *  Copyright (c) 2008 Thorsten “mirabilos” Glaser                               *
 *  Copyright (C) 1999-2002 Michael L Brereton <michael_brereton@ewesoft.com>    *
 *  Copyright (C) 1998, 1999 Wabasoft <www.wabasoft.com>                         *
 *  All Rights Reserved                                                          *
 *                                                                               *
 *  This library and virtual machine is free software; you can redistribute      *
 *  it and/or modify it under the terms of the Amended GNU Lesser General        *
 *  Public License distributed with this software.                               *
 *                                                                               *
 *  Under this license, linking this library or part thereof with other files to *
 *  produce an executable does not in itself require the executable to be        *
 *  covered by the GNU Lesser General Public License.                            *
 *                                                                               *
 *  This library and virtual machine is distributed in the hope that it will     *
 *  be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of    *
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.                         *
 *                                                                               *
 *  You should have received a copy of the License along with this software;     *
 *  if not, please download it at: www.ewesoft.com/LGPL.txt                      *
 *                                                                               *
 *********************************************************************************/
package ewe.fx;


/**
 * Graphics draws on a surface.
 * <p>
 * Surfaces are objects that implement the ISurface interface.
 * MainWindow and Image are both examples of surfaces.
 * <p>
 * Here is an example that uses Graphics to draw a line:
 *
 * <pre>
 * public class MyProgram extends MainWindow
 * {
 * public void onPaint(Graphics g)
 *  {
 *  setColor(0, 0, 255);
 *  g.drawLine(0, 0, 10, 10);
 * ...
 * </pre>
 */

public class Graphics implements ewe.ui.UIConstants
{
ISurface surface;
private Pen pen = new Pen(Color.Black,Pen.SOLID,1);
private Brush brush = new Brush(Color.Black,Brush.SOLID);
public Color color = new Color(0,0,0);
public Color background = new Color(255,255,255);
private static int [] mapped = new int[256*2];
private static Image mapBuf;
private static int [] rgb;
private static int numMapped = 0;
private static int lastMappedIndex = -1;
//===================================================================
public static int mapColor(int value)
//===================================================================
{
	if (mapBuf == null) {
		mapBuf = new Image(2,2);
		rgb = new int[1];
	}

	if (lastMappedIndex != -1) {
		if (mapped[lastMappedIndex*2] == value)
			return mapped[lastMappedIndex*2+1];
	}

	for (int i = 0; i<numMapped; i++){
		if (mapped[i*2] == value) {
			lastMappedIndex = i;
			return mapped[i*2+1];
		}
	}
	rgb[0] = value;
	Graphics g = new Graphics(mapBuf);
	g.setColor(new Color((value >> 16) & 0xff,(value >> 8) & 0xff,value & 0xff));
	g.fillRect(0,0,2,2);
	g.free();
	//mapBuf.setPixels(rgb,0,0,0,1,1,0);
	mapBuf.getPixels(rgb,0,0,0,1,1,0);

	if (numMapped < mapped.length/2) {
		mapped[numMapped*2] = value;
		mapped[numMapped*2+1] = rgb[0];
		lastMappedIndex = numMapped;
		numMapped++;
	}

	return rgb[0];
}

public void setBackground(int r,int g,int b)
{
	background.set(r,g,b);
}
public void setBackground(Color c)
{
	background.set(c);
}

public int getSurfaceType()
{
	if (surface instanceof ewe.ui.Window) return ISurface.WINDOW_SURFACE;
	else if (surface instanceof ewe.fx.Image) return ISurface.IMAGE_SURFACE;
	//else if (surface instanceof ewe.fx.PrinterJob) return ISurface.PRINTERJOB_SURFACE;
	else return 0;
}
public static boolean canAlphaBlend = true;//false;
/**
* This indicates if you can copy from a Window surface. You can always copy from an Image surface and you can never
* copy from a PrinterJob surface.
*/
public static boolean canCopy = false;//true;
public static boolean canMove = false;//true;

static ImageBuffer moveBuffer;
/**
 * This copies a rectangular area in a Graphics to a different location within itself in the most
 * effecient way. It is guaranteed to work, even if the source and destination areas overlap. Note
 * that it will not work if the canMove variable is false.
 * @param x
 * @param y
 * @param width
 * @param height
 * @param dstX
 * @param dstY
 */
public void moveRect(int x, int y, int width, int height, int dstX, int dstY)
{
	if (!canMove) return;
	if (moveBuffer == null) moveBuffer = new ImageBuffer();
	Graphics g = moveBuffer.get(width,height,false);
	g.copyRect(this,x,y,width,height,0,0);
	copyRect(g,0,0,width,height,dstX,dstY);
}

/**
 * The constant for a draw operation that draws the source over
 * the destination.
 */
public static final int DRAW_OVER = 1;

/**
 * The constant for a draw operation that AND's the source with the
 * destination. Commonly used to create image masks.
 */
public static final int DRAW_AND = 2;

/**
 * The constant for a draw operation that OR's the source with the
 * destination. Commonly used with image masks.
 */
public static final int DRAW_OR = 3;

/**
 * The constant for a draw operation that XOR's the source with the
 * destination.
 */
public static final int DRAW_XOR = 4;
/**
 * This is used for drawing images. It explicitly tells the system to
 * use the ALPHA channel when drawing images.
 */
public static final int DRAW_ALPHA = 5;

/**
 * Constructs a graphics object which can be used to draw on the given
 * surface. For the sake of the methods in this class, the given surface
 * is known as the "current surface".
 * <p>
 * If you are trying to create a graphics object for drawing in a subclass
 * of control, use the createGraphics() method in the Control class. It
 * creates a graphics object and translated the origin to the origin of
 * the control.
 */
public Graphics(ISurface surface)
{
	this(surface,false);
}
public Graphics(ISurface surface,boolean forCopying)
	{
	if (!forCopying)
	if (surface instanceof Image)
		if (((Image)surface).wasLoaded)
			throw new IllegalArgumentException("Cannot draw to a loaded image.");
	this.surface = surface;
	_nativeCreate();
	}


private native void _nativeCreate();

/**
 * Clears the current clipping rectangle. This allows drawing to occur
 * anywhere on the current surface.
 */
public native void clearClip();

public boolean isValid() {return true;}

/**
* This returns true if you can do a bitBlt() using this Graphics object as a source.
*/
public boolean canCopyFrom()
{
	int t = getSurfaceType();
	if (t == ISurface.IMAGE_SURFACE) return true;
	if (t != ISurface.WINDOW_SURFACE) return false;
	return canCopy;
}
/**
 * Copies a rectangular area from a surface to the given coordinates on
 * the current surface. The copy operation is performed by combining
 * pixels according to the setting of the current drawing operation.
 * The same surface can be used as a source and destination to
 * implement quick scrolling.
 * <p>
 * Not all combinations of surfaces are supported on all platforms.
 * PalmOS has problems copying from an Window surface to an Image and
 * between two Image surfaces. Java doesn't allow copying from an
 * Window surface to an Image.
 *
 * @param surface the surface to copy from
 * @param x the source x coordinate
 * @param y the source y coordinate
 * @param width the width of the area on the source surface
 * @param height the height of the area on the source surface
 * @param dstX the destination x location on the current surface
 * @param dstY the destination y location on the current surface
 * @see #setDrawOp
 */
public void copyRect(ISurface surface, int x, int y,
	int width, int height, int dstX, int dstY)
{
	Graphics g = new Graphics(surface,true);
	copyGraphics(g,x,y,width,height,dstX,dstY);
	g.free();
}

public boolean copyRect(Graphics source,int x,int y,int width,int height,int destX,int destY)
{
	try{
		if (source.canCopyFrom()){
			copyGraphics(source,x,y,width,height,destX,destY);
			return true;
		}
	}catch(Throwable t){}
	return false;
}
public boolean copyRect(Graphics source,Rect sourceArea,Rect destArea,Mask mask)
{
	//if (destArea.width != sourceArea.width || destArea.height != sourceArea.height) return false;
	int w = sourceArea.width, h = sourceArea.height;
	if (mask != null)
		if (w != mask.getWidth() || h != mask.getHeight())
			return false;
	Image img = new Image(w,h);
	Graphics g = new Graphics(img);
	g.copyRect(source,sourceArea.x,sourceArea.y,w,h,0,0);
	g.free();
	if (mask == null){
		drawImage(img,null,null,new Rect(0,0,w,h),destArea,0);
		img.free();
	}else{
		mImage mi = mask.toMImage(img);
		drawImage(img,mi.mask,null,new Rect(0,0,w,h),destArea,0);
		mi.free();
	}
	return true;
}


private native void copyGraphics(Graphics source,int x,int y,int width,int height,int destX,int destY);


//public native void copyRect(ISurface surface, int x, int y,
//	int width, int height, int dstX, int dstY);


/**
 * Frees any system resources (native device contexts) associated with the
 * graphics object. After calling this function, the graphics object can no
 * longer be used to draw. Calling this method is not required since any
 * system resources allocated will be freed when the object is garbage
 * collected. However, if a program uses many graphics objects, free()
 * should be called whenever one is no longer needed to prevent allocating
 * too many system resources before garbage collection can occur.
 */
public void free()
{
	flush();
	nativeFree();
}

native void nativeFree();



/**
 * Draws text at the given coordinates. The x and y coordinates specify
 * the upper left hand corner of the text's bounding box.
 * @param chars the character array to display
 * @param start the start position in array
 * @param count the number of characters to display
 * @param x the left coordinate of the text's bounding box
 * @param y the top coordinate of the text's bounding box
 */
public native void drawText(char chars[], int start, int count, int x, int y);

private static char [] charBuff = new char[1];
public void drawChar(char which,int x,int y,int options)
{
	charBuff[0] = which;
	drawText(charBuff,0,1,x,y);
}

public void drawFormattedText(String s,int x,int y,FormattedTextSpecs fts)
{
	drawFormattedText(ewe.sys.Vm.getStringChars(s),0,s.length(),x,y,fts);
}
public void drawFormattedText(String s,int start,int length,int x,int y,FormattedTextSpecs fts)
{
	drawFormattedText(ewe.sys.Vm.getStringChars(s),start,length,x,y,fts);
}
public void drawFormattedText(char [] s,int start,int length,int x,int y,FormattedTextSpecs fts)
{
	char [] all = s;
	int [] positions = null;
	if (fts != null)
		positions = fts.calculatedPositions;
	if (positions == null){
		return;
	}
	int max = start+length;
	if (max > all.length) max = all.length;
	int p = start <= 0 ? 0 : positions[start-1];
	for (int i = start; i<max; i++){
		char c = all[i];
		if (c != '\t') drawChar(c,x+p,y,0);
		p = positions[i];
	}
}

/*
{
	int [] positions = null;
	if (fts != null)
		positions = fts.calculatedPositions;
	if (positions == null){
		FontMetrics fm = new FontMetrics(curFont,surface);
		positions = fm.getFormattedTextPositions(s,fts,null);
	}
	y += _fontAscent;
	char [] all = ewe.sys.Vm.getStringChars(s);
	for (int i = 0; i<all.length; i++)
		_g.drawChars(all,i,1,x+positions[i],y);
}
*/
/** Draws an image at the given x and y coordinates.*/
public void drawImage(Image image, int x, int y)
	{
	if (image == null) return;
/*
	try{
		copyRect(image, 0, 0, image.getWidth(), image.getHeight(), x, y);
	}catch(Throwable t){
*/
		drawImage(image,null,null,x,y,image.getWidth(), image.getHeight());
	//}
	}

//===================================================================
public void drawImage(Image image,Image mask,Color transparent,int x,int y,int width,int height)
//===================================================================
{
/*
	if (transparent != null && image != null && mask == null)
		if (image.getColorMasks(transparent)){
			mask = image.colorMask;
			image = image.colorImage;
		}
	nativeDrawImage(image,mask,null,x,y,width,height);
*/
	nativeDrawImage(image,mask,transparent,x,y,width,height);

}
//-------------------------------------------------------------------
private native void nativeDrawImage(Image image,Image mask,Color notUsed,int x,int y,int width,int height);
//-------------------------------------------------------------------
/**
* This will draw and scale a portion of an image into the destination area. There are currently
* no scale options defined.
**/
//===================================================================
public void drawImage(
//===================================================================
Image image,Image mask,Color transparent,
Rect sourceImageArea,
Rect destArea,
int scaleOptions)
{
/*
	if (transparent != null && image != null && mask == null)
		if (image.getColorMasks(transparent)){
			mask = image.colorMask;
			image = image.colorImage;
		}
		*/
	nativeDrawImage(image,mask,transparent,sourceImageArea,destArea,scaleOptions);
}
//-------------------------------------------------------------------
private native void nativeDrawImage(
//-------------------------------------------------------------------
Image image,Image mask,Color notUsed,
Rect sourceImageArea,
Rect destArea,
int scaleOptions);

/** Draws a cursor by XORing the given rectangular area on the surface.
  * Since it is XORed, calling the method a second time with the same
  * parameters will erase the cursor.
  */
public native void drawCursor(int x, int y, int width, int height);


/**
 * Draws a line at the given coordinates. The drawing includes both
 * endpoints of the line.
 */
public native void drawLine(int x1, int y1, int x2, int y2);
static Object [] curves3 = new Object[]
{
	new int[]{3,3,1,0},
	new int[]{0,-1,-3,-3}
};
static Object [] curves3Ret = new Object[2];
static Object [] curves;
static int lastCurves = -1;
Object [] getRoundRectPoints(int style,int x, int y, int width, int height, int radius,int labelWidth)
{
	int r = radius;
	boolean all = (style & BF_RECT) == BF_RECT;
	boolean hasHLine = width > radius*2;
	boolean hasVLine = height > radius*2;
	//ewe.sys.Vm.debug("Radius: "+radius);
	Object [] curve;
	if (radius == 3){
		curve = curves3Ret;
		curve[0] = curves3[0];
		curve[1] = curves3[1];
 	}else if (radius == lastCurves) curve = (Object[])curves.clone();
	else {
		curve = getArcPoints(0,0,radius,radius,0,90,0);
		Object obj = curve.clone();
		curves = (Object[])obj;
		int [] cx = (int []) curve[0], cy = (int [])curve[1];
	}
	lastCurves = radius;
	int [] cx = (int []) curve[0], cy = (int [])curve[1];
	int cl = cx.length;
	int total = cl*4;
	int xp[] = new int[total+(labelWidth != 0 ? 1 : 0)], yp[] = new int[total+(labelWidth != 0 ? 1 : 0)];
	curve[0] = xp; curve[1] = yp;
	int dx, dy, j = 0;
	dx = r+x; dy = r+y;
	for (int i = 0; i<cl; i++){
		xp[j] = dx-cx[cl-1-i];
		yp[j++] = cy[cl-1-i]+dy;
	}
	dx = r+x; dy = height-1-r+y;
	for (int i = 0; i<cl; i++){
		xp[j] = dx-cx[i];
		yp[j++] = dy-cy[i];
	}
	dx = width-1-r+x; dy = height-1-r+y;
	for (int i = 0; i<cl; i++){
		xp[j] = cx[cl-1-i]+dx;
		yp[j++] = dy-cy[cl-1-i];
	}
	dx = width-1-r+x; dy = r+y;
	for (int i = 0; i<cl; i++){
		xp[j] = cx[i]+dx;
		yp[j++] = cy[i]+dy;
	}
	if (labelWidth != 0){
		xp[j] = x+4+labelWidth;
		yp[j++] = y;
	}
	if (all) return curve;
	int x2[] = new int[cl*2+2], y2[] = new int[cl*2+2];
	int end = cl*2+2-1;
	int s = 0;
	j = 0;
	if ((style & BF_TOP) == 0){ // Top one is missing, must start from top left.
		x2[j] = x; y2[j++] = y; s = cl*1;
		x2[end] = x+width-1; y2[end] = y;
	}else if ((style & BF_LEFT) == 0) { // Left one is missing, must start from bottom left.
		x2[j] = x; y2[j++] = y+height-1; s = cl*2;
		x2[end] = x; y2[end] = y;
	}else if ((style & BF_BOTTOM) == 0) { // Bottom one is missing, must start from bottom right.
		x2[j] = x+width-1; y2[j++] = y+height; s = cl*3;
		x2[end] = x; y2[end] = y+height;
	}else if ((style & BF_RIGHT) == 0) { // Right one is missing, must start from top right.
		x2[j] = x+width-1; y2[j++] = y; s = 0;
		x2[end] = x+width-1; y2[end] = y+height;
	}
	for (int i = 0; i<cl; i++){
		x2[j] = xp[i+s];
		y2[j++] = yp[i+s];
	}
 	s = (s+cl)%(cl*4);
	for (int i = 0; i<cl; i++){
		x2[j] = xp[i+s];
		y2[j++] = yp[i+s];
	}
	curve[0] = x2; curve[1] = y2;
	return curve;
}
/*
Object [] getRoundRectPoints(int style,int x, int y, int width, int height, int radius,int labelWidth)
{
	int r = radius;
	boolean all = (style & BF_RECT) == BF_RECT;
	boolean hasHLine = width > radius*2;
	boolean hasVLine = height > radius*2;
	Object [] curve = getArcPoints(0,0,radius,radius,0,90,0);
	int [] cx = (int []) curve[0], cy = (int [])curve[1];
	int cl = cx.length;
	int total = cl*4;
	int xp[] = new int[total+(labelWidth != 0 ? 1 : 0)], yp[] = new int[total+(labelWidth != 0 ? 1 : 0)];
	curve[0] = xp; curve[1] = yp;
	int dx, dy, j = 0;
	dx = r+x; dy = r+y;
	for (int i = 0; i<cl; i++){
		xp[j] = dx-cx[cl-1-i];
		yp[j++] = cy[cl-1-i]+dy;
	}
	dx = r+x; dy = height-1-r+y;
	for (int i = 0; i<cl; i++){
		xp[j] = dx-cx[i];
		yp[j++] = dy-cy[i];
	}
	dx = width-1-r+x; dy = height-1-r+y;
	for (int i = 0; i<cl; i++){
		xp[j] = cx[cl-1-i]+dx;
		yp[j++] = dy-cy[cl-1-i];
	}
	dx = width-1-r+x; dy = r+y;
	for (int i = 0; i<cl; i++){
		xp[j] = cx[i]+dx;
		yp[j++] = cy[i]+dy;
	}
	if (labelWidth != 0){
		xp[j] = x+4+labelWidth;
		yp[j++] = y;
	}
	if (all) return curve;
	int x2[] = new int[cl*2+2], y2[] = new int[cl*2+2];
	int end = cl*2+2-1;
	int s = 0;
	j = 0;
	if ((style & BF_TOP) == 0){ // Top one is missing, must start from top left.
		x2[j] = x; y2[j++] = y; s = cl*1;
		x2[end] = x+width-1; y2[end] = y;
	}else if ((style & BF_LEFT) == 0) { // Left one is missing, must start from bottom left.
		x2[j] = x; y2[j++] = y+height-1; s = cl*2;
		x2[end] = x; y2[end] = y;
	}else if ((style & BF_BOTTOM) == 0) { // Bottom one is missing, must start from bottom right.
		x2[j] = x+width-1; y2[j++] = y+height; s = cl*3;
		x2[end] = x; y2[end] = y+height;
	}else if ((style & BF_RIGHT) == 0) { // Right one is missing, must start from top right.
		x2[j] = x+width-1; y2[j++] = y; s = 0;
		x2[end] = x+width-1; y2[end] = y+height-1;
	}
	for (int i = 0; i<cl; i++){
		x2[j] = xp[i+s];
		y2[j++] = yp[i+s];
	}
 	s = (s+cl)%(cl*4);
	for (int i = 0; i<cl; i++){
		x2[j] = xp[i+s];
		y2[j++] = yp[i+s];
	}
	curve[0] = x2; curve[1] = y2;
	return curve;
}
*/
void doPolygon(Object [] points,boolean fill)
{
	int [] xp = (int [])points[0], yp = (int [])points[1];
	if (fill) fillPolygon(xp,yp,yp.length);
	else drawPolygon(xp,yp,yp.length);
}
void doLines(Object [] points)
{
	int [] xp = (int [])points[0], yp = (int [])points[1];
	drawLines(xp,yp,yp.length);
}
/**
 * Draw a rectangle with rounded corners.
 * @param radius the radius of the arcs that are at the corners.
 * @return
 */
//===================================================================
public void drawRoundRect(int x,int y,int width,int height,int radius)
//===================================================================
{
	doPolygon(getRoundRectPoints(BF_RECT,x,y,width,height,radius,0),false);
}
/**

 * Fill a rectangle with rounded corners.
 * @param radius the radius of the arcs that are at the corners.
 * @return
 */
//===================================================================
public void fillRoundRect(int x,int y,int width,int height,int radius)
//===================================================================
{
	doPolygon(getRoundRectPoints(BF_RECT,x,y,width,height,radius,0),true);

}

/**
 * Draws a rectangle at the given coordinates.
 */
public native void drawRect(int x,int y,int width,int height);
/*
public void drawRect(int x, int y, int width, int height)
	{
	// NOTE: only valid for drawing rects with width >=1, height >= 1
	int x2 = x + width - 1;
	int y2 = y + height - 1;
	drawLine(x, y, x2 - 1, y);
	drawLine(x2, y, x2, y2 - 1);
	drawLine(x2, y2, x + 1, y2);
	drawLine(x, y2, x, y + 1);

	}
*/
/**
 * Draws a dotted line at the given coordinates. Dotted lines must
 * be either horizontal or vertical, they can't be drawn at arbitrary angles.
 * @deprecated
 */
public native void drawDots(int x1, int y1, int x2, int y2);


/**
 * Draws the outline of a polygon with the given coordinates.
 * The polygon is automatically closed, you should not duplicate
 * the start point to close the polygon.
 * @param x x vertex coordinates
 * @param y y vertex coordinates
 * @param count number of vertices
 */
public void drawPolygon(int x[], int y[], int count)
	{
	if (count < 3)
		return;
	int i = 0;
	for (; i < count - 1; i++)
		drawLine(x[i], y[i], x[i + 1], y[i + 1]);
	drawLine(x[i], y[i], x[0], y[0]);
	}

//===================================================================
public void drawLines(int x[], int y[], int count)
//===================================================================
{
	for (int i = 0; i < count - 1; i++)
		drawLine(x[i], y[i], x[i + 1], y[i + 1]);
}

/**
* Draws the outline of a circular or elliptical arc covering the specified rectangle.
* The resulting arc begins at startAngle and extends for arcAngle degrees, using the current color. Angles are interpreted such that 0 degrees is at the 3 o'clock position. A positive value indicates a counter-clockwise rotation while a negative value indicates a clockwise rotation.
*
* The center of the arc is the center of the rectangle whose origin is (x, y) and whose size is specified by the width and height arguments.
**/
//===================================================================
public void drawArc(int x,int y,int width,int height,float startAngle,float angle)
//===================================================================
{
	Object [] both = getArcPoints(x+width/2-1,y+height/2-1,width/2,height/2,startAngle,startAngle+angle,0);
	int [] xp = (int [])both[0];
	int [] yp = (int [])both[1];
	drawLines(xp,yp,xp.length);
}
/**
* Draws the outline of a circular or elliptical arc covering the specified rectangle, and includes
* lines that connect the end and start point of the arc to each other (ie the connecting chord).
* The resulting arc begins at startAngle and extends for arcAngle degrees, using the current color. Angles are interpreted such that 0 degrees is at the 3 o'clock position. A positive value indicates a counter-clockwise rotation while a negative value indicates a clockwise rotation.
*
* The center of the arc is the center of the rectangle whose origin is (x, y) and whose size is specified by the width and height arguments.
**/

//===================================================================
public void drawClosedArc(int x,int y,int width,int height,float startAngle,float angle)
//===================================================================
{
	Object [] both = getArcPoints(x+width/2-1,y+height/2-1,width/2,height/2,startAngle,startAngle+angle,0);
	int [] xp = (int [])both[0];
	int [] yp = (int [])both[1];
	//xp[0] = x+width/2-1; yp[0] = y+height/2-1;
	drawPolygon(xp,yp,xp.length);
}
/**
* Draws the outline of a circular or elliptical arc covering the specified rectangle, and includes
* lines that connect the end and start point of the arc to each other (ie the connecting chord).
* The resulting arc begins at startAngle and extends for arcAngle degrees, using the current color. Angles are interpreted such that 0 degrees is at the 3 o'clock position. A positive value indicates a counter-clockwise rotation while a negative value indicates a clockwise rotation.
*
* The center of the arc is the center of the rectangle whose origin is (x, y) and whose size is specified by the width and height arguments.
**/

//===================================================================
public void fillClosedArc(int x,int y,int width,int height,float startAngle,float angle)
//===================================================================
{
	Object [] both = getArcPoints(x+width/2-1,y+height/2-1,width/2,height/2,startAngle,startAngle+angle,0);
	int [] xp = (int [])both[0];
	int [] yp = (int [])both[1];
	//xp[0] = x+width/2-1; yp[0] = y+height/2-1;
	fillPolygon(xp,yp,xp.length);
}/**
* Draws the outline of a circular or elliptical arc covering the specified rectangle, and includes
* lines that connect the end and start point of the arc to the center of the ellipse.
* The resulting arc begins at startAngle and extends for arcAngle degrees, using the current color. Angles are interpreted such that 0 degrees is at the 3 o'clock position. A positive value indicates a counter-clockwise rotation while a negative value indicates a clockwise rotation.
*
* The center of the arc is the center of the rectangle whose origin is (x, y) and whose size is specified by the width and height arguments.
**/
//===================================================================
public void drawPie(int x,int y,int width,int height,float startAngle,float angle)
//===================================================================
{
	Object [] both = getArcPoints(x+width/2-1,y+height/2-1,width/2,height/2,startAngle,startAngle+angle,1);
	int [] xp = (int [])both[0];
	int [] yp = (int [])both[1];
	xp[0] = x+width/2-1; yp[0] = y+height/2-1;
	drawPolygon(xp,yp,xp.length);
}
/**
* Draws and fills the outline of a circular or elliptical arc covering the specified rectangle, and includes
* lines that connect the end and start point of the arc to the center of the ellipse.
* The resulting arc begins at startAngle and extends for arcAngle degrees, using the current color. Angles are interpreted such that 0 degrees is at the 3 o'clock position. A positive value indicates a counter-clockwise rotation while a negative value indicates a clockwise rotation.
*
* The center of the arc is the center of the rectangle whose origin is (x, y) and whose size is specified by the width and height arguments.
**/
//===================================================================
public void fillPie(int x,int y,int width,int height,float startAngle,float angle)
//===================================================================
{
	Object [] both = getArcPoints(x+width/2-1,y+height/2-1,width/2,height/2,startAngle,startAngle+angle,1);
	int [] xp = (int [])both[0];
	int [] yp = (int [])both[1];
	xp[0] = x+width/2-1; yp[0] = y+height/2-1;
	fillPolygon(xp,yp,xp.length);
}
//-------------------------------------------------------------------
protected native static Object [] getArcPoints(int xc,int yc,int rx,int rw,float startAngle,float endAngle,int flags);
//-------------------------------------------------------------------
/**
 * Draws a filled polygon with the given coordinates.
 * The polygon is automatically closed, you should not duplicate
 * the start point to close the polygon. The polygon is filled
 * according to Jordan's rule - a point is inside if a horizontal
 * line to the point intersects the polygon an odd number of times.
 * This function is not implemented for the PalmOS VM. Under PalmOS only
 * the outline of the polygon is drawn.
 * @param x x vertex coordinates
 * @param y y vertex coordinates
 * @param count number of vertices
 */
public native void fillPolygon(int x[], int y[], int count);


/**
 * Draws text at the given coordinates. The x and y coordinates specify
 * the upper left hand corner of the text.
 */
public native void drawText(String s, int x, int y);


/**
 * Fills a rectangular area with the current color.
 */
public native void fillRect(int x, int y, int width, int height);


/**

 * Sets a clipping rectangle. Anything drawn outside of the rectangular
 * area specified will be clipped. Setting a clip overrides any previous clip.
 */
public native void setClip(int x, int y, int width, int height);


/**
 * Sets the x, y, width and height coordinates in the rectangle passed
 * to the current clip coordinates. To reduce the use of temporary objects
 * during drawing, this method does not allocate its own rectangle
 * object. If there is no current clip, null will be returned and
 * the rectangle passed will remain unchanged. Upon success, the
 * rectangle passed to the method will be returned.
 */
public native Rect getClip(Rect r);
private static Rect reducedClip = new Rect();
/**
* This alters the clipping region of the current graphics to be the intersection
* of the original clipping region and the newly specified region. It returns a Rect
* which you can then use to call restoreClip() to return the clipping region to its
* original value. This works even if there was no original clipping region since null
* will be returned in that case.
**/
//===================================================================
public Rect reduceClip(int x,int y,int width,int height,Rect oldClip)
//===================================================================
{
	Rect r = getClip(oldClip);
	if (r == null) {
		setClip(x,y,width,height);
		return null;
	}else{
		Rect r2 = reducedClip.set(x,y,width,height);
		r.getIntersection(r2,r2);
		setClip(r2.x,r2.y,r2.width,r2.height);
		return r;
	}
}
/**
* This alters the clipping region of the current graphics to be the intersection
* of the original clipping region and the newly specified region. It returns a Rect
* which you can then use to call restoreClip() to return the clipping region to its
* original value. This works even if there was no original clipping region.
**/
//===================================================================
public Rect reduceClip(Rect newRect)
//===================================================================
{
	return reduceClip(newRect.x,newRect.y,newRect.width,newRect.height,new Rect());
}
/**
* Use this with a Rect value returned from reduceClip()
**/
//===================================================================
public void restoreClip(Rect r)
//===================================================================
{
	if (r == null) clearClip();
	else setClip(r.x,r.y,r.width,r.height);
}
/**
 * Sets the current color for drawing operations.
 * @param r the red value (0..255)
 * @param g the green value (0..255)
 * @param b the blue value (0..255)
 */
public void setColor(int r, int g, int b)
	{
	color.set(r,g,b);
	native_setColor(r, g, b);
	}

public Color getColor()
{
	return color.getCopy();
}
public Color getBackground()
{
	return background.getCopy();
}

private native void native_setColor(int r, int g, int b);


/**
 * Sets the drawing operation. The setting determines the raster
 * operation to use when drawing lines, rectangles, text and
 * images on the current surface. It also determines how pixels are
 * combined when copying one surface to another. The setting of
 * DRAW_OVER, where any drawing simply draws over the pixels on
 * a surface, is the default.
 * <p>
 * Not all operations are supported on all platforms. When used with
 * Java, DRAW_OVER is supported for all types of drawing and DRAW_XOR
 * is supported for drawing lines, rectangles, text and images.
 * However, DRAW_XOR is not supported when copying surface areas and
 * the DRAW_AND and DRAW_OR operations aren't supported at all under




 * Java.
 * <p>
 * PalmOS platforms supports all the drawing operations when drawing
 * images and copying surface regions. However, only the DRAW_OVER
 * operation is supported when drawing lines, rectangles and text.
 * If you need to use the XOR drawing operation for drawing lines
 * under PalmOS, you can draw the line into an image and then draw
 * the image with an XOR drawing operation.
 * <p>
 * Win32 and Windows CE platforms support all the drawing operations
 * except when drawing text. Only DRAW_OVER is supported when drawing
 * text. If you need to draw XOR'ed text, you can draw the text into
 * an image and then draw the image with an XOR draw operation.
 * <p>
 * When calculating the result of XOR, AND and OR drawing, the value
 * of the color black is all 1's (fully set) in binary and white is
 * all 0's (fully unset).
 *
 * @param op drawing operation
 * @see #DRAW_OVER
 * @see #DRAW_AND
 * @see #DRAW_OR
 * @see #DRAW_XOR
 */
public native void setDrawOp(int drawOp);




/** Sets the current font for operations that draw text. */
public native void setFont(Font font);


/**
 * Translates the origin of the current coordinate system by the given

 * x and y values.
 */
public native void translate(int x, int y);


//===================================================================
public Pen getPen(){return pen;}
//===================================================================

//===================================================================
public Pen setPen(Pen newPen)
//===================================================================
{
	Pen was = pen;
	if (newPen == null) pen = null;
	else pen = new Pen(newPen.color,newPen.style,newPen.thickness);
	nativeSetPen(pen);
	return was;
}
//===================================================================
protected native void nativeSetPen(Pen p);
//===================================================================
//===================================================================

public Brush getBrush(){return brush;}
//===================================================================
//===================================================================

public Brush setBrush(Brush newBrush)
//===================================================================
{
	Brush was = brush;
	if (newBrush == null) brush = null;
	else brush = new Brush(newBrush.color,newBrush.style);
	nativeSetBrush(newBrush);
	return was;
}
//===================================================================
protected native void nativeSetBrush(Brush b);
//===================================================================

//MLB
public native void drawEllipse(int x,int y,int width,int height);
public native void fillEllipse(int x,int y,int width,int height);

//public boolean empty = false;
private static Graphics emptyGraphics;
//===================================================================
public static Graphics getEmptyGraphics() {return null;}

//===================================================================
public void setColor(Color c){setColor(c.getRed(),c.getGreen(),c.getBlue());}
//===================================================================
public void drawRect(int x,int y,int width,int height,int thickness)
//===================================================================
{
	drawRect(x,y,width,height);
/*
	fillRect(x,y,width,thickness);

	fillRect(x,y+height-thickness,width,thickness);
	fillRect(x,y,thickness,height);
	fillRect(x+width-thickness,y,thickness,height);
*/
}
//===================================================================
//public void draw3DRect(Rect r,boolean recessed) {draw3DRect(r,recessed,false);}
//===================================================================
//===================================================================
public void changePen(Color c,int style,int thickness)
//===================================================================
{
	if (pen == null) pen = new Pen(c,style,thickness);
	else {
		pen.color = c;
		pen.style = style;
		pen.thickness = thickness;
	}
	nativeSetPen(pen);
}
//===================================================================
public void changeBrush(Color c,int style)
//===================================================================
{
	if (c == null) brush = null;
	else if (brush == null) brush = new Brush(c,style);
	else{
		brush.color = c;
		brush.style = style;
	}
	nativeSetBrush(brush);
}
private static Color roundOutline = Color.DarkGray;//new Color(0,0,50);
private static Color innerFill = new Color(0,0,0),innerLine = new Color(0,0,0);
private static final int fillChange = 20;
//-------------------------------------------------------------------
private void draw3DRoundRect(Rect r,int style,boolean flat,Color fill,Color outlineColor,int labelWidth)
//-------------------------------------------------------------------
{
	int edge = style|BF_RECT;
	boolean down = ((edge & EDGE_SUNKEN) == EDGE_SUNKEN);
	if ((style & BF_FLAT) == BF_FLAT) flat = true;
	boolean button = ((style & BF_BUTTON) != 0);
	boolean outline = ((style & BDR_OUTLINE) != 0);
	innerLine.set(Color.LighterGray);
	if (fill != null && button){
		if (down)
			innerFill.set(
				fill.red-fillChange < 0 ? 0 : fill.red-fillChange,
				fill.green-fillChange < 0 ? 0 : fill.green-fillChange,
				fill.blue-fillChange < 0 ? 0 : fill.blue-fillChange);
		else{
			int fc = fillChange*2;
			innerLine.set(
				fill.red+fc > 255 ? 255 : fill.red+fc,
				fill.green+fc > 255 ? 255 : fill.green+fc,
				fill.blue+fc > 255 ? 255 : fill.blue+fc);
			innerFill.set(
				fill.red+fillChange > 255 ? 255 : fill.red+fillChange,
				fill.green+fillChange > 255 ? 255 : fill.green+fillChange,
				fill.blue+fillChange > 255 ? 255 : fill.blue+fillChange);
		}
		fill = innerFill;
	}
	if ((style & BDR_OUTLINE) == 0 || outlineColor == null) outlineColor = roundOutline;
	Object [] got = getRoundRectPoints(style,r.x,r.y,r.width,r.height,3,labelWidth);
	boolean willDoInner = !flat && !down;
	if ((style & BF_RECT) == BF_RECT) {
		changePen(outlineColor,Pen.SOLID,1);
		if (fill != null){
			changeBrush(willDoInner ? innerLine : fill,Brush.SOLID);
			doPolygon(got,true);
		}else{
			if (labelWidth == 0) doPolygon(got,false);
			else doLines(got);
		}
		if (!flat && !down){
			got = getRoundRectPoints(style,r.x+1,r.y+1,r.width-2,r.height-2,3,labelWidth);
			changePen(innerLine,Pen.SOLID,1);
			if (fill != null){
				changeBrush(fill,Brush.SOLID);
				doPolygon(got,true);
			}else{
				if (labelWidth == 0) doPolygon(got,false);
				else doLines(got);
			}
		}
	}else{
		setPen(null);
		changeBrush(fill,Brush.SOLID);
		doPolygon(got,fill != null);
		changePen(outlineColor,Pen.SOLID,1);
		doLines(got);
		if (!flat){
			got = getRoundRectPoints(style,r.x+1,r.y+1,r.width-2,r.height-2,3,labelWidth);
			changePen(innerLine,Pen.SOLID,1);
			doLines(got);
		}
	}
}
public static Color LighterGray = Color.LighterGray;
//===================================================================
public void drawEdge(Rect r,int edge,int flags,int labelWidth)
//===================================================================
{

		int x = r.x, y = r.y, w = r.width, h = r.height;
		int rec = 0;
		if (((edge|BF_RECT) & EDGE_SUNKEN) == EDGE_SUNKEN){
			if ((flags & BF_TOP) != 0) {
				if (labelWidth == 0){
					setColor(Color.DarkGray);
					drawLine(x,y,x+w-1,y);
					setColor(Color.Black);
					drawLine(x+1,y+1,x+w-2,y+1);
				}else{
					setColor(Color.DarkGray);
					drawLine(x,y,x+4,y);
					setColor(Color.Black);
					drawLine(x+1,y+1,x+4,y+1);
					setColor(Color.DarkGray);
					drawLine(x+4+labelWidth,y,x+w-1,y);
					setColor(Color.Black);
					drawLine(x+4+labelWidth,y+1,x+w-2,y+1);
				}
			}
			if ((flags & BF_LEFT) != 0) {
				setColor(Color.DarkGray);
				drawLine(x,y,x,y+h-1);
				setColor(Color.Black);
				drawLine(x+1,y+1,x+1,y+h-2);
			}
			if ((flags & BF_BOTTOM) != 0) {
				setColor(Color.White);
				drawLine(x,y+h-1,x+w-1,y+h-1);
				setColor(LighterGray);
				drawLine(x+1,y+h-2,x+w-2,y+h-2);
			}
			if ((flags & BF_RIGHT) != 0) {
				setColor(Color.White);
				drawLine(x+w-1,y,x+w-1,y+h-1);
				setColor(LighterGray);
				drawLine(x+w-2,y+1,x+w-2,y+h-2);
			}
		}else if (((edge|BF_RECT) & EDGE_ETCHED) == EDGE_ETCHED){
			if ((flags & BF_TOP) != 0) {
				if (labelWidth == 0){
					setColor(Color.DarkGray);
					drawLine(x,y,x+w-1,y);
					setColor(Color.White);
					drawLine(x+1,y+1,x+w-2,y+1);
				}else{
					setColor(Color.DarkGray);

					drawLine(x,y,x+4,y);
					setColor(Color.White);
					drawLine(x+1,y+1,x+4,y+1);
					setColor(Color.DarkGray);
					drawLine(x+4+labelWidth,y,x+w-1,y);
					setColor(Color.White);
					drawLine(x+4+labelWidth,y+1,x+w-2,y+1);
				}
			}
			if ((flags & BF_LEFT) != 0) {
				setColor(Color.DarkGray);
				drawLine(x,y,x,y+h-1);
				setColor(Color.White);
				rec = ((flags & BF_BOTTOM) != 0) ? 1 : 0;
				drawLine(x+1,y+1,x+1,y+h-1-rec);
			}
			if ((flags & BF_BOTTOM) != 0) {
				setColor(Color.White);
				drawLine(x,y+h-1,x+w-1,y+h-1);
				setColor(Color.DarkGray);
				drawLine(x+1,y+h-2,x+w-2,y+h-2);
			}
			if ((flags & BF_RIGHT) != 0) {

				setColor(Color.White);
				drawLine(x+w-1,y,x+w-1,y+h-1);
				setColor(Color.DarkGray);
				rec = ((flags & BF_BOTTOM) != 0) ? 1 : 0;
				drawLine(x+w-2,y+1,x+w-2,y+h-1-rec);

			}
		}else if (((edge|BF_RECT) & EDGE_BUMP) == EDGE_BUMP){
			if ((flags & BF_TOP) != 0) {
				if (labelWidth == 0){
					setColor(LighterGray);
					drawLine(x,y,x+w-1,y);
					setColor(Color.Black);
					drawLine(x+1,y+1,x+w-2,y+1);
				}else{
					setColor(LighterGray);
					drawLine(x,y,x+4,y);

					setColor(Color.Black);
					drawLine(x+1,y+1,x+4,y+1);
					setColor(LighterGray);
					drawLine(x+4+labelWidth,y,x+w-1,y);
					setColor(Color.Black);
					drawLine(x+4+labelWidth,y+1,x+w-2,y+1);
				}
			}
			if ((flags & BF_LEFT) != 0) {
				setColor(LighterGray);
				drawLine(x,y,x,y+h-1);
				setColor(Color.Black);
				drawLine(x+1,y+1,x+1,y+h-2);
			}
			if ((flags & BF_BOTTOM) != 0) {
				setColor(Color.Black);
				drawLine(x,y+h-1,x+w-1,y+h-1);
				setColor(LighterGray);
				drawLine(x+1,y+h-2,x+w-2,y+h-2);
			}
			if ((flags & BF_RIGHT) != 0) {
				setColor(Color.Black);
				drawLine(x+w-1,y,x+w-1,y+h-1);
				setColor(LighterGray);
				drawLine(x+w-2,y+1,x+w-2,y+h-2);
			}
		}else{
			if ((flags & BF_TOP) != 0) {
				if (labelWidth == 0){
					setColor(LighterGray);
					drawLine(x,y,x+w-1,y);
					setColor(Color.White);
					drawLine(x+1,y+1,x+w-2,y+1);
				}else{
					setColor(LighterGray);
					drawLine(x,y,x+4,y);
					setColor(Color.White);
					drawLine(x+1,y+1,x+4,y+1);
					setColor(LighterGray);
					drawLine(x+4+labelWidth,y,x+w-1,y);
					setColor(Color.White);
					drawLine(x+4+labelWidth,y+1,x+w-2,y+1);
				}
			}
			if ((flags & BF_LEFT) != 0) {
				setColor(LighterGray);
				drawLine(x,y,x,y+h-1);
				rec = ((flags & BF_BOTTOM) != 0) ? 1 : 0;
				setColor(Color.White);
				drawLine(x+1,y+1,x+1,y+h-1-rec);
			}


			if ((flags & BF_BOTTOM) != 0) {
				setColor(Color.Black);
				drawLine(x,y+h-1,x+w-1,y+h-1);
				setColor(Color.DarkGray);
				drawLine(x+1,y+h-2,x+w-2,y+h-2);
			}
			if ((flags & BF_RIGHT) != 0) {
				setColor(Color.Black);
				drawLine(x+w-1,y,x+w-1,y+h-1);
				rec = ((flags & BF_BOTTOM) != 0) ? 1 : 0;
				setColor(Color.DarkGray);
				drawLine(x+w-2,y+1,x+w-2,y+h-1-rec);
			}
		}
}
private static Pen aPen = new Pen(Color.Black,Pen.SOLID,1);

//-------------------------------------------------------------------
private void notNativedraw3DRect(Rect r,int style,boolean flat,Color fill,Color outlineColor,int labelWidth)
//-------------------------------------------------------------------
{
	if ((style & (BF_SOFT|BDR_DOTTED|BDR_NOBORDER|BF_SQUARE)) == BF_SOFT) {
		draw3DRoundRect(r,style,flat,fill,outlineColor,labelWidth);
		return;
	}
	Pen oldPen = setPen(null);

	try{
	Graphics g = this;
	if (fill != null){
		setColor(fill);
		fillRect(r.x,r.y,r.width,r.height);
	}
	int edge = style & 0xffff;
	int flags = style & 0xffff0000;
	if ((edge & BDR_DOTTED) != 0) {
		edge = BDR_DOTTED|BDR_OUTLINE;
		flags = BF_RECT;
	}
	if ((edge & BDR_NOBORDER) == 0) {
		if (flat && ((edge & BDR_DOTTED) == 0)) edge = BDR_OUTLINE;
		if ((edge & 0xf) != 0) drawEdge(r,edge,flags,labelWidth);
		if ((edge & BDR_OUTLINE) != 0) {
			if ((outlineColor != null) || ((edge & BDR_DOTTED) != 0)){
				if (outlineColor == null) outlineColor = Color.Black;
			}
			aPen.color = outlineColor;
			aPen.thickness = 1;
			aPen.style = ((edge & BDR_DOTTED) == 0) ? Pen.SOLID : Pen.DASH;
			g.setPen(aPen);
			if ((flags & BF_TOP) != 0)
				if (labelWidth == 0) drawLine(r.x,r.y,r.x+r.width-1,r.y);
				else {
					drawLine(r.x,r.y,r.x+4,r.y);
					drawLine(r.x+4+labelWidth,r.y,r.x+r.width-1,r.y);
				}
			if ((flags & BF_RIGHT) != 0) drawLine(r.x+r.width-1,r.y,r.x+r.width-1,r.y+r.height-1);
			if ((flags & BF_BOTTOM) != 0) drawLine(r.x+r.width-1,r.y+r.height-1,r.x,r.y+r.height-1);
			if ((flags & BF_LEFT) != 0) drawLine(r.x,r.y+r.height-1,r.x,r.y);
		}
	}
	}finally{
		setPen(oldPen);
	}
}

//===================================================================
public void draw3DRect(Rect r,int style,boolean flat,Color fill,Color outlineColor)
//===================================================================
{
	notNativedraw3DRect(r,style,flat,fill,outlineColor,0);
}

//===================================================================
public void draw3DRect(Rect r,int style,boolean flat,Color fill,Color outlineColor,int labelWidth)
//===================================================================
{
	notNativedraw3DRect(r,style,flat,fill,outlineColor,labelWidth);
}

/* Replaced July 15th 2002
public static Color LighterGray = Color.LighterGray;

//===================================================================
public void drawEdge(Rect r,int edge,int flags)
//===================================================================
{

		int x = r.x, y = r.y, w = r.width, h = r.height;
		int rec = 0;
		if (((edge|BF_RECT) & EDGE_SUNKEN) == EDGE_SUNKEN){
			if ((flags & BF_TOP) != 0) {
				setColor(Color.DarkGray);
				drawLine(x,y,x+w-1,y);
				setColor(Color.Black);
				drawLine(x+1,y+1,x+w-2,y+1);
			}
			if ((flags & BF_LEFT) != 0) {
				setColor(Color.DarkGray);
				drawLine(x,y,x,y+h-1);
				setColor(Color.Black);
				drawLine(x+1,y+1,x+1,y+h-2);
			}
			if ((flags & BF_BOTTOM) != 0) {
				setColor(Color.White);
				drawLine(x,y+h-1,x+w-1,y+h-1);
				setColor(LighterGray);
				drawLine(x+1,y+h-2,x+w-2,y+h-2);
			}
			if ((flags & BF_RIGHT) != 0) {
				setColor(Color.White);
				drawLine(x+w-1,y,x+w-1,y+h-1);
				setColor(LighterGray);
				drawLine(x+w-2,y+1,x+w-2,y+h-2);
			}
		}else if (((edge|BF_RECT) & EDGE_ETCHED) == EDGE_ETCHED){
			if ((flags & BF_TOP) != 0) {
				setColor(Color.DarkGray);
				drawLine(x,y,x+w-1,y);
				setColor(Color.White);
				drawLine(x+1,y+1,x+w-2,y+1);
			}
			if ((flags & BF_LEFT) != 0) {
				setColor(Color.DarkGray);

				drawLine(x,y,x,y+h-1);
				setColor(Color.White);
				rec = ((flags & BF_BOTTOM) != 0) ? 1 : 0;
				drawLine(x+1,y+1,x+1,y+h-1-rec);
			}
			if ((flags & BF_BOTTOM) != 0) {
				setColor(Color.White);
				drawLine(x,y+h-1,x+w-1,y+h-1);
				setColor(Color.DarkGray);
				drawLine(x+1,y+h-2,x+w-2,y+h-2);
			}
			if ((flags & BF_RIGHT) != 0) {

				setColor(Color.White);
				drawLine(x+w-1,y,x+w-1,y+h-1);
				setColor(Color.DarkGray);
				rec = ((flags & BF_BOTTOM) != 0) ? 1 : 0;
				drawLine(x+w-2,y+1,x+w-2,y+h-1-rec);

			}
		}else if (((edge|BF_RECT) & EDGE_BUMP) == EDGE_BUMP){
			if ((flags & BF_TOP) != 0) {
				setColor(LighterGray);
				drawLine(x,y,x+w-1,y);
				setColor(Color.Black);
				drawLine(x+1,y+1,x+w-2,y+1);
			}
			if ((flags & BF_LEFT) != 0) {
				setColor(LighterGray);
				drawLine(x,y,x,y+h-1);
				setColor(Color.Black);
				drawLine(x+1,y+1,x+1,y+h-2);
			}
			if ((flags & BF_BOTTOM) != 0) {
				setColor(Color.Black);
				drawLine(x,y+h-1,x+w-1,y+h-1);
				setColor(LighterGray);
				drawLine(x+1,y+h-2,x+w-2,y+h-2);
			}

			if ((flags & BF_RIGHT) != 0) {
				setColor(Color.Black);
				drawLine(x+w-1,y,x+w-1,y+h-1);
				setColor(LighterGray);
				drawLine(x+w-2,y+1,x+w-2,y+h-2);
			}
		}else{
			if ((flags & BF_TOP) != 0) {
				setColor(LighterGray);
				drawLine(x,y,x+w-1,y);
				setColor(Color.White);
				drawLine(x+1,y+1,x+w-2,y+1);
			}
			if ((flags & BF_LEFT) != 0) {
				setColor(LighterGray);
				drawLine(x,y,x,y+h-1);
				rec = ((flags & BF_BOTTOM) != 0) ? 1 : 0;
				setColor(Color.White);
				drawLine(x+1,y+1,x+1,y+h-1-rec);
			}
			if ((flags & BF_BOTTOM) != 0) {
				setColor(Color.Black);
				drawLine(x,y+h-1,x+w-1,y+h-1);
				setColor(Color.DarkGray);
				drawLine(x+1,y+h-2,x+w-2,y+h-2);
			}
			if ((flags & BF_RIGHT) != 0) {
				setColor(Color.Black);
				drawLine(x+w-1,y,x+w-1,y+h-1);
				rec = ((flags & BF_BOTTOM) != 0) ? 1 : 0;
				setColor(Color.DarkGray);
				drawLine(x+w-2,y+1,x+w-2,y+h-1-rec);
			}
		}
}

//==================================================================
public void notNativedraw3DRect(Rect r,int style,boolean flat,Color fill,Color outlineColor)
//==================================================================
{
	Pen oldPen = setPen(null);
	try{
	Graphics g = this;
	if (fill != null){

		setColor(fill);
		fillRect(r.x,r.y,r.width,r.height);
	}
	int edge = style & 0xffff;
	int flags = style & 0xffff0000;
	if ((edge & BDR_DOTTED) != 0) {
		edge = BDR_DOTTED|BDR_OUTLINE;
		flags = BF_RECT;
	}
	if ((edge & BDR_NOBORDER) == 0) {
		if (flat && ((edge & BDR_DOTTED) == 0)) edge = BDR_OUTLINE;
		if ((edge & 0xf) != 0) drawEdge(r,edge,flags);
		if ((edge & BDR_OUTLINE) != 0) {
			if ((outlineColor != null) || ((edge & BDR_DOTTED) != 0)){
				if (outlineColor == null) outlineColor = Color.Black;
			}
			if ((edge & BDR_DOTTED) == 0){
				g.setPen(new Pen(outlineColor,Pen.SOLID,1));
				if ((flags & BF_TOP) != 0) drawLine(r.x,r.y,r.x+r.width-1,r.y);
				if ((flags & BF_RIGHT) != 0) drawLine(r.x+r.width-1,r.y,r.x+r.width-1,r.y+r.height-1);
				if ((flags & BF_BOTTOM) != 0) drawLine(r.x+r.width-1,r.y+r.height-1,r.x,r.y+r.height-1);
				if ((flags & BF_LEFT) != 0) drawLine(r.x,r.y+r.height-1,r.x,r.y);
			}else{
				g.setPen(new Pen(outlineColor,Pen.DOT,1));
				if ((flags & BF_TOP) != 0) drawLine(r.x,r.y,r.x+r.width-1,r.y);
				if ((flags & BF_RIGHT) != 0) drawLine(r.x+r.width-1,r.y,r.x+r.width-1,r.y+r.height-1);
				if ((flags & BF_BOTTOM) != 0) drawLine(r.x+r.width-1,r.y+r.height-1,r.x,r.y+r.height-1);
				if ((flags & BF_LEFT) != 0) drawLine(r.x,r.y+r.height-1,r.x,r.y);
			}

		}
	}
	}finally{

		setPen(oldPen);
	}
}
*/
//==================================================================
public void drawDiamond(Rect r,int which)
//==================================================================
{
	int x = r.x, y = r.y, w = r.width, h = r.height;
//..................................................................
	if (w % 2 == 0) w--;

	if (h % 2 == 0) h--;
	if (w > h) w = h;
	if (h > w) h = w;
	int half = h/2;
	int [] xx = new int[4], yy = new int[4];
	x += r.width/2-1; y += r.height/2-1;
	int num = 3;
	switch(which){
		case Up:
			xx[0] = x-half; xx[1] = x; xx[2] = x+half;
			yy[0] = y; yy[1] = y-half; yy[2] = y;
			break;
		case Down:
			xx[0] = x-half; xx[1] = x; xx[2] = x+half;
			yy[0] = y; yy[1] = y+half; yy[2] = y;
			break;
		case Left:
			yy[0] = y-half; yy[1] = y; yy[2] = y+half;
			xx[0] = x; xx[1] = x-half; xx[2] = x;
			break;
		case Right:
			yy[0] = y-half; yy[1] = y; yy[2] = y+half;
			xx[0] = x; xx[1] = x+half; xx[2] = x;
			break;

		case All:
			xx[0] = x-half; xx[1] = x; xx[2] = x+half; xx[3] = x;
			yy[0] = y; yy[1] = y-half; yy[2] = y; yy[3] = y+half;
			num = 4;
			break;
	}
	fillPolygon(xx,yy,num);
//..................................................................
	//int p = w/2;
/*
		for (int i = 0; i<=p; i++){
			if (which == Left || which == All) drawLine(x+i,y+p-i,x+i,y+p+i);

			if (which == Right || which == All) drawLine(x+w-1-i,y+p-i,x+w-1-i,y+p+i);
		}
	if (which == Up || which == Down)
		for (int i = 0; i<=p; i++){
			if (which == Up) drawLine(x+p-i,y+i,x+p+i,y+i);
			if (which == Down) drawLine(x+p-i,y+w-1-i,x+p+i,y+w-1-i);
		}
	*/
}
//-------------------------------------------------------------------
protected void drawTail(Rect r,int which)
//-------------------------------------------------------------------
{
	int w = r.width/2, h = r.height/2;
	int x = 0, y = 0;
	if (which == Up) {
		x = (r.width-w)/2;
		y = h;
	}else if (which == Down) x = (r.width-w)/2;
	else if (which == Left) {
		y = (r.height-h)/2;
		x = w;
	}else if (which == Right)
		y = (r.height-h)/2;
	fillRect(r.x+x,r.y+y,w,h);
}

//===================================================================
public void drawArrow(Rect r,int which)
//===================================================================
{
	drawDiamond(r,which);
	drawTail(r,which);
}
/*
//===================================================================
public void draw3DButton(Rect r,boolean pressed,Color back,boolean flat,boolean border)
//===================================================================
{
	int opts = (pressed ? EDGE_SUNKEN : EDGE_RAISED)|BDR_OUTLINE;
	if (!border) opts |= BDR_NOBORDER;
	draw3DRect(r,opts,flat,back,Color.Black);
	if (true) return;
	Graphics g = this;
	int x = r.x, y = r.y, w = r.width, h = r.height;
	if (back != null) {
		setColor(back);
		g.fillRect(x,y,w,h);
	}
	if (!border) return;
	if (flat) {
		setColor(Color.Black);
		g.drawRect(r.x,r.y,r.width,r.height);
		return;
	}

	if (pressed){
		setColor(Color.LightGray);//White);
		g.drawLine(x+1,y+h-2,x+w-2,y+h-2);
		g.drawLine(x+w-2,y+1,x+w-2,y+h-2);
		setColor(Color.DarkGray);
		g.drawLine(x,y,x+w-1,y);
		g.drawLine(x,y,x,y+h-1);
		setColor(Color.Black);
		g.drawLine(x+1,y+1,x+w-2,y+1);
		g.drawLine(x+1,y+1,x+1,y+h-2);
		setColor(Color.DarkGray);
		g.drawLine(x,y+h-1,x+w-1,y+h-1);

		g.drawLine(x+w-1,y,x+w-1,y+h-1);
	}else{
		setColor(Color.DarkGray);
		g.drawLine(x,y,x+w-1,y);
		g.drawLine(x,y,x,y+h-1);
		setColor(Color.White);
		g.drawLine(x+1,y+1,x+w-2,y+1);
		g.drawLine(x+1,y+1,x+1,y+h-2);
		setColor(Color.Black);
		g.drawLine(x,y+h-1,x+w-1,y+h-1);
		g.drawLine(x+w-1,y,x+w-1,y+h-1);
		setColor(Color.DarkGray);
		g.drawLine(x+1,y+h-2,x+w-2,y+h-2);
		g.drawLine(x+w-2,y+1,x+w-2,y+h-2);
	}
}
*/
//===================================================================
public void reset()
//===================================================================
{
	clearClip();
	setDrawOp(DRAW_OVER);
}
//===================================================================
public boolean isEmpty() {return false;/*empty;*/}
//===================================================================

//===================================================================
public static Graphics createNew(ISurface is)
//===================================================================
{
	return new Graphics(is);
	//return new mGraphics(is);

}
//==================================================================
public static Image getImage(int w,int h)
//==================================================================
{
	if (w < 1) w = 1;
	if (h < 1) h = 1;
	return new Image(w,h);
}
/*
//===================================================================
public void drawText(FontMetrics fm,String [] lines,Rect where,int alignment)
//===================================================================
{
	drawText(fm,lines,where,alignment,CENTER);
}
*/
/**
* Modify subArea so that it is anchored appropriately in largeArea.
* anchor should be North, South, etc.
**/
//===================================================================
public void anchor(Rect subArea,Rect largeArea,int anchor)
//===================================================================
{

	subArea.x = ((largeArea.width-subArea.width)/2);
	subArea.y = ((largeArea.height-subArea.height)/2);
	if ((anchor & WEST) != 0) subArea.x = 0;
	else if ((anchor & EAST) != 0) subArea.x = largeArea.width-subArea.width;
	if ((anchor & NORTH) != 0) subArea.y = 0;

	else if ((anchor & SOUTH) != 0) subArea.y = largeArea.height-subArea.height;
	subArea.x += largeArea.x;
	subArea.y += largeArea.y;
}

private static Rect anchored = new Rect();
//===================================================================
public void drawImage(IImage image,int imageDrawOptions,Rect dest,int anchor)
//===================================================================
{
	if (image == null) return;
	anchor(anchored.set(0,0,image.getWidth(),image.getHeight()),dest,anchor);
	image.draw(this,anchored.x,anchored.y,imageDrawOptions);
}

//===================================================================
public void drawText(FontMetrics fm,String [] lines,Rect where,int alignment,int anchor)
//===================================================================
{
	if (lines == null) lines = new String[0];
	drawText(fm,lines,where,alignment,anchor,0,lines.length);
}

//===================================================================

public native void drawText(FontMetrics fm,String [] lines,Rect where,int alignment,int anchor,
int startLine,int endLine);

private static String [] strings = new String[1];
//===================================================================
public static void getSize(FontMetrics fm,String line,Dimension d)
//===================================================================
{
	strings[0] = line;
	getSize(fm,strings,0,1,d);
}
//===================================================================

public static void getSize(FontMetrics fm,String line,Dimension d,FormattedTextSpecs fts)
//===================================================================
{
	strings[0] = line;
	getSize(fm,strings,0,1,d,fts);
}
//===================================================================
public static void getSize(FontMetrics fm,String [] lines,int start,int end,Dimension dest,FormattedTextSpecs fts)
//===================================================================
{
	if (fts == null) getSize(fm,lines,start,end,dest);
	else{
		int w = 0, h = 0;
		int fh = fm.getHeight(), leading = fm.getLeading();
		if (start < 0) start = 0;
		if (end > lines.length) end = lines.length;
		for (int i = start; i<end; i++){
			int wd = FormattedTextSpecs.getWidthAndPositions(lines[i],fts,fm,false);
			if (wd > w) w = wd;
			if (i != 0) h += leading;
			h += fh;


		}
		dest.width = w;
		dest.height = h;
	}
}
//===================================================================
public static native void getSize(FontMetrics fm,String [] lines,int start,int end,Dimension dest);
//===================================================================
//===================================================================
public void drawText(FontMetrics fm,String [] lines,Rect where,int alignment,int anchor,
int startLine,int endLine,FormattedTextSpecs fts)
//===================================================================
{
	if (fts == null) drawText(fm,lines,where,alignment,anchor,startLine,endLine);
	else{
		getSize(fm,lines,startLine,endLine,Dimension.buff,fts);
		Rect tr = Rect.buff; tr.width = Dimension.buff.width; tr.height = Dimension.buff.height; tr.x = tr.y = 0;
		anchor(tr,where,anchor);
		drawTextIn(fm,lines,tr,alignment,startLine,endLine,fts);
	}
}
/*
//===================================================================
public void nonNativeDrawText(FontMetrics fm,String [] lines,Rect where,int alignment,int anchor,
int startLine,int endLine)
//===================================================================
{
	Rect tr = ewe.ui.Gui.getSize(fm,lines,0,0);
	anchor(tr,where,anchor);
	drawTextIn(fm,lines,tr,alignment,startLine,endLine);
}
*/
//-------------------------------------------------------------------
protected void drawTextIn(FontMetrics fm,String [] lines,Rect where,int alignment,int start,int end,FormattedTextSpecs fts)

//-------------------------------------------------------------------
{
	Graphics g = this;
	if (fts == null) fts = new FormattedTextSpecs();
	fts.metrics = fm;
	if (start >= lines.length || end <= start) return;
	if (end > lines.length) end = lines.length;
	int num = end-start;
	int h = fm.getHeight(), leading = fm.getLeading();
	int y = where.y;
	g.setFont(fm.getFont());
	//if (lines.length == 9) System.out.println("\n");
	//for (int i = 0; i<start; i++) y += h+leading;
	for (int i = 0; i<num; i++){
		String l = lines[i+start];
		if (i != 0) y += leading;
		int w = FormattedTextSpecs.getWidthAndPositions(l,fts,fm,false);
		int xp = where.x;
		if (alignment == Right) {
			//l = where.toString();
			xp += where.width-w;
			//ewe.ui.Control.np.x = 0;
		}
		else if (alignment == CENTER) xp += (where.width-w)/2;
		if (fts.isFormatted) g.drawFormattedText(l,xp,y,fts);
		else g.drawText(l,xp,y);
		//if (lines.length == 9) System.out.println(l+" @ "+xp+","+y);
		y += h;
	}
}
//-------------------------------------------------------------------
void changePen(int style,int thick)
//-------------------------------------------------------------------
{
	if (pen == null) setPen(new Pen(color,style,thick));
	else {
		pen.style = style;
		pen.thickness = thick;
		pen.color = color;
	}
}
//===================================================================
public void drawVerticalTriangle(Rect bounds,boolean up)
//===================================================================
{
	Rect r = bounds;
	int w = bounds.width;
	if ((w & 1) == 0) w--;
	if (w < 0) return;
	int n = w/2;
	int h = w+1; //(n+1)*2
	int f = up ? -1 : 1;
	int px = 0;
	int st = 2;
	if (h > r.height){
		px = 1; h = w; // (n*2)+1
		if (h > r.height){
			h = w-1; // (n*2)
			if (h > r.height){

				px = 0; h = n+1; st = 1;
			}
		}
	}

	changePen(Pen.SOLID,1);

	int sx = r.x, ex = r.x+w-1;
	int yy = up ? r.y+h-1 : r.y+r.height-h;
	int min = r.y, max = r.y+r.height-1;
	for (int y = 0; y<h; y++){
		if (yy >= min && yy <= max)
			drawLine(sx,yy,ex,yy);
		if ((px+1)%st == 0) {
			sx++;
			ex--;
		}
		px++;
		yy += f;
	}

}

//===================================================================
public void drawHorizontalTriangle(Rect bounds,boolean left)
//===================================================================
{
	Rect r = bounds;
	int w = bounds.height;
	if ((w & 1) == 0) w--;
	if (w < 0) return;
	int n = w/2;
	int h = w+1; //(n+1)*2
	int f = left ? -1 : 1;


	int px = 0;
	int st = 2;
	if (h > r.width){
		px = 1; h = w; // (n*2)+1
		if (h > r.width){
			h = w-1; // (n*2)
			if (h > r.width){
				px = 0; h = n+1; st = 1;
			}
		}
	}

	changePen(Pen.SOLID,1);

	int sx = r.y, ex = r.y+w-1;

	int yy = left ? r.x+h-1 : r.x+r.width-h;
	int min = r.x, max = r.x+r.width-1;
	for (int y = 0; y<h; y++){
		if (yy >= min && yy <= max)
		drawLine(yy,sx,yy,ex);
		if ((px+1)%st == 0) {
			sx++;
			ex--;
		}
		px++;
		yy += f;
	}
}
/*
//===================================================================
public void drawVerticalTriangle(Rect bounds,boolean up)
//===================================================================
{
	int [] x = new int[3], y = new int[3];

	int w = bounds.width;
	if ((w % 2) != 0) w--;
	if (!up) {
		x[0] = bounds.x;
		x[1] = bounds.x+w;
		x[2] = bounds.x+(w/2);
		y[0] = bounds.y;
		y[1] = bounds.y;
		y[2] = bounds.y+bounds.height;
	}else{
		x[0] = bounds.x;
		x[1] = bounds.x+w;
		x[2] = bounds.x+(w/2);
		y[0] = bounds.y+bounds.height;
		y[1] = bounds.y+bounds.height;

		y[2] = bounds.y;
	}
	fillPolygon(x,y,3);
}
*/
//===================================================================
public void draw3DDiamond(Rect rect,boolean pressed,Color back) {draw3DDiamond(rect,pressed,back,false);}
//===================================================================
public void draw3DDiamond(Rect rect,boolean pressed,Color back,boolean amFlat)
//==================================================================
{
	Rect r = new Rect(); r.set(rect);
	if (pressed || amFlat){
		setColor(Color.Black);
		drawDiamond(r,Up);
		if (!amFlat)setColor(Color.DarkGray);
		drawDiamond(r,Down);
		r.x++; r.y++; r.width -= 2; r.height -=2;
		setColor(Color.White);
		drawDiamond(r,Down);
		if (!amFlat) setColor(Color.DarkGray);
		drawDiamond(r,Up);
		r.x++; r.y++; r.width -= 2; r.height -=2;
	}else {

		setColor(Color.Black);
		drawDiamond(r,Down);
		setColor(Color.DarkGray);
		drawDiamond(r,Up);
		r.x++; r.y++; r.width -= 2; r.height -=2;
		setColor(Color.DarkGray);
		drawDiamond(r,Down);
		setColor(Color.White);
		drawDiamond(r,Up);
		r.x++; r.y++; r.width -= 2; r.height -=2;
	}
	setColor(back);
	drawDiamond(r,All);
}

public static final int SPECIAL_TICK = 1;
public static final int SPECIAL_X = 2;

//===================================================================
public void drawSpecial(int what,Rect where,Color c)
//===================================================================
{
	Pen old = getPen();
	switch(what){
		case SPECIAL_TICK:
			setPen(new Pen(c,Pen.SOLID,2));
			drawLine(where.x+where.width-2,where.y+2,where.x-2,where.y+where.height-2);
			drawLine(where.x-2,where.y+where.height-2,where.x-2,where.y+where.height/3);
			break;
		case SPECIAL_X:
			setPen(new Pen(c,Pen.SOLID,2));
			drawLine(where.x+where.width-2,where.y,where.x,where.y+where.height-2);
			drawLine(where.x,where.y,where.x+where.width-2,where.y+where.height-2);
			break;
	}
	setPen(old);
}

/**
* Get a FontMetrics for the font on the ISurface for this Graphics.
**/
//===================================================================
public FontMetrics getFontMetrics(Font font)
//===================================================================
{
	return new FontMetrics(font,surface);
}
/**
* This is a fast way of drawing a solid line of thickness 1 of a particular color.
* This will set the pen/brush of the Graphics to an indeterminate state.
* @see setPixelRGB()
* @see fillRectRGB()
**/
public  void drawLineRGB(int x1,int y1,int x2,int y2,int rgb)

{
	pen = null;
	setColor((rgb >> 16) & 0xff,(rgb >> 8) & 0xff, rgb & 0xff);

	drawLine(x1,y1,x2,y2);
}
/**
* This is a fast way of setting pixel to a particular color.
* This will set the pen/brush of the Graphics to an indeterminate state.
* @see drawLineRGB()
* @see fillRectRGB()
**/
public  void setPixelRGB(int x,int y,int rgb)
{drawLineRGB(x,y,x,y,rgb);}
/**

* This is a fast way of setting pixel to a particular color.
* This will set the pen/brush of the Graphics to an indeterminate state.
* @see drawLineRGB()
* @see setPixelRGB()
**/
public void fillRectRGB(int x1,int y1,int x2,int y2,int rgb)
{
	int dy = y1 > y2 ? 1 : -1;
	for (int y = y1;;y+=dy){
		drawLineRGB(x1,y,x2,y,rgb);
		if (y == y2) break;
	}
}
private static boolean inUpdate;
/**
* On some systems it may be necessary to call this method to have operations made to the
* Graphics actually be displayed on the screen. On most systems this will have no effect.
**/
public void flush()
{
	// Do flushing here.
	if (ewe.ui.PenEvent.tipIsVisible && !inUpdate)
	try{
		inUpdate = true;
		ewe.ui.PenEvent.refreshTip(surface);
	}finally{
		inUpdate = false;
	}
}

private static Object [] getWindowIconImages()
{
	Image [] ret = new Image[2];
	ret[0] = new Image("ewe/ewesmall.bmp");
	ret[1] = null;
	return ret;
}
}
