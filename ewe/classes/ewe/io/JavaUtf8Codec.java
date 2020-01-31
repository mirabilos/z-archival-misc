package ewe.io;
import ewe.util.*;

//##################################################################
public class JavaUtf8Codec implements TextCodec{
//##################################################################


/**
* This is a creation option. It specifies that CR characters should be removed when
* encoding text into UTF.
**/
public static final int STRIP_CR_ON_DECODE = 0x1;
/**
* This is a creation option. It specifies that CR characters should be removed when
* decoding text from UTF.
**/
public static final int STRIP_CR_ON_ENCODE = 0x2;
/**
* This is a creation option. It specifies that CR characters should be removed when
* decoding text from UTF AND encoding text to UTF.
**/
public static final int STRIP_CR = STRIP_CR_ON_DECODE|STRIP_CR_ON_ENCODE;

private int flags = 0;

//===================================================================
public JavaUtf8Codec(int options)
//===================================================================
{
	flags = options;
}
//===================================================================
public JavaUtf8Codec()
//===================================================================
{
	this(0);
}
//===================================================================
public ByteArray encodeText(char [] text, int start, int length, boolean endOfData, ByteArray dest) throws IOException
//===================================================================
{
	if (dest == null) dest = new ByteArray();
	int size = length == 0 ? 0 : Utils.sizeofJavaUtf8String(text,start,length);
	if (dest.data == null || dest.data.length < size)
		dest.data = new byte[size];
	byte [] destination = dest.data;
	int s = 0;
	for (int i = 0; i<length; i++){
		char c = text[i+start];
		if (c == 13 && ((flags & STRIP_CR_ON_ENCODE) != 0)) continue;
		if (c >= 0x1 && c <= 0x7f) destination[s++] = (byte)c;
		else if (c == 0 || (c >= 0x80 && c <= 0x7ff)) {
			//ewe.sys.Vm.debug(">"+(int)c);
			destination[s++] = (byte)(0xc0 | ((c >> 6) & 0x1f));
			destination[s++] = (byte) (0x80 | (c & 0x3f));
		}else{
			destination[s++] = (byte)(0xe0 | ((c >> 12) & 0xf));
			destination[s++] = (byte) (0x80 | ((c >> 6) & 0x3f));
			destination[s++] = (byte) (0x80 | (c  & 0x3f));
		}
	}
	//Utils.encodeJavaUtf8String(text,start,length,dest.data,0);
	dest.length = s;
	return dest;
}

private int byteOne = -1, byteTwo = -2;
//-------------------------------------------------------------------
private IOException badFormat()
//-------------------------------------------------------------------
{
	return new IOException("Bad format");
}

/**
* If dest is null, this will return the number of output chars that will be produced. If
* dest is not null this will return the number of unprocessed bytes at the end (either 0, 1
* or 2).
**/
//-------------------------------------------------------------------
private int decodeUtf(byte[] encoded, int start, int length, char[] dest, int destOffset)
//-------------------------------------------------------------------
{
	if (dest == null) destOffset = 0;
	int i = 0, t = destOffset;
	char ch;
	for (i = 0; i<length; i++){
		byte c = encoded[i+start];
		if ((c & 0x80) == 0){
			ch = (char)c;
			if (ch == 13 && ((flags & STRIP_CR_ON_DECODE) != 0)) t--;
			else if (dest != null) dest[t] = ch;
		}else if ((c & 0xe0) == 0xc0) {
			if (i == length-1){ // No more bytes.
				if (dest == null) return t-destOffset;
				else return 1;
			}
			ch = (char)((((char)c & 0x1f)<<6) + ((char)encoded[i+start+1] & 0x3f));
			if (ch == 13 && ((flags & STRIP_CR_ON_DECODE) != 0)) t--;
			else if (dest != null) dest[t] = ch;
			i++;
		}else if ((c & 0xf0) == 0xe0) {
			if (i > length-3){//Not enough bytes, need two more.
				if (dest == null) return t-destOffset;
				else return length-i;
			}
			ch = (char)((((char)c & 0x0f)<<12) + (((char)encoded[i+start+1] & 0x3f)<<6)+((char)encoded[i+start+2] & 0x3f));
			if (ch == 13 && ((flags & STRIP_CR_ON_DECODE) != 0)) t--;
			else if (dest != null) dest[t] = ch;
			i += 2;
		}
		t++;
		//ewe.sys.Vm.debug("<"+(int)buffer[t]);
	}
	if (dest == null) return t-destOffset;
	else return 0;
}
//===================================================================
public CharArray decodeText(byte [] encoded, int start, int length, boolean endOfData, CharArray dest) throws IOException
//===================================================================
{
	int toPut = -1;
	if (dest == null) dest = new CharArray();
	dest.length = 0;
	//
	// First check if there are any bytes left over from the last run.
	//
	if (byteOne != -1){
		if ((byteOne & 0xe0) == 0xc0){ //Need one more byte.
			if (length < 1) {//No bytes available in the array.
				if (endOfData) throw badFormat();
				else return dest;
			}else{//Yes, at least one byte available in the array.
				char got = (char)((((char)byteOne & 0x1f)<<6) + ((char)encoded[start] & 0x3f));
				toPut = (int)got & 0xffff;
				start++; length--;
				byteOne = byteTwo = -1;
			}
		//
		//Must need two more bytes.
		//
		}else{
			if (byteTwo == -1){
				if (length < 1){//No bytes available in the array.
					if (endOfData) throw badFormat();
					else return dest;
				}else{//Yes, at least one byte available in the array.
					byteTwo = (int)encoded[start] & 0xff;
					start++; length--;
				}
			}
			//At this point byteTwo WILL be valid.
			if (length < 1){//No bytes available in the array.
				if (endOfData) throw badFormat();
				else return dest;
			}else{//Yes, at least one byte available in the array.
				char got = (char)((((char)byteOne & 0x0f)<<12) + (((char)byteTwo & 0x3f)<<6)+((char)encoded[start] & 0x3f));
				toPut = (int)got & 0xffff;
				start++; length--;
				byteOne = byteTwo = -1;
			}
		}
	}
	int need = decodeUtf(encoded,start,length,null,0);
	if (toPut == 13 && ((flags & STRIP_CR_ON_DECODE) != 0)) toPut = -1;
	if (toPut != -1) need++;
	if (dest.data == null || dest.data.length < need)
		dest.data = new char[need];
	if (toPut != -1) dest.data[0] = (char)toPut;
	int left = decodeUtf(encoded,start,length,dest.data,(toPut != -1) ? 1 : 0);
	dest.length = need;
	byteOne = byteTwo = -1;
	//ewe.sys.Vm.debug("Left: "+left);
	if (left > 0) byteOne = encoded[start+length-1];
	if (left > 1) {
		byteTwo = byteOne;
		byteOne = encoded[start+length-2];
	}
	if (left != 0 && endOfData) throw badFormat();
	return dest;
}
//===================================================================
public void closeCodec() throws IOException
//===================================================================
{
	byteOne = byteTwo = -1;
}
/*
//===================================================================
static void outputData(CharArray ca)
//===================================================================
{
	String out = "";
	for (int i = 0; i<ca.length; i++)
		out += ewe.sys.Long.l1.set(ca.data[i]).toString(4,ewe.sys.Long.HEX|ewe.sys.Long.ZERO_FILL)+" ";
	if (ca.length != 0) ewe.sys.Vm.debug("Got: "+out);
	else ewe.sys.Vm.debug("Got: Nothing");
}
//===================================================================
public static void main(String args[]) throws IOException
//===================================================================
{
	ewe.sys.Vm.startEwe(args);
	char [] source = new char[]{0x12,0x123,0x11,0xf123,0x21};
	JavaUtf8Codec jc = new JavaUtf8Codec();
	ByteArray ba = jc.encodeText(source,0,source.length,true,null);
	ewe.sys.Vm.debug("Size: "+ba.length);
	CharArray ca = null;
	int end = ba.length;
	int step = 2;
	for (int i = 0; i<end; i += step){
		int num = end-i;
		if (num > step) num = step;
		outputData(ca = jc.decodeText(ba.data,i,num,false,ca));
	}
	outputData(ca = jc.decodeText(null,0,0,true,ca));
	ewe.sys.Vm.exit(0);
}
*/
//===================================================================
public Object getCopy()
//===================================================================
{
	return new JavaUtf8Codec(flags);
}
//##################################################################
}
//##################################################################

