package ewe.security;
import ewe.sys.Time;
import ewe.io.StreamCorruptedException;
import ewe.io.IOException;
import ewe.util.Utils;
/**
* A Signature is used to either hold a received Ewe digital signature,
* or to generate a Ewe digital signature.
**/
//##################################################################
public class Signature{
//##################################################################
/**
* This is the signer's public key (for verifying signatures) or private key (for creating signatures).
**/
public EncryptionKey signersKey;
/**
* This is an optional timestamp to be embedded in the signature.
**/
public Time time;
/**
* This is the length of the document being signed.
**/
public int length;
/**
* This is the fully encoded signature bytes for transmission or storage along with the document.
**/
public byte[] signature;

private byte[] sigData;

/**
* This is used on a signature that has already been generated. After the construction, the
* length variable will hold the length of the document and the time variable will be set
* to the timestamp in the signature, or null if no timestamp was encoded.<p>
* After calling this constructor you can call the verify() methods to verify if a document
* is the one that was signed.
* @param signature the recieved or stored encoded signature bytes.
* @param signersPublicKey the public key of the signer.
* @exception StreamCorruptedException if the data is invalid in any way.
*/
//===================================================================
public Signature(byte[] signature,EncryptionKey signersPublicKey)
throws StreamCorruptedException
//===================================================================
{
	try{
		this.signature = signature;
		signersKey = signersPublicKey;
		sigData = signersKey.decrypt(signature,0,signature.length);
		length = Utils.readInt(sigData,sigData.length-12,4);
		long tm = Utils.readLong(sigData,sigData.length-8);
		if (tm == -1L) time = null;
		else {
			time = new Time();
			time.setEncodedTime(tm);
		}
	}catch(Exception e){
		throw new StreamCorruptedException();
	}
}
/**
 * Create a Signature object in preparation for signing. After creating the object with
 * this constructor, you should call one of the sign() methods.
 * @param signersKey the private key of the entity that will be signing the data.
 * @param signatureTime an optional timestamp to include in the signature.
 */
//===================================================================
public Signature(EncryptionKey signersPrivateKey,Time signatureTime)
//===================================================================
{
	this.signersKey = signersPrivateKey;
	this.time = signatureTime;
}
/**
* Produce a signature for a document. The signature variable is set to be the final
* signature bytes and this is also returned.
* @param data the data bytes to sign.
* @param offset the start of the data bytes in the data parameter.
* @param length the number of bytes in the data.
* @param digest an optional pre-created SHA1 object to create the document digest.
* @return the bytes for the signature.
* @exception IOException if an encryption of data processing error occurs.
*/
//===================================================================
public byte[] sign(byte[] data,int offset,int length,SHA1 digest)
throws IOException
//===================================================================
{
	if (digest == null) digest = new SHA1();
	else digest.reset();
	digest.update(data,offset,length);
	return getSignature(digest);
}

/**
 * Add bytes to the signature. After calling this for all bytes you should call
 * getSignature() to get the signature.
 * @param data the source bytes.
 * @param offset the start of the bytes in the data.
 * @param length the number of bytes in the data.
 * @param digest the SHA1 using to create the message digets (must not be null).
 */
public void addToSignature(byte[] data,int offset,int length,SHA1 digest)
{
	digest.update(data,offset,length);
}

/**
 * Get the signature for a document that has had all its bytes passed through digest,
 * either through addToSignature() or directly via update(). After this call the signature
 * is placed in the "signature" field.
 * @param digest the digest being used for the signature.
 * @return the signature bytes.
 * @throws IOException if there is an error encrypting the signature.
 */
public byte[] getSignature(SHA1 digest) throws IOException
{
	digest.digest();
	byte[] sigData = new byte[SHA1.DIGEST_SIZE+12];
	digest.getDigest(sigData,0);
	Utils.writeInt(length,sigData,sigData.length-12,4);
	Utils.writeLong(time != null ? time.getEncodedTime() : -1L,sigData,sigData.length-8);
	return signature = signersKey.encrypt(sigData,0,sigData.length);
}
//-------------------------------------------------------------------
private boolean equals(byte[] one,byte[] two,int twoLength)
//-------------------------------------------------------------------
{
	if (one == two) return true;
	if (one == null || two == null) return false;
	if (one.length != twoLength) return false;
	for (int i = 0; i<one.length; i++){
		if (one[i] != two[i]) return false;
	}
	return true;
}
/**
 * Verify that a document with a particular data length and with a particular digest
 * bytes is the same as the one that was signed with this signature.
 * @param dataLength the length of the data.
 * @param digest the SHA1 digest of the data.
 * @return true if it verified correctly.
 */
//===================================================================
public boolean verify(int dataLength, byte[] digest)
//===================================================================
{
	if (this.length != length) return false;
	return equals(digest,sigData,sigData.length-12);
}
/**
 * Verify that a document is the same as the one that was signed with this signature.
* @param data the data to verify.
* @param offset the start of the data.
* @param length the number of bytes in the data.
* @param digest an optional pre-created SHA1 object to digest the data.
* @return true if it verified correctly.
*/
//===================================================================
public boolean verify(byte[] data,int offset,int length,SHA1 digest)
//===================================================================
{
	if (length != this.length) return false;
	if (digest == null) digest = new SHA1();
	else digest.reset();
	return equals(digest.digest(data,offset,length),sigData,sigData.length-12);
}
//##################################################################
}
//##################################################################

