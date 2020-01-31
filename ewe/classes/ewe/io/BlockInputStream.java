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
import ewe.util.ByteArray;
import ewe.util.Utils;
import ewe.security.SecureDocument;
import ewe.security.EncryptionKey;
/**
* A BlockInputStream is used to read in distinct blocks of data as sent by BlockOutputStream.
* The data sent by the BlockOutputStream may be Secure Documents as represented by ewe.security.SecureDocument
* and this BlockInputStream will then attempt to decode and validate them using the encryption
* parameters as provided by setPassword() and setKeys().
**/
//##################################################################
public class BlockInputStream extends BlockIO{
//##################################################################
protected InputStream in;
/**
* Create a BlockInputStream using the provided InputStream for reading
* in data.
**/
//===================================================================
public BlockInputStream(InputStream in)
//===================================================================
{
	super(false);
	this.in = in;
}
/**
* Set the decryptor to be used explicitly.
**/
//===================================================================
public void setDecryptor(DataProcessor decryptor) throws IOException
//===================================================================
{
	getSecureDocument().setDecryptor(decryptor);
}
/**
* Set the decryptor by creating a new Decryptor which uses the provided password.
**/
//===================================================================
public void setDecryptor(String password) throws IOException
//===================================================================
{
	getSecureDocument().setPassword(password);
}
/**
* This is used to read the next block of incoming data.
* @param dest an optional destination ByteArray. If this is null a new one is created.
* The destination ByteArray is always cleared before the new data is added.
* @return the destination ByteArray or a new ByteArray. On a clean end-of-stream this will
* return null.
* @exception IOException if an error occured reading or decoding or decrypting the data.
*/
//===================================================================
public ByteArray readBlock(ByteArray dest) throws IOException
//===================================================================
{
	if (dest == null) dest = new ByteArray();
	else dest.clear();
	//
	int did = 0;
	while(did < 4){
		int got = in.read(intBuff,did,4-did);
		if (got == -1)
			if (did == 0) return null; //EOF
			else throw new EOFException();
		did += got;
	}
	int length = Utils.readInt(intBuff,0,4);
	boolean hasEncryption = false;
	if (length < 0){
		hasEncryption = true;
		length = -length;
	}
	int got = 0;
	ByteArray get = hasEncryption ? new ByteArray() : dest;
	get.makeSpace(0,length);
	StreamUtils.readFully(in,get.data,0,length);
	if (!hasEncryption) return get;
	SecureDocument sd = getSecureDocument();
	sd.setData(get.data,0,get.length);
	return sd.decode(dest);
}
/**
* This is used to read the next block of incoming data.
* @return A new ByteArray containing the data or null if the stream has closed cleanly.
* @exception IOException if an error occured reading or decoding or decrypting the data.
*/
//===================================================================
public ByteArray readBlock() throws IOException
//===================================================================
{
	return readBlock(null);
}
/**
* close this BlockInputStream and the underlying input stream.
* @exception IOException if an error occured closing the underlying stream.
*/
//===================================================================
public void close() throws IOException
//===================================================================
{
	in.close();
}
/**
 * Set the Public/Private keys.
 * @param myPrivateKey This is used to decrypt the symmetric session key for each block of data.
 * @param remotePublicKey The remotePublicKey is used to verify the signature
 * of data signed by the sender. If it is null signatures will not be verified.
 */
//===================================================================
public void setKeys(EncryptionKey myPrivateKey, EncryptionKey remotePublicKey)
//===================================================================
{
	SecureDocument sd = getSecureDocument();
	sd.setKeys(myPrivateKey,remotePublicKey);
}

//##################################################################
}
//##################################################################

