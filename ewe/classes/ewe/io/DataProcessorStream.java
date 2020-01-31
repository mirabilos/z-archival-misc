/*********************************************************************************
 *  Ewe Virtual Machine - Version 1.14, January 24, 2002                         *
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
package ewe.io;
import ewe.util.ByteArray;
//import ewe.sys.Vm;
/**
* This is a ONE-WAY stream. Which way is up to you - depending on whether
* you write data to it or read data from it. You MUST ensure that you flush (for output)
* or close the stream after you have finished sending/receiving all the data to ensure
* that data processing is fully complete.
*
* This does NOT call closeProcess on the DataProcessor.
**/
//##################################################################
public class DataProcessorStream extends StreamAdapter implements OverridesClose{
//##################################################################
protected int blockSize = 0;
/**
* In the case of block sizes greater than 1, it may be necessary to pad
* the final set of data before processing. The padByte value is used to
* determine the byte value that will be used for this padding.
**/
public byte padByte = 0;
public DataProcessor processor;

ByteArray pendingOutput;
ByteArray pendingProcessing;
ByteArray pendingInput;

//===================================================================
public DataProcessorStream(DataProcessor processor,BasicStream stream)
//===================================================================
{
	this(processor,stream,1024);
}
//===================================================================
public DataProcessorStream(DataProcessor processor,BasicStream stream,int bufferSize)
//===================================================================
{
	super(stream);
	this.processor = processor;
	pendingProcessing = new ByteArray();
	int bs = processor.getBlockSize();
	if (bs <= 0) pendingProcessing.data = new byte[bufferSize];
	else {
		blockSize = bs;
		int max = processor.getMaxBlockSize();
		if (max > 0) if (bufferSize > max) bufferSize = max;
		int numBlocks = bufferSize/blockSize;
		if (numBlocks < 1) numBlocks = 1;
		pendingProcessing.data = new byte[numBlocks*blockSize];
	}
}

//-------------------------------------------------------------------
protected int cantWrite()
//-------------------------------------------------------------------
{
	stream.close();
	closed = true;
	error = "No more data can be written to destination stream.";
	return READWRITE_ERROR;
}
//-------------------------------------------------------------------
protected int cantProcess(Exception e)
//-------------------------------------------------------------------
{
	stream.close();
	closed = true;
	error = e == null ? "The DataProcessor reported an error." : e.getMessage();
	return READWRITE_ERROR;
}

//-------------------------------------------------------------------
protected int writeSomeOutput()
//-------------------------------------------------------------------
{
	if (pendingOutput == null) return -1;
	if (pendingOutput.length == 0) return -1;
	int wrote = stream.nonBlockingWrite(pendingOutput.data,0,pendingOutput.length);
	if (wrote < 0) return cantWrite();
	if (wrote != 0){
		pendingOutput.length -= wrote;
		if (pendingOutput.length != 0)
			ewe.sys.Vm.copyArray(pendingOutput.data,wrote,pendingOutput.data,0,pendingOutput.length);
	}
	return wrote;
}

//-------------------------------------------------------------------
protected boolean clearOutput()
//-------------------------------------------------------------------
{
	while(true){
		int wrote = writeSomeOutput();
		if (wrote == -1) return true;
		if (wrote < 0) return false;
		nap();
	}
}
//===================================================================
public int nonBlockingWrite(byte [] data,int offset,int length)
//===================================================================
{
	if (closed) return READWRITE_CLOSED;
	int wrote = writeSomeOutput();
	if (wrote != -1){// -1 indicates nothing to write, -2 indicates error.
		if (wrote < 0) return wrote;
		return 0;
	}
	// Don't accept any of this data unless you are finished sending off the old processed data.
	if (pendingOutput != null)
		if (pendingOutput.length != 0) return 0;

	int needToProcess = pendingProcessing.data.length-pendingProcessing.length;
	if (needToProcess > length) needToProcess = length;
	if (needToProcess != 0)
		ewe.sys.Vm.copyArray(data,offset,pendingProcessing.data,pendingProcessing.length,needToProcess);
	pendingProcessing.length += needToProcess;
	//......................................................
	// Now process what you can.
	//......................................................
	int excess = blockSize != 0 ? pendingProcessing.length % blockSize : 0;
	int toSend = pendingProcessing.length - excess;
	if (toSend != 0){
		try{
			pendingOutput = processor.processBlock(pendingProcessing.data,0,toSend,false,pendingOutput);
			pendingProcessing.length -= toSend;
			if (pendingProcessing.length != 0)
					ewe.sys.Vm.copyArray(pendingProcessing.data,toSend,pendingProcessing.data,0,pendingProcessing.length);
		}catch(IOException e){
			return cantProcess(e);
		}
	}
	wrote = writeSomeOutput();
	if (wrote != -1)// -1 indicates nothing to write, -2 indicates error.
		if (wrote < 0) return wrote;
	return needToProcess;
}
boolean hasEnded = false;
//===================================================================
public int nonBlockingRead(byte [] data,int offset,int length)
//===================================================================
{
	if (closed) return READWRITE_CLOSED;
	if (pendingInput != null){
		if (pendingInput.length != 0){
			if (pendingInput.length < length) length = pendingInput.length;
			ewe.sys.Vm.copyArray(pendingInput.data,0,data,offset,length);
			pendingInput.length -= length;
			if (pendingInput.length != 0)
				ewe.sys.Vm.copyArray(pendingInput.data,length,pendingInput.data,0,pendingInput.length);
			return length;
		}
	}
	if (hasEnded){
		return READWRITE_CLOSED;
	}
	//......................................................
	// Read in any data from the stream, trying to fill the buffer.
	//......................................................
	int needToProcess = pendingProcessing.data.length-pendingProcessing.length;
	if (needToProcess != 0){
		int got = stream.nonBlockingRead(pendingProcessing.data,pendingProcessing.length,needToProcess);
		if (got < 0) {
			hasEnded = true;
			stream.close();
			if (got == READWRITE_ERROR){
				closed = true;
				error = "Source stream reported an error.";
				return READWRITE_ERROR;
			}
		}else pendingProcessing.length += got;
	}
	//......................................................
	// Now process what you can.
	//......................................................
	int excess = blockSize != 0 ? pendingProcessing.length % blockSize : 0;
	if (hasEnded) { //Must pad the data if the underlying stream has ended.
		if (excess != 0){
			excess = blockSize-excess;
			for (int i = 0; i<excess; i++)
				pendingProcessing.data[pendingProcessing.length+i] = padByte;
			pendingProcessing.length += excess;
			excess = 0;
		}
		//If it has ended then excess will always be zero.
	}
	int toSend = pendingProcessing.length - excess;
	if (toSend != 0 || hasEnded){
		try{
			pendingInput = processor.processBlock(pendingProcessing.data,0,toSend,hasEnded,pendingInput);
			pendingProcessing.length -= toSend;
			if (pendingProcessing.length != 0)
					ewe.sys.Vm.copyArray(pendingProcessing.data,toSend,pendingProcessing.data,0,pendingProcessing.length);
		}catch(IOException e){
			return cantProcess(e);
		}
	}
	return 0;
}

//-------------------------------------------------------------------
protected boolean finalWrite()
//-------------------------------------------------------------------
{
	if (closed) return true;
	if (!clearOutput()) return false;
	if (blockSize != 0){
		int needToProcess = pendingProcessing.length % blockSize;
		if (needToProcess != 0) needToProcess = blockSize-needToProcess;
		int st = pendingProcessing.length;
		for (int i = 0; i<needToProcess; i++)
			pendingProcessing.data[i+st] = padByte;
		pendingProcessing.length += needToProcess;
	}
	try{
		pendingOutput = processor.processBlock(pendingProcessing.data,0,pendingProcessing.length,true,pendingOutput);
		pendingProcessing.length = 0;
	}catch(IOException e){
		cantProcess(e);
		return false;
	}
	if (!clearOutput()) return false;
	return true;
}

//===================================================================
public boolean closeStream() throws IOException
//===================================================================
{
	if (!closed){
		if (pendingOutput != null) finalWrite();
		try{
			flush();
		}catch(Exception e){
			return false;
		}finally{
			closed = true;
		}
		if (!stream.close()) throw new IOException();
	}
	return true;
}
//##################################################################
}
//##################################################################

