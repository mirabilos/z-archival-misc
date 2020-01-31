/*
Note - this is the Linux version of Image.java
*/
/*********************************************************************************
 *  Ewe Virtual Machine - Version 1.14, January 24, 2002                         *
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
import ewe.sys.SystemResourceException;
import ewe.sys.UseNonNativeMethodException;
import ewe.sys.Vm;
import ewe.util.ByteArray;

/**
 * Image is a rectangular image.
 * <p>
 * You can draw into an image and copy an image to a surface using a Graphics
 * object.
 * @see Graphics
 */
public class Image implements ISurface,IImage,ImageData
{
int width; //This must be first.
int height; //This must be second.
public Color transparent; //This must be third.
public Color background; //This must be fourth.
Color maskedWith; //This must be fifth
Image colorMask, colorImage; //This must be sixth, seventh .
public String name; //This must be eighth
public boolean freed = false; //This must be nineth
public boolean hasAlpha = false; //This must be tenth
boolean wasLoaded = false; // This must be eleventh
byte [] alphaChannel = null;// This must be twelvth
//int myOptions = 0; // This must be thirteenth
//int[] colorMap; // This must be fourteenth and last

private static final boolean NoImageDataSupport = true;
/**
@deprecated - use RGB_IMAGE instead.
* This is an option to be used in Image initializers.
Use this when creating an image to force the use of 24-bit or 32-bit
* images. Use it without ALPHA_CHANNEL for 24-bit.
**/
public static final int TRUE_COLOR = 0x1;
/**
* This is an option to be used in Image initializers.
* Use this when creating an image with an alpha (transparency) channel.
**/
public static final int ALPHA_CHANNEL = 0x2;
/**
This is an option to be  used in Image initializers.
It is used to specify a 24-bit image RGB image. It is the same as TRUE_COLOR
**/
public static final int RGB_IMAGE = 0x1;
/**
This is an option to be  used in Image initializers.
It is used to specify a 32-bit image RGB image with an Alpha (transparency)
channel. It is the same as RGB_IMAGE|ALPHA_CHANNEL.
**/
public static final int ARGB_IMAGE = 0x1|0x2;
/**
* @deprecated - use INDEXED_IMAGE instead.
* Use this to specify an indexed image. If neither INDEXED nor TRUE_COLOR is
* specified in the option for the constructor - then the appropriate one will
* be picked depending on the image type (i.e. INDEXED for bitmap images, and
* TRUE_COLOR for PNG images).
**/
public static final int INDEXED = 0x4;
/**
Use this to specify an indexed image that is optimized for the display.
**/
public static final int INDEXED_IMAGE = 0x4;
/**
* Used as an option with the constructor Image(Image other,int options).
* Use this to specify that the new image is to be displayed on screen. This ensures that
* its alpha pixels are correctly rendered when used with Java 1.1 and PersonalJava.
**/
public static final int FOR_DISPLAY = 0x8;
/**
* This is an option for use with the constructor Image(ewe.io.Stream stream,int options,int requestedWidth,int requestedHeight)
* It specifies that the image should be scaled in the X and Y plane to be exactly the size
* specified. If this is omitted the image will be scaled to fit in the specfied dimensions
* but keeping the same aspect ratio.
**/
public static final int SCALE_IMAGE = 0x10;
/**
Use this to specify a monochrome image, thereby using the smallest amount of space
for the image.
**/
public static final int MONO_IMAGE = 0x20;
/**
Use this to specify a black and white image that is optimized for the
default display. This may be actually represented by a gray scale image.
**/
public static final int BLACK_AND_WHITE_IMAGE = 0x40;
/**
Use this to specify a GRAY_SCALE_IMAGE image that is optimized for the
default display - without specifying the actual number of gray levels.
**/
public static final int GRAY_SCALE_IMAGE = 0x800;
/**
Use this to specify a GRAY_SCALE_IMAGE image at one byte per pixel.
**/
public static final int GRAY_SCALE_256_IMAGE = 0x400;
/**
Use this to specify a GRAY_SCALE_IMAGE image at one byte per pixel.
**/
public static final int GRAY_SCALE_16_IMAGE = 0x200;
/**
Use this to specify a GRAY_SCALE_IMAGE image at two bits per pixel.
**/
public static final int GRAY_SCALE_4_IMAGE = 0x100;
/**
Use this to specify a GRAY_SCALE_IMAGE image at one bit per pixel - this is the same
as MONO_IMAGE;
**/
public static final int GRAY_SCALE_2_IMAGE = MONO_IMAGE;
/**
Use this to specify an INDEXED_IMAGE image at one byte per pixel.
**/
public static final int INDEXED_256_IMAGE = 0x4000;
/**
Use this to specify an INDEXED_IMAGE image at 4 bits per pixel.
**/
public static final int INDEXED_16_IMAGE = 0x2000;
/**
Use this to specify an INDEXED_IMAGE image at 2 bits per pixel.
**/
public static final int INDEXED_4_IMAGE = 0x1000;
/**
Use this to specify an INDEXED_IMAGE image at 2 bits per pixel.
**/
public static final int INDEXED_2_IMAGE = 0x8000;
/**
* Used as an option with the constructor Image(Image other,int options).
* Use this to specify that the new image is to be displayed on screen. This ensures that
* its alpha pixels are correctly rendered when used with Java 1.1 and PersonalJava.
**/
public static final int FASTEST_DISPLAY_IMAGE = 0x10000;
/**
Use this during image creation that the EXACT image type must be created
and if it cannot, then an ImageTypeNotSupportedException should be thrown.
Note that this does not apply to INDEXED_IMAGE - which tells the system to
create the best INDEXED_IMAGE suitable for display on the platform, or BLACK_AND_WHITE_IMAGE
which tells the system to create the best image type for display on the platform
which will display only black and white pixels.
**/
public static final int EXACT_TYPE_IMAGE = 0x20000000;


private static final int AN_INDEXED_TYPE = (INDEXED_256_IMAGE|INDEXED_16_IMAGE|INDEXED_4_IMAGE|INDEXED_2_IMAGE);
private static final int A_GRAYSCALE_TYPE = (GRAY_SCALE_256_IMAGE|GRAY_SCALE_16_IMAGE|GRAY_SCALE_4_IMAGE|GRAY_SCALE_2_IMAGE);
private static final int TYPE_IS_SPECIFIED = (AN_INDEXED_TYPE|A_GRAYSCALE_TYPE|RGB_IMAGE);

public boolean usesAlpha() {return hasAlpha;}

private static final int INFO_BITS_PER_PIXEL = 1;
private static final int INFO_BYTES_PER_LINE = 2;
//private native int getNativeInfo(int which, Object[] dest);

/**
 * Returns true if this image was decoded. A decoded image cannot be drawn to.
 */
//===================================================================
public boolean wasDecoded() {return wasLoaded;}
//===================================================================
/**
* This tells the VM that you will no longer be making changes to this Image. It
* gives the VM the option of caching pixel information about the Image. This is not
* necessary for Images that are decoded from a Formatted image representation.
**/
//===================================================================
public void freeze()
//===================================================================
{
	wasLoaded = true;
}

private native void _nativeCreate(int options)throws ewe.sys.SystemResourceException;

/**
* Use this to load an image from an array of bytes.
**/
/*
public Image(ByteArray image,int options) throws IllegalArgumentException, ewe.sys.SystemResourceException
{
	decodeFrom(image,options);
}
*/

/**
* If this is set to a value > 0 then whenever numImages % debugImageSize is zero during
* creation, the number of images will be displayed on the screen.
**/
public static int debugImageSize = 0;
/**
* This is the number of Images that have been created but not freed. This will also
* count images that have been garbage collected but not freed before collection.
**/
public static int numImages = 0;

//##################################################################
{
/*
	numImages++;
	if (debugImageSize > 0)
		if (numImages % debugImageSize == 0)
			ewe.sys.Vm.debug("Images: "+numImages);
*/
}
//##################################################################

public boolean enableAlpha()
{
	if (wasLoaded) return false;
	hasAlpha = true;
	if (alphaChannel == null)
		Mask.makeOpaque(alphaChannel = new byte[width*height]);
	return true;
}

//-------------------------------------------------------------------
protected Image(){}
//-------------------------------------------------------------------

//-------------------------------------------------------------------
private native void _nativeLoad(ewe.io.BasicStream file);
//-------------------------------------------------------------------
private static boolean hasNative = true;

//-------------------------------------------------------------------
private void throwBadFormat() throws IllegalArgumentException
//-------------------------------------------------------------------
{
	String str = "The image ";
	if (name != null) str += name+" ";
	str += "cannot be decoded.";
	throw new IllegalArgumentException(str);
}
//-------------------------------------------------------------------
protected void setSize(ImageCodec ic,Rect sourceArea,int requestedWidth,int requestedHeight,int options)
//-------------------------------------------------------------------
{
	width = ic.width; height = ic.height;
	if (sourceArea != null){
		width = height = 1;
 		if (!ic.isJPEGFile) throw new IllegalArgumentException("Cannot decode a portion of this image.");
		if (sourceArea.x < 0 || sourceArea.x + sourceArea.width > ic.width || sourceArea.y < 0 || sourceArea.y + sourceArea.height > ic.height)
			throw new IllegalArgumentException("The source area is not completely within the image.");
		width = sourceArea.width;
		height = sourceArea.height;
	}
	if (requestedWidth > 0 && requestedHeight > 0 && !ic.isJPEGFile) throw new IllegalArgumentException("Cannot decode scaled version of this image.");
	if (!ic.isJPEGFile || requestedWidth <= 0 || requestedHeight <= 0) return;
	if ((options & SCALE_IMAGE) != 0){
		width = requestedWidth;
		height = requestedHeight;
	}else{
		double xscale =(double)requestedWidth/width;
		double yscale = (double)requestedHeight/height;
		double scale = Math.min(xscale,yscale);
		width = (int)(scale*width);
		height = (int)(scale*height);
		if (width < 1) width = 1;
		if (height < 1) height = 1;
	}
	ic.width = width;
	ic.height = height;
}
//-------------------------------------------------------------------
static ByteArray getBytes(ewe.io.Stream stream) throws ewe.io.IOException
//-------------------------------------------------------------------
{
		ewe.io.MemoryFile mf = new ewe.io.MemoryFile();
		new ewe.io.IOTransfer().transfer(stream,mf);
		return mf.data;
}
//-------------------------------------------------------------------
private static ImageInfo getInfo(Object source,ImageInfo destination) throws IllegalArgumentException, ewe.io.IOException
//-------------------------------------------------------------------
{
	ByteArray image = source instanceof ByteArray ? (ByteArray)source : null;
	ewe.io.Stream stream = image == null ? (ewe.io.Stream)source : new ewe.io.MemoryFile(image.data,0,image.length,"r");
	ewe.io.RandomAccessStream ras = ewe.io.RewindableStream.toRewindableStream(stream);
	ImageCodec ic = new ImageCodec();
	if (!ic.decode(ras)) throw new IllegalArgumentException();
	if (destination == null) destination = new ImageInfo();
	destination.width = ic.width;
	destination.height = ic.height;
	destination.canScale = (ic.isJPEGFile);

	if (ic.isJPEGFile) destination.format = destination.FORMAT_JPEG;
	else if (ic.isGIFFile) destination.format = destination.FORMAT_GIF;
	else if (ic.isBMPFile) destination.format = destination.FORMAT_BMP;
	else destination.format = destination.FORMAT_PNG;

	destination.size = ic.width*ic.height*4;

	return destination;
}
/**
 * Get the information on an Image as stored in a formatted form.
 * @param imageBytes The bytes for the image.
 * @param destination An optional destination ImageInfo object.
 * @return An ImageInfo object holding information on the image.
 * @exception IllegalArgumentException If the image bytes is a corrupted or not recognized format.
 */
//===================================================================
public static ImageInfo getImageInfo(ByteArray imageBytes,ImageInfo destination) throws IllegalArgumentException
//===================================================================
{
	try{
		return getInfo(imageBytes,destination);
	}catch(ewe.io.IOException e){
		throw new IllegalArgumentException();
	}
}
/**
 * Get the information on an Image as stored in a formatted form.
 * @param imageBytes The bytes for the image.
 * @param destination An optional destination ImageInfo object.
 * @return An ImageInfo object holding information on the image.
 * @exception IllegalArgumentException If the image bytes is a corrupted or not recognized format.
 */
//===================================================================
public static ImageInfo getImageInfo(ewe.io.Stream imageBytes,ImageInfo destination) throws IllegalArgumentException, ewe.io.IOException
//===================================================================
{
	return getInfo(imageBytes,destination);
}
//-------------------------------------------------------------------
private void decodeFrom(Object source,int options,int requestedWidth,int requestedHeight) throws ewe.io.IOException, IllegalArgumentException
//-------------------------------------------------------------------
{
	decodeFrom(source,options,null,requestedWidth,requestedHeight);
}


//-------------------------------------------------------------------
private void decodeFrom(Object source,int options,Rect sourceArea,int requestedWidth,int requestedHeight) throws ewe.io.IOException, IllegalArgumentException
//-------------------------------------------------------------------
{
	wasLoaded = true;
	ByteArray image = source instanceof ByteArray ? (ByteArray)source : null;
	ewe.io.Stream stream = image == null ? (ewe.io.Stream)source : new ewe.io.MemoryFile(image.data,0,image.length,"r");
	ewe.io.RandomAccessStream ras = ewe.io.RewindableStream.toRewindableStream(stream);
	ImageCodec ic = new ImageCodec();
	if (!ic.decode(ras)) throwBadFormat();
	try{
		setSize(ic,sourceArea,requestedWidth,requestedHeight,options);
		ewe.io.RewindableStream.rewind(ras);
		if (ic.isBMPFile){
			if (image == null) image = getBytes(ras);
			if (hasNative){
				//ewe.sys.Vm.debug("Decoding Bmp natively!");
				try{_nativeLoad(ras); return;}catch(UnsatisfiedLinkError l){hasNative = false;}
			}
			try{
				//ewe.sys.Vm.debug("Decoding Bmp non-natively!");
				if (!readBMP(image,"-- bitmap --")) width = height = 0;
			}catch(Exception e){
				e.printStackTrace();
				width = height = 0;
			}
		}else {
			if ((options & (TRUE_COLOR|INDEXED)) == 0)
				options |= TRUE_COLOR;
			_nativeCreate(options);
			if (!ic.toImage(this,sourceArea)) {
				width = height = 0;
			}
		}
	}finally{
		if (width <= 0 || height <= 0) throwBadFormat();
	}
}
/*
//-------------------------------------------------------------------
private void decodeFrom(ByteArray image,int options) throws IllegalArgumentException, ewe.sys.SystemResourceException
//-------------------------------------------------------------------
{
	wasLoaded = true;
	ewe.io.RandomAccessStream ras = new ewe.io.MemoryFile(image.data,0,image.length,ewe.io.RandomAccessFile.READ_ONLY);
	ImageCodec ic = new ImageCodec();
	if (!ic.decode(ras)) throwBadFormat();
	try{
	width = ic.width; height = ic.height;
	ras.seek(0);
	if (ic.isGIFFile){
		width = height = 0;
		return;
	}
	if (ic.isBMPFile){
		if (hasNative)
			try{_nativeLoad(ras); return;}catch(UnsatisfiedLinkError l){hasNative = false;}
		try{
			if (!readBMP(image,"-- bitmap --")) width = height = 0;
		}catch(Exception e){
			width = height = 0;
		}
	}else {
		if ((options & (TRUE_COLOR|INDEXED)) == 0)

			options |= TRUE_COLOR;
		_nativeCreate(options);
		if (!ic.toImage(this)) {
			width = height = 0;

		}
	}
	}finally{
		if (width <= 0 || height <= 0) throwBadFormat();
	}
}
*/
// Intel architecture getUInt32
private static int inGetUInt32(byte bytes[], int off)
	{
	return ((bytes[off + 3]&0xFF) << 24) | ((bytes[off + 2]&0xFF) << 16) |
		((bytes[off + 1]&0xFF) << 8) | (bytes[off]&0xFF);
	}

// Intel architecture getUInt16
private static int inGetUInt16(byte bytes[], int off)
	{
	return ((bytes[off + 1]&0xFF) << 8) | (bytes[off]&0xFF);
	}
private int readBytes(ByteArray data, byte b[], int offset)
	{
	ewe.sys.Vm.copyArray(data.data,offset,b,0,b.length);
	return b.length;
	}

private void pixelsToRGB(int bitsPerPixel, int width, byte pixels[], int pixelOffset,
	int rgb[], int rgbOffset, int cmap[])
	{
	if (bitsPerPixel == 24){
		int alpha = 255<<24;
		for (int i = 0, px = pixelOffset; i<width; i++){
			rgb[rgbOffset+i] = alpha|((int)pixels[px] & 0xff)<< 0 | ((int)pixels[px+1] & 0xff) << 8 | ((int)pixels[px+2] & 0xff) << 16;
			px += 3;
		}
		return;
	}
	int mask, step;
	if (bitsPerPixel == 1)
		{
		mask = 0x1;
		step = 1;
		}
	else if (bitsPerPixel == 4)
		{
		mask = 0x0F;
		step = 4;
		}
	else // bitsPerPixel == 8
		{
		mask = 0xFF;
		step = 8;
		}
	int bit = 8 - step;
	int bytnum = pixelOffset;
	int byt = pixels[bytnum++];
	int x = 0;
	while (true)
		{
		int colorIndex = ((mask << bit) & byt) >> bit;
		rgb[rgbOffset++] = cmap[colorIndex] | (0xFF << 24);
		if (++x >= width)
			break;
		if (bit == 0)
			{
			bit = 8 - step;
			byt = pixels[bytnum++];
			}
		else
			bit -= step;
		}
	}

public static boolean debugImages = false;
private boolean readBMP(ByteArray data, String name)
	{
	// read header (54 bytes)
	// 0-1   magic chars 'BM'
	// 2-5   uint32 filesize (not reliable)
	// 6-7   uint16 0
	// 8-9   uint16 0
	// 10-13 uint32 bitmapOffset
	// 14-17 uint32 info size
	// 18-21 int32  width
	// 22-25 int32  height
	// 26-27 uint16 nplanes
	// 28-29 uint16 bits per pixel
	// 30-33 uint32 compression flag
	// 34-37 uint32 image size in bytes
	// 38-41 int32  biXPelsPerMeter (unused)
	// 32-45 int32  biYPelsPerMeter (unused)
	// 46-49 uint32 colors used (unused)
	// 50-53 uint32 important color count (unused)
	int off = 0;
	byte header[] = new byte[54];
	off += readBytes(data, header, off);
	if (header[0] != 'B' || header[1] != 'M')
		{
		ewe.sys.Vm.debug("ERROR: " + name + " is not a BMP image");
		return false;
		}

	int bitmapOffset = inGetUInt32(header, 10);

	int infoSize = inGetUInt32(header, 14);
	if (infoSize != 40)
		{
		ewe.sys.Vm.debug("ERROR: " + name + " is old-style BMP");
		return false;
		}
	int width = inGetUInt32(header, 18);
	int height = inGetUInt32(header, 22);
	if (width < 0 || height < 0 || width > 65535 || height > 65535)
		{
		ewe.sys.Vm.debug("ERROR: " + name + " has invalid width/height");
		return false;
		}
	int bpp = inGetUInt16(header, 28);
	if (bpp != 1 && bpp != 4 && bpp != 8 && bpp != 24)
		{
		ewe.sys.Vm.debug("ERROR: " + name + " is not a 2, 16, 256 or 24-bit color image");
		return false;
		}
	int compression = inGetUInt32(header, 30);
	if (compression != 0)
		{
		ewe.sys.Vm.debug("ERROR: " + name + " is a compressed image");
		return false;
		}
	int numColors = 1 << bpp;
	int scanlen = (width * bpp + 7) / 8; // # bytes
	scanlen = ((scanlen + 3) / 4) * 4; // end on 32 bit boundry
	// read colormap
	//
	// 0-3 uint32 col[0]
	// 4-7 uint32 col[1]
	// ...

	int cmapSize = bitmapOffset - 54;
	byte cmapData[] = new byte[cmapSize];
	if (cmapSize != 0)
		off += readBytes(data, cmapData,off);

	if (cmapSize == 0) numColors = 0;
	int cmap[] = new int[numColors];
	int j = 0;
	for (int i = 0; i < numColors && j<=cmapSize-4; i++)
		{
		byte blue = cmapData[j++];
		byte green = cmapData[j++];
		byte red = cmapData[j++];
		j++; // skip reserved
		cmap[i] = ((red & 0xFF) << 16) | ((green & 0xFF) << 8) | (blue & 0xFF);
		}

	// read pixels and store in RGB buffer
	int [] rgb = new int[width * height];
	byte pixels[] = new byte[scanlen];
	for (int y = height - 1; y >= 0; y--)
		{
		off += readBytes(data, pixels, off);
		if (width == 0)
			continue;
		pixelsToRGB(bpp, width, pixels, 0, rgb, y * width, cmap);
		}
	if (debugImages)
		ewe.sys.Vm.debug(width+", "+height+", "+scanlen);

	// create the image from the RGB buffer
	this.width = width;
	this.height = height;
	_nativeCreate(TRUE_COLOR);
// Set pixels
	setPixels(rgb,0,0,0,width,height,0);
	return true;
/*
	java.awt.image.MemoryImageSource imageSource;
	imageSource = new java.awt.image.MemoryImageSource(width, height, rgb, 0, width);
	java.awt.Toolkit awtToolkit = java.awt.Toolkit.getDefaultToolkit();
	_awtImage = awtToolkit.createImage(imageSource);
	if (is12)
		try{
			java.awt.Image old = _awtImage;
			_awtImage = toBufferedImage(_awtImage);
			old.flush();
			rgb = null;
		}catch(Error e){}
	*/

	}


/**
 * Sets one or more row(s) of pixels in an image. This method sets the values of
 * a number of pixel rows in an image and is commonly used when writing code

 * to load an image from a stream such as a file. The source pixels byte array
 * must be in 1, 4 or 8 bit per pixel format with a matching color map size
 * of 2, 16 or 256 colors.
 * <p>
 * Each color in the color map of the source pixels is identified by a single
 * integer value. The integer is composed of 8 bits (value [0..255]) of red,
 * green and blue using the following calculation:
 * <pre>
 * int color = (red << 16) | (green << 8) | blue;
 * </pre>
 * As an example, to load a 16 color image, we would pass bitsPerPixel
 * as 4 and would create a int array of 16 values for the color map.
 * Then we would set each of the values in the color map to the colors
 * used using the equation above. We could then either read data line
 * by line from the source stream, calling this method for each row of
 * pixels or could read a number of rows at once and then call this
 * method to set the pixels.
 * <p>
 * The former approach uses less memory, the latter approach is faster.
 *
 * @deprecated - Use the other setPixels() instead.
 * @param bitsPerPixel bits per pixel of the source pixels (1, 4 or 8) for index images,
 * 32 for true color ARGB images.
 * @param colorMap the color map of the source pixels - must be 2, 16 or 256 in length)
 * or it can be null for true color ARGB images.
 * @param bytesPerRow number of bytes per row of pixels in the source pixels array
 * @param numRows the number of rows of pixels in the source pixels array
 * @param y y coordinate in the image to start setting pixels
 * @param pixels array containing the source pixels - for true color ARGB images
 * this can be an array of integers. For indexed images it should be an array of bytes.
 */

public native void setPixels(int bitsPerPixel, int colorMap[], int bytesPerRow,
	int numRows, int y, Object pixels);


/**
 * Sets the image width and height to 0 and frees any systems resources
 * associated with the image.
 */
public void free()
{
	if (freed) return;
	if (colorMask != null) colorMask.free();
	if (colorImage != null) colorImage.free();
	nativeFree();
	freed = true;
	numImages--;
	//ewe.sys.Vm.debug("Freed: "+numImages);
}
private native void nativeFree();



/** Returns the height of the image. */
public int getHeight()
	{
	return height;

	}

/** Returns the width of the image. */

public int getWidth()
	{
	return width;
	}
public Color getBackground()
{
	return background;
}
public void draw(Graphics g,int x,int y,int options)
{
	if (!hasAlpha)
		g.drawImage(this,x,y);
	else{
		g.setDrawOp(Graphics.DRAW_ALPHA);
		g.drawImage(this,null,null,x,y,width,height);
	}

}
/**
* Sets a block of pixels in ARGB integer values. Each value in the source
* array represents a single pixel in Alpha-Red-Green-Blue format (8-bits per color). "offset"
* is the start of the int values in the array. x,y,width,height specify the rectangle that
* the pixels cover (a height of 1 represents a single line of pixels). At the moment, no options
* are defined.
**/
public native void setPixels(int [] source,int offset,int x,int y,int width,int height,int options);
/**
* Gets a block of pixels in ARGB integer values. Each value put in the destination
* array represents a single pixel in Alpha-Red-Green-Blue format (8-bits per color). "offset"
* is the start of the int values in the array. x,y,width,height specify the rectangle that
* the pixels cover (a height of 1 represents a single line of pixels). At the moment, no options
* are defined.
**/
public native int [] getPixels(int [] dest,int offset,int x,int y,int width,int height,int options);

//==================================================================
public static Image invert(Image what)
//==================================================================
{
	int w = what.getWidth();
	int h = what.getHeight();
	Image inverted = new Image(w,h);
	Graphics g = new Graphics(inverted);

	g.setColor(0,0,0);
	g.fillRect(0,0,w,h);
	g.setDrawOp(g.DRAW_XOR);
	g.drawImage(what,0,0);
	g.free();
	return inverted;
}

//-------------------------------------------------------------------
byte [] toMono(int [] rgb,int width,int height,int [] imageMask)
//-------------------------------------------------------------------
{
	int bpl = width/8;
	byte [] ret = new byte[bpl*height];
	byte mask = (byte)0x80;
	int i = 0;
	int s = 0;
	boolean isMask = imageMask == null;
	for (int y = 0; y<height; y++){
		for (int x = 0; x<width; x++){

			if (mask == 0) {
				i++;
				mask = (byte)0x80;
			}
			int r = rgb[s];
			if (isMask) {if (r == 0) ret[i] |= mask;}
			else if (imageMask[s] != 0 && r != 0) ret[i] |= mask;
			s++;
			mask = (byte)((mask >> 1) & 0x7f);
		}
	}
	return ret;
}

private native int nativeToCursor(Image mask,int hotx,int hoty);
/*
* This transforms the image into a mouse cursor suitable for use on the current
* platform. Will return null if the cursor could not be created.
**/
//===================================================================
Object toCursor(Image mask,Point hotSpot)
//===================================================================
{
	Image mask2 = invert(mask);
	int got = nativeToCursor(mask2,hotSpot.x,hotSpot.y);
	mask2.free();
	mask.free();
	free();
	if (got == 0) return null;
	return new ewe.sys.Long().set(got);
/*
	if (hotSpot == null) hotSpot = new Point(0,0);
	Dimension size = new Dimension();
	if (!getCursorSize(size)) return null;
	Image source = this;
	if (width != size.width || height != size.height) {
		source = new Image(size.width,size.height);
		Graphics g = new Graphics(source);
		Rect src = new Rect(0,0,width,height);
		Rect dest = new Rect(0,0,size.width,size.height);
		g.drawImage(this,null,null,src,dest,0);
		g.free();
		free();
		Image temp = new Image(size.width,size.height);
		g = new Graphics(temp);
		g.drawImage(mask,null,null,src,dest,0);
		g.free();
		mask.free();
		mask = temp;
	}
	int w = size.width, h = size.height;
	int [] ret = source.getPixels(new int[w*h],0,0,0,w,h,0);
	int [] maskRgb = mask.getPixels(new int[w*h],0,0,0,w,h,0);
	byte [] and = toMono(maskRgb,w,h,null);
	byte [] xor = toMono(ret,w,h,maskRgb);
	int got = toCursor(and,xor,hotSpot.x,hotSpot.y,w,h);
	mask.free();
	source.free();
	if (got == 0) return null;
	return new ewe.sys.Long().set(got);
*/
}
/**
 * Create a native Icon object.
This will create a native Icon reference which can be used for any system calls that require a native Icon.
For this to work the image must be defined with all pixels that are to be transparent being white.
Other pixels that are not to be transparent can also be white. The mask must be loaded from a
<b>monochrome ".bmp" file</b> and it should be black where the icon is not to be transparent and white where
it is to be transparent.
 * @param mask An image that is used as a mask for this image to define the transparent area of the icon.
 * @return An Object which can be passed to any method that requires a native icon reference. It will return null
 * if an icon could not be made.
 */
//===================================================================
public Object toIcon(Image mask)
//===================================================================
{
	//ewe.sys.Vm.debug("Inverting! "+mask.width+", "+mask.height);
/*
	Image image = new Image(this,0);
	Image msk2 = invert(mask);
	Graphics g = new Graphics(image);
	g.setDrawOp(g.DRAW_XOR);
	g.drawImage(msk2,0,0);
	g.free();
	int got = toIcon(image,mask,0);
	image.free();
	msk2.free();
*/
	int got = toIcon(this,mask,0);
	if (got == 0) return null;
	return new ewe.sys.Long().set(got);
}
/**


 * Create a platform specific version of this image. Under Win32 this returns itself. Under Java it
	returns a java.awt.Image.
 * @return a native version of the image.
 */
//===================================================================
public Object toNativeImage(Object colorOrMask)
//===================================================================
{
	return this;
}
//-------------------------------------------------------------------
private native int getNativeResourcePointer();
//-------------------------------------------------------------------
/**
* Return an Object that holds a reference to the native resource used
* by the Image. On a Java VM this will be a java.awt.Image, while on other platforms
* this will be an ewe.sys.Long() object which contains a 32-bit pointer to a
* platform specific structure. The structure will be defined in the "eni.h" and
* will allow you to access the image bits directly.
**/
//===================================================================
public Object getNativeResource()
//===================================================================
{
	return new ewe.sys.Long().set(getNativeResourcePointer());
}
/**
 * Create a native Icon given the name of an Image and the name of its mask. The mask must be a monochrome
	BMP file. The images created are freed and only the native Icon itself is returned.
 * @param imageName The name of the image.
 * @param maskName The name of the mask
 * @return a native Icon.
 */
//===================================================================
public static Object toIcon(String imageName,String maskName)
//===================================================================
{
	Image image = new Image(imageName);
	Image mask = new Image(maskName);
	Object ret = image.toIcon(mask);

	image.free();
	mask.free();
	return ret;
}
//-------------------------------------------------------------------
private static native boolean getCursorSize(Dimension dest);
//-------------------------------------------------------------------
//-------------------------------------------------------------------
//private static native int toCursor(byte [] imageBits,byte [] maskBits,int x,int y,int width,int height);
//-------------------------------------------------------------------
private static native int toIcon(Image image,Image mask,int options);
//-------------------------------------------------------------------

//Make native!
//-------------------------------------------------------------------
protected static native void fixMasks(Image image,Image mask,Color c);
//-------------------------------------------------------------------
/*
{
	int col = c.getRed() << 16 | c.getGreen() << 8 | c.getBlue();
	int [] line = new int[width];
	for (int y = 0; y<height; y++) {
		colorMask.getPixels(line,0,0,y,width,1,0);
		for (int x = 0; x<width; x++){

			if ((line[x] & 0xffffff) == col) line[x] = 0;
			else line[x] = 0xffffff;
		}
		colorMask.setPixels(line,0,0,y,width,1,0);
		colorImage.getPixels(line,0,0,y,width,1,0);
		for (int x = 0; x<width; x++){
			if ((line[x] & 0xffffff) == col) line[x] = 0xffffff;
		}
		colorImage.setPixels(line,0,0,y,width,1,0);
	}
}
*/
//-------------------------------------------------------------------
boolean getColorMasks(Color transparent)

//-------------------------------------------------------------------
{
		if (maskedWith != null)
			if (maskedWith.equals(transparent)) return true;
		maskedWith = transparent;
		if (colorImage != this && colorImage != null) colorImage.free();
		if (colorMask != this && colorMask != null) colorMask.free();
		colorImage = new Image(width,height,TRUE_COLOR);
		Graphics g = new Graphics(colorImage);
		g.drawImage(this,0,0); g.free();
		colorMask = new Image(width,height,TRUE_COLOR);
		g = new Graphics(colorMask);
		g.drawImage(this,0,0); g.free();
		fixMasks(colorImage,colorMask,transparent);
		return true;
}

//===================================================================
public void makeGray(Color transparent)
//===================================================================
{

	if (width == 0 || height == 0) return;
	int [] all = new int[width*height];
	getPixels(all,0,0,0,width,height,0);
	grayPixels(all,0,all.length,transparent == null ? -1 : transparent.toInt() & 0xffffff);
	setPixels(all,0,0,0,width,height,0);
}
//-------------------------------------------------------------------
private static native void grayPixels(int [] buff,int start,int length,int transparent);
//-------------------------------------------------------------------
/*
{
	for (int i = 0; i<length; i++){
		int val = buff[i+start];
		if ((val & 0xffffff) == transparent) continue;
		int r = (val >> 16) & 0xff, g = (val >> 8) & 0xff, b = val & 0xff;
		int tot = r+g+b;
		if (tot == 0) tot = 3*64;
		int newVal = tot/3;
		val = (val & 0xff000000) | (newVal << 16) | (newVal << 8) | newVal;
		buff[i+start] = val;
	}
}
*/

/*
{
	int [] dest = new int[width];
	int t;
	for (int y = 0; y<height; y++){
		getPixels(dest,0,0,y,width,1,0);
		if (degree == 180 || degree == 270)
			for (int s = 0; s<width/2; s++){
				t = dest[s];
				dest[s] = dest[width-1-s];
				dest[width-1-s] = t;
			}
		if (degree == 0) destination.setPixels(dest,0,0,y,width,1,0);
		else if (degree == 90) destination.setPixels(dest,0,height-1-y,0,1,width,0);

		else if (degree == 180) destination.setPixels(dest,0,0,height-1-y,width,1,0);
		else if (degree == 270) destination.setPixels(dest,0,y,0,1,width,0);
	}
}
*/
/**
 * This rotates the image and places it into the destination Image (which should)
 * be the correct size to receive the rotated image. If the destination Image is null a new
 * one will be created and returned.
 * @param destination The destination image. If it is null it will return a new Image.
 * @param degree This should be 90 for a 90 degree clockwise rotation, 180 for 180 degree rotation
 * or 270 for 270 degree clockwise rotation (or 90 degree anti-clockwise rotation).
 * @return The destination image or a new Image if destination was null.
 */
//===================================================================
public Image rotate(Image destination,int degree)
//===================================================================
{
	while (degree < 0) degree += 360;
	boolean changeAspect = degree == 90 || degree == 270;
	if (destination == null)
		destination = changeAspect ? new Image(height,width,TRUE_COLOR) : new Image(width,height,TRUE_COLOR);
	if (width == 0 || height == 0) return destination;
	switch(degree){
		case 90: case 270:
			if (destination.width < height || destination.height < width)
				return destination;
			break;
		default:
			if (destination.width < width || destination.height < height)
				return destination;
	}
	try{
		doRotate(destination,degree);
	}catch(UseNonNativeMethodException e){
		PixelBuffer pb = new PixelBuffer(this);
		int tx = PixelBuffer.TRANSFORM_ROTATE_90;
		if (degree == 270) tx = PixelBuffer.TRANSFORM_ROTATE_270;
		else if (degree == 180) tx = PixelBuffer.TRANSFORM_ROTATE_180;
		pb = pb.transform(tx,null);
		Graphics g = new Graphics(destination);
		pb.draw(g,0,0,0);
		g.free();
	}
	return destination;
}

private native void doRotate(Image destination,int degree) throws UseNonNativeMethodException;
/*
public Image scale(Rect sourceArea,Rect destArea,Image destination)
{
	throw new UnsatisfiedLinkError();
}
*/
public Image scale(int newWidth, int newHeight)
{
	return scale(newWidth,newHeight,0);
}
public Image scaleStrip(int newWidth,int newHeight,int destOptions,int yOffset,int height,Image destination)
{
	throw new UnsatisfiedLinkError();
}
public Image scale(int newWidth, int newHeight,int destOptions)
{
	return new PixelBuffer(this).scale(newWidth,newHeight).toImage();
	//return scale(new Rect(0,0,width,height),new Rect(0,0,newWidth,newHeight),null);
}
/*
{
	int [] dest = new int[width];
	int t;
	for (int y = 0; y<height; y++){
		getPixels(dest,0,0,y,width,1,0);
		if (degree == 180 || degree == 270)
			for (int s = 0; s<width/2; s++){
				t = dest[s];
				dest[s] = dest[width-1-s];
				dest[width-1-s] = t;
			}
		if (degree == 0) destination.setPixels(dest,0,0,y,width,1,0);
		else if (degree == 90) destination.setPixels(dest,0,height-1-y,0,1,width,0);
		else if (degree == 180) destination.setPixels(dest,0,0,height-1-y,width,1,0);
		else if (degree == 270) destination.setPixels(dest,0,y,0,1,width,0);
	}
}
*/
//===================================================================
public int getImageScanLineType()
//===================================================================
{
	return SCAN_LINE_INT_ARRAY;
	/*
	int type = getImageType();
	if (type == TYPE_ARGB || type == TYPE_RGB) return SCAN_LINE_INT_ARRAY;
	else return SCAN_LINE_BYTE_ARRAY;
	*/
}
//===================================================================
public int getImageScanLineLength()
//===================================================================
{
	return width;
	/*
	int type = getImageType();
	if (type == TYPE_UNKNOWN) return 0;
	int bpp = getBPP(type);
	if (bpp == 32) return width;
	return getNativeInfo(INFO_BYTES_PER_LINE,null);
	*/
}
//-------------------------------------------------------------------
//private native int nativeGetSetImageScanLines(boolean isGet,int startLine, int numLines, Object destArray, int offset, int destScanLineLength);
//-------------------------------------------------------------------

//===================================================================
public void getImageScanLines(int startLine, int numLines, Object destArray, int offset, int destScanLineLength)
//===================================================================
{
	if (destArray == null) throw new NullPointerException();
	//
	int sll = getImageScanLineLength();
	if (destScanLineLength <= 0) destScanLineLength = sll;
	else if (destScanLineLength < sll) throw new IllegalArgumentException();
	//
	int type = getImageType();
	//

	if (type != TYPE_ARGB && type != TYPE_RGB){
		/*
		byte[] dest = (byte[])destArray;
		int ret = nativeGetSetImageScanLines(true,startLine,numLines,destArray,offset,destScanLineLength);
		if (ret == -1) throw new IllegalStateException();
		*/
	}else{
		if (destScanLineLength == sll)
			getPixels((int[])destArray,offset,0,startLine,width,numLines,0);
		else for (int i = 0; i<numLines; i++){
			getPixels((int[])destArray,offset+i*destScanLineLength,0,startLine+i,width,1,0);
		}
	}
}
//===================================================================
public void setImageScanLines(int startLine, int numLines, Object sourceArray, int offset, int sourceScanLineLength)
//===================================================================
{
	if (sourceArray == null) throw new NullPointerException();
	//
	int sll = getImageScanLineLength();
	if (sourceScanLineLength <= 0) sourceScanLineLength = sll;
	else if (sourceScanLineLength < sll) throw new IllegalArgumentException();
	//
	int type = getImageType();
	if (type != TYPE_ARGB && type != TYPE_RGB){
		/*
		byte[] src = (byte[])sourceArray;
		int ret = nativeGetSetImageScanLines(false,startLine,numLines,src,offset,sourceScanLineLength);
		if (ret == -1) throw new IllegalStateException();
		*/
	}else{
		if (sourceScanLineLength == sll)
			setPixels((int[])sourceArray,offset,0,startLine,width,numLines,0);
		else for (int i = 0; i<numLines; i++){
			setPixels((int[])sourceArray,offset+i*sourceScanLineLength,0,startLine+i,width,1,0);
		}
	}
}
//===================================================================
public int getImageWidth()
//===================================================================
{
	return width;
}
//===================================================================
public int getImageHeight()
//===================================================================
{
	return height;
}

//===================================================================
//public boolean openImageData(boolean forWriting){return true;}
//===================================================================
public boolean isWritableImage() {return true;}
//===================================================================
//public boolean closeImageData(){return true;}
//===================================================================


/**
 * Creates an image of the specified width and height that can be used for
 * drawing.
* @param width The width of the image. This must be greater than 0.
* @param height The height of the image. This must be greater than 0.
* @param options one or more of the image option values (e.g. TRUE_COLOR, MONO_IMAGE, GRAY_SCALE_IMAGE etc).
* @exception IllegalArgumentException if the image width or height is not within range.
* @exception ewe.sys.SystemResourceException if the underlying system could not create
* the image because of system resource problems.
*/
//===================================================================
public Image(int width, int height, int options)
throws IllegalArgumentException, ewe.sys.SystemResourceException, ImageTypeNotSupportedException
//===================================================================
{
	this();
	if (width <= 0 || height <= 0) throw new IllegalArgumentException();
	try{
		this.width = width;
		this.height = height;
		if ((options & (FASTEST_DISPLAY_IMAGE|INDEXED_IMAGE)) != 0)
			options |= INDEXED_256_IMAGE;
			//options |= INDEXED_IMAGE;
		//myOptions = options;
		/*
		if ((options & INDEXED_IMAGE) != 0){
			_nativeCreate(options);
			return;
		}else */
		if ((options & RGB_IMAGE) != 0 || (options & TYPE_IS_SPECIFIED) == 0){
			_nativeCreate(options);
			return;
		}else if ((options & BLACK_AND_WHITE_IMAGE) != 0){
			options &= ~BLACK_AND_WHITE_IMAGE;
			options |= MONO_IMAGE;
			//myOptions = options;
		}
		create(width,height,options,null,0,0,null);
	}finally{
		if ((options & ALPHA_CHANNEL) != 0) enableAlpha();
	}
}
/**
 * Creates an image of the specified width and height that can be used for
 * drawing, with default (zero) options.
* @param width The width of the image. This must be greater than 0.
* @param height The height of the image. This must be greater than 0.
* @exception IllegalArgumentException if the image width or height is not within range.
* @exception ewe.sys.SystemResourceException if the underlying system could not create
* the image because of system resource problems.
*/
//===================================================================
public Image(int width, int height)
throws IllegalArgumentException, ewe.sys.SystemResourceException, ImageTypeNotSupportedException
//===================================================================
{
	this(width,height,0);
}
/**
* This is the best way to create a copy of an image. The new Image CAN be drawn to, even if the original image
* was a decoded BMP/PNG image. If the old image had an alpha channel, then this new one will
* as well.
* @param other The image to copy.
* @param options Options used when creating the new image.
* @exception ewe.sys.SystemResourceException if the underlying system could not create
* the image because of system resource problems.
*/
//===================================================================
public Image(Image other,int options) throws ewe.sys.SystemResourceException
//===================================================================
{
	this(other.width,other.height,options != 0 ? options : (other.hasAlpha ? TRUE_COLOR : 0));
	if ((options & FOR_DISPLAY) != 0) wasLoaded = true;
	transparent = other.transparent;
	background = other.background;
	if (other.hasAlpha || wasLoaded) {
		this.hasAlpha = true;
		if (other.alphaChannel != null){
			alphaChannel = new byte[other.alphaChannel.length];
			for (int i = 0; i<alphaChannel.length; i++)
				alphaChannel[i] = (byte)0xff;
			ewe.sys.Vm.copyArray(other.alphaChannel,0,alphaChannel,0,alphaChannel.length);
		}
		setPixels(other.getPixels(null,0,0,0,width,height,0),0,0,0,width,height,0);
	}else{
		Graphics g = new Graphics(this);
		g.drawImage(other,0,0);
		g.free();
	}
}
/**
* This creates an Image from a system dependant native image type. When running on a Java VM, a
* java.awt.Image object can be used. This will convert the Java image into a Ewe Image. There is no
* object currently that can be used with this constructor in a native Ewe VM.
* @param nativeImage an appropriate native image.
* @param options options for image creation.
* @exception IllegalArgumentException if the nativeImage is not appropriate.
* @exception ewe.sys.SystemResourceException if the underlying system could not create the image because of system resource problems.
*/
//===================================================================
public Image(Object nativeImage,int options) throws IllegalArgumentException, ewe.sys.SystemResourceException
//===================================================================
{
	this();
	if ((options & FOR_DISPLAY) != 0) wasLoaded = true;
	throw new IllegalArgumentException();
}
/**
 Loads and constructs an image from a resource or file which contains a formatted image.
 See the class documentation for information on which types of formatted images the Ewe VM
	can decode.<p>
	The path given is the path to the image file which can be in a ewe file or on the file system.
* @param path the name of the image resource or file.
* @param options creation options.
* @exception ImageDecodingException if there was an error decoding the image.
* @exception UnsupportedImageFormatException if the image format is not supported.
* @exception ImageNotFoundException if the image could not be found.
* @exception ewe.sys.SystemResourceException if the underlying system could not create the image because of system resource problems.
*/
//===================================================================
public Image(String path,int options)
throws ImageDecodingException, UnsupportedImageFormatException, ImageNotFoundException, ewe.sys.SystemResourceException
//===================================================================
{
	this();
	name = path;
	width = height = 0;
	byte [] all = ewe.sys.Vm.readResource(null,path);
	if (all == null) throw new ImageNotFoundException(path);
	ByteArray ba = new ByteArray(all);
	try{
		decodeFrom(ba,options,0,0);
	}catch(ewe.io.IOException e){
		throwBadFormat();
	}
}
/**
 Loads and constructs an image from a resource or file which contains a formatted image.
 See the class documentation for information on which types of formatted images the Ewe VM
	can decode.<p>
	The path given is the path to the image file which can be in a ewe file or on the file system.
* @param path the name of the image resource or file.
* @exception ImageDecodingException if there was an error decoding the image.
* @exception UnsupportedImageFormatException if the image format is not supported.
* @exception ImageNotFoundException if the image could not be found.
* @exception ewe.sys.SystemResourceException if the underlying system could not create the image because of system resource problems.
*/
//===================================================================
public Image(String path)
throws ImageDecodingException, UnsupportedImageFormatException, ImageNotFoundException, ewe.sys.SystemResourceException
//===================================================================
{
		this(path,0);
}
/**
Use this to load a formatted image from an array of bytes, optionally resizing a portion of the image during decoding
<b>if possible</b>. If resizing during decoding is not possible, the returned image will be
the full sized image. Currently resizing during decoding is only possible on a native Ewe
VM decoding a JPEG formatted image.
* @param image the formatted image bytes.
* @param options creation options.
* @param sourceArea an optional area within the source image to decode - if this is null, then the entire image is used.
* @param requestedWidth the desired width of the decoded image - if this is zero, then the original width is used.
* @param requestedHeight the desired height of the decoded image - if this is zero, then the original height is used.
* @exception ImageDecodingException if there was an error decoding the image.
* @exception UnsupportedImageFormatException if the image format is not supported.
* @exception ewe.sys.SystemResourceException if the underlying system could not create the image because of system resource problems.
*/
//===================================================================
public Image(ByteArray image,int options,Rect sourceArea,int requestedWidth,int requestedHeight)
throws ImageDecodingException, UnsupportedImageFormatException, ewe.sys.SystemResourceException
//===================================================================
{
	this();
	try{
		decodeFrom(image,options,sourceArea,requestedWidth,requestedHeight);
	}catch(ewe.io.IOException e){
		throwBadFormat();
	}
}
/**
Use this to load a formatted image from an array of bytes, optionally resizing the image during decoding
<b>if possible</b>. If resizing during decoding is not possible, the returned image will be
the full sized image. Currently resizing during decoding is only possible on a native Ewe
VM decoding a JPEG formatted image.

* @param image the formatted image bytes.
* @param options creation options.
* @param requestedWidth the desired width of the decoded image - if this is zero, then the original width is used.
* @param requestedHeight the desired height of the decoded image - if this is zero, then the original height is used.
* @exception ImageDecodingException if there was an error decoding the image.
* @exception UnsupportedImageFormatException if the image format is not supported.
* @exception ewe.sys.SystemResourceException if the underlying system could not create the image because of system resource problems.
*/
//===================================================================
public Image(ByteArray image,int options,int requestedWidth,int requestedHeight)
throws ImageDecodingException, UnsupportedImageFormatException, ewe.sys.SystemResourceException
//===================================================================
{
	this();
	try{
		decodeFrom(image,options,requestedWidth,requestedHeight);
	}catch(ewe.io.IOException e){
		throwBadFormat();
	}
}
/**
* Use this to load a formatted image from an array of bytes.
* @param image the formatted image bytes.
* @param options creation options.
* @exception ImageDecodingException if there was an error decoding the image.
* @exception UnsupportedImageFormatException if the image format is not supported.
* @exception ewe.sys.SystemResourceException if the underlying system could not create the image because of system resource problems.
*/
//===================================================================
public Image(ByteArray image,int options)
throws ImageDecodingException, UnsupportedImageFormatException, ewe.sys.SystemResourceException
//===================================================================
{
	this(image,options,0,0);
}
/**
Use this to load a formatted image from an ewe.io.Stream, optionally resizing a portion of the image during decoding
<b>if possible</b>. If resizing during decoding is not possible, the returned image will be
the full sized image. Currently resizing during decoding is only possible on a native Ewe
VM decoding a JPEG formatted image.
* @param stream the formatted image bytes in a Stream.
* @param options creation options.
* @param sourceArea an optional area within the source image to decode - if this is null, then the entire image is used.
* @param requestedWidth the desired width of the decoded image - if this is zero, then the original width is used.
* @param requestedHeight the desired height of the decoded image - if this is zero, then the original height is used.
* @exception ewe.io.IOException if there was an error reading from the stream.
* @exception ImageDecodingException if there was an error decoding the image.
* @exception UnsupportedImageFormatException if the image format is not supported.
* @exception ewe.sys.SystemResourceException if the underlying system could not create the image because of system resource problems.
*/
//===================================================================
public Image(ewe.io.Stream stream,int options,Rect sourceArea,int requestedWidth,int requestedHeight)
throws ewe.io.IOException, ImageDecodingException, UnsupportedImageFormatException, ewe.sys.SystemResourceException
//===================================================================
{
	this();
	decodeFrom(stream,options,sourceArea,requestedWidth,requestedHeight);
}
/**
Use this to load a formatted image from an ewe.io.Stream, optionally resizing the image during decoding
<b>if possible</b>. If resizing during decoding is not possible, the returned image will be
the full sized image. Currently resizing during decoding is only possible on a native Ewe
VM decoding a JPEG formatted image.
* @param stream the formatted image bytes in a Stream.
* @param options creation options.
* @param requestedWidth the desired width of the decoded image - if this is zero, then the original width is used.
* @param requestedHeight the desired height of the decoded image - if this is zero, then the original height is used.
* @exception ewe.io.IOException if there was an error reading from the stream.
* @exception ImageDecodingException if there was an error decoding the image.
* @exception UnsupportedImageFormatException if the image format is not supported.
* @exception ewe.sys.SystemResourceException if the underlying system could not create the image because of system resource problems.
*/
//===================================================================
public Image(ewe.io.Stream stream,int options,int requestedWidth,int requestedHeight)
throws ewe.io.IOException, ImageDecodingException, UnsupportedImageFormatException, ewe.sys.SystemResourceException
//===================================================================
{
	this();
	decodeFrom(stream,options,requestedWidth,requestedHeight);
}
/**
Use this to load a formatted image from an ewe.io.Stream.
* @param stream the formatted image bytes in a Stream.
* @param options creation options.
* @exception ewe.io.IOException if there was an error reading from the stream.
* @exception ImageDecodingException if there was an error decoding the image.
* @exception UnsupportedImageFormatException if the image format is not supported.
* @exception ewe.sys.SystemResourceException if the underlying system could not create the image because of system resource problems.
*/
//===================================================================
public Image(ewe.io.Stream stream,int options)
throws ewe.io.IOException, ImageDecodingException, UnsupportedImageFormatException, ewe.sys.SystemResourceException
//===================================================================
{
	this(stream,options,0,0);
}

/**
 * Use this to create an RGB_IMAGE or an ARGB_IMAGE from the image pixels.
* @param width The width of the image - this must be greater than 0.
* @param height The height of the image - this must be greater than 0.
* @param options The exact image type, either RGB_IMAGE or ARGB_IMAGE.
 * @param pixels an int[] containing the pixel data.
 * @param offset the start point of the pixel data in the array.
 * @param scanLineLength the length of each scan line in the pixel array.
 */
//===================================================================
public Image(int width, int height, int options, int[] pixels, int offset, int scanLineLength)
//===================================================================
{
	this(width,height,options);
	if (pixels != null) setImageScanLines(0,height,pixels,offset,scanLineLength);
}
/**
* Use this to create a mono, grayscale or indexed image from a the image pixel bits and the
* color table (for indexed images).
* @param width The width of the image - this must be greater than 0.
* @param height The height of the image - this must be greater than 0.
* @param options The exact image type, either MONO_IMAGE, or GRAY_SCALE_xxx_IMAGE or INDEXED_xxx_IMAGE.
* @param bits The bits for the images.
* @param offset The start of the bits for the images within the bits array.
* @param scanLineLength The number of bytes per scan line within the bits array.
* @param colorTable For INDEXED_xxx_IMAGE types, this is the color table as ARGB
color values - but where the A value is not used. This array must be the correct length for
the image type.
* @exception IllegalArgumentException if the image dimensions are not valid.
* @exception ewe.sys.SystemResourceException if the underlying system could not create the image because of system resource problems.
* @exception ImageTypeNotSupportedException if the specified type is not supported by the system.
*/
//===================================================================
public Image(int width, int height, int options, byte[] bits, int offset, int scanLineLength, int[] colorTable)
throws IllegalArgumentException, SystemResourceException, ImageTypeNotSupportedException
//===================================================================
{
	this(width,height,options);
	if (
	((options & MONO_IMAGE) == 0) &&
	((options & A_GRAYSCALE_TYPE) == 0) &&
	((options & AN_INDEXED_TYPE) == 0)) throw new IllegalArgumentException("Must specify a Mono, Grayscale or Indexed image");
	create(width,height,options,bits,offset,scanLineLength,colorTable);
	if ((options & ALPHA_CHANNEL) != 0) enableAlpha();
}
/*
//-------------------------------------------------------------------
private static int getImageType(int myOptions, boolean usesAlpha)
//-------------------------------------------------------------------
{
	if (usesAlpha) return TYPE_ARGB;
	else if ((myOptions & MONO_IMAGE) != 0) return TYPE_MONO;
	else if ((myOptions & GRAY_SCALE_256_IMAGE) != 0) return TYPE_GRAY_SCALE_256;
	else if ((myOptions & GRAY_SCALE_16_IMAGE) != 0) return TYPE_GRAY_SCALE_16;
	else if ((myOptions & GRAY_SCALE_4_IMAGE) != 0) return TYPE_GRAY_SCALE_4;
	else if ((myOptions & GRAY_SCALE_2_IMAGE) != 0) return TYPE_GRAY_SCALE_2;
	else if ((myOptions & INDEXED_256_IMAGE) != 0) return TYPE_INDEXED_256;
	else if ((myOptions & INDEXED_16_IMAGE) != 0) return TYPE_INDEXED_16;
	else if ((myOptions & INDEXED_4_IMAGE) != 0) return TYPE_INDEXED_4;
	else if ((myOptions & INDEXED_2_IMAGE) != 0) return TYPE_INDEXED_2;
	else return TYPE_RGB;
}
*/
//===================================================================
public int getImageType()
//===================================================================
{
	return usesAlpha() ? TYPE_ARGB : TYPE_RGB;
	//return getImageType(myOptions,usesAlpha());
}
//-------------------------------------------------------------------
private static int getBPP(int type)
//-------------------------------------------------------------------
{
	switch(type){
		case TYPE_GRAY_SCALE_256:
		case TYPE_INDEXED_256: return 8;
		case TYPE_GRAY_SCALE_16:
		case TYPE_INDEXED_16: return 4;
		case TYPE_GRAY_SCALE_4:
		case TYPE_INDEXED_4: return 2;
		case TYPE_GRAY_SCALE_2:
		case TYPE_INDEXED_2: return 1;
		default:
			return 32;
	}
}

private native int _indexCreate(byte[] bits, int offset, int scanLineLength,int bitsPerPixel,int[] colorTable);

//-------------------------------------------------------------------
private void create(int width, int height, int options, byte[] bits, int offset, int scanLineLength, int[] colorTable)
//-------------------------------------------------------------------
{
	//if (NoImageDataSupport)
	throw new RuntimeException("Cannot create an Image from bits under GTK");
	/*
	if (width < 1 || height < 1) throw new IllegalArgumentException();
	this.width = width;
	this.height = height;
	//
	if ((options & GRAY_SCALE_IMAGE) != 0)
		options = GRAY_SCALE_256_IMAGE;
	else if ((options & BLACK_AND_WHITE_IMAGE) != 0)
		options = true ? MONO_IMAGE : GRAY_SCALE_256_IMAGE;
	//
	int bpp = 0;
	if ((options & (GRAY_SCALE_256_IMAGE|INDEXED_256_IMAGE)) != 0) bpp = 8;
	else if ((options & (GRAY_SCALE_16_IMAGE|INDEXED_16_IMAGE)) != 0) bpp = 4;
	else if ((options & (GRAY_SCALE_4_IMAGE|INDEXED_4_IMAGE)) != 0) bpp = 2;
	else if ((options & (GRAY_SCALE_2_IMAGE|INDEXED_2_IMAGE)) != 0) bpp = 1;
	else throw new IllegalArgumentException();
	//int bytesPerLine = ((bpp*width)+7)/8;
	//
	if (bpp == 2){
		if ((options & EXACT_TYPE_IMAGE) != 0)
			throw new ImageTypeNotSupportedException(options);
		if ((options & GRAY_SCALE_4_IMAGE) != 0) {
			options = (options & ~GRAY_SCALE_4_IMAGE) | GRAY_SCALE_16_IMAGE;
			bpp = 4;
			//FIXME convert bits.
		}else if ((options & INDEXED_4_IMAGE) != 0){
			options = (options & ~INDEXED_4_IMAGE) | INDEXED_16_IMAGE;
			colorTable = null;
			bpp = 4;
		}
	}
	//
	myOptions = options;
	//
	if ((options & GRAY_SCALE_256_IMAGE) != 0){
		colorTable = ImageTool.getGrayScaleColorTable(TYPE_GRAY_SCALE_256);
	}else if ((options & GRAY_SCALE_16_IMAGE) != 0){
		colorTable = ImageTool.getGrayScaleColorTable(TYPE_GRAY_SCALE_16);
	}else if ((options & GRAY_SCALE_4_IMAGE) != 0){
		colorTable = ImageTool.getGrayScaleColorTable(TYPE_GRAY_SCALE_4);
	}else if ((options & GRAY_SCALE_2_IMAGE) != 0){
		colorTable = ImageTool.getGrayScaleColorTable(TYPE_GRAY_SCALE_2);
	}else
		if (colorTable == null){
			if ((options & INDEXED_256_IMAGE) != 0)
				colorTable = ImageTool.makeDefaultColorTable(TYPE_INDEXED_256);
			else if ((options & INDEXED_16_IMAGE) != 0)
				colorTable = ImageTool.makeDefaultColorTable(TYPE_INDEXED_16);
			else if ((options & INDEXED_4_IMAGE) != 0)
				colorTable = ImageTool.makeDefaultColorTable(TYPE_INDEXED_4);
			else if ((options & INDEXED_2_IMAGE) != 0)
				colorTable = ImageTool.makeDefaultColorTable(TYPE_INDEXED_2);
			if (colorTable == null) throw new NullPointerException("A ColorTable is required");
		}
	if (colorTable.length < (1<<bpp)) throw new ArrayIndexOutOfBoundsException();
	colorMap = (int[])colorTable.clone();
	int ret = _indexCreate(bits,offset,scanLineLength,bpp,colorTable);
	if (ret == -1) throw new ImageTypeNotSupportedException(options);
	if (ret == -2) throw new SystemResourceException();
	*/
	/*
	int cl = colorTable.length;
	byte[]
		red = new byte[cl],
		green = new byte[cl],
		blue = new byte[cl];
	for (int i = 0; i<cl; i++){
		int ct = colorTable[i];
		red[i] = (byte)((ct >> 16) & 0xff);
		green[i] = (byte)((ct >> 8) & 0xff);
		blue[i] = (byte)(ct & 0xff);
	}
	ColorModel cm = new IndexColorModel(bpp,red.length,red,green,blue);
	DataBufferByte dbb = new DataBufferByte(bytesPerLine*height);
	WritableRaster wr = Raster.createPackedRaster(dbb,width,height,bpp,new java.awt.Point(0,0));
	//
	if (bits != null){
		byte[] ib = dbb.getData();
		int s = offset;
		for (int h = 0; h<height; h++){
			System.arraycopy(bits,s,ib,h*bytesPerLine,bytesPerLine);
			s += scanLineLength;
		}
	}
	//
	return new BufferedImage(cm,wr,true,null);
	*/
}

//===================================================================
public byte[] getImageBits()
//===================================================================
{
	return null;
}
//===================================================================
public int[] getImageColorTable()
//===================================================================
{
	return null;
	//return colorMap;
}

}

