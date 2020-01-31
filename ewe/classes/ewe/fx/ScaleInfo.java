package ewe.fx;
import ewe.sys.Vm;
import ewe.util.Utils;

//##################################################################
class ScaleInfo{
//##################################################################
//
// DO NOT MOVE ANY OF THESE FIELDS.
//
ImageData source, destination;
//
// This is set if source and destination are images respectively.
//
Image sourceImage, destinationImage;
//
int srcType, dstType;
//
int newWidth, newHeight, destX, destY;
//
int sWidth, sHeight, sbpl;
//
int dWidth, dHeight, dbpl;
//
int[] red, green, blue;
int[] scales;
//
byte[] srcB, destB;
int[] srcI;
//
int[] alpha;
//
int options;
int dsll;
//
// These are used if the width of the destination is actually greater
// than the width of the area that will be copied across.
//
Object temp;
int tempLength;
//-------------------------------------------------------------------
void getSetScanLine(int whichLine, boolean isGet)
//-------------------------------------------------------------------
{
	if (isGet)
		source.getImageScanLines(whichLine,1,srcI == null ? (Object)srcB: (Object)srcI,0,sbpl);
	else{
		if (temp != null){
			//destination.getImageScanLines(whichLine,1,temp,0,tempLength);
			System.arraycopy(destB != null ? (Object)destB : (Object)red,0,temp,0,dbpl);
			destination.setImageScanLines(whichLine,1,temp,0,tempLength);
		}else{
			destination.setImageScanLines(whichLine,1,destB != null ? (Object)destB : (Object)red,0,dbpl);
		}
	}
}

//-------------------------------------------------------------------
private static final int S_GRAY = ImageTool.S_GRAY, S_MONO = ImageTool.S_MONO, S_COLOR = ImageTool.S_COLOR;
//-------------------------------------------------------------------

//-------------------------------------------------------------------
private static boolean hasNative = true;
private native void nativeScale();
//-------------------------------------------------------------------
private static final int[] masks = {0x80,0x40,0x20,0x10,0x08,0x04,0x02,0x1};

//-------------------------------------------------------------------
void scale()
//-------------------------------------------------------------------
{
	//
	if (hasNative) try{
		nativeScale();
		return;
	}catch(SecurityException e){
		hasNative = false;
	}catch(UnsatisfiedLinkError er){
		hasNative = false;
	}
	boolean isRough = (options & ImageTool.SCALE_ROUGH) != 0;
	//
	int curSourceScanLine = -1;
	//
	int dx = sWidth/newWidth;
	int rx = sWidth%newWidth;
	//
	int dy = sHeight/newHeight;
	int ry = sHeight%newHeight;
	int yq = 0;
	int maxX = dWidth+destX;
	//
	long maxScale = isRough ? 1 : (long)(dx+1)*(long)(dy+1);
	boolean runAverage = (maxScale*255L) > Integer.MAX_VALUE;
	boolean usesAlpha = alpha != null;
	//
	//ewe.sys.Vm.debug(srcGray+", "+dstGray+", "+newWidth+", "+newHeight+", "+yOffset+", "+stripHeight);
	//
	int sy = 0;
	boolean ended = false;
	int y; for(y = 0; y<destY+dHeight && y<newHeight; y++){
		int yt = dy;
		yq += ry;
		if (yq >= newHeight) {
			yt++; //How many source lines to use.
			yq -= newHeight;
		}
		if (y >= destY){
			//
			int yyt = yt == 0 ? 1 : yt;
			//
			// yyt now holds the number of source scan lines that will be used for the current
			// destination scan line.
			//
			// The only way we can be on the same scan line we did last time,
			// is if we only done one scan line.
			//
			boolean dontPrepare = sy == curSourceScanLine;
			if (dontPrepare){
				yyt = 0;
			}else{
				Utils.zeroArrayRegion(red,0,dWidth);
				if (green != null) Utils.zeroArrayRegion(green,0,dWidth);
				if (blue != null) Utils.zeroArrayRegion(blue,0,dWidth);
				if (alpha != null) Utils.zeroArrayRegion(alpha,0,dWidth);
				if (destB != null) Utils.zeroArrayRegion(destB,0,destB.length);
				if (isRough) yyt = 1;
			}
			int yy; for(yy = 0; yy<yyt; yy++){
				//
				// Read in the current scan line.
				//
				int srcLine = sy+yy;
				if (srcLine != curSourceScanLine)
					getSetScanLine(srcLine,true);
					//source.getImageScanLines(srcLine,1,srcI == null ? (Object)srcB: (Object)srcI,0,sbpl);
				curSourceScanLine = srcLine;
				//
				// Now go across horizontally.
				//
				int xq = 0, di = 0, sx = 0;
				int x; for(x = 0; x<newWidth && x<maxX; x++){
					int xt = dx, xxt;
					xq += rx;
					if (xq >= newWidth){
						xt++;
						xq -= newWidth;
					}
					xxt = xt == 0 ? 1 : xt;
					if (isRough) xxt = 1;
					if (x >= destX){
						int ss = (srcType == S_MONO) ? sx/8 : sx;
						int smask = masks[sx%8];
						int xx; for(xx = 0; xx<xxt; xx++){
							int r, g, b, a = 0;
							if (srcType == S_COLOR){
								int value = srcI[ss];
								a = (value >> 24) & 0xff;
								r = (value >> 16) & 0xff;
								g = (value >> 8) & 0xff;
								b = (value) & 0xff;
								if (usesAlpha && a == 0) r = g = b = 0;
								ss++;
							}else if (srcType == S_GRAY){
								r = ((int)srcB[ss]) & 0xff;
								g = b = r;
								ss++;
							}else{ // MONO
								int value = 0;
								if ((srcB[ss] & smask) != 0) value = 0xff;
								r = value;
								g = b = r;
								smask >>= 1;
								if (smask == 0) {
									smask = 0x80;
									ss++;
								}
							}
							if (runAverage){
								long didAlready = (yy*xxt)+xx;
								if (dstType == S_COLOR){
									red[di] = (int)((((long)red[di]*didAlready)+r)/(didAlready+1));
									green[di] = (int)((((long)green[di]*didAlready)+g)/(didAlready+1));
									blue[di] = (int)((((long)blue[di]*didAlready)+b)/(didAlready+1));
									if (alpha != null) alpha[di] = (int)((((long)alpha[di]*didAlready)+a)/(didAlready+1));
								}else{
									red[di] += (int)((((long)red[di]*didAlready)+((r+g+b)/3))/(didAlready+1));
								}
							}else{
								if (dstType == S_COLOR){
									red[di] += r;
									green[di] += g;
									blue[di] += b;
									if (alpha != null) alpha[di] += a;
								}else{
									red[di] += (r+g+b)/3;
								}
							}
						}
						//
						// Done all the horizontal pixels on this line, for this dest pixel.
						// Now move on to the next pixel on this line.
						//
						scales[di] = xxt*yyt;
						di++;
					}
					sx += xt;
				}
			}
			//
			// Have done one destination scan line.
			// So now average it out and write it.
			//
			if (!dontPrepare){
				int i;
				if (!isRough && !runAverage)
					for (i = 0; i<red.length; i++){
						red[i] /= scales[i];
						if (green != null) green[i] /= scales[i];
						if (blue != null) blue[i] /= scales[i];
						if (alpha != null) alpha[i] /= scales[i];
					}
				//
				if (dstType == S_COLOR){
					for (i = 0; i<red.length; i++){
						red[i] = ((red[i] & 0xff) << 16)|((green[i] & 0xff) << 8)|(blue[i] & 0xff);
						if (alpha != null) red[i] |= (alpha[i] & 0xff) << 24;
						else red[i] |= 0xff000000;
					}
				}else if (dstType == S_GRAY){
					for (i = 0; i<red.length; i++)
						destB[i] = (byte)red[i];
				}else{
					int mm = 0x80, dd = 0;
					for (i = 0; i<red.length; i++){
						if (red[i] > 127) destB[dd] |= (byte)mm;
						mm >>= 1;
						if (mm == 0) {
							mm = 0x80;
							dd++;
						}
					}
				}
			}
			getSetScanLine(y-destY,false);
		}
		sy += yt;
	}
}
//##################################################################
}
//##################################################################

