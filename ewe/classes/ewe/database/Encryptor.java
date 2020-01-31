package ewe.database;
import ewe.io.*;
import ewe.sys.Time;
import ewe.util.*;

/**
 * @deprecated - use ewe.security.Encryptor instead.
 */
//##################################################################
public class Encryptor implements DataProcessor{
//##################################################################

private boolean bigRandom = false;

//===================================================================
public int getBlockSize()
//===================================================================
{
	return 1;
}
//===================================================================
public int getMaxBlockSize()
//===================================================================
{
	return 0;
}
//===================================================================
public void closeProcess()
//===================================================================
{
}

private long key;

//===================================================================
public Encryptor(String key)
//===================================================================
{
	setKey(key);
}
//===================================================================
public void setKey(Object key)
//===================================================================
{
	this.key = makeKey(mString.toString(key));
}
//===================================================================
public Encryptor()
//===================================================================
{

}
ByteArray temp;

//-------------------------------------------------------------------
byte [] doTheProcess(long key,byte [] theBytes)
//-------------------------------------------------------------------
{
	byte [] ret = encrypt(key,theBytes);
	return ret;
}

//===================================================================
public ByteArray processBlock(byte [] data, int start, int length, boolean lastInBlock, ByteArray dest) throws IOException
//===================================================================
{
	try{
		if (dest == null) dest = new ByteArray();
		dest.length = 0;
		if (temp == null) temp = new ByteArray();
		if (length != 0)
			temp.insert(temp.length,data,start,length);
		byte [] ret = doTheProcess(key,temp.toBytes());
		if (!lastInBlock) return dest;
		temp.length = 0;
		dest.data = ret;
		dest.length = ret.length;
		return dest;
	}catch(Exception e){
		throw new EncryptedDataException();//"Encryption/Decryption error!");
	}
}
//-------------------------------------------------------------------
 static long getLong(byte [] from,int sourceStart)
//-------------------------------------------------------------------
{
	long ret = 0;
	for (int i = 0; i<8; i++){
		ret = ret << 8;
		ret |= (from[sourceStart+i] & 0xff);
	}
	return ret;
}
//-------------------------------------------------------------------
 static void putLong(long val,byte [] dest,int destStart)
//-------------------------------------------------------------------
{
	for (int i = 7; i>=0; i--){
		if (destStart+i < dest.length)
			dest[destStart+i] = (byte)(val & 0xff);
		val = val >> 8;
	}
}
//-------------------------------------------------------------------
 long encryptSegment(long key,byte [] source,int sourceStart,byte [] dest, int destStart)
//-------------------------------------------------------------------
{
	long out = getLong(source,sourceStart);
	if (dest != null) putLong(out^key,dest,destStart);
	return out;
}
//-------------------------------------------------------------------
 long decryptSegment(long key,byte [] source,int sourceStart,byte [] dest, int destStart)
//-------------------------------------------------------------------
{
	long out = getLong(source,sourceStart);
	if (dest != null) putLong(out^key,dest,destStart);
	return out^key;
}

//-------------------------------------------------------------------
static long makeKey(String textKey)
//-------------------------------------------------------------------
{
	byte [] key = new byte[8];
	for (int i = 0; i<8; i++) key[i] = 0;
	int len = textKey.length();
	int l = len;
	if (len < 8) l = 8;
	int i2 = l-1;
	int i3 = 0;
	for (int i = 0; i<8; i+=2) {
		int idx = i3 % len;
		key[i] = (byte)(textKey.charAt(idx) & 0xff);
		idx = i2 % len;
		key[i+1] = (byte)(textKey.charAt(idx) & 0xff);
		i2--; i3++;
	}
	return getLong(key,0);
}

//-------------------------------------------------------------------
void putRandomBytes(byte [] dest,int destOffset,int length)
//-------------------------------------------------------------------
{
	for (int i = 0; i<length; i++)
		dest[destOffset+i] = (byte)((Math.random()*255)+1);
}

//-------------------------------------------------------------------
byte [] prepareForEncrypt(byte [] source,int sourceOffset,int length)
//-------------------------------------------------------------------
{
	int prepend = bigRandom ? 16+(int)(Math.random()*16) : 16;
	int append = bigRandom ? 24+(int)(Math.random()*16) : 8;
	append -= (prepend+append+length)%8;
	byte [] dest = new byte[length+prepend+append];
	putRandomBytes(dest,0,prepend);
	ewe.sys.Vm.copyArray(source,sourceOffset,dest,prepend,length);
	putRandomBytes(dest,length+prepend,append);
	long len = length;
	putLong(length,dest,prepend-8);
	dest[prepend-9] = 0;
	return dest;
}

//-------------------------------------------------------------------
static byte [] encrypt(String key,byte [] source)
//-------------------------------------------------------------------
{
	Encryptor e = new Encryptor();
	return e.encrypt(e.makeKey(key),source);
}
//-------------------------------------------------------------------
static byte [] decrypt(String key,byte [] source)
//-------------------------------------------------------------------
{
	Encryptor e = new Encryptor();
	return e.decrypt(e.makeKey(key),source);
}
/*
//===================================================================
public static String encryptToText(String key,byte [] source)
//===================================================================
{
	byte [] all = encrypt(key,source);
	ByteArray ret = new Base64Codec().encode(all,0,all.length,null);
	byte [] encrypted = ret.toBytes();
	return mString.fromAscii(encrypted,0,encrypted.length);
}
//===================================================================
public static byte [] decryptFromText(String key,String base64Text)
//===================================================================
{
	byte [] ascii = mString.toAscii(base64Text);
	ByteArray ret = new Base64Codec().decode(ascii,0,ascii.length,null);
	return decrypt(key,ret.toBytes());
}
//===================================================================
static String encryptToText(String key,String data)
//===================================================================
{
	return encryptToText(key,Utils.encodeJavaUtf8String(data));
}

//===================================================================
static String decryptToText(String key,String encoded)
//===================================================================
{
	byte [] all = decryptFromText(key,encoded);
	if (all == null) return null;
	return Utils.decodeJavaUtf8String(all,0,all.length);
}
*/

//-------------------------------------------------------------------
byte [] encrypt(long key,byte [] source)
//-------------------------------------------------------------------
{
	return encrypt(key,source,0,source.length);
}
//-------------------------------------------------------------------
byte [] decrypt(long key,byte [] source)
//-------------------------------------------------------------------
{
	return decrypt(key,source,0,source.length);
}
//-------------------------------------------------------------------
 byte [] encrypt(long key,byte [] source,int sourceOffset,int length)
//-------------------------------------------------------------------
{
	byte [] ec = prepareForEncrypt(source,sourceOffset,length);
	return straightEncrypt(key,ec,0,ec.length);
}

//-------------------------------------------------------------------
byte [] straightEncrypt(long originalKey,byte [] source,int sourceOffset,int length)
//-------------------------------------------------------------------
{
	byte [] ret = new byte[length];
	long key = originalKey;
	for (int i = 0; i<length/8; i++){
		key = encryptSegment(key,source,sourceOffset+i*8,ret,i*8)|originalKey;
		//key = originalKey;
	}
	return ret;
}
//-------------------------------------------------------------------
byte [] decrypt(long key,byte [] source,int sourceOffset,int length)
//-------------------------------------------------------------------
{
	try{
		byte [] ret = straightDecrypt(key,source,sourceOffset,length);
		int lenPos = -1;
		for (int i = 0; i<ret.length; i++) {
			if (ret[i] == 0) {
				lenPos = i+1;
				break;
			}
		}
		if (lenPos == -1) throw new RuntimeException();
		int len = (int)getLong(ret,lenPos);
		if (len > ret.length-8) throw new RuntimeException();
		byte [] data = new byte[len];
		ewe.sys.Vm.copyArray(ret,lenPos+8,data,0,len);
		return data;
	}catch(Exception e){
		return new byte[0];
	}
}
//-------------------------------------------------------------------
 byte [] straightDecrypt(long originalKey,byte [] source,int sourceOffset,int length)
//-------------------------------------------------------------------
{
	byte [] ret = new byte[length];
	long key = originalKey;
	for (int i = 0; i<length/8; i++){
		key = decryptSegment(key,source,sourceOffset+i*8,ret,i*8)|originalKey;
		//key = originalKey;
	}
	return ret;
}
//===================================================================
public static ByteArray makeEncryptorTest(DataProcessor dp,int minSize,ByteArray out) throws IOException
//===================================================================
{
	if (out == null) out = new ByteArray();
	out.clear();
	if (minSize < 8) minSize = 8;
	int min = dp.getBlockSize();
	if (min < 1) min = 1;
	if (minSize%min != 0) minSize += min-(minSize%min);
	int max = dp.getMaxBlockSize();
	if (max > 0 && max<minSize) minSize = max;
	int numInts = minSize/4;
	Random r = new Random(new Time().getEncodedTime());
	int total = 0;
	byte[] all = new byte[minSize];
	for (int i = 0; i<numInts-1; i++){
		int ri = r.nextInt();
		total += ri;
		Utils.writeInt(ri,all,i*4,4);
	}
	Utils.writeInt(~total,all,(numInts-1)*4,4);
	dp.processBlock(all,0,all.length,true,out);
	return out;
}
//-------------------------------------------------------------------
public static boolean testDecryptor(DataProcessor dp,byte[] src,int offset,int length)
throws IOException
//-------------------------------------------------------------------
{
	ByteArray got = IO.processAll(dp,src,offset,length,null);
	int numInts = got.length/4;
	int total = 0;
	for (int i = 0; i<numInts; i++)
		total += Utils.readInt(got.data,i*4,4);
	return total == 0xffffffff;
}
//##################################################################
}
//##################################################################

