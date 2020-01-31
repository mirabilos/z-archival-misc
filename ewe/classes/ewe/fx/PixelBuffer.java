package ewe.fx;
/**
* A PixelBuffer is used to store ARGB pixel information in an integer array.
* This is unlike an Image, which uses a system dependant image implementation.
* However there are some methods, such as draw() which will cause the pixel buffer
* to create an Image from its data for rendering.
* <p>
* PixelBuffers are generally used for composing complex images with alpha (transparency)
* channels. This may be necessary because the normal draw operations done via a Graphics
* object do not support the writing of alpha channel data. For example, you cannot use
* fillRect() to fill an area of a drawing surface with a particular color that has an alpha
* channel. However using PixelBuffer.addAlphaChannel(Image im,Color transparent) will allow
* you to add an alpha channel to a previous drawn image.
* <p>
* Any Images created out of the data from the PixelBuffer will be considered to have
* an alpha (transparency) channel.
**/
//##################################################################
public class PixelBuffer implements IImage{
//##################################################################
// Don't move the next 3 variables.
int width;
int height;
int [] buffer;
//======================
/**
* The background for use when drawing via draw(). This defaults to White.
**/
public Color background = Color.White;

//-------------------------------------------------------------------
private void checkArea(int x,int y,int w,int h) throws IllegalArgumentException
//-------------------------------------------------------------------
{
	if (x < 0 || y < 0 || w < 0 || h < 0 || x+w > width || y+h > height)
		throw new IllegalArgumentException("The requested area is not within the PixelBuffer");
}

//-------------------------------------------------------------------
private void setup(int width,int height,int [] bufferToUse)
//-------------------------------------------------------------------
{
	//numPixbuffs++;
	//ewe.sys.Vm.debug("++PB: "+numPixbuffs);
	if (width < 0 || height < 0) throw new IllegalArgumentException("width or height is unacceptable.");
	this.width = width;
	this.height = height;
	if (bufferToUse == null) bufferToUse = new int[width*height];
	else if (bufferToUse.length < width*height) throw new IllegalArgumentException("Specified buffer is not big enough.");
	buffer = bufferToUse;
}
//===================================================================
public PixelBuffer(int width,int height,int [] bufferToUse) throws IllegalArgumentException
//===================================================================
{
	setup(width,height,bufferToUse);
}
//===================================================================
public PixelBuffer(int width,int height) throws IllegalArgumentException
//===================================================================
{
	this(width,height,null);
}
//===================================================================
public PixelBuffer(IImage from,Rect area) throws IllegalArgumentException
//===================================================================
{
	this(area.width,area.height,null);
	if (from.getPixels(buffer,0,area.x,area.y,area.width,area.height,0) == null){
		//if (true) throw new IllegalArgumentException("Cannot get pixel data from supplied IImage");
		Graphics g = getDrawingBuffer(null,null,1);
		from.draw(g,-area.x,-area.y,0);
		putDrawingBuffer(PUT_SET);
	}
}
//===================================================================
public PixelBuffer(IImage from) throws IllegalArgumentException
//===================================================================
{
	this(from,new Rect(0,0,from.getWidth(),from.getHeight()));
}

//===================================================================
public PixelBuffer(IImage from,Rect sourceArea,Dimension newSize,Object useBuffer)
//===================================================================
{
	PixelBuffer pb = sourceArea == null ? new PixelBuffer(from) : new PixelBuffer(from,sourceArea);
	if (newSize != null) pb = pb.scale(newSize.width,newSize.height,useBuffer);
	setup(pb.width,pb.height,pb.buffer);
/*
	if (sourceArea == null) sourceArea = new Rect(0,0,from.getWidth(),from.getHeight());
	if (newSize != null) setup(newSize.width,newSize.height,useBuffer);
	else setup(sourceArea.width,sourceArea.height,useBuffer);
	if (newSize == null){
		if (from.getPixels(buffer,0,sourceArea.x,sourceArea.y,sourceArea.width,sourceArea.height,0) == null){
			//if (true) throw new IllegalArgumentException("Cannot get pixel data from supplied IImage");
			Graphics g = getDrawingBuffer(null,null,1);
			from.draw(g,-sourceArea.x,-sourceArea.y,0);
			putDrawingBuffer(PUT_SET);
		}
	}else{

	}
	*/
}
/**
* This exposes the internal buffer that contains the pixel data.
**/
//===================================================================
public int [] getBuffer()
//===================================================================
{
	return buffer;
}
private Image image, drawing;
/**
* If you modify the pixels in the buffer as provided by getBuffer(), then
* call this method to let the buffer know about the changes.
**/
//===================================================================
public void bufferChanged()
//===================================================================
{
	if (image != null) image.free();
	image = null;
}
//===================================================================
public int getWidth() {return width;}
public int getHeight() {return height;}
//===================================================================
public void free()
//===================================================================
{
	if (image != null) image.free();
	if (drawing != null) drawing.free();
	buffer = null;
	image = drawing = null;
}
/**
* This is the same as put(other,x,y,PUT_BLEND).
**/
//===================================================================
public void blend(PixelBuffer other,int x,int y)
//===================================================================
{
	put(other,x,y,PUT_BLEND);
}
/**
* This is used with put() - it alpha blends in the incoming data with this PixelBuffer's data.
**/
public static final int PUT_BLEND = 1;
/**
* This is used with put() - it overwrites this PixelBuffer's data with in the incoming data.
**/
public static final int PUT_SET = 2;

/**
* This is used with put() and the PUT_SET option - it overwrites this PixelBuffer's data with in the incoming data so
* long as the incoming data is NOT FULLY TRANSPARENT (i.e an alpha value of 0).
**/
public static final int PUT_NONTRANSPARENT_ONLY = 0x80000000;
/**
 * This merges the pixels from the other PixelBuffer with the pixels in this PixelBuffer at the
	specified location.
	This will either replace the pixels in this PixelBuffer (if the PUT_SET)
	option is used OR it will blend the incoming pixels with the pixels in this PixelBuffer (if
	the PUT_BLEND) option is used.
 * @param other The other PixelBuffer to merge with this one.
 * @param x The x location where the incoming PixelBuffer should be put.
 * @param y The y location where the incoming PixelBuffer should be put.
 * @param operation This should be either PUT_SET or PUT_BLEND. If PUT_SET is used you can
	also specify PUT_NONTRANSPARENT_ONLY which tells it to replace pixels in this PixelBuffer
	ONLY if the corresponding incoming pixels are not fully transparent.
 */
//===================================================================
public void put(PixelBuffer other,int x,int y,int operation)
//===================================================================
{
	put(other,x,y,operation,null);
}

private Object [] buff = new Object[3];

//-------------------------------------------------------------------
private void checkMask(Mask mask,PixelBuffer pixbuff)
//-------------------------------------------------------------------
{
	if (mask != null)
		if (mask.width != pixbuff.width || mask.height != pixbuff.height)
			throw new IllegalArgumentException("The Mask is not the same size as the PixelBuffer");
}
/**
 * This merges the pixels from the other PixelBuffer with the pixels in this PixelBuffer at the
	specified location.
	This will either replace the pixels in this PixelBuffer (if the PUT_SET)
	option is used OR it will blend the incoming pixels with the pixels in this PixelBuffer (if
	the PUT_BLEND) option is used.
 * @param other The other PixelBuffer to merge with this one.
 * @param x The x location where the incoming PixelBuffer should be put.
 * @param y The y location where the incoming PixelBuffer should be put.
 * @param operation This should be either PUT_SET or PUT_BLEND.
 * @param mask A mask specifying which incoming pixels should be merged with the Pixels in
 * this PixelBuffer.
 */
//===================================================================
public void put(PixelBuffer other,int x,int y,int operation,Mask mask)
//===================================================================
{
	checkMask(mask,other);
	buff[0] = new Rect(x,y,operation,0);
	buff[1] = mask == null ? null : mask.bits;
	pixbufOperation(other,buff,PUT);
	bufferChanged();
}
/**
 * This method will go through the pixel buffer data and set the alpha value of each
 * pixel to the specified value (between 0.0 and 1.0).
* @param alpha The alpha value between 0.0 and 1.0
**/
//===================================================================
public void setAlpha(double alpha)
//===================================================================
{
	setAlpha((Color)null,alpha);
}
/**
 * This method will go through the pixel buffer data and set the alpha value of each
 * pixel to the specified value (between 0.0 and 1.0), EXCEPT for any pixels that is equal
 * to the transparent Color - those pixels will be set to have an alpha value of 0. If this is
 * null, then all pixels will have the alpha value set to the same.
 * @param transparent the transparent Color.
* @param alpha The alpha value between 0.0 and 1.0
*/
//===================================================================
public void setAlpha(Color transparent,double alpha)
//===================================================================
{
	int[] buff = new int[3];
	buff[0] = (int)(alpha*255);
	buff[1] = transparent == null ? 0xff000000 : (Graphics.mapColor(transparent.toInt() & 0xffffff) & 0xffffff);
	buff[2] = transparent == null ? 0xff000000 : (transparent.toInt() & 0xffffff);
	pixbufOperation(buff,null,SET_ALPHA);
	bufferChanged();
}
/**
 * This method will go through the pixel buffer data and set the alpha value of each
 * pixel which is included in the mask. Pixels not in the mask will have their pixel values set to 0.
 * @param mask The pixel mask.
* @param alpha The alpha value between 0.0 and 1.0
*/
//===================================================================
public void setAlpha(Mask mask,double alpha)
//===================================================================
{
	int[] buff = new int[3];
	buff[0] = (int)(alpha*255);
	checkMask(mask,this);
	pixbufOperation(buff,mask.bits,SET_ALPHA);
	bufferChanged();
}

/**
 * This method will go through the pixel buffer data and scale the alpha value of
 * each pixel by the specified amount - not allowing any alpha value to go higher than 1.0. Note
 * that totally transparent pixels (alpha value of 0) will always remain fully transparent.
 */
//===================================================================
public void scaleAlpha(double byHowMuch)
//===================================================================
{
	if (byHowMuch < 0) byHowMuch = 0;
	else if (byHowMuch > 256.0) byHowMuch = 256.0;
	int[] buff = new int[1];
	buff[0] = (int)(byHowMuch*0x100);
	pixbufOperation(buff,null,SCALE_ALPHA);
	bufferChanged();
}
//===================================================================
public Color getBackground()
//===================================================================
{
	return background;
}
/**
* This always returns true.
**/
//===================================================================
public boolean usesAlpha() {return true;}
//===================================================================

private Rect drawBufferArea;
private Color drawBufferColor;
private double drawBufferAlpha;
private Graphics drawBufferGraphics;
/**
 * This will create a drawing surface, initially filled with the supplied transparent color.
 * After calling this method, draw on the surface using the supplied Graphics and when complete
 * call putDrawingBuffer(int operation) to put back the drawing buffer pixels into the original
 PixelBuffer.
 * @param area An area within the pixbuf to use - will default to the entire area if null.
 * @param transparent The initial color to consider the transparent color when putting
	the area back into the PixelBuffer, or Color(80,255,80) if none is supplied.
 * @param alphaValue the alphaValue to set all the non-transparent pixels to (between 0.0 and 1.0).
 * @return a Graphics object that you can use to draw on.
 */
//===================================================================
public Graphics getDrawingBuffer(Rect area,Color transparent,double alphaValue)
//===================================================================
{
	if (drawBufferArea == null) {
		drawBufferArea = new Rect();
		drawBufferColor = new Color(0,0,0);
	}
	if (area == null) drawBufferArea.set(0,0,width,height);
	else drawBufferArea.set(area);
	if (transparent == null) drawBufferColor.set(80,255,80);
	else drawBufferColor.set(transparent);
	drawBufferAlpha = alphaValue;

	if (drawing != null)
 		if (drawing.getWidth() != drawBufferArea.width && drawing.getHeight() != drawBufferArea.height){
			drawing.free();
			drawing = null;
		}

	if (drawing == null) drawing = new Image(drawBufferArea.width,drawBufferArea.height,Image.TRUE_COLOR);

	drawBufferGraphics = new Graphics(drawing);
	drawBufferGraphics.setColor(drawBufferColor);
	drawBufferGraphics.fillRect(0,0,drawBufferArea.width,drawBufferArea.height);
	drawBufferGraphics.setColor(Color.Black);
	return drawBufferGraphics;
}
/**
 * Call this after calling getDrawingBuffer() to update this PixelBuffer with the data you
 * have drawn.
 * This will put the data that was drawn onto the Graphics created by getDrawingBuffer(), using
 * one of the PUT_ operations.
 * @param operation One of PUT_BLEND or PUT_SET
 */
//===================================================================
public void putDrawingBuffer(int operation)
//===================================================================
{
	putDrawingBuffer(operation,null);
}
/**
 * Call this after calling getDrawingBuffer() to update this PixelBuffer with the data you
 * have drawn, in the area specified by the mask.
 * This will put the data that was drawn onto the Graphics created by getDrawingBuffer(), using
 * one of the PUT_ operations.
 * @param operation One of PUT_BLEND or PUT_SET
 * @param mask A mask specifying which pixels should be put. Any pixels not in this area are unchanged.
 */
//===================================================================
public void putDrawingBuffer(int operation,Mask mask)
//===================================================================
{
	image = null;
	if (drawBufferGraphics != null) drawBufferGraphics.free();
	PixelBuffer pb = new PixelBuffer(drawing,new Rect(0,0,drawBufferArea.width,drawBufferArea.height));
	if (mask == null) pb.setAlpha(drawBufferColor,drawBufferAlpha);
	else pb.setAlpha(mask,drawBufferAlpha);
	put(pb,drawBufferArea.x,drawBufferArea.y,operation,mask);
	pb.free();
}

//-------------------------------------------------------------------
private Image toImage(Image image)
//-------------------------------------------------------------------
{
	if (image == null) {
		image = new Image(width,height,/*Image.TRUE_COLOR|*/Image.FOR_DISPLAY);
		image.enableAlpha();
	}
	image.setPixels(buffer,0,0,0,width,height,0);
	return image;
}
/**
* This returns a new ready for display Image with an alpha channel
* from the pixels within this PixelBuffer. Note that the toMImage()
* method is more useful if you intend to display the image, since
* an mImage() displays more effeciently depending on the nature of
* the image.
**/
//===================================================================
public Image toImage()
//===================================================================
{
	Image im = new Image(width,height,Image.FOR_DISPLAY);
	im.enableAlpha();
	toImage(im);
	im.freeze();
	return im;
}
/**
* This returns a new Image (which can be drawn on) from the pixels within this PixelBuffer.
Note that the toMImage()
* method is more useful if you intend to display the image, since
* an mImage() displays more effeciently depending on the nature of
* the image.
**/
//===================================================================
public Image toDrawableImage()
//===================================================================
{
	image = new Image(width,height,Image.TRUE_COLOR);
	image.enableAlpha();
	return toImage(image);
}

/**
* This returns a new ready for display mImage with an alpha channel
* from the pixels within this PixelBuffer. An mImage will display
* more effeciently than an Image depending on the nature of the image.
**/
//===================================================================
public mImage toMImage()
//===================================================================
{
	mImage i = new mImage(toImage(null));
	i.freeSource();
	return i;
}
//===================================================================
public void draw(Graphics g,int x,int y,int options)
//===================================================================
{
	image = toImage(image);
	new mImage(image).draw(g,x,y,options);
}

//===================================================================
public PixelBuffer getArea(int x,int y,int width,int height) throws IllegalArgumentException
//===================================================================
{
	return new PixelBuffer(width,height,getPixels(null,0,x,y,width,height,0));
}

//===================================================================
public PixelBuffer scale(int newWidth,int newHeight,Object useBuffer)
//===================================================================
{
	return scale(newWidth,newHeight,null,0,useBuffer);
}

//===================================================================
public PixelBuffer scale(int newWidth,int newHeight)
//===================================================================
{
	return scale(newWidth,newHeight,null,0,null);
}
/**
Convert the PixelBuffer to a Mask where all black pixels are opaque and white
pixels are transparent.
**/
//===================================================================
public Mask toMask()
//===================================================================
{
	Mask m = new Mask(width,height);
	m.fromImageMask(buffer);
	return m;
}
/**
* An option for scale().
**/
public static final int SCALE_KEEP_ASPECT_RATIO = 0x1;

/**
 * Scale the data in the PixelBuffer into an output buffer (an array of Ints).
 * @param useBuffer An optional re-usable buffer to use for the data calculations. It can
	be a ByteArray or a byte [] object. If it is null a new one will be created and returned.
 * @param newWidth The width of the new scaled PixelBuffer.
 * @param newHeight The height of the new scaled PixelBuffer.
 * @param sourceArea An optional rectangle specifying the source area in this PixelBuffer.
	If it is null then entire area is used.
 * @param scaleOptions This can be zero or SCALE_KEEP_ASPECT_RATIO.
 * @return The new scaled PixelBuffer.
 * @exception IllegalArgumentException If there was an error with any of the arguments.
 */
	/*
//===================================================================
public Object scale(Object outputBuffer,int newWidth,int newHeight,Rect sourceArea,int scaleOptions) throws IllegalArgumentException
//===================================================================
{
	if (sourceArea == null) sourceArea = new Rect(0,0,width,height);
	else checkArea(sourceArea.x,sourceArea.y,sourceArea.width,sourceArea.height);
	if (newWidth < 0 || newHeight < 0) throw new IllegalArgumentException();
	int [] dest;
	if (useBuffer instanceof int []) dest = (int [])useBuffer;
	else if (useBuffer instanceof ewe.util.IntArray) {
		ewe.util.IntArray ia = (ewe.util.IntArray)useBuffer;
		if (ia.data.length < newWidth * newHeight) ia.data = new int[newWidth * newHeight];
		dest = ia.data;
	}else
		dest = new int[newWidth * newHeight];
	if (dest.length < newWidth * newHeight) throw new IllegalArgumentException();
	if ((scaleOptions & SCALE_KEEP_ASPECT_RATIO) != 0){
		double xscale =(double)newWidth/width;
		double yscale = (double)newHeight/height;
		double scale = Math.min(xscale,yscale);
		newWidth = (int)(scale*width);
		newHeight = (int)(scale*height);
		if (newWidth < 1) newWidth = 1;
		if (newHeight < 1) newHeight = 1;
	}
	if (newWidth > 0 && newHeight > 0){
		Object [] p = new Object[2];
		p[0] = sourceArea;
		p[1] = new Rect(scaleOptions,0,newWidth,newHeight);
		pixbufOperation(dest,p,SCALE);
	}
	dest;
}
*/
/**
 * Scale the PixelBuffer to produce a new PixelBuffer - optionally scaling only a portion of
	the original PixelBuffer.
 * @param newWidth The width of the new PixelBuffer.
 * @param newHeight The height of the new PixelBuffer.
 * @param sourceArea An optional rectangle specifying the source area in this PixelBuffer.
	If it is null then entire area is used.
 * @param scaleOptions This can be zero or SCALE_KEEP_ASPECT_RATIO.
 * @param useBuffer An optional re-usable buffer to use for the data calculations. It can
	be a ByteArray or a byte [] object or it can be a PixelBuffer. If useBuffer is a PixelBuffer
	its data buffer will be used (if big enough) and its width and height will be adjusted
	and that PixelBuffer will be returned.
 * @return The new scaled PixelBuffer or the useBuffer argument if it was a PixelBuffer.
 * @exception IllegalArgumentException If there was an error with any of the arguments.
 */
//===================================================================
public PixelBuffer scale(int newWidth,int newHeight,Rect sourceArea,int scaleOptions,Object useBuffer) throws IllegalArgumentException
//===================================================================
{
	if (sourceArea == null) sourceArea = new Rect(0,0,width,height);
	else checkArea(sourceArea.x,sourceArea.y,sourceArea.width,sourceArea.height);
	//
	if (newWidth < 0 || newHeight < 0) throw new IllegalArgumentException();
	//
	if ((scaleOptions & SCALE_KEEP_ASPECT_RATIO) != 0){
		double xscale =(double)newWidth/width;
		double yscale = (double)newHeight/height;
		double scale = Math.min(xscale,yscale);
		newWidth = (int)(scale*width);
		newHeight = (int)(scale*height);
		if (newWidth < 1) newWidth = 1;
		if (newHeight < 1) newHeight = 1;
	}
	//
	int [] dest;
	if (useBuffer instanceof int []) dest = (int [])useBuffer;
	else if (useBuffer instanceof ewe.util.IntArray) {
		ewe.util.IntArray ia = (ewe.util.IntArray)useBuffer;
		if (ia.data.length < newWidth * newHeight) ia.data = new int[newWidth * newHeight];
		dest = ia.data;
	}else if (useBuffer instanceof PixelBuffer){
		PixelBuffer pb = (PixelBuffer)useBuffer;
		if (pb.buffer == null || pb.buffer.length < newWidth*newHeight)
			pb.buffer = new int[newWidth*newHeight];
		pb.width = newWidth;
		pb.height = newHeight;
		dest = pb.buffer;
	}else
		dest = new int[newWidth * newHeight];
	//
	if (dest.length < newWidth * newHeight) throw new IllegalArgumentException();
	if (newWidth > 0 && newHeight > 0){
		Object [] p = new Object[2];
		p[0] = sourceArea;
		p[1] = new Rect(scaleOptions,0,newWidth,newHeight);
		pixbufOperation(dest,p,SCALE);
	}
	PixelBuffer ret = useBuffer instanceof PixelBuffer ? (PixelBuffer)useBuffer : new PixelBuffer(newWidth,newHeight,dest);
	ret.background = background;
	return ret;
}
/**
* This is a transformation for the transform() method.
**/
public static final int TRANSFORM_ROTATE_90 = 1;
/**
* This is a transformation for the transform() method.
**/
public static final int TRANSFORM_ROTATE_180 = 2;
/**
* This is a transformation for the transform() method.
**/
public static final int TRANSFORM_ROTATE_270 = 3;
/**
* This is a transformation for the transform() method.
**/
public static final int TRANSFORM_MIRROR_HORIZONTAL = 4;
/**
* This is a transformation for the transform() method.
**/
public static final int TRANSFORM_MIRROR_VERTICAL = 5;
/**
* This does one of a number of specific transformations on the PixelBuffer.
* @param transformation One of the TRANSFORM_XXX values.
* @param useBuffer An optional byte [] or ByteArray object to use as the buffer for the
* newly created PixelBuffer
* @return A new PixelBuffer holding the transformed image (this may be of different dimensions
* to the original).
*/
//===================================================================
public PixelBuffer transform(int transformation,Object useBuffer)
//===================================================================
{
	int newWidth = width, newHeight = height;
	if (transformation == TRANSFORM_ROTATE_90 || transformation == TRANSFORM_ROTATE_270){
		newWidth = height;
		newHeight = width;
	}
	int [] dest;
	if (useBuffer instanceof int []) dest = (int [])useBuffer;
	else if (useBuffer instanceof ewe.util.IntArray) {
		ewe.util.IntArray ia = (ewe.util.IntArray)useBuffer;
		if (ia.data.length < newWidth * newHeight) ia.data = new int[newWidth * newHeight];
		dest = ia.data;
	}else
		dest = new int[newWidth * newHeight];
	if (dest.length < newWidth * newHeight) throw new IllegalArgumentException();
	pixbufOperation(dest,null,TRANSFORM+transformation);
	return new PixelBuffer(newWidth,newHeight,dest);
}
//===================================================================
public int [] getPixels(int []dest,int offset,int x,int y,int w,int h,int options) throws IllegalArgumentException
//===================================================================
{
	checkArea(x,y,w,h);
	return getPixelArea(buffer,width,height,dest,offset,x,y,w,h);
}
//-------------------------------------------------------------------
private static int [] getPixelArea(int [] source,int sourceWidth,int sourceHeight,int [] dest,int destOffset,int x,int y,int w,int h)
//-------------------------------------------------------------------
{
	//if (x < 0 || y < 0 || w < 0 || h < 0 || x+w > sourceWidth || y+h > sourceHeight) throw new IllegalArgumentException();
	if (dest == null) dest = new int[w*h+destOffset];
	else if (dest.length < destOffset+w*h) throw new IllegalArgumentException();
	if (w == sourceWidth && h == sourceHeight && x == 0 && y == 0)
		ewe.sys.Vm.copyArray(source,0,dest,destOffset,w*h);
	else if (w != 0 && h != 0){
		int so = y*sourceWidth+x;
		int doff = destOffset;
		for (int yy = 0; yy < h; yy++){
			ewe.sys.Vm.copyArray(source,so,dest,destOffset+doff,w);
			so += sourceWidth;
			doff += w;
		}
	}
	return dest;
}
//-------------------------------------------------------------------
private static boolean hasNative = true;
//-------------------------------------------------------------------
private static final int SET_ALPHA = 1;
private static final int SCALE_ALPHA = 2;
private static final int PUT = 3;
private static final int SCALE = 4;
private static final int TRANSFORM = 10;
/*
private static Object[] pars;
private static int[] intPars;
//-------------------------------------------------------------------
private static Object[] getPars()
//-------------------------------------------------------------------
{
	if (pars == null) pars = new Object[10];
	if (intPars == null) intPars = new int[10];
	return pars;
}
//-------------------------------------------------------------------
private static int [] getIntPars()
//-------------------------------------------------------------------
{
	getPars();
	return intPars;
}
*/
//-------------------------------------------------------------------
static private final int blend(int as,int ad,int s,int d,int shift)
//-------------------------------------------------------------------
{
	int cs, cd, ascs, adcd;
	ascs = (((s >> shift) & 0xff)*as) >> 8; cd = (d >> shift) & 0xff;
	if (ad == 0xff) ascs += cd-((cd *as) >> 8);
	else if (ad != 0){
		adcd = (cd*ad) >> 8; ascs += adcd; adcd = (adcd*as)>>8; ascs -= adcd;
	}
	if (ascs < 0) ascs = 0;
	else ascs &= 0xff;
	return	ascs << shift;
}
//-------------------------------------------------------------------
private int pixbufOperation(Object par1,Object par2,int operation)
//-------------------------------------------------------------------
{
	if (hasNative)
 	try{
		return nativePixbufOperation(par1,par2,operation);
	}catch(UnsatisfiedLinkError e){
		hasNative = false;
	}catch(SecurityException e){
		hasNative = false;
	}

	if (operation > TRANSFORM){
		int [] src = this.buffer;
		int [] dest = (int [])par1;
		int sx, sy, dx, dy, soff, doff;
		int height = this.height;
		int width = this.width;
		switch(operation-TRANSFORM){
			case 1: // Rotate 90
				soff = 0;
				for (sy = 0; sy<height; sy++){
					doff = height-sy-1;
					for (sx = 0; sx<width; sx++){
						dest[doff] = src[soff++];
						doff += height;
					}
				}
				break;
			case 2: // Rotate 180
				soff = 0;
				for (sy = 0; sy<height; sy++){
					doff = (height-sy)*width;
					for (sx = 0; sx<width; sx++){
						dest[--doff] = src[soff++];
					}
				}
				break;
			case 3: // Rotate 270
				soff = 0;
				for (sy = 0; sy<height; sy++){
					doff = ((width-1)*height)+sy;
					for (sx = 0; sx<width; sx++){
						dest[doff] = src[soff++];
						doff -= height;
					}
				}
				break;

			case 4: // HMirror
				soff = 0;
				for (sy = 0; sy<height; sy++){
					doff = soff+width;
					for (sx = 0; sx<width; sx++)
						dest[--doff] = src[soff++];
				}
				break;
			case 5: // VMirror
				soff = 0;
				for (sy = 0; sy<height; sy++){
					doff = (height-1-sy)*width;
					for (sx = 0; sx<width; sx++)
						dest[doff++] = src[soff++];
				}
				break;

			default: return 0;
		}
		return 1;
	}

	switch(operation){


	case PUT: {
		PixelBuffer other = (PixelBuffer)par1;
		Object [] pbuff = (Object [])par2;
		Rect p2 = (Rect)pbuff[0];
		byte [] masks = (byte [])pbuff[1];
		int width = this.width, height = this.height;
		int[] dest = this.buffer;
		int[] src = other.buffer;
		int x = p2.x, y = p2.y, op = p2.width & ~PUT_NONTRANSPARENT_ONLY, w = other.width, h = other.height;
		boolean to = (p2.width & PUT_NONTRANSPARENT_ONLY) != 0;
		int so = 0;
		for (int yy = 0; yy < h; yy++){
			int off = (yy+y)*width+x;
			int bpl = (w+7)/8;
			int moff = bpl*yy-1;
			byte by = 0;
			byte mask = (byte)0x01;
			for (int xx = 0; xx < w; xx++){
				if (masks != null){
					mask = (byte)((mask >> 1) & 0x7f);
					if (mask == 0) {
						mask = (byte)0x80;
						moff++;
						by = masks[moff];
					}
					if ((by & mask) == 0) {
						so++;
						off++;
						continue;
					}
				}
				int s = src[so++];
				if (op == PUT_SET){
					if (!to || ((s & 0xff000000) != 0)) dest[off] = s;
				}else if (op == PUT_BLEND){
					int as = (s >> 24) & 0xff;
					if (as == 0xff) dest[off] = s;
					else if (as == 0)
						;
					else{
						int d = dest[off], ad = (d >> 24) & 0xff;
						int save = 0;
						save |= blend(as,ad,s,d,16);
						save |= blend(as,ad,s,d,8);
						save |= blend(as,ad,s,d,0);
						save |= blend(as,ad,0xff000000,0xff000000,24);
						dest[off] = save;

					}
				}
				off++;
			}
		}
	}
	break;
	case SET_ALPHA:{
		int[] dest = this.buffer;
		int[] p = (int [])par1;
		byte[] masks = (byte [])par2;
		int len = dest.length;
		int alpha = p[0], ashift = 0;
		if (alpha < 0) alpha = 0;
		else if (alpha > 255) alpha = 255;
		ashift = alpha << 24;
		if (masks != null){
			int bpl = (width+7)/8;
			int y = 0, i = 0;
			for (y = 0; y<height; y++){
				int off = bpl*y-1;
				byte by = 0;
				byte mask = (byte)0x01;
				int x;
				for (x = 0; x<width; x++){
					mask = (byte)((mask >> 1) & 0x7f);
					if (mask == 0) {
						mask = (byte)0x80;
						off++;
						by = masks[off];
					}
					if ((by & mask) != 0)
						dest[i] = (dest[i] & 0xffffff)|ashift;
					i++;
				}
			}
		}else{
			int tc1 = 0, tc2 = 0;
			boolean hasColor = (tc1 & 0xff000000) == 0;
			tc1 = p[1];
			tc2 = p[2];
			for (int i = 0; i<len; i++){
				int d = dest[i];
				int d2 = d & 0xffffff;
				if (hasColor){
					if (d2 != tc1 && d2 != tc2) dest[i] = d2 | ashift;
					else
						dest[i] = d2;
				}else
					dest[i] = d2 | ashift;
			}
		}
	break;
	}
	case SCALE_ALPHA:{
		int[] dest = this.buffer;
		int[] p = (int [])par1;
		int len = dest.length;
		int alpha = p[0];
		for (int i = 0; i<len; i++){
			int d = dest[i];
			int a = (((d >> 24) & 0xff)*alpha) >> 8;
			if (a > 0xff) a = 0xff;
			dest[i] = (d & 0xffffff) | (a << 24);
		}
	break;
	}
	case SCALE:{
		int [] dest = (int[])par1;
		int [] src = this.buffer;
		Object [] pars = (Object [])par2;
		Rect srcRect = (Rect)pars[0];
		Rect dstRect = (Rect)pars[1];
		int h = dstRect.height, w = dstRect.width;
		int sx = srcRect.x, sy = srcRect.y;
		int sh = srcRect.height, sw = srcRect.width;
		double xsc = (double)sw/(double)w;
		double ysc = (double)sh/(double)h;
		double y = 0;
		int off = 0;
		for (int line = 0; line < h; line++, y += ysc){
			if (y >= sh) y = sh-1;
			int srcOff = ((int)y+sy)*width;
			double x = 0;
			for (int col = 0; col < w; col++, x += xsc){
				if (x >= sw) x = sw-1;
				dest[off++] = src[srcOff+((int)x+sx)];
			}
		}
	break;
	}

	}
	return 0;
}

//-------------------------------------------------------------------
private native int nativePixbufOperation(Object par1,Object par2,int operation);
//-------------------------------------------------------------------
/**
* This will attempt to get pixels for an IImage. If the IImage cannot provide the pixels directly,
* then a PixelBuffer will be created and the IImage drawn on it. Then the pixels from the pixel
* buffer will be returned.
**/
//===================================================================
public static int [] getPixelsFor(IImage image,int [] dest,int offset,Rect area,int options,Color substituteBackground)
//===================================================================
{
	PixelBuffer pb = new PixelBuffer(area.width,area.height);
	Graphics g = pb.getDrawingBuffer(null,substituteBackground,1);
	image.draw(g,-area.x,-area.y,0);
	pb.putDrawingBuffer(PUT_SET);
	int []ret = pb.getPixels(dest,offset,0,0,area.width,area.height,0);
	pb.free();
	return ret;
}
/**
* This gets a section of an Image, starting at a particular point and in a shape represented by Mask.
* @param image The original image.
* @param m The mask representing the bits of the Image that should be taken. This Mask can be
smaller than the full image - the p parameter specifies where in the image the section should
be taken from.
* @param p The point in the Image where the section should be taken from.
* @param alphaValue An alpha value to be applied to all pixels taken from the Image.
* @return A new PixelBuffer that contains the pixels taken from the Image. All pixels which
* were excluded from the Mask will be fuly transparent.
*/
//===================================================================
public static PixelBuffer getImageSection(IImage image,Mask m,Point p,double alphaValue)
//===================================================================
{
	PixelBuffer pb = new PixelBuffer(m.width,m.height);
	Graphics g = pb.getDrawingBuffer(null,null,alphaValue);
	image.draw(g,-p.x,-p.y,0);
	pb.putDrawingBuffer(PUT_SET,m);
	return pb;
}
/**
* This creates a native Icon to be used in (say) a TaskbarWindow or Window, given any
* IImage object.
**/
//===================================================================
public static Object toIcon(IImage image)
//===================================================================
{
	PixelBuffer pb = new PixelBuffer(image);
	Image im = pb.toImage();
	Mask mask = new Mask(image.getWidth(),image.getHeight());
	mask.fromImage(im);
	Image m = mask.toImageMask();
	Object ret = im.toIcon(m);
	pb.free();
	return ret;
}
/*
//===================================================================
public static Image openImage(Object streamOrByteArray,Dimension requestedSize,Dimension fullSize,boolean forceScale)
//===================================================================
{

}
*/
/*
static int numPixbuffs;
public void finalize()
{
	numPixbuffs--;
	ewe.sys.Vm.debug("--PB: "+numPixbuffs);
}
*/
//##################################################################
}
//##################################################################

