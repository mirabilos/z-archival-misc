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
import ewe.sys.*;
import ewe.util.ByteArray;
import ewe.util.CharArray;
import ewex.registry.*;
//##################################################################
public class IO{
//##################################################################
/**
This method attempts to read <b>count</b> bytes from the stream and will not return
until either all count bytes have been read or the end of the stream has been reached.
If mustReadAll is true, then failing to read all count bytes (because the stream ends
before reaching count bytes) will result in an IOException being thrown.<p>
Use this method when IO performance is a priority.
* @param s the input Stream.
* @param dest the destination array to hold the incoming bytes.
* @param start the offset within the destination array.
* @param count the number of bytes to read (this must be > 0).
* @param useFastStream if this is true AND the Stream implements FastStream, then
* the quickRead() method will be used to read the data.
* @param mustReadAll if this is true then an IOException will be thrown if a total of count bytes
* is not read in.
* @return The actual number of bytes read in, or -1 if the end of the stream has been reached.
* @exception IOException if an error occurs.
*/
//===================================================================
public static int read(Stream s,byte[] dest,int start,int count,boolean useFastStream,boolean mustReadAll)
throws IOException
//===================================================================
{
	if (count <= 0) return 0;
	FastStream fs = useFastStream && s instanceof FastStream ? (FastStream)s : null;
	int did = 0;
	while(count > 0){
		int ret = fs == null ? s.read(dest,start,count) : fs.quickRead(dest,start,count,false);
		if (ret <= 0){
			if (mustReadAll) throw new IOException("Unexpected end of stream");
			break;
		}
		did += ret;
		start += ret;
		count -= ret;
	}
	return did == 0 ? -1 : did;
}
/**
This method attempts to read <b>count</b> bytes from the stream and will not return
until either all count bytes have been read or the end of the stream has been reached.
If mustReadAll is true, then failing to read all count bytes (because the stream ends
before reaching count bytes) will result in an IOException being thrown.
Use this method when IO performance is a priority.
* @param s the InputStream to read from.
* @param dest the destination array to hold the incoming bytes.
* @param start the offset within the destination array.
* @param count the number of bytes to read (this must be > 0).
* @param useFastStream if this is true AND the Stream implements FastStream, then
* the quickRead() method will be used to read the data.
* @param mustReadAll if this is true then an IOException will be thrown if a total of count bytes
* is not read in.
* @return The actual number of bytes read in, or -1 if the end of the stream has been reached.
* @exception IOException if an error occurs.
*/
//===================================================================
public static int read(InputStream s,byte[] dest,int start,int count,boolean useFastStream,boolean mustReadAll)
throws IOException
//===================================================================
{
	if (count <= 0) return 0;
	FastStream fs = s.getFastStream();
	int did = 0;
	while(count > 0){
		int ret = fs == null ? s.read(dest,start,count) : fs.quickRead(dest,start,count,false);
		if (ret <= 0){
			if (mustReadAll) throw new IOException("Unexpected end of stream");
			break;
		}
		did += ret;
		start += ret;
		count -= ret;
	}
	return did == 0 ? -1 : did;
}

/**
 * Write out all the bytes to a Stream, optionally using the FastStream interface if available.
 * @param s the Stream to write to.
 * @param src the source data bytes to write.
 * @param start the offset within the src array.
 * @param count the number of bytes to write.
 * @param useFastStream if this is true and the Stream implements FastStream, then the FastStrea.quickWrite() method
 * will be used.
 * @exception IOException if an error occurs.
 */
//===================================================================
public static void write(Stream s,byte[] src,int start,int count,boolean useFastStream) throws IOException
//===================================================================
{
	if (s instanceof FastStream) ((FastStream)s).quickWrite(src,start,count);
	else s.write(src,start,count);
}
/**
 * Write out all the bytes to an OutputStream, optionally using the FastStream interface if available.
 * @param s the Stream to write to.
 * @param src the source data bytes to write.
 * @param start the offset within the src array.
 * @param count the number of bytes to write.
 * @param useFastStream if this is true and the Stream implements FastStream, then the FastStrea.quickWrite() method
 * will be used.
 * @exception IOException if an error occurs.
 */
//===================================================================
public static void write(OutputStream s,byte[] src,int start,int count,boolean useFastStream) throws IOException
//===================================================================
{
	if (s instanceof FastStream) ((FastStream)s).quickWrite(src,start,count);
	else s.write(src,start,count);
}
/**
Read all the bytes from a File, possibly using a FastStream. The destination must have
enough room to hold the incoming bytes.
* @param in The input File.
* @param dest The destination array.
* @param start the start position in the array.
* @param useFastStream true if the FastStream interface should be used if available.
* @return the number of bytes read.
* @exception IOException if there was an error reading the file.
*/
//===================================================================
public static int readAllBytes(File in,byte[] dest,int start,boolean useFastStream) throws IOException
//===================================================================
{
	long total = in.length();
	if (total > Integer.MAX_VALUE) throw new IOException("File is too big.");
	Stream ins = in.toReadableStream();
	try{
		read(ins,dest,start,(int)total,useFastStream,true);
	}finally{
		ins.close();
	}
	return (int)total;
}
/**
Read all the bytes from a File, possibly using a FastStream. The bytes read in are
appended to the destination ByteArray.
* @param in The input File.
* @param dest The destination ByteArray. If it is null a new one will be created and returned.
* @param useFastStream true if the FastStream interface should be used if available.
* @return the destination ByteArray or a new ByteArray if dest was null.
* @exception IOException if there was an error reading the file.
*/
//===================================================================
public static ByteArray readAllBytes(File in,ByteArray dest,boolean useFastStream) throws IOException
//===================================================================
{
	if (dest == null) dest = new ByteArray();
	if (dest.data == null) dest.data = new byte[0];
	//
	long total = in.length();
	if (total > Integer.MAX_VALUE) throw new IOException("File is too big.");
	int t = (int)total;
	int d = dest.length;
	dest.makeSpace(d,t);
	Stream s = in.toReadableStream();
	try{
		read(s,dest.data,d,t,useFastStream,true);
	}finally{
		s.close();
	}
	return dest;
}
//===================================================================
public static CharArray readAllChars(File in,CharArray dest,boolean useFastStream,TextCodec codec,ByteArray buffer)
throws IOException
//===================================================================
{
	if (codec == null) codec = new JavaUtf8Codec();
	if (buffer != null) buffer.length = 0;
	buffer = readAllBytes(in,buffer,useFastStream);
	return codec.decodeText(buffer.data,0,buffer.length,true,dest);
}
//===================================================================
public static CharArray readAllChars(File in,CharArray dest,boolean useFastStream)
throws IOException
//===================================================================
{
	return readAllChars(in,dest,useFastStream,null,null);
}

/**
* This is a blocking readAll. It will block the current thread.
**/
//===================================================================
public static void readAll(Stream s,byte [] dest,int start,int count) throws IOException
//===================================================================
{
	while(count > 0){
		int ret = s.read(dest,start,count);
		if (ret <= 0) throw new IOException("Unexpected end of stream");
		start += ret;
		count -= ret;
	}
	return;
}
/**
* This is a blocking readAll. It will block the current thread.
**/
//===================================================================
public static void readAll(InputStream s,byte [] dest,int start,int count) throws IOException
//===================================================================
{
	while(count > 0){
		int ret = s.read(dest,start,count);
		if (ret <= 0) throw new IOException("Unexpected end of stream");
		start += ret;
		count -= ret;
	}
	return;
}
/**
* This is a blocking readAll. It will block the current thread
**/
//===================================================================
public static void readAll(Stream s,byte [] dest) throws IOException
//===================================================================
{
	readAll(s,dest,0,dest.length);
}
/**
* This is a blocking readFully. It will block the current thread or
* Coroutine.
* @deprecated use readAll instead.
**/
//===================================================================
public static int readFully(Stream s,byte [] dest,int start,int count)
//===================================================================
{
	int did = 0;
	while(count > 0){
		int ret = s.readBytes(dest,start,count);
		if (ret < 0) return ret;
		if (ret == 0) return did;
		did += ret;
		start += ret;
		count -= ret;
	}
	return did;
}
/**
* This is a blocking readFully. It will block the current thread or
* Coroutine.
* @deprecated use readAll instead.
**/
//===================================================================
public static int readFully(Stream s,byte [] dest)
//===================================================================
{
	return readFully(s,dest,0,dest.length);
}

//===================================================================
public static boolean skip(RandomAccessStream ras,int num)
//===================================================================
{
	return ras.seek(ras.getFilePosition()+num);
}

//===================================================================
public static ByteArray processAll(DataProcessor dp,byte [] source)
//===================================================================
{
	try{
		return processAll(dp,source,0,source.length,null);
	}catch(IOException e){
		return null;
	}
}
//===================================================================
public static ByteArray processAll(DataProcessor dp,byte [] source,int offset,int length)
//===================================================================
{
	try{
		return processAll(dp,source,offset,length,null);
	}catch(IOException e){
		return null;
	}
}
//===================================================================
public static ByteArray processAll(DataProcessor dp,byte [] source,int offset,int length,ByteArray dest)
throws IOException
//===================================================================
{
	if (dest == null) dest = new ByteArray();
	dest.clear();
	int bs = dp.getBlockSize();
	if (bs <= 0) bs = 1;
	int max = dp.getMaxBlockSize();
	if (max <= 0) max = 0;
	if ((max == 0 || length <= max) && (length % bs) == 0){
		return dp.processBlock(source,offset,length,true,dest);
	}
	ByteArray buff = new ByteArray(), buff2 = null;
	int did = 0;
	while(did < length){
		//
		// Get up to max of the data.
		//
		buff.clear();
		if ((max != 0) && ((length-did) > max)){
			buff.append(source,offset,max);
			did += max;
		}else{
			buff.append(source,offset,(length-did));
			did = length;
		}
		//
		int toAppend = bs-(length%bs);
		if (toAppend != 0 && toAppend != bs)
			buff.append(new byte[toAppend],0,toAppend);
		if (max == 0 || length <= max) //All I would have done is padded the data.
			return dp.processBlock(buff.data,0,buff.length,true,dest);
		else{
			buff2 = dp.processBlock(buff.data,0,buff.length,(length == did),buff2);
			dest.append(buff2.data,0,buff2.length);
			buff2.clear();
		}
	}
	return dest;
	/*
	MemoryFile in = new MemoryFile(source,offset,length,RandomAccessFile.READ_WRITE);
	MemoryFile out = new MemoryFile();
	DataProcessorStream dsp = new DataProcessorStream(dp,out);
	boolean did = new IOTransfer().run(in,dsp);
	dsp.close();
	if (!did) return null;
 	return out.data;
	*/
}
private static ByteArray pip;
/**
* This is used to process data in place. In order for it to work the data processor must
* have a block length of 1 byte and must produce output data exactly the same length as the
* input data, and it should have no maximum input data size.
**/
//===================================================================
public static void processInPlace(DataProcessor dp,byte [] data,int offset,int length) throws IOException
//===================================================================
{
		ByteArray got = dp.processBlock(data,offset,length,true,pip);
		pip = got;
		ewe.sys.Vm.copyArray(pip.data,0,data,offset,length);
}
/**
* This reads all the bytes in a Stream (it will close the stream afterwards). If Stream is null
* it will return a null byte array. You can provide a StringBuffer "error" to accept any possible
* error messages.
**/
//===================================================================
public static byte [] readAllBytes(Stream input,StringBuffer error)
//===================================================================
{
	if (input == null) return null;
	MemoryFile mf = MemoryFile.createFrom(input,error);
	if (mf == null) return null;
	return mf.data.toBytes();
}
/**
 * Read all bytes from a Stream, closing the Stream when done. The bytes read in
 * are placed in a ewe.util.ByteArray object that is placed as the returnValue of
 * the returned Handle.
 * @param input The input stream to read from, it will be closed upon completion.
* @param knownSize The number of bytes expected to read. If it is -1 then the number to read
* will be unknown.
* @param stopAfterKnownSize if this is true then the copying will stop after the known number of bytes.
 * @return A Handle that can be used to monitor the reading of the data.
*/
//===================================================================
public static Handle readAllBytes(final Stream input,final int knownSize,final boolean stopAfterKnownSize)
//===================================================================
{
	int opts = StreamUtils.CLOSE_INPUT;
	if (!stopAfterKnownSize) opts |= StreamUtils.DONT_STOP_AFTER_KNOWN_SIZE;
	return StreamUtils.readAllBytes(input.toInputStream(),null,knownSize,opts);
/*
	return new ewe.sys.TaskObject(){
		protected void doRun(){
			handle.doing = "Reading data";
			interruptThreadToStop = true;
			MemoryFile mf = new MemoryFile();
			IOTransfer iot = new IOTransfer(input,mf,knownSize);
			iot.stopAfterTotalToCopy = stopAfterKnownSize;
			Handle h = iot.startTask();
			try{
				if (waitOn(h,h.Success,true)){
					handle.returnValue = mf.data;
					handle.set(handle.Succeeded);
				}else{
					h.stop(0);
					handle.set(Handle.Failed);
				}
			}finally{
				input.close();
			}
		}
	}.startTask();
*/
}
//-------------------------------------------------------------------
private static String fixKey(String keyName)
//-------------------------------------------------------------------
{
	if (!keyName.toUpperCase().startsWith("HKEY_")){
		if (!keyName.startsWith("\\")) keyName = "\\"+keyName;
		keyName = "HKEY_LOCAL_MACHINE\\Software"+keyName;
	}
	return keyName;
}
//-------------------------------------------------------------------
private static File toConfigFile(String keyName)
//-------------------------------------------------------------------
{
	int idx = keyName.lastIndexOf('\\');
	String got = (idx == -1 ? keyName : keyName.substring(idx+1))+".cfg";
	String home = ewe.sys.Vm.getProperty("HOME",File.getProgramDirectory());
	return ewe.sys.Vm.newFileObject().getNew(home).getChild(got);
}
//===================================================================
public static boolean canAccessRegistry()
//===================================================================
{
	try{
		//return Registry.isInitialized(false);
		return true;
	}catch(Throwable t){
		return false;
	}
}
/**
* Option for saveConfigInfo().
**/
public static final int SAVE_IN_REGISTRY = 0x1;
/**
* Option for saveConfigInfo().
**/
public static final int SAVE_IN_FILE = 0x2;
/**
* Option for saveConfigInfo().
**/
public static final int SAVE_IN_FILE_OR_REGISTRY = 0;


/**
 * Save a Vector containing a list of strings using saveConfigInfo. The strings will be separated using
 * a '\n' separator.
 * @param text The list of strings to save.
 * @param keyName The key name to use. This will be passed to saveConfigInfo().
 * @param saveOptions one of the SAVE_IN options.
 * @exception IOException If there was an error saving the information.
 */
//===================================================================
public static void saveStringList(ewe.util.Vector text,String keyName,int saveOptions) throws IOException
//===================================================================
{
	String s = "";
	if (text != null)
		for (int i = 0; i<text.size(); i++){
			if (i != 0) s += '\n';
			s += ewe.util.mString.toString(text.get(i));
		}
	saveConfigInfo(s,keyName,saveOptions);
}
/**
 * Read a Vector containing a list of string using getConfigInfo.
 * @param keyName The name of the key to save under.
 * @return null if the data is not present, else a Vector of strings.
 * @exception IOException If there was an error reading the information.
 */
//===================================================================
public static ewe.util.Vector getStringList(String keyName) throws IOException
//===================================================================
{
	String got = getConfigInfo(keyName);
	if (got == null) return null;
	return new ewe.util.Vector(ewe.util.mString.split(got,'\n'));
}
/**
  Save the text in the registry. If saveOptions is SAVE_IN_REGISTRY and its length is less than or equal to 1000 chars
	bytes, the text will be saved under the name in the registry. <p>
* @param data The text data to save.
* @param keyName The registry key name to store the data in. The actual save location will be
	"HKEY_LOCAL_MACHINE\Software\"+name, and the value will be saved as the default value. However if
	the name starts with HKEY_xxx, then it is assumed that the name provided will be the full
	registry key name.<p>
	If the string length is greater than 1000 chars OR if SAVE_IN_FILE is specified, it will be saved in a file in the program directory
	with a file name equal to the last subkey name in the keyName with a ".cfg" extension. And the file
	name will be saved in the registry instead.
* @param saveOptions should be one of SAVE_IN_REGISTRY or SAVE_IN_FILE or SAVE_IN_FILE_OR_REGISTRY. SAVE_IN_FILE_OR_REGISTRY
tells it to save wherever is most appropriate given the data size.
* @exception IOException if the data could not be saved successfully.
*/
//===================================================================
public static void saveConfigInfo(String data,String keyName,int saveOptions) throws IOException
//===================================================================
{
	keyName = fixKey(keyName);

	boolean reg = canAccessRegistry();
	boolean toRegistry = reg && ((saveOptions == SAVE_IN_REGISTRY) ||
	((saveOptions == SAVE_IN_FILE_OR_REGISTRY) && (data.length() < 1000)));

	String saveFile = null;

	if (toRegistry){
		RegistryKey rk = Registry.getLocalKey(0,keyName,true,true);
		if (rk == null) toRegistry = false;
		else{
			Object fn = rk.getValue("File");
			if (fn instanceof String) {
				saveFile = (String)fn;
				toRegistry = false;
			}
		}
	}
	if (!toRegistry){
		File configFile;
		if (saveFile == null){
			configFile = toConfigFile(keyName);
		}else{
			configFile =  ewe.sys.Vm.newFileObject().getNew(saveFile);
		}
		SafeFile sf = new SafeFile(configFile);
		File out = sf.getTempFile();
		StreamWriter pw = new StreamWriter(out.toWritableStream(false));
		pw.println(data);
		pw.close();
		sf.setNewFile(out);
		if (reg){
			RegistryKey rk = Registry.getLocalKey(0,keyName,true,true);
			if (rk != null) rk.setValue("File",sf.getFile().getFullPath());
		}
	}else{
		if (reg){
			RegistryKey rk = Registry.getLocalKey(0,keyName,true,true);
			if (rk != null) rk.setValue(null,data);
		}
	}
}
/**
 * Read configuration data which may be stored in the registry or in a configuration file.
* @param keyName The registry key name to store the data in. The actual save location will be
	"HKEY_LOCAL_MACHINE\Software\"+name, and the value will be saved as the default value. However if
	the name starts with HKEYS_xxx, then it is assumed that the name provided will be the full
	registry key name.<p>
	If the string length is greater than 1000 chars OR if SAVE_IN_FILE is specified, it will be saved in a file in the program directory
	with a file name equal to the last subkey name in the keyName with a ".cfg" extension. And the file
	name will be saved in the registry instead.
 * @return The configuration found or null if not found.
 * @exception IOException If there is configuration info but there was an error reading it.
 */
//===================================================================
public static String getConfigInfo(String keyName) throws IOException
//===================================================================
{
	return getConfigInfo(keyName,SAVE_IN_FILE_OR_REGISTRY);
}
/**
 * Read configuration data which may be stored in the registry or in a configuration file.
* @param keyName The registry key name to store the data in. The actual save location will be
	"HKEY_LOCAL_MACHINE\Software\"+name, and the value will be saved as the default value. However if
	the name starts with HKEYS_xxx, then it is assumed that the name provided will be the full
	registry key name.<p>
	If the string length is greater than 1000 chars OR if SAVE_IN_FILE is specified, it will be saved in a file in the program directory
	with a file name equal to the last subkey name in the keyName with a ".cfg" extension. And the file
	name will be saved in the registry instead.
 * @return The configuration found or null if not found.
 * @exception IOException If there is configuration info but there was an error reading it.
 */
//===================================================================
public static String getConfigInfo(String keyName,int options) throws IOException
//===================================================================
{
	keyName = fixKey(keyName);
	File configFile = null;
	if ((options & SAVE_IN_FILE) == 0){
		try{
			RegistryKey rk = Registry.getLocalKey(0,keyName,true,false);
			if (rk != null){
				Object got = rk.getValue("File");
			if (got instanceof String)
				configFile = ewe.sys.Vm.newFileObject().getNew((String)got);
			else {
				got = rk.getValue(null);
				if (got instanceof String)
					if (((String)got).length() != 0)
						return (String)got;
			}
			}
		}catch(Throwable t){
		}
	}
	if (configFile == null) configFile = toConfigFile(keyName);
	SafeFile sf = new SafeFile(configFile);
	if (!sf.getFile().exists()) return null;
	StreamReader br = new StreamReader(sf.getFile().getInputStream());
	String ret = br.readLine();
	br.close();
	return ret;
}
/**
 * Save a LiveData object as configuration information in the registry or file.
 * @param data The LiveData object to save.
* @param keyName The registry key name to store the data in. The actual save location will be
	"HKEY_LOCAL_MACHINE\Software\"+name, and the value will be saved as the default value. However if
	the name starts with HKEY_xxx, then it is assumed that the name provided will be the full
	registry key name.<p>
	If the string length is greater than 1000 chars OR if SAVE_IN_FILE is specified, it will be saved in a file in the program directory
	with a file name equal to the last subkey name in the keyName with a ".cfg" extension. And the file
	name will be saved in the registry instead.
 * @exception IOException If there was an error saving the data.
 */
//===================================================================
public static void saveConfigInfo(ewe.data.LiveData data,String keyName,int saveOptions) throws IOException
//===================================================================
{
	if (data == null) return;
	saveConfigInfo(data.textEncode(),keyName,saveOptions);
}
/**
 * Save a LiveData object as configuration information in the registry or file.
 * @param data The LiveData object to save.
* @param keyName The registry key name to store the data in. The actual save location will be
	"HKEY_LOCAL_MACHINE\Software\"+name, and the value will be saved as the default value. However if
	the name starts with HKEY_xxx, then it is assumed that the name provided will be the full
	registry key name.<p>
	If the string length is greater than 1000 chars OR if SAVE_IN_FILE is specified, it will be saved in a file in the program directory
	with a file name equal to the last subkey name in the keyName with a ".cfg" extension. And the file
	name will be saved in the registry instead.
 * @exception IOException If there was an error saving the data.
 */
//===================================================================
public static void saveConfigInfo(ewe.data.LiveData data,String keyName) throws IOException
//===================================================================
{
	if (data == null) return;
	saveConfigInfo(data.textEncode(),keyName,SAVE_IN_FILE_OR_REGISTRY);
}
/**
 * Get a LiveData object from configuration information in the registry or file.
 * @param data The LiveData object to load.
* @param keyName The registry key name to store the data in. The actual save location will be
	"HKEY_LOCAL_MACHINE\Software\"+name, and the value will be saved as the default value. However if
	the name starts with HKEY_xxx, then it is assumed that the name provided will be the full
	registry key name.<p>
	If the string length is greater than 1000 chars OR if SAVE_IN_FILE is specified, it will be saved in a file in the program directory
	with a file name equal to the last subkey name in the keyName with a ".cfg" extension. And the file
	name will be saved in the registry instead.
 * @exception IOException If there is configuration info but there was an error reading it.
 */
//===================================================================
public static boolean getConfigInfo(ewe.data.LiveData data,String keyName,int options) throws IOException
//===================================================================
{
	if (data == null) return false;
	String got = getConfigInfo(keyName,options);
	if (got == null) return false;
	data.textDecode(got);
	return true;
}
/**
 * Get a LiveData object from configuration information in the registry or file.
 * @param data The LiveData object to load.
* @param keyName The registry key name to store the data in. The actual save location will be
	"HKEY_LOCAL_MACHINE\Software\"+name, and the value will be saved as the default value. However if
	the name starts with HKEY_xxx, then it is assumed that the name provided will be the full
	registry key name.<p>
	If the string length is greater than 1000 chars OR if SAVE_IN_FILE is specified, it will be saved in a file in the program directory
	with a file name equal to the last subkey name in the keyName with a ".cfg" extension. And the file
	name will be saved in the registry instead.
 * @exception IOException If there is configuration info but there was an error reading it.
 */
//===================================================================
public static boolean getConfigInfo(ewe.data.LiveData data,String keyName) throws IOException
//===================================================================
{
	if (data == null) return false;
	String got = getConfigInfo(keyName);
	if (got == null) return false;
	data.textDecode(got);
	return true;
}
/**
Request the creation of a RandomAccessStream from the Streamable object.
* @param s The Streamable object.
* @param mode The RandomAccess mode, "r" or "rw".
* @param masterHandle an optional Handle used to abort the process if necessary.
* @param masterTimeOut the length of time to wait for.
* @return The created Stream or null if the masterTimeOut timed out.
* @exception HandleStoppedException If the masterHandle was stopped externally.
* @exception IOException If the Streamable failed to provide the requested Stream.
* @exception InterruptedException If this mThread was interrupted.
* @exception IllegalArgumentException If the mode is not correct.
*/
//===================================================================
public static RandomAccessStream toRandomAccessStream(Streamable s,String mode,Handle masterHandle,TimeOut masterTimeOut) throws IllegalArgumentException, HandleStoppedException, IOException, InterruptedException
//===================================================================
{
	return (RandomAccessStream)toStream(s.toStream(true,mode),masterHandle,masterTimeOut);
}
/**
Request the creation of a readable Stream from the Streamable object.
* @param s The Streamable object.
* @param masterHandle an optional Handle used to abort the process if necessary.
* @param masterTimeOut the length of time to wait for.
* @return The created Stream or null if the masterTimeOut timed out.
* @exception HandleStoppedException If the masterHandle was stopped externally.
* @exception IOException If the Streamable failed to provide the requested Stream.
* @exception InterruptedException If this mThread was interrupted.
*/
//===================================================================
public static Stream toReadableStream(Streamable s,Handle masterHandle,TimeOut masterTimeOut) throws HandleStoppedException, IOException, InterruptedException
//===================================================================
{
	return (Stream)toStream(s.toStream(true,"r"),masterHandle,masterTimeOut);
}
/**
* Request the creation of a writable Stream form the Streamable object.
* @param s The Streamable object.
* @param append if appending to the current data is requested.
* @param masterHandle an optional Handle used to abort the process if necessary.
* @param masterTimeOut the length of time to wait for.
* @return The created Stream or null if the masterTimeOut timed out.
* @exception HandleStoppedException If the masterHandle was stopped externally.
* @exception IOException If the Streamable failed to provide the requested Stream.
* @exception InterruptedException If this mThread was interrupted.
*/
//===================================================================
public static Stream toWritableStream(Streamable s,boolean append,Handle masterHandle,TimeOut masterTimeOut) throws HandleStoppedException, IOException, InterruptedException
//===================================================================
{
	return (Stream)toStream(s.toStream(true,append ? "a" : "w"),masterHandle,masterTimeOut);
}

//-------------------------------------------------------------------
static Object toStream(Handle got,Handle masterHandle,TimeOut masterTimeOut) throws HandleStoppedException, IOException, InterruptedException
//-------------------------------------------------------------------
{
	try{
		if (!got.waitOn(Handle.Success,new TimeOut(100),masterHandle,masterTimeOut)){
			if (masterHandle.shouldStop) throw new HandleStoppedException();
			return null;
		}
	}catch(HandleStoppedException e){
		if (got.errorObject instanceof IOException) throw (IOException)got.errorObject;
		else throw new IOException(got.error);
	}
	return got.returnValue;
}


public static int checkReadWrite(IOHandle rw,TimeOut checkTime,Handle master,TimeOut masterTime)
{
	if (rw.waitOnFlags(rw.Succeeded,checkTime,master,masterTime))
		return rw.bytesTransferred;
	else if (rw.errorCode == rw.STREAM_END_REACHED)
		return 0;
	else
		return -1;
}

public static void main(String args[]) throws IOException
{
	ewe.sys.Vm.startEwe(args);
	saveConfigInfo("Hello, this is config!","Ewesoft\\IOUtils",SAVE_IN_FILE_OR_REGISTRY);
	ewe.sys.Vm.debug("Info: "+getConfigInfo("Ewesoft\\IOUtils"));
	ewe.sys.Vm.sleep(1000);
	ewe.sys.Vm.exit(0);
}

/**
* The name for the Ewe library's Ascii Codec.
**/
public final static String ASCII_CODEC = "ASCII";
/**
* The name for the Ewe library's Java UTF8 Codec.
**/
public final static String JAVA_UTF8_CODEC = "JAVA_UTF8";

private static ewe.util.Hashtable codecs;
//-------------------------------------------------------------------
private static void setupCodecs()
//-------------------------------------------------------------------
{
	if (codecs != null) return;
	codecs = new ewe.util.Hashtable();
	codecs.put(ASCII_CODEC,new AsciiCodec());
	codecs.put(JAVA_UTF8_CODEC,new JavaUtf8Codec());
	codecs.put("UTF-8",new JavaUtf8Codec());
	codecs.put("UTF8",new JavaUtf8Codec());
}
/**
 * Add a new codec to the list of codecs.
 * @param codecName The canonical name of the codec.
 * @param codec The codec itself.
 */
//===================================================================
public static void setCodec(String codecName,TextCodec codec)
//===================================================================
{
	setupCodecs();
	if (codec != null) codecs.put(codecName.toUpperCase(),codec);
}
/**
 * Add a new copy of a named codec.
 * @param codecName The canonical name of the codec.
 * @return a new copy of a named codec or null if not found.
 */
//===================================================================
public static TextCodec getCodec(String codecName)
//===================================================================
{
	setupCodecs();
	TextCodec ret = (TextCodec)codecs.get(codecName.toUpperCase());
	if (ret != null) ret = (TextCodec)ret.getCopy();
	return ret;
}

/**
 * Get an Iterator to go through the installed codecs. The object returned by each call
 * to next() on the Iterator will be of type ewe.util.Map.MapEntry object.
 */
//===================================================================
public static ewe.util.Iterator getCodecs()
//===================================================================
{
	setupCodecs();
	return codecs.entries();
}
//===================================================================
public static String newString(byte[] data,int start,int length,String encoding)
throws UnsupportedEncodingException
//===================================================================
{
		TextCodec tc = getCodec(encoding);
		if (tc == null) throw new UnsupportedEncodingException();
		try{
			ewe.util.CharArray ca = tc.decodeText(data,start,length,true,null);
  	  return new String (ca.data,0,ca.length);
		}catch(IOException e){
			return null;
		}
}
//===================================================================
public static String newString(byte[] data,String encoding)
throws UnsupportedEncodingException
//===================================================================
{
	return newString(data,0,data.length,encoding);
}
//===================================================================
public static byte[] getBytes(String data,String encoding)
throws UnsupportedEncodingException
//===================================================================
{
	TextCodec tc = getCodec(encoding);
	if (tc == null) throw new UnsupportedEncodingException();
	char[] all = ewe.sys.Vm.getStringChars(data);
	try{
		return tc.encodeText(all,0,all.length,true,null).toBytes();
	}catch(IOException e){
		return null;
	}
}

//##################################################################
}
//##################################################################


