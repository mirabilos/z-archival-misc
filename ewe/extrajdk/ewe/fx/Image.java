/* $MirOS: contrib/hosted/ewe/extrajdk/ewe/fx/Image.java,v 1.2 2008/05/02 20:52:03 tg Exp $ */

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
 *
 *  This library and virtual machine is distributed in the hope that it will     *
 *  be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of    *
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.                         *
 *                                                                               *
 *  You should have received a copy of the License along with this software;     *
 *  if not, please download it at: www.ewesoft.com/LGPL.txt                      *
 *                                                                               *
 *********************************************************************************/
package ewe.fx;

import ewe.applet.Applet;
import ewe.util.IntArray;
import ewe.util.ByteArray;
import ewe.sys.SystemResourceException;
import java.awt.image.*;
import java.awt.*;
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
public Color transparent; //This must be third and last.
public Color background;
public boolean hasAlpha = false;
boolean wasLoaded = false;
byte [] alphaChannel = null;
int myOptions;

java.awt.Image bufferedImage;
Object maskedWith;

java.awt.Image _awtImage;

public boolean usesAlpha() {return hasAlpha;}

public String name;
public boolean freed = false;

/**
* @deprecated - use RGB_IMAGE instead.
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
This is an option to be  used in Image initializers - it is the same as TRUE_COLOR
It is used to specify a 24-bit image RGB image.
**/
public static final int RGB_IMAGE = 0x1;
/**
This is an option to be  used in Image initializers.
It is used to specify a 32-bit image RGB image with an Alpha (transparency)
channel. It is the same as RGB_IMAGE|ALPHA_CHANNEL
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
default display. This may be actually represented gray scale image.
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
Use this to specify an indexed image that is optimized for the display.
**/
public static final int INDEXED_2_IMAGE = 0x8000;
/**
Use this to specify an INDEXED_IMAGE image at one byte per pixel.
**/
public static final int INDEXED_256_IMAGE = 0x4000;
/**
Use this to specify an INDEXED_IMAGE image at 4 bits per pixel.
**/
public static final int INDEXED_16_IMAGE = 0x2000;
/**
Use this to specify an INDEXED_IMAGE image at one byte per pixel.
**/
public static final int INDEXED_4_IMAGE = 0x1000;
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

//##################################################################
{
	numImages++;
	if (debugImageSize > 0)
		if (numImages % debugImageSize == 0)
			ewe.sys.Vm.debug("Images: "+numImages);
}
//##################################################################



//===================================================================
public boolean enableAlpha()
//===================================================================
{
	//if (wasLoaded) return false;
	hasAlpha = true;
	if (alphaChannel == null)
		Mask.makeOpaque(alphaChannel = new byte[width*height]);
	return true;
}

public static boolean is12 = true;
static {
	try{
 		defaultImageType = BufferedImage.TYPE_INT_ARGB;
		newBufferedImage(1,1);
	}catch(Error e){
		is12 = false;
	}
}

int [] rgb = null;

public static boolean useTrueMono = false;
	//for (int i = 0; i<bits.length; i++) bits[i] = (byte)(i & 0xff);
	/*
public static BufferedImage createMonoImage(int width, int height,byte[] bits,int options)
{
	if (((options & GRAY_SCALE_IMAGE) == 0) && (useTrueMono || ((options & MONO_IMAGE) != 0) || (bits != null))){
		int size = (((width+7)/8)*height);
		if (bits == null) {
			bits = new byte[size];
			java.util.Arrays.fill(bits,(byte)255);
		}
		WritableRaster wr = Raster.createPackedRaster(new DataBufferByte(bits,size),width,height,1,new java.awt.Point(0,0));
		byte [] rgb = new byte[2]; rgb[1] = (byte)0xff; rgb[0] = (byte)0x00;
		ColorModel cm = new IndexColorModel(1,2,rgb,rgb,rgb);
		return new BufferedImage(cm,wr,true,null);
	}else{
		byte[] gray = new byte[256];
		for (int i = 0; i<gray.length; i++) gray[i] = (byte)i;
		ColorModel cm = new IndexColorModel(8,256,gray,gray,gray);
		if (bits == null) bits = new byte[width*height];
		WritableRaster wr = Raster.createPackedRaster(new DataBufferByte(bits,width*height),width,height,8,new java.awt.Point(0,0));
		return new BufferedImage(cm,wr,true,null);
	}
}
public static BufferedImage createMonoImage(int width, int height,int options)
{
	return createMonoImage(width,height,null,options);
}
//-------------------------------------------------------------------
protected void create(int width,int height,int options) throws ewe.sys.SystemResourceException
//-------------------------------------------------------------------
{
	create(width,height,options,null);
}
*/
//-------------------------------------------------------------------
private void fromNative(Object nativeImage,int options) throws IllegalArgumentException, ewe.sys.SystemResourceException
//-------------------------------------------------------------------
{
	if (nativeImage instanceof java.awt.Image){
		java.awt.Image im = (java.awt.Image)nativeImage;
		try{
			_awtImage = toBufferedImage(im);
			width = _awtImage.getWidth(null);
			height = _awtImage.getHeight(null);
			hasAlpha = true;
			rgb = null;
			return;
		}catch(Error e){
			java.awt.Toolkit awtToolkit = java.awt.Toolkit.getDefaultToolkit();
			_awtImage = im;//awtToolkit.createImage(im.getSource());//imageSource);
			width = im.getWidth(null);
			height = im.getHeight(null);
			wasLoaded = false;
			updateRGB();
			wasLoaded = true;
		}
	}else
		throw new IllegalArgumentException("A java.awt.Image is required.");
}

//private native void _nativeCreate();

public java.awt.Image getAWTImage()
	{
	return _awtImage;
	}

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

private int readBytes(java.io.DataInputStream data, byte b[])
	{
	int nread = 0;
	int len = b.length;
	while (true)
		{
		int n = 0;
		try { n = data.read(b, nread, len - nread); }
		catch (Exception e) {}
		if (n <= 0)
			return -1;
		nread += n;
		if (nread == len)
			return len;
		}
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
private void readBMP(java.io.DataInputStream data, String name)
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
	byte header[] = new byte[54];
	if (readBytes(data, header) != 54)
		{
		System.out.println("ERROR: can't read image header for " + name);
		return;
		}
	if (header[0] != 'B' || header[1] != 'M')
		{
		System.out.println("ERROR: " + name + " is not a BMP image");
		return;
		}
	int bitmapOffset = inGetUInt32(header, 10);

	int infoSize = inGetUInt32(header, 14);
	if (infoSize != 40)
		{
		System.out.println("ERROR: " + name + " is old-style BMP");
		return;
		}
	int width = inGetUInt32(header, 18);
	int height = inGetUInt32(header, 22);
	if (width < 0 || height < 0 || width > 65535 || height > 65535)
		{
		System.out.println("ERROR: " + name + " has invalid width/height");
		return;
		}
	int bpp = inGetUInt16(header, 28);
	if (bpp != 1 && bpp != 4 && bpp != 8 && bpp != 24)
		{
		System.out.println("ERROR: " + name + " is not a 2, 16, 256 or 24-bit color image");
		return;
		}
	int compression = inGetUInt32(header, 30);
	if (compression != 0)
		{
		System.out.println("ERROR: " + name + " is a compressed image");
		return;
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
		if (readBytes(data, cmapData) != cmapSize)
			{
			System.out.println("ERROR: can't read colormap of " + name);
			return;
			}

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
	rgb = new int[width * height];
	byte pixels[] = new byte[scanlen];
	for (int y = height - 1; y >= 0; y--)
		{
		if (readBytes(data, pixels) != scanlen)
			{
			System.out.println("ERROR: scanline " + y + " bad in image " + name);
			return;
			}
		if (width == 0)
			continue;
		pixelsToRGB(bpp, width, pixels, 0, rgb, y * width, cmap);
		}
	if (debugImages)
		System.out.println(width+", "+height+", "+scanlen);

	// create the image from the RGB buffer
	this.width = width;
	this.height = height;
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
	}

//-------------------------------------------------------------------
private void throwBadFormat() throws IllegalArgumentException
//-------------------------------------------------------------------
{
	throw new ImageDecodingException(name);
}
//-------------------------------------------------------------------
protected void setSize(ImageCodec ic,Rect sourceArea,int requestedWidth,int requestedHeight,int options)
//-------------------------------------------------------------------
{
	width = ic.width; height = ic.height;
	if (sourceArea != null){
		width = height = 1; //This is to supress the bad format error.
 		if (true) throw new IllegalArgumentException("Cannot decode a portion of this image.");
		if (sourceArea.x < 0 || sourceArea.x + sourceArea.width > ic.width || sourceArea.y < 0 || sourceArea.y + sourceArea.height > ic.height)
			throw new IllegalArgumentException("The source area is not completely within the image.");
		width = sourceArea.width;
		height = sourceArea.height;
	}
	//
	// Cannot scale while decoding under Java yet.
	//
	if (requestedWidth > 0 && requestedHeight > 0) throw new IllegalArgumentException("Cannot decode scaled version of this image.");
	if (true || requestedWidth <= 0 || requestedHeight <= 0) return;
	//
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
	if (ic.isGIFFile || ic.isJPEGFile){
		if (image == null) {
			ewe.io.RewindableStream.rewind(ras);
			image = getBytes(ras);
		}
		java.awt.Image im = Toolkit.getDefaultToolkit().createImage(image.data,0,image.length);
//		if (!new ImagePreparer().prepare(im,true))
//			throw new IllegalArgumentException();
		new ImagePreparer().prepare(im,true);
		ic.width = im.getWidth(null);
		ic.height = im.getHeight(null);
		im.flush();
	}
	destination.width = ic.width;
	destination.height = ic.height;
	destination.canScale = false;
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
		//
		if ((image == null) && (ic.isJPEGFile || ic.isGIFFile || ic.isBMPFile))
			image = getBytes(ras);
		//
		if (ic.isJPEGFile || ic.isGIFFile){
			java.awt.Image im = Toolkit.getDefaultToolkit().createImage(image.data,0,image.length);
			if (new ImagePreparer().prepare(im)){
				fromNative(im,options);
			}
			return;
		}else if (ic.isBMPFile) {
			try{
				java.io.DataInputStream di = new java.io.DataInputStream(new java.io.ByteArrayInputStream(image.data,0,image.length));
				readBMP(di,"-bitmap-");
				di.close();
			}catch(Exception e){
				e.printStackTrace();
			}
		}else {
			if ((options & (TRUE_COLOR|INDEXED|MONO_IMAGE|BLACK_AND_WHITE_IMAGE)) == 0) options |= TRUE_COLOR;
			create(ic.width,ic.height,options);
			if (!ic.toImage(this)) width = height = 0;
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
		ras.seek(0);
		if (ic.isJPEGFile || ic.isGIFFile){
			java.awt.Image im = Toolkit.getDefaultToolkit().createImage(image.data,0,image.length);
			if (new ImagePreparer().prepare(im)){
				fromNative(im,options);
			}
			return;
		}
		if (ic.isBMPFile) {
			try{
				java.io.DataInputStream di = new java.io.DataInputStream(new java.io.ByteArrayInputStream(image.data,0,image.length));
				readBMP(di,"-bitmap-");
				di.close();
			}catch(Exception e){
				e.printStackTrace();
			}
		}else {
			if ((options & (TRUE_COLOR|INDEXED)) == 0)
				options |= TRUE_COLOR;
				create(ic.width,ic.height,options);
			//_nativeCreate(options);
			if (!ic.toImage(this)) {
				width = height = 0;
			}
		}
	}finally{
		if (width <= 0 || height <= 0) throwBadFormat();
	}
}
*/
private void loadImage(String path)
	{
	java.io.InputStream stream = Applet.openInputStream(path);
	if (stream == null)
		{
		System.out.println("ERROR: can't open image file " + path);
		return;
		}
	try
		{
		java.io.DataInputStream data = new java.io.DataInputStream(stream);
		//System.out.println(path);
		readBMP(data, path);
		data.close();
		stream.close();
		}
	catch (Exception e)
		{
		System.out.println("ERROR: when loading Image. Trace appears below.");
		e.printStackTrace();
		}
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

//public native void setPixels(int bitsPerPixel, int colorMap[], int bytesPerRow,
//	int numRows, int y, byte pixels[]);
public void setPixels(int bitsPerPixel, int colorMap[], int bytesPerRow,
	int numRows, int y, Object rawPixels)
	{
	if (bitsPerPixel != 1 && bitsPerPixel != 4 && bitsPerPixel != 8)
		return;
	// convert pixels to RGB values
	int rgb[] = new int[width * numRows];
	for (int r = 0; r < numRows; r++)
		pixelsToRGB(bitsPerPixel, width, (byte [])rawPixels, r * bytesPerRow,
			rgb, r * width, colorMap);
	java.awt.image.MemoryImageSource imageSource;
	imageSource = new java.awt.image.MemoryImageSource(width, numRows, rgb, 0, width);
	java.awt.Toolkit awtToolkit = java.awt.Toolkit.getDefaultToolkit();
	java.awt.Image rowImage = awtToolkit.createImage(imageSource);
	java.awt.Graphics g = _awtImage.getGraphics();
	g.drawImage(rowImage, 0, y, null);
	g.dispose();
	rowImage.flush();
	}



/**
 * Sets the image width and height to 0 and frees any systems resources
 * associated with the image.
 */
//public native void free();
public void free()
	{
	if (freed) return;
	width = 0;
	height = 0;
	if (_awtImage != null) _awtImage.flush();
	if (bufferedImage != null) bufferedImage.flush();
	freed = true;
	numImages--;
	}


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
	g.drawImage(this,x,y);
}
//===================================================================
public static int defaultImageType;
//===================================================================
/**
* Creates the buffered image with the default image type.
* Only call this after the original image is fully prepared.
**/
//===================================================================
public static BufferedImage toBufferedImage(java.awt.Image original) {return toBufferedImage(original,defaultImageType);}
//==================================================================
/**
* Only call this after the original image is fully prepared.
**/
//===================================================================
public static BufferedImage toBufferedImage(java.awt.Image original,int type)
//===================================================================
{
	return scaleInto(newBufferedImage(original.getWidth(null),original.getHeight(null),type),original);
}
/**
* This returns the destination image.
**/
//===================================================================
public static BufferedImage scaleInto(BufferedImage dest,java.awt.Image source)
//===================================================================
{
	Graphics2D gr = (Graphics2D)dest.createGraphics();
	gr.drawImage(source,0,0,dest.getWidth(),dest.getHeight(),0,0,source.getWidth(null),source.getHeight(null),null);
	gr.dispose();
	return dest;
}
//===================================================================
public static BufferedImage newBufferedImage(int w,int h) {return newBufferedImage(w,h,defaultImageType);}
//===================================================================
public static BufferedImage newBufferedImage(int w,int h,int type)
//===================================================================
{
	return new BufferedImage(w,h,type);
}

//-------------------------------------------------------------------
void doCheckMask(Image mask,Color transparent)
//-------------------------------------------------------------------
{
	if (_awtImage == null) return;
	if (mask == null && transparent == null){
		bufferedImage = _awtImage;
		return;
	}
	Object msk = mask;
	if (msk == null) msk = transparent;
	if (bufferedImage == null || maskedWith != msk){
		if (bufferedImage != null) bufferedImage.flush();
		maskedWith = msk;
		updateRGB();
		if (is12)
		try{
			BufferedImage bi = toBufferedImage(_awtImage);
			bufferedImage = bi;
			if (msk instanceof Color){
				int sc = (transparent.getRed() << 16) | (transparent.getGreen() << 8) | transparent.getBlue();
				int w = bi.getWidth();
				int h = bi.getHeight();
				for (int x = 0; x<w; x++)
					for (int y = 0; y<h; y++)
						if ((bi.getRGB(x,y) & 0xffffff) == sc)
							bi.setRGB(x,y,0);
			}else{
				if (mask == null) return;
				if (mask._awtImage == null) return;
				BufferedImage theMask = toBufferedImage(mask._awtImage);
				int w = bi.getWidth();
				int h = bi.getHeight();
				if (w > theMask.getWidth()) w = theMask.getWidth();
				if (h > theMask.getHeight()) w = theMask.getHeight();
				for (int x = 0; x<w; x++)
					for (int y = 0; y<h; y++){
						if ((bi.getRGB(x,y) & 0xffffff) == 0xffffff)
							if ((theMask.getRGB(x,y) & 0xffffff) == 0){
								bi.setRGB(x,y,0);
								//System.out.println(x+","+y);
							}
					}
				theMask.flush();
			}
		}catch(Error e){}
		else{ //1.1 version.
			int newrgb[] = new int[width*height];
			if (msk instanceof Color){
				int sc = (transparent.getRed() << 16) | (transparent.getGreen() << 8) | transparent.getBlue();
				sc = Graphics.mapColor(sc) & 0xffffff;
				for (int x = 0; x<width; x++)
					for (int y = 0; y<height; y++){
						int idx = y*width+x;
						if ((rgb[idx] & 0xffffff) == sc)
							newrgb[idx] = 0;
						else
							newrgb[idx] = rgb[idx];
					}
			}else{
				if (mask != null){
					mask.updateRGB();
					if (mask.rgb != null){
						int w = width;
						int h = height;
						if (w > mask.getWidth()) w = mask.getWidth();
						if (h > mask.getHeight()) h = mask.getHeight();
						int white = Graphics.mapColor(0xffffff) & 0xffffff;
						for (int x = 0; x<w; x++)
							for (int y = 0; y<h; y++){
								int idx = y*width+x;
								newrgb[idx] = rgb[idx];
								if ((rgb[idx] & 0xffffff) == white)
									if ((mask.rgb[idx] & 0xffffff) == 0)
										newrgb[idx] = 0;
							}
					}
				}
			}
			java.awt.image.MemoryImageSource imageSource = new java.awt.image.MemoryImageSource(width, height, newrgb, 0, width);
			java.awt.Toolkit awtToolkit = java.awt.Toolkit.getDefaultToolkit();
			bufferedImage = awtToolkit.createImage(imageSource);
		}
	}
}
	//##################################################################
	class GetPixels implements Runnable, ImageConsumer{
	//##################################################################

	// There is a bug in the Jeode Personal Java VM.
	// The producer does NOT call complete() - so we have to keep track of the lines
	// that are sent and assume completion.
	int linesDone = 0;

	java.awt.image.ImageProducer p;
	GetPixels(java.awt.image.ImageProducer prod)
	{
		p = prod;
	}
	int [] pixmap;
	boolean complete = false;
	int width, height;
	public synchronized void run(){
		p.startProduction(this);
		//System.out.println("production started");
		if (!complete)
			try{wait();}catch(Exception e){}
		//System.out.println("completed");
	}
	public synchronized void setHints(int hints){}
	public synchronized void setPixels(int x,int y,int w,int h,java.awt.image.ColorModel cm,byte [] pixels,int off,int scan)
	{
		//System.out.println("setPixelsBytes() - "+x+", "+y+", "+w+", "+h);
		for (int yy = 0; yy<h; yy++){
			int inpix = x+(y+yy)*width;
			int insrc = off+yy*scan;
			for (int xx = 0; xx<w; xx++)
				pixmap[inpix++] = cm.getRGB(pixels[insrc++] & 0xff);
		}
		if (w == width) linesDone += h;
		if (linesDone == height) imageComplete(0);
	}
	public synchronized void setPixels(int x,int y,int w,int h,java.awt.image.ColorModel cm,int [] pixels,int off,int scan)
	{
		//System.out.println("setPixelsInts() - "+x+", "+y+", "+w+", "+h);
		for (int yy = 0; yy<h; yy++){
			int inpix = x+(y+yy)*width;
			int insrc = off+yy*scan;
			for (int xx = 0; xx<w; xx++)
				pixmap[inpix++] = cm.getRGB(pixels[insrc++]);
		}
		if (w == width) linesDone += h;
		if (linesDone == height) imageComplete(0);
	}
	public synchronized void setDimensions(int w,int h)
	{
		//System.out.println("setDimensions() - "+w+", "+h);
		width = w;
		height = h;
		pixmap = new int[w*h];
	}
	public synchronized void setProperties(java.util.Hashtable t){}
	public synchronized void imageComplete(int status)
	{
		//System.out.println("complete() - "+status);
		p.removeConsumer(this);
		complete = true;
		notify();
	}
	public synchronized void setColorModel(java.awt.image.ColorModel cm){}
	//##################################################################
	}
	//##################################################################

//-------------------------------------------------------------------
void updateRGB() {updateRGB(0,0,width,height);}
//-------------------------------------------------------------------

//-------------------------------------------------------------------
void updateRGB(int x,int y,int w,int h)
//-------------------------------------------------------------------
{
	if (!wasLoaded || rgb == null){
		java.awt.image.ImageProducer p = _awtImage.getSource();
		if (p != null) {
			GetPixels gp = new GetPixels(p);
			Thread t = new Thread(gp);
			t.start();
			try{
				t.join();
				rgb = gp.pixmap;
				if (alphaChannel != null)
					for (int i = 0; i<alphaChannel.length; i++){
						rgb[i] = (rgb[i] & 0xffffff) | (alphaChannel[i] << 24);
					}
			}catch(InterruptedException e){
			}
		}
	}
}

//===================================================================
public int [] getPixels(int [] dest,int offset,int x,int y,int w,int h,int options)
//===================================================================
{
	if (_awtImage == null) return null;
	if (dest != null)
		if (w*h+offset > dest.length) dest = null;
	if (dest == null) dest = new int[w*h+offset];
	if (is12)
		try{
			BufferedImage bi = (BufferedImage)_awtImage;
			bi.getRGB(x,y,w,h,dest,offset,w);
		}catch(Error e){}
	else{
		updateRGB(x,y,w,h);
		if (rgb != null){
			int d = offset;
			int maxrow = height;
			if (y+h < maxrow) maxrow = y+h;
			int maxcol = width;
			if (x+w < maxcol) maxcol = x+w;
			int r = y < 0 ? 0 : y;
			for (; r<maxrow; r++){
				int c = x < 0 ? 0 : x;
				int src = r*width + c;
				for (; c<maxcol; c++)
						dest[d++] = rgb[src++];
			}
		}
	}
	return dest;
}
//===================================================================
public void setPixels(int [] source,int offset,int x,int y,int w,int h,int options)
//===================================================================
{
	if (_awtImage == null || source == null) return;
	if (is12)
		try{
			((BufferedImage)_awtImage).setRGB(x,y,w,h,source,offset,w);
			return;
		}catch(Error e){}

	{
	/*
		java.awt.image.MemoryImageSource imageSource = new java.awt.image.MemoryImageSource(w, h, source, offset, w);
		java.awt.Toolkit awtToolkit = java.awt.Toolkit.getDefaultToolkit();
		java.awt.Image im = awtToolkit.createImage(imageSource);
		java.awt.Graphics gr = _awtImage.getGraphics();
		gr.drawImage(im,x,y,null);
		gr.dispose();
		im.flush();
		if (alphaChannel != null){
			int i = 0;
			for (int r = 0; r<h; r++){
				int off = (r+y)*width+x;
				for (int c = 0; c<w; c++)
					alphaChannel[off++] = (byte)(source[i++] >> 24);
			}
		}
		if (bufferedImage != null){
			if (bufferedImage != _awtImage) bufferedImage.flush();
			bufferedImage = null;
		}
		*/
		if (!wasLoaded){
			java.awt.image.MemoryImageSource imageSource = new java.awt.image.MemoryImageSource(w, h, source, offset, w);
			java.awt.Toolkit awtToolkit = java.awt.Toolkit.getDefaultToolkit();
			java.awt.Image im = awtToolkit.createImage(imageSource);
			java.awt.Graphics gr = _awtImage.getGraphics();
			gr.drawImage(im,x,y,null);
			gr.dispose();
			im.flush();
			if (alphaChannel != null){
				int i = 0;
				for (int r = 0; r<h; r++){
					int off = (r+y)*width+x;
					for (int c = 0; c<w; c++)
						alphaChannel[off++] = (byte)(source[i++] >> 24);
				}
			}
		}else{
			try{
			updateRGB();
			int i = 0;
			for (int r = 0; r<h; r++){
				int off = (r+y)*width+x;
				for (int c = 0; c<w; c++){
					rgb[off] = source[i];
					if (alphaChannel != null)
						alphaChannel[off] = (byte)((source[i] >> 24) & 0xff);
					off++;
					i++;
				}
			}
			java.awt.image.MemoryImageSource imageSource = new java.awt.image.MemoryImageSource(width, height, rgb, 0, width);
			java.awt.Toolkit awtToolkit = java.awt.Toolkit.getDefaultToolkit();
			java.awt.Image im = awtToolkit.createImage(imageSource);
			if (_awtImage != null) _awtImage.flush();
			_awtImage = im;
			}catch(Throwable e){
				//e.printStackTrace();
			}
		}
		if (bufferedImage != null){
			if (bufferedImage != _awtImage) bufferedImage.flush();
			bufferedImage = null;
		}

	}
}
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
	if (what.rgb != null){
		int max = what.rgb.length;
		inverted.rgb = new int[max];
		for (int i = 0; i<max; i++)
			inverted.rgb[i] = what.rgb[i] ^ 0xffffffff;
	}
	return inverted;
}

/**
* This transforms the image into a mouse cursor suitable for use on the current
* platform. Will return null if the cursor could not be created.
**/
//===================================================================
Object toCursor(Image mask,Point hotSpot)
//===================================================================
{
	Object ret = null;
	if (is12)
		try{
			doCheckMask(mask,transparent);
			ret = Toolkit.getDefaultToolkit().createCustomCursor(bufferedImage,new java.awt.Point(hotSpot.x,hotSpot.y),"NewCursor");
		}catch(Throwable t){
		}
	free();
	mask.free();
	return ret;
}
//===================================================================
public Object toIcon(Image mask)
//===================================================================
{
	return toNativeImage(mask);
}
//===================================================================
public Object toNativeImage(Object colorOrMask)
//===================================================================
{
	Image i2 = new Image(this,0);
	Object ret = null;
	i2.doCheckMask(
	colorOrMask instanceof Image ? (Image)colorOrMask : null,
	colorOrMask instanceof Color ? (Color)colorOrMask : null);
	ret = i2.bufferedImage;
	i2.bufferedImage = null;
	i2.free();
	return ret;
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
private static void grayPixels(int [] buff,int start,int length,int transparent)
//-------------------------------------------------------------------
{
	for (int i = 0; i<length; i++){
		int val = buff[i+start];
		if ((val & 0xffffff) == transparent) continue;
		int r = (val >> 16) & 0xff, g = (val >> 8) & 0xff, b = val & 0xff;
		int tot = r+g+b;
		if (tot < 3*128) tot = 3*128;
		int newVal = tot/3;
		val = (val & 0xff000000) | (newVal << 16) | (newVal << 8) | newVal;
		buff[i+start] = val;
	}
}
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
	return _awtImage;
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
	doRotate(destination,degree);
	return destination;
}

private void doRotate(Image destination,int degree)
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
/*
public Image scale(Rect sourceArea,Rect destArea,Image destination)
{
	if (true) throw new UnsatisfiedLinkError();
	try{
		System.out.println("Scaling");
		if (destination == null) destination = new Image(destArea.width+destArea.x,destArea.height+destArea.y,myOptions);
		System.out.println("Created image.");
		Graphics2D gr = (Graphics2D)((BufferedImage)destination._awtImage).createGraphics();
		//java.awt.Graphics gr = destination._awtImage.getGraphics();
		System.out.println("Created graphics.");
		gr.drawImage(_awtImage,
			destArea.x,destArea.y,destArea.x+destArea.width,destArea.y+destArea.height,
			sourceArea.x,sourceArea.y,sourceArea.x+sourceArea.width,sourceArea.y+sourceArea.height,
			null);
		gr.dispose();
		System.out.println("Scaled!");
		return destination;
	}catch(Error e){
		throw new UnsatisfiedLinkError();
	}
}
*/

/*
//===================================================================
public Image scale(int newWidth, int newHeight)
//===================================================================
{
	return scale(newWidth,newHeight,myOptions);
}
//===================================================================
public Image scale(int newWidth, int newHeight,int destOptions)
//===================================================================
{
	return scaleStrip(newWidth,newHeight,destOptions,0,newHeight,null);
}

static byte[] masks ={(byte)0x80,(byte)0x40,(byte)0x20,(byte)0x10,(byte)0x08,(byte)0x04,(byte)0x02,(byte)0x01};

//===================================================================
public Image scaleStrip(int newWidth,int newHeight,int destOptions,int yOffset,int stripHeight,Image destination)
//===================================================================
{
	if (((myOptions & (MONO_IMAGE|BLACK_AND_WHITE_IMAGE|GRAY_SCALE_IMAGE)) != 0)){
		if (destination == null) destination = new Image(newWidth,stripHeight,destOptions);
		byte [] dest = ((DataBufferByte)((BufferedImage)destination._awtImage).getRaster().getDataBuffer()).getData();
		byte [] src =  ((DataBufferByte)((BufferedImage)_awtImage).getRaster().getDataBuffer()).getData();
		boolean srcGray = src.length >= width*height;
		boolean dstGray = dest.length >= newWidth*stripHeight;
		int sbpl = srcGray ? width : (width+7)/8;
		int dbpl = dstGray ? newWidth : (newWidth+7)/8;

		int dx = width/newWidth;
		int rx = width%newWidth;

		int dy = height/newHeight;
		int ry = height%newHeight;
		int cy = 0; //yOffset
		int sy = (cy*height)/newHeight;
		int yq = cy%newHeight;
		//
		//System.out.println(newWidth+", "+newHeight+", "+yOffset+", "+stripHeight);
		//
		for (int y = cy; y<yOffset+stripHeight; y++){
			int di = (y-yOffset)*dbpl;
			byte dmask = (byte)0x80;
			int yt = dy;
			yq += ry;
			if (yq >= newHeight) {
				yt++; //How many source lines to use.
				yq -= newHeight;
			}
			int sx = 0;//sy*width;
			int xq = 0;
			if (y >= yOffset){
				//System.out.println("Y: "+y)
				for (int x = 0; x<newWidth; x++){
					int value = 0;
					int xt = dx;
					xq += rx;
					if (xq >= newWidth){
						xt++;
						xq -= newWidth;
					}
					int yyt = yt == 0 ? 1 : yt;
					int xxt = xt == 0 ? 1 : xt;

					if (srcGray){
						for (int yy = 0; yy<yyt; yy++){
							int ss = sx+(sy+yy)*sbpl;
							for (int xx = 0; xx<xxt; xx++)
								value += (int)src[ss++] & 0xff;
						}
					}else{
						int stx = sx/8;
						for (int yy = 0; yy<yyt; yy++){
							byte smask = masks[sx%8];
							int ss = stx+(sy+yy)*sbpl;
							for (int xx = 0; xx<xxt; xx++){
								if ((src[ss] & smask) != 0) value += 0xff;
								smask = (byte)((smask >> 1) & 0x7f);
								if (smask == 0) {
									smask = (byte)0x80;
									ss++;
								}
							}
						}
					}
					value = value/(yyt*xxt);
					//
					// Value now has the average value of all the pixels.
					//
					if (!dstGray){
						if (value < 127) value = 0;
						else value = 0xff;
					}
					if (dstGray){
						//if (!srcGray) value = 255-value;
						dest[di] = (byte)value;
						di++;
					}else{
						//if (!srcGray) value = 255-value;
						if (value != 0) dest[di] |= dmask;
						else dest[di] &= ~dmask;
						dmask = (byte)((dmask >> 1) & 0x7f);
						if (dmask == 0) {
							dmask = (byte)0x80;
							di++;
						}
					}
					sx += xt;
				}
			}
			sy += yt;
		}
		return destination;
	}else{
		return new PixelBuffer(this).scale(newWidth,newHeight).toImage();
	}
}
*/
/**
* Use this to create a MONO_IMAGE or a GRAY_SCALE_IMAGE from a set of bits.
*/
/*
//===================================================================
public Image(int width, int height, byte[] bits, boolean invert)
//===================================================================
{
	int opts = MONO_IMAGE;
	if (bits != null)
		if (width*height <= bits.length)
			opts = GRAY_SCALE_IMAGE;
	create(width,height,opts,bits);
	//byte [] dest =  ((DataBufferByte)((BufferedImage)_awtImage).getRaster().getDataBuffer()).getData();
	//System.arraycopy(bits,0,dest,0,((width+7)/8)*height);
}
*/
/*
//===================================================================
public Image(int width, int height, byte[] bits)
//===================================================================
{
	this(width,height,bits,false);
}
*/
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
public Image(int width, int height,int options)  throws IllegalArgumentException, ewe.sys.SystemResourceException
//===================================================================
{
	create(width,height,options);
	if ((options & FOR_DISPLAY) != 0) wasLoaded = true;
	if ((options & ALPHA_CHANNEL) != 0) enableAlpha();
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
public Image(int width, int height) throws IllegalArgumentException, ewe.sys.SystemResourceException
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
public Image(Image other,int options)  throws ewe.sys.SystemResourceException
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
* @exception ewe.sys.SystemResourceException if the underlying system could not create
* the image because of system resource problems.
*/
//===================================================================
public Image(Object nativeImage,int options)  throws IllegalArgumentException, ewe.sys.SystemResourceException
//===================================================================
{
	try{
		//create(0,0,options);
		if ((options & FOR_DISPLAY) != 0) wasLoaded = true;
		fromNative(nativeImage,options);
	}catch(IllegalArgumentException ia){
		throw ia;
	}catch(Exception e){
		throw new IllegalArgumentException(e.getMessage());
	}
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
	name = path;
	width = height = 0;
	byte [] all = ewe.sys.Vm.readResource(null,path);
	if (all == null) return;
	ByteArray ba = new ByteArray(all);
	try{
		decodeFrom(ba,options,0,0);
	}catch(ewe.io.IOException e){}
	if (width == 0 || height == 0){
		try{
 			_awtImage = newBufferedImage(1,1);
		}catch(Error e){
			_awtImage = Applet.getImageCreator().createImage(1,1);
		}
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
	try{
		decodeFrom(image,options,sourceArea,requestedWidth,requestedHeight);
	}catch(ewe.io.IOException e){
		throwBadFormat();
	}
}
/**
* Use this to load a formatted image from an array of bytes.
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
	try{
		try{
			decodeFrom(image,options,requestedWidth,requestedHeight);
		}catch(ewe.io.IOException e){
			throwBadFormat();
		}
	}finally{
		if (width == 0 || height == 0){
			try{
	 			_awtImage = newBufferedImage(1,1);
			}catch(Error e){
				_awtImage = Applet.getImageCreator().createImage(1,1);
			}
		}
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
	myOptions = options;
	this.width = width;
	this.height = height;
	_awtImage = createBufferedImage(width,height,options,bits,offset,scanLineLength,colorTable);
	if ((options & ALPHA_CHANNEL) != 0) enableAlpha();
}

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

//-------------------------------------------------------------------
protected void create(int width,int height,int options)
throws IllegalArgumentException, ewe.sys.SystemResourceException
//-------------------------------------------------------------------
{
	if (width < 1 || height < 1) throw new IllegalArgumentException();
	myOptions = options;
	this.width = width;
	this.height = height;
	if ((options & INDEXED_IMAGE) != 0){
		_awtImage = new BufferedImage(width,height,BufferedImage.TYPE_BYTE_INDEXED);
		myOptions &= ~INDEXED_IMAGE;
		myOptions |= INDEXED_256_IMAGE;
		return;
	}else if ((options & BLACK_AND_WHITE_IMAGE) != 0){
		options &= ~BLACK_AND_WHITE_IMAGE;
		options |= MONO_IMAGE;
		myOptions = options;
	}
	int type = getImageType(options,(options & ALPHA_CHANNEL) != 0);
	if (type != TYPE_ARGB && type != TYPE_RGB)
		_awtImage = createBufferedImage(width,height,options,null,0,0,null);
	else
		_awtImage = newBufferedImage(width < 1 ? 1 : width,height < 1 ? 1 : height);
}

//-------------------------------------------------------------------
private static BufferedImage createBufferedImage(int width, int height, int options, byte[] bits, int offset, int scanLineLength, int[] colorTable)
//-------------------------------------------------------------------
{
	if (width < 1 || height < 1) throw new IllegalArgumentException();
	//
	if ((options & GRAY_SCALE_IMAGE) != 0)
		options = GRAY_SCALE_256_IMAGE;
	else if ((options & BLACK_AND_WHITE_IMAGE) != 0)
		options = !useTrueMono ? GRAY_SCALE_256_IMAGE : MONO_IMAGE;
	//
	int bpp = 0;
	if ((options & (GRAY_SCALE_256_IMAGE|INDEXED_256_IMAGE)) != 0) bpp = 8;
	else if ((options & (GRAY_SCALE_16_IMAGE|INDEXED_16_IMAGE)) != 0) bpp = 4;
	else if ((options & (GRAY_SCALE_4_IMAGE|INDEXED_4_IMAGE)) != 0) bpp = 2;
	else if ((options & (GRAY_SCALE_2_IMAGE|INDEXED_2_IMAGE)) != 0) bpp = 1;
	else throw new IllegalArgumentException();
	int bytesPerLine = ((bpp*width)+7)/8;
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
}
//===================================================================
public int getImageType()
//===================================================================
{
	return getImageType(myOptions,usesAlpha());
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
//===================================================================
public byte[] getImageBits()
//===================================================================
{
	try{
		return  ((DataBufferByte)((BufferedImage)_awtImage).getRaster().getDataBuffer()).getData();
	}catch(Throwable t){
		return null;
	}
}
//===================================================================
public int[] getImageColorTable()
//===================================================================
{
	int type = getImageType();
	if (!ImageTool.isAnIndexedImage(type)) return null;
	try{
		IndexColorModel icm = (IndexColorModel)((BufferedImage)_awtImage).getColorModel();
		int size = icm.getMapSize();
		int[] ret = new int[size];
		icm.getRGBs(ret);
		return ret;
	}catch(Throwable t){
		//throw new IllegalStateException();
		return null;
	}
}
//===================================================================
public int getImageScanLineType()
//===================================================================
{
	int type = getImageType();
	if (type == TYPE_ARGB || type == TYPE_RGB) return SCAN_LINE_INT_ARRAY;
	else return SCAN_LINE_BYTE_ARRAY;
}
//===================================================================
public int getImageScanLineLength()
//===================================================================
{
	int type = getImageType();
	if (type == TYPE_UNKNOWN) return 0;
	int bpp = getBPP(type);
	if (bpp == 32) return width;
	else return ((width*bpp)+7)/8;
}
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
	if (type != TYPE_ARGB && type != TYPE_RGB){
		byte[] all = getImageBits();
		int src = startLine*sll;
		int dest = offset;
		if (destScanLineLength == sll)
			System.arraycopy(all,src,destArray,dest,sll*numLines);
		else for (int i = 0; i<numLines; i++){
			System.arraycopy(all,src,destArray,dest,sll);
			src += sll;
			dest += destScanLineLength;
		}
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
		byte[] all = getImageBits();
		int src = startLine*sll;
		int dest = offset;
		if (sourceScanLineLength == sll)
			System.arraycopy(sourceArray,dest,all,src,sll*numLines);
		else for (int i = 0; i<numLines; i++){
			System.arraycopy(sourceArray,dest,all,src,sll);
			src += sll;
			dest += sourceScanLineLength;
		}
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
public boolean isWritableImage()
//===================================================================
{
	return !wasLoaded;
}
//===================================================================
//public boolean openImageData(boolean forWriting){return true;}
//===================================================================

//===================================================================
//public boolean closeImageData(){return true;}
//===================================================================

}
