/*********************************************************************************
 *  Ewe Virtual Machine - Version 1.14, January 24, 2002                         *
 *  Copyright (C) 1999-2002 Michael L Brereton <michael_brereton@ewesoft.com>    *
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
import ewe.util.ByteEncodable;
import ewe.util.ByteDecodable;
import ewe.util.ByteArray;
import ewe.util.Utils;
//import ewe.ui.formatted.TextFormatter;
import ewe.reflect.WeakReference;
/**
* An mImage is a versatile implementation of IImage. One of its major uses is to
* display Images with transparent or partially transparent areas. This is needed
* because the Graphics class does not draw transparent images unless the
* DRAW_ALPHA mode is specified or unless one of the special drawImage() methods are
* used that use an image mask (itself an image) or a transparent color to display
* certain areas of the image as being transparent.<p>
*
* You can also inherit from mImage and override the doDraw() method to custom draw your Image.
**/

//##################################################################
public class mImage implements OnScreenImage, ByteEncodable{//extends DataValue{
//##################################################################
/**
* The base image for the mImage.
**/
public Image image;
/**
* An image mask used for the image. Don't set this directly, instead use setImage(Image image,Image mask) - using setMask()
* will create the appropriate Image mask for use when drawing.
**/
public Image mask;
/**
* A bitmask used for the image. Don't set this directly, instead use setImage(Image image,Mask mask) - using setMask()
* will create the appropriate Image mask for use when drawing.
**/
public Mask bitmask;
/**
* If this is set, then the mImage will draw itself using this other IImage.
**/
public IImage drawable;
/**
* This is used for setting properties of the mImage. Properties should ORed together and stored
* in this variable.
**/
public int properties;
public Color transparentColor;// = Color.White;
public Color backgroundColor;
/**
* A property bit - The image is being prepared.
*/
static public final int IsPreparing = 0x1;
/**
* A property bit - The image is fully prepared.
*/
static public final int IsPrepared = 0x2;
/**
* A property bit - The image is invisible
*/
static public final int IsInvisible = 0x4;
/**
* A property bit - The image has a border.
*/
static public final int HasBorder = 0x8;
/**
* A property bit - The image is empty.
*/
static public final int IsEmpty = 0x10;
/**
* A property bit - The image is always on top.
*/
static public final int AlwaysOnTop = 0x20;
/**
* A property bit - Used by MosaicPanel
*/
static public final int InUse = 0x40;
/**
* A property bit - The image has no hot area.
*/
static public final int IsNotHot = 0x100;
/**
* A property bit - The image is moveable (draggable).
*/
static public final int IsMoveable = 0x200;
/**
* A property bit - The image is locked - not used yet.
*/
static public final int IsLocked = 0x400;
/**
* A property bit - Another image must be completely within this image to be considered "within"
* this image.
*/
static public final int CompletelyIn = 0x800;
/**
* A property bit - The image has moved, this is normally set and cleared automatically.
*/
static public final int HasMoved = 0x1000;
/**
* A property bit - The image has changed, this is normally set and cleared automatically.
*/
static public final int HasChanged = 0x2000;
/**
* A property bit - The image is not still, it is animated.
*/
static public final int IsNotStill = 0x4000;
/**
* A property bit - The image stays on the same spot on the screen, even if the mosaic scrolls.
*/
static public final int RelativeToOrigin = 0x8000;
/**
* A property bit - In order for a "drag over" event to be recognized, the mouse pointer must be over the image.
* That is to say, if you are dragging another image over this one, the mouse pointer itself must be over this image
* for it to be considered dragged over.
*/
static public final int MouseMustBeOver = 0x10000;
/**
* A property bit - When refreshing this image, the associated Mosaic will scroll itself to keep the image fully visible (if possible).
* In order for this to work the virtualSize of the Mosaic must be set.
*/
static public final int KeepOnScreen = 0x20000;
/**
* A property bit - indicates that this image will be animated and so the ImageRefresher
* should be set if it is being placed into a Control.
*/
static public final int IsAnimated = 0x40000;

//==================================================================
public Rect location = new Rect(0,0,0,0);
public Rect lastDrawn = new Rect(0,0,-1,-1);

/**
* Get the X,Y location of the mImage.
* @param dest An optional destination Point object.
* @return The dest object or a new Point if dest is null.
**/
//===================================================================
public Point getLocation(Point dest)
//===================================================================
{
	dest = Point.unNull(dest);
	dest.x = location.x;
	dest.y = location.y;
	return dest;
}
/**
* Get the width and height of the mImage.
* @param dest An optional destination Dimension object.
* @return The dest object or a new Dimension if dest is null.
*/
//===================================================================
public Dimension getSize(Dimension dest)
//===================================================================
{
	dest = Dimension.unNull(dest);
	dest.width = location.width;
	dest.height = location.height;
	return dest;
}

public int getHeight() {return location.height;}
public int getWidth() {return location.width;}

public int drawMode = Graphics.DRAW_OVER;
//==================================================================
protected Image sourceImage, sourceMask;
//==================================================================
/**
* This will free the source image IF it is not being directly used by the mImage.
* Sometimes, when you create an mImage OR if you use setImage(), the mImage does not
* use the actual provided image, but may use a modified version of it. If this is
* the case then this tells the mImage to free the original image.<p>
* You would normally call this after creating the mImage, if you are sure the source Image
* is not to be used again. <p>
**/
//===================================================================
public void freeSource()
//===================================================================
{
	if (sourceImage != null && sourceImage != image){
		sourceImage.free();
		//sourceImage = null;
	}
	if (sourceMask != null && sourceMask != mask){
		sourceMask.free();
		//sourceMask = null;
	}
}

//-------------------------------------------------------------------
protected void freeIfNotOriginal()
//-------------------------------------------------------------------
{
	if (sourceImage != image) {
		if (image != null) image.free();
		if (mask != null) mask.free();
		if (drawable != null) drawable.free();
	}
}
/**
 * Create a new mImage(). Use one of the setImage() methods after creation to specify the display image.
 */
public mImage() {}
/**
 * Create a new mImage() with the named image and mask.
 * @param mi The name of the image.
 * @param mm The name of the mask - which can be null.
*  @exception IllegalArgumentException if the image named by mi or mm is invalid.
*/
public mImage(String mi,String mm)  throws IllegalArgumentException
{super(); Image mk = mm == null ? null : new Image(mm); setImage(new Image(mi),mk); mk.free(); freeSource();}
/**
 * Create a new mImage() with the named image and specified transparent color.
 * @param mi The name of the image.
 * @param trans The transparent color.
*  @exception IllegalArgumentException if the image named by mi is invalid.
 */
public mImage(String mi,Color trans) throws IllegalArgumentException
{super(); setImage(new Image(mi)); transparentColor = trans; freeSource();}
/**
 * Create a new mImage() with the named image.
 * @param mi The name of the image.
*  @exception IllegalArgumentException if the image named by mi is invalid.
 */
public mImage(String mi) throws IllegalArgumentException
{setImage(new Image(mi),0); freeSource();}
/**
 * Create a new mImage() for a specified IImage.
 * @param drawable The drawable
 */
//===================================================================
public mImage(IImage drawable)
//===================================================================
{
	this(drawable,0);
}
/**
 * Create a new mImage() for the specified IImage.
 * @param drawable The drawable.
 * @param drawMode The draw mode for drawing. If this is Graphics.DRAW_ALPHA and the drawable
 * has an alpha channel, then alpha blending will be used for drawing - if supported by the underlying
 * system.
 */
//===================================================================
public mImage(IImage drawable,int drawMode)
//===================================================================
{
	if (drawable == null) {
		this.drawMode = drawMode;
		return;
	}
	if (drawable instanceof Image){
 		setImage((Image)drawable,drawMode);
	}else{
		if (drawMode == 0) drawMode = Graphics.DRAW_OVER;
		this.drawMode = drawMode;
		setImage(drawable);
	}
}
//===================================================================
public mImage(Image image,int drawMode,boolean imageMayChange)
//===================================================================
{
	setImage(image,drawMode,imageMayChange);
}
//==================================================================
public Object getNew() {return new mImage();}
//==================================================================

//===================================================================
public Object getCopy()
//===================================================================
{
	mImage mi = (mImage)getNew();
	mi.copyFrom(this);
	return mi;
}
//===================================================================
public void drawn(Rect where)
//===================================================================
{
	if (where == null) lastDrawn.set(0,0,-1,-1);
	else lastDrawn.set(where);
}
//===================================================================
public void setImage(Image im,Mask mask)
//===================================================================
{
	mask.toMImage(im,0,0,this);
	sourceImage = im;
}
//===================================================================
public void setImage(Image im,Color transparent)
//===================================================================
{
	setImage(im);
	transparentColor = transparent;
}
//===================================================================
public void setImage(IImage im)
//===================================================================
{
	if (im instanceof mImage) {
		setMImage((mImage)im);
		return;
	}else if (im instanceof Image){
		setImage((Image)im);
	}else
		drawable = im;
	location.width = im.getWidth();
	location.height = im.getHeight();
	backgroundColor = im.getBackground();
	properties &= ~HasChanged;
}
//-------------------------------------------------------------------
void setMask(Image mask)
//-------------------------------------------------------------------
{
	this.mask = Image.invert(sourceMask = mask);
	fixColors();
}
/**
 * This sets the mask image for the mImage. The actual value of the "mask"
 * field may not be the provided mask. The actual mask is frequently the
 * inversion of this mask.
 * @param msk The user image mask with black pixels for opaque and white pixels for transparent.
 */
//==================================================================
public void setImage(Image im,Image msk)
//==================================================================
{
	freeIfNotOriginal();
	sourceImage = image = im;
	setMask(msk);
	imageSet();
}
//==================================================================
public void setImage(Image im,int drawMode)
//==================================================================
{
	setImage(im,drawMode,false);
}

//-------------------------------------------------------------------
protected boolean imageMayChange;
//-------------------------------------------------------------------

//==================================================================
public void setImage(Image im,int drawMode,boolean imageMayChange)
//==================================================================
{
	this.imageMayChange = imageMayChange;
	if (drawMode == 0){
		if (im.hasAlpha && im.transparent == null && Graphics.canAlphaBlend)
			drawMode = Graphics.DRAW_ALPHA;
		else
			drawMode = Graphics.DRAW_OVER;
	}
	this.drawMode = drawMode;
	setImage(im);
}
/**
 * Set the Image for the mImage.
 * @param im The image to set.
 */
//==================================================================
public void setImage(Image im)
//==================================================================
{
	freeIfNotOriginal();
	sourceImage = image = im;
	if (im != null) {
		transparentColor = im.transparent;
		backgroundColor = im.background;
		if (im.hasAlpha && transparentColor == null){
			if (drawMode == Graphics.DRAW_ALPHA){
				if (!Mask.hasTrueAlpha(im) && !imageMayChange) {
					drawMode = Graphics.DRAW_OVER;
				}
			}
			if (drawMode != Graphics.DRAW_ALPHA){
				Mask mk = new Mask(im.getWidth(),im.getHeight());
				if (mk.fromImage(im)){
					mk.toMImage(im,0,0,this);
				}
				mk = null;
				sourceImage = im;
			}
		}
	}
	imageSet();
}
//===================================================================
public Color getBackground() {return backgroundColor;}
//===================================================================

//==================================================================
public void setMImage(mImage other)
//==================================================================
{
	freeIfNotOriginal();
	sourceImage = image = other.image;
	sourceMask = mask = other.mask;
	transparentColor = other.transparentColor;
	drawMode = other.drawMode;
	backgroundColor = other.backgroundColor;
	bitmask = other.bitmask;
	drawable = other.drawable;

	if (image == null && drawable == null) drawable = other;
	imageSet();
/*
	transparentColor = null;
	drawMode = DRAW_OVER;
	if (other.mask != null) {
		image = other.image;
		mask = other.mask;
		imageSet();
	}else if (other.transparentColor != null){
		image = other.image;
		transparentColor = other.transparentColor;
		imageSet();
	}else if (other.image != null)
		setImage(other.image,other.drawMode);
	else if (other.drawable != null){
		setImage(other.drawable);
	}else {
		drawable = other;
		imageSet();
	}
*/
}

//==================================================================
public void imageSet()
//==================================================================
{
	IImage i = drawable;
	if (i == null) i = image;
	if (i != null){
		location.width = i.getWidth();
		location.height = i.getHeight();
	}
	if (image != null && image != sourceImage) image.freeze();
	if (mask != null && mask != sourceMask) mask.freeze();
	properties &= ~HasChanged;
}
//==================================================================
public void copyFrom(Object other)
//==================================================================
{
	mImage from = (mImage)other;
	setImage(from);
	properties = from.properties;
}

/**
* Draws the border of the image.
* If the image has a border it will be drawn.
*/
public synchronized void
//==============================================================
	drawBorder(Graphics g,int x,int y,int options)
//==============================================================
{
	Graphics clipped = g;//.create();
	//clipped.clipRect(x,y,dim.width,dim.height);
	if ((properties & HasBorder) != 0) {
		clipped.setColor(0,0,0);
		clipped.setDrawOp(Graphics.DRAW_XOR);
		clipped.drawLine(x,y,x+location.width-1,y);
		clipped.drawLine(x,y,x,y+location.height-1);
		clipped.drawLine(x+location.width-1,y,x+location.width-1,y+location.height-1);
		clipped.drawLine(x,y+location.height-1,x+location.width-1,y+location.height-1);
		clipped.setDrawOp(Graphics.DRAW_OVER);
		//clipped.drawRect(x,y,dim.width-1,dim.height-1);
	}
	//clipped.dispose();
}
/**
* Bottom level draw. Override to change how the image is drawn. Will still
* allow borders to be drawn and allow invisible to have effect.
*/
public void
//==============================================================
	doDraw(Graphics g,int options)
//==============================================================
{
	/*
	if (mask != null){
		g.setDrawOp(g.DRAW_AND);
		g.drawImage(mask,0,0);
		g.setDrawOp(g.DRAW_OR);
	}else
		g.setDrawOp(drawMode);
	g.drawImage(image,0,0);
	g.setDrawOp(g.DRAW_OVER);
	if (true) return;
	*/
	if ((options & DISABLED) != 0){
		Image img = null;
		if (drawable instanceof Image) img = (Image)drawable;
		else img = image;
		if (img != null){
			// This must be TRUE_COLOR otherwise this will not work
			// properly under WinCE
			img = new Image(img,Image.RGB_IMAGE);
			img.makeGray(transparentColor);
			g.drawImage(img,mask,transparentColor,0,0,location.width,location.height);
			img.free();
			return;
		}
	}
	if (drawable != null)
		if (drawable instanceof Image) {
			g.setDrawOp(drawMode);
			g.drawImage((Image)drawable,mask,transparentColor,0,0,location.width,location.height);
		}
		else drawable.draw(g,0,0,options);
	else if (image != null) {
		//if (drawMode == Graphics.DRAW_ALPHA) ewe.sys.Vm.debug("Alpharing!");
		g.setDrawOp(drawMode);
		g.drawImage(image,mask,transparentColor,0,0,location.width,location.height);
	}
}
/**
* Draws itself on the graphic g at a specfied co-ordinate.
* If the image has a border it will be drawn.
*/
public synchronized void
//==============================================================
	draw(Graphics g,int x,int y,int options)
//==============================================================
{
	if ((properties & IsInvisible) != 0) return;
	Graphics clipped = g;//.create();
	//clipped.clipRect(x,y,dim.width+1,dim.height+1);
	if (/*(options & DrawBorderOnly) == 0*/true) {
//		if ((properties & IsEmpty) != 0) {
//		clipped.clearRect(x,y,dim.width,dim.height);
//			clipped.dispose();
//			return;
//		}
		if ((properties & IsEmpty) == 0) {
			clipped.translate(x,y);
			//where.translate(-x,-y);
			doDraw(clipped,options);
			clipped.translate(-x,-y);
			//where.translate(x,y);

		}
	}
	drawBorder(clipped,x,y,options);
	//clipped.dispose();
}
/**
* Draws itself on the graphic g at the point specified by the variable "location".
* If the image has a border it will be drawn.
*/
public synchronized void
//==============================================================
	draw(Graphics g)
//==============================================================
{
	draw(g,location.x,location.y,0);
}
//
/**
* This Area must update itself as the image moves.
**/
public Area hotArea;
//
/**
* This polygon must be relative to the top-left of the image. It will be automatically
* updated as the image moves so you can set it to be a static polygon.
**/
public Polygon hotPolygon;

//===================================================================
public void setHotAreaInImage(Area inImage)
//===================================================================
{
	hotArea = new imageHotArea(inImage,this,this);
}

/**
* This sets the hot area to be within the image and include only the
* opaque portions of the image.
**/
//===================================================================
public Area makeOpaqueHotArea(mImage template)
//===================================================================
{
	if (template == null) template = this;
	if (transparentColor != null)
		return new imageHotArea(transparentColor.toInt(),template,this);
	else
		return new imageHotArea(new Rect(0,0,template.location.width,template.location.height),this,this);
}
private Polygon movedPolygon;
/**
* The area returned here is not relative to the top left of the image. It
* is an absolute area, regardless of the location of the image. If you have an area that is
* hard to translate (e.g. a Polygon) then call convertRelativeArea()
**/
//==================================================================
public Area getHotArea()
//==================================================================
{
	if (hotArea != null) return hotArea;
	else if (hotPolygon != null){
		if (movedPolygon == null) movedPolygon = new Polygon(hotPolygon,location.x,location.y);
		else movedPolygon.translate(hotPolygon,location.x,location.y);
		return movedPolygon;
	}else
		return location;
}
/**
* Returns if the point is on the hot area of the image.
*/
public boolean
//==============================================================
	onHotArea(int x,int y)
//==============================================================
{
	if ((properties & IsNotHot) != 0) return false;
	return getHotArea().isIn(x,y);
}
//===================================================================
public Rect getDim(Rect dest)
//===================================================================
{
	return Rect.unNull(dest).set(0,0,location.width,location.height);
}
public int compareTo(Object other)
{
	if (this == other) return 0;
	return 1;
}
/**
* This transforms the image into a mouse cursor suitable for use on the current
* platform. Will return null if the cursor could not be created. The return value
* should then be passed to <b>ewe.sys.Vm.createCursor(Object cursor)</b>.<br>
* In order for this to work there are a few conditions:<br>
* 1. The mImage must be a mono (black & white) image.<br>
* 2. There <b>must</b> be a mask for the image which <b>must</b> be the same
* size as the image.<br>
* <br>
* The rules for the image and mask are as normal. The mask image should be black
* where you want the image to be opaque and should be white where you want the image
* to be transparent. The areas which are white (transparent) in the mask should
* also be white in the image.<br>
* The image will be scaled if necessary to get it to be the size of cursors on
* the system (for Win32 this is 32x32).
*<p>
* <b>This will free this mImage!<b>
**/
//===================================================================
public Object toCursor(Point hotSpot)
//===================================================================
{
	if (image == null || mask == null) return null;
	if (image.width != mask.width || image.height != mask.height) return null;
	if (hotSpot == null) hotSpot = new Point(0,0);
	return image.toCursor(mask,hotSpot);

}
//===================================================================
public void free()
//===================================================================
{
	if (image != null) image.free();
	if (mask != null) mask.free();
	if (drawable != null) drawable.free();
}
/**
* This gets a copy which has copies of the image resources of this one.
**/
//===================================================================
public mImage getFullCopy()
//===================================================================
{
	mImage mi = (mImage)getNew();
	if (mi.mask != null) {
		mi.mask = new Image(mi.mask,0);
		mi.mask.freeze();
	}
	if (mi.image != null) {
		mi.image = new Image(mi.image,0);
		mi.image.freeze();
	}
	if (mi.drawable instanceof mImage) mi.drawable = ((mImage)mi.drawable).getFullCopy();
	else if (mi.drawable instanceof Image) mi.drawable = new Image((Image)mi.drawable,0);
	return mi;
}

//===================================================================
public boolean usesAlpha()
//===================================================================
{
	if (image != null){
		if (mask != null || transparentColor != null || bitmask != null) return true;
		return image.usesAlpha();
	}
	if (drawable != null) return drawable.usesAlpha();
	return false;

}
//-------------------------------------------------------------------
protected Color getUnusedColor()
//-------------------------------------------------------------------
{
	return new Color(80,255,80);
}
//===================================================================
public int [] getPixels(int[] dest,int offset,int x,int y,int width,int height,int options)
//===================================================================
{
	if (image != null){
		int [] bits = image.getPixels(dest,offset,x,y,width,height,options);
		if (mask != null || transparentColor != null)
			Mask.makeAlpha(bits,offset,new Rect(x,y,width,height),mask,transparentColor);
		return bits;
	}
	if (drawable != null) return drawable.getPixels(dest,offset,x,y,width,height,options);
	return PixelBuffer.getPixelsFor(this,dest,offset,new Rect(x,y,width,height),options,getUnusedColor());
}
/**
* Convert the mImage to a single Image with an alpha channel.
* @param area an area within the image to convert. If this is null it will use the entire image.
* @param imageCreationOptions options to use when creating the new Image.
* @return a new Image or null if this mImage cannot be converted to an Image.
*/
//===================================================================
public Image toAlphaImage(Rect area,int imageCreationOptions) throws IllegalArgumentException
//===================================================================
{
	if (area == null) area = new Rect(0,0,location.width,location.height);
	else if (area.width <= 0 || area.height >= 0 || area.x+area.width > location.width || area.y+area.height > location.height || area.x < 0 || area.y < 0)
		throw new IllegalArgumentException();
	int [] all = getPixels(null,0,area.x,area.y,area.width,area.height,0);
	if (all == null) return null;
	Image im = new Image(area.width,area.height,imageCreationOptions);
	im.enableAlpha();
	im.setPixels(all,0,0,0,area.width,area.height,0);
	return im;
}
/**
 * Produce a new mImage that is a scaled version of a section of this one.
 * @param newWidth
 * @param newHeight
 * @param area An area within the image to scale - or null to scale the entire image.
 * @return a new mImage() or null if the mImage could not be scaled.
* @exception IllegalArgumentException if the area is out of bounds, or if the new size has negative values.
*/
//===================================================================
public mImage scale(int newWidth,int newHeight,Rect area,int imageAndScaleOptions) throws IllegalArgumentException
//===================================================================
{
	if (image == null) return null;
	if (area == null) area = new Rect(0,0,getWidth(),getHeight());
	else if (area.x < 0 || area.y <0 || area.width <= 0 || area.height <= 0 || area.x+area.width > getWidth() || area.y+area.height > getHeight())
		throw new IllegalArgumentException();
	if (newWidth <= 0 || newHeight <= 0) throw new IllegalArgumentException();

	Image im = Mask.scale(image,area,newWidth,newHeight,imageAndScaleOptions);
	if (image.hasAlpha && drawMode == Graphics.DRAW_ALPHA){
		return new mImage(im,Graphics.DRAW_ALPHA);
	}
	if (mask != null){
		mImage mi = new mImage();
		mi.image = im;
		mi.mask = Mask.scale(mask,area,newWidth,newHeight,0);
		mi.imageSet();
		return mi;
	}else if (transparentColor != null){
		mImage mi = new mImage();
		mi.setImage(im,transparentColor);
		return mi;
	}else
		return new mImage(im,drawMode);
}
protected void fixColors()
{
	if (image.hasAlpha && mask != null){
		int wd = image.width;
		int [] ic = new int[wd];
		int [] mc = new int[wd];
		int w = Graphics.mapColor(0xffffff) & 0xffffff;
		/*
		int [] white = new int[1];
		mask.setPixels(white,0,0,0,1,1,0);
		mask.getPixels(white,0,0,0,1,1,0);
		int w = white[0];
		String got = "";
		*/
		for (int i = 0; i<image.height; i++){
			image.getPixels(ic,0,0,i,wd,1,0);
			mask.getPixels(mc,0,0,i,wd,1,0);
			for (int p = 0; p<wd; p++){
				if ((mc[p] & 0xffffff) == w) {
					ic[p] = 0xffffff;
				}
			}
			image.setPixels(ic,0,0,i,wd,1,0);
		}
	}
}

private static IImage defImage;
private static IImage UnknownImage, BrokenImage;

//===================================================================
public static IImage getUnknownImage()
//===================================================================
{
	if (UnknownImage == null)
		UnknownImage = ImageCache.cache.get("ewe/imagesmall.bmp",new ewe.fx.Color(0,255,0));
	return UnknownImage;
}

//===================================================================
public static IImage getBrokenImage()
//===================================================================
{
	if (BrokenImage == null){
		PixelBuffer pb = new PixelBuffer(getUnknownImage());
		Graphics g = pb.getDrawingBuffer(null,null,1.0);
		g.setPen(new Pen(new Color(255,0,0),Pen.SOLID,2));
		g.drawLine(2,2,pb.getWidth()-4,pb.getHeight()-4);
		g.drawLine(2,pb.getHeight()-4,pb.getWidth()-4,2);
		pb.putDrawingBuffer(pb.PUT_BLEND);
		BrokenImage = pb.toMImage();
		pb.free();
	}
	return BrokenImage;
}

//-------------------------------------------------------------------
protected IImage getDefaultImage()
//-------------------------------------------------------------------
{
	if (defImage == null) defImage = getBrokenImage();
	return defImage;
}

//private static ewe.reflect.Type pngEncoder;

//===================================================================
public int encodeBytes(ByteArray dest)
//===================================================================
{
	//if (pngEncoder == null) pngEncoder = new ewe.reflect.Type("ewe.fx.PNGEncoder");
	//Object obj = pngEncoder.newInstance();
	//if (obj == null) return -1;
	try{
		ewe.io.MemoryFile mf = new ewe.io.MemoryFile();
		//pngEncoder.invoke(obj,"writeImage(Lewe/io/Stream;Lewe/fx/IImage;)V",new Object[]{mf,this});
		new PNGEncoder().writeImage(mf,this);
		int length = mf.data.length;
		if (dest == null) return length+4;
		dest.appendInt(length);
		dest.append(mf.data.data,0,mf.data.length);
		return length+4;
	}catch(Exception e){
		return -1;
	}
}
//===================================================================
public int decodeBytes(byte[] source,int offset,int length)
//===================================================================
{
	try{
		if (length == 0 || source == null) throw new Exception();
		int ilen = Utils.readInt(source,offset,4);
		byte[] bytes = new byte[ilen];
		ewe.sys.Vm.copyArray(source,offset+4,bytes,0,ilen);
		Image image = new Image(new ByteArray(bytes),0);
		setImage(image);
		freeSource();
		return length+4;
	}catch(Exception e){
		IImage img = getDefaultImage();
		if (img != null) setImage(img);
		else {
			location.width = location.height = 0;
		}
		return -1;
	}
}
public String toString()
{
	return "Image("+getWidth()+"x"+getHeight()+")";
}
private WeakReference refresher;

//===================================================================
public void setRefresher(ImageRefresher refresher)
//===================================================================
{
	this.refresher = new WeakReference(refresher);
}
//===================================================================
public boolean changeRefresher(ImageRefresher newRefresher, ImageRefresher oldRefresher)
//===================================================================
{
	if (getRefresher() != oldRefresher) return false;
	setRefresher(newRefresher);
	return true;
}
//===================================================================
public ImageRefresher getRefresher()
//===================================================================
{
	return refresher == null ? null : (ImageRefresher)refresher.get();
}
//-------------------------------------------------------------------
protected void refresh(ImageRefresher on)
//-------------------------------------------------------------------
{
	int op = 0;
	if ((properties & KeepOnScreen) != 0)
		op |= on.KEEP_VISIBLE;
	on.refresh(this,op);
}
//##################################################################
}
//##################################################################



