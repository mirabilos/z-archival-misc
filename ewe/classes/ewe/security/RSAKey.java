package ewe.security;
import ewe.math.BigInteger;
import ewe.math.MPN;
import ewe.util.Utils;
import ewe.util.ByteEncodable;
import ewe.util.ByteEncoder;
import ewe.util.ByteArray;
import ewe.io.StreamCorruptedException;
/**
* An RSAKey holds either a private or public key and will
* also perform encryption/decryption operations. No data as to whether
* it is private or public is stored here, you can place that in a higher level object if needed.
* <p>
* Note that due to the nature of this encryption calling decrypt() on data generated
* by encrypt() on the <b>same</b> key will not yield the original text. With this type of
* encryption you can only use a key to decrypt the data encrypted by its public/private
* counterpart.
* <p>
* Use the class RSA to generate a new pair of keys.
**/
//##################################################################
public class RSAKey implements ByteEncodable, EncryptionKey{
//##################################################################
//
// Do not move these two.
//
protected BigInteger exp;
protected BigInteger value;
/**
* Used to store the key in a stream of bytes. It is recommended that whenever the private
* key is stored it is encrypted using an encryption like that provided by ewe.security.Encryptor.
**/
//===================================================================
public int encodeBytes(ByteArray dest)
//===================================================================
{
	int total = ByteEncoder.encodeObject(dest,exp);
	total += ByteEncoder.encodeObject(dest,value);
	return total;
}
//-------------------------------------------------------------------
protected RSAKey(){}
//-------------------------------------------------------------------
/**
* This creates the key from the bytes as encoded by encodeBytes.
**/
//===================================================================
public RSAKey(byte[] encodedBytes,int offset,int length)
throws StreamCorruptedException
//===================================================================
{
	try{
		Object[] both = ByteEncoder.decodeObjects(encodedBytes,offset,length,null);
		exp = (BigInteger)both[0];
		value = (BigInteger)both[1];
	}catch(StreamCorruptedException s){
		throw s;
	}catch(Exception e){
		throw new StreamCorruptedException();
	}
}
/**
* Encrypt the provided data.
**/
//===================================================================
public byte[] encrypt(byte[] data,int offset,int length)
throws ewe.io.IOException
//===================================================================
{
/* Makes no noticable difference!
	if (hasNative) try{
		return encryptDecrypt(data,offset,length,true);
	}catch(UnsatisfiedLinkError e){
		hasNative = false;
	}catch(SecurityException se){
		hasNative = false;
	}
	*/
	try{
	int numInts = (length+3)/4;
	MPN m = new MPN(numInts+1);
	for (int i = 0,w = 0; i<length; i += 4, w++){
		int n = length-i;
		if (n > 4) n = 4;
		m.words[w] = Utils.readInt(data,offset+i,n);
	}
	m.words[numInts] = length;
	m.length = numInts+1;
	process(m);
	m.minimize();
	int ni = m.length;
	byte[] ret = new byte[ni*4];
	for (int i = 0, w = 0; i<ni*4; w++){
		int v = m.words[w];
		ret[i++] = (byte)((v >> 24)&0xff);
		ret[i++] = (byte)((v >> 16)&0xff);
		ret[i++] = (byte)((v >> 8)&0xff);
		ret[i++] = (byte)((v)&0xff);
	}
	return ret;
	}catch(Exception e){
		throw new ewe.io.IOException();
	}
}
/**
* Decrypt the data as encrypted by this key's counterpart.
**/
//===================================================================
public byte[] decrypt(byte[] encrypted,int offset,int length)
throws ewe.io.IOException
//===================================================================
{
/*
	if (hasNative) try{
		return encryptDecrypt(encrypted,offset,length,false);
	}catch(UnsatisfiedLinkError e){
		hasNative = false;
	}catch(SecurityException se){
		hasNative = false;
	}
	*/
	try{
	int numInts = (length+3)/4;
	MPN m = new MPN(numInts);
	for (int i = 0,w = 0; i<length; i += 4, w++){
		int n = length-i;
		if (n > 4) n = 4;
		m.words[w] = Utils.readInt(encrypted,offset+i,n);
	}
	m.length = numInts;
	process(m);
	m.minimize();
	int num = m.words[m.length-1];
	if (num < 0 || num > (m.length-1)*4)
		throw new ewe.io.StreamCorruptedException();
	byte[] ret = new byte[num];
	for (int i = 0, w = 0; i<num; w++){
		int v = m.words[w];
		if (num-i < 4)
			v <<= 8*(4-(num-i));
		ret[i++] = (byte)((v >> 24)&0xff);
		if (i < num)
			ret[i++] = (byte)((v >> 16)&0xff);
		if (i < num)
			ret[i++] = (byte)((v >> 8)&0xff);
		if (i < num)
			ret[i++] = (byte)((v)&0xff);
	}
	return ret;
	}catch(Exception e){
		throw new ewe.io.StreamCorruptedException();
	}
}
//-------------------------------------------------------------------
private void process(MPN data) throws IllegalArgumentException
//-------------------------------------------------------------------
{
	if (data.signum() < 0) throw new IllegalArgumentException();
	data.modPowPositive(new MPN().fromBigInteger(exp),new MPN().fromBigInteger(value));
}
//##################################################################
}
//##################################################################

