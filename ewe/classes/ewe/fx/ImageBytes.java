package ewe.fx;
import ewe.io.MemoryFile;
import ewe.io.IOException;
/**
* This provides no more functionality than a ByteArray. Its sole purpose
* is to allow for automatic creation of an ImageControl UI component
* when creating Editors for objects. Any variable of type ImageBytes gets
* an ImageControl created for it.
**/
//##################################################################
public class ImageBytes extends ewe.util.ByteArray{
//##################################################################
//===================================================================
public ImageBytes(){super();}
//===================================================================
public ImageBytes(byte [] data) {super(data);}
//===================================================================
public ImageBytes(IImage image) {pngEncode(image);}
//===================================================================

/**
* This version of getCopy() returns a new ImageBytes object.
**/
//===================================================================
public Object getCopy()
//===================================================================
{
	ImageBytes ib = new ImageBytes();
	ewe.util.ByteArray ba = (ewe.util.ByteArray)super.getCopy();
	ib.data = ba.data;
	ib.length = ba.length;
	return ib;
}
/**
This method will save the Image as a PNG image into this ImageBytes object, erasing
any data that may have been in it.
**/
//===================================================================
public void pngEncode(IImage image)
//===================================================================
{
	clear();
	MemoryFile mf = new MemoryFile(this,"rw");
	PNGEncoder pe = new PNGEncoder();
	try{
		pe.writeImage(mf,image);
		mf.close();
	}catch(IOException e){}
	if (length == data.length) return;
	byte[] d2 = new byte[length];
	if (length != 0) System.arraycopy(data,0,d2,0,length);
	data = d2;
}
/**
This method will decode the Image that was perviously saved via pngEnocde().
**/
//===================================================================
public mImage pngDecode()
//===================================================================
{
	return new mImage(new Image(this,0));
}
//##################################################################
}
//##################################################################

