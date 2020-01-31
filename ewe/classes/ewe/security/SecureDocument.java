
package ewe.security;
import ewe.io.DataProcessor;
import ewe.io.IOException;
import ewe.io.StreamCorruptedException;
import ewe.reflect.Wrapper;
import ewe.sys.Time;
import ewe.util.ByteArray;
import ewe.util.ByteEncoder;
import ewe.util.Utils;
/**
* A SecureDocument is a document that has been made secure by any one or more of a number
* of ways. These include:<p>
* <nl>
* <li>
* The document has been encrypted - and optionally compressed
* by deflation before encryption. The data is usually salted before encryption.
* <li>
* The key for the document encryption (if any) may be included, itself being encrypted using
* the receivers public key.
* <li>
* The document may be digitally signed by the sender using the sender's private key. This signature
* will contain the SHA1 digest of the message (or salted message if it is encrypted), the length
* of the message (or salted message) and a signature timestamp (which may be zeroed).
* </nl>
**/
//##################################################################
public class SecureDocument{
//##################################################################

byte[] source;
int offset;
int length;
EncryptionKey creatorKey, receiverKey;
DataProcessor decryptor, encryptor;
byte[] lastKey, lastEncryptedKey;
private SHA1 digest;
private SecureRandom random = new SecureRandom();
Time time;

public boolean dontUseCompression = false;
public boolean dontTimestampSignature = false;
public Signature signature = null;
public byte[] encryptedKey = null;

/**
* This is only used if there is a remote Public Key available, since this indicates
* that an encryption password could be created at random for each message, and then
* encrypted in the receivers public key.
**/
public int changeFrequency = 10;
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
//-------------------------------------------------------------------
private boolean equals(byte[] one,byte[] two)
//-------------------------------------------------------------------
{
	if (one == two) return true;
	if (one == null || two == null) return false;
	return equals(one,two,two.length);
}
/**
 * Create a new SecureDocument. After calling this you would call the set() methods
 * to set the security parameters, and then call either encode() to generate the
 * document or decode() to decode the document.
 * @return
 */
//===================================================================
public SecureDocument()
//===================================================================
{
}
/**
 * Set the source data. This is either the document in its secure form (in which case you
 * later wish to decode it using decode()) or the document in its plaintext form (in which
 * case you wish later to call encode() on it to encode it).
 * @param source The source bytes.
 * @param offset The offset.
 * @param length The length.
 * @return this SecureDocument
 */
//===================================================================
public SecureDocument setData(byte[] source,int offset,int length)
//===================================================================
{
	this.source = source;
	this.offset = offset;
	this.length = length;
	return this;
}
/**
 * Set the source data. This is either the document in its secure form (in which case you
 * later wish to decode it using decode()) or the document in its plaintext form (in which
 * case you wish later to call encode() on it to encode it).
 * @param source The source bytes.
 * @return this SecureDocument
 */
//===================================================================
public SecureDocument setData(byte[] source)
//===================================================================
{
	return setData(source,0,source.length);
}
//-------------------------------------------------------------------
protected DataProcessor getEncryptor(byte[] key)
//-------------------------------------------------------------------
{
	return new Encryptor(key);
}
//-------------------------------------------------------------------
protected DataProcessor getDecryptor(byte[] key)
//-------------------------------------------------------------------
{
	return new Decryptor(key);
}
//-------------------------------------------------------------------
protected byte[] stringToKey(String password)
//-------------------------------------------------------------------
{
	return Encryptor.stringToKey(password);
}
/**
 * You can call this before encoding to explicitly set the key used for the symmetric encryption.
 * @param key the key for the encryption.
 * @return This SecureDocument.
 * @exception IOException
 */
//===================================================================
public SecureDocument setPassword(String password) throws IOException
//===================================================================
{
	return setPassword(stringToKey(password));
}
/**
 * You can call this before encoding to explicitly set the key used for the symmetric encryption.
 * @param key the key for the encryption.
 * @return This SecureDocument.
 * @exception IOException
 */
//===================================================================
public SecureDocument setPassword(byte[] key) throws IOException
//===================================================================
{
	if (equals(key,lastKey)) return this;
	encryptor = getEncryptor(key);
	decryptor = getDecryptor(key);
	lastKey = (byte[])key.clone();
	if (receiverKey != null) lastEncryptedKey = receiverKey.encrypt(key,0,key.length);
	changeFrequency = 0;
	return this;
}
//===================================================================
public SecureDocument setEncryptor(DataProcessor encryptor) throws IOException
//===================================================================
{
	this.encryptor = encryptor;
	changeFrequency = 0;
	lastKey = null;
	return this;
}
//===================================================================
public SecureDocument setDecryptor(DataProcessor decryptor) throws IOException
//===================================================================
{
	this.decryptor = decryptor;
	lastKey = null;
	return this;
}
/**
 * Set the private/public keys. If you are going to be creating (encoding) the document
 * then you should set creatorKey to be your private key and receiverKey to be the receiver's
 * key. If you do not set the creatorKey then the document will not be digitaly signed. If
 * you do not set the receiverKey then the document will not be encrypted unless you explicitly
 * set the encryption key using setEncryptor(byte[] key).
 * @param receiverKey The key of the receiver of the document - if this is not null, then random
 * symmetric keys will be used to encode the data and the key will be encrypted using the receiver's
 * public key.
 * @param creatorKey The key of the creator of the document - this is used for signing the document.
 * If it is null, the document will not be signed.
 * @return This SecureDocument
 */
//===================================================================
public SecureDocument setKeys(EncryptionKey receiverKey,EncryptionKey creatorKey)
//===================================================================
{
	this.creatorKey = creatorKey;
	this.receiverKey = receiverKey;
	return this;
}
/**
* This removes all encryption parameters, leaving it in a state unable to encrypt
* or decrypt any data.
* @return This SecureDocument.
*/
//===================================================================
public SecureDocument clearEncryption()
//===================================================================
{
	creatorKey = receiverKey = null;
	lastEncryptedKey = lastKey = null;
	encryptor = decryptor = null;
	changeFrequency = 10;
	return this;
}
/**
* This forces the SecureDocument to change its encryption for encoding by choosing a new random key.
* This is only
* used when a remote Public Key is available. The new encryption key is
* encrypted using the receiver's Public Key and then saved along with the data
* encrypted using that new key.
**/
//===================================================================
public SecureDocument changeEncryption() throws IOException
//===================================================================
{
	if (receiverKey == null) return this;
	byte[] newKey = new byte[20];
	random.nextBytes(newKey);
	lastKey = newKey;
	lastEncryptedKey = receiverKey.encrypt(newKey,0,newKey.length);
	encryptor = getEncryptor(newKey);
	return this;
}
//-------------------------------------------------------------------
private byte[] sign(byte[] src,int offset,int length) throws IOException
//-------------------------------------------------------------------
{
	if (creatorKey == null) return null;
	byte[] signature = new byte[32];
	if (digest == null) digest = new SHA1();
	else digest.reset();
	digest.update(src,offset,length);
	digest.digest();
	digest.getDigest(signature,0);
	Utils.writeInt(length,signature,20,4);
	Utils.writeLong(time == null ? 0 : time.getEncodedTime(),signature,24);
	signature = creatorKey.encrypt(signature,0,signature.length);
	return signature;
}

/**
 * Return whether or not this SecureDocument can encode() any data with any kind of
 * security. This will return true if:<br>
	<ul>
	<li>creatorKey != null - in which case a signature can be generated for the data and saved
	with the document.
	<li>receiverKey != null - in which case a random symmetric key can be chosen and used
	to encrypt the data. The symmetric key will be encrypted using the receiver's public key
	and stored along with the encrypted data.
	<li>encryptor != null - in which case the encryptor will be used to encrypt the data to
	be stored, and it is assumed that the receiver will have the matching decryptor to decrypt
	the data.
	</ul>
 */
//===================================================================
public boolean hasEncryptionParameters()
//===================================================================
{
	return receiverKey != null || creatorKey != null || encryptor != null;
}

/**
 * Encode the plain text as set by setData() into a secure document sequence of bytes. This
 * byte sequence can be stored or transmitted and then another SecureDocument object can be
 * used to decode the data - once it has been set up with the correct decryption parameters.
 * @param destination an optional destination ByteArray.
 * @return the destination or a new ByteArray if destination is null.
 * @exception IOException on an encryption error.
 * @exception IllegalStateException if no security options have been setup.
 */
//===================================================================
public ByteArray encode(ByteArray destination) throws IOException, IllegalStateException
//===================================================================
{
	EncryptionKey remotePublicKey = receiverKey, myPrivateKey = creatorKey;
	boolean usesEncryption = remotePublicKey != null || myPrivateKey != null || encryptor != null;
	if (source == null) throw new  IllegalStateException("The data has not been set.");
	if (!usesEncryption) throw new IllegalStateException("No security parameters have been set.");
	encryptedKey = null;
	ByteArray encryptedData = null;
	signature = null;
	//
	if (remotePublicKey != null){
		//I can randomly choose an encryptor key, encrypt it using the receiver's public key
		//and then send it before I send the encrypted data.
		if (encryptor == null || changeFrequency > 0){
			if (encryptor == null || lastKey == null) changeEncryption();
			else if ((random.nextInt() % changeFrequency) == 0)
				changeEncryption();
		}
		encryptedKey = lastEncryptedKey;
	}
	//
	if (myPrivateKey != null)
		signature = new Signature(myPrivateKey,dontTimestampSignature ? null : new Time());
	//
	if (encryptor != null)
		encryptedData = Encryptor.encrypt(encryptor,null,source,offset,length,dontUseCompression ? Encryptor.ENCRYPT_WITHOUT_COMPRESSION : 0,signature);
	else{
		if (digest == null) digest = new SHA1();
		signature.sign(source,offset,length,digest);
	}
	//
	if (destination == null) destination = new ByteArray();
	destination.clear();
	ByteArray out = destination;
	int flags = 0;
	if (remotePublicKey != null) flags |= 0x1;
	if (encryptor != null) flags |= 0x2;
	if (myPrivateKey != null) flags |= 0x4;
	Wrapper w = new Wrapper();
	ByteEncoder.encode(out,w.setInt(flags));
	if (remotePublicKey != null)
		ByteEncoder.encode(out,w.setObject(encryptedKey));
	if (encryptor != null)
		ByteEncoder.encode(out,encryptedData.data,0,encryptedData.length);
	else
		ByteEncoder.encode(out,source,offset,length);
	if (signature != null)
		ByteEncoder.encode(out,w.setObject(signature.signature));
	return destination;
}
public static final int STATUS_HAS_ENCRYPTION_KEY = 0x1;
public static final int STATUS_DATA_IS_ENCRYPTED = 0x2;
public static final int STATUS_HAS_SIGNATURE = 0x4;


/**
 * Get the encryption status of the encoded document.
 * @return a value that has one or more of the flags set: STATUS_HAS_ENCRYPTION_KEY,
	STATUS_DATA_IS_ENCRYPTED, STATUS_HAS_SIGNATURE
 * @exception StreamCorruptedException if the data is corrupted.
 */
//===================================================================
public int getStatus() throws StreamCorruptedException
//===================================================================
{
	try{
		Wrapper[] all = ByteEncoder.decode(source,0,source.length);
		return all[0].getInt();
	}catch(Exception e){
		throw new StreamCorruptedException();
	}
}
/**
 * Decode the encrypted text as set by setData() into the plain text of the original document.
 * @param dest an optional destination ByteArray.
 * @return the destination or a new ByteArray if dest is null.
 * @exception IOException on a decryption error.
 */
//===================================================================
public ByteArray decode(ByteArray dest) throws IOException
//===================================================================
{
	if (dest == null) dest = new ByteArray();
	dest.clear();
	try{
		Wrapper[] all = ByteEncoder.decode(source,0,source.length);
		int flags = all[0].getInt();
		int i = 1, hashedLength;
		signature = null;
		encryptedKey = null;
		byte[] encryptedData = null;
		byte[] sigData = null;
		byte[] plainData = null;
		byte[] hash = null;
		if ((flags & 0x1) != 0)
			encryptedKey = (byte[])all[i++].getObject();
		if ((flags & 0x2) != 0)
			encryptedData = (byte[])all[i++].getObject();
		else
			plainData = (byte[])all[i++].getObject();
		if ((flags & 0x4) != 0)
			sigData = (byte[])all[i++].getObject();

		if (encryptedKey != null){
			if (receiverKey == null) throw new IOException("Cannot decrypt the data.");
			if (!equals(lastEncryptedKey,encryptedKey)){
				lastEncryptedKey = encryptedKey;
				decryptor = getDecryptor(receiverKey.decrypt(encryptedKey,0,encryptedKey.length));
			}
		}
		//
		if (sigData != null && creatorKey != null){
			if (digest == null) digest = new SHA1();
			signature = new Signature(sigData,creatorKey);
			time = signature.time;
		}
		//
		if (encryptedData != null){
			//ewe.sys.Vm.out.println("Decrypting...");
			if (decryptor == null) throw new IOException("Cannot decrypt the data.");
			return Decryptor.decrypt(decryptor,dest,encryptedData,0,encryptedData.length,signature);
		}else{
			dest.data = plainData;
			dest.length = plainData.length;
			if (signature != null)
				if (!signature.verify(plainData,0,plainData.length,digest))
					throw new StreamCorruptedException("Data failed verification.");
			return dest;
		}
	}catch(IOException io){
		throw io;
	}catch(Exception e){
		//ewe.sys.Vm.debug("Error: "+e.getMessage());
		throw new StreamCorruptedException();
	}
	/*

	ByteArray proc = IO.processAll(decryptor,get.data,0,get.length);
	try{
		int num = (int)(proc.data[0] & 0xf)+4+1;
		length = Utils.readInt(proc.data,num,4);
		dest.append(proc.data,num+4,length);
	}catch(Exception e){
		throw new IOException("Encryption is incorrect");
	}
	*/
}
/**
* Call this on a new SecureDocument to create a byte sequence to be sent
* to the server who you believe is holding the private key of the supplied
* public key. The server should call authenticate() on the received data
* and then send back the authenticated bytes. The sender should then call validateAuthenticator() on
* this same SecureDocument providing the received authenticated bytes. If it returns true
* this proves that the receiver is in possession of the private key.
* @param serverPublicKey the public key of ther server.
* @return a byte sequence to be sent to the server for authentication.
* @exception IOException if this authentication cannot be carried out using the server's public key.
*/
//===================================================================
public byte[] makeAuthenticator(EncryptionKey serverPublicKey) throws IOException
//===================================================================
{
	byte[] keyToUse = new byte[20];
	random.nextBytes(keyToUse);
	//
	// Save these two for use by validateAuthenticator().
	//
	encryptedKey = keyToUse;
	creatorKey = serverPublicKey;
	//
	return serverPublicKey.encrypt(keyToUse,0,keyToUse.length);
}
/**
* This is called by a server when it has received a sequence of bytes as created by
* makeServerAuthenticator(). It will attempt to authenticate that it holds the private
* key of the public key that the requestor believes belongs to this server. It produces
* a sequence of bytes to send back to the requestor for validation using validateAuthenticator().
* @param authenticator the byte sequence as created by makeServerAuthenticator() on the client.
* @param myPrivateKey my private key.
* @return a byte sequence to be sent back to the client to prove that this server does have
* the private key.
* @exception IOException if an error occurs decrypting or encrypting the data - which usually
* indicates that the keys do not match.
*/
//===================================================================
public byte[] authenticate(byte[] authenticator,EncryptionKey myPrivateKey)
throws IOException
//===================================================================
{
	byte[] data = myPrivateKey.decrypt(authenticator,0,authenticator.length);
	byte[] keyToUse = new byte[20];
	random.nextBytes(keyToUse);
	Encryptor ec = new Encryptor(keyToUse);
	byte[] encryptedKey = myPrivateKey.encrypt(keyToUse,0,keyToUse.length);
	byte[] sendBack = ec.encrypt(data,0,data.length);
	ByteArray out = new ByteArray();
	ByteEncoder.encodeObject(out,encryptedKey);
	ByteEncoder.encodeObject(out,sendBack);
	return out.toBytes();
}
/**
 * Validate the reply from the server as created by authenticate(). This can only be
 * called on the same SecureDocument that makeServerAuthenticator() was called on.
 * @param reply the reply from the server as generated by authenticate().
 * @return true if it passed the test, false if not.
 */
//===================================================================
public boolean validateAuthenticator(byte[] reply)
//===================================================================
{
	try{
		Wrapper [] w = ByteEncoder.decode(reply,0,reply.length);
		byte[] ek = (byte[])w[0].getObject();
		byte[] dt = (byte[])w[1].getObject();
		Decryptor d = new Decryptor(creatorKey.decrypt(ek,0,ek.length));
		dt = d.decrypt(dt,0,dt.length);
		return equals(dt,encryptedKey);
	}catch(Exception e){
		return false;
	}
}
//##################################################################
}
//##################################################################

