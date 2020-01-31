/* $MirOS: contrib/hosted/ewe/extrajdk/ewe/fx/Graphics.java,v 1.2 2008/05/02 20:52:03 tg Exp $ */

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

import ewe.ui.Window;
import ewe.applet.Applet;
import java.awt.*;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
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

java.awt.Graphics _g;
private int _fontAscent;
private boolean _xorDrawMode;
private int _transX = 0, _transY = 0;

public static boolean canAlphaBlend = true;

public static boolean canCopy = false;
public static boolean canMove = true;
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
	this(surface,null);
	}

public Graphics(ISurface surface, boolean forCopying)
{
	this(surface,null,forCopying);
}
Graphics(ISurface surface,java.awt.Graphics g)
{
	this(surface,g,false);
}
public Graphics(Object nativeGraphics)
{
	this(null,(java.awt.Graphics)nativeGraphics);
}

Graphics(ISurface surface,java.awt.Graphics g,boolean forCopying)
{
	if (!forCopying)
	if (surface instanceof Image)
		if (((Image)surface).wasLoaded)
			throw new IllegalArgumentException("Cannot draw to a loaded image.");
	this.surface = surface;
	if (g == null) g = createAWTGraphics();
	if (g == null) {
		//new Exception("No Graphics!").printStackTrace();
		g = Image.newBufferedImage(2,2).createGraphics();
	}
	_g = g;
	_g.setColor(java.awt.Color.black);
	try{
		((Graphics2D)_g).setPaint(null);
	}catch(Throwable e){
	}
	setFont(new Font("Helvetica", Font.PLAIN, 12));
}

public boolean isValid() {return _g != null;}

private java.awt.Graphics createAWTGraphics()
	{
	java.awt.Graphics g = null;

	if (surface instanceof Window)
		{
		Window win = (Window)surface;
		g = win.createAWTGraphics();
		}
	else if (surface instanceof Image)
		{
		Image image = (Image)surface;
		java.awt.Image im = image.getAWTImage();
		if (im != null) {
			g = im.getGraphics();
			g.setClip(0,0,image.width,image.height);//This is necessary for Microsoft's VM.
		}
		}
	return g;
	}

/* Not Java
public Graphics(ISurface surface)
	{
	this.surface = surface;
	_nativeCreate();
	}
private native void _nativeCreate();
*/
public int getSurfaceType()
{
	if (surface instanceof ewe.ui.Window) return ISurface.WINDOW_SURFACE;
	else if (surface instanceof ewe.fx.Image) return ISurface.IMAGE_SURFACE;
	//else if (surface instanceof ewe.fx.PrinterJob) return ISurface.PRINTERJOB_SURFACE;
	else return 0;
}
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
 * Clears the current clipping rectangle. This allows drawing to occur
 * anywhere on the current surface.
 */

//public native void clearClip(); Not Java
public void clearClip()
	{
// NOTE: we had the following code in for the JDK 1.1, however, the
// JDK 1.1 is buggy and clearing a clip rectangle in this way seems
// to leave the graphics context messed up.
//
//	try
//		{
//		_g.translate(- _transX, - _transY);
//		_g.setClip(-64000, -64000, 64000, 64000);
//		_g.translate(_transX, _transY);
//		}
//	catch (NoSuchMethodError e)
//		{
		// JDK 1.02 - need to create a copy since there isn't
		// a way to clear the clip rect
		java.awt.Graphics newG = createAWTGraphics();
		newG.setFont(_g.getFont());
		newG.setColor(_g.getColor());
		newG.translate(_transX, _transY);
		if (_xorDrawMode)
			newG.setXORMode(java.awt.Color.white);
		_g.dispose();
		_g = newG;
//		}
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
//public native void copyRect(ISurface surface, int x, int y,
//	int width, int height, int dstX, int dstY);
static boolean copyAppErrDisplayed = false;

public void moveRect(int x,int y,int width,int height,int destX,int destY)
{
	_g.copyArea(x,y,width,height,destX-x,destY-y);
}

public void copyRect(ISurface surface, int x, int y,
	int width, int height, int dstX, int dstY)
{
	if (surface instanceof Image)
			_g.drawImage(((Image)surface).getAWTImage(),
				dstX, dstY, dstX + width, dstY + height,
				x, y, x + width,y + height,
				null);
	else{
	Graphics g = new Graphics(surface,null,true);
	copyGraphics(g,x,y,width,height,dstX,dstY);
	g.free();
	}
}

public boolean copyRect(Graphics source,int x,int y,int width,int height,int destX,int destY)
{
	try{
		if (source.surface instanceof Image){
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


private void copyGraphics(Graphics source,int x,int y,int width,int height,int dstX,int dstY)
{
	try{
	ISurface surface = source.surface;
	if (surface instanceof Window)
		{
		if (this.surface != surface)
			{
			if (!copyAppErrDisplayed)
				{
				System.out.println("WARNING: Copying from apps isn't " +
					"supported under non-native WabaVMs");
				copyAppErrDisplayed = true;
				}
			}
		else
			_g.copyArea(x, y, width, height, dstX - x, dstY - y);
		}
	else if (surface instanceof Image)
		{
		Image srcImage = (Image)surface;
			// JDK 1.1
			_g.drawImage(srcImage.getAWTImage(),
				dstX, dstY, dstX + width, dstY + height,
				source._transX+x, source._transY+y, source._transX+x + width, source._transY +y + height,
				null);
		}
		}catch(Throwable t){}
	}


/*
public void copyRect(ISurface surface, int x, int y,
	int width, int height, int dstX, int dstY)
	{
	if (surface instanceof Window)
		{
		if (this.surface != surface)
			{
			if (!copyAppErrDisplayed)
				{
				System.out.println("WARNING: Copying from apps isn't " +
					"supported under non-native WabaVMs");
				copyAppErrDisplayed = true;
				}
			}
		else
			_g.copyArea(x, y, width, height, dstX - x, dstY - y);
		}
	else if (surface instanceof Image)
		{
		Image srcImage = (Image)surface;
		try
			{
			// JDK 1.1
			_g.drawImage(srcImage.getAWTImage(),
				dstX, dstY, dstX + width, dstY + height,
				x, y, x + width, y + height,
				null);
			}
		catch (NoSuchMethodError e)
			{
			// JDK 1.02
			java.awt.Rectangle r = _g.getClipRect();
			setClip(dstX, dstY, dstX + width, dstY + height);
			_g.drawImage(srcImage.getAWTImage(), dstX - x, dstY - y, null);
			setClip(r.x, r.y, r.width, r.height);
			}
		}
	}
*/

/**
 * Frees any system resources (native device contexts) associated with the
 * graphics object. After calling this function, the graphics object can no
 * longer be used to draw. Calling this method is not required since any
 * system resources allocated will be freed when the object is garbage
 * collected. However, if a program uses many graphics objects, free()
 * should be called whenever one is no longer needed to prevent allocating
 * too many system resources before garbage collection can occur.
 */
//public native void free();
public void free()
	{
	flush();
	_g.dispose();
	}


/**
 * Draws text at the given coordinates. The x and y coordinates specify
 * the upper left hand corner of the text's bounding box.
 * @param chars the character array to display
 * @param start the start position in array
 * @param count the number of characters to display
 * @param x the left coordinate of the text's bounding box
 * @param y the top coordinate of the text's bounding box
 */
//public native void drawText(char chars[], int start, int count, int x, int y);
public void drawText(char chars[], int start, int count, int x, int y)
	{
		drawText(new String(chars,start,count),x,y);
	 // _g.drawChars(chars, start, count, x, y + _fontAscent);
	}


/** Draws an image at the given x and y coordinates.*/
public void drawImage(Image image, int x, int y)
	{
	//copyRect(image, 0, 0, image.getWidth(), image.getHeight(), x, y);
	_g.drawImage(image.getAWTImage(),x,y,null);
	}

//public native void drawImage(Image image,Image mask,Color transparent,int x,int y,int width,int height);
public void drawImage(Image image,Image mask,Color transparent,int x,int y,int width,int height)
{
	try{
		image.doCheckMask(mask,transparent);
		_g.drawImage(image.bufferedImage,x,y,null);
	}catch(Error e){
		if (mask != null){
			setDrawOp(DRAW_AND);
			drawImage(mask,0,0);
			setDrawOp(DRAW_OR);
		}else
			setDrawOp(DRAW_OVER);
		drawImage(image,0,0);
		setDrawOp(DRAW_OVER);
	}
}
/**
* This will draw and scale a portion of an image into the destination area. There are currently
* no scale options defined.
**/
public void drawImage(
Image image,Image mask,Color transparent,
Rect sourceImageArea,
Rect destArea,
int scaleOptions)
{
	image.doCheckMask(mask,transparent);
	if (sourceImageArea == null || destArea == null) return;
	_g.drawImage(image.bufferedImage,
	destArea.x,destArea.y,destArea.x+destArea.width,destArea.y+destArea.height,
	sourceImageArea.x,sourceImageArea.y,sourceImageArea.x+sourceImageArea.width,sourceImageArea.y+sourceImageArea.height,null);
}

/** Draws a cursor by XORing the given rectangular area on the surface.
  * Since it is XORed, calling the method a second time with the same
  * parameters will erase the cursor.
  */
//public native void drawCursor(int x, int y, int width, int height);
public void drawCursor(int x, int y, int width, int height)
	{
	// save current color (xor mode already saved)
	java.awt.Color c = _g.getColor();

	// set current color, XOR drawOp and fill rect
	_g.setColor(java.awt.Color.black);
	_g.setXORMode(java.awt.Color.white);
	_g.fillRect(x, y, width, height);

	// restore XOR drawOp and color
	if (!_xorDrawMode)
		_g.setPaintMode();
	_g.setColor(c);
	}


/**
 * Draws a line at the given coordinates. The drawing includes both
 * endpoints of the line.
 */
//public native void drawLine(int x1, int y1, int x2, int y2);
public void drawLine(int x1, int y1, int x2, int y2)
	{
	_g.drawLine(x1, y1, x2, y2);
	}
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
//public native void drawRect(int x,int y,int width,int height);

/**
 * Draws a dotted line at the given coordinates. Dotted lines must
 * be either horizontal or vertical, they can't be drawn at arbitrary angles.
 */
//public native void drawDots(int x1, int y1, int x2, int y2);
public void drawDots(int x1, int y1, int x2, int y2)
	{
	if (x1 == x2) // vertical
		{
		if (y1 > y2)
			{
			int y = y1;
			y1 = y2;
			y2 = y;
			}
		for (; y1 < y2; y1 += 2)
			_g.drawLine(x1, y1, x1, y1);
		}
	else if (y1 == y2) // horizontal
		{
		if (x1 > x2)
			{
			int x = x1;
			x1 = x2;
			x2 = x;
			}
		for (; x1 < x2; x1 += 2)
			_g.drawLine(x1, y1, x1, y1);
		}
	}



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
	if (Image.is12)
	try{
		draw(new java.awt.Polygon(x,y,count));
		return;
	}catch(Error e){
	}
	int i = 0;
	for (; i < count - 1; i++)
		drawLine(x[i], y[i], x[i + 1], y[i + 1]);
	drawLine(x[i], y[i], x[0], y[0]);
	}

////////////////////////////////////////////////////////////////////////////
// draw an elliptical arc from startAngle to endAngle. c is the fill color and c2 is the outline color (if in fill mode - otherwise, c = outline color)

static int [] xPoints = new int[0], yPoints = new int[0];
static int nPoints = 0;
static int startIndex,endIndex;
static int lastRX=-1,lastRY=-1,lastXC = -1, lastYC = -1,lastSize=0;
static float lastPPD=0;

//-------------------------------------------------------------------
static void arcPiePointDrawAndFill(int xc, int yc, int rx, int ry, float startAngle, float endAngle, boolean append)
//-------------------------------------------------------------------
{
   // this algorithm was created by Guilherme Campos Hazan
   float ppd;
   int index,i;
   int nq,size=0;

   //int oldX,oldY;
   // step 0: correct angle values
   /*
   if (startAngle < 0.1 && endAngle > 359.9) // full circle? use the fastest routine instead
   {
      if (fill)
         fillEllipse(xc,yc,rx,ry,c);
      drawEllipse(xc,yc,rx,ry,fill?c2:c);
      return;
   }
   */
   // step 0: if possible, use cached results
   if (xc != lastXC || yc != lastYC || rx != lastRX || ry != lastRY)
   {
       // step 1: computes how many points the circle has (computes only 45 degrees and mirrors the rest)
       // intermediate terms to speed up loop
        long t1 = rx*rx, t2 = t1<<1, t3 = t2<<1;
        long t4 = ry*ry, t5 = t4<<1, t6 = t5<<1;
        long t7 = rx*t5, t8 = t7<<1, t9 = 0L;
        long d1 = t2 - t7 + (t4>>1);    // error terms
        long d2 = (t1>>1) - t8 + t5;

        int x = rx, y = 0; // ellipse points

        while (d2 < 0)          // til slope = -1
        {
            y++;        // always move up here
            t9 += t3;
            if (d1 < 0) // move straight up
            {
                d1 += t9 + t2;
                d2 += t9;
            }
            else        // move up and left
            {
                x--;
                t8 -= t6;
                d1 += t9 + t2 - t8;
                d2 += t9 + t5 - t8;
            }
            size++;
        }

        do              // rest of top right quadrant
        {
            x--;        // always move left here
            t8 -= t6;
            if (d2 < 0) // move up and left
            {
                y++;
                t9 += t3;
                d2 += t9 + t5 - t8;
            }
            else        // move straight left
                d2 += t5 - t8;
          size++;

        } while (x >= 0);
       nq = size;
       size *= 4;
       // step 2: computes how many points per degree
       ppd = (float)size / 360.0f;
       // step 3: create space in the buffer so it can save all the circle
       size+=2;
       if (nPoints < size)
       {
				/*
          if (xPoints)
          {
             xfree(xPoints);
             xfree(yPoints);
          }
				*/
          xPoints = new int[size];//xmalloc(sizeof(int32)*size);
          yPoints = new int[size];//xmalloc(sizeof(int32)*size);
       }
       nPoints = size;
       // step 4: stores all the circle in the array. the odd arcs are drawn in reverse order
        // intermediate terms to speed up loop
        t2 = t1<<1; t3 = t2<<1;
        t8 = t7<<1; t9 = 0L;
        d1 = t2 - t7 + (t4>>1); // error terms
        d2 = (t1>>1) - t8 + t5;

        x = rx;
        y = 0;  // ellipse points

        i=0;
        while (d2 < 0)          // til slope = -1
        {
            // save 4 points using symmetry
            index = nq*0+i;   xPoints[index]=xc+x; yPoints[index]=yc-y; // 0/3
            index = nq*2-i-1; xPoints[index]=xc-x; yPoints[index]=yc-y; // 1/3
            index = nq*2+i;   xPoints[index]=xc-x; yPoints[index]=yc+y; // 2/3
            index = nq*4-i-1; xPoints[index]=xc+x; yPoints[index]=yc+y; // 3/3
            i++;

            y++;        // always move up here
            t9 += t3;
            if (d1 < 0) // move straight up
            {
                d1 += t9 + t2;
                d2 += t9;
            }
            else        // move up and left
            {
                x--;
                t8 -= t6;
                d1 += t9 + t2 - t8;
                d2 += t9 + t5 - t8;
            }
        }

        do              // rest of top right quadrant
        {
            // save 4 points using symmetry
            index = nq*0+i;   xPoints[index]=xc+x; yPoints[index]=yc-y; // 0/3
            index = nq*2-i-1; xPoints[index]=xc-x; yPoints[index]=yc-y; // 1/3
            index = nq*2+i;   xPoints[index]=xc-x; yPoints[index]=yc+y; // 2/3
            index = nq*4-i-1; xPoints[index]=xc+x; yPoints[index]=yc+y; // 3/3
            i++;

            x--;        // always move left here
            t8 -= t6;
            if (d2 < 0) // move up and left
            {
                y++;
                t9 += t3;
                d2 += t9 + t5 - t8;
            }
            else        // move straight left
                d2 += t5 - t8;
        } while (x >= 0);
       // save last arguments
       lastXC = xc;
       lastYC = yc;
       lastRX = rx;
       lastRY = ry;
       lastPPD = ppd;
       lastSize = size;
   }
   else ppd = lastPPD;
   // step 5: computes the start and end indexes that will become part of the arc
   if (!append) startIndex = (int)(ppd * startAngle);
    endIndex = (int)(ppd * endAngle);
   // step 5 1/2: if only computing the point, return it
   /*
   if (startAnglePoint)
   {
      startAnglePoint[0] = xPoints[startIndex];
      startAnglePoint[1] = yPoints[startIndex];
      return;
   }*/

   if (endIndex == (lastSize-2)) // 360?
      endIndex--;
	 else
   // step 6: fill or draw the polygons
   	 endIndex++;

   /*
   if (pie)
   {
      oldX = xPoints[endIndex];
      oldY = yPoints[endIndex];
      xPoints[endIndex] = xc;
      yPoints[endIndex] = yc;
      endIndex++;
   }
   if (fill)
      fillPolygon(xPoints+startIndex,yPoints+startIndex,endIndex-startIndex,c);
//   if (!fill || c != c2) always draw border
      drawPolygon(xPoints+startIndex,yPoints+startIndex,endIndex-startIndex,fill?c2:c,fill);
   if (pie) // restore saved points
   {
      xPoints[endIndex-1] = oldX;
      yPoints[endIndex-1] = oldY;
   }
	*/
}
////////////////////////////////////////////////////////////////////////////
// calls arcPiePointDrawAndFill twice if the angles cross the 0
static void preArcPiePointDrawAndFill(int xc, int yc, int rx, int ry, float startAngle, float endAngle)
{
   // make sure the values are -359 <= x <= 359
   while (startAngle <= -360) startAngle += 360;
   while (endAngle   <= -360) endAngle   += 360;
   while (startAngle >   360) startAngle -= 360;
   while (endAngle   >   360) endAngle   -= 360;

   if (startAngle > endAngle) // eg 235 to 45
      startAngle -= 360; // set to -45 to 45 so we can handle it correctly
   if (startAngle >= 0 && endAngle <= 0) // eg 135 to -135
      endAngle += 360; // set to 135 to 225

   if (startAngle >= 0 && endAngle >= 0)
      arcPiePointDrawAndFill(xc, yc, rx, ry, startAngle, endAngle,false);
   else
   if (startAngle <= 0 && endAngle >= 0) // eg -45 to 45
   {
      startAngle += 360;
      arcPiePointDrawAndFill(xc, yc, rx, ry, startAngle, 360, false);
      arcPiePointDrawAndFill(xc, yc, rx, ry, 0, endAngle,  true);
   } //else debug("arc/pie/point could not be filled with given angles");
}
//-------------------------------------------------------------------
protected static Object [] getArcPoints(int xc,int yc,int rx,int ry,float startAngle,float endAngle,int flags)
//-------------------------------------------------------------------
{
	int extra = 0;
	preArcPiePointDrawAndFill(xc,yc,rx,ry,startAngle,endAngle);
	int len = endIndex-startIndex+1;
	if ((flags & 0x1) != 0) extra = 1;
	int [] x, y;
	if (len >= 0){
		x = new int[len+extra];
		y = new int[len+extra];
		ewe.sys.Vm.copyArray(xPoints,startIndex,x,extra,len);
		ewe.sys.Vm.copyArray(yPoints,startIndex,y,extra,len);
	}else{
		len = nPoints-startIndex-2;
		x = new int[len+extra+1+endIndex];
		y = new int[len+extra+1+endIndex];
		ewe.sys.Vm.copyArray(xPoints,startIndex,x,extra,len);
		ewe.sys.Vm.copyArray(yPoints,startIndex,y,extra,len);
		ewe.sys.Vm.copyArray(xPoints,0,x,extra+len,endIndex+1);
		ewe.sys.Vm.copyArray(yPoints,0,y,extra+len,endIndex+1);
		/*
		memcpy((int32 *)WOBJ_arrayStart(x)+extra,xPoints+startIndex,len*sizeof(int32));
		memcpy((int32 *)WOBJ_arrayStart(y)+extra,yPoints+startIndex,len*sizeof(int32));
		memcpy((int32 *)WOBJ_arrayStart(x)+len+extra,xPoints,(endIndex+1)*sizeof(int32));
		memcpy((int32 *)WOBJ_arrayStart(y)+len+extra,yPoints,(endIndex+1)*sizeof(int32));
		*/
	}
	Object [] ret = new Object[2];
	ret[0] = x;
	ret[1] = y;
	return ret;
}
//===================================================================
public void drawLines(int x[], int y[], int count)
//===================================================================
{
	if (count <= 1) return;
	if (Image.is12)try {
		java.awt.geom.GeneralPath gp = new java.awt.geom.GeneralPath();
		gp.moveTo(x[0],y[0]);
		for (int i = 1; i < count; i++)
			gp.lineTo(x[i], y[i]);
		draw(gp);
		return;
	}catch(Error e){}
	for (int i = 0; i < count - 1; i++)
		drawLine(x[i], y[i], x[i + 1], y[i + 1]);
}


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
	StringBuffer out = new StringBuffer();
	fillPolygon(xp,yp,xp.length);
}
//-------------------------------------------------------------------
void draw(Shape shape)
//-------------------------------------------------------------------
{
	Graphics2D g2 = (Graphics2D)_g;
	setPen(pen);
	g2.draw(shape);
}
//-------------------------------------------------------------------
void fill(Shape shape)
//-------------------------------------------------------------------
{
	Graphics2D g2 = (Graphics2D)_g;
	setBrush(brush);
	g2.fill(shape);
	setPen(pen);
	g2.draw(shape);
}
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
//public native void fillPolygon(int x[], int y[], int count);
public void fillPolygon(int x[], int y[], int count)
	{

	if (Image.is12)
	try{
		fill(new java.awt.Polygon(x,y,count));
		return;
	}catch(Error e){
	}
	int [] xx = new int[x.length];
	int [] yy = new int[y.length];
	for (int i = 0; i<x.length; i++) xx[i] = x[i];
	for (int i = 0; i<y.length; i++) yy[i] = y[i];
	_g.fillPolygon(xx, yy, count);
}


/**
 * Draws text at the given coordinates. The x and y coordinates specify
 * the upper left hand corner of the text.
 */
//public native void drawText(String s, int x, int y);
public void drawText(String s, int x, int y)
	{
	_g.drawString(getDisplayable(s), x, y + _fontAscent);
	int which = getUnderlined(s);
	if (curFont != null && which != -1)
		underline(curFont,ewe.sys.Vm.getStringChars(s),0,s.length()-2,which,x,y);
	}
private char [] charBuff = new char[1];
public void drawChar(char which,int x,int y,int options)
{
	charBuff[0] = which;
	_g.drawChars(charBuff,0,1,x,y+_fontAscent);
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

private static int [] rx = new int[4], ry = new int[4];
/**
 * Fills a rectangular area with the current color and draws the outline.
 */
//public native void fillRect(int x, int y, int width, int height);

//-------------------------------------------------------------------
private void rectToPolygon(int x, int y, int width, int height)
//-------------------------------------------------------------------
{
	if (Image.is12){
		rx[0] = x; ry[0] = y;
		rx[1] = x+width-1;  ry[1] = y;
		rx[2] = x+width-1; ry[2] = y+height-1;
		rx[3] = x; ry[3] = y+height-1;
	}
}

//===================================================================
public void fillRect(int x, int y, int width, int height)
//===================================================================
{
	if (Image.is12)
	try{
		rectToPolygon(x,y,width,height);
		fill(new java.awt.Polygon(rx,ry,4));
		return;
	}catch(Error e){
	}
	_g.fillRect(x, y, width, height);
}
//===================================================================
public void drawRect(int x, int y, int width, int height)
//===================================================================
{
	if (Image.is12)
	try{
		rectToPolygon(x,y,width,height);
		draw(new java.awt.Polygon(rx,ry,4));
		return;
	}catch(Error e){
	}
	// NOTE: only valid for drawing rects with width >=1, height >= 1
	int x2 = x + width - 1;
	int y2 = y + height - 1;
	drawLine(x, y, x2 - 1, y);
	drawLine(x2, y, x2, y2 - 1);
	drawLine(x2, y2, x + 1, y2);
	drawLine(x, y2, x, y + 1);
}
/**
 * Sets a clipping rectangle. Anything drawn outside of the rectangular
 * area specified will be clipped. Setting a clip overrides any previous clip.
 */
//public native void setClip(int x, int y, int width, int height);
public void setClip(int x, int y, int width, int height)
	{
	try
		{
		// JDK 1.1
		_g.setClip(x, y, width, height);
		}
	catch (NoSuchMethodError e)
		{
		// JDK 1.02
		clearClip();
		_g.clipRect(x, y, width, height);
		}
	}


/**
 * Sets the x, y, width and height coordinates in the rectangle passed
 * to the current clip coordinates. To reduce the use of temporary objects
 * during drawing, this method does not allocate its own rectangle
 * object. If there is no current clip, null will be returned and
 * the rectangle passed will remain unchanged. Upon success, the
 * rectangle passed to the method will be returned.
 */
//public native Rect getClip(Rect r);
public Rect getClip(Rect r)
	{
	if (r == null)
		return null;
	java.awt.Rectangle awtRect;
	try
		{
		// JDK 1.1
		awtRect = _g.getClipBounds();
		}
	catch (NoSuchMethodError e)
		{
		// JDK 1.02
		awtRect = _g.getClipRect();
		}
	if (awtRect == null)
		return null;
	r.x = awtRect.x;
	r.y = awtRect.y;
	r.width = awtRect.width;
	r.height = awtRect.height;
	return r;
	}
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
//public native void setColor(int r, int g, int b);
public void setColor(int r, int g, int b)
	{
	color.set(r,g,b);
	try{
		if (pen == null)
			setPen(new Pen(new Color(r,g,b),Pen.SOLID,1));
		else{
			pen.color = new Color(r,g,b);
			nativeSetPen(pen);
		}
		if (brush == null)
			setBrush(new Brush(new Color(r,g,b),Brush.SOLID));
		else{
			brush.color = new Color(r,g,b);
			nativeSetBrush(brush);
		}
	}catch(Throwable e){
	}
	_g.setColor(new java.awt.Color(r, g, b));
	}

public Color color = new Color(0,0,0);
public Color background = new Color(255,255,255);
public void setBackground(int r,int g,int b)
{
	background.set(r,g,b);
}
public void setBackground(Color c)
{
	background.set(c);
}
public Color getColor()
{
	return color.getCopy();
}
public Color getBackground()
{
	return background.getCopy();
}

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
//public native void setDrawOp(int drawOp);
static boolean drawErrDisplayed = false;
public void setDrawOp(int drawOp)
	{
	if (drawOp != DRAW_XOR)
		{
		_g.setPaintMode();
		_xorDrawMode = false;
		}
	else
		{
		_g.setXORMode(java.awt.Color.white);
		_xorDrawMode = true;
		}
	/*
	if (drawOp != DRAW_XOR && drawOp != DRAW_OVER && !drawErrDisplayed)
		{
		System.out.println("NOTICE: DRAW_AND and DRAW_OR aren't supported under Java");
		drawErrDisplayed = true;
		}
		*/
	}


Font curFont;
/** Sets the current font for operations that draw text. */
//public native void setFont(Font font);
public void setFont(Font font)
	{
	curFont = font;
	java.awt.Font awtFont = font.getAWTFont();
	_g.setFont(awtFont);
	java.awt.FontMetrics fm;
	fm = java.awt.Toolkit.getDefaultToolkit().getFontMetrics(font.getAWTFont());
	_fontAscent = fm.getAscent();
 	}


/**
 * Translates the origin of the current coordinate system by the given
 * x and y values.
 */
//public native void translate(int x, int y);
public void translate(int x, int y)
	{
	_g.translate(x, y);
	_transX += x;
	_transY += y;
	}


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
	nativeSetPen(newPen);
	return was;
}

//===================================================================
//protected native void nativeSetPen(Pen p);
protected void nativeSetPen(Pen p)
{
	if (p != null) 	_g.setColor(new java.awt.Color(p.color.red,p.color.green,p.color.blue));
	try{
		Stroke s = null;
		int cap = 0,join = 0;
		float mitreLimit = p != null ? p.miterLimit : 10;
		if (mitreLimit <= 0) mitreLimit = 10;
		if (p != null) switch(p.style & 0xf00){
			case Pen.CAP_BUTT: cap = BasicStroke.CAP_BUTT; break;
			case Pen.CAP_SQUARE: cap = BasicStroke.CAP_SQUARE; break;
			case Pen.CAP_ROUND: cap = BasicStroke.CAP_ROUND; break;
			default:
				cap = BasicStroke.CAP_BUTT; break;
		}
		if (p != null) switch(p.style & 0xf000){
			case Pen.JOIN_MITER: join = BasicStroke.JOIN_MITER; break;
			case Pen.JOIN_BEVEL: join = BasicStroke.JOIN_BEVEL; break;
			case Pen.JOIN_ROUND: join = BasicStroke.JOIN_ROUND; break;
			default:
				join = BasicStroke.JOIN_MITER; break;
		}
		float[] dashes = null;
		if (p != null) switch(p.style & 0xff){
			case Pen.DASH:
				dashes = new float[]{4.0f,4.0f};
				break;
			case Pen.DOT:
				dashes = new float[]{2.0f,2.0f};
				break;
			case Pen.DASHDOT:
				dashes = new float[]{4.0f,2.0f,2.0f,2.0f};
				break;
			case Pen.DASHDOTDOT:
				dashes = new float[]{4.0f,2.0f,2.0f,2.0f,2.0f,2.0f};
				break;

			default:
				s = new java.awt.BasicStroke(p.thickness,cap,join,mitreLimit);
		}
		if (dashes != null)
			s = new java.awt.BasicStroke(p.thickness,cap,join,mitreLimit,dashes,0);

		if (s == null) s = new java.awt.BasicStroke(1);
		((Graphics2D)_g).setStroke(s);
	}catch(Error e){
		//if (p != null) setColor(p.color);
	}
}
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
//protected native void nativeSetBrush(Brush b);
protected void nativeSetBrush(Brush b)
{
	try{
		if (brush == null)
			((Graphics2D)_g).setPaint(null);
		else {
			Color c = brush.color;
			((Graphics2D)_g).setPaint(new java.awt.Color(c.getRed(),c.getGreen(),c.getBlue()));
		}
	}catch(Error e){}
}
//===================================================================

//MLB
//public native void drawEllipse(int x,int y,int width,int height)
//public native void fillEllipse(int x,int y,int width,int height);
public void drawEllipse(int x,int y,int width,int height)
{
	nativeSetPen(pen);
	_g.drawOval(x,y,width-1,height-1);
}
public void fillEllipse(int x,int y,int width,int height)
{
	nativeSetBrush(brush);
	_g.fillOval(x,y,width,height);
	nativeSetPen(pen);
	_g.drawOval(x,y,width-1,height-1);
}
//public boolean empty = false;
private static Graphics emptyGraphics;
//===================================================================
public static Graphics getEmptyGraphics() {return null;}
//===================================================================
public void setColor(Color c){if (c != null) setColor(c.getRed(),c.getGreen(),c.getBlue());}
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
//public native void draw3DRect(Rect r,int style,boolean flat,Color fill,Color outlineColor);

/*Old version - replaced July 15th 2002
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
*/
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
private static Color innerFill = new Color(0,0,0), innerLine = new Color(0,0,0);
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
			g.setColor(outlineColor);
			if ((edge & BDR_DOTTED) == 0){
			if ((flags & BF_TOP) != 0)
				if (labelWidth == 0) drawLine(r.x,r.y,r.x+r.width-1,r.y);
				else {
					drawLine(r.x,r.y,r.x+4,r.y);
					drawLine(r.x+4+labelWidth,r.y,r.x+r.width-1,r.y);
				}
				//if ((flags & BF_TOP) != 0) drawLine(r.x,r.y,r.x+r.width-1,r.y);
				if ((flags & BF_RIGHT) != 0) drawLine(r.x+r.width-1,r.y,r.x+r.width-1,r.y+r.height-1);
				if ((flags & BF_BOTTOM) != 0) drawLine(r.x+r.width-1,r.y+r.height-1,r.x,r.y+r.height-1);
				if ((flags & BF_LEFT) != 0) drawLine(r.x,r.y+r.height-1,r.x,r.y);
			}else{
				if ((flags & BF_TOP) != 0) drawDots(r.x,r.y,r.x+r.width-1,r.y);
				if ((flags & BF_RIGHT) != 0) drawDots(r.x+r.width-1,r.y,r.x+r.width-1,r.y+r.height-1);
				if ((flags & BF_BOTTOM) != 0) drawDots(r.x+r.width-1,r.y+r.height-1,r.x,r.y+r.height-1);
				if ((flags & BF_LEFT) != 0) drawDots(r.x,r.y+r.height-1,r.x,r.y);
			}
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

//==================================================================
public void drawDiamond(Rect r,int which)
//==================================================================
{
	int x = r.x, y = r.y, w = r.width, h = r.height;
//..................................................................
	if (w % 2 == 1) w--;
	if (h % 2 == 1) h--;
	if (w > h) w = h;
	if (h > w) h = w;
	int half = h/2;
	int [] xx = new int[4], yy = new int[4];
	x += r.width/2-1; y += r.height/2-1;
	int num = 3;
	switch(which){
		case Up:
			xx[0] = x-half+1; xx[1] = x+1; xx[2] = x+half+1;
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
//===================================================================
public void drawText(FontMetrics fm,String [] lines,Rect where,int alignment)
//===================================================================
{
	drawText(fm,lines,where,alignment,CENTER);
}

private static String [] strings = new String[1];

//===================================================================
public static void getSize(FontMetrics fm,String line,Dimension d)
//===================================================================
{
	getSize(fm,line,d,null);
}
//===================================================================
public static void getSize(FontMetrics fm,String line,Dimension d,FormattedTextSpecs fts)
//===================================================================
{
	strings[0] = line;
	getSize(fm,strings,0,1,d,fts);
}
//===================================================================
public static void getSize(FontMetrics fm,String [] lines,int start,int end,Dimension d)
//===================================================================
{
	getSize(fm,lines,start,end,d,null);
}
//===================================================================
public static void getSize(FontMetrics fm,String [] lines,int start,int end,Dimension d,FormattedTextSpecs fts)
//===================================================================
{
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
	d.width = w;
	d.height = h;
}

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
public void drawText(FontMetrics fm,String [] lines,Rect where,int alignment,int anchor,
int startLine,int endLine)
//===================================================================
{
	drawText(fm,lines,where,alignment,anchor,startLine,endLine,null);
}
//===================================================================
public void drawText(FontMetrics fm,String [] lines,Rect where,int alignment,int anchor,
int startLine,int endLine,FormattedTextSpecs fts)
//===================================================================
{
	getSize(fm,lines,startLine,endLine,Dimension.buff,fts);
	Rect tr = Rect.buff; tr.width = Dimension.buff.width; tr.height = Dimension.buff.height; tr.x = tr.y = 0;
	anchor(tr,where,anchor);
	drawTextIn(fm,lines,tr,alignment,startLine,endLine,fts);
}
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

	setPen(null);
	setBrush(null);

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

	setPen(null);
	setBrush(null);

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
public void drawSpecial(int what,Rect where,Pen p,Brush b)
//===================================================================
{
	Pen old = getPen();
	Brush oldb = getBrush();
	switch(what){
		case SPECIAL_TICK:
			setPen(p); //setBrush(b);
			drawLine(where.x+where.width-1,where.y+1,where.x-1,where.y+where.height-1);
			drawLine(where.x-1,where.y+where.height-1,where.x-1,where.y+where.height/3);
			break;
		case SPECIAL_X:
			setPen(p); //setBrush(b);
			drawLine(where.x+where.width-1,where.y,where.x,where.y+where.height-1);
			drawLine(where.x,where.y,where.x+where.width-1,where.y+where.height-1);
			break;
	}
	setPen(old);
	setBrush(oldb);
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
//===================================================================
public static String getDisplayable(String s)
//===================================================================
{
	int len;
	if (s != null)
		if ((len = s.length()) >= 2)
			if (s.charAt(len-1) == '\0')
				return s.substring(0,len-2);
	return s;
}

//===================================================================
public static int getUnderlined(String s)
//===================================================================
{
	int len;
	if (s == null) return -1;
	if ((len = s.length()) < 3) return -1;
  if (s.charAt(len-1) != '\0') return -1;
	char ch = s.charAt(len-2);
	int where = s.indexOf(ch);
	if (where == len-2) where = -1;
	char lc = ewe.sys.Vm.getLocale().changeCase(ch,false);
	if (lc == ch) lc = ewe.sys.Vm.getLocale().changeCase(ch,true);
	if (lc == ch) return where;
	int where2 = s.indexOf(lc);
	if (where == -1) return where2;
	if (where2 == -1) return where;
	if (where < where2) return where;
	return where2;
}
/**
* Call this after you have called drawText().
**/
//===================================================================
public void underline(Object fontOrFontMetrics,char [] text,int start,int length,int underlineStartIndex,int x,int y)
//===================================================================
{
	FontMetrics fm = (fontOrFontMetrics instanceof FontMetrics) ? (FontMetrics)fontOrFontMetrics : getFontMetrics((Font)fontOrFontMetrics);
	int oldWidth = 0, oldStyle = 0;
	Color oldColor = null;
	if (pen != null) {
		oldWidth = pen.thickness;
		pen.thickness = 1;
		oldStyle = pen.style;
		pen.style = pen.SOLID;
		oldColor = pen.color;
		pen.color = color;
		nativeSetPen(pen);
	}
	int before = fm.getTextWidth(text,start,underlineStartIndex-start);
	int after = fm.getCharWidth(text[underlineStartIndex]);
	int height = fm.getHeight()-1;
	drawLine(x+before,y+height,x+before+after-1,y+height);
	if (pen != null){
		pen.thickness = oldWidth;
		pen.style = oldStyle;
		pen.color = oldColor;
		nativeSetPen(pen);
	}
}
/**
* This is a fast way of drawing a solid line of thickness 1 of a particular color.
* This will set the pen/brush of the Graphics to an indeterminate state.
* @see setPixelRGB()
* @see fillRectRGB()
**/
public void drawLineRGB(int x1,int y1,int x2,int y2,int rgb)
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
public void setPixelRGB(int x,int y,int rgb)
{
	drawLineRGB(x,y,x,y,rgb);
}
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
//===================================================================
public void drawImage(Image image,int dx,int dy,int dwidth,int dheight,int sx,int sy,int swidth,int sheight)
//===================================================================
{
	_g.drawImage(image.getAWTImage(),dx,dy,dx+dwidth,dy+dheight,sx,sy,sx+swidth,sy+sheight,null);
}
/*
//===================================================================
public void scale(double xscale, double yscale)
//===================================================================
{
	double[] all = getTransform();
	double[] s = new double[6];
	System.arraycopy(0,0
}
*/
//public static final double[] unityTransform =
//===================================================================
public void setTransform(double[] transforms)
//===================================================================
{
	((Graphics2D)_g).setTransform(transforms == null ? null : new AffineTransform(transforms));
}
//===================================================================
public double[] getTransform(double[] dest)
//===================================================================
{
	if (dest == null) dest = new double[6];
	((Graphics2D)_g).getTransform().getMatrix(dest);
	return dest;
}
//===================================================================
public double[] transform(double[] transform, double[] oldTransform)
//===================================================================
{
	double[] ret = getTransform(oldTransform);
	setTransform(transform);
	return ret;
}
}
