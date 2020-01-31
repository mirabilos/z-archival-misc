package ewe.fx;

/**
This is a Interface that provides image scan line data and can optionally
accept image scan line data.<p>
An ewe.fx.Image class implements this interface so you can access its image data directly.
**/
//##################################################################
public interface ImageData{
//##################################################################

public static final int IS_AN_IMAGE_DATA_TYPE = 0x40000000;
/** An image type - the type could not be determined **/
public static final int TYPE_UNKNOWN = 0;
/** An image type - one bit per pixel. **/
public static final int TYPE_MONO = IS_AN_IMAGE_DATA_TYPE|Image.GRAY_SCALE_2_IMAGE;
/** An image type - one bit per pixel - the same as TYPE_MONO.**/
public static final int TYPE_GRAY_SCALE_2 = TYPE_MONO;
/** An image type - one byte per pixel gray scale. **/
public static final int TYPE_GRAY_SCALE_256 = IS_AN_IMAGE_DATA_TYPE|Image.GRAY_SCALE_256_IMAGE;
/** An image type - 4 bits per pixel gray scale. **/
public static final int TYPE_GRAY_SCALE_16 = IS_AN_IMAGE_DATA_TYPE|Image.GRAY_SCALE_16_IMAGE;
/** An image type - 2 bits per pixel gray scale. **/
public static final int TYPE_GRAY_SCALE_4 = IS_AN_IMAGE_DATA_TYPE|Image.GRAY_SCALE_4_IMAGE;

//public static final int FIRST_GRAY = TYPE_GRAY_SCALE_2;
//public static final int LAST_GRAY = TYPE_GRAY_SCALE_256;

/** An image type - one byte per pixel indexed color. **/
public static final int TYPE_INDEXED_2 = IS_AN_IMAGE_DATA_TYPE|Image.INDEXED_2_IMAGE;
/** An image type - one byte per pixel indexed color. **/
public static final int TYPE_INDEXED_4 = IS_AN_IMAGE_DATA_TYPE|Image.INDEXED_4_IMAGE;
/** An image type - 4 bits per pixel indexed color. **/
public static final int TYPE_INDEXED_16 = IS_AN_IMAGE_DATA_TYPE|Image.INDEXED_16_IMAGE;
/** An image type - 2 bits per pixel indexed color. **/
public static final int TYPE_INDEXED_256 = IS_AN_IMAGE_DATA_TYPE|Image.INDEXED_256_IMAGE;

//public static final int FIRST_INDEX = TYPE_INDEXED_2;
//public static final int LAST_INDEX = TYPE_INDEXED_256;

/** An image type - one byte per color component, three bytes per pixel. **/
public static final int TYPE_RGB = IS_AN_IMAGE_DATA_TYPE|Image.RGB_IMAGE;
/** An image type - one byte per color component, four bytes per pixel. **/
public static final int TYPE_ARGB = IS_AN_IMAGE_DATA_TYPE|Image.ARGB_IMAGE;

/** A Scan line type that is always used by TYPE_MONO and TYPE_GRAY_SCALE_XXX. MAY be used
by TYPE_RGB and TYPE_ARGB **/
public static final int SCAN_LINE_BYTE_ARRAY = 1;
/** A Scan line type that may be used by TYPE_RGB and TYPE_ARGB **/
public static final int SCAN_LINE_INT_ARRAY = 2;

/**
This returns one of the TYPE_XXX values
**/
//===================================================================
public int getImageType();
//===================================================================
/**
Get the type of scan line used by the image - either SCAN_LINE_BYTE_ARRAY or SCAN_LINE_INT_ARRAY
**/
//===================================================================
public int getImageScanLineType();
//===================================================================
/**
If the scan line type is SCAN_LINE_BYTE_ARRAY then this indicates
the number of bytes is needed for one complete scan line.
**/
//===================================================================
public int getImageScanLineLength();
//===================================================================
/**
Place a set of scan lines into a destination array.
The type of the destination array must be either byte[] or int[] depending on the value
of getImageScanLineType().
**/
//===================================================================
public void getImageScanLines(int startLine, int numLines, Object destArray, int offset, int destScanLineLength);
//===================================================================
/**
Place a set of scan lines from a source Array into the ImageData.
The type of the destination array must be either byte[] or int[] depending on the value
of getImageScanLineType().
**/
//===================================================================
public void setImageScanLines(int startLine, int numLines, Object sourceArray, int offset, int sourceScanLineLength)
throws IllegalStateException;
//===================================================================

//===================================================================
public int getImageWidth();
//===================================================================

//===================================================================
public int getImageHeight();
//===================================================================
/**
Open the source to retrieve or optionally set scan lines.
**/
//===================================================================
//public boolean openImageData(boolean forWriting);
//===================================================================
/**
Close the source when you have finished retrieving scan lines.
**/
//===================================================================
//public boolean closeImageData();
//===================================================================
/**
For indexed images, this retrieves the color table as an array of ARGB integers.
If the image type is not TYPE_INDEXED_XXX, then null will be returned.
**/
//===================================================================
public int[] getImageColorTable();
//===================================================================
/**
Returns if you can write data to the Image.
**/
//===================================================================
public boolean isWritableImage();
//===================================================================

//##################################################################
}
//##################################################################

