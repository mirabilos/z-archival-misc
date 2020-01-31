package ewe.fx;
import ewe.io.File;
import ewe.io.IOException;
import ewe.io.RandomAccessStream;
import ewe.io.RewindableStream;
import ewe.io.Stream;
import ewe.sys.Vm;
/**
This class is used for two purposes. It can be used as an Image creator, creating
images of a specific type. And it contains a set of useful static methods for
manipulating images and image data.
**/
//##################################################################
public class ImageTool{
//##################################################################

//===================================================================
public static int toImageDataType(int imageCreationOptionsOrImageDataType)
//===================================================================
{
	int ic = imageCreationOptionsOrImageDataType;
	if ((ic & ImageData.IS_AN_IMAGE_DATA_TYPE) != 0) return ic;
	if ((ic & Image.ARGB_IMAGE) != Image.ARGB_IMAGE) ic &= ~Image.ALPHA_CHANNEL;
	return
	(ic &
		(Image.INDEXED_2_IMAGE|Image.INDEXED_4_IMAGE|Image.INDEXED_16_IMAGE|Image.INDEXED_256_IMAGE|
		Image.GRAY_SCALE_2_IMAGE|Image.GRAY_SCALE_4_IMAGE|Image.GRAY_SCALE_16_IMAGE|Image.GRAY_SCALE_256_IMAGE|
		Image.RGB_IMAGE|Image.ARGB_IMAGE)
	)|ImageData.IS_AN_IMAGE_DATA_TYPE;
}
/**
Convert an ImageData.TYPE_XXXX value to an Image.XXX_IMAGE option for
creating an image.
**/
//===================================================================
public static int imageDataTypeToImageCreationOptions(int imageDataType) throws IllegalArgumentException
//===================================================================
{
	return imageDataType & ~ImageData.IS_AN_IMAGE_DATA_TYPE;
}
//===================================================================
public static boolean isAnIndexedImage(int imageDataType)
//===================================================================
{
	int it = toImageDataType(imageDataType);
	return (it == ImageData.TYPE_INDEXED_2 ||it == ImageData.TYPE_INDEXED_4||it == ImageData.TYPE_INDEXED_16||it == ImageData.TYPE_INDEXED_256);
}
//===================================================================
public static boolean isAGrayScaleImage(int imageDataType)
//===================================================================
{
	int it = toImageDataType(imageDataType);
	return (it == ImageData.TYPE_GRAY_SCALE_2 ||it == ImageData.TYPE_GRAY_SCALE_4||it == ImageData.TYPE_GRAY_SCALE_16||it == ImageData.TYPE_GRAY_SCALE_256);
}
//===================================================================
public static boolean usesColorTable(int imageDataType)
//===================================================================
{
	return isAnIndexedImage(toImageDataType(imageDataType));
}

/** These are the options that will be used for createImage().
By default it is zero.
**/
public int createOptions;
/** This is the optional color map that will be used for createImage(). This is
only necessary for the INDEX_xxx_IMAGE types.
**/
public int[] createColorTable;

//===================================================================
public ImageTool()
//===================================================================
{
	this(0,null);
}
//===================================================================
public ImageTool(int createOptions)
//===================================================================
{
	this(createOptions,null);
}
//===================================================================
public ImageTool(int createOptions, int[] createColorTable)
//===================================================================
{
	this.createOptions = createOptions;
	this.createColorTable = createColorTable;
}
/**
This creates an Image using the createOptions and createColorTable fields.
**/
//===================================================================
public Image createImage(int width, int height)
throws ewe.sys.SystemResourceException, ImageTypeNotSupportedException, IllegalArgumentException
//===================================================================
{
	if (createColorTable == null && usesColorTable(createOptions)) createColorTable = makeDefaultColorTable(createOptions);
	if (createColorTable == null) return new Image(width,height,createOptions);
	else return new Image(width,height,createOptions,null,0,0,createColorTable);
}
/**
If the specified ImageTool is null, then an image will be
created using the default options, otherwise the specified ImageTool is used to create the image.
**/
//===================================================================
public static Image createImageUsing(ImageTool it, int width, int height, int defaultOptions)
throws ewe.sys.SystemResourceException, ImageTypeNotSupportedException, IllegalArgumentException
//===================================================================
{
	if (it == null) return new Image(width,height,defaultOptions);
	else return it.createImage(width,height);
}
/**
Create a new Image that is compatible with the source ImageData, given a specific destination ImageData type.
* @param source The source ImageData.
* @param destinationImageDataType The desired new ImageData type. If this is one of the
* TYPE_INDEXED_XXX values, then the source ImageData must be of the exact same type so that
* the source color table can be transferred to the new image.
* @param newWidth The new width for the image.
* @param newHeight The new height for the image.
* @return The newly created image.
* @exception IllegalArgumentException
* @exception ewe.sys.SystemResourceException
* @exception ImageTypeNotSupportedException
*/
//===================================================================
public static Image createCompatibleImage(ImageData source,int destinationImageDataType,int newWidth,int newHeight)
throws IllegalArgumentException, ewe.sys.SystemResourceException, ImageTypeNotSupportedException
//===================================================================
{
	int options = imageDataTypeToImageCreationOptions(destinationImageDataType);
	int[] colorTable = null;
	int sourceImageDataType = source.getImageType();
	if (isAnIndexedImage(destinationImageDataType)){
		if (destinationImageDataType != sourceImageDataType) throw new IllegalArgumentException();
		colorTable = source.getImageColorTable();
	}
	if (colorTable != null)
		return new Image(newWidth,newHeight,options,null,0,0,colorTable);
	else
		return new Image(newWidth,newHeight,options);
}
/**
Create a new Image that is compatible with the source ImageData.
* @param source The source ImageData.
* @param newWidth The new width for the image.
* @param newHeight The new height for the image.
* @return The newly created image.
* @exception IllegalArgumentException
* @exception ewe.sys.SystemResourceException
* @exception ImageTypeNotSupportedException
*/
//===================================================================
public static Image createCompatibleImage(ImageData source,int newWidth,int newHeight)
throws IllegalArgumentException, ewe.sys.SystemResourceException, ImageTypeNotSupportedException
//===================================================================
{
	return createCompatibleImage(source,source.getImageType(),newWidth,newHeight);
}
/**
Get the scan line length needed for an array representation of a particular ImageData type
for a particular image width, using a scanLineType of either SCAN_LINE_INT_ARRAY or SCAN_LINE_BYTE_ARRAY.
Only TYPE_RGB and TYPE_ARGB can use both SCAN_LINE_INT_ARRAY or SCAN_LINE_BYTE_ARRAY, all other types must
use SCAN_LINE_BYTE_ARRAY.
**/
//===================================================================
public static int getScanLineLengthFor(ImageData data, int width)
//===================================================================
{
	return getScanLineLengthFor(data.getImageType(),data.getImageScanLineType(),width);
}
/**
Get the scan line length needed for an array representation of a particular ImageData type
for a particular image width, using a scanLineType of either SCAN_LINE_INT_ARRAY or SCAN_LINE_BYTE_ARRAY.
Only TYPE_RGB and TYPE_ARGB can use both SCAN_LINE_INT_ARRAY or SCAN_LINE_BYTE_ARRAY, all other types must
use SCAN_LINE_BYTE_ARRAY.
**/
//===================================================================
public static int getScanLineLengthFor(int imageDataType, int scanLineType, int width)
//===================================================================
{
	imageDataType = toImageDataType(imageDataType);
	int bpp = 0;
	switch(imageDataType){
		case ImageData.TYPE_GRAY_SCALE_2: case ImageData.TYPE_INDEXED_2: bpp = 1; break;
		case ImageData.TYPE_GRAY_SCALE_4: case ImageData.TYPE_INDEXED_4: bpp = 2; break;
		case ImageData.TYPE_GRAY_SCALE_16: case ImageData.TYPE_INDEXED_16: bpp = 4; break;
		case ImageData.TYPE_GRAY_SCALE_256: case ImageData.TYPE_INDEXED_256: bpp = 8; break;
		case ImageData.TYPE_RGB: bpp = 24; break;
		case ImageData.TYPE_ARGB: bpp = 32; break;
	}
	if (bpp == 24 || bpp == 32) return scanLineType == ImageData.SCAN_LINE_INT_ARRAY ? width : (bpp*width)/8;
	if (scanLineType != ImageData.SCAN_LINE_BYTE_ARRAY) throw new IllegalArgumentException();
	return ((bpp*width)+7)/8;
}
/**
Create and return an array that can hold scan lines for an ImageData of a
specified type with specified width and height, and using a specified scanline type.
**/
//===================================================================
public static Object getArrayFor(int imageDataType, int scanLineType, int width, int height)
//===================================================================
{
	imageDataType = toImageDataType(imageDataType);
	if (scanLineType == ImageData.SCAN_LINE_INT_ARRAY)
		return new int[height*getScanLineLengthFor(imageDataType,scanLineType,width)];
	else
		return new byte[height*getScanLineLengthFor(imageDataType,scanLineType,width)];
}
/**
Create and return an array that can hold scan lines for an ImageData of a
specified type with specified width and height, and using a specified scanline type.
**/
//===================================================================
public static Object getArrayFor(ImageData id, int width, int height)
//===================================================================
{
	return getArrayFor(id.getImageType(),id.getImageScanLineType(),width,height);
}
//===================================================================
/**
 * Return the number of items (bytes or ints) per pixel in an image scan line.
 * This returns 0 if the number of items per pixel is less than 1. Otherwise it will return 1 or 3 or 4.
 * @param imageDataType the type of the image data.
 * @param scanLineType the type of the scan line.
 * @return the number of items (bytes or ints) per pixel in an image scan line.
 */
public static int getItemsPerPixel(int imageDataType, int scanLineType)
//===================================================================
{
	imageDataType = toImageDataType(imageDataType);
	int bpp = 0;
	switch(imageDataType){
		case ImageData.TYPE_GRAY_SCALE_2: case ImageData.TYPE_INDEXED_2: bpp = 1; break;
		case ImageData.TYPE_GRAY_SCALE_4: case ImageData.TYPE_INDEXED_4: bpp = 2; break;
		case ImageData.TYPE_GRAY_SCALE_16: case ImageData.TYPE_INDEXED_16: bpp = 4; break;
		case ImageData.TYPE_GRAY_SCALE_256: case ImageData.TYPE_INDEXED_256: bpp = 8; break;
		case ImageData.TYPE_RGB: bpp = 24; break;
		case ImageData.TYPE_ARGB: bpp = 32; break;
	}
	if (bpp < 8) return 0;
	if (bpp == 24 || bpp == 32) return scanLineType == ImageData.SCAN_LINE_INT_ARRAY ? 1 : bpp/8;
	if (scanLineType != ImageData.SCAN_LINE_BYTE_ARRAY) throw new IllegalArgumentException();
	return 1;
}
/**
 * Return the number of items (bytes or ints) per pixel in an image scan line.
 * This returns 0 if the number of items per pixel is less than 1. Otherwise it will return 1 or 3 or 4.
 * @param data an ImageData
 * @return the number of items (bytes or ints) per pixel in an image scan line.
 */
public static int getItemsPerPixel(ImageData data)
//===================================================================
{
	return getItemsPerPixel(data.getImageType(),data.getImageScanLineType());
}
/**
An option for the scale() methods. It indicates that rough scaling should be done.
**/
public static final int SCALE_ROUGH = 0x1;
/**
An option for one of the scale() methods. It indicates that the aspect ratio of the
original image should be kept.
**/
public static final int SCALE_KEEP_ASPECT_RATIO = 0x2;
/**
An option for one of the scale() methods. It indicates that the created scaled image
should be made to be of the same type as the source image.
**/
public static final int SCALE_USE_EXACT_TYPE = 0x4;
//-------------------------------------------------------------------
private static int findBestImageDataType(ImageData source,int newWidth,int newHeight,int scaleOptions)
//-------------------------------------------------------------------
{
	boolean scaleDown = source.getImageWidth() > newWidth || source.getImageHeight() > newHeight;
	if ((scaleOptions & SCALE_ROUGH) != 0) scaleDown = false;
	int st = source.getImageType();
	if (scaleDown)
		if (isAnIndexedImage(st)) st = ImageData.TYPE_RGB;
		else if (isAGrayScaleImage(st)) st = ImageData.TYPE_GRAY_SCALE_256;
	return st;
}
/**
 * Scale an image and return a new image.
 * @param source the source ImageData
 * @param newWidth the new width for the image.
 * @param newHeight the new height for the image.
 * @param scaleOptions any of the SCALE_XXX options.
 * @return a scaled image. The type of the scaled image may not be the same as the
 * source ImageData <b>if</b> the image was scaled down and smooth scaling was selected.
 * @exception IllegalArgumentException
 * @exception ewe.sys.SystemResourceException
 * @exception ImageTypeNotSupportedException
 */
//===================================================================
public static Image scale(ImageData source,int newWidth,int newHeight,int scaleOptions)
throws IllegalArgumentException, ewe.sys.SystemResourceException, ImageTypeNotSupportedException
//===================================================================
{
	return scaleSection(source,newWidth,newHeight,null,scaleOptions);
}
/**
 * Scale a section of an image and return a new image.
 * @param source the source ImageData
 * @param sourceArea the area within the source image to scale.
 * @param newWidth the new width for the image.
 * @param newHeight the new height for the image.
 * @param scaleOptions any of the SCALE_XXX options.
 * @return a scaled image. The type of the scaled image may not be the same as the
 * source ImageData <b>if</b> the image was scaled down and smooth scaling was selected.
 * @exception IllegalArgumentException
 * @exception ewe.sys.SystemResourceException
 * @exception ImageTypeNotSupportedException
 */
//===================================================================
public static Image scaleSourceSection(ImageData source,Rect sourceArea,int newWidth,int newHeight,int scaleOptions)
throws IllegalArgumentException, ewe.sys.SystemResourceException, ImageTypeNotSupportedException
//===================================================================
{
	if (sourceArea == null) return scaleSection(source,newWidth,newHeight,null,scaleOptions);
	//
	if (newWidth < 0 || newHeight < 0) throw new IllegalArgumentException();
	//
	int width = source.getImageWidth();
	int height = source.getImageHeight();
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
	double xscale =(double)newWidth/width;
	double yscale = (double)newHeight/height;
	//
	Rect destArea = new Rect((int)(sourceArea.x*xscale),(int)(sourceArea.y*yscale),(int)(sourceArea.width*xscale),(int)(sourceArea.height*yscale));
	if (destArea.width <= 0) destArea.width = 1;
	if (destArea.height <= 0) destArea.height = 1;
	//
	scaleOptions &= ~SCALE_KEEP_ASPECT_RATIO;
	return scaleSection(source,newWidth,newHeight,destArea,scaleOptions);
}

/**
 * Scale an image and return a section of the scaled image.
 * @param source the source ImageData
 * @param newWidth the new width for the image.
 * @param newHeight the new height for the image.
 * @param destinationArea the area in the <b>scaled</b> image to return.
 * @param scaleOptions any of the SCALE_XXX options.
 * @return a scaled image. The type of the scaled image may not be the same as the
 * source ImageData <b>if</b> the image was scaled down and smooth scaling was selected.
 * @exception IllegalArgumentException
 * @exception ewe.sys.SystemResourceException
 * @exception ImageTypeNotSupportedException
 */
//===================================================================
public static Image scaleSection(ImageData source,int newWidth,int newHeight,Rect destinationArea,int scaleOptions)
throws IllegalArgumentException, ewe.sys.SystemResourceException, ImageTypeNotSupportedException
//===================================================================
{
	//
	if (newWidth < 0 || newHeight < 0) throw new IllegalArgumentException();
	//
	int width = source.getImageWidth();
	int height = source.getImageHeight();
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
	if (destinationArea == null) destinationArea = new Rect(0,0,newWidth,newHeight);
	//
	// This is the best way to do it.
	//
	Image destination = createCompatibleImage(source,findBestImageDataType(source,newWidth,newHeight,scaleOptions),destinationArea.width,destinationArea.height);
	scaleSection(source,newWidth,newHeight,destination,destinationArea.x,destinationArea.y,scaleOptions);
	return destination;
	//
	// This is an alternate way of doing this.
	// I use this to test the various parts of it.
	//
	/*
	ArrayImageData aid = new ArrayImageData().set(findBestImageDataType(source,newWidth,newHeight,scaleOptions),destinationArea.width,destinationArea.height);
	scaleSection(source,newWidth,newHeight,aid,destinationArea.x,destinationArea.y,scaleOptions);
	Image im = aid.toImage();
	return im;
	*/
}
/**
Scale a source Image so that it fits exactly into the destination Image using smooth
scaling.
* @param source The source ImageData
* @param destination The destination Image.
*/
//===================================================================
public static void scale(ImageData source, ImageData destination)
//===================================================================
{
	scaleSection(source,destination.getImageWidth(),destination.getImageHeight(),destination,0,0,0);
}
/**
Scale a source Image so that it fits exactly into the destination Image.
* @param source The source ImageData
* @param destination The destination Image.
* @param options only SCALE_ROUGH or 0.
*/
//===================================================================
public static void scale(ImageData source, ImageData destination, int options)
//===================================================================
{
	scaleSection(source,destination.getImageWidth(),destination.getImageHeight(),destination,0,0,options);
}
//
// DONT CHANGE THESE VALUES - THEY ARE USED BY THE NATIVE METHOD.
//
static final int S_GRAY = 1, S_MONO = 2, S_COLOR = 3;
//
//private static RGBImageData sourceWrapper, destWrapper;
/**
Return the best ImageData implementation for the specified IImage. If the IImage
implements ImageData, then the image will be returned, otherwise the IImage will
be wrapped in an RGBImageData value and returned.
**/
//===================================================================
public static ImageData toImageData(IImage image)
//===================================================================
{
	if (image instanceof ImageData)
		return ((ImageData)image);
	else if (image instanceof mImage && !image.usesAlpha() && ((mImage)image).image != null)
 		return ((mImage)image).image;
	else return new RGBImageData(image);
}
/**
Scale an image and then place a section of the scaled image into an ImageData object.
The section will be the same width and height as the destination.
* @param source The source image.
* @param newWidth The width of the complete image after scaling.
* @param newHeight The height of the complete image after scaling.
* @param dest The destination imageData.
* @param destX The x point within the scaled image that should go into the destination image.
* @param destY The y point within the scaled image that should go into the destination image.
* @param options Options for scaling - so far only the SCALE_ROUGH is supported. Smooth
* scaling is done by default.
*/
//===================================================================
public static void scaleSection(ImageData source,int newWidth, int newHeight,ImageData dest,int destX, int destY, int options)
//===================================================================
{
	int st = source.getImageType();
	//
	int destinationType = dest.getImageType();
	int dstType = S_COLOR;
	if (destinationType == ImageData.TYPE_GRAY_SCALE_256) dstType = S_GRAY;
	else if (destinationType == ImageData.TYPE_GRAY_SCALE_2) dstType = S_MONO;
	//private static
	RGBImageData sourceWrapper = null, destWrapper = null;
	if (dstType == S_COLOR){
		if (destWrapper == null) destWrapper = new RGBImageData();
		dest = RGBImageData.toImageData(dest,destWrapper);
		if (dest == null) throw new IllegalArgumentException("The destination cannot be scaled into.");
	}
	boolean dstHasAlpha = dest.getImageType() == ImageData.TYPE_ARGB;
	//
	int srcType = S_COLOR;
	if (st == ImageData.TYPE_GRAY_SCALE_256) srcType = S_GRAY;
	else if (st == ImageData.TYPE_GRAY_SCALE_2) srcType = S_MONO;
	//
	if (srcType == S_COLOR) {
		if (sourceWrapper == null) sourceWrapper = new RGBImageData();
		source = RGBImageData.toImageData(source,sourceWrapper);
		if (source == null) throw new IllegalArgumentException("The source cannot be scaled.");
	}
	boolean srcHasAlpha = source.getImageType() == ImageData.TYPE_ARGB;
	//
	//
	int width = source.getImageWidth();
	int height = source.getImageHeight();
	int sbpl = source.getImageScanLineLength();
	//
	int destWidth = dest.getImageWidth();
	int tdw = destWidth;
	if (destX+tdw > newWidth) {
		tdw = newWidth-destX;
	}
	int dbpl = getScanLineLengthFor(dest,tdw);

 	int[]
		red = new int[tdw], scales = new int[tdw],
		green = dstType == S_COLOR ? new int[tdw] : null,
		blue = dstType == S_COLOR ? new int[tdw] : null,
		alpha = dstHasAlpha && srcHasAlpha ? new int[tdw] : null;
	//
	byte[] destB = dstType == S_COLOR ? null : new byte[dbpl];
	//
	byte[] srcB = null;
	int[] srcI = null;
	if (srcType == S_COLOR) srcI = new int[sbpl];
	else srcB = new byte[sbpl];
	//
	ScaleInfo si = new ScaleInfo();
	si.source = source;
	si.destination = dest;
	si.sourceImage = si.destinationImage = null;
	if (source instanceof Image) si.sourceImage = (Image)source;
	if (dest instanceof Image) si.destinationImage = (Image)dest;
	si.srcType = srcType;
	si.dstType = dstType;
	si.newWidth = newWidth;
	si.newHeight = newHeight;
	si.destX = destX;
	si.destY = destY;
	si.sWidth = width;
	si.sHeight = height;
	si.sbpl = sbpl;
	si.dWidth = tdw;
	si.dHeight = dest.getImageHeight();
	si.dbpl = dbpl;
	si.dsll = dest.getImageScanLineLength();
	si.red = red;
	si.green = green;
	si.blue = blue;
	si.scales = scales;
	si.srcB = srcB;
	si.srcI = srcI;
	si.destB = destB;
	si.alpha = alpha;
	si.options = options;
	if (tdw != destWidth){
		si.temp = getArrayFor(dest,dest.getImageScanLineLength(),1);
		si.tempLength = si.temp instanceof byte[] ? ((byte[])si.temp).length : ((int[])si.temp).length;
	}
	try{
	    si.scale();
	}catch(Exception e){
		e.printStackTrace();
	}
}
/**
This method "imprints" the data in the small ImageData into the big ImageData
at the top left corner of the big ImageData.
@deprecated use overlay instead.
**/
//===================================================================
public static void imprint(ImageData big, ImageData small)
//===================================================================
{
	if (big.getImageScanLineType() != small.getImageScanLineType()) throw new IllegalArgumentException();
	int sh = small.getImageHeight();
	int bh = big.getImageHeight();
	int whichLine = (bh-sh)/2;
	int sw = small.getImageWidth();
	int bw = big.getImageWidth();
	if (sw > bw || sh > bh) throw new IllegalArgumentException();
	int bsll = big.getImageScanLineLength();
	int ssll = small.getImageScanLineLength();
	int size = bsll > ssll ? bsll : ssll;
	Object got = big.getImageScanLineType() == ImageData.SCAN_LINE_INT_ARRAY ?
		(Object)(new int[size]) : (Object)(new byte[size]);
	for (int i = 0; i<sh; i++){
		big.getImageScanLines(i,1,got,0,0);
		small.getImageScanLines(i,1,got,0,0);
		big.setImageScanLines(i,1,got,0,0);
	}
}
private static Object odb, osb;
/**
This method "overlays" the data in the small ImageData over the data in the big image.
This method requires the ImageTypes to be the same on the source and destination images,
<b>and</b> that the each pixel takes up a whole number of bytes of pixels in the scan
lines.
**/
//===================================================================
public static void quickOverlay(ImageData destData, int destX, int destY, ImageData srcData, int srcX, int srcY, int srcWidth, int srcHeight)
//===================================================================
{
	//
	// First clip the source area if necessary so that it fits completely in the srcDat.
	//
	Rect src = new Rect(0,0,srcData.getImageWidth(),srcData.getImageHeight());
	Rect r = new Rect(srcX,srcY,srcWidth,srcHeight);
	src.getIntersection(r,src);
	if (src.width <= 0 || src.height <= 0) return;
	srcX = src.x;
	srcY = src.y;
	//
	// src now holds a valid rectangle within the source image.
	// Now clip src again so that it fits completely within the dest image.
	src.x = destX;
	src.y = destY;
	//
	//
	r.set(0,0,destData.getImageWidth(),destData.getImageHeight());
	src.getIntersection(r,src);
	if (src.width <= 0 || src.height <= 0) return;
	srcWidth = src.width;
	srcHeight = src.height;
	srcX += src.x-destX;
	srcY += src.y-destY;
	destX = src.x;
	destY = src.y;
	//
	// Now we should be dealing with correct values.
	//
	//Vm.debug(srcWidth+", "+srcHeight+", "+srcX+", "+srcY+", "+destX+", "+destY);
	odb = getScanLineBuffer(destData,1,odb);
	osb = getScanLineBuffer(srcData,1,osb);
	int dlen = destData.getImageScanLineLength();
	int slen = srcData.getImageScanLineLength();
	int iSize = getItemsPerPixel(destData);
	int si = srcX*iSize;
	int di = destX*iSize;
	int ilen = srcWidth*iSize;
	for (int i = 0; i<srcHeight; i++){
		destData.getImageScanLines(destY+i,1,odb,0,dlen);
		srcData.getImageScanLines(srcY+i,1,osb,0,slen);
		System.arraycopy(osb,si,odb,di,ilen);
		destData.setImageScanLines(destY+i,1,odb,0,dlen);
	}
}

private static int[] gs2, gs4, gs16, gs256;
/**
Create and return a Gray scale color table. The returned table should <b>not</b>
be altered.
*/
//===================================================================
public static int[] getGrayScaleColorTable(int imageDataType)
//===================================================================
{
	int imageType = toImageDataType(imageDataType);
	if (gs256 == null) {
		gs256 = new int[256];
		for (int i = 0; i<256; i++)
			gs256[i] = (i << 16)|(i<<8)|(i);
	}
	if (gs16 == null) gs16 = new int[]
		{
			0x000000,0x111111,0x222222,0x333333,
			0x444444,0x555555,0x666666,0x777777,
			0x888888,0x999999,0xaaaaaa,0xbbbbbb,
			0xcccccc,0xdddddd,0xeeeeee,0xffffff
		};
	if (gs4 == null) gs4 = new int[]{0,0x555555,0xaaaaaa,0xffffff};
	if (gs2 == null) gs2 = new int[]{0,0xffffff};

	if (imageType == ImageData.TYPE_GRAY_SCALE_256) return gs256;
	else if (imageType == ImageData.TYPE_GRAY_SCALE_16) return gs16;
	else if (imageType == ImageData.TYPE_GRAY_SCALE_4) return gs4;
	else if (imageType == ImageData.TYPE_GRAY_SCALE_2) return gs2;
	else return null;

}

private static int[] c256, c16, c4, c2;
/**
Create and return a Color Table for one of the TYPE_INDEXED_XXX types. The returned table may
be modified if necessary.
**/
//===================================================================
public static int[] makeDefaultColorTable(int imageDataType)
//===================================================================
{
	int imageType = toImageDataType(imageDataType);
	if (c2 == null) c2 = new int[]{0xff000000,0xffffffff};
	if (c4 == null) c4 = new int[]{0xffffffff,0xffff0000,0xff00ff00,0xff0000ff};
	if (c16 == null) c16 =
	new int[]{
		0xff000000, 0xff0000ff, 0xff00ff00, 0xff00ffff,
		0xffff0000, 0xffff00ff, 0xffffff00, 0xffffffff,
		0xff800000, 0xff008000,	0xff000080,
		0xfffff0f0, 0xfff0fff0, 0xfff0f0ff,
		0xff808080, 0xa0a0a0};

	if (c256 == null){
		c256 = new int[256];
		int idx = 0;
		for (int r = 0; r<6; r++){
			int rm = (r*0x33) << 16;
			for (int g = 0; g<6; g++){
				int gm = (g*0x33) << 8;
				for (int b = 0; b<6; b++)
					c256[idx++] = 0xff000000|rm|gm|(b*0x33);
			}
		}
		for (int b = 6; b < 256 && idx < 256; b += 6){
			if (b % 0x33 == 0) continue;
			c256[idx++] = 0xff000000|(b << 16)|(b << 8)|b;
		}
	}
	if (imageType == ImageData.TYPE_INDEXED_256) return (int[])c256.clone();
	else if (imageType == ImageData.TYPE_INDEXED_16) return (int[])c16.clone();
	else if (imageType == ImageData.TYPE_INDEXED_4) return (int[])c4.clone();
	else if (imageType == ImageData.TYPE_INDEXED_2) return (int[])c2.clone();
	else return null;
}
/**
Create an int[] or byte[] buffer to read/write scan lines from the specified ImageData,
re-using a previous buffer object if possible.
* @param imageData The imageData to read/write scan lines.
* @param numScanLines The number of scan lines to read/write.
* @param oldBuffer An object to be re-used if possible.
* @return An int[] or byte[] big enough to hold the scan line data.
*/
//===================================================================
public static  Object getScanLineBuffer(ImageData imageData, int numScanLines, Object oldBuffer)
//===================================================================
{
	Object buffer = oldBuffer;
	int ty = imageData.getImageScanLineType();
	int len = numScanLines*imageData.getImageScanLineLength();
	if (ty == ImageData.SCAN_LINE_INT_ARRAY){
		if (!(buffer instanceof int[])) buffer = null;
		if (buffer != null && ((int[])buffer).length < len) buffer = null;
		if (buffer == null) buffer = new int[len];
	}else{
		if (!(buffer instanceof byte[])) buffer = null;
		if (buffer != null && ((byte[])buffer).length < len) buffer = null;
		if (buffer == null) buffer = new byte[len];
	}
	return buffer;
}
private static int[] argbMasks = {0x80,0xc0,0,0xf0,0,0,0,0xff};

//-------------------------------------------------------------------
private static boolean hasNative = true;
private static native void argbConvert(boolean isTo,int sourceImageDataType, int sourceWidth, int[] sourceColorTable, Object sourceScanLines, int sourceOffset, int sourceScanLineLength, int[] argbData, int argbOffset, int argbScanLineLength, int numScanLines);
//-------------------------------------------------------------------

//===================================================================
public static void toARGB(int sourceImageDataType, int sourceWidth, int[] sourceColorTable, Object sourceScanLines, int sourceOffset, int sourceScanLineLength, int[] argbData, int argbOffset, int argbScanLineLength, int numScanLines)
//===================================================================
{
	int it = toImageDataType(sourceImageDataType);
	//
	if (isAGrayScaleImage(it)) sourceColorTable = getGrayScaleColorTable(it);
	if (isAnIndexedImage(it) && sourceColorTable == null) throw new NullPointerException();
	//
	if (hasNative)try{
		if (sourceScanLines == null || argbData == null) throw new NullPointerException();
		argbConvert(true, sourceImageDataType,  sourceWidth,  sourceColorTable, sourceScanLines, sourceOffset, sourceScanLineLength, argbData, argbOffset, argbScanLineLength, numScanLines);
		return;
	}catch(UnsatisfiedLinkError er){
		hasNative = false;
	}catch(SecurityException ex){
		hasNative = false;
	}
	int bpp = 8;
	if (it == ImageData.TYPE_GRAY_SCALE_2 || it == ImageData.TYPE_INDEXED_2) bpp = 1;
	else if (it == ImageData.TYPE_GRAY_SCALE_4 || it == ImageData.TYPE_INDEXED_4) bpp = 2;
	else if (it == ImageData.TYPE_GRAY_SCALE_16 || it == ImageData.TYPE_INDEXED_16) bpp = 4;
	//
	if (it != ImageData.TYPE_RGB && it != ImageData.TYPE_ARGB){
		byte[] data = (byte[])sourceScanLines;
		int startMask = argbMasks[bpp-1];
		int startShift = 8-bpp;
		for (int y = 0; y<numScanLines; y++){
			int si = sourceOffset+sourceScanLineLength*y;
			int di = argbOffset+argbScanLineLength*y;
			int mask = startMask;
			int shift = startShift;
			for (int x = 0; x<sourceWidth; x++){
				int index = (((int)data[si]&0xff) & mask)>>shift;
				int color = 0xff000000|sourceColorTable[index];
				argbData[di++] = color;
				mask >>= bpp;
				shift -= bpp;
				if (mask == 0){
					mask = startMask;
					shift = startShift;
					si++;
				}
			}
		}
	}else if (sourceScanLines instanceof byte[]){
		//
		// Source IS RGB/ARGB but data is in byte form.
		// The data must be in R,G,B(,A) sequence.
		//
		byte[] data = (byte[])sourceScanLines;
		for (int y = 0; y<numScanLines; y++){
			int si = sourceOffset+sourceScanLineLength*y;
			int di = argbOffset+argbScanLineLength*y;
			for (int x = 0; x<sourceWidth; x++){
				int r = (int)data[si++] & 0xff;
				int g = (int)data[si++] & 0xff;
				int b = (int)data[si++] & 0xff;
				int a = it == ImageData.TYPE_ARGB ? data[si++] & 0xff : 0xff;
				argbData[di++] = (a<<24)|(r<<16)|(g<<8)|(b);
			}
		}
	}else if (it == ImageData.TYPE_ARGB){ // Just copy the data straight across.
		int[] data = (int[])sourceScanLines;
		for (int y = 0; y<numScanLines; y++){
			int si = sourceOffset+sourceScanLineLength*y;
			int di = argbOffset+argbScanLineLength*y;
			System.arraycopy(data,si,argbData,di,sourceWidth);
		}
	}else{ // it == ImageData.TYPE_RGB - have to make sure that the alpha channel is 0xff
		int[] data = (int[])sourceScanLines;
		for (int y = 0; y<numScanLines; y++){
			int si = sourceOffset+sourceScanLineLength*y;
			int di = argbOffset+argbScanLineLength*y;
			for (int x = 0; x<sourceWidth; x++){
				argbData[di++] = data[si++] | 0xff000000;
			}
		}
	}
}
//===================================================================
public static void fromARGB(int[] argbData, int argbOffset, int argbScanLineLength, int destImageDataType, int destWidth, int[]destColorTable, Object destScanLines, int destOffset, int destScanLineLength, int numScanLines)
//===================================================================
{
	int it = toImageDataType(destImageDataType);
	//
	if (isAGrayScaleImage(it)) destColorTable = getGrayScaleColorTable(it);
	if (isAnIndexedImage(it) && destColorTable == null) throw new NullPointerException();
	//
	if (hasNative)try{
		if (destScanLines == null || argbData == null) throw new NullPointerException();
		argbConvert(false, destImageDataType,  destWidth,  destColorTable, destScanLines, destOffset, destScanLineLength, argbData, argbOffset, argbScanLineLength, numScanLines);
		return;
	}catch(UnsatisfiedLinkError er){
		hasNative = false;
	}catch(SecurityException ex){
		hasNative = false;
	}
	int bpp = 8;
	if (it == ImageData.TYPE_GRAY_SCALE_2 || it == ImageData.TYPE_INDEXED_2) bpp = 1;
	else if (it == ImageData.TYPE_GRAY_SCALE_4 || it == ImageData.TYPE_INDEXED_4) bpp = 2;
	else if (it == ImageData.TYPE_GRAY_SCALE_16 || it == ImageData.TYPE_INDEXED_16) bpp = 4;
	//
	if (it != ImageData.TYPE_RGB && it != ImageData.TYPE_ARGB){
		byte[] data = (byte[])destScanLines;
		int startMask = argbMasks[bpp-1];
		int startShift = 8-bpp;
		int lastIndex = -1;
		int lastColor = 0;
		for (int y = 0; y<numScanLines; y++){
			int si = destOffset+destScanLineLength*y;
			int di = argbOffset+argbScanLineLength*y;
			int mask = startMask;
			int shift = startShift;
			data[si] = 0;
			for (int x = 0; x<destWidth; x++){
				int color = argbData[di++] & 0xffffff;
				int index = -1;
				if (color == lastColor && lastIndex != -1){
					index = lastIndex;
				}else{
					for (int i = 0; i<destColorTable.length; i++){
						if ((destColorTable[i] & 0xffffff) == color){
							index = i;
							break;
						}
					}
					//
					// Didn't find an exact match, so have to find
					// closest match.
					//
					if (index == -1){
						int diff = 0;
						int r = (color >> 16) & 0xff, g = (color >> 8) & 0xff, b = color & 0xff;
						for (int i = 0; i<destColorTable.length; i++){
							int cc = destColorTable[i];
							int rd = ((cc >> 16) & 0xff)-r; if (rd < 0) rd = -rd;
							int gd = ((cc >> 8) & 0xff)-g; if (gd < 0) gd = -gd;
							int bd = ((cc) & 0xff)-b; if (bd < 0) bd = -bd;
							int t = rd+gd+bd;
							if (t < diff || index == -1){
								index = i;
								diff = t;
							}
						}
					}
					lastColor = color;
					lastIndex = index;
				}
				data[si] &= (byte)~mask;
				data[si] |= (byte)(index << shift);
				mask >>= bpp;
				shift -= bpp;
				if (mask == 0){
					mask = startMask;
					shift = startShift;
					si++;
				}
			}
		}
	}else if (destScanLines instanceof byte[]){
		//
		// dest IS RGB/ARGB but data is in byte form.
		// The data must be in R,G,B(,A) sequence.
		//
		byte[] data = (byte[])destScanLines;
		for (int y = 0; y<numScanLines; y++){
			int si = destOffset+destScanLineLength*y;
			int di = argbOffset+argbScanLineLength*y;
			for (int x = 0; x<destWidth; x++){
				int color = argbData[di++];
				data[si++] = (byte)(color >> 16);
				data[si++] = (byte)(color >> 8);
				data[si++] = (byte)(color);
				if (it == ImageData.TYPE_ARGB)
					data[si++] = (byte)(color >> 24);
			}
		}
	}else if (it == ImageData.TYPE_ARGB){ // Just copy the data straight across.
		int[] data = (int[])destScanLines;
		for (int y = 0; y<numScanLines; y++){
			int si = destOffset+destScanLineLength*y;
			int di = argbOffset+argbScanLineLength*y;
			System.arraycopy(argbData,di,data,si,destWidth);
		}
	}else{ // it == ImageData.TYPE_RGB - have to make sure that the alpha channel is 0xff
		int[] data = (int[])destScanLines;
		for (int y = 0; y<numScanLines; y++){
			int si = destOffset+destScanLineLength*y;
			int di = argbOffset+argbScanLineLength*y;
			for (int x = 0; x<destWidth; x++){
				data[si++] = argbData[di++] | 0xff000000;
			}
		}
	}
}
/**
 * Retrieve an Image that may be an animated image (such as an animaged GIF)
 * or may be any other type of decodable image.
 * @param imageFile a File representing the image to fetch.
 * @param requestedSize an optional Dimension giving a scaled image size. If this is
 * null OR if the image decoder does not support scaling while decoding, then
 * the image will be returned full sized.
 * @param info if this is not null the ImageInfo information on the image will be
 * placed in here.
 * @param createOptions creation options for the Image constructor.
 * @return an IImage that best represents the Image.
 * @throws IOException if an IO error occurs.
 */
public static IImage getImage(File imageFile,Dimension requestedSize,ImageInfo info,int createOptions)
throws IOException
{
	Stream in = null;
	try{
		in = imageFile.toReadableStream();
		return getImage(in,requestedSize,info,createOptions);
	}finally{
		in.close();
	}
}
//===================================================================
/**
 * Retrieve an Image that may be an animated image (such as an animaged GIF)
 * or may be any other type of decodable image.
 * @param imageBytes an input Stream from which the encoded image bytes may be retrieved.
 * @param requestedSize an optional Dimension giving a scaled image size. If this is
 * null OR if the image decoder does not support scaling while decoding, then
 * the image will be returned full sized.
 * @param info if this is not null the ImageInfo information on the image will be
 * placed in here.
 * @param createOptions creation options for the Image constructor.
 * @return an IImage that best represents the Image.
 * @throws IOException if an IO error occurs.
 */
public static IImage getImage(Stream imageBytes,Dimension requestedSize,ImageInfo info,int createOptions)
throws IOException
//===================================================================
{
	RandomAccessStream in = RewindableStream.toRewindableStream(imageBytes);
	if (in == null) return null;
	ImageInfo ii = Image.getImageInfo(in,info);
	RewindableStream.rewind(in);
	if (ii.format == ii.FORMAT_GIF){
		IImage got = ewe.graphics.AnimatedIcon.getAnimatedImageFromGIF(imageBytes);
		if (got instanceof mImage) return got;
		else return new mImage(got);
	}
	if (requestedSize == null || !ii.canScale) return new mImage(new Image(in,createOptions));
	return new mImage(new Image(in,createOptions,requestedSize.width,requestedSize.height));
}

//##################################################################
}
//##################################################################

