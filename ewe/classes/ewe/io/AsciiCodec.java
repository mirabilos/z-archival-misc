package ewe.io;
import ewe.util.*;

//##################################################################
public class AsciiCodec implements TextCodec{
//##################################################################
/**
* This is a creation option. It specifies that CR characters should be removed when
* encoding text into ASCII.
**/
public static final int STRIP_CR_ON_DECODE = 0x1;
/**
* This is a creation option. It specifies that CR characters should be removed when
* decoding text from ASCII.
**/
public static final int STRIP_CR_ON_ENCODE = 0x2;
/**
* This is a creation option. It specifies that CR characters should be removed when
* decoding text from ASCII AND encoding text to ASCII.
**/
public static final int STRIP_CR = STRIP_CR_ON_DECODE|STRIP_CR_ON_ENCODE;

private int flags = 0;

//===================================================================
public AsciiCodec(int options)
//===================================================================
{
	flags = options;
}
//===================================================================
public AsciiCodec()
//===================================================================
{
	this(0);
}
//===================================================================
public ByteArray encodeText(char [] text, int start, int length, boolean endOfData, ByteArray dest) throws IOException
//===================================================================
{
	if (dest == null) dest = new ByteArray();
	if (dest.data == null || dest.data.length < length)
		dest.data = new byte[length];
	int t = 0;
	for (int i = 0; i<length; i++){
		if (text[i+start] == 13 && ((flags & STRIP_CR_ON_ENCODE) != 0)) t--;
		else dest.data[i+t] = (byte)text[i+start];
	}
	dest.length = length+t;
	return dest;
}
//===================================================================
public CharArray decodeText(byte [] encoded, int start, int length, boolean endOfData, CharArray dest) throws IOException
//===================================================================
{
	int toPut = -1;
	if (dest == null) dest = new CharArray();
	if (dest.data == null || dest.data.length < length)
		dest.data = new char[length];
	int t = 0;
	for (int i = 0; i<length; i++){
		if (encoded[i+start] == 13 && ((flags & STRIP_CR_ON_DECODE) != 0)) t--;
		else {
			dest.data[i+t] = (char)((int)encoded[i+start] & 0xff);
		}
	}
	dest.length = length+t;
	return dest;
}
//===================================================================
public void closeCodec() throws IOException
//===================================================================
{
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
	return new AsciiCodec(flags);
}
//##################################################################
}
//##################################################################

