package ewe.security;
import ewe.io.DataProcessor;
import ewe.util.ByteArray;
import ewe.io.StreamCorruptedException;
import ewe.io.ByteArrayInputStream;
import ewe.zip.InflaterInputStream;
import ewe.io.ByteArrayOutputStream;
import ewe.util.Utils;
import ewe.io.IO;
import ewe.io.IOException;
import ewe.io.StreamUtils;
/**
A Decryptor implements the Blowfish CBC algorithm to decrypt data as encrypted
by an Encryptor. You can use it in two ways:
<p>
You can use it as a straight ewe.io.DataProcessor where you
provide it with encrypted data bytes and then receive the decrypted plain text. However
with this method the data must be padded up to a size that is a multiple of 8 bytes which
is the minimum block size of the Decryptor.
<p>
You can also use the more powerful decrypt() methods <b>if</b> the data was encrypted
by an Encryptor using the encrypt() method. This method adds more advanced capabilities to the
encryption, including "salting" the data (adding some randomness to it), optionally compressing
the data and optionally signing the data with a private key. All these add to the security of
the encrypted data and will also take care of stripping any padding from the encrypted data,
thereby ensuring that the decrypted data is the exact length of the original plain text.
**/
//##################################################################
public class Decryptor extends Encryptor{
//##################################################################


/**
 Create a Decryptor using the specified key. This method uses an SHA1 object to
 create a 160-bit hash of the password and this is then used as the key to the BlowfishCBC
 algorithm.
 @param key a password key.
 */
//===================================================================
public Decryptor(String key)
//===================================================================
{
	super(stringToKey(key));
	isDecryptor = true;
}
/**
 * Create a Decryptor using the specified key.
 * @param key a key which is a sequence of bytes up to a maximum of MAXKEYLENGTH (448 bits)
 */
//===================================================================
public Decryptor(byte[] key)
//===================================================================
{
	super(key);
	isDecryptor = true;
}
/**
* Decrypt data that has been encrypted by an Encrytpor with an encrypt() method using this
* Decryptor object as the decryptor.
* @param encryptedData the encrypted data.
* @param offset the start of the encrypted data bytes.
* @param length the number of data bytes.
* @return the decrypted bytes.
* @exception StreamCorruptedException if there was an error with the data or if the decryptor
* is incorrect.
*/
//===================================================================
public byte[] decrypt(byte[] encryptedData,int offset,int length)
throws StreamCorruptedException
//===================================================================
{
	return decrypt(null,encryptedData,offset,length,null).toBytes();
}
/**
* Decrypt data that has been encrypted by an Encrytpor with an encrypt() method using this
* Decryptor object as the decryptor.
* @param dest an optional destination ByteArray to hold the decrypted data.
* @param encryptedData the encrypted data.
* @param offset the start of the encrypted data bytes.
* @param length the number of data bytes.
* @param signature an optional Signature object setup with the received or stored signature of
* the data as produced by the encrypt() process. If it is not null the Signature will be used
* to validate the decrypted data.
* @return the decrypted bytes in the destination ByteArray or a new one if dest is null.
* @exception StreamCorruptedException if there was an error with the data or if the decryptor
* is incorrect.
*/
//===================================================================
public ByteArray decrypt(ByteArray dest, byte[] encryptedData,int offset,int length, Signature signature)
throws StreamCorruptedException
//===================================================================
{
	return decrypt(this,dest,encryptedData,offset,length,signature);
}
/**
* Decrypt data that has been encrypted by an Encrytpor with an encrypt() method using this
* any DataProcessor object as the decryptor. This method is called by the other two decrypt()
* methods using this Decryptor object as the DataProcessor object.
* @param processor a DataProcessor object to decrypt the data.
* @param dest an optional destination ByteArray to hold the decrypted data.
* @param encryptedData the encrypted data.
* @param offset the start of the encrypted data bytes.
* @param length the number of data bytes.
* @param signature an optional Signature object setup with the received or stored signature of
* the data as produced by the encrypt() process. If it is not null the Signature will be used
* to validate the decrypted data.
* @return the decrypted bytes in the destination ByteArray or a new one if dest is null.
* @exception StreamCorruptedException if there was an error with the data or if the decryptor
* is incorrect.
*/
//===================================================================
public static ByteArray decrypt(DataProcessor processor,ByteArray dest, byte[] encryptedData,int offset,int length, Signature signature)
throws StreamCorruptedException
//===================================================================
{
	try{
		if (dest == null) dest = new ByteArray();
		else dest.clear();
		//
		ByteArray proc = IO.processAll(processor,encryptedData,offset,length);
		//
		int num = (int)(proc.data[0] & 0xf)+8+1;
		length = Utils.readInt(proc.data,num,4);
		if (length < 0 || length+num+4 > proc.length) throw new StreamCorruptedException();
		//
		if (signature != null){
			if (digest == null) digest = new SHA1();
			if (!signature.verify(proc.data,0,length+num+4,digest))
				throw new StreamCorruptedException("Signature not verified.");
		}
		//
		if ((proc.data[0] & 0x90) == 0x90){ //It is compressed.
			ByteArrayInputStream in = new ByteArrayInputStream(proc.data,num+4,length);
			InflaterInputStream is = new InflaterInputStream(in);
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			StreamUtils.transfer(null,is.toInputStream(),bos);
			is.close();
			bos.close();
			proc = bos.toByteArray(null);
			dest.append(proc.data,0,proc.length);
		}else{
			dest.append(proc.data,num+4,length);
		}
		return dest;
	}catch(StreamCorruptedException sc){
		throw sc;
	}catch(IOException e){
		throw new StreamCorruptedException();
	}
}
//##################################################################
}
//##################################################################

