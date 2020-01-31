package ewe.fx;

/**
This class can be used to wrap up an IImage or ImageData of any type to act
as an ImageData of type TYPE_ARGB or TYPE_RGB and, which uses int[] scan lines.<p>
It can also be used to wrap up an ImageData of any type to act as an IImage.
**/
//##################################################################
public class RGBImageData implements ImageData, IImage{
//##################################################################
protected IImage image;
protected ImageData imageData;
protected boolean deferToImageData;
protected boolean usesAlpha;
protected boolean canWrite;
protected int width;
protected int height;
private Object buffer;
private int[] line;
private Image myImage;
/**
Return true if the specified ImageData acts in the same way that an RGBImageData would
act. That is, its type is TYPE_RGB or TYPE_ARGB, and it uses scan lines of type int[], and
its scan line length is equal to its width.
**/
//===================================================================
public static boolean actsAsRGBImageData(ImageData image)
//===================================================================
{
	int ty = image.getImageType();
	if (
		(ty == TYPE_ARGB || ty == TYPE_RGB) &&
		(image.getImageScanLineType() == SCAN_LINE_INT_ARRAY) &&
		(image.getImageScanLineLength() == image.getImageWidth())
		) return true;
	return false;
}
/**
If the specified Image implements RGBImageData in exactly the same way as
an RGBImageData does, then return the Image itself. Otherwise return the image
wrapped in an RGBImageData object - using the provided wrapper object if it is
not null. If this method returns null this indicates that the image parameter did
not act as an RGBImageData would, but could not itself be wrapped up in an RGBImageData
object.
**/
//===================================================================
public static ImageData toImageData(Object image, RGBImageData wrapper)
//===================================================================
{
	if (image instanceof ImageData){
		if (actsAsRGBImageData((ImageData)image)) return (ImageData)image;
	}else if (!(image instanceof IImage)){
		throw new IllegalArgumentException();
	}
	if (wrapper == null) wrapper = new RGBImageData();
	wrapper.set(image);
	return wrapper;
}
//===================================================================
public static IImage toIImage(Object image, RGBImageData wrapper)
//===================================================================
{
	if (image instanceof IImage) return (IImage)image;
	if (!(image instanceof ImageData)) throw new IllegalArgumentException();
	if (wrapper == null) wrapper = new RGBImageData();
	wrapper.set(image);
	return wrapper;
}
/**
If you use this constructor, make sure you call one of the set() method before
using any of the other methods.
**/
//===================================================================
public RGBImageData()
//===================================================================
{

}
//===================================================================
public RGBImageData(Object image)
//===================================================================
{
	set(image);
}
//===================================================================
public RGBImageData(Object image,boolean useImageDataOverIImage)
//===================================================================
{
	set(image,useImageDataOverIImage);
}
public void set(Object newImage)
{
	set(newImage,false);
}
/**
This can be used to set or change the image the RGBImageData is using.
The type of newImage can be IImage or ImageData.
**/
//===================================================================
public void set(Object newImage,boolean useImageDataOverIImage)
//===================================================================
{
	free();
	imageData = null;
	image = null;
	deferToImageData = false;
	//
	if (!useImageDataOverIImage || !(newImage instanceof ImageData)){
		if (newImage instanceof IImage){
			if (!(newImage instanceof ImageData) || !actsAsRGBImageData((ImageData)newImage)){
				image = (IImage)newImage;
				width = image.getWidth();
				height = image.getHeight();
				usesAlpha = image.usesAlpha();
				canWrite = (image instanceof Image && !((Image)image).wasDecoded());
				return;
			}
		}
	}
	//
	if (newImage instanceof ImageData){
		imageData = (ImageData)newImage;
		width = imageData.getImageWidth();
		height = imageData.getImageHeight();
		usesAlpha = imageData.getImageType() == TYPE_ARGB;
		canWrite = imageData.isWritableImage();
		deferToImageData = actsAsRGBImageData(imageData);
		return;
	}
	throw new IllegalArgumentException();
}
/**
This returns either TYPE_ARGB or TYPE_RGB.
**/
//===================================================================
public int getImageType()
//===================================================================
{
	return usesAlpha ? TYPE_ARGB : TYPE_RGB;
}
/**
This always returns SCAN_LINE_INT_ARRAY;
**/
//===================================================================
public int getImageScanLineType()
//===================================================================
{
	return SCAN_LINE_INT_ARRAY;
}
/**
This always returns the width of the original image.
**/
//===================================================================
public int getImageScanLineLength()
//===================================================================
{
	return width;
}
/**
Get the image scan lines, always as ARGB integer values.
**/
//===================================================================
public void getImageScanLines(int startLine, int numLines, Object destArray, int offset, int destScanLineLength)
//===================================================================
{
	if (deferToImageData){
		imageData.getImageScanLines(startLine,numLines,destArray,offset,destScanLineLength);
		return;
	}
	if (!(destArray instanceof int[])) throw new IllegalArgumentException();
	if (imageData != null){
		int gotLines = numLines;
		try{
			buffer = ImageTool.getScanLineBuffer(imageData,numLines,buffer);
		}catch(OutOfMemoryError om){
			gotLines = 1;
			buffer = ImageTool.getScanLineBuffer(imageData,1,buffer);
		}
		int ty = imageData.getImageType();
		int idsll = imageData.getImageScanLineLength();
		int[] ct = imageData.getImageColorTable();
		while(numLines != 0){
			int get = gotLines;
			if (get > numLines) get = numLines;
			imageData.getImageScanLines(startLine,get,buffer,0,idsll);
			ImageTool.toARGB(ty,width,ct,buffer,0,idsll,(int[])destArray,offset,destScanLineLength,get);
			offset += get*destScanLineLength;
			numLines -= get;
		}
	}else{
		int sll = width;
		if (destScanLineLength == sll)
			image.getPixels((int[])destArray,offset,0,startLine,width,numLines,0);
		else for (int i = 0; i<numLines; i++){
			image.getPixels((int[])destArray,offset+i*destScanLineLength,0,startLine+i,width,1,0);
		}
	}
}
/**
Set the image scan lines, always as ARGB integer values.
**/
//===================================================================
public void setImageScanLines(int startLine, int numLines, Object sourceArray, int offset, int sourceScanLineLength)
throws IllegalStateException
//===================================================================
{
	if (deferToImageData){
		imageData.setImageScanLines(startLine,numLines,sourceArray,offset,sourceScanLineLength);
		return;
	}
	if (!canWrite) throw new IllegalStateException();
	if (imageData != null){
		int gotLines = numLines;
		try{
			buffer = ImageTool.getScanLineBuffer(imageData,numLines,buffer);
		}catch(OutOfMemoryError om){
			gotLines = 1;
			buffer = ImageTool.getScanLineBuffer(imageData,1,buffer);
		}
		int ty = imageData.getImageType();
		int idsll = imageData.getImageScanLineLength();
		int[] ct = imageData.getImageColorTable();
		while(numLines != 0){
			int get = gotLines;
			if (get > numLines) get = numLines;
			ImageTool.fromARGB((int[])sourceArray,offset,sourceScanLineLength,ty,width,ct,buffer,0,idsll,get);
			imageData.setImageScanLines(startLine,get,buffer,0,idsll);
			offset += get*sourceScanLineLength;
			numLines -= get;
		}
	}else{
		int sll = width;
		if (!(image instanceof Image)) throw new IllegalStateException();
		Image im = (Image)image;
		if (sourceScanLineLength == sll)
			im.setPixels((int[])sourceArray,offset,0,startLine,width,numLines,0);
		else for (int i = 0; i<numLines; i++){
			im.setPixels((int[])sourceArray,offset+i*sourceScanLineLength,0,startLine+i,width,1,0);
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
/**
Open the source to retrieve or optionally set scan lines.
**/
/*
//===================================================================
public boolean openImageData(boolean forWriting)
//===================================================================
{
	if (forWriting && !canWrite) throw new IllegalArgumentException();
	return true;
}
*/
/**
Close the source when you have finished retrieving scan lines.
**/
/*
//===================================================================
public boolean closeImageData(){return true;}
//===================================================================
*/
/**
This always returns null.
**/
//===================================================================
public int[] getImageColorTable()
//===================================================================
{
	return null;
}
/**
Returns if you can write data to the Image.
**/
//===================================================================
public boolean isWritableImage() {return canWrite;}
//===================================================================

//===================================================================
public Color getBackground(){return null;}
public void draw(Graphics g, int x, int y, int options)
{
	toImage().draw(g,x,y,options);
}
public void free() {buffer = null; if (myImage != null) myImage.free(); myImage = null;}
public boolean usesAlpha(){return usesAlpha;}
public int getWidth() {return width;}
public int getHeight() {return height;}
//===================================================================
public int[] getPixels(int[] dest, int offset, int x, int y, int width, int height, int options)
//===================================================================
{
	if (dest == null) dest = new int[offset+width*height];
	line = (int[])ImageTool.getScanLineBuffer(this,1,line);
	for (int i = 0; i<height; i++){
		getImageScanLines(y+i,1,line,0,this.width);
		System.arraycopy(line,x,dest,offset+(i*width),width);
	}
	return dest;
}

//===================================================================
public Image toImage()
//===================================================================
{
	if (image instanceof Image) return (Image)image;
	if (imageData instanceof Image) return (Image)imageData;
	if (myImage != null) return myImage;
	myImage = new Image(width,height,usesAlpha ? Image.ARGB_IMAGE : Image.RGB_IMAGE);
	line = (int[])ImageTool.getScanLineBuffer(this,1,line);
	for (int i = 0; i<height; i++){
		getImageScanLines(i,1,line,0,width);
		myImage.setImageScanLines(i,1,line,0,width);
	}
	return myImage;
}
//##################################################################
}
//##################################################################

