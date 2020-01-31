/* $MirOS: contrib/hosted/ewe/classes/ewe/fx/ImageCodec.java,v 1.2 2008/05/02 20:52:01 tg Exp $ */

/*********************************************************************************
 *  Ewe Virtual Machine - Version 1.14, January 24, 2002                         *
 *  Copyright (c) 2008 Thorsten “mirabilos” Glaser                               *
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

import ewe.io.BasicStream;
import ewe.io.IO;
import ewe.io.IOException;
import ewe.io.MemoryFile;
import ewe.io.RandomAccessStream;
import ewe.io.Stream;
import ewe.io.StreamObject;
import ewe.util.Errorable;
import ewe.util.Utils;
import ewe.zip.CRC32;
import ewe.zip.InflaterInputStream;

/**
* This class is used by Image for decoding image formats. You would not use this class directly.
**/
//##################################################################
public class ImageCodec extends Errorable{
//##################################################################

public int width; //Must be first.
public int height; //Must be second.
public int type; //Must be third.
public int bitDepth; //Must be fourth.
public int compression; //Must be fifth.
public int filter; //Must be sixth.
public int interlace; //Must be 7th.
public byte [] paletteBytes; //Must be 8th.
public byte [] transparencyBytes; //Must be 9th.
protected int transparentColor; //Must be 10th.
protected int transparentColorLow; //Must be 11th.
public boolean isBMPFile;
public boolean isJPEGFile;
public boolean isGIFFile;
public int pauseTime;

public static boolean canDecodeGIF = true;//false;
public static boolean canDecodeJPEG = true;

protected final static int [] signature = {137,80,78,71,13,10,26,10};

//-------------------------------------------------------------------
protected pngChunk getChunk(RandomAccessStream ras)
//-------------------------------------------------------------------
{
 	pngChunk pc = new pngChunk();
	if (!pc.read(ras))
		return (pngChunk) returnError(pc.error,null);
	return pc;
}

/**
* This is the background color if one was specified.
**/
public Color background;
/**
* This is the transparent color if one was specified.
**/
public Color transparent;

pngChunk palette, header, firstDat, transparency;

RandomAccessStream source;
//ByteArray data = new ByteArray();

//===================================================================
public static boolean hasPNGSignature(byte [] startData)
//===================================================================
{
	if (startData == null) return false;
	if (startData.length < signature.length) return false;
	for (int i = 0; i<signature.length; i++)
		if ((startData[i] & 0xff) != signature[i])
			return false;
	return true;
}
//===================================================================
public static boolean hasBMPSignature(byte [] startData)
//===================================================================
{
	if (startData == null) return false;
	if (startData.length < 3) return false;
	return (startData[0] == (byte)'B' && startData[1] == (byte)'M');
}
//===================================================================
public static boolean hasGIFSignature(byte [] startData)
//===================================================================
{
	if (startData == null) return false;
	if (startData.length < 3) return false;
	return (startData[0] == (byte)'G' && startData[1] == (byte)'I' && startData[2] == (byte)'F');
}
//===================================================================
public static boolean hasJPEGSignature(byte [] startData)
//===================================================================
{
	if (startData == null) return false;
	if (startData.length < 10) return false;
	return (
	startData[0] == (byte)0xff &&
	startData[1] == (byte)0xd8 &&
	startData[2] == (byte)0xff
	/*
	startData[6] == (byte)'J' && startData[7] == (byte)'F' &&
	startData[8] == (byte)'I' && startData[9] == (byte)'F'
	*/
	);
}

//===================================================================
public static boolean isDecodable(byte [] first16Bytes)
//===================================================================
{
	if (hasPNGSignature(first16Bytes)) return true;
	if (hasBMPSignature(first16Bytes)) return true;
	if (hasJPEGSignature(first16Bytes) && canDecodeJPEG) return true;
	if (hasGIFSignature(first16Bytes) && canDecodeGIF) return true;
	return false;
}
private static final int
//============================================================
	readInt(byte [] source,int offset,int numBytes)// throws IOException
//============================================================
{
	int ret = 0;
	for (int i = offset+numBytes-1; i>=offset; i--) {
		ret = (ret<<8) & 0xffffff00;
		ret |= ((int)source[i])&0xff;
	}
	return ret;
}
//===================================================================
public boolean decodeBMP(RandomAccessStream ras)
//===================================================================
{
	source = ras;
	if (!ras.isOpen()) return false;
	if (!ras.seek(0)) return false;
	byte [] p = new byte[16];
	if (IO.readFully(ras,p) == p.length)
	if (hasJPEGSignature(p)){
		if (!ras.seek(0)) return false;
		pngSpecs ps = new pngSpecs(this);
		if (!getJPEGSpecs(ps,ras)) return false;
		width = ps.width;
		height = ps.height;
		isJPEGFile = true;
		return true;
	}
	if (hasGIFSignature(p)){
		if (!ras.seek(0)) return false;
		pngSpecs ps = new pngSpecs(this);
		if (!getGIFSpecs(ps,ras)) return false;
		width = ps.width;
		height = ps.height;
		isGIFFile = true;
		return true;
	}
	if (!ras.seek(0)) return false;
	p = new byte[36];
	if (IO.readFully(ras,p) != p.length) {
		return returnError("Bad Image Format",false);
	}
	if (p[0] == (byte)'B' && p[1] == (byte)'M'){
		int bitmapOffset = readInt(p,10,4);
		int infoSize = readInt(p,14,4);
		if (infoSize == 40){
			width = readInt(p,18,4);
			height = readInt(p,22,4);
			if (width < 65535 && height < 65535){
				if (height < 0) height = -height;
				int bpp = readInt(p,28,2);
				if (bpp == 1 || bpp == 4 || bpp == 8 || bpp == 24){
					int compression = readInt(p,30,4);
					if (compression == 0){
						isBMPFile = true;
						return true;
					}
				}
			}
		}
	}
	return returnError("Bad Image Format",false);
}
/**
* Call this first to decode the parameters of the PNG image.
**/
//===================================================================
public boolean decode(RandomAccessStream ras)
//===================================================================
{
	source = ras;
	if (!ras.isOpen()) return false;
	if (!ras.seek(0)) return false;
	byte [] sig = new byte[signature.length];
	if (IO.readFully(ras,sig) != sig.length) return decodeBMP(ras);
	for (int i = 0; i<sig.length; i++)
		if ((sig[i] & 0xff) != signature[i])
			return decodeBMP(ras);
	pngChunk pc = getChunk(ras);
	if (pc == null || !pc.name.equals("IHDR")) return returnError("No IHDR",false);
	header = pc;
	width = Utils.readInt(pc.data,0,4);
	height = Utils.readInt(pc.data,4,4);
	bitDepth = pc.data[8] & 0xff;
	type = pc.data[9] & 0xff;
	compression = pc.data[10] & 0xff;
	filter = pc.data[11] & 0xff;
	interlace = pc.data[12] & 0xff;

	int scale = 0;
	switch(bitDepth){
		case 1:  scale = 7; break;
		case 2:  scale = 6; break;
		case 4:  scale = 4; break;
		default:  scale = 0; break;
	}

	for (pc = getChunk(ras); pc != null; pc = getChunk(ras)){
		if (pc.name.equals("IDAT")) {
			firstDat = pc;
			break;
		}
		else if (pc.name.equals("PLTE")){
			palette = pc;
			paletteBytes = pc.data;
		}
		else if (pc.name.equals("bKGD")){
			switch(type){
				case 0:
					if (pc.data.length < 2) break;
					int v = bitDepth == 16 ? pc.data[0] & 0xff : (pc.data[1] & 0xff) << scale;
					background = new Color(v,v,v);
					break;
				case 3:
					if (pc.data.length < 1 || palette == null) break;
					int idx = pc.data[0] & 0xff;
					if (idx >= palette.data.length/3) break;
					background = new Color(palette.data[idx*3] & 0xff,palette.data[idx*3+1] & 0xff,palette.data[idx*3+2] & 0xff);
					break;
				case 2:
				case 6:
					if (pc.data.length < 6) break;
					if (bitDepth == 16)
						background = new Color(pc.data[0] & 0xff,pc.data[2] & 0xff,pc.data[4] & 0xff);
					else
						background = new Color(pc.data[1] & 0xff,pc.data[3] & 0xff,pc.data[5] & 0xff);
					break;
				default:
					break;
			}
		}
		else if (pc.name.equals("tRNS")){
			transparency = pc;
			transparencyBytes = pc.data;
			switch(type){
				case 0: //Grayscale.
					if (pc.data.length < 2) transparency = null;
					else {
						transparentColor = (pc.data[0] & 0xff)<<8|(pc.data[1] & 0xff);
						int v = bitDepth == 16 ? pc.data[0] & 0xff : pc.data[1] & 0xff;
						v = v << scale;
						transparent = new Color(v,v,v);
					}
					break;
				case 2: //True Color.
					if (pc.data.length < 6) transparency = null;
					else {
						transparentColor = bitDepth == 16 ?
							(pc.data[0] & 0xff) << 16 |(pc.data[2] & 0xff) << 8 |(pc.data[4] & 0xff):
							(pc.data[1] & 0xff) << 16 |(pc.data[3] & 0xff) << 8 |(pc.data[5] & 0xff);
						transparentColorLow = bitDepth == 16 ?
							(pc.data[1] & 0xff) << 16 |(pc.data[3] & 0xff) << 8 |(pc.data[5] & 0xff):
							0;
						transparent = new Color((transparentColor >> 16) & 0xff,(transparentColor >> 8) & 0xff,(transparentColor >> 0) & 0xff);
					}
					break;
				case 3: //Palette.
					if (palette == null) break;
					int tsize = pc.data.length;
					if (tsize > palette.data.length/3) tsize = palette.data.length/3;
					if (tsize == 1){
						for (int i = 0; i<tsize; i++){
							if (pc.data[i] == 0){
								transparent = new Color((palette.data[i*3] & 0xff),(palette.data[i*3+1] & 0xff),(palette.data[i*3+2] & 0xff));
								//ewe.sys.Vm.debug(transparent.toString(),1);
								break;
							}
						}
					}
					break;
				default:
					transparency = null;
					break;
			}
		}
		else if (pc.name.equals("IEND")) break;
	}
	return true;
}

//-------------------------------------------------------------------
int PaethPredictor (int a,int b,int c)
//-------------------------------------------------------------------
{
	int p = a+b-c;
	int pa = p-a; if (pa < 0) pa = -pa;
	int pb = p-b; if (pb < 0) pb = -pb;
	int pc = p-c; if (pc < 0) pc = -pc;
	if (pa <= pb && pa <= pc) return a;
	else if (pb <= pc) return b;
	else return c;
}

//-------------------------------------------------------------------
boolean unfilter(byte [] line,byte [] old,int length,int bbp)
//-------------------------------------------------------------------
{
		if (line[0] == 1)
			for (int w = 0; w<length-1; w++){
				if (w >= bbp)
					line[1+w] += line[1+w-bbp];
			}
		else if (line[0] == 2)
			for (int w = 0; w<length-1; w++){
				if (old != null)
					line[1+w] += old[1+w];
			}
		else if (line[0] == 3)
			for (int w = 0; w<length-1; w++){
				int av = 0;
				if (w >= bbp) av += (line[1+w-bbp] & 0xff);
				if (old != null) av += (old[1+w] & 0xff);
				av /= 2;
				line[1+w] += av;
			}
		else if (line[0] == 4)
			for (int w = 0; w<length-1; w++){
					line[1+w] +=
						PaethPredictor(w >= bbp ? line[1+w-bbp] & 0xff: 0,old != null ? old[1+w] & 0xff: 0, old != null && w >= bbp ? old[1+w-bbp] & 0xff :0);
			}
		else if (line[0] != 0)
			return returnError("Unknown filter: "+line[0],false);
		return true;
}

//ADAM7 interlacing use.
static final int [] hoffset = {0,4,0,2,0,1,0};
static final int [] voffset = {0,0,4,0,2,0,1};
static final int [] hfreq = {8,8,4,4,2,2,1};
static final int [] vfreq = {8,8,8,4,4,2,2};

//-------------------------------------------------------------------
static native boolean toImagePixels(pngSpecs specs,BasicStream dataStream,Image image);
//-------------------------------------------------------------------
static native boolean toJPEGPixels(pngSpecs specs,BasicStream dataStream,Image image,Rect sourceArea);
//-------------------------------------------------------------------
static native boolean getJPEGSpecs(pngSpecs specs,BasicStream dataStream);
//-------------------------------------------------------------------

//-------------------------------------------------------------------
boolean toImage(Image im)
//-------------------------------------------------------------------
{
	return toImage(im,null);
}
//-------------------------------------------------------------------
boolean toImage(Image im,Rect sourceArea)
//-------------------------------------------------------------------
{
	if (isJPEGFile) {
		if (!source.seek(0)) return returnError("Bad image format.",false);
		if (!toJPEGPixels(new pngSpecs(this),source,im,sourceArea))
			return returnError("Bad image format.",false);
		return true;
	}else if (isGIFFile) {
		if (!source.seek(0)) return returnError("Bad image format.",false);
		if (!toGIFPixels(new pngSpecs(this),source,im,sourceArea))
			return returnError("Bad image format.",false);
		return true;
	}
	Stream str = firstDat.getDataStream();
	Stream is = new InflaterInputStream(str);
	if (type == 6 || type == 4) im.alphaChannel = new byte[width*height];
	boolean ret =
		//javaToImage(im) != null;
		toImagePixels(new pngSpecs(this),is,im);
	if (!ret) return returnError("Bad image format.",false);
	im.transparent = transparent;
	im.background = background;
	im.hasAlpha = type == 6 || type == 4 || transparent != null;
	return true;
}
/*
//===================================================================
public Image toImage(int options)
//===================================================================
{
	Image im = new Image(width,height,(options & Image.TRUE_COLOR));
	boolean ret = toImage(im);
	if (!ret) return null;
	return im;
}
*/
/**
* Call this after calling decode to convert it to an Image.
**/
//===================================================================
public Image javaToImage(Image im)
//===================================================================
{
	int time = ewe.sys.Vm.getTimeStamp();
	int bytesPerLine = width*3;
	int bbp = 3;
	int maskStart = 0xFF;
	int maskShift = 8;
	int scale = 0;
	int freq = 8/bitDepth;
	int maxIdx = palette != null ? palette.data.length/3 : 0;
	int maxT = transparency != null ? transparency.data.length : 0;
	int byteStep = 1;
	if (type == 2 || type == 6){
		if (bitDepth == 16){
			bbp = type == 6 ? 8:6;
			bytesPerLine = width*bbp;
		}else{
			bbp = type == 6 ? 4:3;
			bytesPerLine = width*bbp;
		}
	}else if (type != 2){
		if (bitDepth >= 8) {
			bbp = bitDepth/8;
			bytesPerLine = width*bbp;
			byteStep = bbp;
		}else{
			bbp = 1;
			bytesPerLine = (width+freq-1)/freq;
			switch(bitDepth){
			case 1: maskStart = 0x80; maskShift = 1; scale = 7; break;
			case 2: maskStart = 0xC0; maskShift = 2; scale = 6; break;
			case 4: maskStart = 0xF0; maskShift = 4; scale = 4; break;
			case 8: maskStart = 0xFF; maskShift = 8; scale = 0; break;
			}
		}
		if (type == 4) {
			bbp *= 2;
			byteStep *= 2;
			bytesPerLine = width*bbp;
		}
	}

	Stream str = firstDat.getDataStream();
	//Stream is = new InflaterInputStream(str);
	Stream is = new InflaterInputStream(str);
	byte [] line = new byte[bytesPerLine+1];
	byte [] old = new byte[bytesPerLine+1];
	byte [] temp;
	int [] all = new int[width*height];
//..................................................................
	if (interlace == 0){
//..................................................................
		for (int i = 0; i<height; i++){
			int count = line.length;
			int len = IO.readFully(is,line);
			if (len != line.length)
				return (Image)returnError("Error reading scan line: "+(i+1)+" only got: "+len+" bytes.",null);
			if (!unfilter(line,i == 0 ? null : old,line.length,bbp)) return null;
			if (type == 2 || type == 6){
				//......................................................
				// True Color.
				//......................................................
				int o = i*width;
				for (int w = 0; w<width; w++){
					int wo = w*bbp;
					int alpha = 0xff000000;
					int colorLow = 0;
					int color = (bbp == 3 || bbp == 4) ?
						((line[1+wo] & 0xff) << 16)|((line[2+wo] & 0xff) << 8)|((line[3+wo] & 0xff)):
						((line[1+wo] & 0xff) << 16)|((line[3+wo] & 0xff) << 8)|((line[5+wo] & 0xff));
					if (bbp > 4)
						colorLow = ((line[2+wo] & 0xff) << 16)|((line[4+wo] & 0xff) << 8)|((line[6+wo] & 0xff));
					if (bbp == 4) alpha = (line[4+wo] & 0xff) << 24;
					else if (bbp == 8) alpha = (line[7+wo] & 0xff) << 24;
					else if (transparency != null && color == transparentColor && colorLow == transparentColorLow)
						alpha = 0;
					all[o+w] = alpha|color;
					if (type == 6)
						im.alphaChannel[o+w] = (byte)((alpha >> 24) & 0xff);
				}
			}else{
				int mask = maskStart;
				int idx = 1;
				int o = i*width;
				int shift = 8-maskShift;
				for (int w = 0; w<width; w++){
					boolean maxed = false;
					int v = (line[idx] & mask);
					maxed = v == mask;
					v = v >> shift;
					int fullV = bitDepth == 16 ? (line[idx+1] & mask)|v << 8: v;
					int alpha = 0xff000000;

					if ((transparency != null) && (fullV == transparentColor)){
						//ewe.sys.Vm.debug(fullV+", "+transparentColor,0);
						alpha = 0;
					}
					if (type == 0 || type == 4){
						//......................................................
						// Grayscale.
						//......................................................
						if (maxed) v = 0xff;
						else v = v << scale;
						int gray = (v << 16)|(v << 8)|(v);
						if (type == 4) alpha = (line[idx+bbp/2] & 0xff)<<24;
						all[o+w] = alpha|gray;
						if (type == 4)
							im.alphaChannel[o+w] = (byte)((alpha >> 24) & 0xff);
					}else{
						//......................................................
						// Palette.
						//......................................................
						if (v >= maxIdx) v = 0;
						int color = ((palette.data[v*3] & 0xff) << 16)|((palette.data[v*3+1] & 0xff) << 8)|((palette.data[v*3+2] & 0xff));
						alpha = (v < maxT) ? (transparency.data[v] & 0xff) << 24 : 0xff000000;
						all[o+w] = alpha|color;
					}
					if (shift == 0) {
						mask = maskStart;
						shift = 8-maskShift;
						idx += byteStep;
					}else{
						mask >>= maskShift;
						shift -= maskShift;
					}
				}
			}
			temp = old; old = line; line = temp;
		}
//..................................................................
	}else if (interlace == 1){
//..................................................................
		for (int pass = 1; pass <= 7; pass++){
			int numLines = (height-voffset[pass-1]+(vfreq[pass-1]-1))/vfreq[pass-1];
			int numPixels = (width-hoffset[pass-1]+(hfreq[pass-1]-1))/hfreq[pass-1];
			int numBytes = bbp >= 2 ? bbp*numPixels : (numPixels+freq-1)/freq;
			int pixelLine = voffset[pass-1];
			for (int ln = 0; ln<numLines; ln++){
				int len = IO.readFully(is,line,0,numBytes+1);
				if (len != numBytes+1)
					return (Image)returnError("Error reading scan line: "+(ln+1)+" only got: "+len+" bytes.",null);
				if (!unfilter(line,ln == 0 ? null : old,numBytes+1,bbp)) return null;
				if (type == 2 || type == 6){
					//......................................................
					// True Color.
					//......................................................
					int o = pixelLine*width;
					int pixelCol = hoffset[pass-1];
					for (int w = 0; w<numPixels; w++){
						int wo = w*bbp;
						int alpha = 0xff000000;
						int colorLow = 0;
						int color = (bbp == 3 || bbp == 4) ?
							((line[1+wo] & 0xff) << 16)|((line[2+wo] & 0xff) << 8)|((line[3+wo] & 0xff)):
							((line[1+wo] & 0xff) << 16)|((line[3+wo] & 0xff) << 8)|((line[5+wo] & 0xff));
						if (bbp > 4)
							colorLow = ((line[2+wo] & 0xff) << 16)|((line[4+wo] & 0xff) << 8)|((line[6+wo] & 0xff));
						if (bbp == 4) alpha = (line[4+wo] & 0xff) << 24;
						else if (bbp == 8) alpha = (line[7+wo] & 0xff) << 24;
						else if (transparency != null && color == transparentColor && colorLow == transparentColorLow)
							alpha = 0;
						all[o+pixelCol] = alpha|color;
						pixelCol += hfreq[pass-1];
					}
				}else{
					int mask = maskStart;
					int idx = 1;
					int o = pixelLine*width;
					int pixelCol = hoffset[pass-1];
					int shift = 8-maskShift;
					for (int w = 0; w<numPixels; w++){
						boolean maxed = false;
						int v = (line[idx] & mask);
						maxed = v == mask;
						v = v >> shift;
						int fullV = bitDepth == 16 ? (line[idx+1] & mask)|v << 8: v;
						int alpha = 0xff000000;
						if ((transparency != null) && (fullV == transparentColor))
							alpha = 0;
						if (type == 0 || type == 4){
							//......................................................
							// GrayScale.
							//......................................................
							if (maxed) v = 0xff;
							else v = v << scale;
							int gray = (v << 16)|(v << 8)|(v);
							if (type == 4) alpha = (line[idx+bbp/2] & 0xff)<<24;
							all[o+pixelCol] = alpha|gray;
							pixelCol += hfreq[pass-1];
						}else{
							//......................................................
							// Palette.
							//......................................................
							if (v >= maxIdx) v = 0;
							int color = ((palette.data[v*3] & 0xff) << 16)|((palette.data[v*3+1] & 0xff) << 8)|((palette.data[v*3+2] & 0xff));
							alpha = (v < maxT) ? (transparency.data[v] & 0xff) << 24 : 0xff000000;
							all[o+pixelCol] = alpha|color;
							pixelCol += hfreq[pass-1];
						}
						if (shift == 0) {
							mask = maskStart;
							shift = 8-maskShift;
							idx += byteStep;
						}else{
							mask >>= maskShift;
							shift -= maskShift;
						}
					}
				}
				temp = old; old = line; line = temp;
				pixelLine += vfreq[pass-1];
			}
		}
	}else{
		return (Image)returnError("Unknown interlace method: "+interlace,null);
	}
	int now = ewe.sys.Vm.getTimeStamp();
	time = now-time;

	im.setPixels(all,0,0,0,width,height,0);
	now = ewe.sys.Vm.getTimeStamp()-now;
	convertTime = time;
	setPixelTime = now;
	return im;
}
/**
* This is used for debugging only. Specifies the number of milliseconds it took to convert the image.
* (This is the first step in toImage()).
**/
public int convertTime;
/**
* This is used for debuggin only. Specifies the number of milliseconds it took to create the
* image from the raw pixel values. (This is the second step in toImage())
**/
public int setPixelTime;
//===================================================================
public String toString()
//===================================================================
{
	return "("+width+", "+height+"), Color-type: "+type+", Bit-depth: "+bitDepth+", Compression: "+compression+", Filter: "+filter+", Interlace: "+interlace;
}


//-------------------------------------------------------------------
static boolean readFully(BasicStream in,byte [] dest,int start,int length)
//-------------------------------------------------------------------
{
	while(length != 0){
		int got = in.nonBlockingRead(dest,start,length);
		if (got < 0) return false;
		length -= got; start += got;
	}
	return true;
}
static byte [] gifBuffer;
final static int NULL =   0;
final static int MAX_CODES =  4095;

/* Static variables */
int curr_size;                     /* The current code size */
int clear;                         /* Value for a clear code */
int ending;                        /* Value for a ending code */
int newcodes;                      /* First available code */
int top_slot;                      /* Highest code for current size */
int slot;                          /* Last read code */
/* The following static variables are used
 * for seperating out codes
 */
int navail_bytes = 0;              /* # bytes left in block */
int nbits_left = 0;                /* # bits left in current byte */
byte b1;                           /* Current byte */
byte [] byte_buff = new byte[257];               /* Current block */
int pbytes;                     /* Pointer to next byte in block */

int [] code_mask = {
     0,
     0x0001, 0x0003,
     0x0007, 0x000F,
     0x001F, 0x003F,
     0x007F, 0x00FF,
     0x01FF, 0x03FF,
     0x07FF, 0x0FFF
     };

int stack[] = new int[MAX_CODES + 1];            /* Stack for storing pixels */
int suffix[] = new int[MAX_CODES + 1];           /* Suffix table */
int prefix[] = new int[MAX_CODES + 1];           /* Prefix linked list */

/* This function initializes the decoder for reading a new image.
 */
int init_exp(int size)
{
   curr_size = size + 1;
   top_slot = 1 << curr_size;
   clear = 1 << size;
   ending = clear + 1;
   slot = newcodes = ending + 1;
   navail_bytes = nbits_left = 0;
   return(0);
}

static byte [] byteBuffer = new byte[1];

//===================================================================
int getByte(BasicStream s)
//===================================================================
{
	if (!readFully(s,byteBuffer,0,1)) return -1;
	return ((int)byteBuffer[0] & 0xff);
}


boolean getMoreBytes(BasicStream in)
{
	if (navail_bytes > 0) return true;
	pbytes = 0;
	boolean ret = true;
	if ((navail_bytes = getByte(in)) < 0) ret = false;
	else if (navail_bytes != 0){
		ret = readFully(in,byte_buff,0,navail_bytes);
	}
	//if (!ret) ewe.sys.Vm.debug("getMoreBytes() failed!");
	return ret;
}

int lastCode = -1;
int didCodes = 0;
/* get_next_code()
 * - gets the next code from the GIF file.  Returns the code, or else
 * a negative number in case of file errors...
 */
//===================================================================
int getNextCode(BasicStream in)
//===================================================================
   {
   int i, x;
   int ret;

   if (nbits_left == 0){
			if (!getMoreBytes(in)) return -1;
      b1 = byte_buff[pbytes++];
      nbits_left = 8;
      --navail_bytes;
   }
	 ret = (int)b1 & 0xff;
   ret = (ret >> (8 - nbits_left)) & 0xff;
   while (curr_size > nbits_left){
			if (!getMoreBytes(in)) return -1;
      b1 = byte_buff[pbytes++];
      ret |= ((int)b1 & 0xff) << nbits_left;
      nbits_left += 8;
      --navail_bytes;
   }
   nbits_left -= curr_size;
   ret &= code_mask[curr_size];
	 /*
	 if (didCodes++ < 10) {
	 		ewe.sys.Vm.debug("Code: "+ret);
			lastCode = ret;
	 }
	 */
   return ret;
   }
int badCodeCount;

int curLine = 0;
int lastOff = 0;
int interlacePass = -1;
int lastInterlaceValue = -8;
int interlaceValues[] = new int[]{8,0,8,4,4,2,2,1};
//===================================================================
int outputGifLine(pngSpecs specs,Image im,int [] buff)
//===================================================================
{
	int lineWidth = specs.width;
	for (int i = 0; i<lineWidth; i++){
		if (gifColors != null){
				//int alpha = 0xff000000;
				if (buff[i] > gifColors.length-1 || buff[i] < 0)
					;//ewe.sys.Vm.debug("Error: "+buff[i]+", "+gifColors.length);
				else{
						if (buff[i] == transparentIndex)
							buff[i] = gifColors[buff[i]];
						else
							buff[i] = gifColors[buff[i]]|0xff000000;
				}
		}else{
			int off = buff[i]*50;
			if (off > lastOff) {
				//ewe.sys.Vm.debug("Off: "+off);
				lastOff = off;
			}
			buff[i] = off << 16 | off << 8 | off;
		}
	}
	int actualLine = curLine;
	if (interlacePass >= 0){
		while(true){
			actualLine = lastInterlaceValue+interlaceValues[interlacePass*2];
			if (actualLine > specs.height-1){
				interlacePass++;
				if (interlacePass == 4) return 0;
				lastInterlaceValue = interlaceValues[interlacePass*2+1]-interlaceValues[interlacePass*2];
				continue;
			}
			lastInterlaceValue = actualLine;
			break;
		}
	}
	im.setPixels(buff,0,0,actualLine,lineWidth,1,0);
	curLine++;
	//ewe.sys.Vm.debug("Output: "+lineWidth);
	return 0;
}
int gifDecode(pngSpecs specs,BasicStream s,Image im)
   {
    int sp, bufptr;
   	int [] buf;
    int code, fc, oc, bufcnt;
    int c, size, ret;
		int linewidth = specs.width;
		badCodeCount = 0;
   /* Initialize for decoding a new image...
    */
   if ((size = getByte(s)) < 0)
      return(size);
   if (size < 2 || 9 < size)
      return(-1);//BAD_CODE_SIZE);
   init_exp(size);

   /* Initialize in case they forgot to put in a clear code.
    * (This shouldn't happen, but we'll try and decode it anyway...)
    */
   oc = fc = 0;

   /* Allocate space for the decode buffer
    */
		buf = new int[linewidth+1];

   /* Set up the stack pointer and decode buffer pointer
    */
   sp = 0;//stack;
   bufptr = 0;//buf;
   bufcnt = linewidth;

   /* This is the main loop.  For each code we get we pass through the
    * linked list of prefix codes, pushing the corresponding "character" for
    * each code onto the stack.  When the list reaches a single "character"
    * we push that on the stack too, and then start unstacking each
    * character for output in the correct order.  Special handling is
    * included for the clear code, and the whole thing ends when we get
    * an ending code.
    */
   while ((c = getNextCode(s)) != ending)
      {

      /* If we had a file error, return without completing the decode
       */
      if (c < 0)
         {
				 //ewe.sys.Vm.debug("c<0");
         return(0);
         }

      /* If the code is a clear code, reinitialize all necessary items.
       */
      if (c == clear)
         {
         curr_size = size + 1;
         slot = newcodes;
         top_slot = 1 << curr_size;

         /* Continue reading codes until we get a non-clear code
          * (Another unlikely, but possible case...)
          */
         while ((c = getNextCode(s)) == clear)
            ;

         /* If we get an ending code immediately after a clear code
          * (Yet another unlikely case), then break out of the loop.
          */
         if (c == ending)
            break;

         /* Finally, if the code is beyond the range of already set codes,
          * (This one had better NOT happen...  I have no idea what will
          * result from this, but I doubt it will look good...) then set it
          * to color zero.
          */
         if (c >= slot)
            c = 0;

         oc = fc = c;

         /* And let us not forget to put the char into the buffer... And
          * if, on the off chance, we were exactly one pixel from the end
          * of the line, we have to send the buffer to the outputGifLine()
          * routine...
          */
					buf[bufptr++] = c;
         //*bufptr++ = c;
         if (--bufcnt == 0)
            {
            if ((ret = outputGifLine(specs,im, buf)) < 0)
               {
               return(ret);
               }
						if (curLine >= specs.height) return 0;
            bufptr = 0;//buf;
            bufcnt = linewidth;
            }
         }
      else
         {

         /* In this case, it's not a clear code or an ending code, so
          * it must be a code code...  So we can now decode the code into
          * a stack of character codes. (Clear as mud, right?)
          */
         code = c;

         /* Here we go again with one of those off chances...  If, on the
          * off chance, the code we got is beyond the range of those already
          * set up (Another thing which had better NOT happen...) we trick
          * the decoder into thinking it actually got the last code read.
          * (Hmmn... I'm not sure why this works...  But it does...)
          */
         if (code >= slot)
            {
            if (code > slot)
               ++badCodeCount;
            code = oc;
						stack[sp++] = fc; //*sp++ = fc;
            }

         /* Here we scan back along the linked list of prefixes, pushing
          * helpless characters (ie. suffixes) onto the stack as we do so.
          */
         while (code >= newcodes)
            {
            stack[sp++] = suffix[code];
            code = prefix[code];
            }

         /* Push the last character on the stack, and set up the new
          * prefix and suffix, and if the required slot number is greater
          * than that allowed by the current bit size, increase the bit
          * size.  (NOTE - If we are all full, we *don't* save the new
          * suffix and prefix...  I'm not certain if this is correct...
          * it might be more proper to overwrite the last code...
          */
         stack[sp++] = code;
         if (slot < top_slot)
            {
            suffix[slot] = fc = code;
            prefix[slot++] = oc;
            oc = c;
            }
         if (slot >= top_slot)
            if (curr_size < 12)
               {
               top_slot <<= 1;
               ++curr_size;
               }

         /* Now that we've pushed the decoded string (in reverse order)
          * onto the stack, lets pop it off and put it into our decode
          * buffer...  And when the decode buffer is full, write another
          * line...
          */
         while (sp > 0)//stack)
            {
						buf[bufptr++] = stack[--sp];
            //*bufptr++ = *(--sp);
            if (--bufcnt == 0)
               {
               if ((ret = outputGifLine(specs,im, buf)) < 0)
                  {
                  //free(buf);
                  return(ret);
                  }
							if (curLine >= specs.height) return 0;
               bufptr = 0;
               bufcnt = linewidth;
               }
            }
         }
      }
   ret = 0;
   if (bufcnt != linewidth)
      ret = outputGifLine(specs,im, buf);//, (linewidth - bufcnt));
   //free(buf);
   return(ret);
   }

int [] gifColors;
int transparentIndex = -1;

//-------------------------------------------------------------------
int [] getColorTable(BasicStream dataStream,int size,int [] dest,boolean save)
//-------------------------------------------------------------------
{
	size = size*3;
	if (size != 0){
		if (gifBuffer.length < size)
			gifBuffer = new byte[size];
		if (!readFully(dataStream,gifBuffer,0,size))
			return null;
		if (!save) return new int[0];
		else{
			if (dest == null || dest.length < size/3)
				dest = new int[size/3];
			int c = 0;
			int max = dest.length;
			for (int i = 0; i<max; i++){
				dest[i] = (gifBuffer[c] & 0xff) << 16 | (gifBuffer[c+1] & 0xff) << 8 |(gifBuffer[c+2] & 0xff);
				c += 3;
			}
			return dest;
		}
	}else
		return new int[0];
}

//===================================================================
public boolean skipGifImage(BasicStream dataStream)
//===================================================================
{
	if (getByte(dataStream) < 0) return false;
	while(true){
		int num = getByte(dataStream);
		if (num < 0) return false;
		if (num == 0) return true;
		if (!readFully(dataStream,gifBuffer,0,num)) return false;
	}
}
//-------------------------------------------------------------------
 int getGIFSpecs(int whichImage,pngSpecs specs,BasicStream dataStream,Object imageResource,Rect sourceArea)
//-------------------------------------------------------------------
{
	if (gifBuffer == null) gifBuffer = new byte[256*3];
	int numImages = 0;
	//
	// Read past signature 3-bytes, version 3-bytes, logical screen descriptor 7 bytes.
	//
	if (!readFully(dataStream,gifBuffer,0,13)) return 0;
	int flags = gifBuffer[10];
	boolean hasGlobalColors = (flags & 0x80) != 0;
	int colorResolution = ((flags >> 4) & 0x7)+1;
	int backgroundIndex = (int)gifBuffer[11] & 0xff;
	int globalColorTableSize = hasGlobalColors ? (1 << ((flags & 0x7)+1)) : 0;
	if ((gifColors = getColorTable(dataStream,globalColorTableSize,gifColors,imageResource != null)) == null)
		return 0;
	int imageFlags = 0;
	while(true){
		if (!readFully(dataStream,gifBuffer,0,1)) return 0;
		//
		// Check for extensions.
		//
		if (gifBuffer[0] == 0x3b){
			if (whichImage < 0) return numImages;
			else return -1;
		}
		if (gifBuffer[0] == 0x21){
			if (!readFully(dataStream,gifBuffer,0,2)) return 0;
			int type = gifBuffer[0] & 0xff;
			int size = gifBuffer[1] & 0xff;
			// Read in data bytes and the end-block marker (a single zero byte).
			if (!readFully(dataStream,gifBuffer,0,size+1)) return 0;
			//
			if (type == 0xf9){
				//ewe.sys.Vm.debug((int)gifBuffer[1]+", "+(int)gifBuffer[2]);
				specs.pause = ((int)gifBuffer[1] & 0xff) | (((int)gifBuffer[2]&0xff) << 8);
				boolean hasTransparent = (gifBuffer[0] & 0x1) != 0;
				if (hasTransparent) {
					//ewe.sys.Vm.debug("Has transparent!");
					transparentIndex = gifBuffer[3] & 0xff;
				}
			}
			continue;
		}
		//
		// Get image specs.
		//
		if (gifBuffer[0] == 0x2c){
			if (!readFully(dataStream,gifBuffer,1,9)) return 0;
			specs.x = ((int)gifBuffer[2] & 0xff) << 8 | ((int)gifBuffer[1] & 0xff);
			specs.y = ((int)gifBuffer[4] & 0xff) << 8 | ((int)gifBuffer[3] & 0xff);
			specs.width = ((int)gifBuffer[6] & 0xff) << 8 | ((int)gifBuffer[5] & 0xff);
			specs.height = ((int)gifBuffer[8] & 0xff) << 8 | ((int)gifBuffer[7] & 0xff);
			if (specs.width == 0 || specs.height == 0) continue;
			//ewe.sys.Vm.debug("Got size: "+specs.width+", "+specs.height);
			imageFlags = gifBuffer[9];
			interlacePass = ((imageFlags & 0x40) != 0) ? 0 : -1;
			if ((imageFlags & 0x80) != 0)
				gifColors = getColorTable(dataStream, (1 << ((imageFlags & 0x7)+1)),gifColors,imageResource != null);
			numImages++;
			if (whichImage == (numImages-1)) break;
			else if (!skipGifImage(dataStream)) return 0;
		}
	}
	//
	//
	//
	if (imageResource == null) return numImages;
	Image image = imageResource instanceof Image ? (Image)imageResource : new Image(specs.width,specs.height,Image.TRUE_COLOR);
	image.wasLoaded = true;
	/*
	image.wasLoaded = false;
	Graphics g = new Graphics(image);
	g.setColor(Color.White);
	g.fillRect(0,0,specs.width,specs.height);
	g.free();
	image.wasLoaded = true;
	ewe.sys.Vm.debug("Returned: "+);
	*/
	if (transparentIndex != -1) {
		image.hasAlpha = true;
		if (image.alphaChannel == null)
			Mask.makeOpaque(image.alphaChannel = new byte[specs.width*specs.height]);
	}
	// gifBuffer must be at least 256 bytes.
	specs.transparentColor = transparentIndex;
	specs.interlace = interlacePass;
	if (!decodeGif(specs,dataStream,image,sourceArea,gifColors,gifBuffer))
		return 0;
	if (imageResource instanceof Image [])
		((Image [])imageResource)[0] = image;
	return 1;
	//gifDecode(specs,dataStream,image);
	//return true;
}

//-------------------------------------------------------------------
static native boolean decodeGif(pngSpecs specs,BasicStream dataStream,Image image,Rect sourceArea,int [] colorTable, byte [] gifBuffer);
//-------------------------------------------------------------------

//-------------------------------------------------------------------
boolean getGIFSpecs(pngSpecs specs,BasicStream dataStream)
//-------------------------------------------------------------------
{
	return getGIFSpecs(0,specs,dataStream,null,null) == 1;
}
//-------------------------------------------------------------------
boolean toGIFPixels(pngSpecs specs,BasicStream dataStream,Image image,Rect sourceArea)
//-------------------------------------------------------------------
{
	//ewe.sys.Vm.debug("Image size: "+image.width+", "+image.height);
	return getGIFSpecs(0,specs,dataStream,image,sourceArea) == 1;
}


//===================================================================
public ImageInfo getImageInfo(Stream dataStream,int imageIndex,ImageInfo destination,boolean createImage)
throws IllegalArgumentException
//===================================================================
{
	if (imageIndex < 0) return null;
	if (destination == null) destination = new ImageInfo();
	pngSpecs s = new pngSpecs(this);
	Image [] got = createImage ? new Image[1] : null;
	int ret = getGIFSpecs(imageIndex,s,dataStream,got,null);
	if (ret == 0) throw new IllegalArgumentException();
	else if (ret == -1) return null; //Requested image does not exist.
	destination.width = s.width;
	destination.height = s.height;
	destination.x = s.x;
	destination.y = s.y;
	destination.format = destination.FORMAT_GIF;
	destination.canScale = false;
	destination.size = s.width*s.height*4;
	destination.pauseInMillis = s.pause*10;
	destination.image = got == null ? null : got[0];
	return destination;
}
//===================================================================
public ImageInfo [] getImages(Stream stream,boolean createImages)
//===================================================================
{
	try{
		RandomAccessStream ras = stream instanceof RandomAccessStream ? (RandomAccessStream)stream :
		new MemoryFile(stream,"r");
		ras.seek(0L);
		pngSpecs s = new pngSpecs(this);
		int num = getGIFSpecs(-1,s,ras,null,null);
		if (num == 0) throw new IllegalArgumentException();
		ImageInfo [] ret = new ImageInfo[num];
		for (int i = 0; i<num; i++){
			ras.seek(0L);
			ret[i] = getImageInfo(ras,i,null,true);
			if (ret[i] == null) throw new IllegalArgumentException();
			//ewe.sys.Vm.debug(ret[i].x+", "+ret[i].y+", "+ret[i].width+", "+ret[i].height+", "+ret[i].pauseInMillis);
		}
		if (ras != stream) ras.close();
		return ret;
	}catch(IOException e){
		throw new IllegalArgumentException();
	}
}
/*
//===================================================================
public static void main(String args[]) throws IOException
//===================================================================
{
	ewe.sys.Vm.startEwe(args);
	ImageCodec ic = new ImageCodec();
	ImageInfo [] images = ic.getImages(new File(args[0]).toReadableStream(),true);
	ewe.sys.Vm.debug("Images: "+images.length);
	ewe.sys.mThread.nap(3000);
	ewe.sys.Vm.exit(0);
}
*/
//##################################################################
}
//##################################################################

//##################################################################
class pngChunk extends Errorable{
//##################################################################

int position;
String name;
int dataLength;
CRC32 crc = new CRC32();
byte [] data;
RandomAccessStream source;
RandomAccessStream dataStream;

//===================================================================
public boolean read(RandomAccessStream ras)
//===================================================================
{
	crc.reset();
	source = ras;
	byte [] got = new byte[8];
	position = ras.getFilePosition();
	if (IO.readFully(ras,got) != 8)
		return returnError("Error reading chunk header!",false);
	dataLength = Utils.readInt(got,0,4);
	char [] by = new char[4];
	for (int i = 0; i<4; i++) by[i] = (char)(got[4+i] & 0xff);
	crc.update(got);
	name = new String(by);
	if (name.equals("IDAT")) return true;//skipOver();
	data = new byte[dataLength];
	if (dataLength != 0){
		if (IO.readFully(ras,data) != dataLength)
			return  returnError("Error reading chunk data!",false);
		crc.update(data);
	}
	if (IO.readFully(ras,got,0,4) != 4)
		return returnError("Error reading chunk CRC!",false);
	crc.update(got,0,4);
	return true;
}

//===================================================================
public boolean skipOver()
//===================================================================
{
	return source.seek(position+8+dataLength+4);
}

//===================================================================
public String toString()
//===================================================================
{
	return name+": "+dataLength+" bytes.";
}

//===================================================================
public pngChunk getNextData()
//===================================================================
{
	if (!skipOver()) return null;
	pngChunk pc = new pngChunk();
	if (!pc.read(source)) return null;
	if (!pc.name.equals("IDAT")) return null;
	return pc;
}

//===================================================================
public Stream getDataStream()
//===================================================================
{
	if (name.equals("IDAT")) return new pngDataStream(this);
	else return null;
}
//##################################################################
}
//##################################################################

//##################################################################
class pngDataStream extends StreamObject{
//##################################################################

public boolean flushStream() throws ewe.io.IOException {return true;}

pngChunk curChunk;
//===================================================================
public pngDataStream(pngChunk first)
//===================================================================
{
	curChunk = first;
}

public int pos = 0;

//-------------------------------------------------------------------
public int nonBlockingRead(byte [] dest,int offset,int length)
//-------------------------------------------------------------------
{
	if (curChunk == null) return -1;

	while (pos >= curChunk.dataLength){
		curChunk = curChunk.getNextData();
		if (curChunk == null) return -1;
		pos = 0;
	}
	if (length > curChunk.dataLength-pos) length = curChunk.dataLength-pos;
	curChunk.source.seek(curChunk.position+8+pos);
	int read = IO.readFully(curChunk.source,dest,offset,length);
	if (read > 0) pos += read;
	return read;
}
//-------------------------------------------------------------------
public int nonBlockingWrite(byte [] dest,int offset,int length) {return -2;}
//-------------------------------------------------------------------

//##################################################################
}
//##################################################################
