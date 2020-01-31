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
import ewe.util.*;

//##################################################################
public class Base64Codec extends Errorable{
//##################################################################

/**
* This returns the six bit value of the character. It returns -1 if the
* character is invalid.
*/
public static final String encodingChars =
"ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/";
//==================================================================
public int charToByte(char ch)
//==================================================================
{
	int val = -1;
	if (ch >= 'A' && ch <= 'Z') val = ch-'A';
	else if (ch >= 'a' && ch <= 'z') val = ch-'a'+26;
	else if (ch >= '0' && ch <= '9') val = ch-'0'+52;
	else if (ch == '+') val = 62;
	else if (ch == '/') val = 63;
	else val = -1;
	return val;
}
/**
* This decodes four characters into between 1 to 3 bytes. The four characters MUST
* be valid or '=' signs (for the end of stream padding).
*/
//-------------------------------------------------------------------
protected int decode4(char [] chars,int charOffset,byte [] dest,int destOffset) //throws EncodingError
//-------------------------------------------------------------------
{
	int numEquals = 0;
	int val = 0;
	for (int i = 0; i<4; i++) {
		char ch = chars[charOffset+i];
		int v = 0;
		if (ch != '=') v = charToByte(ch);
		else numEquals++;
		if (v == -1) return -1;//throw new EncodingError("decode4() called with invalid character:'"+ch+"'");
		val = (val << 6)|v;
	}
	dest[destOffset+2] = (byte)(val & 0xff); val >>= 8;
	dest[destOffset+1] = (byte)(val & 0xff); val >>= 8;
	dest[destOffset+0] = (byte)(val & 0xff);
	if (numEquals >= 3) return -1;//throw new EncodingError("Two many pad ('=') characters in stream at decode4()");
	if (numEquals == 2) return 1;
	else if (numEquals == 1) return 2;
	else return 3;
}
//==================================================================
public ewe.util.ByteArray decode(byte [] chars,int offset,int length,ewe.util.ByteArray dest) //throws EncodingError
//==================================================================
{
	if (dest == null) dest = new ByteArray();
	dest.length = 0;
	int max = ((length/4)+1)*3;
	if (dest.data.length < max) dest.data = new byte[max];
	int got = 0;
	byte [] out = dest.data;
	char [] buff = new char[4];
	int sent = 0, i = 0,did = 0;
	try{
		for (i = 0; i < length; i++) {
			char ch = (char)(chars[offset+i] & 0xff);
			if ((charToByte(ch) == -1) && (ch != '=')) continue;
			buff[sent++] = ch;
			if (sent == 4) {
				did = decode4(buff,0,out,got);
				if (did == -1) return (ByteArray)returnError("Encoding error",null);
				got += did;
				if (did != 3) {
					break;
				}
				sent = 0;
			}
		}
	}catch(Exception e) {e.printStackTrace();}
	dest.length = got;
	//byte [] ret = new byte[got];
	//System.arraycopy(out,0,ret,0,got);
	//return ret;
	return dest;
}
byte [] incoming = new byte[3];
int inBuff = 0;
//-------------------------------------------------------------------
protected int encode3(byte [] bytes,int offset,int length,byte [] dest,int destOffset)
//-------------------------------------------------------------------
{
	if (length == 0) return 0;
	int val = 0;
	for (int i = 0; i<3; i++) {
		val = val << 8;
		if (i<length) val |= ((int)bytes[offset+i])&0xff;
	}
	for (int i = 0; i<4; i++) {
		dest[destOffset+3-i] = (byte)(encodingChars.charAt(val & 0x3f) & 0xff);
		val = val >> 6;
	}
	if (length < 3) dest[destOffset+3] = (byte)('=' & 0xff);
	if (length < 2) dest[destOffset+2] = (byte)('=' & 0xff);
	return length;
}
//==================================================================
public ByteArray encode(byte [] bytes,int offset,int length,ByteArray dest)
//==================================================================
{
	if (dest == null) dest = new ByteArray();
	int numOut = ((length+inBuff)/3)*4;
	int did = 0;
	if (dest.data == null) dest.data = new byte[numOut];
	if (dest.data.length < numOut) dest.data = new byte[numOut];
	dest.length = 0;
	byte [] out = dest.data;
	for (int i = 0; i<length; i++) {
		incoming[inBuff++] = bytes[i+offset];
		if (inBuff == 3) {
			encode3(incoming,0,3,out,did);
			did+=4;
			inBuff = 0;
		}
	}
	dest.length = did;
	return dest;
}
//-------------------------------------------------------------------
protected byte [] closeEncoding()
//-------------------------------------------------------------------
{
	if (inBuff == 0) return null;
	byte [] out = new byte[4];
	encode3(incoming,0,inBuff,out,0);
	inBuff = 0;
	return out;
	/*
	if (dest == null) dest = new ByteArray();
	dest.length = 0;
	if (inBuff == 0) return dest;
	if (dest.data.length < 4) dest.data = new byte[4];
	byte [] out = dest.data;
	encode3(incoming,0,inBuff,out,0);
	dest.length = 4;
	inBuff = 0;
	return dest;
	*/
}

/*
//==================================================================
public Handle encode(InputStream source,PrintWriter dest,boolean closeWhenDone)
//==================================================================
{
	mRunnable mr = new encodeThread(source,dest,closeWhenDone);
	mr.start();
	return mr.handle;
}
//==================================================================
public Handle decode(BufferedReader source,OutputStream dest,boolean closeWhenDone)
//==================================================================
{
	mRunnable mr = new decodeThread(source,dest,closeWhenDone);
	mr.start();
	return mr.handle;
}

//===================================================================
public static String encode(byte [] data)
//===================================================================
{
	InputStream src = new ByteArrayInputStream(data);
	StringWriter sw = new StringWriter();
	PrintWriter dest = new PrintWriter(sw);
	Handle h = new Base64Codec().encode(src,dest,true);
	if (!h.waitSucceeded(TimeOut.Forever)) return null;
	return sw.toString();
}
//===================================================================
public static byte [] decode(BufferedReader br)
//===================================================================
{
	ByteArrayOutputStream bos = new ByteArrayOutputStream();
	Handle h = new Base64Codec().decode(br,bos,true);
	if (!h.waitSucceeded(TimeOut.Forever)) return null;
	return bos.toByteArray();
}
//===================================================================
public static byte [] decode(String s)
//===================================================================
{
	BufferedReader br = new BufferedReader(new StringReader(s));
	ByteArrayOutputStream bos = new ByteArrayOutputStream();
	Handle h = new Base64Codec().decode(br,bos,true);
	if (!h.waitSucceeded(TimeOut.Forever)) return null;
	return bos.toByteArray();
}
*/
/*
	//##################################################################
	class encodeThread extends mRunnable{
	//##################################################################
	InputStream source;
	PrintWriter dest;
	boolean closeIt;
	encodeThread(InputStream source,PrintWriter dest,boolean closeIt)
	{
		this.source = source;
		this.dest = dest;
		this.closeIt = closeIt;
	}
	protected void close()
	{
		if (closeIt) {
			try{dest.close();}catch(Exception ex){}
			try{source.close();}catch(Exception ex){}
		}
	}
	public void doRun()
	{
		try {
			long transferred = 0;
			byte [] got = new byte[48];
			while(true) {
				int r = source.read(got,0,48);
				if (r == 0) continue;
				if (r == -1) break;
				transferred += r;
				char [] en = encode(got,0,r);
				if (en.length == 0) continue;
				dest.println(new String(en));
				handle.progress = transferred;
				handle.notifyChange();
			}
			char [] en2 = closeEncoding();
			if (en2.length != 0) dest.println(new String(en2));
			dest.println("");
			dest.flush();
			close();
			handle.set(handle.Succeeded);
		}catch(Exception e){
			close();
			handle.exception = e;
			handle.set(handle.Failed);
		}
	}
	//##################################################################
	}
	//##################################################################
	//##################################################################
	class decodeThread extends mRunnable{
	//##################################################################
	BufferedReader source;
	OutputStream dest;
	boolean closeIt;
	decodeThread(BufferedReader source,OutputStream dest,boolean closeIt)
	{
		this.source = source;
		this.dest = dest;
		this.closeIt = closeIt;
	}
	protected void close()
	{
		if (closeIt) {
			try{dest.close();}catch(Exception ex){}
			try{source.close();}catch(Exception ex){}
		}
	}
	public void doRun()
	{
		try {
			long transferred = 0;
			while(true) {
				String s = source.readLine();
				if (s == null) break;
				if (s.trim().length() == 0) break;
				byte [] ret = decode(s.toCharArray(),0,s.length());
				if (ret.length != 0) dest.write(ret,0,ret.length);
				transferred += ret.length;
				handle.progress = transferred;
				handle.notifyChange();
			}
			close();
			handle.set(handle.Succeeded);
		}catch(Exception e){
			close();
			handle.exception = e;
			handle.set(handle.Failed);
		}
	}
	//##################################################################
	}
	//##################################################################
*/
/*
//===================================================================
public void printBytes(byte [] bytes)
//===================================================================
{
	for (int i = 0; i<bytes.length; i++)
		System.out.print(""+bytes[i]+" ");
	System.out.println("");
}
*/
/*
//===================================================================
public static void main(String args[]) throws EncodingError, IOException
//===================================================================
{
	Base64Codec cd = new Base64Codec();
	if (args[0].toUpperCase().startsWith("-E")) {
		InputStream is = new FileInputStream(args[1]);
		cd.encode(is,new PrintWriter(new FileOutputStream(args[2])),true);
	}else{
		BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(args[1])));
		cd.decode(br,new FileOutputStream(args[2]),true);
	}
	/-*
	BufferedReader bf = new BufferedReader(new InputStreamReader(System.in));
	while(true) {
		String s = bf.readLine();
		if (s == null) break;
		cd.printBytes(cd.decode(s.toCharArray(),0,s.length()));
	}*-/
}
*/
//##################################################################
}
//##################################################################


