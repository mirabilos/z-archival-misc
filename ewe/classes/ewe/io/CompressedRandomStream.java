package ewe.io;
import ewe.sys.Handle;
import ewe.zip.DeflaterOutputStream;
import ewe.zip.InflaterInputStream;
import ewe.zip.ZipConstants;
import ewe.util.ByteArray;
import ewe.util.Utils;
import ewe.util.Vector;
import ewe.util.Tag;
/**
A CompressedRandomStream is a read-only RandomStream that has been compressed for storage, but
optimized for random access reading. This is unlike a stream that has been zipped where the
entire stream is zipped into one entire zipped file. A CompressedRandomStream is compressed
in blocks (of a size that you can select on creation) and a directory of the locations of each
block is maintained, allowing for quick location and decompression of specific bytes within
the file.<p>
Any file may be converted to a CompressedRandomStream, using the static compressStream() methods
or you can run the main() method of CompressedRandomStream to compress a File from the command line.
**/
//##################################################################
public class CompressedRandomStream extends RandomStream{
//##################################################################

/**
* These are the signature bytes of a CompressedRandomStream.
**/
public static final byte[] signature = {(byte)0xee,(byte)0x33,(byte)0xee,(byte)0x22,(byte)0x11,(byte)0xff,(byte)0xee,(byte)0xdd};

/**
 * Compress an InputStream into a CompressedRandomStream.
 * @param handle an optional handle that can be used to abort the process.
 * @param input the source stream.
 * @param out an output RandomStream.
 * @param blockSize the block size to use. A default of 1024 is used if this is zero.
 * @return true if the operation completed, false if it was aborted because stop() was called on the Handle.
 * @exception IOException if an IO error occured.
 */
//===================================================================
public static boolean compressStream(Handle handle, InputStream input, RandomStream out,
int blockSize)
throws IOException
//===================================================================
{
	return compressStream(handle,input,out,blockSize,ZipConstants.Z_DEFAULT_COMPRESSION,-1);
}
/**
 * Compress an InputStream into a CompressedRandomStream.
 * @param handle an optional handle that can be used to abort the process.
 * @param input the source stream.
 * @param out an output RandomStream.
 * @param blockSize the block size to use. A default of 1024 is used if this is zero.
 * @param compressionLevel one of the ZipConstant.Z_xxx_COMPRESSION values, or zero for default.
 * @param knownSize the size of the input stream if it is known, or -1 if it is not known.
 * @return true if the operation completed, false if it was aborted because stop() was called on the Handle.
 * @exception IOException if an IO error occured.
 */
//===================================================================
public static boolean compressStream(Handle handle, InputStream input, RandomStream out,
int blockSize, int compressionLevel, long knownSize)
throws IOException
//===================================================================
{
	if (compressionLevel == 0) compressionLevel = ZipConstants.Z_DEFAULT_COMPRESSION;
	if (blockSize < 256) blockSize = 1024;
	//
	// Write the signature.
	//
	out.seek(0);
	out.write(signature);
	//
	// Write the block size.
	//
	byte[] lb = new byte[8];
	byte[] ib = new byte[4];
	//
	Utils.writeLong((long)blockSize,lb,0);
	out.write(lb);
	//
	// Reserve 8 bytes for the offset of the directory. If it stays as zero then the
	// input file was of zero length.
	//
	Utils.writeLong(0L,lb,0);
	out.write(lb);
	//
	// Reserve an extra 8 bytes for the total length of the uncompressed file.
	//
	out.write(lb);
	//
	int firstOffset = 32;
	int blocks = 0;
	long curPos = firstOffset;
	int [] sizes = new int[16];
	byte[] inputBuffer = new byte[blockSize];
	ByteArray compressed = new ByteArray();
	//
	long totalRead = 0;
	while(handle == null || !handle.shouldStop){
		int toRead = blockSize, didRead = 0;
		//
		// Read in a full block.
		//
		while(toRead != 0){
			int r = input.read(inputBuffer,didRead,toRead);
			if (r == -1) break;
			toRead -= r;
			didRead += r;
		}
		//
		// Didn't read anything? Then finish.
		//
		if (didRead == 0) break;
		//
		// Compress this block.
		//
		totalRead += didRead;
		if (handle != null)
			if (knownSize <= 0) handle.setProgress(-1);
			else handle.setProgress((float)((double)knownSize/totalRead));
		//
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		DeflaterOutputStream dos = new DeflaterOutputStream(bos,compressionLevel,false);
		dos.write(inputBuffer,0,didRead);
		dos.close();
		bos.toByteArray(compressed);
		//
		// Write the compressed data.
		//
		out.write(compressed.data,0,compressed.length);
		//
		// Save the position of the compressed block and its length.
		//
		if (blocks == sizes.length){
			int[] nd = new int[sizes.length*2];
			System.arraycopy(sizes,0,nd,0,sizes.length);
			sizes = nd;
		}
		sizes[blocks++] = compressed.length;
		curPos += compressed.length;
	}
	//
	input.close();
	//
	// Now write the directory.
	//
	if (blocks != 0 && (handle == null || !handle.shouldStop)){
		//
		// Write out the starting offset.
		//
		Utils.writeLong(firstOffset,lb,0);
		out.write(lb);
		//
		// Write out the sizes of each compressed block.
		//
		for (int i = 0; i<blocks; i++){
			Utils.writeInt(sizes[i],ib,0,4);
			out.write(ib);
		}
		// Write the offset of the directory.
		// If blocks is zero then the offset stays as zero.
		//
		out.seek(16);
		Utils.writeLong(curPos,lb,0);
		out.write(lb);
		Utils.writeLong(totalRead,lb,0);
		out.write(lb);
	}
	//
	out.close();
	//
	if (handle == null) return true;
	return !handle.shouldStop;
}

//-------------------------------------------------------------------
private static boolean hasSignature(InputStream ras)
//-------------------------------------------------------------------
{
	try{
		byte[] r = new byte[signature.length];
		IO.readAll(ras,r,0,r.length);
		for (int i = 0; i<r.length; i++)
			if (r[i] != signature[i]) return false;
		return true;
	}catch(IOException e){
		return false;
	}
}
/**
 * Check if a File has the CompressedRandomStream signature.
 * @param input the input file.
 * @return true if the file has the CompressedRandomStream signature.
 * @exception IOException if there is an error reading from the file.
 */
//===================================================================
public static boolean isCompressedRandomStream(File input)
throws IOException
//===================================================================
{
	RandomAccessStream ras = input.toRandomAccessStream("r");
	try{
		return hasSignature(new InputStream(ras));
	}finally{
		ras.close();
	}
}
/**
 * Check if an InputStream has the CompressedRandomStream signature.
 * @param inputStream the InputStream.
 * @return true if the file has the CompressedRandomStream signature.
 * @exception IOException if there is an error reading from the file.
 */
public static boolean isCompressedRandomStream(InputStream inputStream) throws IOException
{
	return hasSignature(inputStream);
}
/**
 * Check if a Stream has the CompressedRandomStream signature.
 * @param stream the input Stream.
 * @return true if the file has the CompressedRandomStream signature.
 * @exception IOException if there is an error reading from the file.
 */
public static boolean isCompressedRandomStream(Stream stream) throws IOException
{
	return hasSignature(new InputStream(stream));
}
/**
 * Check if a RandomAccessStream has the CompressedRandomStream signature.
 * @param stream the input RandomAccessStream.
 * @return true if the file has the CompressedRandomStream signature.
 * @exception IOException if there is an error reading from the file.
 */
public static boolean isCompressedRandomStream(RandomAccessStream stream) throws IOException
{
	return hasSignature(new InputStream(stream));
}
/**
 * Check if a RandomStream has the CompressedRandomStream signature.
 * @param stream the input RandomStream.
 * @return true if the file has the CompressedRandomStream signature.
 * @exception IOException if there is an error reading from the file.
 */
public static boolean isCompressedRandomStream(RandomStream stream) throws IOException
{
	stream.seek(0);
	return hasSignature(new InputStream(stream));
}
protected RandomStream inputStream;
private long[] offsets;
private int[] sizes;
private int blockSize;
private int numBlocks;
private long totalSize;
private byte[] buff = new byte[8];
private byte[] inBuff = new byte[1024];

private long cacheSize  = 1024;
private Vector cache = new Vector();


/**
 * Set the number of bytes to cache in memory.
 * @param cacheSize the number of bytes to cache in memory.
 */
//===================================================================
public void setCacheSize(long cacheSize)
//===================================================================
{
	if (cacheSize < blockSize) cacheSize = blockSize;
	this.cacheSize = cacheSize;

}
/**
 * Get the number of bytes to cache in memory.
 */
//===================================================================
public long getCacheSize()
//===================================================================
{
	return cacheSize;
}
//-------------------------------------------------------------------
private void readFully(byte[] dest, int toRead) throws IOException
//-------------------------------------------------------------------
{
		int didRead = 0;
		while(toRead != 0){
			int r = inputStream.read(dest,didRead,toRead);
			if (r == -1) throw new EOFException();
			toRead -= r;
			didRead += r;
		}
}
//-------------------------------------------------------------------
private long readLong() throws IOException
//-------------------------------------------------------------------
{
	readFully(buff,8);
	return Utils.readLong(buff,0);
}
//-------------------------------------------------------------------
private int readInt() throws IOException
//-------------------------------------------------------------------
{
	readFully(buff,4);
	return Utils.readInt(buff,0,4);
}
//-------------------------------------------------------------------
private byte[] readBlock(int whichBlock) throws IOException
//-------------------------------------------------------------------
{
	int sz = sizes[whichBlock];
	if (inBuff.length < sz) inBuff = new byte[sz];
	inputStream.seek(offsets[whichBlock]);
	readFully(inBuff,sz);
	ByteArrayInputStream bis = new ByteArrayInputStream(inBuff,0,sz);
	InflaterInputStream ifs = new InflaterInputStream(bis);
	int expect = blockSize;
	if (whichBlock == numBlocks-1) expect = (int)(totalSize%blockSize);
	if (expect == 0) expect = blockSize;
	byte[] ret = new byte[expect];
	IO.readAll(ifs,ret);
	ifs.close();
	return ret;
}
//-------------------------------------------------------------------
private void limitCache()
//-------------------------------------------------------------------
{
	int allowedSize = (int)(cacheSize/blockSize);
	if (allowedSize < 1) allowedSize = 1;
	if (allowedSize < cache.size())
		cache.setSize(allowedSize);
}
//-------------------------------------------------------------------
private byte[] findBlock(int whichBlock) throws IOException
//-------------------------------------------------------------------
{
	for (int i = 0; i<cache.size(); i++){
		Tag t = (Tag)cache.get(i);
		if (t.tag == whichBlock){
			cache.del(i);
			cache.add(0,t);
			return (byte[])t.value;
		}
	}
	Tag t = new Tag();
	t.tag = whichBlock;
	t.value = readBlock(whichBlock);
	cache.add(0,t);
	limitCache();
	return (byte[])t.value;
}
/**
 * Create a CompressedRandomStream from a RandomAccessStream.
 * @param compressedFile the compressed file.
 * @exception IOException if there was an error reading the file.
 */
//===================================================================
public CompressedRandomStream(RandomAccessStream compressedFile)
throws IOException
//===================================================================
{
	this(new RandomStream(compressedFile));
}
/**
 * Create a CompressedRandomStream from a RandomStream.
 * @param compressedFile the compressed file.
 * @exception IOException if there was an error reading the file.
 */
//===================================================================
public CompressedRandomStream(RandomStream compressedFile)
throws IOException
//===================================================================
{
	this.inputStream = compressedFile;
	InputStream in = new InputStream(compressedFile);
	if (!hasSignature(new InputStream(in))) throw new IOException("Not a CompressedRandomStream");
	inputStream.seek(8);
	try{
		blockSize = (int)readLong();
		long dirPos = readLong();
		totalSize = readLong();
		numBlocks = (int)((totalSize+(blockSize-1))/blockSize);
		offsets = new long[numBlocks];
		sizes = new int[numBlocks];
		if (numBlocks != 0){
			inputStream.seek(dirPos);
			offsets[0] = readLong();
			sizes[0] = readInt();
			for (int i = 1; i<numBlocks; i++){
				offsets[i] += offsets[i-1]+sizes[i-1];
				sizes[i] = readInt();
			}
		}
		setCacheSize(blockSize*10);
	}catch(EOFException e){
		throw new IOException("Compressed File is corrupted.");
	}
}
/**
This method can be used to convert a File into a CompressedRandomStream. <p>
use the command line:<p>
<pre>
	Ewe ewe.io.CompressedRandomStream input_file output_file [block_size]
</pre>
<p>
The default block_size is 1024 bytes.
**/
//=================================================================
public static void main(String[] args) throws IOException, InterruptedException
//=================================================================
{
	ewe.sys.Vm.startEwe(args);
	if (args.length < 2) {
		ewe.sys.Vm.debug("Usage: ewe.io.CompressedRandomStream <inputfile> <outputfile> [<blocksize>]");
		ewe.sys.mThread.sleep(3000);
		ewe.sys.Vm.exit(-1);
	}
	if (args.length != 0){
		ewe.sys.Vm.debug("Starting....");
		File out = new File(args[1]);
		out.delete();
		int bs = 0;
		if (args.length > 2) bs = ewe.sys.Convert.toInt(args[2]);
		compressStream(null,new FileInputStream(args[0]),new RandomStream(new RandomAccessFile(out,"rw")),bs);
		ewe.sys.Vm.debug("Done!");
	}
	ewe.sys.Vm.exit(0);
}

private long curPos;
//===================================================================
public int read(byte buffer[],int start,int length) throws IOException
//===================================================================
{
	int whichBlock = (int)(curPos/blockSize);
	int expect = blockSize;
	if (whichBlock == numBlocks-1) {
		expect = (int)(totalSize%blockSize);
		if (expect == 0) expect = blockSize;
	}else if (whichBlock >= numBlocks) expect = 0;
	if (expect == 0) return -1;
	int left = expect-(int)(curPos%blockSize);
	if (left <= 0) return -1;
	if (left < length) length = left;
	int offset = (int)(curPos-(whichBlock*blockSize));
	byte[] get = findBlock(whichBlock);
	System.arraycopy(get,offset,buffer,start,length);
	curPos += length;
	return length;
}
//===================================================================
public int read() throws IOException
//===================================================================
{
	return readSingleByteFromMultiByteRead();
}
//===================================================================
public void close() throws IOException
//===================================================================
{
	closed = true;
	inputStream.close();
}
//===================================================================
public void seek(long position) throws IOException
//===================================================================
{
	checkClosed();
	if (position > totalSize) position = totalSize;
  if (position < 0) position = 0;
	curPos = position;
}
//===================================================================
public long tell() throws IOException
//===================================================================
{
	checkClosed();
	return curPos;
}
//===================================================================
public long length() throws IOException
//===================================================================
{
	checkClosed();
	return totalSize;
}
//===================================================================
public boolean canWrite()
//===================================================================
{
	return false;
}
//##################################################################
}
//##################################################################

