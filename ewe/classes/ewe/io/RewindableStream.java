package ewe.io;

/**
* A RewindableStream adapts a BasicStream so that a particular section of
* the stream (i.e. a set number of bytes, starting from the first byte) can
* be accessed randomly, while the remaining bytes can only be accessed
* sequentially.<p>
*
* Such a Stream can be used when you need to decode data from an Input stream,
* but some pre-processing of some number of starting bytes is needed - for example
* in image decoding.<p>
*
* As data is read from the stream it is copied into a buffer as well as being
* returned for consumption. A rewind() operation then causes the stream to seek to
* the start of the stream, and you can subsequently seek() up to that point.<p>
**/

//##################################################################
public class RewindableStream extends RandomStreamObject{
//##################################################################

long curPos;
boolean rewound;
int inBuffer;
byte [] saved = new byte[0];

Stream stream;
RandomAccessStream ras;


//===================================================================
public static RandomAccessStream toRewindableStream(Stream stream)
//===================================================================
{
	if (!(stream instanceof RewindableStream) && (stream instanceof RandomAccessStream)) return (RandomAccessStream)stream;
	else return new RewindableStream(stream);
}

//===================================================================
public static void rewind(RandomAccessStream stream) throws ewe.io.IOException
//===================================================================
{
	if (stream instanceof RewindableStream) ((RewindableStream)stream).rewind();
	else stream.seek((long)0);
}
//===================================================================
public boolean canWrite()
//===================================================================
{
	return false;
}
//===================================================================
public RewindableStream(Stream stream)
//===================================================================
{
	this.stream = stream;
	if (!(stream instanceof RewindableStream) && (stream instanceof RandomAccessStream)) ras = (RandomAccessStream)stream;
}
//===================================================================
public boolean flushStream() throws ewe.io.IOException
//===================================================================
{
	return stream.flushStream();
}
//===================================================================
public boolean closeStream() throws ewe.io.IOException
//===================================================================
{
	return stream.closeStream();
}
//===================================================================
public boolean isOpen()
//===================================================================
{
	return stream.isOpen();
}
//===================================================================
public int nonBlockingRead(byte [] buffer,int start,int length)
//===================================================================
{
	//if (length != 1024) ewe.sys.Vm.debug("Reading: "+length+": "+ras);
	if (ras != null) return ras.nonBlockingRead(buffer,start,length);
	if (rewound){
		if (curPos >= inBuffer){
			int readIn = stream.nonBlockingRead(buffer,start,length);
			if (readIn <= 0) return readIn;
			curPos += readIn;
			return readIn;
		}
	}
	if (curPos < inBuffer){
		int buffered = (int)(inBuffer-curPos);
		if (buffered < length) length = buffered;
		ewe.sys.Vm.copyArray(saved,(int)curPos,buffer,start,length);
		curPos += length;
		return length;
	}
	int readIn = stream.nonBlockingRead(buffer,start,length);
	if (readIn <= 0) return readIn;
	if (curPos+readIn > (long)saved.length){
		int extra = (int)(curPos+readIn-saved.length);
		if (extra < 1024) extra = 1024;
		byte [] nb = new byte[saved.length+extra];
		ewe.sys.Vm.copyArray(saved,0,nb,0,inBuffer);
		saved = nb;
	}
	ewe.sys.Vm.copyArray(buffer,start,saved,inBuffer,readIn);
	curPos += readIn;
	inBuffer += readIn;
	return readIn;
}
//===================================================================
public int nonBlockingWrite(byte [] buffer,int start,int length)
//===================================================================
{
	error = "Cannot write to this Stream";
	return -2;
}
private byte [] skipBuffer;

//-------------------------------------------------------------------
private void throwAccessError() throws IOException
//-------------------------------------------------------------------
{
	ewe.sys.Vm.debug("Access error - "+inBuffer);
	throw new IOException("Access out of rewind range.");
}
/**
 * Tell the Stream to move to the specific position. This is a non-blocking call.
 * @param pos The position to seek to.
 * @return true if the seek completed successfully, false if the seek did not complete yet.
 * @exception IOException if an error occured during the seek.
 */
//===================================================================
public boolean seekPosition(long pos) throws IOException
//===================================================================
{
	//if (pos != curPos) ewe.sys.Vm.debug("seek("+pos+"), "+curPos);
	if (pos < curPos && rewound){
		//String st = ewe.sys.Vm.getStackTrace(new Exception(),5);
		//ewe.sys.Vm.debug("Rewinding: "+st);
	}
	if (ras != null) return ras.seekPosition(pos);
	if (pos == curPos) return true;
	else if (pos < curPos){
		if (curPos > inBuffer) {
			throwAccessError();
			return false;
		}
		curPos = pos;
		return true;
	}else{
		if (skipBuffer == null) skipBuffer = new byte[1024];
		int needToSkip = (int)(pos-curPos);
		//ewe.sys.Vm.debug("Must skip: "+needToSkip+", "+curPos);
		while(needToSkip != 0){
			int read = needToSkip < skipBuffer.length ? needToSkip : skipBuffer.length;
			ewe.io.IO.readAll(this,skipBuffer,0,read);
			needToSkip -= read;
			//curPos += read; <= This is done by readFully.
		}
		return true;
	}
}
/**
 * Retrieve the file position. This is non-blocking
 * @return the position of the stream or -1 if the position is not known yet.
 * @exception IOException if an error occured while getting the position.
 */
//===================================================================
public long tellPosition() throws IOException
//===================================================================
{
	//ewe.sys.Vm.debug("tell()");
	if (ras != null) {
		long pos = ras.tellPosition();
		return pos;
	}
	//if (curPos <= inBuffer)
	return curPos;
	//throw new IOException("Access out of rewind range.");
}

//===================================================================
public void rewind() throws IOException
//===================================================================
{
	//ewe.sys.Vm.debug("Rewinding at: "+curPos);
	rewound = true;
	seek(0);
}
//##################################################################
}
//##################################################################

