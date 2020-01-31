/* $MirOS: contrib/hosted/ewe/classes/ewe/security/Encryptor.java,v 1.2 2008/04/11 03:53:44 tg Exp $ */

package ewe.security;
import ewe.io.ByteArrayOutputStream;
import ewe.io.DataProcessor;
import ewe.io.IO;
import ewe.io.IOException;
import ewe.util.ByteArray;
import ewe.util.Random;
import ewe.util.Utils;
import ewe.zip.DeflaterOutputStream;

//##################################################################
public class Encryptor implements DataProcessor{
//##################################################################

private byte[] myKey;
protected boolean isDecryptor;
BlowfishCBC myFish;
private static SecureRandom random;
protected static SHA1 digest;

public static final int ENCRYPT_WITHOUT_COMPRESSION = 0x1;

//===================================================================
static public byte[] stringToKey(String sPassword)
//===================================================================
{
		int nI, nC;
		SHA1 sh = null;
		byte[] hash;
		// hash down the password to a 160bit key, using SHA-1
		sh = new SHA1();
		for (nI = 0, nC = sPassword.length(); nI < nC; nI++)
		{
			sh.update((byte) (sPassword.charAt(nI) & 0x0ff));
		}
		sh.digest();
		// setup the encryptor (using a dummy IV for now)
		hash = new byte[SHA1.DIGEST_SIZE];
		sh.getDigest(hash, 0);
		return hash;
}

//-------------------------------------------------------------------
protected void reset()
//-------------------------------------------------------------------
{
	myFish.reset(myKey,0,myKey.length);
	//myFish.cleanUp();
	//myFish = new BlowfishCBC(myKey);
}
//===================================================================
public Encryptor(String key)
//===================================================================
{
	this(stringToKey(key));
}
//===================================================================
public Encryptor(byte[] key)
//===================================================================
{
	myFish = new BlowfishCBC(key);
	myKey = key;
}
//===================================================================
public int getBlockSize()
//===================================================================
{
	return myFish.BLOCKSIZE;
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
	myFish.cleanUp();
}
//===================================================================
public ByteArray processBlock(byte[] source,int offset,int length,boolean last, ByteArray dest)
//===================================================================
{
	if (dest == null) dest = new ByteArray();
	dest.clear();
	if (source != null && length > 0){
		dest.makeSpace(0,length);
		if (isDecryptor) myFish.decrypt(source,offset,dest.data,0,length);
		else myFish.encrypt(source,offset,dest.data,0,length);
	}
	if (last) {
		reset();
	}
	return dest;
}
/**
* Produce a random block of bytes that may be used to later test a decryptor. The provided
* encryptor is used to process the block of data and later using testDecryptor(), a decryptor
* is tested to see if it successfully decrypts the data.
* @param encryptor the encryptor to use.
* @param minSize a minimum number of bytes to use. By default it is 8.
* @param out an optional output ByteArray.
* @return the output ByteArray or a new ByteArray.
* @exception IOException if there is an error processing the data.
*/
//===================================================================
public static ByteArray makeEncryptorTest(DataProcessor encryptor,int minSize,ByteArray out) throws IOException
//===================================================================
{
	if (out == null) out = new ByteArray();
	out.clear();
	if (minSize < 8) minSize = 8;
	int min = encryptor.getBlockSize();
	if (min < 1) min = 1;
	if (minSize%min != 0) minSize += min-(minSize%min);
	int max = encryptor.getMaxBlockSize();
	if (max > 0 && max<minSize) minSize = max;
	int numInts = minSize/4;
	Random r = new Random();
	int total = 0;
	byte[] all = new byte[minSize];
	for (int i = 0; i<numInts-1; i++){
		int ri = r.nextInt();
		total += ri;
		Utils.writeInt(ri,all,i*4,4);
	}
	Utils.writeInt(~total,all,(numInts-1)*4,4);
	encryptor.processBlock(all,0,all.length,true,out);
	return out;
}

/**
 * Test a decryptor to see if it is able to decrypt data encrypted by an encryptor that
 * generated the source test block of data.
 * <p>Note that if a decryptor fails this test then it is defintiely unable to decrypt the
 * encryptor's data, but a return of true indicates only that it is highly likely that it
 * will.
 * @param decryptor the decryptor.
 * @param src the data bytes as produced by makeEncryptorTest().
 * @param offset the offset of the data bytes.
 * @param length the number of data bytes.
 * @return false if the decryptor definitely does not decrypt the encryptor's data, true
 * if the decryptor will most likely decrypt its data correctly.
 */
//===================================================================
public static boolean testDecryptor(DataProcessor decryptor,byte[] src,int offset,int length)
//===================================================================
{
	try{
		ByteArray got = IO.processAll(decryptor,src,offset,length,null);
		int numInts = got.length/4;
		int total = 0;
		for (int i = 0; i<numInts; i++)
			total += Utils.readInt(got.data,i*4,4);
		return total == 0xffffffff;
	}catch(IOException e){
		return false;
	}
}
/**
 * Encrypt a "salted" version of the provided plaintext data
 * <b>without</b> compressing the data before encryption and without signing the salted data.<p>
 * The data is first salted to add some randomness to it.
 * The data is the processed using this Encryptor object and the resulting encrypted data is returned in the provided destination
 * ByteArray (or a new one if it is null).
 * @param plaintext The data to be encrypted.
 * @param offset The offset of the data in the array.
 * @param length The number of bytes to be encrypted.
 * @return The encrypted data as an array of bytes.
 * @exception IOException if an error occurs processing the data.
 */
//===================================================================
public byte[] encrypt(byte[] plaintext,int offset,int length)
throws IOException
//===================================================================
{
	ByteArray got = encrypt(null,plaintext,offset,length);
	return got.toBytes();
}
/**
 * Encrypt a "salted" version of the provided plaintext data
 * <b>without</b> compressing the data before encryption<p>
 * The data is first salted to add some randomness to it.
 * The data is the processed using this Encryptor object and the resulting encrypted data is returned in the provided destination
 * ByteArray (or a new one if it is null).
 * @param dest The destination ByteArray or null to get a new one.
 * @param plaintext The data to be encrypted.
 * @param offset The offset of the data in the array.
 * @param length The number of bytes to be encrypted.
 * @return The encrypted data in the destinaton ByteArray, or a new ByteArray.
 * @exception IOException if an error occurs processing the data.
 */
//===================================================================
public ByteArray encrypt(ByteArray dest, byte[] plaintext,int offset,int length)
throws IOException
//===================================================================
{
	return encrypt(dest,plaintext,offset,length,ENCRYPT_WITHOUT_COMPRESSION,null);
}
/**
 * Encrypt a "salted" version of the provided plaintext data
 * optionally compressing the data before encryption and optionally signing the salted/compressed
 * data.<p>
 * The data is first compressed if ENCRYPT_WITHOUT_COMPRESSION is <b>not</b> specified as an option.
 * The data (which may now be compressed) is then salted to add some randomness to it.
 * If the signature parameter is not null the salted version is then signed. The salted version
 * is signed instead of the plaintext version to reduce the chance of the source data being
 * guessed from the signature, based on past signatures.<p>
 * The data is the processed using this Encryptor object and the resulting encrypted data is returned in the provided destination
 * ByteArray (or a new one if it is null).
 * @param dest The destination ByteArray or null to get a new one.
 * @param plaintext The data to be encrypted.
 * @param offset The offset of the data in the array.
 * @param length The number of bytes to be encrypted.
 * @param options This can be ENCRYPT_WITHOUT_COMPRESSION or 0.
 * @param signature This should be a Signature() object with a valid private key. After this method
 * returns, the signature bytes of this object will hold the correct signature for the data.
 * @return The encrypted data in the destinaton ByteArray, or a new ByteArray.
 * @exception IOException if an error occurs processing the data.
 */
//===================================================================
public ByteArray encrypt(ByteArray dest, byte[] plaintext,int offset,int length, int options,Signature signature)
throws IOException
//===================================================================
{
	return encrypt(this,dest,plaintext,offset,length,options,signature);
}
/**
 * Using a particular DataProcessor, encrypt a "salted" version of the provided plaintext data
 * optionally compressing the data before encryption and optionally signing the salted/compressed
 * data.<p>
 * The data is first compressed if ENCRYPT_WITHOUT_COMPRESSION is <b>not</b> specified as an option.
 * The data (which may now be compressed) is then salted to add some randomness to it.
 * If the signature parameter is not null the salted version is then signed. The salted version
 * is signed instead of the plaintext version to reduce the chance of the source data being
 * guessed from the signature, based on past signatures.<p>
 * The data is the processed using the provided DataProcessor (which is assumed to be some kind
 * of encryption algorithm) and the resulting encrypted data is returned in the provided destination
 * ByteArray (or a new one if it is null).
 * @param processor The DataProcessor to be used for encryption (e.g. an instance of an Encryption object).
 * @param dest The destination ByteArray or null to get a new one.
 * @param plaintext The data to be encrypted.
 * @param offset The offset of the data in the array.
 * @param length The number of bytes to be encrypted.
 * @param options This can be ENCRYPT_WITHOUT_COMPRESSION or 0.
 * @param signature This should be a Signature() object with a valid private key. After this method
 * returns, the signature bytes of this object will hold the correct signature for the data.
 * @return The encrypted data in the destinaton ByteArray, or a new ByteArray.
 * @exception IOException if an error occurs processing the data.
 */
//===================================================================
public static ByteArray encrypt(DataProcessor processor,ByteArray dest, byte[] plaintext,int offset,int length, int options,Signature signature)
throws IOException
//===================================================================
{
	boolean compressed = (options & ENCRYPT_WITHOUT_COMPRESSION) == 0;
	if (compressed){
		ByteArrayOutputStream bo = new ByteArrayOutputStream();
		DeflaterOutputStream os = new DeflaterOutputStream(bo);
		os.write(plaintext,offset,length);
		os.close();
		ByteArray cmp = bo.toByteArray(null);
		plaintext = cmp.data;
		offset = 0;
		length = cmp.length;
	}
	//
	ByteArray src = new ByteArray();
	if (random == null)
		random = new SecureRandom();
	byte rand = (byte)random.nextInt();
	//
	// At least 9 random bytes will be placed.
	//
	int num = (rand & 0xf)+8;
	if (compressed) rand |= 0x90;
	else if ((rand & 0x90) == 0x90)
		rand &= ~0x80;
	//
	src.makeSpace(src.length,num+1);
	src.data[0] = rand;
	byte[] nb = random.generateSeed(num);
	System.arraycopy(nb,0,src.data,1,num);
	src.appendInt(length);
	src.append(plaintext,offset,length);
	//
	if (signature != null){
		if (digest == null) digest = new SHA1();
		signature.sign(src.data,0,src.length,digest);
	}
	ByteArray encryptedData = IO.processAll(processor,src.data,0,src.length);
	//
	if (dest == null) dest = new ByteArray();
	dest.data = encryptedData.data;
	dest.length = encryptedData.length;
	return dest;
}
//##################################################################
}
//##################################################################
