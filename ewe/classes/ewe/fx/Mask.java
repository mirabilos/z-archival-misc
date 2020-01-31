package ewe.fx;
/**
A Mask is used to include/exclude pixels of an Image. It is represented simply as an array
of bytes, where each bit maps simply to a pixel of the Image (see the "bits" variable).
**/
//##################################################################
public class Mask{
//##################################################################
//Don't move the first 3 variables.
/**
* The width of the mask.
**/
protected int width;
/**
* The height of the mask.
**/
protected int height;
/**
* This is the inclusion bitmap for the mask.
* The bits are stored with one bit per pixel. A 1 indicates an pixel to include and a 0 indicates
* a pixel to ignore. Each scan line is byte aligned and the number of bytes will be height*((width+7)/8)
**/
protected byte [] bits;
/**
 * Create a Mask with the specified width and height and using the specified bits. The bits provided are actually
 * used by the Mask and no new byte array is created.
 * @param width The width of the mask.
 * @param height The height of the mask.
 * @param bits The bits for the mask.
 * This must be the correct size otherwise an exception will be thrown.
 */
//===================================================================
public Mask(int width,int height,byte [] bits)
//===================================================================
{
	if (width < 0 || height < 0) throw new IllegalArgumentException();
	this.width = width; this.height = height;
	int need = height*((width+7)/8);
	if (bits != null)
		if (bits.length < need) throw new IllegalArgumentException();
	this.bits = bits;
}
/**
 * Create a Mask with the specified width and height. A new array will be created for the bits. The created mask
 * will be all zeros and so be fully exclusive.
 * @param width The width of the mask.
 * @param height The height of the mask.
 */
//===================================================================
public Mask(int width,int height)
//===================================================================
{
	this(width,height,new byte[height*((width+7)/8)]);
}
/**
 * Create a new Mask from an Image.
 * @param imageMask The Image mask where White pixel represent excluded bits and Black pixels
	represent included bits.
 */
//===================================================================
public Mask(Image imageMask)
//===================================================================
{
	this(imageMask.getWidth(),imageMask.getHeight());
	fromImageMask(imageMask);
}
/**
 * Create a new Mask from an Image.
* @param image The image.
* @param transparent A transparent color. All bits in the image that are of this color are
considered excluded from the mask, and all other bits are considered included.
*/
//===================================================================
public Mask(Image image,Color transparent)
//===================================================================
{
	this(image.getWidth(),image.getHeight());
	transparent.toInt();
	fromImageColor(image,transparent);
}

//===================================================================
Mask() {}
//===================================================================
/**
 * Return a copy of this mask, with its own new bit map.
 */
//===================================================================
public Mask getCopy()
//===================================================================
{
	Mask m = new Mask(width,height);
	ewe.sys.Vm.copyArray(bits,0,m.bits,0,m.bits.length);
	return m;
}
//===================================================================
public int getWidth(){return width;}
//===================================================================
public int getHeight() {return height;}
//===================================================================
public byte [] getBitmap() {return bits;}
//===================================================================

private static boolean hasNative = true;

//-------------------------------------------------------------------
private native Object nativeBitManipulate(Object parameters,int operation);
//-------------------------------------------------------------------

private static final int INVERT = 0;
private static final int INTERSECT = 1;
private static final int UNION = 2;
private static final int SUBAREA = 3;
private static final int TOMASKVALUES = 4;
private static final int FROMIMAGEMASK = 5;
private static final int FROMIMAGECOLOR = 6;
private static final int WHITEOUT = 7;
private static final int FROMIMAGEALPHA = 8;
private static final int MAKEOPAQUE = 9;
private static final int SCALE = 10;
private static final int BLEND = 11;
private static final int HAS_TRUE_ALPHA = 12;
private static final int MAKE_ALPHA = 13;

static private final int blend(int as,int ad,int s,int d,int shift)
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
private Object bitManipulate(Object parameters,int operation)
//-------------------------------------------------------------------
{
	if (hasNative) try{
		return nativeBitManipulate(parameters,operation);
	}catch(UnsatisfiedLinkError e){
		hasNative = false;
	}catch(SecurityException se){
		hasNative = false;
	}
	int [] pixels;
	int num = width*height;
	int nb = height*((width+7)/8);
	boolean hasTransparent = false;
	int numT = 0;
	switch(operation){
		case MAKE_ALPHA:{
			Object all[] = (Object [])parameters;
			int bits[] = (int [])all[0];
			int mask[] = all[1] == null ? null : (int [])all[1];
			int others[] = (int [])all[2];
			int offset = others[0], length = others[1], color = others[2], color2 = others[3];
			int i;
			if (mask == null){
				for (i = 0; i<length; i++){
					bits[offset] &= 0xffffff;
					if (bits[offset] != color && bits[offset] != color2) bits[offset] |= 0xff000000;
					offset++;
				}
			}else{
				int mo = 0;
				for (i = 0; i<length; i++){
					bits[offset] &= 0xffffff;
					if ((mask[mo++] & 0xffffff) != 0) bits[offset] |= 0xff000000;
					offset++;
				}
			}
			break;
		}
		case HAS_TRUE_ALPHA:
						pixels = (int [])parameters;
						num = pixels.length;
						for (int dest = 0; dest<num; dest++)
							if (((pixels[dest] & 0xff000000) != 0) && ((pixels[dest] & 0xff000000) != 0xff000000))
								return this;
						return null;
		case BLEND:{
			Object [] pars = (Object [])parameters;
			int [] dest = (int [])pars[0];
			int [] src = (int [])pars[1];
			int len = src.length;
			for (int i = 0; i<len; i++){
				int d = dest[i] & 0xffffff, s = src[i];
				int as = (s >> 24) & 0xff,
						ad = 0xff; //FIXME get the actual value.
				if (as == 0xff) dest[i] = s;
				else if (as == 0) continue;
				else{
					int save = 0;
					save |= blend(as,ad,s,d,16);
					save |= blend(as,ad,s,d,8);
					save |= blend(as,ad,s,d,0);
					save |= blend(as,ad,0xff000000,0xff000000,24);
					dest[i] = save;
				}

			}
			break;
		}

		case SCALE:{
			Object [] pars = (Object [])parameters;
			int [] src = (int [])pars[0];
			int [] dest = (int [])pars[1];
			Rect srcRect = (Rect)pars[2];
			Dimension newDim = (Dimension)pars[3];
	int h = newDim.height, w = newDim.width;
	int sh = srcRect.height, sw = srcRect.width;
	double xsc = (double)sw/(double)w;
	double ysc = (double)sh/(double)h;
	double y = 0;
	for (int line = 0; line < h; line++, y += ysc){
		int off = line*w;
		if (y >= sh) y = sh;
		int srcOff = (int)y*sw;
		double x = 0;
		for (int col = 0; col < w; col++, x += xsc){
			if (x >= sw) x = sw-1;
			dest[off++] = src[srcOff+(int)x];
		}
	}
			break;
		}
		case MAKEOPAQUE:{
			byte [] alpha = (byte [])parameters;
			int len = alpha.length;
			for (int i = 0; i<len; i++) alpha[i] = (byte)0xff;
			return null;
		}
		case INVERT: {
			byte [] o = (byte [])parameters;
			if (o == null)
				for (int i = 0; i<nb; i++) bits[i] = (byte)~bits[i];
			else
				for (int i = 0; i<nb; i++) bits[i] = (byte)(bits[i] ^ o[i]);
			}
			break;
		case INTERSECT:{
			byte [] o = (byte [])parameters;
			for (int i = 0; i<nb; i++) bits[i] = (byte)(bits[i] & o[i]);
		}
			break;
		case UNION:{
			byte [] o = (byte [])parameters;
			for (int i = 0; i<nb; i++) bits[i] = (byte)(bits[i] | o[i]);
		}
			break;

		case TOMASKVALUES:
		case WHITEOUT:
		/*case FROMIMAGEMASK: - Not used */
		case FROMIMAGECOLOR:
		case FROMIMAGEALPHA:
		{
			boolean isFrom = operation == FROMIMAGEMASK || operation == FROMIMAGECOLOR || operation == FROMIMAGEALPHA;
			boolean isWhite = operation == WHITEOUT;
			int sl = (width+7)/8;
			int off = 0, dest = 0;
			int tcolor = 0;
			if (operation == FROMIMAGECOLOR){
				ewe.sys.Long c = (ewe.sys.Long)((Object [])parameters)[1];
				pixels = (int [])((Object [])parameters)[0];
				tcolor = (int)c.value & 0xffffff;
			}else{
				pixels = (int [])parameters;
			}
			for (int y = 0; y<height; y++){
				off = sl*y-1;
				byte by = 0;
				byte mask = (byte)0x01;
				for (int x = 0; x<width; x++){
					mask = (byte)((mask >> 1) & 0x7f);
					if (mask == 0) {
						mask = (byte)0x80;
						off++;
						by = bits[off];
					}
					if (operation == FROMIMAGEALPHA){
						if ((pixels[dest] & 0xff000000) != 0){
							bits[off] |= mask;
						}else{

							hasTransparent = true;
							numT++;
						}
					}else if (isFrom){
						int value = pixels[dest] & 0xffffff;
						if (value != tcolor)
							bits[off] |= mask;
						else{
							hasTransparent = true;
							numT++;
						}
					}else{
						if ((by & mask) == 0) pixels[dest] = 0xffffffff;
						else if (!isWhite) pixels[dest] = 0xff000000;
						else pixels[dest] |= 0xff000000;
					}
					dest++;
				}
			}
		}
			break;
	}
	if (operation == FROMIMAGEALPHA || operation == FROMIMAGECOLOR){
			//ewe.sys.Vm.debug(numT+" transparencies!");
 			if (hasTransparent) return this;
	}
	return null;
}
/**
 * This inverts the bitmap for the mask.
 */
//===================================================================
public void invert()
//===================================================================
{
	bitManipulate(null,INVERT);
}

//-------------------------------------------------------------------
private void validateSize(int width,int height,String message) throws IllegalArgumentException
//-------------------------------------------------------------------
{
	if (width != this.width || height != this.height)
		throw new IllegalArgumentException(message);
}

//-------------------------------------------------------------------
private void checkEqualMask(Mask other) throws IllegalArgumentException
//-------------------------------------------------------------------
{
	validateSize(other.width,other.height,"The Mask argument is not compatible.");
}
/**
 * This unions two masks together. All bits which are set in the other mask will also
 * be set in this Mask.
* @param other The other Mask, which must be the same dimensions as this Mask.
 */
//===================================================================
public void union(Mask other)
//===================================================================
{
	checkEqualMask(other);
	bitManipulate(other.bits,UNION);
}
/**
 * This leaves included only bits which are included in both this Mask and the other Mask.
* @param other The other Mask, which must be the same dimensions as this Mask.
 */
//===================================================================
public void intersection(Mask other)
//===================================================================
{
	checkEqualMask(other);
	bitManipulate(other.bits,INTERSECT);
}
/**
* This inverts all the bits in this Mask which correspond to set bits  in the other Mask.
* @param other The other Mask, which must be the same dimensions as this Mask.
*/
//===================================================================
public void invert(Mask other)
//===================================================================
{
	checkEqualMask(other);
	bitManipulate(other.bits,INVERT);
}
/**
 * This converts this bitmap to an Image based Mask for use with mImages and Graphics.drawImage() methods.
 * It effectively creates a new black and white Image where included bits are black and excluded bits are white.
 */
//===================================================================
public Image toImageMask()
//===================================================================
{
	int [] pixels = new int[width*height];
	bitManipulate(pixels,TOMASKVALUES);
	Image im = new Image(width,height,Image.RGB_IMAGE);
	im.setPixels(pixels,0,0,0,width,height,0);
	return im;
}
/**
* Set this Masks bits based on the provided monochrome (B/W) imageMask image.
* All white pixels in the image are considered to be transparent, non-white are
* considered to be opaque.
**/
//===================================================================
public void fromImageMask(Image imageMask)
//===================================================================
{
	fromImageColor(imageMask,Color.White);
}
/**
* Set this Masks bits based on the provided monochrome (B/W) imageMask image in the form
* of the pixel colors of the image.
* All white pixels in the image are considered to be transparent, non-white are
* considered to be opaque.
**/
//===================================================================
public void fromImageMask(int[] pixels)
//===================================================================
{
	fromImageColor(pixels,Color.White);
}
/**
* Set this Masks bits based on the provided monochrome image in the form
* of the pixel colors of the image.
**/
//===================================================================
public boolean fromImageColor(int[] pixels,Color transparentColor)
//===================================================================
{
	if (width*height > pixels.length) throw new IllegalArgumentException("The image is not the correct size");
	int remapped = Graphics.mapColor(transparentColor.toInt())&0xffffff;
	return bitManipulate(new Object[]{pixels,ewe.sys.Long.l1.set(remapped)},FROMIMAGECOLOR) != null;
}
/**
 * Get the bits for this mask from the transparent color in the image.
 * @param image The image to create a mask for.
 * @return true if there are transparent areas.
 */
//===================================================================
public boolean fromImageColor(Image image,Color transparentColor)
//===================================================================
{
	validateSize(image.getWidth(),image.getHeight(),"The image is not the correct size");
	return fromImageColor(image.getPixels(null,0,0,0,width,height,0),transparentColor);
}
/**
 * Get the bits for this mask from the alpha channel or transparent color in the image.
 * @param image The image to create a mask for.
 * @return true if there are transparent areas.
 */
//===================================================================
public boolean fromImage(Image image)
//===================================================================
{
	if (image.transparent != null)
		return fromImageColor(image,image.transparent);
	else if (image.hasAlpha){
		validateSize(image.getWidth(),image.getHeight(),"The image is not the correct size");
		int [] pixels = image.getPixels(null,0,0,0,width,height,0);
		return bitManipulate(pixels,FROMIMAGEALPHA) != null;
	}else
		return false;
}
/**
 * Get the bits for this mask from the alpha channel of the image.
 * @param image The image to create a mask for.
 * @return true if there are transparent areas.
 */
//===================================================================
public boolean fromIImage(IImage image)
//===================================================================
{
	if (image instanceof Image) return fromImage((Image)image);
	else if (image.usesAlpha()){
		validateSize(image.getWidth(),image.getHeight(),"The image is not the correct size");
		int [] pixels = image.getPixels(null,0,0,0,width,height,0);
		return bitManipulate(pixels,FROMIMAGEALPHA) != null;
	}else
		return false;
}
/**
* This converts the supplied image (which must be the same size as the mask) into an mImage which consists of
* an Image and Image mask.
**/
//===================================================================
public mImage toMImage(IImage image,int srcX,int srcY)
//===================================================================
{
	return toMImage(image,srcX,srcY,null);
}
//-------------------------------------------------------------------
mImage toMImage(IImage image,int srcX,int srcY,mImage dest)
//-------------------------------------------------------------------
{
	if (srcX < 0 || image.getWidth()-srcX < width || srcY < 0 || image.getHeight()-srcY < height)
		throw new IllegalArgumentException();
	mImage mi = dest;
	if (mi == null) mi = new mImage();
	int [] pixels = image.getPixels(null,0,srcX,srcY,width,height,0);
	bitManipulate(pixels,WHITEOUT);
	Image im = new Image(width,height,Image.RGB_IMAGE);
	im.setPixels(pixels,0,0,0,width,height,0);
	im.transparent = null; im.hasAlpha = false;
	Image got = toImageMask();
	mi.setImage(im,got);
	pixels = null;
	//mi.bitmask = this;
	return mi;
}
//===================================================================
public mImage toMImage(IImage image)
//===================================================================
{
	return toMImage(image,0,0);
}

static Mask tool = null;
static Mask getTool()
{
	if (tool == null) tool = new Mask();
	return tool;
}

//===================================================================
static void makeOpaque(byte [] alpha)
//===================================================================
{
	getTool().bitManipulate(alpha,MAKEOPAQUE);
}

static Image scale(Image image,Rect area,int newWidth,int newHeight,int imageAndScaleOptions)
{
	int forDisplay = imageAndScaleOptions & Image.FOR_DISPLAY;
	int opts = imageAndScaleOptions & ~Image.FOR_DISPLAY;
	if (opts == 0) opts = image.hasAlpha ? Image.RGB_IMAGE : 0;
	opts |= forDisplay;
	Image im = new Image(newWidth,newHeight,opts);
	if (image.hasAlpha) im.enableAlpha();
	Object [] pars = new Object[4];
	pars[0] = image.getPixels(null,0,area.x,area.y,area.width,area.height,0);
	pars[1] = new int[newWidth*newHeight];
	pars[2] = area;
	pars[3] = new Dimension(newWidth,newHeight);
	getTool().bitManipulate(pars,SCALE);
	int [] got = (int [])pars[1];
	im.setPixels(got,0,0,0,newWidth,newHeight,0);
	return im;
}

//===================================================================
static public boolean hasTrueAlpha(Image im)
//===================================================================
{
	return getTool().bitManipulate(im.getPixels(null,0,0,0,im.getWidth(),im.getHeight(),0),HAS_TRUE_ALPHA) != null;
}

//===================================================================
static public void blend(Image dest,Image src,int destX,int destY,int width,int height,int srcX,int srcY) throws IllegalArgumentException
//===================================================================
{
	int [] d = dest.getPixels(null,0,destX,destY,width,height,0);
	int [] s = src.getPixels(null,0,srcX,srcY,width,height,0);
	blend(d,s);
	dest.setPixels(d,0,destX,destY,width,height,0);
}

//===================================================================
static public void blend(int  [] dest,int [] src)throws IllegalArgumentException
//===================================================================
{
	Object [] pars = new Object[2];
	pars[0] = dest;
	pars[1] = src;
	if (dest.length != src.length) throw new IllegalArgumentException();
	getTool().bitManipulate(pars,BLEND);
}

static void makeAlpha(int [] bits,int offset,Rect sourceArea,Image mask,Color transparent)
{
	int tc = transparent == null ? 0 : transparent.toInt() & 0xffffff;
	int nc = transparent == null ? 0 : Graphics.mapColor(tc) & 0xffffff;
	int [] mb = mask == null ? null : mask.getPixels(null,0,sourceArea.x,sourceArea.y,sourceArea.width,sourceArea.height,0);
	getTool().bitManipulate(new Object[]{bits,mb,new int[]{offset,sourceArea.width*sourceArea.height,tc,nc}},MAKE_ALPHA);
}

/*
//===================================================================
public void whiteOut(Image image)
//===================================================================
{
	validateSize(image.getWidth(),image.getHeight());
	int [] pixels = image.getPixels( int[width*height];
	if (hasNative) try{
		nativeBitManipulate(pixels,TOIMAGEVALUES);
	}catch(UnsatisfiedLinkError e){
		hasNative = false;
	}
	if (!hasNative) bitManipulate(pixels,TOIMAGEVALUES);
	Image im = new Image(width,height,Image.TRUE_COLOR);
	im.setPixels(pixels,0,0,0,width,height,0);
	return im;
}
*/
//##################################################################
}
//##################################################################

