package ewe.fx;

import ewe.sys.Vm;

/**
This implements an ImageData object using an Array of bytes or ints to store the pixel
information. You may only use an int[] as data if the type of the image
is TYPE_RGB or TYPE_ARGB. You MAY use a byte[] as data for TYPE_RGB or TYPE_ARGB,
but for TYPE_RGB the number of bytes per pixel must be 3 and for TYPE_ARGB it must
be 4.
**/
//##################################################################
public class ArrayImageData implements ImageData{
//##################################################################

private int[]  intValues;
private byte[] byteValues;
private int width;
private int height;
private int arrayOffset;
private int arrayScanLineLength;
private int imageType;
private int[] colorTable;
private boolean isWritable;
private int scanLineLength;

//===================================================================
public Object getArray()
//===================================================================
{
	return intValues == null ? (Object)byteValues : (Object)intValues;
}
//===================================================================
public Image toImage()
//===================================================================
{
	if (byteValues == null)
		return new Image(width,height,ImageTool.imageDataTypeToImageCreationOptions(imageType),intValues, 0, arrayScanLineLength);
	else
		return new Image(width,height,ImageTool.imageDataTypeToImageCreationOptions(imageType),byteValues, 0, arrayScanLineLength,colorTable);
}
/**
Return the scan line type that would be used by default by the specified
image data type.
**/
//===================================================================
public static int getScanLineTypeFor(int imageDataType)
//===================================================================
{
	imageDataType = ImageTool.toImageDataType(imageDataType);
	return (imageDataType == TYPE_RGB || imageDataType == TYPE_ARGB) ? SCAN_LINE_INT_ARRAY : SCAN_LINE_BYTE_ARRAY;
}
/**
Create and set the internal array for the ArrayImageData so that it is compatible
with the image type and scan line type for the specified ImageData parameter.
**/
public ArrayImageData set(ImageData compatibleWith, int width ,int height)
{
	int imageDataType = compatibleWith.getImageType();
	Object array = getArrayFor(imageDataType,compatibleWith.getImageScanLineType(),width,height);
	return set(imageDataType,width,height,array,0,getScanLineLengthFor(imageDataType,array instanceof int[] ? SCAN_LINE_INT_ARRAY : SCAN_LINE_BYTE_ARRAY,width),compatibleWith.getImageColorTable(),true);
}
/**
Create and set the internal array for the ArrayImageData so that it can hold
the scan lines for an Image of the specified type with the specified dimensions.
This method is used to provide a color table, which is necessary for TYPE_INDEXED_XXX
values.<p>
You can use the getArray() method to get the array that this method creates.
**/
//===================================================================
public ArrayImageData set(int imageDataType, int width, int height, int[] colorTable)
//===================================================================
{
	return set(imageDataType,width,height,getArrayFor(imageDataType,width,height),0,getScanLineLengthFor(imageDataType,getScanLineTypeFor(imageDataType),width),colorTable,true);
}
/**
Create and set the internal array for the ArrayImageData so that it can hold
the scan lines for an Image of the specified type with the specified dimensions.
You can use the getArray() method to get the array that this method creates.
**/
//===================================================================
public ArrayImageData set(int imageDataType, int width, int height)
//===================================================================
{
	return set(imageDataType,width,height,getArrayFor(imageDataType,width,height),0);
}
/**
Create and return an array that can hold scan lines for an ImageData of a
specified type with specified width and height, and using a specified scanline type.
**/
//===================================================================
public static Object getArrayFor(int imageDataType, int scanLineType, int width, int height)
//===================================================================
{
	return ImageTool.getArrayFor(imageDataType,scanLineType,width,height);
}

/**
 * This will change the size of the ImageData, without changing the type of the data
 * or the scan line type. It will expand the internal array if necessary to be
 * big enough to hold the image data. Data already stored will not be preserved.
 * @param width the new width of the ImageData
 * @param height the new height of the ImageData
 * @return this ArrayImageData
 * @throws IllegalArgumentException if the width or height are <= 0
 */
public ArrayImageData resizeTo(int width, int height) throws IllegalArgumentException
{
	if (width <= 0 || height <= 0) throw new IllegalArgumentException();
	this.width = width;
	this.height = height;
	if (byteValues == null && intValues == null) throw new IllegalStateException("You must call one of the set() methods before calling this method.");
	int type = ImageTool.toImageDataType(imageType);
	int scanType = byteValues != null ? SCAN_LINE_BYTE_ARRAY : SCAN_LINE_INT_ARRAY;
	arrayOffset = 0;
	scanLineLength = arrayScanLineLength = getScanLineLengthFor(type,scanType,width);
	int need = height*arrayScanLineLength;
	if (scanType == SCAN_LINE_BYTE_ARRAY){
		if (byteValues.length < need) byteValues = new byte[need];
	}else{
		if (intValues.length < need) intValues = new int[need];
	}
	return this;
}
/**
Create and return an array that can hold scan lines for an ImageData of a
specified type with specified width and height.
**/
//===================================================================
public static Object getArrayFor(int imageDataType, int width, int height)
//===================================================================
{
	return getArrayFor(imageDataType,getScanLineTypeFor(imageDataType),width,height);
}
/**
Get the scan line length needed for an array representation of a particular ImageData type
for a particular image width, using a scanLineType of either SCAN_LINE_INT_ARRAY or SCAN_LINE_BYTE_ARRAY.
Only TYPE_RGB and TYPE_ARGB can use both SCAN_LINE_INT_ARRAY or SCAN_LINE_BYTE_ARRAY, all other types must
use SCAN_LINE_BYTE_ARRAY.
<p>
This is the same as ImageTool.getScanLineLengthFor()
**/
//===================================================================
public static int getScanLineLengthFor(int imageDataType, int scanLineType, int width)
//===================================================================
{
	return ImageTool.getScanLineLengthFor(imageDataType, scanLineType, width);
}
/**
Get the scan line length needed for an array representation of a particular ImageData type
for a particular image width. This method assumes that images of type TYPE_RGB and TYPE_ARGB
use int arrays for scan lines and all others use byte arrays.
**/
//===================================================================
public static int getScanLineLengthFor(int imageDataType,int width)
//===================================================================
{
	return getScanLineLengthFor(imageDataType,getScanLineTypeFor(imageDataType),width);
}
//===================================================================
public ArrayImageData set(int imageDataType, int width, int height,Object array,int offset)
//===================================================================
{
	return set(imageDataType,width,height,array,offset,getScanLineLengthFor(imageDataType,array instanceof int[] ? SCAN_LINE_INT_ARRAY : SCAN_LINE_BYTE_ARRAY,width),null,true);
}
//===================================================================
public ArrayImageData set(int imageDataType, int width, int height,Object array, int offset, int arrayScanLineLength,int[] colorTable,boolean writable)
//===================================================================
{
	imageDataType = ImageTool.toImageDataType(imageDataType);
	intValues = null; byteValues = null;
	if (array instanceof int[]) intValues = (int[])array;
	else if (array instanceof byte[]) byteValues =(byte[])array;
	else throw new IllegalArgumentException();
	if (width <= 0 || height <= 0) throw new IllegalArgumentException();
	this.width = width;
	this.height = height;
	this.arrayOffset = offset;
	this.arrayScanLineLength = arrayScanLineLength;
	//Vm.debug(arrayScanLineLength+" for "+width);
	this.imageType = imageDataType;
	this.colorTable = colorTable;
	this.isWritable = writable;
	if (colorTable == null && ImageTool.usesColorTable(imageType))
		colorTable = ImageTool.makeDefaultColorTable(imageType);
	this.scanLineLength = getScanLineLengthFor(imageDataType,intValues == null ? SCAN_LINE_BYTE_ARRAY : SCAN_LINE_INT_ARRAY,width);
	return this;
}

//===================================================================
public int getImageType(){return imageType;}
public int getImageHeight(){return height;}
public int getImageWidth() {return width;}
public boolean isWritableImage() {return isWritable;}
public int[] getImageColorTable() {return colorTable;}
public int getImageScanLineType() {return intValues == null ? SCAN_LINE_BYTE_ARRAY : SCAN_LINE_INT_ARRAY;}
public int getImageScanLineLength(){return scanLineLength;}
//===================================================================
public void getImageScanLines(int startLine, int numLines, Object destination, int offset, int destScanLineLength)
//===================================================================
{
	if ((intValues != null) && !(destination instanceof int[])) throw new IllegalArgumentException();
	if ((byteValues != null) && !(destination instanceof byte[])) throw new IllegalArgumentException();
	int toCopy = destScanLineLength < scanLineLength ? destScanLineLength : scanLineLength;
	int s = startLine*arrayScanLineLength+arrayOffset;
	int d = offset;
	for (int i = 0; i<numLines; i++){
		System.arraycopy(intValues == null ? (Object)byteValues : (Object)intValues,s,destination,d,toCopy);
		s += arrayScanLineLength;
		d += destScanLineLength;
	}
}
//===================================================================
public void setImageScanLines(int startLine, int numLines, Object source, int offset, int sourceScanLineLength)
//===================================================================
{
	//try{
	if ((intValues != null) && !(source instanceof int[])) throw new IllegalArgumentException();
	if ((byteValues != null) && !(source instanceof byte[])) throw new IllegalArgumentException();
	int toCopy = sourceScanLineLength < scanLineLength ? sourceScanLineLength : scanLineLength;
	int s = startLine*arrayScanLineLength+arrayOffset;
	int d = offset;
	for (int i = 0; i<numLines; i++){
		System.arraycopy(source,d,intValues == null ? (Object)byteValues : (Object)intValues,s,toCopy);
		s += arrayScanLineLength;
		d += sourceScanLineLength;
	}
	/*
	}catch(Exception e){
		Vm.debug(startLine+", "+numLines+", "+offset+", "+sourceScanLineLength+", "+scanLineLength);
		Vm.debug(arrayScanLineLength+", "+width+" startLine+numLines"+(startLine+numLines)+", "+height);
		Vm.exit(0);
	}
	*/
}
//##################################################################
}
//##################################################################

