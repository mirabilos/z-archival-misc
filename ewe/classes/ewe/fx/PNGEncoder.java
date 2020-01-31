package ewe.fx;
import ewe.io.*;
import ewe.zip.*;
/**
* This class can be used to save a PNG encoded image. You can use a single method,
* writeImage() to save an existing image to a stream. You can also use it to save
* more data by calling the individual methods in the correct sequence.<p>
*
* In general you will call one of the start() methods, then writeHeader() to write
* the important information. You can then call the writeChunk() methods to write
* any extra chunks you want. Then call startDataChunk() to begin sending data.
* You can then call writeScanLine() to send out as many image scan lines as you want. Then call
* endDataChunk() to end and write a data chunk.<p>
* When you have written all the data chunks you want (you can send a single data chunk if you wish - but
* this takes up more memory) you then call writeEnd() to write the end of the PNG image and then
* closeOutput() if you want to close the stream as well.
**/
//##################################################################
public class PNGEncoder{
//##################################################################

public static final int TYPE_GRAY_SCALE_SCALE = 0;
public static final int TYPE_PALETTE = 3;
public static final int TYPE_GRAY_SCALE_SCALE_ALPHA = 4;
public static final int TYPE_TRUE_COLOR = 2;
public static final int TYPE_TRUE_COLOR_ALPHA = 6;

private int myType;

//-------------------------------------------------------------------
protected boolean usingAlpha()
//-------------------------------------------------------------------
{
	return myType == TYPE_GRAY_SCALE_SCALE_ALPHA || myType == TYPE_TRUE_COLOR_ALPHA;
}
//-------------------------------------------------------------------
protected void writeInt(int value,OutputStream out,CRC32 crc) throws IOException
//-------------------------------------------------------------------
{
	byte [] t = {(byte)((value >> 24)&0xff),(byte)((value >> 16)&0xff),(byte)((value >> 8)&0xff),(byte)((value)&0xff)};
	if (crc != null) crc.update(t);
	out.write(t);
}
//-------------------------------------------------------------------
protected void writeByte(int value,OutputStream out,CRC32 crc) throws IOException
//-------------------------------------------------------------------
{
	if (crc != null) crc.update(value);
	out.write(value);
}
/**
 * Write a PNG chunk to the image.
 * @param value The 4-character code for the chunk.
 * @param bytes The bytes in the chunk to write (not including the length). This can be null, indicating no data.
 * @exception IOException if an error occurs writing to the Stream.
 */
//===================================================================
public void writeChunk(String value,byte [] bytes) throws IOException
//===================================================================
{
	int len = bytes == null ? 0 : bytes.length;
	writeChunk(value,bytes,0,len);
}

/**
 * Write a PNG chunk to the image.
 * @param value The 4-character code for the chunk.
 * @param bytes The bytes in the chunk to write (not including the length).
	This can be null, indicating no data, in which case length must also be 0.
 * @param offset The start location in the bytes.
 * @param length The number of bytes.
 * @exception IOException if an error occurs writing to the Stream.
 */
//===================================================================
public void writeChunk(String value,byte [] bytes,int offset,int length) throws IOException
//===================================================================
{
	if (value.length() != 4) throw new RuntimeException("The size of chunk:\""+value+"\" is incorrect.");
	writeInt(length,output,null);
	CRC32 crc = new CRC32(); crc.reset();
	byte [] t = {(byte)(value.charAt(0) & 0xff),(byte)(value.charAt(1) & 0xff),(byte)(value.charAt(2) & 0xff),(byte)(value.charAt(3) & 0xff)};
	output.write(t);
	crc.update(t);
	if (length > 0) output.write(bytes,offset,length);
	if (length > 0) crc.update(bytes,offset,length);
	writeInt((int)crc.getValue(),output,null);
}

protected OutputStream output;
protected static int [] signature = {137,80,78,71,13,10,26,10};

/**
* This is the very basic start method. It simply writes the PNG signature and
* nothing else.
**/
//===================================================================
public void startOutput(Stream os) throws IOException
//===================================================================
{
	startOutput(new OutputStream(os));
}
//===================================================================
public void startOutput(OutputStream out) throws IOException
//===================================================================
{
	output = out;
	for (int i = 0; i<signature.length; i++) output.write(signature[i]);
}
/**
* This writes the IHDR chunk, always using standard Deflator compression.
**/
//===================================================================
public void writeHeader(int width,int height,int bitDepth,int colorType)
//===================================================================
throws IOException
{
	ByteArrayOutputStream os = new ByteArrayOutputStream();
	writeInt(width,os,null);
	writeInt(height,os,null);
	writeByte(bitDepth,os,null);
	writeByte(colorType,os,null);
	for (int i = 0; i<3; i++) writeByte(0,os,null);
	writeChunk("IHDR",os.toByteArray());
	myType = colorType;
}
/**
* This writes the end chunk marking the end of the PNG file. It does not
* close the stream.
**/
//===================================================================
public void writeEnd() throws IOException
//===================================================================
{
	writeChunk("IEND",null);
}
protected ByteArrayOutputStream curStream;
protected DeflaterOutputStream dos;
/**
* This starts a chunk of scan line data using the default compression.
**/
//===================================================================
public void startDataChunk() throws IOException
//===================================================================
{startDataChunk(ZipConstants.Z_DEFAULT_COMPRESSION );}
/**
* This starts a chunk of scan line data using the specified compression leve.
**/
//===================================================================
public void startDataChunk(int compressionLevel) throws IOException
//===================================================================
{
	curStream = new ByteArrayOutputStream();
	dos = new DeflaterOutputStream(curStream,compressionLevel,false);
}
/**
* Use this to write scan line data. This need not be a full line.
**/
//===================================================================
public void writeScanLine(byte [] line,int offset,int length) throws IOException
//===================================================================
{
	dos.write(0);
	dos.write(line,offset,length);
}
/**
* Closes the current data chunk and writes the compressed data for the current chunk.
**/
//===================================================================
public void endDataChunk() throws IOException
//===================================================================
{
	dos.close();
	byte [] out = curStream.toByteArray();
	writeChunk("IDAT",curStream.toByteArray());
}
/**
* Closes the underlying output stream. Make sure you end any open data chunks and
* call writeEnd() first.
**/
//===================================================================
public void closeOutput() throws IOException
//===================================================================
{
	output.close();
}
/**
* This starts a true color PNG.
* This writes the signature and header, starts the data chunks and is
* ready for scan lines.
**/
//===================================================================
public void start(Stream out,int width,int height,boolean useAlpha) throws IOException
//===================================================================
{
	start(out,width,height,8,useAlpha ? TYPE_TRUE_COLOR_ALPHA : TYPE_TRUE_COLOR);
}
/**
 * This writes the signature and header, starts the data chunks and is
 * ready for scan lines.
 */
//===================================================================
public void start(Stream out,int width,int height,int bitDepth,int type) throws IOException
//===================================================================
{
	startOutput(out);
	writeHeader(width,height,bitDepth,type);
	startDataChunk();
}
/**
 * This ends the current data chunk, writes the end and closes the output.
 */
//===================================================================
public void end() throws IOException
//===================================================================
{
	endDataChunk();
	writeEnd();
	closeOutput();
}
/**
 * Start the PNG file in preparation for output of an image. The header will be written
 * to the file but no data is written. At this point you can write optional chunks using
 * writeChunk() and then you can use writeImage(IImage image) to write the image data, after
 * which you must use writeEnd() and closeOutput() to end the file.
 * @param out The output Stream. To write to a byte array use a new MemoryFile as the Stream.
 * @param image The image to write.
 * @exception IOException If there is an error writing the data.
 * @exception IllegalArgumentException If there is an error with the image.
 */
//===================================================================
public void startImage(Stream out,IImage image) throws IOException, IllegalArgumentException
//===================================================================
{
	writeImage(out,image,image.usesAlpha());
}
/**
 * Start the PNG file in preparation for output of an image. The header will be written
 * to the file but no data is written. At this point you can write optional chunks using
 * writeChunk() and then you can use writeImage(IImage image) to write the image data, after
 * which you must use writeEnd() and closeOutput() to end the file.
 * @param out The output Stream. To write to a byte array use a new MemoryFile as the Stream.
 * @param image The image to write.
 * @param usesAlpha if this is true then an ALPHA channel will be saved in the image.
 * @exception IOException If there is an error writing the data.
 * @exception IllegalArgumentException If there is an error with the image.
**/
//===================================================================
public void startImage(Stream out,IImage image,boolean useAlpha) throws IOException, IllegalArgumentException
//===================================================================
{
	startOutput(out);
	writeHeader(image.getWidth(),image.getHeight(),8,useAlpha ? TYPE_TRUE_COLOR_ALPHA : TYPE_TRUE_COLOR);
}
/**
 * Use this after startImage() to write the actual image data. After calling this you can
 * write additional chunks using writeChunk(). When complete you must call writeEnd() and closeOutput().
 * @param image The image to write.
 * @exception IOException If there is an error writing the data.
 * @exception IllegalArgumentException If there is an error with the image.
**/
//===================================================================
public void writeImage(IImage image) throws IOException, IllegalArgumentException
//===================================================================
{
	int w = image.getWidth(), ht = image.getHeight();
	startDataChunk();
	int [] line = new int[w];
	boolean useAlpha = usingAlpha();
	int factor = (useAlpha ? 4 : 3);
	byte [] bytes = new byte[w*factor];
	for (int h = 0; h<ht; h++){
		image.getPixels(line,0,0,h,w,1,0);
		for (int x = 0,i = 0; x<w; x++){
			bytes[i++] = (byte)((line[x] >> 16) & 0xff);
			bytes[i++] = (byte)((line[x] >> 8) & 0xff);
			bytes[i++] = (byte)((line[x] >> 0) & 0xff);
			if (useAlpha)
				bytes[i++] = (byte)((line[x] >> 24) & 0xff);
		}
		writeScanLine(bytes,0,bytes.length);
	}
	endDataChunk();
}

/**
 * Write out an entire Image to the stream as a complete PNG image, as a TRUE_COLOR or TRUE_COLOR_ALPHA image.
* It will <b>not</b> close the output stream.
 * @param out The output Stream. To write to a byte array use a new MemoryFile as the Stream.
 * @param image The image to write.
 * @exception IOException If there is an error writing the data.
 * @exception IllegalArgumentException If there is an error with the image.
 */
//===================================================================
public void writeImage(Stream out,IImage image) throws IOException, IllegalArgumentException
//===================================================================
{
	writeImage(out,image,image.usesAlpha());
}
/**
* This method will write out an entire image to the stream as a complete PNG image, as a TRUE_COLOR or TRUE_COLOR_ALPHA image.
* It will <b>not</b> close the output stream.
 * @param out The output Stream. To write to a byte array use a new MemoryFile as the Stream.
 * @param image The image to write.
 * @param usesAlpha if this is true then an ALPHA channel will be saved in the image.
 * @exception IOException If there is an error writing the data.
 * @exception IllegalArgumentException If there is an error with the image.
**/
//===================================================================
public void writeImage(Stream out,IImage image,boolean useAlpha) throws IOException, IllegalArgumentException
//===================================================================
{
	int w = image.getWidth();
	start(out,image.getWidth(),image.getHeight(),useAlpha);
	int [] line = new int[w];
	int factor = (useAlpha ? 4 : 3);
	byte [] bytes = new byte[w*factor];
	for (int h = 0; h<image.getHeight(); h++){
		image.getPixels(line,0,0,h,w,1,0);
		for (int x = 0,i = 0; x<w; x++){
			bytes[i++] = (byte)((line[x] >> 16) & 0xff);
			bytes[i++] = (byte)((line[x] >> 8) & 0xff);
			bytes[i++] = (byte)((line[x] >> 0) & 0xff);
			if (useAlpha)
				bytes[i++] = (byte)((line[x] >> 24) & 0xff);
		}
		writeScanLine(bytes,0,bytes.length);
	}
	endDataChunk();
	writeEnd();
}
/*
//===================================================================
public static void main(String args[]) throws IOException
//===================================================================
{
	PNGEncoder pe = new PNGEncoder();
	pe.startOutput(new FileOutputStream(args[0]));
	pe.writeHeader(160,160,4,0);
	byte [] line = new byte[80];
	for (int i = 0; i<16; i++){
		int value = i | ((i << 4) & 0xf0);
		for (int j = 0; j<5; j++) line[i*5+j] = (byte)(value & 0xff);
	}
	pe.startDataChunk();
	for (int h = 0; h<160; h++) pe.writeScanLine(line,0,line.length);
	pe.endDataChunk();
	pe.writeEnd();
	pe.closeOutput();
}
*/
//##################################################################
}
//##################################################################

